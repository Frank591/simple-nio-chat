package ru.fsl.chat.contracts.core;

public enum CommandResult {

    OK(200),
    INTERNAL_SERVER_ERROR(500),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    NOT_FOUND(404);

    private final int code;

    CommandResult(final int code) {
        PrefixedMessage.validateCode(code);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CommandResult parse(int code) {
        if (code == OK.getCode()) {
            return OK;
        }
        if (code == INTERNAL_SERVER_ERROR.getCode()) {
            return INTERNAL_SERVER_ERROR;
        }
        if (code == UNAUTHORIZED.getCode()) {
            return UNAUTHORIZED;
        }
        if (code == NOT_FOUND.getCode()) {
            return NOT_FOUND;
        }
        throw new IllegalArgumentException(String.format("Code = %s is unknown.", code));
    }
}
