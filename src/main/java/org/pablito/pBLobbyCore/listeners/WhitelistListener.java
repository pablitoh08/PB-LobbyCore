package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.ConfigManager;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listener for server whitelist.
 * Kicks non-whitelisted players at pre-login.
 *
 * <p>Optimized: caches the whitelist set for fast O(1) lookups.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class WhitelistListener implements Listener {

    private final MessageManager messageManager;
    private volatile Set<String> allowedPlayersCache = new HashSet<>();

    public WhitelistListener(PBLobbyCore plugin, MessageManager messageManager) {
        this.messageManager = messageManager;
        reloadCache(plugin);
    }

    /**
     * Reloads the whitelist cache from config.
     */
    public void reloadCache(PBLobbyCore plugin) {
        ConfigManager cm = plugin.getConfigManager();
        List<String> allowed = cm.getWhitelistConfig().getStringList("whitelisted-players");
        this.allowedPlayersCache = new HashSet<>(allowed);
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!allowedPlayersCache.contains(event.getName())) {
            event.setLoginResult(Result.KICK_WHITELIST);
            event.setKickMessage(messageManager.getMessage("not-whitelisted"));
        }
    }
}
