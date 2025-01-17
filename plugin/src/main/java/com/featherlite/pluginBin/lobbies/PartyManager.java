package com.featherlite.pluginBin.lobbies;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PartyManager {
    private final Map<UUID, Party> activeParties = new HashMap<>();
    private final Map<UUID, UUID> pendingInvitations = new HashMap<>(); // Track pending invitations
    private InstanceManager instanceManager;

    // Provide a setter method to inject InstanceManager after both objects are initialized
    public void setInstanceManager(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    // Create a new party
    public Party createParty(Player leader, int maxSize) {
        Party party = new Party(leader, maxSize);
        activeParties.put(leader.getUniqueId(), party);
        return party;
    }

    // Get the party a player belongs to
    public Party getParty(Player player) {
        return activeParties.values().stream()
                .filter(party -> party.isMember(player))
                .findFirst()
                .orElse(null);
    }

    // Disband a party led by a specific player
    public void disbandParty(Player leader) {
        activeParties.remove(leader.getUniqueId());
    }

    // Check if a player is in any party
    public boolean isInParty(Player player) {
        return getParty(player) != null;
    }

    public boolean isPlayerPartyLeader(Player player) {
        Party party = getParty(player);
        return party != null && party.isLeader(player);
    }

    // Fetch all party leaders' names
    public List<String> getAllPartyLeaders() {
        return activeParties.values().stream()
                .map(party -> {
                    Player leader = Bukkit.getPlayer(party.getLeader());
                    return (leader != null) ? leader.getName() : null;
                })
                .filter(Objects::nonNull) // Exclude null values for offline or invalid players
                .collect(Collectors.toList());
    }


    // Command-related methods
    public void handleCreateParty(Player leader) {
        if (isInParty(leader)) {
            leader.sendMessage("You are already in a party.");
            return;
        }
        createParty(leader, 4); // Default max size
        leader.sendMessage("Party created with you as the leader.");
    }

    public void handleInvitePlayer(Player leader, String targetName) {
        if (!isInParty(leader)) {
            leader.sendMessage("You are not in a party.");
            return;
        }
        Party party = getParty(leader);
        if (!party.isLeader(leader)) {
            leader.sendMessage("You are not the party leader.");
            return;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            leader.sendMessage("Player not found.");
            return;
        }
        if (pendingInvitations.containsKey(target.getUniqueId())) {
            leader.sendMessage("This player already has a pending invitation.");
            return;
        }

        pendingInvitations.put(target.getUniqueId(), leader.getUniqueId());
        leader.sendMessage("Player invited to your party.");
        target.sendMessage("You have been invited to join " + leader.getName() + "'s party. Use /party accept or /party deny.");
    }

    public void handleAcceptInvite(Player player) {
        UUID inviterUUID = pendingInvitations.get(player.getUniqueId());
        if (inviterUUID == null) {
            player.sendMessage("You do not have any pending invitations.");
            return;
        }

        Party inviterParty = activeParties.get(inviterUUID);
        if (inviterParty == null) {
            player.sendMessage("The party you were invited to no longer exists.");
            pendingInvitations.remove(player.getUniqueId());
            return;
        }

        if (!inviterParty.addMember(player)) {
            player.sendMessage("The party is full.");
        } else {
            player.sendMessage("You have joined the party.");
            inviterParty.getMembers().forEach(memberUUID -> {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member != null) {
                    member.sendMessage(player.getName() + " has joined the party.");
                }
            });
        }
        pendingInvitations.remove(player.getUniqueId());
    }

    public void handleDenyInvite(Player player) {
        if (pendingInvitations.remove(player.getUniqueId()) != null) {
            player.sendMessage("You have denied the party invitation.");
        } else {
            player.sendMessage("You do not have any pending invitations.");
        }
    }

    public void handleLeaveParty(Player player) {
        if (!isInParty(player)) {
            player.sendMessage("You are not in a party.");
            return;
        }
    
        Party party = getParty(player);
        if (party.isLeader(player)) {
            disbandParty(player);
            player.sendMessage("You have disbanded the party.");
        } else {
            party.removeMember(player);
            player.sendMessage("You have left the party.");
        }
    
        // Also handle leaving any game instance the player is in
        if (instanceManager != null) { // Check that the reference is not null
            instanceManager.handlePlayerLeave(player);
        }
    }

    public void handleDisbandParty(Player leader) {
        if (!isInParty(leader)) {
            leader.sendMessage("You are not in a party.");
            return;
        }
        Party party = getParty(leader);
        if (!party.isLeader(leader)) {
            leader.sendMessage("You are not the party leader.");
            return;
        }
        disbandParty(leader);
        leader.sendMessage("Party disbanded.");
    }

    public void handleListPartyMembers(Player player) {
        if (!isInParty(player)) {
            player.sendMessage("You are not in a party.");
            return;
        }
        Party party = getParty(player);
        player.sendMessage("Party members:");
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                player.sendMessage("- " + member.getName());
            }
        }
    }

    

    // Method to suggest online player names for tab completion
    public List<String> suggestOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
