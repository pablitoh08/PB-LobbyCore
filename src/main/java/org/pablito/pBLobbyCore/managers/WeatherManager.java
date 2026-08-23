package org.pablito.pBLobbyCore.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

/**
 * Manages weather and time lock for lobby worlds.
 * Periodically enforces the configured weather and time.
 *
 * @author Pablito
 * @since 2.4
 */
public final class WeatherManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ModuleManager moduleManager;

    private volatile boolean enabled;
    private volatile FixedWeatherType weatherType = FixedWeatherType.CLEAR;
    private volatile FixedTimeOfDay timeOfDay = FixedTimeOfDay.DAY;

    private BukkitTask weatherLockTask;

    public WeatherManager(JavaPlugin plugin, ConfigManager configManager, ModuleManager moduleManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager cannot be null");
        loadSettings();
    }

    // ========== Getters/Setters ==========

    public boolean isEnabled() {
        return enabled;
    }

    public FixedWeatherType getWeatherType() {
        return weatherType;
    }

    public FixedTimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        moduleManager.setEnabled(ModuleManager.WEATHER_LOCK, enabled);
        startTaskIfNeeded();
        if (enabled) applyNow();
    }

    public void setWeatherType(FixedWeatherType type) {
        if (type == null) return;
        this.weatherType = type;
        configManager.getConfig().set("weather-lock.weather", type.name());
        plugin.saveConfig();
        if (enabled) applyNow();
    }

    public void setTimeOfDay(FixedTimeOfDay tod) {
        if (tod == null) return;
        this.timeOfDay = tod;
        configManager.getConfig().set("weather-lock.time", tod.name());
        plugin.saveConfig();
        if (enabled) applyNow();
    }

    // ========== Lifecycle ==========

    /**
     * Reloads settings from config and restarts the task if needed.
     */
    public void reload() {
        cancelTask();
        loadSettings();
        startTaskIfNeeded();
        if (enabled) applyNow();
    }

    /**
     * Cancels the weather lock task. Called on plugin disable.
     */
    public void shutdown() {
        cancelTask();
    }

    // ========== Internal ==========

    private void loadSettings() {
        this.enabled = moduleManager.isEnabled(ModuleManager.WEATHER_LOCK);

        String w = configManager.getConfig().getString("weather-lock.weather", "CLEAR");
        String t = configManager.getConfig().getString("weather-lock.time", "DAY");

        this.weatherType = FixedWeatherType.fromString(w, FixedWeatherType.CLEAR);
        this.timeOfDay = FixedTimeOfDay.fromString(t, FixedTimeOfDay.DAY);
    }

    private void startTaskIfNeeded() {
        if (!enabled) {
            cancelTask();
            return;
        }

        if (weatherLockTask != null) return;

        weatherLockTask = Bukkit.getScheduler().runTaskTimer(plugin, this::applyNow, 20L, 20L * 60L);
    }

    private void cancelTask() {
        if (weatherLockTask != null) {
            weatherLockTask.cancel();
            weatherLockTask = null;
        }
    }

    /**
     * Applies weather and time lock to all worlds immediately.
     */
    public void applyNow() {
        if (!enabled) return;

        for (World world : Bukkit.getWorlds()) {
            world.setTime(timeOfDay.getTime());

            switch (weatherType) {
                case CLEAR -> {
                    world.setStorm(false);
                    world.setThundering(false);
                }
                case RAIN -> {
                    world.setStorm(true);
                    world.setThundering(false);
                }
                case THUNDER -> {
                    world.setStorm(true);
                    world.setThundering(true);
                }
            }

            world.setWeatherDuration(20 * 60 * 60);
            world.setThunderDuration(20 * 60 * 60);
        }
    }

    // ========== Enums ==========

    public enum FixedWeatherType {
        CLEAR, RAIN, THUNDER;

        public static FixedWeatherType fromString(String s, FixedWeatherType def) {
            if (s == null) return def;
            try {
                return FixedWeatherType.valueOf(s.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return def;
            }
        }
    }

    public enum FixedTimeOfDay {
        DAY(6000L),
        SUNSET(12000L),
        NIGHT(18000L),
        SUNRISE(23000L);

        private final long time;

        FixedTimeOfDay(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public static FixedTimeOfDay fromString(String s, FixedTimeOfDay def) {
            if (s == null) return def;
            try {
                return FixedTimeOfDay.valueOf(s.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return def;
            }
        }
    }
}
