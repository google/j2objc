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
import java.nio.ByteBuffer;

/**
 * The interface for channels that can write a set of buffers in a single
 * operation. The corresponding interface for read operations is
 * {@link ScatteringByteChannel}.
 */
public interface GatheringByteChannel extends WritableByteChannel {

    /**
     * Writes bytes from all the given buffers to a channel.
     * <p>
     * This method is equivalent to: {@code write(buffers, 0, buffers.length);}
     *
     * @param buffers
     *            the buffers containing bytes to be written.
     * @return the number of bytes actually written.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread during this write
     *             operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if the channel is closed.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if
     *             {@code offset + length} is greater than the size of
     *             {@code buffers}.
     * @throws IOException
     *             if another I/O error occurs; details are in the message.
     * @throws NonWritableChannelException
     *             if the channel has not been opened in a mode that permits
     *             writing.
     */
    public long write(ByteBuffer[] buffers) throws IOException;

    /**
     * Attempts to write all <code>remaining()</code> bytes from {@code length}
     * byte buffers, in order, starting at {@code buffers[offset]}. The number
     * of bytes actually written is returned.
     * <p>
     * If a write operation is in progress, subsequent threads will block until
     * the write is completed and then contend for the ability to write.
     *
     * @param buffers
     *            the array of byte buffers that is the source for bytes written
     *            to the channel.
     * @param offset
     *            the index of the first buffer in {@code buffers }to get bytes
     *            from.
     * @param length
     *            the number of buffers to get bytes from.
     * @return the number of bytes actually written.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread during this write
     *             operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if the channel is closed.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if
     *             {@code offset + length} is greater than the size of
     *             {@code buffers}.
     * @throws IOException
     *             if another I/O error occurs; details are in the message.
     * @throws NonWritableChannelException
     *             if the channel was not opened for writing.
     */
    public long write(ByteBuffer[] buffers, int offset, int length)
            throws IOException;
}
