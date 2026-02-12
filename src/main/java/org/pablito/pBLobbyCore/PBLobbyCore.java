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
import org.pablito.pBLobbyCore.commands.HideBypassCommand;
import org.pablito.pBLobbyCore.commands.HidePlayersModuleCommand;
import org.pablito.pBLobbyCore.commands.PBLobbyCoreCommand;
import org.pablito.pBLobbyCore.commands.LockChatCommand;
import org.pablito.pBLobbyCore.listeners.ChatLockListener;
import org.pablito.pBLobbyCore.listeners.HidePlayersListener;
import org.pablito.pBLobbyCore.listeners.MaintenanceListener;
import org.pablito.pBLobbyCore.listeners.PlugmanBlocker;
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

    public static final String PERM_HIDE_MODULE = "pblcore.hideplayers.admin";
    public static final String PERM_HIDE_BYPASS = "pblcore.hideplayers.bypass";
    public static final String PERM_HIDE_BYPASS_TOGGLE = "pblcore.hideplayers.bypass.toggle";

    @Override
    public void onEnable() {
        saveDefaultConfig();

        updateConfigIfNeeded();

        this.messageManager = new MessageManager(this);

        setupModulesConfig();
        updateModulesIfNeeded();

        reloadWhitelistConfig();

        loadHidePlayersSettings();

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

        this.hidePlayersListener = new HidePlayersListener(this);
        pm.registerEvents(this.hidePlayersListener, this);

        pm.registerEvents(new PlugmanBlocker(this), this);

        registerExtraCommands();

        applyHidePlayersRules();

        logInfo("log.plugin.enabled", "PBLobbyCore habilitado correctamente.");
    }

    @Override
    public void onDisable() {
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

        // Reasignar ejecutores (manteniendo tu patrón actual)
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

        applyHidePlayersRules();

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
        updateYamlFileIfNeeded("modules.yml", modulesFile, "config-version");
        reloadModulesConfig();
    }

    private void loadHidePlayersSettings() {
        this.hidePlayersEnabled = getModulesConfig().getBoolean("modules.hide-players", false);
        this.hideBypassEnabled.clear(); // bypass solo en memoria
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
}
