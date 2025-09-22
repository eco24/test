package client.Admin.controller.handlers;

import client.Admin.model.AdminModel;
import client.Admin.view.AdminView;
import client.Admin.controller.AdminController;
import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Handles system-level operations (refresh all, about, exit)
 */
public class SystemHandler {

    private final AdminModel model;
    private final AdminView view;
    private final AdminController controller;

    public SystemHandler(AdminModel model, AdminView view, AdminController controller) {
        this.model = model;
        this.view = view;
        this.controller = controller;
    }

    /**
     * Handles refresh all action
     */
    public void handleRefreshAll(ActionEvent e) {
        controller.refreshAll();
    }

    /**
     * Handles about action
     */
    public void handleAbout(ActionEvent e) {
        String aboutMessage = buildAboutMessage();
        JOptionPane.showMessageDialog(view, aboutMessage,
                "About Admin Panel", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Handles exit action
     */
    public void handleExit(ActionEvent e) {
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
     * Builds the about message text
     */
    private String buildAboutMessage() {
        return "Chat Application Admin Panel\n\n" +
                "Version: 1.0.0\n" +
                "CSIT 222 Prelim Project\n\n" +
                "This admin panel allows management of:\n" +
                "• Users (CRUD operations)\n" +
                "• Groups (Delete operations)\n" +
                "• Message monitoring\n\n" +
                "© 2025 Chat Application Team";
    }
}