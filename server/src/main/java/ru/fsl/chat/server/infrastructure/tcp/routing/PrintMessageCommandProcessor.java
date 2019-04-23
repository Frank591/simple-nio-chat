package ru.fsl.chat.server.infrastructure.tcp.routing;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.core.Command;
import ru.fsl.chat.contracts.core.CommandMessage;
import ru.fsl.chat.contracts.core.CommandResult;
import ru.fsl.chat.contracts.core.CommandResultMessage;
import ru.fsl.chat.contracts.dto.PrintMessageRequest;
import ru.fsl.chat.contracts.dto.PrintMessageResponse;
import ru.fsl.chat.server.authorization.UserSession;
import ru.fsl.chat.server.authorization.UserSessionsManager;
import ru.fsl.chat.server.services.ChatMessagesService;

import java.util.UUID;

public class PrintMessageCommandProcessor extends RequiredAuthorizationCommandProcessor {

    private final ChatMessagesService chatMessagesService;


    public PrintMessageCommandProcessor(@NotNull UserSessionsManager userSessionsManager,
                                        @NotNull ChatMessagesService chatMessagesService,
                                        @NotNull Gson gson) {
        super(userSessionsManager, gson);
        this.chatMessagesService = chatMessagesService;
    }

    @Override
    public Command getCommand() {
        return Command.PRINT_MESSAGE;
    }

    @Override
    protected CommandResultMessage processAuthorized(CommandMessage command, UUID corrId, UserSession userSession) {
        checkCommandValid(command.getCommand());
        PrintMessageRequest request;
        try {
            request = gson.fromJson(command.getBody(), PrintMessageRequest.class);
        } catch (JsonSyntaxException e) {
            return new CommandResultMessage(CommandResult.BAD_REQUEST, e.getMessage());
        }
        if (StringUtils.isEmpty(request.getText()) || request.getText().trim() == "") {
            return new CommandResultMessage(CommandResult.BAD_REQUEST, "Chat message can't be empty.");
        }
        if (request.getText().length() > PrintMessageRequest.MAX_MESSAGE_TEXT_LENGTH) {
            return new CommandResultMessage(CommandResult.BAD_REQUEST, "Chat message too long.");
        }
        PrintMessageResponse response = chatMessagesService.registerChatMessage(request, userSession.getUserName(), corrId);
        return new CommandResultMessage(CommandResult.OK, toJson(response));
    }
}
