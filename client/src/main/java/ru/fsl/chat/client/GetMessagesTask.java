package ru.fsl.chat.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.dto.ChatMessageDto;
import ru.fsl.chat.contracts.dto.GetMessagesRequest;
import ru.fsl.chat.contracts.dto.GetMessagesResponse;

import java.util.concurrent.atomic.AtomicLong;

public class GetMessagesTask implements Runnable {

    private final static Logger LOG = LogManager.getLogger(GetMessagesTask.class);
    private final ServerClient serverClient;
    private final Printer printer;
    private AtomicLong serverIndex;
    private final static String logPrefix = "Get messages task ";


    public GetMessagesTask(@NotNull ServerClient serverClient,
                           @NotNull Printer printer) {
        this.serverClient = serverClient;
        this.printer = printer;
        this.serverIndex = new AtomicLong(0);
    }

    @Override
    public void run() {
        LOG.debug(logPrefix + " started");
        try {
            if (!serverClient.isAuthorized()) {
                LOG.debug(logPrefix + " skipped, because client is not authorized on server.");
                return;
            }
            long currServerIndex = serverIndex.longValue();
            GetMessagesRequest request = new GetMessagesRequest(currServerIndex);
            GetMessagesResponse response = serverClient.getMessages(request);
            long maxSeverIndex = currServerIndex;
            for (ChatMessageDto chatMessageDto : response.getMessages()) {
                printer.printFromUser(chatMessageDto.getAuthorName(), chatMessageDto.getText());
                if (chatMessageDto.getServerIndex() > maxSeverIndex) {
                    maxSeverIndex = chatMessageDto.getServerIndex();
                }
            }
            serverIndex.set(maxSeverIndex);
            LOG.debug(logPrefix + " finished successfully.");
        } catch (Exception e) {
            LOG.error(logPrefix + "finished with error.");
        }

    }
}
