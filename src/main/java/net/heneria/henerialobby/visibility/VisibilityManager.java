package net.heneria.henerialobby.visibility;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

public class VisibilityManager {

    public enum Mode {
        ALL,
        VIPS,
        NONE
    }

    private final HeneriaLobby plugin;
    private final Map<UUID, Mode> modes = new HashMap<>();
    private final File file;
    private final FileConfiguration data;

    private final int slot;
    private final Material itemAll;
    private final Material itemVips;
    private final Material itemNone;
    private final long cooldown;
    private final String itemName;
    private final List<String> itemLore;

    public VisibilityManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        var section = plugin.getConfig().getConfigurationSection("player-experience.player-visibility");
        this.slot = section != null ? section.getInt("item-slot", 8) : 8;
        this.itemAll = Material.matchMaterial(section != null ? section.getString("item-all", "LIME_DYE") : "LIME_DYE");
        this.itemVips = Material.matchMaterial(section != null ? section.getString("item-vips", "YELLOW_DYE") : "YELLOW_DYE");
        this.itemNone = Material.matchMaterial(section != null ? section.getString("item-none", "GRAY_DYE") : "GRAY_DYE");
        this.cooldown = (section != null ? section.getLong("cooldown-seconds", 3L) : 3L) * 1000L;
        this.itemName = section != null ? section.getString("item-name", "&aVisibilité des Joueurs &7(Clic Droit)") : "&aVisibilité des Joueurs &7(Clic Droit)";
        this.itemLore = section != null ? section.getStringList("item-lore") : List.of("&7Cliquez pour changer les joueurs", "&7que vous voyez dans le lobby.");

        file = new File(plugin.getDataFolder(), "visibility.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create visibility.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                Mode mode = Mode.valueOf(data.getString(key, "ALL"));
                modes.put(uuid, mode);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public int getSlot() {
        return slot;
    }

    public Material getMaterial(Mode mode) {
        return switch (mode) {
            case ALL -> itemAll;
            case VIPS -> itemVips;
            case NONE -> itemNone;
        };
    }

    public Mode getMode(Player player) {
        return modes.getOrDefault(player.getUniqueId(), Mode.ALL);
    }

    public long getCooldown() {
        return cooldown;
    }

    public String getItemName() {
        return itemName;
    }

    public List<String> getItemLore() {
        return itemLore;
    }

    public void setMode(Player player, Mode mode) {
        modes.put(player.getUniqueId(), mode);
        data.set(player.getUniqueId().toString(), mode.name());
        save();
    }

    private void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save visibility.yml: " + e.getMessage());
        }
    }

    public void apply(Player viewer) {
        Mode mode = getMode(viewer);
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) continue;
            switch (mode) {
                case ALL -> viewer.showPlayer(plugin, target);
                case VIPS -> {
                    if (target.hasPermission("heneria.lobby.canbeseen")) {
                        viewer.showPlayer(plugin, target);
                    } else {
                        viewer.hidePlayer(plugin, target);
                    }
                }
                case NONE -> viewer.hidePlayer(plugin, target);
            }
        }
    }
}
