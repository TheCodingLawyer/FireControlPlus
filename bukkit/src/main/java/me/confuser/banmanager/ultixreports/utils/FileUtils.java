package me.confuser.banmanager.ultixreports.utils;

import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.file.Files;

/**
 * File utility class for UltrixReports
 * 
 * @author confuser
 */
public class FileUtils {
    
    /**
     * Copy a resource file from the plugin JAR to the specified destination
     */
    public static void copyResourceFile(Plugin plugin, String resourcePath, File destination) throws IOException {
        if (destination.exists()) {
            return; // Don't overwrite existing files
        }
        
        InputStream resourceStream = plugin.getResource(resourcePath);
        if (resourceStream == null) {
            plugin.getLogger().warning("Could not find resource: " + resourcePath);
            return;
        }
        
        // Ensure parent directory exists
        destination.getParentFile().mkdirs();
        
        try (InputStream in = resourceStream;
             OutputStream out = Files.newOutputStream(destination.toPath())) {
            
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
} 