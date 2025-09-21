package org.pablito.pBLobbyCore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

import java.util.List;

public class BlockBreakListener implements Listener{

    private final MessageManager messageManager;
    private final List<String> blockbreakBypassPlayers;

    public BlockBreakListener(MessageManager messageManager, PBLobbyCore plugin) {
        this.messageManager = messageManager;
        this.blockbreakBypassPlayers = plugin.getConfig().getStringList("blockbreak-bypass-players");
    }

    @EventHandler
    public void onPlayerDestroyBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if(blockbreakBypassPlayers.contains(player.getName())) {
            return;
        }

        event.setCancelled(true);

        player.sendMessage(messageManager.getMessage("no-block-break"));
    }
}