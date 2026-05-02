package com.advancedfly.listeners;

import com.advancedfly.managers.FreezeManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * FreezeListener enforces all restrictions on frozen players:
 *  - Cannot move (teleported back to freeze location)
 *  - Cannot break or place blocks
 *  - Cannot interact with anything
 *  - Cannot drop items
 *  - Cannot deal damage
 *  - Cleaned up on disconnect
 */
public class FreezeListener implements Listener {

    private final FreezeManager freezeManager;

    public FreezeListener(FreezeManager freezeManager) {
        this.freezeManager = freezeManager;
    }

    /** Lock frozen players in place — teleport back if they move. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!freezeManager.isFrozen(player)) return;

        Location from = event.getFrom();
        Location to   = event.getTo();

        // Allow head rotation but block actual position change
        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            // Preserve yaw/pitch so it doesn't feel jarring
            Location cancel = from.clone();
            cancel.setYaw(to.getYaw());
            cancel.setPitch(to.getPitch());
            event.setTo(cancel);
        }
    }

    /** Prevent frozen players from breaking blocks. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /** Prevent frozen players from placing blocks. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /** Prevent frozen players from interacting with anything. */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /** Prevent frozen players from dropping items. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /** Prevent frozen players from dealing damage. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player
                && freezeManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    /** Clean up freeze state when player disconnects. */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        freezeManager.unfreeze(event.getPlayer().getUniqueId());
    }
}
