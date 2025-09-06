package net.heneria.henerialobby.selector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServerSelector {

    private final HeneriaLobby plugin;
    private final FileConfiguration menuConfig;
    private final Map<UUID, Map<Integer, String>> actions = new HashMap<>();
    private final ItemStack selectorItem;
    private final int selectorSlot;
    private final String menuTitle;
    private final int menuSize;
    private final ConfigurationSection itemsSection;

    public ServerSelector(HeneriaLobby plugin) {
        this.plugin = plugin;
        File configFile = new File(plugin.getDataFolder(), "server-selector.yml");
        if (!configFile.exists()) {
            plugin.saveResource("server-selector.yml", false);
        }
        menuConfig = YamlConfiguration.loadConfiguration(configFile);
        this.menuTitle = menuConfig.getString("menu-title", "&6Menu des jeux");
        this.menuSize = menuConfig.getInt("menu-size", 3) * 9;
        this.itemsSection = menuConfig.getConfigurationSection("items");

        ConfigurationSection sel = plugin.getConfig().getConfigurationSection("selector-item");
        this.selectorSlot = sel != null ? sel.getInt("slot", 0) : 0;
        this.selectorItem = createSelectorItem(sel);
    }

    private ItemStack createSelectorItem(ConfigurationSection sec) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        if (sec != null) {
            applyTexture(item);
            ItemMeta meta = item.getItemMeta();
            if (sec.contains("name")) {
                meta.setDisplayName(color(sec.getString("name")));
            }
            List<String> lore = sec.getStringList("lore");
            if (!lore.isEmpty()) {
                meta.setLore(lore.stream().map(this::color).collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private void applyTexture(ItemStack item) {
        try {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

            // 1. Récupérer la valeur de la texture depuis le fichier config.yml
            String base64Texture = plugin.getConfig().getString("selector-item.texture");

            if (base64Texture != null && !base64Texture.isEmpty()) {
                // 2. Créer un "GameProfile" de Mojang. C'est l'objet qui contient réellement les données de skin.
                // On lui donne un UUID aléatoire et un nom non-nul.
                GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "HeneriaSelectorItem");

                // 3. Ajouter la propriété de texture au GameProfile
                gameProfile.getProperties().put("textures", new Property("textures", base64Texture));

                try {
                    // 4. Utiliser la "réflexion" pour injecter notre GameProfile modifié dans la SkullMeta.
                    // C'est une méthode avancée pour contourner les limitations de l'API.
                    Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(skullMeta, gameProfile);

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    plugin.getLogger().warning("Impossible d'appliquer la texture personnalisée à l'item du sélecteur.");
                    e.printStackTrace();
                }
            }

            item.setItemMeta(skullMeta);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply texture: " + e.getMessage());
        }
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public int getSelectorSlot() {
        return selectorSlot;
    }

    public ItemStack getSelectorItem() {
        return selectorItem.clone();
    }

    public boolean isSelectorItem(ItemStack stack) {
        return stack != null && stack.isSimilar(selectorItem);
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, menuSize, color(plugin.applyPlaceholders(player, menuTitle)));
        Map<Integer, String> actionMap = new HashMap<>();
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection cs = itemsSection.getConfigurationSection(key);
                if (cs == null) continue;

                if (cs.contains("slots")) {
                    ItemStack item = buildItem(cs, player);
                    for (int slot : cs.getIntegerList("slots")) {
                        inv.setItem(slot, item);
                    }
                } else {
                    int slot = cs.getInt("slot");
                    ItemStack item = buildItem(cs, player);
                    inv.setItem(slot, item);
                    String action = cs.getString("action");
                    if (action != null) {
                        actionMap.put(slot, action);
                    }
                }
            }
        }
        player.openInventory(inv);
        actions.put(player.getUniqueId(), actionMap);
    }

    private ItemStack buildItem(ConfigurationSection cs, Player player) {
        ItemStack item = null;
        String headId = cs.getString("head-id");
        if (headId != null && plugin.getHdbApi() != null) {
            try {
                item = plugin.getHdbApi().getItemHead(headId);
                if (item != null) {
                    item = item.clone();
                }
            } catch (Exception e) {
                item = null;
            }
        }
        if (item == null) {
            String matName = cs.getString("material", "STONE");
            item = new ItemStack(Material.valueOf(matName));
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(plugin.applyPlaceholders(player, cs.getString("name", ""))));
        List<String> lore = cs.getStringList("lore");
        if (!lore.isEmpty()) {
            meta.setLore(lore.stream()
                    .map(line -> color(plugin.applyPlaceholders(player, line)))
                    .collect(Collectors.toList()));
        }
        if (cs.getBoolean("enchanted", false)) {
            // Use the modern UNBREAKING enchantment to give the shiny effect without durability tooltip.
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    public boolean isMenu(String title) {
        return ChatColor.stripColor(title).equals(ChatColor.stripColor(color(menuTitle)));
    }

    public String getAction(Player player, int slot) {
        Map<Integer, String> map = actions.get(player.getUniqueId());
        return map != null ? map.get(slot) : null;
    }

    public void clearActions(UUID uuid) {
        actions.remove(uuid);
    }
}
