package ru.bmstu.parallel.chat;

import org.zeromq.ZMQ.*;

import static org.zeromq.ZMQ.*;

public class LocalProxy implements Runnable {
    private final int LOCAL_PROXY_PUB_PORT = ServerStarter.GLOBAL_PROXY_XSUB_PORT;

    private final String HOST = ServerStarter.HOST;
    private final String PROTOCOL = ServerStarter.PROTOCOL;

    private Context context;
    private Socket pullSocket;
    private Socket pubSocket;

    public LocalProxy(final int LOCAL_PROXY_PULL_PORT) {
        context = context(1);
        pullSocket = context.socket(PULL);
        pubSocket = context.socket(PUB);

        pullSocket.bind(PROTOCOL + "://" + HOST + ":" + LOCAL_PROXY_PULL_PORT);
        pubSocket.connect(PROTOCOL + "://" + HOST + ":" + LOCAL_PROXY_PUB_PORT);
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            String msg = pullSocket.recvStr();
            pubSocket.send(msg);
        }

        pullSocket.close();
        context.close();
    }
}
