package org.pablito.pBLobbyCore.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.pablito.pBLobbyCore.PBLobbyCore;

import java.util.List;
import java.util.stream.Collectors;

public class TabManager {

    private final PBLobbyCore plugin;
    private BukkitTask updateTask;

    public TabManager(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    public void startTabUpdater() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        long updateInterval = plugin.getConfig().getLong("tab.update-interval", 20L);

        this.updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerTab(player);
            }
        }, 0L, updateInterval);
    }

    public void stopTabUpdater() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    public void updatePlayerTab(Player player) {
        if (!plugin.getModulesConfig().getBoolean("modules.tab")) {
            return;
        }

        List<String> headerLines = plugin.getConfig().getStringList("tab.tab-header");
        List<String> footerLines = plugin.getConfig().getStringList("tab.tab-footer");

        String header = formatLines(headerLines, player);
        String footer = formatLines(footerLines, player);

        try {
            player.setPlayerListHeader(header);
            player.setPlayerListFooter(footer);
        } catch (Exception e) {
            plugin.getLogger().severe("Error al actualizar el TAB: " + e.getMessage());
        }
    }

    private String formatLines(List<String> lines, Player player) {
        String fullString = lines.stream().collect(Collectors.joining("\n"));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            fullString = PlaceholderAPI.setPlaceholders(player, fullString);
        }

        return ChatColor.translateAlternateColorCodes('&', fullString);
    }
}
