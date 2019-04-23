package ru.fsl.transport.tcp;

import org.jetbrains.annotations.NotNull;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Date;
import java.util.UUID;

public interface TcpSession  {
    boolean isClosed();

    UUID getId();

    Date getCreateDate();

    @NotNull AsynchronousSocketChannel getConnection();
}
