package ru.fsl.chat.contracts.core;

import org.jetbrains.annotations.Nullable;

public class CommandMessage extends PrefixedMessage  {

    private final Command command;

    public CommandMessage(Command command, @Nullable String body) {
        super(command.getCode(), body);
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

}
