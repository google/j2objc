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

package java.net;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

import libcore.io.IoBridge;

/**
 * This class represents a server-side socket that waits for incoming client
 * connections. A {@code ServerSocket} handles the requests and sends back an
 * appropriate reply. The actual tasks that a server socket must accomplish are
 * implemented by an internal {@code SocketImpl} instance.
 */
public class ServerSocket implements Closeable {
    /**
     * The RI specifies that where the caller doesn't give an explicit backlog,
     * the default is 50. The OS disagrees, so we need to explicitly call listen(2).
     */
    private static final int DEFAULT_BACKLOG = 50;

    private final SocketImpl impl;

    /**
     * @hide internal use only
     */
    public SocketImpl getImpl$() {
        return impl;
    }

    static SocketImplFactory factory;

    private boolean isBound;

    private boolean isClosed;

    private InetAddress localAddress;

    /**
     * Constructs a new unbound {@code ServerSocket}.
     *
     * @throws IOException if an error occurs while creating the socket.
     */
    public ServerSocket() throws IOException {
        this.impl = factory != null ? factory.createSocketImpl()
                : new PlainServerSocketImpl();
        impl.create(true);
    }

    /**
     * Constructs a new {@code ServerSocket} instance bound to the given {@code port}.
     * The backlog is set to 50. If {@code port == 0}, a port will be assigned by the OS.
     *
     * @throws IOException if an error occurs while creating the socket.
     */
    public ServerSocket(int port) throws IOException {
        this(port, DEFAULT_BACKLOG, Inet4Address.ANY);
    }

    /**
     * Constructs a new {@code ServerSocket} instance bound to the given {@code port}.
     * The backlog is set to {@code backlog}.
     * If {@code port == 0}, a port will be assigned by the OS.
     *
     * @throws IOException if an error occurs while creating the socket.
     */
    public ServerSocket(int port, int backlog) throws IOException {
        this(port, backlog, Inet4Address.ANY);
    }

    /**
     * Constructs a new {@code ServerSocket} instance bound to the given {@code localAddress}
     * and {@code port}. The backlog is set to {@code backlog}.
     * If {@code localAddress == null}, the ANY address is used.
     * If {@code port == 0}, a port will be assigned by the OS.
     *
     * @throws IOException if an error occurs while creating the socket.
     */
    public ServerSocket(int port, int backlog, InetAddress localAddress) throws IOException {
        checkListen(port);
        this.impl = factory != null ? factory.createSocketImpl()
                : new PlainServerSocketImpl();
        InetAddress addr = (localAddress == null) ? Inet4Address.ANY : localAddress;

        synchronized (this) {
            impl.create(true);
            try {
                impl.bind(addr, port);
                readBackBindState();
                impl.listen(backlog > 0 ? backlog : DEFAULT_BACKLOG);
            } catch (IOException e) {
                close();
                throw e;
            }
        }
    }

    /**
     * Read the cached isBound and localAddress state from the underlying OS socket.
     */
    private void readBackBindState() throws SocketException {
        localAddress = IoBridge.getSocketLocalAddress(impl.fd);
        isBound = true;
    }

    /**
     * Waits for an incoming request and blocks until the connection is opened.
     * This method returns a socket object representing the just opened
     * connection.
     *
     * @return the connection representing socket.
     * @throws IOException
     *             if an error occurs while accepting a new connection.
     */
    public Socket accept() throws IOException {
        checkOpen();
        if (!isBound()) {
            throw new SocketException("Socket is not bound");
        }

        Socket aSocket = new Socket();
        try {
            implAccept(aSocket);
        } catch (IOException e) {
            aSocket.close();
            throw e;
        }
        return aSocket;
    }

    private void checkListen(int aPort) {
        if (aPort < 0 || aPort > 65535) {
            throw new IllegalArgumentException("Port out of range: " + aPort);
        }
    }

    /**
     * Closes this server socket and its implementation. Any attempt to connect
     * to this socket thereafter will fail.
     *
     * @throws IOException
     *             if an error occurs while closing this socket.
     */
    public void close() throws IOException {
        isClosed = true;
        impl.close();
    }

    /**
     * Gets the local IP address of this server socket if this socket has ever been bound,
     * {@code null} otherwise. This is useful for multihomed hosts.
     *
     * @return the local address of this server socket.
     */
    public InetAddress getInetAddress() {
        if (!isBound()) {
            return null;
        }
        return localAddress;
    }

    /**
     * Gets the local port of this server socket or {@code -1} if the socket is not bound.
     * If the socket has ever been bound this method will return the local port it was bound to,
     * even after it has been closed.
     *
     * @return the local port this server is listening on.
     */
    public int getLocalPort() {
        if (!isBound()) {
            return -1;
        }
        return impl.getLocalPort();
    }

    /**
     * Gets the socket {@link SocketOptions#SO_TIMEOUT accept timeout}.
     *
     * @throws IOException
     *             if the option cannot be retrieved.
     */
    public synchronized int getSoTimeout() throws IOException {
        checkOpen();
        return ((Integer) impl.getOption(SocketOptions.SO_TIMEOUT)).intValue();
    }

    /**
     * Invokes the server socket implementation to accept a connection on the
     * given socket {@code aSocket}.
     *
     * @param aSocket
     *            the concrete {@code SocketImpl} to accept the connection
     *            request on.
     * @throws IOException
     *             if the connection cannot be accepted.
     */
    protected final void implAccept(Socket aSocket) throws IOException {
        synchronized (this) {
            impl.accept(aSocket.impl);
            aSocket.accepted();
        }
    }

    /**
     * Sets the server socket implementation factory of this instance. This
     * method may only be invoked with sufficient security privilege and only
     * once during the application lifetime.
     *
     * @param aFactory
     *            the streaming socket factory to be used for further socket
     *            instantiations.
     * @throws IOException
     *             if the factory could not be set or is already set.
     */
    public static synchronized void setSocketFactory(SocketImplFactory aFactory) throws IOException {
        if (factory != null) {
            throw new SocketException("Factory already set");
        }
        factory = aFactory;
    }

    /**
     * Sets the {@link SocketOptions#SO_TIMEOUT accept timeout} in milliseconds for this socket.
     * This accept timeout defines the period the socket will block waiting to
     * accept a connection before throwing an {@code InterruptedIOException}. The value
     * {@code 0} (default) is used to set an infinite timeout. To have effect
     * this option must be set before the blocking method was called.
     *
     * @param timeout the timeout in milliseconds or 0 for no timeout.
     * @throws SocketException
     *             if an error occurs while setting the option.
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        checkOpen();
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        }
        impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout));
    }

    /**
     * Returns a textual representation of this server socket including the
     * address, port and the state. The port field is set to {@code 0} if there
     * is no connection to the server socket.
     *
     * @return the textual socket representation.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(64);
        result.append("ServerSocket[");
        if (!isBound()) {
            return result.append("unbound]").toString();
        }
        return result.append("addr=")
                .append(getInetAddress().getHostName()).append("/")
                .append(getInetAddress().getHostAddress()).append(
                        ",port=0,localport=")
                .append(getLocalPort()).append("]")
                .toString();
    }

    /**
     * Binds this server socket to the given local socket address with a maximum
     * backlog of 50 unaccepted connections. If the {@code localAddr} is set to
     * {@code null} the socket will be bound to an available local address on
     * any free port of the system.
     *
     * @param localAddr
     *            the local address and port to bind on.
     * @throws IllegalArgumentException
     *             if the {@code SocketAddress} is not supported.
     * @throws IOException
     *             if the socket is already bound or a problem occurs during
     *             binding.
     */
    public void bind(SocketAddress localAddr) throws IOException {
        bind(localAddr, DEFAULT_BACKLOG);
    }

    /**
     * Binds this server socket to the given local socket address. If the
     * {@code localAddr} is set to {@code null} the socket will be bound to an
     * available local address on any free port of the system.
     *
     * @param localAddr the local machine address and port to bind on.
     * @param backlog the maximum number of unaccepted connections. Passing 0 or
     *     a negative value yields the default backlog of 50.
     * @throws IllegalArgumentException if the {@code SocketAddress} is not
     *     supported.
     * @throws IOException if the socket is already bound or a problem occurs
     *     during binding.
     */
    public void bind(SocketAddress localAddr, int backlog) throws IOException {
        checkOpen();
        if (isBound()) {
            throw new BindException("Socket is already bound");
        }
        InetAddress addr;
        int port;
        if (localAddr == null) {
            addr = Inet4Address.ANY;
            port = 0;
        } else {
            if (!(localAddr instanceof InetSocketAddress)) {
                throw new IllegalArgumentException("Local address not an InetSocketAddress: " +
                        localAddr.getClass());
            }
            InetSocketAddress inetAddr = (InetSocketAddress) localAddr;
            if ((addr = inetAddr.getAddress()) == null) {
                throw new SocketException("Host is unresolved: " + inetAddr.getHostName());
            }
            port = inetAddr.getPort();
        }

        synchronized (this) {
            try {
                impl.bind(addr, port);
                readBackBindState();
                impl.listen(backlog > 0 ? backlog : DEFAULT_BACKLOG);
            } catch (IOException e) {
                close();
                throw e;
            }
        }
    }

    /**
     * Gets the local socket address of this server socket or {@code null} if the socket is unbound.
     * This is useful on multihomed hosts. If the socket has ever been bound this method will return
     * the local address it was bound to, even after it has been closed.
     *
     * @return the local socket address and port this socket is bound to.
     */
    public SocketAddress getLocalSocketAddress() {
        if (!isBound()) {
            return null;
        }
        return new InetSocketAddress(localAddress, getLocalPort());
    }

    /**
     * Returns whether this server socket is bound to a local address and port
     * or not.
     *
     * @return {@code true} if this socket is bound, {@code false} otherwise.
     */
    public boolean isBound() {
        return isBound;
    }

    /**
     * Returns whether this server socket is closed or not.
     *
     * @return {@code true} if this socket is closed, {@code false} otherwise.
     */
    public boolean isClosed() {
        return isClosed;
    }

    private void checkOpen() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
    }

    /**
     * Sets the value for the socket option {@code SocketOptions.SO_REUSEADDR}.
     *
     * @param reuse
     *            the socket option setting.
     * @throws SocketException
     *             if an error occurs while setting the option value.
     */
    public void setReuseAddress(boolean reuse) throws SocketException {
        checkOpen();
        impl.setOption(SocketOptions.SO_REUSEADDR, Boolean.valueOf(reuse));
    }

    /**
     * Gets the value of the socket option {@code SocketOptions.SO_REUSEADDR}.
     *
     * @return {@code true} if the option is enabled, {@code false} otherwise.
     * @throws SocketException
     *             if an error occurs while reading the option value.
     */
    public boolean getReuseAddress() throws SocketException {
        checkOpen();
        return ((Boolean) impl.getOption(SocketOptions.SO_REUSEADDR)).booleanValue();
    }

    /**
     * Sets this socket's {@link SocketOptions#SO_SNDBUF receive buffer size}.
     */
    public void setReceiveBufferSize(int size) throws SocketException {
        checkOpen();
        if (size < 1) {
            throw new IllegalArgumentException("size < 1");
        }
        impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
    }

    /**
     * Returns this socket's {@link SocketOptions#SO_RCVBUF receive buffer size}.
     */
    public int getReceiveBufferSize() throws SocketException {
        checkOpen();
        return ((Integer) impl.getOption(SocketOptions.SO_RCVBUF)).intValue();
    }

    /**
     * Returns this socket's {@code ServerSocketChannel}, if one exists. A channel is
     * available only if this socket wraps a channel. (That is, you can go from a
     * channel to a socket and back again, but you can't go from an arbitrary socket to a channel.)
     * In practice, this means that the socket must have been created by
     * {@link java.nio.channels.ServerSocketChannel#open}.
     */
    public ServerSocketChannel getChannel() {
        return null;
    }

    /**
     * Sets performance preferences for connection time, latency and bandwidth.
     * <p>
     * This method does currently nothing.
     *
     * @param connectionTime
     *            the value representing the importance of a short connecting
     *            time.
     * @param latency
     *            the value representing the importance of low latency.
     * @param bandwidth
     *            the value representing the importance of high bandwidth.
     */
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        // Our socket implementation only provide one protocol: TCP/IP, so
        // we do nothing for this method
    }
}
