package me.confuser.banmanager.bukkit.admingui.listeners;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.bukkit.admingui.utils.Message;
import me.confuser.banmanager.bukkit.admingui.utils.Settings;
import me.confuser.banmanager.bukkit.admingui.utils.TargetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

	private final AdminGuiIntegration adminGUI;

	public PlayerLoginListener(AdminGuiIntegration plugin) {
		adminGUI = plugin;

		Bukkit.getPluginManager().registerEvents(this, plugin.getBanManagerPlugin());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
			if (Settings.maintenance_mode && !TargetPlayer.hasPermission(event.getPlayer(), "AdminGuiIntegration.maintenance"))
				event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Message.getMessage(event.getPlayer().getUniqueId(), "prefix") + Message.getMessage(event.getPlayer().getUniqueId(), "message_maintenance"));
		}
	}

}





