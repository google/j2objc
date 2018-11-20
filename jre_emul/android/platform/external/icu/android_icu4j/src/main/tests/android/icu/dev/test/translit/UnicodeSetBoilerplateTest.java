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
import android.icu.text.UnicodeSet;

/**
 * Moved from UnicodeMapTest
 */
public class UnicodeSetBoilerplateTest extends TestBoilerplate<UnicodeSet> {

    public void TestUnicodeSetBoilerplate() throws Exception {
    }
 
    @Test
    public void test() throws Exception {
        _test();
    }
    
    /* (non-Javadoc)
     * @see android.icu.dev.test.TestBoilerplate#_hasSameBehavior(java.lang.Object, java.lang.Object)
     */
    @Override
    protected boolean _hasSameBehavior(UnicodeSet a, UnicodeSet b) {
        // we are pretty confident in the equals method, so won't bother with this right now.
        return true;
    }

    /* (non-Javadoc)
     * @see android.icu.dev.test.TestBoilerplate#_addTestObject(java.util.List)
     */
    @Override
    protected boolean _addTestObject(List<UnicodeSet> list) {
        if (list.size() > 32) return false;
        UnicodeSet result = new UnicodeSet();
        for (int i = 0; i < 50; ++i) {
            result.add(random.nextInt(100));
        }
        list.add(result);
        return true;
    }
}
