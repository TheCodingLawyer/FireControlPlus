package me.confuser.banmanager.bukkit.admingui.listeners;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.bukkit.admingui.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PlayerJoinListener implements Listener {

	private final AdminGuiIntegration adminGUI;

	public PlayerJoinListener(AdminGuiIntegration plugin) {
		adminGUI = plugin;

		Bukkit.getPluginManager().registerEvents(this, plugin.getBanManagerPlugin());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();

		if (adminGUI.getConf().getBoolean("cjlm_enabled", true)) {
			if (adminGUI.getPlayers().getString(player.getUniqueId().toString(), null) == null) {
				event.setJoinMessage(Message.chat(adminGUI.getConf().getString("first_join_message", "&7[&1+&7] &6{display_name}").replace("{name}", player.getName()).replace("{display_name}", player.getDisplayName())));
			} else {
				event.setJoinMessage(Message.chat(adminGUI.getConf().getString("join_message", "&7[&a+&7] &6{display_name}").replace("{name}", player.getName()).replace("{display_name}", player.getDisplayName())));
			}
		}

		if (adminGUI.getPlayers().getString(player.getUniqueId().toString(), null) == null) {
			adminGUI.getPlayers().set(player.getUniqueId() + ".name", player.getName());
			if (player.getAddress() != null && player.getAddress().getAddress() != null)
				adminGUI.getPlayers().set(player.getUniqueId() + ".ips", new String[]{player.getAddress().getAddress().toString().replace("/", "")});
			adminGUI.getPlayers().set(player.getUniqueId() + ".firstJoin", System.currentTimeMillis());
		} else {
			List<String> ips = adminGUI.getPlayers().getStringList(player.getUniqueId() + ".ips");
			if (player.getAddress() != null && player.getAddress().getAddress() != null)
				if (!ips.contains(player.getAddress().getAddress().toString().replace("/", "")))
					ips.add(player.getAddress().getAddress().toString().replace("/", ""));
			adminGUI.getPlayers().set(player.getUniqueId() + ".ips", ips);
		}
		adminGUI.getPlayers().set(player.getUniqueId() + ".lastJoin", System.currentTimeMillis());
		adminGUI.savePlayers();

		if (adminGUI.getConf().getBoolean("ac_enabled", false)) {
			String color = adminGUI.getPlayers().getString(player.getUniqueId() + ".chatColor");
			if (color != null) Settings.chat_color.put(player.getUniqueId(), color);
		}

		//TODO: Permissions
		if (adminGUI.getConf().getBoolean("mysql", false) && adminGUI.getConf().getBoolean("ap_enabled", false) && adminGUI.getConf().getInt("ap_storage_type", 0) == 2) {
			if (Permissions.Database.rankNeedFix(player.getName())) Permissions.Database.fixRank(player.getUniqueId(), player.getName());
			Permissions.Database.cacheRank(player.getUniqueId());
		}

		if (adminGUI.getConf().getBoolean("ap_enabled", false)) {
			String rank = adminGUI.getPlayers().getString(player.getName() + ".rank", null);
			if (rank != null) {
				adminGUI.getPlayers().set(player.getName(), null);
				adminGUI.getPlayers().set(player.getUniqueId() + ".rank", rank);
				adminGUI.savePlayers();
			}

			//Vault
			if (AdminGuiIntegration.getVaultChat() != null) {
				AdminGuiIntegration.getVaultChat().setPlayerPrefix(player, Permissions.getPrefix(player.getUniqueId()));
				AdminGuiIntegration.getVaultChat().setPlayerSuffix(player, Permissions.getSuffix(player.getUniqueId()));
			}
			TargetPlayer.givePermissions(player);
		}

		if (adminGUI.getConf().getBoolean("bungeecord_enabled", false)) {
			Channel.send(player.getName(), "send", "online_players");
		} else {
			Settings.online_players.add(player.getName());
		}

		Settings.skulls_players.put(player.getName(), Item.pre_createPlayerHead(player.getName()));

		if (adminGUI.getConf().getBoolean("atl_enabled", false)) {
			if (adminGUI.getConf().getBoolean("atl_show_online_players", false)) {
				for (Player p : Bukkit.getOnlinePlayers()) TargetPlayer.refreshPlayerTabList(p);
			} else {
				TargetPlayer.refreshPlayerTabList(player);
			}
		}

		//Update Checker
		if (adminGUI.getConf().getBoolean("uc_enabled", true) && adminGUI.getConf().getInt("uc_send_type", 1) == 1 && AdminGuiIntegration.new_version != null && TargetPlayer.hasPermission(player, "AdminGuiIntegration.admin")) {
			player.sendMessage(Message.getMessage(player.getUniqueId(), "prefix") + Message.chat(adminGUI.getConf().getString("uc_notify", "&aNew update is available. Please update me to &b{version}&a.").replace("{version}", AdminGuiIntegration.new_version)));
		}

		if (adminGUI.getConf().getInt("initialize_gui", 0) == 1) {
			if (!Settings.task_gui.containsKey(player.getUniqueId()))
				Initialize.GUI(player, player.getInventory().getHelmet());
		}

		if (TargetPlayer.hasPermission(player, "AdminGuiIntegration.admin") && AdminGuiIntegration.getInstance().getConf().getBoolean("admin_tools_enabled", true) && adminGUI.getConf().getBoolean("admin_tools_give_on_join", false)) {
			TargetPlayer.giveAdminTools(player, adminGUI.getConf().getInt("admin_tools_give_on_join_slot", 0));
		}

		//Freeze
		if (AdminGuiIntegration.getInstance().getPlayers().getBoolean(player.getUniqueId() + ".frozen", false)) {
			Settings.freeze.put(player.getUniqueId(), true);
			if (AdminGuiIntegration.getInstance().getConf().getString("freeze_title", null) != null && AdminGuiIntegration.getInstance().getConf().getString("freeze_subtitle", null) != null)
				player.sendTitle(Message.chat(AdminGuiIntegration.getInstance().getConf().getString("freeze_title", "")), Message.chat(AdminGuiIntegration.getInstance().getConf().getString("freeze_subtitle", "")), 50, 72000, 50);
		}
		
		//Vanish - handle vanished players on join (hide from EVERYONE, no exceptions)
		// Check if any online players are vanished - hide them from this joining player
		for (Player online : Bukkit.getOnlinePlayers()) {
			if (!online.equals(player) && Settings.vanish.getOrDefault(online.getUniqueId(), false)) {
				player.hidePlayer(adminGUI.getBanManagerPlugin(), online);
			}
		}
		// If this joining player is vanished, hide them from all others
		if (Settings.vanish.getOrDefault(player.getUniqueId(), false)) {
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (!online.equals(player)) {
					online.hidePlayer(adminGUI.getBanManagerPlugin(), player);
				}
			}
		}
	}
}





