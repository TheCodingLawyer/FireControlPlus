package me.confuser.banmanager.bukkit.tigerreports.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Custom item wrapper for TigerReports integration
 * Adapted from original TigerReports CustomItem class
 */
public class CustomItem {
    
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    
    public CustomItem(Material material, String displayName) {
        this(material, displayName, null);
    }
    
    public CustomItem(Material material, String displayName, List<String> lore) {
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
    }
    
    public ItemStack create() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public ItemStack createWithDetails(String details) {
        ItemStack item = create();
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null && details != null) {
            List<String> currentLore = meta.getLore();
            if (currentLore == null) {
                meta.setLore(Arrays.asList(details));
            } else {
                currentLore.add(details);
                meta.setLore(currentLore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public List<String> getLore() {
        return lore;
    }
}
