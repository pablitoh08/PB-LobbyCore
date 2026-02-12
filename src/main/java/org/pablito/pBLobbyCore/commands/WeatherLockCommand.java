package org.pablito.pBLobbyCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.pablito.pBLobbyCore.PBLobbyCore;

public class WeatherLockCommand implements CommandExecutor {

    private final PBLobbyCore plugin;

    public WeatherLockCommand(PBLobbyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission(PBLobbyCore.PERM_WEATHERLOCK_ADMIN)) {
            sender.sendMessage("§cNo tienes permiso para hacer esto.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "on", "enable" -> {
                plugin.setWeatherLockEnabled(true);
                sender.sendMessage("§8[§bPB-LobbyCore§8] §aWeatherLock enabled.");
            }
            case "off", "disable" -> {
                plugin.setWeatherLockEnabled(false);
                sender.sendMessage("§8[§bPB-LobbyCore§8] §cWeatherLock disabled.");
            }
            case "toggle" -> {
                boolean newState = !plugin.isWeatherLockEnabled();
                plugin.setWeatherLockEnabled(newState);
                sender.sendMessage("§8[§bPB-LobbyCore§8] " + (newState ? "§aWeatherLock enabled." : "§cWeatherLock disabled."));
            }
            case "status" -> {
                sender.sendMessage("§8[§bPB-LobbyCore§8] §fWeatherLock: " +
                        (plugin.isWeatherLockEnabled() ? "§aON" : "§cOFF"));
                sender.sendMessage("§8[§bPB-LobbyCore§8] §fWeather: §b" + plugin.getFixedWeatherType().name());
                sender.sendMessage("§8[§bPB-LobbyCore§8] §fTime: §b" + plugin.getFixedTimeOfDay().name());
            }
            case "weather" -> {
                if (args.length < 2) {
                    sender.sendMessage("§eUsage: /weatherlock weather <clear|rain|thunder>");
                    return true;
                }
                PBLobbyCore.FixedWeatherType type = parseWeather(args[1]);
                if (type == null) {
                    sender.sendMessage("§cInvalid weather. Use: clear, rain, thunder");
                    return true;
                }
                plugin.setWeatherLockWeather(type);
                sender.sendMessage("§8[§bPB-LobbyCore§8] §aWeather set to §b" + type.name() + "§a (saved in config.yml).");
            }
            case "time" -> {
                if (args.length < 2) {
                    sender.sendMessage("§eUsage: /weatherlock time <day|sunset|night|sunrise>");
                    return true;
                }
                PBLobbyCore.FixedTimeOfDay tod = parseTime(args[1]);
                if (tod == null) {
                    sender.sendMessage("§cInvalid time. Use: day, sunset, night, sunrise");
                    return true;
                }
                plugin.setWeatherLockTime(tod);
                sender.sendMessage("§8[§bPB-LobbyCore§8] §aTime set to §b" + tod.name() + "§a (saved in config.yml).");
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§eUsage:");
        sender.sendMessage("§e/" + "weatherlock <on|off|toggle|status>");
        sender.sendMessage("§e/" + "weatherlock weather <clear|rain|thunder>");
        sender.sendMessage("§e/" + "weatherlock time <day|sunset|night|sunrise>");
    }

    private PBLobbyCore.FixedWeatherType parseWeather(String s) {
        if (s == null) return null;
        return switch (s.toLowerCase()) {
            case "clear", "sun", "sunny" -> PBLobbyCore.FixedWeatherType.CLEAR;
            case "rain", "rainy" -> PBLobbyCore.FixedWeatherType.RAIN;
            case "thunder", "storm", "thunderstorm" -> PBLobbyCore.FixedWeatherType.THUNDER;
            default -> null;
        };
    }

    private PBLobbyCore.FixedTimeOfDay parseTime(String s) {
        if (s == null) return null;
        return switch (s.toLowerCase()) {
            case "day", "noon" -> PBLobbyCore.FixedTimeOfDay.DAY;
            case "sunset", "dusk" -> PBLobbyCore.FixedTimeOfDay.SUNSET;
            case "night", "midnight" -> PBLobbyCore.FixedTimeOfDay.NIGHT;
            case "sunrise", "dawn" -> PBLobbyCore.FixedTimeOfDay.SUNRISE;
            default -> null;
        };
    }
}
