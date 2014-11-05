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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Patterns are compiled regular expressions. In many cases, convenience methods such as
 * {@link String#matches String.matches}, {@link String#replaceAll String.replaceAll} and
 * {@link String#split String.split} will be preferable, but if you need to do a lot of work
 * with the same regular expression, it may be more efficient to compile it once and reuse it.
 * The {@code Pattern} class and its companion, {@link Matcher}, also offer more functionality
 * than the small amount exposed by {@code String}.
 *
 * <pre>
 * // String convenience methods:
 * boolean sawFailures = s.matches("Failures: \\d+");
 * String farewell = s.replaceAll("Hello, (\\S+)", "Goodbye, $1");
 * String[] fields = s.split(":");
 *
 * // Direct use of Pattern:
 * Pattern p = Pattern.compile("Hello, (\\S+)");
 * Matcher m = p.matcher(inputString);
 * while (m.find()) { // Find each match in turn; String can't do this.
 *     String name = m.group(1); // Access a submatch group; String can't do this.
 * }
 * </pre>
 *
 * <h3>Regular expression syntax</h3>
 * <span class="datatable">
 * <style type="text/css">
 * .datatable td { padding-right: 20px; }
 * </style>
 *
 * <p>Java supports a subset of Perl 5 regular expression syntax. An important gotcha is that Java
 * has no regular expression literals, and uses plain old string literals instead. This means that
 * you need an extra level of escaping. For example, the regular expression {@code \s+} has to
 * be represented as the string {@code "\\s+"}.
 *
 * <h3>Escape sequences</h3>
 * <p><table>
 * <tr> <td> \ </td> <td>Quote the following metacharacter (so {@code \.} matches a literal {@code .}).</td> </tr>
 * <tr> <td> \Q </td> <td>Quote all following metacharacters until {@code \E}.</td> </tr>
 * <tr> <td> \E </td> <td>Stop quoting metacharacters (started by {@code \Q}).</td> </tr>
 * <tr> <td> \\ </td> <td>A literal backslash.</td> </tr>
 * <tr> <td> &#x005c;u<i>hhhh</i> </td> <td>The Unicode character U+hhhh (in hex).</td> </tr>
 * <tr> <td> &#x005c;x<i>hh</i> </td> <td>The Unicode character U+00hh (in hex).</td> </tr>
 * <tr> <td> \c<i>x</i> </td> <td>The ASCII control character ^x (so {@code \cH} would be ^H, U+0008).</td> </tr>
 *
 * <tr> <td> \a </td> <td>The ASCII bell character (U+0007).</td> </tr>
 * <tr> <td> \e </td> <td>The ASCII ESC character (U+001b).</td> </tr>
 * <tr> <td> \f </td> <td>The ASCII form feed character (U+000c).</td> </tr>
 * <tr> <td> \n </td> <td>The ASCII newline character (U+000a).</td> </tr>
 * <tr> <td> \r </td> <td>The ASCII carriage return character (U+000d).</td> </tr>
 * <tr> <td> \t </td> <td>The ASCII tab character (U+0009).</td> </tr>
 * </table>
 *
 * <h3>Character classes</h3>
 * <p>It's possible to construct arbitrary character classes using set operations:
 * <table>
 * <tr> <td> [abc] </td> <td>Any one of {@code a}, {@code b}, or {@code c}. (Enumeration.)</td> </tr>
 * <tr> <td> [a-c] </td> <td>Any one of {@code a}, {@code b}, or {@code c}. (Range.)</td> </tr>
 * <tr> <td> [^abc] </td> <td>Any character <i>except</i> {@code a}, {@code b}, or {@code c}. (Negation.)</td> </tr>
 * <tr> <td> [[a-f][0-9]] </td> <td>Any character in either range. (Union.)</td> </tr>
 * <tr> <td> [[a-z]&&[jkl]] </td> <td>Any character in both ranges. (Intersection.)</td> </tr>
 * </table>
 * <p>Most of the time, the built-in character classes are more useful:
 * <table>
 * <tr> <td> \d </td> <td>Any digit character (see note below).</td> </tr>
 * <tr> <td> \D </td> <td>Any non-digit character (see note below).</td> </tr>
 * <tr> <td> \s </td> <td>Any whitespace character (see note below).</td> </tr>
 * <tr> <td> \S </td> <td>Any non-whitespace character (see note below).</td> </tr>
 * <tr> <td> \w </td> <td>Any word character (see note below).</td> </tr>
 * <tr> <td> \W </td> <td>Any non-word character (see note below).</td> </tr>
 * <tr> <td> \p{<i>NAME</i>} </td> <td> Any character in the class with the given <i>NAME</i>. </td> </tr>
 * <tr> <td> \P{<i>NAME</i>} </td> <td> Any character <i>not</i> in the named class. </td> </tr>
 * </table>
 * <p>Note that these built-in classes don't just cover the traditional ASCII range. For example,
 * <code>\w</code> is equivalent to the character class <code>[\p{Ll}\p{Lu}\p{Lt}\p{Lo}\p{Nd}]</code>.
 * For more details see <a href="http://www.unicode.org/reports/tr18/#Compatibility_Properties">Unicode TR-18</a>,
 * and bear in mind that the set of characters in each class can vary between Unicode releases.
 * If you actually want to match only ASCII characters, specify the explicit characters you want;
 * if you mean 0-9 use <code>[0-9]</code> rather than <code>\d</code>, which would also include
 * Gurmukhi digits and so forth.
 * <p>There are also a variety of named classes:
 * <ul>
 * <li><a href="../../lang/Character.html#unicode_categories">Unicode category names</a>,
 * prefixed by {@code Is}. For example {@code \p{IsLu}} for all uppercase letters.
 * <li>POSIX class names. These are 'Alnum', 'Alpha', 'ASCII', 'Blank', 'Cntrl', 'Digit',
 * 'Graph', 'Lower', 'Print', 'Punct', 'Upper', 'XDigit'.
 * <li>Unicode block names, as accepted as input to {@link java.lang.Character.UnicodeBlock#forName},
 * prefixed by {@code In}. For example {@code \p{InHebrew}} for all characters in the Hebrew block.
 * <li>Character method names. These are all non-deprecated methods from {@link java.lang.Character}
 * whose name starts with {@code is}, but with the {@code is} replaced by {@code java}.
 * For example, {@code \p{javaLowerCase}}.
 * </ul>
 *
 * <h3>Quantifiers</h3>
 * <p>Quantifiers match some number of instances of the preceding regular expression.
 * <table>
 * <tr> <td> * </td> <td>Zero or more.</td> </tr>
 * <tr> <td> ? </td> <td>Zero or one.</td> </tr>
 * <tr> <td> + </td> <td>One or more.</td> </tr>
 * <tr> <td> {<i>n</i>} </td> <td>Exactly <i>n</i>.</td> </tr>
 * <tr> <td> {<i>n,</i>} </td> <td>At least <i>n</i>.</td> </tr>
 * <tr> <td> {<i>n</i>,<i>m</i>} </td> <td>At least <i>n</i> but not more than <i>m</i>.</td> </tr>
 * </table>
 * <p>Quantifiers are "greedy" by default, meaning that they will match the longest possible input
 * sequence. There are also non-greedy quantifiers that match the shortest possible input sequence.
 * They're same as the greedy ones but with a trailing {@code ?}:
 * <table>
 * <tr> <td> *? </td> <td>Zero or more (non-greedy).</td> </tr>
 * <tr> <td> ?? </td> <td>Zero or one (non-greedy).</td> </tr>
 * <tr> <td> +? </td> <td>One or more (non-greedy).</td> </tr>
 * <tr> <td> {<i>n</i>}? </td> <td>Exactly <i>n</i> (non-greedy).</td> </tr>
 * <tr> <td> {<i>n,</i>}? </td> <td>At least <i>n</i> (non-greedy).</td> </tr>
 * <tr> <td> {<i>n</i>,<i>m</i>}? </td> <td>At least <i>n</i> but not more than <i>m</i> (non-greedy).</td> </tr>
 * </table>
 * <p>Quantifiers allow backtracking by default. There are also possessive quantifiers to prevent
 * backtracking. They're same as the greedy ones but with a trailing {@code +}:
 * <table>
 * <tr> <td> *+ </td> <td>Zero or more (possessive).</td> </tr>
 * <tr> <td> ?+ </td> <td>Zero or one (possessive).</td> </tr>
 * <tr> <td> ++ </td> <td>One or more (possessive).</td> </tr>
 * <tr> <td> {<i>n</i>}+ </td> <td>Exactly <i>n</i> (possessive).</td> </tr>
 * <tr> <td> {<i>n,</i>}+ </td> <td>At least <i>n</i> (possessive).</td> </tr>
 * <tr> <td> {<i>n</i>,<i>m</i>}+ </td> <td>At least <i>n</i> but not more than <i>m</i> (possessive).</td> </tr>
 * </table>
 *
 * <h3>Zero-width assertions</h3>
 * <p><table>
 * <tr> <td> ^ </td> <td>At beginning of line.</td> </tr>
 * <tr> <td> $ </td> <td>At end of line.</td> </tr>
 * <tr> <td> \A </td> <td>At beginning of input.</td> </tr>
 * <tr> <td> \b </td> <td>At word boundary.</td> </tr>
 * <tr> <td> \B </td> <td>At non-word boundary.</td> </tr>
 * <tr> <td> \G </td> <td>At end of previous match.</td> </tr>
 * <tr> <td> \z </td> <td>At end of input.</td> </tr>
 * <tr> <td> \Z </td> <td>At end of input, or before newline at end.</td> </tr>
 * </table>
 *
 * <h3>Look-around assertions</h3>
 * <p>Look-around assertions assert that the subpattern does (positive) or doesn't (negative) match
 * after (look-ahead) or before (look-behind) the current position, without including the matched
 * text in the containing match. The maximum length of possible matches for look-behind patterns
 * must not be unbounded.
 * <p><table>
 * <tr> <td> (?=<i>a</i>) </td> <td>Zero-width positive look-ahead.</td> </tr>
 * <tr> <td> (?!<i>a</i>) </td> <td>Zero-width negative look-ahead.</td> </tr>
 * <tr> <td> (?&lt;=<i>a</i>) </td> <td>Zero-width positive look-behind.</td> </tr>
 * <tr> <td> (?&lt;!<i>a</i>) </td> <td>Zero-width negative look-behind.</td> </tr>
 * </table>
 *
 * <h3>Groups</h3>
 *
 * <p><table>
 * <tr> <td> (<i>a</i>) </td> <td>A capturing group.</td> </tr>
 * <tr> <td> (?:<i>a</i>) </td> <td>A non-capturing group.</td> </tr>
 * <tr> <td> (?&gt;<i>a</i>) </td> <td>An independent non-capturing group. (The first match of the subgroup is the only match tried.)</td> </tr>
 * <tr> <td> \<i>n</i> </td> <td>The text already matched by capturing group <i>n</i>.</td> </tr>
 * </table>
 * <p>See {@link Matcher#group} for details of how capturing groups are numbered and accessed.
 *
 * <h3>Operators</h3>
 * <p><table>
 * <tr> <td> <i>ab</i> </td> <td>Expression <i>a</i> followed by expression <i>b</i>.</td> </tr>
 * <tr> <td> <i>a</i>|<i>b</i> </td> <td>Either expression <i>a</i> or expression <i>b</i>.</td> </tr>
 * </table>
 *
 * <a name="flags"><h3>Flags</h3></a>
 * <p><table>
 * <tr> <td> (?dimsux-dimsux:<i>a</i>) </td> <td>Evaluates the expression <i>a</i> with the given flags enabled/disabled.</td> </tr>
 * <tr> <td> (?dimsux-dimsux) </td> <td>Evaluates the rest of the pattern with the given flags enabled/disabled.</td> </tr>
 * </table>
 *
 * <p>The flags are:
 * <table>
 * <tr><td>{@code i}</td> <td>{@link #CASE_INSENSITIVE}</td> <td>case insensitive matching</td></tr>
 * <tr><td>{@code d}</td> <td>{@link #UNIX_LINES}</td>       <td>only accept {@code '\n'} as a line terminator</td></tr>
 * <tr><td>{@code m}</td> <td>{@link #MULTILINE}</td>        <td>allow {@code ^} and {@code $} to match beginning/end of any line</td></tr>
 * <tr><td>{@code s}</td> <td>{@link #DOTALL}</td>           <td>allow {@code .} to match {@code '\n'} ("s" for "single line")</td></tr>
 * <tr><td>{@code u}</td> <td>{@link #UNICODE_CASE}</td>     <td>enable Unicode case folding</td></tr>
 * <tr><td>{@code x}</td> <td>{@link #COMMENTS}</td>         <td>allow whitespace and comments</td></tr>
 * </table>
 * <p>Either set of flags may be empty. For example, {@code (?i-m)} would turn on case-insensitivity
 * and turn off multiline mode, {@code (?i)} would just turn on case-insensitivity,
 * and {@code (?-m)} would just turn off multiline mode.
 * <p>Note that on Android, {@code UNICODE_CASE} is always on: case-insensitive matching will
 * always be Unicode-aware.
 * <p>There are two other flags not settable via this mechanism: {@link #CANON_EQ} and
 * {@link #LITERAL}. Attempts to use {@link #CANON_EQ} on Android will throw an exception.
 * </span>
 *
 * <h3>Implementation notes</h3>
 *
 * <p>The regular expression implementation used in Android is provided by
 * <a href="http://www.icu-project.org">ICU</a>. The notation for the regular
 * expressions is mostly a superset of those used in other Java language
 * implementations. This means that existing applications will normally work as
 * expected, but in rare cases Android may accept a regular expression that is
 * not accepted by other implementations.
 *
 * <p>In some cases, Android will recognize that a regular expression is a simple
 * special case that can be handled more efficiently. This is true of both the convenience methods
 * in {@code String} and the methods in {@code Pattern}.
 *
 * @see Matcher
 */
public final class Pattern implements Serializable {

    private static final long serialVersionUID = 5073258162644648461L;

    /**
     * This constant specifies that a pattern matches Unix line endings ('\n')
     * only against the '.', '^', and '$' meta characters. Corresponds to {@code (?d)}.
     */
    public static final int UNIX_LINES = 0x01;

    /**
     * This constant specifies that a {@code Pattern} is matched
     * case-insensitively. That is, the patterns "a+" and "A+" would both match
     * the string "aAaAaA". See {@link #UNICODE_CASE}. Corresponds to {@code (?i)}.
     */
    public static final int CASE_INSENSITIVE = 0x02;

    /**
     * This constant specifies that a {@code Pattern} may contain whitespace or
     * comments. Otherwise comments and whitespace are taken as literal
     * characters. Corresponds to {@code (?x)}.
     */
    public static final int COMMENTS = 0x04;

    /**
     * This constant specifies that the meta characters '^' and '$' match only
     * the beginning and end of an input line, respectively. Normally, they
     * match the beginning and the end of the complete input. Corresponds to {@code (?m)}.
     */
    public static final int MULTILINE = 0x08;

    /**
     * This constant specifies that the whole {@code Pattern} is to be taken
     * literally, that is, all meta characters lose their meanings.
     */
    public static final int LITERAL = 0x10;

    /**
     * This constant specifies that the '.' meta character matches arbitrary
     * characters, including line endings, which is normally not the case.
     * Corresponds to {@code (?s)}.
     */
    public static final int DOTALL = 0x20;

    /**
     * This constant specifies that a {@code Pattern} that uses case-insensitive matching
     * will use Unicode case folding. On Android, {@code UNICODE_CASE} is always on:
     * case-insensitive matching will always be Unicode-aware. If your code is intended to
     * be portable and uses case-insensitive matching on non-ASCII characters, you should
     * use this flag. Corresponds to {@code (?u)}.
     */
    public static final int UNICODE_CASE = 0x40;

    /**
     * This constant specifies that a character in a {@code Pattern} and a
     * character in the input string only match if they are canonically
     * equivalent. It is (currently) not supported in Android.
     */
    public static final int CANON_EQ = 0x80;

    private final String pattern;
    private final int flags;

    transient long address;

    /**
     * Returns a {@link Matcher} for this pattern applied to the given {@code input}.
     * The {@code Matcher} can be used to match the {@code Pattern} against the
     * whole input, find occurrences of the {@code Pattern} in the input, or
     * replace parts of the input.
     */
    public Matcher matcher(CharSequence input) {
        return new Matcher(this, input);
    }

    /**
     * Splits the given {@code input} at occurrences of this pattern.
     *
     * <p>If this pattern does not occur in the input, the result is an
     * array containing the input (converted from a {@code CharSequence} to
     * a {@code String}).
     *
     * <p>Otherwise, the {@code limit} parameter controls the contents of the
     * returned array as described below.
     *
     * @param limit
     *            Determines the maximum number of entries in the resulting
     *            array, and the treatment of trailing empty strings.
     *            <ul>
     *            <li>For n &gt; 0, the resulting array contains at most n
     *            entries. If this is fewer than the number of matches, the
     *            final entry will contain all remaining input.
     *            <li>For n &lt; 0, the length of the resulting array is
     *            exactly the number of occurrences of the {@code Pattern}
     *            plus one for the text after the final separator.
     *            All entries are included.
     *            <li>For n == 0, the result is as for n &lt; 0, except
     *            trailing empty strings will not be returned. (Note that
     *            the case where the input is itself an empty string is
     *            special, as described above, and the limit parameter does
     *            not apply there.)
     *            </ul>
     */
    public String[] split(CharSequence input, int limit) {
        return Splitter.split(this, pattern, input.toString(), limit);
    }

    /**
     * Equivalent to {@code split(input, 0)}.
     */
    public String[] split(CharSequence input) {
        return split(input, 0);
    }

    /**
     * Returns the regular expression supplied to {@code compile}.
     */
    public String pattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return pattern;
    }

    /**
     * Returns the flags supplied to {@code compile}.
     */
    public int flags() {
        return flags;
    }

    /**
     * Returns a compiled form of the given {@code regularExpression}, as modified by the
     * given {@code flags}. See the <a href="#flags">flags overview</a> for more on flags.
     *
     * @throws PatternSyntaxException if the regular expression is syntactically incorrect.
     *
     * @see #CANON_EQ
     * @see #CASE_INSENSITIVE
     * @see #COMMENTS
     * @see #DOTALL
     * @see #LITERAL
     * @see #MULTILINE
     * @see #UNICODE_CASE
     * @see #UNIX_LINES
     */
    public static Pattern compile(String regularExpression, int flags) throws PatternSyntaxException {
        return new Pattern(regularExpression, flags);
    }

    /**
     * Equivalent to {@code Pattern.compile(pattern, 0)}.
     */
    public static Pattern compile(String pattern) {
        return new Pattern(pattern, 0);
    }

    private Pattern(String pattern, int flags) throws PatternSyntaxException {
        if ((flags & CANON_EQ) != 0) {
            throw new UnsupportedOperationException("CANON_EQ flag not supported");
        }
        int supportedFlags = CASE_INSENSITIVE | COMMENTS | DOTALL | LITERAL | MULTILINE | UNICODE_CASE | UNIX_LINES;
        if ((flags & ~supportedFlags) != 0) {
            throw new IllegalArgumentException("Unsupported flags: " + (flags & ~supportedFlags));
        }
        this.pattern = pattern;
        this.flags = flags;
        compile();
    }

    private void compile() throws PatternSyntaxException {
        if (pattern == null) {
            throw new NullPointerException("pattern == null");
        }

        String icuPattern = pattern;
        if ((flags & LITERAL) != 0) {
            icuPattern = quote(pattern);
        }

        // These are the flags natively supported by ICU.
        // They even have the same value in native code.
        int icuFlags = flags & (CASE_INSENSITIVE | COMMENTS | MULTILINE | DOTALL | UNIX_LINES);

        address = compileImpl(icuPattern, icuFlags);
    }

    /**
     * Tests whether the given {@code regularExpression} matches the given {@code input}.
     * Equivalent to {@code Pattern.compile(regularExpression).matcher(input).matches()}.
     * If the same regular expression is to be used for multiple operations, it may be more
     * efficient to reuse a compiled {@code Pattern}.
     *
     * @see Pattern#compile(java.lang.String, int)
     * @see Matcher#matches()
     */
    public static boolean matches(String regularExpression, CharSequence input) {
        return new Matcher(new Pattern(regularExpression, 0), input).matches();
    }

    /**
     * Quotes the given {@code string} using "\Q" and "\E", so that all
     * meta-characters lose their special meaning. This method correctly
     * escapes embedded instances of "\Q" or "\E". If the entire result
     * is to be passed verbatim to {@link #compile}, it's usually clearer
     * to use the {@link #LITERAL} flag instead.
     */
    public static String quote(String string) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\Q");
        int apos = 0;
        int k;
        while ((k = string.indexOf("\\E", apos)) >= 0) {
            sb.append(string.substring(apos, k + 2)).append("\\\\E\\Q");
            apos = k + 2;
        }
        return sb.append(string.substring(apos)).append("\\E").toString();
    }

    @Override protected void finalize() throws Throwable {
        try {
            closeImpl(address);
        } finally {
            super.finalize();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        compile();
    }

    private static native void closeImpl(long addr);
    private static native long compileImpl(String regex, int flags);
}
