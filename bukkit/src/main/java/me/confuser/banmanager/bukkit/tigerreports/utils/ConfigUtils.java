package me.confuser.banmanager.bukkit.tigerreports.utils;

import me.confuser.banmanager.bukkit.tigerreports.data.config.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration utilities for TigerReports integration
 * Adapted from original TigerReports ConfigUtils class
 */
public class ConfigUtils {
    
    public static boolean isEnabled(String path) {
        return isEnabled(ConfigFile.CONFIG.get(), path);
    }
    
    public static boolean isEnabled(FileConfiguration config, String path) {
        if (config == null) return false;
        return config.getBoolean(path, false);
    }
    
    public static boolean exists(FileConfiguration config, String path) {
        if (config == null) return false;
        return config.contains(path);
    }
    
    public static String getString(String path, String defaultValue) {
        FileConfiguration config = ConfigFile.CONFIG.get();
        if (config == null) return defaultValue;
        return config.getString(path, defaultValue);
    }
    
    public static int getInt(String path, int defaultValue) {
        FileConfiguration config = ConfigFile.CONFIG.get();
        if (config == null) return defaultValue;
        return config.getInt(path, defaultValue);
    }
    
    public static String getInfoLanguage() {
        return getString("Config.InfoLanguage", "English");
    }
    
    public static String getInfoMessage(String englishMessage, String frenchMessage) {
        return getInfoLanguage().equalsIgnoreCase("French") ? frenchMessage : englishMessage;
    }
    
    /**
     * Check if debug logging is enabled
     */
    public static boolean isDebugEnabled() {
        return isEnabled("Config.Debug.Enabled");
    }
    
    /**
     * Check if report query logging is enabled
     */
    public static boolean isReportQueryLoggingEnabled() {
        return isDebugEnabled() && isEnabled("Config.Debug.LogReportQueries");
    }
    
    /**
     * Check if menu operation logging is enabled
     */
    public static boolean isMenuOperationLoggingEnabled() {
        return isDebugEnabled() && isEnabled("Config.Debug.LogMenuOperations");
    }
}
