package net.heneria.henerialobby.joineffects;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and plays join effects based on permissions.
 */
public class JoinEffectsManager {

    private final HeneriaLobby plugin;
    private final File configFile;
    private FileConfiguration config;
    private final List<JoinEffect> effects = new ArrayList<>();

    public JoinEffectsManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "joineffects.yml");
        reload();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        effects.clear();
        for (String key : config.getKeys(false)) {
            ConfigurationSection sec = config.getConfigurationSection(key);
            if (sec == null) {
                continue;
            }
            JoinEffect effect = new JoinEffect();
            effect.permission = sec.getString("permission", "");
            effect.priority = sec.getInt("priority", 0);

            for (String s : sec.getStringList("sounds")) {
                String[] parts = s.split(",");
                try {
                    Sound sound = Sound.valueOf(parts[0]);
                    float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1f;
                    float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1f;
                    effect.sounds.add(new SoundEffect(sound, volume, pitch));
                } catch (Exception ignored) {
                }
            }

            for (String p : sec.getStringList("particles")) {
                String[] parts = p.split(",");
                try {
                    Particle particle = Particle.valueOf(parts[0]);
                    int count = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                    double radius = parts.length > 2 ? Double.parseDouble(parts[2]) : 0;
                    effect.particles.add(new ParticleEffect(particle, count, radius));
                } catch (Exception ignored) {
                }
            }

            ConfigurationSection fwSec = sec.getConfigurationSection("firework");
            if (fwSec != null && fwSec.getBoolean("enabled", false)) {
                FireworkSettings fw = new FireworkSettings();
                fw.enabled = true;
                fw.power = fwSec.getInt("power", 1);
                for (String colorName : fwSec.getStringList("colors")) {
                    try {
                        fw.colors.add(DyeColor.valueOf(colorName.toUpperCase()).getColor());
                    } catch (Exception ignored) {
                    }
                }
                effect.firework = fw;
            }

            effects.add(effect);
        }
    }

    public void play(Player player) {
        JoinEffect selected = null;
        for (JoinEffect effect : effects) {
            if (effect.permission.isEmpty() || !player.hasPermission(effect.permission)) {
                continue;
            }
            if (selected == null || effect.priority > selected.priority) {
                selected = effect;
            }
        }
        if (selected == null) {
            return;
        }
        Location loc = player.getLocation();
        for (SoundEffect s : selected.sounds) {
            player.playSound(loc, s.sound, s.volume, s.pitch);
        }
        for (ParticleEffect p : selected.particles) {
            loc.getWorld().spawnParticle(p.particle, loc, p.count, p.radius, p.radius, p.radius);
        }
        if (selected.firework != null && selected.firework.enabled) {
            Firework fw = loc.getWorld().spawn(loc, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.setPower(selected.firework.power);
            FireworkEffect.Builder builder = FireworkEffect.builder();
            builder.withColor(selected.firework.colors);
            meta.addEffect(builder.build());
            fw.setFireworkMeta(meta);
        }
    }

    private static class JoinEffect {
        String permission;
        int priority;
        List<SoundEffect> sounds = new ArrayList<>();
        List<ParticleEffect> particles = new ArrayList<>();
        FireworkSettings firework;
    }

    private record SoundEffect(Sound sound, float volume, float pitch) {
    }

    private record ParticleEffect(Particle particle, int count, double radius) {
    }

    private static class FireworkSettings {
        boolean enabled;
        int power;
        List<Color> colors = new ArrayList<>();
    }
}
