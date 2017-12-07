package ru.bmstu.parallel.chat;

import static org.zeromq.ZMQ.*;

public class GlobalProxy implements Runnable {
    private final int GLOBAL_PROXY_XSUB_PORT = ServerStarter.GLOBAL_PROXY_XSUB_PORT;
    private final int GLOBAL_PROXY_XPUB_PORT = ServerStarter.GLOBAL_PROXY_XPUB_PORT;

    private final String HOST = ServerStarter.HOST;
    private final String PROTOCOL = ServerStarter.PROTOCOL;

    @Override
    public void run() {
        Context context = context(1);
        Socket xsub = context.socket(XSUB);
        Socket xpub = context.socket(XPUB);

        xsub.bind(PROTOCOL + "://" + HOST + ":" + GLOBAL_PROXY_XSUB_PORT);
        xpub.bind(PROTOCOL + "://" + HOST + ":" + GLOBAL_PROXY_XPUB_PORT);
        proxy(xsub, xpub, null);

        xsub.close();
        xpub.close();
        context.close();
    }
}
