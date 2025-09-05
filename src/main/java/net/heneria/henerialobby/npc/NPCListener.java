package net.heneria.henerialobby.npc;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

/**
 * Handles interaction with NPCs.
 */
public class NPCListener implements Listener {

    private final NPCManager manager;

    public NPCListener(NPCManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;
        NPC npc = manager.getNPC(stand.getUniqueId());
        if (npc == null) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (player.isSneaking() && player.hasPermission("heneria.lobby.admin.npc")) {
            manager.select(player, npc);
            player.sendMessage("§aNPC sélectionné: " + npc.getName());
            return;
        }
        manager.executeAction(player, npc.getName());
    }
}
