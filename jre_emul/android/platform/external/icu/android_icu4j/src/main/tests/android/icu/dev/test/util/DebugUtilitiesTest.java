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

import org.junit.Test;

import android.icu.dev.test.TestFmwk;

/**
 * @author srl
 *
 */
public class DebugUtilitiesTest extends TestFmwk {

    @Test
    public void TestStrings() {
        logln("ICU4C version was "+DebugUtilitiesData.ICU4C_VERSION);
        int typeCount = DebugUtilities.typeCount();
        if(typeCount==0) {
            errln("typecount is 0!");
        }
        for(int j=0;j<typeCount;j++) {
            int enumCount = DebugUtilities.enumCount(j);
            logln("Type "+j+"/"+typeCount+": "+DebugUtilities.typeString(j) +" - enumCount "+enumCount);
            for(int k=0;k<enumCount;k++) {
                String enumString = DebugUtilities.enumString(j,k);
                int enumValue = DebugUtilities.enumArrayValue(j, k);
                int enumValueFromString = DebugUtilities.enumByString(j, enumString);
                logln("     Enum "+k+"/"+enumCount+": "+enumString +" - value "+enumValue+", value from string:"+enumValueFromString);
                if(enumValue != k) {
                    errln("FAIL: Type "+j+"/"+typeCount+": "+DebugUtilities.typeString(j) +" -    Enum "+k+"/"+enumCount+": "+enumString +" -  ERR: value="+enumValue+" should be "+k);
                }
                if(enumValueFromString != k) {
                    errln("FAIL: Type "+j+"/"+typeCount+": "+DebugUtilities.typeString(j) +" -    Enum "+k+"/"+enumCount+": "+enumString +" -  ERR: enumByString returned="+enumValueFromString+" should be "+k);
                }
            }
        }
    }
}
