package me.confuser.banmanager.bukkit.tigerreports.commands;

import me.confuser.banmanager.bukkit.tigerreports.BanManagerDatabaseAdapter;
import me.confuser.banmanager.bukkit.tigerreports.TigerReportsIntegration;
import me.confuser.banmanager.bukkit.tigerreports.data.config.Message;
import me.confuser.banmanager.bukkit.tigerreports.data.constants.Permission;
import me.confuser.banmanager.bukkit.tigerreports.managers.ReportsManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.UsersManager;
import me.confuser.banmanager.bukkit.tigerreports.managers.VaultManager;
import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reports command for TigerReports integration
 * Adapted from original TigerReports ReportsCommand class
 */
public class ReportsCommand implements TabExecutor {
    
    private static final List<String> ACTIONS = Arrays.asList(
        "reload",
        "notify", 
        "archive",
        "delete",
        "comment",
        "archives",
        "archiveall",
        "deleteall",
        "user",
        "stopcooldown",
        "punish",
        "help"
    );
    
    private final ReportsManager rm;
    private final BanManagerDatabaseAdapter db;
    private final TigerReportsIntegration integration;
    private final VaultManager vm;
    private final UsersManager um;
    
    public ReportsCommand(ReportsManager rm, BanManagerDatabaseAdapter db, TigerReportsIntegration integration,
                         VaultManager vm, UsersManager um) {
        this.rm = rm;
        this.db = db;
        this.integration = integration;
        this.vm = vm;
        this.um = um;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Open reports GUI if no arguments
            User user = checkStaffAction(sender);
            if (user == null) {
                return true;
            }
            user.openReportsMenu(1, true, rm, db, integration, vm, null, um);
            return true;
        }
        
        if (args.length == 1) {
            try {
                // Try to parse as report ID
                int reportId = Integer.parseInt(args[0].replace("#", ""));
                User user = checkStaffAction(sender);
                if (user == null) {
                    return true;
                }
                
                if (reportId >= 0) {
                    // Open specific report
                    rm.getReportByIdAsynchronously(reportId, false, true, db, integration, um, (report) -> {
                        if (report != null) {
                            integration.runTask(() -> {
                                user.sendMessage("Report #" + reportId + " - " + report.getReportedName() + 
                                               " reported by " + report.getReporterName() + " for: " + report.getReason());
                            });
                        } else {
                            integration.runTask(() -> {
                                user.sendErrorMessage(Message.REPORT_NOT_FOUND.get());
                            });
                        }
                    });
                }
                return true;
            } catch (NumberFormatException e) {
                // Not a number, check for actions
                String action = args[0].toLowerCase();
                return handleAction(sender, action, args);
            }
        }
        
        // Handle multi-argument commands
        if (args.length > 1) {
            String action = args[0].toLowerCase();
            return handleAction(sender, action, args);
        }
        
        return true;
    }
    
    private boolean handleAction(CommandSender sender, String action, String[] args) {
        User user = checkStaffAction(sender);
        if (user == null) {
            return true;
        }
        
        switch (action) {
            case "reload":
                if (!Permission.MANAGE.check(sender)) {
                    sender.sendMessage(Message.PERMISSION_COMMAND.get());
                    return true;
                }
                // Reload configuration
                integration.disable();
                integration.enable();
                sender.sendMessage("§aTigerReports integration reloaded.");
                break;
                
            case "archives":
                user.openArchivedReportsMenu(1, true, rm, db, integration, vm, null, um);
                break;
                
            case "notify":
                // Toggle notifications for this user
                sender.sendMessage("§eNotification toggle not yet implemented.");
                break;
                
            case "archive":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /reports archive <id>");
                    return true;
                }
                handleArchiveCommand(user, args[1]);
                break;
                
            case "delete":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /reports delete <id>");
                    return true;
                }
                handleDeleteCommand(user, args[1]);
                break;
                
            case "comment":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /reports comment <id> [message]");
                    return true;
                }
                handleCommentCommand(user, args);
                break;
                
            case "user":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /reports user <player>");
                    return true;
                }
                handleUserCommand(user, args[1]);
                break;
                
            case "punish":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /reports punish <player>");
                    return true;
                }
                handlePunishCommand(user, args[1]);
                break;
                
            case "stopcooldown":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /reports stopcooldown <player>");
                    return true;
                }
                handleStopCooldownCommand(user, args[1]);
                break;
                
            case "archiveall":
                if (!Permission.MANAGE.check(sender)) {
                    sender.sendMessage(Message.PERMISSION_COMMAND.get());
                    return true;
                }
                handleArchiveAllCommand(user, args);
                break;
                
            case "deleteall":
                if (!Permission.MANAGE.check(sender)) {
                    sender.sendMessage(Message.PERMISSION_COMMAND.get());
                    return true;
                }
                handleDeleteAllCommand(user, args);
                break;
                
            case "help":
                handleHelpCommand(sender);
                break;
                
            default:
                sender.sendMessage("§cUnknown action: " + action + ". Use /reports help for available commands.");
                break;
        }
        
        return true;
    }
    
    private void handleArchiveCommand(User user, String reportIdStr) {
        try {
            int reportId = Integer.parseInt(reportIdStr.replace("#", ""));
            rm.getReportByIdAsynchronously(reportId, false, true, db, integration, um, (report) -> {
                if (report != null) {
                    integration.runTask(() -> {
                        if (user.canArchive(report)) {
                            report.setStatus("Closed", user, db, rm, integration);
                            user.sendMessage("§aReport #" + reportId + " has been archived.");
                        } else {
                            user.sendErrorMessage("§cYou cannot archive this report.");
                        }
                    });
                } else {
                    integration.runTask(() -> {
                        user.sendErrorMessage("§cReport #" + reportId + " not found.");
                    });
                }
            });
        } catch (NumberFormatException e) {
            user.sendErrorMessage("§cInvalid report ID: " + reportIdStr);
        }
    }
    
    private void handleDeleteCommand(User user, String reportIdStr) {
        if (!user.hasPermission(Permission.STAFF_DELETE)) {
            user.sendErrorMessage("§cYou don't have permission to delete reports.");
            return;
        }
        
        try {
            int reportId = Integer.parseInt(reportIdStr.replace("#", ""));
            rm.getReportByIdAsynchronously(reportId, false, true, db, integration, um, (report) -> {
                if (report != null) {
                    integration.runTask(() -> {
                        db.deleteReport(reportId, (success) -> {
                            if (success) {
                                user.sendMessage("§aReport #" + reportId + " has been deleted.");
                            } else {
                                user.sendErrorMessage("§cFailed to delete report #" + reportId + ".");
                            }
                        });
                    });
                } else {
                    integration.runTask(() -> {
                        user.sendErrorMessage("§cReport #" + reportId + " not found.");
                    });
                }
            });
        } catch (NumberFormatException e) {
            user.sendErrorMessage("§cInvalid report ID: " + reportIdStr);
        }
    }
    
    private void handleCommentCommand(User user, String[] args) {
        try {
            int reportId = Integer.parseInt(args[1].replace("#", ""));
            
            if (args.length == 2) {
                // Open comments menu
                rm.getReportByIdAsynchronously(reportId, false, true, db, integration, um, (report) -> {
                    if (report != null) {
                        integration.runTask(() -> {
                            user.openCommentsMenu(1, report, rm, db, integration, um, null, vm);
                        });
                    } else {
                        integration.runTask(() -> {
                            user.sendErrorMessage("§cReport #" + reportId + " not found.");
                        });
                    }
                });
            } else {
                // Add comment directly
                StringBuilder comment = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    comment.append(args[i]).append(" ");
                }
                
                user.sendMessage("§eComment system not yet fully implemented.");
                user.sendMessage("§7Would add comment: §f" + comment.toString().trim());
            }
        } catch (NumberFormatException e) {
            user.sendErrorMessage("§cInvalid report ID: " + args[1]);
        }
    }
    
    private void handleUserCommand(User user, String playerName) {
        // Show menu to choose between reports BY player or AGAINST player
        user.sendMessage("§6=== Reports for " + playerName + " ===");
        user.sendMessage("§a/reports user " + playerName + " by §7- Reports made BY " + playerName);
        user.sendMessage("§c/reports user " + playerName + " against §7- Reports made AGAINST " + playerName);
        
        // For now, default to reports against the player (most common use case)
        user.openUserAgainstReportsMenu(1, playerName, rm, db, integration, vm, null, um);
    }
    
    private void handlePunishCommand(User user, String playerName) {
        // Open AdminGUI for the specified player
        user.sendMessage("§aOpening AdminGUI for " + playerName + "...");
        user.getPlayer().performCommand("admin " + playerName);
    }
    
    private void handleStopCooldownCommand(User user, String playerName) {
        user.sendMessage("§eStop cooldown feature not yet implemented for " + playerName);
    }
    
    private void handleArchiveAllCommand(User user, String[] args) {
        String type = args.length > 1 ? args[1].toLowerCase() : "all";
        user.sendMessage("§eArchive all (" + type + ") feature not yet implemented.");
    }
    
    private void handleDeleteAllCommand(User user, String[] args) {
        String type = args.length > 1 ? args[1].toLowerCase() : "all";
        user.sendMessage("§eDelete all (" + type + ") feature not yet implemented.");
    }
    
    private void handleHelpCommand(CommandSender sender) {
        sender.sendMessage("§6=== TigerReports Commands ===");
        sender.sendMessage("§a/reports §7- Open reports GUI");
        sender.sendMessage("§a/reports <id> §7- View specific report");
        sender.sendMessage("§a/reports archives §7- View archived reports");
        sender.sendMessage("§a/reports archive <id> §7- Archive a report");
        sender.sendMessage("§a/reports delete <id> §7- Delete a report");
        sender.sendMessage("§a/reports comment <id> §7- View/add comments");
        sender.sendMessage("§a/reports user <player> §7- View player's reports");
        sender.sendMessage("§a/reports punish <player> §7- Open AdminGUI");
        sender.sendMessage("§a/reports notify §7- Toggle notifications");
        sender.sendMessage("§a/reports reload §7- Reload configuration");
        sender.sendMessage("§a/reports help §7- Show this help");
    }
    
    private User checkStaffAction(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return null;
        }
        
        Player player = (Player) sender;
        
        if (!Permission.STAFF.check(player)) {
            player.sendMessage(Message.PERMISSION_COMMAND.get());
            return null;
        }
        
        User user = um.getOnlineUser(player);
        if (user == null) {
            integration.getLogger().warning("User " + player.getName() + " not found in UsersManager");
            return null;
        }
        
        return user;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            
            // Add action completions
            for (String action : ACTIONS) {
                if (StringUtil.startsWithIgnoreCase(action, partial)) {
                    completions.add(action);
                }
            }
            
            // Add report ID completions (just show format)
            if (StringUtil.startsWithIgnoreCase("#", partial)) {
                completions.add("#<id>");
            }
        } else if (args.length == 2) {
            String action = args[0].toLowerCase();
            String partial = args[1].toLowerCase();
            
            if ("user".equals(action)) {
                // Complete player names
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (StringUtil.startsWithIgnoreCase(player.getName(), partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}
