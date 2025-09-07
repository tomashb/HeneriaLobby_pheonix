package net.heneria.henerialobby.minifoot;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class MinifootSelectionListener implements Listener {

    private final MiniFootManager manager;

    public MinifootSelectionListener(MiniFootManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("heneria.lobby.admin.minifoot")) {
            return;
        }
        if (player.getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Location loc = event.getClickedBlock().getLocation();
            manager.setPos1(player, loc);
            player.sendMessage(ChatColor.GREEN + "Premier point défini: " + format(loc));
            event.setCancelled(true);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Location loc = event.getClickedBlock().getLocation();
            manager.setPos2(player, loc);
            player.sendMessage(ChatColor.GREEN + "Second point défini: " + format(loc));
            event.setCancelled(true);
        }
    }

    private String format(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
}

