package net.heneria.henerialobby.npc;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Handles the /npc command and its subcommands.
 */
public class NPCCommand implements CommandExecutor {

    private final NPCManager manager;

    public NPCCommand(NPCManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only");
            return true;
        }
        if (!player.hasPermission("heneria.lobby.admin.npc")) {
            player.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§cUsage: /npc <create|select|sethead|equip|unequip|link>...");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /npc create <name>");
                    return true;
                }
                String name = args[1];
                if (manager.create(name, player.getLocation())) {
                    player.sendMessage("§aNPC créé.");
                } else {
                    player.sendMessage("§cUn NPC avec ce nom existe déjà.");
                }
                return true;
            case "select":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /npc select <name>");
                    return true;
                }
                NPC npc = manager.getNPC(args[1]);
                if (npc == null) {
                    player.sendMessage("§cNPC introuvable.");
                } else {
                    manager.select(player, npc);
                    player.sendMessage("§aNPC sélectionné: " + npc.getName());
                }
                return true;
            case "sethead":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /npc sethead <player|hdb:id>");
                    return true;
                }
                npc = manager.getSelected(player);
                if (npc == null) {
                    player.sendMessage("§cAucun NPC sélectionné.");
                    return true;
                }
                ItemStack head = manager.createHead(args[1]);
                if (head == null) {
                    player.sendMessage("§cTête introuvable.");
                    return true;
                }
                if (npc.getStand().getEquipment() != null) {
                    npc.getStand().getEquipment().setHelmet(head);
                    manager.saveAll();
                    player.sendMessage("§aTête définie.");
                }
                return true;
            case "equip":
                npc = manager.getSelected(player);
                if (npc == null) {
                    player.sendMessage("§cAucun NPC sélectionné.");
                    return true;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || item.getType() == Material.AIR) {
                    player.sendMessage("§cTenez un objet dans votre main.");
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
                    player.sendMessage("§aObjet équipé.");
                }
                return true;
            case "unequip":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /npc unequip <helmet|chestplate|leggings|boots|hand|offhand>");
                    return true;
                }
                npc = manager.getSelected(player);
                if (npc == null) {
                    player.sendMessage("§cAucun NPC sélectionné.");
                    return true;
                }
                eq = npc.getStand().getEquipment();
                if (eq != null) {
                    switch (args[1].toLowerCase()) {
                        case "helmet": eq.setHelmet(null); break;
                        case "chestplate": eq.setChestplate(null); break;
                        case "leggings": eq.setLeggings(null); break;
                        case "boots": eq.setBoots(null); break;
                        case "hand": eq.setItemInMainHand(null); break;
                        case "offhand": eq.setItemInOffHand(null); break;
                        default:
                            player.sendMessage("§cSlot invalide.");
                            return true;
                    }
                    manager.saveAll();
                    player.sendMessage("§aÉquipement retiré.");
                }
                return true;
            case "link":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /npc link <action> [args...]");
                    return true;
                }
                npc = manager.getSelected(player);
                if (npc == null) {
                    player.sendMessage("§cAucun NPC sélectionné.");
                    return true;
                }
                String action = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                manager.setAction(npc.getName(), action);
                player.sendMessage("§aAction liée.");
                return true;
            default:
                player.sendMessage("§cUsage: /npc <create|select|sethead|equip|unequip|link>...");
                return true;
        }
    }
}
