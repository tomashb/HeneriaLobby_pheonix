package net.heneria.henerialobby.scoreboard;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class ScoreboardManager {

    private final HeneriaLobby plugin;
    private final String title;
    private final List<String> lines;

    public ScoreboardManager(HeneriaLobby plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.title = ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.title", ""));
        this.lines = config.getStringList("scoreboard.lines");
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.isLobbyWorld(player.getWorld()) && (plugin.getMiniFootManager() == null || !plugin.getMiniFootManager().isPlaying(player))) {
                update(player);
            } else {
                var manager = Bukkit.getScoreboardManager();
                if (manager != null) {
                    player.setScoreboard(manager.getNewScoreboard());
                }
            }
        }
    }

    public void update(Player player) {
        var manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return;
        }
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("heneria", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.applyPlaceholders(player, title)));

        int score = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String text = ChatColor.translateAlternateColorCodes('&', plugin.applyPlaceholders(player, line));
            if (text.trim().isEmpty()) {
                text = ChatColor.values()[i].toString();
            } else {
                text = text + ChatColor.values()[i];
            }
            objective.getScore(text).setScore(score--);
        }

        player.setScoreboard(board);
    }
}
