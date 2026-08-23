package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listener for command blocking.
 * Blocks a configurable list of commands.
 *
 * <p>Optimized: caches blocked commands in a HashSet for O(1) lookups.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class CommandBlockerListener implements Listener {

    private final MessageManager messageManager;
    private final Set<String> blockedCommands;

    public CommandBlockerListener(List<String> blockedCommands, MessageManager messageManager) {
        this.messageManager = messageManager;
        this.blockedCommands = new HashSet<>(blockedCommands);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message.length() < 2) return;

        // Extract command without the leading '/'
        String command = message.substring(1).split(" ")[0].toLowerCase();

        if (blockedCommands.contains(command)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(messageManager.getMessage("command-blocked"));
        }
    }
}
