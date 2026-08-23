package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.WeatherManager;

/**
 * Listener for the weather-lock system.
 * Enforces weather and time settings by cancelling unwanted changes.
 *
 * @author Pablito
 * @since 2.4
 */
public class WeatherLockListener implements Listener {

    private final PBLobbyCore plugin;

    public WeatherLockListener(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent e) {
        WeatherManager wm = plugin.getWeatherManager();
        if (!wm.isEnabled()) return;

        if (e.toWeatherState()) {
            // Weather is changing TO stormy
            if (wm.getWeatherType() == WeatherManager.FixedWeatherType.CLEAR) {
                e.setCancelled(true);
            }
            wm.applyNow();
        } else {
            // Weather is changing TO clear
            WeatherManager.FixedWeatherType desired = wm.getWeatherType();
            if (desired == WeatherManager.FixedWeatherType.RAIN || desired == WeatherManager.FixedWeatherType.THUNDER) {
                wm.applyNow();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent e) {
        WeatherManager wm = plugin.getWeatherManager();
        if (!wm.isEnabled()) return;

        WeatherManager.FixedWeatherType desired = wm.getWeatherType();

        if (e.toThunderState() && desired != WeatherManager.FixedWeatherType.THUNDER) {
            e.setCancelled(true);
            wm.applyNow();
        } else if (!e.toThunderState() && desired == WeatherManager.FixedWeatherType.THUNDER) {
            wm.applyNow();
        }
    }
}
