package me.confuser.banmanager.bukkit.tigerreports.data.constants;

import me.confuser.banmanager.bukkit.tigerreports.objects.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Menu item constants for TigerReports integration
 * Adapted from original TigerReports MenuItem enum
 */
public enum MenuItem {
    
    CLOSE(Material.BARRIER, "&cClose"),
    REPORTS(Material.BOOK, "&6Reports"),
    ARCHIVED_REPORTS(Material.BOOKSHELF, "&6Archived Reports"),
    PREVIOUS_PAGE(Material.ARROW, "&ePrevious Page"),
    NEXT_PAGE(Material.ARROW, "&eNext Page"),
    BACK(Material.ARROW, "&eBack"),
    
    // Report processing items
    PROCESS_TRUE(Material.LIME_WOOL, "&aProcess as True"),
    PROCESS_FALSE(Material.RED_WOOL, "&cProcess as False"),
    PROCESS_ABUSIVE(Material.ORANGE_WOOL, "&6Process as Abusive"),
    
    // Report actions
    TELEPORT_TO_REPORTED(Material.ENDER_PEARL, "&bTeleport to Reported Player"),
    TELEPORT_TO_REPORTER(Material.ENDER_PEARL, "&bTeleport to Reporter"),
    ARCHIVE(Material.CHEST, "&eArchive Report"),
    DELETE(Material.TNT, "&cDelete Report"),
    COMMENT(Material.WRITABLE_BOOK, "&aAdd Comment");
    
    private final Material material;
    private final String displayName;
    
    MenuItem(Material material, String displayName) {
        this.material = material;
        this.displayName = displayName;
    }
    
    public static void init() {
        // Initialize menu items if needed
    }
    
    public ItemStack get() {
        return getCustomItem().create();
    }
    
    public ItemStack getWithDetails(String details) {
        return getCustomItem().createWithDetails(details);
    }
    
    public CustomItem getCustomItem() {
        return new CustomItem(material, ChatColor.translateAlternateColorCodes('&', displayName));
    }
}
