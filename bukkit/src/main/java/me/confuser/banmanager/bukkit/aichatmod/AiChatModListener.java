package me.confuser.banmanager.bukkit.aichatmod;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener for chat events to feed into AI moderation
 */
public class AiChatModListener implements Listener {
    
    private final BMBukkitPlugin plugin;
    private final AiChatModManager manager;
    
    public AiChatModListener(BMBukkitPlugin plugin, AiChatModManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!manager.isRunning()) return;
        
        Player player = event.getPlayer();
        
        // Check exemptions
        if (manager.isExempt(player)) {
            return;
        }
        
        // Queue message for AI analysis
        manager.queueMessage(
            player.getUniqueId(),
            player.getName(),
            event.getMessage()
        );
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Show status message to admins on join
        if (manager.isRunning() && manager.getConfig().isStatusMessageEnabled()) {
            if (player.hasPermission("bm.admin") || player.isOp()) {
                // Delay slightly to ensure player sees it
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                            "&7[&aAI Chat Mod&7] &8System is active and monitoring chat"));
                    }
                }, 40L); // 2 second delay
            }
        }
    }
}

