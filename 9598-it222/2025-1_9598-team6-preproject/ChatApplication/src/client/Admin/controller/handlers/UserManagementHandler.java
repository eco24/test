package client.Admin.controller.handlers;

import client.Admin.model.AdminModel;
import client.Admin.view.AdminView;
import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Handles user management operations (CRUD operations for users)
 */
public class UserManagementHandler {

    private final AdminModel model;
    private final AdminView view;

    public UserManagementHandler(AdminModel model, AdminView view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Handles refresh users action
     */
    public void handleRefreshUsers(ActionEvent e) {
        if (model.isAuthenticated()) {
            view.setStatusBar("Refreshing users...");
            model.requestAllUsers();
        }
    }

    /**
     * Handles search user action
     */
    public void handleSearchUser(ActionEvent e) {
        String searchKey = view.getSearchText();
        if (!searchKey.isEmpty() && model.isAuthenticated()) {
            view.setStatusBar("Searching users...");
            model.searchUsers(searchKey);
        } else {
            model.requestAllUsers();
        }
    }

    /**
     * Handles clear search action
     */
    public void handleClearSearch(ActionEvent e) {
        view.getSearchUserField().setText("");
        if (model.isAuthenticated()) {
            model.requestAllUsers();
        }
    }

    /**
     * Handles add user action
     */
    public void handleAddUser(ActionEvent e) {
        if (!model.isAuthenticated()) return;

        UserDialogData userData = showAddUserDialog();
        if (userData != null && validateUserData(userData)) {
            model.addUser(userData.email, userData.password, userData.name, userData.isAdmin);
            view.setStatusBar("Adding user: " + userData.email);
        }
    }

    /**
     * Handles edit user action
     */
    public void handleEditUser(ActionEvent e) {
        if (!model.isAuthenticated()) return;

        String selectedEmail = view.getSelectedUserEmail();
        if (!validateUserSelection(selectedEmail)) {
            return;
        }

        UserEditData editData = showEditUserDialog(selectedEmail);
        if (editData != null && (editData.hasChanges())) {
            model.updateUser(selectedEmail, editData.newName, editData.newPassword);
            view.setStatusBar("Updating user: " + selectedEmail);
        }
    }

    /**
     * Handles delete user action
     */
    public void handleDeleteUser(ActionEvent e) {
        if (!model.isAuthenticated()) return;

        String selectedEmail = view.getSelectedUserEmail();
        if (!validateUserSelection(selectedEmail) || !validateUserDeletion(selectedEmail)) {
            return;
        }

        if (view.showConfirmation("Are you sure you want to delete user: " + selectedEmail + "?")) {
            model.deleteUser(selectedEmail);
            view.setStatusBar("Deleting user: " + selectedEmail);
        }
    }

    /**
     * Shows add user dialog and returns user data
     */
    private UserDialogData showAddUserDialog() {
        JPanel panel = createAddUserPanel();

        int result = JOptionPane.showConfirmDialog(view, panel,
                "Add New User", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return extractUserDataFromPanel(panel);
        }
        return null;
    }

    /**
     * Creates the add user dialog panel
     */
    private JPanel createAddUserPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField nameField = new JTextField(20);
        JCheckBox isAdminCheckBox = new JCheckBox("Admin privileges");

        // Store components in panel for later retrieval
        panel.putClientProperty("emailField", emailField);
        panel.putClientProperty("passwordField", passwordField);
        panel.putClientProperty("nameField", nameField);
        panel.putClientProperty("isAdminCheckBox", isAdminCheckBox);

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

        return panel;
    }

    /**
     * Extracts user data from the dialog panel
     */
    private UserDialogData extractUserDataFromPanel(JPanel panel) {
        JTextField emailField = (JTextField) panel.getClientProperty("emailField");
        JPasswordField passwordField = (JPasswordField) panel.getClientProperty("passwordField");
        JTextField nameField = (JTextField) panel.getClientProperty("nameField");
        JCheckBox isAdminCheckBox = (JCheckBox) panel.getClientProperty("isAdminCheckBox");

        return new UserDialogData(
                emailField.getText().trim(),
                new String(passwordField.getPassword()),
                nameField.getText().trim(),
                isAdminCheckBox.isSelected()
        );
    }

    /**
     * Shows edit user dialog and returns edit data
     */
    private UserEditData showEditUserDialog(String selectedEmail) {
        JPanel panel = createEditUserPanel(selectedEmail);

        int result = JOptionPane.showConfirmDialog(view, panel,
                "Edit User", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return extractEditDataFromPanel(panel);
        }
        return null;
    }

    /**
     * Creates the edit user dialog panel
     */
    private JPanel createEditUserPanel(String selectedEmail) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField nameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        // Store components for later retrieval
        panel.putClientProperty("nameField", nameField);
        panel.putClientProperty("passwordField", passwordField);

        panel.add(new JLabel("Editing user: " + selectedEmail));
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("New Name (leave empty to keep current):"));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(5));
        panel.add(new JLabel("New Password (leave empty to keep current):"));
        panel.add(passwordField);

        return panel;
    }

    /**
     * Extracts edit data from the dialog panel
     */
    private UserEditData extractEditDataFromPanel(JPanel panel) {
        JTextField nameField = (JTextField) panel.getClientProperty("nameField");
        JPasswordField passwordField = (JPasswordField) panel.getClientProperty("passwordField");

        String newName = nameField.getText().trim();
        String newPassword = new String(passwordField.getPassword());

        return new UserEditData(
                newName.isEmpty() ? null : newName,
                newPassword.isEmpty() ? null : newPassword
        );
    }

    /**
     * Validates user data from add dialog
     */
    private boolean validateUserData(UserDialogData userData) {
        if (userData.email.isEmpty() || userData.password.isEmpty() || userData.name.isEmpty()) {
            view.showError("All fields are required");
            return false;
        }

        if (!userData.email.contains("@")) {
            view.showError("Please enter a valid email address");
            return false;
        }

        return true;
    }

    /**
     * Validates user selection for edit/delete operations
     */
    private boolean validateUserSelection(String selectedEmail) {
        if (selectedEmail == null) {
            view.showError("Please select a user");
            return false;
        }
        return true;
    }

    /**
     * Validates if user can be deleted
     */
    private boolean validateUserDeletion(String selectedEmail) {
        if (selectedEmail.equals("admin@chat.com")) {
            view.showError("Cannot delete the main admin account");
            return false;
        }

        if (selectedEmail.equals(model.getAdminEmail())) {
            view.showError("Cannot delete your own account");
            return false;
        }

        return true;
    }

    // ==================== DATA CLASSES ====================

    /**
     * Data class for user dialog information
     */
    private static class UserDialogData {
        final String email;
        final String password;
        final String name;
        final boolean isAdmin;

        UserDialogData(String email, String password, String name, boolean isAdmin) {
            this.email = email;
            this.password = password;
            this.name = name;
            this.isAdmin = isAdmin;
        }
    }

    /**
     * Data class for user edit information
     */
    private static class UserEditData {
        final String newName;
        final String newPassword;

        UserEditData(String newName, String newPassword) {
            this.newName = newName;
            this.newPassword = newPassword;
        }

        boolean hasChanges() {
            return newName != null || newPassword != null;
        }
    }
}