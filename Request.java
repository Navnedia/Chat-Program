/**
 * The Request class defines a serializable object we can use to transmit different
 * types of requests and relevant data from the chat client to the server using our
 * Socket connection. This helps the server distinguish between different connection
 * Requests. The Request interface is extended by several subclass(s) that define
 * different request types: {@link JoinChatRequest}, and {@link  FileTransferRequest}.
 *
 * @see JoinChatRequest
 * @see FileTransferRequest
 * @author Aiden Vandekerckhove
 */
public class Request implements java.io.Serializable {

    /** Enum defines the type of specific type of request, so we know what to do and what fields are available. */
    public final RequestType type;

    public Request(RequestType type) {
        this.type = type;
    }
}
