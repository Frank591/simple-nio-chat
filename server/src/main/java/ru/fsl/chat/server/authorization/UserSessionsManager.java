package ru.fsl.chat.server.authorization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface UserSessionsManager {
    AuthorizationResult authorize(UUID sessionId, String userName);

    @Nullable
    UserSession tryGet(UUID sessionId);

    @NotNull
    UserSession get(UUID sessionId) throws UserSessionNotFoundException;


    void logout(UUID sessionId);

    int getAuthorizedUsersCount();
}
