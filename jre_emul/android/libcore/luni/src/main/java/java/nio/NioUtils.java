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

package java.nio;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.Set;

/**
 * @hide internal use only
 */
public final class NioUtils {
    private NioUtils() {
    }

    public static void freeDirectBuffer(ByteBuffer buffer) {
        if (buffer == null) {
            return;
        }
        ((DirectByteBuffer) buffer).free();
    }

    /**
     * Returns the int file descriptor from within the given FileChannel 'fc'.
     */
    public static FileDescriptor getFD(FileChannel fc) {
        return ((FileChannelImpl) fc).getFD();
    }

    /**
     * Helps bridge between io and nio.
     */
    public static FileChannel newFileChannel(Object stream, FileDescriptor fd, int mode) {
        return new FileChannelImpl(stream, fd, mode);
    }

    /**
     * Exposes the array backing a non-direct ByteBuffer, even if the ByteBuffer is read-only.
     * Normally, attempting to access the array backing a read-only buffer throws.
     */
    public static byte[] unsafeArray(ByteBuffer b) {
        return ((ByteArrayBuffer) b).backingArray;
    }

    /**
     * Exposes the array offset for the array backing a non-direct ByteBuffer,
     * even if the ByteBuffer is read-only.
     */
    public static int unsafeArrayOffset(ByteBuffer b) {
        return ((ByteArrayBuffer) b).arrayOffset;
    }

    /**
     * Sets the supplied option on the channel to have the value if option is a member of
     * allowedOptions.
     *
     * @throws IOException
     *          if the value could not be set due to IO errors.
     * @throws IllegalArgumentException
     *          if the socket option or the value is invalid.
     * @throws UnsupportedOperationException
     *          if the option is not a member of allowedOptions.
     * @throws ClosedChannelException
     *          if the channel is closed
     */
    public static <T> void setSocketOption(
            FileDescriptorChannel channel, Set<SocketOption<?>> allowedOptions,
            SocketOption<T> option, T value)
            throws IOException {

        if (!(option instanceof StandardSocketOptions.SocketOptionImpl)) {
            throw new IllegalArgumentException("SocketOption must come from StandardSocketOptions");
        }
        if (!allowedOptions.contains(option)) {
            throw new UnsupportedOperationException(
                    option + " is not supported for this type of socket");
        }
        if (!channel.getFD().valid()) {
            throw new ClosedChannelException();
        }
        ((StandardSocketOptions.SocketOptionImpl<T>) option).setValue(channel.getFD(), value);
    }

    /**
     * Gets the supplied option from the channel if option is a member of allowedOptions.
     *
     * @throws IOException
     *          if the value could not be read due to IO errors.
     * @throws IllegalArgumentException
     *          if the socket option is invalid.
     * @throws UnsupportedOperationException
     *          if the option is not a member of allowedOptions.
     * @throws ClosedChannelException
     *          if the channel is closed
     */
    public static <T> T getSocketOption(
            FileDescriptorChannel channel, Set<SocketOption<?>> allowedOptions,
            SocketOption<T> option)
            throws IOException {

        if (!(option instanceof StandardSocketOptions.SocketOptionImpl)) {
            throw new IllegalArgumentException("SocketOption must come from StandardSocketOptions");
        }
        if (!allowedOptions.contains(option)) {
            throw new UnsupportedOperationException(
                    option + " is not supported for this type of socket");
        }
        if (!channel.getFD().valid()) {
            throw new ClosedChannelException();
        }
        return ((StandardSocketOptions.SocketOptionImpl<T>) option).getValue(channel.getFD());
    }

}
