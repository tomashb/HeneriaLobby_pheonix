package net.heneria.henerialobby.sheep;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Sheep;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages the "Party Sheep" feature. Handles spawning, persistence and the
 * colour changing task.
 */
public class PartySheepManager {
    public static final String METADATA = "heneria_partysheep";

    private final HeneriaLobby plugin;
    private final Map<UUID, PartySheep> sheeps = new HashMap<>();
    private final File file;
    private final FileConfiguration config;

    private final long respawnDelayTicks;
    private final double launchVertical;
    private final double launchHorizontal;
    private final long colorChangeTicks;
    private BukkitTask colorTask;

    public PartySheepManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "sheep.yml");
        if (!file.exists()) {
            plugin.saveResource("sheep.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);

        respawnDelayTicks = plugin.getConfig().getLong("party-sheep.respawn-delay-seconds", 5L) * 20L;
        launchVertical = plugin.getConfig().getDouble("party-sheep.launch-power-vertical", 1.5);
        launchHorizontal = plugin.getConfig().getDouble("party-sheep.launch-power-horizontal", 0.5);
        colorChangeTicks = plugin.getConfig().getLong("party-sheep.color-change-ticks", 10L);

        loadSheeps();
        startColorTask();
        Bukkit.getPluginManager().registerEvents(new PartySheepListener(this), plugin);
    }

    private void startColorTask() {
        colorTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            DyeColor[] colors = DyeColor.values();
            Random rnd = new Random();
            for (PartySheep ps : sheeps.values()) {
                Sheep sheep = ps.getEntity();
                sheep.setColor(colors[rnd.nextInt(colors.length)]);
            }
        }, 0L, colorChangeTicks);
    }

    private Location parseLocation(String str) {
        String[] p = str.split(";");
        if (p.length < 6) {
            return null;
        }
        World w = Bukkit.getWorld(p[0]);
        if (w == null) {
            return null;
        }
        double x = Double.parseDouble(p[1]);
        double y = Double.parseDouble(p[2]);
        double z = Double.parseDouble(p[3]);
        float yaw = Float.parseFloat(p[4]);
        float pitch = Float.parseFloat(p[5]);
        Location loc = new Location(w, x, y, z, yaw, pitch);
        return loc;
    }

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
    }

    private Sheep spawnSheep(Location loc) {
        Sheep sheep = loc.getWorld().spawn(loc, Sheep.class);
        sheep.setMetadata(METADATA, new FixedMetadataValue(plugin, true));
        sheep.setAI(false);
        sheep.setInvulnerable(true);
        sheep.setSilent(true);
        sheep.setPersistent(true);
        sheep.setRemoveWhenFarAway(false);
        return sheep;
    }

    public void loadSheeps() {
        List<String> list = config.getStringList("sheep-locations");
        for (String s : list) {
            Location loc = parseLocation(s);
            if (loc != null) {
                Sheep ent = spawnSheep(loc);
                sheeps.put(ent.getUniqueId(), new PartySheep(ent, loc));
            }
        }
    }

    public void saveSheeps() {
        List<String> list = new ArrayList<>();
        for (PartySheep ps : sheeps.values()) {
            list.add(serializeLocation(ps.getSpawnLocation()));
        }
        config.set("sheep-locations", list);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSheep(Location loc) {
        Sheep ent = spawnSheep(loc);
        sheeps.put(ent.getUniqueId(), new PartySheep(ent, loc));
        saveSheeps();
    }

    public int removeSheep(Location center, double radius) {
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, PartySheep> entry : sheeps.entrySet()) {
            if (entry.getValue().getSpawnLocation().getWorld().equals(center.getWorld()) &&
                    entry.getValue().getSpawnLocation().distance(center) <= radius) {
                entry.getValue().getEntity().remove();
                toRemove.add(entry.getKey());
            }
        }
        for (UUID id : toRemove) {
            sheeps.remove(id);
        }
        if (!toRemove.isEmpty()) {
            saveSheeps();
        }
        return toRemove.size();
    }

    public void listSheeps(org.bukkit.command.CommandSender sender) {
        sender.sendMessage("Sheeps: " + sheeps.size());
        int i = 1;
        for (PartySheep ps : sheeps.values()) {
            Location l = ps.getSpawnLocation();
            sender.sendMessage(i++ + ". " + l.getWorld().getName() + " " + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
        }
    }

    public void handleHit(Sheep sheep) {
        PartySheep ps = sheeps.remove(sheep.getUniqueId());
        if (ps == null) {
            return;
        }
        sheep.setVelocity(new Vector((Math.random() - 0.5) * 2 * launchHorizontal, launchVertical, (Math.random() - 0.5) * 2 * launchHorizontal));
        Location loc = sheep.getLocation();
        loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, loc.add(0, 0.5, 0), 1);
        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        sheep.setInvisible(true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            sheep.teleport(ps.getSpawnLocation());
            sheep.setVelocity(new Vector(0, 0, 0));
            sheep.setInvisible(false);
            sheeps.put(sheep.getUniqueId(), ps);
        }, respawnDelayTicks);
    }

    public void removeAll() {
        if (colorTask != null) {
            colorTask.cancel();
        }
        for (PartySheep ps : sheeps.values()) {
            ps.getEntity().remove();
        }
    }

    public Collection<PartySheep> getSheeps() {
        return sheeps.values();
    }
}

class PartySheep {
    private final Sheep entity;
    private final Location spawnLocation;

    public PartySheep(Sheep entity, Location spawnLocation) {
        this.entity = entity;
        this.spawnLocation = spawnLocation;
    }

    public Sheep getEntity() {
        return entity;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }
}

