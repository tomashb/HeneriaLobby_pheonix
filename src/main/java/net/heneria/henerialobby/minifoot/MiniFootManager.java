package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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

    /**
     * Charge un emplacement depuis le fichier minifoot.yml de manière sécurisée.
     * @param plugin L'instance du plugin principal.
     * @param path Le chemin vers l'emplacement dans le YAML (ex: "arena.pos1").
     * @return L'objet Location si le chargement réussit, sinon null.
     */
    public Location loadLocationFromConfig(JavaPlugin plugin, String path) {
        File configFile = new File(plugin.getDataFolder(), "minifoot.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // 1. On vérifie que le chemin de base existe dans le fichier
        if (!config.isSet(path)) {
            plugin.getLogger().severe("[DEBUG-LOAD] ERREUR: Le chemin '" + path + "' est INTROUVABLE dans minifoot.yml.");
            return null;
        }

        // 2. On récupère le nom du monde
        String worldName = config.getString(path + ".world");
        if (worldName == null || worldName.isEmpty()) {
            plugin.getLogger().severe("[DEBUG-LOAD] ERREUR: Le nom du monde est manquant pour le chemin '" + path + "'.");
            return null;
        }

        // 3. On essaie de charger le monde via Bukkit
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().severe("[DEBUG-LOAD] ERREUR: Le monde '" + worldName + "' n'a pas pu être chargé ! Assurez-vous que le nom est correct.");
            return null;
        }

        // 4. On vérifie que les coordonnées existent avant de les charger
        if (!config.isSet(path + ".x") || !config.isSet(path + ".y") || !config.isSet(path + ".z")) {
            plugin.getLogger().severe("[DEBUG-LOAD] ERREUR: Une ou plusieurs coordonnées (x, y, z) sont manquantes pour le chemin '" + path + "'.");
            return null;
        }

        // 5. On charge les coordonnées
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");

        plugin.getLogger().info("[DEBUG-LOAD] L'emplacement pour le chemin '" + path + "' a été chargé avec succès.");
        return new Location(world, x, y, z);
    }

    private void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save minifoot.yml: " + e.getMessage());
        }
    }
}
