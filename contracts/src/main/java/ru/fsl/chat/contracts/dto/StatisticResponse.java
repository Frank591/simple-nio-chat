package ru.fsl.chat.contracts.dto;

public class StatisticResponse {
    private final int sessionsCount;

    public StatisticResponse(int sessionsCount) {
        this.sessionsCount = sessionsCount;
    }

    public int getSessionsCount() {
        return sessionsCount;
    }
}
