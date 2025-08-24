package me.confuser.banmanager.bukkit.tigerreports.managers;

import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.objects.menus.Menu;
import me.confuser.banmanager.bukkit.tigerreports.objects.menus.UpdatedMenu;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Menu Update Manager for TigerReports integration
 * Handles live updates of GUI menus when data changes
 */
public class MenuUpdateManager {
    
    // Track open menus by player UUID
    private final Map<UUID, Menu> openMenus = new ConcurrentHashMap<>();
    
    /**
     * Register a menu as open for a player
     */
    public void registerOpenMenu(UUID playerId, Menu menu) {
        openMenus.put(playerId, menu);
    }
    
    /**
     * Unregister a menu for a player
     */
    public void unregisterOpenMenu(UUID playerId) {
        openMenus.remove(playerId);
    }
    
    /**
     * Get the currently open menu for a player
     */
    public Menu getOpenMenu(UUID playerId) {
        return openMenus.get(playerId);
    }
    
    /**
     * Update all open menus that implement UpdatedMenu
     */
    public void updateAllMenus() {
        for (Map.Entry<UUID, Menu> entry : openMenus.entrySet()) {
            UUID playerId = entry.getKey();
            Menu menu = entry.getValue();
            
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Only update menus that implement UpdatedMenu for better performance
                if (menu instanceof UpdatedMenu) {
                    // Update the menu on the main thread
                    TigerReportsIntegration.getInstance().runTask(() -> {
                        try {
                            menu.update(false);
                        } catch (Exception e) {
                            TigerReportsIntegration.getInstance().getLogger().warning(
                                "Failed to update menu for player " + player.getName() + ": " + e.getMessage()
                            );
                        }
                    });
                }
            } else {
                // Player is offline, remove the menu
                openMenus.remove(playerId);
            }
        }
    }
    
    /**
     * Update menus for a specific report
     */
    public void updateMenusForReport(int reportId) {
        for (Map.Entry<UUID, Menu> entry : openMenus.entrySet()) {
            UUID playerId = entry.getKey();
            Menu menu = entry.getValue();
            
            // Check if this menu is related to the report
            if (isMenuRelatedToReport(menu, reportId)) {
                Player player = org.bukkit.Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    // Update the menu on the main thread with minimal delay for better responsiveness
                    TigerReportsIntegration.getInstance().runTaskDelayedly(1L, () -> {
                        try {
                            menu.update(false);
                        } catch (Exception e) {
                            TigerReportsIntegration.getInstance().getLogger().warning(
                                "Failed to update menu for player " + player.getName() + ": " + e.getMessage()
                            );
                        }
                    });
                } else {
                    // Player is offline, remove the menu
                    openMenus.remove(playerId);
                }
            }
        }
    }
    
    /**
     * Check if a menu is related to a specific report
     */
    private boolean isMenuRelatedToReport(Menu menu, int reportId) {
        // Check if it's a ReportManagerMenu (which has a reportId field)
        if (menu instanceof me.confuser.banmanager.bukkit.tigerreports.objects.menus.ReportManagerMenu) {
            try {
                // Use reflection to get the reportId field
                java.lang.reflect.Field reportIdField = menu.getClass().getSuperclass().getDeclaredField("reportId");
                reportIdField.setAccessible(true);
                int menuReportId = (int) reportIdField.get(menu);
                return menuReportId == reportId;
            } catch (Exception e) {
                // If reflection fails, assume it might be related
                return true;
            }
        }
        
        // For other menus (like ReportsMenu, ArchivedReportsMenu), always update
        // since they show lists of reports
        return true;
    }
    
    /**
     * Clean up offline players
     */
    public void cleanupOfflinePlayers() {
        openMenus.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            return player == null || !player.isOnline();
        });
    }
    
    /**
     * Get the number of tracked menus
     */
    public int getTrackedMenuCount() {
        return openMenus.size();
    }
}
