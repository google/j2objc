/*
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
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

/*
 *
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 2002 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */


package java.text;

/**
 * <p>A subclass of BreakIterator whose behavior is specified using a list of rules.</p>
 *
 * <p>There are two kinds of rules, which are separated by semicolons: <i>substitutions</i>
 * and <i>regular expressions.</i></p>
 *
 * <p>A substitution rule defines a name that can be used in place of an expression. It
 * consists of a name, which is a string of characters contained in angle brackets, an equals
 * sign, and an expression. (There can be no whitespace on either side of the equals sign.)
 * To keep its syntactic meaning intact, the expression must be enclosed in parentheses or
 * square brackets. A substitution is visible after its definition, and is filled in using
 * simple textual substitution. Substitution definitions can contain other substitutions, as
 * long as those substitutions have been defined first. Substitutions are generally used to
 * make the regular expressions (which can get quite complex) shorted and easier to read.
 * They typically define either character categories or commonly-used subexpressions.</p>
 *
 * <p>There is one special substitution.&nbsp; If the description defines a substitution
 * called &quot;&lt;ignore&gt;&quot;, the expression must be a [] expression, and the
 * expression defines a set of characters (the &quot;<em>ignore characters</em>&quot;) that
 * will be transparent to the BreakIterator.&nbsp; A sequence of characters will break the
 * same way it would if any ignore characters it contains are taken out.&nbsp; Break
 * positions never occur befoer ignore characters.</p>
 *
 * <p>A regular expression uses a subset of the normal Unix regular-expression syntax, and
 * defines a sequence of characters to be kept together. With one significant exception, the
 * iterator uses a longest-possible-match algorithm when matching text to regular
 * expressions. The iterator also treats descriptions containing multiple regular expressions
 * as if they were ORed together (i.e., as if they were separated by |).</p>
 *
 * <p>The special characters recognized by the regular-expression parser are as follows:</p>
 *
 * <blockquote>
 * <table border="1" width="100%">
 * <tr>
 * <td width="6%">*</td>
 * <td width="94%">Specifies that the expression preceding the asterisk may occur any number
 * of times (including not at all).</td>
 * </tr>
 * <tr>
 * <td width="6%">{}</td>
 * <td width="94%">Encloses a sequence of characters that is optional.</td>
 * </tr>
 * <tr>
 * <td width="6%">()</td>
 * <td width="94%">Encloses a sequence of characters.&nbsp; If followed by *, the sequence
 * repeats.&nbsp; Otherwise, the parentheses are just a grouping device and a way to delimit
 * the ends of expressions containing |.</td>
 * </tr>
 * <tr>
 * <td width="6%">|</td>
 * <td width="94%">Separates two alternative sequences of characters.&nbsp; Either one
 * sequence or the other, but not both, matches this expression.&nbsp; The | character can
 * only occur inside ().</td>
 * </tr>
 * <tr>
 * <td width="6%">.</td>
 * <td width="94%">Matches any character.</td>
 * </tr>
 * <tr>
 * <td width="6%">*?</td>
 * <td width="94%">Specifies a non-greedy asterisk.&nbsp; *? works the same way as *, except
 * when there is overlap between the last group of characters in the expression preceding the
 * * and the first group of characters following the *.&nbsp; When there is this kind of
 * overlap, * will match the longest sequence of characters that match the expression before
 * the *, and *? will match the shortest sequence of characters matching the expression
 * before the *?.&nbsp; For example, if you have &quot;xxyxyyyxyxyxxyxyxyy&quot; in the text,
 * &quot;x[xy]*x&quot; will match through to the last x (i.e., &quot;<strong>xxyxyyyxyxyxxyxyx</strong>yy&quot;,
 * but &quot;x[xy]*?x&quot; will only match the first two xes (&quot;<strong>xx</strong>yxyyyxyxyxxyxyxyy&quot;).</td>
 * </tr>
 * <tr>
 * <td width="6%">[]</td>
 * <td width="94%">Specifies a group of alternative characters.&nbsp; A [] expression will
 * match any single character that is specified in the [] expression.&nbsp; For more on the
 * syntax of [] expressions, see below.</td>
 * </tr>
 * <tr>
 * <td width="6%">/</td>
 * <td width="94%">Specifies where the break position should go if text matches this
 * expression.&nbsp; (e.g., &quot;[a-z]&#42;/[:Zs:]*[1-0]&quot; will match if the iterator sees a
 * run
 * of letters, followed by a run of whitespace, followed by a digit, but the break position
 * will actually go before the whitespace).&nbsp; Expressions that don't contain / put the
 * break position at the end of the matching text.</td>
 * </tr>
 * <tr>
 * <td width="6%">\</td>
 * <td width="94%">Escape character.&nbsp; The \ itself is ignored, but causes the next
 * character to be treated as literal character.&nbsp; This has no effect for many
 * characters, but for the characters listed above, this deprives them of their special
 * meaning.&nbsp; (There are no special escape sequences for Unicode characters, or tabs and
 * newlines; these are all handled by a higher-level protocol.&nbsp; In a Java string,
 * &quot;\n&quot; will be converted to a literal newline character by the time the
 * regular-expression parser sees it.&nbsp; Of course, this means that \ sequences that are
 * visible to the regexp parser must be written as \\ when inside a Java string.)&nbsp; All
 * characters in the ASCII range except for letters, digits, and control characters are
 * reserved characters to the parser and must be preceded by \ even if they currently don't
 * mean anything.</td>
 * </tr>
 * <tr>
 * <td width="6%">!</td>
 * <td width="94%">If ! appears at the beginning of a regular expression, it tells the regexp
 * parser that this expression specifies the backwards-iteration behavior of the iterator,
 * and not its normal iteration behavior.&nbsp; This is generally only used in situations
 * where the automatically-generated backwards-iteration brhavior doesn't produce
 * satisfactory results and must be supplemented with extra client-specified rules.</td>
 * </tr>
 * <tr>
 * <td width="6%"><em>(all others)</em></td>
 * <td width="94%">All other characters are treated as literal characters, which must match
 * the corresponding character(s) in the text exactly.</td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * <p>Within a [] expression, a number of other special characters can be used to specify
 * groups of characters:</p>
 *
 * <blockquote>
 * <table border="1" width="100%">
 * <tr>
 * <td width="6%">-</td>
 * <td width="94%">Specifies a range of matching characters.&nbsp; For example
 * &quot;[a-p]&quot; matches all lowercase Latin letters from a to p (inclusive).&nbsp; The -
 * sign specifies ranges of continuous Unicode numeric values, not ranges of characters in a
 * language's alphabetical order: &quot;[a-z]&quot; doesn't include capital letters, nor does
 * it include accented letters such as a-umlaut.</td>
 * </tr>
 * <tr>
 * <td width="6%">::</td>
 * <td width="94%">A pair of colons containing a one- or two-letter code matches all
 * characters in the corresponding Unicode category.&nbsp; The two-letter codes are the same
 * as the two-letter codes in the Unicode database (for example, &quot;[:Sc::Sm:]&quot;
 * matches all currency symbols and all math symbols).&nbsp; Specifying a one-letter code is
 * the same as specifying all two-letter codes that begin with that letter (for example,
 * &quot;[:L:]&quot; matches all letters, and is equivalent to
 * &quot;[:Lu::Ll::Lo::Lm::Lt:]&quot;).&nbsp; Anything other than a valid two-letter Unicode
 * category code or a single letter that begins a Unicode category code is illegal within
 * colons.</td>
 * </tr>
 * <tr>
 * <td width="6%">[]</td>
 * <td width="94%">[] expressions can nest.&nbsp; This has no effect, except when used in
 * conjunction with the ^ token.</td>
 * </tr>
 * <tr>
 * <td width="6%">^</td>
 * <td width="94%">Excludes the character (or the characters in the [] expression) following
 * it from the group of characters.&nbsp; For example, &quot;[a-z^p]&quot; matches all Latin
 * lowercase letters except p.&nbsp; &quot;[:L:^[&#92;u4e00-&#92;u9fff]]&quot; matches all letters
 * except the Han ideographs.</td>
 * </tr>
 * <tr>
 * <td width="6%"><em>(all others)</em></td>
 * <td width="94%">All other characters are treated as literal characters.&nbsp; (For
 * example, &quot;[aeiou]&quot; specifies just the letters a, e, i, o, and u.)</td>
 * </tr>
 * </table>
 * </blockquote>
 *
 * <p>For a more complete explanation, see <a
 * href="http://www.ibm.com/java/education/boundaries/boundaries.html">http://www.ibm.com/java/education/boundaries/boundaries.html</a>.
 * &nbsp; For examples, see the resource data (which is annotated).</p>
 *
 * @author Richard Gillam
 */
class IcuIteratorWrapper extends BreakIterator {

    /* The wrapped ICU implementation. Non-final for #clone() */
    private android.icu.text.BreakIterator wrapped;

    /**
     * Constructs a IcuIteratorWrapper according to the datafile
     * provided.
     */
    IcuIteratorWrapper(android.icu.text.BreakIterator iterator) {
        wrapped = iterator;
    }

    /**
     * Clones this iterator.
     *
     * @return A newly-constructed IcuIteratorWrapper with the same
     * behavior as this one.
     */
    public Object clone() {
        IcuIteratorWrapper result = (IcuIteratorWrapper) super.clone();
        result.wrapped = (android.icu.text.BreakIterator) wrapped.clone();
        return result;
    }

    /**
     * Returns true if both BreakIterators are of the same class, have the same
     * rules, and iterate over the same text.
     */
    public boolean equals(Object that) {
        if (!(that instanceof IcuIteratorWrapper)) {
            return false;
        }
        return wrapped.equals(((IcuIteratorWrapper) that).wrapped);
    }

    //=======================================================================
    // BreakIterator overrides
    //=======================================================================

    /**
     * Returns text
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * Compute a hashcode for this BreakIterator
     *
     * @return A hash code
     */
    public int hashCode() {
        return wrapped.hashCode();
    }

    /**
     * Sets the current iteration position to the beginning of the text.
     * (i.e., the CharacterIterator's starting offset).
     *
     * @return The offset of the beginning of the text.
     */
    public int first() {
        return wrapped.first();
    }

    /**
     * Sets the current iteration position to the end of the text.
     * (i.e., the CharacterIterator's ending offset).
     *
     * @return The text's past-the-end offset.
     */
    public int last() {
        return wrapped.last();
    }

    /**
     * Advances the iterator either forward or backward the specified number of steps.
     * Negative values move backward, and positive values move forward.  This is
     * equivalent to repeatedly calling next() or previous().
     *
     * @param n The number of steps to move.  The sign indicates the direction
     *          (negative is backwards, and positive is forwards).
     * @return The character offset of the boundary position n boundaries away from
     * the current one.
     */
    public int next(int n) {
        return wrapped.next(n);
    }

    /**
     * Advances the iterator to the next boundary position.
     *
     * @return The position of the first boundary after this one.
     */
    public int next() {
        return wrapped.next();
    }

    /**
     * Advances the iterator backwards, to the last boundary preceding this one.
     *
     * @return The position of the last boundary position preceding this one.
     */
    public int previous() {
        return wrapped.previous();
    }

    /**
     * Throw IllegalArgumentException unless begin <= offset < end.
     */
    protected static final void checkOffset(int offset, CharacterIterator text) {
        if (offset < text.getBeginIndex() || offset > text.getEndIndex()) {
            throw new IllegalArgumentException("offset out of bounds");
        }
    }

    /**
     * Sets the iterator to refer to the first boundary position following
     * the specified position.
     *
     * @return The position of the first break after the current position.
     * @offset The position from which to begin searching for a break position.
     */
    public int following(int offset) {
        CharacterIterator text = getText();
        checkOffset(offset, text);
        return wrapped.following(offset);
    }

    /**
     * Sets the iterator to refer to the last boundary position before the
     * specified position.
     *
     * @return The position of the last boundary before the starting position.
     * @offset The position to begin searching for a break from.
     */
    public int preceding(int offset) {
        // if we start by updating the current iteration position to the
        // position specified by the caller, we can just use previous()
        // to carry out this operation
        CharacterIterator text = getText();
        checkOffset(offset, text);
        return wrapped.preceding(offset);
    }

    /**
     * Returns true if the specfied position is a boundary position.  As a side
     * effect, leaves the iterator pointing to the first boundary position at
     * or after "offset".
     *
     * @param offset the offset to check.
     * @return True if "offset" is a boundary position.
     */
    public boolean isBoundary(int offset) {
        CharacterIterator text = getText();
        checkOffset(offset, text);
        return wrapped.isBoundary(offset);
    }

    /**
     * Returns the current iteration position.
     *
     * @return The current iteration position.
     */
    public int current() {
        return wrapped.current();
    }

    /**
     * Return a CharacterIterator over the text being analyzed.  This version
     * of this method returns the actual CharacterIterator we're using internally.
     * Changing the state of this iterator can have undefined consequences.  If
     * you need to change it, clone it first.
     *
     * @return An iterator over the text being analyzed.
     */
    public CharacterIterator getText() {
        return wrapped.getText();
    }

    public void setText(String newText) {
        wrapped.setText(newText);
    }

    /**
     * Set the iterator to analyze a new piece of text.  This function resets
     * the current iteration position to the beginning of the text.
     *
     * @param newText An iterator over the text to analyze.
     */
    public void setText(CharacterIterator newText) {
        newText.current();
        wrapped.setText(newText);
    }
}
