package net.heneria.henerialobby.minifoot; // Assurez-vous que le package est correct

import org.bukkit.Bukkit;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MiniFootManager implements Listener {

    private final JavaPlugin plugin;
    private Slime ball;
    private final Set<UUID> blueTeam = new HashSet<>();
    private final Set<UUID> redTeam = new HashSet<>();
    private final Map<String, Integer> scores = new HashMap<>();
    private final Map<UUID, Long> pushCooldown = new HashMap<>();

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

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
        // Mettre à jour le scoreboard pour tous les joueurs en jeu
    }

    public Cuboid getArenaZone() {
        return arena;
    }

    public void addPlayerToTeam(Player player) {
        addPlayer(player);
    }

    public void removePlayerFromGame(Player player) {
        removePlayer(player);
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

    private void handleGoal(String scoringTeam) {
        int newScore = scores.get(scoringTeam) + 1;
        scores.put(scoringTeam, newScore);

        // Annoncer le but, etc.
        for (UUID uuid : blueTeam) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }
        for (UUID uuid : redTeam) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }

        ball.teleport(ballSpawn);
        ball.setVelocity(new Vector(0, 0, 0));

        // Téléporter tous les joueurs à leur spawn
        blueTeam.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && blueSpawn != null) p.teleport(blueSpawn);
        });
        redTeam.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && redSpawn != null) p.teleport(redSpawn);
        });

        if (newScore >= scoreToWin) {
            // Gérer la fin de partie
            // Annoncer le gagnant, attendre 15s, kicker tout le monde, et resetScores()
            resetScores();
        } else {
            // Lancer le compte à rebours de 3s avant de pouvoir bouger
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Optimisation : on ne fait rien si le joueur n'a pas changé de bloc
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        
        Cuboid arenaZone = getArenaZone();
        if (arenaZone == null) return; // Pas d'arène configurée

        boolean isNowInside = arenaZone.contains(to);
        boolean wasPreviouslyInside = arenaZone.contains(from);

        // --- LOGIQUE D'ENTRÉE ---
        // Si le joueur n'était PAS dedans avant ET est DEDANS maintenant
        if (isNowInside && !wasPreviouslyInside) {
            // Appeler la méthode qui fait rejoindre une équipe au joueur.
            addPlayerToTeam(player);
        }
        // --- LOGIQUE DE SORTIE ---
        // Si le joueur ÉTAIT dedans avant ET N'EST PLUS DEDANS maintenant
        else if (!isNowInside && wasPreviouslyInside) {
            // Appeler la méthode qui fait quitter la partie au joueur.
            removePlayerFromGame(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Retirer le joueur de la partie s'il se déconnecte
        removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onArmorRemove(InventoryClickEvent event) {
        // Bloquer le retrait de l'armure si le joueur est en jeu
        if (event.getWhoClicked() instanceof Player && isInGame(event.getWhoClicked().getUniqueId())) {
            if (event.getSlot() >= 36 && event.getSlot() <= 39) {
                event.setCancelled(true);
            }
        }
    }

    private void addPlayer(Player p) {
        if (isInGame(p)) return;
        if (blueTeam.size() + redTeam.size() >= maxPlayers) return;
        Set<UUID> team = blueTeam.size() <= redTeam.size() ? blueTeam : redTeam;
        team.add(p.getUniqueId());
        boolean blue = team == blueTeam;
        giveTeamArmor(p, blue);
        if (blue && blueSpawn != null) {
            p.teleport(blueSpawn);
        } else if (!blue && redSpawn != null) {
            p.teleport(redSpawn);
        }
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

    private void removePlayer(Player p) {
        UUID id = p.getUniqueId();
        blueTeam.remove(id);
        redTeam.remove(id);
        p.getInventory().setArmorContents(null);
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
