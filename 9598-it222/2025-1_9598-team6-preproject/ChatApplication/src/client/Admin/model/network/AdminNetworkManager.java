package client.Admin.model.network;

import java.io.*;
import java.net.*;

/**
 * Manages network connections and message transmission for the admin client
 */
public class AdminNetworkManager {

    // Server connection details
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9999;
    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds

    // Connection components
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected;

    // Message receiver thread
    private Thread messageReceiverThread;
    private boolean receiving;

    // Network listener
    private final NetworkListener listener;

    /**
     * Interface for network event callbacks
     */
    public interface NetworkListener {
        void onConnectionStatusChanged(boolean connected);
        void onMessageReceived(String xmlMessage);
    }

    /**
     * Constructor
     * @param listener The network event listener
     */
    public AdminNetworkManager(NetworkListener listener) {
        this.listener = listener;
        this.connected = false;
    }

    /**
     * Connects to the server
     */
    public boolean connect() {
        try {
            // Close existing connection if any
            if (socket != null && !socket.isClosed()) {
                disconnect();
            }

            System.out.println("[NETWORK] Attempting to connect to " + SERVER_HOST + ":" + SERVER_PORT);

            socket = new Socket();
            socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), CONNECTION_TIMEOUT);
            socket.setSoTimeout(1000); // Set read timeout for graceful shutdown

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            connected = true;
            receiving = true;

            // Start message receiver thread
            startMessageReceiver();

            listener.onConnectionStatusChanged(true);
            System.out.println("[NETWORK] Successfully connected to server");

            return true;

        } catch (IOException e) {
            System.err.println("[NETWORK] Connection failed: " + e.getMessage());
            listener.onConnectionStatusChanged(false);
            return false;
        }
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        receiving = false;
        connected = false;

        // Close resources
        try {
            if (messageReceiverThread != null && messageReceiverThread.isAlive()) {
                messageReceiverThread.interrupt();
            }

            closeResources();

        } catch (Exception e) {
            System.err.println("[NETWORK] Error during disconnect: " + e.getMessage());
        }

        listener.onConnectionStatusChanged(false);
        System.out.println("[NETWORK] Disconnected from server");
    }

    /**
     * Closes all network resources
     */
    private void closeResources() throws IOException {
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
    }

    /**
     * Checks if connected to server
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    /**
     * Sends a message to the server
     */
    public void sendMessage(String message) {
        if (out != null && connected) {
            try {
                System.out.println("[NETWORK] Sending: " + message);
                out.println(message);
                out.flush();
            } catch (Exception e) {
                System.err.println("[NETWORK] Error sending message: " + e.getMessage());
                handleConnectionLost();
            }
        } else {
            System.err.println("[NETWORK] Cannot send message - not connected");
        }
    }

    /**
     * Starts the message receiver thread
     */
    private void startMessageReceiver() {
        messageReceiverThread = new Thread(() -> {
            System.out.println("[NETWORK] Message receiver thread started");

            while (receiving && connected && !Thread.currentThread().isInterrupted()) {
                try {
                    if (in == null) break;

                    String message = in.readLine();
                    if (message != null) {
                        System.out.println("[NETWORK] Received: " + message);
                        listener.onMessageReceived(message);
                    } else {
                        // Connection closed by server
                        System.out.println("[NETWORK] Server closed connection");
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout is okay, continue
                    continue;
                } catch (IOException e) {
                    if (receiving && connected) {
                        System.err.println("[NETWORK] Error receiving message: " + e.getMessage());
                    }
                    break;
                }
            }

            // Connection lost
            if (connected) {
                handleConnectionLost();
            }

            System.out.println("[NETWORK] Message receiver thread terminated");
        });

        messageReceiverThread.setDaemon(true);
        messageReceiverThread.setName("AdminMessageReceiver");
        messageReceiverThread.start();
    }

    /**
     * Handles connection loss
     */
    private void handleConnectionLost() {
        connected = false;
        receiving = false;
        listener.onConnectionStatusChanged(false);
        System.out.println("[NETWORK] Connection lost");
    }
}