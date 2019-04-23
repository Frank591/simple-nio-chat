package ru.fsl.chat.contracts.core;

import org.jetbrains.annotations.NotNull;

public final class CommandUtils {

    private CommandUtils(){
    }

    @NotNull
    public static CommandMessage createCommand(PrefixedMessage prefixedMessage) {
        Command command = Command.parse(prefixedMessage.getCode());
        return new CommandMessage(command, prefixedMessage.getBody());
    }

    @NotNull
    public static CommandResultMessage createCommandResult(PrefixedMessage prefixedMessage) {
        CommandResult commandResult = CommandResult.parse(prefixedMessage.getCode());
        return new CommandResultMessage(commandResult, prefixedMessage.getBody());
    }

}
