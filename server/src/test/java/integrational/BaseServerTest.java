package integrational;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import ru.fsl.chat.server.infrastructure.AppUncaughtExceptionHandler;

import ru.fsl.chat.server.SessionManager;
import ru.fsl.chat.server.infrastructure.di.SimpleDIResolver;
import ru.fsl.chat.server.storage.memory.MemoryChatMessageStorage;
import ru.fsl.transport.tcp.TcpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public abstract class BaseServerTest {

    protected static final InetSocketAddress SERVER_ADDRESS = new InetSocketAddress("localhost", 8080);

    @BeforeAll
    public static void beforeAllTests() {
        Thread.setDefaultUncaughtExceptionHandler(new AppUncaughtExceptionHandler());
    }

    protected static TcpServer startServer() throws IOException {
        SessionManager sessionManager = new SessionManager(100);
        MemoryChatMessageStorage memoryChatMessageStorage = new MemoryChatMessageStorage(100);

        final TcpServer tcpServer = new TcpServer("testServer",
                SERVER_ADDRESS,
                SimpleDIResolver.createMessageProcessor(sessionManager, sessionManager, memoryChatMessageStorage, new Gson()),
                sessionManager);
        Thread thread = new Thread(() -> tcpServer.run());
        thread.start();
        return tcpServer;
    }
}
