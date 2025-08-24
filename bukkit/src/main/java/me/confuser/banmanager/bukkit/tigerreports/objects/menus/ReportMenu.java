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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Individual Report Menu - Shows detailed view of a specific report
 * Layout: 54 slots (6 rows)
 * Features: Player heads, report details, status management, processing options
 */
public class ReportMenu extends ReportManagerMenu implements User.UserListener {
    
    private final VaultManager vm;
    private final Object bm; // BungeeManager placeholder
    
    public ReportMenu(User u, int reportId, ReportsManager rm, BanManagerDatabaseAdapter db,
            TigerReportsIntegration tr, VaultManager vm, Object bm, UsersManager um) {
        super(u, 54, 0, Permission.STAFF, reportId, true, false, rm, db, tr, um);
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
        
        Inventory inv = getInventory("§6Report §7> §e#" + r.getId() + " - " + r.getReportedName(), false);
        
        // Fill background with glass panes
        ItemStack glass = MenuRawItem.GUI.create();
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }
        
        // Back button (slot 0)
        inv.setItem(0, createBackButton());
        
        // Report info button (slot 18)
        inv.setItem(18, createReportInfoButton());
        
        // Reporter head (slot 21)
        inv.setItem(21, createParticipantHead(true));
        
        // Report status indicator (slot 22)
        inv.setItem(22, createStatusIndicator());
        
        // Reported player head (slot 23)
        inv.setItem(23, createParticipantHead(false));
        
        // Data button (slot 26)
        inv.setItem(26, createDataButton());
        
        // Status buttons (slots 29, 30, 31)
        inv.setItem(29, createStatusButton("Open", r.getStatus().equals("Open")));
        inv.setItem(30, createStatusButton("In Progress", r.getStatus().equals("In Progress")));
        inv.setItem(31, createStatusButton("Done", r.getStatus().equals("Done")));
        
        // Process button (slot 32) - Opens AdminGUI for punishment
        if (u.hasPermission(Permission.STAFF_ADVANCED)) {
            inv.setItem(32, createProcessButton());
        }
        
        // Archive button (slot 33)
        if (u.canArchive(r)) {
            inv.setItem(33, createArchiveButton());
        }
        
        // Delete button (slot 36)
        if (u.hasPermission(Permission.STAFF_DELETE)) {
            inv.setItem(36, createDeleteButton());
        }
        
        // Comments button (slot 44)
        inv.setItem(44, createCommentsButton());
        
        return inv;
    }
    
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a« Back to Reports");
            meta.setLore(Arrays.asList("§7Click to return to the reports list"));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createReportInfoButton() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Report Information");
            List<String> lore = new ArrayList<>();
            lore.add("§7ID: §e#" + r.getId());
            lore.add("§7Reason: §6" + r.getReason());
            lore.add("§7Date: §e" + rm.formatDate(r.getDate()));
            lore.add("§7Status: " + rm.getStatusColor(r.getStatus()) + r.getStatus());
            lore.add("");
            lore.add("§6Click §7to view full details in chat");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createParticipantHead(boolean isReporter) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        
        if (meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;
            String playerName = isReporter ? r.getReporterName() : r.getReportedName();
            
            try {
                skullMeta.setOwner(playerName);
            } catch (Exception e) {
                // If setting owner fails, continue with default head
            }
            
            skullMeta.setDisplayName((isReporter ? "§aReporter: " : "§cReported: ") + "§f" + playerName);
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Player: §f" + playerName);
            lore.add("§7Online: " + (isPlayerOnline(playerName) ? "§aYes" : "§cNo"));
            
            if (u.hasPermission(Permission.STAFF_TELEPORT)) {
                lore.add("");
                lore.add("§6Left click §7to teleport to current location");
                lore.add("§6Right click §7to teleport to report location");
            }
            
            skullMeta.setLore(lore);
            item.setItemMeta(skullMeta);
        }
        return item;
    }
    
    private ItemStack createStatusIndicator() {
        Material material = Material.REDSTONE_BLOCK;
        String color = "§c";
        
        switch (r.getStatus()) {
            case "Open":
                material = Material.REDSTONE_BLOCK;
                color = "§c";
                break;
            case "In Progress":
                material = Material.GOLD_BLOCK;
                color = "§6";
                break;
            case "Done":
                material = Material.EMERALD_BLOCK;
                color = "§a";
                break;
            case "Closed":
                material = Material.COAL_BLOCK;
                color = "§8";
                break;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + "Status: " + r.getStatus());
            List<String> lore = new ArrayList<>();
            lore.add("§7Current status of this report");
            lore.add("");
            lore.add("§7Use the status buttons below");
            lore.add("§7to change the report status");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createDataButton() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Report Data");
            List<String> lore = new ArrayList<>();
            lore.add("§7View technical report data");
            lore.add("");
            lore.add("§6Left click §7to view data in chat");
            lore.add("§6Right click §7to view message history");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createStatusButton(String status, boolean current) {
        Material material;
        String color;
        String displayName;
        
        switch (status) {
            case "Open":
                material = Material.LIME_TERRACOTTA; // Green clay like original TigerReports
                color = "§a";
                displayName = current ? "§a§lOpen" : "§a§lOpen";
                break;
            case "In Progress":
                material = Material.YELLOW_TERRACOTTA; // Yellow clay like original TigerReports
                color = "§e";
                displayName = current ? "§e§lIn Progress" : "§e§lIn Progress";
                break;
            case "Done":
                material = Material.BLUE_TERRACOTTA; // Blue clay like original TigerReports
                color = "§9";
                displayName = current ? "§9§lDone" : "§9§lDone";
                break;
            default:
                material = Material.GRAY_TERRACOTTA;
                color = "§7";
                displayName = "§7" + status;
                break;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            List<String> lore = new ArrayList<>();
            if (current) {
                lore.add("§7Current status");
                lore.add("");
                lore.add("§8This report is currently " + color + status.toLowerCase());
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                lore.add("§7Click to change status to " + color + status);
                lore.add("");
                lore.add("§8Change the report status to " + color + status.toLowerCase());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createProcessButton() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Process Report");
            List<String> lore = new ArrayList<>();
            lore.add("§7Process this report and take action");
            lore.add("");
            lore.add("§7Opens AdminGUI for the reported player");
            lore.add("§7to ban, mute, kick, or warn them");
            lore.add("");
            lore.add("§6Click §7to open punishment menu");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createArchiveButton() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Archive Report");
            List<String> lore = new ArrayList<>();
            lore.add("§7Close this report and move it to archives");
            lore.add("");
            lore.add("§6Click §7to archive this report");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createDeleteButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cDelete Report");
            List<String> lore = new ArrayList<>();
            lore.add("§7Permanently delete this report");
            lore.add("");
            lore.add("§c⚠ This action cannot be undone!");
            lore.add("");
            lore.add("§cClick §7to delete this report");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createCommentsButton() {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Comments");
            List<String> lore = new ArrayList<>();
            lore.add("§7View and add comments to this report");
            lore.add("");
            lore.add("§7Comments: §e" + r.getCommentsCount());
            lore.add("");
            lore.add("§6Click §7to open comments menu");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private boolean isPlayerOnline(String playerName) {
        return org.bukkit.Bukkit.getPlayer(playerName) != null;
    }
    
    @Override
    public void onClick(ItemStack item, int slot, ClickType click) {
        
        switch (slot) {
            case 0: // Back button
                u.openReportsMenu(1, true, rm, db, tr, vm, bm, um);
                break;
                
            case 18: // Report info
                u.sendMessage("§6=== Report #" + r.getId() + " Details ===");
                u.sendMessage("§7Reporter: §a" + r.getReporterName());
                u.sendMessage("§7Reported: §c" + r.getReportedName());
                u.sendMessage("§7Reason: §6" + r.getReason());
                u.sendMessage("§7Status: " + rm.getStatusColor(r.getStatus()) + r.getStatus());
                u.sendMessage("§7Date: §e" + rm.formatDate(r.getDate()));
                break;
                
            case 21: // Reporter head
            case 23: // Reported head
                if (!u.hasPermission(Permission.STAFF_TELEPORT) || click == null) {
                    return;
                }
                
                String targetPlayer = slot == 21 ? r.getReporterName() : r.getReportedName();
                
                if (click.isLeftClick()) {
                    // Teleport to current location
                    org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(targetPlayer);
                    if (target != null) {
                        u.getPlayer().teleport(target.getLocation());
                        u.sendMessage("§aTeleported to " + targetPlayer + "'s current location");
                    } else {
                        u.sendErrorMessage("Player " + targetPlayer + " is not online");
                    }
                } else if (click.isRightClick()) {
                    // Teleport to report location (if available)
                    u.sendMessage("§cReport location teleportation not yet implemented");
                }
                break;
                
            case 26: // Data button
                if (click.isLeftClick()) {
                    u.sendMessage("§6=== Report #" + r.getId() + " Technical Data ===");
                    u.sendMessage("§7Report ID: §e" + r.getId());
                    u.sendMessage("§7Created: §e" + rm.formatDate(r.getDate()));
                    u.sendMessage("§7Updated: §e" + rm.formatDate(r.getUpdated()));
                    u.sendMessage("§7Status: " + rm.getStatusColor(r.getStatus()) + r.getStatus());
                } else if (click.isRightClick()) {
                    // Show message history for the reported player
                    showMessageHistory(r.getReportedName());
                }
                break;
                
            case 29: // Open status
                if (!r.getStatus().equals("Open")) {
                    r.setStatus("Open", u, db, rm, tr);
                    update(false);
                }
                break;
                
            case 30: // In Progress status
                if (!r.getStatus().equals("In Progress")) {
                    r.setStatus("In Progress", u, db, rm, tr);
                    update(false);
                }
                break;
                
            case 31: // Done status
                if (!r.getStatus().equals("Done")) {
                    r.setStatus("Done", u, db, rm, tr);
                    update(false);
                }
                break;
                
            case 32: // Process button - Open AdminGUI
                if (u.hasPermission(Permission.STAFF_ADVANCED)) {
                    // Close this menu and open AdminGUI for the reported player
                    p.closeInventory();
                    
                    // Get the reported player's UUID
                    String reportedName = r.getReportedName();
                    org.bukkit.entity.Player reportedPlayer = org.bukkit.Bukkit.getPlayer(reportedName);
                    
                    if (reportedPlayer != null) {
                        // Player is online - open AdminGUI directly
                        tr.runTaskDelayedly(40L, () -> {
                            u.sendMessage("§aOpening AdminGUI for " + reportedName + "...");
                            p.performCommand("admin " + reportedName);
                        });
                    } else {
                        // Player is offline - still try to open AdminGUI
                        tr.runTaskDelayedly(40L, () -> {
                            u.sendMessage("§eOpening AdminGUI for offline player " + reportedName + "...");
                            p.performCommand("admin " + reportedName);
                        });
                    }
                }
                break;
                
            case 33: // Archive button
                if (u.canArchive(r)) {
                    ConfirmationMenu confirmMenu = new ConfirmationMenu(
                        u, r, ConfirmationMenu.Action.ARCHIVE,
                        rm, db, tr, vm, bm, um
                    );
                    confirmMenu.open(true);
                }
                break;
                
            case 36: // Delete button
                if (u.hasPermission(Permission.STAFF_DELETE)) {
                    ConfirmationMenu confirmMenu = new ConfirmationMenu(
                        u, r, ConfirmationMenu.Action.DELETE,
                        rm, db, tr, vm, bm, um
                    );
                    confirmMenu.open(true);
                }
                break;
                
            case 44: // Comments button
                u.openCommentsMenu(1, r, rm, db, tr, um, bm, vm);
                break;
        }
    }
    
    @Override
    public void onCooldownChange(User u) {
        // Ignored
    }
    
    @Override
    public void onStatisticsChange(User u) {
        update(false);
    }
    
    private void showMessageHistory(String playerName) {
        me.confuser.banmanager.bukkit.tigerreports.listeners.MessageHistoryListener historyListener = 
            TigerReportsIntegration.getInstance().getMessageHistoryListener();
        
        if (historyListener == null) {
            u.sendErrorMessage("§cMessage history system not available");
            return;
        }
        
        java.util.List<me.confuser.banmanager.bukkit.tigerreports.listeners.MessageHistoryListener.ChatMessage> messages = 
            historyListener.getMessageHistory(playerName);
        
        u.sendMessage("§6=== Message History for " + playerName + " ===");
        
        if (messages.isEmpty()) {
            u.sendMessage("§7No recent messages found for this player.");
            u.sendMessage("§7(Messages are only tracked while the server is running)");
        } else {
            u.sendMessage("§7Showing last " + messages.size() + " messages:");
            u.sendMessage("");
            
            // Show messages in chronological order (oldest first)
            java.util.Collections.reverse(messages);
            
            for (me.confuser.banmanager.bukkit.tigerreports.listeners.MessageHistoryListener.ChatMessage message : messages) {
                u.sendMessage("§7[" + message.getFormattedTimestamp() + "] §f" + message.getMessage());
                u.sendMessage("§8  └ Location: " + message.getFormattedLocation());
            }
            
            u.sendMessage("");
            u.sendMessage("§7This message history can help provide context for the report.");
        }
    }
}
