package me.confuser.banmanager.bukkit.admingui.listeners;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.bukkit.admingui.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayersDroppingItemsListener implements Listener {

	private final AdminGuiIntegration adminGUI;

	public PlayersDroppingItemsListener(AdminGuiIntegration plugin) {
		adminGUI = plugin;

		Bukkit.getPluginManager().registerEvents(this, plugin.getBanManagerPlugin());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDroppingItems(PlayerDropItemEvent event) {
		Player p = event.getPlayer();

		if (Settings.freeze.getOrDefault(p.getUniqueId(), false) && AdminGuiIntegration.getInstance().getConf().getBoolean("freeze_drop_items", true))
			event.setCancelled(true);
	}

}





