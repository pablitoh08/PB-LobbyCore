package org.pablito.pBLobbyCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Abstract base command providing common functionality for all commands.
 * Handles permission checks, console restrictions, and async execution.
 *
 * <p>Subclasses must implement {@link #execute(CommandSender, String[])}.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final PBLobbyCore plugin;
    protected final MessageManager messageManager;
    private final String permission;
    private final boolean requirePlayer;

    /**
     * Creates a new base command.
     *
     * @param plugin the plugin instance
     * @param messageManager the message manager
     * @param permission the permission required (null for no check)
     * @param requirePlayer true if this command can only be run by a player
     */
    protected BaseCommand(PBLobbyCore plugin, MessageManager messageManager, String permission, boolean requirePlayer) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.permission = permission;
        this.requirePlayer = requirePlayer;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(messageManager.getMessage("permission-denied"));
            return true;
        }

        if (requirePlayer && !(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("no-console-command"));
            return true;
        }

        try {
            execute(sender, args);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error executing command /" + label, e);
            sender.sendMessage(messageManager.getMessage("permission-denied"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (permission != null && !sender.hasPermission(permission)) {
            return Collections.emptyList();
        }
        return tabComplete(sender, args);
    }

    /**
     * Executes the command logic.
     *
     * @param sender the command sender
     * @param args the command arguments
     */
    protected abstract void execute(CommandSender sender, String[] args);

    /**
     * Provides tab completion suggestions. Default implementation returns empty list.
     *
     * @param sender the command sender
     * @param args the current arguments
     * @return list of suggestions
     */
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Convenience method to get a player from the sender.
     *
     * @param sender the command sender
     * @return the player, or null if sender is not a player
     */
    protected Player getPlayer(CommandSender sender) {
        return sender instanceof Player player ? player : null;
    }

    /**
     * Creates a partial match filter for tab completion.
     *
     * @param options the full list of options
     * @param input the current input to match against
     * @return filtered list of matching options
     */
    protected List<String> filterCompletions(List<String> options, String input) {
        if (input == null || input.isEmpty()) return new ArrayList<>(options);
        String lowerInput = input.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lowerInput)) {
                filtered.add(option);
            }
        }
        return filtered;
    }
}
