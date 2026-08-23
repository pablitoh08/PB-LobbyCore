package org.pablito.pBLobbyCore.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Manages the lobby spawn location.
 * Handles loading from and saving to config.yml.
 *
 * @author Pablito
 * @since 2.4
 */
public final class SpawnManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    private volatile Location spawnLocation;

    public SpawnManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        loadFromConfig();
    }

    /**
     * Gets the current spawn location.
     *
     * @return the spawn location, or null if not set or world is not loaded
     */
    public Location getSpawnLocation() {
        return spawnLocation != null ? spawnLocation.clone() : null;
    }

    /**
     * Saves a new spawn location to config and cache.
     *
     * @param location the new spawn location
     * @return true if saved successfully
     */
    public boolean saveSpawnLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot save spawn: location or world is null");
            return false;
        }

        configManager.getConfig().set("spawn.world", location.getWorld().getName());
        configManager.getConfig().set("spawn.x", location.getX());
        configManager.getConfig().set("spawn.y", location.getY());
        configManager.getConfig().set("spawn.z", location.getZ());
        configManager.getConfig().set("spawn.yaw", location.getYaw());
        configManager.getConfig().set("spawn.pitch", location.getPitch());
        plugin.saveConfig();

        this.spawnLocation = location.clone();
        return true;
    }

    /**
     * Reloads the spawn location from config.
     */
    public void loadFromConfig() {
        String worldName = configManager.getConfig().getString("spawn.world");
        if (worldName == null) {
            this.spawnLocation = null;
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().log(Level.WARNING, "Spawn world not loaded: " + worldName);
            this.spawnLocation = null;
            return;
        }

        double x = configManager.getConfig().getDouble("spawn.x");
        double y = configManager.getConfig().getDouble("spawn.y");
        double z = configManager.getConfig().getDouble("spawn.z");
        float yaw = (float) configManager.getConfig().getDouble("spawn.yaw");
        float pitch = (float) configManager.getConfig().getDouble("spawn.pitch");

        this.spawnLocation = new Location(world, x, y, z, yaw, pitch);
    }
}
