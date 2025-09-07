package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MiniFootManager {

    private final HeneriaLobby plugin;
    private final File file;
    private final FileConfiguration config;
    private final Map<UUID, Selection> selections = new HashMap<>();

    public MiniFootManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "minifoot.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    private Selection get(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), k -> new Selection());
    }

    public void setPos1(Player player, Location loc) {
        get(player).pos1 = loc;
    }

    public void setPos2(Player player, Location loc) {
        get(player).pos2 = loc;
    }

    public boolean hasSelection(Player player) {
        Selection s = selections.get(player.getUniqueId());
        return s != null && s.pos1 != null && s.pos2 != null;
    }

    public Location getPos1(Player player) {
        Selection s = selections.get(player.getUniqueId());
        return s == null ? null : s.pos1;
    }

    public Location getPos2(Player player) {
        Selection s = selections.get(player.getUniqueId());
        return s == null ? null : s.pos2;
    }

    public void saveArena(Location pos1, Location pos2) {
        config.set("arena.pos1", pos1);
        config.set("arena.pos2", pos2);
        save();
    }

    public void saveGoal(String team, Location pos1, Location pos2) {
        config.set("goal." + team + ".pos1", pos1);
        config.set("goal." + team + ".pos2", pos2);
        save();
    }

    public void saveSpawn(String team, Location loc) {
        config.set("spawn." + team, loc);
        save();
    }

    public void saveBallSpawn(Location loc) {
        config.set("ballspawn", loc);
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Selection {
        Location pos1;
        Location pos2;
    }
}

