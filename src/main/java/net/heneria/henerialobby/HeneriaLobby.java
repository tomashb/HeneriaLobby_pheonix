package net.heneria.henerialobby;

import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.PlaceholderAPI;
import net.heneria.henerialobby.command.CustomCommand;
import net.heneria.henerialobby.command.LobbyAdminCommand;
import net.heneria.henerialobby.command.LobbyCommand;
import net.heneria.henerialobby.command.SetLobbyCommand;
import net.heneria.henerialobby.command.ServersCommand;
import net.heneria.henerialobby.command.HologramCommand;
import net.heneria.henerialobby.listener.SpawnListener;
import net.heneria.henerialobby.listener.SelectorListener;
import net.heneria.henerialobby.listener.ProtectionListener;
import net.heneria.henerialobby.listener.DisplayListener;
import net.heneria.henerialobby.listener.VisibilityListener;
import net.heneria.henerialobby.listener.LaunchpadListener;
import net.heneria.henerialobby.listener.JoinLeaveListener;
import net.heneria.henerialobby.listener.JoinEffectsListener;
import net.heneria.henerialobby.listener.InterfaceChatListener;
import net.heneria.henerialobby.scoreboard.ScoreboardManager;
import net.heneria.henerialobby.tablist.TablistManager;
import net.heneria.henerialobby.selector.ServerSelector;
import net.heneria.henerialobby.spawn.SpawnManager;
import net.heneria.henerialobby.visibility.VisibilityManager;
import net.heneria.henerialobby.joineffects.JoinEffectsManager;
import net.heneria.henerialobby.announcer.Announcer;
import net.heneria.henerialobby.hologram.HologramManager;
import net.heneria.henerialobby.npc.NPCManager;
import net.heneria.henerialobby.npc.NPCCommand;
import net.heneria.henerialobby.npc.NPCListener;
import net.heneria.henerialobby.sheep.PartySheepManager;
import com.masecla.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
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
    private JoinEffectsManager joinEffectsManager;
    private Announcer announcer;
    private HologramManager hologramManager;
    private NPCManager npcManager;
    private PartySheepManager partySheepManager;
    private java.util.Set<String> lobbyWorlds;
    private final java.util.Map<String, Command> customCommands = new java.util.HashMap<>();
    private HeadDatabaseAPI hdbApi = null;

    @Override
    public void onEnable() {
        getLogger().info("HeneriaLobby enabled");

// ----- NOUVEAU BLOC À COLLER -----
// On récupère le plugin HeadDatabase directement depuis le gestionnaire de plugins
Plugin hdbPlugin = getServer().getPluginManager().getPlugin("HeadDatabase");

// On vérifie si le plugin est bien là ET s'il est bien du type "HeadDatabaseAPI"
if (hdbPlugin != null && hdbPlugin instanceof HeadDatabaseAPI) {
    // Si c'est bon, on le "cast" (convertit). C'est la liaison directe.
    this.hdbApi = (HeadDatabaseAPI) hdbPlugin;
    getLogger().info("LIAISON DIRECTE avec l'API de HeadDatabase réussie !");
} else {
    // Si ça échoue, on log une erreur claire.
    getLogger().severe("**************************************************");
    getLogger().severe("HeadDatabase n'a pas été trouvé ou n'est pas une instance valide de l'API.");
    getLogger().severe("Les fonctionnalités de têtes personnalisées seront désactivées.");
    getLogger().severe("**************************************************");
}
// ----- FIN DU NOUVEAU BLOC -----

        saveDefaultConfig();
        saveResourceIfNotExists("messages.yml");
        saveResourceIfNotExists("server-selector.yml");
        saveResourceIfNotExists("scoreboard.yml");
        saveResourceIfNotExists("commands.yml");
        saveResourceIfNotExists("joineffects.yml");
        saveResourceIfNotExists("announcer.yml");
        saveResourceIfNotExists("holograms.yml");
        saveResourceIfNotExists("npcs.yml");
        saveResourceIfNotExists("npc-actions.yml");
        saveResourceIfNotExists("sheep.yml");
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        scoreboardConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "scoreboard.yml"));
        spawnManager = new SpawnManager(this);
        serverSelector = new ServerSelector(this);
        lobbyWorlds = new java.util.HashSet<>(getConfig().getStringList("lobby-worlds"));
        joinEffectsManager = new JoinEffectsManager(this);

        // Debug welcome title configuration loading
        ConfigurationSection welcome = getConfig().getConfigurationSection("interface-and-chat.welcome-title");
        if (welcome == null) {
            getLogger().warning("welcome-title section is missing from config.yml");
        } else {
            ConfigurationSection main = welcome.getConfigurationSection("main-title");
            if (main == null) {
                getLogger().warning("main-title section is missing in welcome-title");
            } else {
                getLogger().info("Loaded welcome title: " + main.getString("text", ""));
            }
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI detected; placeholders enabled");
        } else {
            getLogger().warning("PlaceholderAPI not found; placeholders disabled");
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, VELOCITY_CONNECT);

        if (hologramManager != null) {
            hologramManager.removeAll();
        }
        getCommand("lobby").setExecutor(new LobbyCommand(this, spawnManager));
        getCommand("setlobby").setExecutor(new SetLobbyCommand(this, spawnManager));
        getCommand("servers").setExecutor(new ServersCommand(serverSelector));
        getCommand("lobbyadmin").setExecutor(new LobbyAdminCommand(this));
        hologramManager = new HologramManager(this);
        HologramCommand hologramCommand = new HologramCommand(hologramManager);
        getCommand("hologram").setExecutor(hologramCommand);
        getCommand("hologram").setTabCompleter(hologramCommand);
        npcManager = new NPCManager(this);
        NPCCommand npcCommand = new NPCCommand(npcManager);
        getCommand("npc").setExecutor(npcCommand);
        getCommand("npc").setTabCompleter(npcCommand);
        Bukkit.getPluginManager().registerEvents(new NPCListener(npcManager), this);
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
        Bukkit.getPluginManager().registerEvents(new JoinEffectsListener(joinEffectsManager), this);
        Bukkit.getPluginManager().registerEvents(new InterfaceChatListener(this), this);

        if (getConfig().getBoolean("party-sheep.enabled", true)) {
            partySheepManager = new PartySheepManager(this);
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

        announcer = new Announcer(this);

        loadCustomCommands();
    }

    private void saveResourceIfNotExists(String resource) {
        File file = new File(getDataFolder(), resource);
        if (!file.exists()) {
            saveResource(resource, false);
        }
    }

    @Override
      public void onDisable() {
          unregisterCustomCommands();
          this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, VELOCITY_CONNECT);
          if (hologramManager != null) {
            hologramManager.saveAll();
            hologramManager.removeAll();
        }
        if (npcManager != null) {
            npcManager.saveAll();
            npcManager.removeAll();
        }
        if (partySheepManager != null) {
            partySheepManager.removeAll();
            partySheepManager.saveSheeps();
        }
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

    public HeadDatabaseAPI getHdbApi() {
        return hdbApi;
    }
    public void reloadAll() {
        reloadConfig();
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        scoreboardConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "scoreboard.yml"));
        spawnManager = new SpawnManager(this);
        serverSelector = new ServerSelector(this);
        lobbyWorlds = new java.util.HashSet<>(getConfig().getStringList("lobby-worlds"));
        joinEffectsManager = new JoinEffectsManager(this);

        getCommand("lobby").setExecutor(new LobbyCommand(this, spawnManager));
        getCommand("setlobby").setExecutor(new SetLobbyCommand(this, spawnManager));
        getCommand("servers").setExecutor(new ServersCommand(serverSelector));
        npcManager = new NPCManager(this);
        NPCCommand npcCommand = new NPCCommand(npcManager);
        getCommand("npc").setExecutor(npcCommand);
        getCommand("npc").setTabCompleter(npcCommand);

        org.bukkit.Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        if (getConfig().getBoolean("scoreboard.enabled", true)) {
            scoreboardManager = new ScoreboardManager(this, scoreboardConfig);
            long interval = getConfig().getLong("scoreboard.update-interval", 40L);
            getServer().getScheduler().runTaskTimer(this, () -> scoreboardManager.updateAll(), 0L, interval);
        } else {
            scoreboardManager = null;
        }

        if (getConfig().getBoolean("tablist.enabled", true)) {
            tablistManager = new TablistManager(this, scoreboardConfig);
            long interval = getConfig().getLong("tablist.update-interval", 40L);
            getServer().getScheduler().runTaskTimer(this, () -> tablistManager.updateAll(), 0L, interval);
        } else {
            tablistManager = null;
        }

        announcer = new Announcer(this);

        visibilityManager = null;
        if (getConfig().getBoolean("player-experience.player-visibility.enabled", true)) {
            visibilityManager = new VisibilityManager(this);
            Bukkit.getPluginManager().registerEvents(new VisibilityListener(this, visibilityManager), this);
        }

        Bukkit.getPluginManager().registerEvents(new SpawnListener(this, spawnManager), this);
        Bukkit.getPluginManager().registerEvents(new SelectorListener(this, serverSelector), this);
        if (getConfig().getBoolean("protection.enabled", true)) {
            Bukkit.getPluginManager().registerEvents(new ProtectionListener(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new DisplayListener(this), this);
        if (getConfig().getBoolean("player-experience.launchpads.enabled", true)) {
            Bukkit.getPluginManager().registerEvents(new LaunchpadListener(this), this);
        }
        if (getConfig().getBoolean("player-experience.join-leave-messages.enabled", true)) {
            Bukkit.getPluginManager().registerEvents(new JoinLeaveListener(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new JoinEffectsListener(joinEffectsManager), this);
        Bukkit.getPluginManager().registerEvents(new InterfaceChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NPCListener(npcManager), this);

        hologramManager = new HologramManager(this);
        HologramCommand hologramCommand = new HologramCommand(hologramManager);
        getCommand("hologram").setExecutor(hologramCommand);
        getCommand("hologram").setTabCompleter(hologramCommand);

        loadCustomCommands();
    }

    private CommandMap getCommandMap() {
        return Bukkit.getCommandMap();
    }

    private void unregisterCustomCommands() {
        CommandMap map = getCommandMap();
        if (map == null) {
            return;
        }
        for (Command cmd : customCommands.values()) {
            cmd.unregister(map);
        }
        customCommands.clear();
    }

    private void loadCustomCommands() {
        unregisterCustomCommands();
        File file = new File(getDataFolder(), "commands.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        CommandMap map = getCommandMap();
        if (map == null) {
            return;
        }
        for (String key : cfg.getKeys(false)) {
            ConfigurationSection sec = cfg.getConfigurationSection(key);
            if (sec == null) {
                continue;
            }
            java.util.List<String> aliases = sec.getStringList("aliases");
            Object msgObj = sec.get("message");
            java.util.List<String> messages;
            if (msgObj instanceof java.util.List) {
                messages = sec.getStringList("message");
            } else {
                messages = java.util.List.of(sec.getString("message", ""));
            }
            CustomCommand cmd = new CustomCommand(key, aliases, messages, this);
            map.register(getDescription().getName().toLowerCase(), cmd);
            customCommands.put(key, cmd);
        }
    }

    public String getMessage(String key) {
        String message = messages.getString(key, key);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }

    public ServerSelector getServerSelector() {
        return serverSelector;
    }

    public VisibilityManager getVisibilityManager() {
        return visibilityManager;
    }

    public PartySheepManager getPartySheepManager() {
        return partySheepManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

}

