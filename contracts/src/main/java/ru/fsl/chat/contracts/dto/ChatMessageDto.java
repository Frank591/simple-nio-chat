package ru.fsl.chat.contracts.dto;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChatMessageDto {

    private final UUID id;

    private Long serverIndex;

    private final String text;

    private final String authorName;

    public ChatMessageDto(@NotNull UUID id, @NotNull String text, @NotNull String authorName, long serverIndex) {
        this.id = id;
        this.text = text;
        this.authorName = authorName;
        this.serverIndex = serverIndex;
    }

    public UUID getId() {
        return id;
    }

    public long getServerIndex() {
        return serverIndex;
    }

    public String getText() {
        return text;
    }

    public String getAuthorName() {
        return authorName;
    }


}
