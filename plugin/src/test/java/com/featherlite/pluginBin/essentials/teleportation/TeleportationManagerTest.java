package com.featherlite.pluginBin.essentials.teleportation;

import com.featherlite.pluginBin.essentials.PlayerDataManager;
import com.featherlite.pluginBin.essentials.commands.HomeCommands;
import com.featherlite.pluginBin.essentials.commands.TeleportationCommands;
import java.io.File;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TeleportationManagerTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    private PlayerDataManager data;
    private TeleportationManager manager;
    private HomeManager homes;
    private Player player;
    private World world;
    private World otherWorld;
    private Location current;
    private MockedStatic<Bukkit> bukkit;
    private boolean cancelTeleport;

    @Before
    public void setUp() throws Exception {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(folder.getRoot());
        data = new PlayerDataManager(plugin, "players");
        manager = new TeleportationManager(data, plugin);
        homes = new HomeManager(data);
        world = mock(World.class);
        otherWorld = mock(World.class);
        when(world.getName()).thenReturn("world");
        when(world.getUID()).thenReturn(UUID.randomUUID());
        when(otherWorld.getName()).thenReturn("other");
        when(otherWorld.getUID()).thenReturn(UUID.randomUUID());
        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("Tester");
        when(player.hasPermission(any(String.class))).thenReturn(true);
        when(player.getLocation()).thenAnswer(invocation -> current.clone());
        when(player.getWorld()).thenAnswer(invocation -> current.getWorld());
        current = new Location(world, 1.5, 70, -3.5, 90, 20);
        // Existing player file lets the real data manager exercise persistence.
        File playerFile = new File(folder.getRoot(), "players/Tester-" + player.getUniqueId() + ".yml");
        assertTrue(playerFile.createNewFile());
        bukkit = mockStatic(Bukkit.class);
        bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
        bukkit.when(() -> Bukkit.getWorld("other")).thenReturn(otherWorld);
        when(player.teleport(any(Location.class))).thenAnswer(invocation -> {
            Location destination = invocation.getArgument(0);
            PlayerTeleportEvent event = new PlayerTeleportEvent(player, current.clone(), destination.clone());
            event.setCancelled(cancelTeleport);
            manager.onPlayerTeleport(event);
            if (event.isCancelled()) return false;
            current = event.getTo().clone();
            return true;
        });
    }

    @After
    public void tearDown() {
        if (bukkit != null) bukkit.close();
    }

    @Test
    public void homeAndBackAlternateBetweenDepartureAndHome() {
        Location departure = current.clone();
        Location home = new Location(otherWorld, 100, 80, 200, -45, 10);
        homes.setHome(player, "base", home);
        new HomeCommands(homes, manager).handleHomeCommands(player, null, "home", new String[]{"base"}, true);
        assertEquals(home, current);
        assertBreadcrumb(departure);
        assertTrue(manager.teleportBack(player));
        assertEquals(departure, current);
        assertBreadcrumb(home);
        assertTrue(manager.teleportBack(player));
        assertEquals(home, current);
        assertBreadcrumb(departure);
    }

    @Test
    public void tpAndTpposReplaceBreadcrumbWithLatestDeparture() {
        Player target = mock(Player.class);
        when(target.isOnline()).thenReturn(true);
        when(target.getName()).thenReturn("Target");
        Location targetLocation = new Location(otherWorld, -20, 65, 40, 180, -10);
        when(target.getLocation()).thenReturn(targetLocation);
        bukkit.when(() -> Bukkit.getPlayer("Target")).thenReturn(target);
        TeleportationCommands commands = new TeleportationCommands(manager, null);
        Location departure = current.clone();
        commands.handleTeleportCommands(player, null, "tp", new String[]{"Target"}, true);
        assertEquals(targetLocation, current);
        assertBreadcrumb(departure);
        commands.handleTeleportCommands(player, null, "tppos", new String[]{"10", "90", "30", "world"}, true);
        Location coordinates = new Location(world, 10, 90, 30);
        assertEquals(coordinates, current);
        assertBreadcrumb(targetLocation);
        commands.handleTeleportCommands(player, null, "back", new String[]{}, true);
        assertEquals(targetLocation, current);
        assertBreadcrumb(coordinates);
        commands.handleTeleportCommands(player, null, "back", new String[]{}, true);
        assertEquals(coordinates, current);
    }

    @Test
    public void cancelledHomeAndBackPreserveExistingBreadcrumb() {
        Location breadcrumb = new Location(world, 200, 70, 300);
        manager.onPlayerTeleport(new PlayerTeleportEvent(player, breadcrumb, current));
        Location departure = current.clone();
        homes.setHome(player, "base", new Location(world, 20, 90, 40));
        cancelTeleport = true;
        new HomeCommands(homes, manager).handleHomeCommands(player, null, "home", new String[]{"base"}, true);
        assertEquals(departure, current);
        assertBreadcrumb(breadcrumb);
        assertFalse(manager.teleportBack(player));
        assertEquals(departure, current);
        assertBreadcrumb(breadcrumb);
        verify(player, never()).sendMessage(org.bukkit.ChatColor.GREEN + "Teleported to your last location.");
    }

    @Test
    public void randomTeleportRecordsDepartureAndBackCanReturnToRandomDestination() {
        Location departure = current.clone();
        when(world.getSpawnLocation()).thenReturn(new Location(world, 0, 64, 0));
        when(world.getHighestBlockYAt(any(Location.class))).thenReturn(70);
        Block block = mock(Block.class);
        Material solidMaterial = mock(Material.class);
        when(solidMaterial.isSolid()).thenReturn(true);
        when(block.getType()).thenReturn(solidMaterial);
        when(world.getBlockAt(any(Location.class))).thenReturn(block);
        assertTrue(manager.teleportRandomly(player, 50, 100));
        Location randomDestination = current.clone();
        assertNotEquals(departure, randomDestination);
        assertBreadcrumb(departure);
        assertTrue(manager.teleportBack(player));
        assertEquals(departure, current);
        assertBreadcrumb(randomDestination);
        assertTrue(manager.teleportBack(player));
        assertEquals(randomDestination, current);
    }

    @Test
    public void spawnTeleportRecordsDeparture() {
        Location departure = current.clone();
        Location spawn = new Location(otherWorld, 0, 80, 0, 30, 5);
        assertTrue(manager.setServerSpawn(player, spawn));
        assertTrue(manager.teleportToSpawn(player));
        assertEquals(spawn, current);
        assertBreadcrumb(departure);
        assertTrue(manager.teleportBack(player));
        assertEquals(departure, current);
        assertBreadcrumb(spawn);
    }

    @Test
    public void breadcrumbSurvivesManagerRecreation() {
        Location departure = current.clone();
        player.teleport(new Location(otherWorld, 50, 70, 50));
        Location destination = current.clone();
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(folder.getRoot());
        data = new PlayerDataManager(plugin, "players");
        manager = new TeleportationManager(data, plugin);
        assertTrue(manager.teleportBack(player));
        assertEquals(departure, current);
        assertBreadcrumb(destination);
    }

    @Test
    public void missingBreadcrumbDoesNotTeleport() {
        assertFalse(manager.teleportBack(player));
        verify(player, never()).teleport(any(Location.class));
    }

    @Test
    public void unloadedBreadcrumbWorldDoesNotTeleportOrOverwriteIt() {
        data.updatePlayerData(player, "user-stats.last-teleport.world-name", "unloaded");
        assertFalse(manager.teleportBack(player));
        verify(player, never()).teleport(any(Location.class));
        assertEquals("unloaded", data.getPlayerData(player).getString("user-stats.last-teleport.world-name"));
    }

    @Test
    public void listenerRunsAfterCancellationHandlersAndIgnoresCancelledEvents() throws Exception {
        EventHandler annotation = TeleportationManager.class.getMethod("onPlayerTeleport", PlayerTeleportEvent.class)
                .getAnnotation(EventHandler.class);
        assertEquals(EventPriority.MONITOR, annotation.priority());
        assertTrue(annotation.ignoreCancelled());
        Location departure = new Location(otherWorld, 9, 80, 9, 35, 15);
        manager.onPlayerTeleport(new PlayerTeleportEvent(player, departure, current));
        assertBreadcrumb(departure);
    }

    private void assertBreadcrumb(Location expected) {
        FileConfiguration saved = data.getPlayerData(player);
        String path = "user-stats.last-teleport.";
        assertEquals(expected.getWorld().getName(), saved.getString(path + "world-name"));
        assertEquals(expected.getX(), saved.getDouble(path + "x"), 0);
        assertEquals(expected.getY(), saved.getDouble(path + "y"), 0);
        assertEquals(expected.getZ(), saved.getDouble(path + "z"), 0);
        assertEquals(expected.getYaw(), saved.getDouble(path + "yaw"), 0);
        assertEquals(expected.getPitch(), saved.getDouble(path + "pitch"), 0);
    }
}
