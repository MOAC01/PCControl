package per.uyscuti.thread;

/**
 * Created by AlphaGo on 2017/12/23.
 */

public class SocketConnObj {

    private String host;
    private int port;

    public SocketConnObj(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
