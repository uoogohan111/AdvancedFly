package com.advancedfly;

import com.advancedfly.commands.*;
import com.advancedfly.hooks.PlaceholderHook;
import com.advancedfly.hooks.VaultHook;
import com.advancedfly.listeners.*;
import com.advancedfly.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AdvancedFly — Main plugin entry point.
 * Uses Paper 1.21+ API with the Adventure Component system throughout.
 */
public final class AdvancedFlyPlugin extends JavaPlugin {

    private static AdvancedFlyPlugin instance;

    private ConfigManager    configManager;
    private FlyManager       flyManager;
    private CooldownManager  cooldownManager;
    private SpeedDataManager speedDataManager;
    private FreezeManager    freezeManager;

    private VaultHook vaultHook;
    private boolean   placeholderApiPresent = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager    = new ConfigManager(this);
        cooldownManager  = new CooldownManager(configManager);
        speedDataManager = new SpeedDataManager(this);
        flyManager       = new FlyManager(this, configManager, cooldownManager, speedDataManager);
        freezeManager    = new FreezeManager();

        hookVault();
        hookPlaceholderAPI();
        registerCommands();
        registerListeners();

        if (configManager.isActionBarEnabled()) {
            flyManager.startActionBarTask();
        }

        getLogger().info("AdvancedFly v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (speedDataManager != null) speedDataManager.saveAll();
        getLogger().info("AdvancedFly disabled.");
    }

    private void hookVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            vaultHook = new VaultHook(this);
            if (vaultHook.isEnabled()) {
                getLogger().info("Vault hooked — economy features active.");
            } else {
                vaultHook = null;
                getLogger().warning("Vault found but no economy provider detected.");
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
        var freezeCmd    = new FreezeCommand(freezeManager, configManager);

        getCommand("fly").setExecutor(flyCmd);
        getCommand("fly").setTabCompleter(flyCmd);
        getCommand("flyspeed").setExecutor(flySpeedCmd);
        getCommand("walkspeed").setExecutor(walkSpeedCmd);
        getCommand("freeze").setExecutor(freezeCmd);
        getCommand("freeze").setTabCompleter(freezeCmd);
        getCommand("advancedfly").setExecutor(adminCmd);
        getCommand("advancedfly").setTabCompleter(adminCmd);
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoinListener(flyManager, configManager),       this);
        pm.registerEvents(new PlayerGameModeListener(flyManager, configManager),   this);
        pm.registerEvents(new PlayerMoveListener(flyManager, configManager),       this);
        pm.registerEvents(new PlayerFallProtectionListener(flyManager),            this);
        pm.registerEvents(new FreezeListener(freezeManager),                       this);
    }

    public static AdvancedFlyPlugin getInstance() { return instance; }
    public ConfigManager    getConfigManager()    { return configManager; }
    public FlyManager       getFlyManager()       { return flyManager; }
    public CooldownManager  getCooldownManager()  { return cooldownManager; }
    public SpeedDataManager getSpeedDataManager() { return speedDataManager; }
    public FreezeManager    getFreezeManager()    { return freezeManager; }
    public VaultHook        getVaultHook()        { return vaultHook; }
    public boolean          isPlaceholderApiPresent() { return placeholderApiPresent; }
}
