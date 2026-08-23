package org.pablito.pBLobbyCore.managers;

import org.bukkit.plugin.java.JavaPlugin;
import org.pablito.pBLobbyCore.pvp.PvpToggleStore;

import java.util.Objects;

/**
 * Manages the PvP toggle system including persistent storage.
 *
 * @author Pablito
 * @since 2.4
 */
public final class PvPManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ModuleManager moduleManager;

    private PvpToggleStore pvpToggleStore;

    public PvPManager(JavaPlugin plugin, ConfigManager configManager, ModuleManager moduleManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager cannot be null");
    }

    /**
     * Gets or creates the PvpToggleStore.
     * Lazy initialization to avoid creating the store if the module is disabled.
     */
    public PvpToggleStore getToggleStore() {
        if (pvpToggleStore == null && moduleManager.isEnabled(ModuleManager.NO_PVP)) {
            boolean persistent = configManager.getConfig().getBoolean("no-pvp.save-toggle-on-restart", false);
            this.pvpToggleStore = new PvpToggleStore(plugin, persistent);
        }
        return pvpToggleStore;
    }

    /**
     * Checks if the no-pvp module is enabled.
     */
    public boolean isModuleEnabled() {
        return moduleManager.isEnabled(ModuleManager.NO_PVP);
    }

    /**
     * Reloads the PvP store if it exists.
     */
    public void reload() {
        if (pvpToggleStore != null) {
            boolean persistent = configManager.getConfig().getBoolean("no-pvp.save-toggle-on-restart", false);
            this.pvpToggleStore = new PvpToggleStore(plugin, persistent);
        }
    }
}
