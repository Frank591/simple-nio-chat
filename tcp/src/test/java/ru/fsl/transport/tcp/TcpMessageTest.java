package ru.fsl.transport.tcp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TcpMessageTest {


    @Test
    public void getMessagePrefixTest() {
        int payloadBytesCount = 5;
        String prefix = new String(TcpMessage.getMessagePrefix(payloadBytesCount));
        assertEquals("&00005", prefix);

        payloadBytesCount = 51;
        prefix = new String(TcpMessage.getMessagePrefix(payloadBytesCount));
        assertEquals("&00051", prefix);

        payloadBytesCount = 512;
        prefix = new String(TcpMessage.getMessagePrefix(payloadBytesCount));
        assertEquals("&00512", prefix);

        payloadBytesCount = 5123;
        prefix = new String(TcpMessage.getMessagePrefix(payloadBytesCount));
        assertEquals("&05123", prefix);

        payloadBytesCount = 95123;
        prefix = new String(TcpMessage.getMessagePrefix(payloadBytesCount));
        assertEquals("&95123", prefix);
    }

    @Test
    public void getInvalidMessagePrefixTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> TcpMessage.getMessagePrefix(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> TcpMessage.getMessagePrefix(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> TcpMessage.getMessagePrefix(TcpMessage.MAX_MESSAGE_SIZE_IN_BYTES + 1));
    }

    @Test
    public void escapePayloadTest() {
        String payload = "&213&amp;12";
        String escapeResult = TcpMessage.escapePayload(payload);
        assertEquals("&amp;213&amp;amp;12", escapeResult);
        assertEquals(payload, TcpMessage.unescapePayload(escapeResult));
    }

    @Test
    public void escapeClearPayloadTest() {
        String payload = "123141";
        String escapeResult = TcpMessage.escapePayload(payload);
        assertEquals(payload, escapeResult);
        assertEquals(payload, TcpMessage.unescapePayload(escapeResult));
    }

    @Test
    public void readMessageSizeTest() {
        String messagePrefix = "&00001";
        int messageSize = TcpMessage.readMessageSize(messagePrefix.getBytes());
        assertEquals(1, messageSize);

        messagePrefix = "&00019";
        messageSize = TcpMessage.readMessageSize(messagePrefix.getBytes());
        assertEquals(19, messageSize);

        messagePrefix = "&00719";
        messageSize = TcpMessage.readMessageSize(messagePrefix.getBytes());
        assertEquals(719, messageSize);

        messagePrefix = "&03719";
        messageSize = TcpMessage.readMessageSize(messagePrefix.getBytes());
        assertEquals(3719, messageSize);

        messagePrefix = "&93719";
        messageSize = TcpMessage.readMessageSize(messagePrefix.getBytes());
        assertEquals(93719, messageSize);
    }

    @Test
    public void readMessageSizeErrorsTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> TcpMessage.readMessageSize("&123456".getBytes()));
        Assertions.assertThrows(IllegalStateException.class, () -> TcpMessage.readMessageSize("121347".getBytes()));
        Assertions.assertThrows(NumberFormatException.class, () -> TcpMessage.readMessageSize("&2:347".getBytes()));
    }
}
