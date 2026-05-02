package com.advancedfly.managers;

import com.advancedfly.AdvancedFlyPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * SpeedDataManager persists per-player fly speed to
 * plugins/AdvancedFly/speeds.yml so preferences survive restarts.
 *
 * Values are stored in the 1–10 user scale and converted to the
 * Bukkit 0.1–1.0 scale where needed.
 */
public class SpeedDataManager {

    private final AdvancedFlyPlugin        plugin;
    private final File                     dataFile;
    private       YamlConfiguration        data;

    /** In-memory cache: UUID → Bukkit fly speed (0.1–1.0). */
    private final Map<UUID, Float>         speedCache = new HashMap<>();

    public SpeedDataManager(AdvancedFlyPlugin plugin) {
        this.plugin   = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "speeds.yml");
        load();
    }

    // ── Load / Save ────────────────────────────────────────────────────────────

    private void load() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        data = YamlConfiguration.loadConfiguration(dataFile);

        // Populate in-memory cache
        for (String key : data.getKeys(false)) {
            try {
                UUID  uuid  = UUID.fromString(key);
                float speed = (float) data.getDouble(key, 0.5);
                speedCache.put(uuid, speed);
            } catch (IllegalArgumentException ignored) {
                // Corrupted key — skip
            }
        }
    }

    /** Writes all cached speeds to disk. */
    public void saveAll() {
        for (Map.Entry<UUID, Float> entry : speedCache.entrySet()) {
            data.set(entry.getKey().toString(), (double) entry.getValue());
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save speeds.yml", e);
        }
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Returns the stored Bukkit fly speed for a player,
     * or the config default if none is saved.
     */
    public float getFlySpeed(UUID uuid) {
        return speedCache.getOrDefault(uuid,
                toBukkitSpeed(plugin.getConfigManager().getDefaultFlySpeed()));
    }

    /**
     * Saves and returns a Bukkit speed after validating the user-scale value.
     *
     * @param uuid       player UUID
     * @param userSpeed  1–10 scale value
     */
    public float setFlySpeed(UUID uuid, int userSpeed) {
        float bukkit = toBukkitSpeed(userSpeed);
        speedCache.put(uuid, bukkit);
        // Async save to avoid I/O on main thread
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveAll);
        return bukkit;
    }

    // ── Conversion helpers ─────────────────────────────────────────────────────

    /**
     * Converts a 1–10 user-scale speed to Bukkit's 0.1–1.0 range.
     * Formula: bukkit = userSpeed / 10.0
     */
    public static float toBukkitSpeed(int userSpeed) {
        int clamped = Math.max(1, Math.min(10, userSpeed));
        return clamped / 10.0f;
    }

    /**
     * Converts Bukkit 0.1–1.0 scale back to 1–10 for display.
     */
    public static int toUserSpeed(float bukkitSpeed) {
        return Math.round(bukkitSpeed * 10);
    }
}
