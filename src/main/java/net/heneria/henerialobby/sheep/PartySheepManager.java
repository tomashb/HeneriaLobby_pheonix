package net.heneria.henerialobby.sheep;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * New manager for the interactive party sheep feature.
 * Handles spawn regions, random spawning and the click interactions.
 */
public class PartySheepManager {
    public static final String METADATA = "heneria_partysheep";

    private final HeneriaLobby plugin;
    private final Map<UUID, PartySheepData> sheeps = new HashMap<>();
    private final Map<String, SpawnRegion> regions = new HashMap<>();
    private final Map<UUID, Selection> selections = new HashMap<>();
    private final Random random = new Random();

    private final File file;
    private final FileConfiguration config;

    private final int maxSheepCount;
    private final long respawnDelayTicks;
    private final long spawnCheckTicks;
    private final Sound hitSound;
    private final Particle hitParticle;
    private final Sound launchSound;
    private final Particle launchParticle;
    private final double launchPower;

    private BukkitTask spawnTask;

    public PartySheepManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "sheep.yml");
        if (!file.exists()) {
            plugin.saveResource("sheep.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);

        var cfg = plugin.getConfig().getConfigurationSection("party-sheep");
        maxSheepCount = cfg.getInt("max-sheep-count", 10);
        respawnDelayTicks = cfg.getLong("respawn-delay-seconds", 10L) * 20L;
        spawnCheckTicks = cfg.getLong("spawn-check-interval-seconds", 30L) * 20L;

        ConfigurationSection hitSec = cfg.getConfigurationSection("hit-effects");
        hitSound = Sound.valueOf(hitSec.getString("sound", "ENTITY_CHICKEN_EGG"));
        hitParticle = Particle.valueOf(hitSec.getString("particle", "NOTE"));
        ConfigurationSection launchSec = cfg.getConfigurationSection("launch-effects");
        launchSound = Sound.valueOf(launchSec.getString("sound", "ENTITY_GENERIC_EXPLODE"));
        launchParticle = Particle.valueOf(launchSec.getString("particle", "EXPLOSION"));
        launchPower = launchSec.getDouble("launch-power", 1.5);

        loadRegions();
        Bukkit.getPluginManager().registerEvents(new SelectionListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new PartySheepListener(this), plugin);
        startSpawnTask();
    }

    private void startSpawnTask() {
        spawnTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkSpawn, 0L, spawnCheckTicks);
    }

    private void checkSpawn() {
        while (sheeps.size() < maxSheepCount) {
            spawnRandomSheep();
        }
    }

    private void spawnRandomSheep() {
        if (regions.isEmpty()) return;
        SpawnRegion region = new ArrayList<>(regions.values()).get(random.nextInt(regions.size()));
        var loc = region.getRandomLocation(random);
        Sheep sheep = loc.getWorld().spawn(loc, Sheep.class);
        sheep.setMetadata(METADATA, new FixedMetadataValue(plugin, true));
        sheep.setAI(true);
        sheep.setAware(false);
        sheep.setInvulnerable(true);
        sheep.setSilent(true);
        sheep.setPersistent(true);
        sheep.setRemoveWhenFarAway(false);
        sheeps.put(sheep.getUniqueId(), new PartySheepData(sheep));
    }

    public void handleHit(Sheep sheep) {
        PartySheepData data = sheeps.get(sheep.getUniqueId());
        if (data == null) return;

        int hits = ++data.hits;
        var loc = sheep.getLocation().add(0, 0.5, 0);
        if (hits < 3) {
            loc.getWorld().playSound(loc, hitSound, 1f, 1f);
            loc.getWorld().spawnParticle(hitParticle, loc, 3);
        } else {
            loc.getWorld().playSound(loc, launchSound, 1f, 1f);
            loc.getWorld().spawnParticle(launchParticle, loc, 10);
            sheep.setVelocity(new Vector(0, launchPower, 0));
            sheep.remove();
            sheeps.remove(sheep.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, this::checkSpawn, respawnDelayTicks);
        }
    }

    public void addSpawnRegion(String name, Location pos1, Location pos2) {
        SpawnRegion region = new SpawnRegion(name, pos1, pos2);
        regions.put(name.toLowerCase(), region);
        saveRegions();
    }

    private void loadRegions() {
        regions.clear();
        ConfigurationSection sec = config.getConfigurationSection("spawn-regions");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            SpawnRegion region = SpawnRegion.load(key, sec.getConfigurationSection(key));
            if (region != null) {
                regions.put(key.toLowerCase(), region);
            }
        }
    }

    public void saveRegions() {
        ConfigurationSection root = config.createSection("spawn-regions");
        for (SpawnRegion region : regions.values()) {
            ConfigurationSection sec = root.createSection(region.getName());
            region.save(sec);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save sheep.yml");
        }
    }

    public Selection getOrCreateSelection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), k -> new Selection());
    }

    public Selection getSelection(Player player) {
        return selections.get(player.getUniqueId());
    }

    public void listRegions(org.bukkit.command.CommandSender sender) {
        sender.sendMessage("Regions: " + regions.size());
        for (SpawnRegion region : regions.values()) {
            sender.sendMessage("- " + region.getName());
        }
    }

    public void removeAll() {
        if (spawnTask != null) {
            spawnTask.cancel();
        }
        for (PartySheepData data : sheeps.values()) {
            data.getSheep().remove();
        }
        sheeps.clear();
    }
}

class PartySheepData {
    final Sheep sheep;
    int hits = 0;

    PartySheepData(Sheep sheep) {
        this.sheep = sheep;
    }

    public Sheep getSheep() {
        return sheep;
    }
}

