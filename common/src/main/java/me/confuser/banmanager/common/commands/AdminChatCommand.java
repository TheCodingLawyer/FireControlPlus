package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.util.Message;

public class AdminChatCommand extends CommonCommand {

  public AdminChatCommand(BanManagerPlugin plugin) {
    super(plugin, "adminchat", false);
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    if (!(sender instanceof CommonPlayer)) {
      Message.get("sender.error.not.player").sendTo(sender);
      return true;
    }

    CommonPlayer player = (CommonPlayer) sender;
    
    // The actual admin chat functionality will be handled by the Bukkit implementation
    Message.get("adminchat.toggled").sendTo(sender);
    
    return true;
  }
} 