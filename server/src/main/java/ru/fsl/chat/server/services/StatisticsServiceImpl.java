package ru.fsl.chat.server.services;

import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.dto.StatisticRequest;
import ru.fsl.chat.contracts.dto.StatisticResponse;
import ru.fsl.chat.contracts.dto.StatisticType;
import ru.fsl.chat.server.authorization.UserSessionsManager;
import ru.fsl.transport.tcp.TcpSessionsManager;

public class StatisticsServiceImpl implements StatisticsService {

    private final UserSessionsManager userSessionsManager;
    private final TcpSessionsManager tcpSessionsManager;

    public StatisticsServiceImpl(@NotNull UserSessionsManager userSessionsManager,
                             @NotNull TcpSessionsManager tcpSessionsManager){

        this.userSessionsManager = userSessionsManager;
        this.tcpSessionsManager = tcpSessionsManager;
    }

    @Override
    public StatisticResponse getStatistic(StatisticRequest request){
        if (request.getType() == StatisticType.SESSIONS){
            return new StatisticResponse(tcpSessionsManager.getSessionsCount());
        }
        else if (request.getType() == StatisticType.AUTHORIZED_SESSIONS){
            return new StatisticResponse(userSessionsManager.getAuthorizedUsersCount());
        }
        throw new IllegalArgumentException(String.format("Statistic %s is unknown", request.getType()));
    }

}
