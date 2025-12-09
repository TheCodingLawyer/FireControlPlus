package me.confuser.banmanager.bukkit.aichatmod;

import lombok.Getter;
import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import org.bukkit.command.PluginCommand;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

/**
 * Bootstrap class for AI Chat Moderation feature
 * Handles initialization and lifecycle management
 */
public class AiChatModBootstrap {
    
    private final BMBukkitPlugin plugin;
    
    @Getter
    private AiChatModConfig config;
    
    @Getter
    private AiChatModManager manager;
    
    private AiChatModListener listener;
    
    public AiChatModBootstrap(BMBukkitPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Enable the AI Chat Mod feature
     */
    public void enable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Load config
        config = new AiChatModConfig(plugin.getDataFolder(), plugin.getPlugin().getLogger());
        try (InputStream is = plugin.getResource("aichatmod.yml")) {
            if (is != null) {
                config.load(new InputStreamReader(is));
            } else {
                // Try to load without defaults
                config.load(null);
            }
        } catch (IOException e) {
            plugin.getPlugin().getLogger().warning("Failed to load aichatmod.yml: " + e.getMessage());
            return;
        }
        
        // ALWAYS register command first (so /chatmod status works even when disabled)
        registerCommand();
        
        // Check if feature is enabled
        if (!config.isEnabled()) {
            plugin.getPlugin().getLogger().info("AI Chat Mod is disabled in config");
            return;
        }
        
        // Initialize manager
        manager = new AiChatModManager(plugin, config);
        manager.enable();
        
        if (!manager.isRunning()) {
            plugin.getPlugin().getLogger().warning("AI Chat Mod failed to start - check API key configuration");
            return;
        }
        
        // Register listener (only when actually running)
        listener = new AiChatModListener(plugin, manager);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        
        plugin.getPlugin().getLogger().info("AI Chat Mod enabled successfully with model: " + config.getModel());
    }
    
    /**
     * Register the /chatmod command
     * This is always registered so admins can check status even when disabled
     */
    private void registerCommand() {
        PluginCommand chatmodCmd = plugin.getCommand("chatmod");
        if (chatmodCmd != null) {
            ChatModCommand cmdExecutor = new ChatModCommand(plugin, this);
            chatmodCmd.setExecutor(cmdExecutor);
            chatmodCmd.setTabCompleter(cmdExecutor);
            plugin.getPlugin().getLogger().info("Registered /chatmod command");
        } else {
            plugin.getPlugin().getLogger().warning("Could not register /chatmod command - not found in plugin.yml");
        }
    }
    
    /**
     * Disable the AI Chat Mod feature
     */
    public void disable() {
        if (manager != null) {
            manager.disable();
        }
    }
    
    /**
     * Save the default config file
     */
    private void saveDefaultConfig() {
        File configFile = new File(plugin.getDataFolder(), "aichatmod.yml");
        if (!configFile.exists()) {
            try (InputStream is = plugin.getResource("aichatmod.yml")) {
                if (is != null) {
                    Files.copy(is, configFile.toPath());
                    plugin.getPlugin().getLogger().info("Created default aichatmod.yml");
                }
            } catch (IOException e) {
                plugin.getPlugin().getLogger().warning("Failed to create default aichatmod.yml: " + e.getMessage());
            }
        }
    }
    
    /**
     * Reload the config
     */
    public void reload() {
        if (manager != null) {
            manager.disable();
            manager = null;
        }
        
        try (InputStream is = plugin.getResource("aichatmod.yml")) {
            if (is != null) {
                config.reload(new InputStreamReader(is));
            }
        } catch (IOException e) {
            plugin.getPlugin().getLogger().warning("Failed to reload aichatmod.yml: " + e.getMessage());
        }
        
        if (config.isEnabled()) {
            manager = new AiChatModManager(plugin, config);
            manager.enable();
        }
    }
    
    /**
     * Check if the AI Chat Mod is currently running
     */
    public boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
