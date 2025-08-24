package me.confuser.banmanager.bukkit.tigerreports.objects.menus;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.MenuRawItem;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.ReportsPage;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import me.confuser.banmanager.bukkit.tigerreports.tasks.ResultCallback;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

/**
 * User Against Reports Menu - Shows all reports made AGAINST a specific player
 * Layout: 54 slots (6 rows)
 * Features: Player head, report list, pagination
 */
public class UserAgainstReportsMenu extends Menu implements UpdatedMenu {
    
    private final String targetPlayerName;
    private final ReportsManager rm;
    private final BanManagerDatabaseAdapter db;
    private final TigerReportsIntegration tr;
    private final VaultManager vm;
    private final Object bm; // BungeeManager placeholder
    private final UsersManager um;
    private ReportsPage reportsPage;
    
    public UserAgainstReportsMenu(User u, int page, String targetPlayerName, ReportsManager rm, 
            BanManagerDatabaseAdapter db, TigerReportsIntegration tr, VaultManager vm, 
            Object bm, UsersManager um) {
        super(u, 54, page, Permission.STAFF);
        this.targetPlayerName = targetPlayerName;
        this.rm = rm;
        this.db = db;
        this.tr = tr;
        this.vm = vm;
        this.bm = bm;
        this.um = um;
    }
    
    @Override
    public Inventory onOpen() {
        Inventory inv = getInventory("§6Reports against §c" + targetPlayerName, true);
        
        // Fill background with glass panes
        ItemStack glass = MenuRawItem.GUI.create();
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }
        
        // Player head (slot 0)
        inv.setItem(0, createPlayerHead());
        
        // Page info (slot 4)
        inv.setItem(4, createPageInfoButton());
        
        // Back button (slot 8)
        inv.setItem(8, createBackButton());
        
        // Load reports asynchronously
        loadReports();
        
        return inv;
    }
    
    private ItemStack createPlayerHead() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        
        if (meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;
            
            try {
                skullMeta.setOwner(targetPlayerName);
            } catch (Exception e) {
                // If setting owner fails, continue with default head
            }
            
            skullMeta.setDisplayName("§cReported Player: §e" + targetPlayerName);
            skullMeta.setLore(Arrays.asList(
                "§7Viewing reports made against this player",
                "",
                "§7This shows all reports where",
                "§7this player was reported by others",
                "",
                "§7Click to view player statistics",
                "§7(Feature not yet implemented)"
            ));
            item.setItemMeta(skullMeta);
        }
        return item;
    }
    
    private ItemStack createPageInfoButton() {
        ItemStack item = new ItemStack(Material.BOOKSHELF);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Reports against " + targetPlayerName);
            meta.setLore(Arrays.asList(
                "§7Page: §e" + page,
                "§7Loading reports..."
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a« Back to Reports");
            meta.setLore(Arrays.asList("§7Return to the main reports list"));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private void loadReports() {
        // Load reports made against this player
        rm.getReportsAgainstPlayerAsynchronously(targetPlayerName, false, page, 27, db, tr, um, new ResultCallback<ReportsPage>() {
            @Override
            public void onResultReceived(ReportsPage result) {
                reportsPage = result;
                tr.runTask(() -> update(false));
            }
        });
    }
    
    @Override
    public void onUpdate(Inventory inv) {
        // Update page info
        ItemStack pageInfo = new ItemStack(Material.BOOKSHELF);
        ItemMeta meta = pageInfo.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Reports against " + targetPlayerName);
            if (reportsPage != null) {
                meta.setLore(Arrays.asList(
                    "§7Page: §e" + page,
                    "§7Reports on this page: §e" + reportsPage.getReports().size(),
                    "§7Total reports: §e" + reportsPage.getTotalReports()
                ));
            } else {
                meta.setLore(Arrays.asList(
                    "§7Page: §e" + page,
                    "§7Loading reports..."
                ));
            }
            pageInfo.setItemMeta(meta);
        }
        inv.setItem(4, pageInfo);
        
        // Fill reports (slots 18-44)
        if (reportsPage != null) {
            rm.fillInventoryWithReportsPage(inv, reportsPage, "§6Left click §7to show details.", 
                u.hasPermission(Permission.STAFF_ARCHIVE), "§6Drop key §7to delete.", vm, bm);
        }
        
        // Pagination buttons
        inv.setItem(45, reportsPage != null && reportsPage.hasPreviousPage() ? 
            createPreviousPageButton() : MenuRawItem.GUI.create());
        inv.setItem(53, reportsPage != null && reportsPage.hasNextPage() ? 
            createNextPageButton() : MenuRawItem.GUI.create());
    }
    
    private ItemStack createPreviousPageButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a« Previous Page");
            meta.setLore(Arrays.asList("§7Go to page " + (page - 1)));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createNextPageButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aNext Page »");
            meta.setLore(Arrays.asList("§7Go to page " + (page + 1)));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    @Override
    public void onClick(ItemStack item, int slot, ClickType click) {
        
        switch (slot) {
            case 0: // Player head
                u.sendMessage("§ePlayer statistics feature not yet implemented");
                break;
                
            case 8: // Back button
                u.openReportsMenu(1, true, rm, db, tr, vm, bm, um);
                break;
                
            case 45: // Previous page
                if (reportsPage != null && reportsPage.hasPreviousPage()) {
                    u.openUserAgainstReportsMenu(page - 1, targetPlayerName, rm, db, tr, vm, bm, um);
                }
                break;
                
            case 53: // Next page
                if (reportsPage != null && reportsPage.hasNextPage()) {
                    u.openUserAgainstReportsMenu(page + 1, targetPlayerName, rm, db, tr, vm, bm, um);
                }
                break;
                
            default:
                // Handle report clicks (slots 18-44)
                if (slot >= 18 && slot < 45 && reportsPage != null) {
                    int reportId = reportsPage.getReportIdAtIndex(slot - 18);
                    
                    if (reportId == -1) {
                        update(false);
                    } else {
                        rm.getReportByIdAsynchronously(reportId, false, true, db, tr, um, (report) -> {
                            if (report != null) {
                                tr.runTask(() -> {
                                    if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
                                        if (u.hasPermission(Permission.STAFF_ARCHIVE) && "Open".equals(report.getStatus())) {
                                            ConfirmationMenu confirmMenu = new ConfirmationMenu(
                                                u, report, ConfirmationMenu.Action.ARCHIVE,
                                                rm, db, tr, vm, bm, um
                                            );
                                            confirmMenu.open(true);
                                        }
                                    } else if (click == ClickType.DROP) {
                                        if (u.hasPermission(Permission.STAFF_DELETE)) {
                                            ConfirmationMenu confirmMenu = new ConfirmationMenu(
                                                u, report, ConfirmationMenu.Action.DELETE,
                                                rm, db, tr, vm, bm, um
                                            );
                                            confirmMenu.open(true);
                                        }
                                    } else {
                                        // Open individual report menu
                                        u.openReportMenu(reportId, rm, db, tr, vm, bm, um);
                                    }
                                });
                            }
                        });
                    }
                }
                break;
        }
    }
}
