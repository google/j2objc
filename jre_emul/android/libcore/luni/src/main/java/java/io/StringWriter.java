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
 * A specialized {@link Writer} that writes characters to a {@code StringBuffer}
 * in a sequential manner, appending them in the process. The result can later
 * be queried using the {@link #StringWriter(int)} or {@link #toString()}
 * methods.
 *
 * @see StringReader
 */
public class StringWriter extends Writer {

    private StringBuffer buf;

    /**
     * Constructs a new {@code StringWriter} which has a {@link StringBuffer}
     * allocated with the default size of 16 characters. The {@code
     * StringBuffer} is also the {@code lock} used to synchronize access to this
     * writer.
     */
    public StringWriter() {
        buf = new StringBuffer(16);
        lock = buf;
    }

    /**
     * Constructs a new {@code StringWriter} which has a {@link StringBuffer}
     * allocated with a size of {@code initialSize} characters. The {@code
     * StringBuffer} is also the {@code lock} used to synchronize access to this
     * writer.
     *
     * @param initialSize
     *            the initial size of the target string buffer.
     */
    public StringWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("initialSize < 0: " + initialSize);
        }
        buf = new StringBuffer(initialSize);
        lock = buf;
    }

    /**
     * Calling this method has no effect. In contrast to most {@code Writer} subclasses,
     * the other methods in {@code StringWriter} do not throw an {@code IOException} if
     * {@code close()} has been called.
     *
     * @throws IOException
     *             if an error occurs while closing this writer.
     */
    @Override
    public void close() throws IOException {
        /* empty */
    }

    /**
     * Calling this method has no effect.
     */
    @Override
    public void flush() {
        /* empty */
    }

    /**
     * Gets a reference to this writer's internal {@link StringBuffer}. Any
     * changes made to the returned buffer are reflected in this writer.
     *
     * @return a reference to this writer's internal {@code StringBuffer}.
     */
    public StringBuffer getBuffer() {
        return buf;
    }

    /**
     * Gets a copy of the contents of this writer as a string.
     *
     * @return this writer's contents as a string.
     */
    @Override
    public String toString() {
        return buf.toString();
    }

    /**
     * Writes {@code count} characters starting at {@code offset} in {@code buf}
     * to this writer's {@code StringBuffer}.
     *
     * @param chars
     *            the non-null character array to write.
     * @param offset
     *            the index of the first character in {@code chars} to write.
     * @param count
     *            the maximum number of characters to write.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if {@code
     *             offset + count} is greater than the size of {@code buf}.
     */
    @Override
    public void write(char[] chars, int offset, int count) {
        Arrays.checkOffsetAndCount(chars.length, offset, count);
        if (count == 0) {
            return;
        }
        buf.append(chars, offset, count);
    }

    /**
     * Writes one character to this writer's {@code StringBuffer}. Only the two
     * least significant bytes of the integer {@code oneChar} are written.
     *
     * @param oneChar
     *            the character to write to this writer's {@code StringBuffer}.
     */
    @Override
    public void write(int oneChar) {
        buf.append((char) oneChar);
    }

    /**
     * Writes the characters from the specified string to this writer's {@code
     * StringBuffer}.
     *
     * @param str
     *            the non-null string containing the characters to write.
     */
    @Override
    public void write(String str) {
        buf.append(str);
    }

    /**
     * Writes {@code count} characters from {@code str} starting at {@code
     * offset} to this writer's {@code StringBuffer}.
     *
     * @param str
     *            the non-null string containing the characters to write.
     * @param offset
     *            the index of the first character in {@code str} to write.
     * @param count
     *            the number of characters from {@code str} to write.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if {@code
     *             offset + count} is greater than the length of {@code str}.
     */
    @Override
    public void write(String str, int offset, int count) {
        String sub = str.substring(offset, offset + count);
        buf.append(sub);
    }

    /**
     * Appends the character {@code c} to this writer's {@code StringBuffer}.
     * This method works the same way as {@link #write(int)}.
     *
     * @param c
     *            the character to append to the target stream.
     * @return this writer.
     */
    @Override
    public StringWriter append(char c) {
        write(c);
        return this;
    }

    /**
     * Appends the character sequence {@code csq} to this writer's {@code
     * StringBuffer}. This method works the same way as {@code
     * StringWriter.write(csq.toString())}. If {@code csq} is {@code null}, then
     * the string "null" is written to the target stream.
     *
     * @param csq
     *            the character sequence appended to the target.
     * @return this writer.
     */
    @Override
    public StringWriter append(CharSequence csq) {
        if (csq == null) {
            csq = "null";
        }
        write(csq.toString());
        return this;
    }

    /**
     * Appends a subsequence of the character sequence {@code csq} to this
     * writer's {@code StringBuffer}. This method works the same way as {@code
     * StringWriter.writer(csq.subsequence(start, end).toString())}. If {@code
     * csq} is {@code null}, then the specified subsequence of the string "null"
     * will be written to the target.
     *
     * @param csq
     *            the character sequence appended to the target.
     * @param start
     *            the index of the first char in the character sequence appended
     *            to the target.
     * @param end
     *            the index of the character following the last character of the
     *            subsequence appended to the target.
     * @return this writer.
     * @throws IndexOutOfBoundsException
     *             if {@code start > end}, {@code start < 0}, {@code end < 0} or
     *             either {@code start} or {@code end} are greater or equal than
     *             the length of {@code csq}.
     */
    @Override
    public StringWriter append(CharSequence csq, int start, int end) {
        if (csq == null) {
            csq = "null";
        }
        String output = csq.subSequence(start, end).toString();
        write(output, 0, output.length());
        return this;
    }
}
