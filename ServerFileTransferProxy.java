import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * A ServerFileTransferProxy handles file transfer requests by contacting the file owner over a {@link Socket}
 * connection then forwarding the response data back to the requesting client. The ServerFileTransferProxy also
 * implements the {@link Runnable} interface which allows it to be wrapped with a {@link Thread} object
 * to execute on a separate thread. Having each transfer running on a separate thread lets the chat
 * server support and manage many file transfer requests simultaneously.
 *
 * @see Runnable
 * @author Aiden Vandekerckhove
 */
public class ServerFileTransferProxy implements Runnable {

    /** The active socket of the requesting client. */
    private final Socket clientSocket;
    /** The name of the requested file. */
    private final String filename;

    /** The remote IP address of the client that owns the file. */
    private final InetAddress ownerAddress;
    /** The port number of the owner client's socket for file transfer. */
    private final int ownerListenPort;

    /**
     *  Creates a ServerFileTransferProxy that contacts the owner to request the file
     *  then relays the response data back to the requesting client over the socket connections.
     *
     * @param clientSocket the active socket of the requesting client.
     * @param filename the name of the requested file.
     * @param ownerAddress the remote IP address of the client that owns the file.
     * @param ownerListenPort the port number of the owner client's socket for file transfer.
     */
    public ServerFileTransferProxy(Socket clientSocket, String filename, InetAddress ownerAddress, int ownerListenPort) {
        this.clientSocket = clientSocket;
        this.filename = filename;
        this.ownerAddress = ownerAddress;
        this.ownerListenPort = ownerListenPort;
    }

    /**
     * Execute ServerFileTransferProxy functionality to proxy the file request between the requester to the owner.
     */
    @Override
    public void run() {
        Socket ownerSocket;
        try {
            ownerSocket = new Socket(ownerAddress, ownerListenPort); // Connect to the owners file handler socket.
            // File owner input & output streams to send and receive data from the socket connection.
            DataOutputStream ownerSocketOut = new DataOutputStream(ownerSocket.getOutputStream());
            DataInputStream ownerSocketIn = new DataInputStream(ownerSocket.getInputStream());
            ownerSocketOut.writeUTF(filename); // Send requested file name.

            // Open requesting client output stream and forward the file size:
            DataOutputStream clientSocketOut = new DataOutputStream(clientSocket.getOutputStream());
            long fileSize = ownerSocketIn.readLong();
            clientSocketOut.writeLong(fileSize);

            // Read in file data from the owner socket and forward directly over the requesting client socket:
            int bytesReceived;
//            int total = 0; //! DEBUG
            byte[] dataBuffer = new byte[1500];
            while ((bytesReceived = ownerSocketIn.read(dataBuffer)) != -1) {
//                total += bytesReceived; //! DEBUG
                clientSocketOut.write(dataBuffer, 0, bytesReceived);
            }
//            System.out.println("Done! Total bytes sent: " + total); //! DEBUG

            clientSocket.shutdownOutput();
            clientSocket.close();
        } catch (IOException e) {
            // Always properly close socket and remove dead connections from the list before exiting the thread:
            try {
                clientSocket.shutdownOutput();
                clientSocket.close();
            } catch (IOException ignored) { }
        } catch (Exception e) {
            // Always properly close socket and remove dead connections from the list before exiting the thread:
            try {
                clientSocket.shutdownOutput();
                clientSocket.close();
            } catch (IOException ignored) { }
            System.out.println(e.getMessage());
        }
    }
}
