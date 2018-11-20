/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.sample;

import java.util.ListResourceBundle;

/**
 * This is sample data for ModuleTestSample, which is an illustration
 * of a subclass of ModuleTest.  This data is in a format which
 * is understood by ResourceModule, which for simplicity expects
 * all data, including numeric and boolean data, to be represented
 * by Strings.
 */
public class ModuleTestSampleData extends ListResourceBundle {
    public Object[][] getContents() {
    return contents;
    }

    Object[][] contents = {
    { "Info", new Object[][] {
        { "Description", "This is a sample test module that illustrates ModuleTest " +
          "and uses data formatted for ResourceModule." },
        { "Headers", new String[] {
        "aStringArray", "anIntArray", "aBooleanArray"
        }},
    }},
    

    { "TestData", new Object[][] {
        { "Test01", new Object[][] {
        { "Info", new Object[][] {
            { "Description", "A typical test using both settings and cases." },
            { "Long_Description", "It does not defined its own headers, but instead " +
              "uses the default headers defined for the module.  " +
              "There are two sets of settings and three cases." },
        }},
        { "Settings", new Object[] {
            new Object[][]
            {{ "aString", "this is a string" },
             { "anInt", "43" },
             { "aBoolean", "false" }},
            new Object[][] 
            {{ "aString", "this is another string" },
             { "aBoolean", "true" }}
        }},
        { "Cases", new Object[] {
            new Object[] {
                new String[] { "one", "two", "three" },
                new String[] { "24", "48", "72" },
                new String[] { "true", "false", "true" }
            },
            new Object[] { 
                new String[] { "four", "five", "six" },
                new String[] { "-1", "-5", "-10" },
                new String[] { "true", "false", "false" }
            },
            new Object[] { 
                new String[] { "bagel", "peanuts", "carrot" },
                new String[] { "0", "00001", "10101" },
                new String[] { "false", "false", "False" }
            },
        }}
        }},

        { "Test02", new Object[][] {
        { "Info", new Object[][] {
            { "Description", "A typical test that uses cases but not settings." },
            { "Long_Description", "It defines its own headers." },
            { "Headers", new String[] {
            "aString", "anInt", "aBoolean"
            }},
        }},
        { "Cases", new Object[] {
            new Object[] { "Superstring", "42", "true" },
            new Object[] { "Underdog", "12", "false" },
            new Object[] { "ScoobyDoo", "7", "TrUe" }
        }}
        }},

        { "Test03", new Object[][] {
        { "Info", new Object[][] {
            { "Description", "A typical test that uses just the info, no cases or settings." },
            { "Extra", "This is some extra information." }
        }},
        }},

        // no Test04 data
        // Test04 should cause an exception to be thrown since ModuleTestSample does not
        // specify that it is ok for it to have no data.

        // no Test05 data
        // Test05 should just log this fact, since ModuleTestSample indicates that it is
        // ok for Test05 to have no data in its override of validateMethod.

        { "Test06", new Object[][] {
        { "Info", new Object[][] {
            { "Description", "A test that has bad data." },
            { "Long_Description", "This illustrates how a data error will automatically " +
              "terminate the settings and cases loop." },
            { "Headers", new String[] {
            "IsGood", "Data",
            }},
        }},
        { "Cases", new Object[] {
            new Object[] { "Good", "23" },
            new Object[] { "Good", "-123" },
            new Object[] { "Bad", "Whoops" },
            new Object[] { "Not Executed", "35" },
        }},
        }},

        { "Test07", new Object[][] {
        { "Info", new Object[][] {
            { "Description", "A test that fails with a certain combination of settings and case." },
            { "Long_Description", "This illustrates how a test error will automatically " +
              "terminate the settings and cases loop.  Settings data is values, the case " +
              "data is factors.  The third factor is not a factor of the second value.  " +
              "The test will log an error, which will automatically stop the loop." },
            { "Headers", new String[] {
            "Factor",
            }},
        }},
        { "Settings" , new Object[] {
            new Object[][] {{ "Value", "210" }},
            new Object[][] {{ "Value", "420" }},
            new Object[][] {{ "Value", "42" }},
            new Object[][] {{ "Value", "Not reached." }}
        }},
        { "Cases", new Object[] {
            new Object[] { "2" },
            new Object[] { "3" },
            new Object[] { "5" },
            new Object[] { "7" },
        }},
        }},

        { "Test08", new Object[][] {
        { "Info", new Object[][] {
            { "Description", "A test with data missing from a test case." },
            { "Headers", new String[] {
            "One", "Two", "Three"
            }},
        }},
        { "Cases", new Object[] {
            new Object[] { "1", "2", "3" },
            new Object[] { "4", "5" }, // too short
            new Object[] { "6", "7", "8" },
        }},
        }},

        { "Test09", new Object[][] {
        { "Info", new Object[][] {
            { "Description", "A test with data stored as int arrays instead of strings" },
            { "Headers", new String[] {
            "Radix", "Power", "Value"
            }},
        }},
        { "Cases", new Object[] {
            new Object[] { "2", new int[] { 1, 2, 3 }, new int[] { 2, 4, 8 }},
            new Object[] { "3", new int[] { 3, 4, 5 }, new int[] { 27, 81, 243 }},
            new Object[] { "2", new int[] { 0, 8, 16, 24 }, new int[] { 1, 256, 65536, 65536 * 256 }},
        }},
        }},
    }},
    };
}
