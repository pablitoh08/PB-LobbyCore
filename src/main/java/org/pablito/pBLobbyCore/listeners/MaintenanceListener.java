package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;

import java.util.List;

public class MaintenanceListener implements Listener {

    private final PBLobbyCore plugin;

    public MaintenanceListener(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        boolean maintenanceMode = plugin.getModulesConfig().getBoolean("modules.maintenance-mode");

        String playerName = event.getPlayer().getName();
        List<String> whitelist = plugin.getWhitelistConfig().getStringList("players");

        if (maintenanceMode && !event.getPlayer().hasPermission("pblcore.admin") && !whitelist.contains(playerName)) {
            String kickMessage = plugin.getMessageManager().getMessage("maintenance-kick-message");
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
        }
    }
}