package net.heneria.henerialobby.listener;

import net.heneria.henerialobby.joineffects.JoinEffectsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEffectsListener implements Listener {

    private final JoinEffectsManager manager;

    public JoinEffectsListener(JoinEffectsManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        manager.play(event.getPlayer());
    }
}
