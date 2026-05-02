package com.advancedfly.listeners;

import com.advancedfly.managers.ConfigManager;
import com.advancedfly.managers.FlyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Spawns configurable particles at the player's feet while they are flying.
 *
 * We use MONITOR priority and check only significant movement to keep
 * server impact minimal — PlayerMoveEvent fires very frequently.
 */
public class PlayerMoveListener implements Listener {

    private final FlyManager    flyManager;
    private final ConfigManager config;

    public PlayerMoveListener(FlyManager flyManager, ConfigManager config) {
        this.flyManager = flyManager;
        this.config     = config;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        // Skip if particles are disabled
        if (!config.isParticlesEnabled()) return;

        Player player = event.getPlayer();

        // Only fire for flying players
        if (!flyManager.isFlying(player)) return;

        // Only spawn particles when the player actually moves (not just head rotation)
        // This compares block positions to filter head-only movement
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Spawn a small burst of particles at the player's location
        player.getWorld().spawnParticle(
                config.getParticleType(),
                player.getLocation(),
                5,       // count
                0.1,     // offsetX
                0.1,     // offsetY
                0.1,     // offsetZ
                0.01     // extra (speed)
        );
    }
}
