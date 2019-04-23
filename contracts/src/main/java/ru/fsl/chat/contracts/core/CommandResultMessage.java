package ru.fsl.chat.contracts.core;

import org.jetbrains.annotations.Nullable;

public class CommandResultMessage extends PrefixedMessage {

    private final CommandResult commandResult;

    public CommandResultMessage(CommandResult commandResult, @Nullable String body) {
        super(commandResult.getCode(), body);
        this.commandResult = commandResult;
    }

    public CommandResult getCommandResult() {
        return commandResult;
    }
}
