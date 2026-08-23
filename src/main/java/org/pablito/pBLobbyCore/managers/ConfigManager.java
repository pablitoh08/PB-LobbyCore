package org.pablito.pBLobbyCore.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Manages all plugin configuration files (config.yml, modules.yml, whitelist.yml).
 * Handles loading, saving, reloading, and automatic version updates.
 *
 * <p>This class is thread-safe for read operations. Write operations
 * should be synchronized externally if called from async contexts.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public final class ConfigManager {

    private final JavaPlugin plugin;

    private FileConfiguration config;
    private FileConfiguration modulesConfig;
    private FileConfiguration whitelistConfig;

    private File modulesFile;
    private File whitelistFile;

    /**
     * Creates a new ConfigManager instance.
     *
     * @param plugin the plugin instance
     * @throws NullPointerException if plugin is null
     */
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    /**
     * Initializes all configuration files. Creates them from defaults if they don't exist,
     * and updates them if a newer version is bundled with the plugin.
     */
    public void initialize() {
        plugin.saveDefaultConfig();

        updateConfigFile();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        setupModulesConfig();
        updateModulesFile();

        setupWhitelistConfig();
    }

    // ========== config.yml ==========

    /**
     * Gets the main plugin configuration.
     *
     * @return the config.yml FileConfiguration
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Reloads the main config.yml from disk.
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // ========== modules.yml ==========

    /**
     * Gets the modules configuration.
     *
     * @return the modules.yml FileConfiguration
     */
    public FileConfiguration getModulesConfig() {
        if (modulesConfig == null) {
            setupModulesConfig();
        }
        return modulesConfig;
    }

    /**
     * Reloads modules.yml from disk.
     */
    public void reloadModulesConfig() {
        if (modulesFile != null) {
            modulesConfig = YamlConfiguration.loadConfiguration(modulesFile);
        }
    }

    /**
     * Saves modules.yml to disk.
     */
    public void saveModulesConfig() {
        if (modulesConfig == null || modulesFile == null) return;
        try {
            modulesConfig.save(modulesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save modules.yml: " + e.getMessage(), e);
        }
    }

    // ========== whitelist.yml ==========

    /**
     * Gets the whitelist configuration.
     *
     * @return the whitelist.yml FileConfiguration
     */
    public FileConfiguration getWhitelistConfig() {
        if (whitelistConfig == null) {
            reloadWhitelistConfig();
        }
        return whitelistConfig;
    }

    /**
     * Reloads whitelist.yml from disk.
     */
    public void reloadWhitelistConfig() {
        if (whitelistFile == null) {
            whitelistFile = new File(plugin.getDataFolder(), "whitelist.yml");
        }

        ensureDataFolder();
        ensureFileExists(whitelistFile, "whitelist.yml");

        whitelistConfig = YamlConfiguration.loadConfiguration(whitelistFile);
    }

    // ========== Reload All ==========

    /**
     * Reloads all configuration files and applies version updates.
     */
    public void reloadAll() {
        updateConfigFile();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        reloadModulesConfig();
        updateModulesFile();

        reloadWhitelistConfig();
    }

    // ========== Internal Methods ==========

    private void setupModulesConfig() {
        modulesFile = new File(plugin.getDataFolder(), "modules.yml");

        ensureDataFolder();
        ensureFileExists(modulesFile, "modules.yml");

        modulesConfig = YamlConfiguration.loadConfiguration(modulesFile);
    }

    private void setupWhitelistConfig() {
        whitelistFile = new File(plugin.getDataFolder(), "whitelist.yml");

        ensureDataFolder();
        ensureFileExists(whitelistFile, "whitelist.yml");

        whitelistConfig = YamlConfiguration.loadConfiguration(whitelistFile);
    }

    private void ensureDataFolder() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }

    private void ensureFileExists(File file, String resourceName) {
        if (file.exists()) return;

        try {
            plugin.saveResource(resourceName, false);
        } catch (IllegalArgumentException ignored) {
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create " + resourceName + ": " + e.getMessage(), e);
            }
        }
    }

    private void updateConfigFile() {
        File cfg = new File(plugin.getDataFolder(), "config.yml");
        updateYamlFileIfNeeded("config.yml", cfg);
    }

    private void updateModulesFile() {
        if (modulesFile == null) {
            modulesFile = new File(plugin.getDataFolder(), "modules.yml");
        }
        updateYamlFileIfNeeded("modules.yml", modulesFile);
        reloadModulesConfig();
    }

    /**
     * Updates a YAML file if a newer version is bundled with the plugin.
     * Preserves user modifications while adding new default keys.
     *
     * @param resourceName the resource name in the JAR
     * @param targetFile the target file on disk
     * @return true if the file was updated
     */
    private boolean updateYamlFileIfNeeded(String resourceName, File targetFile) {
        if (targetFile == null) return false;

        ensureDataFolder();

        if (!targetFile.exists()) {
            try {
                plugin.saveResource(resourceName, false);
                return true;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }

        YamlConfiguration current = YamlConfiguration.loadConfiguration(targetFile);

        if (plugin.getResource(resourceName) == null) return false;

        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(Objects.requireNonNull(plugin.getResource(resourceName)), StandardCharsets.UTF_8)
        );

        double currentVersion = current.getDouble("config-version", 0.0);
        double defaultVersion = defaults.getDouble("config-version", 0.0);

        if (defaultVersion <= currentVersion) return false;

        current.setDefaults(defaults);
        current.options().copyDefaults(true);
        current.set("config-version", defaultVersion);

        try {
            current.save(targetFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update " + resourceName + ": " + e.getMessage(), e);
            return false;
        }
    }
}
