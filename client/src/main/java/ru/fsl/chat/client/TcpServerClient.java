package ru.fsl.chat.client;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.core.*;
import ru.fsl.chat.contracts.dto.*;
import ru.fsl.transport.tcp.TcpMessage;
import ru.fsl.transport.tcp.TcpUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;

public class TcpServerClient implements ServerClient {

    private final int DEFAULT_CONNECTION_TIMEOUT_MS = 1000;
    private final int DEFAULT_WRITE_TIMEOUT_MS = 1000;
    private final int DEFAULT_READ_TIMEOUT_MS = 1000;
    private static final Logger LOG = LogManager.getLogger(TcpServerClient.class);

    private volatile boolean isAuthorized;
    private final InetSocketAddress serverAddress;
    private final Gson gson;
    private final ByteBuffer responsePrefixBuffer;
    private AsynchronousSocketChannel connection;
    private final Object connectionLock = new Object();
    private volatile ServerDisconnectSubscriber serverDisconnectSubscriber;

    public TcpServerClient(@NotNull InetSocketAddress serverAddress, @NotNull Gson gson) {
        this.serverAddress = serverAddress;
        this.gson = gson;
        this.responsePrefixBuffer = ByteBuffer.allocate(TcpMessage.PREFIX_SIZE_IN_BYTES);

    }

    @Override
    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    @Override
    public void authorize(UserAuthorizationRequest request) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        if (isAuthorized()) {
            throw new IllegalStateException("Already authorized");
        }
        CommandResultMessage response = sendMessage(new CommandMessage(Command.AUTHORIZE, gson.toJson(request)));
        if (response.getCommandResult() == CommandResult.OK) {
            isAuthorized = true;
        } else {
            throw new RuntimeException(response.getBody());
        }
    }

    @Override
    public boolean isAuthorized() {
        return isConnected() && isAuthorized;
    }

    @Override
    public GetMessagesResponse getMessages(GetMessagesRequest request) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        CommandMessage command = new CommandMessage(Command.GET_MESSAGES, gson.toJson(request));
        CommandResultMessage commandResult = sendAuthorizedMessage(command);
        if (commandResult.getCommandResult() == CommandResult.OK) {
            GetMessagesResponse response = gson.fromJson(commandResult.getBody(), GetMessagesResponse.class);
            return response;
        }
        throw new RuntimeException(commandResult.getBody());
    }

    @Override
    public PrintMessageResponse printMessage(PrintMessageRequest request) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        CommandMessage command = new CommandMessage(Command.PRINT_MESSAGE, gson.toJson(request));
        CommandResultMessage commandResult = sendAuthorizedMessage(command);
        if (commandResult.getCommandResult() == CommandResult.OK) {
            PrintMessageResponse response = gson.fromJson(commandResult.getBody(), PrintMessageResponse.class);
            return response;
        }
        throw new RuntimeException(commandResult.getBody());
    }

    @Override
    public StatisticResponse getStatistic(StatisticRequest request) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        CommandMessage command = new CommandMessage(Command.SHOW, gson.toJson(request));
        CommandResultMessage commandResult = sendAuthorizedMessage(command);
        if (commandResult.getCommandResult() == CommandResult.OK) {
            StatisticResponse response = gson.fromJson(commandResult.getBody(), StatisticResponse.class);
            return response;
        }
        throw new RuntimeException(commandResult.getBody());
    }

    @Override
    public void connect() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        String logPrefix = String.format("Connect to %s:%s ", serverAddress.getHostName(), serverAddress.getPort());
        try {
            LOG.info(logPrefix + "started.");
            synchronized (connectionLock) {
                if (isConnected()) {
                    return;
                }
                connection = AsynchronousSocketChannel.open();
                Future<Void> future = connection.connect(serverAddress);
                future.get(DEFAULT_CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            }
            LOG.info(logPrefix + "finished successfully.");
        } catch (Exception e) {
            LOG.error(logPrefix + "finished with error.", e);
            throw e;
        }
    }

    @Override
    public void disconnect() throws IOException {
        try {
            LOG.info("Disconnect started.");
            synchronized (connectionLock) {
                try {
                    if (!isConnected()) {
                        return;
                    }
                    connection.close();
                } finally {
                    isAuthorized = false;
                    connection = null;
                    if (serverDisconnectSubscriber!=null){
                        serverDisconnectSubscriber.serverDisconnected();
                    }

                }
            }
            LOG.info("Disconnect finished successfully.");
        } catch (Exception e) {
            LOG.error("Disconnect finished with error.", e);
            throw e;
        }
    }

    @Override
    public void subscribeOnServerDisconnect(@NotNull ServerDisconnectSubscriber subscriber) {
        if (serverDisconnectSubscriber != null) {
            throw new IllegalStateException("Already subscribed");
        }
        this.serverDisconnectSubscriber = subscriber;
    }


    private CommandResultMessage sendAuthorizedMessage(CommandMessage command) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        if (!isAuthorized()) {
            throw new IllegalStateException("Connection to server is not authorized.");
        }
        return sendMessage(command);
    }

    private CommandResultMessage sendMessage(CommandMessage command) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        String logPrfix = String.format("Request with command = %s ", command.getCommand());
        LOG.info(logPrfix + " started.");
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Connection not established.");
            }
            TcpMessage tcpMessage = new TcpMessage(command.toString());
            ByteBuffer buffer = ByteBuffer.wrap(tcpMessage.getBytes());
            Future<Integer> writeResult = connection.write(buffer);
            responsePrefixBuffer.clear();
            ByteBuffer responseBuffer;
            synchronized (connectionLock) {
                writeResult.get(DEFAULT_WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                Integer responsePrefixReadBytes = connection.read(responsePrefixBuffer).get(DEFAULT_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                if (responsePrefixReadBytes == TcpUtils.BYTES_TO_READ_WHEN_CONNECTION_CLOSED) {
                    disconnect();
                    throw new IllegalStateException("Can't read response message prefix: connection was closed");
                }
                if (responsePrefixReadBytes != TcpMessage.PREFIX_SIZE_IN_BYTES) {
                    throw new IllegalStateException("Prefix is corrupted. Can't read response");
                }
                int responseSize = TcpMessage.readMessageSize(responsePrefixBuffer.array());
                responseBuffer = ByteBuffer.allocate(responseSize);
                int responseBodyReadBytes = connection.read(responseBuffer).get(DEFAULT_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (responseBodyReadBytes == TcpUtils.BYTES_TO_READ_WHEN_CONNECTION_CLOSED) {
                    disconnect();
                    throw new IllegalStateException("Can't read response message body: connection was closed");
                }
                if (responseBodyReadBytes != responseSize) {
                    throw new IllegalStateException("Body is corrupted. Can't read response");
                }
            }
            String responseAsStr = new String(responseBuffer.array());
            PrefixedMessage response = PrefixedMessage.parse(responseAsStr);
            CommandResultMessage result = CommandUtils.createCommandResult(response);
            LOG.info(logPrfix + "finished successfully.");
            return result;
        } catch (Exception e) {
            LOG.error(logPrfix + "finished with error.", e);
            throw e;
        }
    }
}
