package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.ConfigManager;
import org.pablito.pBLobbyCore.managers.ModuleManager;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listener for maintenance mode.
 * Kicks non-whitelisted players when maintenance is enabled.
 *
 * <p>Optimized: caches the whitelist set for fast O(1) lookups.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class MaintenanceListener implements Listener {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;

    /** Cached whitelist for fast lookup. */
    private volatile Set<String> whitelistCache = new HashSet<>();

    public MaintenanceListener(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        reloadWhitelistCache();
    }

    /**
     * Reloads the whitelist cache from config.
     */
    public void reloadWhitelistCache() {
        ConfigManager cm = plugin.getConfigManager();
        List<String> whitelist = cm.getWhitelistConfig().getStringList("players");
        this.whitelistCache = new HashSet<>(whitelist);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!plugin.getModuleManager().isEnabled(ModuleManager.MAINTENANCE_MODE)) return;

        if (event.getPlayer().hasPermission("pblcore.admin")) return;

        if (!whitelistCache.contains(event.getPlayer().getName())) {
            String kickMessage = messageManager.getMessage("maintenance-kick-message");
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
        }
    }
}
