/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

/**
 * Formats simple patterns like "{1} was born in {0}".
 * Internal version of {@link android.icu.text.SimpleFormatter}
 * with only static methods, to avoid wrapper objects.
 *
 * <p>This class "compiles" pattern strings into a binary format
 * and implements formatting etc. based on that.
 *
 * <p>Format:
 * Index 0: One more than the highest argument number.
 * Followed by zero or more arguments or literal-text segments.
 *
 * <p>An argument is stored as its number, less than ARG_NUM_LIMIT.
 * A literal-text segment is stored as its length (at least 1) offset by ARG_NUM_LIMIT,
 * followed by that many chars.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class SimpleFormatterImpl {
    /**
     * Argument numbers must be smaller than this limit.
     * Text segment lengths are offset by this much.
     * This is currently the only unused char value in compiled patterns,
     * except it is the maximum value of the first unit (max arg +1).
     */
    private static final int ARG_NUM_LIMIT = 0x100;
    private static final char LEN1_CHAR = (char)(ARG_NUM_LIMIT + 1);
    private static final char LEN2_CHAR = (char)(ARG_NUM_LIMIT + 2);
    private static final char LEN3_CHAR = (char)(ARG_NUM_LIMIT + 3);
    /**
     * Initial and maximum char/UChar value set for a text segment.
     * Segment length char values are from ARG_NUM_LIMIT+1 to this value here.
     * Normally 0xffff, but can be as small as ARG_NUM_LIMIT+1 for testing.
     */
    private static final char SEGMENT_LENGTH_ARGUMENT_CHAR = (char)0xffff;
    /**
     * Maximum length of a text segment. Longer segments are split into shorter ones.
     */
    private static final int MAX_SEGMENT_LENGTH = SEGMENT_LENGTH_ARGUMENT_CHAR - ARG_NUM_LIMIT;

    /** "Intern" some common patterns. */
    private static final String[][] COMMON_PATTERNS = {
        { "{0} {1}", "\u0002\u0000" + LEN1_CHAR + " \u0001" },
        { "{0} ({1})", "\u0002\u0000" + LEN2_CHAR + " (\u0001" + LEN1_CHAR + ')' },
        { "{0}, {1}", "\u0002\u0000" + LEN2_CHAR + ", \u0001" },
        { "{0} – {1}", "\u0002\u0000" + LEN3_CHAR + " – \u0001" },  // en dash
    };

    /** Use only static methods. */
    private SimpleFormatterImpl() {}

    /**
     * Creates a compiled form of the pattern string, for use with appropriate static methods.
     * The number of arguments checked against the given limits is the
     * highest argument number plus one, not the number of occurrences of arguments.
     *
     * @param pattern The pattern string.
     * @param min The pattern must have at least this many arguments.
     * @param max The pattern must have at most this many arguments.
     * @return The compiled-pattern string.
     * @throws IllegalArgumentException for bad argument syntax and too few or too many arguments.
     */
    public static String compileToStringMinMaxArguments(
            CharSequence pattern, StringBuilder sb, int min, int max) {
        // Return some precompiled common two-argument patterns.
        if (min <= 2 && 2 <= max) {
            for (String[] pair : COMMON_PATTERNS) {
                if (pair[0].contentEquals(pattern)) {
                    assert pair[1].charAt(0) == 2;
                    return pair[1];
                }
            }
        }
        // Parse consistent with MessagePattern, but
        // - support only simple numbered arguments
        // - build a simple binary structure into the result string
        int patternLength = pattern.length();
        sb.ensureCapacity(patternLength);
        // Reserve the first char for the number of arguments.
        sb.setLength(1);
        int textLength = 0;
        int maxArg = -1;
        boolean inQuote = false;
        for (int i = 0; i < patternLength;) {
            char c = pattern.charAt(i++);
            if (c == '\'') {
                if (i < patternLength && (c = pattern.charAt(i)) == '\'') {
                    // double apostrophe, skip the second one
                    ++i;
                } else if (inQuote) {
                    // skip the quote-ending apostrophe
                    inQuote = false;
                    continue;
                } else if (c == '{' || c == '}') {
                    // Skip the quote-starting apostrophe, find the end of the quoted literal text.
                    ++i;
                    inQuote = true;
                } else {
                    // The apostrophe is part of literal text.
                    c = '\'';
                }
            } else if (!inQuote && c == '{') {
                if (textLength > 0) {
                    sb.setCharAt(sb.length() - textLength - 1, (char)(ARG_NUM_LIMIT + textLength));
                    textLength = 0;
                }
                int argNumber;
                if ((i + 1) < patternLength &&
                        0 <= (argNumber = pattern.charAt(i) - '0') && argNumber <= 9 &&
                        pattern.charAt(i + 1) == '}') {
                    i += 2;
                } else {
                    // Multi-digit argument number (no leading zero) or syntax error.
                    // MessagePattern permits PatternProps.skipWhiteSpace(pattern, index)
                    // around the number, but this class does not.
                    int argStart = i - 1;
                    argNumber = -1;
                    if (i < patternLength && '1' <= (c = pattern.charAt(i++)) && c <= '9') {
                        argNumber = c - '0';
                        while (i < patternLength && '0' <= (c = pattern.charAt(i++)) && c <= '9') {
                            argNumber = argNumber * 10 + (c - '0');
                            if (argNumber >= ARG_NUM_LIMIT) {
                                break;
                            }
                        }
                    }
                    if (argNumber < 0 || c != '}') {
                        throw new IllegalArgumentException(
                                "Argument syntax error in pattern \"" + pattern +
                                "\" at index " + argStart +
                                ": " + pattern.subSequence(argStart, i));
                    }
                }
                if (argNumber > maxArg) {
                    maxArg = argNumber;
                }
                sb.append((char)argNumber);
                continue;
            }  // else: c is part of literal text
            // Append c and track the literal-text segment length.
            if (textLength == 0) {
                // Reserve a char for the length of a new text segment, preset the maximum length.
                sb.append(SEGMENT_LENGTH_ARGUMENT_CHAR);
            }
            sb.append(c);
            if (++textLength == MAX_SEGMENT_LENGTH) {
                textLength = 0;
            }
        }
        if (textLength > 0) {
            sb.setCharAt(sb.length() - textLength - 1, (char)(ARG_NUM_LIMIT + textLength));
        }
        int argCount = maxArg + 1;
        if (argCount < min) {
            throw new IllegalArgumentException(
                    "Fewer than minimum " + min + " arguments in pattern \"" + pattern + "\"");
        }
        if (argCount > max) {
            throw new IllegalArgumentException(
                    "More than maximum " + max + " arguments in pattern \"" + pattern + "\"");
        }
        sb.setCharAt(0, (char)argCount);
        return sb.toString();
    }

    /**
     * @param compiledPattern Compiled form of a pattern string.
     * @return The max argument number + 1.
     */
    public static int getArgumentLimit(String compiledPattern) {
        return compiledPattern.charAt(0);
    }

    /**
     * Formats the given values.
     *
     * @param compiledPattern Compiled form of a pattern string.
     */
    public static String formatCompiledPattern(String compiledPattern, CharSequence... values) {
        return formatAndAppend(compiledPattern, new StringBuilder(), null, values).toString();
    }

    /**
     * Formats the not-compiled pattern with the given values.
     * Equivalent to compileToStringMinMaxArguments() followed by formatCompiledPattern().
     * The number of arguments checked against the given limits is the
     * highest argument number plus one, not the number of occurrences of arguments.
     *
     * @param pattern Not-compiled form of a pattern string.
     * @param min The pattern must have at least this many arguments.
     * @param max The pattern must have at most this many arguments.
     * @return The compiled-pattern string.
     * @throws IllegalArgumentException for bad argument syntax and too few or too many arguments.
     */
    public static String formatRawPattern(String pattern, int min, int max, CharSequence... values) {
        StringBuilder sb = new StringBuilder();
        String compiledPattern = compileToStringMinMaxArguments(pattern, sb, min, max);
        sb.setLength(0);
        return formatAndAppend(compiledPattern, sb, null, values).toString();
    }

    /**
     * Formats the given values, appending to the appendTo builder.
     *
     * @param compiledPattern Compiled form of a pattern string.
     * @param appendTo Gets the formatted pattern and values appended.
     * @param offsets offsets[i] receives the offset of where
     *                values[i] replaced pattern argument {i}.
     *                Can be null, or can be shorter or longer than values.
     *                If there is no {i} in the pattern, then offsets[i] is set to -1.
     * @param values The argument values.
     *               An argument value must not be the same object as appendTo.
     *               values.length must be at least getArgumentLimit().
     *               Can be null if getArgumentLimit()==0.
     * @return appendTo
     */
    public static StringBuilder formatAndAppend(
            String compiledPattern, StringBuilder appendTo, int[] offsets, CharSequence... values) {
        int valuesLength = values != null ? values.length : 0;
        if (valuesLength < getArgumentLimit(compiledPattern)) {
            throw new IllegalArgumentException("Too few values.");
        }
        return format(compiledPattern, values, appendTo, null, true, offsets);
    }

    /**
     * Formats the given values, replacing the contents of the result builder.
     * May optimize by actually appending to the result if it is the same object
     * as the value corresponding to the initial argument in the pattern.
     *
     * @param compiledPattern Compiled form of a pattern string.
     * @param result Gets its contents replaced by the formatted pattern and values.
     * @param offsets offsets[i] receives the offset of where
     *                values[i] replaced pattern argument {i}.
     *                Can be null, or can be shorter or longer than values.
     *                If there is no {i} in the pattern, then offsets[i] is set to -1.
     * @param values The argument values.
     *               An argument value may be the same object as result.
     *               values.length must be at least getArgumentLimit().
     * @return result
     */
    public static StringBuilder formatAndReplace(
            String compiledPattern, StringBuilder result, int[] offsets, CharSequence... values) {
        int valuesLength = values != null ? values.length : 0;
        if (valuesLength < getArgumentLimit(compiledPattern)) {
            throw new IllegalArgumentException("Too few values.");
        }

        // If the pattern starts with an argument whose value is the same object
        // as the result, then we keep the result contents and append to it.
        // Otherwise we replace its contents.
        int firstArg = -1;
        // If any non-initial argument value is the same object as the result,
        // then we first copy its contents and use that instead while formatting.
        String resultCopy = null;
        if (getArgumentLimit(compiledPattern) > 0) {
            for (int i = 1; i < compiledPattern.length();) {
                int n = compiledPattern.charAt(i++);
                if (n < ARG_NUM_LIMIT) {
                    if (values[n] == result) {
                        if (i == 2) {
                            firstArg = n;
                        } else if (resultCopy == null) {
                            resultCopy = result.toString();
                        }
                    }
                } else {
                    i += n - ARG_NUM_LIMIT;
                }
            }
        }
        if (firstArg < 0) {
            result.setLength(0);
        }
        return format(compiledPattern, values, result, resultCopy, false, offsets);
    }

    /**
     * Returns the pattern text with none of the arguments.
     * Like formatting with all-empty string values.
     *
     * @param compiledPattern Compiled form of a pattern string.
     */
    public static String getTextWithNoArguments(String compiledPattern) {
        int capacity = compiledPattern.length() - 1 - getArgumentLimit(compiledPattern);
        StringBuilder sb = new StringBuilder(capacity);
        for (int i = 1; i < compiledPattern.length();) {
            int segmentLength = compiledPattern.charAt(i++) - ARG_NUM_LIMIT;
            if (segmentLength > 0) {
                int limit = i + segmentLength;
                sb.append(compiledPattern, i, limit);
                i = limit;
            }
        }
        return sb.toString();
    }

    private static StringBuilder format(
            String compiledPattern, CharSequence[] values,
            StringBuilder result, String resultCopy, boolean forbidResultAsValue,
            int[] offsets) {
        int offsetsLength;
        if (offsets == null) {
            offsetsLength = 0;
        } else {
            offsetsLength = offsets.length;
            for (int i = 0; i < offsetsLength; i++) {
                offsets[i] = -1;
            }
        }
        for (int i = 1; i < compiledPattern.length();) {
            int n = compiledPattern.charAt(i++);
            if (n < ARG_NUM_LIMIT) {
                CharSequence value = values[n];
                if (value == result) {
                    if (forbidResultAsValue) {
                        throw new IllegalArgumentException("Value must not be same object as result");
                    }
                    if (i == 2) {
                        // We are appending to result which is also the first value object.
                        if (n < offsetsLength) {
                            offsets[n] = 0;
                        }
                    } else {
                        if (n < offsetsLength) {
                            offsets[n] = result.length();
                        }
                        result.append(resultCopy);
                    }
                } else {
                    if (n < offsetsLength) {
                        offsets[n] = result.length();
                    }
                    result.append(value);
                }
            } else {
                int limit = i + (n - ARG_NUM_LIMIT);
                result.append(compiledPattern, i, limit);
                i = limit;
            }
        }
        return result;
    }
}
