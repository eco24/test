package client.Admin.view;

import client.Admin.view.components.*;
import client.Admin.view.styling.AdminTheme;
import client.Admin.view.panels.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * Refactored Admin Client View - Main UI coordinator
 * Uses composition and separation of concerns for better maintainability
 */
public class AdminView extends JFrame {

    // UI Components
    private final AdminLoginPanel loginPanel;
    private final AdminMainPanel mainPanel;
    private final AdminMenuBar menuBar;
    private final AdminStatusBar statusBar;

    // Component access for external interaction
    private final AdminUsersPanel usersPanel;
    private final AdminGroupsPanel groupsPanel;
    private final AdminMessagesPanel messagesPanel;

    /**
     * Constructor - Initializes the Admin View
     */
    public AdminView() {
        // Apply modern UI setup
        AdminTheme.setupLookAndFeel();

        // Initialize frame properties
        initializeFrame();

        // Initialize UI components
        this.loginPanel = new AdminLoginPanel();
        this.usersPanel = new AdminUsersPanel();
        this.groupsPanel = new AdminGroupsPanel();
        this.messagesPanel = new AdminMessagesPanel();
        this.mainPanel = new AdminMainPanel(usersPanel, groupsPanel, messagesPanel);
        this.menuBar = new AdminMenuBar();
        this.statusBar = new AdminStatusBar();

        // Setup layout and show login
        setupLayout();
        showLoginPanel();
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

        getContentPane().setBackground(AdminTheme.BACKGROUND_DARK);

        try {
            setIconImage(AdminTheme.createApplicationIcon());
        } catch (Exception e) {
            System.out.println("Could not set application icon");
        }
    }

    /**
     * Sets up the main layout
     */
    private void setupLayout() {
        setLayout(new CardLayout());
        add(loginPanel, "LOGIN");
        add(mainPanel, "MAIN");
        setJMenuBar(menuBar);
    }

    // ==================== VIEW CONTROL METHODS ====================

    /**
     * Shows the login panel
     */
    public void showLoginPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "LOGIN");
        menuBar.setVisible(false);
        loginPanel.focusPasswordField();
    }

    /**
     * Shows the main admin panel
     */
    public void showMainPanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "MAIN");
        menuBar.setVisible(true);
    }

    // ==================== DATA ACCESS METHODS ====================

    public String getLoginEmail() {
        return loginPanel.getEmail();
    }

    public String getLoginPassword() {
        return loginPanel.getPassword();
    }

    public String getSearchText() {
        return usersPanel.getSearchText();
    }

    public JTextField getSearchUserField() {
        return usersPanel.getSearchField();
    }

    public void clearLoginFields() {
        loginPanel.clearFields();
    }

    public void setLoginStatus(String status) {
        loginPanel.setStatus(status);
    }

    public void setStatusBar(String status) {
        statusBar.setStatus(status);
    }

    public void setConnectionStatus(boolean connected) {
        statusBar.setConnectionStatus(connected);
    }

    // ==================== TABLE METHODS ====================

    public void clearUsersTable() {
        usersPanel.clearTable();
    }

    public void addUserToTable(String email, String name, boolean isAdmin, String created, String status) {
        usersPanel.addUser(email, name, isAdmin, created, status);
    }

    public void clearGroupsTable() {
        groupsPanel.clearTable();
    }

    public void addGroupToTable(String id, String name, String creator, int memberCount, String created) {
        groupsPanel.addGroup(id, name, creator, memberCount, created);
    }

    public int getSelectedUserRow() {
        return usersPanel.getSelectedRow();
    }

    public int getSelectedGroupRow() {
        return groupsPanel.getSelectedRow();
    }

    public String getSelectedUserEmail() {
        return usersPanel.getSelectedUserEmail();
    }

    public String getSelectedGroupId() {
        return groupsPanel.getSelectedGroupId();
    }

    public void appendToMessageLog(String message) {
        messagesPanel.appendMessage(message);
    }

    public void clearMessageLog() {
        messagesPanel.clearLog();
    }

    public String getMessageLog() {
        return messagesPanel.getLogContent();
    }

    // ==================== EVENT LISTENER METHODS ====================

    public void addLoginListener(ActionListener listener) {
        loginPanel.addLoginListener(listener);
    }

    public void addLogoutListener(ActionListener listener) {
        menuBar.addLogoutListener(listener);
    }

    public void addExitListener(ActionListener listener) {
        menuBar.addExitListener(listener);
    }

    public void addRefreshAllListener(ActionListener listener) {
        menuBar.addRefreshAllListener(listener);
    }

    public void addRefreshUsersListener(ActionListener listener) {
        usersPanel.addRefreshListener(listener);
    }

    public void addRefreshGroupsListener(ActionListener listener) {
        groupsPanel.addRefreshListener(listener);
    }

    public void addSearchUserListener(ActionListener listener) {
        usersPanel.addSearchListener(listener);
    }

    public void addClearSearchListener(ActionListener listener) {
        usersPanel.addClearSearchListener(listener);
    }

    public void addAddUserListener(ActionListener listener) {
        usersPanel.addAddUserListener(listener);
    }

    public void addEditUserListener(ActionListener listener) {
        usersPanel.addEditUserListener(listener);
    }

    public void addDeleteUserListener(ActionListener listener) {
        usersPanel.addDeleteUserListener(listener);
    }

    public void addDeleteGroupListener(ActionListener listener) {
        groupsPanel.addDeleteGroupListener(listener);
    }

    public void addViewGroupMembersListener(ActionListener listener) {
        groupsPanel.addViewMembersListener(listener);
    }

    public void addClearMessagesListener(ActionListener listener) {
        messagesPanel.addClearMessagesListener(listener);
    }

    public void addExportMessagesListener(ActionListener listener) {
        messagesPanel.addExportMessagesListener(listener);
    }

    public void addAboutListener(ActionListener listener) {
        menuBar.addAboutListener(listener);
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