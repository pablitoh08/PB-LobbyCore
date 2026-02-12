package org.pablito.pBLobbyCore.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;

public class HidePlayersListener implements Listener {

    private final PBLobbyCore plugin;

    public HidePlayersListener(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTask(plugin, this::applyForAllOnline);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTask(plugin, this::applyForAllOnline);
    }

    public void applyForAllOnline() {
        if (!plugin.isHidePlayersEnabled()) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (viewer.equals(target)) continue;
                    viewer.showPlayer(plugin, target);
                }
            }
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            boolean viewerBypass = plugin.isBypassEnabled(viewer);

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (viewer.equals(target)) continue;

                boolean targetBypass = plugin.isBypassEnabled(target);

                boolean shouldHide = false;

                if (!viewerBypass) {
                    if (!targetBypass) {
                        shouldHide = true;
                    }
                }

                if (shouldHide) viewer.hidePlayer(plugin, target);
                else viewer.showPlayer(plugin, target);
            }
        }
    }
}
