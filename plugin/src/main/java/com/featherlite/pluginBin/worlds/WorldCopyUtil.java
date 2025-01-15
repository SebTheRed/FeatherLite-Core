package com.featherlite.pluginBin.worlds;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class WorldCopyUtil {
    private final JavaPlugin plugin;
    private final boolean isDebuggerOn;

    public WorldCopyUtil(JavaPlugin plugin, boolean isDebuggerOn) {
        this.plugin = plugin;
        this.isDebuggerOn = isDebuggerOn;
    }

    public boolean copyWorld(String baseWorldName, String newWorldName) {
        File baseWorldFolder = new File(Bukkit.getWorldContainer(), baseWorldName);
        if (!baseWorldFolder.exists()) {
            plugin.getLogger().severe("Base world folder " + baseWorldName + " does not exist!");
            return false;
        }
    
        File newWorldFolder = new File(Bukkit.getWorldContainer(), newWorldName);
        try {
            copyFolder(baseWorldFolder, newWorldFolder);
            
            // Delete the uid.dat file from the new world folder
            File uidFile = new File(newWorldFolder, "uid.dat");
            if (uidFile.exists() && !uidFile.delete()) {
                plugin.getLogger().warning("Failed to delete uid.dat file in copied world " + newWorldName);
            }
    
            if (isDebuggerOn) {plugin.getLogger().info("Copied world from " + baseWorldName + " to " + newWorldName);}
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to copy world folder: " + e.getMessage());
            return false;
        }
    }
    

    private void copyFolder(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists() && !target.mkdirs()) {
                throw new IOException("Failed to create target directory: " + target);
            }
            for (File file : source.listFiles()) {
                if (file != null && !file.isHidden()) { // Skip hidden/system files
                    copyFolder(file, new File(target, file.getName()));
                }
            }
        } else {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
}
