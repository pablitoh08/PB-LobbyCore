package org.pablito.pBLobbyCore.pvp;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.RegisteredListener;
import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.commands.BaseCommand;
import org.pablito.pBLobbyCore.managers.MessageManager;

/**
 * Diagnostic command showing EntityDamageByEntityEvent handler order.
 * Usage: /pvpdiag
 */
public class PvpDiagCommand extends BaseCommand {

    public PvpDiagCommand() {
        super(null, null, "pblcore.pvp.diag", true);
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        sender.sendMessage("§7[PB] Listeners de EntityDamageByEntityEvent (orden actual):");
        try {
            RegisteredListener[] listeners = EntityDamageByEntityEvent.getHandlerList().getRegisteredListeners();
            int i = 1;
            for (RegisteredListener rl : listeners) {
                String line = String.format("§8#%02d §7Plugin=§f%s §7Prio=§f%s §7IgnoreCancelled=§f%s",
                        i++, rl.getPlugin().getName(), rl.getPriority().name(), rl.getListener().getClass().getName());
                sender.sendMessage(line);
            }
        } catch (Throwable t) {
            sender.sendMessage("§cNo se pudo listar handlers: " + t.getClass().getSimpleName());
        }

        sender.sendMessage("§7Consejo: nuestro listener debe aparecer al final con §fMONITOR§7.");
    }
}
