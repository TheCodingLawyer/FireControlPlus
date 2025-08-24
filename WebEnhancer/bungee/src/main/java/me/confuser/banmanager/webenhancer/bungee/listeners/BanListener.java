package me.confuser.banmanager.webenhancer.bungee.listeners;

import me.confuser.banmanager.bungee.api.events.PlayerDeniedEvent;
import me.confuser.banmanager.bungee.api.events.PlayerKickedEvent;
import me.confuser.banmanager.bungee.api.events.PluginReloadedEvent;
import me.confuser.banmanager.common.data.PlayerKickData;
import me.confuser.banmanager.webenhancer.bungee.BungeePlugin;
import me.confuser.banmanager.webenhancer.common.data.LogData;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import me.confuser.banmanager.webenhancer.common.listeners.CommonPlayerDeniedListener;

import java.sql.SQLException;
import java.util.Queue;

public class BanListener implements Listener {
  private final BungeePlugin plugin;
  private final CommonPlayerDeniedListener listener;

  public BanListener(BungeePlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonPlayerDeniedListener(plugin.getPlugin());
  }

  @EventHandler
  public void onDeny(PlayerDeniedEvent event) {
    listener.handlePin(event.getPlayer(), event.getMessage());
  }

  // FIX: Add PlayerKickedEvent listener for WebUI synchronization  
  @EventHandler
  public void onPlayerKicked(PlayerKickedEvent event) {
    PlayerKickData kick = event.getKick();

    // Create a log entry for the kick WITHOUT [BanManager] prefix to avoid filtering
    String kickLogMessage = String.format("%s kicked %s%s", 
        kick.getActor().getName(), 
        kick.getPlayer().getName(),
        !kick.getReason().isEmpty() ? " for " + kick.getReason() : "");
    
    LogData kickLog = new LogData(kickLogMessage, System.currentTimeMillis() / 1000L);
    
    // Direct database write since proxy platforms don't use appender queues
    try {
      plugin.getPlugin().getLogStorage().createIfNotExists(kickLog);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @EventHandler
  public void onReload(PluginReloadedEvent event) {
    plugin.getPlugin().setupConfigs();
  }
}
