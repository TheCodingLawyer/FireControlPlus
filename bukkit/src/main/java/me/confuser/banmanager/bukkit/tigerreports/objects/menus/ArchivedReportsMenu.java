package me.confuser.banmanager.bukkit.tigerreports.objects.menus;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.data.config.Message;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.MenuItem;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.ReportsCharacteristics;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.ReportsPage;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Archived reports menu for TigerReports integration
 * Adapted from original TigerReports ArchivedReportsMenu class
 */
public class ArchivedReportsMenu extends Menu implements UpdatedMenu {
    
    private final ReportsManager rm;
    private final BanManagerDatabaseAdapter db;
    private final TigerReportsIntegration tr;
    private final VaultManager vm;
    private final Object bm; // BungeeManager placeholder
    private final UsersManager um;
    
    private ReportsPage reportsPage;
    
    public ArchivedReportsMenu(User u, int page, ReportsManager rm, BanManagerDatabaseAdapter db,
                              TigerReportsIntegration tr, VaultManager vm, Object bm, UsersManager um) {
        super(u, 54, page, Permission.STAFF_ARCHIVE);
        this.rm = rm;
        this.db = db;
        this.tr = tr;
        this.vm = vm;
        this.bm = bm;
        this.um = um;
    }
    
    @Override
    public Inventory onOpen() {
        Inventory inv = getInventory(Message.ARCHIVED_REPORTS_TITLE.get(), true);
        
        // Back button to main reports
        inv.setItem(0, MenuItem.BACK.get());
        
        // Load archived reports page asynchronously
        rm.getReportsPageAsynchronously(page, ReportsCharacteristics.ARCHIVED_REPORTS, db, tr, um, (reportsPage) -> {
            this.reportsPage = reportsPage;
            tr.runTask(() -> update(false));
        });
        
        return inv;
    }
    
    @Override
    public void onUpdate(Inventory inv) {
        if (reportsPage != null) {
            rm.fillInventoryWithReportsPage(
                inv,
                reportsPage,
                Message.REPORT_SHOW_ACTION.get(),
                false, // Can't archive already archived reports
                u.hasPermission(Permission.STAFF_DELETE) ? Message.REPORT_DELETE_ACTION.get() : "",
                vm,
                bm
            );
            
            // Add navigation buttons
            if (reportsPage.hasPreviousPage()) {
                inv.setItem(45, MenuItem.PREVIOUS_PAGE.get());
            }
            if (reportsPage.hasNextPage()) {
                inv.setItem(53, MenuItem.NEXT_PAGE.get());
            }
        }
    }
    
    @Override
    public void onClick(ItemStack item, int slot, ClickType click) {
        if (slot == 0) {
            // Back to main reports
            u.openReportsMenu(1, true, rm, db, tr, vm, bm, um);
        } else if (slot == 45 && reportsPage != null && reportsPage.hasPreviousPage()) {
            // Previous page
            setPage(getPage() - 1);
            u.openArchivedReportsMenu(getPage(), true, rm, db, tr, vm, bm, um);
        } else if (slot == 53 && reportsPage != null && reportsPage.hasNextPage()) {
            // Next page
            setPage(getPage() + 1);
            u.openArchivedReportsMenu(getPage(), true, rm, db, tr, vm, bm, um);
        } else if (slot >= 18 && slot <= size - 10 && reportsPage != null) {
            // Report clicked
            int reportId = reportsPage.getReportIdAtIndex(slot - 18);
            
            if (reportId == -1) {
                update(false);
            } else {
                rm.getReportByIdAsynchronously(reportId, true, true, db, tr, um, (report) -> {
                    if (report != null) {
                        // Open report menu
                        u.sendMessage("Opening archived report #" + reportId); // Placeholder
                    } else {
                        u.sendErrorMessage(Message.REPORT_NOT_FOUND.get());
                    }
                });
            }
        }
    }
}
