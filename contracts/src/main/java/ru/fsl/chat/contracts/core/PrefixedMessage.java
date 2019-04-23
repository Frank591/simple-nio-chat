package ru.fsl.chat.contracts.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrefixedMessage {

    private int code;
    private String body;
    public final static int MIN_CODE_VALUE = 100;
    public final static int MAX_CODE_VALUE = 999;
    public final static int CODE_PREFIX_LENGTH = 3;

    public PrefixedMessage(int code, @Nullable String body) {
        validateCode(code);
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        if (body == null || body == "") {
            return String.valueOf(code);
        }
        return String.valueOf(code) + body;
    }

    public static void validateCode(int code) {
        if (code < MIN_CODE_VALUE || code > MAX_CODE_VALUE) {
            throw new IllegalArgumentException(String.format("Code value = %s is out of range [%s, %s].",
                    code,
                    MIN_CODE_VALUE,
                    MAX_CODE_VALUE));
        }
    }

    @NotNull
    public static PrefixedMessage parse(@NotNull String source) {
        try {
            if (source.length() < CODE_PREFIX_LENGTH) {
                throw new IllegalArgumentException("Input string does not contains code.");
            }
            String codeAStr = source.substring(0, CODE_PREFIX_LENGTH);
            int code = Integer.parseInt(codeAStr);
            String body = null;
            if (source.length() > CODE_PREFIX_LENGTH) {
                body = source.substring(CODE_PREFIX_LENGTH, source.length());
            }
            return new PrefixedMessage(code, body);
        } catch (Exception e) {
            throw new IllegalArgumentException("Input string can't be parsed", e);
        }
    }
}
