package net.heneria.henerialobby.minifoot;

import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class MiniFootBallListener implements Listener {

    private final MiniFootManager miniFootManager;

    public MiniFootBallListener(MiniFootManager miniFootManager) {
        this.miniFootManager = miniFootManager;
    }

    @EventHandler
    public void onBallHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Slime slime) || !(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!miniFootManager.isBall(slime)) {
            return;
        }
        event.setCancelled(true);
        Vector direction = slime.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
        direction.setY(0.3);
        slime.setVelocity(direction.multiply(miniFootManager.getBallPushMultiplier()));
    }
}
