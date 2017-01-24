/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.AccessController;
import java.text.MessageFormat;
import libcore.icu.ICU;

import sun.util.locale.BaseLocale;
import sun.util.locale.InternalLocaleBuilder;
import sun.util.locale.LanguageTag;
import sun.util.locale.LocaleExtensions;
import sun.util.locale.LocaleObjectCache;
import sun.util.locale.LocaleSyntaxException;
import sun.util.locale.LocaleUtils;
import sun.util.locale.ParseStatus;
import sun.util.locale.UnicodeLocaleExtension;

/**
 * A <code>Locale</code> object represents a specific geographical, political,
 * or cultural region. An operation that requires a <code>Locale</code> to perform
 * its task is called <em>locale-sensitive</em> and uses the <code>Locale</code>
 * to tailor information for the user. For example, displaying a number
 * is a locale-sensitive operation&mdash; the number should be formatted
 * according to the customs and conventions of the user's native country,
 * region, or culture.
 *
 * <p> The <code>Locale</code> class implements identifiers
 * interchangeable with BCP 47 (IETF BCP 47, "Tags for Identifying
 * Languages"), with support for the LDML (UTS#35, "Unicode Locale
 * Data Markup Language") BCP 47-compatible extensions for locale data
 * exchange.
 *
 * <p> A <code>Locale</code> object logically consists of the fields
 * described below.
 *
 * <dl>
 *   <dt><a name="def_language"></a><b>language</b></dt>
 *
 *   <dd>ISO 639 alpha-2 or alpha-3 language code, or registered
 *   language subtags up to 8 alpha letters (for future enhancements).
 *   When a language has both an alpha-2 code and an alpha-3 code, the
 *   alpha-2 code must be used.  You can find a full list of valid
 *   language codes in the IANA Language Subtag Registry (search for
 *   "Type: language").  The language field is case insensitive, but
 *   <code>Locale</code> always canonicalizes to lower case.</dd><br>
 *
 *   <dd>Well-formed language values have the form
 *   <code>[a-zA-Z]{2,8}</code>.  Note that this is not the the full
 *   BCP47 language production, since it excludes extlang.  They are
 *   not needed since modern three-letter language codes replace
 *   them.</dd><br>
 *
 *   <dd>Example: "en" (English), "ja" (Japanese), "kok" (Konkani)</dd><br>
 *
 *   <dt><a name="def_script"/></a><b>script</b></dt>
 *
 *   <dd>ISO 15924 alpha-4 script code.  You can find a full list of
 *   valid script codes in the IANA Language Subtag Registry (search
 *   for "Type: script").  The script field is case insensitive, but
 *   <code>Locale</code> always canonicalizes to title case (the first
 *   letter is upper case and the rest of the letters are lower
 *   case).</dd><br>
 *
 *   <dd>Well-formed script values have the form
 *   <code>[a-zA-Z]{4}</code></dd><br>
 *
 *   <dd>Example: "Latn" (Latin), "Cyrl" (Cyrillic)</dd><br>
 *
 *   <dt><a name="def_region"></a><b>country (region)</b></dt>
 *
 *   <dd>ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code.
 *   You can find a full list of valid country and region codes in the
 *   IANA Language Subtag Registry (search for "Type: region").  The
 *   country (region) field is case insensitive, but
 *   <code>Locale</code> always canonicalizes to upper case.</dd><br>
 *
 *   <dd>Well-formed country/region values have
 *   the form <code>[a-zA-Z]{2} | [0-9]{3}</code></dd><br>
 *
 *   <dd>Example: "US" (United States), "FR" (France), "029"
 *   (Caribbean)</dd><br>
 *
 *   <dt><a name="def_variant"></a><b>variant</b></dt>
 *
 *   <dd>Any arbitrary value used to indicate a variation of a
 *   <code>Locale</code>.  Where there are two or more variant values
 *   each indicating its own semantics, these values should be ordered
 *   by importance, with most important first, separated by
 *   underscore('_').  The variant field is case sensitive.</dd><br>
 *
 *   <dd>Note: IETF BCP 47 places syntactic restrictions on variant
 *   subtags.  Also BCP 47 subtags are strictly used to indicate
 *   additional variations that define a language or its dialects that
 *   are not covered by any combinations of language, script and
 *   region subtags.  You can find a full list of valid variant codes
 *   in the IANA Language Subtag Registry (search for "Type: variant").
 *
 *   <p>However, the variant field in <code>Locale</code> has
 *   historically been used for any kind of variation, not just
 *   language variations.  For example, some supported variants
 *   available in Java SE Runtime Environments indicate alternative
 *   cultural behaviors such as calendar type or number script.  In
 *   BCP 47 this kind of information, which does not identify the
 *   language, is supported by extension subtags or private use
 *   subtags.</dd><br>
 *
 *   <dd>Well-formed variant values have the form <code>SUBTAG
 *   (('_'|'-') SUBTAG)*</code> where <code>SUBTAG =
 *   [0-9][0-9a-zA-Z]{3} | [0-9a-zA-Z]{5,8}</code>. (Note: BCP 47 only
 *   uses hyphen ('-') as a delimiter, this is more lenient).</dd><br>
 *
 *   <dd>Example: "polyton" (Polytonic Greek), "POSIX"</dd><br>
 *
 *   <dt><a name="def_extensions"></a><b>extensions</b></dt>
 *
 *   <dd>A map from single character keys to string values, indicating
 *   extensions apart from language identification.  The extensions in
 *   <code>Locale</code> implement the semantics and syntax of BCP 47
 *   extension subtags and private use subtags. The extensions are
 *   case insensitive, but <code>Locale</code> canonicalizes all
 *   extension keys and values to lower case. Note that extensions
 *   cannot have empty values.</dd><br>
 *
 *   <dd>Well-formed keys are single characters from the set
 *   <code>[0-9a-zA-Z]</code>.  Well-formed values have the form
 *   <code>SUBTAG ('-' SUBTAG)*</code> where for the key 'x'
 *   <code>SUBTAG = [0-9a-zA-Z]{1,8}</code> and for other keys
 *   <code>SUBTAG = [0-9a-zA-Z]{2,8}</code> (that is, 'x' allows
 *   single-character subtags).</dd><br>
 *
 *   <dd>Example: key="u"/value="ca-japanese" (Japanese Calendar),
 *   key="x"/value="java-1-7"</dd>
 * </dl>
 *
 * <b>Note:</b> Although BCP 47 requires field values to be registered
 * in the IANA Language Subtag Registry, the <code>Locale</code> class
 * does not provide any validation features.  The <code>Builder</code>
 * only checks if an individual field satisfies the syntactic
 * requirement (is well-formed), but does not validate the value
 * itself.  See {@link Builder} for details.
 *
 * <h4><a name="def_locale_extension"></a>Unicode locale/language extension</h4>
 *
 * <p>UTS#35, "Unicode Locale Data Markup Language" defines optional
 * attributes and keywords to override or refine the default behavior
 * associated with a locale.  A keyword is represented by a pair of
 * key and type.  For example, "nu-thai" indicates that Thai local
 * digits (value:"thai") should be used for formatting numbers
 * (key:"nu").
 *
 * <p>The keywords are mapped to a BCP 47 extension value using the
 * extension key 'u' ({@link #UNICODE_LOCALE_EXTENSION}).  The above
 * example, "nu-thai", becomes the extension "u-nu-thai".code
 *
 * <p>Thus, when a <code>Locale</code> object contains Unicode locale
 * attributes and keywords,
 * <code>getExtension(UNICODE_LOCALE_EXTENSION)</code> will return a
 * String representing this information, for example, "nu-thai".  The
 * <code>Locale</code> class also provides {@link
 * #getUnicodeLocaleAttributes}, {@link #getUnicodeLocaleKeys}, and
 * {@link #getUnicodeLocaleType} which allow you to access Unicode
 * locale attributes and key/type pairs directly.  When represented as
 * a string, the Unicode Locale Extension lists attributes
 * alphabetically, followed by key/type sequences with keys listed
 * alphabetically (the order of subtags comprising a key's type is
 * fixed when the type is defined)
 *
 * <p>A well-formed locale key has the form
 * <code>[0-9a-zA-Z]{2}</code>.  A well-formed locale type has the
 * form <code>"" | [0-9a-zA-Z]{3,8} ('-' [0-9a-zA-Z]{3,8})*</code> (it
 * can be empty, or a series of subtags 3-8 alphanums in length).  A
 * well-formed locale attribute has the form
 * <code>[0-9a-zA-Z]{3,8}</code> (it is a single subtag with the same
 * form as a locale type subtag).
 *
 * <p>The Unicode locale extension specifies optional behavior in
 * locale-sensitive services.  Although the LDML specification defines
 * various keys and values, actual locale-sensitive service
 * implementations in a Java Runtime Environment might not support any
 * particular Unicode locale attributes or key/type pairs.
 *
 * <h4>Creating a Locale</h4>
 *
 * <p>There are several different ways to create a <code>Locale</code>
 * object.
 *
 * <h5>Builder</h5>
 *
 * <p>Using {@link Builder} you can construct a <code>Locale</code> object
 * that conforms to BCP 47 syntax.
 *
 * <h5>Constructors</h5>
 *
 * <p>The <code>Locale</code> class provides three constructors:
 * <blockquote>
 * <pre>
 *     {@link #Locale(String language)}
 *     {@link #Locale(String language, String country)}
 *     {@link #Locale(String language, String country, String variant)}
 * </pre>
 * </blockquote>
 * These constructors allow you to create a <code>Locale</code> object
 * with language, country and variant, but you cannot specify
 * script or extensions.
 *
 * <h5>Factory Methods</h5>
 *
 * <p>The method {@link #forLanguageTag} creates a <code>Locale</code>
 * object for a well-formed BCP 47 language tag.
 *
 * <h5>Locale Constants</h5>
 *
 * <p>The <code>Locale</code> class provides a number of convenient constants
 * that you can use to create <code>Locale</code> objects for commonly used
 * locales. For example, the following creates a <code>Locale</code> object
 * for the United States:
 * <blockquote>
 * <pre>
 *     Locale.US
 * </pre>
 * </blockquote>
 *
 * <h4>Use of Locale</h4>
 *
 * <p>Once you've created a <code>Locale</code> you can query it for information
 * about itself. Use <code>getCountry</code> to get the country (or region)
 * code and <code>getLanguage</code> to get the language code.
 * You can use <code>getDisplayCountry</code> to get the
 * name of the country suitable for displaying to the user. Similarly,
 * you can use <code>getDisplayLanguage</code> to get the name of
 * the language suitable for displaying to the user. Interestingly,
 * the <code>getDisplayXXX</code> methods are themselves locale-sensitive
 * and have two versions: one that uses the default locale and one
 * that uses the locale specified as an argument.
 *
 * <p>The Java Platform provides a number of classes that perform locale-sensitive
 * operations. For example, the <code>NumberFormat</code> class formats
 * numbers, currency, and percentages in a locale-sensitive manner. Classes
 * such as <code>NumberFormat</code> have several convenience methods
 * for creating a default object of that type. For example, the
 * <code>NumberFormat</code> class provides these three convenience methods
 * for creating a default <code>NumberFormat</code> object:
 * <blockquote>
 * <pre>
 *     NumberFormat.getInstance()
 *     NumberFormat.getCurrencyInstance()
 *     NumberFormat.getPercentInstance()
 * </pre>
 * </blockquote>
 * Each of these methods has two variants; one with an explicit locale
 * and one without; the latter uses the default locale:
 * <blockquote>
 * <pre>
 *     NumberFormat.getInstance(myLocale)
 *     NumberFormat.getCurrencyInstance(myLocale)
 *     NumberFormat.getPercentInstance(myLocale)
 * </pre>
 * </blockquote>
 * A <code>Locale</code> is the mechanism for identifying the kind of object
 * (<code>NumberFormat</code>) that you would like to get. The locale is
 * <STRONG>just</STRONG> a mechanism for identifying objects,
 * <STRONG>not</STRONG> a container for the objects themselves.
 *
 * <h4>Compatibility</h4>
 *
 * <p>In order to maintain compatibility with existing usage, Locale's
 * constructors retain their behavior prior to the Java Runtime
 * Environment version 1.7.  The same is largely true for the
 * <code>toString</code> method. Thus Locale objects can continue to
 * be used as they were. In particular, clients who parse the output
 * of toString into language, country, and variant fields can continue
 * to do so (although this is strongly discouraged), although the
 * variant field will have additional information in it if script or
 * extensions are present.
 *
 * <p>In addition, BCP 47 imposes syntax restrictions that are not
 * imposed by Locale's constructors. This means that conversions
 * between some Locales and BCP 47 language tags cannot be made without
 * losing information. Thus <code>toLanguageTag</code> cannot
 * represent the state of locales whose language, country, or variant
 * do not conform to BCP 47.
 *
 * <p>Because of these issues, it is recommended that clients migrate
 * away from constructing non-conforming locales and use the
 * <code>forLanguageTag</code> and <code>Locale.Builder</code> APIs instead.
 * Clients desiring a string representation of the complete locale can
 * then always rely on <code>toLanguageTag</code> for this purpose.
 *
 * <h5><a name="special_cases_constructor"></a>Special cases</h5>
 *
 * <p>For compatibility reasons, two
 * non-conforming locales are treated as special cases.  These are
 * <b><tt>ja_JP_JP</tt></b> and <b><tt>th_TH_TH</tt></b>. These are ill-formed
 * in BCP 47 since the variants are too short. To ease migration to BCP 47,
 * these are treated specially during construction.  These two cases (and only
 * these) cause a constructor to generate an extension, all other values behave
 * exactly as they did prior to Java 7.
 *
 * <p>Java has used <tt>ja_JP_JP</tt> to represent Japanese as used in
 * Japan together with the Japanese Imperial calendar. This is now
 * representable using a Unicode locale extension, by specifying the
 * Unicode locale key <tt>ca</tt> (for "calendar") and type
 * <tt>japanese</tt>. When the Locale constructor is called with the
 * arguments "ja", "JP", "JP", the extension "u-ca-japanese" is
 * automatically added.
 *
 * <p>Java has used <tt>th_TH_TH</tt> to represent Thai as used in
 * Thailand together with Thai digits. This is also now representable using
 * a Unicode locale extension, by specifying the Unicode locale key
 * <tt>nu</tt> (for "number") and value <tt>thai</tt>. When the Locale
 * constructor is called with the arguments "th", "TH", "TH", the
 * extension "u-nu-thai" is automatically added.
 *
 * <h5>Serialization</h5>
 *
 * <p>During serialization, writeObject writes all fields to the output
 * stream, including extensions.
 *
 * <p>During deserialization, readResolve adds extensions as described
 * in <a href="#special_cases_constructor">Special Cases</a>, only
 * for the two cases th_TH_TH and ja_JP_JP.
 *
 * <h5>Legacy language codes</h5>
 *
 * <p>Locale's constructor has always converted three language codes to
 * their earlier, obsoleted forms: <tt>he</tt> maps to <tt>iw</tt>,
 * <tt>yi</tt> maps to <tt>ji</tt>, and <tt>id</tt> maps to
 * <tt>in</tt>.  This continues to be the case, in order to not break
 * backwards compatibility.
 *
 * <p>The APIs added in 1.7 map between the old and new language codes,
 * maintaining the old codes internal to Locale (so that
 * <code>getLanguage</code> and <code>toString</code> reflect the old
 * code), but using the new codes in the BCP 47 language tag APIs (so
 * that <code>toLanguageTag</code> reflects the new one). This
 * preserves the equivalence between Locales no matter which code or
 * API is used to construct them. Java's default resource bundle
 * lookup mechanism also implements this mapping, so that resources
 * can be named using either convention, see {@link ResourceBundle.Control}.
 *
 * <h5>Three-letter language/country(region) codes</h5>
 *
 * <p>The Locale constructors have always specified that the language
 * and the country param be two characters in length, although in
 * practice they have accepted any length.  The specification has now
 * been relaxed to allow language codes of two to eight characters and
 * country (region) codes of two to three characters, and in
 * particular, three-letter language codes and three-digit region
 * codes as specified in the IANA Language Subtag Registry.  For
 * compatibility, the implementation still does not impose a length
 * constraint.
 *
 * <a name="locale_data"></a><h4>Locale data</h4>
 * <p>Note that locale data comes solely from ICU. User-supplied locale service providers (using
 * the {@code java.text.spi} or {@code java.util.spi} mechanisms) are not supported.
 *
 * <p>Here are the versions of ICU (and the corresponding CLDR and Unicode versions) used in
 * various Android releases:
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <tr><td>Android 1.5 (Cupcake)/Android 1.6 (Donut)/Android 2.0 (Eclair)</td>
 *     <td>ICU 3.8</td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-5">CLDR 1.5</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode5.0.0/">Unicode 5.0</a></td></tr>
 * <tr><td>Android 2.2 (Froyo)</td>
 *     <td>ICU 4.2</td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-7">CLDR 1.7</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode5.1.0/">Unicode 5.1</a></td></tr>
 * <tr><td>Android 2.3 (Gingerbread)/Android 3.0 (Honeycomb)</td>
 *     <td>ICU 4.4</td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-8">CLDR 1.8</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode5.2.0/">Unicode 5.2</a></td></tr>
 * <tr><td>Android 4.0 (Ice Cream Sandwich)</td>
 *     <td><a href="http://site.icu-project.org/download/46">ICU 4.6</a></td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-9">CLDR 1.9</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode6.0.0/">Unicode 6.0</a></td></tr>
 * <tr><td>Android 4.1 (Jelly Bean)</td>
 *     <td><a href="http://site.icu-project.org/download/48">ICU 4.8</a></td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-2-0">CLDR 2.0</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode6.0.0/">Unicode 6.0</a></td></tr>
 * <tr><td>Android 4.3 (Jelly Bean MR2)</td>
 *     <td><a href="http://site.icu-project.org/download/50">ICU 50</a></td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-22-1">CLDR 22.1</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode6.2.0/">Unicode 6.2</a></td></tr>
 * <tr><td>Android 4.4 (KitKat)</td>
 *     <td><a href="http://site.icu-project.org/download/51">ICU 51</a></td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-23">CLDR 23</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode6.2.0/">Unicode 6.2</a></td></tr>
 * <tr><td>Android 5.0 (Lollipop)</td>
 *     <td><a href="http://site.icu-project.org/download/53">ICU 53</a></td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-25">CLDR 25</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode6.3.0/">Unicode 6.3</a></td></tr>
 * <tr><td>Android 6.0 (Marshmallow)</td>
 *     <td><a href="http://site.icu-project.org/download/55">ICU 55.1</a></td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-27">CLDR 27.0.1</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode7.0.0/">Unicode 7.0</a></td></tr>
 * </table>
 *
 * <a name="default_locale"></a><h4>Be wary of the default locale</h3>
 * <p>Note that there are many convenience methods that automatically use the default locale, but
 * using them may lead to subtle bugs.
 *
 * <p>The default locale is appropriate for tasks that involve presenting data to the user. In
 * this case, you want to use the user's date/time formats, number
 * formats, rules for conversion to lowercase, and so on. In this case, it's safe to use the
 * convenience methods.
 *
 * <p>The default locale is <i>not</i> appropriate for machine-readable output. The best choice
 * there is usually {@code Locale.US}&nbsp;&ndash; this locale is guaranteed to be available on all
 * devices, and the fact that it has no surprising special cases and is frequently used (especially
 * for computer-computer communication) means that it tends to be the most efficient choice too.
 *
 * <p>A common mistake is to implicitly use the default locale when producing output meant to be
 * machine-readable. This tends to work on the developer's test devices (especially because so many
 * developers use en_US), but fails when run on a device whose user is in a more complex locale.
 *
 * <p>For example, if you're formatting integers some locales will use non-ASCII decimal
 * digits. As another example, if you're formatting floating-point numbers some locales will use
 * {@code ','} as the decimal point and {@code '.'} for digit grouping. That's correct for
 * human-readable output, but likely to cause problems if presented to another
 * computer ({@link Double#parseDouble} can't parse such a number, for example).
 * You should also be wary of the {@link String#toLowerCase} and
 * {@link String#toUpperCase} overloads that don't take a {@code Locale}: in Turkey, for example,
 * the characters {@code 'i'} and {@code 'I'} won't be converted to {@code 'I'} and {@code 'i'}.
 * This is the correct behavior for Turkish text (such as user input), but inappropriate for, say,
 * HTTP headers.
 *
 * @see Builder
 * @see ResourceBundle
 * @see java.text.Format
 * @see java.text.NumberFormat
 * @see java.text.Collator
 * @author Mark Davis
 * @since 1.1
 */
public final class Locale implements Cloneable, Serializable {

    static private final  Cache LOCALECACHE = new Cache();

    /** Useful constant for language.
     */
    static public final Locale ENGLISH = createConstant("en", "");

    /** Useful constant for language.
     */
    static public final Locale FRENCH = createConstant("fr", "");

    /** Useful constant for language.
     */
    static public final Locale GERMAN = createConstant("de", "");

    /** Useful constant for language.
     */
    static public final Locale ITALIAN = createConstant("it", "");

    /** Useful constant for language.
     */
    static public final Locale JAPANESE = createConstant("ja", "");

    /** Useful constant for language.
     */
    static public final Locale KOREAN = createConstant("ko", "");

    /** Useful constant for language.
     */
    static public final Locale CHINESE = createConstant("zh", "");

    /** Useful constant for language.
     */
    static public final Locale SIMPLIFIED_CHINESE = createConstant("zh", "CN");

    /** Useful constant for language.
     */
    static public final Locale TRADITIONAL_CHINESE = createConstant("zh", "TW");

    /** Useful constant for country.
     */
    static public final Locale FRANCE = createConstant("fr", "FR");

    /** Useful constant for country.
     */
    static public final Locale GERMANY = createConstant("de", "DE");

    /** Useful constant for country.
     */
    static public final Locale ITALY = createConstant("it", "IT");

    /** Useful constant for country.
     */
    static public final Locale JAPAN = createConstant("ja", "JP");

    /** Useful constant for country.
     */
    static public final Locale KOREA = createConstant("ko", "KR");

    /** Useful constant for country.
     */
    static public final Locale CHINA = SIMPLIFIED_CHINESE;

    /** Useful constant for country.
     */
    static public final Locale PRC = SIMPLIFIED_CHINESE;

    /** Useful constant for country.
     */
    static public final Locale TAIWAN = TRADITIONAL_CHINESE;

    /** Useful constant for country.
     */
    static public final Locale UK = createConstant("en", "GB");

    /** Useful constant for country.
     */
    static public final Locale US = createConstant("en", "US");

    /** Useful constant for country.
     */
    static public final Locale CANADA = createConstant("en", "CA");

    /** Useful constant for country.
     */
    static public final Locale CANADA_FRENCH = createConstant("fr", "CA");

    /**
     * ISO 639-3 generic code for undetermined languages.
     */
    private static final String UNDETERMINED_LANGUAGE = "und";

    /**
     * Useful constant for the root locale.  The root locale is the locale whose
     * language, country, and variant are empty ("") strings.  This is regarded
     * as the base locale of all locales, and is used as the language/country
     * neutral locale for the locale sensitive operations.
     *
     * @since 1.6
     */
    static public final Locale ROOT = createConstant("", "");

    /**
     * The key for the private use extension ('x').
     *
     * @see #getExtension(char)
     * @see Builder#setExtension(char, String)
     * @since 1.7
     */
    static public final char PRIVATE_USE_EXTENSION = 'x';

    /**
     * The key for Unicode locale extension ('u').
     *
     * @see #getExtension(char)
     * @see Builder#setExtension(char, String)
     * @since 1.7
     */
    static public final char UNICODE_LOCALE_EXTENSION = 'u';

    /** serialization ID
     */
    static final long serialVersionUID = 9149081749638150636L;

    /**
     * Display types for retrieving localized names from the name providers.
     */
    private static final int DISPLAY_LANGUAGE = 0;
    private static final int DISPLAY_COUNTRY  = 1;
    private static final int DISPLAY_VARIANT  = 2;
    private static final int DISPLAY_SCRIPT   = 3;

    /**
     * Private constructor used by getInstance method
     */
    private Locale(BaseLocale baseLocale, LocaleExtensions extensions) {
        this.baseLocale = baseLocale;
        this.localeExtensions = extensions;
    }

    /**
     * Construct a locale from language, country and variant.
     * This constructor normalizes the language value to lowercase and
     * the country value to uppercase.
     * <p>
     * <b>Note:</b>
     * <ul>
     * <li>ISO 639 is not a stable standard; some of the language codes it defines
     * (specifically "iw", "ji", and "in") have changed.  This constructor accepts both the
     * old codes ("iw", "ji", and "in") and the new codes ("he", "yi", and "id"), but all other
     * API on Locale will return only the OLD codes.
     * <li>For backward compatibility reasons, this constructor does not make
     * any syntactic checks on the input.
     * <li>The two cases ("ja", "JP", "JP") and ("th", "TH", "TH") are handled specially,
     * see <a href="#special_cases_constructor">Special Cases</a> for more information.
     * </ul>
     *
     * @param language An ISO 639 alpha-2 or alpha-3 language code, or a language subtag
     * up to 8 characters in length.  See the <code>Locale</code> class description about
     * valid language values.
     * @param country An ISO 3166 alpha-2 country code or a UN M.49 numeric-3 area code.
     * See the <code>Locale</code> class description about valid country values.
     * @param variant Any arbitrary value used to indicate a variation of a <code>Locale</code>.
     * See the <code>Locale</code> class description for the details.
     * @exception NullPointerException thrown if any argument is null.
     */
    public Locale(String language, String country, String variant) {
        if (language== null || country == null || variant == null) {
            throw new NullPointerException();
        }
        baseLocale = BaseLocale.getInstance(convertOldISOCodes(language), "", country, variant);
        localeExtensions = getCompatibilityExtensions(language, "", country, variant);
    }

    /**
     * Construct a locale from language and country.
     * This constructor normalizes the language value to lowercase and
     * the country value to uppercase.
     * <p>
     * <b>Note:</b>
     * <ul>
     * <li>ISO 639 is not a stable standard; some of the language codes it defines
     * (specifically "iw", "ji", and "in") have changed.  This constructor accepts both the
     * old codes ("iw", "ji", and "in") and the new codes ("he", "yi", and "id"), but all other
     * API on Locale will return only the OLD codes.
     * <li>For backward compatibility reasons, this constructor does not make
     * any syntactic checks on the input.
     * </ul>
     *
     * @param language An ISO 639 alpha-2 or alpha-3 language code, or a language subtag
     * up to 8 characters in length.  See the <code>Locale</code> class description about
     * valid language values.
     * @param country An ISO 3166 alpha-2 country code or a UN M.49 numeric-3 area code.
     * See the <code>Locale</code> class description about valid country values.
     * @exception NullPointerException thrown if either argument is null.
     */
    public Locale(String language, String country) {
        this(language, country, "");
    }

    /**
     * Construct a locale from a language code.
     * This constructor normalizes the language value to lowercase.
     * <p>
     * <b>Note:</b>
     * <ul>
     * <li>ISO 639 is not a stable standard; some of the language codes it defines
     * (specifically "iw", "ji", and "in") have changed.  This constructor accepts both the
     * old codes ("iw", "ji", and "in") and the new codes ("he", "yi", and "id"), but all other
     * API on Locale will return only the OLD codes.
     * <li>For backward compatibility reasons, this constructor does not make
     * any syntactic checks on the input.
     * </ul>
     *
     * @param language An ISO 639 alpha-2 or alpha-3 language code, or a language subtag
     * up to 8 characters in length.  See the <code>Locale</code> class description about
     * valid language values.
     * @exception NullPointerException thrown if argument is null.
     * @since 1.4
     */
    public Locale(String language) {
        this(language, "", "");
    }

    /**
     * This method must be called only for creating the Locale.*
     * constants due to making shortcuts.
     */
    private static Locale createConstant(String lang, String country) {
        BaseLocale base = BaseLocale.createInstance(lang, country);
        return getInstance(base, null);
    }

    /**
     * Returns a <code>Locale</code> constructed from the given
     * <code>language</code>, <code>country</code> and
     * <code>variant</code>. If the same <code>Locale</code> instance
     * is available in the cache, then that instance is
     * returned. Otherwise, a new <code>Locale</code> instance is
     * created and cached.
     *
     * @param language lowercase 2 to 8 language code.
     * @param country uppercase two-letter ISO-3166 code and numric-3 UN M.49 area code.
     * @param variant vendor and browser specific code. See class description.
     * @return the <code>Locale</code> instance requested
     * @exception NullPointerException if any argument is null.
     */
    static Locale getInstance(String language, String country, String variant) {
        return getInstance(language, "", country, variant, null);
    }

    static Locale getInstance(String language, String script, String country,
                                      String variant, LocaleExtensions extensions) {
        if (language== null || script == null || country == null || variant == null) {
            throw new NullPointerException();
        }

        if (extensions == null) {
            extensions = getCompatibilityExtensions(language, script, country, variant);
        }

        BaseLocale baseloc = BaseLocale.getInstance(language, script, country, variant);
        return getInstance(baseloc, extensions);
    }

    static Locale getInstance(BaseLocale baseloc, LocaleExtensions extensions) {
        LocaleKey key = new LocaleKey(baseloc, extensions);
        return LOCALECACHE.get(key);
    }

    private static class Cache extends LocaleObjectCache<LocaleKey, Locale> {
        private Cache() {
        }

        @Override
        protected Locale createObject(LocaleKey key) {
            return new Locale(key.base, key.exts);
        }
    }

    private static final class LocaleKey {
        private final BaseLocale base;
        private final LocaleExtensions exts;
        private final int hash;

        private LocaleKey(BaseLocale baseLocale, LocaleExtensions extensions) {
            base = baseLocale;
            exts = extensions;

            // Calculate the hash value here because it's always used.
            int h = base.hashCode();
            if (exts != null) {
                h ^= exts.hashCode();
            }
            hash = h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LocaleKey)) {
                return false;
            }
            LocaleKey other = (LocaleKey)obj;
            if (hash != other.hash || !base.equals(other.base)) {
                return false;
            }
            if (exts == null) {
                return other.exts == null;
            }
            return exts.equals(other.exts);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    /**
     * Gets the current value of the default locale for this instance
     * of the Java Virtual Machine.
     * <p>
     * The Java Virtual Machine sets the default locale during startup
     * based on the host environment. It is used by many locale-sensitive
     * methods if no locale is explicitly specified.
     * It can be changed using the
     * {@link #setDefault(java.util.Locale) setDefault} method.
     *
     * @return the default locale for this instance of the Java Virtual Machine
     */
    public static Locale getDefault() {
        // do not synchronize this method - see 4071298
        // it's OK if more than one default locale happens to be created
        if (defaultLocale == null) {
            defaultLocale = initDefault();
        }
        return defaultLocale;
    }

    /**
     * Gets the current value of the default locale for the specified Category
     * for this instance of the Java Virtual Machine.
     * <p>
     * The Java Virtual Machine sets the default locale during startup based
     * on the host environment. It is used by many locale-sensitive methods
     * if no locale is explicitly specified. It can be changed using the
     * setDefault(Locale.Category, Locale) method.
     *
     * @param category - the specified category to get the default locale
     * @throws NullPointerException - if category is null
     * @return the default locale for the specified Category for this instance
     *     of the Java Virtual Machine
     * @see #setDefault(Locale.Category, Locale)
     * @since 1.7
     */
    public static Locale getDefault(Locale.Category category) {
        // do not synchronize this method - see 4071298
        // it's OK if more than one default locale happens to be created
        switch (category) {
        case DISPLAY:
            if (defaultDisplayLocale == null) {
                defaultDisplayLocale = initDefault(category);
            }
            return defaultDisplayLocale;
        case FORMAT:
            if (defaultFormatLocale == null) {
                defaultFormatLocale = initDefault(category);
            }
            return defaultFormatLocale;
        default:
            assert false: "Unknown Category";
        }
        return getDefault();
    }

    /**
     * @hide visible for testing.
     */
    public static Locale initDefault() {
        // user.locale gets priority
        final String languageTag = System.getProperty("user.locale", "");
        if (!languageTag.isEmpty()) {
            return Locale.forLanguageTag(languageTag);
        }

        // user.locale is empty
        String language, region, script, country, variant;
        language = System.getProperty("user.language", "en");
        // for compatibility, check for old user.region property
        region = System.getProperty("user.region");
        if (region != null) {
            // region can be of form country, country_variant, or _variant
            int i = region.indexOf('_');
            if (i >= 0) {
                country = region.substring(0, i);
                variant = region.substring(i + 1);
            } else {
                country = region;
                variant = "";
            }
            script = "";
        } else {
            script = System.getProperty("user.script", "");
            country = System.getProperty("user.country", "");
            variant = System.getProperty("user.variant", "");
        }
        return getInstance(language, script, country, variant, null);
    }

    private static Locale initDefault(Locale.Category category) {
        // make sure defaultLocale is initialized
        final Locale defaultLocale = getDefault();

        return getInstance(
            System.getProperty(category.languageKey, defaultLocale.getLanguage()),
            System.getProperty(category.scriptKey, defaultLocale.getScript()),
            System.getProperty(category.countryKey, defaultLocale.getCountry()),
            System.getProperty(category.variantKey, defaultLocale.getVariant()),
            null);
    }

    /**
     * Sets the default locale for this instance of the Java Virtual Machine.
     * This does not affect the host locale.
     * <p>
     * If there is a security manager, its <code>checkPermission</code>
     * method is called with a <code>PropertyPermission("user.language", "write")</code>
     * permission before the default locale is changed.
     * <p>
     * The Java Virtual Machine sets the default locale during startup
     * based on the host environment. It is used by many locale-sensitive
     * methods if no locale is explicitly specified.
     * <p>
     * Since changing the default locale may affect many different areas
     * of functionality, this method should only be used if the caller
     * is prepared to reinitialize locale-sensitive code running
     * within the same Java Virtual Machine.
     * <p>
     * By setting the default locale with this method, all of the default
     * locales for each Category are also set to the specified default locale.
     *
     * @throws SecurityException
     *        if a security manager exists and its
     *        <code>checkPermission</code> method doesn't allow the operation.
     * @throws NullPointerException if <code>newLocale</code> is null
     * @param newLocale the new default locale
     * @see SecurityManager#checkPermission
     * @see java.util.PropertyPermission
     */
    public static synchronized void setDefault(Locale newLocale) {
        setDefault(Category.DISPLAY, newLocale);
        setDefault(Category.FORMAT, newLocale);
        defaultLocale = newLocale;
        ICU.setDefaultLocale(newLocale.toLanguageTag());
    }

    /**
     * Sets the default locale for the specified Category for this instance
     * of the Java Virtual Machine. This does not affect the host locale.
     * <p>
     * If there is a security manager, its checkPermission method is called
     * with a PropertyPermission("user.language", "write") permission before
     * the default locale is changed.
     * <p>
     * The Java Virtual Machine sets the default locale during startup based
     * on the host environment. It is used by many locale-sensitive methods
     * if no locale is explicitly specified.
     * <p>
     * Since changing the default locale may affect many different areas of
     * functionality, this method should only be used if the caller is
     * prepared to reinitialize locale-sensitive code running within the
     * same Java Virtual Machine.
     * <p>
     *
     * @param category - the specified category to set the default locale
     * @param newLocale - the new default locale
     * @throws SecurityException - if a security manager exists and its
     *     checkPermission method doesn't allow the operation.
     * @throws NullPointerException - if category and/or newLocale is null
     * @see SecurityManager#checkPermission(java.security.Permission)
     * @see PropertyPermission
     * @see #getDefault(Locale.Category)
     * @since 1.7
     */
    public static synchronized void setDefault(Locale.Category category,
        Locale newLocale) {
        if (category == null)
            throw new NullPointerException("Category cannot be NULL");
        if (newLocale == null)
            throw new NullPointerException("Can't set default locale to NULL");

        /* J2ObjC removed.
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new PropertyPermission
                        ("user.language", "write"));*/
        switch (category) {
        case DISPLAY:
            defaultDisplayLocale = newLocale;
            break;
        case FORMAT:
            defaultFormatLocale = newLocale;
            break;
        default:
            assert false: "Unknown Category";
        }
    }

    /**
     * Returns an array of all installed locales.
     * The returned array represents the union of locales supported
     * by the Java runtime environment and by installed
     * {@link java.util.spi.LocaleServiceProvider LocaleServiceProvider}
     * implementations.  It must contain at least a <code>Locale</code>
     * instance equal to {@link java.util.Locale#US Locale.US}.
     *
     * @return An array of installed locales.
     */
    public static Locale[] getAvailableLocales() {
        // J2ObjC changed: Switched to use ICU. (Android has the same change at master)
        return ICU.getAvailableLocales();
    }

    /**
     * Returns a list of all 2-letter country codes defined in ISO 3166.
     * Can be used to create Locales.
     * <p>
     * <b>Note:</b> The <code>Locale</code> class also supports other codes for
     * country (region), such as 3-letter numeric UN M.49 area codes.
     * Therefore, the list returned by this method does not contain ALL valid
     * codes that can be used to create Locales.
     */
    public static String[] getISOCountries() {
        // Android-changed: Use ICU.
        return ICU.getISOCountries();
    }

    /**
     * Returns a list of all 2-letter language codes defined in ISO 639.
     * Can be used to create Locales.
     * <p>
     * <b>Note:</b>
     * <ul>
     * <li>ISO 639 is not a stable standard&mdash; some languages' codes have changed.
     * The list this function returns includes both the new and the old codes for the
     * languages whose codes have changed.
     * <li>The <code>Locale</code> class also supports language codes up to
     * 8 characters in length.  Therefore, the list returned by this method does
     * not contain ALL valid codes that can be used to create Locales.
     * </ul>
     */
    public static String[] getISOLanguages() {
        // Android-changed: Use ICU.
        return ICU.getISOLanguages();
    }

    /**
     * Returns the language code of this Locale.
     *
     * <p><b>Note:</b> ISO 639 is not a stable standard&mdash; some languages' codes have changed.
     * Locale's constructor recognizes both the new and the old codes for the languages
     * whose codes have changed, but this function always returns the old code.  If you
     * want to check for a specific language whose code has changed, don't do
     * <pre>
     * if (locale.getLanguage().equals("he")) // BAD!
     *    ...
     * </pre>
     * Instead, do
     * <pre>
     * if (locale.getLanguage().equals(new Locale("he").getLanguage()))
     *    ...
     * </pre>
     * @return The language code, or the empty string if none is defined.
     * @see #getDisplayLanguage
     */
    public String getLanguage() {
        return baseLocale.getLanguage();
    }

    /**
     * Returns the script for this locale, which should
     * either be the empty string or an ISO 15924 4-letter script
     * code. The first letter is uppercase and the rest are
     * lowercase, for example, 'Latn', 'Cyrl'.
     *
     * @return The script code, or the empty string if none is defined.
     * @see #getDisplayScript
     * @since 1.7
     */
    public String getScript() {
        return baseLocale.getScript();
    }

    /**
     * Returns the country/region code for this locale, which should
     * either be the empty string, an uppercase ISO 3166 2-letter code,
     * or a UN M.49 3-digit code.
     *
     * @return The country/region code, or the empty string if none is defined.
     * @see #getDisplayCountry
     */
    public String getCountry() {
        return baseLocale.getRegion();
    }

    /**
     * Returns the variant code for this locale.
     *
     * @return The variant code, or the empty string if none is defined.
     * @see #getDisplayVariant
     */
    public String getVariant() {
        return baseLocale.getVariant();
    }

    /**
     * Returns the extension (or private use) value associated with
     * the specified key, or null if there is no extension
     * associated with the key. To be well-formed, the key must be one
     * of <code>[0-9A-Za-z]</code>. Keys are case-insensitive, so
     * for example 'z' and 'Z' represent the same extension.
     *
     * @param key the extension key
     * @return The extension, or null if this locale defines no
     * extension for the specified key.
     * @throws IllegalArgumentException if key is not well-formed
     * @see #PRIVATE_USE_EXTENSION
     * @see #UNICODE_LOCALE_EXTENSION
     * @since 1.7
     */
    public String getExtension(char key) {
        if (!LocaleExtensions.isValidKey(key)) {
            throw new IllegalArgumentException("Ill-formed extension key: " + key);
        }
        return (localeExtensions == null) ? null : localeExtensions.getExtensionValue(key);
    }

    /**
     * Returns the set of extension keys associated with this locale, or the
     * empty set if it has no extensions. The returned set is unmodifiable.
     * The keys will all be lower-case.
     *
     * @return The set of extension keys, or the empty set if this locale has
     * no extensions.
     * @since 1.7
     */
    public Set<Character> getExtensionKeys() {
        if (localeExtensions == null) {
            return Collections.emptySet();
        }
        return localeExtensions.getKeys();
    }

    /**
     * Returns the set of unicode locale attributes associated with
     * this locale, or the empty set if it has no attributes. The
     * returned set is unmodifiable.
     *
     * @return The set of attributes.
     * @since 1.7
     */
    public Set<String> getUnicodeLocaleAttributes() {
        if (localeExtensions == null) {
            return Collections.emptySet();
        }
        return localeExtensions.getUnicodeLocaleAttributes();
    }

    /**
     * Returns the Unicode locale type associated with the specified Unicode locale key
     * for this locale. Returns the empty string for keys that are defined with no type.
     * Returns null if the key is not defined. Keys are case-insensitive. The key must
     * be two alphanumeric characters ([0-9a-zA-Z]), or an IllegalArgumentException is
     * thrown.
     *
     * @param key the Unicode locale key
     * @return The Unicode locale type associated with the key, or null if the
     * locale does not define the key.
     * @throws IllegalArgumentException if the key is not well-formed
     * @throws NullPointerException if <code>key</code> is null
     * @since 1.7
     */
    public String getUnicodeLocaleType(String key) {
        if (!UnicodeLocaleExtension.isKey(key)) {
            throw new IllegalArgumentException("Ill-formed Unicode locale key: " + key);
        }
        return (localeExtensions == null) ? null : localeExtensions.getUnicodeLocaleType(key);
    }

    /**
     * Returns the set of Unicode locale keys defined by this locale, or the empty set if
     * this locale has none.  The returned set is immutable.  Keys are all lower case.
     *
     * @return The set of Unicode locale keys, or the empty set if this locale has
     * no Unicode locale keywords.
     * @since 1.7
     */
    public Set<String> getUnicodeLocaleKeys() {
        if (localeExtensions == null) {
            return Collections.emptySet();
        }
        return localeExtensions.getUnicodeLocaleKeys();
    }

    /**
     * Package locale method returning the Locale's BaseLocale,
     * used by ResourceBundle
     * @return base locale of this Locale
     */
    BaseLocale getBaseLocale() {
        return baseLocale;
    }

    /**
     * Package private method returning the Locale's LocaleExtensions,
     * used by ResourceBundle.
     * @return locale exnteions of this Locale,
     *         or {@code null} if no extensions are defined
     */
     LocaleExtensions getLocaleExtensions() {
         return localeExtensions;
     }

    /**
     * Returns a string representation of this <code>Locale</code>
     * object, consisting of language, country, variant, script,
     * and extensions as below:
     * <p><blockquote>
     * language + "_" + country + "_" + (variant + "_#" | "#") + script + "-" + extensions
     * </blockquote>
     *
     * Language is always lower case, country is always upper case, script is always title
     * case, and extensions are always lower case.  Extensions and private use subtags
     * will be in canonical order as explained in {@link #toLanguageTag}.
     *
     * <p>When the locale has neither script nor extensions, the result is the same as in
     * Java 6 and prior.
     *
     * <p>If both the language and country fields are missing, this function will return
     * the empty string, even if the variant, script, or extensions field is present (you
     * can't have a locale with just a variant, the variant must accompany a well-formed
     * language or country code).
     *
     * <p>If script or extensions are present and variant is missing, no underscore is
     * added before the "#".
     *
     * <p>This behavior is designed to support debugging and to be compatible with
     * previous uses of <code>toString</code> that expected language, country, and variant
     * fields only.  To represent a Locale as a String for interchange purposes, use
     * {@link #toLanguageTag}.
     *
     * <p>Examples: <ul><tt>
     * <li>en
     * <li>de_DE
     * <li>_GB
     * <li>en_US_WIN
     * <li>de__POSIX
     * <li>zh_CN_#Hans
     * <li>zh_TW_#Hant-x-java
     * <li>th_TH_TH_#u-nu-thai</tt></ul>
     *
     * @return A string representation of the Locale, for debugging.
     * @see #getDisplayName
     * @see #toLanguageTag
     */
    @Override
    public final String toString() {
        boolean l = (baseLocale.getLanguage().length() != 0);
        boolean s = (baseLocale.getScript().length() != 0);
        boolean r = (baseLocale.getRegion().length() != 0);
        boolean v = (baseLocale.getVariant().length() != 0);
        boolean e = (localeExtensions != null && localeExtensions.getID().length() != 0);

        StringBuilder result = new StringBuilder(baseLocale.getLanguage());
        if (r || (l && (v || s || e))) {
            result.append('_')
                .append(baseLocale.getRegion()); // This may just append '_'
        }
        if (v && (l || r)) {
            result.append('_')
                .append(baseLocale.getVariant());
        }

        if (s && (l || r)) {
            result.append("_#")
                .append(baseLocale.getScript());
        }

        if (e && (l || r)) {
            result.append('_');
            if (!s) {
                result.append('#');
            }
            result.append(localeExtensions.getID());
        }

        return result.toString();
    }

    /**
     * Returns a well-formed IETF BCP 47 language tag representing
     * this locale.
     *
     * <p>If this <code>Locale</code> has a language, country, or
     * variant that does not satisfy the IETF BCP 47 language tag
     * syntax requirements, this method handles these fields as
     * described below:
     *
     * <p><b>Language:</b> If language is empty, or not <a
     * href="#def_language" >well-formed</a> (for example "a" or
     * "e2"), it will be emitted as "und" (Undetermined).
     *
     * <p><b>Country:</b> If country is not <a
     * href="#def_region">well-formed</a> (for example "12" or "USA"),
     * it will be omitted.
     *
     * <p><b>Variant:</b> If variant <b>is</b> <a
     * href="#def_variant">well-formed</a>, each sub-segment
     * (delimited by '-' or '_') is emitted as a subtag.  Otherwise:
     * <ul>
     *
     * <li>if all sub-segments match <code>[0-9a-zA-Z]{1,8}</code>
     * (for example "WIN" or "Oracle_JDK_Standard_Edition"), the first
     * ill-formed sub-segment and all following will be appended to
     * the private use subtag.  The first appended subtag will be
     * "lvariant", followed by the sub-segments in order, separated by
     * hyphen. For example, "x-lvariant-WIN",
     * "Oracle-x-lvariant-JDK-Standard-Edition".
     *
     * <li>if any sub-segment does not match
     * <code>[0-9a-zA-Z]{1,8}</code>, the variant will be truncated
     * and the problematic sub-segment and all following sub-segments
     * will be omitted.  If the remainder is non-empty, it will be
     * emitted as a private use subtag as above (even if the remainder
     * turns out to be well-formed).  For example,
     * "Solaris_isjustthecoolestthing" is emitted as
     * "x-lvariant-Solaris", not as "solaris".</li></ul>
     *
     * <p><b>Special Conversions:</b> Java supports some old locale
     * representations, including deprecated ISO language codes,
     * for compatibility. This method performs the following
     * conversions:
     * <ul>
     *
     * <li>Deprecated ISO language codes "iw", "ji", and "in" are
     * converted to "he", "yi", and "id", respectively.
     *
     * <li>A locale with language "no", country "NO", and variant
     * "NY", representing Norwegian Nynorsk (Norway), is converted
     * to a language tag "nn-NO".</li></ul>
     *
     * <p><b>Note:</b> Although the language tag created by this
     * method is well-formed (satisfies the syntax requirements
     * defined by the IETF BCP 47 specification), it is not
     * necessarily a valid BCP 47 language tag.  For example,
     * <pre>
     *   new Locale("xx", "YY").toLanguageTag();</pre>
     *
     * will return "xx-YY", but the language subtag "xx" and the
     * region subtag "YY" are invalid because they are not registered
     * in the IANA Language Subtag Registry.
     *
     * @return a BCP47 language tag representing the locale
     * @see #forLanguageTag(String)
     * @since 1.7
     */
    public String toLanguageTag() {
        LanguageTag tag = LanguageTag.parseLocale(baseLocale, localeExtensions);
        StringBuilder buf = new StringBuilder();

        String subtag = tag.getLanguage();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.canonicalizeLanguage(subtag));
        }

        subtag = tag.getScript();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeScript(subtag));
        }

        subtag = tag.getRegion();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeRegion(subtag));
        }

        List<String>subtags = tag.getVariants();
        for (String s : subtags) {
            buf.append(LanguageTag.SEP);
            // preserve casing
            buf.append(s);
        }

        subtags = tag.getExtensions();
        for (String s : subtags) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeExtension(s));
        }

        subtag = tag.getPrivateuse();
        if (subtag.length() > 0) {
            if (buf.length() > 0) {
                buf.append(LanguageTag.SEP);
            }
            buf.append(LanguageTag.PRIVATEUSE).append(LanguageTag.SEP);
            // preserve casing
            buf.append(subtag);
        }

        return buf.toString();
    }

    /**
     * Returns a locale for the specified IETF BCP 47 language tag string.
     *
     * <p>If the specified language tag contains any ill-formed subtags,
     * the first such subtag and all following subtags are ignored.  Compare
     * to {@link Locale.Builder#setLanguageTag} which throws an exception
     * in this case.
     *
     * <p>The following <b>conversions</b> are performed:<ul>
     *
     * <li>The language code "und" is mapped to language "".
     *
     * <li>The language codes "he", "yi", and "id" are mapped to "iw",
     * "ji", and "in" respectively. (This is the same canonicalization
     * that's done in Locale's constructors.)
     *
     * <li>The portion of a private use subtag prefixed by "lvariant",
     * if any, is removed and appended to the variant field in the
     * result locale (without case normalization).  If it is then
     * empty, the private use subtag is discarded:
     *
     * <pre>
     *     Locale loc;
     *     loc = Locale.forLanguageTag("en-US-x-lvariant-POSIX");
     *     loc.getVariant(); // returns "POSIX"
     *     loc.getExtension('x'); // returns null
     *
     *     loc = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Def");
     *     loc.getVariant(); // returns "POSIX_Abc_Def"
     *     loc.getExtension('x'); // returns "urp"
     * </pre>
     *
     * <li>When the languageTag argument contains an extlang subtag,
     * the first such subtag is used as the language, and the primary
     * language subtag and other extlang subtags are ignored:
     *
     * <pre>
     *     Locale.forLanguageTag("ar-aao").getLanguage(); // returns "aao"
     *     Locale.forLanguageTag("en-abc-def-us").toString(); // returns "abc_US"
     * </pre>
     *
     * <li>Case is normalized except for variant tags, which are left
     * unchanged.  Language is normalized to lower case, script to
     * title case, country to upper case, and extensions to lower
     * case.
     *
     * <li>If, after processing, the locale would exactly match either
     * ja_JP_JP or th_TH_TH with no extensions, the appropriate
     * extensions are added as though the constructor had been called:
     *
     * <pre>
     *    Locale.forLanguageTag("ja-JP-x-lvariant-JP").toLanguageTag();
     *    // returns "ja-JP-u-ca-japanese-x-lvariant-JP"
     *    Locale.forLanguageTag("th-TH-x-lvariant-TH").toLanguageTag();
     *    // returns "th-TH-u-nu-thai-x-lvariant-TH"
     * <pre></ul>
     *
     * <p>This implements the 'Language-Tag' production of BCP47, and
     * so supports grandfathered (regular and irregular) as well as
     * private use language tags.  Stand alone private use tags are
     * represented as empty language and extension 'x-whatever',
     * and grandfathered tags are converted to their canonical replacements
     * where they exist.
     *
     * <p>Grandfathered tags with canonical replacements are as follows:
     *
     * <table>
     * <tbody align="center">
     * <tr><th>grandfathered tag</th><th>&nbsp;</th><th>modern replacement</th></tr>
     * <tr><td>art-lojban</td><td>&nbsp;</td><td>jbo</td></tr>
     * <tr><td>i-ami</td><td>&nbsp;</td><td>ami</td></tr>
     * <tr><td>i-bnn</td><td>&nbsp;</td><td>bnn</td></tr>
     * <tr><td>i-hak</td><td>&nbsp;</td><td>hak</td></tr>
     * <tr><td>i-klingon</td><td>&nbsp;</td><td>tlh</td></tr>
     * <tr><td>i-lux</td><td>&nbsp;</td><td>lb</td></tr>
     * <tr><td>i-navajo</td><td>&nbsp;</td><td>nv</td></tr>
     * <tr><td>i-pwn</td><td>&nbsp;</td><td>pwn</td></tr>
     * <tr><td>i-tao</td><td>&nbsp;</td><td>tao</td></tr>
     * <tr><td>i-tay</td><td>&nbsp;</td><td>tay</td></tr>
     * <tr><td>i-tsu</td><td>&nbsp;</td><td>tsu</td></tr>
     * <tr><td>no-bok</td><td>&nbsp;</td><td>nb</td></tr>
     * <tr><td>no-nyn</td><td>&nbsp;</td><td>nn</td></tr>
     * <tr><td>sgn-BE-FR</td><td>&nbsp;</td><td>sfb</td></tr>
     * <tr><td>sgn-BE-NL</td><td>&nbsp;</td><td>vgt</td></tr>
     * <tr><td>sgn-CH-DE</td><td>&nbsp;</td><td>sgg</td></tr>
     * <tr><td>zh-guoyu</td><td>&nbsp;</td><td>cmn</td></tr>
     * <tr><td>zh-hakka</td><td>&nbsp;</td><td>hak</td></tr>
     * <tr><td>zh-min-nan</td><td>&nbsp;</td><td>nan</td></tr>
     * <tr><td>zh-xiang</td><td>&nbsp;</td><td>hsn</td></tr>
     * </tbody>
     * </table>
     *
     * <p>Grandfathered tags with no modern replacement will be
     * converted as follows:
     *
     * <table>
     * <tbody align="center">
     * <tr><th>grandfathered tag</th><th>&nbsp;</th><th>converts to</th></tr>
     * <tr><td>cel-gaulish</td><td>&nbsp;</td><td>xtg-x-cel-gaulish</td></tr>
     * <tr><td>en-GB-oed</td><td>&nbsp;</td><td>en-GB-x-oed</td></tr>
     * <tr><td>i-default</td><td>&nbsp;</td><td>en-x-i-default</td></tr>
     * <tr><td>i-enochian</td><td>&nbsp;</td><td>und-x-i-enochian</td></tr>
     * <tr><td>i-mingo</td><td>&nbsp;</td><td>see-x-i-mingo</td></tr>
     * <tr><td>zh-min</td><td>&nbsp;</td><td>nan-x-zh-min</td></tr>
     * </tbody>
     * </table>
     *
     * <p>For a list of all grandfathered tags, see the
     * IANA Language Subtag Registry (search for "Type: grandfathered").
     *
     * <p><b>Note</b>: there is no guarantee that <code>toLanguageTag</code>
     * and <code>forLanguageTag</code> will round-trip.
     *
     * @param languageTag the language tag
     * @return The locale that best represents the language tag.
     * @throws NullPointerException if <code>languageTag</code> is <code>null</code>
     * @see #toLanguageTag()
     * @see java.util.Locale.Builder#setLanguageTag(String)
     * @since 1.7
     */
    public static Locale forLanguageTag(String languageTag) {
        LanguageTag tag = LanguageTag.parse(languageTag, null);
        InternalLocaleBuilder bldr = new InternalLocaleBuilder();
        bldr.setLanguageTag(tag);
        BaseLocale base = bldr.getBaseLocale();
        LocaleExtensions exts = bldr.getLocaleExtensions();
        if (exts == null && base.getVariant().length() > 0) {
            exts = getCompatibilityExtensions(base.getLanguage(), base.getScript(),
                                              base.getRegion(), base.getVariant());
        }
        return getInstance(base, exts);
    }

    /**
     * Returns a three-letter abbreviation of this locale's language.
     * If the language matches an ISO 639-1 two-letter code, the
     * corresponding ISO 639-2/T three-letter lowercase code is
     * returned.  The ISO 639-2 language codes can be found on-line,
     * see "Codes for the Representation of Names of Languages Part 2:
     * Alpha-3 Code".  If the locale specifies a three-letter
     * language, the language is returned as is.  If the locale does
     * not specify a language the empty string is returned.
     *
     * @return A three-letter abbreviation of this locale's language.
     * @exception MissingResourceException Throws MissingResourceException if
     * three-letter language abbreviation is not available for this locale.
     */
    public String getISO3Language() throws MissingResourceException {
        // Android-changed: Use ICU.getIso3Language. Also return "" for empty languages
        // for the sake of backwards compatibility.
        String lang = baseLocale.getLanguage();
        if (lang.length() == 3) {
            return lang;
        } else if (lang.isEmpty()) {
            return "";
        }

        String language3 = ICU.getISO3Language(lang);
        if (!lang.isEmpty() && language3.isEmpty()) {
            throw new MissingResourceException("Couldn't find 3-letter language code for "
                    + lang, "FormatData_" + toString(), "ShortLanguage");
        }

        return language3;
    }

    /**
     * Returns a three-letter abbreviation for this locale's country.
     * If the country matches an ISO 3166-1 alpha-2 code, the
     * corresponding ISO 3166-1 alpha-3 uppercase code is returned.
     * If the locale doesn't specify a country, this will be the empty
     * string.
     *
     * <p>The ISO 3166-1 codes can be found on-line.
     *
     * @return A three-letter abbreviation of this locale's country.
     * @exception MissingResourceException Throws MissingResourceException if the
     * three-letter country abbreviation is not available for this locale.
     */
    public String getISO3Country() throws MissingResourceException {
        // Android changed: Use.getIso3Country. Also return "" for missing regions.
        final String region = baseLocale.getRegion();
        // Note that this will return an UN.M49 region code
        if (region.length() == 3) {
            return baseLocale.getRegion();
        } else if (region.isEmpty()) {
            return "";
        }

        // Prefix "en-" because ICU doesn't really care about what the language is.
        String country3 = ICU.getISO3Country("en-" + region);
        if (!region.isEmpty() && country3.isEmpty()) {
            throw new MissingResourceException("Couldn't find 3-letter country code for "
                    + baseLocale.getRegion(), "FormatData_" + toString(), "ShortCountry");
        }
        return country3;
    }

    /**
     * Returns a name for the locale's language that is appropriate for display to the
     * user.
     * If possible, the name returned will be localized for the default locale.
     * For example, if the locale is fr_FR and the default locale
     * is en_US, getDisplayLanguage() will return "French"; if the locale is en_US and
     * the default locale is fr_FR, getDisplayLanguage() will return "anglais".
     * If the name returned cannot be localized for the default locale,
     * (say, we don't have a Japanese name for Croatian),
     * this function falls back on the English name, and uses the ISO code as a last-resort
     * value.  If the locale doesn't specify a language, this function returns the empty string.
     */
    public final String getDisplayLanguage() {
        return getDisplayLanguage(getDefault(Category.DISPLAY));
    }

    /**
     * Returns the name of this locale's language, localized to {@code locale}.
     * If the language name is unknown, the language code is returned.
     */
    public String getDisplayLanguage(Locale locale) {
        String languageCode = baseLocale.getLanguage();
        if (languageCode.isEmpty()) {
            return "";
        }

        // Hacks for backward compatibility.
        //
        // Our language tag will contain "und" if the languageCode is invalid
        // or missing. ICU will then return "langue indéterminée" or the equivalent
        // display language for the indeterminate language code.
        //
        // Sigh... ugh... and what not.
        final String normalizedLanguage = normalizeAndValidateLanguage(
                languageCode, false /* strict */);
        if (UNDETERMINED_LANGUAGE.equals(normalizedLanguage)) {
            return languageCode;
        }

        // TODO: We need a new hack or a complete fix for http://b/8049507 --- We would
        // cover the frameworks' tracks when they were using "tl" instead of "fil".
        String result = ICU.getDisplayLanguage(this, locale);
        if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
            result = ICU.getDisplayLanguage(this, Locale.getDefault());
        }
        return result;
    }

    private static String normalizeAndValidateLanguage(String language, boolean strict) {
        if (language == null || language.isEmpty()) {
            return "";
        }

        final String lowercaseLanguage = language.toLowerCase(Locale.ROOT);
        if (!isValidBcp47Alpha(lowercaseLanguage, 2, 3)) {
            if (strict) {
                throw new IllformedLocaleException("Invalid language: " + language);
            } else {
                return UNDETERMINED_LANGUAGE;
            }
        }

        return lowercaseLanguage;
    }

    /*
     * Checks whether a given string is an ASCII alphanumeric string.
     */
    private static boolean isAsciiAlphaNum(String string) {
        for (int i = 0; i < string.length(); i++) {
            final char character = string.charAt(i);
            if (!(character >= 'a' && character <= 'z' ||
                    character >= 'A' && character <= 'Z' ||
                    character >= '0' && character <= '9')) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a name for the the locale's script that is appropriate for display to
     * the user. If possible, the name will be localized for the default locale.  Returns
     * the empty string if this locale doesn't specify a script code.
     *
     * @return the display name of the script code for the current default locale
     * @since 1.7
     */
    public String getDisplayScript() {
        return getDisplayScript(getDefault());
    }

    /**
     * Returns the name of this locale's script code, localized to {@link Locale}. If the
     * script code is unknown, the return value of this method is the same as that of
     * {@link #getScript()}.
     *
     * @since 1.7
     */
    public String getDisplayScript(Locale locale) {
        String scriptCode = baseLocale.getScript();
        if (scriptCode.isEmpty()) {
            return "";
        }

        String result = ICU.getDisplayScript(this, locale);
        if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
            result = ICU.getDisplayScript(this, Locale.getDefault());
        }

        return result;

    }

    /**
     * Returns a name for the locale's country that is appropriate for display to the
     * user.
     * If possible, the name returned will be localized for the default locale.
     * For example, if the locale is fr_FR and the default locale
     * is en_US, getDisplayCountry() will return "France"; if the locale is en_US and
     * the default locale is fr_FR, getDisplayCountry() will return "Etats-Unis".
     * If the name returned cannot be localized for the default locale,
     * (say, we don't have a Japanese name for Croatia),
     * this function falls back on the English name, and uses the ISO code as a last-resort
     * value.  If the locale doesn't specify a country, this function returns the empty string.
     */
    public final String getDisplayCountry() {
        return getDisplayCountry(getDefault(Category.DISPLAY));
    }
    /**
     * Returns the name of this locale's country, localized to {@code locale}.
     * Returns the empty string if this locale does not correspond to a specific
     * country.
     */
    public String getDisplayCountry(Locale locale) {
        String countryCode = baseLocale.getRegion();
        if (countryCode.isEmpty()) {
            return "";
        }

        final String normalizedRegion = normalizeAndValidateRegion(
                countryCode, false /* strict */);
        if (normalizedRegion.isEmpty()) {
            return countryCode;
        }

        String result = ICU.getDisplayCountry(this, locale);
        if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
            result = ICU.getDisplayCountry(this, Locale.getDefault());
        }
        return result;
    }

    private static String normalizeAndValidateRegion(String region, boolean strict) {
        if (region == null || region.isEmpty()) {
            return "";
        }

        final String uppercaseRegion = region.toUpperCase(Locale.ROOT);
        if (!isValidBcp47Alpha(uppercaseRegion, 2, 2) &&
                !isUnM49AreaCode(uppercaseRegion)) {
            if (strict) {
                throw new IllformedLocaleException("Invalid region: " + region);
            } else {
                return "";
            }
        }

        return uppercaseRegion;
    }

    private static boolean isValidBcp47Alpha(String string, int lowerBound, int upperBound) {
        final int length = string.length();
        if (length < lowerBound || length > upperBound) {
            return false;
        }

        for (int i = 0; i < length; ++i) {
            final char character = string.charAt(i);
            if (!(character >= 'a' && character <= 'z' ||
                    character >= 'A' && character <= 'Z')) {
                return false;
            }
        }

        return true;
    }

    /**
     * A UN M.49 is a 3 digit numeric code.
     */
    private static boolean isUnM49AreaCode(String code) {
        if (code.length() != 3) {
            return false;
        }

        for (int i = 0; i < 3; ++i) {
            final char character = code.charAt(i);
            if (!(character >= '0' && character <= '9')) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a name for the locale's variant code that is appropriate for display to the
     * user.  If possible, the name will be localized for the default locale.  If the locale
     * doesn't specify a variant code, this function returns the empty string.
     */
    public final String getDisplayVariant() {
        return getDisplayVariant(getDefault(Category.DISPLAY));
    }

    /**
     * Returns the full variant name in the specified {@code Locale} for the variant code
     * of this {@code Locale}. If there is no matching variant name, the variant code is
     * returned.
     *
     * @since 1.7
     */
    public String getDisplayVariant(Locale locale) {
        String variantCode = baseLocale.getVariant();
        if (variantCode.isEmpty()) {
            return "";
        }

        try {
            normalizeAndValidateVariant(variantCode);
        } catch (IllformedLocaleException ilfe) {
            return variantCode;
        }

        String result = ICU.getDisplayVariant(this, locale);
        if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
            result = ICU.getDisplayVariant(this, Locale.getDefault());
        }

        // The "old style" locale constructors allow us to pass in variants that aren't
        // valid BCP-47 variant subtags. When that happens, toLanguageTag will not emit
        // them. Note that we know variantCode.length() > 0 due to the isEmpty check at
        // the beginning of this function.
        if (result.isEmpty()) {
            return variantCode;
        }
        return result;
    }

    private static String normalizeAndValidateVariant(String variant) {
        if (variant == null || variant.isEmpty()) {
            return "";
        }

        // Note that unlike extensions, we canonicalize to lower case alphabets
        // and underscores instead of hyphens.
        final String normalizedVariant = variant.replace('-', '_');
        String[] subTags = normalizedVariant.split("_");

        for (String subTag : subTags) {
            if (!isValidVariantSubtag(subTag)) {
                throw new IllformedLocaleException("Invalid variant: " + variant);
            }
        }

        return normalizedVariant;
    }

    private static boolean isValidVariantSubtag(String subTag) {
        // The BCP-47 spec states that :
        // - Subtags can be between [5, 8] alphanumeric chars in length.
        // - Subtags that start with a number are allowed to be 4 chars in length.
        if (subTag.length() >= 5 && subTag.length() <= 8) {
            if (isAsciiAlphaNum(subTag)) {
                return true;
            }
        } else if (subTag.length() == 4) {
            final char firstChar = subTag.charAt(0);
            if ((firstChar >= '0' && firstChar <= '9') && isAsciiAlphaNum(subTag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a name for the locale that is appropriate for display to the
     * user. This will be the values returned by getDisplayLanguage(),
     * getDisplayScript(), getDisplayCountry(), and getDisplayVariant() assembled
     * into a single string. The the non-empty values are used in order,
     * with the second and subsequent names in parentheses.  For example:
     * <blockquote>
     * language (script, country, variant)<br>
     * language (country)<br>
     * language (variant)<br>
     * script (country)<br>
     * country<br>
     * </blockquote>
     * depending on which fields are specified in the locale.  If the
     * language, sacript, country, and variant fields are all empty,
     * this function returns the empty string.
     */
    public final String getDisplayName() {
        return getDisplayName(getDefault(Category.DISPLAY));
    }

    /**
     * Returns this locale's language name, country name, and variant, localized
     * to {@code locale}. The exact output form depends on whether this locale
     * corresponds to a specific language, script, country and variant.
     *
     * <p>For example:
     * <ul>
     * <li>{@code new Locale("en").getDisplayName(Locale.US)} -> {@code English}
     * <li>{@code new Locale("en", "US").getDisplayName(Locale.US)} -> {@code English (United States)}
     * <li>{@code new Locale("en", "US", "POSIX").getDisplayName(Locale.US)} -> {@code English (United States,Computer)}
     * <li>{@code Locale.fromLanguageTag("zh-Hant-CN").getDisplayName(Locale.US)} -> {@code Chinese (Traditional Han,China)}
     * <li>{@code new Locale("en").getDisplayName(Locale.FRANCE)} -> {@code anglais}
     * <li>{@code new Locale("en", "US").getDisplayName(Locale.FRANCE)} -> {@code anglais (États-Unis)}
     * <li>{@code new Locale("en", "US", "POSIX").getDisplayName(Locale.FRANCE)} -> {@code anglais (États-Unis,informatique)}.
     * </ul>
     */
    public String getDisplayName(Locale locale) {
        int count = 0;
        StringBuilder buffer = new StringBuilder();
        String languageCode = baseLocale.getLanguage();
        if (!languageCode.isEmpty()) {
            String displayLanguage = getDisplayLanguage(locale);
            buffer.append(displayLanguage.isEmpty() ? languageCode : displayLanguage);
            ++count;
        }
        String scriptCode = baseLocale.getScript();
        if (!scriptCode.isEmpty()) {
            if (count == 1) {
                buffer.append(" (");
            }
            String displayScript = getDisplayScript(locale);
            buffer.append(displayScript.isEmpty() ? scriptCode : displayScript);
            ++count;
        }
        String countryCode = baseLocale.getRegion();
        if (!countryCode.isEmpty()) {
            if (count == 1) {
                buffer.append(" (");
            } else if (count == 2) {
                buffer.append(",");
            }
            String displayCountry = getDisplayCountry(locale);
            buffer.append(displayCountry.isEmpty() ? countryCode : displayCountry);
            ++count;
        }
        String variantCode = baseLocale.getVariant();
        if (!variantCode.isEmpty()) {
            if (count == 1) {
                buffer.append(" (");
            } else if (count == 2 || count == 3) {
                buffer.append(",");
            }
            String displayVariant = getDisplayVariant(locale);
            buffer.append(displayVariant.isEmpty() ? variantCode : displayVariant);
            ++count;
        }
        if (count > 1) {
            buffer.append(")");
        }
        return buffer.toString();
    }

    /**
     * Overrides Cloneable.
     */
    public Object clone()
    {
        try {
            Locale that = (Locale)super.clone();
            return that;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Override hashCode.
     * Since Locales are often used in hashtables, caches the value
     * for speed.
     */
    @Override
    public int hashCode() {
        int hc = hashCodeValue;
        if (hc == 0) {
            hc = baseLocale.hashCode();
            if (localeExtensions != null) {
                hc ^= localeExtensions.hashCode();
            }
            hashCodeValue = hc;
        }
        return hc;
    }

    // Overrides

    /**
     * Returns true if this Locale is equal to another object.  A Locale is
     * deemed equal to another Locale with identical language, script, country,
     * variant and extensions, and unequal to all other objects.
     *
     * @return true if this Locale is equal to the specified object.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)                      // quick check
            return true;
        if (!(obj instanceof Locale))
            return false;
        BaseLocale otherBase = ((Locale)obj).baseLocale;
        if (!baseLocale.equals(otherBase)) {
            return false;
        }
        if (localeExtensions == null) {
            return ((Locale)obj).localeExtensions == null;
        }
        return localeExtensions.equals(((Locale)obj).localeExtensions);
    }

    // ================= privates =====================================

    private transient BaseLocale baseLocale;
    private transient LocaleExtensions localeExtensions;

    /**
     * Calculated hashcode
     */
    private transient volatile int hashCodeValue = 0;

    private static Locale defaultLocale = null;
    private static Locale defaultDisplayLocale = null;
    private static Locale defaultFormatLocale = null;

    /**
     * Format a list using given pattern strings.
     * If either of the patterns is null, then a the list is
     * formatted by concatenation with the delimiter ','.
     * @param stringList the list of strings to be formatted.
     * @param listPattern should create a MessageFormat taking 0-3 arguments
     * and formatting them into a list.
     * @param listCompositionPattern should take 2 arguments
     * and is used by composeList.
     * @return a string representing the list.
     */
    private static String formatList(String[] stringList, String listPattern, String listCompositionPattern) {
        // If we have no list patterns, compose the list in a simple,
        // non-localized way.
        if (listPattern == null || listCompositionPattern == null) {
            StringBuffer result = new StringBuffer();
            for (int i=0; i<stringList.length; ++i) {
                if (i>0) result.append(',');
                result.append(stringList[i]);
            }
            return result.toString();
        }

        // Compose the list down to three elements if necessary
        if (stringList.length > 3) {
            MessageFormat format = new MessageFormat(listCompositionPattern);
            stringList = composeList(format, stringList);
        }

        // Rebuild the argument list with the list length as the first element
        Object[] args = new Object[stringList.length + 1];
        System.arraycopy(stringList, 0, args, 1, stringList.length);
        args[0] = new Integer(stringList.length);

        // Format it using the pattern in the resource
        MessageFormat format = new MessageFormat(listPattern);
        return format.format(args);
    }

    /**
     * Given a list of strings, return a list shortened to three elements.
     * Shorten it by applying the given format to the first two elements
     * recursively.
     * @param format a format which takes two arguments
     * @param list a list of strings
     * @return if the list is three elements or shorter, the same list;
     * otherwise, a new list of three elements.
     */
    private static String[] composeList(MessageFormat format, String[] list) {
        if (list.length <= 3) return list;

        // Use the given format to compose the first two elements into one
        String[] listItems = { list[0], list[1] };
        String newItem = format.format(listItems);

        // Form a new list one element shorter
        String[] newList = new String[list.length-1];
        System.arraycopy(list, 2, newList, 1, newList.length-1);
        newList[0] = newItem;

        // Recurse
        return composeList(format, newList);
    }

    /**
     * @serialField language    String
     *      language subtag in lower case. (See <a href="java/util/Locale.html#getLanguage()">getLanguage()</a>)
     * @serialField country     String
     *      country subtag in upper case. (See <a href="java/util/Locale.html#getCountry()">getCountry()</a>)
     * @serialField variant     String
     *      variant subtags separated by LOWLINE characters. (See <a href="java/util/Locale.html#getVariant()">getVariant()</a>)
     * @serialField hashcode    int
     *      deprecated, for forward compatibility only
     * @serialField script      String
     *      script subtag in title case (See <a href="java/util/Locale.html#getScript()">getScript()</a>)
     * @serialField extensions  String
     *      canonical representation of extensions, that is,
     *      BCP47 extensions in alphabetical order followed by
     *      BCP47 private use subtags, all in lower case letters
     *      separated by HYPHEN-MINUS characters.
     *      (See <a href="java/util/Locale.html#getExtensionKeys()">getExtensionKeys()</a>,
     *      <a href="java/util/Locale.html#getExtension(char)">getExtension(char)</a>)
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("language", String.class),
        new ObjectStreamField("country", String.class),
        new ObjectStreamField("variant", String.class),
        new ObjectStreamField("hashcode", int.class),
        new ObjectStreamField("script", String.class),
        new ObjectStreamField("extensions", String.class),
    };

    /**
     * Serializes this <code>Locale</code> to the specified <code>ObjectOutputStream</code>.
     * @param out the <code>ObjectOutputStream</code> to write
     * @throws IOException
     * @since 1.7
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("language", baseLocale.getLanguage());
        fields.put("script", baseLocale.getScript());
        fields.put("country", baseLocale.getRegion());
        fields.put("variant", baseLocale.getVariant());
        fields.put("extensions", localeExtensions == null ? "" : localeExtensions.getID());
        fields.put("hashcode", -1); // place holder just for backward support
        out.writeFields();
    }

    /**
     * Deserializes this <code>Locale</code>.
     * @param in the <code>ObjectInputStream</code> to read
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllformedLocaleException
     * @since 1.7
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        String language = (String)fields.get("language", "");
        String script = (String)fields.get("script", "");
        String country = (String)fields.get("country", "");
        String variant = (String)fields.get("variant", "");
        String extStr = (String)fields.get("extensions", "");
        baseLocale = BaseLocale.getInstance(convertOldISOCodes(language), script, country, variant);
        if (extStr != null && extStr.length() > 0) {
            try {
                InternalLocaleBuilder bldr = new InternalLocaleBuilder();
                bldr.setExtensions(extStr);
                localeExtensions = bldr.getLocaleExtensions();
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage());
            }
        } else {
            localeExtensions = null;
        }
    }

    /**
     * Returns a cached <code>Locale</code> instance equivalent to
     * the deserialized <code>Locale</code>. When serialized
     * language, country and variant fields read from the object data stream
     * are exactly "ja", "JP", "JP" or "th", "TH", "TH" and script/extensions
     * fields are empty, this method supplies <code>UNICODE_LOCALE_EXTENSION</code>
     * "ca"/"japanese" (calendar type is "japanese") or "nu"/"thai" (number script
     * type is "thai"). See <a href="Locale.html#special_cases_constructor"/>Special Cases</a>
     * for more information.
     *
     * @return an instance of <code>Locale</code> equivalent to
     * the deserialized <code>Locale</code>.
     * @throws java.io.ObjectStreamException
     */
    private Object readResolve() throws java.io.ObjectStreamException {
        return getInstance(baseLocale.getLanguage(), baseLocale.getScript(),
                baseLocale.getRegion(), baseLocale.getVariant(), localeExtensions);
    }

    private static volatile String[] isoLanguages = null;

    private static volatile String[] isoCountries = null;

    private static String convertOldISOCodes(String language) {
        // we accept both the old and the new ISO codes for the languages whose ISO
        // codes have changed, but we always store the OLD code, for backward compatibility
        language = LocaleUtils.toLowerString(language).intern();
        if (language == "he") {
            return "iw";
        } else if (language == "yi") {
            return "ji";
        } else if (language == "id") {
            return "in";
        } else {
            return language;
        }
    }

    private static LocaleExtensions getCompatibilityExtensions(String language,
                                                               String script,
                                                               String country,
                                                               String variant) {
        LocaleExtensions extensions = null;
        // Special cases for backward compatibility support
        if (LocaleUtils.caseIgnoreMatch(language, "ja")
                && script.length() == 0
                && LocaleUtils.caseIgnoreMatch(country, "jp")
                && "JP".equals(variant)) {
            // ja_JP_JP -> u-ca-japanese (calendar = japanese)
            extensions = LocaleExtensions.CALENDAR_JAPANESE;
        } else if (LocaleUtils.caseIgnoreMatch(language, "th")
                && script.length() == 0
                && LocaleUtils.caseIgnoreMatch(country, "th")
                && "TH".equals(variant)) {
            // th_TH_TH -> u-nu-thai (numbersystem = thai)
            extensions = LocaleExtensions.NUMBER_THAI;
        }
        return extensions;
    }

    /**
     * @hide for internal use only.
     */
    public static String adjustLanguageCode(String languageCode) {
        String adjusted = languageCode.toLowerCase(Locale.US);
        // Map new language codes to the obsolete language
        // codes so the correct resource bundles will be used.
        if (languageCode.equals("he")) {
            adjusted = "iw";
        } else if (languageCode.equals("id")) {
            adjusted = "in";
        } else if (languageCode.equals("yi")) {
            adjusted = "ji";
        }

        return adjusted;
    }

    /**
     * Enum for locale categories.  These locale categories are used to get/set
     * the default locale for the specific functionality represented by the
     * category.
     *
     * @see #getDefault(Locale.Category)
     * @see #setDefault(Locale.Category, Locale)
     * @since 1.7
     */
    public enum Category {

        /**
         * Category used to represent the default locale for
         * displaying user interfaces.
         */
        DISPLAY("user.language.display",
                "user.script.display",
                "user.country.display",
                "user.variant.display"),

        /**
         * Category used to represent the default locale for
         * formatting dates, numbers, and/or currencies.
         */
        FORMAT("user.language.format",
               "user.script.format",
               "user.country.format",
               "user.variant.format");

        Category(String languageKey, String scriptKey, String countryKey, String variantKey) {
            this.languageKey = languageKey;
            this.scriptKey = scriptKey;
            this.countryKey = countryKey;
            this.variantKey = variantKey;
        }

        final String languageKey;
        final String scriptKey;
        final String countryKey;
        final String variantKey;
    }

    /**
     * <code>Builder</code> is used to build instances of <code>Locale</code>
     * from values configured by the setters.  Unlike the <code>Locale</code>
     * constructors, the <code>Builder</code> checks if a value configured by a
     * setter satisfies the syntax requirements defined by the <code>Locale</code>
     * class.  A <code>Locale</code> object created by a <code>Builder</code> is
     * well-formed and can be transformed to a well-formed IETF BCP 47 language tag
     * without losing information.
     *
     * <p><b>Note:</b> The <code>Locale</code> class does not provide any
     * syntactic restrictions on variant, while BCP 47 requires each variant
     * subtag to be 5 to 8 alphanumerics or a single numeric followed by 3
     * alphanumerics.  The method <code>setVariant</code> throws
     * <code>IllformedLocaleException</code> for a variant that does not satisfy
     * this restriction. If it is necessary to support such a variant, use a
     * Locale constructor.  However, keep in mind that a <code>Locale</code>
     * object created this way might lose the variant information when
     * transformed to a BCP 47 language tag.
     *
     * <p>The following example shows how to create a <code>Locale</code> object
     * with the <code>Builder</code>.
     * <blockquote>
     * <pre>
     *     Locale aLocale = new Builder().setLanguage("sr").setScript("Latn").setRegion("RS").build();
     * </pre>
     * </blockquote>
     *
     * <p>Builders can be reused; <code>clear()</code> resets all
     * fields to their default values.
     *
     * @see Locale#forLanguageTag
     * @since 1.7
     */
    public static final class Builder {
        private final InternalLocaleBuilder localeBuilder;

        /**
         * Constructs an empty Builder. The default value of all
         * fields, extensions, and private use information is the
         * empty string.
         */
        public Builder() {
            localeBuilder = new InternalLocaleBuilder();
        }

        /**
         * Resets the <code>Builder</code> to match the provided
         * <code>locale</code>.  Existing state is discarded.
         *
         * <p>All fields of the locale must be well-formed, see {@link Locale}.
         *
         * <p>Locales with any ill-formed fields cause
         * <code>IllformedLocaleException</code> to be thrown, except for the
         * following three cases which are accepted for compatibility
         * reasons:<ul>
         * <li>Locale("ja", "JP", "JP") is treated as "ja-JP-u-ca-japanese"
         * <li>Locale("th", "TH", "TH") is treated as "th-TH-u-nu-thai"
         * <li>Locale("no", "NO", "NY") is treated as "nn-NO"</ul>
         *
         * @param locale the locale
         * @return This builder.
         * @throws IllformedLocaleException if <code>locale</code> has
         * any ill-formed fields.
         * @throws NullPointerException if <code>locale</code> is null.
         */
        public Builder setLocale(Locale locale) {
            try {
                localeBuilder.setLocale(locale.baseLocale, locale.localeExtensions);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Resets the Builder to match the provided IETF BCP 47
         * language tag.  Discards the existing state.  Null and the
         * empty string cause the builder to be reset, like {@link
         * #clear}.  Grandfathered tags (see {@link
         * Locale#forLanguageTag}) are converted to their canonical
         * form before being processed.  Otherwise, the language tag
         * must be well-formed (see {@link Locale}) or an exception is
         * thrown (unlike <code>Locale.forLanguageTag</code>, which
         * just discards ill-formed and following portions of the
         * tag).
         *
         * @param languageTag the language tag
         * @return This builder.
         * @throws IllformedLocaleException if <code>languageTag</code> is ill-formed
         * @see Locale#forLanguageTag(String)
         */
        public Builder setLanguageTag(String languageTag) {
            ParseStatus sts = new ParseStatus();
            LanguageTag tag = LanguageTag.parse(languageTag, sts);
            if (sts.isError()) {
                throw new IllformedLocaleException(sts.getErrorMessage(), sts.getErrorIndex());
            }
            localeBuilder.setLanguageTag(tag);
            return this;
        }

        /**
         * Sets the language.  If <code>language</code> is the empty string or
         * null, the language in this <code>Builder</code> is removed.  Otherwise,
         * the language must be <a href="./Locale.html#def_language">well-formed</a>
         * or an exception is thrown.
         *
         * <p>The typical language value is a two or three-letter language
         * code as defined in ISO639.
         *
         * @param language the language
         * @return This builder.
         * @throws IllformedLocaleException if <code>language</code> is ill-formed
         */
        public Builder setLanguage(String language) {
            try {
                localeBuilder.setLanguage(language);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Sets the script. If <code>script</code> is null or the empty string,
         * the script in this <code>Builder</code> is removed.
         * Otherwise, the script must be <a href="./Locale.html#def_script">well-formed</a> or an
         * exception is thrown.
         *
         * <p>The typical script value is a four-letter script code as defined by ISO 15924.
         *
         * @param script the script
         * @return This builder.
         * @throws IllformedLocaleException if <code>script</code> is ill-formed
         */
        public Builder setScript(String script) {
            try {
                localeBuilder.setScript(script);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Sets the region.  If region is null or the empty string, the region
         * in this <code>Builder</code> is removed.  Otherwise,
         * the region must be <a href="./Locale.html#def_region">well-formed</a> or an
         * exception is thrown.
         *
         * <p>The typical region value is a two-letter ISO 3166 code or a
         * three-digit UN M.49 area code.
         *
         * <p>The country value in the <code>Locale</code> created by the
         * <code>Builder</code> is always normalized to upper case.
         *
         * @param region the region
         * @return This builder.
         * @throws IllformedLocaleException if <code>region</code> is ill-formed
         */
        public Builder setRegion(String region) {
            try {
                localeBuilder.setRegion(region);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Sets the variant.  If variant is null or the empty string, the
         * variant in this <code>Builder</code> is removed.  Otherwise, it
         * must consist of one or more <a href="./Locale.html#def_variant">well-formed</a>
         * subtags, or an exception is thrown.
         *
         * <p><b>Note:</b> This method checks if <code>variant</code>
         * satisfies the IETF BCP 47 variant subtag's syntax requirements,
         * and normalizes the value to lowercase letters.  However,
         * the <code>Locale</code> class does not impose any syntactic
         * restriction on variant, and the variant value in
         * <code>Locale</code> is case sensitive.  To set such a variant,
         * use a Locale constructor.
         *
         * @param variant the variant
         * @return This builder.
         * @throws IllformedLocaleException if <code>variant</code> is ill-formed
         */
        public Builder setVariant(String variant) {
            try {
                localeBuilder.setVariant(variant);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Sets the extension for the given key. If the value is null or the
         * empty string, the extension is removed.  Otherwise, the extension
         * must be <a href="./Locale.html#def_extensions">well-formed</a> or an exception
         * is thrown.
         *
         * <p><b>Note:</b> The key {@link Locale#UNICODE_LOCALE_EXTENSION
         * UNICODE_LOCALE_EXTENSION} ('u') is used for the Unicode locale extension.
         * Setting a value for this key replaces any existing Unicode locale key/type
         * pairs with those defined in the extension.
         *
         * <p><b>Note:</b> The key {@link Locale#PRIVATE_USE_EXTENSION
         * PRIVATE_USE_EXTENSION} ('x') is used for the private use code. To be
         * well-formed, the value for this key needs only to have subtags of one to
         * eight alphanumeric characters, not two to eight as in the general case.
         *
         * @param key the extension key
         * @param value the extension value
         * @return This builder.
         * @throws IllformedLocaleException if <code>key</code> is illegal
         * or <code>value</code> is ill-formed
         * @see #setUnicodeLocaleKeyword(String, String)
         */
        public Builder setExtension(char key, String value) {
            try {
                localeBuilder.setExtension(key, value);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Sets the Unicode locale keyword type for the given key.  If the type
         * is null, the Unicode keyword is removed.  Otherwise, the key must be
         * non-null and both key and type must be <a
         * href="./Locale.html#def_locale_extension">well-formed</a> or an exception
         * is thrown.
         *
         * <p>Keys and types are converted to lower case.
         *
         * <p><b>Note</b>:Setting the 'u' extension via {@link #setExtension}
         * replaces all Unicode locale keywords with those defined in the
         * extension.
         *
         * @param key the Unicode locale key
         * @param type the Unicode locale type
         * @return This builder.
         * @throws IllformedLocaleException if <code>key</code> or <code>type</code>
         * is ill-formed
         * @throws NullPointerException if <code>key</code> is null
         * @see #setExtension(char, String)
         */
        public Builder setUnicodeLocaleKeyword(String key, String type) {
            try {
                localeBuilder.setUnicodeLocaleKeyword(key, type);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Adds a unicode locale attribute, if not already present, otherwise
         * has no effect.  The attribute must not be null and must be <a
         * href="./Locale.html#def_locale_extension">well-formed</a> or an exception
         * is thrown.
         *
         * @param attribute the attribute
         * @return This builder.
         * @throws NullPointerException if <code>attribute</code> is null
         * @throws IllformedLocaleException if <code>attribute</code> is ill-formed
         * @see #setExtension(char, String)
         */
        public Builder addUnicodeLocaleAttribute(String attribute) {
            try {
                localeBuilder.addUnicodeLocaleAttribute(attribute);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Removes a unicode locale attribute, if present, otherwise has no
         * effect.  The attribute must not be null and must be <a
         * href="./Locale.html#def_locale_extension">well-formed</a> or an exception
         * is thrown.
         *
         * <p>Attribute comparision for removal is case-insensitive.
         *
         * @param attribute the attribute
         * @return This builder.
         * @throws NullPointerException if <code>attribute</code> is null
         * @throws IllformedLocaleException if <code>attribute</code> is ill-formed
         * @see #setExtension(char, String)
         */
        public Builder removeUnicodeLocaleAttribute(String attribute) {
            if (attribute == null) {
                throw new NullPointerException("attribute == null");
            }

            try {
                localeBuilder.removeUnicodeLocaleAttribute(attribute);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Resets the builder to its initial, empty state.
         *
         * @return This builder.
         */
        public Builder clear() {
            localeBuilder.clear();
            return this;
        }

        /**
         * Resets the extensions to their initial, empty state.
         * Language, script, region and variant are unchanged.
         *
         * @return This builder.
         * @see #setExtension(char, String)
         */
        public Builder clearExtensions() {
            localeBuilder.clearExtensions();
            return this;
        }

        /**
         * Returns an instance of <code>Locale</code> created from the fields set
         * on this builder.
         *
         * <p>This applies the conversions listed in {@link Locale#forLanguageTag}
         * when constructing a Locale. (Grandfathered tags are handled in
         * {@link #setLanguageTag}.)
         *
         * @return A Locale.
         */
        public Locale build() {
            BaseLocale baseloc = localeBuilder.getBaseLocale();
            LocaleExtensions extensions = localeBuilder.getLocaleExtensions();
            if (extensions == null && baseloc.getVariant().length() > 0) {
                extensions = getCompatibilityExtensions(baseloc.getLanguage(), baseloc.getScript(),
                        baseloc.getRegion(), baseloc.getVariant());
            }
            return Locale.getInstance(baseloc, extensions);
        }
    }
}
