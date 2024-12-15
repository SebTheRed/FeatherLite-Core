package com.featherlite.pluginBin.utils;

import org.bukkit.ChatColor;

public class ColorUtils {

    /**
     * Translates Bukkit-style color codes (&a, &b, etc.) to Minecraft color codes (§a, §b, etc.).
     *
     * @param text The text to translate.
     * @return The translated text with Minecraft color codes.
     */
    public static String translateColors(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
