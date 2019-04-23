package ru.fsl.transport.tcp;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class TcpServer {
    private AsynchronousChannelGroup asyncChannelGroup;
    private String name;
    private InetSocketAddress serverAddress;
    private MessageProcessor messageProcessor;
    private final TcpSessionsManager tcpSessionsManager;
    private AsynchronousServerSocketChannel listener;
    private static final Logger LOG = LogManager.getLogger(TcpServer.class.getName());


    public TcpServer(@NotNull String name, @NotNull InetSocketAddress serverAddress, @NotNull MessageProcessor messageProcessor,
                     @NotNull TcpSessionsManager tcpSessionsManager) throws IOException {
        this.name = name;
        this.serverAddress = serverAddress;
        this.messageProcessor = messageProcessor;
        this.tcpSessionsManager = tcpSessionsManager;
        BasicThreadFactory factory = new BasicThreadFactory.Builder()
                .namingPattern(name + "worker-%d")
                .daemon(true)
                .priority(Thread.MAX_PRIORITY)
                .uncaughtExceptionHandler(new ChannelGroupUncaughtExceptionHandler())
                .build();

        asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(
                new ThreadPoolExecutor(0, 16,
                        60L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(), factory));
    }

    public void run() {
        try {
            LOG.info("Start listening at " + serverAddress);
            listener = AsynchronousServerSocketChannel.open(asyncChannelGroup).bind(serverAddress);
            listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            LOG.info("Listening started at " + serverAddress);

            if (listener == null) {
                LOG.fatal("Listener is null");
                return;
            }
            if (!listener.isOpen()) {
                LOG.fatal("Listener is not open");
                return;
            }
            listener.accept(null, new ClientConnectionHandler(listener, tcpSessionsManager, messageProcessor));
            LOG.info("Server started successfully.");
            asyncChannelGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (IOException e) {
            LOG.fatal("Can't open listener at" + serverAddress, e);

        } catch (InterruptedException e) {
            LOG.error("asyncChannelGroup.awaitTermination was interrupted", e);
            try {
                stopServer();
            } catch (IOException ex) {
                LOG.error("Error while stopping server after interrupt awaitTermination", ex);
            }
        } catch (Exception e) {
            LOG.fatal("Unexpected exception while server run", e);
            throw e;
        }
    }

    public void stopServer() throws IOException {
        LOG.info("Stopping server");
        if (listener != null && listener.isOpen()) {
            this.listener.close();
        }
        try {
            //extra time for close listener
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOG.error(e);
        }
        if (asyncChannelGroup != null && !asyncChannelGroup.isShutdown()) {
            this.asyncChannelGroup.shutdownNow();
        }
        LOG.info("Server stopped");
    }

}