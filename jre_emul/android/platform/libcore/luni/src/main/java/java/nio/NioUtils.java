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

import static libcore.io.OsConstants.O_ACCMODE;
import static libcore.io.OsConstants.O_RDONLY;
import static libcore.io.OsConstants.O_WRONLY;

import dalvik.annotation.compat.UnsupportedAppUsage;
import java.io.FileDescriptor;
import java.nio.channels.FileChannel;
import sun.nio.ch.FileChannelImpl;

/**
 * @hide internal use only
 */
public final class NioUtils {
    private NioUtils() {
    }

    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static void freeDirectBuffer(ByteBuffer buffer) {
        if (buffer == null) {
            return;
        }

        DirectByteBuffer dbb = (DirectByteBuffer) buffer;
        // Run the cleaner early, if one is defined.
        if (dbb.cleaner != null) {
            dbb.cleaner.clean();
        }

        dbb.memoryRef.free();
    }

    /* J2ObjC: unused.
     * Returns the int file descriptor from within the given FileChannel 'fc'.
    public static FileDescriptor getFD(FileChannel fc) {
        return ((FileChannelImpl) fc).fd;
    }
    */

    public FileChannel newFileChannel(Object ioObject, FileDescriptor fd, int mode) {
        boolean readable = (mode & O_ACCMODE) != O_WRONLY;
        boolean writable = (mode & O_ACCMODE) != O_RDONLY;
        return FileChannelImpl.open(fd, null, readable, writable, ioObject);
    }

    /**
     * Exposes the array backing a non-direct ByteBuffer, even if the ByteBuffer is read-only.
     * Normally, attempting to access the array backing a read-only buffer throws.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static byte[] unsafeArray(ByteBuffer b) {
        return b.array();
    }

    /**
     * Exposes the array offset for the array backing a non-direct ByteBuffer,
     * even if the ByteBuffer is read-only.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static int unsafeArrayOffset(ByteBuffer b) {
        return b.arrayOffset();
    }
}
