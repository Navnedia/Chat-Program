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
 *     <li>Run the client with the port and host of the server:
 *          <strong>{@code java ChatClient <port> [host]}</strong>.
 *     </li>
 *     <li>Enter a client username when prompted.</li>
 *     <li>Now you can start chatting.</li>
 * </ol>
 *
 * @author Aiden Vandekerckhove
 */
public class ChatClient {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 0;

        // Parse command line arguments to extract run configuration:
        try {
            if (args.length != 1 && args.length != 2) { printUsage(); }

            port = Integer.parseInt(args[0]);
            if (args.length == 2) {
                host = args[1];
            }
        } catch (NumberFormatException e) {
            printUsage();
        }

        // Create socket connection to the server and run threads for simultaneous sending/receiving:
        // start the chat functionality.
        try {
            System.out.println("Waiting for request from server...");
            Socket clientSocket = new Socket(host, port); // Create socket and connect to the chat server on the specified host/port.
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

    public static void printUsage() {
        System.out.println("\nInvalid or missing arguments!\nUsage:" +
                "\n\tjava ChatClient <port> [host]\n");
        System.exit(0);
    }
}
