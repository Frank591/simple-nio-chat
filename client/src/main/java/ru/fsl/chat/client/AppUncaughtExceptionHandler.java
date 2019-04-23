package ru.fsl.chat.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOG = LogManager.getLogger(AppUncaughtExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOG.error(String.format("Uncaught exception inside thread %s(%s)", t.getName(), t.getId()), e);
    }
}
