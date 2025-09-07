package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import net.heneria.henerialobby.selector.ServerSelector;
import net.heneria.henerialobby.visibility.VisibilityManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Slime;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public class MinifootGameListener implements Listener {

    private final HeneriaLobby plugin;
    private final MiniFootManager manager;
    private final Map<UUID, Long> pushCooldown = new HashMap<>();

    public MinifootGameListener(HeneriaLobby plugin, MiniFootManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        boolean wasIn = manager.isPlaying(player);
        boolean inArenaTo = manager.isInArena(event.getTo());
        boolean inArenaFrom = manager.isInArena(event.getFrom());
        if (!inArenaFrom && inArenaTo) {
            if (manager.getTotalPlayers() >= 8) {
                player.sendMessage("§cL'arène est pleine.");
                player.teleport(event.getFrom());
                return;
            }
            manager.handleEnter(player);
        } else if (wasIn && !inArenaTo) {
            manager.handleLeave(player);
            giveLobbyItems(player);
            plugin.updateDisplays(player);
        }

        if (!manager.isPlaying(player)) return;

        Slime ball = manager.getBall();
        if (ball == null || ball.isDead() || !ball.isValid()) return;
        if (!ball.getWorld().equals(player.getWorld())) return;

        double distanceSquared = ball.getLocation().distanceSquared(player.getLocation());
        if (distanceSquared > 1.0) return; // too far to push

        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long last = pushCooldown.get(id);
        if (last != null && now - last < 300) return; // cooldown 300ms

        pushCooldown.put(id, now);
        Vector direction = ball.getLocation().toVector().subtract(player.getLocation().toVector());
        direction.setY(0);
        if (direction.lengthSquared() == 0) return;
        direction.normalize().multiply(manager.getPushForce());
        ball.setVelocity(direction);
    }

    private void giveLobbyItems(Player player) {
        ServerSelector selector = plugin.getServerSelector();
        player.getInventory().setItem(selector.getSelectorSlot(), selector.getSelectorItem());
        VisibilityManager vm = plugin.getVisibilityManager();
        if (vm != null) {
            VisibilityManager.Mode mode = vm.getMode(player);
            player.getInventory().setItem(vm.getSlot(), new ItemStack(vm.getMaterial(mode)));
        }
        player.updateInventory();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!manager.isPlaying(player)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        Material type = item.getType();
        if (type == Material.LEATHER_HELMET || type == Material.LEATHER_CHESTPLATE
                || type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_BOOTS) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!manager.isPlaying(player)) return;
        Material type = event.getItemDrop().getItemStack().getType();
        if (type == Material.LEATHER_HELMET || type == Material.LEATHER_CHESTPLATE
                || type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_BOOTS) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!manager.isPlaying(player)) return;
        manager.handleLeave(player);
    }
}
