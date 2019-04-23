package ru.fsl.chat.server.storage.memory;

import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.server.storage.ChatMessage;
import ru.fsl.chat.server.storage.ChatMessageStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MemoryChatMessageStorage implements ChatMessageStorage {

    private final int maxMessageCount;
    private int messageCount = 0;
    ConcurrentLinkedQueue<ChatMessage> chatMessages;
    private final Object addLock = new Object();

    public MemoryChatMessageStorage(int maxMessageCount) {
        this.maxMessageCount = maxMessageCount;
        chatMessages = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void add(ChatMessage chatMessage) {
        //lock for put in order chat messages using timestamp
        synchronized (addLock) {
            if (messageCount > maxMessageCount) {
                throw new IllegalStateException(String.format("Unexpected state: message count=%s > max message count=%s.",
                        messageCount,
                        maxMessageCount));
            }
            chatMessage.initServerIndex(System.nanoTime());
            if (messageCount == maxMessageCount) {
                chatMessages.poll();
                messageCount--;
            }
            chatMessages.add(chatMessage);
            messageCount++;
        }
    }

    @Override
    @NotNull
    public List<ChatMessage> getNewMessages(long serverIndex) {
        List<ChatMessage> result = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            if (chatMessage.getServerIndex() <= serverIndex) {
                continue;
            }
            result.add(chatMessage);
        }
        return result;
    }

    void clear() {
        synchronized (addLock) {
            chatMessages.clear();
            messageCount = 0;
        }
    }
}
