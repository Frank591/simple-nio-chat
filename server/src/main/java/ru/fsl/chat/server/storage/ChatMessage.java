package ru.fsl.chat.server.storage;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChatMessage {

    private static final int MAX_CHAT_MESSAGE_TEXT_LENGTH = 140;
    private static final int MAX_USER_NAME_LENGTH = 50;

    private final UUID id;

    private Long serverIndex;

    private final String text;

    private final String authorName;

    public ChatMessage(UUID id, String text, String authorName) {
        this.id = id;
        if (text.length()> MAX_CHAT_MESSAGE_TEXT_LENGTH){
            throw new IllegalArgumentException("Text too long");
        }
        if (authorName.length()> MAX_USER_NAME_LENGTH){
            throw new IllegalArgumentException("Author name too long");
        }
        this.text = text;
        this.authorName = authorName;
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

    public void initServerIndex(@NotNull Long serverIndex) {
        if (this.serverIndex != null) {
            throw new IllegalStateException("Server index already initialized.");
        }
        this.serverIndex = serverIndex;
    }
}
