package me.confuser.banmanager.bukkit.tigerreports.objects.menus;

import org.bukkit.inventory.Inventory;

/**
 * Interface for menus that can be updated
 * Adapted from original TigerReports UpdatedMenu interface
 */
public interface UpdatedMenu {
    
    void onUpdate(Inventory inv);
}
