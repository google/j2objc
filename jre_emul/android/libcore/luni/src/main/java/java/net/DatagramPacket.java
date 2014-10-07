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
 * This class represents a datagram packet which contains data either to be sent
 * or received through a {@code DatagramSocket}. It holds additional information
 * such as its source or destination host.
 *
 * @see DatagramSocket
 */
public final class DatagramPacket {

    private byte[] data;

    /**
     * Length of the data to be sent or size of data that was received via
     * DatagramSocket#receive() method call.
     */
    private int length;

    /**
     * The last user-supplied length (as opposed to a length set by simply receiving a packet).
     * This length (unlike 'length') is sticky, and survives until the user sets another length.
     * It's used to limit the amount of data that will be taken from future packets.
     */
    private int userSuppliedLength;

    private InetAddress address;

    private int port = -1; // The default port number is -1

    private int offset = 0;

    /**
     * Constructs a new {@code DatagramPacket} object to receive data up to
     * {@code length} bytes.
     *
     * @param data
     *            a byte array to store the read characters.
     * @param length
     *            the length of the data buffer.
     */
    public DatagramPacket(byte[] data, int length) {
        this(data, 0, length);
    }

    /**
     * Constructs a new {@code DatagramPacket} object to receive data up to
     * {@code length} bytes with a specified buffer offset.
     *
     * @param data
     *            a byte array to store the read characters.
     * @param offset
     *            the offset of the byte array where the bytes is written.
     * @param length
     *            the length of the data.
     */
    public DatagramPacket(byte[] data, int offset, int length) {
        setData(data, offset, length);
    }

    /**
     * Constructs a new {@code DatagramPacket} object to send data to the port
     * {@code aPort} of the address {@code host}. The {@code length} must be
     * lesser than or equal to the size of {@code data}. The first {@code
     * length} bytes from the byte array position {@code offset} are sent.
     *
     * @param data
     *            a byte array which stores the characters to be sent.
     * @param offset
     *            the offset of {@code data} where to read from.
     * @param length
     *            the length of data.
     * @param host
     *            the address of the target host.
     * @param aPort
     *            the port of the target host.
     */
    public DatagramPacket(byte[] data, int offset, int length, InetAddress host, int aPort) {
        this(data, offset, length);
        setPort(aPort);
        address = host;
    }

    /**
     * Constructs a new {@code DatagramPacket} object to send data to the port
     * {@code aPort} of the address {@code host}. The {@code length} must be
     * lesser than or equal to the size of {@code data}. The first {@code
     * length} bytes are sent.
     *
     * @param data
     *            a byte array which stores the characters to be sent.
     * @param length
     *            the length of data.
     * @param host
     *            the address of the target host.
     * @param port
     *            the port of the target host.
     */
    public DatagramPacket(byte[] data, int length, InetAddress host, int port) {
        this(data, 0, length, host, port);
    }

    /**
     * Gets the sender or destination IP address of this datagram packet.
     *
     * @return the address from where the datagram was received or to which it
     *         is sent.
     */
    public synchronized InetAddress getAddress() {
        return address;
    }

    /**
     * Gets the data of this datagram packet.
     *
     * @return the received data or the data to be sent.
     */
    public synchronized byte[] getData() {
        return data;
    }

    /**
     * Gets the length of the data stored in this datagram packet.
     *
     * @return the length of the received data or the data to be sent.
     */
    public synchronized int getLength() {
        return length;
    }

    /**
     * Gets the offset of the data stored in this datagram packet.
     *
     * @return the position of the received data or the data to be sent.
     */
    public synchronized int getOffset() {
        return offset;
    }

    /**
     * Gets the port number of the target or sender host of this datagram
     * packet.
     *
     * @return the port number of the origin or target host.
     */
    public synchronized int getPort() {
        return port;
    }

    /**
     * Sets the IP address of the target host.
     *
     * @param addr
     *            the target host address.
     */
    public synchronized void setAddress(InetAddress addr) {
        address = addr;
    }

    /**
     * Sets the data buffer for this datagram packet.
     */
    public synchronized void setData(byte[] data, int offset, int byteCount) {
        if ((offset | byteCount) < 0 || offset > data.length || byteCount > data.length - offset) {
            throw new IllegalArgumentException();
        }
        this.data = data;
        this.offset = offset;
        this.length = byteCount;
        this.userSuppliedLength = byteCount;
    }

    /**
     * Sets the data buffer for this datagram packet. The length of the datagram
     * packet is set to the buffer length.
     *
     * @param buf
     *            the buffer to store the data.
     */
    public synchronized void setData(byte[] buf) {
        length = buf.length; // This will check for null
        userSuppliedLength = length;
        data = buf;
        offset = 0;
    }

    /**
     * Sets the length of the datagram packet. This length plus the offset must
     * be lesser than or equal to the buffer size.
     *
     * @param length
     *            the length of this datagram packet.
     */
    public synchronized void setLength(int length) {
        if (length < 0 || offset + length > data.length) {
            throw new IndexOutOfBoundsException("length=" + length + ", offset=" + offset +
                                                ", buffer size=" + data.length);
        }
        this.length = length;
        this.userSuppliedLength = length;
    }

    /**
     * Resets 'length' to the last user-supplied length, ready to receive another packet.
     * @hide for PlainDatagramSocketImpl
     */
    public void resetLengthForReceive() {
        this.length = userSuppliedLength;
    }

    /**
     * Sets 'length' without changing 'userSuppliedLength', after receiving a packet.
     * @hide for IoBridge
     */
    public void setReceivedLength(int length) {
        this.length = length;
    }

    /**
     * Sets the port number of the target host of this datagram packet.
     *
     * @param aPort
     *            the target host port number.
     */
    public synchronized void setPort(int aPort) {
        if (aPort < 0 || aPort > 65535) {
            throw new IllegalArgumentException("Port out of range: " + aPort);
        }
        port = aPort;
    }

    /**
     * Constructs a new {@code DatagramPacket} object to send data to the
     * address {@code sockAddr}. The {@code length} must be lesser than or equal
     * to the size of {@code data}. The first {@code length} bytes of the data
     * are sent.
     *
     * @param data
     *            the byte array to store the data.
     * @param length
     *            the length of the data.
     * @param sockAddr
     *            the target host address and port.
     * @throws SocketException
     *             if an error in the underlying protocol occurs.
     */
    public DatagramPacket(byte[] data, int length, SocketAddress sockAddr) throws SocketException {
        this(data, 0, length);
        setSocketAddress(sockAddr);
    }

    /**
     * Constructs a new {@code DatagramPacket} object to send data to the
     * address {@code sockAddr}. The {@code length} must be lesser than or equal
     * to the size of {@code data}. The first {@code length} bytes of the data
     * are sent.
     *
     * @param data
     *            the byte array to store the data.
     * @param offset
     *            the offset of the data.
     * @param length
     *            the length of the data.
     * @param sockAddr
     *            the target host address and port.
     * @throws SocketException
     *             if an error in the underlying protocol occurs.
     */
    public DatagramPacket(byte[] data, int offset, int length,
            SocketAddress sockAddr) throws SocketException {
        this(data, offset, length);
        setSocketAddress(sockAddr);
    }

    /**
     * Gets the host address and the port to which this datagram packet is sent
     * as a {@code SocketAddress} object.
     *
     * @return the SocketAddress of the target host.
     */
    public synchronized SocketAddress getSocketAddress() {
        return new InetSocketAddress(getAddress(), getPort());
    }

    /**
     * Sets the {@code SocketAddress} for this datagram packet.
     *
     * @param sockAddr
     *            the SocketAddress of the target host.
     */
    public synchronized void setSocketAddress(SocketAddress sockAddr) {
        if (!(sockAddr instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Socket address not an InetSocketAddress: " +
                    (sockAddr == null ? null : sockAddr.getClass()));
        }
        InetSocketAddress inetAddr = (InetSocketAddress) sockAddr;
        if (inetAddr.isUnresolved()) {
            throw new IllegalArgumentException("Socket address unresolved: " + sockAddr);
        }
        port = inetAddr.getPort();
        address = inetAddr.getAddress();
    }
}
