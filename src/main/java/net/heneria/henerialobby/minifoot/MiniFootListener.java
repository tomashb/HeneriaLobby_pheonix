package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MiniFootListener implements Listener {

    private final HeneriaLobby plugin;
    private final FileConfiguration miniFootConfig;

    public MiniFootListener(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.miniFootConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "minifoot.yml"));
    }

    private Location getLocation(String path) {
        if (!miniFootConfig.isConfigurationSection(path)) {
            return null;
        }
        String worldName = miniFootConfig.getString(path + ".world");
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        int x = miniFootConfig.getInt(path + ".x");
        int y = miniFootConfig.getInt(path + ".y");
        int z = miniFootConfig.getInt(path + ".z");
        return new Location(world, x, y, z);
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

        Location arenaPos1 = getLocation("arena.pos1");
        Location arenaPos2 = getLocation("arena.pos2");

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

