package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.util.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ReportAdminCommand extends CommonCommand {
  private static final Set<UUID> adminReportMode = new HashSet<>();

  public ReportAdminCommand(BanManagerPlugin plugin) {
    super(plugin, "reportadmin", false);
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    if (!(sender instanceof CommonPlayer)) {
      Message.get("sender.error.not.player").sendTo(sender);
      return true;
    }

    CommonPlayer player = (CommonPlayer) sender;
    UUID playerId = player.getUniqueId();

    if (adminReportMode.contains(playerId)) {
      adminReportMode.remove(playerId);
      Message.get("reportadmin.disabled").sendTo(sender);
    } else {
      adminReportMode.add(playerId);
      Message.get("reportadmin.enabled").sendTo(sender);
    }

    return true;
  }

  public static boolean isInAdminReportMode(UUID playerId) {
    return adminReportMode.contains(playerId);
  }

  public static void removeFromAdminMode(UUID playerId) {
    adminReportMode.remove(playerId);
  }
}

