public class JoinChatRequest extends Request implements java.io.Serializable {

    public String username;
    public int clientListenPort;

    public JoinChatRequest(String username, int clientListenPort) {
        super(RequestType.JOIN_CHAT);
        this.username = username;
        this.clientListenPort = clientListenPort;
    }
}
