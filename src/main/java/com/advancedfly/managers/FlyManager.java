package com.advancedfly.managers;

import com.advancedfly.AdvancedFlyPlugin;
import com.advancedfly.hooks.VaultHook;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * FlyManager is the central hub for all flight-related logic.
 *
 * Responsibilities:
 *  - Enabling / disabling flight with full validation
 *  - Tracking which players are flying (in-memory)
 *  - Sending action-bar updates on a repeating task
 *  - Granting fall-damage immunity after landing
 */
public class FlyManager {

    private final AdvancedFlyPlugin plugin;
    private final ConfigManager     config;
    private final CooldownManager   cooldowns;
    private final SpeedDataManager  speedData;

    /** Set of UUIDs currently flying via this plugin. */
    private final Set<UUID>  flyingPlayers     = new HashSet<>();
    /** Players with active fall-damage protection (UUID → expiry time ms). */
    private final Map<UUID, Long> fallProtected = new HashMap<>();

    private BukkitTask actionBarTask;

    public FlyManager(AdvancedFlyPlugin plugin,
                      ConfigManager configManager,
                      CooldownManager cooldownManager,
                      SpeedDataManager speedDataManager) {
        this.plugin    = plugin;
        this.config    = configManager;
        this.cooldowns = cooldownManager;
        this.speedData = speedDataManager;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Toggle flight for a player.
     * Handles: world check, cooldown, economy charge, and state update.
     *
     * @param player   the target player
     * @param executor who issued the command (null = plugin/system call)
     * @param isAdmin  true if executor has advancedfly.admin
     */
    public void toggleFly(Player player, Player executor, boolean isAdmin) {
        // World restriction check (admin bypass)
        if (!isAdmin && isWorldDisabled(player)) {
            sendMsg(executor != null ? executor : player,
                    config.getMessage("world-disabled"));
            return;
        }

        // Cooldown check (only if toggling own fly)
        if (executor == null || executor.equals(player)) {
            long remaining = cooldowns.getRemaining(player.getUniqueId());
            if (remaining > 0 && !isAdmin) {
                sendMsg(player, config.getMessage("cooldown-active",
                        config.secondsTag(remaining)));
                return;
            }
        }

        // Economy charge
        if (config.isEconomyEnabled() && !isAdmin) {
            VaultHook vault = plugin.getVaultHook();
            if (vault != null && !vault.charge(player, config.getEconomyCost())) {
                sendMsg(player, config.getMessage("economy-insufficient",
                        config.amountTag(config.getEconomyCost())));
                return;
            } else if (vault != null) {
                sendMsg(player, config.getMessage("economy-charge",
                        config.amountTag(config.getEconomyCost())));
            }
        }

        boolean enabling = !flyingPlayers.contains(player.getUniqueId());
        setFly(player, enabling);

        // Notify the player
        if (enabling) {
            sendMsg(player, config.getMessage("fly-enabled"));
        } else {
            sendMsg(player, config.getMessage("fly-disabled"));
        }

        // Notify the executor if different (admin toggling another player)
        if (executor != null && !executor.equals(player)) {
            if (enabling) {
                sendMsg(executor, config.getMessage("fly-enabled-other",
                        config.playerTag(player.getName())));
            } else {
                sendMsg(executor, config.getMessage("fly-disabled-other",
                        config.playerTag(player.getName())));
            }
            sendMsg(player, config.getMessage("fly-toggled-by-admin"));
        }

        // Register cooldown after successful toggle
        if (executor == null || executor.equals(player)) {
            cooldowns.setCooldown(player.getUniqueId());
        }
    }

    /**
     * Directly enable or disable flight without checks (for event handling).
     */
    public void setFly(Player player, boolean enable) {
        if (enable) {
            player.setAllowFlight(true);
            player.setFlying(true);
            flyingPlayers.add(player.getUniqueId());

            // Apply saved/default fly speed
            float speed = speedData.getFlySpeed(player.getUniqueId());
            player.setFlySpeed(speed);
        } else {
            // Give fall protection before revoking flight
            grantFallProtection(player);
            player.setFlying(false);
            player.setAllowFlight(false);
            flyingPlayers.remove(player.getUniqueId());
        }
    }

    /** Returns true if this plugin considers the player to be flying. */
    public boolean isFlying(Player player) {
        return flyingPlayers.contains(player.getUniqueId());
    }

    /** Grants fall-damage immunity for the configured duration. */
    public void grantFallProtection(Player player) {
        long expiryMs = System.currentTimeMillis()
                + config.getFallProtectionDuration() * 1000L;
        fallProtected.put(player.getUniqueId(), expiryMs);
    }

    /** Returns true if the player still has active fall-damage protection. */
    public boolean hasFallProtection(Player player) {
        Long expiry = fallProtected.get(player.getUniqueId());
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            fallProtected.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    /** Removes all flight/state data for a player (e.g. on quit). */
    public void cleanupPlayer(UUID uuid) {
        flyingPlayers.remove(uuid);
        fallProtected.remove(uuid);
        cooldowns.clearCooldown(uuid);
    }

    /** Starts the repeating task that sends action-bar text to flying players. */
    public void startActionBarTask() {
        // Run every 20 ticks (1 second) — action bar fades after ~2 s so we refresh
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Component bar = config.getMessageNoPrefix("action-bar-flying");
            for (UUID uuid : flyingPlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.sendActionBar(bar);
                }
            }
        }, 0L, 20L);
    }

    /** Cancels the action-bar task (called on disable). */
    public void stopActionBarTask() {
        if (actionBarTask != null && !actionBarTask.isCancelled()) {
            actionBarTask.cancel();
        }
    }

    /** @return true if the player's current world bans flight. */
    public boolean isWorldDisabled(Player player) {
        return config.getDisabledWorlds()
                     .contains(player.getWorld().getName());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void sendMsg(Player player, Component msg) {
        player.sendMessage(msg);
    }
}
