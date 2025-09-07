package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

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
    private final java.util.Set<UUID> blueTeam = new java.util.HashSet<>();
    private final java.util.Set<UUID> redTeam = new java.util.HashSet<>();
    private Slime ball;
    private int respawnCountdown = -1;

    public MiniFootManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "minifoot.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        startBallTask();
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

    private Location getBallSpawn() {
        return config.getLocation("ballspawn");
    }

    public boolean isInArena(Location loc) {
        Location pos1 = config.getLocation("arena.pos1");
        Location pos2 = config.getLocation("arena.pos2");
        if (pos1 == null || pos2 == null || loc == null || loc.getWorld() == null) {
            return false;
        }
        if (!loc.getWorld().equals(pos1.getWorld())) {
            return false;
        }
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    public enum Team {
        BLUE,
        RED
    }

    public boolean isPlaying(Player player) {
        UUID id = player.getUniqueId();
        return blueTeam.contains(id) || redTeam.contains(id);
    }

    private Team smallerTeam() {
        return blueTeam.size() <= redTeam.size() ? Team.BLUE : Team.RED;
    }

    public int getTotalPlayers() {
        return blueTeam.size() + redTeam.size();
    }

    private ItemStack[] createArmor(Team team) {
        Color color = team == Team.BLUE ? Color.BLUE : Color.RED;
        ItemStack[] armor = new ItemStack[4];
        armor[3] = colored(Material.LEATHER_HELMET, color);
        armor[2] = colored(Material.LEATHER_CHESTPLATE, color);
        armor[1] = colored(Material.LEATHER_LEGGINGS, color);
        armor[0] = colored(Material.LEATHER_BOOTS, color);
        return armor;
    }

    private ItemStack colored(Material mat, Color color) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    private Location getSpawn(Team team) {
        return config.getLocation("spawn." + (team == Team.BLUE ? "blue" : "red"));
    }

    public void handleEnter(Player player) {
        if (isPlaying(player)) {
            return;
        }
        Team team = smallerTeam();
        (team == Team.BLUE ? blueTeam : redTeam).add(player.getUniqueId());

        player.getInventory().clear();
        player.getInventory().setArmorContents(createArmor(team));

        Location spawn = getSpawn(team);
        Location ballLoc = getBallSpawn();
        if (spawn != null) {
            if (ballLoc != null && ballLoc.getWorld() != null) {
                spawn = spawn.clone();
                spawn.setDirection(ballLoc.toVector().subtract(spawn.toVector()));
            }
            player.teleport(spawn);
        }

        updateScoreboards();
    }

    public void handleLeave(Player player) {
        UUID id = player.getUniqueId();
        blueTeam.remove(id);
        redTeam.remove(id);
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        updateScoreboards();
    }

    public void updateScoreboards() {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("minifoot", "dummy", org.bukkit.ChatColor.GOLD + "Mini-Foot");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.getScore(org.bukkit.ChatColor.BLUE + "Bleus: " + org.bukkit.ChatColor.WHITE + blueTeam.size()).setScore(2);
        obj.getScore(org.bukkit.ChatColor.RED + "Rouges: " + org.bukkit.ChatColor.WHITE + redTeam.size()).setScore(1);
        for (UUID uuid : blueTeam) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.setScoreboard(board);
            }
        }
        for (UUID uuid : redTeam) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.setScoreboard(board);
            }
        }
    }

    private void spawnBall() {
        Location loc = getBallSpawn();
        if (loc == null || loc.getWorld() == null) {
            return;
        }
        Slime slime = loc.getWorld().spawn(loc, Slime.class);
        slime.setSize(1);
        slime.setInvulnerable(true);
        slime.setRemoveWhenFarAway(false);
        if (slime.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            slime.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0);
        }
        slime.setMetadata("minifoot-ball", new FixedMetadataValue(plugin, true));
        ball = slime;
    }

    private void startBallTask() {
        if (getBallSpawn() == null) {
            return;
        }
        spawnBall();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (ball == null || ball.isDead() || !ball.isValid()) {
                if (respawnCountdown == -1) {
                    respawnCountdown = 3; // seconds before respawn
                } else if (respawnCountdown-- <= 0) {
                    spawnBall();
                    respawnCountdown = -1;
                }
                return;
            }
            if (ball.isOnGround()) {
                Vector vel = ball.getVelocity();
                if (vel.getY() != 0) {
                    vel.setY(0);
                    ball.setVelocity(vel);
                }
            }
        }, 20L, 20L);
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

