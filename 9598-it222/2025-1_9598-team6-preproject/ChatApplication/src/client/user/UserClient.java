package client.user;

import client.user.controller.UserController;
import client.user.model.UserModel;
import client.user.view.UserView;
import javax.swing.*;

/**
 * Main entry point for the User Client application
 * Initializes the MVC components and starts the GUI
 */
public class UserClient {

    /**
     * Main method to launch the user client
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
                UserModel model = new UserModel();
                UserView view = new UserView();
                UserController controller = new UserController(model, view);

                // Make the view visible
                view.setVisible(true);

                System.out.println("[USER CLIENT] User client started successfully");

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to start user client: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start user client: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}