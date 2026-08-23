package org.pablito.pBLobbyCore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Listener for anti-alt detection.
 * Blocks duplicate accounts from the same IP address.
 *
 * <p>Optimized: uses HashSet for O(1) exception lookups.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class AntiAltsListener implements Listener {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;
    private final ConcurrentMap<InetAddress, UUID> ipToPlayerMap = new ConcurrentHashMap<>();
    private volatile Set<String> exceptionsCache = new HashSet<>();

    public AntiAltsListener(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        reloadExceptionsCache();
    }

    /**
     * Reloads the exceptions cache from config.
     */
    public void reloadExceptionsCache() {
        this.exceptionsCache = new HashSet<>(
                plugin.getConfig().getStringList("alt-ip-exceptions"));
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        InetAddress playerIp = event.getAddress();

        if (exceptionsCache.contains(playerIp.getHostAddress())) return;

        UUID existingPlayerUUID = ipToPlayerMap.get(playerIp);
        if (existingPlayerUUID != null) {
            Player existingPlayer = plugin.getServer().getPlayer(existingPlayerUUID);

            if (existingPlayer != null && existingPlayer.isOnline()) {
                String kickMessage = messageManager.getMessage("kick-message-same-ip")
                        .replace("%player_name%", existingPlayer.getName());
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage);
            }
        } else {
            ipToPlayerMap.put(playerIp, event.getUniqueId());
        }
    }
}
