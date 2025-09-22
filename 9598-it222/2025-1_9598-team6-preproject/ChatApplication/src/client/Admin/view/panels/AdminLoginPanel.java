package client.Admin.view.panels;

import client.Admin.view.styling.AdminTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Login panel component for the Admin application
 */
public class AdminLoginPanel extends JPanel {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    /**
     * Constructor
     */
    public AdminLoginPanel() {
        setLayout(new BorderLayout());
        setBackground(AdminTheme.BACKGROUND_DARK);

        initializeComponents();
        setupLayout();
    }

    /**
     * Initializes UI components
     */
    private void initializeComponents() {
        // Email field
        emailField = AdminTheme.createStyledTextField();
        emailField.setText("admin@chat.com");
        emailField.setPreferredSize(new Dimension(300, 35));

        // Password field
        passwordField = AdminTheme.createStyledPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 35));

        // Login button
        loginButton = AdminTheme.createStyledButton("Login", AdminTheme.ACCENT_BLUE);
        loginButton.setPreferredSize(new Dimension(300, 40));

        // Status label
        statusLabel = AdminTheme.createStyledLabel(" ", AdminTheme.FONT_STATUS, AdminTheme.ACCENT_RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Sets up the layout
     */
    private void setupLayout() {
        // Center container
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(AdminTheme.BACKGROUND_DARK);

        // Login card
        JPanel loginCard = createLoginCard();
        centerPanel.add(loginCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Creates the main login card
     */
    private JPanel createLoginCard() {
        JPanel loginCard = new JPanel(new GridBagLayout());
        loginCard.setPreferredSize(new Dimension(450, 400));
        loginCard.setBackground(AdminTheme.SURFACE_DARK);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AdminTheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = AdminTheme.getStandardInsets();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        // Title
        JLabel titleLabel = AdminTheme.createStyledLabel("Admin Control Center",
                AdminTheme.FONT_TITLE, AdminTheme.TEXT_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        loginCard.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = AdminTheme.createStyledLabel("Secure Administrator Access",
                AdminTheme.FONT_SUBTITLE, AdminTheme.TEXT_SECONDARY);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        loginCard.add(subtitleLabel, gbc);

        // Email label
        JLabel emailLabel = AdminTheme.createStyledLabel("Administrator Email:",
                AdminTheme.FONT_LABEL, AdminTheme.TEXT_PRIMARY);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 5, 10);
        loginCard.add(emailLabel, gbc);

        // Email field
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 10, 15, 10);
        loginCard.add(emailField, gbc);

        // Password label
        JLabel passwordLabel = AdminTheme.createStyledLabel("Password:",
                AdminTheme.FONT_LABEL, AdminTheme.TEXT_PRIMARY);
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 10, 5, 10);
        loginCard.add(passwordLabel, gbc);

        // Password field
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 10, 15, 10);
        loginCard.add(passwordField, gbc);

        // Login button
        gbc.gridy = 6;
        gbc.insets = new Insets(15, 10, 15, 10);
        loginCard.add(loginButton, gbc);

        // Status label
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 10, 0, 10);
        loginCard.add(statusLabel, gbc);

        return loginCard;
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Gets the email from the email field
     */
    public String getEmail() {
        return emailField.getText().trim();
    }

    /**
     * Gets the password from the password field
     */
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    /**
     * Clears login fields
     */
    public void clearFields() {
        passwordField.setText("");
        statusLabel.setText(" ");
    }

    /**
     * Sets login status message
     */
    public void setStatus(String status) {
        statusLabel.setText(status);
        statusLabel.setForeground(status.isEmpty() ? AdminTheme.TEXT_SECONDARY : AdminTheme.ACCENT_RED);
    }

    /**
     * Focuses the password field
     */
    public void focusPasswordField() {
        SwingUtilities.invokeLater(() -> passwordField.requestFocus());
    }

    /**
     * Adds login action listener
     */
    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
        passwordField.addActionListener(listener);
    }
}