// WebAppManager.java
package com.featherlite.pluginBin.webapp;

import org.bukkit.entity.Player;

import com.featherlite.pluginBin.FeatherCore;
import com.featherlite.pluginBin.FileManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.io.OutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;



public class WebAppManager {
    private final FeatherCore plugin;
    private final FileManager fileManager;
    private final boolean isDebuggerOn;

    public WebAppManager(FeatherCore plugin, FileManager fileManager, boolean isDebuggerOn) {
        this.plugin = plugin;
        this.fileManager = fileManager;
        this.isDebuggerOn = isDebuggerOn;
    }


    

    // Handle generating a new session and sending session data to Firebase
    public boolean handleGenerateSession(Player player, Map<UUID, String> activeSessions) {
        if (!player.hasPermission("feathercore.session")) {
            player.sendMessage("You do not have permission to generate a session link.");
            return true;
        }

        String sessionID = UUID.randomUUID().toString();
        activeSessions.put(player.getUniqueId(), sessionID);

        try {
            boolean success = sendSessionToFirebase(player, sessionID);

            if (success) {
                String sessionLink = "https://featherlite.app/session/" + sessionID;
                player.sendMessage("Follow the app link to manage all FeatherLite configs.");
                player.sendMessage(sessionLink);
                plugin.getLogger().info("Session link generated for " + player.getName() + ": " + sessionLink);
            } else {
                player.sendMessage("Server error: Unable to start session. Please try again later.");
                plugin.getLogger().warning("Failed to send session data to Firebase for player " + player.getName());
            }
        } catch (Exception e) {
            player.sendMessage("An unexpected server error occurred. Please try again later.");
            plugin.getLogger().severe("Error creating session for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    // Handle saving updates with a session string
    public boolean handleSaveCommand(Player player, String sessionID, Map<UUID, String> activeSessions) {
        if (!player.hasPermission("feathercore.session")) {
            player.sendMessage("You do not have permission to save changes.");
            return true;
        }

        if (sessionID == null || sessionID.isEmpty()) {
            player.sendMessage("Invalid session ID provided.");
            return true;
        }

        // Try to fetch and apply changes from Firebase based on the session ID
        try {
            boolean changesApplied = applyChangesFromFirebase(sessionID);
            if (changesApplied) {
                player.sendMessage("Changes saved and applied successfully.");
                plugin.getLogger().info("Changes applied successfully for session: " + sessionID);
            } else {
                player.sendMessage("Failed to apply changes. Please ensure the session is valid.");
                plugin.getLogger().warning("Failed to apply changes for session: " + sessionID);
            }
        } catch (Exception e) {
            player.sendMessage("An error occurred while applying changes. Please try again later.");
            plugin.getLogger().severe("Error applying changes for session " + sessionID + ": " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    // Fetch and apply changes from Firebase based on sessionID
    private boolean applyChangesFromFirebase(String sessionID) throws Exception {
        String jsonData = fetchChangesFromFirebase(sessionID);
        try {
            Map<String, Map<String, Map<String, String>>> changes = parseJsonToMap(jsonData);

            // Iterate over the changes and apply them to their respective plugins/configs
            for (String pluginName : changes.keySet()) {
                Map<String, Map<String, String>> pluginChanges = changes.get(pluginName);

                for (String configName : pluginChanges.keySet()) {
                    Map<String, String> configChanges = pluginChanges.get(configName);
                    var config = fileManager.loadConfig(pluginName, configName);

                    for (Map.Entry<String, String> entry : configChanges.entrySet()) {
                        config.set(entry.getKey(), entry.getValue());
                    }
                    fileManager.saveConfig(config, pluginName + "/" + configName);
                }
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error applying changes for session " + sessionID + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String fetchChangesFromFirebase(String sessionID) throws Exception {
        // Firebase retrieval logic here, return JSON string
        return "{\"examplePlugin\": {\"config.yml\": {\"key1\": \"value1\", \"key2\": \"value2\"}}}";
    }









    
    private Map<String, Map<String, Map<String, String>>> parseJsonToMap(String jsonData) {
        // Parse the JSON string into a nested map structure
        Gson gson = new Gson();
        // The type definition helps Gson understand the deeply nested structure.
        Type type = new TypeToken<Map<String, Map<String, Map<String, String>>>>() {}.getType();
        return gson.fromJson(jsonData, type);
    }

    private boolean sendSessionToFirebase(Player player, String sessionID) {
        String requesterUUID = player.getUniqueId().toString();

        // Gather all matching FeatherLite plugin directories and their configs using FileManager
        Map<String, Map<String, Map<String, Object>>> pluginData = gatherFeatherLiteData();

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("sessionToken", sessionID);
        requestData.put("requesterUUID", requesterUUID);
        requestData.put("pluginData", pluginData);

        String jsonPayload = new Gson().toJson(requestData);

        player.sendMessage("App session starting up...");
        if (isDebuggerOn) {plugin.getLogger().info("Sending JSON payload to Firebase: " + jsonPayload);}

        try {
            String firebaseFunctionURL = "https://us-central1-featherlite-73f54.cloudfunctions.net/startSession";
            URL url = new URL(firebaseFunctionURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            player.sendMessage("Firebase response code: " + responseCode);
            plugin.getLogger().info("Firebase response code: " + responseCode);
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            player.sendMessage("Error: Failed to send session data to Firebase.");
            plugin.getLogger().severe("Failed to send session data to Firebase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    private Map<String, Map<String, Map<String, Object>>> gatherFeatherLiteData() {
        Map<String, Map<String, Map<String, Object>>> pluginData = new HashMap<>();
        File pluginsFolder = plugin.getDataFolder().getParentFile();
    
        if (!pluginsFolder.exists() || !pluginsFolder.isDirectory()) {
            plugin.getLogger().warning("Plugins folder missing or not a directory.");
            return pluginData;
        }
    
        for (File subDir : pluginsFolder.listFiles(File::isDirectory)) {
            if (subDir.getName().startsWith("FeatherLite-")) {
                if (isDebuggerOn) {plugin.getLogger().info("Processing plugin directory: " + subDir.getName());}
                String pluginName = subDir.getName();
    
                // Call the FileManager method to read YAML contents as strings
                Map<String, String> pluginConfigData = fileManager.readAllYamlFilesInDirectory(subDir);
                
                if (!pluginConfigData.isEmpty()) {
                    // Convert to Map<String, Map<String, Object>> format
                    Map<String, Map<String, Object>> nestedData = new HashMap<>();
                    
                    // Loop through the files and convert each YAML file data into a nested map structure
                    for (Map.Entry<String, String> entry : pluginConfigData.entrySet()) {
                        // Wrap each YAML content string into a map under the filename key
                        Map<String, Object> fileDataMap = new HashMap<>();
                        fileDataMap.put("content", entry.getValue());
                        nestedData.put(entry.getKey(), fileDataMap);
                    }
    
                    pluginData.put(pluginName, nestedData);
                    plugin.getLogger().info("Added data for plugin: " + pluginName);
                } else {
                    plugin.getLogger().warning("No config data found for plugin: " + pluginName);
                }
            }
        }
        return pluginData;
    }
    
    

    
    // private Map<String, Map<String, Object>> readConfigsFromDirectory(File pluginDir) {
    //     Map<String, Map<String, Object>> configsData = new HashMap<>();
    //     plugin.getLogger().info("Reading configs from directory: " + pluginDir.getName());
    
    //     // Recursively read configurations from directories and subdirectories without including the top-level directory name in paths
    //     readConfigsRecursive(pluginDir, configsData, "");
        
    //     if (configsData.isEmpty()) {
    //         plugin.getLogger().warning("No configs found in directory: " + pluginDir.getName());
    //     } else {
    //         plugin.getLogger().info("Configs read successfully from directory: " + pluginDir.getName());
    //     }
    
    //     return configsData;
    // }
    
    // private void readConfigsRecursive(File dir, Map<String, Map<String, Object>> configsData, String basePath) {
    //     // Loop through all files and directories in the current directory
    //     for (File file : dir.listFiles()) {
    //         if (file.isDirectory()) {
    //             // If it's a directory, recursively read its contents without including the plugin's name in nested keys
    //             String nestedBasePath = basePath.isEmpty() ? file.getName() : basePath + "/" + file.getName();
    //             plugin.getLogger().info("Entering directory: " + nestedBasePath);
    //             readConfigsRecursive(file, configsData, nestedBasePath);
    //         } else if (file.getName().endsWith(".yml")) {
    //             // If it's a .yml file, load its entire contents
    //             plugin.getLogger().info("Reading YAML file: " + file.getName());
    //             FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    
    //             // Parse entire YAML file into a Map
    //             Map<String, Object> fileContent = config.getValues(true); // Get all keys deeply as a flat Map
    
    //             // Add the parsed data to configsData under its relative path
    //             configsData.put(basePath + "/" + file.getName(), fileContent);
    //             plugin.getLogger().info("Loaded data from file: " + file.getName());
    //         }
    //     }
    // }
    
    // private Map<String, Object> getDeepValues(FileConfiguration config, String parentPath) {
    //     Map<String, Object> result = new HashMap<>();
    
    //     // Loop through all keys at the current level
    //     for (String key : config.getKeys(false)) {
    //         String fullPath = parentPath.isEmpty() ? key : parentPath + "." + key;
    //         Object value = config.get(fullPath);
    
    //         if (value instanceof MemorySection) {
    //             // If the value is another section, get its nested values recursively
    //             plugin.getLogger().info("Entering nested section: " + fullPath);
    //             result.put(key, getDeepValues(config, fullPath));
    //         } else {
    //             // Otherwise, just put the value in the map
    //             plugin.getLogger().info("Adding value: " + fullPath + " = " + value);
    //             result.put(key, value);
    //         }
    //     }
    
    //     return result;
    // }
    
}
