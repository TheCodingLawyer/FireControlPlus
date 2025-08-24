package me.confuser.banmanager.bukkit.tigerreports.data.constants;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Permission constants for TigerReports integration
 * Adapted from original TigerReports Permission enum
 */
public enum Permission {
    
    REPORT("tigerreports.report"),
    STAFF("tigerreports.staff"),
    STAFF_ADVANCED("tigerreports.staff.advanced"),
    STAFF_DELETE("tigerreports.staff.delete"),
    STAFF_ARCHIVE("tigerreports.staff.archive"),
    STAFF_ARCHIVE_AUTO("tigerreports.staff.archive.auto"),
    STAFF_TELEPORT("tigerreports.staff.teleport"),
    MANAGE("tigerreports.manage"),
    REPORT_EXEMPT("tigerreports.report.exempt");
    
    private final String permission;
    
    Permission(String permission) {
        this.permission = permission;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public boolean check(CommandSender sender) {
        return sender.hasPermission(permission) || sender.hasPermission("tigerreports.*");
    }
    
    public boolean check(Player player) {
        return player.hasPermission(permission) || player.hasPermission("tigerreports.*");
    }
}
