/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.nr.agent.instrumentation.tomcat;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.api.agent.NewRelic;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class TomcatUtils {

    private static final String JMX_PREFIX = "Catalina";
    private static final String JMX_EMBEDDED_PREFIX = "Tomcat";
    private static final String JMX_EMBEDDED_DATASOURCE_PREFIX = "org.apache.tomcat.jdbc.pool.jmx";

    private static final AtomicBoolean addedJmx = new AtomicBoolean(false);

    public static void addJmx() {
        if (System.getProperty("com.sun.aas.installRoot") == null) {
            if (!addedJmx.getAndSet(true)) {
                //We need to add all three possible types because embedded tomcat breaks out
                //the types (which we named PREFIX) differently than standalone tomcat, yet the same Server/HostConfig_Instrumentation is used.
                AgentBridge.jmxApi.addJmxMBeanGroup(JMX_PREFIX);
                AgentBridge.jmxApi.addJmxMBeanGroup(JMX_EMBEDDED_PREFIX);
                AgentBridge.jmxApi.addJmxMBeanGroup(JMX_EMBEDDED_DATASOURCE_PREFIX);

                NewRelic.getAgent().getLogger().log(Level.FINER, "Added JMX for Tomcat");
            }
        }
    }

}
