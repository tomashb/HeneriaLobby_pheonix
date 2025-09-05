package net.heneria.henerialobby.announcer;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple auto broadcaster that periodically sends messages to players
 * present in the lobby worlds.
 */
public class Announcer {

    private final HeneriaLobby plugin;
    private final boolean enabled;
    private final long interval;
    private final boolean random;
    private final String prefix;
    private final List<String> messages;
    private int index = 0;

    public Announcer(HeneriaLobby plugin) {
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder(), "announcer.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        this.enabled = cfg.getBoolean("enabled", true);
        this.interval = cfg.getLong("interval-seconds", 180L) * 20L;
        this.random = cfg.getBoolean("random", true);
        this.prefix = cfg.getString("prefix", "");
        this.messages = cfg.getStringList("messages");
        if (enabled && !messages.isEmpty()) {
            Bukkit.getScheduler().runTaskTimer(plugin, this::broadcast, interval, interval);
        }
    }

    private void broadcast() {
        String msg;
        if (random) {
            msg = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        } else {
            msg = messages.get(index);
            index = (index + 1) % messages.size();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!plugin.isLobbyWorld(player.getWorld())) {
                continue;
            }
            String text = prefix + msg;
            text = ChatColor.translateAlternateColorCodes('&', plugin.applyPlaceholders(player, text));
            player.sendMessage(text);
        }
    }
}
