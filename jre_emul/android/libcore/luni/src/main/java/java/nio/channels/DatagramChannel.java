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
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * A {@code DatagramChannel} is a selectable channel that represents a partial
 * abstraction of a datagram socket. The {@code socket} method of this class can
 * return the related {@code DatagramSocket} instance, which can handle the
 * socket.
 * <p>
 * A datagram channel is open but not connected when created with the
 * {@code open()} method. After it is connected, it will keep the connected
 * status until it is disconnected or closed. The benefit of a connected channel
 * is the reduced effort of security checks during send and receive. When
 * invoking {@code read} or {@code write}, a connected channel is required.
 * <p>
 * Datagram channels are thread-safe; only one thread can read or write at the
 * same time.
 */
public abstract class DatagramChannel extends AbstractSelectableChannel
        implements ByteChannel, ScatteringByteChannel, GatheringByteChannel {

    /**
     * Constructs a new {@code DatagramChannel}.
     *
     * @param selectorProvider
     *            an instance of SelectorProvider.
     */
    protected DatagramChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
    }

    /**
     * Creates an opened and not-connected datagram channel.
     * <p>
     * This channel is created by calling the <code>openDatagramChannel</code>
     * method of the default {@link SelectorProvider} instance.
     *
     * @return the new channel which is open but not connected.
     * @throws IOException
     *             if some I/O error occurs.
     */
    public static DatagramChannel open() throws IOException {
        return SelectorProvider.provider().openDatagramChannel();
    }

    /**
     * Gets the valid operations of this channel. Datagram channels support read
     * and write operations, so this method returns (
     * <code>SelectionKey.OP_READ</code> | <code>SelectionKey.OP_WRITE</code> ).
     *
     * @see java.nio.channels.SelectableChannel#validOps()
     * @return valid operations in bit-set.
     */
    @Override
    public final int validOps() {
        return (SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * Returns the related datagram socket of this channel, which does not
     * define additional public methods to those defined by
     * {@link DatagramSocket}.
     *
     * @return the related DatagramSocket instance.
     */
    public abstract DatagramSocket socket();

    /**
     * Returns whether this channel's socket is connected or not.
     *
     * @return <code>true</code> if this channel's socket is connected;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isConnected();

    /**
     * Connects the socket of this channel to a remote address, which is the
     * only communication peer for getting and sending datagrams after being
     * connected.
     * <p>
     * This method can be called at any time without affecting the read and
     * write operations being processed at the time the method is called. The
     * connection status does not change until the channel is disconnected or
     * closed.
     * <p>
     * This method executes the same security checks as the connect method of
     * the {@link DatagramSocket} class.
     *
     * @param address
     *            the address to be connected to.
     * @return this channel.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set and the channel will be closed.
     * @throws IOException
     *             if some other I/O error occurs.
     */
    public abstract DatagramChannel connect(SocketAddress address)
            throws IOException;

    /**
     * Disconnects the socket of this channel, which has been connected before
     * in order to send and receive datagrams.
     * <p>
     * This method can be called at any time without affecting the read and
     * write operations being underway. It does not have any effect if the
     * socket is not connected or the channel is closed.
     *
     * @return this channel.
     * @throws IOException
     *             some other I/O error occurs.
     */
    public abstract DatagramChannel disconnect() throws IOException;

    /**
     * Gets a datagram from this channel.
     * <p>
     * This method transfers a datagram from the channel into the target byte
     * buffer. If this channel is in blocking mode, it waits for the datagram
     * and returns its address when it is available. If this channel is in
     * non-blocking mode and no datagram is available, it returns {@code null}
     * immediately. The transfer starts at the current position of the buffer,
     * and if there is not enough space remaining in the buffer to store the
     * datagram then the part of the datagram that does not fit is discarded.
     * <p>
     * This method can be called at any time and it will block if there is
     * another thread that has started a read operation on the channel.
     * <p>
     * This method executes the same security checks as the receive method of
     * the {@link DatagramSocket} class.
     *
     * @param target
     *            the byte buffer to store the received datagram.
     * @return the address of the datagram if the transfer is performed, or null
     *         if the channel is in non-blocking mode and no datagram is
     *         available.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set and the channel will be closed.
     * @throws IOException
     *             some other I/O error occurs.
     */
    public abstract SocketAddress receive(ByteBuffer target) throws IOException;

    /**
     * Sends a datagram through this channel. The datagram consists of the
     * remaining bytes in {@code source}.
     * <p>
     * If this channel is in blocking mode then the datagram is sent as soon as
     * there is enough space in the underlying output buffer. If this channel is
     * in non-blocking mode then the datagram is only sent if there is enough
     * space in the underlying output buffer at that moment. The transfer action
     * is just like a regular write operation.
     * <p>
     * This method can be called at any time and it will block if another thread
     * has started a send operation on this channel.
     * <p>
     * This method executes the same security checks as the send method of the
     * {@link DatagramSocket} class.
     *
     * @param source
     *            the byte buffer with the datagram to be sent.
     * @param address
     *            the destination address for the datagram.
     * @return the number of bytes sent. This is the number of bytes remaining
     *         in {@code source} or zero if the channel is in non-blocking mode
     *         and there is not enough space for the datagram in the underlying
     *         output buffer.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set and the channel will be closed.
     * @throws IOException
     *             some other I/O error occurs.
     */
    public abstract int send(ByteBuffer source, SocketAddress address) throws IOException;

    /**
     * Reads a datagram from this channel into the byte buffer.
     * <p>
     * The precondition for calling this method is that the channel is connected
     * and the incoming datagram is from the connected address. If the buffer is
     * not big enough to store the datagram, the part of the datagram that does
     * not fit in the buffer is discarded. Otherwise, this method has the same
     * behavior as the {@code read} method in the {@link ReadableByteChannel}
     * interface.
     *
     * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
     * @param target
     *            the byte buffer to store the received datagram.
     * @return a non-negative number as the number of bytes read, or -1 as the
     *         read operation reaches the end of stream.
     * @throws NotYetConnectedException
     *             if the channel is not connected yet.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set and the channel will be closed.
     * @throws IOException
     *             some other I/O error occurs.
     */
    public abstract int read(ByteBuffer target) throws IOException;

    /**
     * Reads a datagram from this channel into an array of byte buffers.
     * <p>
     * The precondition for calling this method is that the channel is connected
     * and the incoming datagram is from the connected address. If the buffers
     * do not have enough remaining space to store the datagram, the part of the
     * datagram that does not fit in the buffers is discarded. Otherwise, this
     * method has the same behavior as the {@code read} method in the
     * {@link ScatteringByteChannel} interface.
     *
     * @see java.nio.channels.ScatteringByteChannel#read(java.nio.ByteBuffer[],
     *      int, int)
     * @param targets
     *            the byte buffers to store the received datagram.
     * @param offset
     *            a non-negative offset in the array of buffers, pointing to the
     *            starting buffer to store the bytes transferred, must not be
     *            bigger than {@code targets.length}.
     * @param length
     *            a non-negative length to indicate the maximum number of
     *            buffers to be filled, must not be bigger than
     *            {@code targets.length - offset}.
     * @return a non-negative number as the number of bytes read, or -1 if the
     *         read operation reaches the end of stream.
     * @throws NotYetConnectedException
     *             if the channel is not connected yet.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set and the channel will be closed.
     * @throws IOException
     *             some other I/O error occurs.
     */
    public abstract long read(ByteBuffer[] targets, int offset, int length)
            throws IOException;

    /**
     * Reads a datagram from this channel into an array of byte buffers.
     * <p>
     * The precondition for calling this method is that the channel is connected
     * and the incoming datagram is from the connected address. If the buffers
     * do not have enough remaining space to store the datagram, the part of the
     * datagram that does not fit in the buffers is discarded. Otherwise, this
     * method has the same behavior as the {@code read} method in the
     * {@link ScatteringByteChannel} interface.
     *
     * @see java.nio.channels.ScatteringByteChannel#read(java.nio.ByteBuffer[])
     * @param targets
     *            the byte buffers to store the received datagram.
     * @return a non-negative number as the number of bytes read, or -1 if the
     *         read operation reaches the end of stream.
     * @throws NotYetConnectedException
     *             if the channel is not connected yet.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set and the channel will be closed.
     * @throws IOException
     *             some other I/O error occurs.
     */
    public synchronized final long read(ByteBuffer[] targets)
            throws IOException {
        return read(targets, 0, targets.length);
    }

    /**
     * Writes a datagram from the byte buffer to this channel.
     * <p>
     * The precondition of calling this method is that the channel is connected
     * and the datagram is sent to the connected address. Otherwise, this method
     * has the same behavior as the {@code write} method in the
     * {@link WritableByteChannel} interface.
     *
     * @see java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer)
     * @param source
     *            the byte buffer as the source of the datagram.
     * @return a non-negative number of bytes written.
     * @throws NotYetConnectedException
     *             if the channel is not connected yet.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set and the channel will be closed.
     * @throws IOException
     *             some other I/O error occurs.
     */
    public abstract int write(ByteBuffer source) throws IOException;

    /**
     * Writes a datagram from the byte buffers to this channel.
     * <p>
     * The precondition of calling this method is that the channel is connected
     * and the datagram is sent to the connected address. Otherwise, this method
     * has the same behavior as the {@code write} method in the
     * {@link GatheringByteChannel} interface.
     *
     * @see java.nio.channels.GatheringByteChannel#write(java.nio.ByteBuffer[],
     *      int, int)
     * @param sources
     *            the byte buffers as the source of the datagram.
     * @param offset
     *            a non-negative offset in the array of buffers, pointing to the
     *            starting buffer to be retrieved, must be no larger than
     *            {@code sources.length}.
     * @param length
     *            a non-negative length to indicate the maximum number of
     *            buffers to be submitted, must be no bigger than
     *            {@code sources.length - offset}.
     * @return the number of bytes written. If this method is called, it returns
     *         the number of bytes that where remaining in the byte buffers. If
     *         the channel is in non-blocking mode and there was not enough
     *         space for the datagram in the buffer, it may return zero.
     * @throws NotYetConnectedException
     *             if the channel is not connected yet.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set and the channel will be closed.
     * @throws IOException
     *             some other I/O error occurs.
     */
    public abstract long write(ByteBuffer[] sources, int offset, int length)
            throws IOException;

    /**
     * Writes a datagram from the byte buffers to this channel.
     * <p>
     * The precondition of calling this method is that the channel is connected
     * and the datagram is sent to the connected address. Otherwise, this method
     * has the same behavior as the write method in the
     * {@link GatheringByteChannel} interface.
     *
     * @see java.nio.channels.GatheringByteChannel#write(java.nio.ByteBuffer[])
     * @param sources
     *            the byte buffers as the source of the datagram.
     * @return the number of bytes written. If this method is called, it returns
     *         the number of bytes that where remaining in the byte buffer. If
     *         the channel is in non-blocking mode and there was not enough
     *         space for the datagram in the buffer, it may return zero.
     * @throws NotYetConnectedException
     *             if the channel is not connected yet.
     * @throws ClosedChannelException
     *             if the channel is already closed.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set and the channel will be closed.
     * @throws IOException
     *             some other I/O error occurs.
     */
    public synchronized final long write(ByteBuffer[] sources)
            throws IOException {
        return write(sources, 0, sources.length);
    }
}
