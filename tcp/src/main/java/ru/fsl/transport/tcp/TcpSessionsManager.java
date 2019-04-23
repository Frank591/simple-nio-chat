package ru.fsl.transport.tcp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.UUID;

public interface TcpSessionsManager {

    int getSessionsCount();

    TcpSession createTcpSession(AsynchronousSocketChannel userConnection);

    void closeTcpSession(UUID sessionId) throws IOException;

    @Nullable
    TcpSession tryGetTcpSession(UUID sessionId);

    @NotNull
    TcpSession getTcpSession(UUID sessionId) throws TcpSessionNotFoundException;

    boolean canCreateTcpSession();

}
