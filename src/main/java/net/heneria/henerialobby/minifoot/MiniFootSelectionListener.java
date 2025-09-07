package net.heneria.henerialobby.minifoot;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MiniFootSelectionListener implements Listener {

    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    public Location getPos1(UUID uuid) {
        return pos1.get(uuid);
    }

    public Location getPos2(UUID uuid) {
        return pos2.get(uuid);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("heneria.lobby.admin.minifoot")) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE) return;
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (action == Action.LEFT_CLICK_BLOCK) {
            pos1.put(player.getUniqueId(), block.getLocation());
            player.sendMessage(ChatColor.GREEN + "Position 1 définie.");
            event.setCancelled(true);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            pos2.put(player.getUniqueId(), block.getLocation());
            player.sendMessage(ChatColor.GREEN + "Position 2 définie.");
            event.setCancelled(true);
        }
    }
}
