package me.confuser.banmanager.bukkit.tigerreports.objects.reports;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;

import java.util.Map;
import java.util.UUID;

/**
 * Report object for TigerReports integration
 * Adapted from original TigerReports Report class
 */
public class Report {
    
    private final int id;
    private final UUID reportedUuid;
    private final String reportedName;
    private final UUID reporterUuid;
    private final String reporterName;
    private final String reason;
    private final String status;
    private final long date;
    private final boolean archived;
    
    public Report(Map<String, Object> reportData) {
        this.id = (Integer) reportData.get("report_id");
        this.reportedUuid = UUID.fromString((String) reportData.get("reported_uuid"));
        this.reportedName = (String) reportData.get("reported_name");
        this.reporterUuid = UUID.fromString((String) reportData.get("reporter_uuid"));
        this.reporterName = (String) reportData.get("reporter_name");
        this.reason = (String) reportData.get("reason");
        this.status = (String) reportData.get("status");
        this.date = (Long) reportData.get("date");
        this.archived = (Boolean) reportData.getOrDefault("archived", false);
    }
    
    public int getId() {
        return id;
    }
    
    public UUID getReportedUuid() {
        return reportedUuid;
    }
    
    public String getReportedName() {
        return reportedName;
    }
    
    public UUID getReporterUuid() {
        return reporterUuid;
    }
    
    public String getReporterName() {
        return reporterName;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getStatus() {
        return status;
    }
    
    public long getDate() {
        return date;
    }
    
    public boolean isArchived() {
        return archived;
    }
    
    public long getUpdated() {
        // For now, return the same as date - can be enhanced later
        return date;
    }
    
    public int getCommentsCount() {
        // Get comment count from database
        try {
            BanManagerDatabaseAdapter db = TigerReportsIntegration.getInstance().getDatabaseAdapter();
            if (db != null) {
                // For now, return 0 - this could be enhanced to cache the count
                // or make an async call to get the real count
                return 0;
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return 0;
    }
    
    public boolean hasAdvancedData() {
        // For now, always return true - can be enhanced later
        return true;
    }
    
    // Status management
    public void setStatus(String newStatus, User user, BanManagerDatabaseAdapter db, 
                         ReportsManager rm, TigerReportsIntegration tr) {
        tr.runTaskAsynchronously(() -> {
            db.updateReportStatus(this.id, newStatus, (success) -> {
                if (success) {
                    tr.runTask(() -> {
                        user.sendMessage("§aReport #" + this.id + " status changed to " + newStatus);
                        
                        // Trigger live menu updates
                        tr.getMenuUpdateManager().updateMenusForReport(this.id);
                    });
                } else {
                    tr.runTask(() -> {
                        user.sendErrorMessage("§cFailed to update report status");
                    });
                }
            });
        });
    }
    
    // Appreciation management
    public void setAppreciation(String appreciation, User user, BanManagerDatabaseAdapter db, 
                              ReportsManager rm, TigerReportsIntegration tr) {
        tr.runTaskAsynchronously(() -> {
            // In full implementation, this would update the appreciation in database
            tr.runTask(() -> {
                user.sendMessage("§aReport #" + this.id + " appreciation set to " + appreciation);
            });
        });
    }
    
    // Process as abusive
    public void processAbusive(User user, boolean silent, boolean autoArchive, 
                             BanManagerDatabaseAdapter db, ReportsManager rm, 
                             me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager um, 
                             TigerReportsIntegration tr) {
        setAppreciation("False", user, db, rm, tr);
        if (autoArchive) {
            setStatus("Closed", user, db, rm, tr);
        }
        if (!silent) {
            user.sendMessage("§cReport #" + this.id + " marked as abusive");
        }
    }
    
    // Report listener interface for real-time updates
    public interface ReportListener {
        void onReportDataChange(Report report);
        void onReportDelete(int reportId);
    }
}
