package me.confuser.banmanager.bukkit.admingui.listeners;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.bukkit.admingui.ui.AdminUI;
import me.confuser.banmanager.bukkit.admingui.utils.Message;
import me.confuser.banmanager.bukkit.admingui.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.Material;

public class InventoryClickListener implements Listener {

	private final AdminGuiIntegration adminGUI;
	private final AdminUI adminUI = new AdminUI();

	public InventoryClickListener(AdminGuiIntegration plugin) {
		adminGUI = plugin;

		Bukkit.getPluginManager().registerEvents(this, plugin.getBanManagerPlugin());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDrag(InventoryDragEvent e) {
		String title = e.getView().getTitle();
		Player p = (Player) e.getWhoClicked();
		
		// Check if this is an AdminGUI inventory
		if (isAdminGUIInventory(title, p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onClick(InventoryClickEvent e) {
		String title = e.getView().getTitle();
		String player = e.getWhoClicked().getName();
		Player p = (Player) e.getWhoClicked();

		if (Settings.freeze.getOrDefault(p.getUniqueId(), false) && adminGUI.getConf().getBoolean("freeze_move_inventory", true)) {
			e.setCancelled(true);
			return;
		}

		try {
			if (isAdminGUIInventory(title, p)) {

				e.setCancelled(true);
				
				// Clear cursor to prevent sticky items
				if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
					e.setCursor(null);
				}

				if (e.getCurrentItem() != null) {
					if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_main"))) {
						adminUI.clicked_main(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), e.isLeftClick());
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_player").replace("{player}", player))) {
						adminUI.clicked_player(p, e.getSlot(), e.getCurrentItem(), e.getInventory());
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_world"))) {
						adminUI.clicked_world(p, e.getSlot(), e.getCurrentItem(), e.getInventory());
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_players"))) {
						adminUI.clicked_players(p, e.getSlot(), e.getCurrentItem(), e.getInventory());
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_plugins"))) {
						adminUI.clicked_plugins(p, e.getSlot(), e.getCurrentItem(), e.getInventory());
					} else if (title.contains(Message.getMessage(p.getUniqueId(), "inventory_commands"))) {
						adminUI.clicked_commands(p, e.getSlot(), e.getCurrentItem(), e.getInventory());
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_unban"))) {
						adminUI.clicked_unban_players(p, e.getSlot(), e.getCurrentItem(), e.getInventory());
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_unmute"))) {
						adminUI.clicked_unmute_players(p, e.getSlot(), e.getCurrentItem(), e.getInventory());
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "players_color").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_players_settings(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()));
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_actions").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_actions(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()));
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_kick").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_kick(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()));
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_ban").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_ban(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()));
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_warn").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_warn(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()));
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_mute").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_mute(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()));
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_potions").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_potions(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()));
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_spawner").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_spawner(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()));
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_money").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_money(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()));
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_money_give").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_money_amount(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()), 1);
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_money_set").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_money_amount(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()), 2);
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_money_take").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_money_amount(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()), 3);
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_inventory").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_inventory(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()), e.isLeftClick());
					} else if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_ender_chest").replace("{player}", Settings.target_player.get(p.getUniqueId()).getName()))) {
						adminUI.clicked_ender_chest(p, e.getSlot(), e.getCurrentItem(), e.getInventory(), Settings.target_player.get(p.getUniqueId()), e.isLeftClick());
					}
				}
			}
		} catch (Exception ex) {
			// Log the exception for debugging instead of silently ignoring it
			adminGUI.getBanManagerPlugin().getLogger().warning("AdminGUI click error: " + ex.getMessage());
			if (adminGUI.getConf().getBoolean("debug", false)) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Optimized method to check if an inventory belongs to AdminGUI
	 * This replaces the massive OR chain for better performance
	 */
	private boolean isAdminGUIInventory(String title, Player p) {
		String player = p.getName();
		
		// Quick checks for common inventories first
		if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_main")) ||
			title.equals(Message.getMessage(p.getUniqueId(), "inventory_world")) ||
			title.equals(Message.getMessage(p.getUniqueId(), "inventory_players")) ||
			title.equals(Message.getMessage(p.getUniqueId(), "inventory_plugins")) ||
			title.equals(Message.getMessage(p.getUniqueId(), "inventory_unban")) ||
			title.equals(Message.getMessage(p.getUniqueId(), "inventory_unmute"))) {
			return true;
		}
		
		// Check player-specific inventories
		if (title.equals(Message.getMessage(p.getUniqueId(), "inventory_player").replace("{player}", player))) {
			return true;
		}
		
		// Check commands inventory (uses contains)
		if (title.contains(Message.getMessage(p.getUniqueId(), "inventory_commands"))) {
			return true;
		}
		
		// Check target player inventories (only if target player exists)
		if (Settings.target_player.containsKey(p.getUniqueId())) {
			String targetPlayerName = Settings.target_player.get(p.getUniqueId()).getName();
			
			return title.equals(Message.getMessage(p.getUniqueId(), "players_color").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_actions").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_kick").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_ban").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_warn").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_mute").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_potions").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_spawner").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_inventory").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_ender_chest").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_money").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_money_give").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_money_set").replace("{player}", targetPlayerName)) ||
				   title.equals(Message.getMessage(p.getUniqueId(), "inventory_money_take").replace("{player}", targetPlayerName));
		}
		
		return false;
	}
}





