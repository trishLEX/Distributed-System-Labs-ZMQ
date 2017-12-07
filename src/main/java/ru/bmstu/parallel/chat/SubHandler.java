package ru.bmstu.parallel.chat;

import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.zeromq.ZMQ;

import static org.zeromq.ZMQ.SUB;

public class SubHandler implements Runnable {
    private final int SERVER_SUB_PORT = ServerStarter.GLOBAL_PROXY_XPUB_PORT;

    private final String HOST = ServerStarter.HOST;
    private final String PROTOCOL = ServerStarter.PROTOCOL;

    ZMQ.Context context;
    ZMQ.Socket sub;
    Server server;

    public SubHandler(Server server) {
        this.server = server;
        context = ZMQ.context(1);
        sub = context.socket(SUB);
    }

    @Override
    public void run() {
        sub.connect(PROTOCOL + "://" + HOST + ":" + SERVER_SUB_PORT);
        sub.subscribe("");

        while (!Thread.currentThread().isInterrupted()) {
            String msg = sub.recvStr();

            for (WebSocketChannel session: server.getWebSocketHandler().getPeerConnections()) {
                WebSockets.sendText(msg, session, null);
            }
        }

        context.close();
    }
}
