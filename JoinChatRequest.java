/**
 * The JoinChatRequest class defines a serializable object to transmit the data
 * necessary to join the chat session. The serialization means it can be transmitted
 * over the socket connection.
 *
 * @see Request
 * @author Aiden Vandekerckhove
 */
public class JoinChatRequest extends Request implements java.io.Serializable {

    /** The requested chat username for our client. */
    public final String username;
    /** The port of the active file listen server on the chat client. */
    public final int clientListenPort;

    public JoinChatRequest(String username, int clientListenPort) {
        super(RequestType.JOIN_CHAT);
        this.username = username;
        this.clientListenPort = clientListenPort;
    }
}
