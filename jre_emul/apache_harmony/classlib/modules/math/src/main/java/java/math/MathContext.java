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

package java.math;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

/**
 * Immutable objects describing settings such as rounding mode and digit
 * precision for the numerical operations provided by class {@link BigDecimal}.
 */
public final class MathContext implements Serializable {

    /**
     * A {@code MathContext} which corresponds to the IEEE 754r quadruple
     * decimal precision format: 34 digit precision and
     * {@link RoundingMode#HALF_EVEN} rounding.
     */
    public static final MathContext DECIMAL128 = new MathContext(34,
            RoundingMode.HALF_EVEN);

    /**
     * A {@code MathContext} which corresponds to the IEEE 754r single decimal
     * precision format: 7 digit precision and {@link RoundingMode#HALF_EVEN}
     * rounding.
     */
    public static final MathContext DECIMAL32 = new MathContext(7,
            RoundingMode.HALF_EVEN);

    /**
     * A {@code MathContext} which corresponds to the IEEE 754r double decimal
     * precision format: 16 digit precision and {@link RoundingMode#HALF_EVEN}
     * rounding.
     */
    public static final MathContext DECIMAL64 = new MathContext(16,
            RoundingMode.HALF_EVEN);

    /**
     * A {@code MathContext} for unlimited precision with
     * {@link RoundingMode#HALF_UP} rounding.
     */
    public static final MathContext UNLIMITED = new MathContext(0,
            RoundingMode.HALF_UP);

    /** This is the serialVersionUID used by the sun implementation */
    private static final long serialVersionUID = 5579720004786848255L;

    /**
     * The number of digits to be used for an operation; results are rounded to
     * this precision.
     */
    private int precision;

    /**
     * A {@code RoundingMode} object which specifies the algorithm to be used
     * for rounding.
     */
    private RoundingMode roundingMode;

    /**
     * An array of {@code char} containing: {@code
     * 'p','r','e','c','i','s','i','o','n','='}. It's used to improve the
     * methods related to {@code String} conversion.
     *
     * @see #MathContext(String)
     * @see #toString()
     */
    private final static char[] chPrecision = { 'p', 'r', 'e', 'c', 'i', 's',
            'i', 'o', 'n', '=' };

    /**
     * An array of {@code char} containing: {@code
     * 'r','o','u','n','d','i','n','g','M','o','d','e','='}. It's used to
     * improve the methods related to {@code String} conversion.
     *
     * @see #MathContext(String)
     * @see #toString()
     */
    private final static char[] chRoundingMode = { 'r', 'o', 'u', 'n', 'd',
            'i', 'n', 'g', 'M', 'o', 'd', 'e', '=' };

    /**
     * Constructs a new {@code MathContext} with the specified precision and
     * with the rounding mode {@link RoundingMode#HALF_UP HALF_UP}. If the
     * precision passed is zero, then this implies that the computations have to
     * be performed exact, the rounding mode in this case is irrelevant.
     *
     * @param precision
     *            the precision for the new {@code MathContext}.
     * @throws IllegalArgumentException
     *             if {@code precision < 0}.
     */
    public MathContext(int precision) {
        this(precision, RoundingMode.HALF_UP);
    }

    /**
     * Constructs a new {@code MathContext} with the specified precision and
     * with the specified rounding mode. If the precision passed is zero, then
     * this implies that the computations have to be performed exact, the
     * rounding mode in this case is irrelevant.
     *
     * @param precision
     *            the precision for the new {@code MathContext}.
     * @param roundingMode
     *            the rounding mode for the new {@code MathContext}.
     * @throws IllegalArgumentException
     *             if {@code precision < 0}.
     * @throws NullPointerException
     *             if {@code roundingMode} is {@code null}.
     */
    public MathContext(int precision, RoundingMode roundingMode) {
        if (precision < 0) {
            throw new IllegalArgumentException("Digits < 0"); //$NON-NLS-1$
        }
        if (roundingMode == null) {
            // math.0D=null RoundingMode
            throw new NullPointerException("null RoundingMode"); //$NON-NLS-1$
        }
        this.precision = precision;
        this.roundingMode = roundingMode;
    }

    /**
     * Constructs a new {@code MathContext} from a string. The string has to
     * specify the precision and the rounding mode to be used and has to follow
     * the following syntax: "precision=&lt;precision&gt; roundingMode=&lt;roundingMode&gt;"
     * This is the same form as the one returned by the {@link #toString}
     * method.
     *
     * @param val
     *            a string describing the precision and rounding mode for the
     *            new {@code MathContext}.
     * @throws IllegalArgumentException
     *             if the string is not in the correct format or if the
     *             precision specified is < 0.
     */
    public MathContext(String val) {
        char[] charVal = val.toCharArray();
        int i; // Index of charVal
        int j; // Index of chRoundingMode
        int digit; // It will contain the digit parsed

        if ((charVal.length < 27) || (charVal.length > 45)) {
            throw new IllegalArgumentException("bad string format"); //$NON-NLS-1$
        }
        // Parsing "precision=" String
        for (i = 0; (i < chPrecision.length) && (charVal[i] == chPrecision[i]); i++) {
            ;
        }

        if (i < chPrecision.length) {
            throw new IllegalArgumentException("bad string format"); //$NON-NLS-1$
        }
        // Parsing the value for "precision="...
        digit = Character.digit(charVal[i], 10);
        if (digit == -1) {
            throw new IllegalArgumentException("bad string format"); //$NON-NLS-1$
        }
        this.precision = this.precision * 10 + digit;
        i++;

        do {
            digit = Character.digit(charVal[i], 10);
            if (digit == -1) {
                if (charVal[i] == ' ') {
                    // It parsed all the digits
                    i++;
                    break;
                }
                // It isn't  a valid digit, and isn't a white space
                throw new IllegalArgumentException("bad string format"); //$NON-NLS-1$
            }
            // Accumulating the value parsed
            this.precision = this.precision * 10 + digit;
            if (this.precision < 0) {
                throw new IllegalArgumentException("bad string format"); //$NON-NLS-1$
            }
            i++;
        } while (true);
        // Parsing "roundingMode="
        for (j = 0; (j < chRoundingMode.length)
                && (charVal[i] == chRoundingMode[j]); i++, j++) {
            ;
        }

        if (j < chRoundingMode.length) {
            throw new IllegalArgumentException("bad string format"); //$NON-NLS-1$
        }
        // Parsing the value for "roundingMode"...
        this.roundingMode = RoundingMode.valueOf(String.valueOf(charVal, i,
                charVal.length - i));
    }

    /* Public Methods */

    /**
     * Returns the precision. The precision is the number of digits used for an
     * operation. Results are rounded to this precision. The precision is
     * guaranteed to be non negative. If the precision is zero, then the
     * computations have to be performed exact, results are not rounded in this
     * case.
     *
     * @return the precision.
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Returns the rounding mode. The rounding mode is the strategy to be used
     * to round results.
     * <p>
     * The rounding mode is one of
     * {@link RoundingMode#UP},
     * {@link RoundingMode#DOWN},
     * {@link RoundingMode#CEILING},
     * {@link RoundingMode#FLOOR},
     * {@link RoundingMode#HALF_UP},
     * {@link RoundingMode#HALF_DOWN},
     * {@link RoundingMode#HALF_EVEN}, or
     * {@link RoundingMode#UNNECESSARY}.
     *
     * @return the rounding mode.
     */
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    /**
     * Returns true if x is a {@code MathContext} with the same precision
     * setting and the same rounding mode as this {@code MathContext} instance.
     *
     * @param x
     *            object to be compared.
     * @return {@code true} if this {@code MathContext} instance is equal to the
     *         {@code x} argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object x) {
        return ((x instanceof MathContext)
                && (((MathContext) x).getPrecision() == precision) && (((MathContext) x)
                .getRoundingMode() == roundingMode));
    }

    /**
     * Returns the hash code for this {@code MathContext} instance.
     *
     * @return the hash code for this {@code MathContext}.
     */
    @Override
    public int hashCode() {
        // Make place for the necessary bits to represent 8 rounding modes
        return ((precision << 3) | roundingMode.ordinal());
    }

    /**
     * Returns the string representation for this {@code MathContext} instance.
     * The string has the form
     * {@code
     * "precision=&lt;precision&gt; roundingMode=&lt;roundingMode&gt;"
     * } where {@code &lt;precision&gt;} is an integer describing the number
     * of digits used for operations and {@code &lt;roundingMode&gt;} is the
     * string representation of the rounding mode.
     *
     * @return a string representation for this {@code MathContext} instance
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(45);

        sb.append(chPrecision);
        sb.append(precision);
        sb.append(' ');
        sb.append(chRoundingMode);
        sb.append(roundingMode);
        return sb.toString();
    }

    /**
     * Makes checks upon deserialization of a {@code MathContext} instance.
     * Checks whether {@code precision >= 0} and {@code roundingMode != null}
     *
     * @throws StreamCorruptedException
     *             if {@code precision < 0}
     * @throws StreamCorruptedException
     *             if {@code roundingMode == null}
     */
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        if (precision < 0) {
            throw new StreamCorruptedException("bad precision value"); //$NON-NLS-1$
        }
        if (roundingMode == null) {
            throw new StreamCorruptedException("null roundingMode"); //$NON-NLS-1$
        }
    }

}
