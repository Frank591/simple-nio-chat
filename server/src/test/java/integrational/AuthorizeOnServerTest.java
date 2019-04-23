package integrational;

import org.junit.jupiter.api.Test;
import ru.fsl.transport.tcp.TcpServer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthorizeOnServerTest extends BaseServerTest {


    @Test
    public void testAuthorizeAlreadyAuthorizedUser() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        final TcpServer tcpServer = startServer();

        //authorize testUser from first connection
        try (TestTcpClient client = new TestTcpClient(SERVER_ADDRESS)) {
            client.connect();

            String resp = client.sendMessage("100{\"userName\":\"testUser\"}");
            assertEquals("200", resp);

            //try to authorize testUser again from second connection without closing first connection
            try (TestTcpClient client1 = new TestTcpClient(SERVER_ADDRESS)) {
                client1.connect();

                String resp1 = client1.sendMessage("100{\"userName\":\"testUser\"}");
                assertTrue(resp1.contains("500"), "Response is " + resp);
            }
        }
        tcpServer.stopServer();
    }

}
