import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * A simple chat client application for connecting to a {@link ChatServer}
 * over a {@link Socket} connection. The chat client supports two-way communication
 * using multi-threading to both send and receive messages at the same time.
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
 *     <li>Now you can start chatting.</li>
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

        // Create socket connection to the server and run threads for simultaneous sending/receiving:
        // start the chat functionality.
        try {
            System.out.println("Waiting for request from server..."); //! DO I NEED THIS?
            Socket clientSocket = new Socket(serverHost, serverPort); // Create socket and connect to the chat server on the specified host/port.

//            ServerSocket serverSocket = new ServerSocket(listenPort); // Create local listen server for file requests.
//            // Forward local server port number to the other side:
//            DataOutputStream socketOut = new DataOutputStream(clientSocket.getOutputStream());
//            socketOut.writeInt(listenPort);

            setUsername(clientSocket); // Get username and sent it to the server.

            // Start the socket connection workers to send and receive data from the socket connection:
            // The sender runs on a separate Thread  to allow the application to send and receive at the same time.
            Thread sender = new Thread(new Sender(clientSocket));
            sender.start();
            // The receiver will run on the main thread, so we can call run directly since it isn't wrapped in a Thread.
            Receiver receiver = new Receiver(clientSocket);
            receiver.run();

            clientSocket.close(); // Fail safe.
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Prompt the user to input a username and send the selected username to the server over the
     * {@link Socket} connection.
     *
     * @param clientSocket the active {@link Socket} connection object.
     * @throws IOException if an I/O exception occurs.
     */
    public static void setUsername(Socket clientSocket) throws IOException  {
        System.out.println("What is your name?"); // Prompt client to give a username.
        // Buffer to read in Standard input from user line-by-line.
        BufferedReader stdinBuffer = new BufferedReader(new InputStreamReader(System.in));
        // Output stream for sending data over the socket connection.
        DataOutputStream socketOut = new DataOutputStream(clientSocket.getOutputStream());

        String message;
        // Read in username from standard input and forward to the server:
        if ((message = stdinBuffer.readLine()) != null) {
            socketOut.writeUTF(message);
            System.out.println("Sending name to server...");
        }
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

    public static void printUsage() {
        System.out.println("\nInvalid or missing arguments!\nUsage:" +
                "\n\tjava ChatClient <port> [host]\n");
        System.exit(0);
    }
}
