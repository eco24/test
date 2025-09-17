package client.user.view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.awt.RenderingHints;
import java.awt.GradientPaint;

/**
 * User Client View - GUI Implementation
 * Provides chat interface following MVC pattern
 */
public class UserView extends JFrame {

    // ==================== LOGIN PANEL COMPONENTS ====================
    private JPanel loginPanel;
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JButton loginButton;
    private JButton showRegisterButton;
    private JLabel loginStatusLabel;

    // ==================== REGISTRATION PANEL COMPONENTS ====================
    private JPanel registerPanel;
    private JTextField regEmailField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JTextField regNameField;
    private JButton registerButton;
    private JButton backToLoginButton;
    private JLabel regStatusLabel;

    // ==================== MAIN CHAT PANEL COMPONENTS ====================
    private JPanel mainPanel;

    // User info panel
    private JLabel userNameLabel;
    private JLabel userStatusLabel;
    private JButton logoutButton;

    // Contacts panel
    private JList<String> contactsList;
    private DefaultListModel<String> contactsListModel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton addFriendButton;
    private JButton createGroupButton;
    private JButton refreshButton;
    private JButton inviteToGroupButton;
    private JButton kickFromGroupButton;
    private JButton groupSettingsButton;

    // Chat area
    private JTextPane chatArea;
    private StyledDocument chatDocument;
    private JTextField messageField;
    private JButton sendButton;
    private JButton attachButton;
    private JLabel typingLabel;
    private JLabel currentChatLabel;

    // Friend requests panel
    private JPanel friendRequestsPanel;
    private DefaultListModel<String> friendRequestsModel;
    private JList<String> friendRequestsList;

    private JPanel groupInvitesPanel;
    private DefaultListModel<String> groupInvitesModel;
    private JList<String> groupInvitesList;

    // Status bar
    private JLabel statusBarLabel;
    private JLabel connectionStatusLabel;

    // Current chat context
    private String currentChatType = "broadcast";
    private String currentChatTarget = null;
    private String currentGroupCreator = null;

    // Styles for chat formatting
    private SimpleAttributeSet myMessageStyle;
    private SimpleAttributeSet otherMessageStyle;
    private SimpleAttributeSet systemMessageStyle;
    private SimpleAttributeSet timestampStyle;

    /**
     * Constructor - Initializes the User View
     */
    public UserView() {
        initializeFrame();
        initializeStyles();
        initializeComponents();
        setupLayout();
        showLoginPanel();
    }

    /**
     * Initializes the main frame properties
     */
    private void initializeFrame() {
        setTitle("Chat Application");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 700);
        setMinimumSize(new Dimension(750, 550));
        setLocationRelativeTo(null);

        // Set application icon if available
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/chat-icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Icon not found, continue without it
        }
    }

    /**
     * Initializes text styles for chat formatting
     */
    private void initializeStyles() {
        myMessageStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(myMessageStyle, new Color(0, 102, 204));
        StyleConstants.setBold(myMessageStyle, true);

        otherMessageStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(otherMessageStyle, new Color(51, 51, 51));

        systemMessageStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(systemMessageStyle, new Color(128, 128, 128));
        StyleConstants.setItalic(systemMessageStyle, true);

        timestampStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(timestampStyle, new Color(160, 160, 160));
        StyleConstants.setFontSize(timestampStyle, 10);
    }

    /**
     * Initializes all UI components
     */
    private void initializeComponents() {
        initializeLoginPanel();
        initializeRegisterPanel();
        initializeMainPanel();
    }

    /**
     * Sets up the layout
     */
    private void setupLayout() {
        setLayout(new CardLayout());
        add(loginPanel, "LOGIN");
        add(registerPanel, "REGISTER");
        add(mainPanel, "MAIN");
    }

    /**
     * Initializes the login panel
     */
    private void initializeLoginPanel() {
        loginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(138, 43, 226),  // Blue violet
                        0, getHeight(), new Color(75, 0, 130)  // Indigo
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        loginPanel.setLayout(new GridBagLayout());

        JPanel loginBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark card background
                g2d.setColor(new Color(45, 55, 72, 240)); // Dark blue-gray with transparency
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        loginBox.setLayout(new GridBagLayout());
        loginBox.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        loginBox.setPreferredSize(new Dimension(400, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userIcon = new JLabel("👤", SwingConstants.CENTER);
        userIcon.setFont(new Font("Dialog", Font.PLAIN, 48));
        userIcon.setForeground(new Color(56, 178, 172)); // Teal color
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        loginBox.add(userIcon, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel emailPanel = new JPanel(new BorderLayout());
        emailPanel.setOpaque(false);
        JLabel emailIcon = new JLabel("📧 ");
        emailIcon.setForeground(new Color(160, 174, 192));
        loginEmailField = new JTextField(20);
        loginEmailField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginEmailField.setBackground(new Color(74, 85, 104));
        loginEmailField.setForeground(Color.WHITE);
        loginEmailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(113, 128, 150), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        loginEmailField.setCaretColor(Color.WHITE);
        emailPanel.add(emailIcon, BorderLayout.WEST);
        emailPanel.add(loginEmailField, BorderLayout.CENTER);

        gbc.gridwidth = 2;
        loginBox.add(emailPanel, gbc);

        gbc.gridy = 2;
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setOpaque(false);
        JLabel passwordIcon = new JLabel("🔒 ");
        passwordIcon.setForeground(new Color(160, 174, 192));
        loginPasswordField = new JPasswordField(20);
        loginPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPasswordField.setBackground(new Color(74, 85, 104));
        loginPasswordField.setForeground(Color.WHITE);
        loginPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(113, 128, 150), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        loginPasswordField.setCaretColor(Color.WHITE);
        passwordPanel.add(passwordIcon, BorderLayout.WEST);
        passwordPanel.add(loginPasswordField, BorderLayout.CENTER);

        loginBox.add(passwordPanel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(30, 10, 10, 10);
        loginButton = new JButton("LOG IN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Bright pink/magenta gradient like reference
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(236, 72, 153),  // Pink
                        0, getHeight(), new Color(219, 39, 119)  // Darker pink
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Button text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(getText(), x, y);
            }
        };
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(320, 45));
        loginBox.add(loginButton, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 10, 10);
        showRegisterButton = new JButton("Create Account");
        showRegisterButton.setFont(new Font("Arial", Font.PLAIN, 14));
        showRegisterButton.setForeground(new Color(160, 174, 192));
        showRegisterButton.setFocusPainted(false);
        showRegisterButton.setBorderPainted(false);
        showRegisterButton.setContentAreaFilled(false);
        showRegisterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBox.add(showRegisterButton, gbc);

        // Status label
        gbc.gridy = 5;
        loginStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        loginStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        loginStatusLabel.setForeground(Color.WHITE);
        loginBox.add(loginStatusLabel, gbc);

        loginPanel.add(loginBox);
    }

    /**
     * Initializes the registration panel
     */
    private void initializeRegisterPanel() {
        registerPanel = new JPanel();
        registerPanel.setLayout(new GridBagLayout());
        registerPanel.setBackground(new Color(245, 245, 245));

        JPanel registerBox = new JPanel();
        registerBox.setLayout(new GridBagLayout());
        registerBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                        "Create New Account",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 16),
                        new Color(33, 150, 243)
                ),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        registerBox.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name field
        gbc.gridy = 0;
        gbc.gridx = 0;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerBox.add(nameLabel, gbc);

        gbc.gridx = 1;
        regNameField = new JTextField(20);
        regNameField.setFont(new Font("Arial", Font.PLAIN, 14));
        registerBox.add(regNameField, gbc);

        // Email field
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerBox.add(emailLabel, gbc);

        gbc.gridx = 1;
        regEmailField = new JTextField(20);
        regEmailField.setFont(new Font("Arial", Font.PLAIN, 14));
        registerBox.add(regEmailField, gbc);

        // Password field
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerBox.add(passwordLabel, gbc);

        gbc.gridx = 1;
        regPasswordField = new JPasswordField(20);
        regPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        registerBox.add(regPasswordField, gbc);

        // Confirm password field
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerBox.add(confirmLabel, gbc);

        gbc.gridx = 1;
        regConfirmPasswordField = new JPasswordField(20);
        regConfirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        registerBox.add(regConfirmPasswordField, gbc);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setBackground(Color.WHITE);

        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBackground(new Color(76, 175, 80));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setPreferredSize(new Dimension(100, 35));

        backToLoginButton = new JButton("Back");
        backToLoginButton.setFont(new Font("Arial", Font.BOLD, 14));
        backToLoginButton.setBackground(new Color(158, 158, 158));
        backToLoginButton.setForeground(Color.WHITE);
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLoginButton.setPreferredSize(new Dimension(100, 35));

        buttonsPanel.add(registerButton);
        buttonsPanel.add(backToLoginButton);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        registerBox.add(buttonsPanel, gbc);

        // Status label
        gbc.gridy = 5;
        regStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        regStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        regStatusLabel.setForeground(Color.RED);
        registerBox.add(regStatusLabel, gbc);

        registerPanel.add(registerBox);
    }

    /**
     * Initializes the main chat panel
     */
    private void initializeMainPanel() {
        mainPanel = new JPanel(new BorderLayout());

        // Top panel with user info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topPanel.setBackground(new Color(0, 150, 136));

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.setOpaque(false);

        userNameLabel = new JLabel("Welcome!");
        userNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userNameLabel.setForeground(Color.WHITE);
        userInfoPanel.add(userNameLabel);

        userInfoPanel.add(Box.createHorizontalStrut(20));

        userStatusLabel = new JLabel("● Online");
        userStatusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userStatusLabel.setForeground(Color.WHITE);
        userInfoPanel.add(userStatusLabel);

        topPanel.add(userInfoPanel, BorderLayout.WEST);

        JPanel topButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topButtonsPanel.setOpaque(false);

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(0, 150, 136));
        topButtonsPanel.add(logoutButton);

        topPanel.add(topButtonsPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Split pane for contacts and chat
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);

        // Left panel - Contacts
        JPanel leftPanel = createContactsPanel();

        // Right panel - Chat area
        JPanel rightPanel = createChatPanel();

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        statusBarLabel = new JLabel(" Ready");
        statusBarLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusPanel.add(statusBarLabel, BorderLayout.WEST);

        connectionStatusLabel = new JLabel("Connected ");
        connectionStatusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        connectionStatusLabel.setForeground(new Color(76, 175, 80));
        statusPanel.add(connectionStatusLabel, BorderLayout.EAST);

        mainPanel.add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the contacts panel
     */
    private JPanel createContactsPanel() {
        JPanel contactsPanel = new JPanel(new BorderLayout());
        contactsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchPanel.add(searchField, BorderLayout.CENTER);

        searchButton = new JButton("🔍");
        searchButton.setPreferredSize(new Dimension(40, 25));
        searchButton.setBackground(new Color(0, 150, 136));
        searchButton.setForeground(Color.BLACK);
        searchButton.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 136), 2));
        searchPanel.add(searchButton, BorderLayout.EAST);

        contactsPanel.add(searchPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Contacts list
        contactsListModel = new DefaultListModel<>();
        contactsListModel.addElement("📢 General Chatroom");

        contactsList = new JList<>(contactsListModel);
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.setFont(new Font("Arial", Font.PLAIN, 13));
        contactsList.setForeground(Color.BLACK);
        contactsList.setCellRenderer(new ContactsListRenderer());
        contactsList.setSelectedIndex(0);

        JScrollPane contactsScrollPane = new JScrollPane(contactsList);
        tabbedPane.addTab("Contacts", contactsScrollPane);

        // Friend requests
        friendRequestsPanel = new JPanel(new BorderLayout());
        friendRequestsModel = new DefaultListModel<>();
        friendRequestsList = new JList<>(friendRequestsModel);
        friendRequestsList.setFont(new Font("Arial", Font.PLAIN, 13));

        JScrollPane requestsScrollPane = new JScrollPane(friendRequestsList);
        friendRequestsPanel.add(requestsScrollPane, BorderLayout.CENTER);

        JPanel requestButtonsPanel = new JPanel(new FlowLayout());
        JButton acceptButton = new JButton("✓ Accept");
        JButton rejectButton = new JButton("✗ Reject");
        acceptButton.setFont(new Font("Arial", Font.PLAIN, 11));
        rejectButton.setFont(new Font("Arial", Font.PLAIN, 11));
        acceptButton.setBackground(new Color(76, 175, 80));
        acceptButton.setForeground(Color.BLACK);
        acceptButton.setBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2));
        rejectButton.setBackground(new Color(244, 67, 54));
        rejectButton.setForeground(Color.BLACK);
        rejectButton.setBorder(BorderFactory.createLineBorder(new Color(244, 67, 54), 2));
        requestButtonsPanel.add(acceptButton);
        requestButtonsPanel.add(rejectButton);
        friendRequestsPanel.add(requestButtonsPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Requests", friendRequestsPanel);

        groupInvitesPanel = new JPanel(new BorderLayout());
        groupInvitesModel = new DefaultListModel<>();
        groupInvitesList = new JList<>(groupInvitesModel);
        groupInvitesList.setFont(new Font("Arial", Font.PLAIN, 13));

        JScrollPane invitesScrollPane = new JScrollPane(groupInvitesList);
        groupInvitesPanel.add(invitesScrollPane, BorderLayout.CENTER);

        JPanel inviteButtonsPanel = new JPanel(new FlowLayout());
        JButton joinGroupButton = new JButton("✓ Join");
        JButton rejectGroupButton = new JButton("✗ Reject");
        joinGroupButton.setFont(new Font("Arial", Font.PLAIN, 11));
        rejectGroupButton.setFont(new Font("Arial", Font.PLAIN, 11));
        joinGroupButton.setBackground(new Color(0, 150, 136));
        joinGroupButton.setForeground(Color.BLACK);
        joinGroupButton.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 136), 2));
        rejectGroupButton.setBackground(new Color(244, 67, 54));
        rejectGroupButton.setForeground(Color.BLACK);
        rejectGroupButton.setBorder(BorderFactory.createLineBorder(new Color(244, 67, 54), 2));
        inviteButtonsPanel.add(joinGroupButton);
        inviteButtonsPanel.add(rejectGroupButton);
        groupInvitesPanel.add(inviteButtonsPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Group Invites", groupInvitesPanel);

        contactsPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        addFriendButton = new JButton("➕ Add Friend");
        createGroupButton = new JButton("👥 Create Group");
        refreshButton = new JButton("🔄 Refresh");
        inviteToGroupButton = new JButton("📧 Invite to Group");
        kickFromGroupButton = new JButton("🚫 Kick User");
        groupSettingsButton = new JButton("⚙️ Group Settings");

        addFriendButton.setFont(new Font("Arial", Font.PLAIN, 11));
        addFriendButton.setBackground(new Color(33, 150, 243));
        addFriendButton.setForeground(Color.BLACK);
        addFriendButton.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 2));

        createGroupButton.setFont(new Font("Arial", Font.PLAIN, 11));
        createGroupButton.setBackground(new Color(0, 150, 136));
        createGroupButton.setForeground(Color.BLACK);
        createGroupButton.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 136), 2));

        refreshButton.setFont(new Font("Arial", Font.PLAIN, 11));
        refreshButton.setBackground(new Color(158, 158, 158));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setBorder(BorderFactory.createLineBorder(new Color(158, 158, 158), 2));

        inviteToGroupButton.setFont(new Font("Arial", Font.PLAIN, 11));
        inviteToGroupButton.setBackground(new Color(255, 193, 7));
        inviteToGroupButton.setForeground(Color.BLACK);
        inviteToGroupButton.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7), 2));
        inviteToGroupButton.setEnabled(false); // Initially disabled

        kickFromGroupButton.setFont(new Font("Arial", Font.PLAIN, 11));
        kickFromGroupButton.setBackground(new Color(244, 67, 54));
        kickFromGroupButton.setForeground(Color.BLACK);
        kickFromGroupButton.setBorder(BorderFactory.createLineBorder(new Color(244, 67, 54), 2));
        kickFromGroupButton.setEnabled(false); // Initially disabled

        groupSettingsButton.setFont(new Font("Arial", Font.PLAIN, 11));
        groupSettingsButton.setBackground(new Color(156, 39, 176));
        groupSettingsButton.setForeground(Color.BLACK);
        groupSettingsButton.setBorder(BorderFactory.createLineBorder(new Color(156, 39, 176), 2));
        groupSettingsButton.setEnabled(false); // Initially disabled

        bottomPanel.add(addFriendButton);
        bottomPanel.add(createGroupButton);
        bottomPanel.add(refreshButton);
        bottomPanel.add(inviteToGroupButton);
        bottomPanel.add(kickFromGroupButton);
        bottomPanel.add(groupSettingsButton);

        contactsPanel.add(bottomPanel, BorderLayout.SOUTH);

        return contactsPanel;
    }

    /**
     * Creates the chat panel
     */
    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 150, 136), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        headerPanel.setBackground(new Color(240, 240, 240));

        currentChatLabel = new JLabel("General Chatroom");
        currentChatLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(currentChatLabel, BorderLayout.WEST);

        typingLabel = new JLabel(" ");
        typingLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        typingLabel.setForeground(Color.GRAY);
        headerPanel.add(typingLabel, BorderLayout.CENTER);

        chatPanel.add(headerPanel, BorderLayout.NORTH);

        // Chat area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 13));
        chatDocument = chatArea.getStyledDocument();

        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 136), 2));
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 13));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 150, 136), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        inputPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        attachButton = new JButton("📎");
        attachButton.setPreferredSize(new Dimension(40, 25));
        attachButton.setToolTipText("Attach file");
        attachButton.setBackground(new Color(158, 158, 158));
        attachButton.setForeground(Color.BLACK);
        attachButton.setBorder(BorderFactory.createLineBorder(new Color(158, 158, 158), 2));
        buttonPanel.add(attachButton);

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setBackground(new Color(0, 150, 136));
        sendButton.setForeground(Color.BLACK);
        sendButton.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 136), 2));
        sendButton.setPreferredSize(new Dimension(80, 25));
        buttonPanel.add(sendButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        return chatPanel;
    }

    /**
     * Custom renderer for contacts list
     */
    private class ContactsListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            String text = value.toString();
            if (text.startsWith("📢")) {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            } else if (text.contains("👥")) {
                label.setForeground(new Color(0, 9, 18));
            } else if (text.contains("●")) {
                label.setForeground(new Color(1, 12, 1));
            } else if (text.contains("○")) {
                label.setForeground(Color.black);
            }

            return label;
        }
    }

    // ==================== VIEW CONTROL METHODS ====================

    public void showLoginPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "LOGIN");
        loginEmailField.requestFocus();
    }

    public void showRegisterPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "REGISTER");
        regNameField.requestFocus();
    }

    public void showMainPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "MAIN");
        messageField.requestFocus();
    }

    // ==================== DATA ACCESS METHODS ====================

    // Login panel getters
    public String getLoginEmail() {
        return loginEmailField.getText().trim();
    }

    public String getLoginPassword() {
        return new String(loginPasswordField.getPassword());
    }

    public void clearLoginFields() {
        loginEmailField.setText("");
        loginPasswordField.setText("");
        loginStatusLabel.setText(" ");
    }

    public void setLoginStatus(String status) {
        loginStatusLabel.setText(status);
    }

    // Registration panel getters
    public String getRegName() {
        return regNameField.getText().trim();
    }

    public String getRegEmail() {
        return regEmailField.getText().trim();
    }

    public String getRegPassword() {
        return new String(regPasswordField.getPassword());
    }

    public String getRegConfirmPassword() {
        return new String(regConfirmPasswordField.getPassword());
    }

    public void clearRegisterFields() {
        regNameField.setText("");
        regEmailField.setText("");
        regPasswordField.setText("");
        regConfirmPasswordField.setText("");
        regStatusLabel.setText(" ");
    }

    public void setRegisterStatus(String status) {
        regStatusLabel.setText(status);
    }

    // Main panel methods
    public void setUserName(String name) {
        userNameLabel.setText("Welcome, " + name + "!");
    }

    public void setUserStatus(String status) {
        userStatusLabel.setText(status);
    }

    public String getMessage() {
        return messageField.getText().trim();
    }

    public void clearMessage() {
        messageField.setText("");
    }

    public String getSearchText() {
        return searchField.getText().trim();
    }

    public void clearSearch() {
        searchField.setText("");
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public String getSelectedContact() {
        return contactsList.getSelectedValue();
    }

    public String getSelectedFriendRequest() {
        return friendRequestsList.getSelectedValue();
    }

    public void setCurrentChat(String chatName) {
        currentChatLabel.setText(chatName);
    }

    public void setTypingStatus(String status) {
        typingLabel.setText(status);
    }

    public void setStatusBar(String status) {
        statusBarLabel.setText(" " + status);
    }

    public void setConnectionStatus(boolean connected) {
        if (connected) {
            connectionStatusLabel.setText("Connected ");
            connectionStatusLabel.setForeground(new Color(76, 175, 80));
        } else {
            connectionStatusLabel.setText("Disconnected ");
            connectionStatusLabel.setForeground(Color.RED);
        }
    }

    // ==================== CHAT DISPLAY METHODS ====================

    /**
     * Appends a message to the chat area
     */
    public void appendMessage(String sender, String message, boolean isMyMessage, boolean isSystem) {
        try {
            // Add timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String timestamp = "[" + sdf.format(new Date()) + "] ";
            chatDocument.insertString(chatDocument.getLength(), timestamp, timestampStyle);

            if (isSystem) {
                // System message
                chatDocument.insertString(chatDocument.getLength(), message + "\n", systemMessageStyle);
            } else {
                // Regular message
                SimpleAttributeSet style = isMyMessage ? myMessageStyle : otherMessageStyle;
                chatDocument.insertString(chatDocument.getLength(), sender + ": ", style);
                chatDocument.insertString(chatDocument.getLength(), message + "\n", null);
            }

            // Scroll to bottom
            chatArea.setCaretPosition(chatDocument.getLength());

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public DefaultListModel<String> getContactsListModel() {
        return contactsListModel;
    }


    /**
     * Clears the chat area
     */
//    public void clearChat() {
//        chatArea.setText("");
//    }

    // ==================== CONTACTS MANAGEMENT ====================

    /**
     * Adds a contact to the list
     */

    public void addContact(String contact, boolean isOnline) {
        if (contact == null || contact.isEmpty()) {
            System.err.println("[VIEW] Attempted to add empty contact");
            return;
        }

        String status = isOnline ? "●" : "○";
        String displayString = status + " " + contact;

        // Check if already exists
        if (!contactsListModel.contains(displayString)) {
            contactsListModel.addElement(displayString);
            System.out.println("[VIEW] Added contact to list: " + displayString);
        } else {
            System.out.println("[VIEW] Contact already in list: " + displayString);
        }
    }

    /**
     * Adds a group to the list
     */
    public void addGroup(String groupName) {
        if (!contactsListModel.contains("👥 " + groupName)) {
            contactsListModel.addElement("👥 " + groupName);
        }
    }

    /**
     * Updates contact online status
     */
    public void updateContactStatus(String contact, boolean isOnline) {
        String oldStatus = isOnline ? "○" : "●";
        String newStatus = isOnline ? "●" : "○";

        for (int i = 0; i < contactsListModel.size(); i++) {
            String item = contactsListModel.get(i);
            if (item.equals(oldStatus + " " + contact)) {
                contactsListModel.set(i, newStatus + " " + contact);
                break;
            }
        }
    }

    /**
     * Clears all contacts except general chatroom
     */
    public void clearContacts() {
        contactsListModel.clear();
        contactsListModel.addElement("📢 General Chatroom");
    }

    /**
     * Adds a friend request
     */
    public void addFriendRequest(String fromUser) {
        if (!friendRequestsModel.contains(fromUser)) {
            friendRequestsModel.addElement(fromUser);
            // Update tab title to show count
            Component comp = ((JTabbedPane)friendRequestsPanel.getParent()).getTabComponentAt(1);
            if (comp != null) {
                ((JTabbedPane)friendRequestsPanel.getParent()).setTitleAt(1,
                        "Requests (" + friendRequestsModel.size() + ")");
            }
        }
    }

    /**
     * Removes a friend request
     */
    public void removeFriendRequest(String fromUser) {
        friendRequestsModel.removeElement(fromUser);
        // Update tab title
        Component comp = ((JTabbedPane)friendRequestsPanel.getParent()).getTabComponentAt(1);
        if (comp != null) {
            String title = friendRequestsModel.isEmpty() ? "Requests" :
                    "Requests (" + friendRequestsModel.size() + ")";
            ((JTabbedPane)friendRequestsPanel.getParent()).setTitleAt(1, title);
        }
    }

    // ==================== GROUP MANAGEMENT METHODS ====================

    /**
     * Sets current chat context and updates group management buttons
     */
    public void setCurrentChatContext(String type, String target, String groupCreator) {
        this.currentChatType = type;
        this.currentChatTarget = target;
        this.currentGroupCreator = groupCreator;

        // Update group management buttons based on context
        boolean isGroup = "group".equals(type);
        boolean isCreator = groupCreator != null && groupCreator.equals(getCurrentUserEmail());

        inviteToGroupButton.setEnabled(isGroup);
        kickFromGroupButton.setEnabled(isGroup && isCreator);
        groupSettingsButton.setEnabled(isGroup && isCreator);
    }

    /**
     * Gets current user email (to be set by controller)
     */
    private String currentUserEmail = "";

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    /**
     * Adds a group invite
     */
    public void addGroupInvite(String groupName, String inviter) {
        String inviteText = groupName + " (from " + inviter + ")";
        if (!groupInvitesModel.contains(inviteText)) {
            groupInvitesModel.addElement(inviteText);
            // Update tab title to show count
            updateGroupInvitesTabTitle();
        }
    }

    /**
     * Removes a group invite
     */
    public void removeGroupInvite(String groupName, String inviter) {
        String inviteText = groupName + " (from " + inviter + ")";
        groupInvitesModel.removeElement(inviteText);
        updateGroupInvitesTabTitle();
    }

    /**
     * Updates group invites tab title with count
     */
    private void updateGroupInvitesTabTitle() {
        try {
            JTabbedPane tabbedPane = (JTabbedPane) groupInvitesPanel.getParent();
            String title = groupInvitesModel.isEmpty() ? "Group Invites" :
                    "Group Invites (" + groupInvitesModel.size() + ")";
            tabbedPane.setTitleAt(2, title);
        } catch (Exception e) {
            // Ignore if tab structure changes
        }
    }

    /**
     * Gets selected group invite
     */
    public String getSelectedGroupInvite() {
        return groupInvitesList.getSelectedValue();
    }

    // ==================== EVENT LISTENER METHODS ====================

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    public void addShowRegisterListener(ActionListener listener) {
        showRegisterButton.addActionListener(listener);
    }

    public void addRegisterListener(ActionListener listener) {
        // This would be for the actual register button in register panel
        if (registerPanel != null) {
            for (Component comp : registerPanel.getComponents()) {
                if (comp instanceof JButton && ((JButton)comp).getText().equals("Register")) {
                    ((JButton)comp).addActionListener(listener);
                    break;
                }
            }
        }
    }

    public void addBackToLoginListener(ActionListener listener) {
        // This would be for the back to login button in register panel
        if (registerPanel != null) {
            for (Component comp : registerPanel.getComponents()) {
                if (comp instanceof JButton && ((JButton)comp).getText().contains("Back")) {
                    ((JButton)comp).addActionListener(listener);
                    break;
                }
            }
        }
    }

    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }

    public void addSendListener(ActionListener listener) {
        sendButton.addActionListener(listener);
    }

    public void addAttachListener(ActionListener listener) {
        attachButton.addActionListener(listener);
    }

    public void addSearchListener(ActionListener listener) {
        searchButton.addActionListener(listener);
    }

    public void addAddFriendListener(ActionListener listener) {
        addFriendButton.addActionListener(listener);
    }

    public void addCreateGroupListener(ActionListener listener) {
        createGroupButton.addActionListener(listener);
    }

    public void addRefreshListener(ActionListener listener) {
        refreshButton.addActionListener(listener);
    }

    public void addContactSelectionListener(javax.swing.event.ListSelectionListener listener) {
        contactsList.addListSelectionListener(listener);
    }

    public void addAcceptFriendListener(ActionListener listener) {
        // Get the accept button from friend requests panel
        for (Component comp : ((JPanel)friendRequestsPanel.getComponent(1)).getComponents()) {
            if (comp instanceof JButton && ((JButton)comp).getText().contains("Accept")) {
                ((JButton)comp).addActionListener(listener);
                break;
            }
        }
    }

    public void addRejectFriendListener(ActionListener listener) {
        // Get the reject button from friend requests panel
        for (Component comp : ((JPanel)friendRequestsPanel.getComponent(1)).getComponents()) {
            if (comp instanceof JButton && ((JButton)comp).getText().contains("Reject")) {
                ((JButton)comp).addActionListener(listener);
                break;
            }
        }
    }

    public void addInviteToGroupListener(ActionListener listener) {
        inviteToGroupButton.addActionListener(listener);
    }

    public void addKickFromGroupListener(ActionListener listener) {
        kickFromGroupButton.addActionListener(listener);
    }

    public void addGroupSettingsListener(ActionListener listener) {
        groupSettingsButton.addActionListener(listener);
    }

    public void addJoinGroupListener(ActionListener listener) {
        // Get the join button from group invites panel
        for (Component comp : ((JPanel)groupInvitesPanel.getComponent(1)).getComponents()) {
            if (comp instanceof JButton && ((JButton)comp).getText().contains("Join")) {
                ((JButton)comp).addActionListener(listener);
                break;
            }
        }
    }

    public void addRejectGroupInviteListener(ActionListener listener) {
        // Get the reject button from group invites panel
        for (Component comp : ((JPanel)groupInvitesPanel.getComponent(1)).getComponents()) {
            if (comp instanceof JButton && ((JButton)comp).getText().contains("Reject")) {
                ((JButton)comp).addActionListener(listener);
                break;
            }
        }
    }

    public void addGroupInviteSelectionListener(javax.swing.event.ListSelectionListener listener) {
        groupInvitesList.addListSelectionListener(listener);
    }

    // ==================== DIALOG METHODS ====================

    public boolean showConfirmation(String message) {
        int result = JOptionPane.showConfirmDialog(this, message, "Confirmation", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    public String showGroupSettingsDialog(String currentName) {
        return JOptionPane.showInputDialog(this,
                "Enter new group name:",
                "Group Settings",
                JOptionPane.PLAIN_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public String showInputDialog(String title, String message) {
        return JOptionPane.showInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
