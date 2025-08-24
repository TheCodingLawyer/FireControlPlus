package me.confuser.banmanager.velocity.listeners;

import com.velocitypowered.api.event.*;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.listeners.CommonJoinHandler;
import me.confuser.banmanager.common.listeners.CommonJoinListener;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.velocity.BMVelocityPlugin;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.VelocityPlayer;
import me.confuser.banmanager.velocity.VelocityServer;
import me.confuser.banmanager.velocity.util.FloodgateUtil;

public class JoinListener extends Listener {
  private final CommonJoinListener listener;
  private BMVelocityPlugin plugin;

  public JoinListener(BMVelocityPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonJoinListener(plugin.getPlugin());
  }

  @Subscribe(order = PostOrder.EARLY)
  public EventTask banCheck(LoginEvent event) {
    return EventTask.async(() -> this.checkBanJoin(event));
  }

  public void checkBanJoin(final LoginEvent event) {
    java.util.UUID uuid = event.getPlayer().getUniqueId();
    String name = event.getPlayer().getUsername();

    FloodgateUtil.FloodgateIdentity bedrock = FloodgateUtil.remapIdentity(uuid, name);
    if (bedrock != null) {
      uuid = bedrock.uuid;
      name = bedrock.name;
    }

    listener.banCheck(uuid, name,
            IPUtils.toIPAddress(event.getPlayer().getRemoteAddress().getAddress()), new BanJoinHandler(plugin.getPlugin(), event));

    if (event.getResult().isAllowed()) {
      listener.onPreJoin(uuid, name,
              IPUtils.toIPAddress(event.getPlayer().getRemoteAddress().getAddress()));
    }
  }

  @Subscribe
  public void onJoin(PostLoginEvent event) {
    listener.onJoin(new VelocityPlayer(event.getPlayer(), plugin.getPlugin().getConfig().isOnlineMode()));
  }

  @Subscribe(order = PostOrder.LAST)
  public void onPlayerLogin(PostLoginEvent event) {
    listener.onPlayerLogin(new VelocityPlayer(event.getPlayer(), plugin.getPlugin().getConfig().isOnlineMode()), new LoginHandler(event));
  }

  @RequiredArgsConstructor
  public static class BanJoinHandler implements CommonJoinHandler {
    private final BanManagerPlugin plugin;
    private final LoginEvent event;

    @Override
    public void handleDeny(Message message) {
      event.setResult(ResultedEvent.ComponentResult.denied(VelocityServer.formatMessage(message.toString())));
    }

    @Override
    public void handlePlayerDeny(PlayerData player, Message message) {
      plugin.getServer().callEvent("PlayerDeniedEvent", player, message);
      event.setResult(ResultedEvent.ComponentResult.denied(VelocityServer.formatMessage(message.toString())));
    }
  }

  @RequiredArgsConstructor
  public class LoginHandler implements CommonJoinHandler {
    private final PostLoginEvent event;

    @Override
    public void handleDeny(Message message) {
      event.getPlayer().disconnect(VelocityServer.formatMessage(message.toString()));
    }

    @Override
    public void handlePlayerDeny(PlayerData player, Message message) {
      handleDeny(message);
    }
  }
}
