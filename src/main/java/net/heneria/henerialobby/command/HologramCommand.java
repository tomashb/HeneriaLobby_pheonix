package net.heneria.henerialobby.command;

import net.heneria.henerialobby.hologram.HologramManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.Arrays;
import java.util.Locale;

/**
 * Command allowing administrators to manage holograms.
 */
public class HologramCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX_SUCCESS = "§a[Succès] §f";
    private static final String PREFIX_ERROR = "§c[Erreur] §f";
    private static final String PREFIX_INFO = "§e[Info] §f";

    private final HologramManager manager;

    public HologramCommand(HologramManager manager) {
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
        if (!player.hasPermission("heneria.lobby.admin.hologram")) {
            error(player, "Vous n'avez pas la permission.");
            return true;
        }
        if (args.length == 0) {
            info(player, "Utilisez /hologram help pour la liste des commandes.");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help":
                sendHelp(player);
                return true;
            case "create":
                if (args.length < 3) {
                    error(player, "Usage: /hologram create <name> <text>");
                    return true;
                }
                String name = args[1];
                String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                if (manager.create(name, player.getLocation(), text)) {
                    success(player, "Hologramme créé.");
                } else {
                    error(player, "Un hologramme avec ce nom existe déjà.");
                }
                return true;
            case "delete":
                if (args.length < 2) {
                    error(player, "Usage: /hologram delete <name>");
                    return true;
                }
                if (manager.delete(args[1])) {
                    success(player, "Hologramme supprimé.");
                } else {
                    error(player, "Hologramme introuvable.");
                }
                return true;
            case "addline":
                if (args.length < 3) {
                    error(player, "Usage: /hologram addline <name> <text>");
                    return true;
                }
                name = args[1];
                text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                if (manager.addLine(name, text)) {
                    success(player, "Ligne ajoutée.");
                } else {
                    error(player, "Hologramme introuvable.");
                }
                return true;
            case "setline":
                if (args.length < 4) {
                    error(player, "Usage: /hologram setline <name> <number> <text>");
                    return true;
                }
                name = args[1];
                int index;
                try {
                    index = Integer.parseInt(args[2]) - 1;
                } catch (NumberFormatException e) {
                    error(player, "Numéro invalide.");
                    return true;
                }
                text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                if (manager.setLine(name, index, text)) {
                    success(player, "Ligne modifiée.");
                } else {
                    error(player, "Hologramme ou ligne introuvable.");
                }
                return true;
            case "movehere":
                if (args.length < 2) {
                    error(player, "Usage: /hologram movehere <name>");
                    return true;
                }
                name = args[1];
                Location loc = player.getLocation();
                if (manager.move(name, loc)) {
                    success(player, "Hologramme déplacé.");
                } else {
                    error(player, "Hologramme introuvable.");
                }
                return true;
            default:
                info(player, "Utilisez /hologram help pour la liste des commandes.");
                return true;
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6===== Commandes Hologrammes =====");
        player.sendMessage("§e/hologram create <nom> <texte> §7- Crée un hologramme");
        player.sendMessage("§e/hologram delete <nom> §7- Supprime un hologramme");
        player.sendMessage("§e/hologram addline <nom> <texte> §7- Ajoute une ligne");
        player.sendMessage("§e/hologram setline <nom> <num> <texte> §7- Modifie une ligne");
        player.sendMessage("§e/hologram movehere <nom> §7- Déplace l'hologramme ici");
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("heneria.lobby.admin.hologram")) {
            return java.util.Collections.emptyList();
        }
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("create", "delete", "addline", "setline", "movehere", "help"), new java.util.ArrayList<>());
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            switch (sub) {
                case "delete":
                case "addline":
                case "setline":
                case "movehere":
                    return StringUtil.copyPartialMatches(args[1], manager.getNames(), new java.util.ArrayList<>());
                default:
                    break;
            }
        }
        return java.util.Collections.emptyList();
    }
}
