package me.confuser.banmanager.ultixreports;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.ultixreports.configs.UltrixConfig;
import me.confuser.banmanager.ultixreports.database.BanManagerReportAdapter;
import me.confuser.banmanager.ultixreports.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * UltrixReports - Enhanced reporting system for BanManager
 * Based on TigerReports but integrated with BanManager's infrastructure
 * 
 * @author confuser (integration), MrTigreroux (original TigerReports)
 */
public class UltrixReports {

    private static UltrixReports instance;

    @Getter
    private BanManagerPlugin banManagerPlugin;
    @Getter
    private me.confuser.banmanager.bukkit.BMBukkitPlugin bukkitPlugin;
    @Getter
    private UltrixConfig config;
    @Getter
    private BanManagerReportAdapter reportAdapter;
    
    private boolean loaded = false;
    private final Set<Consumer<Boolean>> loadUnloadListeners = new HashSet<>();

    /**
     * Initialize UltrixReports as an integrated component of BanManager
     */
    public static void initializeWithBanManager(me.confuser.banmanager.bukkit.BMBukkitPlugin bukkitPlugin) {
        if (instance != null) {
            bukkitPlugin.getLogger().warning("UltrixReports is already initialized!");
            return;
        }
        
        instance = new UltrixReports();
        instance.bukkitPlugin = bukkitPlugin;
        instance.banManagerPlugin = bukkitPlugin.getPlugin();
        
        bukkitPlugin.getLogger().info("Initializing UltrixReports with BanManager integration...");
        
        try {
            instance.load();
        } catch (Exception e) {
            bukkitPlugin.getLogger().severe("Failed to load UltrixReports: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("UltrixReports initialization failed", e);
        }
    }

    /**
     * Shutdown UltrixReports
     */
    public static void shutdown() {
        if (instance != null && instance.loaded) {
            instance.unload();
            instance = null;
        }
    }

    private void load() throws Exception {
        // Initialize configuration
        setupConfigs();
        
        // Initialize report adapter
        this.reportAdapter = new BanManagerReportAdapter(this);
        
        if (!reportAdapter.isInitialized()) {
            throw new IllegalStateException("BanManager report system is not properly initialized");
        }
        
        // TODO: Register commands when command classes are implemented
        // setupCommands();
        
        // TODO: Register listeners when listener classes are implemented  
        // setupListeners();
        
        // TODO: Start periodic tasks when task classes are implemented
        // setupTasks();
        
        loaded = true;
        
        // Notify load listeners
        for (Consumer<Boolean> listener : loadUnloadListeners) {
            listener.accept(true);
        }
        
        bukkitPlugin.getLogger().info("UltrixReports loaded successfully!");
    }

    private void unload() {
        bukkitPlugin.getLogger().info("Unloading UltrixReports...");
        
        // Cancel all tasks
        Bukkit.getScheduler().cancelTasks(bukkitPlugin);
        
        // Notify unload listeners
        for (Consumer<Boolean> listener : loadUnloadListeners) {
            listener.accept(false);
        }
        
        loaded = false;
        bukkitPlugin.getLogger().info("UltrixReports unloaded successfully!");
    }

    private void setupConfigs() throws IOException {
        File dataFolder = new File(bukkitPlugin.getDataFolder(), "ultrixreports");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Copy default configuration files from BanManager resources
        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            FileUtils.copyResourceFile(bukkitPlugin, "ultrixreports-config.yml", configFile);
        }
        
        // Copy default message files
        String[] languages = {"english", "french", "german", "spanish", "italian", "dutch", "polish", "russian", "chinese", "vietnamese"};
        File messagesDir = new File(dataFolder, "languages");
        if (!messagesDir.exists()) {
            messagesDir.mkdirs();
        }
        
        for (String lang : languages) {
            File langFile = new File(messagesDir, lang + ".yml");
            if (!langFile.exists()) {
                // Try to copy the specific language file, fallback to English if not found
                String resourcePath = "tigerreports/default-messages/" + lang + ".yml";
                try {
                    FileUtils.copyResourceFile(bukkitPlugin, resourcePath, langFile);
                } catch (Exception e) {
                    // Use default English if specific language file doesn't exist
                    FileUtils.copyResourceFile(bukkitPlugin, "tigerreports/default-messages/english.yml", langFile);
                }
            }
        }

        // Initialize configuration
        this.config = new UltrixConfig(dataFolder);
    }

    // TODO: Implement these methods when the corresponding classes are available
    /*
    private void setupCommands() {
        // Command setup will be implemented when ReportCommand and ReportsCommand are available
    }

    private void setupListeners() {
        // Listener setup will be implemented when PlayerListener and InventoryListener are available
    }

    private void setupTasks() {
        // Task setup will be implemented when MenuUpdater and ReportsNotifier are available
    }
    */



    public void addLoadUnloadListener(Consumer<Boolean> listener) {
        loadUnloadListeners.add(listener);
    }

    public void removeLoadUnloadListener(Consumer<Boolean> listener) {
        loadUnloadListeners.remove(listener);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public static UltrixReports getInstance() {
        return instance;
    }
} 