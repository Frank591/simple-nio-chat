package ru.fsl.chat.server.authorization;

import org.jetbrains.annotations.Nullable;

public class AuthorizationResult {

    private final AuthorizationResultStatus status;
    private final @Nullable UserSession userSession;

    public AuthorizationResult(AuthorizationResultStatus status, @Nullable UserSession userSession) {
        this.status = status;
        this.userSession = userSession;
    }

    public AuthorizationResultStatus getStatus(){
        return status;
    }
}
