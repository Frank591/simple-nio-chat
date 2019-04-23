package ru.fsl.chat.server.services;

import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.dto.StatisticRequest;
import ru.fsl.chat.contracts.dto.StatisticResponse;

public interface StatisticsService {
    @NotNull StatisticResponse getStatistic(@NotNull StatisticRequest request);
}
