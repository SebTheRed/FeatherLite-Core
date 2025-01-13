package com.featherlite.pluginBin.zones;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Zone {
    private String name;
    private String description;

    private FileConfiguration config;
    private File configFile;
    private String world; // New field for the world

    private boolean isGameZone;
    private boolean gameOverrideSpawnPoint;
    
    // Coordinates
    private Location spawnPoint;
    private Location teleportPoint;
    private Location cornerOne;
    private Location cornerTwo;

    // Natural rules
    private boolean lavaIgnite, lightningStrike, waterFlow, lavaFlow, snowFall, snowMelt,
            iceMelt, mushroomGrowth, leafDecay, grassGrowth, myceliumSpread, vineGrowth, rockGrowth,
            sculkGrowth, cropGrowth, soilDry;

    // Mob rules
    private boolean creeperExplosion, enderDragonBreak, ghastFireballBreak, endermanBreak, snowmanTrails,
            ravagerBreak, mobDamage, mobDestroyPaintings, mobDestroyItemFrames, witherBreak;
    private Set<String> hostileMobSpawnList = new HashSet<>();
    private Set<String> passiveMobSpawnList = new HashSet<>();
    private boolean allowCustomMobs;

    // Protection rules
    private boolean fireSpread, interact, mount, chestAccess,
            pvp, sleep, respawnAnchors, tnt, vehiclePlace, vehicleDestroy, ignite, trampling,
            frostWalker, itemFrameRotation, stationInteract;
    private Set<String> buildList = new HashSet<>();
    private Set<String> breakList = new HashSet<>();
    private Set<String> explosionProofBlocks = new HashSet<>();

    // Player rules
    private String entryMessage, exitMessage;
    private Set<String> entryList = new HashSet<>();
    private Set<String> exitList = new HashSet<>();
    private boolean enderpearl, chorusFruit;

    // Map rules
    private boolean itemPickup, itemDrop, expDrop, fallDamage, weatherLock, naturalHealthRegen, naturalHungerDrain;
    private Set<String> blockedCommands = new HashSet<>();

    public Zone(String name, File configFile) {

        this.configFile = configFile;
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.name = name;
        this.world = config.getString("information.world"); // Load world from config

        setupZoneFromConfig();
    }


    public void setupZoneFromConfig() {

        this.description = config.getString("information.description");
        this.isGameZone = config.getBoolean("information.is-game-zone", false);
        this.gameOverrideSpawnPoint = config.getBoolean("information.game-override-spawn-point", false);

        // Parse coordinates
        this.spawnPoint = parseLocation(config, "coordinates.spawn-point");
        this.teleportPoint = parseLocation(config, "coordinates.teleport-point");
        this.cornerOne = parseLocation(config, "coordinates.corner-one");
        this.cornerTwo = parseLocation(config, "coordinates.corner-two");

        // Natural rules
        this.lavaIgnite = config.getBoolean("natrual-rules.lava-ignite", false);
        this.lightningStrike = config.getBoolean("natrual-rules.lightning-strike", false);
        this.waterFlow = config.getBoolean("natrual-rules.water-flow", false);
        this.lavaFlow = config.getBoolean("natrual-rules.lava-flow", false);
        this.snowFall = config.getBoolean("natrual-rules.snow-fall", false);
        this.snowMelt = config.getBoolean("natrual-rules.snow-melt", false);
        this.iceMelt = config.getBoolean("natrual-rules.ice-melt", false);
        this.mushroomGrowth = config.getBoolean("natrual-rules.mushroom-growth", false);
        this.leafDecay = config.getBoolean("natrual-rules.leaf-decay", false);
        this.grassGrowth = config.getBoolean("natrual-rules.grass-growth", false);
        this.myceliumSpread = config.getBoolean("natrual-rules.mycelium-spread", false);
        this.vineGrowth = config.getBoolean("natrual-rules.vine-growth", false);
        this.rockGrowth = config.getBoolean("natrual-rules.rock-growth", false);
        this.sculkGrowth = config.getBoolean("natrual-rules.sculk-growth", false);
        this.cropGrowth = config.getBoolean("natrual-rules.crop-growth", false);
        this.soilDry = config.getBoolean("natrual-rules.soil-dry", false);

        // Mob rules
        this.creeperExplosion = config.getBoolean("mob-rules.creeper-explosion", false);
        this.enderDragonBreak = config.getBoolean("mob-rules.enderdragon-break", false);
        this.ghastFireballBreak = config.getBoolean("mob-rules.ghast-fireball-break", false);
        this.endermanBreak = config.getBoolean("mob-rules.enderman-break", false);
        this.snowmanTrails = config.getBoolean("mob-rules.snowman-trails", false);
        this.ravagerBreak = config.getBoolean("mob-rules.ravager-break", false);
        this.mobDamage = config.getBoolean("mob-rules.mob-damage", false);
        this.mobDestroyPaintings = config.getBoolean("mob-rules.mob-destroy-paintings", true);
        this.mobDestroyItemFrames = config.getBoolean("mob-rules.mob-destroy-item-frames", true);
        this.witherBreak = config.getBoolean("mob-rules.wither-break", false);
        loadList(config, "mob-rules.hostile-mob-spawn-list", hostileMobSpawnList);
        loadList(config, "mob-rules.passive-mob-spawn-list", passiveMobSpawnList);

        // Protection rules
        this.fireSpread = config.getBoolean("protection-rules.fire-spread", false);
        this.interact = config.getBoolean("protection-rules.interact", true);
        this.mount = config.getBoolean("protection-rules.mount", false);
        // this.damagePassiveMobs = config.getBoolean("protection-rules.damage-passive-mobs", true);
        // this.damageHostileMobs = config.getBoolean("protection-rules.damage-hostile-mobs", true);
        this.chestAccess = config.getBoolean("protection-rules.chest-access", true);
        this.pvp = config.getBoolean("protection-rules.pvp", true);
        this.sleep = config.getBoolean("protection-rules.sleep", false);
        this.respawnAnchors = config.getBoolean("protection-rules.respawn-anchors", false);
        this.tnt = config.getBoolean("protection-rules.tnt", true);
        this.vehiclePlace = config.getBoolean("protection-rules.vehicle-place", false);
        this.vehicleDestroy = config.getBoolean("protection-rules.vehicle-destroy", false);
        this.ignite = config.getBoolean("protection-rules.ignite", false);
        this.trampling = config.getBoolean("protection-rules.trampling", false);
        this.frostWalker = config.getBoolean("protection-rules.frost-walker", false);
        this.itemFrameRotation = config.getBoolean("protection-rules.item-frame-rotation", false);
        this.stationInteract = config.getBoolean("protection-rules.station-interact", false);
        this.allowCustomMobs = config.getBoolean("mob-rules.allow-custom-mobs", true);
        loadList(config, "protection-rules.build-list", buildList);
        loadList(config, "protection-rules.break-list", breakList);
        loadList(config, "protection-rules.explosion-proof-blocks", explosionProofBlocks);

        // Player rules
        this.entryMessage = config.getString("player-rules.entry-message", "Welcome to " + name);
        this.exitMessage = config.getString("player-rules.exit-message", "Goodbye");
        loadList(config, "player-rules.entry-list", entryList);
        loadList(config, "player-rules.exit-list", exitList);
        this.enderpearl = config.getBoolean("player-rules.enderpearl", false);
        this.chorusFruit = config.getBoolean("player-rules.chorus-fruit", false);

        // Map rules
        this.itemPickup = config.getBoolean("map-rules.item-pickup", true);
        this.itemDrop = config.getBoolean("map-rules.item-drop", true);
        this.expDrop = config.getBoolean("map-rules.exp-drop", false);
        this.fallDamage = config.getBoolean("map-rules.fall-damage", false);
        this.weatherLock = config.getBoolean("map-rules.weather-lock", false);
        this.naturalHealthRegen = config.getBoolean("map-rules.natural-health-regen", true);
        this.naturalHungerDrain = config.getBoolean("map-rules.natural-hunger-drain", false);
        loadList(config, "map-rules.blocked-commands", blockedCommands);
    }



    private Location parseLocation(FileConfiguration config, String path) {
        if (!config.isConfigurationSection(path)) return null;
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        return new Location(org.bukkit.Bukkit.getWorld(world), x, y, z);
    }

    private void loadList(FileConfiguration config, String path, Set<String> list) {
        List<String> configList = config.getStringList(path);
        if (configList.contains("ALL") || configList.contains("ANY")) {
            list.clear();
            list.add("ALL");
        } else if (configList.contains("NONE")) {
            list.clear();
        } else {
            list.addAll(configList);
        }
    }

    // Utility methods for setting values in the configuration
    private void setConfigBoolean(String path, boolean value) {
        config.set(path, value);
        saveConfig();
    }
    
    public void setConfigString(String path, String value) {
        config.set(path, value);
        saveConfig();
    }
    
    public void saveConfig() {
        try {
            config.save(new File(Bukkit.getPluginManager().getPlugin("FeatherLite").getDataFolder(), "zones.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // List methods
    // Mob spawning rules check
    public boolean canSpawnHostileMob(String mobType) {
        if (hostileMobSpawnList.contains("NONE")) return false;
        return hostileMobSpawnList.contains("ANY") || hostileMobSpawnList.contains(mobType) || hostileMobSpawnList.contains("ALL");
    }
    // Setter for hostileMobSpawnList
    public void setHostileMobSpawnList(Set<String> hostileMobSpawnList) {
        this.hostileMobSpawnList.clear();
        this.hostileMobSpawnList.addAll(hostileMobSpawnList);
        config.set("mob-rules.hostile-mob-spawn-list", new ArrayList<>(hostileMobSpawnList));
        saveConfig();
    }
    

    public boolean canSpawnPassiveMob(String mobType) {
        if (passiveMobSpawnList.contains("NONE")) return false;
        return passiveMobSpawnList.contains("ANY") || passiveMobSpawnList.contains(mobType) || passiveMobSpawnList.contains("ALL");
    }
    // Setter for passiveMobSpawnList
    public void setPassiveMobSpawnList(Set<String> passiveMobSpawnList) {
        this.passiveMobSpawnList.clear();
        this.passiveMobSpawnList.addAll(passiveMobSpawnList);
        config.set("mob-rules.passive-mob-spawn-list", new ArrayList<>(passiveMobSpawnList));
        saveConfig();
    }
    


    // Build permissions check
    public boolean canBuild(String material) {
        if (buildList.contains("NONE")) {
            return false;
        }
        return buildList.contains("ALL") || buildList.contains(material) || buildList.contains("ANY") ;
    }
    // Setter for buildList
    public void setBuildList(Set<String> buildList) {
        this.buildList.clear();
        this.buildList.addAll(buildList);
        config.set("protection-rules.build-list", new ArrayList<>(buildList));
        saveConfig();
    }


    // Break permissions check
    public boolean canBreak(String material) {
        if (breakList.contains("NONE")) {
            return false;
        }
        return breakList.contains("ALL") || breakList.contains(material) || breakList.contains("ANY");
    }
    // Setter for breakList
    public void setBreakList(Set<String> breakList) {
        this.breakList.clear();
        this.breakList.addAll(breakList);
        config.set("protection-rules.break-list", new ArrayList<>(breakList));
        saveConfig();
    }
    
    
    

    // Explosion-proof blocks check
    public boolean isExplosionProof(String material) {
        if (explosionProofBlocks.contains("ALL") || explosionProofBlocks.contains("ANY")) {
            return true;
        }
        return explosionProofBlocks.contains(material);
    }
    // Setter for explosionProofBlocks
    public void setExplosionProofBlocks(Set<String> explosionProofBlocks) {
        this.explosionProofBlocks.clear();
        this.explosionProofBlocks.addAll(explosionProofBlocks);
        config.set("protection-rules.explosion-proof-blocks", new ArrayList<>(explosionProofBlocks));
        saveConfig();
    }

    // Command blocking check
    public boolean isCommandBlocked(String command) {
        if (blockedCommands.contains("NONE")) {
            return false;
        }
        return blockedCommands.contains("ALL") || blockedCommands.contains(command) || blockedCommands.contains("ANY");
    }
    // Setter for blockedCommands
    public void setBlockedCommands(Set<String> blockedCommands) {
        this.blockedCommands.clear();
        this.blockedCommands.addAll(blockedCommands);
        config.set("map-rules.blocked-commands", new ArrayList<>(blockedCommands));
        saveConfig();
    }

    // Entry permission check
    public boolean canEnter(String playerName) {
        if (entryList.contains("NONE")) {
            return false;
        }
        return entryList.contains("ALL") || entryList.contains(playerName) || entryList.contains("ANY");
    }
    // Setter for entryList
    public void setEntryList(Set<String> entryList) {
        this.entryList.clear();
        this.entryList.addAll(entryList);
        config.set("player-rules.entry-list", new ArrayList<>(entryList));
        saveConfig();
    }




    // Exit permission check
    public boolean canExit(String playerName) {
        if (exitList.contains("NONE")) {
            return false;
        }
        return exitList.contains("ALL") || exitList.contains(playerName) || exitList.contains("ANY");
    }
    // Setter for exitList
    public void setExitList(Set<String> exitList) {
        this.exitList.clear();
        this.exitList.addAll(exitList);
        config.set("player-rules.exit-list", new ArrayList<>(exitList));
        saveConfig();
    }


    
    // Define getters and utility methods for each rule...
    public String getName() {
        return name;
    }
    
    // Getter for world
    public String getWorld() {
        return world;
    }

    // Setter for world (if needed in runtime)
    public void setWorld(String world) {
        this.world = world;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String desc) {
        setConfigString("information.description", desc);
    }
    
    public boolean isGameZone() {
        return isGameZone;
    }
    
    public boolean isGameOverrideSpawnPoint() {
        return gameOverrideSpawnPoint;
    }
    
    public Location getSpawnPoint() {
        return spawnPoint;
    }
    
    public Location getTeleportPoint() {
        return teleportPoint;
    }
    
    public Location getCornerOne() {
        return cornerOne;
    }
    
    public Location getCornerTwo() {
        return cornerTwo;
    }

    // Utility method for setting config boolean values
    public String getEntryMessage() {
        return entryMessage;
    }
    
    public void setEntryMessage(String entryMessage) {
        setConfigString("player-rules.entry-message", entryMessage);
    }
    
    public String getExitMessage() {
        return exitMessage;
    }
    
    public void setExitMessage(String exitMessage) {
        setConfigString("player-rules.exit-message", exitMessage);
    }


    
    public String getDebugInfo() {
        StringBuilder debugInfo = new StringBuilder();
    
        debugInfo.append("Zone Name: ").append(name).append("\n");
        debugInfo.append("Description: ").append(description).append("\n");
        debugInfo.append("World: ").append(world).append("\n");
        debugInfo.append("Is Game Zone: ").append(isGameZone).append("\n");
        debugInfo.append("Game Override Spawn Point: ").append(gameOverrideSpawnPoint).append("\n");
    
        // Coordinates
        debugInfo.append("Spawn Point: ").append(formatLocation(spawnPoint)).append("\n");
        debugInfo.append("Teleport Point: ").append(formatLocation(teleportPoint)).append("\n");
        debugInfo.append("Corner One: ").append(formatLocation(cornerOne)).append("\n");
        debugInfo.append("Corner Two: ").append(formatLocation(cornerTwo)).append("\n");
    
        // Natural Rules
        debugInfo.append("Natural Rules:").append("\n");
        debugInfo.append("  Lava Ignite: ").append(lavaIgnite).append("\n");
        debugInfo.append("  Lightning Strike: ").append(lightningStrike).append("\n");
        debugInfo.append("  Water Flow: ").append(waterFlow).append("\n");
        debugInfo.append("  Lava Flow: ").append(lavaFlow).append("\n");
        debugInfo.append("  Snow Fall: ").append(snowFall).append("\n");
        debugInfo.append("  Snow Melt: ").append(snowMelt).append("\n");
        debugInfo.append("  Ice Melt: ").append(iceMelt).append("\n");
        debugInfo.append("  Mushroom Growth: ").append(mushroomGrowth).append("\n");
        debugInfo.append("  Leaf Decay: ").append(leafDecay).append("\n");
        debugInfo.append("  Grass Growth: ").append(grassGrowth).append("\n");
        debugInfo.append("  Mycelium Spread: ").append(myceliumSpread).append("\n");
        debugInfo.append("  Vine Growth: ").append(vineGrowth).append("\n");
        debugInfo.append("  Rock Growth: ").append(rockGrowth).append("\n");
        debugInfo.append("  Sculk Growth: ").append(sculkGrowth).append("\n");
        debugInfo.append("  Crop Growth: ").append(cropGrowth).append("\n");
        debugInfo.append("  Soil Dry: ").append(soilDry).append("\n");
    
        // Mob Rules
        debugInfo.append("Mob Rules:").append("\n");
        debugInfo.append("  Creeper Explosion: ").append(creeperExplosion).append("\n");
        debugInfo.append("  Ender Dragon Break: ").append(enderDragonBreak).append("\n");
        debugInfo.append("  Ghast Fireball Break: ").append(ghastFireballBreak).append("\n");
        debugInfo.append("  Enderman Break: ").append(endermanBreak).append("\n");
        debugInfo.append("  Snowman Trails: ").append(snowmanTrails).append("\n");
        debugInfo.append("  Ravager Break: ").append(ravagerBreak).append("\n");
        debugInfo.append("  Mob Damage: ").append(mobDamage).append("\n");
        debugInfo.append("  Mob Destroy Paintings: ").append(mobDestroyPaintings).append("\n");
        debugInfo.append("  Mob Destroy Item Frames: ").append(mobDestroyItemFrames).append("\n");
        debugInfo.append("  Wither Break: ").append(witherBreak).append("\n");
        debugInfo.append("  Hostile Mob Spawn List: ").append(hostileMobSpawnList).append("\n");
        debugInfo.append("  Passive Mob Spawn List: ").append(passiveMobSpawnList).append("\n");
        debugInfo.append("  Allow Custom Mobs: ").append(allowCustomMobs).append("\n");
    
        // Protection Rules
        debugInfo.append("Protection Rules:").append("\n");
        debugInfo.append("  Fire Spread: ").append(fireSpread).append("\n");
        debugInfo.append("  Interact: ").append(interact).append("\n");
        debugInfo.append("  Mount: ").append(mount).append("\n");
        debugInfo.append("  Chest Access: ").append(chestAccess).append("\n");
        debugInfo.append("  PvP: ").append(pvp).append("\n");
        debugInfo.append("  Sleep: ").append(sleep).append("\n");
        debugInfo.append("  Respawn Anchors: ").append(respawnAnchors).append("\n");
        debugInfo.append("  TNT: ").append(tnt).append("\n");
        debugInfo.append("  Vehicle Place: ").append(vehiclePlace).append("\n");
        debugInfo.append("  Vehicle Destroy: ").append(vehicleDestroy).append("\n");
        debugInfo.append("  Ignite: ").append(ignite).append("\n");
        debugInfo.append("  Trampling: ").append(trampling).append("\n");
        debugInfo.append("  Frost Walker: ").append(frostWalker).append("\n");
        debugInfo.append("  Item Frame Rotation: ").append(itemFrameRotation).append("\n");
        debugInfo.append("  Station Interact: ").append(stationInteract).append("\n");
        debugInfo.append("  Build List: ").append(buildList).append("\n");
        debugInfo.append("  Break List: ").append(breakList).append("\n");
        debugInfo.append("  Explosion Proof Blocks: ").append(explosionProofBlocks).append("\n");
    
        // Player Rules
        debugInfo.append("Player Rules:").append("\n");
        debugInfo.append("  Entry Message: ").append(entryMessage).append("\n");
        debugInfo.append("  Exit Message: ").append(exitMessage).append("\n");
        debugInfo.append("  Entry List: ").append(entryList).append("\n");
        debugInfo.append("  Exit List: ").append(exitList).append("\n");
        debugInfo.append("  Enderpearl: ").append(enderpearl).append("\n");
        debugInfo.append("  Chorus Fruit: ").append(chorusFruit).append("\n");
    
        // Map Rules
        debugInfo.append("Map Rules:").append("\n");
        debugInfo.append("  Item Pickup: ").append(itemPickup).append("\n");
        debugInfo.append("  Item Drop: ").append(itemDrop).append("\n");
        debugInfo.append("  EXP Drop: ").append(expDrop).append("\n");
        debugInfo.append("  Fall Damage: ").append(fallDamage).append("\n");
        debugInfo.append("  Weather Lock: ").append(weatherLock).append("\n");
        debugInfo.append("  Natural Health Regen: ").append(naturalHealthRegen).append("\n");
        debugInfo.append("  Natural Hunger Drain: ").append(naturalHungerDrain).append("\n");
        debugInfo.append("  Blocked Commands: ").append(blockedCommands).append("\n");
    
        return debugInfo.toString();
    }
    
    private String formatLocation(Location location) {
        if (location == null) return "null";
        return "World: " + location.getWorld().getName() + ", X: " + location.getX() + ", Y: " + location.getY() + ", Z: " + location.getZ();
    }







 // Natural rule getters and setters
    public boolean isLavaIgnite() { return lavaIgnite; }
    public void setLavaIgnite(boolean lavaIgnite) { setConfigBoolean("natural-rules.lava-ignite", lavaIgnite); }


    public boolean isLightningStrike() { return lightningStrike; }
    public void setLightningStrike(boolean lightningStrike) { setConfigBoolean("natural-rules.lightning-strike", lightningStrike); }

    public boolean isWaterFlow() { return waterFlow; }
    public void setWaterFlow(boolean waterFlow) { setConfigBoolean("natural-rules.water-flow", waterFlow); }

    public boolean isLavaFlow() { return lavaFlow; }
    public void setLavaFlow(boolean lavaFlow) { setConfigBoolean("natural-rules.lava-flow", lavaFlow); }

    public boolean isSnowFall() { return snowFall; }
    public void setSnowFall(boolean snowFall) { setConfigBoolean("natural-rules.snow-fall", snowFall); }

    public boolean isSnowMelt() { return snowMelt; }
    public void setSnowMelt(boolean snowMelt) { setConfigBoolean("natural-rules.snow-melt", snowMelt); }

    public boolean isIceMelt() { return iceMelt; }
    public void setIceMelt(boolean iceMelt) { setConfigBoolean("natural-rules.ice-melt", iceMelt); }

    public boolean isMushroomGrowth() { return mushroomGrowth; }
    public void setMushroomGrowth(boolean mushroomGrowth) { setConfigBoolean("natural-rules.mushroom-growth", mushroomGrowth); }

    public boolean isLeafDecay() { return leafDecay; }
    public void setLeafDecay(boolean leafDecay) { setConfigBoolean("natural-rules.leaf-decay", leafDecay); }

    public boolean isGrassGrowth() { return grassGrowth; }
    public void setGrassGrowth(boolean grassGrowth) { setConfigBoolean("natural-rules.grass-growth", grassGrowth); }

    public boolean isMyceliumSpread() { return myceliumSpread; }
    public void setMyceliumSpread(boolean myceliumSpread) { setConfigBoolean("natural-rules.mycelium-spread", myceliumSpread); }

    public boolean isVineGrowth() { return vineGrowth; }
    public void setVineGrowth(boolean vineGrowth) { setConfigBoolean("natural-rules.vine-growth", vineGrowth); }

    public boolean isRockGrowth() { return rockGrowth; }
    public void setRockGrowth(boolean rockGrowth) { setConfigBoolean("natural-rules.rock-growth", rockGrowth); }

    public boolean isSculkGrowth() { return sculkGrowth; }
    public void setSculkGrowth(boolean sculkGrowth) { setConfigBoolean("natural-rules.sculk-growth", sculkGrowth); }

    public boolean isCropGrowth() { return cropGrowth; }
    public void setCropGrowth(boolean cropGrowth) { setConfigBoolean("natural-rules.crop-growth", cropGrowth); }

    public boolean isSoilDry() { return soilDry; }
    public void setSoilDry(boolean soilDry) { setConfigBoolean("natural-rules.soil-dry", soilDry); }

    // Mob rule getters and setters
    public boolean isCreeperExplosion() { return creeperExplosion; }
    public void setCreeperExplosion(boolean creeperExplosion) { setConfigBoolean("mob-rules.creeper-explosion", creeperExplosion); }

    public boolean isEnderDragonBreak() { return enderDragonBreak; }
    public void setEnderDragonBreak(boolean enderDragonBreak) { setConfigBoolean("mob-rules.enderdragon-break", enderDragonBreak); }

    public boolean isGhastFireballBreak() { return ghastFireballBreak; }
    public void setGhastFireballBreak(boolean ghastFireballBreak) { setConfigBoolean("mob-rules.ghast-fireball-break", ghastFireballBreak); }

    public boolean isEndermanBreak() { return endermanBreak; }
    public void setEndermanBreak(boolean endermanBreak) { setConfigBoolean("mob-rules.enderman-break", endermanBreak); }

    public boolean isSnowmanTrails() { return snowmanTrails; }
    public void setSnowmanTrails(boolean snowmanTrails) { setConfigBoolean("mob-rules.snowman-trails", snowmanTrails); }

    public boolean isRavagerBreak() { return ravagerBreak; }
    public void setRavagerBreak(boolean ravagerBreak) { setConfigBoolean("mob-rules.ravager-break", ravagerBreak); }

    public boolean isMobDamage() { return mobDamage; }
    public void setMobDamage(boolean mobDamage) { setConfigBoolean("mob-rules.mob-damage", mobDamage); }

    public boolean isMobDestroyPaintings() { return mobDestroyPaintings; }
    public void setMobDestroyPaintings(boolean mobDestroyPaintings) { setConfigBoolean("mob-rules.mob-destroy-paintings", mobDestroyPaintings); }

    public boolean isMobDestroyItemFrames() { return mobDestroyItemFrames; }
    public void setMobDestroyItemFrames(boolean mobDestroyItemFrames) { setConfigBoolean("mob-rules.mob-destroy-item-frames", mobDestroyItemFrames); }

    public boolean isWitherBreak() { return witherBreak; }
    public void setWitherBreak(boolean witherBreak) { setConfigBoolean("mob-rules.wither-break", witherBreak); }

    public boolean isCustomMobsAllowed() {return allowCustomMobs;}
    public void setIsCustomMobsAllowed(boolean allowCustomMobs) {setConfigBoolean("mob-rules.allow-custom-mobs", allowCustomMobs);};

    // Protection rule getters and setters
    public boolean isFireSpread() { return fireSpread; }
    public void setFireSpread(boolean fireSpread) { setConfigBoolean("protection-rules.fire-spread", fireSpread); }

    public boolean isInteract() { return interact; }
    public void setInteract(boolean interact) { setConfigBoolean("protection-rules.interact", interact); }

    public boolean isMount() { return mount; }
    public void setMount(boolean mount) { setConfigBoolean("protection-rules.mount", mount); }

    // public boolean isDamagePassiveMobs() { return damagePassiveMobs; }
    // public void setDamagePassiveMobs(boolean damagePassiveMobs) { setConfigBoolean("protection-rules.damage-passive-mobs", damagePassiveMobs); }

    // public boolean isDamageHostileMobs() { return damageHostileMobs; }
    // public void setDamageHostileMobs(boolean damageHostileMobs) { setConfigBoolean("protection-rules.damage-hostile-mobs", damageHostileMobs); }

    public boolean isChestAccess() { return chestAccess; }
    public void setChestAccess(boolean chestAccess) { setConfigBoolean("protection-rules.chest-access", chestAccess); }

    public boolean isPvp() { return pvp; }
    public void setPvp(boolean pvp) { setConfigBoolean("protection-rules.pvp", pvp); }

    public boolean isSleep() { return sleep; }
    public void setSleep(boolean sleep) { setConfigBoolean("protection-rules.sleep", sleep); }

    public boolean isRespawnAnchors() { return respawnAnchors; }
    public void setRespawnAnchors(boolean respawnAnchors) { setConfigBoolean("protection-rules.respawn-anchors", respawnAnchors); }

    public boolean isTnt() { return tnt; }
    public void setTnt(boolean tnt) { setConfigBoolean("protection-rules.tnt", tnt); }

    public boolean isVehiclePlace() { return vehiclePlace; }
    public void setVehiclePlace(boolean vehiclePlace) { setConfigBoolean("protection-rules.vehicle-place", vehiclePlace); }

    public boolean isVehicleDestroy() { return vehicleDestroy; }
    public void setVehicleDestroy(boolean vehicleDestroy) { setConfigBoolean("protection-rules.vehicle-destroy", vehicleDestroy); }

    public boolean isIgnite() { return ignite; }
    public void setIgnite(boolean ignite) { setConfigBoolean("protection-rules.ignite", ignite); }

    public boolean isTrampling() { return trampling; }
    public void setTrampling(boolean trampling) { setConfigBoolean("protection-rules.trampling", trampling); }

    public boolean isFrostWalker() { return frostWalker; }
    public void setFrostWalker(boolean frostWalker) { setConfigBoolean("protection-rules.frost-walker", frostWalker); }

    public boolean isItemFrameRotation() { return itemFrameRotation; }
    public void setItemFrameRotation(boolean itemFrameRotation) { setConfigBoolean("protection-rules.item-frame-rotation", itemFrameRotation); }

    public boolean isStationInteract() { return stationInteract; }
    public void setIsStationInteract(boolean stationInteract) { setConfigBoolean("protection-rules.station-interact", stationInteract); }

    // Player rule getters and setters
    public boolean isEnderpearl() { return enderpearl; }
    public void setEnderpearl(boolean enderpearl) { setConfigBoolean("player-rules.enderpearl", enderpearl); }

    public boolean isChorusFruit() { return chorusFruit; }
    public void setChorusFruit(boolean chorusFruit) { setConfigBoolean("player-rules.chorus-fruit", chorusFruit); }

    // Map rule getters and setters
    public boolean isItemPickup() { return itemPickup; }
    public void setItemPickup(boolean itemPickup) { setConfigBoolean("map-rules.item-pickup", itemPickup); }

    public boolean isItemDrop() { return itemDrop; }
    public void setItemDrop(boolean itemDrop) { setConfigBoolean("map-rules.item-drop", itemDrop); }

    public boolean isExpDrop() { return expDrop; }
    public void setExpDrop(boolean expDrop) { setConfigBoolean("map-rules.exp-drop", expDrop); }

    public boolean isFallDamage() { return fallDamage; }
    public void setFallDamage(boolean fallDamage) { setConfigBoolean("map-rules.fall-damage", fallDamage); }

    public boolean isWeatherLock() { return weatherLock; }
    public void setWeatherLock(boolean weatherLock) { setConfigBoolean("map-rules.weather-lock", weatherLock); }

    public boolean isNaturalHealthRegen() { return naturalHealthRegen; }
    public void setNaturalHealthRegen(boolean naturalHealthRegen) { setConfigBoolean("map-rules.natural-health-regen", naturalHealthRegen); };

    public boolean isNaturalHungerDrain() { return naturalHungerDrain; };
    public void setNaturalHungerDrain(boolean naturalHungerDrain) { setConfigBoolean("map-rules.natural-hunger-drain", naturalHungerDrain); };

    public boolean isWithinBounds(Location location) {
        if (cornerOne == null || cornerTwo == null || location.getWorld() == null) {
            return false;
        }
    
        // Ensure the location's world name starts with the zone's world name
        if (!location.getWorld().getName().startsWith(this.world)) {
            return false;
        }
    
        double x1 = Math.min(cornerOne.getX(), cornerTwo.getX());
        double y1 = Math.min(cornerOne.getY(), cornerTwo.getY());
        double z1 = Math.min(cornerOne.getZ(), cornerTwo.getZ());
    
        double x2 = Math.max(cornerOne.getX(), cornerTwo.getX());
        double y2 = Math.max(cornerOne.getY(), cornerTwo.getY());
        double z2 = Math.max(cornerOne.getZ(), cornerTwo.getZ());
    
        double locX = location.getX();
        double locY = location.getY();
        double locZ = location.getZ();
    
        return locX >= x1 && locX <= x2 &&
               locY >= y1 && locY <= y2 &&
               locZ >= z1 && locZ <= z2;
    }
    

    
    

    // Additional getters for various properties
}
