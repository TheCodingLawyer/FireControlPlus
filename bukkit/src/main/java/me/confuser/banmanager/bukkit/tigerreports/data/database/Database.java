package me.confuser.banmanager.bukkit.tigerreports.data.database;

import me.confuser.banmanager.bukkit.tigerreports.tasks.ResultCallback;
import me.confuser.banmanager.bukkit.tigerreports.tasks.TaskScheduler;

import java.sql.SQLException;
import java.util.List;

/**
 * Abstract database class for TigerReports integration
 * Adapted from original TigerReports Database class
 */
public abstract class Database {
    
    protected final TaskScheduler taskScheduler;
    
    public Database(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }
    
    public abstract void initialize();
    
    public abstract boolean isConnectionValid() throws SQLException;
    
    public abstract void closeConnection();
    
    public abstract void startClosing();
    
    public abstract void query(String sql, List<Object> parameters, ResultCallback<QueryResult> callback);
    
    public abstract void update(String sql, List<Object> parameters, ResultCallback<Integer> callback);
}
