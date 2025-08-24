package me.confuser.banmanager.bukkit.tigerreports.listeners;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Player listener for TigerReports integration
 * Adapted from original TigerReports PlayerListener class
 */
public class PlayerListener implements Listener {
    
    private final ReportsManager rm;
    private final BanManagerDatabaseAdapter db;
    private final TigerReportsIntegration integration;
    private final VaultManager vm;
    private final UsersManager um;
    
    public PlayerListener(ReportsManager rm, BanManagerDatabaseAdapter db, TigerReportsIntegration integration,
                         VaultManager vm, UsersManager um) {
        this.rm = rm;
        this.db = db;
        this.integration = integration;
        this.vm = vm;
        this.um = um;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!integration.isLoaded()) {
            return;
        }
        
        // Process user connection
        um.processUserConnection(e.getPlayer());
        
        User u = um.getOnlineUser(e.getPlayer());
        if (u != null) {
            // Additional processing can be added here
            // For example, checking for pending reports notifications
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (!integration.isLoaded()) {
            return;
        }
        
        // Process user disconnection
        um.processUserDisconnection(e.getPlayer());
    }
    

}
