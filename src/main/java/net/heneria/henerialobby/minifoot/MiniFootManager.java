package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MiniFootManager {
    private final HeneriaLobby plugin;
    private final File configFile;
    private final FileConfiguration config;

    public MiniFootManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "minifoot.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void setLocation(String path, Location loc) {
        Map<String, Object> map = new HashMap<>();
        map.put("world", loc.getWorld().getName());
        map.put("x", loc.getBlockX());
        map.put("y", loc.getBlockY());
        map.put("z", loc.getBlockZ());
        config.createSection(path, map);
        save();
    }

    public void setArenaPos(int index, Location loc) {
        config.set("arena.world", loc.getWorld().getName());
        setLocation("arena.pos" + index, loc);
    }

    public void setTeamSpawn(String team, Location loc) {
        setLocation("teams." + team + ".spawn", loc);
    }

    public void setTeamGoalPos(String team, int index, Location loc) {
        setLocation("teams." + team + ".goal.pos" + index, loc);
    }

    public void setBallSpawn(Location loc) {
        setLocation("ball-spawn", loc);
    }

    private void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save minifoot.yml: " + e.getMessage());
        }
    }
}
