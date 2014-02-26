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
 * A {@code ReadableByteChannel} is a type of {@link Channel} that can read
 * bytes.
 * <p>
 * Read operations are synchronous on a {@code ReadableByteChannel}, that is,
 * if a read is already in progress on the channel then subsequent reads will
 * block until the first read completes. It is undefined whether non-read
 * operations will block.
 */
public interface ReadableByteChannel extends Channel {

    /**
     * Reads bytes from the channel into the given buffer.
     * <p>
     * The maximum number of bytes that will be read is the
     * {@link java.nio.Buffer#remaining() remaining} number of bytes in the
     * buffer when the method is invoked. The bytes will be read into the buffer
     * starting at the buffer's current
     * {@link java.nio.Buffer#position() position}.
     * <p>
     * The call may block if other threads are also attempting to read from the
     * same channel.
     * <p>
     * Upon completion, the buffer's {@code position} is updated to the end of
     * the bytes that were read. The buffer's
     * {@link java.nio.Buffer#limit() limit} is not changed.
     *
     * @param buffer
     *            the byte buffer to receive the bytes.
     * @return the number of bytes actually read.
     * @throws AsynchronousCloseException
     *             if another thread closes the channel during the read.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while the
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if the channel is closed.
     * @throws IOException
     *             another I/O error occurs, details are in the message.
     * @throws NonReadableChannelException
     *             if the channel was not opened for reading.
     */
    public int read(ByteBuffer buffer) throws IOException;
}
