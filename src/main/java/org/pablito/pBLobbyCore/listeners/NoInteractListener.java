package org.pablito.pBLobbyCore.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.pablito.pBLobbyCore.utils.MessageManager;

public class NoInteractListener implements Listener {

    private final MessageManager messageManager;

    public NoInteractListener(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Material blockType = event.getClickedBlock().getType();

        if (blockType == Material.CHEST ||
                blockType == Material.TRAPPED_CHEST ||
                blockType == Material.FURNACE ||
                blockType == Material.CRAFTING_TABLE ||
                blockType == Material.ANVIL ||
                blockType == Material.ENDER_CHEST) {

            event.setCancelled(true);
            player.sendMessage(messageManager.getMessage("no-interact"));
        }
    }
}