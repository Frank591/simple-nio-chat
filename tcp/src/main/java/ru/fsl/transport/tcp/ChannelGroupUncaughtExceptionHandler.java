package ru.fsl.transport.tcp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChannelGroupUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOG = LogManager.getLogger(ChannelGroupUncaughtExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOG.error(String.format("Uncaught exception inside thread %s(%s)", t.getName(), t.getId()), e);
    }
}
