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

import java.util.Arrays;

/*-[
#import "zlib.h"
]-*/

/**
 * The Adler-32 class is used to compute the {@code Adler32} checksum from a set
 * of data. Compared to {@link CRC32} it trades reliability for speed.
 * Refer to RFC 1950 for the specification.
 */
public class Adler32 implements Checksum {

    private long adler = 1;

    /**
     * Returns the {@code Adler32} checksum for all input received.
     *
     * @return The checksum for this instance.
     */
    public long getValue() {
        return adler;
    }

    /**
     * Reset this instance to its initial checksum.
     */
    public void reset() {
        adler = 1;
    }

    /**
     * Update this {@code Adler32} checksum with the single byte provided as
     * argument.
     *
     * @param i
     *            the byte to update checksum with.
     */
    public void update(int i) {
        adler = updateByteImpl(i, adler);
    }

    /**
     * Update this {@code Adler32} checksum using the contents of {@code buf}.
     *
     * @param buf
     *            bytes to update checksum with.
     */
    public void update(byte[] buf) {
        update(buf, 0, buf.length);
    }

    /**
     * Update this {@code Adler32} checksum with the contents of {@code buf},
     * starting from {@code offset} and reading {@code byteCount} bytes of data.
     */
    public void update(byte[] buf, int offset, int byteCount) {
        Arrays.checkOffsetAndCount(buf.length, offset, byteCount);
        adler = updateImpl(buf, offset, byteCount, adler);
    }

    private native long updateImpl(byte[] buf, int offset, int byteCount, long adler1) /*-[
        return adler32((uLong) adler1, (Bytef *) (buf->buffer_ + offset), (uInt) byteCount);
    ]-*/;

    private native long updateByteImpl(int val, long adler1) /*-[
        return adler32((uLong) adler1, (Bytef *) (&val), 1);
    ]-*/;
}
