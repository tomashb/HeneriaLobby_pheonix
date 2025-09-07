package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.metadata.FixedMetadataValue;
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

