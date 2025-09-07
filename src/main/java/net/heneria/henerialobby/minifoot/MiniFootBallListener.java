package net.heneria.henerialobby.minifoot;

import org.bukkit.Sound;
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
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (!miniFootManager.isTheFootball(event.getEntity())) {
            return;
        }

        event.setCancelled(true);

        Slime ball = (Slime) event.getEntity();
        Vector direction = player.getLocation().getDirection().normalize();
        direction.setY(0.35);
        double pushPower = miniFootManager.getBallPushMultiplier();
        ball.setVelocity(direction.multiply(pushPower));

        player.playSound(player.getLocation(), Sound.ENTITY_SLIME_SQUISH_SMALL, 0.5f, 1.0f);
    }
}
