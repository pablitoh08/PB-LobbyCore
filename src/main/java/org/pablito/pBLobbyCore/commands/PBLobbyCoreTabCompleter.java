package org.pablito.pBLobbyCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.pablito.pBLobbyCore.PBLobbyCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PBLobbyCoreTabCompleter implements TabCompleter {

    private final PBLobbyCore plugin;

    private static final List<String> MAIN_COMMANDS = new ArrayList<>(List.of("reload", "setspawn", "alt"));
    private static final List<String> ALT_SUBCOMMANDS = List.of("add", "remove");
    private static final List<String> MAINTENANCE_SUBCOMMANDS = List.of("on", "off");

    public PBLobbyCoreTabCompleter(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("pblcore.admin")) {
            return Collections.emptyList();
        }

        // Si el módulo de mantenimiento está activo, se añade a la lista de comandos principales
        if (plugin.getModulesConfig().getBoolean("modules.maintenance-mode")) {
            if (!MAIN_COMMANDS.contains("maintenance")) {
                MAIN_COMMANDS.add("maintenance");
            }
        } else {
            // Si el módulo está desactivado, se asegura de que no esté en la lista para el autocompletado
            MAIN_COMMANDS.remove("maintenance");
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], MAIN_COMMANDS, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("maintenance")) {
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[1], MAINTENANCE_SUBCOMMANDS, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("alt")) {
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[1], ALT_SUBCOMMANDS, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("alt")) {
            List<String> completions = new ArrayList<>();
            String subCommand = args[1];

            if (subCommand.equalsIgnoreCase("add")) {
                List<String> ipAddresses = new ArrayList<>();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getAddress() != null) {
                        String ip = player.getAddress().getAddress().getHostAddress();
                        ipAddresses.add(ip);
                    }
                }
                StringUtil.copyPartialMatches(args[2], ipAddresses, completions);
            } else if (subCommand.equalsIgnoreCase("remove")) {
                List<String> exceptions = plugin.getConfig().getStringList("alt-ip-exceptions");
                StringUtil.copyPartialMatches(args[2], exceptions, completions);
            }

            Collections.sort(completions);
            return completions;
        }

        return Collections.emptyList();
    }
}