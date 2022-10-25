/*
 *
 *  * Copyright 2022 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.nr.agent.instrumentation.log4j1;

import com.newrelic.api.agent.NewRelic;
import org.apache.log4j.spi.LoggingEvent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Log4j1Util {
    // Linking metadata attributes used in blob
    private static final String BLOB_PREFIX = "NR-LINKING";
    private static final String BLOB_DELIMITER = "|";
    private static final String TRACE_ID = "trace.id";
    private static final String HOSTNAME = "hostname";
    private static final String ENTITY_GUID = "entity.guid";
    private static final String ENTITY_NAME = "entity.name";
    private static final String SPAN_ID = "span.id";
    // Keep track of first time a log4j1 configuration is loaded and instrumented
    public static AtomicBoolean log4j1Instrumented = new AtomicBoolean(false);

    public static void setLog4j1Enabled() {
        if (!log4j1Instrumented.getAndSet(true)) {
            NewRelic.incrementCounter("Supportability/Logging/Java/Log4j1/enabled");
        }
    }

    public static String appendAgentMetadataIfLocalDecoratingEnabled(String formattedLogMessage) {
        if (formattedLogMessage != null && isApplicationLoggingLocalDecoratingEnabled()) {
            int breakLine = formattedLogMessage.lastIndexOf("\n");
            StringBuilder builder = new StringBuilder(formattedLogMessage);
            if (breakLine != -1) {
                builder.replace(breakLine, breakLine + 1, "");
            }
            return builder.append(getLinkingMetadataBlob()).append("\n").toString();
        }
        return formattedLogMessage;
    }

    public static void generateMetricsAndOrLogEventIfEnabled(LoggingEvent event) {
        if (isApplicationLoggingEnabled()) {
            if (isApplicationLoggingMetricsEnabled()) {
                generateLogMetrics(event);
            }
            if (isApplicationLoggingForwardingEnabled()) {
                recordNewRelicLogEvent(event);
            }
        }
    }

    private static void generateLogMetrics(LoggingEvent event) {
        NewRelic.incrementCounter("Logging/lines");
        NewRelic.incrementCounter("Logging/lines/" + event.getLevel().toString());
    }

    private static void recordNewRelicLogEvent(LoggingEvent event) {
        if (shouldCreateNewRelicLogEventFor(event)) {
            Map<String, Object> logEventMap = LoggingEventMap.from(event);
            // FIXME!!!
            //AgentBridge.getAgent().getLogSender().recordLogEvent(logEventMap);
        }
    }

    // appends agent linking metadata of format
    // NR-LINKING|entity.guid|hostname|trace.id|span.id|entity.name|
    private static String getLinkingMetadataBlob() {
        Map<String, String> agentLinkingMetadata = NewRelic.getAgent().getLinkingMetadata();
        StringBuilder blob = new StringBuilder();
        blob.append(" ").append(BLOB_PREFIX).append(BLOB_DELIMITER);

        if (agentLinkingMetadata != null && agentLinkingMetadata.size() > 0) {
            appendAttributeToBlob(agentLinkingMetadata.get(ENTITY_GUID), blob);
            appendAttributeToBlob(agentLinkingMetadata.get(HOSTNAME), blob);
            appendAttributeToBlob(agentLinkingMetadata.get(TRACE_ID), blob);
            appendAttributeToBlob(agentLinkingMetadata.get(SPAN_ID), blob);
            appendAttributeToBlob(urlEncode(agentLinkingMetadata.get(ENTITY_NAME)), blob);
        }
        return blob.toString();
    }

    private static void appendAttributeToBlob(String attribute, StringBuilder blob) {
        if (attribute != null && !attribute.isEmpty()) {
            blob.append(attribute);
        }
        blob.append(BLOB_DELIMITER);
    }

    static String urlEncode(String value) {
        try {
            if (value != null) {
                value = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
            }
        } catch (UnsupportedEncodingException e) {
            NewRelic.getAgent()
                    .getLogger()
                    .log(java.util.logging.Level.WARNING, "Unable to URL encode entity.name for application_logging.local_decorating", e);
        }
        return value;
    }

    private static boolean shouldCreateNewRelicLogEventFor(LoggingEvent event) {
        return event != null &&
                event.getMessage() != null &&
                event.getThrowableInformation() != null &&
                event.getThrowableInformation().getThrowable() != null;
    }

    private static boolean isApplicationLoggingEnabled() {
        return is("application_logging.enabled", true);
    }

    private static boolean isApplicationLoggingMetricsEnabled() {
        return is("application_logging.metrics.enabled", true);
    }

    private static boolean isApplicationLoggingForwardingEnabled() {
        return is("application_logging.forwarding.enabled", true);
    }

    private static boolean isApplicationLoggingLocalDecoratingEnabled() {
        return is("application_logging.local_decorating.enabled", false);
    }

    private static boolean is(String key, boolean defaultValue) {
        return NewRelic.getAgent().getConfig().getValue(key, defaultValue);
    }
}