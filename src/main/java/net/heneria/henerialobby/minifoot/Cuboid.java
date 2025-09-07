package net.heneria.henerialobby.minifoot;

import org.bukkit.Location;
import org.bukkit.World;

public class Cuboid {
    private final World world;
    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;

    public Cuboid(Location loc1, Location loc2) {
        this.world = loc1.getWorld();
        this.minX = Math.min(loc1.getX(), loc2.getX());
        this.minY = Math.min(loc1.getY(), loc2.getY());
        this.minZ = Math.min(loc1.getZ(), loc2.getZ());
        this.maxX = Math.max(loc1.getX(), loc2.getX());
        this.maxY = Math.max(loc1.getY(), loc2.getY());
        this.maxZ = Math.max(loc1.getZ(), loc2.getZ());
    }

    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() != world) {
            return false;
        }
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
}
