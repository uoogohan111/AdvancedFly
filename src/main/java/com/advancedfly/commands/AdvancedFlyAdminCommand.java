package com.advancedfly.commands;

import com.advancedfly.AdvancedFlyPlugin;
import com.advancedfly.gui.FlyGuiMenu;
import com.advancedfly.managers.ConfigManager;
import com.advancedfly.managers.FlyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /advancedfly (alias: /afly)
 *
 * Sub-commands:
 *   reload  — reloads config.yml
 *   gui     — opens the fly GUI (player only)
 *   status  — prints fly status for the sender
 */
public class AdvancedFlyAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = List.of("reload", "gui", "status");

    private final AdvancedFlyPlugin plugin;
    private final FlyManager        flyManager;
    private final ConfigManager     config;

    public AdvancedFlyAdminCommand(AdvancedFlyPlugin plugin,
                                   FlyManager flyManager,
                                   ConfigManager config) {
        this.plugin     = plugin;
        this.flyManager = flyManager;
        this.config     = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!sender.hasPermission("advancedfly.admin")) {
            sender.sendMessage(config.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            // Default: show help
            sendHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase()) {

            case "reload" -> {
                config.reload();
                sender.sendMessage(config.getMessage("reload-success"));
                yield true;
            }

            case "gui" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(config.getMessage("player-only"));
                    yield true;
                }
                new FlyGuiMenu(plugin, flyManager, config).open(player);
                yield true;
            }

            case "status" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(config.getMessage("player-only"));
                    yield true;
                }
                boolean flying = flyManager.isFlying(player);
                // Reuse fly-enabled / fly-disabled messages as status indicator
                player.sendMessage(config.getMessage(
                        flying ? "fly-enabled" : "fly-disabled"));
                yield true;
            }

            default -> {
                sendHelp(sender);
                yield true;
            }
        };
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(net.kyori.adventure.text.Component.text(
                "§6AdvancedFly commands: §freload, gui, status"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 1) {
            return SUB_COMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
