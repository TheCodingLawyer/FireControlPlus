package me.confuser.banmanager.bukkit.tigerreports.tasks;

/**
 * Result callback interface for asynchronous operations
 * Adapted from original TigerReports ResultCallback interface
 */
public interface ResultCallback<T> {
    
    void onResultReceived(T result);
}
