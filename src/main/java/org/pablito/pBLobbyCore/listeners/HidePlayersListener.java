package org.pablito.pBLobbyCore.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.HidePlayersManager;

import java.util.Collection;

/**
 * Listener for the hide-players system.
 * Applies hide/show rules on join/quit events.
 *
 * <p>Optimized: uses cached player arrays to avoid repeated getOnlinePlayers() calls.
 * Scheduled task to avoid heavy computation in event handlers.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class HidePlayersListener implements Listener {

    private final PBLobbyCore plugin;

    public HidePlayersListener(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTask(plugin, this::applyForAllOnline);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTask(plugin, this::applyForAllOnline);
    }

    /**
     * Applies hide/show rules for all online players.
     * Optimized to snapshot the player list once and use direct manager calls.
     */
    public void applyForAllOnline() {
        HidePlayersManager hpm = plugin.getHidePlayersManager();

        // Snapshot the online players once to avoid repeated iteration
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        Player[] playerArray = onlinePlayers.toArray(new Player[0]);
        int size = playerArray.length;

        if (!hpm.isEnabled()) {
            // When disabled, show everyone to everyone
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i != j) {
                        playerArray[i].showPlayer(plugin, playerArray[j]);
                    }
                }
            }
            return;
        }

        // When enabled, apply bypass rules
        // Cache bypass states to avoid repeated permission checks
        boolean[] bypassStates = new boolean[size];
        for (int i = 0; i < size; i++) {
            bypassStates[i] = hpm.isBypassEnabled(playerArray[i]);
        }

        for (int i = 0; i < size; i++) {
            Player viewer = playerArray[i];
            boolean viewerBypass = bypassStates[i];

            for (int j = 0; j < size; j++) {
                if (i == j) continue;

                Player target = playerArray[j];
                boolean targetBypass = bypassStates[j];

                // Show if viewer has bypass OR target has bypass
                if (viewerBypass || targetBypass) {
                    viewer.showPlayer(plugin, target);
                } else {
                    viewer.hidePlayer(plugin, target);
                }
            }
        }
    }
}
