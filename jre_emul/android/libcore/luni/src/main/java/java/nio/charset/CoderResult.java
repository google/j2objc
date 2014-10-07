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
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Used to indicate the result of encoding/decoding. There are four types of
 * results:
 * <ol>
 * <li>UNDERFLOW indicates that all input has been processed but more input is
 * required. It is represented by the unique object
 * <code>CoderResult.UNDERFLOW</code>.
 * <li>OVERFLOW indicates an insufficient output buffer size. It is represented
 * by the unique object <code>CoderResult.OVERFLOW</code>.
 * <li>A malformed-input error indicates that an unrecognizable sequence of
 * input units has been encountered. Get an instance of this type of result by
 * calling <code>CoderResult.malformedForLength(int)</code> with the length of
 * the malformed-input.
 * <li>An unmappable-character error indicates that a sequence of input units
 * can not be mapped to the output charset. Get an instance of this type of
 * result by calling <code>CoderResult.unmappableForLength(int)</code> with
 * the input sequence size indicating the identity of the unmappable character.
 * </ol>
 */
public class CoderResult {

    // indicating underflow error type
    private static final int TYPE_UNDERFLOW = 1;

    // indicating overflow error type
    private static final int TYPE_OVERFLOW = 2;

    // indicating malformed-input error type
    private static final int TYPE_MALFORMED_INPUT = 3;

    // indicating unmappable character error type
    private static final int TYPE_UNMAPPABLE_CHAR = 4;

    /**
     * Result object indicating that there is insufficient data in the
     * encoding/decoding buffer or that additional data is required.
     */
    public static final CoderResult UNDERFLOW = new CoderResult(TYPE_UNDERFLOW,
            0);

    /**
     * Result object used to indicate that the output buffer does not have
     * enough space available to store the result of the encoding/decoding.
     */
    public static final CoderResult OVERFLOW = new CoderResult(TYPE_OVERFLOW, 0);

    /*
     * Stores unique result objects for each malformed-input error of a certain
     * length
     */
    private static WeakHashMap<Integer, CoderResult> _malformedErrors = new WeakHashMap<Integer, CoderResult>();

    /*
     * Stores unique result objects for each unmappable-character error of a
     * certain length
     */
    private static WeakHashMap<Integer, CoderResult> _unmappableErrors = new WeakHashMap<Integer, CoderResult>();

    // the type of this result
    private final int type;

    // the length of the erroneous input
    private final int length;

    /**
     * Constructs a <code>CoderResult</code> object with its text description.
     *
     * @param type
     *            the type of this result
     * @param length
     *            the length of the erroneous input
     */
    private CoderResult(int type, int length) {
        this.type = type;
        this.length = length;
    }

    /**
     * Gets a <code>CoderResult</code> object indicating a malformed-input
     * error.
     *
     * @param length
     *            the length of the malformed-input.
     * @return a <code>CoderResult</code> object indicating a malformed-input
     *         error.
     * @throws IllegalArgumentException
     *             if <code>length</code> is non-positive.
     */
    public static synchronized CoderResult malformedForLength(int length)
            throws IllegalArgumentException {
        if (length > 0) {
            Integer key = Integer.valueOf(length);
            synchronized (_malformedErrors) {
                CoderResult r = _malformedErrors.get(key);
                if (r == null) {
                    r = new CoderResult(TYPE_MALFORMED_INPUT, length);
                    _malformedErrors.put(key, r);
                }
                return r;
            }
        }
        throw new IllegalArgumentException("length <= 0: " + length);
    }

    /**
     * Gets a <code>CoderResult</code> object indicating an unmappable
     * character error.
     *
     * @param length
     *            the length of the input unit sequence denoting the unmappable
     *            character.
     * @return a <code>CoderResult</code> object indicating an unmappable
     *         character error.
     * @throws IllegalArgumentException
     *             if <code>length</code> is non-positive.
     */
    public static synchronized CoderResult unmappableForLength(int length)
            throws IllegalArgumentException {
        if (length > 0) {
            Integer key = Integer.valueOf(length);
            synchronized (_unmappableErrors) {
                CoderResult r = _unmappableErrors.get(key);
                if (r == null) {
                    r = new CoderResult(TYPE_UNMAPPABLE_CHAR, length);
                    _unmappableErrors.put(key, r);
                }
                return r;
            }
        }
        throw new IllegalArgumentException("length <= 0: " + length);
    }

    /**
     * Returns true if this result is an underflow condition.
     *
     * @return true if an underflow, otherwise false.
     */
    public boolean isUnderflow() {
        return this.type == TYPE_UNDERFLOW;
    }

    /**
     * Returns true if this result represents a malformed-input error or an
     * unmappable-character error.
     *
     * @return true if this is a malformed-input error or an
     *         unmappable-character error, otherwise false.
     */
    public boolean isError() {
        return this.type == TYPE_MALFORMED_INPUT
                || this.type == TYPE_UNMAPPABLE_CHAR;
    }

    /**
     * Returns true if this result represents a malformed-input error.
     *
     * @return true if this is a malformed-input error, otherwise false.
     */
    public boolean isMalformed() {
        return this.type == TYPE_MALFORMED_INPUT;
    }

    /**
     * Returns true if this result is an overflow condition.
     *
     * @return true if this is an overflow, otherwise false.
     */
    public boolean isOverflow() {
        return this.type == TYPE_OVERFLOW;
    }

    /**
     * Returns true if this result represents an unmappable-character error.
     *
     * @return true if this is an unmappable-character error, otherwise false.
     */
    public boolean isUnmappable() {
        return this.type == TYPE_UNMAPPABLE_CHAR;
    }

    /**
     * Gets the length of the erroneous input. The length is only meaningful to
     * a malformed-input error or an unmappable character error.
     *
     * @return the length, as an integer, of this object's erroneous input.
     * @throws UnsupportedOperationException
     *             if this result is an overflow or underflow.
     */
    public int length() throws UnsupportedOperationException {
        if (this.type == TYPE_MALFORMED_INPUT || this.type == TYPE_UNMAPPABLE_CHAR) {
            return this.length;
        }
        throw new UnsupportedOperationException("length meaningless for " + toString());
    }

    /**
     * Throws an exception corresponding to this coder result.
     *
     * @throws BufferUnderflowException
     *             in case this is an underflow.
     * @throws BufferOverflowException
     *             in case this is an overflow.
     * @throws UnmappableCharacterException
     *             in case this is an unmappable-character error.
     * @throws MalformedInputException
     *             in case this is a malformed-input error.
     * @throws CharacterCodingException
     *             the default exception.
     */
    public void throwException() throws BufferUnderflowException,
            BufferOverflowException, UnmappableCharacterException,
            MalformedInputException, CharacterCodingException {
        switch (this.type) {
            case TYPE_UNDERFLOW:
                throw new BufferUnderflowException();
            case TYPE_OVERFLOW:
                throw new BufferOverflowException();
            case TYPE_UNMAPPABLE_CHAR:
                throw new UnmappableCharacterException(this.length);
            case TYPE_MALFORMED_INPUT:
                throw new MalformedInputException(this.length);
            default:
                throw new CharacterCodingException();
        }
    }

    /**
     * Returns a text description of this result.
     *
     * @return a text description of this result.
     */
    @Override
    public String toString() {
        String dsc = null;
        switch (this.type) {
            case TYPE_UNDERFLOW:
                dsc = "UNDERFLOW error";
                break;
            case TYPE_OVERFLOW:
                dsc = "OVERFLOW error";
                break;
            case TYPE_UNMAPPABLE_CHAR:
                dsc = "Unmappable-character error with erroneous input length "
                        + this.length;
                break;
            case TYPE_MALFORMED_INPUT:
                dsc = "Malformed-input error with erroneous input length "
                        + this.length;
                break;
            default:
                dsc = "";
                break;
        }
        return getClass().getName() + "[" + dsc + "]";
    }
}
