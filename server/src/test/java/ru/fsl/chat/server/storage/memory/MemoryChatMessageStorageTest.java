package ru.fsl.chat.server.storage.memory;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.fsl.chat.server.storage.ChatMessage;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MemoryChatMessageStorageTest {
    private final MemoryChatMessageStorage storage = new MemoryChatMessageStorage(10);

    @BeforeEach
    void beforeEach() {
        storage.clear();
    }

    @Test
    void AddMessageTest() {
        ChatMessage msg = new ChatMessage(UUID.randomUUID(), "test", "test");
        storage.add(msg);
        List<ChatMessage> newMessages = storage.getNewMessages(0);
        assertNotNull(newMessages);
        assertEquals(1, newMessages.size());
        ChatMessage msgFromStorage = newMessages.get(0);
        assertEquals(msg.getId(), msgFromStorage.getId());
        assertEquals(msg.getAuthorName(), msgFromStorage.getAuthorName());
        assertEquals(msg.getText(), msgFromStorage.getText());
        assertNotNull(msgFromStorage.getServerIndex());
    }

    @Test
    void PushOutMessageTest() throws InterruptedException {
        final int maxMsgCount = 10;
        MemoryChatMessageStorage localStorage = new MemoryChatMessageStorage(maxMsgCount);

        ChatMessage msgForPushOut = new ChatMessage(UUID.randomUUID(), "MessageForPushOut", Thread.currentThread().getName());
        localStorage.add(msgForPushOut);

        Thread thread = new Thread(() -> {
            for (int i = 0; i < maxMsgCount / 2; i++) {
                ChatMessage msgFromThread = new ChatMessage(UUID.randomUUID(),
                        String.format("message №%s from thread %s", i, Thread.currentThread().getName()),
                        Thread.currentThread().getName());

                localStorage.add(msgFromThread);
            }
        });
        thread.start();

        for (int i = 0; i < maxMsgCount / 2; i++) {
            ChatMessage msgFromMainThread = new ChatMessage(UUID.randomUUID(),
                    String.format("message №%s from thread %s", i, Thread.currentThread().getName()),
                    Thread.currentThread().getName());

            localStorage.add(msgFromMainThread);
        }
        thread.join();

        List<ChatMessage> newMessages = localStorage.getNewMessages(0);
        assertEquals(maxMsgCount, newMessages.size());
        for(ChatMessage chatMessage: newMessages){
            if (chatMessage.getText().equals(msgForPushOut.getText())){
                throw new IllegalArgumentException(String.format("Message with text=%s should not exists.", msgForPushOut.getText()));
            }
        }

    }


}
