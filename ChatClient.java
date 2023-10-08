import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple messenger application for simple two-way communication over a {@link Socket}
 * connection.<br>
 *
 * Usage:<br>
 *      - javac Messenger.java<br>
 *      - java Messenger [-l] <port> [host]
 *
 * @author Aiden Vandekerckhove
 */
public class Messenger {

    public static void main(String[] args) {
        boolean listenServer = false;
        String host = "localhost";
        int port = 0;

        // Parse command line arguments to extract run configuration:
        try {
            if (args.length < 1) {
                printUsage();
            } else if (args[0].equals("-l")) {
                listenServer = true;
                if (args.length >= 2) {
                    port = Integer.parseInt(args[1]);
                    if (args.length == 3) {
                        host = args[2];
                    }
                } else {
                    printUsage();
                }
            } else {
                port = Integer.parseInt(args[0]);
                if (args.length == 2) {
                    host = args[1];
                }
            }
        } catch (NumberFormatException e) {
            printUsage();
        }

        // Start sockets and run messenger functionality:
        try {
            Socket clientSocket;
            if (listenServer) {
                // Create server socket, bind to port, start listening, then get client socket from accepted connection:
                ServerSocket serverSocket = new ServerSocket(port);
                clientSocket = serverSocket.accept();
                serverSocket.close();
            } else { // Client application.
                clientSocket = new Socket(host, port);
            }

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

    public static void printUsage() {
        System.out.println("\nInvalid or missing arguments!\nUsage:" +
                "\n\tjava Messenger [-l] <port> [host]\n");
        System.exit(0);
    }
}
