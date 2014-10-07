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
 * A specialized {@link Reader} that reads characters from a {@code String} in
 * a sequential manner.
 *
 * @see StringWriter
 */
public class StringReader extends Reader {
    private String str;

    private int markpos = -1;

    private int pos;

    private int count;

    /**
     * Construct a new {@code StringReader} with {@code str} as source. The size
     * of the reader is set to the {@code length()} of the string and the Object
     * to synchronize access through is set to {@code str}.
     *
     * @param str
     *            the source string for this reader.
     */
    public StringReader(String str) {
        this.str = str;
        this.count = str.length();
    }

    /**
     * Closes this reader. Once it is closed, read operations on this reader
     * will throw an {@code IOException}. Only the first invocation of this
     * method has any effect.
     */
    @Override
    public void close() {
        str = null;
    }

    /**
     * Returns a boolean indicating whether this reader is closed.
     *
     * @return {@code true} if closed, otherwise {@code false}.
     */
    private boolean isClosed() {
        return str == null;
    }

    /**
     * Sets a mark position in this reader. The parameter {@code readLimit} is
     * ignored for this class. Calling {@code reset()} will reposition the
     * reader back to the marked position.
     *
     * @param readLimit
     *            ignored for {@code StringReader} instances.
     * @throws IllegalArgumentException
     *             if {@code readLimit < 0}.
     * @throws IOException
     *             if this reader is closed.
     * @see #markSupported()
     * @see #reset()
     */
    @Override
    public void mark(int readLimit) throws IOException {
        if (readLimit < 0) {
            throw new IllegalArgumentException("readLimit < 0: " + readLimit);
        }

        synchronized (lock) {
            checkNotClosed();
            markpos = pos;
        }
    }

    private void checkNotClosed() throws IOException {
        if (isClosed()) {
            throw new IOException("StringReader is closed");
        }
    }

    /**
     * Indicates whether this reader supports the {@code mark()} and {@code
     * reset()} methods. This implementation returns {@code true}.
     *
     * @return always {@code true}.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads a single character from the source string and returns it as an
     * integer with the two higher-order bytes set to 0. Returns -1 if the end
     * of the source string has been reached.
     *
     * @return the character read or -1 if the end of the source string has been
     *         reached.
     * @throws IOException
     *             if this reader is closed.
     */
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            checkNotClosed();
            if (pos != count) {
                return str.charAt(pos++);
            }
            return -1;
        }
    }

    /**
     * Reads up to {@code count} characters from the source string and stores
     * them at {@code offset} in the character array {@code buffer}. Returns the
     * number of characters actually read or -1 if the end of the source string
     * has been reached.
     *
     * @throws IndexOutOfBoundsException
                if {@code offset < 0 || count < 0 || offset + count > buffer.length}.
     * @throws IOException
     *             if this reader is closed.
     */
    @Override
    public int read(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            checkNotClosed();
            Arrays.checkOffsetAndCount(buffer.length, offset, count);
            if (count == 0) {
                return 0;
            }
            if (pos == this.count) {
                return -1;
            }
            int end = pos + count > this.count ? this.count : pos + count;
            str.getChars(pos, end, buffer, offset);
            int read = end - pos;
            pos = end;
            return read;
        }
    }

    /**
     * Indicates whether this reader is ready to be read without blocking. This
     * implementation always returns {@code true}.
     *
     * @return always {@code true}.
     * @throws IOException
     *             if this reader is closed.
     * @see #read()
     * @see #read(char[], int, int)
     */
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            checkNotClosed();
            return true;
        }
    }

    /**
     * Resets this reader's position to the last {@code mark()} location.
     * Invocations of {@code read()} and {@code skip()} will occur from this new
     * location. If this reader has not been marked, it is reset to the
     * beginning of the source string.
     *
     * @throws IOException
     *             if this reader is closed.
     * @see #mark(int)
     * @see #markSupported()
     */
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            checkNotClosed();
            pos = markpos != -1 ? markpos : 0;
        }
    }

    /**
     * Moves {@code charCount} characters in the source string. Unlike the {@link
     * Reader#skip(long) overridden method}, this method may skip negative skip
     * distances: this rewinds the input so that characters may be read again.
     * When the end of the source string has been reached, the input cannot be
     * rewound.
     *
     * @param charCount
     *            the maximum number of characters to skip. Positive values skip
     *            forward; negative values skip backward.
     * @return the number of characters actually skipped. This is bounded below
     *            by the number of characters already read and above by the
     *            number of characters remaining:<br> {@code -(num chars already
     *            read) <= distance skipped <= num chars remaining}.
     * @throws IOException
     *             if this reader is closed.
     * @see #mark(int)
     * @see #markSupported()
     * @see #reset()
     */
    @Override
    public long skip(long charCount) throws IOException {
        synchronized (lock) {
            checkNotClosed();

            int minSkip = -pos;
            int maxSkip = count - pos;

            if (maxSkip == 0 || charCount > maxSkip) {
                charCount = maxSkip; // no rewinding if we're at the end
            } else if (charCount < minSkip) {
                charCount = minSkip;
            }

            pos += charCount;
            return charCount;
        }
    }
}
