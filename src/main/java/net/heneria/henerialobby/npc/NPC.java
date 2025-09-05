package net.heneria.henerialobby.npc;

import org.bukkit.entity.ArmorStand;

/**
 * Simple container representing a managed NPC backed by an ArmorStand.
 */
public class NPC {

    private final String name;
    private final ArmorStand stand;

    public NPC(String name, ArmorStand stand) {
        this.name = name;
        this.stand = stand;
    }

    public String getName() {
        return name;
    }

    public ArmorStand getStand() {
        return stand;
    }
}
