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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*-[
#include "java/util/Arrays.h"
#include "J2ObjC_icu.h"
]-*/

/**
 * The wrapper for the primitive type {@code char}. This class also provides a
 * number of utility methods for working with characters.
 *
 * <p>Character data is kept up to date as Unicode evolves.
 * See the <a href="../util/Locale.html#locale_data">Locale data</a> section of
 * the {@code Locale} documentation for details of the Unicode versions implemented by current
 * and historical Android releases.
 *
 * <p>The Unicode specification, character tables, and other information are available at
 * <a href="http://www.unicode.org/">http://www.unicode.org/</a>.
 *
 * <p>Unicode characters are referred to as <i>code points</i>. The range of valid
 * code points is U+0000 to U+10FFFF. The <i>Basic Multilingual Plane (BMP)</i>
 * is the code point range U+0000 to U+FFFF. Characters above the BMP are
 * referred to as <i>Supplementary Characters</i>. On the Java platform, UTF-16
 * encoding and {@code char} pairs are used to represent code points in the
 * supplementary range. A pair of {@code char} values that represent a
 * supplementary character are made up of a <i>high surrogate</i> with a value
 * range of 0xD800 to 0xDBFF and a <i>low surrogate</i> with a value range of
 * 0xDC00 to 0xDFFF.
 * <p>
 * On the Java platform a {@code char} value represents either a single BMP code
 * point or a UTF-16 unit that's part of a surrogate pair. The {@code int} type
 * is used to represent all Unicode code points.
 *
 * <a name="unicode_categories"><h3>Unicode categories</h3></a>
 * <p>Here's a list of the Unicode character categories and the corresponding Java constant,
 * grouped semantically to provide a convenient overview. This table is also useful in
 * conjunction with {@code \p} and {@code \P} in {@link java.util.regex.Pattern regular expressions}.
 * <span class="datatable">
 * <style type="text/css">
 * .datatable td { padding-right: 20px; }
 * </style>
 * <p><table>
 * <tr> <td> Cn </td> <td> Unassigned </td>  <td>{@link #UNASSIGNED}</td> </tr>
 * <tr> <td> Cc </td> <td> Control </td>     <td>{@link #CONTROL}</td> </tr>
 * <tr> <td> Cf </td> <td> Format </td>      <td>{@link #FORMAT}</td> </tr>
 * <tr> <td> Co </td> <td> Private use </td> <td>{@link #PRIVATE_USE}</td> </tr>
 * <tr> <td> Cs </td> <td> Surrogate </td>   <td>{@link #SURROGATE}</td> </tr>
 * <tr> <td><br></td> </tr>
 * <tr> <td> Lu </td> <td> Uppercase letter </td> <td>{@link #UPPERCASE_LETTER}</td> </tr>
 * <tr> <td> Ll </td> <td> Lowercase letter </td> <td>{@link #LOWERCASE_LETTER}</td> </tr>
 * <tr> <td> Lt </td> <td> Titlecase letter </td> <td>{@link #TITLECASE_LETTER}</td> </tr>
 * <tr> <td> Lm </td> <td> Modifier letter </td>  <td>{@link #MODIFIER_LETTER}</td> </tr>
 * <tr> <td> Lo </td> <td> Other letter </td>     <td>{@link #OTHER_LETTER}</td> </tr>
 * <tr> <td><br></td> </tr>
 * <tr> <td> Mn </td> <td> Non-spacing mark </td>       <td>{@link #NON_SPACING_MARK}</td> </tr>
 * <tr> <td> Me </td> <td> Enclosing mark </td>         <td>{@link #ENCLOSING_MARK}</td> </tr>
 * <tr> <td> Mc </td> <td> Combining spacing mark </td> <td>{@link #COMBINING_SPACING_MARK}</td> </tr>
 * <tr> <td><br></td> </tr>
 * <tr> <td> Nd </td> <td> Decimal digit number </td> <td>{@link #DECIMAL_DIGIT_NUMBER}</td> </tr>
 * <tr> <td> Nl </td> <td> Letter number </td>        <td>{@link #LETTER_NUMBER}</td> </tr>
 * <tr> <td> No </td> <td> Other number </td>         <td>{@link #OTHER_NUMBER}</td> </tr>
 * <tr> <td><br></td> </tr>
 * <tr> <td> Pd </td> <td> Dash punctuation </td>          <td>{@link #DASH_PUNCTUATION}</td> </tr>
 * <tr> <td> Ps </td> <td> Start punctuation </td>         <td>{@link #START_PUNCTUATION}</td> </tr>
 * <tr> <td> Pe </td> <td> End punctuation </td>           <td>{@link #END_PUNCTUATION}</td> </tr>
 * <tr> <td> Pc </td> <td> Connector punctuation </td>     <td>{@link #CONNECTOR_PUNCTUATION}</td> </tr>
 * <tr> <td> Pi </td> <td> Initial quote punctuation </td> <td>{@link #INITIAL_QUOTE_PUNCTUATION}</td> </tr>
 * <tr> <td> Pf </td> <td> Final quote punctuation </td>   <td>{@link #FINAL_QUOTE_PUNCTUATION}</td> </tr>
 * <tr> <td> Po </td> <td> Other punctuation </td>         <td>{@link #OTHER_PUNCTUATION}</td> </tr>
 * <tr> <td><br></td> </tr>
 * <tr> <td> Sm </td> <td> Math symbol </td>     <td>{@link #MATH_SYMBOL}</td> </tr>
 * <tr> <td> Sc </td> <td> Currency symbol </td> <td>{@link #CURRENCY_SYMBOL}</td> </tr>
 * <tr> <td> Sk </td> <td> Modifier symbol </td> <td>{@link #MODIFIER_SYMBOL}</td> </tr>
 * <tr> <td> So </td> <td> Other symbol </td>    <td>{@link #OTHER_SYMBOL}</td> </tr>
 * <tr> <td><br></td> </tr>
 * <tr> <td> Zs </td> <td> Space separator </td>     <td>{@link #SPACE_SEPARATOR}</td> </tr>
 * <tr> <td> Zl </td> <td> Line separator </td>      <td>{@link #LINE_SEPARATOR}</td> </tr>
 * <tr> <td> Zp </td> <td> Paragraph separator </td> <td>{@link #PARAGRAPH_SEPARATOR}</td> </tr>
 * </table>
 * </span>
 *
 * @since 1.0
 */
//@FindBugsSuppressWarnings("DM_NUMBER_CTOR")
public final class Character implements Serializable, Comparable<Character> {
    private static final long serialVersionUID = 3786198910865385080L;

    private final char value;

    /**
     * The minimum {@code Character} value.
     */
    public static final char MIN_VALUE = '\u0000';

    /**
     * The maximum {@code Character} value.
     */
    public static final char MAX_VALUE = '\uffff';

    /**
     * The minimum radix used for conversions between characters and integers.
     */
    public static final int MIN_RADIX = 2;

    /**
     * The maximum radix used for conversions between characters and integers.
     */
    public static final int MAX_RADIX = 36;

    /**
     * The {@link Class} object that represents the primitive type {@code char}.
     */
    @SuppressWarnings("unchecked")
    public static final Class<Character> TYPE
            = (Class<Character>) char[].class.getComponentType();
    // Note: Character.TYPE can't be set to "char.class", since *that* is
    // defined to be "java.lang.Character.TYPE";

    /**
     * Unicode category constant Cn.
     */
    public static final byte UNASSIGNED = 0;

    /**
     * Unicode category constant Lu.
     */
    public static final byte UPPERCASE_LETTER = 1;

    /**
     * Unicode category constant Ll.
     */
    public static final byte LOWERCASE_LETTER = 2;

    /**
     * Unicode category constant Lt.
     */
    public static final byte TITLECASE_LETTER = 3;

    /**
     * Unicode category constant Lm.
     */
    public static final byte MODIFIER_LETTER = 4;

    /**
     * Unicode category constant Lo.
     */
    public static final byte OTHER_LETTER = 5;

    /**
     * Unicode category constant Mn.
     */
    public static final byte NON_SPACING_MARK = 6;

    /**
     * Unicode category constant Me.
     */
    public static final byte ENCLOSING_MARK = 7;

    /**
     * Unicode category constant Mc.
     */
    public static final byte COMBINING_SPACING_MARK = 8;

    /**
     * Unicode category constant Nd.
     */
    public static final byte DECIMAL_DIGIT_NUMBER = 9;

    /**
     * Unicode category constant Nl.
     */
    public static final byte LETTER_NUMBER = 10;

    /**
     * Unicode category constant No.
     */
    public static final byte OTHER_NUMBER = 11;

    /**
     * Unicode category constant Zs.
     */
    public static final byte SPACE_SEPARATOR = 12;

    /**
     * Unicode category constant Zl.
     */
    public static final byte LINE_SEPARATOR = 13;

    /**
     * Unicode category constant Zp.
     */
    public static final byte PARAGRAPH_SEPARATOR = 14;

    /**
     * Unicode category constant Cc.
     */
    public static final byte CONTROL = 15;

    /**
     * Unicode category constant Cf.
     */
    public static final byte FORMAT = 16;

    /**
     * Unicode category constant Co.
     */
    public static final byte PRIVATE_USE = 18;

    /**
     * Unicode category constant Cs.
     */
    public static final byte SURROGATE = 19;

    /**
     * Unicode category constant Pd.
     */
    public static final byte DASH_PUNCTUATION = 20;

    /**
     * Unicode category constant Ps.
     */
    public static final byte START_PUNCTUATION = 21;

    /**
     * Unicode category constant Pe.
     */
    public static final byte END_PUNCTUATION = 22;

    /**
     * Unicode category constant Pc.
     */
    public static final byte CONNECTOR_PUNCTUATION = 23;

    /**
     * Unicode category constant Po.
     */
    public static final byte OTHER_PUNCTUATION = 24;

    /**
     * Unicode category constant Sm.
     */
    public static final byte MATH_SYMBOL = 25;

    /**
     * Unicode category constant Sc.
     */
    public static final byte CURRENCY_SYMBOL = 26;

    /**
     * Unicode category constant Sk.
     */
    public static final byte MODIFIER_SYMBOL = 27;

    /**
     * Unicode category constant So.
     */
    public static final byte OTHER_SYMBOL = 28;

    /**
     * Unicode category constant Pi.
     *
     * @since 1.4
     */
    public static final byte INITIAL_QUOTE_PUNCTUATION = 29;

    /**
     * Unicode category constant Pf.
     *
     * @since 1.4
     */
    public static final byte FINAL_QUOTE_PUNCTUATION = 30;

    /**
     * Unicode bidirectional constant.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_UNDEFINED = -1;

    /**
     * Unicode bidirectional constant L.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = 0;

    /**
     * Unicode bidirectional constant R.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = 1;

    /**
     * Unicode bidirectional constant AL.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2;

    /**
     * Unicode bidirectional constant EN.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = 3;

    /**
     * Unicode bidirectional constant ES.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4;

    /**
     * Unicode bidirectional constant ET.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5;

    /**
     * Unicode bidirectional constant AN.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_ARABIC_NUMBER = 6;

    /**
     * Unicode bidirectional constant CS.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7;

    /**
     * Unicode bidirectional constant NSM.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_NONSPACING_MARK = 8;

    /**
     * Unicode bidirectional constant BN.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = 9;

    /**
     * Unicode bidirectional constant B.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10;

    /**
     * Unicode bidirectional constant S.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = 11;

    /**
     * Unicode bidirectional constant WS.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_WHITESPACE = 12;

    /**
     * Unicode bidirectional constant ON.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_OTHER_NEUTRALS = 13;

    /**
     * Unicode bidirectional constant LRE.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;

    /**
     * Unicode bidirectional constant LRO.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;

    /**
     * Unicode bidirectional constant RLE.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;

    /**
     * Unicode bidirectional constant RLO.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;

    /**
     * Unicode bidirectional constant PDF.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;

    /**
     * The minimum value of a high surrogate or leading surrogate unit in UTF-16
     * encoding, {@code '\uD800'}.
     *
     * @since 1.5
     */
    public static final char MIN_HIGH_SURROGATE = '\uD800';

    /**
     * The maximum value of a high surrogate or leading surrogate unit in UTF-16
     * encoding, {@code '\uDBFF'}.
     *
     * @since 1.5
     */
    public static final char MAX_HIGH_SURROGATE = '\uDBFF';

    /**
     * The minimum value of a low surrogate or trailing surrogate unit in UTF-16
     * encoding, {@code '\uDC00'}.
     *
     * @since 1.5
     */
    public static final char MIN_LOW_SURROGATE = '\uDC00';

    /**
     * The maximum value of a low surrogate or trailing surrogate unit in UTF-16
     * encoding, {@code '\uDFFF'}.
     *
     * @since 1.5
     */
    public static final char MAX_LOW_SURROGATE = '\uDFFF';

    /**
     * The minimum value of a surrogate unit in UTF-16 encoding, {@code '\uD800'}.
     *
     * @since 1.5
     */
    public static final char MIN_SURROGATE = '\uD800';

    /**
     * The maximum value of a surrogate unit in UTF-16 encoding, {@code '\uDFFF'}.
     *
     * @since 1.5
     */
    public static final char MAX_SURROGATE = '\uDFFF';

    /**
     * The minimum value of a supplementary code point, {@code U+010000}.
     *
     * @since 1.5
     */
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x10000;

    /**
     * The minimum code point value, {@code U+0000}.
     *
     * @since 1.5
     */
    public static final int MIN_CODE_POINT = 0x000000;

    /**
     * The maximum code point value, {@code U+10FFFF}.
     *
     * @since 1.5
     */
    public static final int MAX_CODE_POINT = 0x10FFFF;

    /**
     * The number of bits required to represent a {@code Character} value
     * unsigned form.
     *
     * @since 1.5
     */
    public static final int SIZE = 16;

    /*
     * Represents a subset of the Unicode character set.
     */
    public static class Subset {
        private final String name;

        /**
         * Constructs a new {@code Subset}.
         */
        protected Subset(String name) {
            if (name == null) {
                throw new NullPointerException("name == null");
            }
            this.name = name;
        }

        /**
         * Compares this character subset for identity with the specified object.
         */
        @Override public final boolean equals(Object object) {
            return object == this;
        }

        /**
         * Returns this subset's hash code, which is the hash code computed by
         *         {@link java.lang.Object#hashCode()}.
         */
        @Override public final int hashCode() {
            return super.hashCode();
        }

        /**
         * Returns this subset's name.
         */
        @Override public final String toString() {
            return name;
        }
    }

    /**
     * Represents a block of Unicode characters. This class provides constants for various
     * well-known blocks (but not all blocks) and methods for looking up a block
     * by name {@link #forName} or by code point {@link #of}.
     *
     * @since 1.2
     */
    public static final class UnicodeBlock extends Subset {
        int rangeStart;
        int rangeEnd;

        /**
         * The Surrogates Area Unicode block.
         *
         * @deprecated As of Java 5, this block has been replaced by
         *             {@link #HIGH_SURROGATES},
         *             {@link #HIGH_PRIVATE_USE_SURROGATES} and
         *             {@link #LOW_SURROGATES}.
         */
        @Deprecated
        public static final UnicodeBlock SURROGATES_AREA =
            new UnicodeBlock("SURROGATES_AREA", 0xD800, 0xDFFF);

        /** The Basic Latin Unicode block. */
        public static final UnicodeBlock BASIC_LATIN =
            new UnicodeBlock("BASIC_LATIN", 0, 0x7F);

        /** The Latin-1 Supplement Unicode block. */
        public static final UnicodeBlock LATIN_1_SUPPLEMENT =
            new UnicodeBlock("LATIN_1_SUPPLEMENT", 0x80, 0xFF);

        /** The Latin Extended-A Unicode block. */
        public static final UnicodeBlock LATIN_EXTENDED_A =
            new UnicodeBlock("LATIN_EXTENDED_A", 0x100, 0x17F);

        /** The Latin Extended-B Unicode block. */
        public static final UnicodeBlock LATIN_EXTENDED_B =
            new UnicodeBlock("LATIN_EXTENDED_B", 0x180, 0x24F);

        /** The IPA Extensions Unicode block. */
        public static final UnicodeBlock IPA_EXTENSIONS =
            new UnicodeBlock("IPA_EXTENSIONS", 0x250, 0x2AF);

        /** The Spacing Modifier Letters Unicode block. */
        public static final UnicodeBlock SPACING_MODIFIER_LETTERS =
            new UnicodeBlock("SPACING_MODIFIER_LETTERS", 0x2B0, 0x2FF);

        /** The Combining Diacritical Marks Unicode block. */
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS =
            new UnicodeBlock("COMBINING_DIACRITICAL_MARKS", 0x300, 0x36F);

        /**
         * The Greek and Coptic Unicode block. Previously referred to as Greek.
         */
        public static final UnicodeBlock GREEK = new UnicodeBlock("GREEK", 0x370, 0x3FF);

        /** The Cyrillic Unicode block. */
        public static final UnicodeBlock CYRILLIC = new UnicodeBlock("CYRILLIC", 0x400, 0x4FF);

        /**
         * The Cyrillic Supplement Unicode block. Previously referred to as Cyrillic Supplementary.
         */
        public static final UnicodeBlock CYRILLIC_SUPPLEMENTARY =
            new UnicodeBlock("CYRILLIC_SUPPLEMENTARY", 0x500, 0x52F);

        /** The Armenian Unicode block. */
        public static final UnicodeBlock ARMENIAN = new UnicodeBlock("ARMENIAN", 0x530, 0x58F);

        /** The Hebrew Unicode block. */
        public static final UnicodeBlock HEBREW = new UnicodeBlock("HEBREW", 0x590, 0x5FF);

        /** The Arabic Unicode block. */
        public static final UnicodeBlock ARABIC = new UnicodeBlock("ARABIC", 0x600, 0x6FF);

        /** The Syriac Unicode block. */
        public static final UnicodeBlock SYRIAC = new UnicodeBlock("SYRIAC", 0x700, 0x74F);

        /** The Thaana Unicode block. */
        public static final UnicodeBlock THAANA = new UnicodeBlock("THAANA", 0x780, 0x7BF);

        /** The Devanagari Unicode block. */
        public static final UnicodeBlock DEVANAGARI = new UnicodeBlock("DEVANAGARI", 0x900, 0x97F);

        /** The Bengali Unicode block. */
        public static final UnicodeBlock BENGALI = new UnicodeBlock("BENGALI", 0x980, 0x9FF);

        /** The Gurmukhi Unicode block. */
        public static final UnicodeBlock GURMUKHI = new UnicodeBlock("GURMUKHI", 0xA00, 0xA7F);

        /** The Gujarati Unicode block. */
        public static final UnicodeBlock GUJARATI = new UnicodeBlock("GUJARATI", 0xA80, 0xAFF);

        /** The Oriya Unicode block. */
        public static final UnicodeBlock ORIYA = new UnicodeBlock("ORIYA", 0xB00, 0xB7F);

        /** The Tamil Unicode block. */
        public static final UnicodeBlock TAMIL = new UnicodeBlock("TAMIL", 0xB80, 0xBFF);

        /** The Telugu Unicode block. */
        public static final UnicodeBlock TELUGU = new UnicodeBlock("TELUGU", 0xC00, 0xC7F);

        /** The Kannada Unicode block. */
        public static final UnicodeBlock KANNADA = new UnicodeBlock("KANNADA", 0xC80, 0xCFF);

        /** The Malayalam Unicode block. */
        public static final UnicodeBlock MALAYALAM = new UnicodeBlock("MALAYALAM", 0xD00, 0xD7F);

        /** The Sinhala Unicode block. */
        public static final UnicodeBlock SINHALA = new UnicodeBlock("SINHALA", 0xD80, 0xDFF);

        /** The Thai Unicode block. */
        public static final UnicodeBlock THAI = new UnicodeBlock("THAI", 0xE00, 0xE7F);

        /** The Lao Unicode block. */
        public static final UnicodeBlock LAO = new UnicodeBlock("LAO", 0xE80, 0xEFF);

        /** The Tibetan Unicode block. */
        public static final UnicodeBlock TIBETAN = new UnicodeBlock("TIBETAN", 0xF00, 0xFFF);

        /** The Myanmar Unicode block. */
        public static final UnicodeBlock MYANMAR = new UnicodeBlock("MYANMAR", 0x1000, 0x109F);

        /** The Georgian Unicode block. */
        public static final UnicodeBlock GEORGIAN = new UnicodeBlock("GEORGIAN", 0x10A0, 0x10FF);

        /** The Hangul Jamo Unicode block. */
        public static final UnicodeBlock HANGUL_JAMO =
            new UnicodeBlock("HANGUL_JAMO", 0x1100, 0x11FF);

        /** The Ethiopic Unicode block. */
        public static final UnicodeBlock ETHIOPIC = new UnicodeBlock("ETHIOPIC", 0x1200, 0x137F);

        /** The Cherokee Unicode block. */
        public static final UnicodeBlock CHEROKEE = new UnicodeBlock("CHEROKEE", 0x13A0, 0x13FF);

        /** The Unified Canadian Aboriginal Syllabics Unicode block. */
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS =
            new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS", 0x1400, 0x167F);

        /** The Ogham Unicode block. */
        public static final UnicodeBlock OGHAM = new UnicodeBlock("OGHAM", 0x1680, 0x169f);

        /** The Runic Unicode block. */
        public static final UnicodeBlock RUNIC = new UnicodeBlock("RUNIC", 0x16A0, 0x16FF);

        /** The Tagalog Unicode block. */
        public static final UnicodeBlock TAGALOG = new UnicodeBlock("TAGALOG", 0x1700, 0x171F);

        /** The Hanunoo Unicode block. */
        public static final UnicodeBlock HANUNOO = new UnicodeBlock("HANUNOO", 0x1720, 0x173F);

        /** The Buhid Unicode block. */
        public static final UnicodeBlock BUHID = new UnicodeBlock("BUHID", 0x1740, 0x175F);

        /** The Tagbanwa Unicode block. */
        public static final UnicodeBlock TAGBANWA = new UnicodeBlock("TAGBANWA", 0x1760, 0x177F);

        /** The Khmer Unicode block. */
        public static final UnicodeBlock KHMER = new UnicodeBlock("KHMER", 0x1780, 0x17FF);

        /** The Mongolian Unicode block. */
        public static final UnicodeBlock MONGOLIAN = new UnicodeBlock("MONGOLIAN", 0x1800, 0x18AF);

        /** The Limbu Unicode block. */
        public static final UnicodeBlock LIMBU = new UnicodeBlock("LIMBU", 0x1900, 0x194F);

        /** The Tai Le Unicode block. */
        public static final UnicodeBlock TAI_LE = new UnicodeBlock("TAI_LE", 0x1950, 0x197F);

        /** The Khmer Symbols Unicode block. */
        public static final UnicodeBlock KHMER_SYMBOLS =
            new UnicodeBlock("KHMER_SYMBOLS", 0x19E0, 0x19FF);

        /** The Phonetic Extensions Unicode block. */
        public static final UnicodeBlock PHONETIC_EXTENSIONS =
            new UnicodeBlock("PHONETIC_EXTENSIONS", 0x1D00, 0x1D7F);

        /** The Latin Extended Additional Unicode block. */
        public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL =
            new UnicodeBlock("LATIN_EXTENDED_ADDITIONAL", 0x1E00, 0x1EFF);

        /** The Greek Extended Unicode block. */
        public static final UnicodeBlock GREEK_EXTENDED =
            new UnicodeBlock("GREEK_EXTENDED", 0x1F00, 0x1FFF);

        /** The General Punctuation Unicode block. */
        public static final UnicodeBlock GENERAL_PUNCTUATION =
            new UnicodeBlock("GENERAL_PUNCTUATION", 0x2000, 0x206F);

        /** The Superscripts and Subscripts Unicode block. */
        public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS =
            new UnicodeBlock("SUPERSCRIPTS_AND_SUBSCRIPTS", 0x2070, 0x209F);

        /** The Currency Symbols Unicode block. */
        public static final UnicodeBlock CURRENCY_SYMBOLS =
            new UnicodeBlock("CURRENCY_SYMBOLS", 0x2070, 0x20CF);

        /**
         * The Combining Diacritical Marks for Symbols Unicode
         * Block. Previously referred to as Combining Marks for
         * Symbols.
         */
        public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS =
            new UnicodeBlock("COMBINING_MARKS_FOR_SYMBOLS", 0x20D0, 0x20FF);

        /** The Letterlike Symbols Unicode block. */
        public static final UnicodeBlock LETTERLIKE_SYMBOLS =
            new UnicodeBlock("LETTERLIKE_SYMBOLS", 0x2100, 0x214F);

        /** The Number Forms Unicode block. */
        public static final UnicodeBlock NUMBER_FORMS =
            new UnicodeBlock("NUMBER_FORMS", 0x2150, 0x218F);

        /** The Arrows Unicode block. */
        public static final UnicodeBlock ARROWS = new UnicodeBlock("ARROWS", 0x2190, 0x21FF);

        /** The Mathematical Operators Unicode block. */
        public static final UnicodeBlock MATHEMATICAL_OPERATORS =
            new UnicodeBlock("MATHEMATICAL_OPERATORS", 0x2200, 0x22FF);

        /** The Miscellaneous Technical Unicode block. */
        public static final UnicodeBlock MISCELLANEOUS_TECHNICAL =
            new UnicodeBlock("MISCELLANEOUS_TECHNICAL", 0x2300, 0x23FF);

        /** The Control Pictures Unicode block. */
        public static final UnicodeBlock CONTROL_PICTURES =
            new UnicodeBlock("CONTROL_PICTURES", 0x2400, 0x243F);

        /** The Optical Character Recognition Unicode block. */
        public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION =
            new UnicodeBlock("OPTICAL_CHARACTER_RECOGNITION", 0x2440, 0x245F);

        /** The Enclosed Alphanumerics Unicode block. */
        public static final UnicodeBlock ENCLOSED_ALPHANUMERICS =
            new UnicodeBlock("ENCLOSED_ALPHANUMERICS", 0x2460, 0x24FF);

        /** The Box Drawing Unicode block. */
        public static final UnicodeBlock BOX_DRAWING =
            new UnicodeBlock("BOX_DRAWING", 0x2500, 0x257F);

        /** The Block Elements Unicode block. */
        public static final UnicodeBlock BLOCK_ELEMENTS =
            new UnicodeBlock("BLOCK_ELEMENTS", 0x2580, 0x259F);

        /** The Geometric Shapes Unicode block. */
        public static final UnicodeBlock GEOMETRIC_SHAPES =
            new UnicodeBlock("GEOMETRIC_SHAPES", 0x25A0, 0x25FF);

        /** The Miscellaneous Symbols Unicode block. */
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS =
            new UnicodeBlock("MISCELLANEOUS_SYMBOLS", 0x2600, 0x26FF);

        /** The Dingbats Unicode block. */
        public static final UnicodeBlock DINGBATS = new UnicodeBlock("DINGBATS", 0x2700, 0x27BF);

        /** The Miscellaneous Mathematical Symbols-A Unicode block. */
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A =
            new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A", 0x27C0, 0x27EF);

        /** The Supplemental Arrows-A Unicode block. */
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_A =
            new UnicodeBlock("SUPPLEMENTAL_ARROWS_A", 0x27F0, 0x27FF);

        /** The Braille Patterns Unicode block. */
        public static final UnicodeBlock BRAILLE_PATTERNS =
            new UnicodeBlock("BRAILLE_PATTERNS", 0x2800, 0x28FF);

        /** The Supplemental Arrows-B Unicode block. */
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_B =
            new UnicodeBlock("SUPPLEMENTAL_ARROWS_B", 0x2900, 0x297F);

        /** The Miscellaneous Mathematical Symbols-B Unicode block. */
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B =
            new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B", 0x2980, 0x29FF);

        /** The Supplemental Mathematical Operators Unicode block. */
        public static final UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS =
            new UnicodeBlock("SUPPLEMENTAL_MATHEMATICAL_OPERATORS", 0x2A00, 0x2AFF);

        /** The Miscellaneous Symbols and Arrows Unicode block. */
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_ARROWS =
            new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_ARROWS", 0x2B00, 0x2BFF);

        /** The CJK Radicals Supplement Unicode block. */
        public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT =
            new UnicodeBlock("CJK_RADICALS_SUPPLEMENT", 0x2E80, 0x2EFF);

        /** The Kangxi Radicals Unicode block. */
        public static final UnicodeBlock KANGXI_RADICALS =
            new UnicodeBlock("KANGXI_RADICALS", 0x2F00, 0x2FDF);

        /** The Ideographic Description Characters Unicode block. */
        public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS =
            new UnicodeBlock("IDEOGRAPHIC_DESCRIPTION_CHARACTERS", 0x2FF0, 0x2FFF);

        /** The CJK Symbols and Punctuation Unicode block. */
        public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION =
            new UnicodeBlock("CJK_SYMBOLS_AND_PUNCTUATION", 0x3000, 0x303F);

        /** The Hiragana Unicode block. */
        public static final UnicodeBlock HIRAGANA = new UnicodeBlock("HIRAGANA", 0x3040, 0x309F);

        /** The Katakana Unicode block. */
        public static final UnicodeBlock KATAKANA = new UnicodeBlock("KATAKANA", 0x30A0, 0x30FF);

        /** The Bopomofo Unicode block. */
        public static final UnicodeBlock BOPOMOFO = new UnicodeBlock("BOPOMOFO", 0x3100, 0x312F);

        /** The Hangul Compatibility Jamo Unicode block. */
        public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO =
            new UnicodeBlock("HANGUL_COMPATIBILITY_JAMO", 0x3130, 0x318F);

        /** The Kanbun Unicode block. */
        public static final UnicodeBlock KANBUN = new UnicodeBlock("KANBUN", 0x3190, 0x319F);

        /** The Bopomofo Extended Unicode block. */
        public static final UnicodeBlock BOPOMOFO_EXTENDED =
            new UnicodeBlock("BOPOMOFO_EXTENDED", 0x31A0, 0x31BF);

        /** The Katakana Phonetic Extensions Unicode block. */
        public static final UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS =
            new UnicodeBlock("KATAKANA_PHONETIC_EXTENSIONS", 0x31F0, 0x31FF);

        /** The Enclosed CJK Letters and Months Unicode block. */
        public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS =
            new UnicodeBlock("ENCLOSED_CJK_LETTERS_AND_MONTHS", 0x3200, 0x32FF);

        /** The CJK Compatibility Unicode block. */
        public static final UnicodeBlock CJK_COMPATIBILITY =
            new UnicodeBlock("CJK_COMPATIBILITY", 0x3300, 0x33FF);

        /** The CJK Unified Ideographs Extension A Unicode block. */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A =
            new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A", 0x3400, 0x4DBF);

        /** The Yijing Hexagram Symbols Unicode block. */
        public static final UnicodeBlock YIJING_HEXAGRAM_SYMBOLS =
            new UnicodeBlock("YIJING_HEXAGRAM_SYMBOLS", 0x4DC0, 0x4DFF);

        /** The CJK Unified Ideographs Unicode block. */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS =
            new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS", 0x4E00, 0x9FFF);

        /** The Yi Syllables Unicode block. */
        public static final UnicodeBlock YI_SYLLABLES =
            new UnicodeBlock("YI_SYLLABLES", 0xA000, 0xA48F);

        /** The Yi Radicals Unicode block. */
        public static final UnicodeBlock YI_RADICALS =
            new UnicodeBlock("YI_RADICALS", 0xA490, 0xA4CF);

        /** The Hangul Syllables Unicode block. */
        public static final UnicodeBlock HANGUL_SYLLABLES =
            new UnicodeBlock("HANGUL_SYLLABLES", 0xAC00, 0xD7AF);

        /**
         * The High Surrogates Unicode block. This block represents
         * code point values in the high surrogate range 0xD800 to 0xDB7F
         */
        public static final UnicodeBlock HIGH_SURROGATES =
            new UnicodeBlock("HIGH_SURROGATES", 0xD800, 0xDB7F);

        /**
         * The High Private Use Surrogates Unicode block. This block
         * represents code point values in the high surrogate range 0xDB80 to
         * 0xDBFF
         */
        public static final UnicodeBlock HIGH_PRIVATE_USE_SURROGATES =
            new UnicodeBlock("HIGH_PRIVATE_USE_SURROGATES", 0xDB80, 0xDBFF);

        /**
         * The Low Surrogates Unicode block. This block represents
         * code point values in the low surrogate range 0xDC00 to 0xDFFF
         */
        public static final UnicodeBlock LOW_SURROGATES =
            new UnicodeBlock("LOW_SURROGATES", 0xDC00, 0xDFFF);

        /** The Private Use Area Unicode block. */
        public static final UnicodeBlock PRIVATE_USE_AREA =
            new UnicodeBlock("PRIVATE_USE_AREA", 0xE000, 0xF8FF);

        /** The CJK Compatibility Ideographs Unicode block. */
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS =
            new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS", 0xF900, 0xFAFF);

        /** The Alphabetic Presentation Forms Unicode block. */
        public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS =
            new UnicodeBlock("ALPHABETIC_PRESENTATION_FORMS", 0xFB00, 0xFB4F);

        /** The Arabic Presentation Forms-A Unicode block. */
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A =
            new UnicodeBlock("ARABIC_PRESENTATION_FORMS_A", 0xFB50, 0xFDFF);

        /** The Variation Selectors Unicode block. */
        public static final UnicodeBlock VARIATION_SELECTORS =
            new UnicodeBlock("VARIATION_SELECTORS", 0xFE00, 0xFE0F);

        /** The Combining Half Marks Unicode block. */
        public static final UnicodeBlock COMBINING_HALF_MARKS =
            new UnicodeBlock("COMBINING_HALF_MARKS", 0xFE20, 0xFE2F);

        /** The CJK Compatibility Forms Unicode block. */
        public static final UnicodeBlock CJK_COMPATIBILITY_FORMS =
            new UnicodeBlock("CJK_COMPATIBILITY_FORMS", 0xFE30, 0xFE4F);

        /** The Small Form Variants Unicode block. */
        public static final UnicodeBlock SMALL_FORM_VARIANTS =
            new UnicodeBlock("SMALL_FORM_VARIANTS", 0xFE50, 0xFE6F);

        /** The Arabic Presentation Forms-B Unicode block. */
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B =
            new UnicodeBlock("ARABIC_PRESENTATION_FORMS_B", 0xFB50, 0xFDFF);

        /** The Halfwidth and Fullwidth Forms Unicode block. */
        public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS =
            new UnicodeBlock("HALFWIDTH_AND_FULLWIDTH_FORMS", 0xFF00, 0xFFEF);

        /** The Specials Unicode block. */
        public static final UnicodeBlock SPECIALS =
            new UnicodeBlock("SPECIALS", 0xFFF0, 0xFFFF);

        /** The Linear B Syllabary Unicode block. */
        public static final UnicodeBlock LINEAR_B_SYLLABARY =
            new UnicodeBlock("LINEAR_B_SYLLABARY", 0x10000, 0x1007F);

        /** The Linear B Ideograms Unicode block. */
        public static final UnicodeBlock LINEAR_B_IDEOGRAMS =
            new UnicodeBlock("LINEAR_B_IDEOGRAMS", 0x10080, 0x100FF);

        /** The Aegean Numbers Unicode block. */
        public static final UnicodeBlock AEGEAN_NUMBERS =
            new UnicodeBlock("AEGEAN_NUMBERS", 0x10100, 0x1013F);

        /** The Old Italic Unicode block. */
        public static final UnicodeBlock OLD_ITALIC =
            new UnicodeBlock("OLD_ITALIC", 0x10300, 0x1032F);

        /** The Gothic Unicode block. */
        public static final UnicodeBlock GOTHIC = new UnicodeBlock("GOTHIC", 0x10330, 0x1034F);

        /** The Ugaritic Unicode block. */
        public static final UnicodeBlock UGARITIC = new UnicodeBlock("UGARITIC", 0x10380, 0x1039F);

        /** The Deseret Unicode block. */
        public static final UnicodeBlock DESERET = new UnicodeBlock("DESERET", 0x10400, 0x1044F);

        /** The Shavian Unicode block. */
        public static final UnicodeBlock SHAVIAN = new UnicodeBlock("SHAVIAN", 0x10450, 0x1047F);

        /** The Osmanya Unicode block. */
        public static final UnicodeBlock OSMANYA = new UnicodeBlock("OSMANYA", 0x10f80, 0x104AF);

        /** The Cypriot Syllabary Unicode block. */
        public static final UnicodeBlock CYPRIOT_SYLLABARY =
            new UnicodeBlock("CYPRIOT_SYLLABARY", 0x10800, 0x1085F);

        /** The Byzantine Musical Symbols Unicode block. */
        public static final UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS =
            new UnicodeBlock("BYZANTINE_MUSICAL_SYMBOLS", 0x1D000, 0x1D0FF);

        /** The Musical Symbols Unicode block. */
        public static final UnicodeBlock MUSICAL_SYMBOLS =
            new UnicodeBlock("MUSICAL_SYMBOLS", 0x1D100, 0x1D1FF);

        /** The Tai Xuan Jing Symbols Unicode block. */
        public static final UnicodeBlock TAI_XUAN_JING_SYMBOLS =
            new UnicodeBlock("TAI_XUAN_JING_SYMBOLS", 0x1D300, 0x1D35F);

        /** The Mathematical Alphanumeric Symbols Unicode block. */
        public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS =
            new UnicodeBlock("MATHEMATICAL_ALPHANUMERIC_SYMBOLS", 0x1D400, 0x1D7FF);

        /** The CJK Unified Ideographs Extension B Unicode block. */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B =
            new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B", 0x20000, 0x2a6DF);

        /** The CJK Compatibility Ideographs Supplement Unicode block. */
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT =
            new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT", 0x2F800, 0x2FA1F);

        /** The Tags Unicode block. */
        public static final UnicodeBlock TAGS = new UnicodeBlock("TAGS", 0xE0000, 0xE007F);

        /** The Variation Selectors Supplement Unicode block. */
        public static final UnicodeBlock VARIATION_SELECTORS_SUPPLEMENT =
            new UnicodeBlock("VARIATION_SELECTORS_SUPPLEMENT", 0xE0100, 0xE01EF);

        /** The Supplementary Private Use Area-A Unicode block. */
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A =
            new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_A", 0xF0000, 0xFFFFF);

        /** The Supplementary Private Use Area-B Unicode block. */
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B =
            new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_B", 0x100000, 0x10FFFF);

        // Unicode 4.1.

        /** The Ancient Greek Musical Notation Unicode 4.1 block. */
        public static final UnicodeBlock ANCIENT_GREEK_MUSICAL_NOTATION =
            new UnicodeBlock("ANCIENT_GREEK_MUSICAL_NOTATION", 0x1D200, 0x1D24F);

        /** The Ancient Greek Numbers Unicode 4.1 block. */
        public static final UnicodeBlock ANCIENT_GREEK_NUMBERS =
            new UnicodeBlock("ANCIENT_GREEK_NUMBERS", 0x10140, 0x1018F);

        /** The Arabic Supplement Unicode 4.1 block. */
        public static final UnicodeBlock ARABIC_SUPPLEMENT =
            new UnicodeBlock("ARABIC_SUPPLEMENT", 0x750, 0x77F);

        /** The Buginese Unicode 4.1 block. */
        public static final UnicodeBlock BUGINESE = new UnicodeBlock("BUGINESE", 0x1A00, 0x1A1F);

        /** The CJK Strokes Unicode 4.1 block. */
        public static final UnicodeBlock CJK_STROKES =
            new UnicodeBlock("CJK_STROKES", 0x31c0, 0x31EF);

        /** The Combining Diacritical Marks Supplement Unicode 4.1 block. */
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_SUPPLEMENT =
            new UnicodeBlock("COMBINING_DIACRITICAL_MARKS_SUPPLEMENT", 0x1DC0, 0x1DFF);

        /** The Coptic Unicode 4.1 block. */
        public static final UnicodeBlock COPTIC = new UnicodeBlock("COPTIC", 0x2C80, 0x2CFF);

        /** The Ethiopic Extended Unicode 4.1 block. */
        public static final UnicodeBlock ETHIOPIC_EXTENDED =
            new UnicodeBlock("ETHIOPIC_EXTENDED", 0x2D80, 0x2DDF);

        /** The Ethiopic Supplement Unicode 4.1 block. */
        public static final UnicodeBlock ETHIOPIC_SUPPLEMENT =
            new UnicodeBlock("ETHIOPIC_SUPPLEMENT", 0x1380, 0x139F);

        /** The Georgian Supplement Unicode 4.1 block. */
        public static final UnicodeBlock GEORGIAN_SUPPLEMENT =
            new UnicodeBlock("GEORGIAN_SUPPLEMENT", 0x2D00, 0x2D2F);

        /** The Glagolitic Unicode 4.1 block. */
        public static final UnicodeBlock GLAGOLITIC =
            new UnicodeBlock("GLAGOLITIC", 0x2C00, 0x2C5F);

        /** The Kharoshthi Unicode 4.1 block. */
        public static final UnicodeBlock KHAROSHTHI =
            new UnicodeBlock("KHAROSHTHI", 0x10A00, 0x10A5F);

        /** The Modifier Tone Letters Unicode 4.1 block. */
        public static final UnicodeBlock MODIFIER_TONE_LETTERS =
            new UnicodeBlock("MODIFIER_TONE_LETTERS", 0xA700, 0xA71F);

        /** The New Tai Lue Unicode 4.1 block. */
        public static final UnicodeBlock NEW_TAI_LUE =
            new UnicodeBlock("NEW_TAI_LUE", 0x1980, 0x19DF);

        /** The Old Persian Unicode 4.1 block. */
        public static final UnicodeBlock OLD_PERSIAN =
            new UnicodeBlock("OLD_PERSIAN", 0x103A0, 0x103DF);

        /** The Phonetic Extensions Supplement Unicode 4.1 block. */
        public static final UnicodeBlock PHONETIC_EXTENSIONS_SUPPLEMENT =
            new UnicodeBlock("PHONETIC_EXTENSIONS_SUPPLEMENT", 0x1D80, 0x1DBF);

        /** The Supplemental Punctuation Unicode 4.1 block. */
        public static final UnicodeBlock SUPPLEMENTAL_PUNCTUATION =
            new UnicodeBlock("SUPPLEMENTAL_PUNCTUATION", 0x2E00, 0x2E7F);

        /** The Syloti Nagri Unicode 4.1 block. */
        public static final UnicodeBlock SYLOTI_NAGRI =
            new UnicodeBlock("SYLOTI_NAGRI", 0xA800, 0xA82F);

        /** The Tifinagh Unicode 4.1 block. */
        public static final UnicodeBlock TIFINAGH = new UnicodeBlock("TIFINAGH", 0x2D30, 0x2D7F);

        /** The Vertical Forms Unicode 4.1 block. */
        public static final UnicodeBlock VERTICAL_FORMS =
            new UnicodeBlock("VERTICAL_FORMS", 0xFE10, 0xFE1F);

        // Unicode 5.0.

        /** The NKo Unicode 5.0 block. */
        public static final UnicodeBlock NKO = new UnicodeBlock("NKO", 0x7C0, 0x7FF);

        /** The Balinese Unicode 5.0 block. */
        public static final UnicodeBlock BALINESE = new UnicodeBlock("BALINESE", 0x1B00, 0x1B7F);

        /** The Latin Extended C Unicode 5.0 block. */
        public static final UnicodeBlock LATIN_EXTENDED_C =
            new UnicodeBlock("LATIN_EXTENDED_C", 0x2C60, 0x2C7F);

        /** The Latin Extended D Unicode 5.0 block. */
        public static final UnicodeBlock LATIN_EXTENDED_D =
            new UnicodeBlock("LATIN_EXTENDED_D", 0xA720, 0xA7FF);

        /** The Phags-pa Unicode 5.0 block. */
        public static final UnicodeBlock PHAGS_PA = new UnicodeBlock("PHAGS_PA", 0xA840, 0xA87F);

        /** The Phoenician Unicode 5.0 block. */
        public static final UnicodeBlock PHOENICIAN =
            new UnicodeBlock("PHOENICIAN", 0x10900, 0x1091F);

        /** The Cuneiform Unicode 5.0 block. */
        public static final UnicodeBlock CUNEIFORM =
            new UnicodeBlock("CUNEIFORM", 0x12000, 0x123FF);

        /** The Cuneiform Numbers And Punctuation Unicode 5.0 block. */
        public static final UnicodeBlock CUNEIFORM_NUMBERS_AND_PUNCTUATION =
            new UnicodeBlock("CUNEIFORM_NUMBERS_AND_PUNCTUATION", 0x12400, 0x1247F);

        /** The Counting Rod Numerals Unicode 5.0 block. */
        public static final UnicodeBlock COUNTING_ROD_NUMERALS =
            new UnicodeBlock("COUNTING_ROD_NUMERALS", 0x1D360, 0x1D37F);

        // Unicode 5.1.

        /** The Sudanese Unicode 5.1 block. */
        public static final UnicodeBlock SUNDANESE = new UnicodeBlock("SUNDANESE", 0x1B80, 0x1BBF);

        /** The Lepcha Unicode 5.1 block. */
        public static final UnicodeBlock LEPCHA = new UnicodeBlock("LEPCHA", 0x1C00, 0x1C4F);

        /** The Ol Chiki Unicode 5.1 block. */
        public static final UnicodeBlock OL_CHIKI = new UnicodeBlock("OL_CHIKI", 0x1C50, 0x1C7F);

        /** The Cyrillic Extended-A Unicode 5.1 block. */
        public static final UnicodeBlock CYRILLIC_EXTENDED_A =
            new UnicodeBlock("CYRILLIC_EXTENDED_A", 0x2DE0, 0x2DFF);

        /** The Vai Unicode 5.1 block. */
        public static final UnicodeBlock VAI = new UnicodeBlock("VAI", 0xA500, 0xA63F);

        /** The Cyrillic Extended-B Unicode 5.1 block. */
        public static final UnicodeBlock CYRILLIC_EXTENDED_B =
            new UnicodeBlock("CYRILLIC_EXTENDED_B", 0xA640, 0xA69F);

        /** The Saurashtra Unicode 5.1 block. */
        public static final UnicodeBlock SAURASHTRA =
            new UnicodeBlock("SAURASHTRA", 0xA880, 0xA8DF);

        /** The Kayah Li Unicode 5.1 block. */
        public static final UnicodeBlock KAYAH_LI = new UnicodeBlock("KAYAH_LI", 0xA930, 0xA95F);

        /** The Rejang Unicode 5.1 block. */
        public static final UnicodeBlock REJANG = new UnicodeBlock("REJANG", 0xA930, 0xa5F);

        /** The Cham Unicode 5.1 block. */
        public static final UnicodeBlock CHAM = new UnicodeBlock("CHAM", 0xAA00, 0xAA5F);

        /** The Ancient Symbols Unicode 5.1 block. */
        public static final UnicodeBlock ANCIENT_SYMBOLS =
            new UnicodeBlock("ANCIENT_SYMBOLS", 0x10190, 0x101CF);

        /** The Phaistos Disc Unicode 5.1 block. */
        public static final UnicodeBlock PHAISTOS_DISC =
            new UnicodeBlock("PHAISTOS_DISC", 0x101D0, 0x101FF);

        /** The Lycian Unicode 5.1 block. */
        public static final UnicodeBlock LYCIAN = new UnicodeBlock("LYCIAN", 0x10280, 0x1029F);

        /** The Carian Unicode 5.1 block. */
        public static final UnicodeBlock CARIAN = new UnicodeBlock("CARIAN", 0x102A0, 0x102DF);

        /** The Lydian Unicode 5.1 block. */
        public static final UnicodeBlock LYDIAN = new UnicodeBlock("LYDIAN", 0x10920, 0x1093F);

        /** The Mahjong Tiles Unicode 5.1 block. */
        public static final UnicodeBlock MAHJONG_TILES =
            new UnicodeBlock("MAHJONG_TILES", 0x1F000, 0x1F02F);

        /** The Domino Tiles Unicode 5.1 block. */
        public static final UnicodeBlock DOMINO_TILES =
            new UnicodeBlock("DOMINO_TILES", 0x1F030, 0x1F09F);

        // Unicode 5.2.

        /** The Samaritan Unicode 5.2 block. */
        public static final UnicodeBlock SAMARITAN = new UnicodeBlock("SAMARITAN", 0x800, 0x83F);

        /** The Unified Canadian Aboriginal Syllabics Expanded Unicode 5.2 block. */
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED =
            new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED", 0x1400, 0x167F);

        /** The Tai Tham Unicode 5.2 block. */
        public static final UnicodeBlock TAI_THAM = new UnicodeBlock("TAI_THAM", 0x1A20, 0x1AAF);

        /** The Vedic Extensions Unicode 5.2 block. */
        public static final UnicodeBlock VEDIC_EXTENSIONS =
            new UnicodeBlock("VEDIC_EXTENSIONS", 0x1CD0, 0x1CFF);

        /** The Lisu Extensions Unicode 5.2 block. */
        public static final UnicodeBlock LISU = new UnicodeBlock("LISU", 0xA4D0, 0xA4FF);

        /** The Bamum Extensions Unicode 5.2 block. */
        public static final UnicodeBlock BAMUM = new UnicodeBlock("BAMUM", 0xA6A0, 0xA6FF);

        /** The Common Indic Number Forms Unicode 5.2 block. */
        public static final UnicodeBlock COMMON_INDIC_NUMBER_FORMS =
            new UnicodeBlock("COMMON_INDIC_NUMBER_FORMS", 0xA830, 0xA83F);

        /** The Devanagari Extended Unicode 5.2 block. */
        public static final UnicodeBlock DEVANAGARI_EXTENDED =
            new UnicodeBlock("DEVANAGARI_EXTENDED", 0xA8E0, 0xA8FF);

        /** The Hangul Jamo Extended-A Unicode 5.2 block. */
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_A =
            new UnicodeBlock("HANGUL_JAMO_EXTENDED_A", 0xA980, 0xA9DF);

        /** The Javanese Unicode 5.2 block. */
        public static final UnicodeBlock JAVANESE = new UnicodeBlock("JAVANESE", 0xA980, 0xA9DF);

        /** The Myanmar Extended-A Unicode 5.2 block. */
        public static final UnicodeBlock MYANMAR_EXTENDED_A =
            new UnicodeBlock("MYANMAR_EXTENDED_A", 0xAA60, 0xAA7F);

        /** The Tai Viet Unicode 5.2 block. */
        public static final UnicodeBlock TAI_VIET = new UnicodeBlock("TAI_VIET", 0xAA80, 0xAADF);

        /** The Meetei Mayek Unicode 5.2 block. */
        public static final UnicodeBlock MEETEI_MAYEK =
            new UnicodeBlock("MEETEI_MAYEK", 0xAAE0, 0xABFF);

        /** The Hangul Jamo Extended-B Unicode 5.2 block. */
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_B =
            new UnicodeBlock("HANGUL_JAMO_EXTENDED_B", 0xD7B0, 0xD7FF);

        /** The Imperial Aramaic Unicode 5.2 block. */
        public static final UnicodeBlock IMPERIAL_ARAMAIC =
            new UnicodeBlock("IMPERIAL_ARAMAIC", 0x10840, 0x1085F);

        /** The Old South Arabian Unicode 5.2 block. */
        public static final UnicodeBlock OLD_SOUTH_ARABIAN =
            new UnicodeBlock("OLD_SOUTH_ARABIAN", 0x10A60, 0x10A7F);

        /** The Avestan Unicode 5.2 block. */
        public static final UnicodeBlock AVESTAN = new UnicodeBlock("AVESTAN", 0x10B00, 0x10B3F);

        /** The Inscriptional Pathian Unicode 5.2 block. */
        public static final UnicodeBlock INSCRIPTIONAL_PARTHIAN =
            new UnicodeBlock("INSCRIPTIONAL_PARTHIAN", 0x10B40, 0x10B5F);

        /** The Inscriptional Pahlavi Unicode 5.2 block. */
        public static final UnicodeBlock INSCRIPTIONAL_PAHLAVI =
            new UnicodeBlock("INSCRIPTIONAL_PAHLAVI", 0x10B60, 0x10B7F);

        /** The Old Turkic Unicode 5.2 block. */
        public static final UnicodeBlock OLD_TURKIC =
            new UnicodeBlock("OLD_TURKIC", 0x10C00, 0x10C4F);

        /** The Rumi Numeral Symbols Unicode 5.2 block. */
        public static final UnicodeBlock RUMI_NUMERAL_SYMBOLS =
            new UnicodeBlock("RUMI_NUMERAL_SYMBOLS", 0x10E60, 0x10E7F);

        /** The Kaithi Unicode 5.2 block. */
        public static final UnicodeBlock KAITHI = new UnicodeBlock("KAITHI", 0x11080, 0x110CF);

        /** The Egyptian Hieroglyphs Unicode 5.2 block. */
        public static final UnicodeBlock EGYPTIAN_HIEROGLYPHS =
            new UnicodeBlock("EGYPTIAN_HIEROGLYPHS", 0x13000, 0x1342F);

        /** The Enclosed Alphanumeric Supplement Unicode 5.2 block. */
        public static final UnicodeBlock ENCLOSED_ALPHANUMERIC_SUPPLEMENT =
            new UnicodeBlock("ENCLOSED_ALPHANUMERIC_SUPPLEMENT", 0x1F100, 0x1F1FF);

        /** The Enclosed Ideographic Supplement Unicode 5.2 block. */
        public static final UnicodeBlock ENCLOSED_IDEOGRAPHIC_SUPPLEMENT =
            new UnicodeBlock("ENCLOSED_IDEOGRAPHIC_SUPPLEMENT", 0x1F200, 0x1F2FF);

        /** The CJK Unified Ideographs Unicode 5.2 block. */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C =
            new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C", 0x2A700, 0x2B73F);

        // Unicode 6.0.

        /** The Mandaic Unicode 6.0 block. */
        public static final UnicodeBlock MANDAIC = new UnicodeBlock("MANDAIC", 0x840, 0x85F);

        /** The Batak Unicode 6.0 block. */
        public static final UnicodeBlock BATAK = new UnicodeBlock("BATAK", 0x1BC0, 0x1BFF);

        /** The Ethiopic Extended-A Unicode 6.0 block. */
        public static final UnicodeBlock ETHIOPIC_EXTENDED_A =
            new UnicodeBlock("ETHIOPIC_EXTENDED_A", 0x2DE0, 0x2DFF);

        /** The Brahmi Unicode 6.0 block. */
        public static final UnicodeBlock BRAHMI = new UnicodeBlock("BRAHMI", 0x11000, 0x1107F);

        /** The Bamum Supplement Unicode 6.0 block. */
        public static final UnicodeBlock BAMUM_SUPPLEMENT =
            new UnicodeBlock("BAMUM_SUPPLEMENT", 0x16800, 0x16A3F);

        /** The Kana Supplement Unicode 6.0 block. */
        public static final UnicodeBlock KANA_SUPPLEMENT =
            new UnicodeBlock("KANA_SUPPLEMENT", 0x1B000, 0x1B0FF);

        /** The Playing Cards Supplement Unicode 6.0 block. */
        public static final UnicodeBlock PLAYING_CARDS =
            new UnicodeBlock("PLAYING_CARDS", 0x1F0A0, 0x1F0FF);

        /** The Miscellaneous Symbols And Pictographs Supplement Unicode 6.0 block. */
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS =
            new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS", 0x1F300, 0x1F5FF);

        /** The Emoticons Unicode 6.0 block. */
        public static final UnicodeBlock EMOTICONS =
            new UnicodeBlock("EMOTICONS", 0x1F600, 0x1F64F);

        /** The Transport And Map Symbols Unicode 6.0 block. */
        public static final UnicodeBlock TRANSPORT_AND_MAP_SYMBOLS =
            new UnicodeBlock("TRANSPORT_AND_MAP_SYMBOLS", 0x1F680, 0x1F6FF);

        /** The Alchemical Symbols Unicode 6.0 block. */
        public static final UnicodeBlock ALCHEMICAL_SYMBOLS =
            new UnicodeBlock("ALCHEMICAL_SYMBOLS", 0x1F700, 0x1F77F);

        /** The CJK Unified Ideographs Extension-D Unicode 6.0 block. */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D =
            new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D", 0x2B740, 0x2B81F);

        /*
         * All of the UnicodeBlocks above, in the icu4c UBlock enum order.
         */
        private static UnicodeBlock[] BLOCKS = new UnicodeBlock[] {
            null, // icu4c numbers blocks starting at 1, so index 0 should be null.

            UnicodeBlock.BASIC_LATIN,
            UnicodeBlock.LATIN_1_SUPPLEMENT,
            UnicodeBlock.LATIN_EXTENDED_A,
            UnicodeBlock.LATIN_EXTENDED_B,
            UnicodeBlock.IPA_EXTENSIONS,
            UnicodeBlock.SPACING_MODIFIER_LETTERS,
            UnicodeBlock.COMBINING_DIACRITICAL_MARKS,
            UnicodeBlock.GREEK,
            UnicodeBlock.CYRILLIC,
            UnicodeBlock.ARMENIAN,
            UnicodeBlock.HEBREW,
            UnicodeBlock.ARABIC,
            UnicodeBlock.SYRIAC,
            UnicodeBlock.THAANA,
            UnicodeBlock.DEVANAGARI,
            UnicodeBlock.BENGALI,
            UnicodeBlock.GURMUKHI,
            UnicodeBlock.GUJARATI,
            UnicodeBlock.ORIYA,
            UnicodeBlock.TAMIL,
            UnicodeBlock.TELUGU,
            UnicodeBlock.KANNADA,
            UnicodeBlock.MALAYALAM,
            UnicodeBlock.SINHALA,
            UnicodeBlock.THAI,
            UnicodeBlock.LAO,
            UnicodeBlock.TIBETAN,
            UnicodeBlock.MYANMAR,
            UnicodeBlock.GEORGIAN,
            UnicodeBlock.HANGUL_JAMO,
            UnicodeBlock.ETHIOPIC,
            UnicodeBlock.CHEROKEE,
            UnicodeBlock.UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS,
            UnicodeBlock.OGHAM,
            UnicodeBlock.RUNIC,
            UnicodeBlock.KHMER,
            UnicodeBlock.MONGOLIAN,
            UnicodeBlock.LATIN_EXTENDED_ADDITIONAL,
            UnicodeBlock.GREEK_EXTENDED,
            UnicodeBlock.GENERAL_PUNCTUATION,
            UnicodeBlock.SUPERSCRIPTS_AND_SUBSCRIPTS,
            UnicodeBlock.CURRENCY_SYMBOLS,
            UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS,
            UnicodeBlock.LETTERLIKE_SYMBOLS,
            UnicodeBlock.NUMBER_FORMS,
            UnicodeBlock.ARROWS,
            UnicodeBlock.MATHEMATICAL_OPERATORS,
            UnicodeBlock.MISCELLANEOUS_TECHNICAL,
            UnicodeBlock.CONTROL_PICTURES,
            UnicodeBlock.OPTICAL_CHARACTER_RECOGNITION,
            UnicodeBlock.ENCLOSED_ALPHANUMERICS,
            UnicodeBlock.BOX_DRAWING,
            UnicodeBlock.BLOCK_ELEMENTS,
            UnicodeBlock.GEOMETRIC_SHAPES,
            UnicodeBlock.MISCELLANEOUS_SYMBOLS,
            UnicodeBlock.DINGBATS,
            UnicodeBlock.BRAILLE_PATTERNS,
            UnicodeBlock.CJK_RADICALS_SUPPLEMENT,
            UnicodeBlock.KANGXI_RADICALS,
            UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS,
            UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION,
            UnicodeBlock.HIRAGANA,
            UnicodeBlock.KATAKANA,
            UnicodeBlock.BOPOMOFO,
            UnicodeBlock.HANGUL_COMPATIBILITY_JAMO,
            UnicodeBlock.KANBUN,
            UnicodeBlock.BOPOMOFO_EXTENDED,
            UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS,
            UnicodeBlock.CJK_COMPATIBILITY,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
            UnicodeBlock.YI_SYLLABLES,
            UnicodeBlock.YI_RADICALS,
            UnicodeBlock.HANGUL_SYLLABLES,
            UnicodeBlock.HIGH_SURROGATES,
            UnicodeBlock.HIGH_PRIVATE_USE_SURROGATES,
            UnicodeBlock.LOW_SURROGATES,
            UnicodeBlock.PRIVATE_USE_AREA,
            UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
            UnicodeBlock.ALPHABETIC_PRESENTATION_FORMS,
            UnicodeBlock.ARABIC_PRESENTATION_FORMS_A,
            UnicodeBlock.COMBINING_HALF_MARKS,
            UnicodeBlock.CJK_COMPATIBILITY_FORMS,
            UnicodeBlock.SMALL_FORM_VARIANTS,
            UnicodeBlock.ARABIC_PRESENTATION_FORMS_B,
            UnicodeBlock.SPECIALS,
            UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS,

            // Unicode 3.1.
            UnicodeBlock.OLD_ITALIC,
            UnicodeBlock.GOTHIC,
            UnicodeBlock.DESERET,
            UnicodeBlock.BYZANTINE_MUSICAL_SYMBOLS,
            UnicodeBlock.MUSICAL_SYMBOLS,
            UnicodeBlock.MATHEMATICAL_ALPHANUMERIC_SYMBOLS,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
            UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT,
            UnicodeBlock.TAGS,

            // Unicode 3.2.
            UnicodeBlock.CYRILLIC_SUPPLEMENTARY,
            UnicodeBlock.TAGALOG,
            UnicodeBlock.HANUNOO,
            UnicodeBlock.BUHID,
            UnicodeBlock.TAGBANWA,
            UnicodeBlock.MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A,
            UnicodeBlock.SUPPLEMENTAL_ARROWS_A,
            UnicodeBlock.SUPPLEMENTAL_ARROWS_B,
            UnicodeBlock.MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B,
            UnicodeBlock.SUPPLEMENTAL_MATHEMATICAL_OPERATORS,
            UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS,
            UnicodeBlock.VARIATION_SELECTORS,
            UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_A,
            UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_B,

            // Unicode 4.0.
            UnicodeBlock.LIMBU,
            UnicodeBlock.TAI_LE,
            UnicodeBlock.KHMER_SYMBOLS,
            UnicodeBlock.PHONETIC_EXTENSIONS,
            UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_ARROWS,
            UnicodeBlock.YIJING_HEXAGRAM_SYMBOLS,
            UnicodeBlock.LINEAR_B_SYLLABARY,
            UnicodeBlock.LINEAR_B_IDEOGRAMS,
            UnicodeBlock.AEGEAN_NUMBERS,
            UnicodeBlock.UGARITIC,
            UnicodeBlock.SHAVIAN,
            UnicodeBlock.OSMANYA,
            UnicodeBlock.CYPRIOT_SYLLABARY,
            UnicodeBlock.TAI_XUAN_JING_SYMBOLS,
            UnicodeBlock.VARIATION_SELECTORS_SUPPLEMENT,

            // Unicode 4.1.
            UnicodeBlock.ANCIENT_GREEK_MUSICAL_NOTATION,
            UnicodeBlock.ANCIENT_GREEK_NUMBERS,
            UnicodeBlock.ARABIC_SUPPLEMENT,
            UnicodeBlock.BUGINESE,
            UnicodeBlock.CJK_STROKES,
            UnicodeBlock.COMBINING_DIACRITICAL_MARKS_SUPPLEMENT,
            UnicodeBlock.COPTIC,
            UnicodeBlock.ETHIOPIC_EXTENDED,
            UnicodeBlock.ETHIOPIC_SUPPLEMENT,
            UnicodeBlock.GEORGIAN_SUPPLEMENT,
            UnicodeBlock.GLAGOLITIC,
            UnicodeBlock.KHAROSHTHI,
            UnicodeBlock.MODIFIER_TONE_LETTERS,
            UnicodeBlock.NEW_TAI_LUE,
            UnicodeBlock.OLD_PERSIAN,
            UnicodeBlock.PHONETIC_EXTENSIONS_SUPPLEMENT,
            UnicodeBlock.SUPPLEMENTAL_PUNCTUATION,
            UnicodeBlock.SYLOTI_NAGRI,
            UnicodeBlock.TIFINAGH,
            UnicodeBlock.VERTICAL_FORMS,

            // Unicode 5.0.
            UnicodeBlock.NKO,
            UnicodeBlock.BALINESE,
            UnicodeBlock.LATIN_EXTENDED_C,
            UnicodeBlock.LATIN_EXTENDED_D,
            UnicodeBlock.PHAGS_PA,
            UnicodeBlock.PHOENICIAN,
            UnicodeBlock.CUNEIFORM,
            UnicodeBlock.CUNEIFORM_NUMBERS_AND_PUNCTUATION,
            UnicodeBlock.COUNTING_ROD_NUMERALS,

            // Unicode 5.1.
            UnicodeBlock.SUNDANESE,
            UnicodeBlock.LEPCHA,
            UnicodeBlock.OL_CHIKI,
            UnicodeBlock.CYRILLIC_EXTENDED_A,
            UnicodeBlock.VAI,
            UnicodeBlock.CYRILLIC_EXTENDED_B,
            UnicodeBlock.SAURASHTRA,
            UnicodeBlock.KAYAH_LI,
            UnicodeBlock.REJANG,
            UnicodeBlock.CHAM,
            UnicodeBlock.ANCIENT_SYMBOLS,
            UnicodeBlock.PHAISTOS_DISC,
            UnicodeBlock.LYCIAN,
            UnicodeBlock.CARIAN,
            UnicodeBlock.LYDIAN,
            UnicodeBlock.MAHJONG_TILES,
            UnicodeBlock.DOMINO_TILES,

            // Unicode 5.2.
            UnicodeBlock.SAMARITAN,
            UnicodeBlock.UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED,
            UnicodeBlock.TAI_THAM,
            UnicodeBlock.VEDIC_EXTENSIONS,
            UnicodeBlock.LISU,
            UnicodeBlock.BAMUM,
            UnicodeBlock.COMMON_INDIC_NUMBER_FORMS,
            UnicodeBlock.DEVANAGARI_EXTENDED,
            UnicodeBlock.HANGUL_JAMO_EXTENDED_A,
            UnicodeBlock.JAVANESE,
            UnicodeBlock.MYANMAR_EXTENDED_A,
            UnicodeBlock.TAI_VIET,
            UnicodeBlock.MEETEI_MAYEK,
            UnicodeBlock.HANGUL_JAMO_EXTENDED_B,
            UnicodeBlock.IMPERIAL_ARAMAIC,
            UnicodeBlock.OLD_SOUTH_ARABIAN,
            UnicodeBlock.AVESTAN,
            UnicodeBlock.INSCRIPTIONAL_PARTHIAN,
            UnicodeBlock.INSCRIPTIONAL_PAHLAVI,
            UnicodeBlock.OLD_TURKIC,
            UnicodeBlock.RUMI_NUMERAL_SYMBOLS,
            UnicodeBlock.KAITHI,
            UnicodeBlock.EGYPTIAN_HIEROGLYPHS,
            UnicodeBlock.ENCLOSED_ALPHANUMERIC_SUPPLEMENT,
            UnicodeBlock.ENCLOSED_IDEOGRAPHIC_SUPPLEMENT,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C,

            // Unicode 6.0.
            UnicodeBlock.MANDAIC,
            UnicodeBlock.BATAK,
            UnicodeBlock.ETHIOPIC_EXTENDED_A,
            UnicodeBlock.BRAHMI,
            UnicodeBlock.BAMUM_SUPPLEMENT,
            UnicodeBlock.KANA_SUPPLEMENT,
            UnicodeBlock.PLAYING_CARDS,
            UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS,
            UnicodeBlock.EMOTICONS,
            UnicodeBlock.TRANSPORT_AND_MAP_SYMBOLS,
            UnicodeBlock.ALCHEMICAL_SYMBOLS,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D,
        };

        private static final Map<String, UnicodeBlock> blockAliasMap =
            new HashMap<String, UnicodeBlock>();

        static {
          blockAliasMap.put("AEGEAN_NUMBERS", UnicodeBlock.AEGEAN_NUMBERS);
          blockAliasMap.put("ALCHEMICAL", UnicodeBlock.ALCHEMICAL_SYMBOLS);
          blockAliasMap.put("ALPHABETIC_PF", UnicodeBlock.ALPHABETIC_PRESENTATION_FORMS);
          blockAliasMap.put("ANCIENT_GREEK_MUSIC", UnicodeBlock.ANCIENT_GREEK_MUSICAL_NOTATION);
          blockAliasMap.put("ANCIENT_GREEK_NUMBERS", UnicodeBlock.ANCIENT_GREEK_NUMBERS);
          blockAliasMap.put("ANCIENT_SYMBOLS", UnicodeBlock.ANCIENT_SYMBOLS);
          blockAliasMap.put("ARABIC", UnicodeBlock.ARABIC);
          blockAliasMap.put("ARABIC_PF_A", UnicodeBlock.ARABIC_PRESENTATION_FORMS_A);
          blockAliasMap.put("ARABIC_PF_B", UnicodeBlock.ARABIC_PRESENTATION_FORMS_B);
          blockAliasMap.put("ARABIC_PRESENTATION_FORMS-A", UnicodeBlock.ARABIC_PRESENTATION_FORMS_A);
          blockAliasMap.put("ARABIC_SUP", UnicodeBlock.ARABIC_SUPPLEMENT);
          blockAliasMap.put("ARMENIAN", UnicodeBlock.ARMENIAN);
          blockAliasMap.put("ARROWS", UnicodeBlock.ARROWS);
          blockAliasMap.put("ASCII", UnicodeBlock.BASIC_LATIN);
          blockAliasMap.put("AVESTAN", UnicodeBlock.AVESTAN);
          blockAliasMap.put("BALINESE", UnicodeBlock.BALINESE);
          blockAliasMap.put("BAMUM", UnicodeBlock.BAMUM);
          blockAliasMap.put("BAMUM_SUP", UnicodeBlock.BAMUM_SUPPLEMENT);
          blockAliasMap.put("BATAK", UnicodeBlock.BATAK);
          blockAliasMap.put("BENGALI", UnicodeBlock.BENGALI);
          blockAliasMap.put("BLOCK_ELEMENTS", UnicodeBlock.BLOCK_ELEMENTS);
          blockAliasMap.put("BOPOMOFO", UnicodeBlock.BOPOMOFO);
          blockAliasMap.put("BOPOMOFO_EXT", UnicodeBlock.BOPOMOFO_EXTENDED);
          blockAliasMap.put("BOX_DRAWING", UnicodeBlock.BOX_DRAWING);
          blockAliasMap.put("BRAHMI", UnicodeBlock.BRAHMI);
          blockAliasMap.put("BRAILLE", UnicodeBlock.BRAILLE_PATTERNS);
          blockAliasMap.put("BUGINESE", UnicodeBlock.BUGINESE);
          blockAliasMap.put("BUHID", UnicodeBlock.BUHID);
          blockAliasMap.put("BYZANTINE_MUSIC", UnicodeBlock.BYZANTINE_MUSICAL_SYMBOLS);
          blockAliasMap.put("CARIAN", UnicodeBlock.CARIAN);
          blockAliasMap.put("CHAM", UnicodeBlock.CHAM);
          blockAliasMap.put("CHEROKEE", UnicodeBlock.CHEROKEE);
          blockAliasMap.put("CJK", UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
          blockAliasMap.put("CJK_COMPAT", UnicodeBlock.CJK_COMPATIBILITY);
          blockAliasMap.put("CJK_COMPAT_FORMS", UnicodeBlock.CJK_COMPATIBILITY_FORMS);
          blockAliasMap.put("CJK_COMPAT_IDEOGRAPHS", UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
          blockAliasMap.put("CJK_COMPAT_IDEOGRAPHS_SUP", UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT);
          blockAliasMap.put("CJK_EXT_A", UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
          blockAliasMap.put("CJK_EXT_B", UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);
          blockAliasMap.put("CJK_EXT_C", UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C);
          blockAliasMap.put("CJK_EXT_D", UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D);
          blockAliasMap.put("CJK_RADICALS_SUP", UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
          blockAliasMap.put("CJK_STROKES", UnicodeBlock.CJK_STROKES);
          blockAliasMap.put("CJK_SYMBOLS", UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
          blockAliasMap.put("COMBINING_DIACRITICAL_MARKS_FOR_SYMBOLS", UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS);
          blockAliasMap.put("COMPAT_JAMO", UnicodeBlock.HANGUL_COMPATIBILITY_JAMO);
          blockAliasMap.put("CONTROL_PICTURES", UnicodeBlock.CONTROL_PICTURES);
          blockAliasMap.put("COPTIC", UnicodeBlock.COPTIC);
          blockAliasMap.put("COUNTING_ROD", UnicodeBlock.COUNTING_ROD_NUMERALS);
          blockAliasMap.put("CUNEIFORM", UnicodeBlock.CUNEIFORM);
          blockAliasMap.put("CUNEIFORM_NUMBERS", UnicodeBlock.CUNEIFORM_NUMBERS_AND_PUNCTUATION);
          blockAliasMap.put("CURRENCY_SYMBOLS", UnicodeBlock.CURRENCY_SYMBOLS);
          blockAliasMap.put("CYPRIOT_SYLLABARY", UnicodeBlock.CYPRIOT_SYLLABARY);
          blockAliasMap.put("CYRILLIC", UnicodeBlock.CYRILLIC);
          blockAliasMap.put("CYRILLIC_EXT_A", UnicodeBlock.CYRILLIC_EXTENDED_A);
          blockAliasMap.put("CYRILLIC_EXT_B", UnicodeBlock.CYRILLIC_EXTENDED_B);
          blockAliasMap.put("CYRILLIC_SUP", UnicodeBlock.CYRILLIC_SUPPLEMENTARY);
          blockAliasMap.put("CYRILLIC_SUPPLEMENT", UnicodeBlock.CYRILLIC_SUPPLEMENTARY);
          blockAliasMap.put("DESERET", UnicodeBlock.DESERET);
          blockAliasMap.put("DEVANAGARI", UnicodeBlock.DEVANAGARI);
          blockAliasMap.put("DEVANAGARI_EXT", UnicodeBlock.DEVANAGARI_EXTENDED);
          blockAliasMap.put("DIACRITICALS", UnicodeBlock.COMBINING_DIACRITICAL_MARKS);
          blockAliasMap.put("DIACRITICALS_FOR_SYMBOLS", UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS);
          blockAliasMap.put("DIACRITICALS_FOR_SYMBOLS", UnicodeBlock.COMBINING_DIACRITICAL_MARKS_SUPPLEMENT);
          blockAliasMap.put("DIACRITICALS_SUP", UnicodeBlock.COMBINING_DIACRITICAL_MARKS_SUPPLEMENT);
          blockAliasMap.put("DINGBATS", UnicodeBlock.DINGBATS);
          blockAliasMap.put("DOMINO", UnicodeBlock.DOMINO_TILES);
          blockAliasMap.put("EGYPTIAN_HIEROGLYPHS", UnicodeBlock.EGYPTIAN_HIEROGLYPHS);
          blockAliasMap.put("EMOTICONS", UnicodeBlock.EMOTICONS);
          blockAliasMap.put("ENCLOSED_ALPHANUM", UnicodeBlock.ENCLOSED_ALPHANUMERICS);
          blockAliasMap.put("ENCLOSED_ALPHANUM_SUP", UnicodeBlock.ENCLOSED_ALPHANUMERIC_SUPPLEMENT);
          blockAliasMap.put("ENCLOSED_CJK", UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS);
          blockAliasMap.put("ENCLOSED_IDEOGRAPHIC_SUP", UnicodeBlock.ENCLOSED_IDEOGRAPHIC_SUPPLEMENT);
          blockAliasMap.put("ETHIOPIC", UnicodeBlock.ETHIOPIC);
          blockAliasMap.put("ETHIOPIC_EXT", UnicodeBlock.ETHIOPIC_EXTENDED);
          blockAliasMap.put("ETHIOPIC_EXT_A", UnicodeBlock.ETHIOPIC_EXTENDED_A);
          blockAliasMap.put("ETHIOPIC_SUP", UnicodeBlock.ETHIOPIC_SUPPLEMENT);
          blockAliasMap.put("GEOMETRIC_SHAPES", UnicodeBlock.GEOMETRIC_SHAPES);
          blockAliasMap.put("GEORGIAN", UnicodeBlock.GEORGIAN);
          blockAliasMap.put("GEORGIAN_SUP", UnicodeBlock.GEORGIAN_SUPPLEMENT);
          blockAliasMap.put("GLAGOLITIC", UnicodeBlock.GLAGOLITIC);
          blockAliasMap.put("GOTHIC", UnicodeBlock.GOTHIC);
          blockAliasMap.put("GREEK_AND_COPTIC", UnicodeBlock.GREEK);
          blockAliasMap.put("GREEK_EXT", UnicodeBlock.GREEK_EXTENDED);
          blockAliasMap.put("GUJARATI", UnicodeBlock.GUJARATI);
          blockAliasMap.put("GURMUKHI", UnicodeBlock.GURMUKHI);
          blockAliasMap.put("HALF_AND_FULL_FORMS", UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
          blockAliasMap.put("HALF_MARKS", UnicodeBlock.COMBINING_HALF_MARKS);
          blockAliasMap.put("HANGUL", UnicodeBlock.HANGUL_SYLLABLES);
          blockAliasMap.put("HANUNOO", UnicodeBlock.HANUNOO);
          blockAliasMap.put("HEBREW", UnicodeBlock.HEBREW);
          blockAliasMap.put("HIGH_PU_SURROGATES", UnicodeBlock.HIGH_PRIVATE_USE_SURROGATES);
          blockAliasMap.put("HIGH_SURROGATES", UnicodeBlock.HIGH_SURROGATES);
          blockAliasMap.put("HIRAGANA", UnicodeBlock.HIRAGANA);
          blockAliasMap.put("IDC", UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS);
          blockAliasMap.put("IMPERIAL_ARAMAIC", UnicodeBlock.IMPERIAL_ARAMAIC);
          blockAliasMap.put("INDIC_NUMBER_FORMS", UnicodeBlock.COMMON_INDIC_NUMBER_FORMS);
          blockAliasMap.put("INSCRIPTIONAL_PAHLAVI", UnicodeBlock.INSCRIPTIONAL_PAHLAVI);
          blockAliasMap.put("INSCRIPTIONAL_PARTHIAN", UnicodeBlock.INSCRIPTIONAL_PARTHIAN);
          blockAliasMap.put("IPA_EXT", UnicodeBlock.IPA_EXTENSIONS);
          blockAliasMap.put("JAMO", UnicodeBlock.HANGUL_JAMO);
          blockAliasMap.put("JAMO_EXT_A", UnicodeBlock.HANGUL_JAMO_EXTENDED_A);
          blockAliasMap.put("JAMO_EXT_B", UnicodeBlock.HANGUL_JAMO_EXTENDED_B);
          blockAliasMap.put("JAVANESE", UnicodeBlock.JAVANESE);
          blockAliasMap.put("KAITHI", UnicodeBlock.KAITHI);
          blockAliasMap.put("KANA_SUP", UnicodeBlock.KANA_SUPPLEMENT);
          blockAliasMap.put("KANBUN", UnicodeBlock.KANBUN);
          blockAliasMap.put("KANGXI", UnicodeBlock.KANGXI_RADICALS);
          blockAliasMap.put("KANNADA", UnicodeBlock.KANNADA);
          blockAliasMap.put("KATAKANA", UnicodeBlock.KATAKANA);
          blockAliasMap.put("KATAKANA_EXT", UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS);
          blockAliasMap.put("KAYAH_LI", UnicodeBlock.KAYAH_LI);
          blockAliasMap.put("KHAROSHTHI", UnicodeBlock.KHAROSHTHI);
          blockAliasMap.put("KHMER", UnicodeBlock.KHMER);
          blockAliasMap.put("KHMER_SYMBOLS", UnicodeBlock.KHMER_SYMBOLS);
          blockAliasMap.put("LAO", UnicodeBlock.LAO);
          blockAliasMap.put("LATIN_1_SUP", UnicodeBlock.LATIN_1_SUPPLEMENT);
          blockAliasMap.put("LATIN_EXT_A", UnicodeBlock.LATIN_EXTENDED_A);
          blockAliasMap.put("LATIN_EXT_ADDITIONAL", UnicodeBlock.LATIN_EXTENDED_ADDITIONAL);
          blockAliasMap.put("LATIN_EXT_B", UnicodeBlock.LATIN_EXTENDED_B);
          blockAliasMap.put("LATIN_EXT_C", UnicodeBlock.LATIN_EXTENDED_C);
          blockAliasMap.put("LATIN_EXT_D", UnicodeBlock.LATIN_EXTENDED_D);
          blockAliasMap.put("LEPCHA", UnicodeBlock.LEPCHA);
          blockAliasMap.put("LETTERLIKE_SYMBOLS", UnicodeBlock.LETTERLIKE_SYMBOLS);
          blockAliasMap.put("LIMBU", UnicodeBlock.LIMBU);
          blockAliasMap.put("LINEAR_B_IDEOGRAMS", UnicodeBlock.LINEAR_B_IDEOGRAMS);
          blockAliasMap.put("LINEAR_B_SYLLABARY", UnicodeBlock.LINEAR_B_SYLLABARY);
          blockAliasMap.put("LISU", UnicodeBlock.LISU);
          blockAliasMap.put("LOW_SURROGATES", UnicodeBlock.LOW_SURROGATES);
          blockAliasMap.put("LYCIAN", UnicodeBlock.LYCIAN);
          blockAliasMap.put("LYDIAN", UnicodeBlock.LYDIAN);
          blockAliasMap.put("MAHJONG", UnicodeBlock.MAHJONG_TILES);
          blockAliasMap.put("MALAYALAM", UnicodeBlock.MALAYALAM);
          blockAliasMap.put("MANDAIC", UnicodeBlock.MANDAIC);
          blockAliasMap.put("MATH_ALPHANUM", UnicodeBlock.MATHEMATICAL_ALPHANUMERIC_SYMBOLS);
          blockAliasMap.put("MATH_OPERATORS", UnicodeBlock.MATHEMATICAL_OPERATORS);
          blockAliasMap.put("MEETEI_MAYEK", UnicodeBlock.MEETEI_MAYEK);
          blockAliasMap.put("MISC_ARROWS", UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_ARROWS);
          blockAliasMap.put("MISC_MATH_SYMBOLS_A", UnicodeBlock.MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A);
          blockAliasMap.put("MISC_MATH_SYMBOLS_B", UnicodeBlock.MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B);
          blockAliasMap.put("MISC_PICTOGRAPHS", UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS);
          blockAliasMap.put("MISC_SYMBOLS", UnicodeBlock.MISCELLANEOUS_SYMBOLS);
          blockAliasMap.put("MISC_TECHNICAL", UnicodeBlock.MISCELLANEOUS_TECHNICAL);
          blockAliasMap.put("MODIFIER_LETTERS", UnicodeBlock.SPACING_MODIFIER_LETTERS);
          blockAliasMap.put("MODIFIER_TONE_LETTERS", UnicodeBlock.MODIFIER_TONE_LETTERS);
          blockAliasMap.put("MONGOLIAN", UnicodeBlock.MONGOLIAN);
          blockAliasMap.put("MUSIC", UnicodeBlock.MUSICAL_SYMBOLS);
          blockAliasMap.put("MYANMAR", UnicodeBlock.MYANMAR);
          blockAliasMap.put("MYANMAR_EXT_A", UnicodeBlock.MYANMAR_EXTENDED_A);
          blockAliasMap.put("NEW_TAI_LUE", UnicodeBlock.NEW_TAI_LUE);
          blockAliasMap.put("NKO", UnicodeBlock.NKO);
          blockAliasMap.put("NUMBER_FORMS", UnicodeBlock.NUMBER_FORMS);
          blockAliasMap.put("OCR", UnicodeBlock.OPTICAL_CHARACTER_RECOGNITION);
          blockAliasMap.put("OGHAM", UnicodeBlock.OGHAM);
          blockAliasMap.put("OL_CHIKI", UnicodeBlock.OL_CHIKI);
          blockAliasMap.put("OLD_ITALIC", UnicodeBlock.OLD_ITALIC);
          blockAliasMap.put("OLD_PERSIAN", UnicodeBlock.OLD_PERSIAN);
          blockAliasMap.put("OLD_SOUTH_ARABIAN", UnicodeBlock.OLD_SOUTH_ARABIAN);
          blockAliasMap.put("OLD_TURKIC", UnicodeBlock.OLD_TURKIC);
          blockAliasMap.put("ORIYA", UnicodeBlock.ORIYA);
          blockAliasMap.put("OSMANYA", UnicodeBlock.OSMANYA);
          blockAliasMap.put("PHAGS_PA", UnicodeBlock.PHAGS_PA);
          blockAliasMap.put("PHAISTOS", UnicodeBlock.PHAISTOS_DISC);
          blockAliasMap.put("PHOENICIAN", UnicodeBlock.PHOENICIAN);
          blockAliasMap.put("PHONETIC_EXT", UnicodeBlock.PHONETIC_EXTENSIONS);
          blockAliasMap.put("PHONETIC_EXT_SUP", UnicodeBlock.PHONETIC_EXTENSIONS_SUPPLEMENT);
          blockAliasMap.put("PLAYING_CARDS", UnicodeBlock.PLAYING_CARDS);
          blockAliasMap.put("PUA", UnicodeBlock.PRIVATE_USE_AREA);
          blockAliasMap.put("PUNCTUATION", UnicodeBlock.GENERAL_PUNCTUATION);
          blockAliasMap.put("REJANG", UnicodeBlock.REJANG);
          blockAliasMap.put("RUMI", UnicodeBlock.RUMI_NUMERAL_SYMBOLS);
          blockAliasMap.put("RUNIC", UnicodeBlock.RUNIC);
          blockAliasMap.put("SAMARITAN", UnicodeBlock.SAMARITAN);
          blockAliasMap.put("SAURASHTRA", UnicodeBlock.SAURASHTRA);
          blockAliasMap.put("SHAVIAN", UnicodeBlock.SHAVIAN);
          blockAliasMap.put("SINHALA", UnicodeBlock.SINHALA);
          blockAliasMap.put("SMALL_FORMS", UnicodeBlock.SMALL_FORM_VARIANTS);
          blockAliasMap.put("SPECIALS", UnicodeBlock.SPECIALS);
          blockAliasMap.put("SUNDANESE", UnicodeBlock.SUNDANESE);
          blockAliasMap.put("SUP_ARROWS_A", UnicodeBlock.SUPPLEMENTAL_ARROWS_A);
          blockAliasMap.put("SUP_ARROWS_B", UnicodeBlock.SUPPLEMENTAL_ARROWS_B);
          blockAliasMap.put("SUP_MATH_OPERATORS", UnicodeBlock.SUPPLEMENTAL_MATHEMATICAL_OPERATORS);
          blockAliasMap.put("SUP_PUA_A", UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_A);
          blockAliasMap.put("SUP_PUA_B", UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_B);
          blockAliasMap.put("SUP_PUNCTUATION", UnicodeBlock.SUPPLEMENTAL_PUNCTUATION);
          blockAliasMap.put("SUPER_AND_SUB", UnicodeBlock.SUPERSCRIPTS_AND_SUBSCRIPTS);
          blockAliasMap.put("SYLOTI_NAGRI", UnicodeBlock.SYLOTI_NAGRI);
          blockAliasMap.put("SYRIAC", UnicodeBlock.SYRIAC);
          blockAliasMap.put("TAGALOG", UnicodeBlock.TAGALOG);
          blockAliasMap.put("TAGBANWA", UnicodeBlock.TAGBANWA);
          blockAliasMap.put("TAGS", UnicodeBlock.TAGS);
          blockAliasMap.put("TAI_LE", UnicodeBlock.TAI_LE);
          blockAliasMap.put("TAI_THAM", UnicodeBlock.TAI_THAM);
          blockAliasMap.put("TAI_VIET", UnicodeBlock.TAI_VIET);
          blockAliasMap.put("TAI_XUAN_JING", UnicodeBlock.TAI_XUAN_JING_SYMBOLS);
          blockAliasMap.put("TAMIL", UnicodeBlock.TAMIL);
          blockAliasMap.put("TELUGU", UnicodeBlock.TELUGU);
          blockAliasMap.put("THAANA", UnicodeBlock.THAANA);
          blockAliasMap.put("THAI", UnicodeBlock.THAI);
          blockAliasMap.put("TIBETAN", UnicodeBlock.TIBETAN);
          blockAliasMap.put("TIFINAGH", UnicodeBlock.TIFINAGH);
          blockAliasMap.put("TRANSPORT_AND_MAP", UnicodeBlock.TRANSPORT_AND_MAP_SYMBOLS);
          blockAliasMap.put("UCAS", UnicodeBlock.UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS);
          blockAliasMap.put("UCAS_EXT", UnicodeBlock.UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED);
          blockAliasMap.put("UGARITIC", UnicodeBlock.UGARITIC);
          blockAliasMap.put("VAI", UnicodeBlock.VAI);
          blockAliasMap.put("VEDIC_EXT", UnicodeBlock.VEDIC_EXTENSIONS);
          blockAliasMap.put("VERTICAL_FORMS", UnicodeBlock.VERTICAL_FORMS);
          blockAliasMap.put("VS", UnicodeBlock.VARIATION_SELECTORS);
          blockAliasMap.put("VS_SUP", UnicodeBlock.VARIATION_SELECTORS_SUPPLEMENT);
          blockAliasMap.put("YI_RADICALS", UnicodeBlock.YI_RADICALS);
          blockAliasMap.put("YI_SYLLABLES", UnicodeBlock.YI_SYLLABLES);
          blockAliasMap.put("YIJING", UnicodeBlock.YIJING_HEXAGRAM_SYMBOLS);
        }

        /**
         * Returns the Unicode block for the given block name, or null if there is no
         * such block.
         *
         * <p>Block names may be one of the following:
         * <ul>
         * <li>Canonical block name, as defined by the Unicode specification;
         * case-insensitive.</li>
         * <li>Canonical block name without any spaces, as defined by the
         * Unicode specification; case-insensitive.</li>
         * <li>A {@code UnicodeBlock} constant identifier. This is determined by
         * converting the canonical name to uppercase and replacing all spaces and hyphens
         * with underscores.</li>
         * </ul>
         *
         * @throws NullPointerException
         *             if {@code blockName == null}.
         * @throws IllegalArgumentException
         *             if {@code blockName} is not the name of any known block.
         * @since 1.5
         */
        public static UnicodeBlock forName(String blockName) {
            if (blockName == null) {
                throw new NullPointerException("blockName == null");
            }
            int block = forNameImpl(blockName);
            if (block == -1) {
                throw new IllegalArgumentException("Unknown block: " + blockName);
            }
            return BLOCKS[block];
        }

        /**
         * Returns the Unicode block containing the given code point, or null if the
         * code point does not belong to any known block.
         */
        public static UnicodeBlock of(char c) {
            return of((int) c);
        }

        /**
         * Returns the Unicode block containing the given code point, or null if the
         * code point does not belong to any known block.
         */
        public static UnicodeBlock of(int codePoint) {
            checkValidCodePoint(codePoint);
            int block = ofImpl(codePoint);
            if (block == -1 || block >= BLOCKS.length) {
                return null;
            }
            return BLOCKS[block];
        }

        private UnicodeBlock(String blockName, int rangeStart, int rangeEnd) {
            super(blockName);
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
        }

        private static int forNameImpl(String blockName) {
          // From Unicode Character Database's Blocks.txt:
          // "When comparing block names, casing, whitespace, hyphens, and underbars are ignored."
          blockName = blockName.toUpperCase().replace(' ', '_').replace('-', '_');
          if (blockAliasMap.containsKey(blockName)) {
            blockName = blockAliasMap.get(blockName).toString();
          }
          for (int i = 1; i < BLOCKS.length; i++) {
            if (blockName.equals(BLOCKS[i].toString())) {
              return i;
            }
          }
          return -1;
        }

        private static int ofImpl(int codePoint) {
          for (int i = 1; i < BLOCKS.length; i++) {
            if (codePoint >= BLOCKS[i].rangeStart && codePoint <= BLOCKS[i].rangeEnd) {
              return i;
            }
          }
          return -1;
        }
    }

    /**
     * Constructs a new {@code Character} with the specified primitive char
     * value.
     *
     * @param value
     *            the primitive char value to store in the new instance.
     */
    public Character(char value) {
        this.value = value;
    }

    /**
     * Gets the primitive value of this character.
     *
     * @return this object's primitive value.
     */
    public char charValue() {
        return value;
    }

    private static void checkValidCodePoint(int codePoint) {
        if (!isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException("Invalid code point: " + codePoint);
        }
    }

    /**
     * Compares this object to the specified character object to determine their
     * relative order.
     *
     * @param c
     *            the character object to compare this object to.
     * @return {@code 0} if the value of this character and the value of
     *         {@code c} are equal; a positive value if the value of this
     *         character is greater than the value of {@code c}; a negative
     *         value if the value of this character is less than the value of
     *         {@code c}.
     * @see java.lang.Comparable
     * @since 1.2
     */
    public int compareTo(Character c) {
        return compare(value, c.value);
    }

    /**
     * Compares two {@code char} values.
     * @return 0 if lhs = rhs, less than 0 if lhs &lt; rhs, and greater than 0 if lhs &gt; rhs.
     * @since 1.7
     */
    public static int compare(char lhs, char rhs) {
        return lhs - rhs;
    }

    /**
     * Returns a {@code Character} instance for the {@code char} value passed.
     * <p>
     * If it is not necessary to get a new {@code Character} instance, it is
     * recommended to use this method instead of the constructor, since it
     * maintains a cache of instances which may result in better performance.
     *
     * @param c
     *            the char value for which to get a {@code Character} instance.
     * @return the {@code Character} instance for {@code c}.
     * @since 1.5
     */
    public static Character valueOf(char c) {
        return c < 128 ? smallValueOf(c) : new Character(c);
    }

    private static native Character smallValueOf(char c) /*-[
      static id smallValues[128];
      static dispatch_once_t once;
      dispatch_once(&once, ^{
          for (jchar i = 0; i < 128; i++) {
            smallValues[i] = RETAIN_([[JavaLangCharacter alloc] initWithChar:i]);
          }
      });
      return smallValues[c];
    ]-*/;

    /**
     * Indicates whether {@code codePoint} is a valid Unicode code point.
     *
     * @param codePoint
     *            the code point to test.
     * @return {@code true} if {@code codePoint} is a valid Unicode code point;
     *         {@code false} otherwise.
     * @since 1.5
     */
    public static boolean isValidCodePoint(int codePoint) {
        return (MIN_CODE_POINT <= codePoint && MAX_CODE_POINT >= codePoint);
    }

    /**
     * Indicates whether {@code codePoint} is within the supplementary code
     * point range.
     *
     * @param codePoint
     *            the code point to test.
     * @return {@code true} if {@code codePoint} is within the supplementary
     *         code point range; {@code false} otherwise.
     * @since 1.5
     */
    public static boolean isSupplementaryCodePoint(int codePoint) {
        return (MIN_SUPPLEMENTARY_CODE_POINT <= codePoint && MAX_CODE_POINT >= codePoint);
    }

    /**
     * Indicates whether {@code ch} is a high- (or leading-) surrogate code unit
     * that is used for representing supplementary characters in UTF-16
     * encoding.
     *
     * @param ch
     *            the character to test.
     * @return {@code true} if {@code ch} is a high-surrogate code unit;
     *         {@code false} otherwise.
     * @see #isLowSurrogate(char)
     * @since 1.5
     */
    public static boolean isHighSurrogate(char ch) {
        return (MIN_HIGH_SURROGATE <= ch && MAX_HIGH_SURROGATE >= ch);
    }

    /**
     * Indicates whether {@code ch} is a low- (or trailing-) surrogate code unit
     * that is used for representing supplementary characters in UTF-16
     * encoding.
     *
     * @param ch
     *            the character to test.
     * @return {@code true} if {@code ch} is a low-surrogate code unit;
     *         {@code false} otherwise.
     * @see #isHighSurrogate(char)
     * @since 1.5
     */
    public static boolean isLowSurrogate(char ch) {
        return (MIN_LOW_SURROGATE <= ch && MAX_LOW_SURROGATE >= ch);
    }

    /**
     * Returns true if the given character is a high or low surrogate.
     * @since 1.7
     */
    public static boolean isSurrogate(char ch) {
        return ch >= MIN_SURROGATE && ch <= MAX_SURROGATE;
    }

    /**
     * Indicates whether the specified character pair is a valid surrogate pair.
     *
     * @param high
     *            the high surrogate unit to test.
     * @param low
     *            the low surrogate unit to test.
     * @return {@code true} if {@code high} is a high-surrogate code unit and
     *         {@code low} is a low-surrogate code unit; {@code false}
     *         otherwise.
     * @see #isHighSurrogate(char)
     * @see #isLowSurrogate(char)
     * @since 1.5
     */
    public static boolean isSurrogatePair(char high, char low) {
        return (isHighSurrogate(high) && isLowSurrogate(low));
    }

    /**
     * Calculates the number of {@code char} values required to represent the
     * specified Unicode code point. This method checks if the {@code codePoint}
     * is greater than or equal to {@code 0x10000}, in which case {@code 2} is
     * returned, otherwise {@code 1}. To test if the code point is valid, use
     * the {@link #isValidCodePoint(int)} method.
     *
     * @param codePoint
     *            the code point for which to calculate the number of required
     *            chars.
     * @return {@code 2} if {@code codePoint >= 0x10000}; {@code 1} otherwise.
     * @see #isValidCodePoint(int)
     * @see #isSupplementaryCodePoint(int)
     * @since 1.5
     */
    public static int charCount(int codePoint) {
        return (codePoint >= 0x10000 ? 2 : 1);
    }

    /**
     * Converts a surrogate pair into a Unicode code point. This method assumes
     * that the pair are valid surrogates. If the pair are <i>not</i> valid
     * surrogates, then the result is indeterminate. The
     * {@link #isSurrogatePair(char, char)} method should be used prior to this
     * method to validate the pair.
     *
     * @param high
     *            the high surrogate unit.
     * @param low
     *            the low surrogate unit.
     * @return the Unicode code point corresponding to the surrogate unit pair.
     * @see #isSurrogatePair(char, char)
     * @since 1.5
     */
    public static int toCodePoint(char high, char low) {
        // See RFC 2781, Section 2.2
        // http://www.ietf.org/rfc/rfc2781.txt
        int h = (high & 0x3FF) << 10;
        int l = low & 0x3FF;
        return (h | l) + 0x10000;
    }

    /**
     * Returns the code point at {@code index} in the specified sequence of
     * character units. If the unit at {@code index} is a high-surrogate unit,
     * {@code index + 1} is less than the length of the sequence and the unit at
     * {@code index + 1} is a low-surrogate unit, then the supplementary code
     * point represented by the pair is returned; otherwise the {@code char}
     * value at {@code index} is returned.
     *
     * @param seq
     *            the source sequence of {@code char} units.
     * @param index
     *            the position in {@code seq} from which to retrieve the code
     *            point.
     * @return the Unicode code point or {@code char} value at {@code index} in
     *         {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if the {@code index} is negative or greater than or equal to
     *             the length of {@code seq}.
     * @since 1.5
     */
    public static int codePointAt(CharSequence seq, int index) {
        if (seq == null) {
            throw new NullPointerException("seq == null");
        }
        int len = seq.length();
        if (index < 0 || index >= len) {
            throw new IndexOutOfBoundsException();
        }

        char high = seq.charAt(index++);
        if (index >= len) {
            return high;
        }
        char low = seq.charAt(index);
        if (isSurrogatePair(high, low)) {
            return toCodePoint(high, low);
        }
        return high;
    }

    /*-[
    jint JavaLangCharacter_codePointAtRaw(const jchar *seq, jint index, jint limit) {
      jchar high = seq[index++];
      if (index >= limit) {
        return high;
      }
      jchar low = seq[index];
      if (JavaLangCharacter_isSurrogatePairWithChar_withChar_(high, low)) {
        return JavaLangCharacter_toCodePointWithChar_withChar_(high, low);
      }
      return high;
    }
    ]-*/

    /**
     * Returns the code point at {@code index} in the specified array of
     * character units. If the unit at {@code index} is a high-surrogate unit,
     * {@code index + 1} is less than the length of the array and the unit at
     * {@code index + 1} is a low-surrogate unit, then the supplementary code
     * point represented by the pair is returned; otherwise the {@code char}
     * value at {@code index} is returned.
     *
     * @param seq
     *            the source array of {@code char} units.
     * @param index
     *            the position in {@code seq} from which to retrieve the code
     *            point.
     * @return the Unicode code point or {@code char} value at {@code index} in
     *         {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if the {@code index} is negative or greater than or equal to
     *             the length of {@code seq}.
     * @since 1.5
     */
    public static native int codePointAt(char[] seq, int index) /*-[
      if (seq == nil) {
        @throw [[[JavaLangNullPointerException alloc] initWithNSString:@"seq == null"] autorelease];
      }
      jint len = seq->size_;
      if (index < 0 || index >= len) {
        @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
      }
      return JavaLangCharacter_codePointAtRaw(seq->buffer_, index, len);
    ]-*/;

    /**
     * Returns the code point at {@code index} in the specified array of
     * character units, where {@code index} has to be less than {@code limit}.
     * If the unit at {@code index} is a high-surrogate unit, {@code index + 1}
     * is less than {@code limit} and the unit at {@code index + 1} is a
     * low-surrogate unit, then the supplementary code point represented by the
     * pair is returned; otherwise the {@code char} value at {@code index} is
     * returned.
     *
     * @param seq
     *            the source array of {@code char} units.
     * @param index
     *            the position in {@code seq} from which to get the code point.
     * @param limit
     *            the index after the last unit in {@code seq} that can be used.
     * @return the Unicode code point or {@code char} value at {@code index} in
     *         {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code index < 0}, {@code index >= limit},
     *             {@code limit < 0} or if {@code limit} is greater than the
     *             length of {@code seq}.
     * @since 1.5
     */
    public static native int codePointAt(char[] seq, int index, int limit) /*-[
      if (index < 0 || index >= limit || limit < 0
          || limit > ((IOSCharArray *)nil_chk(seq))->size_) {
        @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
      }
      nil_chk(seq);
      return JavaLangCharacter_codePointAtRaw(seq->buffer_, index, limit);
    ]-*/;

    /**
     * Returns the code point that precedes {@code index} in the specified
     * sequence of character units. If the unit at {@code index - 1} is a
     * low-surrogate unit, {@code index - 2} is not negative and the unit at
     * {@code index - 2} is a high-surrogate unit, then the supplementary code
     * point represented by the pair is returned; otherwise the {@code char}
     * value at {@code index - 1} is returned.
     *
     * @param seq
     *            the source sequence of {@code char} units.
     * @param index
     *            the position in {@code seq} following the code
     *            point that should be returned.
     * @return the Unicode code point or {@code char} value before {@code index}
     *         in {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if the {@code index} is less than 1 or greater than the
     *             length of {@code seq}.
     * @since 1.5
     */
    public static int codePointBefore(CharSequence seq, int index) {
        if (seq == null) {
            throw new NullPointerException("seq == null");
        }
        int len = seq.length();
        if (index < 1 || index > len) {
            throw new IndexOutOfBoundsException();
        }

        char low = seq.charAt(--index);
        if (--index < 0) {
            return low;
        }
        char high = seq.charAt(index);
        if (isSurrogatePair(high, low)) {
            return toCodePoint(high, low);
        }
        return low;
    }

    /*-[
    jint JavaLangCharacter_codePointBeforeRaw(const jchar *seq, jint index, jint start) {
      jchar low = seq[--index];
      if (--index < start) {
        return low;
      }
      jchar high = seq[index];
      if (JavaLangCharacter_isSurrogatePairWithChar_withChar_(high, low)) {
        return JavaLangCharacter_toCodePointWithChar_withChar_(high, low);
      }
      return low;
    }
    ]-*/

    /**
     * Returns the code point that precedes {@code index} in the specified
     * array of character units. If the unit at {@code index - 1} is a
     * low-surrogate unit, {@code index - 2} is not negative and the unit at
     * {@code index - 2} is a high-surrogate unit, then the supplementary code
     * point represented by the pair is returned; otherwise the {@code char}
     * value at {@code index - 1} is returned.
     *
     * @param seq
     *            the source array of {@code char} units.
     * @param index
     *            the position in {@code seq} following the code
     *            point that should be returned.
     * @return the Unicode code point or {@code char} value before {@code index}
     *         in {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if the {@code index} is less than 1 or greater than the
     *             length of {@code seq}.
     * @since 1.5
     */
    public static native int codePointBefore(char[] seq, int index) /*-[
      if (seq == nil) {
        @throw [[[JavaLangNullPointerException alloc] initWithNSString:@"seq == null"] autorelease];
      }
      jint len = seq->size_;
      if (index < 1 || index > len) {
        @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
      }
      return JavaLangCharacter_codePointBeforeRaw(seq->buffer_, index, 0);
    ]-*/;

    /**
     * Returns the code point that precedes the {@code index} in the specified
     * array of character units and is not less than {@code start}. If the unit
     * at {@code index - 1} is a low-surrogate unit, {@code index - 2} is not
     * less than {@code start} and the unit at {@code index - 2} is a
     * high-surrogate unit, then the supplementary code point represented by the
     * pair is returned; otherwise the {@code char} value at {@code index - 1}
     * is returned.
     *
     * @param seq
     *            the source array of {@code char} units.
     * @param index
     *            the position in {@code seq} following the code point that
     *            should be returned.
     * @param start
     *            the index of the first element in {@code seq}.
     * @return the Unicode code point or {@code char} value before {@code index}
     *         in {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if the {@code index <= start}, {@code start < 0},
     *             {@code index} is greater than the length of {@code seq}, or
     *             if {@code start} is equal or greater than the length of
     *             {@code seq}.
     * @since 1.5
     */
    public static native int codePointBefore(char[] seq, int index, int start) /*-[
      if (seq == nil) {
        @throw [[[JavaLangNullPointerException alloc] initWithNSString:@"seq == null"] autorelease];
      }
      jint len = seq->size_;
      if (index <= start || index > len || start < 0 || start >= len) {
        @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
      }
      return JavaLangCharacter_codePointBeforeRaw(seq->buffer_, index, start);
    ]-*/;

    /**
     * Converts the specified Unicode code point into a UTF-16 encoded sequence
     * and copies the value(s) into the char array {@code dst}, starting at
     * index {@code dstIndex}.
     *
     * @param codePoint
     *            the Unicode code point to encode.
     * @param dst
     *            the destination array to copy the encoded value into.
     * @param dstIndex
     *            the index in {@code dst} from where to start copying.
     * @return the number of {@code char} value units copied into {@code dst}.
     * @throws IllegalArgumentException if {@code codePoint} is not a valid code point.
     * @throws NullPointerException
     *             if {@code dst} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code dstIndex} is negative, greater than or equal to
     *             {@code dst.length} or equals {@code dst.length - 1} when
     *             {@code codePoint} is a
     *             {@link #isSupplementaryCodePoint(int) supplementary code point}.
     * @since 1.5
     */
    public static int toChars(int codePoint, char[] dst, int dstIndex) {
        checkValidCodePoint(codePoint);
        if (dst == null) {
            throw new NullPointerException("dst == null");
        }
        if (dstIndex < 0 || dstIndex >= dst.length) {
            throw new IndexOutOfBoundsException();
        }

        if (isSupplementaryCodePoint(codePoint)) {
            if (dstIndex == dst.length - 1) {
                throw new IndexOutOfBoundsException();
            }
            // See RFC 2781, Section 2.1
            // http://www.ietf.org/rfc/rfc2781.txt
            int cpPrime = codePoint - 0x10000;
            int high = 0xD800 | ((cpPrime >> 10) & 0x3FF);
            int low = 0xDC00 | (cpPrime & 0x3FF);
            dst[dstIndex] = (char) high;
            dst[dstIndex + 1] = (char) low;
            return 2;
        }

        dst[dstIndex] = (char) codePoint;
        return 1;
    }

    /**
     * Converts the specified Unicode code point into a UTF-16 encoded sequence
     * and returns it as a char array.
     *
     * @param codePoint
     *            the Unicode code point to encode.
     * @return the UTF-16 encoded char sequence. If {@code codePoint} is a
     *         {@link #isSupplementaryCodePoint(int) supplementary code point},
     *         then the returned array contains two characters, otherwise it
     *         contains just one character.
     * @throws IllegalArgumentException if {@code codePoint} is not a valid code point.
     * @since 1.5
     */
    public static char[] toChars(int codePoint) {
        checkValidCodePoint(codePoint);
        if (isSupplementaryCodePoint(codePoint)) {
            int cpPrime = codePoint - 0x10000;
            int high = 0xD800 | ((cpPrime >> 10) & 0x3FF);
            int low = 0xDC00 | (cpPrime & 0x3FF);
            return new char[] { (char) high, (char) low };
        }
        return new char[] { (char) codePoint };
    }

    /**
     * Counts the number of Unicode code points in the subsequence of the
     * specified character sequence, as delineated by {@code beginIndex} and
     * {@code endIndex}. Any surrogate values with missing pair values will be
     * counted as one code point.
     *
     * @param seq
     *            the {@code CharSequence} to look through.
     * @param beginIndex
     *            the inclusive index to begin counting at.
     * @param endIndex
     *            the exclusive index to stop counting at.
     * @return the number of Unicode code points.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code beginIndex < 0}, {@code beginIndex > endIndex} or
     *             if {@code endIndex} is greater than the length of {@code seq}.
     * @since 1.5
     */
    public static int codePointCount(CharSequence seq, int beginIndex,
            int endIndex) {
        if (seq == null) {
            throw new NullPointerException("seq == null");
        }
        int len = seq.length();
        if (beginIndex < 0 || endIndex > len || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }

        int result = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            char c = seq.charAt(i);
            if (isHighSurrogate(c)) {
                if (++i < endIndex) {
                    c = seq.charAt(i);
                    if (!isLowSurrogate(c)) {
                        result++;
                    }
                }
            }
            result++;
        }
        return result;
    }

    /*-[
    jint JavaLangCharacter_codePointCountRaw(const jchar *seq, jint offset, jint count) {
      jint endIndex = offset + count;
      jint result = 0;
      for (jint i = offset; i < endIndex; i++) {
        jchar c = seq[i];
        if (JavaLangCharacter_isHighSurrogateWithChar_(c)) {
          if (++i < endIndex) {
            c = seq[i];
            if (!JavaLangCharacter_isLowSurrogateWithChar_(c)) {
              result++;
            }
          }
        }
        result++;
      }
      return result;
    }
    ]-*/

    /**
     * Counts the number of Unicode code points in the subsequence of the
     * specified char array, as delineated by {@code offset} and {@code count}.
     * Any surrogate values with missing pair values will be counted as one code
     * point.
     *
     * @param seq
     *            the char array to look through
     * @param offset
     *            the inclusive index to begin counting at.
     * @param count
     *            the number of {@code char} values to look through in
     *            {@code seq}.
     * @return the number of Unicode code points.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0}, {@code count < 0} or if
     *             {@code offset + count} is greater than the length of
     *             {@code seq}.
     * @since 1.5
     */
    public static native int codePointCount(char[] seq, int offset, int count) /*-[
      nil_chk(seq);
      JavaUtilArrays_checkOffsetAndCountWithInt_withInt_withInt_(seq->size_, offset, count);
      return JavaLangCharacter_codePointCountRaw(seq->buffer_, offset, count);
    ]-*/;

    /**
     * Determines the index in the specified character sequence that is offset
     * {@code codePointOffset} code points from {@code index}.
     *
     * @param seq
     *            the character sequence to find the index in.
     * @param index
     *            the start index in {@code seq}.
     * @param codePointOffset
     *            the number of code points to look backwards or forwards; may
     *            be a negative or positive value.
     * @return the index in {@code seq} that is {@code codePointOffset} code
     *         points away from {@code index}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code index < 0}, {@code index} is greater than the
     *             length of {@code seq}, or if there are not enough values in
     *             {@code seq} to skip {@code codePointOffset} code points
     *             forwards or backwards (if {@code codePointOffset} is
     *             negative) from {@code index}.
     * @since 1.5
     */
    public static int offsetByCodePoints(CharSequence seq, int index, int codePointOffset) {
        if (seq == null) {
            throw new NullPointerException("seq == null");
        }
        int len = seq.length();
        if (index < 0 || index > len) {
            throw new IndexOutOfBoundsException();
        }

        if (codePointOffset == 0) {
            return index;
        }

        if (codePointOffset > 0) {
            int codePoints = codePointOffset;
            int i = index;
            while (codePoints > 0) {
                codePoints--;
                if (i >= len) {
                    throw new IndexOutOfBoundsException();
                }
                if (isHighSurrogate(seq.charAt(i))) {
                    int next = i + 1;
                    if (next < len && isLowSurrogate(seq.charAt(next))) {
                        i++;
                    }
                }
                i++;
            }
            return i;
        }

        int codePoints = -codePointOffset;
        int i = index;
        while (codePoints > 0) {
            codePoints--;
            i--;
            if (i < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (isLowSurrogate(seq.charAt(i))) {
                int prev = i - 1;
                if (prev >= 0 && isHighSurrogate(seq.charAt(prev))) {
                    i--;
                }
            }
        }
        return i;
    }

    /*-[
    jint JavaLangCharacter_offsetByCodePointsRaw(
        const jchar *seq, jint start, jint count, jint index, jint codePointOffset) {
      jint end = start + count;
      if (index < start || index > end) {
        @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
      }
      if (codePointOffset == 0) {
        return index;
      }
      if (codePointOffset > 0) {
        jint codePoints = codePointOffset;
        jint i = index;
        while (codePoints > 0) {
          codePoints--;
          if (i >= end) {
            @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
          }
          if (JavaLangCharacter_isHighSurrogateWithChar_(seq[i])) {
            jint next = i + 1;
            if (next < end && JavaLangCharacter_isLowSurrogateWithChar_(seq[next])) {
              i++;
            }
          }
          i++;
        }
        return i;
      }
      jint codePoints = -codePointOffset;
      jint i = index;
      while (codePoints > 0) {
        codePoints--;
        i--;
        if (i < start) {
          @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
        }
        if (JavaLangCharacter_isLowSurrogateWithChar_(seq[i])) {
          jint prev = i - 1;
          if (prev >= start && JavaLangCharacter_isHighSurrogateWithChar_(seq[prev])) {
            i--;
          }
        }
      }
      return i;
    }
    ]-*/

    /**
     * Determines the index in a subsequence of the specified character array
     * that is offset {@code codePointOffset} code points from {@code index}.
     * The subsequence is delineated by {@code start} and {@code count}.
     *
     * @param seq
     *            the character array to find the index in.
     * @param start
     *            the inclusive index that marks the beginning of the
     *            subsequence.
     * @param count
     *            the number of {@code char} values to include within the
     *            subsequence.
     * @param index
     *            the start index in the subsequence of the char array.
     * @param codePointOffset
     *            the number of code points to look backwards or forwards; may
     *            be a negative or positive value.
     * @return the index in {@code seq} that is {@code codePointOffset} code
     *         points away from {@code index}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code count < 0},
     *             {@code index < start}, {@code index > start + count},
     *             {@code start + count} is greater than the length of
     *             {@code seq}, or if there are not enough values in
     *             {@code seq} to skip {@code codePointOffset} code points
     *             forward or backward (if {@code codePointOffset} is
     *             negative) from {@code index}.
     * @since 1.5
     */
    public static native int offsetByCodePoints(char[] seq, int start, int count,
            int index, int codePointOffset) /*-[
      nil_chk(seq);
      JavaUtilArrays_checkOffsetAndCountWithInt_withInt_withInt_(seq->size_, start, count);
      return JavaLangCharacter_offsetByCodePointsRaw(
          seq->buffer_, start, count, index, codePointOffset);
    ]-*/;

    /**
     * Convenience method to determine the value of the specified character
     * {@code c} in the supplied radix. The value of {@code radix} must be
     * between MIN_RADIX and MAX_RADIX.
     *
     * @param c
     *            the character to determine the value of.
     * @param radix
     *            the radix.
     * @return the value of {@code c} in {@code radix} if {@code radix} lies
     *         between {@link #MIN_RADIX} and {@link #MAX_RADIX}; -1 otherwise.
     */
    public static int digit(char c, int radix) {
        return digit((int) c, radix);
    }

    /**
     * Convenience method to determine the value of the character
     * {@code codePoint} in the supplied radix. The value of {@code radix} must
     * be between MIN_RADIX and MAX_RADIX.
     *
     * @param codePoint
     *            the character, including supplementary characters.
     * @param radix
     *            the radix.
     * @return if {@code radix} lies between {@link #MIN_RADIX} and
     *         {@link #MAX_RADIX} then the value of the character in the radix;
     *         -1 otherwise.
     */
    public static native int digit(int codePoint, int radix) /*-[
      return u_digit(codePoint, radix);
    ]-*/;

    /**
     * Compares this object with the specified object and indicates if they are
     * equal. In order to be equal, {@code object} must be an instance of
     * {@code Character} and have the same char value as this object.
     *
     * @param object
     *            the object to compare this double with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Character}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        return (object instanceof Character) && (((Character) object).value == value);
    }

    /**
     * Returns the character which represents the specified digit in the
     * specified radix. The {@code radix} must be between {@code MIN_RADIX} and
     * {@code MAX_RADIX} inclusive; {@code digit} must not be negative and
     * smaller than {@code radix}. If any of these conditions does not hold, 0
     * is returned.
     *
     * @param digit
     *            the integer value.
     * @param radix
     *            the radix.
     * @return the character which represents the {@code digit} in the
     *         {@code radix}.
     */
    public static native char forDigit(int digit, int radix) /*-[
      return u_forDigit(digit, radix);
    ]-*/;

    /**
     * Returns a human-readable name for the given code point,
     * or null if the code point is unassigned.
     *
     * <p>As a fallback mechanism this method returns strings consisting of the Unicode
     * block name (with underscores replaced by spaces), a single space, and the uppercase
     * hex value of the code point, using as few digits as necessary.
     *
     * <p>Examples:
     * <ul>
     * <li>{@code Character.getName(0)} returns "NULL".
     * <li>{@code Character.getName('e')} returns "LATIN SMALL LETTER E".
     * <li>{@code Character.getName('\u0666')} returns "ARABIC-INDIC DIGIT SIX".
     * <li>{@code Character.getName(0xe000)} returns "PRIVATE USE AREA E000".
     * </ul>
     *
     * <p>Note that the exact strings returned will vary from release to release.
     *
     * @throws IllegalArgumentException if {@code codePoint} is not a valid code point.
     * @since 1.7
     */
    public static String getName(int codePoint) {
        checkValidCodePoint(codePoint);
        if (getType(codePoint) == Character.UNASSIGNED) {
            return null;
        }
        String result = getNameImpl(codePoint);
        if (result == null) {
            String blockName = Character.UnicodeBlock.of(codePoint).toString().replace('_', ' ');
            result = blockName + " " + IntegralToString.intToHexString(codePoint, true, 0);
        }
        return result;
    }

    private static native String getNameImpl(int codePoint) /*-[
      // iOS doesn't provide Unicode character names. A names list table would be very big,
      // so don't support character names until there is a demonstrated customer need.
      return nil;
    ]-*/;

    /**
     * Returns the numeric value of the specified Unicode character.
     * See {@link #getNumericValue(int)}.
     *
     * @param c the character
     * @return a non-negative numeric integer value if a numeric value for
     *         {@code c} exists, -1 if there is no numeric value for {@code c},
     *         -2 if the numeric value can not be represented as an integer.
     */
    public static int getNumericValue(char c) {
        return getNumericValue((int) c);
    }

    /**
     * Gets the numeric value of the specified Unicode code point. For example,
     * the code point '\u216B' stands for the Roman number XII, which has the
     * numeric value 12.
     *
     * <p>There are two points of divergence between this method and the Unicode
     * specification. This method treats the letters a-z (in both upper and lower
     * cases, and their full-width variants) as numbers from 10 to 35. The
     * Unicode specification also supports the idea of code points with non-integer
     * numeric values; this method does not (except to the extent of returning -2
     * for such code points).
     *
     * @param codePoint the code point
     * @return a non-negative numeric integer value if a numeric value for
     *         {@code codePoint} exists, -1 if there is no numeric value for
     *         {@code codePoint}, -2 if the numeric value can not be
     *         represented with an integer.
     */
    public static int getNumericValue(int codePoint) {
        // This is both an optimization and papers over differences between Java and ICU.
        if (codePoint < 128) {
            if (codePoint >= '0' && codePoint <= '9') {
                return codePoint - '0';
            }
            if (codePoint >= 'a' && codePoint <= 'z') {
                return codePoint - ('a' - 10);
            }
            if (codePoint >= 'A' && codePoint <= 'Z') {
                return codePoint - ('A' - 10);
            }
            return -1;
        }
        // Full-width uppercase A-Z.
        if (codePoint >= 0xff21 && codePoint <= 0xff3a) {
            return codePoint - 0xff17;
        }
        // Full-width lowercase a-z.
        if (codePoint >= 0xff41 && codePoint <= 0xff5a) {
            return codePoint - 0xff37;
        }
        return -1;
    }

    /**
     * Gets the general Unicode category of the specified character.
     *
     * @param c
     *            the character to get the category of.
     * @return the Unicode category of {@code c}.
     */
    public static int getType(char c) {
        return getType((int) c);
    }

    /**
     * Gets the general Unicode category of the specified code point.
     *
     * @param codePoint
     *            the Unicode code point to get the category of.
     * @return the Unicode category of {@code codePoint}.
     */
    public static native int getType(int codePoint) /*-[
      int type = u_charType(codePoint);
      if (type >= 17) {  // JRE character type enum values skip 17.
        type++;
      }
      return type;
    ]-*/;

    /**
     * Gets the Unicode directionality of the specified character.
     *
     * @param c
     *            the character to get the directionality of.
     * @return the Unicode directionality of {@code c}.
     */
    public static byte getDirectionality(char c) {
        return getDirectionality((int)c);
    }

    /**
     * Gets the Unicode directionality of the specified character.
     *
     * @param codePoint
     *            the Unicode code point to get the directionality of.
     * @return the Unicode directionality of {@code codePoint}.
     */
    public static native byte getDirectionality(int codePoint) /*-[
      // Maps ICU's UCharDirection enum ordinals to the equivalent directionality constants.
      static jbyte icuDirectionality[] = {
        JavaLangCharacter_DIRECTIONALITY_LEFT_TO_RIGHT,
        JavaLangCharacter_DIRECTIONALITY_RIGHT_TO_LEFT,
        JavaLangCharacter_DIRECTIONALITY_EUROPEAN_NUMBER,
        JavaLangCharacter_DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR,
        JavaLangCharacter_DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR,
        JavaLangCharacter_DIRECTIONALITY_ARABIC_NUMBER,
        JavaLangCharacter_DIRECTIONALITY_COMMON_NUMBER_SEPARATOR,
        JavaLangCharacter_DIRECTIONALITY_PARAGRAPH_SEPARATOR,
        JavaLangCharacter_DIRECTIONALITY_SEGMENT_SEPARATOR,
        JavaLangCharacter_DIRECTIONALITY_WHITESPACE,
        JavaLangCharacter_DIRECTIONALITY_OTHER_NEUTRALS,
        JavaLangCharacter_DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING,
        JavaLangCharacter_DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE,
        JavaLangCharacter_DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC,
        JavaLangCharacter_DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
        JavaLangCharacter_DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE,
        JavaLangCharacter_DIRECTIONALITY_POP_DIRECTIONAL_FORMAT,
        JavaLangCharacter_DIRECTIONALITY_NONSPACING_MARK,
        JavaLangCharacter_DIRECTIONALITY_BOUNDARY_NEUTRAL
      };
      #define ICU_DIRECTIONALITY_MAX 19

      if (JavaLangCharacter_getTypeWithInt_(codePoint) == JavaLangCharacter_UNASSIGNED) {
        return JavaLangCharacter_DIRECTIONALITY_UNDEFINED;
      }
      int directionality = u_charDirection(codePoint);
      if (directionality < 0 || directionality >= ICU_DIRECTIONALITY_MAX) {
        return JavaLangCharacter_DIRECTIONALITY_UNDEFINED;
      }
      return icuDirectionality[directionality];
    ]-*/;

    /**
     * Indicates whether the specified character is mirrored.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is mirrored; {@code false}
     *         otherwise.
     */
    public static boolean isMirrored(char c) {
        return isMirrored((int) c);
    }

    /**
     * Indicates whether the specified code point is mirrored.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is mirrored, {@code false}
     *         otherwise.
     */
    public static native boolean isMirrored(int codePoint) /*-[
      return u_isMirrored(codePoint);
    ]-*/;

    @Override
    public int hashCode() {
        return value;
    }

    /**
     * Returns the high surrogate for the given code point. The result is meaningless if
     * the given code point is not a supplementary character.
     * @since 1.7
     */
    public static char highSurrogate(int codePoint) {
        return (char) ((codePoint >> 10) + 0xd7c0);
    }

    /**
     * Returns the low surrogate for the given code point. The result is meaningless if
     * the given code point is not a supplementary character.
     * @since 1.7
     */
    public static char lowSurrogate(int codePoint) {
        return (char) ((codePoint & 0x3ff) | 0xdc00);
    }

    /**
     * Returns true if the given code point is alphabetic. That is,
     * if it is in any of the Lu, Ll, Lt, Lm, Lo, Nl, or Other_Alphabetic categories.
     * @since 1.7
     */
    public static native boolean isAlphabetic(int codePoint) /*-[
        // iOS only supports 16-bit characters.
        if (codePoint >= 0x8000) {
            return false;
        }
        return [[NSCharacterSet letterCharacterSet] characterIsMember:(unichar) codePoint];
    ]-*/;

    /**
     * Returns true if the given code point is in the Basic Multilingual Plane (BMP).
     * Such code points can be represented by a single {@code char}.
     * @since 1.7
     */
    public static boolean isBmpCodePoint(int codePoint) {
       return codePoint >= Character.MIN_VALUE && codePoint <= Character.MAX_VALUE;
    }

    /**
     * Indicates whether the specified character is defined in the Unicode
     * specification.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if the general Unicode category of the character is
     *         not {@code UNASSIGNED}; {@code false} otherwise.
     */
    public static boolean isDefined(char c) {
        return isDefined((int) c);
    }

    /**
     * Indicates whether the specified code point is defined in the Unicode
     * specification.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if the general Unicode category of the code point is
     *         not {@code UNASSIGNED}; {@code false} otherwise.
     */
    public static native boolean isDefined(int codePoint) /*-[
      return u_isdefined(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is a digit.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a digit; {@code false}
     *         otherwise.
     */
    public static boolean isDigit(char c) {
        return isDigit((int) c);
    }

    /**
     * Indicates whether the specified code point is a digit.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is a digit; {@code false}
     *         otherwise.
     */
    public static native boolean isDigit(int codePoint) /*-[
      return u_isdigit(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is ignorable in a Java or
     * Unicode identifier.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is ignorable; {@code false} otherwise.
     */
    public static boolean isIdentifierIgnorable(char c) {
        return isIdentifierIgnorable((int) c);
    }

    /**
     * Returns true if the given code point is a CJKV ideographic character.
     * @since 1.7
     */
    public static native boolean isIdeographic(int codePoint) /*-[
      return u_getIntPropertyValue(codePoint, UCHAR_IDEOGRAPHIC);
    ]-*/;

    /**
     * Indicates whether the specified code point is ignorable in a Java or
     * Unicode identifier.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is ignorable; {@code false}
     *         otherwise.
     */
    public static native boolean isIdentifierIgnorable(int codePoint) /*-[
      return u_isIDIgnorable(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is an ISO control character.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is an ISO control character;
     *         {@code false} otherwise.
     */
    public static boolean isISOControl(char c) {
        return isISOControl((int) c);
    }

    /**
     * Indicates whether the specified code point is an ISO control character.
     *
     * @param c
     *            the code point to check.
     * @return {@code true} if {@code c} is an ISO control character;
     *         {@code false} otherwise.
     */
    public static native boolean isISOControl(int c) /*-[
      return u_isISOControl(c);
    ]-*/;

    /**
     * Indicates whether the specified character is a valid part of a Java
     * identifier other than the first character.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is valid as part of a Java identifier;
     *         {@code false} otherwise.
     */
    public static boolean isJavaIdentifierPart(char c) {
        return isJavaIdentifierPart((int) c);
    }

    /**
     * Indicates whether the specified code point is a valid part of a Java
     * identifier other than the first character.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code c} is valid as part of a Java identifier;
     *         {@code false} otherwise.
     */
    public static native boolean isJavaIdentifierPart(int codePoint) /*-[
      return u_isJavaIDPart(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is a valid first character for
     * a Java identifier.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a valid first character of a Java
     *         identifier; {@code false} otherwise.
     */
    public static boolean isJavaIdentifierStart(char c) {
        return isJavaIdentifierStart((int) c);
    }

    /**
     * Indicates whether the specified code point is a valid first character for
     * a Java identifier.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is a valid start of a Java
     *         identifier; {@code false} otherwise.
     */
    public static native boolean isJavaIdentifierStart(int codePoint) /*-[
      return u_isJavaIDStart(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is a Java letter.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a Java letter; {@code false}
     *         otherwise.
     * @deprecated Use {@link #isJavaIdentifierStart(char)} instead.
     */
    @Deprecated
    public static boolean isJavaLetter(char c) {
        return isJavaIdentifierStart(c);
    }

    /**
     * Indicates whether the specified character is a Java letter or digit
     * character.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a Java letter or digit;
     *         {@code false} otherwise.
     * @deprecated Use {@link #isJavaIdentifierPart(char)} instead.
     */
    @Deprecated
    public static boolean isJavaLetterOrDigit(char c) {
        return isJavaIdentifierPart(c);
    }

    /**
     * Indicates whether the specified character is a letter.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a letter; {@code false} otherwise.
     */
    public static boolean isLetter(char c) {
        return isLetter((int) c);
    }

    /**
     * Indicates whether the specified code point is a letter.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is a letter; {@code false}
     *         otherwise.
     */
    public static native boolean isLetter(int codePoint) /*-[
      return u_isalpha(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is a letter or a digit.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a letter or a digit; {@code false}
     *         otherwise.
     */
    public static boolean isLetterOrDigit(char c) {
        return isLetterOrDigit((int) c);
    }

    /**
     * Indicates whether the specified code point is a letter or a digit.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is a letter or a digit;
     *         {@code false} otherwise.
     */
    public static native boolean isLetterOrDigit(int codePoint) /*-[
      return u_isalnum(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is a lower case letter.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a lower case letter; {@code false}
     *         otherwise.
     */
    public static boolean isLowerCase(char c) {
        return isLowerCase((int) c);
    }

    /**
     * Indicates whether the specified code point is a lower case letter.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is a lower case letter;
     *         {@code false} otherwise.
     */
    public static native boolean isLowerCase(int codePoint) /*-[
      return u_islower(codePoint);
    ]-*/;

    /**
     * Use {@link #isWhitespace(char)} instead.
     * @deprecated Use {@link #isWhitespace(char)} instead.
     */
    @Deprecated
    public static boolean isSpace(char c) {
        return c == '\n' || c == '\t' || c == '\f' || c == '\r' || c == ' ';
    }

    /**
     * See {@link #isSpaceChar(int)}.
     */
    public static boolean isSpaceChar(char c) {
        return isSpaceChar((int) c);
    }

    /**
     * Returns true if the given code point is a Unicode space character.
     * The exact set of characters considered as whitespace varies with Unicode version.
     * Note that non-breaking spaces are considered whitespace.
     * Note also that line separators are not considered whitespace; see {@link #isWhitespace}
     * for an alternative.
     */
    public static native boolean isSpaceChar(int codePoint) /*-[
      return u_isJavaSpaceChar(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is a titlecase character.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a titlecase character, {@code false}
     *         otherwise.
     */
    public static boolean isTitleCase(char c) {
        return isTitleCase((int) c);
    }

    /**
     * Indicates whether the specified code point is a titlecase character.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is a titlecase character,
     *         {@code false} otherwise.
     */
    public static native boolean isTitleCase(int codePoint) /*-[
      return u_istitle(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is valid as part of a Unicode
     * identifier other than the first character.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is valid as part of a Unicode
     *         identifier; {@code false} otherwise.
     */
    public static boolean isUnicodeIdentifierPart(char c) {
        return isUnicodeIdentifierPart((int) c);
    }

    /**
     * Indicates whether the specified code point is valid as part of a Unicode
     * identifier other than the first character.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is valid as part of a Unicode
     *         identifier; {@code false} otherwise.
     */
    public static native boolean isUnicodeIdentifierPart(int codePoint) /*-[
      return u_isIDPart(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is a valid initial character
     * for a Unicode identifier.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a valid first character for a
     *         Unicode identifier; {@code false} otherwise.
     */
    public static boolean isUnicodeIdentifierStart(char c) {
        return isUnicodeIdentifierStart((int) c);
    }

    /**
     * Indicates whether the specified code point is a valid initial character
     * for a Unicode identifier.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is a valid first character for
     *         a Unicode identifier; {@code false} otherwise.
     */
    public static native boolean isUnicodeIdentifierStart(int codePoint) /*-[
      return u_isIDStart(codePoint);
    ]-*/;

    /**
     * Indicates whether the specified character is an upper case letter.
     *
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a upper case letter; {@code false}
     *         otherwise.
     */
    public static boolean isUpperCase(char c) {
        return isUpperCase((int) c);
    }

    /**
     * Indicates whether the specified code point is an upper case letter.
     *
     * @param codePoint
     *            the code point to check.
     * @return {@code true} if {@code codePoint} is a upper case letter;
     *         {@code false} otherwise.
     */
    public static native boolean isUpperCase(int codePoint) /*-[
      return u_isupper(codePoint);
    ]-*/;

    /**
     * See {@link #isWhitespace(int)}.
     */
    public static boolean isWhitespace(char c) {
        return isWhitespace((int) c);
    }

    /**
     * Returns true if the given code point is a Unicode whitespace character.
     * The exact set of characters considered as whitespace varies with Unicode version.
     * Note that non-breaking spaces are not considered whitespace.
     * Note also that line separators are considered whitespace; see {@link #isSpaceChar}
     * for an alternative.
     */
    public static native boolean isWhitespace(int codePoint) /*-[
      return u_isWhitespace(codePoint);
    ]-*/;

    /**
     * Reverses the order of the first and second byte in the specified
     * character.
     *
     * @param c
     *            the character to reverse.
     * @return the character with reordered bytes.
     */
    public static char reverseBytes(char c) {
        return (char)((c<<8) | (c>>8));
    }

    /**
     * Returns the lower case equivalent for the specified character if the
     * character is an upper case letter. Otherwise, the specified character is
     * returned unchanged.
     *
     * @param c
     *            the character
     * @return if {@code c} is an upper case character then its lower case
     *         counterpart, otherwise just {@code c}.
     */
    public static char toLowerCase(char c) {
        return (char) toLowerCase((int) c);
    }

    /**
     * Returns the lower case equivalent for the specified code point if it is
     * an upper case letter. Otherwise, the specified code point is returned
     * unchanged.
     *
     * @param codePoint
     *            the code point to check.
     * @return if {@code codePoint} is an upper case character then its lower
     *         case counterpart, otherwise just {@code codePoint}.
     */
    public static native int toLowerCase(int codePoint) /*-[
      return u_tolower(codePoint);
    ]-*/;

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Converts the specified character to its string representation.
     *
     * @param value
     *            the character to convert.
     * @return the character converted to a string.
     */
    public static String toString(char value) {
        return String.valueOf(value);
    }

    /**
     * Returns the title case equivalent for the specified character if it
     * exists. Otherwise, the specified character is returned unchanged.
     *
     * @param c
     *            the character to convert.
     * @return the title case equivalent of {@code c} if it exists, otherwise
     *         {@code c}.
     */
    public static char toTitleCase(char c) {
        return (char) toTitleCase((int) c);
    }

    /**
     * Returns the title case equivalent for the specified code point if it
     * exists. Otherwise, the specified code point is returned unchanged.
     *
     * @param codePoint
     *            the code point to convert.
     * @return the title case equivalent of {@code codePoint} if it exists,
     *         otherwise {@code codePoint}.
     */
    public static native int toTitleCase(int codePoint) /*-[
      return u_totitle(codePoint);
    ]-*/;

    /**
     * Returns the upper case equivalent for the specified character if the
     * character is a lower case letter. Otherwise, the specified character is
     * returned unchanged.
     *
     * @param c
     *            the character to convert.
     * @return if {@code c} is a lower case character then its upper case
     *         counterpart, otherwise just {@code c}.
     */
    public static char toUpperCase(char c) {
        return (char) toUpperCase((int) c);
    }

    /**
     * Returns the upper case equivalent for the specified code point if the
     * code point is a lower case letter. Otherwise, the specified code point is
     * returned unchanged.
     *
     * @param codePoint
     *            the code point to convert.
     * @return if {@code codePoint} is a lower case character then its upper
     *         case counterpart, otherwise just {@code codePoint}.
     */
    public static native int toUpperCase(int codePoint) /*-[
      return u_toupper(codePoint);
    ]-*/;

    // Search the sorted values in the array and return the nearest index.
    private static int binarySearchRange(int[] table, int c) {
      int value = 0;
      int low = 0, mid = -1, high = table.length - 1;
      while (low <= high) {
        mid = (low + high) >> 1;
        value = table[mid];
        if (c > value)
          low = mid + 1;
        else if (c == value)
          return mid;
        else
          high = mid - 1;
      }
      return mid - (c < value ? 1 : 0);
    }
}
