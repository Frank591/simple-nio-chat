package ru.fsl.chat.logging;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class LogUtils {

    private LogUtils() {

    }

    public static String createLogPrefix(@NotNull String eventName, @NotNull UUID correlationId) {
        return String.format("%s[corrId=%s] ", eventName, correlationId);
    }
}
