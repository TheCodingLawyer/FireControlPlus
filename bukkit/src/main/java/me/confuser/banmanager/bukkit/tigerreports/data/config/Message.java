package me.confuser.banmanager.bukkit.tigerreports.data.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Message configuration enum for TigerReports integration
 * Adapted from original TigerReports Message enum
 */
public enum Message {
    
    // Basic messages
    INVALID_SYNTAX_REPORT("Messages.InvalidSyntaxReport", "&cUsage: /report <player> [reason]"),
    COOLDOWN("Messages.Cooldown", "&cYou must wait _Time_ before reporting again."),
    PERMISSION_COMMAND("Messages.PermissionCommand", "&cYou don't have permission to use this command."),
    
    // Report messages
    REPORT_SENT("Messages.ReportSent", "&aYour report has been sent successfully."),
    REPORT_NOTIFY("Messages.ReportNotify", "&6[Report] &e_Reporter_ &7reported &e_Reported_ &7for: &f_Reason_"),
    
    // Menu titles
    REPORTS_TITLE("Messages.ReportsTitle", "Reports"),
    REPORT_TITLE("Messages.ReportTitle", "Report #_Id_"),
    ARCHIVED_REPORTS_TITLE("Messages.ArchivedReportsTitle", "Archived Reports"),
    
    // Menu items
    ARCHIVED_REPORTS_DETAILS("Messages.ArchivedReportsDetails", "&7Click to view archived reports"),
    REPORT_SHOW_ACTION("Messages.ReportShowAction", "&aClick to view report"),
    REPORT_DELETE_ACTION("Messages.ReportDeleteAction", "&cShift+Click to delete"),
    
    // Status messages
    PLAYER_NOT_FOUND("Messages.PlayerNotFound", "&cPlayer not found."),
    REPORT_NOT_FOUND("Messages.ReportNotFound", "&cReport not found."),
    
    // Processing messages
    REPORT_PROCESSED("Messages.ReportProcessed", "&aReport has been processed."),
    REPORT_ARCHIVED("Messages.ReportArchived", "&aReport has been archived."),
    REPORT_DELETED("Messages.ReportDeleted", "&aReport has been deleted.");
    
    private final String path;
    private final String defaultValue;
    
    Message(String path, String defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }
    
    public String get() {
        FileConfiguration config = ConfigFile.MESSAGES.get();
        if (config != null) {
            String message = config.getString(path, defaultValue);
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        return ChatColor.translateAlternateColorCodes('&', defaultValue);
    }
    
    public String get(String... replacements) {
        String message = get();
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }
}
