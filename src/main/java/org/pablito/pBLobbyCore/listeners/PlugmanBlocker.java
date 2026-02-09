package org.pablito.pBLobbyCore.listeners;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

public class PlugmanBlocker implements Listener {

    private final PBLobbyCore plugin;
    private final MessageManager messages;

    public PlugmanBlocker(PBLobbyCore plugin) {
        this.plugin = plugin;
        MessageManager mm = plugin.getMessageManager();
        this.messages = (mm != null) ? mm : new MessageManager(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String raw = safe(event.getMessage());
        Player sender = event.getPlayer();

        if (sender.hasPermission("pblcore.plugman.bypass")) return;

        if (isBlockedCommand(raw)) {
            event.setCancelled(true);
            sender.sendMessage(msg("commands.plugman_blocked",
                    "&cEste comando está bloqueado para proteger el servidor."));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsoleCommand(ServerCommandEvent event) {
        String raw = safe("/" + event.getCommand());
        CommandSender sender = event.getSender();

        if (isBlockedCommand(raw)) {
            event.setCancelled(true);
            sender.sendMessage(stripColors(msg("commands.plugman_blocked",
                    "&cEste comando está bloqueado para proteger el servidor.")));
        }
    }

    private boolean isBlockedCommand(String command) {
        String c = command.toLowerCase();

        boolean isPlugman = c.startsWith("/plugman") || c.startsWith("/plugmanx");
        if (!isPlugman) return false;

        String thisPlugin = plugin.getName().toLowerCase();

        boolean targetsThis = c.contains(thisPlugin);
        if (!targetsThis) return false;

        return c.contains("disable")
                || c.contains("reload")
                || c.contains("unload")
                || c.contains("restart")
                || c.contains("load")
                || c.contains("enable");
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private String msg(String key, String def) {
        try {
            String m = messages.getMessage(key);
            if (m == null || m.isEmpty()) return color(def);
            return m;
        } catch (Throwable ignored) {
            return color(def);
        }
    }

    private String color(String s) {
        return s.replace('&', '§');
    }

    private String stripColors(String s) {
        return s.replaceAll("§[0-9A-FK-ORa-fk-or]", "");
    }
}
