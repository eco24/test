package client.Admin.model.data;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Manages cached data for the admin application
 */
public class DataCache {

    // Data storage
    private List<Map<String, String>> usersList;
    private List<Map<String, String>> groupsList;
    private List<String> messageLog;

    // Date formatter for log entries
    private final SimpleDateFormat dateFormat;

    /**
     * Constructor
     */
    public DataCache() {
        this.usersList = new ArrayList<>();
        this.groupsList = new ArrayList<>();
        this.messageLog = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    // ==================== USERS DATA ====================

    /**
     * Sets the users list
     */
    public void setUsersList(List<Map<String, String>> users) {
        this.usersList = new ArrayList<>(users);
    }

    /**
     * Gets a copy of the users list
     */
    public List<Map<String, String>> getUsersList() {
        return new ArrayList<>(usersList);
    }

    /**
     * Clears the users list
     */
    public void clearUsersList() {
        usersList.clear();
    }

    // ==================== GROUPS DATA ====================

    /**
     * Sets the groups list
     */
    public void setGroupsList(List<Map<String, String>> groups) {
        this.groupsList = new ArrayList<>(groups);
    }

    /**
     * Gets a copy of the groups list
     */
    public List<Map<String, String>> getGroupsList() {
        return new ArrayList<>(groupsList);
    }

    /**
     * Clears the groups list
     */
    public void clearGroupsList() {
        groupsList.clear();
    }

    // ==================== MESSAGE LOG ====================

    /**
     * Adds a log entry with timestamp
     */
    public String addLogEntry(String message) {
        String timestamp = dateFormat.format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
        messageLog.add(logEntry);
        return logEntry;
    }

    /**
     * Gets a copy of the message log
     */
    public List<String> getMessageLog() {
        return new ArrayList<>(messageLog);
    }

    /**
     * Clears the message log
     */
    public void clearMessageLog() {
        messageLog.clear();
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Clears all cached data
     */
    public void clearAll() {
        clearUsersList();
        clearGroupsList();
        clearMessageLog();
    }

    /**
     * Gets cache statistics
     */
    public Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("users", usersList.size());
        stats.put("groups", groupsList.size());
        stats.put("logEntries", messageLog.size());
        return stats;
    }
}