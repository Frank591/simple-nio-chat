package ru.fsl.chat.contracts.dto;

public class StatisticRequest {
    private final StatisticType type;

    public StatisticRequest(StatisticType type) {
        this.type = type;
    }

    public StatisticType getType() {
        return type;
    }
}
