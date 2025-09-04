package net.heneria.henerialobby.command;

import net.heneria.henerialobby.HeneriaLobby;
import net.heneria.henerialobby.spawn.SpawnManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand implements CommandExecutor {

    private final HeneriaLobby plugin;
    private final SpawnManager spawnManager;

    public SetLobbyCommand(HeneriaLobby plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("heneria.lobby.admin")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        spawnManager.saveSpawn(player.getLocation());
        player.sendMessage(plugin.applyPlaceholders(player, plugin.getMessage("spawn-set-success")));
        return true;
    }
}

