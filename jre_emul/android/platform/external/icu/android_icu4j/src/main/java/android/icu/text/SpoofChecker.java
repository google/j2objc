/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ***************************************************************************
 * Copyright (C) 2008-2016 International Business Machines Corporation
 * and others. All Rights Reserved.
 ***************************************************************************
 *
 * Unicode Spoof Detection
 */

package android.icu.text;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.icu.impl.ICUBinary;
import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterCategory;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.util.ULocale;

/**
 * <p>
 * This class, based on <a href="http://unicode.org/reports/tr36">Unicode Technical Report #36</a> and
 * <a href="http://unicode.org/reports/tr39">Unicode Technical Standard #39</a>, has two main functions:
 *
 * <ol>
 * <li>Checking whether two strings are visually <em>confusable</em> with each other, such as "desordenado" and
 * "ԁеѕогԁепаԁо".</li>
 * <li>Checking whether an individual string is likely to be an attempt at confusing the reader (<em>spoof
 * detection</em>), such as "pаypаl" spelled with Cyrillic 'а' characters.</li>
 * </ol>
 *
 * <p>
 * Although originally designed as a method for flagging suspicious identifier strings such as URLs,
 * <code>SpoofChecker</code> has a number of other practical use cases, such as preventing attempts to evade bad-word
 * content filters.
 *
 * <h2>Confusables</h2>
 *
 * <p>
 * The following example shows how to use <code>SpoofChecker</code> to check for confusability between two strings:
 *
 * <pre>
 * <code>
 * SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CONFUSABLE).build();
 * int result = sc.areConfusable("desordenado", "ԁеѕогԁепаԁо");
 * System.out.println(result != 0);  // true
 * </code>
 * </pre>
 *
 * <p>
 * <code>SpoofChecker</code> uses a builder paradigm: options are specified within the context of a lightweight
 * {@link SpoofChecker.Builder} object, and upon calling {@link SpoofChecker.Builder#build}, expensive data loading
 * operations are performed, and an immutable <code>SpoofChecker</code> is returned.
 *
 * <p>
 * The first line of the example creates a <code>SpoofChecker</code> object with confusable-checking enabled; the second
 * line performs the confusability test. For best performance, the instance should be created once (e.g., upon
 * application startup), and the more efficient {@link SpoofChecker#areConfusable} method can be used at runtime.
 *
 * <p>
 * UTS 39 defines two strings to be <em>confusable</em> if they map to the same skeleton. A <em>skeleton</em> is a
 * sequence of families of confusable characters, where each family has a single exemplar character.
 * {@link SpoofChecker#getSkeleton} computes the skeleton for a particular string, so the following snippet is
 * equivalent to the example above:
 *
 * <pre>
 * <code>
 * SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CONFUSABLE).build();
 * boolean result = sc.getSkeleton("desordenado").equals(sc.getSkeleton("ԁеѕогԁепаԁо"));
 * System.out.println(result);  // true
 * </code>
 * </pre>
 *
 * <p>
 * If you need to check if a string is confusable with any string in a dictionary of many strings, rather than calling
 * {@link SpoofChecker#areConfusable} many times in a loop, {@link SpoofChecker#getSkeleton} can be used instead, as
 * shown below:
 *
 * <pre>
 * // Setup:
 * String[] DICTIONARY = new String[]{ "lorem", "ipsum" }; // example
 * SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CONFUSABLE).build();
 * HashSet&lt;String&gt; skeletons = new HashSet&lt;String&gt;();
 * for (String word : DICTIONARY) {
 *   skeletons.add(sc.getSkeleton(word));
 * }
 *
 * // Live Check:
 * boolean result = skeletons.contains(sc.getSkeleton("1orern"));
 * System.out.println(result);  // true
 * </pre>
 *
 * <p>
 * <b>Note:</b> Since the Unicode confusables mapping table is frequently updated, confusable skeletons are <em>not</em>
 * guaranteed to be the same between ICU releases. We therefore recommend that you always compute confusable skeletons
 * at runtime and do not rely on creating a permanent, or difficult to update, database of skeletons.
 *
 * <h2>Spoof Detection</h2>
 *
 * <p>
 * The following snippet shows a minimal example of using <code>SpoofChecker</code> to perform spoof detection on a
 * string:
 *
 * <pre>
 * SpoofChecker sc = new SpoofChecker.Builder()
 *     .setAllowedChars(SpoofChecker.RECOMMENDED.cloneAsThawed().addAll(SpoofChecker.INCLUSION))
 *     .setRestrictionLevel(SpoofChecker.RestrictionLevel.MODERATELY_RESTRICTIVE)
 *     .setChecks(SpoofChecker.ALL_CHECKS &~ SpoofChecker.CONFUSABLE)
 *     .build();
 * boolean result = sc.failsChecks("pаypаl");  // with Cyrillic 'а' characters
 * System.out.println(result);  // true
 * </pre>
 *
 * <p>
 * As in the case for confusability checking, it is good practice to create one <code>SpoofChecker</code> instance at
 * startup, and call the cheaper {@link SpoofChecker#failsChecks} online. In the second line, we specify the set of
 * allowed characters to be those with type RECOMMENDED or INCLUSION, according to the recommendation in UTS 39. In the
 * third line, the CONFUSABLE checks are disabled. It is good practice to disable them if you won't be using the
 * instance to perform confusability checking.
 *
 * <p>
 * To get more details on why a string failed the checks, use a {@link SpoofChecker.CheckResult}:
 *
 * <pre>
 * <code>
 * SpoofChecker sc = new SpoofChecker.Builder()
 *     .setAllowedChars(SpoofChecker.RECOMMENDED.cloneAsThawed().addAll(SpoofChecker.INCLUSION))
 *     .setRestrictionLevel(SpoofChecker.RestrictionLevel.MODERATELY_RESTRICTIVE)
 *     .setChecks(SpoofChecker.ALL_CHECKS &~ SpoofChecker.CONFUSABLE)
 *     .build();
 * SpoofChecker.CheckResult checkResult = new SpoofChecker.CheckResult();
 * boolean result = sc.failsChecks("pаypаl", checkResult);
 * System.out.println(checkResult.checks);  // 16
 * </code>
 * </pre>
 *
 * <p>
 * The return value is a bitmask of the checks that failed. In this case, there was one check that failed:
 * {@link SpoofChecker#RESTRICTION_LEVEL}, corresponding to the fifth bit (16). The possible checks are:
 *
 * <ul>
 * <li><code>RESTRICTION_LEVEL</code>: flags strings that violate the
 * <a href="http://unicode.org/reports/tr39/#Restriction_Level_Detection">Restriction Level</a> test as specified in UTS
 * 39; in most cases, this means flagging strings that contain characters from multiple different scripts.</li>
 * <li><code>INVISIBLE</code>: flags strings that contain invisible characters, such as zero-width spaces, or character
 * sequences that are likely not to display, such as multiple occurrences of the same non-spacing mark.</li>
 * <li><code>CHAR_LIMIT</code>: flags strings that contain characters outside of a specified set of acceptable
 * characters. See {@link SpoofChecker.Builder#setAllowedChars} and {@link SpoofChecker.Builder#setAllowedLocales}.</li>
 * <li><code>MIXED_NUMBERS</code>: flags strings that contain digits from multiple different numbering systems.</li>
 * </ul>
 *
 * <p>
 * These checks can be enabled independently of each other. For example, if you were interested in checking for only the
 * INVISIBLE and MIXED_NUMBERS conditions, you could do:
 *
 * <pre>
 * <code>
 * SpoofChecker sc = new SpoofChecker.Builder()
 *     .setChecks(SpoofChecker.INVISIBLE | SpoofChecker.MIXED_NUMBERS)
 *     .build();
 * boolean result = sc.failsChecks("৪8");
 * System.out.println(result);  // true
 * </code>
 * </pre>
 *
 * <p>
 * <b>Note:</b> The Restriction Level is the most powerful of the checks. The full logic is documented in
 * <a href="http://unicode.org/reports/tr39/#Restriction_Level_Detection">UTS 39</a>, but the basic idea is that strings
 * are restricted to contain characters from only a single script, <em>except</em> that most scripts are allowed to have
 * Latin characters interspersed. Although the default restriction level is <code>HIGHLY_RESTRICTIVE</code>, it is
 * recommended that users set their restriction level to <code>MODERATELY_RESTRICTIVE</code>, which allows Latin mixed
 * with all other scripts except Cyrillic, Greek, and Cherokee, with which it is often confusable. For more details on
 * the levels, see UTS 39 or {@link SpoofChecker.RestrictionLevel}. The Restriction Level test is aware of the set of
 * allowed characters set in {@link SpoofChecker.Builder#setAllowedChars}. Note that characters which have script code
 * COMMON or INHERITED, such as numbers and punctuation, are ignored when computing whether a string has multiple
 * scripts.
 *
 * <h2>Additional Information</h2>
 *
 * <p>
 * A <code>SpoofChecker</code> instance may be used repeatedly to perform checks on any number of identifiers.
 *
 * <p>
 * <b>Thread Safety:</b> The methods on <code>SpoofChecker</code> objects are thread safe. The test functions for
 * checking a single identifier, or for testing whether two identifiers are potentially confusable, may called
 * concurrently from multiple threads using the same <code>SpoofChecker</code> instance.
 *
 * @hide Only a subset of ICU is exposed in Android
 */
public class SpoofChecker {

    /**
     * Constants from UTS 39 for use in setRestrictionLevel.
     */
    public enum RestrictionLevel {
        /**
         * All characters in the string are in the identifier profile and all characters in the string are in the ASCII
         * range.
         */
        ASCII,
        /**
         * The string classifies as ASCII-Only, or all characters in the string are in the identifier profile and the
         * string is single-script, according to the definition in UTS 39 section 5.1.
         */
        SINGLE_SCRIPT_RESTRICTIVE,
        /**
         * The string classifies as Single Script, or all characters in the string are in the identifier profile and the
         * string is covered by any of the following sets of scripts, according to the definition in UTS 39 section 5.1:
         * <ul>
         * <li>Latin + Han + Bopomofo (or equivalently: Latn + Hanb)</li>
         * <li>Latin + Han + Hiragana + Katakana (or equivalently: Latn + Jpan)</li>
         * <li>Latin + Han + Hangul (or equivalently: Latn +Kore)</li>
         * </ul>
         */
        HIGHLY_RESTRICTIVE,
        /**
         * The string classifies as Highly Restrictive, or all characters in the string are in the identifier profile
         * and the string is covered by Latin and any one other Recommended or Aspirational script, except Cyrillic,
         * Greek, and Cherokee.
         */
        MODERATELY_RESTRICTIVE,
        /**
         * All characters in the string are in the identifier profile. Allow arbitrary mixtures of scripts, such as
         * Ωmega, Teχ, HλLF-LIFE, Toys-Я-Us.
         */
        MINIMALLY_RESTRICTIVE,
        /**
         * Any valid identifiers, including characters outside of the Identifier Profile, such as I♥NY.org
         */
        UNRESTRICTIVE,
    }

    /**
     * Security Profile constant from UTS 39 for use in {@link SpoofChecker.Builder#setAllowedChars}.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final UnicodeSet INCLUSION = new UnicodeSet(
            "['\\-.\\:\\u00B7\\u0375\\u058A\\u05F3\\u05F4\\u06FD\\u06FE\\u0F0B\\u200C\\u200D\\u2010\\u"
                    + "2019\\u2027\\u30A0\\u30FB]").freeze();
    // Note: data from http://unicode.org/Public/security/9.0.0/IdentifierStatus.txt
    // There is tooling to generate this constant in the unicodetools project:
    //      org.unicode.text.tools.RecommendedSetGenerator
    // It will print the Java and C++ code to the console for easy copy-paste into this file.

    /**
     * Security Profile constant from UTS 39 for use in {@link SpoofChecker.Builder#setAllowedChars}.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final UnicodeSet RECOMMENDED = new UnicodeSet(
            "[0-9A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u0131\\u0134-\\u013E\\u0141-\\u014"
                    + "8\\u014A-\\u017E\\u018F\\u01A0\\u01A1\\u01AF\\u01B0\\u01CD-\\u01DC\\u01DE-\\u01E3\\u01E"
                    + "6-\\u01F0\\u01F4\\u01F5\\u01F8-\\u021B\\u021E\\u021F\\u0226-\\u0233\\u0259\\u02BB\\u02B"
                    + "C\\u02EC\\u0300-\\u0304\\u0306-\\u030C\\u030F-\\u0311\\u0313\\u0314\\u031B\\u0323-\\u03"
                    + "28\\u032D\\u032E\\u0330\\u0331\\u0335\\u0338\\u0339\\u0342\\u0345\\u037B-\\u037D\\u0386"
                    + "\\u0388-\\u038A\\u038C\\u038E-\\u03A1\\u03A3-\\u03CE\\u03FC-\\u045F\\u048A-\\u0529\\u05"
                    + "2E\\u052F\\u0531-\\u0556\\u0559\\u0561-\\u0586\\u05B4\\u05D0-\\u05EA\\u05F0-\\u05F2\\u0"
                    + "620-\\u063F\\u0641-\\u0655\\u0660-\\u0669\\u0670-\\u0672\\u0674\\u0679-\\u068D\\u068F-"
                    + "\\u06D3\\u06D5\\u06E5\\u06E6\\u06EE-\\u06FC\\u06FF\\u0750-\\u07B1\\u08A0-\\u08AC\\u08B2"
                    + "\\u08B6-\\u08BD\\u0901-\\u094D\\u094F\\u0950\\u0956\\u0957\\u0960-\\u0963\\u0966-\\u096"
                    + "F\\u0971-\\u0977\\u0979-\\u097F\\u0981-\\u0983\\u0985-\\u098C\\u098F\\u0990\\u0993-\\u0"
                    + "9A8\\u09AA-\\u09B0\\u09B2\\u09B6-\\u09B9\\u09BC-\\u09C4\\u09C7\\u09C8\\u09CB-\\u09CE\\u"
                    + "09D7\\u09E0-\\u09E3\\u09E6-\\u09F1\\u0A01-\\u0A03\\u0A05-\\u0A0A\\u0A0F\\u0A10\\u0A13-"
                    + "\\u0A28\\u0A2A-\\u0A30\\u0A32\\u0A35\\u0A38\\u0A39\\u0A3C\\u0A3E-\\u0A42\\u0A47\\u0A48\\"
                    + "u0A4B-\\u0A4D\\u0A5C\\u0A66-\\u0A74\\u0A81-\\u0A83\\u0A85-\\u0A8D\\u0A8F-\\u0A91\\u0A9"
                    + "3-\\u0AA8\\u0AAA-\\u0AB0\\u0AB2\\u0AB3\\u0AB5-\\u0AB9\\u0ABC-\\u0AC5\\u0AC7-\\u0AC9\\u0"
                    + "ACB-\\u0ACD\\u0AD0\\u0AE0-\\u0AE3\\u0AE6-\\u0AEF\\u0B01-\\u0B03\\u0B05-\\u0B0C\\u0B0F\\"
                    + "u0B10\\u0B13-\\u0B28\\u0B2A-\\u0B30\\u0B32\\u0B33\\u0B35-\\u0B39\\u0B3C-\\u0B43\\u0B47"
                    + "\\u0B48\\u0B4B-\\u0B4D\\u0B56\\u0B57\\u0B5F-\\u0B61\\u0B66-\\u0B6F\\u0B71\\u0B82\\u0B83"
                    + "\\u0B85-\\u0B8A\\u0B8E-\\u0B90\\u0B92-\\u0B95\\u0B99\\u0B9A\\u0B9C\\u0B9E\\u0B9F\\u0BA3"
                    + "\\u0BA4\\u0BA8-\\u0BAA\\u0BAE-\\u0BB9\\u0BBE-\\u0BC2\\u0BC6-\\u0BC8\\u0BCA-\\u0BCD\\u0B"
                    + "D0\\u0BD7\\u0BE6-\\u0BEF\\u0C01-\\u0C03\\u0C05-\\u0C0C\\u0C0E-\\u0C10\\u0C12-\\u0C28\\u"
                    + "0C2A-\\u0C33\\u0C35-\\u0C39\\u0C3D-\\u0C44\\u0C46-\\u0C48\\u0C4A-\\u0C4D\\u0C55\\u0C56"
                    + "\\u0C60\\u0C61\\u0C66-\\u0C6F\\u0C80\\u0C82\\u0C83\\u0C85-\\u0C8C\\u0C8E-\\u0C90\\u0C92"
                    + "-\\u0CA8\\u0CAA-\\u0CB3\\u0CB5-\\u0CB9\\u0CBC-\\u0CC4\\u0CC6-\\u0CC8\\u0CCA-\\u0CCD\\u0"
                    + "CD5\\u0CD6\\u0CE0-\\u0CE3\\u0CE6-\\u0CEF\\u0CF1\\u0CF2\\u0D02\\u0D03\\u0D05-\\u0D0C\\u0"
                    + "D0E-\\u0D10\\u0D12-\\u0D3A\\u0D3D-\\u0D43\\u0D46-\\u0D48\\u0D4A-\\u0D4E\\u0D54-\\u0D57"
                    + "\\u0D60\\u0D61\\u0D66-\\u0D6F\\u0D7A-\\u0D7F\\u0D82\\u0D83\\u0D85-\\u0D8E\\u0D91-\\u0D9"
                    + "6\\u0D9A-\\u0DA5\\u0DA7-\\u0DB1\\u0DB3-\\u0DBB\\u0DBD\\u0DC0-\\u0DC6\\u0DCA\\u0DCF-\\u0"
                    + "DD4\\u0DD6\\u0DD8-\\u0DDE\\u0DF2\\u0E01-\\u0E32\\u0E34-\\u0E3A\\u0E40-\\u0E4E\\u0E50-\\"
                    + "u0E59\\u0E81\\u0E82\\u0E84\\u0E87\\u0E88\\u0E8A\\u0E8D\\u0E94-\\u0E97\\u0E99-\\u0E9F\\u"
                    + "0EA1-\\u0EA3\\u0EA5\\u0EA7\\u0EAA\\u0EAB\\u0EAD-\\u0EB2\\u0EB4-\\u0EB9\\u0EBB-\\u0EBD\\"
                    + "u0EC0-\\u0EC4\\u0EC6\\u0EC8-\\u0ECD\\u0ED0-\\u0ED9\\u0EDE\\u0EDF\\u0F00\\u0F20-\\u0F29"
                    + "\\u0F35\\u0F37\\u0F3E-\\u0F42\\u0F44-\\u0F47\\u0F49-\\u0F4C\\u0F4E-\\u0F51\\u0F53-\\u0F"
                    + "56\\u0F58-\\u0F5B\\u0F5D-\\u0F68\\u0F6A-\\u0F6C\\u0F71\\u0F72\\u0F74\\u0F7A-\\u0F80\\u0"
                    + "F82-\\u0F84\\u0F86-\\u0F92\\u0F94-\\u0F97\\u0F99-\\u0F9C\\u0F9E-\\u0FA1\\u0FA3-\\u0FA6"
                    + "\\u0FA8-\\u0FAB\\u0FAD-\\u0FB8\\u0FBA-\\u0FBC\\u0FC6\\u1000-\\u1049\\u1050-\\u109D\\u10"
                    + "C7\\u10CD\\u10D0-\\u10F0\\u10F7-\\u10FA\\u10FD-\\u10FF\\u1200-\\u1248\\u124A-\\u124D\\u"
                    + "1250-\\u1256\\u1258\\u125A-\\u125D\\u1260-\\u1288\\u128A-\\u128D\\u1290-\\u12B0\\u12B2"
                    + "-\\u12B5\\u12B8-\\u12BE\\u12C0\\u12C2-\\u12C5\\u12C8-\\u12D6\\u12D8-\\u1310\\u1312-\\u1"
                    + "315\\u1318-\\u135A\\u135D-\\u135F\\u1380-\\u138F\\u1780-\\u17A2\\u17A5-\\u17A7\\u17A9-"
                    + "\\u17B3\\u17B6-\\u17CA\\u17D2\\u17D7\\u17DC\\u17E0-\\u17E9\\u1C80-\\u1C88\\u1E00-\\u1E9"
                    + "9\\u1E9E\\u1EA0-\\u1EF9\\u1F00-\\u1F15\\u1F18-\\u1F1D\\u1F20-\\u1F45\\u1F48-\\u1F4D\\u1"
                    + "F50-\\u1F57\\u1F59\\u1F5B\\u1F5D\\u1F5F-\\u1F70\\u1F72\\u1F74\\u1F76\\u1F78\\u1F7A\\u1F"
                    + "7C\\u1F80-\\u1FB4\\u1FB6-\\u1FBA\\u1FBC\\u1FC2-\\u1FC4\\u1FC6-\\u1FC8\\u1FCA\\u1FCC\\u1"
                    + "FD0-\\u1FD2\\u1FD6-\\u1FDA\\u1FE0-\\u1FE2\\u1FE4-\\u1FEA\\u1FEC\\u1FF2-\\u1FF4\\u1FF6-"
                    + "\\u1FF8\\u1FFA\\u1FFC\\u2D27\\u2D2D\\u2D80-\\u2D96\\u2DA0-\\u2DA6\\u2DA8-\\u2DAE\\u2DB0"
                    + "-\\u2DB6\\u2DB8-\\u2DBE\\u2DC0-\\u2DC6\\u2DC8-\\u2DCE\\u2DD0-\\u2DD6\\u2DD8-\\u2DDE\\u3"
                    + "005-\\u3007\\u3041-\\u3096\\u3099\\u309A\\u309D\\u309E\\u30A1-\\u30FA\\u30FC-\\u30FE\\u"
                    + "3105-\\u312D\\u31A0-\\u31BA\\u3400-\\u4DB5\\u4E00-\\u9FD5\\uA660\\uA661\\uA674-\\uA67B"
                    + "\\uA67F\\uA69F\\uA717-\\uA71F\\uA788\\uA78D\\uA78E\\uA790-\\uA793\\uA7A0-\\uA7AA\\uA7AE"
                    + "\\uA7FA\\uA9E7-\\uA9FE\\uAA60-\\uAA76\\uAA7A-\\uAA7F\\uAB01-\\uAB06\\uAB09-\\uAB0E\\uAB"
                    + "11-\\uAB16\\uAB20-\\uAB26\\uAB28-\\uAB2E\\uAC00-\\uD7A3\\uFA0E\\uFA0F\\uFA11\\uFA13\\uF"
                    + "A14\\uFA1F\\uFA21\\uFA23\\uFA24\\uFA27-\\uFA29\\U00020000-\\U0002A6D6\\U0002A700-\\U0"
                    + "002B734\\U0002B740-\\U0002B81D\\U0002B820-\\U0002CEA1]").freeze();
    // Note: data from http://unicode.org/Public/security/9.0.0/IdentifierStatus.txt
    // There is tooling to generate this constant in the unicodetools project:
    //      org.unicode.text.tools.RecommendedSetGenerator
    // It will print the Java and C++ code to the console for easy copy-paste into this file.

    /**
     * Constants for the kinds of checks that USpoofChecker can perform. These values are used both to select the set of
     * checks that will be performed, and to report results from the check function.
     *
     */

    /**
     * When performing the two-string {@link SpoofChecker#areConfusable} test, this flag in the return value indicates
     * that the two strings are visually confusable and that they are from the same script, according to UTS 39 section
     * 4.
     */
    public static final int SINGLE_SCRIPT_CONFUSABLE = 1;

    /**
     * When performing the two-string {@link SpoofChecker#areConfusable} test, this flag in the return value indicates
     * that the two strings are visually confusable and that they are <b>not</b> from the same script, according to UTS
     * 39 section 4.
     */
    public static final int MIXED_SCRIPT_CONFUSABLE = 2;

    /**
     * When performing the two-string {@link SpoofChecker#areConfusable} test, this flag in the return value indicates
     * that the two strings are visually confusable and that they are not from the same script but both of them are
     * single-script strings, according to UTS 39 section 4.
     */
    public static final int WHOLE_SCRIPT_CONFUSABLE = 4;

    /**
     * Enable this flag in {@link SpoofChecker.Builder#setChecks} to turn on all types of confusables. You may set the
     * checks to some subset of SINGLE_SCRIPT_CONFUSABLE, MIXED_SCRIPT_CONFUSABLE, or WHOLE_SCRIPT_CONFUSABLE to make
     * {@link SpoofChecker#areConfusable} return only those types of confusables.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final int CONFUSABLE = SINGLE_SCRIPT_CONFUSABLE | MIXED_SCRIPT_CONFUSABLE | WHOLE_SCRIPT_CONFUSABLE;

    /**
     * This flag is deprecated and no longer affects the behavior of SpoofChecker.
     *
     * @deprecated ICU 58 Any case confusable mappings were removed from UTS 39; the corresponding ICU API was
     * deprecated.
     */
    @Deprecated
    public static final int ANY_CASE = 8;

    /**
     * Check that an identifier satisfies the requirements for the restriction level specified in
     * {@link SpoofChecker.Builder#setRestrictionLevel}. The default restriction level is
     * {@link RestrictionLevel#HIGHLY_RESTRICTIVE}.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final int RESTRICTION_LEVEL = 16;

    /**
     * Check that an identifier contains only characters from a single script (plus chars from the common and inherited
     * scripts.) Applies to checks of a single identifier check only.
     *
     * @deprecated ICU 51 Use RESTRICTION_LEVEL
     */
    @Deprecated
    public static final int SINGLE_SCRIPT = RESTRICTION_LEVEL;

    /**
     * Check an identifier for the presence of invisible characters, such as zero-width spaces, or character sequences
     * that are likely not to display, such as multiple occurrences of the same non-spacing mark. This check does not
     * test the input string as a whole for conformance to any particular syntax for identifiers.
     */
    public static final int INVISIBLE = 32;

    /**
     * Check that an identifier contains only characters from a specified set of acceptable characters. See
     * {@link Builder#setAllowedChars} and {@link Builder#setAllowedLocales}. Note that a string that fails this check
     * will also fail the {@link #RESTRICTION_LEVEL} check.
     */
    public static final int CHAR_LIMIT = 64;

    /**
     * Check that an identifier does not mix numbers from different numbering systems. For more information, see UTS 39
     * section 5.3.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final int MIXED_NUMBERS = 128;

    // Update CheckResult.toString() when a new check is added.

    /**
     * Enable all spoof checks.
     */
    public static final int ALL_CHECKS = 0xFFFFFFFF;

    // Used for checking for ASCII-Only restriction level
    static final UnicodeSet ASCII = new UnicodeSet(0, 0x7F).freeze();

    /**
     * private constructor: a SpoofChecker has to be built by the builder
     */
    private SpoofChecker() {
    }

    /**
     * SpoofChecker Builder. To create a SpoofChecker, first instantiate a SpoofChecker.Builder, set the desired
     * checking options on the builder, then call the build() function to create a SpoofChecker instance.
     */
    public static class Builder {
        int fChecks; // Bit vector of checks to perform.
        SpoofData fSpoofData;
        final UnicodeSet fAllowedCharsSet = new UnicodeSet(0, 0x10ffff); // The UnicodeSet of allowed characters.
        // for this Spoof Checker. Defaults to all chars.
        final Set<ULocale> fAllowedLocales = new LinkedHashSet<ULocale>(); // The list of allowed locales.
        private RestrictionLevel fRestrictionLevel;

        /**
         * Constructor: Create a default Unicode Spoof Checker Builder, configured to perform all checks except for
         * LOCALE_LIMIT and CHAR_LIMIT. Note that additional checks may be added in the future, resulting in the changes
         * to the default checking behavior.
         */
        public Builder() {
            fChecks = ALL_CHECKS;
            fSpoofData = null;
            fRestrictionLevel = RestrictionLevel.HIGHLY_RESTRICTIVE;
        }

        /**
         * Constructor: Create a Spoof Checker Builder, and set the configuration from an existing SpoofChecker.
         *
         * @param src
         *            The existing checker.
         */
        public Builder(SpoofChecker src) {
            fChecks = src.fChecks;
            fSpoofData = src.fSpoofData; // For the data, we will either use the source data
                                         // as-is, or drop the builder's reference to it
                                         // and generate new data, depending on what our
                                         // caller does with the builder.
            fAllowedCharsSet.set(src.fAllowedCharsSet);
            fAllowedLocales.addAll(src.fAllowedLocales);
            fRestrictionLevel = src.fRestrictionLevel;
        }

        /**
         * Create a SpoofChecker with current configuration.
         *
         * @return SpoofChecker
         */
        public SpoofChecker build() {
            // TODO: Make this data loading be lazy (see #12696).
            if (fSpoofData == null) {
                // read binary file
                fSpoofData = SpoofData.getDefault();
            }

            // Copy all state from the builder to the new SpoofChecker.
            // Make sure that everything is either cloned or copied, so
            // that subsequent re-use of the builder won't modify the built
            // SpoofChecker.
            //
            // One exception to this: the SpoofData is just assigned.
            // If the builder subsequently needs to modify fSpoofData
            // it will create a new SpoofData object first.

            SpoofChecker result = new SpoofChecker();
            result.fChecks = this.fChecks;
            result.fSpoofData = this.fSpoofData;
            result.fAllowedCharsSet = (UnicodeSet) (this.fAllowedCharsSet.clone());
            result.fAllowedCharsSet.freeze();
            result.fAllowedLocales = new HashSet<ULocale>(this.fAllowedLocales);
            result.fRestrictionLevel = this.fRestrictionLevel;
            return result;
        }

        /**
         * Specify the source form of the spoof data Spoof Checker. The inputs correspond to the Unicode data file
         * confusables.txt as described in Unicode UAX 39. The syntax of the source data is as described in UAX 39 for
         * these files, and the content of these files is acceptable input.
         *
         * @param confusables
         *            the Reader of confusable characters definitions, as found in file confusables.txt from
         *            unicode.org.
         * @throws ParseException
         *             To report syntax errors in the input.
         *
         * @hide draft / provisional / internal are hidden on Android
         */
        public Builder setData(Reader confusables) throws ParseException, IOException {

            // Compile the binary data from the source (text) format.
            // Drop the builder's reference to any pre-existing data, which may
            // be in use in an already-built checker.

            fSpoofData = new SpoofData();
            ConfusabledataBuilder.buildConfusableData(confusables, fSpoofData);
            return this;
        }

        /**
         * Deprecated as of ICU 58; use {@link SpoofChecker.Builder#setData(Reader confusables)} instead.
         *
         * @param confusables
         *            the Reader of confusable characters definitions, as found in file confusables.txt from
         *            unicode.org.
         * @param confusablesWholeScript
         *            No longer supported.
         * @throws ParseException
         *             To report syntax errors in the input.
         *
         * @deprecated ICU 58
         */
        @Deprecated
        public Builder setData(Reader confusables, Reader confusablesWholeScript) throws ParseException, IOException {
            setData(confusables);
            return this;
        }

        /**
         * Specify the bitmask of checks that will be performed by {@link SpoofChecker#failsChecks}. Calling this method
         * overwrites any checks that may have already been enabled. By default, all checks are enabled.
         *
         * To enable specific checks and disable all others, the "whitelisted" checks should be ORed together. For
         * example, to fail strings containing characters outside of the set specified by {@link #setAllowedChars} and
         * also strings that contain digits from mixed numbering systems:
         *
         * <pre>
         * {@code
         * builder.setChecks(SpoofChecker.CHAR_LIMIT | SpoofChecker.MIXED_NUMBERS);
         * }
         * </pre>
         *
         * To disable specific checks and enable all others, the "blacklisted" checks should be ANDed away from
         * ALL_CHECKS. For example, if you are not planning to use the {@link SpoofChecker#areConfusable} functionality,
         * it is good practice to disable the CONFUSABLE check:
         *
         * <pre>
         * {@code
         * builder.setChecks(SpoofChecker.ALL_CHECKS & ~SpoofChecker.CONFUSABLE);
         * }
         * </pre>
         *
         * Note that methods such as {@link #setAllowedChars}, {@link #setAllowedLocales}, and
         * {@link #setRestrictionLevel} will enable certain checks when called. Those methods will OR the check they
         * enable onto the existing bitmask specified by this method. For more details, see the documentation of those
         * methods.
         *
         * @param checks
         *            The set of checks that this spoof checker will perform. The value is an 'or' of the desired
         *            checks.
         * @return self
         */
        public Builder setChecks(int checks) {
            // Verify that the requested checks are all ones (bits) that
            // are acceptable, known values.
            if (0 != (checks & ~SpoofChecker.ALL_CHECKS)) {
                throw new IllegalArgumentException("Bad Spoof Checks value.");
            }
            this.fChecks = (checks & SpoofChecker.ALL_CHECKS);
            return this;
        }

        /**
         * Limit characters that are acceptable in identifiers being checked to those normally used with the languages
         * associated with the specified locales. Any previously specified list of locales is replaced by the new
         * settings.
         *
         * A set of languages is determined from the locale(s), and from those a set of acceptable Unicode scripts is
         * determined. Characters from this set of scripts, along with characters from the "common" and "inherited"
         * Unicode Script categories will be permitted.
         *
         * Supplying an empty string removes all restrictions; characters from any script will be allowed.
         *
         * The {@link #CHAR_LIMIT} test is automatically enabled for this SpoofChecker when calling this function with a
         * non-empty list of locales.
         *
         * The Unicode Set of characters that will be allowed is accessible via the {@link #getAllowedChars} function.
         * setAllowedLocales() will <i>replace</i> any previously applied set of allowed characters.
         *
         * Adjustments, such as additions or deletions of certain classes of characters, can be made to the result of
         * {@link #setAllowedChars} by fetching the resulting set with {@link #getAllowedChars}, manipulating it with
         * the Unicode Set API, then resetting the spoof detectors limits with {@link #setAllowedChars}.
         *
         * @param locales
         *            A Set of ULocales, from which the language and associated script are extracted. If the locales Set
         *            is null, no restrictions will be placed on the allowed characters.
         *
         * @return self
         */
        public Builder setAllowedLocales(Set<ULocale> locales) {
            fAllowedCharsSet.clear();

            for (ULocale locale : locales) {
                // Add the script chars for this locale to the accumulating set
                // of allowed chars.
                addScriptChars(locale, fAllowedCharsSet);
            }

            // If our caller provided an empty list of locales, we disable the
            // allowed characters checking
            fAllowedLocales.clear();
            if (locales.size() == 0) {
                fAllowedCharsSet.add(0, 0x10ffff);
                fChecks &= ~CHAR_LIMIT;
                return this;
            }

            // Add all common and inherited characters to the set of allowed
            // chars.
            UnicodeSet tempSet = new UnicodeSet();
            tempSet.applyIntPropertyValue(UProperty.SCRIPT, UScript.COMMON);
            fAllowedCharsSet.addAll(tempSet);
            tempSet.applyIntPropertyValue(UProperty.SCRIPT, UScript.INHERITED);
            fAllowedCharsSet.addAll(tempSet);

            // Store the updated spoof checker state.
            fAllowedLocales.clear();
            fAllowedLocales.addAll(locales);
            fChecks |= CHAR_LIMIT;
            return this;
        }

        /**
         * Limit characters that are acceptable in identifiers being checked to those normally used with the languages
         * associated with the specified locales. Any previously specified list of locales is replaced by the new
         * settings.
         *
         * @param locales
         *            A Set of Locales, from which the language and associated script are extracted. If the locales Set
         *            is null, no restrictions will be placed on the allowed characters.
         *
         * @return self
         */
        public Builder setAllowedJavaLocales(Set<Locale> locales) {
            HashSet<ULocale> ulocales = new HashSet<ULocale>(locales.size());
            for (Locale locale : locales) {
                ulocales.add(ULocale.forLocale(locale));
            }
            return setAllowedLocales(ulocales);
        }

        // Add (union) to the UnicodeSet all of the characters for the scripts
        // used for the specified locale. Part of the implementation of
        // setAllowedLocales.
        private void addScriptChars(ULocale locale, UnicodeSet allowedChars) {
            int scripts[] = UScript.getCode(locale);
            if (scripts != null) {
                UnicodeSet tmpSet = new UnicodeSet();
                for (int i = 0; i < scripts.length; i++) {
                    tmpSet.applyIntPropertyValue(UProperty.SCRIPT, scripts[i]);
                    allowedChars.addAll(tmpSet);
                }
            }
            // else it's an unknown script.
            // Maybe they asked for the script of "zxx", which refers to no linguistic content.
            // Maybe they asked for the script of a newer locale that we don't know in the older version of ICU.
        }

        /**
         * Limit the acceptable characters to those specified by a Unicode Set. Any previously specified character limit
         * is is replaced by the new settings. This includes limits on characters that were set with the
         * setAllowedLocales() function. Note that the RESTRICTED set is useful.
         *
         * The {@link #CHAR_LIMIT} test is automatically enabled for this SpoofChecker by this function.
         *
         * @param chars
         *            A Unicode Set containing the list of characters that are permitted. The incoming set is cloned by
         *            this function, so there are no restrictions on modifying or deleting the UnicodeSet after calling
         *            this function. Note that this clears the allowedLocales set.
         * @return self
         */
        public Builder setAllowedChars(UnicodeSet chars) {
            fAllowedCharsSet.set(chars);
            fAllowedLocales.clear();
            fChecks |= CHAR_LIMIT;
            return this;
        }

        /**
         * Set the loosest restriction level allowed for strings. The default if this is not called is
         * {@link RestrictionLevel#HIGHLY_RESTRICTIVE}. Calling this method enables the {@link #RESTRICTION_LEVEL} and
         * {@link #MIXED_NUMBERS} checks, corresponding to Sections 5.1 and 5.2 of UTS 39. To customize which checks are
         * to be performed by {@link SpoofChecker#failsChecks}, see {@link #setChecks}.
         *
         * @param restrictionLevel
         *            The loosest restriction level allowed.
         * @return self
         * @hide draft / provisional / internal are hidden on Android
         */
        public Builder setRestrictionLevel(RestrictionLevel restrictionLevel) {
            fRestrictionLevel = restrictionLevel;
            fChecks |= RESTRICTION_LEVEL | MIXED_NUMBERS;
            return this;
        }

        /*
         * *****************************************************************************
         * Internal classes for compililing confusable data into its binary (runtime) form.
         * *****************************************************************************
         */
        // ---------------------------------------------------------------------
        //
        // buildConfusableData Compile the source confusable data, as defined by
        // the Unicode data file confusables.txt, into the binary
        // structures used by the confusable detector.
        //
        // The binary structures are described in uspoof_impl.h
        //
        // 1. parse the data, making a hash table mapping from a codepoint to a String.
        //
        // 2. Sort all of the strings encountered by length, since they will need to
        // be stored in that order in the final string table.
        // TODO: Sorting these strings by length is no longer needed since the removal of
        // the string lengths table.  This logic can be removed to save processing time
        // when building confusables data.
        //
        // 3. Build a list of keys (UChar32s) from the mapping table. Sort the
        // list because that will be the ordering of our runtime table.
        //
        // 4. Generate the run time string table. This is generated before the key & value
        // table because we need the string indexes when building those tables.
        //
        // 5. Build the run-time key and value table. These are parallel tables, and
        // are built at the same time

        // class ConfusabledataBuilder
        // An instance of this class exists while the confusable data is being built from source.
        // It encapsulates the intermediate data structures that are used for building.
        // It exports one static function, to do a confusable data build.
        private static class ConfusabledataBuilder {

            private Hashtable<Integer, SPUString> fTable;
            private UnicodeSet fKeySet; // A set of all keys (UChar32s) that go into the
                                        // four mapping tables.

            // The compiled data is first assembled into the following four collections,
            // then output to the builder's SpoofData object.
            private StringBuffer fStringTable;
            private ArrayList<Integer> fKeyVec;
            private ArrayList<Integer> fValueVec;
            private SPUStringPool stringPool;
            private Pattern fParseLine;
            private Pattern fParseHexNum;
            private int fLineNum;

            ConfusabledataBuilder() {
                fTable = new Hashtable<Integer, SPUString>();
                fKeySet = new UnicodeSet();
                fKeyVec = new ArrayList<Integer>();
                fValueVec = new ArrayList<Integer>();
                stringPool = new SPUStringPool();
            }

            void build(Reader confusables, SpoofData dest) throws ParseException, java.io.IOException {
                StringBuffer fInput = new StringBuffer();

                // Convert the user input data from UTF-8 to char (UTF-16)
                LineNumberReader lnr = new LineNumberReader(confusables);
                do {
                    String line = lnr.readLine();
                    if (line == null) {
                        break;
                    }
                    fInput.append(line);
                    fInput.append('\n');
                } while (true);

                // Regular Expression to parse a line from Confusables.txt. The expression will match
                // any line. What was matched is determined by examining which capture groups have a match.
                // Capture Group 1: the source char
                // Capture Group 2: the replacement chars
                // Capture Group 3-6 the table type, SL, SA, ML, or MA (deprecated)
                // Capture Group 7: A blank or comment only line.
                // Capture Group 8: A syntactically invalid line. Anything that didn't match before.
                // Example Line from the confusables.txt source file:
                // "1D702 ; 006E 0329 ; SL # MATHEMATICAL ITALIC SMALL ETA ... "
                fParseLine = Pattern.compile("(?m)^[ \\t]*([0-9A-Fa-f]+)[ \\t]+;" + // Match the source char
                        "[ \\t]*([0-9A-Fa-f]+" + // Match the replacement char(s)
                        "(?:[ \\t]+[0-9A-Fa-f]+)*)[ \\t]*;" + // (continued)
                        "\\s*(?:(SL)|(SA)|(ML)|(MA))" + // Match the table type
                        "[ \\t]*(?:#.*?)?$" + // Match any trailing #comment
                        "|^([ \\t]*(?:#.*?)?)$" + // OR match empty lines or lines with only a #comment
                        "|^(.*?)$"); // OR match any line, which catches illegal lines.

                // Regular expression for parsing a hex number out of a space-separated list of them.
                // Capture group 1 gets the number, with spaces removed.
                fParseHexNum = Pattern.compile("\\s*([0-9A-F]+)");

                // Zap any Byte Order Mark at the start of input. Changing it to a space
                // is benign given the syntax of the input.
                if (fInput.charAt(0) == 0xfeff) {
                    fInput.setCharAt(0, (char) 0x20);
                }

                // Parse the input, one line per iteration of this loop.
                Matcher matcher = fParseLine.matcher(fInput);
                while (matcher.find()) {
                    fLineNum++;
                    if (matcher.start(7) >= 0) {
                        // this was a blank or comment line.
                        continue;
                    }
                    if (matcher.start(8) >= 0) {
                        // input file syntax error.
                        // status = U_PARSE_ERROR;
                        throw new ParseException(
                                "Confusables, line " + fLineNum + ": Unrecognized Line: " + matcher.group(8),
                                matcher.start(8));
                    }

                    // We have a good input line. Extract the key character and mapping
                    // string, and
                    // put them into the appropriate mapping table.
                    int keyChar = Integer.parseInt(matcher.group(1), 16);
                    if (keyChar > 0x10ffff) {
                        throw new ParseException(
                                "Confusables, line " + fLineNum + ": Bad code point: " + matcher.group(1),
                                matcher.start(1));
                    }
                    Matcher m = fParseHexNum.matcher(matcher.group(2));

                    StringBuilder mapString = new StringBuilder();
                    while (m.find()) {
                        int c = Integer.parseInt(m.group(1), 16);
                        if (c > 0x10ffff) {
                            throw new ParseException(
                                    "Confusables, line " + fLineNum + ": Bad code point: " + Integer.toString(c, 16),
                                    matcher.start(2));
                        }
                        mapString.appendCodePoint(c);
                    }
                    assert (mapString.length() >= 1);

                    // Put the map (value) string into the string pool
                    // This a little like a Java intern() - any duplicates will be
                    // eliminated.
                    SPUString smapString = stringPool.addString(mapString.toString());

                    // Add the char . string mapping to the table.
                    // For Unicode 8, the SL, SA and ML tables have been discontinued.
                    // All input data from confusables.txt is tagged MA.
                    fTable.put(keyChar, smapString);

                    fKeySet.add(keyChar);
                }

                // Input data is now all parsed and collected.
                // Now create the run-time binary form of the data.
                //
                // This is done in two steps. First the data is assembled into vectors and strings,
                // for ease of construction, then the contents of these collections are copied
                // into the actual SpoofData object.

                // Build up the string array, and record the index of each string therein
                // in the (build time only) string pool.
                // Strings of length one are not entered into the strings array.
                // (Strings in the table are sorted by length)

                stringPool.sort();
                fStringTable = new StringBuffer();
                int poolSize = stringPool.size();
                int i;
                for (i = 0; i < poolSize; i++) {
                    SPUString s = stringPool.getByIndex(i);
                    int strLen = s.fStr.length();
                    int strIndex = fStringTable.length();
                    if (strLen == 1) {
                        // strings of length one do not get an entry in the string table.
                        // Keep the single string character itself here, which is the same
                        // convention that is used in the final run-time string table index.
                        s.fCharOrStrTableIndex = s.fStr.charAt(0);
                    } else {
                        s.fCharOrStrTableIndex = strIndex;
                        fStringTable.append(s.fStr);
                    }
                }

                // Construct the compile-time Key and Value table.
                //
                // The keys in the Key table follow the format described in uspoof.h for the
                // Cfu confusables data structure.
                //
                // Starting in ICU 58, each code point has exactly one entry in the data
                // structure.

                for (String keyCharStr : fKeySet) {
                    int keyChar = keyCharStr.codePointAt(0);
                    SPUString targetMapping = fTable.get(keyChar);
                    assert targetMapping != null;

                    // Throw a sane exception if trying to consume a long string.  Otherwise,
                    // codePointAndLengthToKey will throw an assertion error.
                    if (targetMapping.fStr.length() > 256) {
                        throw new IllegalArgumentException("Confusable prototypes cannot be longer than 256 entries.");
                    }

                    int key = ConfusableDataUtils.codePointAndLengthToKey(keyChar, targetMapping.fStr.length());
                    int value = targetMapping.fCharOrStrTableIndex;

                    fKeyVec.add(key);
                    fValueVec.add(value);
                }

                // Put the assembled data into the destination SpoofData object.

                // The Key Table
                // While copying the keys to the output array,
                // also sanity check that the keys are sorted.
                int numKeys = fKeyVec.size();
                dest.fCFUKeys = new int[numKeys];
                int previousCodePoint = 0;
                for (i = 0; i < numKeys; i++) {
                    int key = fKeyVec.get(i);
                    int codePoint = ConfusableDataUtils.keyToCodePoint(key);
                    // strictly greater because there can be only one entry per code point
                    assert codePoint > previousCodePoint;
                    dest.fCFUKeys[i] = key;
                    previousCodePoint = codePoint;
                }

                // The Value Table, parallels the key table
                int numValues = fValueVec.size();
                assert (numKeys == numValues);
                dest.fCFUValues = new short[numValues];
                i = 0;
                for (int value : fValueVec) {
                    assert (value < 0xffff);
                    dest.fCFUValues[i++] = (short) value;
                }

                // The Strings Table.
                dest.fCFUStrings = fStringTable.toString();
            }

            public static void buildConfusableData(Reader confusables, SpoofData dest)
                    throws java.io.IOException, ParseException {
                ConfusabledataBuilder builder = new ConfusabledataBuilder();
                builder.build(confusables, dest);
            }

            /*
             * *****************************************************************************
             * Internal classes for compiling confusable data into its binary (runtime) form.
             * *****************************************************************************
             */
            // SPUString
            // Holds a string that is the result of one of the mappings defined
            // by the confusable mapping data (confusables.txt from Unicode.org)
            // Instances of SPUString exist during the compilation process only.

            private static class SPUString {
                String fStr; // The actual string.
                int fCharOrStrTableIndex; // Index into the final runtime data for this string.
                // (or, for length 1, the single string char itself,
                // there being no string table entry for it.)

                SPUString(String s) {
                    fStr = s;
                    fCharOrStrTableIndex = 0;
                }
            }

            // Comparison function for ordering strings in the string pool.
            // Compare by length first, then, within a group of the same length,
            // by code point order.

            private static class SPUStringComparator implements Comparator<SPUString> {
                @Override
                public int compare(SPUString sL, SPUString sR) {
                    int lenL = sL.fStr.length();
                    int lenR = sR.fStr.length();
                    if (lenL < lenR) {
                        return -1;
                    } else if (lenL > lenR) {
                        return 1;
                    } else {
                        return sL.fStr.compareTo(sR.fStr);
                    }
                }

                final static SPUStringComparator INSTANCE = new SPUStringComparator();
            }

            // String Pool A utility class for holding the strings that are the result of
            // the spoof mappings. These strings will utimately end up in the
            // run-time String Table.
            // This is sort of like a sorted set of strings, except that ICU's anemic
            // built-in collections don't support those, so it is implemented with a
            // combination of a uhash and a Vector.
            private static class SPUStringPool {
                public SPUStringPool() {
                    fVec = new Vector<SPUString>();
                    fHash = new Hashtable<String, SPUString>();
                }

                public int size() {
                    return fVec.size();
                }

                // Get the n-th string in the collection.
                public SPUString getByIndex(int index) {
                    SPUString retString = fVec.elementAt(index);
                    return retString;
                }

                // Add a string. Return the string from the table.
                // If the input parameter string is already in the table, delete the
                // input parameter and return the existing string.
                public SPUString addString(String src) {
                    SPUString hashedString = fHash.get(src);
                    if (hashedString == null) {
                        hashedString = new SPUString(src);
                        fHash.put(src, hashedString);
                        fVec.addElement(hashedString);
                    }
                    return hashedString;
                }

                // Sort the contents; affects the ordering of getByIndex().
                public void sort() {
                    Collections.sort(fVec, SPUStringComparator.INSTANCE);
                }

                private Vector<SPUString> fVec; // Elements are SPUString *
                private Hashtable<String, SPUString> fHash; // Key: Value:
            }

        }
    }

    /**
     * Get the Restriction Level that is being tested.
     *
     * @return The restriction level
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public RestrictionLevel getRestrictionLevel() {
        return fRestrictionLevel;
    }

    /**
     * Get the set of checks that this Spoof Checker has been configured to perform.
     *
     * @return The set of checks that this spoof checker will perform.
     */
    public int getChecks() {
        return fChecks;
    }

    /**
     * Get a read-only set of locales for the scripts that are acceptable in strings to be checked. If no limitations on
     * scripts have been specified, an empty set will be returned.
     *
     * setAllowedChars() will reset the list of allowed locales to be empty.
     *
     * The returned set may not be identical to the originally specified set that is supplied to setAllowedLocales();
     * the information other than languages from the originally specified locales may be omitted.
     *
     * @return A set of locales corresponding to the acceptable scripts.
     */
    public Set<ULocale> getAllowedLocales() {
        return Collections.unmodifiableSet(fAllowedLocales);
    }

    /**
     * Get a set of {@link java.util.Locale} instances for the scripts that are acceptable in strings to be checked. If
     * no limitations on scripts have been specified, an empty set will be returned.
     *
     * @return A set of locales corresponding to the acceptable scripts.
     */
    public Set<Locale> getAllowedJavaLocales() {
        HashSet<Locale> locales = new HashSet<Locale>(fAllowedLocales.size());
        for (ULocale uloc : fAllowedLocales) {
            locales.add(uloc.toLocale());
        }
        return locales;
    }

    /**
     * Get a UnicodeSet for the characters permitted in an identifier. This corresponds to the limits imposed by the Set
     * Allowed Characters functions. Limitations imposed by other checks will not be reflected in the set returned by
     * this function.
     *
     * The returned set will be frozen, meaning that it cannot be modified by the caller.
     *
     * @return A UnicodeSet containing the characters that are permitted by the CHAR_LIMIT test.
     */
    public UnicodeSet getAllowedChars() {
        return fAllowedCharsSet;
    }

    /**
     * A struct-like class to hold the results of a Spoof Check operation. Tells which check(s) have failed.
     */
    public static class CheckResult {
        /**
         * Indicates which of the spoof check(s) have failed. The value is a bitwise OR of the constants for the tests
         * in question: RESTRICTION_LEVEL, CHAR_LIMIT, and so on.
         *
         * @see Builder#setChecks
         */
        public int checks;

        /**
         * The index of the first string position that failed a check.
         *
         * @deprecated ICU 51. No longer supported. Always set to zero.
         */
        @Deprecated
        public int position;

        /**
         * The numerics found in the string, if MIXED_NUMBERS was set; otherwise null.  The set will contain the zero
         * digit from each decimal number system found in the input string.
         *
         * @hide draft / provisional / internal are hidden on Android
         */
        public UnicodeSet numerics;

        /**
         * The restriction level that the text meets, if RESTRICTION_LEVEL is set; otherwise null.
         *
         * @hide draft / provisional / internal are hidden on Android
         */
        public RestrictionLevel restrictionLevel;

        /**
         * Default constructor
         */
        public CheckResult() {
            checks = 0;
            position = 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("checks:");
            if (checks == 0) {
                sb.append(" none");
            } else if (checks == ALL_CHECKS) {
                sb.append(" all");
            } else {
                if ((checks & SINGLE_SCRIPT_CONFUSABLE) != 0) {
                    sb.append(" SINGLE_SCRIPT_CONFUSABLE");
                }
                if ((checks & MIXED_SCRIPT_CONFUSABLE) != 0) {
                    sb.append(" MIXED_SCRIPT_CONFUSABLE");
                }
                if ((checks & WHOLE_SCRIPT_CONFUSABLE) != 0) {
                    sb.append(" WHOLE_SCRIPT_CONFUSABLE");
                }
                if ((checks & ANY_CASE) != 0) {
                    sb.append(" ANY_CASE");
                }
                if ((checks & RESTRICTION_LEVEL) != 0) {
                    sb.append(" RESTRICTION_LEVEL");
                }
                if ((checks & INVISIBLE) != 0) {
                    sb.append(" INVISIBLE");
                }
                if ((checks & CHAR_LIMIT) != 0) {
                    sb.append(" CHAR_LIMIT");
                }
                if ((checks & MIXED_NUMBERS) != 0) {
                    sb.append(" MIXED_NUMBERS");
                }
            }
            sb.append(", numerics: ").append(numerics.toPattern(false));
            sb.append(", position: ").append(position);
            sb.append(", restrictionLevel: ").append(restrictionLevel);
            return sb.toString();
        }
    }

    /**
     * Check the specified string for possible security issues. The text to be checked will typically be an identifier
     * of some sort. The set of checks to be performed was specified when building the SpoofChecker.
     *
     * @param text
     *            A String to be checked for possible security issues.
     * @param checkResult
     *            Output parameter, indicates which specific tests failed. May be null if the information is not wanted.
     * @return True there any issue is found with the input string.
     */
    public boolean failsChecks(String text, CheckResult checkResult) {
        int length = text.length();

        int result = 0;
        if (checkResult != null) {
            checkResult.position = 0;
            checkResult.numerics = null;
            checkResult.restrictionLevel = null;
        }

        if (0 != (this.fChecks & RESTRICTION_LEVEL)) {
            RestrictionLevel textRestrictionLevel = getRestrictionLevel(text);
            if (textRestrictionLevel.compareTo(fRestrictionLevel) > 0) {
                result |= RESTRICTION_LEVEL;
            }
            if (checkResult != null) {
                checkResult.restrictionLevel = textRestrictionLevel;
            }
        }

        if (0 != (this.fChecks & MIXED_NUMBERS)) {
            UnicodeSet numerics = new UnicodeSet();
            getNumerics(text, numerics);
            if (numerics.size() > 1) {
                result |= MIXED_NUMBERS;
            }
            if (checkResult != null) {
                checkResult.numerics = numerics;
            }
        }

        if (0 != (this.fChecks & CHAR_LIMIT)) {
            int i;
            int c;
            for (i = 0; i < length;) {
                // U16_NEXT(text, i, length, c);
                c = Character.codePointAt(text, i);
                i = Character.offsetByCodePoints(text, i, 1);
                if (!this.fAllowedCharsSet.contains(c)) {
                    result |= CHAR_LIMIT;
                    break;
                }
            }
        }

        if (0 != (this.fChecks & INVISIBLE)) {
            // This check needs to be done on NFD input
            String nfdText = nfdNormalizer.normalize(text);

            // scan for more than one occurrence of the same non-spacing mark
            // in a sequence of non-spacing marks.
            int i;
            int c;
            int firstNonspacingMark = 0;
            boolean haveMultipleMarks = false;
            UnicodeSet marksSeenSoFar = new UnicodeSet(); // Set of combining marks in a
                                                          // single combining sequence.
            for (i = 0; i < length;) {
                c = Character.codePointAt(nfdText, i);
                i = Character.offsetByCodePoints(nfdText, i, 1);
                if (Character.getType(c) != UCharacterCategory.NON_SPACING_MARK) {
                    firstNonspacingMark = 0;
                    if (haveMultipleMarks) {
                        marksSeenSoFar.clear();
                        haveMultipleMarks = false;
                    }
                    continue;
                }
                if (firstNonspacingMark == 0) {
                    firstNonspacingMark = c;
                    continue;
                }
                if (!haveMultipleMarks) {
                    marksSeenSoFar.add(firstNonspacingMark);
                    haveMultipleMarks = true;
                }
                if (marksSeenSoFar.contains(c)) {
                    // report the error, and stop scanning.
                    // No need to find more than the first failure.
                    result |= INVISIBLE;
                    break;
                }
                marksSeenSoFar.add(c);
            }
        }
        if (checkResult != null) {
            checkResult.checks = result;
        }
        return (0 != result);
    }

    /**
     * Check the specified string for possible security issues. The text to be checked will typically be an identifier
     * of some sort. The set of checks to be performed was specified when building the SpoofChecker.
     *
     * @param text
     *            A String to be checked for possible security issues.
     * @return True there any issue is found with the input string.
     */
    public boolean failsChecks(String text) {
        return failsChecks(text, null);
    }

    /**
     * Check the whether two specified strings are visually confusable. The types of confusability to be tested - single
     * script, mixed script, or whole script - are determined by the check options set for the SpoofChecker.
     *
     * The tests to be performed are controlled by the flags SINGLE_SCRIPT_CONFUSABLE MIXED_SCRIPT_CONFUSABLE
     * WHOLE_SCRIPT_CONFUSABLE At least one of these tests must be selected.
     *
     * ANY_CASE is a modifier for the tests. Select it if the identifiers may be of mixed case. If identifiers are case
     * folded for comparison and display to the user, do not select the ANY_CASE option.
     *
     *
     * @param s1
     *            The first of the two strings to be compared for confusability.
     * @param s2
     *            The second of the two strings to be compared for confusability.
     * @return Non-zero if s1 and s1 are confusable. If not 0, the value will indicate the type(s) of confusability
     *         found, as defined by spoof check test constants.
     */
    public int areConfusable(String s1, String s2) {
        //
        // See section 4 of UAX 39 for the algorithm for checking whether two strings are confusable,
        // and for definitions of the types (single, whole, mixed-script) of confusables.

        // We only care about a few of the check flags. Ignore the others.
        // If no tests relevant to this function have been specified, signal an error.
        // TODO: is this really the right thing to do? It's probably an error on
        // the caller's part, but logically we would just return 0 (no error).
        if ((this.fChecks & CONFUSABLE) == 0) {
            throw new IllegalArgumentException("No confusable checks are enabled.");
        }

        // Compute the skeletons and check for confusability.
        String s1Skeleton = getSkeleton(s1);
        String s2Skeleton = getSkeleton(s2);
        if (!s1Skeleton.equals(s2Skeleton)) {
            return 0;
        }

        // If we get here, the strings are confusable. Now we just need to set the flags for the appropriate classes
        // of confusables according to UTS 39 section 4.
        // Start by computing the resolved script sets of s1 and s2.
        ScriptSet s1RSS = new ScriptSet();
        getResolvedScriptSet(s1, s1RSS);
        ScriptSet s2RSS = new ScriptSet();
        getResolvedScriptSet(s2, s2RSS);

        // Turn on all applicable flags
        int result = 0;
        if (s1RSS.intersects(s2RSS)) {
            result |= SINGLE_SCRIPT_CONFUSABLE;
        } else {
            result |= MIXED_SCRIPT_CONFUSABLE;
            if (!s1RSS.isEmpty() && !s2RSS.isEmpty()) {
                result |= WHOLE_SCRIPT_CONFUSABLE;
            }
        }

        // Turn off flags that the user doesn't want
        result &= fChecks;

        return result;
    }

    /**
     * Get the "skeleton" for an identifier string. Skeletons are a transformation of the input string; Two strings are
     * confusable if their skeletons are identical. See Unicode UAX 39 for additional information.
     *
     * Using skeletons directly makes it possible to quickly check whether an identifier is confusable with any of some
     * large set of existing identifiers, by creating an efficiently searchable collection of the skeletons.
     *
     * Skeletons are computed using the algorithm and data described in Unicode UAX 39.
     *
     * @param str
     *            The input string whose skeleton will be generated.
     * @return The output skeleton string.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getSkeleton(CharSequence str) {
        // Apply the skeleton mapping to the NFD normalized input string
        // Accumulate the skeleton, possibly unnormalized, in a String.
        String nfdId = nfdNormalizer.normalize(str);
        int normalizedLen = nfdId.length();
        StringBuilder skelSB = new StringBuilder();
        for (int inputIndex = 0; inputIndex < normalizedLen;) {
            int c = Character.codePointAt(nfdId, inputIndex);
            inputIndex += Character.charCount(c);
            this.fSpoofData.confusableLookup(c, skelSB);
        }
        String skelStr = skelSB.toString();
        skelStr = nfdNormalizer.normalize(skelStr);
        return skelStr;
    }

    /**
     * Calls {@link SpoofChecker#getSkeleton(CharSequence id)}. Starting with ICU 55, the "type" parameter has been
     * ignored, and starting with ICU 58, this function has been deprecated.
     *
     * @param type
     *            No longer supported. Prior to ICU 55, was used to specify the mapping table SL, SA, ML, or MA.
     * @param id
     *            The input identifier whose skeleton will be generated.
     * @return The output skeleton string.
     *
     * @deprecated ICU 58
     */
    @Deprecated
    public String getSkeleton(int type, String id) {
        return getSkeleton(id);
    }

    /**
     * Equality function. Return true if the two SpoofChecker objects incorporate the same confusable data and have
     * enabled the same set of checks.
     *
     * @param other
     *            the SpoofChecker being compared with.
     * @return true if the two SpoofCheckers are equal.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SpoofChecker)) {
            return false;
        }
        SpoofChecker otherSC = (SpoofChecker) other;
        if (fSpoofData != otherSC.fSpoofData && fSpoofData != null && !fSpoofData.equals(otherSC.fSpoofData)) {
            return false;
        }
        if (fChecks != otherSC.fChecks) {
            return false;
        }
        if (fAllowedLocales != otherSC.fAllowedLocales && fAllowedLocales != null
                && !fAllowedLocales.equals(otherSC.fAllowedLocales)) {
            return false;
        }
        if (fAllowedCharsSet != otherSC.fAllowedCharsSet && fAllowedCharsSet != null
                && !fAllowedCharsSet.equals(otherSC.fAllowedCharsSet)) {
            return false;
        }
        if (fRestrictionLevel != otherSC.fRestrictionLevel) {
            return false;
        }
        return true;
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    public int hashCode() {
        return fChecks
                ^ fSpoofData.hashCode()
                ^ fAllowedLocales.hashCode()
                ^ fAllowedCharsSet.hashCode()
                ^ fRestrictionLevel.ordinal();
    }

    /**
     * Computes the augmented script set for a code point, according to UTS 39 section 5.1.
     */
    private static void getAugmentedScriptSet(int codePoint, ScriptSet result) {
        result.clear();
        UScript.getScriptExtensions(codePoint, result);

        // Section 5.1 step 1
        if (result.get(UScript.HAN)) {
            result.set(UScript.HAN_WITH_BOPOMOFO);
            result.set(UScript.JAPANESE);
            result.set(UScript.KOREAN);
        }
        if (result.get(UScript.HIRAGANA)) {
            result.set(UScript.JAPANESE);
        }
        if (result.get(UScript.KATAKANA)) {
            result.set(UScript.JAPANESE);
        }
        if (result.get(UScript.HANGUL)) {
            result.set(UScript.KOREAN);
        }
        if (result.get(UScript.BOPOMOFO)) {
            result.set(UScript.HAN_WITH_BOPOMOFO);
        }

        // Section 5.1 step 2
        if (result.get(UScript.COMMON) || result.get(UScript.INHERITED)) {
            result.setAll();
        }
    }

    /**
     * Computes the resolved script set for a string, according to UTS 39 section 5.1.
     */
    private void getResolvedScriptSet(CharSequence input, ScriptSet result) {
        getResolvedScriptSetWithout(input, UScript.CODE_LIMIT, result);
    }

    /**
     * Computes the resolved script set for a string, omitting characters having the specified script. If
     * UScript.CODE_LIMIT is passed as the second argument, all characters are included.
     */
    private void getResolvedScriptSetWithout(CharSequence input, int script, ScriptSet result) {
        result.setAll();

        ScriptSet temp = new ScriptSet();
        for (int utf16Offset = 0; utf16Offset < input.length();) {
            int codePoint = Character.codePointAt(input, utf16Offset);
            utf16Offset += Character.charCount(codePoint);

            // Compute the augmented script set for the character
            getAugmentedScriptSet(codePoint, temp);

            // Intersect the augmented script set with the resolved script set, but only if the character doesn't
            // have the script specified in the function call
            if (script == UScript.CODE_LIMIT || !temp.get(script)) {
                result.and(temp);
            }
        }
    }

    /**
     * Computes the set of numerics for a string, according to UTS 39 section 5.3.
     */
    private void getNumerics(String input, UnicodeSet result) {
        result.clear();

        for (int utf16Offset = 0; utf16Offset < input.length();) {
            int codePoint = Character.codePointAt(input, utf16Offset);
            utf16Offset += Character.charCount(codePoint);

            // Store a representative character for each kind of decimal digit
            if (UCharacter.getType(codePoint) == UCharacterCategory.DECIMAL_DIGIT_NUMBER) {
                // Store the zero character as a representative for comparison.
                // Unicode guarantees it is codePoint - value
                result.add(codePoint - UCharacter.getNumericValue(codePoint));
            }
        }
    }

    /**
     * Computes the restriction level of a string, according to UTS 39 section 5.2.
     */
    private RestrictionLevel getRestrictionLevel(String input) {
        // Section 5.2 step 1:
        if (!fAllowedCharsSet.containsAll(input)) {
            return RestrictionLevel.UNRESTRICTIVE;
        }

        // Section 5.2 step 2:
        if (ASCII.containsAll(input)) {
            return RestrictionLevel.ASCII;
        }

        // Section 5.2 steps 3:
        ScriptSet resolvedScriptSet = new ScriptSet();
        getResolvedScriptSet(input, resolvedScriptSet);

        // Section 5.2 step 4:
        if (!resolvedScriptSet.isEmpty()) {
            return RestrictionLevel.SINGLE_SCRIPT_RESTRICTIVE;
        }

        // Section 5.2 step 5:
        ScriptSet resolvedNoLatn = new ScriptSet();
        getResolvedScriptSetWithout(input, UScript.LATIN, resolvedNoLatn);

        // Section 5.2 step 6:
        if (resolvedNoLatn.get(UScript.HAN_WITH_BOPOMOFO) || resolvedNoLatn.get(UScript.JAPANESE)
                || resolvedNoLatn.get(UScript.KOREAN)) {
            return RestrictionLevel.HIGHLY_RESTRICTIVE;
        }

        // Section 5.2 step 7:
        if (!resolvedNoLatn.isEmpty() && !resolvedNoLatn.get(UScript.CYRILLIC) && !resolvedNoLatn.get(UScript.GREEK)
                && !resolvedNoLatn.get(UScript.CHEROKEE)) {
            return RestrictionLevel.MODERATELY_RESTRICTIVE;
        }

        // Section 5.2 step 8:
        return RestrictionLevel.MINIMALLY_RESTRICTIVE;
    }

    // Data Members
    private int fChecks; // Bit vector of checks to perform.
    private SpoofData fSpoofData;
    private Set<ULocale> fAllowedLocales; // The Set of allowed locales.
    private UnicodeSet fAllowedCharsSet; // The UnicodeSet of allowed characters.
    private RestrictionLevel fRestrictionLevel;

    private static Normalizer2 nfdNormalizer = Normalizer2.getNFDInstance();

    // Confusable Mappings Data Structures, version 2.0
    //
    // This description and the corresponding implementation are to be kept
    // in-sync with the copy in icu4c uspoof_impl.h.
    //
    // For the confusable data, we are essentially implementing a map,
    //     key: a code point
    //     value: a string. Most commonly one char in length, but can be more.
    //
    // The keys are stored as a sorted array of 32 bit ints.
    //         bits 0-23 a code point value
    //         bits 24-31 length of value string, in UChars (between 1 and 256 UChars).
    //     The key table is sorted in ascending code point order. (not on the
    //     32 bit int value, the flag bits do not participate in the sorting.)
    //
    //     Lookup is done by means of a binary search in the key table.
    //
    // The corresponding values are kept in a parallel array of 16 bit ints.
    //     If the value string is of length 1, it is literally in the value array.
    //     For longer strings, the value array contains an index into the strings
    //     table.
    //
    // String Table:
    //     The strings table contains all of the value strings (those of length two or greater)
    //     concatentated together into one long char (UTF-16) array.
    //
    //     There is no nul character or other mark between adjacent strings.
    //
    //----------------------------------------------------------------------------
    //
    //  Changes from format version 1 to format version 2:
    //        1) Removal of the whole-script confusable data tables.
    //        2) Removal of the SL/SA/ML/MA and multi-table flags in the key bitmask.
    //        3) Expansion of string length value in the key bitmask from 2 bits to 8 bits.
    //        4) Removal of the string lengths table since 8 bits is sufficient for the
    //           lengths of all entries in confusables.txt.
    //
    private static final class ConfusableDataUtils {
        public static final int FORMAT_VERSION = 2; // version for ICU 58

        public static final int keyToCodePoint(int key) {
            return key & 0x00ffffff;
        }

        public static final int keyToLength(int key) {
            return ((key & 0xff000000) >> 24) + 1;
        }

        public static final int codePointAndLengthToKey(int codePoint, int length) {
            assert (codePoint & 0x00ffffff) == codePoint;
            assert length <= 256;
            return codePoint | ((length - 1) << 24);
        }
    }

    // -------------------------------------------------------------------------------------
    //
    // SpoofData
    //
    // This class corresponds to the ICU SpoofCheck data.
    //
    // The data can originate with the Binary ICU data that is generated in ICU4C,
    // or it can originate from source rules that are compiled in ICU4J.
    //
    // This class does not include the set of checks to be performed, but only
    // data that is serialized into the ICU binary data.
    //
    // Because Java cannot easily wrap binary data like ICU4C, the binary data is
    // copied into Java structures that are convenient for use by the run time code.
    //
    // ---------------------------------------------------------------------------------------
    private static class SpoofData {

        // The Confusable data, Java data structures for.
        int[] fCFUKeys;
        short[] fCFUValues;
        String fCFUStrings;

        private static final int DATA_FORMAT = 0x43667520; // "Cfu "

        private static final class IsAcceptable implements Authenticate {
            @Override
            public boolean isDataVersionAcceptable(byte version[]) {
                return version[0] == ConfusableDataUtils.FORMAT_VERSION || version[1] != 0 || version[2] != 0
                        || version[3] != 0;
            }
        }

        private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();

        private static final class DefaultData {
            private static SpoofData INSTANCE = null;
            private static IOException EXCEPTION = null;

            static {
                // Note: Although this is static, the Java runtime can delay execution of this block until
                // the data is actually requested via SpoofData.getDefault().
                try {
                    INSTANCE = new SpoofData(ICUBinary.getRequiredData("confusables.cfu"));
                } catch (IOException e) {
                    EXCEPTION = e;
                }
            }
        }

        /**
         * @return instance for Unicode standard data
         */
        public static SpoofData getDefault() {
            if (DefaultData.EXCEPTION != null) {
                throw new MissingResourceException(
                        "Could not load default confusables data: " + DefaultData.EXCEPTION.getMessage(),
                        "SpoofChecker", "");
            }
            return DefaultData.INSTANCE;
        }

        // SpoofChecker Data constructor for use from data builder.
        // Initializes a new, empty data area that will be populated later.
        private SpoofData() {
        }

        // Constructor for use when creating from prebuilt default data.
        // A ByteBuffer is what the ICU internal data loading functions provide.
        private SpoofData(ByteBuffer bytes) throws java.io.IOException {
            ICUBinary.readHeader(bytes, DATA_FORMAT, IS_ACCEPTABLE);
            bytes.mark();
            readData(bytes);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SpoofData)) {
                return false;
            }
            SpoofData otherData = (SpoofData) other;
            if (!Arrays.equals(fCFUKeys, otherData.fCFUKeys))
                return false;
            if (!Arrays.equals(fCFUValues, otherData.fCFUValues))
                return false;
            if (!Utility.sameObjects(fCFUStrings, otherData.fCFUStrings) && fCFUStrings != null
                    && !fCFUStrings.equals(otherData.fCFUStrings))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(fCFUKeys)
                    ^ Arrays.hashCode(fCFUValues)
                    ^ fCFUStrings.hashCode();
        }

        // Set the SpoofChecker data from pre-built binary data in a byte buffer.
        // The binary data format is as described for ICU4C spoof data.
        //
        private void readData(ByteBuffer bytes) throws java.io.IOException {
            int magic = bytes.getInt();
            if (magic != 0x3845fdef) {
                throw new IllegalArgumentException("Bad Spoof Check Data.");
            }
            @SuppressWarnings("unused")
            int dataFormatVersion = bytes.getInt();
            @SuppressWarnings("unused")
            int dataLength = bytes.getInt();

            int CFUKeysOffset = bytes.getInt();
            int CFUKeysSize = bytes.getInt();

            int CFUValuesOffset = bytes.getInt();
            int CFUValuesSize = bytes.getInt();

            int CFUStringTableOffset = bytes.getInt();
            int CFUStringTableSize = bytes.getInt();

            // We have now read the file header, and obtained the position for each
            // of the data items. Now read each in turn, first seeking the
            // input stream to the position of the data item.

            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUKeysOffset);
            fCFUKeys = ICUBinary.getInts(bytes, CFUKeysSize, 0);

            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUValuesOffset);
            fCFUValues = ICUBinary.getShorts(bytes, CFUValuesSize, 0);

            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUStringTableOffset);
            fCFUStrings = ICUBinary.getString(bytes, CFUStringTableSize, 0);
        }

        /**
         * Append the confusable skeleton transform for a single code point to a StringBuilder. The string to be
         * appended will between 1 and 18 characters as of Unicode 9.
         *
         * This is the heart of the confusable skeleton generation implementation.
         */
        public void confusableLookup(int inChar, StringBuilder dest) {
            // Perform a binary search.
            // [lo, hi), i.e lo is inclusive, hi is exclusive.
            // The result after the loop will be in lo.
            int lo = 0;
            int hi = length();
            do {
                int mid = (lo + hi) / 2;
                if (codePointAt(mid) > inChar) {
                    hi = mid;
                } else if (codePointAt(mid) < inChar) {
                    lo = mid;
                } else {
                    // Found result. Break early.
                    lo = mid;
                    break;
                }
            } while (hi - lo > 1);

            // Did we find an entry? If not, the char maps to itself.
            if (codePointAt(lo) != inChar) {
                dest.appendCodePoint(inChar);
                return;
            }

            // Add the element to the string builder and return.
            appendValueTo(lo, dest);
            return;
        }

        /**
         * Return the number of confusable entries in this SpoofData.
         *
         * @return The number of entries.
         */
        public int length() {
            return fCFUKeys.length;
        }

        /**
         * Return the code point (key) at the specified index.
         *
         * @param index
         *            The index within the SpoofData.
         * @return The code point.
         */
        public int codePointAt(int index) {
            return ConfusableDataUtils.keyToCodePoint(fCFUKeys[index]);
        }

        /**
         * Append the confusable skeleton at the specified index to the StringBuilder dest.
         *
         * @param index
         *            The index within the SpoofData.
         * @param dest
         *            The StringBuilder to which to append the skeleton.
         */
        public void appendValueTo(int index, StringBuilder dest) {
            int stringLength = ConfusableDataUtils.keyToLength(fCFUKeys[index]);

            // Value is either a char (for strings of length 1) or
            // an index into the string table (for longer strings)
            short value = fCFUValues[index];
            if (stringLength == 1) {
                dest.append((char) value);
            } else {
                dest.append(fCFUStrings, value, value + stringLength);
            }
        }
    }

    // -------------------------------------------------------------------------------
    //
    // ScriptSet - Script code bit sets.
    // Extends Java BitSet with input/output support and a few helper methods.
    // Note: The I/O is not currently being used, so it has been commented out. If
    // it is needed again, the code can be restored.
    //
    // -------------------------------------------------------------------------------
    static class ScriptSet extends BitSet {

        // Eclipse default value to quell warnings:
        private static final long serialVersionUID = 1L;

        // // The serialized version of this class can hold INT_CAPACITY * 32 scripts.
        // private static final int INT_CAPACITY = 6;
        // private static final long serialVersionUID = INT_CAPACITY;
        // static {
        // assert ScriptSet.INT_CAPACITY * Integer.SIZE <= UScript.CODE_LIMIT;
        // }
        //
        // public ScriptSet() {
        // }
        //
        // public ScriptSet(ByteBuffer bytes) throws java.io.IOException {
        // for (int i = 0; i < INT_CAPACITY; i++) {
        // int bits = bytes.getInt();
        // for (int j = 0; j < Integer.SIZE; j++) {
        // if ((bits & (1 << j)) != 0) {
        // set(i * Integer.SIZE + j);
        // }
        // }
        // }
        // }
        //
        // public void output(DataOutputStream os) throws java.io.IOException {
        // for (int i = 0; i < INT_CAPACITY; i++) {
        // int bits = 0;
        // for (int j = 0; j < Integer.SIZE; j++) {
        // if (get(i * Integer.SIZE + j)) {
        // bits |= (1 << j);
        // }
        // }
        // os.writeInt(bits);
        // }
        // }

        public void and(int script) {
            this.clear(0, script);
            this.clear(script + 1, UScript.CODE_LIMIT);
        }

        public void setAll() {
            this.set(0, UScript.CODE_LIMIT);
        }

        public boolean isFull() {
            return cardinality() == UScript.CODE_LIMIT;
        }

        public void appendStringTo(StringBuilder sb) {
            sb.append("{ ");
            if (isEmpty()) {
                sb.append("- ");
            } else if (isFull()) {
                sb.append("* ");
            } else {
                for (int script = 0; script < UScript.CODE_LIMIT; script++) {
                    if (get(script)) {
                        sb.append(UScript.getShortName(script));
                        sb.append(" ");
                    }
                }
            }
            sb.append("}");
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<ScriptSet ");
            appendStringTo(sb);
            sb.append(">");
            return sb.toString();
        }
    }
}
