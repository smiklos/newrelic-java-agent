/*
 *
 *  * Copyright 2022 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package org.glassfish.jersey.server;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.agent.bridge.Transaction;
import com.newrelic.api.agent.Segment;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.WeaveAllConstructors;
import com.newrelic.api.agent.weaver.Weaver;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.internal.util.collection.Value;

public class ServerRuntime_Instrumentation {

    @Weave(type = MatchType.ExactClass, originalName = "org.glassfish.jersey.server.ServerRuntime$AsyncResponder")
    static class AsyncResponder_Instrumentation {

        @NewField
        Token token;

        @NewField
        Segment segment;

        private final Responder_Instrumentation responder = Weaver.callOriginal();

        @WeaveAllConstructors
        AsyncResponder_Instrumentation() {
            Transaction transaction = AgentBridge.getAgent().getTransaction(false);
            if (transaction != null) {
                segment = transaction.startSegment("Java", "Jakarta-RSAsync@Suspended");
                segment.setMetricName("Java", "Jakarta-RSAsync@Suspended");
                token = transaction.getToken();
            }
        }

        private boolean resume(Runnable handler) {
            if (token != null) {
                responder.token = token;
            }
            boolean result = Weaver.callOriginal();
            if (token != null) {
                segment.end();
            }
            return result;
        }

        private boolean cancel(final Value<Response> responseValue) {
            if (token != null) {
                responder.token = token;
            }
            boolean result = Weaver.callOriginal();
            if (token != null) {
                segment.end();
            }
            return result;
        }
    }

    @Weave(type = MatchType.ExactClass, originalName = "org.glassfish.jersey.server.ServerRuntime$ConnectionCallbackRunner")
    static class ConnectionCallbackRunner_Instrumentation {

        public void onDisconnect(final AsyncResponse disconnected) {
            Weaver.callOriginal();

            if (disconnected instanceof AsyncResponder_Instrumentation) {
                AsyncResponder_Instrumentation asyncResponder = (AsyncResponder_Instrumentation) disconnected;

                // Ensure that we always end the Segment and expire the Token, even if the case of a client disconnect
                Segment segment = asyncResponder.segment;
                if (segment != null) {
                    segment.end();
                }

                Token token = asyncResponder.token;
                if (token != null) {
                    token.expire();
                    asyncResponder.token = null;
                }
            }
        }

    }

}
