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

/**
 * Used to parse a string and return either a single or double precision
 * floating point number.
 * @hide
 */
final class StringToReal {

    private static final class StringExponentPair {
        String s;
        long e;
        boolean negative;

        // Flags for two special non-error failure cases.
        boolean infinity;
        boolean zero;

        public float specialValue() {
            if (infinity) {
                return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
            }
            return negative ? -0.0f : 0.0f;
        }
    }

    /**
     * Takes a String and an integer exponent. The String should hold a positive
     * integer value (or zero). The exponent will be used to calculate the
     * floating point number by taking the positive integer the String
     * represents and multiplying by 10 raised to the power of the of the
     * exponent. Returns the closest double value to the real number, or Double.longBitsToDouble(-1).
     */
    private static native double parseDblImpl(String s, int e);

    /**
     * Takes a String and an integer exponent. The String should hold a positive
     * integer value (or zero). The exponent will be used to calculate the
     * floating point number by taking the positive integer the String
     * represents and multiplying by 10 raised to the power of the of the
     * exponent. Returns the closest float value to the real number, or Float.intBitsToFloat(-1).
     */
    private static native float parseFltImpl(String s, int e);

    private static NumberFormatException invalidReal(String s, boolean isDouble) {
        throw new NumberFormatException("Invalid " + (isDouble ? "double" : "float") + ": \"" + s + "\"");
    }

    /**
     * Returns a StringExponentPair containing a String with no leading or trailing white
     * space and trailing zeroes eliminated. The exponent of the
     * StringExponentPair will be used to calculate the floating point number by
     * taking the positive integer the String represents and multiplying by 10
     * raised to the power of the of the exponent.
     */
    private static StringExponentPair initialParse(String s, int length, boolean isDouble) {
        StringExponentPair result = new StringExponentPair();
        if (length == 0) {
            throw invalidReal(s, isDouble);
        }
        result.negative = (s.charAt(0) == '-');

        // We ignore trailing double or float indicators; the method you called determines
        // what you'll get.
        char c = s.charAt(length - 1);
        if (c == 'D' || c == 'd' || c == 'F' || c == 'f') {
            length--;
            if (length == 0) {
                throw invalidReal(s, isDouble);
            }
        }

        int end = Math.max(s.indexOf('E'), s.indexOf('e'));
        if (end != -1) {
            // Is there anything after the 'e'?
            if (end + 1 == length) {
                throw invalidReal(s, isDouble);
            }

            // Do we have an optional explicit sign?
            int exponentOffset = end + 1;
            boolean negativeExponent = false;
            char firstExponentChar = s.charAt(exponentOffset);
            if (firstExponentChar == '+' || firstExponentChar == '-') {
                negativeExponent = (firstExponentChar == '-');
                ++exponentOffset;
            }

            // Do we have a valid positive integer?
            String exponentString = s.substring(exponentOffset, length);
            if (exponentString.isEmpty()) {
                throw invalidReal(s, isDouble);
            }
            for (int i = 0; i < exponentString.length(); ++i) {
                char ch = exponentString.charAt(i);
                if (ch < '0' || ch > '9') {
                    throw invalidReal(s, isDouble);
                }
            }

            // Parse the integer exponent.
            try {
                result.e = Integer.parseInt(exponentString);
                if (negativeExponent) {
                    result.e = -result.e;
                }
            } catch (NumberFormatException ex) {
                // We already checked the string, so the exponent must have been out of range for an int.
                if (negativeExponent) {
                    result.zero = true;
                } else {
                    result.infinity = true;
                }
                return result;
            }
        } else {
            end = length;
        }
        if (length == 0) {
            throw invalidReal(s, isDouble);
        }

        int start = 0;
        c = s.charAt(start);
        if (c == '-') {
            ++start;
            --length;
            result.negative = true;
        } else if (c == '+') {
            ++start;
            --length;
        }
        if (length == 0) {
            throw invalidReal(s, isDouble);
        }

        int decimal = s.indexOf('.');
        if (decimal > -1) {
            result.e -= end - decimal - 1;
            s = s.substring(start, decimal) + s.substring(decimal + 1, end);
        } else {
            s = s.substring(start, end);
        }

        if ((length = s.length()) == 0) {
            throw invalidReal(s, isDouble);
        }

        end = length;
        while (end > 1 && s.charAt(end - 1) == '0') {
            --end;
        }

        start = 0;
        while (start < end - 1 && s.charAt(start) == '0') {
            start++;
        }

        if (end != length || start != 0) {
            result.e += length - end;
            s = s.substring(start, end);
        }

        // This is a hack for https://issues.apache.org/jira/browse/HARMONY-329
        // Trim the length of very small numbers, natives can only handle down
        // to E-309
        final int APPROX_MIN_MAGNITUDE = -359;
        final int MAX_DIGITS = 52;
        length = s.length();
        if (length > MAX_DIGITS && result.e < APPROX_MIN_MAGNITUDE) {
            int d = Math.min(APPROX_MIN_MAGNITUDE - (int) result.e, length - 1);
            s = s.substring(0, length - d);
            result.e += d;
        }

        // This is a hack for https://issues.apache.org/jira/browse/HARMONY-6641
        // The magic 1024 was determined experimentally; the more plausible -324 and +309 were
        // not sufficient to pass both our tests and harmony's tests.
        if (result.e < -1024) {
            result.zero = true;
            return result;
        } else if (result.e > 1024) {
            result.infinity = true;
            return result;
        }

        result.s = s;
        return result;
    }

    // Parses "+Nan", "NaN", "-Nan", "+Infinity", "Infinity", and "-Infinity", case-insensitively.
    private static float parseName(String name, boolean isDouble) {
        // Explicit sign?
        boolean negative = false;
        int i = 0;
        int length = name.length();
        char firstChar = name.charAt(i);
        if (firstChar == '-') {
            negative = true;
            ++i;
            --length;
        } else if (firstChar == '+') {
            ++i;
            --length;
        }

        if (length == 8 && name.regionMatches(false, i, "Infinity", 0, 8)) {
            return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
        }
        if (length == 3 && name.regionMatches(false, i, "NaN", 0, 3)) {
            return Float.NaN;
        }
        throw invalidReal(name, isDouble);
    }

    /**
     * Returns the closest double value to the real number in the string.
     *
     * @param s
     *            the String that will be parsed to a floating point
     * @return the double closest to the real number
     *
     * @throws NumberFormatException
     *                if the String doesn't represent a double
     */
    public static double parseDouble(String s) {
        s = s.trim();
        int length = s.length();

        if (length == 0) {
            throw invalidReal(s, true);
        }

        // See if this could be a named double
        char last = s.charAt(length - 1);
        if (last == 'y' || last == 'N') {
            return parseName(s, true);
        }

        // See if it could be a hexadecimal representation.
        // We don't use startsWith because there might be a leading sign.
        if (s.indexOf("0x") != -1 || s.indexOf("0X") != -1) {
            return HexStringParser.parseDouble(s);
        }

        StringExponentPair info = initialParse(s, length, true);
        if (info.infinity || info.zero) {
            return info.specialValue();
        }
        double result = parseDblImpl(info.s, (int) info.e);
        if (Double.doubleToRawLongBits(result) == 0xffffffffffffffffL) {
            throw invalidReal(s, true);
        }
        return info.negative ? -result : result;
    }

    /**
     * Returns the closest float value to the real number in the string.
     *
     * @param s
     *            the String that will be parsed to a floating point
     * @return the float closest to the real number
     *
     * @throws NumberFormatException
     *                if the String doesn't represent a float
     */
    public static float parseFloat(String s) {
        s = s.trim();
        int length = s.length();

        if (length == 0) {
            throw invalidReal(s, false);
        }

        // See if this could be a named float
        char last = s.charAt(length - 1);
        if (last == 'y' || last == 'N') {
            return parseName(s, false);
        }

        // See if it could be a hexadecimal representation
        // We don't use startsWith because there might be a leading sign.
        if (s.indexOf("0x") != -1 || s.indexOf("0X") != -1) {
            return HexStringParser.parseFloat(s);
        }

        StringExponentPair info = initialParse(s, length, false);
        if (info.infinity || info.zero) {
            return info.specialValue();
        }
        float result = parseFltImpl(info.s, (int) info.e);
        if (Float.floatToRawIntBits(result) == 0xffffffff) {
            throw invalidReal(s, false);
        }
        return info.negative ? -result : result;
    }
}
