package org.pablito.pBLobbyCore.pvp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PvpToggleStore {

    private final JavaPlugin plugin;
    private final boolean persistent;
    private final File dataFile;
    private final YamlConfiguration data;

    private final Set<UUID> enabledNow = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastUse = new ConcurrentHashMap<>();

    public PvpToggleStore(JavaPlugin plugin, boolean persistent) {
        this.plugin = plugin;
        this.persistent = persistent;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        this.data = new YamlConfiguration();

        if (persistent) {
            try {
                if (!dataFile.exists()) dataFile.createNewFile();
                data.load(dataFile);
                if (data.isConfigurationSection("pvp")) {
                    for (String key : Objects.requireNonNull(data.getConfigurationSection("pvp")).getKeys(false)) {
                        try {
                            UUID uuid = UUID.fromString(key);
                            if (data.getBoolean("pvp." + key, false)) {
                                enabledNow.add(uuid);
                            }
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public boolean isEnabled(UUID uuid) { return enabledNow.contains(uuid); }

    public void setEnabled(UUID uuid, boolean enabled) {
        if (enabled) enabledNow.add(uuid); else enabledNow.remove(uuid);
        if (persistent) {
            data.set("pvp." + uuid.toString(), enabled);
            try { data.save(dataFile); } catch (IOException ignored) {}
        }
    }

    public void setLastUse(UUID uuid, long millis) { lastUse.put(uuid, millis); }
    public long getLastUse(UUID uuid) { return lastUse.getOrDefault(uuid, 0L); }

    public boolean isBypass(String playerName) {
        List<String> bypass = plugin.getConfig().getStringList("pvp-bypass-players");
        for (String n : bypass) if (n.equalsIgnoreCase(playerName)) return true;
        return false;
    }
}
