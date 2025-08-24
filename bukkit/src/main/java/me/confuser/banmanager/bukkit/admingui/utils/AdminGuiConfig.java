package me.confuser.banmanager.bukkit.admingui.utils;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * Configuration manager for AdminGUI integration
 * Handles loading and managing AdminGUI configs within BanManager structure
 */
public class AdminGuiConfig {
    
    private final BMBukkitPlugin plugin;
    private final File adminGuiFolder;
    private final Logger logger;
    
    private YamlConfiguration config;
    private YamlConfiguration settings;
    private YamlConfiguration permissions;
    private YamlConfiguration players;
    private YamlConfiguration commands;
    
    // Config files
    private File configFile;
    private File settingsFile;
    private File permissionsFile;
    private File playersFile;
    private File commandsFile;
    
    public AdminGuiConfig(BMBukkitPlugin plugin, File adminGuiFolder) {
        this.plugin = plugin;
        this.adminGuiFolder = adminGuiFolder;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Load all AdminGUI configuration files
     */
    public void load() throws IOException {
        // Create config files
        createConfigFiles();
        
        // Load configurations
        config = YamlConfiguration.loadConfiguration(configFile);
        settings = YamlConfiguration.loadConfiguration(settingsFile);
        permissions = YamlConfiguration.loadConfiguration(permissionsFile);
        players = YamlConfiguration.loadConfiguration(playersFile);
        commands = YamlConfiguration.loadConfiguration(commandsFile);
        
        logger.info("AdminGUI configurations loaded");
    }
    
    /**
     * Create config files if they don't exist
     */
    private void createConfigFiles() throws IOException {
        configFile = new File(adminGuiFolder, "config.yml");
        settingsFile = new File(adminGuiFolder, "settings.yml");
        permissionsFile = new File(adminGuiFolder, "permissions.yml");
        playersFile = new File(adminGuiFolder, "players.yml");
        commandsFile = new File(adminGuiFolder, "commands.yml");
        
        // Create default config if it doesn't exist
        if (!configFile.exists()) {
            createDefaultConfig();
        }
        
        // Create other files if they don't exist
        createFileIfNotExists(settingsFile, "settings.yml");
        createFileIfNotExists(permissionsFile, "permissions.yml");
        createFileIfNotExists(playersFile, "players.yml");
        createFileIfNotExists(commandsFile, "commands.yml");
    }
    
    /**
     * Create default AdminGUI config adapted for BanManager integration
     */
    private void createDefaultConfig() throws IOException {
        YamlConfiguration defaultConfig = new YamlConfiguration();
        
        // AdminGUI settings adapted for BanManager
        defaultConfig.set("gui_type", 1);
        defaultConfig.set("initialize_gui", 1);
        defaultConfig.set("initialize_delay", 2);
        defaultConfig.set("initialize_reminder", false);
        defaultConfig.set("default_language", "English");
        defaultConfig.set("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE");
        defaultConfig.set("date_format", "yyyy-MM-dd HH:mm:ss");
        defaultConfig.set("show_ips", true);
        
        // Update checker
        defaultConfig.set("uc_enabled", true);
        defaultConfig.set("uc_notify", "&aNew AdminGUI update is available. Please update BanManager to &b{version}&a.");
        defaultConfig.set("uc_send_type", 1);
        
        // Admin tools
        defaultConfig.set("admin_tools_enabled", true);
        defaultConfig.set("admin_tools_name", "&c&lAdmin Tools");
        defaultConfig.set("admin_tools_material", "NETHER_STAR");
        defaultConfig.set("admin_tools_enchantment", false);
        defaultConfig.set("admin_tools_lore", "&dClick me to open Admin GUI");
        defaultConfig.set("admin_tools_give_on_join", false);
        defaultConfig.set("admin_tools_give_on_join_slot", 0);
        
        // Admin chat
        defaultConfig.set("ac_enabled", false);
        defaultConfig.set("ac_format", "{vault_prefix} &7{display_name} &7> {message}");
        defaultConfig.set("ac_default_color", "LIGHT_GRAY_WOOL");
        defaultConfig.set("ac_beautifier", true);
        defaultConfig.set("ac_no_chat_reports", 0);
        defaultConfig.set("ac_delay", 0);
        defaultConfig.set("ac_delay_message", "&cYou need to wait {seconds} seconds to sent another message!");
        
        // Freeze settings
        defaultConfig.set("freeze_player_move", true);
        defaultConfig.set("freeze_move_inventory", true);
        defaultConfig.set("freeze_execute_commands", true);
        defaultConfig.set("freeze_send_message", true);
        defaultConfig.set("freeze_break_blocks", true);
        defaultConfig.set("freeze_place_block", true);
        defaultConfig.set("freeze_drop_items", true);
        defaultConfig.set("freeze_admin_chat", "staffchat");
        defaultConfig.set("freeze_title", "&bYou are frozen!");
        defaultConfig.set("freeze_subtitle", "&cPlease listen to staff!");
        
        // Multiplayer sleep
        defaultConfig.set("ms_enabled", false);
        defaultConfig.set("ms_percentages", 50);
        defaultConfig.set("ms_message", "&9{display_name} went to sleep. Only {until_skip} more players needs to go to sleep for the night to be skipped.");
        
        // Command spy
        defaultConfig.set("acs_enabled", false);
        defaultConfig.set("acs_format", "&7[Command Spy] {name} &7> {message}");
        
        // BanManager integration settings
        defaultConfig.set("banmanager_integration", true);
        defaultConfig.set("use_banmanager_database", true);
        defaultConfig.set("use_banmanager_permissions", true);
        
        defaultConfig.save(configFile);
        logger.info("Created default AdminGUI config");
    }
    
    /**
     * Create a file if it doesn't exist
     */
    private void createFileIfNotExists(File file, String resourceName) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            // Try to copy from resources if available
            try (InputStream is = plugin.getResource("admingui/" + resourceName)) {
                if (is != null) {
                    Files.copy(is, file.toPath());
                }
            } catch (Exception e) {
                // Create empty file if resource not found
                YamlConfiguration.loadConfiguration(file).save(file);
            }
        }
    }
    
    // Configuration getters
    public YamlConfiguration getConfig() { return config; }
    public YamlConfiguration getSettings() { return settings; }
    public YamlConfiguration getPermissions() { return permissions; }
    public YamlConfiguration getPlayers() { return players; }
    public YamlConfiguration getCommands() { return commands; }
    
    // Convenience methods for common config values
    public int getGuiType() { return config.getInt("gui_type", 1); }
    public boolean isInitializeGui() { return config.getBoolean("initialize_gui", true); }
    public int getInitializeDelay() { return config.getInt("initialize_delay", 2); }
    public String getDefaultLanguage() { return config.getString("default_language", "English"); }
    public String getGuiDefaultColor() { return config.getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE"); }
    public String getDateFormat() { return config.getString("date_format", "yyyy-MM-dd HH:mm:ss"); }
    public boolean isShowIps() { return config.getBoolean("show_ips", true); }
    
    public boolean isAdminToolsEnabled() { return config.getBoolean("admin_tools_enabled", true); }
    public String getAdminToolsName() { return config.getString("admin_tools_name", "&c&lAdmin Tools"); }
    public String getAdminToolsMaterial() { return config.getString("admin_tools_material", "NETHER_STAR"); }
    
    public boolean isAdminChatEnabled() { return config.getBoolean("ac_enabled", false); }
    public String getAdminChatFormat() { return config.getString("ac_format", "{vault_prefix} &7{display_name} &7> {message}"); }
    
    public boolean isMultiplayerSleepEnabled() { return config.getBoolean("ms_enabled", false); }
    public int getMultiplayerSleepPercentage() { return config.getInt("ms_percentages", 50); }
    
    public boolean isCommandSpyEnabled() { return config.getBoolean("acs_enabled", false); }
    public String getCommandSpyFormat() { return config.getString("acs_format", "&7[Command Spy] {name} &7> {message}"); }
    
    public boolean isFreezeEnabled() { 
        return config.getBoolean("freeze_player_move", true) || 
               config.getBoolean("freeze_move_inventory", true) ||
               config.getBoolean("freeze_execute_commands", true);
    }
    
    // BanManager integration settings
    public boolean isBanManagerIntegration() { return config.getBoolean("banmanager_integration", true); }
    public boolean isUseBanManagerDatabase() { return config.getBoolean("use_banmanager_database", true); }
    public boolean isUseBanManagerPermissions() { return config.getBoolean("use_banmanager_permissions", true); }
    
    /**
     * Save all configurations
     */
    public void save() {
        try {
            config.save(configFile);
            settings.save(settingsFile);
            permissions.save(permissionsFile);
            players.save(playersFile);
            commands.save(commandsFile);
        } catch (IOException e) {
            logger.severe("Failed to save AdminGUI configurations: " + e.getMessage());
        }
    }
    
    /**
     * Reload all configurations
     */
    public void reload() {
        try {
            load();
            logger.info("AdminGUI configurations reloaded");
        } catch (IOException e) {
            logger.severe("Failed to reload AdminGUI configurations: " + e.getMessage());
        }
    }
} 
