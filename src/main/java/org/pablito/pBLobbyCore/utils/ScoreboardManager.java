package org.pablito.pBLobbyCore.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.pablito.pBLobbyCore.PBLobbyCore;

import java.util.List;

public class ScoreboardManager {

    private final PBLobbyCore plugin;
    private BukkitTask updateTask;

    public ScoreboardManager(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    public void startScoreboardUpdater() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        long updateInterval = plugin.getConfig().getLong("scoreboard.update-interval", 20L);
        this.updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 0L, updateInterval);
    }

    public void stopScoreboardUpdater() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    public void updateScoreboard(Player player) {
        if (!plugin.getModulesConfig().getBoolean("modules.scoreboard")) {
            removeScoreboard(player);
            return;
        }

        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

        if (objective == null || !objective.getName().equals("pblcore_scoreboard")) {
            createScoreboard(player);
            scoreboard = player.getScoreboard();
            objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        }

        String newTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.title", "Scoreboard"));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            newTitle = PlaceholderAPI.setPlaceholders(player, newTitle);
        }
        objective.setDisplayName(newTitle);

        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        int blankLineCounter = 0; // Contador para las líneas vacías

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String processedLine = ChatColor.translateAlternateColorCodes('&', line);

            if (processedLine.trim().isEmpty()) {
                processedLine = ChatColor.RESET.toString() + ChatColor.values()[blankLineCounter];
                blankLineCounter++;
            } else if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                processedLine = PlaceholderAPI.setPlaceholders(player, processedLine);
            }

            if (processedLine.length() > 40) {
                processedLine = processedLine.substring(0, 40);
            }

            objective.getScore(processedLine).setScore(lines.size() - i);
        }
    }

    public void createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.title", "Scoreboard"));
        Objective objective = scoreboard.registerNewObjective("pblcore_scoreboard", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
    }

    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}