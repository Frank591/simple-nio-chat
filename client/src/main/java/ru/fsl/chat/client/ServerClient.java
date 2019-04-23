package ru.fsl.chat.client;

import ru.fsl.chat.contracts.dto.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ServerClient {
    boolean isConnected();

    void authorize(UserAuthorizationRequest request) throws InterruptedException, ExecutionException, TimeoutException, IOException;

    boolean isAuthorized();

    GetMessagesResponse getMessages(GetMessagesRequest request) throws InterruptedException, ExecutionException, TimeoutException, IOException;

    PrintMessageResponse printMessage(PrintMessageRequest request) throws InterruptedException, ExecutionException, TimeoutException, IOException;

    StatisticResponse getStatistic(StatisticRequest request) throws InterruptedException, ExecutionException, TimeoutException, IOException;

    void connect() throws IOException, InterruptedException, ExecutionException, TimeoutException;

    void disconnect() throws IOException;

    void subscribeOnServerDisconnect(ServerDisconnectSubscriber subscriber);
}
