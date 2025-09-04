package net.heneria.henerialobby;

import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.PlaceholderAPI;
import net.heneria.henerialobby.command.LobbyCommand;
import net.heneria.henerialobby.command.SetLobbyCommand;
import net.heneria.henerialobby.command.ServersCommand;
import net.heneria.henerialobby.listener.SpawnListener;
import net.heneria.henerialobby.listener.SelectorListener;
import net.heneria.henerialobby.listener.ProtectionListener;
import net.heneria.henerialobby.listener.DisplayListener;
import net.heneria.henerialobby.listener.VisibilityListener;
import net.heneria.henerialobby.listener.LaunchpadListener;
import net.heneria.henerialobby.listener.JoinLeaveListener;
import net.heneria.henerialobby.scoreboard.ScoreboardManager;
import net.heneria.henerialobby.tablist.TablistManager;
import net.heneria.henerialobby.selector.ServerSelector;
import net.heneria.henerialobby.spawn.SpawnManager;
import net.heneria.henerialobby.visibility.VisibilityManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class HeneriaLobby extends JavaPlugin {

    /**
     * Channel used by Velocity to handle player connections. Using the raw
     * string avoids a hard dependency on the Velocity API which caused the
     * plugin to fail loading when the library was absent on the server.
     */
    private static final String VELOCITY_CONNECT = "velocity:connect";

    private SpawnManager spawnManager;
    private FileConfiguration messages;
    private ServerSelector serverSelector;
    private FileConfiguration scoreboardConfig;
    private ScoreboardManager scoreboardManager;
    private TablistManager tablistManager;
    private VisibilityManager visibilityManager;
    private java.util.Set<String> lobbyWorlds;

    @Override
    public void onEnable() {
        getLogger().info("HeneriaLobby enabled");

        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("server-selector.yml", false);
        saveResource("scoreboard.yml", false);
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        scoreboardConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "scoreboard.yml"));
        spawnManager = new SpawnManager(this);
        serverSelector = new ServerSelector(this);
        lobbyWorlds = new java.util.HashSet<>(getConfig().getStringList("lobby-worlds"));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI detected; placeholders enabled");
        } else {
            getLogger().warning("PlaceholderAPI not found; placeholders disabled");
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, VELOCITY_CONNECT);

        getCommand("lobby").setExecutor(new LobbyCommand(this, spawnManager));
        getCommand("setlobby").setExecutor(new SetLobbyCommand(this, spawnManager));
        getCommand("servers").setExecutor(new ServersCommand(serverSelector));
        Bukkit.getPluginManager().registerEvents(new SpawnListener(this, spawnManager), this);
        Bukkit.getPluginManager().registerEvents(new SelectorListener(this, serverSelector), this);
        if (getConfig().getBoolean("protection.enabled", true)) {
            Bukkit.getPluginManager().registerEvents(new ProtectionListener(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new DisplayListener(this), this);

        if (getConfig().getBoolean("player-experience.player-visibility.enabled", true)) {
            visibilityManager = new VisibilityManager(this);
            Bukkit.getPluginManager().registerEvents(new VisibilityListener(this, visibilityManager), this);
        }
        if (getConfig().getBoolean("player-experience.launchpads.enabled", true)) {
            Bukkit.getPluginManager().registerEvents(new LaunchpadListener(this), this);
        }
        if (getConfig().getBoolean("player-experience.join-leave-messages.enabled", true)) {
            Bukkit.getPluginManager().registerEvents(new JoinLeaveListener(this), this);
        }

        if (getConfig().getBoolean("scoreboard.enabled", true)) {
            scoreboardManager = new ScoreboardManager(this, scoreboardConfig);
            long interval = getConfig().getLong("scoreboard.update-interval", 40L);
            getServer().getScheduler().runTaskTimer(this, () -> scoreboardManager.updateAll(), 0L, interval);
        }

        if (getConfig().getBoolean("tablist.enabled", true)) {
            tablistManager = new TablistManager(this, scoreboardConfig);
            long interval = getConfig().getLong("tablist.update-interval", 40L);
            getServer().getScheduler().runTaskTimer(this, () -> tablistManager.updateAll(), 0L, interval);
        }
    }

    @Override
      public void onDisable() {
          this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, VELOCITY_CONNECT);
      }

    public void sendPlayer(Player player, String server) {
        var out = ByteStreams.newDataOutput();
        out.writeUTF(server);
        player.sendPluginMessage(this, VELOCITY_CONNECT, out.toByteArray());
    }

      public String applyPlaceholders(Player player, String text) {
          if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
              return PlaceholderAPI.setPlaceholders(player, text);
          }
          return text;
      }

      public boolean isLobbyWorld(org.bukkit.World world) {
          return world != null && lobbyWorlds.contains(world.getName());
      }

      public void updateDisplays(Player player) {
          if (scoreboardManager != null || tablistManager != null) {
              if (isLobbyWorld(player.getWorld())) {
                  if (scoreboardManager != null) {
                      scoreboardManager.update(player);
                  }
                  if (tablistManager != null) {
                      tablistManager.update(player);
                  }
              } else {
                  if (scoreboardManager != null) {
                      var manager = Bukkit.getScoreboardManager();
                      if (manager != null) {
                          player.setScoreboard(manager.getNewScoreboard());
                      }
                  }
                  if (tablistManager != null) {
                      player.setPlayerListHeaderFooter("", "");
                  }
              }
          }
      }

    public String getMessage(String key) {
        String message = messages.getString(key, key);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }

    public ServerSelector getServerSelector() {
        return serverSelector;
    }
}

