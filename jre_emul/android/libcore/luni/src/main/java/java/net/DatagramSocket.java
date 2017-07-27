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
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.DatagramChannel;

import libcore.io.ErrnoException;
import libcore.io.NetworkBridge;
import libcore.io.NetworkOs;
import static libcore.io.OsConstants.*;

/**
 * This class implements a UDP socket for sending and receiving {@code
 * DatagramPacket}. A {@code DatagramSocket} object can be used for both
 * endpoints of a connection for a packet delivery service.
 *
 * @see DatagramPacket
 * @see DatagramSocketImplFactory
 */
public class DatagramSocket implements Closeable {

    DatagramSocketImpl impl;

    InetAddress address;

    int port = -1;

    static DatagramSocketImplFactory factory;

    boolean isBound = false;

    private boolean isConnected = false;

    private SocketException pendingConnectException;

    private boolean isClosed = false;

    private Object lock = new Object();

    /**
     * Constructs a UDP datagram socket which is bound to any available port on
     * the localhost.
     *
     * @throws SocketException
     *             if an error occurs while creating or binding the socket.
     */
    public DatagramSocket() throws SocketException {
        this(0);
    }

    /**
     * Constructs a UDP datagram socket which is bound to the specific port
     * {@code aPort} on the localhost. Valid values for {@code aPort} are
     * between 0 and 65535 inclusive.
     *
     * @param aPort
     *            the port to bind on the localhost.
     * @throws SocketException
     *             if an error occurs while creating or binding the socket.
     */
    public DatagramSocket(int aPort) throws SocketException {
        checkPort(aPort);
        createSocket(aPort, Inet4Address.ANY);
    }

    /**
     * Constructs a UDP datagram socket which is bound to the specific local
     * address {@code addr} on port {@code aPort}. Valid values for {@code
     * aPort} are between 0 and 65535 inclusive.
     *
     * @param aPort
     *            the port to bind on the localhost.
     * @param addr
     *            the address to bind on the localhost.
     * @throws SocketException
     *             if an error occurs while creating or binding the socket.
     */
    public DatagramSocket(int aPort, InetAddress addr) throws SocketException {
        checkPort(aPort);
        createSocket(aPort, (addr == null) ? Inet4Address.ANY : addr);
    }

    private void checkPort(int aPort) {
        if (aPort < 0 || aPort > 65535) {
            throw new IllegalArgumentException("Port out of range: " + aPort);
        }
    }

    /**
     * Closes this UDP datagram socket and all possibly associated channels.
     */
    // In the documentation jdk1.1.7a/guide/net/miscNet.html, this method is
    // noted as not being synchronized.
    public void close() {
        isClosed = true;
        impl.close();
    }

    /**
     * Sets the DatagramSocket and its related DatagramSocketImpl state as if a successful close()
     * took place, without actually performing an OS close().
     *
     * @hide used in java.nio
     */
    public void onClose() {
        isClosed = true;
        impl.onClose();
    }

    /**
     * Disconnects this UDP datagram socket from the remote host. This method
     * called on an unconnected socket does nothing.
     */
    public void disconnect() {
        if (isClosed() || !isConnected()) {
            return;
        }
        impl.disconnect();
        address = null;
        port = -1;
        isConnected = false;
    }

    /**
     * Sets the DatagramSocket and its related DatagramSocketImpl state as if a successful
     * disconnect() took place, without actually performing a disconnect().
     *
     * @hide used in java.nio
     */
    public void onDisconnect() {
        address = null;
        port = -1;
        isConnected = false;
        impl.onDisconnect();
    }

    synchronized void createSocket(int aPort, InetAddress addr) throws SocketException {
        impl = factory != null ? factory.createDatagramSocketImpl()
                : new PlainDatagramSocketImpl();
        impl.create();
        try {
            impl.bind(aPort, addr);
            isBound = true;
        } catch (SocketException e) {
            close();
            throw e;
        }
    }

    /**
     * Gets the {@code InetAddress} instance representing the remote address to
     * which this UDP datagram socket is connected.
     *
     * @return the remote address this socket is connected to or {@code null} if
     *         this socket is not connected.
     */
    public InetAddress getInetAddress() {
        return address;
    }

    /**
     * Returns the local address to which this socket is bound, a wildcard address if this
     * socket is not yet bound, or {@code null} if this socket is closed.
     */
    public InetAddress getLocalAddress() {
        try {
            return NetworkBridge.getSocketLocalAddress(impl.fd);
        } catch (SocketException ex) {
            return null;
        }
    }

    /**
     * Gets the local port which this socket is bound to.
     *
     * @return the local port of this socket or {@code -1} if this socket is
     *         closed and {@code 0} if it is unbound.
     */
    public int getLocalPort() {
        if (isClosed()) {
            return -1;
        }
        if (!isBound()) {
            return 0;
        }
        return impl.getLocalPort();
    }

    /**
     * Gets the remote port which this socket is connected to.
     *
     * @return the remote port of this socket. The return value {@code -1}
     *         indicates that this socket is not connected.
     */
    public int getPort() {
        return port;
    }

    /**
     * Indicates whether this socket is multicast or not.
     *
     * @return the return value is always {@code false}.
     */
    boolean isMulticastSocket() {
        return false;
    }

    /**
     * Returns this socket's {@link SocketOptions#SO_RCVBUF receive buffer size}.
     */
    public synchronized int getReceiveBufferSize() throws SocketException {
        checkOpen();
        return ((Integer) impl.getOption(SocketOptions.SO_RCVBUF)).intValue();
    }

    /**
     * Returns this socket's {@link SocketOptions#SO_SNDBUF send buffer size}.
     */
    public synchronized int getSendBufferSize() throws SocketException {
        checkOpen();
        return ((Integer) impl.getOption(SocketOptions.SO_SNDBUF)).intValue();
    }

    /**
     * Gets the socket {@link SocketOptions#SO_TIMEOUT receive timeout}.
     *
     * @throws SocketException
     *                if an error occurs while getting the option value.
     */
    public synchronized int getSoTimeout() throws SocketException {
        checkOpen();
        return ((Integer) impl.getOption(SocketOptions.SO_TIMEOUT)).intValue();
    }

    /**
     * Receives a packet from this socket and stores it in the argument {@code
     * pack}. All fields of {@code pack} must be set according to the data
     * received. If the received data is longer than the packet buffer size it
     * is truncated. This method blocks until a packet is received or a timeout
     * has expired.
     *
     * @param pack
     *            the {@code DatagramPacket} to store the received data.
     * @throws IOException
     *                if an error occurs while receiving the packet.
     */
    public synchronized void receive(DatagramPacket pack) throws IOException {
        checkOpen();
        ensureBound();
        if (pack == null) {
            throw new NullPointerException("pack == null");
        }
        if (pendingConnectException != null) {
            throw new SocketException("Pending connect failure", pendingConnectException);
        }
        pack.resetLengthForReceive();
        impl.receive(pack);
    }

    /**
     * Sends a packet over this socket.
     *
     * @param pack
     *            the {@code DatagramPacket} which has to be sent.
     * @throws IOException
     *                if an error occurs while sending the packet.
     */
    public void send(DatagramPacket pack) throws IOException {
        checkOpen();
        ensureBound();

        InetAddress packAddr = pack.getAddress();
        if (address != null) { // The socket is connected
            if (packAddr != null) {
                if (!address.equals(packAddr) || port != pack.getPort()) {
                    throw new IllegalArgumentException("Packet address mismatch with connected address");
                }
            } else {
                pack.setAddress(address);
                pack.setPort(port);
            }
        } else {
            // not connected so the target address is not allowed to be null
            if (packAddr == null) {
                throw new NullPointerException("Destination address is null");
            }
        }
        impl.send(pack);
    }

    /**
     * Sets the network interface used by this socket.  Any packets sent
     * via this socket are transmitted via the specified interface.  Any
     * packets received by this socket will come from the specified
     * interface.  Broadcast datagrams received on this interface will
     * be processed by this socket. This corresponds to Linux's SO_BINDTODEVICE.
     *
     * @hide used by GoogleTV for DHCP
     */
    public void setNetworkInterface(NetworkInterface netInterface) throws SocketException {
        if (netInterface == null) {
            throw new NullPointerException("netInterface == null");
        }
        try {
            NetworkOs.setsockoptIfreq(impl.fd, SOL_SOCKET, SO_BINDTODEVICE, netInterface.getName());
        } catch (ErrnoException errnoException) {
            throw new SocketException(errnoException.getMessage(), errnoException);
        }
    }

    /**
     * Sets this socket's {@link SocketOptions#SO_SNDBUF send buffer size}.
     */
    public synchronized void setSendBufferSize(int size) throws SocketException {
        if (size < 1) {
            throw new IllegalArgumentException("size < 1");
        }
        checkOpen();
        impl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(size));
    }

    /**
     * Sets this socket's {@link SocketOptions#SO_SNDBUF receive buffer size}.
     */
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        if (size < 1) {
            throw new IllegalArgumentException("size < 1");
        }
        checkOpen();
        impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
    }

    /**
     * Sets the {@link SocketOptions#SO_TIMEOUT read timeout} in milliseconds for this socket.
     * This receive timeout defines the period the socket will block waiting to
     * receive data before throwing an {@code InterruptedIOException}. The value
     * {@code 0} (default) is used to set an infinite timeout. To have effect
     * this option must be set before the blocking method was called.
     *
     * @param timeout the timeout in milliseconds or 0 for no timeout.
     * @throws SocketException
     *                if an error occurs while setting the option.
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        }
        checkOpen();
        impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout));
    }

    /**
     * Sets the socket implementation factory. This may only be invoked once
     * over the lifetime of the application. This factory is used to create
     * a new datagram socket implementation.
     *
     * @param fac
     *            the socket factory to use.
     * @throws IOException
     *                if the factory has already been set.
     * @see DatagramSocketImplFactory
     */
    public static synchronized void setDatagramSocketImplFactory(DatagramSocketImplFactory fac)
            throws IOException {
        if (factory != null) {
            throw new SocketException("Factory already set");
        }
        factory = fac;
    }

    /**
     * Constructs a new {@code DatagramSocket} using the specific datagram
     * socket implementation {@code socketImpl}. The created {@code
     * DatagramSocket} will not be bound.
     *
     * @param socketImpl
     *            the DatagramSocketImpl to use.
     */
    protected DatagramSocket(DatagramSocketImpl socketImpl) {
        if (socketImpl == null) {
            throw new NullPointerException("socketImpl == null");
        }
        impl = socketImpl;
    }

    /**
     * Constructs a new {@code DatagramSocket} bound to the host/port specified
     * by the {@code SocketAddress} {@code localAddr} or an unbound {@code
     * DatagramSocket} if the {@code SocketAddress} is {@code null}.
     *
     * @param localAddr
     *            the local machine address and port to bind to.
     * @throws IllegalArgumentException
     *             if the SocketAddress is not supported
     * @throws SocketException
     *             if a problem occurs creating or binding the socket.
     */
    public DatagramSocket(SocketAddress localAddr) throws SocketException {
        if (localAddr != null) {
            if (!(localAddr instanceof InetSocketAddress)) {
                throw new IllegalArgumentException("Local address not an InetSocketAddress: " +
                        localAddr.getClass());
            }
            checkPort(((InetSocketAddress) localAddr).getPort());
        }
        impl = factory != null ? factory.createDatagramSocketImpl()
                : new PlainDatagramSocketImpl();
        impl.create();
        if (localAddr != null) {
            try {
                bind(localAddr);
            } catch (SocketException e) {
                close();
                throw e;
            }
        }
        // SocketOptions.SO_BROADCAST is set by default for DatagramSocket
        setBroadcast(true);
    }

    void checkOpen() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
    }

    private void ensureBound() throws SocketException {
        if (!isBound()) {
            impl.bind(0, Inet4Address.ANY);
            isBound = true;
        }
    }

    /**
     * Binds this socket to the local address and port specified by {@code
     * localAddr}. If this value is {@code null} any free port on a valid local
     * address is used.
     *
     * @param localAddr
     *            the local machine address and port to bind on.
     * @throws IllegalArgumentException
     *             if the SocketAddress is not supported
     * @throws SocketException
     *             if the socket is already bound or a problem occurs during
     *             binding.
     */
    public void bind(SocketAddress localAddr) throws SocketException {
        checkOpen();
        int localPort;
        InetAddress addr;
        if (localAddr == null) {
            localPort = 0;
            addr = Inet4Address.ANY;
        } else {
            if (!(localAddr instanceof InetSocketAddress)) {
                throw new IllegalArgumentException("Local address not an InetSocketAddress: " +
                        localAddr.getClass());
            }
            InetSocketAddress inetAddr = (InetSocketAddress) localAddr;
            addr = inetAddr.getAddress();
            if (addr == null) {
                throw new SocketException("Host is unresolved: " + inetAddr.getHostName());
            }
            localPort = inetAddr.getPort();
            checkPort(localPort);
        }
        impl.bind(localPort, addr);
        isBound = true;
    }

    /**
     * Sets the DatagramSocket and its related DatagramSocketImpl state as if a successful bind()
     * took place, without actually performing an OS bind().
     *
     * @hide used in java.nio
     */
    public void onBind(InetAddress localAddress, int localPort) {
        isBound = true;
        impl.onBind(localAddress, localPort);
    }

    /**
     * Connects this datagram socket to the address and port specified by {@code peer}.
     * Future calls to {@link #send} will use this as the default target, and {@link #receive}
     * will only accept packets from this source.
     *
     * @throws SocketException if an error occurs.
     */
    public void connect(SocketAddress peer) throws SocketException {
        if (peer == null) {
            throw new IllegalArgumentException("peer == null");
        }

        if (!(peer instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("peer not an InetSocketAddress: " + peer.getClass());
        }

        InetSocketAddress isa = (InetSocketAddress) peer;
        if (isa.getAddress() == null) {
            throw new SocketException("Host is unresolved: " + isa.getHostName());
        }

        synchronized (lock) {
            checkOpen();
            ensureBound();

            this.address = isa.getAddress();
            this.port = isa.getPort();
            this.isConnected = true;

            impl.connect(address, port);
        }
    }

    /**
     * Sets the DatagramSocket and its related DatagramSocketImpl state as if a successful connect()
     * took place, without actually performing an OS connect().
     *
     * @hide used in java.nio
     */
    public void onConnect(InetAddress remoteAddress, int remotePort) {
        isConnected = true;
        this.address = remoteAddress;
        this.port = remotePort;
        impl.onConnect(remoteAddress, remotePort);
    }

    /**
     * Connects this datagram socket to the specific {@code address} and {@code port}.
     * Future calls to {@link #send} will use this as the default target, and {@link #receive}
     * will only accept packets from this source.
     *
     * <p>Beware: because it can't throw, this method silently ignores failures.
     * Use {@link #connect(SocketAddress)} instead.
     */
    public void connect(InetAddress address, int port) {
        if (address == null) {
            throw new IllegalArgumentException("address == null");
        }
        try {
            connect(new InetSocketAddress(address, port));
        } catch (SocketException connectException) {
            // TODO: or just use SneakyThrow? There's a clear API bug here.
            pendingConnectException = connectException;
        }
    }

    /**
     * Returns true if this socket is bound to a local address. See {@link #bind}.
     */
    public boolean isBound() {
        return isBound;
    }

    /**
     * Returns true if this datagram socket is connected to a remote address. See {@link #connect}.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Returns the {@code SocketAddress} this socket is connected to, or null for an unconnected
     * socket.
     */
    public SocketAddress getRemoteSocketAddress() {
        if (!isConnected()) {
            return null;
        }
        return new InetSocketAddress(getInetAddress(), getPort());
    }

    /**
     * Returns the {@code SocketAddress} this socket is bound to, or {@code null} for an unbound or
     * closed socket.
     */
    public SocketAddress getLocalSocketAddress() {
        if (isClosed() || !isBound()) {
            return null;
        }
        return new InetSocketAddress(getLocalAddress(), getLocalPort());
    }

    /**
     * Sets the socket option {@code SocketOptions.SO_REUSEADDR}. This option
     * has to be enabled if more than one UDP socket wants to be bound to the
     * same address. That could be needed for receiving multicast packets.
     * <p>
     * There is an undefined behavior if this option is set after the socket is
     * already bound.
     *
     * @param reuse
     *            the socket option value to enable or disable this option.
     * @throws SocketException
     *             if the socket is closed or the option could not be set.
     */
    public void setReuseAddress(boolean reuse) throws SocketException {
        checkOpen();
        impl.setOption(SocketOptions.SO_REUSEADDR, Boolean.valueOf(reuse));
    }

    /**
     * Gets the state of the socket option {@code SocketOptions.SO_REUSEADDR}.
     *
     * @return {@code true} if the option is enabled, {@code false} otherwise.
     * @throws SocketException
     *             if the socket is closed or the option is invalid.
     */
    public boolean getReuseAddress() throws SocketException {
        checkOpen();
        return ((Boolean) impl.getOption(SocketOptions.SO_REUSEADDR)).booleanValue();
    }

    /**
     * Sets the socket option {@code SocketOptions.SO_BROADCAST}. This option
     * must be enabled to send broadcast messages.
     *
     * @param broadcast
     *            the socket option value to enable or disable this option.
     * @throws SocketException
     *             if the socket is closed or the option could not be set.
     */
    public void setBroadcast(boolean broadcast) throws SocketException {
        checkOpen();
        impl.setOption(SocketOptions.SO_BROADCAST, Boolean.valueOf(broadcast));
    }

    /**
     * Gets the state of the socket option {@code SocketOptions.SO_BROADCAST}.
     *
     * @return {@code true} if the option is enabled, {@code false} otherwise.
     * @throws SocketException
     *             if the socket is closed or the option is invalid.
     */
    public boolean getBroadcast() throws SocketException {
        checkOpen();
        return ((Boolean) impl.getOption(SocketOptions.SO_BROADCAST)).booleanValue();
    }

    /**
     * Sets the {@see SocketOptions#IP_TOS} value for every packet sent by this socket.
     *
     * @throws SocketException
     *             if the socket is closed or the option could not be set.
     */
    public void setTrafficClass(int value) throws SocketException {
        checkOpen();
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Value doesn't fit in an unsigned byte: " + value);
        }
        impl.setOption(SocketOptions.IP_TOS, Integer.valueOf(value));
    }

    /**
     * Returns this socket's {@see SocketOptions#IP_TOS} setting.
     *
     * @throws SocketException
     *             if the socket is closed or the option is invalid.
     */
    public int getTrafficClass() throws SocketException {
        checkOpen();
        return (Integer) impl.getOption(SocketOptions.IP_TOS);
    }

    /**
     * Gets the state of this socket.
     *
     * @return {@code true} if the socket is closed, {@code false} otherwise.
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Returns this socket's {@code DatagramChannel}, if one exists. A channel is
     * available only if this socket wraps a channel. (That is, you can go from a
     * channel to a socket and back again, but you can't go from an arbitrary socket to a channel.)
     * In practice, this means that the socket must have been created by
     * {@link java.nio.channels.DatagramChannel#open}.
     */
    public DatagramChannel getChannel() {
        return null;
    }

    /**
     * @hide internal use only
     */
    public FileDescriptor getFileDescriptor$() {
        return impl.fd;
    }
}
