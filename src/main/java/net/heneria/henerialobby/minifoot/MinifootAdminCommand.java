package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MinifootAdminCommand implements CommandExecutor, TabCompleter {

    private final HeneriaLobby plugin;
    private final MiniFootManager manager;

    public MinifootAdminCommand(HeneriaLobby plugin, MiniFootManager manager) {
        this.plugin = plugin;
        this.manager = manager;
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
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "setarena":
                if (!manager.hasSelection(player)) {
                    player.sendMessage(ChatColor.RED + "Sélectionnez deux positions avec la hache en bois.");
                    return true;
                }
                manager.saveArena(manager.getPos1(player), manager.getPos2(player));
                player.sendMessage(ChatColor.GREEN + "Arène définie.");
                return true;
            case "setgoal":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " setgoal <blue|red>");
                    return true;
                }
                if (!manager.hasSelection(player)) {
                    player.sendMessage(ChatColor.RED + "Sélectionnez deux positions avec la hache en bois.");
                    return true;
                }
                String team = args[1].toLowerCase();
                if (!team.equals("blue") && !team.equals("red")) {
                    player.sendMessage(ChatColor.RED + "L'équipe doit être blue ou red.");
                    return true;
                }
                manager.saveGoal(team, manager.getPos1(player), manager.getPos2(player));
                player.sendMessage(ChatColor.GREEN + "But " + team + " défini.");
                return true;
            case "setspawn":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " setspawn <blue|red>");
                    return true;
                }
                String color = args[1].toLowerCase();
                if (!color.equals("blue") && !color.equals("red")) {
                    player.sendMessage(ChatColor.RED + "L'équipe doit être blue ou red.");
                    return true;
                }
                manager.saveSpawn(color, player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Spawn " + color + " défini.");
                return true;
            case "setballspawn":
                manager.saveBallSpawn(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Spawn de la balle défini.");
                return true;
            default:
                sendHelp(player, label);
                return true;
        }
    }

    private void sendHelp(Player player, String label) {
        player.sendMessage(ChatColor.YELLOW + "Mini-Foot - Commandes d'administration:");
        player.sendMessage(ChatColor.AQUA + "/" + label + " setarena" + ChatColor.GRAY + " - Définit la zone de l'arène");
        player.sendMessage(ChatColor.AQUA + "/" + label + " setgoal <blue|red>" + ChatColor.GRAY + " - Définit une zone de but");
        player.sendMessage(ChatColor.AQUA + "/" + label + " setspawn <blue|red>" + ChatColor.GRAY + " - Définit un spawn d'équipe");
        player.sendMessage(ChatColor.AQUA + "/" + label + " setballspawn" + ChatColor.GRAY + " - Définit le spawn de la balle");
        player.sendMessage(ChatColor.AQUA + "/" + label + " help" + ChatColor.GRAY + " - Affiche cette aide");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("heneria.lobby.admin.minifoot")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            List<String> subs = Arrays.asList("setarena", "setgoal", "setspawn", "setballspawn", "help");
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], subs, completions);
            Collections.sort(completions);
            return completions;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("setgoal") || args[0].equalsIgnoreCase("setspawn"))) {
            List<String> colors = Arrays.asList("blue", "red");
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[1], colors, completions);
            Collections.sort(completions);
            return completions;
        }
        return Collections.emptyList();
    }
}

