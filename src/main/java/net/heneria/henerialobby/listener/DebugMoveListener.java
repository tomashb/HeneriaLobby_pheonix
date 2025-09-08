package net.heneria.henerialobby.listener;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listener used to debug unexpected movement cancellations by logging
 * whenever a {@link PlayerMoveEvent} that changes the player's position or
 * rotation is cancelled by another plugin.
 */
public class DebugMoveListener implements Listener {

    private final HeneriaLobby plugin;

    public DebugMoveListener(HeneriaLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMoveDebug(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()
                || from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch()) {
            if (event.isCancelled()) {
                plugin.getLogger().warning("[MOVE-DEBUG] PlayerMoveEvent for "
                        + event.getPlayer().getName() + " was cancelled!");
            }
        }
    }
}

