package me.confuser.banmanager.webenhancer.bukkit.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import fr.mrtigreroux.tigerreports.events.NewReportEvent;
import fr.mrtigreroux.tigerreports.events.ReportStatusChangeEvent;
import fr.mrtigreroux.tigerreports.events.ProcessReportEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Bridge listener to integrate TigerReports events with BanManager WebEnhancer
 * This allows WebEnhancer to log TigerReports activities
 */
public class TigerReportsBridgeListener implements Listener {
    
    private final BanManagerPlugin plugin;
    
    public TigerReportsBridgeListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNewReport(NewReportEvent event) {
        try {
            // Log the new report event
            plugin.getLogger().info("TigerReports: New report created - " + event.getReport().toString());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to log TigerReports new report event: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReportStatusChange(ReportStatusChangeEvent event) {
        try {
            // Log the report status change
            plugin.getLogger().info("TigerReports: Report status changed - " + event.getReport().toString());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to log TigerReports status change event: " + e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProcessReport(ProcessReportEvent event) {
        try {
            // Log the report processing
            plugin.getLogger().info("TigerReports: Report processed - " + event.getReport().toString());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to log TigerReports process report event: " + e.getMessage());
        }
    }
}
