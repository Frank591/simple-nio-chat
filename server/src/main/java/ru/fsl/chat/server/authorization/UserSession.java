package ru.fsl.chat.server.authorization;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

public class UserSession  {


    private final UUID id;
    private final String userName;
    private final Date authDate;
    private final Date createDate;

    public UserSession(@NotNull UUID id, @NotNull String userName, @NotNull Date authDate, @NotNull Date createDate) {
        this.id = id;
        this.userName = userName;
        this.authDate = authDate;
        this.createDate = createDate;
    }

    @NotNull
    public String getUserName() {
        return userName;
    }

    @NotNull
    public Date getAuthDate() {
        return authDate;
    }

    @NotNull
        public UUID getId() {
        return id;
    }

    @NotNull
    public Date getCreateDate() {
        return createDate;
    }
}
