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

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.InterruptibleChannel;

/**
 * {@code AbstractInterruptibleChannel} is the root class for interruptible
 * channels.
 * <p>
 * The basic usage pattern for an interruptible channel is to invoke
 * {@code begin()} before any I/O operation that potentially blocks
 * indefinitely, then {@code end(boolean)} after completing the operation. The
 * argument to the {@code end} method should indicate if the I/O operation has
 * actually completed so that any change may be visible to the invoker.
*/
public abstract class AbstractInterruptibleChannel implements Channel, InterruptibleChannel {

    private volatile boolean closed = false;

    volatile boolean interrupted = false;

    private final Runnable interruptAndCloseRunnable = new Runnable() {
        @Override public void run() {
            try {
                interrupted = true;
                AbstractInterruptibleChannel.this.close();
            } catch (IOException ignored) {
            }
        }
    };

    protected AbstractInterruptibleChannel() {
    }

    @Override public synchronized final boolean isOpen() {
        return !closed;
    }

    /**
     * Closes an open channel. If the channel is already closed then this method
     * has no effect, otherwise it closes the receiver via the
     * {@code implCloseChannel} method.
     * <p>
     * If an attempt is made to perform an operation on a closed channel then a
     * {@link java.nio.channels.ClosedChannelException} is thrown.
     * <p>
     * If multiple threads attempt to simultaneously close a channel, then only
     * one thread will run the closure code and the others will be blocked until
     * the first one completes.
     *
     * @throws IOException
     *             if a problem occurs while closing this channel.
     * @see java.nio.channels.Channel#close()
     */
    @Override public final void close() throws IOException {
        if (!closed) {
            synchronized (this) {
                if (!closed) {
                    closed = true;
                    implCloseChannel();
                }
            }
        }
    }

    /**
     * Indicates the beginning of a code section that includes an I/O operation
     * that is potentially blocking. After this operation, the application
     * should invoke the corresponding {@code end(boolean)} method.
     */
    protected final void begin() {
        Thread.currentThread().pushInterruptAction$(interruptAndCloseRunnable);
    }

    /**
     * Indicates the end of a code section that has been started with
     * {@code begin()} and that includes a potentially blocking I/O operation.
     *
     * @param success
     *            pass {@code true} if the blocking operation has succeeded and
     *            has had a noticeable effect; {@code false} otherwise.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread while this method
     *             is executing.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             method is executing.
     */
    protected final void end(boolean success) throws AsynchronousCloseException {
        Thread.currentThread().popInterruptAction$(interruptAndCloseRunnable);
        if (interrupted) {
            interrupted = false;
            throw new ClosedByInterruptException();
        }
        if (!success && closed) {
            throw new AsynchronousCloseException();
        }
    }

    /**
     * Implements the channel closing behavior.
     * <p>
     * Closes the channel with a guarantee that the channel is not currently
     * closed through another invocation of {@code close()} and that the method
     * is thread-safe.
     * <p>
     * Any outstanding threads blocked on I/O operations on this channel must be
     * released with either a normal return code, or by throwing an
     * {@code AsynchronousCloseException}.
     *
     * @throws IOException
     *             if a problem occurs while closing the channel.
     */
    protected abstract void implCloseChannel() throws IOException;
}
