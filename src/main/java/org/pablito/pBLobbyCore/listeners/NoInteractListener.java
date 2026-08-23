package org.pablito.pBLobbyCore.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.EnumSet;
import java.util.Set;

/**
 * Listener for interaction prevention.
 * Blocks interaction with specific block types (chests, furnaces, etc.).
 *
 * <p>Optimized: uses EnumSet for O(1) material lookups.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class NoInteractListener implements Listener {

    private static final Set<Material> BLOCKED_MATERIALS = EnumSet.of(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.FURNACE,
            Material.CRAFTING_TABLE,
            Material.ANVIL,
            Material.ENDER_CHEST
    );

    private final MessageManager messageManager;

    public NoInteractListener(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material blockType = event.getClickedBlock().getType();

        if (BLOCKED_MATERIALS.contains(blockType)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(messageManager.getMessage("no-interact"));
        }
    }
}
