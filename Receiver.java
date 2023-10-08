import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A Receiver lets the application wait for incoming data from a {@link Socket} connection.
 * Receiver also implements the {@link Runnable} interface which means that, if desired, the
 * Receiver can be wrapped with a {@link Thread} object to execute on a separate thread.
 *
 * @see Runnable
 * @author Aiden Vandekerckhove
 */
public class Receiver implements Runnable {

    private final Socket clientSocket;

    /**
     * Creates a Receiver that uses the supplied Socket to wait/receive
     * messages and print them to the stdout.
     *
     * @param clientSocket the active {@link Socket} connection object.
     */
    public Receiver(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Execute Receiver functionality to wait and read incoming messages from the socket
     * connection, then print out these messages to the stdout. Exit the process when the
     * other side of the socket connection is closed.
     */
    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            // Input stream to read in data from the socket connection.
            DataInputStream socketIn = new DataInputStream(clientSocket.getInputStream());

            while (true) { // Read in loop waiting to receive data:
                String message = socketIn.readUTF();
                System.out.println(message);
            }
        } catch (IOException e) {
            System.exit(0); // Other side of the socket shutdown. Time to exit.
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
