package com.advancedfly.hooks;

import com.advancedfly.AdvancedFlyPlugin;
import com.advancedfly.managers.FlyManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registers the %advancedfly_status% placeholder with PlaceholderAPI.
 *
 * Returns "flying" or "grounded" depending on the player's current state.
 */
public class PlaceholderHook extends PlaceholderExpansion {

    private final AdvancedFlyPlugin plugin;
    private final FlyManager        flyManager;

    public PlaceholderHook(AdvancedFlyPlugin plugin, FlyManager flyManager) {
        this.plugin     = plugin;
        this.flyManager = flyManager;
    }

    @Override public @NotNull String getIdentifier() { return "advancedfly"; }
    @Override public @NotNull String getAuthor()     { return "AdvancedFly"; }
    @Override public @NotNull String getVersion()    { return plugin.getDescription().getVersion(); }

    /** Persist so PAPI doesn't remove us on reload. */
    @Override public boolean persist() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        // %advancedfly_status%
        if (params.equalsIgnoreCase("status")) {
            return flyManager.isFlying(player) ? "flying" : "grounded";
        }

        return null; // Unknown placeholder — let PAPI handle
    }
}
