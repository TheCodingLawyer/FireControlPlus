package me.confuser.banmanager.bukkit.admingui.reports;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.ormlite.support.CompiledStatement;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager for querying Aevorin reports directly from database
 * No external dependencies - pure BanManager integration
 */
public class AevorinReportsManager {
    
    private final BanManagerPlugin plugin;
    private static final String REPORTS_TABLE = "reports";
    
    public AevorinReportsManager(BanManagerPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Get reports from Aevorin database
     * @param page Page number (starts at 1)
     * @param status Filter by status (PENDING, RESOLVED, REJECTED, or null for all)
     * @return List of report data maps
     */
    public List<AevorinReport> getReports(int page, String status) {
        List<AevorinReport> reports = new ArrayList<>();
        DatabaseConnection conn = null;
        CompiledStatement statement = null;
        DatabaseResults results = null;
        StringBuilder sql = new StringBuilder(); // Moved outside try for error logging
        
        // Ensure page is at least 1 to prevent negative offset
        if (page < 1) page = 1;
        
        try {
            conn = plugin.getLocalConn().getReadOnlyConnection(REPORTS_TABLE);
            
            // Join with minecraft_players to get usernames from UUIDs
            sql.append("SELECT r.id, r.reporter_uuid, r.reported_uuid, r.reason, r.status, r.created_at, r.server_name, ");
            sql.append("reported.player_username AS reported_username, ");
            sql.append("reporter.player_username AS reporter_username ");
            sql.append("FROM ").append(REPORTS_TABLE).append(" r ");
            sql.append("LEFT JOIN minecraft_players reported ON r.reported_uuid = reported.player_uuid ");
            sql.append("LEFT JOIN minecraft_players reporter ON r.reporter_uuid = reporter.player_uuid ");
            
            if (status != null && !status.equals("ALL")) {
                sql.append("WHERE UPPER(r.status) = UPPER('").append(status).append("') ");
                AdminGuiIntegration.getInstance().getLogger().info("Filtering reports by status: " + status);
            }
            
            sql.append("ORDER BY r.created_at DESC ");
            int offset = Math.max(0, (page - 1) * 27); // Ensure offset is never negative
            sql.append("LIMIT 27 OFFSET ").append(offset); // 27 reports per page (3 rows of 9)
            
            statement = conn.compileStatement(sql.toString(), StatementBuilder.StatementType.SELECT, 
                null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
            results = statement.runQuery(null);
            
            while (results.next()) {
                AevorinReport report = new AevorinReport();
                report.setId(results.getInt(results.findColumn("id")));
                
                // Get usernames from JOIN (may be null if player not in minecraft_players)
                String reportedName = results.getString(results.findColumn("reported_username"));
                String reporterName = results.getString(results.findColumn("reporter_username"));
                
                report.setReportedUsername(reportedName != null ? reportedName : "Unknown");
                report.setReporterUsername(reporterName != null ? reporterName : "Unknown");
                
                report.setReason(results.getString(results.findColumn("reason")));
                report.setStatus(results.getString(results.findColumn("status")));
                
                // Get timestamp - MineTrax uses TIMESTAMP (datetime)
                try {
                    // Try to get as Timestamp first
                    java.sql.Timestamp timestamp = results.getTimestamp(results.findColumn("created_at"));
                    if (timestamp != null) {
                        report.setCreatedAt(timestamp.getTime() / 1000); // Convert milliseconds to seconds
                    } else {
                        report.setCreatedAt(System.currentTimeMillis() / 1000);
                    }
                } catch (Exception e) {
                    // Fallback to current time
                    report.setCreatedAt(System.currentTimeMillis() / 1000);
                }
                
                reports.add(report);
            }
            
            AdminGuiIntegration.getInstance().getLogger().info("Fetched " + reports.size() + " reports (page " + page + ", filter: " + status + ")");
            
        } catch (Exception e) {
            AdminGuiIntegration.getInstance().getLogger().severe("Failed to fetch Aevorin reports: " + e.getMessage());
            AdminGuiIntegration.getInstance().getLogger().severe("Query was: " + (statement != null ? sql.toString() : "unknown"));
            AdminGuiIntegration.getInstance().getLogger().severe("Please check your MineTrax database reports table columns!");
            e.printStackTrace();
        } finally {
            if (results != null) {
                try { results.close(); } catch (Exception e) { /* ignore */ }
            }
            if (statement != null) {
                try { statement.close(); } catch (Exception e) { /* ignore */ }
            }
            if (conn != null) {
                try { plugin.getLocalConn().releaseConnection(conn); } catch (Exception e) { /* ignore */ }
            }
        }
        
        return reports;
    }
    
    /**
     * Get total count of reports
     */
    public int getReportsCount(String status) {
        DatabaseConnection conn = null;
        CompiledStatement statement = null;
        DatabaseResults results = null;
        int count = 0;
        
        try {
            conn = plugin.getLocalConn().getReadOnlyConnection(REPORTS_TABLE);
            
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) FROM ").append(REPORTS_TABLE);
            
            if (status != null && !status.equals("ALL")) {
                sql.append(" WHERE UPPER(status) = UPPER('").append(status).append("')");
            }
            
            statement = conn.compileStatement(sql.toString(), StatementBuilder.StatementType.SELECT, 
                null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
            results = statement.runQuery(null);
            
            if (results.next()) {
                count = results.getInt(0);
            }
            
        } catch (Exception e) {
            AdminGuiIntegration.getInstance().getLogger().warning("Failed to count Aevorin reports: " + e.getMessage());
        } finally {
            if (results != null) {
                try { results.close(); } catch (Exception e) { /* ignore */ }
            }
            if (statement != null) {
                try { statement.close(); } catch (Exception e) { /* ignore */ }
            }
            if (conn != null) {
                try { plugin.getLocalConn().releaseConnection(conn); } catch (Exception e) { /* ignore */ }
            }
        }
        
        return count;
    }
    
    /**
     * Update report status
     */
    public boolean updateReportStatus(int reportId, String newStatus) {
        DatabaseConnection conn = null;
        CompiledStatement statement = null;
        DatabaseResults verifyResults = null;
        
        try {
            conn = plugin.getLocalConn().getReadWriteConnection(REPORTS_TABLE);
            
            // Ensure status is uppercase to match database enum
            String upperStatus = newStatus.toUpperCase();
            
            // Validate status to prevent SQL injection (whitelist only)
            if (!upperStatus.equals("PENDING") && !upperStatus.equals("RESOLVED") && !upperStatus.equals("REJECTED")) {
                AdminGuiIntegration.getInstance().getLogger().warning("Invalid status: " + upperStatus);
                return false;
            }
            
            // Build UPDATE query with timestamp
            long currentTimestamp = System.currentTimeMillis() / 1000; // Unix timestamp in seconds
            String sql = "UPDATE " + REPORTS_TABLE + " SET status = '" + upperStatus +
                        "', updated_at = FROM_UNIXTIME(" + currentTimestamp + ") WHERE id = " + reportId;
            
            AdminGuiIntegration.getInstance().getLogger().info("Updating report #" + reportId + " to status: " + upperStatus);
            AdminGuiIntegration.getInstance().getLogger().info("SQL: " + sql);
            
            // Execute update and get affected rows
            statement = conn.compileStatement(sql, StatementBuilder.StatementType.UPDATE,
                    null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
            int rows = statement.runUpdate();

            // Verify with a quick SELECT
            String verifySql = "SELECT status FROM " + REPORTS_TABLE + " WHERE id = " + reportId + " LIMIT 1";
            CompiledStatement verifyStmt = conn.compileStatement(verifySql, StatementBuilder.StatementType.SELECT,
                    null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
            verifyResults = verifyStmt.runQuery(null);
            boolean updated = false;
            if (verifyResults.next()) {
                String dbStatus = verifyResults.getString(0);
                updated = upperStatus.equalsIgnoreCase(dbStatus);
            }

            AdminGuiIntegration.getInstance().getLogger().info("Update executed, rows=" + rows + ", verified=" + updated);

            return updated;
            
        } catch (Exception e) {
            AdminGuiIntegration.getInstance().getLogger().severe("Failed to update report status for ID " + reportId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (verifyResults != null) {
                try { verifyResults.close(); } catch (Exception e) { /* ignore */ }
            }
            if (statement != null) {
                try { statement.close(); } catch (Exception e) { /* ignore */ }
            }
            if (conn != null) {
                try { plugin.getLocalConn().releaseConnection(conn); } catch (Exception e) { /* ignore */ }
            }
        }
    }
    
    /**
     * Delete a report from the database
     * This deletes from both game (BanManager) and website (MineTrax)
     * since they share the same database
     */
    public boolean deleteReport(int reportId) {
        DatabaseConnection conn = null;
        CompiledStatement statement = null;
        
        try {
            conn = plugin.getLocalConn().getReadWriteConnection(REPORTS_TABLE);
            
            // First check if the report exists
            String checkSql = "SELECT id FROM " + REPORTS_TABLE + " WHERE id = " + reportId;
            CompiledStatement checkStmt = conn.compileStatement(checkSql, StatementBuilder.StatementType.SELECT,
                    null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
            DatabaseResults checkResults = checkStmt.runQuery(null);
            boolean exists = checkResults.next();
            checkResults.close();
            checkStmt.close();
            
            if (!exists) {
                AdminGuiIntegration.getInstance().getLogger().warning("Report #" + reportId + " not found in database");
                return false;
            }
            
            // Delete the report
            String sql = "DELETE FROM " + REPORTS_TABLE + " WHERE id = " + reportId;
            
            AdminGuiIntegration.getInstance().getLogger().info("Deleting report #" + reportId);
            AdminGuiIntegration.getInstance().getLogger().info("SQL: " + sql);
            
            statement = conn.compileStatement(sql, StatementBuilder.StatementType.DELETE,
                    null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
            int rows = statement.runUpdate();
            
            AdminGuiIntegration.getInstance().getLogger().info("Delete executed, rows affected: " + rows);
            
            return rows > 0;
            
        } catch (Exception e) {
            AdminGuiIntegration.getInstance().getLogger().severe("Failed to delete report #" + reportId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (statement != null) {
                try { statement.close(); } catch (Exception e) { /* ignore */ }
            }
            if (conn != null) {
                try { plugin.getLocalConn().releaseConnection(conn); } catch (Exception e) { /* ignore */ }
            }
        }
    }
}

