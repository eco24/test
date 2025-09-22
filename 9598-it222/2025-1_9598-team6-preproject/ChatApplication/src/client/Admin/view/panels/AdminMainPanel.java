package client.Admin.view.panels;

import client.Admin.view.styling.AdminTheme;
import javax.swing.*;
import java.awt.*;

/**
 * Main panel containing the tabbed interface
 */
public class AdminMainPanel extends JPanel {

    private JTabbedPane tabbedPane;

    /**
     * Constructor
     */
    public AdminMainPanel(AdminUsersPanel usersPanel, AdminGroupsPanel groupsPanel, AdminMessagesPanel messagesPanel) {
        setLayout(new BorderLayout());
        setBackground(AdminTheme.BACKGROUND_DARK);

        initializeTabbedPane(usersPanel, groupsPanel, messagesPanel);
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Initializes the tabbed pane with all panels
     */
    private void initializeTabbedPane(AdminUsersPanel usersPanel, AdminGroupsPanel groupsPanel, AdminMessagesPanel messagesPanel) {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(AdminTheme.FONT_BUTTON);
        tabbedPane.setBackground(AdminTheme.SURFACE_DARK);
        tabbedPane.setForeground(AdminTheme.TEXT_PRIMARY);
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AdminTheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 0, 10)
        ));

        // Add tabs with enhanced styling
        tabbedPane.addTab("👥 Users Management", usersPanel);
        tabbedPane.addTab("🏢 Groups Overview", groupsPanel);
        tabbedPane.addTab("📋 System Logs", messagesPanel);

        // Style each tab
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setBackgroundAt(i, AdminTheme.SURFACE_DARK);
            tabbedPane.setForegroundAt(i, AdminTheme.TEXT_PRIMARY);
        }
    }
}