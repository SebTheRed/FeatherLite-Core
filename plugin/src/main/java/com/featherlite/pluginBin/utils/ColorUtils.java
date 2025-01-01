package com.featherlite.pluginBin.utils;

import org.bukkit.ChatColor;

public class ColorUtils {
   
    /**
     * Parses a string for custom color codes (e.g., "&a") and replaces them with ChatColor equivalents.
     *
     * @param input The string to parse.
     * @return The parsed string with Minecraft color codes applied.
     */
    public static String parseColors(String input) {
        if (input == null) return "";

        // Replace your custom symbol-based color codes with ChatColor equivalents
        String output = input
                .replace("&0", ChatColor.BLACK.toString())
                .replace("&1", ChatColor.DARK_BLUE.toString())
                .replace("&2", ChatColor.DARK_GREEN.toString())
                .replace("&3", ChatColor.DARK_AQUA.toString())
                .replace("&4", ChatColor.DARK_RED.toString())
                .replace("&5", ChatColor.DARK_PURPLE.toString())
                .replace("&6", ChatColor.GOLD.toString())
                .replace("&7", ChatColor.GRAY.toString())
                .replace("&8", ChatColor.DARK_GRAY.toString())
                .replace("&9", ChatColor.BLUE.toString())
                .replace("&a", ChatColor.GREEN.toString())
                .replace("&b", ChatColor.AQUA.toString())
                .replace("&c", ChatColor.RED.toString())
                .replace("&d", ChatColor.LIGHT_PURPLE.toString())
                .replace("&e", ChatColor.YELLOW.toString())
                .replace("&f", ChatColor.WHITE.toString())
                .replace("&l", ChatColor.BOLD.toString())
                .replace("&o", ChatColor.ITALIC.toString())
                .replace("&n", ChatColor.UNDERLINE.toString())
                .replace("&m", ChatColor.STRIKETHROUGH.toString())
                .replace("&k", ChatColor.MAGIC.toString())
                .replace("&r", ChatColor.RESET.toString())

                .replace("§0", ChatColor.BLACK.toString())
                .replace("§1", ChatColor.DARK_BLUE.toString())
                .replace("§2", ChatColor.DARK_GREEN.toString())
                .replace("§3", ChatColor.DARK_AQUA.toString())
                .replace("§4", ChatColor.DARK_RED.toString())
                .replace("§5", ChatColor.DARK_PURPLE.toString())
                .replace("§6", ChatColor.GOLD.toString())
                .replace("§7", ChatColor.GRAY.toString())
                .replace("§8", ChatColor.DARK_GRAY.toString())
                .replace("§9", ChatColor.BLUE.toString())
                .replace("§a", ChatColor.GREEN.toString())
                .replace("§b", ChatColor.AQUA.toString())
                .replace("§c", ChatColor.RED.toString())
                .replace("§d", ChatColor.LIGHT_PURPLE.toString())
                .replace("§e", ChatColor.YELLOW.toString())
                .replace("§f", ChatColor.WHITE.toString())
                .replace("§l", ChatColor.BOLD.toString())
                .replace("§o", ChatColor.ITALIC.toString())
                .replace("§n", ChatColor.UNDERLINE.toString())
                .replace("§m", ChatColor.STRIKETHROUGH.toString())
                .replace("§k", ChatColor.MAGIC.toString())
                .replace("§r", ChatColor.RESET.toString());

        return output;
    }
    

}
