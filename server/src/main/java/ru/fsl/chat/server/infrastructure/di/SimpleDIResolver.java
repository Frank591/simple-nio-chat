package ru.fsl.chat.server.infrastructure.di;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.core.Command;
import ru.fsl.chat.server.authorization.UserSessionsManager;
import ru.fsl.chat.server.infrastructure.tcp.routing.*;
import ru.fsl.chat.server.storage.ChatMessageStorage;
import ru.fsl.chat.server.storage.memory.MemoryChatMessageStorage;
import ru.fsl.chat.server.services.ChatMessagesService;
import ru.fsl.chat.server.services.ChatMessagesServiceImpl;
import ru.fsl.chat.server.services.StatisticsService;
import ru.fsl.chat.server.services.StatisticsServiceImpl;
import ru.fsl.transport.tcp.MessageProcessor;
import ru.fsl.transport.tcp.TcpSessionsManager;

import java.util.Collections;
import java.util.HashMap;

public class SimpleDIResolver {

    private SimpleDIResolver() {
    }

    private static ChatMessagesService createChatMessageService(@NotNull ChatMessageStorage chatMessageStorage) {
        return new ChatMessagesServiceImpl(chatMessageStorage);
    }

    private static StatisticsService createStatisticsService(@NotNull UserSessionsManager userSessionsManager,@NotNull TcpSessionsManager tcpSessionsManager) {
        return new StatisticsServiceImpl(userSessionsManager, tcpSessionsManager);
    }

    public static ChatMessageStorage createChatMessageStorage() {
        return new MemoryChatMessageStorage(100);
    }

    public static MessageProcessor createMessageProcessor(@NotNull TcpSessionsManager tcpSessionsManager,
                                                          @NotNull UserSessionsManager userSessionsManager,
                                                          @NotNull ChatMessageStorage chatMessageStorage,
                                                          @NotNull Gson gson) {
        HashMap<Command, CommandProcessor> commandProcessors = new HashMap<>();

        AuthorizeCommandProcessor authorizeCommandProcessor = new AuthorizeCommandProcessor(userSessionsManager, gson);
        commandProcessors.put(authorizeCommandProcessor.getCommand(), authorizeCommandProcessor);

        ShowCommandProcessor showCommandProcessor = new ShowCommandProcessor(userSessionsManager, createStatisticsService(userSessionsManager, tcpSessionsManager), gson);
        commandProcessors.put(showCommandProcessor.getCommand(), showCommandProcessor);

        PrintMessageCommandProcessor printMessageCommandProcessor = new PrintMessageCommandProcessor(userSessionsManager, createChatMessageService(chatMessageStorage), gson);
        commandProcessors.put(printMessageCommandProcessor.getCommand(), printMessageCommandProcessor);

        GetMessagesCommandProcessor getMessagesCommandProcessor = new GetMessagesCommandProcessor(userSessionsManager, createChatMessageService(chatMessageStorage), gson);
        commandProcessors.put(getMessagesCommandProcessor.getCommand(), getMessagesCommandProcessor);

        return new CommandsProcessor(Collections.unmodifiableMap(commandProcessors));
    }

}
