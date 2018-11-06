/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
*/
package android.icu.dev.test.util;

import java.util.Iterator;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.TextTrieMap;

public class TextTrieMapTest extends TestFmwk {

    private static final Integer SUN = new Integer(1);
    private static final Integer MON = new Integer(2);
    private static final Integer TUE = new Integer(3);
    private static final Integer WED = new Integer(4);
    private static final Integer THU = new Integer(5);
    private static final Integer FRI = new Integer(6);
    private static final Integer SAT = new Integer(7);

    private static final Integer FOO = new Integer(-1);
    private static final Integer BAR = new Integer(-2);
    
    private static final Object[][] TESTDATA = {
        {"Sunday", SUN},
        {"Monday", MON},
        {"Tuesday", TUE},
        {"Wednesday", WED},
        {"Thursday", THU},
        {"Friday", FRI},
        {"Saturday", SAT},
        {"Sun", SUN},
        {"Mon", MON},
        {"Tue", TUE},
        {"Wed", WED},
        {"Thu", THU},
        {"Fri", FRI},
        {"Sat", SAT},
        {"S", SUN},
        {"M", MON},
        {"T", TUE},
        {"W", WED},
        {"T", THU},
        {"F", FRI},
        {"S", SAT}
    };

    private static final Object[][] TESTCASES = {
        {"Sunday", SUN, SUN},
        {"sunday", null, SUN},
        {"Mo", MON, MON},
        {"mo", null, MON},
        {"Thursday Friday", THU, THU},
        {"T", new Object[]{TUE, THU}, new Object[]{TUE, THU}},
        {"TEST", new Object[]{TUE, THU}, new Object[]{TUE, THU}},
        {"SUN", new Object[]{SUN, SAT}, SUN},
        {"super", null, SUN},
        {"NO", null, null}
    };

    @Test
    public void TestCaseSensitive() {
        Iterator itr = null;
        TextTrieMap map = new TextTrieMap(false);
        for (int i = 0; i < TESTDATA.length; i++) {
            map.put((String)TESTDATA[i][0], TESTDATA[i][1]);
        }

        logln("Test for get(String)");
        for (int i = 0; i < TESTCASES.length; i++) {
            itr = map.get((String)TESTCASES[i][0]);
            checkResult(itr, TESTCASES[i][1]);
        }

        logln("Test for get(String, int)");
        StringBuffer textBuf = new StringBuffer();
        for (int i = 0; i < TESTCASES.length; i++) {
            textBuf.setLength(0);
            for (int j = 0; j < i; j++) {
                textBuf.append('X');
            }
            textBuf.append(TESTCASES[i][0]);
            itr = map.get(textBuf.toString(), i);
            checkResult(itr, TESTCASES[i][1]);
        }

        // Add duplicated entry
        map.put("Sunday", FOO);
        // Add duplicated entry with different casing
        map.put("sunday", BAR);

        // Make sure the all entries are returned
        itr = map.get("Sunday");
        checkResult(itr, new Object[]{FOO, SUN});
    }

    @Test
    public void TestCaseInsensitive() {
        Iterator itr = null;
        TextTrieMap map = new TextTrieMap(true);
        for (int i = 0; i < TESTDATA.length; i++) {
            map.put((String)TESTDATA[i][0], TESTDATA[i][1]);
        }

        logln("Test for get(String)");
        for (int i = 0; i < TESTCASES.length; i++) {
            itr = map.get((String)TESTCASES[i][0]);
            checkResult(itr, TESTCASES[i][2]);
        }
        
        logln("Test for get(String, int)");
        StringBuffer textBuf = new StringBuffer();
        for (int i = 0; i < TESTCASES.length; i++) {
            textBuf.setLength(0);
            for (int j = 0; j < i; j++) {
                textBuf.append('X');
            }
            textBuf.append(TESTCASES[i][0]);
            itr = map.get(textBuf.toString(), i);
            checkResult(itr, TESTCASES[i][2]);
        }

        // Add duplicated entry
        map.put("Sunday", FOO);
        // Add duplicated entry with different casing
        map.put("sunday", BAR);

        // Make sure the all entries are returned
        itr = map.get("Sunday");
        checkResult(itr, new Object[]{SUN, FOO, BAR});
    }

    private boolean eql(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            if (o1 == null && o2 == null) {
                return true;
            }
            return false;
        }
        return o1.equals(o2);
    }

    private void checkResult(Iterator itr, Object expected) {
        if (itr == null) {
            if (expected != null) {
                errln("FAIL: Empty results - Expected: " + expected);
            }
            return;
        }
        if (expected == null && itr != null) {
            errln("FAIL: Empty result is expected");
            return;
        }

        Object[] exp;
        if (expected instanceof Object[]) {
            exp = (Object[])expected;
        } else {
            exp = new Object[]{expected};
        }

        boolean[] found = new boolean[exp.length];
        while (itr.hasNext()) {
            Object val = itr.next();
            for (int i = 0; i < exp.length; i++) {
                if (eql(exp[i], val)) {
                    found[i] = true;
                }
            }
        }
        for (int i = 0; i < exp.length; i++) {
            if (found[i] == false) {
                errln("FAIL: The search result does not contain " + exp[i]);
            }
        }
    }
}
