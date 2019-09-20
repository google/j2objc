/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2007-2016, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/
package android.icu.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.MissingResourceException;

import android.icu.lang.UCharacter;
import android.icu.math.BigDecimal;
import android.icu.text.NumberFormat;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

/*
 * NumberFormat implementation dedicated/optimized for DateFormat,
 * used by SimpleDateFormat implementation.
 * This class is not thread-safe.
 */
/**
 * @hide Only a subset of ICU is exposed in Android
 */
public final class DateNumberFormat extends NumberFormat {

    private static final long serialVersionUID = -6315692826916346953L;

    private char[] digits;
    private char zeroDigit; // For backwards compatibility
    private char minusSign;
    private boolean positiveOnly = false;

    private static final int DECIMAL_BUF_SIZE = 20; // 20 digits is good enough to store Long.MAX_VALUE
    private transient char[] decimalBuf = new char[DECIMAL_BUF_SIZE];

    private static SimpleCache<ULocale, char[]> CACHE = new SimpleCache<ULocale, char[]>();

    private int maxIntDigits;
    private int minIntDigits;

    public DateNumberFormat(ULocale loc, String digitString, String nsName) {
        initialize(loc,digitString,nsName);
    }

    public DateNumberFormat(ULocale loc, char zeroDigit, String nsName) {
        StringBuffer buf = new StringBuffer();
        for ( int i = 0 ; i < 10 ; i++ ) {
            buf.append((char)(zeroDigit+i));
        }
        initialize(loc,buf.toString(),nsName);
    }

    private void initialize(ULocale loc,String digitString,String nsName) {
        char[] elems = CACHE.get(loc);
        if (elems == null) {
            // Missed cache
            String minusString;
            ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, loc);
            try {
                minusString = rb.getStringWithFallback("NumberElements/"+nsName+"/symbols/minusSign");
            } catch (MissingResourceException ex) {
                if ( !nsName.equals("latn") ) {
                    try {
                       minusString = rb.getStringWithFallback("NumberElements/latn/symbols/minusSign");
                    } catch (MissingResourceException ex1) {
                        minusString = "-";
                    }
                } else {
                    minusString = "-";
                }
            }
            elems = new char[11];
            for ( int i = 0 ; i < 10 ; i++ ) {
                 elems[i] = digitString.charAt(i);
            }
            elems[10] = minusString.charAt(0);
            CACHE.put(loc, elems);
        }

        digits = new char[10];
        System.arraycopy(elems, 0, digits, 0, 10);
        zeroDigit = digits[0];

        minusSign = elems[10];
    }

    @Override
    public void setMaximumIntegerDigits(int newValue) {
        maxIntDigits = newValue;
    }

    @Override
    public int getMaximumIntegerDigits() {
        return maxIntDigits;
    }

    @Override
    public void setMinimumIntegerDigits(int newValue) {
        minIntDigits = newValue;
    }

    @Override
    public int getMinimumIntegerDigits() {
        return minIntDigits;
    }

    /* For supporting SimpleDateFormat.parseInt */
    public void setParsePositiveOnly(boolean isPositiveOnly) {
        positiveOnly = isPositiveOnly;
    }

    public char getZeroDigit() {
        return zeroDigit;
    }

    public void setZeroDigit(char zero) {
        zeroDigit = zero;
        if (digits == null) {
            digits = new char[10];
        }
        digits[0] = zero;
        for ( int i = 1 ; i < 10 ; i++ ) {
            digits[i] = (char)(zero+i);
        }
    }

    public char[] getDigits() {
        return digits.clone();
    }

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo,
            FieldPosition pos) {
        throw new UnsupportedOperationException("StringBuffer format(double, StringBuffer, FieldPostion) is not implemented");
    }

    @Override
    public StringBuffer format(long numberL, StringBuffer toAppendTo,
            FieldPosition pos) {

        if (numberL < 0) {
            // negative
            toAppendTo.append(minusSign);
            numberL = -numberL;
        }

        // Note: NumberFormat used by DateFormat only uses int numbers.
        // Remainder operation on 32bit platform using long is significantly slower
        // than int.  So, this method casts long number into int.
        int number = (int)numberL;

        int limit = decimalBuf.length < maxIntDigits ? decimalBuf.length : maxIntDigits;
        int index = limit - 1;
        while (true) {
            decimalBuf[index] = digits[(number % 10)];
            number /= 10;
            if (index == 0 || number == 0) {
                break;
            }
            index--;
        }
        int padding = minIntDigits - (limit - index);
        for (; padding > 0; padding--) {
            decimalBuf[--index] = digits[0];
        }
        int length = limit - index;
        toAppendTo.append(decimalBuf, index, length);
        pos.setBeginIndex(0);
        if (pos.getField() == NumberFormat.INTEGER_FIELD) {
            pos.setEndIndex(length);
        } else {
            pos.setEndIndex(0);
        }
        return toAppendTo;
    }

    @Override
    public StringBuffer format(BigInteger number, StringBuffer toAppendTo,
            FieldPosition pos) {
        throw new UnsupportedOperationException("StringBuffer format(BigInteger, StringBuffer, FieldPostion) is not implemented");
    }

    @Override
    public StringBuffer format(java.math.BigDecimal number, StringBuffer toAppendTo,
            FieldPosition pos) {
        throw new UnsupportedOperationException("StringBuffer format(BigDecimal, StringBuffer, FieldPostion) is not implemented");
    }

    @Override
    public StringBuffer format(BigDecimal number,
            StringBuffer toAppendTo, FieldPosition pos) {
        throw new UnsupportedOperationException("StringBuffer format(BigDecimal, StringBuffer, FieldPostion) is not implemented");
    }

    /*
     * Note: This method only parse integer numbers which can be represented by long
     */
    private static final long PARSE_THRESHOLD = 922337203685477579L; // (Long.MAX_VALUE / 10) - 1

    @Override
    public Number parse(String text, ParsePosition parsePosition) {
        long num = 0;
        boolean sawNumber = false;
        boolean negative = false;
        int base = parsePosition.getIndex();
        int offset = 0;
        for (; base + offset < text.length(); offset++) {
            char ch = text.charAt(base + offset);
            if (offset == 0 && ch == minusSign) {
                if (positiveOnly) {
                    break;
                }
                negative = true;
            } else {
                int digit = ch - digits[0];
                if (digit < 0 || 9 < digit) {
                    digit = UCharacter.digit(ch);
                }
                if (digit < 0 || 9 < digit) {
                    for ( digit = 0 ; digit < 10 ; digit++ ) {
                        if ( ch == digits[digit]) {
                            break;
                        }
                    }
                }
                if (0 <= digit && digit <= 9 && num < PARSE_THRESHOLD) {
                    sawNumber = true;
                    num = num * 10 + digit;
                } else {
                    break;
                }
            }
        }
        Number result = null;
        if (sawNumber) {
            num = negative ? num * (-1) : num;
            result = Long.valueOf(num);
            parsePosition.setIndex(base + offset);
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !super.equals(obj) || !(obj instanceof DateNumberFormat)) {
            return false;
        }
        DateNumberFormat other = (DateNumberFormat)obj;
        return (this.maxIntDigits == other.maxIntDigits
                && this.minIntDigits == other.minIntDigits
                && this.minusSign == other.minusSign
                && this.positiveOnly == other.positiveOnly
                && Arrays.equals(this.digits, other.digits));
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (digits == null) {
            setZeroDigit(zeroDigit);
        }
        // re-allocate the work buffer
        decimalBuf = new char[DECIMAL_BUF_SIZE];
    }

    @Override
    public Object clone() {
        DateNumberFormat dnfmt = (DateNumberFormat)super.clone();
        dnfmt.digits = this.digits.clone();
        dnfmt.decimalBuf = new char[DECIMAL_BUF_SIZE];
        return dnfmt;
    }
}

//eof
