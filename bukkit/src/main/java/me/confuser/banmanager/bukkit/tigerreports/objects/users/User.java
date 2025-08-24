package me.confuser.banmanager.bukkit.tigerreports.objects.users;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.menus.*;
import me.confuser.banmanager.bukkit.tigerreports.objects.reports.Report;
import me.confuser.banmanager.bukkit.tigerreports.tasks.ResultCallback;
import me.confuser.banmanager.bukkit.tigerreports.tasks.TaskScheduler;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * User wrapper for TigerReports integration
 * Adapted from original TigerReports User class
 */
public class User {
    
    private final Player player;
    private Menu openedMenu;
    
    public User(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getName() {
        return player.getName();
    }
    
    public UUID getUniqueId() {
        return player.getUniqueId();
    }
    
    public boolean isOnline() {
        return player != null && player.isOnline();
    }
    
    public boolean hasPermission(Permission permission) {
        return permission.check(player);
    }
    
    public Menu getOpenedMenu() {
        return openedMenu;
    }
    
    public void setOpenedMenu(Menu menu) {
        this.openedMenu = menu;
    }
    
    public void sendErrorMessage(String message) {
        if (player != null) {
            player.sendMessage(message);
        }
    }
    
    public void sendMessage(String message) {
        if (player != null) {
            player.sendMessage(message);
        }
    }
    
    // Menu opening methods
    public void openReportsMenu(int page, boolean sound, ReportsManager rm, BanManagerDatabaseAdapter db,
                               TigerReportsIntegration tr, VaultManager vm, Object bm, UsersManager um) {
        ReportsMenu menu = new ReportsMenu(this, page, rm, db, tr, vm, bm, um);
        menu.open(sound);
    }
    
    public void openArchivedReportsMenu(int page, boolean sound, ReportsManager rm, BanManagerDatabaseAdapter db,
                                      TigerReportsIntegration tr, VaultManager vm, Object bm, UsersManager um) {
        ArchivedReportsMenu menu = new ArchivedReportsMenu(this, page, rm, db, tr, vm, bm, um);
        menu.open(sound);
    }
    
    // Individual report menu
    public void openReportMenu(int reportId, ReportsManager rm, BanManagerDatabaseAdapter db,
                              TigerReportsIntegration tr, VaultManager vm, Object bm, UsersManager um) {
        ReportMenu menu = new ReportMenu(this, reportId, rm, db, tr, vm, bm, um);
        menu.open(true);
    }
    
    // Process menu
    public void openProcessMenu(Report report, ReportsManager rm, BanManagerDatabaseAdapter db,
                               TigerReportsIntegration tr, VaultManager vm, Object bm, UsersManager um) {
        ProcessMenu menu = new ProcessMenu(this, report.getId(), rm, db, tr, vm, bm, um);
        menu.open(true);
    }
    
    // Comments menu
    public void openCommentsMenu(int page, Report report, ReportsManager rm, BanManagerDatabaseAdapter db,
                                TigerReportsIntegration tr, UsersManager um, Object bm, VaultManager vm) {
        CommentsMenu menu = new CommentsMenu(this, page, report, rm, db, tr, um, bm, vm);
        menu.open(true);
    }
    
    // User reports menus
    public void openUserReportsMenu(int page, String playerName, ReportsManager rm, BanManagerDatabaseAdapter db,
                                   TigerReportsIntegration tr, VaultManager vm, Object bm, UsersManager um) {
        UserReportsMenu menu = new UserReportsMenu(this, page, playerName, rm, db, tr, vm, bm, um);
        menu.open(true);
    }
    
    public void openUserAgainstReportsMenu(int page, String playerName, ReportsManager rm, BanManagerDatabaseAdapter db,
                                          TigerReportsIntegration tr, VaultManager vm, Object bm, UsersManager um) {
        UserAgainstReportsMenu menu = new UserAgainstReportsMenu(this, page, playerName, rm, db, tr, vm, bm, um);
        menu.open(true);
    }
    
    // Report access and archiving methods
    public boolean canAccessToReport(Report report, boolean allowAccessIfArchived) {
        // Basic permission check - can be expanded later
        return hasPermission(Permission.STAFF) && (allowAccessIfArchived || !"Closed".equals(report.getStatus()));
    }
    
    public boolean canArchive(Report report) {
        return hasPermission(Permission.STAFF_ARCHIVE) && !"Closed".equals(report.getStatus());
    }
    
    // Cooldown methods
    public void getCooldownAsynchronously(BanManagerDatabaseAdapter db, TigerReportsIntegration tr, 
                                        ResultCallback<String> callback) {
        // For now, no cooldown - this can be implemented later
        tr.runTaskAsynchronously(() -> {
            tr.runTask(() -> callback.onResultReceived(null));
        });
    }
    
    // User listener interface for menu updates
    public interface UserListener {
        void onCooldownChange(User u);
        void onStatisticsChange(User u);
    }
}
