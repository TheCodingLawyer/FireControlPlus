package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.util.Message;

public class AdminCommandSpyCommand extends CommonCommand {

  public AdminCommandSpyCommand(BanManagerPlugin plugin) {
    super(plugin, "admincommandspy", false);
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    if (!(sender instanceof CommonPlayer)) {
      Message.get("sender.error.not.player").sendTo(sender);
      return true;
    }

    CommonPlayer player = (CommonPlayer) sender;
    
    // The actual command spy functionality will be handled by the Bukkit implementation
    Message.get("commandspy.toggled").sendTo(sender);
    
    return true;
  }
} 