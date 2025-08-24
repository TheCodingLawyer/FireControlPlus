package me.confuser.banmanager.bungee.listeners;

import me.confuser.banmanager.bungee.BMBungeePlugin;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ormlite.dao.GenericRawResults;
import me.confuser.banmanager.common.util.UUIDUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class CommandQueueListener implements Runnable {
    private final BMBungeePlugin bungeePlugin;
    private final BanManagerPlugin plugin;
    private final String serverId;

    public CommandQueueListener(BMBungeePlugin bungeePlugin, BanManagerPlugin plugin) {
        this.plugin = plugin;
        this.bungeePlugin = bungeePlugin;
        // Read server id from environment or default to 'ALL'
        String envServerId = System.getenv("BM_SERVER_ID");
        this.serverId = (envServerId == null || envServerId.isEmpty()) ? "ALL" : envServerId;
    }

    @Override
    public void run() {
        if (plugin == null) return;

        try {
            processKickCommands();
        } catch (Exception e) {
            plugin.getLogger().warning("Error processing command queue: " + e.getMessage());
        }
    }

    private void processKickCommands() throws SQLException {
        String selectQuery = "SELECT id, HEX(player_id), HEX(actor_id), args FROM bm_pending_commands WHERE processed = 0 AND (server_id = ? OR server_id = 'ALL') AND command = 'kick' ORDER BY created ASC LIMIT 10";
        
        GenericRawResults<String[]> results = plugin.getPlayerStorage().queryRaw(selectQuery, serverId);
        
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
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
                    if (player != null && player.isConnected()) {
                        // Execute kick
                        PlayerData actorData = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(actorId));
                        String actorName = actorData != null ? actorData.getName() : "Console";
                        
                                                    ProxyServer.getInstance().getScheduler().runAsync(bungeePlugin, () -> {
                            String msg = ChatColor.RED + "Kicked" + ChatColor.DARK_GRAY + ": " + ChatColor.GRAY + reason;
                            player.disconnect(TextComponent.fromLegacyText(msg));
                            plugin.getLogger().info("Kicked player " + player.getName() + " by " + actorName + ": " + reason);
                        });
                    }
                    
                    // Mark as processed regardless of whether player was online
                    String updateQuery = "UPDATE bm_pending_commands SET processed = 1 WHERE id = ?";
                    plugin.getPlayerStorage().executeRaw(updateQuery, String.valueOf(commandId));
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to process kick command " + commandId + ": " + e.getMessage());
                    
                    // Mark the failed command processed to prevent infinite retries
                    String updateQuery = "UPDATE bm_pending_commands SET processed = 1 WHERE id = ?";
                    try {
                        plugin.getPlayerStorage().executeRaw(updateQuery, String.valueOf(commandId));
                    } catch (SQLException updateEx) {
                        plugin.getLogger().warning("Failed to update failed command " + commandId + ": " + updateEx.getMessage());
                    }
                }
            }
        } finally {
            try {
                results.close();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to close results: " + e.getMessage());
            }
        }
    }
} 