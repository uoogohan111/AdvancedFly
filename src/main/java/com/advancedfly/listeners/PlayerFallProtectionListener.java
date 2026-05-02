package com.advancedfly.listeners;

import com.advancedfly.managers.FlyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Cancels fall damage for players who recently had flight disabled,
 * protecting them during the configurable grace period.
 */
public class PlayerFallProtectionListener implements Listener {

    private final FlyManager flyManager;

    public PlayerFallProtectionListener(FlyManager flyManager) {
        this.flyManager = flyManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        // Only care about fall damage
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        if (!(event.getEntity() instanceof Player player)) return;

        if (flyManager.hasFallProtection(player)) {
            event.setCancelled(true);
        }
    }
}
