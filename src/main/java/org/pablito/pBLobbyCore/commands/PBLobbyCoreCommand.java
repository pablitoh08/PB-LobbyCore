package org.pablito.pBLobbyCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main admin command for PB-LobbyCore.
 * Usage: /pblcore <reload|setspawn|alt|maintenance|lock|unlock>
 */
public class PBLobbyCoreCommand extends BaseCommand {

    public PBLobbyCoreCommand(PBLobbyCore plugin, MessageManager messageManager) {
        super(plugin, messageManager, "pblcore.admin", false);
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messageManager.getMessage("command-usage"));
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> executeReload(sender);
            case "setspawn" -> executeSetSpawn(sender);
            case "alt" -> executeAlt(sender, args);
            case "maintenance" -> executeMaintenance(sender, args);
            case "lock", "unlock" -> executeLockUnlock(sender, subCommand);
            default -> sender.sendMessage(messageManager.getMessage("command-usage"));
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterCompletions(
                    Arrays.asList("reload", "setspawn", "alt", "maintenance", "lock", "unlock"), args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            return switch (sub) {
                case "alt" -> filterCompletions(Arrays.asList("add", "remove"), args[1]);
                case "maintenance" -> filterCompletions(Arrays.asList("on", "off"), args[1]);
                default -> super.tabComplete(sender, args);
            };
        }
        return super.tabComplete(sender, args);
    }

    private void executeReload(CommandSender sender) {
        plugin.reloadPluginConfigs();
        sender.sendMessage(messageManager.getMessage("plugin-reloaded"));
    }

    private void executeSetSpawn(CommandSender sender) {
        Player player = getPlayer(sender);
        if (player == null) {
            sender.sendMessage(messageManager.getMessage("no-console-command"));
            return;
        }
        plugin.getSpawnManager().saveSpawnLocation(player.getLocation());
        player.sendMessage(messageManager.getMessage("spawn-set"));
    }

    private void executeAlt(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messageManager.getMessage("alt-usage"));
            return;
        }

        String altSubCommand = args[1].toLowerCase();
        String ipAddress = args[2];

        if (!ipAddress.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            sender.sendMessage(messageManager.getMessage("alt-invalid-ip"));
            return;
        }

        List<String> exceptions = plugin.getConfigManager().getConfig().getStringList("alt-ip-exceptions");

        switch (altSubCommand) {
            case "add" -> {
                if (exceptions.contains(ipAddress)) {
                    sender.sendMessage(messageManager.getMessage("alt-ip-already-added"));
                    return;
                }
                exceptions.add(ipAddress);
                plugin.getConfigManager().getConfig().set("alt-ip-exceptions", exceptions);
                plugin.saveConfig();
                sender.sendMessage(messageManager.getMessage("alt-ip-added").replace("%ip_address%", ipAddress));
            }
            case "remove" -> {
                if (!exceptions.contains(ipAddress)) {
                    sender.sendMessage(messageManager.getMessage("alt-ip-not-in-list"));
                    return;
                }
                exceptions.remove(ipAddress);
                plugin.getConfigManager().getConfig().set("alt-ip-exceptions", exceptions);
                plugin.saveConfig();
                sender.sendMessage(messageManager.getMessage("alt-ip-removed").replace("%ip_address%", ipAddress));
                sender.sendMessage(messageManager.getMessage("alt-ip-removed-recheck").replace("%ip_address%", ipAddress));

                // Re-check online players with same IP
                List<Player> playersWithSameIp = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getAddress() != null && p.getAddress().getAddress().getHostAddress().equals(ipAddress)) {
                        playersWithSameIp.add(p);
                    }
                }
                if (playersWithSameIp.size() > 1) {
                    Player mainPlayer = playersWithSameIp.get(0);
                    String kickMessage = messageManager.getMessage("kick-message-same-ip")
                            .replace("%player_name%", mainPlayer.getName());
                    for (int i = 1; i < playersWithSameIp.size(); i++) {
                        playersWithSameIp.get(i).kickPlayer(kickMessage);
                    }
                }
            }
            default -> sender.sendMessage(messageManager.getMessage("alt-usage"));
        }
    }

    private void executeMaintenance(CommandSender sender, String[] args) {
        if (!plugin.getModuleManager().isEnabled("maintenance-mode")) {
            sender.sendMessage(messageManager.getMessage("module-disabled"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(messageManager.getMessage("maintenance-usage"));
            return;
        }

        String state = args[1].toLowerCase();
        boolean newState;
        if (state.equals("on")) {
            newState = true;
            sender.sendMessage(messageManager.getMessage("maintenance-on"));
        } else if (state.equals("off")) {
            newState = false;
            sender.sendMessage(messageManager.getMessage("maintenance-off"));
        } else {
            sender.sendMessage(messageManager.getMessage("maintenance-usage"));
            return;
        }

        plugin.getModuleManager().setEnabled("maintenance-mode", newState);
    }

    private void executeLockUnlock(CommandSender sender, String subCommand) {
        if (!plugin.getModuleManager().isEnabled("chat-lock")) {
            sender.sendMessage(messageManager.getMessage("module-disabled"));
            return;
        }

        boolean newState = subCommand.equals("lock");
        plugin.getChatManager().setChatLocked(newState);

        if (newState) {
            sender.sendMessage(messageManager.getMessage("chat-locked-enabled"));
        } else {
            sender.sendMessage(messageManager.getMessage("chat-locked-disabled"));
        }
    }
}
