import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * A Sender accepts input from the user with standard input to carry out different operations
 * such as (message, file request, and exit). Messages are sent over a socket {@link Socket}
 * connection, and files requests are carried out on a separate thread with the {@link FileRequester}
 * class. Sender also implements the {@link Runnable} interface which means that, if desired,
 * the Sender can be wrapped with a {@link Thread} object to execute on a separate thread.
 *
 * @see Runnable
 * @see FileRequester
 * @author Aiden Vandekerckhove
 */
public class Sender implements Runnable {

    private final Socket clientSocket;
    private final InetAddress fileServerAddress;
    private final int fileServerPort;
    private final BufferedReader stdinBuffer;

    /**
     * Creates a Sender that uses user input from stdin to carry out operations
     * for messages and file transfer over socket connections.
     *
     * @param clientSocket the active {@link Socket} connection object for text messages.
     * @param fileServerAddress the remote IP address of the chat server address for file transfer.
     * @param fileServerPort the port number of the remote chat server socket for file transfer.
     * @param stdinBuffer a standard input buffer reader to get input from the user line-by-line.
     */
    public Sender(Socket clientSocket, InetAddress fileServerAddress, int fileServerPort, BufferedReader stdinBuffer) {
        this.clientSocket = clientSocket;
        this.fileServerAddress = fileServerAddress;
        this.fileServerPort = fileServerPort;
        this.stdinBuffer = stdinBuffer;
    }

    /**
     * Execute Sender functionality to start reading from the stdin and executing operations.
     */
    @Override
    public void run() {
        try {
            // Output stream for sending data over the socket connection.
            DataOutputStream socketOut = new DataOutputStream(clientSocket.getOutputStream());

            String operation, message, filename, fileOwner;
            while (true) { // User input operations loop:
                displayMenuOptions();

                // Parse the desired operation:
                if ((operation = stdinBuffer.readLine()) == null || operation.equalsIgnoreCase("x")) {
                    break; // Exit operation.
                } else if (operation.equalsIgnoreCase("m")) { // message operation:
                    System.out.println("Enter your message:");
                    if ((message = stdinBuffer.readLine()) == null) { break; }

                    socketOut.writeUTF(message);
//                    System.out.println("Sending Message: " + message); //! DEBUG
                } else if (operation.equalsIgnoreCase("f")) { // File transfer operation:
                    System.out.println("Who owns the file?");
                    if ((fileOwner = stdinBuffer.readLine()) == null) { break; }
                    System.out.println("Which file do you want?");
                    if ((filename = stdinBuffer.readLine()) == null) { break; }

                    // Create separate thread to carry out making the file request and writing the file locally:
                    Thread fileRequest = new Thread(new FileRequester(fileServerAddress, fileServerPort, fileOwner, filename));
                    fileRequest.start();
//                    System.out.println("Requesting File {" + fileOwner +"}: " + filename); // DEBUG
                }
            }

            // The user closed the standard input, so we close the output stream, the socket, and then exit the program:
            System.out.println("closing your sockets...goodbye");
            clientSocket.shutdownOutput();
            clientSocket.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /** Prints message to show navigation menu options. */
    public static void displayMenuOptions() {
        System.out.println("Enter an option ('m', 'f', 'x'):\n" +
                           "  (M)essage (send)\n" +
                           "  (F)ile (request)\n" +
                           " e(X)it");
    }
}
