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

package java.nio.channels;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

/**
 * A {@code ServerSocketChannel} is a partial abstraction of a selectable,
 * stream-oriented listening socket. Binding and manipulation of socket options
 * can only be done through the associated {@link ServerSocket} object, returned
 * by calling {@code socket()}. ServerSocketChannels can not be constructed for
 * an already existing server-socket, nor can a {@link java.net.SocketImpl} be assigned.
 * <p>
 * A server-socket channel is open but not bound when created by the {@code
 * open()} method. Calling {@code accept} before bound will cause a
 * {@link NotYetBoundException}. It can be bound by calling the bind method of a
 * related {@code ServerSocket} instance.
 */
public abstract class ServerSocketChannel extends AbstractSelectableChannel
        implements NetworkChannel {

    /**
     * Constructs a new {@link ServerSocketChannel}.
     *
     * @param selectorProvider
     *            an instance of SelectorProvider.
     */
    protected ServerSocketChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
    }

    /**
     * Creates an open and unbound server-socket channel.
     * <p>
     * This channel is created by calling {@code openServerSocketChannel} method
     * of the default {@code SelectorProvider} instance.
     *
     * @return the new channel which is open but unbound.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public static ServerSocketChannel open() throws IOException {
        return SelectorProvider.provider().openServerSocketChannel();
    }

    /**
     * Gets the valid operations of this channel. Server-socket channels support
     * accepting operation, so this method returns {@code
     * SelectionKey.OP_ACCEPT}.
     *
     * @see java.nio.channels.SelectableChannel#validOps()
     * @return the operations supported by this channel.
     */
    @Override
    public final int validOps() {
        return SelectionKey.OP_ACCEPT;
    }

    /**
     * Return the server-socket assigned this channel, which does not declare
     * any public methods that are not declared in {@code ServerSocket}.
     *
     * @return the server-socket assigned to this channel.
     */
    public abstract ServerSocket socket();

    /**
     * {@inheritDoc}
     *
     * <p>This is equivalent to {@code bind(local, 0)}.
     * @hide Until ready for a public API change
     */
    @Override
    public final ServerSocketChannel bind(SocketAddress local) throws IOException {
        return bind(local, 0);
    }

    /**
     * Binds this server channel to the given local socket address. If the {@code localAddr} is set
     * to {@code null} the socket will be bound to an available local address on any free port of
     * the system.
     *
     * @param localAddr
     *             the local machine address and port to bind on.
     * @param backlog the maximum number of unaccepted connections. Passing 0 or
     *             a negative value yields the default backlog of 50.
     * @return this {@code ServerSocketChannel}.
     * @throws UnsupportedAddressTypeException
     *             if the {@code SocketAddress} is not supported.
     * @throws ClosedChannelException
     *             if the channel is closed.
     * @throws AlreadyBoundException
     *             if the channel is already bound.
     * @throws IOException
     *             if another I/O error occurs.
     * @since 1.7
     * @hide Until ready for a public API change
     */
    public ServerSocketChannel bind(SocketAddress localAddr, int backlog) throws IOException {
        // This method was added for interoperability with Java 7, where it is abstract. It is
        // concrete here to avoid breaking existing Android applications that extend this class.
        throw new UnsupportedOperationException("Subclasses must override this method");
    }

    /** @hide Until ready for a public API change */
     @Override
    public SocketAddress getLocalAddress() throws IOException {
        // This method was added for interoperability with Java 7, where it is abstract. It is
        // concrete here to avoid breaking existing Android applications that extend this class.
        throw new UnsupportedOperationException("Subclasses must override this method");
    }

    /** @hide Until ready for a public API change */
    @Override
    public <T> T getOption(SocketOption<T> option) throws IOException {
        // This method was added for interoperability with Java 7, where it is abstract. It is
        // concrete here to avoid breaking existing Android applications that extend this class.
        throw new UnsupportedOperationException("Subclasses must override this method");
    }

    /** @hide Until ready for a public API change */
    @Override
    public <T> ServerSocketChannel setOption(SocketOption<T> option, T value) throws IOException {
        // This method was added for interoperability with Java 7, where it is abstract. It is
        // concrete here to avoid breaking existing Android applications that extend this class.
        throw new UnsupportedOperationException("Subclasses must override this method");
    }

    /** @hide Until ready for a public API change */
    @Override
    public Set<SocketOption<?>> supportedOptions() {
        // This method was added for interoperability with Java 7, where it is abstract. It is
        // concrete here to avoid breaking existing Android applications that extend this class.
        throw new UnsupportedOperationException("Subclasses must override this method");
    }

    /**
     * Accepts a connection to this server-socket channel.
     * <p>
     * This method returns {@code null} when this channel is non-blocking and no
     * connection is available, otherwise it blocks until a new connection is
     * available or an I/O error occurs. The socket channel returned by this
     * method will always be in blocking mode.
     * <p>
     * This method just executes the same security checks as the {@code
     * accept()} method of the {@link ServerSocket} class.
     *
     * @return the accepted {@code SocketChannel} instance, or {@code null} if
     *         the channel is non-blocking and no connection is available.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IOException
     *             if another I/O error occurs.
     * @throws NotYetBoundException
     *             if the socket has not yet been bound.
     */
    public abstract SocketChannel accept() throws IOException;
}
