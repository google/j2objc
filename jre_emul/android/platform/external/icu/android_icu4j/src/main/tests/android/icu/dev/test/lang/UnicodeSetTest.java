/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.lang;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.util.CollectionUtilities;
import android.icu.impl.SortedSetRelation;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterEnums.ECharacterCategory;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.text.SymbolTable;
import android.icu.text.UTF16;
import android.icu.text.UnicodeMatcher;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSet.ComparisonStyle;
import android.icu.text.UnicodeSet.EntryRange;
import android.icu.text.UnicodeSet.SpanCondition;
import android.icu.text.UnicodeSetIterator;
import android.icu.text.UnicodeSetSpanner;
import android.icu.text.UnicodeSetSpanner.CountMethod;
import android.icu.text.UnicodeSetSpanner.TrimOption;
import android.icu.util.OutputInt;

/**
 * @test
 * @summary General test of UnicodeSet
 */
public class UnicodeSetTest extends TestFmwk {

    static final String NOT = "%%%%";

    private static final boolean isCccValue(int ccc) {
        switch (ccc) {
        case 0:
        case 1:
        case 7:
        case 8:
        case 9:
        case 200:
        case 202:
        case 216:
        case 218:
        case 220:
        case 222:
        case 224:
        case 226:
        case 228:
        case 230:
        case 232:
        case 233:
        case 234:
        case 240:
            return true;
        default:
            return false;
        }
    }

    @Test
    public void TestPropertyAccess() {
        int count = 0; 
        // test to see that all of the names work
        for (int propNum = UProperty.BINARY_START; propNum < UProperty.INT_LIMIT; ++propNum) {
            count++;
            //Skipping tests in the non-exhaustive mode to shorten the test time ticket#6475
            if(TestFmwk.getExhaustiveness()<=5 && count%5!=0){
                continue;
            }
            if (propNum >= UProperty.BINARY_LIMIT && propNum < UProperty.INT_START) { // skip the gap
                propNum = UProperty.INT_START;
            }
            for (int nameChoice = UProperty.NameChoice.SHORT; nameChoice <= UProperty.NameChoice.LONG; ++nameChoice) {
                String propName;
                try {
                    propName = UCharacter.getPropertyName(propNum, nameChoice);
                    if (propName == null) {
                        if (nameChoice == UProperty.NameChoice.SHORT) continue; // allow non-existent short names
                        throw new NullPointerException();
                    }
                } catch (RuntimeException e1) {
                    errln("Can't get property name for: "
                            + "Property (" + propNum + ")"
                            + ", NameChoice: " + nameChoice + ", "
                            + e1.getClass().getName());
                    continue;
                }
                logln("Property (" + propNum + "): " + propName);
                for (int valueNum = UCharacter.getIntPropertyMinValue(propNum); valueNum <= UCharacter.getIntPropertyMaxValue(propNum); ++valueNum) {
                    String valueName;
                    try {
                        valueName = UCharacter.getPropertyValueName(propNum, valueNum, nameChoice);
                        if (valueName == null) {
                            if (nameChoice == UProperty.NameChoice.SHORT) continue; // allow non-existent short names
                            if ((propNum == UProperty.CANONICAL_COMBINING_CLASS ||
                                    propNum == UProperty.LEAD_CANONICAL_COMBINING_CLASS ||
                                    propNum == UProperty.TRAIL_CANONICAL_COMBINING_CLASS) &&
                                    !isCccValue(valueNum)) {
                                // Only a few of the canonical combining classes have names.
                                // Otherwise they are just integer values.
                                continue;
                            } else {
                                throw new NullPointerException();
                            }
                        }
                    } catch (RuntimeException e1) {
                        errln("Can't get property value name for: "
                                + "Property (" + propNum + "): " + propName + ", " 
                                + "Value (" + valueNum + ") "
                                + ", NameChoice: " + nameChoice + ", "
                                + e1.getClass().getName());
                        continue;
                    }
                    logln("Value (" + valueNum + "): " + valueName);
                    UnicodeSet testSet;
                    try {
                        testSet = new UnicodeSet("[:" + propName + "=" + valueName + ":]");
                    } catch (RuntimeException e) {
                        errln("Can't create UnicodeSet for: "
                                + "Property (" + propNum + "): " + propName + ", " 
                                + "Value (" + valueNum + "): " + valueName + ", "
                                + e.getClass().getName());
                        continue;
                    }
                    UnicodeSet collectedErrors = new UnicodeSet();
                    for (UnicodeSetIterator it = new UnicodeSetIterator(testSet); it.next();) {
                        int value = UCharacter.getIntPropertyValue(it.codepoint, propNum);
                        if (value != valueNum) {
                            collectedErrors.add(it.codepoint);
                        }
                    }
                    if (collectedErrors.size() != 0) {
                        errln("Property Value Differs: " 
                                + "Property (" + propNum + "): " + propName + ", " 
                                + "Value (" + valueNum + "): " + valueName + ", "
                                + "Differing values: " + collectedErrors.toPattern(true));
                    }
                }
            } 
        }
    }


    /**
     * Test toPattern().
     */
    @Test
    public void TestToPattern() throws Exception {
        // Test that toPattern() round trips with syntax characters
        // and whitespace.
        for (int i = 0; i < OTHER_TOPATTERN_TESTS.length; ++i) {
            checkPat(OTHER_TOPATTERN_TESTS[i], new UnicodeSet(OTHER_TOPATTERN_TESTS[i]));
        }
        for (int i = 0; i <= 0x10FFFF; ++i) {
            if ((i <= 0xFF && !UCharacter.isLetter(i)) || UCharacter.isWhitespace(i)) {
                // check various combinations to make sure they all work.
                if (i != 0 && !toPatternAux(i, i)) continue;
                if (!toPatternAux(0, i)) continue;
                if (!toPatternAux(i, 0xFFFF)) continue;
            }
        } 

        // Test pattern behavior of multicharacter strings.
        UnicodeSet s = new UnicodeSet("[a-z {aa} {ab}]");
        expectToPattern(s, "[a-z{aa}{ab}]",
                new String[] {"aa", "ab", NOT, "ac"});
        s.add("ac");
        expectToPattern(s, "[a-z{aa}{ab}{ac}]",
                new String[] {"aa", "ab", "ac", NOT, "xy"});

        s.applyPattern("[a-z {\\{l} {r\\}}]");
        expectToPattern(s, "[a-z{r\\}}{\\{l}]",
                new String[] {"{l", "r}", NOT, "xy"});
        s.add("[]");
        expectToPattern(s, "[a-z{\\[\\]}{r\\}}{\\{l}]",
                new String[] {"{l", "r}", "[]", NOT, "xy"});

        s.applyPattern("[a-z {\u4E01\u4E02}{\\n\\r}]");
        expectToPattern(s, "[a-z{\\u000A\\u000D}{\\u4E01\\u4E02}]",
                new String[] {"\u4E01\u4E02", "\n\r"});

        s.clear();
        s.add("abc");
        s.add("abc");
        expectToPattern(s, "[{abc}]",
                new String[] {"abc", NOT, "ab"});

        // JB#3400: For 2 character ranges prefer [ab] to [a-b]
        s.clear(); 
        s.add('a', 'b');
        expectToPattern(s, "[ab]", null);

        // Cover applyPattern, applyPropertyAlias
        s.clear();
        s.applyPattern("[ab ]", true);
        expectToPattern(s, "[ab]", new String[] {"a", NOT, "ab", " "});
        s.clear();
        s.applyPattern("[ab ]", false);
        expectToPattern(s, "[\\ ab]", new String[] {"a", "\u0020", NOT, "ab"});

        s.clear();
        s.applyPropertyAlias("nv", "0.5");
        s.retainAll(new UnicodeSet("[:age=6.0:]"));  // stabilize this test
        expectToPattern(s, "[\\u00BD\\u0B73\\u0D74\\u0F2A\\u2CFD\\uA831\\U00010141\\U00010175\\U00010176\\U00010E7B]", null);
        // Unicode 5.1 adds Malayalam 1/2 (\u0D74)
        // Unicode 5.2 adds U+A831 NORTH INDIC FRACTION ONE HALF and U+10E7B RUMI FRACTION ONE HALF
        // Unicode 6.0 adds U+0B73 ORIYA FRACTION ONE HALF

        s.clear();
        s.applyPropertyAlias("gc", "Lu");
        // TODO expectToPattern(s, what?)

        // RemoveAllStrings()
        s.clear();
        s.applyPattern("[a-z{abc}{def}]");
        expectToPattern(s, "[a-z{abc}{def}]", null);
        s.removeAllStrings();
        expectToPattern(s, "[a-z]", null);
    }

    static String[] OTHER_TOPATTERN_TESTS = {
        "[[:latin:]&[:greek:]]", 
        "[[:latin:]-[:greek:]]",
        "[:nonspacing mark:]"
    };


    public boolean toPatternAux(int start, int end) {
        // use Integer.toString because Utility.hex doesn't handle ints
        String source = "0x" + Integer.toString(start,16).toUpperCase();
        if (start != end) source += "..0x" + Integer.toString(end,16).toUpperCase();
        UnicodeSet testSet = new UnicodeSet();
        testSet.add(start, end);
        return checkPat(source, testSet);
    }

    boolean checkPat (String source, UnicodeSet testSet) {
        String pat = "";
        try {
            // What we want to make sure of is that a pattern generated
            // by toPattern(), with or without escaped unprintables, can
            // be passed back into the UnicodeSet constructor.
            String pat0 = testSet.toPattern(true);
            if (!checkPat(source + " (escaped)", testSet, pat0)) return false;

            //String pat1 = unescapeLeniently(pat0);
            //if (!checkPat(source + " (in code)", testSet, pat1)) return false;

            String pat2 = testSet.toPattern(false);
            if (!checkPat(source, testSet, pat2)) return false;

            //String pat3 = unescapeLeniently(pat2);
            //if (!checkPat(source + " (in code)", testSet, pat3)) return false;

            //logln(source + " => " + pat0 + ", " + pat1 + ", " + pat2 + ", " + pat3);
            logln(source + " => " + pat0 + ", " + pat2);
        } catch (Exception e) {
            errln("EXCEPTION in toPattern: " + source + " => " + pat);
            return false;
        }
        return true;
    }

    boolean checkPat (String source, UnicodeSet testSet, String pat) {
        UnicodeSet testSet2 = new UnicodeSet(pat);
        if (!testSet2.equals(testSet)) {
            errln("Fail toPattern: " + source + "; " + pat + " => " +
                    testSet2.toPattern(false) + ", expected " +
                    testSet.toPattern(false));
            return false;
        }
        return true;
    }

    // NOTE: copied the following from Utility. There ought to be a version in there with a flag
    // that does the Java stuff

    public static int unescapeAt(String s, int[] offset16) {
        int c;
        int result = 0;
        int n = 0;
        int minDig = 0;
        int maxDig = 0;
        int bitsPerDigit = 4;
        int dig;
        int i;

        /* Check that offset is in range */
        int offset = offset16[0];
        int length = s.length();
        if (offset < 0 || offset >= length) {
            return -1;
        }

        /* Fetch first UChar after '\\' */
        c = UTF16.charAt(s, offset);
        offset += UTF16.getCharCount(c);

        /* Convert hexadecimal and octal escapes */
        switch (c) {
        case 'u':
            minDig = maxDig = 4;
            break;
            /*
         case 'U':
         minDig = maxDig = 8;
         break;
         case 'x':
         minDig = 1;
         maxDig = 2;
         break;
             */
        default:
            dig = UCharacter.digit(c, 8);
            if (dig >= 0) {
                minDig = 1;
                maxDig = 3;
                n = 1; /* Already have first octal digit */
                bitsPerDigit = 3;
                result = dig;
            }
            break;
        }
        if (minDig != 0) {
            while (offset < length && n < maxDig) {
                // TEMPORARY
                // TODO: Restore the char32-based code when UCharacter.digit
                // is working (Bug 66).

                //c = UTF16.charAt(s, offset);
                //dig = UCharacter.digit(c, (bitsPerDigit == 3) ? 8 : 16);
                c = s.charAt(offset);
                dig = Character.digit((char)c, (bitsPerDigit == 3) ? 8 : 16);
                if (dig < 0) {
                    break;
                }
                result = (result << bitsPerDigit) | dig;
                //offset += UTF16.getCharCount(c);
                ++offset;
                ++n;
            }
            if (n < minDig) {
                return -1;
            }
            offset16[0] = offset;
            return result;
        }

        /* Convert C-style escapes in table */
        for (i=0; i<UNESCAPE_MAP.length; i+=2) {
            if (c == UNESCAPE_MAP[i]) {
                offset16[0] = offset;
                return UNESCAPE_MAP[i+1];
            } else if (c < UNESCAPE_MAP[i]) {
                break;
            }
        }

        /* If no special forms are recognized, then consider
         * the backslash to generically escape the next character. */
        offset16[0] = offset;
        return c;
    }

    /* This map must be in ASCENDING ORDER OF THE ESCAPE CODE */
    static private final char[] UNESCAPE_MAP = {
        /*"   0x22, 0x22 */
        /*'   0x27, 0x27 */
        /*?   0x3F, 0x3F */
        /*\   0x5C, 0x5C */
        /*a*/ 0x61, 0x07,
        /*b*/ 0x62, 0x08,
        /*f*/ 0x66, 0x0c,
        /*n*/ 0x6E, 0x0a,
        /*r*/ 0x72, 0x0d,
        /*t*/ 0x74, 0x09,
        /*v*/ 0x76, 0x0b
    };

    /**
     * Convert all escapes in a given string using unescapeAt().
     * Leave invalid escape sequences unchanged.
     */
    public static String unescapeLeniently(String s) {
        StringBuffer buf = new StringBuffer();
        int[] pos = new int[1];
        for (int i=0; i<s.length(); ) {
            char c = s.charAt(i++);
            if (c == '\\') {
                pos[0] = i;
                int e = unescapeAt(s, pos);
                if (e < 0) {
                    buf.append(c);
                } else {
                    UTF16.append(buf, e);
                    i = pos[0];
                }
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    @Test
    public void TestPatterns() {
        UnicodeSet set = new UnicodeSet();
        expectPattern(set, "[[a-m]&[d-z]&[k-y]]",  "km");
        expectPattern(set, "[[a-z]-[m-y]-[d-r]]",  "aczz");
        expectPattern(set, "[a\\-z]",  "--aazz");
        expectPattern(set, "[-az]",  "--aazz");
        expectPattern(set, "[az-]",  "--aazz");
        expectPattern(set, "[[[a-z]-[aeiou]i]]", "bdfnptvz");

        // Throw in a test of complement
        set.complement();
        String exp = '\u0000' + "aeeoouu" + (char)('z'+1) + '\uFFFF';
        expectPairs(set, exp);
    }

    @Test
    public void TestCategories() {
        int failures = 0;
        UnicodeSet set = new UnicodeSet("[:Lu:]");
        expectContainment(set, "ABC", "abc");

        // Make sure generation of L doesn't pollute cached Lu set
        // First generate L, then Lu
        // not used int TOP = 0x200; // Don't need to go over the whole range:
        set = new UnicodeSet("[:L:]");
        for (int i=0; i<0x200; ++i) {
            boolean l = UCharacter.isLetter(i);
            if (l != set.contains((char)i)) {
                errln("FAIL: L contains " + (char)i + " = " + 
                        set.contains((char)i));
                if (++failures == 10) break;
            }
        }

        set = new UnicodeSet("[:Lu:]");
        for (int i=0; i<0x200; ++i) {
            boolean lu = (UCharacter.getType(i) == ECharacterCategory.UPPERCASE_LETTER);
            if (lu != set.contains((char)i)) {
                errln("FAIL: Lu contains " + (char)i + " = " + 
                        set.contains((char)i));
                if (++failures == 20) break;
            }
        }
    }

    @Test
    public void TestAddRemove() {
        UnicodeSet set = new UnicodeSet();
        set.add('a', 'z');
        expectPairs(set, "az");
        set.remove('m', 'p');
        expectPairs(set, "alqz");
        set.remove('e', 'g');
        expectPairs(set, "adhlqz");
        set.remove('d', 'i');
        expectPairs(set, "acjlqz");
        set.remove('c', 'r');
        expectPairs(set, "absz");
        set.add('f', 'q');
        expectPairs(set, "abfqsz");
        set.remove('a', 'g');
        expectPairs(set, "hqsz");
        set.remove('a', 'z');
        expectPairs(set, "");

        // Try removing an entire set from another set
        expectPattern(set, "[c-x]", "cx");
        UnicodeSet set2 = new UnicodeSet();
        expectPattern(set2, "[f-ky-za-bc[vw]]", "acfkvwyz");
        set.removeAll(set2);
        expectPairs(set, "deluxx");

        // Try adding an entire set to another set
        expectPattern(set, "[jackiemclean]", "aacceein");
        expectPattern(set2, "[hitoshinamekatajamesanderson]", "aadehkmort");
        set.addAll(set2);
        expectPairs(set, "aacehort");

        // Test commutativity
        expectPattern(set, "[hitoshinamekatajamesanderson]", "aadehkmort");
        expectPattern(set2, "[jackiemclean]", "aacceein");
        set.addAll(set2);
        expectPairs(set, "aacehort");
    }

    /**
     * Make sure minimal representation is maintained.
     */
    @Test
    public void TestMinimalRep() {
        // This is pretty thoroughly tested by checkCanonicalRep()
        // run against the exhaustive operation results.  Use the code
        // here for debugging specific spot problems.

        // 1 overlap against 2
        UnicodeSet set = new UnicodeSet("[h-km-q]");
        UnicodeSet set2 = new UnicodeSet("[i-o]");
        set.addAll(set2);
        expectPairs(set, "hq");
        // right
        set.applyPattern("[a-m]");
        set2.applyPattern("[e-o]");
        set.addAll(set2);
        expectPairs(set, "ao");
        // left
        set.applyPattern("[e-o]");
        set2.applyPattern("[a-m]");
        set.addAll(set2);
        expectPairs(set, "ao");
        // 1 overlap against 3
        set.applyPattern("[a-eg-mo-w]");
        set2.applyPattern("[d-q]");
        set.addAll(set2);
        expectPairs(set, "aw");
    }

    @Test
    public void TestAPI() {
        // default ct
        UnicodeSet set = new UnicodeSet();
        if (!set.isEmpty() || set.getRangeCount() != 0) {
            errln("FAIL, set should be empty but isn't: " +
                    set);
        }

        // clear(), isEmpty()
        set.add('a');
        if (set.isEmpty()) {
            errln("FAIL, set shouldn't be empty but is: " +
                    set);
        }
        set.clear();
        if (!set.isEmpty()) {
            errln("FAIL, set should be empty but isn't: " +
                    set);
        }

        // size()
        set.clear();
        if (set.size() != 0) {
            errln("FAIL, size should be 0, but is " + set.size() +
                    ": " + set);
        }
        set.add('a');
        if (set.size() != 1) {
            errln("FAIL, size should be 1, but is " + set.size() +
                    ": " + set);
        }
        set.add('1', '9');
        if (set.size() != 10) {
            errln("FAIL, size should be 10, but is " + set.size() +
                    ": " + set);
        }
        set.clear();
        set.complement();
        if (set.size() != 0x110000) {
            errln("FAIL, size should be 0x110000, but is" + set.size());
        }

        // contains(first, last)
        set.clear();
        set.applyPattern("[A-Y 1-8 b-d l-y]");
        for (int i = 0; i<set.getRangeCount(); ++i) {
            int a = set.getRangeStart(i);
            int b = set.getRangeEnd(i);
            if (!set.contains(a, b)) {
                errln("FAIL, should contain " + (char)a + '-' + (char)b +
                        " but doesn't: " + set);
            }
            if (set.contains((char)(a-1), b)) {
                errln("FAIL, shouldn't contain " +
                        (char)(a-1) + '-' + (char)b +
                        " but does: " + set);
            }
            if (set.contains(a, (char)(b+1))) {
                errln("FAIL, shouldn't contain " +
                        (char)a + '-' + (char)(b+1) +
                        " but does: " + set);
            }
        }

        // Ported InversionList test.
        UnicodeSet a = new UnicodeSet((char)3,(char)10);
        UnicodeSet b = new UnicodeSet((char)7,(char)15);
        UnicodeSet c = new UnicodeSet();

        logln("a [3-10]: " + a);
        logln("b [7-15]: " + b);
        c.set(a); c.addAll(b);
        UnicodeSet exp = new UnicodeSet((char)3,(char)15);
        if (c.equals(exp)) {
            logln("c.set(a).add(b): " + c);
        } else {
            errln("FAIL: c.set(a).add(b) = " + c + ", expect " + exp);
        }
        c.complement();
        exp.set((char)0, (char)2);
        exp.add((char)16, UnicodeSet.MAX_VALUE);
        if (c.equals(exp)) {
            logln("c.complement(): " + c);
        } else {
            errln(Utility.escape("FAIL: c.complement() = " + c + ", expect " + exp));
        }
        c.complement();
        exp.set((char)3, (char)15);
        if (c.equals(exp)) {
            logln("c.complement(): " + c);
        } else {
            errln("FAIL: c.complement() = " + c + ", expect " + exp);
        }
        c.set(a); c.complementAll(b);
        exp.set((char)3,(char)6);
        exp.add((char)11,(char) 15);
        if (c.equals(exp)) {
            logln("c.set(a).complement(b): " + c);
        } else {
            errln("FAIL: c.set(a).complement(b) = " + c + ", expect " + exp);
        }

        exp.set(c);
        c = bitsToSet(setToBits(c));
        if (c.equals(exp)) {
            logln("bitsToSet(setToBits(c)): " + c);
        } else {
            errln("FAIL: bitsToSet(setToBits(c)) = " + c + ", expect " + exp);
        } 

        // Additional tests for coverage JB#2118
        //UnicodeSet::complement(class UnicodeString const &)
        //UnicodeSet::complementAll(class UnicodeString const &)
        //UnicodeSet::containsNone(class UnicodeSet const &)
        //UnicodeSet::containsNone(long,long)
        //UnicodeSet::containsSome(class UnicodeSet const &)
        //UnicodeSet::containsSome(long,long)
        //UnicodeSet::removeAll(class UnicodeString const &)
        //UnicodeSet::retain(long)
        //UnicodeSet::retainAll(class UnicodeString const &)
        //UnicodeSet::serialize(unsigned short *,long,enum UErrorCode &)
        //UnicodeSetIterator::getString(void)
        set.clear();
        set.complement("ab");
        exp.applyPattern("[{ab}]");
        if (!set.equals(exp)) { errln("FAIL: complement(\"ab\")"); return; }

        UnicodeSetIterator iset = new UnicodeSetIterator(set);
        if (!iset.next() || iset.codepoint != UnicodeSetIterator.IS_STRING) {
            errln("FAIL: UnicodeSetIterator.next/IS_STRING");
        } else if (!iset.string.equals("ab")) {
            errln("FAIL: UnicodeSetIterator.string");
        }

        set.add((char)0x61, (char)0x7A);
        set.complementAll("alan");
        exp.applyPattern("[{ab}b-kmo-z]");
        if (!set.equals(exp)) { errln("FAIL: complementAll(\"alan\")"); return; }

        exp.applyPattern("[a-z]");
        if (set.containsNone(exp)) { errln("FAIL: containsNone(UnicodeSet)"); }
        if (!set.containsSome(exp)) { errln("FAIL: containsSome(UnicodeSet)"); }
        exp.applyPattern("[aln]");
        if (!set.containsNone(exp)) { errln("FAIL: containsNone(UnicodeSet)"); }
        if (set.containsSome(exp)) { errln("FAIL: containsSome(UnicodeSet)"); }

        if (set.containsNone((char)0x61, (char)0x7A)) {
            errln("FAIL: containsNone(char, char)");
        }
        if (!set.containsSome((char)0x61, (char)0x7A)) {
            errln("FAIL: containsSome(char, char)");
        }
        if (!set.containsNone((char)0x41, (char)0x5A)) {
            errln("FAIL: containsNone(char, char)");
        }
        if (set.containsSome((char)0x41, (char)0x5A)) {
            errln("FAIL: containsSome(char, char)");
        }

        set.removeAll("liu");
        exp.applyPattern("[{ab}b-hj-kmo-tv-z]");
        if (!set.equals(exp)) { errln("FAIL: removeAll(\"liu\")"); return; }

        set.retainAll("star");
        exp.applyPattern("[rst]");
        if (!set.equals(exp)) { errln("FAIL: retainAll(\"star\")"); return; }

        set.retain((char)0x73);
        exp.applyPattern("[s]");
        if (!set.equals(exp)) { errln("FAIL: retain('s')"); return; }

        // ICU 2.6 coverage tests
        // public final UnicodeSet retain(String s);
        // public final UnicodeSet remove(int c);
        // public final UnicodeSet remove(String s);
        // public int hashCode();
        set.applyPattern("[a-z{ab}{cd}]");
        set.retain("cd");
        exp.applyPattern("[{cd}]");
        if (!set.equals(exp)) { errln("FAIL: retain(\"cd\")"); return; }

        set.applyPattern("[a-z{ab}{cd}]");
        set.remove((char)0x63);
        exp.applyPattern("[abd-z{ab}{cd}]");
        if (!set.equals(exp)) { errln("FAIL: remove('c')"); return; }

        set.remove("cd");
        exp.applyPattern("[abd-z{ab}]");
        if (!set.equals(exp)) { errln("FAIL: remove(\"cd\")"); return; }

        if (set.hashCode() != exp.hashCode()) {
            errln("FAIL: hashCode() unequal");
        }
        exp.clear();
        if (set.hashCode() == exp.hashCode()) {
            errln("FAIL: hashCode() equal");
        }

        {
            //Cover addAll(Collection) and addAllTo(Collection)  
            //  Seems that there is a bug in addAll(Collection) operation
            //    Ram also add a similar test to UtilityTest.java
            logln("Testing addAll(Collection) ... ");  
            String[] array = {"a", "b", "c", "de"};
            List list = Arrays.asList(array);
            Set aset = new HashSet(list);
            logln(" *** The source set's size is: " + aset.size());

            set.clear();
            set.addAll(aset);
            if (set.size() != aset.size()) {
                errln("FAIL: After addAll, the UnicodeSet size expected " + aset.size() +
                        ", " + set.size() + " seen instead!");
            } else {
                logln("OK: After addAll, the UnicodeSet size got " + set.size());
            }

            List list2 = new ArrayList();
            set.addAllTo(list2);

            //verify the result
            log(" *** The elements are: ");
            String s = set.toPattern(true);
            logln(s);
            Iterator myiter = list2.iterator();
            while(myiter.hasNext()) {
                log(myiter.next().toString() + "  ");
            }
            logln("");  // a new line
        }

    }

    @Test
    public void TestStrings() {
        //  Object[][] testList = {
        //  {I_EQUALS,  UnicodeSet.fromAll("abc"),
        //  new UnicodeSet("[a-c]")},
        //  
        //  {I_EQUALS,  UnicodeSet.from("ch").add('a','z').add("ll"),
        //  new UnicodeSet("[{ll}{ch}a-z]")},
        //  
        //  {I_EQUALS,  UnicodeSet.from("ab}c"),  
        //  new UnicodeSet("[{ab\\}c}]")},
        //  
        //  {I_EQUALS,  new UnicodeSet('a','z').add('A', 'Z').retain('M','m').complement('X'), 
        //  new UnicodeSet("[[a-zA-Z]&[M-m]-[X]]")},
        //  };
        //  
        //  for (int i = 0; i < testList.length; ++i) {
        //  expectRelation(testList[i][0], testList[i][1], testList[i][2], "(" + i + ")");
        //  }        

        UnicodeSet[][] testList = {
                {UnicodeSet.fromAll("abc"),
                    new UnicodeSet("[a-c]")},

                    {UnicodeSet.from("ch").add('a','z').add("ll"),
                        new UnicodeSet("[{ll}{ch}a-z]")},

                        {UnicodeSet.from("ab}c"),  
                            new UnicodeSet("[{ab\\}c}]")},

                            {new UnicodeSet('a','z').add('A', 'Z').retain('M','m').complement('X'), 
                                new UnicodeSet("[[a-zA-Z]&[M-m]-[X]]")},
        };

        for (int i = 0; i < testList.length; ++i) {
            if (!testList[i][0].equals(testList[i][1])) {
                errln("FAIL: sets unequal; see source code (" + i + ")");
            }
        }        
    }

    static final Integer 
    I_ANY = new Integer(SortedSetRelation.ANY),
    I_CONTAINS = new Integer(SortedSetRelation.CONTAINS),
    I_DISJOINT = new Integer(SortedSetRelation.DISJOINT),
    I_NO_B = new Integer(SortedSetRelation.NO_B),
    I_ISCONTAINED = new Integer(SortedSetRelation.ISCONTAINED),
    I_EQUALS = new Integer(SortedSetRelation.EQUALS),
    I_NO_A = new Integer(SortedSetRelation.NO_A),
    I_NONE = new Integer(SortedSetRelation.NONE);

    @Test
    public void TestSetRelation() {

        String[] choices = {"a", "b", "cd", "ef"};
        int limit = 1 << choices.length;

        SortedSet iset = new TreeSet();
        SortedSet jset = new TreeSet();

        for (int i = 0; i < limit; ++i) {
            pick(i, choices, iset);
            for (int j = 0; j < limit; ++j) {
                pick(j, choices, jset);
                checkSetRelation(iset, jset, "(" + i + ")");
            }
        }
    }

    @Test
    public void TestSetSpeed() {
        // skip unless verbose
        if (!isVerbose()) return;

        SetSpeed2(100);
        SetSpeed2(1000);
    }

    public void SetSpeed2(int size) {

        SortedSet iset = new TreeSet();
        SortedSet jset = new TreeSet();

        for (int i = 0; i < size*2; i += 2) { // only even values
            iset.add(new Integer(i));
            jset.add(new Integer(i));
        }

        int iterations = 1000000 / size;

        logln("Timing comparison of Java vs Utility");
        logln("For about " + size + " objects that are almost all the same.");

        CheckSpeed(iset, jset, "when a = b", iterations);

        iset.add(new Integer(size + 1));    // add odd value in middle

        CheckSpeed(iset, jset, "when a contains b", iterations);        
        CheckSpeed(jset, iset, "when b contains a", iterations);

        jset.add(new Integer(size - 1));    // add different odd value in middle

        CheckSpeed(jset, iset, "when a, b are disjoint", iterations);        
    }

    void CheckSpeed(SortedSet iset, SortedSet jset, String message, int iterations) {
        CheckSpeed2(iset, jset, message, iterations);
        CheckSpeed3(iset, jset, message, iterations);
    }

    void CheckSpeed2(SortedSet iset, SortedSet jset, String message, int iterations) {
        boolean x;
        boolean y;

        // make sure code is loaded:
        x = iset.containsAll(jset);
        y = SortedSetRelation.hasRelation(iset, SortedSetRelation.CONTAINS, jset);
        if (x != y) errln("FAIL contains comparison");

        double start = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            x |= iset.containsAll(jset);
        }
        double middle = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            y |= SortedSetRelation.hasRelation(iset, SortedSetRelation.CONTAINS, jset);
        }
        double end = System.currentTimeMillis();

        double jtime = (middle - start)/iterations;
        double utime = (end - middle)/iterations;

        NumberFormat nf = NumberFormat.getPercentInstance();
        logln("Test contains: " + message + ": Java: " + jtime
                + ", Utility: " + utime + ", u:j: " + nf.format(utime/jtime));
    }

    void CheckSpeed3(SortedSet iset, SortedSet jset, String message, int iterations) {
        boolean x;
        boolean y;

        // make sure code is loaded:
        x = iset.equals(jset);
        y = SortedSetRelation.hasRelation(iset, SortedSetRelation.EQUALS, jset);
        if (x != y) errln("FAIL equality comparison");


        double start = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            x |= iset.equals(jset);
        }
        double middle = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            y |= SortedSetRelation.hasRelation(iset, SortedSetRelation.EQUALS, jset);
        }
        double end = System.currentTimeMillis();

        double jtime = (middle - start)/iterations;
        double utime = (end - middle)/iterations;

        NumberFormat nf = NumberFormat.getPercentInstance();
        logln("Test equals:   " + message + ": Java: " + jtime
                + ", Utility: " + utime + ", u:j: " + nf.format(utime/jtime));
    }

    void pick(int bits, Object[] examples, SortedSet output) {
        output.clear();
        for (int k = 0; k < 32; ++k) {
            if (((1<<k) & bits) != 0) output.add(examples[k]);
        }
    }

    public static final String[] RELATION_NAME = {
        "both-are-null",
        "a-is-null", 
        "equals", 
        "is-contained-in",
        "b-is-null",
        "is-disjoint_with",
        "contains", 
        "any", };

    boolean dumbHasRelation(Collection A, int filter, Collection B) {
        Collection ab = new TreeSet(A);
        ab.retainAll(B);
        if (ab.size() > 0 && (filter & SortedSetRelation.A_AND_B) == 0) return false; 

        // A - B size == A.size - A&B.size
        if (A.size() > ab.size() && (filter & SortedSetRelation.A_NOT_B) == 0) return false; 

        // B - A size == B.size - A&B.size
        if (B.size() > ab.size() && (filter & SortedSetRelation.B_NOT_A) == 0) return false; 


        return true;
    }    

    void checkSetRelation(SortedSet a, SortedSet b, String message) {
        for (int i = 0; i < 8; ++i) {

            boolean hasRelation = SortedSetRelation.hasRelation(a, i, b);
            boolean dumbHasRelation = dumbHasRelation(a, i, b);

            logln(message + " " + hasRelation + ":\t" + a + "\t" + RELATION_NAME[i] + "\t" + b);

            if (hasRelation != dumbHasRelation) {
                errln("FAIL: " + 
                        message + " " + dumbHasRelation + ":\t" + a + "\t" + RELATION_NAME[i] + "\t" + b);
            }
        }
        logln("");
    }

    /**
     * Test the [:Latin:] syntax.
     */
    @Test
    public void TestScriptSet() {

        expectContainment("[:Latin:]", "aA", CharsToUnicodeString("\\u0391\\u03B1"));

        expectContainment("[:Greek:]", CharsToUnicodeString("\\u0391\\u03B1"), "aA");

        /* Jitterbug 1423 */
        expectContainment("[[:Common:][:Inherited:]]", CharsToUnicodeString("\\U00003099\\U0001D169\\u0000"), "aA");

    }

    /**
     * Test the [:Latin:] syntax.
     */
    @Test
    public void TestPropertySet() {
        String[] DATA = {
                // Pattern, Chars IN, Chars NOT in

                "[:Latin:]",
                "aA",
                "\u0391\u03B1",

                "[\\p{Greek}]",
                "\u0391\u03B1",
                "aA",

                "\\P{ GENERAL Category = upper case letter }",
                "abc",
                "ABC",

                // Combining class: @since ICU 2.2
                // Check both symbolic and numeric
                "\\p{ccc=Nukta}",
                "\u0ABC",
                "abc",

                "\\p{Canonical Combining Class = 11}",
                "\u05B1",
                "\u05B2",

                "[:c c c = iota subscript :]",
                "\u0345",
                "xyz",

                // Bidi class: @since ICU 2.2
                "\\p{bidiclass=lefttoright}",
                "abc",
                "\u0671\u0672",

                // Binary properties: @since ICU 2.2
                "\\p{ideographic}",
                "\u4E0A",
                "x",

                "[:math=false:]",
                "q)*(", // )(and * were removed from math in Unicode 4.0.1
                "+<>^",

                // JB#1767 \N{}, \p{ASCII}
                "[:Ascii:]",
                "abc\u0000\u007F",
                "\u0080\u4E00",

                "[\\N{ latin small letter  a  }[:name= latin small letter z:]]",
                "az",
                "qrs",

                // JB#2015
                "[:any:]",
                "a\\U0010FFFF",
                "",

                "[:nv=0.5:]",
                "\u00BD\u0F2A",
                "\u00BC",

                // JB#2653: Age
                "[:Age=1.1:]",
                "\u03D6", // 1.1
                "\u03D8\u03D9", // 3.2

                "[:Age=3.1:]", 
                "\\u1800\\u3400\\U0002f800", 
                "\\u0220\\u034f\\u30ff\\u33ff\\ufe73\\U00010000\\U00050000", 

                // JB#2350: Case_Sensitive
                "[:Case Sensitive:]",
                "A\u1FFC\\U00010410",
                ";\u00B4\\U00010500",


                // Regex compatibility test
                "[-b]", // leading '-' is literal
                "-b",
                "ac",

                "[^-b]", // leading '-' is literal
                "ac",
                "-b",

                "[b-]", // trailing '-' is literal
                "-b",
                "ac",

                "[^b-]", // trailing '-' is literal
                "ac",
                "-b",

                "[a-b-]", // trailing '-' is literal
                "ab-",
                "c=",

                "[[a-q]&[p-z]-]", // trailing '-' is literal
                "pq-",
                "or=",

                "[\\s|\\)|:|$|\\>]", // from regex tests
                "s|):$>",
                "\\abc",

                "[\uDC00cd]", // JB#2906: isolated trail at start
                "cd\uDC00",
                "ab\uD800\\U00010000",

                "[ab\uD800]", // JB#2906: isolated trail at start
                "ab\uD800",
                "cd\uDC00\\U00010000",

                "[ab\uD800cd]", // JB#2906: isolated lead in middle
                "abcd\uD800",
                "ef\uDC00\\U00010000",

                "[ab\uDC00cd]", // JB#2906: isolated trail in middle
                "abcd\uDC00",
                "ef\uD800\\U00010000",

                "[:^lccc=0:]", // Lead canonical class
                "\u0300\u0301",
                "abcd\u00c0\u00c5",

                "[:^tccc=0:]", // Trail canonical class
                "\u0300\u0301\u00c0\u00c5",
                "abcd",

                "[[:^lccc=0:][:^tccc=0:]]", // Lead and trail canonical class
                "\u0300\u0301\u00c0\u00c5",
                "abcd",

                "[[:^lccc=0:]-[:^tccc=0:]]", // Stuff that starts with an accent but ends with a base (none right now)
                "",
                "abcd\u0300\u0301\u00c0\u00c5",

                "[[:ccc=0:]-[:lccc=0:]-[:tccc=0:]]", // Weirdos. Complete canonical class is zero, but both lead and trail are not
                "\u0F73\u0F75\u0F81",
                "abcd\u0300\u0301\u00c0\u00c5",

                "[:Assigned:]",
                "A\\uE000\\uF8FF\\uFDC7\\U00010000\\U0010FFFD",
                "\\u0888\\uFDD3\\uFFFE\\U00050005",

                // Script_Extensions, new in Unicode 6.0
                "[:scx=Arab:]",
                "\\u061E\\u061F\\u0620\\u0621\\u063F\\u0640\\u0650\\u065E\\uFDF1\\uFDF2\\uFDF3",
                "\\u061D\\uFDEF\\uFDFE",

                // U+FDF2 has Script=Arabic and also Arab in its Script_Extensions,
                // so scx-sc is missing U+FDF2.
                "[[:Script_Extensions=Arabic:]-[:Arab:]]",
                "\\u0640\\u064B\\u0650\\u0655",
                "\\uFDF2"
        };

        for (int i=0; i<DATA.length; i+=3) {  
            expectContainment(DATA[i], DATA[i+1], DATA[i+2]);
        }
    }

    @Test
    public void TestUnicodeSetStrings() {
        UnicodeSet uset = new UnicodeSet("[a{bc}{cd}pqr\u0000]");
        logln(uset + " ~ " + uset.getRegexEquivalent());
        String[][] testStrings = {{"x", "none"},
                {"bc", "all"},
                {"cdbca", "all"},
                {"a", "all"},
                {"bcx", "some"},
                {"ab", "some"},
                {"acb", "some"},
                {"bcda", "some"},
                {"dccbx", "none"},
        };
        for (int i = 0; i < testStrings.length; ++i) {
            check(uset, testStrings[i][0], testStrings[i][1]);
        }
    }


    private void check(UnicodeSet uset, String string, String desiredStatus) {
        boolean shouldContainAll = desiredStatus.equals("all");
        boolean shouldContainNone = desiredStatus.equals("none");
        if (uset.containsAll(string) != shouldContainAll) {
            errln("containsAll " +  string + " should be " + shouldContainAll);
        } else {
            logln("containsAll " +  string + " = " + shouldContainAll);
        }
        if (uset.containsNone(string) != shouldContainNone) {
            errln("containsNone " +  string + " should be " + shouldContainNone);
        } else {
            logln("containsNone " +  string + " = " + shouldContainNone);
        }
    }

    /**
     * Test cloning of UnicodeSet
     */
    @Test
    public void TestClone() {
        UnicodeSet s = new UnicodeSet("[abcxyz]");
        UnicodeSet t = (UnicodeSet) s.clone();
        expectContainment(t, "abc", "def");
    }

    /**
     * Test the indexOf() and charAt() methods.
     */
    @Test
    public void TestIndexOf() {
        UnicodeSet set = new UnicodeSet("[a-cx-y3578]");
        for (int i=0; i<set.size(); ++i) {
            int c = set.charAt(i);
            if (set.indexOf(c) != i) {
                errln("FAIL: charAt(" + i + ") = " + c +
                        " => indexOf() => " + set.indexOf(c));
            }
        }
        int c = set.charAt(set.size());
        if (c != -1) {
            errln("FAIL: charAt(<out of range>) = " +
                    Utility.escape(String.valueOf(c)));
        }
        int j = set.indexOf('q');
        if (j != -1) {
            errln("FAIL: indexOf('q') = " + j);
        }
    }

    @Test
    public void TestContainsString() {
        UnicodeSet x = new UnicodeSet("[a{bc}]");
        if (x.contains("abc")) errln("FAIL");
    }

    @Test
    public void TestExhaustive() {
        // exhaustive tests. Simulate UnicodeSets with integers.
        // That gives us very solid tests (except for large memory tests).

        char limit = (char)128;

        for (char i = 0; i < limit; ++i) {
            logln("Testing " + i + ", " + bitsToSet(i));
            _testComplement(i);

            // AS LONG AS WE ARE HERE, check roundtrip
            checkRoundTrip(bitsToSet(i));

            for (char j = 0; j < limit; ++j) {
                _testAdd(i,j);
                _testXor(i,j);
                _testRetain(i,j);
                _testRemove(i,j);
            }
        }
    }

    /**
     * Make sure each script name and abbreviated name can be used
     * to construct a UnicodeSet.
     */
    @Test
    public void TestScriptNames() {
        for (int i=0; i<UScript.CODE_LIMIT; ++i) {
            for (int j=0; j<2; ++j) {
                String pat = "";
                try {
                    String name =
                            (j==0) ? UScript.getName(i) : UScript.getShortName(i);
                            pat = "[:" + name + ":]";
                            UnicodeSet set = new UnicodeSet(pat);
                            logln("Ok: " + pat + " -> " + set.toPattern(false));
                } catch (IllegalArgumentException e) {
                    if (pat.length() == 0) {
                        errln("FAIL (in UScript): No name for script " + i);
                    } else {
                        errln("FAIL: Couldn't create " + pat);
                    }
                }
            }
        }
    }

    /**
     * Test closure API.
     */
    @Test
    public void TestCloseOver() {
        String CASE = String.valueOf(UnicodeSet.CASE);
        String[] DATA = {
                // selector, input, output
                CASE,
                "[aq\u00DF{Bc}{bC}{Fi}]",
                "[aAqQ\u00DF\u1E9E\uFB01{ss}{bc}{fi}]", // U+1E9E LATIN CAPITAL LETTER SHARP S is new in Unicode 5.1

                CASE,
                "[\u01F1]", // 'DZ'
                "[\u01F1\u01F2\u01F3]",

                CASE,
                "[\u1FB4]",
                "[\u1FB4{\u03AC\u03B9}]",

                CASE,
                "[{F\uFB01}]",
                "[\uFB03{ffi}]",            

                CASE,
                "[a-z]","[A-Za-z\u017F\u212A]",
                CASE,
                "[abc]","[A-Ca-c]",
                CASE,
                "[ABC]","[A-Ca-c]",
        };

        UnicodeSet s = new UnicodeSet();
        UnicodeSet t = new UnicodeSet();
        for (int i=0; i<DATA.length; i+=3) {
            int selector = Integer.parseInt(DATA[i]);
            String pat = DATA[i+1];
            String exp = DATA[i+2];
            s.applyPattern(pat);
            s.closeOver(selector);
            t.applyPattern(exp);
            if (s.equals(t)) {
                logln("Ok: " + pat + ".closeOver(" + selector + ") => " + exp);
            } else {
                errln("FAIL: " + pat + ".closeOver(" + selector + ") => " +
                        s.toPattern(true) + ", expected " + exp);
            }
        }

        // Test the pattern API
        s.applyPattern("[abc]", UnicodeSet.CASE);
        expectContainment(s, "abcABC", "defDEF");
        s = new UnicodeSet("[^abc]", UnicodeSet.CASE);
        expectContainment(s, "defDEF", "abcABC");
    }

    @Test
    public void TestEscapePattern() {
        // The following pattern must contain at least one range "c-d"
        // where c or d is a Pattern_White_Space.
        String pattern =
                "[\\uFEFF \\u200E-\\u20FF \\uFFF9-\\uFFFC \\U0001D173-\\U0001D17A \\U000F0000-\\U000FFFFD ]";
        String exp =
                "[\\u200E-\\u20FF\\uFEFF\\uFFF9-\\uFFFC\\U0001D173-\\U0001D17A\\U000F0000-\\U000FFFFD]";
        // We test this with two passes; in the second pass we
        // pre-unescape the pattern.  Since U+200E is Pattern_White_Space,
        // this fails -- which is what we expect.
        for (int pass=1; pass<=2; ++pass) {
            String pat = pattern;
            if (pass==2) {
                pat = Utility.unescape(pat);
            }
            // Pattern is only good for pass 1
            boolean isPatternValid = (pass==1);

            UnicodeSet set = null;
            try {
                set = new UnicodeSet(pat);
            } catch (IllegalArgumentException e) {
                set = null;
            }
            if ((set != null) != isPatternValid){
                errln("FAIL: applyPattern(" +
                        Utility.escape(pat) + ") => " + set);
                continue;
            }
            if (set == null) {
                continue;
            }
            if (set.contains((char)0x0644)){
                errln("FAIL: " + Utility.escape(pat) + " contains(U+0664)");
            }

            String newpat = set.toPattern(true);
            if (newpat.equals(exp)) {
                logln(Utility.escape(pat) + " => " + newpat);
            } else {
                errln("FAIL: " + Utility.escape(pat) + " => " + newpat);
            }

            for (int i=0; i<set.getRangeCount(); ++i) {
                StringBuffer str = new StringBuffer("Range ");
                str.append((char)(0x30 + i))
                .append(": ");
                UTF16.append(str, set.getRangeStart(i));
                str.append(" - ");
                UTF16.append(str, set.getRangeEnd(i));
                String s = Utility.escape(str.toString() + " (" + set.getRangeStart(i) + " - " +
                        set.getRangeEnd(i) + ")");
                if (set.getRangeStart(i) < 0) {
                    errln("FAIL: " + s);
                } else {
                    logln(s);
                }
            }
        }
    }

    @Test
    public void TestSymbolTable() {
        // Multiple test cases can be set up here.  Each test case
        // is terminated by null:
        // var, value, var, value,..., input pat., exp. output pat., null
        String DATA[] = {
                "us", "a-z", "[0-1$us]", "[0-1a-z]", null,
                "us", "[a-z]", "[0-1$us]", "[0-1[a-z]]", null,
                "us", "\\[a\\-z\\]", "[0-1$us]", "[-01\\[\\]az]", null
        };

        for (int i=0; i<DATA.length; ++i) {
            TokenSymbolTable sym = new TokenSymbolTable();

            // Set up variables
            while (DATA[i+2] != null) {
                sym.add(DATA[i], DATA[i+1]);
                i += 2;
            }

            // Input pattern and expected output pattern
            String inpat = DATA[i], exppat = DATA[i+1];
            i += 2;

            ParsePosition pos = new ParsePosition(0);
            UnicodeSet us = new UnicodeSet(inpat, pos, sym);

            // results
            if (pos.getIndex() != inpat.length()) {
                errln("Failed to read to end of string \""
                        + inpat + "\": read to "
                        + pos.getIndex() + ", length is "
                        + inpat.length());
            }

            UnicodeSet us2 = new UnicodeSet(exppat);
            if (!us.equals(us2)) {
                errln("Failed, got " + us + ", expected " + us2);
            } else {
                logln("Ok, got " + us);
            }

            //cover Unicode(String,ParsePosition,SymbolTable,int)
            ParsePosition inpos = new ParsePosition(0);
            UnicodeSet inSet = new UnicodeSet(inpat, inpos, sym, UnicodeSet.IGNORE_SPACE);
            UnicodeSet expSet = new UnicodeSet(exppat);
            if (!inSet.equals(expSet)) {
                errln("FAIL: Failed, got " + inSet + ", expected " + expSet);
            } else {
                logln("OK: got " + inSet);
            }
        }
    }

    /**
     * Test that Posix style character classes [:digit:], etc.
     *   have the Unicode definitions from TR 18.
     */
    @Test
    public void TestPosixClasses() {
        expectEqual("POSIX alpha", "[:alpha:]", "\\p{Alphabetic}");
        expectEqual("POSIX lower", "[:lower:]", "\\p{lowercase}");
        expectEqual("POSIX upper", "[:upper:]", "\\p{Uppercase}");
        expectEqual("POSIX punct", "[:punct:]", "\\p{gc=Punctuation}");
        expectEqual("POSIX digit", "[:digit:]", "\\p{gc=DecimalNumber}");
        expectEqual("POSIX xdigit", "[:xdigit:]", "[\\p{DecimalNumber}\\p{HexDigit}]");
        expectEqual("POSIX alnum", "[:alnum:]", "[\\p{Alphabetic}\\p{DecimalNumber}]");
        expectEqual("POSIX space", "[:space:]", "\\p{Whitespace}");
        expectEqual("POSIX blank", "[:blank:]", "[\\p{Whitespace}-[\\u000a\\u000B\\u000c\\u000d\\u0085\\p{LineSeparator}\\p{ParagraphSeparator}]]");
        expectEqual("POSIX cntrl", "[:cntrl:]", "\\p{Control}");
        expectEqual("POSIX graph", "[:graph:]", "[^\\p{Whitespace}\\p{Control}\\p{Surrogate}\\p{Unassigned}]");
        expectEqual("POSIX print", "[:print:]", "[[:graph:][:blank:]-[\\p{Control}]]");
    }

    @Test
    public void TestHangulSyllable() {
        final UnicodeSet lvt = new UnicodeSet("[:Hangul_Syllable_Type=LVT_Syllable:]");
        assertNotEquals("LVT count", new UnicodeSet(), lvt);
        logln(lvt + ": " + lvt.size());
        final UnicodeSet lv = new UnicodeSet("[:Hangul_Syllable_Type=LV_Syllable:]");
        assertNotEquals("LV count", new UnicodeSet(), lv);
        logln(lv + ": " + lv.size());
    }

    /**
     * Test that frozen classes disallow changes. For 4217
     */
    @Test
    public void TestFrozen() {
        UnicodeSet test = new UnicodeSet("[[:whitespace:]A]");
        test.freeze();
        checkModification(test, true);
        checkModification(test, false);
    }

    /**
     * Test Generic support
     */
    @Test
    public void TestGenerics() {
        UnicodeSet set1 = new UnicodeSet("[a-b d-g {ch} {zh}]").freeze();
        UnicodeSet set2 = new UnicodeSet("[e-f {ch}]").freeze();
        UnicodeSet set3 = new UnicodeSet("[d m-n {dh}]").freeze();
        // A useful range of sets for testing, including both characters and strings
        // set 1 contains set2
        // set 1 is overlaps with set 3
        // set 2 is disjoint with set 3

        //public Iterator<String> iterator() {

        ArrayList<String> oldList = new ArrayList<String>();
        for (UnicodeSetIterator it = new UnicodeSetIterator(set1); it.next();) {
            oldList.add(it.getString());
        }

        ArrayList<String> list1 = new ArrayList<String>();
        for (String s : set1) {
            list1.add(s);
        }
        assertEquals("iteration test", oldList, list1);

        //addAllTo(Iterable<T>, U)
        list1.clear();
        set1.addAllTo(list1);
        assertEquals("iteration test", oldList, list1);

        list1 = set1.addAllTo(new ArrayList<String>());
        assertEquals("addAllTo", oldList, list1);

        ArrayList<String> list2 = set2.addAllTo(new ArrayList<String>());
        ArrayList<String> list3 = set3.addAllTo(new ArrayList<String>());

        // put them into different order, to check that order doesn't matter
        TreeSet sorted1 = set1.addAllTo(new TreeSet<String>());
        TreeSet sorted2 = set2.addAllTo(new TreeSet<String>());
        TreeSet sorted3 = set3.addAllTo(new TreeSet<String>());

        //containsAll(Collection<String> collection)
        assertTrue("containsAll", set1.containsAll(list1));
        assertTrue("containsAll", set1.containsAll(sorted1));
        assertTrue("containsAll", set1.containsAll(list2));
        assertTrue("containsAll", set1.containsAll(sorted2));
        assertFalse("containsAll", set1.containsAll(list3));
        assertFalse("containsAll", set1.containsAll(sorted3));
        assertFalse("containsAll", set2.containsAll(list3));
        assertFalse("containsAll", set2.containsAll(sorted3));

        //containsSome(Collection<String>)
        assertTrue("containsSome", set1.containsSome(list1));
        assertTrue("containsSome", set1.containsSome(sorted1));
        assertTrue("containsSome", set1.containsSome(list2));
        assertTrue("containsSome", set1.containsSome(sorted2));
        assertTrue("containsSome", set1.containsSome(list3));
        assertTrue("containsSome", set1.containsSome(sorted3));
        assertFalse("containsSome", set2.containsSome(list3));
        assertFalse("containsSome", set2.containsSome(sorted3));

        //containsNone(Collection<String>)
        assertFalse("containsNone", set1.containsNone(list1));
        assertFalse("containsNone", set1.containsNone(sorted1));
        assertFalse("containsNone", set1.containsNone(list2));
        assertFalse("containsNone", set1.containsNone(sorted2));
        assertFalse("containsNone", set1.containsNone(list3));
        assertFalse("containsNone", set1.containsNone(sorted3));
        assertTrue("containsNone", set2.containsNone(list3));
        assertTrue("containsNone", set2.containsNone(sorted3));

        //addAll(String...)
        UnicodeSet other3 = new UnicodeSet().addAll("d", "m", "n", "dh");
        assertEquals("addAll", set3, other3);

        //removeAll(Collection<String>)
        UnicodeSet mod1 = new UnicodeSet(set1).removeAll(set2);
        UnicodeSet mod2 = new UnicodeSet(set1).removeAll(list2);
        assertEquals("remove all", mod1, mod2);

        //retainAll(Collection<String>)
        mod1 = new UnicodeSet(set1).retainAll(set2);
        mod2 = new UnicodeSet(set1).retainAll(set2.addAllTo(new LinkedHashSet<String>()));
        assertEquals("remove all", mod1, mod2);
    }

    @Test
    public void TestComparison() {
        UnicodeSet set1 = new UnicodeSet("[a-b d-g {ch} {zh}]").freeze();
        UnicodeSet set2 = new UnicodeSet("[c-e {ch}]").freeze();
        UnicodeSet set3 = new UnicodeSet("[d m-n z {dh}]").freeze();

        //compareTo(UnicodeSet)
        // do indirectly, by sorting
        List<UnicodeSet> unsorted = Arrays.asList(set3, set2, set1);
        List<UnicodeSet> goalShortest = Arrays.asList(set2, set3, set1);
        List<UnicodeSet> goalLongest = Arrays.asList(set1, set3, set2);
        List<UnicodeSet> goalLex = Arrays.asList(set1, set2, set3);

        List<UnicodeSet> sorted = new ArrayList(new TreeSet<UnicodeSet>(unsorted));
        assertNotEquals("compareTo-shorter-first", unsorted, sorted);
        assertEquals("compareTo-shorter-first", goalShortest, sorted);

        TreeSet<UnicodeSet> sorted1 = new TreeSet<UnicodeSet>(new Comparator<UnicodeSet>(){
            public int compare(UnicodeSet o1, UnicodeSet o2) {
                // TODO Auto-generated method stub
                return o1.compareTo(o2, ComparisonStyle.LONGER_FIRST);
            }});
        sorted1.addAll(unsorted);
        sorted = new ArrayList(sorted1);
        assertNotEquals("compareTo-longer-first", unsorted, sorted);
        assertEquals("compareTo-longer-first", goalLongest, sorted);

        sorted1 = new TreeSet<UnicodeSet>(new Comparator<UnicodeSet>(){
            public int compare(UnicodeSet o1, UnicodeSet o2) {
                // TODO Auto-generated method stub
                return o1.compareTo(o2, ComparisonStyle.LEXICOGRAPHIC);
            }});
        sorted1.addAll(unsorted);
        sorted = new ArrayList(sorted1);
        assertNotEquals("compareTo-lex", unsorted, sorted);
        assertEquals("compareTo-lex", goalLex, sorted);

        //compare(String, int)
        // make a list of interesting combinations
        List<String> sources = Arrays.asList("\u0000", "a", "b", "\uD7FF", "\uD800", "\uDBFF", "\uDC00", "\uDFFF", "\uE000", "\uFFFD", "\uFFFF");
        TreeSet<String> target = new TreeSet<String>();
        for (String s : sources) {
            target.add(s);
            for (String t : sources) {
                target.add(s + t);
                for (String u : sources) {
                    target.add(s + t + u);
                }
            }
        }
        // now compare all the combinations. If any of them is a code point, use it.
        int maxErrorCount = 0;
        compare:
            for (String last : target) {
                for (String curr : target) {
                    int lastCount = Character.codePointCount(last, 0, last.length());
                    int currCount = Character.codePointCount(curr, 0, curr.length());
                    int comparison;
                    if (lastCount == 1) {
                        comparison = UnicodeSet.compare(last.codePointAt(0), curr);
                    } else if (currCount == 1) {
                        comparison = UnicodeSet.compare(last, curr.codePointAt(0));
                    } else {
                        continue;
                    }
                    if (comparison != last.compareTo(curr)) {
                        // repeat for debugging
                        if (lastCount == 1) {
                            comparison = UnicodeSet.compare(last.codePointAt(0), curr);
                        } else if (currCount == 1) {
                            comparison = UnicodeSet.compare(last, curr.codePointAt(0));
                        }
                        if (maxErrorCount++ > 10) {
                            errln(maxErrorCount + " Failure in comparing " + last + " & " + curr + "\tOmitting others...");
                            break compare;
                        }
                        errln(maxErrorCount + " Failure in comparing " + last + " & " + curr);
                    }
                }
            }

        //compare(Iterable<T>, Iterable<T>)
        int max = 10;
        List<String> test1 = new ArrayList<String>(max);
        List<String> test2 = new ArrayList<String>(max);
        for (int i = 0; i <= max; ++i) {
            test1.add("a" + i);
            test2.add("a" + (max - i)); // add in reverse order
        }
        assertNotEquals("compare iterable test", test1, test2);
        TreeSet<CharSequence> sortedTest1 = new TreeSet<CharSequence>(test1);
        TreeSet<CharSequence> sortedTest2 = new TreeSet<CharSequence>(test2);
        assertEquals("compare iterable test", sortedTest1, sortedTest2);
    }

    @Test
    public void TestRangeConstructor() {
        UnicodeSet w = new UnicodeSet().addAll(3,5);
        UnicodeSet s = new UnicodeSet(3,5);
        assertEquals("new constructor", w, s);

        w = new UnicodeSet().addAll(3,5).addAll(7,7);
        UnicodeSet t = new UnicodeSet(3,5, 7,7);
        assertEquals("new constructor", w, t);
        // check to make sure right exceptions are thrown
        Class expected = IllegalArgumentException.class;
        Class actual;

        try {
            actual = null;
            @SuppressWarnings("unused")
            UnicodeSet u = new UnicodeSet(5);
        } catch (IllegalArgumentException e) {
            actual = e.getClass();
        }
        assertEquals("exception if odd", expected, actual);

        try {
            actual = null;
            @SuppressWarnings("unused")
            UnicodeSet u = new UnicodeSet(3, 2, 7, 9);
        } catch (IllegalArgumentException e) {
            actual = e.getClass();
        }
        assertEquals("exception for start/end problem", expected, actual);

        try {
            actual = null;
            @SuppressWarnings("unused")
            UnicodeSet u = new UnicodeSet(3, 5, 6, 9);
        } catch (IllegalArgumentException e) {
            actual = e.getClass();
        }
        assertEquals("exception for end/start problem", expected, actual);

        CheckRangeSpeed(10000, new UnicodeSet("[:whitespace:]"));
        CheckRangeSpeed(1000, new UnicodeSet("[:letter:]"));
    }

    /**
     * @param iterations
     * @param testSet
     */
    private void CheckRangeSpeed(int iterations, UnicodeSet testSet) {
        testSet.complement().complement();
        String testPattern = testSet.toString();
        // fill a set of pairs from the pattern
        int[] pairs = new int[testSet.getRangeCount()*2];
        int j = 0;
        for (UnicodeSetIterator it = new UnicodeSetIterator(testSet); it.nextRange();) {
            pairs[j++] = it.codepoint;
            pairs[j++] = it.codepointEnd;
        }
        UnicodeSet fromRange = new UnicodeSet(testSet);
        assertEquals("from range vs pattern", testSet, fromRange);

        double start = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            fromRange = new UnicodeSet(testSet);
        }
        double middle = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            new UnicodeSet(testPattern);
        }
        double end = System.currentTimeMillis();

        double rangeConstructorTime = (middle - start)/iterations;
        double patternConstructorTime = (end - middle)/iterations;
        String message = "Range constructor:\t" + rangeConstructorTime + ";\tPattern constructor:\t" + patternConstructorTime + "\t\t"
                + percent.format(rangeConstructorTime/patternConstructorTime-1);
        if (rangeConstructorTime < 2*patternConstructorTime) {
            logln(message);
        } else {
            errln(message);
        }
    }

    NumberFormat percent = NumberFormat.getPercentInstance();
    {
        percent.setMaximumFractionDigits(2);
    }
    // ****************************************
    // UTILITIES
    // ****************************************

    public void checkModification(UnicodeSet original, boolean isFrozen) {
        main:
            for (int i = 0; ;++i) {
                UnicodeSet test = (UnicodeSet) (isFrozen ? original.clone() : original.cloneAsThawed());
                boolean gotException = true;
                boolean checkEquals = true;
                try {
                    switch(i) {
                    case 0: test.add(0); break;
                    case 1: test.add(0,1); break;
                    case 2: test.add("a"); break;
                    case 3: List a = new ArrayList(); a.add("a"); test.addAll(a); break;
                    case 4: test.addAll("ab"); break;
                    case 5: test.addAll(new UnicodeSet("[ab]")); break;
                    case 6: test.applyIntPropertyValue(0,0); break;
                    case 7: test.applyPattern("[ab]"); break;
                    case 8: test.applyPattern("[ab]", true); break;
                    case 9: test.applyPattern("[ab]", 0); break;
                    case 10: test.applyPropertyAlias("hex","true"); break;
                    case 11: test.applyPropertyAlias("hex", "true", null); break;
                    case 12: test.closeOver(UnicodeSet.CASE); break;
                    case 13: test.compact(); checkEquals = false; break;
                    case 14: test.complement(0); break;
                    case 15: test.complement(0,0); break;
                    case 16: test.complement("ab"); break;
                    case 17: test.complementAll("ab"); break;
                    case 18: test.complementAll(new UnicodeSet("[ab]")); break;
                    case 19: test.remove(' '); break;
                    case 20: test.remove(' ','a'); break;
                    case 21: test.remove(" "); break;
                    case 22: test.removeAll(" a"); break;
                    case 23: test.removeAll(new UnicodeSet("[\\ a]")); break;
                    case 24: test.retain(' '); break;
                    case 25: test.retain(' ','a'); break;
                    case 26: test.retain(" "); break;
                    case 27: test.retainAll(" a"); break;
                    case 28: test.retainAll(new UnicodeSet("[\\ a]")); break;
                    case 29: test.set(0,1); break;
                    case 30: test.set(new UnicodeSet("[ab]")); break;

                    default: continue main; // so we don't keep having to change the endpoint, and gaps are not skipped.
                    case 35: return;
                    }
                    gotException = false;
                } catch (UnsupportedOperationException e) {
                    // do nothing
                }
                if (isFrozen && !gotException) errln(i + ") attempt to modify frozen object didn't result in an exception");
                if (!isFrozen && gotException) errln(i + ") attempt to modify thawed object did result in an exception");
                if (checkEquals) {
                    if (test.equals(original)) {
                        if (!isFrozen) errln(i + ") attempt to modify thawed object didn't change the object");
                    } else { // unequal
                        if (isFrozen) errln(i + ") attempt to modify frozen object changed the object");
                    }
                }
            }
    }

    // Following cod block is commented out to eliminate PrettyPrinter depenencies

    //    String[] prettyData = {
    //            "[\\uD7DE-\\uD90C \\uDCB5-\\uDD9F]", // special case
    //            "[:any:]",
    //            "[:whitespace:]",
    //            "[:linebreak=AL:]",
    //    };
    //
    //    public void TestPrettyPrinting() {
    //        try{
    //            PrettyPrinter pp = new PrettyPrinter();
    //
    //            int i = 0;
    //            for (; i < prettyData.length; ++i) {
    //                UnicodeSet test = new UnicodeSet(prettyData[i]);
    //                checkPrettySet(pp, i, test);
    //            }
    //            Random random = new Random(0);
    //            UnicodeSet test = new UnicodeSet();
    //
    //            // To keep runtimes under control, make the number of random test cases
    //            //   to try depends on the test framework exhaustive setting.
    //            //  params.inclusions = 5:   default exhaustive value
    //            //  params.inclusions = 10:  max exhaustive value.
    //            int iterations = 50;
    //            if (params.inclusion > 5) {
    //                iterations = (params.inclusion-5) * 200;
    //            }
    //            for (; i < iterations; ++i) {
    //                double start = random.nextGaussian() * 0x10000;
    //                if (start < 0) start = - start;
    //                if (start > 0x10FFFF) {
    //                    start = 0x10FFFF;
    //                }
    //                double end = random.nextGaussian() * 0x100;
    //                if (end < 0) end = -end;
    //                end = start + end;
    //                if (end > 0x10FFFF) {
    //                    end = 0x10FFFF;
    //                }
    //                test.complement((int)start, (int)end);
    //                checkPrettySet(pp, i, test);
    //            }
    //        }catch(RuntimeException ex){
    //            warnln("Could not load Collator");
    //        }
    //    }
    //
    //    private void checkPrettySet(PrettyPrinter pp, int i, UnicodeSet test) {
    //        String pretty = pp.toPattern(test);
    //        UnicodeSet retry = new UnicodeSet(pretty);
    //        if (!test.equals(retry)) {
    //            errln(i + ". Failed test: " + test + " != " + pretty);
    //        } else {
    //            logln(i + ". Worked for " + truncate(test.toString()) + " => " + truncate(pretty));
    //        }
    //    }
    //
    //    private String truncate(String string) {
    //        if (string.length() <= 100) return string;
    //        return string.substring(0,97) + "...";
    //    }

    public class TokenSymbolTable implements SymbolTable {
        HashMap contents = new HashMap();

        /**
         * (Non-SymbolTable API) Add the given variable and value to
         * the table.  Variable should NOT contain leading '$'.
         */
        public void add(String var, String value) {
            char[] buffer = new char[value.length()];
            value.getChars(0, value.length(), buffer, 0);
            add(var, buffer);
        }

        /**
         * (Non-SymbolTable API) Add the given variable and value to
         * the table.  Variable should NOT contain leading '$'.
         */
        public void add(String var, char[] body) {
            logln("TokenSymbolTable: add \"" + var + "\" => \"" +
                    new String(body) + "\"");
            contents.put(var, body);
        }

        /* (non-Javadoc)
         * @see android.icu.text.SymbolTable#lookup(java.lang.String)
         */
        public char[] lookup(String s) {
            logln("TokenSymbolTable: lookup \"" + s + "\" => \"" +
                    new String((char[]) contents.get(s)) + "\"");
            return (char[])contents.get(s);
        }

        /* (non-Javadoc)
         * @see android.icu.text.SymbolTable#lookupMatcher(int)
         */
        public UnicodeMatcher lookupMatcher(int ch) {
            return null;
        }

        /* (non-Javadoc)
         * @see android.icu.text.SymbolTable#parseReference(java.lang.String,
     java.text.ParsePosition, int)
         */
        public String parseReference(String text, ParsePosition pos, int
                limit) {
            int cp;
            int start = pos.getIndex();
            int i;
            for (i = start; i < limit; i += UTF16.getCharCount(cp)) {
                cp = UTF16.charAt(text, i);
                if (!android.icu.lang.UCharacter.isUnicodeIdentifierPart(cp)) {
                    break;
                }
            }
            logln("TokenSymbolTable: parse \"" + text + "\" from " +
                    start + " to " + i +
                    " => \"" + text.substring(start,i) + "\"");
            pos.setIndex(i);
            return text.substring(start,i);
        }
    }

    @Test
    public void TestSurrogate() {
        String DATA[] = {
                // These should all behave identically
                "[abc\\uD800\\uDC00]",
                "[abc\uD800\uDC00]",
                "[abc\\U00010000]",
        };
        for (int i=0; i<DATA.length; ++i) {
            logln("Test pattern " + i + " :" + Utility.escape(DATA[i]));
            UnicodeSet set = new UnicodeSet(DATA[i]);
            expectContainment(set,
                    CharsToUnicodeString("abc\\U00010000"),
                    "\uD800;\uDC00"); // split apart surrogate-pair
            if (set.size() != 4) {
                errln(Utility.escape("FAIL: " + DATA[i] + ".size() == " + 
                        set.size() + ", expected 4"));
            }
        }
    }

    @Test
    public void TestContains() {
        int limit = 256; // combinations to test
        for (int i = 0; i < limit; ++i) {
            logln("Trying: " + i);
            UnicodeSet x = bitsToSet(i);
            for (int j = 0; j < limit; ++j) {
                UnicodeSet y = bitsToSet(j);
                boolean containsNone = (i & j) == 0;
                boolean containsAll = (i & j) == j;
                boolean equals = i == j;
                if (containsNone != x.containsNone(y)) {
                    x.containsNone(y); // repeat for debugging
                    errln("FAILED: " + x +  " containsSome " + y);
                }
                if (containsAll != x.containsAll(y)) {
                    x.containsAll(y); // repeat for debugging
                    errln("FAILED: " + x +  " containsAll " + y);
                }
                if (equals != x.equals(y)) {
                    x.equals(y); // repeat for debugging
                    errln("FAILED: " + x +  " equals " + y);
                }
            }
        }
    }

    void _testComplement(int a) {
        UnicodeSet x = bitsToSet(a);
        UnicodeSet z = bitsToSet(a);
        z.complement();
        int c = setToBits(z);
        if (c != (~a)) {
            errln("FAILED: add: ~" + x +  " != " + z);
            errln("FAILED: add: ~" + a + " != " + c);
        }
        checkCanonicalRep(z, "complement " + a);
    }

    void _testAdd(int a, int b) {
        UnicodeSet x = bitsToSet(a);
        UnicodeSet y = bitsToSet(b);
        UnicodeSet z = bitsToSet(a);
        z.addAll(y);
        int c = setToBits(z);
        if (c != (a | b)) {
            errln(Utility.escape("FAILED: add: " + x + " | " + y + " != " + z));
            errln("FAILED: add: " + a + " | " + b + " != " + c);
        }
        checkCanonicalRep(z, "add " + a + "," + b);
    }

    void _testRetain(int a, int b) {
        UnicodeSet x = bitsToSet(a);
        UnicodeSet y = bitsToSet(b);
        UnicodeSet z = bitsToSet(a);
        z.retainAll(y);
        int c = setToBits(z);
        if (c != (a & b)) {
            errln("FAILED: retain: " + x + " & " + y + " != " + z);
            errln("FAILED: retain: " + a + " & " + b + " != " + c);
        }
        checkCanonicalRep(z, "retain " + a + "," + b);
    }

    void _testRemove(int a, int b) {
        UnicodeSet x = bitsToSet(a);
        UnicodeSet y = bitsToSet(b);
        UnicodeSet z = bitsToSet(a);
        z.removeAll(y);
        int c = setToBits(z);
        if (c != (a &~ b)) {
            errln("FAILED: remove: " + x + " &~ " + y + " != " + z);
            errln("FAILED: remove: " + a + " &~ " + b + " != " + c);
        }
        checkCanonicalRep(z, "remove " + a + "," + b);
    }

    void _testXor(int a, int b) {
        UnicodeSet x = bitsToSet(a);
        UnicodeSet y = bitsToSet(b);
        UnicodeSet z = bitsToSet(a);
        z.complementAll(y);
        int c = setToBits(z);
        if (c != (a ^ b)) {
            errln("FAILED: complement: " + x + " ^ " + y + " != " + z);
            errln("FAILED: complement: " + a + " ^ " + b + " != " + c);
        }
        checkCanonicalRep(z, "complement " + a + "," + b);
    }

    /**
     * Check that ranges are monotonically increasing and non-
     * overlapping.
     */
    void checkCanonicalRep(UnicodeSet set, String msg) {
        int n = set.getRangeCount();
        if (n < 0) {
            errln("FAIL result of " + msg +
                    ": range count should be >= 0 but is " +
                    n + " for " + Utility.escape(set.toString()));
            return;
        }
        int last = 0;
        for (int i=0; i<n; ++i) {
            int start = set.getRangeStart(i);
            int end = set.getRangeEnd(i);
            if (start > end) {
                errln("FAIL result of " + msg +
                        ": range " + (i+1) +
                        " start > end: " + start + ", " + end +
                        " for " + Utility.escape(set.toString()));
            }
            if (i > 0 && start <= last) {
                errln("FAIL result of " + msg +
                        ": range " + (i+1) +
                        " overlaps previous range: " + start + ", " + end +
                        " for " + Utility.escape(set.toString()));
            }
            last = end;
        }
    }

    /**
     * Convert a bitmask to a UnicodeSet.
     */
    UnicodeSet bitsToSet(int a) {
        UnicodeSet result = new UnicodeSet();
        for (int i = 0; i < 32; ++i) {
            if ((a & (1<<i)) != 0) {
                result.add((char)i,(char)i);
            }
        }

        return result;
    }

    /**
     * Convert a UnicodeSet to a bitmask.  Only the characters
     * U+0000 to U+0020 are represented in the bitmask.
     */
    static int setToBits(UnicodeSet x) {
        int result = 0;
        for (int i = 0; i < 32; ++i) {
            if (x.contains((char)i)) {
                result |= (1<<i);
            }
        }
        return result;
    }

    /**
     * Return the representation of an inversion list based UnicodeSet
     * as a pairs list.  Ranges are listed in ascending Unicode order.
     * For example, the set [a-zA-M3] is represented as "33AMaz".
     */
    static String getPairs(UnicodeSet set) {
        StringBuffer pairs = new StringBuffer();
        for (int i=0; i<set.getRangeCount(); ++i) {
            int start = set.getRangeStart(i);
            int end = set.getRangeEnd(i);
            if (end > 0xFFFF) {
                end = 0xFFFF;
                i = set.getRangeCount(); // Should be unnecessary
            }
            pairs.append((char)start).append((char)end);
        }
        return pairs.toString();
    }

    /**
     * Test function. Make sure that the sets have the right relation
     */

    void expectRelation(Object relationObj, Object set1Obj, Object set2Obj, String message) {
        int relation = ((Integer) relationObj).intValue();
        UnicodeSet set1 = (UnicodeSet) set1Obj;
        UnicodeSet set2 = (UnicodeSet) set2Obj;

        // by-the-by, check the iterator
        checkRoundTrip(set1);
        checkRoundTrip(set2);

        boolean contains = set1.containsAll(set2);
        boolean isContained = set2.containsAll(set1);
        boolean disjoint = set1.containsNone(set2);
        boolean equals = set1.equals(set2);

        UnicodeSet intersection = new UnicodeSet(set1).retainAll(set2);
        UnicodeSet minus12 = new UnicodeSet(set1).removeAll(set2);
        UnicodeSet minus21 = new UnicodeSet(set2).removeAll(set1);

        // test basic properties

        if (contains != (intersection.size() == set2.size())) {
            errln("FAIL contains1" + set1.toPattern(true) + ", " + set2.toPattern(true));
        }

        if (contains != (intersection.equals(set2))) {
            errln("FAIL contains2" + set1.toPattern(true) + ", " + set2.toPattern(true));
        }

        if (isContained != (intersection.size() == set1.size())) {
            errln("FAIL isContained1" + set1.toPattern(true) + ", " + set2.toPattern(true));
        }

        if (isContained != (intersection.equals(set1))) {
            errln("FAIL isContained2" + set1.toPattern(true) + ", " + set2.toPattern(true));
        }

        if ((contains && isContained) != equals) {
            errln("FAIL equals" + set1.toPattern(true) + ", " + set2.toPattern(true));
        }

        if (disjoint != (intersection.size() == 0)) {
            errln("FAIL disjoint" + set1.toPattern(true) + ", " + set2.toPattern(true));
        }

        // Now see if the expected relation is true
        int status = (minus12.size() != 0 ? 4 : 0)
                | (intersection.size() != 0 ? 2 : 0)
                | (minus21.size() != 0 ? 1 : 0);

        if (status != relation) {
            errln("FAIL relation incorrect" + message
                    + "; desired = " + RELATION_NAME[relation]
                            + "; found = " + RELATION_NAME[status]
                                    + "; set1 = " + set1.toPattern(true)
                                    + "; set2 = " + set2.toPattern(true)
                    );
        }
    }

    /**
     * Basic consistency check for a few items.
     * That the iterator works, and that we can create a pattern and
     * get the same thing back
     */

    void checkRoundTrip(UnicodeSet s) {
        String pat = s.toPattern(false);
        UnicodeSet t = copyWithIterator(s, false);
        checkEqual(s, t, "iterator roundtrip");

        t = copyWithIterator(s, true); // try range
        checkEqual(s, t, "iterator roundtrip");

        t = new UnicodeSet(pat);
        checkEqual(s, t, "toPattern(false)");

        pat = s.toPattern(true);
        t = new UnicodeSet(pat);
        checkEqual(s, t, "toPattern(true)");
    }

    UnicodeSet copyWithIterator(UnicodeSet s, boolean withRange) {
        UnicodeSet t = new UnicodeSet();
        UnicodeSetIterator it = new UnicodeSetIterator(s);
        if (withRange) {
            while (it.nextRange()) {
                if (it.codepoint == UnicodeSetIterator.IS_STRING) {
                    t.add(it.string);
                } else {
                    t.add(it.codepoint, it.codepointEnd);
                }
            }
        } else {
            while (it.next()) {
                if (it.codepoint == UnicodeSetIterator.IS_STRING) {
                    t.add(it.string);
                } else {
                    t.add(it.codepoint);
                }
            }
        }
        return t;
    }

    boolean checkEqual(UnicodeSet s, UnicodeSet t, String message) {
        if (!s.equals(t)) {
            errln("FAIL " + message
                    + "; source = " + s.toPattern(true)
                    + "; result = " + t.toPattern(true)
                    );
            return false;
        }
        return true;
    }

    void expectEqual(String name, String pat1, String pat2) {
        UnicodeSet set1, set2;
        try {
            set1 = new UnicodeSet(pat1);
            set2 = new UnicodeSet(pat2);
        } catch (IllegalArgumentException e) {
            errln("FAIL: Couldn't create UnicodeSet from pattern for \"" + name + "\": " + e.getMessage());
            return;
        }
        if(!set1.equals(set2)) {
            errln("FAIL: Sets built from patterns differ for \"" + name + "\"");
        }
    }

    /**
     * Expect the given set to contain the characters in charsIn and
     * to not contain those in charsOut.
     */
    void expectContainment(String pat, String charsIn, String charsOut) {
        UnicodeSet set;
        try {
            set = new UnicodeSet(pat);
        } catch (IllegalArgumentException e) {
            errln("FAIL: Couldn't create UnicodeSet from pattern \"" +
                    pat + "\": " + e.getMessage());
            return;
        }
        expectContainment(set, charsIn, charsOut);
    }

    /**
     * Expect the given set to contain the characters in charsIn and
     * to not contain those in charsOut.
     */
    void expectContainment(UnicodeSet set, String charsIn, String charsOut) {
        StringBuffer bad = new StringBuffer();
        if (charsIn != null) {
            charsIn = Utility.unescape(charsIn);
            for (int i=0; i<charsIn.length(); ) {
                int c = UTF16.charAt(charsIn,i);
                i += UTF16.getCharCount(c);
                if (!set.contains(c)) {
                    UTF16.append(bad,c);
                }
            }
            if (bad.length() > 0) {
                errln(Utility.escape("FAIL: set " + set + " does not contain " + bad +
                        ", expected containment of " + charsIn));
            } else {
                logln(Utility.escape("Ok: set " + set + " contains " + charsIn));
            }
        }
        if (charsOut != null) {
            charsOut = Utility.unescape(charsOut);
            bad.setLength(0);
            for (int i=0; i<charsOut.length(); ) {
                int c = UTF16.charAt(charsOut,i);
                i += UTF16.getCharCount(c);
                if (set.contains(c)) {
                    UTF16.append(bad, c);
                }
            }
            if (bad.length() > 0) {
                errln(Utility.escape("FAIL: set " + set + " contains " + bad +
                        ", expected non-containment of " + charsOut));
            } else {
                logln(Utility.escape("Ok: set " + set + " does not contain " + charsOut));
            }
        }
    }

    void expectPattern(UnicodeSet set,
            String pattern,
            String expectedPairs) {
        set.applyPattern(pattern);
        if (!getPairs(set).equals(expectedPairs)) {
            errln("FAIL: applyPattern(\"" + pattern +
                    "\") => pairs \"" +
                    Utility.escape(getPairs(set)) + "\", expected \"" +
                    Utility.escape(expectedPairs) + "\"");
        } else {
            logln("Ok:   applyPattern(\"" + pattern +
                    "\") => pairs \"" +
                    Utility.escape(getPairs(set)) + "\"");
        }
    }

    void expectToPattern(UnicodeSet set,
            String expPat,
            String[] expStrings) {
        String pat = set.toPattern(true);
        if (pat.equals(expPat)) {
            logln("Ok:   toPattern() => \"" + pat + "\"");
        } else {
            errln("FAIL: toPattern() => \"" + pat + "\", expected \"" + expPat + "\"");
            return;
        }
        if (expStrings == null) {
            return;
        }
        boolean in = true;
        for (int i=0; i<expStrings.length; ++i) {
            if (expStrings[i] == NOT) { // sic; pointer comparison
                in = false;
                continue;
            }
            boolean contained = set.contains(expStrings[i]);
            if (contained == in) {
                logln("Ok: " + expPat + 
                        (contained ? " contains {" : " does not contain {") +
                        Utility.escape(expStrings[i]) + "}");
            } else {
                errln("FAIL: " + expPat + 
                        (contained ? " contains {" : " does not contain {") +
                        Utility.escape(expStrings[i]) + "}");
            }
        }
    }

    void expectPairs(UnicodeSet set, String expectedPairs) {
        if (!getPairs(set).equals(expectedPairs)) {
            errln("FAIL: Expected pair list \"" +
                    Utility.escape(expectedPairs) + "\", got \"" +
                    Utility.escape(getPairs(set)) + "\"");
        }
    }
    static final String CharsToUnicodeString(String s) {
        return Utility.unescape(s);
    }

    /* Test the method public UnicodeSet getSet() */
    @Test
    public void TestGetSet() {
        UnicodeSetIterator us = new UnicodeSetIterator();
        try {
            us.getSet();
        } catch (Exception e) {
            errln("UnicodeSetIterator.getSet() was not suppose to given an " + "an exception.");
        }
    }

    /* Tests the method public UnicodeSet add(Collection<?> source) */
    @Test
    public void TestAddCollection() {
        UnicodeSet us = new UnicodeSet();
        Collection<?> s = null;
        try {
            us.add(s);
            errln("UnicodeSet.add(Collection<?>) was suppose to return an exception for a null parameter.");
        } catch (Exception e) {
        }
    }

    @Test
    public void TestConstants() {
        assertEquals("Empty", new UnicodeSet(), UnicodeSet.EMPTY);
        assertEquals("All", new UnicodeSet(0,0x10FFFF), UnicodeSet.ALL_CODE_POINTS);
    }

    @Test
    public void TestIteration() {
        UnicodeSet us1 = new UnicodeSet("[abcM{xy}]");
        assertEquals("", "M, a-c", CollectionUtilities.join(us1.ranges(), ", "));

        // Sample code
        for (@SuppressWarnings("unused") EntryRange range : us1.ranges()) { 
            // do something with code points between range.codepointEnd and range.codepointEnd; 
        }
        for (@SuppressWarnings("unused") String s : us1.strings()) { 
            // do something with each string;
        }

        String[] tests = {
                "[M-Qzab{XY}{ZW}]",
                "[]",
                "[a]",
                "[a-c]",
                "[{XY}]",
        };
        for (String test : tests) {
            UnicodeSet us = new UnicodeSet(test);
            UnicodeSetIterator it = new UnicodeSetIterator(us);
            for (EntryRange range : us.ranges()) {
                final String title = range.toString();
                logln(title);
                it.nextRange();
                assertEquals(title, it.codepoint, range.codepoint);
                assertEquals(title, it.codepointEnd, range.codepointEnd);
            }
            for (String s : us.strings()) {
                it.nextRange();
                assertEquals("strings", it.string, s);
            }
            assertFalse("", it.next());
        }
    }

    @Test
    public void TestReplaceAndDelete() {
        UnicodeSetSpanner m;

        m = new UnicodeSetSpanner(new UnicodeSet("[._]"));
        assertEquals("", "abc", m.deleteFrom("_._a_._b_._c_._"));        
        assertEquals("", "_.__.__.__._", m.deleteFrom("_._a_._b_._c_._", SpanCondition.NOT_CONTAINED));

        assertEquals("", "a_._b_._c", m.trim("_._a_._b_._c_._"));
        assertEquals("", "a_._b_._c_._", m.trim("_._a_._b_._c_._", TrimOption.LEADING));
        assertEquals("", "_._a_._b_._c", m.trim("_._a_._b_._c_._", TrimOption.TRAILING));

        assertEquals("", "a??b??c", m.replaceFrom("a_._b_._c", "??", CountMethod.WHOLE_SPAN));
        assertEquals("", "a??b??c", m.replaceFrom(m.trim("_._a_._b_._c_._"), "??", CountMethod.WHOLE_SPAN));
        assertEquals("", "XYXYXYaXYXYXYbXYXYXYcXYXYXY", m.replaceFrom("_._a_._b_._c_._", "XY"));
        assertEquals("", "XYaXYbXYcXY", m.replaceFrom("_._a_._b_._c_._", "XY", CountMethod.WHOLE_SPAN));

        m = new UnicodeSetSpanner(new UnicodeSet("\\p{uppercase}"));
        assertEquals("", "TQBF", m.deleteFrom("The Quick Brown Fox.", SpanCondition.NOT_CONTAINED));

        m = new UnicodeSetSpanner(m.getUnicodeSet().addAll(new UnicodeSet("\\p{lowercase}")));
        assertEquals("", "TheQuickBrownFox", m.deleteFrom("The Quick Brown Fox.", SpanCondition.NOT_CONTAINED));

        m = new UnicodeSetSpanner(new UnicodeSet("[{ab}]"));
        assertEquals("", "XXc acb", m.replaceFrom("ababc acb", "X"));
        assertEquals("", "Xc acb", m.replaceFrom("ababc acb", "X", CountMethod.WHOLE_SPAN));
        assertEquals("", "ababX", m.replaceFrom("ababc acb", "X", CountMethod.WHOLE_SPAN, SpanCondition.NOT_CONTAINED));
    }

    @Test
    public void TestCodePoints() {
        // test supplemental code points and strings clusters
        checkCodePoints("x\u0308", "z\u0308", CountMethod.MIN_ELEMENTS, SpanCondition.SIMPLE, null, 1);
        checkCodePoints("𣿡", "𣿢", CountMethod.MIN_ELEMENTS, SpanCondition.SIMPLE, null, 1);
        checkCodePoints("👦", "👧", CountMethod.MIN_ELEMENTS, SpanCondition.SIMPLE, null, 1);
    }

    private void checkCodePoints(String a, String b, CountMethod quantifier, SpanCondition spanCondition, 
            String expectedReplaced, int expectedCount) {
        final String ab = a+b;
        UnicodeSetSpanner m = new UnicodeSetSpanner(new UnicodeSet("[{" + a + "}]"));
        assertEquals("new UnicodeSetSpanner(\"[{" + a + "}]\").countIn(\"" + ab + "\")", 
                expectedCount,
                callCountIn(m, ab, quantifier, spanCondition)
                );

        if (expectedReplaced == null) {
            expectedReplaced = "-" + b;
        }
        assertEquals("new UnicodeSetSpanner(\"[{" + a + "}]\").replaceFrom(\"" + ab + "\", \"-\")", 
                expectedReplaced, m.replaceFrom(ab, "-", quantifier));
    }

    @Test
    public void TestCountIn() {
        UnicodeSetSpanner m = new UnicodeSetSpanner(new UnicodeSet("[ab]"));
        checkCountIn(m, CountMethod.MIN_ELEMENTS, SpanCondition.SIMPLE, "abc", 2);
        checkCountIn(m, CountMethod.WHOLE_SPAN, SpanCondition.SIMPLE, "abc", 1);
        checkCountIn(m, CountMethod.MIN_ELEMENTS, SpanCondition.NOT_CONTAINED, "acccb", 3);
    }

    public void checkCountIn(UnicodeSetSpanner m, CountMethod countMethod, SpanCondition spanCondition, String target, int expected) {
        final String message = "countIn " + countMethod + ", " + spanCondition;
        assertEquals(message, callCountIn(m, target, countMethod, spanCondition), expected);
    }

    public int callCountIn(UnicodeSetSpanner m, final String ab, CountMethod countMethod, SpanCondition spanCondition) {
        return spanCondition != SpanCondition.SIMPLE ? m.countIn(ab, countMethod, spanCondition)
                : countMethod != CountMethod.MIN_ELEMENTS ? m.countIn(ab, countMethod)
                        : m.countIn(ab);
    }

    @Test
    public void testForSpanGaps() {
        String[] items = {"a", "b", "c", "{ab}", "{bc}", "{cd}", "{abc}", "{bcd}"};
        final int limit = 1<<items.length;
        // build long string for testing
        StringBuilder longBuffer = new StringBuilder();
        for (int i = 1; i < limit; ++i) {
            longBuffer.append("x");
            longBuffer.append(getCombinations(items, i));
        }
        String longString = longBuffer.toString();
        longString = longString.replace("{","").replace("}","");

        long start = System.nanoTime();
        for (int i = 1; i < limit; ++i) {
            UnicodeSet us = new UnicodeSet("[" + getCombinations(items, i) + "]");
            int problemFound = checkSpan(longString, us, SpanCondition.SIMPLE);
            if (problemFound >= 0) {
                assertEquals("Testing " + longString + ", found gap at", -1, problemFound);
                break;
            }
        }
        long end = System.nanoTime();
        logln("Time for SIMPLE   :\t" + (end-start));
        start = System.nanoTime();
        for (int i = 1; i < limit; ++i) {
            UnicodeSet us = new UnicodeSet("[" + getCombinations(items, i) + "]");
            int problemFound = checkSpan(longString, us, SpanCondition.CONTAINED);
            if (problemFound >= 0) {
                assertEquals("Testing " + longString + ", found gap at", -1, problemFound);
                break;
            }
        }
        end = System.nanoTime();
        logln("Time for CONTAINED:\t" + (end-start));
    }

    /**
     * Check that there are no gaps, when we alternate spanning. That is, there
     * should only be a zero length span at the very start.
     * @param longString
     * @param us
     * @param simple
     */
    private int checkSpan(String longString, UnicodeSet us, SpanCondition spanCondition) {
        int start = 0;
        while (start < longString.length()) {
            int limit = us.span(longString, start, spanCondition);
            if (limit == longString.length()) {
                break;
            } else if (limit == start && start != 0) {
                return start;
            }
            start = limit;
            limit = us.span(longString, start, SpanCondition.NOT_CONTAINED);
            if (limit == start) {
                return start;
            }
            start = limit;
        }
        return -1; // all ok
    }

    private String getCombinations(String[] items, int bitset) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; bitset != 0; ++i) {
            int other = bitset & (1 << i);
            if (other != 0) {
                bitset ^= other;
                result.append(items[i]);
            }
        }
        return result.toString();
    }

    @Test
    public void TestCharSequenceArgs() {
        // statics
        assertEquals("CharSequence from", new UnicodeSet("[{abc}]"), UnicodeSet.from(new StringBuilder("abc")));
        assertEquals("CharSequence fromAll", new UnicodeSet("[a-c]"), UnicodeSet.fromAll(new StringBuilder("abc")));
        assertEquals("CharSequence compare", 1.0f, Math.signum(UnicodeSet.compare(new StringBuilder("abc"), 0x61)));
        assertEquals("CharSequence compare", -1.0f, Math.signum(UnicodeSet.compare(0x61, new StringBuilder("abc"))));
        assertEquals("CharSequence compare", 0.0f, Math.signum(UnicodeSet.compare(new StringBuilder("a"), 0x61)));
        assertEquals("CharSequence compare", 0.0f, Math.signum(UnicodeSet.compare(0x61, new StringBuilder("a"))));
        assertEquals("CharSequence getSingleCodePoint", 0x1F466, UnicodeSet.getSingleCodePoint(new StringBuilder("👦")));

        // iterables/arrays
        Iterable<StringBuilder> iterable = Arrays.asList(new StringBuilder("A"), new StringBuilder("B"));
        assertEquals("CharSequence containsAll", true, new UnicodeSet("[AB]").containsAll(iterable));
        assertEquals("CharSequence containsAll", false, new UnicodeSet("[a-cA]").containsAll(iterable));
        assertEquals("CharSequence containsNone", true, new UnicodeSet("[a-c]").containsNone(iterable) );
        assertEquals("CharSequence containsNone", false, new UnicodeSet("[a-cA]").containsNone(iterable) );
        assertEquals("CharSequence containsSome", true, new UnicodeSet("[a-cA]").containsSome(iterable) );
        assertEquals("CharSequence containsSome", false, new UnicodeSet("[a-c]").containsSome(iterable) );
        assertEquals("CharSequence addAll", new UnicodeSet("[a-cAB]"), new UnicodeSet("[a-cA]").addAll(new StringBuilder("A"), new StringBuilder("B")) );
        assertEquals("CharSequence removeAll", new UnicodeSet("[a-c]"), new UnicodeSet("[a-cA]").removeAll( iterable) );
        assertEquals("CharSequence retainAll", new UnicodeSet("[A]"), new UnicodeSet("[a-cA]").retainAll( iterable) );

        // UnicodeSet results
        assertEquals("CharSequence add", new UnicodeSet("[Aa-c{abc}{qr}]"), new UnicodeSet("[a-cA{qr}]").add(new StringBuilder("abc")) );
        assertEquals("CharSequence retain", new UnicodeSet("[{abc}]"), new UnicodeSet("[a-cA{abc}{qr}]").retain(new StringBuilder("abc")) );
        assertEquals("CharSequence remove", new UnicodeSet("[Aa-c{qr}]"), new UnicodeSet("[a-cA{abc}{qr}]").remove(new StringBuilder("abc")) );
        assertEquals("CharSequence complement", new UnicodeSet("[Aa-c{qr}]"), new UnicodeSet("[a-cA{abc}{qr}]").complement(new StringBuilder("abc")) );
        assertEquals("CharSequence complement", new UnicodeSet("[Aa-c{abc}{qr}]"), new UnicodeSet("[a-cA{qr}]").complement(new StringBuilder("abc")) );

        assertEquals("CharSequence addAll", new UnicodeSet("[a-cABC]"), new UnicodeSet("[a-cA]").addAll(new StringBuilder("ABC")) );
        assertEquals("CharSequence retainAll", new UnicodeSet("[a-c]"), new UnicodeSet("[a-cA]").retainAll(new StringBuilder("abcB")) );
        assertEquals("CharSequence removeAll", new UnicodeSet("[Aab]"), new UnicodeSet("[a-cA]").removeAll(new StringBuilder("cC")) );
        assertEquals("CharSequence complementAll", new UnicodeSet("[ABbc]"), new UnicodeSet("[a-cA]").complementAll(new StringBuilder("aB")) );

        // containment
        assertEquals("CharSequence contains", true, new UnicodeSet("[a-cA{ab}]"). contains(new StringBuilder("ab")) ); 
        assertEquals("CharSequence containsNone", false, new UnicodeSet("[a-cA]"). containsNone(new StringBuilder("ab"))  );
        assertEquals("CharSequence containsSome", true, new UnicodeSet("[a-cA{ab}]"). containsSome(new StringBuilder("ab"))  );

        // spanning
        assertEquals("CharSequence span", 3, new UnicodeSet("[a-cA]"). span(new StringBuilder("abc"), SpanCondition.SIMPLE) );
        assertEquals("CharSequence span", 3, new UnicodeSet("[a-cA]"). span(new StringBuilder("abc"), 1, SpanCondition.SIMPLE) );
        assertEquals("CharSequence spanBack", 0, new UnicodeSet("[a-cA]"). spanBack(new StringBuilder("abc"), SpanCondition.SIMPLE) );
        assertEquals("CharSequence spanBack", 0, new UnicodeSet("[a-cA]"). spanBack(new StringBuilder("abc"), 1, SpanCondition.SIMPLE) );

        // internal
        OutputInt outCount = new OutputInt();
        assertEquals("CharSequence matchesAt", 2, new UnicodeSet("[a-cA]"). matchesAt(new StringBuilder("abc"), 1) );
        assertEquals("CharSequence spanAndCount", 3, new UnicodeSet("[a-cA]"). spanAndCount(new StringBuilder("abc"), 1, SpanCondition.SIMPLE, outCount ) );
        assertEquals("CharSequence findIn", 3, new UnicodeSet("[a-cA]"). findIn(new StringBuilder("abc"), 1, true) );
        assertEquals("CharSequence findLastIn", -1, new UnicodeSet("[a-cA]"). findLastIn(new StringBuilder("abc"), 1, true) );
        assertEquals("CharSequence add", "c", new UnicodeSet("[abA]"). stripFrom(new StringBuilder("abc"), true));
    }

    @Test
    public void TestAStringRange() {
        String[][] tests = {
                {"[{ax}-{bz}]", "[{ax}{ay}{az}{bx}{by}{bz}]"},
                {"[{a}-{c}]", "[a-c]"},
                //{"[a-{c}]", "[a-c]"}, // don't handle these yet: enable once we do
                //{"[{a}-c]", "[a-c]"}, // don't handle these yet: enable once we do
                {"[{ax}-{by}-{cz}]", "Error: '-' not after char, string, or set at \"[{ax}-{by}-{|cz}]\""},
                {"[{a}-{bz}]", "Error: Range must have equal-length strings at \"[{a}-{bz}|]\""},
                {"[{ax}-{b}]", "Error: Range must have equal-length strings at \"[{ax}-{b}|]\""},
                {"[{ax}-bz]", "Error: Invalid range at \"[{ax}-b|z]\""},
                {"[ax-{bz}]", "Error: Range must have 2 valid strings at \"[ax-{bz}|]\""},
                {"[{bx}-{az}]", "Error: Range must have xᵢ ≤ yᵢ for each index i at \"[{bx}-{az}|]\""},
        };
        int i = 0;
        for (String[] test : tests) {
            String expected = test[1];
            if (test[1].startsWith("[")) {
                expected = new UnicodeSet(expected).toPattern(false);
            }
            String actual;
            try {
                actual = new UnicodeSet(test[0]).toPattern(false);
            } catch (Exception e) {
                actual = e.getMessage();
            }
            assertEquals("StringRange " + i, expected, actual);
            ++i;
        }
    }

    @Test
    public void testAddAll_CharacterSequences() {
        UnicodeSet unicodeSet = new UnicodeSet();
        unicodeSet.addAll("a", "b");
        assertEquals("Wrong UnicodeSet pattern", "[ab]", unicodeSet.toPattern(true));
        unicodeSet.addAll("b", "x");
        assertEquals("Wrong UnicodeSet pattern", "[abx]", unicodeSet.toPattern(true));
        unicodeSet.addAll(new CharSequence[]{new StringBuilder("foo"), new StringBuffer("bar")});
        assertEquals("Wrong UnicodeSet pattern", "[abx{bar}{foo}]", unicodeSet.toPattern(true));
    }

    @Test
    public void testCompareTo() {
        Set<String> test_set = Collections.emptySet();
        assertEquals("UnicodeSet not empty", 0, UnicodeSet.EMPTY.compareTo(test_set));
        assertEquals("UnicodeSet comparison wrong",
                0, UnicodeSet.fromAll("a").compareTo(Collections.singleton("a")));

        // Longer is bigger
        assertTrue("UnicodeSet is empty", 
                UnicodeSet.ALL_CODE_POINTS.compareTo(test_set) > 0);
        assertTrue("UnicodeSet not empty",
                UnicodeSet.EMPTY.compareTo(Collections.singleton("a")) < 0);

        // Equal length compares on first difference.
        assertTrue("UnicodeSet comparison wrong",
                UnicodeSet.fromAll("a").compareTo(Collections.singleton("b")) < 0);
        assertTrue("UnicodeSet comparison wrong",
                UnicodeSet.fromAll("ab").compareTo(Arrays.asList("a", "c")) < 0);
        assertTrue("UnicodeSet comparison wrong",
                UnicodeSet.fromAll("b").compareTo(Collections.singleton("a")) > 0);
    }
}
