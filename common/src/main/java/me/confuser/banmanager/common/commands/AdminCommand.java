package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.util.Message;

public class AdminCommand extends CommonCommand {

  public AdminCommand(BanManagerPlugin plugin) {
    super(plugin, "admin", false);
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    if (!(sender instanceof CommonPlayer)) {
      Message.get("sender.error.not.player").sendTo(sender);
      return true;
    }

    CommonPlayer player = (CommonPlayer) sender;
    
    // For now, just send a message - the actual GUI will be handled by the Bukkit implementation
    Message.get("admin.gui.opening").sendTo(sender);
    
    return true;
  }
} 