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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import java.time.Duration;
import java.util.Objects;

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
        ConfigurationSection main = section.getConfigurationSection("main-title");
        if (main == null) {
            return;
        }
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            MiniMessage mm = MiniMessage.miniMessage();
            String mainText = plugin.applyPlaceholders(player, main.getString("text", ""));
            Component mainComp = mm.deserialize(mainText);
            Duration mainIn = Duration.ofMillis((long) (main.getDouble("fade-in", 0D) * 1000));
            Duration mainStay = Duration.ofMillis((long) (main.getDouble("stay", 0D) * 1000));
            Duration mainOut = Duration.ofMillis((long) (main.getDouble("fade-out", 0D) * 1000));
            player.sendTitlePart(TitlePart.TIMES, Title.Times.times(mainIn, mainStay, mainOut));
            player.sendTitlePart(TitlePart.TITLE, mainComp);

            java.util.List<java.util.Map<?, ?>> subs = section.getMapList("subtitles");
            long delay = 0L;
            for (java.util.Map<?, ?> map : subs) {
                String text = plugin.applyPlaceholders(player, Objects.toString(map.get("text"), ""));
                Component subComp = mm.deserialize(text);
                double fi = map.get("fade-in") instanceof Number ? ((Number) map.get("fade-in")).doubleValue() : 0D;
                double st = map.get("stay") instanceof Number ? ((Number) map.get("stay")).doubleValue() : 0D;
                double fo = map.get("fade-out") instanceof Number ? ((Number) map.get("fade-out")).doubleValue() : 0D;
                Duration in = Duration.ofMillis((long) (fi * 1000));
                Duration stay = Duration.ofMillis((long) (st * 1000));
                Duration out = Duration.ofMillis((long) (fo * 1000));
                long ticks = (long) ((fi + st + fo) * 20);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.sendTitlePart(TitlePart.TIMES, Title.Times.times(in, stay, out));
                    player.sendTitlePart(TitlePart.SUBTITLE, subComp);
                }, delay);
                delay += ticks;
            }
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
