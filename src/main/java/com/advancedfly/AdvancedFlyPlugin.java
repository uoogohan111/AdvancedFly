package com.advancedfly;

import com.advancedfly.commands.*;
import com.advancedfly.hooks.PlaceholderHook;
import com.advancedfly.hooks.VaultHook;
import com.advancedfly.listeners.*;
import com.advancedfly.managers.ConfigManager;
import com.advancedfly.managers.CooldownManager;
import com.advancedfly.managers.FlyManager;
import com.advancedfly.managers.SpeedDataManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AdvancedFly — Main plugin entry point.
 *
 * Bootstraps all managers, commands, and listeners in the correct order.
 * Uses Paper 1.21+ API with the Adventure Component system throughout.
 */
public final class AdvancedFlyPlugin extends JavaPlugin {

    // ── Singleton access ─────────────────────────────────────────────────────
    private static AdvancedFlyPlugin instance;

    // ── Managers ──────────────────────────────────────────────────────────────
    private ConfigManager   configManager;
    private FlyManager      flyManager;
    private CooldownManager cooldownManager;
    private SpeedDataManager speedDataManager;

    // ── Optional hooks ────────────────────────────────────────────────────────
    private VaultHook       vaultHook;
    private boolean         placeholderApiPresent = false;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        instance = this;

        // 1. Save & load default config
        saveDefaultConfig();

        // 2. Initialise managers
        configManager   = new ConfigManager(this);
        cooldownManager = new CooldownManager(configManager);
        speedDataManager = new SpeedDataManager(this);
        flyManager      = new FlyManager(this, configManager, cooldownManager, speedDataManager);

        // 3. Register optional third-party hooks
        hookVault();
        hookPlaceholderAPI();

        // 4. Register commands
        registerCommands();

        // 5. Register event listeners
        registerListeners();

        // 6. Start repeating action-bar task
        if (configManager.isActionBarEnabled()) {
            flyManager.startActionBarTask();
        }

        getLogger().info("AdvancedFly v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        // Persist any unsaved fly-speed data
        if (speedDataManager != null) {
            speedDataManager.saveAll();
        }
        getLogger().info("AdvancedFly disabled.");
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private void hookVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            vaultHook = new VaultHook(this);
            if (vaultHook.isEnabled()) {
                getLogger().info("Vault hooked — economy features active.");
            } else {
                vaultHook = null;
                getLogger().warning("Vault found but no economy provider detected. Economy features disabled.");
            }
        }
    }

    private void hookPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this, flyManager).register();
            placeholderApiPresent = true;
            getLogger().info("PlaceholderAPI hooked — %advancedfly_status% available.");
        }
    }

    private void registerCommands() {
        var flyCmd       = new FlyCommand(this, flyManager, configManager);
        var flySpeedCmd  = new FlySpeedCommand(this, configManager);
        var walkSpeedCmd = new WalkSpeedCommand(this, configManager);
        var adminCmd     = new AdvancedFlyAdminCommand(this, flyManager, configManager);

        getCommand("fly").setExecutor(flyCmd);
        getCommand("fly").setTabCompleter(flyCmd);

        getCommand("flyspeed").setExecutor(flySpeedCmd);
        getCommand("walkspeed").setExecutor(walkSpeedCmd);

        getCommand("advancedfly").setExecutor(adminCmd);
        getCommand("advancedfly").setTabCompleter(adminCmd);
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoinListener(flyManager, configManager),          this);
        pm.registerEvents(new PlayerGameModeListener(flyManager, configManager),      this);
        pm.registerEvents(new PlayerMoveListener(flyManager, configManager),          this);
        pm.registerEvents(new PlayerFallProtectionListener(flyManager),               this);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public static AdvancedFlyPlugin getInstance() { return instance; }

    public ConfigManager    getConfigManager()   { return configManager; }
    public FlyManager       getFlyManager()      { return flyManager; }
    public CooldownManager  getCooldownManager() { return cooldownManager; }
    public SpeedDataManager getSpeedDataManager(){ return speedDataManager; }

    /** @return Vault hook, or null if Vault is not present / no economy provider */
    public VaultHook getVaultHook() { return vaultHook; }

    public boolean isPlaceholderApiPresent() { return placeholderApiPresent; }
}
