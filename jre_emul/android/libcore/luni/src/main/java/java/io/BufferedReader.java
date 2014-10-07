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
 * Wraps an existing {@link Reader} and <em>buffers</em> the input. Expensive
 * interaction with the underlying reader is minimized, since most (smaller)
 * requests can be satisfied by accessing the buffer alone. The drawback is that
 * some extra space is required to hold the buffer and that copying takes place
 * when filling that buffer, but this is usually outweighed by the performance
 * benefits.
 *
 * <p/>A typical application pattern for the class looks like this:<p/>
 *
 * <pre>
 * BufferedReader buf = new BufferedReader(new FileReader(&quot;file.java&quot;));
 * </pre>
 *
 * @see BufferedWriter
 * @since 1.1
 */
public class BufferedReader extends Reader {

    private Reader in;

    /**
     * The characters that can be read and refilled in bulk. We maintain three
     * indices into this buffer:<pre>
     *     { X X X X X X X X X X X X - - }
     *           ^     ^             ^
     *           |     |             |
     *         mark   pos           end</pre>
     * Pos points to the next readable character. End is one greater than the
     * last readable character. When {@code pos == end}, the buffer is empty and
     * must be {@link #fillBuf() filled} before characters can be read.
     *
     * <p>Mark is the value pos will be set to on calls to {@link #reset}. Its
     * value is in the range {@code [0...pos]}. If the mark is {@code -1}, the
     * buffer cannot be reset.
     *
     * <p>MarkLimit limits the distance between the mark and the pos. When this
     * limit is exceeded, {@link #reset} is permitted (but not required) to
     * throw an exception. For shorter distances, {@link #reset} shall not throw
     * (unless the reader is closed).
     */
    private char[] buf;

    private int pos;

    private int end;

    private int mark = -1;

    private int markLimit = -1;

    /**
     * readLine returns a line as soon as it sees '\n' or '\r'. In the latter
     * case, there might be a following '\n' that should be treated as part of
     * the same line ending. Both readLine and all read methods are supposed
     * to skip the '\n' (and clear this field) but only readLine looks for '\r'
     * and sets it.
     */
    private boolean lastWasCR;

    /**
     * We also need to keep the 'lastWasCR' state for the mark position, in case
     * we reset to there.
     */
    private boolean markedLastWasCR;

    /**
     * Constructs a new {@code BufferedReader}, providing {@code in} with a buffer
     * of 8192 characters.
     *
     * @param in the {@code Reader} the buffer reads from.
     */
    public BufferedReader(Reader in) {
        this(in, 8192);
    }

    /**
     * Constructs a new {@code BufferedReader}, providing {@code in} with {@code size} characters
     * of buffer.
     *
     * @param in the {@code InputStream} the buffer reads from.
     * @param size the size of buffer in characters.
     * @throws IllegalArgumentException if {@code size <= 0}.
     */
    public BufferedReader(Reader in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.in = in;
        buf = new char[size];
    }

    /**
     * Closes this reader. This implementation closes the buffered source reader
     * and releases the buffer. Nothing is done if this reader has already been
     * closed.
     *
     * @throws IOException
     *             if an error occurs while closing this reader.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (!isClosed()) {
                in.close();
                buf = null;
            }
        }
    }

    /**
     * Populates the buffer with data. It is an error to call this method when
     * the buffer still contains data; ie. if {@code pos < end}.
     *
     * @return the number of chars read into the buffer, or -1 if the end of the
     *      source stream has been reached.
     */
    private int fillBuf() throws IOException {
        // assert(pos == end);

        if (mark == -1 || (pos - mark >= markLimit)) {
            /* mark isn't set or has exceeded its limit. use the whole buffer */
            int result = in.read(buf, 0, buf.length);
            if (result > 0) {
                mark = -1;
                pos = 0;
                end = result;
            }
            return result;
        }

        if (mark == 0 && markLimit > buf.length) {
            /* the only way to make room when mark=0 is by growing the buffer */
            int newLength = buf.length * 2;
            if (newLength > markLimit) {
                newLength = markLimit;
            }
            char[] newbuf = new char[newLength];
            System.arraycopy(buf, 0, newbuf, 0, buf.length);
            buf = newbuf;
        } else if (mark > 0) {
            /* make room by shifting the buffered data to left mark positions */
            System.arraycopy(buf, mark, buf, 0, buf.length - mark);
            pos -= mark;
            end -= mark;
            mark = 0;
        }

        /* Set the new position and mark position */
        int count = in.read(buf, pos, buf.length - pos);
        if (count != -1) {
            end += count;
        }
        return count;
    }

    /**
     * Indicates whether or not this reader is closed.
     *
     * @return {@code true} if this reader is closed, {@code false}
     *         otherwise.
     */
    private boolean isClosed() {
        return buf == null;
    }

    /**
     * Sets a mark position in this reader. The parameter {@code markLimit}
     * indicates how many characters can be read before the mark is invalidated.
     * Calling {@code reset()} will reposition the reader back to the marked
     * position if {@code markLimit} has not been surpassed.
     *
     * @param markLimit
     *            the number of characters that can be read before the mark is
     *            invalidated.
     * @throws IllegalArgumentException
     *             if {@code markLimit < 0}.
     * @throws IOException
     *             if an error occurs while setting a mark in this reader.
     * @see #markSupported()
     * @see #reset()
     */
    @Override
    public void mark(int markLimit) throws IOException {
        if (markLimit < 0) {
            throw new IllegalArgumentException("markLimit < 0:" + markLimit);
        }
        synchronized (lock) {
            checkNotClosed();
            this.markLimit = markLimit;
            this.mark = pos;
            this.markedLastWasCR = lastWasCR;
        }
    }

    private void checkNotClosed() throws IOException {
        if (isClosed()) {
            throw new IOException("BufferedReader is closed");
        }
    }

    /**
     * Indicates whether this reader supports the {@code mark()} and
     * {@code reset()} methods. This implementation returns {@code true}.
     *
     * @return {@code true} for {@code BufferedReader}.
     * @see #mark(int)
     * @see #reset()
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads a single character from this reader and returns it with the two
     * higher-order bytes set to 0. If possible, BufferedReader returns a
     * character from the buffer. If there are no characters available in the
     * buffer, it fills the buffer and then returns a character. It returns -1
     * if there are no more characters in the source reader.
     *
     * @return the character read or -1 if the end of the source reader has been
     *         reached.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            checkNotClosed();
            int ch = readChar();
            if (lastWasCR && ch == '\n') {
                ch = readChar();
            }
            lastWasCR = false;
            return ch;
        }
    }

    private int readChar() throws IOException {
        if (pos < end || fillBuf() != -1) {
            return buf[pos++];
        }
        return -1;
    }

    /**
     * Reads up to {@code length} characters from this reader and stores them
     * at {@code offset} in the character array {@code buffer}. Returns the
     * number of characters actually read or -1 if the end of the source reader
     * has been reached. If all the buffered characters have been used, a mark
     * has not been set and the requested number of characters is larger than
     * this readers buffer size, BufferedReader bypasses the buffer and simply
     * places the results directly into {@code buffer}.
     *
     * @throws IndexOutOfBoundsException
     *     if {@code offset < 0 || length < 0 || offset + length > buffer.length}.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        synchronized (lock) {
            checkNotClosed();
            Arrays.checkOffsetAndCount(buffer.length, offset, length);
            if (length == 0) {
                return 0;
            }

            maybeSwallowLF();

            int outstanding = length;
            while (outstanding > 0) {
                // If there are chars in the buffer, grab those first.
                int available = end - pos;
                if (available > 0) {
                    int count = available >= outstanding ? outstanding : available;
                    System.arraycopy(buf, pos, buffer, offset, count);
                    pos += count;
                    offset += count;
                    outstanding -= count;
                }

                /*
                 * Before attempting to read from the underlying stream, make
                 * sure we really, really want to. We won't bother if we're
                 * done, or if we've already got some chars and reading from the
                 * underlying stream would block.
                 */
                if (outstanding == 0 || (outstanding < length && !in.ready())) {
                    break;
                }

                // assert(pos == end);

                /*
                 * If we're unmarked and the requested size is greater than our
                 * buffer, read the chars directly into the caller's buffer. We
                 * don't read into smaller buffers because that could result in
                 * a many reads.
                 */
                if ((mark == -1 || (pos - mark >= markLimit)) && outstanding >= buf.length) {
                    int count = in.read(buffer, offset, outstanding);
                    if (count > 0) {
                        outstanding -= count;
                        mark = -1;
                    }
                    break; // assume the source stream gave us all that it could
                }

                if (fillBuf() == -1) {
                    break; // source is exhausted
                }
            }

            int count = length - outstanding;
            if (count > 0) {
                return count;
            }
            return -1;
        }
    }

    /**
     * Peeks at the next input character, refilling the buffer if necessary. If
     * this character is a newline character ("\n"), it is discarded.
     */
    final void chompNewline() throws IOException {
        if ((pos != end || fillBuf() != -1) && buf[pos] == '\n') {
            ++pos;
        }
    }

    // If the last character was CR and the next character is LF, skip it.
    private void maybeSwallowLF() throws IOException {
        if (lastWasCR) {
            chompNewline();
            lastWasCR = false;
        }
    }

    /**
     * Returns the next line of text available from this reader. A line is
     * represented by zero or more characters followed by {@code '\n'},
     * {@code '\r'}, {@code "\r\n"} or the end of the reader. The string does
     * not include the newline sequence.
     *
     * @return the contents of the line or {@code null} if no characters were
     *         read before the end of the reader has been reached.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    public String readLine() throws IOException {
        synchronized (lock) {
            checkNotClosed();

            maybeSwallowLF();

            // Do we have a whole line in the buffer?
            for (int i = pos; i < end; ++i) {
                char ch = buf[i];
                if (ch == '\n' || ch == '\r') {
                    String line = new String(buf, pos, i - pos);
                    pos = i + 1;
                    lastWasCR = (ch == '\r');
                    return line;
                }
            }

            // Accumulate buffers in a StringBuilder until we've read a whole line.
            StringBuilder result = new StringBuilder(end - pos + 80);
            result.append(buf, pos, end - pos);
            while (true) {
                pos = end;
                if (fillBuf() == -1) {
                    // If there's no more input, return what we've read so far, if anything.
                    return (result.length() > 0) ? result.toString() : null;
                }

                // Do we have a whole line in the buffer now?
                for (int i = pos; i < end; ++i) {
                    char ch = buf[i];
                    if (ch == '\n' || ch == '\r') {
                        result.append(buf, pos, i - pos);
                        pos = i + 1;
                        lastWasCR = (ch == '\r');
                        return result.toString();
                    }
                }

                // Add this whole buffer to the line-in-progress and try again...
                result.append(buf, pos, end - pos);
            }
        }
    }

    /**
     * Indicates whether this reader is ready to be read without blocking.
     *
     * @return {@code true} if this reader will not block when {@code read} is
     *         called, {@code false} if unknown or blocking will occur.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     * @see #read()
     * @see #read(char[], int, int)
     * @see #readLine()
     */
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            checkNotClosed();
            return ((end - pos) > 0) || in.ready();
        }
    }

    /**
     * Resets this reader's position to the last {@code mark()} location.
     * Invocations of {@code read()} and {@code skip()} will occur from this new
     * location.
     *
     * @throws IOException
     *             if this reader is closed or no mark has been set.
     * @see #mark(int)
     * @see #markSupported()
     */
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            checkNotClosed();
            if (mark == -1) {
                throw new IOException("Invalid mark");
            }
            this.pos = mark;
            this.lastWasCR = this.markedLastWasCR;
        }
    }

    /**
     * Skips at most {@code charCount} chars in this stream. Subsequent calls to
     * {@code read} will not return these chars unless {@code reset} is
     * used.
     *
     * <p>Skipping characters may invalidate a mark if {@code markLimit}
     * is surpassed.
     *
     * @return the number of characters actually skipped.
     * @throws IllegalArgumentException if {@code charCount < 0}.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    @Override
    public long skip(long charCount) throws IOException {
        if (charCount < 0) {
            throw new IllegalArgumentException("charCount < 0: " + charCount);
        }
        synchronized (lock) {
            checkNotClosed();
            if (end - pos >= charCount) {
                pos += charCount;
                return charCount;
            }

            long read = end - pos;
            pos = end;
            while (read < charCount) {
                if (fillBuf() == -1) {
                    return read;
                }
                if (end - pos >= charCount - read) {
                    pos += charCount - read;
                    return charCount;
                }
                // Couldn't get all the characters, skip what we read
                read += (end - pos);
                pos = end;
            }
            return charCount;
        }
    }
}
