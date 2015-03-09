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
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * The extension of {@code ServerSocket} which provides secure server sockets
 * based on protocols like SSL, TLS, or others.
 */
public abstract class SSLServerSocket extends ServerSocket {

    /**
     * Only to be used by subclasses.
     * <p>
     * Creates a TCP server socket with the default authentication context.
     *
     * @throws IOException
     *             if creating the socket fails.
     */
    protected SSLServerSocket() throws IOException {
    }

    /**
     * Only to be used by subclasses.
     * <p>
     * Creates a TCP server socket on the specified port with the default
     * authentication context. The connection's default backlog size is 50
     * connections.
     * @param port
     *            the port to listen on.
     * @throws IOException
     *             if creating the socket fails.
     */
    protected SSLServerSocket(int port) throws IOException {
        super(port);
    }

    /**
     * Only to be used by subclasses.
     * <p>
     * Creates a TCP server socket on the specified port using the specified
     * backlog and the default authentication context.
     *
     * @param port
     *            the port to listen on.
     * @param backlog
     *            the number of pending connections to queue.
     * @throws IOException
     *             if creating the socket fails.
     */
    protected SSLServerSocket(int port, int backlog) throws IOException {
        super(port, backlog);
    }

    /**
     * Only to be used by subclasses.
     * <p>
     * Creates a TCP server socket on the specified port, using the specified
     * backlog, listening on the specified interface, and using the default
     * authentication context.
     *
     * @param port
     *            the port the listen on.
     * @param backlog
     *            the number of pending connections to queue.
     * @param address
     *            the address of the interface to accept connections on.
     * @throws IOException
     *             if creating the socket fails.
     */
    protected SSLServerSocket(int port, int backlog, InetAddress address) throws IOException {
        super(port, backlog, address);
    }

    /**
     * Returns the names of the enabled cipher suites to be used for new
     * connections.
     *
     * @return the names of the enabled cipher suites to be used for new
     *         connections.
     */
    public abstract String[] getEnabledCipherSuites();

    /**
     * Sets the names of the cipher suites to be enabled for new connections.
     * Only cipher suites returned by {@link #getSupportedCipherSuites()} are
     * allowed.
     *
     * @param suites
     *            the names of the to be enabled cipher suites.
     * @throws IllegalArgumentException
     *             if one of the cipher suite names is not supported.
     */
    public abstract void setEnabledCipherSuites(String[] suites);

    /**
     * Returns the names of the supported cipher suites.
     *
     * @return the names of the supported cipher suites.
     */
    public abstract String[] getSupportedCipherSuites();

    /**
     * Returns the names of the supported protocols.
     *
     * @return the names of the supported protocols.
     */
    public abstract String[] getSupportedProtocols();

    /**
     * Returns the names of the enabled protocols to be used for new
     * connections.
     *
     * @return the names of the enabled protocols to be used for new
     *         connections.
     */
    public abstract String[] getEnabledProtocols();

    /**
     * Sets the names of the protocols to be enabled for new connections. Only
     * protocols returned by {@link #getSupportedProtocols()} are allowed.
     *
     * @param protocols
     *            the names of the to be enabled protocols.
     * @throws IllegalArgumentException
     *             if one of the protocols is not supported.
     */
    public abstract void setEnabledProtocols(String[] protocols);

    /**
     * Sets whether server-mode connections will be configured to require client
     * authentication. The client authentication is one of the following:
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
     * Returns whether server-mode connections will be configured to require
     * client authentication.
     *
     * @return {@code true} if client authentication is required, {@code false}
     *         if no client authentication is needed.
     */
    public abstract boolean getNeedClientAuth();

    /**
     * Sets whether server-mode connections will be configured to request client
     * authentication. The client authentication is one of the following:
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
     * Returns whether server-mode connections will be configured to request
     * client authentication.
     *
     * @return {@code true} is client authentication will be requested,
     *         {@code false} if no client authentication is needed.
     */
    public abstract boolean getWantClientAuth();

    /**
     * Sets whether new connections should act in client mode when handshaking.
     *
     * @param mode
     *            {@code true} if new connections should act in client mode,
     *            {@code false} if not.
     */
    public abstract void setUseClientMode(boolean mode);

    /**
     * Returns whether new connection will act in client mode when handshaking.
     *
     * @return {@code true} if new connections will act in client mode when
     *         handshaking, {@code false} if not.
     */
    public abstract boolean getUseClientMode();

    /**
     * Sets whether new SSL sessions may be established for new connections.
     *
     * @param flag
     *            {@code true} if new SSL sessions may be established,
     *            {@code false} if existing SSL sessions must be reused.
     */
    public abstract void setEnableSessionCreation(boolean flag);

    /**
     * Returns whether new SSL sessions may be established for new connections.
     *
     * @return {@code true} if new SSL sessions may be established,
     *         {@code false} if existing SSL sessions must be reused.
     */
    public abstract boolean getEnableSessionCreation();
}
