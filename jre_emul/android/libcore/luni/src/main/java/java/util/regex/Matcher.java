/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.regex;

/**
 * The result of applying a {@code Pattern} to a given input. See {@link Pattern} for
 * example uses.
 */
public final class Matcher implements MatchResult {

    /**
     * Holds the pattern, that is, the compiled regular expression.
     */
    private Pattern pattern;

    /**
     * The address of the native peer.
     */
    private long address;

    /**
     * Holds the input text.
     */
    private String input;
    // Need to hold a strong reference to the char buffer passed to uregex.
    private char[] inputChars;

    /**
     * Holds the start of the region, or 0 if the matching should start at the
     * beginning of the text.
     */
    private int regionStart;

    /**
     * Holds the end of the region, or input.length() if the matching should
     * go until the end of the input.
     */
    private int regionEnd;

    /**
     * Holds the position where the next append operation will take place.
     */
    private int appendPos;

    /**
     * Reflects whether a match has been found during the most recent find
     * operation.
     */
    private boolean matchFound;

    /**
     * Holds the offsets for the most recent match.
     */
    private int[] matchOffsets;

    /**
     * Reflects whether the bounds of the region are anchoring.
     */
    private boolean anchoringBounds = true;

    /**
     * Reflects whether the bounds of the region are transparent.
     */
    private boolean transparentBounds;

    /**
     * Creates a matcher for a given combination of pattern and input. Both
     * elements can be changed later on.
     *
     * @param pattern
     *            the pattern to use.
     * @param input
     *            the input to use.
     */
    Matcher(Pattern pattern, CharSequence input) {
        usePattern(pattern);
        reset(input);
    }

    /**
     * Appends a literal part of the input plus a replacement for the current
     * match to a given {@link StringBuffer}. The literal part is exactly the
     * part of the input between the previous match and the current match. The
     * method can be used in conjunction with {@link #find()} and
     * {@link #appendTail(StringBuffer)} to walk through the input and replace
     * all occurrences of the {@code Pattern} with something else.
     *
     * @param buffer
     *            the {@code StringBuffer} to append to.
     * @param replacement
     *            the replacement text.
     * @return the {@code Matcher} itself.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public Matcher appendReplacement(StringBuffer buffer, String replacement) {
        buffer.append(input.substring(appendPos, start()));
        appendEvaluated(buffer, replacement);
        appendPos = end();

        return this;
    }


    /**
     * Internal helper method to append a given string to a given string buffer.
     * If the string contains any references to groups, these are replaced by
     * the corresponding group's contents.
     *
     * @param buffer
     *            the string buffer.
     * @param s
     *            the string to append.
     */
    private void appendEvaluated(StringBuffer buffer, String s) {
        boolean escape = false;
        boolean dollar = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && !escape) {
                escape = true;
            } else if (c == '$' && !escape) {
                dollar = true;
            } else if (c >= '0' && c <= '9' && dollar) {
                buffer.append(group(c - '0'));
                dollar = false;
            } else {
                buffer.append(c);
                dollar = false;
                escape = false;
            }
        }

        // This seemingly stupid piece of code reproduces a JDK bug.
        if (escape) {
            throw new ArrayIndexOutOfBoundsException(s.length());
        }
    }


    /**
     * Resets the {@code Matcher}. This results in the region being set to the
     * whole input. Results of a previous find get lost. The next attempt to
     * find an occurrence of the {@link Pattern} in the string will start at the
     * beginning of the input.
     *
     * @return the {@code Matcher} itself.
     */
    public Matcher reset() {
        return reset(input, 0, input.length());
    }

    /**
     * Provides a new input and resets the {@code Matcher}. This results in the
     * region being set to the whole input. Results of a previous find get lost.
     * The next attempt to find an occurrence of the {@link Pattern} in the
     * string will start at the beginning of the input.
     *
     * @param input
     *            the new input sequence.
     *
     * @return the {@code Matcher} itself.
     */
    public Matcher reset(CharSequence input) {
        return reset(input.toString(), 0, input.length());
    }

    /**
     * Resets the Matcher. A new input sequence and a new region can be
     * specified. Results of a previous find get lost. The next attempt to find
     * an occurrence of the Pattern in the string will start at the beginning of
     * the region. This is the internal version of reset() to which the several
     * public versions delegate.
     *
     * @param input
     *            the input sequence.
     * @param start
     *            the start of the region.
     * @param end
     *            the end of the region.
     *
     * @return the matcher itself.
     */
    private Matcher reset(String input, int start, int end) {
        if (input == null) {
            throw new IllegalArgumentException("input == null");
        }

        if (start < 0 || end < 0 || start > input.length() || end > input.length() || start > end) {
            throw new IndexOutOfBoundsException();
        }

        this.input = input;
        this.inputChars = this.input.toCharArray();
        this.regionStart = start;
        this.regionEnd = end;
        resetForInput();

        matchFound = false;
        appendPos = 0;

        return this;
    }

    /**
     * Sets a new pattern for the {@code Matcher}. Results of a previous find
     * get lost. The next attempt to find an occurrence of the {@link Pattern}
     * in the string will start at the beginning of the input.
     *
     * @param pattern
     *            the new {@code Pattern}.
     *
     * @return the {@code Matcher} itself.
     */
    public Matcher usePattern(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern == null");
        }

        this.pattern = pattern;

        if (address != 0) {
            closeImpl(address);
            address = 0; // In case openImpl throws.
        }
        address = openImpl(pattern.address);

        if (input != null) {
            resetForInput();
        }

        matchOffsets = new int[(groupCount() + 1) * 2];
        matchFound = false;
        return this;
    }

    private void resetForInput() {
        setInputImpl(address, inputChars, regionStart, regionEnd, anchoringBounds,
            transparentBounds);
    }

    /**
     * Resets this matcher and sets a region. Only characters inside the region
     * are considered for a match.
     *
     * @param start
     *            the first character of the region.
     * @param end
     *            the first character after the end of the region.
     * @return the {@code Matcher} itself.
     */
    public Matcher region(int start, int end) {
        return reset(input, start, end);
    }

    /**
     * Appends the (unmatched) remainder of the input to the given
     * {@link StringBuffer}. The method can be used in conjunction with
     * {@link #find()} and {@link #appendReplacement(StringBuffer, String)} to
     * walk through the input and replace all matches of the {@code Pattern}
     * with something else.
     *
     * @return the {@code StringBuffer}.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public StringBuffer appendTail(StringBuffer buffer) {
        if (appendPos < regionEnd) {
            buffer.append(input.substring(appendPos, regionEnd));
        }
        return buffer;
    }


    /**
     * Replaces the first occurrence of this matcher's pattern in the input with
     * a given string.
     *
     * @param replacement
     *            the replacement text.
     * @return the modified input string.
     */
    public String replaceFirst(String replacement) {
        reset();
        StringBuffer buffer = new StringBuffer(input.length());
        if (find()) {
            appendReplacement(buffer, replacement);
        }
        return appendTail(buffer).toString();
    }

    /**
     * Replaces all occurrences of this matcher's pattern in the input with a
     * given string.
     *
     * @param replacement
     *            the replacement text.
     * @return the modified input string.
     */
    public String replaceAll(String replacement) {
        reset();
        StringBuffer buffer = new StringBuffer(input.length());
        while (find()) {
            appendReplacement(buffer, replacement);
        }
        return appendTail(buffer).toString();
    }

    /**
     * Returns the {@link Pattern} instance used inside this matcher.
     */
    public Pattern pattern() {
        return pattern;
    }

    /**
     * Returns true if there is another match in the input, starting
     * from the given position. The region is ignored.
     *
     * @throws IndexOutOfBoundsException if {@code start < 0 || start > input.length()}
     */
    public boolean find(int start) {
        if (start < 0 || start > input.length()) {
            throw new IndexOutOfBoundsException("start=" + start + "; length=" + input.length());
        }

        matchFound = findImpl(address, start, matchOffsets);
        return matchFound;
    }

    /**
     * Moves to the next occurrence of the pattern in the input. If a
     * previous match was successful, the method continues the search from the
     * first character following that match in the input. Otherwise it searches
     * either from the region start (if one has been set), or from position 0.
     *
     * @return true if (and only if) a match has been found.
     */
    public boolean find() {
        matchFound = findNextImpl(address, matchOffsets);
        return matchFound;
    }

    /**
     * Tries to match the {@link Pattern}, starting from the beginning of the
     * region (or the beginning of the input, if no region has been set).
     * Doesn't require the {@code Pattern} to match against the whole region.
     *
     * @return true if (and only if) the {@code Pattern} matches.
     */
    public boolean lookingAt() {
        matchFound = lookingAtImpl(address, matchOffsets);
        return matchFound;
    }

    /**
     * Tries to match the {@link Pattern} against the entire region (or the
     * entire input, if no region has been set).
     *
     * @return true if (and only if) the {@code Pattern} matches the entire
     *         region.
     */
    public boolean matches() {
        matchFound = matchesImpl(address, matchOffsets);
        return matchFound;
    }

    /**
     * Returns a replacement string for the given one that has all backslashes
     * and dollar signs escaped.
     */
    public static String quoteReplacement(String s) {
        StringBuilder result = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$') {
                result.append('\\');
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * Converts the current match into a separate {@link MatchResult} instance
     * that is independent from this matcher. The new object is unaffected when
     * the state of this matcher changes.
     *
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public MatchResult toMatchResult() {
        ensureMatch();
        return new MatchResultImpl(input, matchOffsets);
    }

    /**
     * Determines whether this matcher has anchoring bounds enabled or not. When
     * anchoring bounds are enabled, the start and end of the input match the
     * '^' and '$' meta-characters, otherwise not. Anchoring bounds are enabled
     * by default.
     *
     * @return the {@code Matcher} itself.
     */
    public Matcher useAnchoringBounds(boolean value) {
        anchoringBounds = value;
        useAnchoringBoundsImpl(address, value);
        return this;
    }

    /**
     * Returns true if this matcher has anchoring bounds enabled. When
     * anchoring bounds are enabled, the start and end of the input match the
     * '^' and '$' meta-characters, otherwise not. Anchoring bounds are enabled
     * by default.
     */
    public boolean hasAnchoringBounds() {
        return anchoringBounds;
    }

    /**
     * Determines whether this matcher has transparent bounds enabled or not.
     * When transparent bounds are enabled, the parts of the input outside the
     * region are subject to lookahead and lookbehind, otherwise they are not.
     * Transparent bounds are disabled by default.
     *
     * @return the {@code Matcher} itself.
     */
    public Matcher useTransparentBounds(boolean value) {
        transparentBounds = value;
        useTransparentBoundsImpl(address, value);
        return this;
    }

    /**
     * Makes sure that a successful match has been made. Is invoked internally
     * from various places in the class.
     *
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    private void ensureMatch() {
        if (!matchFound) {
            throw new IllegalStateException("No successful match so far");
        }
    }

    /**
     * Returns true if this matcher has transparent bounds enabled. When
     * transparent bounds are enabled, the parts of the input outside the region
     * are subject to lookahead and lookbehind, otherwise they are not.
     * Transparent bounds are disabled by default.
     */
    public boolean hasTransparentBounds() {
        return transparentBounds;
    }

    /**
     * Returns this matcher's region start, that is, the index of the first character that is
     * considered for a match.
     */
    public int regionStart() {
        return regionStart;
    }

    /**
     * Returns this matcher's region end, that is, the index of the first character that is
     * not considered for a match.
     */
    public int regionEnd() {
        return regionEnd;
    }

    /**
     * Returns true if the most recent match succeeded and additional input could cause
     * it to fail. If this method returns false and a match was found, then more input
     * might change the match but the match won't be lost. If a match was not found,
     * then requireEnd has no meaning.
     */
    public boolean requireEnd() {
        return requireEndImpl(address);
    }

    /**
     * Returns true if the most recent matching operation attempted to access
     * additional text beyond the available input, meaning that additional input
     * could change the results of the match.
     */
    public boolean hitEnd() {
        return hitEndImpl(address);
    }

    @Override protected void finalize() throws Throwable {
        try {
            closeImpl(address);
        } finally {
            super.finalize();
        }
    }

    /**
     * Returns a string representing this {@code Matcher}.
     * The format of this string is unspecified.
     */
    @Override public String toString() {
        return getClass().getName() + "[pattern=" + pattern() +
            " region=" + regionStart() + "," + regionEnd() +
            " lastmatch=" + (matchFound ? group() : "") + "]";
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if no successful match has been made.
     */
    public int end() {
        return end(0);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if no successful match has been made.
     */
    public int end(int group) {
        ensureMatch();
        return matchOffsets[(group * 2) + 1];
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if no successful match has been made.
     */
    public String group() {
        return group(0);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if no successful match has been made.
     */
    public String group(int group) {
        ensureMatch();
        int from = matchOffsets[group * 2];
        int to = matchOffsets[(group * 2) + 1];
        if (from == -1 || to == -1) {
            return null;
        } else {
            return input.substring(from, to);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int groupCount() {
        return groupCountImpl(address);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if no successful match has been made.
     */
    public int start() {
        return start(0);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if no successful match has been made.
     */
    public int start(int group) throws IllegalStateException {
        ensureMatch();
        return matchOffsets[group * 2];
    }

    private static native void closeImpl(long addr);
    private static native boolean findImpl(long addr, int startIndex, int[] offsets);
    private static native boolean findNextImpl(long addr, int[] offsets);
    private static native int groupCountImpl(long addr);
    private static native boolean hitEndImpl(long addr);
    private static native boolean lookingAtImpl(long addr, int[] offsets);
    private static native boolean matchesImpl(long addr, int[] offsets);
    private static native long openImpl(long patternAddr);
    private static native boolean requireEndImpl(long addr);
    private static native void setInputImpl(long addr, char[] s, int start, int end,
        boolean anchoringBounds, boolean transparentBounds);
    private static native void useAnchoringBoundsImpl(long addr, boolean value);
    private static native void useTransparentBoundsImpl(long addr, boolean value);
}
