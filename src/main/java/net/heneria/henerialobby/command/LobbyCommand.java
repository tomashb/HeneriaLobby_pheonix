package net.heneria.henerialobby.command;

import net.heneria.henerialobby.HeneriaLobby;
import net.heneria.henerialobby.spawn.SpawnManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {

    private final HeneriaLobby plugin;
    private final SpawnManager spawnManager;

    public LobbyCommand(HeneriaLobby plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!spawnManager.teleport(player)) {
            player.sendMessage(plugin.getMessage("spawn-not-set"));
            return true;
        }

        player.sendMessage(plugin.applyPlaceholders(player, plugin.getMessage("spawn-teleport")));
        return true;
    }
}

