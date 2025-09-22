package org.pablito.pBLobbyCore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.pablito.pBLobbyCore.commands.LockChatCommand;
import org.pablito.pBLobbyCore.commands.PBLobbyCoreCommand;
import org.pablito.pBLobbyCore.commands.PBLobbyCoreTabCompleter;
import org.pablito.pBLobbyCore.listeners.*;
import org.pablito.pBLobbyCore.utils.ScoreboardManager;
import org.pablito.pBLobbyCore.utils.MessageManager;
import org.pablito.pBLobbyCore.utils.TabManager;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PBLobbyCore extends JavaPlugin {

    private FileConfiguration modulesConfig;
    private File modulesFile;
    private FileConfiguration whitelistConfig;
    private File whitelistFile;
    private MessageManager messageManager;
    private BukkitTask maintenanceCheckerTask;
    private ScoreboardManager scoreboardManager;
    private TabManager tabManager;

    private boolean chatLocked = false;

    @Override
    public void onEnable() {
        setupPlugin();
        startMaintenanceChecker();

        String version = getDescription().getVersion();
        getLogger().info("[PBLobbyCore] PB-LobbyCore v" + version + " plugin enabled.");
        getLogger().info("[PBLobbyCore] Developed by Pablohs08");
    }

    @Override
    public void onDisable() {
        if (maintenanceCheckerTask != null) {
            maintenanceCheckerTask.cancel();
        }
        if (scoreboardManager != null) {
            scoreboardManager.stopScoreboardUpdater();
        }
        getLogger().info("[PBLobbyCore] PB-LobbyCore plugin disabled.");
        getLogger().info("[Pablito] Developed by Pablohs08");
    }

    public void reloadPluginConfigs() {
        unloadListeners();
        if (maintenanceCheckerTask != null) {
            maintenanceCheckerTask.cancel();
        }
        if (scoreboardManager != null) {
            scoreboardManager.stopScoreboardUpdater();
            scoreboardManager = null;
        }
        if (tabManager != null) {
            tabManager.stopTabUpdater();
            tabManager = null;
        }

        this.reloadConfig();
        this.loadModulesConfig();
        this.loadWhitelistConfig();

        setupPlugin();
        startMaintenanceChecker();
    }

    private void setupPlugin() {
        loadAllConfigs();

        this.messageManager = new MessageManager(this);
        getServer().getPluginManager().registerEvents(new PlugmanBlocker(this), this);

        Objects.requireNonNull(this.getCommand("pblcore")).setExecutor(new PBLobbyCoreCommand(this, this.messageManager));
        Objects.requireNonNull(this.getCommand("pblcore")).setTabCompleter(new PBLobbyCoreTabCompleter(this));

        Objects.requireNonNull(this.getCommand("lock")).setExecutor(new LockChatCommand(this, this.messageManager));
        Objects.requireNonNull(this.getCommand("unlock")).setExecutor(new LockChatCommand(this, this.messageManager));

        loadModules();
    }

    private void startMaintenanceChecker() {
        this.maintenanceCheckerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            boolean maintenanceMode = getModulesConfig().getBoolean("modules.maintenance-mode");

            if (maintenanceMode) {
                List<String> whitelist = getWhitelistConfig().getStringList("players");
                String kickMessage = getMessageManager().getMessage("maintenance-kick-message");

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("pblcore.admin") && !whitelist.contains(player.getName())) {
                        Bukkit.getScheduler().runTask(this, () -> player.kickPlayer(kickMessage));
                    }
                }
            }
        }, 0L, 100L);
    }

    private void unloadListeners() {
        HandlerList.unregisterAll(this);
    }

    private void loadAllConfigs() {
        this.saveDefaultConfig();
        this.loadModulesConfig();
        this.loadWhitelistConfig();
        this.saveLanguageFiles();
    }

    public void loadWhitelistConfig() {
        this.whitelistFile = new File(getDataFolder(), "whitelist.yml");
        if(!this.whitelistFile.exists()) {
            this.saveResource("whitelist.yml", false);
        }
        this.whitelistConfig = YamlConfiguration.loadConfiguration(this.whitelistFile);
    }

    public FileConfiguration getWhitelistConfig() {
        return this.whitelistConfig;
    }

    public void saveWhitelistConfig() {
        try {
            this.whitelistConfig.save(this.whitelistFile);
        } catch (IOException e) {
            getLogger().severe("Whitelist configuration could not be saved: " + e.getMessage());
        }
    }

    private void saveLanguageFiles() {
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File esFile = new File(langFolder, "es.yml");
        if (!esFile.exists()) {
            saveResource("lang/es.yml", false);
        }

        File enFile = new File(langFolder, "en.yml");
        if (!enFile.exists()) {
            saveResource("lang/en.yml", false);
        }

        File frFile = new File(langFolder, "fr.yml");
        if(!frFile.exists()) {
            saveResource("lang/fr.yml", false);
        }

        File deFile = new File(langFolder, "de.yml");
        if(!deFile.exists()) {
            saveResource("lang/de.yml", false);
        }

        File plFile = new File(langFolder, "pl.yml");
        if(!plFile.exists()) {
            saveResource("lang/pl.yml", false);
        }

        File prFile = new File(langFolder, "pr.yml");
        if(!prFile.exists()) {
            saveResource("lang/pr.yml", false);
        }

        File ruFile = new File(langFolder, "ru.yml");
        if(!ruFile.exists()) {
            saveResource("lang/ru.yml", false);
        }

        File ukFile = new File(langFolder, "uk.yml");
        if(!ukFile.exists()) {
            saveResource("lang/uk.yml", false);
        }
    }

    private void loadModules() {
        boolean isWhitelistEnabled = modulesConfig.getBoolean("modules.whitelist", false);
        boolean isItemDropEnabled = modulesConfig.getBoolean("modules.no-item-drop", false);
        boolean isBreakBlockEnabled = modulesConfig.getBoolean("modules.no-block-break", false);
        boolean isBlockPlaceEnabled = modulesConfig.getBoolean("modules.no-block-place", false);
        boolean isNoDamageEnabled = modulesConfig.getBoolean("modules.no-damage", false);
        boolean isPlayerJoinQuitEnabled = modulesConfig.getBoolean("modules.join-quit-messages", false);
        boolean isCommandBlockerEnabled = modulesConfig.getBoolean("modules.command-blocker", false);
        boolean isMobSpawnerEnabled = modulesConfig.getBoolean("modules.no-mobs", false);
        boolean isPvPEnabled = modulesConfig.getBoolean("modules.no-pvp", false);
        boolean isVoidFallTeleportEnabled = modulesConfig.getBoolean("modules.void-fall-teleport", false);
        boolean isAltsEnabled = modulesConfig.getBoolean("modules.alt", false);
        boolean isMaintenanceModeEnabled = modulesConfig.getBoolean("modules.maintenance-mode", false);
        boolean isScoreboardEnabled = modulesConfig.getBoolean("modules.scoreboard", false);
        boolean isTabEnabled = modulesConfig.getBoolean("modules.tab", false);
        boolean isChatLockEnabled = modulesConfig.getBoolean("modules.chat-lock", false);


        if (isWhitelistEnabled) {
            getServer().getPluginManager().registerEvents(new WhitelistListener(this, messageManager), this);
        }
        if (isItemDropEnabled) {
            getServer().getPluginManager().registerEvents(new ItemDropListener(messageManager), this);
        }
        if (isBreakBlockEnabled) {
            getServer().getPluginManager().registerEvents(new BlockBreakListener(messageManager, this), this);
        }
        if (isBlockPlaceEnabled) {
            getServer().getPluginManager().registerEvents(new BlockPlaceListener(messageManager, this), this);
        }
        if (isNoDamageEnabled) {
            getServer().getPluginManager().registerEvents(new NoDamageListener(), this);
        }
        if (isPlayerJoinQuitEnabled) {
            getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(messageManager), this);
        }
        if (isCommandBlockerEnabled) {
            getServer().getPluginManager().registerEvents(new CommandBlockerListener(this.getConfig(), messageManager), this);
        }
        if (isMobSpawnerEnabled) {
            getServer().getPluginManager().registerEvents(new MobSpawnerListener(), this);
        }
        if(isPvPEnabled) {
            getServer().getPluginManager().registerEvents(new PvpListener(messageManager, this.getConfig()), this);
        }
        if (isVoidFallTeleportEnabled) {
            getServer().getPluginManager().registerEvents(new VoidFallListener(this, messageManager), this);
        }
        if (isAltsEnabled) {
            getServer().getPluginManager().registerEvents(new AntiAltsListener(this, messageManager), this);
        }
        if (isMaintenanceModeEnabled) {
            getServer().getPluginManager().registerEvents(new MaintenanceListener(this), this);
        }
        if (isScoreboardEnabled) {
            if (!this.getConfig().contains("scoreboard")) {
                getLogger().severe("The Scoreboard module is enabled, but the configuration was not found in config.yml. Please check your file!");
            } else {
                this.scoreboardManager = new ScoreboardManager(this);
                getServer().getPluginManager().registerEvents(new ScoreboardListener(this, this.scoreboardManager), this);
                this.scoreboardManager.startScoreboardUpdater();
            }
        }
        if (isTabEnabled) {
            this.tabManager = new TabManager(this);
            this.tabManager.startTabUpdater();
        }
        if (isChatLockEnabled) {
            getServer().getPluginManager().registerEvents(new ChatLockListener(this, messageManager), this);
        }
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void saveSpawnLocation(Location location) {
        FileConfiguration config = getConfig();
        config.set("spawn.world", location.getWorld().getName());
        config.set("spawn.x", location.getX());
        config.set("spawn.y", location.getY());
        config.set("spawn.z", location.getZ());
        config.set("spawn.yaw", location.getYaw());
        config.set("spawn.pitch", location.getPitch());
        saveConfig();
    }

    public Location getSpawnLocation() {
        FileConfiguration config = getConfig();
        if (!config.contains("spawn.world")) {
            return null;
        }

        return new Location(
                Bukkit.getWorld(config.getString("spawn.world")),
                config.getDouble("spawn.x"),
                config.getDouble("spawn.y"),
                config.getDouble("spawn.z"),
                (float) config.getDouble("spawn.yaw"),
                (float) config.getDouble("spawn.pitch")
        );
    }

    public FileConfiguration getModulesConfig() {
        return this.modulesConfig;
    }

    public void saveModulesConfig() {
        try {
            this.modulesConfig.save(this.modulesFile);
        } catch (IOException e) {
            getLogger().severe("The modules file could not be saved: " + e.getMessage());
        }
    }

    private void loadModulesConfig() {
        this.modulesFile = new File(getDataFolder(), "modules.yml");
        if (!this.modulesFile.exists()) {
            this.saveResource("modules.yml", false);
        }
        this.modulesConfig = YamlConfiguration.loadConfiguration(this.modulesFile);
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        FileConfiguration config = getConfig();
        if (!config.contains("alt-ip-exceptions")) {
            config.set("alt-ip-exceptions", new ArrayList<String>());
            saveConfig();
        }
    }

    public boolean isChatLocked() {
        return chatLocked;
    }

    public void setChatLocked(boolean chatLocked) {
        this.chatLocked = chatLocked;
    }
}