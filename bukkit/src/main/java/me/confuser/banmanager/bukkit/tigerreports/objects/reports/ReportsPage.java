package me.confuser.banmanager.bukkit.tigerreports.objects.reports;

import java.util.List;
import java.util.Map;

/**
 * Reports page object for TigerReports integration
 * Adapted from original TigerReports ReportsPage class
 */
public class ReportsPage {
    
    private final List<Map<String, Object>> reports;
    private final int page;
    private final int pageSize;
    private final int totalReports;
    
    public ReportsPage(List<Map<String, Object>> reports, int page, int pageSize) {
        this.reports = reports;
        this.page = page;
        this.pageSize = pageSize;
        this.totalReports = -1; // Unknown total, use old logic
    }
    
    public ReportsPage(List<Map<String, Object>> reports, int page, int pageSize, int totalReports) {
        this.reports = reports;
        this.page = page;
        this.pageSize = pageSize;
        this.totalReports = totalReports;
    }
    
    public List<Map<String, Object>> getReports() {
        return reports;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public boolean hasNextPage() {
        if (totalReports >= 0) {
            // Use total count if available
            int totalPages = (int) Math.ceil((double) totalReports / pageSize);
            return page < totalPages;
        } else {
            // Fallback to old logic if total is unknown
            return reports.size() >= pageSize;
        }
    }
    
    public boolean hasPreviousPage() {
        return page > 1;
    }
    
    public int getTotalReports() {
        return totalReports;
    }
    
    public int getReportIdAtIndex(int index) {
        if (index >= 0 && index < reports.size()) {
            Map<String, Object> report = reports.get(index);
            return (Integer) report.get("report_id");
        }
        return -1;
    }
}
