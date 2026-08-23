package org.pablito.pBLobbyCore.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * Manages localized messages from language files under lang/.
 * Caches resolved messages for performance.
 *
 * <p>This class is fully thread-safe.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public final class MessageManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    private volatile FileConfiguration messagesConfig;
    private volatile String prefix;

    /** Cache for resolved and color-coded messages. */
    private final ConcurrentMap<String, String> messageCache = new ConcurrentHashMap<>();

    public MessageManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        loadMessages();
    }

    /**
     * Loads (or reloads) messages from the language file specified in config.yml.
     * Clears the message cache.
     */
    public void loadMessages() {
        String language = configManager.getConfig().getString("language", "en");
        File langFile = new File(plugin.getDataFolder(), "lang" + File.separator + language + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().log(Level.WARNING, "Language file not found: " + language + ".yml, falling back to en.yml");
            langFile = new File(plugin.getDataFolder(), "lang" + File.separator + "en.yml");
        }

        this.messagesConfig = YamlConfiguration.loadConfiguration(langFile);
        this.prefix = ChatColor.translateAlternateColorCodes('&',
                messagesConfig.getString("prefix", ""));
        this.messageCache.clear();
    }

    /**
     * Gets a message by its path, with color codes translated and prefix replaced.
     * Results are cached after first resolution.
     *
     * @param path the message path in the YAML (e.g., "plugin-reloaded")
     * @return the formatted message, or a red error message if not found
     */
    public String getMessage(String path) {
        return messageCache.computeIfAbsent(path, this::resolveMessage);
    }

    /**
     * Forces a reload of a specific message from disk (invalidates cache for that key).
     *
     * @param path the message path
     */
    public void invalidateMessage(String path) {
        messageCache.remove(path);
    }

    /**
     * Clears the entire message cache.
     */
    public void clearCache() {
        messageCache.clear();
    }

    /**
     * Gets the plugin prefix (already color-coded).
     *
     * @return the prefix string
     */
    public String getPrefix() {
        return prefix;
    }

    private String resolveMessage(String path) {
        String message = messagesConfig.getString("messages." + path);
        if (message == null) {
            return ChatColor.RED + "Error: Message not found for path '" + path + "'";
        }

        String finalMessage = message.replace("%prefix%", this.prefix);
        return ChatColor.translateAlternateColorCodes('&', finalMessage);
    }
}
