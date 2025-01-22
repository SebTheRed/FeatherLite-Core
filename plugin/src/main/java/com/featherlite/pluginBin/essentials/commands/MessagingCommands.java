package com.featherlite.pluginBin.essentials.commands;

import com.featherlite.pluginBin.essentials.messaging.MessagingManager;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MessagingCommands implements TabCompleter {
    private final MessagingManager messagingManager;

    public MessagingCommands(MessagingManager messagingManager) {
        this.messagingManager = messagingManager;
    }

    public boolean handleMessagingCommands(CommandSender sender, Command command, String label, String[] args, boolean isPlayer) {

        if (label.toLowerCase() == "broadcast") {
            if (isPlayer && !(sender.hasPermission("core.broadcast") || sender.isOp())) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                return true;
            }
            handleBroadcast(sender, args);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use messaging commands.");
            return true;
        }

        Player player = (Player) sender;

        if (isPlayer && !(sender.hasPermission("core.message") || sender.isOp())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to message players.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "msg":
                return handleMsg(player, args);
            case "r":
                return handleReply(player, args);
            case "msgtoggle":
                return handleMsgToggle(player);
            case "ignore":
                return handleIgnore(player, args);
            default:
                player.sendMessage(ChatColor.RED + "Unknown command.");
                return true;
        }
    }

    private boolean handleMsg(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (messagingManager.isMessageToggled(target) || messagingManager.getIgnoredPlayers(target).contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "This player is not accepting private messages.");
            return true;
        }

        String message = String.join(" ", args).substring(args[0].length()).trim();
        target.sendMessage(ChatColor.LIGHT_PURPLE + "[PM] " + player.getName() + ": " + ChatColor.WHITE + message);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "[PM to " + target.getName() + "] " + ChatColor.WHITE + message);

        messagingManager.setLastMessaged(player, target);
        return true;
    }

    private boolean handleReply(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /r <message>");
            return true;
        }

        UUID lastMessaged = messagingManager.getLastMessaged(player);
        if (lastMessaged == null) {
            player.sendMessage(ChatColor.RED + "No one has messaged you recently.");
            return true;
        }

        Player target = Bukkit.getPlayer(lastMessaged);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "The last player to message you is no longer online.");
            return true;
        }

        return handleMsg(player, new String[]{target.getName(), String.join(" ", args)});
    }

    private boolean handleBroadcast(CommandSender sender, String[] args) {
        if ((sender instanceof Player) && !(sender.hasPermission("core.messaging.broadcast") || sender.isOp())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to broadcast messages.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /broadcast <message>");
            return true;
        }

        String message = String.join(" ", args);
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Broadcast] " + ChatColor.YELLOW + message);
        return true;
    }

    private boolean handleMsgToggle(Player player) {
        messagingManager.toggleMessaging(player);
        player.sendMessage(ChatColor.GREEN + "Private messaging " + (messagingManager.isMessageToggled(player) ? "disabled." : "enabled."));
        return true;
    }

    private boolean handleIgnore(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /ignore <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        messagingManager.toggleIgnore(player, target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + (messagingManager.getIgnoredPlayers(player).contains(target.getUniqueId())
                ? "You are now ignoring " + target.getName() + "."
                : "You are no longer ignoring " + target.getName() + "."));
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
    
        Player player = (Player) sender;
    
        // Check permissions before providing suggestions
        // if (alias.equalsIgnoreCase("broadcast") && !(player.hasPermission("core.broadcast") || player.isOp())) {
        //     return Collections.emptyList(); // No suggestions if the player lacks permission
        // }
    
        // if ((alias.equalsIgnoreCase("msg") || alias.equalsIgnoreCase("ignore")) && !(player.hasPermission("core.message") || player.isOp())) {
        //     return Collections.emptyList(); // No suggestions for messaging-related commands if permission is missing
        // }
    
        List<String> suggestions = new ArrayList<>();
        switch (alias.toLowerCase()) {
            case "msg":
            case "ignore":
                if (args.length == 1) {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        suggestions.add(onlinePlayer.getName());
                    }
                }
                break;
            case "broadcast":
                if (args.length == 1) {
                    suggestions.add("<message>");
                }
                break;
            case "r":
            case "msgtoggle":
                // No suggestions for /r or /msgtoggle
                break;
        }
        return suggestions;
    }
    

}
