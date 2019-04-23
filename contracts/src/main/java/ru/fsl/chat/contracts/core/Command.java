package ru.fsl.chat.contracts.core;

public enum Command {
    AUTHORIZE(100),
    GET_MESSAGES(101),
    PRINT_MESSAGE(102),
    SHOW(103);

    private final int code;

    Command(final int code) {
        PrefixedMessage.validateCode(code);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Command parse(int code) {
        if (code == AUTHORIZE.getCode()) {
            return AUTHORIZE;
        }
        if (code == GET_MESSAGES.getCode()) {
            return GET_MESSAGES;
        }
        if (code == PRINT_MESSAGE.getCode()) {
            return PRINT_MESSAGE;
        }
        if (code == SHOW.getCode()) {
            return SHOW;
        }
        throw new IllegalArgumentException(String.format("Code = %s is unknown.", code));
    }
}
