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

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * The abstract superclass for datagram and multicast socket implementations.
 */
public abstract class DatagramSocketImpl implements SocketOptions {

    /**
     * File descriptor that is used to address this socket.
     */
    protected FileDescriptor fd;

    /**
     * The number of the local port to which this socket is bound.
     */
    protected int localPort;

    /**
     * Constructs an unbound datagram socket implementation.
     */
    public DatagramSocketImpl() {
        localPort = -1;
    }

    /**
     * Binds the datagram socket to the given localhost/port. Sockets must be
     * bound prior to attempting to send or receive data.
     *
     * @param port
     *            the port on the localhost to bind.
     * @param addr
     *            the address on the multihomed localhost to bind.
     * @throws SocketException
     *                if an error occurs while binding, for example, if the port
     *                has been already bound.
     */
    protected abstract void bind(int port, InetAddress addr) throws SocketException;

    /**
     * Closes this socket.
     */
    protected abstract void close();

    /**
     * This method allocates the socket descriptor in the underlying operating
     * system.
     *
     * @throws SocketException
     *             if an error occurs while creating the socket.
     */
    protected abstract void create() throws SocketException;

    /**
     * Gets the {@code FileDescriptor} of this datagram socket, which is invalid
     * if the socket is closed or not bound.
     *
     * @return the current file descriptor of this socket.
     */
    protected FileDescriptor getFileDescriptor() {
        return fd;
    }

    /**
     * Returns the local port to which this socket is bound.
     */
    protected int getLocalPort() {
        return localPort;
    }

    /**
     * Gets the time-to-live (TTL) for multicast packets sent on this socket.
     *
     * @return the time-to-live option as a byte value.
     * @throws IOException
     *             if an error occurs while getting the time-to-live option
     *             value.
     * @deprecated Use {@link #getTimeToLive} instead.
     * @see #getTimeToLive()
     */
    @Deprecated
    protected abstract byte getTTL() throws IOException;

    /**
     * Gets the time-to-live (TTL) for multicast packets sent on this socket.
     * The TTL option defines how many routers a packet may be pass before it is
     * discarded.
     *
     * @return the time-to-live option as an integer value.
     * @throws IOException
     *             if an error occurs while getting the time-to-live option
     *             value.
     */
    protected abstract int getTimeToLive() throws IOException;

    /**
     * Adds this socket to the multicast group {@code addr}. A socket must join
     * a group before being able to receive data. Further, a socket may be a
     * member of multiple groups but may join any group only once.
     *
     * @param addr
     *            the multicast group to which this socket has to be joined.
     * @throws IOException
     *             if an error occurs while joining the specified multicast
     *             group.
     */
    protected abstract void join(InetAddress addr) throws IOException;

    /**
     * Adds this socket to the multicast group {@code addr}. A socket must join
     * a group before being able to receive data. Further, a socket may be a
     * member of multiple groups but may join any group only once.
     *
     * @param addr
     *            the multicast group to which this socket has to be joined.
     * @param netInterface
     *            the local network interface which will receive the multicast
     *            datagram packets.
     * @throws IOException
     *             if an error occurs while joining the specified multicast
     *             group.
     */
    protected abstract void joinGroup(SocketAddress addr,
            NetworkInterface netInterface) throws IOException;

    /**
     * Removes this socket from the multicast group {@code addr}.
     *
     * @param addr
     *            the multicast group to be left.
     * @throws IOException
     *             if an error occurs while leaving the group or no multicast
     *             address was assigned.
     */
    protected abstract void leave(InetAddress addr) throws IOException;

    /**
     * Removes this socket from the multicast group {@code addr}.
     *
     * @param addr
     *            the multicast group to be left.
     * @param netInterface
     *            the local network interface on which this socket has to be
     *            removed.
     * @throws IOException
     *             if an error occurs while leaving the group.
     */
    protected abstract void leaveGroup(SocketAddress addr,
            NetworkInterface netInterface) throws IOException;

    /**
     * Peeks at the incoming packet to this socket and returns the address of
     * the {@code sender}. The method will block until a packet is received or
     * timeout expires.
     *
     * @param sender
     *            the origin address of a packet.
     * @return the address of {@code sender} as an integer value.
     * @throws IOException
     *                if an error or a timeout occurs while reading the address.
     */
    protected abstract int peek(InetAddress sender) throws IOException;

    /**
     * Receives data and stores it in the supplied datagram packet {@code pack}.
     * This call will block until either data has been received or, if a timeout
     * is set, the timeout has expired. If the timeout expires an {@code
     * InterruptedIOException} is thrown.
     *
     * @param pack
     *            the datagram packet container to fill in the received data.
     * @throws IOException
     *                if an error or timeout occurs while receiving data.
     */
    protected abstract void receive(DatagramPacket pack) throws IOException;

    /**
     * Sends the given datagram packet {@code pack}. The packet contains the
     * data and the address and port information of the target host as well.
     *
     * @param pack
     *            the datagram packet to be sent.
     * @throws IOException
     *                if an error occurs while sending the packet.
     */
    protected abstract void send(DatagramPacket pack) throws IOException;

    /**
     * Sets the time-to-live (TTL) option for multicast packets sent on this
     * socket.
     *
     * @param ttl
     *            the time-to-live option value. Valid values are 0 &lt; ttl
     *            &lt;= 255.
     * @throws IOException
     *             if an error occurs while setting the option.
     */
    protected abstract void setTimeToLive(int ttl) throws IOException;

    /**
     * Sets the time-to-live (TTL) option for multicast packets sent on this
     * socket.
     *
     * @param ttl
     *            the time-to-live option value. Valid values are 0 &lt; ttl
     *            &lt;= 255.
     * @throws IOException
     *             if an error occurs while setting the option.
     * @deprecated Use {@link #setTimeToLive} instead.
     * @see #setTimeToLive(int)
     */
    @Deprecated
    protected abstract void setTTL(byte ttl) throws IOException;

    /**
     * Connects this socket to the specified remote address and port.
     *
     * @param inetAddr
     *            the address of the target host which has to be connected.
     * @param port
     *            the port on the target host which has to be connected.
     * @throws SocketException
     *                if the datagram socket cannot be connected to the
     *                specified remote address and port.
     */
    protected void connect(InetAddress inetAddr, int port)
            throws SocketException {
        // do nothing
    }

    /**
     * Disconnects this socket from the remote host.
     */
    protected void disconnect() {
        // do nothing
    }

    /**
     * Receives data into the supplied datagram packet by peeking. The data is
     * not removed from socket buffer and can be received again by another
     * {@code peekData()} or {@code receive()} call. This call blocks until
     * either data has been received or, if a timeout is set, the timeout has
     * been expired.
     *
     * @param pack
     *            the datagram packet used to store the data.
     * @return the port the packet was received from.
     * @throws IOException
     *                if an error occurs while peeking at the data.
     */
    protected abstract int peekData(DatagramPacket pack) throws IOException;

    /**
     * Initialize the bind() state.
     * @hide used in java.nio.
     */
    protected void onBind(InetAddress localAddress, int localPort) {
        // Do not add any code to these methods. They are concrete only to preserve API
        // compatibility.
    }

    /**
     * Initialize the connect() state.
     * @hide used in java.nio.
     */
    protected void onConnect(InetAddress remoteAddress, int remotePort) {
        // Do not add any code to these methods. They are concrete only to preserve API
        // compatibility.
    }

    /**
     * Initialize the disconnected state.
     * @hide used in java.nio.
     */
    protected void onDisconnect() {
        // Do not add any code to these methods. They are concrete only to preserve API
        // compatibility.
    }

    /**
     * Initialize the closed state.
     * @hide used in java.nio.
     */
    protected void onClose() {
        // Do not add any code to these methods. They are concrete only to preserve API
        // compatibility.
    }
}
