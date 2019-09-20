/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import android.icu.impl.SimpleFormatterImpl;

/**
 * Formats simple patterns like "{1} was born in {0}".
 * Minimal subset of MessageFormat; fast, simple, minimal dependencies.
 * Supports only numbered arguments with no type nor style parameters,
 * and formats only string values.
 * Quoting via ASCII apostrophe compatible with ICU MessageFormat default behavior.
 *
 * <p>Factory methods throw exceptions for syntax errors
 * and for too few or too many arguments/placeholders.
 *
 * <p>SimpleFormatter objects are immutable and can be safely cached like strings.
 *
 * <p>Example:
 * <pre>
 * SimpleFormatter fmt = SimpleFormatter.compile("{1} '{born}' in {0}");
 *
 * // Output: "paul {born} in england"
 * System.out.println(fmt.format("england", "paul"));
 * </pre>
 *
 * @see MessageFormat
 * @see MessagePattern.ApostropheMode
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public final class SimpleFormatter {
    // For internal use in Java, use SimpleFormatterImpl directly instead:
    // It is most efficient to compile patterns to compiled-pattern strings
    // and use them with static methods.
    // (Avoids allocating SimpleFormatter wrapper objects.)

    /**
     * Binary representation of the compiled pattern.
     * @see SimpleFormatterImpl
     */
    private final String compiledPattern;

    private SimpleFormatter(String compiledPattern) {
        this.compiledPattern = compiledPattern;
    }

    /**
     * Creates a formatter from the pattern string.
     *
     * @param pattern The pattern string.
     * @return The new SimpleFormatter object.
     * @throws IllegalArgumentException for bad argument syntax.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static SimpleFormatter compile(CharSequence pattern) {
        return compileMinMaxArguments(pattern, 0, Integer.MAX_VALUE);
    }

    /**
     * Creates a formatter from the pattern string.
     * The number of arguments checked against the given limits is the
     * highest argument number plus one, not the number of occurrences of arguments.
     *
     * @param pattern The pattern string.
     * @param min The pattern must have at least this many arguments.
     * @param max The pattern must have at most this many arguments.
     * @return The new SimpleFormatter object.
     * @throws IllegalArgumentException for bad argument syntax and too few or too many arguments.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static SimpleFormatter compileMinMaxArguments(CharSequence pattern, int min, int max) {
        StringBuilder sb = new StringBuilder();
        String compiledPattern = SimpleFormatterImpl.compileToStringMinMaxArguments(pattern, sb, min, max);
        return new SimpleFormatter(compiledPattern);
    }

    /**
     * @return The max argument number + 1.
     * @hide draft / provisional / internal are hidden on Android
     */
    public int getArgumentLimit() {
        return SimpleFormatterImpl.getArgumentLimit(compiledPattern);
    }

    /**
     * Formats the given values.
     * @hide draft / provisional / internal are hidden on Android
     */
    public String format(CharSequence... values) {
        return SimpleFormatterImpl.formatCompiledPattern(compiledPattern, values);
    }

    /**
     * Formats the given values, appending to the appendTo builder.
     *
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
     * @hide draft / provisional / internal are hidden on Android
     */
    public StringBuilder formatAndAppend(
            StringBuilder appendTo, int[] offsets, CharSequence... values) {
        return SimpleFormatterImpl.formatAndAppend(compiledPattern, appendTo, offsets, values);
    }

    /**
     * Formats the given values, replacing the contents of the result builder.
     * May optimize by actually appending to the result if it is the same object
     * as the value corresponding to the initial argument in the pattern.
     *
     * @param result Gets its contents replaced by the formatted pattern and values.
     * @param offsets offsets[i] receives the offset of where
     *                values[i] replaced pattern argument {i}.
     *                Can be null, or can be shorter or longer than values.
     *                If there is no {i} in the pattern, then offsets[i] is set to -1.
     * @param values The argument values.
     *               An argument value may be the same object as result.
     *               values.length must be at least getArgumentLimit().
     * @return result
     * @hide draft / provisional / internal are hidden on Android
     */
    public StringBuilder formatAndReplace(
            StringBuilder result, int[] offsets, CharSequence... values) {
        return SimpleFormatterImpl.formatAndReplace(compiledPattern, result, offsets, values);
    }

    /**
     * Returns a string similar to the original pattern, only for debugging.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    public String toString() {
        String[] values = new String[getArgumentLimit()];
        for (int i = 0; i < values.length; i++) {
            values[i] = "{" + i + '}';
        }
        return formatAndAppend(new StringBuilder(), null, values).toString();
    }

    /**
     * Returns the pattern text with none of the arguments.
     * Like formatting with all-empty string values.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getTextWithNoArguments() {
        return SimpleFormatterImpl.getTextWithNoArguments(compiledPattern);
    }
}
