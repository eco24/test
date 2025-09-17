package server;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Manages all XML file operations for data persistence
 * Handles users, messages, groups, and friends data
 */
public class XMLDataManager {
    // File paths
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.xml";
    private static final String MESSAGES_FILE = DATA_DIR + "/messages.xml";
    private static final String GROUPS_FILE = DATA_DIR + "/groups.xml";
    private static final String FRIENDS_FILE = DATA_DIR + "/friends.xml";

    // XML processing objects
    private DocumentBuilderFactory dbFactory;
    private DocumentBuilder dBuilder;
    private TransformerFactory transformerFactory;
    private Transformer transformer;

    // Date formatter
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Synchronization locks for thread safety
    private final Object usersLock = new Object();
    private final Object messagesLock = new Object();
    private final Object groupsLock = new Object();
    private final Object friendsLock = new Object();

    /**
     * Constructor initializes XML processors and data files
     */
    public XMLDataManager() {
        try {
            // Initialize XML processors
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            // Initialize data files
            initializeDataFiles();

            System.out.println("[DATA] XML Data Manager initialized");

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to initialize XML Data Manager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes all required XML data files
     */
    private void initializeDataFiles() {
        try {
            // Create data directory if it doesn't exist
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
                System.out.println("[DATA] Created data directory");
            }

            // Initialize each XML file
            initializeXMLFile(USERS_FILE, "users");
            initializeXMLFile(MESSAGES_FILE, "messages");
            initializeXMLFile(GROUPS_FILE, "groups");
            initializeXMLFile(FRIENDS_FILE, "friends");

            // Create default admin account if it doesn't exist
            createDefaultAdmin();

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to initialize data files: " + e.getMessage());
        }
    }

    /**
     * Initializes a single XML file with root element
     */
    private void initializeXMLFile(String filename, String rootElement) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                Document doc = dBuilder.newDocument();
                Element root = doc.createElement(rootElement);
                doc.appendChild(root);
                saveDocument(doc, filename);
                System.out.println("[DATA] Created " + filename);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to create " + filename + ": " + e.getMessage());
            }
        }
    }

    /**
     * Creates default admin account if it doesn't exist
     */
    private void createDefaultAdmin() {
        try {
            // Small delay to ensure files are initialized
            Thread.sleep(100);

            if (!userExists("admin@chat.com")) {
                boolean success = registerUser("admin@chat.com", "admin123", "Administrator", true);
                if (success) {
                    System.out.println("[DATA] Created default admin account (admin@chat.com / admin123)");
                } else {
                    System.err.println("[ERROR] Failed to create default admin account");
                }
            } else {
                System.out.println("[DATA] Admin account already exists");
            }
        } catch (InterruptedException e) {
            System.err.println("[ERROR] Interrupted while creating admin account");
        }
    }

    // ==================== USER MANAGEMENT ====================

    /**
     * Registers a new user
     */
    public boolean registerUser(String email, String password, String name, boolean isAdmin) {
        synchronized (usersLock) {
            try {
                // Check if user already exists
                if (userExists(email)) {
                    return false;
                }

                Document doc = parseXMLFile(USERS_FILE);
                Element root = doc.getDocumentElement();

                // Create new user element
                Element user = doc.createElement("user");

                Element emailEl = doc.createElement("email");
                emailEl.setTextContent(email);
                user.appendChild(emailEl);

                Element passwordEl = doc.createElement("password");
                passwordEl.setTextContent(hashPassword(password));
                user.appendChild(passwordEl);

                Element nameEl = doc.createElement("name");
                nameEl.setTextContent(name);
                user.appendChild(nameEl);

                Element isAdminEl = doc.createElement("isAdmin");
                isAdminEl.setTextContent(String.valueOf(isAdmin));
                user.appendChild(isAdminEl);

                Element createdEl = doc.createElement("created");
                createdEl.setTextContent(LocalDateTime.now().format(DATE_FORMATTER));
                user.appendChild(createdEl);

                root.appendChild(user);
                saveDocument(doc, USERS_FILE);

                System.out.println("[DATA] Registered user: " + email);
                return true;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to register user: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Validates user credentials
     */
    public boolean validateUser(String email, String password) {
        synchronized (usersLock) {
            try {
                Document doc = parseXMLFile(USERS_FILE);
                NodeList users = doc.getElementsByTagName("user");

                String hashedPassword = hashPassword(password);

                for (int i = 0; i < users.getLength(); i++) {
                    Element user = (Element) users.item(i);
                    String userEmail = getElementValue(user, "email");
                    String userPassword = getElementValue(user, "password");

                    if (email.equals(userEmail) && hashedPassword.equals(userPassword)) {
                        return true;
                    }
                }

                return false;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to validate user: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Checks if a user exists
     */
    public boolean userExists(String email) {
        synchronized (usersLock) {
            try {
                Document doc = parseXMLFile(USERS_FILE);
                NodeList users = doc.getElementsByTagName("user");

                for (int i = 0; i < users.getLength(); i++) {
                    Element user = (Element) users.item(i);
                    if (email.equals(getElementValue(user, "email"))) {
                        return true;
                    }
                }

                return false;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to check user existence: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Gets user's name
     */
    public String getUserName(String email) {
        synchronized (usersLock) {
            try {
                Document doc = parseXMLFile(USERS_FILE);
                NodeList users = doc.getElementsByTagName("user");

                for (int i = 0; i < users.getLength(); i++) {
                    Element user = (Element) users.item(i);
                    if (email.equals(getElementValue(user, "email"))) {
                        return getElementValue(user, "name");
                    }
                }

                return null;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to get user name: " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Checks if user is admin
     */
    public boolean isUserAdmin(String email) {
        synchronized (usersLock) {
            try {
                Document doc = parseXMLFile(USERS_FILE);
                NodeList users = doc.getElementsByTagName("user");

                for (int i = 0; i < users.getLength(); i++) {
                    Element user = (Element) users.item(i);
                    if (email.equals(getElementValue(user, "email"))) {
                        return Boolean.parseBoolean(getElementValue(user, "isAdmin"));
                    }
                }

                return false;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to check admin status: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Gets all users (for admin)
     */
    public List<Map<String, String>> getAllUsers() {
        synchronized (usersLock) {
            List<Map<String, String>> usersList = new ArrayList<>();

            try {
                Document doc = parseXMLFile(USERS_FILE);
                NodeList users = doc.getElementsByTagName("user");

                for (int i = 0; i < users.getLength(); i++) {
                    Element user = (Element) users.item(i);
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("email", getElementValue(user, "email"));
                    userMap.put("name", getElementValue(user, "name"));
                    userMap.put("isAdmin", getElementValue(user, "isAdmin"));
                    userMap.put("created", getElementValue(user, "created"));
                    usersList.add(userMap);
                }

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to get all users: " + e.getMessage());
            }

            return usersList;
        }
    }

    /**
     * Deletes a user (admin function)
     */
    public boolean deleteUser(String email) {
        synchronized (usersLock) {
            try {
                Document doc = parseXMLFile(USERS_FILE);
                NodeList users = doc.getElementsByTagName("user");

                for (int i = 0; i < users.getLength(); i++) {
                    Element user = (Element) users.item(i);
                    if (email.equals(getElementValue(user, "email"))) {
                        user.getParentNode().removeChild(user);
                        saveDocument(doc, USERS_FILE);

                        // Also remove user's friends and group memberships
                        removeUserFromFriends(email);
                        removeUserFromGroups(email);

                        System.out.println("[DATA] Deleted user: " + email);
                        return true;
                    }
                }

                return false;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to delete user: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Updates user information (admin function)
     */
    public boolean updateUser(String email, String newName, String newPassword) {
        synchronized (usersLock) {
            try {
                Document doc = parseXMLFile(USERS_FILE);
                NodeList users = doc.getElementsByTagName("user");

                for (int i = 0; i < users.getLength(); i++) {
                    Element user = (Element) users.item(i);
                    if (email.equals(getElementValue(user, "email"))) {
                        if (newName != null && !newName.isEmpty()) {
                            updateElementValue(user, "name", newName);
                        }
                        if (newPassword != null && !newPassword.isEmpty()) {
                            updateElementValue(user, "password", hashPassword(newPassword));
                        }

                        saveDocument(doc, USERS_FILE);
                        System.out.println("[DATA] Updated user: " + email);
                        return true;
                    }
                }

                return false;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to update user: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Searches for users
     */
    public List<Map<String, String>> searchUsers(String searchKey, String currentUser) {
        synchronized (usersLock) {
            List<Map<String, String>> results = new ArrayList<>();
            List<String> friends = getFriendEmails(currentUser);

            try {
                Document doc = parseXMLFile(USERS_FILE);
                NodeList users = doc.getElementsByTagName("user");

                String searchLower = searchKey.toLowerCase();

                // First add friends that match
                for (int i = 0; i < users.getLength(); i++) {
                    Element user = (Element) users.item(i);
                    String email = getElementValue(user, "email");
                    String name = getElementValue(user, "name");

                    if (!email.equals(currentUser) && friends.contains(email)) {
                        if (name.toLowerCase().contains(searchLower) ||
                                email.toLowerCase().contains(searchLower)) {
                            Map<String, String> result = new HashMap<>();
                            result.put("email", email);
                            result.put("name", name);
                            result.put("isFriend", "true");
                            results.add(result);
                        }
                    }
                }

                // Then add non-friends that match
                for (int i = 0; i < users.getLength(); i++) {
                    Element user = (Element) users.item(i);
                    String email = getElementValue(user, "email");
                    String name = getElementValue(user, "name");

                    if (!email.equals(currentUser) && !friends.contains(email)) {
                        if (name.toLowerCase().contains(searchLower) ||
                                email.toLowerCase().contains(searchLower)) {
                            Map<String, String> result = new HashMap<>();
                            result.put("email", email);
                            result.put("name", name);
                            result.put("isFriend", "false");
                            results.add(result);
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to search users: " + e.getMessage());
            }

            return results;
        }
    }

    // ==================== MESSAGE MANAGEMENT ====================

    /**
     * Stores a broadcast message
     */
    public void storeBroadcastMessage(String sender, String content) {
        synchronized (messagesLock) {
            try {
                Document doc = parseXMLFile(MESSAGES_FILE);
                Element root = doc.getDocumentElement();

                Element message = doc.createElement("message");
                message.setAttribute("type", "broadcast");
                message.setAttribute("id", generateMessageId());

                Element senderEl = doc.createElement("sender");
                senderEl.setTextContent(sender);
                message.appendChild(senderEl);

                Element contentEl = doc.createElement("content");
                contentEl.setTextContent(content);
                message.appendChild(contentEl);

                Element timestampEl = doc.createElement("timestamp");
                timestampEl.setTextContent(LocalDateTime.now().format(DATE_FORMATTER));
                message.appendChild(timestampEl);

                root.appendChild(message);
                saveDocument(doc, MESSAGES_FILE);

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to store broadcast message: " + e.getMessage());
            }
        }
    }

    /**
     * Stores an offline message for a user
     */
    public void storeOfflineMessage(String recipient, String sender, String content) {
        synchronized (messagesLock) {
            try {
                Document doc = parseXMLFile(MESSAGES_FILE);
                Element root = doc.getDocumentElement();

                Element message = doc.createElement("message");
                message.setAttribute("type", "offline");
                message.setAttribute("id", generateMessageId());

                Element senderEl = doc.createElement("sender");
                senderEl.setTextContent(sender);
                message.appendChild(senderEl);

                Element recipientEl = doc.createElement("recipient");
                recipientEl.setTextContent(recipient);
                message.appendChild(recipientEl);

                Element contentEl = doc.createElement("content");
                contentEl.setTextContent(content);
                message.appendChild(contentEl);

                Element timestampEl = doc.createElement("timestamp");
                timestampEl.setTextContent(LocalDateTime.now().format(DATE_FORMATTER));
                message.appendChild(timestampEl);

                Element deliveredEl = doc.createElement("delivered");
                deliveredEl.setTextContent("false");
                message.appendChild(deliveredEl);

                root.appendChild(message);
                saveDocument(doc, MESSAGES_FILE);

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to store offline message: " + e.getMessage());
            }
        }
    }

    /**
     * Stores a group message for offline members
     */
    public void storeGroupMessage(String groupId, String recipient, String sender, String content) {
        synchronized (messagesLock) {
            try {
                Document doc = parseXMLFile(MESSAGES_FILE);
                Element root = doc.getDocumentElement();

                Element message = doc.createElement("message");
                message.setAttribute("type", "group_offline");
                message.setAttribute("id", generateMessageId());

                Element groupIdEl = doc.createElement("groupId");
                groupIdEl.setTextContent(groupId);
                message.appendChild(groupIdEl);

                Element senderEl = doc.createElement("sender");
                senderEl.setTextContent(sender);
                message.appendChild(senderEl);

                Element recipientEl = doc.createElement("recipient");
                recipientEl.setTextContent(recipient);
                message.appendChild(recipientEl);

                Element contentEl = doc.createElement("content");
                contentEl.setTextContent(content);
                message.appendChild(contentEl);

                Element timestampEl = doc.createElement("timestamp");
                timestampEl.setTextContent(LocalDateTime.now().format(DATE_FORMATTER));
                message.appendChild(timestampEl);

                Element deliveredEl = doc.createElement("delivered");
                deliveredEl.setTextContent("false");
                message.appendChild(deliveredEl);

                root.appendChild(message);
                saveDocument(doc, MESSAGES_FILE);

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to store group message: " + e.getMessage());
            }
        }
    }

    /**
     * Gets offline messages for a user and marks them as delivered
     */
    public List<String> getOfflineMessages(String userEmail) {
        synchronized (messagesLock) {
            List<String> messages = new ArrayList<>();

            try {
                Document doc = parseXMLFile(MESSAGES_FILE);
                NodeList messageNodes = doc.getElementsByTagName("message");

                for (int i = 0; i < messageNodes.getLength(); i++) {
                    Element message = (Element) messageNodes.item(i);
                    String type = message.getAttribute("type");

                    if (("offline".equals(type) || "group_offline".equals(type)) &&
                            userEmail.equals(getElementValue(message, "recipient")) &&
                            "false".equals(getElementValue(message, "delivered"))) {

                        // Build XML message to send
                        String xmlMessage = buildOfflineMessageXML(message, type);
                        messages.add(xmlMessage);

                        // Mark as delivered
                        updateElementValue(message, "delivered", "true");
                    }
                }

                if (!messages.isEmpty()) {
                    saveDocument(doc, MESSAGES_FILE);
                }

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to get offline messages: " + e.getMessage());
            }

            return messages;
        }
    }

    /**
     * Builds XML message from stored offline message
     */
    private String buildOfflineMessageXML(Element message, String type) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        if ("offline".equals(type)) {
            xml.append("<message type=\"private\">");
            xml.append("<sender>").append(getElementValue(message, "sender")).append("</sender>");
            xml.append("<content>").append(escapeXML(getElementValue(message, "content"))).append("</content>");
            xml.append("<timestamp>").append(getElementValue(message, "timestamp")).append("</timestamp>");
            xml.append("<offline>true</offline>");
            xml.append("</message>");
        } else if ("group_offline".equals(type)) {
            xml.append("<message type=\"group\">");
            xml.append("<groupId>").append(getElementValue(message, "groupId")).append("</groupId>");
            xml.append("<sender>").append(getElementValue(message, "sender")).append("</sender>");
            xml.append("<content>").append(escapeXML(getElementValue(message, "content"))).append("</content>");
            xml.append("<timestamp>").append(getElementValue(message, "timestamp")).append("</timestamp>");
            xml.append("<offline>true</offline>");
            xml.append("</message>");
        }

        return xml.toString();
    }

    // ==================== GROUP MANAGEMENT ====================

    /**
     * Creates a new group
     */
    public String createGroup(String groupName, String creator, List<String> members) {
        synchronized (groupsLock) {
            try {
                Document doc = parseXMLFile(GROUPS_FILE);
                Element root = doc.getDocumentElement();

                String groupId = "GRP_" + System.currentTimeMillis();

                Element group = doc.createElement("group");
                group.setAttribute("id", groupId);

                Element nameEl = doc.createElement("name");
                nameEl.setTextContent(groupName);
                group.appendChild(nameEl);

                Element creatorEl = doc.createElement("creator");
                creatorEl.setTextContent(creator);
                group.appendChild(creatorEl);

                Element membersEl = doc.createElement("members");
                for (String member : members) {
                    Element memberEl = doc.createElement("member");
                    memberEl.setTextContent(member);
                    membersEl.appendChild(memberEl);
                }
                group.appendChild(membersEl);

                Element createdEl = doc.createElement("created");
                createdEl.setTextContent(LocalDateTime.now().format(DATE_FORMATTER));
                group.appendChild(createdEl);

                root.appendChild(group);
                saveDocument(doc, GROUPS_FILE);

                System.out.println("[DATA] Created group: " + groupName + " (ID: " + groupId + ")");
                return groupId;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to create group: " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Gets all groups for a user
     */
    public List<Map<String, String>> getUserGroups(String userEmail) {
        synchronized (groupsLock) {
            List<Map<String, String>> userGroups = new ArrayList<>();

            try {
                Document doc = parseXMLFile(GROUPS_FILE);
                NodeList groups = doc.getElementsByTagName("group");

                for (int i = 0; i < groups.getLength(); i++) {
                    Element group = (Element) groups.item(i);
                    NodeList members = group.getElementsByTagName("member");

                    for (int j = 0; j < members.getLength(); j++) {
                        if (userEmail.equals(members.item(j).getTextContent())) {
                            Map<String, String> groupInfo = new HashMap<>();
                            groupInfo.put("id", group.getAttribute("id"));
                            groupInfo.put("name", getElementValue(group, "name"));
                            groupInfo.put("creator", getElementValue(group, "creator"));
                            groupInfo.put("memberCount", String.valueOf(members.getLength()));
                            userGroups.add(groupInfo);
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to get user groups: " + e.getMessage());
            }

            return userGroups;
        }
    }

    /**
     * Gets members of a group
     */
    public List<String> getGroupMembers(String groupId) {
        synchronized (groupsLock) {
            List<String> membersList = new ArrayList<>();

            try {
                Document doc = parseXMLFile(GROUPS_FILE);
                NodeList groups = doc.getElementsByTagName("group");

                for (int i = 0; i < groups.getLength(); i++) {
                    Element group = (Element) groups.item(i);
                    if (groupId.equals(group.getAttribute("id"))) {
                        NodeList members = group.getElementsByTagName("member");
                        for (int j = 0; j < members.getLength(); j++) {
                            membersList.add(members.item(j).getTextContent());
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to get group members: " + e.getMessage());
            }

            return membersList;
        }
    }

    /**
     * Checks if user is member of a group
     */
    public boolean isGroupMember(String groupId, String userEmail) {
        List<String> members = getGroupMembers(groupId);
        return members.contains(userEmail);
    }

    /**
     * Deletes a group
     */
    public boolean deleteGroup(String groupId) {
        synchronized (groupsLock) {
            try {
                Document doc = parseXMLFile(GROUPS_FILE);
                NodeList groups = doc.getElementsByTagName("group");

                for (int i = 0; i < groups.getLength(); i++) {
                    Element group = (Element) groups.item(i);
                    if (groupId.equals(group.getAttribute("id"))) {
                        group.getParentNode().removeChild(group);
                        saveDocument(doc, GROUPS_FILE);
                        System.out.println("[DATA] Deleted group: " + groupId);
                        return true;
                    }
                }

                return false;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to delete group: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Removes user from all groups
     */
    private void removeUserFromGroups(String userEmail) {
        synchronized (groupsLock) {
            try {
                Document doc = parseXMLFile(GROUPS_FILE);
                NodeList groups = doc.getElementsByTagName("group");
                boolean modified = false;

                for (int i = 0; i < groups.getLength(); i++) {
                    Element group = (Element) groups.item(i);
                    NodeList members = group.getElementsByTagName("member");

                    for (int j = members.getLength() - 1; j >= 0; j--) {
                        Element member = (Element) members.item(j);
                        if (userEmail.equals(member.getTextContent())) {
                            member.getParentNode().removeChild(member);
                            modified = true;
                        }
                    }
                }

                if (modified) {
                    saveDocument(doc, GROUPS_FILE);
                }

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to remove user from groups: " + e.getMessage());
            }
        }
    }

    // ==================== FRIEND MANAGEMENT ====================

    /**
     * Sends a friend request
     */
    public boolean sendFriendRequest(String fromUser, String toUser) {
        synchronized (friendsLock) {
            try {
                // Check if users exist
                if (!userExists(toUser)) {
                    return false;
                }

                // Check if already friends or request already exists
                if (areFriends(fromUser, toUser) || hasPendingRequest(fromUser, toUser)) {
                    return false;
                }

                Document doc = parseXMLFile(FRIENDS_FILE);
                Element root = doc.getDocumentElement();

                Element request = doc.createElement("request");
                request.setAttribute("id", "REQ_" + System.currentTimeMillis());

                Element fromEl = doc.createElement("from");
                fromEl.setTextContent(fromUser);
                request.appendChild(fromEl);

                Element toEl = doc.createElement("to");
                toEl.setTextContent(toUser);
                request.appendChild(toEl);

                Element statusEl = doc.createElement("status");
                statusEl.setTextContent("pending");
                request.appendChild(statusEl);

                Element timestampEl = doc.createElement("timestamp");
                timestampEl.setTextContent(LocalDateTime.now().format(DATE_FORMATTER));
                request.appendChild(timestampEl);

                root.appendChild(request);
                saveDocument(doc, FRIENDS_FILE);

                System.out.println("[DATA] Friend request from " + fromUser + " to " + toUser);
                return true;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to send friend request: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Responds to a friend request (accept/reject)
     */
    public boolean respondToFriendRequest(String fromUser, String toUser, boolean accept) {
        synchronized (friendsLock) {
            try {
                Document doc = parseXMLFile(FRIENDS_FILE);
                NodeList requests = doc.getElementsByTagName("request");
                Element targetRequest = null;

                // Find the request
                for (int i = 0; i < requests.getLength(); i++) {
                    Element request = (Element) requests.item(i);
                    if (fromUser.equals(getElementValue(request, "from")) &&
                            toUser.equals(getElementValue(request, "to")) &&
                            "pending".equals(getElementValue(request, "status"))) {
                        targetRequest = request;
                        break;
                    }
                }

                if (targetRequest == null) {
                    return false;
                }

                if (accept) {
                    // Update request status
                    updateElementValue(targetRequest, "status", "accepted");

                    // Create friendship
                    Element root = doc.getDocumentElement();
                    Element friendship = doc.createElement("friendship");
                    friendship.setAttribute("id", "FRD_" + System.currentTimeMillis());

                    Element user1El = doc.createElement("user1");
                    user1El.setTextContent(fromUser);
                    friendship.appendChild(user1El);

                    Element user2El = doc.createElement("user2");
                    user2El.setTextContent(toUser);
                    friendship.appendChild(user2El);

                    Element createdEl = doc.createElement("created");
                    createdEl.setTextContent(LocalDateTime.now().format(DATE_FORMATTER));
                    friendship.appendChild(createdEl);

                    root.appendChild(friendship);

                    System.out.println("[DATA] Friendship created between " + fromUser + " and " + toUser);
                } else {
                    // Update request status to rejected
                    updateElementValue(targetRequest, "status", "rejected");
                    System.out.println("[DATA] Friend request rejected: " + fromUser + " to " + toUser);
                }

                saveDocument(doc, FRIENDS_FILE);
                return true;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to respond to friend request: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Checks if two users are friends
     */
    public boolean areFriends(String user1, String user2) {
        synchronized (friendsLock) {
            try {
                Document doc = parseXMLFile(FRIENDS_FILE);
                NodeList friendships = doc.getElementsByTagName("friendship");

                for (int i = 0; i < friendships.getLength(); i++) {
                    Element friendship = (Element) friendships.item(i);
                    String u1 = getElementValue(friendship, "user1");
                    String u2 = getElementValue(friendship, "user2");

                    if ((u1.equals(user1) && u2.equals(user2)) ||
                            (u1.equals(user2) && u2.equals(user1))) {
                        return true;
                    }
                }

                return false;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to check friendship: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Checks if there's a pending request between users
     */
    private boolean hasPendingRequest(String fromUser, String toUser) {
        synchronized (friendsLock) {
            try {
                Document doc = parseXMLFile(FRIENDS_FILE);
                NodeList requests = doc.getElementsByTagName("request");

                for (int i = 0; i < requests.getLength(); i++) {
                    Element request = (Element) requests.item(i);
                    String from = getElementValue(request, "from");
                    String to = getElementValue(request, "to");
                    String status = getElementValue(request, "status");

                    if ("pending".equals(status) &&
                            ((from.equals(fromUser) && to.equals(toUser)) ||
                                    (from.equals(toUser) && to.equals(fromUser)))) {
                        return true;
                    }
                }

                return false;

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to check pending request: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Gets user's friends list
     */
    public List<Map<String, String>> getUserFriends(String userEmail) {
        synchronized (friendsLock) {
            List<Map<String, String>> friendsList = new ArrayList<>();

            try {
                Document doc = parseXMLFile(FRIENDS_FILE);
                NodeList friendships = doc.getElementsByTagName("friendship");

                System.out.println("[DATA] Looking for friends of: " + userEmail);
                System.out.println("[DATA] Total friendships in file: " + friendships.getLength());

                for (int i = 0; i < friendships.getLength(); i++) {
                    Element friendship = (Element) friendships.item(i);
                    String user1 = getElementValue(friendship, "user1");
                    String user2 = getElementValue(friendship, "user2");

                    String friendEmail = null;
                    if (user1.equals(userEmail)) {
                        friendEmail = user2;
                    } else if (user2.equals(userEmail)) {
                        friendEmail = user1;
                    }

                    if (friendEmail != null) {
                        String friendName = getUserName(friendEmail);
                        if (friendName == null || friendName.isEmpty()) {
                            friendName = friendEmail; // Use email as fallback
                        }

                        Map<String, String> friendInfo = new HashMap<>();
                        friendInfo.put("email", friendEmail);
                        friendInfo.put("name", friendName);
                        friendsList.add(friendInfo);

                        System.out.println("[DATA] Found friend: " + friendName + " (" + friendEmail + ")");
                    }
                }

                System.out.println("[DATA] Total friends found: " + friendsList.size());

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to get user friends: " + e.getMessage());
                e.printStackTrace();
            }

            return friendsList;
        }
    }

    /**
     * Gets friend emails only
     */
    private List<String> getFriendEmails(String userEmail) {
        List<String> emails = new ArrayList<>();
        for (Map<String, String> friend : getUserFriends(userEmail)) {
            emails.add(friend.get("email"));
        }
        return emails;
    }

    /**
     * Gets pending friend requests for a user
     */
    public List<Map<String, String>> getPendingFriendRequests(String userEmail) {
        synchronized (friendsLock) {
            List<Map<String, String>> pendingRequests = new ArrayList<>();

            try {
                Document doc = parseXMLFile(FRIENDS_FILE);
                NodeList requests = doc.getElementsByTagName("request");

                for (int i = 0; i < requests.getLength(); i++) {
                    Element request = (Element) requests.item(i);
                    if (userEmail.equals(getElementValue(request, "to")) &&
                            "pending".equals(getElementValue(request, "status"))) {

                        String fromEmail = getElementValue(request, "from");
                        Map<String, String> requestInfo = new HashMap<>();
                        requestInfo.put("from", fromEmail);
                        requestInfo.put("fromName", getUserName(fromEmail));
                        requestInfo.put("timestamp", getElementValue(request, "timestamp"));
                        pendingRequests.add(requestInfo);
                    }
                }

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to get pending requests: " + e.getMessage());
            }

            return pendingRequests;
        }
    }

    /**
     * Removes user from all friendships
     */
    private void removeUserFromFriends(String userEmail) {
        synchronized (friendsLock) {
            try {
                Document doc = parseXMLFile(FRIENDS_FILE);
                boolean modified = false;

                // Remove friendships
                NodeList friendships = doc.getElementsByTagName("friendship");
                for (int i = friendships.getLength() - 1; i >= 0; i--) {
                    Element friendship = (Element) friendships.item(i);
                    String user1 = getElementValue(friendship, "user1");
                    String user2 = getElementValue(friendship, "user2");

                    if (user1.equals(userEmail) || user2.equals(userEmail)) {
                        friendship.getParentNode().removeChild(friendship);
                        modified = true;
                    }
                }

                // Remove requests
                NodeList requests = doc.getElementsByTagName("request");
                for (int i = requests.getLength() - 1; i >= 0; i--) {
                    Element request = (Element) requests.item(i);
                    String from = getElementValue(request, "from");
                    String to = getElementValue(request, "to");

                    if (from.equals(userEmail) || to.equals(userEmail)) {
                        request.getParentNode().removeChild(request);
                        modified = true;
                    }
                }

                if (modified) {
                    saveDocument(doc, FRIENDS_FILE);
                }

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to remove user from friends: " + e.getMessage());
            }
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Parses an XML file
     */
    private Document parseXMLFile(String filename) throws Exception {
        File file = new File(filename);
        return dBuilder.parse(file);
    }

    /**
     * Saves a document to XML file
     */
    private void saveDocument(Document doc, String filename) throws TransformerException {
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filename));
        transformer.transform(source, result);
    }

    /**
     * Gets element value from parent
     */
    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    /**
     * Updates element value
     */
    private void updateElementValue(Element parent, String tagName, String newValue) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            nodeList.item(0).setTextContent(newValue);
        }
    }

    /**
     * Hashes a password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            // Fallback to plain text if hashing fails (not recommended for production)
            System.err.println("[WARNING] Password hashing failed, using plain text");
            return password;
        }
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
     * Generates a unique message ID
     */
    private String generateMessageId() {
        return "MSG_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
    }
}