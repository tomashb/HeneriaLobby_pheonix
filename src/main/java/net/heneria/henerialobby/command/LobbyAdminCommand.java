package net.heneria.henerialobby.command;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyAdminCommand implements CommandExecutor {

    private final HeneriaLobby plugin;

    public LobbyAdminCommand(HeneriaLobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("heneria.lobby.admin.reload")) {
                    sender.sendMessage(plugin.getMessage("no-permission"));
                    return true;
                }
                plugin.reloadAll();
                sender.sendMessage(ChatColor.GREEN + "Configurations rechargées.");
                return true;
            }
            if (args[0].equalsIgnoreCase("sheep")) {
                return handleSheep(sender, label, args);
            }
        }
        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " reload|sheep");
        return true;
    }

    private boolean handleSheep(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande doit être exécutée en jeu.");
            return true;
        }
        if (!player.hasPermission("heneria.lobby.admin.sheep")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }
        if (plugin.getPartySheepManager() == null) {
            sender.sendMessage(ChatColor.RED + "Les moutons de fête sont désactivés.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " sheep <add|remove|list> [radius]");
            return true;
        }
        String sub = args[1].toLowerCase();
        switch (sub) {
            case "add":
                plugin.getPartySheepManager().addSheep(player.getLocation());
                sender.sendMessage(ChatColor.GREEN + "Mouton de fête ajouté.");
                return true;
            case "remove":
                double radius = 5.0;
                if (args.length >= 3) {
                    try {
                        radius = Double.parseDouble(args[2]);
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(ChatColor.RED + "Rayon invalide.");
                        return true;
                    }
                }
                int removed = plugin.getPartySheepManager().removeSheep(player.getLocation(), radius);
                sender.sendMessage(ChatColor.GREEN + String.valueOf(removed) + " mouton(s) supprimé(s).");
                return true;
            case "list":
                plugin.getPartySheepManager().listSheeps(sender);
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " sheep <add|remove|list> [radius]");
                return true;
        }
    }
}

