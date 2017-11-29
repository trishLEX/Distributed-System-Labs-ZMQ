package ru.bmstu.parallel.chat;

public class ServerStarter {
    private static final int SERVER_1_PORT = 8080;
    private static final int SERVER_1_LOCAL_PROXY_PORT = 8888;

    private static final int SERVER_2_PORT = 8085;
    private static final int SERVER_2_LOCAL_PROXY_PORT = 8893;

    public static final String HOST = "localhost";
    public static final String PROTOCOL = "tcp";

    public static final int GLOBAL_PROXY_XSUB_PORT = 9000;
    public static final int GLOBAL_PROXY_XPUB_PORT = 9090;

    public static void main(final String[] args) {
        GlobalProxy globalProxy = new GlobalProxy();
        Thread threadGlobalProxy = new Thread(globalProxy);
        threadGlobalProxy.start();

        Server server1 = new Server(SERVER_1_PORT, SERVER_1_LOCAL_PROXY_PORT);
        Server server2 = new Server(SERVER_2_PORT, SERVER_2_LOCAL_PROXY_PORT);

        Thread threadServer1 = new Thread(server1);
        Thread threadServer2 = new Thread(server2);

        threadServer1.start();
        threadServer2.start();
    }
}
