package net.heneria.henerialobby.minifoot; // Assurez-vous que le package est correct

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MiniFootManager {

    private final JavaPlugin plugin;
    private Slime ball;
    private final Set<UUID> blueTeam = new HashSet<>();
    private final Set<UUID> redTeam = new HashSet<>();
    private final Map<String, Integer> scores = new HashMap<>();
    private final Map<UUID, Long> pushCooldown = new HashMap<>();
    private boolean frozen = false;

    // Variables de configuration
    private boolean enabled;
    private int scoreToWin;
    private int maxPlayers;
    private double pushMultiplier;
    private Location ballSpawn;
    private Location blueSpawn;
    private Location redSpawn;
    private Cuboid arena;
    private Cuboid goalBlue;
    private Cuboid goalRed;

    public MiniFootManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAndStart() {
        File file = new File(plugin.getDataFolder(), "minifoot.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        enabled = config.getBoolean("enabled", false);
        if (!enabled) return;

        scoreToWin = config.getInt("score-to-win", 3);
        maxPlayers = config.getInt("max-players", 8);
        pushMultiplier = config.getDouble("ball-push-multiplier", 1.0);

        String worldName = config.getString("arena.world");
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            arena = new Cuboid(getLocation(world, config.getConfigurationSection("arena.pos1")),
                    getLocation(world, config.getConfigurationSection("arena.pos2")));
            ballSpawn = getLocation(world, config.getConfigurationSection("ball-spawn"));
            blueSpawn = getLocation(world, config.getConfigurationSection("teams.blue.spawn"));
            redSpawn = getLocation(world, config.getConfigurationSection("teams.red.spawn"));
            goalBlue = new Cuboid(getLocation(world, config.getConfigurationSection("teams.blue.goal.pos1")),
                    getLocation(world, config.getConfigurationSection("teams.blue.goal.pos2")));
            goalRed = new Cuboid(getLocation(world, config.getConfigurationSection("teams.red.goal.pos1")),
                    getLocation(world, config.getConfigurationSection("teams.red.goal.pos2")));
        }

        plugin.getServer().getPluginManager().registerEvents(new MiniFootListener(this), plugin);
        spawnBall();
        startGameLoop();
        resetScores();
    }

    private Location getLocation(World world, ConfigurationSection sec) {
        if (world == null || sec == null) return null;
        double x = sec.getDouble("x");
        double y = sec.getDouble("y");
        double z = sec.getDouble("z");
        return new Location(world, x, y, z);
    }

    private void resetScores() {
        scores.put("blue", 0);
        scores.put("red", 0);
        updateScoreboards();
    }

    public Cuboid getArenaZone() {
        return arena;
    }

    public void addPlayerToTeam(Player player) {
        if (isInGame(player)) return;
        if (blueTeam.size() + redTeam.size() >= maxPlayers) return;
        // determine smallest team
        Set<UUID> team = blueTeam.size() <= redTeam.size() ? blueTeam : redTeam;
        team.add(player.getUniqueId());
        boolean blue = team == blueTeam;

        // clear inventory and give armor
        player.getInventory().clear();
        giveTeamArmor(player, blue);

        // teleport to spawn looking at ball
        Location spawn = blue ? blueSpawn : redSpawn;
        if (spawn != null) {
            Location dest = spawn.clone();
            if (ball != null) {
                dest.setDirection(ball.getLocation().toVector().subtract(dest.toVector()));
            }
            player.teleport(dest);
        }

        // show scoreboard next tick
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            updateScoreboard(player);
        });

        updateScoreboards();
    }

    public void removePlayerFromGame(Player player) {
        UUID id = player.getUniqueId();
        if (!isInGame(id)) return;
        blueTeam.remove(id);
        redTeam.remove(id);
        pushCooldown.remove(id);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        if (plugin instanceof net.heneria.henerialobby.HeneriaLobby hl) {
            var selector = hl.getServerSelector();
            if (selector != null) {
                player.getInventory().setItem(selector.getSelectorSlot(), selector.getSelectorItem());
            }
            var vis = hl.getVisibilityManager();
            if (vis != null) {
                var mode = vis.getMode(player);
                player.getInventory().setItem(vis.getSlot(), new ItemStack(vis.getMaterial(mode)));
            }
            hl.updateDisplays(player);
        }

        // teleport to lobby spawn via command
        player.performCommand("lobby");

        updateScoreboards();
    }

    public void spawnBall() {
        // Logique pour nettoyer les anciens slimes et en créer un nouveau
        if (ball != null && !ball.isDead()) {
            ball.remove();
        }
        if (ballSpawn == null) return;
        World world = ballSpawn.getWorld();
        if (world == null) return;
        ball = (Slime) world.spawnEntity(ballSpawn, EntityType.SLIME);
        ball.setSize(1);
        AttributeInstance speed = ball.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.0);
        ball.setGravity(true);
        ball.setInvulnerable(true);
    }

    private void startGameLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ball == null || !ball.isValid()) {
                    spawnBall();
                    return;
                }

                // Logique Anti-Saut (plus agressive)
                if (ball.isOnGround()) {
                    Vector velocity = ball.getVelocity();
                    if (velocity.getY() > 0) {
                        velocity.setY(0);
                        ball.setVelocity(velocity);
                    }
                }

                // Logique de détection de but
                if (goalRed != null && goalRed.contains(ball.getLocation())) {
                    handleGoal("blue"); // L'équipe BLEUE marque dans le but ROUGE
                } else if (goalBlue != null && goalBlue.contains(ball.getLocation())) {
                    handleGoal("red"); // L'équipe ROUGE marque dans le but BLEU
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // S'exécute toutes les ticks
    }

    public void pushBall(Player player) {
        if (frozen) return;
        if (ball == null || !ball.isValid()) return;
        if (player.getLocation().distanceSquared(ball.getLocation()) > 2.25) return;
        long now = System.currentTimeMillis();
        long last = pushCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 200) return;
        pushCooldown.put(player.getUniqueId(), now);
        Vector dir = ball.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(pushMultiplier);
        ball.setVelocity(dir);
    }

    private java.util.List<Player> getPlayers() {
        java.util.List<Player> list = new java.util.ArrayList<>();
        for (UUID id : blueTeam) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) list.add(p);
        }
        for (UUID id : redTeam) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) list.add(p);
        }
        return list;
    }

    private void updateScoreboard(Player player) {
        var manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("minifoot", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(ChatColor.GOLD + "MiniFoot");
        obj.getScore(ChatColor.BLUE + "Bleus: " + scores.get("blue")).setScore(2);
        obj.getScore(ChatColor.RED + "Rouges: " + scores.get("red")).setScore(1);
        obj.getScore(ChatColor.YELLOW + "Max: " + scoreToWin).setScore(0);
        player.setScoreboard(board);
    }

    private void updateScoreboards() {
        getPlayers().forEach(this::updateScoreboard);
    }

    private void teleportTeamsToSpawn() {
        for (UUID uuid : blueTeam) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && blueSpawn != null) p.teleport(blueSpawn);
        }
        for (UUID uuid : redTeam) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && redSpawn != null) p.teleport(redSpawn);
        }
    }

    private void freezePlayers(int seconds, Runnable after) {
        frozen = true;
        for (Player p : getPlayers()) {
            p.setWalkSpeed(0f);
            p.setFlySpeed(0f);
        }
        new BukkitRunnable() {
            int count = seconds;

            @Override
            public void run() {
                for (Player p : getPlayers()) {
                    p.sendTitle(ChatColor.YELLOW + String.valueOf(count), "", 0, 20, 0);
                }
                if (count-- <= 0) {
                    for (Player p : getPlayers()) {
                        p.setWalkSpeed(0.2f);
                        p.setFlySpeed(0.1f);
                        p.sendTitle("", "", 0, 0, 0);
                    }
                    frozen = false;
                    cancel();
                    if (after != null) after.run();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void endGame(String winningTeam) {
        String title = winningTeam.equals("blue") ? ChatColor.BLUE + "Victoire des Bleus" : ChatColor.RED + "Victoire des Rouges";
        getPlayers().forEach(p -> p.sendTitle(title, "", 10, 60, 10));
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : new java.util.ArrayList<>(getPlayers())) {
                    removePlayerFromGame(p);
                }
                resetScores();
                spawnBall();
            }
        }.runTaskLater(plugin, 15 * 20L);
    }

    private void handleGoal(String scoringTeam) {
        int newScore = scores.get(scoringTeam) + 1;
        scores.put(scoringTeam, newScore);

        // play sound and title
        String title = scoringTeam.equals("blue") ? ChatColor.BLUE + "But des Bleus" : ChatColor.RED + "But des Rouges";
        getPlayers().forEach(p -> {
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            p.sendTitle(title, "", 10, 40, 10);
        });

        // reset ball
        if (ballSpawn != null && ball != null) {
            ball.teleport(ballSpawn);
            ball.setVelocity(new Vector(0, 0, 0));
        }

        teleportTeamsToSpawn();
        updateScoreboards();

        freezePlayers(3, () -> {
            if (newScore >= scoreToWin) {
                endGame(scoringTeam);
            }
        });
    }

    private void giveTeamArmor(Player p, boolean blue) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        java.awt.Color awtColor = blue ? java.awt.Color.BLUE : java.awt.Color.RED;
        org.bukkit.Color color = org.bukkit.Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
        LeatherArmorMeta meta;
        meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setColor(color);
        helmet.setItemMeta(meta);
        meta = (LeatherArmorMeta) chest.getItemMeta();
        meta.setColor(color);
        chest.setItemMeta(meta);
        meta = (LeatherArmorMeta) legs.getItemMeta();
        meta.setColor(color);
        legs.setItemMeta(meta);
        meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(color);
        boots.setItemMeta(meta);
        p.getInventory().setArmorContents(new ItemStack[]{boots, legs, chest, helmet});
    }

    public boolean isInGame(Player p) {
        return isInGame(p.getUniqueId());
    }

    public boolean isInGame(UUID uuid) {
        return blueTeam.contains(uuid) || redTeam.contains(uuid);
    }

    public void saveArena(Location pos1, Location pos2) {
        File file = new File(plugin.getDataFolder(), "minifoot.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("arena.world", pos1.getWorld().getName());
        config.set("arena.pos1.x", pos1.getX());
        config.set("arena.pos1.y", pos1.getY());
        config.set("arena.pos1.z", pos1.getZ());
        config.set("arena.pos2.x", pos2.getX());
        config.set("arena.pos2.y", pos2.getY());
        config.set("arena.pos2.z", pos2.getZ());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadAndStart();
    }

    public void saveGoal(String team, Location pos1, Location pos2) {
        File file = new File(plugin.getDataFolder(), "minifoot.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String base = "teams." + team + ".goal";
        config.set(base + ".pos1.x", pos1.getX());
        config.set(base + ".pos1.y", pos1.getY());
        config.set(base + ".pos1.z", pos1.getZ());
        config.set(base + ".pos2.x", pos2.getX());
        config.set(base + ".pos2.y", pos2.getY());
        config.set(base + ".pos2.z", pos2.getZ());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadAndStart();
    }

    public void saveSpawn(String team, Location loc) {
        File file = new File(plugin.getDataFolder(), "minifoot.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String base = "teams." + team + ".spawn";
        config.set(base + ".x", loc.getX());
        config.set(base + ".y", loc.getY());
        config.set(base + ".z", loc.getZ());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadAndStart();
    }

    public void saveBallSpawn(Location loc) {
        File file = new File(plugin.getDataFolder(), "minifoot.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("ball-spawn.x", loc.getX());
        config.set("ball-spawn.y", loc.getY());
        config.set("ball-spawn.z", loc.getZ());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadAndStart();
    }
}
