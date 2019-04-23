package ru.fsl.chat.server.authorization;

import java.util.UUID;

public class UserSessionNotFoundException  extends Exception {

    public UserSessionNotFoundException(UUID sessionId){
        super(String.format("User authorization with id=%s not exists.", sessionId));
    }
}
