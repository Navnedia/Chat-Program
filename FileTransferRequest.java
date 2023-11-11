public class FileTransferRequest extends Request implements java.io.Serializable { //! ALTERNATE NAME: ServerTransferRequest or TransferRequest.

    public String fileOwner;
    public String filename;

    public FileTransferRequest(String fileOwner, String filename) {
        super(RequestType.FILE_REQUEST);
        this.fileOwner = fileOwner;
        this.filename = filename;
    }
}
