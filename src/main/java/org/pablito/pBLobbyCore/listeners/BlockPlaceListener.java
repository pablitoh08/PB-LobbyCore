package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Player;
import org.pablito.pBLobbyCore.utils.MessageManager;
import org.pablito.pBLobbyCore.PBLobbyCore;

import java.util.List;

public class BlockPlaceListener implements Listener{

    private final MessageManager messageManager;
    private final List<String> blockplaceBypassPlayers;

    public BlockPlaceListener(MessageManager messageManager, PBLobbyCore plugin) {
        this.messageManager = messageManager;
        this.blockplaceBypassPlayers = plugin.getConfig().getStringList("blockplace-bypass-players");
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if(blockplaceBypassPlayers.contains(player.getName())) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(messageManager.getMessage("no-block-place"));
    }
}