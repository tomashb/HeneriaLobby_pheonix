package net.heneria.henerialobby.listener;

import net.heneria.henerialobby.HeneriaLobby;
import net.heneria.henerialobby.selector.ServerSelector;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;

public class ProtectionListener implements Listener {

    private final HeneriaLobby plugin;
    private final Set<String> worlds;
    private final long lockTime;
    private final ServerSelector selector;

    public ProtectionListener(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.worlds = new HashSet<>(plugin.getConfig().getStringList("lobby-worlds"));
        this.lockTime = plugin.getConfig().getLong("protection.lock-time-to", 6000L);
        this.selector = plugin.getServerSelector();

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (String name : worlds) {
                World world = Bukkit.getWorld(name);
                if (world != null) {
                    world.setTime(lockTime);
                    world.setStorm(false);
                    world.setThundering(false);
                }
            }
        }, 0L, 100L);
    }

    private boolean isProtected(World world) {
        return world != null && worlds.contains(world.getName());
    }

    private boolean canBypass(Player player) {
        return player.isOp() || player.hasPermission("heneria.lobby.bypass.protection");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (isProtected(player.getWorld()) && !canBypass(player)) {
                player.setGameMode(GameMode.ADVENTURE);
            }
        });
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isProtected(event.getBlock().getWorld()) && !canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isProtected(event.getBlock().getWorld()) && !canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && isProtected(player.getWorld()) && !canBypass(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && isProtected(player.getWorld()) && !canBypass(player)) {
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isProtected(player.getWorld()) && !canBypass(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!isProtected(player.getWorld()) || canBypass(player)) {
            return;
        }
        if (selector != null && selector.isMenu(event.getView().getTitle())) {
            return;
        }
        event.setCancelled(true);
    }
}
