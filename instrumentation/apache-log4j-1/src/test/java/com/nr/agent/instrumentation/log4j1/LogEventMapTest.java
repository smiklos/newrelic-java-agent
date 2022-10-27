package com.nr.agent.instrumentation.log4j1;

import com.newrelic.agent.bridge.logging.AppLoggingUtils;
import com.newrelic.agent.bridge.logging.LogAttributeKey;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
public class LogEventMapTest {

    @Mock Category category;

    @Test
    public void minimalCaseShouldNotFail() {
        // given a logging event with all null parameters that are allowed
        LoggingEvent minimalEvent = new LoggingEvent(null, category, null, null, null);

        // when logging event created
        Map<LogAttributeKey, Object> loggingEventMap = LoggingEventMap.from(minimalEvent);

        // then it is not null
        assertNotNull(loggingEventMap);

        // and thread/timestamp fields are not null
        assertEquals(minimalEvent.getThreadName(), loggingEventMap.get(AppLoggingUtils.THREAD_NAME));
        assertNotNull(loggingEventMap.get(AppLoggingUtils.THREAD_ID));
        assertNotNull(loggingEventMap.get(AppLoggingUtils.TIMESTAMP));
    }

    @Test
    public void baseCase() {
        // given a logging event created with all parameters
        String message = "Hello";
        Throwable throwable = new RuntimeException("SIMULATED");
        String fqnOfLogger = "com.newrelic.SomeClass";
        LoggingEvent event = new LoggingEvent(fqnOfLogger, category, Priority.ERROR, "Hello", throwable);

        // when logging even created
        Map<LogAttributeKey, Object> loggingEventMap = LoggingEventMap.from(event);

        // then it is not null
        assertNotNull(loggingEventMap);

        // and all message/throwable fields are set correctly
        assertEquals(message, event.getRenderedMessage());
        assertEquals(throwable, event.getThrowableInformation().getThrowable());
        assertEquals(Level.ERROR, event.getLevel());
        assertEquals(fqnOfLogger, loggingEventMap.get(AppLoggingUtils.LOGGER_FQCN));

        // and thread/timestamp fields are set
        assertEquals(event.getThreadName(), loggingEventMap.get(AppLoggingUtils.THREAD_NAME));
        assertNotNull(loggingEventMap.get(AppLoggingUtils.THREAD_ID));
        assertNotNull(loggingEventMap.get(AppLoggingUtils.TIMESTAMP));
    }
}
