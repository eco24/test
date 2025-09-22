package client.Admin.controller;

import client.Admin.model.AdminModel;
import client.Admin.view.AdminView;
import client.Admin.controller.handlers.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.*;
import java.util.*;

/**
 * Admin Client Controller - Connects Model and View
 * Handles user interactions and updates view based on model changes
 */
public class AdminController implements AdminModel.ModelListener {

    private final AdminModel model;
    private final AdminView view;
    private final Timer refreshTimer;
    private boolean autoRefresh = false;

    // Event handler classes
    private final AuthenticationHandler authHandler;
    private final UserManagementHandler userHandler;
    private final GroupManagementHandler groupHandler;
    private final MessageHandler messageHandler;
    private final SystemHandler systemHandler;

    /**
     * Constructor
     * @param model The admin model
     * @param view The admin view
     */
    public AdminController(AdminModel model, AdminView view) {
        this.model = model;
        this.view = view;

        // Initialize event handlers
        this.authHandler = new AuthenticationHandler(model, view);
        this.userHandler = new UserManagementHandler(model, view);
        this.groupHandler = new GroupManagementHandler(model, view);
        this.messageHandler = new MessageHandler(model, view);
        this.systemHandler = new SystemHandler(model, view, this);

        // Register as model listener
        model.addListener(this);

        // Initialize event listeners
        initializeListeners();

        // Set up auto-refresh timer (disabled by default)
        this.refreshTimer = setupAutoRefresh();

        System.out.println("[CONTROLLER] Admin controller initialized");
    }

    /**
     * Initializes all event listeners by delegating to handler classes
     */
    private void initializeListeners() {
        // Authentication listeners
        view.addLoginListener(authHandler::handleLogin);
        view.addLogoutListener(authHandler::handleLogout);
        view.addExitListener(systemHandler::handleExit);

        // User management listeners
        view.addRefreshUsersListener(userHandler::handleRefreshUsers);
        view.addSearchUserListener(userHandler::handleSearchUser);
        view.addClearSearchListener(userHandler::handleClearSearch);
        view.addAddUserListener(userHandler::handleAddUser);
        view.addEditUserListener(userHandler::handleEditUser);
        view.addDeleteUserListener(userHandler::handleDeleteUser);

        // Group management listeners
        view.addRefreshGroupsListener(groupHandler::handleRefreshGroups);
        view.addDeleteGroupListener(groupHandler::handleDeleteGroup);
        view.addViewGroupMembersListener(groupHandler::handleViewGroupMembers);

        // Message log listeners
        view.addClearMessagesListener(messageHandler::handleClearMessages);
        view.addExportMessagesListener(messageHandler::handleExportMessages);

        // System listeners
        view.addRefreshAllListener(systemHandler::handleRefreshAll);
        view.addAboutListener(systemHandler::handleAbout);

        // Window listener
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                systemHandler.handleExit(null);
            }
        });
    }

    /**
     * Sets up auto-refresh timer
     */
    private Timer setupAutoRefresh() {
        return new Timer(30000, e -> {  // Refresh every 30 seconds
            if (autoRefresh && model.isAuthenticated()) {
                refreshAll();
            }
        });
    }

    // ==================== MODEL LISTENER IMPLEMENTATION ====================

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            view.setConnectionStatus(connected);
            if (!connected && model.isAuthenticated()) {
                view.showError("Connection to server lost");
                view.showLoginPanel();
            }
        });
    }

    @Override
    public void onLoginResult(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            if (success) {
                handleSuccessfulLogin();
            } else {
                handleFailedLogin(message);
            }
        });
    }

    @Override
    public void onUsersDataReceived(List<Map<String, String>> users) {
        SwingUtilities.invokeLater(() -> {
            updateUsersTable(users);
            view.setStatusBar("Loaded " + users.size() + " users");
        });
    }

    @Override
    public void onGroupsDataReceived(List<Map<String, String>> groups) {
        SwingUtilities.invokeLater(() -> {
            updateGroupsTable(groups);
            view.setStatusBar("Loaded " + groups.size() + " groups");
        });
    }

    @Override
    public void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> view.appendToMessageLog(message));
    }

    @Override
    public void onErrorReceived(String error) {
        SwingUtilities.invokeLater(() -> {
            view.showError(error);
            view.setStatusBar("Error: " + error);
        });
    }

    @Override
    public void onOperationResult(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            if (success) {
                view.showSuccess(message);
                refreshAll();
            } else {
                view.showError(message);
            }
            view.setStatusBar(message);
        });
    }

    // ==================== HELPER METHODS ====================

    /**
     * Handles successful login
     */
    private void handleSuccessfulLogin() {
        view.clearLoginFields();
        view.setLoginStatus("");
        view.showMainPanel();
        view.setStatusBar("Logged in as: " + model.getAdminEmail());

        refreshAll();

        if (autoRefresh) {
            refreshTimer.start();
        }
    }

    /**
     * Handles failed login
     */
    private void handleFailedLogin(String message) {
        view.setLoginStatus(message);
        view.showError(message);
    }

    /**
     * Updates the users table with received data
     */
    private void updateUsersTable(List<Map<String, String>> users) {
        view.clearUsersTable();

        for (Map<String, String> user : users) {
            String email = user.get("email");
            String name = user.get("name");
            boolean isAdmin = Boolean.parseBoolean(user.get("isAdmin"));
            String created = user.get("created");
            String status = determineUserStatus(email);

            view.addUserToTable(email, name, isAdmin, created, status);
        }
    }

    /**
     * Updates the groups table with received data
     */
    private void updateGroupsTable(List<Map<String, String>> groups) {
        view.clearGroupsTable();

        for (Map<String, String> group : groups) {
            String id = group.get("id");
            String name = group.get("name");
            String creator = group.get("creator");
            int memberCount = Integer.parseInt(group.get("memberCount"));
            String created = group.get("created");

            view.addGroupToTable(id, name, creator, memberCount, created);
        }
    }

    /**
     * Determines user status for display
     */
    private String determineUserStatus(String email) {
        if (email.equals(model.getAdminEmail())) {
            return "Online (You)";
        }
        return "Offline";  // Could be enhanced to check actual online status
    }

    /**
     * Refreshes all data
     */
    public void refreshAll() {
        if (model.isAuthenticated()) {
            view.setStatusBar("Refreshing all data...");
            model.requestAllUsers();
            model.requestAllGroups();
        }
    }

    /**
     * Enables or disables auto-refresh
     */
    public void setAutoRefresh(boolean enabled) {
        this.autoRefresh = enabled;
        if (enabled && model.isAuthenticated()) {
            refreshTimer.start();
        } else {
            refreshTimer.stop();
        }
    }

    // ==================== GETTERS FOR HANDLERS ====================

    public AdminModel getModel() {
        return model;
    }

    public AdminView getView() {
        return view;
    }
}