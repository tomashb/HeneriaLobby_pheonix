package net.heneria.henerialobby.minifoot;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MiniFootListener implements Listener {

    private final MiniFootManager manager;

    public MiniFootListener(MiniFootManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        Cuboid arena = manager.getArenaZone();
        if (arena == null) return;
        boolean isNowInside = arena.contains(to);
        boolean wasInside = arena.contains(from);
        if (isNowInside && !wasInside) {
            manager.addPlayerToTeam(player);
        } else if (!isNowInside && wasInside) {
            manager.removePlayerFromGame(player);
        }
        if (manager.isInGame(player)) {
            manager.pushBall(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.removePlayerFromGame(event.getPlayer());
    }

    @EventHandler
    public void onArmorRemove(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (manager.isInGame(player) && event.getSlot() >= 36 && event.getSlot() <= 39) {
                event.setCancelled(true);
            }
        }
    }
}
