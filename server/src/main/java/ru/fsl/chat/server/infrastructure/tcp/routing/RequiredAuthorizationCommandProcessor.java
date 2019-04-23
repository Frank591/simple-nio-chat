package ru.fsl.chat.server.infrastructure.tcp.routing;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.core.CommandMessage;
import ru.fsl.chat.contracts.core.CommandResult;
import ru.fsl.chat.contracts.core.CommandResultMessage;
import ru.fsl.chat.server.authorization.UserSession;
import ru.fsl.chat.server.authorization.UserSessionsManager;


import java.util.UUID;

public abstract class RequiredAuthorizationCommandProcessor extends CommandProcessor {

    protected final UserSessionsManager userSessionsManager;


    public RequiredAuthorizationCommandProcessor(@NotNull UserSessionsManager userSessionsManager,
                                                 @NotNull Gson gson) {
        super(gson);
        this.userSessionsManager = userSessionsManager;
    }


    @Override
    public CommandResultMessage process(CommandMessage command, UUID correlationId, UUID sessionId) {
        checkCommandValid(command.getCommand());
        UserSession userSession = userSessionsManager.tryGet(sessionId);
        if (userSession == null) {
            return new CommandResultMessage(CommandResult.UNAUTHORIZED, null);
        }
        return processAuthorized(command, correlationId, userSession);
    }

    protected abstract CommandResultMessage processAuthorized(@NotNull CommandMessage command,
                                                              @NotNull UUID correlationId,
                                                              @NotNull UserSession session);


}
