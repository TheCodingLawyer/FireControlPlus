package me.confuser.banmanager.bukkit.tigerreports.utils;

import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Web utilities for TigerReports integration
 * Adapted from original TigerReports WebUtils class
 */
public class WebUtils {
    
    public static void checkNewVersion(Plugin plugin, TigerReportsIntegration integration, 
                                     String resourceId, Consumer<String> callback) {
        // For now, we'll skip version checking in the integration
        // This can be implemented later if needed
        integration.getLogger().info("Version checking disabled for TigerReports integration");
    }
}
