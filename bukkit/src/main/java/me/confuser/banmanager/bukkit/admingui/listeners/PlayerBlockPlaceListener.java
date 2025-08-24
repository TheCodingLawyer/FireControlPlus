package me.confuser.banmanager.bukkit.admingui.listeners;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.bukkit.admingui.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlayerBlockPlaceListener implements Listener {

	private final AdminGuiIntegration adminGUI;

	public PlayerBlockPlaceListener(AdminGuiIntegration plugin) {
		adminGUI = plugin;

		Bukkit.getPluginManager().registerEvents(this, plugin.getBanManagerPlugin());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		Player p = event.getPlayer();

		if (Settings.freeze.getOrDefault(p.getUniqueId(), false) && AdminGuiIntegration.getInstance().getConf().getBoolean("freeze_place_block", true))
			event.setCancelled(true);
	}

}





