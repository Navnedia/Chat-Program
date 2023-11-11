/**
 * The FileTransferRequest class defines a serializable object to transmit the details
 * necessary to request a file transfer. The serialization means it can be transmitted
 * over the socket connection.
 *
 * @see Request
 * @author Aiden Vandekerckhove
 */
public class FileTransferRequest extends Request implements java.io.Serializable {

    /** The username of the chat client that has the requested file. */
    public final String fileOwner;
    /** The name of the file we would like to request. */
    public final String filename;

    public FileTransferRequest(String fileOwner, String filename) {
        super(RequestType.FILE_REQUEST);
        this.fileOwner = fileOwner;
        this.filename = filename;
    }
}
