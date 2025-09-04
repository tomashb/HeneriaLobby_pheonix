package net.heneria.henerialobby.command;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CustomCommand extends Command {

    private final HeneriaLobby plugin;
    private final List<String> messages;

    public CustomCommand(String name, List<String> aliases, List<String> messages, HeneriaLobby plugin) {
        super(name);
        if (aliases != null) {
            this.setAliases(aliases);
        }
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }
        for (String line : messages) {
            player.sendMessage(plugin.applyPlaceholders(player, ChatColor.translateAlternateColorCodes('&', line)));
        }
        return true;
    }
}

