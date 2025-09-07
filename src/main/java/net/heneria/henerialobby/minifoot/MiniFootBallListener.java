package net.heneria.henerialobby.minifoot;

import org.bukkit.event.Listener;

public class MiniFootBallListener implements Listener {

    private final MiniFootManager miniFootManager;

    public MiniFootBallListener(MiniFootManager miniFootManager) {
        this.miniFootManager = miniFootManager;
    }

    // Ancienne mécanique de tir par clic gauche supprimée.
    // La poussée du ballon est désormais gérée par contact dans MiniFootListener#onPlayerMove.
}
