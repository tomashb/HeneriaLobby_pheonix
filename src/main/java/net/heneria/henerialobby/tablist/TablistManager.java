package net.heneria.henerialobby.tablist;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class TablistManager {

    private final HeneriaLobby plugin;
    private final String header;
    private final String footer;

    public TablistManager(HeneriaLobby plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.header = config.getString("tablist.header", "");
        this.footer = config.getString("tablist.footer", "");
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.isLobbyWorld(player.getWorld())) {
                update(player);
            } else {
                player.setPlayerListHeaderFooter("", "");
            }
        }
    }

    public void update(Player player) {
        String h = ChatColor.translateAlternateColorCodes('&', plugin.applyPlaceholders(player, header));
        String f = ChatColor.translateAlternateColorCodes('&', plugin.applyPlaceholders(player, footer));
        player.setPlayerListHeaderFooter(h, f);
    }
}
