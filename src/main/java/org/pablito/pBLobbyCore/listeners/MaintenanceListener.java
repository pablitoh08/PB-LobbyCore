package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

import java.util.List;

public class MaintenanceListener implements Listener {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;

    public MaintenanceListener(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        boolean maintenanceMode = plugin.getModulesConfig().getBoolean("modules.maintenance-mode", false);

        if (!maintenanceMode) {
            return;
        }

        String playerName = event.getPlayer().getName();

        List<String> whitelist = plugin.getWhitelistConfig().getStringList("players");

        if (event.getPlayer().hasPermission("pblcore.admin")) {
            return;
        }

        if (!whitelist.contains(playerName)) {
            String kickMessage = messageManager.getMessage("maintenance-kick-message");
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
        }
    }
}
