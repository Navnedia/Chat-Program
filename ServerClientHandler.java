import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A ServerClientHandler lets the application wait for incoming messages from a {@link Socket} connection,
 * then forwards messages to other clients connected to the chat server. The ServerClientHandler also
 * implements the {@link Runnable} interface which allows it to be wrapped with a {@link Thread} object
 * to execute on a separate thread. Having each client handler running on a separate thread lets the chat
 * server support and manage many chat users simultaneously.
 *
 * @see Runnable
 * @author Aiden Vandekerckhove
 */
public class ServerClientHandler implements Runnable {

    private final Socket clientSocket;

    /**
     * Creates ServerClientHandler that uses the supplied Socket to
     * receive messages from the user, then forwards the
     * messages to the other clients.
     *
     * @param clientSocket the active {@link Socket} connection object.
     */
    public ServerClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Executes the client handler functionality to wait for incoming messages from the socket
     * connection, and then forward the messages to other connected clients. Exits the thread
     * and removes itself from the connected clients list when the chat client closes the
     * socket connection.
     */
    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            // Input stream to read in data from the socket connection.
            DataInputStream socketIn = new DataInputStream(clientSocket.getInputStream());
            String clientUsername = socketIn.readUTF(); // The first message is set as the username.

            while (true) { // Read in loop waiting to receive messages:
                String message = socketIn.readUTF();
                // Block thread execution to avoid race condition on shared client list.
                synchronized (ChatServer.connectedClients) {
                    // Retransmit the message to each connected client, but NOT back to the original sender:
                    for (Socket recipientSocket : ChatServer.connectedClients) {
                        if (!recipientSocket.equals(clientSocket) && recipientSocket.isConnected()) {
                            try {
                                DataOutputStream recipientOut = new DataOutputStream(recipientSocket.getOutputStream());
                                recipientOut.writeUTF(clientUsername + ": " + message);
                            } catch (Exception e) { // Catch so the client doesn't crash when one recipient fails.
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Client shutdown the socket connection to the chat server. Remove client from list and exit handler thread:
            synchronized (ChatServer.connectedClients) { // Block thread execution to avoid race condition on shared client list.
                ChatServer.connectedClients.remove(clientSocket);
            }
        } catch (Exception e) {
            // Always properly close socket and remove dead connections from the list before exiting the thread:
            try {
                synchronized (ChatServer.connectedClients) { // Block thread execution to avoid race condition on shared client list.
                    ChatServer.connectedClients.remove(clientSocket);
                }
                clientSocket.shutdownOutput();
                clientSocket.close();
            } catch (IOException ignored) { }
            System.out.println(e.getMessage());
        }
    }
}
