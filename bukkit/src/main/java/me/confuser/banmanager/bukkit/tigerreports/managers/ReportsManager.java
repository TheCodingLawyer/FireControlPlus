package me.confuser.banmanager.bukkit.tigerreports.managers;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.Report;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.ReportsPage;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.ReportsCharacteristics;
import me.confuser.banmanager.bukkit.tigerreports.tasks.ResultCallback;
import me.confuser.banmanager.bukkit.tigerreports.tasks.TaskScheduler;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;

/**
 * Reports manager for TigerReports integration
 * Adapted from original TigerReports ReportsManager class
 */
public class ReportsManager {
    
    public void getReportByIdAsynchronously(int reportId, boolean includeArchived, boolean includeComments,
                                          BanManagerDatabaseAdapter database, TaskScheduler taskScheduler,
                                          UsersManager usersManager, ResultCallback<Report> callback) {
        database.getReportById(reportId, (reportData) -> {
            if (reportData != null) {
                Report report = new Report(reportData);
                callback.onResultReceived(report);
            } else {
                callback.onResultReceived(null);
            }
        });
    }
    
    public void getReportsPageAsynchronously(int page, ReportsCharacteristics characteristics,
                                           BanManagerDatabaseAdapter database, TaskScheduler taskScheduler,
                                           UsersManager usersManager, ResultCallback<ReportsPage> callback) {
        String[] statuses;
        if (characteristics == ReportsCharacteristics.CURRENT_REPORTS) {
            // Current reports should include both Open and In Progress reports
            statuses = new String[]{"Open", "In Progress"};
        } else if (characteristics == ReportsCharacteristics.ARCHIVED_REPORTS) {
            // Archived reports should only include Closed reports
            statuses = new String[]{"Closed"};
        } else {
            // All reports
            statuses = new String[]{"ALL"};
        }
        int limit = 28; // Standard page size for GUI
        
        database.getReportsMultipleStatuses(page, limit, statuses, (reports) -> {
            // Get total count for proper pagination
            database.getReportsCountMultipleStatuses(statuses, (totalCount) -> {
                ReportsPage reportsPage = new ReportsPage(reports, page, limit, totalCount);
                callback.onResultReceived(reportsPage);
            });
        });
    }
    
    public void fillInventoryWithReportsPage(Inventory inventory, ReportsPage reportsPage,
                                           String showAction, boolean canArchive, String deleteAction,
                                           VaultManager vaultManager, Object bungeeManager) {
        // Fill inventory with report items
        List<Map<String, Object>> reports = reportsPage.getReports();
        int startSlot = 18; // Start after the top border
        
        // Use slots 18-44 like original TigerReports (27 slots total)
        int maxReports = Math.min(reports.size(), 27);
        for (int i = 0; i < maxReports; i++) {
            Map<String, Object> reportData = reports.get(i);
            
            // Create report item
            org.bukkit.inventory.ItemStack reportItem = createReportItem(reportData, showAction, canArchive, deleteAction);
            inventory.setItem(startSlot + i, reportItem);
        }
        
        // Clear any remaining slots from 18-44 to prevent old items from showing
        for (int slot = startSlot + maxReports; slot < 45; slot++) {
            inventory.setItem(slot, null);
        }
    }
    
    private org.bukkit.inventory.ItemStack createReportItem(Map<String, Object> reportData, 
                                                          String showAction, boolean canArchive, String deleteAction) {
        // Create a player head item for the report
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        
        if (meta != null && meta instanceof org.bukkit.inventory.meta.SkullMeta) {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) meta;
            
            String reportedName = (String) reportData.get("reported_name");
            String reporterName = (String) reportData.get("reporter_name");
            String reason = (String) reportData.get("reason");
            String status = (String) reportData.get("status");
            Integer reportId = (Integer) reportData.get("report_id");
            
            // Set the player skin for the reported player
            try {
                skullMeta.setOwner(reportedName);
            } catch (Exception e) {
                // If setting owner fails, continue with default head
            }
            
            Object dateObj = reportData.get("date");
            
            // Set display name like TigerReports: "Report > #ID - PlayerName"
            skullMeta.setDisplayName("§6Report §7> §e#" + reportId + " - " + reportedName);
            
            // Create beautiful lore like original TigerReports
            java.util.List<String> lore = new java.util.ArrayList<>();
            
            // Empty line at top
            lore.add("");
            
            // Status with proper color
            String statusColor = getStatusColor(status);
            lore.add("§7Status: " + statusColor + status);
            
            // Date
            String dateStr = formatDate(dateObj);
            lore.add("§7Date: §e" + dateStr);
            
            // Empty line
            lore.add("");
            
            // Reporter(s)
            lore.add("§7Reporter(s): §a" + reporterName);
            
            // Reported player
            lore.add("§7Reported: §c" + reportedName);
            
            // Reason
            lore.add("§7Reason: §6" + reason);
            
            // Actions section
            if (showAction != null && !showAction.isEmpty()) {
                lore.add("");
                lore.add("§6Left click §7to show details.");
                
                if (canArchive && "Open".equals(status)) {
                    lore.add("§6Right click §7to archive.");
                }
                
                if (deleteAction != null && !deleteAction.isEmpty()) {
                    lore.add("§6Drop key §7to delete.");
                }
            }
            
            skullMeta.setLore(lore);
            
            // Add enchantment glow for waiting/open reports like original TigerReports
            if ("Open".equals(status)) {
                skullMeta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
                skullMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(skullMeta);
        }
        
        return item;
    }
    
    public String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "open":
            case "waiting":
                return "§e"; // Yellow for waiting/open
            case "in progress":
            case "in_progress":
                return "§6"; // Orange for in progress
            case "closed":
            case "done":
                return "§a"; // Green for done/closed
            case "invalid":
            case "abusive":
                return "§c"; // Red for invalid/abusive
            default:
                return "§7"; // Gray for unknown
        }
    }
    
    public String formatDate(Object dateObj) {
        if (dateObj == null) {
            return "Unknown";
        }
        
        try {
            // Handle different date formats that might come from the database
            if (dateObj instanceof java.util.Date) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm");
                return sdf.format((java.util.Date) dateObj);
            } else if (dateObj instanceof java.sql.Timestamp) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm");
                return sdf.format(new java.util.Date(((java.sql.Timestamp) dateObj).getTime()));
            } else if (dateObj instanceof Long) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm");
                return sdf.format(new java.util.Date((Long) dateObj));
            } else {
                return dateObj.toString();
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    public void processReport(int reportId, String status, String reason, 
                            BanManagerDatabaseAdapter database, ResultCallback<Boolean> callback) {
        database.updateReportStatus(reportId, status, callback);
    }
    
    // Report listener management for real-time updates
    public void addReportListener(int reportId, Report.ReportListener listener) {
        // Placeholder - in full implementation, this would manage listeners for report updates
        if (me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils.isMenuOperationLoggingEnabled()) {
            TigerReportsIntegration.getInstance().getLogger().info("Added report listener for report " + reportId);
        }
    }
    
    public void removeReportListener(int reportId, Report.ReportListener listener) {
        // Placeholder - in full implementation, this would remove listeners
        TigerReportsIntegration.getInstance().getLogger().info("Removed report listener for report " + reportId);
    }
    
    // Get reports by specific player (reporter)
    public void getReportsByPlayerAsynchronously(String playerName, boolean archived, int page, int pageSize,
                                               BanManagerDatabaseAdapter db, TigerReportsIntegration tr, 
                                               UsersManager um, ResultCallback<ReportsPage> callback) {
        tr.runTaskAsynchronously(() -> {
            try {
                // Get reports where this player is the reporter
                db.getReportsByReporter(playerName, archived, page, pageSize, callback);
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to get reports by player: " + e.getMessage());
                tr.runTask(() -> callback.onResultReceived(new ReportsPage(new java.util.ArrayList<>(), page, pageSize)));
            }
        });
    }
    
    // Get reports against specific player (reported)
    public void getReportsAgainstPlayerAsynchronously(String playerName, boolean archived, int page, int pageSize,
                                                    BanManagerDatabaseAdapter db, TigerReportsIntegration tr, 
                                                    UsersManager um, ResultCallback<ReportsPage> callback) {
        tr.runTaskAsynchronously(() -> {
            try {
                // Get reports where this player is the reported player
                db.getReportsAgainstPlayer(playerName, archived, page, pageSize, callback);
            } catch (Exception e) {
                TigerReportsIntegration.getInstance().getLogger().severe("Failed to get reports against player: " + e.getMessage());
                tr.runTask(() -> callback.onResultReceived(new ReportsPage(new java.util.ArrayList<>(), page, pageSize)));
            }
        });
    }
    
    // Comment notification methods
    public void notifyCommentAdded(int reportId, me.confuser.banmanager.bukkit.tigerreports.objects.Comment comment) {
        TigerReportsIntegration.getInstance().getLogger().info("Comment added to report " + reportId + " by " + comment.getAuthorName());
        // In full implementation, this would notify all staff with open menus about the new comment
    }
    
    public void notifyCommentDeleted(int reportId, int commentId) {
        TigerReportsIntegration.getInstance().getLogger().info("Comment " + commentId + " deleted from report " + reportId);
        // In full implementation, this would notify all staff with open menus about the deleted comment
    }
}
