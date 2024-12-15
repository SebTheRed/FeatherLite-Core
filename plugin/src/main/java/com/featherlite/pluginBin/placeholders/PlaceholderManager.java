package com.featherlite.pluginBin.placeholders;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PlaceholderManager {

    private static PlaceholderManager instance;
    private final Map<String, Function<Player, String>> placeholderResolvers = new HashMap<>();

    public PlaceholderManager() {
        // Register default placeholders
        registerPlaceholder("online_player_count", PlaceholderMethods::getOnlinePlayerCount);
        registerPlaceholder("max_player_count", PlaceholderMethods::getMaxPlayerCount);
        registerPlaceholder("player_name", PlaceholderMethods::getPlayerName);
        registerPlaceholder("player_health", PlaceholderMethods::getPlayerHealth);
        registerPlaceholder("player_max_health", PlaceholderMethods::getPlayerMaxHealth);
        registerPlaceholder("player_xp_level", PlaceholderMethods::getPlayerXpLevel);
        registerPlaceholder("world_name", PlaceholderMethods::getWorldName);
        registerPlaceholder("world_time", PlaceholderMethods::getWorldTime);
        registerPlaceholder("loaded_chunks", PlaceholderMethods::getLoadedChunks);
        registerPlaceholder("entity_count", PlaceholderMethods::getEntityCount);
        registerPlaceholder("server_tps", PlaceholderMethods::getServerTps);

        // registerPlaceholder("server_uptime", PlaceholderMethods::getServerUptime);
        registerPlaceholder("player_x", PlaceholderMethods::getPlayerX);
        registerPlaceholder("player_y", PlaceholderMethods::getPlayerY);
        registerPlaceholder("player_z", PlaceholderMethods::getPlayerZ);
        registerPlaceholder("current_date", PlaceholderMethods::getCurrentDate);
        registerPlaceholder("current_time", PlaceholderMethods::getCurrentTime);
        registerPlaceholder("player_ping", PlaceholderMethods::getPlayerPing);
        registerPlaceholder("current_biome", PlaceholderMethods::getCurrentBiome);
        registerPlaceholder("player_hunger", PlaceholderMethods::getPlayerHunger);

        // Zones
        registerPlaceholder("zone_name", PlaceholderZones::getZoneName);  // name of zone player is in.
        registerPlaceholder("zone_description", PlaceholderZones::getZoneDescription);
        registerPlaceholder("zone_world", PlaceholderZones::getZoneWorld);
        registerPlaceholder("zone_entry_message", PlaceholderZones::getZoneEntryMessage);
        registerPlaceholder("zone_exit_message", PlaceholderZones::getZoneExitMessage);
        registerPlaceholder("zone_is_game_zone", PlaceholderZones::isGameZone);

        // Parties
        registerPlaceholder("party_leader", PlaceholderParties::getPartyLeader);
        registerPlaceholder("party_size", PlaceholderParties::getPartySize);
        registerPlaceholder("party_size_max", PlaceholderParties::getPartyMaxSize);
        registerPlaceholder("party_members", PlaceholderParties::getPartyMembers);

        // Lobbies
        registerPlaceholder("game_state", PlaceholderLobbies::getGameState);
        registerPlaceholder("game_type", PlaceholderLobbies::getGameState);
        registerPlaceholder("game_player_count", PlaceholderLobbies::getGameState);
        registerPlaceholder("game_minimum_players", PlaceholderLobbies::getGameState);
        registerPlaceholder("game_max_players", PlaceholderLobbies::getGameState);
        registerPlaceholder("game_world", PlaceholderLobbies::getGameState);

        // Worlds
        registerPlaceholder("world_name", PlaceholderWorlds::getCurrentWorldName);
        registerPlaceholder("world_environment", PlaceholderWorlds::getCurrentWorldEnvironment);
        registerPlaceholder("world_players", PlaceholderWorlds::getPlayersInWorld);
        registerPlaceholder("worlds_loaded", PlaceholderWorlds::getLoadedWorlds);

        // Economy
        registerPlaceholder("player_balance", PlaceholderEconomy::getCurrentBalance);
        registerPlaceholder("top_balance", PlaceholderEconomy::getTopBalance);
    }

    public static PlaceholderManager getInstance() {
        if (instance == null) {
            instance = new PlaceholderManager();
        }
        return instance;
    }

    /**
     * Registers a placeholder with its resolver.
     * 
     * @param key       The placeholder key (e.g., "online_player_count").
     * @param resolver  A function that resolves the value of the placeholder.
     */
    public void registerPlaceholder(String key, Function<Player, String> resolver) {
        placeholderResolvers.put(key, resolver);
    }

    /**
     * Resolves a placeholder in a string.
     * 
     * @param input  The string containing placeholders (e.g., "Online: <online_player_count>").
     * @param player The player context for resolving player-specific placeholders.
     * @return The string with placeholders replaced by their values.
     */
    public String resolvePlaceholders(String input, Player player) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Parse and replace placeholders
        StringBuilder resolved = new StringBuilder();
        int startIndex = 0;

        while (startIndex < input.length()) {
            int openCarat = input.indexOf('<', startIndex);
            if (openCarat == -1) {
                // No more placeholders
                resolved.append(input.substring(startIndex));
                break;
            }

            int closeCarat = input.indexOf('>', openCarat);
            if (closeCarat == -1) {
                // Malformed placeholder
                resolved.append(input.substring(startIndex));
                break;
            }

            // Append text before the placeholder
            resolved.append(input.substring(startIndex, openCarat));

            // Resolve placeholder
            String placeholderKey = input.substring(openCarat + 1, closeCarat);
            String resolvedValue = placeholderResolvers.getOrDefault(placeholderKey, (p) -> "<unknown>")
                                                       .apply(player);

            resolved.append(resolvedValue);
            startIndex = closeCarat + 1;
        }

        return resolved.toString();
    }

}
