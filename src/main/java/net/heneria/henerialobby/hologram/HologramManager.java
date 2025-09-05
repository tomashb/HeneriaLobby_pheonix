package net.heneria.henerialobby.hologram;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles persistence and management of all holograms on the server.
 */
public class HologramManager {

    private final HeneriaLobby plugin;
    private final Map<String, Hologram> holograms = new HashMap<>();
    private final File file;
    private final FileConfiguration config;

    public HologramManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "holograms.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create holograms.yml");
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadAll();
        long interval = plugin.getConfig().getLong("holograms.update-interval", 100L);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::refreshAll, interval, interval);
    }

    private void loadAll() {
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;
            String world = section.getString("world");
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            List<String> lines = section.getStringList("lines");
            if (world == null) continue;
            Location loc = new Location(Bukkit.getWorld(world), x, y, z);
            Hologram holo = new Hologram(plugin, key, loc, lines);
            holo.spawn();
            holograms.put(key.toLowerCase(), holo);
        }
    }

    public void saveAll() {
        for (String k : config.getKeys(false)) { config.set(k, null); }
        for (Hologram holo : holograms.values()) {
            ConfigurationSection section = config.createSection(holo.getName());
            Location loc = holo.getLocation();
            section.set("world", loc.getWorld().getName());
            section.set("x", loc.getX());
            section.set("y", loc.getY());
            section.set("z", loc.getZ());
            section.set("lines", holo.getLines());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save holograms.yml");
        }
    }

    public boolean create(String name, Location loc, String line) {
        if (holograms.containsKey(name.toLowerCase())) {
            return false;
        }
        Hologram holo = new Hologram(plugin, name, loc, java.util.List.of(line));
        holo.spawn();
        holograms.put(name.toLowerCase(), holo);
        saveAll();
        return true;
    }

    public boolean delete(String name) {
        Hologram holo = holograms.remove(name.toLowerCase());
        if (holo == null) return false;
        holo.remove();
        saveAll();
        return true;
    }

    public boolean addLine(String name, String text) {
        Hologram holo = holograms.get(name.toLowerCase());
        if (holo == null) return false;
        holo.addLine(text);
        saveAll();
        return true;
    }

    public boolean setLine(String name, int index, String text) {
        Hologram holo = holograms.get(name.toLowerCase());
        if (holo == null) return false;
        if (index < 0 || index >= holo.getLines().size()) return false;
        holo.setLine(index, text);
        saveAll();
        return true;
    }

    public boolean move(String name, Location loc) {
        Hologram holo = holograms.get(name.toLowerCase());
        if (holo == null) return false;
        holo.move(loc);
        saveAll();
        return true;
    }

    /**
     * Returns a set of all hologram names.
     */
    public java.util.Set<String> getNames() {
        return new java.util.HashSet<>(holograms.keySet());
    }

    public void refreshAll() {
        holograms.values().forEach(Hologram::update);
    }

    public void removeAll() {
        holograms.values().forEach(Hologram::remove);
        holograms.clear();
    }
}
