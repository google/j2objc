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
 * A converter that can convert a byte sequence from a charset into a 16-bit
 * Unicode character sequence.
 * <p>
 * The input byte sequence is wrapped by a
 * {@link java.nio.ByteBuffer ByteBuffer} and the output character sequence is a
 * {@link java.nio.CharBuffer CharBuffer}. A decoder instance should be used in
 * the following sequence, which is referred to as a decoding operation:
 * <ol>
 * <li>invoking the {@link #reset() reset} method to reset the decoder if the
 * decoder has been used;</li>
 * <li>invoking the {@link #decode(ByteBuffer, CharBuffer, boolean) decode}
 * method until the additional input is not needed, the <code>endOfInput</code>
 * parameter must be set to false, the input buffer must be filled and the
 * output buffer must be flushed between invocations;</li>
 * <li>invoking the {@link #decode(ByteBuffer, CharBuffer, boolean) decode}
 * method for the last time, and then the <code>endOfInput</code> parameter
 * must be set to true;</li>
 * <li>invoking the {@link #flush(CharBuffer) flush} method to flush the
 * output.</li>
 * </ol>
 * <p>
 * The {@link #decode(ByteBuffer, CharBuffer, boolean) decode} method will
 * convert as many bytes as possible, and the process won't stop until the input
 * bytes have run out, the output buffer has been filled or some error has
 * happened. A {@link CoderResult CoderResult} instance will be returned to
 * indicate the stop reason, and the invoker can identify the result and choose
 * further action, which includes filling the input buffer, flushing the output
 * buffer or recovering from an error and trying again.
 * <p>
 * There are two common decoding errors. One is named malformed and it is
 * returned when the input byte sequence is illegal for the current specific
 * charset, the other is named unmappable character and it is returned when a
 * problem occurs mapping a legal input byte sequence to its Unicode character
 * equivalent.
 * <p>
 * Both errors can be handled in three ways, the default one is to report the
 * error to the invoker by a {@link CoderResult CoderResult} instance, and the
 * alternatives are to ignore it or to replace the erroneous input with the
 * replacement string. The replacement string is "\uFFFD" by default and can be
 * changed by invoking {@link #replaceWith(String) replaceWith} method. The
 * invoker of this decoder can choose one way by specifying a
 * {@link CodingErrorAction CodingErrorAction} instance for each error type via
 * {@link #onMalformedInput(CodingErrorAction) onMalformedInput} method and
 * {@link #onUnmappableCharacter(CodingErrorAction) onUnmappableCharacter}
 * method.
 * <p>
 * This is an abstract class and encapsulates many common operations of the
 * decoding process for all charsets. Decoders for a specific charset should
 * extend this class and need only to implement the
 * {@link #decodeLoop(ByteBuffer, CharBuffer) decodeLoop} method for the basic
 * decoding. If a subclass maintains an internal state, it should override the
 * {@link #implFlush(CharBuffer) implFlush} method and the
 * {@link #implReset() implReset} method in addition.
 * <p>
 * This class is not thread-safe.
 *
 * @see java.nio.charset.Charset
 * @see java.nio.charset.CharsetEncoder
 */
public abstract class CharsetDecoder {

    /*
     * internal status consts
     */
    private static final int INIT = 0;

    private static final int ONGOING = 1;

    private static final int END = 2;

    private static final int FLUSH = 3;

    // average number of chars for one byte
    private float averChars;

    // maximum number of chars for one byte
    private float maxChars;

    // charset for this decoder
    private Charset cs;

    // specify the action if malformed input error encountered
    private CodingErrorAction malformAction;

    // specify the action if unmappable character error encountered
    private CodingErrorAction unmapAction;

    // the replacement string
    private String replace;

    // the current status
    private int status;

    /**
     * Constructs a new <code>CharsetDecoder</code> using the given
     * <code>Charset</code>, average number and maximum number of characters
     * created by this decoder for one input byte, and the default replacement
     * string "\uFFFD".
     *
     * @param charset
     *            the <code>Charset</code> to be used by this decoder.
     * @param averageCharsPerByte
     *            the average number of characters created by this decoder for
     *            one input byte, must be positive.
     * @param maxCharsPerByte
     *            the maximum number of characters created by this decoder for
     *            one input byte, must be positive.
     * @throws IllegalArgumentException
     *             if <code>averageCharsPerByte</code> or
     *             <code>maxCharsPerByte</code> is negative.
     */
    protected CharsetDecoder(Charset charset, float averageCharsPerByte,
            float maxCharsPerByte) {
        if (averageCharsPerByte <= 0 || maxCharsPerByte <= 0) {
            throw new IllegalArgumentException("Characters number for one byte must be positive.");
        }
        if (averageCharsPerByte > maxCharsPerByte) {
            throw new IllegalArgumentException(
                "averageCharsPerByte is greater than maxCharsPerByte");
        }
        averChars = averageCharsPerByte;
        maxChars = maxCharsPerByte;
        cs = charset;
        status = INIT;
        malformAction = CodingErrorAction.REPORT;
        unmapAction = CodingErrorAction.REPORT;
        replace = "\ufffd"; //$NON-NLS-1$
    }

    /**
     * Gets the average number of characters created by this decoder for a
     * single input byte.
     *
     * @return the average number of characters created by this decoder for a
     *         single input byte.
     */
    public final float averageCharsPerByte() {
        return averChars;
    }

    /**
     * Gets the <code>Charset</code> which this decoder uses.
     *
     * @return the <code>Charset</code> which this decoder uses.
     */
    public final Charset charset() {
        return cs;
    }

    /**
     * This is a facade method for the decoding operation.
     * <p>
     * This method decodes the remaining byte sequence of the given byte buffer
     * into a new character buffer. This method performs a complete decoding
     * operation, resets at first, then decodes, and flushes at last.
     * <p>
     * This method should not be invoked while another {@code decode} operation
     * is ongoing.
     *
     * @param in
     *            the input buffer.
     * @return a new <code>CharBuffer</code> containing the the characters
     *         produced by this decoding operation. The buffer's limit will be
     *         the position of the last character in the buffer, and the
     *         position will be zero.
     * @throws IllegalStateException
     *             if another decoding operation is ongoing.
     * @throws MalformedInputException
     *             if an illegal input byte sequence for this charset was
     *             encountered, and the action for malformed error is
     *             {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}
     * @throws UnmappableCharacterException
     *             if a legal but unmappable input byte sequence for this
     *             charset was encountered, and the action for unmappable
     *             character error is
     *             {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}.
     *             Unmappable means the byte sequence at the input buffer's
     *             current position cannot be mapped to a Unicode character
     *             sequence.
     * @throws CharacterCodingException
     *             if another exception happened during the decode operation.
     */
    public final CharBuffer decode(ByteBuffer in)
            throws CharacterCodingException {
        reset();
        int length = (int) (in.remaining() * averChars);
        CharBuffer output = CharBuffer.allocate(length);
        CoderResult result = null;
        while (true) {
            result = decode(in, output, false);
            checkCoderResult(result);
            if (result.isUnderflow()) {
                break;
            } else if (result.isOverflow()) {
                output = allocateMore(output);
            }
        }
        result = decode(in, output, true);
        checkCoderResult(result);

        while (true) {
            result = flush(output);
            checkCoderResult(result);
            if (result.isOverflow()) {
                output = allocateMore(output);
            } else {
                break;
            }
        }

        output.flip();
        status = FLUSH;
        return output;
    }

    /*
     * checks the result whether it needs to throw CharacterCodingException.
     */
    private void checkCoderResult(CoderResult result)
            throws CharacterCodingException {
        if (result.isMalformed() && malformAction == CodingErrorAction.REPORT) {
            throw new MalformedInputException(result.length());
        } else if (result.isUnmappable()
                && unmapAction == CodingErrorAction.REPORT) {
            throw new UnmappableCharacterException(result.length());
        }
    }

    /*
     * original output is full and doesn't have remaining. allocate more space
     * to new CharBuffer and return it, the contents in the given buffer will be
     * copied into the new buffer.
     */
    private CharBuffer allocateMore(CharBuffer output) {
        if (output.capacity() == 0) {
            return CharBuffer.allocate(1);
        }
        CharBuffer result = CharBuffer.allocate(output.capacity() * 2);
        output.flip();
        result.put(output);
        return result;
    }

    /**
     * Decodes bytes starting at the current position of the given input buffer,
     * and writes the equivalent character sequence into the given output buffer
     * from its current position.
     * <p>
     * The buffers' position will be changed with the reading and writing
     * operation, but their limits and marks will be kept intact.
     * <p>
     * A <code>CoderResult</code> instance will be returned according to
     * following rules:
     * <ul>
     * <li>{@link CoderResult#OVERFLOW CoderResult.OVERFLOW} indicates that
     * even though not all of the input has been processed, the buffer the
     * output is being written to has reached its capacity. In the event of this
     * code being returned this method should be called once more with an
     * <code>out</code> argument that has not already been filled.</li>
     * <li>{@link CoderResult#UNDERFLOW CoderResult.UNDERFLOW} indicates that
     * as many bytes as possible in the input buffer have been decoded. If there
     * is no further input and no remaining bytes in the input buffer then this
     * operation may be regarded as complete. Otherwise, this method should be
     * called once more with additional input.</li>
     * <li>A {@link CoderResult#malformedForLength(int) malformed input} result
     * indicates that some malformed input error has been encountered, and the
     * erroneous bytes start at the input buffer's position and their number can
     * be got by result's {@link CoderResult#length() length}. This kind of
     * result can be returned only if the malformed action is
     * {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}. </li>
     * <li>A {@link CoderResult#unmappableForLength(int) unmappable character}
     * result indicates that some unmappable character error has been
     * encountered, and the erroneous bytes start at the input buffer's position
     * and their number can be got by result's
     * {@link CoderResult#length() length}. This kind of result can be returned
     * only if the unmappable character action is
     * {@link CodingErrorAction#REPORT CodingErrorAction.REPORT}. </li>
     * </ul>
     * <p>
     * The <code>endOfInput</code> parameter indicates that the invoker cannot
     * provide further input. This parameter is true if and only if the bytes in
     * current input buffer are all inputs for this decoding operation. Note
     * that it is common and won't cause an error if the invoker sets false and
     * then can't provide more input, while it may cause an error if the invoker
     * always sets true in several consecutive invocations. This would make the
     * remaining input to be treated as malformed input.
     * <p>
     * This method invokes the
     * {@link #decodeLoop(ByteBuffer, CharBuffer) decodeLoop} method to
     * implement the basic decode logic for a specific charset.
     *
     * @param in
     *            the input buffer.
     * @param out
     *            the output buffer.
     * @param endOfInput
     *            true if all the input characters have been provided.
     * @return a <code>CoderResult</code> instance which indicates the reason
     *         of termination.
     * @throws IllegalStateException
     *             if decoding has started or no more input is needed in this
     *             decoding progress.
     * @throws CoderMalfunctionError
     *             if the {@link #decodeLoop(ByteBuffer, CharBuffer) decodeLoop}
     *             method threw an <code>BufferUnderflowException</code> or
     *             <code>BufferOverflowException</code>.
     */
    public final CoderResult decode(ByteBuffer in, CharBuffer out,
            boolean endOfInput) {
        /*
         * status check
         */
        if ((status == FLUSH) || (!endOfInput && status == END)) {
            throw new IllegalStateException();
        }

        CoderResult result = null;

        // begin to decode
        while (true) {
            CodingErrorAction action = null;
            try {
                result = decodeLoop(in, out);
            } catch (BufferOverflowException ex) {
                // unexpected exception
                throw new CoderMalfunctionError(ex);
            } catch (BufferUnderflowException ex) {
                // unexpected exception
                throw new CoderMalfunctionError(ex);
            }

            /*
             * result handling
             */
            if (result.isUnderflow()) {
                int remaining = in.remaining();
                status = endOfInput ? END : ONGOING;
                if (endOfInput && remaining > 0) {
                    result = CoderResult.malformedForLength(remaining);
                } else {
                    return result;
                }
            }
            if (result.isOverflow()) {
                return result;
            }
            // set coding error handle action
            action = malformAction;
            if (result.isUnmappable()) {
                action = unmapAction;
            }
            // If the action is IGNORE or REPLACE, we should continue decoding.
            if (action == CodingErrorAction.REPLACE) {
                if (out.remaining() < replace.length()) {
                    return CoderResult.OVERFLOW;
                }
                out.put(replace);
            } else {
                if (action != CodingErrorAction.IGNORE)
                    return result;
            }
            in.position(in.position() + result.length());
        }
    }

    /**
     * Decodes bytes into characters. This method is called by the
     * {@link #decode(ByteBuffer, CharBuffer, boolean) decode} method.
     * <p>
     * This method will implement the essential decoding operation, and it won't
     * stop decoding until either all the input bytes are read, the output
     * buffer is filled, or some exception is encountered. Then it will return a
     * <code>CoderResult</code> object indicating the result of current
     * decoding operation. The rules to construct the <code>CoderResult</code>
     * are the same as for
     * {@link #decode(ByteBuffer, CharBuffer, boolean) decode}. When an
     * exception is encountered in the decoding operation, most implementations
     * of this method will return a relevant result object to the
     * {@link #decode(ByteBuffer, CharBuffer, boolean) decode} method, and some
     * performance optimized implementation may handle the exception and
     * implement the error action itself.
     * <p>
     * The buffers are scanned from their current positions, and their positions
     * will be modified accordingly, while their marks and limits will be
     * intact. At most {@link ByteBuffer#remaining() in.remaining()} characters
     * will be read, and {@link CharBuffer#remaining() out.remaining()} bytes
     * will be written.
     * <p>
     * Note that some implementations may pre-scan the input buffer and return a
     * <code>CoderResult.UNDERFLOW</code> until it receives sufficient input.
     *
     * @param in
     *            the input buffer.
     * @param out
     *            the output buffer.
     * @return a <code>CoderResult</code> instance indicating the result.
     */
    protected abstract CoderResult decodeLoop(ByteBuffer in, CharBuffer out);

    /**
     * Gets the charset detected by this decoder; this method is optional.
     * <p>
     * If implementing an auto-detecting charset, then this decoder returns the
     * detected charset from this method when it is available. The returned
     * charset will be the same for the rest of the decode operation.
     * <p>
     * If insufficient bytes have been read to determine the charset, an
     * <code>IllegalStateException</code> will be thrown.
     * <p>
     * The default implementation always throws
     * <code>UnsupportedOperationException</code>, so it should be overridden
     * by a subclass if needed.
     *
     * @return the charset detected by this decoder, or null if it is not yet
     *         determined.
     * @throws UnsupportedOperationException
     *             if this decoder does not implement an auto-detecting charset.
     * @throws IllegalStateException
     *             if insufficient bytes have been read to determine the
     *             charset.
     */
    public Charset detectedCharset() {
        throw new UnsupportedOperationException();
    }

    /**
     * Flushes this decoder.
     *
     * This method will call {@link #implFlush(CharBuffer) implFlush}. Some
     * decoders may need to write some characters to the output buffer when they
     * have read all input bytes; subclasses can override
     * {@link #implFlush(CharBuffer) implFlush} to perform the writing operation.
     * <p>
     * The maximum number of written bytes won't be larger than
     * {@link CharBuffer#remaining() out.remaining()}. If some decoder wants to
     * write more bytes than an output buffer's remaining space allows, then a
     * <code>CoderResult.OVERFLOW</code> will be returned, and this method
     * must be called again with a character buffer that has more remaining
     * space. Otherwise this method will return
     * <code>CoderResult.UNDERFLOW</code>, which means one decoding process
     * has been completed successfully.
     * <p>
     * During the flush, the output buffer's position will be changed
     * accordingly, while its mark and limit will be intact.
     *
     * @param out
     *            the given output buffer.
     * @return <code>CoderResult.UNDERFLOW</code> or
     *         <code>CoderResult.OVERFLOW</code>.
     * @throws IllegalStateException
     *             if this decoder hasn't read all input bytes during one
     *             decoding process, which means neither after calling
     *             {@link #decode(ByteBuffer) decode(ByteBuffer)} nor after
     *             calling {@link #decode(ByteBuffer, CharBuffer, boolean)
     *             decode(ByteBuffer, CharBuffer, boolean)} with true as value
     *             for the last boolean parameter.
     */
    public final CoderResult flush(CharBuffer out) {
        if (status != END && status != INIT) {
            throw new IllegalStateException();
        }
        CoderResult result = implFlush(out);
        if (result == CoderResult.UNDERFLOW) {
            status = FLUSH;
        }
        return result;
    }

    /**
     * Flushes this decoder. The default implementation does nothing and always
     * returns <code>CoderResult.UNDERFLOW</code>; this method can be
     * overridden if needed.
     *
     * @param out
     *            the output buffer.
     * @return <code>CoderResult.UNDERFLOW</code> or
     *         <code>CoderResult.OVERFLOW</code>.
     */
    protected CoderResult implFlush(CharBuffer out) {
        return CoderResult.UNDERFLOW;
    }

    /**
     * Notifies that this decoder's <code>CodingErrorAction</code> specified
     * for malformed input error has been changed. The default implementation
     * does nothing; this method can be overridden if needed.
     *
     * @param newAction
     *            the new action.
     */
    protected void implOnMalformedInput(CodingErrorAction newAction) {
        // default implementation is empty
    }

    /**
     * Notifies that this decoder's <code>CodingErrorAction</code> specified
     * for unmappable character error has been changed. The default
     * implementation does nothing; this method can be overridden if needed.
     *
     * @param newAction
     *            the new action.
     */
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
        // default implementation is empty
    }

    /**
     * Notifies that this decoder's replacement has been changed. The default
     * implementation does nothing; this method can be overridden if needed.
     *
     * @param newReplacement
     *            the new replacement string.
     */
    protected void implReplaceWith(String newReplacement) {
        // default implementation is empty
    }

    /**
     * Reset this decoder's charset related state. The default implementation
     * does nothing; this method can be overridden if needed.
     */
    protected void implReset() {
        // default implementation is empty
    }

    /**
     * Indicates whether this decoder implements an auto-detecting charset.
     *
     * @return <code>true</code> if this decoder implements an auto-detecting
     *         charset.
     */
    public boolean isAutoDetecting() {
        return false;
    }

    /**
     * Indicates whether this decoder has detected a charset; this method is
     * optional.
     * <p>
     * If this decoder implements an auto-detecting charset, then this method
     * may start to return true during decoding operation to indicate that a
     * charset has been detected in the input bytes and that the charset can be
     * retrieved by invoking the {@link #detectedCharset() detectedCharset}
     * method.
     * <p>
     * Note that a decoder that implements an auto-detecting charset may still
     * succeed in decoding a portion of the given input even when it is unable
     * to detect the charset. For this reason users should be aware that a
     * <code>false</code> return value does not indicate that no decoding took
     * place.
     * <p>
     * The default implementation always throws an
     * <code>UnsupportedOperationException</code>; it should be overridden by
     * a subclass if needed.
     *
     * @return <code>true</code> if this decoder has detected a charset.
     * @throws UnsupportedOperationException
     *             if this decoder doesn't implement an auto-detecting charset.
     */
    public boolean isCharsetDetected() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets this decoder's <code>CodingErrorAction</code> when malformed input
     * occurred during the decoding process.
     *
     * @return this decoder's <code>CodingErrorAction</code> when malformed
     *         input occurred during the decoding process.
     */
    public CodingErrorAction malformedInputAction() {
        return malformAction;
    }

    /**
     * Gets the maximum number of characters which can be created by this
     * decoder for one input byte, must be positive.
     *
     * @return the maximum number of characters which can be created by this
     *         decoder for one input byte, must be positive.
     */
    public final float maxCharsPerByte() {
        return maxChars;
    }

    /**
     * Sets this decoder's action on malformed input errors.
     *
     * This method will call the
     * {@link #implOnMalformedInput(CodingErrorAction) implOnMalformedInput}
     * method with the given new action as argument.
     *
     * @param newAction
     *            the new action on malformed input error.
     * @return this decoder.
     * @throws IllegalArgumentException
     *             if {@code newAction} is {@code null}.
     */
    public final CharsetDecoder onMalformedInput(CodingErrorAction newAction) {
        if (null == newAction) {
            throw new IllegalArgumentException();
        }
        malformAction = newAction;
        implOnMalformedInput(newAction);
        return this;
    }

    /**
     * Sets this decoder's action on unmappable character errors.
     *
     * This method will call the
     * {@link #implOnUnmappableCharacter(CodingErrorAction) implOnUnmappableCharacter}
     * method with the given new action as argument.
     *
     * @param newAction
     *            the new action on unmappable character error.
     * @return this decoder.
     * @throws IllegalArgumentException
     *             if {@code newAction} is {@code null}.
     */
    public final CharsetDecoder onUnmappableCharacter(
            CodingErrorAction newAction) {
        if (null == newAction) {
            throw new IllegalArgumentException();
        }
        unmapAction = newAction;
        implOnUnmappableCharacter(newAction);
        return this;
    }

    /**
     * Gets the replacement string, which is never null or empty.
     *
     * @return the replacement string, cannot be null or empty.
     */
    public final String replacement() {
        return replace;
    }

    /**
     * Sets the new replacement string.
     *
     * This method first checks the given replacement's validity, then changes
     * the replacement value, and at last calls the
     * {@link #implReplaceWith(String) implReplaceWith} method with the given
     * new replacement as argument.
     *
     * @param newReplacement
     *            the replacement string, cannot be null or empty. Its length
     *            cannot be larger than {@link #maxCharsPerByte()}.
     * @return this decoder.
     * @throws IllegalArgumentException
     *             if the given replacement cannot satisfy the requirement
     *             mentioned above.
     */
    public final CharsetDecoder replaceWith(String newReplacement) {
        if (null == newReplacement || newReplacement.length() == 0) {
            throw new IllegalArgumentException("Replacement string cannot be null or empty.");
        }
        if (newReplacement.length() > maxChars) {
            throw new IllegalArgumentException(
                "Replacement string's length cannot be larger than max characters per byte.");
        }
        replace = newReplacement;
        implReplaceWith(newReplacement);
        return this;
    }

    /**
     * Resets this decoder. This method will reset the internal status, and then
     * calls <code>implReset()</code> to reset any status related to the
     * specific charset.
     *
     * @return this decoder.
     */
    public final CharsetDecoder reset() {
        status = INIT;
        implReset();
        return this;
    }

    /**
     * Gets this decoder's <code>CodingErrorAction</code> when an unmappable
     * character error occurred during the decoding process.
     *
     * @return this decoder's <code>CodingErrorAction</code> when an
     *         unmappable character error occurred during the decoding process.
     */
    public CodingErrorAction unmappableCharacterAction() {
        return unmapAction;
    }
}
