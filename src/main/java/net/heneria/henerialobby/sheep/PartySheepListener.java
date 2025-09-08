package net.heneria.henerialobby.sheep;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Listener that reacts when a party sheep is hit by a player.
 */
public class PartySheepListener implements Listener {
    private final PartySheepManager manager;

    public PartySheepListener(PartySheepManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Sheep sheep)) return;
        if (!sheep.hasMetadata(PartySheepManager.METADATA)) return;
        event.setCancelled(true);
        manager.handleHit(sheep);
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Sheep sheep)) return;
        if (!sheep.hasMetadata(PartySheepManager.METADATA)) return;
        Player player = event.getPlayer();
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!player.getInventory().getItemInMainHand().getType().isAir()) return;
        sheep.addPassenger(player);
    }
}
