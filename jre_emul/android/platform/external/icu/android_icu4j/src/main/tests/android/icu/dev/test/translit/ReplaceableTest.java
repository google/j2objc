/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.translit;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.Utility;
import android.icu.text.Replaceable;
import android.icu.text.ReplaceableString;
import android.icu.text.Transliterator;

/**
 * @test
 * @summary Round trip test of Transliterator
 */
public class ReplaceableTest extends TestFmwk {
    @org.junit.Test
    public void Test() {
        check("Lower", "ABCD", "1234");
        check("Upper", "abcd\u00DF", "123455"); // must map 00DF to SS
        check("Title", "aBCD", "1234");
        check("NFC", "A\u0300E\u0300", "13");
        check("NFD", "\u00C0\u00C8", "1122");
        check("*(x) > A $1 B", "wxy", "11223");
        check("*(x)(y) > A $2 B $1 C $2 D", "wxyz", "113322334");
        check("*(x)(y)(z) > A $3 B $2 C $1 D", "wxyzu", "114433225");
        // TODO Revisit the following in 2.6 or later.
        check("*x > a", "xyz", "223"); // expect "123"?
        check("*x > a", "wxy", "113"); // expect "123"?
        check("*x > a", "\uFFFFxy", "_33"); // expect "_23"?
        check("*(x) > A $1 B", "\uFFFFxy", "__223");
    }
    
    void check(String transliteratorName, String test, String shouldProduceStyles) {
        TestReplaceable tr = new TestReplaceable(test, null);
        String original = tr.toString();
        
        Transliterator t;
        if (transliteratorName.startsWith("*")) {
            transliteratorName = transliteratorName.substring(1);
            t = Transliterator.createFromRules("test", transliteratorName,
                                               Transliterator.FORWARD);
        } else {
            t = Transliterator.getInstance(transliteratorName);
        }
        t.transliterate(tr);
        String newStyles = tr.getStyles();
        if (!newStyles.equals(shouldProduceStyles)) {
            errln("FAIL Styles: " + transliteratorName + " ( "
                + original + " ) => " + tr.toString() + "; should be {" + shouldProduceStyles + "}!");
        } else {
            logln("OK: " + transliteratorName + " ( " + original + " ) => " + tr.toString());
        }
        
        if (!tr.hasMetaData() || tr.chars.hasMetaData() 
            || tr.styles.hasMetaData()) {
            errln("Fail hasMetaData()");
        }
    }
    

    /**
     * This is a test class that simulates styled text.
     * It associates a style number (0..65535) with each character,
     * and maintains that style in the normal fashion:
     * When setting text from raw string or characters,<br>
     * Set the styles to the style of the first character replaced.<br>
     * If no characters are replaced, use the style of the previous character.<br>
     * If at start, use the following character<br>
     * Otherwise use NO_STYLE.
     */
    static class TestReplaceable implements Replaceable {
        ReplaceableString chars;
        ReplaceableString styles;
        
        static final char NO_STYLE = '_';

        static final char NO_STYLE_MARK = 0xFFFF;
        
        TestReplaceable (String text, String styles) {
            chars = new ReplaceableString(text);
            StringBuffer s = new StringBuffer();
            for (int i = 0; i < text.length(); ++i) {
                if (styles != null && i < styles.length()) {
                    s.append(styles.charAt(i));
                } else {
                    if (text.charAt(i) == NO_STYLE_MARK) {
                        s.append(NO_STYLE);
                    } else {
                        s.append((char) (i + '1'));
                    }
                }
            }
            this.styles = new ReplaceableString(s.toString());
        }
        
        public String getStyles() {
            return styles.toString();
        }
        
        public String toString() {
            return chars.toString() + "{" + styles.toString() + "}";
        }

        public String substring(int start, int limit) {
            return chars.substring(start, limit);
        }

        public int length() {
            return chars.length();
        }

        public char charAt(int offset) {
            return chars.charAt(offset);
        }

        public int char32At(int offset) {
            return chars.char32At(offset);
        }

        public void getChars(int srcStart, int srcLimit, char dst[], int dstStart) {
            chars.getChars(srcStart, srcLimit, dst, dstStart);
        }

        public void replace(int start, int limit, String text) {
            if (substring(start,limit).equals(text)) return; // NO ACTION!
            if (DEBUG) System.out.print(Utility.escape(toString() + " -> replace(" + start +
                                            "," + limit + "," + text) + ") -> ");
            chars.replace(start, limit, text);
            fixStyles(start, limit, text.length());
            if (DEBUG) System.out.println(Utility.escape(toString()));
        }
        
        public void replace(int start, int limit, char[] charArray,
                            int charsStart, int charsLen) {
            if (substring(start,limit).equals(new String(charArray, charsStart, charsLen-charsStart))) return; // NO ACTION!
            this.chars.replace(start, limit, charArray, charsStart, charsLen);
            fixStyles(start, limit, charsLen);
        }

        void fixStyles(int start, int limit, int newLen) {
            char newStyle = NO_STYLE;
            if (start != limit && styles.charAt(start) != NO_STYLE) {
                newStyle = styles.charAt(start);
            } else if (start > 0 && charAt(start-1) != NO_STYLE_MARK) {
                newStyle = styles.charAt(start-1);
            } else if (limit < styles.length()) {
                newStyle = styles.charAt(limit);
            }
            // dumb implementation for now.
            StringBuffer s = new StringBuffer();
            for (int i = 0; i < newLen; ++i) {
                // this doesn't really handle an embedded NO_STYLE_MARK
                // in the middle of a long run of characters right -- but
                // that case shouldn't happen anyway
                if (charAt(start+i) == NO_STYLE_MARK) {
                    s.append(NO_STYLE);
                } else {
                    s.append(newStyle);
                }
            }
            styles.replace(start, limit, s.toString());
        }

        public void copy(int start, int limit, int dest) {
            chars.copy(start, limit, dest);
            styles.copy(start, limit, dest);
        }
        
        public boolean hasMetaData() {
            return true;
        }

        static final boolean DEBUG = false;
    }
    
    @org.junit.Test
    public void Test5789() {
        String rules =
            "IETR > IET | \\' R; # (1) do split ietr between t and r\r\n" +
            "I[EH] > I; # (2) friedrich";
        Transliterator trans = Transliterator.createFromRules("foo", rules, Transliterator.FORWARD);
        String result =  trans.transliterate("BLENKDIETRICH");
        assertEquals("Rule breakage", "BLENKDIET'RICH", result);
    }
}
