/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.util.regex;

/* J2ObjC removed.
import com.android.icu.util.regex.MatcherNative;
*/

/**
 * An engine that performs match operations on a {@linkplain java.lang.CharSequence
 * character sequence} by interpreting a {@link Pattern}.
 *
 * <p> A matcher is created from a pattern by invoking the pattern's {@link
 * Pattern#matcher matcher} method.  Once created, a matcher can be used to
 * perform three different kinds of match operations:
 *
 * <ul>
 *
 *   <li><p> The {@link #matches matches} method attempts to match the entire
 *   input sequence against the pattern.  </p></li>
 *
 *   <li><p> The {@link #lookingAt lookingAt} method attempts to match the
 *   input sequence, starting at the beginning, against the pattern.  </p></li>
 *
 *   <li><p> The {@link #find find} method scans the input sequence looking for
 *   the next subsequence that matches the pattern.  </p></li>
 *
 * </ul>
 *
 * <p> Each of these methods returns a boolean indicating success or failure.
 * More information about a successful match can be obtained by querying the
 * state of the matcher.
 *
 * <p> A matcher finds matches in a subset of its input called the
 * <i>region</i>. By default, the region contains all of the matcher's input.
 * The region can be modified via the{@link #region region} method and queried
 * via the {@link #regionStart regionStart} and {@link #regionEnd regionEnd}
 * methods. The way that the region boundaries interact with some pattern
 * constructs can be changed. See {@link #useAnchoringBounds
 * useAnchoringBounds} and {@link #useTransparentBounds useTransparentBounds}
 * for more details.
 *
 * <p> This class also defines methods for replacing matched subsequences with
 * new strings whose contents can, if desired, be computed from the match
 * result.  The {@link #appendReplacement appendReplacement} and {@link
 * #appendTail appendTail} methods can be used in tandem in order to collect
 * the result into an existing string buffer, or the more convenient {@link
 * #replaceAll replaceAll} method can be used to create a string in which every
 * matching subsequence in the input sequence is replaced.
 *
 * <p> The explicit state of a matcher includes the start and end indices of
 * the most recent successful match.  It also includes the start and end
 * indices of the input subsequence captured by each <a
 * href="Pattern.html#cg">capturing group</a> in the pattern as well as a total
 * count of such subsequences.  As a convenience, methods are also provided for
 * returning these captured subsequences in string form.
 *
 * <p> The explicit state of a matcher is initially undefined; attempting to
 * query any part of it before a successful match will cause an {@link
 * IllegalStateException} to be thrown.  The explicit state of a matcher is
 * recomputed by every match operation.
 *
 * <p> The implicit state of a matcher includes the input character sequence as
 * well as the <i>append position</i>, which is initially zero and is updated
 * by the {@link #appendReplacement appendReplacement} method.
 *
 * <p> A matcher may be reset explicitly by invoking its {@link #reset()}
 * method or, if a new input sequence is desired, its {@link
 * #reset(java.lang.CharSequence) reset(CharSequence)} method.  Resetting a
 * matcher discards its explicit state information and sets the append position
 * to zero.
 *
 * <p> Instances of this class are not safe for use by multiple concurrent
 * threads. </p>
 *
 *
 * @author      Mike McCloskey
 * @author      Mark Reinhold
 * @author      JSR-51 Expert Group
 * @since       1.4
 * @spec        JSR-51
 */

/**
 * J2ObjC modified: Synchronizations are removed because they were added in the Android sources
 * to safeguard against hard-to-debug crashes from incorrect concurrent use of this class.
 */
public final class Matcher implements MatchResult {

    /**
     * The Pattern object that created this Matcher.
     */
    private Pattern parentPattern;

    /**
     * Holds the offsets for the most recent match.
     */
    int[] groups;

    /**
     * The range within the sequence that is to be matched (between  0
     * and text.length()).
     */
    int from, to;

    /**
     * Holds the input text.
     */
    String text;

    /* J2ObjC added: platform-specific implementation. */
    private char[] inputChars;

    /**
     * Reflects whether a match has been found during the most recent find
     * operation.
     */
    private boolean matchFound;

    /* J2ObjC removed.
    private MatcherNative nativeMatcher;
    */

    /* J2ObjC added: platform-specific implementation. */
    private long address;

    /**
     * The index of the last position appended in a substitution.
     */
    int appendPos = 0;

    /**
     * Holds the original CharSequence for use in {@link #reset}. {@link #text} is used during
     * matching. Note that CharSequence is mutable while String is not, so reset can cause the input
     * to match to change.
     */
    private CharSequence originalInput;

    /**
     * If transparentBounds is true then the boundaries of this
     * matcher's region are transparent to lookahead, lookbehind,
     * and boundary matching constructs that try to see beyond them.
     */
    boolean transparentBounds = false;

    /**
     * If anchoringBounds is true then the boundaries of this
     * matcher's region match anchors such as ^ and $.
     */
    boolean anchoringBounds = true;

    /**
     * All matchers have the state used by Pattern during a match.
     */
    Matcher(Pattern parent, CharSequence text) {
        usePattern(parent);
        reset(text);
    }

    /**
     * Returns the pattern that is interpreted by this matcher.
     *
     * @return  The pattern for which this matcher was created
     */
    public Pattern pattern() {
        return parentPattern;
    }

    /**
     * Returns the match state of this matcher as a {@link MatchResult}.
     * The result is unaffected by subsequent operations performed upon this
     * matcher.
     *
     * @return  a <code>MatchResult</code> with the state of this matcher
     * @since 1.5
     */
    public MatchResult toMatchResult() {
        ensureMatch();
        return new OffsetBasedMatchResult(text, groups);
    }

    /**
     * Changes the <tt>Pattern</tt> that this <tt>Matcher</tt> uses to
     * find matches with.
     *
     * <p> This method causes this matcher to lose information
     * about the groups of the last match that occurred. The
     * matcher's position in the input is maintained and its
     * last append position is unaffected.</p>
     *
     * @param  newPattern
     *         The new pattern used by this matcher
     * @return  This matcher
     * @throws  IllegalArgumentException
     *          If newPattern is <tt>null</tt>
     * @since 1.5
     */
    public Matcher usePattern(Pattern newPattern) {
        if (newPattern == null)
            throw new IllegalArgumentException("Pattern cannot be null");

        /* J2ObjC removed.
        synchronized (this) {
            // may throw
            nativeMatcher = MatcherNative.create(newPattern.nativePattern);
        }
        */
        parentPattern = newPattern;

        /* J2ObjC added: platform-specific implementation. */
        if (address != 0) {
            closeImpl(address);
            address = 0;
        }
        address = openImpl(parentPattern.address);

        if (text != null) {
            resetForInput();
        }

        groups = new int[(groupCount() + 1) * 2];
        matchFound = false;
        return this;
    }

    /**
     * Resets this matcher.
     *
     * <p> Resetting a matcher discards all of its explicit state information
     * and sets its append position to zero. The matcher's region is set to the
     * default region, which is its entire character sequence. The anchoring
     * and transparency of this matcher's region boundaries are unaffected.
     *
     * @return  This matcher
     */
    public Matcher reset() {
        return reset(originalInput, 0, originalInput.length());
    }

    /**
     * Resets this matcher with a new input sequence.
     *
     * <p> Resetting a matcher discards all of its explicit state information
     * and sets its append position to zero.  The matcher's region is set to
     * the default region, which is its entire character sequence.  The
     * anchoring and transparency of this matcher's region boundaries are
     * unaffected.
     *
     * @param  input
     *         The new input character sequence
     *
     * @return  This matcher
     */
    public Matcher reset(CharSequence input) {
        return reset(input, 0, input.length());
    }

    /**
     * Returns the start index of the previous match.
     *
     * @return  The index of the first character matched
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     */
    public int start() {
        return start(0);
    }

    /**
     * Returns the start index of the subsequence captured by the given group
     * during the previous match operation.
     *
     * <p> <a href="Pattern.html#cg">Capturing groups</a> are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression <i>m.</i><tt>start(0)</tt> is equivalent to
     * <i>m.</i><tt>start()</tt>.  </p>
     *
     * @param  group
     *         The index of a capturing group in this matcher's pattern
     *
     * @return  The index of the first character captured by the group,
     *          or <tt>-1</tt> if the match was successful but the group
     *          itself did not match anything
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     *
     * @throws  IndexOutOfBoundsException
     *          If there is no capturing group in the pattern
     *          with the given index
     */
    public int start(int group) {
        ensureMatch();
        if (group < 0 || group > groupCount())
            throw new IndexOutOfBoundsException("No group " + group);
        return groups[group * 2];
    }

    /**
     * Returns the start index of the subsequence captured by the given
     * <a href="Pattern.html#groupname">named-capturing group</a> during the
     * previous match operation.
     *
     * @param  name
     *         The name of a named-capturing group in this matcher's pattern
     *
     * @return  The index of the first character captured by the group,
     *          or {@code -1} if the match was successful but the group
     *          itself did not match anything
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     *
     * @throws  IllegalArgumentException
     *          If there is no capturing group in the pattern
     *          with the given name
     * @since 1.8
     */
    public int start(String name) {
        return groups[getMatchedGroupIndex(name) * 2];
    }

    /**
     * Returns the offset after the last character matched.
     *
     * @return  The offset after the last character matched
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     */
    public int end() {
        return end(0);
    }

    /**
     * Returns the offset after the last character of the subsequence
     * captured by the given group during the previous match operation.
     *
     * <p> <a href="Pattern.html#cg">Capturing groups</a> are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression <i>m.</i><tt>end(0)</tt> is equivalent to
     * <i>m.</i><tt>end()</tt>.  </p>
     *
     * @param  group
     *         The index of a capturing group in this matcher's pattern
     *
     * @return  The offset after the last character captured by the group,
     *          or <tt>-1</tt> if the match was successful
     *          but the group itself did not match anything
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     *
     * @throws  IndexOutOfBoundsException
     *          If there is no capturing group in the pattern
     *          with the given index
     */
    public int end(int group) {
        ensureMatch();
        if (group < 0 || group > groupCount())
            throw new IndexOutOfBoundsException("No group " + group);
        return groups[group * 2 + 1];
    }

    /**
     * Returns the offset after the last character of the subsequence
     * captured by the given <a href="Pattern.html#groupname">named-capturing
     * group</a> during the previous match operation.
     *
     * @param  name
     *         The name of a named-capturing group in this matcher's pattern
     *
     * @return  The offset after the last character captured by the group,
     *          or {@code -1} if the match was successful
     *          but the group itself did not match anything
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     *
     * @throws  IllegalArgumentException
     *          If there is no capturing group in the pattern
     *          with the given name
     * @since 1.8
     */
    public int end(String name) {
        return groups[getMatchedGroupIndex(name) * 2 + 1];
    }

    /**
     * Returns the input subsequence matched by the previous match.
     *
     * <p> For a matcher <i>m</i> with input sequence <i>s</i>,
     * the expressions <i>m.</i><tt>group()</tt> and
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(),</tt>&nbsp;<i>m.</i><tt>end())</tt>
     * are equivalent.  </p>
     *
     * <p> Note that some patterns, for example <tt>a*</tt>, match the empty
     * string.  This method will return the empty string when the pattern
     * successfully matches the empty string in the input.  </p>
     *
     * @return The (possibly empty) subsequence matched by the previous match,
     *         in string form
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     */
    public String group() {
        return group(0);
    }

    /**
     * Returns the input subsequence captured by the given group during the
     * previous match operation.
     *
     * <p> For a matcher <i>m</i>, input sequence <i>s</i>, and group index
     * <i>g</i>, the expressions <i>m.</i><tt>group(</tt><i>g</i><tt>)</tt> and
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(</tt><i>g</i><tt>),</tt>&nbsp;<i>m.</i><tt>end(</tt><i>g</i><tt>))</tt>
     * are equivalent.  </p>
     *
     * <p> <a href="Pattern.html#cg">Capturing groups</a> are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression <tt>m.group(0)</tt> is equivalent to <tt>m.group()</tt>.
     * </p>
     *
     * <p> If the match was successful but the group specified failed to match
     * any part of the input sequence, then <tt>null</tt> is returned. Note
     * that some groups, for example <tt>(a*)</tt>, match the empty string.
     * This method will return the empty string when such a group successfully
     * matches the empty string in the input.  </p>
     *
     * @param  group
     *         The index of a capturing group in this matcher's pattern
     *
     * @return  The (possibly empty) subsequence captured by the group
     *          during the previous match, or <tt>null</tt> if the group
     *          failed to match part of the input
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     *
     * @throws  IndexOutOfBoundsException
     *          If there is no capturing group in the pattern
     *          with the given index
     */
    public String group(int group) {
        ensureMatch();
        if (group < 0 || group > groupCount())
            throw new IndexOutOfBoundsException("No group " + group);
        if ((groups[group*2] == -1) || (groups[group*2+1] == -1))
            return null;
        return getSubSequence(groups[group * 2], groups[group * 2 + 1]).toString();
    }

    /**
     * Returns the input subsequence captured by the given
     * <a href="Pattern.html#groupname">named-capturing group</a> during the previous
     * match operation.
     *
     * <p> If the match was successful but the group specified failed to match
     * any part of the input sequence, then <tt>null</tt> is returned. Note
     * that some groups, for example <tt>(a*)</tt>, match the empty string.
     * This method will return the empty string when such a group successfully
     * matches the empty string in the input.  </p>
     *
     * @param  name
     *         The name of a named-capturing group in this matcher's pattern
     *
     * @return  The (possibly empty) subsequence captured by the named group
     *          during the previous match, or <tt>null</tt> if the group
     *          failed to match part of the input
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     *
     * @throws  IllegalArgumentException
     *          If there is no capturing group in the pattern
     *          with the given name
     * @since 1.7
     */
    public String group(String name) {
        int group = getMatchedGroupIndex(name);
        if ((groups[group*2] == -1) || (groups[group*2+1] == -1))
            return null;
        return getSubSequence(groups[group * 2], groups[group * 2 + 1]).toString();
    }

    /**
     * Returns the number of capturing groups in this matcher's pattern.
     *
     * <p> Group zero denotes the entire pattern by convention. It is not
     * included in this count.
     *
     * <p> Any non-negative integer smaller than or equal to the value
     * returned by this method is guaranteed to be a valid group index for
     * this matcher.  </p>
     *
     * @return The number of capturing groups in this matcher's pattern
     */
    public int groupCount() {
        /* J2ObjC modified: platform-specific implementation.
        synchronized (this) {
            return nativeMatcher.groupCount();
        }
        */
        return groupCountImpl(address);
    }

    /**
     * Attempts to match the entire region against the pattern.
     *
     * <p> If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.  </p>
     *
     * @return  <tt>true</tt> if, and only if, the entire region sequence
     *          matches this matcher's pattern
     */
    public boolean matches() {
        /* J2ObjC modified: platform-specific implementation.
        synchronized (this) {
            matchFound = nativeMatcher.matches(groups);
        }
        */
        matchFound = matchesImpl(address, groups);
        return matchFound;
    }

    /**
     * Attempts to find the next subsequence of the input sequence that matches
     * the pattern.
     *
     * <p> This method starts at the beginning of this matcher's region, or, if
     * a previous invocation of the method was successful and the matcher has
     * not since been reset, at the first character not matched by the previous
     * match.
     *
     * <p> If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.  </p>
     *
     * @return  <tt>true</tt> if, and only if, a subsequence of the input
     *          sequence matches this matcher's pattern
     */
    public boolean find() {
        /* J2ObjC modified: platform-specific implementation.
        synchronized (this) {
            matchFound = nativeMatcher.findNext(groups);
        }
        */
        matchFound = findNextImpl(address, groups);
        return matchFound;
    }

    /**
     * Resets this matcher and then attempts to find the next subsequence of
     * the input sequence that matches the pattern, starting at the specified
     * index.
     *
     * <p> If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods, and subsequent
     * invocations of the {@link #find()} method will start at the first
     * character not matched by this match.  </p>
     *
     * @param start the index to start searching for a match
     * @throws  IndexOutOfBoundsException
     *          If start is less than zero or if start is greater than the
     *          length of the input sequence.
     *
     * @return  <tt>true</tt> if, and only if, a subsequence of the input
     *          sequence starting at the given index matches this matcher's
     *          pattern
     */
    public boolean find(int start) {
        int limit = getTextLength();
        if ((start < 0) || (start > limit))
            throw new IndexOutOfBoundsException("Illegal start index");
        reset();
        /* J2ObjC modified: platform-specific implementation.
        synchronized (this) {
            matchFound = nativeMatcher.find(start, groups);
        }
        */
        matchFound = findImpl(address, start, groups);
        return matchFound;
    }

    /**
     * Attempts to match the input sequence, starting at the beginning of the
     * region, against the pattern.
     *
     * <p> Like the {@link #matches matches} method, this method always starts
     * at the beginning of the region; unlike that method, it does not
     * require that the entire region be matched.
     *
     * <p> If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.  </p>
     *
     * @return  <tt>true</tt> if, and only if, a prefix of the input
     *          sequence matches this matcher's pattern
     */
    public boolean lookingAt() {
        /* J2ObjC modified: platform-specific implementation.
        synchronized (this) {
            matchFound = nativeMatcher.lookingAt(groups);
        }
        */
        matchFound = lookingAtImpl(address, groups);
        return matchFound;
    }

    /**
     * Returns a literal replacement <code>String</code> for the specified
     * <code>String</code>.
     *
     * This method produces a <code>String</code> that will work
     * as a literal replacement <code>s</code> in the
     * <code>appendReplacement</code> method of the {@link Matcher} class.
     * The <code>String</code> produced will match the sequence of characters
     * in <code>s</code> treated as a literal sequence. Slashes ('\') and
     * dollar signs ('$') will be given no special meaning.
     *
     * @param  s The string to be literalized
     * @return  A literal string replacement
     * @since 1.5
     */
    public static String quoteReplacement(String s) {
        if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
            return s;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Implements a non-terminal append-and-replace step.
     *
     * <p> This method performs the following actions: </p>
     *
     * <ol>
     *
     *   <li><p> It reads characters from the input sequence, starting at the
     *   append position, and appends them to the given string buffer.  It
     *   stops after reading the last character preceding the previous match,
     *   that is, the character at index {@link
     *   #start()}&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>.  </p></li>
     *
     *   <li><p> It appends the given replacement string to the string buffer.
     *   </p></li>
     *
     *   <li><p> It sets the append position of this matcher to the index of
     *   the last character matched, plus one, that is, to {@link #end()}.
     *   </p></li>
     *
     * </ol>
     *
     * <p> The replacement string may contain references to subsequences
     * captured during the previous match: Each occurrence of
     * <tt>${</tt><i>name</i><tt>}</tt> or <tt>$</tt><i>g</i>
     * will be replaced by the result of evaluating the corresponding
     * {@link #group(String) group(name)} or {@link #group(int) group(g)}
     * respectively. For  <tt>$</tt><i>g</i>,
     * the first number after the <tt>$</tt> is always treated as part of
     * the group reference. Subsequent numbers are incorporated into g if
     * they would form a legal group reference. Only the numerals '0'
     * through '9' are considered as potential components of the group
     * reference. If the second group matched the string <tt>"foo"</tt>, for
     * example, then passing the replacement string <tt>"$2bar"</tt> would
     * cause <tt>"foobar"</tt> to be appended to the string buffer. A dollar
     * sign (<tt>$</tt>) may be included as a literal in the replacement
     * string by preceding it with a backslash (<tt>\$</tt>).
     *
     * <p> Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     *
     * <p> This method is intended to be used in a loop together with the
     * {@link #appendTail appendTail} and {@link #find find} methods.  The
     * following code, for example, writes <tt>one dog two dogs in the
     * yard</tt> to the standard-output stream: </p>
     *
     * <blockquote><pre>
     * Pattern p = Pattern.compile("cat");
     * Matcher m = p.matcher("one cat two cats in the yard");
     * StringBuffer sb = new StringBuffer();
     * while (m.find()) {
     *     m.appendReplacement(sb, "dog");
     * }
     * m.appendTail(sb);
     * System.out.println(sb.toString());</pre></blockquote>
     *
     * @param  sb
     *         The target string buffer
     *
     * @param  replacement
     *         The replacement string
     *
     * @return  This matcher
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     *
     * @throws  IllegalArgumentException
     *          If the replacement string refers to a named-capturing
     *          group that does not exist in the pattern
     *
     * @throws  IndexOutOfBoundsException
     *          If the replacement string refers to a capturing group
     *          that does not exist in the pattern
     */
    public Matcher appendReplacement(StringBuffer sb, String replacement) {

        sb.append(text.substring(appendPos, start()));
        appendEvaluated(sb, replacement);
        appendPos = end();

        return this;
    }

    /**
     * Appends a literal part of the input plus a replacement for the current
     * match to a given {@link StringBuilder}. The literal part is exactly the
     * part of the input between the previous match and the current match. The
     * method can be used in conjunction with {@link #find()} and
     * {@link #appendTail(StringBuilder)} to walk through the input and replace
     * all occurrences of the {@code Pattern} with something else.
     *
     * @param buffer
     *            the {@code StringBuilder} to append to.
     * @param replacement
     *            the replacement text.
     * @return the {@code Matcher} itself.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public Matcher appendReplacement(StringBuilder buffer, String replacement) {

        buffer.append(text.substring(appendPos, start()));
        appendEvaluated(buffer, replacement);
        appendPos = end();

        return this;
    }

    /**
     * Internal helper method to append a given string to a given string buffer.
     * If the string contains any references to groups, these are replaced by
     * the corresponding group's contents.
     *
     * @param buffer the string buffer.
     * @param s the string to append.
     */
    private void appendEvaluated(StringBuffer buffer, String s) {
        boolean escape = false;
        boolean dollar = false;
        boolean escapeNamedGroup = false;
        int escapeNamedGroupStart = -1;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && !escape) {
                escape = true;
            } else if (c == '$' && !escape) {
                dollar = true;
            } else if (c >= '0' && c <= '9' && dollar && !escapeNamedGroup) {
                String groupValue = group(c - '0');
                if (groupValue != null) {
                    buffer.append(groupValue);
                }
                dollar = false;
            } else if (c == '{' && dollar) {
                escapeNamedGroup = true;
                escapeNamedGroupStart = i;
            } else if (c == '}' && dollar && escapeNamedGroup) {
                String groupValue = group(s.substring(escapeNamedGroupStart + 1, i));
                if (groupValue != null) {
                    buffer.append(groupValue);
                }
                dollar = false;
                escapeNamedGroup = false;
            } else if (c != '}' && dollar && escapeNamedGroup) {
                continue;
            } else {
                buffer.append(c);
                dollar = false;
                escape = false;
                escapeNamedGroup = false;
            }
        }

        if (escape) {
            throw new IllegalArgumentException("character to be escaped is missing");
        }

        if (dollar) {
            throw new IllegalArgumentException("Illegal group reference: group index is missing");
        }

        if (escapeNamedGroup) {
            throw new IllegalArgumentException("Missing ending brace '}' from replacement string");
        }
    }

    /**
     * Internal helper method to append a given string to a given string builder.
     * If the string contains any references to groups, these are replaced by
     * the corresponding group's contents.
     *
     * @param buffer
     *            the string builder.
     * @param s
     *            the string to append.
     */
    private void appendEvaluated(StringBuilder buffer, String s) {
        boolean escape = false;
        boolean dollar = false;
        boolean escapeNamedGroup = false;
        int escapeNamedGroupStart = -1;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && !escape) {
                escape = true;
            } else if (c == '$' && !escape) {
                dollar = true;
            } else if (c >= '0' && c <= '9' && dollar && !escapeNamedGroup) {
                String groupValue = group(c - '0');
                if (groupValue != null) {
                    buffer.append(groupValue);
                }
                dollar = false;
            } else if (c == '{' && dollar) {
                escapeNamedGroup = true;
                escapeNamedGroupStart = i;
            } else if (c == '}' && dollar && escapeNamedGroup) {
                String groupValue = group(s.substring(escapeNamedGroupStart + 1, i));
                if (groupValue != null) {
                    buffer.append(groupValue);
                }
                dollar = false;
                escapeNamedGroup = false;
            } else if (c != '}' && dollar && escapeNamedGroup) {
                continue;
            } else {
                buffer.append(c);
                dollar = false;
                escape = false;
                escapeNamedGroup = false;
            }
        }

        if (escape) {
            throw new IllegalArgumentException("character to be escaped is missing");
        }

        if (dollar) {
            throw new IllegalArgumentException("Illegal group reference: group index is missing");
        }

        if (escapeNamedGroup) {
            throw new IllegalArgumentException("Missing ending brace '}' from replacement string");
        }
    }

    /**
     * Implements a terminal append-and-replace step.
     *
     * <p> This method reads characters from the input sequence, starting at
     * the append position, and appends them to the given string buffer.  It is
     * intended to be invoked after one or more invocations of the {@link
     * #appendReplacement appendReplacement} method in order to copy the
     * remainder of the input sequence.  </p>
     *
     * @param  sb
     *         The target string buffer
     *
     * @return  The target string buffer
     */
    public StringBuffer appendTail(StringBuffer sb) {
        if (appendPos < to) {
            sb.append(text.substring(appendPos, to));
        }
        return sb;
    }

    /**
     * Appends the (unmatched) remainder of the input to the given
     * {@link StringBuilder}. The method can be used in conjunction with
     * {@link #find()} and {@link #appendReplacement(StringBuilder, String)} to
     * walk through the input and replace all matches of the {@code Pattern}
     * with something else.
     *
     * @return the {@code StringBuilder}.
     * @throws IllegalStateException
     *             if no successful match has been made.
     */
    public StringBuilder appendTail(StringBuilder buffer) {
        if (appendPos < to) {
            buffer.append(text.substring(appendPos, to));
        }
        return buffer;
    }


    /**
     * Replaces every subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     *
     * <p> This method first resets this matcher.  It then scans the input
     * sequence looking for matches of the pattern.  Characters that are not
     * part of any match are appended directly to the result string; each match
     * is replaced in the result by the replacement string.  The replacement
     * string may contain references to captured subsequences as in the {@link
     * #appendReplacement appendReplacement} method.
     *
     * <p> Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     *
     * <p> Given the regular expression <tt>a*b</tt>, the input
     * <tt>"aabfooaabfooabfoob"</tt>, and the replacement string
     * <tt>"-"</tt>, an invocation of this method on a matcher for that
     * expression would yield the string <tt>"-foo-foo-foo-"</tt>.
     *
     * <p> Invoking this method changes this matcher's state.  If the matcher
     * is to be used in further matching operations then it should first be
     * reset.  </p>
     *
     * @param  replacement
     *         The replacement string
     *
     * @return  The string constructed by replacing each matching subsequence
     *          by the replacement string, substituting captured subsequences
     *          as needed
     */
    public String replaceAll(String replacement) {
        reset();
        boolean result = find();
        if (result) {
            StringBuffer sb = new StringBuffer();
            do {
                appendReplacement(sb, replacement);
                result = find();
            } while (result);
            appendTail(sb);
            return sb.toString();
        }
        return text.toString();
    }

    /**
     * Replaces the first subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     *
     * <p> This method first resets this matcher.  It then scans the input
     * sequence looking for a match of the pattern.  Characters that are not
     * part of the match are appended directly to the result string; the match
     * is replaced in the result by the replacement string.  The replacement
     * string may contain references to captured subsequences as in the {@link
     * #appendReplacement appendReplacement} method.
     *
     * <p>Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     *
     * <p> Given the regular expression <tt>dog</tt>, the input
     * <tt>"zzzdogzzzdogzzz"</tt>, and the replacement string
     * <tt>"cat"</tt>, an invocation of this method on a matcher for that
     * expression would yield the string <tt>"zzzcatzzzdogzzz"</tt>.  </p>
     *
     * <p> Invoking this method changes this matcher's state.  If the matcher
     * is to be used in further matching operations then it should first be
     * reset.  </p>
     *
     * @param  replacement
     *         The replacement string
     * @return  The string constructed by replacing the first matching
     *          subsequence by the replacement string, substituting captured
     *          subsequences as needed
     */
    public String replaceFirst(String replacement) {
        if (replacement == null)
            throw new NullPointerException("replacement");
        reset();
        if (!find())
            return text.toString();
        StringBuffer sb = new StringBuffer();
        appendReplacement(sb, replacement);
        appendTail(sb);
        return sb.toString();
    }

    /**
     * Sets the limits of this matcher's region. The region is the part of the
     * input sequence that will be searched to find a match. Invoking this
     * method resets the matcher, and then sets the region to start at the
     * index specified by the <code>start</code> parameter and end at the
     * index specified by the <code>end</code> parameter.
     *
     * <p>Depending on the transparency and anchoring being used (see
     * {@link #useTransparentBounds useTransparentBounds} and
     * {@link #useAnchoringBounds useAnchoringBounds}), certain constructs such
     * as anchors may behave differently at or around the boundaries of the
     * region.
     *
     * @param  start
     *         The index to start searching at (inclusive)
     * @param  end
     *         The index to end searching at (exclusive)
     * @throws  IndexOutOfBoundsException
     *          If start or end is less than zero, if
     *          start is greater than the length of the input sequence, if
     *          end is greater than the length of the input sequence, or if
     *          start is greater than end.
     * @return  this matcher
     * @since 1.5
     */
    public Matcher region(int start, int end) {
        return reset(originalInput, start, end);
    }

    /**
     * Reports the start index of this matcher's region. The
     * searches this matcher conducts are limited to finding matches
     * within {@link #regionStart regionStart} (inclusive) and
     * {@link #regionEnd regionEnd} (exclusive).
     *
     * @return  The starting point of this matcher's region
     * @since 1.5
     */
    public int regionStart() {
        return from;
    }

    /**
     * Reports the end index (exclusive) of this matcher's region.
     * The searches this matcher conducts are limited to finding matches
     * within {@link #regionStart regionStart} (inclusive) and
     * {@link #regionEnd regionEnd} (exclusive).
     *
     * @return  the ending point of this matcher's region
     * @since 1.5
     */
    public int regionEnd() {
        return to;
    }

    /**
     * Queries the transparency of region bounds for this matcher.
     *
     * <p> This method returns <tt>true</tt> if this matcher uses
     * <i>transparent</i> bounds, <tt>false</tt> if it uses <i>opaque</i>
     * bounds.
     *
     * <p> See {@link #useTransparentBounds useTransparentBounds} for a
     * description of transparent and opaque bounds.
     *
     * <p> By default, a matcher uses opaque region boundaries.
     *
     * @return <tt>true</tt> iff this matcher is using transparent bounds,
     *         <tt>false</tt> otherwise.
     * @see java.util.regex.Matcher#useTransparentBounds(boolean)
     * @since 1.5
     */
    public boolean hasTransparentBounds() {
        return transparentBounds;
    }

    /**
     * Sets the transparency of region bounds for this matcher.
     *
     * <p> Invoking this method with an argument of <tt>true</tt> will set this
     * matcher to use <i>transparent</i> bounds. If the boolean
     * argument is <tt>false</tt>, then <i>opaque</i> bounds will be used.
     *
     * <p> Using transparent bounds, the boundaries of this
     * matcher's region are transparent to lookahead, lookbehind,
     * and boundary matching constructs. Those constructs can see beyond the
     * boundaries of the region to see if a match is appropriate.
     *
     * <p> Using opaque bounds, the boundaries of this matcher's
     * region are opaque to lookahead, lookbehind, and boundary matching
     * constructs that may try to see beyond them. Those constructs cannot
     * look past the boundaries so they will fail to match anything outside
     * of the region.
     *
     * <p> By default, a matcher uses opaque bounds.
     *
     * @param  b a boolean indicating whether to use opaque or transparent
     *         regions
     * @return this matcher
     * @see java.util.regex.Matcher#hasTransparentBounds
     * @since 1.5
     */
    public Matcher useTransparentBounds(boolean b) {
        /* J2ObjC modified: platform-specific implementation.
        synchronized (this) {
            transparentBounds = b;
            nativeMatcher.useTransparentBounds(b);
        }
        */
        transparentBounds = b;
        useTransparentBoundsImpl(address, b);
        return this;
    }

    /**
     * Queries the anchoring of region bounds for this matcher.
     *
     * <p> This method returns <tt>true</tt> if this matcher uses
     * <i>anchoring</i> bounds, <tt>false</tt> otherwise.
     *
     * <p> See {@link #useAnchoringBounds useAnchoringBounds} for a
     * description of anchoring bounds.
     *
     * <p> By default, a matcher uses anchoring region boundaries.
     *
     * @return <tt>true</tt> iff this matcher is using anchoring bounds,
     *         <tt>false</tt> otherwise.
     * @see java.util.regex.Matcher#useAnchoringBounds(boolean)
     * @since 1.5
     */
    public boolean hasAnchoringBounds() {
        return anchoringBounds;
    }

    /**
     * Sets the anchoring of region bounds for this matcher.
     *
     * <p> Invoking this method with an argument of <tt>true</tt> will set this
     * matcher to use <i>anchoring</i> bounds. If the boolean
     * argument is <tt>false</tt>, then <i>non-anchoring</i> bounds will be
     * used.
     *
     * <p> Using anchoring bounds, the boundaries of this
     * matcher's region match anchors such as ^ and $.
     *
     * <p> Without anchoring bounds, the boundaries of this
     * matcher's region will not match anchors such as ^ and $.
     *
     * <p> By default, a matcher uses anchoring region boundaries.
     *
     * @param  b a boolean indicating whether or not to use anchoring bounds.
     * @return this matcher
     * @see java.util.regex.Matcher#hasAnchoringBounds
     * @since 1.5
     */
    public Matcher useAnchoringBounds(boolean b) {
        /* J2ObjC modified: platform-specific implementation.
        synchronized (this) {
            anchoringBounds = b;
            nativeMatcher.useAnchoringBounds(b);
        }
        */
        anchoringBounds = b;
        useAnchoringBoundsImpl(address, b);
        return this;
    }

    /**
     * <p>Returns the string representation of this matcher. The
     * string representation of a <code>Matcher</code> contains information
     * that may be useful for debugging. The exact format is unspecified.
     *
     * @return  The string representation of this matcher
     * @since 1.5
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("java.util.regex.Matcher");
        sb.append("[pattern=" + pattern());
        sb.append(" region=");
        sb.append(regionStart() + "," + regionEnd());
        sb.append(" lastmatch=");
        if (matchFound && (group() != null)) {
            sb.append(group());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * <p>Returns true if the end of input was hit by the search engine in
     * the last match operation performed by this matcher.
     *
     * <p>When this method returns true, then it is possible that more input
     * would have changed the result of the last search.
     *
     * @return  true iff the end of input was hit in the last match; false
     *          otherwise
     * @since 1.5
     */
    public boolean hitEnd() {
        /* J2ObjC modified: platform-specific implementation.
        synchronized (this) {
            return nativeMatcher.hitEnd();
        }
        */
        return hitEndImpl(address);
    }

    /**
     * <p>Returns true if more input could change a positive match into a
     * negative one.
     *
     * <p>If this method returns true, and a match was found, then more
     * input could cause the match to be lost. If this method returns false
     * and a match was found, then more input might change the match but the
     * match won't be lost. If a match was not found, then requireEnd has no
     * meaning.
     *
     * @return  true iff more input could change a positive match into a
     *          negative one.
     * @since 1.5
     */
    public boolean requireEnd() {
        /* J2ObjC modified: platform-specific implementation.
        synchronized (this) {
            return nativeMatcher.requireEnd();
        }
        */
        return requireEndImpl(address);
    }

    /**
     * Returns the end index of the text.
     *
     * @return the index after the last character in the text
     */
    int getTextLength() {
        return text.length();
    }

    /**
     * Generates a String from this Matcher's input in the specified range.
     *
     * @param  beginIndex   the beginning index, inclusive
     * @param  endIndex     the ending index, exclusive
     * @return A String generated from this Matcher's input
     */
    CharSequence getSubSequence(int beginIndex, int endIndex) {
        return text.subSequence(beginIndex, endIndex);
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
            throw new IllegalArgumentException("input == null");
        }

        if (start < 0 || end < 0 || start > input.length() || end > input.length() || start > end) {
            throw new IndexOutOfBoundsException();
        }

        this.originalInput = input;
        /* J2ObjC added: platform-specific implementation. */
        this.inputChars = input.toString().toCharArray();
        this.text = input.toString();
        this.from = start;
        this.to = end;
        resetForInput();

        matchFound = false;
        appendPos = 0;

        return this;
    }

    private void resetForInput() {
        /* J2ObjC modified: platform-specific implementation.
        synchronized (this) {
            nativeMatcher.setInput(text, from, to);
            nativeMatcher.useAnchoringBounds(anchoringBounds);
            nativeMatcher.useTransparentBounds(transparentBounds);
        }
        */
        setInputImpl(address, inputChars, from, to, anchoringBounds, transparentBounds);
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

    private int getMatchedGroupIndex(String name) {
        ensureMatch();
        /* J2ObjC modified: platform-specific implementation.
        int result = nativeMatcher.getMatchedGroupIndex(name);
        */
        int result = getMatchedGroupIndexImpl(address, name);
        if (result < 0) {
            throw new IllegalArgumentException("No capturing group in the pattern " +
                    "with the name " + name);
        }
        return result;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            closeImpl(address);
        } finally {
            super.finalize();
        }
    }

    static {
        initICU();
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
    private static native int getMatchedGroupIndexImpl(long addr, String name);
    private static native void initICU();


    /**
     * A trivial match result implementation that's based on an array of integers
     * representing match offsets. The array is of the form
     * {@code { start1, end1, start2, end2 ....}) where each consecutive pair of elements represents
     * the start and end of a match respectively.
     */
    static final class OffsetBasedMatchResult implements MatchResult {
        private final String input;
        private final int[] offsets;

        OffsetBasedMatchResult(String input, int[] offsets) {
            this.input = input;
            this.offsets = offsets.clone();
        }

        @Override
        public int start() {
            return start(0);
        }

        @Override
        public int start(int group) {
            return offsets[2 * group];
        }

        @Override
        public int end() {
            return end(0);
        }

        @Override
        public int end(int group) {
            return offsets[2 * group + 1];
        }

        @Override
        public String group() {
            return group(0);
        }

        @Override
        public String group(int group) {
            final int start = start(group);
            final int end = end(group);
            if (start == -1 || end == -1) {
                return null;
            }

            return input.substring(start, end);
        }

        @Override
        public int groupCount() {
            return (offsets.length / 2) - 1;
        }
    }
}
