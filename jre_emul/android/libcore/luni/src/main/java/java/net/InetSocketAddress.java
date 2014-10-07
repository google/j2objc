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

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * This class represents a socket endpoint described by a IP address and a port
 * number. It is a concrete implementation of {@code SocketAddress} for IP.
 */
public class InetSocketAddress extends SocketAddress {

    private static final long serialVersionUID = 5076001401234631237L;

    // Exactly one of hostname or addr should be set.
    private final InetAddress addr;
    private final String hostname;
    private final int port;

    /**
     * @hide internal use only
     */
    public InetSocketAddress() {
        // These will be filled in the native implementation of recvfrom.
        this.addr = null;
        this.hostname = null;
        this.port = -1;
    }

    /**
     * Creates a socket endpoint with the given port number {@code port} and
     * no specified address. The range for valid port numbers is between 0 and
     * 65535 inclusive.
     *
     * @param port
     *            the specified port number to which this socket is bound.
     */
    public InetSocketAddress(int port) {
        this((InetAddress) null, port);
    }

    /**
     * Creates a socket endpoint with the given port number {@code port} and
     * {@code address}. The range for valid port numbers is between 0 and 65535
     * inclusive. If {@code address} is {@code null} this socket is bound to the
     * IPv4 wildcard address.
     *
     * @param port
     *            the specified port number to which this socket is bound.
     * @param address
     *            the specified address to which this socket is bound.
     */
    public InetSocketAddress(InetAddress address, int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port=" + port);
        }
        this.addr = (address == null) ? Inet4Address.ANY : address;
        this.hostname = null;
        this.port = port;
    }

    /**
     * Creates a socket endpoint with the given port number {@code port} and the
     * hostname {@code host}. The hostname is tried to be resolved and cannot be
     * {@code null}. The range for valid port numbers is between 0 and 65535
     * inclusive.
     *
     * @param port
     *            the specified port number to which this socket is bound.
     * @param host
     *            the specified hostname to which this socket is bound.
     */
    public InetSocketAddress(String host, int port) {
        this(host, port, true);
    }

    /*
     * Internal constructor for InetSocketAddress(String, int) and
     * createUnresolved(String, int);
     */
    InetSocketAddress(String hostname, int port, boolean needResolved) {
        if (hostname == null || port < 0 || port > 65535) {
            throw new IllegalArgumentException("host=" + hostname + ", port=" + port);
        }

        InetAddress addr = null;
        if (needResolved) {
            try {
                addr = InetAddress.getByName(hostname);
                hostname = null;
            } catch (UnknownHostException ignored) {
            }
        }
        this.addr = addr;
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Creates an {@code InetSocketAddress} without trying to resolve the
     * hostname into an {@code InetAddress}. The address field is marked as
     * unresolved.
     *
     * @param host
     *            the specified hostname to which this socket is bound.
     * @param port
     *            the specified port number to which this socket is bound.
     * @return the created InetSocketAddress instance.
     * @throws IllegalArgumentException
     *             if the hostname {@code host} is {@code null} or the port is
     *             not in the range between 0 and 65535.
     */
    public static InetSocketAddress createUnresolved(String host, int port) {
        return new InetSocketAddress(host, port, false);
    }

    /**
     * Returns this socket address' port.
     */
    public final int getPort() {
        return port;
    }

    /**
     * Returns this socket address' address.
     */
    public final InetAddress getAddress() {
        return addr;
    }

    /**
     * Returns the hostname, doing a reverse DNS lookup on the {@code InetAddress} if no
     * hostname string was provided at construction time. Use {@link #getHostString} to
     * avoid the reverse DNS lookup.
     */
    public final String getHostName() {
        return (addr != null) ? addr.getHostName() : hostname;
    }

    /**
     * Returns the hostname if known, or the result of {@code InetAddress.getHostAddress}.
     * Unlike {@link #getHostName}, this method will never cause a DNS lookup.
     * @since 1.7
     */
    public final String getHostString() {
        return (hostname != null) ? hostname : addr.getHostAddress();
    }

    /**
     * Returns whether this socket address is unresolved or not.
     *
     * @return {@code true} if this socket address is unresolved, {@code false}
     *         otherwise.
     */
    public final boolean isUnresolved() {
        return addr == null;
    }

    /**
     * Returns a string containing the address (or the hostname for an
     * unresolved {@code InetSocketAddress}) and port number.
     * For example: {@code "www.google.com/74.125.224.115:80"} or {@code "/127.0.0.1:80"}.
     */
    @Override public String toString() {
        return ((addr != null) ? addr.toString() : hostname) + ":" + port;
    }

    /**
     * Compares two socket endpoints and returns true if they are equal. Two
     * socket endpoints are equal if the IP address or the hostname of both are
     * equal and they are bound to the same port.
     *
     * @param socketAddr
     *            the object to be tested for equality.
     * @return {@code true} if this socket and the given socket object {@code
     *         socketAddr} are equal, {@code false} otherwise.
     */
    @Override
    public final boolean equals(Object socketAddr) {
        if (this == socketAddr) {
            return true;
        }
        if (!(socketAddr instanceof InetSocketAddress)) {
            return false;
        }
        InetSocketAddress iSockAddr = (InetSocketAddress) socketAddr;

        // check the ports as we always need to do this
        if (port != iSockAddr.port) {
            return false;
        }

        // we only use the hostnames in the comparison if the addrs were not
        // resolved
        if ((addr == null) && (iSockAddr.addr == null)) {
            return hostname.equals(iSockAddr.hostname);
        }

        // addrs were resolved so use them for the comparison
        if (addr == null) {
            // if we are here we know iSockAddr is not null so just return
            // false
            return false;
        }
        return addr.equals(iSockAddr.addr);
    }

    @Override
    public final int hashCode() {
        if (addr == null) {
            return hostname.hashCode() + port;
        }
        return addr.hashCode() + port;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
}
