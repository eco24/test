package client.Admin.model;

import client.Admin.model.network.AdminNetworkManager;
import client.Admin.model.network.MessageProcessor;
import client.Admin.model.network.XmlMessageBuilder;
import client.Admin.model.data.AdminSession;
import client.Admin.model.data.DataCache;
import java.util.*;

/**
 * Admin Client Model - Handles business logic and coordinates components
 * Refactored to use composition and separation of concerns
 */
public class AdminModel implements AdminNetworkManager.NetworkListener {

    // Core components
    private final AdminNetworkManager networkManager;
    private final MessageProcessor messageProcessor;
    private final XmlMessageBuilder xmlBuilder;
    private final AdminSession session;
    private final DataCache dataCache;

    // Listeners
    private final List<ModelListener> listeners;

    /**
     * Interface for model event listeners
     */
    public interface ModelListener {
        void onConnectionStatusChanged(boolean connected);
        void onLoginResult(boolean success, String message);
        void onUsersDataReceived(List<Map<String, String>> users);
        void onGroupsDataReceived(List<Map<String, String>> groups);
        void onMessageReceived(String message);
        void onErrorReceived(String error);
        void onOperationResult(boolean success, String message);
    }

    /**
     * Constructor
     */
    public AdminModel() {
        this.listeners = new ArrayList<>();
        this.session = new AdminSession();
        this.dataCache = new DataCache();
        this.xmlBuilder = new XmlMessageBuilder();
        this.networkManager = new AdminNetworkManager(this);
        this.messageProcessor = new MessageProcessor(session, dataCache, this);
    }

    // ==================== CONNECTION MANAGEMENT ====================

    /**
     * Connects to the server
     */
    public boolean connect() {
        return networkManager.connect();
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        networkManager.disconnect();
        session.clearSession();
    }

    /**
     * Checks if connected to server
     */
    public boolean isConnected() {
        return networkManager.isConnected();
    }

    /**
     * Checks if authenticated
     */
    public boolean isAuthenticated() {
        return session.isAuthenticated() && networkManager.isConnected();
    }

    // ==================== AUTHENTICATION ====================

    /**
     * Attempts to login as admin
     */
    public void login(String email, String password) {
        if (!validateLoginInput(email, password)) {
            return;
        }

        // Connect if not already connected
        if (!networkManager.isConnected()) {
            if (!networkManager.connect()) {
                notifyLoginResult(false, "Failed to connect to server");
                return;
            }
        }

        // Store admin email in session
        session.setAdminEmail(email.trim());

        // Send login message
        String loginXML = xmlBuilder.createLoginMessage(email.trim(), password);
        networkManager.sendMessage(loginXML);
        addLogEntry("Login request sent for: " + email);
    }

    /**
     * Logs out from the server
     */
    public void logout() {
        if (session.isAuthenticated()) {
            String logoutXML = xmlBuilder.createLogoutMessage();
            networkManager.sendMessage(logoutXML);
            session.clearSession();
            addLogEntry("Logged out successfully");
        }
        networkManager.disconnect();
    }

    // ==================== USER MANAGEMENT ====================

    /**
     * Requests all users from server
     */
    public void requestAllUsers() {
        if (!validateAuthentication()) return;

        String requestXML = xmlBuilder.createRequestMessage("admin_getusers");
        networkManager.sendMessage(requestXML);
        addLogEntry("Requested users list");
    }

    /**
     * Searches for users
     */
    public void searchUsers(String searchKey) {
        if (!validateAuthentication()) return;

        String searchXML = xmlBuilder.createSearchUsersMessage(searchKey);
        networkManager.sendMessage(searchXML);
        addLogEntry("Searching users for: " + searchKey);
    }

    /**
     * Adds a new user
     */
    public void addUser(String email, String password, String name, boolean isAdmin) {
        if (!validateAuthentication()) return;

        String addUserXML = xmlBuilder.createAddUserMessage(email, password, name, isAdmin);
        networkManager.sendMessage(addUserXML);
        addLogEntry("Adding new user: " + email);
    }

    /**
     * Updates user information
     */
    public void updateUser(String email, String newName, String newPassword) {
        if (!validateAuthentication()) return;

        String updateXML = xmlBuilder.createUpdateUserMessage(email, newName, newPassword);
        networkManager.sendMessage(updateXML);
        addLogEntry("Updating user: " + email);
    }

    /**
     * Deletes a user
     */
    public void deleteUser(String email) {
        if (!validateAuthentication()) return;

        if (email.equals("admin@chat.com")) {
            notifyError("Cannot delete the main admin account");
            return;
        }

        String deleteXML = xmlBuilder.createDeleteUserMessage(email);
        networkManager.sendMessage(deleteXML);
        addLogEntry("Deleting user: " + email);
    }

    // ==================== GROUP MANAGEMENT ====================

    /**
     * Requests all groups from server
     */
    public void requestAllGroups() {
        if (!validateAuthentication()) return;

        String requestXML = xmlBuilder.createRequestMessage("admin_getgroups");
        networkManager.sendMessage(requestXML);
        addLogEntry("Requested groups list");
    }

    /**
     * Deletes a group
     */
    public void deleteGroup(String groupId) {
        if (!validateAuthentication()) return;

        String deleteXML = xmlBuilder.createDeleteGroupMessage(groupId);
        networkManager.sendMessage(deleteXML);
        addLogEntry("Deleting group: " + groupId);
    }

    /**
     * Gets group members
     */
    public void getGroupMembers(String groupId) {
        if (!validateAuthentication()) return;

        String requestXML = xmlBuilder.createGetGroupMembersMessage(groupId);
        networkManager.sendMessage(requestXML);
        addLogEntry("Requested members for group: " + groupId);
    }

    // ==================== NETWORK LISTENER IMPLEMENTATION ====================

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        if (!connected && session.isAuthenticated()) {
            session.clearSession();
        }
        notifyConnectionStatusChanged(connected);
    }

    @Override
    public void onMessageReceived(String xmlMessage) {
        messageProcessor.processMessage(xmlMessage);
    }

    // ==================== MESSAGE PROCESSOR CALLBACKS ====================

    /**
     * Called by MessageProcessor when login response is received
     */
    public void handleLoginResult(boolean success, String message, String sessionId, String adminName, boolean isAdmin) {
        if (success && isAdmin) {
            session.setAuthenticationData(sessionId, adminName, true);
            notifyLoginResult(true, "Login successful");
            addLogEntry("Successfully authenticated as admin: " + session.getAdminEmail());
        } else if (success && !isAdmin) {
            notifyLoginResult(false, "Access denied: Admin privileges required");
            addLogEntry("Login failed: Not an admin user");
            networkManager.disconnect();
        } else {
            notifyLoginResult(false, message);
            addLogEntry("Login failed: " + message);
        }
    }

    /**
     * Called by MessageProcessor when users data is received
     */
    public void handleUsersData(List<Map<String, String>> users) {
        notifyUsersDataReceived(users);
        addLogEntry("Received " + users.size() + " users");
    }

    /**
     * Called by MessageProcessor when groups data is received
     */
    public void handleGroupsData(List<Map<String, String>> groups) {
        notifyGroupsDataReceived(groups);
        addLogEntry("Received " + groups.size() + " groups");
    }

    /**
     * Called by MessageProcessor when group members are received
     */
    public void handleGroupMembers(String groupId, List<String> members) {
        StringBuilder membersText = new StringBuilder("Group " + groupId + " Members:\n");
        for (String member : members) {
            membersText.append("- ").append(member).append("\n");
        }
        notifyMessage(membersText.toString());
        addLogEntry("Received members for group: " + groupId);
    }

    /**
     * Called by MessageProcessor when operation result is received
     */
    public void handleOperationResult(boolean success, String message) {
        notifyOperationResult(success, message);
        addLogEntry("Operation result: " + message);
    }

    /**
     * Called by MessageProcessor when error is received
     */
    public void handleError(String error) {
        notifyError(error);
        addLogEntry("Error received: " + error);
    }

    /**
     * Called by MessageProcessor when force logout occurs
     */
    public void handleForceLogout(String reason) {
        session.clearSession();
        notifyError("Forced logout: " + reason);
        addLogEntry("Forced logout: " + reason);
        networkManager.disconnect();
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates login input
     */
    private boolean validateLoginInput(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            notifyLoginResult(false, "Email cannot be empty");
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            notifyLoginResult(false, "Password cannot be empty");
            return false;
        }

        return true;
    }

    /**
     * Validates authentication for operations
     */
    private boolean validateAuthentication() {
        if (!session.isAuthenticated()) {
            notifyError("Not authenticated");
            return false;
        }
        return true;
    }

    // ==================== LOGGING ====================

    /**
     * Adds a log entry with timestamp
     */
    private void addLogEntry(String message) {
        String logEntry = dataCache.addLogEntry(message);
        notifyMessage(logEntry);
        System.out.println(logEntry); // Also print to console for debugging
    }

    // ==================== LISTENER MANAGEMENT ====================

    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }

    private void notifyConnectionStatusChanged(boolean connected) {
        for (ModelListener listener : listeners) {
            try {
                listener.onConnectionStatusChanged(connected);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyLoginResult(boolean success, String message) {
        for (ModelListener listener : listeners) {
            try {
                listener.onLoginResult(success, message);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyUsersDataReceived(List<Map<String, String>> users) {
        for (ModelListener listener : listeners) {
            try {
                listener.onUsersDataReceived(users);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyGroupsDataReceived(List<Map<String, String>> groups) {
        for (ModelListener listener : listeners) {
            try {
                listener.onGroupsDataReceived(groups);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyMessage(String message) {
        for (ModelListener listener : listeners) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyError(String error) {
        for (ModelListener listener : listeners) {
            try {
                listener.onErrorReceived(error);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyOperationResult(boolean success, String message) {
        for (ModelListener listener : listeners) {
            try {
                listener.onOperationResult(success, message);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    // ==================== GETTERS ====================

    public String getAdminEmail() {
        return session.getAdminEmail();
    }

    public String getAdminName() {
        return session.getAdminName();
    }

    public String getSessionId() {
        return session.getSessionId();
    }

    public List<Map<String, String>> getUsersList() {
        return dataCache.getUsersList();
    }

    public List<Map<String, String>> getGroupsList() {
        return dataCache.getGroupsList();
    }

    public List<String> getMessageLog() {
        return dataCache.getMessageLog();
    }
}