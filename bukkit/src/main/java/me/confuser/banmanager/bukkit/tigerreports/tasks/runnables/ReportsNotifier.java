package me.confuser.banmanager.bukkit.tigerreports.tasks.runnables;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Reports notifier task for TigerReports integration
 * Adapted from original TigerReports ReportsNotifier class
 */
public class ReportsNotifier {
    
    private static int taskId = -1;
    
    public static void startIfNeeded(BanManagerDatabaseAdapter db, TigerReportsIntegration integration) {
        if (taskId != -1) {
            return; // Already running
        }
        
        int minutesInterval = ConfigUtils.getInt("Config.Notifications.Staff.MinutesInterval", 0);
        if (minutesInterval <= 0) {
            return; // Disabled
        }
        
        taskId = integration.runTaskRepeatedly(
            minutesInterval * 60 * 1000L, // Initial delay
            minutesInterval * 60 * 1000L, // Repeat interval
            () -> {
                // Get pending reports count and notify staff
                db.getReports(1, 1, "Open", (reports) -> {
                    if (reports != null && !reports.isEmpty()) {
                        integration.runTask(() -> {
                            String message = "§6[TigerReports] §eThere are pending reports to review. Use /reports to view them.";
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (Permission.STAFF.check(player)) {
                                    player.sendMessage(message);
                                }
                            }
                        });
                    }
                });
            }
        );
    }
    
    public static void stop() {
        if (taskId != -1) {
            TigerReportsIntegration integration = TigerReportsIntegration.getInstance();
            if (integration != null) {
                integration.cancelTask(taskId);
            }
            taskId = -1;
        }
    }
}
