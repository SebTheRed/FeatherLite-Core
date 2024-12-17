package com.featherlite.pluginBin.lobbies;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party {
    private UUID leader;
    private List<UUID> members;
    private int maxSize;

    public Party(Player leader, int maxSize) {
        this.leader = leader.getUniqueId();
        this.members = new ArrayList<>();
        this.members.add(leader.getUniqueId());
        this.maxSize = maxSize;
    }

    public UUID getLeader() {
        return leader;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public boolean addMember(Player player) {
        if (members.size() >= maxSize) return false;
        return members.add(player.getUniqueId());
    }

    public boolean removeMember(Player player) {
        return members.remove(player.getUniqueId());
    }

    public boolean isMember(Player player) {
        return members.contains(player.getUniqueId());
    }

    public boolean isLeader(Player player) {
        return player.getUniqueId().equals(leader);
    }

    public void disband() {
        members.clear();
    }

    public int getMaxSize() {
        return maxSize;
    }
}
