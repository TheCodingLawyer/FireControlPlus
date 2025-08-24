package me.confuser.banmanager.velocity.listeners;

import com.velocitypowered.api.proxy.ProxyServer;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ormlite.dao.GenericRawResults;
import me.confuser.banmanager.common.util.UUIDUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class CommandQueueListener implements Runnable {
    private final ProxyServer server;
    private final BanManagerPlugin plugin;
    private final String serverId;

    public CommandQueueListener(ProxyServer server, BanManagerPlugin plugin) {
        this.server = server;
        this.plugin = plugin;
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
                    Optional<com.velocitypowered.api.proxy.Player> playerOpt = server.getPlayer(playerId);
                    if (playerOpt.isPresent()) {
                        com.velocitypowered.api.proxy.Player player = playerOpt.get();
                        
                        // Execute kick through proper BanManager storage to fire PlayerKickedEvent
                        PlayerData actorData = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(actorId));
                        PlayerData playerData = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(playerId));
                        
                        if (actorData != null && playerData != null) {
                            plugin.getScheduler().runAsync(() -> {
                                try {
                                    // Create kick data and add through storage (fires PlayerKickedEvent)
                                    me.confuser.banmanager.common.data.PlayerKickData kickData = 
                                        new me.confuser.banmanager.common.data.PlayerKickData(playerData, actorData, reason);
                                    plugin.getPlayerKickStorage().addKick(kickData, false);
                                    
                                    // The kick message and disconnect will be handled by BanManager's kick system
                                    plugin.getLogger().info("Kicked player " + player.getUsername() + " by " + actorData.getName() + ": " + reason);
                                } catch (Exception e) {
                                    plugin.getLogger().warning("Failed to process kick through storage: " + e.getMessage());
                                    // Fallback to direct disconnect
                                    Component msg = Component.text("Kicked", NamedTextColor.RED)
                                        .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                                        .append(Component.text(reason, NamedTextColor.GRAY));
                                    player.disconnect(msg);
                                }
                            });
                        } else {
                            // Fallback if player/actor data not found
                            plugin.getScheduler().runAsync(() -> {
                                Component msg = Component.text("Kicked", NamedTextColor.RED)
                                    .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                                    .append(Component.text(reason, NamedTextColor.GRAY));
                                player.disconnect(msg);
                                plugin.getLogger().info("Kicked player " + player.getUsername() + " (fallback): " + reason);
                            });
                        }
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