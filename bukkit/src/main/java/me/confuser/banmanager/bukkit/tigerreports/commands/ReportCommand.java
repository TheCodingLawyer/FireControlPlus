package me.confuser.banmanager.bukkit.tigerreports.commands;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.data.config.ConfigFile;
import me.confuser.banmanager.bukkit.tigerreports.data.config.Message;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import me.confuser.banmanager.bukkit.tigerreports.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Report command for TigerReports integration
 * Adapted from original TigerReports ReportCommand class
 */
public class ReportCommand implements TabExecutor {
    
    private final TigerReportsIntegration integration;
    private final ReportsManager rm;
    private final BanManagerDatabaseAdapter db;
    private final VaultManager vm;
    private final UsersManager um;
    
    public ReportCommand(TigerReportsIntegration integration, ReportsManager rm, BanManagerDatabaseAdapter db,
                        VaultManager vm, UsersManager um) {
        this.integration = integration;
        this.rm = rm;
        this.db = db;
        this.vm = vm;
        this.um = um;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check permission
        if (!Permission.REPORT.check(player)) {
            player.sendMessage(Message.PERMISSION_COMMAND.get());
            return true;
        }
        
        FileConfiguration config = ConfigFile.CONFIG.get();
        if (args.length == 0 || (args.length == 1 && !ConfigUtils.exists(config, "Config.DefaultReasons.Reason1"))) {
            player.sendMessage(Message.INVALID_SYNTAX_REPORT.get());
            return true;
        }
        
        User user = um.getOnlineUser(player);
        if (user == null) {
            integration.getLogger().warning("User " + player.getName() + " not found in UsersManager");
            return true;
        }
        
        // Check cooldown
        user.getCooldownAsynchronously(db, integration, (cooldown) -> {
            if (cooldown != null) {
                user.sendErrorMessage(Message.COOLDOWN.get().replace("_Time_", cooldown));
                return;
            }
            
            String reportedName = args[0];
            String reason;
            
            if (args.length == 1) {
                // Open reason selection GUI if only player name provided
                // For now, we'll require a reason
                user.sendErrorMessage(Message.INVALID_SYNTAX_REPORT.get());
                return;
            } else {
                // Build reason from arguments
                StringBuilder reasonBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    reasonBuilder.append(args[i]);
                    if (i < args.length - 1) {
                        reasonBuilder.append(" ");
                    }
                }
                reason = reasonBuilder.toString();
            }
            
            // Validate reason length
            int minCharacters = ConfigUtils.getInt("Config.MinCharacters", 3);
            if (reason.length() < minCharacters) {
                user.sendErrorMessage("Reason must be at least " + minCharacters + " characters long.");
                return;
            }
            
            // Find reported player
            Player reportedPlayer = Bukkit.getPlayer(reportedName);
            if (reportedPlayer == null) {
                // Check if we should allow reporting offline players
                boolean reportOnline = ConfigUtils.isEnabled("Config.ReportOnline");
                if (reportOnline) {
                    user.sendErrorMessage(Message.PLAYER_NOT_FOUND.get());
                    return;
                }
                // For offline players, we'd need to look up their UUID from BanManager's database
                user.sendErrorMessage("Reporting offline players is not yet implemented.");
                return;
            }
            
            // Check if player is trying to report themselves
            if (reportedPlayer.equals(player)) {
                // Allow self-reporting if in BanManager admin mode
                if (!me.confuser.banmanager.common.commands.ReportAdminCommand.isInAdminReportMode(player.getUniqueId())) {
                    user.sendErrorMessage("You cannot report yourself. Use /reportadmin to enable admin report mode for testing.");
                    return;
                } else {
                    // Notify that admin mode is being used
                    user.sendMessage("§e[Admin Mode] §7Self-reporting enabled for testing purposes.");
                }
            }
            
            // Check if reported player has immunity
            if (Permission.REPORT_EXEMPT.check(reportedPlayer)) {
                // Allow reporting immune players if in BanManager admin mode
                if (!me.confuser.banmanager.common.commands.ReportAdminCommand.isInAdminReportMode(player.getUniqueId())) {
                    user.sendErrorMessage("This player cannot be reported. Use /reportadmin to enable admin report mode for testing.");
                    return;
                } else {
                    // Notify that admin mode is bypassing immunity
                    user.sendMessage("§e[Admin Mode] §7Bypassing report immunity for testing purposes.");
                }
            }
            
            // Create the report
            UUID reportedUuid = reportedPlayer.getUniqueId();
            UUID reporterUuid = player.getUniqueId();
            
            db.createReport(reportedUuid, reporterUuid, reason, "", "", (reportId) -> {
                if (reportId > 0) {
                    integration.runTask(() -> {
                        user.sendMessage(Message.REPORT_SENT.get());
                        
                        // Notify staff
                        String notifyMessage = Message.REPORT_NOTIFY.get()
                            .replace("_Reporter_", player.getName())
                            .replace("_Reported_", reportedPlayer.getName())
                            .replace("_Reason_", reason);
                        
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            if (Permission.STAFF.check(onlinePlayer)) {
                                onlinePlayer.sendMessage(notifyMessage);
                            }
                        }
                    });
                } else {
                    integration.runTask(() -> {
                        user.sendErrorMessage("Failed to create report. Please try again.");
                    });
                }
            });
        });
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Complete player names
            String partial = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (StringUtil.startsWithIgnoreCase(player.getName(), partial)) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}
