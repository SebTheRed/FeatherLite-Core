package com.featherlite.pluginBin.placeholders;

import com.featherlite.pluginBin.lobbies.Party;
import com.featherlite.pluginBin.lobbies.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderParties {
    private static PartyManager partyManager;


    // Placeholder: <party_leader>
    public static String getPartyLeader(Player player) {
        Party party = getPartyForPlayer(player);
        if (party != null) {
            Player leader = Bukkit.getPlayer(party.getLeader());
            return leader != null ? leader.getName() : "Offline";
        }
        return "No Party";
    }

    // Placeholder: <party_size>
    public static String getPartySize(Player player) {
        Party party = getPartyForPlayer(player);
        return party != null ? String.valueOf(party.getMembers().size()) : "0";
    }

    // Placeholder: <party_max_size>
    public static String getPartyMaxSize(Player player) {
        Party party = getPartyForPlayer(player);
        return party != null ? String.valueOf(party.getMaxSize()) : "0";
    }

    // Placeholder: <party_members>
    public static String getPartyMembers(Player player) {
        Party party = getPartyForPlayer(player);
        if (party != null) {
            return party.getMembers().stream()
                    .map(uuid -> {
                        Player p = Bukkit.getPlayer(uuid);
                        return p != null ? p.getName() : "Offline";
                    })
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("No Members");
        }
        return "No Party";
    }

    // Utility: Get the Party for the player
    private static Party getPartyForPlayer(Player player) {
        if (partyManager == null) return null;
        return partyManager.getParty(player);
    }
}
