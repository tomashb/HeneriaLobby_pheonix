package net.heneria.henerialobby.sheep;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles the wooden-axe selection for defining spawn regions.
 */
public class SelectionListener implements Listener {

    private final PartySheepManager manager;

    public SelectionListener(PartySheepManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE) {
            return;
        }
        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : null;
        if (loc == null) return;

        Selection sel = manager.getOrCreateSelection(player);
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            sel.setPos1(loc);
            player.sendMessage(ChatColor.GREEN + "Position 1 définie : " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
            event.setCancelled(true);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            sel.setPos2(loc);
            player.sendMessage(ChatColor.GREEN + "Position 2 définie : " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
            event.setCancelled(true);
        }
    }
}

