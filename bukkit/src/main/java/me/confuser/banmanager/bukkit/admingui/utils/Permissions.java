package me.confuser.banmanager.bukkit.admingui.utils;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Permissions {

	public static HashMap<UUID, String> cache_ranks = new HashMap<>();
	
	// Simple replacement for Database functionality
	public static class Database {
		public static SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		public static String cacheRank(UUID uuid) {
			// Simplified - just return cached rank or default
			return cache_ranks.getOrDefault(uuid, "default");
		}
		
		public static void setRank(UUID uuid, String name, String rank) {
			// Simplified - just cache the rank
			cache_ranks.put(uuid, rank);
		}
		
		public static boolean rankNeedFix(String playerName) {
			// Simplified - no rank fixing needed
			return false;
		}
		
		public static void fixRank(UUID uuid, String playerName) {
			// Simplified - no-op
		}
	}

	public static String getPrefix(UUID uuid) {
		String rank;

		if (AdminGuiIntegration.getInstance().getConf().getBoolean("mysql", false) && AdminGuiIntegration.getInstance().getConf().getInt("ap_storage_type", 0) == 2) {
			if (cache_ranks.getOrDefault(uuid, null) != null) {
				rank = cache_ranks.get(uuid);
			} else {
				rank = Database.cacheRank(uuid);
			}
		} else {
			rank = AdminGuiIntegration.getInstance().getPlayers().getString(uuid + ".rank", null);
		}

		if (rank == null) {
			return AdminGuiIntegration.getInstance().getPermissions().getString("groups.default.prefix", "");
		} else {
			return AdminGuiIntegration.getInstance().getPermissions().getString("groups." + rank + ".prefix", "");
		}
	}

	public static String getSuffix(UUID uuid) {
		String rank;

		if (AdminGuiIntegration.getInstance().getConf().getBoolean("mysql", false) && AdminGuiIntegration.getInstance().getConf().getInt("ap_storage_type", 0) == 2) {
			if (cache_ranks.getOrDefault(uuid, null) != null) {
				rank = cache_ranks.get(uuid);
			} else {
				rank = Database.cacheRank(uuid);
			}
		} else {
			rank = AdminGuiIntegration.getInstance().getPlayers().getString(uuid + ".rank", null);
		}

		if (rank == null) {
			return AdminGuiIntegration.getInstance().getPermissions().getString("groups.default.suffix", "");
		} else {
			return AdminGuiIntegration.getInstance().getPermissions().getString("groups." + rank + ".suffix", "");
		}
	}

	public static String getRank(UUID uuid, String name) {
		String rank = null;

		if (AdminGuiIntegration.getInstance().getConf().getBoolean("mysql", false) && AdminGuiIntegration.getInstance().getConf().getInt("ap_storage_type", 0) == 2) {

			if (cache_ranks.getOrDefault(uuid, null) != null) {
				rank = cache_ranks.get(uuid);
			} else {
				rank = Database.cacheRank(uuid);
			}

		} else {
			if (uuid != null) {
				rank = AdminGuiIntegration.getInstance().getPlayers().getString(uuid + ".rank", null);
			} else {
				Set<String> con_sec = AdminGuiIntegration.getInstance().getPlayers().getConfigurationSection("").getKeys(false);
				for (String uuid_name : con_sec) {
					if (AdminGuiIntegration.getInstance().getPlayers().getString(uuid_name + ".name").equals(name)) {
						rank = AdminGuiIntegration.getInstance().getPlayers().getString(uuid_name + ".rank", null);
						break;
					}
				}
			}
		}

		if (rank == null) {
			return "default";
		} else {
			return rank;
		}
	}

	public static boolean setPermission(UUID uuid, String name, String permission){
		if(uuid != null){
			List<String> permissions = AdminGuiIntegration.getInstance().getPlayers().getStringList(uuid + ".permissions");
			if(permissions.contains(permission)) return true;
			permissions.add(permission);
			AdminGuiIntegration.getInstance().getPlayers().set(uuid + ".permissions", permissions);
			AdminGuiIntegration.getInstance().getPlayers().set(uuid + ".name", name);
			AdminGuiIntegration.getInstance().savePlayers();

			Player target_player = Bukkit.getPlayer(uuid);
			if (target_player != null && target_player.isOnline()) {
				TargetPlayer.refreshPermissions(target_player);
			}
			return true;
		}

		boolean changed = false;
		Set<String> con_sec = AdminGuiIntegration.getInstance().getPlayers().getConfigurationSection("").getKeys(false);
		for (String uuid_name : con_sec) {
			if (AdminGuiIntegration.getInstance().getPlayers().getString(uuid_name + ".name").equals(name)) {
				List<String> permissions = AdminGuiIntegration.getInstance().getPlayers().getStringList(uuid_name + ".permissions");
				if(permissions.contains(permission)) return true;
				permissions.add(permission);
				AdminGuiIntegration.getInstance().getPlayers().set(uuid_name + ".permissions", permissions);
				AdminGuiIntegration.getInstance().getPlayers().set(uuid_name + ".name", name);
				changed = true;
				break;
			}
		}

		if(!changed){
			List<String> permissions = AdminGuiIntegration.getInstance().getPlayers().getStringList(name + ".permissions");
			if(permissions.contains(permission)) return true;
			permissions.add(permission);
			AdminGuiIntegration.getInstance().getPlayers().set(name + ".name", name);
			AdminGuiIntegration.getInstance().getPlayers().set(name + ".permissions", permissions);
		}

		AdminGuiIntegration.getInstance().savePlayers();

		Player target_player = Bukkit.getPlayer(name);
		if (target_player != null && target_player.isOnline()) {
			TargetPlayer.refreshPermissions(target_player);
		}
		return true;
	}

	public static boolean removePermission(UUID uuid, String name, String permission){
		if(uuid != null){
			List<String> permissions = AdminGuiIntegration.getInstance().getPlayers().getStringList(uuid + ".permissions");
			if(!permissions.contains(permission)) return true;
			permissions.remove(permission);
			AdminGuiIntegration.getInstance().getPlayers().set(uuid + ".permissions", permissions);
			AdminGuiIntegration.getInstance().getPlayers().set(uuid + ".name", name);
			AdminGuiIntegration.getInstance().savePlayers();

			Player target_player = Bukkit.getPlayer(uuid);
			if (target_player != null && target_player.isOnline()) {
				TargetPlayer.refreshPermissions(target_player);
			}
			return true;
		}

		boolean changed = false;
		Set<String> con_sec = AdminGuiIntegration.getInstance().getPlayers().getConfigurationSection("").getKeys(false);
		for (String uuid_name : con_sec) {
			if (AdminGuiIntegration.getInstance().getPlayers().getString(uuid_name + ".name").equals(name)) {
				List<String> permissions = AdminGuiIntegration.getInstance().getPlayers().getStringList(uuid_name + ".permissions");
				if(!permissions.contains(permission)) return true;
				permissions.remove(permission);
				AdminGuiIntegration.getInstance().getPlayers().set(uuid_name + ".permissions", permissions);
				AdminGuiIntegration.getInstance().getPlayers().set(uuid_name + ".name", name);
				changed = true;
				break;
			}
		}

		if(!changed){
			List<String> permissions = AdminGuiIntegration.getInstance().getPlayers().getStringList(name + ".permissions");
			if(!permissions.contains(permission)) return true;
			permissions.remove(permission);
			AdminGuiIntegration.getInstance().getPlayers().set(name + ".name", name);
			AdminGuiIntegration.getInstance().getPlayers().set(name + ".permissions", permissions);
		}

		AdminGuiIntegration.getInstance().savePlayers();

		Player target_player = Bukkit.getPlayer(name);
		if (target_player != null && target_player.isOnline()) {
			TargetPlayer.refreshPermissions(target_player);
		}
		return true;
	}

	public static boolean setRank(UUID uuid, String name, String rank) {
		if (AdminGuiIntegration.getInstance().getPermissions().getString("groups." + rank + ".prefix") != null) {
			if (AdminGuiIntegration.getInstance().getConf().getBoolean("mysql", false) && AdminGuiIntegration.getInstance().getConf().getInt("ap_storage_type", 0) == 2) {
				Database.setRank(uuid, name, rank);
				if (uuid != null) {
					Database.cacheRank(uuid);
					//Vault
					if (AdminGuiIntegration.getVaultChat() != null) {
						AdminGuiIntegration.getVaultChat().setPlayerPrefix(Bukkit.getPlayer(uuid), getPrefix(uuid));
						AdminGuiIntegration.getVaultChat().setPlayerSuffix(Bukkit.getPlayer(uuid), getSuffix(uuid));
					}
				}
			} else {
				if (uuid != null) {
					if (rank.equals("default")) {
						AdminGuiIntegration.getInstance().getPlayers().set(uuid + ".rank", null);
					} else {
						AdminGuiIntegration.getInstance().getPlayers().set(uuid + ".name", name);
						AdminGuiIntegration.getInstance().getPlayers().set(uuid + ".rank", rank);
					}

					if (AdminGuiIntegration.getInstance().getConf().getBoolean("bungeecord_enabled", false) && AdminGuiIntegration.getInstance().getConf().getInt("ap_storage_type", 0) == 1) {
						Channel.send("Console", "rank", uuid.toString(), name, rank);
					}

					//Vault
					if (AdminGuiIntegration.getVaultChat() != null) {
						AdminGuiIntegration.getVaultChat().setPlayerPrefix(Bukkit.getPlayer(uuid), getPrefix(uuid));
						AdminGuiIntegration.getVaultChat().setPlayerSuffix(Bukkit.getPlayer(uuid), getSuffix(uuid));
					}

				} else {
					boolean changed = false;
					Set<String> con_sec = AdminGuiIntegration.getInstance().getPlayers().getConfigurationSection("").getKeys(false);
					for (String uuid_name : con_sec) {
						if (AdminGuiIntegration.getInstance().getPlayers().getString(uuid_name + ".name").equals(name)) {
							AdminGuiIntegration.getInstance().getPlayers().set(uuid_name + ".name", name);
							AdminGuiIntegration.getInstance().getPlayers().set(uuid_name + ".rank", rank);
							changed = true;
							break;
						}
					}
					if (!changed) {
						AdminGuiIntegration.getInstance().getPlayers().set(name + ".name", name);
						AdminGuiIntegration.getInstance().getPlayers().set(name + ".rank", rank);
					}

					if (AdminGuiIntegration.getInstance().getConf().getBoolean("bungeecord_enabled", false) && AdminGuiIntegration.getInstance().getConf().getInt("ap_storage_type", 0) == 1) {
						Channel.send("Console", "rank", "null", name, rank);
					}
				}
				AdminGuiIntegration.getInstance().savePlayers();
			}
			return true;
		}
		return false;
	}

	public static boolean saveRank(UUID uuid, String name, String rank) {
		if (AdminGuiIntegration.getInstance().getPermissions().getString("groups." + rank + ".prefix") != null) {
			Player target_player;
			if (uuid != null) {
				if (rank.equals("default")) {
					AdminGuiIntegration.getInstance().getPlayers().set(uuid + ".rank", null);
				} else {
					AdminGuiIntegration.getInstance().getPlayers().set(uuid + ".name", name);
					AdminGuiIntegration.getInstance().getPlayers().set(uuid + ".rank", rank);
				}
				target_player = Bukkit.getPlayer(uuid);
			} else {
				target_player = Bukkit.getPlayer(name);
				if (target_player != null && target_player.isOnline()) {
					if (rank.equals("default")) {
						AdminGuiIntegration.getInstance().getPlayers().set(target_player.getUniqueId() + ".rank", null);
					} else {
						AdminGuiIntegration.getInstance().getPlayers().set(target_player.getUniqueId() + ".name", name);
						AdminGuiIntegration.getInstance().getPlayers().set(target_player.getUniqueId() + ".rank", rank);
					}
				} else {
					AdminGuiIntegration.getInstance().getPlayers().set(name + ".name", name);
					AdminGuiIntegration.getInstance().getPlayers().set(name + ".rank", rank);
				}
			}
			AdminGuiIntegration.getInstance().savePlayers();

			if (target_player != null && target_player.isOnline()) {
				TargetPlayer.refreshPermissions(target_player);
				TargetPlayer.refreshPlayerTabList(target_player);
			}

			return true;
		}
		return false;
	}
}
