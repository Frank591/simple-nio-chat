package ru.fsl.transport.tcp;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.logging.LogUtils;

import java.util.UUID;

public final class TcpUtils {
    private TcpUtils() {
    }

    public static final int BYTES_TO_READ_WHEN_CONNECTION_CLOSED = -1;


    /**
     * If {@code bytesToRead} = {@code -1}, then no bytes could be read because the channel has reached
     * end-of-stream.
     */
    public static boolean isEndOfStream(int bytesToRead) {
        return bytesToRead == BYTES_TO_READ_WHEN_CONNECTION_CLOSED;
    }

    public static byte[] concatByteArrays(@NotNull byte[] first, @NotNull byte[] second) {

        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static byte[] concatByteArraysWithPostfix(@NotNull byte[] first, @NotNull byte[] second, byte postfix) {
        final int POSTFIX_BYTE_LEN = 1;

        byte[] result = new byte[first.length + second.length + POSTFIX_BYTE_LEN];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        result[first.length + second.length] = postfix;
        return result;
    }

    public static void closeSession(TcpSessionsManager manager, UUID sessionId, UUID correlationId, Logger log) {
        String logPrefix = LogUtils.createLogPrefix(String.format("Close authorization with id=%s ", sessionId), correlationId);
        try {
            log.info(logPrefix + "started.");
            manager.closeTcpSession(sessionId);
            log.info(logPrefix + "finished successfully.");
        } catch (Exception e) {
            log.error(logPrefix + "finished with error.", e);
        }
    }

}
