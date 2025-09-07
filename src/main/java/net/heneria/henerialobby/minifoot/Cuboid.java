package net.heneria.henerialobby.minifoot;

import org.bukkit.Location;
import org.bukkit.World;

public class Cuboid {
    private final World world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    public Cuboid(Location pos1, Location pos2) {
        this.world = pos1.getWorld();
        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public boolean contains(Location location) {
        if (!location.getWorld().equals(this.world)) {
            return false;
        }
        return location.getBlockX() >= minX && location.getBlockX() <= maxX &&
               location.getBlockY() >= minY && location.getBlockY() <= maxY &&
               location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ;
    }
}
