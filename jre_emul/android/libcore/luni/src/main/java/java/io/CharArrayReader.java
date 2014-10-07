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
 * A specialized {@link Reader} for reading the contents of a char array.
 *
 * @see CharArrayWriter
 */
public class CharArrayReader extends Reader {
    /**
     * The buffer for characters.
     */
    protected char[] buf;

    /**
     * The current buffer position.
     */
    protected int pos;

    /**
     * The current mark position.
     */
    protected int markedPos = -1;

    /**
     * The ending index of the buffer.
     */
    protected int count;

    /**
     * Constructs a CharArrayReader on the char array {@code buf}. The size of
     * the reader is set to the length of the buffer and the object to to read
     * from is set to {@code buf}.
     *
     * @param buf
     *            the char array from which to read.
     */
    public CharArrayReader(char[] buf) {
        this.buf = buf;
        this.count = buf.length;
    }

    /**
     * Constructs a CharArrayReader on the char array {@code buf}. The size of
     * the reader is set to {@code length} and the start position from which to
     * read the buffer is set to {@code offset}.
     *
     * @param buf
     *            the char array from which to read.
     * @param offset
     *            the index of the first character in {@code buf} to read.
     * @param length
     *            the number of characters that can be read from {@code buf}.
     * @throws IllegalArgumentException
     *             if {@code offset < 0} or {@code length < 0}, or if
     *             {@code offset} is greater than the size of {@code buf} .
     */
    public CharArrayReader(char[] buf, int offset, int length) {
        /*
         * The spec of this constructor is broken. In defining the legal values
         * of offset and length, it doesn't consider buffer's length. And to be
         * compatible with the broken spec, we must also test whether
         * (offset + length) overflows.
         */
        if (offset < 0 || offset > buf.length || length < 0 || offset + length < 0) {
            throw new IllegalArgumentException();
        }
        this.buf = buf;
        this.pos = offset;
        this.markedPos = offset;

        /* This is according to spec */
        int bufferLength = buf.length;
        this.count = offset + length < bufferLength ? length : bufferLength;
    }

    /**
     * This method closes this CharArrayReader. Once it is closed, you can no
     * longer read from it. Only the first invocation of this method has any
     * effect.
     */
    @Override
    public void close() {
        synchronized (lock) {
            if (isOpen()) {
                buf = null;
            }
        }
    }

    /**
     * Indicates whether this reader is open.
     *
     * @return {@code true} if the reader is open, {@code false} otherwise.
     */
    private boolean isOpen() {
        return buf != null;
    }

    /**
     * Indicates whether this reader is closed.
     *
     * @return {@code true} if the reader is closed, {@code false} otherwise.
     */
    private boolean isClosed() {
        return buf == null;
    }

    /**
     * Sets a mark position in this reader. The parameter {@code readLimit} is
     * ignored for CharArrayReaders. Calling {@code reset()} will reposition the
     * reader back to the marked position provided the mark has not been
     * invalidated.
     *
     * @param readLimit
     *            ignored for CharArrayReaders.
     * @throws IOException
     *             if this reader is closed.
     */
    @Override
    public void mark(int readLimit) throws IOException {
        synchronized (lock) {
            checkNotClosed();
            markedPos = pos;
        }
    }

    private void checkNotClosed() throws IOException {
        if (isClosed()) {
            throw new IOException("CharArrayReader is closed");
        }
    }

    /**
     * Indicates whether this reader supports the {@code mark()} and
     * {@code reset()} methods.
     *
     * @return {@code true} for CharArrayReader.
     * @see #mark(int)
     * @see #reset()
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads a single character from this reader and returns it as an integer
     * with the two higher-order bytes set to 0. Returns -1 if no more
     * characters are available from this reader.
     *
     * @return the character read as an int or -1 if the end of the reader has
     *         been reached.
     * @throws IOException
     *             if this reader is closed.
     */
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            checkNotClosed();
            if (pos == count) {
                return -1;
            }
            return buf[pos++];
        }
    }

    /**
     * Reads up to {@code count} characters from this CharArrayReader and
     * stores them at {@code offset} in the character array {@code buffer}.
     * Returns the number of characters actually read or -1 if the end of reader
     * was encountered.
     *
     * @throws IndexOutOfBoundsException
     * if {@code offset < 0 || count < 0 || offset + count > buffer.length}.
     * @throws IOException
     *             if this reader is closed.
     */
    @Override
    public int read(char[] buffer, int offset, int count) throws IOException {
        Arrays.checkOffsetAndCount(buffer.length, offset, count);
        synchronized (lock) {
            checkNotClosed();
            if (pos < this.count) {
                int bytesRead = pos + count > this.count ? this.count - pos : count;
                System.arraycopy(this.buf, pos, buffer, offset, bytesRead);
                pos += bytesRead;
                return bytesRead;
            }
            return -1;
        }
    }

    /**
     * Indicates whether this reader is ready to be read without blocking.
     * Returns {@code true} if the next {@code read} will not block. Returns
     * {@code false} if this reader may or may not block when {@code read} is
     * called. The implementation in CharArrayReader always returns {@code true}
     * even when it has been closed.
     *
     * @return {@code true} if this reader will not block when {@code read} is
     *         called, {@code false} if unknown or blocking will occur.
     * @throws IOException
     *             if this reader is closed.
     */
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            checkNotClosed();
            return pos != count;
        }
    }

    /**
     * Resets this reader's position to the last {@code mark()} location.
     * Invocations of {@code read()} and {@code skip()} will occur from this new
     * location. If this reader has not been marked, it is reset to the
     * beginning of the string.
     *
     * @throws IOException
     *             if this reader is closed.
     */
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            checkNotClosed();
            pos = markedPos != -1 ? markedPos : 0;
        }
    }

    /**
     * Skips {@code charCount} characters in this reader. Subsequent calls to
     * {@code read} will not return these characters unless {@code reset}
     * is used. This method does nothing and returns 0 if {@code charCount <= 0}.
     *
     * @return the number of characters actually skipped.
     * @throws IOException
     *             if this reader is closed.
     */
    @Override
    public long skip(long charCount) throws IOException {
        synchronized (lock) {
            checkNotClosed();
            if (charCount <= 0) {
                return 0;
            }
            long skipped = 0;
            if (charCount < this.count - pos) {
                pos = pos + (int) charCount;
                skipped = charCount;
            } else {
                skipped = this.count - pos;
                pos = this.count;
            }
            return skipped;
        }
    }
}
