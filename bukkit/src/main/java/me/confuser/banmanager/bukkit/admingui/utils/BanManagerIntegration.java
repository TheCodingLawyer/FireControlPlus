package me.confuser.banmanager.bukkit.admingui.utils;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

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
            String command;
            if (expires == null) {
                // Permanent ban - always use silent command to avoid duplicate messages
                command = "bmsilentban " + targetName + " " + cleanedReason;
            } else {
                // Temporary ban - always use silent command to avoid duplicate messages
                long durationSeconds = (expires.getTime() - System.currentTimeMillis()) / 1000;
                command = "bmsilenttempban " + targetName + " " + durationSeconds + "s " + cleanedReason;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return true;
        } catch (Exception e) {
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
            String command;
            if (expires == null) {
                // Permanent mute - always use silent command to avoid duplicate messages
                command = "bmsilentmute " + targetName + " " + cleanedReason;
            } else {
                // Temporary mute - always use silent command to avoid duplicate messages
                long durationSeconds = (expires.getTime() - System.currentTimeMillis()) / 1000;
                command = "bmsilenttempmute " + targetName + " " + durationSeconds + "s " + cleanedReason;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return true;
        } catch (Exception e) {
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
     */
    public static boolean kickPlayer(String actorUUID, String actorName, String targetUUID, String targetName, String reason) {
        if (!isBanManagerEnabled()) return false;

        try {
            String cleanedReason = cleanReason(reason);
            // Use silent kick command to avoid duplicate messages
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bmsilentkick " + targetName + " " + cleanedReason);
            return true;
        } catch (Exception e) {
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
            String command;
            if (expires == null) {
                // Permanent warning - always use silent command to avoid duplicate messages
                command = "bmsilentwarn " + targetName + " " + cleanedReason;
            } else {
                // Temporary warning - always use silent command to avoid duplicate messages
                long durationSeconds = (expires.getTime() - System.currentTimeMillis()) / 1000;
                command = "bmsilenttempwarn " + targetName + " " + durationSeconds + "s " + cleanedReason;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all banned players - returns null to use existing GUI logic
     * BanManager API access varies by version, so we let the GUI handle this
     */
    public static List<PlayerBanData> getBannedPlayers() {
        // Let the GUI use its existing logic for listing banned players
        return null;
    }

    /**
     * Get all muted players - returns null to use existing GUI logic
     * BanManager API access varies by version, so we let the GUI handle this
     */
    public static List<PlayerMuteData> getMutedPlayers() {
        // Let the GUI use its existing logic for listing muted players
        return null;
    }

    /**
     * Check if a player is banned - simplified version
     * @param uuid The player's UUID
     * @return false (let existing GUI logic handle this)
     */
    public static boolean isBanned(UUID uuid) {
        // Let the GUI use its existing logic for ban checking
        return false;
    }

    /**
     * Check if a player is muted - simplified version
     * @param uuid The player's UUID
     * @return false (let existing GUI logic handle this)
     */
    public static boolean isMuted(UUID uuid) {
        // Let the GUI use its existing logic for mute checking
        return false;
    }

    /**
     * Get player's ban information - simplified version
     * @param uuid The player's UUID
     * @return null (let existing GUI logic handle this)
     */
    public static PlayerBanData getPlayerBan(UUID uuid) {
        // Let the GUI use its existing logic
        return null;
    }

    /**
     * Get player's mute information - simplified version
     * @param uuid The player's UUID
     * @return null (let existing GUI logic handle this)
     */
    public static PlayerMuteData getPlayerMute(UUID uuid) {
        // Let the GUI use its existing logic
        return null;
    }
}
