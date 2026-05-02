package com.advancedfly.commands;

import com.advancedfly.managers.ConfigManager;
import com.advancedfly.managers.FreezeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles /freeze [player].
 *
 *  /freeze          → freeze/unfreeze yourself
 *  /freeze <player> → freeze/unfreeze another player (requires advancedfly.freeze.others)
 */
public class FreezeCommand implements CommandExecutor, TabCompleter {

    private final FreezeManager freezeManager;
    private final ConfigManager config;

    public FreezeCommand(FreezeManager freezeManager, ConfigManager config) {
        this.freezeManager = freezeManager;
        this.config        = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player executor)) {
            sender.sendMessage(config.getMessage("player-only"));
            return true;
        }

        boolean isAdmin = executor.hasPermission("advancedfly.admin");

        // /freeze <otherPlayer>
        if (args.length >= 1) {
            if (!isAdmin && !executor.hasPermission("advancedfly.freeze.others")) {
                executor.sendMessage(config.getMessage("no-permission"));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                executor.sendMessage(config.getMessage("player-not-found",
                        config.playerTag(args[0])));
                return true;
            }

            boolean nowFrozen = freezeManager.toggleFreeze(target);

            // Notify target
            target.sendMessage(config.getMessage(
                    nowFrozen ? "freeze-enabled-self" : "freeze-disabled-self"));

            // Notify executor
            executor.sendMessage(config.getMessage(
                    nowFrozen ? "freeze-enabled-other" : "freeze-disabled-other",
                    config.playerTag(target.getName())));
            return true;
        }

        // /freeze (self)
        if (!isAdmin && !executor.hasPermission("advancedfly.freeze")) {
            executor.sendMessage(config.getMessage("no-permission"));
            return true;
        }

        boolean nowFrozen = freezeManager.toggleFreeze(executor);
        executor.sendMessage(config.getMessage(
                nowFrozen ? "freeze-enabled-self" : "freeze-disabled-self"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 1
                && (sender.hasPermission("advancedfly.freeze.others")
                    || sender.hasPermission("advancedfly.admin"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase()
                            .startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
