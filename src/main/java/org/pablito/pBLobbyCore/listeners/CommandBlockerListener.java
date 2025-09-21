package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.pablito.pBLobbyCore.utils.MessageManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class CommandBlockerListener implements Listener {

    private final MessageManager messageManager;
    private final List<String> blockedCommands;

    public CommandBlockerListener(FileConfiguration config, MessageManager messageManager) {
        this.messageManager = messageManager;
        this.blockedCommands = config.getStringList("blocked_commands");
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().substring(1).split(" ")[0];

        if (blockedCommands.contains(command)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(messageManager.getMessage("command-blocked"));
        }
    }
}