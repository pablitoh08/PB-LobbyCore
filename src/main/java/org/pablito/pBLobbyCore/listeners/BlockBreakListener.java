package org.pablito.pBLobbyCore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Listener for block-break prevention.
 * Cancels all block-break events unless player is in the bypass list.
 *
 * <p>Optimized: uses HashSet for O(1) bypass lookups.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class BlockBreakListener implements Listener {

    private final MessageManager messageManager;
    private final Set<String> bypassPlayers;

    public BlockBreakListener(MessageManager messageManager, PBLobbyCore plugin) {
        this.messageManager = messageManager;
        this.bypassPlayers = new HashSet<>(plugin.getConfig().getStringList("blockbreak-bypass-players"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDestroyBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (bypassPlayers.contains(player.getName())) return;

        event.setCancelled(true);
        player.sendMessage(messageManager.getMessage("no-block-break"));
    }
}
