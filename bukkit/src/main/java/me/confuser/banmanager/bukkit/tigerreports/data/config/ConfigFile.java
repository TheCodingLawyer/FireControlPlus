package me.confuser.banmanager.bukkit.tigerreports.data.config;

import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Configuration file enum for TigerReports integration
 * Adapted from original TigerReports ConfigFile enum
 */
public enum ConfigFile {
    
    CONFIG("config.yml"),
    MESSAGES("messages.yml");
    
    private final String fileName;
    private FileConfiguration config;
    private File file;
    
    ConfigFile(String fileName) {
        this.fileName = fileName;
    }
    
    public void load(TigerReportsIntegration integration) {
        this.file = new File(integration.getDataFolder(), fileName);
        
        if (!file.exists()) {
            try {
                // Copy default file from resources
                integration.getBanManagerPlugin().saveResource("tigerreports/" + fileName, false);
            } catch (Exception e) {
                integration.getLogger().warning("Could not create default " + fileName + ": " + e.getMessage());
            }
        }
        
        this.config = YamlConfiguration.loadConfiguration(file);
    }
    
    public FileConfiguration get() {
        return config;
    }
    
    public void save() {
        if (config != null && file != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Could not save " + fileName + ": " + e.getMessage());
            }
        }
    }
    
    public void reload() {
        if (file != null) {
            this.config = YamlConfiguration.loadConfiguration(file);
        }
    }
}
