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
 * The interface for channels that can read data into a set of buffers in a
 * single operation. The corresponding interface for writes is
 * {@link GatheringByteChannel}.
 */
public interface ScatteringByteChannel extends ReadableByteChannel {

    /**
     * Reads bytes from this channel into the specified array of buffers.
     * <p>
     * This method is equivalent to {@code read(buffers, 0, buffers.length);}
     *
     * @param buffers
     *            the array of byte buffers to store the bytes being read.
     * @return the number of bytes actually read.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread during this read
     *             operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if the channel is closed.
     * @throws IOException
     *             if another I/O error occurs; details are in the message.
     * @throws NonWritableChannelException
     *             if the channel has not been opened in a mode that permits
     *             reading.
     */
    public long read(ByteBuffer[] buffers) throws IOException;

    /**
     * Attempts to read all {@code remaining()} bytes from {@code length} byte
     * buffers, in order, starting at {@code buffers[offset]}. The number of
     * bytes actually read is returned.
     * <p>
     * If a read operation is in progress, subsequent threads will block until
     * the read is completed and will then contend for the ability to read.
     *
     * @param buffers
     *            the array of byte buffers into which the bytes will be copied.
     * @param offset
     *            the index of the first buffer to store bytes in.
     * @param length
     *            the maximum number of buffers to store bytes in.
     * @return the number of bytes actually read.
     * @throws AsynchronousCloseException
     *             if the channel is closed by another thread during this read
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
     *             reading.
     */
    public long read(ByteBuffer[] buffers, int offset, int length)
            throws IOException;
}
