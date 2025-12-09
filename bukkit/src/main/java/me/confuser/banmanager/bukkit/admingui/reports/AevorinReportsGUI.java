package me.confuser.banmanager.bukkit.admingui.reports;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.bukkit.admingui.utils.Item;
import me.confuser.banmanager.bukkit.admingui.utils.Message;
import me.confuser.banmanager.bukkit.admingui.utils.Settings;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * GUI for viewing Aevorin reports
 * Simple, clean interface integrated with AdminGUI
 */
public class AevorinReportsGUI implements Listener {
    
    private final AevorinReportsManager reportsManager;
    
    public AevorinReportsGUI(AdminGuiIntegration plugin) {
        this.reportsManager = new AevorinReportsManager(plugin.getBanManagerPlugin().getPlugin());
        Bukkit.getPluginManager().registerEvents(this, plugin.getBanManagerPlugin());
    }
    
    /**
     * Open reports GUI for player
     */
    public void openReportsGUI(Player player, int page) {
        // Ensure page is at least 1
        if (page < 1) page = 1;
        
        final int finalPage = page; // Make final for lambda
        Bukkit.getScheduler().runTaskAsynchronously(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
            // Always show ALL reports (no filtering)
            List<AevorinReport> reports = reportsManager.getReports(finalPage, "ALL");
            int totalReports = reportsManager.getReportsCount("ALL");
            int totalPages = (int) Math.ceil(totalReports / 45.0); // 45 reports per page now
            
            Bukkit.getScheduler().runTask(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
                Inventory inv = createReportsInventory(player, reports, finalPage, totalPages);
                player.openInventory(inv);
            });
        });
    }
    
    private Inventory createReportsInventory(Player player, List<AevorinReport> reports, int page, int totalPages) {
        String title = Message.chat("&4&lReports &7(Page " + page + "/" + Math.max(1, totalPages) + ")");
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        // Store page info for click handler (no longer storing filter since we show all)
        Settings.reports_page.put(player.getUniqueId(), page);
        Settings.reports_filter.put(player.getUniqueId(), "ALL");
        
        // All reports fill all available slots (no filter buttons)
        // Row 1 (slots 0-8): First 9 reports
        // Row 2 (slots 9-17): Next 9 reports
        // Row 3 (slots 18-26): Next 9 reports
        // Row 4 (slots 27-35): Next 9 reports
        // Row 5 (slots 36-44): Next 9 reports
        // Row 6 (slots 45-53): Last 9 reports + navigation buttons
        
        int slot = 0;
        for (int i = 0; i < Math.min(reports.size(), 45); i++) { // Fill slots 0-44 with reports (5 rows)
            AevorinReport report = reports.get(i);
            ItemStack item = createReportItem(report);
            inv.setItem(slot, item);
            slot++;
        }
        
        // Navigation buttons (bottom row, slots 45-53)
        if (page > 1) {
            createNavigationButton(inv, 45, "&e◀ Previous Page", "ARROW");
        }

        if (page < totalPages) {
            createNavigationButton(inv, 53, "&eNext Page ▶", "ARROW");
        }

        // Back button (center bottom slot 50)
        if (AdminGuiIntegration.gui_type == 1) {
            Item.after_createPlayerHead(inv, Settings.skulls.get("MHF_Redstone"), 1, 50, Message.chat("&c&lBack to Menu"));
        } else {
            Item.create(inv, "REDSTONE_BLOCK", 1, 50, Message.chat("&c&lBack to Menu"));
        }
        
        return inv;
    }
    
    private void createFilterButton(Inventory inv, int slot, String filter, String name, String currentFilter) {
        Material material = Material.PAPER;
        String color = "&7";
        
        if (filter.equals(currentFilter)) {
            material = Material.MAP; // Highlighted
            color = "&a&l";
        }
        
        switch (filter) {
            case "ALL":
                material = filter.equals(currentFilter) ? Material.BOOK : Material.PAPER;
                break;
            case "PENDING":
                material = Material.CLOCK;
                break;
            case "RESOLVED":
                material = Material.LIME_DYE;
                break;
            case "REJECTED":
                material = Material.RED_DYE;
                break;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Message.chat(color + name));
            List<String> lore = new ArrayList<>();
            if (filter.equals(currentFilter)) {
                lore.add(Message.chat("&a✓ Currently viewing"));
            } else {
                lore.add(Message.chat("&7Click to filter"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }
    
    private void createNavigationButton(Inventory inv, int slot, String name, String material) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Message.chat(name));
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }
    
    private ItemStack createReportItem(AevorinReport report) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(Message.chat("&6Report #" + report.getId()));
            
            List<String> lore = new ArrayList<>();
            lore.add(Message.chat("&7Reported: &f" + report.getReportedUsername()));
            lore.add(Message.chat("&7By: &f" + report.getReporterUsername()));
            lore.add(Message.chat("&7Reason: &f" + (report.getReason().length() > 30 ? 
                report.getReason().substring(0, 30) + "..." : report.getReason())));
            lore.add(Message.chat("&7Status: " + report.getStatusDisplay()));
            
            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            String date = sdf.format(new Date(report.getCreatedAt() * 1000));
            lore.add(Message.chat("&7Date: &f" + date));
            
            lore.add("");
            lore.add(Message.chat("&eLeft Click &7to open in admin"));
            lore.add(Message.chat("&eRight Click &7to teleport to player"));
            lore.add(Message.chat("&c&lShift+Right Click &7to DELETE report"));
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        // Check if this is the Reports GUI (title contains "Reports" - works with color codes)
        if (!title.contains("Reports") && !ChatColor.stripColor(title).contains("Reports")) return;
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        
        int slot = event.getRawSlot();
        int page = Settings.reports_page.getOrDefault(player.getUniqueId(), 1);

        // Navigation buttons (no filter buttons)
        if (slot == 45) { // Previous page
            if (page > 1) {
                openReportsGUI(player, page - 1);
            }
            return;
        }

        if (slot == 53) { // Next page
            openReportsGUI(player, page + 1);
            return;
        }

        // Back button (center bottom slot 50)
        if (slot == 50) {
            // Close current inventory first
            player.closeInventory();
            // Open main AdminGUI using a delayed task to ensure inventory is closed
            Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
                me.confuser.banmanager.bukkit.admingui.ui.AdminUI adminUI = new me.confuser.banmanager.bukkit.admingui.ui.AdminUI();
                player.openInventory(adminUI.GUI_Main(player));
            }, 1L); // 1 tick delay
            return;
        }

        // Report clicked (slots 0-44)
        if (slot >= 0 && slot < 45) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = item.getItemMeta().getDisplayName();
                if (displayName.contains("Report #")) {
                    // Extract report ID
                    try {
                        String idStr = displayName.replaceAll("[^0-9]", "");
                        int reportId = Integer.parseInt(idStr);

                        // Get report details
                        if (event.getClick().isShiftClick() && event.getClick().isRightClick()) {
                            // Shift+Right Click - DELETE report from both game and website
                            deleteReport(player, reportId);
                        } else if (event.getClick().isLeftClick()) {
                            // Left Click - Open admin page for the report
                            openReportInAdmin(player, reportId);
                        } else if (event.getClick().isRightClick()) {
                            // Right Click - Teleport to reported player
                            teleportToReportedPlayer(player, item);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(Message.chat("&cInvalid report ID"));
                    }
                }
            }
        }
    }
    
    private void openReportInAdmin(Player player, int reportId) {
        player.closeInventory();

        // Always send players to the main reports dashboard (no per-ID pages in MineTrax)
        String url = "https://theblockcade.com/admin/reports";

        TextComponent prefix = new TextComponent("Report #" + reportId + " (click link below)");
        prefix.setColor(net.md_5.bungee.api.ChatColor.GOLD);

        TextComponent link = new TextComponent(url);
        link.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        link.setUnderlined(true);
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        player.spigot().sendMessage(prefix);
        player.spigot().sendMessage(link);
    }
    
    private void teleportToReportedPlayer(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line.contains("Reported:")) {
                    String username = line.replace("Reported:", "").replaceAll("§.", "").trim();
                    Player target = Bukkit.getPlayer(username);
                    if (target != null && target.isOnline()) {
                        player.teleport(target);
                        player.sendMessage(Message.chat("&aTeleported to &e" + target.getName()));
                    } else {
                        player.sendMessage(Message.chat("&cPlayer is not online"));
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Delete a report from both in-game and website (MineTrax database)
     */
    private void deleteReport(Player player, int reportId) {
        player.sendMessage(Message.chat("&eDeleting report #" + reportId + "..."));
        
        // Run database operation async
        Bukkit.getScheduler().runTaskAsynchronously(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
            boolean success = reportsManager.deleteReport(reportId);
            
            // Return to main thread to update GUI
            Bukkit.getScheduler().runTask(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
                if (success) {
                    player.sendMessage(Message.chat("&a✓ Report #" + reportId + " has been &cDELETED &afrom both game and website!"));
                    // Refresh the reports GUI
                    int page = Settings.reports_page.getOrDefault(player.getUniqueId(), 1);
                    openReportsGUI(player, page);
                } else {
                    player.sendMessage(Message.chat("&c✗ Failed to delete report #" + reportId + ". Check console for errors."));
                }
            });
        });
    }
}

