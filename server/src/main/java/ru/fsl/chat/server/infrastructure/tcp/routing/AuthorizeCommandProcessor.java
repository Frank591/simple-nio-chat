package ru.fsl.chat.server.infrastructure.tcp.routing;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.core.Command;
import ru.fsl.chat.contracts.core.CommandMessage;
import ru.fsl.chat.contracts.core.CommandResult;
import ru.fsl.chat.contracts.core.CommandResultMessage;
import ru.fsl.chat.server.authorization.AuthorizationResult;
import ru.fsl.chat.server.authorization.AuthorizationResultStatus;
import ru.fsl.chat.server.authorization.UserSession;
import ru.fsl.chat.server.authorization.UserSessionsManager;
import ru.fsl.chat.contracts.dto.UserAuthorizationRequest;

import java.util.UUID;

public class AuthorizeCommandProcessor extends CommandProcessor {

    private final UserSessionsManager userSessionManager;

    public AuthorizeCommandProcessor(@NotNull UserSessionsManager userSessionManager, @NotNull Gson gson) {
        super(gson);
        this.userSessionManager = userSessionManager;
    }


    @Override
    public Command getCommand() {
        return Command.AUTHORIZE;
    }

    @Override
    public CommandResultMessage process(@NotNull CommandMessage commandMessage,
                                        @NotNull UUID correlationId,
                                        @NotNull UUID sessionId) {
        checkCommandValid(commandMessage.getCommand());
        UserSession userSession = userSessionManager.tryGet(sessionId);
        if (userSession != null) {
            return new CommandResultMessage(CommandResult.OK, null);
        }
        UserAuthorizationRequest request;
        try {
            request = gson.fromJson(commandMessage.getBody(), UserAuthorizationRequest.class);
        } catch (JsonSyntaxException e) {
            return new CommandResultMessage(CommandResult.BAD_REQUEST, e.getMessage());
        }
        if (StringUtils.isEmpty(request.getUserName()) || request.getUserName().trim() == "") {
            return new CommandResultMessage(CommandResult.BAD_REQUEST, "User name can't be null or empty");
        }
        if (request.getUserName().length() > UserAuthorizationRequest.MAX_USER_NAME_LENGTH) {
            return new CommandResultMessage(CommandResult.BAD_REQUEST,
                    String.format("User name length can't be longer than %s characters", UserAuthorizationRequest.MAX_USER_NAME_LENGTH));
        }

        AuthorizationResult result = userSessionManager.authorize(sessionId, request.getUserName());
        if (result.getStatus() == AuthorizationResultStatus.SUCCESS) {
            return new CommandResultMessage(CommandResult.OK, null);
        } else {
            return new CommandResultMessage(CommandResult.INTERNAL_SERVER_ERROR, getErrorMessage(result.getStatus(), request.getUserName()));
        }
    }

    private static String getErrorMessage(AuthorizationResultStatus status, String userName) {
        if (status == AuthorizationResultStatus.SESSION_CLOSED) {
            return "User authorization is closed";
        }
        if (status == AuthorizationResultStatus.SESSION_NOT_FOUND) {
            return "User authorization does not found. ";
        }
        if (status == AuthorizationResultStatus.ALREADY_AUTHORIZED) {
            return String.format("User with name=%s already has authorized authorization.", userName);
        }
        throw new IllegalArgumentException(String.format("Unknown error status=%s", status));
    }
}
