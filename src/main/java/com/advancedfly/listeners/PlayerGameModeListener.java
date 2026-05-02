package com.advancedfly.listeners;

import com.advancedfly.managers.ConfigManager;
import com.advancedfly.managers.FlyManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

/**
 * Listens for game-mode changes and adjusts flight accordingly:
 *
 *  • Survival  → auto-disable flight (if configured)
 *  • Creative  → auto-enable  flight (if configured)
 *  • Adventure → auto-disable flight (same as survival)
 */
public class PlayerGameModeListener implements Listener {

    private final FlyManager    flyManager;
    private final ConfigManager config;

    public PlayerGameModeListener(FlyManager flyManager, ConfigManager config) {
        this.flyManager = flyManager;
        this.config     = config;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player   player  = event.getPlayer();
        GameMode newMode = event.getNewGameMode();

        switch (newMode) {
            case SURVIVAL, ADVENTURE -> {
                if (config.isAutoDisableOnSurvival() && flyManager.isFlying(player)) {
                    flyManager.setFly(player, false);
                }
            }
            case CREATIVE -> {
                if (config.isAutoEnableOnCreative()
                        && !flyManager.isWorldDisabled(player)) {
                    flyManager.setFly(player, true);
                }
            }
            default -> { /* SPECTATOR — leave Bukkit in control */ }
        }
    }
}
