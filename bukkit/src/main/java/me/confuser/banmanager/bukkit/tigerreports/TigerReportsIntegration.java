package me.confuser.banmanager.bukkit.tigerreports;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.bukkit.tigerreports.commands.ReportCommand;
import me.confuser.banmanager.bukkit.tigerreports.commands.ReportsCommand;
import me.confuser.banmanager.bukkit.tigerreports.data.config.ConfigFile;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.MenuItem;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.MenuRawItem;
import me.confuser.banmanager.bukkit.tigerreports.listeners.CommentChatListener;
import me.confuser.banmanager.bukkit.tigerreports.listeners.InventoryListener;
import me.confuser.banmanager.bukkit.tigerreports.listeners.MessageHistoryListener;
import me.confuser.banmanager.bukkit.tigerreports.listeners.PlayerListener;
import me.confuser.banmanager.bukkit.tigerreports.managers.MenuUpdateManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.tasks.TaskScheduler;
import me.confuser.banmanager.bukkit.tigerreports.tasks.runnables.MenuUpdater;
import me.confuser.banmanager.bukkit.tigerreports.tasks.runnables.ReportsNotifier;
import me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils;
import me.confuser.banmanager.bukkit.tigerreports.utils.WebUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Real TigerReports integration class - contains the actual TigerReports code adapted for BanManager
 * This is NOT a rewrite - it's the original TigerReports code modified to work within BanManager
 * and use BanManager's database system for WebUI integration
 */
public class TigerReportsIntegration implements TaskScheduler {
    
    private static final String SPIGOTMC_RESOURCE_ID = "25773";
    
    private static TigerReportsIntegration instance;
    
    private final BMBukkitPlugin banManagerPlugin;
    private final BanManagerPlugin plugin;
    private final Logger logger;
    private final File dataFolder;
    
    private boolean loaded = false;
    private String newVersion = null;
    private boolean needUpdatesInstructions = false;
    
    // BanManager Database Integration - Use BanManager's database instead of separate connection
    private BanManagerDatabaseAdapter databaseAdapter;
    
    // TigerReports Managers
    private UsersManager usersManager;
    private ReportsManager reportsManager;
    private VaultManager vaultManager = null;
    private MenuUpdateManager menuUpdateManager;
    
    // Chat listener for comments
    private me.confuser.banmanager.bukkit.tigerreports.listeners.CommentChatListener commentChatListener;
    
    // Message history listener
    private me.confuser.banmanager.bukkit.tigerreports.listeners.MessageHistoryListener messageHistoryListener;
    
    // Vault Integration
    private static Permission perms = null;
    private static Chat chat = null;
    
    // Config files
    private File configFile;
    private File messagesFile;
    
    public TigerReportsIntegration(BMBukkitPlugin banManagerPlugin) {
        this.banManagerPlugin = banManagerPlugin;
        this.plugin = banManagerPlugin.getPlugin();
        this.logger = banManagerPlugin.getLogger();
        this.dataFolder = new File(banManagerPlugin.getDataFolder(), "tigerreports");
        instance = this;
    }
    
    public static TigerReportsIntegration getInstance() {
        return instance;
    }
    
    public BMBukkitPlugin getBanManagerPlugin() {
        return banManagerPlugin;
    }
    
    public BanManagerPlugin getPlugin() {
        return plugin;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public File getDataFolder() {
        return dataFolder;
    }
    
    public BanManagerDatabaseAdapter getDatabaseAdapter() {
        return databaseAdapter;
    }
    
    public UsersManager getUsersManager() {
        return usersManager;
    }
    
    public ReportsManager getReportsManager() {
        return reportsManager;
    }
    
    public VaultManager getVaultManager() {
        return vaultManager;
    }
    
    public me.confuser.banmanager.bukkit.tigerreports.listeners.CommentChatListener getCommentChatListener() {
        return commentChatListener;
    }
    
    public me.confuser.banmanager.bukkit.tigerreports.listeners.MessageHistoryListener getMessageHistoryListener() {
        return messageHistoryListener;
    }
    
    public MenuUpdateManager getMenuUpdateManager() {
        return menuUpdateManager;
    }
    
    public BMBukkitPlugin getBMBukkitPlugin() {
        return banManagerPlugin;
    }
    
    public static Permission getVaultPermissions() {
        return perms;
    }
    
    public static Chat getVaultChat() {
        return chat;
    }
    
    public void enable() {
        try {
            logger.info("Enabling TigerReports integration...");
            
            // Create data folder if it doesn't exist
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            // Setup config files
            setupConfigFiles();
            
            // Load configurations
            loadConfigurations();
            
            // Initialize menu items
            MenuRawItem.init();
            MenuItem.init();
            
            // Initialize managers
            usersManager = new UsersManager();
            reportsManager = new ReportsManager();
            menuUpdateManager = new MenuUpdateManager();
            
            // Setup Vault integration
            setupVaultIntegration();
            
            // Initialize BanManager database adapter
            databaseAdapter = new BanManagerDatabaseAdapter(plugin);
            
            // Register event listeners
            registerListeners();
            
            // Register commands
            registerCommands();
            
            // Start background tasks
            startBackgroundTasks();
            
            // Process online players
            processOnlinePlayers();
            
            // Check for updates if enabled
            checkForUpdates();
            
            setLoaded(true);
            logger.info("TigerReports integration enabled successfully!");
            
        } catch (Exception e) {
            logger.severe("Failed to enable TigerReports integration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public void disable() {
        logger.info("Disabling TigerReports integration...");
        
        // Stop background tasks
        MenuUpdater.stop();
        ReportsNotifier.stop();
        
        // Clear command executors
        setCommandExecutor("report", null);
        setCommandExecutor("reports", null);
        
        // Close any open menus
        if (usersManager != null) {
            usersManager.closeAllMenus();
        }
        
        // Unregister all event listeners
        // Note: We don't unregister all listeners as that would affect BanManager
        // Instead, our listeners will check if integration is loaded
        
        // Clean up managers
        reportsManager = null;
        usersManager = null;
        vaultManager = null;
        databaseAdapter = null;
        
        newVersion = null;
        setLoaded(false);
        
        logger.info("TigerReports integration disabled");
    }
    
    private void setupConfigFiles() throws IOException {
        configFile = new File(dataFolder, "config.yml");
        messagesFile = new File(dataFolder, "messages.yml");
        
        // Copy default config files if they don't exist
        if (!configFile.exists()) {
            banManagerPlugin.saveResource("tigerreports/config.yml", false);
        }
        if (!messagesFile.exists()) {
            banManagerPlugin.saveResource("tigerreports/messages.yml", false);
        }
    }
    
    private void loadConfigurations() {
        for (ConfigFile configFiles : ConfigFile.values()) {
            configFiles.load(this);
        }
    }
    
    private void setupVaultIntegration() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        vaultManager = new VaultManager(pm.getPlugin("Vault") != null);
        
        if (vaultManager.isVaultEnabled()) {
            RegisteredServiceProvider<Permission> rsp = banManagerPlugin.getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp != null) {
                perms = rsp.getProvider();
            }
            
            RegisteredServiceProvider<Chat> rspChat = banManagerPlugin.getServer().getServicesManager().getRegistration(Chat.class);
            if (rspChat != null) {
                chat = rspChat.getProvider();
            }
        }
    }
    
    private void registerListeners() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        
        pm.registerEvents(new InventoryListener(databaseAdapter, usersManager), banManagerPlugin);
        pm.registerEvents(new PlayerListener(reportsManager, databaseAdapter, this, vaultManager, usersManager), banManagerPlugin);
        
        // Initialize and register comment chat listener
        commentChatListener = new me.confuser.banmanager.bukkit.tigerreports.listeners.CommentChatListener(
            databaseAdapter, reportsManager, this, vaultManager, usersManager
        );
        pm.registerEvents(commentChatListener, banManagerPlugin);
        
        // Initialize and register message history listener
        messageHistoryListener = new me.confuser.banmanager.bukkit.tigerreports.listeners.MessageHistoryListener();
        pm.registerEvents(messageHistoryListener, banManagerPlugin);
    }
    
    private void registerCommands() {
        setCommandExecutor("report", new ReportCommand(this, reportsManager, databaseAdapter, vaultManager, usersManager));
        setCommandExecutor("reports", new ReportsCommand(reportsManager, databaseAdapter, this, vaultManager, usersManager));
    }
    
    private void setCommandExecutor(String commandName, org.bukkit.command.CommandExecutor commandExecutor) {
        PluginCommand command = banManagerPlugin.getCommand(commandName);
        if (command != null) {
            command.setExecutor(commandExecutor);
        } else {
            logger.warning("Command /" + commandName + " is not registered in plugin.yml");
        }
    }
    
    private void startBackgroundTasks() {
        ReportsNotifier.startIfNeeded(databaseAdapter, this);
    }
    
    private void processOnlinePlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            usersManager.processUserConnection(p);
            // Additional processing can be added here
        }
    }
    
    private void checkForUpdates() {
        if (ConfigUtils.isEnabled("Config.CheckNewVersion")) {
            WebUtils.checkNewVersion(
                banManagerPlugin,
                this,
                SPIGOTMC_RESOURCE_ID,
                (newVersion) -> {
                    this.newVersion = newVersion;
                }
            );
        }
    }
    
    public String getNewVersion() {
        return newVersion;
    }
    
    public boolean isLoaded() {
        return loaded;
    }
    
    private void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
    
    public boolean needUpdatesInstructions() {
        return needUpdatesInstructions;
    }
    
    // Task scheduler methods for compatibility with original TigerReports
    public int runTaskDelayedly(long delay, Runnable task) {
        return Bukkit.getScheduler().runTaskLater(banManagerPlugin, task, msToTicks(delay)).getTaskId();
    }
    
    public int runTaskDelayedlyAsynchronously(long delay, Runnable task) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(banManagerPlugin, task, msToTicks(delay)).getTaskId();
    }
    
    public void runTaskAsynchronously(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(banManagerPlugin, task);
    }
    
    public void runTask(Runnable task) {
        Bukkit.getScheduler().runTask(banManagerPlugin, task);
    }
    
    public int runTaskRepeatedly(long delay, long period, Runnable task) {
        return Bukkit.getScheduler().runTaskTimer(banManagerPlugin, task, msToTicks(delay), msToTicks(period)).getTaskId();
    }
    
    public void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }
    
    private static long msToTicks(long ms) {
        return (ms * 20) / 1000;
    }
    
    public org.bukkit.configuration.file.FileConfiguration getConfig() {
        return ConfigFile.CONFIG.get();
    }
}
