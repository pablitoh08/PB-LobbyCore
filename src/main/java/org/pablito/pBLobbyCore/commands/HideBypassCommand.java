package org.pablito.pBLobbyCore.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.HidePlayersManager;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.Arrays;
import java.util.List;

/**
 * Command to toggle hide players bypass for the executing player.
 * Usage: /hidebypass <on|off|toggle|status>
 */
public class HideBypassCommand extends BaseCommand {

    public HideBypassCommand(PBLobbyCore plugin) {
        super(plugin, plugin.getMessageManager(), HidePlayersManager.PERM_BYPASS_TOGGLE, true);
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) return;

        if (!player.hasPermission(HidePlayersManager.PERM_BYPASS)) {
            player.sendMessage(messageManager.getMessage("hide.bypass.no_permission"));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(messageManager.getMessage("hide.bypass.usage"));
            return;
        }

        HidePlayersManager hpm = plugin.getHidePlayersManager();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "on", "enable" -> {
                hpm.setBypassEnabled(player, true);
                player.sendMessage(messageManager.getMessage("hide.bypass.enabled"));
            }
            case "off", "disable" -> {
                hpm.setBypassEnabled(player, false);
                player.sendMessage(messageManager.getMessage("hide.bypass.disabled"));
            }
            case "toggle" -> {
                boolean newState = hpm.toggleBypass(player);
                player.sendMessage(messageManager.getMessage(newState ? "hide.bypass.enabled" : "hide.bypass.disabled"));
            }
            case "status" -> {
                boolean st = hpm.isBypassEnabled(player);
                player.sendMessage(messageManager.getMessage(st ? "hide.bypass.status_on" : "hide.bypass.status_off"));
            }
            default -> player.sendMessage(messageManager.getMessage("hide.bypass.usage"));
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterCompletions(Arrays.asList("on", "off", "toggle", "status"), args[0]);
        }
        return super.tabComplete(sender, args);
    }
}
