package integrational;


import org.jetbrains.annotations.NotNull;
import ru.fsl.transport.tcp.TcpMessage;
import ru.fsl.transport.tcp.TcpUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Test tcp client
 */

public class TestTcpClient implements AutoCloseable {

    private AsynchronousSocketChannel client;
    private InetSocketAddress addressForConnect;

    public TestTcpClient(@NotNull InetSocketAddress addressForConnect) {
        this.addressForConnect = addressForConnect;
    }


    public void connect() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        client = AsynchronousSocketChannel.open();
        Future<Void> future = client.connect(addressForConnect);
        future.get(10000, TimeUnit.MILLISECONDS);
    }

    public String sendMessage(String payload) throws ExecutionException, InterruptedException, TimeoutException {
        if (client == null) {
            throw new IllegalStateException("Connection is not established.");
        }

        TcpMessage tcpMessage = new TcpMessage(payload);
        ByteBuffer buffer = ByteBuffer.wrap(tcpMessage.getBytes());
        Future<Integer> writeResult = client.write(buffer);
        writeResult.get(1000,TimeUnit.MILLISECONDS);
        ByteBuffer responsePrefixBuffer = ByteBuffer.allocate(TcpMessage.PREFIX_SIZE_IN_BYTES);
        Integer responsePrefixReadBytes = client.read(responsePrefixBuffer).get(1000, TimeUnit.MILLISECONDS);
        if (responsePrefixReadBytes == TcpUtils.BYTES_TO_READ_WHEN_CONNECTION_CLOSED){
            throw new RuntimeException("Connection was closed");
        }
        if (responsePrefixReadBytes != TcpMessage.PREFIX_SIZE_IN_BYTES){
            throw new IllegalStateException("Prefix is corrupted. Can't read response");
        }
        int responseSize = TcpMessage.readMessageSize(responsePrefixBuffer.array());
        ByteBuffer responseBuffer = ByteBuffer.allocate(responseSize);
        int responseBodyReadBytes = client.read(responseBuffer).get(1000, TimeUnit.MILLISECONDS);
        if (responseBodyReadBytes == TcpUtils.BYTES_TO_READ_WHEN_CONNECTION_CLOSED){
            throw new RuntimeException("Connection was closed");
        }
        if (responseBodyReadBytes != responseSize){
            throw new IllegalStateException("Body is corrupted. Can't read response");
        }
        return new String(responseBuffer.array());
    }


    @Override
    public void close() throws IOException {
        if (client != null && client.isOpen()) {
            client.close();
        }
    }
}
