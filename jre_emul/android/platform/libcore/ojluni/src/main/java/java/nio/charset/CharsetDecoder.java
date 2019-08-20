/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

// -- This file was mechanically generated: Do not edit! -- //

package java.nio.charset;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;


/**
 * An engine that can transform a sequence of bytes in a specific charset into a sequence of
 * sixteen-bit Unicode characters.
 *
 * <a name="steps"></a>
 *
 * <p> The input byte sequence is provided in a byte buffer or a series
 * of such buffers.  The output character sequence is written to a character buffer
 * or a series of such buffers.  A decoder should always be used by making
 * the following sequence of method invocations, hereinafter referred to as a
 * <i>decoding operation</i>:
 *
 * <ol>
 *
 *   <li><p> Reset the decoder via the {@link #reset reset} method, unless it
 *   has not been used before; </p></li>
 *
 *   <li><p> Invoke the {@link #decode decode} method zero or more times, as
 *   long as additional input may be available, passing <tt>false</tt> for the
 *   <tt>endOfInput</tt> argument and filling the input buffer and flushing the
 *   output buffer between invocations; </p></li>
 *
 *   <li><p> Invoke the {@link #decode decode} method one final time, passing
 *   <tt>true</tt> for the <tt>endOfInput</tt> argument; and then </p></li>
 *
 *   <li><p> Invoke the {@link #flush flush} method so that the decoder can
 *   flush any internal state to the output buffer. </p></li>
 *
 * </ol>
 *
 * Each invocation of the {@link #decode decode} method will decode as many
 * bytes as possible from the input buffer, writing the resulting characters
 * to the output buffer.  The {@link #decode decode} method returns when more
 * input is required, when there is not enough room in the output buffer, or
 * when a decoding error has occurred.  In each case a {@link CoderResult}
 * object is returned to describe the reason for termination.  An invoker can
 * examine this object and fill the input buffer, flush the output buffer, or
 * attempt to recover from a decoding error, as appropriate, and try again.
 *
 * <a name="ce"></a>
 *
 * <p> There are two general types of decoding errors.  If the input byte
 * sequence is not legal for this charset then the input is considered <i>malformed</i>.  If
 * the input byte sequence is legal but cannot be mapped to a valid
 * Unicode character then an <i>unmappable character</i> has been encountered.
 *
 * <a name="cae"></a>
 *
 * <p> How a decoding error is handled depends upon the action requested for
 * that type of error, which is described by an instance of the {@link
 * CodingErrorAction} class.  The possible error actions are to {@linkplain
 * CodingErrorAction#IGNORE ignore} the erroneous input, {@linkplain
 * CodingErrorAction#REPORT report} the error to the invoker via
 * the returned {@link CoderResult} object, or {@linkplain CodingErrorAction#REPLACE
 * replace} the erroneous input with the current value of the
 * replacement string.  The replacement
 *





 * has the initial value <tt>"&#92;uFFFD"</tt>;

 *
 * its value may be changed via the {@link #replaceWith(java.lang.String)
 * replaceWith} method.
 *
 * <p> The default action for malformed-input and unmappable-character errors
 * is to {@linkplain CodingErrorAction#REPORT report} them.  The
 * malformed-input error action may be changed via the {@link
 * #onMalformedInput(CodingErrorAction) onMalformedInput} method; the
 * unmappable-character action may be changed via the {@link
 * #onUnmappableCharacter(CodingErrorAction) onUnmappableCharacter} method.
 *
 * <p> This class is designed to handle many of the details of the decoding
 * process, including the implementation of error actions.  A decoder for a
 * specific charset, which is a concrete subclass of this class, need only
 * implement the abstract {@link #decodeLoop decodeLoop} method, which
 * encapsulates the basic decoding loop.  A subclass that maintains internal
 * state should, additionally, override the {@link #implFlush implFlush} and
 * {@link #implReset implReset} methods.
 *
 * <p> Instances of this class are not safe for use by multiple concurrent
 * threads.  </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see ByteBuffer
 * @see CharBuffer
 * @see Charset
 * @see CharsetEncoder
 */

public abstract class CharsetDecoder {

    private final Charset charset;
    private final float averageCharsPerByte;
    private final float maxCharsPerByte;

    private String replacement;
    private CodingErrorAction malformedInputAction
        = CodingErrorAction.REPORT;
    private CodingErrorAction unmappableCharacterAction
        = CodingErrorAction.REPORT;

    // Internal states
    //
    private static final int ST_RESET   = 0;
    private static final int ST_CODING  = 1;
    private static final int ST_END     = 2;
    private static final int ST_FLUSHED = 3;

    private int state = ST_RESET;

    private static String stateNames[]
        = { "RESET", "CODING", "CODING_END", "FLUSHED" };


    /**
     * Initializes a new decoder.  The new decoder will have the given
     * chars-per-byte and replacement values.
     *
     * @param  cs
     *         The charset that created this decoder
     *
     * @param  averageCharsPerByte
     *         A positive float value indicating the expected number of
     *         characters that will be produced for each input byte
     *
     * @param  maxCharsPerByte
     *         A positive float value indicating the maximum number of
     *         characters that will be produced for each input byte
     *
     * @param  replacement
     *         The initial replacement; must not be <tt>null</tt>, must have
     *         non-zero length, must not be longer than maxCharsPerByte,
     *         and must be {@linkplain #isLegalReplacement legal}
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     */
    private
    CharsetDecoder(Charset cs,
                   float averageCharsPerByte,
                   float maxCharsPerByte,
                   String replacement)
    {
        this.charset = cs;
        if (averageCharsPerByte <= 0.0f)
            throw new IllegalArgumentException("Non-positive "
                                               + "averageCharsPerByte");
        if (maxCharsPerByte <= 0.0f)
            throw new IllegalArgumentException("Non-positive "
                                               + "maxCharsPerByte");
        /* J2ObjC removed.
        if (!Charset.atBugLevel("1.4")) {*/
            if (averageCharsPerByte > maxCharsPerByte)
                throw new IllegalArgumentException("averageCharsPerByte"
                                                   + " exceeds "
                                                   + "maxCharsPerByte");
        //}
        this.replacement = replacement;
        this.averageCharsPerByte = averageCharsPerByte;
        this.maxCharsPerByte = maxCharsPerByte;
        // Android-removed
        // replaceWith(replacement);
    }

    /**
     * Initializes a new decoder.  The new decoder will have the given
     * chars-per-byte values and its replacement will be the
     * string <tt>"&#92;uFFFD"</tt>.
     *
     * @param  cs
     *         The charset that created this decoder
     *
     * @param  averageCharsPerByte
     *         A positive float value indicating the expected number of
     *         characters that will be produced for each input byte
     *
     * @param  maxCharsPerByte
     *         A positive float value indicating the maximum number of
     *         characters that will be produced for each input byte
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     */
    protected CharsetDecoder(Charset cs,
                             float averageCharsPerByte,
                             float maxCharsPerByte)
    {
        this(cs,
             averageCharsPerByte, maxCharsPerByte,
             "\uFFFD");
    }

    /**
     * Returns the charset that created this decoder.
     *
     * @return  This decoder's charset
     */
    public final Charset charset() {
        return charset;
    }

    /**
     * Returns this decoder's replacement value.
     *
     * @return  This decoder's current replacement,
     *          which is never <tt>null</tt> and is never empty
     */
    public final String replacement() {

        return replacement;




    }

    /**
     * Changes this decoder's replacement value.
     *
     * <p> This method invokes the {@link #implReplaceWith implReplaceWith}
     * method, passing the new replacement, after checking that the new
     * replacement is acceptable.  </p>
     *
     * @param  newReplacement  The replacement value
     *

     *         The new replacement; must not be <tt>null</tt>
     *         and must have non-zero length







     *
     * @return  This decoder
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameter do not hold
     */
    public final CharsetDecoder replaceWith(String newReplacement) {
        if (newReplacement == null)
            throw new IllegalArgumentException("Null replacement");
        int len = newReplacement.length();
        if (len == 0)
            throw new IllegalArgumentException("Empty replacement");
        if (len > maxCharsPerByte)
            throw new IllegalArgumentException("Replacement too long");

        this.replacement = newReplacement;






        implReplaceWith(this.replacement);
        return this;
    }

    /**
     * Reports a change to this decoder's replacement value.
     *
     * <p> The default implementation of this method does nothing.  This method
     * should be overridden by decoders that require notification of changes to
     * the replacement.  </p>
     *
     * @param  newReplacement    The replacement value
     */
    protected void implReplaceWith(String newReplacement) {
    }









































    /**
     * Returns this decoder's current action for malformed-input errors.
     *
     * @return The current malformed-input action, which is never <tt>null</tt>
     */
    public CodingErrorAction malformedInputAction() {
        return malformedInputAction;
    }

    /**
     * Changes this decoder's action for malformed-input errors.
     *
     * <p> This method invokes the {@link #implOnMalformedInput
     * implOnMalformedInput} method, passing the new action.  </p>
     *
     * @param  newAction  The new action; must not be <tt>null</tt>
     *
     * @return  This decoder
     *
     * @throws IllegalArgumentException
     *         If the precondition on the parameter does not hold
     */
    public final CharsetDecoder onMalformedInput(CodingErrorAction newAction) {
        if (newAction == null)
            throw new IllegalArgumentException("Null action");
        malformedInputAction = newAction;
        implOnMalformedInput(newAction);
        return this;
    }

    /**
     * Reports a change to this decoder's malformed-input action.
     *
     * <p> The default implementation of this method does nothing.  This method
     * should be overridden by decoders that require notification of changes to
     * the malformed-input action.  </p>
     *
     * @param  newAction  The new action
     */
    protected void implOnMalformedInput(CodingErrorAction newAction) { }

    /**
     * Returns this decoder's current action for unmappable-character errors.
     *
     * @return The current unmappable-character action, which is never
     *         <tt>null</tt>
     */
    public CodingErrorAction unmappableCharacterAction() {
        return unmappableCharacterAction;
    }

    /**
     * Changes this decoder's action for unmappable-character errors.
     *
     * <p> This method invokes the {@link #implOnUnmappableCharacter
     * implOnUnmappableCharacter} method, passing the new action.  </p>
     *
     * @param  newAction  The new action; must not be <tt>null</tt>
     *
     * @return  This decoder
     *
     * @throws IllegalArgumentException
     *         If the precondition on the parameter does not hold
     */
    public final CharsetDecoder onUnmappableCharacter(CodingErrorAction
                                                      newAction)
    {
        if (newAction == null)
            throw new IllegalArgumentException("Null action");
        unmappableCharacterAction = newAction;
        implOnUnmappableCharacter(newAction);
        return this;
    }

    /**
     * Reports a change to this decoder's unmappable-character action.
     *
     * <p> The default implementation of this method does nothing.  This method
     * should be overridden by decoders that require notification of changes to
     * the unmappable-character action.  </p>
     *
     * @param  newAction  The new action
     */
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) { }

    /**
     * Returns the average number of characters that will be produced for each
     * byte of input.  This heuristic value may be used to estimate the size
     * of the output buffer required for a given input sequence.
     *
     * @return  The average number of characters produced
     *          per byte of input
     */
    public final float averageCharsPerByte() {
        return averageCharsPerByte;
    }

    /**
     * Returns the maximum number of characters that will be produced for each
     * byte of input.  This value may be used to compute the worst-case size
     * of the output buffer required for a given input sequence.
     *
     * @return  The maximum number of characters that will be produced per
     *          byte of input
     */
    public final float maxCharsPerByte() {
        return maxCharsPerByte;
    }

    /**
     * Decodes as many bytes as possible from the given input buffer,
     * writing the results to the given output buffer.
     *
     * <p> The buffers are read from, and written to, starting at their current
     * positions.  At most {@link Buffer#remaining in.remaining()} bytes
     * will be read and at most {@link Buffer#remaining out.remaining()}
     * characters will be written.  The buffers' positions will be advanced to
     * reflect the bytes read and the characters written, but their marks and
     * limits will not be modified.
     *
     * <p> In addition to reading bytes from the input buffer and writing
     * characters to the output buffer, this method returns a {@link CoderResult}
     * object to describe its reason for termination:
     *
     * <ul>
     *
     *   <li><p> {@link CoderResult#UNDERFLOW} indicates that as much of the
     *   input buffer as possible has been decoded.  If there is no further
     *   input then the invoker can proceed to the next step of the
     *   <a href="#steps">decoding operation</a>.  Otherwise this method
     *   should be invoked again with further input.  </p></li>
     *
     *   <li><p> {@link CoderResult#OVERFLOW} indicates that there is
     *   insufficient space in the output buffer to decode any more bytes.
     *   This method should be invoked again with an output buffer that has
     *   more {@linkplain Buffer#remaining remaining} characters. This is
     *   typically done by draining any decoded characters from the output
     *   buffer.  </p></li>
     *
     *   <li><p> A {@linkplain CoderResult#malformedForLength
     *   malformed-input} result indicates that a malformed-input
     *   error has been detected.  The malformed bytes begin at the input
     *   buffer's (possibly incremented) position; the number of malformed
     *   bytes may be determined by invoking the result object's {@link
     *   CoderResult#length() length} method.  This case applies only if the
     *   {@linkplain #onMalformedInput malformed action} of this decoder
     *   is {@link CodingErrorAction#REPORT}; otherwise the malformed input
     *   will be ignored or replaced, as requested.  </p></li>
     *
     *   <li><p> An {@linkplain CoderResult#unmappableForLength
     *   unmappable-character} result indicates that an
     *   unmappable-character error has been detected.  The bytes that
     *   decode the unmappable character begin at the input buffer's (possibly
     *   incremented) position; the number of such bytes may be determined
     *   by invoking the result object's {@link CoderResult#length() length}
     *   method.  This case applies only if the {@linkplain #onUnmappableCharacter
     *   unmappable action} of this decoder is {@link
     *   CodingErrorAction#REPORT}; otherwise the unmappable character will be
     *   ignored or replaced, as requested.  </p></li>
     *
     * </ul>
     *
     * In any case, if this method is to be reinvoked in the same decoding
     * operation then care should be taken to preserve any bytes remaining
     * in the input buffer so that they are available to the next invocation.
     *
     * <p> The <tt>endOfInput</tt> parameter advises this method as to whether
     * the invoker can provide further input beyond that contained in the given
     * input buffer.  If there is a possibility of providing additional input
     * then the invoker should pass <tt>false</tt> for this parameter; if there
     * is no possibility of providing further input then the invoker should
     * pass <tt>true</tt>.  It is not erroneous, and in fact it is quite
     * common, to pass <tt>false</tt> in one invocation and later discover that
     * no further input was actually available.  It is critical, however, that
     * the final invocation of this method in a sequence of invocations always
     * pass <tt>true</tt> so that any remaining undecoded input will be treated
     * as being malformed.
     *
     * <p> This method works by invoking the {@link #decodeLoop decodeLoop}
     * method, interpreting its results, handling error conditions, and
     * reinvoking it as necessary.  </p>
     *
     *
     * @param  in
     *         The input byte buffer
     *
     * @param  out
     *         The output character buffer
     *
     * @param  endOfInput
     *         <tt>true</tt> if, and only if, the invoker can provide no
     *         additional input bytes beyond those in the given buffer
     *
     * @return  A coder-result object describing the reason for termination
     *
     * @throws  IllegalStateException
     *          If a decoding operation is already in progress and the previous
     *          step was an invocation neither of the {@link #reset reset}
     *          method, nor of this method with a value of <tt>false</tt> for
     *          the <tt>endOfInput</tt> parameter, nor of this method with a
     *          value of <tt>true</tt> for the <tt>endOfInput</tt> parameter
     *          but a return value indicating an incomplete decoding operation
     *
     * @throws  CoderMalfunctionError
     *          If an invocation of the decodeLoop method threw
     *          an unexpected exception
     */
    public final CoderResult decode(ByteBuffer in, CharBuffer out,
                                    boolean endOfInput)
    {
        int newState = endOfInput ? ST_END : ST_CODING;
        if ((state != ST_RESET) && (state != ST_CODING)
            && !(endOfInput && (state == ST_END)))
            throwIllegalStateException(state, newState);
        state = newState;

        for (;;) {

            CoderResult cr;
            try {
                cr = decodeLoop(in, out);
            } catch (BufferUnderflowException x) {
                throw new CoderMalfunctionError(x);
            } catch (BufferOverflowException x) {
                throw new CoderMalfunctionError(x);
            }

            if (cr.isOverflow())
                return cr;

            if (cr.isUnderflow()) {
                if (endOfInput && in.hasRemaining()) {
                    cr = CoderResult.malformedForLength(in.remaining());
                    // Fall through to malformed-input case
                } else {
                    return cr;
                }
            }

            CodingErrorAction action = null;
            if (cr.isMalformed())
                action = malformedInputAction;
            else if (cr.isUnmappable())
                action = unmappableCharacterAction;
            else
                assert false : cr.toString();

            if (action == CodingErrorAction.REPORT)
                return cr;

            if (action == CodingErrorAction.REPLACE) {
                if (out.remaining() < replacement.length())
                    return CoderResult.OVERFLOW;
                out.put(replacement);
            }

            if ((action == CodingErrorAction.IGNORE)
                || (action == CodingErrorAction.REPLACE)) {
                // Skip erroneous input either way
                in.position(in.position() + cr.length());
                continue;
            }

            assert false;
        }

    }

    /**
     * Flushes this decoder.
     *
     * <p> Some decoders maintain internal state and may need to write some
     * final characters to the output buffer once the overall input sequence has
     * been read.
     *
     * <p> Any additional output is written to the output buffer beginning at
     * its current position.  At most {@link Buffer#remaining out.remaining()}
     * characters will be written.  The buffer's position will be advanced
     * appropriately, but its mark and limit will not be modified.
     *
     * <p> If this method completes successfully then it returns {@link
     * CoderResult#UNDERFLOW}.  If there is insufficient room in the output
     * buffer then it returns {@link CoderResult#OVERFLOW}.  If this happens
     * then this method must be invoked again, with an output buffer that has
     * more room, in order to complete the current <a href="#steps">decoding
     * operation</a>.
     *
     * <p> If this decoder has already been flushed then invoking this method
     * has no effect.
     *
     * <p> This method invokes the {@link #implFlush implFlush} method to
     * perform the actual flushing operation.  </p>
     *
     * @param  out
     *         The output character buffer
     *
     * @return  A coder-result object, either {@link CoderResult#UNDERFLOW} or
     *          {@link CoderResult#OVERFLOW}
     *
     * @throws  IllegalStateException
     *          If the previous step of the current decoding operation was an
     *          invocation neither of the {@link #flush flush} method nor of
     *          the three-argument {@link
     *          #decode(ByteBuffer,CharBuffer,boolean) decode} method
     *          with a value of <tt>true</tt> for the <tt>endOfInput</tt>
     *          parameter
     */
    public final CoderResult flush(CharBuffer out) {
        if (state == ST_END) {
            CoderResult cr = implFlush(out);
            if (cr.isUnderflow())
                state = ST_FLUSHED;
            return cr;
        }

        if (state != ST_FLUSHED)
            throwIllegalStateException(state, ST_FLUSHED);

        return CoderResult.UNDERFLOW; // Already flushed
    }

    /**
     * Flushes this decoder.
     *
     * <p> The default implementation of this method does nothing, and always
     * returns {@link CoderResult#UNDERFLOW}.  This method should be overridden
     * by decoders that may need to write final characters to the output buffer
     * once the entire input sequence has been read. </p>
     *
     * @param  out
     *         The output character buffer
     *
     * @return  A coder-result object, either {@link CoderResult#UNDERFLOW} or
     *          {@link CoderResult#OVERFLOW}
     */
    protected CoderResult implFlush(CharBuffer out) {
        return CoderResult.UNDERFLOW;
    }

    /**
     * Resets this decoder, clearing any internal state.
     *
     * <p> This method resets charset-independent state and also invokes the
     * {@link #implReset() implReset} method in order to perform any
     * charset-specific reset actions.  </p>
     *
     * @return  This decoder
     *
     */
    public final CharsetDecoder reset() {
        implReset();
        state = ST_RESET;
        return this;
    }

    /**
     * Resets this decoder, clearing any charset-specific internal state.
     *
     * <p> The default implementation of this method does nothing.  This method
     * should be overridden by decoders that maintain internal state.  </p>
     */
    protected void implReset() { }

    /**
     * Decodes one or more bytes into one or more characters.
     *
     * <p> This method encapsulates the basic decoding loop, decoding as many
     * bytes as possible until it either runs out of input, runs out of room
     * in the output buffer, or encounters a decoding error.  This method is
     * invoked by the {@link #decode decode} method, which handles result
     * interpretation and error recovery.
     *
     * <p> The buffers are read from, and written to, starting at their current
     * positions.  At most {@link Buffer#remaining in.remaining()} bytes
     * will be read, and at most {@link Buffer#remaining out.remaining()}
     * characters will be written.  The buffers' positions will be advanced to
     * reflect the bytes read and the characters written, but their marks and
     * limits will not be modified.
     *
     * <p> This method returns a {@link CoderResult} object to describe its
     * reason for termination, in the same manner as the {@link #decode decode}
     * method.  Most implementations of this method will handle decoding errors
     * by returning an appropriate result object for interpretation by the
     * {@link #decode decode} method.  An optimized implementation may instead
     * examine the relevant error action and implement that action itself.
     *
     * <p> An implementation of this method may perform arbitrary lookahead by
     * returning {@link CoderResult#UNDERFLOW} until it receives sufficient
     * input.  </p>
     *
     * @param  in
     *         The input byte buffer
     *
     * @param  out
     *         The output character buffer
     *
     * @return  A coder-result object describing the reason for termination
     */
    protected abstract CoderResult decodeLoop(ByteBuffer in,
                                              CharBuffer out);

    /**
     * Convenience method that decodes the remaining content of a single input
     * byte buffer into a newly-allocated character buffer.
     *
     * <p> This method implements an entire <a href="#steps">decoding
     * operation</a>; that is, it resets this decoder, then it decodes the
     * bytes in the given byte buffer, and finally it flushes this
     * decoder.  This method should therefore not be invoked if a decoding
     * operation is already in progress.  </p>
     *
     * @param  in
     *         The input byte buffer
     *
     * @return A newly-allocated character buffer containing the result of the
     *         decoding operation.  The buffer's position will be zero and its
     *         limit will follow the last character written.
     *
     * @throws  IllegalStateException
     *          If a decoding operation is already in progress
     *
     * @throws  MalformedInputException
     *          If the byte sequence starting at the input buffer's current
     *          position is not legal for this charset and the current malformed-input action
     *          is {@link CodingErrorAction#REPORT}
     *
     * @throws  UnmappableCharacterException
     *          If the byte sequence starting at the input buffer's current
     *          position cannot be mapped to an equivalent character sequence and
     *          the current unmappable-character action is {@link
     *          CodingErrorAction#REPORT}
     */
    public final CharBuffer decode(ByteBuffer in)
        throws CharacterCodingException
    {
        int n = (int)(in.remaining() * averageCharsPerByte());
        CharBuffer out = CharBuffer.allocate(n);

        if ((n == 0) && (in.remaining() == 0))
            return out;
        reset();
        for (;;) {
            CoderResult cr = in.hasRemaining() ?
                decode(in, out, true) : CoderResult.UNDERFLOW;
            if (cr.isUnderflow())
                cr = flush(out);

            if (cr.isUnderflow())
                break;
            if (cr.isOverflow()) {
                n = 2*n + 1;    // Ensure progress; n might be 0!
                CharBuffer o = CharBuffer.allocate(n);
                out.flip();
                o.put(out);
                out = o;
                continue;
            }
            cr.throwException();
        }
        out.flip();
        return out;
    }



    /**
     * Tells whether or not this decoder implements an auto-detecting charset.
     *
     * <p> The default implementation of this method always returns
     * <tt>false</tt>; it should be overridden by auto-detecting decoders to
     * return <tt>true</tt>.  </p>
     *
     * @return  <tt>true</tt> if, and only if, this decoder implements an
     *          auto-detecting charset
     */
    public boolean isAutoDetecting() {
        return false;
    }

    /**
     * Tells whether or not this decoder has yet detected a
     * charset&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> If this decoder implements an auto-detecting charset then at a
     * single point during a decoding operation this method may start returning
     * <tt>true</tt> to indicate that a specific charset has been detected in
     * the input byte sequence.  Once this occurs, the {@link #detectedCharset
     * detectedCharset} method may be invoked to retrieve the detected charset.
     *
     * <p> That this method returns <tt>false</tt> does not imply that no bytes
     * have yet been decoded.  Some auto-detecting decoders are capable of
     * decoding some, or even all, of an input byte sequence without fixing on
     * a particular charset.
     *
     * <p> The default implementation of this method always throws an {@link
     * UnsupportedOperationException}; it should be overridden by
     * auto-detecting decoders to return <tt>true</tt> once the input charset
     * has been determined.  </p>
     *
     * @return  <tt>true</tt> if, and only if, this decoder has detected a
     *          specific charset
     *
     * @throws  UnsupportedOperationException
     *          If this decoder does not implement an auto-detecting charset
     */
    public boolean isCharsetDetected() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves the charset that was detected by this
     * decoder&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> If this decoder implements an auto-detecting charset then this
     * method returns the actual charset once it has been detected.  After that
     * point, this method returns the same value for the duration of the
     * current decoding operation.  If not enough input bytes have yet been
     * read to determine the actual charset then this method throws an {@link
     * IllegalStateException}.
     *
     * <p> The default implementation of this method always throws an {@link
     * UnsupportedOperationException}; it should be overridden by
     * auto-detecting decoders to return the appropriate value.  </p>
     *
     * @return  The charset detected by this auto-detecting decoder,
     *          or <tt>null</tt> if the charset has not yet been determined
     *
     * @throws  IllegalStateException
     *          If insufficient bytes have been read to determine a charset
     *
     * @throws  UnsupportedOperationException
     *          If this decoder does not implement an auto-detecting charset
     */
    public Charset detectedCharset() {
        throw new UnsupportedOperationException();
    }
































































































    private void throwIllegalStateException(int from, int to) {
        throw new IllegalStateException("Current state = " + stateNames[from]
                                        + ", new state = " + stateNames[to]);
    }

}
