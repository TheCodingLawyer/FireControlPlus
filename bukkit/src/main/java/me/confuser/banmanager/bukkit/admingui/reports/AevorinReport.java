package me.confuser.banmanager.bukkit.admingui.reports;

/**
 * Simple data class for Aevorin report
 */
public class AevorinReport {
    private int id;
    private String reportedUsername;
    private String reporterUsername;
    private String reason;
    private String status; // PENDING, RESOLVED, REJECTED
    private long createdAt;
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getReportedUsername() {
        return reportedUsername;
    }
    
    public void setReportedUsername(String reportedUsername) {
        this.reportedUsername = reportedUsername;
    }
    
    public String getReporterUsername() {
        return reporterUsername;
    }
    
    public void setReporterUsername(String reporterUsername) {
        this.reporterUsername = reporterUsername;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get display status with color
     */
    public String getStatusDisplay() {
        switch (status) {
            case "PENDING":
                return "&e⏳ Pending";
            case "RESOLVED":
                return "&a✔ Resolved";
            case "REJECTED":
                return "&c✖ Rejected";
            default:
                return "&7? Unknown";
        }
    }
}








