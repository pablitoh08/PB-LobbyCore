package org.pablito.pBLobbyCore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Listener for PvP bypass protection.
 * Cancels PvP for players in the bypass list.
 *
 * <p>Optimized: uses HashSet for O(1) bypass lookups.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class PvpListener implements Listener {

    private final MessageManager messageManager;
    private final Set<String> pvpBypassPlayers;

    public PvpListener(MessageManager messageManager, java.util.List<String> bypassList) {
        this.messageManager = messageManager;
        this.pvpBypassPlayers = new HashSet<>(bypassList);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player damager)) return;

        if (pvpBypassPlayers.contains(victim.getName())) {
            event.setCancelled(true);
            damager.sendMessage(messageManager.getMessage("no-pvp"));
            return;
        }

        if (pvpBypassPlayers.contains(damager.getName())) return;

        event.setCancelled(true);
        damager.sendMessage(messageManager.getMessage("no-pvp"));
    }
}
