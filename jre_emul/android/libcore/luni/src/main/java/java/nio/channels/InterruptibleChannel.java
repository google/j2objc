/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio.channels;

import java.io.IOException;

/**
 * Channels that implement this interface can be asynchronously closed and
 * interrupted.
 * <p>
 * A channel that can be asynchronously closed permits that a thread blocked on
 * an I/O operation (the I/O thread) can be released by another thread calling
 * the channel's {@link #close()} method. The I/O thread will throw an
 * {@link AsynchronousCloseException} and the channel will be closed.
 * <p>
 * A channel that is interruptible permits a thread blocked on an I/O operation
 * (the I/O thread) to be interrupted by another thread (by invoking
 * {@link Thread#interrupt()} on the I/O thread). When the I/O thread is
 * interrupted it will throw a {@link ClosedByInterruptException}, it will have
 * its interrupted status set and the channel will be closed. If the I/O thread
 * attempts to make an I/O call with the interrupt status set the call will
 * immediately fail with a {@link ClosedByInterruptException}.
 */
public interface InterruptibleChannel extends Channel {

    /**
     * Closes the channel.
     * <p>
     * Any threads that are blocked on I/O operations on this channel will be
     * interrupted with an {@link AsynchronousCloseException}. Otherwise, this
     * method behaves the same as defined in the {@code Channel} interface.
     *
     * @throws IOException
     *             if an I/O error occurs while closing the channel.
     */
    public void close() throws IOException;
}
