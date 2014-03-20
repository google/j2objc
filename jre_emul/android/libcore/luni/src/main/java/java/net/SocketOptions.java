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


/**
 * Defines an interface for socket implementations to get and set socket
 * options. It is implemented by the classes {@code SocketImpl} and {@code
 * DatagramSocketImpl}.
 *
 * @see SocketImpl
 * @see DatagramSocketImpl
 */
public interface SocketOptions {
    /**
     * Number of seconds to wait when closing a socket if there is still some buffered data to be
     * sent.
     *
     * <p>The option can be set to disabled using {@link #setOption(int, Object)} with a value of
     * {@code Boolean.FALSE}.
     *
     * <p>If this option is set to 0, the TCP socket is closed forcefully and the call to
     * {@code close} returns immediately.
     *
     * If this option is disabled, closing a socket will return immediately and the close will be
     * handled in the background.
     *
     * <p>If this option is set to a value greater than 0, the value is interpreted as the number of
     * seconds to wait. If all data could be sent during this time, the socket is closed normally.
     * Otherwise the connection will be closed forcefully.
     *
     * <p>Valid numeric values for this option are in the range 0 to 65535 inclusive. (Larger
     * timeouts will be treated as 65535s timeouts; roughly 18 hours.)
     *
     * <p>This option is intended for use with sockets in blocking mode. The behavior of this option
     * for non-blocking sockets is undefined.
     */
    public static final int SO_LINGER = 128;

    /**
     * Integer timeout in milliseconds for blocking accept or read/receive operations (but not
     * write/send operations). A timeout of 0 means no timeout. Negative
     * timeouts are not allowed.
     *
     * <p>An {@code InterruptedIOException} is thrown if this timeout expires.
     */
    public static final int SO_TIMEOUT = 4102;

    /**
     * This boolean option specifies whether data is sent immediately on this socket or buffered.
     * <p>
     * If set to {@code Boolean.TRUE} the Nagle algorithm is disabled and there is no buffering.
     * This could lead to low packet efficiency. When set to {@code Boolean.FALSE} the the socket
     * implementation uses buffering to try to reach a higher packet efficiency.
     *
     * <p>See <a href="http://www.ietf.org/rfc/rfc1122.txt">RFC 1122: Requirements for Internet
     * Hosts -- Communication Layers</a> for more information about buffering and the Nagle
     * algorithm.
     */
    public static final int TCP_NODELAY = 1;

    /**
     * This is an IPv4-only socket option whose functionality is subsumed by
     * {@link #IP_MULTICAST_IF2} and not implemented on Android.
     */
    public static final int IP_MULTICAST_IF = 16;

    /**
     * This option does not correspond to any Unix socket option and is not implemented on Android.
     */
    public static final int SO_BINDADDR = 15;

    /**
     * This boolean option specifies whether a reuse of a local address is allowed when another
     * socket has not yet been removed by the operating system.
     *
     * <p>For connection-oriented sockets, if this option is disabled and if there is another socket
     * in state TIME_WAIT on a given address then another socket binding to that address would fail.
     * Setting this value after a socket is bound has no effect.
     *
     * <p>For datagram sockets this option determines whether several sockets can listen on the
     * same address; when enabled each socket will receive a copy of the datagram.
     *
     * <p>See <a href="https://www.ietf.org/rfc/rfc793.txt">RFC 793: Transmission Control Protocol
     * </a> for more information about socket re-use.
     */
    public static final int SO_REUSEADDR = 4;

    /**
     * The size in bytes of a socket's send buffer. This must be an integer greater than 0.
     * This is a hint to the kernel; the kernel may use a larger buffer.
     *
     * <p>For datagram sockets, it is implementation-defined whether packets larger than
     * this size can be sent.
     */
    public static final int SO_SNDBUF = 4097;

    /**
     * The size in bytes of a socket's receive buffer. This must be an integer greater than 0.
     * This is a hint to the kernel; the kernel may use a larger buffer.
     *
     * <p>For datagram sockets, packets larger than this value will be discarded.
     *
     * <p>See <a href="http://www.ietf.org/rfc/rfc1323.txt">RFC1323: TCP Extensions for High
     * Performance</a> for more information about TCP/IP buffering.
     */
    public static final int SO_RCVBUF = 4098;

    /**
     * This boolean option specifies whether the kernel sends keepalive messages on
     * connection-oriented sockets.
     *
     * <p>See <a href="http://www.ietf.org/rfc/rfc1122.txt">RFC 1122: Requirements for Internet
     * Hosts -- Communication Layers</a> for more information on keep-alive.
     */
    public static final int SO_KEEPALIVE = 8;

    /**
     * This integer option specifies the value for the type-of-service field of the IPv4 header,
     * or the traffic class field of the IPv6 header. These correspond to the IP_TOS and IPV6_TCLASS
     * socket options. These may be ignored by the underlying OS. Values must be between 0 and 255
     * inclusive.
     *
     * <p>See <a href="http://www.ietf.org/rfc/rfc1349.txt">RFC 1349</a> for more about IPv4
     * and <a href="http://www.ietf.org/rfc/rfc2460.txt">RFC 2460</a> for more about IPv6.
     */
    public static final int IP_TOS = 3;

    /**
     * This boolean option specifies whether the local loopback of multicast packets is
     * enabled or disabled. This loopback is enabled by default on multicast sockets.
     *
     * <p>See <a href="http://tools.ietf.org/rfc/rfc1112.txt">RFC 1112: Host Extensions for IP
     * Multicasting</a> for more information about IP multicast.
     *
     * <p>See {@link MulticastSocket#setLoopbackMode}.
     */
    public static final int IP_MULTICAST_LOOP = 18;

    /**
     * This boolean option can be used to enable or disable broadcasting on datagram sockets. This
     * option must be enabled to send broadcast messages. The default value is false.
     */
    public static final int SO_BROADCAST = 32;

    /**
     * This boolean option specifies whether sending TCP urgent data is supported on
     * this socket or not.
     */
    public static final int SO_OOBINLINE = 4099;

    /**
     * This integer option sets the outgoing interface for multicast packets
     * using an interface index.
     *
     * <p>See <a href="http://tools.ietf.org/rfc/rfc1112.txt">RFC 1112: Host Extensions for IP
     * Multicasting</a> for more information about IP multicast.
     */
    public static final int IP_MULTICAST_IF2 = 31;

    /**
     * Gets the value for the specified socket option.
     *
     * @return the option value.
     * @param optID
     *            the option identifier.
     * @throws SocketException
     *             if an error occurs reading the option value.
     */
    public Object getOption(int optID) throws SocketException;

    /**
     * Sets the value of the specified socket option.
     *
     * @param optID
     *            the option identifier.
     * @param val
     *            the value to be set for the option.
     * @throws SocketException
     *             if an error occurs setting the option value.
     */
    public void setOption(int optID, Object val) throws SocketException;
}
