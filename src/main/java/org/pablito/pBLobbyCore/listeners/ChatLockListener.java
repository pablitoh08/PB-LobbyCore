package org.pablito.pBLobbyCore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

import java.util.List;

public class ChatLockListener implements Listener {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;

    public ChatLockListener(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.isChatLocked()) {
            if (player.hasPermission("pblcore.chatlock.bypass")) {
                return;
            }

            List<String> bypassList = plugin.getConfig().getStringList("chat-bypass-players");
            if (bypassList.contains(player.getName())) {
                return;
            }

            event.setCancelled(true);
            player.sendMessage(messageManager.getMessage("chat-locked"));
        }
    }
}