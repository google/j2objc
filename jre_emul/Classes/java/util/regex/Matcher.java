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
     * Holds the handle for the native version of the pattern.
     */
    private int address;

    /**
     * Holds the input text.
     */
    private String input;

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
     * Holds the position where the next find operation will take place.
     */
    private int findPos;

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
        return reset(input, 0, input.length());
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
    private Matcher reset(CharSequence input, int start, int end) {
        if (input == null) {
            throw new IllegalArgumentException();
        }

        if (start < 0 || end < 0 || start > input.length() || end > input.length() || start > end) {
            throw new IndexOutOfBoundsException();
        }

        this.input = input.toString();
        this.regionStart = start;
        this.regionEnd = end;
        resetForInput();

        matchFound = false;
        findPos = regionStart;
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
            throw new IllegalArgumentException();
        }

        this.pattern = pattern;

        if (address != 0) {
            closeImpl(address);
            address = 0;
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
        setInputImpl(address, input, regionStart, regionEnd);
        useAnchoringBoundsImpl(address, anchoringBounds);
        useTransparentBoundsImpl(address, transparentBounds);
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
     * @param buffer
     *            the {@code StringBuffer} to append to.
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
     *
     * @return the {@code Pattern} instance.
     */
    public Pattern pattern() {
        return pattern;
    }

    /**
     * Returns the text that matched a given group of the regular expression.
     * Explicit capturing groups in the pattern are numbered left to right in order
     * of their <i>opening</i> parenthesis, starting at 1.
     * The special group 0 represents the entire match (as if the entire pattern is surrounded
     * by an implicit capturing group).
     * For example, "a((b)c)" matching "abc" would give the following groups:
     * <pre>
     * 0 "abc"
     * 1 "bc"
     * 2 "b"
     * </pre>
     *
     * <p>An optional capturing group that failed to match as part of an overall
     * successful match (for example, "a(b)?c" matching "ac") returns null.
     * A capturing group that matched the empty string (for example, "a(b?)c" matching "ac")
     * returns the empty string.
     *
     * @throws IllegalStateException
     *             if no successful match has been made.
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
     * Returns the text that matched the whole regular expression.
     *
     * @return the text.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public String group() {
        return group(0);
    }

    /**
     * Returns the next occurrence of the {@link Pattern} in the input. The
     * method starts the search from the given character in the input.
     *
     * @param start
     *            The index in the input at which the find operation is to
     *            begin. If this is less than the start of the region, it is
     *            automatically adjusted to that value. If it is beyond the end
     *            of the region, the method will fail.
     * @return true if (and only if) a match has been found.
     */
    public boolean find(int start) {
        findPos = start;

        if (findPos < regionStart) {
            findPos = regionStart;
        } else if (findPos >= regionEnd) {
            matchFound = false;
            return false;
        }

        matchFound = findImpl(address, input, findPos, matchOffsets);
        if (matchFound) {
            findPos = matchOffsets[1];
        }
        return matchFound;
    }

    /**
     * Returns the next occurrence of the {@link Pattern} in the input. If a
     * previous match was successful, the method continues the search from the
     * first character following that match in the input. Otherwise it searches
     * either from the region start (if one has been set), or from position 0.
     *
     * @return true if (and only if) a match has been found.
     */
    public boolean find() {
        matchFound = findNextImpl(address, input, matchOffsets);
        if (matchFound) {
            findPos = matchOffsets[1];
        }
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
        matchFound = lookingAtImpl(address, input, matchOffsets);
        if (matchFound) {
            findPos = matchOffsets[1];
        }
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
        matchFound = matchesImpl(address, input, matchOffsets);
        if (matchFound) {
            findPos = matchOffsets[1];
        }
        return matchFound;
    }

    /**
     * Returns the index of the first character of the text that matched a given
     * group.
     *
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public int start(int group) throws IllegalStateException {
        ensureMatch();
        return matchOffsets[group * 2];
    }

    /**
     * Returns the index of the first character following the text that matched
     * a given group.
     *
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public int end(int group) {
        ensureMatch();
        return matchOffsets[(group * 2) + 1];
    }

    /**
     * Returns a replacement string for the given one that has all backslashes
     * and dollar signs escaped.
     *
     * @param s
     *            the input string.
     * @return the input string, with all backslashes and dollar signs having
     *         been escaped.
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
     * Returns the index of the first character of the text that matched the
     * whole regular expression.
     *
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public int start() {
        return start(0);
    }

    /**
     * Returns the number of groups in the results, which is always equal to
     * the number of groups in the original regular expression.
     *
     * @return the number of groups.
     */
    public int groupCount() {
        return groupCountImpl(address);
    }

    /**
     * Returns the index of the first character following the text that matched
     * the whole regular expression.
     *
     * @return the character index.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public int end() {
        return end(0);
    }

    /**
     * Converts the current match into a separate {@link MatchResult} instance
     * that is independent from this matcher. The new object is unaffected when
     * the state of this matcher changes.
     *
     * @return the new {@code MatchResult}.
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
     * @param value
     *            the new value for anchoring bounds.
     * @return the {@code Matcher} itself.
     */
    public Matcher useAnchoringBounds(boolean value) {
        anchoringBounds = value;
        useAnchoringBoundsImpl(address, value);
        return this;
    }

    /**
     * Indicates whether this matcher has anchoring bounds enabled. When
     * anchoring bounds are enabled, the start and end of the input match the
     * '^' and '$' meta-characters, otherwise not. Anchoring bounds are enabled
     * by default.
     *
     * @return true if (and only if) the {@code Matcher} uses anchoring bounds.
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
     * @param value
     *            the new value for transparent bounds.
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
     * Indicates whether this matcher has transparent bounds enabled. When
     * transparent bounds are enabled, the parts of the input outside the region
     * are subject to lookahead and lookbehind, otherwise they are not.
     * Transparent bounds are disabled by default.
     *
     * @return true if (and only if) the {@code Matcher} uses anchoring bounds.
     */
    public boolean hasTransparentBounds() {
        return transparentBounds;
    }

    /**
     * Returns this matcher's region start, that is, the first character that is
     * considered for a match.
     *
     * @return the start of the region.
     */
    public int regionStart() {
        return regionStart;
    }

    /**
     * Returns this matcher's region end, that is, the first character that is
     * not considered for a match.
     *
     * @return the end of the region.
     */
    public int regionEnd() {
        return regionEnd;
    }

    /**
     * Indicates whether more input might change a successful match into an
     * unsuccessful one.
     *
     * @return true if (and only if) more input might change a successful match
     *         into an unsuccessful one.
     */
    public boolean requireEnd() {
        return requireEndImpl(address);
    }

    /**
     * Indicates whether the last match hit the end of the input.
     *
     * @return true if (and only if) the last match hit the end of the input.
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

    private static native void closeImpl(int addr) /*-[
  	[self doesNotRecognizeSelector:_cmd];
    ]-*/;
    private static native boolean findImpl(int addr, String s, int startIndex, int[] offsets) /*-[
  	[self doesNotRecognizeSelector:_cmd];
  	return 0;
    ]-*/;
    private static native boolean findNextImpl(int addr, String s, int[] offsets) /*-[
      [self doesNotRecognizeSelector:_cmd];
      return 0;
    ]-*/;
    private static native int groupCountImpl(int addr) /*-[
      [self doesNotRecognizeSelector:_cmd];
      return 0;
    ]-*/;
    private static native boolean hitEndImpl(int addr) /*-[
      [self doesNotRecognizeSelector:_cmd];
      return 0;
    ]-*/;
    private static native boolean lookingAtImpl(int addr, String s, int[] offsets) /*-[
      [self doesNotRecognizeSelector:_cmd];
      return 0;
    ]-*/;
    private static native boolean matchesImpl(int addr, String s, int[] offsets) /*-[
      [self doesNotRecognizeSelector:_cmd];
      return 0;
    ]-*/;
    private static native int openImpl(int patternAddr) /*-[
      [self doesNotRecognizeSelector:_cmd];
      return 0;
    ]-*/;
    private static native boolean requireEndImpl(int addr) /*-[
      [self doesNotRecognizeSelector:_cmd];
      return 0;
    ]-*/;
    private static native void setInputImpl(int addr, String s, int start, int end) /*-[
      [self doesNotRecognizeSelector:_cmd];
    ]-*/;
    private static native void useAnchoringBoundsImpl(int addr, boolean value) /*-[
      [self doesNotRecognizeSelector:_cmd];
    ]-*/;
    private static native void useTransparentBoundsImpl(int addr, boolean value) /*-[
      [self doesNotRecognizeSelector:_cmd];
    ]-*/;
}
