package net.heneria.henerialobby.npc;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles loading, saving and basic manipulation of NPCs.
 */
public class NPCManager {

    private final HeneriaLobby plugin;
    private final Map<String, NPC> npcs = new HashMap<>();
    private final Map<UUID, NPC> byId = new HashMap<>();
    private final Map<UUID, NPC> selections = new HashMap<>();
    private final Map<String, String> actions = new HashMap<>();

    private final File file;
    private final FileConfiguration config;
    private final File actionsFile;
    private final FileConfiguration actionsConfig;

    public NPCManager(HeneriaLobby plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "npcs.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.actionsFile = new File(plugin.getDataFolder(), "npc-actions.yml");
        this.actionsConfig = YamlConfiguration.loadConfiguration(actionsFile);
        loadAll();
        loadActions();
    }

    private void loadAll() {
        npcs.clear();
        byId.clear();
        ConfigurationSection root = config.getConfigurationSection("npcs");
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(key);
            if (sec == null) continue;
            ConfigurationSection locSec = sec.getConfigurationSection("location");
            if (locSec == null) continue;
            String world = locSec.getString("world");
            double x = locSec.getDouble("x");
            double y = locSec.getDouble("y");
            double z = locSec.getDouble("z");
            float yaw = (float) locSec.getDouble("yaw");
            var w = Bukkit.getWorld(world);
            if (w == null) continue;
            Location loc = new Location(w, x, y, z, yaw, 0f);
            ArmorStand stand = w.spawn(loc, ArmorStand.class, as -> {
                as.setGravity(false);
                as.setBasePlate(false);
                as.setArms(true);
                as.setVisible(true);
                as.setInvulnerable(true);
                as.setPersistent(true);
                as.setRemoveWhenFarAway(false);
                as.setCanPickupItems(false);
            });
            ConfigurationSection equipSec = sec.getConfigurationSection("equipment");
            if (equipSec != null) {
                var eq = stand.getEquipment();
                if (eq != null) {
                    eq.setHelmet(equipSec.getItemStack("helmet"));
                    eq.setChestplate(equipSec.getItemStack("chestplate"));
                    eq.setLeggings(equipSec.getItemStack("leggings"));
                    eq.setBoots(equipSec.getItemStack("boots"));
                    eq.setItemInMainHand(equipSec.getItemStack("hand"));
                    eq.setItemInOffHand(equipSec.getItemStack("offhand"));
                }
            }
            NPC npc = new NPC(key, stand);
            npcs.put(key.toLowerCase(), npc);
            byId.put(stand.getUniqueId(), npc);
        }
    }

    private void loadActions() {
        actions.clear();
        ConfigurationSection sec = actionsConfig.getConfigurationSection("actions");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                actions.put(key.toLowerCase(), sec.getString(key));
            }
        }
    }

    public void saveAll() {
        ConfigurationSection root = config.createSection("npcs");
        for (NPC npc : npcs.values()) {
            ConfigurationSection sec = root.createSection(npc.getName());
            ArmorStand stand = npc.getStand();
            sec.set("uuid", stand.getUniqueId().toString());
            Location loc = stand.getLocation();
            sec.set("location.world", loc.getWorld().getName());
            sec.set("location.x", loc.getX());
            sec.set("location.y", loc.getY());
            sec.set("location.z", loc.getZ());
            sec.set("location.yaw", loc.getYaw());
            var eq = stand.getEquipment();
            if (eq != null) {
                sec.set("equipment.helmet", eq.getHelmet());
                sec.set("equipment.chestplate", eq.getChestplate());
                sec.set("equipment.leggings", eq.getLeggings());
                sec.set("equipment.boots", eq.getBoots());
                sec.set("equipment.hand", eq.getItemInMainHand());
                sec.set("equipment.offhand", eq.getItemInOffHand());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save npcs.yml");
        }
        ConfigurationSection act = actionsConfig.createSection("actions");
        for (Map.Entry<String, String> en : actions.entrySet()) {
            act.set(en.getKey(), en.getValue());
        }
        try {
            actionsConfig.save(actionsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save npc-actions.yml");
        }
    }

    public boolean create(String name, Location loc) {
        if (npcs.containsKey(name.toLowerCase())) return false;
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setGravity(false);
            as.setBasePlate(false);
            as.setArms(true);
            as.setVisible(true);
            as.setInvulnerable(true);
            as.setPersistent(true);
            as.setRemoveWhenFarAway(false);
            as.setCanPickupItems(false);
        });
        NPC npc = new NPC(name, stand);
        npcs.put(name.toLowerCase(), npc);
        byId.put(stand.getUniqueId(), npc);
        saveAll();
        return true;
    }

    public NPC getNPC(String name) {
        return npcs.get(name.toLowerCase());
    }

    public NPC getNPC(UUID uuid) {
        return byId.get(uuid);
    }

    public void select(Player player, NPC npc) {
        selections.put(player.getUniqueId(), npc);
    }

    public NPC getSelected(Player player) {
        return selections.get(player.getUniqueId());
    }

    public ItemStack createHead(String input) {
        ItemStack head = null;
        if (input.startsWith("hdb:")) {
            String id = input.substring(4);
            try {
                Class<?> apiClass = Class.forName("me.arcaniax.hdb.api.HeadDatabaseAPI");
                Object api = apiClass.getDeclaredConstructor().newInstance();
                head = (ItemStack) apiClass.getMethod("getItemHead", String.class).invoke(api, id);
            } catch (Exception ignored) {
            }
        } else {
            var item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.PLAYER_HEAD);
            var meta = (org.bukkit.inventory.meta.SkullMeta) item.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(input));
                item.setItemMeta(meta);
            }
            head = item;
        }
        return head;
    }

    public void setAction(String npcName, String action) {
        actions.put(npcName.toLowerCase(), action);
        saveAll();
    }

    public String getAction(String npcName) {
        return actions.get(npcName.toLowerCase());
    }

    /**
     * Deletes an NPC by name and removes all associated data.
     *
     * @param name the NPC name
     * @return true if the NPC existed and was removed
     */
    public boolean delete(String name) {
        NPC npc = npcs.remove(name.toLowerCase());
        if (npc == null) {
            return false;
        }
        byId.remove(npc.getStand().getUniqueId());
        actions.remove(name.toLowerCase());
        // remove selections pointing to this NPC
        selections.values().removeIf(n -> n == npc);
        npc.getStand().remove();
        saveAll();
        return true;
    }

    /**
     * Returns a set of all NPC names.
     */
    public java.util.Set<String> getNames() {
        return new java.util.HashSet<>(npcs.keySet());
    }

    public void executeAction(Player player, String npcName) {
        String action = getAction(npcName);
        if (action == null) return;
        if (action.toLowerCase().startsWith("runcommand ")) {
            String cmd = action.substring("runcommand ".length());
            Bukkit.dispatchCommand(player, cmd.replace("%player%", player.getName()));
        } else if (action.equalsIgnoreCase("openservermenu")) {
            plugin.getServerSelector().openMenu(player);
        }
    }

    public void removeAll() {
        for (NPC npc : npcs.values()) {
            npc.getStand().remove();
        }
        npcs.clear();
        byId.clear();
    }
}
