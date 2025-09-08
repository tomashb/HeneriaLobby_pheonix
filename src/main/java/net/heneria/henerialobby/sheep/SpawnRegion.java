package net.heneria.henerialobby.sheep;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Random;

/**
 * Represents a cuboid region where sheep can spawn.
 */
public class SpawnRegion {
    private final String name;
    private final World world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    public SpawnRegion(String name, Location pos1, Location pos2) {
        this.name = name;
        this.world = pos1.getWorld();
        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public String getName() {
        return name;
    }

    public World getWorld() {
        return world;
    }

    public Location getRandomLocation(Random rnd) {
        int x = rnd.nextInt(maxX - minX + 1) + minX;
        int z = rnd.nextInt(maxZ - minZ + 1) + minZ;
        int y = world.getHighestBlockYAt(x, z);
        if (y < minY) {
            y = minY;
        }
        if (y > maxY) {
            y = maxY;
        }
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

    public void save(ConfigurationSection section) {
        section.set("world", world.getName());
        section.set("pos1.x", minX);
        section.set("pos1.y", minY);
        section.set("pos1.z", minZ);
        section.set("pos2.x", maxX);
        section.set("pos2.y", maxY);
        section.set("pos2.z", maxZ);
    }

    public static SpawnRegion load(String name, ConfigurationSection sec) {
        String worldName = sec.getString("world");
        World w = Bukkit.getWorld(worldName);
        if (w == null) return null;
        int x1 = sec.getInt("pos1.x");
        int y1 = sec.getInt("pos1.y");
        int z1 = sec.getInt("pos1.z");
        int x2 = sec.getInt("pos2.x");
        int y2 = sec.getInt("pos2.y");
        int z2 = sec.getInt("pos2.z");
        Location pos1 = new Location(w, x1, y1, z1);
        Location pos2 = new Location(w, x2, y2, z2);
        return new SpawnRegion(name, pos1, pos2);
    }
}

