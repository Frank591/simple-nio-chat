package ru.fsl.chat.server.services;

import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.dto.GetMessagesRequest;
import ru.fsl.chat.contracts.dto.GetMessagesResponse;
import ru.fsl.chat.contracts.dto.PrintMessageRequest;
import ru.fsl.chat.contracts.dto.PrintMessageResponse;

import java.util.UUID;

public interface ChatMessagesService {
    @NotNull PrintMessageResponse registerChatMessage(@NotNull PrintMessageRequest request,
                                                      @NotNull String userName,
                                                      @NotNull UUID corrId
    );

    @NotNull GetMessagesResponse getMessages(@NotNull GetMessagesRequest request, @NotNull UUID corrId);
}
