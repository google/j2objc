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
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PlainSocketImpl;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.SocketUtils;
import java.net.StandardSocketOptions;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Set;

import libcore.io.ErrnoException;
import libcore.io.Libcore;
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import static libcore.io.OsConstants.*;

/*
 * The default implementation class of java.nio.channels.SocketChannel.
 */
class SocketChannelImpl extends SocketChannel implements FileDescriptorChannel {
    private static final int SOCKET_STATUS_UNINITIALIZED = -1;

    // Status before connect.
    private static final int SOCKET_STATUS_UNCONNECTED = 0;

    // Status connection pending.
    private static final int SOCKET_STATUS_PENDING = 1;

    // Status after connection success.
    private static final int SOCKET_STATUS_CONNECTED = 2;

    // Status closed.
    private static final int SOCKET_STATUS_CLOSED = 3;

    private final FileDescriptor fd;

    // Our internal Socket.
    private SocketAdapter socket = null;

    // The address to be connected.
    private InetSocketAddress connectAddress = null;

    // The local address the socket is bound to.
    private InetAddress localAddress = null;
    private int localPort;

    private int status = SOCKET_STATUS_UNINITIALIZED;

    // Whether the socket is bound.
    private volatile boolean isBound = false;

    private final Object readLock = new Object();

    private final Object writeLock = new Object();

    /*
     * Constructor for creating a connected socket channel.
     */
    public SocketChannelImpl(SelectorProvider selectorProvider) throws IOException {
        this(selectorProvider, true);
    }

    /*
     * Constructor for creating an optionally connected socket channel.
     */
    public SocketChannelImpl(SelectorProvider selectorProvider, boolean connect) throws IOException {
        super(selectorProvider);
        status = SOCKET_STATUS_UNCONNECTED;
        fd = (connect ? IoBridge.socket(true) : new FileDescriptor());
    }

    /*
     * Constructor for use by Pipe.SinkChannel and Pipe.SourceChannel.
     */
    public SocketChannelImpl(SelectorProvider selectorProvider, FileDescriptor existingFd) throws IOException {
        super(selectorProvider);
        status = SOCKET_STATUS_CONNECTED;
        fd = existingFd;
    }

    /*
     * Getting the internal Socket If we have not the socket, we create a new
     * one.
     */
    @Override
    synchronized public Socket socket() {
        if (socket == null) {
            try {
                InetAddress addr = null;
                int port = 0;
                if (connectAddress != null) {
                    addr = connectAddress.getAddress();
                    port = connectAddress.getPort();
                }
                socket = new SocketAdapter(new PlainSocketImpl(fd, localPort, addr, port), this);
            } catch (SocketException e) {
                return null;
            }
        }
        return socket;
    }

    /** @hide Until ready for a public API change */
    @Override
    synchronized public final SocketChannel bind(SocketAddress local) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (isBound) {
            throw new AlreadyBoundException();
        }

        if (local == null) {
            local = new InetSocketAddress(Inet4Address.ANY, 0);
        } else if (!(local instanceof InetSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }

        InetSocketAddress localAddress = (InetSocketAddress) local;
        IoBridge.bind(fd, localAddress.getAddress(), localAddress.getPort());
        onBind(true /* updateSocketState */);
        return this;
    }

    /**
     * Initialise the isBound, localAddress and localPort state from the file descriptor. Used when
     * some or all of the bound state has been left to the OS to decide, or when the Socket handled
     * bind() or connect().
     *
     * @param updateSocketState
     *      if the associated socket (if present) needs to be updated
     * @hide package visible for other nio classes
     */
    void onBind(boolean updateSocketState) {
        SocketAddress sa;
        try {
            sa = Libcore.os.getsockname(fd);
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
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        return isBound ? new InetSocketAddress(localAddress, localPort) : null;
    }

    /** @hide Until ready for a public API change */
    @Override
    public <T> T getOption(SocketOption<T> option) throws IOException {
        return NioUtils.getSocketOption(this, StandardSocketOptions.SOCKET_OPTIONS, option);
    }

    /** @hide Until ready for a public API change */
    @Override
    public <T> SocketChannel setOption(SocketOption<T> option, T value) throws IOException {
        NioUtils.setSocketOption(this, StandardSocketOptions.SOCKET_OPTIONS, option, value);
        return this;
    }

    /** @hide Until ready for a public API change */
    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return StandardSocketOptions.SOCKET_OPTIONS;
    }

    @Override
    synchronized public boolean isConnected() {
        return status == SOCKET_STATUS_CONNECTED;
    }

    @Override
    synchronized public boolean isConnectionPending() {
        return status == SOCKET_STATUS_PENDING;
    }

    @Override
    public boolean connect(SocketAddress socketAddress) throws IOException {
        // status must be open and unconnected
        checkUnconnected();

        // check the address
        InetSocketAddress inetSocketAddress = validateAddress(socketAddress);
        InetAddress normalAddr = inetSocketAddress.getAddress();
        int port = inetSocketAddress.getPort();

        // When connecting, map ANY address to localhost
        if (normalAddr.isAnyLocalAddress()) {
            normalAddr = InetAddress.getLocalHost();
        }

        boolean isBlocking = isBlocking();
        boolean finished = false;
        int newStatus;
        try {
            if (isBlocking) {
                begin();
            }
            // When in blocking mode, IoBridge.connect() will return without an exception when the
            // socket is connected. When in non-blocking mode it will return without an exception
            // without knowing the result of the connection attempt, which could still be going on.
            IoBridge.connect(fd, normalAddr, port);
            newStatus = isBlocking ? SOCKET_STATUS_CONNECTED : SOCKET_STATUS_PENDING;
            finished = true;
        } catch (IOException e) {
            if (isEINPROGRESS(e)) {
                newStatus = SOCKET_STATUS_PENDING;
            } else {
                if (isOpen()) {
                    close();
                    finished = true;
                }
                throw e;
            }
        } finally {
            if (isBlocking) {
                end(finished);
            }
        }

        // If the channel was not bound, a connection attempt will have caused an implicit bind() to
        // take place. Keep the local address state held by the channel and the socket up to date.
        if (!isBound) {
            onBind(true /* updateSocketState */);
        }

        // Keep the connected state held by the channel and the socket up to date.
        onConnectStatusChanged(inetSocketAddress, newStatus, true /* updateSocketState */);

        return status == SOCKET_STATUS_CONNECTED;
    }

    /**
     * Initialise the connect() state with the supplied information.
     *
     * @param updateSocketState
     *     if the associated socket (if present) needs to be updated
     * @hide package visible for other nio classes
     */
    void onConnectStatusChanged(InetSocketAddress address, int status, boolean updateSocketState) {
        this.status = status;
        connectAddress = address;
        if (status == SOCKET_STATUS_CONNECTED && updateSocketState && socket != null) {
            socket.onConnect(connectAddress.getAddress(), connectAddress.getPort());
        }
    }

    private boolean isEINPROGRESS(IOException e) {
        if (isBlocking()) {
            return false;
        }
        if (e instanceof ConnectException) {
            Throwable cause = e.getCause();
            if (cause instanceof ErrnoException) {
                return ((ErrnoException) cause).errno == EINPROGRESS;
            }
        }
        return false;
    }

    @Override
    public boolean finishConnect() throws IOException {
        synchronized (this) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
            if (status == SOCKET_STATUS_CONNECTED) {
                return true;
            }
            if (status != SOCKET_STATUS_PENDING) {
                throw new NoConnectionPendingException();
            }
        }

        boolean finished = false;
        try {
            begin();
            InetAddress inetAddress = connectAddress.getAddress();
            int port = connectAddress.getPort();
            finished = IoBridge.isConnected(fd, inetAddress, port, 0, 0); // Return immediately.
        } catch (ConnectException e) {
            if (isOpen()) {
                close();
                finished = true;
            }
            throw e;
        } finally {
            end(finished);
        }

        synchronized (this) {
            status = (finished ? SOCKET_STATUS_CONNECTED : status);
            if (finished && socket != null) {
                socket.onConnect(connectAddress.getAddress(), connectAddress.getPort());
            }
        }
        return finished;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        dst.checkWritable();
        checkOpenConnected();
        if (!dst.hasRemaining()) {
            return 0;
        }
        return readImpl(dst);
    }

    @Override
    public long read(ByteBuffer[] targets, int offset, int length) throws IOException {
        Arrays.checkOffsetAndCount(targets.length, offset, length);
        checkOpenConnected();
        int totalCount = FileChannelImpl.calculateTotalRemaining(targets, offset, length, true);
        if (totalCount == 0) {
            return 0;
        }
        byte[] readArray = new byte[totalCount];
        ByteBuffer readBuffer = ByteBuffer.wrap(readArray);
        int readCount;
        // read data to readBuffer, and then transfer data from readBuffer to targets.
        readCount = readImpl(readBuffer);
        readBuffer.flip();
        if (readCount > 0) {
            int left = readCount;
            int index = offset;
            // transfer data from readArray to targets
            while (left > 0) {
                int putLength = Math.min(targets[index].remaining(), left);
                targets[index].put(readArray, readCount - left, putLength);
                index++;
                left -= putLength;
            }
        }
        return readCount;
    }

    private int readImpl(ByteBuffer dst) throws IOException {
        synchronized (readLock) {
            int readCount = 0;
            try {
                if (isBlocking()) {
                    begin();
                }
                readCount = IoBridge.recvfrom(true, fd, dst, 0, null, false);
                if (readCount > 0) {
                    dst.position(dst.position() + readCount);
                }
            } finally {
                if (isBlocking()) {
                    end(readCount > 0);
                }
            }
            return readCount;
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (src == null) {
            throw new NullPointerException("src == null");
        }
        checkOpenConnected();
        if (!src.hasRemaining()) {
            return 0;
        }
        return writeImpl(src);
    }

    @Override
    public long write(ByteBuffer[] sources, int offset, int length) throws IOException {
        Arrays.checkOffsetAndCount(sources.length, offset, length);
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

    private int writeImpl(ByteBuffer src) throws IOException {
        synchronized (writeLock) {
            if (!src.hasRemaining()) {
                return 0;
            }
            int writeCount = 0;
            try {
                if (isBlocking()) {
                    begin();
                }
                writeCount = IoBridge.sendto(fd, src, 0, null, 0);
                if (writeCount > 0) {
                    src.position(src.position() + writeCount);
                }
            } finally {
                if (isBlocking()) {
                    end(writeCount >= 0);
                }
            }
            return writeCount;
        }
    }

    /*
     * Status check, open and "connected", when read and write.
     */
    synchronized private void checkOpenConnected() throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (!isConnected()) {
            throw new NotYetConnectedException();
        }
    }

    /*
     * Status check, open and "unconnected", before connection.
     */
    synchronized private void checkUnconnected() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (status == SOCKET_STATUS_CONNECTED) {
            throw new AlreadyConnectedException();
        }
        if (status == SOCKET_STATUS_PENDING) {
            throw new ConnectionPendingException();
        }
    }

    /*
     * Shared by this class and DatagramChannelImpl, to do the address transfer
     * and check.
     */
    static InetSocketAddress validateAddress(SocketAddress socketAddress) {
        if (socketAddress == null) {
            throw new IllegalArgumentException("socketAddress == null");
        }
        if (!(socketAddress instanceof InetSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        if (inetSocketAddress.isUnresolved()) {
            throw new UnresolvedAddressException();
        }
        return inetSocketAddress;
    }

    /*
     * Do really closing action here.
     */
    @Override
    protected synchronized void implCloseSelectableChannel() throws IOException {
        if (status != SOCKET_STATUS_CLOSED) {
            status = SOCKET_STATUS_CLOSED;
            // IoBridge.closeSocket(fd) is idempotent: It is safe to call on an already-closed file
            // descriptor.
            IoBridge.closeSocket(fd);
            if (socket != null && !socket.isClosed()) {
                socket.onClose();
            }
        }
    }

    @Override protected void implConfigureBlocking(boolean blocking) throws IOException {
        IoUtils.setBlocking(fd, blocking);
    }

    /*
     * Get the fd.
     */
    public FileDescriptor getFD() {
        return fd;
    }

    /* @hide used by ServerSocketChannelImpl to sync channel state during accept() */
    public void onAccept(InetSocketAddress remoteAddress, boolean updateSocketState) {
        onBind(updateSocketState);
        onConnectStatusChanged(remoteAddress, SOCKET_STATUS_CONNECTED, updateSocketState);
    }

    /*
     * Adapter classes for internal socket.
     */
    private static class SocketAdapter extends Socket {
        @Weak
        private final SocketChannelImpl channel;
        @Weak
        private final PlainSocketImpl socketImpl;

        SocketAdapter(PlainSocketImpl socketImpl, SocketChannelImpl channel)
                throws SocketException {
            super(socketImpl);
            this.socketImpl = socketImpl;
            this.channel = channel;
            SocketUtils.setCreated(this);

            // Sync state socket state with the channel it is being created from
            if (channel.isBound) {
                onBind(channel.localAddress, channel.localPort);
            }
            if (channel.isConnected()) {
                onConnect(channel.connectAddress.getAddress(), channel.connectAddress.getPort());
            }
            if (!channel.isOpen()) {
                onClose();
            }

        }

        @Override
        public SocketChannel getChannel() {
            return channel;
        }

        @Override
        public void connect(SocketAddress remoteAddr, int timeout) throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            if (isConnected()) {
                throw new AlreadyConnectedException();
            }
            super.connect(remoteAddr, timeout);
            channel.onBind(false);
            if (super.isConnected()) {
                InetSocketAddress remoteInetAddress = (InetSocketAddress) remoteAddr;
                channel.onConnectStatusChanged(
                        remoteInetAddress, SOCKET_STATUS_CONNECTED, false /* updateSocketState */);
            }
        }

        @Override
        public void bind(SocketAddress localAddr) throws IOException {
            if (channel.isConnected()) {
                throw new AlreadyConnectedException();
            }
            if (SocketChannelImpl.SOCKET_STATUS_PENDING == channel.status) {
                throw new ConnectionPendingException();
            }
            super.bind(localAddr);
            channel.onBind(false);
        }

        @Override
        public void close() throws IOException {
            synchronized (channel) {
                super.close();
                if (channel.isOpen()) {
                    // channel.close() recognizes the socket is closed and avoids recursion. There
                    // is no channel.onClose() because the "closed" field is private.
                    channel.close();
                }
            }
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new BlockingCheckOutputStream(super.getOutputStream(), channel);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new BlockingCheckInputStream(super.getInputStream(), channel);
        }

        @Override
        public FileDescriptor getFileDescriptor$() {
            return socketImpl.getFD$();
        }
    }

    /*
     * Throws an IllegalBlockingModeException if the channel is in non-blocking
     * mode when performing write operations.
     */
    private static class BlockingCheckOutputStream extends FilterOutputStream {
        private final SocketChannel channel;

        public BlockingCheckOutputStream(OutputStream out, SocketChannel channel) {
            super(out);
            this.channel = channel;
        }

        @Override
        public void write(byte[] buffer, int offset, int byteCount) throws IOException {
            checkBlocking();
            out.write(buffer, offset, byteCount);
        }

        @Override
        public void write(int oneByte) throws IOException {
            checkBlocking();
            out.write(oneByte);
        }

        @Override
        public void write(byte[] buffer) throws IOException {
            checkBlocking();
            out.write(buffer);
        }

        @Override
        public void close() throws IOException {
            super.close();
            // channel.close() recognizes the socket is closed and avoids recursion. There is no
            // channel.onClose() because the "closed" field is private.
            channel.close();
        }

        private void checkBlocking() {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
        }
    }

    /*
     * Throws an IllegalBlockingModeException if the channel is in non-blocking
     * mode when performing read operations.
     */
    private static class BlockingCheckInputStream extends FilterInputStream {
        private final SocketChannel channel;

        public BlockingCheckInputStream(InputStream in, SocketChannel channel) {
            super(in);
            this.channel = channel;
        }

        @Override
        public int read() throws IOException {
            checkBlocking();
            return in.read();
        }

        @Override
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            checkBlocking();
            return in.read(buffer, byteOffset, byteCount);
        }

        @Override
        public int read(byte[] buffer) throws IOException {
            checkBlocking();
            return in.read(buffer);
        }

        @Override
        public void close() throws IOException {
            super.close();
            // channel.close() recognizes the socket is closed and avoids recursion. There is no
            // channel.onClose() because the "closed" field is private.
            channel.close();
        }

        private void checkBlocking() {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
        }
    }
}
