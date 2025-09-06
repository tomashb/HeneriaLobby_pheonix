package net.heneria.henerialobby.npc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import com.masecla.api.HeadDatabaseAPI;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Handles the /npc command and its subcommands.
 */
public class NPCCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX_SUCCESS = "§a[Succès] §f";
    private static final String PREFIX_ERROR = "§c[Erreur] §f";
    private static final String PREFIX_INFO = "§e[Info] §f";

    private final NPCManager manager;

    public NPCCommand(NPCManager manager) {
        this.manager = manager;
    }

    private void success(Player player, String msg) {
        player.sendMessage(PREFIX_SUCCESS + msg);
    }

    private void error(Player player, String msg) {
        player.sendMessage(PREFIX_ERROR + msg);
    }

    private void info(Player player, String msg) {
        player.sendMessage(PREFIX_INFO + msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only");
            return true;
        }
        if (!player.hasPermission("heneria.lobby.admin.npc")) {
            error(player, "Vous n'avez pas la permission.");
            return true;
        }
        if (args.length == 0) {
            info(player, "Utilisez /npc help pour la liste des commandes.");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help":
                sendHelp(player);
                return true;
            case "create":
                if (args.length < 2) {
                    error(player, "Usage: /npc create <name>");
                    return true;
                }
                String name = args[1];
                if (manager.create(name, player.getLocation())) {
                    success(player, "NPC créé.");
                } else {
                    error(player, "Un NPC avec ce nom existe déjà.");
                }
                return true;
            case "delete":
                if (args.length < 2) {
                    error(player, "Usage: /npc delete <name>");
                    return true;
                }
                if (manager.delete(args[1])) {
                    success(player, "NPC supprimé.");
                } else {
                    error(player, "NPC introuvable.");
                }
                return true;
            case "select":
                if (args.length < 2) {
                    error(player, "Usage: /npc select <name>");
                    return true;
                }
                NPC npc = manager.getNPC(args[1]);
                if (npc == null) {
                    error(player, "NPC introuvable.");
                } else {
                    manager.select(player, npc);
                    success(player, "NPC sélectionné: " + npc.getName());
                }
                return true;
            case "move":
                npc = manager.getSelected(player);
                if (npc == null) {
                    error(player, "Aucun NPC sélectionné.");
                    return true;
                }
                npc.getStand().teleport(player.getLocation());
                manager.saveAll();
                success(player, "NPC déplacé.");
                return true;
            case "sethead":
            case "skin": // alias
                if (args.length < 2) {
                    error(player, "Usage: /npc sethead <player|hdb:id>");
                    return true;
                }
                npc = manager.getSelected(player);
                if (npc == null) {
                    error(player, "Aucun NPC sélectionné.");
                    return true;
                }
                String target = args[1];
                if (target.startsWith("hdb:")) {
                    if (Bukkit.getPluginManager().getPlugin("HeadDatabase") == null) {
                        error(player, "Le plugin HeadDatabase n'est pas installé.");
                        return true;
                    }
                    String id = target.substring(4);
                    manager.getPlugin().getLogger().info("[DEBUG] La commande a extrait l'ID suivant : '" + id + "'");
                    HeadDatabaseAPI api = manager.getPlugin().getHdbApi();
                    if (api == null) {
                        manager.getPlugin().getLogger().severe("[DEBUG] La commande ne peut pas continuer car la variable hdbApi est null !");
                        player.sendMessage("§c[Erreur Interne] L'API de HeadDatabase n'est pas initialisée.");
                        return true;
                    }
                    ItemStack head = api.getItemHead(id);
                    if (head == null) {
                        manager.getPlugin().getLogger().warning("[DEBUG] L'appel à hdbApi.getItemHead('" + id + "') a retourné null. L'ID est peut-être invalide pour l'API.");
                        player.sendMessage("§cTête introuvable via l'API.");
                        return true;
                    } else {
                        manager.getPlugin().getLogger().info("[DEBUG] L'API a retourné une tête avec succès ! Application sur le PNJ...");
                        if (npc.getStand().getEquipment() != null) {
                            npc.getStand().getEquipment().setHelmet(head);
                            manager.saveAll();
                        }
                        player.sendMessage("§aTête appliquée avec succès !");
                        return true;
                    }
                }
                ItemStack head = manager.createHead(target);
                if (head == null) {
                    error(player, "Tête introuvable.");
                    return true;
                }
                if (npc.getStand().getEquipment() != null) {
                    npc.getStand().getEquipment().setHelmet(head);
                    manager.saveAll();
                    success(player, "Skin appliqué.");
                }
                return true;
            case "equip":
                npc = manager.getSelected(player);
                if (npc == null) {
                    error(player, "Aucun NPC sélectionné.");
                    return true;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || item.getType() == Material.AIR) {
                    error(player, "Tenez un objet dans votre main.");
                    return true;
                }
                var eq = npc.getStand().getEquipment();
                if (eq != null) {
                    String type = item.getType().name();
                    if (type.endsWith("_HELMET")) {
                        eq.setHelmet(item.clone());
                    } else if (type.endsWith("_CHESTPLATE")) {
                        eq.setChestplate(item.clone());
                    } else if (type.endsWith("_LEGGINGS")) {
                        eq.setLeggings(item.clone());
                    } else if (type.endsWith("_BOOTS")) {
                        eq.setBoots(item.clone());
                    } else {
                        eq.setItemInMainHand(item.clone());
                    }
                    manager.saveAll();
                    success(player, "Objet équipé.");
                }
                return true;
            case "unequip":
                if (args.length < 2) {
                    error(player, "Usage: /npc unequip <helmet|chestplate|leggings|boots|hand|offhand>");
                    return true;
                }
                npc = manager.getSelected(player);
                if (npc == null) {
                    error(player, "Aucun NPC sélectionné.");
                    return true;
                }
                eq = npc.getStand().getEquipment();
                if (eq != null) {
                    switch (args[1].toLowerCase(Locale.ROOT)) {
                        case "helmet": eq.setHelmet(null); break;
                        case "chestplate": eq.setChestplate(null); break;
                        case "leggings": eq.setLeggings(null); break;
                        case "boots": eq.setBoots(null); break;
                        case "hand": eq.setItemInMainHand(null); break;
                        case "offhand": eq.setItemInOffHand(null); break;
                        default:
                            error(player, "Slot invalide.");
                            return true;
                    }
                    manager.saveAll();
                    success(player, "Équipement retiré.");
                }
                return true;
            case "link":
                if (args.length < 2) {
                    error(player, "Usage: /npc link <action> [args...]");
                    return true;
                }
                npc = manager.getSelected(player);
                if (npc == null) {
                    error(player, "Aucun NPC sélectionné.");
                    return true;
                }
                String action = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                manager.setAction(npc.getName(), action);
                success(player, "Action liée.");
                return true;
            default:
                info(player, "Utilisez /npc help pour la liste des commandes.");
                return true;
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6===== Commandes PNJ =====");
        player.sendMessage("§e/npc create <nom> §7- Crée un PNJ");
        player.sendMessage("§e/npc delete <nom> §7- Supprime un PNJ");
        player.sendMessage("§e/npc select <nom> §7- Sélectionne un PNJ");
        player.sendMessage("§e/npc move §7- Déplace le PNJ sélectionné");
        player.sendMessage("§e/npc sethead <joueur|hdb:id> §7- Change la tête du PNJ sélectionné");
        player.sendMessage("§e/npc equip §7- Équipe l'objet tenu au PNJ sélectionné");
        player.sendMessage("§e/npc unequip <slot> §7- Retire un équipement");
        player.sendMessage("§e/npc link <action> [args] §7- Lie une action au PNJ sélectionné");
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("heneria.lobby.admin.npc")) {
            return java.util.Collections.emptyList();
        }
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("create", "delete", "select", "move", "sethead", "equip", "unequip", "link", "help"), new java.util.ArrayList<>());
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            switch (sub) {
                case "delete":
                case "select":
                    return StringUtil.copyPartialMatches(args[1], manager.getNames(), new java.util.ArrayList<>());
                case "unequip":
                    return StringUtil.copyPartialMatches(args[1], Arrays.asList("helmet", "chestplate", "leggings", "boots", "hand", "offhand"), new java.util.ArrayList<>());
                case "link":
                    return StringUtil.copyPartialMatches(args[1], Arrays.asList("openservermenu", "runcommand", "sendmessage"), new java.util.ArrayList<>());
                case "sethead":
                case "skin":
                    return StringUtil.copyPartialMatches(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), new java.util.ArrayList<>());
                default:
                    break;
            }
        }
        return java.util.Collections.emptyList();
    }
}

