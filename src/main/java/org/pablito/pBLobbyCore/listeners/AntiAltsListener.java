package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.entity.Player;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AntiAltsListener implements Listener {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;
    private final ConcurrentMap<InetAddress, UUID> ipToPlayerMap = new ConcurrentHashMap<>();

    public AntiAltsListener(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        InetAddress playerIp = event.getAddress();
        String playerName = event.getName();

        List<String> exceptions = plugin.getConfig().getStringList("alt-ip-exceptions");
        if (exceptions.contains(playerIp.getHostAddress())) {
            return;
        }

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