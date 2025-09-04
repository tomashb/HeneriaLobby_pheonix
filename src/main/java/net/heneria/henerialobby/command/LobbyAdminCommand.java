package net.heneria.henerialobby.command;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LobbyAdminCommand implements CommandExecutor {

    private final HeneriaLobby plugin;

    public LobbyAdminCommand(HeneriaLobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("heneria.lobby.admin.reload")) {
                sender.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }
            plugin.reloadAll();
            sender.sendMessage(ChatColor.GREEN + "Configurations rechargées.");
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " reload");
        return true;
    }
}

