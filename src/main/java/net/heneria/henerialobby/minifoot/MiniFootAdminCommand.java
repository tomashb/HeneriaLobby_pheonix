package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MiniFootAdminCommand implements CommandExecutor, TabCompleter {
    private final HeneriaLobby plugin;
    private final MiniFootManager manager;

    public MiniFootAdminCommand(HeneriaLobby plugin, MiniFootManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is for players only");
            return true;
        }
        if (!player.hasPermission("heneria.lobby.admin.minifoot")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("Usage: /minifootadmin <setarena|setgoal|setspawn|setballspawn>");
            return true;
        }
        String sub = args[0].toLowerCase();
        Location loc = player.getLocation();
        switch (sub) {
            case "setarena" -> {
                if (args.length < 2 || (!args[1].equals("1") && !args[1].equals("2"))) {
                    player.sendMessage("Usage: /minifootadmin setarena <1|2>");
                    return true;
                }
                int index = Integer.parseInt(args[1]);
                manager.setArenaPos(index, loc);
                player.sendMessage("Arena corner " + index + " set to " + format(loc));
            }
            case "setgoal" -> {
                if (args.length < 3 || (!args[1].equalsIgnoreCase("blue") && !args[1].equalsIgnoreCase("red"))
                        || (!args[2].equals("1") && !args[2].equals("2"))) {
                    player.sendMessage("Usage: /minifootadmin setgoal <blue|red> <1|2>");
                    return true;
                }
                String team = args[1].toLowerCase();
                int index = Integer.parseInt(args[2]);
                manager.setTeamGoalPos(team, index, loc);
                player.sendMessage("Goal " + team + " corner " + index + " set to " + format(loc));
            }
            case "setspawn" -> {
                if (args.length < 2 || (!args[1].equalsIgnoreCase("blue") && !args[1].equalsIgnoreCase("red"))) {
                    player.sendMessage("Usage: /minifootadmin setspawn <blue|red>");
                    return true;
                }
                String team = args[1].toLowerCase();
                manager.setTeamSpawn(team, loc);
                player.sendMessage("Spawn for team " + team + " set to " + format(loc));
            }
            case "setballspawn" -> {
                manager.setBallSpawn(loc);
                player.sendMessage("Ball spawn set to " + format(loc));
            }
            default -> player.sendMessage("Unknown subcommand");
        }
        return true;
    }

    private String format(Location loc) {
        return String.format("%s %d %d %d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("setarena", "setgoal", "setspawn", "setballspawn");
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "setarena" -> Arrays.asList("1", "2");
                case "setgoal", "setspawn" -> Arrays.asList("blue", "red");
                default -> Collections.emptyList();
            };
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("setgoal")) {
            return Arrays.asList("1", "2");
        }
        return Collections.emptyList();
    }
}
