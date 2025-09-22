package client.Admin.model.data;

/**
 * Manages admin session information
 */
public class AdminSession {

    private String adminEmail;
    private String adminName;
    private String sessionId;
    private boolean authenticated;

    /**
     * Constructor
     */
    public AdminSession() {
        this.authenticated = false;
    }

    /**
     * Sets the admin email during login attempt
     */
    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    /**
     * Sets authentication data after successful login
     */
    public void setAuthenticationData(String sessionId, String adminName, boolean authenticated) {
        this.sessionId = sessionId;
        this.adminName = adminName;
        this.authenticated = authenticated;
    }

    /**
     * Clears all session data
     */
    public void clearSession() {
        this.adminEmail = null;
        this.adminName = null;
        this.sessionId = null;
        this.authenticated = false;
    }

    // ==================== GETTERS ====================

    /**
     * Gets admin email
     */
    public String getAdminEmail() {
        return adminEmail;
    }

    /**
     * Gets admin name
     */
    public String getAdminName() {
        return adminName;
    }

    /**
     * Gets session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Checks if authenticated
     */
    public boolean isAuthenticated() {
        return authenticated;
    }
}