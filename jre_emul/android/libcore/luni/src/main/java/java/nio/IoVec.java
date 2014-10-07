/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import libcore.io.Libcore;
import libcore.io.ErrnoException;

/**
 * Used to implement java.nio read(ByteBuffer[])/write(ByteBuffer[]) operations as POSIX readv(2)
 * and writev(2) calls.
 */
final class IoVec {
    enum Direction { READV, WRITEV };

    private final ByteBuffer[] byteBuffers;
    private final int offset;
    private final int bufferCount;

    private final Object[] ioBuffers;
    private final int[] offsets;
    private final int[] byteCounts;

    private final Direction direction;

    IoVec(ByteBuffer[] byteBuffers, int offset, int bufferCount, Direction direction) {
        this.byteBuffers = byteBuffers;
        this.offset = offset;
        this.bufferCount = bufferCount;
        this.direction = direction;
        this.ioBuffers = new Object[bufferCount];
        this.offsets = new int[bufferCount];
        this.byteCounts = new int[bufferCount];
    }

    int init() {
        int totalRemaining = 0;
        for (int i = 0; i < bufferCount; ++i) {
            ByteBuffer b = byteBuffers[i + offset];
            if (direction == Direction.READV) {
                b.checkWritable();
            }
            int remaining = b.remaining();
            if (b.isDirect()) {
                ioBuffers[i] = b;
                offsets[i] = b.position();
            } else {
                ioBuffers[i] = NioUtils.unsafeArray(b);
                offsets[i] = NioUtils.unsafeArrayOffset(b) + b.position();
            }
            byteCounts[i] = remaining;
            totalRemaining += remaining;
        }
        return totalRemaining;
    }

    int doTransfer(FileDescriptor fd) throws IOException {
        try {
            if (direction == Direction.READV) {
                int result = Libcore.os.readv(fd, ioBuffers, offsets, byteCounts);
                if (result == 0) {
                    result = -1;
                }
                return result;
            } else {
                return Libcore.os.writev(fd, ioBuffers, offsets, byteCounts);
            }
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    void didTransfer(int byteCount) {
        for (int i = 0; byteCount > 0 && i < bufferCount; ++i) {
            ByteBuffer b = byteBuffers[i + offset];
            if (byteCounts[i] < byteCount) {
                b.position(b.limit());
                byteCount -= byteCounts[i];
            } else {
                b.position((direction == Direction.WRITEV ? b.position() : 0) + byteCount);
                byteCount = 0;
            }
        }
    }
}
