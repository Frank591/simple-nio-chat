package ru.fsl.transport.tcp;

public class TcpMessage {

    public static final int MAX_MESSAGE_SIZE_IN_BYTES = 99993;
    public static final int PREFIX_SIZE_IN_BYTES = 6;
    public static final String MESSAGE_START_CHARACTER = "&";
    public static final String ESCAPED_MESSAGE_START_CHARACTER = "&amp;";

    private static final String PREFIX_TEMPLATE = MESSAGE_START_CHARACTER + "%0" + (PREFIX_SIZE_IN_BYTES - 1) + "d";
    private final String payload;
    private byte[] messageBytes;


    public TcpMessage(String payload) {
        this.payload = payload;
        byte[] payloadBytes = escapePayload(payload).getBytes();
        if (payloadBytes.length > MAX_MESSAGE_SIZE_IN_BYTES) {
            throw new IllegalArgumentException(String.format("Message bytes count should be less than %s, but it was %s.",
                    MAX_MESSAGE_SIZE_IN_BYTES,
                    payloadBytes.length));
        }
        byte[] prefixBytes = getMessagePrefix(payloadBytes.length);
        messageBytes = TcpUtils.concatByteArrays(prefixBytes, payloadBytes);
    }

    public byte[] getBytes() {
        return messageBytes;
    }

    public String getPayload() {
        return payload;
    }

    static byte[] getMessagePrefix(int payloadBytesCount) {
        if (payloadBytesCount <= 0 || payloadBytesCount > MAX_MESSAGE_SIZE_IN_BYTES) {
            throw new IllegalArgumentException("Payload bytes count should be greater than 0.");
        }
        String formatted = String.format(PREFIX_TEMPLATE, payloadBytesCount);
        return formatted.getBytes();
    }

    static String escapePayload(String payload) {
        return payload.replace(MESSAGE_START_CHARACTER, ESCAPED_MESSAGE_START_CHARACTER);
    }

    static String unescapePayload(String payload) {
        return payload.replace(ESCAPED_MESSAGE_START_CHARACTER, MESSAGE_START_CHARACTER);
    }


    public static int readMessageSize(byte[] prefixBytes) {
        if (prefixBytes.length != PREFIX_SIZE_IN_BYTES) {
            throw new IllegalArgumentException(String.format("Expected byte array size=%s, actual=%s.", PREFIX_SIZE_IN_BYTES, prefixBytes.length));
        }
        String prefixAsStr = new String(prefixBytes);
        char firstChar =prefixAsStr.charAt(0);
        if (firstChar != TcpMessage.MESSAGE_START_CHARACTER.charAt(0)) {
            throw new IllegalStateException(String.format("Prefix '%s' is invalid.", firstChar));
        }
        return Integer.parseInt(prefixAsStr.substring(1, PREFIX_SIZE_IN_BYTES));
    }


}
