package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.permissions.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PermissionsCommands implements TabCompleter {
    private final PermissionManager permissionManager;

    public PermissionsCommands(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public boolean handlePermissionsCommands(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Usage: /permissions <reload|addgroup|removegroup|setperm|me>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "showall":
                if (player.hasPermission("feathercore.permissions.showall")) {
                    permissionManager.showAllServerPermissions(player);
                }
            case "reload":
                if (player.hasPermission("feathercore.permissions.reload")) {
                    permissionManager.reloadConfig();
                    player.sendMessage("Permissions configuration reloaded.");
                } else {
                    player.sendMessage("You do not have permission to reload the configuration.");
                }
                break;

            case "addgroup":
                if (args.length < 3) {
                    player.sendMessage("Usage: /permissions addgroup <playerName> <groupName>");
                    return true;
                }
                if (player.hasPermission("feathercore.permissions.manage.groups")) {
                    permissionManager.addPlayerToGroup(args[1], args[2], player);
                } else {
                    player.sendMessage("You do not have permission to manage groups.");
                }
                break;

            case "removegroup":
                if (args.length < 3) {
                    player.sendMessage("Usage: /permissions removegroup <playerName> <groupName>");
                    return true;
                }
                if (player.hasPermission("feathercore.permissions.manage.groups")) {
                    permissionManager.removePlayerFromGroup(args[1], args[2], player);
                } else {
                    player.sendMessage("You do not have permission to manage groups.");
                }
                break;

            case "setperm":
                if (args.length < 4) {
                    player.sendMessage("Usage: /permissions setperm <playerName> <permission> <true|false>");
                    return true;
                }
                if (player.hasPermission("feathercore.permissions.manage.permissions")) {
                    boolean value = Boolean.parseBoolean(args[3]);
                    permissionManager.setPlayerPermission(args[1], args[2], value, player);
                } else {
                    player.sendMessage("You do not have permission to manage individual permissions.");
                }
                break;

            case "my":
                if (player.hasPermission("feathercore.permissions.view")) {
                    if (args[1].equalsIgnoreCase("groups") || args[1].equalsIgnoreCase("group")) {
                        List<String> activeGroups = permissionManager.getPlayerGroups(player);
                        player.sendMessage("Your active groups:");
                        for (String group : activeGroups) {
                            player.sendMessage("- " + group);
                        }
                    } 
                    if (args[1].equalsIgnoreCase("permissions") || args[1].equalsIgnoreCase("perms")) {
                        List<String> activePermissions = permissionManager.getPlayerEffectivePermissions(player);
                        player.sendMessage("Your active permissions:");
                        for (String perm : activePermissions) {
                            player.sendMessage("- " + perm);
                        }
                    }

                } else {
                    player.sendMessage("You do not have permission to view your active permissions.");
                }
                break;

            default:
                player.sendMessage("Unknown command. Use /perms <reload | addgroup | removegroup | setperm | my>");
        }
        return true;
    }

    // Tab completion logic
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        Player player = (Player) sender;

        if (args.length == 1) {
            return Arrays.asList("reload", "addgroup", "removegroup", "setperm", "my");
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "addgroup":
                case "removegroup":
                case "setperm":
                    List<String> playerNames = new ArrayList<>();
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        playerNames.add(onlinePlayer.getName());
                    }
                    return playerNames;
                case "my":
                    return Arrays.asList("permissions", "groups");
            }
        }

        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "addgroup":
                case "removegroup":
                    return new ArrayList<>(permissionManager.getAvailableGroups());
                case "setperm":
                    return Arrays.asList("example.permission", "other.permission");
            }
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("setperm")) {
            return Arrays.asList("true", "false");
        }

        return Collections.emptyList();
    }
}
