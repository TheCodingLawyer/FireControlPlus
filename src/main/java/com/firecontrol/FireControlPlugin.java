package com.firecontrol;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class FireControlPlugin extends JavaPlugin implements Listener {
    
    private boolean fireSpreadDisabled;
    private boolean showMessages;
    private String blockMessage;
    private boolean logBlocks;
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Load configuration
        loadConfig();
        
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        
        // Log startup message
        getLogger().info("FireControl v" + getDescription().getVersion() + " enabled!");
        getLogger().info("Fire spread is currently " + (fireSpreadDisabled ? "DISABLED" : "ENABLED"));
    }
    
    @Override
    public void onDisable() {
        getLogger().info("FireControl disabled!");
    }
    
    private void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        
        fireSpreadDisabled = config.getBoolean("fire-spread.disabled", true);
        showMessages = config.getBoolean("fire-spread.show-messages", true);
        blockMessage = ChatColor.translateAlternateColorCodes('&', 
            config.getString("fire-spread.block-message", "&cFire spread is disabled on this server"));
        logBlocks = config.getBoolean("logging.log-blocks", false);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        // Check if fire spread is disabled and if the spreading block is fire
        if (fireSpreadDisabled && event.getSource().getType() == Material.FIRE) {
            // Cancel the fire spread
            event.setCancelled(true);
            
            // Log if enabled
            if (logBlocks) {
                getLogger().info("Blocked fire spread from " + 
                    event.getSource().getLocation() + " to " + event.getBlock().getLocation());
            }
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("firecontrol")) {
            return false;
        }
        
        if (!sender.hasPermission("firecontrol.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "FireControl v" + getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "Usage: /firecontrol <enable|disable|status|reload>");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "enable":
                fireSpreadDisabled = false;
                getConfig().set("fire-spread.disabled", false);
                saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Fire spread has been ENABLED!");
                getLogger().info(sender.getName() + " enabled fire spread");
                break;
                
            case "disable":
                fireSpreadDisabled = true;
                getConfig().set("fire-spread.disabled", true);
                saveConfig();
                sender.sendMessage(ChatColor.RED + "Fire spread has been DISABLED!");
                getLogger().info(sender.getName() + " disabled fire spread");
                break;
                
            case "status":
                sender.sendMessage(ChatColor.YELLOW + "Fire spread is currently " + 
                    (fireSpreadDisabled ? ChatColor.RED + "DISABLED" : ChatColor.GREEN + "ENABLED"));
                break;
                
            case "reload":
                loadConfig();
                sender.sendMessage(ChatColor.GREEN + "FireControl configuration reloaded!");
                sender.sendMessage(ChatColor.YELLOW + "Fire spread is now " + 
                    (fireSpreadDisabled ? ChatColor.RED + "DISABLED" : ChatColor.GREEN + "ENABLED"));
                getLogger().info(sender.getName() + " reloaded FireControl configuration");
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand! Use: enable, disable, status, or reload");
                break;
        }
        
        return true;
    }
} 