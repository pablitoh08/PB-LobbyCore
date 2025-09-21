package org.pablito.pBLobbyCore.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

public class VoidFallListener implements Listener {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;

    public VoidFallListener(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onPlayerFallIntoVoid(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location spawnLocation = plugin.getSpawnLocation();

        if (spawnLocation == null || !player.getWorld().equals(spawnLocation.getWorld())) {
            return;
        }

        if (player.getLocation().getY() < 0) {
            player.teleport(spawnLocation);
            player.sendMessage(messageManager.getMessage("teleported-to-spawn"));
        }
    }
}