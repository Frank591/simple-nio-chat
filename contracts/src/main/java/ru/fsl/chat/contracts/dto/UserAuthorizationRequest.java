package ru.fsl.chat.contracts.dto;

import org.jetbrains.annotations.NotNull;

public class UserAuthorizationRequest {

    public static final int MAX_USER_NAME_LENGTH = 50;

    private final String userName;

    public UserAuthorizationRequest(@NotNull String userName) {
        if (userName.trim() == "") {
            throw new IllegalArgumentException("User name can't be empty.");
        }
        if (userName.length() > MAX_USER_NAME_LENGTH) {
            throw new IllegalArgumentException("User name too long.");
        }
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
