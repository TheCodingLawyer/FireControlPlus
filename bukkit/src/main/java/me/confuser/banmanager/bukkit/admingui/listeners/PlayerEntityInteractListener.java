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
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerEntityInteractListener implements Listener {

	private final AdminUI adminUI = new AdminUI();
	private final AdminGuiIntegration adminGUI;

	public PlayerEntityInteractListener(AdminGuiIntegration plugin) {
		adminGUI = plugin;

		Bukkit.getPluginManager().registerEvents(this, plugin.getBanManagerPlugin());
	}

	@EventHandler
	public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			Player player = event.getPlayer();
			Player target = (Player) event.getRightClicked();

			if (player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta() != null && player.getItemInHand().getItemMeta().getLore() != null) {
				if (player.getItemInHand().getItemMeta().getLore().contains(Message.chat(AdminGuiIntegration.getInstance().getConf().getString("admin_tools_lore", "&dClick me to open Admin GUI")))) {
					if (TargetPlayer.hasPermission(player, "AdminGuiIntegration.admin")) {
						Settings.target_player.put(player.getUniqueId(), target);
						if (player.getName().equals(target.getName())) {
							player.openInventory(adminUI.GUI_Player(player));
						} else {
							player.openInventory(adminUI.GUI_Players_Settings(player, target, target.getName()));
						}
					} else {
						player.sendMessage(Message.getMessage(player.getUniqueId(), "prefix") + Message.getMessage(player.getUniqueId(), "permission"));
					}
					event.setCancelled(true);
				}
			}
		}
	}

}





