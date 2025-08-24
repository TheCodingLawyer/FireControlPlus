package fr.mrtigreroux.tigerreports.events;

import java.util.Objects;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.confuser.banmanager.bukkit.tigerreports.objects.reports.Report;

/**
 * @author MrTigreroux
 * Bridge class for WebEnhancer compatibility - redirects to BanManager TigerReports integration
 */
public class NewReportEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    private final Report r;
    private final boolean bungee;
    
    public NewReportEvent(Report r, boolean bungee) {
        this.r = Objects.requireNonNull(r);
        this.bungee = bungee;
    }
    
    public Report getReport() {
        return r;
    }
    
    public boolean isFromBungeeCord() {
        return bungee;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}

