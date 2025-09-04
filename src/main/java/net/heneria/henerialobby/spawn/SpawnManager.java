package net.heneria.henerialobby.spawn;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class SpawnManager {

    private final HeneriaLobby plugin;
    private final File spawnFile;
    private final FileConfiguration spawnConfig;
    private Location spawnLocation;

    public SpawnManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        if (!spawnFile.exists()) {
            this.spawnConfig = new YamlConfiguration();
        } else {
            this.spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
            loadSpawn();
        }
    }

    public void saveSpawn(Location location) {
        this.spawnLocation = location;
        spawnConfig.set("location.world", location.getWorld().getName());
        spawnConfig.set("location.x", location.getX());
        spawnConfig.set("location.y", location.getY());
        spawnConfig.set("location.z", location.getZ());
        spawnConfig.set("location.yaw", location.getYaw());
        spawnConfig.set("location.pitch", location.getPitch());
        try {
            spawnConfig.save(spawnFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSpawn() {
        if (!spawnConfig.contains("location.world")) {
            return;
        }
        String worldName = spawnConfig.getString("location.world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' defined in spawn.yml does not exist!");
            return;
        }
        double x = spawnConfig.getDouble("location.x");
        double y = spawnConfig.getDouble("location.y");
        double z = spawnConfig.getDouble("location.z");
        float yaw = (float) spawnConfig.getDouble("location.yaw");
        float pitch = (float) spawnConfig.getDouble("location.pitch");
        spawnLocation = new Location(world, x, y, z, yaw, pitch);
    }

    public boolean teleport(Player player) {
        if (spawnLocation == null) {
            return false;
        }
        player.teleport(spawnLocation);
        return true;
    }

    public boolean hasSpawn() {
        return spawnLocation != null;
    }
}

