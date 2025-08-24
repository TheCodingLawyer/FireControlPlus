package me.confuser.banmanager.webenhancer.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.api.events.PlayerDeniedEvent;
import me.confuser.banmanager.velocity.api.events.PlayerKickedEvent;
import me.confuser.banmanager.common.data.PlayerKickData;
import me.confuser.banmanager.webenhancer.common.listeners.CommonPlayerDeniedListener;
import me.confuser.banmanager.webenhancer.common.data.LogData;
import me.confuser.banmanager.webenhancer.velocity.VelocityPlugin;

import java.sql.SQLException;
import java.util.Queue;

public class BanListener extends Listener {
    private final VelocityPlugin velocityPlugin;
    private final CommonPlayerDeniedListener listener;

    public BanListener(VelocityPlugin velocityPlugin) {
        this.velocityPlugin = velocityPlugin;
        this.listener = new CommonPlayerDeniedListener(velocityPlugin.getPlugin());
    }

    @Subscribe
    public void onDeny(PlayerDeniedEvent event) {
        listener.handlePin(event.getPlayer(), event.getMessage());
    }

    // FIX: Add PlayerKickedEvent listener for WebUI synchronization
    @Subscribe  
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
            velocityPlugin.getPlugin().getLogStorage().createIfNotExists(kickLog);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onReload(ProxyReloadEvent event) {
        velocityPlugin.getPlugin().setupConfigs();
    }

}
