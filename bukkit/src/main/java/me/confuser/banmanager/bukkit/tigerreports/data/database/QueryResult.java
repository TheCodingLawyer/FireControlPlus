package me.confuser.banmanager.bukkit.tigerreports.data.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query result wrapper for database operations
 * Adapted from original TigerReports QueryResult class
 */
public class QueryResult {
    
    private final List<Map<String, Object>> rows;
    
    public QueryResult() {
        this.rows = new ArrayList<>();
    }
    
    public QueryResult(List<Map<String, Object>> rows) {
        this.rows = rows != null ? rows : new ArrayList<>();
    }
    
    public List<Map<String, Object>> getRows() {
        return rows;
    }
    
    public Map<String, Object> getFirstRow() {
        return rows.isEmpty() ? new HashMap<>() : rows.get(0);
    }
    
    public boolean isEmpty() {
        return rows.isEmpty();
    }
    
    public int size() {
        return rows.size();
    }
    
    public Object getValue(int row, String column) {
        if (row >= 0 && row < rows.size()) {
            return rows.get(row).get(column);
        }
        return null;
    }
    
    public Object getFirstValue(String column) {
        return getValue(0, column);
    }
}
