package net.heneria.henerialobby.command;

import net.heneria.henerialobby.selector.ServerSelector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServersCommand implements CommandExecutor {

    private final ServerSelector selector;

    public ServersCommand(ServerSelector selector) {
        this.selector = selector;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            selector.openMenu(player);
        }
        return true;
    }
}
