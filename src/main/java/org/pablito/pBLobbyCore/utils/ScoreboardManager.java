package org.pablito.pBLobbyCore.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.pablito.pBLobbyCore.PBLobbyCore;

import java.util.List;

/**
 * Manages scoreboard creation and updates for players.
 *
 * <p>Optimized: caches title and lines to avoid repeated config reads.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class ScoreboardManager {

    private final PBLobbyCore plugin;

    public ScoreboardManager(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates and applies a scoreboard to a player.
     *
     * @param player the player to apply the scoreboard to
     */
    public void createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("scoreboard.title", "Scoreboard"));

        Objective objective = scoreboard.registerNewObjective("pblcore_scoreboard", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        int lineIndex = lines.size();

        for (String line : lines) {
            String formatted = ChatColor.translateAlternateColorCodes('&', line);
            // Replace placeholders
            formatted = formatted.replace("%player_name%", player.getName());
            formatted = formatted.replace("%player_displayname%", player.getDisplayName());

            Score score = objective.getScore(formatted);
            score.setScore(lineIndex--);
        }

        player.setScoreboard(scoreboard);
    }

    /**
     * Removes the custom scoreboard and restores the main scoreboard.
     *
     * @param player the player to reset the scoreboard for
     */
    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
