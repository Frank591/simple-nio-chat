package ru.fsl.chat.server.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.dto.*;
import ru.fsl.chat.logging.LogUtils;
import ru.fsl.chat.server.storage.ChatMessage;
import ru.fsl.chat.server.storage.ChatMessageStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatMessagesServiceImpl implements ChatMessagesService {

    private final ChatMessageStorage chatMessageStorage;
    private final Logger LOG = LogManager.getLogger(ChatMessagesServiceImpl.class);


    public ChatMessagesServiceImpl(@NotNull ChatMessageStorage chatMessageStorage) {

        this.chatMessageStorage = chatMessageStorage;

    }

    @Override
    @NotNull
    public PrintMessageResponse registerChatMessage(@NotNull PrintMessageRequest request,
                                                    @NotNull String userName,
                                                    @NotNull UUID corrId
    ) {
        final String logPrefix = LogUtils.createLogPrefix("Register chat message", corrId);
        ChatMessage msg = new ChatMessage(UUID.randomUUID(), request.getText(), userName);
        chatMessageStorage.add(msg);
        LOG.debug(logPrefix + " - chat message accepted.");
        return new PrintMessageResponse(msg.getId());
    }

    @Override
    @NotNull
    public GetMessagesResponse getMessages(@NotNull GetMessagesRequest request, @NotNull UUID corrId) {
        Iterable<ChatMessage> newChatMessages = chatMessageStorage.getNewMessages(request.getLastAcceptedServerIndex());
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();

        for (ChatMessage chatMessage : newChatMessages) {
            chatMessageDtos.add(convert(chatMessage));
        }
        final String logPrefix = LogUtils.createLogPrefix("Get new chat messages", corrId);
        LOG.debug(logPrefix, String.format(" - %s new messages found for serverIndex=%s", chatMessageDtos.size(), request.getLastAcceptedServerIndex()));
        return new GetMessagesResponse(chatMessageDtos.toArray(new ChatMessageDto[chatMessageDtos.size()]));
    }

    private static ChatMessageDto convert(ChatMessage chatMessage) {
        return new ChatMessageDto(chatMessage.getId(), chatMessage.getText(), chatMessage.getAuthorName(), chatMessage.getServerIndex());
    }

}
