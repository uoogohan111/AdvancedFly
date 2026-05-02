package com.advancedfly.listeners;

import com.advancedfly.managers.ConfigManager;
import com.advancedfly.managers.FlyManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles fly-state restoration on login and cleanup on logout.
 */
public class PlayerJoinListener implements Listener {

    private final FlyManager    flyManager;
    private final ConfigManager config;

    public PlayerJoinListener(FlyManager flyManager, ConfigManager config) {
        this.flyManager = flyManager;
        this.config     = config;
    }

    /**
     * When a player joins, restore flight if:
     *  - They were previously flying (Bukkit persists allowFlight on the Player object), OR
     *  - They are in creative and auto-enable is on.
     *
     * Note: Bukkit already persists allowFlight across sessions via the playerdata NBT.
     * We sync our internal tracking set here.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!config.isRestoreFlyOnJoin()) return;

        // If player had flight allowed (saved in playerdata) and world is not disabled
        if (player.getAllowFlight() && !flyManager.isWorldDisabled(player)) {
            // Re-register with our manager so action-bar etc. work correctly
            flyManager.setFly(player, true);
            return;
        }

        // Auto-enable creative fly
        if (config.isAutoEnableOnCreative()
                && player.getGameMode() == GameMode.CREATIVE
                && !flyManager.isWorldDisabled(player)) {
            flyManager.setFly(player, true);
        }
    }

    /** Clean up all state when the player disconnects. */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        flyManager.cleanupPlayer(event.getPlayer().getUniqueId());
    }
}
