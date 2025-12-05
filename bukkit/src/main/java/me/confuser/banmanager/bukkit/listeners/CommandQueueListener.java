package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ormlite.dao.GenericRawResults;
import me.confuser.banmanager.common.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class CommandQueueListener implements Runnable {
    private final BMBukkitPlugin plugin;
    private final BanManagerPlugin bmPlugin;
    private final String serverId;
    private boolean tableChecked = false;
    private boolean tableExists = false;

    public CommandQueueListener(BMBukkitPlugin plugin) {
        this.plugin = plugin;
        this.bmPlugin = plugin.getPlugin();
        // Read server id from environment or default to 'ALL'
        String envServerId = System.getenv("BM_SERVER_ID");
        this.serverId = (envServerId == null || envServerId.isEmpty()) ? "ALL" : envServerId;
    }

    @Override
    public void run() {
        if (bmPlugin == null) return;

        // Check/create table once on first run
        if (!tableChecked) {
            tableChecked = true;
            tableExists = ensureTableExists();
            if (!tableExists) {
                bmPlugin.getLogger().warning("bm_pending_commands table could not be created. Web kicks disabled.");
                return;
            }
        }
        
        if (!tableExists) return;

        try {
            processKickCommands();
        } catch (Exception e) {
            String msg = e.getMessage();
            // Don't spam logs for table-not-found errors
            if (msg == null || (!msg.contains("doesn't exist") && !msg.contains("bm_pending_commands"))) {
                bmPlugin.getLogger().warning("Error processing command queue: " + msg);
            }
        }
    }
    
    private boolean ensureTableExists() {
        try {
            // Try to create the table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS bm_pending_commands (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "server_id VARCHAR(50) NOT NULL DEFAULT 'ALL', " +
                "command VARCHAR(50) NOT NULL, " +
                "player_id BINARY(16) NOT NULL, " +
                "actor_id BINARY(16) NOT NULL, " +
                "args TEXT, " +
                "processed TINYINT(1) NOT NULL DEFAULT 0, " +
                "created BIGINT UNSIGNED NOT NULL, " +
                "INDEX idx_server_command (server_id, command), " +
                "INDEX idx_processed (processed)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            
            bmPlugin.getPlayerStorage().executeRawNoArgs(createTableSQL);
            bmPlugin.getLogger().info("bm_pending_commands table ready for web kicks");
            return true;
        } catch (SQLException e) {
            // Table creation failed, try to check if it exists anyway
            try {
                bmPlugin.getPlayerStorage().queryRaw("SELECT 1 FROM bm_pending_commands LIMIT 1");
                bmPlugin.getLogger().info("bm_pending_commands table exists");
                return true;
            } catch (SQLException e2) {
                bmPlugin.getLogger().warning("Could not create bm_pending_commands: " + e.getMessage());
                return false;
            }
        }
    }

    private void processKickCommands() throws SQLException {
        String selectQuery = "SELECT id, HEX(player_id), HEX(actor_id), args FROM bm_pending_commands WHERE processed = 0 AND (server_id = ? OR server_id = 'ALL') AND command = 'kick' ORDER BY created ASC LIMIT 10";
        
        GenericRawResults<String[]> results = bmPlugin.getPlayerStorage().queryRaw(selectQuery, serverId);
        
        try {
            for (String[] row : results) {
                int commandId = Integer.parseInt(row[0]);
                String playerIdStr = row[1];
                String actorIdStr = row[2];

                // Convert hex to UUID format
                if (playerIdStr != null && playerIdStr.length() == 32) {
                    playerIdStr = playerIdStr.replaceFirst(
                            "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
                            "$1-$2-$3-$4-$5");
                }
                if (actorIdStr != null && actorIdStr.length() == 32) {
                    actorIdStr = actorIdStr.replaceFirst(
                            "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
                            "$1-$2-$3-$4-$5");
                }
                String reason = row[3];
                
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    UUID actorId = UUID.fromString(actorIdStr);
                    
                    // Check if player is online
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        // Execute kick
                        PlayerData actorData = bmPlugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(actorId));
                        String actorName = actorData != null ? actorData.getName() : "Console";
                        
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            String msg = ChatColor.RED + "Kicked" + ChatColor.DARK_GRAY + ": " + ChatColor.GRAY + reason;
                            player.kickPlayer(msg);
                            bmPlugin.getLogger().info("Kicked player " + player.getName() + " by " + actorName + ": " + reason);
                        });
                    }
                    
                    // Mark as processed regardless of whether player was online
                    String updateQuery = "UPDATE bm_pending_commands SET processed = 1 WHERE id = ?";
                    bmPlugin.getPlayerStorage().executeRaw(updateQuery, String.valueOf(commandId));
                    
                } catch (Exception e) {
                    bmPlugin.getLogger().warning("Failed to process kick command " + commandId + ": " + e.getMessage());
                    
                    // Mark the failed command processed to prevent infinite retries
                    String updateQuery = "UPDATE bm_pending_commands SET processed = 1 WHERE id = ?";
                    try {
                        bmPlugin.getPlayerStorage().executeRaw(updateQuery, String.valueOf(commandId));
                    } catch (SQLException updateEx) {
                        bmPlugin.getLogger().warning("Failed to update failed command " + commandId + ": " + updateEx.getMessage());
                    }
                }
            }
        } finally {
            try {
                results.close();
            } catch (IOException e) {
                bmPlugin.getLogger().warning("Failed to close results: " + e.getMessage());
            }
        }
    }
} 