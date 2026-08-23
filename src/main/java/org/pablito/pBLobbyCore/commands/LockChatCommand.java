package org.pablito.pBLobbyCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.ChatManager;
import org.pablito.pBLobbyCore.managers.MessageManager;

/**
 * Command to lock/unlock global chat.
 * Usage: /lock or /unlock
 */
public class LockChatCommand extends BaseCommand {

    public LockChatCommand(PBLobbyCore plugin, MessageManager messageManager) {
        super(plugin, messageManager, "pblcore.chatlock.use", false);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Permission is checked by BaseCommand.onCommand
        ChatManager chatManager = plugin.getChatManager();
        if (!chatManager.isModuleEnabled()) {
            sender.sendMessage(messageManager.getMessage("module-disabled"));
            return true;
        }

        boolean newState = command.getName().equalsIgnoreCase("lock");
        chatManager.setChatLocked(newState);

        if (newState) {
            sender.sendMessage(messageManager.getMessage("chat-locked-enabled"));
        } else {
            sender.sendMessage(messageManager.getMessage("chat-locked-disabled"));
        }
        return true;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        // Not used - onCommand is overridden directly for lock/unlock
    }
}
