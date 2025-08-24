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
 * Reports menu for TigerReports integration
 * Adapted from original TigerReports ReportsMenu class
 */
public class ReportsMenu extends Menu implements UpdatedMenu {
    
    private final ReportsManager rm;
    private final BanManagerDatabaseAdapter db;
    private final TigerReportsIntegration tr;
    private final VaultManager vm;
    private final Object bm; // BungeeManager placeholder
    private final UsersManager um;
    
    private ReportsPage reportsPage;
    
    public ReportsMenu(User u, int page, ReportsManager rm, BanManagerDatabaseAdapter db,
                      TigerReportsIntegration tr, VaultManager vm, Object bm, UsersManager um) {
        super(u, 54, page, Permission.STAFF);
        this.rm = rm;
        this.db = db;
        this.tr = tr;
        this.vm = vm;
        this.bm = bm;
        this.um = um;
    }
    
    @Override
    public Inventory onOpen() {
        Inventory inv = getInventory(Message.REPORTS_TITLE.get(), true);
        
        if (u.hasPermission(Permission.STAFF_ARCHIVE)) {
            inv.setItem(8, MenuItem.ARCHIVED_REPORTS.getWithDetails(Message.ARCHIVED_REPORTS_DETAILS.get()));
        }
        
        // Load reports page asynchronously
        rm.getReportsPageAsynchronously(page, ReportsCharacteristics.CURRENT_REPORTS, db, tr, um, (reportsPage) -> {
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
                u.hasPermission(Permission.STAFF_ARCHIVE),
                u.hasPermission(Permission.STAFF_DELETE) ? Message.REPORT_DELETE_ACTION.get() : "",
                vm,
                bm
            );
            
            // Add navigation buttons
            if (reportsPage.hasPreviousPage()) {
                inv.setItem(45, MenuItem.PREVIOUS_PAGE.get());
                me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration.getInstance().getLogger().info("Added previous page button (page " + reportsPage.getPage() + ")");
            }
            if (reportsPage.hasNextPage()) {
                inv.setItem(53, MenuItem.NEXT_PAGE.get());
                me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration.getInstance().getLogger().info("Added next page button (page " + reportsPage.getPage() + ", total: " + reportsPage.getTotalReports() + ")");
            }
        }
    }
    
    @Override
    public void onClick(ItemStack item, int slot, ClickType click) {
        if (slot == 8 && u.hasPermission(Permission.STAFF_ARCHIVE)) {
            // Open archived reports menu
            u.openArchivedReportsMenu(1, true, rm, db, tr, vm, bm, um);
        } else if (slot == 45 && reportsPage != null && reportsPage.hasPreviousPage()) {
            // Previous page
            setPage(getPage() - 1);
            u.openReportsMenu(getPage(), true, rm, db, tr, vm, bm, um);
        } else if (slot == 53 && reportsPage != null && reportsPage.hasNextPage()) {
            // Next page
            setPage(getPage() + 1);
            u.openReportsMenu(getPage(), true, rm, db, tr, vm, bm, um);
        } else if (slot >= 18 && slot < 45 && reportsPage != null) {
            // Report clicked (slots 18-44 like original TigerReports)
            int reportId = reportsPage.getReportIdAtIndex(slot - 18);
            
            if (reportId == -1) {
                update(false);
            } else {
                rm.getReportByIdAsynchronously(reportId, false, true, db, tr, um, (report) -> {
                    if (report != null) {
                        tr.runTask(() -> {
                            if (click == org.bukkit.event.inventory.ClickType.RIGHT || click == org.bukkit.event.inventory.ClickType.SHIFT_RIGHT) {
                                // Right click: Archive report (if has permission and report is open)
                                if (u.hasPermission(Permission.STAFF_ARCHIVE) && "Open".equals(report.getStatus())) {
                                    // Open beautiful confirmation menu like original TigerReports
                                    ConfirmationMenu confirmMenu = new ConfirmationMenu(
                                        u, report, ConfirmationMenu.Action.ARCHIVE, 
                                        rm, db, tr, vm, bm, um
                                    );
                                    confirmMenu.open(true);
                                } else {
                                    u.sendErrorMessage("§cYou cannot archive this report.");
                                }
                            } else if (click == org.bukkit.event.inventory.ClickType.DROP) {
                                // Drop key: Delete report (if has permission)
                                if (u.hasPermission(Permission.STAFF_DELETE)) {
                                    // Open beautiful confirmation menu like original TigerReports
                                    ConfirmationMenu confirmMenu = new ConfirmationMenu(
                                        u, report, ConfirmationMenu.Action.DELETE, 
                                        rm, db, tr, vm, bm, um
                                    );
                                    confirmMenu.open(true);
                                } else {
                                    u.sendErrorMessage("§cYou don't have permission to delete reports.");
                                }
                            } else {
                                // Left click: Open individual report menu
                                u.openReportMenu(reportId, rm, db, tr, vm, bm, um);
                            }
                        });
                    } else {
                        tr.runTask(() -> {
                            u.sendErrorMessage("§cReport not found.");
                        });
                    }
                });
            }
        }
    }
}
