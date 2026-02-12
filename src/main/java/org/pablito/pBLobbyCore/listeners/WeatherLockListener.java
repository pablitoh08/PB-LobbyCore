package org.pablito.pBLobbyCore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;

public class WeatherLockListener implements Listener {

    private final PBLobbyCore plugin;

    public WeatherLockListener(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent e) {
        if (!plugin.isWeatherLockEnabled()) return;

        if (e.toWeatherState()) {
            PBLobbyCore.FixedWeatherType desired = plugin.getFixedWeatherType();
            if (desired == PBLobbyCore.FixedWeatherType.CLEAR) {
                e.setCancelled(true);
            }
            plugin.applyWeatherLockNow();
            return;
        }

        PBLobbyCore.FixedWeatherType desired = plugin.getFixedWeatherType();
        if (desired == PBLobbyCore.FixedWeatherType.RAIN || desired == PBLobbyCore.FixedWeatherType.THUNDER) {
            plugin.applyWeatherLockNow();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent e) {
        if (!plugin.isWeatherLockEnabled()) return;

        PBLobbyCore.FixedWeatherType desired = plugin.getFixedWeatherType();

        if (e.toThunderState() && desired != PBLobbyCore.FixedWeatherType.THUNDER) {
            e.setCancelled(true);
            plugin.applyWeatherLockNow();
            return;
        }

        if (!e.toThunderState() && desired == PBLobbyCore.FixedWeatherType.THUNDER) {
            plugin.applyWeatherLockNow();
        }
    }
}
