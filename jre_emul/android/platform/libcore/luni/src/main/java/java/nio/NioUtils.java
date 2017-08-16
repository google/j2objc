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

import com.google.j2objc.LibraryNotLinkedError;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Set;

import static libcore.io.OsConstants.*;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

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

    /**
     * Helps bridge between io and nio.
     */
    public static FileChannel newFileChannel(Closeable ioObject, FileDescriptor fd, int mode) {
      ChannelFactory factory = ChannelFactory.INSTANCE;
      if (factory == null) {
        throw new LibraryNotLinkedError("Channel support", "jre_channels",
            "JavaNioChannelFactoryImpl");
      }
      return factory.newFileChannel(ioObject, fd, mode);
    }

    public static FileChannel newFileChannelSafe(Object stream, FileDescriptor fd, int mode) {
      ChannelFactory factory = ChannelFactory.INSTANCE;
      if (factory != null) {
        return factory.newFileChannel(stream, fd, mode);
      } else {
        return null;
      }
    }

    static interface ChannelFactory {
      FileChannel newFileChannel(Object stream, FileDescriptor fd, int mode);

      static final ChannelFactory INSTANCE = getChannelFactory();
    }

    // Native implementation avoids the use of Class.forName(). This code might end up invoked from
    // within the internals of Class.forName(), and re-invoking it causes deadlock.
    private static native ChannelFactory getChannelFactory() /*-[
      Class cls = NSClassFromString(@"JavaNioChannelFactoryImpl");
      if (cls) {
        return [[[cls alloc] init] autorelease];
      }
      return nil;
    ]-*/;

    /**
     * Exposes the array backing a non-direct ByteBuffer, even if the ByteBuffer is read-only.
     * Normally, attempting to access the array backing a read-only buffer throws.
     */
    public static byte[] unsafeArray(ByteBuffer b) {
        return b.array();
    }

    /**
     * Exposes the array offset for the array backing a non-direct ByteBuffer,
     * even if the ByteBuffer is read-only.
     */
    public static int unsafeArrayOffset(ByteBuffer b) {
        return b.arrayOffset();
    }
}
