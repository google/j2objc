/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.net.ssl;

import java.nio.ByteBuffer;

/**
 * The abstract implementation of secure communications using SSL, TLS, or other
 * protocols. It includes the setup, handshake, and encrypt/decrypt
 * functionality needed to create a secure connection.
 *
 * <h3>Default configuration</h3>
 * <p>{@code SSLEngine} instances obtained from default {@link SSLContext} are configured as
 * follows:
 *
 * <h4>Protocols</h4>
 * <table>
 *     <thead>
 *         <tr>
 *             <th>Protocol</th>
 *             <th>Supported (API Levels)</th>
 *             <th>Enabled by default (API Levels)</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td>SSLv3</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>TLSv1</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <h4>Cipher suites</h4>
 * <table>
 *     <thead>
 *         <tr>
 *             <th>Cipher suite</th>
 *             <th>Supported (API Levels)</th>
 *             <th>Enabled by default (API Levels)</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td>SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_EXPORT_WITH_RC4_40_MD5</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_WITH_RC4_128_MD5</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_DSS_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_RSA_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_EXPORT_WITH_RC4_40_MD5</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_NULL_MD5</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_NULL_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_RC4_128_MD5</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_RC4_128_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_DES_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_DSS_WITH_DES_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_RSA_WITH_DES_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_DES_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_DES_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_NULL_WITH_NULL_NULL</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_DES_CBC_SHA</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_NULL_MD5</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_NULL_SHA</td>
 *             <td>1-8</td>
 *             <td></td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @since 1.5
 */
public abstract class SSLEngine {
    private final String peerHost;
    private final int peerPort;

    /**
     * Creates a new {@code SSLEngine} instance.
     */
    protected SSLEngine() {
        peerHost = null;
        peerPort = -1;
    }

    /**
     * Creates a new {@code SSLEngine} instance with the specified host and
     * port.
     *
     * @param host
     *            the name of the host.
     * @param port
     *            the port of the host.
     */
    protected SSLEngine(String host, int port) {
        this.peerHost = host;
        this.peerPort = port;
    }

    /**
     * Returns the name of the peer host.
     *
     * @return the name of the peer host, or {@code null} if none is available.
     */
    public String getPeerHost() {
        return peerHost;
    }

    /**
     * Returns the port number of the peer host.
     *
     * @return the port number of the peer host, or {@code -1} is none is
     *         available.
     */
    public int getPeerPort() {
        return peerPort;
    }

    /**
     * Initiates a handshake on this engine.
     * <p>
     * Calling this method is not needed for the initial handshake: it will be
     * called by {@code wrap} or {@code unwrap} if the initial handshake has not
     * been started yet.
     *
     * @throws SSLException
     *             if starting the handshake fails.
     * @throws IllegalStateException
     *             if the engine does not have all the needed settings (e.g.
     *             client/server mode not set).
     */
    public abstract void beginHandshake() throws SSLException;

    /**
     * Notifies this engine instance that no more inbound network data will be
     * sent to this engine.
     *
     * @throws SSLException
     *             if this engine did not receive a needed protocol specific
     *             close notification message from the peer.
     */
    public abstract void closeInbound() throws SSLException;

    /**
     * Notifies this engine instance that no more outbound application data will
     * be sent to this engine.
     */
    public abstract void closeOutbound();

    /**
     * Returns a delegate task for this engine instance. Some engine operations
     * may require the results of blocking or long running operations, and the
     * {@code SSLEngineResult} instances returned by this engine may indicate
     * that a delegated task result is needed. In this case the
     * {@link Runnable#run() run} method of the returned {@code Runnable}
     * delegated task must be called.
     *
     * @return a delegate task, or {@code null} if none are available.
     */
    public abstract Runnable getDelegatedTask();

    /**
     * Returns the SSL cipher suite names that are enabled in this engine
     * instance.
     *
     * @return the SSL cipher suite names that are enabled in this engine
     *         instance.
     */
    public abstract String[] getEnabledCipherSuites();

    /**
     * Returns the protocol version names that are enabled in this engine
     * instance.
     *
     * @return the protocol version names that are enabled in this engine
     *         instance.
     */
    public abstract String[] getEnabledProtocols();

    /**
     * Returns whether new SSL sessions may be established by this engine.
     *
     * @return {@code true} if new session may be established, {@code false} if
     *         existing sessions must be reused.
     */
    public abstract boolean getEnableSessionCreation();

    /**
     * Returns the status of the handshake of this engine instance.
     *
     * @return the status of the handshake of this engine instance.
     */
    public abstract SSLEngineResult.HandshakeStatus getHandshakeStatus();

    /**
     * Returns whether this engine instance will require client authentication.
     *
     * @return {@code true} if this engine will require client authentication,
     *         {@code false} if no client authentication is needed.
     */
    public abstract boolean getNeedClientAuth();

    /**
     * Returns the SSL session for this engine instance.
     *
     * @return the SSL session for this engine instance.
     */
    public abstract SSLSession getSession();

    /**
     * Returns the SSL cipher suite names that are supported by this engine.
     * These cipher suites can be enabled using
     * {@link #setEnabledCipherSuites(String[])}.
     *
     * @return the SSL cipher suite names that are supported by this engine.
     */
    public abstract String[] getSupportedCipherSuites();

    /**
     * Returns the protocol names that are supported by this engine. These
     * protocols can be enables using {@link #setEnabledProtocols(String[])}.
     *
     * @return the protocol names that are supported by this engine.
     */
    public abstract String[] getSupportedProtocols();

    /**
     * Returns whether this engine is set to act in client mode when
     * handshaking.
     *
     * @return {@code true} if the engine is set to do handshaking in client
     *         mode.
     */
    public abstract boolean getUseClientMode();

    /**
     * Returns whether this engine will request client authentication.
     *
     * @return {@code true} if client authentication will be requested,
     *         {@code false} otherwise.
     */
    public abstract boolean getWantClientAuth();

    /**
     * Returns whether no more inbound data will be accepted by this engine.
     *
     * @return {@code true} if no more inbound data will be accepted by this
     *         engine, {@code false} otherwise.
     */
    public abstract boolean isInboundDone();

    /**
     * Returns whether no more outbound data will be produced by this engine.
     *
     * @return {@code true} if no more outbound data will be producted by this
     *         engine, {@code otherwise} false.
     */
    public abstract boolean isOutboundDone();

    /**
     * Sets the SSL cipher suite names that should be enabled in this engine
     * instance. Only cipher suites listed by {@code getSupportedCipherSuites()}
     * are allowed.
     *
     * @param suites
     *            the SSL cipher suite names to be enabled.
     * @throws IllegalArgumentException
     *             if one of the specified cipher suites is not supported, or if
     *             {@code suites} is {@code null}.
     */
    public abstract void setEnabledCipherSuites(String[] suites);

    /**
     * Sets the protocol version names that should be enabled in this engine
     * instance. Only protocols listed by {@code getSupportedProtocols()} are
     * allowed.
     *
     * @param protocols
     *            the protocol version names to be enabled.
     * @throws IllegalArgumentException
     *             if one of the protocol version names is not supported, or if
     *             {@code protocols} is {@code null}.
     */
    public abstract void setEnabledProtocols(String[] protocols);

    /**
     * Sets whether new SSL sessions may be established by this engine instance.
     *
     * @param flag
     *            {@code true} if new SSL sessions may be established,
     *            {@code false} if existing SSL sessions must be reused.
     */
    public abstract void setEnableSessionCreation(boolean flag);

    /**
     * Sets whether this engine must require client authentication. The client
     * authentication is one of:
     * <ul>
     * <li>authentication required</li>
     * <li>authentication requested</li>
     * <li>no authentication needed</li>
     * </ul>
     * This method overrides the setting of {@link #setWantClientAuth(boolean)}.
     *
     * @param need
     *            {@code true} if client authentication is required,
     *            {@code false} if no authentication is needed.
     */
    public abstract void setNeedClientAuth(boolean need);

    /**
     * Sets whether this engine should act in client (or server) mode when
     * handshaking.
     *
     * @param mode
     *            {@code true} if this engine should act in client mode,
     *            {@code false} if not.
     * @throws IllegalArgumentException
     *             if this method is called after starting the initial
     *             handshake.
     */
    public abstract void setUseClientMode(boolean mode);

    /**
     * Sets whether this engine should request client authentication. The client
     * authentication is one of the following:
     * <ul>
     * <li>authentication required</li>
     * <li>authentication requested</li>
     * <li>no authentication needed</li>
     * </ul>
     * This method overrides the setting of {@link #setNeedClientAuth(boolean)}.
     *
     * @param want
     *            {@code true} if client authentication should be requested,
     *            {@code false} if no authentication is needed.
     */
    public abstract void setWantClientAuth(boolean want);

    /**
     * Decodes the incoming network data buffer into application data buffers.
     * If a handshake has not been started yet, it will automatically be
     * started.
     *
     * @param src
     *            the buffer with incoming network data
     * @param dsts
     *            the array of destination buffers for incoming application
     *            data.
     * @param offset
     *            the offset in the array of destination buffers to which data
     *            is to be transferred.
     * @param length
     *            the maximum number of destination buffers to be used.
     * @return the result object of this operation.
     * @throws SSLException
     *             if a problem occurred while processing the data.
     * @throws IndexOutOfBoundsException
     *             if {@code length} is greater than
     *             {@code dsts.length - offset}.
     * @throws java.nio.ReadOnlyBufferException
     *             if one of the destination buffers is read-only.
     * @throws IllegalArgumentException
     *             if {@code src}, {@code dsts}, or one of the entries in
     *             {@code dsts} is {@code null}.
     * @throws IllegalStateException
     *             if the engine does not have all the needed settings (e.g.
     *             client/server mode not set).
     */
    public abstract SSLEngineResult unwrap(ByteBuffer src,
                                           ByteBuffer[] dsts,
                                           int offset,
                                           int length) throws SSLException;

    /**
     * Encodes the outgoing application data buffers into the network data
     * buffer. If a handshake has not been started yet, it will automatically be
     * started.
     *
     * @param srcs
     *            the array of source buffers of outgoing application data.
     * @param offset
     *            the offset in the array of source buffers from which data is
     *            to be retrieved.
     * @param length
     *            the maximum number of source buffers to be used.
     * @param dst
     *            the destination buffer for network data.
     * @return the result object of this operation.
     * @throws SSLException
     *             if a problem occurred while processing the data.
     * @throws IndexOutOfBoundsException
     *             if {@code length} is greater than
     *             {@code srcs.length - offset}.
     * @throws java.nio.ReadOnlyBufferException
     *             if the destination buffer is readonly.
     * @throws IllegalArgumentException
     *             if {@code srcs}, {@code dst}, or one the entries in
     *             {@code srcs} is {@code null}.
     * @throws IllegalStateException
     *             if the engine does not have all the needed settings (e.g.
     *             client/server mode not set).
     */
    public abstract SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst)
            throws SSLException;

    /**
     * Decodes the incoming network data buffer into the application data
     * buffer. If a handshake has not been started yet, it will automatically be
     * started.
     *
     * @param src
     *            the buffer with incoming network data
     * @param dst
     *            the destination buffer for incoming application data.
     * @return the result object of this operation.
     * @throws SSLException
     *             if a problem occurred while processing the data.
     * @throws java.nio.ReadOnlyBufferException
     *             if one of the destination buffers is read-only.
     * @throws IllegalArgumentException
     *             if {@code src} or {@code dst} is {@code null}.
     * @throws IllegalStateException
     *             if the engine does not have all the needed settings (e.g.
     *             client/server mode not set).
     */
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        return unwrap(src, new ByteBuffer[] { dst }, 0, 1);
    }

    /**
     * Decodes the incoming network data buffer into the application data
     * buffers. If a handshake has not been started yet, it will automatically
     * be started.
     *
     * @param src
     *            the buffer with incoming network data
     * @param dsts
     *            the array of destination buffers for incoming application
     *            data.
     * @return the result object of this operation.
     * @throws SSLException
     *             if a problem occurred while processing the data.
     * @throws java.nio.ReadOnlyBufferException
     *             if one of the destination buffers is read-only.
     * @throws IllegalArgumentException
     *             if {@code src} or {@code dsts} is {@code null}.
     * @throws IllegalStateException
     *             if the engine does not have all the needed settings (e.g.
     *             client/server mode not set).
     */
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts) throws SSLException {
        if (dsts == null) {
            throw new IllegalArgumentException("Byte buffer array dsts is null");
        }
        return unwrap(src, dsts, 0, dsts.length);
    }

    /**
     * Encodes the outgoing application data buffers into the network data
     * buffer. If a handshake has not been started yet, it will automatically be
     * started.
     *
     * @param srcs
     *            the array of source buffers of outgoing application data.
     * @param dst
     *            the destination buffer for network data.
     * @return the result object of this operation.
     * @throws SSLException
     *             if a problem occurred while processing the data.
     * @throws java.nio.ReadOnlyBufferException
     *             if the destination buffer is readonly.
     * @throws IllegalArgumentException
     *             if {@code srcs} or {@code dst} is {@code null}.
     * @throws IllegalStateException
     *             if the engine does not have all the needed settings (e.g.
     *             client/server mode not set).
     */
    public SSLEngineResult wrap(ByteBuffer[] srcs, ByteBuffer dst) throws SSLException {
        if (srcs == null) {
            throw new IllegalArgumentException("Byte buffer array srcs is null");
        }
        return wrap(srcs, 0, srcs.length, dst);
    }

    /**
     * Encodes the outgoing application data buffer into the network data
     * buffer. If a handshake has not been started yet, it will automatically be
     * started.
     *
     * @param src
     *            the source buffers of outgoing application data.
     * @param dst
     *            the destination buffer for network data.
     * @return the result object of this operation.
     * @throws SSLException
     *             if a problem occurred while processing the data.
     * @throws java.nio.ReadOnlyBufferException
     *             if the destination buffer is readonly.
     * @throws IllegalArgumentException
     *             if {@code src} or {@code dst} is {@code null}.
     * @throws IllegalStateException
     *             if the engine does not have all the needed settings (e.g.
     *             client/server mode not set).
     */
    public SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        return wrap(new ByteBuffer[] { src }, 0, 1, dst);
    }

    /**
     * Returns a new SSLParameters based on this SSLSocket's current
     * cipher suites, protocols, and client authentication settings.
     *
     * @since 1.6
     */
    public SSLParameters getSSLParameters() {
        SSLParameters p = new SSLParameters();
        p.setCipherSuites(getEnabledCipherSuites());
        p.setProtocols(getEnabledProtocols());
        p.setNeedClientAuth(getNeedClientAuth());
        p.setWantClientAuth(getWantClientAuth());
        return p;
    }

    /**
     * Sets various SSL handshake parameters based on the SSLParameter
     * argument. Specifically, sets the SSLEngine's enabled cipher
     * suites if the parameter's cipher suites are non-null. Similarly
     * sets the enabled protocols. If the parameters specify the want
     * or need for client authentication, those requirements are set
     * on the SSLEngine, otherwise both are set to false.
     * @since 1.6
     */
    public void setSSLParameters(SSLParameters p) {
        String[] cipherSuites = p.getCipherSuites();
        if (cipherSuites != null) {
            setEnabledCipherSuites(cipherSuites);
        }
        String[] protocols = p.getProtocols();
        if (protocols != null) {
            setEnabledProtocols(protocols);
        }
        if (p.getNeedClientAuth()) {
            setNeedClientAuth(true);
        } else if (p.getWantClientAuth()) {
            setWantClientAuth(true);
        } else {
            setWantClientAuth(false);
        }
    }
}
