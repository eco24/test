package client.Admin.model;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/**
 * Admin Client Model - Handles business logic and server communication
 * Fixed version with improved error handling and connection management
 */
public class AdminModel {

    // Server connection details
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9999;

    // Connection components
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected;
    private boolean authenticated;

    // Admin information
    private String adminEmail;
    private String adminName;
    private String sessionId;

    // Data storage
    private List<Map<String, String>> usersList;
    private List<Map<String, String>> groupsList;
    private List<String> messageLog;

    // Message receiver thread
    private Thread messageReceiverThread;
    private boolean receiving;

    // Listeners
    private List<ModelListener> listeners;

    /**
     * Interface for model event listeners
     */
    public interface ModelListener {
        void onConnectionStatusChanged(boolean connected);
        void onLoginResult(boolean success, String message);
        void onUsersDataReceived(List<Map<String, String>> users);
        void onGroupsDataReceived(List<Map<String, String>> groups);
        void onMessageReceived(String message);
        void onErrorReceived(String error);
        void onOperationResult(boolean success, String message);
    }

    /**
     * Constructor
     */
    public AdminModel() {
        this.connected = false;
        this.authenticated = false;
        this.usersList = new ArrayList<>();
        this.groupsList = new ArrayList<>();
        this.messageLog = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    // ==================== CONNECTION MANAGEMENT ====================

    /**
     * Connects to the server
     */
    public boolean connect() {
        try {
            // Close existing connection if any
            if (socket != null && !socket.isClosed()) {
                disconnect();
            }

            logMessage("Attempting to connect to " + SERVER_HOST + ":" + SERVER_PORT);
            socket = new Socket();
            socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), 5000); // 5 second timeout

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            connected = true;
            receiving = true;

            // Start message receiver thread
            startMessageReceiver();

            notifyConnectionStatusChanged(true);
            logMessage("Successfully connected to server");

            return true;

        } catch (IOException e) {
            logMessage("Failed to connect to server: " + e.getMessage());
            System.err.println("[ERROR] Connection failed: " + e.getMessage());
            notifyConnectionStatusChanged(false);
            return false;
        }
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        receiving = false;

        if (authenticated) {
            sendLogoutMessage();
        }

        connected = false;
        authenticated = false;

        // Close resources
        try {
            if (messageReceiverThread != null && messageReceiverThread.isAlive()) {
                messageReceiverThread.interrupt();
            }

            if (in != null) {
                in.close();
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error during disconnect: " + e.getMessage());
        }

        notifyConnectionStatusChanged(false);
        logMessage("Disconnected from server");
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
        return authenticated && connected;
    }

    // ==================== AUTHENTICATION ====================

    /**
     * Attempts to login as admin
     */
    public void login(String email, String password) {
        logMessage("Starting login process for: " + email);

        // Validate input
        if (email == null || email.trim().isEmpty()) {
            notifyLoginResult(false, "Email cannot be empty");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            notifyLoginResult(false, "Password cannot be empty");
            return;
        }

        // Connect if not already connected
        if (!connected) {
            if (!connect()) {
                notifyLoginResult(false, "Failed to connect to server");
                return;
            }
        }

        // Store admin email
        this.adminEmail = email.trim();

        // Send login message
        String loginXML = createLoginXML(email.trim(), password);
        sendMessage(loginXML);
        logMessage("Login request sent for: " + email);
    }

    /**
     * Logs out from the server
     */
    public void logout() {
        if (authenticated) {
            sendLogoutMessage();
            authenticated = false;
            adminEmail = null;
            adminName = null;
            sessionId = null;
            logMessage("Logged out successfully");
        }
        disconnect();
    }

    // ==================== USER MANAGEMENT ====================

    /**
     * Requests all users from server
     */
    public void requestAllUsers() {
        if (!authenticated) {
            notifyError("Not authenticated");
            return;
        }

        String requestXML = createRequestXML("admin_getusers");
        sendMessage(requestXML);
        logMessage("Requested users list");
    }

    /**
     * Searches for users
     */
    public void searchUsers(String searchKey) {
        if (!authenticated) {
            notifyError("Not authenticated");
            return;
        }

        String searchXML = createSearchUsersXML(searchKey);
        sendMessage(searchXML);
        logMessage("Searching users for: " + searchKey);
    }

    /**
     * Adds a new user
     */
    public void addUser(String email, String password, String name, boolean isAdmin) {
        if (!authenticated) {
            notifyError("Not authenticated");
            return;
        }

        String addUserXML = createAddUserXML(email, password, name, isAdmin);
        sendMessage(addUserXML);
        logMessage("Adding new user: " + email);
    }

    /**
     * Updates user information
     */
    public void updateUser(String email, String newName, String newPassword) {
        if (!authenticated) {
            notifyError("Not authenticated");
            return;
        }

        String updateXML = createUpdateUserXML(email, newName, newPassword);
        sendMessage(updateXML);
        logMessage("Updating user: " + email);
    }

    /**
     * Deletes a user
     */
    public void deleteUser(String email) {
        if (!authenticated) {
            notifyError("Not authenticated");
            return;
        }

        if (email.equals("admin@chat.com")) {
            notifyError("Cannot delete the main admin account");
            return;
        }

        String deleteXML = createDeleteUserXML(email);
        sendMessage(deleteXML);
        logMessage("Deleting user: " + email);
    }

    // ==================== GROUP MANAGEMENT ====================

    /**
     * Requests all groups from server
     */
    public void requestAllGroups() {
        if (!authenticated) {
            notifyError("Not authenticated");
            return;
        }

        String requestXML = createRequestXML("admin_getgroups");
        sendMessage(requestXML);
        logMessage("Requested groups list");
    }

    /**
     * Deletes a group
     */
    public void deleteGroup(String groupId) {
        if (!authenticated) {
            notifyError("Not authenticated");
            return;
        }

        String deleteXML = createDeleteGroupXML(groupId);
        sendMessage(deleteXML);
        logMessage("Deleting group: " + groupId);
    }

    /**
     * Gets group members
     */
    public void getGroupMembers(String groupId) {
        if (!authenticated) {
            notifyError("Not authenticated");
            return;
        }

        String requestXML = createGetGroupMembersXML(groupId);
        sendMessage(requestXML);
        logMessage("Requested members for group: " + groupId);
    }

    // ==================== MESSAGE HANDLING ====================

    /**
     * Starts the message receiver thread
     */
    private void startMessageReceiver() {
        messageReceiverThread = new Thread(() -> {
            logMessage("Message receiver thread started");

            while (receiving && connected && !Thread.currentThread().isInterrupted()) {
                try {
                    if (in == null) break;

                    String message = in.readLine();
                    if (message != null) {
                        logMessage("Received: " + message);
                        processMessage(message);
                    } else {
                        // Connection closed by server
                        logMessage("Server closed connection");
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout is okay, continue
                    continue;
                } catch (IOException e) {
                    if (receiving && connected) {
                        logMessage("Error receiving message: " + e.getMessage());
                        System.err.println("[ERROR] Error receiving message: " + e.getMessage());
                    }
                    break;
                }
            }

            // Connection lost
            if (connected) {
                connected = false;
                notifyConnectionStatusChanged(false);
                logMessage("Message receiver thread terminated - connection lost");
            }
        });

        messageReceiverThread.setDaemon(true);
        messageReceiverThread.setName("AdminMessageReceiver");
        messageReceiverThread.start();
    }

    /**
     * Processes received XML messages
     */
    private void processMessage(String xmlMessage) {
        try {
            if (xmlMessage == null || xmlMessage.trim().isEmpty()) {
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlMessage)));
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            String messageType = root.getTagName();

            logMessage("Processing message type: " + messageType);

            if ("response".equals(messageType)) {
                String responseType = root.getAttribute("type");
                handleResponse(responseType, doc);
            } else if ("message".equals(messageType)) {
                String msgType = root.getAttribute("type");
                handleMessage(msgType, doc);
            } else {
                logMessage("Unknown message format: " + messageType);
            }

        } catch (Exception e) {
            logMessage("Error processing message: " + e.getMessage());
            System.err.println("[ERROR] Error processing XML: " + xmlMessage);
            e.printStackTrace();
        }
    }

    /**
     * Handles response messages
     */
    private void handleResponse(String responseType, Document doc) {
        logMessage("Handling response type: " + responseType);

        switch (responseType) {
            case "login":
                handleLoginResponse(doc);
                break;

            case "adminUsers":
                handleUsersResponse(doc);
                break;

            case "adminGroups":
                handleGroupsResponse(doc);
                break;

            case "groupMembers":
                handleGroupMembersResponse(doc);
                break;

            case "general":
                handleGeneralResponse(doc);
                break;

            default:
                logMessage("Unknown response type: " + responseType);
        }
    }

    /**
     * Handles message notifications
     */
    private void handleMessage(String messageType, Document doc) {
        switch (messageType) {
            case "error":
                handleErrorMessage(doc);
                break;

            case "forceLogout":
                handleForceLogout(doc);
                break;

            default:
                logMessage("Unknown message type: " + messageType);
        }
    }

    /**
     * Handles login response
     */
    private void handleLoginResponse(Document doc) {
        try {
            String successStr = getElementValue(doc, "success");
            boolean success = Boolean.parseBoolean(successStr);

            logMessage("Login response - success: " + success);

            if (success) {
                sessionId = getElementValue(doc, "sessionId");
                adminName = getElementValue(doc, "userName");
                String isAdminStr = getElementValue(doc, "isAdmin");
                boolean isAdmin = Boolean.parseBoolean(isAdminStr);

                logMessage("Session ID: " + sessionId + ", Admin Name: " + adminName + ", Is Admin: " + isAdmin);

                if (isAdmin) {
                    authenticated = true;
                    notifyLoginResult(true, "Login successful");
                    logMessage("Successfully authenticated as admin: " + adminEmail);
                } else {
                    notifyLoginResult(false, "Access denied: Admin privileges required");
                    logMessage("Login failed: Not an admin user");
                    disconnect();
                }
            } else {
                String errorMsg = getElementValue(doc, "message");
                if (errorMsg.isEmpty()) {
                    errorMsg = "Invalid credentials";
                }
                notifyLoginResult(false, errorMsg);
                logMessage("Login failed: " + errorMsg);
            }
        } catch (Exception e) {
            logMessage("Error handling login response: " + e.getMessage());
            notifyLoginResult(false, "Error processing login response");
        }
    }

    /**
     * Handles users list response
     */
    private void handleUsersResponse(Document doc) {
        usersList.clear();
        NodeList userNodes = doc.getElementsByTagName("user");

        for (int i = 0; i < userNodes.getLength(); i++) {
            Element userElement = (Element) userNodes.item(i);
            Map<String, String> user = new HashMap<>();
            user.put("email", getElementValue(userElement, "email"));
            user.put("name", getElementValue(userElement, "name"));
            user.put("isAdmin", getElementValue(userElement, "isAdmin"));
            user.put("created", getElementValue(userElement, "created"));
            usersList.add(user);
        }

        notifyUsersDataReceived(new ArrayList<>(usersList));
        logMessage("Received " + usersList.size() + " users");
    }

    /**
     * Handles groups list response
     */
    private void handleGroupsResponse(Document doc) {
        groupsList.clear();
        NodeList groupNodes = doc.getElementsByTagName("group");

        for (int i = 0; i < groupNodes.getLength(); i++) {
            Element groupElement = (Element) groupNodes.item(i);
            Map<String, String> group = new HashMap<>();
            group.put("id", getElementValue(groupElement, "id"));
            group.put("name", getElementValue(groupElement, "name"));
            group.put("creator", getElementValue(groupElement, "creator"));
            group.put("memberCount", getElementValue(groupElement, "memberCount"));
            group.put("created", getElementValue(groupElement, "created"));
            groupsList.add(group);
        }

        notifyGroupsDataReceived(new ArrayList<>(groupsList));
        logMessage("Received " + groupsList.size() + " groups");
    }

    /**
     * Handles group members response
     */
    private void handleGroupMembersResponse(Document doc) {
        String groupId = getElementValue(doc, "groupId");
        NodeList memberNodes = doc.getElementsByTagName("member");

        StringBuilder members = new StringBuilder("Group " + groupId + " Members:\n");
        for (int i = 0; i < memberNodes.getLength(); i++) {
            members.append("- ").append(memberNodes.item(i).getTextContent()).append("\n");
        }

        notifyMessage(members.toString());
        logMessage("Received members for group: " + groupId);
    }

    /**
     * Handles general response
     */
    private void handleGeneralResponse(Document doc) {
        String successStr = getElementValue(doc, "success");
        boolean success = Boolean.parseBoolean(successStr);
        String message = getElementValue(doc, "message");

        notifyOperationResult(success, message);
        logMessage("Operation result: " + message);
    }

    /**
     * Handles error messages
     */
    private void handleErrorMessage(Document doc) {
        String error = getElementValue(doc, "error");
        if (error.isEmpty()) {
            error = getElementValue(doc, "message");
        }
        notifyError(error);
        logMessage("Error received: " + error);
    }

    /**
     * Handles force logout
     */
    private void handleForceLogout(Document doc) {
        String reason = getElementValue(doc, "reason");
        authenticated = false;
        notifyError("Forced logout: " + reason);
        logMessage("Forced logout: " + reason);
        disconnect();
    }

    /**
     * Sends a message to the server
     */
    private void sendMessage(String message) {
        if (out != null && connected) {
            try {
                logMessage("Sending: " + message);
                out.println(message);
                out.flush();
            } catch (Exception e) {
                logMessage("Error sending message: " + e.getMessage());
                connected = false;
                notifyConnectionStatusChanged(false);
            }
        } else {
            logMessage("Cannot send message - not connected");
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
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"login\">" +
                "<email><![CDATA[" + email + "]]></email>" +
                "<password><![CDATA[" + password + "]]></password>" +
                "</message>";
    }

    private String createRequestXML(String requestType) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"" + requestType + "\"/>";
    }

    private String createSearchUsersXML(String searchKey) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"admin_searchusers\">" +
                "<searchKey><![CDATA[" + searchKey + "]]></searchKey>" +
                "</message>";
    }

    private String createAddUserXML(String email, String password, String name, boolean isAdmin) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"admin_adduser\">" +
                "<email><![CDATA[" + email + "]]></email>" +
                "<password><![CDATA[" + password + "]]></password>" +
                "<name><![CDATA[" + name + "]]></name>" +
                "<isAdmin>" + isAdmin + "</isAdmin>" +
                "</message>";
    }

    private String createUpdateUserXML(String email, String newName, String newPassword) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<message type=\"admin_updateuser\">");
        xml.append("<email><![CDATA[").append(email).append("]]></email>");
        if (newName != null && !newName.trim().isEmpty()) {
            xml.append("<name><![CDATA[").append(newName.trim()).append("]]></name>");
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            xml.append("<password><![CDATA[").append(newPassword).append("]]></password>");
        }
        xml.append("</message>");
        return xml.toString();
    }

    private String createDeleteUserXML(String email) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"admin_deleteuser\">" +
                "<email><![CDATA[" + email + "]]></email>" +
                "</message>";
    }

    private String createDeleteGroupXML(String groupId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"admin_deletegroup\">" +
                "<groupId><![CDATA[" + groupId + "]]></groupId>" +
                "</message>";
    }

    private String createGetGroupMembersXML(String groupId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"admin_getgroupmembers\">" +
                "<groupId><![CDATA[" + groupId + "]]></groupId>" +
                "</message>";
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Gets element value from XML document
     */
    private String getElementValue(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent() != null ? node.getTextContent().trim() : "";
        }
        return "";
    }

    /**
     * Gets element value from parent element
     */
    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent() != null ? node.getTextContent().trim() : "";
        }
        return "";
    }

    /**
     * Logs a message with timestamp
     */
    private void logMessage(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
        messageLog.add(logEntry);
        System.out.println(logEntry); // Also print to console for debugging

        // Notify listeners
        notifyMessage(logEntry);
    }

    // ==================== LISTENER MANAGEMENT ====================

    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }

    private void notifyConnectionStatusChanged(boolean connected) {
        for (ModelListener listener : listeners) {
            try {
                listener.onConnectionStatusChanged(connected);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyLoginResult(boolean success, String message) {
        for (ModelListener listener : listeners) {
            try {
                listener.onLoginResult(success, message);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyUsersDataReceived(List<Map<String, String>> users) {
        for (ModelListener listener : listeners) {
            try {
                listener.onUsersDataReceived(users);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyGroupsDataReceived(List<Map<String, String>> groups) {
        for (ModelListener listener : listeners) {
            try {
                listener.onGroupsDataReceived(groups);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyMessage(String message) {
        for (ModelListener listener : listeners) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyError(String error) {
        for (ModelListener listener : listeners) {
            try {
                listener.onErrorReceived(error);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void notifyOperationResult(boolean success, String message) {
        for (ModelListener listener : listeners) {
            try {
                listener.onOperationResult(success, message);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    // ==================== GETTERS ====================

    public String getAdminEmail() {
        return adminEmail;
    }

    public String getAdminName() {
        return adminName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<Map<String, String>> getUsersList() {
        return new ArrayList<>(usersList);
    }

    public List<Map<String, String>> getGroupsList() {
        return new ArrayList<>(groupsList);
    }

    public List<String> getMessageLog() {
        return new ArrayList<>(messageLog);
    }
}
