public class Request implements java.io.Serializable {
    public enum RequestType {
        JOIN_CHAT, FILE_REQUEST
    }

    public RequestType type;

    public Request(RequestType type) {
        this.type = type;
    }
}
