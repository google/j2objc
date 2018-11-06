/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2012-2013, Google, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.format;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.ListFormatter;
import android.icu.util.ULocale;

public class ListFormatterTest extends TestFmwk {
    String[] HardcodedTestData = {
            "",
            "A",
            "A and B",
            "A; B, and C",
            "A; B, C, and D",
            "A; B, C, D, and E"
    };

    @Test
    public void TestBasic() {
        ListFormatter formatter = new ListFormatter("{0} and {1}", "{0}; {1}", "{0}, {1}", "{0}, and {1}");
        checkData(formatter, HardcodedTestData);
    }

    String[] EnglishTestData = {
            "",
            "A",
            "A and B",
            "A, B, and C",
            "A, B, C, and D",
            "A, B, C, D, and E"
    };

    @Test
    public void TestEnglish() {
        checkData(ListFormatter.getInstance(ULocale.ENGLISH), EnglishTestData);
        checkData(ListFormatter.getInstance(ULocale.US), EnglishTestData);
        // Redundant tests for code coverage.
        checkData(ListFormatter.getInstance(Locale.ENGLISH), EnglishTestData);
        if (isDefaultLocaleEnglishLike()) {
            checkData(ListFormatter.getInstance(), EnglishTestData);
        }
    }

    String[] JapaneseTestData = {
            "",
            "A",
            "A、B",
            "A、B、C",
            "A、B、C、D",
            "A、B、C、D、E"
    };

    @Test
    public void TestJapanese() {
        checkData(ListFormatter.getInstance(ULocale.JAPANESE), JapaneseTestData);
    }

    String[] outOfOrderTestData = {
            "",
            "A",
            "B after A",
            "C in the last after B after the first A",
            "D in the last after C after B after the first A",
            "E in the last after D after C after B after the first A"
    };
    @Test
    public void TestPatternOutOfOrder() {
        ListFormatter formatter = new ListFormatter("{1} after {0}", "{1} after the first {0}", "{1} after {0}",
                                                    "{1} in the last after {0}");
        checkData(formatter, outOfOrderTestData);
    }

    String[] RootTestData = {
            "",
            "A",
            "A, B",
            "A, B, C",
            "A, B, C, D",
            "A, B, C, D, E"
    };

    @Test
    public void TestSpecial() {
        checkData(ListFormatter.getInstance(ULocale.ROOT), RootTestData);
        if (isDefaultLocaleEnglishLike()) {
          checkData(ListFormatter.getInstance(new ULocale("xxx")), EnglishTestData);
        }
    }

    public void checkData(ListFormatter listFormat, String[] strings) {
        assertEquals("0", strings[0], listFormat.format());
        assertEquals("1", strings[1], listFormat.format("A"));
        assertEquals("2", strings[2], listFormat.format("A", "B"));
        assertEquals("3", strings[3], listFormat.format("A", "B", "C"));
        assertEquals("4", strings[4], listFormat.format("A", "B", "C", "D"));
        assertEquals("5", strings[5], listFormat.format("A", "B", "C", "D", "E"));
    }
    
    @Test
    public void TestFromList() {
        ListFormatter listFormatter = ListFormatter.getInstance(ULocale.ENGLISH);
        ArrayList<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        assertEquals("list", "A, B, and C", listFormatter.format(list));
    }
    
    @Test
    public void TestCreatePatternForNumItems() {
        ListFormatter listFormatter = ListFormatter.getInstance(ULocale.ENGLISH);
        assertEquals(
                "createPatternForNumItems",
                "{0}, {1}, and {2}",
                listFormatter.getPatternForNumItems(3));        
    }
    
    @Test
    public void TestGetPatternForNumItemsException() {
        ListFormatter listFormatter = ListFormatter.getInstance(ULocale.ENGLISH);
        try {
            listFormatter.getPatternForNumItems(0);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException expected) {
            // expected.
        }
    }
    
    @Test
    public void TestGetLocale() {
        assertEquals(
                "getLocale", ULocale.ENGLISH, ListFormatter.getInstance(ULocale.ENGLISH).getLocale());
    }
    
    @Test
    public void Test9946() {
        ListFormatter listFormatter = ListFormatter.getInstance(ULocale.ENGLISH);
        assertEquals("bug 9946", "{0}, {1}, and {2}", listFormatter.format("{0}", "{1}", "{2}"));
    }

    private boolean isDefaultLocaleEnglishLike() {
        ULocale defaultLocale = ULocale.getDefault(ULocale.Category.FORMAT);
        return defaultLocale.equals(ULocale.ENGLISH) || defaultLocale.equals(ULocale.US);
    }
}
