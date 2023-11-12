import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple application to host a chat server that links together multiple clients
 * and allows for two-way communication and file transfer over a {@link Socket} connection. Each
 * client connection is handled and broken out into its own {@link ServerClientHandler} thread
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
    /** A hashmap mapping client usernames the host and port number for their file listen server. */
    public static final HashMap<String, ClientDetails> clientFileServers = new HashMap<>();

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        // Parse port number from command line arguments:
        try {
            if (args.length != 1) { printUsage(); }
            listenPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printUsage();
        }

        // Track active chat client connections, and spin up a thread to handle messages and file requests from the clients:
        // Create server socket, bind to port, and start listening for socket connections.
        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            while (true) { // Wait for new connections repeating forever.
                Socket clientSocket = serverSocket.accept(); // Accept incoming connections and get client socket.

                //! Determine what kind of connection operation this is, file transfer or join chat.
                //! Get the first few metadata messages:
                //!     - For chat connect, get the clients file server port, and username and store these somehow.
                //!     - For file transfer, the owner and file name.
                //! Create the appropriate thread, a file proxy, or client handler.

                //! COULD THIS BE CLEANED UP?
                Request connectionRequest;
                try {
                    // Read in the request data from the socket:
                    ObjectInputStream socketIn = new ObjectInputStream(clientSocket.getInputStream());
                    connectionRequest = (Request) socketIn.readObject();

                    // Parse and handle request types:
                    if (connectionRequest.type == RequestType.JOIN_CHAT) { // Handle bew user join chat request:
                        JoinChatRequest joinRequest = (JoinChatRequest) connectionRequest;
    //                    System.out.println("(Join) name: " + joinRequest.username + ", file port: " + joinRequest.clientListenPort); //! DEBUG

                        // Synchronize and block thread execution to avoid race condition on shared client list.
                        boolean userExists;
                        synchronized (clientFileServers) {
                            userExists = clientFileServers.containsKey(joinRequest.username); // Check if the username is taken.
                        }
                        if (userExists) { // Reject request: username already taken.
                            // Send back a name taken/unavailable message.
                            DataOutputStream socketOut = new DataOutputStream(clientSocket.getOutputStream());
                            socketOut.writeUTF("Username \"" + joinRequest.username
                                    + "\" is already taken! Please choose a different name.");

                            clientSocket.shutdownOutput();
                            clientSocket.close();
                            continue;
                        }

                        // Track connected clients:
                        // Synchronize and block thread execution to avoid race condition on shared client list.
                        synchronized (clientFileServers) {
                            clientFileServers.put(joinRequest.username,
                                    new ClientDetails(clientSocket.getInetAddress(), joinRequest.clientListenPort));
                        }
                        synchronized (connectedClients) {
                            connectedClients.add(clientSocket);
                        }

                        /* Each client handler runs on a separate thread so the server application can handle
                           receiving and forwarding messages for multiple clients at a time. */
                        Thread clientHandler = new Thread(new ServerClientHandler(clientSocket, joinRequest.username));
                        clientHandler.start();
                    } else if (connectionRequest.type == RequestType.FILE_REQUEST) { // Handle file transfer request:
                        FileTransferRequest fileRequest = (FileTransferRequest) connectionRequest;
    //                    System.out.println("(File Request) owner: " + fileRequest.fileOwner + ", file: " + fileRequest.filename); //! DEBUG

                        // Synchronize and block thread execution to avoid race condition on shared client list.
                        ClientDetails owner;
                        synchronized (clientFileServers) {
                            owner = clientFileServers.get(fileRequest.fileOwner); // Get file owner client details.
                        }
                        if (owner == null) { // Reject request: client username doesn't exist.
                            DataOutputStream socketOut = new DataOutputStream(clientSocket.getOutputStream());
                            socketOut.writeLong(0L);

                            clientSocket.shutdownOutput();
                            clientSocket.close();
                            continue;
                        }

                        /* Each transfer request runs on a separate thread so the server application
                            can handle proxying multiple concurrent file requests. */
                        Thread transferProxy = new Thread(new ServerFileTransferProxy(clientSocket,
                                fileRequest.filename, owner.address, owner.listenPort));
                        transferProxy.start();
                    } else {
                        System.out.println("Bad Request Ignored!");
                        clientSocket.close();
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    clientSocket.close();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /** Prints error message to show how to properly use this program and exits. */
    public static void printUsage() {
        System.out.println("\nInvalid or missing arguments!\nUsage:" +
                "\n\tjava ChatServer <port>\n");
        System.exit(0);
    }
}
