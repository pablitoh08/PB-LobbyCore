package org.pablito.pBLobbyCore.commands;

import org.bukkit.command.CommandSender;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.MessageManager;
import org.pablito.pBLobbyCore.managers.WeatherManager;

import java.util.Arrays;
import java.util.List;

/**
 * Command to manage weather lock settings.
 * Usage: /weatherlock <on|off|toggle|status|weather|time>
 */
public class WeatherLockCommand extends BaseCommand {

    public WeatherLockCommand(PBLobbyCore plugin) {
        super(plugin, plugin.getMessageManager(), "pblcore.weatherlock.admin", false);
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        WeatherManager wm = plugin.getWeatherManager();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "on", "enable" -> {
                wm.setEnabled(true);
                sender.sendMessage("§8[§bPB-LobbyCore§8] §aWeatherLock enabled.");
            }
            case "off", "disable" -> {
                wm.setEnabled(false);
                sender.sendMessage("§8[§bPB-LobbyCore§8] §cWeatherLock disabled.");
            }
            case "toggle" -> {
                boolean newState = !wm.isEnabled();
                wm.setEnabled(newState);
                sender.sendMessage("§8[§bPB-LobbyCore§8] " + (newState ? "§aWeatherLock enabled." : "§cWeatherLock disabled."));
            }
            case "status" -> {
                sender.sendMessage("§8[§bPB-LobbyCore§8] §fWeatherLock: " +
                        (wm.isEnabled() ? "§aON" : "§cOFF"));
                sender.sendMessage("§8[§bPB-LobbyCore§8] §fWeather: §b" + wm.getWeatherType().name());
                sender.sendMessage("§8[§bPB-LobbyCore§8] §fTime: §b" + wm.getTimeOfDay().name());
            }
            case "weather" -> {
                if (args.length < 2) {
                    sender.sendMessage("§eUsage: /weatherlock weather <clear|rain|thunder>");
                    return;
                }
                WeatherManager.FixedWeatherType type = parseWeather(args[1]);
                if (type == null) {
                    sender.sendMessage("§cInvalid weather. Use: clear, rain, thunder");
                    return;
                }
                wm.setWeatherType(type);
                sender.sendMessage("§8[§bPB-LobbyCore§8] §aWeather set to §b" + type.name() + "§a (saved in config.yml).");
            }
            case "time" -> {
                if (args.length < 2) {
                    sender.sendMessage("§eUsage: /weatherlock time <day|sunset|night|sunrise>");
                    return;
                }
                WeatherManager.FixedTimeOfDay tod = parseTime(args[1]);
                if (tod == null) {
                    sender.sendMessage("§cInvalid time. Use: day, sunset, night, sunrise");
                    return;
                }
                wm.setTimeOfDay(tod);
                sender.sendMessage("§8[§bPB-LobbyCore§8] §aTime set to §b" + tod.name() + "§a (saved in config.yml).");
            }
            default -> sendHelp(sender);
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterCompletions(
                    Arrays.asList("on", "off", "toggle", "status", "weather", "time"), args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("weather")) {
                return filterCompletions(Arrays.asList("clear", "rain", "thunder"), args[1]);
            }
            if (sub.equals("time")) {
                return filterCompletions(Arrays.asList("day", "sunset", "night", "sunrise"), args[1]);
            }
        }
        return super.tabComplete(sender, args);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§eUsage:");
        sender.sendMessage("§e/weatherlock <on|off|toggle|status>");
        sender.sendMessage("§e/weatherlock weather <clear|rain|thunder>");
        sender.sendMessage("§e/weatherlock time <day|sunset|night|sunrise>");
    }

    private WeatherManager.FixedWeatherType parseWeather(String s) {
        if (s == null) return null;
        return switch (s.toLowerCase()) {
            case "clear", "sun", "sunny" -> WeatherManager.FixedWeatherType.CLEAR;
            case "rain", "rainy" -> WeatherManager.FixedWeatherType.RAIN;
            case "thunder", "storm", "thunderstorm" -> WeatherManager.FixedWeatherType.THUNDER;
            default -> null;
        };
    }

    private WeatherManager.FixedTimeOfDay parseTime(String s) {
        if (s == null) return null;
        return switch (s.toLowerCase()) {
            case "day", "noon" -> WeatherManager.FixedTimeOfDay.DAY;
            case "sunset", "dusk" -> WeatherManager.FixedTimeOfDay.SUNSET;
            case "night", "midnight" -> WeatherManager.FixedTimeOfDay.NIGHT;
            case "sunrise", "dawn" -> WeatherManager.FixedTimeOfDay.SUNRISE;
            default -> null;
        };
    }
}
