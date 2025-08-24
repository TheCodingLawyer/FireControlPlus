package me.confuser.banmanager.bukkit.tigerreports.tasks.runnables;

import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.objects.menus.UpdatedMenu;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils;

/**
 * Menu updater task for TigerReports integration
 * Adapted from original TigerReports MenuUpdater class
 */
public class MenuUpdater {
    
    private static int taskId = -1;
    
    public static void start() {
        if (taskId != -1) {
            return; // Already running
        }
        
        TigerReportsIntegration integration = TigerReportsIntegration.getInstance();
        if (integration == null) {
            return;
        }
        
        int interval = ConfigUtils.getInt("Config.MenuUpdatesInterval", 5); // Reduced from 10 to 5 seconds for better responsiveness
        if (interval <= 0) {
            return; // Disabled
        }
        
        taskId = integration.runTaskRepeatedly(interval * 1000L, interval * 1000L, () -> {
            if (integration.getUsersManager() != null) {
                for (User user : integration.getUsersManager().getUsers()) {
                    if (user.getOpenedMenu() instanceof UpdatedMenu) {
                        user.getOpenedMenu().update(false);
                    }
                }
            }
        });
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
