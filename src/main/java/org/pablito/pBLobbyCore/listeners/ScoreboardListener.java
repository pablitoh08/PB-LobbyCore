package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.ScoreboardManager;

public class ScoreboardListener implements Listener {

    private final ScoreboardManager scoreboardManager;

    public ScoreboardListener(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        scoreboardManager.createScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        scoreboardManager.removeScoreboard(event.getPlayer());
    }
}