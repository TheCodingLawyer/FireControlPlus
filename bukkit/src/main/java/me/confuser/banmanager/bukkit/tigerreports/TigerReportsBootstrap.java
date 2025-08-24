package me.confuser.banmanager.bukkit.tigerreports;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;

/**
 * Bootstrap class for TigerReports integration with BanManager
 * Following the same pattern as AdminGUI integration
 */
public class TigerReportsBootstrap {
    
    private final BMBukkitPlugin banManagerPlugin;
    private TigerReportsIntegration tigerReportsIntegration;
    
    public TigerReportsBootstrap(BMBukkitPlugin banManagerPlugin) {
        this.banManagerPlugin = banManagerPlugin;
    }
    
    public void enable() {
        try {
            tigerReportsIntegration = new TigerReportsIntegration(banManagerPlugin);
            tigerReportsIntegration.enable();
            banManagerPlugin.getLogger().info("TigerReports integration enabled successfully!");
        } catch (Exception e) {
            banManagerPlugin.getLogger().severe("Failed to enable TigerReports integration: " + e.getMessage());
            throw e;
        }
    }
    
    public void disable() {
        if (tigerReportsIntegration != null) {
            tigerReportsIntegration.disable();
            tigerReportsIntegration = null;
        }
    }
    
    public TigerReportsIntegration getTigerReportsIntegration() {
        return tigerReportsIntegration;
    }
}
