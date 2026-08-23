package org.pablito.pBLobbyCore.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.pablito.pBLobbyCore.listeners.HidePlayersListener;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the hide-players system including per-player bypass states.
 * Uses ConcurrentHashMap for thread-safe bypass tracking.
 *
 * @author Pablito
 * @since 2.4
 */
public final class HidePlayersManager {

    public static final String PERM_MODULE = "pblcore.hideplayers.admin";
    public static final String PERM_BYPASS = "pblcore.hideplayers.bypass";
    public static final String PERM_BYPASS_TOGGLE = "pblcore.hideplayers.bypass.toggle";

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ModuleManager moduleManager;

    private volatile boolean enabled;
    private final Set<UUID> bypassEnabled = ConcurrentHashMap.newKeySet();

    private HidePlayersListener listener;

    public HidePlayersManager(JavaPlugin plugin, ConfigManager configManager, ModuleManager moduleManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager cannot be null");
        this.enabled = moduleManager.isEnabled(ModuleManager.HIDE_PLAYERS);
    }

    /**
     * Registers the hide players listener. Called during plugin enable.
     *
     * @param listener the listener to register
     */
    public void setListener(HidePlayersListener listener) {
        this.listener = listener;
    }

    // ========== Module Control ==========

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        moduleManager.setEnabled(ModuleManager.HIDE_PLAYERS, enabled);
        applyRules();
    }

    // ========== Bypass Control ==========

    /**
     * Checks if a player has the bypass permission.
     */
    public boolean canUseBypass(Player player) {
        return player != null && player.hasPermission(PERM_BYPASS);
    }

    /**
     * Checks if a player currently has bypass enabled.
     */
    public boolean isBypassEnabled(Player player) {
        return player != null && canUseBypass(player) && bypassEnabled.contains(player.getUniqueId());
    }

    /**
     * Sets the bypass state for a player.
     */
    public void setBypassEnabled(Player player, boolean enabled) {
        if (player == null) return;

        if (enabled) {
            bypassEnabled.add(player.getUniqueId());
        } else {
            bypassEnabled.remove(player.getUniqueId());
        }

        applyRules();
    }

    /**
     * Toggles bypass state for a player.
     *
     * @return the new state
     */
    public boolean toggleBypass(Player player) {
        boolean newState = !isBypassEnabled(player);
        setBypassEnabled(player, newState);
        return newState;
    }

    /**
     * Gets an unmodifiable view of all bypass UUIDs.
     */
    public Set<UUID> getAllBypassed() {
        return Collections.unmodifiableSet(bypassEnabled);
    }

    /**
     * Removes a player from the bypass set (called on disconnect).
     */
    public void removeBypass(Player player) {
        if (player != null) {
            bypassEnabled.remove(player.getUniqueId());
        }
    }

    // ========== Rules Application ==========

    /**
     * Applies hide/show rules for all online players.
     */
    public void applyRules() {
        if (listener != null) {
            listener.applyForAllOnline();
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                for (Player t : Bukkit.getOnlinePlayers()) {
                    if (p.equals(t)) continue;
                    p.showPlayer(plugin, t);
                }
            }
        }
    }

    /**
     * Reloads settings from config.
     */
    public void reload() {
        this.enabled = moduleManager.isEnabled(ModuleManager.HIDE_PLAYERS);
        bypassEnabled.clear();
    }
}
