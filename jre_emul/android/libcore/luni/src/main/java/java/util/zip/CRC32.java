/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.util.zip;

/*-[
#import "zlib.h"
]-*/

import java.util.Arrays;

/**
 * The CRC32 class is used to compute a CRC32 checksum from data provided as
 * input value. See also {@link Adler32} which is almost as good, but cheaper.
 *
 * Ported to j2objc by Alexander Jarvis
 */
public class CRC32 implements Checksum {

    private long crc = 0L;

    long tbytes = 0L;

    /**
     * Returns the CRC32 checksum for all input received.
     *
     * @return The checksum for this instance.
     */
    public long getValue() {
        return crc;
    }

    /**
     * Resets the CRC32 checksum to it initial state.
     */
    public void reset() {
        tbytes = crc = 0;

    }

    /**
     * Updates this checksum with the byte value provided as integer.
     *
     * @param val
     *            represents the byte to update the checksum.
     */
    public void update(int val) {
        crc = updateByteImpl((byte) val, crc);
    }

    /**
     * Updates this checksum with the bytes contained in buffer {@code buf}.
     *
     * @param buf
     *            the buffer holding the data to update the checksum with.
     */
    public void update(byte[] buf) {
        update(buf, 0, buf.length);
    }

    /**
     * Update this {@code CRC32} checksum with the contents of {@code buf},
     * starting from {@code offset} and reading {@code byteCount} bytes of data.
     */
    public void update(byte[] buf, int offset, int byteCount) {
        Arrays.checkOffsetAndCount(buf.length, offset, byteCount);
        tbytes += byteCount;
        crc = updateImpl(buf, offset, byteCount, crc);
    }

    private native long updateImpl(byte[] buf, int offset, int byteCount, long crc1) /*-[
        return crc32((uLong) crc1, (Bytef *) (buf->buffer_ + offset), (uInt) byteCount);
    ]-*/;

    private native long updateByteImpl(byte val, long crc1) /*-[
        return crc32((uLong) crc1, (Bytef *) (&val), 1);
    ]-*/;
}
