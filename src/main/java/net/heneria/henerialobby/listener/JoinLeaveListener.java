package net.heneria.henerialobby.listener;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {

    private final HeneriaLobby plugin;

    public JoinLeaveListener(HeneriaLobby plugin) {
        this.plugin = plugin;
    }

    private String format(Player player, String message) {
        message = message.replace("%player_name%", player.getName());
        message = plugin.applyPlaceholders(player, message);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        var section = plugin.getConfig().getConfigurationSection("player-experience.join-leave-messages");
        if (section == null || !section.getBoolean("enabled", true)) {
            event.setJoinMessage(null);
            return;
        }
        String msg = section.getString("join", "");
        if (msg == null || msg.isEmpty()) {
            event.setJoinMessage(null);
        } else {
            event.setJoinMessage(format(event.getPlayer(), msg));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        var section = plugin.getConfig().getConfigurationSection("player-experience.join-leave-messages");
        if (section == null || !section.getBoolean("enabled", true)) {
            event.setQuitMessage(null);
            return;
        }
        String msg = section.getString("leave", "");
        if (msg == null || msg.isEmpty()) {
            event.setQuitMessage(null);
        } else {
            event.setQuitMessage(format(event.getPlayer(), msg));
        }
    }
}
