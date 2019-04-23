package ru.fsl.transport.tcp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.UUID;

public class ClientConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

    private static final Logger LOG = LogManager.getLogger(ClientConnectionHandler.class);
    private final AsynchronousServerSocketChannel listener;
    private final TcpSessionsManager tcpSessionManager;
    private MessageProcessor messageProcessor;

    public ClientConnectionHandler(@NotNull AsynchronousServerSocketChannel listener,
                                   @NotNull TcpSessionsManager tcpSessionManager,
                                   @NotNull MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
        this.listener = listener;
        this.tcpSessionManager = tcpSessionManager;
    }

    @Override
    public void completed(@NotNull AsynchronousSocketChannel clientConnection, @Nullable Object attachment) {
        try {
            if (listener.isOpen()) {
                listener.accept(null, this);
            }
            if (!clientConnection.isOpen()) {
                LOG.warn(String.format("Connection %s is not open. Skip connection handling.", clientConnection));
                return;
            }
            if (!tcpSessionManager.canCreateTcpSession()) {
                LOG.info(String.format("Too many active sessions. Connection %s refused.", clientConnection));
                try {
                    clientConnection.close();
                } catch (IOException closeE) {
                    LOG.error(String.format("Error while closing refused connection %s", clientConnection), closeE);
                }
                return;
            }
            TcpSession userSession = tcpSessionManager.createTcpSession(clientConnection);
            LOG.info(String.format("Session with id=%s created.", userSession.getId()));
            ByteBuffer buffer = ByteBuffer.allocate(TcpMessage.PREFIX_SIZE_IN_BYTES);
            UUID correlationId = UUID.randomUUID();
            LOG.info(String.format("CorrId=%s created for authorization with id=%s.", correlationId, userSession.getId()));
            clientConnection.read(buffer,
                    correlationId,
                    new MessagePrefixReadHandler(tcpSessionManager,
                            userSession,
                            buffer,
                            messageProcessor));
        } catch (Exception e) {
            LOG.error("Unexpected error while accept user connection. ", e);
            try {
                if (clientConnection.isOpen()) {
                    clientConnection.close();
                }
            } catch (IOException closeE) {
                LOG.error(String.format("Error while closing refused connection %s", clientConnection), closeE);
            }
            throw e;
        }
    }

    @Override
    public void failed(Throwable e, Object attachment) {
        if (e instanceof AsynchronousCloseException) {
            LOG.info("Connection was asynchronously closed.");
        } else {
            LOG.error("Can't accept connection.", e);
        }
        if (listener.isOpen()) {
            listener.accept(null, this);
        } else {
            LOG.info("Connection was closed. Listening stopped.");
        }
    }
}
