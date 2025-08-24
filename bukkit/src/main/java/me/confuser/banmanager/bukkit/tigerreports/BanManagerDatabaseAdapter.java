package me.confuser.banmanager.bukkit.tigerreports;

import me.confuser.banmanager.bukkit.tigerreports.data.database.Database;
import me.confuser.banmanager.bukkit.tigerreports.data.database.QueryResult;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.ReportsPage;
import me.confuser.banmanager.bukkit.tigerreports.tasks.ResultCallback;
import me.confuser.banmanager.bukkit.tigerreports.tasks.TaskScheduler;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportCommentData;
import me.confuser.banmanager.common.data.ReportState;
import me.confuser.banmanager.common.storage.PlayerReportStorage;
import me.confuser.banmanager.common.storage.PlayerReportCommentStorage;
import me.confuser.banmanager.common.storage.PlayerStorage;
import me.confuser.banmanager.common.storage.ReportStateStorage;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Database adapter that bridges TigerReports database operations to BanManager's database system
 * This ensures all reports created through TigerReports appear in BanManager's WebUI
 */
public class BanManagerDatabaseAdapter extends Database {
    
    private final BanManagerPlugin banManagerPlugin;
    private final PlayerReportStorage reportStorage;
    private final PlayerReportCommentStorage commentStorage;
    private final PlayerStorage playerStorage;
    private final ReportStateStorage reportStateStorage;
    
    // Report state mappings from TigerReports to BanManager
    private static final Map<String, Integer> REPORT_STATE_MAPPING = new HashMap<>();
    static {
        REPORT_STATE_MAPPING.put("WAITING", 1);    // Open
        REPORT_STATE_MAPPING.put("IN_PROGRESS", 2); // In Progress  
        REPORT_STATE_MAPPING.put("DONE", 3);       // Closed
        REPORT_STATE_MAPPING.put("ABUSIVE", 4);    // Invalid/Abusive
    }
    
    public BanManagerDatabaseAdapter(BanManagerPlugin banManagerPlugin) {
        super(TigerReportsIntegration.getInstance());
        this.banManagerPlugin = banManagerPlugin;
        this.reportStorage = banManagerPlugin.getPlayerReportStorage();
        this.commentStorage = banManagerPlugin.getPlayerReportCommentStorage();
        this.playerStorage = banManagerPlugin.getPlayerStorage();
        this.reportStateStorage = banManagerPlugin.getReportStateStorage();
    }
    
    @Override
    public void initialize() {
        // BanManager database is already initialized
        // We just need to ensure report states exist
        ensureReportStatesExist();
    }
    
    @Override
    public boolean isConnectionValid() throws SQLException {
        // Use BanManager's connection validation - simplified approach
        try {
            // Just check if we can access the report storage
            return reportStorage != null && banManagerPlugin != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void ensureReportStatesExist() {
        try {
            // Ensure all required report states exist in BanManager
            String[] states = {"Open", "In Progress", "Closed", "Invalid"};
            for (String stateName : states) {
                try {
                    ReportState state = reportStateStorage.queryForFirst(
                        reportStateStorage.queryBuilder().where().eq("name", stateName).prepare()
                    );
                    if (state == null) {
                        ReportState newState = new ReportState(stateName);
                        reportStateStorage.create(newState);
                        TigerReportsIntegration.getInstance().getLogger().info("Created report state: " + stateName);
                    } else {
                        TigerReportsIntegration.getInstance().getLogger().info("Report state already exists: " + stateName + " (ID: " + state.getId() + ")");
                    }
                } catch (SQLException e) {
                    TigerReportsIntegration.getInstance().getLogger().warning("Failed to create/check report state " + stateName + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            TigerReportsIntegration.getInstance().getLogger().warning("Failed to ensure report states exist: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a new report in BanManager's database
     */
    public void createReport(UUID reportedUuid, UUID reporterUuid, String reason, String location, 
                           String messages, ResultCallback<Integer> callback) {
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                // Get or create player data
                PlayerData reportedPlayer = getOrCreatePlayer(reportedUuid);
                PlayerData reporterPlayer = getOrCreatePlayer(reporterUuid);
                
                // Get default report state (Open)
                ReportState openState = getReportState("Open");
                
                // Create the report
                PlayerReportData reportData = new PlayerReportData(reportedPlayer, reporterPlayer, reason, openState);
                
                boolean success = reportStorage.report(reportData, false);
                
                if (success) {
                    TigerReportsIntegration.getInstance().getLogger().info("Successfully created report ID: " + reportData.getId() + 
                        " - " + reportedPlayer.getName() + " reported by " + reporterPlayer.getName() + " for: " + reason);
                    if (callback != null) {
                        taskScheduler.runTask(() -> callback.onResultReceived(reportData.getId()));
                    }
                } else {
                    TigerReportsIntegration.getInstance().getLogger().warning("Failed to create report in database");
                    if (callback != null) {
                        taskScheduler.runTask(() -> callback.onResultReceived(-1));
                    }
                }
                
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to create report: " + e.getMessage());
                e.printStackTrace();
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(-1));
                }
            }
        });
    }
    
    /**
     * Get report by ID
     */
    public void getReportById(int reportId, ResultCallback<Map<String, Object>> callback) {
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                PlayerReportData report = reportStorage.queryForId(reportId);
                if (report != null) {
                    Map<String, Object> reportMap = convertReportToMap(report);
                    if (callback != null) {
                        taskScheduler.runTask(() -> callback.onResultReceived(reportMap));
                    }
                } else {
                    if (callback != null) {
                        taskScheduler.runTask(() -> callback.onResultReceived(null));
                    }
                }
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to get report by ID: " + e.getMessage());
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(null));
                }
            }
        });
    }
    
    /**
     * Get total count of reports (multiple statuses)
     */
    public void getReportsCountMultipleStatuses(String[] statuses, ResultCallback<Integer> callback) {
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                List<PlayerReportData> reports;
                
                if (statuses.length == 1 && statuses[0].equals("ALL")) {
                    reports = reportStorage.queryForAll();
                } else {
                    // Count for multiple statuses
                    List<PlayerReportData> allStatusReports = new ArrayList<>();
                    for (String status : statuses) {
                        ReportState state = getReportState(status);
                        List<PlayerReportData> statusReports = reportStorage.queryBuilder()
                            .where().eq("state_id", state.getId())
                            .query();
                        allStatusReports.addAll(statusReports);
                    }
                    reports = allStatusReports;
                }
                
                int count = reports.size();
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(count));
                }
                
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to get reports count: " + e.getMessage());
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(0));
                }
            }
        });
    }
    
    /**
     * Get total count of reports (single status - legacy method)
     */
    public void getReportsCount(String status, ResultCallback<Integer> callback) {
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                List<PlayerReportData> reports;
                
                if (status != null && !status.equals("ALL")) {
                    ReportState state = getReportState(status);
                    reports = reportStorage.queryBuilder()
                        .where().eq("state_id", state.getId())
                        .query();
                } else {
                    reports = reportStorage.queryForAll();
                }
                
                int count = reports.size();
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(count));
                }
                
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to get reports count: " + e.getMessage());
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(0));
                }
            }
        });
    }
    
    /**
     * Get reports with pagination (multiple statuses)
     */
    public void getReportsMultipleStatuses(int page, int limit, String[] statuses, ResultCallback<List<Map<String, Object>>> callback) {
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                List<PlayerReportData> reports;
                
                if (statuses.length == 1 && statuses[0].equals("ALL")) {
                    if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isReportQueryLoggingEnabled()) {
                        TigerReportsIntegration.getInstance().getLogger().info("Querying all reports");
                    }
                    reports = reportStorage.queryForAll();
                } else {
                    // Query for multiple statuses
                    if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isReportQueryLoggingEnabled()) {
                        TigerReportsIntegration.getInstance().getLogger().info("Querying reports with statuses: " + java.util.Arrays.toString(statuses));
                    }
                    
                    List<PlayerReportData> allStatusReports = new ArrayList<>();
                    for (String status : statuses) {
                        ReportState state = getReportState(status);
                        List<PlayerReportData> statusReports = reportStorage.queryBuilder()
                            .where().eq("state_id", state.getId())
                            .query();
                        allStatusReports.addAll(statusReports);
                    }
                    reports = allStatusReports;
                }
                
                if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isReportQueryLoggingEnabled()) {
                    TigerReportsIntegration.getInstance().getLogger().info("Found " + reports.size() + " reports in database");
                }
                
                // Sort by creation date (newest first)
                reports.sort((r1, r2) -> Long.compare(r2.getCreated(), r1.getCreated()));
                
                // Apply pagination
                int offset = (page - 1) * limit;
                int endIndex = Math.min(offset + limit, reports.size());
                
                List<Map<String, Object>> result = new ArrayList<>();
                for (int i = offset; i < endIndex && i < reports.size(); i++) {
                    result.add(convertReportToMap(reports.get(i)));
                }
                
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(result));
                }
                
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to get reports: " + e.getMessage());
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(new ArrayList<>()));
                }
            }
        });
    }
    
    /**
     * Get reports with pagination (single status - legacy method)
     */
    public void getReports(int page, int limit, String status, ResultCallback<List<Map<String, Object>>> callback) {
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                List<PlayerReportData> reports;
                
                if (status != null && !status.equals("ALL")) {
                    ReportState state = getReportState(status);
                    if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isReportQueryLoggingEnabled()) {
                        TigerReportsIntegration.getInstance().getLogger().info("Querying reports with status: " + status + " (State ID: " + state.getId() + ")");
                    }
                    reports = reportStorage.queryBuilder()
                        .where().eq("state_id", state.getId())
                        .query();
                } else {
                    if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isReportQueryLoggingEnabled()) {
                        TigerReportsIntegration.getInstance().getLogger().info("Querying all reports");
                    }
                    reports = reportStorage.queryForAll();
                }
                
                if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isReportQueryLoggingEnabled()) {
                    TigerReportsIntegration.getInstance().getLogger().info("Found " + reports.size() + " reports in database");
                }
                
                // Apply pagination
                int offset = (page - 1) * limit;
                int endIndex = Math.min(offset + limit, reports.size());
                
                List<Map<String, Object>> result = new ArrayList<>();
                for (int i = offset; i < endIndex && i < reports.size(); i++) {
                    result.add(convertReportToMap(reports.get(i)));
                }
                
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(result));
                }
                
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to get reports: " + e.getMessage());
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(new ArrayList<>()));
                }
            }
        });
    }
    
    /**
     * Update report status
     */
    public void updateReportStatus(int reportId, String status, ResultCallback<Boolean> callback) {
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                PlayerReportData report = reportStorage.queryForId(reportId);
                if (report != null) {
                    ReportState newState = getReportState(status);
                    report.setState(newState);
                    
                    int updated = reportStorage.update(report);
                    boolean success = updated > 0;
                    
                    if (callback != null) {
                        taskScheduler.runTask(() -> callback.onResultReceived(success));
                    }
                } else {
                    if (callback != null) {
                        taskScheduler.runTask(() -> callback.onResultReceived(false));
                    }
                }
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to update report status: " + e.getMessage());
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(false));
                }
            }
        });
    }
    
    /**
     * Add comment to report
     */
    public void addReportComment(int reportId, UUID commenterUuid, String comment, ResultCallback<Boolean> callback) {
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                PlayerReportData report = reportStorage.queryForId(reportId);
                PlayerData commenter = getOrCreatePlayer(commenterUuid);
                
                if (report != null && commenter != null) {
                    PlayerReportCommentData commentData = new PlayerReportCommentData(report, commenter, comment);
                    
                    int created = banManagerPlugin.getPlayerReportCommentStorage().create(commentData);
                    boolean success = created > 0;
                    
                    if (callback != null) {
                        taskScheduler.runTask(() -> callback.onResultReceived(success));
                    }
                } else {
                    if (callback != null) {
                        taskScheduler.runTask(() -> callback.onResultReceived(false));
                    }
                }
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to add report comment: " + e.getMessage());
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(false));
                }
            }
        });
    }
    
    private PlayerData getOrCreatePlayer(UUID uuid) throws SQLException {
        PlayerData player = playerStorage.queryForId(UUIDUtils.toBytes(uuid));
        if (player == null) {
            // Create a temporary player entry - BanManager will update it when player joins
            player = new PlayerData(uuid, "Unknown");
            playerStorage.create(player);
        }
        return player;
    }
    
    private ReportState getReportState(String stateName) throws SQLException {
        ReportState state = reportStateStorage.queryForFirst(
            reportStateStorage.queryBuilder().where().eq("name", stateName).prepare()
        );
        if (state == null) {
            // Create the state if it doesn't exist
            state = new ReportState(stateName);
            reportStateStorage.create(state);
        }
        return state;
    }
    
    private Map<String, Object> convertReportToMap(PlayerReportData report) {
        Map<String, Object> map = new HashMap<>();
        map.put("report_id", report.getId());
        map.put("reported_uuid", report.getPlayer().getUUID().toString());
        map.put("reported_name", report.getPlayer().getName());
        map.put("reporter_uuid", report.getActor().getUUID().toString());
        map.put("reporter_name", report.getActor().getName());
        map.put("reason", report.getReason());
        map.put("status", report.getState().getName());
        // Convert from seconds to milliseconds for proper date handling
        map.put("date", report.getCreated() * 1000L);
        map.put("updated", report.getUpdated() * 1000L);
        map.put("archived", false); // TigerReports concept, map to BanManager's closed state
        
        // Additional fields that TigerReports expects
        map.put("appreciation", "NONE");
        map.put("reported_ip", "");
        map.put("reported_location", "");
        map.put("reported_messages", "");
        map.put("reported_gamemode", "");
        map.put("reported_on_ground", false);
        map.put("reported_sneak", false);
        map.put("reported_sprint", false);
        map.put("reported_health", 20.0);
        map.put("reported_food", 20);
        map.put("reported_effects", "");
        map.put("reporter_ip", "");
        map.put("reporter_location", "");
        map.put("reporter_messages", "");
        
        return map;
    }
    
    @Override
    public void closeConnection() {
        // Don't close BanManager's connection
    }
    
    @Override
    public void startClosing() {
        // Don't close BanManager's connection
    }
    
    // Implement required abstract methods with BanManager integration
    @Override
    public void query(String sql, List<Object> parameters, ResultCallback<QueryResult> callback) {
        // For complex queries, we'll need to implement this using BanManager's connection
        // For now, most operations should use the specific methods above
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                // This would need to be implemented for complex TigerReports queries
                // Most operations should use the specific methods above instead
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(new QueryResult()));
                }
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to execute query: " + e.getMessage());
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(null));
                }
            }
        });
    }
    
    @Override
    public void update(String sql, List<Object> parameters, ResultCallback<Integer> callback) {
        // Similar to query method - implement for specific TigerReports update operations
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(0));
                }
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to execute update: " + e.getMessage());
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(0));
                }
            }
        });
    }
    
    /**
     * Delete a report permanently
     */
    public void deleteReport(int reportId, ResultCallback<Boolean> callback) {
        taskScheduler.runTaskAsynchronously(() -> {
            try {
                // Get the report first to log the deletion
                PlayerReportData report = reportStorage.queryForId(reportId);
                if (report != null) {
                    // Delete the report
                    int deleted = reportStorage.deleteById(reportId);
                    boolean success = deleted > 0;
                    
                                    if (success) {
                    TigerReportsIntegration.getInstance().getLogger().info("Deleted report ID: " + reportId +
                        " - " + report.getPlayer().getName() + " reported by " + report.getActor().getName());
                    
                    // Trigger live menu updates
                    taskScheduler.runTask(() -> {
                        TigerReportsIntegration.getInstance().getMenuUpdateManager().updateMenusForReport(reportId);
                    });
                }

                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(success));
                }
                } else {
                    TigerReportsIntegration.getInstance().getLogger().warning("Cannot delete report ID: " + reportId + " - report not found");
                    if (callback != null) {
                        taskScheduler.runTask(() -> callback.onResultReceived(false));
                    }
                }
                    } catch (Exception e) {
            TigerReportsIntegration.getInstance().getLogger().severe("Failed to delete report: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(false));
            }
        }
    });
}

// Get reports by reporter (player who made the reports)
public void getReportsByReporter(String reporterName, boolean archived, int page, int pageSize, 
                                ResultCallback<ReportsPage> callback) {
    taskScheduler.runTaskAsynchronously(() -> {
        try {
            // Simplified approach - get all reports and filter by reporter name
            List<PlayerReportData> allReports = reportStorage.queryForAll();
            List<PlayerReportData> filteredReports = new ArrayList<>();
            
            for (PlayerReportData report : allReports) {
                if (report.getActor().getName().equals(reporterName)) {
                    filteredReports.add(report);
                }
            }
            
            // Sort by creation date (newest first)
            filteredReports.sort((a, b) -> Long.compare(b.getCreated(), a.getCreated()));
            
            // Apply pagination
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, filteredReports.size());
            List<PlayerReportData> pageReports = filteredReports.subList(startIndex, endIndex);

            List<Map<String, Object>> reportMaps = new ArrayList<>();
            for (PlayerReportData report : pageReports) {
                reportMaps.add(convertReportToMap(report));
            }

            ReportsPage reportsPage = new ReportsPage(reportMaps, page, pageSize, filteredReports.size());
            
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(reportsPage));
            }
        } catch (Exception e) {
            TigerReportsIntegration.getInstance().getLogger().severe("Failed to get reports by reporter: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(new ReportsPage(new ArrayList<>(), page, pageSize)));
            }
        }
    });
}

// Get reports against player (player who was reported)
public void getReportsAgainstPlayer(String reportedName, boolean archived, int page, int pageSize, 
                                   ResultCallback<ReportsPage> callback) {
    taskScheduler.runTaskAsynchronously(() -> {
        try {
            // Simplified approach - get all reports and filter by reported player name
            List<PlayerReportData> allReports = reportStorage.queryForAll();
            List<PlayerReportData> filteredReports = new ArrayList<>();
            
            for (PlayerReportData report : allReports) {
                if (report.getPlayer().getName().equals(reportedName)) {
                    filteredReports.add(report);
                }
            }
            
            // Sort by creation date (newest first)
            filteredReports.sort((a, b) -> Long.compare(b.getCreated(), a.getCreated()));
            
            // Apply pagination
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, filteredReports.size());
            List<PlayerReportData> pageReports = filteredReports.subList(startIndex, endIndex);

            List<Map<String, Object>> reportMaps = new ArrayList<>();
            for (PlayerReportData report : pageReports) {
                reportMaps.add(convertReportToMap(report));
            }

            ReportsPage reportsPage = new ReportsPage(reportMaps, page, pageSize, filteredReports.size());
            
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(reportsPage));
            }
        } catch (Exception e) {
            TigerReportsIntegration.getInstance().getLogger().severe("Failed to get reports against player: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(new ReportsPage(new ArrayList<>(), page, pageSize)));
            }
        }
    });
}

// Comment management methods
public void createComment(int reportId, UUID authorUuid, String authorName, String message, 
                         me.confuser.banmanager.bukkit.tigerreports.tasks.ResultCallback<me.confuser.banmanager.bukkit.tigerreports.objects.Comment> callback) {
    taskScheduler.runTaskAsynchronously(() -> {
        try {
            // Get or create author player
            PlayerData author = getOrCreatePlayer(authorUuid);
            
            // Get the report
            PlayerReportData report = reportStorage.queryForId(reportId);
            if (report == null) {
                TigerReportsIntegration.getInstance().getLogger().warning("Cannot create comment for report ID: " + reportId + " - report not found");
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(null));
                }
                return;
            }
            
            // Create comment
            PlayerReportCommentData commentData = new PlayerReportCommentData(
                report, author, message
            );
            
            commentStorage.create(commentData);
            
            // Create Comment object
            me.confuser.banmanager.bukkit.tigerreports.objects.Comment comment = 
                new me.confuser.banmanager.bukkit.tigerreports.objects.Comment(
                    commentData.getId(), reportId, authorUuid, authorName, message, 
                    commentData.getCreated() * 1000L, false
                );
            
            TigerReportsIntegration.getInstance().getLogger().info("Created comment for report ID: " + reportId + " by " + authorName);
            
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(comment));
            }
        } catch (Exception e) {
            TigerReportsIntegration.getInstance().getLogger().severe("Failed to create comment: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(null));
            }
        }
    });
}

public void getComments(int reportId, int page, int pageSize, 
                       me.confuser.banmanager.bukkit.tigerreports.tasks.ResultCallback<java.util.List<me.confuser.banmanager.bukkit.tigerreports.objects.Comment>> callback) {
    taskScheduler.runTaskAsynchronously(() -> {
        try {
            List<PlayerReportCommentData> commentData = commentStorage.queryBuilder()
                .where().eq("report_id", reportId)
                .query();
            
            List<me.confuser.banmanager.bukkit.tigerreports.objects.Comment> comments = new ArrayList<>();
            for (PlayerReportCommentData data : commentData) {
                me.confuser.banmanager.bukkit.tigerreports.objects.Comment comment = 
                    new me.confuser.banmanager.bukkit.tigerreports.objects.Comment(
                        data.getId(), reportId, data.getActor().getUUID(), data.getActor().getName(),
                        data.getComment(), data.getCreated() * 1000L, false // TODO: Add sent status to database
                    );
                comments.add(comment);
            }
            
            // Sort by creation date (newest first)
            comments.sort((a, b) -> Long.compare(b.getCreated(), a.getCreated()));
            
            // Apply pagination
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, comments.size());
            List<me.confuser.banmanager.bukkit.tigerreports.objects.Comment> pageComments = 
                comments.subList(startIndex, endIndex);
            
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(pageComments));
            }
        } catch (Exception e) {
            TigerReportsIntegration.getInstance().getLogger().severe("Failed to get comments: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(new ArrayList<>()));
            }
        }
    });
}

public void updateComment(int commentId, String newMessage, ResultCallback<Boolean> callback) {
    taskScheduler.runTaskAsynchronously(() -> {
        try {
            // For now, comment editing is not supported since PlayerReportCommentData doesn't have setters
            // This would require database schema changes to support comment editing
            TigerReportsIntegration.getInstance().getLogger().warning("Comment editing not yet supported - would need database schema changes");
            
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(false));
            }
        } catch (Exception e) {
            TigerReportsIntegration.getInstance().getLogger().severe("Failed to update comment: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(false));
            }
        }
    });
}

public void deleteComment(int commentId, ResultCallback<Boolean> callback) {
    taskScheduler.runTaskAsynchronously(() -> {
        try {
            PlayerReportCommentData comment = commentStorage.queryForId(commentId);
            if (comment != null) {
                int deleted = commentStorage.deleteById(commentId);
                boolean success = deleted > 0;
                
                if (success) {
                    TigerReportsIntegration.getInstance().getLogger().info("Deleted comment ID: " + commentId);
                }
                
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(success));
                }
            } else {
                TigerReportsIntegration.getInstance().getLogger().warning("Cannot delete comment ID: " + commentId + " - comment not found");
                if (callback != null) {
                    taskScheduler.runTask(() -> callback.onResultReceived(false));
                }
            }
        } catch (Exception e) {
            TigerReportsIntegration.getInstance().getLogger().severe("Failed to delete comment: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                taskScheduler.runTask(() -> callback.onResultReceived(false));
            }
        }
    });
}

public void updateCommentSentStatus(int commentId, boolean sent, ResultCallback<Boolean> callback) {
    // For now, just return success since we don't have sent status in database yet
    // This can be enhanced later by adding a sent column to the comments table
    if (callback != null) {
        taskScheduler.runTask(() -> callback.onResultReceived(true));
    }
}
}
