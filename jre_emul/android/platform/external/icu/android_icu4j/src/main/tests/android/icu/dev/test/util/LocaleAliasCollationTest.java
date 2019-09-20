/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.dev.test.util;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import android.icu.text.Collator;
import android.icu.util.ULocale;

public class LocaleAliasCollationTest extends android.icu.dev.test.TestFmwk {
    private static final ULocale[][] _LOCALES = {
            {new ULocale("en", "RH"), new ULocale("en", "ZW")},
            {new ULocale("in"), new ULocale("id")},
            {new ULocale("in", "ID"), new ULocale("id", "ID")},
            {new ULocale("iw"), new ULocale("he")},
            {new ULocale("iw", "IL"), new ULocale("he", "IL")},
            {new ULocale("ji"), new ULocale("yi")},

            {new ULocale("en", "BU"), new ULocale("en", "MM")},
            {new ULocale("en", "DY"), new ULocale("en", "BJ")},
            {new ULocale("en", "HV"), new ULocale("en", "BF")},
            {new ULocale("en", "NH"), new ULocale("en", "VU")},
            {new ULocale("en", "TP"), new ULocale("en", "TL")},
            {new ULocale("en", "ZR"), new ULocale("en", "CD")}
    };

    private static final int _LOCALE_NUMBER = _LOCALES.length;
    private ULocale[] available = null;
    private HashMap availableMap = new HashMap();
    private static final ULocale _DEFAULT_LOCALE = ULocale.US;

    public LocaleAliasCollationTest() {
    }

    @Before
    public void init() {
        available = ULocale.getAvailableLocales();
        for(int i=0; i<available.length;i++){
            availableMap.put(available[i].toString(),"");
        }
    }

    @Test
    public void TestCollation() {
        ULocale defLoc = ULocale.getDefault();
        ULocale.setDefault(_DEFAULT_LOCALE);
        for (int i=0; i<_LOCALE_NUMBER; i++) {
            ULocale oldLoc = _LOCALES[i][0];
            ULocale newLoc = _LOCALES[i][1];
            if(availableMap.get(_LOCALES[i][1])==null){
                logln(_LOCALES[i][1]+" is not available. Skipping!");
                continue;
            }
            Collator c1 = Collator.getInstance(oldLoc);
            Collator c2 = Collator.getInstance(newLoc);

            if (!c1.equals(c2)) {
                errln("CollationTest: c1!=c2: newLoc= "+newLoc +" oldLoc= "+oldLoc);
            }

            logln("Collation old:"+oldLoc+"   new:"+newLoc);
        }
        ULocale.setDefault(defLoc);
    }
}
