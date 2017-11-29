package ru.bmstu.parallel.chat;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import org.zeromq.ZMQ;

import static org.zeromq.ZMQ.PUSH;

public class Server implements Runnable{
    private final String HOST = ServerStarter.HOST;
    private final String PROTOCOL = ServerStarter.PROTOCOL;

    private Undertow server;
    private LocalProxy localProxy;
    private SubHandler subSocketHandler;
    private WebSocketProtocolHandshakeHandler webSocketHandler;

    public Server(final int SERVER_PORT, final int LOCAL_PROXY_PULL_PORT) {
        this.localProxy = new LocalProxy(LOCAL_PROXY_PULL_PORT);
        Thread threadLocalProxy = new Thread(localProxy);
        threadLocalProxy.start();

        this.subSocketHandler = new SubHandler(this);
        Thread threadSubSocketHandler = new Thread(subSocketHandler);
        threadSubSocketHandler.start();

        ResourceHandler resourceHandler = Handlers.resource(new ClassPathResourceManager(Server.class.getClassLoader(), ""))
                .addWelcomeFiles("index.html");

        ThreadLocal<ZMQ.Socket> pushLocalSocket = new ThreadLocal<>();

        webSocketHandler = Handlers.websocket((exchange, channel) -> {
            channel.getReceiveSetter().set(new AbstractReceiveListener() {

                @Override
                protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                    final String messageData = message.getData();

                    if (pushLocalSocket.get() == null) {
                        ZMQ.Context context = ZMQ.context(1);

                        ZMQ.Socket pusher = context.socket(PUSH);
                        pusher.connect(PROTOCOL + "://" + HOST + ":" + LOCAL_PROXY_PULL_PORT);
                        pusher.send(messageData);

                        pushLocalSocket.set(pusher);
                    }
                    else
                        pushLocalSocket.get().send(messageData);
                }
            });
            channel.resumeReceives();
        });

        server = Undertow.builder()
                .addHttpListener(SERVER_PORT, HOST)
                .setHandler(Handlers.path()
                        .addPrefixPath("/chatsocket", webSocketHandler)
                        .addPrefixPath("/", resourceHandler)
                        .addPrefixPath("/connections", Handlers.path(exchange -> {
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                            exchange.getResponseSender().send("" + webSocketHandler.getPeerConnections().size());
                        }))
                )
                .build();
    }

    public WebSocketProtocolHandshakeHandler getWebSocketHandler() {
        return webSocketHandler;
    }

    @Override
    public void run() {
        server.start();
    }
}
