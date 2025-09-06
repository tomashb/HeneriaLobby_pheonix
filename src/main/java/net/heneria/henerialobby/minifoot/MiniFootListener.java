package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MiniFootListener implements Listener {

    private final HeneriaLobby plugin;
    private final MiniFootManager miniFootManager;

    public MiniFootListener(HeneriaLobby plugin, MiniFootManager miniFootManager) {
        this.plugin = plugin;
        this.miniFootManager = miniFootManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Location playerLocation = event.getTo();
        if (playerLocation == null) {
            return;
        }

        Location arenaPos1 = miniFootManager.loadLocationFromConfig(plugin, "arena.pos1");
        Location arenaPos2 = miniFootManager.loadLocationFromConfig(plugin, "arena.pos2");

        if (arenaPos1 == null || arenaPos2 == null) {
            return;
        }

        plugin.getLogger().info("[DEBUG] Joueur '" + player.getName() + "' est à : " +
                "MONDE=" + playerLocation.getWorld().getName() + ", " +
                "X=" + playerLocation.getBlockX() + ", " +
                "Y=" + playerLocation.getBlockY() + ", " +
                "Z=" + playerLocation.getBlockZ());

        int minX = Math.min(arenaPos1.getBlockX(), arenaPos2.getBlockX());
        int maxX = Math.max(arenaPos1.getBlockX(), arenaPos2.getBlockX());
        int minY = Math.min(arenaPos1.getBlockY(), arenaPos2.getBlockY());
        int maxY = Math.max(arenaPos1.getBlockY(), arenaPos2.getBlockY());
        int minZ = Math.min(arenaPos1.getBlockZ(), arenaPos2.getBlockZ());
        int maxZ = Math.max(arenaPos1.getBlockZ(), arenaPos2.getBlockZ());

        plugin.getLogger().info("[DEBUG] Zone Arène chargée : " +
                "MONDE=" + arenaPos1.getWorld().getName() + ", " +
                "X=" + minX + " à " + maxX + ", " +
                "Y=" + minY + " à " + maxY + ", " +
                "Z=" + minZ + " à " + maxZ);

        // La logique de vérification qui suit...
    }
}

