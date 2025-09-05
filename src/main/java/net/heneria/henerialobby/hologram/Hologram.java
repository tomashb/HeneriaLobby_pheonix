package net.heneria.henerialobby.hologram;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single hologram composed of multiple ArmorStands stacked on top of
 * each other. Each line is displayed by an invisible ArmorStand.
 */
public class Hologram {

    private final HeneriaLobby plugin;
    private final String name;
    private Location location;
    private final List<String> lines;
    private final List<ArmorStand> stands = new ArrayList<>();

    public Hologram(HeneriaLobby plugin, String name, Location location, List<String> lines) {
        this.plugin = plugin;
        this.name = name;
        this.location = location.clone();
        this.lines = new ArrayList<>(lines);
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location.clone();
    }

    public List<String> getLines() {
        return lines;
    }

    /**
     * Spawns the ArmorStands representing this hologram.
     */
    public void spawn() {
        remove();
        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String parsed = plugin.applyPlaceholders(null, raw);
            parsed = ChatColor.translateAlternateColorCodes('&', parsed);
            Location loc = location.clone().subtract(0, 0.25 * i, 0);
            ArmorStand stand = location.getWorld().spawn(loc, ArmorStand.class, as -> {
                as.setInvisible(true);
                as.setGravity(false);
                as.setMarker(true);
                as.setCustomName(parsed);
                as.setCustomNameVisible(true);
            });
            stands.add(stand);
        }
    }

    /**
     * Removes all ArmorStands of this hologram.
     */
    public void remove() {
        for (ArmorStand stand : stands) {
            stand.remove();
        }
        stands.clear();
    }

    /**
     * Updates the custom names of the ArmorStands to refresh placeholders.
     */
    public void update() {
        for (int i = 0; i < stands.size(); i++) {
            ArmorStand stand = stands.get(i);
            String parsed = plugin.applyPlaceholders(null, lines.get(i));
            parsed = ChatColor.translateAlternateColorCodes('&', parsed);
            stand.setCustomName(parsed);
        }
    }

    public void move(Location newLocation) {
        this.location = newLocation.clone();
        spawn();
    }

    public void addLine(String text) {
        lines.add(text);
        spawn();
    }

    public void setLine(int index, String text) {
        lines.set(index, text);
        spawn();
    }
}
