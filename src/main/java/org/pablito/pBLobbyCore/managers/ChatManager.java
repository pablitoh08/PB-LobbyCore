package org.pablito.pBLobbyCore.managers;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Manages the chat lock state.
 *
 * @author Pablito
 * @since 2.4
 */
public final class ChatManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ModuleManager moduleManager;

    private volatile boolean chatLocked = false;

    public ChatManager(JavaPlugin plugin, ConfigManager configManager, ModuleManager moduleManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager cannot be null");
    }

    /**
     * Checks if chat is currently locked.
     */
    public boolean isChatLocked() {
        return chatLocked;
    }

    /**
     * Sets the chat lock state.
     */
    public void setChatLocked(boolean locked) {
        this.chatLocked = locked;
    }

    /**
     * Toggles the chat lock state.
     *
     * @return the new state
     */
    public boolean toggle() {
        chatLocked = !chatLocked;
        return chatLocked;
    }

    /**
     * Checks if the chat-lock module is enabled.
     */
    public boolean isModuleEnabled() {
        return moduleManager.isEnabled(ModuleManager.CHAT_LOCK);
    }
}
