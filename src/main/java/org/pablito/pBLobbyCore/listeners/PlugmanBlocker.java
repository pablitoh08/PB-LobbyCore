package org.pablito.pBLobbyCore.listeners;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;

/**
 * Listener that blocks plugman commands targeting this plugin.
 * Prevents other plugins or players from unloading/disabling PB-LobbyCore.
 *
 * @author Pablito
 * @since 2.4
 */
public class PlugmanBlocker implements Listener {

    private final PBLobbyCore plugin;

    public PlugmanBlocker(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String raw = safe(event.getMessage());
        Player sender = event.getPlayer();

        if (sender.hasPermission("pblcore.plugman.bypass")) return;

        if (isBlockedCommand(raw)) {
            event.setCancelled(true);
            sender.sendMessage(getBlockMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsoleCommand(ServerCommandEvent event) {
        String raw = safe("/" + event.getCommand());
        CommandSender sender = event.getSender();

        if (isBlockedCommand(raw)) {
            event.setCancelled(true);
            sender.sendMessage(stripColors(getBlockMessage()));
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

    private String getBlockMessage() {
        if (plugin.getMessageManager() != null) {
            String msg = plugin.getMessageManager().getMessage("commands.plugman_blocked");
            if (msg != null && !msg.isEmpty() && !msg.contains("Message not found")) {
                return msg;
            }
        }
        return "§cEste comando está bloqueado para proteger el servidor.";
    }

    private String stripColors(String s) {
        return s.replaceAll("§[0-9A-FK-ORa-fk-or]", "");
    }
}
