package me.confuser.banmanager.bukkit.tigerreports.listeners;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.Comment;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.Report;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Chat listener for comment input in TigerReports
 * Handles chat-based comment creation and editing
 */
public class CommentChatListener implements Listener {
    
    private final BanManagerDatabaseAdapter db;
    private final ReportsManager rm;
    private final TigerReportsIntegration tr;
    private final VaultManager vm;
    private final UsersManager um;
    
    // Track players who are currently writing comments
    private final Map<UUID, CommentSession> commentSessions = new HashMap<>();
    
    public CommentChatListener(BanManagerDatabaseAdapter db, ReportsManager rm, TigerReportsIntegration tr,
                              VaultManager vm, UsersManager um) {
        this.db = db;
        this.rm = rm;
        this.tr = tr;
        this.vm = vm;
        this.um = um;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        CommentSession session = commentSessions.get(playerId);
        if (session == null) {
            return; // Player is not writing a comment
        }
        
        event.setCancelled(true); // Cancel the chat message
        
        String message = event.getMessage().trim();
        
        // Check for cancel command
        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("exit")) {
            commentSessions.remove(playerId);
            player.sendMessage("§cComment cancelled.");
            
            // Reopen the comments menu
            tr.runTask(() -> {
                User user = um.getOnlineUser(player);
                if (user != null && session.report != null) {
                    user.openCommentsMenu(1, session.report, rm, db, tr, um, null, vm);
                }
            });
            return;
        }
        
        // Validate message length
        if (message.length() < 3) {
            player.sendMessage("§cComment must be at least 3 characters long. Type 'cancel' to cancel.");
            return;
        }
        
        if (message.length() > 500) {
            player.sendMessage("§cComment is too long (max 500 characters). Type 'cancel' to cancel.");
            return;
        }
        
        // Process the comment
        CommentSession finalSession = session;
        commentSessions.remove(playerId);
        
        if (session.editingComment != null) {
            // Edit existing comment
            editComment(player, session.editingComment, message, finalSession);
        } else {
            // Create new comment
            createComment(player, session.report, message, finalSession);
        }
    }
    
    private void createComment(Player player, Report report, String message, CommentSession session) {
        player.sendMessage("§aCreating comment...");
        
        tr.runTaskAsynchronously(() -> {
            db.createComment(report.getId(), player.getUniqueId(), player.getName(), message, (comment) -> {
                tr.runTask(() -> {
                    if (comment != null) {
                        player.sendMessage("§aComment added successfully!");
                        
                        // Reopen comments menu to show the new comment
                        User user = um.getOnlineUser(player);
                        if (user != null) {
                            user.openCommentsMenu(1, report, rm, db, tr, um, null, vm);
                        }
                        
                        // Notify other staff about the new comment
                        rm.notifyCommentAdded(report.getId(), comment);
                    } else {
                        player.sendMessage("§cFailed to create comment. Please try again.");
                        
                        // Reopen comments menu
                        User user = um.getOnlineUser(player);
                        if (user != null) {
                            user.openCommentsMenu(1, report, rm, db, tr, um, null, vm);
                        }
                    }
                });
            });
        });
    }
    
    private void editComment(Player player, Comment comment, String newMessage, CommentSession session) {
        player.sendMessage("§aUpdating comment...");
        
        tr.runTaskAsynchronously(() -> {
            db.updateComment(comment.getId(), newMessage, (success) -> {
                tr.runTask(() -> {
                    if (success) {
                        player.sendMessage("§aComment updated successfully!");
                    } else {
                        player.sendMessage("§cFailed to update comment. Please try again.");
                    }
                    
                    // Reopen comments menu
                    User user = um.getOnlineUser(player);
                    if (user != null && session.report != null) {
                        user.openCommentsMenu(1, session.report, rm, db, tr, um, null, vm);
                    }
                });
            });
        });
    }
    
    // Start a comment session for a player
    public void startCommentSession(Player player, Report report) {
        UUID playerId = player.getUniqueId();
        commentSessions.put(playerId, new CommentSession(report, null));
        
        player.sendMessage("§6=== Add Comment ===");
        player.sendMessage("§7Type your comment in chat:");
        player.sendMessage("§7(Type 'cancel' to cancel)");
        player.closeInventory();
    }
    
    // Start an edit session for a player
    public void startEditSession(Player player, Report report, Comment comment) {
        UUID playerId = player.getUniqueId();
        commentSessions.put(playerId, new CommentSession(report, comment));
        
        player.sendMessage("§6=== Edit Comment ===");
        player.sendMessage("§7Current message: §f" + comment.getMessage());
        player.sendMessage("§7Type your new comment in chat:");
        player.sendMessage("§7(Type 'cancel' to cancel)");
        player.closeInventory();
    }
    
    // Check if a player is in a comment session
    public boolean isInCommentSession(UUID playerId) {
        return commentSessions.containsKey(playerId);
    }
    
    // Cancel a comment session
    public void cancelCommentSession(UUID playerId) {
        commentSessions.remove(playerId);
    }
    
    // Comment session data
    private static class CommentSession {
        final Report report;
        final Comment editingComment; // null for new comments
        
        CommentSession(Report report, Comment editingComment) {
            this.report = report;
            this.editingComment = editingComment;
        }
    }
}

