package me.confuser.banmanager.bukkit.tigerreports.objects.menus;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.MenuRawItem;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.listeners.CommentChatListener;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.Comment;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.Report;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Comments Menu - Shows and manages comments on reports
 * Layout: 54 slots (6 rows)
 * Features: View comments, add comments, pagination
 */
public class CommentsMenu extends ReportManagerMenu implements UpdatedMenu {
    
    private final VaultManager vm;
    private final Object bm; // BungeeManager placeholder
    private List<Comment> comments = new ArrayList<>();
    private int totalComments = 0;
    
    public CommentsMenu(User u, int page, Report r, ReportsManager rm, BanManagerDatabaseAdapter db,
            TigerReportsIntegration tr, UsersManager um, Object bm, VaultManager vm) {
        super(u, 54, page, Permission.STAFF, r.getId(), rm, db, tr, um);
        this.vm = vm;
        this.bm = bm;
        this.r = r; // Set the report directly since it's passed in
    }
    
    @Override
    public Inventory onOpen() {
        String error = checkReport();
        if (error != null) {
            u.sendErrorMessage(error);
            return null;
        }
        
        Inventory inv = getInventory("§6Comments §7> §eReport #" + r.getId(), false);
        
        // Fill background with glass panes
        ItemStack glass = MenuRawItem.GUI.create();
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }
        
        // Back button (slot 0)
        inv.setItem(0, createBackButton());
        
        // Page info (slot 4)
        inv.setItem(4, createPageInfoButton());
        
        // Add comment button (slot 8)
        inv.setItem(8, createAddCommentButton());
        
        // Load comments asynchronously
        loadComments(inv);
        
        return inv;
    }
    
    private void loadComments(Inventory inv) {
        db.getComments(r.getId(), page, 27, (loadedComments) -> {
            tr.runTask(() -> {
                this.comments = loadedComments;
                this.totalComments = loadedComments.size(); // Simplified for now
                fillCommentsArea(inv);
                updatePaginationButtons(inv);
            });
        });
    }
    
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a« Back to Report");
            meta.setLore(Arrays.asList("§7Return to the report details"));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createPageInfoButton() {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Comments");
            meta.setLore(Arrays.asList(
                "§7Page: §e" + page,
                "§7Total Comments: §e" + r.getCommentsCount(),
                "",
                "§7Staff can add comments to communicate",
                "§7about this report and coordinate actions"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createAddCommentButton() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aAdd Comment");
            meta.setLore(Arrays.asList(
                "§7Add a new comment to this report",
                "",
                "§aClick §7to start writing a comment"
            ));
            item.setItemMeta(meta);
        }
        return item;
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
    
    private void fillCommentsArea(Inventory inv) {
        // Clear comment slots first
        for (int slot = 18; slot < 45; slot++) {
            inv.setItem(slot, null);
        }
        
        // Fill with actual comments
        for (int i = 0; i < comments.size() && i < 27; i++) {
            Comment comment = comments.get(i);
            inv.setItem(18 + i, comment.getItem(u.hasPermission(Permission.STAFF_DELETE)));
        }
    }
    
    private void updatePaginationButtons(Inventory inv) {
        // Previous page button (slot 47)
        if (page > 1) {
            inv.setItem(47, createPreviousPageButton());
        } else {
            inv.setItem(47, MenuRawItem.GUI.create());
        }
        
        // Next page button (slot 51)
        if (hasNextPage()) {
            inv.setItem(51, createNextPageButton());
        } else {
            inv.setItem(51, MenuRawItem.GUI.create());
        }
    }
    
    private boolean hasNextPage() {
        // For now, assume no next page since we load all comments
        // This can be enhanced with proper pagination later
        return false;
    }
    
    @Override
    public void onClick(ItemStack item, int slot, ClickType click) {
        
        switch (slot) {
            case 0: // Back button
                u.openReportMenu(r.getId(), rm, db, tr, vm, bm, um);
                break;
                
            case 8: // Add comment button
                // Start comment session via chat listener
                CommentChatListener chatListener = TigerReportsIntegration.getInstance().getCommentChatListener();
                if (chatListener != null) {
                    chatListener.startCommentSession(u.getPlayer(), r);
                } else {
                    u.sendErrorMessage("§cComment system not available");
                }
                break;
                
            case 47: // Previous page
                if (page > 1) {
                    u.openCommentsMenu(page - 1, r, rm, db, tr, um, bm, vm);
                }
                break;
                
            case 51: // Next page
                if (hasNextPage()) {
                    u.openCommentsMenu(page + 1, r, rm, db, tr, um, bm, vm);
                }
                break;
                
            default:
                // Handle comment clicks (slots 18-44)
                if (slot >= 18 && slot < 45) {
                    int commentIndex = slot - 18;
                    if (commentIndex < comments.size()) {
                        Comment comment = comments.get(commentIndex);
                        
                        if (click == ClickType.LEFT || click == ClickType.SHIFT_LEFT) {
                            // Edit comment - disabled for now due to database limitations
                            u.sendMessage("§eComment editing is not yet supported.");
                            u.sendMessage("§7You can only add new comments or delete existing ones.");
                            u.sendMessage("§7This feature requires database schema changes.");
                        } else if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
                            // Toggle sent state
                            comment.toggleSent(db);
                            u.sendMessage("§aComment sent status toggled");
                            // Refresh the menu
                            tr.runTaskDelayedly(10L, () -> update(false));
                        } else if (click == ClickType.DROP) {
                            // Delete comment
                            if (u.hasPermission(Permission.STAFF_DELETE)) {
                                comment.delete(db, rm);
                                u.sendMessage("§aComment deleted");
                                // Refresh the menu
                                tr.runTaskDelayedly(10L, () -> update(false));
                            } else {
                                u.sendErrorMessage("§cYou don't have permission to delete comments");
                            }
                        }
                    }
                }
                break;
        }
    }
    
    @Override
    public void onUpdate(Inventory inv) {
        // Reload comments from database
        loadComments(inv);
        
        // Update page info
        inv.setItem(4, createPageInfoButton());
    }
}
