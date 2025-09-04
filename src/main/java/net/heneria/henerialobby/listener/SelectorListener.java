package net.heneria.henerialobby.listener;

import net.heneria.henerialobby.HeneriaLobby;
import net.heneria.henerialobby.selector.ServerSelector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class SelectorListener implements Listener {

    private final HeneriaLobby plugin;
    private final ServerSelector selector;

    public SelectorListener(HeneriaLobby plugin, ServerSelector selector) {
        this.plugin = plugin;
        this.selector = selector;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.getInventory().setItem(selector.getSelectorSlot(), selector.getSelectorItem());
        });
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (selector.isSelectorItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (selector.isSelectorItem(event.getCurrentItem())) {
            event.setCancelled(true);
        }
        if (selector.isMenu(event.getView().getTitle())) {
            event.setCancelled(true);
            String action = selector.getAction((Player) event.getWhoClicked(), event.getRawSlot());
            if (action != null && action.startsWith("server:")) {
                String server = action.split(":", 2)[1];
                plugin.sendPlayer((Player) event.getWhoClicked(), server);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (selector.isSelectorItem(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && selector.isSelectorItem(event.getItem())) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
                    || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                selector.openMenu(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        selector.clearActions(event.getPlayer().getUniqueId());
    }
}
