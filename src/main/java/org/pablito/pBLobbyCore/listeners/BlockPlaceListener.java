package org.pablito.pBLobbyCore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Listener for block-place prevention.
 * Cancels all block-place events unless player is in the bypass list.
 *
 * <p>Optimized: uses HashSet for O(1) bypass lookups.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class BlockPlaceListener implements Listener {

    private final MessageManager messageManager;
    private final Set<String> bypassPlayers;

    public BlockPlaceListener(MessageManager messageManager, PBLobbyCore plugin) {
        this.messageManager = messageManager;
        this.bypassPlayers = new HashSet<>(plugin.getConfig().getStringList("blockplace-bypass-players"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (bypassPlayers.contains(player.getName())) return;

        event.setCancelled(true);
        player.sendMessage(messageManager.getMessage("no-block-place"));
    }
}
