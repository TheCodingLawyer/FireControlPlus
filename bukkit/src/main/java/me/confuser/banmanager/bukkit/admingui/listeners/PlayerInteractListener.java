package me.confuser.banmanager.bukkit.admingui.listeners;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.bukkit.admingui.ui.AdminUI;
import me.confuser.banmanager.bukkit.admingui.utils.Message;
import me.confuser.banmanager.bukkit.admingui.utils.Settings;
import me.confuser.banmanager.bukkit.admingui.utils.TargetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

	private final AdminUI adminUI = new AdminUI();
	private final AdminGuiIntegration adminGUI;

	public PlayerInteractListener(AdminGuiIntegration plugin) {
		adminGUI = plugin;

		Bukkit.getPluginManager().registerEvents(this, plugin.getBanManagerPlugin());
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.hasItem()) {
			if (event.getItem() != null) {
				if (event.getItem().hasItemMeta() && event.getItem().getItemMeta() != null) {
					if (event.getItem().getItemMeta().hasLore() && event.getItem().getItemMeta().getLore() != null) {
						if (event.getItem().getItemMeta().getLore().contains(Message.chat(AdminGuiIntegration.getInstance().getConf().getString("admin_tools_lore", "&dClick me to open Admin GUI")))) {
							Player player = event.getPlayer();
							if (player.getOpenInventory().getType() != InventoryType.CHEST) {
								if (TargetPlayer.hasPermission(player, "AdminGuiIntegration.admin")) {
									Settings.target_player.put(player.getUniqueId(), player);
									player.openInventory(adminUI.GUI_Main(player));
								} else {
									player.sendMessage(Message.getMessage(player.getUniqueId(), "prefix") + Message.getMessage(player.getUniqueId(), "permission"));
								}
							}
							event.setCancelled(true);
						}
					}
				}
			}
		}

	}

}





