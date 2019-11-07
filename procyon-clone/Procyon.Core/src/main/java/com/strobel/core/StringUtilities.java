/*
 * StringUtilities.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.core;

import com.strobel.util.ContractUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * @author Mike Strobel
 */
public final class StringUtilities {
    public final static String EMPTY = "";

    private StringUtilities() {
        throw ContractUtils.unreachable();
    }

    private final static StringComparator[] _comparators = new StringComparator[] { StringComparator.Ordinal, StringComparator.OrdinalIgnoreCase };

    public static boolean isNullOrEmpty(final String s) {
        return s == null || s.length() == 0;
    }

    public static boolean equals(final String s1, final String s2) {
        return StringComparator.Ordinal.equals(s1, s2);
    }

    public static boolean equals(final String s1, final String s2, final StringComparison comparison) {
        return _comparators[VerifyArgument.notNull(comparison, "comparison").ordinal()].equals(s1, s2);
    }

    public static int compare(final String s1, final String s2) {
        return StringComparator.Ordinal.compare(s1, s2);
    }

    public static int compare(final String s1, final String s2, final StringComparison comparison) {
        return _comparators[VerifyArgument.notNull(comparison, "comparison").ordinal()].compare(s1, s2);
    }

    public static int getHashCode(final String s) {
        if (isNullOrEmpty(s)) {
            return 0;
        }
        return s.hashCode();
    }

    public static int getHashCodeIgnoreCase(final String s) {
        if (isNullOrEmpty(s)) {
            return 0;
        }

        int hash = 0;

        for (int i = 0, n = s.length(); i < n; i++) {
            hash = 31 * hash + Character.toLowerCase(s.charAt(i));
        }

        return hash;
    }

    public static boolean isNullOrWhitespace(final String s) {
        if (isNullOrEmpty(s)) {
            return true;
        }
        for (int i = 0, length = s.length(); i < length; i++) {
            final char ch = s.charAt(i);
            if (!Character.isWhitespace(ch)) {
                return false;
            }
        }
        return true;
    }

    public static boolean startsWith(final CharSequence value, final CharSequence prefix) {
        return substringEquals(
            VerifyArgument.notNull(value, "value"),
            0,
            VerifyArgument.notNull(prefix, "prefix"),
            0,
            prefix.length(),
            StringComparison.Ordinal
        );
    }

    public static boolean startsWithIgnoreCase(final CharSequence value, final String prefix) {
        return substringEquals(
            VerifyArgument.notNull(value, "value"),
            0,
            VerifyArgument.notNull(prefix, "prefix"),
            0,
            prefix.length(),
            StringComparison.OrdinalIgnoreCase
        );
    }

    public static boolean endsWith(final CharSequence value, final CharSequence suffix) {
        final int valueLength = VerifyArgument.notNull(value, "value").length();
        final int suffixLength = VerifyArgument.notNull(suffix, "suffix").length();
        final int testOffset = valueLength - suffixLength;

        return testOffset >= 0 &&
               substringEquals(
                   value,
                   testOffset,
                   suffix,
                   0,
                   suffixLength,
                   StringComparison.Ordinal
               );
    }

    public static boolean endsWithIgnoreCase(final CharSequence value, final String suffix) {
        final int valueLength = VerifyArgument.notNull(value, "value").length();
        final int suffixLength = VerifyArgument.notNull(suffix, "suffix").length();
        final int testOffset = valueLength - suffixLength;

        return testOffset >= 0 &&
               substringEquals(
                   value,
                   testOffset,
                   suffix,
                   0,
                   suffixLength,
                   StringComparison.OrdinalIgnoreCase
               );
    }

    public static String concat(final Iterable<String> values) {
        return join(null, values);
    }

    public static String concat(final String... values) {
        return join(null, values);
    }

    public static String join(final String separator, final Iterable<?> values) {
        VerifyArgument.notNull(values, "values");

        final StringBuilder sb = new StringBuilder();

        boolean appendSeparator = false;

        for (final Object value : values) {
            if (value == null) {
                continue;
            }

            if (appendSeparator) {
                sb.append(separator);
            }

            appendSeparator = true;

            sb.append(value);
        }

        return sb.toString();
    }

    public static String join(final String separator, final String... values) {
        if (ArrayUtilities.isNullOrEmpty(values)) {
            return EMPTY;
        }

        final StringBuilder sb = new StringBuilder();

        for (int i = 0, n = values.length; i < n; i++) {
            final String value = values[i];

            if (value == null) {
                continue;
            }

            if (i != 0 && separator != null) {
                sb.append(separator);
            }

            sb.append(value);
        }

        return sb.toString();
    }

    public static boolean substringEquals(
        final CharSequence value,
        final int offset,
        final CharSequence comparand,
        final int comparandOffset,
        final int substringLength) {

        return substringEquals(
            value,
            offset,
            comparand,
            comparandOffset,
            substringLength,
            StringComparison.Ordinal
        );
    }

    public static boolean substringEquals(
        final CharSequence value,
        final int offset,
        final CharSequence comparand,
        final int comparandOffset,
        final int substringLength,
        final StringComparison comparison) {

        VerifyArgument.notNull(value, "value");
        VerifyArgument.notNull(comparand, "comparand");

        VerifyArgument.isNonNegative(offset, "offset");
        VerifyArgument.isNonNegative(comparandOffset, "comparandOffset");

        VerifyArgument.isNonNegative(substringLength, "substringLength");

        final int valueLength = value.length();

        if (offset + substringLength > valueLength) {
            return false;
        }

        final int comparandLength = comparand.length();

        if (comparandOffset + substringLength > comparandLength) {
            return false;
        }

        final boolean ignoreCase = comparison == StringComparison.OrdinalIgnoreCase;

        for (int i = 0; i < substringLength; i++) {
            final char vc = value.charAt(offset + i);
            final char cc = comparand.charAt(comparandOffset + i);

            if (vc == cc || ignoreCase && Character.toLowerCase(vc) == Character.toLowerCase(cc)) {
                continue;
            }

            return false;
        }

        return true;
    }

    public static boolean isTrue(final String value) {
        if (isNullOrWhitespace(value)) {
            return false;
        }

        final String trimmedValue = value.trim();

        if (trimmedValue.length() == 1) {
            final char ch = Character.toLowerCase(trimmedValue.charAt(0));
            return ch == 't' || ch == 'y' || ch == '1';
        }

        return StringComparator.OrdinalIgnoreCase.equals(trimmedValue, "true") ||
               StringComparator.OrdinalIgnoreCase.equals(trimmedValue, "yes");
    }

    public static boolean isFalse(final String value) {
        if (isNullOrWhitespace(value)) {
            return false;
        }

        final String trimmedValue = value.trim();

        if (trimmedValue.length() == 1) {
            final char ch = Character.toLowerCase(trimmedValue.charAt(0));
            return ch == 'f' || ch == 'n' || ch == '0';
        }

        return StringComparator.OrdinalIgnoreCase.equals(trimmedValue, "false") ||
               StringComparator.OrdinalIgnoreCase.equals(trimmedValue, "no");
    }

    public static String removeLeft(final String value, final String prefix) {
        return removeLeft(value, prefix, false);
    }

    public static String removeLeft(final String value, final String prefix, final boolean ignoreCase) {
        VerifyArgument.notNull(value, "value");

        if (isNullOrEmpty(prefix)) {
            return value;
        }

        final int prefixLength = prefix.length();
        final int remaining = value.length() - prefixLength;

        if (remaining < 0) {
            return value;
        }

        if (remaining == 0) {
            if (ignoreCase) {
                return value.equalsIgnoreCase(prefix) ? EMPTY : value;
            }
            return value.equals(prefix) ? EMPTY : value;
        }

        if (ignoreCase) {
            return startsWithIgnoreCase(value, prefix)
                   ? value.substring(prefixLength)
                   : value;
        }

        return value.startsWith(prefix)
               ? value.substring(prefixLength)
               : value;
    }

    public static String removeLeft(final String value, final char[] removeChars) {
        VerifyArgument.notNull(value, "value");
        VerifyArgument.notNull(removeChars, "removeChars");

        final int totalLength = value.length();
        int start = 0;

        while (start < totalLength && ArrayUtilities.contains(removeChars, value.charAt(start))) {
            ++start;
        }

        return start > 0 ? value.substring(start) : value;
    }

    public static String removeRight(final String value, final String suffix) {
        return removeRight(value, suffix, false);
    }

    public static String removeRight(final String value, final String suffix, final boolean ignoreCase) {
        VerifyArgument.notNull(value, "value");

        if (isNullOrEmpty(suffix)) {
            return value;
        }

        final int valueLength = value.length();
        final int suffixLength = suffix.length();
        final int end = valueLength - suffixLength;

        if (end < 0) {
            return value;
        }

        if (end == 0) {
            if (ignoreCase) {
                return value.equalsIgnoreCase(suffix) ? EMPTY : value;
            }
            return value.equals(suffix) ? EMPTY : value;
        }

        if (ignoreCase) {
            return endsWithIgnoreCase(value, suffix)
                   ? value.substring(0, end)
                   : value;
        }

        return value.endsWith(suffix)
               ? value.substring(0, end)
               : value;
    }

    public static String removeRight(final String value, final char[] removeChars) {
        VerifyArgument.notNull(value, "value");
        VerifyArgument.notNull(removeChars, "removeChars");

        final int totalLength = value.length();
        int length = totalLength;

        while (length > 0 && ArrayUtilities.contains(removeChars, value.charAt(length - 1))) {
            --length;
        }

        return length == totalLength ? value : value.substring(0, length);
    }

    public static String padLeft(final String value, final int length) {
        VerifyArgument.notNull(value, "value");
        VerifyArgument.isNonNegative(length, "length");

        if (length == 0) {
            return value;
        }

        return String.format("%1$" + length + "s", value);
    }

    public static String padRight(final String value, final int length) {
        VerifyArgument.notNull(value, "value");
        VerifyArgument.isNonNegative(length, "length");

        if (length == 0) {
            return value;
        }

        return String.format("%1$-" + length + "s", value);
    }

    public static String trimLeft(final String value) {
        VerifyArgument.notNull(value, "value");

        final int totalLength = value.length();
        int start = 0;

        while (start < totalLength && value.charAt(start) <= ' ') {
            ++start;
        }

        return start > 0 ? value.substring(start) : value;
    }

    public static String trimRight(final String value) {
        VerifyArgument.notNull(value, "value");

        final int totalLength = value.length();
        int length = totalLength;

        while (length > 0 && value.charAt(length - 1) <= ' ') {
            --length;
        }

        return length == totalLength ? value : value.substring(0, length);
    }

    public static String trimAndRemoveLeft(final String value, final String prefix) {
        return trimAndRemoveLeft(value, prefix, false);
    }

    public static String trimAndRemoveLeft(final String value, final String prefix, final boolean ignoreCase) {
        VerifyArgument.notNull(value, "value");

        final String trimmedValue = value.trim();
        final String result = removeLeft(trimmedValue, prefix, ignoreCase);

        //noinspection StringEquality
        if (result == trimmedValue) {
            return trimmedValue;
        }

        return trimLeft(result);
    }

    public static String trimAndRemoveLeft(final String value, final char[] removeChars) {
        VerifyArgument.notNull(value, "value");

        final String trimmedValue = value.trim();
        final String result = removeLeft(trimmedValue, removeChars);

        //noinspection StringEquality
        if (result == trimmedValue) {
            return trimmedValue;
        }

        return trimLeft(result);
    }

    public static String trimAndRemoveRight(final String value, final String suffix) {
        return trimAndRemoveRight(value, suffix, false);
    }

    public static String trimAndRemoveRight(final String value, final String suffix, final boolean ignoreCase) {
        VerifyArgument.notNull(value, "value");

        final String trimmedValue = value.trim();
        final String result = removeRight(trimmedValue, suffix, ignoreCase);

        //noinspection StringEquality
        if (result == trimmedValue) {
            return trimmedValue;
        }

        return trimRight(result);
    }

    public static String trimAndRemoveRight(final String value, final char[] removeChars) {
        VerifyArgument.notNull(value, "value");

        final String trimmedValue = value.trim();
        final String result = removeRight(trimmedValue, removeChars);

        //noinspection StringEquality
        if (result == trimmedValue) {
            return trimmedValue;
        }

        return trimRight(result);
    }

    public static int getUtf8ByteCount(final String value) {
        VerifyArgument.notNull(value, "value");

        if (value.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (int i = 0, n = value.length(); i < n; ++i, ++count) {
            final char c = value.charAt(i);
            if (c > 0x07FF) {
                count += 2;
            }
            else if (c > 0x007F) {
                ++count;
            }
        }

        return count;
    }

    public static String escape(final char ch) {
        return escapeCharacter(ch, false);
    }

    private static String escapeCharacter(final char ch, final boolean isUnicodeSupported) {
        if (ch == '\'') {
            return "\\'";
        }

        if (shouldEscape(ch, false, isUnicodeSupported)) {
            switch (ch) {
                case '\0':
                    return "\\0";
                case '\b':
                    return "\\b";
                case '\f':
                    return "\\f";
                default:
                    return format("\\u%1$04x", (int) ch);
            }
        }

        return String.valueOf(ch);
    }

    public static String escape(final char ch, final boolean quote) {
        return escape(ch, quote, false);
    }

    public static String escape(final char ch, final boolean quote, final boolean isUnicodeSupported) {
        if (quote) {
            if (ch == '\'') {
                return "'\\''";
            }

            if (shouldEscape(ch, true, isUnicodeSupported)) {
                switch (ch) {
                    case '\0':
                        return "'\\0'";
                    case '\t':
                        return "'\\t'";
                    case '\b':
                        return "'\\b'";
                    case '\n':
                        return "'\\n'";
                    case '\r':
                        return "'\\r'";
                    case '\f':
                        return "'\\f'";
                    case '\"':
                        return "'\\\"'";
                    case '\\':
                        return "'\\\\'";
                    default:
                        return format("'\\u%1$04x'", (int) ch);
                }
            }

            return "'" + ch + "'";
        }

        return escape(ch);
    }

    public static String escape(final String value) {
        return escape(value, false);
    }

    public static String escape(final String value, final boolean quote) {
        return escape(value, quote, false);
    }

    @SuppressWarnings("ConstantConditions")
    public static String escape(final String value, final boolean quote, final boolean isUnicodeSupported) {
        if (value == null) {
            return null;
        }

        StringBuilder sb;

        if (quote) {
            sb = new StringBuilder(value.length());
            sb.append('"');
        }
        else {
            sb = null;
        }

        for (int i = 0, n = value.length(); i < n; i++) {
            final char ch = value.charAt(i);
            final boolean shouldEscape = shouldEscape(ch, quote, isUnicodeSupported);

            if (shouldEscape) {
                if (sb == null) {
                    sb = new StringBuilder();

                    if (i != 0) {
                        sb.append(value, 0, i);
                    }
                }

                switch (ch) {
                    case '\0':
                        sb.append("\\u0000");
                        continue;
                    case '\t':
                        sb.append('\\');
                        sb.append('t');
                        continue;
                    case '\b':
                        sb.append('\\');
                        sb.append('b');
                        continue;
                    case '\n':
                        sb.append('\\');
                        sb.append('n');
                        continue;
                    case '\r':
                        sb.append('\\');
                        sb.append('r');
                        continue;
                    case '\f':
                        sb.append('\\');
                        sb.append('f');
                        continue;
                    case '\"':
                        sb.append('\\');
                        sb.append('"');
                        continue;
                    case '\\':
                        sb.append('\\');
                        sb.append('\\');
                        continue;
                    default:
                        sb.append(format("\\u%1$04x", (int) ch));
                        continue;
                }
            }
            else if (sb != null) {
                sb.append(ch);
            }
        }

        if (quote) {
            sb.append('"');
        }

        if (sb == null) {
            return value;
        }

        return sb.toString();
    }

    public static String escapeIdentifier(final String value, final boolean isUnicodeSupported) {
        if (isNullOrEmpty(value)) {
            return value;
        }

        StringBuilder sb = null;

        final char start = value.charAt(0);

        if (!Character.isJavaIdentifierStart(start)) {
            sb = new StringBuilder(value.length() * 2);
            sb.append(start);
        }

        for (int i = 1, n = value.length(); i < n; i++) {
            final char ch = value.charAt(i);

            final boolean valid = Character.isJavaIdentifierPart(ch) &&
                                  (isUnicodeSupported || ch < 192);

            if (valid && sb == null) {
                continue;
            }

            if (sb == null) {
                sb = new StringBuilder(value.length() * 2);
            }

            if (valid) {
                sb.append(ch);
            }
            else {
                sb.append(format("\\u%1$04x", (int) ch));
            }
        }

        if (sb == null) {
            return value;
        }

        return sb.toString();
    }

    private static boolean shouldEscape(final char ch, final boolean quote, final boolean isUnicodeSupported) {
        switch (ch) {
            case '\0':
            case '\b':
            case '\f':
                return true;

            case '\"':
            case '\\':
            case '\n':
            case '\r':
            case '\t':
                return quote;

            default: {
                switch (Character.getType(ch)) {
                    case Character.CONTROL:
                    case Character.FORMAT:
                    case Character.UNASSIGNED:
                        return true;

                    default:
                        return !isUnicodeSupported && ch >= 192 ||
                               quote && Character.isWhitespace(ch) && ch != ' ';
                }
            }
        }
    }

    public static String repeat(final char ch, final int length) {
        VerifyArgument.isNonNegative(length, "length");
        final char[] c = new char[length];
        Arrays.fill(c, 0, length, ch);
        return new String(c);
    }

    public static List<String> split(
        final String value,
        final char firstDelimiter,
        final char... additionalDelimiters) {

        return split(value, true, firstDelimiter, additionalDelimiters);
    }

    public static List<String> split(
        final String value,
        final boolean removeEmptyEntries,
        final char firstDelimiter,
        final char... additionalDelimiters) {

        VerifyArgument.notNull(value, "value");

        final int end = value.length();
        final ArrayList<String> parts = new ArrayList<>();

        if (end == 0) {
            return parts;
        }

        int start = 0;
        int i = start;

        while (i < end) {
            final char ch = value.charAt(i);

            if (ch == firstDelimiter || contains(additionalDelimiters, ch)) {
                if (i != start || !removeEmptyEntries) {
                    parts.add(value.substring(start, i));
                }

                start = i + 1;

                if (!removeEmptyEntries && start == end) {
                    parts.add(EMPTY);
                }
            }

            ++i;
        }

        if (start < end) {
            parts.add(value.substring(start, end));
        }

        return parts;
    }

    public static List<String> split(final String value, final char[] delimiters) {
        return split(value, true, delimiters);
    }

    public static List<String> split(
        final String value,
        final boolean removeEmptyEntries,
        final char[] delimiters) {

        VerifyArgument.notNull(value, "value");
        VerifyArgument.notNull(delimiters, "delimiters");

        final int end = value.length();
        final ArrayList<String> parts = new ArrayList<>();

        if (end == 0) {
            return parts;
        }

        int start = 0;
        int i = start;

        while (i < end) {
            final char ch = value.charAt(i);

            if (contains(delimiters, ch)) {
                if (i != start || !removeEmptyEntries) {
                    parts.add(value.substring(start, i));
                }

                start = i + 1;
            }

            ++i;
        }

        if (start < end) {
            parts.add(value.substring(start, end));
        }

        return parts;
    }

    private static boolean contains(final char[] array, final char value) {
        for (final char c : array) {
            if (c == value) {
                return true;
            }
        }
        return false;
    }
}
