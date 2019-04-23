package ru.fsl.transport.tcp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.logging.LogUtils;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MessageReadHandler implements CompletionHandler<Integer, Object> {

    private MessagePrefixReadHandler prefixReadHandler;

    private final TcpSessionsManager tcpSessionManager;
    private final TcpSession tcpSession;
    private final ByteBuffer buffer;
    private final MessageProcessor messageProcessor;
    private static final Logger LOG = LogManager.getLogger(MessageReadHandler.class);
    private static final int DEFAULT_WRITE_RESPONSE_TIMEOUT_MS = 1500;
    private final String logPrefix;
    private final UUID correlationId;

    public MessageReadHandler(@NotNull MessagePrefixReadHandler prefixReadHandler,
                              @NotNull TcpSessionsManager tcpSessionManager,
                              @NotNull TcpSession tcpSession,
                              @NotNull ByteBuffer buffer,
                              @NotNull MessageProcessor messageProcessor,
                              @NotNull UUID correlationId) {
        this.prefixReadHandler = prefixReadHandler;
        this.tcpSessionManager = tcpSessionManager;
        this.tcpSession = tcpSession;
        this.buffer = buffer;
        this.messageProcessor = messageProcessor;
        logPrefix = LogUtils.createLogPrefix("Process tcp message payload", correlationId);
        this.correlationId = correlationId;
    }

    @Override
    public void completed(Integer bytesRead, Object attachment) {

        if (tcpSession.isClosed()) {
            LOG.warn(logPrefix + "is closed. Processing stopped.");
            return;
        }
        boolean isEndOfStream = TcpUtils.isEndOfStream(bytesRead);
        UUID sessionId = tcpSession.getId();
        if (isEndOfStream) {
            LOG.warn(logPrefix + "- end of stream detected. Attempt to close authorization.");
            TcpUtils.closeSession(tcpSessionManager, sessionId, correlationId, LOG);
            return;
        }
        String request = new String(buffer.array());
        LOG.debug(String.format(logPrefix + "- message accepted: %s", request));
        buffer.clear();
        //process tcp message payload
        try {
            TcpMessage tcpResponse = messageProcessor.process(request, correlationId, tcpSession.getId());
            LOG.debug(String.format(logPrefix + "- response message generated. Payload is %s", tcpResponse.getPayload()));
            Future<Integer> writeResultFuture = tcpSession.getConnection().write(ByteBuffer.wrap(tcpResponse.getBytes()));
            int writeResult = writeResultFuture.get(DEFAULT_WRITE_RESPONSE_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOG.error(logPrefix + "- can't send response message.", e);
        }
        try {
            //prepare to process next message
            ByteBuffer messagePrefixBuffer = prefixReadHandler.getMessagePrefixBuffer();
            messagePrefixBuffer.clear();
            UUID newCorrelationId = UUID.randomUUID();
            LOG.info(String.format("CorrId=%s created for authorization with id=%s.", newCorrelationId, tcpSession.getId()));
            tcpSession.getConnection().read(messagePrefixBuffer, newCorrelationId, prefixReadHandler);
        } catch (Exception e) {
            LOG.error(logPrefix + " - unexpected error while prepare to process next message. Attempt to close authorization. ", e);
            TcpUtils.closeSession(tcpSessionManager, sessionId, correlationId, LOG);
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        LOG.error(logPrefix + "can't read message. Attempt to close authorization.", exc);
        TcpUtils.closeSession(tcpSessionManager, tcpSession.getId(), correlationId, LOG);
    }
}
