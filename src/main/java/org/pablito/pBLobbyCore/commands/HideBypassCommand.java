package org.pablito.pBLobbyCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pablito.pBLobbyCore.PBLobbyCore;

public class HideBypassCommand implements CommandExecutor {

    private final PBLobbyCore plugin;

    public HideBypassCommand(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission(PBLobbyCore.PERM_HIDE_BYPASS_TOGGLE) || !player.hasPermission(PBLobbyCore.PERM_HIDE_BYPASS)) {
            player.sendMessage(plugin.tr("hide.bypass.no_permission", "§cNo tienes permiso para usar el bypass."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.tr("hide.bypass.usage", "§eUso: /hidebypass <on|off|toggle|status>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "on", "enable" -> {
                plugin.setBypassEnabled(player, true);
                player.sendMessage(plugin.tr("hide.bypass.enabled", "§aBypass activado."));
            }
            case "off", "disable" -> {
                plugin.setBypassEnabled(player, false);
                player.sendMessage(plugin.tr("hide.bypass.disabled", "§cBypass desactivado."));
            }
            case "toggle" -> {
                boolean newState = !plugin.isBypassEnabled(player);
                plugin.setBypassEnabled(player, newState);
                player.sendMessage(plugin.tr(newState ? "hide.bypass.enabled" : "hide.bypass.disabled",
                        newState ? "§aBypass activado." : "§cBypass desactivado."));
            }
            case "status" -> {
                boolean st = plugin.isBypassEnabled(player);
                player.sendMessage(plugin.tr(st ? "hide.bypass.status_on" : "hide.bypass.status_off",
                        st ? "§aBypass: ACTIVO" : "§cBypass: INACTIVO"));
            }
            default -> player.sendMessage(plugin.tr("hide.bypass.usage", "§eUso: /hidebypass <on|off|toggle|status>"));
        }

        return true;
    }
}
