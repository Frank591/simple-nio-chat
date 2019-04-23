package ru.fsl.chat.server.infrastructure.tcp.routing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.core.*;
import ru.fsl.chat.logging.LogUtils;
import ru.fsl.transport.tcp.MessageProcessor;
import ru.fsl.transport.tcp.TcpMessage;


import java.util.Map;
import java.util.UUID;

public class CommandsProcessor implements MessageProcessor {

    private final Map<Command, CommandProcessor> commandProcessors;
    private final static Logger LOG = LogManager.getLogger(CommandsProcessor.class);

    public CommandsProcessor(@NotNull Map<Command, CommandProcessor> commandProcessors) {
        this.commandProcessors = commandProcessors;
    }


    @Override
    public TcpMessage process(String request, UUID correlationId, UUID sessionId) {

        PrefixedMessage requestAsPrefixedMessage;
        try {
            requestAsPrefixedMessage = PrefixedMessage.parse(request);
        } catch (Exception e) {
            LOG.error(createLogPrefix(correlationId) + "- message can't be parsed as PrefixedMessage.", e);
            return createTcpMessage(new CommandResultMessage(CommandResult.INTERNAL_SERVER_ERROR,
                    "Message can't be parsed as PrefixedMessage."));
        }
        CommandMessage commandMessage;
        try {
            commandMessage = CommandUtils.createCommand(requestAsPrefixedMessage);
        } catch (Exception e) {
            LOG.error(createLogPrefix(correlationId) + "- message can't be parsed as CommandMessage.", e);
            return createTcpMessage(new CommandResultMessage(CommandResult.INTERNAL_SERVER_ERROR,
                    "Message can't be parsed as CommandMessage."));
        }
        return createTcpMessage(process(commandMessage, correlationId, sessionId));
    }

    private CommandResultMessage process(@NotNull CommandMessage command, @NotNull UUID corrId, @NotNull UUID sessionId) {
        try {

            CommandProcessor commandProcessor = commandProcessors.get(command.getCommand());
            if (commandProcessor == null) {
                String errorMsg = String.format("processor for command %s not found.", command.getCommand());
                LOG.warn(createLogPrefix(corrId) + errorMsg);
                return new CommandResultMessage(CommandResult.NOT_FOUND, errorMsg);
            }
            return commandProcessor.process(command, corrId, sessionId);
        } catch (Exception e) {
            String errorMsg = String.format("unexpected error while process command = %s.",
                    command.getCommand());
            LOG.error(createLogPrefix(corrId) + errorMsg, e);
            return new CommandResultMessage(CommandResult.INTERNAL_SERVER_ERROR, errorMsg);
        }
    }

    private static TcpMessage createTcpMessage(CommandResultMessage commandResultMessage) {
        return new TcpMessage(commandResultMessage.toString());
    }

    private static String createLogPrefix(@NotNull UUID corrId) {
        return LogUtils.createLogPrefix("Process command", corrId);
    }


}
