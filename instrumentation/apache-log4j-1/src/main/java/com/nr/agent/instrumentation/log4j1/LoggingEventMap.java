package com.nr.agent.instrumentation.log4j1;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.HashMap;
import java.util.Map;

class LoggingEventMap {
    static final int DEFAULT_NUM_OF_LOG_EVENT_ATTRIBUTES = 10;
    static final String MESSAGE = "message";
    static final String TIMESTAMP = "timestamp";
    static final String LEVEL = "level";
    static final String ERROR_MESSAGE = "error.message";
    static final String ERROR_CLASS = "error.class";
    static final String ERROR_STACK = "error.stack";
    static final String THREAD_NAME = "thread.name";
    static final String THREAD_ID = "thread.id";
    static final String LOGGER_NAME = "logger.name";
    static final String LOGGER_FQCN = "logger.fqcn";
    static final String UNKNOWN = "UNKNOWN";

    static Map<String, Object> from(LoggingEvent event) {
        HashMap<String, Object> logEventMap = new HashMap<>(DEFAULT_NUM_OF_LOG_EVENT_ATTRIBUTES);
        String message = event.getRenderedMessage();
        Throwable throwable = null;
        ThrowableInformation throwableInformation = event.getThrowableInformation();
        if (throwableInformation != null) {
            throwable = throwableInformation.getThrowable();
        }
        if (message != null && !message.isEmpty()) {
            logEventMap.put(MESSAGE, message);
        }
        logEventMap.put(TIMESTAMP, event.getTimeStamp());

        Level level = event.getLevel();
        if (level != null) {
            String levelName = level.toString();
            if (levelName.isEmpty()) {
                logEventMap.put(LEVEL, UNKNOWN);
            } else {
                logEventMap.put(LEVEL, levelName);
            }
        }

        String errorStack = Log4j1ExceptionUtil.getErrorStack(throwable);
        if (errorStack != null) {
            logEventMap.put(ERROR_STACK, errorStack);
        }

        String errorMessage = Log4j1ExceptionUtil.getErrorMessage(throwable);
        if (errorMessage != null) {
            logEventMap.put(ERROR_MESSAGE, errorMessage);
        }

        String errorClass = Log4j1ExceptionUtil.getErrorClass(throwable);
        if (errorClass != null) {
            logEventMap.put(ERROR_CLASS, errorClass);
        }

        String threadName = event.getThreadName();
        if (threadName != null) {
            logEventMap.put(THREAD_NAME, threadName);
        }

        logEventMap.put(THREAD_ID, Thread.currentThread().getId());

        String loggerName = event.getLoggerName();
        if (loggerName != null) {
            logEventMap.put(LOGGER_NAME, loggerName);
        }

        String loggerFqcn = event.getFQNOfLoggerClass();
        if (loggerFqcn != null) {
            logEventMap.put(LOGGER_FQCN, loggerFqcn);
        }
        return logEventMap;
    }
}