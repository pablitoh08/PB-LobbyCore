package org.pablito.pBLobbyCore.pvp;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.pablito.pBLobbyCore.managers.MessageManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listener for the no-PvP system.
 * Handles PvP toggle, mutual consent, bypass, and force-override.
 *
 * <p>Optimized: caches config values and bypass list for fast access.</p>
 *
 * @author Pablito
 * @since 2.4
 */
public class NoPvpListener implements Listener {

    private final JavaPlugin plugin;
    private final MessageManager messages;
    private final PvpToggleStore store;

    /** Cached bypass player names for O(1) lookups. */
    private volatile Set<String> bypassPlayersCache = new HashSet<>();

    public NoPvpListener(JavaPlugin plugin, MessageManager messages, PvpToggleStore store) {
        this.plugin = plugin;
        this.messages = messages;
        this.store = store;
        reloadBypassCache();
    }

    /**
     * Reloads the bypass players cache from config.
     */
    public void reloadBypassCache() {
        List<String> bypass = plugin.getConfig().getStringList("pvp-bypass-players");
        this.bypassPlayersCache = new HashSet<>(bypass);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent e) {
        Player victim = (e.getEntity() instanceof Player) ? (Player) e.getEntity() : null;
        Player attacker = resolveAttacker(e.getDamager());
        if (victim == null || attacker == null) return;

        if (!plugin.getConfig().getBoolean("modules.no-pvp", true)) return;

        boolean allowToggle = plugin.getConfig().getBoolean("no-pvp.allow-pvp-toggle", true);
        boolean requireMutual = plugin.getConfig().getBoolean("no-pvp.require-mutual-consent", true);
        boolean bypassRespects = plugin.getConfig().getBoolean("no-pvp.bypass-respects-toggle", true);
        boolean forceOverride = plugin.getConfig().getBoolean("no-pvp.force-override-external", true);

        if (!allowToggle) {
            e.setCancelled(true);
            return;
        }

        boolean atkOn = store.isEnabled(attacker.getUniqueId());
        boolean vicOn = store.isEnabled(victim.getUniqueId());

        boolean atkBypass = bypassPlayersCache.contains(attacker.getName())
                || attacker.hasPermission("pblcore.pvp.bypass");
        boolean vicBypass = bypassPlayersCache.contains(victim.getName())
                || victim.hasPermission("pblcore.pvp.bypass");

        if (!bypassRespects && (atkBypass || vicBypass)) {
            if (forceOverride) forceAllow(e, victim);
            return;
        }

        boolean permitir = requireMutual ? (atkOn && vicOn) : atkOn;

        if (permitir) {
            if (forceOverride) forceAllow(e, victim);
        } else {
            e.setCancelled(true);
            if (requireMutual) attacker.sendMessage(messages.getMessage("pvp-require-mutual"));
            else attacker.sendMessage(messages.getMessage("pvp-status-off"));
        }
    }

    private void forceAllow(EntityDamageEvent e, Player victim) {
        e.setCancelled(false);
        if (victim != null) {
            try {
                victim.setNoDamageTicks(0);
            } catch (Throwable ignored) {
            }
        }
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player) return (Player) damager;
        if (damager instanceof Projectile) {
            Object shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player) return (Player) shooter;
        }
        return null;
    }
}
