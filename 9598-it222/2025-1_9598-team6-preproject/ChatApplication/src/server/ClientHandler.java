package server;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;

/**
 * Handles individual client connections and processes their messages
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ChatServer server;
    private String userEmail;
    private String userName;
    private String sessionId;
    private boolean isAuthenticated;
    private boolean isRunning;
    private boolean isAdmin;

    /**
     * Constructor for ClientHandler
     * @param socket The client socket connection
     * @param server Reference to the main server
     */
    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.isAuthenticated = false;
        this.isRunning = true;
        this.isAdmin = false;

        try {
            // Set up input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to create streams for client: " + e.getMessage());
            disconnect();
        }
    }

    /**
     * Main run method for the client handler thread
     */
    @Override
    public void run() {
        try {
            System.out.println("[HANDLER] Client handler started for " + socket.getInetAddress());

            // Process messages from client
            String inputLine;
            while (isRunning && (inputLine = in.readLine()) != null) {
                System.out.println("[RECEIVED] From " + (userEmail != null ? userEmail : "unauthenticated") +
                        ": " + inputLine.substring(0, Math.min(inputLine.length(), 100)) + "...");
                processMessage(inputLine);
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("[ERROR] Connection error with client " + userEmail + ": " + e.getMessage());
            }
        } finally {
            System.out.println("[HANDLER] Client handler ending for " + (userEmail != null ? userEmail : "unknown"));
            cleanup();
        }
    }

    /**
     * Processes incoming XML messages from the client
     * @param xmlMessage The XML message to process
     */
    private void processMessage(String xmlMessage) {
        try {
            // Parse the XML message
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlMessage)));
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            String messageType = root.getAttribute("type");

            // Route message based on type
            switch (messageType.toLowerCase()) {
                case "login":
                    handleLogin(doc);
                    break;
                case "register":
                    handleRegistration(doc);
                    break;
                case "logout":
                    handleLogout();
                    break;
                case "broadcast":
                    if (isAuthenticated) {
                        handleBroadcast(doc);
                    } else {
                        sendError("Authentication required");
                    }
                    break;
                case "private":
                    if (isAuthenticated) {
                        handlePrivateMessage(doc);
                    } else {
                        sendError("Authentication required");
                    }
                    break;
                case "group":
                    if (isAuthenticated) {
                        handleGroupMessage(doc);
                    } else {
                        sendError("Authentication required");
                    }
                    break;
                case "creategroup":
                    if (isAuthenticated) {
                        handleCreateGroup(doc);
                    } else {
                        sendError("Authentication required");
                    }
                    break;
                case "search":
                    if (isAuthenticated) {
                        handleSearchUser(doc);
                    } else {
                        sendError("Authentication required");
                    }
                    break;
                case "addfriend":
                    if (isAuthenticated) {
                        handleAddFriend(doc);
                    } else {
                        sendError("Authentication required");
                    }
                    break;
                case "acceptfriend":
                    if (isAuthenticated) {
                        handleAcceptFriend(doc);
                    } else {
                        sendError("Authentication required");
                    }
                    break;
                case "getfriends":
                    if (isAuthenticated) {
                        handleGetFriends();
                    } else {
                        sendError("Authentication required");
                    }
                    break;
                case "getgroups":
                    if (isAuthenticated) {
                        handleGetGroups();
                    } else {
                        sendError("Authentication required");
                    }
                    break;
                case "getonlineusers":
                    if (isAuthenticated) {
                        handleGetOnlineUsers();
                    } else {
                        sendError("Authentication required");
                    }
                    break;
                // Admin operations
                case "admin_getusers":
                    if (isAuthenticated && isAdmin) {
                        handleAdminGetUsers(doc);
                    } else {
                        sendError("Admin authentication required");
                    }
                    break;
                case "admin_deleteuser":
                    if (isAuthenticated && isAdmin) {
                        handleAdminDeleteUser(doc);
                    } else {
                        sendError("Admin authentication required");
                    }
                    break;
                case "admin_updateuser":
                    if (isAuthenticated && isAdmin) {
                        handleAdminUpdateUser(doc);
                    } else {
                        sendError("Admin authentication required");
                    }
                    break;
                case "admin_deletegroup":
                    if (isAuthenticated && isAdmin) {
                        handleAdminDeleteGroup(doc);
                    } else {
                        sendError("Admin authentication required");
                    }
                    break;
                default:
                    sendError("Unknown message type: " + messageType);
                    System.out.println("[WARNING] Unknown message type: " + messageType);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error processing message: " + e.getMessage());
            e.printStackTrace();
            sendError("Error processing message: " + e.getMessage());
        }
    }

    /**
     * Handles user login
     */
    private void handleLogin(Document doc) {
        String email = getElementValue(doc, "email");
        String password = getElementValue(doc, "password");

        System.out.println("[LOGIN] Attempt for user: " + email);

        if (server.getDataManager().validateUser(email, password)) {
            // Check if user is admin
            this.isAdmin = server.getDataManager().isUserAdmin(email);

            // Create session
            this.userEmail = email;
            this.userName = server.getDataManager().getUserName(email);
            this.sessionId = server.getSessionManager().createSession(email);
            this.isAuthenticated = true;

            // Register with server
            server.registerClient(email, this);

            // Send success response
            String response = createLoginResponse(true, sessionId, userName, isAdmin);
            sendMessage(response);

            System.out.println("[LOGIN] Success for user: " + email + " (Admin: " + isAdmin + ")");

            // Send offline messages
            sendOfflineMessages();

            // Send friend requests
            sendPendingFriendRequests();

        } else {
            String response = createLoginResponse(false, null, null, false);
            sendMessage(response);
            System.out.println("[LOGIN] Failed for user: " + email);
        }
    }

    /**
     * Handles user registration
     */
    private void handleRegistration(Document doc) {
        String email = getElementValue(doc, "email");
        String password = getElementValue(doc, "password");
        String name = getElementValue(doc, "name");

        System.out.println("[REGISTER] New registration attempt for: " + email);

        boolean success = server.getDataManager().registerUser(email, password, name, false);

        String response = createRegistrationResponse(success);
        sendMessage(response);

        System.out.println("[REGISTER] " + (success ? "Success" : "Failed") + " for: " + email);
    }

    /**
     * Handles user logout
     */
    private void handleLogout() {
        System.out.println("[LOGOUT] User logging out: " + userEmail);
        String response = createLogoutResponse();
        sendMessage(response);
        cleanup();
    }

    /**
     * Handles broadcast messages
     */
    private void handleBroadcast(Document doc) {
        String message = getElementValue(doc, "content");
        server.broadcastMessage(message, userEmail);
        System.out.println("[BROADCAST] From " + userEmail + ": " + message);
    }

    /**
     * Handles private messages
     */
    private void handlePrivateMessage(Document doc) {
        String recipient = getElementValue(doc, "recipient");
        String message = getElementValue(doc, "content");

        // Check if they are friends
        if (server.getDataManager().areFriends(userEmail, recipient)) {
            server.sendPrivateMessage(recipient, userEmail, message);
            //sendSuccess("Message sent");
            System.out.println("[PRIVATE] From " + userEmail + " to " + recipient);
        } else {
            sendError("You can only send private messages to friends");
        }
    }

    /**
     * Handles group messages
     */
    private void handleGroupMessage(Document doc) {
        String groupId = getElementValue(doc, "groupId");
        String message = getElementValue(doc, "content");

        // Check if user is member of the group
        if (server.getDataManager().isGroupMember(groupId, userEmail)) {
            server.sendGroupMessage(groupId, userEmail, message);
            System.out.println("[GROUP] From " + userEmail + " to group " + groupId);
        } else {
            sendError("You are not a member of this group");
        }
    }

    /**
     * Handles group creation
     */
    private void handleCreateGroup(Document doc) {
        String groupName = getElementValue(doc, "groupName");
        NodeList memberNodes = doc.getElementsByTagName("member");

        List<String> members = new ArrayList<>();
        members.add(userEmail); // Add creator as member

        for (int i = 0; i < memberNodes.getLength(); i++) {
            members.add(memberNodes.item(i).getTextContent());
        }

        String groupId = server.getDataManager().createGroup(groupName, userEmail, members);

        if (groupId != null) {
            String response = createGroupResponse(groupId, groupName);
            sendMessage(response);
            System.out.println("[GROUP] Created by " + userEmail + ": " + groupName + " (ID: " + groupId + ")");

            // Notify members
            notifyGroupMembers(groupId, groupName, members);
        } else {
            sendError("Failed to create group");
        }
    }

    /**
     * Handles user search
     */
    private void handleSearchUser(Document doc) {
        String searchKey = getElementValue(doc, "searchKey");
        List<Map<String, String>> results = server.getDataManager().searchUsers(searchKey, userEmail);

        String response = createSearchResponse(results);
        sendMessage(response);
        System.out.println("[SEARCH] User " + userEmail + " searched for: " + searchKey);
    }

    /**
     * Handles friend request
     */
    private void handleAddFriend(Document doc) {
        String friendEmail = getElementValue(doc, "friendEmail");

        if (server.getDataManager().sendFriendRequest(userEmail, friendEmail)) {
            sendSuccess("Friend request sent");

            // Notify the friend if online
            if (server.isUserOnline(friendEmail)) {
                String notification = createFriendRequestNotification(userEmail, userName);
                server.sendMessageToClient(friendEmail, notification);
            }

            System.out.println("[FRIEND] Request from " + userEmail + " to " + friendEmail);
        } else {
            sendError("Friend request failed. User might not exist or request already sent.");
        }
    }

    /**
     * Handles friend request acceptance
     */
    private void handleAcceptFriend(Document doc) {
        String requesterEmail = getElementValue(doc, "requesterEmail");
        boolean accept = Boolean.parseBoolean(getElementValue(doc, "accept"));

        if (server.getDataManager().respondToFriendRequest(requesterEmail, userEmail, accept)) {
            sendSuccess(accept ? "Friend request accepted" : "Friend request rejected");

            if (accept) {
                // Notify the requester if online
                if (server.isUserOnline(requesterEmail)) {
                    String notification = createFriendAcceptedNotification(userEmail, userName);
                    server.sendMessageToClient(requesterEmail, notification);
                }
            }

            System.out.println("[FRIEND] " + userEmail + (accept ? " accepted " : " rejected ") +
                    "request from " + requesterEmail);
        } else {
            sendError("Failed to respond to friend request");
        }
    }

    /**
     * Handles getting user's friends list
     */
    private void handleGetFriends() {
        List<Map<String, String>> friends = server.getDataManager().getUserFriends(userEmail);
        String response = createFriendsListResponse(friends);
        sendMessage(response);
    }

    /**
     * Handles getting user's groups
     */
    private void handleGetGroups() {
        List<Map<String, String>> groups = server.getDataManager().getUserGroups(userEmail);
        String response = createGroupsListResponse(groups);
        sendMessage(response);
    }

    /**
     * Handles getting online users
     */
    private void handleGetOnlineUsers() {
        List<String> onlineUsers = server.getOnlineUsers();
        String response = createOnlineUsersResponse(onlineUsers);
        sendMessage(response);
    }

    // Admin handlers

    private void handleAdminGetUsers(Document doc) {
        List<Map<String, String>> users = server.getDataManager().getAllUsers();
        String response = createAdminUsersResponse(users);
        sendMessage(response);
        System.out.println("[ADMIN] " + userEmail + " requested users list");
    }

    private void handleAdminDeleteUser(Document doc) {
        String emailToDelete = getElementValue(doc, "email");

        if (emailToDelete.equals("admin@chat.com")) {
            sendError("Cannot delete the main admin account");
            return;
        }

        boolean success = server.getDataManager().deleteUser(emailToDelete);
        if (success) {
            // Force logout if user is online
            if (server.isUserOnline(emailToDelete)) {
                String forceLogoutMsg = createForceLogoutMessage();
                server.sendMessageToClient(emailToDelete, forceLogoutMsg);
            }
            sendSuccess("User deleted successfully");
            System.out.println("[ADMIN] " + userEmail + " deleted user: " + emailToDelete);
        } else {
            sendError("Failed to delete user");
        }
    }

    private void handleAdminUpdateUser(Document doc) {
        String emailToUpdate = getElementValue(doc, "email");
        String newName = getElementValue(doc, "name");
        String newPassword = getElementValue(doc, "password");

        boolean success = server.getDataManager().updateUser(emailToUpdate, newName, newPassword);
        sendMessage(createSuccessResponse(success, success ? "User updated" : "Update failed"));

        if (success) {
            System.out.println("[ADMIN] " + userEmail + " updated user: " + emailToUpdate);
        }
    }

    private void handleAdminDeleteGroup(Document doc) {
        String groupId = getElementValue(doc, "groupId");

        boolean success = server.getDataManager().deleteGroup(groupId);
        sendMessage(createSuccessResponse(success, success ? "Group deleted" : "Delete failed"));

        if (success) {
            System.out.println("[ADMIN] " + userEmail + " deleted group: " + groupId);
        }
    }

    /**
     * Sends offline messages to the user
     */
    private void sendOfflineMessages() {
        List<String> offlineMessages = server.getDataManager().getOfflineMessages(userEmail);
        for (String message : offlineMessages) {
            sendMessage(message);
        }
        if (!offlineMessages.isEmpty()) {
            System.out.println("[OFFLINE] Sent " + offlineMessages.size() + " offline messages to " + userEmail);
        }
    }

    /**
     * Sends pending friend requests to the user
     */
    private void sendPendingFriendRequests() {
        List<Map<String, String>> pendingRequests = server.getDataManager().getPendingFriendRequests(userEmail);
        for (Map<String, String> request : pendingRequests) {
            String notification = createFriendRequestNotification(
                    request.get("from"),
                    request.get("fromName")
            );
            sendMessage(notification);
        }
        if (!pendingRequests.isEmpty()) {
            System.out.println("[FRIEND] Sent " + pendingRequests.size() + " pending friend requests to " + userEmail);
        }
    }

    /**
     * Notifies group members about new group
     */
    private void notifyGroupMembers(String groupId, String groupName, List<String> members) {
        String notification = createGroupInviteNotification(groupId, groupName, userName);

        for (String member : members) {
            if (!member.equals(userEmail) && server.isUserOnline(member)) {
                server.sendMessageToClient(member, notification);
            }
        }
    }

    /**
     * Sends a message to the client
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }

    /**
     * Sends an error message to the client
     */
    public void sendError(String error) {
        String errorMessage = createErrorMessage(error);
        sendMessage(errorMessage);
    }

    /**
     * Sends a success message to the client
     */
    public void sendSuccess(String message) {
        String successMessage = createSuccessResponse(true, message);
        sendMessage(successMessage);
    }

    /**
     * Forces the user to logout
     */
    public void forceLogout() {
        String logoutMessage = createForceLogoutMessage();
        sendMessage(logoutMessage);
        cleanup();
    }

    /**
     * Disconnects the client
     */
    public void disconnect() {
        isRunning = false;
        cleanup();
    }

    /**
     * Cleans up resources and unregisters client
     */
    private void cleanup() {
        isRunning = false;

        if (userEmail != null) {
            server.unregisterClient(userEmail);
            if (sessionId != null) {
                server.getSessionManager().invalidateSession(sessionId);
            }
        }

        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[ERROR] Error during cleanup: " + e.getMessage());
        }
    }

    // ==================== XML Response Creation Methods ====================

    private String createLoginResponse(boolean success, String sessionId, String userName, boolean isAdmin) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<response type=\"login\">" +
                "<success>" + success + "</success>" +
                (success ? "<sessionId>" + sessionId + "</sessionId>" +
                        "<userName>" + userName + "</userName>" +
                        "<isAdmin>" + isAdmin + "</isAdmin>" : "") +
                "</response>";
    }

    private String createRegistrationResponse(boolean success) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<response type=\"register\">" +
                "<success>" + success + "</success>" +
                "</response>";
    }

    private String createLogoutResponse() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<response type=\"logout\">" +
                "<success>true</success>" +
                "</response>";
    }

    private String createGroupResponse(String groupId, String groupName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<response type=\"createGroup\">" +
                "<success>true</success>" +
                "<groupId>" + groupId + "</groupId>" +
                "<groupName>" + groupName + "</groupName>" +
                "</response>";
    }

    private String createSearchResponse(List<Map<String, String>> results) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<response type=\"search\">");
        xml.append("<results>");

        for (Map<String, String> user : results) {
            xml.append("<user>");
            xml.append("<email>").append(user.get("email")).append("</email>");
            xml.append("<name>").append(user.get("name")).append("</name>");
            xml.append("<isFriend>").append(user.get("isFriend")).append("</isFriend>");
            xml.append("</user>");
        }

        xml.append("</results>");
        xml.append("</response>");

        return xml.toString();
    }

    private String createFriendsListResponse(List<Map<String, String>> friends) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<response type=\"friendsList\">");
        xml.append("<friends>");

        for (Map<String, String> friend : friends) {
            xml.append("<friend>");
            xml.append("<email>").append(friend.get("email")).append("</email>");
            xml.append("<name>").append(friend.get("name")).append("</name>"); // Make sure it's "name" not "n"
            xml.append("</friend>");
        }

        xml.append("</friends>");
        xml.append("</response>");

        System.out.println("[DEBUG] Sending friends list with " + friends.size() + " friends");
        return xml.toString();
    }

    private String createGroupsListResponse(List<Map<String, String>> groups) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<response type=\"groupsList\">");
        xml.append("<groups>");

        for (Map<String, String> group : groups) {
            xml.append("<group>");
            xml.append("<id>").append(group.get("id")).append("</id>");
            xml.append("<name>").append(group.get("name")).append("</name>");
            xml.append("<creator>").append(group.get("creator")).append("</creator>");
            xml.append("<memberCount>").append(group.get("memberCount")).append("</memberCount>");
            xml.append("</group>");
        }

        xml.append("</groups>");
        xml.append("</response>");

        return xml.toString();
    }

    private String createOnlineUsersResponse(List<String> onlineUsers) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<response type=\"onlineUsers\">");
        xml.append("<users>");

        for (String user : onlineUsers) {
            xml.append("<user>").append(user).append("</user>");
        }

        xml.append("</users>");
        xml.append("</response>");

        return xml.toString();
    }

    private String createAdminUsersResponse(List<Map<String, String>> users) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<response type=\"adminUsers\">");
        xml.append("<users>");

        for (Map<String, String> user : users) {
            xml.append("<user>");
            xml.append("<email>").append(user.get("email")).append("</email>");
            xml.append("<name>").append(user.get("name")).append("</name>");
            xml.append("<isAdmin>").append(user.get("isAdmin")).append("</isAdmin>");
            xml.append("<created>").append(user.get("created")).append("</created>");
            xml.append("</user>");
        }

        xml.append("</users>");
        xml.append("</response>");

        return xml.toString();
    }

    private String createSuccessResponse(boolean success, String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<response type=\"general\">" +
                "<success>" + success + "</success>" +
                "<message>" + message + "</message>" +
                "</response>";
    }

    private String createErrorMessage(String error) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"error\">" +
                "<error>" + error + "</error>" +
                "</message>";
    }

    private String createForceLogoutMessage() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"forceLogout\">" +
                "<reason>Logged in from another location</reason>" +
                "</message>";
    }

    private String createFriendRequestNotification(String fromEmail, String fromName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"friendRequest\">" +
                "<from>" + fromEmail + "</from>" +
                "<fromName>" + fromName + "</fromName>" +
                "<timestamp>" + System.currentTimeMillis() + "</timestamp>" +
                "</message>";
    }

    private String createFriendAcceptedNotification(String friendEmail, String friendName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"friendAccepted\">" +
                "<friend>" + friendEmail + "</friend>" +
                "<friendName>" + friendName + "</friendName>" +
                "<timestamp>" + System.currentTimeMillis() + "</timestamp>" +
                "</message>";
    }

    private String createGroupInviteNotification(String groupId, String groupName, String inviterName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"groupInvite\">" +
                "<groupId>" + groupId + "</groupId>" +
                "<groupName>" + groupName + "</groupName>" +
                "<inviter>" + inviterName + "</inviter>" +
                "<timestamp>" + System.currentTimeMillis() + "</timestamp>" +
                "</message>";
    }

    // ==================== Utility Methods ====================

    /**
     * Gets the text content of an element by tag name
     */
    private String getElementValue(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    /**
     * Gets the text content of a child element
     */
    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    /**
     * Escapes XML special characters
     */
    private String escapeXML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}