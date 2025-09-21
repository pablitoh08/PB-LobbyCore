package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

import java.util.List;

public class WhitelistListener implements Listener {

    private final List<String> allowedPlayers;
    private final MessageManager messageManager;

    public WhitelistListener(PBLobbyCore plugin, MessageManager messageManager) {
        this.messageManager = messageManager;
        this.allowedPlayers = plugin.getWhitelistConfig().getStringList("whitelisted-players");
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();

        if (!allowedPlayers.contains(playerName)) {
            event.setLoginResult(Result.KICK_WHITELIST);
            event.setKickMessage(messageManager.getMessage("not-whitelisted"));
        }
    }
}