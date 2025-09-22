package client.Admin.controller.handlers;

import client.Admin.model.AdminModel;
import client.Admin.view.AdminView;
import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Handles authentication-related operations (login/logout)
 */
public class AuthenticationHandler {

    private final AdminModel model;
    private final AdminView view;

    public AuthenticationHandler(AdminModel model, AdminView view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Handles login action
     */
    public void handleLogin(ActionEvent e) {
        String email = view.getLoginEmail();
        String password = view.getLoginPassword();

        if (!validateLoginInput(email, password)) {
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

    /**
     * Handles logout action
     */
    public void handleLogout(ActionEvent e) {
        if (view.showConfirmation("Are you sure you want to logout?")) {
            performLogout();
        }
    }

    /**
     * Validates login input
     */
    private boolean validateLoginInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            view.setLoginStatus("Please enter email and password");
            return false;
        }
        return true;
    }

    /**
     * Performs the actual logout process
     */
    private void performLogout() {
        model.logout();
        view.clearLoginFields();
        view.clearUsersTable();
        view.clearGroupsTable();
        view.clearMessageLog();
        view.showLoginPanel();
        view.setStatusBar("Logged out");
    }
}