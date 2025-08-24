package me.confuser.banmanager.bukkit.tigerreports.objects.menus;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.Report;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import me.confuser.banmanager.bukkit.tigerreports.tasks.ResultCallback;

/**
 * Base class for report management menus that need to load and display specific reports
 */
public abstract class ReportManagerMenu extends Menu implements Report.ReportListener {
    
    protected final int reportId;
    protected final boolean withAdvancedData;
    protected final boolean allowAccessIfArchived;
    protected Report r = null;
    protected boolean reportCollectionRequested = false;
    protected final ReportsManager rm;
    protected final BanManagerDatabaseAdapter db;
    protected final TigerReportsIntegration tr;
    protected final UsersManager um;
    
    public ReportManagerMenu(User u, int size, int page, Permission permission, int reportId,
            ReportsManager rm, BanManagerDatabaseAdapter db, TigerReportsIntegration tr, UsersManager um) {
        this(u, size, page, permission, reportId, false, rm, db, tr, um);
    }
    
    public ReportManagerMenu(User u, int size, int page, Permission permission, int reportId,
            boolean allowAccessIfArchived, ReportsManager rm, BanManagerDatabaseAdapter db,
            TigerReportsIntegration tr, UsersManager um) {
        this(u, size, page, permission, reportId, false, allowAccessIfArchived, rm, db, tr, um);
    }
    
    public ReportManagerMenu(User u, int size, int page, Permission permission, int reportId,
            boolean withAdvancedData, boolean allowAccessIfArchived, ReportsManager rm, 
            BanManagerDatabaseAdapter db, TigerReportsIntegration tr, UsersManager um) {
        super(u, size, page, permission);
        this.reportId = reportId;
        this.withAdvancedData = withAdvancedData;
        this.allowAccessIfArchived = allowAccessIfArchived;
        this.rm = rm;
        this.db = db;
        this.tr = tr;
        this.um = um;
    }
    
    @Override
    public void open(boolean sound) {
        if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isMenuOperationLoggingEnabled()) {
            TigerReportsIntegration.getInstance().getLogger().info("ReportManagerMenu: opening report " + reportId);
        }
        rm.addReportListener(reportId, this);
        
        if (isValidReport(r)) {
            if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isMenuOperationLoggingEnabled()) {
                TigerReportsIntegration.getInstance().getLogger().info("ReportManagerMenu: valid report " + reportId + ", opening...");
            }
            super.open(sound);
        } else if (!reportCollectionRequested) {
            reportCollectionRequested = true;
            if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isMenuOperationLoggingEnabled()) {
                TigerReportsIntegration.getInstance().getLogger().info("ReportManagerMenu: start collection of report " + reportId);
            }
            
            rm.getReportByIdAsynchronously(reportId, withAdvancedData, true, db, tr, um, new ResultCallback<Report>() {
                @Override
                public void onResultReceived(Report r) {
                    ReportManagerMenu.this.r = r;
                    if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isMenuOperationLoggingEnabled()) {
                        TigerReportsIntegration.getInstance().getLogger().info("ReportManagerMenu: report " + reportId + " collected, opening...");
                    }
                    ReportManagerMenu.super.open(sound);
                    reportCollectionRequested = false;
                }
            });
        }
    }
    
    private boolean isValidReport(Report r) {
        return r != null && (!withAdvancedData || r.hasAdvancedData());
    }
    
    @Override
    public void onReportDataChange(Report r) {
        if (reportId == r.getId()) {
            if (!isValidReport(this.r)) {
                if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isMenuOperationLoggingEnabled()) {
                    TigerReportsIntegration.getInstance().getLogger().info("ReportManagerMenu: onReportDataChanged(" + r.getId() + "): menu report is not (yet) valid, no update");
                }
                return;
            }
            
            if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isMenuOperationLoggingEnabled()) {
                TigerReportsIntegration.getInstance().getLogger().info("ReportManagerMenu: onReportDataChanged(" + r.getId() + "): user = " + u.getName() + ", calls update()");
            }
            update(false);
        }
    }
    
    @Override
    public void onReportDelete(int reportId) {
        if (this.reportId == reportId) {
            if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isMenuOperationLoggingEnabled()) {
                TigerReportsIntegration.getInstance().getLogger().info("ReportManagerMenu: onReportDeleted(" + reportId + "): user = " + u.getName());
            }
            u.sendErrorMessage("This report has been deleted.");
            p.closeInventory();
        }
    }
    
    protected String checkReport() {
        if (!isValidReport(r)) {
            return "Invalid report.";
        } else if (!u.canAccessToReport(r, allowAccessIfArchived)) {
            return "You don't have permission to access this report.";
        } else {
            return null;
        }
    }
    
    public ReportManagerMenu setReport(Report r) {
        if (isValidReport(r)) {
            this.r = r;
        }
        return this;
    }
    
    public void onClose() {
        if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isMenuOperationLoggingEnabled()) {
            TigerReportsIntegration.getInstance().getLogger().info("ReportManagerMenu: onClose(): remove listener for report " + reportId);
        }
        rm.removeReportListener(reportId, this);
        // Note: Parent Menu class doesn't have onClose method
    }
}
