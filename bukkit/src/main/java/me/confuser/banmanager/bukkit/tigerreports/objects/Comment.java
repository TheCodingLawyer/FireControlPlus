package me.confuser.banmanager.bukkit.tigerreports.objects;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

/**
 * Comment object for TigerReports integration
 * Represents a comment on a report for staff communication
 */
public class Comment {
    
    private final int id;
    private final int reportId;
    private final UUID authorUuid;
    private final String authorName;
    private final String message;
    private final long created;
    private boolean sent;
    
    public Comment(int id, int reportId, UUID authorUuid, String authorName, String message, long created, boolean sent) {
        this.id = id;
        this.reportId = reportId;
        this.authorUuid = authorUuid;
        this.authorName = authorName;
        this.message = message;
        this.created = created;
        this.sent = sent;
    }
    
    public int getId() {
        return id;
    }
    
    public int getReportId() {
        return reportId;
    }
    
    public UUID getAuthorUuid() {
        return authorUuid;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getCreated() {
        return created;
    }
    
    public boolean isSent() {
        return sent;
    }
    
    public void setSent(boolean sent) {
        this.sent = sent;
    }
    
    public ItemStack getItem(boolean canDelete) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6Comment by §a" + authorName);
            
            String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(created));
            String sentStatus = sent ? "§aSent" : "§7Not sent";
            
            meta.setLore(Arrays.asList(
                "§7Date: §e" + dateStr,
                "§7Status: " + sentStatus,
                "",
                "§7Message:",
                "§f" + message,
                "",
                "§8Left click §7(editing disabled)",
                "§6Right click §7to toggle sent state" + (canDelete ? "" : ""),
                canDelete ? "§6Drop key §7to delete" : ""
            ));
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void delete(BanManagerDatabaseAdapter db, ReportsManager rm) {
        TigerReportsIntegration.getInstance().runTaskAsynchronously(() -> {
            db.deleteComment(this.id, (success) -> {
                if (success) {
                    TigerReportsIntegration.getInstance().getLogger().info("Deleted comment ID: " + this.id);
                    // Notify listeners that comment was deleted
                    rm.notifyCommentDeleted(this.reportId, this.id);
                } else {
                    TigerReportsIntegration.getInstance().getLogger().warning("Failed to delete comment ID: " + this.id);
                }
            });
        });
    }
    
    public void toggleSent(BanManagerDatabaseAdapter db) {
        this.sent = !this.sent;
        TigerReportsIntegration.getInstance().runTaskAsynchronously(() -> {
            db.updateCommentSentStatus(this.id, this.sent, (success) -> {
                if (!success) {
                    TigerReportsIntegration.getInstance().getLogger().warning("Failed to update comment sent status: " + this.id);
                }
            });
        });
    }
}
