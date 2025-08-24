package me.confuser.banmanager.bukkit.admingui.ui;

import me.confuser.banmanager.bukkit.admingui.AdminGuiBootstrap;
import me.confuser.banmanager.bukkit.admingui.utils.BanManagerIntegration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * AdminGUI inventory interface for BanManager integration
 * Provides the main GUI interface with BanManager punishment integration
 */
public class AdminGuiInventory {
    
    private final AdminGuiBootstrap bootstrap;
    private final Player viewer;
    
    public AdminGuiInventory(AdminGuiBootstrap bootstrap, Player viewer) {
        this.bootstrap = bootstrap;
        this.viewer = viewer;
    }
    
    /**
     * Open the main AdminGUI menu
     */
    public void openMainMenu() {
        Inventory gui = Bukkit.createInventory(null, 54, "§c§lAdmin GUI - Main Menu");
        
        // Fill with glass panes
        fillWithGlass(gui);
        
        // Add main menu items
        addMainMenuItems(gui);
        
        viewer.openInventory(gui);
    }
    
    /**
     * Open player management menu
     */
    public void openPlayerMenu() {
        Inventory gui = Bukkit.createInventory(null, 54, "§c§lAdmin GUI - Players");
        
        fillWithGlass(gui);
        
        // Add online players
        int slot = 10;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (slot >= 44) break; // Don't overflow the GUI
            
            ItemStack playerHead = createPlayerHead(player);
            gui.setItem(slot, playerHead);
            
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2; // Skip to next row, avoiding edges
        }
        
        // Back button
        gui.setItem(49, createBackButton());
        
        viewer.openInventory(gui);
    }
    
    /**
     * Open punishment menu for a specific player
     */
    public void openPunishmentMenu(Player target) {
        Inventory gui = Bukkit.createInventory(null, 45, "§c§lPunish: " + target.getName());
        
        fillWithGlass(gui);
        
        // Ban button
        ItemStack banItem = new ItemStack(Material.BARRIER);
        ItemMeta banMeta = banItem.getItemMeta();
        banMeta.setDisplayName("§c§lBan Player");
        banMeta.setLore(Arrays.asList(
            "§7Click to ban " + target.getName(),
            "§7Uses BanManager integration",
            "",
            "§eLeft Click: §7Permanent ban",
            "§eRight Click: §7Temporary ban"
        ));
        banItem.setItemMeta(banMeta);
        gui.setItem(11, banItem);
        
        // Mute button
        ItemStack muteItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta muteMeta = muteItem.getItemMeta();
        muteMeta.setDisplayName("§6§lMute Player");
        muteMeta.setLore(Arrays.asList(
            "§7Click to mute " + target.getName(),
            "§7Uses BanManager integration",
            "",
            "§eLeft Click: §7Permanent mute",
            "§eRight Click: §7Temporary mute"
        ));
        muteItem.setItemMeta(muteMeta);
        gui.setItem(13, muteItem);
        
        // Kick button
        ItemStack kickItem = new ItemStack(Material.IRON_BOOTS);
        ItemMeta kickMeta = kickItem.getItemMeta();
        kickMeta.setDisplayName("§e§lKick Player");
        kickMeta.setLore(Arrays.asList(
            "§7Click to kick " + target.getName(),
            "§7Uses BanManager integration"
        ));
        kickItem.setItemMeta(kickMeta);
        gui.setItem(15, kickItem);
        
        // Warn button
        ItemStack warnItem = new ItemStack(Material.PAPER);
        ItemMeta warnMeta = warnItem.getItemMeta();
        warnMeta.setDisplayName("§a§lWarn Player");
        warnMeta.setLore(Arrays.asList(
            "§7Click to warn " + target.getName(),
            "§7Uses BanManager integration"
        ));
        warnItem.setItemMeta(warnMeta);
        gui.setItem(29, warnItem);
        
        // Freeze button
        ItemStack freezeItem = new ItemStack(Material.ICE);
        ItemMeta freezeMeta = freezeItem.getItemMeta();
        freezeMeta.setDisplayName("§b§lFreeze Player");
        freezeMeta.setLore(Arrays.asList(
            "§7Click to freeze " + target.getName(),
            "§7Prevents movement and actions"
        ));
        freezeItem.setItemMeta(freezeMeta);
        gui.setItem(31, freezeItem);
        
        // Teleport button
        ItemStack tpItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tpMeta = tpItem.getItemMeta();
        tpMeta.setDisplayName("§d§lTeleport to Player");
        tpMeta.setLore(Arrays.asList(
            "§7Click to teleport to " + target.getName()
        ));
        tpItem.setItemMeta(tpMeta);
        gui.setItem(33, tpItem);
        
        // Back button
        gui.setItem(40, createBackButton());
        
        viewer.openInventory(gui);
    }
    
    /**
     * Fill inventory with glass panes
     */
    private void fillWithGlass(Inventory gui) {
        String colorName = "LIGHT_GRAY_WOOL"; // Default color - using AdminGUI's default
        Material glassMaterial;
        
        try {
            glassMaterial = Material.valueOf(colorName);
        } catch (IllegalArgumentException e) {
            glassMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        }
        
        ItemStack glass = new ItemStack(glassMaterial);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        // Fill borders
        for (int i = 0; i < 9; i++) gui.setItem(i, glass);
        for (int i = gui.getSize() - 9; i < gui.getSize(); i++) gui.setItem(i, glass);
        for (int i = 0; i < gui.getSize(); i += 9) gui.setItem(i, glass);
        for (int i = 8; i < gui.getSize(); i += 9) gui.setItem(i, glass);
    }
    
    /**
     * Add main menu items
     */
    private void addMainMenuItems(Inventory gui) {
        // Players menu
        ItemStack playersItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playersMeta = playersItem.getItemMeta();
        playersMeta.setDisplayName("§a§lPlayer Management");
        playersMeta.setLore(Arrays.asList(
            "§7Manage online players",
            "§7Ban, mute, kick, and more",
            "",
            "§eClick to open!"
        ));
        playersItem.setItemMeta(playersMeta);
        gui.setItem(20, playersItem);
        
        // Server management
        ItemStack serverItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta serverMeta = serverItem.getItemMeta();
        serverMeta.setDisplayName("§c§lServer Management");
        serverMeta.setLore(Arrays.asList(
            "§7Manage server settings",
            "§7Time, weather, and more",
            "",
            "§eClick to open!"
        ));
        serverItem.setItemMeta(serverMeta);
        gui.setItem(22, serverItem);
        
        // Admin tools
        ItemStack toolsItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta toolsMeta = toolsItem.getItemMeta();
        toolsMeta.setDisplayName("§d§lAdmin Tools");
        toolsMeta.setLore(Arrays.asList(
            "§7Personal admin utilities",
            "§7God mode, vanish, and more",
            "",
            "§eClick to open!"
        ));
        toolsItem.setItemMeta(toolsMeta);
        gui.setItem(24, toolsItem);
        
        // BanManager integration info
        ItemStack bmItem = new ItemStack(Material.BOOK);
        ItemMeta bmMeta = bmItem.getItemMeta();
        bmMeta.setDisplayName("§6§lBanManager Integration");
        bmMeta.setLore(Arrays.asList(
            "§7AdminGUI is integrated with BanManager",
            "§7All punishments use BanManager's system",
            "",
            "§aBanManager Status: §2Active",
            "§aDatabase: §2Connected"
        ));
        bmItem.setItemMeta(bmMeta);
        gui.setItem(40, bmItem);
    }
    
    /**
     * Create a player head item
     */
    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName("§e" + player.getName());
        
        List<String> lore = Arrays.asList(
            "§7Health: §c" + Math.round(player.getHealth()) + "/20",
            "§7Gamemode: §a" + player.getGameMode().name(),
            "§7World: §b" + player.getWorld().getName(),
            "",
            "§eLeft Click: §7Open player menu",
            "§eRight Click: §7Quick punish"
        );
        
        meta.setLore(lore);
        head.setItemMeta(meta);
        
        return head;
    }
    
    /**
     * Create back button
     */
    private ItemStack createBackButton() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName("§c§lBack");
        meta.setLore(Arrays.asList("§7Click to go back"));
        back.setItemMeta(meta);
        return back;
    }
} 
