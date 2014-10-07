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
import libcore.io.Streams;

/**
 * Wraps an existing {@link InputStream} and counts the line terminators
 * encountered while reading the data. Line numbering starts at 0. Recognized
 * line terminator sequences are {@code '\r'}, {@code '\n'} and {@code "\r\n"}.
 * When using {@code read}, line terminator sequences are always translated into
 * {@code '\n'}.
 *
 * @deprecated Use {@link LineNumberReader} instead.
 */
@Deprecated
public class LineNumberInputStream extends FilterInputStream {

    private int lineNumber;

    private int markedLineNumber = -1;

    private int lastChar = -1;

    private int markedLastChar;

    /**
     * Constructs a new {@code LineNumberInputStream} on the {@link InputStream}
     * {@code in}. Line numbers are counted for all data read from this stream.
     *
     * <p><strong>Warning:</strong> passing a null source creates an invalid
     * {@code LineNumberInputStream}. All operations on such a stream will fail.
     *
     * @param in
     *            The non-null input stream to count line numbers.
     */
    public LineNumberInputStream(InputStream in) {
        super(in);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Note that the source stream may just be a sequence of {@code "\r\n"} bytes
     * which are converted into {@code '\n'} by this stream. Therefore,
     * {@code available} returns only {@code in.available() / 2} bytes as
     * result.
     */
    @Override
    public int available() throws IOException {
        return in.available() / 2 + (lastChar == -1 ? 0 : 1);
    }

    /**
     * Returns the current line number for this stream. Numbering starts at 0.
     *
     * @return the current line number.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Sets a mark position in this stream. The parameter {@code readlimit}
     * indicates how many bytes can be read before the mark is invalidated.
     * Sending {@code reset()} will reposition this stream back to the marked
     * position, provided that {@code readlimit} has not been surpassed.
     * The line number count will also be reset to the last marked
     * line number count.
     * <p>
     * This implementation sets a mark in the filtered stream.
     *
     * @param readlimit
     *            the number of bytes that can be read from this stream before
     *            the mark is invalidated.
     * @see #markSupported()
     * @see #reset()
     */
    @Override
    public void mark(int readlimit) {
        in.mark(readlimit);
        markedLineNumber = lineNumber;
        markedLastChar = lastChar;
    }

    /**
     * Reads a single byte from the filtered stream and returns it as an integer
     * in the range from 0 to 255. Returns -1 if the end of this stream has been
     * reached.
     * <p>
     * The line number count is incremented if a line terminator is encountered.
     * Recognized line terminator sequences are {@code '\r'}, {@code '\n'} and
     * {@code "\r\n"}. Line terminator sequences are always translated into
     * {@code '\n'}.
     *
     * @return the byte read or -1 if the end of the filtered stream has been
     *         reached.
     * @throws IOException
     *             if the stream is closed or another IOException occurs.
     */
    @SuppressWarnings("fallthrough")
    @Override
    public int read() throws IOException {
        int currentChar = lastChar;
        if (currentChar == -1) {
            currentChar = in.read();
        } else {
            lastChar = -1;
        }
        switch (currentChar) {
            case '\r':
                currentChar = '\n';
                lastChar = in.read();
                if (lastChar == '\n') {
                    lastChar = -1;
                }
                // fall through
            case '\n':
                lineNumber++;
        }
        return currentChar;
    }

    /**
     * Reads up to {@code byteCount} bytes from the filtered stream and stores
     * them in the byte array {@code buffer} starting at {@code byteOffset}.
     * Returns the number of bytes actually read or -1 if no bytes have been
     * read and the end of this stream has been reached.
     *
     * <p>The line number count is incremented if a line terminator is encountered.
     * Recognized line terminator sequences are {@code '\r'}, {@code '\n'} and
     * {@code "\r\n"}. Line terminator sequences are always translated into
     * {@code '\n'}.
     *
     * @throws IndexOutOfBoundsException
     *     if {@code byteOffset < 0 || byteCount < 0 || byteOffset + byteCount > buffer.length}.
     * @throws IOException
     *             if this stream is closed or another IOException occurs.
     * @throws NullPointerException
     *             if {@code buffer == null}.
     */
    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        Arrays.checkOffsetAndCount(buffer.length, byteOffset, byteCount);
        for (int i = 0; i < byteCount; ++i) {
            int currentChar;
            try {
                currentChar = read();
            } catch (IOException e) {
                if (i != 0) {
                    return i;
                }
                throw e;
            }
            if (currentChar == -1) {
                return i == 0 ? -1 : i;
            }
            buffer[byteOffset + i] = (byte) currentChar;
        }
        return byteCount;
    }

    /**
     * Resets this stream to the last marked location. It also resets the line
     * count to what is was when this stream was marked.
     *
     * @throws IOException
     *             if this stream is already closed, no mark has been set or the
     *             mark is no longer valid because more than {@code readlimit}
     *             bytes have been read since setting the mark.
     * @see #mark(int)
     * @see #markSupported()
     */
    @Override
    public void reset() throws IOException {
        in.reset();
        lineNumber = markedLineNumber;
        lastChar = markedLastChar;
    }

    /**
     * Sets the line number of this stream to the specified
     * {@code lineNumber}. Note that this may have side effects on the
     * line number associated with the last marked position.
     *
     * @param lineNumber
     *            the new lineNumber value.
     * @see #mark(int)
     * @see #reset()
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Skips {@code count} number of bytes in this stream. Subsequent
     * calls to {@code read} will not return these bytes unless {@code reset} is
     * used. This implementation skips {@code byteCount} bytes in the
     * filtered stream and increments the line number count whenever line
     * terminator sequences are skipped.
     *
     * @param byteCount
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * @throws IOException
     *             if this stream is closed or another IOException occurs.
     * @see #mark(int)
     * @see #read()
     * @see #reset()
     */
    @Override
    public long skip(long byteCount) throws IOException {
        return Streams.skipByReading(this, byteCount);
    }
}
