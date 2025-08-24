package me.confuser.banmanager.bukkit.admingui.listeners;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.bukkit.admingui.utils.Channel;
import me.confuser.banmanager.bukkit.admingui.utils.Message;
import me.confuser.banmanager.bukkit.admingui.utils.Settings;
import me.confuser.banmanager.bukkit.admingui.utils.TargetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Map;

public class PlayerCommandListener implements Listener {

	private final AdminGuiIntegration adminGUI;

	public PlayerCommandListener(AdminGuiIntegration plugin) {
		adminGUI = plugin;

		Bukkit.getPluginManager().registerEvents(this, plugin.getBanManagerPlugin());
	}

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		String message = event.getMessage();

		// Ensure AdminGUI commands work even if another plugin also registers them
		String lower = message.toLowerCase();
		if (lower.startsWith("/admin ") || lower.equals("/admin")) {
			String suffix = message.length() > 6 ? message.substring(6) : "";
			p.performCommand("banmanager:admin" + suffix);
			event.setCancelled(true);
			return;
		}
		if (lower.startsWith("/adminchat ") || lower.equals("/adminchat")) {
			String suffix = message.length() > 10 ? message.substring(10) : "";
			p.performCommand("banmanager:adminchat" + suffix);
			event.setCancelled(true);
			return;
		}
		if (lower.startsWith("/admincommandspy ") || lower.equals("/admincommandspy")) {
			String suffix = message.length() > 16 ? message.substring(16) : "";
			p.performCommand("banmanager:admincommandspy" + suffix);
			event.setCancelled(true);
			return;
		}

		if (adminGUI.getConf().getBoolean("command_disabler_enabled", false) && !TargetPlayer.hasPermission(p, "AdminGuiIntegration.commanddisabler.bypass")) {
			if (adminGUI.getConf().getConfigurationSection("disabled_commands") != null) {
				for (String command : adminGUI.getConf().getConfigurationSection("disabled_commands").getValues(false).keySet()) {
					if (command.equals(message.split(" ")[0])) {
						p.sendMessage(Message.chat(adminGUI.getConf().getString("disabled_commands." + command, Message.getMessage(p.getUniqueId(), "permission"))));
						event.setCancelled(true);
						return;
					}
				}
			}
		}

		if (Settings.freeze.getOrDefault(p.getUniqueId(), false) && AdminGuiIntegration.getInstance().getConf().getBoolean("freeze_execute_commands", true)) {
			event.setCancelled(true);
			return;
		}

		//RTP Command
		if ((message.equals("/rtp") || message.equals("/wild")) && adminGUI.getConf().getBoolean("rtp_enabled", false)) {
			if (AdminGuiIntegration.getInstance().getConf().getDouble("rtp_delay", 10) > 0 && !TargetPlayer.hasPermission(p, "AdminGuiIntegration.rtp.delay.bypass")) {
				long last_rtp_send = Settings.rtp_delay.getOrDefault(p.getUniqueId(), 0L);
				if (last_rtp_send != 0L) {
					if (last_rtp_send + (AdminGuiIntegration.getInstance().getConf().getDouble("rtp_delay", 10) * 1000) >= System.currentTimeMillis()) {
						p.sendMessage(Message.chat(AdminGuiIntegration.getInstance().getConf().getString("rtp_delay_message", "&cYou need to wait {seconds} seconds, before you can execute /rtp command again!").replace("{seconds}", AdminGuiIntegration.getInstance().getConf().getString("rtp_delay", "10"))));
						event.setCancelled(true);
						return;
					} else {
						Settings.rtp_delay.put(p.getUniqueId(), System.currentTimeMillis());
					}
				} else {
					Settings.rtp_delay.put(p.getUniqueId(), System.currentTimeMillis());
				}
			}

			p.sendMessage(Message.chat(AdminGuiIntegration.getInstance().getConf().getString("rtp_begin", "&aFinding safe location...")));

			if (TargetPlayer.safeTeleport(p)) {
				p.sendMessage(Message.chat(AdminGuiIntegration.getInstance().getConf().getString("rtp_success", "&aYou have been teleported to safe location.")));
			} else {
				p.sendMessage(Message.chat(AdminGuiIntegration.getInstance().getConf().getString("rtp_failed", "&cSafe location to teleport can't be found. Please try again later.")));
			}
			event.setCancelled(true);
		}

		if (adminGUI.getConf().getBoolean("acs_enabled", false)) {
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				if (TargetPlayer.hasPermission(player, "AdminGuiIntegration.chat.spy") && Settings.command_spy.getOrDefault(player.getUniqueId(), false) && !player.getUniqueId().equals(p.getUniqueId())) {
					for (String ignored_command : adminGUI.getConf().getStringList("acs_ignore"))
						if (message.startsWith(ignored_command)) return;
					player.sendMessage(Message.chat(adminGUI.getConf().getString("acs_format").replace("{name}", p.getName()).replace("{display_name}", p.getDisplayName()).replace("{message}", message)));
				}
			}
		}

		// Check if custom command creator section exists
		if (AdminGuiIntegration.getInstance().getConf().getConfigurationSection("ccc") != null) {
			for (Map.Entry<String, Object> slots : AdminGuiIntegration.getInstance().getConf().getConfigurationSection("ccc").getValues(false).entrySet()) {

				List<String> aliases = adminGUI.getConf().getStringList("ccc." + slots.getKey() + ".aliases");

			int firstSpaceIndex = message.indexOf(" ");
			if (firstSpaceIndex == -1) firstSpaceIndex = message.length();

			if (message.startsWith("/" + slots.getKey()) || aliases.contains(message.substring(0, firstSpaceIndex).replace("/", ""))) {
				String name = adminGUI.getConf().getString("ccc." + slots.getKey() + ".name");
				String format = adminGUI.getConf().getString("ccc." + slots.getKey() + ".format");
				String permission = adminGUI.getConf().getString("ccc." + slots.getKey() + ".permission");

				if (name == null) name = slots.getKey();

				if (TargetPlayer.hasPermission(p, permission)) {
					String current_channel = Settings.custom_chat_channel.getOrDefault(p.getUniqueId(), "");

					if (message.replace("/", "").equals(slots.getKey()) || !message.contains(" ")) {
						if (current_channel.equals(slots.getKey())) {
							Settings.custom_chat_channel.remove(p.getUniqueId());
							p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_disabled").replace("{name}", name));
						} else {
							Settings.custom_chat_channel.put(p.getUniqueId(), slots.getKey());
							p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_enabled").replace("{name}", name));
						}
					} else {

						String message2 = message.substring(message.indexOf(" ") + 1);

						for (Player target : Bukkit.getServer().getOnlinePlayers()) {
							if (TargetPlayer.hasPermission(target, permission)) {
								target.sendMessage(Message.chat(format.replace("{name}", p.getName()).replace("{display_name}", p.getDisplayName()).replace("{message}", message2)));
							}
						}
						Bukkit.getConsoleSender().sendMessage(Message.chat(format.replace("{name}", p.getName()).replace("{display_name}", p.getDisplayName()).replace("{message}", message2)));

						if (adminGUI.getConf().getBoolean("bungeecord_enabled", false) && adminGUI.getConf().getBoolean("bungeecord_custom_chat_channels", false)) {
							String serverName = adminGUI.getConf().getString("server_name", "Default");
							Channel.send(p.getUniqueId().toString(), "custom_chat_channels", slots.getKey(), serverName, p.getName(), message2);
						}
					}
				} else {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "permission"));
				}
				event.setCancelled(true);
			}
		}
		} // End of custom command creator null check


	}

}





