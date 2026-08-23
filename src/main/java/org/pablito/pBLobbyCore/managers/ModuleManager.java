package org.pablito.pBLobbyCore.managers;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages module enable/disable states from modules.yml.
 * Provides fast lookups via cached boolean map.
 *
 * <p>All methods are thread-safe for read operations.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public final class ModuleManager {

    private final ConfigManager configManager;
    private volatile Map<String, Boolean> moduleCache;

    /**
     * Known module names as defined in modules.yml.
     */
    public static final String WHITELIST = "whitelist";
    public static final String NO_ITEM_DROP = "no-item-drop";
    public static final String NO_BLOCK_BREAK = "no-block-break";
    public static final String NO_BLOCK_PLACE = "no-block-place";
    public static final String NO_INTERACT = "no-interact";
    public static final String NO_DAMAGE = "no-damage";
    public static final String JOIN_QUIT_MESSAGES = "join-quit-messages";
    public static final String COMMAND_BLOCKER = "command-blocker";
    public static final String NO_MOBS = "no-mobs";
    public static final String NO_PVP = "no-pvp";
    public static final String VOID_FALL_TELEPORT = "void-fall-teleport";
    public static final String NO_ALTS = "no-alts";
    public static final String MAINTENANCE_MODE = "maintenance-mode";
    public static final String SCOREBOARD = "scoreboard";
    public static final String TAB = "tab";
    public static final String CHAT_LOCK = "chat-lock";
    public static final String CHAT_ANNOUNCEMENTS = "chat-announcements";
    public static final String HIDE_PLAYERS = "hide-players";
    public static final String WEATHER_LOCK = "weather-lock";

    public ModuleManager(ConfigManager configManager) {
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        reloadCache();
    }

    /**
     * Checks if a module is enabled.
     *
     * @param module the module name (use constants from this class)
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled(String module) {
        return moduleCache.getOrDefault(module, false);
    }

    /**
     * Sets a module's enabled state and persists it.
     *
     * @param module the module name
     * @param enabled the new state
     */
    public void setEnabled(String module, boolean enabled) {
        configManager.getModulesConfig().set("modules." + module, enabled);
        configManager.saveModulesConfig();
        moduleCache = new HashMap<>(moduleCache);
        moduleCache.put(module, enabled);
    }

    /**
     * Reloads the module cache from disk.
     */
    public void reloadCache() {
        FileConfiguration modulesConfig = configManager.getModulesConfig();
        Map<String, Boolean> newCache = new HashMap<>();

        for (String module : getAllModuleNames()) {
            newCache.put(module, modulesConfig.getBoolean("modules." + module, false));
        }

        this.moduleCache = Collections.unmodifiableMap(newCache);
    }

    /**
     * Returns an unmodifiable view of all module states.
     *
     * @return map of module name to enabled state
     */
    public Map<String, Boolean> getAllModules() {
        return moduleCache;
    }

    private String[] getAllModuleNames() {
        return new String[]{
                WHITELIST, NO_ITEM_DROP, NO_BLOCK_BREAK, NO_BLOCK_PLACE,
                NO_INTERACT, NO_DAMAGE, JOIN_QUIT_MESSAGES, COMMAND_BLOCKER,
                NO_MOBS, NO_PVP, VOID_FALL_TELEPORT, NO_ALTS,
                MAINTENANCE_MODE, SCOREBOARD, TAB, CHAT_LOCK,
                CHAT_ANNOUNCEMENTS, HIDE_PLAYERS, WEATHER_LOCK
        };
    }
}
