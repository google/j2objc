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

import java.text.FieldPosition;
import java.text.ParsePosition;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.SelectFormat;

/**
 * @author kirtig 
 * This class tests the API functionality of the SelectFormat
 */
public class SelectFormatAPITest extends TestFmwk {
  
    static final String SIMPLE_PATTERN1 = "feminine {feminineVerbValue1} other{otherVerbValue1}";
    static final String SIMPLE_PATTERN2 = "feminine {feminineVerbValue2} other{otherVerbValue2}";

    /**
     * API tests for constructors
     */
    @Test
    public void TestConstructors() {
        SelectFormat selFmt = new SelectFormat(SIMPLE_PATTERN1);
        assertNotNull("Error: TestConstructors - SelectFormat object constructed "
                      + "with argument constructor is null" , selFmt );  
    }

    /**
     * API tests for equals() method
     */
    @Test
    public void TestEquals() {
        SelectFormat selFmt1 = null;

        //Check equality for pattern constructed SelectFormats
        selFmt1 = new SelectFormat(SIMPLE_PATTERN1);
        SelectFormat selFmt2 = new SelectFormat(SIMPLE_PATTERN1);
        assertTrue("Equals test failed while checking equality for " 
                   + "pattern constructed SelectFormats ." 
                   , selFmt1.equals(selFmt2) ); 

        //Check equality for 2 objects  
        Object selFmt3 = new SelectFormat(SIMPLE_PATTERN1);
        Object selFmt4 = new SelectFormat(SIMPLE_PATTERN1);
        Object selFmt5 = new SelectFormat(SIMPLE_PATTERN2);
        assertTrue("Equals test failed while checking equality for object 1." 
                , selFmt3.equals(selFmt4) );
        assertTrue("Equals test failed while checking equality for object 2." 
                    , selFmt1.equals(selFmt3) );
        assertFalse("Equals test failed while checking equality for object 3." 
                , selFmt3.equals(selFmt5) );
    }

    /**
     * API tests for applyPattern() method
     */
    @Test
    public void TestApplyPatternToPattern() {
        SelectFormat selFmt = null;
        String pattern = "masculine{masculineVerbValue} other{otherVerbValue}";

        //Check for applyPattern/toPattern
        selFmt = new SelectFormat(SIMPLE_PATTERN1);
        selFmt.applyPattern(pattern);
        assertEquals("Failed in applyPattern,toPattern with unexpected output"
                     , pattern,  selFmt.toPattern() );

        //Check for invalid pattern
        try {
            String brokenPattern = "broken }{ pattern";
            selFmt.applyPattern(brokenPattern);
            errln("Failed in applyPattern.  applyPattern should throw IllegalArgumentException for " + brokenPattern);
        } catch (IllegalArgumentException e) {
            // This is OK
        }
    }

    /**
     * API tests for toString() method
     */
    @Test
    public void TestToString(){
        SelectFormat selFmt = null;

        //Check toString for pattern constructed SelectFormat
        selFmt = new SelectFormat(SIMPLE_PATTERN1);
        String expected = "pattern='feminine {feminineVerbValue1} other{otherVerbValue1}'";
        assertEquals("Failed in TestToString with unexpected output 2"
                     , expected, selFmt.toString() );
    }

    /**
     * API tests for hashCode() method
     */
    @Test
    public void TestHashCode(){
        //Check hashCode for pattern constructed SelectFormat
        SelectFormat selFmt = new SelectFormat(SIMPLE_PATTERN1);
        SelectFormat selFmt1 = new SelectFormat(SIMPLE_PATTERN1);
        SelectFormat selFmt2 = new SelectFormat(SIMPLE_PATTERN2);
        assertEquals("Failed in TestHashCode 1 with unexpected output"
                     , selFmt.hashCode(), selFmt1.hashCode() );
        assertNotEquals("Failed in TestHashCode 2 with unexpected output"
                     , selFmt.hashCode(), selFmt2.hashCode() );
    }

    /**
     * API tests for toPattern() method
     */
    @Test
    public void TestToPattern(){
        SelectFormat selFmt = new SelectFormat(SIMPLE_PATTERN1);
        assertEquals("Failed in TestToPattern 2 with unexpected output"
                     , SIMPLE_PATTERN1, selFmt.toPattern() );
    }

    /**
     * API tests for format() method
     */
    @Test
    public void TestFormat(){
        //Check format for pattern constructed object
        SelectFormat selFmt1 = new SelectFormat(SIMPLE_PATTERN1);
        String expected = "feminineVerbValue1";
        assertEquals("Failed in TestFormat with unexpected output 1"
                     , expected 
                     , selFmt1.format("feminine") );

        //Check format with appendTo for pattern constructed object
        expected = "AppendHere-otherVerbValue1";
        StringBuffer strBuf = new StringBuffer("AppendHere-");
        assertEquals("Failed in TestFormat with unexpected output 2"
                     , expected
                     , (selFmt1.format("other", strBuf, new FieldPosition(0))).toString());
       
        //Check format throws exception on invalid argument.
        boolean threwException = false;
        try {
            StringBuffer buf = new StringBuffer("AppendHere-");
            selFmt1.format(Integer.valueOf(0), buf, new FieldPosition(0));
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue("Did not throw IllegalArgumentException.", threwException);
    }

    /**
     * API tests for parseObject() method
     */
    @Test
    public void TestParseObject(){
        //Check parseObject
        try {
            SelectFormat selFmt = new SelectFormat(SIMPLE_PATTERN1);
            selFmt.parseObject("feminine", new ParsePosition(0) );
            fail("Failed in TestParseObject - UnsupportedOperationException not received");
        } catch (UnsupportedOperationException e){
            //Expect this Exception
        }
    }
}

