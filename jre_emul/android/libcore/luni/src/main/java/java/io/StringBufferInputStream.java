/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

import java.util.Arrays;

/**
 * A specialized {@link InputStream} that reads bytes from a {@code String} in
 * a sequential manner.
 *
 * @deprecated Use {@link StringReader} instead.
 */
@Deprecated
public class StringBufferInputStream extends InputStream {
    /**
     * The source string containing the data to read.
     */
    protected String buffer;

    /**
     * The total number of characters in the source string.
     */
    protected int count;

    /**
     * The current position within the source string.
     */
    protected int pos;

    /**
     * Construct a new {@code StringBufferInputStream} with {@code str} as
     * source. The size of the stream is set to the {@code length()} of the
     * string.
     *
     * @param str
     *            the source string for this stream.
     * @throws NullPointerException
     *             if {@code str} is {@code null}.
     */
    public StringBufferInputStream(String str) {
        if (str == null) {
            throw new NullPointerException("str == null");
        }
        buffer = str;
        count = str.length();
    }

    @Override
    public synchronized int available() {
        return count - pos;
    }

    /**
     * Reads a single byte from the source string and returns it as an integer
     * in the range from 0 to 255. Returns -1 if the end of the source string
     * has been reached.
     *
     * @return the byte read or -1 if the end of the source string has been
     *         reached.
     */
    @Override
    public synchronized int read() {
        return pos < count ? buffer.charAt(pos++) & 0xFF : -1;
    }

    @Override public synchronized int read(byte[] buffer, int byteOffset, int byteCount) {
        if (buffer == null) {
            throw new NullPointerException("buffer == null");
        }
        Arrays.checkOffsetAndCount(buffer.length, byteOffset, byteCount);
        if (byteCount == 0) {
            return 0;
        }

        int copylen = count - pos < byteCount ? count - pos : byteCount;
        for (int i = 0; i < copylen; ++i) {
            buffer[byteOffset + i] = (byte) this.buffer.charAt(pos + i);
        }
        pos += copylen;
        return copylen;
    }

    /**
     * Resets this stream to the beginning of the source string.
     */
    @Override
    public synchronized void reset() {
        pos = 0;
    }

    /**
     * Skips {@code charCount} characters in the source string. It does nothing and
     * returns 0 if {@code charCount} is negative. Less than {@code charCount} characters are
     * skipped if the end of the source string is reached before the operation
     * completes.
     *
     * @return the number of characters actually skipped.
     */
    @Override
    public synchronized long skip(long charCount) {
        if (charCount <= 0) {
            return 0;
        }

        int numskipped;
        if (this.count - pos < charCount) {
            numskipped = this.count - pos;
            pos = this.count;
        } else {
            numskipped = (int) charCount;
            pos += charCount;
        }
        return numskipped;
    }
}
