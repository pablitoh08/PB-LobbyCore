package org.pablito.pBLobbyCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PBLobbyCoreCommand implements CommandExecutor {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;
    private final ReloadManager reloadManager;

    public PBLobbyCoreCommand(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.reloadManager = new ReloadManager(plugin, messageManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messageManager.getMessage("command-usage"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            if (!sender.hasPermission("pblcore.admin")) {
                sender.sendMessage(messageManager.getMessage("permission-denied"));
                return true;
            }
            reloadManager.reloadPlugin();
            sender.sendMessage(messageManager.getMessage("plugin-reloaded"));
            return true;
        }

        if (subCommand.equals("setspawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getMessage("no-console-command"));
                return true;
            }
            if (!sender.hasPermission("pblcore.admin")) {
                sender.sendMessage(messageManager.getMessage("permission-denied"));
                return true;
            }
            Player player = (Player) sender;
            plugin.saveSpawnLocation(player.getLocation());
            player.sendMessage(messageManager.getMessage("spawn-set"));
            return true;
        }

        if (subCommand.equals("alt")) {
            if (!sender.hasPermission("pblcore.admin")) {
                sender.sendMessage(messageManager.getMessage("permission-denied"));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(messageManager.getMessage("alt-usage"));
                return true;
            }
            String altSubCommand = args[1];
            String ipAddress = args[2];
            if (!ipAddress.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                sender.sendMessage(messageManager.getMessage("alt-invalid-ip"));
                return true;
            }
            List<String> exceptions = plugin.getConfig().getStringList("alt-ip-exceptions");
            if (altSubCommand.equalsIgnoreCase("add")) {
                if (exceptions.contains(ipAddress)) {
                    sender.sendMessage(messageManager.getMessage("alt-ip-already-added"));
                    return true;
                }
                exceptions.add(ipAddress);
                plugin.getConfig().set("alt-ip-exceptions", exceptions);
                plugin.saveConfig();
                sender.sendMessage(messageManager.getMessage("alt-ip-added").replace("%ip_address%", ipAddress));
                return true;
            }
            if (altSubCommand.equalsIgnoreCase("remove")) {
                if (!exceptions.contains(ipAddress)) {
                    sender.sendMessage(messageManager.getMessage("alt-ip-not-in-list"));
                    return true;
                }
                exceptions.remove(ipAddress);
                plugin.getConfig().set("alt-ip-exceptions", exceptions);
                plugin.saveConfig();
                sender.sendMessage(messageManager.getMessage("alt-ip-removed").replace("%ip_address%", ipAddress));
                sender.sendMessage(messageManager.getMessage("alt-ip-removed-recheck").replace("%ip_address%", ipAddress));
                List<Player> playersWithSameIp = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getAddress() != null && p.getAddress().getAddress().getHostAddress().equals(ipAddress)) {
                        playersWithSameIp.add(p);
                    }
                }
                if (playersWithSameIp.size() > 1) {
                    Player mainPlayer = playersWithSameIp.get(0);
                    String kickMessage = messageManager.getMessage("kick-message-same-ip").replace("%player_name%", mainPlayer.getName());
                    for (int i = 1; i < playersWithSameIp.size(); i++) {
                        Player altPlayer = playersWithSameIp.get(i);
                        altPlayer.kickPlayer(kickMessage);
                    }
                }
                return true;
            }
            sender.sendMessage(messageManager.getMessage("alt-usage"));
            return true;
        }

        if (subCommand.equals("maintenance")) {
            if (!plugin.getModulesConfig().getBoolean("modules.maintenance-mode")) {
                sender.sendMessage(messageManager.getMessage("module-disabled"));
                return true;
            }
            if (!sender.hasPermission("pblcore.admin")) {
                sender.sendMessage(messageManager.getMessage("permission-denied"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(messageManager.getMessage("maintenance-usage"));
                return true;
            }
            boolean newState;
            if (args[1].equalsIgnoreCase("on")) {
                newState = true;
                sender.sendMessage(messageManager.getMessage("maintenance-on"));
            } else if (args[1].equalsIgnoreCase("off")) {
                newState = false;
                sender.sendMessage(messageManager.getMessage("maintenance-off"));
            } else {
                sender.sendMessage(messageManager.getMessage("maintenance-usage"));
                return true;
            }
            plugin.getModulesConfig().set("modules.maintenance-mode", newState);
            plugin.saveModulesConfig();
            return true;
        }

        if (subCommand.equals("lock") || subCommand.equals("unlock")) {
            if (!plugin.getModulesConfig().getBoolean("modules.chat-lock")) {
                sender.sendMessage(messageManager.getMessage("module-disabled"));
                return true;
            }
            if (!sender.hasPermission("pblcore.chatlock.use")) {
                sender.sendMessage(messageManager.getMessage("permission-denied"));
                return true;
            }

            boolean newState = subCommand.equals("lock");
            plugin.setChatLocked(newState);

            if (newState) {
                sender.sendMessage(messageManager.getMessage("chat-locked-enabled"));
            } else {
                sender.sendMessage(messageManager.getMessage("chat-locked-disabled"));
            }
            return true;
        }

        sender.sendMessage(messageManager.getMessage("command-usage"));
        return true;
    }
}