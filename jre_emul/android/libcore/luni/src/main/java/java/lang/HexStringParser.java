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

package java.lang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Parses hex string to a single or double precision floating point number.
 *
 * TODO: rewrite this!
 *
 * @hide
 */
final class HexStringParser {

    private static final int DOUBLE_EXPONENT_WIDTH = 11;

    private static final int DOUBLE_MANTISSA_WIDTH = 52;

    private static final int FLOAT_EXPONENT_WIDTH = 8;

    private static final int FLOAT_MANTISSA_WIDTH = 23;

    private static final int HEX_RADIX = 16;

    private static final int MAX_SIGNIFICANT_LENGTH = 15;

    private static final String HEX_SIGNIFICANT = "0[xX](\\p{XDigit}+\\.?|\\p{XDigit}*\\.\\p{XDigit}+)";

    private static final String BINARY_EXPONENT = "[pP]([+-]?\\d+)";

    private static final String FLOAT_TYPE_SUFFIX = "[fFdD]?";

    private static final String HEX_PATTERN = "[\\x00-\\x20]*([+-]?)" + HEX_SIGNIFICANT
            + BINARY_EXPONENT + FLOAT_TYPE_SUFFIX + "[\\x00-\\x20]*";

    private static final Pattern PATTERN = Pattern.compile(HEX_PATTERN);

    private final int EXPONENT_WIDTH;

    private final int MANTISSA_WIDTH;

    private final long EXPONENT_BASE;

    private final long MAX_EXPONENT;

    private final long MIN_EXPONENT;

    private final long MANTISSA_MASK;

    private long sign;

    private long exponent;

    private long mantissa;

    private String abandonedNumber="";

    public HexStringParser(int exponentWidth, int mantissaWidth) {
        this.EXPONENT_WIDTH = exponentWidth;
        this.MANTISSA_WIDTH = mantissaWidth;

        this.EXPONENT_BASE = ~(-1L << (exponentWidth - 1));
        this.MAX_EXPONENT = ~(-1L << exponentWidth);
        this.MIN_EXPONENT = -(MANTISSA_WIDTH + 1);
        this.MANTISSA_MASK = ~(-1L << mantissaWidth);
    }

    /*
     * Parses the hex string to a double number.
     */
    public static double parseDouble(String hexString) {
        HexStringParser parser = new HexStringParser(DOUBLE_EXPONENT_WIDTH, DOUBLE_MANTISSA_WIDTH);
        long result = parser.parse(hexString, true);
        return Double.longBitsToDouble(result);
    }

    /*
     * Parses the hex string to a float number.
     */
    public static float parseFloat(String hexString) {
        HexStringParser parser = new HexStringParser(FLOAT_EXPONENT_WIDTH, FLOAT_MANTISSA_WIDTH);
        int result = (int) parser.parse(hexString, false);
        return Float.intBitsToFloat(result);
    }

    private long parse(String hexString, boolean isDouble) {
        Matcher matcher = PATTERN.matcher(hexString);
        if (!matcher.matches()) {
            throw new NumberFormatException("Invalid hex " + (isDouble ? "double" : "float")+ ":" +
                    hexString);
        }

        String signStr = matcher.group(1);
        String significantStr = matcher.group(2);
        String exponentStr = matcher.group(3);

        parseHexSign(signStr);
        parseExponent(exponentStr);
        parseMantissa(significantStr);

        sign <<= (MANTISSA_WIDTH + EXPONENT_WIDTH);
        exponent <<= MANTISSA_WIDTH;
        return sign | exponent | mantissa;
    }

    /*
     * Parses the sign field.
     */
    private void parseHexSign(String signStr) {
        this.sign = signStr.equals("-") ? 1 : 0;
    }

    /*
     * Parses the exponent field.
     */
    private void parseExponent(String exponentStr) {
        char leadingChar = exponentStr.charAt(0);
        int expSign = (leadingChar == '-' ? -1 : 1);
        if (!Character.isDigit(leadingChar)) {
            exponentStr = exponentStr.substring(1);
        }

        try {
            exponent = expSign * Long.parseLong(exponentStr);
            checkedAddExponent(EXPONENT_BASE);
        } catch (NumberFormatException e) {
            exponent = expSign * Long.MAX_VALUE;
        }
    }

    /*
     * Parses the mantissa field.
     */
    private void parseMantissa(String significantStr) {
        String[] strings = significantStr.split("\\.");
        String strIntegerPart = strings[0];
        String strDecimalPart = strings.length > 1 ? strings[1] : "";

        String significand = getNormalizedSignificand(strIntegerPart,strDecimalPart);
        if (significand.equals("0")) {
            setZero();
            return;
        }

        int offset = getOffset(strIntegerPart, strDecimalPart);
        checkedAddExponent(offset);

        if (exponent >= MAX_EXPONENT) {
            setInfinite();
            return;
        }

        if (exponent <= MIN_EXPONENT) {
            setZero();
            return;
        }

        if (significand.length() > MAX_SIGNIFICANT_LENGTH) {
            abandonedNumber = significand.substring(MAX_SIGNIFICANT_LENGTH);
            significand = significand.substring(0, MAX_SIGNIFICANT_LENGTH);
        }

        mantissa = Long.parseLong(significand, HEX_RADIX);

        if (exponent >= 1) {
            processNormalNumber();
        } else{
            processSubNormalNumber();
        }

    }

    private void setInfinite() {
        exponent = MAX_EXPONENT;
        mantissa = 0;
    }

    private void setZero() {
        exponent = 0;
        mantissa = 0;
    }

    /*
     * Sets the exponent variable to Long.MAX_VALUE or -Long.MAX_VALUE if
     * overflow or underflow happens.
     */
    private void checkedAddExponent(long offset) {
        long result = exponent + offset;
        int expSign = Long.signum(exponent);
        if (expSign * Long.signum(offset) > 0 && expSign * Long.signum(result) < 0) {
            exponent = expSign * Long.MAX_VALUE;
        } else {
            exponent = result;
        }
    }

    private void processNormalNumber(){
        int desiredWidth = MANTISSA_WIDTH + 2;
        fitMantissaInDesiredWidth(desiredWidth);
        round();
        mantissa = mantissa & MANTISSA_MASK;
    }

    private void processSubNormalNumber(){
        int desiredWidth = MANTISSA_WIDTH + 1;
        desiredWidth += (int)exponent;//lends bit from mantissa to exponent
        exponent = 0;
        fitMantissaInDesiredWidth(desiredWidth);
        round();
        mantissa = mantissa & MANTISSA_MASK;
    }

    /*
     * Adjusts the mantissa to desired width for further analysis.
     */
    private void fitMantissaInDesiredWidth(int desiredWidth){
        int bitLength = countBitsLength(mantissa);
        if (bitLength > desiredWidth) {
            discardTrailingBits(bitLength - desiredWidth);
        } else {
            mantissa <<= (desiredWidth - bitLength);
        }
    }

    /*
     * Stores the discarded bits to abandonedNumber.
     */
    private void discardTrailingBits(long num) {
        long mask = ~(-1L << num);
        abandonedNumber += (mantissa & mask);
        mantissa >>= num;
    }

    /*
     * The value is rounded up or down to the nearest infinitely precise result.
     * If the value is exactly halfway between two infinitely precise results,
     * then it should be rounded up to the nearest infinitely precise even.
     */
    private void round() {
        String result = abandonedNumber.replaceAll("0+", "");
        boolean moreThanZero = (result.length() > 0 ? true : false);

        int lastDiscardedBit = (int) (mantissa & 1L);
        mantissa >>= 1;
        int tailBitInMantissa = (int) (mantissa & 1L);

        if (lastDiscardedBit == 1 && (moreThanZero || tailBitInMantissa == 1)) {
            int oldLength = countBitsLength(mantissa);
            mantissa += 1L;
            int newLength = countBitsLength(mantissa);

            //Rounds up to exponent when whole bits of mantissa are one-bits.
            if (oldLength >= MANTISSA_WIDTH && newLength > oldLength) {
                checkedAddExponent(1);
            }
        }
    }

    /*
     * Returns the normalized significand after removing the leading zeros.
     */
    private String getNormalizedSignificand(String strIntegerPart, String strDecimalPart) {
        String significand = strIntegerPart + strDecimalPart;
        significand = significand.replaceFirst("^0+", "");
        if (significand.length() == 0) {
            significand = "0";
        }
        return significand;
    }

    /*
     * Calculates the offset between the normalized number and unnormalized
     * number. In a normalized representation, significand is represented by the
     * characters "0x1." followed by a lowercase hexadecimal representation of
     * the rest of the significand as a fraction.
     */
    private int getOffset(String strIntegerPart, String strDecimalPart) {
        strIntegerPart = strIntegerPart.replaceFirst("^0+", "");

        //If the Integer part is a nonzero number.
        if (strIntegerPart.length() != 0) {
            String leadingNumber = strIntegerPart.substring(0, 1);
            return (strIntegerPart.length() - 1) * 4 + countBitsLength(Long.parseLong(leadingNumber,HEX_RADIX)) - 1;
        }

        //If the Integer part is a zero number.
        int i;
        for (i = 0; i < strDecimalPart.length() && strDecimalPart.charAt(i) == '0'; i++);
        if (i == strDecimalPart.length()) {
            return 0;
        }
        String leadingNumber=strDecimalPart.substring(i,i + 1);
        return (-i - 1) * 4 + countBitsLength(Long.parseLong(leadingNumber, HEX_RADIX)) - 1;
    }

    private int countBitsLength(long value) {
        int leadingZeros = Long.numberOfLeadingZeros(value);
        return Long.SIZE - leadingZeros;
    }
}
