package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.pablito.pBLobbyCore.utils.MessageManager;

public class ItemDropListener implements Listener{

    private final MessageManager messageManager;

    public ItemDropListener(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);
        player.sendMessage(messageManager.getMessage("no-item-drop"));
    }
}
