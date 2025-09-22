package client.Admin.model.network;

import client.Admin.model.AdminModel;
import client.Admin.model.data.AdminSession;
import client.Admin.model.data.DataCache;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.util.*;

/**
 * Processes incoming XML messages from the server
 */
public class MessageProcessor {

    private final AdminSession session;
    private final DataCache dataCache;
    private final AdminModel model;

    /**
     * Constructor
     * @param session The admin session
     * @param dataCache The data cache
     * @param model The admin model for callbacks
     */
    public MessageProcessor(AdminSession session, DataCache dataCache, AdminModel model) {
        this.session = session;
        this.dataCache = dataCache;
        this.model = model;
    }

    /**
     * Processes received XML messages
     */
    public void processMessage(String xmlMessage) {
        try {
            if (xmlMessage == null || xmlMessage.trim().isEmpty()) {
                return;
            }

            Document doc = parseXmlMessage(xmlMessage);
            if (doc == null) return;

            Element root = doc.getDocumentElement();
            String messageType = root.getTagName();

            System.out.println("[PROCESSOR] Processing message type: " + messageType);

            switch (messageType) {
                case "response":
                    handleResponse(root.getAttribute("type"), doc);
                    break;
                case "message":
                    handleMessage(root.getAttribute("type"), doc);
                    break;
                default:
                    System.out.println("[PROCESSOR] Unknown message format: " + messageType);
            }

        } catch (Exception e) {
            System.err.println("[PROCESSOR] Error processing message: " + e.getMessage());
            System.err.println("[PROCESSOR] XML: " + xmlMessage);
            e.printStackTrace();
        }
    }

    /**
     * Parses XML message string into Document
     */
    private Document parseXmlMessage(String xmlMessage) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlMessage)));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            System.err.println("[PROCESSOR] Error parsing XML: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handles response messages
     */
    private void handleResponse(String responseType, Document doc) {
        System.out.println("[PROCESSOR] Handling response type: " + responseType);

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
                System.out.println("[PROCESSOR] Unknown response type: " + responseType);
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
                System.out.println("[PROCESSOR] Unknown message type: " + messageType);
        }
    }

    /**
     * Handles login response
     */
    private void handleLoginResponse(Document doc) {
        try {
            boolean success = Boolean.parseBoolean(getElementValue(doc, "success"));

            System.out.println("[PROCESSOR] Login response - success: " + success);

            if (success) {
                String sessionId = getElementValue(doc, "sessionId");
                String adminName = getElementValue(doc, "userName");
                boolean isAdmin = Boolean.parseBoolean(getElementValue(doc, "isAdmin"));

                System.out.println("[PROCESSOR] Session ID: " + sessionId + ", Admin Name: " + adminName + ", Is Admin: " + isAdmin);

                model.handleLoginResult(true, "Login successful", sessionId, adminName, isAdmin);
            } else {
                String errorMsg = getElementValue(doc, "message");
                if (errorMsg.isEmpty()) {
                    errorMsg = "Invalid credentials";
                }
                model.handleLoginResult(false, errorMsg, null, null, false);
            }
        } catch (Exception e) {
            System.err.println("[PROCESSOR] Error handling login response: " + e.getMessage());
            model.handleLoginResult(false, "Error processing login response", null, null, false);
        }
    }

    /**
     * Handles users list response
     */
    private void handleUsersResponse(Document doc) {
        List<Map<String, String>> users = new ArrayList<>();
        NodeList userNodes = doc.getElementsByTagName("user");

        for (int i = 0; i < userNodes.getLength(); i++) {
            Element userElement = (Element) userNodes.item(i);
            Map<String, String> user = createUserMap(userElement);
            users.add(user);
        }

        dataCache.setUsersList(users);
        model.handleUsersData(new ArrayList<>(users));
    }

    /**
     * Creates a user map from XML element
     */
    private Map<String, String> createUserMap(Element userElement) {
        Map<String, String> user = new HashMap<>();
        user.put("email", getElementValue(userElement, "email"));
        user.put("name", getElementValue(userElement, "name"));
        user.put("isAdmin", getElementValue(userElement, "isAdmin"));
        user.put("created", getElementValue(userElement, "created"));
        return user;
    }

    /**
     * Handles groups list response
     */
    private void handleGroupsResponse(Document doc) {
        List<Map<String, String>> groups = new ArrayList<>();
        NodeList groupNodes = doc.getElementsByTagName("group");

        for (int i = 0; i < groupNodes.getLength(); i++) {
            Element groupElement = (Element) groupNodes.item(i);
            Map<String, String> group = createGroupMap(groupElement);
            groups.add(group);
        }

        dataCache.setGroupsList(groups);
        model.handleGroupsData(new ArrayList<>(groups));
    }

    /**
     * Creates a group map from XML element
     */
    private Map<String, String> createGroupMap(Element groupElement) {
        Map<String, String> group = new HashMap<>();
        group.put("id", getElementValue(groupElement, "id"));
        group.put("name", getElementValue(groupElement, "name"));
        group.put("creator", getElementValue(groupElement, "creator"));
        group.put("memberCount", getElementValue(groupElement, "memberCount"));
        group.put("created", getElementValue(groupElement, "created"));
        return group;
    }

    /**
     * Handles group members response
     */
    private void handleGroupMembersResponse(Document doc) {
        String groupId = getElementValue(doc, "groupId");
        NodeList memberNodes = doc.getElementsByTagName("member");

        List<String> members = new ArrayList<>();
        for (int i = 0; i < memberNodes.getLength(); i++) {
            members.add(memberNodes.item(i).getTextContent());
        }

        model.handleGroupMembers(groupId, members);
    }

    /**
     * Handles general response
     */
    private void handleGeneralResponse(Document doc) {
        boolean success = Boolean.parseBoolean(getElementValue(doc, "success"));
        String message = getElementValue(doc, "message");
        model.handleOperationResult(success, message);
    }

    /**
     * Handles error messages
     */
    private void handleErrorMessage(Document doc) {
        String error = getElementValue(doc, "error");
        if (error.isEmpty()) {
            error = getElementValue(doc, "message");
        }
        model.handleError(error);
    }

    /**
     * Handles force logout
     */
    private void handleForceLogout(Document doc) {
        String reason = getElementValue(doc, "reason");
        model.handleForceLogout(reason);
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
}