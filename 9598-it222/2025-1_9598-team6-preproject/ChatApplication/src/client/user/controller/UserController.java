package client.user.controller;

import client.user.model.UserModel;
import client.user.view.UserView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User Client Controller - Connects Model and View
 * Handles user interactions and updates view based on model changes
 */
public class UserController implements UserModel.ModelListener {

    private UserModel model;
    private UserView view;

    /**
     * Constructor
     * @param model The user model
     * @param view The user view
     */
    public UserController(UserModel model, UserView view) {
        this.model = model;
        this.view = view;

        // Register as model listener
        model.addListener(this);

        // Initialize event listeners
        initializeListeners();

        System.out.println("[CONTROLLER] User controller initialized");
    }

    /**
     * Initializes all event listeners
     */
    private void initializeListeners() {
        // Login panel listeners
        view.addLoginListener(new LoginListener());
        view.addShowRegisterListener(e -> view.showRegisterPanel());

        // Registration panel listeners
        view.addRegisterListener(new RegisterListener());
        view.addBackToLoginListener(e -> view.showLoginPanel());

        // Main panel listeners
        view.addLogoutListener(new LogoutListener());
        view.addSendListener(new SendMessageListener());
        view.addAttachListener(new AttachFileListener());
        view.addSearchListener(new SearchListener());
        view.addAddFriendListener(new AddFriendListener());
        view.addCreateGroupListener(new CreateGroupListener());
        view.addRefreshListener(new RefreshListener());

        // Contact selection listener
        view.addContactSelectionListener(new ContactSelectionListener());

        // Friend request listeners
        view.addAcceptFriendListener(new AcceptFriendListener());
        view.addRejectFriendListener(new RejectFriendListener());

        // Window listener
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
    }

    // ==================== EVENT LISTENERS ====================

    /**
     * Handles login action
     */
    private class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = view.getLoginEmail();
            String password = view.getLoginPassword();

            // Validate input
            if (email.isEmpty() || password.isEmpty()) {
                view.setLoginStatus("Please enter email and password");
                return;
            }

            if (!isValidEmail(email)) {
                view.setLoginStatus("Please enter a valid email address");
                return;
            }

            view.setLoginStatus("Connecting to server...");
            view.setStatusBar("Attempting login...");

            // Perform login in background thread
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    model.login(email, password);
                    return null;
                }
            };
            worker.execute();
        }
    }

    /**
     * Handles registration action
     */
    private class RegisterListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = view.getRegName();
            String email = view.getRegEmail();
            String password = view.getRegPassword();
            String confirmPassword = view.getRegConfirmPassword();

            // Validate input
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                view.setRegisterStatus("All fields are required");
                return;
            }

            if (!isValidEmail(email)) {
                view.setRegisterStatus("Please enter a valid email address");
                return;
            }

            if (!password.equals(confirmPassword)) {
                view.setRegisterStatus("Passwords do not match");
                return;
            }

            if (password.length() < 6) {
                view.setRegisterStatus("Password must be at least 6 characters");
                return;
            }

            view.setRegisterStatus("Registering...");

            // Perform registration in background thread
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    model.register(email, password, name);
                    return null;
                }
            };
            worker.execute();
        }
    }

    /**
     * Handles logout action
     */
    private class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (view.showConfirmation("Are you sure you want to logout?")) {
                model.logout();
                view.clearLoginFields();
                view.clearContacts();
                //view.clearChat();
                view.showLoginPanel();
                view.setStatusBar("Logged out");
            }
        }
    }

    /**
     * Handles send message action
     */
    private class SendMessageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = view.getMessage();
            if (!message.isEmpty() && model.isAuthenticated()) {
                model.sendChatMessage(message);
                view.clearMessage();
            }
        }
    }

    /**
     * Handles attach file action
     */
    private class AttachFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select file to send");

            int result = fileChooser.showOpenDialog(view);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // Check file size (limit to 5MB)
                if (file.length() > 5 * 1024 * 1024) {
                    view.showError("File size must be less than 5MB");
                    return;
                }

                // In a real implementation, you would encode the file and send it
                view.showInfo("File sending not yet implemented");
                view.setStatusBar("File: " + file.getName() + " selected");
            }
        }
    }

    /**
     * Handles search action
     */

    private class SearchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String searchKey = view.getSearchText();
            if (!searchKey.isEmpty() && model.isAuthenticated()) {
                view.setStatusBar("Searching for: " + searchKey);

                // Use SwingWorker for async search
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        model.searchUsers(searchKey);
                        return null;
                    }
                };
                worker.execute();
            }
        }
    }

    /**
     * Handles add friend action
     */
    private class AddFriendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!model.isAuthenticated()) return;

            String searchKey = view.showInputDialog(
                    "Enter name or email to search for users:",
                    "Add Friend - Search Users"
            );

            if (searchKey != null && !searchKey.trim().isEmpty()) {
                view.setStatusBar("Searching for: " + searchKey);
                model.searchUsers(searchKey.trim());
                // Results will be handled by onSearchResults callback
            }
        }
    }

    /**
     * Handles create group action
     */
    private class CreateGroupListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!model.isAuthenticated()) return;

            // Get group name
            String groupName = view.showInputDialog(
                    "Enter group name:",
                    "Create Group"
            );

            if (groupName == null || groupName.trim().isEmpty()) {
                return;
            }

            // Get friends list for selection
            List<UserModel.Friend> friends = model.getFriendsList();
            if (friends.isEmpty()) {
                view.showError("You need to have friends to create a group");
                return;
            }

            // Create selection dialog
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("Select members for the group:"));

            List<JCheckBox> checkBoxes = new ArrayList<>();
            for (UserModel.Friend friend : friends) {
                JCheckBox checkBox = new JCheckBox(friend.name + " (" + friend.email + ")");
                checkBoxes.add(checkBox);
                panel.add(checkBox);
            }

            int result = JOptionPane.showConfirmDialog(view, panel,
                    "Select Group Members", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                List<String> selectedMembers = new ArrayList<>();
                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isSelected()) {
                        selectedMembers.add(friends.get(i).email);
                    }
                }

                if (selectedMembers.isEmpty()) {
                    view.showError("Please select at least one member");
                    return;
                }

                model.createGroup(groupName, selectedMembers);
                view.setStatusBar("Creating group: " + groupName);
            }
        }
    }

    /**
     * Handles refresh action
     */
    private class RefreshListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isAuthenticated()) {
                view.setStatusBar("Manually refreshing data...");
                model.refreshAll();
                view.appendMessage("System", "Data refreshed manually", false, true);
            }
        }
    }

    /**
     * Handles contact selection
     */
    private class ContactSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) return;

            String selected = view.getSelectedContact();
            if (selected == null) return;

            // Clear chat area when switching conversations
//            view.clearChat();

            if (selected.startsWith("📢")) {
                // General chatroom
                model.setCurrentChat("broadcast", null);
                view.setCurrentChat("General Chatroom");
                view.appendMessage("System", "Switched to General Chatroom", false, true);

            } else if (selected.startsWith("👥")) {
                // Group chat
                String groupName = selected.substring(2).trim();

                // Find group ID
                String groupId = null;
                for (UserModel.Group group : model.getGroupsList()) {
                    if (group.name.equals(groupName)) {
                        groupId = group.id;
                        break;
                    }
                }

                if (groupId != null) {
                    model.setCurrentChat("group", groupId);
                    view.setCurrentChat(groupName);
                    view.appendMessage("System", "Switched to group: " + groupName, false, true);
                }

            } else {
                // Private chat with friend
                String friendInfo = selected.substring(2).trim(); // Remove status indicator

                // Extract email from friend info
                String friendEmail = null;
                for (UserModel.Friend friend : model.getFriendsList()) {
                    if (friendInfo.contains(friend.name)) {
                        friendEmail = friend.email;
                        break;
                    }
                }

                if (friendEmail != null) {
                    model.setCurrentChat("private", friendEmail);
                    view.setCurrentChat(friendInfo);
                    view.appendMessage("System", "Private chat with " + friendInfo, false, true);

                    // Load chat history if available
                    List<UserModel.ChatMessage> history = model.getChatHistory(friendEmail);
                    for (UserModel.ChatMessage msg : history) {
                        boolean isMyMessage = msg.sender.equals(model.getUserName());
                        view.appendMessage(msg.sender, msg.content, isMyMessage, false);
                    }
                }
            }
        }
    }

    /**
     * Handles accept friend request
     */
    private class AcceptFriendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selected = view.getSelectedFriendRequest();
            if (selected != null) {
                // Extract email from request info
                String requesterEmail = extractEmailFromRequest(selected);
                if (requesterEmail != null) {
                    model.respondToFriendRequest(requesterEmail, true);
                    view.removeFriendRequest(selected);
                    view.setStatusBar("Friend request accepted");
                }
            } else {
                view.showError("Please select a friend request");
            }
        }
    }

    /**
     * Handles reject friend request
     */
    private class RejectFriendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selected = view.getSelectedFriendRequest();
            if (selected != null) {
                // Extract email from request info
                String requesterEmail = extractEmailFromRequest(selected);
                if (requesterEmail != null) {
                    model.respondToFriendRequest(requesterEmail, false);
                    view.removeFriendRequest(selected);
                    view.setStatusBar("Friend request rejected");
                }
            } else {
                view.showError("Please select a friend request");
            }
        }
    }

    // ==================== MODEL LISTENER IMPLEMENTATION ====================

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            view.setConnectionStatus(connected);
            if (!connected && model.isAuthenticated()) {
                view.showError("Connection to server lost");
                view.showLoginPanel();
            }
        });
    }

    @Override
    public void onLoginResult(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            if (success) {
                view.clearLoginFields();
                view.setLoginStatus("");
                view.setUserName(model.getUserName());
                view.showMainPanel();
                view.setStatusBar("Logged in as: " + model.getUserEmail());
                view.setUserStatus("● Online");

                // Set initial chat to broadcast
                model.setCurrentChat("broadcast", null);
                view.setCurrentChat("General Chatroom");
                view.appendMessage("System", "Welcome to Chat Application!", false, true);
                view.appendMessage("System", "Auto-refresh enabled (every 30 seconds)", false, true);

            } else {
                view.setLoginStatus(message);
                view.showError(message);
            }
        });
    }

    @Override
    public void onRegistrationResult(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            if (success) {
                view.clearRegisterFields();
                view.showSuccess(message);
                view.showLoginPanel();
            } else {
                view.setRegisterStatus(message);
                view.showError(message);
            }
        });
    }

    @Override
    public void onMessageReceived(String sender, String message, String type) {
        SwingUtilities.invokeLater(() -> {
            // Get current chat context from view
            String currentChat = view.getSelectedContact();

            boolean shouldDisplay = false;

            if ("broadcast".equals(type) && currentChat != null && currentChat.contains("General Chatroom")) {
                shouldDisplay = true;
            } else if ("private".equals(type)) {
                // Only display if we're in private chat with the sender
                // This check should be done in model, but we can double-check here
                shouldDisplay = true;
            } else if ("group".equals(type)) {
                // Only display if we're viewing this group
                shouldDisplay = true;
            } else if ("system".equals(type)) {
                shouldDisplay = true;
            }

            if (shouldDisplay) {
                boolean isMyMessage = sender.equals("You") || sender.equals(model.getUserName());
                boolean isSystem = type.equals("system");

                view.appendMessage(sender, message, isMyMessage, isSystem);

                // Play notification sound for new messages (not own messages)
                if (!isMyMessage && !isSystem) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
    }

    @Override
    public void onFriendsListUpdated(List<UserModel.Friend> friends) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("[CONTROLLER] Updating friends list in UI with " + friends.size() + " friends");

            // Don't clear contacts, just update friends
            // First, remove all existing friend entries (keep General Chatroom and groups)
            for (int i = view.getContactsListModel().size() - 1; i >= 0; i--) {
                String item = view.getContactsListModel().get(i);
                // Remove if it's a friend entry (has status indicator ● or ○)
                if (item.startsWith("●") || item.startsWith("○")) {
                    view.getContactsListModel().remove(i);
                }
            }

            // Add all friends to contacts list
            for (UserModel.Friend friend : friends) {
                String displayName = friend.name;
                if (displayName == null || displayName.isEmpty()) {
                    displayName = friend.email; // Fallback to email if name is empty
                }
                view.addContact(displayName, friend.isOnline);
                System.out.println("[CONTROLLER] Added friend to UI: " + displayName + " (online: " + friend.isOnline + ")");
            }

            view.setStatusBar("Friends list updated: " + friends.size() + " friends (auto-refresh active)");
        });
    }

    @Override
    public void onGroupsListUpdated(List<UserModel.Group> groups) {
        SwingUtilities.invokeLater(() -> {
            // Add groups to contacts list
            for (UserModel.Group group : groups) {
                view.addGroup(group.name);
            }

            view.setStatusBar("Groups list updated: " + groups.size() + " groups (auto-refresh active)");
        });
    }

    @Override
    public void onFriendRequestReceived(UserModel.FriendRequest request) {
        SwingUtilities.invokeLater(() -> {
            String requestInfo = request.fromName + " (" + request.fromEmail + ")";
            view.addFriendRequest(requestInfo);
            view.showInfo("New friend request from: " + request.fromName);
            view.setStatusBar("Friend request received from " + request.fromName);

            // Play notification sound
            Toolkit.getDefaultToolkit().beep();
        });
    }

    @Override
    public void onFriendRequestResponse(String fromUser, boolean accepted) {
        SwingUtilities.invokeLater(() -> {
            String message = accepted ?
                    fromUser + " is now your friend!" :
                    "Friend request to " + fromUser + " was rejected";

            view.showInfo(message);
            view.setStatusBar(message);
        });
    }

    @Override
    public void onUserStatusChanged(String userEmail, boolean isOnline) {
        SwingUtilities.invokeLater(() -> {
            // Find and update friend status in contacts
            for (UserModel.Friend friend : model.getFriendsList()) {
                if (friend.email.equals(userEmail)) {
                    view.updateContactStatus(friend.name, isOnline);

                    String statusMsg = friend.name + " is now " +
                            (isOnline ? "online" : "offline");
                    view.appendMessage("System", statusMsg, false, true);
                    break;
                }
            }
        });
    }

    @Override
    public void onErrorReceived(String error) {
        SwingUtilities.invokeLater(() -> {
            view.showError(error);
            view.setStatusBar("Error: " + error);
        });
    }

    @Override
    public void onOperationResult(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            if (success) {
                view.showSuccess(message);
            } else {
                view.showError(message);
            }
            view.setStatusBar(message);
        });
    }

    @Override
    public void onSearchResults(List<Map<String, String>> results) {
        System.out.println("[DEBUG] onSearchResults called with " + results.size() + " results");
        SwingUtilities.invokeLater(() -> {
            if (results.isEmpty()) {
                view.showInfo("No users found");
                return;
            }

            // Create a list of user strings for display
            String[] userOptions = new String[results.size()];
            for (int i = 0; i < results.size(); i++) {
                Map<String, String> user = results.get(i);
                String display = user.get("name") + " (" + user.get("email") + ")";
                if ("true".equals(user.get("isFriend"))) {
                    display += " - Already Friend";
                }
                userOptions[i] = display;
            }

            // Show selection dialog
            String selected = (String) JOptionPane.showInputDialog(
                    view,
                    "Found " + results.size() + " user(s). Select one to add as friend:",
                    "Search Results",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    userOptions,
                    userOptions[0]
            );

            if (selected != null) {
                // Check if already friend
                if (selected.contains("Already Friend")) {
                    view.showInfo("You are already friends with this user");
                    return;
                }

                // Extract email from selected string
                int startIdx = selected.indexOf("(") + 1;
                int endIdx = selected.indexOf(")");
                if (startIdx > 0 && endIdx > startIdx) {
                    String friendEmail = selected.substring(startIdx, endIdx);

                    // Confirm friend request
                    int confirm = JOptionPane.showConfirmDialog(
                            view,
                            "Send friend request to " + friendEmail + "?",
                            "Add Friend",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        model.sendFriendRequest(friendEmail);
                        view.setStatusBar("Friend request sent to: " + friendEmail);
                    }
                }
            }
        });
    }

    // ==================== HELPER METHODS ====================

    /**
     * Validates email format
     */
    private boolean isValidEmail(String email) {
        return email != null &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Extracts email from friend request info string
     */
    private String extractEmailFromRequest(String requestInfo) {
        int start = requestInfo.indexOf("(");
        int end = requestInfo.indexOf(")");
        if (start != -1 && end != -1 && start < end) {
            return requestInfo.substring(start + 1, end);
        }
        return null;
    }

    /**
     * Handles application exit
     */
    private void handleExit() {
        if (model.isAuthenticated()) {
            if (view.showConfirmation("Are you sure you want to exit?")) {
                view.setStatusBar("Stopping auto-refresh and logging out...");
                model.logout();
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
}
