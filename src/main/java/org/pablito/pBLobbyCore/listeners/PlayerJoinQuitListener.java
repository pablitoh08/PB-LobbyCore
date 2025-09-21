package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.pablito.pBLobbyCore.utils.MessageManager;

public class PlayerJoinQuitListener implements Listener {

    private final MessageManager messageManager;

    public PlayerJoinQuitListener(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String joinMessage = messageManager.getMessage("join_message");
        joinMessage = joinMessage.replace("%player%", event.getPlayer().getName());
        event.setJoinMessage(joinMessage);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String quitMessage = messageManager.getMessage("quit_message");
        quitMessage = quitMessage.replace("%player%", event.getPlayer().getName());
        event.setQuitMessage(quitMessage);
    }
}