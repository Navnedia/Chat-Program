import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple chat client application for connecting to a {@link ChatServer}
 * over {@link Socket} connections. The chat client supports two-way communication
 * and file transfer using multi-threading to both send and receive at the same time.
 * <br><br>
 * <strong>Program Usage:</strong>
 * <ol>
 *     <li>Compile the code using:
 *          <strong>{@code javac ChatClient.java}</strong>.
 *     </li>
 *     <li>Run the client by first supplying a listening port to send and receive files on.
 *          Then specify the desired port and host of the chat server you wish to connect to:<br>
 *          <strong>{@code java MessengerWithFiles -l <listening port number> -p <connect server port>
 *              [-s] [connect server address]}</strong>.
 *     </li>
 *     <li>Enter a client username when prompted.</li>
 *     <li>Now you can start chatting and transferring files.</li>
 * </ol>
 *
 * @author Aiden Vandekerckhove
 */
public class ChatClient {

    /** Local port to listen for incoming connections. */
    private static int listenPort = -1;
    /** Remote chat server port to connect to. */
    private static int serverPort = -1;
    /** Remote server host address to connect to. */
    private static String serverHost = "localhost";

    public static void main(String[] args) {
        parseArguments(args); // Parse command line arguments to extract run configuration.

        /*
         *  Create socket connection to the chat server and send over connection details with the
         *  local file listen server port. Then launch separate threads for sending messages, receiving
         *  messages, and handling file transfer request, so we can do operations simultaneously.
         *
         * Incoming file request will be sent to the separate local server socket. Outgoing file
         * requests will be sent as a new connection to the same chat server port using the request
         * objects to distinguish types of requests.
        */
        try {
//            System.out.println("Connecting to the chat server..."); //! DEBUG
            Socket clientSocket = new Socket(serverHost, serverPort); // Create socket and connect to the chat server on the specified host/port.
            ServerSocket serverSocket = new ServerSocket(listenPort); // Create local listen server for file requests.

            BufferedReader stdinBuffer = new BufferedReader(new InputStreamReader(System.in)); // A buffer to read in standard input line-by-line.

            String username = getUsername(stdinBuffer); // Prompt user to select a chat display name.
            if (username == null) { return; }

            // Forward the join request object with the client username and local server port number to the chat server:
//            System.out.println("Sending name and data the to server..."); //! DEBUG
            ObjectOutputStream socketOut = new ObjectOutputStream(clientSocket.getOutputStream());
            socketOut.writeObject(new JoinChatRequest(username, listenPort));

            // Start the workers to send and receive messages and file data with socket connections:
            // The fileRequestHandler runs in a separate thread so the application can handle messages and file requests separately.
            Thread fileRequestHandler = new Thread(new FileRequestHandler(serverSocket));
            fileRequestHandler.start();
            // The sender runs on a separate Thread to allow the application to send and receive at the same time.
            Thread sender = new Thread(new Sender(clientSocket, clientSocket.getInetAddress(), serverPort, stdinBuffer));
            sender.start();
            // The receiver will run on the main thread, so we can call run directly since it isn't wrapped in a Thread.
            Receiver receiver = new Receiver(clientSocket);
            receiver.run();

            serverSocket.close(); // Fail safe.
            clientSocket.close(); // Fail safe.
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Prompt the user to input a username.
     * @return username string from the standard input.
     * @throws IOException if an I/O exception occurs.
     */
    public static String getUsername(BufferedReader stdinBuffer) throws IOException {
        System.out.println("What is your name?"); // Prompt client to give a username.
        return stdinBuffer.readLine();
    }

    /**
     * Parse the command line arguments to extract run configuration arguments. Options can be
     * in any order, but must follow the usage guidelines and include the flag argument value
     * immediately after the flag.
     *
     * @param args an array of the command line arguments.
     */
    public static void parseArguments(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-l": // Listening port (required):
                        listenPort = Integer.parseInt(args[++i]);
                        break;
                    case "-p": // Connect Server port (required for client):
                        serverPort = Integer.parseInt(args[++i]);
                        break;
                    case "-s": // Connect Server address (optional for client):
                        if (args[++i].startsWith("-")) { printUsage(); }
                        serverHost = args[i];
                        break;
                    default: // Error case, if it doesn't match one of the flag the command is invalid syntax.
                        printUsage();
                        break;
                }
            }
            if (listenPort == -1 || serverPort == -1) { printUsage(); } // Ensure the required arguments are supplied.
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            printUsage();
        }
    }

    /** Prints error message to show how to properly use this program and exits. */
    public static void printUsage() {
        System.out.println("\nInvalid or missing arguments!\nUsage:" +
                "\n\tjava ChatClient <port> [host]\n");
        System.exit(0);
    }
}
