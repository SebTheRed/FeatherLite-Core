package com.featherlite.pluginBin.lobbies;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStartEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final GameInstance instance;

    public GameStartEvent(GameInstance instance) {
        this.instance = instance;
    }

    public GameInstance getInstance() {
        return instance;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
