package me.confuser.banmanager.bukkit.aichatmod;

import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

import java.util.UUID;

/**
 * Data class for storing AI moderation actions
 * This is stored in the BanManager database for tracking and the /chatmod command
 */
@DatabaseTable(tableName = "bm_ai_mod_actions")
public class AiModActionData {
    
    @DatabaseField(generatedId = true)
    @Getter
    private int id;
    
    @DatabaseField(canBeNull = false, columnName = "player_uuid")
    @Getter
    private String playerUuid;
    
    @DatabaseField(canBeNull = false, columnName = "player_name")
    @Getter
    private String playerName;
    
    @DatabaseField(canBeNull = false, columnName = "action_type")
    @Getter
    private String actionType; // warn, mute, tempban, etc.
    
    @DatabaseField(canBeNull = true)
    @Getter
    private String duration; // For timed punishments
    
    @DatabaseField(canBeNull = false)
    @Getter
    private String reason;
    
    @DatabaseField(canBeNull = false)
    @Getter
    private String category; // profanity, harassment, hate-speech, etc.
    
    @DatabaseField(canBeNull = false, columnName = "original_message")
    @Getter
    private String originalMessage; // The message that triggered the action
    
    @DatabaseField(canBeNull = false, columnName = "ai_confidence")
    @Getter
    private double aiConfidence; // AI's confidence in the decision (0.0-1.0)
    
    @DatabaseField(canBeNull = true, columnName = "ai_analysis")
    @Getter
    private String aiAnalysis; // AI's explanation
    
    @DatabaseField(canBeNull = false, columnName = "offense_count")
    @Getter
    @Setter
    private int offenseCount; // Current offense count for escalation
    
    @DatabaseField(canBeNull = false, columnDefinition = "BIGINT UNSIGNED NOT NULL")
    @Getter
    private long created;
    
    @DatabaseField(canBeNull = false, columnName = "server_name")
    @Getter
    private String serverName;
    
    // Required for ORMLite
    public AiModActionData() {
        this.created = System.currentTimeMillis() / 1000L;
    }
    
    public AiModActionData(UUID playerUuid, String playerName, String actionType, 
                          String duration, String reason, String category,
                          String originalMessage, double aiConfidence, 
                          String aiAnalysis, int offenseCount, String serverName) {
        this.playerUuid = playerUuid.toString();
        this.playerName = playerName;
        this.actionType = actionType;
        this.duration = duration;
        this.reason = reason;
        this.category = category;
        this.originalMessage = originalMessage;
        this.aiConfidence = aiConfidence;
        this.aiAnalysis = aiAnalysis;
        this.offenseCount = offenseCount;
        this.serverName = serverName;
        this.created = System.currentTimeMillis() / 1000L;
    }
    
    public UUID getPlayerUUID() {
        return UUID.fromString(playerUuid);
    }
    
    /**
     * Get human-readable time since action
     */
    public String getTimeSince() {
        long now = System.currentTimeMillis() / 1000L;
        long diff = now - created;
        
        if (diff < 60) {
            return diff + "s ago";
        } else if (diff < 3600) {
            return (diff / 60) + "m ago";
        } else if (diff < 86400) {
            return (diff / 3600) + "h ago";
        } else {
            return (diff / 86400) + "d ago";
        }
    }
    
    /**
     * Get formatted action string for display
     */
    public String getFormattedAction() {
        if (duration != null && !duration.isEmpty()) {
            return actionType + " (" + duration + ")";
        }
        return actionType;
    }
}

