package ru.fsl.transport.tcp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.logging.LogUtils;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.UUID;

public class MessagePrefixReadHandler implements CompletionHandler<Integer, UUID> {

    private final static Logger LOG = LogManager.getLogger(MessagePrefixReadHandler.class);

    private final TcpSessionsManager tcpSessionManager;
    private final TcpSession tcpSession;
    private final ByteBuffer prefixBuffer;
    private final MessageProcessor messageProcessor;

    public MessagePrefixReadHandler(@NotNull TcpSessionsManager tcpSessionManager,
                                    @NotNull TcpSession tcpSession,
                                    @NotNull ByteBuffer prefixBuffer,
                                    @NotNull MessageProcessor messageProcessor) {
        this.tcpSessionManager = tcpSessionManager;
        this.tcpSession = tcpSession;
        this.prefixBuffer = prefixBuffer;
        this.messageProcessor = messageProcessor;

    }

    @Override
    public void completed(Integer bytesRead, UUID correlationId) {
        String logPrefix = createLogPrefix(correlationId);
        if (tcpSession.isClosed()) {
            LOG.warn(logPrefix + "is closed. Message processing stopped.");
            return;
        }
        UUID sessionId = tcpSession.getId();
        boolean isEndOfStream = TcpUtils.isEndOfStream(bytesRead);
        if (isEndOfStream) {
            LOG.warn(logPrefix + " - end of stream detected. Attempt to close user authorization.", sessionId);
            TcpUtils.closeSession(tcpSessionManager, sessionId, correlationId, LOG);
            return;
        }
        if (bytesRead < prefixBuffer.limit()) {
            LOG.error(logPrefix + " - unexpected bytes to read count. Attempt to close user authorization.");
            TcpUtils.closeSession(tcpSessionManager, sessionId, correlationId, LOG);
            return;
        }

        int messageSize;
        try {
            messageSize = TcpMessage.readMessageSize(prefixBuffer.array());
            LOG.debug(logPrefix + String.format("- message size=%s", messageSize));
        } catch (Exception e) {
            //TODO implement skip corrupted message behaviour
            LOG.error(String.format(logPrefix + "- message with corrupted prefix=%s received.  Attempt to close user authorization.",
                    new String(prefixBuffer.array())), e);
            TcpUtils.closeSession(tcpSessionManager, tcpSession.getId(), correlationId, LOG);
            return;
        }
        try {
            prefixBuffer.clear();
            ByteBuffer messageBuffer = ByteBuffer.allocate(messageSize);
            tcpSession.getConnection().read(messageBuffer,
                    null,
                    new MessageReadHandler(this, tcpSessionManager, tcpSession, messageBuffer, messageProcessor, correlationId));
        } catch (Exception e) {
            LOG.error(logPrefix + " - unexpected error while prepare to process message body. Attempt to close authorization. ", e);
            TcpUtils.closeSession(tcpSessionManager, sessionId, correlationId, LOG);
        }

    }

    @Override
    public void failed(Throwable exc, UUID correlationId) {
        LOG.error(createLogPrefix(correlationId) + "can't read message. Attempt to close authorization.", exc);
        TcpUtils.closeSession(tcpSessionManager, tcpSession.getId(), correlationId, LOG);
    }

    public ByteBuffer getMessagePrefixBuffer() {
        return prefixBuffer;
    }

    private static String createLogPrefix(UUID correlationId){
        return LogUtils.createLogPrefix("Process tcp message prefix", correlationId);
    }
}
