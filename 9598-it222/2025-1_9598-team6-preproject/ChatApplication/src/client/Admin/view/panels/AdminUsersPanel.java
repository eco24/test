package client.Admin.view.panels;

import client.Admin.view.styling.AdminTheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * Users management panel component
 */
public class AdminUsersPanel extends JPanel {

    private JTextField searchUserField;
    private JButton searchUserButton;
    private JButton clearSearchButton;
    private JButton refreshUsersButton;
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;

    private JTable usersTable;
    private DefaultTableModel usersTableModel;

    /**
     * Constructor
     */
    public AdminUsersPanel() {
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
        // Search components
        searchUserField = AdminTheme.createStyledTextField();
        searchUserField.setPreferredSize(new Dimension(200, 35));

        searchUserButton = AdminTheme.createStyledButton("Search", AdminTheme.ACCENT_BLUE);
        clearSearchButton = AdminTheme.createStyledButton("Clear", AdminTheme.SURFACE_LIGHT);
        refreshUsersButton = AdminTheme.createStyledButton("🔄 Refresh", AdminTheme.ACCENT_GREEN);

        // Action buttons
        addUserButton = AdminTheme.createStyledButton("➕ Add User", AdminTheme.ACCENT_GREEN);
        editUserButton = AdminTheme.createStyledButton("✏️ Edit User", AdminTheme.ACCENT_ORANGE);
        deleteUserButton = AdminTheme.createStyledButton("🗑️ Delete User", AdminTheme.ACCENT_RED);

        // Users table
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
        AdminTheme.styleTable(usersTable);
    }

    /**
     * Sets up the layout
     */
    private void setupLayout() {
        // Top panel with search controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel with table
        JScrollPane usersScrollPane = new JScrollPane(usersTable);
        AdminTheme.styleScrollPane(usersScrollPane);
        add(usersScrollPane, BorderLayout.CENTER);

        // Bottom panel with action buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the top panel with search controls
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setBackground(AdminTheme.SURFACE_DARK);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AdminTheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel searchLabel = AdminTheme.createStyledLabel("🔍 Search Users:",
                AdminTheme.FONT_BUTTON, AdminTheme.TEXT_PRIMARY);

        topPanel.add(searchLabel);
        topPanel.add(searchUserField);
        topPanel.add(searchUserButton);
        topPanel.add(clearSearchButton);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(refreshUsersButton);

        return topPanel;
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

        bottomPanel.add(addUserButton);
        bottomPanel.add(editUserButton);
        bottomPanel.add(deleteUserButton);

        return bottomPanel;
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Gets search text
     */
    public String getSearchText() {
        return searchUserField.getText().trim();
    }

    /**
     * Gets search field reference
     */
    public JTextField getSearchField() {
        return searchUserField;
    }

    /**
     * Clears the users table
     */
    public void clearTable() {
        usersTableModel.setRowCount(0);
    }

    /**
     * Adds a user to the table
     */
    public void addUser(String email, String name, boolean isAdmin, String created, String status) {
        Vector<Object> row = new Vector<>();
        row.add(email);
        row.add(name);
        row.add(isAdmin);
        row.add(created);
        row.add(status);
        usersTableModel.addRow(row);
    }

    /**
     * Gets selected row index
     */
    public int getSelectedRow() {
        return usersTable.getSelectedRow();
    }

    /**
     * Gets selected user email
     */
    public String getSelectedUserEmail() {
        int row = getSelectedRow();
        if (row >= 0) {
            return (String) usersTableModel.getValueAt(row, 0);
        }
        return null;
    }

    // ==================== EVENT LISTENER METHODS ====================

    public void addSearchListener(ActionListener listener) {
        searchUserButton.addActionListener(listener);
        searchUserField.addActionListener(listener);
    }

    public void addClearSearchListener(ActionListener listener) {
        clearSearchButton.addActionListener(listener);
    }

    public void addRefreshListener(ActionListener listener) {
        refreshUsersButton.addActionListener(listener);
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
}