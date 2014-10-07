/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import libcore.util.EmptyArray;

/**
 * Returns a fixed string based on a numeric value. The class can be used in
 * conjunction with the {@link MessageFormat} class to handle plurals in
 * messages. {@code ChoiceFormat} enables users to attach a format to a range of
 * numbers. The choice is specified with an ascending list of doubles, where
 * each item specifies a half-open interval up to the next item as in the
 * following: X matches j if and only if {@code limit[j] <= X < limit[j+1]}.
 * <p>
 * If there is no match, then either the first or last index is used. The first
 * or last index is used depending on whether the number is too low or too high.
 * The length of the format array must be the same as the length of the limits
 * array.
 * <h5>Examples:</h5>
 * <blockquote>
 *
 * <pre>
 * double[] limits = {1, 2, 3, 4, 5, 6, 7};
 * String[] fmts = {"Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat"};
 *
 * double[] limits2 = {0, 1, ChoiceFormat.nextDouble(1)};
 * String[] fmts2 = {"no files", "one file", "many files"};
 * </pre>
 * </blockquote>
 * <p>
 * ChoiceFormat.nextDouble(double) allows to get the double following the one
 * passed to the method. This is used to create half open intervals.
 * <p>
 * {@code ChoiceFormat} objects also may be converted to and from patterns.
 * The conversion can be done programmatically, as in the example above, or
 * by using a pattern like the following:
 * <blockquote>
 *
 * <pre>
 * "1#Sun|2#Mon|3#Tue|4#Wed|5#Thur|6#Fri|7#Sat"
 * "0#are no files|1#is one file|1&lt;are many files"
 * </pre>
 *
 * </blockquote>
 * <p>
 * where:
 * <ul>
 * <li><number>"#"</number> specifies an inclusive limit value;</li>
 * <li><number>"<"</number> specifies an exclusive limit value.</li>
 * </ul>
 */
public class ChoiceFormat extends NumberFormat {

    private static final long serialVersionUID = 1795184449645032964L;

    private double[] choiceLimits;

    private String[] choiceFormats;

    /**
     * Constructs a new {@code ChoiceFormat} with the specified double values
     * and associated strings. When calling
     * {@link #format(double, StringBuffer, FieldPosition) format} with a double
     * value {@code d}, then the element {@code i} in {@code formats} is
     * selected where {@code i} fulfills {@code limits[i] <= d < limits[i+1]}.
     * <p>
     * The length of the {@code limits} and {@code formats} arrays must be the
     * same.
     *
     * @param limits
     *            an array of doubles in ascending order. The lowest and highest
     *            possible values are negative and positive infinity.
     * @param formats
     *            the strings associated with the ranges defined through {@code
     *            limits}. The lower bound of the associated range is at the
     *            same index as the string.
     */
    public ChoiceFormat(double[] limits, String[] formats) {
        setChoices(limits, formats);
    }

    /**
     * Constructs a new {@code ChoiceFormat} with the strings and limits parsed
     * from the specified pattern.
     *
     * @param template
     *            the pattern of strings and ranges.
     * @throws IllegalArgumentException
     *            if an error occurs while parsing the pattern.
     */
    public ChoiceFormat(String template) {
        applyPattern(template);
    }

    /**
     * Parses the pattern to determine new strings and ranges for this
     * {@code ChoiceFormat}.
     *
     * @param template
     *            the pattern of strings and ranges.
     * @throws IllegalArgumentException
     *            if an error occurs while parsing the pattern.
     */
    public void applyPattern(String template) {
        double[] limits = new double[5];
        List<String> formats = new ArrayList<String>();
        int length = template.length(), limitCount = 0, index = 0;
        StringBuffer buffer = new StringBuffer();
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        ParsePosition position = new ParsePosition(0);
        while (true) {
            index = skipWhitespace(template, index);
            if (index >= length) {
                if (limitCount == limits.length) {
                    choiceLimits = limits;
                } else {
                    choiceLimits = new double[limitCount];
                    System.arraycopy(limits, 0, choiceLimits, 0, limitCount);
                }
                choiceFormats = new String[formats.size()];
                for (int i = 0; i < formats.size(); i++) {
                    choiceFormats[i] = formats.get(i);
                }
                return;
            }

            position.setIndex(index);
            Number value = format.parse(template, position);
            index = skipWhitespace(template, position.getIndex());
            if (position.getErrorIndex() != -1 || index >= length) {
                // Fix Harmony 540
                choiceLimits = EmptyArray.DOUBLE;
                choiceFormats = EmptyArray.STRING;
                return;
            }
            char ch = template.charAt(index++);
            if (limitCount == limits.length) {
                double[] newLimits = new double[limitCount * 2];
                System.arraycopy(limits, 0, newLimits, 0, limitCount);
                limits = newLimits;
            }
            double next;
            switch (ch) {
                case '#':
                case '\u2264':
                    next = value.doubleValue();
                    break;
                case '<':
                    next = nextDouble(value.doubleValue());
                    break;
                default:
                    throw new IllegalArgumentException("Bad character '" + ch + "' in template: " + template);
            }
            if (limitCount > 0 && next <= limits[limitCount - 1]) {
                throw new IllegalArgumentException("Bad template: " + template);
            }
            buffer.setLength(0);
            position.setIndex(index);
            upTo(template, position, buffer, '|');
            index = position.getIndex();
            limits[limitCount++] = next;
            formats.add(buffer.toString());
        }
    }

    /**
     * Returns a new instance of {@code ChoiceFormat} with the same ranges and
     * strings as this {@code ChoiceFormat}.
     *
     * @return a shallow copy of this {@code ChoiceFormat}.
     *
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        ChoiceFormat clone = (ChoiceFormat) super.clone();
        clone.choiceLimits = choiceLimits.clone();
        clone.choiceFormats = choiceFormats.clone();
        return clone;
    }

    /**
     * Compares the specified object with this {@code ChoiceFormat}. The object
     * must be an instance of {@code ChoiceFormat} and have the same limits and
     * formats to be equal to this instance.
     *
     * @param object
     *            the object to compare with this instance.
     * @return {@code true} if the specified object is equal to this instance;
     *         {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ChoiceFormat)) {
            return false;
        }
        ChoiceFormat choice = (ChoiceFormat) object;
        return Arrays.equals(choiceLimits, choice.choiceLimits)
                && Arrays.equals(choiceFormats, choice.choiceFormats);
    }

    /**
     * Appends the string associated with the range in which the specified
     * double value fits to the specified string buffer.
     *
     * @param value
     *            the double to format.
     * @param buffer
     *            the target string buffer to append the formatted value to.
     * @param field
     *            a {@code FieldPosition} which is ignored.
     * @return the string buffer.
     */
    @Override
    public StringBuffer format(double value, StringBuffer buffer,
            FieldPosition field) {
        for (int i = choiceLimits.length - 1; i >= 0; i--) {
            if (choiceLimits[i] <= value) {
                return buffer.append(choiceFormats[i]);
            }
        }
        return choiceFormats.length == 0 ? buffer : buffer
                .append(choiceFormats[0]);
    }

    /**
     * Appends the string associated with the range in which the specified long
     * value fits to the specified string buffer.
     *
     * @param value
     *            the long to format.
     * @param buffer
     *            the target string buffer to append the formatted value to.
     * @param field
     *            a {@code FieldPosition} which is ignored.
     * @return the string buffer.
     */
    @Override
    public StringBuffer format(long value, StringBuffer buffer,
            FieldPosition field) {
        return format((double) value, buffer, field);
    }

    /**
     * Returns the strings associated with the ranges of this {@code
     * ChoiceFormat}.
     *
     * @return an array of format strings.
     */
    public Object[] getFormats() {
        return choiceFormats;
    }

    /**
     * Returns the limits of this {@code ChoiceFormat}.
     *
     * @return the array of doubles which make up the limits of this {@code
     *         ChoiceFormat}.
     */
    public double[] getLimits() {
        return choiceLimits;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * return the same value for this method.
     *
     * @return the receiver's hash.
     *
     * @see #equals
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < choiceLimits.length; i++) {
            long v = Double.doubleToLongBits(choiceLimits[i]);
            hashCode += (int) (v ^ (v >>> 32)) + choiceFormats[i].hashCode();
        }
        return hashCode;
    }

    /**
     * Returns the double value which is closest to the specified double but
     * larger.
     *
     * @param value
     *            a double value.
     * @return the next larger double value.
     */
    public static final double nextDouble(double value) {
        if (value == Double.POSITIVE_INFINITY) {
            return value;
        }
        long bits;
        // Handle -0.0
        if (value == 0) {
            bits = 0;
        } else {
            bits = Double.doubleToLongBits(value);
        }
        return Double.longBitsToDouble(value < 0 ? bits - 1 : bits + 1);
    }

    /**
     * Returns the double value which is closest to the specified double but
     * either larger or smaller as specified.
     *
     * @param value
     *            a double value.
     * @param increment
     *            {@code true} to get the next larger value, {@code false} to
     *            get the previous smaller value.
     * @return the next larger or smaller double value.
     */
    public static double nextDouble(double value, boolean increment) {
        return increment ? nextDouble(value) : previousDouble(value);
    }

    /**
     * Parses a double from the specified string starting at the index specified
     * by {@code position}. The string is compared to the strings of this
     * {@code ChoiceFormat} and if a match occurs then the lower bound of the
     * corresponding range in the limits array is returned. If the string is
     * successfully parsed then the index of the {@code ParsePosition} passed to
     * this method is updated to the index following the parsed text.
     * <p>
     * If one of the format strings of this {@code ChoiceFormat} instance is
     * found in {@code string} starting at {@code position.getIndex()} then
     * <ul>
     * <li>the index in {@code position} is set to the index following the
     * parsed text;
     * <li>the {@link java.lang.Double Double} corresponding to the format
     * string is returned.</li>
     * </ul>
     * <p>
     * If none of the format strings is found in {@code string} then
     * <ul>
     * <li>the error index in {@code position} is set to the current index in
     * {@code position};</li>
     * <li> {@link java.lang.Double#NaN Double.NaN} is returned.
     * </ul>
     * @param string
     *            the source string to parse.
     * @param position
     *            input/output parameter, specifies the start index in {@code
     *            string} from where to start parsing. See the <em>Returns</em>
     *            section for a description of the output values.
     * @return a Double resulting from the parse, or Double.NaN if there is an
     *         error
     */
    @Override
    public Number parse(String string, ParsePosition position) {
        int offset = position.getIndex();
        for (int i = 0; i < choiceFormats.length; i++) {
            if (string.startsWith(choiceFormats[i], offset)) {
                position.setIndex(offset + choiceFormats[i].length());
                return new Double(choiceLimits[i]);
            }
        }
        position.setErrorIndex(offset);
        return new Double(Double.NaN);
    }

    /**
     * Returns the double value which is closest to the specified double but
     * smaller.
     *
     * @param value
     *            a double value.
     * @return the next smaller double value.
     */
    public static final double previousDouble(double value) {
        if (value == Double.NEGATIVE_INFINITY) {
            return value;
        }
        long bits;
        // Handle 0.0
        if (value == 0) {
            bits = 0x8000000000000000L;
        } else {
            bits = Double.doubleToLongBits(value);
        }
        return Double.longBitsToDouble(value <= 0 ? bits + 1 : bits - 1);
    }

    /**
     * Sets the double values and associated strings of this ChoiceFormat. When
     * calling {@link #format(double, StringBuffer, FieldPosition) format} with
     * a double value {@code d}, then the element {@code i} in {@code formats}
     * is selected where {@code i} fulfills
     * {@code limits[i] <= d < limits[i+1]}.
     * <p>
     * The length of the {@code limits} and {@code formats} arrays must be the
     * same.
     *
     * @param limits
     *            an array of doubles in ascending order. The lowest and highest
     *            possible values are negative and positive infinity.
     * @param formats
     *            the strings associated with the ranges defined through {@code
     *            limits}. The lower bound of the associated range is at the
     *            same index as the string.
     */
    public void setChoices(double[] limits, String[] formats) {
        if (limits.length != formats.length) {
            throw new IllegalArgumentException("limits.length != formats.length: " +
                                               limits.length + " != " + formats.length);
        }
        choiceLimits = limits;
        choiceFormats = formats;
    }

    private int skipWhitespace(String string, int index) {
        int length = string.length();
        while (index < length && Character.isWhitespace(string.charAt(index))) {
            index++;
        }
        return index;
    }

    /**
     * Returns the pattern of this {@code ChoiceFormat} which specifies the
     * ranges and their associated strings.
     *
     * @return the pattern.
     */
    public String toPattern() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < choiceLimits.length; i++) {
            if (i != 0) {
                buffer.append('|');
            }
            String previous = String.valueOf(previousDouble(choiceLimits[i]));
            String limit = String.valueOf(choiceLimits[i]);
            if (previous.length() < limit.length()) {
                buffer.append(previous);
                buffer.append('<');
            } else {
                buffer.append(limit);
                buffer.append('#');
            }
            boolean quote = (choiceFormats[i].indexOf('|') != -1);
            if (quote) {
                buffer.append('\'');
            }
            buffer.append(choiceFormats[i]);
            if (quote) {
                buffer.append('\'');
            }
        }
        return buffer.toString();
    }
}
