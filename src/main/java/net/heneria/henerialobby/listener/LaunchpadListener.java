package net.heneria.henerialobby.listener;

import net.heneria.henerialobby.HeneriaLobby;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

public class LaunchpadListener implements Listener {

    private final HeneriaLobby plugin;
    private final Material plateMaterial;
    private final Material blockMaterial;
    private final double vertical;
    private final double horizontal;
    private final Sound sound;

    public LaunchpadListener(HeneriaLobby plugin) {
        this.plugin = plugin;
        var section = plugin.getConfig().getConfigurationSection("player-experience.launchpads");
        this.plateMaterial = Material.matchMaterial(section != null ? section.getString("plate-material", "STONE_PRESSURE_PLATE") : "STONE_PRESSURE_PLATE");
        this.blockMaterial = Material.matchMaterial(section != null ? section.getString("block-material", "IRON_BLOCK") : "IRON_BLOCK");
        this.vertical = section != null ? section.getDouble("power-vertical", 1.8) : 1.8;
        this.horizontal = section != null ? section.getDouble("power-horizontal", 2.2) : 2.2;
        Sound s;
        try {
            s = Sound.valueOf(section != null ? section.getString("sound", "ENTITY_BAT_TAKEOFF") : "ENTITY_BAT_TAKEOFF");
        } catch (IllegalArgumentException e) {
            s = Sound.ENTITY_BAT_TAKEOFF;
        }
        this.sound = s;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;
        if (!plugin.isLobbyWorld(event.getPlayer().getWorld())) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != plateMaterial) return;
        Block below = block.getRelative(BlockFace.DOWN);
        if (below.getType() != blockMaterial) return;
        var player = event.getPlayer();
        Vector vec = player.getLocation().getDirection().setY(0).normalize().multiply(horizontal);
        vec.setY(vertical);
        player.setVelocity(vec);
        player.playSound(player.getLocation(), sound, 1f, 1f);
    }
}
