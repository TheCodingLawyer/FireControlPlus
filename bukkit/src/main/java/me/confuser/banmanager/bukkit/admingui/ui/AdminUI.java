package me.confuser.banmanager.bukkit.admingui.ui;

import me.confuser.banmanager.bukkit.admingui.utils.BanManagerIntegration;
import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import me.confuser.banmanager.bukkit.admingui.XMaterial;
import me.confuser.banmanager.bukkit.admingui.utils.*;
import me.confuser.banmanager.bukkit.admingui.utils.potions.Version_12;
import me.confuser.banmanager.bukkit.admingui.utils.potions.Version_14;
import me.confuser.banmanager.bukkit.admingui.utils.potions.Version_8;
import me.confuser.banmanager.bukkit.admingui.utils.spawners.materials.*;
import me.confuser.banmanager.bukkit.admingui.utils.spawners.messages.*;
// import de.myzelyam.api.vanish.VanishAPI; // Commented out - optional dependency
import net.milkbowl.vault.economy.EconomyResponse;
// import org.apache.commons.lang.math.NumberUtils; // Commented out - optional dependency
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.bukkit.Bukkit.getServer;
import org.bukkit.ChatColor;
import static org.bukkit.Bukkit.getVersion;

public class AdminUI {

	/**
	 * Helper method to execute punishment - let BanManager handle all messaging
	 * Returns to Actions GUI after punishment is complete
	 */
	private void executePunishmentWithCleanBroadcast(Player admin, Player target, String punishmentType, String reasonKey, Date time, boolean silent, String messageKey) {
		String reason = Message.getMessage(admin.getUniqueId(), reasonKey);
		
		// Execute the punishment - let BanManager handle broadcast messages
		switch (punishmentType) {
			case "ban":
				BanManagerIntegration.banPlayer(admin.getUniqueId().toString(), admin.getName(), target.getUniqueId().toString(), target.getName(), reason, time, silent);
				break;
			case "mute":
				BanManagerIntegration.mutePlayer(admin.getUniqueId().toString(), admin.getName(), target.getUniqueId().toString(), target.getName(), reason, time, silent);
				break;
			case "warn":
				BanManagerIntegration.warnPlayer(admin.getUniqueId().toString(), admin.getName(), target.getUniqueId().toString(), target.getName(), reason, time, silent);
				break;
		}
		
		// Close current inventory and reopen Actions GUI after short delay
		final Player targetRef = target;
		admin.closeInventory();
		Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
			if (admin.isOnline() && targetRef.isOnline()) {
				admin.openInventory(GUI_Actions(admin, targetRef));
			}
		}, 5L); // 5 ticks = 0.25 seconds - enough for punishment to process
		
		// No additional broadcast - BanManager handles this
	}

	//Kick
	private final HashMap<UUID, Boolean> kick_silence = new HashMap<>();

	//Ban
	private final HashMap<UUID, Integer> ban_years = new HashMap<>();
	private final HashMap<UUID, Integer> ban_months = new HashMap<>();
	private final HashMap<UUID, Integer> ban_days = new HashMap<>();
	private final HashMap<UUID, Integer> ban_hours = new HashMap<>();
	private final HashMap<UUID, Integer> ban_minutes = new HashMap<>();
	private final HashMap<UUID, Boolean> ban_silence = new HashMap<>();

    //Warn
    private final HashMap<UUID, Boolean> warn_silence = new HashMap<>();

	//Mute
	private final HashMap<UUID, Integer> mute_years = new HashMap<>();
	private final HashMap<UUID, Integer> mute_months = new HashMap<>();
	private final HashMap<UUID, Integer> mute_days = new HashMap<>();
	private final HashMap<UUID, Integer> mute_hours = new HashMap<>();
	private final HashMap<UUID, Integer> mute_minutes = new HashMap<>();
	private final HashMap<UUID, Boolean> mute_silence = new HashMap<>();

	//Page
	private final HashMap<UUID, Integer> page = new HashMap<>();
	private final HashMap<UUID, Integer> pages = new HashMap<>();

	//Player GUI color
	private final HashMap<UUID, String> gui_color = new HashMap<>();

	//Unban Page
	private final HashMap<UUID, Integer> unban_page = new HashMap<>();
	private final HashMap<UUID, Integer> unban_pages = new HashMap<>();

	//Unmute Page
	private final HashMap<UUID, Integer> unmute_page = new HashMap<>();
	private final HashMap<UUID, Integer> unmute_pages = new HashMap<>();

	//Potions
	private final HashMap<UUID, Integer> duration = new HashMap<>();
	private final HashMap<UUID, Integer> level = new HashMap<>();

	public static String stripNonDigits(final CharSequence input) {
		final StringBuilder sb = new StringBuilder(input.length());
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c > 47 && c < 58) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public Inventory GUI_Main(Player p) {

		Inventory inv_main = Bukkit.createInventory(null, 36, Message.getMessage(p.getUniqueId(), "inventory_main"));

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 36; i++) {
		// 	Item.create(inv_main, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		Item.after_createPlayerHead(inv_main, Settings.skulls_players.get(p.getName()), 1, 11, Message.getMessage(p.getUniqueId(), "main_player").replace("{player}", p.getName()));
		Item.after_createPlayerHead(inv_main, Settings.skulls.get("Black1_TV"), 1, 15, Message.getMessage(p.getUniqueId(), "main_players"));

		if (AdminGuiIntegration.gui_type == 1) {
			Item.after_createPlayerHead(inv_main, Settings.skulls.get("0qt"), 1, 13, Message.getMessage(p.getUniqueId(), "main_world"));
			Item.create(inv_main, "BOOK", 1, 17, "&6&lReports");
			if (Settings.maintenance_mode) {
				Item.after_createPlayerHead(inv_main, Settings.skulls.get("BKing2012"), 1, 28, Message.getMessage(p.getUniqueId(), "main_maintenance_mode"));
			} else {
				Item.after_createPlayerHead(inv_main, Settings.skulls.get("AverageJoe"), 1, 28, Message.getMessage(p.getUniqueId(), "main_maintenance_mode"));
			}
			if (TargetPlayer.hasPermission(p, "admingui.unban") || TargetPlayer.hasPermission(p, "admingui.unmute")) {
				Item.after_createPlayerHead(inv_main, Settings.skulls.get("LobbyPlugin"), 1, 32, Message.getMessage(p.getUniqueId(), "main_unban_players"));
			}
			String currentLangType1 = Settings.language.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("default_language", "English"));
			if (currentLangType1 == null) currentLangType1 = "English";
			Item.after_createPlayerHead(inv_main, Settings.skulls.get("Opp"), 1, 34, Message.getMessage(p.getUniqueId(), "main_language") + currentLangType1);
			Item.after_createPlayerHead(inv_main, Settings.skulls.get("MHF_Redstone"), 1, 36, Message.getMessage(p.getUniqueId(), "main_quit"));
		} else {
			Item.create(inv_main, "GRASS_BLOCK", 1, 13, Message.getMessage(p.getUniqueId(), "main_world"));
			Item.create(inv_main, "BOOK", 1, 17, "&6&lReports");
			if (Settings.maintenance_mode) {
				Item.create(inv_main, "GLOWSTONE_DUST", 1, 28, Message.getMessage(p.getUniqueId(), "main_maintenance_mode"));
			} else {
				Item.create(inv_main, "REDSTONE", 1, 28, Message.getMessage(p.getUniqueId(), "main_maintenance_mode"));
			}
			if (TargetPlayer.hasPermission(p, "admingui.unban") || TargetPlayer.hasPermission(p, "admingui.unmute")) {
				Item.create(inv_main, "BARRIER", 1, 32, Message.getMessage(p.getUniqueId(), "main_unban_players"));
			}
			String currentLang = Settings.language.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("default_language", "English"));
			if (currentLang == null) currentLang = "English";
			Item.create(inv_main, "COMMAND_BLOCK", 1, 34, Message.getMessage(p.getUniqueId(), "main_language") + currentLang);
			Item.create(inv_main, "REDSTONE_BLOCK", 1, 36, Message.getMessage(p.getUniqueId(), "main_quit"));
		}

        // Replace color changer with an Actions button (sword) that opens the player selector
        Item.create(inv_main, "DIAMOND_SWORD", 1, 30, Message.getMessage(p.getUniqueId(), "players_settings_actions"));

		return inv_main;
	}

	public Inventory GUI_Player(Player p) {

		String inventory_player_name = Message.getMessage(p.getUniqueId(), "inventory_player").replace("{player}", p.getName());

		Inventory inv_player = Bukkit.createInventory(null, 45, inventory_player_name);

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 45; i++) {
		// 	Item.create(inv_player, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		if (TargetPlayer.hasPermission(p, "admingui.info")) {
			if (AdminGuiIntegration.getEconomy() != null) {
				if (AdminGuiIntegration.getInstance().getConf().getBoolean("show_ips", true) && p.getAddress() != null && p.getAddress().getAddress() != null) {
					Item.createPlayerHead(inv_player, p.getName(), 1, 5, Message.getMessage(p.getUniqueId(), "player_info").replace("{player}", p.getName()), Message.chat("&eHeal: " + Math.round(p.getHealth())), Message.chat("&7Feed: " + Math.round(p.getFoodLevel())), Message.chat("&2Money: " + AdminGuiIntegration.getEconomy().format(AdminGuiIntegration.getEconomy().getBalance(p.getName()))), Message.chat("&aGamemode: " + p.getGameMode()), Message.chat("&5IP: " + p.getAddress().getAddress().toString().replace("/", "")));
				} else {
					Item.createPlayerHead(inv_player, p.getName(), 1, 5, Message.getMessage(p.getUniqueId(), "player_info").replace("{player}", p.getName()), Message.chat("&eHeal: " + Math.round(p.getHealth())), Message.chat("&7Feed: " + Math.round(p.getFoodLevel())), Message.chat("&2Money: " + AdminGuiIntegration.getEconomy().format(AdminGuiIntegration.getEconomy().getBalance(p.getName()))), Message.chat("&aGamemode: " + p.getGameMode()));
				}
			} else {
				if (AdminGuiIntegration.getInstance().getConf().getBoolean("show_ips", true) && p.getAddress() != null && p.getAddress().getAddress() != null) {
					Item.createPlayerHead(inv_player, p.getName(), 1, 5, Message.getMessage(p.getUniqueId(), "player_info").replace("{player}", p.getName()), Message.chat("&eHeal: " + Math.round(p.getHealth())), Message.chat("&7Feed: " + Math.round(p.getFoodLevel())), Message.chat("&aGamemode: " + p.getGameMode()), Message.chat("&5IP: " + p.getAddress().getAddress().toString().replace("/", "")));
				} else {
					Item.createPlayerHead(inv_player, p.getName(), 1, 5, Message.getMessage(p.getUniqueId(), "player_info").replace("{player}", p.getName()), Message.chat("&eHeal: " + Math.round(p.getHealth())), Message.chat("&7Feed: " + Math.round(p.getFoodLevel())), Message.chat("&aGamemode: " + p.getGameMode()));
				}
			}
		} else {
			Item.createPlayerHead(inv_player, p.getName(), 1, 5, Message.getMessage(p.getUniqueId(), "player_info").replace("{player}", p.getName()));
		}

		if (AdminGuiIntegration.gui_type == 1) {
			if (TargetPlayer.hasPermission(p, "admingui.heal")) {
				Item.after_createPlayerHead(inv_player, Settings.skulls.get("Ground15"), 1, 11, Message.getMessage(p.getUniqueId(), "player_heal"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 11, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.feed")) {
				Item.after_createPlayerHead(inv_player, Settings.skulls.get("Burger_guy"), 1, 13, Message.getMessage(p.getUniqueId(), "player_feed"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 13, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.gamemode")) {
				if (p.getPlayer().getGameMode() == GameMode.SURVIVAL) {
					Item.after_createPlayerHead(inv_player, Settings.skulls.get("Zyne"), 1, 15, Message.getMessage(p.getUniqueId(), "player_survival"));
				} else if (p.getPlayer().getGameMode() == GameMode.ADVENTURE) {
					Item.after_createPlayerHead(inv_player, Settings.skulls.get("Mannahara"), 1, 15, Message.getMessage(p.getUniqueId(), "player_adventure"));
				} else if (p.getPlayer().getGameMode() == GameMode.CREATIVE) {
					Item.after_createPlayerHead(inv_player, Settings.skulls.get("ThaBrick"), 1, 15, Message.getMessage(p.getUniqueId(), "player_creative"));
				} else if (p.getPlayer().getGameMode() == GameMode.SPECTATOR) {
					Item.after_createPlayerHead(inv_player, Settings.skulls.get("3i5g00d"), 1, 15, Message.getMessage(p.getUniqueId(), "player_spectator"));
				}
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 15, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.god")) {
				if (!getVersion().contains("1.8")) {
					if (p.isInvulnerable()) {
						Item.after_createPlayerHead(inv_player, Settings.skulls.get("Ground15"), 1, 17, Message.getMessage(p.getUniqueId(), "player_god_disabled"));
					} else {
						Item.after_createPlayerHead(inv_player, Settings.skulls.get("EDDxample"), 1, 17, Message.getMessage(p.getUniqueId(), "player_god_enabled"));
					}
				} else {
					if (Settings.god.getOrDefault(p.getUniqueId(), false)) {

						Item.after_createPlayerHead(inv_player, Settings.skulls.get("Ground15"), 1, 17, Message.getMessage(p.getUniqueId(), "player_god_disabled"));
					} else {
						Item.after_createPlayerHead(inv_player, Settings.skulls.get("EDDxample"), 1, 17, Message.getMessage(p.getUniqueId(), "player_god_enabled"));
					}
				}
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 17, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.spawner")) {
				Item.after_createPlayerHead(inv_player, Settings.skulls.get("MFH_Spawner"), 1, 21, Message.getMessage(p.getUniqueId(), "player_spawner"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 21, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.kill")) {
				Item.after_createPlayerHead(inv_player, Settings.skulls.get("ZeeFear"), 1, 23, Message.getMessage(p.getUniqueId(), "player_kill"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 23, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.burn")) {
				Item.after_createPlayerHead(inv_player, Settings.skulls.get("haohanklliu"), 1, 25, Message.getMessage(p.getUniqueId(), "player_burn"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 25, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.lightning")) {
				Item.after_createPlayerHead(inv_player, Settings.skulls.get("raichuthink"), 1, 27, Message.getMessage(p.getUniqueId(), "player_lightning"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 27, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.money")) {
				Item.after_createPlayerHead(inv_player, Settings.skulls.get("MrSnowDK"), 1, 31, Message.getMessage(p.getUniqueId(), "player_money"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 31, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.custom")) {
				Item.after_createPlayerHead(inv_player, Settings.skulls.get("Opp"), 1, 35, Message.getMessage(p.getUniqueId(), "player_custom"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 35, Message.getMessage(p.getUniqueId(), "permission"));
			}

			Item.after_createPlayerHead(inv_player, Settings.skulls.get("MHF_Redstone"), 1, 45, Message.getMessage(p.getUniqueId(), "player_back"));
		} else {
			if (TargetPlayer.hasPermission(p, "admingui.heal")) {
				Item.create(inv_player, "GOLDEN_APPLE", 1, 11, Message.getMessage(p.getUniqueId(), "player_heal"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 11, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.feed")) {
				Item.create(inv_player, "COOKED_BEEF", 1, 13, Message.getMessage(p.getUniqueId(), "player_feed"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 13, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.gamemode")) {
				if (p.getPlayer().getGameMode() == GameMode.SURVIVAL) {
					Item.create(inv_player, "DIRT", 1, 15, Message.getMessage(p.getUniqueId(), "player_survival"));
				} else if (p.getPlayer().getGameMode() == GameMode.ADVENTURE) {
					Item.create(inv_player, "GRASS_BLOCK", 1, 15, Message.getMessage(p.getUniqueId(), "player_adventure"));
				} else if (p.getPlayer().getGameMode() == GameMode.CREATIVE) {
					Item.create(inv_player, "BRICKS", 1, 15, Message.getMessage(p.getUniqueId(), "player_creative"));
				} else if (p.getPlayer().getGameMode() == GameMode.SPECTATOR) {
					if (getVersion().contains("1.8")) {
						Item.create(inv_player, "POTION", 1, 15, Message.getMessage(p.getUniqueId(), "player_spectator"));
					} else {
						Item.create(inv_player, "SPLASH_POTION", 1, 15, Message.getMessage(p.getUniqueId(), "player_spectator"));
					}
				}
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 15, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.god")) {
				if (!getVersion().contains("1.8")) {
					if (p.isInvulnerable()) {
						Item.create(inv_player, "RED_TERRACOTTA", 1, 17, Message.getMessage(p.getUniqueId(), "player_god_disabled"));
					} else {
						Item.create(inv_player, "LIME_TERRACOTTA", 1, 17, Message.getMessage(p.getUniqueId(), "player_god_enabled"));
					}
				} else {
					if (Settings.god.getOrDefault(p.getUniqueId(), false)) {
						Item.create(inv_player, "RED_TERRACOTTA", 1, 17, Message.getMessage(p.getUniqueId(), "player_god_disabled"));
					} else {
						Item.create(inv_player, "LIME_TERRACOTTA", 1, 17, Message.getMessage(p.getUniqueId(), "player_god_enabled"));
					}
				}
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 17, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.spawner")) {
				Item.create(inv_player, "SPAWNER", 1, 21, Message.getMessage(p.getUniqueId(), "player_spawner"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 21, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.kill")) {
				Item.create(inv_player, "DIAMOND_SWORD", 1, 23, Message.getMessage(p.getUniqueId(), "player_kill"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 23, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.burn")) {
				Item.create(inv_player, "FLINT_AND_STEEL", 1, 25, Message.getMessage(p.getUniqueId(), "player_burn"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 25, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.lightning")) {
				Item.create(inv_player, "STICK", 1, 27, Message.getMessage(p.getUniqueId(), "player_lightning"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 27, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.money")) {
				Item.create(inv_player, "PAPER", 1, 31, Message.getMessage(p.getUniqueId(), "player_money"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 31, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.custom")) {
				Item.create(inv_player, "COMMAND_BLOCK", 1, 35, Message.getMessage(p.getUniqueId(), "player_custom"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 35, Message.getMessage(p.getUniqueId(), "permission"));
			}

			Item.create(inv_player, "REDSTONE_BLOCK", 1, 45, Message.getMessage(p.getUniqueId(), "player_back"));
		}

		if (TargetPlayer.hasPermission(p, "admingui.potions")) {
			Item.create(inv_player, "POTION", 1, 19, Message.getMessage(p.getUniqueId(), "player_potions"));
		} else {
			Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 19, Message.getMessage(p.getUniqueId(), "permission"));
		}

		if (TargetPlayer.hasPermission(p, "admingui.firework")) {
			Item.create(inv_player, "FIREWORK_ROCKET", 1, 29, Message.getMessage(p.getUniqueId(), "player_firework"));
		} else {
			Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 29, Message.getMessage(p.getUniqueId(), "permission"));
		}

		if (TargetPlayer.hasPermission(p, "admingui.vanish")) {
			// Check if player is vanished using our built-in system
			boolean isVanished = Settings.vanish.getOrDefault(p.getUniqueId(), false);
				if (isVanished) {
					Item.create(inv_player, "FEATHER", 1, 33, Message.getMessage(p.getUniqueId(), "player_vanish_disabled"));
			} else {
				Item.create(inv_player, "FEATHER", 1, 33, Message.getMessage(p.getUniqueId(), "player_vanish_enabled"));
			}
		} else {
			Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 33, Message.getMessage(p.getUniqueId(), "permission"));
		}

		if (AdminGuiIntegration.getInstance().getConf().getBoolean("ac_enabled")) {
			if (TargetPlayer.hasPermission(p, "admingui.chat.color.change")) {
				Item.create(inv_player, Settings.chat_color.getOrDefault(p.getUniqueId(), "LIGHT_GRAY_WOOL"), 1, 37, Message.getMessage(p.getUniqueId(), "player_chat_color"));
			} else {
				Item.create(inv_player, "RED_STAINED_GLASS_PANE", 1, 37, Message.getMessage(p.getUniqueId(), "permission"));
			}
		}

		return inv_player;
	}

	private Inventory GUI_World(Player p) {

		Inventory inv_world = Bukkit.createInventory(null, 27, Message.getMessage(p.getUniqueId(), "inventory_world"));

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 27; i++) {
		// 	Item.create(inv_world, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		if (AdminGuiIntegration.gui_type == 1) {
			if (TargetPlayer.hasPermission(p, "admingui.time")) {
				if (p.getPlayer().getWorld().getTime() < 13000) {
					Item.after_createPlayerHead(inv_world, Settings.skulls.get("Ground15"), 1, 11, Message.getMessage(p.getUniqueId(), "world_day"));
				} else {
					Item.after_createPlayerHead(inv_world, Settings.skulls.get("EDDxample"), 1, 11, Message.getMessage(p.getUniqueId(), "world_night"));
				}
			} else {
				Item.create(inv_world, "RED_STAINED_GLASS_PANE", 1, 11, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.weather")) {
				if (p.getPlayer().getWorld().isThundering()) {
					Item.after_createPlayerHead(inv_world, Settings.skulls.get("LapisBlock"), 1, 13, Message.getMessage(p.getUniqueId(), "world_thunder"));
				} else if (p.getPlayer().getWorld().hasStorm()) {
					Item.after_createPlayerHead(inv_world, Settings.skulls.get("emack0714"), 1, 13, Message.getMessage(p.getUniqueId(), "world_rain"));
				} else {
					Item.after_createPlayerHead(inv_world, Settings.skulls.get("Super_Sniper"), 1, 13, Message.getMessage(p.getUniqueId(), "world_clear"));
				}
			} else {
				Item.create(inv_world, "RED_STAINED_GLASS_PANE", 1, 13, Message.getMessage(p.getUniqueId(), "permission"));
			}

			Item.after_createPlayerHead(inv_world, Settings.skulls.get("MHF_Redstone"), 1, 27, Message.getMessage(p.getUniqueId(), "world_back"));
		} else {
			if (TargetPlayer.hasPermission(p, "admingui.time")) {
				if (p.getPlayer().getWorld().getTime() < 13000) {
					Item.create(inv_world, "GOLD_BLOCK", 1, 11, Message.getMessage(p.getUniqueId(), "world_day"));
				} else {
					Item.create(inv_world, "COAL_BLOCK", 1, 11, Message.getMessage(p.getUniqueId(), "world_night"));
				}
			} else {
				Item.create(inv_world, "RED_STAINED_GLASS_PANE", 1, 11, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.weather")) {
				if (p.getPlayer().getWorld().isThundering()) {
					Item.create(inv_world, "BLUE_TERRACOTTA", 1, 13, Message.getMessage(p.getUniqueId(), "world_thunder"));
				} else if (p.getPlayer().getWorld().hasStorm()) {
					Item.create(inv_world, "CYAN_TERRACOTTA", 1, 13, Message.getMessage(p.getUniqueId(), "world_rain"));
				} else {
					Item.create(inv_world, "LIGHT_BLUE_TERRACOTTA", 1, 13, Message.getMessage(p.getUniqueId(), "world_clear"));
				}
			} else {
				Item.create(inv_world, "RED_STAINED_GLASS_PANE", 1, 13, Message.getMessage(p.getUniqueId(), "permission"));
			}

			Item.create(inv_world, "REDSTONE_BLOCK", 1, 27, Message.getMessage(p.getUniqueId(), "world_back"));
		}

		return inv_world;
	}

	private Inventory GUI_Players(Player p) {

		Inventory inv_players = Bukkit.createInventory(null, 54, Message.getMessage(p.getUniqueId(), "inventory_players"));

		int online = Settings.online_players.size();

		pages.put(p.getUniqueId(), (int) Math.ceil((float) online / 45));

		// Background tiles removed for cleaner look
		// for (int i = 1; i <= 53; i++) {
		// 	Item.create(inv_players, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		if (AdminGuiIntegration.gui_type == 1) {
			if (page.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.after_createPlayerHead(inv_players, Settings.skulls.get("MHF_ArrowLeft"), 1, 49, Message.getMessage(p.getUniqueId(), "players_previous"));
			}

			if (pages.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.after_createPlayerHead(inv_players, Settings.skulls.get("MHF_Question"), page.getOrDefault(p.getUniqueId(), 1), 50, Message.getMessage(p.getUniqueId(), "players_page") + " " + page.getOrDefault(p.getUniqueId(), 1));
			}

			if (pages.getOrDefault(p.getUniqueId(), 1) > page.getOrDefault(p.getUniqueId(), 1)) {
				Item.after_createPlayerHead(inv_players, Settings.skulls.get("MHF_ArrowRight"), 1, 51, Message.getMessage(p.getUniqueId(), "players_next"));
			}

			Item.after_createPlayerHead(inv_players, Settings.skulls.get("MHF_Redstone"), 1, 54, Message.getMessage(p.getUniqueId(), "players_back"));
		} else {
			if (page.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.create(inv_players, "PAPER", 1, 49, Message.getMessage(p.getUniqueId(), "players_previous"));
			}

			if (pages.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.create(inv_players, "BOOK", page.getOrDefault(p.getUniqueId(), 1), 50, Message.getMessage(p.getUniqueId(), "players_page") + " " + page.getOrDefault(p.getUniqueId(), 1));
			}

			if (pages.getOrDefault(p.getUniqueId(), 1) > page.getOrDefault(p.getUniqueId(), 1)) {
				Item.create(inv_players, "PAPER", 1, 51, Message.getMessage(p.getUniqueId(), "players_next"));
			}

			Item.create(inv_players, "REDSTONE_BLOCK", 1, 54, Message.getMessage(p.getUniqueId(), "players_back"));
		}

		Bukkit.getScheduler().runTaskAsynchronously(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
			int player_slot = (page.getOrDefault(p.getUniqueId(), 1) - 1) * 45;

			for (int i = 0; i < 45; i++) {
				if (player_slot < online) {
					Item.createPlayerHead(inv_players, Settings.online_players.get(player_slot), 1, i + 1, Message.getMessage(p.getUniqueId(), "players_color").replace("{player}", Settings.online_players.get(player_slot)), Message.getMessage(p.getUniqueId(), "players_more"));
					player_slot++;
				}
			}
		});

		return inv_players;
	}

	private Inventory GUI_Plugins(Player p) {

		ConfigurationSection one;
		YamlConfiguration yamlConfiguration;

		switch (Settings.custom_method.getOrDefault(p.getUniqueId(), 0)) {
			case 1:
				one = AdminGuiIntegration.getInstance().getComm().getConfigurationSection("plugins");
				yamlConfiguration = AdminGuiIntegration.getInstance().getComm();
				break;
			case 2:
				one = AdminGuiIntegration.getInstance().getComo().getConfigurationSection("plugins");
				yamlConfiguration = AdminGuiIntegration.getInstance().getComo();
				break;
			default:
				one = AdminGuiIntegration.getInstance().getPlug().getConfigurationSection("plugins");
				yamlConfiguration = AdminGuiIntegration.getInstance().getPlug();
				break;
		}

		Inventory inv_plugins = Bukkit.createInventory(null, 54, Message.getMessage(p.getUniqueId(), "inventory_plugins"));

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 54; i++) {
		// 	Item.create(inv_plugins, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		for (Map.Entry<String, Object> plug_slot : one.getValues(false).entrySet()) {
			int i = Integer.parseInt(plug_slot.getKey());
			Item.create(inv_plugins, yamlConfiguration.getString("plugins." + i + ".material"), 1, i, yamlConfiguration.getString("plugins." + i + ".name"));
		}

		if (AdminGuiIntegration.gui_type == 1) {
			Item.after_createPlayerHead(inv_plugins, Settings.skulls.get("MHF_Redstone"), 1, 54, Message.getMessage(p.getUniqueId(), "plugins_back"));
		} else {
			Item.create(inv_plugins, "REDSTONE_BLOCK", 1, 54, Message.getMessage(p.getUniqueId(), "plugins_back"));
		}

		return inv_plugins;
	}

	private Inventory GUI_Commands(Player p, int slot) {

		ConfigurationSection two;
		YamlConfiguration yamlConfiguration;

		switch (Settings.custom_method.getOrDefault(p.getUniqueId(), 0)) {
			case 1:
				two = AdminGuiIntegration.getInstance().getComm().getConfigurationSection("plugins." + slot + ".commands");
				yamlConfiguration = AdminGuiIntegration.getInstance().getComm();
				break;
			case 2:
				two = AdminGuiIntegration.getInstance().getComo().getConfigurationSection("plugins." + slot + ".commands");
				yamlConfiguration = AdminGuiIntegration.getInstance().getComo();
				break;
			default:
				two = AdminGuiIntegration.getInstance().getPlug().getConfigurationSection("plugins." + slot + ".commands");
				yamlConfiguration = AdminGuiIntegration.getInstance().getPlug();
				break;
		}

		Inventory inv_commands = Bukkit.createInventory(null, 54, Message.chat(yamlConfiguration.getString("plugins." + slot + ".name")) + " " + Message.getMessage(p.getUniqueId(), "inventory_commands"));

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 54; i++) {
		// 	Item.create(inv_commands, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		for (Map.Entry<String, Object> comm_slot : two.getValues(false).entrySet()) {
			int j = Integer.parseInt(comm_slot.getKey());
			if (yamlConfiguration.getString("plugins." + slot + ".commands." + slot + ".permission") != null) {
				if (TargetPlayer.hasPermission(p, yamlConfiguration.getString("plugins." + slot + ".commands." + j + ".permission"))) {
					Item.create(inv_commands, yamlConfiguration.getString("plugins." + slot + ".commands." + j + ".material"), 1, j, yamlConfiguration.getString("plugins." + slot + ".commands." + j + ".name"));
				} else {
					Item.create(inv_commands, "RED_STAINED_GLASS_PANE", 1, j, Message.getMessage(p.getUniqueId(), "permission"));
				}
			} else {
				Item.create(inv_commands, yamlConfiguration.getString("plugins." + slot + ".commands." + j + ".material"), 1, j, yamlConfiguration.getString("plugins." + slot + ".commands." + j + ".name"));
			}
		}

		if (AdminGuiIntegration.gui_type == 1) {
			Item.after_createPlayerHead(inv_commands, Settings.skulls.get("MHF_Redstone"), 1, 54, Message.getMessage(p.getUniqueId(), "commands_back"));
		} else {
			Item.create(inv_commands, "REDSTONE_BLOCK", 1, 54, Message.getMessage(p.getUniqueId(), "commands_back"));
		}

		return inv_commands;
	}

	public Inventory GUI_Unban_Players(Player p) {

		Inventory inv_unban_players = Bukkit.createInventory(null, 54, Message.getMessage(p.getUniqueId(), "inventory_unban"));

		// Background tiles removed for cleaner look
		// for (int i = 1; i <= 53; i++) {
		// 	Item.create(inv_unban_players, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		if (AdminGuiIntegration.gui_type == 1) {
			if (unban_page.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.after_createPlayerHead(inv_unban_players, Settings.skulls.get("MHF_ArrowLeft"), 1, 49, Message.getMessage(p.getUniqueId(), "unban_previous"));
			}

			if (unban_pages.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.after_createPlayerHead(inv_unban_players, Settings.skulls.get("MHF_Question"), unban_page.getOrDefault(p.getUniqueId(), 1), 50, Message.getMessage(p.getUniqueId(), "unban_page") + " " + unban_page.getOrDefault(p.getUniqueId(), 1));
			}

			if (unban_pages.getOrDefault(p.getUniqueId(), 1) > unban_page.getOrDefault(p.getUniqueId(), 1)) {
				Item.after_createPlayerHead(inv_unban_players, Settings.skulls.get("MHF_ArrowRight"), 1, 51, Message.getMessage(p.getUniqueId(), "unban_next"));
			}

			Item.after_createPlayerHead(inv_unban_players, Settings.skulls.get("MHF_Redstone"), 1, 54, Message.getMessage(p.getUniqueId(), "unban_back"));
		} else {
			if (unban_page.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.create(inv_unban_players, "PAPER", 1, 49, Message.getMessage(p.getUniqueId(), "unban_previous"));
			}

			if (unban_pages.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.create(inv_unban_players, "BOOK", unban_page.getOrDefault(p.getUniqueId(), 1), 50, Message.getMessage(p.getUniqueId(), "unban_page") + " " + unban_page.getOrDefault(p.getUniqueId(), 1));
			}

			if (unban_pages.getOrDefault(p.getUniqueId(), 1) > unban_page.getOrDefault(p.getUniqueId(), 1)) {
				Item.create(inv_unban_players, "PAPER", 1, 51, Message.getMessage(p.getUniqueId(), "unban_next"));
			}

			Item.create(inv_unban_players, "REDSTONE_BLOCK", 1, 54, Message.getMessage(p.getUniqueId(), "unban_back"));
		}

		if (BanManagerIntegration.isBanManagerEnabled()) {
			// Use BanManager's database to get banned players
			ArrayList<String> pl = new ArrayList<>(BanManagerIntegration.getBannedPlayerNames());
			int online = pl.size();
			unban_pages.put(p.getUniqueId(), (int) Math.ceil((float) online / 45));

			Bukkit.getScheduler().runTaskAsynchronously(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				int player_slot = (unban_page.getOrDefault(p.getUniqueId(), 1) - 1) * 45;

				for (int i = 0; i < 45; i++) {
					if (player_slot < online) {
						String playerName = pl.get(player_slot);
						BannedPlayerIn banInfo = BanManagerIntegration.getBanInfo(playerName);
						
						if (banInfo != null) {
							String createdStr = banInfo.created != null ? banInfo.created.toString() : "Unknown";
							String expiresStr = banInfo.until != null ? banInfo.until.toString() : "Never";
							String reasonStr = banInfo.reason != null ? banInfo.reason : "No reason";
							
							Item.createPlayerHead(inv_unban_players, playerName, 1, i + 1, 
								Message.getMessage(p.getUniqueId(), "unban_color").replace("{player}", playerName), 
								Message.chat("&aBanned: &6" + createdStr), 
								Message.chat("&aExpiration: &6" + expiresStr),
								Message.chat("&aReason: &6" + reasonStr),
								" ", 
								Message.getMessage(p.getUniqueId(), "unban_more"));
						}
						player_slot++;
					}
				}
			});
		} else {
			// Fallback to vanilla/LiteBans logic
			ArrayList<String> pl = new ArrayList<>();

			for (OfflinePlayer all : getServer().getBannedPlayers()) pl.add(all.getName());
			int online = pl.size();
			unban_pages.put(p.getUniqueId(), (int) Math.ceil((float) online / 45));

			Bukkit.getScheduler().runTaskAsynchronously(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {

				int player_slot = (unban_page.getOrDefault(p.getUniqueId(), 1) - 1) * 45;

				for (int i = 0; i < 45; i++) {
					if (player_slot < online) {
						if (Bukkit.getBanList(BanList.Type.NAME).getBanEntry(pl.get(player_slot)) == null) {
							player_slot++;
							continue;
						}
						if (Bukkit.getBanList(BanList.Type.NAME).getBanEntry(pl.get(player_slot)).getExpiration() == null) {
							Item.createPlayerHead(inv_unban_players, pl.get(player_slot), 1, i + 1, Message.getMessage(p.getUniqueId(), "unban_color").replace("{player}", pl.get(player_slot)), Message.chat("&aBanned: &6" + Bukkit.getBanList(BanList.Type.NAME).getBanEntry(pl.get(player_slot)).getCreated()), Message.chat("&aExpiration: &6Never"), " ", Message.getMessage(p.getUniqueId(), "unban_more"));
						} else {
							Item.createPlayerHead(inv_unban_players, pl.get(player_slot), 1, i + 1, Message.getMessage(p.getUniqueId(), "unban_color").replace("{player}", pl.get(player_slot)), Message.chat("&aBanned: &6" + Bukkit.getBanList(BanList.Type.NAME).getBanEntry(pl.get(player_slot)).getCreated()), Message.chat("&aExpiration: &6" + Bukkit.getBanList(BanList.Type.NAME).getBanEntry(pl.get(player_slot)).getExpiration()), " ", Message.getMessage(p.getUniqueId(), "unban_more"));
						}
						player_slot++;
					}
				}

			});
		}

		return inv_unban_players;
	}

	public Inventory GUI_Unmute_Players(Player p) {

		Inventory inv_unmute_players = Bukkit.createInventory(null, 54, Message.getMessage(p.getUniqueId(), "inventory_unmute"));

		// Background tiles removed for cleaner look
		// for (int i = 1; i <= 53; i++) {
		// 	Item.create(inv_unmute_players, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		if (AdminGuiIntegration.gui_type == 1) {
			if (unmute_page.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.after_createPlayerHead(inv_unmute_players, Settings.skulls.get("MHF_ArrowLeft"), 1, 49, Message.getMessage(p.getUniqueId(), "unmute_previous"));
			}

			if (unmute_pages.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.after_createPlayerHead(inv_unmute_players, Settings.skulls.get("MHF_Question"), unmute_page.getOrDefault(p.getUniqueId(), 1), 50, Message.getMessage(p.getUniqueId(), "unmute_page") + " " + unmute_page.getOrDefault(p.getUniqueId(), 1));
			}

			if (unmute_pages.getOrDefault(p.getUniqueId(), 1) > unmute_page.getOrDefault(p.getUniqueId(), 1)) {
				Item.after_createPlayerHead(inv_unmute_players, Settings.skulls.get("MHF_ArrowRight"), 1, 51, Message.getMessage(p.getUniqueId(), "unmute_next"));
			}

			Item.after_createPlayerHead(inv_unmute_players, Settings.skulls.get("MHF_Redstone"), 1, 54, Message.getMessage(p.getUniqueId(), "unmute_back"));
		} else {
			if (unmute_page.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.create(inv_unmute_players, "PAPER", 1, 49, Message.getMessage(p.getUniqueId(), "unmute_previous"));
			}

			if (unmute_pages.getOrDefault(p.getUniqueId(), 1) > 1) {
				Item.create(inv_unmute_players, "BOOK", unmute_page.getOrDefault(p.getUniqueId(), 1), 50, Message.getMessage(p.getUniqueId(), "unmute_page") + " " + unmute_page.getOrDefault(p.getUniqueId(), 1));
			}

			if (unmute_pages.getOrDefault(p.getUniqueId(), 1) > unmute_page.getOrDefault(p.getUniqueId(), 1)) {
				Item.create(inv_unmute_players, "PAPER", 1, 51, Message.getMessage(p.getUniqueId(), "unmute_next"));
			}

			Item.create(inv_unmute_players, "REDSTONE_BLOCK", 1, 54, Message.getMessage(p.getUniqueId(), "unmute_back"));
		}

		if (BanManagerIntegration.isBanManagerEnabled()) {

			java.util.List<me.confuser.banmanager.common.data.PlayerMuteData> abs = BanManagerIntegration.getMutedPlayers();
			int online = abs != null ? abs.size() : 0;
			unmute_pages.put(p.getUniqueId(), (int) Math.ceil((float) online / 45));

			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Bukkit.getScheduler().runTaskAsynchronously(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				int player_slot = (unmute_page.getOrDefault(p.getUniqueId(), 1) - 1) * 45;

				for (int i = 0; i < 45; i++) {
					if (player_slot < online && abs != null) {
						me.confuser.banmanager.common.data.PlayerMuteData mute = abs.get(player_slot);
						Item.createPlayerHead(inv_unmute_players, mute.getPlayer().getName(), 1, i + 1, Message.getMessage(p.getUniqueId(), "unmute_color").replace("{player}", mute.getPlayer().getName()), Message.chat("&aMuted by: &6" + mute.getActor().getName()), Message.chat("&aMuted on: &6" + sdf.format(new Date(mute.getCreated() * 1000))), Message.chat("&aExpiration: &6" + (mute.getExpires() == 0 ? "Never" : sdf.format(new Date(mute.getExpires() * 1000)))), " ", Message.getMessage(p.getUniqueId(), "unmute_more"));
						player_slot++;
					}
				}
			});
		}
		return inv_unmute_players;
	}

	public Inventory GUI_Players_Settings(Player p, Player target_player, String target_name) {

		String inventory_players_settings_name = Message.getMessage(p.getUniqueId(), "players_color").replace("{player}", target_name);
		Inventory inv_players_settings = Bukkit.createInventory(null, 27, inventory_players_settings_name);

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 27; i++) {
		// 	Item.create(inv_players_settings, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		if (TargetPlayer.hasPermission(p, "admingui.info")) {
			//TODO: Bungee
			if (AdminGuiIntegration.getInstance().getConf().getBoolean("bungeecord_enabled", false) && target_player == null) {
				Item.createPlayerHead(inv_players_settings, target_name, 1, 5, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target_name));
			} else {
				if (AdminGuiIntegration.getEconomy() != null) {
					if (AdminGuiIntegration.getInstance().getConf().getBoolean("show_ips", true) && target_player.getAddress() != null && target_player.getAddress().getAddress() != null) {
						Item.createPlayerHead(inv_players_settings, target_name, 1, 5, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target_name), Message.chat("&eHeal: " + Math.round(target_player.getHealth())), Message.chat("&7Feed: " + Math.round(target_player.getFoodLevel())), Message.chat("&2Money: " + AdminGuiIntegration.getEconomy().format(AdminGuiIntegration.getEconomy().getBalance(target_name))), Message.chat("&aGamemode: " + target_player.getGameMode()), Message.chat("&5IP: " + target_player.getAddress().getAddress().toString().replace("/", "")));
					} else {
						Item.createPlayerHead(inv_players_settings, target_name, 1, 5, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target_name), Message.chat("&eHeal: " + Math.round(target_player.getHealth())), Message.chat("&7Feed: " + Math.round(target_player.getFoodLevel())), Message.chat("&2Money: " + AdminGuiIntegration.getEconomy().format(AdminGuiIntegration.getEconomy().getBalance(target_name))), Message.chat("&aGamemode: " + target_player.getGameMode()));
					}
				} else {
					if (AdminGuiIntegration.getInstance().getConf().getBoolean("show_ips", true) && target_player.getAddress() != null && target_player.getAddress().getAddress() != null) {
						Item.createPlayerHead(inv_players_settings, target_name, 1, 5, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target_name), Message.chat("&eHeal: " + Math.round(target_player.getHealth())), Message.chat("&7Feed: " + Math.round(target_player.getFoodLevel())), Message.chat("&aGamemode: " + target_player.getGameMode()), Message.chat("&5IP: " + target_player.getAddress().getAddress().toString().replace("/", "")));
					} else {
						Item.createPlayerHead(inv_players_settings, target_name, 1, 5, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target_name), Message.chat("&eHeal: " + Math.round(target_player.getHealth())), Message.chat("&7Feed: " + Math.round(target_player.getFoodLevel())), Message.chat("&aGamemode: " + target_player.getGameMode()));
					}
				}
			}
		} else {
			Item.createPlayerHead(inv_players_settings, target_name, 1, 5, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target_name));
		}

		if (AdminGuiIntegration.gui_type == 1) {
			// Warn button (slot 11)
			if (TargetPlayer.hasPermission(p, "admingui.warn")) {
				Item.after_createPlayerHead(inv_players_settings, Settings.skulls.get("Push_red_button"), 1, 11, Message.getMessage(p.getUniqueId(), "players_settings_warn_player"));
			} else {
				Item.create(inv_players_settings, "RED_STAINED_GLASS_PANE", 1, 11, Message.getMessage(p.getUniqueId(), "permission"));
			}

			// Mute button (slot 13 - with proper spacing)
			if (TargetPlayer.hasPermission(p, "admingui.mute")) {
				Item.after_createPlayerHead(inv_players_settings, Settings.skulls.get("LobbyPlugin"), 1, 13, Message.getMessage(p.getUniqueId(), "players_settings_mute_player"));
			} else {
				Item.create(inv_players_settings, "RED_STAINED_GLASS_PANE", 1, 13, Message.getMessage(p.getUniqueId(), "permission"));
			}

			// Ban button (slot 17 - with proper spacing)
			if (TargetPlayer.hasPermission(p, "admingui.ban")) {
				Item.after_createPlayerHead(inv_players_settings, Settings.skulls.get("MHF_TNT2"), 1, 17, Message.getMessage(p.getUniqueId(), "players_settings_ban_player"));
			} else {
				Item.create(inv_players_settings, "RED_STAINED_GLASS_PANE", 1, 17, Message.getMessage(p.getUniqueId(), "permission"));
			}

			// Kick button (slot 15 - with proper spacing)
			if (TargetPlayer.hasPermission(p, "admingui.kick.other")) {
				Item.after_createPlayerHead(inv_players_settings, Settings.skulls.get("MHF_Enderman"), 1, 15, Message.getMessage(p.getUniqueId(), "players_settings_kick_player"));
			} else {
				Item.create(inv_players_settings, "RED_STAINED_GLASS_PANE", 1, 15, Message.getMessage(p.getUniqueId(), "permission"));
			}

			// Actions button (slot 23 - bottom row under player head)
			Item.after_createPlayerHead(inv_players_settings, Settings.skulls.get("ZiGmUnDo"), 1, 23, Message.getMessage(p.getUniqueId(), "players_settings_actions"));

			Item.after_createPlayerHead(inv_players_settings, Settings.skulls.get("MHF_Redstone"), 1, 27, Message.getMessage(p.getUniqueId(), "players_settings_back"));
		} else {
			// Warn button (slot 11)
			if (TargetPlayer.hasPermission(p, "admingui.warn")) {
				Item.create(inv_players_settings, "BELL", 1, 11, Message.getMessage(p.getUniqueId(), "players_settings_warn_player"));
			} else {
				Item.create(inv_players_settings, "RED_STAINED_GLASS_PANE", 1, 11, Message.getMessage(p.getUniqueId(), "permission"));
			}

			// Mute button (slot 13 - change to NOTE_BLOCK to avoid goat horn/bell text)
			if (TargetPlayer.hasPermission(p, "admingui.mute")) {
				Item.create(inv_players_settings, "NOTE_BLOCK", 1, 13, Message.getMessage(p.getUniqueId(), "players_settings_mute_player"));
			} else {
				Item.create(inv_players_settings, "RED_STAINED_GLASS_PANE", 1, 13, Message.getMessage(p.getUniqueId(), "permission"));
			}

			// Ban button (slot 17 - with proper spacing)
			if (TargetPlayer.hasPermission(p, "admingui.ban")) {
				Item.create(inv_players_settings, "BARRIER", 1, 17, Message.getMessage(p.getUniqueId(), "players_settings_ban_player"));
			} else {
				Item.create(inv_players_settings, "RED_STAINED_GLASS_PANE", 1, 17, Message.getMessage(p.getUniqueId(), "permission"));
			}

			// Kick button (slot 15 - with proper spacing)
			if (TargetPlayer.hasPermission(p, "admingui.kick.other")) {
				Item.create(inv_players_settings, "RABBIT_FOOT", 1, 15, Message.getMessage(p.getUniqueId(), "players_settings_kick_player"));
			} else {
				Item.create(inv_players_settings, "RED_STAINED_GLASS_PANE", 1, 15, Message.getMessage(p.getUniqueId(), "permission"));
			}


			Item.create(inv_players_settings, "REDSTONE_BLOCK", 1, 27, Message.getMessage(p.getUniqueId(), "players_settings_back"));
		}

		return inv_players_settings;
	}

	public Inventory GUI_Actions(Player p, Player target) {

		String inventory_actions_name = Message.getMessage(p.getUniqueId(), "inventory_actions").replace("{player}", target.getName());
		Settings.target_player.put(p.getUniqueId(), target);

		Inventory inv_actions = Bukkit.createInventory(null, 54, inventory_actions_name);

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 54; i++) {
		// 	Item.create(inv_actions, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		if (TargetPlayer.hasPermission(p, "admingui.info")) {
			if (AdminGuiIntegration.getEconomy() != null) {
				if (AdminGuiIntegration.getInstance().getConf().getBoolean("show_ips", true) && target.getAddress() != null && target.getAddress().getAddress() != null) {
					Item.createPlayerHead(inv_actions, target.getName(), 1, 5, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target.getName()), Message.chat("&eHeal: " + Math.round(target.getHealth())), Message.chat("&7Feed: " + Math.round(target.getFoodLevel())), Message.chat("&2Money: " + AdminGuiIntegration.getEconomy().format(AdminGuiIntegration.getEconomy().getBalance(target.getName()))), Message.chat("&aGamemode: " + target.getGameMode()), Message.chat("&5IP: " + target.getAddress().getAddress().toString().replace("/", "")));
				} else {
					Item.createPlayerHead(inv_actions, target.getName(), 1, 5, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target.getName()), Message.chat("&eHeal: " + Math.round(target.getHealth())), Message.chat("&7Feed: " + Math.round(target.getFoodLevel())), Message.chat("&2Money: " + AdminGuiIntegration.getEconomy().format(AdminGuiIntegration.getEconomy().getBalance(target.getName()))), Message.chat("&aGamemode: " + target.getGameMode()));
				}
			} else {
				if (AdminGuiIntegration.getInstance().getConf().getBoolean("show_ips", true) && target.getAddress() != null && target.getAddress().getAddress() != null) {
					Item.createPlayerHead(inv_actions, target.getName(), 1, 5, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target.getName()), Message.chat("&eHeal: " + Math.round(target.getHealth())), Message.chat("&7Feed: " + Math.round(target.getFoodLevel())), Message.chat("&aGamemode: " + target.getGameMode()), Message.chat("&5IP: " + target.getAddress().getAddress().toString().replace("/", "")));
				} else {
					Item.createPlayerHead(inv_actions, target.getName(), 1, 5, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target.getName()), Message.chat("&eHeal: " + Math.round(target.getHealth())), Message.chat("&7Feed: " + Math.round(target.getFoodLevel())), Message.chat("&aGamemode: " + target.getGameMode()));
				}
			}
		} else {
			Item.createPlayerHead(inv_actions, target.getName(), 1, 5, Message.getMessage(p.getUniqueId(), "actions_info").replace("{player}", target.getName()));
		}

		if (AdminGuiIntegration.gui_type == 1) {
			if (TargetPlayer.hasPermission(p, "admingui.heal.other")) {
				Item.after_createPlayerHead(inv_actions, Settings.skulls.get("IM_"), 1, 11, Message.getMessage(p.getUniqueId(), "actions_heal"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 11, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.feed.other")) {
				Item.after_createPlayerHead(inv_actions, Settings.skulls.get("Burger_guy"), 1, 13, Message.getMessage(p.getUniqueId(), "actions_feed"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 13, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.gamemode.other")) {
				if (target.getGameMode() == GameMode.SURVIVAL) {
					Item.after_createPlayerHead(inv_actions, Settings.skulls.get("Zyne"), 1, 15, Message.getMessage(p.getUniqueId(), "actions_survival"));
				} else if (target.getGameMode() == GameMode.ADVENTURE) {
					Item.after_createPlayerHead(inv_actions, Settings.skulls.get("Mannahara"), 1, 15, Message.getMessage(p.getUniqueId(), "actions_adventure"));
				} else if (target.getGameMode() == GameMode.CREATIVE) {
					Item.after_createPlayerHead(inv_actions, Settings.skulls.get("ThaBrick"), 1, 15, Message.getMessage(p.getUniqueId(), "actions_creative"));
				} else if (target.getGameMode() == GameMode.SPECTATOR) {
					Item.after_createPlayerHead(inv_actions, Settings.skulls.get("3i5g00d"), 1, 15, Message.getMessage(p.getUniqueId(), "actions_spectator"));
				}
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 15, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.god.other")) {
				if (!getVersion().contains("1.8")) {
					if (target.isInvulnerable()) {
						Item.after_createPlayerHead(inv_actions, Settings.skulls.get("Ground15"), 1, 17, Message.getMessage(p.getUniqueId(), "actions_god_disabled"));
					} else {
						Item.after_createPlayerHead(inv_actions, Settings.skulls.get("EDDxample"), 1, 17, Message.getMessage(p.getUniqueId(), "actions_god_enabled"));
					}
				} else {
					if (Settings.god.getOrDefault(target.getUniqueId(), false)) {
						Item.after_createPlayerHead(inv_actions, Settings.skulls.get("Ground15"), 1, 17, Message.getMessage(p.getUniqueId(), "actions_god_disabled"));
					} else {
						Item.after_createPlayerHead(inv_actions, Settings.skulls.get("EDDxample"), 1, 17, Message.getMessage(p.getUniqueId(), "actions_god_enabled"));
					}
				}
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 17, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.kill.other")) {
				Item.after_createPlayerHead(inv_actions, Settings.skulls.get("ZeeFear"), 1, 23, Message.getMessage(p.getUniqueId(), "actions_kill_player"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 23, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.spawner.other")) {
				Item.after_createPlayerHead(inv_actions, Settings.skulls.get("MFH_Spawner"), 1, 25, Message.getMessage(p.getUniqueId(), "actions_spawner"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 25, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.inventory")) {
				Item.after_createPlayerHead(inv_actions, Settings.skulls.get("ElMarcosFTW"), 1, 29, Message.getMessage(p.getUniqueId(), "actions_inventory"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 29, Message.getMessage(p.getUniqueId(), "permission"));
			}

			//TODO: Ender Chest
			if (TargetPlayer.hasPermission(p, "admingui.enderchest")) {
				Item.after_createPlayerHead(inv_actions, Settings.skulls.get("ElMarcosFTW"), 1, 31, Message.getMessage(p.getUniqueId(), "actions_ender_chest"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 31, Message.getMessage(p.getUniqueId(), "permission"));
			}

			// Money button (slot 39 - two slots down from potion button)
			if (TargetPlayer.hasPermission(p, "admingui.money.other")) {
				Item.after_createPlayerHead(inv_actions, Settings.skulls.get("MrSnowDK"), 1, 39, Message.getMessage(p.getUniqueId(), "actions_money"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 39, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.burn.other")) {
				Item.after_createPlayerHead(inv_actions, Settings.skulls.get("haohanklliu"), 1, 33, Message.getMessage(p.getUniqueId(), "actions_burn_player"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 33, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.lightning.other")) {
				Item.after_createPlayerHead(inv_actions, Settings.skulls.get("raichuthink"), 1, 37, Message.getMessage(p.getUniqueId(), "actions_lightning"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 37, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.custom")) {
				Item.after_createPlayerHead(inv_actions, Settings.skulls.get("Opp"), 1, 43, Message.getMessage(p.getUniqueId(), "actions_custom"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 43, Message.getMessage(p.getUniqueId(), "permission"));
			}

			Item.after_createPlayerHead(inv_actions, Settings.skulls.get("MHF_Redstone"), 1, 54, Message.getMessage(p.getUniqueId(), "actions_back"));
		} else {
			if (TargetPlayer.hasPermission(p, "admingui.heal.other")) {
				Item.create(inv_actions, "GOLDEN_APPLE", 1, 11, Message.getMessage(p.getUniqueId(), "actions_heal"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 11, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.feed.other")) {
				Item.create(inv_actions, "COOKED_BEEF", 1, 13, Message.getMessage(p.getUniqueId(), "actions_feed"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 13, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.gamemode.other")) {
				if (target.getGameMode() == GameMode.SURVIVAL) {
					Item.create(inv_actions, "DIRT", 1, 15, Message.getMessage(p.getUniqueId(), "actions_survival"));
				} else if (target.getGameMode() == GameMode.ADVENTURE) {
					Item.create(inv_actions, "GRASS_BLOCK", 1, 15, Message.getMessage(p.getUniqueId(), "actions_adventure"));
				} else if (target.getGameMode() == GameMode.CREATIVE) {
					Item.create(inv_actions, "BRICKS", 1, 15, Message.getMessage(p.getUniqueId(), "actions_creative"));
				} else if (target.getGameMode() == GameMode.SPECTATOR) {
					if (getVersion().contains("1.8")) {
						Item.create(inv_actions, "POTION", 1, 15, Message.getMessage(p.getUniqueId(), "actions_spectator"));
					} else {
						Item.create(inv_actions, "SPLASH_POTION", 1, 15, Message.getMessage(p.getUniqueId(), "actions_spectator"));
					}
				}
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 15, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.god.other")) {
				if (!getVersion().contains("1.8")) {
					if (target.isInvulnerable()) {
						Item.create(inv_actions, "RED_TERRACOTTA", 1, 17, Message.getMessage(p.getUniqueId(), "actions_god_disabled"));
					} else {
						Item.create(inv_actions, "LIME_TERRACOTTA", 1, 17, Message.getMessage(p.getUniqueId(), "actions_god_enabled"));
					}
				} else {
					if (Settings.god.getOrDefault(target.getUniqueId(), false)) {
						Item.create(inv_actions, "RED_TERRACOTTA", 1, 17, Message.getMessage(p.getUniqueId(), "actions_god_disabled"));
					} else {
						Item.create(inv_actions, "LIME_TERRACOTTA", 1, 17, Message.getMessage(p.getUniqueId(), "actions_god_enabled"));
					}
				}
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 17, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.kill.other")) {
				Item.create(inv_actions, "DIAMOND_SWORD", 1, 23, Message.getMessage(p.getUniqueId(), "actions_kill_player"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 23, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.spawner.other")) {
				Item.create(inv_actions, "SPAWNER", 1, 25, Message.getMessage(p.getUniqueId(), "actions_spawner"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 25, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.inventory")) {
				Item.create(inv_actions, "BOOK", 1, 29, Message.getMessage(p.getUniqueId(), "actions_inventory"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 29, Message.getMessage(p.getUniqueId(), "permission"));
			}

			// Money button (slot 39 - two slots down from potion button)
			if (TargetPlayer.hasPermission(p, "admingui.money.other")) {
				Item.create(inv_actions, "PAPER", 1, 39, Message.getMessage(p.getUniqueId(), "actions_money"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 39, Message.getMessage(p.getUniqueId(), "permission"));
			}

			//TODO: Ender Chest
			if (TargetPlayer.hasPermission(p, "admingui.enderchest")) {
				Item.create(inv_actions, "ENDER_CHEST", 1, 31, Message.getMessage(p.getUniqueId(), "actions_ender_chest"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 31, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.burn.other")) {
				Item.create(inv_actions, "FLINT_AND_STEEL", 1, 33, Message.getMessage(p.getUniqueId(), "actions_burn_player"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 33, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.lightning.other")) {
				if (getVersion().contains("1.8") || getVersion().contains("1.9") || getVersion().contains("1.10") || getVersion().contains("1.11") || getVersion().contains("1.12")) {
					Item.create(inv_actions, "STICK", 1, 37, Message.getMessage(p.getUniqueId(), "actions_lightning"));
				} else {
					Item.create(inv_actions, "TRIDENT", 1, 37, Message.getMessage(p.getUniqueId(), "actions_lightning"));
				}
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 37, Message.getMessage(p.getUniqueId(), "permission"));
			}

			if (TargetPlayer.hasPermission(p, "admingui.custom")) {
				Item.create(inv_actions, "COMMAND_BLOCK", 1, 43, Message.getMessage(p.getUniqueId(), "actions_custom"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 43, Message.getMessage(p.getUniqueId(), "permission"));
			}

			Item.create(inv_actions, "REDSTONE_BLOCK", 1, 54, Message.getMessage(p.getUniqueId(), "actions_back"));
		}

		if (TargetPlayer.hasPermission(p, "admingui.teleport")) {
			Item.create(inv_actions, "ENDER_PEARL", 1, 19, Message.getMessage(p.getUniqueId(), "actions_teleport_to_player"));
		} else {
			Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 19, Message.getMessage(p.getUniqueId(), "permission"));
		}

		if (TargetPlayer.hasPermission(p, "admingui.potions.other")) {
			Item.create(inv_actions, "POTION", 1, 21, Message.getMessage(p.getUniqueId(), "actions_potions"));
		} else {
			Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 21, Message.getMessage(p.getUniqueId(), "permission"));
		}

		if (TargetPlayer.hasPermission(p, "admingui.teleport.other")) {
			if (Bukkit.getVersion().contains("1.8")) {
				Item.create(inv_actions, "ENDER_PEARL", 1, 27, Message.getMessage(p.getUniqueId(), "actions_teleport_player_to_you"));
			} else {
				Item.create(inv_actions, "END_CRYSTAL", 1, 27, Message.getMessage(p.getUniqueId(), "actions_teleport_player_to_you"));
			}
		} else {
			Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 27, Message.getMessage(p.getUniqueId(), "permission"));
		}

		if (TargetPlayer.hasPermission(p, "admingui.vanish.other")) {
			// Check if target is vanished using our built-in system
			boolean isTargetVanished = Settings.vanish.getOrDefault(target.getUniqueId(), false);
			if (isTargetVanished) {
					Item.create(inv_actions, "FEATHER", 1, 35, Message.getMessage(p.getUniqueId(), "actions_vanish_disabled"));
			} else {
				Item.create(inv_actions, "FEATHER", 1, 35, Message.getMessage(p.getUniqueId(), "actions_vanish_enabled"));
			}
		} else {
			Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 35, Message.getMessage(p.getUniqueId(), "permission"));
		}

		if (TargetPlayer.hasPermission(p, "admingui.firework.other")) {
			Item.create(inv_actions, "FIREWORK_ROCKET", 1, 41, Message.getMessage(p.getUniqueId(), "actions_firework"));
		} else {
			Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 41, Message.getMessage(p.getUniqueId(), "permission"));
		}


		if (AdminGuiIntegration.getInstance().getConf().getBoolean("ac_enabled")) {
			if (TargetPlayer.hasPermission(p, "admingui.chat.color.change.other")) {
				Item.create(inv_actions, Settings.chat_color.getOrDefault(target.getUniqueId(), "LIGHT_GRAY_WOOL"), 1, 43, Message.getMessage(p.getUniqueId(), "actions_chat_color"));
			} else {
				Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 43, Message.getMessage(p.getUniqueId(), "permission"));
			}
		}

		if (TargetPlayer.hasPermission(p, "admingui.freeze.other")) {
			if (Settings.freeze.getOrDefault(target.getUniqueId(), false)) {
				Item.create(inv_actions, "ICE", 1, 45, Message.getMessage(p.getUniqueId(), "actions_freeze_disabled"));
			} else {
				Item.create(inv_actions, "ICE", 1, 45, Message.getMessage(p.getUniqueId(), "actions_freeze_enabled"));
			}
		} else {
			Item.create(inv_actions, "RED_STAINED_GLASS_PANE", 1, 45, Message.getMessage(p.getUniqueId(), "permission"));
		}


		return inv_actions;
	}

	public Inventory GUI_Kick(Player p, Player target) {

		String inventory_kick_name = Message.getMessage(p.getUniqueId(), "inventory_kick").replace("{player}", target.getName());
		Settings.target_player.put(p.getUniqueId(), target);

		Inventory inv_kick = Bukkit.createInventory(null, 36, inventory_kick_name);

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 36; i++) {
		// 	Item.create(inv_kick, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		for (Map.Entry<String, Object> kick_slot : AdminGuiIntegration.getInstance().getKick().getConfigurationSection("slots").getValues(false).entrySet()) {
			int i = Integer.parseInt(kick_slot.getKey());
			Item.create(inv_kick, AdminGuiIntegration.getInstance().getKick().getString("slots." + i + ".material"), 1, i, AdminGuiIntegration.getInstance().getKick().getString("slots." + i + ".name"));
		}

		if (AdminGuiIntegration.gui_type == 1) {
			if (kick_silence.getOrDefault(p.getUniqueId(), false)) {
				Item.after_createPlayerHead(inv_kick, Settings.skulls.get("Ground15"), 1, 28, Message.getMessage(p.getUniqueId(), "ban_silence_enabled"));
			} else {
				Item.after_createPlayerHead(inv_kick, Settings.skulls.get("EDDxample"), 1, 28, Message.getMessage(p.getUniqueId(), "ban_silence_disabled"));
			}

			Item.after_createPlayerHead(inv_kick, Settings.skulls.get("MHF_Redstone"), 1, 36, Message.getMessage(p.getUniqueId(), "kick_back"));
		} else {
			if (kick_silence.getOrDefault(p.getUniqueId(), false)) {
				Item.create(inv_kick, "LIME_TERRACOTTA", 1, 28, Message.getMessage(p.getUniqueId(), "ban_silence_enabled"));
			} else {
				Item.create(inv_kick, "RED_TERRACOTTA", 1, 28, Message.getMessage(p.getUniqueId(), "ban_silence_disabled"));
			}

			Item.create(inv_kick, "REDSTONE_BLOCK", 1, 36, Message.getMessage(p.getUniqueId(), "kick_back"));
		}

		return inv_kick;
	}

	public Inventory GUI_Ban(Player p, Player target) {

		String inventory_ban_name = Message.getMessage(p.getUniqueId(), "inventory_ban").replace("{player}", target.getName());
		Settings.target_player.put(p.getUniqueId(), target);

		Inventory inv_ban = Bukkit.createInventory(null, 36, inventory_ban_name);

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 36; i++) {
		// 	Item.create(inv_ban, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		if (ban_years.getOrDefault(p.getUniqueId(), 0) == 0) {
			Item.create(inv_ban, "RED_STAINED_GLASS_PANE", 1, 12, Message.getMessage(p.getUniqueId(), "ban_years"));
		} else {
			Item.create(inv_ban, "CLOCK", ban_years.getOrDefault(p.getUniqueId(), 0), 12, Message.getMessage(p.getUniqueId(), "ban_years"));
		}

		if (ban_months.getOrDefault(p.getUniqueId(), 0) == 0) {
			Item.create(inv_ban, "RED_STAINED_GLASS_PANE", 1, 13, Message.getMessage(p.getUniqueId(), "ban_months"));
		} else {
			Item.create(inv_ban, "CLOCK", ban_months.getOrDefault(p.getUniqueId(), 0), 13, Message.getMessage(p.getUniqueId(), "ban_months"));
		}

		if (ban_days.getOrDefault(p.getUniqueId(), 0) == 0) {
			Item.create(inv_ban, "RED_STAINED_GLASS_PANE", 1, 14, Message.getMessage(p.getUniqueId(), "ban_days"));
		} else {
			Item.create(inv_ban, "CLOCK", ban_days.getOrDefault(p.getUniqueId(), 0), 14, Message.getMessage(p.getUniqueId(), "ban_days"));
		}

		if (ban_hours.getOrDefault(p.getUniqueId(), 0) == 0) {
			Item.create(inv_ban, "RED_STAINED_GLASS_PANE", 1, 15, Message.getMessage(p.getUniqueId(), "ban_hours"));
		} else {
			Item.create(inv_ban, "CLOCK", ban_hours.getOrDefault(p.getUniqueId(), 0), 15, Message.getMessage(p.getUniqueId(), "ban_hours"));
		}

		if (ban_minutes.getOrDefault(p.getUniqueId(), 0) == 0) {
			Item.create(inv_ban, "RED_STAINED_GLASS_PANE", 1, 16, Message.getMessage(p.getUniqueId(), "ban_minutes"));
		} else {
			Item.create(inv_ban, "CLOCK", ban_minutes.getOrDefault(p.getUniqueId(), 0), 16, Message.getMessage(p.getUniqueId(), "ban_minutes"));
		}

		Item.create(inv_ban, "WHITE_TERRACOTTA", 1, 30, Message.getMessage(p.getUniqueId(), "ban_hacking"));
		Item.create(inv_ban, "ORANGE_TERRACOTTA", 1, 31, Message.getMessage(p.getUniqueId(), "ban_griefing"));
		Item.create(inv_ban, "MAGENTA_TERRACOTTA", 1, 32, Message.getMessage(p.getUniqueId(), "ban_spamming"));
		Item.create(inv_ban, "LIGHT_BLUE_TERRACOTTA", 1, 33, Message.getMessage(p.getUniqueId(), "ban_advertising"));
		Item.create(inv_ban, "YELLOW_TERRACOTTA", 1, 34, Message.getMessage(p.getUniqueId(), "ban_swearing"));

		if (AdminGuiIntegration.gui_type == 1) {
			if (ban_silence.getOrDefault(p.getUniqueId(), false)) {
				Item.after_createPlayerHead(inv_ban, Settings.skulls.get("Ground15"), 1, 28, Message.getMessage(p.getUniqueId(), "ban_silence_enabled"));
			} else {
				Item.after_createPlayerHead(inv_ban, Settings.skulls.get("EDDxample"), 1, 28, Message.getMessage(p.getUniqueId(), "ban_silence_disabled"));
			}
			Item.after_createPlayerHead(inv_ban, Settings.skulls.get("MHF_Redstone"), 1, 36, Message.getMessage(p.getUniqueId(), "ban_back"));
		} else {
			if (ban_silence.getOrDefault(p.getUniqueId(), false)) {
				Item.create(inv_ban, "LIME_TERRACOTTA", 1, 28, Message.getMessage(p.getUniqueId(), "ban_silence_enabled"));
			} else {
				Item.create(inv_ban, "RED_TERRACOTTA", 1, 28, Message.getMessage(p.getUniqueId(), "ban_silence_disabled"));
			}
			Item.create(inv_ban, "REDSTONE_BLOCK", 1, 36, Message.getMessage(p.getUniqueId(), "ban_back"));
		}

		return inv_ban;
	}

	public Inventory GUI_Warn(Player p, Player target) {

		String inventory_warn_name = Message.getMessage(p.getUniqueId(), "inventory_warn").replace("{player}", target.getName());
		Settings.target_player.put(p.getUniqueId(), target);

		Inventory inv_warn = Bukkit.createInventory(null, 36, inventory_warn_name);

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 36; i++) {
		// 	Item.create(inv_warn, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		// Remove duration selectors for warns; only show reasons and controls

		// Warn reason presets
		Item.create(inv_warn, "WHITE_TERRACOTTA", 1, 30, Message.getMessage(p.getUniqueId(), "warn_rule_violation"));
		Item.create(inv_warn, "ORANGE_TERRACOTTA", 1, 31, Message.getMessage(p.getUniqueId(), "warn_minor_offense"));
		Item.create(inv_warn, "MAGENTA_TERRACOTTA", 1, 32, Message.getMessage(p.getUniqueId(), "warn_chat_spam"));
		Item.create(inv_warn, "LIGHT_BLUE_TERRACOTTA", 1, 33, Message.getMessage(p.getUniqueId(), "warn_inappropriate_language"));
		Item.create(inv_warn, "YELLOW_TERRACOTTA", 1, 34, Message.getMessage(p.getUniqueId(), "warn_general"));

		if (AdminGuiIntegration.gui_type == 1) {
			if (warn_silence.getOrDefault(p.getUniqueId(), false)) {
				Item.after_createPlayerHead(inv_warn, Settings.skulls.get("Ground15"), 1, 19, Message.getMessage(p.getUniqueId(), "warn_silence_enabled"));
			} else {
				Item.after_createPlayerHead(inv_warn, Settings.skulls.get("EDDxample"), 1, 19, Message.getMessage(p.getUniqueId(), "warn_silence_disabled"));
			}
			Item.after_createPlayerHead(inv_warn, Settings.skulls.get("MHF_Redstone"), 1, 36, Message.getMessage(p.getUniqueId(), "warn_back"));
		} else {
			if (warn_silence.getOrDefault(p.getUniqueId(), false)) {
				Item.create(inv_warn, "LIME_TERRACOTTA", 1, 19, Message.getMessage(p.getUniqueId(), "warn_silence_enabled"));
			} else {
				Item.create(inv_warn, "RED_TERRACOTTA", 1, 19, Message.getMessage(p.getUniqueId(), "warn_silence_disabled"));
			}
			Item.create(inv_warn, "REDSTONE_BLOCK", 1, 36, Message.getMessage(p.getUniqueId(), "warn_back"));
		}

		return inv_warn;
	}

	public Inventory GUI_Mute(Player p, Player target) {

		String inventory_mute_name = Message.getMessage(p.getUniqueId(), "inventory_mute").replace("{player}", target.getName());
		Settings.target_player.put(p.getUniqueId(), target);

		Inventory inv_mute = Bukkit.createInventory(null, 36, inventory_mute_name);

		// Background tiles removed for cleaner look
		// for (int i = 1; i < 36; i++) {
		// 	Item.create(inv_mute, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		// Full duration selectors (mirror ban screen exactly)
		if (mute_years.getOrDefault(p.getUniqueId(), 0) == 0) {
			Item.create(inv_mute, "RED_STAINED_GLASS_PANE", 1, 12, Message.getMessage(p.getUniqueId(), "mute_years"));
		} else {
			Item.create(inv_mute, "CLOCK", mute_years.getOrDefault(p.getUniqueId(), 0), 12, Message.getMessage(p.getUniqueId(), "mute_years"));
		}

		if (mute_months.getOrDefault(p.getUniqueId(), 0) == 0) {
			Item.create(inv_mute, "RED_STAINED_GLASS_PANE", 1, 13, Message.getMessage(p.getUniqueId(), "mute_months"));
		} else {
			Item.create(inv_mute, "CLOCK", mute_months.getOrDefault(p.getUniqueId(), 0), 13, Message.getMessage(p.getUniqueId(), "mute_months"));
		}

		if (mute_days.getOrDefault(p.getUniqueId(), 0) == 0) {
			Item.create(inv_mute, "RED_STAINED_GLASS_PANE", 1, 14, Message.getMessage(p.getUniqueId(), "mute_days"));
		} else {
			Item.create(inv_mute, "CLOCK", mute_days.getOrDefault(p.getUniqueId(), 0), 14, Message.getMessage(p.getUniqueId(), "mute_days"));
		}

		if (mute_hours.getOrDefault(p.getUniqueId(), 0) == 0) {
			Item.create(inv_mute, "RED_STAINED_GLASS_PANE", 1, 15, Message.getMessage(p.getUniqueId(), "mute_hours"));
		} else {
			Item.create(inv_mute, "CLOCK", mute_hours.getOrDefault(p.getUniqueId(), 0), 15, Message.getMessage(p.getUniqueId(), "mute_hours"));
		}

		if (mute_minutes.getOrDefault(p.getUniqueId(), 0) == 0) {
			Item.create(inv_mute, "RED_STAINED_GLASS_PANE", 1, 16, Message.getMessage(p.getUniqueId(), "mute_minutes"));
		} else {
			Item.create(inv_mute, "CLOCK", mute_minutes.getOrDefault(p.getUniqueId(), 0), 16, Message.getMessage(p.getUniqueId(), "mute_minutes"));
		}

		// Mute reason presets
		Item.create(inv_mute, "WHITE_TERRACOTTA", 1, 30, Message.getMessage(p.getUniqueId(), "mute_chat_abuse"));
		Item.create(inv_mute, "ORANGE_TERRACOTTA", 1, 31, Message.getMessage(p.getUniqueId(), "mute_spamming"));
		Item.create(inv_mute, "MAGENTA_TERRACOTTA", 1, 32, Message.getMessage(p.getUniqueId(), "mute_inappropriate_language"));
		Item.create(inv_mute, "LIGHT_BLUE_TERRACOTTA", 1, 33, Message.getMessage(p.getUniqueId(), "mute_harassment"));
		Item.create(inv_mute, "YELLOW_TERRACOTTA", 1, 34, Message.getMessage(p.getUniqueId(), "mute_advertising"));

		if (AdminGuiIntegration.gui_type == 1) {
			if (mute_silence.getOrDefault(p.getUniqueId(), false)) {
				Item.after_createPlayerHead(inv_mute, Settings.skulls.get("Ground15"), 1, 19, Message.getMessage(p.getUniqueId(), "mute_silence_enabled"));
			} else {
				Item.after_createPlayerHead(inv_mute, Settings.skulls.get("EDDxample"), 1, 19, Message.getMessage(p.getUniqueId(), "mute_silence_disabled"));
			}
			Item.after_createPlayerHead(inv_mute, Settings.skulls.get("MHF_Redstone"), 1, 36, Message.getMessage(p.getUniqueId(), "mute_back"));
		} else {
			if (mute_silence.getOrDefault(p.getUniqueId(), false)) {
				Item.create(inv_mute, "LIME_TERRACOTTA", 1, 19, Message.getMessage(p.getUniqueId(), "mute_silence_enabled"));
			} else {
				Item.create(inv_mute, "RED_TERRACOTTA", 1, 19, Message.getMessage(p.getUniqueId(), "mute_silence_disabled"));
			}
			Item.create(inv_mute, "REDSTONE_BLOCK", 1, 36, Message.getMessage(p.getUniqueId(), "mute_back"));
		}

		return inv_mute;
	}

	public Inventory GUI_potions(Player p, Player target) {

		String inventory_potions_name = Message.getMessage(p.getUniqueId(), "inventory_potions").replace("{player}", target.getName());
		Settings.target_player.put(p.getUniqueId(), target);

		Inventory inv_potions = Bukkit.createInventory(null, 54, inventory_potions_name); // Changed to double chest size

		// Background tiles removed for cleaner look
		// for (int i = 1; i <= 54; i++) {
		// 	Item.create(inv_potions, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		// Place potions (now has room for up to 45 potions in rows 1-5)
		if (Bukkit.getVersion().contains("1.12") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.9")) {
			for (Version_12 potion : Version_12.values()) {
				Item.create(inv_potions, "POTION", 1, potion.ordinal() + 1, Message.getMessage(p.getUniqueId(), potion.name()));
			}
		} else if (Bukkit.getVersion().contains("1.8")) {
			for (Version_8 potion : Version_8.values()) {
				Item.create(inv_potions, "POTION", 1, potion.ordinal() + 1, Message.getMessage(p.getUniqueId(), potion.name()));
			}
		} else {
			// 1.14+ (includes all new potions)
			for (Version_14 potion : Version_14.values()) {
				Item.create(inv_potions, "POTION", 1, potion.ordinal() + 1, Message.getMessage(p.getUniqueId(), potion.name()));
			}
		}

		// Control buttons (bottom row, slots 46-54)
		Item.create(inv_potions, "CLOCK", duration.getOrDefault(p.getUniqueId(), 1), 49, Message.getMessage(p.getUniqueId(), "potions_time"));
		Item.create(inv_potions, "RED_STAINED_GLASS_PANE", 1, 50, Message.getMessage(p.getUniqueId(), "potions_remove_all"));
		Item.create(inv_potions, "BEACON", level.getOrDefault(p.getUniqueId(), 1), 51, Message.getMessage(p.getUniqueId(), "potions_level"));

		// Back button (bottom right)
		if (AdminGuiIntegration.gui_type == 1) {
			Item.after_createPlayerHead(inv_potions, Settings.skulls.get("MHF_Redstone"), 1, 54, Message.getMessage(p.getUniqueId(), "potions_back"));
		} else {
			Item.create(inv_potions, "REDSTONE_BLOCK", 1, 54, Message.getMessage(p.getUniqueId(), "potions_back"));
		}

		return inv_potions;
	}

	public Inventory GUI_Spawner(Player p, Player target) {

		String inventory_spawner_name = Message.getMessage(p.getUniqueId(), "inventory_spawner").replace("{player}", target.getName());
		Inventory inv_spawner = Bukkit.createInventory(null, 54, inventory_spawner_name);

		Settings.target_player.put(p.getUniqueId(), target);

		if (Bukkit.getVersion().contains("1.14")) {
			for (Material_Version_14 material : Material_Version_14.values()) {
				Item.create(inv_spawner, material.name(), 1, material.ordinal() + 1, Message.getMessage(p.getUniqueId(), Message_Version_14.values()[material.ordinal()].name()));
			}
		} else if (Bukkit.getVersion().contains("1.13")) {
			for (Material_Version_13 material : Material_Version_13.values()) {
				Item.create(inv_spawner, material.name(), 1, material.ordinal() + 1, Message.getMessage(p.getUniqueId(), Message_Version_13.values()[material.ordinal()].name()));
			}
		} else if (Bukkit.getVersion().contains("1.12")) {
			for (Material_Version_12 material : Material_Version_12.values()) {
				Item.create(inv_spawner, material.name(), 1, material.ordinal() + 1, Message.getMessage(p.getUniqueId(), Message_Version_12.values()[material.ordinal()].name()));
			}
		} else if (Bukkit.getVersion().contains("1.11")) {
			for (Material_Version_11 material : Material_Version_11.values()) {
				Item.create(inv_spawner, material.name(), 1, material.ordinal() + 1, Message.getMessage(p.getUniqueId(), Message_Version_11.values()[material.ordinal()].name()));
			}
		} else if (Bukkit.getVersion().contains("1.10")) {
			for (Material_Version_10 material : Material_Version_10.values()) {
				Item.create(inv_spawner, material.name(), 1, material.ordinal() + 1, Message.getMessage(p.getUniqueId(), Message_Version_10.values()[material.ordinal()].name()));
			}
		} else if (Bukkit.getVersion().contains("1.9")) {
			for (Material_Version_9 material : Material_Version_9.values()) {
				Item.create(inv_spawner, material.name(), 1, material.ordinal() + 1, Message.getMessage(p.getUniqueId(), Message_Version_9.values()[material.ordinal()].name()));
			}
		} else if (Bukkit.getVersion().contains("1.8")) {
			for (Material_Version_8 material : Material_Version_8.values()) {
				Item.create(inv_spawner, material.name(), 1, material.ordinal() + 1, Message.getMessage(p.getUniqueId(), Message_Version_8.values()[material.ordinal()].name()));
			}
		} else {
			for (Material_Version_15 material : Material_Version_15.values()) {
				Item.create(inv_spawner, material.name(), 1, material.ordinal() + 1, Message.getMessage(p.getUniqueId(), Message_Version_15.values()[material.ordinal()].name()));
			}
		}

		if (AdminGuiIntegration.gui_type == 1) {
			Item.after_createPlayerHead(inv_spawner, Settings.skulls.get("MHF_Redstone"), 1, 54, Message.getMessage(p.getUniqueId(), "spawner_back"));
		} else {
			Item.create(inv_spawner, "REDSTONE_BLOCK", 1, 54, Message.getMessage(p.getUniqueId(), "spawner_back"));
		}

		return inv_spawner;
	}

	public Inventory GUI_Money(Player p, Player target) {

		String inventory_money_name = Message.getMessage(p.getUniqueId(), "inventory_money").replace("{player}", target.getName());
		Settings.target_player.put(p.getUniqueId(), target);

		Inventory inv_money = Bukkit.createInventory(null, 27, inventory_money_name);

		if (target.isOnline()) {

			// Background tiles removed for cleaner look
			// for (int i = 1; i < 27; i++) {
			// 	Item.create(inv_money, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
			// }

			Item.create(inv_money, "PAPER", 1, 12, Message.getMessage(p.getUniqueId(), "money_give"));
			Item.create(inv_money, "BOOK", 1, 14, Message.getMessage(p.getUniqueId(), "money_set"));
			Item.create(inv_money, "PAPER", 1, 16, Message.getMessage(p.getUniqueId(), "money_take"));

		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}

		if (AdminGuiIntegration.gui_type == 1) {
			Item.after_createPlayerHead(inv_money, Settings.skulls.get("MHF_Redstone"), 1, 27, Message.getMessage(p.getUniqueId(), "money_back"));
		} else {
			Item.create(inv_money, "REDSTONE_BLOCK", 1, 27, Message.getMessage(p.getUniqueId(), "money_back"));
		}

		return inv_money;
	}

	public Inventory GUI_Money_Amount(Player p, Player target, int option) {

		String inventory_money_amount_name;

		switch (option) {
			case 1:
				inventory_money_amount_name = Message.getMessage(p.getUniqueId(), "inventory_money_give").replace("{player}", target.getName());
				break;
			case 3:
				inventory_money_amount_name = Message.getMessage(p.getUniqueId(), "inventory_money_take").replace("{player}", target.getName());
				break;
			default:
				inventory_money_amount_name = Message.getMessage(p.getUniqueId(), "inventory_money_set").replace("{player}", target.getName());
		}
		Settings.target_player.put(p.getUniqueId(), target);

		Inventory inv_money_amount = Bukkit.createInventory(null, 36, inventory_money_amount_name);

		if (target.isOnline()) {

			// Background tiles removed for cleaner look
			// for (int i = 1; i < 36; i++) {
			// 	Item.create(inv_money_amount, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
			// }

			for (int i = 1; i <= 10; i++) {
				Item.create(inv_money_amount, "PAPER", 1, i, "&a&l" + AdminGuiIntegration.getEconomy().format(i * 100));
			}

			for (int i = 11, j = 1; i < 20; i++, j++) {
				Item.create(inv_money_amount, "PAPER", 1, i, "&a&l" + AdminGuiIntegration.getEconomy().format(j * 1500));
			}

			for (int i = 20, j = 1; i <= 25; i++, j++) {
				Item.create(inv_money_amount, "PAPER", 1, i, "&a&l" + AdminGuiIntegration.getEconomy().format(j * 15000));
			}

			for (int i = 26, j = 1; i < 36; i++, j++) {
				Item.create(inv_money_amount, "PAPER", 1, i, "&a&l" + AdminGuiIntegration.getEconomy().format(j * 100000));
			}

		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}

		if (AdminGuiIntegration.gui_type == 1) {
			Item.after_createPlayerHead(inv_money_amount, Settings.skulls.get("MHF_Redstone"), 1, 36, Message.getMessage(p.getUniqueId(), "money_back"));
		} else {
			Item.create(inv_money_amount, "REDSTONE_BLOCK", 1, 36, Message.getMessage(p.getUniqueId(), "money_back"));
		}

		return inv_money_amount;
	}

	public Inventory GUI_Inventory(Player p, Player target) {

		String inventory_inventory_name = Message.getMessage(p.getUniqueId(), "inventory_inventory").replace("{player}", target.getName());
		Settings.target_player.put(p.getUniqueId(), target);

		Inventory inv_inventory = Bukkit.createInventory(null, 54, inventory_inventory_name);

		if (target.isOnline()) {

			ItemStack[] items = target.getInventory().getContents();
			ItemStack[] armor = target.getInventory().getArmorContents();

			for (int i = 0; i < items.length; i++) {
				if (items[i] != null) {
					inv_inventory.setItem(i, items[i]);
				} else {
					inv_inventory.setItem(i, null);
				}
			}

			for (int i = 0, j = 36; i < armor.length; i++, j++) {
				if (armor[i] != null) {
					inv_inventory.setItem(j, armor[i]);
				} else {
					inv_inventory.setItem(j, null);
				}
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}

		// Background tiles removed for cleaner look
		// for (int i = 42; i < 54; i++) {
		// 	Item.create(inv_inventory, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		Item.create(inv_inventory, "GREEN_TERRACOTTA", 1, 46, Message.getMessage(p.getUniqueId(), "inventory_refresh"));

		if (TargetPlayer.hasPermission(p, "admingui.inventory.edit")) {
			Item.create(inv_inventory, "BLUE_TERRACOTTA", 1, 50, Message.getMessage(p.getUniqueId(), "inventory_clear"));
		}else{
			Item.create(inv_inventory, "RED_STAINED_GLASS_PANE", 1, 50, Message.getMessage(p.getUniqueId(), "permission"));
		}

		if (AdminGuiIntegration.gui_type == 1) {
			Item.after_createPlayerHead(inv_inventory, Settings.skulls.get("MHF_Redstone"), 1, 54, Message.getMessage(p.getUniqueId(), "inventory_back"));
		} else {
			Item.create(inv_inventory, "REDSTONE_BLOCK", 1, 54, Message.getMessage(p.getUniqueId(), "inventory_back"));
		}

		return inv_inventory;
	}

	public Inventory GUI_Ender_Chest(Player p, Player target) {

		String inventory_ender_chest_name = Message.getMessage(p.getUniqueId(), "inventory_ender_chest").replace("{player}", target.getName());
		Settings.target_player.put(p.getUniqueId(), target);

		Inventory inv_ender_chest = Bukkit.createInventory(null, 36, inventory_ender_chest_name);

		if (target.isOnline()) {

			ItemStack[] items = target.getEnderChest().getContents();

			for (int i = 0; i < items.length; i++) {
				if (items[i] != null) {
					inv_ender_chest.setItem(i, items[i]);
				} else {
					inv_ender_chest.setItem(i, null);
				}
			}

		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}

		// Background tiles removed for cleaner look
		// for (int i = 28; i < 36; i++) {
		// 	Item.create(inv_ender_chest, gui_color.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("gui_default_color", "LIGHT_BLUE_STAINED_GLASS_PANE")), 1, i, " ");
		// }

		Item.create(inv_ender_chest, "GREEN_TERRACOTTA", 1, 28, Message.getMessage(p.getUniqueId(), "inventory_refresh"));

		if (TargetPlayer.hasPermission(p, "admingui.enderchest.edit")) {
			Item.create(inv_ender_chest, "BLUE_TERRACOTTA", 1, 32, Message.getMessage(p.getUniqueId(), "inventory_clear"));
		}else{
			Item.create(inv_ender_chest, "RED_STAINED_GLASS_PANE", 1, 32, Message.getMessage(p.getUniqueId(), "permission"));
		}

		if (AdminGuiIntegration.gui_type == 1) {
			Item.after_createPlayerHead(inv_ender_chest, Settings.skulls.get("MHF_Redstone"), 1, 36, Message.getMessage(p.getUniqueId(), "inventory_back"));
		} else {
			Item.create(inv_ender_chest, "REDSTONE_BLOCK", 1, 36, Message.getMessage(p.getUniqueId(), "inventory_back"));
		}

		return inv_ender_chest;
	}

	public void clicked_main(Player p, int slot, ItemStack clicked, Inventory inv, boolean isLeftClick) {

		if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "main_quit"))) {
			p.closeInventory();
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "main_player").replace("{player}", p.getName()))) {
			p.openInventory(GUI_Player(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "main_world"))) {
			p.openInventory(GUI_World(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "main_players"))) {
			//TODO: Bungee
			if (AdminGuiIntegration.getInstance().getConf().getBoolean("bungeecord_enabled", false)) {
				Channel.send(p.getName(), "send", "online_players");
			}
			// Ensure we open the 27-slot Player Settings flow unless explicitly set by the sword button
			Settings.player_selector_mode.remove(p.getUniqueId());
			p.openInventory(GUI_Players(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.chat("&6&lReports"))) {
			// Open Aevorin Reports GUI
			p.closeInventory();
			if (TargetPlayer.hasPermission(p, "admingui.reports")) {
				AdminGuiIntegration.getInstance().getReportsGUI().openReportsGUI(p, 1);
			} else {
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "permission"));
			}
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "main_maintenance_mode"))) {
			if (TargetPlayer.hasPermission(p, "admingui.maintenance.manage")) {
				if (Settings.maintenance_mode) {
					Settings.maintenance_mode = false;
					AdminGuiIntegration.getInstance().getSett().set("maintenance", false);
					AdminGuiIntegration.getInstance().saveSettings();
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_maintenance_disabled"));
					p.closeInventory();
				} else {
					Settings.maintenance_mode = true;
					AdminGuiIntegration.getInstance().getSett().set("maintenance", true);
					AdminGuiIntegration.getInstance().saveSettings();
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_maintenance_enabled"));
					p.closeInventory();
					for (Player pl : getServer().getOnlinePlayers()) {
						if (!pl.isOp() && !TargetPlayer.hasPermission(pl, "admingui.maintenance")) {
							pl.kickPlayer(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_maintenance"));
						}
					}
				}
			} else {
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "permission"));
				p.closeInventory();
			}
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "main_unban_players"))) {
			if (isLeftClick) {
				p.openInventory(GUI_Unban_Players(p));
			} else {
				p.openInventory(GUI_Unmute_Players(p));
			}
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "main_language") + (Settings.language.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("default_language", "English")) != null ? Settings.language.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("default_language", "English")) : "English"))) {
			String lang = Settings.language.getOrDefault(p.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("default_language", "English"));
			if (lang == null) lang = "English";
			if (Language.enabled_languages.size() > 1) {
				for (int i = 0; i < Language.enabled_languages.size(); i++) {
					if (Language.enabled_languages.get(i).equals(lang)) {
						if (Language.enabled_languages.size() - 1 == i) {
							Settings.language.put(p.getUniqueId(), Language.enabled_languages.get(0));
						} else {
							Settings.language.put(p.getUniqueId(), Language.enabled_languages.get(i + 1));
						}
						break;
					}
				}
			}
            p.openInventory(GUI_Main(p));
        } else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_settings_actions"))) {
            // New Actions button from main menu opens player selector; on select, go to Actions
            if (AdminGuiIntegration.getInstance().getConf().getBoolean("bungeecord_enabled", false)) {
                Channel.send(p.getName(), "send", "online_players");
            }
            Settings.player_selector_mode.put(p.getUniqueId(), "actions");
            p.openInventory(GUI_Players(p));
		}
	}

	public void clicked_player(Player p, int slot, ItemStack clicked, Inventory inv) {

		if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_back"))) {
			p.openInventory(GUI_Main(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_info").replace("{player}", p.getName()))) {
			p.openInventory(GUI_Player(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_heal"))) {
			p.setHealth(20);
			p.setFireTicks(0);
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_heal"));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_feed"))) {
			p.setFoodLevel(20);
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_feed"));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_survival"))) {
			if (TargetPlayer.hasPermission(p, "admingui.gamemode.adventure")) {
				p.setGameMode(GameMode.ADVENTURE);
			} else if (TargetPlayer.hasPermission(p, "admingui.gamemode.creative")) {
				p.setGameMode(GameMode.CREATIVE);
			} else if (TargetPlayer.hasPermission(p, "admingui.gamemode.spectator")) {
				p.setGameMode(GameMode.SPECTATOR);
			} else {
				p.setGameMode(GameMode.SURVIVAL);
			}
			p.openInventory(GUI_Player(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_adventure"))) {
			if (TargetPlayer.hasPermission(p, "admingui.gamemode.creative")) {
				p.setGameMode(GameMode.CREATIVE);
			} else if (TargetPlayer.hasPermission(p, "admingui.gamemode.spectator")) {
				p.setGameMode(GameMode.SPECTATOR);
			} else if (TargetPlayer.hasPermission(p, "admingui.gamemode.survival")) {
				p.setGameMode(GameMode.SURVIVAL);
			} else {
				p.setGameMode(GameMode.ADVENTURE);
			}
			p.openInventory(GUI_Player(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_creative"))) {
			if (TargetPlayer.hasPermission(p, "admingui.gamemode.spectator")) {
				p.setGameMode(GameMode.SPECTATOR);
			} else if (TargetPlayer.hasPermission(p, "admingui.gamemode.survival")) {
				p.setGameMode(GameMode.SURVIVAL);
			} else if (TargetPlayer.hasPermission(p, "admingui.gamemode.adventure")) {
				p.setGameMode(GameMode.ADVENTURE);
			} else {
				p.setGameMode(GameMode.CREATIVE);
			}
			p.openInventory(GUI_Player(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_spectator"))) {
			if (TargetPlayer.hasPermission(p, "admingui.gamemode.survival")) {
				p.setGameMode(GameMode.SURVIVAL);
			} else if (TargetPlayer.hasPermission(p, "admingui.gamemode.adventure")) {
				p.setGameMode(GameMode.ADVENTURE);
			} else if (TargetPlayer.hasPermission(p, "admingui.gamemode.creative")) {
				p.setGameMode(GameMode.CREATIVE);
			} else {
				p.setGameMode(GameMode.SPECTATOR);
			}
			p.openInventory(GUI_Player(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_god_enabled"))) {
			if (Bukkit.getVersion().contains("1.8")) {
				Settings.god.put(p.getUniqueId(), true);
			} else {
				p.setInvulnerable(true);
			}
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_god_enabled"));
			p.openInventory(GUI_Player(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_god_disabled"))) {
			if (Bukkit.getVersion().contains("1.8")) {
				Settings.god.put(p.getUniqueId(), false);
			} else {
				p.setInvulnerable(false);
			}
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_god_disabled"));
			p.openInventory(GUI_Player(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_potions"))) {
			p.openInventory(GUI_potions(p, p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_spawner"))) {
			p.openInventory(GUI_Spawner(p, p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_kill"))) {
			p.setHealth(0);
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_kill"));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_burn"))) {
			p.setFireTicks(500);
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_burn"));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_lightning"))) {
			p.closeInventory();
			final Player selfRef = p;
			Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				if (selfRef.isOnline()) {
					selfRef.getWorld().strikeLightning(selfRef.getLocation());
					selfRef.setHealth(0); // Instant kill
					selfRef.sendMessage(Message.getMessage(selfRef.getUniqueId(), "prefix") + Message.chat("&c⚡ You were struck by lightning!"));
				}
			}, 2L);
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_firework"))) {
			Fireworks.createRandom(p);
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_money"))) {
			p.openInventory(GUI_Money(p, p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_vanish_enabled"))) {
			// Toggle vanish ON for self using built-in Bukkit API
			Settings.vanish.put(p.getUniqueId(), true);
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (!online.equals(p)) {
					online.hidePlayer(AdminGuiIntegration.getInstance().getBanManagerPlugin(), p);
				}
			}
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_hide"));
			final Player selfRef1 = p;
			p.closeInventory();
			Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				if (selfRef1.isOnline()) {
					selfRef1.openInventory(GUI_Player(selfRef1));
				}
			}, 2L);
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_vanish_disabled"))) {
			// Toggle vanish OFF for self using built-in Bukkit API
			Settings.vanish.put(p.getUniqueId(), false);
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (!online.equals(p)) {
					online.showPlayer(AdminGuiIntegration.getInstance().getBanManagerPlugin(), p);
				}
			}
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_visible"));
			final Player selfRef2 = p;
			p.closeInventory();
			Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				if (selfRef2.isOnline()) {
					selfRef2.openInventory(GUI_Player(selfRef2));
				}
			}, 2L);
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_custom"))) {
			Settings.custom_method.put(p.getUniqueId(), 1);
			p.openInventory(GUI_Plugins(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "player_chat_color"))) {
			switch (Settings.chat_color.getOrDefault(p.getUniqueId(), "LIGHT_GRAY_WOOL")) {
				case "WHITE_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "ORANGE_WOOL");
					break;
				case "ORANGE_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "MAGENTA_WOOL");
					break;
				case "MAGENTA_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "LIGHT_BLUE_WOOL");
					break;
				case "LIGHT_BLUE_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "YELLOW_WOOL");
					break;
				case "YELLOW_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "LIME_WOOL");
					break;
				case "LIME_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "GRAY_WOOL");
					break;
				case "GRAY_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "CLOCK");
					break;
				case "CLOCK":
					if (getVersion().contains("1.8") || getVersion().contains("1.9") || getVersion().contains("1.10") || getVersion().contains("1.11") || getVersion().contains("1.12") || getVersion().contains("1.13") || getVersion().contains("1.14") || getVersion().contains("1.15")) {
						Settings.chat_color.put(p.getUniqueId(), "LIGHT_GRAY_WOOL");
					} else {
						Settings.chat_color.put(p.getUniqueId(), "EXPERIENCE_BOTTLE");
					}
					break;
				case "EXPERIENCE_BOTTLE":
					Settings.chat_color.put(p.getUniqueId(), "LIGHT_GRAY_WOOL");
					break;
				case "LIGHT_GRAY_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "CYAN_WOOL");
					break;
				case "CYAN_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "PURPLE_WOOL");
					break;
				case "PURPLE_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "BLUE_WOOL");
					break;
				case "BLUE_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "GREEN_WOOL");
					break;
				case "GREEN_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "PINK_WOOL");
					break;
				case "PINK_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "RED_WOOL");
					break;
				case "RED_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "BLACK_WOOL");
					break;
				case "BLACK_WOOL":
					Settings.chat_color.put(p.getUniqueId(), "WHITE_WOOL");
					break;
			}
			p.openInventory(GUI_Player(p));
		}
	}

	public void clicked_world(Player p, int slot, ItemStack clicked, Inventory inv) {

		if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "world_back"))) {
			p.openInventory(GUI_Main(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "world_day"))) {
			p.getPlayer().getWorld().setTime(13000);
			p.openInventory(GUI_World(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "world_night"))) {
			p.getPlayer().getWorld().setTime(0);
			p.openInventory(GUI_World(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "world_clear"))) {
			World world = p.getWorld();
			world.setThundering(false);
			world.setStorm(true);
			p.openInventory(GUI_World(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "world_rain"))) {
			World world = p.getWorld();
			world.setStorm(true);
			world.setThundering(true);
			p.openInventory(GUI_World(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "world_thunder"))) {
			World world = p.getWorld();
			world.setStorm(false);
			world.setThundering(false);
			p.openInventory(GUI_World(p));
		}

	}

	public void clicked_players(Player p, int slot, ItemStack clicked, Inventory inv) {

        if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_back"))) {
            p.openInventory(GUI_Main(p));
            Settings.player_selector_mode.remove(p.getUniqueId());
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_previous"))) {
			page.put(p.getUniqueId(), page.getOrDefault(p.getUniqueId(), 1) - 1);
			p.openInventory(GUI_Players(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_next"))) {
			page.put(p.getUniqueId(), page.getOrDefault(p.getUniqueId(), 1) + 1);
			p.openInventory(GUI_Players(p));
		} else if (clicked.getItemMeta() != null) {
			ItemMeta meta = clicked.getItemMeta();
			String displayNameStripped = ChatColor.stripColor(meta.getDisplayName());
			
			// Multiple methods to detect player head (for compatibility)
			Material clickedType = clicked.getType();
			boolean isPlayerHead = clickedType == XMaterial.PLAYER_HEAD.parseMaterial() 
				|| clickedType == Material.PLAYER_HEAD 
				|| clickedType.name().contains("SKULL") 
				|| clickedType.name().contains("HEAD");
			
			// FIX: Use Message.chat() to convert & codes to § codes for proper comparison
			String expectedLore = Message.chat(Message.getMessage(p.getUniqueId(), "players_more"));
			boolean loreMatches = meta.getLore() != null && meta.getLore().contains(expectedLore);
			
			// Also try to get player name from skull owner if it's a skull
			String targetName = displayNameStripped;
			if (meta instanceof SkullMeta) {
				SkullMeta skullMeta = (SkullMeta) meta;
				if (skullMeta.getOwningPlayer() != null) {
					targetName = skullMeta.getOwningPlayer().getName();
				} else if (skullMeta.getOwner() != null && !skullMeta.getOwner().isEmpty()) {
					targetName = skullMeta.getOwner();
				}
			}
			
			if (isPlayerHead || loreMatches) {
				// Try multiple ways to find the target player
				Player target_p = getServer().getPlayer(targetName);
				if (target_p == null && !targetName.equals(displayNameStripped)) {
					target_p = getServer().getPlayer(displayNameStripped);
				}
				
				if (target_p != null) {
					// Use .equals() for proper UUID comparison, not != which compares references
					if (!p.getUniqueId().equals(target_p.getUniqueId())) {
						Settings.target_player.put(p.getUniqueId(), target_p);
						if ("actions".equals(Settings.player_selector_mode.getOrDefault(p.getUniqueId(), ""))) {
							p.openInventory(GUI_Actions(p, target_p));
							Settings.player_selector_mode.remove(p.getUniqueId());
						} else {
							p.openInventory(GUI_Players_Settings(p, target_p, target_p.getName()));
						}
					} else {
						// Clicking on self - open Players_Settings (punishment menu) for consistency
						Settings.target_player.put(p.getUniqueId(), p);
						if ("actions".equals(Settings.player_selector_mode.getOrDefault(p.getUniqueId(), ""))) {
							p.openInventory(GUI_Actions(p, p));
							Settings.player_selector_mode.remove(p.getUniqueId());
						} else {
							p.openInventory(GUI_Players_Settings(p, p, p.getName()));
						}
					}
				} else if (AdminGuiIntegration.getInstance().getConf().getBoolean("bungeecord_enabled", false) && Settings.online_players.contains(targetName)) {
					//TODO: Bungee
					switch (AdminGuiIntegration.getInstance().getConf().getInt("control_type", 0)) {
						case 0:
							Channel.send(p.getName(), "connect", targetName);
							break;
						case 1:
							Settings.target_player.put(p.getUniqueId(), null);
							// Can't open Actions for remote player without a live Player object; fall back
							p.openInventory(GUI_Players_Settings(p, null, targetName));
							break;
						default:
							p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.chat("&cPlayer " + targetName + " is not located in the same server as you."));
							break;
					}
				} else {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
					p.closeInventory();
					Settings.player_selector_mode.remove(p.getUniqueId());
				}
			}
		}

	}

	public void clicked_plugins(Player p, int slot, ItemStack clicked, Inventory inv) {
		if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "plugins_back"))) {
			p.openInventory(GUI_Main(p));
		} else if (clicked.getType() != Material.AIR && clicked.getType() != XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE.parseMaterial()) {
			Settings.plugin_slot.put(p.getUniqueId(), slot + 1);
			p.openInventory(GUI_Commands(p, slot + 1));
		}
	}

	public void clicked_commands(Player p, int slot, ItemStack clicked, Inventory inv) {
		slot++;
		if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "commands_back"))) {
			p.openInventory(GUI_Plugins(p));
		} else if (clicked.getType() != Material.AIR && clicked.getType() != XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE.parseMaterial() && clicked.getType() != XMaterial.RED_STAINED_GLASS_PANE.parseMaterial()) {
			YamlConfiguration yamlConfiguration;

			switch (Settings.custom_method.getOrDefault(p.getUniqueId(), 0)) {
				case 1:
					yamlConfiguration = AdminGuiIntegration.getInstance().getComm();
					break;
				case 2:
					yamlConfiguration = AdminGuiIntegration.getInstance().getComo();
					break;
				default:
					yamlConfiguration = AdminGuiIntegration.getInstance().getPlug();
					break;
			}

			if (yamlConfiguration.getBoolean("plugins." + Settings.plugin_slot.getOrDefault(p.getUniqueId(), 1) + ".commands." + slot + ".console_sender")) {
				if (yamlConfiguration.isList("plugins." + Settings.plugin_slot.getOrDefault(p.getUniqueId(), 1) + ".commands." + slot + ".command")) {
					List<String> comm_list = yamlConfiguration.getStringList("plugins." + Settings.plugin_slot.getOrDefault(p.getUniqueId(), 1) + ".commands." + slot + ".command");
					for (String command : comm_list) {
						getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("/", "").replace("{player}", p.getName()).replace("{target_player}", Settings.target_player.getOrDefault(p.getUniqueId(), p).getName()));
					}
				} else {
					getServer().dispatchCommand(Bukkit.getConsoleSender(), yamlConfiguration.getString("plugins." + Settings.plugin_slot.getOrDefault(p.getUniqueId(), 1) + ".commands." + slot + ".command").replace("/", "").replace("{player}", p.getName()).replace("{target_player}", Settings.target_player.getOrDefault(p.getUniqueId(), p).getName()));
				}
			} else {
				if (yamlConfiguration.isList("plugins." + Settings.plugin_slot.getOrDefault(p.getUniqueId(), 1) + ".commands." + slot + ".command")) {
					List<String> comm_list = yamlConfiguration.getStringList("plugins." + Settings.plugin_slot.getOrDefault(p.getUniqueId(), 1) + ".commands." + slot + ".command");
					for (String command : comm_list) {
						getServer().dispatchCommand(p, command.replace("/", "").replace("{player}", p.getName()).replace("{target_player}", Settings.target_player.getOrDefault(p.getUniqueId(), p).getName()));
					}
				} else {
					getServer().dispatchCommand(p, yamlConfiguration.getString("plugins." + Settings.plugin_slot.getOrDefault(p.getUniqueId(), 1) + ".commands." + slot + ".command").replace("/", "").replace("{player}", p.getName()).replace("{target_player}", Settings.target_player.getOrDefault(p.getUniqueId(), p).getName()));
				}
			}
		}
	}

	public void clicked_unban_players(Player p, int slot, ItemStack clicked, Inventory inv) {

		if (clicked.getItemMeta().getLore() != null) {
			if (clicked.getItemMeta().getLore().contains(Message.getMessage(p.getUniqueId(), "unban_more"))) {
				if (BanManagerIntegration.isBanManagerEnabled()) {
					if (BanManagerIntegration.unbanPlayer(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()))) {
						p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_unban_player").replace("{player}", ChatColor.stripColor(clicked.getItemMeta().getDisplayName())));
					} else {
						p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
					}
				} else if (Bukkit.getPluginManager().isPluginEnabled("LiteBans")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "unban " + ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
				} else {
					OfflinePlayer target_p = getServer().getOfflinePlayer(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
					if (target_p.isBanned()) {
						Bukkit.getBanList(BanList.Type.NAME).pardon(target_p.getName());
						p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_unban_player").replace("{player}", target_p.getName()));
					} else {
						p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
					}
				}

				p.closeInventory();
			}
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "unban_back"))) {
			p.openInventory(GUI_Main(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "unban_previous"))) {
			unban_page.put(p.getUniqueId(), unban_page.getOrDefault(p.getUniqueId(), 1) - 1);
			p.openInventory(GUI_Unban_Players(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "unban_next"))) {
			unban_page.put(p.getUniqueId(), unban_page.getOrDefault(p.getUniqueId(), 1) + 1);
			p.openInventory(GUI_Unban_Players(p));
		}

	}

	public void clicked_unmute_players(Player p, int slot, ItemStack clicked, Inventory inv) {

		if (clicked.getItemMeta().getLore() != null) {
			if (clicked.getItemMeta().getLore().contains(Message.getMessage(p.getUniqueId(), "unmute_more"))) {
				if (BanManagerIntegration.isBanManagerEnabled()) {
					if (BanManagerIntegration.unmutePlayer(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()))) {
						p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_unmute_player").replace("{player}", ChatColor.stripColor(clicked.getItemMeta().getDisplayName())));
					} else {
						p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
					}
				} else if (Bukkit.getPluginManager().isPluginEnabled("LiteBans")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "unmute " + ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
				}
				p.closeInventory();
			}
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "unmute_back"))) {
			p.openInventory(GUI_Main(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "unmute_previous"))) {
			unmute_page.put(p.getUniqueId(), unmute_page.getOrDefault(p.getUniqueId(), 1) - 1);
			p.openInventory(GUI_Unban_Players(p));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "unmute_next"))) {
			unmute_page.put(p.getUniqueId(), unmute_page.getOrDefault(p.getUniqueId(), 1) + 1);
			p.openInventory(GUI_Unban_Players(p));
		}

	}

	public void clicked_players_settings(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player) {

		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_settings_back"))) {
				p.openInventory(GUI_Players(p));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_settings_info").replace("{player}", target_player.getName()))) {
				p.openInventory(GUI_Players_Settings(p, target_player, target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_settings_actions"))) {
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_settings_ban_player"))) {
				p.openInventory(GUI_Ban(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_settings_warn_player"))) {
				p.openInventory(GUI_Warn(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_settings_mute_player"))) {
				p.openInventory(GUI_Mute(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "players_settings_kick_player"))) {
				p.openInventory(GUI_Kick(p, target_player));
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}

	}

	public void clicked_actions(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player) {

		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_back"))) {
				p.openInventory(GUI_Main(p));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_info").replace("{player}", target_player.getName()))) {
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_survival"))
					|| InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_adventure"))
					|| InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_creative"))
					|| InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_spectator")) ) {
				// Cycle gamemode: SURVIVAL -> CREATIVE -> ADVENTURE -> SPECTATOR -> SURVIVAL
				GameMode current = target_player.getGameMode();
				GameMode next;
				switch (current) {
					case SURVIVAL:
						next = GameMode.CREATIVE;
						break;
					case CREATIVE:
						next = GameMode.ADVENTURE;
						break;
					case ADVENTURE:
						next = GameMode.SPECTATOR;
						break;
					default:
						next = GameMode.SURVIVAL;
						break;
				}
				target_player.setGameMode(next);
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.chat("&7Set &e" + target_player.getName() + "&7 to &b" + next.name()));
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.chat("&7Your gamemode is now &b" + next.name()));
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_teleport_to_player"))) {
				p.teleport(target_player.getLocation());
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_target_player_teleport").replace("{player}", target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_kill_player"))) {
				target_player.setHealth(0);
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_kill").replace("{player}", target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_burn_player"))) {
				target_player.setFireTicks(500);
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_burn").replace("{player}", target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_teleport_player_to_you"))) {
				target_player.teleport(p.getLocation());
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_teleport").replace("{player}", p.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_heal"))) {
				target_player.setHealth(20);
				target_player.setFireTicks(0);
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_heal").replace("{player}", p.getName()));
				p.sendMessage(Message.chat(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_heal").replace("{player}", target_player.getName())));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_feed"))) {
				target_player.setFoodLevel(20);
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_feed").replace("{player}", p.getName()));
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_feed").replace("{player}", target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_god_enabled"))) {
				if (Bukkit.getVersion().contains("1.8")) {
					Settings.god.put(target_player.getUniqueId(), true);
				} else {
					target_player.setInvulnerable(true);
				}
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_god_enabled").replace("{player}", target_player.getName()));
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_god_enabled").replace("{player}", p.getName()));
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_god_disabled"))) {
				if (Bukkit.getVersion().contains("1.8")) {
					Settings.god.put(target_player.getUniqueId(), false);
				} else {
					target_player.setInvulnerable(false);
				}
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_god_disabled").replace("{player}", target_player.getName()));
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_god_disabled").replace("{player}", p.getName()));
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_potions"))) {
				p.openInventory(GUI_potions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_spawner"))) {
				p.openInventory(GUI_Spawner(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_inventory"))) {
				p.openInventory(GUI_Inventory(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_ender_chest"))) {
				p.openInventory(GUI_Ender_Chest(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_vanish_enabled"))) {
			// Toggle vanish ON - hide player from others using built-in Bukkit API
			Settings.vanish.put(target_player.getUniqueId(), true);
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (!online.equals(target_player)) {
					online.hidePlayer(AdminGuiIntegration.getInstance().getBanManagerPlugin(), target_player);
				}
			}
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_hide").replace("{player}", target_player.getName()));
					target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_hide"));
			final Player targetVanish1 = target_player;
				p.closeInventory();
			Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				if (p.isOnline() && targetVanish1.isOnline()) {
					p.openInventory(GUI_Actions(p, targetVanish1));
				}
			}, 2L);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_vanish_disabled"))) {
			// Toggle vanish OFF - show player to others using built-in Bukkit API
			Settings.vanish.put(target_player.getUniqueId(), false);
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (!online.equals(target_player)) {
					online.showPlayer(AdminGuiIntegration.getInstance().getBanManagerPlugin(), target_player);
				}
			}
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_visible").replace("{player}", target_player.getName()));
					target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_visible"));
			final Player targetVanish2 = target_player;
				p.closeInventory();
			Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				if (p.isOnline() && targetVanish2.isOnline()) {
					p.openInventory(GUI_Actions(p, targetVanish2));
				}
			}, 2L);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_lightning"))) {
			// Strike lightning on target player (with delay to prevent GUI issues)
			final Player targetLightning = target_player;
			p.closeInventory();
			Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				if (targetLightning.isOnline()) {
					targetLightning.getWorld().strikeLightning(targetLightning.getLocation());
					targetLightning.setHealth(0); // Instant kill
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.chat("&c⚡ Struck lightning on &e" + targetLightning.getName() + " &c(killed)"));
					targetLightning.sendMessage(Message.getMessage(targetLightning.getUniqueId(), "prefix") + Message.chat("&c⚡ You were struck by lightning!"));
					// Reopen main GUI after lightning (player is dead, can't open their Actions GUI)
					if (p.isOnline()) {
						Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
							p.openInventory(GUI_Main(p));
						}, 20L); // Delay a bit for death animation
					}
				}
			}, 2L);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_firework"))) {
				Fireworks.createRandom(target_player);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_fakeop"))) {
				Bukkit.broadcastMessage(Message.chat("&7&o[Server: Made " + target_player.getName() + " a server operator]"));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_custom"))) {
				Settings.custom_method.put(p.getUniqueId(), 2);
				p.openInventory(GUI_Plugins(p));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_chat_color"))) {
				switch (Settings.chat_color.getOrDefault(target_player.getUniqueId(), "LIGHT_GRAY_WOOL")) {
					case "WHITE_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "ORANGE_WOOL");
						break;
					case "ORANGE_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "MAGENTA_WOOL");
						break;
					case "MAGENTA_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "LIGHT_BLUE_WOOL");
						break;
					case "LIGHT_BLUE_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "YELLOW_WOOL");
						break;
					case "YELLOW_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "LIME_WOOL");
						break;
					case "LIME_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "GRAY_WOOL");
						break;
					case "GRAY_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "CLOCK");
						break;
					case "CLOCK":
						if (getVersion().contains("1.8") || getVersion().contains("1.9") || getVersion().contains("1.10") || getVersion().contains("1.11") || getVersion().contains("1.12") || getVersion().contains("1.13") || getVersion().contains("1.14") || getVersion().contains("1.15")) {
							Settings.chat_color.put(target_player.getUniqueId(), "LIGHT_GRAY_WOOL");
						} else {
							Settings.chat_color.put(target_player.getUniqueId(), "EXPERIENCE_BOTTLE");
						}
						break;
					case "EXPERIENCE_BOTTLE":
						Settings.chat_color.put(target_player.getUniqueId(), "LIGHT_GRAY_WOOL");
						break;
					case "LIGHT_GRAY_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "CYAN_WOOL");
						break;
					case "CYAN_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "PURPLE_WOOL");
						break;
					case "PURPLE_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "BLUE_WOOL");
						break;
					case "BLUE_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "GREEN_WOOL");
						break;
					case "GREEN_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "PINK_WOOL");
						break;
					case "PINK_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "RED_WOOL");
						break;
					case "RED_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "BLACK_WOOL");
						break;
					case "BLACK_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "WHITE_WOOL");
						break;
				}
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_freeze_enabled")) || InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_freeze_disabled"))) {
			final Player targetRef = target_player;
				if (Settings.freeze.getOrDefault(target_player.getUniqueId(), false)) {
				// Unfreeze player
					Settings.freeze.put(target_player.getUniqueId(), false);
					AdminGuiIntegration.getInstance().getPlayers().set(target_player.getUniqueId() + ".frozen", null);
					if (AdminGuiIntegration.getInstance().getConf().getString("freeze_title", null) != null || AdminGuiIntegration.getInstance().getConf().getString("freeze_subtitle", null) != null)
						target_player.resetTitle();
					Settings.custom_chat_channel.remove(target_player.getUniqueId());
					target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "message_freeze_disabled").replace("{player}", p.getName()));
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.chat("&aUnfroze &e" + target_player.getName()));
				} else {
					if (!TargetPlayer.hasPermission(target_player, "admingui.freeze.bypass")) {
					// Freeze player
						Settings.freeze.put(target_player.getUniqueId(), true);
						AdminGuiIntegration.getInstance().getPlayers().set(target_player.getUniqueId() + ".frozen", true);
						if (AdminGuiIntegration.getInstance().getConf().getString("freeze_title", null) != null && AdminGuiIntegration.getInstance().getConf().getString("freeze_subtitle", null) != null)
							target_player.sendTitle(Message.chat(AdminGuiIntegration.getInstance().getConf().getString("freeze_title", "")), Message.chat(AdminGuiIntegration.getInstance().getConf().getString("freeze_subtitle", "")), 50, 72000, 50);
						Settings.custom_chat_channel.put(target_player.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("freeze_admin_chat", "adminchat"));
						target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "message_freeze_enabled").replace("{player}", p.getName()));
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.chat("&aFroze &e" + target_player.getName()));
					} else {
						p.closeInventory();
						p.sendMessage(Message.getMessage(p.getUniqueId(), "permission"));
					return;
					}
				}
				AdminGuiIntegration.getInstance().savePlayers();
			// Use delayed task to reopen inventory to prevent cursor jump
			p.closeInventory();
			Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				if (p.isOnline() && targetRef.isOnline()) {
					p.openInventory(GUI_Actions(p, targetRef));
				}
			}, 2L);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_money"))) {
				p.openInventory(GUI_Money(p, target_player));
			}
		} else if (AdminGuiIntegration.getInstance().getConf().getBoolean("bungeecord_enabled", false)) {
			//TODO: Bungee
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_back"))) {
				p.openInventory(GUI_Main(p));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_info").replace("{player}", target_player.getName()))) {
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_survival"))) {
				Channel.send(p.getName(), "gamemode", target_player.getName(), "adventure");
				//target_player.setGameMode(GameMode.ADVENTURE);
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_adventure"))) {
				target_player.setGameMode(GameMode.CREATIVE);
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_creative"))) {
				target_player.setGameMode(GameMode.SPECTATOR);
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_spectator"))) {
				target_player.setGameMode(GameMode.SURVIVAL);
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_teleport_to_player"))) {
				p.teleport(target_player.getLocation());
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_target_player_teleport").replace("{player}", target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_kill_player"))) {
				target_player.setHealth(0);
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_kill").replace("{player}", target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_burn_player"))) {
				target_player.setFireTicks(500);
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_burn").replace("{player}", target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_teleport_player_to_you"))) {
				target_player.teleport(p.getLocation());
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_teleport").replace("{player}", p.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_heal"))) {
				target_player.setHealth(20);
				target_player.setFireTicks(0);
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_heal").replace("{player}", p.getName()));
				p.sendMessage(Message.chat(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_heal").replace("{player}", target_player.getName())));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_feed"))) {
				target_player.setFoodLevel(20);
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_feed").replace("{player}", p.getName()));
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_feed").replace("{player}", target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_god_enabled"))) {
				if (Bukkit.getVersion().contains("1.8")) {
					Settings.god.put(target_player.getUniqueId(), true);
				} else {
					target_player.setInvulnerable(true);
				}
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_god_enabled").replace("{player}", target_player.getName()));
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_god_enabled").replace("{player}", p.getName()));
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_god_disabled"))) {
				if (Bukkit.getVersion().contains("1.8")) {
					Settings.god.put(target_player.getUniqueId(), false);
				} else {
					target_player.setInvulnerable(false);
				}
				p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_god_disabled").replace("{player}", target_player.getName()));
				target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_god_disabled").replace("{player}", p.getName()));
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_potions"))) {
				p.openInventory(GUI_potions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_spawner"))) {
				p.openInventory(GUI_Spawner(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_inventory"))) {
				p.openInventory(GUI_Inventory(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_ender_chest"))) {
				p.openInventory(GUI_Ender_Chest(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_vanish_enabled"))) {
			// Toggle vanish ON - hide player from others using built-in Bukkit API
			Settings.vanish.put(target_player.getUniqueId(), true);
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (!online.equals(target_player)) {
					online.hidePlayer(AdminGuiIntegration.getInstance().getBanManagerPlugin(), target_player);
				}
			}
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_hide").replace("{player}", target_player.getName()));
					target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_hide"));
			final Player targetVanish1 = target_player;
				p.closeInventory();
			Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				if (p.isOnline() && targetVanish1.isOnline()) {
					p.openInventory(GUI_Actions(p, targetVanish1));
				}
			}, 2L);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_vanish_disabled"))) {
			// Toggle vanish OFF - show player to others using built-in Bukkit API
			Settings.vanish.put(target_player.getUniqueId(), false);
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (!online.equals(target_player)) {
					online.showPlayer(AdminGuiIntegration.getInstance().getBanManagerPlugin(), target_player);
				}
			}
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_visible").replace("{player}", target_player.getName()));
					target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_visible"));
			final Player targetVanish2 = target_player;
				p.closeInventory();
			Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				if (p.isOnline() && targetVanish2.isOnline()) {
					p.openInventory(GUI_Actions(p, targetVanish2));
				}
			}, 2L);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_lightning"))) {
			// Strike lightning on target player (with delay to prevent GUI issues)
			final Player targetLightning = target_player;
			p.closeInventory();
			Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
				if (targetLightning.isOnline()) {
					targetLightning.getWorld().strikeLightning(targetLightning.getLocation());
					targetLightning.setHealth(0); // Instant kill
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.chat("&c⚡ Struck lightning on &e" + targetLightning.getName() + " &c(killed)"));
					targetLightning.sendMessage(Message.getMessage(targetLightning.getUniqueId(), "prefix") + Message.chat("&c⚡ You were struck by lightning!"));
					// Reopen main GUI after lightning (player is dead, can't open their Actions GUI)
					if (p.isOnline()) {
						Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
							p.openInventory(GUI_Main(p));
						}, 20L); // Delay a bit for death animation
					}
				}
			}, 2L);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_firework"))) {
				Fireworks.createRandom(target_player);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_fakeop"))) {
				Bukkit.broadcastMessage(Message.chat("&7&o[Server: Made " + target_player.getName() + " a server operator]"));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_custom"))) {
				Settings.custom_method.put(p.getUniqueId(), 2);
				p.openInventory(GUI_Plugins(p));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_chat_color"))) {
				switch (Settings.chat_color.getOrDefault(target_player.getUniqueId(), "LIGHT_GRAY_WOOL")) {
					case "WHITE_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "ORANGE_WOOL");
						break;
					case "ORANGE_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "MAGENTA_WOOL");
						break;
					case "MAGENTA_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "LIGHT_BLUE_WOOL");
						break;
					case "LIGHT_BLUE_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "YELLOW_WOOL");
						break;
					case "YELLOW_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "LIME_WOOL");
						break;
					case "LIME_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "GRAY_WOOL");
						break;
					case "GRAY_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "CLOCK");
						break;
					case "CLOCK":
						if (getVersion().contains("1.8") || getVersion().contains("1.9") || getVersion().contains("1.10") || getVersion().contains("1.11") || getVersion().contains("1.12") || getVersion().contains("1.13") || getVersion().contains("1.14") || getVersion().contains("1.15")) {
							Settings.chat_color.put(target_player.getUniqueId(), "LIGHT_GRAY_WOOL");
						} else {
							Settings.chat_color.put(target_player.getUniqueId(), "EXPERIENCE_BOTTLE");
						}
						break;
					case "EXPERIENCE_BOTTLE":
						Settings.chat_color.put(target_player.getUniqueId(), "LIGHT_GRAY_WOOL");
						break;
					case "LIGHT_GRAY_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "CYAN_WOOL");
						break;
					case "CYAN_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "PURPLE_WOOL");
						break;
					case "PURPLE_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "BLUE_WOOL");
						break;
					case "BLUE_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "GREEN_WOOL");
						break;
					case "GREEN_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "PINK_WOOL");
						break;
					case "PINK_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "RED_WOOL");
						break;
					case "RED_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "BLACK_WOOL");
						break;
					case "BLACK_WOOL":
						Settings.chat_color.put(target_player.getUniqueId(), "WHITE_WOOL");
						break;
				}
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_freeze_enabled")) || InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_freeze_disabled"))) {
				if (Settings.freeze.getOrDefault(target_player.getUniqueId(), false)) {
					Settings.freeze.put(target_player.getUniqueId(), false);
					AdminGuiIntegration.getInstance().getPlayers().set(target_player.getUniqueId() + ".frozen", null);
					if (AdminGuiIntegration.getInstance().getConf().getString("freeze_title", null) != null || AdminGuiIntegration.getInstance().getConf().getString("freeze_subtitle", null) != null)
						target_player.resetTitle();
					Settings.custom_chat_channel.remove(target_player.getUniqueId());
					target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "message_freeze_disabled").replace("{player}", p.getName()));
				} else {
					if (!TargetPlayer.hasPermission(target_player, "admingui.freeze.bypass")) {
						Settings.freeze.put(target_player.getUniqueId(), true);
						AdminGuiIntegration.getInstance().getPlayers().set(target_player.getUniqueId() + ".frozen", true);
						if (AdminGuiIntegration.getInstance().getConf().getString("freeze_title", null) != null && AdminGuiIntegration.getInstance().getConf().getString("freeze_subtitle", null) != null)
							target_player.sendTitle(Message.chat(AdminGuiIntegration.getInstance().getConf().getString("freeze_title", "")), Message.chat(AdminGuiIntegration.getInstance().getConf().getString("freeze_subtitle", "")), 50, 72000, 50);
						Settings.custom_chat_channel.put(target_player.getUniqueId(), AdminGuiIntegration.getInstance().getConf().getString("freeze_admin_chat", "adminchat"));
						target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "message_freeze_enabled").replace("{player}", p.getName()));
					} else {
						p.closeInventory();
						p.sendMessage(Message.getMessage(p.getUniqueId(), "permission"));
					}
				}
				AdminGuiIntegration.getInstance().savePlayers();
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_money"))) {
				p.openInventory(GUI_Money(p, target_player));
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}

	}

	public void clicked_kick(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player) {

		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "kick_back"))) {
				p.openInventory(GUI_Players_Settings(p, target_player, target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_silence_disabled"))) {
				kick_silence.put(p.getUniqueId(), true);
				p.openInventory(GUI_Kick(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_silence_enabled"))) {
				kick_silence.put(p.getUniqueId(), false);
				p.openInventory(GUI_Kick(p, target_player));
			} else if (clicked.getType() != Material.AIR && !Objects.requireNonNull(clicked.getItemMeta()).getDisplayName().equals(" ")) {
				if (TargetPlayer.hasPermission(target_player, "admingui.kick.bypass")) {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_kick_bypass"));
				} else {
					String kickReason = AdminGuiIntegration.getInstance().getKick().getString("slots." + (slot + 1) + ".name");
					if (BanManagerIntegration.isBanManagerEnabled()) {
						BanManagerIntegration.kickPlayer(p.getUniqueId().toString(), p.getName(), target_player.getUniqueId().toString(), target_player.getName(), kickReason, kick_silence.getOrDefault(p.getUniqueId(), false));
					} else {
						// Fallback: kick with basic message if BanManager is not available
						target_player.kickPlayer("You have been kicked for: " + ChatColor.stripColor(Message.chat(kickReason)));
					}
					// No additional broadcast - BanManager handles kick messaging
				}
				// Keep GUI open for multiple punishments
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}

	}

	public void clicked_ban(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player) {

		long mil_year = 31556952000L;
		long mil_month = 2592000000L;
		long mil_day = 86400000L;
		long mil_hour = 3600000L;
		long mil_minute = 60000L;

		Date time = null;

		if (!(ban_minutes.getOrDefault(p.getUniqueId(), 0) == 0 && ban_hours.getOrDefault(p.getUniqueId(), 0) == 0 && ban_days.getOrDefault(p.getUniqueId(), 0) == 0 && ban_months.getOrDefault(p.getUniqueId(), 0) == 0 && ban_years.getOrDefault(p.getUniqueId(), 0) == 0)) {
			time = new Date(System.currentTimeMillis() + (mil_minute * ban_minutes.getOrDefault(p.getUniqueId(), 0)) + (mil_hour * ban_hours.getOrDefault(p.getUniqueId(), 0)) + (mil_day * ban_days.getOrDefault(p.getUniqueId(), 0)) + (mil_month * ban_months.getOrDefault(p.getUniqueId(), 0)) + (mil_year * ban_years.getOrDefault(p.getUniqueId(), 0)));
		}

		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_back"))) {
				p.openInventory(GUI_Players_Settings(p, target_player, target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_silence_disabled"))) {
				ban_silence.put(p.getUniqueId(), true);
				p.openInventory(GUI_Ban(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_silence_enabled"))) {
				ban_silence.put(p.getUniqueId(), false);
				p.openInventory(GUI_Ban(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_hacking"))) {
				if (TargetPlayer.hasPermission(target_player, "admingui.ban.bypass")) {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_ban_bypass"));
				} else {
					executePunishmentWithCleanBroadcast(p, target_player, "ban", "ban_hacking", time, ban_silence.getOrDefault(p.getUniqueId(), false), "message_player_ban");
				}
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_griefing"))) {
				if (TargetPlayer.hasPermission(target_player, "admingui.ban.bypass")) {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_ban_bypass"));
				} else {
					executePunishmentWithCleanBroadcast(p, target_player, "ban", "ban_griefing", time, ban_silence.getOrDefault(p.getUniqueId(), false), "message_player_ban");
				}
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_spamming"))) {
				if (TargetPlayer.hasPermission(target_player, "admingui.ban.bypass")) {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_ban_bypass"));
				} else {
									executePunishmentWithCleanBroadcast(p, target_player, "ban", "ban_spamming", time, ban_silence.getOrDefault(p.getUniqueId(), false), "message_player_ban");
				}
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_advertising"))) {
				if (TargetPlayer.hasPermission(target_player, "admingui.ban.bypass")) {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_ban_bypass"));
				} else {
									executePunishmentWithCleanBroadcast(p, target_player, "ban", "ban_advertising", time, ban_silence.getOrDefault(p.getUniqueId(), false), "message_player_ban");
				}
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_swearing"))) {
				if (TargetPlayer.hasPermission(target_player, "admingui.ban.bypass")) {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_ban_bypass"));
				} else {
									executePunishmentWithCleanBroadcast(p, target_player, "ban", "ban_swearing", time, ban_silence.getOrDefault(p.getUniqueId(), false), "message_player_ban");
				}
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_years"))) {
				switch (ban_years.getOrDefault(p.getUniqueId(), 0)) {
					case 0: ban_years.put(p.getUniqueId(), 1); break;
					case 1: ban_years.put(p.getUniqueId(), 2); break;
					case 2: ban_years.put(p.getUniqueId(), 3); break;
					case 3: ban_years.put(p.getUniqueId(), 4); break;
					case 4: ban_years.put(p.getUniqueId(), 5); break;
					case 5: ban_years.put(p.getUniqueId(), 6); break;
					case 6: ban_years.put(p.getUniqueId(), 7); break;
					case 7: ban_years.put(p.getUniqueId(), 8); break;
					case 8: ban_years.put(p.getUniqueId(), 9); break;
					case 9: ban_years.put(p.getUniqueId(), 10); break;
					case 10: ban_years.put(p.getUniqueId(), 15); break;
					case 15: ban_years.put(p.getUniqueId(), 20); break;
					case 20: ban_years.put(p.getUniqueId(), 30); break;
					case 30: ban_years.put(p.getUniqueId(), 0); break;
				}
				p.openInventory(GUI_Ban(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_months"))) {
				switch (ban_months.getOrDefault(p.getUniqueId(), 0)) {
					case 0: ban_months.put(p.getUniqueId(), 1); break;
					case 1: ban_months.put(p.getUniqueId(), 2); break;
					case 2: ban_months.put(p.getUniqueId(), 3); break;
					case 3: ban_months.put(p.getUniqueId(), 4); break;
					case 4: ban_months.put(p.getUniqueId(), 5); break;
					case 5: ban_months.put(p.getUniqueId(), 6); break;
					case 6: ban_months.put(p.getUniqueId(), 7); break;
					case 7: ban_months.put(p.getUniqueId(), 8); break;
					case 8: ban_months.put(p.getUniqueId(), 9); break;
					case 9: ban_months.put(p.getUniqueId(), 10); break;
					case 10: ban_months.put(p.getUniqueId(), 11); break;
					case 11: ban_months.put(p.getUniqueId(), 12); break;
					case 12: ban_months.put(p.getUniqueId(), 0); break;
				}
				p.openInventory(GUI_Ban(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_days"))) {
				switch (ban_days.getOrDefault(p.getUniqueId(), 0)) {
					case 0: ban_days.put(p.getUniqueId(), 1); break;
					case 1: ban_days.put(p.getUniqueId(), 2); break;
					case 2: ban_days.put(p.getUniqueId(), 3); break;
					case 3: ban_days.put(p.getUniqueId(), 4); break;
					case 4: ban_days.put(p.getUniqueId(), 5); break;
					case 5: ban_days.put(p.getUniqueId(), 6); break;
					case 6: ban_days.put(p.getUniqueId(), 7); break;
					case 7: ban_days.put(p.getUniqueId(), 8); break;
					case 8: ban_days.put(p.getUniqueId(), 9); break;
					case 9: ban_days.put(p.getUniqueId(), 10); break;
					case 10: ban_days.put(p.getUniqueId(), 15); break;
					case 15: ban_days.put(p.getUniqueId(), 20); break;
					case 20: ban_days.put(p.getUniqueId(), 30); break;
					case 30: ban_days.put(p.getUniqueId(), 0); break;
				}
				p.openInventory(GUI_Ban(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_hours"))) {
				switch (ban_hours.getOrDefault(p.getUniqueId(), 0)) {
					case 0: ban_hours.put(p.getUniqueId(), 1); break;
					case 1: ban_hours.put(p.getUniqueId(), 2); break;
					case 2: ban_hours.put(p.getUniqueId(), 3); break;
					case 3: ban_hours.put(p.getUniqueId(), 6); break;
					case 6: ban_hours.put(p.getUniqueId(), 12); break;
					case 12: ban_hours.put(p.getUniqueId(), 24); break;
					case 24: ban_hours.put(p.getUniqueId(), 48); break;
					case 48: ban_hours.put(p.getUniqueId(), 72); break;
					case 72: ban_hours.put(p.getUniqueId(), 168); break; // 1 week
					case 168: ban_hours.put(p.getUniqueId(), 0); break;
				}
				p.openInventory(GUI_Ban(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "ban_minutes"))) {
				switch (ban_minutes.getOrDefault(p.getUniqueId(), 0)) {
					case 0: ban_minutes.put(p.getUniqueId(), 5); break;
					case 5: ban_minutes.put(p.getUniqueId(), 10); break;
					case 10: ban_minutes.put(p.getUniqueId(), 15); break;
					case 15: ban_minutes.put(p.getUniqueId(), 30); break;
					case 30: ban_minutes.put(p.getUniqueId(), 45); break;
					case 45: ban_minutes.put(p.getUniqueId(), 60); break;
					case 60: ban_minutes.put(p.getUniqueId(), 90); break;
					case 90: ban_minutes.put(p.getUniqueId(), 120); break;
					case 120: ban_minutes.put(p.getUniqueId(), 0); break;
				}
				p.openInventory(GUI_Ban(p, target_player));
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}

	}

	public void clicked_warn(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player) {

        // No durations for warns anymore
        Date time = null;

		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "warn_back"))) {
				p.openInventory(GUI_Players_Settings(p, target_player, target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "warn_silence_disabled"))) {
				warn_silence.put(p.getUniqueId(), true);
				p.openInventory(GUI_Warn(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "warn_silence_enabled"))) {
				warn_silence.put(p.getUniqueId(), false);
				p.openInventory(GUI_Warn(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "warn_rule_violation"))) {
				executePunishmentWithCleanBroadcast(p, target_player, "warn", "warn_rule_violation", time, warn_silence.getOrDefault(p.getUniqueId(), false), "message_player_warn");
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "warn_minor_offense"))) {
				executePunishmentWithCleanBroadcast(p, target_player, "warn", "warn_minor_offense", time, warn_silence.getOrDefault(p.getUniqueId(), false), "message_player_warn");
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "warn_chat_spam"))) {
				executePunishmentWithCleanBroadcast(p, target_player, "warn", "warn_chat_spam", time, warn_silence.getOrDefault(p.getUniqueId(), false), "message_player_warn");
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "warn_inappropriate_language"))) {
				executePunishmentWithCleanBroadcast(p, target_player, "warn", "warn_inappropriate_language", time, warn_silence.getOrDefault(p.getUniqueId(), false), "message_player_warn");
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "warn_general"))) {
				executePunishmentWithCleanBroadcast(p, target_player, "warn", "warn_general", time, warn_silence.getOrDefault(p.getUniqueId(), false), "message_player_warn");
				// Keep GUI open for multiple punishments
            }
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}

	}

	public void clicked_mute(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player) {

		long mil_year = 31556952000L;
		long mil_month = 2592000000L;
		long mil_day = 86400000L;
		long mil_hour = 3600000L;
		long mil_minute = 60000L;

		Date time = null;

		if (!(mute_minutes.getOrDefault(p.getUniqueId(), 0) == 0 && mute_hours.getOrDefault(p.getUniqueId(), 0) == 0 && mute_days.getOrDefault(p.getUniqueId(), 0) == 0 && mute_months.getOrDefault(p.getUniqueId(), 0) == 0 && mute_years.getOrDefault(p.getUniqueId(), 0) == 0)) {
			time = new Date(System.currentTimeMillis() + (mil_minute * mute_minutes.getOrDefault(p.getUniqueId(), 0)) + (mil_hour * mute_hours.getOrDefault(p.getUniqueId(), 0)) + (mil_day * mute_days.getOrDefault(p.getUniqueId(), 0)) + (mil_month * mute_months.getOrDefault(p.getUniqueId(), 0)) + (mil_year * mute_years.getOrDefault(p.getUniqueId(), 0)));
		}

		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_back"))) {
				p.openInventory(GUI_Players_Settings(p, target_player, target_player.getName()));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_silence_disabled"))) {
				mute_silence.put(p.getUniqueId(), true);
				p.openInventory(GUI_Mute(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_silence_enabled"))) {
				mute_silence.put(p.getUniqueId(), false);
				p.openInventory(GUI_Mute(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_chat_abuse"))) {
				executePunishmentWithCleanBroadcast(p, target_player, "mute", "mute_chat_abuse", time, mute_silence.getOrDefault(p.getUniqueId(), false), "message_player_mute");
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_spamming"))) {
				executePunishmentWithCleanBroadcast(p, target_player, "mute", "mute_spamming", time, mute_silence.getOrDefault(p.getUniqueId(), false), "message_player_mute");
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_inappropriate_language"))) {
				executePunishmentWithCleanBroadcast(p, target_player, "mute", "mute_inappropriate_language", time, mute_silence.getOrDefault(p.getUniqueId(), false), "message_player_mute");
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_harassment"))) {
				executePunishmentWithCleanBroadcast(p, target_player, "mute", "mute_harassment", time, mute_silence.getOrDefault(p.getUniqueId(), false), "message_player_mute");
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_advertising"))) {
				executePunishmentWithCleanBroadcast(p, target_player, "mute", "mute_advertising", time, mute_silence.getOrDefault(p.getUniqueId(), false), "message_player_mute");
				// Keep GUI open for multiple punishments
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_years"))) {
				switch (mute_years.getOrDefault(p.getUniqueId(), 0)) {
					case 0:
						mute_years.put(p.getUniqueId(), 1);
						break;
					case 1:
						mute_years.put(p.getUniqueId(), 2);
						break;
					case 2:
						mute_years.put(p.getUniqueId(), 3);
						break;
					case 3:
						mute_years.put(p.getUniqueId(), 5);
						break;
					case 5:
						mute_years.put(p.getUniqueId(), 0);
						break;
				}
				p.openInventory(GUI_Mute(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_months"))) {
				switch (mute_months.getOrDefault(p.getUniqueId(), 0)) {
					case 0:
						mute_months.put(p.getUniqueId(), 1);
						break;
					case 1:
						mute_months.put(p.getUniqueId(), 3);
						break;
					case 3:
						mute_months.put(p.getUniqueId(), 6);
						break;
					case 6:
						mute_months.put(p.getUniqueId(), 12);
						break;
					case 12:
						mute_months.put(p.getUniqueId(), 0);
						break;
				}
				p.openInventory(GUI_Mute(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_days"))) {
				switch (mute_days.getOrDefault(p.getUniqueId(), 0)) {
					case 0:
						mute_days.put(p.getUniqueId(), 1);
						break;
					case 1:
						mute_days.put(p.getUniqueId(), 3);
						break;
					case 3:
						mute_days.put(p.getUniqueId(), 7);
						break;
					case 7:
						mute_days.put(p.getUniqueId(), 14);
						break;
					case 14:
						mute_days.put(p.getUniqueId(), 30);
						break;
					case 30:
						mute_days.put(p.getUniqueId(), 0);
						break;
				}
				p.openInventory(GUI_Mute(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_hours"))) {
				switch (mute_hours.getOrDefault(p.getUniqueId(), 0)) {
					case 0:
						mute_hours.put(p.getUniqueId(), 1);
						break;
					case 1:
						mute_hours.put(p.getUniqueId(), 2);
						break;
					case 2:
						mute_hours.put(p.getUniqueId(), 3);
						break;
					case 3:
						mute_hours.put(p.getUniqueId(), 6);
						break;
					case 6:
						mute_hours.put(p.getUniqueId(), 12);
						break;
					case 12:
						mute_hours.put(p.getUniqueId(), 24);
						break;
					case 24:
						mute_hours.put(p.getUniqueId(), 48);
						break;
					case 48:
						mute_hours.put(p.getUniqueId(), 72);
						break;
					case 72:
						mute_hours.put(p.getUniqueId(), 0);
						break;
				}
				p.openInventory(GUI_Mute(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "mute_minutes"))) {
				switch (mute_minutes.getOrDefault(p.getUniqueId(), 0)) {
					case 0:
						mute_minutes.put(p.getUniqueId(), 5);
						break;
					case 5:
						mute_minutes.put(p.getUniqueId(), 10);
						break;
					case 10:
						mute_minutes.put(p.getUniqueId(), 15);
						break;
					case 15:
						mute_minutes.put(p.getUniqueId(), 30);
						break;
					case 30:
						mute_minutes.put(p.getUniqueId(), 45);
						break;
					case 45:
						mute_minutes.put(p.getUniqueId(), 60);
						break;
					case 60:
						mute_minutes.put(p.getUniqueId(), 90);
						break;
					case 90:
						mute_minutes.put(p.getUniqueId(), 120);
						break;
					case 120:
						mute_minutes.put(p.getUniqueId(), 0);
						break;
				}
				p.openInventory(GUI_Mute(p, target_player));
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}

	}

	public void clicked_potions(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player) {

		TargetPlayer targetPlayer = new TargetPlayer();

		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_back"))) {
				if (p.getName().equals(target_player.getName())) {
					p.openInventory(GUI_Player(p));
				} else {
					p.openInventory(GUI_Actions(p, target_player));
				}

			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_time"))) {
				switch (duration.getOrDefault(p.getUniqueId(), 1)) {
					case 1:
						duration.put(p.getUniqueId(), 2);
						break;
					case 2:
						duration.put(p.getUniqueId(), 3);
						break;
					case 3:
						duration.put(p.getUniqueId(), 4);
						break;
					case 4:
						duration.put(p.getUniqueId(), 5);
						break;
					case 5:
						duration.put(p.getUniqueId(), 7);
						break;
					case 7:
						duration.put(p.getUniqueId(), 10);
						break;
					case 10:
						duration.put(p.getUniqueId(), 15);
						break;
					case 15:
						duration.put(p.getUniqueId(), 20);
						break;
					case 20:
						duration.put(p.getUniqueId(), 1000000);
						break;
					case 1000000:
						duration.put(p.getUniqueId(), 1);
						break;
				}
				p.openInventory(GUI_potions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_level"))) {
				switch (level.getOrDefault(p.getUniqueId(), 1)) {
					case 1:
						level.put(p.getUniqueId(), 2);
						break;
					case 2:
						level.put(p.getUniqueId(), 3);
						break;
					case 3:
						level.put(p.getUniqueId(), 4);
						break;
					case 4:
						level.put(p.getUniqueId(), 5);
						break;
					case 5:
						level.put(p.getUniqueId(), 1);
						break;
				}
				p.openInventory(GUI_potions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_remove_all"))) {
				for (PotionEffect effect : target_player.getActivePotionEffects()) {
					target_player.removePotionEffect(effect.getType());
				}

				if (p.getName().equals(target_player.getName())) {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_potions_remove"));
				} else {
					target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_target_player_potions_remove").replace("{player}", p.getName()));
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_potions_remove").replace("{player}", target_player.getName()));
				}

				p.openInventory(GUI_potions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_night_vision"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.NIGHT_VISION, "potions_night_vision", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_invisibility"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.INVISIBILITY, "potions_invisibility", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_jump_boost"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.JUMP_BOOST, "potions_jump_boost", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_fire_resistance"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.FIRE_RESISTANCE, "potions_fire_resistance", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_speed"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.SPEED, "potions_speed", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_slowness"))) {
				                                targetPlayer.setPotionEffect(p, target_player, PotionEffectType.SLOWNESS, "potions_slowness", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_water_breathing"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.WATER_BREATHING, "potions_water_breathing", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_instant_health"))) {
				                                targetPlayer.setPotionEffect(p, target_player, PotionEffectType.INSTANT_HEALTH, "potions_instant_health", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_instant_damage"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.INSTANT_DAMAGE, "potions_instant_damage", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_poison"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.POISON, "potions_poison", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_regeneration"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.REGENERATION, "potions_regeneration", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_strength"))) {
				                                targetPlayer.setPotionEffect(p, target_player, PotionEffectType.STRENGTH, "potions_strength", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_weakness"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.WEAKNESS, "potions_weakness", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_luck"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.LUCK, "potions_luck", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_slow_falling"))) {
				targetPlayer.setPotionEffect(p, target_player, PotionEffectType.SLOW_FALLING, "potions_slow_falling", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_glowing"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.GLOWING, "potions_glowing", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_levitation"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.LEVITATION, "potions_levitation", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_absorption"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.ABSORPTION, "potions_absorption", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_health_boost"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.HEALTH_BOOST, "potions_health_boost", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_hunger"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.HUNGER, "potions_hunger", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_mining_fatigue"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.MINING_FATIGUE, "potions_mining_fatigue", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_nausea"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.NAUSEA, "potions_nausea", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_resistance"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.RESISTANCE, "potions_resistance", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_saturation"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.SATURATION, "potions_saturation", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_wither"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.WITHER, "potions_wither", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_haste"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.HASTE, "potions_haste", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_conduit_power"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.CONDUIT_POWER, "potions_conduit_power", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_dolphins_grace"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.DOLPHINS_GRACE, "potions_dolphins_grace", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_bad_luck"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.UNLUCK, "potions_bad_luck", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_darkness"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.DARKNESS, "potions_darkness", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_wind_charged"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.WIND_CHARGED, "potions_wind_charged", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_weaving"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.WEAVING, "potions_weaving", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_oozing"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.OOZING, "potions_oozing", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_infested"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.INFESTED, "potions_infested", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_raid_omen"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.RAID_OMEN, "potions_raid_omen", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_trial_omen"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.TRIAL_OMEN, "potions_trial_omen", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_bad_omen"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.BAD_OMEN, "potions_bad_omen", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
		} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "potions_hero_of_the_village"))) {
			targetPlayer.setPotionEffect(p, target_player, PotionEffectType.HERO_OF_THE_VILLAGE, "potions_hero_of_the_village", duration.getOrDefault(p.getUniqueId(), 1), level.getOrDefault(p.getUniqueId(), 1));
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}
	}

	public void clicked_spawner(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player) {

		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_back"))) {
				if (p.getName().equals(target_player.getName())) {
					p.openInventory(GUI_Player(p));
				} else {
					p.openInventory(GUI_Actions(p, target_player));
				}
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_bat"))) {
				Entity.spawn(target_player.getLocation(), EntityType.BAT);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_bee"))) {
				Entity.spawn(target_player.getLocation(), EntityType.BEE);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_blaze"))) {
				Entity.spawn(target_player.getLocation(), EntityType.BLAZE);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_cat"))) {
				Entity.spawn(target_player.getLocation(), EntityType.CAT);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_cave_spider"))) {
				Entity.spawn(target_player.getLocation(), EntityType.CAVE_SPIDER);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_chicken"))) {
				Entity.spawn(target_player.getLocation(), EntityType.CHICKEN);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_cod"))) {
				Entity.spawn(target_player.getLocation(), EntityType.COD);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_cow"))) {
				Entity.spawn(target_player.getLocation(), EntityType.COW);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_creeper"))) {
				Entity.spawn(target_player.getLocation(), EntityType.CREEPER);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_dolphin"))) {
				Entity.spawn(target_player.getLocation(), EntityType.DOLPHIN);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_donkey"))) {
				Entity.spawn(target_player.getLocation(), EntityType.DONKEY);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_drowned"))) {
				Entity.spawn(target_player.getLocation(), EntityType.DROWNED);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_elder_guardian"))) {
				Entity.spawn(target_player.getLocation(), EntityType.ELDER_GUARDIAN);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_enderman"))) {
				Entity.spawn(target_player.getLocation(), EntityType.ENDERMAN);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_endermite"))) {
				Entity.spawn(target_player.getLocation(), EntityType.ENDERMITE);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_evoker"))) {
				Entity.spawn(target_player.getLocation(), EntityType.EVOKER);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_fox"))) {
				Entity.spawn(target_player.getLocation(), EntityType.FOX);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_ghast"))) {
				Entity.spawn(target_player.getLocation(), EntityType.GHAST);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_guardian"))) {
				Entity.spawn(target_player.getLocation(), EntityType.GUARDIAN);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_horse"))) {
				Entity.spawn(target_player.getLocation(), EntityType.HORSE);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_husk"))) {
				Entity.spawn(target_player.getLocation(), EntityType.HUSK);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_llama"))) {
				Entity.spawn(target_player.getLocation(), EntityType.LLAMA);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_magma_cube"))) {
				Entity.spawn(target_player.getLocation(), EntityType.MAGMA_CUBE);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_mooshroom"))) {
				                                Entity.spawn(target_player.getLocation(), EntityType.MOOSHROOM);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_mule"))) {
				Entity.spawn(target_player.getLocation(), EntityType.MULE);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_ocelot"))) {
				Entity.spawn(target_player.getLocation(), EntityType.OCELOT);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_panda"))) {
				Entity.spawn(target_player.getLocation(), EntityType.PANDA);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_parrot"))) {
				Entity.spawn(target_player.getLocation(), EntityType.PARROT);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_phantom"))) {
				Entity.spawn(target_player.getLocation(), EntityType.PHANTOM);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_pig"))) {
				Entity.spawn(target_player.getLocation(), EntityType.PIG);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_pillager"))) {
				Entity.spawn(target_player.getLocation(), EntityType.PILLAGER);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_polar_bear"))) {
				Entity.spawn(target_player.getLocation(), EntityType.POLAR_BEAR);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_pufferfish"))) {
				Entity.spawn(target_player.getLocation(), EntityType.PUFFERFISH);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_rabbit"))) {
				Entity.spawn(target_player.getLocation(), EntityType.RABBIT);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_ravager"))) {
				Entity.spawn(target_player.getLocation(), EntityType.RAVAGER);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_salmon"))) {
				Entity.spawn(target_player.getLocation(), EntityType.SALMON);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_sheep"))) {
				Entity.spawn(target_player.getLocation(), EntityType.SHEEP);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_shulker"))) {
				Entity.spawn(target_player.getLocation(), EntityType.SHULKER);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_silverfish"))) {
				Entity.spawn(target_player.getLocation(), EntityType.SILVERFISH);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_skeleton"))) {
				Entity.spawn(target_player.getLocation(), EntityType.SKELETON);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_skeleton_horse"))) {
				Entity.spawn(target_player.getLocation(), EntityType.SKELETON_HORSE);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_slime"))) {
				Entity.spawn(target_player.getLocation(), EntityType.SLIME);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_spider"))) {
				Entity.spawn(target_player.getLocation(), EntityType.SPIDER);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_squid"))) {
				Entity.spawn(target_player.getLocation(), EntityType.SQUID);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_stray"))) {
				Entity.spawn(target_player.getLocation(), EntityType.STRAY);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_tropical_fish"))) {
				Entity.spawn(target_player.getLocation(), EntityType.TROPICAL_FISH);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_turtle"))) {
				Entity.spawn(target_player.getLocation(), EntityType.TURTLE);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_vex"))) {
				Entity.spawn(target_player.getLocation(), EntityType.VEX);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_villager"))) {
				Entity.spawn(target_player.getLocation(), EntityType.VILLAGER);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_vindicator"))) {
				Entity.spawn(target_player.getLocation(), EntityType.VINDICATOR);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_wandering_trader"))) {
				Entity.spawn(target_player.getLocation(), EntityType.WANDERING_TRADER);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_witch"))) {
				Entity.spawn(target_player.getLocation(), EntityType.WITCH);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_wolf"))) {
				Entity.spawn(target_player.getLocation(), EntityType.WOLF);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_zombie"))) {
				Entity.spawn(target_player.getLocation(), EntityType.ZOMBIE);
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "spawner_zombie_pigman"))) {
				Entity.spawn(target_player.getLocation(), EntityType.valueOf("PIG_ZOMBIE"));
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}
	}

	public void clicked_money(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player) {
		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "money_back"))) {
				if (p.getName().equals(target_player.getName())) {
					p.openInventory(GUI_Player(p));
				} else {
					p.openInventory(GUI_Players_Settings(p, target_player, target_player.getName()));
				}
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "money_give"))) {
				if (AdminGuiIntegration.getEconomy() != null) {
					p.openInventory(GUI_Money_Amount(p, target_player, 1));
				} else {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "vault_required"));
					p.closeInventory();
				}
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "money_set"))) {
				if (AdminGuiIntegration.getEconomy() != null) {
					p.openInventory(GUI_Money_Amount(p, target_player, 2));
				} else {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "vault_required"));
					p.closeInventory();
				}
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "money_take"))) {
				if (AdminGuiIntegration.getEconomy() != null) {
					p.openInventory(GUI_Money_Amount(p, target_player, 3));
				} else {
					p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "vault_required"));
					p.closeInventory();
				}
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}
	}

	public void clicked_money_amount(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player, int option) {
		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "money_amount_back"))) {
				p.openInventory(GUI_Money(p, target_player));
			} else {
				if (clicked.hasItemMeta()) {
					if (clicked.getItemMeta().hasDisplayName()) {
						String amount = stripNonDigits(clicked.getItemMeta().getDisplayName());
						// Simple number check instead of NumberUtils (optional dependency removed)
						try {
							Double.parseDouble(amount);
							// NumberUtils.isNumber(amount) replacement - if parsing succeeds, it's a number
							if (AdminGuiIntegration.getEconomy() != null) {
								switch (option) {
									case 1:
										EconomyResponse r = AdminGuiIntegration.getEconomy().depositPlayer(target_player, Double.parseDouble(amount));
										if (r.transactionSuccess()) {
											if (p.getName().equals(target_player.getName())) {
												p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_give").replace("{amount}", AdminGuiIntegration.getEconomy().format(r.amount)).replace("{player}", target_player.getName()).replace("{balance}", AdminGuiIntegration.getEconomy().format(r.balance)));
											} else {
												p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_give").replace("{amount}", AdminGuiIntegration.getEconomy().format(r.amount)).replace("{player}", target_player.getName()).replace("{balance}", AdminGuiIntegration.getEconomy().format(r.balance)));
												target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_give").replace("{amount}", AdminGuiIntegration.getEconomy().format(r.amount)).replace("{player}", p.getName()).replace("{balance}", AdminGuiIntegration.getEconomy().format(r.balance)));
											}
										} else {
											p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_transaction_error").replace("{amount}", AdminGuiIntegration.getEconomy().format(r.amount)).replace("{player}", target_player.getName()));
										}
										p.closeInventory();
										break;
									case 3:
										if (AdminGuiIntegration.getEconomy().getBalance(target_player) >= Double.parseDouble(amount)) {
											EconomyResponse s = AdminGuiIntegration.getEconomy().withdrawPlayer(target_player, Double.parseDouble(amount));
											if (s.transactionSuccess()) {
												if (p.getName().equals(target_player.getName())) {
													p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_take").replace("{amount}", AdminGuiIntegration.getEconomy().format(s.amount)).replace("{player}", target_player.getName()).replace("{balance}", AdminGuiIntegration.getEconomy().format(s.balance)));
												} else {
													p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_take").replace("{amount}", AdminGuiIntegration.getEconomy().format(s.amount)).replace("{player}", target_player.getName()).replace("{balance}", AdminGuiIntegration.getEconomy().format(s.balance)));
													target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_take").replace("{amount}", AdminGuiIntegration.getEconomy().format(s.amount)).replace("{player}", p.getName()).replace("{balance}", AdminGuiIntegration.getEconomy().format(s.balance)));
												}
											} else {
												p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_transaction_error").replace("{amount}", AdminGuiIntegration.getEconomy().format(s.amount)).replace("{player}", target_player.getName()));
											}
										} else {
											if (p.getName().equals(target_player.getName())) {
												p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_take_error"));
											} else {
												p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_take_error"));
											}
										}
										p.closeInventory();
										break;
									default:
										double balance = AdminGuiIntegration.getEconomy().getBalance(target_player);
										AdminGuiIntegration.getEconomy().withdrawPlayer(target_player, balance);
										EconomyResponse t = AdminGuiIntegration.getEconomy().depositPlayer(target_player, Double.parseDouble(amount));
										if (t.transactionSuccess()) {
											if (p.getName().equals(target_player.getName())) {
												p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_set").replace("{amount}", AdminGuiIntegration.getEconomy().format(t.amount)).replace("{player}", target_player.getName()).replace("{balance}", AdminGuiIntegration.getEconomy().format(t.balance)));
											} else {
												p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_set").replace("{amount}", AdminGuiIntegration.getEconomy().format(t.amount)).replace("{player}", target_player.getName()).replace("{balance}", AdminGuiIntegration.getEconomy().format(t.balance)));
												target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_target_player_set").replace("{amount}", AdminGuiIntegration.getEconomy().format(t.amount)).replace("{player}", p.getName()).replace("{balance}", AdminGuiIntegration.getEconomy().format(t.balance)));
											}
										} else {
											p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_transaction_error").replace("{amount}", AdminGuiIntegration.getEconomy().format(t.amount)).replace("{player}", target_player.getName()));
										}
										p.closeInventory();
								}
							} else {
								p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "vault_required"));
								p.closeInventory();
							}
						} catch (NumberFormatException e) {
							// Not a valid number
							p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "is_not_a_number").replace("{number}", amount));
							p.closeInventory();
						}
					}
				}
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}
	}

	public void clicked_inventory(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player, boolean left_click) {

		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "inventory_back"))) {
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "inventory_clear"))) {
				target_player.getInventory().clear();
				p.openInventory(GUI_Inventory(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "inventory_refresh"))) {
				p.openInventory(GUI_Inventory(p, target_player));
			} else {
				if (TargetPlayer.hasPermission(p, "admingui.inventory.edit")) {
					if (left_click) {
						target_player.getInventory().addItem(clicked);
					} else {
						if (clicked.getType() == target_player.getInventory().getItem(slot).getType() && clicked.getAmount() == target_player.getInventory().getItem(slot).getAmount()) {
							target_player.getInventory().setItem(slot, null);
						}
					}
					target_player.updateInventory();
					p.openInventory(GUI_Inventory(p, target_player));
				}
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}
	}

	//TODO: Ender Chest
	public void clicked_ender_chest(Player p, int slot, ItemStack clicked, Inventory inv, Player target_player, boolean left_click) {

		if (target_player.isOnline()) {
			if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "inventory_back"))) {
				p.openInventory(GUI_Actions(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "inventory_clear"))) {
				target_player.getEnderChest().clear();
				p.openInventory(GUI_Ender_Chest(p, target_player));
			} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "inventory_refresh"))) {
				p.openInventory(GUI_Ender_Chest(p, target_player));
			} else {
				if (TargetPlayer.hasPermission(p, "admingui.enderchest.edit")) {
					if (left_click) {
						target_player.getEnderChest().addItem(clicked);
					} else {
						if (clicked.getType() == target_player.getEnderChest().getItem(slot).getType() && clicked.getAmount() == target_player.getEnderChest().getItem(slot).getAmount()) {
							target_player.getEnderChest().setItem(slot, null);
						}
					}
					p.openInventory(GUI_Ender_Chest(p, target_player));
				}
			}
		} else {
			p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_not_found"));
			p.closeInventory();
		}
	}
}
