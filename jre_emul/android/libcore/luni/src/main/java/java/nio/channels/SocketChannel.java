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
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * A {@code SocketChannel} is a selectable channel that provides a partial
 * abstraction of stream connecting socket.
 *
 * The {@link #socket()} method returns a {@link Socket} instance which
 * allows a wider range of socket operations than {@code SocketChannel} itself.
 *
 * <p>A socket channel is open but not connected when created by {@link #open}.
 * After connecting it by calling {@link #connect(SocketAddress)}, it will remain
 * connected until closed.
 *
 * <p>If the connection is non-blocking then
 * {@link #connect(SocketAddress)} is used to initiate the connection, followed
 * by a call of {@link #finishConnect} to perform the final steps of
 * connecting. {@link #isConnectionPending} to tests whether we're still
 * trying to connect; {@link #isConnected} tests whether the socket connect
 * completed successfully. Note that realistic code should use a {@link Selector}
 * instead of polling. Note also that {@link java.net.Socket} can connect with a
 * timeout, which is the most common use for a non-blocking connect.
 *
 * <p>The input and output sides of a channel can be shut down independently and
 * asynchronously without closing the channel. The {@link Socket#shutdownInput} method
 * on the socket returned by {@link #socket}
 * is used for the input side of a channel and subsequent read operations return
 * -1, which means end of stream. If another thread is blocked in a read
 * operation when the shutdown occurs, the read will end without effect and
 * return end of stream. Likewise the {@link Socket#shutdownOutput} method is used for the
 * output side of the channel; subsequent write operations throw a
 * {@link ClosedChannelException}. If the output is shut down and another thread
 * is blocked in a write operation, an {@link AsynchronousCloseException} will
 * be thrown to the pending thread.
 *
 * <p>Socket channels are thread-safe, no more than one thread can read or write at
 * any given time. The {@link #connect(SocketAddress)} and {@link
 * #finishConnect()} methods are synchronized against each other; when they are
 * processing, calls to {@link #read} and {@link #write} will block.
 */
public abstract class SocketChannel extends AbstractSelectableChannel implements
        ByteChannel, ScatteringByteChannel, GatheringByteChannel {

    /**
     * Constructs a new {@code SocketChannel}.
     *
     * @param selectorProvider
     *            an instance of SelectorProvider.
     */
    protected SocketChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
    }

    /**
     * Creates an open and unconnected socket channel.
     * <p>
     * This channel is created by calling {@code openSocketChannel()} of the
     * default {@link SelectorProvider} instance.
     *
     * @return the new channel which is open but unconnected.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public static SocketChannel open() throws IOException {
        return SelectorProvider.provider().openSocketChannel();
    }

    /**
     * Creates a socket channel and connects it to a socket address.
     * <p>
     * This method performs a call to {@code open()} followed by a call to
     * {@code connect(SocketAddress)}.
     *
     * @param address
     *            the socket address to be connected to.
     * @return the new connected channel.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread while this method
     *             is executing.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is executing. The calling thread will have the
     *             interrupt state set and the channel will be closed.
     * @throws UnresolvedAddressException
     *             if the address is not resolved.
     * @throws UnsupportedAddressTypeException
     *             if the address type is not supported.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public static SocketChannel open(SocketAddress address) throws IOException {
        SocketChannel socketChannel = open();
        if (socketChannel != null) {
            socketChannel.connect(address);
        }
        return socketChannel;
    }

    /**
     * Gets the valid operations of this channel. Socket channels support
     * connect, read and write operation, so this method returns
     * {@code SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE}.
     *
     * @return the operations supported by this channel.
     * @see java.nio.channels.SelectableChannel#validOps()
     */
    @Override
    public final int validOps() {
        return (SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * Returns the socket assigned to this channel, which does not declare any public
     * methods that are not declared in {@code Socket}.
     *
     * @return the socket assigned to this channel.
     */
    public abstract Socket socket();

    /**
     * Indicates whether this channel's socket is connected.
     *
     * @return {@code true} if this channel's socket is connected, {@code false}
     *         otherwise.
     */
    public abstract boolean isConnected();

    /**
     * Indicates whether this channel's socket is still trying to connect.
     *
     * @return {@code true} if the connection is initiated but not finished;
     *         {@code false} otherwise.
     */
    public abstract boolean isConnectionPending();

    /**
     * Connects this channel's socket with a remote address.
     * <p>
     * If this channel is blocking, this method will suspend until connecting is
     * finished or an I/O exception occurs. If the channel is non-blocking,
     * this method will return {@code true} if the connection is finished at
     * once or return {@code false} when the connection must be finished later
     * by calling {@code finishConnect()}.
     * <p>
     * This method can be called at any moment and can block other read and
     * write operations while connecting. It executes the same security checks
     * as the connect method of the {@code Socket} class.
     *
     * @param address
     *            the address to connect with.
     * @return {@code true} if the connection is finished, {@code false}
     *         otherwise.
     * @throws AlreadyConnectedException
     *             if the channel is already connected.
     * @throws ConnectionPendingException
     *             a non-blocking connecting operation is already executing on
     *             this channel.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread while this method
     *             is executing.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The calling thread will have the
     *             interrupt state set and this channel will be closed.
     * @throws UnresolvedAddressException
     *             if the address is not resolved.
     * @throws UnsupportedAddressTypeException
     *             if the address type is not supported.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public abstract boolean connect(SocketAddress address) throws IOException;

    /**
     * Completes the connection process initiated by a call of {@code
     * connect(SocketAddress)}.
     * <p>
     * This method returns {@code true} if the connection is finished already
     * and returns {@code false} if the channel is non-blocking and the
     * connection is not finished yet.
     * <p>
     * If this channel is in blocking mode, this method will suspend and return
     * {@code true} when the connection is finished. It closes this channel and
     * throws an exception if the connection fails.
     * <p>
     * This method can be called at any moment and it can block other {@code
     * read} and {@code write} operations while connecting.
     *
     * @return {@code true} if the connection is successfully finished, {@code
     *         false} otherwise.
     * @throws NoConnectionPendingException
     *             if the channel is not connected and the connection process
     *             has not been initiated.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread while this method
     *             is executing.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The calling thread has the
     *             interrupt state set, and this channel is closed.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public abstract boolean finishConnect() throws IOException;

    /**
     * Reads bytes from this socket channel into the given buffer.
     * <p>
     * The maximum number of bytes that will be read is the remaining number of
     * bytes in the buffer when the method is invoked. The bytes will be copied
     * into the buffer starting at the buffer's current position.
     * <p>
     * The call may block if other threads are also attempting to read from this
     * channel.
     * <p>
     * Upon completion, the buffer's position is set to the end of the bytes
     * that have been read. The buffer's limit is not changed.
     *
     * @param target
     *            the byte buffer to receive the bytes.
     * @return the number of bytes actually read.
     * @throws AsynchronousCloseException
     *             if another thread closes the channel during the read.
     * @throws NotYetConnectedException
     *             if this channel is not yet connected.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IOException
     *             if another I/O error occurs.
     * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
     */
    public abstract int read(ByteBuffer target) throws IOException;

    /**
     * Reads bytes from this socket channel into a subset of the given buffers.
     * This method attempts to read all {@code remaining()} bytes from {@code
     * length} byte buffers, in order, starting at {@code targets[offset]}. The
     * number of bytes actually read is returned.
     * <p>
     * If a read operation is in progress, subsequent threads will block until
     * the read is completed and will then contend for the ability to read.
     *
     * @param targets
     *            the array of byte buffers into which the bytes will be copied.
     * @param offset
     *            the index of the first buffer to store bytes in.
     * @param length
     *            the maximum number of buffers to store bytes in.
     * @return the number of bytes actually read.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread during this read
     *             operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if {@code
     *             offset + length} is greater than the size of {@code targets}.
     * @throws IOException
     *             if another I/O error occurs.
     * @throws NotYetConnectedException
     *             if this channel is not yet connected.
     * @see java.nio.channels.ScatteringByteChannel#read(java.nio.ByteBuffer[],
     *      int, int)
     */
    public abstract long read(ByteBuffer[] targets, int offset, int length) throws IOException;

    /**
     * Reads bytes from this socket channel and stores them in the specified
     * array of buffers. This method attempts to read as many bytes as can be
     * stored in the buffer array from this channel and returns the number of
     * bytes actually read.
     * <p>
     * If a read operation is in progress, subsequent threads will block until
     * the read is completed and will then contend for the ability to read.
     * <p>
     * Calling this method is equivalent to calling {@code read(targets, 0,
     * targets.length);}
     *
     * @param targets
     *            the array of byte buffers into which the bytes will be copied.
     * @return the number of bytes actually read.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread during this read
     *             operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IOException
     *             if another I/O error occurs.
     * @throws NotYetConnectedException
     *             if this channel is not yet connected.
     */
    public synchronized final long read(ByteBuffer[] targets) throws IOException {
        return read(targets, 0, targets.length);
    }

    /**
     * Writes bytes from the given byte buffer to this socket channel. The
     * maximum number of bytes that are written is the remaining number of bytes
     * in the buffer when this method is invoked. The bytes are taken from the
     * buffer starting at the buffer's position.
     * <p>
     * The call may block if other threads are also attempting to write to the
     * same channel.
     * <p>
     * Upon completion, the buffer's position is updated to the end of the bytes
     * that have been written. The buffer's limit is not changed.
     *
     * @param source
     *            the byte buffer containing the bytes to be written.
     * @return the number of bytes actually written.
     * @throws AsynchronousCloseException
     *             if another thread closes the channel during the write.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if the channel was already closed.
     * @throws IOException
     *             if another I/O error occurs.
     * @throws NotYetConnectedException
     *             if this channel is not connected yet.
     * @see java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer)
     */
    public abstract int write(ByteBuffer source) throws IOException;

    /**
     * Attempts to write a subset of the given bytes from the buffers to this
     * socket channel. This method attempts to write all {@code remaining()}
     * bytes from {@code length} byte buffers, in order, starting at {@code
     * sources[offset]}. The number of bytes actually written is returned.
     * <p>
     * If a write operation is in progress, subsequent threads will block until
     * the write is completed and then contend for the ability to write.
     *
     * @param sources
     *            the array of byte buffers that is the source for bytes written
     *            to this channel.
     * @param offset
     *            the index of the first buffer in {@code buffers }to get bytes
     *            from.
     * @param length
     *            the number of buffers to get bytes from.
     * @return the number of bytes actually written to this channel.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread during this write
     *             operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if {@code
     *             offset + length} is greater than the size of {@code sources}.
     * @throws IOException
     *             if another I/O error occurs.
     * @throws NotYetConnectedException
     *             if this channel is not yet connected.
     * @see java.nio.channels.GatheringByteChannel#write(java.nio.ByteBuffer[],
     *      int, int)
     */
    public abstract long write(ByteBuffer[] sources, int offset, int length) throws IOException;

    /**
     * Writes bytes from all the given byte buffers to this socket channel.
     * <p>
     * Calling this method is equivalent to calling {@code write(sources, 0,
     * sources.length);}
     *
     * @param sources
     *            the buffers containing bytes to write.
     * @return the number of bytes actually written.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread during this write
     *             operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IOException
     *             if another I/O error occurs.
     * @throws NotYetConnectedException
     *             if this channel is not yet connected.
     * @see java.nio.channels.GatheringByteChannel#write(java.nio.ByteBuffer[])
     */
    public synchronized final long write(ByteBuffer[] sources) throws IOException {
        return write(sources, 0, sources.length);
    }
}
