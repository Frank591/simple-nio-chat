package ru.fsl.chat.client;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class Application {
    private static final Logger LOG = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            LOG.info("Starting chat client..");

            if (args.length != 2) {
                LOG.error(String.format("Invalid command line args count: required 2 but was %s.", args.length));
                System.exit(-1);
            }
            String serverHost = args[0];
            int serverPort = Integer.parseInt(args[1]);
            InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
            ServerClient serverClient = new TcpServerClient(serverAddress, new Gson());
            Thread.setDefaultUncaughtExceptionHandler(new AppUncaughtExceptionHandler());
            ChatClient chatClient = new ChatClient(new ConsolePrinter(), serverClient);
            Scanner in = new Scanner(System.in);
            while (!chatClient.isClosed()){
                String userInput = in.nextLine();
                chatClient.acceptCommand(userInput);
                Thread.sleep(500);
            }
            LOG.info("Chat client closed.");
        } catch (Exception e) {
            LOG.fatal("Unexpected error. Server stopped.", e);
        }
    }
}
