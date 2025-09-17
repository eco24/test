package client.Admin.view;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * Modernized Admin Client View - Professional Dark Theme UI
 */
public class AdminView extends JFrame {

    // Color scheme - Dark modern theme
    private static final Color BACKGROUND_DARK = new Color(26, 32, 44);
    private static final Color SURFACE_DARK = new Color(45, 55, 72);
    private static final Color SURFACE_LIGHT = new Color(74, 85, 104);
    private static final Color ACCENT_BLUE = new Color(56, 178, 172);
    private static final Color ACCENT_GREEN = new Color(72, 187, 120);
    private static final Color ACCENT_RED = new Color(245, 101, 101);
    private static final Color ACCENT_ORANGE = new Color(237, 137, 54);
    private static final Color TEXT_PRIMARY = new Color(237, 242, 247);
    private static final Color TEXT_SECONDARY = new Color(160, 174, 192);
    private static final Color BORDER_COLOR = new Color(74, 85, 104);

    // ==================== LOGIN PANEL COMPONENTS ====================
    private JPanel loginPanel;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    // ==================== MAIN PANEL COMPONENTS ====================
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;

    // ==================== USERS TAB COMPONENTS ====================
    private JPanel usersPanel;
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;
    private JButton refreshUsersButton;
    private JTextField searchUserField;
    private JButton searchUserButton;
    private JButton clearSearchButton;

    // ==================== GROUPS TAB COMPONENTS ====================
    private JPanel groupsPanel;
    private JTable groupsTable;
    private DefaultTableModel groupsTableModel;
    private JButton deleteGroupButton;
    private JButton viewGroupMembersButton;
    private JButton refreshGroupsButton;

    // ==================== MESSAGES TAB COMPONENTS ====================
    private JPanel messagesPanel;
    private JTextArea messagesLogArea;
    private JButton clearMessagesButton;
    private JButton exportMessagesButton;

    // ==================== MENU BAR ====================
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu viewMenu;
    private JMenu helpMenu;
    private JMenuItem logoutMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem refreshAllMenuItem;
    private JMenuItem aboutMenuItem;

    // ==================== STATUS BAR ====================
    private JLabel statusBarLabel;
    private JLabel connectionStatusLabel;

    /**
     * Constructor - Initializes the Admin View
     */
    public AdminView() {
        try {
            // Set system look and feel as base
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setupModernUI();
        initializeFrame();
        initializeComponents();
        setupLayout();
        showLoginPanel();
    }

    /**
     * Sets up modern UI properties
     */
    private void setupModernUI() {
        // Set modern UI properties
        try {
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumb", SURFACE_LIGHT);
            UIManager.put("ScrollBar.track", SURFACE_DARK);
        } catch (Exception ignored) {}
    }

    /**
     * Initializes the main frame properties
     */
    private void initializeFrame() {
        setTitle("Admin Control Center - Chat Application");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);

        // Set dark theme
        getContentPane().setBackground(BACKGROUND_DARK);

        // Try to set application icon
        try {
            setIconImage(createDefaultIcon());
        } catch (Exception e) {
            System.out.println("Could not set application icon");
        }
    }

    /**
     * Creates a simple default icon
     */
    private Image createDefaultIcon() {
        int size = 32;
        java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = icon.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ACCENT_BLUE);
        g2.fillRoundRect(4, 4, size-8, size-8, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("A", 12, 22);
        g2.dispose();
        return icon;
    }

    /**
     * Initializes all UI components
     */
    private void initializeComponents() {
        initializeLoginPanel();
        initializeMainPanel();
        initializeMenuBar();
        initializeStatusBar();
    }

    /**
     * Sets up the layout
     */
    private void setupLayout() {
        setLayout(new CardLayout());
        add(loginPanel, "LOGIN");
        add(mainPanel, "MAIN");
        setJMenuBar(menuBar);
    }

    /**
     * Initializes the modern login panel
     */
    private void initializeLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setLayout(new BorderLayout());
        loginPanel.setBackground(BACKGROUND_DARK);

        // Center container
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_DARK);

        // Login card
        JPanel loginCard = new JPanel(new GridBagLayout());
        loginCard.setPreferredSize(new Dimension(450, 400));
        loginCard.setBackground(SURFACE_DARK);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Admin Control Center", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        loginCard.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Secure Administrator Access", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        loginCard.add(subtitleLabel, gbc);

        // Email field
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 5, 10);
        JLabel emailLabel = new JLabel("Administrator Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setForeground(TEXT_PRIMARY);
        loginCard.add(emailLabel, gbc);

        gbc.gridy = 3;
        emailField = createStyledTextField();
        emailField.setText("admin@chat.com");
        emailField.setPreferredSize(new Dimension(300, 35));
        loginCard.add(emailField, gbc);

        // Password field
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 10, 5, 10);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(TEXT_PRIMARY);
        loginCard.add(passwordLabel, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 10, 15, 10);
        passwordField = createStyledPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 35));
        loginCard.add(passwordField, gbc);

        // Login button
        gbc.gridy = 6;
        gbc.insets = new Insets(15, 10, 15, 10);
        loginButton = createStyledButton("Login", ACCENT_BLUE);
        loginButton.setPreferredSize(new Dimension(300, 40));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginCard.add(loginButton, gbc);

        // Status label
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 10, 0, 10);
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ACCENT_RED);
        loginCard.add(statusLabel, gbc);

        centerPanel.add(loginCard);
        loginPanel.add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Creates styled text field
     */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(SURFACE_LIGHT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    /**
     * Creates styled password field
     */
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setBackground(SURFACE_LIGHT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    /**
     * Creates styled button
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);

        if (text.equals("Login")) {
            button.setForeground(Color.BLACK);
        } else {
            button.setForeground(Color.WHITE);
        }

        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Made font bold for better visibility

        // Enhanced hover effect with better visual feedback
        button.addMouseListener(new MouseAdapter() {
            Color originalColor = button.getBackground();
            Color originalForeground = button.getForeground();

            @Override
            public void mouseEntered(MouseEvent e) {
                if (text.equals("Login")) {
                    button.setBackground(originalColor.brighter());
                    button.setForeground(Color.BLACK);
                } else {
                    button.setBackground(originalColor.brighter());
                    button.setForeground(Color.WHITE);
                }
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(originalColor.brighter().brighter(), 1),
                        BorderFactory.createEmptyBorder(9, 19, 9, 19)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
                button.setForeground(originalForeground);
                button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            }
        });

        return button;
    }

    /**
     * Initializes the main panel with tabs
     */
    private void initializeMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_DARK);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Made font bold
        tabbedPane.setBackground(SURFACE_DARK);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 0, 10)
        ));

        UIManager.put("TabbedPane.selected", ACCENT_BLUE);
        UIManager.put("TabbedPane.selectedForeground", Color.BLACK); // Black text on selected tabs
        UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.background", SURFACE_DARK);

        // Initialize tabs
        initializeUsersTab();
        initializeGroupsTab();
        initializeMessagesTab();

        // Add tabs with enhanced styling
        tabbedPane.addTab("👥 Users Management", usersPanel);
        tabbedPane.addTab("🏢 Groups Overview", groupsPanel);
        tabbedPane.addTab("📋 System Logs", messagesPanel);

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setBackgroundAt(i, SURFACE_DARK);
            tabbedPane.setForegroundAt(i, TEXT_PRIMARY);
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Initializes the Users management tab
     */
    private void initializeUsersTab() {
        usersPanel = new JPanel(new BorderLayout(10, 10));
        usersPanel.setBackground(BACKGROUND_DARK);
        usersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setBackground(SURFACE_DARK);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel searchLabel = new JLabel("🔍 Search Users:");
        searchLabel.setForeground(TEXT_PRIMARY);
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topPanel.add(searchLabel);

        searchUserField = createStyledTextField();
        searchUserField.setPreferredSize(new Dimension(200, 35)); // Slightly taller
        topPanel.add(searchUserField);

        searchUserButton = createStyledButton("Search", ACCENT_BLUE);
        topPanel.add(searchUserButton);

        clearSearchButton = createStyledButton("Clear", SURFACE_LIGHT);
        topPanel.add(clearSearchButton);

        topPanel.add(Box.createHorizontalStrut(20));

        refreshUsersButton = createStyledButton("🔄 Refresh", ACCENT_GREEN);
        topPanel.add(refreshUsersButton);

        usersPanel.add(topPanel, BorderLayout.NORTH);

        // Users table with enhanced styling
        String[] userColumns = {"📧 Email", "👤 Name", "🔐 Admin", "📅 Created", "🟢 Status"};
        usersTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2) return Boolean.class;
                return String.class;
            }
        };

        usersTable = new JTable(usersTableModel);
        styleTable(usersTable);

        JScrollPane usersScrollPane = new JScrollPane(usersTable);
        styleScrollPane(usersScrollPane);
        usersPanel.add(usersScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        bottomPanel.setBackground(SURFACE_DARK);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        addUserButton = createStyledButton("➕ Add User", ACCENT_GREEN);
        editUserButton = createStyledButton("✏️ Edit User", ACCENT_ORANGE);
        deleteUserButton = createStyledButton("🗑️ Delete User", ACCENT_RED);

        bottomPanel.add(addUserButton);
        bottomPanel.add(editUserButton);
        bottomPanel.add(deleteUserButton);

        usersPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Initializes the Groups management tab
     */
    private void initializeGroupsTab() {
        groupsPanel = new JPanel(new BorderLayout(10, 10));
        groupsPanel.setBackground(BACKGROUND_DARK);
        groupsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setBackground(SURFACE_DARK);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        refreshGroupsButton = createStyledButton("🔄 Refresh Groups", ACCENT_GREEN);
        topPanel.add(refreshGroupsButton);

        groupsPanel.add(topPanel, BorderLayout.NORTH);

        // Groups table with enhanced column headers
        String[] groupColumns = {"🆔 Group ID", "🏷️ Name", "👤 Creator", "👥 Members", "📅 Created"};
        groupsTableModel = new DefaultTableModel(groupColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        groupsTable = new JTable(groupsTableModel);
        styleTable(groupsTable);

        JScrollPane groupsScrollPane = new JScrollPane(groupsTable);
        styleScrollPane(groupsScrollPane);
        groupsPanel.add(groupsScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        bottomPanel.setBackground(SURFACE_DARK);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        viewGroupMembersButton = createStyledButton("👁️ View Members", ACCENT_BLUE);
        deleteGroupButton = createStyledButton("🗑️ Delete Group", ACCENT_RED);

        bottomPanel.add(viewGroupMembersButton);
        bottomPanel.add(deleteGroupButton);

        groupsPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Initializes the Messages log tab
     */
    private void initializeMessagesTab() {
        messagesPanel = new JPanel(new BorderLayout(10, 10));
        messagesPanel.setBackground(BACKGROUND_DARK);
        messagesPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        messagesLogArea = new JTextArea();
        messagesLogArea.setEditable(false);
        messagesLogArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        messagesLogArea.setBackground(SURFACE_DARK);
        messagesLogArea.setForeground(TEXT_PRIMARY);
        messagesLogArea.setCaretColor(ACCENT_BLUE);
        messagesLogArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JScrollPane scrollPane = new JScrollPane(messagesLogArea);
        styleScrollPane(scrollPane);
        messagesPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        bottomPanel.setBackground(SURFACE_DARK);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        clearMessagesButton = createStyledButton("🗑️ Clear Log", ACCENT_RED);
        exportMessagesButton = createStyledButton("📤 Export Log", ACCENT_BLUE);

        bottomPanel.add(clearMessagesButton);
        bottomPanel.add(exportMessagesButton);

        messagesPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Styles a table with modern dark theme
     */
    private void styleTable(JTable table) {
        table.setBackground(SURFACE_DARK);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ACCENT_BLUE);
        table.setSelectionForeground(Color.BLACK); // Black text on selected rows for better contrast
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(35); // Increased row height for better readability
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(SURFACE_LIGHT);
        header.setForeground(TEXT_PRIMARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        header.setPreferredSize(new Dimension(0, 40)); // Taller header
    }

    /**
     * Styles a scroll pane with modern dark theme
     */
    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(SURFACE_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getVerticalScrollBar().setBackground(SURFACE_DARK);
        scrollPane.getHorizontalScrollBar().setBackground(SURFACE_DARK);
    }

    /**
     * Initializes the menu bar
     */
    private void initializeMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(SURFACE_DARK);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // File menu
        fileMenu = createStyledMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        refreshAllMenuItem = createStyledMenuItem("Refresh All", KeyEvent.VK_R);
        refreshAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        fileMenu.add(refreshAllMenuItem);

        fileMenu.addSeparator();

        logoutMenuItem = createStyledMenuItem("Logout", KeyEvent.VK_L);
        logoutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        fileMenu.add(logoutMenuItem);

        exitMenuItem = createStyledMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        // View menu
        viewMenu = createStyledMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);

        // Help menu
        helpMenu = createStyledMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        aboutMenuItem = createStyledMenuItem("About", KeyEvent.VK_A);
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);
    }

    /**
     * Creates styled menu
     */
    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(TEXT_PRIMARY);
        menu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return menu;
    }

    /**
     * Creates styled menu item
     */
    private JMenuItem createStyledMenuItem(String text, int mnemonic) {
        JMenuItem item = new JMenuItem(text, mnemonic);
        item.setBackground(SURFACE_DARK);
        item.setForeground(TEXT_PRIMARY);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return item;
    }

    /**
     * Initializes the status bar
     */
    private void initializeStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(SURFACE_DARK);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, ACCENT_BLUE), // Blue accent border
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        statusPanel.setPreferredSize(new Dimension(0, 35)); // Slightly taller

        statusBarLabel = new JLabel("🚀 Ready");
        statusBarLabel.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Bold font
        statusBarLabel.setForeground(TEXT_PRIMARY); // Primary text color
        statusPanel.add(statusBarLabel, BorderLayout.WEST);

        connectionStatusLabel = new JLabel("❌ Disconnected ");
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        connectionStatusLabel.setForeground(ACCENT_RED);
        statusPanel.add(connectionStatusLabel, BorderLayout.EAST);

        mainPanel.add(statusPanel, BorderLayout.SOUTH);
    }

    // ==================== VIEW CONTROL METHODS ====================

    /**
     * Shows the login panel
     */
    public void showLoginPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "LOGIN");
        if (menuBar != null) menuBar.setVisible(false);
        if (passwordField != null) {
            passwordField.requestFocus();
        }
    }

    /**
     * Shows the main admin panel
     */
    public void showMainPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "MAIN");
        if (menuBar != null) menuBar.setVisible(true);
    }

    // ==================== DATA ACCESS METHODS ====================

    public String getLoginEmail() {
        return emailField.getText().trim();
    }

    public String getLoginPassword() {
        return new String(passwordField.getPassword());
    }

    public String getSearchText() {
        return searchUserField.getText().trim();
    }

    public JTextField getSearchUserField() {
        return searchUserField;
    }

    public void clearLoginFields() {
        passwordField.setText("");
        statusLabel.setText(" ");
    }

    public void setLoginStatus(String status) {
        statusLabel.setText(status);
        statusLabel.setForeground(status.isEmpty() ? TEXT_SECONDARY : ACCENT_RED);
    }

    public void setStatusBar(String status) {
        statusBarLabel.setText(" " + status);
    }

    public void setConnectionStatus(boolean connected) {
        if (connected) {
            connectionStatusLabel.setText("✅ Connected ");
            connectionStatusLabel.setForeground(ACCENT_GREEN);
        } else {
            connectionStatusLabel.setText("❌ Disconnected ");
            connectionStatusLabel.setForeground(ACCENT_RED);
        }
    }

    // ==================== TABLE METHODS ====================

    public void clearUsersTable() {
        usersTableModel.setRowCount(0);
    }

    public void addUserToTable(String email, String name, boolean isAdmin, String created, String status) {
        Vector<Object> row = new Vector<>();
        row.add(email);
        row.add(name);
        row.add(isAdmin);
        row.add(created);
        row.add(status);
        usersTableModel.addRow(row);
    }

    public void clearGroupsTable() {
        groupsTableModel.setRowCount(0);
    }

    public void addGroupToTable(String id, String name, String creator, int memberCount, String created) {
        Vector<Object> row = new Vector<>();
        row.add(id);
        row.add(name);
        row.add(creator);
        row.add(memberCount);
        row.add(created);
        groupsTableModel.addRow(row);
    }

    public int getSelectedUserRow() {
        return usersTable.getSelectedRow();
    }

    public int getSelectedGroupRow() {
        return groupsTable.getSelectedRow();
    }

    public String getSelectedUserEmail() {
        int row = getSelectedUserRow();
        if (row >= 0) {
            return (String) usersTableModel.getValueAt(row, 0);
        }
        return null;
    }

    public String getSelectedGroupId() {
        int row = getSelectedGroupRow();
        if (row >= 0) {
            return (String) groupsTableModel.getValueAt(row, 0);
        }
        return null;
    }

    public void appendToMessageLog(String message) {
        SwingUtilities.invokeLater(() -> {
            messagesLogArea.append(message + "\n");
            messagesLogArea.setCaretPosition(messagesLogArea.getDocument().getLength());
        });
    }

    public void clearMessageLog() {
        messagesLogArea.setText("");
    }

    public String getMessageLog() {
        return messagesLogArea.getText();
    }

    // ==================== EVENT LISTENER METHODS ====================

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
        passwordField.addActionListener(listener);
    }

    public void addLogoutListener(ActionListener listener) {
        logoutMenuItem.addActionListener(listener);
    }

    public void addExitListener(ActionListener listener) {
        exitMenuItem.addActionListener(listener);
    }

    public void addRefreshAllListener(ActionListener listener) {
        refreshAllMenuItem.addActionListener(listener);
    }

    public void addRefreshUsersListener(ActionListener listener) {
        refreshUsersButton.addActionListener(listener);
    }

    public void addRefreshGroupsListener(ActionListener listener) {
        refreshGroupsButton.addActionListener(listener);
    }

    public void addSearchUserListener(ActionListener listener) {
        searchUserButton.addActionListener(listener);
        searchUserField.addActionListener(listener);
    }

    public void addClearSearchListener(ActionListener listener) {
        clearSearchButton.addActionListener(listener);
    }

    public void addAddUserListener(ActionListener listener) {
        addUserButton.addActionListener(listener);
    }

    public void addEditUserListener(ActionListener listener) {
        editUserButton.addActionListener(listener);
    }

    public void addDeleteUserListener(ActionListener listener) {
        deleteUserButton.addActionListener(listener);
    }

    public void addDeleteGroupListener(ActionListener listener) {
        deleteGroupButton.addActionListener(listener);
    }

    public void addViewGroupMembersListener(ActionListener listener) {
        viewGroupMembersButton.addActionListener(listener);
    }

    public void addClearMessagesListener(ActionListener listener) {
        clearMessagesButton.addActionListener(listener);
    }

    public void addExportMessagesListener(ActionListener listener) {
        exportMessagesButton.addActionListener(listener);
    }

    public void addAboutListener(ActionListener listener) {
        aboutMenuItem.addActionListener(listener);
    }

    public void addWindowListener(WindowListener listener) {
        super.addWindowListener(listener);
    }

    // ==================== DIALOG METHODS ====================

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean showConfirmation(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public String showInputDialog(String message, String title) {
        return JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
    }
}
