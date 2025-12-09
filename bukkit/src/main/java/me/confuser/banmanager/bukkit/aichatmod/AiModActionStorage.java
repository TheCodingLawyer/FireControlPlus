package me.confuser.banmanager.bukkit.aichatmod;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.stmt.Where;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage for AI moderation actions
 * Handles database operations for tracking AI mod actions
 */
public class AiModActionStorage extends BaseDaoImpl<AiModActionData, Integer> {
    
    private final BanManagerPlugin plugin;
    
    // Cache for offense counts (UUID -> offense count)
    private final ConcurrentHashMap<UUID, OffenseTracker> offenseCache = new ConcurrentHashMap<>();
    
    public AiModActionStorage(BanManagerPlugin plugin) throws SQLException {
        super(plugin.getLocalConn(), AiModActionData.class);
        this.plugin = plugin;
        
        if (!this.isTableExists()) {
            TableUtils.createTable(connectionSource, AiModActionData.class);
            plugin.getLogger().info("Created AI mod actions table");
        } else {
            // Add any new columns if needed
            try {
                executeRawNoArgs("ALTER TABLE `bm_ai_mod_actions` ADD COLUMN `ai_analysis` TEXT");
            } catch (SQLException ignored) {}
            
            try {
                executeRawNoArgs("ALTER TABLE `bm_ai_mod_actions` ADD COLUMN `server_name` VARCHAR(64) DEFAULT 'unknown'");
            } catch (SQLException ignored) {}
        }
    }
    
    /**
     * Record a new AI moderation action
     */
    public void recordAction(AiModActionData action) throws SQLException {
        create(action);
        
        // Update cache
        UUID uuid = action.getPlayerUUID();
        OffenseTracker tracker = offenseCache.computeIfAbsent(uuid, k -> new OffenseTracker());
        tracker.incrementOffense();
    }
    
    /**
     * Get offense count for a player within the time window
     */
    public int getOffenseCount(UUID playerUuid, int windowMinutes) throws SQLException {
        // Check cache first
        OffenseTracker cached = offenseCache.get(playerUuid);
        if (cached != null && !cached.isExpired(windowMinutes)) {
            return cached.getCount();
        }
        
        // Query database
        long windowStart = (System.currentTimeMillis() / 1000L) - (windowMinutes * 60L);
        
        QueryBuilder<AiModActionData, Integer> qb = queryBuilder();
        Where<AiModActionData, Integer> where = qb.where();
        where.eq("player_uuid", playerUuid.toString())
             .and()
             .ge("created", windowStart);
        
        long count = qb.countOf();
        
        // Update cache
        OffenseTracker tracker = offenseCache.computeIfAbsent(playerUuid, k -> new OffenseTracker());
        tracker.setCount((int) count);
        
        return (int) count;
    }
    
    /**
     * Get recent actions for the /chatmod command
     */
    public List<AiModActionData> getRecentActions(int limit) throws SQLException {
        QueryBuilder<AiModActionData, Integer> qb = queryBuilder();
        qb.orderBy("created", false); // Most recent first
        qb.limit((long) limit);
        return qb.query();
    }
    
    /**
     * Get actions for a specific player
     */
    public List<AiModActionData> getPlayerActions(UUID playerUuid, int limit) throws SQLException {
        QueryBuilder<AiModActionData, Integer> qb = queryBuilder();
        qb.where().eq("player_uuid", playerUuid.toString());
        qb.orderBy("created", false);
        qb.limit((long) limit);
        return qb.query();
    }
    
    /**
     * Get actions by category
     */
    public List<AiModActionData> getActionsByCategory(String category, int limit) throws SQLException {
        QueryBuilder<AiModActionData, Integer> qb = queryBuilder();
        qb.where().eq("category", category);
        qb.orderBy("created", false);
        qb.limit((long) limit);
        return qb.query();
    }
    
    /**
     * Get total action count
     */
    public long getTotalActionCount() throws SQLException {
        return queryBuilder().countOf();
    }
    
    /**
     * Get action count in the last X hours
     */
    public long getRecentActionCount(int hours) throws SQLException {
        long since = (System.currentTimeMillis() / 1000L) - (hours * 3600L);
        return queryBuilder().where().ge("created", since).countOf();
    }
    
    /**
     * Clear offense cache for a player (used when time window expires)
     */
    public void clearOffenseCache(UUID playerUuid) {
        offenseCache.remove(playerUuid);
    }
    
    /**
     * Clear all expired offense caches
     */
    public void cleanupOffenseCache(int windowMinutes) {
        offenseCache.entrySet().removeIf(entry -> entry.getValue().isExpired(windowMinutes));
    }
    
    /**
     * Simple offense tracker with timestamp
     */
    private static class OffenseTracker {
        private int count;
        private long lastUpdated;
        
        public OffenseTracker() {
            this.count = 0;
            this.lastUpdated = System.currentTimeMillis();
        }
        
        public void incrementOffense() {
            this.count++;
            this.lastUpdated = System.currentTimeMillis();
        }
        
        public void setCount(int count) {
            this.count = count;
            this.lastUpdated = System.currentTimeMillis();
        }
        
        public int getCount() {
            return count;
        }
        
        public boolean isExpired(int windowMinutes) {
            return System.currentTimeMillis() - lastUpdated > windowMinutes * 60 * 1000L;
        }
    }
}

