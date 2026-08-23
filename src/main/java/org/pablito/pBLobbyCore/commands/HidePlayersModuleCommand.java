package org.pablito.pBLobbyCore.commands;

import org.bukkit.command.CommandSender;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.HidePlayersManager;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.Arrays;
import java.util.List;

/**
 * Command to enable/disable the global hide-players module.
 * Usage: /hideplayers <on|off|toggle|status>
 */
public class HidePlayersModuleCommand extends BaseCommand {

    public HidePlayersModuleCommand(PBLobbyCore plugin) {
        super(plugin, plugin.getMessageManager(), HidePlayersManager.PERM_MODULE, false);
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messageManager.getMessage("hide.module.usage"));
            return;
        }

        HidePlayersManager hpm = plugin.getHidePlayersManager();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "on", "enable" -> {
                hpm.setEnabled(true);
                sender.sendMessage(messageManager.getMessage("hide.module.enabled"));
            }
            case "off", "disable" -> {
                hpm.setEnabled(false);
                sender.sendMessage(messageManager.getMessage("hide.module.disabled"));
            }
            case "toggle" -> {
                boolean newState = !hpm.isEnabled();
                hpm.setEnabled(newState);
                sender.sendMessage(messageManager.getMessage(newState ? "hide.module.enabled" : "hide.module.disabled"));
            }
            case "status" -> {
                boolean st = hpm.isEnabled();
                sender.sendMessage(messageManager.getMessage(st ? "hide.module.status_on" : "hide.module.status_off"));
            }
            default -> sender.sendMessage(messageManager.getMessage("hide.module.usage"));
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
