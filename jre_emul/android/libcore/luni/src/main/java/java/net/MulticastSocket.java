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
import java.util.Enumeration;
import libcore.io.IoUtils;

/**
 * This class implements a multicast socket for sending and receiving IP
 * multicast datagram packets.
 *
 * @see DatagramSocket
 */
public class MulticastSocket extends DatagramSocket {
    /**
     * Stores the address supplied to setInterface so we can return it from getInterface. The
     * translation to an interface index is lossy because an interface can have multiple addresses.
     */
    private InetAddress setAddress;

    /**
     * Constructs a multicast socket, bound to any available port on the
     * local host.
     *
     * @throws IOException if an error occurs.
     */
    public MulticastSocket() throws IOException {
        setReuseAddress(true);
    }

    /**
     * Constructs a multicast socket, bound to the specified {@code port} on the
     * local host.
     *
     * @throws IOException if an error occurs.
     */
    public MulticastSocket(int port) throws IOException {
        super(port);
        setReuseAddress(true);
    }

    /**
     * Constructs a {@code MulticastSocket} bound to the address and port specified by
     * {@code localAddress}, or an unbound {@code MulticastSocket} if {@code localAddress == null}.
     *
     * @throws IllegalArgumentException if {@code localAddress} is not supported (because it's not
     * an {@code InetSocketAddress}, say).
     * @throws IOException if an error occurs.
     */
    public MulticastSocket(SocketAddress localAddress) throws IOException {
        super(localAddress);
        setReuseAddress(true);
    }

    /**
     * Returns an address of the outgoing network interface used by this socket. To avoid
     * inherent unpredictability, new code should use {@link #getNetworkInterface} instead.
     *
     * @throws SocketException if an error occurs.
     */
    public InetAddress getInterface() throws SocketException {
        checkOpen();
        if (setAddress != null) {
            return setAddress;
        }
        InetAddress ipvXaddress = (InetAddress) impl.getOption(SocketOptions.IP_MULTICAST_IF);
        if (ipvXaddress.isAnyLocalAddress()) {
            // the address was not set at the IPv4 level so check the IPv6
            // level
            NetworkInterface theInterface = getNetworkInterface();
            if (theInterface != null) {
                Enumeration<InetAddress> addresses = theInterface.getInetAddresses();
                if (addresses != null) {
                    while (addresses.hasMoreElements()) {
                        InetAddress nextAddress = addresses.nextElement();
                        if (nextAddress instanceof Inet6Address) {
                            return nextAddress;
                        }
                    }
                }
            }
        }
        return ipvXaddress;
    }

    /**
     * Returns the outgoing network interface used by this socket.
     *
     * @throws SocketException if an error occurs.
     */
    public NetworkInterface getNetworkInterface() throws SocketException {
        checkOpen();
        int index = (Integer) impl.getOption(SocketOptions.IP_MULTICAST_IF2);
        if (index != 0) {
            return NetworkInterface.getByIndex(index);
        }
        return NetworkInterface.forUnboundMulticastSocket();
    }

    /**
     * Returns the time-to-live (TTL) for multicast packets sent on this socket.
     *
     * @throws IOException if an error occurs.
     */
    public int getTimeToLive() throws IOException {
        checkOpen();
        return impl.getTimeToLive();
    }

    /**
     * Returns the time-to-live (TTL) for multicast packets sent on this socket.
     *
     * @throws IOException if an error occurs.
     * @deprecated Use {@link #getTimeToLive} instead.
     */
    @Deprecated
    public byte getTTL() throws IOException {
        checkOpen();
        return impl.getTTL();
    }

    /**
     * Adds this socket to the specified multicast group. A socket must join a
     * group before data may be received. A socket may be a member of multiple
     * groups but may join any group only once.
     *
     * @param groupAddr
     *            the multicast group to be joined.
     * @throws IOException if an error occurs.
     */
    public void joinGroup(InetAddress groupAddr) throws IOException {
        checkJoinOrLeave(groupAddr);
        impl.join(groupAddr);
    }

    /**
     * Adds this socket to the specified multicast group. A socket must join a
     * group before data may be received. A socket may be a member of multiple
     * groups but may join any group only once.
     *
     * @param groupAddress
     *            the multicast group to be joined.
     * @param netInterface
     *            the network interface on which the datagram packets will be
     *            received.
     * @throws IOException
     *                if the specified address is not a multicast address.
     * @throws IllegalArgumentException
     *                if no multicast group is specified.
     */
    public void joinGroup(SocketAddress groupAddress, NetworkInterface netInterface) throws IOException {
        checkJoinOrLeave(groupAddress, netInterface);
        impl.joinGroup(groupAddress, netInterface);
    }

    /**
     * Removes this socket from the specified multicast group.
     *
     * @param groupAddr
     *            the multicast group to be left.
     * @throws NullPointerException
     *                if {@code groupAddr} is {@code null}.
     * @throws IOException
     *                if the specified group address is not a multicast address.
     */
    public void leaveGroup(InetAddress groupAddr) throws IOException {
        checkJoinOrLeave(groupAddr);
        impl.leave(groupAddr);
    }

    /**
     * Removes this socket from the specified multicast group.
     *
     * @param groupAddress
     *            the multicast group to be left.
     * @param netInterface
     *            the network interface on which the addresses should be
     *            dropped.
     * @throws IOException
     *                if the specified group address is not a multicast address.
     * @throws IllegalArgumentException
     *                if {@code groupAddress} is {@code null}.
     */
    public void leaveGroup(SocketAddress groupAddress, NetworkInterface netInterface) throws IOException {
        checkJoinOrLeave(groupAddress, netInterface);
        impl.leaveGroup(groupAddress, netInterface);
    }

    private void checkJoinOrLeave(SocketAddress groupAddress, NetworkInterface netInterface) throws IOException {
        checkOpen();
        if (groupAddress == null) {
            throw new IllegalArgumentException("groupAddress == null");
        }

        if (netInterface != null && !netInterface.getInetAddresses().hasMoreElements()) {
            throw new SocketException("No address associated with interface: " + netInterface);
        }

        if (!(groupAddress instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Group address not an InetSocketAddress: " +
                    groupAddress.getClass());
        }

        InetAddress groupAddr = ((InetSocketAddress) groupAddress).getAddress();
        if (groupAddr == null) {
            throw new SocketException("Group address has no address: " + groupAddress);
        }

        if (!groupAddr.isMulticastAddress()) {
            throw new IOException("Not a multicast group: " + groupAddr);
        }
    }

    private void checkJoinOrLeave(InetAddress groupAddr) throws IOException {
        checkOpen();
        if (!groupAddr.isMulticastAddress()) {
            throw new IOException("Not a multicast group: " + groupAddr);
        }
    }

    /**
     * Sends the given {@code packet} on this socket, using the given {@code ttl}. This method is
     * deprecated because it modifies the TTL socket option for this socket twice on each call.
     *
     * @throws IOException if an error occurs.
     * @deprecated Use {@link #setTimeToLive} instead.
     */
    @Deprecated
    public void send(DatagramPacket packet, byte ttl) throws IOException {
        checkOpen();
        InetAddress packAddr = packet.getAddress();
        int currTTL = getTimeToLive();
        if (packAddr.isMulticastAddress() && (byte) currTTL != ttl) {
            try {
                setTimeToLive(ttl & 0xff);
                impl.send(packet);
            } finally {
                setTimeToLive(currTTL);
            }
        } else {
            impl.send(packet);
        }
    }

    /**
     * Sets the outgoing network interface used by this socket. The interface used is the first
     * interface found to have the given {@code address}. To avoid inherent unpredictability,
     * new code should use {@link #getNetworkInterface} instead.
     *
     * @throws SocketException if an error occurs.
     */
    public void setInterface(InetAddress address) throws SocketException {
        checkOpen();
        if (address == null) {
            throw new NullPointerException("address == null");
        }

        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
        if (networkInterface == null) {
            throw new SocketException("Address not associated with an interface: " + address);
        }
        impl.setOption(SocketOptions.IP_MULTICAST_IF2, networkInterface.getIndex());
        this.setAddress = address;
    }

    /**
     * Sets the outgoing network interface used by this socket to the given
     * {@code networkInterface}.
     *
     * @throws SocketException if an error occurs.
     */
    public void setNetworkInterface(NetworkInterface networkInterface) throws SocketException {
        checkOpen();
        if (networkInterface == null) {
            throw new SocketException("networkInterface == null");
        }

        impl.setOption(SocketOptions.IP_MULTICAST_IF2, networkInterface.getIndex());
        this.setAddress = null;
    }

    /**
     * Sets the time-to-live (TTL) for multicast packets sent on this socket.
     * Valid TTL values are between 0 and 255 inclusive.
     *
     * @throws IOException if an error occurs.
     */
    public void setTimeToLive(int ttl) throws IOException {
        checkOpen();
        if (ttl < 0 || ttl > 255) {
            throw new IllegalArgumentException("TimeToLive out of bounds: " + ttl);
        }
        impl.setTimeToLive(ttl);
    }

    /**
     * Sets the time-to-live (TTL) for multicast packets sent on this socket.
     * Valid TTL values are between 0 and 255 inclusive.
     *
     * @throws IOException if an error occurs.
     * @deprecated Use {@link #setTimeToLive} instead.
     */
    @Deprecated
    public void setTTL(byte ttl) throws IOException {
        checkOpen();
        impl.setTTL(ttl);
    }

    @Override
    synchronized void createSocket(int aPort, InetAddress addr) throws SocketException {
        impl = factory != null ? factory.createDatagramSocketImpl() : new PlainDatagramSocketImpl();
        impl.create();
        try {
            impl.setOption(SocketOptions.SO_REUSEADDR, Boolean.TRUE);
            impl.bind(aPort, addr);
            isBound = true;
        } catch (SocketException e) {
            close();
            throw e;
        }
    }

    /**
     * Returns true if multicast loopback is <i>disabled</i>.
     * See {@link SocketOptions#IP_MULTICAST_LOOP}, and note that the sense of this is the
     * opposite of the underlying Unix {@code IP_MULTICAST_LOOP}.
     *
     * @throws SocketException if an error occurs.
     */
    public boolean getLoopbackMode() throws SocketException {
        checkOpen();
        return !((Boolean) impl.getOption(SocketOptions.IP_MULTICAST_LOOP)).booleanValue();
    }

    /**
     * Disables multicast loopback if {@code disable == true}.
     * See {@link SocketOptions#IP_MULTICAST_LOOP}, and note that the sense of this is the
     * opposite of the underlying Unix {@code IP_MULTICAST_LOOP}: true means disabled, false
     * means enabled.
     *
     * @throws SocketException if an error occurs.
     */
    public void setLoopbackMode(boolean disable) throws SocketException {
        checkOpen();
        impl.setOption(SocketOptions.IP_MULTICAST_LOOP, Boolean.valueOf(!disable));
    }
}
