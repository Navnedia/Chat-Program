import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * A Sender lets the application accept input from the user with standard input and is
 * then able to send that input over the {@link Socket} connection. Sender also implements
 * the {@link Runnable} interface which means that, if desired, the Sender can be wrapped
 * with a {@link Thread} object to execute on a separate thread.
 *
 * @see Runnable
 * @author Aiden Vandekerckhove
 */
public class Sender implements Runnable {

    private final Socket clientSocket;

    /**
     * Creates a Sender that uses the supplied Socket to send
     * messages it reads in from the stdin.
     *
     * @param clientSocket the active {@link Socket} connection object.
     */
    public Sender(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Execute Sender functionality to start reading from the stdin and forwarding
     * the messages over the socket connection. Close socket and exit process when
     * the stdin is terminated.
     */
    @Override
    public void run() {
        try {
            // Buffer to read in Standard input from user line-by-line.
            BufferedReader stdinBuffer = new BufferedReader(new InputStreamReader(System.in));
            // Output stream for sending data over the socket connection.
            DataOutputStream socketOut = new DataOutputStream(clientSocket.getOutputStream());

            // Read in lines from the standard input and send over socket until the stdin is closed:
            String message;
            while ((message = stdinBuffer.readLine()) != null) {
                socketOut.writeUTF(message);
            }
            // The user closed the standard input, so we close the output stream, the socket, and then exit the program:
            clientSocket.shutdownOutput();
            clientSocket.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
