package com.featherlite.pluginBin.chat;

import com.featherlite.pluginBin.permissions.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatControlListener implements Listener {

    private final JavaPlugin plugin;
    private final ChatManager chatManager;
    private final PermissionManager permissionManager;
    private final Map<Player, Long> lastMessageTime = new HashMap<>();

    // Spam interval in ticks (10 ticks = 0.5 seconds)
    private static final long SPAM_INTERVAL_TICKS = 20;

    public ChatControlListener(JavaPlugin plugin, ChatManager chatManager, PermissionManager permissionManager) {
        this.plugin = plugin;
        this.chatManager = chatManager;
        this.permissionManager = permissionManager;
        // Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
    
        // Spam prevention
        if (!player.hasPermission("core.chat.spam") && !player.isOp()) {
            World world = player.getWorld();
            long currentTick = world.getGameTime();
            long lastTick = lastMessageTime.getOrDefault(player, -1L);
    
            if (lastTick != -1 && (currentTick - lastTick < SPAM_INTERVAL_TICKS)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Please avoid spamming the chat!");
                return;
            }
    
            lastMessageTime.put(player, currentTick);
        }
    
        // Retrieve the player's groups using PermissionManager
        List<String> playerGroups = permissionManager.getPlayerGroups(player);
        // plugin.getLogger().info("Player groups: " + playerGroups);
    
        // Determine the highest-weighted prefix
        String prefix = chatManager.getHighestWeightedPrefix(playerGroups);
        // plugin.getLogger().info("Selected prefix: " + prefix); # BRING THIS BACK W/ DEBUGGER
    
        // Use the player's display name
        String displayName = player.getDisplayName();
    
        // Determine chat message color
        ChatColor messageColor = player.hasPermission("core.chat.white") ? ChatColor.WHITE : ChatColor.GRAY;
    
        // Set the chat format
        event.setFormat(ChatColor.translateAlternateColorCodes('&', prefix) + " " + displayName + ": " + messageColor + message);
    
        // Banned words prevention
        if (!player.hasPermission("core.chat.badwords") && !player.isOp()) {
            List<String> bannedWords = chatManager.getBannedWords();
            for (String word : bannedWords) {
                if (message.toLowerCase().contains(word.toLowerCase())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Your message contains banned words!");
                    return;
                }
            }
        }
    }
    
}
