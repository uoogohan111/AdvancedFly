package com.advancedfly.hooks;

import com.advancedfly.AdvancedFlyPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * VaultHook wraps the Vault Economy API.
 *
 * Provides a single charge() method that deducts the configured cost
 * from a player's balance, returning false if funds are insufficient.
 */
public class VaultHook {

    private Economy economy;
    private boolean enabled = false;

    public VaultHook(AdvancedFlyPlugin plugin) {
        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
            enabled = true;
        }
    }

    /** @return true if an economy provider was found. */
    public boolean isEnabled() { return enabled; }

    /**
     * Attempts to charge the player the given amount.
     *
     * @return true  if the player had enough funds and was charged.
     *         false if the player cannot afford it.
     */
    public boolean charge(Player player, double amount) {
        if (!enabled || economy == null) return true; // no economy → free

        if (!economy.has(player, amount)) return false;

        economy.withdrawPlayer(player, amount);
        return true;
    }

    /** Returns the player's current balance (for informational display). */
    public double getBalance(Player player) {
        if (!enabled || economy == null) return 0.0;
        return economy.getBalance(player);
    }
}
