package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.economy.EconomyManager;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EconomyCommands implements TabCompleter {
    private final EconomyManager economyManager;

    public EconomyCommands(EconomyManager economyManager, JavaPlugin plugin) {
        this.economyManager = economyManager;
    }

    public boolean handleEconomyCommands(String label, CommandSender sender, String[] args, boolean isPlayer) {
        if (isPlayer && label.equalsIgnoreCase("bal")) {
            return handleBalCommand(sender);
            
        }
        if (isPlayer && label.equalsIgnoreCase("baltop")) {
            return handleBaltopCommand(sender, args);
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco <bal | baltop | pay | give | take>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "bal":
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can enter the command /eco bal."); return true;}
                return handleBalCommand(sender);
            case "baltop":
                return handleBaltopCommand(sender, args);
            case "pay":
                return handlePayCommand(sender, args);
            case "give":
            case "take":
                return handleAdminEcoCommand(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /eco <bal|baltop|pay|give|take>");
                return true;
        }
    }

    // Handle /eco bal
    private boolean handleBalCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("core.eco.bal")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to check your balance.");
            return true;
        }

        double balance = economyManager.getBalance(player.getUniqueId());
        String prefix = economyManager.getCurrencyPrefix();
        player.sendMessage(ChatColor.GREEN + "Your balance: " + prefix + balance);
        return true;
    }

    // Handle /eco baltop
    private boolean handleBaltopCommand(CommandSender sender, String[] args) {
        int page = 1; // Default to the first page

        if (!sender.hasPermission("core.eco.baltop")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to view the balance leaderboard.");
            return true;
        }

        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid page number. Please enter a valid number.");
                return true;
            }
        }

        sendBaltop(sender, page);
        return true;
    }

    // Handle /eco pay
    private boolean handlePayCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!sender.hasPermission("core.eco.pay")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to pay other players.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco pay <playername> <amount>");
            return true;
        }

        Player payer = (Player) sender;
        UUID recipientUUID = resolvePlayerUUID(args[1]);

        if (recipientUUID == null) {
            payer.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (payer.getUniqueId().equals(recipientUUID)) {
            payer.sendMessage(ChatColor.RED + "You cannot pay yourself.");
            return true;
        }

        try {
            double amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                payer.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
                return true;
            }

            if (economyManager.getBalance(payer.getUniqueId()) < amount) {
                payer.sendMessage(ChatColor.RED + "You do not have enough funds.");
                return true;
            }

            economyManager.withdraw(payer.getUniqueId(), amount);
            economyManager.deposit(recipientUUID, amount);

            String prefix = economyManager.getCurrencyPrefix();
            payer.sendMessage(ChatColor.GREEN + "You paid " + prefix + amount + " to " + args[1] + ".");
            Player recipient = Bukkit.getPlayer(recipientUUID);
            if (recipient != null) {
                recipient.sendMessage(ChatColor.GREEN + "You received " + prefix + amount + " from " + payer.getName() + ".");
            }
            return true;

        } catch (NumberFormatException e) {
            payer.sendMessage(ChatColor.RED + "Invalid amount. Please enter a valid number.");
            return true;
        }
    }

    // Handle /eco give and /eco take
    private boolean handleAdminEcoCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco <give|take> <player> <amount>");
            return true;
        }

        UUID targetUUID = resolvePlayerUUID(args[1]);
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        try {
            double amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
                return true;
            }

            String prefix = economyManager.getCurrencyPrefix();

            switch (args[0].toLowerCase()) {
                case "give":
                    if (!sender.hasPermission("core.eco.give")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to give money.");
                        return true;
                    }
                    economyManager.deposit(targetUUID, amount);
                    sender.sendMessage(ChatColor.GREEN + "You gave " + prefix + amount + " to " + args[1] + ".");
                    break;

                case "take":
                    if (!sender.hasPermission("core.eco.take")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to take money.");
                        return true;
                    }
                    economyManager.withdraw(targetUUID, amount);
                    sender.sendMessage(ChatColor.GREEN + "You took " + prefix + amount + " from " + args[1] + ".");
                    break;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown admin command. Use /eco <give|take>.");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount. Please enter a valid number.");
        }

        return true;
    }

    private UUID resolvePlayerUUID(String playerName) {
        Player onlinePlayer = Bukkit.getPlayerExact(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }
        return null;
    }

    private void sendBaltop(CommandSender sender, int page) {
        List<Map.Entry<OfflinePlayer, Double>> leaderboard = economyManager.getTopBalances(economyManager.getDefaultCurrency());
        int entriesPerPage = 10;
        int totalPages = (int) Math.ceil((double) leaderboard.size() / entriesPerPage);

        if (page > totalPages) {
            sender.sendMessage(ChatColor.RED + "Page " + page + " does not exist. Total pages: " + totalPages);
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "Top Balances - Page " + page + "/" + totalPages);

        int startIndex = (page - 1) * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, leaderboard.size());
        String prefix = economyManager.getCurrencyPrefix();

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<OfflinePlayer, Double> entry = leaderboard.get(i);
            OfflinePlayer topPlayer = entry.getKey();
            double topBalance = entry.getValue();
            sender.sendMessage(ChatColor.YELLOW + "" + (i + 1) + ". " + topPlayer.getName() + ": " + prefix + topBalance);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("bal");
            suggestions.add("baltop");
            suggestions.add("pay");
            if (sender.hasPermission("economy.admin")) {
                suggestions.add("give");
                suggestions.add("take");
            }
            return suggestions;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("pay") || args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take"))) {
            List<String> playerNames = new ArrayList<>();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                playerNames.add(onlinePlayer.getName());
            }
            return playerNames;
        }

        if (args.length == 3) {
            List<String>suggestions = new ArrayList<>();
            String amount = "<amount>";
            // String currency = "<currency>";
            suggestions.add(amount);
            // suggestions.add(currency);
            return suggestions;
        }

        return null;
    }
}
