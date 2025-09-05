package net.heneria.henerialobby.listener;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class InterfaceChatListener implements Listener {

    private final HeneriaLobby plugin;

    public InterfaceChatListener(HeneriaLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("interface-and-chat.welcome-title");
        if (section == null || !section.getBoolean("enabled", true)) {
            return;
        }
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            String title = section.getString("title", "");
            String subtitle = section.getString("subtitle", "");
            int fadeIn = section.getInt("fade-in", 0) * 20;
            int stay = section.getInt("stay", 0) * 20;
            int fadeOut = section.getInt("fade-out", 0) * 20;
            title = ChatColor.translateAlternateColorCodes('&', plugin.applyPlaceholders(player, title));
            subtitle = ChatColor.translateAlternateColorCodes('&', plugin.applyPlaceholders(player, subtitle));
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("interface-and-chat.chat-format");
        if (section == null || !section.getBoolean("enabled", true)) {
            return;
        }
        Player player = event.getPlayer();
        String format = section.getString("format", "%vault_prefix% &7%player_name% &8» &f%message%");
        format = plugin.applyPlaceholders(player, format);
        format = ChatColor.translateAlternateColorCodes('&', format);
        format = format.replace("%message%", "%2$s");
        event.setFormat(format);
        if (player.hasPermission("heneria.lobby.chatcolor")) {
            event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
        }
    }
}
