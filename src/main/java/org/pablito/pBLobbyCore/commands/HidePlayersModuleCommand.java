package org.pablito.pBLobbyCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.pablito.pBLobbyCore.PBLobbyCore;

public class HidePlayersModuleCommand implements CommandExecutor {

    private final PBLobbyCore plugin;

    public HidePlayersModuleCommand(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission(PBLobbyCore.PERM_HIDE_MODULE)) {
            sender.sendMessage(plugin.tr("hide.module.no_permission", "§cNo tienes permiso para hacer esto."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.tr("hide.module.usage", "§eUso: /hideplayers <on|off|toggle|status>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "on", "enable" -> {
                plugin.setHidePlayersEnabled(true);
                sender.sendMessage(plugin.tr("hide.module.enabled", "§aMódulo de ocultar jugadores activado."));
            }
            case "off", "disable" -> {
                plugin.setHidePlayersEnabled(false);
                sender.sendMessage(plugin.tr("hide.module.disabled", "§cMódulo de ocultar jugadores desactivado."));
            }
            case "toggle" -> {
                boolean newState = !plugin.isHidePlayersEnabled();
                plugin.setHidePlayersEnabled(newState);
                sender.sendMessage(plugin.tr(newState ? "hide.module.enabled" : "hide.module.disabled",
                        newState ? "§aMódulo activado." : "§cMódulo desactivado."));
            }
            case "status" -> {
                sender.sendMessage(plugin.tr(plugin.isHidePlayersEnabled() ? "hide.module.status_on" : "hide.module.status_off",
                        plugin.isHidePlayersEnabled() ? "§aOcultar jugadores: ACTIVO" : "§cOcultar jugadores: INACTIVO"));
            }
            default -> sender.sendMessage(plugin.tr("hide.module.usage", "§eUso: /hideplayers <on|off|toggle|status>"));
        }

        return true;
    }
}
