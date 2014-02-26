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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

/**
 * A class for turning a character stream into a byte stream. Data written to
 * the target input stream is converted into bytes by either a default or a
 * provided character converter. The default encoding is taken from the
 * "file.encoding" system property. {@code OutputStreamWriter} contains a buffer
 * of bytes to be written to target stream and converts these into characters as
 * needed. The buffer size is 8K.
 *
 * @see InputStreamReader
 */
public class OutputStreamWriter extends Writer {

    private final OutputStream out;

    private CharsetEncoder encoder;

    private ByteBuffer bytes = ByteBuffer.allocate(8192);

    /**
     * Constructs a new OutputStreamWriter using {@code out} as the target
     * stream to write converted characters to. The default character encoding
     * is used.
     *
     * @param out
     *            the non-null target stream to write converted bytes to.
     */
    public OutputStreamWriter(OutputStream out) {
        this(out, Charset.defaultCharset());
    }

    /**
     * Constructs a new OutputStreamWriter using {@code out} as the target
     * stream to write converted characters to and {@code charsetName} as the character
     * encoding. If the encoding cannot be found, an
     * UnsupportedEncodingException error is thrown.
     *
     * @param out
     *            the target stream to write converted bytes to.
     * @param charsetName
     *            the string describing the desired character encoding.
     * @throws NullPointerException
     *             if {@code charsetName} is {@code null}.
     * @throws UnsupportedEncodingException
     *             if the encoding specified by {@code charsetName} cannot be found.
     */
    public OutputStreamWriter(OutputStream out, final String charsetName)
            throws UnsupportedEncodingException {
        super(out);
        if (charsetName == null) {
            throw new NullPointerException("charsetName == null");
        }
        this.out = out;
        try {
            encoder = Charset.forName(charsetName).newEncoder();
        } catch (Exception e) {
            throw new UnsupportedEncodingException(charsetName);
        }
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    /**
     * Constructs a new OutputStreamWriter using {@code out} as the target
     * stream to write converted characters to and {@code cs} as the character
     * encoding.
     *
     * @param out
     *            the target stream to write converted bytes to.
     * @param cs
     *            the {@code Charset} that specifies the character encoding.
     */
    public OutputStreamWriter(OutputStream out, Charset cs) {
        super(out);
        this.out = out;
        encoder = cs.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    /**
     * Constructs a new OutputStreamWriter using {@code out} as the target
     * stream to write converted characters to and {@code charsetEncoder} as the character
     * encoder.
     *
     * @param out
     *            the target stream to write converted bytes to.
     * @param charsetEncoder
     *            the character encoder used for character conversion.
     */
    public OutputStreamWriter(OutputStream out, CharsetEncoder charsetEncoder) {
        super(out);
        charsetEncoder.charset();
        this.out = out;
        encoder = charsetEncoder;
    }

    /**
     * Closes this writer. This implementation flushes the buffer as well as the
     * target stream. The target stream is then closed and the resources for the
     * buffer and converter are released.
     *
     * <p>Only the first invocation of this method has any effect. Subsequent calls
     * do nothing.
     *
     * @throws IOException
     *             if an error occurs while closing this writer.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (encoder != null) {
                drainEncoder();
                flushBytes(false);
                out.close();
                encoder = null;
                bytes = null;
            }
        }
    }

    /**
     * Flushes this writer. This implementation ensures that all buffered bytes
     * are written to the target stream. After writing the bytes, the target
     * stream is flushed as well.
     *
     * @throws IOException
     *             if an error occurs while flushing this writer.
     */
    @Override
    public void flush() throws IOException {
        flushBytes(true);
    }

    private void flushBytes(boolean flushUnderlyingStream) throws IOException {
        synchronized (lock) {
            checkStatus();
            int position = bytes.position();
            if (position > 0) {
                bytes.flip();
                out.write(bytes.array(), bytes.arrayOffset(), position);
                bytes.clear();
            }
            if (flushUnderlyingStream) {
                out.flush();
            }
        }
    }

    private void convert(CharBuffer chars) throws IOException {
        while (true) {
            CoderResult result = encoder.encode(chars, bytes, false);
            if (result.isOverflow()) {
                // Make room and try again.
                flushBytes(false);
                continue;
            } else if (result.isError()) {
                result.throwException();
            }
            break;
        }
    }

    private void drainEncoder() throws IOException {
        // Strictly speaking, I think it's part of the CharsetEncoder contract that you call
        // encode with endOfInput true before flushing. Our ICU-based implementations don't
        // actually need this, and you'd hope that any reasonable implementation wouldn't either.
        // CharsetEncoder.encode doesn't actually pass the boolean through to encodeLoop anyway!
        CharBuffer chars = CharBuffer.allocate(0);
        while (true) {
            CoderResult result = encoder.encode(chars, bytes, true);
            if (result.isError()) {
                result.throwException();
            } else if (result.isOverflow()) {
                flushBytes(false);
                continue;
            }
            break;
        }

        // Some encoders (such as ISO-2022-JP) have stuff to write out after all the
        // characters (such as shifting back into a default state). In our implementation,
        // this is actually the first time ICU is told that we've run out of input.
        CoderResult result = encoder.flush(bytes);
        while (!result.isUnderflow()) {
            if (result.isOverflow()) {
                flushBytes(false);
                result = encoder.flush(bytes);
            } else {
                result.throwException();
            }
        }
    }

    private void checkStatus() throws IOException {
        if (encoder == null) {
            throw new IOException("OutputStreamWriter is closed");
        }
    }

    /**
     * Returns the canonical name of the encoding used by this writer to convert characters to
     * bytes, or null if this writer has been closed. Most callers should probably keep
     * track of the String or Charset they passed in; this method may not return the same
     * name.
     */
    public String getEncoding() {
        if (encoder == null) {
            return null;
        }
        return encoder.charset().name();
    }

    /**
     * Writes {@code count} characters starting at {@code offset} in {@code buf}
     * to this writer. The characters are immediately converted to bytes by the
     * character converter and stored in a local buffer. If the buffer gets full
     * as a result of the conversion, this writer is flushed.
     *
     * @param buffer
     *            the array containing characters to write.
     * @param offset
     *            the index of the first character in {@code buf} to write.
     * @param count
     *            the maximum number of characters to write.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if
     *             {@code offset + count} is greater than the size of
     *             {@code buf}.
     * @throws IOException
     *             if this writer has already been closed or another I/O error
     *             occurs.
     */
    @Override
    public void write(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            checkStatus();
            Arrays.checkOffsetAndCount(buffer.length, offset, count);
            CharBuffer chars = CharBuffer.wrap(buffer, offset, count);
            convert(chars);
        }
    }

    /**
     * Writes the character {@code oneChar} to this writer. The lowest two bytes
     * of the integer {@code oneChar} are immediately converted to bytes by the
     * character converter and stored in a local buffer. If the buffer gets full
     * by converting this character, this writer is flushed.
     *
     * @param oneChar
     *            the character to write.
     * @throws IOException
     *             if this writer is closed or another I/O error occurs.
     */
    @Override
    public void write(int oneChar) throws IOException {
        synchronized (lock) {
            checkStatus();
            CharBuffer chars = CharBuffer.wrap(new char[] { (char) oneChar });
            convert(chars);
        }
    }

    /**
     * Writes {@code count} characters starting at {@code offset} in {@code str}
     * to this writer. The characters are immediately converted to bytes by the
     * character converter and stored in a local buffer. If the buffer gets full
     * as a result of the conversion, this writer is flushed.
     *
     * @param str
     *            the string containing characters to write.
     * @param offset
     *            the start position in {@code str} for retrieving characters.
     * @param count
     *            the maximum number of characters to write.
     * @throws IOException
     *             if this writer has already been closed or another I/O error
     *             occurs.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if
     *             {@code offset + count} is bigger than the length of
     *             {@code str}.
     */
    @Override
    public void write(String str, int offset, int count) throws IOException {
        synchronized (lock) {
            if (count < 0) {
                throw new StringIndexOutOfBoundsException(str, offset, count);
            }
            if (str == null) {
                throw new NullPointerException("str == null");
            }
            if ((offset | count) < 0 || offset > str.length() - count) {
                throw new StringIndexOutOfBoundsException(str, offset, count);
            }
            checkStatus();
            CharBuffer chars = CharBuffer.wrap(str, offset, count + offset);
            convert(chars);
        }
    }

    @Override boolean checkError() {
        return out.checkError();
    }
}
