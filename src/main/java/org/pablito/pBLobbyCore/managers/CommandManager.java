package org.pablito.pBLobbyCore.managers;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Centralized command registration manager.
 * Eliminates duplicate command registration code.
 *
 * @author Pablito
 * @since 2.4
 */
public final class CommandManager {

    private final JavaPlugin plugin;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    /**
     * Registers a command executor and optional tab completer.
     *
     * @param commandName the command name as defined in plugin.yml
     * @param executor the command executor
     * @param tabCompleter the tab completer (may be null)
     * @return true if the command was found and registered
     */
    public boolean registerCommand(String commandName, CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command == null) {
            plugin.getLogger().log(Level.WARNING, "Command '{0}' not found in plugin.yml", commandName);
            return false;
        }

        command.setExecutor(executor);
        if (tabCompleter != null) {
            command.setTabCompleter(tabCompleter);
        }
        return true;
    }

    /**
     * Registers a command executor only (no tab completer).
     *
     * @param commandName the command name as defined in plugin.yml
     * @param executor the command executor
     * @return true if the command was found and registered
     */
    public boolean registerCommand(String commandName, CommandExecutor executor) {
        return registerCommand(commandName, executor, null);
    }

    /**
     * Registers a command that implements both CommandExecutor and TabCompleter.
     *
     * @param commandName the command name as defined in plugin.yml
     * @param handler the handler implementing both interfaces
     * @return true if the command was found and registered
     */
    public boolean registerCommand(String commandName, CommandExecutorAndTab handler) {
        return registerCommand(commandName, handler, handler);
    }

    /**
     * Interface for objects that implement both CommandExecutor and TabCompleter.
     */
    public interface CommandExecutorAndTab extends CommandExecutor, TabCompleter {
    }
}
