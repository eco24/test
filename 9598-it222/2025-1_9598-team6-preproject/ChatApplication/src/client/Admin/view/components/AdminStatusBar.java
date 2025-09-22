package client.Admin.view.components;

import client.Admin.view.styling.AdminTheme;
import javax.swing.*;
import java.awt.*;

/**
 * Status bar component for the Admin application
 */
public class AdminStatusBar extends JPanel {

    private JLabel statusLabel;
    private JLabel connectionStatusLabel;

    /**
     * Constructor
     */
    public AdminStatusBar() {
        setLayout(new BorderLayout());
        setBackground(AdminTheme.SURFACE_DARK);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, AdminTheme.ACCENT_BLUE),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        setPreferredSize(new Dimension(0, 35));

        initializeComponents();
        setupLayout();
    }

    /**
     * Initializes UI components
     */
    private void initializeComponents() {
        statusLabel = AdminTheme.createStyledLabel("🚀 Ready", AdminTheme.FONT_STATUS, AdminTheme.TEXT_PRIMARY);

        connectionStatusLabel = AdminTheme.createStyledLabel("❌ Disconnected ",
                AdminTheme.FONT_STATUS, AdminTheme.ACCENT_RED);
    }

    /**
     * Sets up the layout
     */
    private void setupLayout() {
        add(statusLabel, BorderLayout.WEST);
        add(connectionStatusLabel, BorderLayout.EAST);
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Sets the status message
     */
    public void setStatus(String status) {
        statusLabel.setText(" " + status);
    }

    /**
     * Sets the connection status
     */
    public void setConnectionStatus(boolean connected) {
        if (connected) {
            connectionStatusLabel.setText("✅ Connected ");
            connectionStatusLabel.setForeground(AdminTheme.ACCENT_GREEN);
        } else {
            connectionStatusLabel.setText("❌ Disconnected ");
            connectionStatusLabel.setForeground(AdminTheme.ACCENT_RED);
        }
    }
}