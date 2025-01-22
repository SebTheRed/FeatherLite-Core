package com.featherlite.pluginBin.zones;

import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ZoneListeners implements Listener {

    private final ZoneManager zoneManager;
    private final Map<Player, Zone> playerZoneCache = new HashMap<>();
    private final boolean isDebuggerOn;

    public ZoneListeners(ZoneManager zoneManager, JavaPlugin plugin, boolean isDebuggerOn) {
        this.zoneManager = zoneManager;
        this.isDebuggerOn = isDebuggerOn;
    }

    private boolean canBypassZone(Player player) {
        boolean hasPermission = player.hasPermission("core.zonebypass");
        boolean isOperator = player.isOp();
    
        // Log detailed information about permission check
        if (isDebuggerOn) {Bukkit.getLogger().info(String.format("Checking zone bypass for player '%s': Permission=%s, Op=%s",player.getName(),hasPermission,isOperator));}
    
        boolean canBypass = hasPermission || isOperator;
        return canBypass;
    }
    
    

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        // Only proceed if the player has actually changed block coordinates
        if (to == null || hasNotMovedToNewBlock(event)) return;

        Zone currentZone = zoneManager.getZoneAtLocation(to);
        Zone previousZone = playerZoneCache.get(player);

        // Only proceed if the player has moved into a different zone
        if (currentZone != previousZone) {
            // Send exit message if leaving a zone
            if (previousZone != null && previousZone.getExitMessage() != null) {
                player.sendMessage(ChatColor.RED + previousZone.getExitMessage());
            }
            // Send entry message if entering a new zone
            if (currentZone != null && currentZone.getEntryMessage() != null) {
                player.sendMessage(ChatColor.GREEN + currentZone.getEntryMessage());
            }
            // Update the cache with the new zone
            playerZoneCache.put(player, currentZone);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerZoneCache.remove(event.getPlayer());
    }

    private boolean hasNotMovedToNewBlock(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        
        return from.getBlockX() == to.getBlockX() &&
               from.getBlockY() == to.getBlockY() &&
               from.getBlockZ() == to.getBlockZ();
    }


    

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (canBypassZone(player)) return;
        Location blockLocation = event.getBlock().getLocation();
        Zone zone = zoneManager.getZoneAtLocation(blockLocation);

        if (zone != null && !zone.canBreak(event.getBlock().getType().name())) {
            event.setCancelled(true);
            if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "You cannot break this block in this zone.");}
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (canBypassZone(player)) return;
        Location blockLocation = event.getBlockPlaced().getLocation();
        Zone zone = zoneManager.getZoneAtLocation(blockLocation);

        if (zone != null && !zone.canBuild(event.getBlock().getType().name())) {
            event.setCancelled(true);
            if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "You cannot place blocks in this zone.");}
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
    
        Player player = (Player) event.getEntity();
        if (canBypassZone(player)) return;
        Zone zone = zoneManager.getZoneAtLocation(player.getLocation());
    
        if (zone != null) {
            switch (event.getCause()) {
                case FALL:
                    if (!zone.isFallDamage()) event.setCancelled(true);
                    break;
                case ENTITY_ATTACK:
                    // Check if the damage is caused by another entity
                    if (event instanceof EntityDamageByEntityEvent) {
                        EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
                        // Handle PvP restriction
                        if (entityEvent.getDamager() instanceof Player) {
                            Player attacker = (Player) entityEvent.getDamager();
                            if (!zone.isPvp()) {
                                event.setCancelled(true);
                                if (isDebuggerOn) {attacker.sendMessage(ChatColor.RED + "PvP is not allowed in this zone.");}
                            }
                        } else {
                            if (!zone.isMobDamage()) {
                                event.setCancelled(true);
                            }
                        }
                    }
                    break;
        
            }
        }
    }


    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        Zone zone = zoneManager.getZoneAtLocation(event.getLocation());
    
        if (zone != null) {
            EntityType entityType = event.getEntityType();
            boolean cancel = false;
    
            switch (entityType) {
                case CREEPER:
                    cancel = !zone.isCreeperExplosion();
                    break;
                case GHAST:
                    cancel = !zone.isGhastFireballBreak();
                    break;
                case ENDER_DRAGON:
                    cancel = !zone.isEnderDragonBreak();
                    break;
                case WITHER:
                    cancel = !zone.isWitherBreak();
                    break;
                case TNT:
                    cancel = !zone.isTnt();
                    break;
            }
    
            if (cancel != false) event.setCancelled(true);
            else event.blockList().removeIf(block -> !zone.isExplosionProof(block.getType().name()));
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (canBypassZone(player)) return;
        Zone zone = zoneManager.getZoneAtLocation(player.getLocation());

        if (zone != null && !zone.isItemPickup()) {
            event.setCancelled(true);
            if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "Item pickup is disabled in this zone.");}
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (canBypassZone(player)) return;
        Zone zone = zoneManager.getZoneAtLocation(player.getLocation());

        if (zone != null && !zone.isItemDrop()) {
            event.setCancelled(true);
            if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "Item dropping is disabled in this zone.");}
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (canBypassZone(player)) return;
        Zone zone = zoneManager.getZoneAtLocation(player.getLocation());
        
        String command = event.getMessage().split(" ")[0].substring(1); // Get command name

        if (zone != null && zone.isCommandBlocked(command)) {
            event.setCancelled(true);
            if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "This command is blocked in this zone.");}
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (canBypassZone(player)) return;
        Zone zone = zoneManager.getZoneAtLocation(player.getLocation()); // Check zone at player's location before respawn
        
        if (zone != null) {
            // If game override is enabled, allow the game plugin to handle respawn logic
            if (zone.isGameOverrideSpawnPoint()) {
                return;
            }
    
            // Otherwise, set the respawn location to the zone's spawn point
            Location spawnPoint = zone.getSpawnPoint();
            if (spawnPoint != null) {
                event.setRespawnLocation(spawnPoint);
                if (isDebuggerOn) {player.sendMessage(ChatColor.GREEN + "You have respawned at the "+ zone.getName() +" designated spawn point.");}
            }
        }
    }
    

    // Prevent block ignition if disabled in the zone
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Zone zone = zoneManager.getZoneAtLocation(event.getBlock().getLocation());
        if (event.getCause() == BlockIgniteEvent.IgniteCause.LAVA) {
            if (zone != null && !zone.isLavaIgnite()) {
                event.setCancelled(true);
            }
        } else if (zone != null && !zone.isIgnite()) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onLightningStrike(LightningStrikeEvent event) {
        Zone zone = zoneManager.getZoneAtLocation(event.getLightning().getLocation());
        if (zone != null && !zone.isLightningStrike()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityMount(EntityMountEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Zone zone = zoneManager.getZoneAtLocation(player.getLocation());

            if (zone != null && !zone.isMount()) {
                event.setCancelled(true);
                if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "You cannot mount entities in this zone.");}
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (canBypassZone(player)) return;
        Zone zone = zoneManager.getZoneAtLocation(player.getLocation());

        // Exit early if no zone or invalid action
        if (zone == null || (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)) {
            return;
        }

        Material clickedBlockType = event.getClickedBlock() != null ? event.getClickedBlock().getType() : null;
        Material itemType = event.getItem() != null ? event.getItem().getType() : null;

        // 1. Ender Pearl and Chorus Fruit Usage
        if (itemType == Material.ENDER_PEARL && !zone.isEnderpearl()) {
            event.setCancelled(true);
            if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "Ender pearl usage is not allowed in this zone.");}
            return;
        } else if (itemType == Material.CHORUS_FRUIT && !zone.isChorusFruit()) {
            event.setCancelled(true);
            if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "Chorus fruit usage is not allowed in this zone.");}
            return;
        }

        // 2. Vehicle Placement Check
        boolean isBoat = itemType == Material.OAK_BOAT || itemType == Material.SPRUCE_BOAT ||
                        itemType == Material.BIRCH_BOAT || itemType == Material.JUNGLE_BOAT ||
                        itemType == Material.DARK_OAK_BOAT || itemType == Material.ACACIA_BOAT ||
                        itemType == Material.MANGROVE_BOAT || itemType == Material.BAMBOO_RAFT;
        boolean isMinecart = itemType == Material.MINECART || itemType == Material.CHEST_MINECART ||
                            itemType == Material.FURNACE_MINECART || itemType == Material.HOPPER_MINECART ||
                            itemType == Material.TNT_MINECART;

        if ((isBoat || isMinecart) && !zone.isVehiclePlace()) {
            event.setCancelled(true);
            if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "Vehicle placement is disabled in this zone.");}
            return;
        }

        // 3. Interactables Check
        if (!zone.isInteract() && clickedBlockType != null) {
            if (clickedBlockType == Material.OAK_DOOR || clickedBlockType == Material.SPRUCE_DOOR ||
                clickedBlockType == Material.BIRCH_DOOR || clickedBlockType == Material.JUNGLE_DOOR ||
                clickedBlockType == Material.ACACIA_DOOR || clickedBlockType == Material.DARK_OAK_DOOR ||
                clickedBlockType == Material.MANGROVE_DOOR || clickedBlockType == Material.BAMBOO_DOOR ||
                clickedBlockType == Material.IRON_DOOR || clickedBlockType == Material.OAK_TRAPDOOR ||
                clickedBlockType == Material.SPRUCE_TRAPDOOR || clickedBlockType == Material.BIRCH_TRAPDOOR ||
                clickedBlockType == Material.JUNGLE_TRAPDOOR || clickedBlockType == Material.ACACIA_TRAPDOOR ||
                clickedBlockType == Material.DARK_OAK_TRAPDOOR || clickedBlockType == Material.MANGROVE_TRAPDOOR ||
                clickedBlockType == Material.BAMBOO_TRAPDOOR || clickedBlockType == Material.IRON_TRAPDOOR ||
                clickedBlockType == Material.LEVER || clickedBlockType == Material.STONE_BUTTON || 
                clickedBlockType == Material.OAK_BUTTON || clickedBlockType == Material.SPRUCE_BUTTON ||
                clickedBlockType == Material.BIRCH_BUTTON || clickedBlockType == Material.JUNGLE_BUTTON ||
                clickedBlockType == Material.ACACIA_BUTTON || clickedBlockType == Material.DARK_OAK_BUTTON ||
                clickedBlockType == Material.MANGROVE_BUTTON || clickedBlockType == Material.BAMBOO_BUTTON ||
                clickedBlockType == Material.POLISHED_BLACKSTONE_BUTTON || clickedBlockType == Material.CAULDRON ||
                clickedBlockType == Material.COMPOSTER || clickedBlockType == Material.NOTE_BLOCK ||
                clickedBlockType == Material.ITEM_FRAME) {
                event.setCancelled(true);
                if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "You cannot interact with this block in this zone.");}
                return;
            }
        }

        // 4. Station Interact Check
        if (!zone.isStationInteract() && clickedBlockType != null) {
            if (clickedBlockType == Material.CRAFTING_TABLE || clickedBlockType == Material.ENCHANTING_TABLE ||
                clickedBlockType == Material.SMITHING_TABLE || clickedBlockType == Material.CARTOGRAPHY_TABLE ||
                clickedBlockType == Material.BREWING_STAND || clickedBlockType == Material.GRINDSTONE ||
                clickedBlockType == Material.LOOM || clickedBlockType == Material.STONECUTTER) {
                event.setCancelled(true);
                if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "Station interaction is disabled in this zone.");}
                return;
            }
        }

        // 5. Bucket Placement Check
        if (itemType == Material.WATER_BUCKET && !zone.isWaterBucket()) {
            event.setCancelled(true);
            if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "Placing water with a bucket is not allowed in this zone.");}
            return;
        } else if (itemType == Material.LAVA_BUCKET && !zone.isLavaBucket()) {
            event.setCancelled(true);
            if (isDebuggerOn) {player.sendMessage(ChatColor.RED + "Placing lava with a bucket is not allowed in this zone.");}
            return;
        }
    }

    

    @EventHandler
    public void onItemFrameRotate(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame) {
            Player player = event.getPlayer();
            if (canBypassZone(player)) return;
            Zone zone = zoneManager.getZoneAtLocation(player.getLocation());

            if (zone != null && !zone.isItemFrameRotation()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot rotate item frames in this zone.");
            }
        }
    }



    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (canBypassZone(player)) return;
        Zone zone = zoneManager.getZoneAtLocation(player.getLocation());

        if (zone != null && !zone.isSleep()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot sleep in this zone.");
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        Zone zone = zoneManager.getZoneAtLocation(event.getWorld().getSpawnLocation()); // Check for world-level weather rule

        if (zone != null && zone.isWeatherLock()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRespawnAnchorUse(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (canBypassZone(player)) return;
        Zone zone = zoneManager.getZoneAtLocation(player.getLocation());

        if (zone != null && !zone.isRespawnAnchors()) {
            event.setRespawnLocation(player.getWorld().getSpawnLocation()); // Redirect to world spawn if respawn anchors are not allowed
            player.sendMessage(ChatColor.RED + "Respawn anchors are not allowed in this zone.");
        }
    }

    @EventHandler
    public void onCropTrample(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND) {
            Zone zone = zoneManager.getZoneAtLocation(event.getBlock().getLocation());

            if (zone != null && !zone.isTrampling()) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onExpDrop(EntityDeathEvent event) {
        Location location = event.getEntity().getLocation();
        Zone zone = zoneManager.getZoneAtLocation(location);

        if (zone != null && !zone.isExpDrop()) {
            event.setDroppedExp(0);
        }
    }


    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Location location = event.getVehicle().getLocation();
        Zone zone = zoneManager.getZoneAtLocation(location);
    
        if (zone != null && !zone.isVehicleDestroy()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onNaturalHealthRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            Player player = (Player) event.getEntity();
            if (canBypassZone(player)) return;
            Zone zone = zoneManager.getZoneAtLocation(player.getLocation());

            if (zone != null && !zone.isNaturalHealthRegen()) {
                event.setCancelled(true);
            }
        }
    }



    @EventHandler
    public void onRavagerBlockBreak(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.RAVAGER) {
            Zone zone = zoneManager.getZoneAtLocation(event.getBlock().getLocation());

            if (zone != null && !zone.isRavagerBreak()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEndermanBlockPickup(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.ENDERMAN) {
            Zone zone = zoneManager.getZoneAtLocation(event.getBlock().getLocation());

            if (zone != null && !zone.isEndermanBreak()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingEntityBreak(HangingBreakByEntityEvent event) {
        Zone zone = zoneManager.getZoneAtLocation(event.getEntity().getLocation());

        if (zone != null) {
            if (event.getEntity() instanceof org.bukkit.entity.Painting && !zone.isMobDestroyPaintings()) {
                event.setCancelled(true);
            } else if (event.getEntity() instanceof org.bukkit.entity.ItemFrame && !zone.isMobDestroyItemFrames()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        // Check if the inventory is opened by a player
        if (!(event.getPlayer() instanceof Player)) return;
    
        Player player = (Player) event.getPlayer();
    
        // Allow bypass
        if (canBypassZone(player)) return;
    
        // Fetch the zone at the player's location
        Zone zone = zoneManager.getZoneAtLocation(player.getLocation());
    
        if (zone != null && !zone.isChestAccess()) {
            // Only block access to "Chest" and "Large Chest" inventories
            String inventoryTitle = event.getView().getTitle();
    
            if ("Chest".equalsIgnoreCase(inventoryTitle) || "Large Chest".equalsIgnoreCase(inventoryTitle)) {
                event.setCancelled(true);
    
                // Debug message for blocking chest access
                if (isDebuggerOn) {
                    player.sendMessage(ChatColor.RED + "You cannot access chests in this zone.");
                }
            }
        }
    }
    

    @EventHandler
    public void onHungerDrain(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (canBypassZone(player)) return;
            Zone zone = zoneManager.getZoneAtLocation(player.getLocation());

            if (zone != null && !zone.isNaturalHungerDrain()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSnowmanTrail(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.SNOW_GOLEM && event.getTo() == Material.SNOW) {
            Zone zone = zoneManager.getZoneAtLocation(event.getBlock().getLocation());

            if (zone != null && !zone.isSnowmanTrails()) {
                event.setCancelled(true);
            }
        }
    }

    // @EventHandler
    // public void onTntExplosion(EntityExplodeEvent event) {
    //     if (event.getEntityType() == EntityType.TNT) {
    //         Zone zone = zoneManager.getZoneAtLocation(event.getLocation());

    //         if (zone != null && !zone.isTnt()) {
    //             event.setCancelled(true);
    //         }
    //     }
    // }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        Material blockType = event.getBlock().getType();
        Zone zone = zoneManager.getZoneAtLocation(event.getBlock().getLocation());
    
        if (zone == null) {
            return;
        }
    
        switch (blockType) {
            case SNOW:
                if (!zone.isSnowMelt()) {
                    event.setCancelled(true);
                }
                break;
                
            case ICE:
                if (!zone.isIceMelt()) {
                    event.setCancelled(true);
                }
                break;
    
            case FARMLAND:
                if (!zone.isSoilDry()) {
                    event.setCancelled(true);
                }
                break;
    
            // Add more cases as needed for other fade types
        }
    }
    

    @EventHandler
    public void onSnowFall(BlockFormEvent event) {
        if (event.getNewState().getType() == Material.SNOW) {
            Zone zone = zoneManager.getZoneAtLocation(event.getBlock().getLocation());
            if (zone != null && !zone.isSnowFall()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent event) {
        Material material = event.getBlock().getType();
        Zone zone = zoneManager.getZoneAtLocation(event.getBlock().getLocation());

        if (zone != null) {
            if (material == Material.WATER && !zone.isWaterFlow()) {
                event.setCancelled(true);
            } else if (material == Material.LAVA && !zone.isLavaFlow()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCropGrowth(StructureGrowEvent event) {
        Zone zone = zoneManager.getZoneAtLocation(event.getLocation());

        if (zone != null && !zone.isCropGrowth()) {
            event.setCancelled(true);
        }
    }

    
    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        Zone zone = zoneManager.getZoneAtLocation(event.getBlock().getLocation());
    
        if (zone == null) return; // Exit early if no zone is found
    
        Material sourceType = event.getSource().getType();
        boolean cancel = false;
    
        // Check for natural growth types and fire spread
        switch (sourceType) {
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
                cancel = !zone.isMushroomGrowth();
                break;
            case VINE:
                cancel = !zone.isVineGrowth();
                break;
            case GRASS_BLOCK:
                cancel = !zone.isGrassGrowth();
                break;
            case MYCELIUM:
                cancel = !zone.isMyceliumSpread();
                break;
            case FIRE:
                cancel = !zone.isFireSpread(); // Check fire spread rule in the zone
                break;
            default:
                break;
        }
    
        if (cancel) event.setCancelled(true);
    }
    


    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        Zone zone = zoneManager.getZoneAtLocation(event.getBlock().getLocation());

        if (zone != null && !zone.isLeafDecay()) {
            event.setCancelled(true);
        }
    }



    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        Location location = event.getLocation();
        Zone zone = zoneManager.getZoneAtLocation(location);
    
        if (zone == null) return; // Exit early if no zone
    
        EntityType entityType = event.getEntityType();
        String entityTypeName = entityType.name();
        CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();
    
        // Check for custom spawn first
        if (spawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            if (!zone.isCustomMobsAllowed()) {
                event.setCancelled(true);
                return;
            }
            // If custom mobs are allowed, no further checks are necessary for this entity
            return;
        }
    
        // For non-custom spawns, check if the entity is hostile or passive and apply restrictions
        if (isHostileMob(entityType)) {
            // Handle hostile mob spawn restriction
            if (!zone.canSpawnHostileMob(entityTypeName)) {
                event.setCancelled(true);
            }
        } else if (isPassiveMob(entityType)) {
            // Handle passive mob spawn restriction
            if (!zone.canSpawnPassiveMob(entityTypeName)) {
                event.setCancelled(true);
            }
        }
    }
    
    

private boolean isHostileMob(EntityType entityType) {
    switch (entityType) {
        case ZOMBIE:
        case SKELETON:
        case CREEPER:
        case SPIDER:
        case ENDERMAN:
        case PHANTOM:
        case BLAZE:
        case WITCH:
        case WITHER_SKELETON:
        case DROWNED:
        case HUSK:
        case PILLAGER:
        case RAVAGER:
        case VEX:
        case VINDICATOR:
        case WITHER:
        case EVOKER:
        case GUARDIAN:
        case ELDER_GUARDIAN:
        case HOGLIN:
        case PIGLIN_BRUTE:
        case ZOGLIN:
        case STRAY:
            return true;
        default:
            return false;
    }
}

/** 
 * Determines if the entity type is a passive mob.
 * Adjusts as necessary for Minecraft updates or specific server rules.
 */
private boolean isPassiveMob(EntityType entityType) {
    switch (entityType) {
        case SHEEP:
        case COW:
        case CHICKEN:
        case PIG:
        case RABBIT:
        case HORSE:
        case DONKEY:
        case MULE:
        case LLAMA:
        case CAT:
        case VILLAGER:
        case TURTLE:
        case DOLPHIN:
        case BAT:
        case PARROT:
        case FOX:
        case BEE:
        case MOOSHROOM:
        case STRIDER:
        case GLOW_SQUID:
        case AXOLOTL:
            return true;
        default:
            return false;
    }
}


}
