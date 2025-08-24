package me.confuser.banmanager.bukkit.tigerreports.objects.menus;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.MenuRawItem;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.Report;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Process Menu - Allows staff to mark reports as True/False/Uncertain and take action
 * Layout: 27 slots (3 rows)
 * Features: True/False/Uncertain buttons, AdminGUI integration for punishments
 */
public class ProcessMenu extends ReportManagerMenu {
    
    private static final int[] GUI_POSITIONS = new int[] {
            1, 2, 3, 4, 5, 6, 7, 10, 16, 19, 20, 21, 22, 23, 24, 25
    };
    
    private final VaultManager vm;
    private final Object bm; // BungeeManager placeholder
    
    public ProcessMenu(User u, int reportId, ReportsManager rm, BanManagerDatabaseAdapter db,
            TigerReportsIntegration tr, VaultManager vm, Object bm, UsersManager um) {
        super(u, 27, 0, Permission.STAFF, reportId, rm, db, tr, um);
        this.vm = vm;
        this.bm = bm;
    }
    
    @Override
    public Inventory onOpen() {
        String error = checkReport();
        if (error != null) {
            u.sendErrorMessage(error);
            return null;
        }
        
        Inventory inv = getInventory("§6Process Report §7> §e#" + r.getId(), false);
        
        // Fill background with glass panes
        ItemStack glass = MenuRawItem.GUI.create();
        for (int position : GUI_POSITIONS) {
            inv.setItem(position, glass);
        }
        
        // Report info at top (slot 0)
        inv.setItem(0, createReportInfoItem());
        
        // True button (slot 11) - Green
        inv.setItem(11, createAppreciationButton("True", Material.GREEN_TERRACOTTA, "§a", 
            "Mark this report as TRUE", "The report is valid and action should be taken", true));
        
        // Uncertain button (slot 13) - Yellow  
        inv.setItem(13, createAppreciationButton("Uncertain", Material.YELLOW_TERRACOTTA, "§e",
            "Mark this report as UNCERTAIN", "The report needs more investigation", false));
        
        // False button (slot 15) - Red
        inv.setItem(15, createAppreciationButton("False", Material.RED_TERRACOTTA, "§c",
            "Mark this report as FALSE", "The report is invalid or abusive", false));
        
        // Cancel button (slot 18)
        inv.setItem(18, createCancelButton());
        
        return inv;
    }
    
    private ItemStack createReportInfoItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Report #" + r.getId());
            List<String> lore = Arrays.asList(
                "§7Reporter: §a" + r.getReporterName(),
                "§7Reported: §c" + r.getReportedName(),
                "§7Reason: §6" + r.getReason(),
                "§7Status: " + rm.getStatusColor(r.getStatus()) + r.getStatus(),
                "",
                "§7Choose how to process this report:"
            );
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createAppreciationButton(String appreciation, Material material, String color, 
                                             String title, String description, boolean punishment) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + appreciation);
            
            List<String> lore = Arrays.asList(
                "§7" + description,
                "",
                "§7" + title,
                punishment ? "§7Opens AdminGUI for punishment" : "§7Closes the report automatically",
                "",
                "§6Click §7to process as " + color + appreciation
            );
            meta.setLore(lore);
            
            if (punishment) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cCancel");
            meta.setLore(Arrays.asList(
                "§7Return to the report menu",
                "§7without processing this report",
                "",
                "§cClick §7to cancel"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    @Override
    public void onClick(ItemStack item, int slot, ClickType click) {
        
        switch (slot) {
            case 11: // True - Open AdminGUI for punishment
                processTrue();
                break;
                
            case 13: // Uncertain
                processUncertain();
                break;
                
            case 15: // False
                processFalse();
                break;
                
            case 18: // Cancel
                u.openReportMenu(r.getId(), rm, db, tr, vm, bm, um);
                break;
        }
    }
    
    private void processTrue() {
        // Mark report as processed with True appreciation
        r.setAppreciation("True", u, db, rm, tr);
        
        // Close this menu
        p.closeInventory();
        
        // Get the reported player's name
        String reportedName = r.getReportedName();
        
        // Open AdminGUI for punishment after a short delay
        tr.runTaskDelayedly(40L, () -> {
            u.sendMessage("§aReport #" + r.getId() + " marked as §2TRUE");
            u.sendMessage("§aOpening AdminGUI to punish " + reportedName + "...");
            p.performCommand("admin " + reportedName);
        });
        
        // Auto-archive if user has permission
        if (u.hasPermission(Permission.STAFF_ARCHIVE_AUTO)) {
            tr.runTaskDelayedly(1200L, () -> {
                r.setStatus("Closed", u, db, rm, tr);
                u.sendMessage("§7Report automatically archived");
            }); // Archive after 60 seconds to give time for punishment
        }
    }
    
    private void processUncertain() {
        // Mark report as processed with Uncertain appreciation
        r.setAppreciation("Uncertain", u, db, rm, tr);
        r.setStatus("In Progress", u, db, rm, tr);
        
        u.sendMessage("§eReport #" + r.getId() + " marked as §6UNCERTAIN");
        u.sendMessage("§7Report status changed to In Progress for further investigation");
        
        // Return to reports menu
        u.openReportsMenu(1, true, rm, db, tr, vm, bm, um);
    }
    
    private void processFalse() {
        // Mark report as processed with False appreciation
        r.setAppreciation("False", u, db, rm, tr);
        
        u.sendMessage("§cReport #" + r.getId() + " marked as §4FALSE");
        u.sendMessage("§7Report marked as invalid or abusive");
        
        // Auto-archive if user has permission
        if (u.hasPermission(Permission.STAFF_ARCHIVE_AUTO)) {
            r.setStatus("Closed", u, db, rm, tr);
            u.sendMessage("§7Report automatically archived");
        }
        
        // Return to reports menu
        u.openReportsMenu(1, true, rm, db, tr, vm, bm, um);
    }
}
