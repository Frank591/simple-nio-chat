package ru.fsl.chat.server.infrastructure.tcp.routing;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.core.Command;
import ru.fsl.chat.contracts.core.CommandMessage;
import ru.fsl.chat.contracts.core.CommandResult;
import ru.fsl.chat.contracts.core.CommandResultMessage;
import ru.fsl.chat.contracts.dto.StatisticRequest;
import ru.fsl.chat.contracts.dto.StatisticResponse;
import ru.fsl.chat.server.authorization.UserSession;
import ru.fsl.chat.server.authorization.UserSessionsManager;
import ru.fsl.chat.server.services.StatisticsService;

import java.util.UUID;

public class ShowCommandProcessor extends RequiredAuthorizationCommandProcessor {


    private final StatisticsService statisticsService;

    public ShowCommandProcessor(@NotNull UserSessionsManager userSessionsManager,
                                @NotNull StatisticsService statisticsService,
                                @NotNull Gson gson) {
        super(userSessionsManager, gson);
        this.statisticsService = statisticsService;
    }

    @Override
    public Command getCommand() {
        return Command.SHOW;
    }

    @Override
    protected CommandResultMessage processAuthorized(@NotNull CommandMessage command,
                                                     @NotNull UUID correlationId,
                                                     @NotNull UserSession userSession) {
        StatisticRequest request;
        try {
            request = gson.fromJson(command.getBody(), StatisticRequest.class);
        } catch (JsonSyntaxException e) {
            return new CommandResultMessage(CommandResult.BAD_REQUEST, e.getMessage());
        }
        StatisticResponse response = statisticsService.getStatistic(request);
        return new CommandResultMessage(CommandResult.OK, toJson(response));
    }
}
