package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.webapp.WebAppManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class AppCommands {

    private final WebAppManager webAppManager;
    private final Map<UUID, String> activeSessions;

    public AppCommands(WebAppManager webAppManager, Map<UUID, String> activeSessions) {
        this.webAppManager = webAppManager;
        this.activeSessions = activeSessions;
    }

    public boolean handleAppCommands(CommandSender sender, String[] args, boolean isPlayer, JavaPlugin plugin) {

        Player player = (isPlayer ? (Player) sender : null);

        if (!isPlayer) {
            plugin.getLogger().warning("/app cannot be executed from the console! It must be executed from in-game.");
            return true;
        } else {
            player = (Player) sender;
        }
        // Permission check for "/app" and "/app save {sessionString}"
        if (player.isOp() || player.hasPermission("core.editor")) {
            // Handle "/app save {sessionString}" command to apply changes
            if (args.length == 2 && args[0].equalsIgnoreCase("save")) {
                String sessionID = args[1];
                return webAppManager.handleSaveCommand(player, sessionID, activeSessions);
            }

            // Handle "/app" command to generate a new session
            if (args.length == 0) {
                return webAppManager.handleGenerateSession(player, activeSessions);
            }
        }
        player.sendMessage("You do not have permission to perform this action.");
        return true;
    }
}
