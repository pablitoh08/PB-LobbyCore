package org.pablito.pBLobbyCore.pvp;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.commands.BaseCommand;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.Arrays;
import java.util.List;

/**
 * Command to toggle PvP state.
 * Usage: /pvp [on|off|status]
 */
public class PvpCommand extends BaseCommand {

    private final PvpToggleStore store;

    public PvpCommand(PBLobbyCore plugin, MessageManager messageManager, PvpToggleStore store) {
        super(plugin, messageManager, "pblcore.pvp", true);
        this.store = store;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) return;

        if (!plugin.getModuleManager().isEnabled("no-pvp")) {
            player.sendMessage(messageManager.getMessage("pvp-module-disabled"));
            return;
        }

        if (!plugin.getConfig().getBoolean("no-pvp.allow-pvp-toggle", true)) {
            player.sendMessage(messageManager.getMessage("pvp-toggle-not-allowed"));
            return;
        }

        // Cooldown check
        int cooldown = plugin.getConfig().getInt("no-pvp.cooldown-seconds", 0);
        if (cooldown > 0) {
            long last = store.getLastUse(player.getUniqueId());
            long now = System.currentTimeMillis();
            long remaining = last + cooldown * 1000L - now;
            if (remaining > 0) {
                long seconds = (remaining + 999) / 1000;
                player.sendMessage(messageManager.getMessage("pvp-cooldown")
                        .replace("%seconds%", String.valueOf(seconds)));
                return;
            }
            store.setLastUse(player.getUniqueId(), now);
        }

        String sub = args.length > 0 ? args[0].toLowerCase() : "toggle";
        switch (sub) {
            case "status" -> {
                player.sendMessage(store.isEnabled(player.getUniqueId())
                        ? messageManager.getMessage("pvp-status-on")
                        : messageManager.getMessage("pvp-status-off"));
            }
            case "on", "enable" -> {
                store.setEnabled(player.getUniqueId(), true);
                player.sendMessage(messageManager.getMessage("pvp-enabled"));
            }
            case "off", "disable" -> {
                store.setEnabled(player.getUniqueId(), false);
                player.sendMessage(messageManager.getMessage("pvp-disabled"));
            }
            default -> {
                boolean next = !store.isEnabled(player.getUniqueId());
                store.setEnabled(player.getUniqueId(), next);
                player.sendMessage(messageManager.getMessage(next ? "pvp-enabled" : "pvp-disabled"));
            }
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
