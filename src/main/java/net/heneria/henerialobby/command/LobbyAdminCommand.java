package net.heneria.henerialobby.command;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.heneria.henerialobby.sheep.Selection;

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
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " sheep <setspawnregion|listregions> [name]");
            return true;
        }
        String sub = args[1].toLowerCase();
        switch (sub) {
            case "setspawnregion":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " sheep setspawnregion <name>");
                    return true;
                }
                Selection sel = plugin.getPartySheepManager().getSelection(player);
                if (sel == null || !sel.isComplete()) {
                    sender.sendMessage(ChatColor.RED + "Sélection incomplète. Utilisez la hache en bois pour définir deux positions.");
                    return true;
                }
                plugin.getPartySheepManager().addSpawnRegion(args[2], sel.getPos1(), sel.getPos2());
                sender.sendMessage(ChatColor.GREEN + "Zone de spawn ajoutée : " + args[2]);
                return true;
            case "listregions":
                plugin.getPartySheepManager().listRegions(sender);
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " sheep <setspawnregion|listregions> [name]");
                return true;
        }
    }
}

