package me.confuser.banmanager.bukkit.aichatmod;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command handler for /chatmod
 * Allows admins to view AI moderation actions and stats
 */
public class ChatModCommand implements CommandExecutor, TabCompleter {
    
    private final BMBukkitPlugin plugin;
    private final AiChatModBootstrap bootstrap;
    
    public ChatModCommand(BMBukkitPlugin plugin, AiChatModBootstrap bootstrap) {
        this.plugin = plugin;
        this.bootstrap = bootstrap;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permission check
        if (!sender.hasPermission("bm.command.chatmod") && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "list":
            case "actions":
                listActions(sender, args);
                break;
            case "player":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /chatmod player <name> [limit]");
                    return true;
                }
                listPlayerActions(sender, args[1], args.length > 2 ? parseInt(args[2], 10) : 10);
                break;
            case "stats":
                showStats(sender);
                break;
            case "status":
                showStatus(sender);
                break;
            case "category":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /chatmod category <category> [limit]");
                    return true;
                }
                listByCategory(sender, args[1], args.length > 2 ? parseInt(args[2], 10) : 10);
                break;
            case "reload":
                reloadConfig(sender);
                break;
            default:
                showHelp(sender);
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━ " + ChatColor.GREEN + "AI Chat Mod" + ChatColor.GOLD + " ━━━━━━━━━━━━━");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/chatmod list [limit]" + ChatColor.GRAY + " - View recent AI actions");
        sender.sendMessage(ChatColor.YELLOW + "/chatmod player <name> [limit]" + ChatColor.GRAY + " - View player's AI actions");
        sender.sendMessage(ChatColor.YELLOW + "/chatmod category <category> [limit]" + ChatColor.GRAY + " - View by category");
        sender.sendMessage(ChatColor.YELLOW + "/chatmod stats" + ChatColor.GRAY + " - View AI mod statistics");
        sender.sendMessage(ChatColor.YELLOW + "/chatmod status" + ChatColor.GRAY + " - Check AI mod status");
        sender.sendMessage(ChatColor.YELLOW + "/chatmod reload" + ChatColor.GRAY + " - Reload config");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void listActions(CommandSender sender, String[] args) {
        if (!checkRunning(sender)) return;
        
        int limit = args.length > 1 ? parseInt(args[1], 20) : 20;
        
        AiModActionStorage storage = bootstrap.getManager().getActionStorage();
        if (storage == null) {
            sender.sendMessage(ChatColor.RED + "AI Chat Mod storage not initialized.");
            return;
        }
        
        try {
            List<AiModActionData> actions = storage.getRecentActions(limit);
            
            if (actions.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "No AI moderation actions found.");
                return;
            }
            
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━ " + ChatColor.GREEN + "Recent AI Actions" + ChatColor.GOLD + " ━━━━━━━━━");
            sender.sendMessage("");
            
            for (AiModActionData action : actions) {
                sendActionLine(sender, action);
            }
            
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GRAY + "Showing " + actions.size() + " most recent actions");
            sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "Error loading actions: " + e.getMessage());
        }
    }
    
    private void listPlayerActions(CommandSender sender, String playerName, int limit) {
        if (!checkRunning(sender)) return;
        
        AiModActionStorage storage = bootstrap.getManager().getActionStorage();
        if (storage == null) {
            sender.sendMessage(ChatColor.RED + "AI Chat Mod storage not initialized.");
            return;
        }
        
        try {
            // Try to get player UUID
            Player player = plugin.getServer().getPlayer(playerName);
            UUID uuid = player != null ? player.getUniqueId() : null;
            
            if (uuid == null) {
                // Try to find by name in storage
                List<AiModActionData> allActions = storage.getRecentActions(1000);
                List<AiModActionData> playerActions = allActions.stream()
                    .filter(a -> a.getPlayerName().equalsIgnoreCase(playerName))
                    .limit(limit)
                    .collect(Collectors.toList());
                
                if (playerActions.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No AI actions found for player: " + playerName);
                    return;
                }
                
                displayPlayerActions(sender, playerName, playerActions);
            } else {
                List<AiModActionData> actions = storage.getPlayerActions(uuid, limit);
                
                if (actions.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No AI actions found for player: " + playerName);
                    return;
                }
                
                displayPlayerActions(sender, playerName, actions);
            }
            
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "Error loading actions: " + e.getMessage());
        }
    }
    
    private void displayPlayerActions(CommandSender sender, String playerName, List<AiModActionData> actions) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━ " + ChatColor.AQUA + playerName + "'s AI Actions" + ChatColor.GOLD + " ━━━━━━━━━");
        sender.sendMessage("");
        
        for (AiModActionData action : actions) {
            sendActionLine(sender, action);
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "Total: " + actions.size() + " actions");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void listByCategory(CommandSender sender, String category, int limit) {
        if (!checkRunning(sender)) return;
        
        AiModActionStorage storage = bootstrap.getManager().getActionStorage();
        if (storage == null) {
            sender.sendMessage(ChatColor.RED + "AI Chat Mod storage not initialized.");
            return;
        }
        
        try {
            List<AiModActionData> actions = storage.getActionsByCategory(category, limit);
            
            if (actions.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "No actions found for category: " + category);
                return;
            }
            
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━ " + ChatColor.RED + category + " Actions" + ChatColor.GOLD + " ━━━━━━━━━");
            sender.sendMessage("");
            
            for (AiModActionData action : actions) {
                sendActionLine(sender, action);
            }
            
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GRAY + "Showing " + actions.size() + " actions in category: " + category);
            sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "Error loading actions: " + e.getMessage());
        }
    }
    
    private void sendActionLine(CommandSender sender, AiModActionData action) {
        ChatColor actionColor = getActionColor(action.getActionType());
        
        sender.sendMessage(
            ChatColor.GRAY + action.getTimeSince() + " " +
            ChatColor.YELLOW + action.getPlayerName() + " " +
            ChatColor.GRAY + "→ " +
            actionColor + action.getFormattedAction() + " " +
            ChatColor.GRAY + "[" + ChatColor.WHITE + action.getCategory() + ChatColor.GRAY + "] " +
            ChatColor.DARK_GRAY + "(#" + action.getOffenseCount() + ")"
        );
        
        // Show message snippet on hover (for console, just show it)
        if (!(sender instanceof Player)) {
            String msg = action.getOriginalMessage();
            if (msg.length() > 50) {
                msg = msg.substring(0, 47) + "...";
            }
            sender.sendMessage(ChatColor.DARK_GRAY + "  └─ \"" + msg + "\"");
        }
    }
    
    private ChatColor getActionColor(String action) {
        switch (action.toLowerCase()) {
            case "warn":
                return ChatColor.YELLOW;
            case "mute":
            case "tempmute":
                return ChatColor.GOLD;
            case "ban":
            case "tempban":
                return ChatColor.RED;
            default:
                return ChatColor.WHITE;
        }
    }
    
    private void showStats(CommandSender sender) {
        if (!checkRunning(sender)) return;
        
        AiModActionStorage storage = bootstrap.getManager().getActionStorage();
        if (storage == null) {
            sender.sendMessage(ChatColor.RED + "AI Chat Mod storage not initialized.");
            return;
        }
        
        try {
            long total = storage.getTotalActionCount();
            long last24h = storage.getRecentActionCount(24);
            long lastHour = storage.getRecentActionCount(1);
            
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━ " + ChatColor.GREEN + "AI Mod Statistics" + ChatColor.GOLD + " ━━━━━━━━━");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + "Total Actions: " + ChatColor.WHITE + total);
            sender.sendMessage(ChatColor.YELLOW + "Last 24 Hours: " + ChatColor.WHITE + last24h);
            sender.sendMessage(ChatColor.YELLOW + "Last Hour: " + ChatColor.WHITE + lastHour);
            sender.sendMessage(ChatColor.YELLOW + "Model: " + ChatColor.WHITE + bootstrap.getConfig().getModel());
            sender.sendMessage(ChatColor.YELLOW + "Confidence Threshold: " + ChatColor.WHITE + 
                String.format("%.0f%%", bootstrap.getConfig().getConfidenceThreshold() * 100));
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "Error loading stats: " + e.getMessage());
        }
    }
    
    private void showStatus(CommandSender sender) {
        AiChatModConfig config = bootstrap.getConfig();
        boolean running = bootstrap.isRunning();
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━ " + ChatColor.GREEN + "AI Mod Status" + ChatColor.GOLD + " ━━━━━━━━━━");
        sender.sendMessage("");
        
        if (config == null) {
            sender.sendMessage(ChatColor.RED + "● AI Chat Mod config not loaded");
            return;
        }
        
        if (!config.isEnabled()) {
            sender.sendMessage(ChatColor.YELLOW + "● AI Chat Mod is " + ChatColor.BOLD + "DISABLED" + ChatColor.RESET + ChatColor.YELLOW + " in config");
            sender.sendMessage(ChatColor.GRAY + "  Set 'enabled: true' in aichatmod.yml");
        } else if (running) {
            sender.sendMessage(ChatColor.GREEN + "● AI Chat Mod is " + ChatColor.BOLD + "ACTIVE");
        } else {
            sender.sendMessage(ChatColor.RED + "● AI Chat Mod is " + ChatColor.BOLD + "ENABLED but NOT RUNNING");
            sender.sendMessage(ChatColor.GRAY + "  Check API key in aichatmod.yml");
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Configuration:");
        sender.sendMessage(ChatColor.GRAY + "  Enabled: " + (config.isEnabled() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        sender.sendMessage(ChatColor.GRAY + "  Model: " + ChatColor.WHITE + config.getModel());
        sender.sendMessage(ChatColor.GRAY + "  API Key: " + ChatColor.WHITE + 
            (config.getApiKey().isEmpty() || config.getApiKey().equals("your-api-key-here") 
                ? ChatColor.RED + "NOT SET" 
                : ChatColor.GREEN + "Configured"));
        sender.sendMessage(ChatColor.GRAY + "  Check Interval: " + ChatColor.WHITE + config.getCheckInterval() + "s");
        sender.sendMessage(ChatColor.GRAY + "  Max Queue: " + ChatColor.WHITE + config.getMaxQueueSize());
        sender.sendMessage(ChatColor.GRAY + "  Bad Words: " + ChatColor.WHITE + config.getBadWords().size() + " configured");
        sender.sendMessage(ChatColor.GRAY + "  Categories: " + ChatColor.WHITE + String.join(", ", config.getCategories()));
        sender.sendMessage(ChatColor.GRAY + "  Offense Window: " + ChatColor.WHITE + config.getOffenseWindow() + " minutes");
        sender.sendMessage(ChatColor.GRAY + "  Punishment Levels: " + ChatColor.WHITE + config.getPunishmentLevels().size());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void reloadConfig(CommandSender sender) {
        if (!sender.hasPermission("bm.command.chatmod.reload") && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reload the config.");
            return;
        }
        
        bootstrap.reload();
        sender.sendMessage(ChatColor.GREEN + "AI Chat Mod config reloaded!");
        showStatus(sender);
    }
    
    /**
     * Check if the AI Chat Mod is running, show error if not
     */
    private boolean checkRunning(CommandSender sender) {
        if (!bootstrap.isRunning()) {
            sender.sendMessage(ChatColor.RED + "AI Chat Mod is not running.");
            sender.sendMessage(ChatColor.GRAY + "Use /chatmod status to check configuration.");
            return false;
        }
        return true;
    }
    
    private int parseInt(String str, int defaultVal) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("bm.command.chatmod") && !sender.isOp()) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("list", "player", "category", "stats", "status", "reload").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("player")) {
                // Return online player names
                return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("category") && bootstrap.getConfig() != null) {
                // Return categories from config
                return bootstrap.getConfig().getCategories().stream()
                    .filter(c -> c.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
}
