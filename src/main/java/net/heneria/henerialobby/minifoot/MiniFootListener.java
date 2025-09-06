package net.heneria.henerialobby.minifoot;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MiniFootListener implements Listener {

    private final HeneriaLobby plugin;
    private final MiniFootManager miniFootManager;

    public MiniFootListener(HeneriaLobby plugin, MiniFootManager miniFootManager) {
        this.plugin = plugin;
        this.miniFootManager = miniFootManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // On ne fait rien si le joueur n'a pas changé de bloc
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // --- TRACE 1 ---
        plugin.getLogger().info("[TRACE 1] Événement de mouvement détecté pour " + player.getName());

        // Vérification si le joueur est déjà dans une partie
        if (miniFootManager.isInGame(player)) {
            // --- TRACE 2 ---
            plugin.getLogger().info("[TRACE 2] Le joueur est DÉJÀ en partie. La logique s'arrête ici.");
            return;
        }

        // Chargement des positions de l'arène
        Location pos1 = miniFootManager.getArenaPos1();
        Location pos2 = miniFootManager.getArenaPos2();

        if (pos1 == null || pos2 == null || pos1.getWorld() == null) {
            // --- TRACE 3 ---
            plugin.getLogger().severe("[TRACE 3] Les coordonnées de l'arène ne sont pas chargées (null). La logique s'arrête ici.");
            return;
        }

        // --- TRACE 4 ---
        plugin.getLogger().info("[TRACE 4] Coordonnées de l'arène chargées. Monde attendu : '" + pos1.getWorld().getName() + "'");

        // Vérification du monde
        if (!event.getTo().getWorld().equals(pos1.getWorld())) {
            // --- TRACE 5 ---
            plugin.getLogger().warning("[TRACE 5] Le joueur n'est pas dans le bon monde (il est dans '" + event.getTo().getWorld().getName() + "'). La logique s'arrête ici.");
            return;
        }

        // Calcul des limites
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        // Vérification finale
        boolean isInside = (event.getTo().getBlockX() >= minX && event.getTo().getBlockX() <= maxX &&
                            event.getTo().getBlockY() >= minY && event.getTo().getBlockY() <= maxY &&
                            event.getTo().getBlockZ() >= minZ && event.getTo().getBlockZ() <= maxZ);

        // --- TRACE 6 ---
        plugin.getLogger().info("[TRACE 6] Vérification de la position... Le joueur est à l'intérieur ? -> " + isInside);

        if (isInside) {
            // --- TRACE 7 ---
            plugin.getLogger().info("[TRACE 7] Le joueur EST DANS LA ZONE. Appel de la méthode pour rejoindre une équipe...");
            miniFootManager.addPlayerToTeam(player);
        }
    }
}

