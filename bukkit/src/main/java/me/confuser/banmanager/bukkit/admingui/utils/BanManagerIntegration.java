package me.confuser.banmanager.bukkit.admingui.utils;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Enhanced BanManager integration utility class
 * Uses BanManager commands for compatibility across different BanManager versions
 * Provides fallback command-based integration that works reliably
 */
public class BanManagerIntegration {

    private static BanManagerPlugin getBanManagerPlugin() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BanManager");
        if (plugin instanceof BMBukkitPlugin) {
            return ((BMBukkitPlugin) plugin).getPlugin();
        }
        return null;
    }

    /**
     * Clean punishment reason by removing color codes and formatting
     * @param reason The raw reason that may contain color codes
     * @return Clean reason without color codes
     */
    private static String cleanReason(String reason) {
        if (reason == null) return "";
        // Strip color codes and trim whitespace
        return ChatColor.stripColor(reason).trim();
    }

    /**
     * Check if BanManager is available and enabled
     */
    public static boolean isBanManagerEnabled() {
        return getBanManagerPlugin() != null;
    }

    private static org.bukkit.command.CommandSender resolveSender(String actorUUID, String actorName) {
        try {
            if (actorUUID != null) {
                UUID uuid = UUID.fromString(actorUUID);
                org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
                if (p != null) return p;
            }
            if (actorName != null) {
                org.bukkit.entity.Player p = Bukkit.getPlayerExact(actorName);
                if (p != null) return p;
            }
        } catch (Exception ignored) {
        }
        return Bukkit.getConsoleSender();
    }

    /**
     * Ban a player using BanManager commands
     * @param actorUUID The UUID of the admin performing the ban
     * @param actorName The name of the admin performing the ban
     * @param targetUUID The UUID of the player to ban
     * @param targetName The name of the player to ban
     * @param reason The ban reason
     * @param expires The expiration date (null for permanent)
     * @param silent Whether the ban should be silent
     */
    public static boolean banPlayer(String actorUUID, String actorName, String targetUUID, String targetName, String reason, Date expires, boolean silent) {
        if (!isBanManagerEnabled()) return false;

        try {
            String cleanedReason = cleanReason(reason);
            String silentFlag = silent ? "-s " : "";
            String command;
            if (expires == null) {
                // Permanent ban: ban <player> [-s] <reason>
                command = "ban " + targetName + " " + silentFlag + cleanedReason;
            } else {
                // Temporary ban: tempban <player> <time> [-s] <reason>
                long durationSeconds = (expires.getTime() - System.currentTimeMillis()) / 1000;
                command = "tempban " + targetName + " " + durationSeconds + "s " + silentFlag + cleanedReason;
            }
            Bukkit.getLogger().info("[AdminGUI] Executing: " + command);
            Bukkit.dispatchCommand(resolveSender(actorUUID, actorName), command);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().severe("[AdminGUI] Ban command failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Unban a player using BanManager commands
     * @param targetName The name of the player to unban
     */
    public static boolean unbanPlayer(String targetName) {
        if (!isBanManagerEnabled()) return false;

        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "unban " + targetName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mute a player using BanManager commands
     * @param actorUUID The UUID of the admin performing the mute
     * @param actorName The name of the admin performing the mute
     * @param targetUUID The UUID of the player to mute
     * @param targetName The name of the player to mute
     * @param reason The mute reason
     * @param expires The expiration date (null for permanent)
     * @param silent Whether the mute should be silent
     */
    public static boolean mutePlayer(String actorUUID, String actorName, String targetUUID, String targetName, String reason, Date expires, boolean silent) {
        if (!isBanManagerEnabled()) return false;

        try {
            String cleanedReason = cleanReason(reason);
            String silentFlag = silent ? "-s " : "";
            String command;
            if (expires == null) {
                // Permanent mute: mute <player> [-s] <reason>
                command = "mute " + targetName + " " + silentFlag + cleanedReason;
            } else {
                // Temporary mute: tempmute <player> <time> [-s] <reason>
                long durationSeconds = (expires.getTime() - System.currentTimeMillis()) / 1000;
                command = "tempmute " + targetName + " " + durationSeconds + "s " + silentFlag + cleanedReason;
            }
            Bukkit.getLogger().info("[AdminGUI] Executing: " + command);
            Bukkit.dispatchCommand(resolveSender(actorUUID, actorName), command);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().severe("[AdminGUI] Mute command failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Unmute a player using BanManager commands
     * @param targetName The name of the player to unmute
     */
    public static boolean unmutePlayer(String targetName) {
        if (!isBanManagerEnabled()) return false;

        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "unmute " + targetName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kick a player using BanManager commands
     * @param actorUUID The UUID of the admin performing the kick
     * @param actorName The name of the admin performing the kick
     * @param targetUUID The UUID of the player to kick
     * @param targetName The name of the player to kick
     * @param reason The kick reason
     * @param silent Whether the kick should be silent
     */
    public static boolean kickPlayer(String actorUUID, String actorName, String targetUUID, String targetName, String reason, boolean silent) {
        if (!isBanManagerEnabled()) return false;

        try {
            String cleanedReason = cleanReason(reason);
            String silentFlag = silent ? "-s " : "";
            // kick <player> [-s] <reason>
            String command = "kick " + targetName + " " + silentFlag + cleanedReason;
            Bukkit.getLogger().info("[AdminGUI] Executing: " + command);
            Bukkit.dispatchCommand(resolveSender(actorUUID, actorName), command);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().severe("[AdminGUI] Kick command failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Warn a player using BanManager commands
     * @param actorUUID The UUID of the admin performing the warn
     * @param actorName The name of the admin performing the warn
     * @param targetUUID The UUID of the player to warn
     * @param targetName The name of the player to warn
     * @param reason The warn reason
     * @param expires The expiration date (null for permanent)
     * @param silent Whether the warn should be silent
     */
    public static boolean warnPlayer(String actorUUID, String actorName, String targetUUID, String targetName, String reason, Date expires, boolean silent) {
        if (!isBanManagerEnabled()) return false;

        try {
            String cleanedReason = cleanReason(reason);
            String silentFlag = silent ? "-s " : "";
            String command;
            if (expires == null) {
                // Permanent warning: warn <player> [-s] <reason>
                command = "warn " + targetName + " " + silentFlag + cleanedReason;
            } else {
                // Temporary warning: tempwarn <player> <time> [-s] <reason>
                long durationSeconds = (expires.getTime() - System.currentTimeMillis()) / 1000;
                command = "tempwarn " + targetName + " " + durationSeconds + "s " + silentFlag + cleanedReason;
            }
            Bukkit.getLogger().info("[AdminGUI] Executing: " + command);
            Bukkit.dispatchCommand(resolveSender(actorUUID, actorName), command);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().severe("[AdminGUI] Warn command failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all banned players from BanManager's database
     * @return List of all active bans, or empty list if none
     */
    public static List<PlayerBanData> getBannedPlayers() {
        BanManagerPlugin plugin = getBanManagerPlugin();
        if (plugin == null) return new ArrayList<>();
        
        try {
            Collection<PlayerBanData> bans = plugin.getPlayerBanStorage().getBans().values();
            return new ArrayList<>(bans);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[AdminGUI] Failed to get banned players: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all muted players from BanManager's database
     * @return List of all active mutes, or empty list if none
     */
    public static List<PlayerMuteData> getMutedPlayers() {
        BanManagerPlugin plugin = getBanManagerPlugin();
        if (plugin == null) return new ArrayList<>();
        
        try {
            Collection<PlayerMuteData> mutes = plugin.getPlayerMuteStorage().getMutes().values();
            return new ArrayList<>(mutes);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[AdminGUI] Failed to get muted players: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Check if a player is banned in BanManager
     * @param uuid The player's UUID
     * @return true if banned, false otherwise
     */
    public static boolean isBanned(UUID uuid) {
        BanManagerPlugin plugin = getBanManagerPlugin();
        if (plugin == null) return false;
        
        try {
            return plugin.getPlayerBanStorage().isBanned(uuid);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a player is muted in BanManager
     * @param uuid The player's UUID
     * @return true if muted, false otherwise
     */
    public static boolean isMuted(UUID uuid) {
        BanManagerPlugin plugin = getBanManagerPlugin();
        if (plugin == null) return false;
        
        try {
            return plugin.getPlayerMuteStorage().isMuted(uuid);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get player's ban information from BanManager
     * @param uuid The player's UUID
     * @return PlayerBanData or null if not banned
     */
    public static PlayerBanData getPlayerBan(UUID uuid) {
        BanManagerPlugin plugin = getBanManagerPlugin();
        if (plugin == null) return null;
        
        try {
            return plugin.getPlayerBanStorage().getBan(uuid);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get player's mute information from BanManager
     * @param uuid The player's UUID
     * @return PlayerMuteData or null if not muted
     */
    public static PlayerMuteData getPlayerMute(UUID uuid) {
        BanManagerPlugin plugin = getBanManagerPlugin();
        if (plugin == null) return null;
        
        try {
            return plugin.getPlayerMuteStorage().getMute(uuid);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get a list of banned player names for the AdminGUI
     * @return List of banned player names
     */
    public static List<String> getBannedPlayerNames() {
        List<String> names = new ArrayList<>();
        for (PlayerBanData ban : getBannedPlayers()) {
            if (ban.getPlayer() != null && ban.getPlayer().getName() != null) {
                names.add(ban.getPlayer().getName());
            }
        }
        return names;
    }
    
    /**
     * Get a list of muted player names for the AdminGUI
     * @return List of muted player names
     */
    public static List<String> getMutedPlayerNames() {
        List<String> names = new ArrayList<>();
        for (PlayerMuteData mute : getMutedPlayers()) {
            if (mute.getPlayer() != null && mute.getPlayer().getName() != null) {
                names.add(mute.getPlayer().getName());
            }
        }
        return names;
    }
    
    /**
     * Get ban info for display in AdminGUI
     * @param playerName The player's name
     * @return BannedPlayerIn object with ban details, or null if not found
     */
    public static BannedPlayerIn getBanInfo(String playerName) {
        for (PlayerBanData ban : getBannedPlayers()) {
            if (ban.getPlayer() != null && ban.getPlayer().getName() != null 
                && ban.getPlayer().getName().equalsIgnoreCase(playerName)) {
                
                Date until = ban.getExpires() == 0 ? null : new Date(ban.getExpires() * 1000L);
                Date created = new Date(ban.getCreated() * 1000L);
                
                return new BannedPlayerIn(
                    ban.getActor().getUUID().toString(),
                    ban.getActor().getName(),
                    ban.getPlayer().getUUID().toString(),
                    ban.getPlayer().getName(),
                    ban.getReason(),
                    until,
                    "local",
                    created
                );
            }
        }
        return null;
    }
    
    /**
     * Get mute info for display in AdminGUI
     * @param playerName The player's name
     * @return MutedPlayerIn object with mute details, or null if not found
     */
    public static MutedPlayerIn getMuteInfo(String playerName) {
        for (PlayerMuteData mute : getMutedPlayers()) {
            if (mute.getPlayer() != null && mute.getPlayer().getName() != null 
                && mute.getPlayer().getName().equalsIgnoreCase(playerName)) {
                
                Date until = mute.getExpires() == 0 ? null : new Date(mute.getExpires() * 1000L);
                Date created = new Date(mute.getCreated() * 1000L);
                
                return new MutedPlayerIn(
                    mute.getActor().getUUID().toString(),
                    mute.getActor().getName(),
                    mute.getPlayer().getUUID().toString(),
                    mute.getPlayer().getName(),
                    mute.getReason(),
                    until,
                    "local",
                    created
                );
            }
        }
        return null;
    }
}
