/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
* Copyright (c) 2002-2010, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Alan Liu
* Created: November 5 2002
* Since: ICU 2.4
**********************************************************************
*/
package android.icu.dev.test.lang;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;

public class UPropertyAliasesTest extends TestFmwk {
  
    public UPropertyAliasesTest() {}
    
    /**
     * Test the property names and property value names API.
     */
    @Test
    public void TestPropertyNames() {
        int p, v, choice, rev;
        for (p=0; ; ++p) {
            boolean sawProp = false;
            for (choice=0; ; ++choice) {
                String name = null;
                try {
                    name = UCharacter.getPropertyName(p, choice);
                    if (!sawProp) log("prop " + p + ":");
                    String n = (name != null) ? ("\"" + name + '"') : "null";
                    log(" " + choice + "=" + n);
                    sawProp = true;
                } catch (IllegalArgumentException e) {
                    if (choice > 0) break;
                }
                if (name != null) {
                    /* test reverse mapping */
                    rev = UCharacter.getPropertyEnum(name);
                    if (rev != p) {
                        errln("Property round-trip failure: " + p + " -> " +
                              name + " -> " + rev);
                    }
                }
            }
            if (sawProp) {
                /* looks like a valid property; check the values */
                String pname = UCharacter.getPropertyName(p, UProperty.NameChoice.LONG);
                int max = 0;
                if (p == UProperty.CANONICAL_COMBINING_CLASS) {
                    max = 255;
                } else if (p == UProperty.GENERAL_CATEGORY_MASK) {
                    /* it's far too slow to iterate all the way up to
                       the real max, U_GC_P_MASK */
                    max = 0x1000; // U_GC_NL_MASK;
                } else if (p == UProperty.BLOCK) {
                    /* UBlockCodes, unlike other values, start at 1 */
                    max = 1;
                }
                logln("");
                for (v=-1; ; ++v) {
                    boolean sawValue = false;
                    for (choice=0; ; ++choice) {
                        String vname = null;
                        try {
                            vname = UCharacter.getPropertyValueName(p, v, choice);
                            String n = (vname != null) ? ("\"" + vname + '"') : "null";
                            if (!sawValue) log(" " + pname + ", value " + v + ":");
                            log(" " + choice + "=" + n);
                            sawValue = true;
                        }
                        catch (IllegalArgumentException e) {
                            if (choice>0) break;
                        }
                        if (vname != null) {
                            /* test reverse mapping */
                            rev = UCharacter.getPropertyValueEnum(p, vname);
                            if (rev != v) {
                                errln("Value round-trip failure (" + pname +
                                      "): " + v + " -> " +
                                      vname + " -> " + rev);
                            }
                        }
                    }
                    if (sawValue) {
                        logln("");
                    }
                    if (!sawValue && v>=max) break;
                }
            }
            if (!sawProp) {
                if (p>=UProperty.STRING_LIMIT) {
                    break;
                } else if (p>=UProperty.DOUBLE_LIMIT) {
                    p = UProperty.STRING_START - 1;
                } else if (p>=UProperty.MASK_LIMIT) {
                    p = UProperty.DOUBLE_START - 1;
                } else if (p>=UProperty.INT_LIMIT) {
                    p = UProperty.MASK_START - 1;
                } else if (p>=UProperty.BINARY_LIMIT) {
                    p = UProperty.INT_START - 1;
                }
            }
        }
        
        int i = UCharacter.getIntPropertyMinValue(
                                        UProperty.CANONICAL_COMBINING_CLASS);
        try {
            for (; i <= UCharacter.getIntPropertyMaxValue(
                                          UProperty.CANONICAL_COMBINING_CLASS);
                 i ++) {   
                 UCharacter.getPropertyValueName(
                                           UProperty.CANONICAL_COMBINING_CLASS,
                                           i, UProperty.NameChoice.LONG);
            }
        }      
        catch (IllegalArgumentException e) {
            errln("0x" + Integer.toHexString(i) 
                  + " should have a null property value name");
        }
    }

    @Test
    public void TestUnknownPropertyNames() {
        try {
            int p = UCharacter.getPropertyEnum("??");
            errln("UCharacter.getPropertyEnum(??) returned " + p +
                  " rather than throwing an exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            int p = UCharacter.getPropertyValueEnum(UProperty.LINE_BREAK, "?!");
            errln("UCharacter.getPropertyValueEnum(UProperty.LINE_BREAK, ?!) returned " + p +
                  " rather than throwing an exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
}
