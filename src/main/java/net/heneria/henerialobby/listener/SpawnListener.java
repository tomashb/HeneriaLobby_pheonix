package net.heneria.henerialobby.listener;

import net.heneria.henerialobby.HeneriaLobby;
import net.heneria.henerialobby.spawn.SpawnManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SpawnListener implements Listener {

    private final HeneriaLobby plugin;
    private final SpawnManager spawnManager;

    public SpawnListener(HeneriaLobby plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (spawnManager.teleport(event.getPlayer())) {
            event.getPlayer().sendMessage(plugin.applyPlaceholders(event.getPlayer(), plugin.getMessage("spawn-teleport")));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!spawnManager.hasSpawn()) {
            return;
        }
        if (event.getTo() != null && event.getTo().getY() < plugin.getConfig().getDouble("void-teleport-y")) {
            spawnManager.teleport(event.getPlayer());
            event.getPlayer().sendMessage(plugin.applyPlaceholders(event.getPlayer(), plugin.getMessage("spawn-teleport")));
        }
    }
}

