/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2008, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.data;
import java.util.ListResourceBundle;

public class TestDataElements_testtypes extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public final Object[][] getContents() {
          return  contents;
    }

    private static Object[][] contents = {
                {
                    "binarytest",
                    new byte[] {
                        0,   1,   2,   3,   4,   5,   6,   7,   8,   9,   10,  11,  12,  13,  14,
                    },
                },
                {
                    "emptyarray",
                    new String[] { 
                    },
                },
                {
                    "emptybin",
                    new byte[] {},
                },
                {
                    "emptyexplicitstring",
                    "",
                },
                {
                    "emptyint",
                    new Integer(0),
                },
                {
                    "emptyintv",
                    new Integer[] {
                    },
                },
                {
                    "emptystring",
                    "",
                },
                {
                    "emptytable",
                    new Object[][]{
                    },
                },
                {
                    "importtest",
                    new byte[] {
                        0,   1,   2,   3,   4,   5,   6,   7,   8,   9,   10,  11,  12,  13,  14,
                    },
                },
                {
                    "integerarray",
                    new Integer[] {
                        new Integer(1),
                        new Integer(2),
                        new Integer(3),
                        new Integer(-3),
                        new Integer(4),
                        new Integer(5),
                        new Integer(6),
                        new Integer(7),
                    },
                },
                {
                    "menu",
                    new Object[][]{
                        {
                            "file",
                            new Object[][]{
                                {
                                    "exit",
                                    "Exit",
                                },
                                {
                                    "open",
                                    "Open",
                                },
                                {
                                    "save",
                                    "Save",
                                },
                            },
                        },
                    },
                },
                {
                    "minusone",
                    new Integer(-1),
                },
                {
                    "one",
                    new Integer(1),
                },
                {
                    "onehundredtwentythree",
                    new Integer(123),
                },
                {
                    "plusone",
                    new Integer(1),
                },
                {
                    "string",
                    new String[] { 
                    },
                },
                {
                    "stringTable",
                    new Object[]{
                        new String[] { 
                        },

                    },
                },
                {
                    "test_underscores",
                    "test message ....",
                },
                {
                    "testescape",
                    "tab:\u0009 cr:\f ff:\u000C newline:\n backslash:\\" +
                    " quote=\\\' doubleQuote=\\\" singlequoutes=''",
                },
                {
                    "zerotest",
                    "abc\u0000def",
                },
    };
}
