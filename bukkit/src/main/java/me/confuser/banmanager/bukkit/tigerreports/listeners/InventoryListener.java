package me.confuser.banmanager.bukkit.tigerreports.listeners;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

/**
 * Inventory listener for TigerReports integration
 * Adapted from original TigerReports InventoryListener class
 */
public class InventoryListener implements Listener {
    
    private final BanManagerDatabaseAdapter db;
    private final UsersManager um;
    
    public InventoryListener(BanManagerDatabaseAdapter db, UsersManager um) {
        this.db = db;
        this.um = um;
    }
    
    @EventHandler(priority = EventPriority.LOW)
    private void onInventoryDrag(InventoryDragEvent e) {
        if (checkMenuAction(e.getWhoClicked(), e.getInventory()) != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    private void onInventoryClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        User u = checkMenuAction(e.getWhoClicked(), inv);
        if (u != null) {
            if (inv.getType() == InventoryType.CHEST) {
                e.setCancelled(true);
                // Always process clicks and clear cursor to prevent sticky items
                if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                    e.setCursor(null); // Clear cursor to prevent sticky items
                }
                u.getOpenedMenu().click(e.getCurrentItem(), e.getSlot(), e.getClick());
            } else if (
                inv.getType() == InventoryType.PLAYER
                        && (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                                || e.getAction() == InventoryAction.COLLECT_TO_CURSOR)
            ) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    private void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }
        
        Player p = (Player) e.getPlayer();
        User u = um.getOnlineUser(p);
        if (u != null) {
            u.setOpenedMenu(null);
            
            // Unregister from menu update manager
            me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration.getInstance()
                .getMenuUpdateManager().unregisterOpenMenu(p.getUniqueId());
            
            // Removed database close operation to improve performance
            // Database connections are managed by the connection pool
        }
    }
    
    private User checkMenuAction(HumanEntity whoClicked, Inventory inv) {
        if (!(whoClicked instanceof Player) || inv == null) {
            return null;
        }
        Player p = (Player) whoClicked;
        User u = um.getOnlineUser(p);
        if (u == null) {
            return null;
        }
        return u.getOpenedMenu() != null ? u : null;
    }
}
