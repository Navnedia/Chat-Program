import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * A FileRequester lets the application carry out a transfer request for a file from another
 * client connected to the chat server. Files are transferred over the {@link Socket} connection,
 * then stored locally. FileRequester also implements the {@link Runnable} interface which means
 * that, if desired, the FileRequester can be wrapped with a {@link Thread} object to execute on
 * a separate thread.
 *
 * @see Runnable
 * @see FileRequestHandler
 * @author Aiden Vandekerckhove
 */
public class FileRequester implements Runnable {

    private final InetAddress fileServerAddress;
    private final int fileServerPort;
    private final String fileOwner;
    private final String filename;

    /**
     * Creates a FileRequester that uses the owners name to request a file transfer
     * from another client on the chat server.
     *
     * @param fileServerAddress the remote IP address of the chat server address for file transfer.
     * @param fileServerPort the port number of the remote chat server socket for file transfer.
     * @param fileOwner the username of the remote chat client that owns the file we want to request.
     * @param filename the name of the remote file we want to request.
     */
    public FileRequester(InetAddress fileServerAddress, int fileServerPort, String fileOwner, String filename) {
        this.fileServerAddress = fileServerAddress;
        this.fileServerPort = fileServerPort;
        this.fileOwner = fileOwner;
        this.filename = filename;
    }

    /**
     * Execute FileRequester functionality to carry out a file request and write socket data
     * to a local file. Close the thread when the single file transfer request is complete.
     */
    @Override
    public void run() {
        try { // Make request to server for a specific file.
            Socket fileSocket = new Socket(fileServerAddress, fileServerPort);
            // Input & output streams to send and receive data from the socket connection.
            ObjectOutputStream socketOut = new ObjectOutputStream(fileSocket.getOutputStream());
            DataInputStream socketIn = new DataInputStream(fileSocket.getInputStream());

//            System.out.println("Requesting: " + filename + ", from user: " + fileOwner); //! DEBUG
            socketOut.writeObject(new FileTransferRequest(fileOwner, filename)); // Send file transfer request object to the server.
            long fileSize = socketIn.readLong();
//            System.out.println(fileSize + "bytes expected"); //! DEBUG
            if (fileSize == 0) { return; } // Check file size in not zero.
//            System.out.println("Reading in: " + filename); //! DEBUG

            // Read socket data into buffer and write data to file in pieces:
            int bytesReceived;
            byte[] fileBuffer = new byte[1500];
            FileOutputStream fileOutput = new FileOutputStream(filename);
            while ((bytesReceived = socketIn.read(fileBuffer)) != -1) {
//                System.out.println(bytesReceived + " bytes received"); //! DEBUG
                fileOutput.write(fileBuffer, 0, bytesReceived);
            }
            fileOutput.close();
        } catch (IOException e) { // Other side of the socket may have shutdown.
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
