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
import libcore.util.SneakyThrow;

/**
 * Wraps an existing {@link Writer} and <em>buffers</em> the output. Expensive
 * interaction with the underlying reader is minimized, since most (smaller)
 * requests can be satisfied by accessing the buffer alone. The drawback is that
 * some extra space is required to hold the buffer and that copying takes place
 * when filling that buffer, but this is usually outweighed by the performance
 * benefits.
 *
 * <p/>A typical application pattern for the class looks like this:<p/>
 *
 * <pre>
 * BufferedWriter buf = new BufferedWriter(new FileWriter(&quot;file.java&quot;));
 * </pre>
 *
 * @see BufferedReader
 */
public class BufferedWriter extends Writer {

    private Writer out;

    private char[] buf;

    private int pos;

    /**
     * Constructs a new {@code BufferedWriter}, providing {@code out} with a buffer
     * of 8192 chars.
     *
     * @param out the {@code Writer} the buffer writes to.
     */
    public BufferedWriter(Writer out) {
        this(out, 8192);
    }

    /**
     * Constructs a new {@code BufferedWriter}, providing {@code out} with {@code size} chars
     * of buffer.
     *
     * @param out the {@code OutputStream} the buffer writes to.
     * @param size the size of buffer in chars.
     * @throws IllegalArgumentException if {@code size <= 0}.
     */
    public BufferedWriter(Writer out, int size) {
        super(out);
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.out = out;
        this.buf = new char[size];
    }

    /**
     * Closes this writer. The contents of the buffer are flushed, the target
     * writer is closed, and the buffer is released. Only the first invocation
     * of close has any effect.
     *
     * @throws IOException
     *             if an error occurs while closing this writer.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                return;
            }

            Throwable thrown = null;
            try {
                flushInternal();
            } catch (Throwable e) {
                thrown = e;
            }
            buf = null;

            try {
                out.close();
            } catch (Throwable e) {
                if (thrown == null) {
                    thrown = e;
                }
            }
            out = null;

            if (thrown != null) {
                SneakyThrow.sneakyThrow(thrown);
            }
        }
    }

    /**
     * Flushes this writer. The contents of the buffer are committed to the
     * target writer and it is then flushed.
     *
     * @throws IOException
     *             if an error occurs while flushing this writer.
     */
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            checkNotClosed();
            flushInternal();
            out.flush();
        }
    }

    private void checkNotClosed() throws IOException {
        if (isClosed()) {
            throw new IOException("BufferedWriter is closed");
        }
    }

    /**
     * Flushes the internal buffer.
     */
    private void flushInternal() throws IOException {
        if (pos > 0) {
            out.write(buf, 0, pos);
        }
        pos = 0;
    }

    /**
     * Indicates whether this writer is closed.
     *
     * @return {@code true} if this writer is closed, {@code false} otherwise.
     */
    private boolean isClosed() {
        return out == null;
    }

    /**
     * Writes a newline to this writer. On Android, this is {@code "\n"}.
     * The target writer may or may not be flushed when a newline is written.
     *
     * @throws IOException
     *             if an error occurs attempting to write to this writer.
     */
    public void newLine() throws IOException {
        write(System.lineSeparator());
    }

    /**
     * Writes {@code count} characters starting at {@code offset} in
     * {@code buffer} to this writer. If {@code count} is greater than this
     * writer's buffer, then the buffer is flushed and the characters are
     * written directly to the target writer.
     *
     * @param buffer
     *            the array containing characters to write.
     * @param offset
     *            the start position in {@code buffer} for retrieving characters.
     * @param count
     *            the maximum number of characters to write.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if
     *             {@code offset + count} is greater than the size of
     *             {@code buffer}.
     * @throws IOException
     *             if this writer is closed or another I/O error occurs.
     */
    @Override
    public void write(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            checkNotClosed();
            if (buffer == null) {
                throw new NullPointerException("buffer == null");
            }
            Arrays.checkOffsetAndCount(buffer.length, offset, count);
            if (pos == 0 && count >= this.buf.length) {
                out.write(buffer, offset, count);
                return;
            }
            int available = this.buf.length - pos;
            if (count < available) {
                available = count;
            }
            if (available > 0) {
                System.arraycopy(buffer, offset, this.buf, pos, available);
                pos += available;
            }
            if (pos == this.buf.length) {
                out.write(this.buf, 0, this.buf.length);
                pos = 0;
                if (count > available) {
                    offset += available;
                    available = count - available;
                    if (available >= this.buf.length) {
                        out.write(buffer, offset, available);
                        return;
                    }

                    System.arraycopy(buffer, offset, this.buf, pos, available);
                    pos += available;
                }
            }
        }
    }

    /**
     * Writes the character {@code oneChar} to this writer. If the buffer
     * gets full by writing this character, this writer is flushed. Only the
     * lower two bytes of the integer {@code oneChar} are written.
     *
     * @param oneChar
     *            the character to write.
     * @throws IOException
     *             if this writer is closed or another I/O error occurs.
     */
    @Override
    public void write(int oneChar) throws IOException {
        synchronized (lock) {
            checkNotClosed();
            if (pos >= buf.length) {
                out.write(buf, 0, buf.length);
                pos = 0;
            }
            buf[pos++] = (char) oneChar;
        }
    }

    /**
     * Writes {@code count} characters starting at {@code offset} in {@code str}
     * to this writer. If {@code count} is greater than this writer's buffer,
     * then this writer is flushed and the remaining characters are written
     * directly to the target writer. If count is negative no characters are
     * written to the buffer. This differs from the behavior of the superclass.
     *
     * @param str
     *            the non-null String containing characters to write.
     * @param offset
     *            the start position in {@code str} for retrieving characters.
     * @param count
     *            maximum number of characters to write.
     * @throws IOException
     *             if this writer has already been closed or another I/O error
     *             occurs.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code offset + count} is greater
     *             than the length of {@code str}.
     */
    @Override
    public void write(String str, int offset, int count) throws IOException {
        synchronized (lock) {
            checkNotClosed();
            if (count <= 0) {
                return;
            }
            if (offset < 0 || offset > str.length() - count) {
                throw new StringIndexOutOfBoundsException(str, offset, count);
            }
            if (pos == 0 && count >= buf.length) {
                char[] chars = new char[count];
                str.getChars(offset, offset + count, chars, 0);
                out.write(chars, 0, count);
                return;
            }
            int available = buf.length - pos;
            if (count < available) {
                available = count;
            }
            if (available > 0) {
                str.getChars(offset, offset + available, buf, pos);
                pos += available;
            }
            if (pos == buf.length) {
                out.write(this.buf, 0, this.buf.length);
                pos = 0;
                if (count > available) {
                    offset += available;
                    available = count - available;
                    if (available >= buf.length) {
                        char[] chars = new char[count];
                        str.getChars(offset, offset + available, chars, 0);
                        out.write(chars, 0, available);
                        return;
                    }
                    str.getChars(offset, offset + available, buf, pos);
                    pos += available;
                }
            }
        }
    }
}
