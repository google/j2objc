/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.util;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.MessageFormat;
import android.icu.text.SimpleFormatter;
import android.icu.util.ULocale;

public class SimpleFormatterTest extends TestFmwk {

    /**
     * Constructor
     */
     public SimpleFormatterTest()
     {
     }
       
     // public methods -----------------------------------------------
     
     @Test
     public void TestWithNoArguments() {
         SimpleFormatter fmt = SimpleFormatter.compile("This doesn''t have templates '{0}");
         assertEquals(
                 "getArgumentLimit",
                 0,
                 fmt.getArgumentLimit());
         assertEquals(
                 "format",
                 "This doesn't have templates {0}",
                 fmt.format("unused"));
         assertEquals(
                 "format with values=null",
                 "This doesn't have templates {0}",
                 fmt.format((CharSequence[])null));
         assertEquals(
                 "toString",
                 "This doesn't have templates {0}",
                 fmt.toString());
         int[] offsets = new int[1];
         assertEquals(
                 "formatAndAppend",
                 "This doesn't have templates {0}",
                 fmt.formatAndAppend(new StringBuilder(), offsets).toString());
         assertEquals(
                 "offsets[0]",
                 -1,
                 offsets[0]);
         assertEquals(
                 "formatAndAppend with values=null",
                 "This doesn't have templates {0}",
                 fmt.formatAndAppend(new StringBuilder(), null, (CharSequence[])null).toString());
         assertEquals(
                 "formatAndReplace with values=null",
                 "This doesn't have templates {0}",
                 fmt.formatAndReplace(new StringBuilder(), null, (CharSequence[])null).toString());
     }

     @Test
     public void TestSyntaxErrors() {
         try {
             SimpleFormatter.compile("{}");
             fail("Syntax error did not yield an exception.");
         } catch (IllegalArgumentException expected) {
         }
         try {
             SimpleFormatter.compile("{12d");
             fail("Syntax error did not yield an exception.");
         } catch (IllegalArgumentException expected) {
         }
     }

     @Test
     public void TestOneArgument() {
        assertEquals("TestOneArgument",
                "1 meter",
                SimpleFormatter.compile("{0} meter").format("1"));
     }

     @Test
     public void TestBigArgument() {
         SimpleFormatter fmt = SimpleFormatter.compile("a{20}c");
         assertEquals("{20} count", 21, fmt.getArgumentLimit());
         CharSequence[] values = new CharSequence[21];
         values[20] = "b";
         assertEquals("{20}=b", "abc", fmt.format(values));
      }

     @Test
     public void TestGetTextWithNoArguments() {
         assertEquals(
                 "",
                 "Templates  and  are here.",
                 SimpleFormatter.compile(
                         "Templates {1}{2} and {3} are here.").getTextWithNoArguments());
     }
     
     @Test
     public void TestTooFewArgumentValues() {
         SimpleFormatter fmt = SimpleFormatter.compile(
                 "Templates {2}{1} and {4} are out of order.");
         try {
             fmt.format("freddy", "tommy", "frog", "leg");
             fail("Expected IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             // Expected
         }
         try {
             fmt.formatAndAppend(
                     new StringBuilder(), null, "freddy", "tommy", "frog", "leg");
             fail("Expected IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             // Expected
         }
         try {
             fmt.formatAndReplace(
                     new StringBuilder(), null, "freddy", "tommy", "frog", "leg");
             fail("Expected IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             // Expected
         }
     }
     
     @Test
     public void TestWithArguments() {
         SimpleFormatter fmt = SimpleFormatter.compile(
                 "Templates {2}{1} and {4} are out of order.");
         assertEquals(
                 "getArgumentLimit",
                 5,
                 fmt.getArgumentLimit()); 
         assertEquals(
                 "toString",
                 "Templates {2}{1} and {4} are out of order.",
                 fmt.toString());
        int[] offsets = new int[6]; 
        assertEquals(
                 "format",
                 "123456: Templates frogtommy and {0} are out of order.",
                 fmt.formatAndAppend(
                         new StringBuilder("123456: "),
                         offsets,
                         "freddy", "tommy", "frog", "leg", "{0}").toString());
         
         int[] expectedOffsets = {-1, 22, 18, -1, 32, -1};
         verifyOffsets(expectedOffsets, offsets);
     }
     
     @Test
     public void TestFormatUseAppendToAsArgument() {
         SimpleFormatter fmt = SimpleFormatter.compile(
                 "Arguments {0} and {1}");
         StringBuilder appendTo = new StringBuilder("previous:");
         try {
             fmt.formatAndAppend(appendTo, null, appendTo, "frog");
             fail("IllegalArgumentException expected.");
         } catch (IllegalArgumentException e) {
             // expected.
         }
     }
     
     @Test
     public void TestFormatReplaceNoOptimization() {
         SimpleFormatter fmt = SimpleFormatter.compile("{2}, {0}, {1} and {3}");
         int[] offsets = new int[4];
         StringBuilder result = new StringBuilder("original");
        assertEquals(
                 "format",
                 "frog, original, freddy and by",
                 fmt.formatAndReplace(
                         result,
                         offsets,
                         result, "freddy", "frog", "by").toString());
         
         int[] expectedOffsets = {6, 16, 0, 27};
         verifyOffsets(expectedOffsets, offsets);
     }
     
     
     @Test
     public void TestFormatReplaceNoOptimizationLeadingText() {
         SimpleFormatter fmt = SimpleFormatter.compile("boo {2}, {0}, {1} and {3}");
         int[] offsets = new int[4];
         StringBuilder result = new StringBuilder("original");
        assertEquals(
                 "format",
                 "boo original, freddy, frog and by",
                 fmt.formatAndReplace(
                         result,
                         offsets,
                         "freddy", "frog", result, "by").toString());
         
         int[] expectedOffsets = {14, 22, 4, 31};
         verifyOffsets(expectedOffsets, offsets);
     }
     
     @Test
     public void TestFormatReplaceOptimization() {
         SimpleFormatter fmt = SimpleFormatter.compile("{2}, {0}, {1} and {3}");
         int[] offsets = new int[4];
         StringBuilder result = new StringBuilder("original");
        assertEquals(
                 "format",
                 "original, freddy, frog and by",
                 fmt.formatAndReplace(
                         result,
                         offsets,
                         "freddy", "frog", result, "by").toString());
         
         int[] expectedOffsets = {10, 18, 0, 27};
         verifyOffsets(expectedOffsets, offsets);  
     }
     
     @Test
     public void TestFormatReplaceOptimizationNoOffsets() {
         SimpleFormatter fmt = SimpleFormatter.compile("{2}, {0}, {1} and {3}");
         StringBuilder result = new StringBuilder("original");
        assertEquals(
                 "format",
                 "original, freddy, frog and by",
                 fmt.formatAndReplace(
                         result,
                         null,
                         "freddy", "frog", result, "by").toString());
         
     }
     
     @Test
     public void TestFormatReplaceNoOptimizationNoOffsets() {
         SimpleFormatter fmt = SimpleFormatter.compile(
                 "Arguments {0} and {1}");
         StringBuilder result = new StringBuilder("previous:");
         assertEquals(
                 "",
                 "Arguments previous: and frog",
                 fmt.formatAndReplace(result, null, result, "frog").toString());
     }
     
     @Test
     public void TestFormatReplaceNoOptimizationLeadingArgumentUsedTwice() {
         SimpleFormatter fmt = SimpleFormatter.compile(
                 "{2}, {0}, {1} and {3} {2}");
         StringBuilder result = new StringBuilder("original");
         int[] offsets = new int[4];
         assertEquals(
                 "",
                 "original, freddy, frog and by original",
                 fmt.formatAndReplace(
                         result,
                         offsets,
                         "freddy", "frog", result, "by").toString());
         int[] expectedOffsets = {10, 18, 30, 27};
         verifyOffsets(expectedOffsets, offsets);
     }

     @Test
     public void TestQuotingLikeMessageFormat() {
         String pattern = "{0} don't can''t '{5}''}{a' again '}'{1} to the '{end";
         SimpleFormatter spf = SimpleFormatter.compile(pattern);
         MessageFormat mf = new MessageFormat(pattern, ULocale.ROOT);
         String expected = "X don't can't {5}'}{a again }Y to the {end";
         assertEquals("MessageFormat", expected, mf.format(new Object[] { "X", "Y" }));
         assertEquals("SimpleFormatter", expected, spf.format("X", "Y"));
     }

     private void verifyOffsets(int[] expected, int[] actual) {
         for (int i = 0; i < expected.length; ++i) {
             if (expected[i] != actual[i]) {
                 errln("Expected "+expected[i]+", got " + actual[i]);
             }
         } 
     }
     
}
