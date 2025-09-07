package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MiniFootAdminCommand implements CommandExecutor {

    private final HeneriaLobby plugin;
    private final MiniFootManager manager;
    private final MiniFootSelectionListener selector;

    public MiniFootAdminCommand(HeneriaLobby plugin, MiniFootManager manager, MiniFootSelectionListener selector) {
        this.plugin = plugin;
        this.manager = manager;
        this.selector = selector;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("heneria.lobby.admin.minifoot")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setarena" -> {
                Location p1 = selector.getPos1(player.getUniqueId());
                Location p2 = selector.getPos2(player.getUniqueId());
                if (p1 == null || p2 == null) {
                    player.sendMessage(ChatColor.RED + "Vous devez sélectionner deux positions avec la hache.");
                    return true;
                }
                manager.saveArena(p1, p2);
                player.sendMessage(ChatColor.GREEN + "[MiniFoot] La zone de l'arène a été définie avec succès !");
            }
            case "setgoal" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " setgoal <blue|red>");
                    return true;
                }
                Location p1 = selector.getPos1(player.getUniqueId());
                Location p2 = selector.getPos2(player.getUniqueId());
                if (p1 == null || p2 == null) {
                    player.sendMessage(ChatColor.RED + "Vous devez sélectionner deux positions avec la hache.");
                    return true;
                }
                String team = args[1].equalsIgnoreCase("blue") ? "blue" : args[1].equalsIgnoreCase("red") ? "red" : null;
                if (team == null) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " setgoal <blue|red>");
                    return true;
                }
                manager.saveGoal(team, p1, p2);
                if (team.equals("blue")) {
                    player.sendMessage(ChatColor.GREEN + "[MiniFoot] La zone de but pour l'équipe §9BLEUE §aa été définie.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "[MiniFoot] La zone de but pour l'équipe §cROUGE §aa été définie.");
                }
            }
            case "setspawn" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " setspawn <blue|red>");
                    return true;
                }
                String team = args[1].equalsIgnoreCase("blue") ? "blue" : args[1].equalsIgnoreCase("red") ? "red" : null;
                if (team == null) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " setspawn <blue|red>");
                    return true;
                }
                manager.saveSpawn(team, player.getLocation());
                if (team.equals("blue")) {
                    player.sendMessage(ChatColor.GREEN + "[MiniFoot] Le spawn de l'équipe §9BLEUE §aa été défini.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "[MiniFoot] Le spawn de l'équipe §cROUGE §aa été défini.");
                }
            }
            case "setballspawn" -> {
                manager.saveBallSpawn(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "[MiniFoot] Le spawn de la balle a été défini.");
            }
            default -> sendHelp(player, label);
        }
        return true;
    }

    private void sendHelp(Player player, String label) {
        player.sendMessage(ChatColor.YELLOW + "--- MiniFoot Admin ---");
        player.sendMessage(ChatColor.GOLD + "/" + label + " setarena" + ChatColor.WHITE + " - Définir l'arène" );
        player.sendMessage(ChatColor.GOLD + "/" + label + " setgoal <blue|red>" + ChatColor.WHITE + " - Définir le but" );
        player.sendMessage(ChatColor.GOLD + "/" + label + " setspawn <blue|red>" + ChatColor.WHITE + " - Définir le spawn" );
        player.sendMessage(ChatColor.GOLD + "/" + label + " setballspawn" + ChatColor.WHITE + " - Définir le spawn de la balle" );
    }
}
