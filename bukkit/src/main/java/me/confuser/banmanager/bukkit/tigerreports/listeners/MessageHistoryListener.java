package me.confuser.banmanager.bukkit.tigerreports.listeners;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerChatHistoryData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.storage.PlayerChatHistoryStorage;
import me.confuser.banmanager.common.util.UUIDUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message history listener for TigerReports integration
 * Tracks player chat messages for report context
 */
public class MessageHistoryListener implements Listener {
    
    // Store message history for each player (UUID -> List of messages)
    private final Map<UUID, List<ChatMessage>> messageHistory = new ConcurrentHashMap<>();
    
    // Maximum messages to store per player
    private static final int MAX_MESSAGES_PER_PLAYER = 50;
    
    // Maximum age of messages to keep (in milliseconds) - 30 days
    private static final long MAX_MESSAGE_AGE = 30L * 24L * 60L * 60L * 1000L;
    
    public MessageHistoryListener() {
        // Start cleanup task to remove old messages
        startCleanupTask();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage();
        
        // Create chat message record for in-memory storage
        ChatMessage chatMessage = new ChatMessage(
            System.currentTimeMillis(),
            player.getName(),
            message,
            player.getWorld().getName(),
            player.getLocation().getBlockX(),
            player.getLocation().getBlockY(),
            player.getLocation().getBlockZ()
        );
        
        // Add to in-memory history
        messageHistory.computeIfAbsent(playerId, k -> new ArrayList<>()).add(chatMessage);
        
        // Trim history if too long
        List<ChatMessage> playerMessages = messageHistory.get(playerId);
        if (playerMessages.size() > MAX_MESSAGES_PER_PLAYER) {
            playerMessages.remove(0); // Remove oldest message
        }
        
        // Also store in database for persistence and WebUI access
        storeChatMessageInDatabase(player, message);
    }
    
    private void storeChatMessageInDatabase(Player player, String message) {
        TigerReportsIntegration.getInstance().runTaskAsynchronously(() -> {
            try {
                BMBukkitPlugin bukkitPlugin = TigerReportsIntegration.getInstance().getBMBukkitPlugin();
                BanManagerPlugin banManagerPlugin = bukkitPlugin.getPlugin();
                PlayerChatHistoryStorage chatHistoryStorage = banManagerPlugin.getPlayerChatHistoryStorage();
                
                // Get or create player data
                PlayerData playerData = banManagerPlugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player.getUniqueId()));
                if (playerData == null) {
                    playerData = new PlayerData(player.getUniqueId(), player.getName());
                    banManagerPlugin.getPlayerStorage().create(playerData);
                }
                
                // Create and store chat history record
                PlayerChatHistoryData chatData = new PlayerChatHistoryData(
                    playerData,
                    message,
                    player.getWorld().getName(),
                    player.getLocation().getBlockX(),
                    player.getLocation().getBlockY(),
                    player.getLocation().getBlockZ()
                );
                
                chatHistoryStorage.create(chatData);
                
                // Clean up old messages (keep only last 500 per player)
                cleanupOldDatabaseMessages(playerData, chatHistoryStorage);
                
            } catch (SQLException e) {
                TigerReportsIntegration.getInstance().getLogger().warning("Failed to store chat message in database: " + e.getMessage());
            }
        });
    }
    
    private void cleanupOldDatabaseMessages(PlayerData playerData, PlayerChatHistoryStorage chatHistoryStorage) {
        try {
            List<PlayerChatHistoryData> playerMessages = chatHistoryStorage.queryBuilder()
                .where().eq("player_id", playerData)
                .query();
            
            // Sort by creation time (newest first)
            playerMessages.sort((a, b) -> Long.compare(b.getCreated(), a.getCreated()));
            
            // Keep only the latest 500 messages per player
            if (playerMessages.size() > 500) {
                for (int i = 500; i < playerMessages.size(); i++) {
                    chatHistoryStorage.delete(playerMessages.get(i));
                }
            }
        } catch (SQLException e) {
            TigerReportsIntegration.getInstance().getLogger().warning("Failed to cleanup old chat messages: " + e.getMessage());
        }
    }
    
    /**
     * Get message history for a player
     */
    public List<ChatMessage> getMessageHistory(UUID playerId) {
        List<ChatMessage> messages = messageHistory.get(playerId);
        if (messages == null) {
            return new ArrayList<>();
        }
        
        // Return a copy to prevent modification
        return new ArrayList<>(messages);
    }
    
    /**
     * Get message history for a player by name
     */
    public List<ChatMessage> getMessageHistory(String playerName) {
        // Find player by name (less efficient but needed for reports)
        for (Map.Entry<UUID, List<ChatMessage>> entry : messageHistory.entrySet()) {
            List<ChatMessage> messages = entry.getValue();
            if (!messages.isEmpty() && messages.get(messages.size() - 1).getPlayerName().equals(playerName)) {
                return new ArrayList<>(messages);
            }
        }
        return new ArrayList<>();
    }
    
    /**
     * Clear message history for a player
     */
    public void clearMessageHistory(UUID playerId) {
        messageHistory.remove(playerId);
    }
    
    /**
     * Get recent messages from all players (for context)
     */
    public List<ChatMessage> getRecentMessages(int limit) {
        List<ChatMessage> allMessages = new ArrayList<>();
        
        for (List<ChatMessage> playerMessages : messageHistory.values()) {
            allMessages.addAll(playerMessages);
        }
        
        // Sort by timestamp (newest first)
        allMessages.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        
        // Return limited results
        return allMessages.subList(0, Math.min(limit, allMessages.size()));
    }
    
    private void startCleanupTask() {
        // Run cleanup every 6 hours (since we keep messages for 30 days now)
        TigerReportsIntegration.getInstance().runTaskRepeatedly(
            6 * 60 * 60 * 1000L, // 6 hours initial delay
            6 * 60 * 60 * 1000L, // 6 hours interval
            this::cleanupOldMessages
        );
    }
    
    private void cleanupOldMessages() {
        long currentTime = System.currentTimeMillis();
        long cutoffTime = currentTime - MAX_MESSAGE_AGE;
        
        for (List<ChatMessage> playerMessages : messageHistory.values()) {
            playerMessages.removeIf(message -> message.getTimestamp() < cutoffTime);
        }
        
        // Remove empty lists
        messageHistory.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    /**
     * Chat message data class
     */
    public static class ChatMessage {
        private final long timestamp;
        private final String playerName;
        private final String message;
        private final String world;
        private final int x, y, z;
        
        public ChatMessage(long timestamp, String playerName, String message, String world, int x, int y, int z) {
            this.timestamp = timestamp;
            this.playerName = playerName;
            this.message = message;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getWorld() {
            return world;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getZ() {
            return z;
        }
        
        public String getFormattedTimestamp() {
            return new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(timestamp));
        }
        
        public String getFormattedLocation() {
            return world + " (" + x + ", " + y + ", " + z + ")";
        }
    }
}

