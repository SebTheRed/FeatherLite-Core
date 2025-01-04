package com.featherlite.pluginBin.menus;

import java.util.HashMap;
import java.util.Map;

public class MenuPage {
    private String title; // Title of the page
    private final Map<Integer, MenuButton> items = new HashMap<>(); // Buttons in the page

    // Constructor
    public MenuPage(String title) {
        this.title = title;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addItem(int slot, MenuButton button) {
        if (slot < 0 || slot > 53) {
            throw new IllegalArgumentException("Slot must be between 0 and 53.");
        }
        items.put(slot, button);
    }

    public MenuButton getItem(int slot) {
        return items.get(slot);
    }

    public Map<Integer, MenuButton> getItems() {
        return items;
    }
}
