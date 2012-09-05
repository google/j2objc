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

/**
 * A specialized {@link Writer} for class for writing content to an (internal)
 * char array. As bytes are written to this writer, the char array may be
 * expanded to hold more characters. When the writing is considered to be
 * finished, a copy of the char array can be requested from the class.
 *
 * @see CharArrayReader
 */
public class CharArrayWriter extends Writer {

    /**
     * The buffer for characters.
     */
    protected char[] buf;

    /**
     * The ending index of the buffer.
     */
    protected int count;

    /**
     * Constructs a new {@code CharArrayWriter} which has a buffer allocated
     * with the default size of 32 characters. This buffer is also used as the
     * {@code lock} to synchronize access to this writer.
     */
    public CharArrayWriter() {
        super();
        buf = new char[32];
        lock = buf;
    }

    /**
     * Constructs a new {@code CharArrayWriter} which has a buffer allocated
     * with the size of {@code initialSize} characters. The buffer is also used
     * as the {@code lock} to synchronize access to this writer.
     * 
     * @param initialSize
     *            the initial size of this CharArrayWriters buffer.
     * @throws IllegalArgumentException
     *             if {@code initialSize < 0}.
     */
    public CharArrayWriter(int initialSize) {
        super();
        if (initialSize < 0) {
            throw new IllegalArgumentException("size must be >= 0");
        }
        buf = new char[initialSize];
        lock = buf;
    }

    /**
     * Closes this writer. The implementation in {@code CharArrayWriter} does nothing.
     */
    @Override
    public void close() {
        /* empty */
    }

    private void expand(int i) {
        /* Can the buffer handle @i more chars, if not expand it */
        if (count + i <= buf.length) {
            return;
        }

        int newLen = Math.max(2 * buf.length, count + i);
        char[] newbuf = new char[newLen];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
    }

    /**
     * Flushes this writer. The implementation in {@code CharArrayWriter} does nothing.
     */
    @Override
    public void flush() {
        /* empty */
    }

    /**
     * Resets this writer. The current write position is reset to the beginning
     * of the buffer. All written characters are lost and the size of this
     * writer is set to 0.
     */
    public void reset() {
        synchronized (lock) {
            count = 0;
        }
    }

    /**
     * Returns the size of this writer, that is the number of characters it
     * stores. This number changes if this writer is reset or when more
     * characters are written to it.
     * 
     * @return this CharArrayWriter's current size in characters.
     */
    public int size() {
        synchronized (lock) {
            return count;
        }
    }

    /**
     * Returns the contents of the receiver as a char array. The array returned
     * is a copy and any modifications made to this writer after calling this
     * method are not reflected in the result.
     * 
     * @return this CharArrayWriter's contents as a new char array.
     */
    public char[] toCharArray() {
        synchronized (lock) {
            char[] result = new char[count];
            System.arraycopy(buf, 0, result, 0, count);
            return result;
        }
    }

    /**
     * Returns the contents of this {@code CharArrayWriter} as a string. The
     * string returned is a copy and any modifications made to this writer after
     * calling this method are not reflected in the result.
     * 
     * @return this CharArrayWriters contents as a new string.
     */
    @Override
    public String toString() {
        synchronized (lock) {
            return new String(buf, 0, count);
        }
    }

    /**
     * Writes {@code count} characters starting at {@code offset} in {@code c}
     * to this writer.
     * 
     * @param c
     *            the non-null array containing characters to write.
     * @param offset
     *            the index of the first character in {@code buf} to write.
     * @param len
     *            maximum number of characters to write.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code len < 0}, or if
     *             {@code offset + len} is bigger than the size of {@code c}.
     */
    @Override
    public void write(char[] c, int offset, int len) {
        // avoid int overflow
        if (offset < 0 || offset > c.length || len < 0
                || len > c.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (lock) {
            expand(len);
            System.arraycopy(c, offset, this.buf, this.count, len);
            this.count += len;
        }
    }

    /**
     * Writes the specified character {@code oneChar} to this writer.
     * This implementation writes the two low order bytes of the integer
     * {@code oneChar} to the buffer.
     * 
     * @param oneChar
     *            the character to write.
     */
    @Override
    public void write(int oneChar) {
        synchronized (lock) {
            expand(1);
            buf[count++] = (char) oneChar;
        }
    }

    /**
     * Writes {@code count} number of characters starting at {@code offset} from
     * the string {@code str} to this CharArrayWriter.
     * 
     * @param str
     *            the non-null string containing the characters to write.
     * @param offset
     *            the index of the first character in {@code str} to write.
     * @param len
     *            the number of characters to retrieve and write.
     * @throws NullPointerException
     *             if {@code str} is {@code null}.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset < 0} or {@code len < 0}, or if
     *             {@code offset + len} is bigger than the length of
     *             {@code str}.
     */
    @Override
    public void write(String str, int offset, int len) {
        if (str == null) {
            throw new NullPointerException("buffer is null");
        }
        // avoid int overflow
        if (offset < 0 || offset > str.length() || len < 0
                || len > str.length() - offset) {
            throw new StringIndexOutOfBoundsException();
        }
        synchronized (lock) {
            expand(len);
            str.getChars(offset, offset + len, buf, this.count);
            this.count += len;
        }
    }

    /**
     * Writes the contents of this {@code CharArrayWriter} to another {@code
     * Writer}. The output is all the characters that have been written to the
     * receiver since the last reset or since it was created.
     * 
     * @param out
     *            the non-null {@code Writer} on which to write the contents.
     * @throws NullPointerException
     *             if {@code out} is {@code null}.
     * @throws IOException
     *             if an error occurs attempting to write out the contents.
     */
    public void writeTo(Writer out) throws IOException {
        synchronized (lock) {
            out.write(buf, 0, count);
        }
    }

    /**
     * Appends a char {@code c} to the {@code CharArrayWriter}. The method works
     * the same way as {@code write(c)}.
     * 
     * @param c
     *            the character appended to the CharArrayWriter.
     * @return this CharArrayWriter.
     */
    @Override
    public CharArrayWriter append(char c) {
        write(c);
        return this;
    }

    /**
     * Appends a {@code CharSequence} to the {@code CharArrayWriter}. The method
     * works the same way as {@code write(csq.toString())}. If {@code csq} is
     * {@code null}, then it will be substituted with the string {@code "null"}.
     * 
     * @param csq
     *            the {@code CharSequence} appended to the {@code
     *            CharArrayWriter}, may be {@code null}.
     * @return this CharArrayWriter.
     */
    @Override
    public CharArrayWriter append(CharSequence csq) {
        if (null == csq) {
            append(TOKEN_NULL, 0, TOKEN_NULL.length());
        } else {
            append(csq, 0, csq.length());
        }
        return this;
    }

    /**
     * Append a subsequence of a {@code CharSequence} to the {@code
     * CharArrayWriter}. The first and last characters of the subsequence are
     * specified by the parameters {@code start} and {@code end}. A call to
     * {@code CharArrayWriter.append(csq)} works the same way as {@code
     * CharArrayWriter.write(csq.subSequence(start, end).toString)}. If {@code
     * csq} is {@code null}, then it will be substituted with the string {@code
     * "null"}.
     * 
     * @param csq
     *            the {@code CharSequence} appended to the {@code
     *            CharArrayWriter}, may be {@code null}.
     * @param start
     *            the index of the first character in the {@code CharSequence}
     *            appended to the {@code CharArrayWriter}.
     * @param end
     *            the index of the character after the last one in the {@code
     *            CharSequence} appended to the {@code CharArrayWriter}.
     * @return this CharArrayWriter.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code end < 0}, {@code start > end},
     *             or if {@code end} is greater than the length of {@code csq}.
     */
    @Override
    public CharArrayWriter append(CharSequence csq, int start, int end) {
        if (null == csq) {
            csq = TOKEN_NULL;
        }
        String output = csq.subSequence(start, end).toString();
        write(output, 0, output.length());
        return this;
    }
}
