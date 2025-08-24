package me.confuser.banmanager.bukkit.admingui;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;

/**
 * Bootstrap class for AdminGUI integration with BanManager
 */
public class AdminGuiBootstrap {
    
    private final BMBukkitPlugin banManagerPlugin;
    private AdminGuiIntegration adminGuiIntegration;
    
    public AdminGuiBootstrap(BMBukkitPlugin banManagerPlugin) {
        this.banManagerPlugin = banManagerPlugin;
    }
    
    public void enable() {
        try {
            adminGuiIntegration = new AdminGuiIntegration(banManagerPlugin);
            adminGuiIntegration.enable();
            banManagerPlugin.getLogger().info("AdminGUI integration enabled successfully!");
        } catch (Exception e) {
            banManagerPlugin.getLogger().severe("Failed to enable AdminGUI integration: " + e.getMessage());
            throw e;
        }
    }
    
    public void disable() {
        if (adminGuiIntegration != null) {
            adminGuiIntegration.disable();
            adminGuiIntegration = null;
        }
    }
    
    public AdminGuiIntegration getAdminGuiIntegration() {
        return adminGuiIntegration;
    }
} 
