package net.heneria.henerialobby.sheep;

import org.bukkit.Location;

/**
 * Simple cuboid selection used by administrators to define
 * spawn regions for party sheep.
 */
public class Selection {
    private Location pos1;
    private Location pos2;

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null && pos1.getWorld().equals(pos2.getWorld());
    }
}

