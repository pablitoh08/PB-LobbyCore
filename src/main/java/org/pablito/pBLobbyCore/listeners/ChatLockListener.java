package org.pablito.pBLobbyCore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.managers.ChatManager;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listener for the chat-lock system.
 * Cancels chat events when chat is locked (with bypass support).
 *
 * <p>Optimized: caches bypass player names to avoid config reads on every chat event.
 * Uses EventPriority.HIGHEST to run before most other plugins.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class ChatLockListener implements Listener {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;

    /** Cached set of bypass player names for fast lookup. */
    private volatile Set<String> bypassCache = new HashSet<>();

    public ChatLockListener(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        reloadBypassCache();
    }

    /**
     * Reloads the bypass player cache from config.
     * Should be called when config is reloaded.
     */
    public void reloadBypassCache() {
        List<String> bypassList = plugin.getConfig().getStringList("chat-bypass-players");
        this.bypassCache = new HashSet<>(bypassList);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        ChatManager chatManager = plugin.getChatManager();
        if (!chatManager.isChatLocked()) return;

        Player player = event.getPlayer();

        if (player.hasPermission("pblcore.chatlock.bypass")) return;
        if (bypassCache.contains(player.getName())) return;

        event.setCancelled(true);
        player.sendMessage(messageManager.getMessage("chat-locked"));
    }
}
