package ru.fsl.chat.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.fsl.chat.contracts.dto.*;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ChatClient implements ServerDisconnectSubscriber {

    private static final Logger LOG = LogManager.getLogger(ChatClient.class);
    private final Printer printer;
    private final ServerClient serverClient;
    volatile boolean isClosed = false;
    private final static String DELIMITER = ":";
    private final ScheduledExecutorService scheduler;
    private final static int GET_MESSAGES_TIMEOUT_MS = 1000;


    public ChatClient(@NotNull Printer printer,
                      @NotNull ServerClient serverClient) {
        this.printer = printer;
        this.serverClient = serverClient;
        serverClient.subscribeOnServerDisconnect(this);
        BasicThreadFactory factory = new BasicThreadFactory.Builder()
                .namingPattern("get chat messages worker-%d")
                .daemon(true)
                .uncaughtExceptionHandler(new AppUncaughtExceptionHandler())
                .build();
        scheduler = Executors.newScheduledThreadPool(1, factory);
        scheduler.scheduleWithFixedDelay(new GetMessagesTask(serverClient,
                        printer),
                0, GET_MESSAGES_TIMEOUT_MS,
                TimeUnit.MILLISECONDS);


        printer.printSystem("Welcome to chat client. \r\nType HELP to see available commands.\r\n");
    }

    public void acceptCommand(String commandAsStr) {
        if (StringUtils.isEmpty(commandAsStr)) {
            return;
        }
        if (UICommand.CLOSE.name().equalsIgnoreCase(commandAsStr)) {
            processCloseCommand();
        } else if (UICommand.CONNECT.name().equalsIgnoreCase(commandAsStr)) {
            processConnectCommand();
        } else if (UICommand.HELP.name().equalsIgnoreCase(commandAsStr)) {
            processHelpCommand();
        } else if (isStartsWithCommand(UICommand.AUTHORIZE, commandAsStr)) {
            processAuthorizeCommand(commandAsStr);
        } else if (isStartsWithCommand(UICommand.PRINT, commandAsStr)) {
            processPrintCommand(commandAsStr);
        } else if (UICommand.SHOW.name().equalsIgnoreCase(commandAsStr)) {
            processShowCommand();
        } else {
            printer.printSystem(String.format("'%s' is unknown command", commandAsStr));
        }

    }

    public boolean isClosed() {
        return isClosed;
    }


    @Override
    public void serverDisconnected() {
        printer.printSystem("you are disconnected from server");
    }

    private boolean isStartsWithCommand(UICommand command, @NotNull String source) {
        return source.toLowerCase().startsWith(command.name().toLowerCase());
    }

    private void processHelpCommand() {
        logUICommandAccepted(UICommand.HELP);
        StringBuilder stringBuilder = new StringBuilder();
        for (UICommand command : UICommand.values()) {
            stringBuilder.append(String.format("%s\r\n", command.name().toUpperCase()));
        }
        printer.printSystem(stringBuilder.toString());
        logUICommandProcessed(UICommand.HELP);
    }

    private void processAuthorizeCommand(String commandAsStr) {
        logUICommandAccepted(UICommand.AUTHORIZE);
        try {
            String[] commandParts = commandAsStr.split(DELIMITER);
            if (commandParts.length != 2) {
                printer.printSystem("invalid authorize command format");
                return;
            }
            String userName = commandParts[1];
            if (userName == null || userName.trim() == "") {
                printer.printSystem("user name can't be empty");
            }
            serverClient.authorize(new UserAuthorizationRequest(userName));
            printer.printSystem("authorized");
        } catch (Exception e) {
            LOG.error("Error while print message", e);
            printer.printSystem("can't authorize user");
        }
        logUICommandProcessed(UICommand.AUTHORIZE);
    }

    private void processPrintCommand(String commandAsStr) {
        logUICommandAccepted(UICommand.PRINT);
        try {
            String[] commandParts = commandAsStr.split(DELIMITER);
            if (commandParts.length != 2) {
                printer.printSystem("invalid print command format");
                return;
            }
            String message = commandParts[1];
            if (message == null || message.trim() == "") {
                printer.printSystem("message can't be empty");
            }
            serverClient.printMessage(new PrintMessageRequest(message));
        } catch (Exception e) {
            LOG.error("Error while print message", e);
            printer.printSystem("can't print message");
        }
        logUICommandProcessed(UICommand.PRINT);
    }

    private void processConnectCommand() {
        logUICommandAccepted(UICommand.CONNECT);
        try {
            serverClient.connect();
            printer.printSystem("connected");
        } catch (Exception e) {
            LOG.error("Error while connection to server", e);
            printer.printSystem("can't connect");
        }
        logUICommandProcessed(UICommand.CONNECT);
    }

    private void processShowCommand() {
        logUICommandAccepted(UICommand.SHOW);
        try {
            StatisticRequest request = new StatisticRequest(StatisticType.SESSIONS);
            StatisticResponse response = serverClient.getStatistic(request);
            printer.printSystem("server sessions count = " + response.getSessionsCount());
        } catch (Exception e) {
            LOG.error("Error while request server statistic", e);
            printer.printSystem("server statistic unavailable");
        }
        logUICommandProcessed(UICommand.SHOW);
    }

    private void processCloseCommand() {
        logUICommandAccepted(UICommand.CLOSE);
        try {
            serverClient.disconnect();
        } catch (Exception e) {
            LOG.error("Error while disconnecting from server", e);
        }
        isClosed = true;
        logUICommandProcessed(UICommand.CLOSE);
    }


    private void logUICommandAccepted(UICommand command) {
        LOG.debug(String.format("UICommand '%s' accepted", command));
    }

    private void logUICommandProcessed(UICommand command) {
        LOG.debug(String.format("UICommand '%s' processed.", command));
    }
}
