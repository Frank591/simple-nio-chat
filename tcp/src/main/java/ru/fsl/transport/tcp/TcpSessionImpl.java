package ru.fsl.transport.tcp;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Date;
import java.util.UUID;

public class TcpSessionImpl implements TcpSession {

    private final UUID id;

    private final Date createDate;

    private final AsynchronousSocketChannel connection;

    private volatile boolean isClosed = false;

    public TcpSessionImpl(UUID id, Date createDate, AsynchronousSocketChannel connection) {
        this.id = id;
        this.createDate = createDate;
        this.connection = connection;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    public void close() throws IOException {
        try {
            if (isClosed) {
                throw new IllegalStateException("Session already closed.");
            }
            if (connection.isOpen()) {
                connection.close();
            }
        } finally {
            isClosed = true;
        }
    }

    @Override
    @NotNull
    public AsynchronousSocketChannel getConnection() {
        if (isClosed) {
            throw new IllegalStateException("Session is closed.");
        }
        return connection;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }
}
