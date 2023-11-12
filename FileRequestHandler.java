import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

//! To handle concurrent file requests, we might want to refactor and break requests into separate threads, the FileRequestListener, and the FileRequestHandler.

/**
 * A FileRequestHandler waits for incoming {@link Socket} connections then handles the file
 * transfer requests by reading in local files and sending data over the socket.
 * FileRequestHandler also implements the {@link Runnable} interface which means that,
 * if desired, the FileRequestHandler can be wrapped with a {@link Thread} object to
 * execute on a separate thread.
 *
 * @see Runnable
 * @author Aiden Vandekerckhove
 */
public class FileRequestHandler implements Runnable {

    private final ServerSocket fileRequestSocket;

    /**
     * Creates a FileRequestHandler that uses the supplied ServerSocket to wait for incoming
     * connections then handles the file transfer requests.
     *
     * @param fileRequestSocket the active {@link ServerSocket} object to listen for new connection requests.
     */
    public FileRequestHandler(ServerSocket fileRequestSocket) {
        this.fileRequestSocket = fileRequestSocket;
    }

    /**
     * Execute FileRequestHandler functionality to wait for incoming connections on the socket, then
     * read in and transfer file data over the socket connection. Close the socket when the single
     * file transfer request is complete.
     */
    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            while (true) { // Loop to process incoming file requests:
                try {
                    Socket fileSocket = fileRequestSocket.accept();
                    // Input & output streams to send and receive data from the socket connection.
                    DataInputStream socketIn = new DataInputStream(fileSocket.getInputStream());
                    DataOutputStream socketOut = new DataOutputStream(fileSocket.getOutputStream());

                    String filename = socketIn.readUTF(); // Get the requested filename.
//                System.out.println("Received request for: " + filename); // DEBUG
                    File file = new File(filename);
                    if (file.exists() && file.canRead()) { // Send back the file size if it exists.
                        long fileSize = file.length();
                        if (fileSize > 0) {
                            socketOut.writeLong(fileSize);
                        } else {
                            socketOut.writeLong(0L);
                            fileSocket.shutdownOutput();
                            fileSocket.close();
                            continue;
                        }
                    } else {
                        socketOut.writeLong(0L);
                        fileSocket.shutdownOutput();
                        fileSocket.close();
                        continue;
                    }
//                System.out.println("Sending: " + filename); // DEBUG

                    // Read file into buffer and send over the socket in pieces:
                    int bytesRead;
                    byte[] fileBuffer = new byte[1500];
                    FileInputStream fileInput = new FileInputStream(file);
                    while ((bytesRead = fileInput.read(fileBuffer)) != -1) {
//                    System.out.println(bytesRead + " bytes read"); // DEBUG
                        socketOut.write(fileBuffer, 0, bytesRead);
                    }
                    fileInput.close();

                    fileSocket.shutdownOutput();
                    fileSocket.close();
                } catch (IOException ignored) { }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
