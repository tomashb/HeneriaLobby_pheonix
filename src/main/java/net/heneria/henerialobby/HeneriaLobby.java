package net.heneria.henerialobby;

import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.clip.placeholderapi.PlaceholderAPI;
import net.heneria.henerialobby.command.LobbyCommand;
import net.heneria.henerialobby.command.SetLobbyCommand;
import net.heneria.henerialobby.command.ServersCommand;
import net.heneria.henerialobby.listener.SpawnListener;
import net.heneria.henerialobby.listener.SelectorListener;
import net.heneria.henerialobby.selector.ServerSelector;
import net.heneria.henerialobby.spawn.SpawnManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class HeneriaLobby extends JavaPlugin {

    private static final MinecraftChannelIdentifier VELOCITY_CONNECT = MinecraftChannelIdentifier.from("velocity:connect");

    private SpawnManager spawnManager;
    private FileConfiguration messages;
    private ServerSelector serverSelector;

    @Override
    public void onEnable() {
        getLogger().info("HeneriaLobby enabled");

        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("server-selector.yml", false);
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        spawnManager = new SpawnManager(this);
        serverSelector = new ServerSelector(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI detected; placeholders enabled");
        } else {
            getLogger().warning("PlaceholderAPI not found; placeholders disabled");
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, VELOCITY_CONNECT.getId());

        getCommand("lobby").setExecutor(new LobbyCommand(this, spawnManager));
        getCommand("setlobby").setExecutor(new SetLobbyCommand(this, spawnManager));
        getCommand("servers").setExecutor(new ServersCommand(serverSelector));
        Bukkit.getPluginManager().registerEvents(new SpawnListener(this, spawnManager), this);
        Bukkit.getPluginManager().registerEvents(new SelectorListener(this, serverSelector), this);
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

    public String getMessage(String key) {
        String message = messages.getString(key, key);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }
}

