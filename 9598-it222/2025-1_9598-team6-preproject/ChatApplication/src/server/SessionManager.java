package server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Manages user sessions for the chat server
 * Ensures single session per user and handles session validation
 */
public class SessionManager {
    // Maps to store session information
    private Map<String, Session> sessions;              // sessionId -> Session object
    private Map<String, String> userToSession;          // userEmail -> sessionId
    private final SecureRandom random;
    private static final int SESSION_ID_LENGTH = 32;
    private static final long SESSION_TIMEOUT_MINUTES = 480; // 8 hours

    /**
     * Inner class to represent a session
     */
    private static class Session {
        String sessionId;
        String userEmail;
        LocalDateTime createdTime;
        LocalDateTime lastActivityTime;
        String ipAddress;

        Session(String sessionId, String userEmail, String ipAddress) {
            this.sessionId = sessionId;
            this.userEmail = userEmail;
            this.ipAddress = ipAddress;
            this.createdTime = LocalDateTime.now();
            this.lastActivityTime = LocalDateTime.now();
        }

        void updateActivity() {
            this.lastActivityTime = LocalDateTime.now();
        }

        boolean isExpired() {
            long minutesSinceActivity = ChronoUnit.MINUTES.between(lastActivityTime, LocalDateTime.now());
            return minutesSinceActivity > SESSION_TIMEOUT_MINUTES;
        }
    }

    /**
     * Constructor initializes the session storage
     */
    public SessionManager() {
        this.sessions = new ConcurrentHashMap<>();
        this.userToSession = new ConcurrentHashMap<>();
        this.random = new SecureRandom();

        // Start session cleanup thread
        startSessionCleanup();

        System.out.println("[SESSION] Session Manager initialized");
    }

    /**
     * Creates a new session for a user
     * If user already has a session, it will be invalidated first
     * @param userEmail The email of the user
     * @return The new session ID
     */
    public synchronized String createSession(String userEmail) {
        return createSession(userEmail, null);
    }

    /**
     * Creates a new session for a user with IP address tracking
     * @param userEmail The email of the user
     * @param ipAddress The IP address of the client
     * @return The new session ID
     */
    public synchronized String createSession(String userEmail, String ipAddress) {
        // Check if user already has an active session
        if (userToSession.containsKey(userEmail)) {
            String oldSessionId = userToSession.get(userEmail);
            System.out.println("[SESSION] Invalidating existing session for user: " + userEmail);
            invalidateSession(oldSessionId);
        }

        // Generate new session ID
        String sessionId = generateSessionId();

        // Ensure session ID is unique
        while (sessions.containsKey(sessionId)) {
            sessionId = generateSessionId();
        }

        // Create new session
        Session session = new Session(sessionId, userEmail, ipAddress);
        sessions.put(sessionId, session);
        userToSession.put(userEmail, sessionId);

        System.out.println("[SESSION] Created new session for user: " + userEmail +
                " (ID: " + sessionId.substring(0, 8) + "...)");

        return sessionId;
    }

    /**
     * Validates a session ID
     * @param sessionId The session ID to validate
     * @return True if session is valid and not expired, false otherwise
     */
    public boolean isValidSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }

        Session session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }

        // Check if session has expired
        if (session.isExpired()) {
            System.out.println("[SESSION] Session expired for user: " + session.userEmail);
            invalidateSession(sessionId);
            return false;
        }

        // Update last activity time
        session.updateActivity();
        return true;
    }

    /**
     * Invalidates a session
     * @param sessionId The session ID to invalidate
     */
    public synchronized void invalidateSession(String sessionId) {
        if (sessionId == null) {
            return;
        }

        Session session = sessions.remove(sessionId);
        if (session != null) {
            userToSession.remove(session.userEmail);
            System.out.println("[SESSION] Invalidated session for user: " + session.userEmail);
        }
    }

    /**
     * Invalidates all sessions for a specific user
     * @param userEmail The email of the user
     */
    public synchronized void invalidateUserSessions(String userEmail) {
        String sessionId = userToSession.get(userEmail);
        if (sessionId != null) {
            invalidateSession(sessionId);
        }
    }

    /**
     * Gets the user email associated with a session
     * @param sessionId The session ID
     * @return The user email, or null if session doesn't exist
     */
    public String getUserBySession(String sessionId) {
        Session session = sessions.get(sessionId);
        return session != null ? session.userEmail : null;
    }

    /**
     * Gets the session ID for a user
     * @param userEmail The user's email
     * @return The session ID, or null if user has no active session
     */
    public String getSessionByUser(String userEmail) {
        return userToSession.get(userEmail);
    }

    /**
     * Updates the activity timestamp for a session
     * @param sessionId The session ID
     */
    public void updateSessionActivity(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            session.updateActivity();
        }
    }

    /**
     * Gets information about a session
     * @param sessionId The session ID
     * @return Map containing session information, or null if session doesn't exist
     */
    public Map<String, String> getSessionInfo(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }

        Map<String, String> info = new HashMap<>();
        info.put("sessionId", session.sessionId);
        info.put("userEmail", session.userEmail);
        info.put("createdTime", session.createdTime.toString());
        info.put("lastActivityTime", session.lastActivityTime.toString());
        info.put("ipAddress", session.ipAddress != null ? session.ipAddress : "unknown");

        return info;
    }

    /**
     * Gets all active sessions
     * @return List of maps containing session information
     */
    public List<Map<String, String>> getAllSessions() {
        List<Map<String, String>> allSessions = new ArrayList<>();

        for (Session session : sessions.values()) {
            Map<String, String> info = new HashMap<>();
            info.put("sessionId", session.sessionId);
            info.put("userEmail", session.userEmail);
            info.put("createdTime", session.createdTime.toString());
            info.put("lastActivityTime", session.lastActivityTime.toString());
            info.put("ipAddress", session.ipAddress != null ? session.ipAddress : "unknown");
            allSessions.add(info);
        }

        return allSessions;
    }

    /**
     * Gets the number of active sessions
     * @return The count of active sessions
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Checks if a user has an active session
     * @param userEmail The user's email
     * @return True if user has an active session, false otherwise
     */
    public boolean hasActiveSession(String userEmail) {
        String sessionId = userToSession.get(userEmail);
        return sessionId != null && isValidSession(sessionId);
    }

    /**
     * Clears all sessions (use with caution)
     */
    public synchronized void clearAllSessions() {
        int count = sessions.size();
        sessions.clear();
        userToSession.clear();
        System.out.println("[SESSION] Cleared all " + count + " sessions");
    }

    /**
     * Generates a random session ID
     * @return A unique session ID string
     */
    private String generateSessionId() {
        byte[] randomBytes = new byte[SESSION_ID_LENGTH];
        random.nextBytes(randomBytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : randomBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    /**
     * Starts a background thread to clean up expired sessions
     */
    private void startSessionCleanup() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    // Wait 30 minutes between cleanup runs
                    Thread.sleep(30 * 60 * 1000);

                    // Clean up expired sessions
                    int cleanedCount = cleanupExpiredSessions();
                    if (cleanedCount > 0) {
                        System.out.println("[SESSION] Cleaned up " + cleanedCount + " expired sessions");
                    }

                } catch (InterruptedException e) {
                    System.out.println("[SESSION] Cleanup thread interrupted");
                    break;
                }
            }
        });

        cleanupThread.setDaemon(true);
        cleanupThread.setName("SessionCleanupThread");
        cleanupThread.start();

        System.out.println("[SESSION] Session cleanup thread started");
    }

    /**
     * Removes expired sessions
     * @return The number of sessions removed
     */
    private synchronized int cleanupExpiredSessions() {
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            if (entry.getValue().isExpired()) {
                toRemove.add(entry.getKey());
            }
        }

        for (String sessionId : toRemove) {
            invalidateSession(sessionId);
        }

        return toRemove.size();
    }

    /**
     * Gets statistics about sessions
     * @return Map containing session statistics
     */
    public Map<String, Object> getSessionStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalSessions", sessions.size());
        stats.put("uniqueUsers", userToSession.size());

        // Calculate average session age
        if (!sessions.isEmpty()) {
            long totalMinutes = 0;
            LocalDateTime now = LocalDateTime.now();

            for (Session session : sessions.values()) {
                totalMinutes += ChronoUnit.MINUTES.between(session.createdTime, now);
            }

            stats.put("averageSessionAgeMinutes", totalMinutes / sessions.size());
        } else {
            stats.put("averageSessionAgeMinutes", 0);
        }

        // Find oldest and newest sessions
        LocalDateTime oldest = null;
        LocalDateTime newest = null;

        for (Session session : sessions.values()) {
            if (oldest == null || session.createdTime.isBefore(oldest)) {
                oldest = session.createdTime;
            }
            if (newest == null || session.createdTime.isAfter(newest)) {
                newest = session.createdTime;
            }
        }

        if (oldest != null) {
            stats.put("oldestSessionTime", oldest.toString());
        }
        if (newest != null) {
            stats.put("newestSessionTime", newest.toString());
        }

        return stats;
    }

    /**
     * Validates a session and returns the associated user email
     * @param sessionId The session ID to validate
     * @return The user email if session is valid, null otherwise
     */
    public String validateSessionAndGetUser(String sessionId) {
        if (isValidSession(sessionId)) {
            return getUserBySession(sessionId);
        }
        return null;
    }

    /**
     * Extends a session's timeout
     * @param sessionId The session ID
     * @return True if session was extended, false if session doesn't exist
     */
    public boolean extendSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            session.updateActivity();
            System.out.println("[SESSION] Extended session for user: " + session.userEmail);
            return true;
        }
        return false;
    }

    /**
     * Gets the remaining time for a session before it expires
     * @param sessionId The session ID
     * @return Minutes remaining, or -1 if session doesn't exist
     */
    public long getSessionRemainingMinutes(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return -1;
        }

        long minutesSinceActivity = ChronoUnit.MINUTES.between(session.lastActivityTime, LocalDateTime.now());
        return SESSION_TIMEOUT_MINUTES - minutesSinceActivity;
    }
}