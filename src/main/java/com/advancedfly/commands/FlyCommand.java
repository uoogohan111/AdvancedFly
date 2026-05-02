package com.advancedfly.commands;

import com.advancedfly.AdvancedFlyPlugin;
import com.advancedfly.managers.ConfigManager;
import com.advancedfly.managers.FlyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /fly [player] command.
 *
 *  /fly           → toggle own flight
 *  /fly <player>  → toggle another player's flight (requires advancedfly.fly.others)
 */
public class FlyCommand implements CommandExecutor, TabCompleter {

    private final AdvancedFlyPlugin plugin;
    private final FlyManager        flyManager;
    private final ConfigManager     config;

    public FlyCommand(AdvancedFlyPlugin plugin,
                      FlyManager flyManager,
                      ConfigManager config) {
        this.plugin     = plugin;
        this.flyManager = flyManager;
        this.config     = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        // Only players can fly
        if (!(sender instanceof Player executor)) {
            sender.sendMessage(config.getMessage("player-only"));
            return true;
        }

        boolean isAdmin = executor.hasPermission("advancedfly.admin");

        // /fly <otherPlayer>
        if (args.length >= 1) {
            if (!isAdmin && !executor.hasPermission("advancedfly.fly.others")) {
                executor.sendMessage(config.getMessage("no-permission"));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                executor.sendMessage(config.getMessage("player-not-found",
                        config.playerTag(args[0])));
                return true;
            }

            flyManager.toggleFly(target, executor, isAdmin);
            return true;
        }

        // /fly (self)
        if (!isAdmin && !executor.hasPermission("advancedfly.fly")) {
            executor.sendMessage(config.getMessage("no-permission"));
            return true;
        }

        flyManager.toggleFly(executor, null, isAdmin);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 1
                && (sender.hasPermission("advancedfly.fly.others")
                    || sender.hasPermission("advancedfly.admin"))) {
            // Suggest online player names filtered by partial input
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase()
                            .startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
