package ru.fsl.transport.tcp;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface MessageProcessor {
    TcpMessage process(String message, @NotNull UUID correlationId, @NotNull UUID sessionId);
}
