package ru.fsl.chat.contracts.dto;

import org.jetbrains.annotations.NotNull;

public class GetMessagesResponse {

    @NotNull
    private final ChatMessageDto[] messages;

    public GetMessagesResponse(@NotNull ChatMessageDto[] messages) {
        this.messages = messages;
    }

    public ChatMessageDto[] getMessages() {
        return messages;
    }
}
