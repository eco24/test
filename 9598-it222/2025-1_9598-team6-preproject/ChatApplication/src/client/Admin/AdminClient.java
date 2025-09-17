package client.Admin;

import client.Admin.controller.AdminController;
import client.Admin.model.AdminModel;
import client.Admin.view.AdminView;
import javax.swing.*;

/**
 * Main entry point for the Admin Client application
 * Initializes the MVC components and starts the GUI
 */
public class AdminClient {

    /**
     * Main method to launch the admin client
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Set the look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default if system look and feel is not available
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // Run GUI in Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Create MVC components
                AdminModel model = new AdminModel();
                AdminView view = new AdminView();
                AdminController controller = new AdminController(model,view);

                // Make the view visible
                view.setVisible(true);

                System.out.println("[ADMIN CLIENT] Admin client started successfully");

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to start admin client: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start admin client: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}