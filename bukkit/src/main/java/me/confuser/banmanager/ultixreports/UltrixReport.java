package me.confuser.banmanager.ultixreports;

import lombok.Getter;

import java.util.UUID;

/**
 * UltrixReport - Wrapper for BanManager reports to provide TigerReports-like functionality
 * 
 * @author confuser
 */
@Getter
public class UltrixReport {
    
    private final int id;
    private final UUID reportedUUID;
    private final String reportedName;
    private final UUID reporterUUID;
    private final String reporterName;
    private final String reason;
    private final long created;
    private final String status;
    private final UUID assigneeUUID;
    private final String assigneeName;
    
    public UltrixReport(int id, UUID reportedUUID, String reportedName, UUID reporterUUID, String reporterName, 
                       String reason, long created, String status, UUID assigneeUUID, String assigneeName) {
        this.id = id;
        this.reportedUUID = reportedUUID;
        this.reportedName = reportedName;
        this.reporterUUID = reporterUUID;
        this.reporterName = reporterName;
        this.reason = reason;
        this.created = created;
        this.status = status;
        this.assigneeUUID = assigneeUUID;
        this.assigneeName = assigneeName;
    }
    
    /**
     * Check if this report is open/waiting
     */
    public boolean isOpen() {
        return "Open".equalsIgnoreCase(status) || "Waiting".equalsIgnoreCase(status);
    }
    
    /**
     * Check if this report is closed/resolved
     */
    public boolean isClosed() {
        return "Closed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status) || "Done".equalsIgnoreCase(status);
    }
    
    /**
     * Check if this report is assigned to a staff member
     */
    public boolean isAssigned() {
        return assigneeUUID != null;
    }
    
    /**
     * Get the report age in seconds
     */
    public long getAge() {
        return System.currentTimeMillis() / 1000L - created;
    }
    
    /**
     * Get formatted creation time
     */
    public String getFormattedDate() {
        return java.time.Instant.ofEpochSecond(created).toString();
    }
    
    @Override
    public String toString() {
        return String.format("UltrixReport{id=%d, reported=%s, reporter=%s, reason='%s', status='%s'}", 
            id, reportedName, reporterName, reason, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UltrixReport that = (UltrixReport) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 