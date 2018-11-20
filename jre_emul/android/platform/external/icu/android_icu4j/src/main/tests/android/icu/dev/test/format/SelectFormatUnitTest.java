/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (c) 2004-2011, International Business Machines
 * Corporation and others.  All Rights Reserved.
 * Copyright (C) 2010 , Yahoo! Inc.                                            
 *******************************************************************************
 */
package android.icu.dev.test.format;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.SelectFormat;

/**
 * @author kirtig 
 * This class does the unit testing for the SelectFormat
 */
public class SelectFormatUnitTest extends TestFmwk {
  
    static final String SIMPLE_PATTERN = "feminine {feminineVerbValue} other{otherVerbValue}";

    /**
     * Unit tests for pattern syntax
     */
    @Test
    public void TestPatternSyntax() {
        String checkSyntaxData[] = {
            "odd{foo}",
            "*odd{foo} other{bar}",
            "odd{foo},other{bar}",
            "od d{foo} other{bar}",
            "odd{foo}{foobar}other{foo}",
            "odd{foo1}other{foo2}}",  
            "odd{foo1}other{{foo2}",  
            "odd{fo{o1}other{foo2}}"
        };

        //Test SelectFormat pattern syntax
        SelectFormat selFmt = new SelectFormat(SIMPLE_PATTERN);
        for (int i=0; i<checkSyntaxData.length; ++i) {
            try {
                selFmt.applyPattern(checkSyntaxData[i]);
                errln("\nERROR: Unexpected result - SelectFormat Unit Test failed "
                      + "to detect syntax error with pattern: "+checkSyntaxData[i]);
            } catch (IllegalArgumentException e){
                // ok
                continue;
            }
        }

        // ICU 4.8 does not check for duplicate keywords any more.
        selFmt.applyPattern("odd{foo} odd{bar} other{foobar}");
        assertEquals("should use first occurrence of the 'odd' keyword", "foo", selFmt.format("odd"));
        selFmt.applyPattern("odd{foo} other{bar} other{foobar}");
        assertEquals("should use first occurrence of the 'other' keyword", "bar", selFmt.format("other"));
    }

    /**
     * Unit tests for invalid keywords 
     */
    @Test
    public void TestInvalidKeyword() {
        // Test formatting with invalid keyword:
        // one which contains Pattern_Syntax or Pattern_White_Space.
        String keywords[] = {
            "9Keyword-_",
            "-Keyword-_",
            "_Keyword-_",
            "\\u00E9Keyword-_",
            "Key word",
            " Keyword",
            "Keyword ",
            "Key*word-_",
            "*Keyword-_"
        };

        String expected = "Invalid formatting argument.";
        SelectFormat selFmt = new SelectFormat(SIMPLE_PATTERN);
        for (int i = 0; i< 6; i++ ){
            try {
                selFmt.format( keywords[i]);
                fail("Error:TestInvalidKeyword failed to detect invalid keyword "
                     + "for keyword: " + keywords[i]  );
            } catch (IllegalArgumentException e){
                assertEquals("Error:TestInvalidKeyword failed with unexpected "
                            +"error message for keyword: " + keywords[i] 
                            , expected , e.getMessage() );
                continue;
            }
        }

    }

    /**
     * API tests for  applyPattern and format
     */
    @Test
    public void TestApplyFormat() {
        //Test applying and formatting with various pattern
        String patternTestData[] = {
            "fem {femValue} other{even}",
            "other{odd or even}",
            "odd{The number {0, number, integer} is odd.}other{The number {0, number, integer} is even.}",
            "odd{The number {1} is odd}other{The number {1} is even}"
        };

        String formatArgs[] = {
            "fem",
            "other",
            "odd"
        };

        String expFormatResult[][] = {
            {
                "femValue",
                "even",
                "even",
            },
            {
                "odd or even",
            "odd or even",
            "odd or even",
            },
            {
                "The number {0, number, integer} is even.",
                "The number {0, number, integer} is even.",
                "The number {0, number, integer} is odd.",
            },
            {
                "The number {1} is even",
                "The number {1} is even",
                "The number {1} is odd",
            }
        };

        log("SelectFormat Unit test: Testing  applyPattern() and format() ...");
        SelectFormat selFmt = new SelectFormat(SIMPLE_PATTERN); 

        for (int i=0; i<patternTestData.length; ++i) {
            try {
                selFmt.applyPattern(patternTestData[i]);
            } catch (IllegalArgumentException e){
                errln("ERROR: SelectFormat Unit Test failed to apply pattern- "
                     + patternTestData[i] );
                continue;
            }

            //Format with the keyword array
            for (int j=0; j<3; j++) {
                assertEquals("ERROR: SelectFormat Unit test failed in format() with unexpected result", selFmt.format(formatArgs[j]) ,expFormatResult[i][j] );
            }
        }
    }

}

