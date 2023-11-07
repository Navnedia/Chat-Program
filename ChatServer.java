import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * A simple application to host a chat server that links together multiple clients
 * and allows for two-way communication over a {@link Socket} connection. Each client
 * connection is handled and broken out into its own {@link ServerClientHandler} thread
 * for processing.
 * <br><br>
 * <strong>Program Usage:</strong>
 * <ol>
 *     <li>Compile the code using:
 *          <strong>{@code javac ChatServer.java}</strong>.
 *     </li>
 *     <li>Run the server on your desired port:
 *          <strong>{@code java ChatServer <port>}</strong>.
 *     </li>
 *     <li>Connect to the server with as many {@link ChatClient}s as you want.</li>
 *     <li>Now you can start chatting.</li>
 * </ol>
 *
 * @author Aiden Vandekerckhove
 */
public class ChatServer {

    /** Local port to listen for incoming connections. */
    private static int listenPort = -1;
    /** A list of all active chat client socket connections. */
    public static final ArrayList<Socket> connectedClients = new ArrayList<>();

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        // Parse port number from command line arguments:
        try {
            if (args.length != 1) { printUsage(); }
            listenPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printUsage();
        }

        // Create server socket, bind to port, and start listening for socket connections.
        // Track active chat client connections, and spin up a thread to handle messages from the client:
        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            while (true) { // Wait for new connections repeating forever.
                Socket clientSocket = serverSocket.accept(); // Accept incoming connections and get client socket.
                synchronized (connectedClients) {
                    // Block thread execution to avoid race condition on shared client list.
                    connectedClients.add(clientSocket);
                }
                /* Each client handler runs on a separate thread so the server application can handle
                   receiving and forwarding messages for multiple clients at a time. */
                Thread clientHandler = new Thread(new ServerClientHandler(clientSocket));
                clientHandler.start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void printUsage() {
        System.out.println("\nInvalid or missing arguments!\nUsage:" +
                "\n\tjava ChatServer [-l] <port>\n");
        System.exit(0);
    }
}
