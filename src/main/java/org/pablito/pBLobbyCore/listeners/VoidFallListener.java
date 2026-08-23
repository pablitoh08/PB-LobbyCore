package org.pablito.pBLobbyCore.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.MessageManager;
import org.pablito.pBLobbyCore.managers.SpawnManager;

/**
 * Listener for void-fall teleport.
 * Teleports players back to spawn when they fall below Y=0.
 *
 * <p>Optimized: uses early returns and caches spawn location reference.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class VoidFallListener implements Listener {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;

    public VoidFallListener(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFallIntoVoid(PlayerMoveEvent event) {
        // Only check when player actually moves to a new block (Y changes)
        if (event.getFrom().getBlockY() == event.getTo().getBlockY()) return;

        Player player = event.getPlayer();
        if (player.getLocation().getY() >= 0) return;

        SpawnManager spawnManager = plugin.getSpawnManager();
        Location spawnLocation = spawnManager.getSpawnLocation();

        if (spawnLocation == null || !player.getWorld().equals(spawnLocation.getWorld())) return;

        player.teleport(spawnLocation);
        player.sendMessage(messageManager.getMessage("teleported-to-spawn"));
    }
}
