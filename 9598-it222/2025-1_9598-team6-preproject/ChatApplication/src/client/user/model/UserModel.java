package client.user.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * User Client Model - Handles business logic and server communication
 */
public class UserModel {

    // Server connection details
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9999;

    // Connection components
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected;
    private boolean authenticated;

    // User information
    private String userEmail;
    private String userName;
    private String sessionId;

    // Chat context
    private String currentChatType = "broadcast"; // broadcast, private, group
    private String currentChatTarget = null;

    // Data storage
    private List<Friend> friendsList;
    private List<Group> groupsList;
    private List<String> onlineUsers;
    private List<FriendRequest> pendingRequests;
    private Map<String, List<ChatMessage>> chatHistory;
    private List<Map<String, String>> lastSearchResults;

    // Message receiver thread
    private Thread messageReceiverThread;
    private boolean receiving;

    // Listeners
    private List<ModelListener> listeners;

    /**
     * Inner class to represent a friend
     */
    public static class Friend {
        public String email;
        public String name;
        public boolean isOnline;

        public Friend(String email, String name, boolean isOnline) {
            this.email = email;
            this.name = name;
            this.isOnline = isOnline;
        }
    }

    /**
     * Inner class to represent a group
     */
    public static class Group {
        public String id;
        public String name;
        public List<String> members;

        public Group(String id, String name) {
            this.id = id;
            this.name = name;
            this.members = new ArrayList<>();
        }
    }

    /**
     * Inner class to represent a friend request
     */
    public static class FriendRequest {
        public String fromEmail;
        public String fromName;
        public String timestamp;

        public FriendRequest(String fromEmail, String fromName, String timestamp) {
            this.fromEmail = fromEmail;
            this.fromName = fromName;
            this.timestamp = timestamp;
        }
    }

    /**
     * Inner class to represent a chat message
     */
    public static class ChatMessage {
        public String sender;
        public String content;
        public String timestamp;
        public String type; // broadcast, private, group, system

        public ChatMessage(String sender, String content, String timestamp, String type) {
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.type = type;
        }
    }

    /**
     * Interface for model event listeners
     */
    public interface ModelListener {
        void onConnectionStatusChanged(boolean connected);
        void onLoginResult(boolean success, String message);
        void onRegistrationResult(boolean success, String message);
        void onMessageReceived(String sender, String message, String type);
        void onFriendsListUpdated(List<Friend> friends);
        void onGroupsListUpdated(List<Group> groups);
        void onFriendRequestReceived(FriendRequest request);
        void onFriendRequestResponse(String fromUser, boolean accepted);
        void onUserStatusChanged(String userEmail, boolean isOnline);
        void onErrorReceived(String error);
        void onOperationResult(boolean success, String message);
        void onSearchResults(List<Map<String, String>> results);
    }

    private void notifySearchResults(List<Map<String, String>> results) {
        for (ModelListener listener : listeners) {
            listener.onSearchResults(results);
        }
    }

    /**
     * Constructor
     */
    public UserModel() {
        this.connected = false;
        this.authenticated = false;
        this.friendsList = new ArrayList<>();
        this.groupsList = new ArrayList<>();
        this.onlineUsers = new ArrayList<>();
        this.pendingRequests = new ArrayList<>();
        this.chatHistory = new HashMap<>();
        this.lastSearchResults = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    // ==================== CONNECTION MANAGEMENT ====================

    /**
     * Connects to the server
     * @return true if connection successful, false otherwise
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            connected = true;
            receiving = true;

            // Start message receiver thread
            startMessageReceiver();

            notifyConnectionStatusChanged(true);
            System.out.println("[MODEL] Connected to server");

            return true;

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to connect to server: " + e.getMessage());
            notifyConnectionStatusChanged(false);
            return false;
        }
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        if (authenticated) {
            sendLogoutMessage();
        }

        receiving = false;
        connected = false;
        authenticated = false;

        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("[ERROR] Error during disconnect: " + e.getMessage());
        }

        notifyConnectionStatusChanged(false);
        System.out.println("[MODEL] Disconnected from server");
    }

    /**
     * Checks if connected to server
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    /**
     * Checks if authenticated
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    // ==================== AUTHENTICATION ====================

    /**
     * Attempts to login
     * @param email User email
     * @param password User password
     */
    public void login(String email, String password) {
        if (!connected && !connect()) {
            notifyLoginResult(false, "Failed to connect to server");
            return;
        }

        String loginXML = createLoginXML(email, password);
        sendMessage(loginXML);
        System.out.println("[MODEL] Login attempt for: " + email);
    }

    /**
     * Registers a new user
     * @param email User email
     * @param password User password
     * @param name User name
     */
    public void register(String email, String password, String name) {
        if (!connected && !connect()) {
            notifyRegistrationResult(false, "Failed to connect to server");
            return;
        }

        String registerXML = createRegisterXML(email, password, name);
        sendMessage(registerXML);
        System.out.println("[MODEL] Registration attempt for: " + email);
    }

    /**
     * Logs out from the server
     */
    public void logout() {
        if (authenticated) {
            sendLogoutMessage();
        }

        authenticated = false;
        clearUserData();
        System.out.println("[MODEL] User logged out");
    }

    // ==================== MESSAGING ====================

    /**
     * Sends a message based on current chat context
     * @param message The message to send
     */
    public void sendChatMessage(String message) {
        if (!authenticated) return;

        String messageXML;

        switch (currentChatType) {
            case "broadcast":
                messageXML = createBroadcastXML(message);
                break;

            case "private":
                if (currentChatTarget != null) {
                    messageXML = createPrivateMessageXML(currentChatTarget, message);
                } else {
                    notifyError("No recipient selected for private message");
                    return;
                }
                break;

            case "group":
                if (currentChatTarget != null) {
                    messageXML = createGroupMessageXML(currentChatTarget, message);
                } else {
                    notifyError("No group selected for group message");
                    return;
                }
                break;

            default:
                return;
        }

        sendMessage(messageXML);

        // Add to local chat history
        String historyKey = currentChatTarget != null ? currentChatTarget : "broadcast";
        addToHistory(historyKey, new ChatMessage("You", message, new Date().toString(), currentChatType));

        // Show own message immediately (only once)
        notifyMessageReceived("You", message, currentChatType);
    }

    /**
     * Sets the current chat context
     * @param type Chat type (broadcast, private, group)
     * @param target Target email or group ID (null for broadcast)
     */
    public void setCurrentChat(String type, String target) {
        this.currentChatType = type;
        this.currentChatTarget = target;
        System.out.println("[MODEL] Chat context set to: " + type +
                (target != null ? " with " + target : ""));
    }

    // ==================== FRIEND MANAGEMENT ====================

    /**
     * Searches for users
     * @param searchKey Search term
     */
    public void searchUsers(String searchKey) {
        if (!authenticated) return;

        String searchXML = createSearchXML(searchKey);
        sendMessage(searchXML);
        System.out.println("[MODEL] Searching users for: " + searchKey);
    }

    /**
     * Sends a friend request
     * @param friendEmail Email of the user to add as friend
     */
    public void sendFriendRequest(String friendEmail) {
        if (!authenticated) return;

        // Check if already friends
        for (Friend friend : friendsList) {
            if (friend.email.equals(friendEmail)) {
                notifyError("Already friends with " + friendEmail);
                return;
            }
        }

        String requestXML = createFriendRequestXML(friendEmail);
        sendMessage(requestXML);
        System.out.println("[MODEL] Friend request sent to: " + friendEmail);
    }

    /**
     * Responds to a friend request
     * @param fromEmail Email of the requester
     * @param accept Whether to accept or reject
     */
    public void respondToFriendRequest(String fromEmail, boolean accept) {
        if (!authenticated) return;

        String responseXML = createFriendResponseXML(fromEmail, accept);
        sendMessage(responseXML);

        // Remove from pending requests
        pendingRequests.removeIf(req -> req.fromEmail.equals(fromEmail));

        System.out.println("[MODEL] Friend request from " + fromEmail +
                (accept ? " accepted" : " rejected"));
    }

    /**
     * Refreshes friends list
     */
    public void refreshFriends() {
        if (!authenticated) return;

        String requestXML = createRequestXML("getfriends");
        sendMessage(requestXML);
        System.out.println("[MODEL] Refreshing friends list");
    }

    /**
     * Checks for pending friend requests from the server
     */
    public void checkForFriendRequests() {
        if (!authenticated) return;

        String requestXML = "<message type=\"checkFriendRequests\">" +
                "<from>" + userEmail + "</from>" +
                "</message>";
        sendMessage(requestXML);
        System.out.println("[MODEL] Checking for friend requests");
    }

    // ==================== GROUP MANAGEMENT ====================

    /**
     * Creates a new group
     * @param groupName Name of the group
     * @param members List of member emails
     */
    public void createGroup(String groupName, List<String> members) {
        if (!authenticated) return;

        // Add self to members
        if (!members.contains(userEmail)) {
            members.add(userEmail);
        }

        String groupXML = createGroupXML(groupName, members);
        sendMessage(groupXML);
        System.out.println("[MODEL] Creating group: " + groupName);
    }

    /**
     * Refreshes groups list
     */
    public void refreshGroups() {
        if (!authenticated) return;

        String requestXML = createRequestXML("getgroups");
        sendMessage(requestXML);
        System.out.println("[MODEL] Refreshing groups list");
    }

    /**
     * Refreshes all data (friends and groups)
     */
    public void refreshAll() {
        refreshFriends();
        refreshGroups();
    }

    // ==================== MESSAGE HANDLING ====================

    /**
     * Starts the message receiver thread
     */
    private void startMessageReceiver() {
        messageReceiverThread = new Thread(() -> {
            while (receiving && connected) {
                try {
                    String message = in.readLine();
                    if (message != null) {
                        processMessage(message);
                    }
                } catch (IOException e) {
                    if (receiving) {
                        System.err.println("[ERROR] Error receiving message: " + e.getMessage());
                        connected = false;
                        notifyConnectionStatusChanged(false);
                    }
                    break;
                }
            }
        });

        messageReceiverThread.setDaemon(true);
        messageReceiverThread.setName("UserMessageReceiver");
        messageReceiverThread.start();
    }

    /**
     * Processes received XML messages
     */
    private void processMessage(String xmlMessage) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlMessage)));
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            String messageType = root.getTagName();

            if ("response".equals(messageType)) {
                String responseType = root.getAttribute("type");
                handleResponse(responseType, doc);
            } else if ("message".equals(messageType)) {
                String msgType = root.getAttribute("type");
                handleMessage(msgType, doc);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles response messages
     */
    private void handleResponse(String responseType, Document doc) {
        switch (responseType) {
            case "login":
                handleLoginResponse(doc);
                break;

            case "register":
                handleRegisterResponse(doc);
                break;

            case "search":
                handleSearchResponse(doc);
                break;

            case "friendsList":
                handleFriendsListResponse(doc);
                break;

            case "groupsList":
                handleGroupsListResponse(doc);
                break;

            case "createGroup":
                handleCreateGroupResponse(doc);
                break;

            case "general":
                handleGeneralResponse(doc);
                break;

            default:
                System.out.println("[DEBUG] Unknown response type: " + responseType);
        }
    }

    /**
     * Handles message notifications
     */
    private void handleMessage(String messageType, Document doc) {
        switch (messageType) {
            case "broadcast":
                handleBroadcastMessage(doc);
                break;

            case "private":
                handlePrivateMessage(doc);
                break;

            case "group":
                handleGroupMessage(doc);
                break;

            case "friendRequest":
                handleFriendRequest(doc);
                break;

            case "friendAccepted":
                handleFriendAccepted(doc);
                break;

            case "groupInvite":
                handleGroupInvite(doc);
                break;

            case "status":
                handleStatusUpdate(doc);
                break;

            case "error":
                handleErrorMessage(doc);
                break;

            case "forceLogout":
                handleForceLogout(doc);
                break;

            default:
                System.out.println("[DEBUG] Unknown message type: " + messageType);
        }
    }

    /**
     * Handles login response
     */
    private void handleLoginResponse(Document doc) {
        boolean success = Boolean.parseBoolean(getElementValue(doc, "success"));

        if (success) {
            this.userEmail = getLoginEmail();
            this.userName = getElementValue(doc, "userName");
            this.sessionId = getElementValue(doc, "sessionId");
            this.authenticated = true;

            notifyLoginResult(true, "Login successful");
            System.out.println("[MODEL] Logged in as: " + userName);

            // Request initial data with a small delay to ensure server is ready
            new Thread(() -> {
                try {
                    Thread.sleep(100); // Small delay
                    refreshFriends();  // Get friends first
                    Thread.sleep(100); // Small delay between requests
                    refreshGroups();   // Then groups
                    Thread.sleep(100);
                    checkForFriendRequests(); // Check for pending friend requests
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } else {
            notifyLoginResult(false, "Invalid credentials");
        }
    }

    /**
     * Handles registration response
     */
    private void handleRegisterResponse(Document doc) {
        boolean success = Boolean.parseBoolean(getElementValue(doc, "success"));

        if (success) {
            notifyRegistrationResult(true, "Registration successful! Please login.");
        } else {
            notifyRegistrationResult(false, "Registration failed. Email may already exist.");
        }
    }

    /**
     * Handles search response
     */
    private void handleSearchResponse(Document doc) {
        NodeList userNodes = doc.getElementsByTagName("user");
        lastSearchResults.clear();  // Clear previous results

        for (int i = 0; i < userNodes.getLength(); i++) {
            Element userElement = (Element) userNodes.item(i);
            String email = getElementValue(userElement, "email");
            String name = getElementValue(userElement, "n");
            boolean isFriend = Boolean.parseBoolean(getElementValue(userElement, "isFriend"));

            Map<String, String> result = new HashMap<>();
            result.put("email", email);
            result.put("name", name);
            result.put("isFriend", String.valueOf(isFriend));
            lastSearchResults.add(result);
        }

        // THIS IS THE KEY LINE - Call notifySearchResults, NOT notifyOperationResult
        System.out.println("[DEBUG] Calling notifySearchResults with " + lastSearchResults.size() + " results");
        notifySearchResults(lastSearchResults);

        System.out.println("[MODEL] Search found " + lastSearchResults.size() + " users");
        System.out.println("[DEBUG] Calling notifySearchResults with " + lastSearchResults.size() + " results");

    }


    /**
     * Handles friends list response
     */
    private void handleFriendsListResponse(Document doc) {
        friendsList.clear();
        NodeList friendNodes = doc.getElementsByTagName("friend");

        System.out.println("[DEBUG] Processing friends list response with " + friendNodes.getLength() + " friends");

        for (int i = 0; i < friendNodes.getLength(); i++) {
            Element friendElement = (Element) friendNodes.item(i);
            String email = getElementValue(friendElement, "email");
            String name = getElementValue(friendElement, "name"); // Changed from "n" to "name"

            // If name is still empty, try "n" as fallback
            if (name.isEmpty()) {
                name = getElementValue(friendElement, "n");
            }

            System.out.println("[DEBUG] Friend found: " + name + " (" + email + ")");

            // Check if online
            boolean isOnline = onlineUsers.contains(email);

            friendsList.add(new Friend(email, name, isOnline));
        }

        notifyFriendsListUpdated(friendsList);
        System.out.println("[MODEL] Friends list updated: " + friendsList.size() + " friends");
    }

    /**
     * Handles groups list response
     */
    private void handleGroupsListResponse(Document doc) {
        groupsList.clear();
        NodeList groupNodes = doc.getElementsByTagName("group");

        for (int i = 0; i < groupNodes.getLength(); i++) {
            Element groupElement = (Element) groupNodes.item(i);
            String id = getElementValue(groupElement, "id");
            String name = getElementValue(groupElement, "n");

            Group group = new Group(id, name);

            // Add members if provided
            NodeList memberNodes = groupElement.getElementsByTagName("member");
            for (int j = 0; j < memberNodes.getLength(); j++) {
                group.members.add(memberNodes.item(j).getTextContent());
            }

            groupsList.add(group);
        }

        notifyGroupsListUpdated(groupsList);
        System.out.println("[MODEL] Groups list updated: " + groupsList.size() + " groups");
    }

    /**
     * Handles group creation response
     */
    private void handleCreateGroupResponse(Document doc) {
        boolean success = Boolean.parseBoolean(getElementValue(doc, "success"));

        if (success) {
            String groupId = getElementValue(doc, "groupId");
            String groupName = getElementValue(doc, "groupName");

            notifyOperationResult(true, "Group '" + groupName + "' created successfully");

            // Refresh groups list
            refreshGroups();
        } else {
            notifyOperationResult(false, "Failed to create group");
        }
    }

    /**
     * Handles broadcast message
     */
    private void handleBroadcastMessage(Document doc) {
        String sender = getElementValue(doc, "sender");
        String content = getElementValue(doc, "content");
        String timestamp = getElementValue(doc, "timestamp");

        // Don't show own messages twice
        if ("broadcast".equals(currentChatType)) {
            // Don't show own messages twice
            if (!sender.equals(userEmail)) {
                addToHistory("broadcast", new ChatMessage(sender, content, timestamp, "broadcast"));
                notifyMessageReceived(sender, content, "broadcast");
            }
        } else {
            // Just store in history, don't display
            addToHistory("broadcast", new ChatMessage(sender, content, timestamp, "broadcast"));
        }
    }

    /**
     * Handles private message
     */
    private void handlePrivateMessage(Document doc) {
        String sender = getElementValue(doc, "sender");
        String content = getElementValue(doc, "content");
        String timestamp = getElementValue(doc, "timestamp");
        boolean isOffline = Boolean.parseBoolean(getElementValue(doc, "offline"));

        addToHistory(sender, new ChatMessage(sender, content, timestamp, "private"));

        if ("private".equals(currentChatType) && sender.equals(currentChatTarget)) {
            if (isOffline) {
                notifyMessageReceived(sender, "[Offline] " + content, "private");
            } else {
                notifyMessageReceived(sender, content, "private");
            }
        }
    }

    /**
     * Handles group message
     */
    private void handleGroupMessage(Document doc) {
        String groupId = getElementValue(doc, "groupId");
        String sender = getElementValue(doc, "sender");
        String content = getElementValue(doc, "content");
        String timestamp = getElementValue(doc, "timestamp");

        // Don't show own messages twice
        if (!sender.equals(userEmail)) {
            addToHistory(groupId, new ChatMessage(sender, content, timestamp, "group"));

            // Only show if currently viewing this group
            if ("group".equals(currentChatType) && groupId.equals(currentChatTarget)) {
                // Find group name
                String groupName = groupId;
                for (Group group : groupsList) {
                    if (group.id.equals(groupId)) {
                        groupName = group.name;
                        break;
                    }
                }

                notifyMessageReceived(sender, content, "group");
            }
        }
    }

    /**
     * Handles friend request
     */
    private void handleFriendRequest(Document doc) {
        String fromEmail = getElementValue(doc, "from");
        String fromName = getElementValue(doc, "fromName");
        String timestamp = getElementValue(doc, "timestamp");

        FriendRequest request = new FriendRequest(fromEmail, fromName, timestamp);
        pendingRequests.add(request);

        notifyFriendRequestReceived(request);
        System.out.println("[MODEL] Friend request received from: " + fromName);
    }

    /**
     * Handles friend accepted notification
     */
    private void handleFriendAccepted(Document doc) {
        String friendEmail = getElementValue(doc, "friend");
        String friendName = getElementValue(doc, "friendName");

        notifyFriendRequestResponse(friendEmail, true);
        notifyMessageReceived("System", friendName + " accepted your friend request!", "system");

        // Refresh friends list
        refreshFriends();
    }

    /**
     * Handles group invite
     */
    private void handleGroupInvite(Document doc) {
        String groupId = getElementValue(doc, "groupId");
        String groupName = getElementValue(doc, "groupName");
        String inviter = getElementValue(doc, "inviter");

        notifyMessageReceived("System", inviter + " added you to group: " + groupName, "system");

        // Refresh groups list
        refreshGroups();
    }

    /**
     * Handles user status update
     */
    private void handleStatusUpdate(Document doc) {
        String user = getElementValue(doc, "user");
        String status = getElementValue(doc, "status");

        boolean isOnline = "online".equals(status);

        if (isOnline) {
            if (!onlineUsers.contains(user)) {
                onlineUsers.add(user);
            }
        } else {
            onlineUsers.remove(user);
        }

        // Update friend status
        for (Friend friend : friendsList) {
            if (friend.email.equals(user)) {
                friend.isOnline = isOnline;
                break;
            }
        }

        notifyUserStatusChanged(user, isOnline);
    }

    /**
     * Handles general response
     */
    private void handleGeneralResponse(Document doc) {
        boolean success = Boolean.parseBoolean(getElementValue(doc, "success"));
        String message = getElementValue(doc, "message");

        if ("Message sent".equals(message)) {
            return;  // Don't show popup for message confirmations
        }

        notifyOperationResult(success, message);
    }

    /**
     * Handles error messages
     */
    private void handleErrorMessage(Document doc) {
        String error = getElementValue(doc, "e");
        notifyError(error);
    }

    /**
     * Handles force logout
     */
    private void handleForceLogout(Document doc) {
        String reason = getElementValue(doc, "reason");
        authenticated = false;
        notifyError("Forced logout: " + reason);
        disconnect();
    }

    /**
     * Sends a message to the server
     */
    private void sendMessage(String message) {
        if (out != null && connected) {
            out.println(message);
            out.flush();
        }
    }

    /**
     * Sends logout message
     */
    private void sendLogoutMessage() {
        String logoutXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"logout\"/>";
        sendMessage(logoutXML);
    }

    // ==================== XML CREATION METHODS ====================

    private String createLoginXML(String email, String password) {
        this.userEmail = email; // Store for later use
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"login\">" +
                "<email>" + email + "</email>" +
                "<password>" + password + "</password>" +
                "</message>";
    }

    private String createRegisterXML(String email, String password, String name) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"register\">" +
                "<email>" + email + "</email>" +
                "<password>" + password + "</password>" +
                "<name>" + name + "</name>" +
                "</message>";
    }

    private String createBroadcastXML(String content) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"broadcast\">" +
                "<content>" + escapeXML(content) + "</content>" +
                "</message>";
    }

    private String createPrivateMessageXML(String recipient, String content) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"private\">" +
                "<recipient>" + recipient + "</recipient>" +
                "<content>" + escapeXML(content) + "</content>" +
                "</message>";
    }

    private String createGroupMessageXML(String groupId, String content) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"group\">" +
                "<groupId>" + groupId + "</groupId>" +
                "<content>" + escapeXML(content) + "</content>" +
                "</message>";
    }

    private String createSearchXML(String searchKey) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"search\">" +
                "<searchKey>" + searchKey + "</searchKey>" +
                "</message>";
    }

    private String createFriendRequestXML(String friendEmail) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"addfriend\">" +
                "<friendEmail>" + friendEmail + "</friendEmail>" +
                "</message>";
    }

    private String createFriendResponseXML(String requesterEmail, boolean accept) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"acceptfriend\">" +
                "<requesterEmail>" + requesterEmail + "</requesterEmail>" +
                "<accept>" + accept + "</accept>" +
                "</message>";
    }

    private String createGroupXML(String groupName, List<String> members) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<message type=\"creategroup\">");
        xml.append("<groupName>").append(groupName).append("</groupName>");
        xml.append("<members>");
        for (String member : members) {
            xml.append("<member>").append(member).append("</member>");
        }
        xml.append("</members>");
        xml.append("</message>");
        return xml.toString();
    }

    private String createRequestXML(String requestType) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"" + requestType + "\"/>";
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Gets element value from XML document
     */
    private String getElementValue(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    /**
     * Gets element value from parent element
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

    /**
     * Adds message to chat history
     */
    private void addToHistory(String key, ChatMessage message) {
        if (!chatHistory.containsKey(key)) {
            chatHistory.put(key, new ArrayList<>());
        }
        chatHistory.get(key).add(message);
    }

    /**
     * Clears all data
     */
    private void clearUserData() {
        friendsList.clear();
        groupsList.clear();
        onlineUsers.clear();
        pendingRequests.clear();
        chatHistory.clear();
    }

    /**
     * Gets the login email (stored temporarily)
     */
    private String getLoginEmail() {
        return userEmail;
    }

    // ==================== LISTENER MANAGEMENT ====================

    /**
     * Adds a model listener
     */
    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a model listener
     */
    public void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }

    private void notifyConnectionStatusChanged(boolean connected) {
        for (ModelListener listener : listeners) {
            listener.onConnectionStatusChanged(connected);
        }
    }

    private void notifyLoginResult(boolean success, String message) {
        for (ModelListener listener : listeners) {
            listener.onLoginResult(success, message);
        }
    }

    private void notifyRegistrationResult(boolean success, String message) {
        for (ModelListener listener : listeners) {
            listener.onRegistrationResult(success, message);
        }
    }

    private void notifyMessageReceived(String sender, String message, String type) {
        for (ModelListener listener : listeners) {
            listener.onMessageReceived(sender, message, type);
        }
    }

    private void notifyFriendsListUpdated(List<Friend> friends) {
        for (ModelListener listener : listeners) {
            listener.onFriendsListUpdated(friends);
        }
    }

    private void notifyGroupsListUpdated(List<Group> groups) {
        for (ModelListener listener : listeners) {
            listener.onGroupsListUpdated(groups);
        }
    }

    private void notifyFriendRequestReceived(FriendRequest request) {
        for (ModelListener listener : listeners) {
            listener.onFriendRequestReceived(request);
        }
    }

    private void notifyFriendRequestResponse(String fromUser, boolean accepted) {
        for (ModelListener listener : listeners) {
            listener.onFriendRequestResponse(fromUser, accepted);
        }
    }

    private void notifyUserStatusChanged(String userEmail, boolean isOnline) {
        for (ModelListener listener : listeners) {
            listener.onUserStatusChanged(userEmail, isOnline);
        }
    }

    private void notifyError(String error) {
        for (ModelListener listener : listeners) {
            listener.onErrorReceived(error);
        }
    }

    private void notifyOperationResult(boolean success, String message) {
        for (ModelListener listener : listeners) {
            listener.onOperationResult(success, message);
        }
    }

    // ==================== GETTERS ====================

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public List<Friend> getFriendsList() {
        return new ArrayList<>(friendsList);
    }

    public List<Group> getGroupsList() {
        return new ArrayList<>(groupsList);
    }

    public List<FriendRequest> getPendingRequests() {
        return new ArrayList<>(pendingRequests);
    }

    public List<ChatMessage> getChatHistory(String key) {
        return chatHistory.getOrDefault(key, new ArrayList<>());
    }

    public List<Map<String, String>> getLastSearchResults() {
        return new ArrayList<>(lastSearchResults);
    }

    // ==================== DEBUG METHODS ====================

    /**
     * Debug method to print current friends list
     */
    public void debugPrintFriends() {
        System.out.println("=== Current Friends List ===");
        System.out.println("Total friends: " + friendsList.size());
        for (Friend friend : friendsList) {
            System.out.println("- " + friend.name + " (" + friend.email + ") - Online: " + friend.isOnline);
        }
        System.out.println("==========================");
    }

    /**
     * Debug method to print current groups list
     */
    public void debugPrintGroups() {
        System.out.println("=== Current Groups List ===");
        System.out.println("Total groups: " + groupsList.size());
        for (Group group : groupsList) {
            System.out.println("- " + group.name + " (ID: " + group.id + ")");
            System.out.println("  Members: " + group.members);
        }
        System.out.println("==========================");
    }

    /**
     * Debug method to print current chat history
     */
    public void debugPrintChatHistory(String key) {
        System.out.println("=== Chat History for " + key + " ===");
        List<ChatMessage> messages = chatHistory.getOrDefault(key, new ArrayList<>());
        for (ChatMessage message : messages) {
            System.out.println("[" + message.timestamp + "] " + message.sender + ": " + message.content);
        }
        System.out.println("==========================");
    }

    /**
     * Debug method to print current pending friend requests
     */
    public void debugPrintPendingRequests() {
        System.out.println("=== Pending Friend Requests ===");
        System.out.println("Total requests: " + pendingRequests.size());
        for (FriendRequest request : pendingRequests) {
            System.out.println("- From: " + request.fromName + " (" + request.fromEmail + ") - Timestamp: " + request.timestamp);
        }
        System.out.println("==========================");
    }

    /**
     * Debug method to print current online users
     */
    public void debugPrintOnlineUsers() {
        System.out.println("=== Online Users ===");
        System.out.println("Total online: " + onlineUsers.size());
        for (String user : onlineUsers) {
            System.out.println("- " + user);
        }
        System.out.println("==========================");
    }
}
