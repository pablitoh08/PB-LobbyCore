package org.pablito.pBLobbyCore;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.pablito.pBLobbyCore.commands.*;
import org.pablito.pBLobbyCore.listeners.*;
import org.pablito.pBLobbyCore.managers.*;
import org.pablito.pBLobbyCore.pvp.NoPvpListener;
import org.pablito.pBLobbyCore.pvp.PvpCommand;
import org.pablito.pBLobbyCore.pvp.PvpDiagCommand;
import org.pablito.pBLobbyCore.utils.ScoreboardManager;

import java.util.Objects;

/**
 * Main plugin class for PB-LobbyCore.
 * Acts as a lightweight coordinator delegating to specialized managers.
 *
 * @author Pablito
 * @since 2.4
 */
public class PBLobbyCore extends JavaPlugin {

    private static PBLobbyCore instance;

    // Managers
    private ConfigManager configManager;
    private ModuleManager moduleManager;
    private MessageManager messageManager;
    private SpawnManager spawnManager;
    private WeatherManager weatherManager;
    private HidePlayersManager hidePlayersManager;
    private ChatManager chatManager;
    private PvPManager pvpManager;
    private CommandManager commandManager;

    // ========== Singleton ==========

    public static PBLobbyCore getInstance() {
        return instance;
    }

    // ========== Lifecycle ==========

    @Override
    public void onEnable() {
        instance = this;

        try {
            initializeManagers();
            registerListeners();
            registerCommands();
            setupBStats();

            getLogger().info("PBLobbyCore enabled successfully.");
        } catch (Exception e) {
            getLogger().severe("Failed to enable PBLobbyCore: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        shutdownManagers();
        instance = null;
        getLogger().info("PBLobbyCore disabled.");
    }

    // ========== Initialization ==========

    private void initializeManagers() {
        this.configManager = new ConfigManager(this);
        this.configManager.initialize();

        this.moduleManager = new ModuleManager(configManager);
        this.messageManager = new MessageManager(this, configManager);
        this.spawnManager = new SpawnManager(this, configManager);
        this.chatManager = new ChatManager(this, configManager, moduleManager);
        this.pvpManager = new PvPManager(this, configManager, moduleManager);
        this.weatherManager = new WeatherManager(this, configManager, moduleManager);
        this.hidePlayersManager = new HidePlayersManager(this, configManager, moduleManager);
        this.commandManager = new CommandManager(this);
    }

    private void shutdownManagers() {
        if (weatherManager != null) weatherManager.shutdown();
        getServer().getScheduler().cancelTasks(this);
    }

    // ========== Listener Registration ==========

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        // Always registered (module-controlled internally)
        registerMaintenanceListener(pm);
        registerChatLockListener(pm);
        registerNoPvpListener(pm);
        registerHidePlayersListener(pm);
        registerWeatherLockListener(pm);
        registerPlugmanBlocker(pm);

        // Conditionally registered based on modules
        if (moduleManager.isEnabled(ModuleManager.NO_BLOCK_BREAK))
            pm.registerEvents(new BlockBreakListener(messageManager, this), this);
        if (moduleManager.isEnabled(ModuleManager.NO_BLOCK_PLACE))
            pm.registerEvents(new BlockPlaceListener(messageManager, this), this);
        if (moduleManager.isEnabled(ModuleManager.NO_ITEM_DROP))
            pm.registerEvents(new ItemDropListener(messageManager), this);
        if (moduleManager.isEnabled(ModuleManager.NO_INTERACT))
            pm.registerEvents(new NoInteractListener(messageManager), this);
        if (moduleManager.isEnabled(ModuleManager.NO_DAMAGE))
            pm.registerEvents(new NoDamageListener(), this);
        if (moduleManager.isEnabled(ModuleManager.NO_MOBS))
            pm.registerEvents(new MobSpawnerListener(), this);
        if (moduleManager.isEnabled(ModuleManager.COMMAND_BLOCKER))
            pm.registerEvents(new CommandBlockerListener(configManager.getConfig().getStringList("blocked_commands"), messageManager), this);
        if (moduleManager.isEnabled(ModuleManager.VOID_FALL_TELEPORT))
            pm.registerEvents(new VoidFallListener(this, messageManager), this);
        if (moduleManager.isEnabled(ModuleManager.NO_ALTS))
            pm.registerEvents(new AntiAltsListener(this, messageManager), this);
        if (moduleManager.isEnabled(ModuleManager.JOIN_QUIT_MESSAGES))
            pm.registerEvents(new PlayerJoinQuitListener(messageManager), this);
        if (moduleManager.isEnabled(ModuleManager.WHITELIST))
            pm.registerEvents(new WhitelistListener(this, messageManager), this);
        if (moduleManager.isEnabled(ModuleManager.SCOREBOARD))
            pm.registerEvents(new ScoreboardListener(new ScoreboardManager(this)), this);
    }

    private void registerMaintenanceListener(PluginManager pm) {
        if (moduleManager.isEnabled(ModuleManager.MAINTENANCE_MODE)) {
            pm.registerEvents(new MaintenanceListener(this, messageManager), this);
        }
    }

    private void registerChatLockListener(PluginManager pm) {
        if (moduleManager.isEnabled(ModuleManager.CHAT_LOCK)) {
            pm.registerEvents(new ChatLockListener(this, messageManager), this);
        }
    }

    private void registerNoPvpListener(PluginManager pm) {
        if (moduleManager.isEnabled(ModuleManager.NO_PVP)) {
            pm.registerEvents(new NoPvpListener(this, messageManager, pvpManager.getToggleStore()), this);
        }
    }

    private void registerHidePlayersListener(PluginManager pm) {
        HidePlayersListener listener = new HidePlayersListener(this);
        hidePlayersManager.setListener(listener);
        pm.registerEvents(listener, this);
        hidePlayersManager.applyRules();
    }

    private void registerWeatherLockListener(PluginManager pm) {
        pm.registerEvents(new WeatherLockListener(this), this);
        weatherManager.reload();
    }

    private void registerPlugmanBlocker(PluginManager pm) {
        pm.registerEvents(new PlugmanBlocker(this), this);
    }

    // ========== Command Registration ==========

    private void registerCommands() {
        PluginCommand coreCmd = getCommand("pblcore");
        if (coreCmd != null) {
            coreCmd.setExecutor(new PBLobbyCoreCommand(this, messageManager));
        }

        commandManager.registerCommand("lock", new LockChatCommand(this, messageManager));
        commandManager.registerCommand("unlock", new LockChatCommand(this, messageManager));
        commandManager.registerCommand("pvp", new PvpCommand(this, messageManager, pvpManager.getToggleStore()));
        commandManager.registerCommand("pvpdiag", new PvpDiagCommand());
        commandManager.registerCommand("hideplayers", new HidePlayersModuleCommand(this));
        commandManager.registerCommand("hidebypass", new HideBypassCommand(this));
        commandManager.registerCommand("weatherlock", new WeatherLockCommand(this));
    }

    // ========== Reload ==========

    /**
     * Reloads all plugin configurations and re-registers commands.
     */
    public void reloadPluginConfigs() {
        configManager.reloadAll();
        moduleManager.reloadCache();
        messageManager.loadMessages();
        spawnManager.loadFromConfig();
        hidePlayersManager.reload();
        weatherManager.reload();

        // Re-register commands with fresh references
        registerCommands();

        hidePlayersManager.applyRules();
    }

    // ========== bStats ==========

    private void setupBStats() {
        try {
            int pluginId = 29428;
            new Metrics(this, pluginId);
        } catch (Throwable t) {
            getLogger().warning("Failed to initialize bStats: " + t.getMessage());
        }
    }

    // ========== Manager Accessors (for backward compatibility) ==========

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public WeatherManager getWeatherManager() {
        return weatherManager;
    }

    public HidePlayersManager getHidePlayersManager() {
        return hidePlayersManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public PvPManager getPvpManager() {
        return pvpManager;
    }

    // ========== Backward Compatibility Methods ==========
    // These delegate to the new managers so existing listeners still compile.

    /** @deprecated Use {@link #getModuleManager()} instead */
    @Deprecated
    public org.bukkit.configuration.file.FileConfiguration getModulesConfig() {
        return configManager.getModulesConfig();
    }

    /** @deprecated Use {@link ConfigManager#saveModulesConfig()} instead */
    @Deprecated
    public void saveModulesConfig() {
        configManager.saveModulesConfig();
    }

    /** @deprecated Use {@link #getChatManager()} instead */
    @Deprecated
    public boolean isChatLocked() {
        return chatManager.isChatLocked();
    }

    /** @deprecated Use {@link #getChatManager()} instead */
    @Deprecated
    public void setChatLocked(boolean locked) {
        chatManager.setChatLocked(locked);
    }

    /** @deprecated Use {@link #getSpawnManager()} instead */
    @Deprecated
    public void saveSpawnLocation(org.bukkit.Location loc) {
        spawnManager.saveSpawnLocation(loc);
    }

    /** @deprecated Use {@link #getSpawnManager()} instead */
    @Deprecated
    public org.bukkit.Location getSpawnLocation() {
        return spawnManager.getSpawnLocation();
    }

    /** @deprecated Use {@link #getHidePlayersManager()} instead */
    @Deprecated
    public boolean isHidePlayersEnabled() {
        return hidePlayersManager.isEnabled();
    }

    /** @deprecated Use {@link #getHidePlayersManager()} instead */
    @Deprecated
    public void setHidePlayersEnabled(boolean enabled) {
        hidePlayersManager.setEnabled(enabled);
    }

    /** @deprecated Use {@link #getHidePlayersManager()} instead */
    @Deprecated
    public boolean canUseBypass(Player player) {
        return hidePlayersManager.canUseBypass(player);
    }

    /** @deprecated Use {@link #getHidePlayersManager()} instead */
    @Deprecated
    public boolean isBypassEnabled(Player player) {
        return hidePlayersManager.isBypassEnabled(player);
    }

    /** @deprecated Use {@link #getHidePlayersManager()} instead */
    @Deprecated
    public void setBypassEnabled(Player player, boolean enabled) {
        hidePlayersManager.setBypassEnabled(player, enabled);
    }

    /** @deprecated Use {@link #getHidePlayersManager()} instead */
    @Deprecated
    public void applyHidePlayersRules() {
        hidePlayersManager.applyRules();
    }

    /** @deprecated Use {@link #getWeatherManager()} instead */
    @Deprecated
    public boolean isWeatherLockEnabled() {
        return weatherManager.isEnabled();
    }

    /** @deprecated Use {@link #getWeatherManager()} instead */
    @Deprecated
    public void setWeatherLockEnabled(boolean enabled) {
        weatherManager.setEnabled(enabled);
    }

    /** @deprecated Use {@link #getWeatherManager()} instead */
    @Deprecated
    public void setWeatherLockWeather(WeatherManager.FixedWeatherType type) {
        weatherManager.setWeatherType(type);
    }

    /** @deprecated Use {@link #getWeatherManager()} instead */
    @Deprecated
    public void setWeatherLockTime(WeatherManager.FixedTimeOfDay tod) {
        weatherManager.setTimeOfDay(tod);
    }

    /** @deprecated Use {@link #getWeatherManager()} instead */
    @Deprecated
    public WeatherManager.FixedWeatherType getFixedWeatherType() {
        return weatherManager.getWeatherType();
    }

    /** @deprecated Use {@link #getWeatherManager()} instead */
    @Deprecated
    public WeatherManager.FixedTimeOfDay getFixedTimeOfDay() {
        return weatherManager.getTimeOfDay();
    }

    /** @deprecated Use {@link #getWeatherManager()} instead */
    @Deprecated
    public void applyWeatherLockNow() {
        weatherManager.applyNow();
    }

    /** @deprecated Use {@link MessageManager#getMessage(String)} instead */
    @Deprecated
    public String tr(String key, String fallback) {
        if (messageManager == null || key == null || key.isEmpty()) return fallback;
        String msg = messageManager.getMessage(key);
        if (msg == null) return fallback;
        if (msg.contains("Message not found for path")) return fallback;
        return msg;
    }
}
