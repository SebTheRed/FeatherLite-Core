package com.featherlite.pluginBin.menus;

import net.md_5.bungee.api.ChatColor;
import java.util.HashMap;
import java.util.Map;


public class Menu {
    private final String id;
    private final int slots; // The number of slots in the inventory (must be a multiple of 9)
    private final Map<String, MenuPage> pages; // Map of page names to MenuPage objects

    public Menu(String id, int slots) {
        this.id = id;
        this.slots = slots;
        this.pages = new HashMap<>();
    }

    public String getID() {
        return id;
    }

    public int getSlots() {
        return slots;
    }

    public Map<String, MenuPage> getPages() {
        return pages;
    }

    // Add a page to the menu
    public void addPage(String pageName, MenuPage page) {
        pages.put(pageName, page);
    }

    // Retrieve a page by its name
    public MenuPage getPage(String pageName) {
        return pages.get(pageName);
    }

    // Check if a specific page exists
    public boolean hasPage(String pageName) {
        return pages.containsKey(pageName);
    }

    // Find a page by its normalized title
    public MenuPage getPageByTitle(String normalizedTitle) {
        for (MenuPage page : pages.values()) {
            String pageTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', page.getTitle()));
            if (pageTitle.equals(normalizedTitle)) {
                return page;
            }
        }
        return null;
    }

}
