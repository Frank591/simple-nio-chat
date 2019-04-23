package ru.fsl.chat.server.infrastructure.tcp.routing;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.core.Command;
import ru.fsl.chat.contracts.core.CommandMessage;
import ru.fsl.chat.contracts.core.CommandResultMessage;

import java.util.UUID;

public abstract class CommandProcessor {


    protected final @NotNull Gson gson;

    public CommandProcessor(@NotNull Gson gson){
        this.gson = gson;
    }

    public abstract Command getCommand();


    public abstract CommandResultMessage process(@NotNull CommandMessage command, @NotNull UUID correlationId, @NotNull UUID sessionId);

    protected void checkCommandValid(Command command) {
        if (command != getCommand()) {
            throw new IllegalStateException(String.format("Command %s can't be processed using %s",
                    command,
                    this.getClass().getSimpleName()));
        }
    }

    protected String toJson(Object source){
        return gson.toJson(source);
    }

}
