package org.pablito.pBLobbyCore.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.pablito.pBLobbyCore.utils.MessageManager;

import java.util.List;

public class PvpListener implements Listener {

    private final MessageManager messageManager;
    private final List<String> pvpBypassPlayers;

    public PvpListener(MessageManager messageManager, FileConfiguration config) {
        this.messageManager = messageManager;
        this.pvpBypassPlayers = config.getStringList("pvp-bypass-players");
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            if (pvpBypassPlayers.contains(victim.getName())) {
                event.setCancelled(true);
                damager.sendMessage(messageManager.getMessage("no-pvp"));
                return;
            }

            if (pvpBypassPlayers.contains(damager.getName())) {
                return;
            }

            event.setCancelled(true);
            damager.sendMessage(messageManager.getMessage("no-pvp"));
        }
    }
}