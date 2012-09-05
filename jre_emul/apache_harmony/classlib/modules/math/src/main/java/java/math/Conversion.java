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

/**
 * Static library that provides {@link BigInteger} base conversion from/to any
 * integer represented in an {@link java.lang.String} Object.
 */
class Conversion {

    /** Just to denote that this class can't be instantiated */
    private Conversion() {}

    /**
     * Holds the maximal exponent for each radix, so that radix<sup>digitFitInInt[radix]</sup>
     * fit in an {@code int} (32 bits).
     */
    static final int[] digitFitInInt = { -1, -1, 31, 19, 15, 13, 11,
            11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 5 };

    /**
     * bigRadices values are precomputed maximal powers of radices (integer
     * numbers from 2 to 36) that fit into unsigned int (32 bits). bigRadices[0] =
     * 2 ^ 31, bigRadices[8] = 10 ^ 9, etc.
     */

    static final int bigRadices[] = { -2147483648, 1162261467,
            1073741824, 1220703125, 362797056, 1977326743, 1073741824,
            387420489, 1000000000, 214358881, 429981696, 815730721, 1475789056,
            170859375, 268435456, 410338673, 612220032, 893871739, 1280000000,
            1801088541, 113379904, 148035889, 191102976, 244140625, 308915776,
            387420489, 481890304, 594823321, 729000000, 887503681, 1073741824,
            1291467969, 1544804416, 1838265625, 60466176 };

    
    /** @see BigInteger#toString(int) */
    static String bigInteger2String(BigInteger val, int radix) {
        int sign = val.sign;
        int numberLength = val.numberLength;
        int digits[] = val.digits;

        if (sign == 0) {
            return "0"; //$NON-NLS-1$
        }
        if (numberLength == 1) {
            int highDigit = digits[numberLength - 1];
            long v = highDigit & 0xFFFFFFFFL;
            if (sign < 0) {
                v = -v;
            }
            return Long.toString(v, radix);
        }
        if ((radix == 10) || (radix < Character.MIN_RADIX)
                || (radix > Character.MAX_RADIX)) {
            return val.toString();
        }
        double bitsForRadixDigit;
        bitsForRadixDigit = Math.log(radix) / Math.log(2);
        int resLengthInChars = (int) (val.abs().bitLength() / bitsForRadixDigit + ((sign < 0) ? 1
                : 0)) + 1;

        char result[] = new char[resLengthInChars];
        int currentChar = resLengthInChars;
        int resDigit;
        if (radix != 16) {
            int temp[] = new int[numberLength];
            System.arraycopy(digits, 0, temp, 0, numberLength);
            int tempLen = numberLength;
            int charsPerInt = digitFitInInt[radix];
            int i;
            // get the maximal power of radix that fits in int
            int bigRadix = bigRadices[radix - 2];
            while (true) {
                // divide the array of digits by bigRadix and convert remainders
                // to characters collecting them in the char array
                resDigit = Division.divideArrayByInt(temp, temp, tempLen,
                        bigRadix);
                int previous = currentChar;
                do {
                    result[--currentChar] = Character.forDigit(
                            resDigit % radix, radix);
                } while (((resDigit /= radix) != 0) && (currentChar != 0));
                int delta = charsPerInt - previous + currentChar;
                for (i = 0; i < delta && currentChar > 0; i++) {
                    result[--currentChar] = '0';
                }
                for (i = tempLen - 1; (i > 0) && (temp[i] == 0); i--) {
                    ;
                }
                tempLen = i + 1;
                if ((tempLen == 1) && (temp[0] == 0)) { // the quotient is 0
                    break;
                }
            }
        } else {
            // radix == 16
            for (int i = 0; i < numberLength; i++) {
                for (int j = 0; (j < 8) && (currentChar > 0); j++) {
                    resDigit = digits[i] >> (j << 2) & 0xf;
                    result[--currentChar] = Character.forDigit(resDigit, 16);
                }
            }
        }
        while (result[currentChar] == '0') {
            currentChar++;
        }
        if (sign == -1) {
            result[--currentChar] = '-';
        }
        return new String(result, currentChar, resLengthInChars - currentChar);
    }

    /**
     * Builds the correspondent {@code String} representation of {@code val}
     * being scaled by {@code scale}.
     * 
     * @see BigInteger#toString()
     * @see BigDecimal#toString()
     */
    static String toDecimalScaledString(BigInteger val, int scale) {
        int sign = val.sign;
        int numberLength = val.numberLength;
        int digits[] = val.digits;
        int resLengthInChars;
        int currentChar;
        char result[];

        if (sign == 0) {
            switch (scale) {
                case 0:
                    return "0"; //$NON-NLS-1$
                case 1:
                    return "0.0"; //$NON-NLS-1$
                case 2:
                    return "0.00"; //$NON-NLS-1$
                case 3:
                    return "0.000"; //$NON-NLS-1$
                case 4:
                    return "0.0000"; //$NON-NLS-1$
                case 5:
                    return "0.00000"; //$NON-NLS-1$
                case 6:
                    return "0.000000"; //$NON-NLS-1$
                default:
                    StringBuilder result1 = new StringBuilder();
                    if (scale < 0) {
                        result1.append("0E+"); //$NON-NLS-1$
                    } else {
                        result1.append("0E"); //$NON-NLS-1$
                    }
                    result1.append(-scale);
                    return result1.toString();
            }
        }
        // one 32-bit unsigned value may contains 10 decimal digits
        resLengthInChars = numberLength * 10 + 1 + 7;
        // Explanation why +1+7:
        // +1 - one char for sign if needed.
        // +7 - For "special case 2" (see below) we have 7 free chars for
        // inserting necessary scaled digits.
        result = new char[resLengthInChars + 1];
        // allocated [resLengthInChars+1] characters.
        // a free latest character may be used for "special case 1" (see
        // below)
        currentChar = resLengthInChars;
        if (numberLength == 1) {
            int highDigit = digits[0];
            if (highDigit < 0) {
                long v = highDigit & 0xFFFFFFFFL;
                do {
                    long prev = v;
                    v /= 10;
                    result[--currentChar] = (char) (0x0030 + ((int) (prev - v * 10)));
                } while (v != 0);
            } else {
                int v = highDigit;
                do {
                    int prev = v;
                    v /= 10;
                    result[--currentChar] = (char) (0x0030 + (prev - v * 10));
                } while (v != 0);
            }
        } else {
            int temp[] = new int[numberLength];
            int tempLen = numberLength;
            System.arraycopy(digits, 0, temp, 0, tempLen);
            BIG_LOOP: while (true) {
                // divide the array of digits by bigRadix and convert
                // remainders
                // to characters collecting them in the char array
                long result11 = 0;
                for (int i1 = tempLen - 1; i1 >= 0; i1--) {
                    long temp1 = (result11 << 32)
                            + (temp[i1] & 0xFFFFFFFFL);
                    long res = divideLongByBillion(temp1);
                    temp[i1] = (int) res;
                    result11 = (int) (res >> 32);
                }
                int resDigit = (int) result11;
                int previous = currentChar;
                do {
                    result[--currentChar] = (char) (0x0030 + (resDigit % 10));
                } while (((resDigit /= 10) != 0) && (currentChar != 0));
                int delta = 9 - previous + currentChar;
                for (int i = 0; (i < delta) && (currentChar > 0); i++) {
                    result[--currentChar] = '0';
                }
                int j = tempLen - 1;
                for (; temp[j] == 0; j--) {
                    if (j == 0) { // means temp[0] == 0
                        break BIG_LOOP;
                    }
                }
                tempLen = j + 1;
            }
            while (result[currentChar] == '0') {
                currentChar++;
            }
        }
        boolean negNumber = (sign < 0);
        int exponent = resLengthInChars - currentChar - scale - 1;
        if (scale == 0) {
            if (negNumber) {
                result[--currentChar] = '-';
            }
            return new String(result, currentChar, resLengthInChars
                    - currentChar);
        }
        if ((scale > 0) && (exponent >= -6)) {
            if (exponent >= 0) {
                // special case 1
                int insertPoint = currentChar + exponent;
                for (int j = resLengthInChars - 1; j >= insertPoint; j--) {
                    result[j + 1] = result[j];
                }
                result[++insertPoint] = '.';
                if (negNumber) {
                    result[--currentChar] = '-';
                }
                return new String(result, currentChar, resLengthInChars
                        - currentChar + 1);
            }
            // special case 2
            for (int j = 2; j < -exponent + 1; j++) {
                result[--currentChar] = '0';
            }
            result[--currentChar] = '.';
            result[--currentChar] = '0';
            if (negNumber) {
                result[--currentChar] = '-';
            }
            return new String(result, currentChar, resLengthInChars
                    - currentChar);
        }
        int startPoint = currentChar + 1;
        int endPoint = resLengthInChars;
        StringBuilder result1 = new StringBuilder(16 + endPoint - startPoint);
        if (negNumber) {
            result1.append('-');
        }
        if (endPoint - startPoint >= 1) {
            result1.append(result[currentChar]);
            result1.append('.');
            result1.append(result, currentChar + 1, resLengthInChars
                    - currentChar - 1);
        } else {
            result1.append(result, currentChar, resLengthInChars
                    - currentChar);
        }
        result1.append('E');
        if (exponent > 0) {
            result1.append('+');
        }
        result1.append(Integer.toString(exponent));
        return result1.toString();
    }

    /* can process only 32-bit numbers */
    static String toDecimalScaledString(long value, int scale) {
        int resLengthInChars;
        int currentChar;
        char result[];
        boolean negNumber = value < 0;
        if(negNumber) {
            value = -value;
        }
        if (value == 0) {
            switch (scale) {
                case 0: return "0"; //$NON-NLS-1$
                case 1: return "0.0"; //$NON-NLS-1$
                case 2: return "0.00"; //$NON-NLS-1$
                case 3: return "0.000"; //$NON-NLS-1$
                case 4: return "0.0000"; //$NON-NLS-1$
                case 5: return "0.00000"; //$NON-NLS-1$
                case 6: return "0.000000"; //$NON-NLS-1$
                default:
                    StringBuilder result1 = new StringBuilder();
                    if (scale  < 0) {
                        result1.append("0E+"); //$NON-NLS-1$
                    } else {
                        result1.append("0E"); //$NON-NLS-1$
                    }
                    result1.append( (scale == Integer.MIN_VALUE) ? "2147483648" : Integer.toString(-scale)); //$NON-NLS-1$
                    return result1.toString();
            }
        }
        // one 32-bit unsigned value may contains 10 decimal digits
        resLengthInChars = 18;
        // Explanation why +1+7:
        // +1 - one char for sign if needed.
        // +7 - For "special case 2" (see below) we have 7 free chars for
        //  inserting necessary scaled digits.
        result = new char[resLengthInChars+1];
        //  Allocated [resLengthInChars+1] characters.
        // a free latest character may be used for "special case 1" (see below)
        currentChar = resLengthInChars;
        long v = value;
        do {
            long prev = v;
            v /= 10;
            result[--currentChar] = (char) (0x0030 + (prev - v * 10));
        } while (v != 0);
        
        long exponent = (long)resLengthInChars - (long)currentChar - scale - 1L;
        if (scale == 0) {
            if (negNumber) {
                result[--currentChar] = '-';
            }
            return new String(result, currentChar, resLengthInChars - currentChar);
        }
        if (scale > 0 && exponent >= -6) {
            if (exponent >= 0) {
                // special case 1
                int insertPoint = currentChar + (int) exponent ;
                for(int j=resLengthInChars-1; j>=insertPoint; j--) {
                    result[j+1] = result[j];
                }
                result[++insertPoint]='.';
                if (negNumber) {
                    result[--currentChar] = '-';
                }
                return new String(result, currentChar, resLengthInChars - currentChar + 1);
            }
            // special case 2
            for (int j = 2; j < -exponent + 1; j++) {
                result[--currentChar] = '0';
            }
            result[--currentChar] = '.';
            result[--currentChar] = '0';
            if (negNumber) {
                result[--currentChar] = '-';
            }
            return new String(result, currentChar, resLengthInChars - currentChar);
        }
        int startPoint = currentChar + 1;
        int endPoint = resLengthInChars;
        StringBuilder result1 = new StringBuilder(16+endPoint-startPoint);
        if (negNumber) {
            result1.append('-');
        }
        if (endPoint - startPoint >= 1) {
            result1.append(result[currentChar]);
            result1.append('.');
            result1.append(result,currentChar+1,resLengthInChars - currentChar-1);
        } else {
            result1.append(result,currentChar,resLengthInChars - currentChar);
        }
        result1.append('E');
        if (exponent > 0) {
            result1.append('+');
        }
        result1.append(Long.toString(exponent));
        return result1.toString();
    }
    
    static long divideLongByBillion(long a) {
        long quot;
        long rem;

        if (a >= 0) {
            long bLong = 1000000000L;
            quot = (a / bLong);
            rem = (a % bLong);
        } else {
            /*
             * Make the dividend positive shifting it right by 1 bit then get
             * the quotient an remainder and correct them properly
             */
            long aPos = a >>> 1;
            long bPos = 1000000000L >>> 1;
            quot = aPos / bPos;
            rem = aPos % bPos;
            // double the remainder and add 1 if 'a' is odd
            rem = (rem << 1) + (a & 1);
        }
        return ((rem << 32) | (quot & 0xFFFFFFFFL));
    }

    /** @see BigInteger#doubleValue() */
    static double bigInteger2Double(BigInteger val) {
        // val.bitLength() < 64
        if ((val.numberLength < 2)
                || ((val.numberLength == 2) && (val.digits[1] > 0))) {
            return val.longValue();
        }
        // val.bitLength() >= 33 * 32 > 1024
        if (val.numberLength > 32) {
            return ((val.sign > 0) ? Double.POSITIVE_INFINITY
                    : Double.NEGATIVE_INFINITY);
        }
        int bitLen = val.abs().bitLength();
        long exponent = bitLen - 1;
        int delta = bitLen - 54;
        // We need 54 top bits from this, the 53th bit is always 1 in lVal.
        long lVal = val.abs().shiftRight(delta).longValue();
        /*
         * Take 53 bits from lVal to mantissa. The least significant bit is
         * needed for rounding.
         */
        long mantissa = lVal & 0x1FFFFFFFFFFFFFL;
        if (exponent == 1023) {
            if (mantissa == 0X1FFFFFFFFFFFFFL) {
                return ((val.sign > 0) ? Double.POSITIVE_INFINITY
                        : Double.NEGATIVE_INFINITY);
            }
            if (mantissa == 0x1FFFFFFFFFFFFEL) {
                return ((val.sign > 0) ? Double.MAX_VALUE : -Double.MAX_VALUE);
            }
        }
        // Round the mantissa
        if (((mantissa & 1) == 1)
                && (((mantissa & 2) == 2) || BitLevel.nonZeroDroppedBits(delta,
                        val.digits))) {
            mantissa += 2;
        }
        mantissa >>= 1; // drop the rounding bit
        long resSign = (val.sign < 0) ? 0x8000000000000000L : 0;
        exponent = ((1023 + exponent) << 52) & 0x7FF0000000000000L;
        long result = resSign | exponent | mantissa;
        return Double.longBitsToDouble(result);
    }
}
