package ru.fsl.chat.contracts.dto;

public class GetMessagesRequest {

    private final long lastAcceptedServerIndex;

    public GetMessagesRequest(long lastAcceptedServerIndex) {
        this.lastAcceptedServerIndex = lastAcceptedServerIndex;
    }

    public long getLastAcceptedServerIndex() {
        return lastAcceptedServerIndex;
    }
}
