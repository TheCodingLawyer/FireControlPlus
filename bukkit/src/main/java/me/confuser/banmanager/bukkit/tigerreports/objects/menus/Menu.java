package me.confuser.banmanager.bukkit.tigerreports.objects.menus;

import me.confuser.banmanager.bukkit.tigerreports.data.config.Message;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.MenuItem;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.MenuRawItem;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract menu class for TigerReports integration
 * Adapted from original TigerReports Menu class
 */
public abstract class Menu {
    
    // Static click delay tracking to prevent rapid clicking across all menus
    private static final Map<UUID, Long> lastClickTime = new HashMap<>();
    private static final long CLICK_DELAY_MS = 50; // 50ms delay between clicks for better responsiveness
    
    protected final User u;
    protected final Player p;
    protected final int size;
    protected int page;
    private final Permission permission;
    private boolean isInvCurrentlyModified = false;
    private boolean updateRequested = false;
    
    public Menu(User u, int size, int page, Permission permission) {
        this.u = u;
        this.p = u.getPlayer();
        this.size = size;
        this.page = page;
        this.permission = permission;
    }
    
    protected boolean check() {
        if (!u.isOnline()) {
            return false;
        }
        
        String error = permission != null && !u.hasPermission(permission)
                ? Message.PERMISSION_COMMAND.get()
                : null;
        if (error != null) {
            u.sendErrorMessage(error);
            p.closeInventory();
            return false;
        } else {
            return true;
        }
    }
    
    protected Inventory getInventory(String title, boolean borders) {
        if (title.length() > 32) {
            title = title.substring(0, 29) + "..";
        }
        Inventory inv = Bukkit.createInventory(null, size, title);
        if (borders) {
            ItemStack gui = MenuRawItem.GUI.create();
            int size = inv.getSize();
            for (int position = 9; position < 18; position++) {
                inv.setItem(position, gui);
            }
            for (int position = size - 9; position < size; position++) {
                inv.setItem(position, gui);
            }
            inv.setItem(size - 5, MenuItem.CLOSE.get());
        }
        return inv;
    }
    
    public void open(boolean sound) {
        if (!check()) {
            return;
        }
        
        u.setOpenedMenu(null); // Close previous menu if any
        
        isInvCurrentlyModified = true;
        Inventory inv = onOpen();
        if (inv == null) {
            p.closeInventory();
            isInvCurrentlyModified = false;
            return;
        }
        
        if (this instanceof UpdatedMenu) {
            ((UpdatedMenu) this).onUpdate(inv);
        }
        
        p.openInventory(inv);
        u.setOpenedMenu(this);
        isInvCurrentlyModified = false;
        
        // Register with menu update manager for live updates
        me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration.getInstance()
            .getMenuUpdateManager().registerOpenMenu(p.getUniqueId(), this);
        
        // Play sound if requested
        if (sound) {
            me.confuser.banmanager.bukkit.tigerreports.ConfigSound.MENU.play(p, 
                me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration.getInstance().getConfig());
        }
    }
    
    public void click(ItemStack item, int slot, ClickType click) {
        if (item == null || isInvCurrentlyModified) {
            return;
        }
        
        // Check for click delay to prevent rapid clicking
        UUID playerId = p.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastClick = lastClickTime.get(playerId);
        
        if (lastClick != null && (currentTime - lastClick) < CLICK_DELAY_MS) {
            return; // Ignore click if too soon after last click
        }
        
        lastClickTime.put(playerId, currentTime);
        
        if (slot == size - 5) { // Close button
            p.closeInventory();
            return;
        }
        
        onClick(item, slot, click);
    }
    
    public void update(boolean sound) {
        if (!check() || isInvCurrentlyModified) {
            return;
        }
        
        updateRequested = true;
        
        if (this instanceof UpdatedMenu) {
            Inventory inv = p.getOpenInventory().getTopInventory();
            if (inv != null) {
                isInvCurrentlyModified = true;
                ((UpdatedMenu) this).onUpdate(inv);
                isInvCurrentlyModified = false;
            }
        }
        
        updateRequested = false;
        
        // Play sound if requested
        if (sound) {
            me.confuser.banmanager.bukkit.tigerreports.ConfigSound.MENU.play(p, 
                me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration.getInstance().getConfig());
        }
    }
    
    public abstract Inventory onOpen();
    
    public abstract void onClick(ItemStack item, int slot, ClickType click);
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
}
