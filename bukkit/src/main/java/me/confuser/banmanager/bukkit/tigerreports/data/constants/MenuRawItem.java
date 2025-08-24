package me.confuser.banmanager.bukkit.tigerreports.data.constants;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Raw menu item constants for TigerReports integration
 * Adapted from original TigerReports MenuRawItem enum
 */
public enum MenuRawItem {
    
    GUI(Material.GRAY_STAINED_GLASS_PANE, " "),
    GREEN_CLAY(Material.LIME_TERRACOTTA, ""),
    RED_CLAY(Material.RED_TERRACOTTA, "");
    
    private final Material material;
    private final String displayName;
    
    MenuRawItem(Material material, String displayName) {
        this.material = material;
        this.displayName = displayName;
    }
    
    public static void init() {
        // Initialize menu raw items if needed
    }
    
    public ItemStack create() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null && !displayName.isEmpty()) {
                meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', displayName));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    public CustomItemBuilder builder() {
        return new CustomItemBuilder(material);
    }
    
    public static class CustomItemBuilder {
        private final Material material;
        private String name;
        private java.util.List<String> lore;
        
        public CustomItemBuilder(Material material) {
            this.material = material;
        }
        
        public CustomItemBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public CustomItemBuilder lore(String... loreLines) {
            this.lore = java.util.Arrays.asList(loreLines);
            return this;
        }
        
        public ItemStack create() {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (name != null) {
                    meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', name));
                }
                if (lore != null) {
                    java.util.List<String> coloredLore = new java.util.ArrayList<>();
                    for (String line : lore) {
                        coloredLore.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
                    }
                    meta.setLore(coloredLore);
                }
                item.setItemMeta(meta);
            }
            return item;
        }
    }
}
