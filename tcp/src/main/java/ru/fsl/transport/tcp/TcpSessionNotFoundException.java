package ru.fsl.transport.tcp;

import java.util.UUID;

public class TcpSessionNotFoundException extends Exception {

    public TcpSessionNotFoundException(UUID sessionId){
        super(String.format("Tcp authorization with id=%s not exists.", sessionId));
    }
}
