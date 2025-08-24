package me.confuser.banmanager.bukkit.tigerreports.objects.menus;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.data.config.Message;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.MenuRawItem;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.Report;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Confirmation menu for TigerReports integration
 * Adapted from original TigerReports ConfirmationMenu class
 */
public class ConfirmationMenu extends Menu {
    
    public enum Action {
        ARCHIVE("ARCHIVE", Permission.STAFF_ARCHIVE),
        DELETE("DELETE", Permission.STAFF_DELETE);
        
        private final String displayedName;
        private final Permission permission;
        
        Action(String displayedName, Permission permission) {
            this.displayedName = displayedName;
            this.permission = permission;
        }
        
        public String getDisplayedName() {
            return displayedName;
        }
        
        public Permission getRequiredPermission() {
            return permission;
        }
    }
    
    private final Action action;
    private final Report report;
    private final ReportsManager rm;
    private final BanManagerDatabaseAdapter db;
    private final TigerReportsIntegration tr;
    private final VaultManager vm;
    private final Object bm; // BungeeManager placeholder
    private final UsersManager um;
    
    public ConfirmationMenu(User u, Report report, Action action, ReportsManager rm, 
                           BanManagerDatabaseAdapter db, TigerReportsIntegration tr, 
                           VaultManager vm, Object bm, UsersManager um) {
        super(u, 27, 0, Permission.STAFF);
        this.action = action;
        this.report = report;
        this.rm = rm;
        this.db = db;
        this.tr = tr;
        this.vm = vm;
        this.bm = bm;
        this.um = um;
    }
    
    @Override
    public Inventory onOpen() {
        String reportName = "Report #" + report.getId() + " - " + report.getReportedName();
        String actionDisplayed = action.getDisplayedName();
        
        // Create inventory with title like original TigerReports
        String title = "&6" + actionDisplayed.toLowerCase() + " &7> " + reportName;
        Inventory inv = getInventory(org.bukkit.ChatColor.translateAlternateColorCodes('&', title), false);
        
        // Add GUI border items (gray glass panes) like original TigerReports
        ItemStack gui = MenuRawItem.GUI.create();
        for (int position : new int[] {
                1, 2, 3, 5, 6, 7, 10, 12, 14, 16, 19, 20, 21, 23, 24, 25
        }) {
            inv.setItem(position, gui);
        }
        
        // Green terracotta - CONFIRM button (slot 11)
        String confirmMessage = "&a&lConfirm " + actionDisplayed.toLowerCase();
        String confirmDetails = "&6Click &7to " + actionDisplayed.toLowerCase() + " " + reportName + " &7permanently.";
        inv.setItem(11, MenuRawItem.GREEN_CLAY.builder()
            .name(confirmMessage)
            .lore(confirmDetails)
            .create());
        
        // Report item in center (slot 13)
        inv.setItem(13, createReportDisplayItem());
        
        // Red terracotta - CANCEL button (slot 15)
        String cancelMessage = "&c&lCancel " + actionDisplayed.toLowerCase();
        String cancelDetails = "&6Click &7to cancel " + actionDisplayed.toLowerCase() + ".";
        inv.setItem(15, MenuRawItem.RED_CLAY.builder()
            .name(cancelMessage)
            .lore(cancelDetails)
            .create());
        
        return inv;
    }
    
    private ItemStack createReportDisplayItem() {
        // Create a player head item showing the report details
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        
        if (meta != null && meta instanceof org.bukkit.inventory.meta.SkullMeta) {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) meta;
            
            // Set the player skin
            try {
                skullMeta.setOwner(report.getReportedName());
            } catch (Exception e) {
                // If setting owner fails, continue with default head
            }
            
            // Set display name
            skullMeta.setDisplayName("§6Report §7> §e#" + report.getId() + " - " + report.getReportedName());
            
            // Set lore with report details
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add("§7Status: " + rm.getStatusColor(report.getStatus()) + report.getStatus());
            lore.add("§7Date: §e" + rm.formatDate(report.getDate()));
            lore.add("");
            lore.add("§7Reporter: §a" + report.getReporterName());
            lore.add("§7Reported: §c" + report.getReportedName());
            lore.add("§7Reason: §6" + report.getReason());
            
            skullMeta.setLore(lore);
            item.setItemMeta(skullMeta);
        }
        
        return item;
    }
    
    @Override
    public void onClick(ItemStack item, int slot, ClickType click) {
        if (slot == 11) {
            // CONFIRM button clicked
            if (!u.hasPermission(action.getRequiredPermission())) {
                u.sendErrorMessage("§cYou don't have permission to perform this action.");
                u.openReportsMenu(1, false, rm, db, tr, vm, bm, um);
                return;
            }
            
            // Perform the action
            switch (action) {
                case DELETE:
                    performDelete();
                    break;
                case ARCHIVE:
                    performArchive();
                    break;
            }
            
            // Close menu and return to reports list
            u.openReportsMenu(1, false, rm, db, tr, vm, bm, um);
            
        } else if (slot == 15) {
            // CANCEL button clicked
            u.openReportsMenu(1, true, rm, db, tr, vm, bm, um);
        }
    }
    
    private void performDelete() {
        db.deleteReport(report.getId(), (success) -> {
            tr.runTask(() -> {
                if (success) {
                    u.sendMessage("§aReport #" + report.getId() + " has been permanently deleted.");
                    // Play success sound
                    me.confuser.banmanager.bukkit.tigerreports.ConfigSound.REPORT.play(u.getPlayer(), tr.getConfig());
                } else {
                    u.sendErrorMessage("§cFailed to delete report #" + report.getId() + ". Please try again.");
                    // Play error sound
                    me.confuser.banmanager.bukkit.tigerreports.ConfigSound.ERROR.play(u.getPlayer(), tr.getConfig());
                }
            });
        });
    }
    
    private void performArchive() {
        rm.processReport(report.getId(), "Closed", "Archived via GUI", db, (success) -> {
            tr.runTask(() -> {
                if (success) {
                    u.sendMessage("§aReport #" + report.getId() + " has been archived.");
                    // Play success sound
                    me.confuser.banmanager.bukkit.tigerreports.ConfigSound.REPORT.play(u.getPlayer(), tr.getConfig());
                } else {
                    u.sendErrorMessage("§cFailed to archive report #" + report.getId() + ". Please try again.");
                    // Play error sound
                    me.confuser.banmanager.bukkit.tigerreports.ConfigSound.ERROR.play(u.getPlayer(), tr.getConfig());
                }
            });
        });
    }
}
