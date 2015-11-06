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

package java.nio;

import com.google.j2objc.annotations.Weak;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PlainDatagramSocketImpl;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Set;

import libcore.io.ErrnoException;
import libcore.io.IoUtils;
import libcore.io.NetworkBridge;
import libcore.io.NetworkOs;
import libcore.util.EmptyArray;

/*
 * The default implementation class of java.nio.channels.DatagramChannel.
 */
class DatagramChannelImpl extends DatagramChannel implements FileDescriptorChannel {
    // The fd to interact with native code
    private final FileDescriptor fd;

    // Our internal DatagramSocket.
    private DatagramSocket socket;

    // The remote address to be connected.
    InetSocketAddress connectAddress;

    // The local address.
    InetAddress localAddress;

    // local port
    private int localPort;

    // At first, uninitialized.
    boolean connected = false;

    // whether the socket is bound
    boolean isBound = false;

    private final Object readLock = new Object();
    private final Object writeLock = new Object();

    /*
     * Constructor
     */
    protected DatagramChannelImpl(SelectorProvider selectorProvider) throws IOException {
        super(selectorProvider);
        fd = NetworkBridge.socket(false);
    }

    /*
     * for native call
     */
    @SuppressWarnings("unused")
    private DatagramChannelImpl() {
        super(SelectorProvider.provider());
        fd = new FileDescriptor();
        connectAddress = new InetSocketAddress(0);
    }

    /*
     * Getting the internal DatagramSocket If we have not the socket, we create
     * a new one.
     */
    @Override
    synchronized public DatagramSocket socket() {
        if (socket == null) {
            socket = new DatagramSocketAdapter(new PlainDatagramSocketImpl(fd, localPort), this);
        }
        return socket;
    }

    /** @hide Until ready for a public API change */
    @Override
    synchronized public DatagramChannel bind(SocketAddress local) throws IOException {
        checkOpen();
        if (isBound) {
            throw new AlreadyBoundException();
        }

        if (local == null) {
            local = new InetSocketAddress(Inet4Address.ANY, 0);
        } else if (!(local instanceof InetSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }

        InetSocketAddress localAddress = (InetSocketAddress) local;
        NetworkBridge.bind(fd, localAddress.getAddress(), localAddress.getPort());
        onBind(true /* updateSocketState */);
        return this;
    }

    /**
     * Initialise the isBound, localAddress and localPort state from the file descriptor. Used when
     * some or all of the bound state has been left to the OS to decide, or when the Socket handled
     * bind() or connect().
     *
     * @param updateSocketState
     *        if the associated socket (if present) needs to be updated
     * @hide used to sync state, non-private to avoid synthetic method
     */
    void onBind(boolean updateSocketState) {
        SocketAddress sa;
        try {
            sa = NetworkOs.getsockname(fd);
        } catch (ErrnoException errnoException) {
            throw new AssertionError(errnoException);
        }
        isBound = true;
        InetSocketAddress localSocketAddress = (InetSocketAddress) sa;
        localAddress = localSocketAddress.getAddress();
        localPort = localSocketAddress.getPort();
        if (updateSocketState && socket != null) {
            socket.onBind(localAddress, localPort);
        }
    }

    /** @hide Until ready for a public API change */
    @Override
    synchronized public SocketAddress getLocalAddress() throws IOException {
        checkOpen();
        return isBound ? new InetSocketAddress(localAddress, localPort) : null;
    }

    /** @hide Until ready for a public API change */
    @Override
    public <T> T getOption(SocketOption<T> option) throws IOException {
        return ChannelUtils.getSocketOption(
                this, StandardSocketOptions.DATAGRAM_SOCKET_OPTIONS, option);
    }

    /** @hide Until ready for a public API change */
    @Override
    public <T> DatagramChannel setOption(SocketOption<T> option, T value) throws IOException {
        checkOpen();
        ChannelUtils.setSocketOption(
                this, StandardSocketOptions.DATAGRAM_SOCKET_OPTIONS, option, value);
        return this;
    }

    /** @hide Until ready for a public API change */
    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return StandardSocketOptions.DATAGRAM_SOCKET_OPTIONS;
    }

    @Override
    synchronized public boolean isConnected() {
        return connected;
    }

    @Override
    synchronized public DatagramChannel connect(SocketAddress address) throws IOException {
        // must be open
        checkOpen();
        // status must be un-connected.
        if (connected) {
            throw new IllegalStateException();
        }

        // check the address
        InetSocketAddress inetSocketAddress = SocketChannelImpl.validateAddress(address);
        InetAddress remoteAddress = inetSocketAddress.getAddress();
        int remotePort = inetSocketAddress.getPort();
        try {
            begin();
            NetworkBridge.connect(fd, remoteAddress, remotePort);
        } catch (ConnectException e) {
            // ConnectException means connect fail, not exception
        } finally {
            end(true);
        }

        // connect() performs a bind() if an explicit bind() was not performed. Keep the local
        // address state held by the channel and the socket up to date.
        if (!isBound) {
            onBind(true /* updateSocketState */);
        }

        // Keep the connected state held by the channel and the socket up to date.
        onConnect(remoteAddress, remotePort, true /* updateSocketState */);
        return this;
    }

    /**
     * Initialize the state associated with being connected, optionally syncing the socket if there
     * is one.
     * @hide used to sync state, non-private to avoid synthetic method
     */
    void onConnect(InetAddress remoteAddress, int remotePort, boolean updateSocketState) {
        connected = true;
        connectAddress = new InetSocketAddress(remoteAddress, remotePort);
        if (updateSocketState && socket != null) {
            socket.onConnect(remoteAddress, remotePort);
        }
    }

    @Override
    synchronized public DatagramChannel disconnect() throws IOException {
        if (!isConnected() || !isOpen()) {
            return this;
        }

        // Keep the disconnected state held by the channel and the socket up to date.
        onDisconnect(true /* updateSocketState */);

        try {
            NetworkOs.connect(fd, InetAddress.UNSPECIFIED, 0);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
        return this;
    }

    /**
     * Initialize the state associated with being disconnected, optionally syncing the socket if
     * there is one.
     * @hide used to sync state, non-private to avoid synthetic method
     */
    void onDisconnect(boolean updateSocketState) {
        connected = false;
        connectAddress = null;
        if (updateSocketState && socket != null && socket.isConnected()) {
            socket.onDisconnect();
        }
    }

    @Override
    public SocketAddress receive(ByteBuffer target) throws IOException {
        target.checkWritable();
        checkOpen();

        if (!isBound) {
            return null;
        }

        SocketAddress retAddr = null;
        try {
            begin();

            // receive real data packet, (not peek)
            synchronized (readLock) {
                boolean loop = isBlocking();
                if (!target.isDirect()) {
                    retAddr = receiveImpl(target, loop);
                } else {
                    retAddr = receiveDirectImpl(target, loop);
                }
            }
        } catch (InterruptedIOException e) {
            // this line used in Linux
            return null;
        } finally {
            end(retAddr != null);
        }
        return retAddr;
    }

    private SocketAddress receiveImpl(ByteBuffer target, boolean loop) throws IOException {
        SocketAddress retAddr = null;
        DatagramPacket receivePacket;
        int oldposition = target.position();
        int received;
        // TODO: disallow mapped buffers and lose this conditional?
        if (target.hasArray()) {
            receivePacket = new DatagramPacket(target.array(), target.position() + target.arrayOffset(), target.remaining());
        } else {
            receivePacket = new DatagramPacket(new byte[target.remaining()], target.remaining());
        }
        do {
            received = NetworkBridge.recvfrom(false, fd, receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(), 0, receivePacket, isConnected());
            if (receivePacket.getAddress() != null) {
                if (received > 0) {
                    if (target.hasArray()) {
                        target.position(oldposition + received);
                    } else {
                        // copy the data of received packet
                        target.put(receivePacket.getData(), 0, received);
                    }
                }
                retAddr = receivePacket.getSocketAddress();
                break;
            }
        } while (loop);
        return retAddr;
    }

    private SocketAddress receiveDirectImpl(ByteBuffer target, boolean loop) throws IOException {
        SocketAddress retAddr = null;
        DatagramPacket receivePacket = new DatagramPacket(EmptyArray.BYTE, 0);
        int oldposition = target.position();
        int received;
        do {
            received = NetworkBridge.recvfrom(false, fd, target, 0, receivePacket, isConnected());
            if (receivePacket.getAddress() != null) {
                // copy the data of received packet
                if (received > 0) {
                    target.position(oldposition + received);
                }
                retAddr = receivePacket.getSocketAddress();
                break;
            }
        } while (loop);
        return retAddr;
    }

    @Override
    public int send(ByteBuffer source, SocketAddress socketAddress) throws IOException {
        checkNotNull(source);
        checkOpen();

        InetSocketAddress isa = (InetSocketAddress) socketAddress;
        if (isa.getAddress() == null) {
            throw new IOException();
        }

        if (isConnected() && !connectAddress.equals(isa)) {
            throw new IllegalArgumentException("Connected to " + connectAddress +
                                               ", not " + socketAddress);
        }

        synchronized (writeLock) {
            int sendCount = 0;
            try {
                begin();
                int oldPosition = source.position();
                sendCount = NetworkBridge.sendto(fd, source, 0, isa.getAddress(), isa.getPort());
                if (sendCount > 0) {
                    source.position(oldPosition + sendCount);
                }
                if (!isBound) {
                    onBind(true /* updateSocketState */);
                }
            } finally {
                end(sendCount >= 0);
            }
            return sendCount;
        }
    }

    @Override
    public int read(ByteBuffer target) throws IOException {
        target.checkWritable();
        checkOpenConnected();

        if (!target.hasRemaining()) {
            return 0;
        }

        int readCount;
        if (target.isDirect() || target.hasArray()) {
            readCount = readImpl(target);
            if (readCount > 0) {
                target.position(target.position() + readCount);
            }

        } else {
            byte[] readArray = new byte[target.remaining()];
            ByteBuffer readBuffer = ByteBuffer.wrap(readArray);
            readCount = readImpl(readBuffer);
            if (readCount > 0) {
                target.put(readArray, 0, readCount);
            }
        }
        return readCount;
    }

    @Override
    public long read(ByteBuffer[] targets, int offset, int length) throws IOException {
        Arrays.checkOffsetAndCount(targets.length, offset, length);

        // status must be open and connected
        checkOpenConnected();
        int totalCount = FileChannelImpl.calculateTotalRemaining(targets, offset, length, true);
        if (totalCount == 0) {
            return 0;
        }

        // read data to readBuffer, and then transfer data from readBuffer to
        // targets.
        ByteBuffer readBuffer = ByteBuffer.allocate(totalCount);
        int readCount;
        readCount = readImpl(readBuffer);
        int left = readCount;
        int index = offset;
        // transfer data from readBuffer to targets
        byte[] readArray = readBuffer.array();
        while (left > 0) {
            int putLength = Math.min(targets[index].remaining(), left);
            targets[index].put(readArray, readCount - left, putLength);
            index++;
            left -= putLength;
        }
        return readCount;
    }

    /*
     * read from channel, and store the result in the target.
     */
    private int readImpl(ByteBuffer dst) throws IOException {
        synchronized (readLock) {
            int readCount = 0;
            try {
                begin();
                readCount = NetworkBridge.recvfrom(false, fd, dst, 0, null, isConnected());
            } catch (InterruptedIOException e) {
                // InterruptedIOException will be thrown when timeout.
                return 0;
            } finally {
                end(readCount > 0);
            }
            return readCount;
        }
    }

    @Override public int write(ByteBuffer src) throws IOException {
        checkNotNull(src);
        checkOpenConnected();
        if (!src.hasRemaining()) {
            return 0;
        }

        int writeCount = writeImpl(src);
        if (writeCount > 0) {
            src.position(src.position() + writeCount);
        }
        return writeCount;
    }

    /**
     * @see java.nio.channels.DatagramChannel#write(java.nio.ByteBuffer[], int,
     *      int)
     */
    @Override
    public long write(ByteBuffer[] sources, int offset, int length) throws IOException {
        Arrays.checkOffsetAndCount(sources.length, offset, length);

        // status must be open and connected
        checkOpenConnected();
        int count = FileChannelImpl.calculateTotalRemaining(sources, offset, length, false);
        if (count == 0) {
            return 0;
        }
        ByteBuffer writeBuf = ByteBuffer.allocate(count);
        for (int val = offset; val < length + offset; val++) {
            ByteBuffer source = sources[val];
            int oldPosition = source.position();
            writeBuf.put(source);
            source.position(oldPosition);
        }
        writeBuf.flip();
        int result = writeImpl(writeBuf);
        int val = offset;
        int written = result;
        while (result > 0) {
            ByteBuffer source = sources[val];
            int gap = Math.min(result, source.remaining());
            source.position(source.position() + gap);
            val++;
            result -= gap;
        }
        return written;
    }

    private int writeImpl(ByteBuffer buf) throws IOException {
        synchronized (writeLock) {
            int result = 0;
            try {
                begin();
                result = NetworkBridge.sendto(fd, buf, 0, null, 0);
            } finally {
                end(result > 0);
            }
            return result;
        }
    }

    @Override protected synchronized void implCloseSelectableChannel() throws IOException {
        // A closed channel is not connected.
        onDisconnect(true /* updateSocketState */);
        NetworkBridge.closeSocket(fd);

        if (socket != null && !socket.isClosed()) {
            socket.onClose();
        }
    }

    @Override protected void implConfigureBlocking(boolean blocking) throws IOException {
        IoUtils.setBlocking(fd, blocking);
    }

    /*
     * Status check, must be open.
     */
    private void checkOpen() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    /*
     * Status check, must be open and connected, for read and write.
     */
    private void checkOpenConnected() throws IOException {
        checkOpen();
        if (!isConnected()) {
            throw new NotYetConnectedException();
        }
    }

    /*
     * Buffer check, must not null
     */
    private void checkNotNull(ByteBuffer source) {
        if (source == null) {
            throw new NullPointerException("source == null");
        }
    }

    /*
     * Get the fd for internal use.
     */
    public FileDescriptor getFD() {
        return fd;
    }

    /*
     * The adapter class of DatagramSocket
     */
    private static class DatagramSocketAdapter extends DatagramSocket {

        /*
         * The internal datagramChannelImpl.
         */
        @Weak
        private final DatagramChannelImpl channelImpl;

        /*
         * Constructor initialize the datagramSocketImpl and datagramChannelImpl
         */
        DatagramSocketAdapter(DatagramSocketImpl socketimpl, DatagramChannelImpl channelImpl) {
            super(socketimpl);
            this.channelImpl = channelImpl;

            // Sync state socket state with the channel it is being created from
            if (channelImpl.isBound) {
                onBind(channelImpl.localAddress, channelImpl.localPort);
            }
            if (channelImpl.connected) {
                onConnect(
                        channelImpl.connectAddress.getAddress(),
                        channelImpl.connectAddress.getPort());
            } else {
                onDisconnect();
            }
            if (!channelImpl.isOpen()) {
                onClose();
            }
        }

        /*
         * Get the internal datagramChannelImpl
         */
        @Override
        public DatagramChannel getChannel() {
            return channelImpl;
        }

        @Override
        public void bind(SocketAddress localAddr) throws SocketException {
            if (channelImpl.isConnected()) {
                throw new AlreadyConnectedException();
            }
            super.bind(localAddr);
            channelImpl.onBind(false /* updateSocketState */);
        }

        @Override
        public void connect(SocketAddress peer) throws SocketException {
            if (isConnected()) {
                // RI compatibility: If the socket is already connected this fails.
                throw new IllegalStateException("Socket is already connected.");
            }
            super.connect(peer);
            // Connect may have performed an implicit bind(). Sync up here.
            channelImpl.onBind(false /* updateSocketState */);

            InetSocketAddress inetSocketAddress = (InetSocketAddress) peer;
            channelImpl.onConnect(
                    inetSocketAddress.getAddress(), inetSocketAddress.getPort(),
                    false /* updateSocketState */);
        }

        @Override
        public void connect(InetAddress address, int port) {
            // To avoid implementing connect() twice call this.connect(SocketAddress) in preference
            // to super.connect().
            try {
                connect(new InetSocketAddress(address, port));
            } catch (SocketException e) {
                // Ignored - there is nothing we can report here.
            }
        }

        @Override
        public void receive(DatagramPacket packet) throws IOException {
            if (!channelImpl.isBlocking()) {
                throw new IllegalBlockingModeException();
            }

            boolean wasBound = isBound();
            super.receive(packet);
            if (!wasBound) {
                // DatagramSocket.receive() will implicitly bind if it hasn't been done explicitly.
                // Sync the channel state with the socket.
                channelImpl.onBind(false /* updateSocketState */);
            }
        }

        @Override
        public void send(DatagramPacket packet) throws IOException {
            if (!channelImpl.isBlocking()) {
                throw new IllegalBlockingModeException();
            }

            // DatagramSocket.send() will implicitly bind if it hasn't been done explicitly. Force
            // bind() here so that the channel state stays in sync with the socket.
            boolean wasBound = isBound();
            super.send(packet);
            if (!wasBound) {
                // DatagramSocket.send() will implicitly bind if it hasn't been done explicitly.
                // Sync the channel state with the socket.
                channelImpl.onBind(false /* updateSocketState */);
            }
        }

        @Override
        public void close() {
            synchronized (channelImpl) {
                super.close();
                if (channelImpl.isOpen()) {
                    try {
                        channelImpl.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }

        @Override
        public void disconnect() {
            super.disconnect();
            channelImpl.onDisconnect(false /* updateSocketState */);
        }
    }
}
