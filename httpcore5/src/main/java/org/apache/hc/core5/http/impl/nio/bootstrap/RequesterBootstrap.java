/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.core5.http.impl.nio.bootstrap;

import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.ConnectionConfig;
import org.apache.hc.core5.http.impl.ConnectionListener;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.DefaultContentLengthStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.nio.ClientHttp1IOEventHandlerFactory;
import org.apache.hc.core5.http.impl.nio.DefaultHttpRequestWriterFactory;
import org.apache.hc.core5.http.impl.nio.DefaultHttpResponseParserFactory;
import org.apache.hc.core5.http.nio.ssl.BasicClientTlsStrategy;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.pool.ConnPoolListener;
import org.apache.hc.core5.pool.StrictConnPool;
import org.apache.hc.core5.reactor.IOReactorConfig;

/**
 * @since 5.0
 */
public class RequesterBootstrap {

    private IOReactorConfig ioReactorConfig;
    private ConnectionConfig connectionConfig;
    private HttpProcessor httpProcessor;
    private ConnectionReuseStrategy connStrategy;
    private int defaultMaxPerRoute;
    private int maxTotal;
    private long timeToLive;
    private TimeUnit timeUnit;
    private TlsStrategy tlsStrategy;
    private ExceptionListener exceptionListener;
    private ConnectionListener connectionListener;
    private Http1StreamListener streamListener;
    private ConnPoolListener<HttpHost> connPoolListener;

    private RequesterBootstrap() {
    }

    public static RequesterBootstrap bootstrap() {
        return new RequesterBootstrap();
    }

    /**
     * Sets I/O reactor configuration.
     */
    public final RequesterBootstrap setIOReactorConfig(final IOReactorConfig ioReactorConfig) {
        this.ioReactorConfig = ioReactorConfig;
        return this;
    }

    /**
     * Sets connection configuration.
     */
    public final RequesterBootstrap setConnectionConfig(final ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
        return this;
    }

    /**
     * Assigns {@link HttpProcessor} instance.
     */
    public final RequesterBootstrap setHttpProcessor(final HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
        return this;
    }

    /**
     * Assigns {@link ConnectionReuseStrategy} instance.
     */
    public final RequesterBootstrap setConnectionReuseStrategy(final ConnectionReuseStrategy connStrategy) {
        this.connStrategy = connStrategy;
        return this;
    }

    public final RequesterBootstrap setDefaultMaxPerRoute(final int defaultMaxPerRoute) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        return this;
    }

    public final RequesterBootstrap setMaxTotal(final int maxTotal) {
        this.maxTotal = maxTotal;
        return this;
    }

    public final RequesterBootstrap setTimeToLive(final long timeToLive, final TimeUnit timeUnit) {
        this.timeToLive = timeToLive;
        this.timeUnit = timeUnit;
        return this;
    }

    /**
     * Assigns {@link TlsStrategy} instance.
     */
    public final RequesterBootstrap setTlsStrategy(final TlsStrategy tlsStrategy) {
        this.tlsStrategy = tlsStrategy;
        return this;
    }

    /**
     * Assigns {@link ExceptionListener} instance.
     */
    public final RequesterBootstrap setExceptionListener(final ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
        return this;
    }

    /**
     * Assigns {@link ConnectionListener} instance.
     */
    public final RequesterBootstrap setConnectionListener(final ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        return this;
    }

    /**
     * Assigns {@link Http1StreamListener} instance.
     */
    public final RequesterBootstrap setStreamListener(final Http1StreamListener streamListener) {
        this.streamListener = streamListener;
        return this;
    }

    /**
     * Assigns {@link ConnPoolListener} instance.
     */
    public final RequesterBootstrap setConnPoolListener(final ConnPoolListener<HttpHost> connPoolListener) {
        this.connPoolListener = connPoolListener;
        return this;
    }

    public HttpAsyncRequester create() {
        final StrictConnPool<HttpHost, ClientEndpoint> connPool = new StrictConnPool<>(
                defaultMaxPerRoute > 0 ? defaultMaxPerRoute : 20,
                maxTotal > 0 ? maxTotal : 50,
                timeToLive, timeUnit != null ? timeUnit : TimeUnit.MILLISECONDS,
                connPoolListener);
        final ClientHttp1IOEventHandlerFactory ioEventHandlerFactory = new ClientHttp1IOEventHandlerFactory(
                httpProcessor != null ? httpProcessor : HttpProcessors.client(),
                connectionConfig,
                connStrategy != null ? connStrategy : DefaultConnectionReuseStrategy.INSTANCE,
                DefaultHttpResponseParserFactory.INSTANCE,
                DefaultHttpRequestWriterFactory.INSTANCE,
                DefaultContentLengthStrategy.INSTANCE,
                DefaultContentLengthStrategy.INSTANCE,
                connectionListener,
                streamListener);
        return new HttpAsyncRequester(
                ioReactorConfig,
                ioEventHandlerFactory,
                connPool,
                tlsStrategy != null ? tlsStrategy : new BasicClientTlsStrategy(),
                exceptionListener);
    }

}
