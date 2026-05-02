package com.advancedfly.commands;

import com.advancedfly.managers.ConfigManager;
import com.advancedfly.managers.SpeedDataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles /walkspeed <1-10|reset>.
 * /walkspeed reset restores Bukkit's default walk speed (0.2f = roughly speed 2).
 */
public class WalkSpeedCommand implements CommandExecutor {

    private static final float DEFAULT_WALK_SPEED = 0.2f;

    private final ConfigManager config;

    public WalkSpeedCommand(com.advancedfly.AdvancedFlyPlugin plugin,
                            ConfigManager config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("advancedfly.walkspeed")
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
            player.setWalkSpeed(DEFAULT_WALK_SPEED);
            player.sendMessage(config.getMessage("walkspeed-reset",
                    config.speedTag(SpeedDataManager.toUserSpeed(DEFAULT_WALK_SPEED))));
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

        float bukkit = SpeedDataManager.toBukkitSpeed(speed);
        player.setWalkSpeed(bukkit);

        player.sendMessage(config.getMessage("walkspeed-set", config.speedTag(speed)));
        return true;
    }
}
