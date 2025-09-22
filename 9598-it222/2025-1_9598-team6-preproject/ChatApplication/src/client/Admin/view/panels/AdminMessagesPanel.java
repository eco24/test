package client.Admin.view.panels;

import client.Admin.view.styling.AdminTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Messages/logs panel component
 */
public class AdminMessagesPanel extends JPanel {

    private JTextArea messagesLogArea;
    private JButton clearMessagesButton;
    private JButton exportMessagesButton;

    /**
     * Constructor
     */
    public AdminMessagesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(AdminTheme.BACKGROUND_DARK);
        setBorder(AdminTheme.createBorderedPanel(AdminTheme.BACKGROUND_DARK).getBorder());

        initializeComponents();
        setupLayout();
    }

    /**
     * Initializes UI components
     */
    private void initializeComponents() {
        // Messages log area
        messagesLogArea = AdminTheme.createStyledTextArea();

        // Action buttons
        clearMessagesButton = AdminTheme.createStyledButton("🗑️ Clear Log", AdminTheme.ACCENT_RED);
        exportMessagesButton = AdminTheme.createStyledButton("📤 Export Log", AdminTheme.ACCENT_BLUE);
    }

    /**
     * Sets up the layout
     */
    private void setupLayout() {
        // Center panel with log area
        JScrollPane scrollPane = new JScrollPane(messagesLogArea);
        AdminTheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with action buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the bottom panel with action buttons
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        bottomPanel.setBackground(AdminTheme.SURFACE_DARK);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AdminTheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        bottomPanel.add(clearMessagesButton);
        bottomPanel.add(exportMessagesButton);

        return bottomPanel;
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Appends a message to the log
     */
    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messagesLogArea.append(message + "\n");
            messagesLogArea.setCaretPosition(messagesLogArea.getDocument().getLength());
        });
    }

    /**
     * Clears the message log
     */
    public void clearLog() {
        messagesLogArea.setText("");
    }

    /**
     * Gets the log content
     */
    public String getLogContent() {
        return messagesLogArea.getText();
    }

    // ==================== EVENT LISTENER METHODS ====================

    public void addClearMessagesListener(ActionListener listener) {
        clearMessagesButton.addActionListener(listener);
    }

    public void addExportMessagesListener(ActionListener listener) {
        exportMessagesButton.addActionListener(listener);
    }
}