package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.zones.Zone;
import com.featherlite.pluginBin.zones.ZoneManager;

import io.papermc.paper.command.brigadier.Commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;

import org.bukkit.command.TabCompleter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ZonesCommands implements TabCompleter {

    private final ZoneManager zoneManager;
    private final JavaPlugin plugin;

    // Map to store ongoing zone creations, mapping player UUIDs to creation states
    private final Map<UUID, ZoneCreationState> creationStates = new HashMap<>();

    public ZonesCommands(ZoneManager zoneManager, JavaPlugin plugin) {
        this.zoneManager = zoneManager;
        this.plugin = plugin;
    }

    public boolean handleZoneCommands(CommandSender sender, String[] args, boolean isPlayer) {

        Player player = (isPlayer ? (Player) sender : null);

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (!isPlayer) {sender.sendMessage("You can only create zones in-game as a player!"); return true;}
                return handleCreateZone(player, args);
            case "pos1":
                if (!isPlayer) {sender.sendMessage("You can only create zones in-game as a player!"); return true;}
                return handleSetPos1(player);
            case "pos2":
                if (!isPlayer) {sender.sendMessage("You can only create zones in-game as a player!"); return true;}
                return handleSetPos2(player);
            case "delete":
                return handleDeleteZone(sender, args);
            case "list":
                return handleListZones(sender);
            case "info":
                return handleZoneInfo(sender, args);
            case "here":
                if (!isPlayer) {sender.sendMessage("Only players can check the zone they are inside."); return true;}
                Location loc = player.getLocation(); 
                Zone zoneAt = zoneManager.getZoneAtLocation(loc);
                handleZoneInfoWithZone(sender, zoneAt, args);
                return true;
            case "reload":
                return handleReloadZones(sender);
            case "rule":
                return handleSetRule(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Usage: /zone <create|delete|list|info|reload|pos1|pos2>");
                return true;
        }
    }

    private boolean handleCreateZone(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /zone create <name>");
            return true;
        }
        String zoneName = args[1];

        // Store the zone name in the player's creation state
        creationStates.put(player.getUniqueId(), new ZoneCreationState(zoneName));

        player.sendMessage(ChatColor.GREEN + "Zone creation started with name: " + ChatColor.YELLOW + zoneName);
        player.sendMessage(ChatColor.GREEN + "Step onto the first position and type " + ChatColor.YELLOW + "/zone pos1");
        return true;
    }

    private boolean handleSetPos1(Player player) {
        UUID playerId = player.getUniqueId();
        ZoneCreationState state = creationStates.get(playerId);

        if (state == null) {
            player.sendMessage(ChatColor.RED + "You have not started creating a zone. Use /zone create <name> to begin.");
            return true;
        }

        state.setPos1(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "First position set. Now, step onto the second position and type " + ChatColor.YELLOW + "/zone pos2");
        return true;
    }

    private boolean handleSetPos2(Player player) {
        UUID playerId = player.getUniqueId();
        ZoneCreationState state = creationStates.get(playerId);

        if (state == null || state.getPos1() == null) {
            player.sendMessage(ChatColor.RED + "Please set the first position with /zone pos1 before setting the second position.");
            return true;
        }

        // Set the second position and finalize zone creation
        state.setPos2(player.getLocation());

        // Create the zone
        Zone newZone = zoneManager.createZone(state.getZoneName(), state.getPos1(), state.getPos2(), "Welcome to " + state.getZoneName(), "Goodbye from " + state.getZoneName());
        if (newZone != null) {
            player.sendMessage(ChatColor.GREEN + "Zone " + ChatColor.YELLOW + state.getZoneName() + ChatColor.GREEN + " created successfully!");
            player.sendMessage(ChatColor.GREEN + "To set rules, use " + ChatColor.YELLOW + "/zone " + state.getZoneName() + " rule <rule-name> <value>");
            player.sendMessage(ChatColor.GREEN + "Or edit online at " + ChatColor.YELLOW + "/app");

            // Remove the creation state after the zone is created
            creationStates.remove(playerId);
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create zone " + state.getZoneName() + ".");
        }

        return true;
    }

    private boolean handleDeleteZone(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /zone delete <name>");
            return true;
        }
        String zoneName = args[1];
        boolean success = zoneManager.deleteZone(zoneName);
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Zone " + zoneName + " deleted successfully!");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to delete zone " + zoneName + ". It may not exist.");
        }
        return true;
    }

    private boolean handleListZones(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Zones:");
        for (String zoneName : zoneManager.getZones().keySet()) {
            sender.sendMessage(ChatColor.YELLOW + "- " + zoneName);
        }
        return true;
    }

    private boolean handleZoneInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /zone info <name>");
            return true;
        }
        String zoneName = args[1];
        Zone zone = zoneManager.getZone(zoneName);
        if (zone != null) {
            sender.sendMessage(ChatColor.GOLD + "Zone Info: " + zoneName);
            sender.sendMessage(ChatColor.YELLOW + "Description: " + zone.getDescription());
            sender.sendMessage(ChatColor.YELLOW + "World: " + zone.getWorld());
            sender.sendMessage(ChatColor.YELLOW + "Entry Message: " + zone.getEntryMessage());
            sender.sendMessage(ChatColor.YELLOW + "Exit Message: " + zone.getExitMessage());
            sender.sendMessage(ChatColor.WHITE + "Debug: " + zone.getDebugInfo());
        } else {
            sender.sendMessage(ChatColor.RED + "Zone " + zoneName + " does not exist.");
        }
        return true;
    }


    private boolean handleZoneInfoWithZone(CommandSender sender, Zone zone, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /zone here");
            return true;
        }
        if (zone != null) {
            sender.sendMessage(ChatColor.GOLD + "Zone Info: " + zone.getName());
            sender.sendMessage(ChatColor.YELLOW + "Description: " + zone.getDescription());
            sender.sendMessage(ChatColor.YELLOW + "World: " + zone.getWorld());
            sender.sendMessage(ChatColor.YELLOW + "Entry Message: " + zone.getEntryMessage());
            sender.sendMessage(ChatColor.YELLOW + "Exit Message: " + zone.getExitMessage());
            sender.sendMessage(ChatColor.WHITE + "Debug: " + zone.getDebugInfo());
        } else {
            sender.sendMessage(ChatColor.RED + "You are not inside of a zone!");
        }

        return true;
    }


    private boolean handleReloadZones(CommandSender sender) {
        zoneManager.reloadAllZones();
        sender.sendMessage(ChatColor.GREEN + "All zones reloaded successfully!");
        return true;
    }



    private boolean handleSetRule(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /zone rule <name> <rule_name> <rule_value>");
            return true;
        }
    
        String zoneName = args[1];
        String rule = args[2].toLowerCase();
        String value = args[3];
    
        Zone zone = zoneManager.getZone(zoneName);
        if (zone == null) {
            sender.sendMessage(ChatColor.RED + "Zone " + zoneName + " does not exist.");
            return true;
        }
    
        // Call appropriate setter based on rule name
        boolean success = setZoneRule(zone, rule, value);
    
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Rule " + rule + " set to " + value + " for zone " + zoneName);
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to set rule " + rule + ". Please check the rule name and value.");
        }
    
        return true;
    }




    private boolean setZoneRule(Zone zone, String rule, String value) {
        try {
            switch (rule.toLowerCase()) {
                // Natural Rules (Booleans)
                case "lavaignite":
                    zone.setLavaIgnite(Boolean.parseBoolean(value));
                    break;
                case "lightningstrike":
                    zone.setLightningStrike(Boolean.parseBoolean(value));
                    break;
                case "waterflow":
                    zone.setWaterFlow(Boolean.parseBoolean(value));
                    break;
                case "lavaflow":
                    zone.setLavaFlow(Boolean.parseBoolean(value));
                    break;
                case "snowfall":
                    zone.setSnowFall(Boolean.parseBoolean(value));
                    break;
                case "snowmelt":
                    zone.setSnowMelt(Boolean.parseBoolean(value));
                    break;
                case "icemelt":
                    zone.setIceMelt(Boolean.parseBoolean(value));
                    break;
                case "mushroomgrowth":
                    zone.setMushroomGrowth(Boolean.parseBoolean(value));
                    break;
                case "leafdecay":
                    zone.setLeafDecay(Boolean.parseBoolean(value));
                    break;
                case "grassgrowth":
                    zone.setGrassGrowth(Boolean.parseBoolean(value));
                    break;
                case "myceliumspread":
                    zone.setMyceliumSpread(Boolean.parseBoolean(value));
                    break;
                case "vinegrowth":
                    zone.setVineGrowth(Boolean.parseBoolean(value));
                    break;
                case "rockgrowth":
                    zone.setRockGrowth(Boolean.parseBoolean(value));
                    break;
                case "sculkgrowth":
                    zone.setSculkGrowth(Boolean.parseBoolean(value));
                    break;
                case "cropgrowth":
                    zone.setCropGrowth(Boolean.parseBoolean(value));
                    break;
                case "soildry":
                    zone.setSoilDry(Boolean.parseBoolean(value));
                    break;
    
                // Mob Rules (Booleans and Lists)
                case "creeperexplosion":
                    zone.setCreeperExplosion(Boolean.parseBoolean(value));
                    break;
                case "enderdragonbreak":
                    zone.setEnderDragonBreak(Boolean.parseBoolean(value));
                    break;
                case "ghastfireballbreak":
                    zone.setGhastFireballBreak(Boolean.parseBoolean(value));
                    break;
                case "endermanbreak":
                    zone.setEndermanBreak(Boolean.parseBoolean(value));
                    break;
                case "snowmantrails":
                    zone.setSnowmanTrails(Boolean.parseBoolean(value));
                    break;
                case "ravagerbreak":
                    zone.setRavagerBreak(Boolean.parseBoolean(value));
                    break;
                case "mobdamage":
                    zone.setMobDamage(Boolean.parseBoolean(value));
                    break;
                case "mobdestroypaintings":
                    zone.setMobDestroyPaintings(Boolean.parseBoolean(value));
                    break;
                case "mobdestroyitemframes":
                    zone.setMobDestroyItemFrames(Boolean.parseBoolean(value));
                    break;
                case "witherbreak":
                    zone.setWitherBreak(Boolean.parseBoolean(value));
                    break;
                case "allowcustommobs":
                    zone.setIsCustomMobsAllowed(Boolean.parseBoolean(value));
                    break;
                case "hostilemobspawnlist":
                    zone.setHostileMobSpawnList(parseList(value));
                    break;
                case "passivemobspawnlist":
                    zone.setPassiveMobSpawnList(parseList(value));
                    break;
    
                // Protection Rules (Booleans and Lists)
                case "firespread":
                    zone.setFireSpread(Boolean.parseBoolean(value));
                    break;
                case "interact":
                    zone.setInteract(Boolean.parseBoolean(value));
                    break;
                case "mount":
                    zone.setMount(Boolean.parseBoolean(value));
                    break;
                case "chestaccess":
                    zone.setChestAccess(Boolean.parseBoolean(value));
                    break;
                case "pvp":
                    zone.setPvp(Boolean.parseBoolean(value));
                    break;
                case "sleep":
                    zone.setSleep(Boolean.parseBoolean(value));
                    break;
                case "respawnanchors":
                    zone.setRespawnAnchors(Boolean.parseBoolean(value));
                    break;
                case "tnt":
                    zone.setTnt(Boolean.parseBoolean(value));
                    break;
                case "vehicleplace":
                    zone.setVehiclePlace(Boolean.parseBoolean(value));
                    break;
                case "vehicledestroy":
                    zone.setVehicleDestroy(Boolean.parseBoolean(value));
                    break;
                case "ignite":
                    zone.setIgnite(Boolean.parseBoolean(value));
                    break;
                case "trampling":
                    zone.setTrampling(Boolean.parseBoolean(value));
                    break;
                case "frostwalker":
                    zone.setFrostWalker(Boolean.parseBoolean(value));
                    break;
                case "itemframerotation":
                    zone.setItemFrameRotation(Boolean.parseBoolean(value));
                    break;
                case "stationinteract":
                    zone.setIsStationInteract(Boolean.parseBoolean(value));
                    break;
                case "buildlist":
                    zone.setBuildList(parseList(value));
                    break;
                case "breaklist":
                    zone.setBreakList(parseList(value));
                    break;
                case "explosionproofblocks":
                    zone.setExplosionProofBlocks(parseList(value));
                    break;
    
                // Player Rules (Booleans, Strings, and Lists)
                case "entrymessage":
                    zone.setEntryMessage(value);
                    break;
                case "exitmessage":
                    zone.setExitMessage(value);
                    break;
                case "entrylist":
                    zone.setEntryList(parseList(value));
                    break;
                case "exitlist":
                    zone.setExitList(parseList(value));
                    break;
                case "enderpearl":
                    zone.setEnderpearl(Boolean.parseBoolean(value));
                    break;
                case "chorusfruit":
                    zone.setChorusFruit(Boolean.parseBoolean(value));
                    break;
    
                // Map Rules (Booleans and Lists)
                case "itempickup":
                    zone.setItemPickup(Boolean.parseBoolean(value));
                    break;
                case "itemdrop":
                    zone.setItemDrop(Boolean.parseBoolean(value));
                    break;
                case "expdrop":
                    zone.setExpDrop(Boolean.parseBoolean(value));
                    break;
                case "falldamage":
                    zone.setFallDamage(Boolean.parseBoolean(value));
                    break;
                case "weatherlock":
                    zone.setWeatherLock(Boolean.parseBoolean(value));
                    break;
                case "naturalhealthregen":
                    zone.setNaturalHealthRegen(Boolean.parseBoolean(value));
                    break;
                case "naturalhungerdrain":
                    zone.setNaturalHungerDrain(Boolean.parseBoolean(value));
                    break;
                case "blockedcommands":
                    zone.setBlockedCommands(parseList(value));
                    break;
    
                // Information (String fields)
                case "description":
                    zone.setDescription(value);
                    break;
                case "world":
                    zone.setWorld(value);
                    break;
    
                default:
                    return false; // Rule not recognized
            }
            
            // Save changes to config
            zone.saveConfig();
            return true;
    
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Setting failed
        }
    }



    private Set<String> parseList(String value) {
        Set<String> result = new HashSet<>();
        if (value.equalsIgnoreCase("ALL")) {
            result.add("ALL");
        } else if (!value.equalsIgnoreCase("NONE")) {
            for (String item : value.split(",")) {
                result.add(item.trim().toUpperCase());
            }
        }
        return result;
    }


    // Inner class to keep track of zone creation states for each player
    private static class ZoneCreationState {
        private final String zoneName;
        private Location pos1;
        private Location pos2;

        public ZoneCreationState(String zoneName) {this.zoneName = zoneName;}
        public String getZoneName() {return zoneName;}

        public Location getPos1() {return pos1;}
        public void setPos1(Location pos1) {this.pos1 = pos1;}

        public Location getPos2() {return pos2;}
        public void setPos2(Location pos2) {this.pos2 = pos2;}
    }




        @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggest primary subcommands
            return Arrays.asList("create", "delete", "list", "info", "reload", "pos1", "pos2", "rule");
        }

        if (args[0].equalsIgnoreCase("rule")) {
            if (args.length == 2) {
                // Suggest existing zone names
                return zoneManager.getZones().keySet().stream().collect(Collectors.toList());
            } else if (args.length == 3) {
                // Suggest rule names
                return Arrays.asList(
                    // Natural Rules
                    "lavaignite", "lightningstrike", "waterflow", "lavaflow", "snowfall", "snowmelt",
                    "icemelt", "mushroomgrowth", "leafdecay", "grassgrowth", "myceliumspread", "vinegrowth",
                    "rockgrowth", "sculkgrowth", "cropgrowth", "soildry",
                    
                    // Mob Rules
                    "creeperexplosion", "enderdragonbreak", "ghastfireballbreak", "endermanbreak", "snowmantrails",
                    "ravagerbreak", "mobdamage", "mobdestroypaintings", "mobdestroyitemframes", "witherbreak",
                    "allowcustommobs", "hostilemobspawnlist", "passivemobspawnlist",
                    
                    // Protection Rules
                    "firespread", "interact", "mount", "chestaccess", "pvp", "sleep", "respawnanchors", "tnt",
                    "vehicleplace", "vehicledestroy", "ignite", "trampling", "frostwalker", "itemframerotation",
                    "stationinteract", "buildlist", "breaklist", "explosionproofblocks",
                    
                    // Player Rules
                    "entrymessage", "exitmessage", "entrylist", "exitlist", "enderpearl", "chorusfruit",
                    
                    // Map Rules
                    "itempickup", "itemdrop", "expdrop", "falldamage", "weatherlock", "naturalhealthregen",
                    "naturalhungerdrain", "blockedcommands",
                    
                    // Information fields
                    "description", "world"
                );
            } else if (args.length == 4) {
                // Suggest common values
                return Arrays.asList("true", "false", "ALL", "NONE", "COMMA,SEPERATED,LIST");
            }
        }

        return Collections.emptyList(); // No suggestion
    }

}
