package client.Admin.controller;

import client.Admin.model.AdminModel;
import client.Admin.view.AdminView;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Admin Client Controller - Connects Model and View
 * Handles user interactions and updates view based on model changes
 */
public class AdminController implements AdminModel.ModelListener {

    private AdminModel model;
    private AdminView view;
    private Timer refreshTimer;
    private boolean autoRefresh = false;

    /**
     * Constructor
     * @param model The admin model
     * @param view The admin view
     */
    public AdminController(AdminModel model, AdminView view) {
        this.model = model;
        this.view = view;

        // Register as model listener
        model.addListener(this);

        // Initialize event listeners
        initializeListeners();

        // Set up auto-refresh timer (disabled by default)
        setupAutoRefresh();

        System.out.println("[CONTROLLER] Admin controller initialized");
    }

    /**
     * Initializes all event listeners
     */
    private void initializeListeners() {
        // Login/Logout listeners
        view.addLoginListener(new LoginListener());
        view.addLogoutListener(new LogoutListener());
        view.addExitListener(new ExitListener());

        // User management listeners
        view.addRefreshUsersListener(new RefreshUsersListener());
        view.addSearchUserListener(new SearchUserListener());
        view.addClearSearchListener(new ClearSearchListener());
        view.addAddUserListener(new AddUserListener());
        view.addEditUserListener(new EditUserListener());
        view.addDeleteUserListener(new DeleteUserListener());

        // Group management listeners
        view.addRefreshGroupsListener(new RefreshGroupsListener());
        view.addDeleteGroupListener(new DeleteGroupListener());
        view.addViewGroupMembersListener(new ViewGroupMembersListener());

        // Message log listeners
        view.addClearMessagesListener(new ClearMessagesListener());
        view.addExportMessagesListener(new ExportMessagesListener());

        // Menu listeners
        view.addRefreshAllListener(new RefreshAllListener());
        view.addAboutListener(new AboutListener());

        // Window listener
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
    }

    /**
     * Sets up auto-refresh timer
     */
    private void setupAutoRefresh() {
        refreshTimer = new Timer(30000, e -> {  // Refresh every 30 seconds
            if (autoRefresh && model.isAuthenticated()) {
                refreshAll();
            }
        });
    }

    // ==================== EVENT LISTENERS ====================

    /**
     * Handles login action
     */
    private class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = view.getLoginEmail();
            String password = view.getLoginPassword();

            if (email.isEmpty() || password.isEmpty()) {
                view.setLoginStatus("Please enter email and password");
                return;
            }

            view.setLoginStatus("Connecting to server...");
            view.setStatusBar("Attempting login...");

            // Perform login in background thread
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    model.login(email, password);
                    return null;
                }
            };
            worker.execute();
        }
    }

    /**
     * Handles logout action
     */
    private class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (view.showConfirmation("Are you sure you want to logout?")) {
                model.logout();
                view.clearLoginFields();
                view.clearUsersTable();
                view.clearGroupsTable();
                view.clearMessageLog();
                view.showLoginPanel();
                view.setStatusBar("Logged out");
            }
        }
    }

    /**
     * Handles exit action
     */
    private class ExitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            handleExit();
        }
    }

    /**
     * Handles refresh users action
     */
    private class RefreshUsersListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isAuthenticated()) {
                view.setStatusBar("Refreshing users...");
                model.requestAllUsers();
            }
        }
    }

    /**
     * Handles search user action
     */
    private class SearchUserListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String searchKey = view.getSearchText();
            if (!searchKey.isEmpty() && model.isAuthenticated()) {
                view.setStatusBar("Searching users...");
                model.searchUsers(searchKey);
            } else {
                model.requestAllUsers();
            }
        }
    }

    /**
     * Handles clear search action
     */
    private class ClearSearchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.getSearchUserField().setText("");
            if (model.isAuthenticated()) {
                model.requestAllUsers();
            }
        }
    }

    /**
     * Handles add user action
     */
    private class AddUserListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!model.isAuthenticated()) return;

            // Create user input dialog
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JTextField emailField = new JTextField(20);
            JPasswordField passwordField = new JPasswordField(20);
            JTextField nameField = new JTextField(20);
            JCheckBox isAdminCheckBox = new JCheckBox("Admin privileges");

            panel.add(new JLabel("Email:"));
            panel.add(emailField);
            panel.add(Box.createVerticalStrut(5));
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);
            panel.add(Box.createVerticalStrut(5));
            panel.add(new JLabel("Name:"));
            panel.add(nameField);
            panel.add(Box.createVerticalStrut(5));
            panel.add(isAdminCheckBox);

            int result = JOptionPane.showConfirmDialog(view, panel,
                    "Add New User", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword());
                String name = nameField.getText().trim();
                boolean isAdmin = isAdminCheckBox.isSelected();

                if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    view.showError("All fields are required");
                    return;
                }

                if (!email.contains("@")) {
                    view.showError("Please enter a valid email address");
                    return;
                }

                model.addUser(email, password, name, isAdmin);
                view.setStatusBar("Adding user: " + email);
            }
        }
    }

    /**
     * Handles edit user action
     */
    private class EditUserListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!model.isAuthenticated()) return;

            String selectedEmail = view.getSelectedUserEmail();
            if (selectedEmail == null) {
                view.showError("Please select a user to edit");
                return;
            }

            // Create edit dialog
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JTextField nameField = new JTextField(20);
            JPasswordField passwordField = new JPasswordField(20);

            panel.add(new JLabel("Editing user: " + selectedEmail));
            panel.add(Box.createVerticalStrut(10));
            panel.add(new JLabel("New Name (leave empty to keep current):"));
            panel.add(nameField);
            panel.add(Box.createVerticalStrut(5));
            panel.add(new JLabel("New Password (leave empty to keep current):"));
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(view, panel,
                    "Edit User", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                String newPassword = new String(passwordField.getPassword());

                if (newName.isEmpty() && newPassword.isEmpty()) {
                    view.showError("No changes specified");
                    return;
                }

                model.updateUser(selectedEmail,
                        newName.isEmpty() ? null : newName,
                        newPassword.isEmpty() ? null : newPassword);
                view.setStatusBar("Updating user: " + selectedEmail);
            }
        }
    }

    /**
     * Handles delete user action
     */
    private class DeleteUserListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!model.isAuthenticated()) return;

            String selectedEmail = view.getSelectedUserEmail();
            if (selectedEmail == null) {
                view.showError("Please select a user to delete");
                return;
            }

            if (selectedEmail.equals("admin@chat.com")) {
                view.showError("Cannot delete the main admin account");
                return;
            }

            if (selectedEmail.equals(model.getAdminEmail())) {
                view.showError("Cannot delete your own account");
                return;
            }

            if (view.showConfirmation("Are you sure you want to delete user: " + selectedEmail + "?")) {
                model.deleteUser(selectedEmail);
                view.setStatusBar("Deleting user: " + selectedEmail);
            }
        }
    }

    /**
     * Handles refresh groups action
     */
    private class RefreshGroupsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isAuthenticated()) {
                view.setStatusBar("Refreshing groups...");
                model.requestAllGroups();
            }
        }
    }

    /**
     * Handles delete group action
     */
    private class DeleteGroupListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!model.isAuthenticated()) return;

            String selectedGroupId = view.getSelectedGroupId();
            if (selectedGroupId == null) {
                view.showError("Please select a group to delete");
                return;
            }

            if (view.showConfirmation("Are you sure you want to delete group: " + selectedGroupId + "?")) {
                model.deleteGroup(selectedGroupId);
                view.setStatusBar("Deleting group: " + selectedGroupId);
            }
        }
    }

    /**
     * Handles view group members action
     */
    private class ViewGroupMembersListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!model.isAuthenticated()) return;

            String selectedGroupId = view.getSelectedGroupId();
            if (selectedGroupId == null) {
                view.showError("Please select a group to view members");
                return;
            }

            model.getGroupMembers(selectedGroupId);
            view.setStatusBar("Fetching members for group: " + selectedGroupId);
        }
    }

    /**
     * Handles clear messages action
     */
    private class ClearMessagesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (view.showConfirmation("Are you sure you want to clear the message log?")) {
                view.clearMessageLog();
                view.setStatusBar("Message log cleared");
            }
        }
    }

    /**
     * Handles export messages action
     */
    private class ExportMessagesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Message Log");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String defaultFileName = "admin_log_" + sdf.format(new Date()) + ".txt";
            fileChooser.setSelectedFile(new File(defaultFileName));

            int result = fileChooser.showSaveDialog(view);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.write(view.getMessageLog());
                    view.showSuccess("Message log exported successfully");
                    view.setStatusBar("Log exported to: " + file.getName());
                } catch (IOException ex) {
                    view.showError("Failed to export log: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Handles refresh all action
     */
    private class RefreshAllListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            refreshAll();
        }
    }

    /**
     * Handles about action
     */
    private class AboutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String aboutMessage =
                    "Chat Application Admin Panel\n\n" +
                            "Version: 1.0.0\n" +
                            "CSIT 222 Prelim Project\n\n" +
                            "This admin panel allows management of:\n" +
                            "• Users (CRUD operations)\n" +
                            "• Groups (Delete operations)\n" +
                            "• Message monitoring\n\n" +
                            "© 2025 Chat Application Team";

            JOptionPane.showMessageDialog(view, aboutMessage,
                    "About Admin Panel", JOptionPane.INFORMATION_MESSAGE);
        }
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
                view.clearLoginFields();
                view.setLoginStatus("");
                view.showMainPanel();
                view.setStatusBar("Logged in as: " + model.getAdminEmail());

                // Initial data load
                refreshAll();

                // Start auto-refresh if enabled
                if (autoRefresh) {
                    refreshTimer.start();
                }
            } else {
                view.setLoginStatus(message);
                view.showError(message);
            }
        });
    }

    @Override
    public void onUsersDataReceived(List<Map<String, String>> users) {
        SwingUtilities.invokeLater(() -> {
            view.clearUsersTable();

            // Check online status for each user
            for (Map<String, String> user : users) {
                String email = user.get("email");
                String name = user.get("name");
                boolean isAdmin = Boolean.parseBoolean(user.get("isAdmin"));
                String created = user.get("created");
                String status = "Offline";  // Default status

                // You could check with server for online status
                if (email.equals(model.getAdminEmail())) {
                    status = "Online (You)";
                }

                view.addUserToTable(email, name, isAdmin, created, status);
            }

            view.setStatusBar("Loaded " + users.size() + " users");
        });
    }

    @Override
    public void onGroupsDataReceived(List<Map<String, String>> groups) {
        SwingUtilities.invokeLater(() -> {
            view.clearGroupsTable();

            for (Map<String, String> group : groups) {
                String id = group.get("id");
                String name = group.get("name");
                String creator = group.get("creator");
                int memberCount = Integer.parseInt(group.get("memberCount"));
                String created = group.get("created");

                view.addGroupToTable(id, name, creator, memberCount, created);
            }

            view.setStatusBar("Loaded " + groups.size() + " groups");
        });
    }

    @Override
    public void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> {
            view.appendToMessageLog(message);
        });
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
                // Refresh data after successful operation
                refreshAll();
            } else {
                view.showError(message);
            }
            view.setStatusBar(message);
        });
    }

    // ==================== HELPER METHODS ====================

    /**
     * Refreshes all data
     */
    private void refreshAll() {
        if (model.isAuthenticated()) {
            view.setStatusBar("Refreshing all data...");
            model.requestAllUsers();
            model.requestAllGroups();
        }
    }

    /**
     * Handles application exit
     */
    private void handleExit() {
        if (model.isAuthenticated()) {
            if (view.showConfirmation("Are you sure you want to exit?")) {
                model.logout();
                System.exit(0);
            }
        } else {
            System.exit(0);
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
}
