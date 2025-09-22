package client.Admin.view.panels;

import client.Admin.view.styling.AdminTheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * Groups management panel component
 */
public class AdminGroupsPanel extends JPanel {

    private JButton refreshGroupsButton;
    private JButton deleteGroupButton;
    private JButton viewGroupMembersButton;

    private JTable groupsTable;
    private DefaultTableModel groupsTableModel;

    /**
     * Constructor
     */
    public AdminGroupsPanel() {
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
        // Action buttons
        refreshGroupsButton = AdminTheme.createStyledButton("🔄 Refresh Groups", AdminTheme.ACCENT_GREEN);
        viewGroupMembersButton = AdminTheme.createStyledButton("👁️ View Members", AdminTheme.ACCENT_BLUE);
        deleteGroupButton = AdminTheme.createStyledButton("🗑️ Delete Group", AdminTheme.ACCENT_RED);

        // Groups table
        String[] groupColumns = {"🆔 Group ID", "🏷️ Name", "👤 Creator", "👥 Members", "📅 Created"};
        groupsTableModel = new DefaultTableModel(groupColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        groupsTable = new JTable(groupsTableModel);
        AdminTheme.styleTable(groupsTable);
    }

    /**
     * Sets up the layout
     */
    private void setupLayout() {
        // Top panel with refresh button
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel with table
        JScrollPane groupsScrollPane = new JScrollPane(groupsTable);
        AdminTheme.styleScrollPane(groupsScrollPane);
        add(groupsScrollPane, BorderLayout.CENTER);

        // Bottom panel with action buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the top panel with refresh controls
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setBackground(AdminTheme.SURFACE_DARK);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AdminTheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        topPanel.add(refreshGroupsButton);
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

        bottomPanel.add(viewGroupMembersButton);
        bottomPanel.add(deleteGroupButton);

        return bottomPanel;
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Clears the groups table
     */
    public void clearTable() {
        groupsTableModel.setRowCount(0);
    }

    /**
     * Adds a group to the table
     */
    public void addGroup(String id, String name, String creator, int memberCount, String created) {
        Vector<Object> row = new Vector<>();
        row.add(id);
        row.add(name);
        row.add(creator);
        row.add(memberCount);
        row.add(created);
        groupsTableModel.addRow(row);
    }

    /**
     * Gets selected row index
     */
    public int getSelectedRow() {
        return groupsTable.getSelectedRow();
    }

    /**
     * Gets selected group ID
     */
    public String getSelectedGroupId() {
        int row = getSelectedRow();
        if (row >= 0) {
            return (String) groupsTableModel.getValueAt(row, 0);
        }
        return null;
    }

    // ==================== EVENT LISTENER METHODS ====================

    public void addRefreshListener(ActionListener listener) {
        refreshGroupsButton.addActionListener(listener);
    }

    public void addDeleteGroupListener(ActionListener listener) {
        deleteGroupButton.addActionListener(listener);
    }

    public void addViewMembersListener(ActionListener listener) {
        viewGroupMembersButton.addActionListener(listener);
    }
}