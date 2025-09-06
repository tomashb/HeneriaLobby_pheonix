package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MiniFootListener implements Listener {

    private final HeneriaLobby plugin;

    public MiniFootListener(HeneriaLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // --- DÉBUT DU DEBUG ---
        // Ce message doit apparaître dans la console CHAQUE FOIS qu'un joueur bouge.
        // S'il n'apparaît JAMAIS, cela signifie que l'événement n'est pas enregistré (voir point 1).
        System.out.println("[DEBUG MiniFoot] PlayerMoveEvent déclenché pour " + event.getPlayer().getName());
        // --- FIN DU DEBUG ---

        // ... le reste du code qui vérifie si le joueur entre dans la zone ...
    }
}

