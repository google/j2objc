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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.SecureRandom;

/**
 * The <i>Service Provider Interface</i> (SPI) for the {@code SSLContext} class.
 */
public abstract class SSLContextSpi {

    /**
     * Creates a new {@code SSLContextSpi} instance.
     */
    public SSLContextSpi() {
    }

    /**
     * Initializes this {@code SSLContext} instance. All of the arguments are
     * optional, and the security providers will be searched for the required
     * implementations of the needed algorithms.
     *
     * @param km
     *            the key sources or {@code null}.
     * @param tm
     *            the trust decision sources or {@code null}.
     * @param sr
     *            the randomness source or {@code null.}
     * @throws KeyManagementException
     *             if initializing this instance fails.
     */
    protected abstract void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr)
            throws KeyManagementException;

    /**
     * Returns a socket factory for this instance.
     *
     * @return a socket factory for this instance.
     */
    protected abstract SSLSocketFactory engineGetSocketFactory();

    /**
     * Returns a server socket factory for this instance.
     *
     * @return a server socket factory for this instance.
     */
    protected abstract SSLServerSocketFactory engineGetServerSocketFactory();

    /**
     * Creates an {@code SSLEngine} instance from this context with the
     * specified hostname and port.
     *
     * @param host
     *            the name of the host
     * @param port
     *            the port
     * @return an {@code SSLEngine} instance from this context.
     * @throws UnsupportedOperationException
     *             if the provider does not support the operation.
     */
    protected abstract SSLEngine engineCreateSSLEngine(String host, int port);

    /**
     * Creates an {@code SSLEngine} instance from this context.
     *
     * @return an {@code SSLEngine} instance from this context.
     * @throws UnsupportedOperationException
     *             if the provider does not support the operation.
     */
    protected abstract SSLEngine engineCreateSSLEngine();

    /**
     * Returns the SSL session context that encapsulates the set of SSL sessions
     * that can be used for the server side of the SSL handshake.
     *
     * @return the SSL server session context for this context or {@code null}
     *         if the underlying provider does not provide an implementation of
     *         the {@code SSLSessionContext} interface.
     */
    protected abstract SSLSessionContext engineGetServerSessionContext();

    /**
     * Returns the SSL session context that encapsulates the set of SSL sessions
     * that can be used for the client side of the SSL handshake.
     *
     * @return the SSL client session context for this context or {@code null}
     *         if the underlying provider does not provide an implementation of
     *         the {@code SSLSessionContext} interface.
     */
    protected abstract SSLSessionContext engineGetClientSessionContext();


    /**
     * Returns a new SSLParameters instance that includes the default
     * SSL handshake parameters values including cipher suites,
     * protocols, and client authentication.
     *
     * <p>The default implementation returns an SSLParameters with values
     * based an SSLSocket created from this instances SocketFactory.
     *
     * @since 1.6
     */
    protected javax.net.ssl.SSLParameters engineGetDefaultSSLParameters() {
        return createSSLParameters(false);
    }

    /**
     * Returns a new SSLParameters instance that includes all
     * supported cipher suites and protocols.
     *
     * <p>The default implementation returns an SSLParameters with values
     * based an SSLSocket created from this instances SocketFactory.
     *
     * @since 1.6
     */
    protected javax.net.ssl.SSLParameters engineGetSupportedSSLParameters() {
        return createSSLParameters(true);
    }

    private javax.net.ssl.SSLParameters createSSLParameters(boolean supported) {
        try {
            SSLSocket s = (SSLSocket) engineGetSocketFactory().createSocket();
            javax.net.ssl.SSLParameters p = new javax.net.ssl.SSLParameters();
            String[] cipherSuites;
            String[] protocols;
            if (supported) {
                cipherSuites = s.getSupportedCipherSuites();
                protocols = s.getSupportedProtocols();
            } else {
                cipherSuites = s.getEnabledCipherSuites();
                protocols = s.getEnabledProtocols();
            }
            p.setCipherSuites(cipherSuites);
            p.setProtocols(protocols);
            p.setNeedClientAuth(s.getNeedClientAuth());
            p.setWantClientAuth(s.getWantClientAuth());
            return p;
        } catch (IOException e) {
            /*
             * SSLContext.getDefaultSSLParameters specifies to throw
             * UnsupportedOperationException if there is a problem getting the
             * parameters
             */
            throw new UnsupportedOperationException("Could not access supported SSL parameters");
        }
    }
}
