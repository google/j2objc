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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import org.apache.harmony.security.fortress.Engine;


/**
 * The public API for secure socket protocol implementations. It acts as factory
 * for {@code SSLSocketFactory}'s and {@code SSLEngine}s.
 */
public class SSLContext {
    // StoreSSLContext service name
    private static final String SERVICE = "SSLContext";

    // Used to access common engine functionality
    private static final Engine ENGINE = new Engine(SERVICE);

    /**
     * Default SSLContext that can be replaced with SSLContext.setDefault()
     */
    private static SSLContext DEFAULT;

    /**
     * Returns the default SSLContext.
     *
     * The default SSL context can be set with {@link #setDefault}. If
     * not, one will be created with {@code
     * SSLContext.getInstance("Default")}, which will already be
     * initialized.
     *
     * @throws NoSuchAlgorithmException if there is a problem creating
     * the default instance.
     * @since 1.6
     */
    public static SSLContext getDefault() throws NoSuchAlgorithmException {
        synchronized (ENGINE) {
            if (DEFAULT == null) {
                DEFAULT = SSLContext.getInstance("Default");
            }
            return DEFAULT;
        }
    }

    /**
     * Sets the default SSLContext instance as returned by {@link
     * #getDefault()} to a non-null initialized value.
     *
     * @throws NullPointerException on a null argument
     * @since 1.6
     */
    public static void setDefault(SSLContext sslContext) {
        if (sslContext == null) {
            throw new NullPointerException("sslContext == null");
        }
        synchronized (ENGINE) {
            DEFAULT = sslContext;
        }
    }

    /**
     * Creates a new {@code SSLContext} instance for the specified protocol.
     *
     * <p>The following protocols are supported:
     * <table>
     *     <thead>
     *         <tr>
     *             <th>Protocol</th>
     *             <th>API Levels</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>Default</td>
     *             <td>9+</td>
     *         </tr>
     *         <tr>
     *             <td>SSL</td>
     *             <td>9+</td>
     *         </tr>
     *         <tr>
     *             <td>SSLv3</td>
     *             <td>9+</td>
     *         </tr>
     *         <tr>
     *             <td>TLS</td>
     *             <td>1+</td>
     *         </tr>
     *         <tr>
     *             <td>TLSv1</td>
     *             <td>1+</td>
     *         </tr>
     *         <tr>
     *             <td>TLSv1.1</td>
     *             <td>16+</td>
     *         </tr>
     *         <tr>
     *             <td>TLSv1.2</td>
     *             <td>16+</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * @param protocol
     *            the requested protocol to create a context for.
     * @return the created {@code SSLContext} instance.
     * @throws NoSuchAlgorithmException
     *             if no installed provider can provide the requested protocol
     * @throws NullPointerException
     *             if {@code protocol} is {@code null} (instead of
     *             NoSuchAlgorithmException as in 1.4 release)
     */
    public static SSLContext getInstance(String protocol) throws NoSuchAlgorithmException {
        if (protocol == null) {
            throw new NullPointerException("protocol == null");
        }
        Engine.SpiAndProvider sap = ENGINE.getInstance(protocol, null);
        return new SSLContext((SSLContextSpi) sap.spi, sap.provider, protocol);
    }

    /**
     * Creates a new {@code SSLContext} instance for the specified protocol from
     * the specified provider.
     *
     * <p>The following combinations are supported:
     * <table>
     *     <thead>
     *         <tr>
     *             <th>Protocol</th>
     *             <th>Provider</th>
     *             <th>API Levels</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>Default</td>
     *             <td>AndroidOpenSSL</td>
     *             <td>9+</td>
     *         </tr>
     *         <tr>
     *             <td>SSL</td>
     *             <td>AndroidOpenSSL</td>
     *             <td>9+</td>
     *         </tr>
     *         <tr>
     *             <td>SSL</td>
     *             <td>HarmonyJSSE</td>
     *             <td>9-19</td>
     *         </tr>
     *         <tr>
     *             <td>SSLv3</td>
     *             <td>AndroidOpenSSL</td>
     *             <td>9+</td>
     *         </tr>
     *         <tr>
     *             <td>SSLv3</td>
     *             <td>HarmonyJSSE</td>
     *             <td>9-19</td>
     *         </tr>
     *         <tr>
     *             <td>TLS</td>
     *             <td>AndroidOpenSSL</td>
     *             <td>9+</td>
     *         </tr>
     *         <tr>
     *             <td>TLS</td>
     *             <td>HarmonyJSSE</td>
     *             <td>1-19</td>
     *         </tr>
     *         <tr>
     *             <td>TLSv1</td>
     *             <td>AndroidOpenSSL</td>
     *             <td>9+</td>
     *         </tr>
     *         <tr>
     *             <td>TLSv1</td>
     *             <td>HarmonyJSSE</td>
     *             <td>1-19</td>
     *         </tr>
     *         <tr>
     *             <td>TLSv1.1</td>
     *             <td>AndroidOpenSSL</td>
     *             <td>16+</td>
     *         </tr>
     *         <tr>
     *             <td>TLSv1.2</td>
     *             <td>AndroidOpenSSL</td>
     *             <td>16+</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * <p><strong>NOTE:</strong> The best practice is to rely on platform
     * defaults rather than explicitly specify a provider.
     * {@link #getDefault()} and {@link #getInstance(String)} are normally
     * preferred over this method.
     *
     * @param protocol
     *            the requested protocol to create a context for.
     * @param provider
     *            the name of the provider that provides the requested protocol.
     * @return an {@code SSLContext} for the requested protocol.
     * @throws NoSuchAlgorithmException
     *             if the specified provider cannot provider the requested
     *             protocol.
     * @throws NoSuchProviderException
     *             if the specified provider does not exits.
     * @throws NullPointerException
     *             if {@code protocol} is {@code null} (instead of
     *             NoSuchAlgorithmException as in 1.4 release)
     */
    public static SSLContext getInstance(String protocol, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (provider.length() == 0) {
            throw new IllegalArgumentException("Provider is empty");
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(protocol, impProvider);
    }

    /**
     * Creates a new {@code SSLContext} instance for the specified protocol from
     * the specified provider.
     *
     * @param protocol
     *            the requested protocol to create a context for
     * @param provider
     *            the provider that provides the requested protocol.
     * @return an {@code SSLContext} for the requested protocol.
     * @throws NoSuchAlgorithmException
     *             if the specified provider cannot provide the requested
     *             protocol.
     * @throws NullPointerException
     *             if {@code protocol} is {@code null} (instead of
     *             NoSuchAlgorithmException as in 1.4 release)
     */
    public static SSLContext getInstance(String protocol, Provider provider)
            throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("provider is null");
        }
        if (protocol == null) {
            throw new NullPointerException("protocol == null");
        }
        Object spi = ENGINE.getInstance(protocol, provider, null);
        return new SSLContext((SSLContextSpi) spi, provider, protocol);
    }

    private final Provider provider;

    private final SSLContextSpi spiImpl;

    private final String protocol;

    /**
     * Creates a new {@code SSLContext}.
     *
     * @param contextSpi
     *            the implementation delegate.
     * @param provider
     *            the provider.
     * @param protocol
     *            the protocol name.
     */
    protected SSLContext(SSLContextSpi contextSpi, Provider provider, String protocol) {
        this.provider = provider;
        this.protocol = protocol;
        this.spiImpl = contextSpi;
    }

    /**
     * Returns the name of the secure socket protocol of this instance.
     *
     * @return the name of the secure socket protocol of this instance.
     */
    public final String getProtocol() {
        return protocol;
    }

    /**
     * Returns the provider of this {@code SSLContext} instance.
     *
     * @return the provider of this {@code SSLContext} instance.
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Initializes this {@code SSLContext} instance. Three aspects of the context can be configured
     * during initialization:
     * <ul>
     *   <li>Providers of key material for key exchange and peer authentication
     *       ({@link KeyManager} instances),</li>
     *   <li>Providers of trust decisions about peers ({@link TrustManager} instances),
     *   </li>
     *   <li>Provider of randomness ({@link SecureRandom} instance).</li>
     * </ul>
     *
     * <p>For each type of {@code KeyManager} or {@code TrustManager} used by this context, only the
     * first matching instance from {@code km} or {@code tm} will be used. For example, only the
     * first instance of {@link X509TrustManager} from {@code tm} will be used.
     *
     * <p>For any parameter set to {@code null} defaults will be used. In that case, the installed
     * security providers will be searched for the highest priority implementation of the required
     * primitives. For {@code km} and {@code tm}, the highest priority implementation
     * of {@link KeyManagerFactory} and {@link TrustManagerFactory} will be used to obtain the
     * required types of {@code KeyManager} and {@code TrustManager}. For {@code sr}, the default
     * {@code SecureRandom} implementation will be used.
     *
     * @param km
     *            the key sources or {@code null} for default.
     * @param tm
     *            the trust decision sources or {@code null} for default.
     * @param sr
     *            the randomness source or {@code null} for default.
     * @throws KeyManagementException
     *             if initializing this instance fails.
     */
    public final void init(KeyManager[] km, TrustManager[] tm, SecureRandom sr)
            throws KeyManagementException {
        spiImpl.engineInit(km, tm, sr);
    }

    /**
     * Returns a socket factory for this instance.
     *
     * @return a socket factory for this instance.
     */
    public final SSLSocketFactory getSocketFactory() {
        return spiImpl.engineGetSocketFactory();
    }

    /**
     * Returns a server socket factory for this instance.
     *
     * @return a server socket factory for this instance.
     */
    public final SSLServerSocketFactory getServerSocketFactory() {
        return spiImpl.engineGetServerSocketFactory();
    }

    /**
     * Creates an {@code SSLEngine} instance from this context.
     *
     * @return an {@code SSLEngine} instance from this context.
     * @throws UnsupportedOperationException
     *             if the provider does not support the operation.
     */
    public final SSLEngine createSSLEngine() {
        return spiImpl.engineCreateSSLEngine();
    }

    /**
     * Creates an {@code SSLEngine} instance from this context with the
     * specified hostname and port.
     *
     * @param peerHost
     *            the name of the host
     * @param peerPort
     *            the port
     * @return an {@code SSLEngine} instance from this context.
     * @throws UnsupportedOperationException
     *             if the provider does not support the operation.
     */
    public final SSLEngine createSSLEngine(String peerHost, int peerPort) {
        return spiImpl.engineCreateSSLEngine(peerHost, peerPort);
    }

    /**
     * Returns the SSL session context that encapsulates the set of SSL sessions
     * that can be used for handshake of server-side SSL sockets.
     *
     * @return the SSL server session context for this context or {@code null}
     *         if the underlying provider does not provide an implementation of
     *         the {@code SSLSessionContext} interface.
     */
    public final SSLSessionContext getServerSessionContext() {
        return spiImpl.engineGetServerSessionContext();
    }

    /**
     * Returns the SSL session context that encapsulates the set of SSL sessions
     * that can be used for handshake of client-side SSL sockets.
     *
     * @return the SSL client session context for this context or {@code null}
     *         if the underlying provider does not provide an implementation of
     *         the {@code SSLSessionContext} interface.
     */
    public final SSLSessionContext getClientSessionContext() {
        return spiImpl.engineGetClientSessionContext();
    }

    /**
     * Returns the default SSL handshake parameters for SSLSockets
     * created by this SSLContext.
     *
     * @throws UnsupportedOperationException
     * @since 1.6
     */
    public final SSLParameters getDefaultSSLParameters() {
        return spiImpl.engineGetDefaultSSLParameters();
    }

    /**
     * Returns SSL handshake parameters for SSLSockets that includes
     * all supported cipher suites and protocols.
     *
     * @throws UnsupportedOperationException
     * @since 1.6
     */
    public final SSLParameters getSupportedSSLParameters() {
        return spiImpl.engineGetSupportedSSLParameters();
    }
}
