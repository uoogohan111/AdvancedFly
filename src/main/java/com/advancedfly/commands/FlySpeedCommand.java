package com.advancedfly.commands;

import com.advancedfly.AdvancedFlyPlugin;
import com.advancedfly.managers.ConfigManager;
import com.advancedfly.managers.SpeedDataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles /flyspeed <1-10|reset>.
 * Saves the speed per-player and applies it immediately if they are flying.
 * /flyspeed reset restores the config default.
 */
public class FlySpeedCommand implements CommandExecutor {

    private final AdvancedFlyPlugin plugin;
    private final ConfigManager     config;

    public FlySpeedCommand(AdvancedFlyPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("advancedfly.flyspeed")
                && !player.hasPermission("advancedfly.admin")) {
            player.sendMessage(config.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(config.getMessage("invalid-speed"));
            return true;
        }

        // Handle reset
        if (args[0].equalsIgnoreCase("reset")) {
            int defaultSpeed = config.getDefaultFlySpeed();
            SpeedDataManager speedData = plugin.getSpeedDataManager();
            float bukkit = speedData.setFlySpeed(player.getUniqueId(), defaultSpeed);
            player.setFlySpeed(bukkit);
            player.sendMessage(config.getMessage("flyspeed-reset", config.speedTag(defaultSpeed)));
            return true;
        }

        int speed;
        try {
            speed = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(config.getMessage("invalid-speed"));
            return true;
        }

        if (speed < 1 || speed > 10) {
            player.sendMessage(config.getMessage("invalid-speed"));
            return true;
        }

        // Save and apply
        SpeedDataManager speedData = plugin.getSpeedDataManager();
        float bukkit = speedData.setFlySpeed(player.getUniqueId(), speed);
        player.setFlySpeed(bukkit);

        player.sendMessage(config.getMessage("flyspeed-set", config.speedTag(speed)));
        return true;
    }
}
