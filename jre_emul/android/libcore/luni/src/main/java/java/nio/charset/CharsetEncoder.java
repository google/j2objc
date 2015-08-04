/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.nio.charset;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Transforms a sequence of 16-bit Java characters to a byte sequence in some encoding.
 *
 * <p>The input character sequence is a {@link java.nio.CharBuffer CharBuffer} and the
 * output byte sequence is a {@link java.nio.ByteBuffer ByteBuffer}.
 *
 * <p>Use {@link #encode(CharBuffer)} to encode an entire {@code CharBuffer} to a
 * new {@code ByteBuffer}, or {@link #encode(CharBuffer, ByteBuffer, boolean)} for more
 * control. When using the latter method, the entire operation proceeds as follows:
 * <ol>
 * <li>Invoke {@link #reset()} to reset the encoder if this instance has been used before.</li>
 * <li>Invoke {@link #encode(CharBuffer, ByteBuffer, boolean) encode} with the {@code endOfInput}
 * parameter set to false until additional input is not needed (as signaled by the return value).
 * The input buffer must be filled and the output buffer must be flushed between invocations.
 * <p>The {@link #encode(CharBuffer, ByteBuffer, boolean) encode} method will
 * convert as many characters as possible, and the process won't stop until the
 * input buffer has been exhausted, the output buffer has been filled, or an
 * error has occurred. A {@link CoderResult CoderResult} instance will be
 * returned to indicate the current state. The caller should fill the input buffer, flush
 * the output buffer, or recovering from an error and try again, accordingly.
 * </li>
 * <li>Invoke {@link #encode(CharBuffer, ByteBuffer, boolean) encode} for the last time with
 * {@code endOfInput} set to true.</li>
 * <li>Invoke {@link #flush(ByteBuffer)} to flush remaining output.</li>
 * </ol>
 *
 * <p>There are two classes of encoding error: <i>malformed input</i>
 * signifies that the input character sequence is not legal, while <i>unmappable character</i>
 * signifies that the input is legal but cannot be mapped to a byte sequence (because the charset
 * cannot represent the character, for example).
 *
 * <p>Errors can be handled in three ways. The default is to
 * {@link CodingErrorAction#REPORT report} the error to the caller. The alternatives are to
 * {@link CodingErrorAction#IGNORE ignore} the error or {@link CodingErrorAction#REPLACE replace}
 * the problematic input with the byte sequence returned by {@link #replacement}. The disposition
 * for each of the two kinds of error can be set independently using the {@link #onMalformedInput}
 * and {@link #onUnmappableCharacter} methods.
 *
 * <p>The default replacement bytes depend on the charset but can be overridden using the
 * {@link #replaceWith} method.
 *
 * <p>This class is abstract and encapsulates many common operations of the
 * encoding process for all charsets. Encoders for a specific charset should
 * extend this class and need only to implement the
 * {@link #encodeLoop(CharBuffer, ByteBuffer) encodeLoop} method for basic
 * encoding.
 *
 * <p>This class is not thread-safe.
 *
 * @see java.nio.charset.Charset
 * @see java.nio.charset.CharsetDecoder
 */
public abstract class CharsetEncoder {
    private static final int READY = 0;
    private static final int ONGOING = 1;
    private static final int END = 2;
    private static final int FLUSH = 3;
    private static final int INIT = 4;

    private final Charset cs;

    private final float averageBytesPerChar;
    private final float maxBytesPerChar;

    private byte[] replacementBytes;

    private int status;
    // internal status indicates encode(CharBuffer) operation is finished
    private boolean finished;

    private CodingErrorAction malformedInputAction;
    private CodingErrorAction unmappableCharacterAction;

    // decoder instance for this encoder's charset, used for replacement value checking
    private CharsetDecoder decoder;

    /**
     * Constructs a new {@code CharsetEncoder} using the given parameters and
     * the replacement byte array {@code { (byte) '?' }}.
     */
    protected CharsetEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar) {
        this(cs, averageBytesPerChar, maxBytesPerChar, new byte[] { (byte) '?' });
    }

    /**
     * Constructs a new <code>CharsetEncoder</code> using the given
     * <code>Charset</code>, replacement byte array, average number and
     * maximum number of bytes created by this encoder for one input character.
     *
     * @param cs
     *            the <code>Charset</code> to be used by this encoder.
     * @param averageBytesPerChar
     *            average number of bytes created by this encoder for one single
     *            input character, must be positive.
     * @param maxBytesPerChar
     *            maximum number of bytes which can be created by this encoder
     *            for one single input character, must be positive.
     * @param replacement
     *            the replacement byte array, cannot be null or empty, its
     *            length cannot be larger than <code>maxBytesPerChar</code>,
     *            and must be a legal replacement.
     * @throws IllegalArgumentException
     *             if any parameters are invalid.
     */
    protected CharsetEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar, byte[] replacement) {
        if (averageBytesPerChar <= 0 || maxBytesPerChar <= 0) {
            throw new IllegalArgumentException("averageBytesPerChar and maxBytesPerChar must both be positive");
        }
        if (averageBytesPerChar > maxBytesPerChar) {
            throw new IllegalArgumentException("averageBytesPerChar is greater than maxBytesPerChar");
        }
        this.cs = cs;
        this.averageBytesPerChar = averageBytesPerChar;
        this.maxBytesPerChar = maxBytesPerChar;
        status = INIT;
        malformedInputAction = CodingErrorAction.REPORT;
        unmappableCharacterAction = CodingErrorAction.REPORT;
        this.replacementBytes = replacement;
    }

    /**
     * Returns the average number of bytes created by this encoder for a single
     * input character.
     */
    public final float averageBytesPerChar() {
        return averageBytesPerChar;
    }

    /**
     * Checks if the given character can be encoded by this encoder.
     * <p>
     * Note that this method can change the internal status of this encoder, so
     * it should not be called when another encoding process is ongoing,
     * otherwise it will throw an <code>IllegalStateException</code>.
     * <p>
     * This method can be overridden for performance improvement.
     *
     * @param c
     *            the given encoder.
     * @return true if given character can be encoded by this encoder.
     * @throws IllegalStateException
     *             if another encode process is ongoing so that the current
     *             internal status is neither RESET or FLUSH.
     */
    public boolean canEncode(char c) {
        return implCanEncode(CharBuffer.wrap(new char[] { c }));
    }

    // implementation of canEncode
    private boolean implCanEncode(CharBuffer cb) {
        if (status == FLUSH || status == INIT) {
            status = READY;
        }
        if (status != READY) {
            throw new IllegalStateException("encoding already in progress");
        }
        CodingErrorAction malformBak = malformedInputAction;
        CodingErrorAction unmapBak = unmappableCharacterAction;
        onMalformedInput(CodingErrorAction.REPORT);
        onUnmappableCharacter(CodingErrorAction.REPORT);
        boolean result = true;
        try {
            this.encode(cb);
        } catch (CharacterCodingException e) {
            result = false;
        }
        onMalformedInput(malformBak);
        onUnmappableCharacter(unmapBak);
        reset();
        return result;
    }

    /**
     * Checks if a given <code>CharSequence</code> can be encoded by this
     * encoder.
     *
     * Note that this method can change the internal status of this encoder, so
     * it should not be called when another encode process is ongoing, otherwise
     * it will throw an <code>IllegalStateException</code>.
     *
     * This method can be overridden for performance improvement.
     *
     * @param sequence
     *            the given <code>CharSequence</code>.
     * @return true if the given <code>CharSequence</code> can be encoded by
     *         this encoder.
     * @throws IllegalStateException
     *             if current internal status is neither RESET or FLUSH.
     */
    public boolean canEncode(CharSequence sequence) {
        CharBuffer cb;
        if (sequence instanceof CharBuffer) {
            cb = ((CharBuffer) sequence).duplicate();
        } else {
            cb = CharBuffer.wrap(sequence);
        }
        return implCanEncode(cb);
    }

    /**
     * Returns the {@link Charset} which this encoder uses.
     */
    public final Charset charset() {
        return cs;
    }

    /**
     * This is a facade method for the encoding operation.
     * <p>
     * This method encodes the remaining character sequence of the given
     * character buffer into a new byte buffer. This method performs a complete
     * encoding operation, resets at first, then encodes, and flushes at last.
     * <p>
     * This method should not be invoked if another encode operation is ongoing.
     *
     * @param in
     *            the input buffer.
     * @return a new <code>ByteBuffer</code> containing the bytes produced by
     *         this encoding operation. The buffer's limit will be the position
     *         of the last byte in the buffer, and the position will be zero.
     * @throws IllegalStateException
     *             if another encoding operation is ongoing.
     * @throws MalformedInputException
     *             if an illegal input character sequence for this charset is
     *             encountered, and the action for malformed error is
     *             {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}
     * @throws UnmappableCharacterException
     *             if a legal but unmappable input character sequence for this
     *             charset is encountered, and the action for unmappable
     *             character error is
     *             {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}.
     *             Unmappable means the Unicode character sequence at the input
     *             buffer's current position cannot be mapped to a equivalent
     *             byte sequence.
     * @throws CharacterCodingException
     *             if other exception happened during the encode operation.
     */
    public abstract ByteBuffer encode(CharBuffer in) throws CharacterCodingException;

    /**
     * Encodes characters starting at the current position of the given input
     * buffer, and writes the equivalent byte sequence into the given output
     * buffer from its current position.
     * <p>
     * The buffers' position will be changed with the reading and writing
     * operation, but their limits and marks will be kept intact.
     * <p>
     * A <code>CoderResult</code> instance will be returned according to
     * following rules:
     * <ul>
     * <li>A {@link CoderResult#malformedForLength(int) malformed input} result
     * indicates that some malformed input error was encountered, and the
     * erroneous characters start at the input buffer's position and their
     * number can be got by result's {@link CoderResult#length() length}. This
     * kind of result can be returned only if the malformed action is
     * {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}.</li>
     * <li>{@link CoderResult#UNDERFLOW CoderResult.UNDERFLOW} indicates that
     * as many characters as possible in the input buffer have been encoded. If
     * there is no further input and no characters left in the input buffer then
     * this task is complete. If this is not the case then the client should
     * call this method again supplying some more input characters.</li>
     * <li>{@link CoderResult#OVERFLOW CoderResult.OVERFLOW} indicates that the
     * output buffer has been filled, while there are still some characters
     * remaining in the input buffer. This method should be invoked again with a
     * non-full output buffer.</li>
     * <li>A {@link CoderResult#unmappableForLength(int) unmappable character}
     * result indicates that some unmappable character error was encountered,
     * and the erroneous characters start at the input buffer's position and
     * their number can be got by result's {@link CoderResult#length() length}.
     * This kind of result can be returned only on
     * {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}.</li>
     * </ul>
     * <p>
     * The <code>endOfInput</code> parameter indicates if the invoker can
     * provider further input. This parameter is true if and only if the
     * characters in the current input buffer are all inputs for this encoding
     * operation. Note that it is common and won't cause an error if the invoker
     * sets false and then has no more input available, while it may cause an
     * error if the invoker always sets true in several consecutive invocations.
     * This would make the remaining input to be treated as malformed input.
     * input.
     * <p>
     * This method invokes the
     * {@link #encodeLoop(CharBuffer, ByteBuffer) encodeLoop} method to
     * implement the basic encode logic for a specific charset.
     *
     * @param in
     *            the input buffer.
     * @param out
     *            the output buffer.
     * @param endOfInput
     *            true if all the input characters have been provided.
     * @return a <code>CoderResult</code> instance indicating the result.
     * @throws IllegalStateException
     *             if the encoding operation has already started or no more
     *             input is needed in this encoding process.
     * @throws CoderMalfunctionError
     *             If the {@link #encodeLoop(CharBuffer, ByteBuffer) encodeLoop}
     *             method threw an <code>BufferUnderflowException</code> or
     *             <code>BufferUnderflowException</code>.
     */
    public final CoderResult encode(CharBuffer in, ByteBuffer out, boolean endOfInput) {
        try {
            return encodeLoop(in, out);
        } catch (BufferOverflowException e) {
            throw new CoderMalfunctionError(e);
        } catch (BufferUnderflowException e) {
            throw new CoderMalfunctionError(e);
        }
    }

    /**
     * Encodes characters into bytes. This method is called by
     * {@link #encode(CharBuffer, ByteBuffer, boolean) encode}.
     * <p>
     * This method will implement the essential encoding operation, and it won't
     * stop encoding until either all the input characters are read, the output
     * buffer is filled, or some exception is encountered. Then it will
     * return a <code>CoderResult</code> object indicating the result of the
     * current encoding operation. The rule to construct the
     * <code>CoderResult</code> is the same as for
     * {@link #encode(CharBuffer, ByteBuffer, boolean) encode}. When an
     * exception is encountered in the encoding operation, most implementations
     * of this method will return a relevant result object to the
     * {@link #encode(CharBuffer, ByteBuffer, boolean) encode} method, and some
     * performance optimized implementation may handle the exception and
     * implement the error action itself.
     * <p>
     * The buffers are scanned from their current positions, and their positions
     * will be modified accordingly, while their marks and limits will be
     * intact. At most {@link CharBuffer#remaining() in.remaining()} characters
     * will be read, and {@link ByteBuffer#remaining() out.remaining()} bytes
     * will be written.
     * <p>
     * Note that some implementations may pre-scan the input buffer and return
     * <code>CoderResult.UNDERFLOW</code> until it receives sufficient input.
     * <p>
     * @param in
     *            the input buffer.
     * @param out
     *            the output buffer.
     * @return a <code>CoderResult</code> instance indicating the result.
     */
    protected abstract CoderResult encodeLoop(CharBuffer in, ByteBuffer out);

    /**
     * Flushes this encoder.
     * <p>
     * The maximum number of written bytes won't larger than
     * {@link ByteBuffer#remaining() out.remaining()}. If some encoder wants to
     * write more bytes than the output buffer's available remaining space, then
     * <code>CoderResult.OVERFLOW</code> will be returned, and this method
     * must be called again with a byte buffer that has free space. Otherwise
     * this method will return <code>CoderResult.UNDERFLOW</code>, which
     * means one encoding process has been completed successfully.
     * <p>
     * During the flush, the output buffer's position will be changed
     * accordingly, while its mark and limit will be intact.
     *
     * @param out
     *            the given output buffer.
     * @return <code>CoderResult.UNDERFLOW</code> or
     *         <code>CoderResult.OVERFLOW</code>.
     * @throws IllegalStateException
     *             if this encoder hasn't read all input characters during one
     *             encoding process, which means neither after calling
     *             {@link #encode(CharBuffer) encode(CharBuffer)} nor after
     *             calling {@link #encode(CharBuffer, ByteBuffer, boolean)
     *             encode(CharBuffer, ByteBuffer, boolean)} with {@code true}
     *             for the last boolean parameter.
     */
    public final CoderResult flush(ByteBuffer out) {
       return CoderResult.UNDERFLOW;
    }

    /**
     * Returns this encoder's <code>CodingErrorAction</code> when a malformed
     * input error occurred during the encoding process.
     */
    public CodingErrorAction malformedInputAction() {
        return malformedInputAction;
    }

    /**
     * Returns the maximum number of bytes which can be created by this encoder for
     * one input character, must be positive.
     */
    public final float maxBytesPerChar() {
        return maxBytesPerChar;
    }

    /**
     * Sets this encoder's action on malformed input error.
     *
     * @param newAction
     *            the new action on malformed input error.
     * @return this encoder.
     * @throws IllegalArgumentException
     *             if the given newAction is null.
     */
    public final CharsetEncoder onMalformedInput(CodingErrorAction newAction) {
        if (newAction == null) {
            throw new IllegalArgumentException("newAction == null");
        }
        malformedInputAction = newAction;
        return this;
    }

    /**
     * Sets this encoder's action on unmappable character error.
     *
     * @param newAction
     *            the new action on unmappable character error.
     * @return this encoder.
     * @throws IllegalArgumentException
     *             if the given newAction is null.
     */
    public final CharsetEncoder onUnmappableCharacter(CodingErrorAction newAction) {
        if (newAction == null) {
            throw new IllegalArgumentException("newAction == null");
        }
        unmappableCharacterAction = newAction;
        return this;
    }

    /**
     * Returns the replacement byte array, which is never null or empty.
     */
    public final byte[] replacement() {
        return replacementBytes;
    }

    /**
     * Sets the new replacement value.
     *
     * This method first checks the given replacement's validity, then changes
     * the replacement value.
     *
     * @param replacement
     *            the replacement byte array, cannot be null or empty, its
     *            length cannot be larger than <code>maxBytesPerChar</code>,
     *            and it must be legal replacement.
     * @return this encoder.
     * @throws IllegalArgumentException
     *             if the given replacement cannot satisfy the requirement
     *             mentioned above.
     */
    public final CharsetEncoder replaceWith(byte[] replacement) {
        if (replacement == null) {
            throw new IllegalArgumentException("replacement == null");
        }
        if (replacement.length == 0) {
            throw new IllegalArgumentException("replacement.length == 0");
        }
        if (replacement.length > maxBytesPerChar()) {
            throw new IllegalArgumentException("replacement.length > maxBytesPerChar: " +
                    replacement.length + " > " + maxBytesPerChar());
        }
        this.replacementBytes = replacement;
        return this;
    }

    /**
     * Resets this encoder.
     *
     * @return this encoder.
     */
    public final CharsetEncoder reset() {
        return this;
    }

    /**
     * Returns this encoder's <code>CodingErrorAction</code> when unmappable
     * character occurred during encoding process.
     */
    public CodingErrorAction unmappableCharacterAction() {
        return unmappableCharacterAction;
    }
}
