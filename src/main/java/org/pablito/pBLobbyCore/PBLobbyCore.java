package org.pablito.pBLobbyCore;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.pablito.pBLobbyCore.commands.HideBypassCommand;
import org.pablito.pBLobbyCore.commands.HidePlayersModuleCommand;
import org.pablito.pBLobbyCore.commands.LockChatCommand;
import org.pablito.pBLobbyCore.commands.PBLobbyCoreCommand;
import org.pablito.pBLobbyCore.commands.WeatherLockCommand;
import org.pablito.pBLobbyCore.listeners.ChatLockListener;
import org.pablito.pBLobbyCore.listeners.HidePlayersListener;
import org.pablito.pBLobbyCore.listeners.MaintenanceListener;
import org.pablito.pBLobbyCore.listeners.PlugmanBlocker;
import org.pablito.pBLobbyCore.listeners.WeatherLockListener;
import org.pablito.pBLobbyCore.pvp.NoPvpListener;
import org.pablito.pBLobbyCore.pvp.PvpCommand;
import org.pablito.pBLobbyCore.pvp.PvpDiagCommand;
import org.pablito.pBLobbyCore.pvp.PvpToggleStore;
import org.pablito.pBLobbyCore.utils.MessageManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PBLobbyCore extends JavaPlugin {

    private FileConfiguration modulesConfig;
    private File modulesFile;

    private FileConfiguration whitelistConfig;
    private File whitelistFile;

    private MessageManager messageManager;

    private Location spawnLocation;

    private boolean chatLocked = false;

    private PvpToggleStore pvpToggleStore;

    private Metrics metrics;

    private boolean hidePlayersEnabled;
    private final Set<UUID> hideBypassEnabled = new HashSet<>();
    private HidePlayersListener hidePlayersListener;

    private boolean weatherLockEnabled;
    private FixedWeatherType fixedWeatherType = FixedWeatherType.CLEAR;
    private FixedTimeOfDay fixedTimeOfDay = FixedTimeOfDay.DAY;
    private BukkitTask weatherLockTask;

    public static final String PERM_HIDE_MODULE = "pblcore.hideplayers.admin";
    public static final String PERM_HIDE_BYPASS = "pblcore.hideplayers.bypass";
    public static final String PERM_HIDE_BYPASS_TOGGLE = "pblcore.hideplayers.bypass.toggle";

    public static final String PERM_WEATHERLOCK_ADMIN = "pblcore.weatherlock.admin";

    @Override
    public void onEnable() {
        saveDefaultConfig();

        updateConfigIfNeeded();

        this.messageManager = new MessageManager(this);

        setupModulesConfig();
        updateModulesIfNeeded();

        reloadWhitelistConfig();

        loadHidePlayersSettings();
        loadWeatherLockSettings();

        setupBStats();

        loadSpawnLocationFromConfig();

        PluginCommand coreCmd = getCommand("pblcore");
        if (coreCmd != null) {
            coreCmd.setExecutor(new PBLobbyCoreCommand(this, this.messageManager));
        } else {
            logSevere("log.command.missing_pblcore", "No se encontró el comando 'pblcore' en plugin.yml");
        }

        PluginManager pm = getServer().getPluginManager();

        if (getModulesConfig().getBoolean("modules.maintenance-mode", false)) {
            pm.registerEvents(new MaintenanceListener(this, this.messageManager), this);
            logInfo("log.listener.loaded_maintenance", "[Maintenance] Listener cargado (maintenance-mode).");
        }

        if (getModulesConfig().getBoolean("modules.chat-lock", true)) {
            pm.registerEvents(new ChatLockListener(this, this.messageManager), this);
            logInfo("log.listener.loaded_chatlock", "[ChatLock] Listener cargado.");
        }

        if (getModulesConfig().getBoolean("modules.no-pvp", true)) {
            boolean persistent = getConfig().getBoolean("no-pvp.save-toggle-on-restart", false);
            this.pvpToggleStore = new PvpToggleStore(this, persistent);
            pm.registerEvents(new NoPvpListener(this, this.messageManager, this.pvpToggleStore), this);
            logInfo("log.listener.loaded_nopvp", "[No-PvP] Listener cargado (opt-in /pvp).");
        }

        // Hide Players
        this.hidePlayersListener = new HidePlayersListener(this);
        pm.registerEvents(this.hidePlayersListener, this);

        // Weather lock
        pm.registerEvents(new WeatherLockListener(this), this);
        startWeatherLockTaskIfNeeded();
        applyWeatherLockNow();

        pm.registerEvents(new PlugmanBlocker(this), this);

        registerExtraCommands();

        applyHidePlayersRules();

        logInfo("log.plugin.enabled", "PBLobbyCore habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        if (weatherLockTask != null) {
            weatherLockTask.cancel();
            weatherLockTask = null;
        }
        logInfo("log.plugin.disabled", "PBLobbyCore deshabilitado.");
    }

    private void registerExtraCommands() {
        PluginCommand lockCmd = getCommand("lock");
        if (lockCmd != null) lockCmd.setExecutor(new LockChatCommand(this, this.messageManager));

        PluginCommand unlockCmd = getCommand("unlock");
        if (unlockCmd != null) unlockCmd.setExecutor(new LockChatCommand(this, this.messageManager));

        PluginCommand pvpCmd = getCommand("pvp");
        if (pvpCmd != null) {
            if (this.pvpToggleStore == null) {
                boolean persistent = getConfig().getBoolean("no-pvp.save-toggle-on-restart", false);
                this.pvpToggleStore = new PvpToggleStore(this, persistent);
            }
            pvpCmd.setExecutor(new PvpCommand(this, this.messageManager, this.pvpToggleStore));
        }

        PluginCommand diagCmd = getCommand("pvpdiag");
        if (diagCmd != null) diagCmd.setExecutor(new PvpDiagCommand(this));

        PluginCommand hidePlayersCmd = getCommand("hideplayers");
        if (hidePlayersCmd != null) hidePlayersCmd.setExecutor(new HidePlayersModuleCommand(this));

        PluginCommand hideBypassCmd = getCommand("hidebypass");
        if (hideBypassCmd != null) hideBypassCmd.setExecutor(new HideBypassCommand(this));

        PluginCommand weatherLockCmd = getCommand("weatherlock");
        if (weatherLockCmd != null) weatherLockCmd.setExecutor(new WeatherLockCommand(this));
    }

    public void saveSpawnLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            logWarn("log.spawn.save_failed_null", "No se pudo guardar el spawn: location o world nulos.");
            return;
        }

        getConfig().set("spawn.world", loc.getWorld().getName());
        getConfig().set("spawn.x", loc.getX());
        getConfig().set("spawn.y", loc.getY());
        getConfig().set("spawn.z", loc.getZ());
        getConfig().set("spawn.yaw", loc.getYaw());
        getConfig().set("spawn.pitch", loc.getPitch());
        saveConfig();

        this.spawnLocation = loc.clone();
    }

    public Location getSpawnLocation() {
        if (this.spawnLocation == null) {
            loadSpawnLocationFromConfig();
        }
        return this.spawnLocation;
    }

    private void loadSpawnLocationFromConfig() {
        String worldName = getConfig().getString("spawn.world");
        if (worldName == null) {
            this.spawnLocation = null;
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            logWarn("log.spawn.world_not_loaded", "El mundo configurado para el spawn no está cargado: " + worldName);
            this.spawnLocation = null;
            return;
        }

        double x = getConfig().getDouble("spawn.x");
        double y = getConfig().getDouble("spawn.y");
        double z = getConfig().getDouble("spawn.z");
        float yaw = (float) getConfig().getDouble("spawn.yaw");
        float pitch = (float) getConfig().getDouble("spawn.pitch");

        this.spawnLocation = new Location(world, x, y, z, yaw, pitch);
    }

    public void reloadPluginConfigs() {
        updateConfigIfNeeded();

        reloadModulesConfig();
        updateModulesIfNeeded();

        reloadWhitelistConfig();

        this.messageManager = new MessageManager(this);

        loadHidePlayersSettings();
        loadWeatherLockSettings();

        PluginCommand coreCmd = getCommand("pblcore");
        if (coreCmd != null) coreCmd.setExecutor(new PBLobbyCoreCommand(this, this.messageManager));

        PluginCommand lockCmd = getCommand("lock");
        if (lockCmd != null) lockCmd.setExecutor(new org.pablito.pBLobbyCore.commands.LockChatCommand(this, this.messageManager));

        PluginCommand unlockCmd = getCommand("unlock");
        if (unlockCmd != null) unlockCmd.setExecutor(new org.pablito.pBLobbyCore.commands.LockChatCommand(this, this.messageManager));

        PluginCommand pvpCmd = getCommand("pvp");
        if (pvpCmd != null) {
            if (this.pvpToggleStore == null) {
                boolean persistent = getConfig().getBoolean("no-pvp.save-toggle-on-restart", false);
                this.pvpToggleStore = new org.pablito.pBLobbyCore.pvp.PvpToggleStore(this, persistent);
            }
            pvpCmd.setExecutor(new org.pablito.pBLobbyCore.pvp.PvpCommand(this, this.messageManager, this.pvpToggleStore));
        }

        PluginCommand diagCmd = getCommand("pvpdiag");
        if (diagCmd != null) diagCmd.setExecutor(new org.pablito.pBLobbyCore.pvp.PvpDiagCommand(this));

        PluginCommand hidePlayersCmd = getCommand("hideplayers");
        if (hidePlayersCmd != null) hidePlayersCmd.setExecutor(new HidePlayersModuleCommand(this));

        PluginCommand hideBypassCmd = getCommand("hidebypass");
        if (hideBypassCmd != null) hideBypassCmd.setExecutor(new HideBypassCommand(this));

        PluginCommand weatherLockCmd = getCommand("weatherlock");
        if (weatherLockCmd != null) weatherLockCmd.setExecutor(new WeatherLockCommand(this));

        applyHidePlayersRules();

        startWeatherLockTaskIfNeeded();
        applyWeatherLockNow();

        logInfo("log.plugin.reloaded", "[PBLobbyCore] Configs recargadas y ejecutores re-asignados.");
    }

    public boolean isChatLocked() {
        return chatLocked;
    }

    public void setChatLocked(boolean locked) {
        this.chatLocked = locked;
        logInfo("log.chatlock.state", locked ? "[ChatLock] Chat bloqueado." : "[ChatLock] Chat desbloqueado.");
    }

    private void setupModulesConfig() {
        if (modulesFile == null) {
            modulesFile = new File(getDataFolder(), "modules.yml");
        }

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        if (!modulesFile.exists()) {
            try {
                saveResource("modules.yml", false);
            } catch (IllegalArgumentException ignored) {
            }

            if (!modulesFile.exists()) {
                try {
                    modulesFile.createNewFile();
                } catch (IOException e) {
                    getLogger().severe(tr("log.file.create_failed_modules", "No se pudo crear modules.yml: ") + e.getMessage());
                }
            }
        }

        modulesConfig = YamlConfiguration.loadConfiguration(modulesFile);
    }

    public FileConfiguration getModulesConfig() {
        if (modulesConfig == null) setupModulesConfig();
        return modulesConfig;
    }

    public void reloadModulesConfig() {
        modulesConfig = YamlConfiguration.loadConfiguration(modulesFile);
    }

    public void saveModulesConfig() {
        if (modulesConfig == null || modulesFile == null) return;
        try {
            modulesConfig.save(modulesFile);
        } catch (IOException e) {
            getLogger().severe(tr("log.file.save_failed_modules", "No se pudo guardar modules.yml: ") + e.getMessage());
        }
    }

    public FileConfiguration getWhitelistConfig() {
        if (whitelistConfig == null) reloadWhitelistConfig();
        return whitelistConfig;
    }

    public void reloadWhitelistConfig() {
        if (whitelistFile == null) {
            whitelistFile = new File(getDataFolder(), "whitelist.yml");
        }

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        if (!whitelistFile.exists()) {
            try {
                saveResource("whitelist.yml", false);
            } catch (IllegalArgumentException ignored) {
            }

            if (!whitelistFile.exists()) {
                try {
                    whitelistFile.createNewFile();
                } catch (IOException e) {
                    getLogger().severe(tr("log.file.create_failed_whitelist", "No se pudo crear whitelist.yml: ") + e.getMessage());
                }
            }
        }

        whitelistConfig = YamlConfiguration.loadConfiguration(whitelistFile);
    }

    public void saveWhitelistConfig() {
        if (whitelistConfig == null || whitelistFile == null) return;
        try {
            whitelistConfig.save(whitelistFile);
        } catch (IOException e) {
            getLogger().severe(tr("log.file.save_failed_whitelist", "No se pudo guardar whitelist.yml: ") + e.getMessage());
        }
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }

    public String tr(String key, String fallback) {
        if (this.messageManager == null || key == null || key.isEmpty()) return fallback;

        String msg = this.messageManager.getMessage(key);

        if (msg == null) return fallback;
        if (msg.contains("Message not found for path")) return fallback;

        return msg;
    }

    private void logInfo(String key, String fallback) {
        getLogger().info(tr(key, fallback));
    }

    private void logWarn(String key, String fallback) {
        getLogger().warning(tr(key, fallback));
    }

    private void logSevere(String key, String fallback) {
        getLogger().severe(tr(key, fallback));
    }

    /* =========================================================
     * bStats
     * ========================================================= */

    private void setupBStats() {
        try {
            int pluginId = 29428;
            this.metrics = new Metrics(this, pluginId);
        } catch (Throwable t) {
            logWarn("log.bstats.failed", "[bStats] No se pudo inicializar: " + t.getMessage());
        }
    }

    private boolean updateYamlFileIfNeeded(String resourceName, File targetFile, String versionPath) {
        if (targetFile == null) return false;

        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        if (!targetFile.exists()) {
            try {
                saveResource(resourceName, false);
                return true;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }

        YamlConfiguration current = YamlConfiguration.loadConfiguration(targetFile);

        if (getResource(resourceName) == null) return false;

        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(getResource(resourceName), StandardCharsets.UTF_8)
        );

        double currentVersion = current.getDouble(versionPath, 0.0);
        double defaultVersion = defaults.getDouble(versionPath, 0.0);

        if (defaultVersion <= currentVersion) return false;

        current.setDefaults(defaults);
        current.options().copyDefaults(true);
        current.set(versionPath, defaultVersion);

        try {
            current.save(targetFile);
            return true;
        } catch (IOException e) {
            getLogger().severe(tr("log.file.save_failed_modules", "No se pudo guardar " + resourceName + ": ") + e.getMessage());
            return false;
        }
    }

    private void updateConfigIfNeeded() {
        File cfg = new File(getDataFolder(), "config.yml");
        updateYamlFileIfNeeded("config.yml", cfg, "config-version");
        reloadConfig();
    }

    private void updateModulesIfNeeded() {
        if (modulesFile == null) modulesFile = new File(getDataFolder(), "modules.yml");
        updateYamlFileIfNeeded("modules.yml", modulesFile, "config-version"); // tu modules.yml usa config-version
        reloadModulesConfig();
    }

    private void loadHidePlayersSettings() {
        this.hidePlayersEnabled = getModulesConfig().getBoolean("modules.hide-players", false);
        this.hideBypassEnabled.clear();
    }

    public boolean isHidePlayersEnabled() {
        return hidePlayersEnabled;
    }

    public void setHidePlayersEnabled(boolean enabled) {
        this.hidePlayersEnabled = enabled;
        getModulesConfig().set("modules.hide-players", enabled);
        saveModulesConfig();
        applyHidePlayersRules();
    }

    public boolean canUseBypass(Player player) {
        return player != null && player.hasPermission(PERM_HIDE_BYPASS);
    }

    public boolean isBypassEnabled(Player player) {
        return player != null && canUseBypass(player) && hideBypassEnabled.contains(player.getUniqueId());
    }

    public void setBypassEnabled(Player player, boolean enabled) {
        if (player == null) return;

        if (enabled) hideBypassEnabled.add(player.getUniqueId());
        else hideBypassEnabled.remove(player.getUniqueId());

        applyHidePlayersRules();
    }

    public void applyHidePlayersRules() {
        if (this.hidePlayersListener != null) {
            this.hidePlayersListener.applyForAllOnline();
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                for (Player t : Bukkit.getOnlinePlayers()) {
                    if (p.equals(t)) continue;
                    p.showPlayer(this, t);
                }
            }
        }
    }

    public enum FixedWeatherType {
        CLEAR, RAIN, THUNDER;

        public static FixedWeatherType fromString(String s, FixedWeatherType def) {
            if (s == null) return def;
            try {
                return FixedWeatherType.valueOf(s.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return def;
            }
        }
    }

    public enum FixedTimeOfDay {
        DAY(6000L),
        SUNSET(12000L),
        NIGHT(18000L),
        SUNRISE(23000L);

        private final long time;

        FixedTimeOfDay(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public static FixedTimeOfDay fromString(String s, FixedTimeOfDay def) {
            if (s == null) return def;
            try {
                return FixedTimeOfDay.valueOf(s.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return def;
            }
        }
    }

    private void loadWeatherLockSettings() {
        this.weatherLockEnabled = getModulesConfig().getBoolean("modules.weather-lock", false);

        String w = getConfig().getString("weather-lock.weather", "CLEAR");
        String t = getConfig().getString("weather-lock.time", "DAY");

        this.fixedWeatherType = FixedWeatherType.fromString(w, FixedWeatherType.CLEAR);
        this.fixedTimeOfDay = FixedTimeOfDay.fromString(t, FixedTimeOfDay.DAY);
    }

    public boolean isWeatherLockEnabled() {
        return weatherLockEnabled;
    }

    public FixedWeatherType getFixedWeatherType() {
        return fixedWeatherType;
    }

    public FixedTimeOfDay getFixedTimeOfDay() {
        return fixedTimeOfDay;
    }

    public void setWeatherLockEnabled(boolean enabled) {
        this.weatherLockEnabled = enabled;
        getModulesConfig().set("modules.weather-lock", enabled);
        saveModulesConfig();

        startWeatherLockTaskIfNeeded();
        if (enabled) applyWeatherLockNow();
    }

    public void setWeatherLockWeather(FixedWeatherType type) {
        if (type == null) return;
        this.fixedWeatherType = type;
        getConfig().set("weather-lock.weather", type.name());
        saveConfig();
        if (weatherLockEnabled) applyWeatherLockNow();
    }

    public void setWeatherLockTime(FixedTimeOfDay tod) {
        if (tod == null) return;
        this.fixedTimeOfDay = tod;
        getConfig().set("weather-lock.time", tod.name());
        saveConfig();
        if (weatherLockEnabled) applyWeatherLockNow();
    }

    private void startWeatherLockTaskIfNeeded() {
        if (!weatherLockEnabled) {
            if (weatherLockTask != null) {
                weatherLockTask.cancel();
                weatherLockTask = null;
            }
            return;
        }

        if (weatherLockTask != null) return;

        weatherLockTask = Bukkit.getScheduler().runTaskTimer(this, this::applyWeatherLockNow, 20L, 20L * 60L);
    }

    public void applyWeatherLockNow() {
        if (!weatherLockEnabled) return;

        for (World w : Bukkit.getWorlds()) {
            w.setTime(fixedTimeOfDay.getTime());

            switch (fixedWeatherType) {
                case CLEAR -> {
                    w.setStorm(false);
                    w.setThundering(false);
                }
                case RAIN -> {
                    w.setStorm(true);
                    w.setThundering(false);
                }
                case THUNDER -> {
                    w.setStorm(true);
                    w.setThundering(true);
                }
            }

            w.setWeatherDuration(20 * 60 * 60);
            w.setThunderDuration(20 * 60 * 60);
        }
    }
}
