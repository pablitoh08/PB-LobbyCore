package org.pablito.pBLobbyCore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

import org.jetbrains.annotations.NotNull;

public class AnnounceCommand implements CommandExecutor {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final String ANNOUNCE_PREFIX_KEY = "announce-prefix";

    public AnnounceCommand(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("pblcore.command.announce")) {
            sender.sendMessage(messageManager.getMessage("permission-denied"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(messageManager.getMessage("announce-usage"));
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg).append(" ");
        }
        String rawMessage = sb.toString().trim();

        String prefix = messageManager.getMessage(ANNOUNCE_PREFIX_KEY);

        Component announcementPrefix = miniMessage.deserialize(prefix);
        Component announcementMessage = miniMessage.deserialize(rawMessage);

        Component finalAnnouncement = announcementPrefix.append(announcementMessage);

        Bukkit.broadcast(finalAnnouncement);

        return true;
    }
}