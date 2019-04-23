package ru.fsl.chat.server.authorization;

public enum AuthorizationResultStatus {
    SUCCESS,
    SESSION_NOT_FOUND,
    SESSION_CLOSED,
    ALREADY_AUTHORIZED
}
