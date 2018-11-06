/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2016, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.translit;

import java.util.List;

import org.junit.Test;

import android.icu.dev.test.TestBoilerplate;

/**
 * Moved from UnicodeMapTest
 */
public class StringBoilerplateTest extends TestBoilerplate<String> {
    public void TestStringBoilerplate() throws Exception {
    }

    @Test
    public void test() throws Exception {
        _test();
    }
    
    /* (non-Javadoc)
     * @see android.icu.dev.test.TestBoilerplate#_hasSameBehavior(java.lang.Object, java.lang.Object)
     */
    @Override
    protected boolean _hasSameBehavior(String a, String b) {
        // we are pretty confident in the equals method, so won't bother with this right now.
        return true;
    }

    /* (non-Javadoc)
     * @see android.icu.dev.test.TestBoilerplate#_addTestObject(java.util.List)
     */
    @Override
    protected boolean _addTestObject(List<String> list) {
        if (list.size() > 31) return false;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 10; ++i) {
            result.append((char)random.nextInt(0xFF));
        }
        list.add(result.toString());
        return true;
    }
}
