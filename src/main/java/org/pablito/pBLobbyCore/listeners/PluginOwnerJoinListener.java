package org.pablito.pBLobbyCore.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;

public class PluginOwnerJoinListener implements Listener {

    private final PBLobbyCore plugin;
    private static final String OWNER_NAME = "Pablohs08";

    public PluginOwnerJoinListener(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!p.getName().equalsIgnoreCase(OWNER_NAME)) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            p.sendMessage("§7§m--------------------------------");
            p.sendMessage("§8[§bPB-LobbyCore§8] §fEste servidor está utilizando tu plugin.");
            p.sendMessage("§7§m--------------------------------");
        });
    }
}
