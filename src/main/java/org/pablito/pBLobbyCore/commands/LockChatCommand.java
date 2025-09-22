package org.pablito.pBLobbyCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

public class LockChatCommand implements CommandExecutor {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;

    public LockChatCommand(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pblcore.chatlock.use")) {
            sender.sendMessage(messageManager.getMessage("permission-denied"));
            return true;
        }

        if (!plugin.getModulesConfig().getBoolean("modules.chat-lock")) {
            sender.sendMessage(messageManager.getMessage("module-disabled"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("lock")) {
            plugin.setChatLocked(true);
            sender.sendMessage(messageManager.getMessage("chat-locked-enabled"));
        } else if (command.getName().equalsIgnoreCase("unlock")) {
            plugin.setChatLocked(false);
            sender.sendMessage(messageManager.getMessage("chat-locked-disabled"));
        }

        return true;
    }
}