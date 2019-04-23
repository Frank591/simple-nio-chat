package ru.fsl.chat.contracts.dto;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PrintMessageResponse {

    @NotNull private final UUID messageId;

    public PrintMessageResponse(@NotNull UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getMessageId() {
        return messageId;
    }
}
