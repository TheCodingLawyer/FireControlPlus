package me.confuser.banmanager.ultixreports.database;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportCommentData;
import me.confuser.banmanager.common.data.ReportState;
import me.confuser.banmanager.ultixreports.UltrixReports;
import me.confuser.banmanager.ultixreports.UltrixReport;
import me.confuser.banmanager.ultixreports.utils.UUIDUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Database adapter that bridges TigerReports functionality with BanManager's database
 * This allows UltrixReports to use BanManager's existing player report system
 * while providing TigerReports-like GUI and functionality
 * 
 * @author confuser
 */
public class BanManagerReportAdapter {
    
    private final UltrixReports plugin;
    private final BanManagerPlugin banManager;
    
    public BanManagerReportAdapter(UltrixReports plugin) {
        this.plugin = plugin;
        this.banManager = plugin.getBanManagerPlugin();
    }
    
    /**
     * Create a new report using BanManager's system
     */
    public boolean createReport(UUID reporterUUID, String reporterName, UUID reportedUUID, String reportedName, String reason) {
        try {
            // Get or create player data for reporter and reported
            PlayerData reporter = getOrCreatePlayerData(reporterUUID, reporterName);
            PlayerData reported = getOrCreatePlayerData(reportedUUID, reportedName);
            
            if (reporter == null || reported == null) {
                plugin.getBukkitPlugin().getLogger().warning("Failed to get player data for report creation");
                return false;
            }
            
            // Get the default report state (usually "Open" or "Waiting")
            ReportState defaultState = getDefaultReportState();
            if (defaultState == null) {
                plugin.getBukkitPlugin().getLogger().warning("No default report state found");
                return false;
            }
            
            // Create the report
            PlayerReportData report = new PlayerReportData(reported, reporter, reason, defaultState);
            
            // Save to database
            boolean success = banManager.getPlayerReportStorage().report(report, false);
            
            if (success) {
                plugin.getBukkitPlugin().getLogger().info("Report created: " + reporterName + " reported " + reportedName + " for: " + reason);
            }
            
            return success;
            
        } catch (SQLException e) {
            plugin.getBukkitPlugin().getLogger().log(Level.SEVERE, "Failed to create report", e);
            return false;
        }
    }
    
    /**
     * Get all reports for display in GUI
     */
    public List<UltrixReport> getAllReports() {
        List<UltrixReport> reports = new ArrayList<>();
        
        try {
            // TODO: Implement when BanManager API method is available
            plugin.getBukkitPlugin().getLogger().info("getAllReports() - To be implemented with proper BanManager API");
            
        } catch (Exception e) {
            plugin.getBukkitPlugin().getLogger().log(Level.SEVERE, "Failed to get reports", e);
        }
        
        return reports;
    }
    
    /**
     * Get reports for a specific player
     */
    public List<UltrixReport> getReportsForPlayer(UUID playerUUID) {
        List<UltrixReport> reports = new ArrayList<>();
        
        try {
            // TODO: Implement when BanManager API method is available
            plugin.getBukkitPlugin().getLogger().info("getReportsForPlayer() - To be implemented with proper BanManager API");
            
        } catch (Exception e) {
            plugin.getBukkitPlugin().getLogger().log(Level.SEVERE, "Failed to get reports for player " + playerUUID, e);
        }
        
        return reports;
    }
    
    /**
     * Get reports by a specific player (reports they made)
     */
    public List<UltrixReport> getReportsByPlayer(UUID playerUUID) {
        List<UltrixReport> reports = new ArrayList<>();
        
        try {
            // TODO: Implement when BanManager API method is available
            plugin.getBukkitPlugin().getLogger().info("getReportsByPlayer() - To be implemented with proper BanManager API");
            
        } catch (Exception e) {
            plugin.getBukkitPlugin().getLogger().log(Level.SEVERE, "Failed to get reports by player " + playerUUID, e);
        }
        
        return reports;
    }
    
    /**
     * Process a report (mark as true, false, or abusive)
     */
    public boolean processReport(int reportId, UUID staffUUID, String staffName, String status, String staffReason) {
        try {
            PlayerReportData report = banManager.getPlayerReportStorage().queryForId(reportId);
            if (report == null) {
                return false;
            }
            
            // Get staff player data
            PlayerData staff = getOrCreatePlayerData(staffUUID, staffName);
            if (staff == null) {
                return false;
            }
            
            // Get the appropriate report state
            ReportState newState = getReportStateByName(status);
            if (newState == null) {
                newState = getDefaultReportState();
            }
            
            // Update report state
            report.setState(newState);
            banManager.getPlayerReportStorage().update(report);
            
            // Add comment if staff reason provided
            if (staffReason != null && !staffReason.trim().isEmpty()) {
                addReportComment(reportId, staffUUID, staffName, "Processed as " + status + ": " + staffReason);
            }
            
            return true;
            
        } catch (SQLException e) {
            plugin.getBukkitPlugin().getLogger().log(Level.SEVERE, "Failed to process report " + reportId, e);
            return false;
        }
    }
    
    /**
     * Add a comment to a report
     */
    public boolean addReportComment(int reportId, UUID authorUUID, String authorName, String comment) {
        try {
            PlayerReportData report = banManager.getPlayerReportStorage().queryForId(reportId);
            if (report == null) {
                return false;
            }
            
            PlayerData author = getOrCreatePlayerData(authorUUID, authorName);
            if (author == null) {
                return false;
            }
            
            PlayerReportCommentData commentData = new PlayerReportCommentData(report, author, comment);
            banManager.getPlayerReportCommentStorage().create(commentData);
            
            return true;
            
        } catch (SQLException e) {
            plugin.getBukkitPlugin().getLogger().log(Level.SEVERE, "Failed to add comment to report " + reportId, e);
            return false;
        }
    }
    
    /**
     * Convert BanManager report to UltrixReport
     */
    private UltrixReport convertBanManagerReport(PlayerReportData bmReport) {
        try {
            return new UltrixReport(
                bmReport.getId(),
                UUIDUtils.fromBytes(bmReport.getPlayer().getId()),
                bmReport.getPlayer().getName(),
                UUIDUtils.fromBytes(bmReport.getActor().getId()),
                bmReport.getActor().getName(),
                bmReport.getReason(),
                bmReport.getCreated(),
                bmReport.getState().getName(),
                bmReport.getAssignee() != null ? UUIDUtils.fromBytes(bmReport.getAssignee().getId()) : null,
                bmReport.getAssignee() != null ? bmReport.getAssignee().getName() : null
            );
        } catch (Exception e) {
            plugin.getBukkitPlugin().getLogger().log(Level.WARNING, "Failed to convert BanManager report to UltrixReport", e);
            return null;
        }
    }
    
    /**
     * Get or create player data for a UUID and name
     */
    private PlayerData getOrCreatePlayerData(UUID uuid, String name) {
        try {
            // Try to get existing player data
            PlayerData playerData = banManager.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
            
            if (playerData == null) {
                // Create new player data
                playerData = new PlayerData(uuid, name);
                banManager.getPlayerStorage().create(playerData);
            } else if (!playerData.getName().equals(name)) {
                // Update name if different
                playerData.setName(name);
                banManager.getPlayerStorage().update(playerData);
            }
            
            return playerData;
            
        } catch (SQLException e) {
            plugin.getBukkitPlugin().getLogger().log(Level.SEVERE, "Failed to get or create player data for " + uuid, e);
            return null;
        }
    }
    
    /**
     * Get the default report state (usually "Open" or "Waiting")
     */
    private ReportState getDefaultReportState() {
        try {
            List<ReportState> states = banManager.getReportStateStorage().queryForAll();
            
            // Look for "Open" or "Waiting" state first
            for (ReportState state : states) {
                if ("Open".equalsIgnoreCase(state.getName()) || "Waiting".equalsIgnoreCase(state.getName())) {
                    return state;
                }
            }
            
            // Return first available state if no default found
            return states.isEmpty() ? null : states.get(0);
            
        } catch (SQLException e) {
            plugin.getBukkitPlugin().getLogger().log(Level.SEVERE, "Failed to get default report state", e);
            return null;
        }
    }
    
    /**
     * Get report state by name
     */
    private ReportState getReportStateByName(String name) {
        try {
            List<ReportState> states = banManager.getReportStateStorage().queryForAll();
            
            for (ReportState state : states) {
                if (state.getName().equalsIgnoreCase(name)) {
                    return state;
                }
            }
            
        } catch (SQLException e) {
            plugin.getBukkitPlugin().getLogger().log(Level.SEVERE, "Failed to get report state by name: " + name, e);
        }
        
        return null;
    }
    
    /**
     * Check if BanManager is properly initialized
     */
    public boolean isInitialized() {
        return banManager != null && 
               banManager.getPlayerReportStorage() != null && 
               banManager.getPlayerStorage() != null &&
               banManager.getReportStateStorage() != null;
    }
} 