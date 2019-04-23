package ru.fsl.chat.server;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.fsl.chat.server.infrastructure.AppUncaughtExceptionHandler;
import ru.fsl.chat.server.infrastructure.di.SimpleDIResolver;
import ru.fsl.chat.server.storage.ChatMessageStorage;
import ru.fsl.transport.tcp.MessageProcessor;
import ru.fsl.transport.tcp.TcpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Application {

    private static final Logger LOG = LogManager.getLogger(Application.class);
    private static final InetSocketAddress SERVER_ADDRESS = new InetSocketAddress("localhost", 8080);

    public static void main(String[] args) {
        LOG.info(String.format("Starting server at %s:%s", SERVER_ADDRESS.getHostName(), SERVER_ADDRESS.getPort()));
        Thread.setDefaultUncaughtExceptionHandler(new AppUncaughtExceptionHandler());
        try {
            SessionManager chatUsersSessionManager = new SessionManager(1000);
            ChatMessageStorage chatMessageStorage = SimpleDIResolver.createChatMessageStorage();
            Gson gson = new Gson();
            MessageProcessor messageProcessor = SimpleDIResolver.createMessageProcessor(chatUsersSessionManager, chatUsersSessionManager, chatMessageStorage, gson);

            final TcpServer tcpServer = new TcpServer("testServer", SERVER_ADDRESS, messageProcessor, chatUsersSessionManager);
            tcpServer.run();
        } catch (Exception e) {
            LOG.fatal("Unexpected error. Server stopped.", e);
        }
    }

}
