package org.pablito.pBLobbyCore.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MessageManager {

    private final JavaPlugin plugin;
    private FileConfiguration messagesConfig;
    private String prefix;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        String language = plugin.getConfig().getString("language", "en");

        File langFile = new File(plugin.getDataFolder(), "lang" + File.separator + language + ".yml");

        this.messagesConfig = YamlConfiguration.loadConfiguration(langFile);

        this.prefix = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("prefix", ""));
    }

    public String getMessage(String path) {
        String message = messagesConfig.getString("messages." + path);
        if (message == null) {
            return ChatColor.RED + "Error: Message not found for path '" + path + "'";
        }

        String finalMessage = message.replace("%prefix%", this.prefix);
        return ChatColor.translateAlternateColorCodes('&', finalMessage);
    }
}