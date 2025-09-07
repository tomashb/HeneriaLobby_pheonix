package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MiniFootManager {
    private final HeneriaLobby plugin;
    private final File configFile;
    private final FileConfiguration config;

    private Location arenaPos1;
    private Location arenaPos2;
    private final Set<UUID> playersInGame = new HashSet<>();
    private final Map<String, Set<UUID>> teamPlayers = new HashMap<>();
    private Slime ball;

    public MiniFootManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "minifoot.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
        reloadArenaPositions();

        // Task to ensure the ball is always present in the arena
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ball == null || ball.isDead() || !ball.isValid()) {
                    spawnBall();
                }
            }
        }.runTaskTimer(plugin, 0L, 100L);
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
        reloadArenaPositions();
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

    public void reloadArenaPositions() {
        this.arenaPos1 = loadLocationFromConfig(plugin, "arena.pos1");
        this.arenaPos2 = loadLocationFromConfig(plugin, "arena.pos2");
    }

    public Location getArenaPos1() {
        return arenaPos1;
    }

    public Location getArenaPos2() {
        return arenaPos2;
    }

    public boolean isInGame(Player player) {
        return playersInGame.contains(player.getUniqueId());
    }

    public void addPlayerToTeam(Player player) {
        int maxPlayers = config.getInt("max-players", 8);
        if (playersInGame.size() >= maxPlayers) {
            String msg = plugin.getMessage("minifoot.arena-full")
                    .replace("%current_players%", String.valueOf(playersInGame.size()))
                    .replace("%max_players%", String.valueOf(maxPlayers));
            player.sendMessage(plugin.applyPlaceholders(player, msg));
            return;
        }
        teamPlayers.computeIfAbsent("blue", k -> new HashSet<>());
        teamPlayers.computeIfAbsent("red", k -> new HashSet<>());

        String team = teamPlayers.get("blue").size() <= teamPlayers.get("red").size() ? "blue" : "red";
        teamPlayers.get(team).add(player.getUniqueId());
        playersInGame.add(player.getUniqueId());

        String teamColor = team.equals("blue") ? ChatColor.BLUE.toString() : ChatColor.RED.toString();
        String teamName = team.equals("blue") ? "Bleue" : "Rouge";
        String joinMsg = plugin.getMessage("minifoot.join-team")
                .replace("%team_color%", teamColor)
                .replace("%team_name%", teamName);
        player.sendMessage(plugin.applyPlaceholders(player, joinMsg));

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta;
        Color color = team.equals("blue") ? Color.BLUE : Color.RED;
        meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setColor(color);
        helmet.setItemMeta(meta);
        meta = (LeatherArmorMeta) chest.getItemMeta();
        meta.setColor(color);
        chest.setItemMeta(meta);
        meta = (LeatherArmorMeta) leggings.getItemMeta();
        meta.setColor(color);
        leggings.setItemMeta(meta);
        meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(color);
        boots.setItemMeta(meta);
        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chest);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);

        Location spawn = loadLocationFromConfig(plugin, "teams." + team + ".spawn");
        Location ballSpawn = loadLocationFromConfig(plugin, "ball-spawn");
        if (spawn != null) {
            if (ballSpawn != null) {
                spawn.setDirection(ballSpawn.toVector().subtract(spawn.toVector()));
            }
            plugin.getLogger().info("[DEBUG] Téléportation de " + player.getName() + " au spawn de l'équipe " + team + " aux coordonnées : " + spawn.toString());
            player.teleport(spawn);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> applyMiniFootScoreboard(player), 1L);
    }

    public void removePlayerFromGame(Player player) {
        for (var entry : teamPlayers.entrySet()) {
            entry.getValue().remove(player.getUniqueId());
        }
        playersInGame.remove(player.getUniqueId());

        plugin.updateDisplays(player);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        var selector = plugin.getServerSelector();
        if (selector != null) {
            player.getInventory().setItem(selector.getSelectorSlot(), selector.getSelectorItem());
        }

        var visibility = plugin.getVisibilityManager();
        if (visibility != null && plugin.isLobbyWorld(player.getWorld())) {
            var mode = visibility.getMode(player);
            player.getInventory().setItem(visibility.getSlot(), new ItemStack(visibility.getMaterial(mode)));
            visibility.apply(player);
            for (var other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(player)) {
                    visibility.apply(other);
                }
            }
        }

        player.sendMessage(ChatColor.RED + "Vous avez quitté la partie de mini-foot.");
    }

    public void spawnBall() {
        Location loc = loadLocationFromConfig(plugin, "ball-spawn");
        if (loc == null) {
            plugin.getLogger().warning("[DEBUG] Impossible de faire apparaitre le ballon : emplacement non défini.");
            return;
        }
        World world = loc.getWorld();
        if (world == null) {
            plugin.getLogger().warning("[DEBUG] Impossible de faire apparaitre le ballon : monde introuvable.");
            return;
        }
        ball = world.spawn(loc, Slime.class, slime -> {
            slime.setSize(1);
            slime.setAI(false);
            slime.setGravity(true);
            slime.setInvulnerable(true);
            slime.setSilent(true);
            slime.setCollidable(true);
        });
        plugin.getLogger().info("[DEBUG] Apparition du ballon aux coordonnées "
                + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ".");
    }

    private void applyMiniFootScoreboard(Player player) {
        var manager = Bukkit.getScoreboardManager();
        plugin.getLogger().info("[DEBUG] Tentative d'application du scoreboard de mini-foot pour " + player.getName());
        if (manager == null) {
            return;
        }
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("minifoot", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "★ MINI-FOOT ★");
        objective.getScore(ChatColor.WHITE + "Objectif : " + ChatColor.GREEN + "3 Buts" + ChatColor.RESET).setScore(6);
        objective.getScore(ChatColor.DARK_GRAY.toString()).setScore(5);
        objective.getScore(ChatColor.BLUE + "" + ChatColor.BOLD + "Équipe Bleue").setScore(4);
        objective.getScore(ChatColor.BLUE + "" + ChatColor.BOLD + "» " + ChatColor.WHITE + "0").setScore(3);
        objective.getScore(ChatColor.RED + "" + ChatColor.BOLD + "Équipe Rouge").setScore(2);
        objective.getScore(ChatColor.RED + "" + ChatColor.BOLD + "» " + ChatColor.WHITE + "0").setScore(1);
        player.setScoreboard(board);
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
