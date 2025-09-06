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
        // --- PARTIE 1 : OPTIMISATION ET VÉRIFICATIONS PRÉLIMINAIRES ---

        // On ne fait rien si le joueur n'a pas changé de bloc (ignore les mouvements de souris)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Location to = event.getTo();

        // Le développeur doit avoir une méthode pour savoir si le joueur est déjà dans une partie
        if (miniFootManager.isInGame(player)) {
            // Ici, on pourra plus tard vérifier s'il sort de l'arène pour le faire quitter.
            return;
        }

        // Charger les coins de l'arène (depuis les variables chargées au démarrage)
        Location pos1 = miniFootManager.getArenaPos1();
        Location pos2 = miniFootManager.getArenaPos2();

        // On vérifie que l'arène est bien configurée
        if (pos1 == null || pos2 == null || pos1.getWorld() == null) {
            return;
        }

        // On vérifie que le joueur est dans le bon monde
        if (!to.getWorld().equals(pos1.getWorld())) {
            return;
        }

        // --- PARTIE 2 : LA COMPARAISON FINALE ET CORRECTE ---

        // Calcul des limites min/max de la zone
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        // La condition IF qui vérifie si la position du joueur est DANS le cube de l'arène
        boolean isInside = (to.getBlockX() >= minX && to.getBlockX() <= maxX &&
                            to.getBlockY() >= minY && to.getBlockY() <= maxY &&
                            to.getBlockZ() >= minZ && to.getBlockZ() <= maxZ);

        // --- PARTIE 3 : DÉCLENCHEMENT DE L'ACTION ---

        if (isInside) {
            // Le joueur est entré dans la zone !
            // Appeler la méthode qui gère l'ajout du joueur à une équipe.
            // Par exemple :
            miniFootManager.addPlayerToTeam(player);
        }
    }
}

