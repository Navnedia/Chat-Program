import java.net.InetAddress;

/** Holds client details to make it easy to reference these values in one place. */
public class ClientDetails {

    public final InetAddress address;
    public final int listenPort;

    public ClientDetails(InetAddress address, int listenPort) {
        this.address = address;
        this.listenPort = listenPort;
    }
}
