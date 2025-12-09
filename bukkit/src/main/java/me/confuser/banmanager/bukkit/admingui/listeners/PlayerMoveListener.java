package me.confuser.banmanager.bukkit.admingui.listeners;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.bukkit.admingui.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

	private final AdminGuiIntegration adminGUI;

	public PlayerMoveListener(AdminGuiIntegration plugin) {
		adminGUI = plugin;

		Bukkit.getPluginManager().registerEvents(this, plugin.getBanManagerPlugin());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();

		// Freeze - prevent ALL movement (not just block changes)
		if (Settings.freeze.getOrDefault(p.getUniqueId(), false) && AdminGuiIntegration.getInstance().getConf().getBoolean("freeze_player_move", true) && e.getTo() != null) {
			// Cancel any position change (including tiny movements)
			if (e.getFrom().getX() != e.getTo().getX() || 
			    e.getFrom().getY() != e.getTo().getY() || 
			    e.getFrom().getZ() != e.getTo().getZ()) {
				e.setCancelled(true);
				// Teleport player back to exact location to prevent any drift
				p.teleport(e.getFrom());
			}
		}
	}

}





