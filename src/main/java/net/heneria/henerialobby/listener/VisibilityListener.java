package net.heneria.henerialobby.listener;

import net.heneria.henerialobby.HeneriaLobby;
import net.heneria.henerialobby.visibility.VisibilityManager;
import net.heneria.henerialobby.visibility.VisibilityManager.Mode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class VisibilityListener implements Listener {

    private final HeneriaLobby plugin;
    private final VisibilityManager manager;
    private final int slot;
    private final Material matAll;
    private final Material matVips;
    private final Material matNone;
    private final long cooldown;
    private final Map<UUID, Long> lastUse = new HashMap<>();

    public VisibilityListener(HeneriaLobby plugin, VisibilityManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.slot = manager.getSlot();
        this.matAll = manager.getMaterial(Mode.ALL);
        this.matVips = manager.getMaterial(Mode.VIPS);
        this.matNone = manager.getMaterial(Mode.NONE);
        this.cooldown = manager.getCooldown();
    }

    private ItemStack createItem(Player player, Mode mode) {
        ItemStack item = new ItemStack(manager.getMaterial(mode));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.applyPlaceholders(player, manager.getItemName())));
            var lore = manager.getItemLore();
            if (!lore.isEmpty()) {
                meta.setLore(lore.stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', plugin.applyPlaceholders(player, line)))
                        .collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (!plugin.isLobbyWorld(player.getWorld())) {
            return;
        }
        Mode mode = manager.getMode(player);
        player.getInventory().setItem(slot, createItem(player, mode));
        manager.apply(player);
        for (var other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                manager.apply(other);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        var player = event.getPlayer();
        if (player.getInventory().getHeldItemSlot() != slot) return;
        long now = System.currentTimeMillis();
        long last = lastUse.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < cooldown) {
            long remaining = (cooldown - (now - last)) / 1000L + 1;
            String msg = plugin.getMessage("visibility-cooldown").replace("%seconds%", String.valueOf(remaining));
            player.sendMessage(plugin.applyPlaceholders(player, msg));
            event.setCancelled(true);
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) return;
        Material type = item.getType();
        if (type != matAll && type != matVips && type != matNone) return;

        Mode current = manager.getMode(player);
        Mode next = switch (current) {
            case ALL -> Mode.VIPS;
            case VIPS -> Mode.NONE;
            case NONE -> Mode.ALL;
        };

        manager.setMode(player, next);
        player.getInventory().setItem(slot, createItem(player, next));
        lastUse.put(player.getUniqueId(), now);
        manager.apply(player);
        for (var other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                manager.apply(other);
            }
        }
        String key = switch (next) {
            case ALL -> "visibility-all";
            case VIPS -> "visibility-vips";
            case NONE -> "visibility-none";
        };
        player.sendMessage(plugin.applyPlaceholders(player, plugin.getMessage(key)));
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlot() == slot && event.getCurrentItem() != null) {
            Material type = event.getCurrentItem().getType();
            if (type == matAll || type == matVips || type == matNone) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Material type = event.getItemDrop().getItemStack().getType();
        if (type == matAll || type == matVips || type == matNone) {
            event.setCancelled(true);
        }
    }
}
