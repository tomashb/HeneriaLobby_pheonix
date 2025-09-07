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
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import net.heneria.henerialobby.selector.ServerSelector;
import net.heneria.henerialobby.visibility.VisibilityManager;
import net.heneria.henerialobby.spawn.SpawnManager;

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
    private int blueScore = 0;
    private int redScore = 0;

    // Physics configuration
    private final double pushForce;
    private final double friction;

    public MiniFootManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "minifoot.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.pushForce = config.getDouble("ball.push-force", 1.0);
        this.friction = config.getDouble("ball.friction", 0.96);
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

    private boolean isInGoal(Team team, Location loc) {
        String key = team == Team.BLUE ? "goal.blue" : "goal.red";
        Location pos1 = config.getLocation(key + ".pos1");
        Location pos2 = config.getLocation(key + ".pos2");
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
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        updateScoreboards();
    }

    public void updateScoreboards() {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("minifoot", "dummy", org.bukkit.ChatColor.GOLD + "Mini-Foot");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.getScore(org.bukkit.ChatColor.BLUE + "Bleus: " + org.bukkit.ChatColor.WHITE + blueScore).setScore(3);
        obj.getScore(org.bukkit.ChatColor.RED + "Rouges: " + org.bukkit.ChatColor.WHITE + redScore).setScore(2);
        obj.getScore(org.bukkit.ChatColor.YELLOW + "Joueurs: " + org.bukkit.ChatColor.WHITE + getTotalPlayers()).setScore(1);
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

    private java.util.List<Player> getPlayers() {
        java.util.List<Player> players = new java.util.ArrayList<>();
        for (UUID uuid : blueTeam) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                players.add(p);
            }
        }
        for (UUID uuid : redTeam) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                players.add(p);
            }
        }
        return players;
    }

    private void freezePlayers(boolean freeze) {
        for (Player p : getPlayers()) {
            if (freeze) {
                p.setWalkSpeed(0f);
                p.setFlySpeed(0f);
            } else {
                p.setWalkSpeed(0.2f);
                p.setFlySpeed(0.1f);
            }
        }
    }

    private void giveLobbyItems(Player player) {
        ServerSelector selector = plugin.getServerSelector();
        player.getInventory().setItem(selector.getSelectorSlot(), selector.getSelectorItem());
        VisibilityManager vm = plugin.getVisibilityManager();
        if (vm != null) {
            VisibilityManager.Mode mode = vm.getMode(player);
            player.getInventory().setItem(vm.getSlot(), new ItemStack(vm.getMaterial(mode)));
        }
        player.updateInventory();
    }

    private void startCountdown(Runnable end) {
        freezePlayers(true);
        for (int i = 3; i > 0; i--) {
            final int sec = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player p : getPlayers()) {
                    p.sendTitle(org.bukkit.ChatColor.GOLD + String.valueOf(sec), "", 0, 20, 0);
                }
            }, (3 - i) * 20L);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            freezePlayers(false);
            end.run();
        }, 60L);
    }

    private void handleGoal(Team scoringTeam) {
        if (scoringTeam == Team.BLUE) {
            blueScore++;
        } else {
            redScore++;
        }
        updateScoreboards();
        for (Player p : getPlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            p.sendTitle(org.bukkit.ChatColor.GOLD + "But !", (scoringTeam == Team.BLUE ? org.bukkit.ChatColor.BLUE : org.bukkit.ChatColor.RED) + "Équipe " + (scoringTeam == Team.BLUE ? "Bleue" : "Rouge"), 10, 40, 10);
            Team team = blueTeam.contains(p.getUniqueId()) ? Team.BLUE : Team.RED;
            Location spawn = getSpawn(team);
            if (spawn != null) {
                p.teleport(spawn);
            }
        }
        if (ball != null) {
            Location spawn = getBallSpawn();
            if (spawn != null) {
                ball.teleport(spawn);
                ball.setVelocity(new Vector());
            }
        }
        Team winner = null;
        if (blueScore >= 3) {
            winner = Team.BLUE;
        }
        if (redScore >= 3) {
            winner = Team.RED;
        }
        Team winTeam = winner;
        startCountdown(() -> {
            if (winTeam != null) {
                announceVictory(winTeam);
            }
        });
    }

    private void announceVictory(Team team) {
        for (Player p : getPlayers()) {
            p.sendTitle(org.bukkit.ChatColor.GOLD + "Victoire !", (team == Team.BLUE ? org.bukkit.ChatColor.BLUE : org.bukkit.ChatColor.RED) + "Équipe " + (team == Team.BLUE ? "Bleue" : "Rouge"), 10, 60, 10);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : new java.util.ArrayList<>(getPlayers())) {
                handleLeave(p);
                giveLobbyItems(p);
                plugin.updateDisplays(p);
                SpawnManager sm = plugin.getSpawnManager();
                if (sm != null && sm.hasSpawn()) {
                    sm.teleport(p);
                } else {
                    p.teleport(p.getWorld().getSpawnLocation());
                }
            }
            blueScore = 0;
            redScore = 0;
            updateScoreboards();
            if (ball != null) {
                ball.remove();
                ball = null;
            }
        }, 15 * 20L);
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

    public Slime getBall() {
        return ball;
    }

    public double getPushForce() {
        return pushForce;
    }

    private void startBallTask() {
        if (getBallSpawn() == null) {
            return;
        }
        spawnBall();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (ball == null || ball.isDead() || !ball.isValid()) {
                if (respawnCountdown == -1) {
                    respawnCountdown = 60; // 3 seconds in ticks
                } else if (respawnCountdown-- <= 0) {
                    spawnBall();
                    respawnCountdown = -1;
                }
                return;
            }

            Location loc = ball.getLocation();
            if (isInGoal(Team.BLUE, loc)) {
                handleGoal(Team.RED);
                return;
            }
            if (isInGoal(Team.RED, loc)) {
                handleGoal(Team.BLUE);
                return;
            }

            Vector vel = ball.getVelocity();

            if (ball.isOnGround() && vel.getY() != 0) {
                vel.setY(0);
            }

            // Apply friction to horizontal movement
            vel.setX(vel.getX() * friction);
            vel.setZ(vel.getZ() * friction);

            Location pos1 = config.getLocation("arena.pos1");
            Location pos2 = config.getLocation("arena.pos2");
            if (pos1 != null && pos2 != null) {
                double minX = Math.min(pos1.getX(), pos2.getX());
                double maxX = Math.max(pos1.getX(), pos2.getX());
                double minZ = Math.min(pos1.getZ(), pos2.getZ());
                double maxZ = Math.max(pos1.getZ(), pos2.getZ());
                double x = loc.getX();
                double z = loc.getZ();
                boolean bounced = false;
                if (x <= minX || x >= maxX) {
                    vel.setX(-vel.getX());
                    loc.setX(x <= minX ? minX : maxX);
                    bounced = true;
                }
                if (z <= minZ || z >= maxZ) {
                    vel.setZ(-vel.getZ());
                    loc.setZ(z <= minZ ? minZ : maxZ);
                    bounced = true;
                }
                if (bounced) {
                    ball.teleport(loc);
                }
            }

            // Stop tiny movements
            if (Math.abs(vel.getX()) < 0.01) vel.setX(0);
            if (Math.abs(vel.getZ()) < 0.01) vel.setZ(0);

            ball.setVelocity(vel);
        }, 1L, 1L);
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

