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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardSocketOptions;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

import libcore.io.ErrnoException;
import libcore.io.IoUtils;
import static libcore.io.OsConstants.*;

/**
 * The default ServerSocketChannel.
 */
final class ServerSocketChannelImpl extends ServerSocketChannel implements FileDescriptorChannel {

    private final ServerSocketAdapter socket;

    private final Object acceptLock = new Object();

    public ServerSocketChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.socket = new ServerSocketAdapter(this);
    }

    @Override public ServerSocket socket() {
        return socket;
    }

    /** @hide Until ready for a public API change */
    @Override
    public final ServerSocketChannel bind(SocketAddress localAddr, int backlog) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (socket.isBound()) {
            throw new AlreadyBoundException();
        }
        if (localAddr != null && !(localAddr instanceof InetSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }

        socket.bind(localAddr, backlog);
        return this;
    }

    /** @hide Until ready for a public API change */
    @Override
    public SocketAddress getLocalAddress() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        return socket.getLocalSocketAddress();
    }

    /** @hide Until ready for a public API change */
    @Override
    public <T> T getOption(SocketOption<T> option) throws IOException {
        return NioUtils.getSocketOption(this, StandardSocketOptions.SERVER_SOCKET_OPTIONS, option);
    }

    /** @hide Until ready for a public API change */
    @Override
    public <T> ServerSocketChannel setOption(SocketOption<T> option, T value) throws IOException {
        NioUtils.setSocketOption(this, StandardSocketOptions.SERVER_SOCKET_OPTIONS, option, value);
        return this;
    }

    /** @hide Until ready for a public API change */
    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return StandardSocketOptions.SERVER_SOCKET_OPTIONS;
    }

    @Override
    public SocketChannel accept() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (!socket.isBound()) {
            throw new NotYetBoundException();
        }

        // Create an empty socket channel. This will be populated by ServerSocketAdapter.implAccept.
        SocketChannelImpl result = new SocketChannelImpl(provider(), false);
        try {
            begin();
            synchronized (acceptLock) {
                try {
                    socket.implAccept(result);
                } catch (SocketTimeoutException e) {
                    if (shouldThrowSocketTimeoutExceptionFromAccept(e)) {
                        throw e;
                    }
                    // Otherwise, this is a non-blocking socket and there's nothing ready, so we'll
                    // fall through and return null.
                }
            }
        } finally {
            end(result.isConnected());
        }
        return result.isConnected() ? result : null;
    }

    private boolean shouldThrowSocketTimeoutExceptionFromAccept(SocketTimeoutException e) {
        if (isBlocking()) {
            return true;
        }
        Throwable cause = e.getCause();
        if (cause instanceof ErrnoException) {
            if (((ErrnoException) cause).errno == EAGAIN) {
                return false;
            }
        }
        return true;
    }

    @Override protected void implConfigureBlocking(boolean blocking) throws IOException {
        IoUtils.setBlocking(socket.getFD$(), blocking);
    }

    @Override
    synchronized protected void implCloseSelectableChannel() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    @Override
    public FileDescriptor getFD() {
        return socket.getFD$();
    }

    private static class ServerSocketAdapter extends ServerSocket {
        @Weak
        private final ServerSocketChannelImpl channelImpl;

        ServerSocketAdapter(ServerSocketChannelImpl aChannelImpl) throws IOException {
            this.channelImpl = aChannelImpl;
        }

        @Override public Socket accept() throws IOException {
            if (!isBound()) {
                throw new IllegalBlockingModeException();
            }
            SocketChannel sc = channelImpl.accept();
            if (sc == null) {
                throw new IllegalBlockingModeException();
            }
            return sc.socket();
        }

        public Socket implAccept(SocketChannelImpl clientSocketChannel) throws IOException {
            Socket clientSocket = clientSocketChannel.socket();
            boolean connectOK = false;
            try {
                synchronized (this) {
                    super.implAccept(clientSocket);

                    // Sync the client socket's associated channel state with the Socket and OS.
                    InetSocketAddress remoteAddress =
                            new InetSocketAddress(
                                    clientSocket.getInetAddress(), clientSocket.getPort());
                    clientSocketChannel.onAccept(remoteAddress, false /* updateSocketState */);
                }
                connectOK = true;
            } finally {
                if (!connectOK) {
                    clientSocket.close();
                }
            }
            return clientSocket;
        }

        @Override public ServerSocketChannel getChannel() {
            return channelImpl;
        }

        @Override public void close() throws IOException {
            synchronized (channelImpl) {
                super.close();
                if (channelImpl.isOpen()) {
                    channelImpl.close();
                }
            }
        }

        private FileDescriptor getFD$() {
            return super.getImpl$().getFD$();
        }
    }
}
