package net.heneria.henerialobby.command;

import net.heneria.henerialobby.hologram.HologramManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command allowing administrators to manage holograms.
 */
public class HologramCommand implements CommandExecutor {

    private final HologramManager manager;

    public HologramCommand(HologramManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only");
            return true;
        }
        if (!player.hasPermission("heneria.lobby.admin.hologram")) {
            player.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§cUsage: /hologram <create|delete|addline|setline|movehere>...");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /hologram create <name> <text>");
                    return true;
                }
                String name = args[1];
                String text = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                if (manager.create(name, player.getLocation(), text)) {
                    player.sendMessage("§aHologramme créé.");
                } else {
                    player.sendMessage("§cUn hologramme avec ce nom existe déjà.");
                }
                return true;
            case "delete":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /hologram delete <name>");
                    return true;
                }
                if (manager.delete(args[1])) {
                    player.sendMessage("§aHologramme supprimé.");
                } else {
                    player.sendMessage("§cHologramme introuvable.");
                }
                return true;
            case "addline":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /hologram addline <name> <text>");
                    return true;
                }
                name = args[1];
                text = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                if (manager.addLine(name, text)) {
                    player.sendMessage("§aLigne ajoutée.");
                } else {
                    player.sendMessage("§cHologramme introuvable.");
                }
                return true;
            case "setline":
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /hologram setline <name> <number> <text>");
                    return true;
                }
                name = args[1];
                int index;
                try {
                    index = Integer.parseInt(args[2]) - 1;
                } catch (NumberFormatException e) {
                    player.sendMessage("§cNuméro invalide.");
                    return true;
                }
                text = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                if (manager.setLine(name, index, text)) {
                    player.sendMessage("§aLigne modifiée.");
                } else {
                    player.sendMessage("§cHologramme ou ligne introuvable.");
                }
                return true;
            case "movehere":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /hologram movehere <name>");
                    return true;
                }
                name = args[1];
                Location loc = player.getLocation();
                if (manager.move(name, loc)) {
                    player.sendMessage("§aHologramme déplacé.");
                } else {
                    player.sendMessage("§cHologramme introuvable.");
                }
                return true;
            default:
                player.sendMessage("§cUsage: /hologram <create|delete|addline|setline|movehere>...");
                return true;
        }
    }
}
