package com.advancedfly.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * CooldownManager tracks when each player last toggled flight
 * and enforces the configurable cooldown period.
 */
public class CooldownManager {

    private final ConfigManager             config;
    /** Maps player UUID → System.currentTimeMillis() of last toggle. */
    private final Map<UUID, Long>           lastToggle = new HashMap<>();

    public CooldownManager(ConfigManager config) {
        this.config = config;
    }

    /**
     * Returns remaining cooldown in seconds (0 if none active).
     */
    public long getRemaining(UUID uuid) {
        if (config.getFlyCooldown() <= 0) return 0;

        Long last = lastToggle.get(uuid);
        if (last == null) return 0;

        long elapsed  = (System.currentTimeMillis() - last) / 1000;
        long remaining = config.getFlyCooldown() - elapsed;
        return Math.max(0, remaining);
    }

    /** Records a toggle event for the given player right now. */
    public void setCooldown(UUID uuid) {
        lastToggle.put(uuid, System.currentTimeMillis());
    }

    /** Removes cooldown data for a player (e.g. on quit). */
    public void clearCooldown(UUID uuid) {
        lastToggle.remove(uuid);
    }
}
