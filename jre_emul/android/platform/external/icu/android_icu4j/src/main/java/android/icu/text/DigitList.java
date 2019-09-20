/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2015, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

import java.math.BigInteger;

/**
 * <code>DigitList</code> handles the transcoding between numeric values and
 * strings of characters.  It only represents non-negative numbers.  The
 * division of labor between <code>DigitList</code> and
 * <code>DecimalFormat</code> is that <code>DigitList</code> handles the radix
 * 10 representation issues and numeric conversion, including rounding;
 * <code>DecimalFormat</code> handles the locale-specific issues such as
 * positive and negative representation, digit grouping, decimal point,
 * currency, and so on.
 *
 * <p>A <code>DigitList</code> is a representation of a finite numeric value.
 * <code>DigitList</code> objects do not represent <code>NaN</code> or infinite
 * values.  A <code>DigitList</code> value can be converted to a
 * <code>BigDecimal</code> without loss of precision.  Conversion to other
 * numeric formats may involve loss of precision, depending on the specific
 * value.
 *
 * <p>The <code>DigitList</code> representation consists of a string of
 * characters, which are the digits radix 10, from '0' to '9'.  It also has a
 * base 10 exponent associated with it.  The value represented by a
 * <code>DigitList</code> object can be computed by mulitplying the fraction
 * <em>f</em>, where 0 <= <em>f</em> < 1, derived by placing all the digits of
 * the list to the right of the decimal point, by 10^exponent.
 *
 * @see java.util.Locale
 * @see java.text.Format
 * @see NumberFormat
 * @see DecimalFormat
 * @see java.text.ChoiceFormat
 * @see java.text.MessageFormat
 * @version      1.18 08/12/98
 * @author       Mark Davis, Alan Liu
 * @hide Made public for testing
 * */
public final class DigitList {
    /**
     * The maximum number of significant digits in an IEEE 754 double, that
     * is, in a Java double.  This must not be increased, or garbage digits
     * will be generated, and should not be decreased, or accuracy will be lost.
     */
    public static final int MAX_LONG_DIGITS = 19; // == Long.toString(Long.MAX_VALUE).length()
    public static final int DBL_DIG = 17;

    /**
     * These data members are intentionally public and can be set directly.
     *
     * The value represented is given by placing the decimal point before
     * digits[decimalAt].  If decimalAt is < 0, then leading zeros between
     * the decimal point and the first nonzero digit are implied.  If decimalAt
     * is > count, then trailing zeros between the digits[count-1] and the
     * decimal point are implied.
     *
     * Equivalently, the represented value is given by f * 10^decimalAt.  Here
     * f is a value 0.1 <= f < 1 arrived at by placing the digits in Digits to
     * the right of the decimal.
     *
     * DigitList is normalized, so if it is non-zero, figits[0] is non-zero.  We
     * don't allow denormalized numbers because our exponent is effectively of
     * unlimited magnitude.  The count value contains the number of significant
     * digits present in digits[].
     *
     * Zero is represented by any DigitList with count == 0 or with each digits[i]
     * for all i <= count == '0'.
     */
    public int decimalAt = 0;
    public int count = 0;
    public byte[] digits = new byte[MAX_LONG_DIGITS];

    private final void ensureCapacity(int digitCapacity, int digitsToCopy) {
        if (digitCapacity > digits.length) {
            byte[] newDigits = new byte[digitCapacity * 2];
            System.arraycopy(digits, 0, newDigits, 0, digitsToCopy);
            digits = newDigits;
        }
    }

    /**
     * Return true if the represented number is zero.
     */
    boolean isZero()
    {
        for (int i=0; i<count; ++i) if (digits[i] != '0') return false;
        return true;
    }

// Unused as of ICU 2.6 - alan
//    /**
//     * Clears out the digits.
//     * Use before appending them.
//     * Typically, you set a series of digits with append, then at the point
//     * you hit the decimal point, you set myDigitList.decimalAt = myDigitList.count;
//     * then go on appending digits.
//     */
//    public void clear () {
//        decimalAt = 0;
//        count = 0;
//    }

    /**
     * Appends digits to the list.
     */
    public void append (int digit) {
        ensureCapacity(count+1, count);
        digits[count++] = (byte) digit;
    }
    
    public byte getDigitValue(int i) {
        return (byte) (digits[i] - '0');
    }
    
    /**
     * Utility routine to get the value of the digit list
     * If (count == 0) this throws a NumberFormatException, which
     * mimics Long.parseLong().
     */
    public final double getDouble() {
        if (count == 0) return 0.0;
        StringBuilder temp = new StringBuilder(count);
        temp.append('.');
        for (int i = 0; i < count; ++i) temp.append((char)(digits[i]));
        temp.append('E');
        temp.append(Integer.toString(decimalAt));
        return Double.valueOf(temp.toString()).doubleValue();
        // long value = Long.parseLong(temp.toString());
        // return (value * Math.pow(10, decimalAt - count));
    }

    /**
     * Utility routine to get the value of the digit list.
     * If (count == 0) this returns 0, unlike Long.parseLong().
     */
    public final long getLong() {
        // for now, simple implementation; later, do proper IEEE native stuff

        if (count == 0) return 0;

        // We have to check for this, because this is the one NEGATIVE value
        // we represent.  If we tried to just pass the digits off to parseLong,
        // we'd get a parse failure.
        if (isLongMIN_VALUE()) return Long.MIN_VALUE;

        StringBuilder temp = new StringBuilder(count);
        for (int i = 0; i < decimalAt; ++i)
        {
            temp.append((i < count) ? (char)(digits[i]) : '0');
        }
        return Long.parseLong(temp.toString());
    }

    /**
     * Return a <code>BigInteger</code> representing the value stored in this
     * <code>DigitList</code>.  This method assumes that this object contains
     * an integral value; if not, it will return an incorrect value.
     * [bnf]
     * @param isPositive determines the sign of the returned result
     * @return the value of this object as a <code>BigInteger</code>
     */
    public BigInteger getBigInteger(boolean isPositive) {
        if (isZero()) return BigInteger.valueOf(0);
        //Eclipse stated the following is "dead code"
        /*if (false) {
            StringBuilder stringRep = new StringBuilder(count);
            if (!isPositive) {
                stringRep.append('-');
            }
            for (int i=0; i<count; ++i) {
                stringRep.append((char) digits[i]);
            }
            int d = decimalAt;
            while (d-- > count) {
                stringRep.append('0');
            }
            return new BigInteger(stringRep.toString());
        } else*/ {
            int len = decimalAt > count ? decimalAt : count;
            if (!isPositive) {
                len += 1;
            }
            char[] text = new char[len];
            int n = 0;
            if (!isPositive) {
                text[0] = '-';
                for (int i = 0; i < count; ++i) {
                    text[i+1] = (char)digits[i];
                }
                n = count+1;
            } else {
                for (int i = 0; i < count; ++i) {
                    text[i] = (char)digits[i];
                }
                n = count;
            }
            for (int i = n; i < text.length; ++i) {
                text[i] = '0';
            } 
            return new BigInteger(new String(text));
        }
    }

    private String getStringRep(boolean isPositive) {
        if (isZero()) return "0";
        StringBuilder stringRep = new StringBuilder(count+1);
        if (!isPositive) {
            stringRep.append('-');
        }
        int d = decimalAt;
        if (d < 0) {
            stringRep.append('.');
            while (d < 0) {
                stringRep.append('0');
                ++d;
            }
            d = -1;
        }
        for (int i=0; i<count; ++i) {
            if (d == i) {
                stringRep.append('.');
            }
            stringRep.append((char) digits[i]);
        }
        while (d-- > count) {
            stringRep.append('0');
        }
        return stringRep.toString();
    }

    /**
     * Return an <code>ICU BigDecimal</code> representing the value stored in this
     * <code>DigitList</code>.
     * [bnf]
     * @param isPositive determines the sign of the returned result
     * @return the value of this object as a <code>BigDecimal</code>
     */
    public android.icu.math.BigDecimal getBigDecimalICU(boolean isPositive) {
        if (isZero()) {
            return android.icu.math.BigDecimal.valueOf(0);
        }
        // if exponential notion is negative,
        // we prefer to use BigDecimal constructor with scale,
        // because it works better when extremely small value
        // is used.  See #5698.
        long scale = (long)count - (long)decimalAt;
        if (scale > 0) {
            int numDigits = count;
            if (scale > (long)Integer.MAX_VALUE) {
                // try to reduce the scale
                long numShift = scale - (long)Integer.MAX_VALUE;
                if (numShift < count) {
                    numDigits -= numShift;
                } else {
                    // fallback to 0
                    return new android.icu.math.BigDecimal(0);
                }
            }
            StringBuilder significantDigits = new StringBuilder(numDigits + 1);
            if (!isPositive) {
                significantDigits.append('-');
            }
            for (int i = 0; i < numDigits; i++) {
                significantDigits.append((char)digits[i]);
            }
            BigInteger unscaledVal = new BigInteger(significantDigits.toString());
            return new android.icu.math.BigDecimal(unscaledVal, (int)scale);
        } else {
            return new android.icu.math.BigDecimal(getStringRep(isPositive));
        }
    }

    /**
     * Return whether or not this objects represented value is an integer.
     * [bnf]
     * @return true if the represented value of this object is an integer
     */
    boolean isIntegral() {
        // Trim trailing zeros.  This does not change the represented value.
        while (count > 0 && digits[count - 1] == (byte)'0') --count;
        return count == 0 || decimalAt >= count;
    }

// Unused as of ICU 2.6 - alan
//    /**
//     * Return true if the number represented by this object can fit into
//     * a long.
//     */
//    boolean fitsIntoLong(boolean isPositive)
//    {
//        // Figure out if the result will fit in a long.  We have to
//        // first look for nonzero digits after the decimal point;
//        // then check the size.  If the digit count is 18 or less, then
//        // the value can definitely be represented as a long.  If it is 19
//        // then it may be too large.
//
//        // Trim trailing zeros.  This does not change the represented value.
//        while (count > 0 && digits[count - 1] == (byte)'0') --count;
//
//        if (count == 0) {
//            // Positive zero fits into a long, but negative zero can only
//            // be represented as a double. - bug 4162852
//            return isPositive;
//        }
//
//        if (decimalAt < count || decimalAt > MAX_LONG_DIGITS) return false;
//
//        if (decimalAt < MAX_LONG_DIGITS) return true;
//
//        // At this point we have decimalAt == count, and count == MAX_LONG_DIGITS.
//        // The number will overflow if it is larger than 9223372036854775807
//        // or smaller than -9223372036854775808.
//        for (int i=0; i<count; ++i)
//        {
//            byte dig = digits[i], max = LONG_MIN_REP[i];
//            if (dig > max) return false;
//            if (dig < max) return true;
//        }
//
//        // At this point the first count digits match.  If decimalAt is less
//        // than count, then the remaining digits are zero, and we return true.
//        if (count < decimalAt) return true;
//
//        // Now we have a representation of Long.MIN_VALUE, without the leading
//        // negative sign.  If this represents a positive value, then it does
//        // not fit; otherwise it fits.
//        return !isPositive;
//    }

// Unused as of ICU 2.6 - alan
//    /**
//     * Set the digit list to a representation of the given double value.
//     * This method supports fixed-point notation.
//     * @param source Value to be converted; must not be Inf, -Inf, Nan,
//     * or a value <= 0.
//     * @param maximumFractionDigits The most fractional digits which should
//     * be converted.
//     */
//    public final void set(double source, int maximumFractionDigits)
//    {
//        set(source, maximumFractionDigits, true);
//    }

    /**
     * Set the digit list to a representation of the given double value.
     * This method supports both fixed-point and exponential notation.
     * @param source Value to be converted; must not be Inf, -Inf, Nan,
     * or a value <= 0.
     * @param maximumDigits The most fractional or total digits which should
     * be converted.
     * @param fixedPoint If true, then maximumDigits is the maximum
     * fractional digits to be converted.  If false, total digits.
     */
    final void set(double source, int maximumDigits, boolean fixedPoint)
    {
        if (source == 0) source = 0;
        // Generate a representation of the form DDDDD, DDDDD.DDDDD, or
        // DDDDDE+/-DDDDD.
        String rep = Double.toString(source);

        didRound = false;

        set(rep, MAX_LONG_DIGITS);

        if (fixedPoint) {
            // The negative of the exponent represents the number of leading
            // zeros between the decimal and the first non-zero digit, for
            // a value < 0.1 (e.g., for 0.00123, -decimalAt == 2).  If this
            // is more than the maximum fraction digits, then we have an underflow
            // for the printed representation.
            if (-decimalAt > maximumDigits) {
                count = 0;
                return;
            } else if (-decimalAt == maximumDigits) {
                if (shouldRoundUp(0)) {
                    count = 1;
                    ++decimalAt;
                    digits[0] = (byte)'1';
                } else {
                    count = 0;
                }
                return;
            }
            // else fall through
        }

        // Eliminate trailing zeros.
        while (count > 1 && digits[count - 1] == '0')
            --count;

        // Eliminate digits beyond maximum digits to be displayed.
        // Round up if appropriate.
        round(fixedPoint ? (maximumDigits + decimalAt) : maximumDigits == 0 ? -1 : maximumDigits);
    }

    /**
     * Given a string representation of the form DDDDD, DDDDD.DDDDD,
     * or DDDDDE+/-DDDDD, set this object's value to it.  Ignore
     * any leading '-'.
     */
    private void set(String rep, int maxCount) {
        decimalAt = -1;
        count = 0;
        int exponent = 0;
        // Number of zeros between decimal point and first non-zero digit after
        // decimal point, for numbers < 1.
        int leadingZerosAfterDecimal = 0;
        boolean nonZeroDigitSeen = false;
        // Skip over leading '-'
        int i=0;
        if (rep.charAt(i) == '-') {
            ++i;
        }
        for (; i < rep.length(); ++i) {
            char c = rep.charAt(i);
            if (c == '.') {
                decimalAt = count;
            } else if (c == 'e' || c == 'E') {
                ++i;
                // Integer.parseInt doesn't handle leading '+' signs
                if (rep.charAt(i) == '+') {
                    ++i;
                }
                exponent = Integer.valueOf(rep.substring(i)).intValue();
                break;
            } else if (count < maxCount) {
                if (!nonZeroDigitSeen) {
                    nonZeroDigitSeen = (c != '0');
                    if (!nonZeroDigitSeen && decimalAt != -1) {
                        ++leadingZerosAfterDecimal;
                    }
                }

                if (nonZeroDigitSeen) {
                    ensureCapacity(count+1, count);
                    digits[count++] = (byte)c;
                }
            }
        }
        if (decimalAt == -1) {
            decimalAt = count;
        }
        decimalAt += exponent - leadingZerosAfterDecimal;
    }

    /**
     * Return true if truncating the representation to the given number
     * of digits will result in an increment to the last digit.  This
     * method implements half-even rounding, the default rounding mode.
     * [bnf]
     * @param maximumDigits the number of digits to keep, from 0 to
     * <code>count-1</code>.  If 0, then all digits are rounded away, and
     * this method returns true if a one should be generated (e.g., formatting
     * 0.09 with "#.#").
     * @return true if digit <code>maximumDigits-1</code> should be
     * incremented
     */
    private boolean shouldRoundUp(int maximumDigits) {
        // variable not used boolean increment = false;
        // Implement IEEE half-even rounding
        /*Bug 4243108
          format(0.0) gives "0.1" if preceded by parse("99.99") [Richard/GCL]
        */
        if (maximumDigits < count) {
            if (digits[maximumDigits] > '5') {
                return true;
            } else if (digits[maximumDigits] == '5' ) {
                for (int i=maximumDigits+1; i<count; ++i) {
                    if (digits[i] != '0') {
                        return true;
                    }
                }
                return maximumDigits > 0 && (digits[maximumDigits-1] % 2 != 0);
            }
        }
        return false;
    }

    /**
     * Round the representation to the given number of digits.
     * @param maximumDigits The maximum number of digits to be shown.
     * Upon return, count will be less than or equal to maximumDigits.
     * This now performs rounding when maximumDigits is 0, formerly it did not.
     */
    public final void round(int maximumDigits) {
        // Eliminate digits beyond maximum digits to be displayed.
        // Round up if appropriate.
        // [bnf] rewritten to fix 4179818
        if (maximumDigits >= 0 && maximumDigits < count) {
            if (shouldRoundUp(maximumDigits)) {
                // Rounding up involves incrementing digits from LSD to MSD.
                // In most cases this is simple, but in a worst case situation
                // (9999..99) we have to adjust the decimalAt value.
                for (;;)
                {
                    --maximumDigits;
                    if (maximumDigits < 0)
                    {
                        // We have all 9's, so we increment to a single digit
                        // of one and adjust the exponent.
                        digits[0] = (byte) '1';
                        ++decimalAt;
                        maximumDigits = 0; // Adjust the count
                        didRound = true;
                        break;
                    }

                    ++digits[maximumDigits];
                    didRound = true;
                    if (digits[maximumDigits] <= '9') break;
                    // digits[maximumDigits] = '0'; // Unnecessary since we'll truncate this
                }
                ++maximumDigits; // Increment for use as count
            }
            count = maximumDigits;
        }
        // Bug 4217661 DecimalFormat formats 1.001 to "1.00" instead of "1"
        // Eliminate trailing zeros. [Richard/GCL]
        // [dlf] moved outside if block, see ticket #6408
        while (count > 1 && digits[count-1] == '0') {
          --count;
        }
    }

    // Value to indicate that rounding was done. 
    private boolean didRound = false;
    
    /**
     * Indicates if last digit set was rounded or not.
     * true indicates it was rounded.
     * false indicates rounding has not been done.
     */
    public boolean wasRounded() {
        return didRound;
    }
    
    /**
     * Utility routine to set the value of the digit list from a long
     */
    public final void set(long source)
    {
        set(source, 0);
    }

    /**
     * Set the digit list to a representation of the given long value.
     * @param source Value to be converted; must be >= 0 or ==
     * Long.MIN_VALUE.
     * @param maximumDigits The most digits which should be converted.
     * If maximumDigits is lower than the number of significant digits
     * in source, the representation will be rounded.  Ignored if <= 0.
     */
    public final void set(long source, int maximumDigits)
    {
        // This method does not expect a negative number. However,
        // "source" can be a Long.MIN_VALUE (-9223372036854775808),
        // if the number being formatted is a Long.MIN_VALUE.  In that
        // case, it will be formatted as -Long.MIN_VALUE, a number
        // which is outside the legal range of a long, but which can
        // be represented by DigitList.
        // [NEW] Faster implementation
        didRound = false;
        
        if (source <= 0) {
            if (source == Long.MIN_VALUE) {
                decimalAt = count = MAX_LONG_DIGITS;
                System.arraycopy(LONG_MIN_REP, 0, digits, 0, count);
            } else {
                count = 0;
                decimalAt = 0;
            }
        } else {
            int left = MAX_LONG_DIGITS;
            int right;
            while (source > 0) {
                digits[--left] = (byte) (((long) '0') + (source % 10));
                source /= 10;
            }
            decimalAt = MAX_LONG_DIGITS-left;
            // Don't copy trailing zeros
            // we are guaranteed that there is at least one non-zero digit,
            // so we don't have to check lower bounds
            for (right = MAX_LONG_DIGITS - 1; digits[right] == (byte) '0'; --right) {}
            count = right - left + 1;
            System.arraycopy(digits, left, digits, 0, count);
        }        
        if (maximumDigits > 0) round(maximumDigits);
    }

    /**
     * Set the digit list to a representation of the given BigInteger value.
     * [bnf]
     * @param source Value to be converted
     * @param maximumDigits The most digits which should be converted.
     * If maximumDigits is lower than the number of significant digits
     * in source, the representation will be rounded.  Ignored if <= 0.
     */
    public final void set(BigInteger source, int maximumDigits) {
        String stringDigits = source.toString();

        count = decimalAt = stringDigits.length();
        didRound = false;
        
        // Don't copy trailing zeros
        while (count > 1 && stringDigits.charAt(count - 1) == '0') --count;

        int offset = 0;
        if (stringDigits.charAt(0) == '-') {
            ++offset;
            --count;
            --decimalAt;
        }

        ensureCapacity(count, 0);
        for (int i = 0; i < count; ++i) {
            digits[i] = (byte) stringDigits.charAt(i + offset);
        }

        if (maximumDigits > 0) round(maximumDigits);
    }

    /**
     * Internal method that sets this digit list to represent the
     * given value.  The value is given as a String of the format
     * returned by BigDecimal.
     * @param stringDigits value to be represented with the following
     * syntax, expressed as a regular expression: -?\d*.?\d*
     * Must not be an empty string.
     * @param maximumDigits The most digits which should be converted.
     * If maximumDigits is lower than the number of significant digits
     * in source, the representation will be rounded.  Ignored if <= 0.
     * @param fixedPoint If true, then maximumDigits is the maximum
     * fractional digits to be converted.  If false, total digits.
     */
    private void setBigDecimalDigits(String stringDigits,
                                     int maximumDigits, boolean fixedPoint) {
//|        // Find the first non-zero digit, the decimal, and the last non-zero digit.
//|        int first=-1, last=stringDigits.length()-1, decimal=-1;
//|        for (int i=0; (first<0 || decimal<0) && i<=last; ++i) {
//|            char c = stringDigits.charAt(i);
//|            if (c == '.') {
//|                decimal = i;
//|            } else if (first < 0 && (c >= '1' && c <= '9')) {
//|                first = i;
//|            }
//|        }
//|
//|        if (first < 0) {
//|            clear();
//|            return;
//|        }
//|
//|        // At this point we know there is at least one non-zero digit, so the
//|        // following loop is safe.
//|        for (;;) {
//|            char c = stringDigits.charAt(last);
//|            if (c != '0' && c != '.') {
//|                break;
//|            }
//|            --last;
//|        }
//|
//|        if (decimal < 0) {
//|            decimal = stringDigits.length();
//|        }
//|
//|        count = last - first;
//|        if (decimal < first || decimal > last) {
//|            ++count;
//|        }
//|        decimalAt = decimal - first;
//|        if (decimalAt < 0) {
//|            ++decimalAt;
//|        }
//|
//|        ensureCapacity(count, 0);
//|        for (int i = 0; i < count; ++i) {
//|            digits[i] = (byte) stringDigits.charAt(first++);
//|            if (first == decimal) {
//|                ++first;
//|            }
//|        }

        didRound = false;

        // The maxDigits here could also be Integer.MAX_VALUE
        set(stringDigits, stringDigits.length());

        // Eliminate digits beyond maximum digits to be displayed.
        // Round up if appropriate.
    // {dlf} Some callers depend on passing '0' to round to mean 'don't round', but
    // rather than pass that information explicitly, we rely on some magic with maximumDigits
    // and decimalAt.  Unfortunately, this is no good, because there are cases where maximumDigits
    // is zero and we do want to round, e.g. BigDecimal values -1 < x < 1.  So since round
    // changed to perform rounding when the argument is 0, we now force the argument
    // to -1 in the situations where it matters.
        round(fixedPoint ? (maximumDigits + decimalAt) : maximumDigits == 0 ? -1 : maximumDigits);
    }

    /**
     * Set the digit list to a representation of the given BigDecimal value.
     * [bnf]
     * @param source Value to be converted
     * @param maximumDigits The most digits which should be converted.
     * If maximumDigits is lower than the number of significant digits
     * in source, the representation will be rounded.  Ignored if <= 0.
     * @param fixedPoint If true, then maximumDigits is the maximum
     * fractional digits to be converted.  If false, total digits.
     */
    public final void set(java.math.BigDecimal source,
                          int maximumDigits, boolean fixedPoint) {
        setBigDecimalDigits(source.toString(), maximumDigits, fixedPoint);
    }

    /*
     * Set the digit list to a representation of the given BigDecimal value.
     * [bnf]
     * @param source Value to be converted
     * @param maximumDigits The most digits which should be converted.
     * If maximumDigits is lower than the number of significant digits
     * in source, the representation will be rounded.  Ignored if <= 0.
     * @param fixedPoint If true, then maximumDigits is the maximum
     * fractional digits to be converted.  If false, total digits.
     */
    public final void set(android.icu.math.BigDecimal source,
                          int maximumDigits, boolean fixedPoint) {
        setBigDecimalDigits(source.toString(), maximumDigits, fixedPoint);
    }

    /**
     * Returns true if this DigitList represents Long.MIN_VALUE;
     * false, otherwise.  This is required so that getLong() works.
     */
    private boolean isLongMIN_VALUE()
    {
        if (decimalAt != count || count != MAX_LONG_DIGITS)
            return false;

            for (int i = 0; i < count; ++i)
        {
            if (digits[i] != LONG_MIN_REP[i]) return false;
        }

        return true;
    }

    private static byte[] LONG_MIN_REP;

    static
    {
        // Store the representation of LONG_MIN without the leading '-'
        String s = Long.toString(Long.MIN_VALUE);
        LONG_MIN_REP = new byte[MAX_LONG_DIGITS];
        for (int i=0; i < MAX_LONG_DIGITS; ++i)
        {
            LONG_MIN_REP[i] = (byte)s.charAt(i + 1);
        }
    }

// Unused -- Alan 2003-05
//    /**
//     * Return the floor of the log base 10 of a given double.
//     * This method compensates for inaccuracies which arise naturally when
//     * computing logs, and always give the correct value.  The parameter
//     * must be positive and finite.
//     */
//    private static final int log10(double d)
//    {
//        // The reason this routine is needed is that simply taking the
//        // log and dividing by log10 yields a result which may be off
//        // by 1 due to rounding errors.  For example, the naive log10
//        // of 1.0e300 taken this way is 299, rather than 300.
//        double log10 = Math.log(d) / LOG10;
//        int ilog10 = (int)Math.floor(log10);
//        // Positive logs could be too small, e.g. 0.99 instead of 1.0
//        if (log10 > 0 && d >= Math.pow(10, ilog10 + 1))
//        {
//            ++ilog10;
//        }
//        // Negative logs could be too big, e.g. -0.99 instead of -1.0
//        else if (log10 < 0 && d < Math.pow(10, ilog10))
//        {
//            --ilog10;
//        }
//        return ilog10;
//    }
//
//    private static final double LOG10 = Math.log(10.0);

    /**
     * equality test between two digit lists.
     */
    public boolean equals(Object obj) {
        if (this == obj)                      // quick check
            return true;
        if (!(obj instanceof DigitList))         // (1) same object?
            return false;
        DigitList other = (DigitList) obj;
        if (count != other.count ||
        decimalAt != other.decimalAt)
            return false;
        for (int i = 0; i < count; i++)
            if (digits[i] != other.digits[i])
                return false;
        return true;
    }

    /**
     * Generates the hash code for the digit list.
     */
    public int hashCode() {
        int hashcode = decimalAt;

        for (int i = 0; i < count; i++)
            hashcode = hashcode * 37 + digits[i];

        return hashcode;
    }

    public String toString()
    {
        if (isZero()) return "0";
        StringBuilder buf = new StringBuilder("0.");
        for (int i=0; i<count; ++i) buf.append((char)digits[i]);
        buf.append("x10^");
        buf.append(decimalAt);
        return buf.toString();
    }
}
