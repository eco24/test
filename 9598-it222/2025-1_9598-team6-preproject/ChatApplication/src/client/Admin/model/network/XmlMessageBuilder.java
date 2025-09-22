package client.Admin.model.network;

/**
 * Builds XML messages for server communication
 */
public class XmlMessageBuilder {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /**
     * Creates login message
     */
    public String createLoginMessage(String email, String password) {
        return XML_HEADER +
                "<message type=\"login\">" +
                "<email><![CDATA[" + email + "]]></email>" +
                "<password><![CDATA[" + password + "]]></password>" +
                "</message>";
    }

    /**
     * Creates logout message
     */
    public String createLogoutMessage() {
        return XML_HEADER +
                "<message type=\"logout\"/>";
    }

    /**
     * Creates generic request message
     */
    public String createRequestMessage(String requestType) {
        return XML_HEADER +
                "<message type=\"" + requestType + "\"/>";
    }

    /**
     * Creates search users message
     */
    public String createSearchUsersMessage(String searchKey) {
        return XML_HEADER +
                "<message type=\"admin_searchusers\">" +
                "<searchKey><![CDATA[" + searchKey + "]]></searchKey>" +
                "</message>";
    }

    /**
     * Creates add user message
     */
    public String createAddUserMessage(String email, String password, String name, boolean isAdmin) {
        return XML_HEADER +
                "<message type=\"admin_adduser\">" +
                "<email><![CDATA[" + email + "]]></email>" +
                "<password><![CDATA[" + password + "]]></password>" +
                "<name><![CDATA[" + name + "]]></name>" +
                "<isAdmin>" + isAdmin + "</isAdmin>" +
                "</message>";
    }

    /**
     * Creates update user message
     */
    public String createUpdateUserMessage(String email, String newName, String newPassword) {
        StringBuilder xml = new StringBuilder();
        xml.append(XML_HEADER);
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

    /**
     * Creates delete user message
     */
    public String createDeleteUserMessage(String email) {
        return XML_HEADER +
                "<message type=\"admin_deleteuser\">" +
                "<email><![CDATA[" + email + "]]></email>" +
                "</message>";
    }

    /**
     * Creates delete group message
     */
    public String createDeleteGroupMessage(String groupId) {
        return XML_HEADER +
                "<message type=\"admin_deletegroup\">" +
                "<groupId><![CDATA[" + groupId + "]]></groupId>" +
                "</message>";
    }

    /**
     * Creates get group members message
     */
    public String createGetGroupMembersMessage(String groupId) {
        return XML_HEADER +
                "<message type=\"admin_getgroupmembers\">" +
                "<groupId><![CDATA[" + groupId + "]]></groupId>" +
                "</message>";
    }
}