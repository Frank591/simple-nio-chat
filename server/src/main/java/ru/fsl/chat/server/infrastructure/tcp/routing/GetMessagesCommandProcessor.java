package ru.fsl.chat.server.infrastructure.tcp.routing;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.core.Command;
import ru.fsl.chat.contracts.core.CommandMessage;
import ru.fsl.chat.contracts.core.CommandResult;
import ru.fsl.chat.contracts.core.CommandResultMessage;
import ru.fsl.chat.contracts.dto.GetMessagesRequest;
import ru.fsl.chat.contracts.dto.GetMessagesResponse;
import ru.fsl.chat.server.authorization.UserSession;
import ru.fsl.chat.server.authorization.UserSessionsManager;
import ru.fsl.chat.server.services.ChatMessagesService;

import java.util.UUID;

public class GetMessagesCommandProcessor extends RequiredAuthorizationCommandProcessor {


    private final ChatMessagesService chatMessagesService;

    public GetMessagesCommandProcessor(@NotNull UserSessionsManager userSessionsManager,
                                       @NotNull ChatMessagesService chatMessagesService,
                                       @NotNull Gson gson) {
        super(userSessionsManager,gson);

        this.chatMessagesService = chatMessagesService;
    }

    @Override
    protected CommandResultMessage processAuthorized(@NotNull CommandMessage command, @NotNull UUID correlationId, @NotNull UserSession session) {
        GetMessagesRequest request;
        try {
            request = gson.fromJson(command.getBody(), GetMessagesRequest.class);
        } catch (JsonSyntaxException e) {
            return new CommandResultMessage(CommandResult.BAD_REQUEST, e.getMessage());
        }
        GetMessagesResponse response = chatMessagesService.getMessages(request, correlationId);
        return new CommandResultMessage(CommandResult.OK, toJson(response));
    }

    @Override
    public Command getCommand() {
        return Command.GET_MESSAGES;
    }


}
