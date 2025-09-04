package net.heneria.henerialobby;

import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HeneriaLobby extends JavaPlugin {

    private static final MinecraftChannelIdentifier VELOCITY_CONNECT = MinecraftChannelIdentifier.from("velocity", "connect");

    @Override
    public void onEnable() {
        getLogger().info("HeneriaLobby enabled");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI detected; placeholders enabled");
        } else {
            getLogger().warning("PlaceholderAPI not found; placeholders disabled");
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, VELOCITY_CONNECT.getId());
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, VELOCITY_CONNECT.getId());
    }

    public void sendPlayer(Player player, String server) {
        var out = ByteStreams.newDataOutput();
        out.writeUTF(server);
        player.sendPluginMessage(this, VELOCITY_CONNECT.getId(), out.toByteArray());
    }

    public String applyPlaceholders(Player player, String text) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }
}
