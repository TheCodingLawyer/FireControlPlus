package me.confuser.banmanager.bukkit.tigerreports.tasks;

/**
 * Task scheduler interface for TigerReports integration
 * Adapted from original TigerReports TaskScheduler interface
 */
public interface TaskScheduler {
    
    int runTaskDelayedly(long delay, Runnable task);
    
    int runTaskDelayedlyAsynchronously(long delay, Runnable task);
    
    void runTaskAsynchronously(Runnable task);
    
    void runTask(Runnable task);
    
    int runTaskRepeatedly(long delay, long period, Runnable task);
    
    void cancelTask(int taskId);
}
