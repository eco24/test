package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Main server class that handles incoming client connections
 * and manages the overall chat server operations
 */
public class ChatServer {
    private static final int PORT = 9999;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private SessionManager sessionManager;
    private XMLDataManager dataManager;
    private Map<String, ClientHandler> activeClients;
    private boolean isRunning;

    /**
     * Constructor initializes server components
     */
    public ChatServer() {
        threadPool = Executors.newCachedThreadPool();
        sessionManager = new SessionManager();
        dataManager = new XMLDataManager();
        activeClients = new ConcurrentHashMap<>();
        isRunning = false;
    }


    /**
     * Starts the server and begins listening for client connections
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("===========================================");
            System.out.println("Chat Server started successfully!");
            System.out.println("Server listening on port: " + PORT);
            System.out.println("===========================================");

            // Accept client connections in a loop
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[NEW CONNECTION] Client connected from: " +
                            clientSocket.getInetAddress().getHostAddress());

                    // Create new handler for each client
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    threadPool.execute(clientHandler);
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("[ERROR] Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to start server on port " + PORT + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    /**
     * Broadcasts a message to all connected users
     * @param message The message content to broadcast
     * @param sender The email of the sender
     */
    public void broadcastMessage(String message, String sender) {
        String xmlMessage = createBroadcastXML(sender, message);

        // Send to all active clients
        for (Map.Entry<String, ClientHandler> entry : activeClients.entrySet()) {
            ClientHandler client = entry.getValue();
            client.sendMessage(xmlMessage);
            System.out.println("[BROADCAST] Message from " + sender + " sent to " + entry.getKey());
        }

        // Store message for offline users
        dataManager.storeBroadcastMessage(sender, message);
        System.out.println("[BROADCAST] Message stored for offline users");
    }

    /**
     * Sends a private message to a specific user
     * @param recipient The email of the recipient
     * @param sender The email of the sender
     * @param message The message content
     */
    public void sendPrivateMessage(String recipient, String sender, String message) {
        String xmlMessage = createPrivateMessageXML(sender, recipient, message);

        ClientHandler recipientHandler = activeClients.get(recipient);
        if (recipientHandler != null) {
            // Recipient is online, send directly
            recipientHandler.sendMessage(xmlMessage);
            System.out.println("[PRIVATE] Message from " + sender + " sent to " + recipient);
        } else {
            // Recipient is offline, store for later delivery
            dataManager.storeOfflineMessage(recipient, sender, message);
            System.out.println("[PRIVATE] Message from " + sender + " stored for offline user " + recipient);
        }
    }

    /**
     * Sends a group message to all members of a group
     * @param groupId The ID of the group
     * @param sender The email of the sender
     * @param message The message content
     */
    public void sendGroupMessage(String groupId, String sender, String message) {
        List<String> members = dataManager.getGroupMembers(groupId);
        String xmlMessage = createGroupMessageXML(groupId, sender, message);

        for (String member : members) {
            if (!member.equals(sender)) { // Don't send back to sender
                ClientHandler memberHandler = activeClients.get(member);
                if (memberHandler != null) {
                    memberHandler.sendMessage(xmlMessage);
                    System.out.println("[GROUP] Message from " + sender + " sent to " + member + " in group " + groupId);
                } else {
                    // Store for offline member
                    dataManager.storeGroupMessage(groupId, member, sender, message);
                }
            }
        }
    }

    /**
     * Registers a client handler for an authenticated user
     * @param email The email of the user
     * @param handler The client handler for this user
     */
    public synchronized void registerClient(String email, ClientHandler handler) {
        // Check if user is already logged in from another location
        if (activeClients.containsKey(email)) {
            System.out.println("[SESSION] Forcing logout for existing session of " + email);
            ClientHandler oldHandler = activeClients.get(email);
            oldHandler.forceLogout();
        }

        activeClients.put(email, handler);
        System.out.println("[REGISTER] User " + email + " registered. Active users: " + activeClients.size());

        // Notify other users that this user is online
        notifyUserStatus(email, true);
    }

    /**
     * Unregisters a client handler when user disconnects
     * @param email The email of the user
     */
    public synchronized void unregisterClient(String email) {
        if (email != null && activeClients.remove(email) != null) {
            System.out.println("[UNREGISTER] User " + email + " unregistered. Active users: " + activeClients.size());

            // Notify other users that this user is offline
            notifyUserStatus(email, false);
        }
    }

    /**
     * Notifies all users about a user's online/offline status
     * @param email The email of the user whose status changed
     * @param isOnline True if user came online, false if went offline
     */
    private void notifyUserStatus(String email, boolean isOnline) {
        String statusXML = createStatusUpdateXML(email, isOnline);

        for (Map.Entry<String, ClientHandler> entry : activeClients.entrySet()) {
            if (!entry.getKey().equals(email)) {
                entry.getValue().sendMessage(statusXML);
            }
        }
    }
    /**
     * Sends a message to a specific client
     * @param email The email of the recipient
     * @param message The message to send
     * @return true if sent successfully, false if user not online
     */
    public boolean sendMessageToClient(String email, String message) {
        ClientHandler handler = activeClients.get(email);
        if (handler != null) {
            handler.sendMessage(message);
            return true;
        }
        return false;
    }

    /**
     * Gets the list of currently online users
     * @return List of email addresses of online users
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(activeClients.keySet());
    }

    /**
     * Checks if a user is currently online
     * @param email The email to check
     * @return True if user is online, false otherwise
     */
    public boolean isUserOnline(String email) {
        return activeClients.containsKey(email);
    }

    /**
     * Gets the session manager instance
     * @return The session manager
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Gets the XML data manager instance
     * @return The data manager
     */
    public XMLDataManager getDataManager() {
        return dataManager;
    }

    /**
     * Shuts down the server gracefully
     */
    public void shutdown() {
        System.out.println("[SHUTDOWN] Shutting down server...");
        isRunning = false;

        // Disconnect all clients
        for (ClientHandler handler : activeClients.values()) {
            handler.disconnect();
        }

        // Shutdown thread pool
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }

        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[SHUTDOWN] Server stopped");
    }

    // XML Message Creation Helper Methods

    private String createBroadcastXML(String sender, String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"broadcast\">" +
                "<sender>" + sender + "</sender>" +
                "<content>" + escapeXML(message) + "</content>" +
                "<timestamp>" + System.currentTimeMillis() + "</timestamp>" +
                "</message>";
    }

    private String createPrivateMessageXML(String sender, String recipient, String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"private\">" +
                "<sender>" + sender + "</sender>" +
                "<recipient>" + recipient + "</recipient>" +
                "<content>" + escapeXML(message) + "</content>" +
                "<timestamp>" + System.currentTimeMillis() + "</timestamp>" +
                "</message>";
    }

    private String createGroupMessageXML(String groupId, String sender, String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"group\">" +
                "<groupId>" + groupId + "</groupId>" +
                "<sender>" + sender + "</sender>" +
                "<content>" + escapeXML(message) + "</content>" +
                "<timestamp>" + System.currentTimeMillis() + "</timestamp>" +
                "</message>";
    }

    private String createStatusUpdateXML(String email, boolean isOnline) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<message type=\"status\">" +
                "<user>" + email + "</user>" +
                "<status>" + (isOnline ? "online" : "offline") + "</status>" +
                "<timestamp>" + System.currentTimeMillis() + "</timestamp>" +
                "</message>";
    }

    private String escapeXML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Main method to start the server
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        ChatServer server = new ChatServer();

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SHUTDOWN] Shutdown signal received");
            server.shutdown();
        }));

        // Start the server
        server.start();
    }
}