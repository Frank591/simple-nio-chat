package ru.fsl.chat.server.storage;

public interface ChatMessageStorage {

    void add(ChatMessage chatMessage);

    Iterable<ChatMessage> getNewMessages(long serverIndex);
}
