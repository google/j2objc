/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
*******************************************************************************
* Copyright (C) 1996-2014, International Business Machines Corporation and
* others. All Rights Reserved.
*******************************************************************************
*/


package android.icu.dev.test.lang;


import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.text.BreakIterator;
import android.icu.text.CaseMap;
import android.icu.text.Edits;
import android.icu.text.RuleBasedBreakIterator;
import android.icu.text.UTF16;
import android.icu.util.ULocale;


/**
* <p>Testing character casing</p>
* <p>Mostly following the test cases in strcase.cpp for ICU</p>
* @author Syn Wee Quek
* @since march 14 2002
*/
public final class UCharacterCaseTest extends TestFmwk
{
    // constructor -----------------------------------------------------------

    /**
     * Constructor
     */
    public UCharacterCaseTest()
    {
    }

    // public methods --------------------------------------------------------

    /**
     * Testing the uppercase and lowercase function of UCharacter
     */
    @Test
    public void TestCharacter()
    {
        for (int i = 0; i < CHARACTER_LOWER_.length; i ++) {
            if (UCharacter.isLetter(CHARACTER_LOWER_[i]) &&
                !UCharacter.isLowerCase(CHARACTER_LOWER_[i])) {
                errln("FAIL isLowerCase test for \\u" +
                      hex(CHARACTER_LOWER_[i]));
                break;
            }
            if (UCharacter.isLetter(CHARACTER_UPPER_[i]) &&
                !(UCharacter.isUpperCase(CHARACTER_UPPER_[i]) ||
                  UCharacter.isTitleCase(CHARACTER_UPPER_[i]))) {
                errln("FAIL isUpperCase test for \\u" +
                      hex(CHARACTER_UPPER_[i]));
                break;
            }
            if (CHARACTER_LOWER_[i] !=
                UCharacter.toLowerCase(CHARACTER_UPPER_[i]) ||
                (CHARACTER_UPPER_[i] !=
                UCharacter.toUpperCase(CHARACTER_LOWER_[i]) &&
                CHARACTER_UPPER_[i] !=
                UCharacter.toTitleCase(CHARACTER_LOWER_[i]))) {
                errln("FAIL case conversion test for \\u" +
                      hex(CHARACTER_UPPER_[i]) +
                      " to \\u" + hex(CHARACTER_LOWER_[i]));
                break;
            }
            if (CHARACTER_LOWER_[i] !=
                UCharacter.toLowerCase(CHARACTER_LOWER_[i])) {
                errln("FAIL lower case conversion test for \\u" +
                      hex(CHARACTER_LOWER_[i]));
                break;
            }
            if (CHARACTER_UPPER_[i] !=
                UCharacter.toUpperCase(CHARACTER_UPPER_[i]) &&
                CHARACTER_UPPER_[i] !=
                UCharacter.toTitleCase(CHARACTER_UPPER_[i])) {
                errln("FAIL upper case conversion test for \\u" +
                      hex(CHARACTER_UPPER_[i]));
                break;
            }
            logln("Ok    \\u" + hex(CHARACTER_UPPER_[i]) + " and \\u" +
                  hex(CHARACTER_LOWER_[i]));
        }
    }

    @Test
    public void TestFolding()
    {
        // test simple case folding
        for (int i = 0; i < FOLDING_SIMPLE_.length; i += 3) {
            if (UCharacter.foldCase(FOLDING_SIMPLE_[i], true) !=
                FOLDING_SIMPLE_[i + 1]) {
                errln("FAIL: foldCase(\\u" + hex(FOLDING_SIMPLE_[i]) +
                      ", true) should be \\u" + hex(FOLDING_SIMPLE_[i + 1]));
            }
            if (UCharacter.foldCase(FOLDING_SIMPLE_[i],
                                    UCharacter.FOLD_CASE_DEFAULT) !=
                                                      FOLDING_SIMPLE_[i + 1]) {
                errln("FAIL: foldCase(\\u" + hex(FOLDING_SIMPLE_[i]) +
                      ", UCharacter.FOLD_CASE_DEFAULT) should be \\u"
                      + hex(FOLDING_SIMPLE_[i + 1]));
            }
            if (UCharacter.foldCase(FOLDING_SIMPLE_[i], false) !=
                FOLDING_SIMPLE_[i + 2]) {
                errln("FAIL: foldCase(\\u" + hex(FOLDING_SIMPLE_[i]) +
                      ", false) should be \\u" + hex(FOLDING_SIMPLE_[i + 2]));
            }
            if (UCharacter.foldCase(FOLDING_SIMPLE_[i],
                                    UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I) !=
                                    FOLDING_SIMPLE_[i + 2]) {
                errln("FAIL: foldCase(\\u" + hex(FOLDING_SIMPLE_[i]) +
                      ", UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I) should be \\u"
                      + hex(FOLDING_SIMPLE_[i + 2]));
            }
        }

        // Test full string case folding with default option and separate
        // buffers
        if (!FOLDING_DEFAULT_[0].equals(UCharacter.foldCase(FOLDING_MIXED_[0], true))) {
            errln("FAIL: foldCase(" + prettify(FOLDING_MIXED_[0]) +
                  ", true)=" + prettify(UCharacter.foldCase(FOLDING_MIXED_[0], true)) +
                  " should be " + prettify(FOLDING_DEFAULT_[0]));
        }

        if (!FOLDING_DEFAULT_[0].equals(UCharacter.foldCase(FOLDING_MIXED_[0], UCharacter.FOLD_CASE_DEFAULT))) {
                    errln("FAIL: foldCase(" + prettify(FOLDING_MIXED_[0]) +
                          ", UCharacter.FOLD_CASE_DEFAULT)=" + prettify(UCharacter.foldCase(FOLDING_MIXED_[0], UCharacter.FOLD_CASE_DEFAULT))
                          + " should be " + prettify(FOLDING_DEFAULT_[0]));
                }

        if (!FOLDING_EXCLUDE_SPECIAL_I_[0].equals(
                            UCharacter.foldCase(FOLDING_MIXED_[0], false))) {
            errln("FAIL: foldCase(" + prettify(FOLDING_MIXED_[0]) +
                  ", false)=" + prettify(UCharacter.foldCase(FOLDING_MIXED_[0], false))
                  + " should be " + prettify(FOLDING_EXCLUDE_SPECIAL_I_[0]));
        }

        if (!FOLDING_EXCLUDE_SPECIAL_I_[0].equals(
                                    UCharacter.foldCase(FOLDING_MIXED_[0], UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I))) {
            errln("FAIL: foldCase(" + prettify(FOLDING_MIXED_[0]) +
                  ", UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I)=" + prettify(UCharacter.foldCase(FOLDING_MIXED_[0], UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I))
                  + " should be " + prettify(FOLDING_EXCLUDE_SPECIAL_I_[0]));
        }

        if (!FOLDING_DEFAULT_[1].equals(UCharacter.foldCase(FOLDING_MIXED_[1], true))) {
           errln("FAIL: foldCase(" + prettify(FOLDING_MIXED_[1]) +
                 ", true)=" + prettify(UCharacter.foldCase(FOLDING_MIXED_[1], true))
                 + " should be " + prettify(FOLDING_DEFAULT_[1]));
        }

        if (!FOLDING_DEFAULT_[1].equals(UCharacter.foldCase(FOLDING_MIXED_[1], UCharacter.FOLD_CASE_DEFAULT))) {
            errln("FAIL: foldCase(" + prettify(FOLDING_MIXED_[1]) +
                         ", UCharacter.FOLD_CASE_DEFAULT)=" + prettify(UCharacter.foldCase(FOLDING_MIXED_[1], UCharacter.FOLD_CASE_DEFAULT))
                         + " should be " + prettify(FOLDING_DEFAULT_[1]));
        }

        // alternate handling for dotted I/dotless i (U+0130, U+0131)
        if (!FOLDING_EXCLUDE_SPECIAL_I_[1].equals(
                        UCharacter.foldCase(FOLDING_MIXED_[1], false))) {
            errln("FAIL: foldCase(" + prettify(FOLDING_MIXED_[1]) +
                  ", false)=" + prettify(UCharacter.foldCase(FOLDING_MIXED_[1], false))
                  + " should be " + prettify(FOLDING_EXCLUDE_SPECIAL_I_[1]));
        }

        if (!FOLDING_EXCLUDE_SPECIAL_I_[1].equals(
                                UCharacter.foldCase(FOLDING_MIXED_[1], UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I))) {
            errln("FAIL: foldCase(" + prettify(FOLDING_MIXED_[1]) +
                  ", UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I)=" + prettify(UCharacter.foldCase(FOLDING_MIXED_[1], UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I))
                  + " should be "
                  + prettify(FOLDING_EXCLUDE_SPECIAL_I_[1]));
        }
    }

    /**
     * Testing the strings case mapping methods
     */
    @Test
    public void TestUpper()
    {
        // uppercase with root locale and in the same buffer
        if (!UPPER_ROOT_.equals(UCharacter.toUpperCase(UPPER_BEFORE_))) {
            errln("Fail " + UPPER_BEFORE_ + " after uppercase should be " +
                  UPPER_ROOT_ + " instead got " +
                  UCharacter.toUpperCase(UPPER_BEFORE_));
        }

        // uppercase with turkish locale and separate buffers
        if (!UPPER_TURKISH_.equals(UCharacter.toUpperCase(TURKISH_LOCALE_,
                                                         UPPER_BEFORE_))) {
            errln("Fail " + UPPER_BEFORE_ +
                  " after turkish-sensitive uppercase should be " +
                  UPPER_TURKISH_ + " instead of " +
                  UCharacter.toUpperCase(TURKISH_LOCALE_, UPPER_BEFORE_));
        }

        // uppercase a short string with root locale
        if (!UPPER_MINI_UPPER_.equals(UCharacter.toUpperCase(UPPER_MINI_))) {
            errln("error in toUpper(root locale)=\"" + UPPER_MINI_ +
                  "\" expected \"" + UPPER_MINI_UPPER_ + "\"");
        }

        if (!SHARED_UPPERCASE_TOPKAP_.equals(
                       UCharacter.toUpperCase(SHARED_LOWERCASE_TOPKAP_))) {
            errln("toUpper failed: expected \"" +
                  SHARED_UPPERCASE_TOPKAP_ + "\", got \"" +
                  UCharacter.toUpperCase(SHARED_LOWERCASE_TOPKAP_) + "\".");
        }

        if (!SHARED_UPPERCASE_TURKISH_.equals(
                  UCharacter.toUpperCase(TURKISH_LOCALE_,
                                         SHARED_LOWERCASE_TOPKAP_))) {
            errln("toUpper failed: expected \"" +
                  SHARED_UPPERCASE_TURKISH_ + "\", got \"" +
                  UCharacter.toUpperCase(TURKISH_LOCALE_,
                                     SHARED_LOWERCASE_TOPKAP_) + "\".");
        }

        if (!SHARED_UPPERCASE_GERMAN_.equals(
                UCharacter.toUpperCase(GERMAN_LOCALE_,
                                       SHARED_LOWERCASE_GERMAN_))) {
            errln("toUpper failed: expected \"" + SHARED_UPPERCASE_GERMAN_
                  + "\", got \"" + UCharacter.toUpperCase(GERMAN_LOCALE_,
                                        SHARED_LOWERCASE_GERMAN_) + "\".");
        }

        if (!SHARED_UPPERCASE_GREEK_.equals(
                UCharacter.toUpperCase(SHARED_LOWERCASE_GREEK_))) {
            errln("toLower failed: expected \"" + SHARED_UPPERCASE_GREEK_ +
                  "\", got \"" + UCharacter.toUpperCase(
                                        SHARED_LOWERCASE_GREEK_) + "\".");
        }
    }

    @Test
    public void TestLower()
    {
        if (!LOWER_ROOT_.equals(UCharacter.toLowerCase(LOWER_BEFORE_))) {
            errln("Fail " + LOWER_BEFORE_ + " after lowercase should be " +
                  LOWER_ROOT_ + " instead of " +
                  UCharacter.toLowerCase(LOWER_BEFORE_));
        }

        // lowercase with turkish locale
        if (!LOWER_TURKISH_.equals(UCharacter.toLowerCase(TURKISH_LOCALE_,
                                                          LOWER_BEFORE_))) {
            errln("Fail " + LOWER_BEFORE_ +
                  " after turkish-sensitive lowercase should be " +
                  LOWER_TURKISH_ + " instead of " +
                  UCharacter.toLowerCase(TURKISH_LOCALE_, LOWER_BEFORE_));
        }
        if (!SHARED_LOWERCASE_ISTANBUL_.equals(
                     UCharacter.toLowerCase(SHARED_UPPERCASE_ISTANBUL_))) {
            errln("1. toLower failed: expected \"" +
                  SHARED_LOWERCASE_ISTANBUL_ + "\", got \"" +
              UCharacter.toLowerCase(SHARED_UPPERCASE_ISTANBUL_) + "\".");
        }

        if (!SHARED_LOWERCASE_TURKISH_.equals(
                UCharacter.toLowerCase(TURKISH_LOCALE_,
                                       SHARED_UPPERCASE_ISTANBUL_))) {
            errln("2. toLower failed: expected \"" +
                  SHARED_LOWERCASE_TURKISH_ + "\", got \"" +
                  UCharacter.toLowerCase(TURKISH_LOCALE_,
                                SHARED_UPPERCASE_ISTANBUL_) + "\".");
        }
        if (!SHARED_LOWERCASE_GREEK_.equals(
                UCharacter.toLowerCase(GREEK_LOCALE_,
                                       SHARED_UPPERCASE_GREEK_))) {
            errln("toLower failed: expected \"" + SHARED_LOWERCASE_GREEK_ +
                  "\", got \"" + UCharacter.toLowerCase(GREEK_LOCALE_,
                                        SHARED_UPPERCASE_GREEK_) + "\".");
        }
    }

    @Test
    public void TestTitleRegression() throws java.io.IOException {
        boolean isIgnorable = UCharacter.hasBinaryProperty('\'', UProperty.CASE_IGNORABLE);
        assertTrue("Case Ignorable check of ASCII apostrophe", isIgnorable);
        assertEquals("Titlecase check",
                "The Quick Brown Fox Can't Jump Over The Lazy Dogs.",
                UCharacter.toTitleCase(ULocale.ENGLISH, "THE QUICK BROWN FOX CAN'T JUMP OVER THE LAZY DOGS.", null));
    }

    @Test
    public void TestTitle()
    {
         try{
            for (int i = 0; i < TITLE_DATA_.length;) {
                String test = TITLE_DATA_[i++];
                String expected = TITLE_DATA_[i++];
                ULocale locale = new ULocale(TITLE_DATA_[i++]);
                int breakType = Integer.parseInt(TITLE_DATA_[i++]);
                String optionsString = TITLE_DATA_[i++];
                BreakIterator iter =
                    breakType >= 0 ?
                        BreakIterator.getBreakInstance(locale, breakType) :
                        breakType == -2 ?
                            // Open a trivial break iterator that only delivers { 0, length }
                            // or even just { 0 } as boundaries.
                            new RuleBasedBreakIterator(".*;") :
                            null;
                int options = 0;
                if (optionsString.indexOf('L') >= 0) {
                    options |= UCharacter.TITLECASE_NO_LOWERCASE;
                }
                if (optionsString.indexOf('A') >= 0) {
                    options |= UCharacter.TITLECASE_NO_BREAK_ADJUSTMENT;
                }
                String result = UCharacter.toTitleCase(locale, test, iter, options);
                if (!expected.equals(result)) {
                    errln("titlecasing for " + prettify(test) + " (options " + options + ") should be " +
                          prettify(expected) + " but got " +
                          prettify(result));
                }
                if (options == 0) {
                    result = UCharacter.toTitleCase(locale, test, iter);
                    if (!expected.equals(result)) {
                        errln("titlecasing for " + prettify(test) + " should be " +
                              prettify(expected) + " but got " +
                              prettify(result));
                    }
                }
            }
         }catch(Exception ex){
            warnln("Could not find data for BreakIterators");
         }
    }

    @Test
    public void TestDutchTitle() {
        ULocale LOC_DUTCH = new ULocale("nl");
        int options = 0;
        options |= UCharacter.TITLECASE_NO_LOWERCASE;
        BreakIterator iter = BreakIterator.getWordInstance(LOC_DUTCH);

        assertEquals("Dutch titlecase check in English",
                "Ijssel Igloo Ijmuiden",
                UCharacter.toTitleCase(ULocale.ENGLISH, "ijssel igloo IJMUIDEN", null));

        assertEquals("Dutch titlecase check in Dutch",
                "IJssel Igloo IJmuiden",
                UCharacter.toTitleCase(LOC_DUTCH, "ijssel igloo IJMUIDEN", null));

        // Also check the behavior using Java Locale
        Locale JAVALOC_DUTCH = new Locale("nl");
        assertEquals("Dutch titlecase check in English (Java Locale)",
                "Ijssel Igloo Ijmuiden",
                UCharacter.toTitleCase(Locale.ENGLISH, "ijssel igloo IJMUIDEN", null));

        assertEquals("Dutch titlecase check in Dutch (Java Locale)",
                "IJssel Igloo IJmuiden",
                UCharacter.toTitleCase(JAVALOC_DUTCH, "ijssel igloo IJMUIDEN", null));

        iter.setText("ijssel igloo IjMUIdEN iPoD ijenough");
        assertEquals("Dutch titlecase check in Dutch with nolowercase option",
                "IJssel Igloo IJMUIdEN IPoD IJenough",
                UCharacter.toTitleCase(LOC_DUTCH, "ijssel igloo IjMUIdEN iPoD ijenough", iter, options));
    }

    @Test
    public void TestSpecial()
    {
        for (int i = 0; i < SPECIAL_LOCALES_.length; i ++) {
            int    j      = i * 3;
            Locale locale = SPECIAL_LOCALES_[i];
            String str    = SPECIAL_DATA_[j];
            if (locale != null) {
                if (!SPECIAL_DATA_[j + 1].equals(
                     UCharacter.toLowerCase(locale, str))) {
                    errln("error lowercasing special characters " +
                        hex(str) + " expected " + hex(SPECIAL_DATA_[j + 1])
                        + " for locale " + locale.toString() + " but got " +
                        hex(UCharacter.toLowerCase(locale, str)));
                }
                if (!SPECIAL_DATA_[j + 2].equals(
                     UCharacter.toUpperCase(locale, str))) {
                    errln("error uppercasing special characters " +
                        hex(str) + " expected " + SPECIAL_DATA_[j + 2]
                        + " for locale " + locale.toString() + " but got " +
                        hex(UCharacter.toUpperCase(locale, str)));
                }
            }
            else {
                if (!SPECIAL_DATA_[j + 1].equals(
                     UCharacter.toLowerCase(str))) {
                    errln("error lowercasing special characters " +
                        hex(str) + " expected " + SPECIAL_DATA_[j + 1] +
                        " but got " +
                        hex(UCharacter.toLowerCase(locale, str)));
                }
                if (!SPECIAL_DATA_[j + 2].equals(
                     UCharacter.toUpperCase(locale, str))) {
                    errln("error uppercasing special characters " +
                        hex(str) + " expected " + SPECIAL_DATA_[j + 2] +
                        " but got " +
                        hex(UCharacter.toUpperCase(locale, str)));
                }
            }
        }

        // turkish & azerbaijani dotless i & dotted I
        // remove dot above if there was a capital I before and there are no
        // more accents above
        if (!SPECIAL_DOTTED_LOWER_TURKISH_.equals(UCharacter.toLowerCase(
                                        TURKISH_LOCALE_, SPECIAL_DOTTED_))) {
            errln("error in dots.toLower(tr)=\"" + SPECIAL_DOTTED_ +
                  "\" expected \"" + SPECIAL_DOTTED_LOWER_TURKISH_ +
                  "\" but got " + UCharacter.toLowerCase(TURKISH_LOCALE_,
                                                         SPECIAL_DOTTED_));
        }
        if (!SPECIAL_DOTTED_LOWER_GERMAN_.equals(UCharacter.toLowerCase(
                                             GERMAN_LOCALE_, SPECIAL_DOTTED_))) {
            errln("error in dots.toLower(de)=\"" + SPECIAL_DOTTED_ +
                  "\" expected \"" + SPECIAL_DOTTED_LOWER_GERMAN_ +
                  "\" but got " + UCharacter.toLowerCase(GERMAN_LOCALE_,
                                                         SPECIAL_DOTTED_));
        }

        // lithuanian dot above in uppercasing
        if (!SPECIAL_DOT_ABOVE_UPPER_LITHUANIAN_.equals(
             UCharacter.toUpperCase(LITHUANIAN_LOCALE_, SPECIAL_DOT_ABOVE_))) {
            errln("error in dots.toUpper(lt)=\"" + SPECIAL_DOT_ABOVE_ +
                  "\" expected \"" + SPECIAL_DOT_ABOVE_UPPER_LITHUANIAN_ +
                  "\" but got " + UCharacter.toUpperCase(LITHUANIAN_LOCALE_,
                                                         SPECIAL_DOT_ABOVE_));
        }
        if (!SPECIAL_DOT_ABOVE_UPPER_GERMAN_.equals(UCharacter.toUpperCase(
                                        GERMAN_LOCALE_, SPECIAL_DOT_ABOVE_))) {
            errln("error in dots.toUpper(de)=\"" + SPECIAL_DOT_ABOVE_ +
                  "\" expected \"" + SPECIAL_DOT_ABOVE_UPPER_GERMAN_ +
                  "\" but got " + UCharacter.toUpperCase(GERMAN_LOCALE_,
                                                         SPECIAL_DOT_ABOVE_));
        }

        // lithuanian adds dot above to i in lowercasing if there are more
        // above accents
        if (!SPECIAL_DOT_ABOVE_LOWER_LITHUANIAN_.equals(
            UCharacter.toLowerCase(LITHUANIAN_LOCALE_,
                                   SPECIAL_DOT_ABOVE_UPPER_))) {
            errln("error in dots.toLower(lt)=\"" + SPECIAL_DOT_ABOVE_UPPER_ +
                  "\" expected \"" + SPECIAL_DOT_ABOVE_LOWER_LITHUANIAN_ +
                  "\" but got " + UCharacter.toLowerCase(LITHUANIAN_LOCALE_,
                                                   SPECIAL_DOT_ABOVE_UPPER_));
        }
        if (!SPECIAL_DOT_ABOVE_LOWER_GERMAN_.equals(
            UCharacter.toLowerCase(GERMAN_LOCALE_,
                                   SPECIAL_DOT_ABOVE_UPPER_))) {
            errln("error in dots.toLower(de)=\"" + SPECIAL_DOT_ABOVE_UPPER_ +
                  "\" expected \"" + SPECIAL_DOT_ABOVE_LOWER_GERMAN_ +
                  "\" but got " + UCharacter.toLowerCase(GERMAN_LOCALE_,
                                                   SPECIAL_DOT_ABOVE_UPPER_));
        }
    }

    /**
     * Tests for case mapping in the file SpecialCasing.txt
     * This method reads in SpecialCasing.txt file for testing purposes.
     * A default path is provided relative to the src path, however the user
     * could set a system property to change the directory path.<br>
     * e.g. java -DUnicodeData="data_dir_path" com.ibm.dev.test.lang.UCharacterTest
     */
    @Test
    public void TestSpecialCasingTxt()
    {
        try
        {
            // reading in the SpecialCasing file
            BufferedReader input = TestUtil.getDataReader(
                                                  "unicode/SpecialCasing.txt");
            while (true)
            {
                String s = input.readLine();
                if (s == null) {
                    break;
                }
                if (s.length() == 0 || s.charAt(0) == '#') {
                    continue;
                }

                String chstr[] = getUnicodeStrings(s);
                StringBuffer strbuffer   = new StringBuffer(chstr[0]);
                StringBuffer lowerbuffer = new StringBuffer(chstr[1]);
                StringBuffer upperbuffer = new StringBuffer(chstr[3]);
                Locale locale = null;
                for (int i = 4; i < chstr.length; i ++) {
                    String condition = chstr[i];
                    if (Character.isLowerCase(chstr[i].charAt(0))) {
                        // specified locale
                        locale = new Locale(chstr[i], "");
                    }
                    else if (condition.compareToIgnoreCase("Not_Before_Dot")
                                                      == 0) {
                        // turns I into dotless i
                    }
                    else if (condition.compareToIgnoreCase(
                                                      "More_Above") == 0) {
                            strbuffer.append((char)0x300);
                            lowerbuffer.append((char)0x300);
                            upperbuffer.append((char)0x300);
                    }
                    else if (condition.compareToIgnoreCase(
                                                "After_Soft_Dotted") == 0) {
                            strbuffer.insert(0, 'i');
                            lowerbuffer.insert(0, 'i');
                            String lang = "";
                            if (locale != null) {
                                lang = locale.getLanguage();
                            }
                            if (lang.equals("tr") || lang.equals("az")) {
                                // this is to be removed when 4.0 data comes out
                                // and upperbuffer.insert uncommented
                                // see jitterbug 2344
                                chstr[i] = "After_I";
                                strbuffer.deleteCharAt(0);
                                lowerbuffer.deleteCharAt(0);
                                i --;
                                continue;
                                // upperbuffer.insert(0, '\u0130');
                            }
                            else {
                                upperbuffer.insert(0, 'I');
                            }
                    }
                    else if (condition.compareToIgnoreCase(
                                                      "Final_Sigma") == 0) {
                            strbuffer.insert(0, 'c');
                            lowerbuffer.insert(0, 'c');
                            upperbuffer.insert(0, 'C');
                    }
                    else if (condition.compareToIgnoreCase("After_I") == 0) {
                            strbuffer.insert(0, 'I');
                            lowerbuffer.insert(0, 'i');
                            String lang = "";
                            if (locale != null) {
                                lang = locale.getLanguage();
                            }
                            if (lang.equals("tr") || lang.equals("az")) {
                                upperbuffer.insert(0, 'I');
                            }
                    }
                }
                chstr[0] = strbuffer.toString();
                chstr[1] = lowerbuffer.toString();
                chstr[3] = upperbuffer.toString();
                if (locale == null) {
                    if (!UCharacter.toLowerCase(chstr[0]).equals(chstr[1])) {
                        errln(s);
                        errln("Fail: toLowerCase for character " +
                              Utility.escape(chstr[0]) + ", expected "
                              + Utility.escape(chstr[1]) + " but resulted in " +
                              Utility.escape(UCharacter.toLowerCase(chstr[0])));
                    }
                    if (!UCharacter.toUpperCase(chstr[0]).equals(chstr[3])) {
                        errln(s);
                        errln("Fail: toUpperCase for character " +
                              Utility.escape(chstr[0]) + ", expected "
                              + Utility.escape(chstr[3]) + " but resulted in " +
                              Utility.escape(UCharacter.toUpperCase(chstr[0])));
                    }
                }
                else {
                    if (!UCharacter.toLowerCase(locale, chstr[0]).equals(
                                                                   chstr[1])) {
                        errln(s);
                        errln("Fail: toLowerCase for character " +
                              Utility.escape(chstr[0]) + ", expected "
                              + Utility.escape(chstr[1]) + " but resulted in " +
                              Utility.escape(UCharacter.toLowerCase(locale,
                                                                    chstr[0])));
                    }
                    if (!UCharacter.toUpperCase(locale, chstr[0]).equals(
                                                                   chstr[3])) {
                        errln(s);
                        errln("Fail: toUpperCase for character " +
                              Utility.escape(chstr[0]) + ", expected "
                              + Utility.escape(chstr[3]) + " but resulted in " +
                              Utility.escape(UCharacter.toUpperCase(locale,
                                                                    chstr[0])));
                    }
                }
            }
            input.close();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
    }

    @Test
    public void TestUpperLower()
    {
        int upper[] = {0x0041, 0x0042, 0x00b2, 0x01c4, 0x01c6, 0x01c9, 0x01c8,
                        0x01c9, 0x000c};
        int lower[] = {0x0061, 0x0062, 0x00b2, 0x01c6, 0x01c6, 0x01c9, 0x01c9,
                        0x01c9, 0x000c};
        String upperTest = "abcdefg123hij.?:klmno";
        String lowerTest = "ABCDEFG123HIJ.?:KLMNO";

        // Checks LetterLike Symbols which were previously a source of
        // confusion [Bertrand A. D. 02/04/98]
        for (int i = 0x2100; i < 0x2138; i ++) {
            /* Unicode 5.0 adds lowercase U+214E (TURNED SMALL F) to U+2132 (TURNED CAPITAL F) */
            if (i != 0x2126 && i != 0x212a && i != 0x212b && i!=0x2132) {
                if (i != UCharacter.toLowerCase(i)) { // itself
                    errln("Failed case conversion with itself: \\u"
                            + Utility.hex(i, 4));
                }
                if (i != UCharacter.toUpperCase(i)) {
                    errln("Failed case conversion with itself: \\u"
                            + Utility.hex(i, 4));
                }
            }
        }
        for (int i = 0; i < upper.length; i ++) {
            if (UCharacter.toLowerCase(upper[i]) != lower[i]) {
                errln("FAILED UCharacter.tolower() for \\u"
                        + Utility.hex(upper[i], 4)
                        + " Expected \\u" + Utility.hex(lower[i], 4)
                        + " Got \\u"
                        + Utility.hex(UCharacter.toLowerCase(upper[i]), 4));
            }
        }
        logln("testing upper lower");
        for (int i = 0; i < upperTest.length(); i ++) {
            logln("testing to upper to lower");
            if (UCharacter.isLetter(upperTest.charAt(i)) &&
                !UCharacter.isLowerCase(upperTest.charAt(i))) {
                errln("Failed isLowerCase test at \\u"
                        + Utility.hex(upperTest.charAt(i), 4));
            }
            else if (UCharacter.isLetter(lowerTest.charAt(i))
                     && !UCharacter.isUpperCase(lowerTest.charAt(i))) {
                errln("Failed isUpperCase test at \\u"
                      + Utility.hex(lowerTest.charAt(i), 4));
            }
            else if (upperTest.charAt(i)
                            != UCharacter.toLowerCase(lowerTest.charAt(i))) {
                errln("Failed case conversion from \\u"
                        + Utility.hex(lowerTest.charAt(i), 4) + " To \\u"
                        + Utility.hex(upperTest.charAt(i), 4));
            }
            else if (lowerTest.charAt(i)
                    != UCharacter.toUpperCase(upperTest.charAt(i))) {
                errln("Failed case conversion : \\u"
                        + Utility.hex(upperTest.charAt(i), 4) + " To \\u"
                        + Utility.hex(lowerTest.charAt(i), 4));
            }
            else if (upperTest.charAt(i)
                    != UCharacter.toLowerCase(upperTest.charAt(i))) {
                errln("Failed case conversion with itself: \\u"
                        + Utility.hex(upperTest.charAt(i)));
            }
            else if (lowerTest.charAt(i)
                    != UCharacter.toUpperCase(lowerTest.charAt(i))) {
                errln("Failed case conversion with itself: \\u"
                        + Utility.hex(lowerTest.charAt(i)));
            }
        }
        logln("done testing upper Lower");
    }

    private void assertGreekUpper(String s, String expected) {
        assertEquals("toUpper/Greek(" + s + ')', expected, UCharacter.toUpperCase(GREEK_LOCALE_, s));
    }

    @Test
    public void TestGreekUpper() {
        // http://bugs.icu-project.org/trac/ticket/5456
        assertGreekUpper("άδικος, κείμενο, ίριδα", "ΑΔΙΚΟΣ, ΚΕΙΜΕΝΟ, ΙΡΙΔΑ");
        // https://bugzilla.mozilla.org/show_bug.cgi?id=307039
        // https://bug307039.bmoattachments.org/attachment.cgi?id=194893
        assertGreekUpper("Πατάτα", "ΠΑΤΑΤΑ");
        assertGreekUpper("Αέρας, Μυστήριο, Ωραίο", "ΑΕΡΑΣ, ΜΥΣΤΗΡΙΟ, ΩΡΑΙΟ");
        assertGreekUpper("Μαΐου, Πόρος, Ρύθμιση", "ΜΑΪΟΥ, ΠΟΡΟΣ, ΡΥΘΜΙΣΗ");
        assertGreekUpper("ΰ, Τηρώ, Μάιος", "Ϋ, ΤΗΡΩ, ΜΑΪΟΣ");
        assertGreekUpper("άυλος", "ΑΫΛΟΣ");
        assertGreekUpper("ΑΫΛΟΣ", "ΑΫΛΟΣ");
        assertGreekUpper("Άκλιτα ρήματα ή άκλιτες μετοχές", "ΑΚΛΙΤΑ ΡΗΜΑΤΑ Ή ΑΚΛΙΤΕΣ ΜΕΤΟΧΕΣ");
        // http://www.unicode.org/udhr/d/udhr_ell_monotonic.html
        assertGreekUpper("Επειδή η αναγνώριση της αξιοπρέπειας", "ΕΠΕΙΔΗ Η ΑΝΑΓΝΩΡΙΣΗ ΤΗΣ ΑΞΙΟΠΡΕΠΕΙΑΣ");
        assertGreekUpper("νομικού ή διεθνούς", "ΝΟΜΙΚΟΥ Ή ΔΙΕΘΝΟΥΣ");
        // http://unicode.org/udhr/d/udhr_ell_polytonic.html
        assertGreekUpper("Ἐπειδὴ ἡ ἀναγνώριση", "ΕΠΕΙΔΗ Η ΑΝΑΓΝΩΡΙΣΗ");
        assertGreekUpper("νομικοῦ ἢ διεθνοῦς", "ΝΟΜΙΚΟΥ Ή ΔΙΕΘΝΟΥΣ");
        // From Google bug report
        assertGreekUpper("Νέο, Δημιουργία", "ΝΕΟ, ΔΗΜΙΟΥΡΓΙΑ");
        // http://crbug.com/234797
        assertGreekUpper("Ελάτε να φάτε τα καλύτερα παϊδάκια!", "ΕΛΑΤΕ ΝΑ ΦΑΤΕ ΤΑ ΚΑΛΥΤΕΡΑ ΠΑΪΔΑΚΙΑ!");
        assertGreekUpper("Μαΐου, τρόλεϊ", "ΜΑΪΟΥ, ΤΡΟΛΕΪ");
        assertGreekUpper("Το ένα ή το άλλο.", "ΤΟ ΕΝΑ Ή ΤΟ ΑΛΛΟ.");
        // http://multilingualtypesetting.co.uk/blog/greek-typesetting-tips/
        assertGreekUpper("ρωμέικα", "ΡΩΜΕΪΚΑ");
    }

    private static final class EditChange {
        private boolean change;
        private int oldLength, newLength;
        EditChange(boolean change, int oldLength, int newLength) {
            this.change = change;
            this.oldLength = oldLength;
            this.newLength = newLength;
        }
    }

    private static void checkEditsIter(
            String name, Edits.Iterator ei1, Edits.Iterator ei2,  // two equal iterators
            EditChange[] expected, boolean withUnchanged) {
        assertFalse(name, ei2.findSourceIndex(-1));

        int expSrcIndex = 0;
        int expDestIndex = 0;
        int expReplIndex = 0;
        for (int expIndex = 0; expIndex < expected.length; ++expIndex) {
            EditChange expect = expected[expIndex];
            String msg = name + ' ' + expIndex;
            if (withUnchanged || expect.change) {
                assertTrue(msg, ei1.next());
                assertEquals(msg, expect.change, ei1.hasChange());
                assertEquals(msg, expect.oldLength, ei1.oldLength());
                assertEquals(msg, expect.newLength, ei1.newLength());
                assertEquals(msg, expSrcIndex, ei1.sourceIndex());
                assertEquals(msg, expDestIndex, ei1.destinationIndex());
                assertEquals(msg, expReplIndex, ei1.replacementIndex());
            }

            if (expect.oldLength > 0) {
                assertTrue(msg, ei2.findSourceIndex(expSrcIndex));
                assertEquals(msg, expect.change, ei2.hasChange());
                assertEquals(msg, expect.oldLength, ei2.oldLength());
                assertEquals(msg, expect.newLength, ei2.newLength());
                assertEquals(msg, expSrcIndex, ei2.sourceIndex());
                assertEquals(msg, expDestIndex, ei2.destinationIndex());
                assertEquals(msg, expReplIndex, ei2.replacementIndex());
                if (!withUnchanged) {
                    // For some iterators, move past the current range
                    // so that findSourceIndex() has to look before the current index.
                    ei2.next();
                    ei2.next();
                }
            }

            expSrcIndex += expect.oldLength;
            expDestIndex += expect.newLength;
            if (expect.change) {
                expReplIndex += expect.newLength;
            }
        }
        String msg = name + " end";
        assertFalse(msg, ei1.next());
        assertFalse(msg, ei1.hasChange());
        assertEquals(msg, 0, ei1.oldLength());
        assertEquals(msg, 0, ei1.newLength());
        assertEquals(msg, expSrcIndex, ei1.sourceIndex());
        assertEquals(msg, expDestIndex, ei1.destinationIndex());
        assertEquals(msg, expReplIndex, ei1.replacementIndex());

        assertFalse(name, ei2.findSourceIndex(expSrcIndex));
    }

    @Test
    public void TestEdits() {
        Edits edits = new Edits();
        assertFalse("new Edits", edits.hasChanges());
        assertEquals("new Edits", 0, edits.lengthDelta());
        edits.addUnchanged(1);  // multiple unchanged ranges are combined
        edits.addUnchanged(10000);  // too long, and they are split
        edits.addReplace(0, 0);
        edits.addUnchanged(2);
        assertFalse("unchanged 10003", edits.hasChanges());
        assertEquals("unchanged 10003", 0, edits.lengthDelta());
        edits.addReplace(1, 1);  // multiple short equal-length edits are compressed
        edits.addUnchanged(0);
        edits.addReplace(1, 1);
        edits.addReplace(1, 1);
        edits.addReplace(0, 10);
        edits.addReplace(100, 0);
        edits.addReplace(3000, 4000);  // variable-length encoding
        edits.addReplace(100000, 100000);
        assertTrue("some edits", edits.hasChanges());
        assertEquals("some edits", 10 - 100 + 1000, edits.lengthDelta());

        EditChange[] coarseExpectedChanges = new EditChange[] {
                new EditChange(false, 10003, 10003),
                new EditChange(true, 103103, 104013)
        };
        checkEditsIter("coarse",
                edits.getCoarseIterator(), edits.getCoarseIterator(),
                coarseExpectedChanges, true);
        checkEditsIter("coarse changes",
                edits.getCoarseChangesIterator(), edits.getCoarseChangesIterator(),
                coarseExpectedChanges, false);

        EditChange[] fineExpectedChanges = new EditChange[] {
                new EditChange(false, 10003, 10003),
                new EditChange(true, 1, 1),
                new EditChange(true, 1, 1),
                new EditChange(true, 1, 1),
                new EditChange(true, 0, 10),
                new EditChange(true, 100, 0),
                new EditChange(true, 3000, 4000),
                new EditChange(true, 100000, 100000)
        };
        checkEditsIter("fine",
                edits.getFineIterator(), edits.getFineIterator(),
                fineExpectedChanges, true);
        checkEditsIter("fine changes",
                edits.getFineChangesIterator(), edits.getFineChangesIterator(),
                fineExpectedChanges, false);

        edits.reset();
        assertFalse("reset", edits.hasChanges());
        assertEquals("reset", 0, edits.lengthDelta());
        Edits.Iterator ei = edits.getCoarseChangesIterator();
        assertFalse("reset then iterator", ei.next());
    }

    @Test
    public void TestCaseMapWithEdits() {
        StringBuilder sb = new StringBuilder();
        Edits edits = new Edits();

        sb = CaseMap.toLower().omitUnchangedText().apply(TURKISH_LOCALE_, "IstanBul", sb, edits);
        assertEquals("toLower(Istanbul)", "ıb", sb.toString());
        EditChange[] lowerExpectedChanges = new EditChange[] {
                new EditChange(true, 1, 1),
                new EditChange(false, 4, 4),
                new EditChange(true, 1, 1),
                new EditChange(false, 2, 2)
        };
        checkEditsIter("toLower(Istanbul)",
                edits.getFineIterator(), edits.getFineIterator(),
                lowerExpectedChanges, true);

        sb.delete(0, sb.length());
        edits.reset();
        sb = CaseMap.toUpper().omitUnchangedText().apply(GREEK_LOCALE_, "Πατάτα", sb, edits);
        assertEquals("toUpper(Πατάτα)", "ΑΤΑΤΑ", sb.toString());
        EditChange[] upperExpectedChanges = new EditChange[] {
                new EditChange(false, 1, 1),
                new EditChange(true, 1, 1),
                new EditChange(true, 1, 1),
                new EditChange(true, 1, 1),
                new EditChange(true, 1, 1),
                new EditChange(true, 1, 1)
        };
        checkEditsIter("toUpper(Πατάτα)",
                edits.getFineIterator(), edits.getFineIterator(),
                upperExpectedChanges, true);

        sb.delete(0, sb.length());
        edits.reset();
        sb = CaseMap.toTitle().omitUnchangedText().noBreakAdjustment().noLowercase().apply(
                new Locale("nl"), null, "IjssEL IglOo", sb, edits);
        assertEquals("toTitle(IjssEL IglOo)", "J", sb.toString());
        EditChange[] titleExpectedChanges = new EditChange[] {
                new EditChange(false, 1, 1),
                new EditChange(true, 1, 1),
                new EditChange(false, 10, 10)
        };
        checkEditsIter("toTitle(IjssEL IglOo)",
                edits.getFineIterator(), edits.getFineIterator(),
                titleExpectedChanges, true);

        sb.delete(0, sb.length());
        edits.reset();
        sb = CaseMap.fold().omitUnchangedText().turkic().apply("IßtanBul", sb, edits);
        assertEquals("fold(IßtanBul)", "ıssb", sb.toString());
        EditChange[] foldExpectedChanges = new EditChange[] {
                new EditChange(true, 1, 1),
                new EditChange(true, 1, 2),
                new EditChange(false, 3, 3),
                new EditChange(true, 1, 1),
                new EditChange(false, 2, 2)
        };
        checkEditsIter("fold(IßtanBul)",
                edits.getFineIterator(), edits.getFineIterator(),
                foldExpectedChanges, true);
    }

    // private data members - test data --------------------------------------

    private static final Locale TURKISH_LOCALE_ = new Locale("tr", "TR");
    private static final Locale GERMAN_LOCALE_ = new Locale("de", "DE");
    private static final Locale GREEK_LOCALE_ = new Locale("el", "GR");
    private static final Locale ENGLISH_LOCALE_ = new Locale("en", "US");
    private static final Locale LITHUANIAN_LOCALE_ = new Locale("lt", "LT");

    private static final int CHARACTER_UPPER_[] =
                      {0x41, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047,
                       0x00b1, 0x00b2, 0xb3, 0x0048, 0x0049, 0x004a, 0x002e,
                       0x003f, 0x003a, 0x004b, 0x004c, 0x4d, 0x004e, 0x004f,
                       0x01c4, 0x01c8, 0x000c, 0x0000};
    private static final int CHARACTER_LOWER_[] =
                      {0x61, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067,
                       0x00b1, 0x00b2, 0xb3, 0x0068, 0x0069, 0x006a, 0x002e,
                       0x003f, 0x003a, 0x006b, 0x006c, 0x6d, 0x006e, 0x006f,
                       0x01c6, 0x01c9, 0x000c, 0x0000};

    /*
     * CaseFolding.txt says about i and its cousins:
     *   0049; C; 0069; # LATIN CAPITAL LETTER I
     *   0049; T; 0131; # LATIN CAPITAL LETTER I
     *
     *   0130; F; 0069 0307; # LATIN CAPITAL LETTER I WITH DOT ABOVE
     *   0130; T; 0069; # LATIN CAPITAL LETTER I WITH DOT ABOVE
     * That's all.
     * See CaseFolding.txt and the Unicode Standard for how to apply the case foldings.
     */
    private static final int FOLDING_SIMPLE_[] = {
        // input, default, exclude special i
        0x61,   0x61,  0x61,
        0x49,   0x69,  0x131,
        0x130,  0x130, 0x69,
        0x131,  0x131, 0x131,
        0xdf,   0xdf,  0xdf,
        0xfb03, 0xfb03, 0xfb03,
        0x1040e,0x10436,0x10436,
        0x5ffff,0x5ffff,0x5ffff
    };
    private static final String FOLDING_MIXED_[] =
                          {"\u0061\u0042\u0130\u0049\u0131\u03d0\u00df\ufb03\ud93f\udfff",
                           "A\u00df\u00b5\ufb03\uD801\uDC0C\u0130\u0131"};
    private static final String FOLDING_DEFAULT_[] =
         {"\u0061\u0062\u0069\u0307\u0069\u0131\u03b2\u0073\u0073\u0066\u0066\u0069\ud93f\udfff",
          "ass\u03bcffi\uD801\uDC34i\u0307\u0131"};
    private static final String FOLDING_EXCLUDE_SPECIAL_I_[] =
         {"\u0061\u0062\u0069\u0131\u0131\u03b2\u0073\u0073\u0066\u0066\u0069\ud93f\udfff",
          "ass\u03bcffi\uD801\uDC34i\u0131"};
    /**
     * "IESUS CHRISTOS"
     */
    private static final String SHARED_UPPERCASE_GREEK_ =
        "\u0399\u0395\u03a3\u03a5\u03a3\u0020\u03a7\u03a1\u0399\u03a3\u03a4\u039f\u03a3";
    /**
     * "iesus christos"
     */
    private static final String SHARED_LOWERCASE_GREEK_ =
        "\u03b9\u03b5\u03c3\u03c5\u03c2\u0020\u03c7\u03c1\u03b9\u03c3\u03c4\u03bf\u03c2";
    private static final String SHARED_LOWERCASE_TURKISH_ =
        "\u0069\u0073\u0074\u0061\u006e\u0062\u0075\u006c\u002c\u0020\u006e\u006f\u0074\u0020\u0063\u006f\u006e\u0073\u0074\u0061\u006e\u0074\u0131\u006e\u006f\u0070\u006c\u0065\u0021";
    private static final String SHARED_UPPERCASE_TURKISH_ =
        "\u0054\u004f\u0050\u004b\u0041\u0050\u0049\u0020\u0050\u0041\u004c\u0041\u0043\u0045\u002c\u0020\u0130\u0053\u0054\u0041\u004e\u0042\u0055\u004c";
    private static final String SHARED_UPPERCASE_ISTANBUL_ =
                                          "\u0130STANBUL, NOT CONSTANTINOPLE!";
    private static final String SHARED_LOWERCASE_ISTANBUL_ =
                                          "i\u0307stanbul, not constantinople!";
    private static final String SHARED_LOWERCASE_TOPKAP_ =
                                          "topkap\u0131 palace, istanbul";
    private static final String SHARED_UPPERCASE_TOPKAP_ =
                                          "TOPKAPI PALACE, ISTANBUL";
    private static final String SHARED_LOWERCASE_GERMAN_ =
                                          "S\u00FC\u00DFmayrstra\u00DFe";
    private static final String SHARED_UPPERCASE_GERMAN_ =
                                          "S\u00DCSSMAYRSTRASSE";

    private static final String UPPER_BEFORE_ =
         "\u0061\u0042\u0069\u03c2\u00df\u03c3\u002f\ufb03\ufb03\ufb03\ud93f\udfff";
    private static final String UPPER_ROOT_ =
         "\u0041\u0042\u0049\u03a3\u0053\u0053\u03a3\u002f\u0046\u0046\u0049\u0046\u0046\u0049\u0046\u0046\u0049\ud93f\udfff";
    private static final String UPPER_TURKISH_ =
         "\u0041\u0042\u0130\u03a3\u0053\u0053\u03a3\u002f\u0046\u0046\u0049\u0046\u0046\u0049\u0046\u0046\u0049\ud93f\udfff";
    private static final String UPPER_MINI_ = "\u00df\u0061";
    private static final String UPPER_MINI_UPPER_ = "\u0053\u0053\u0041";

    private static final String LOWER_BEFORE_ =
                      "\u0061\u0042\u0049\u03a3\u00df\u03a3\u002f\ud93f\udfff";
    private static final String LOWER_ROOT_ =
                      "\u0061\u0062\u0069\u03c3\u00df\u03c2\u002f\ud93f\udfff";
    private static final String LOWER_TURKISH_ =
                      "\u0061\u0062\u0131\u03c3\u00df\u03c2\u002f\ud93f\udfff";

    /**
     * each item is an array with input string, result string, locale ID, break iterator, options
     * the break iterator is specified as an int, same as in BreakIterator.KIND_*:
     * 0=KIND_CHARACTER  1=KIND_WORD  2=KIND_LINE  3=KIND_SENTENCE  4=KIND_TITLE  -1=default (NULL=words)  -2=no breaks (.*)
     * options: T=U_FOLD_CASE_EXCLUDE_SPECIAL_I  L=U_TITLECASE_NO_LOWERCASE  A=U_TITLECASE_NO_BREAK_ADJUSTMENT
     * see ICU4C source/test/testdata/casing.txt
     */
    private static final String TITLE_DATA_[] = {
        "\u0061\u0042\u0020\u0069\u03c2\u0020\u00df\u03c3\u002f\ufb03\ud93f\udfff",
        "\u0041\u0042\u0020\u0049\u03a3\u0020\u0053\u0073\u03a3\u002f\u0046\u0066\u0069\ud93f\udfff",
        "",
        "0",
        "",

        "\u0061\u0042\u0020\u0069\u03c2\u0020\u00df\u03c3\u002f\ufb03\ud93f\udfff",
        "\u0041\u0062\u0020\u0049\u03c2\u0020\u0053\u0073\u03c3\u002f\u0046\u0066\u0069\ud93f\udfff",
        "",
        "1",
        "",

        "\u02bbaMeLikA huI P\u016b \u02bb\u02bb\u02bbiA", "\u02bbAmelika Hui P\u016b \u02bb\u02bb\u02bbIa", // titlecase first _cased_ letter, j4933
        "",
        "-1",
        "",

        " tHe QUIcK bRoWn", " The Quick Brown",
        "",
        "4",
        "",

        "\u01c4\u01c5\u01c6\u01c7\u01c8\u01c9\u01ca\u01cb\u01cc",
        "\u01c5\u01c5\u01c5\u01c8\u01c8\u01c8\u01cb\u01cb\u01cb", // UBRK_CHARACTER
        "",
        "0",
        "",

        "\u01c9ubav ljubav", "\u01c8ubav Ljubav", // Lj vs. L+j
        "",
        "-1",
        "",

        "'oH dOn'T tItLeCaSe AfTeR lEtTeR+'",  "'Oh Don't Titlecase After Letter+'",
        "",
        "-1",
        "",

        "a \u02bbCaT. A \u02bbdOg! \u02bbeTc.",
        "A \u02bbCat. A \u02bbDog! \u02bbEtc.",
        "",
        "-1",
        "", // default

        "a \u02bbCaT. A \u02bbdOg! \u02bbeTc.",
        "A \u02bbcat. A \u02bbdog! \u02bbetc.",
        "",
        "-1",
        "A", // U_TITLECASE_NO_BREAK_ADJUSTMENT

        "a \u02bbCaT. A \u02bbdOg! \u02bbeTc.",
        "A \u02bbCaT. A \u02bbdOg! \u02bbETc.",
        "",
        "3",
        "L", // UBRK_SENTENCE and U_TITLECASE_NO_LOWERCASE


        "\u02bbcAt! \u02bbeTc.",
        "\u02bbCat! \u02bbetc.",
        "",
        "-2",
        "", // -2=Trivial break iterator

        "\u02bbcAt! \u02bbeTc.",
        "\u02bbcat! \u02bbetc.",
        "",
        "-2",
        "A", // U_TITLECASE_NO_BREAK_ADJUSTMENT

        "\u02bbcAt! \u02bbeTc.",
        "\u02bbCAt! \u02bbeTc.",
        "",
        "-2",
        "L", // U_TITLECASE_NO_LOWERCASE

        "\u02bbcAt! \u02bbeTc.",
        "\u02bbcAt! \u02bbeTc.",
        "",
        "-2",
        "AL", // Both options

        // Test case for ticket #7251: UCharacter.toTitleCase() throws OutOfMemoryError
        // when TITLECASE_NO_LOWERCASE encounters a single-letter word
        "a b c",
        "A B C",
        "",
        "1",
        "L" // U_TITLECASE_NO_LOWERCASE
    };


    /**
     * <p>basic string, lower string, upper string, title string</p>
     */
    private static final String SPECIAL_DATA_[] = {
        UTF16.valueOf(0x1043C) + UTF16.valueOf(0x10414),
        UTF16.valueOf(0x1043C) + UTF16.valueOf(0x1043C),
        UTF16.valueOf(0x10414) + UTF16.valueOf(0x10414),
        "ab'cD \uFB00i\u0131I\u0130 \u01C7\u01C8\u01C9 " +
                         UTF16.valueOf(0x1043C) + UTF16.valueOf(0x10414),
        "ab'cd \uFB00i\u0131ii\u0307 \u01C9\u01C9\u01C9 " +
                              UTF16.valueOf(0x1043C) + UTF16.valueOf(0x1043C),
        "AB'CD FFIII\u0130 \u01C7\u01C7\u01C7 " +
                              UTF16.valueOf(0x10414) + UTF16.valueOf(0x10414),
        // sigmas followed/preceded by cased letters
        "i\u0307\u03a3\u0308j \u0307\u03a3\u0308j i\u00ad\u03a3\u0308 \u0307\u03a3\u0308 ",
        "i\u0307\u03c3\u0308j \u0307\u03c3\u0308j i\u00ad\u03c2\u0308 \u0307\u03c3\u0308 ",
        "I\u0307\u03a3\u0308J \u0307\u03a3\u0308J I\u00ad\u03a3\u0308 \u0307\u03a3\u0308 "
    };
    private static final Locale SPECIAL_LOCALES_[] = {
        null,
        ENGLISH_LOCALE_,
        null,
    };

    private static final String SPECIAL_DOTTED_ =
            "I \u0130 I\u0307 I\u0327\u0307 I\u0301\u0307 I\u0327\u0307\u0301";
    private static final String SPECIAL_DOTTED_LOWER_TURKISH_ =
            "\u0131 i i i\u0327 \u0131\u0301\u0307 i\u0327\u0301";
    private static final String SPECIAL_DOTTED_LOWER_GERMAN_ =
            "i i\u0307 i\u0307 i\u0327\u0307 i\u0301\u0307 i\u0327\u0307\u0301";
    private static final String SPECIAL_DOT_ABOVE_ =
            "a\u0307 \u0307 i\u0307 j\u0327\u0307 j\u0301\u0307";
    private static final String SPECIAL_DOT_ABOVE_UPPER_LITHUANIAN_ =
            "A\u0307 \u0307 I J\u0327 J\u0301\u0307";
    private static final String SPECIAL_DOT_ABOVE_UPPER_GERMAN_ =
            "A\u0307 \u0307 I\u0307 J\u0327\u0307 J\u0301\u0307";
    private static final String SPECIAL_DOT_ABOVE_UPPER_ =
            "I I\u0301 J J\u0301 \u012e \u012e\u0301 \u00cc\u00cd\u0128";
    private static final String SPECIAL_DOT_ABOVE_LOWER_LITHUANIAN_ =
            "i i\u0307\u0301 j j\u0307\u0301 \u012f \u012f\u0307\u0301 i\u0307\u0300i\u0307\u0301i\u0307\u0303";
    private static final String SPECIAL_DOT_ABOVE_LOWER_GERMAN_ =
            "i i\u0301 j j\u0301 \u012f \u012f\u0301 \u00ec\u00ed\u0129";

    // private methods -------------------------------------------------------

    /**
     * Converting the hex numbers represented between ';' to Unicode strings
     * @param str string to break up into Unicode strings
     * @return array of Unicode strings ending with a null
     */
    private String[] getUnicodeStrings(String str)
    {
        List<String> v = new ArrayList<String>(10);
        int start = 0;
        for (int casecount = 4; casecount > 0; casecount --) {
            int end = str.indexOf("; ", start);
            String casestr = str.substring(start, end);
            StringBuffer buffer = new StringBuffer();
            int spaceoffset = 0;
            while (spaceoffset < casestr.length()) {
                int nextspace = casestr.indexOf(' ', spaceoffset);
                if (nextspace == -1) {
                    nextspace = casestr.length();
                }
                buffer.append((char)Integer.parseInt(
                                     casestr.substring(spaceoffset, nextspace),
                                                      16));
                spaceoffset = nextspace + 1;
            }
            start = end + 2;
            v.add(buffer.toString());
        }
        int comments = str.indexOf(" #", start);
        if (comments != -1 && comments != start) {
            if (str.charAt(comments - 1) == ';') {
                comments --;
            }
            String conditions = str.substring(start, comments);
            int offset = 0;
            while (offset < conditions.length()) {
                int spaceoffset = conditions.indexOf(' ', offset);
                if (spaceoffset == -1) {
                    spaceoffset = conditions.length();
                }
                v.add(conditions.substring(offset, spaceoffset));
                offset = spaceoffset + 1;
            }
        }
        int size = v.size();
        String result[] = new String[size];
        for (int i = 0; i < size; i ++) {
            result[i] = v.get(i);
        }
        return result;
    }
}
