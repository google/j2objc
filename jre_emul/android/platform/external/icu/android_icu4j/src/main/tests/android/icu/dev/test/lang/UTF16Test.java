/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 1996-2014, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

package android.icu.dev.test.lang;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.UTF16Util;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.text.ReplaceableString;
import android.icu.text.UTF16;
import android.icu.text.UTF16.StringComparator;

/**
* Testing class for UTF16
* @author Syn Wee Quek
* @since feb 09 2001
*/
public final class UTF16Test extends TestFmwk
{
    // constructor ===================================================

    /**
     * Constructor
     */
    public UTF16Test()
    {
    }

    // public methods ================================================

    /**
     * Testing UTF16 class methods append
     */
    @Test
    public void TestAppend()
    {
          StringBuffer strbuff = new StringBuffer("this is a string ");
          char array[] = new char[UCharacter.MAX_VALUE >> 2];
          int strsize = strbuff.length();
          int arraysize = strsize;

          if (0 != strsize) {
            strbuff.getChars(0, strsize, array, 0);
        }
          for (int i = 1; i < UCharacter.MAX_VALUE; i += 100) {
        UTF16.append(strbuff, i);
        arraysize = UTF16.append(array, arraysize, i);

        String arraystr = new String(array, 0, arraysize);
        if (!arraystr.equals(strbuff.toString())) {
        errln("FAIL Comparing char array append and string append " +
              "with 0x" + Integer.toHexString(i));
        }

        // this is to cater for the combination of 0xDBXX 0xDC50 which
        // forms a supplementary character
        if (i == 0xDC51) {
        strsize --;
        }

        if (UTF16.countCodePoint(strbuff) != strsize + (i / 100) + 1) {
        errln("FAIL Counting code points in string appended with " +
              " 0x" + Integer.toHexString(i));
        break;
        }
    }

    // coverage for new 1.5 - cover only so no real test
    strbuff = new StringBuffer();
    UTF16.appendCodePoint(strbuff, 0x10000);
    if (strbuff.length() != 2) {
        errln("fail appendCodePoint");
    }
    }

    /**
     * Testing UTF16 class methods bounds
     */
    @Test
    public void TestBounds()
    {
          StringBuffer strbuff =
                        //0     12345     6     7     8     9
        new StringBuffer("\udc000123\ud800\udc00\ud801\udc01\ud802");
          String str = strbuff.toString();
          char array[] = str.toCharArray();
          int boundtype[] = {UTF16.SINGLE_CHAR_BOUNDARY,
               UTF16.SINGLE_CHAR_BOUNDARY,
               UTF16.SINGLE_CHAR_BOUNDARY,
               UTF16.SINGLE_CHAR_BOUNDARY,
               UTF16.SINGLE_CHAR_BOUNDARY,
               UTF16.LEAD_SURROGATE_BOUNDARY,
               UTF16.TRAIL_SURROGATE_BOUNDARY,
               UTF16.LEAD_SURROGATE_BOUNDARY,
               UTF16.TRAIL_SURROGATE_BOUNDARY,
               UTF16.SINGLE_CHAR_BOUNDARY};
          int length = str.length();
          for (int i = 0; i < length; i ++) {
        if (UTF16.bounds(str, i) != boundtype[i]) {
                  errln("FAIL checking bound type at index " + i);
        }
        if (UTF16.bounds(strbuff, i) != boundtype[i]) {
                  errln("FAIL checking bound type at index " + i);
        }
        if (UTF16.bounds(array, 0, length, i) != boundtype[i]) {
                  errln("FAIL checking bound type at index " + i);
        }
          }
          // does not straddle between supplementary character
          int start = 4;
          int limit = 9;
          int subboundtype1[] = {UTF16.SINGLE_CHAR_BOUNDARY,
                   UTF16.LEAD_SURROGATE_BOUNDARY,
                   UTF16.TRAIL_SURROGATE_BOUNDARY,
                   UTF16.LEAD_SURROGATE_BOUNDARY,
                   UTF16.TRAIL_SURROGATE_BOUNDARY};
          try {
        UTF16.bounds(array, start, limit, -1);
        errln("FAIL Out of bounds index in bounds should fail");
          } catch (Exception e) {
        // getting rid of warnings
        System.out.print("");
          }

          for (int i = 0; i < limit - start; i ++) {
        if (UTF16.bounds(array, start, limit, i) != subboundtype1[i]) {
                  errln("FAILED Subarray bounds in [" + start + ", " + limit +
              "] expected " + subboundtype1[i] + " at offset " + i);
        }
          }

          // starts from the mid of a supplementary character
          int subboundtype2[] = {UTF16.SINGLE_CHAR_BOUNDARY,
                   UTF16.LEAD_SURROGATE_BOUNDARY,
                   UTF16.TRAIL_SURROGATE_BOUNDARY};

          start = 6;
          limit = 9;
          for (int i = 0; i < limit - start; i ++) {
        if (UTF16.bounds(array, start, limit, i) != subboundtype2[i]) {
                  errln("FAILED Subarray bounds in [" + start + ", " + limit +
              "] expected " + subboundtype2[i] + " at offset " + i);
        }
          }

          // ends in the mid of a supplementary character
          int subboundtype3[] = {UTF16.LEAD_SURROGATE_BOUNDARY,
                   UTF16.TRAIL_SURROGATE_BOUNDARY,
                   UTF16.SINGLE_CHAR_BOUNDARY};
          start = 5;
          limit = 8;
          for (int i = 0; i < limit - start; i ++) {
        if (UTF16.bounds(array, start, limit, i) != subboundtype3[i]) {
                  errln("FAILED Subarray bounds in [" + start + ", " + limit +
              "] expected " + subboundtype3[i] + " at offset " + i);
        }
          }
    }

    /**
     * Testing UTF16 class methods charAt and charAtCodePoint
     */
    @Test
    public void TestCharAt()
    {
          StringBuffer strbuff =
        new StringBuffer("12345\ud800\udc0167890\ud800\udc02");
          if (UTF16.charAt(strbuff, 0) != '1' || UTF16.charAt(strbuff, 2) != '3'
              || UTF16.charAt(strbuff, 5) != 0x10001 ||
        UTF16.charAt(strbuff, 6) != 0x10001 ||
        UTF16.charAt(strbuff, 12) != 0x10002 ||
        UTF16.charAt(strbuff, 13) != 0x10002) {
        errln("FAIL Getting character from string buffer error" );
          }
          String str = strbuff.toString();
          if (UTF16.charAt(str, 0) != '1' || UTF16.charAt(str, 2) != '3' ||
        UTF16.charAt(str, 5) != 0x10001 || UTF16.charAt(str, 6) != 0x10001
        || UTF16.charAt(str, 12) != 0x10002 ||
        UTF16.charAt(str, 13) != 0x10002)
        {
              errln("FAIL Getting character from string error" );
        }
          char array[] = str.toCharArray();
          int start = 0;
          int limit = str.length();
          if (UTF16.charAt(array, start, limit, 0) != '1' ||
        UTF16.charAt(array, start, limit, 2) != '3' ||
        UTF16.charAt(array, start, limit, 5) != 0x10001 ||
        UTF16.charAt(array, start, limit, 6) != 0x10001 ||
        UTF16.charAt(array, start, limit, 12) != 0x10002 ||
        UTF16.charAt(array, start, limit, 13) != 0x10002) {
        errln("FAIL Getting character from array error" );
          }
          // check the sub array here.
          start = 6;
          limit = 13;
          try {
        UTF16.charAt(array, start, limit, -1);
        errln("FAIL out of bounds error expected");
          } catch (Exception e) {
        System.out.print("");
          }
          try {
        UTF16.charAt(array, start, limit, 8);
        errln("FAIL out of bounds error expected");
          } catch (Exception e) {
        System.out.print("");
          }
          if (UTF16.charAt(array, start, limit, 0) != 0xdc01) {
        errln("FAIL Expected result in subarray 0xdc01");
          }
          if (UTF16.charAt(array, start, limit, 6) != 0xd800) {
        errln("FAIL Expected result in subarray 0xd800");
          }
          ReplaceableString replaceable = new ReplaceableString(str);
          if (UTF16.charAt(replaceable, 0) != '1' ||
              UTF16.charAt(replaceable, 2) != '3' ||
        UTF16.charAt(replaceable, 5) != 0x10001 ||
        UTF16.charAt(replaceable, 6) != 0x10001 ||
        UTF16.charAt(replaceable, 12) != 0x10002 ||
        UTF16.charAt(replaceable, 13) != 0x10002) {
        errln("FAIL Getting character from replaceable error" );
          }
          
          StringBuffer strbuffer = new StringBuffer("0xD805");
          UTF16.charAt((CharSequence)strbuffer, 0);
    }

    /**
     * Testing UTF16 class methods countCodePoint
     */
    @Test
    public void TestCountCodePoint()
    {
        StringBuffer strbuff = new StringBuffer("");
        char         array[] = null;
        if (UTF16.countCodePoint(strbuff) != 0 ||
        UTF16.countCodePoint("") != 0 ||
        UTF16.countCodePoint(array,0 ,0) != 0) {
        errln("FAIL Counting code points for empty strings");
        }

        strbuff = new StringBuffer("this is a string ");
        String str = strbuff.toString();
        array = str.toCharArray();
        int size = str.length();

        if (UTF16.countCodePoint(array, 0, 0) != 0) {
        errln("FAIL Counting code points for 0 offset array");
        }

        if (UTF16.countCodePoint(str) != size ||
        UTF16.countCodePoint(strbuff) != size ||
        UTF16.countCodePoint(array, 0, size) != size) {
        errln("FAIL Counting code points");
        }

        UTF16.append(strbuff, 0x10000);
        str = strbuff.toString();
        array = str.toCharArray();
        if (UTF16.countCodePoint(str) != size + 1 ||
        UTF16.countCodePoint(strbuff) != size + 1 ||
        UTF16.countCodePoint(array, 0, size + 1) != size + 1 ||
        UTF16.countCodePoint(array, 0, size + 2) != size + 1) {
        errln("FAIL Counting code points");
        }
        UTF16.append(strbuff, 0x61);
        str = strbuff.toString();
        array = str.toCharArray();
        if (UTF16.countCodePoint(str) != size + 2 ||
        UTF16.countCodePoint(strbuff) != size + 2 ||
        UTF16.countCodePoint(array, 0, size + 1) != size + 1 ||
        UTF16.countCodePoint(array, 0, size + 2) != size + 1 ||
        UTF16.countCodePoint(array, 0, size + 3) != size + 2) {
        errln("FAIL Counting code points");
        }
    }

    /**
     * Testing UTF16 class methods delete
     */
    @Test
    public void TestDelete()
    {                                        //01234567890123456
        StringBuffer strbuff = new StringBuffer("these are strings");
        int size = strbuff.length();
        char array[] = strbuff.toString().toCharArray();

        UTF16.delete(strbuff, 3);
        UTF16.delete(strbuff, 3);
        UTF16.delete(strbuff, 3);
        UTF16.delete(strbuff, 3);
        UTF16.delete(strbuff, 3);
        UTF16.delete(strbuff, 3);
        try {
        UTF16.delete(strbuff, strbuff.length());
        errln("FAIL deleting out of bounds character should fail");
        } catch (Exception e) {
        System.out.print("");
        }
        UTF16.delete(strbuff, strbuff.length() - 1);
        if (!strbuff.toString().equals("the string")) {
        errln("FAIL expected result after deleting characters is " +
          "\"the string\"");
        }

        size = UTF16.delete(array, size, 3);
        size = UTF16.delete(array, size, 3);
        size = UTF16.delete(array, size, 3);
        size = UTF16.delete(array, size, 3);
        size = UTF16.delete(array, size, 3);
        size = UTF16.delete(array, size, 3);
        try {
        UTF16.delete(array, size, size);
        errln("FAIL deleting out of bounds character should fail");
        } catch (Exception e) {
        System.out.print("");
        }
        size = UTF16.delete(array, size, size - 1);
        String str = new String(array, 0, size);
        if (!str.equals("the string")) {
        errln("FAIL expected result after deleting characters is " +
          "\"the string\"");
        }
    //012345678     9     01     2      3     4
        strbuff = new StringBuffer("string: \ud800\udc00 \ud801\udc01 \ud801\udc01");
        size = strbuff.length();
        array = strbuff.toString().toCharArray();

        UTF16.delete(strbuff, 8);
        UTF16.delete(strbuff, 8);
        UTF16.delete(strbuff, 9);
        UTF16.delete(strbuff, 8);
        UTF16.delete(strbuff, 9);
        UTF16.delete(strbuff, 6);
        UTF16.delete(strbuff, 6);
        if (!strbuff.toString().equals("string")) {
        errln("FAIL expected result after deleting characters is \"string\"");
        }

        size = UTF16.delete(array, size, 8);
        size = UTF16.delete(array, size, 8);
        size = UTF16.delete(array, size, 9);
        size = UTF16.delete(array, size, 8);
        size = UTF16.delete(array, size, 9);
        size = UTF16.delete(array, size, 6);
        size = UTF16.delete(array, size, 6);
        str = new String(array, 0, size);
        if (!str.equals("string")) {
        errln("FAIL expected result after deleting characters is \"string\"");
        }
    }

    /**
     * Testing findOffsetFromCodePoint and findCodePointOffset
     */
    @Test
    public void TestfindOffset()
    {
        // jitterbug 47
        String str = "a\uD800\uDC00b";
        StringBuffer strbuff = new StringBuffer(str);
        char array[] = str.toCharArray();
        int limit = str.length();
        if (UTF16.findCodePointOffset(str, 0) != 0 ||
        UTF16.findOffsetFromCodePoint(str, 0) != 0 ||
        UTF16.findCodePointOffset(strbuff, 0) != 0 ||
        UTF16.findOffsetFromCodePoint(strbuff, 0) != 0 ||
        UTF16.findCodePointOffset(array, 0, limit, 0) != 0 ||
        UTF16.findOffsetFromCodePoint(array, 0, limit, 0) != 0) {
        errln("FAIL Getting the first codepoint offset to a string with " +
          "supplementary characters");
        }
        if (UTF16.findCodePointOffset(str, 1) != 1 ||
        UTF16.findOffsetFromCodePoint(str, 1) != 1 ||
        UTF16.findCodePointOffset(strbuff, 1) != 1 ||
        UTF16.findOffsetFromCodePoint(strbuff, 1) != 1 ||
        UTF16.findCodePointOffset(array, 0, limit, 1) != 1 ||
        UTF16.findOffsetFromCodePoint(array, 0, limit, 1) != 1) {
        errln("FAIL Getting the second codepoint offset to a string with " +
          "supplementary characters");
        }
        if (UTF16.findCodePointOffset(str, 2) != 1 ||
        UTF16.findOffsetFromCodePoint(str, 2) != 3 ||
        UTF16.findCodePointOffset(strbuff, 2) != 1 ||
        UTF16.findOffsetFromCodePoint(strbuff, 2) != 3 ||
        UTF16.findCodePointOffset(array, 0, limit, 2) != 1 ||
        UTF16.findOffsetFromCodePoint(array, 0, limit, 2) != 3) {
        errln("FAIL Getting the third codepoint offset to a string with " +
          "supplementary characters");
        }
        if (UTF16.findCodePointOffset(str, 3) != 2 ||
        UTF16.findOffsetFromCodePoint(str, 3) != 4 ||
        UTF16.findCodePointOffset(strbuff, 3) != 2 ||
        UTF16.findOffsetFromCodePoint(strbuff, 3) != 4 ||
        UTF16.findCodePointOffset(array, 0, limit, 3) != 2 ||
        UTF16.findOffsetFromCodePoint(array, 0, limit, 3) != 4) {
        errln("FAIL Getting the last codepoint offset to a string with " +
          "supplementary characters");
        }
        if (UTF16.findCodePointOffset(str, 4) != 3 ||
        UTF16.findCodePointOffset(strbuff, 4) != 3 ||
        UTF16.findCodePointOffset(array, 0, limit, 4) != 3) {
        errln("FAIL Getting the length offset to a string with " +
          "supplementary characters");
        }
        try {
        UTF16.findCodePointOffset(str, 5);
        errln("FAIL Getting the a non-existence codepoint to a string " +
          "with supplementary characters");
        } catch (Exception e) {
        // this is a success
        logln("Passed out of bounds codepoint offset");
        }
        try {
        UTF16.findOffsetFromCodePoint(str, 4);
        errln("FAIL Getting the a non-existence codepoint to a string " +
          "with supplementary characters");
        } catch (Exception e) {
        // this is a success
        logln("Passed out of bounds codepoint offset");
        }
        try {
        UTF16.findCodePointOffset(strbuff, 5);
        errln("FAIL Getting the a non-existence codepoint to a string " +
          "with supplementary characters");
        } catch (Exception e) {
        // this is a success
        logln("Passed out of bounds codepoint offset");
        }
        try {
        UTF16.findOffsetFromCodePoint(strbuff, 4);
        errln("FAIL Getting the a non-existence codepoint to a string " +
          "with supplementary characters");
        } catch (Exception e) {
        // this is a success
        logln("Passed out of bounds codepoint offset");
        }
        try {
        UTF16.findCodePointOffset(array, 0, limit, 5);
        errln("FAIL Getting the a non-existence codepoint to a string " +
          "with supplementary characters");
        } catch (Exception e) {
        // this is a success
        logln("Passed out of bounds codepoint offset");
        }
        try {
        UTF16.findOffsetFromCodePoint(array, 0, limit, 4);
        errln("FAIL Getting the a non-existence codepoint to a string " +
          "with supplementary characters");
        } catch (Exception e) {
        // this is a success
        logln("Passed out of bounds codepoint offset");
        }

        if (UTF16.findCodePointOffset(array, 1, 3, 0) != 0 ||
        UTF16.findOffsetFromCodePoint(array, 1, 3, 0) != 0 ||
        UTF16.findCodePointOffset(array, 1, 3, 1) != 0 ||
        UTF16.findCodePointOffset(array, 1, 3, 2) != 1 ||
        UTF16.findOffsetFromCodePoint(array, 1, 3, 1) != 2) {
        errln("FAIL Getting valid codepoint offset in sub array");
        }
    }

    /**
     * Testing UTF16 class methods getCharCount, *Surrogate
     */
    @Test
    public void TestGetCharCountSurrogate()
    {
        if (UTF16.getCharCount(0x61) != 1 ||
        UTF16.getCharCount(0x10000) != 2) {
        errln("FAIL getCharCount result failure");
        }
        if (UTF16.getLeadSurrogate(0x61) != 0 ||
        UTF16.getTrailSurrogate(0x61) != 0x61 ||
        UTF16.isLeadSurrogate((char)0x61) ||
        UTF16.isTrailSurrogate((char)0x61) ||
        UTF16.getLeadSurrogate(0x10000) != 0xd800 ||
        UTF16.getTrailSurrogate(0x10000) != 0xdc00 ||
        UTF16.isLeadSurrogate((char)0xd800) != true ||
        UTF16.isTrailSurrogate((char)0xd800) ||
        UTF16.isLeadSurrogate((char)0xdc00) ||
        UTF16.isTrailSurrogate((char)0xdc00) != true) {
        errln("FAIL *Surrogate result failure");
        }

        if (UTF16.isSurrogate((char)0x61) || !UTF16.isSurrogate((char)0xd800)
            || !UTF16.isSurrogate((char)0xdc00)) {
        errln("FAIL isSurrogate result failure");
        }
    }

    /**
     * Testing UTF16 class method insert
     */
    @Test
    public void TestInsert()
    {
        StringBuffer strbuff = new StringBuffer("0123456789");
        char array[] = new char[128];
        int srcEnd = strbuff.length();
        if (0 != srcEnd) {
            strbuff.getChars(0, srcEnd, array, 0);
        }
        int length = 10;
        UTF16.insert(strbuff, 5, 't');
        UTF16.insert(strbuff, 5, 's');
        UTF16.insert(strbuff, 5, 'e');
        UTF16.insert(strbuff, 5, 't');
        if (!(strbuff.toString().equals("01234test56789"))) {
        errln("FAIL inserting \"test\"");
        }
        length = UTF16.insert(array, length, 5, 't');
        length = UTF16.insert(array, length, 5, 's');
        length = UTF16.insert(array, length, 5, 'e');
        length = UTF16.insert(array, length, 5, 't');
        String str = new String(array, 0, length);
        if (!(str.equals("01234test56789"))) {
        errln("FAIL inserting \"test\"");
        }
        UTF16.insert(strbuff, 0, 0x10000);
        UTF16.insert(strbuff, 11, 0x10000);
        UTF16.insert(strbuff, strbuff.length(), 0x10000);
        if (!(strbuff.toString().equals(
                    "\ud800\udc0001234test\ud800\udc0056789\ud800\udc00"))) {
        errln("FAIL inserting supplementary characters");
        }
        length = UTF16.insert(array, length, 0, 0x10000);
        length = UTF16.insert(array, length, 11, 0x10000);
        length = UTF16.insert(array, length, length, 0x10000);
        str = new String(array, 0, length);
        if (!(str.equals(
             "\ud800\udc0001234test\ud800\udc0056789\ud800\udc00"))) {
        errln("FAIL inserting supplementary characters");
        }

        try {
        UTF16.insert(strbuff, -1, 0);
        errln("FAIL invalid insertion offset");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.insert(strbuff, 64, 0);
        errln("FAIL invalid insertion offset");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.insert(array, length, -1, 0);
        errln("FAIL invalid insertion offset");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.insert(array, length, 64, 0);
        errln("FAIL invalid insertion offset");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        // exceeded array size
        UTF16.insert(array, array.length, 64, 0);
        errln("FAIL invalid insertion offset");
        } catch (Exception e) {
        System.out.print("");
        }
    }

    /*
     * Testing moveCodePointOffset APIs
     */

    //
    //   checkMoveCodePointOffset
    //      Run a single test case through each of the moveCodePointOffset() functions.
    //          Parameters -
    //              s               The string to work in.
    //              startIdx        The starting position within the string.
    //              amount          The number of code points to move.
    //              expectedResult  The string index after the move, or -1 if the
    //                              function should throw an exception.
    private void checkMoveCodePointOffset(String s, int startIdx, int amount, int expectedResult) {
        // Test with the String flavor of moveCodePointOffset
        try {
            int result = UTF16.moveCodePointOffset(s, startIdx, amount);
            if (result != expectedResult) {
                errln("FAIL: UTF16.moveCodePointOffset(String \"" + s + "\", " + startIdx + ", " + amount + ")" +
                        " returned "  + result + ", expected result was " +
                        (expectedResult==-1 ? "exception" : Integer.toString(expectedResult)));
            }
        }
        catch (IndexOutOfBoundsException e) {
            if (expectedResult != -1) {
                errln("FAIL: UTF16.moveCodePointOffset(String \"" + s + "\", " + startIdx + ", " + amount + ")" +
                        " returned exception" + ", expected result was " + expectedResult);
            }
        }

        // Test with the StringBuffer flavor of moveCodePointOffset
        StringBuffer sb = new StringBuffer(s);
        try {
            int result = UTF16.moveCodePointOffset(sb, startIdx, amount);
            if (result != expectedResult) {
                errln("FAIL: UTF16.moveCodePointOffset(StringBuffer \"" + s + "\", " + startIdx + ", " + amount + ")" +
                        " returned "  + result + ", expected result was " +
                        (expectedResult==-1 ? "exception" : Integer.toString(expectedResult)));
            }
        }
        catch (IndexOutOfBoundsException e) {
            if (expectedResult != -1) {
                errln("FAIL: UTF16.moveCodePointOffset(StringBuffer \"" + s + "\", " + startIdx + ", " + amount + ")" +
                        " returned exception" + ", expected result was " + expectedResult);
            }
        }

        // Test with the char[] flavor of moveCodePointOffset
        char ca[] = s.toCharArray();
        try {
            int result = UTF16.moveCodePointOffset(ca, 0, s.length(), startIdx, amount);
            if (result != expectedResult) {
                errln("FAIL: UTF16.moveCodePointOffset(char[] \"" + s + "\", 0, " + s.length()
                        + ", " + startIdx + ", " + amount + ")" +
                        " returned "  + result + ", expected result was " +
                        (expectedResult==-1 ? "exception" : Integer.toString(expectedResult)));
            }
        }
        catch (IndexOutOfBoundsException e) {
            if (expectedResult != -1) {
                errln("FAIL: UTF16.moveCodePointOffset(char[] \"" + s + "\", 0, " + s.length()
                        + ", " + startIdx + ", " + amount + ")" +
                        " returned exception" + ", expected result was " + expectedResult);
            }
        }

        // Put the test string into the interior of a char array,
        //   run test on the subsection of the array.
        char ca2[] = new char[s.length()+2];
        ca2[0] = (char)0xd800;
        ca2[s.length()+1] = (char)0xd8ff;
        s.getChars(0, s.length(), ca2, 1);
        try {
            int result = UTF16.moveCodePointOffset(ca2, 1, s.length()+1, startIdx, amount);
            if (result != expectedResult) {
                errln("UTF16.moveCodePointOffset(char[] \"" + "." + s + ".\", 1, " + (s.length()+1)
                        + ", " + startIdx + ", " + amount + ")" +
                         " returned "  + result + ", expected result was " +
                        (expectedResult==-1 ? "exception" : Integer.toString(expectedResult)));
            }
        }
        catch (IndexOutOfBoundsException e) {
            if (expectedResult != -1) {
                errln("UTF16.moveCodePointOffset(char[] \"" + "." + s + ".\", 1, " + (s.length()+1)
                        + ", " + startIdx + ", " + amount + ")" +
                        " returned exception" + ", expected result was " + expectedResult);
            }
        }

    }


    @Test
    public void TestMoveCodePointOffset()
    {
        // checkMoveCodePointOffset(String, startIndex, amount, expected );  expected=-1 for exception.

        // No Supplementary chars
        checkMoveCodePointOffset("abc", 1,  1, 2);
        checkMoveCodePointOffset("abc", 1, -1, 0);
        checkMoveCodePointOffset("abc", 1, -2, -1);
        checkMoveCodePointOffset("abc", 1,  2, 3);
        checkMoveCodePointOffset("abc", 1,  3, -1);
        checkMoveCodePointOffset("abc", 1,  0, 1);

        checkMoveCodePointOffset("abc", 3, 0, 3);
        checkMoveCodePointOffset("abc", 4, 0, -1);
        checkMoveCodePointOffset("abc", 0, 0, 0);
        checkMoveCodePointOffset("abc", -1, 0, -1);

        checkMoveCodePointOffset("", 0, 0, 0);
        checkMoveCodePointOffset("", 0, -1, -1);
        checkMoveCodePointOffset("", 0, 1, -1);

        checkMoveCodePointOffset("a", 0, 0, 0);
        checkMoveCodePointOffset("a", 1, 0, 1);
        checkMoveCodePointOffset("a", 0, 1, 1);
        checkMoveCodePointOffset("a", 1, -1, 0);


        // Supplementary in middle of string
        checkMoveCodePointOffset("a\ud800\udc00b", 0, 1, 1);
        checkMoveCodePointOffset("a\ud800\udc00b", 0, 2, 3);
        checkMoveCodePointOffset("a\ud800\udc00b", 0, 3, 4);
        checkMoveCodePointOffset("a\ud800\udc00b", 0, 4, -1);

        checkMoveCodePointOffset("a\ud800\udc00b", 4, -1, 3);
        checkMoveCodePointOffset("a\ud800\udc00b", 4, -2, 1);
        checkMoveCodePointOffset("a\ud800\udc00b", 4, -3, 0);
        checkMoveCodePointOffset("a\ud800\udc00b", 4, -4, -1);

        // Supplementary at start of string
        checkMoveCodePointOffset("\ud800\udc00ab", 0, 1, 2);
        checkMoveCodePointOffset("\ud800\udc00ab", 1, 1, 2);
        checkMoveCodePointOffset("\ud800\udc00ab", 2, 1, 3);
        checkMoveCodePointOffset("\ud800\udc00ab", 2, -1, 0);
        checkMoveCodePointOffset("\ud800\udc00ab", 1, -1, 0);
        checkMoveCodePointOffset("\ud800\udc00ab", 0, -1, -1);


        // Supplementary at end of string
        checkMoveCodePointOffset("ab\ud800\udc00", 1, 1, 2);
        checkMoveCodePointOffset("ab\ud800\udc00", 2, 1, 4);
        checkMoveCodePointOffset("ab\ud800\udc00", 3, 1, 4);
        checkMoveCodePointOffset("ab\ud800\udc00", 4, 1, -1);

        checkMoveCodePointOffset("ab\ud800\udc00", 5, -2, -1);
        checkMoveCodePointOffset("ab\ud800\udc00", 4, -1, 2);
        checkMoveCodePointOffset("ab\ud800\udc00", 3, -1, 2);
        checkMoveCodePointOffset("ab\ud800\udc00", 2, -1, 1);
        checkMoveCodePointOffset("ab\ud800\udc00", 1, -1, 0);

        // Unpaired surrogate in middle
        checkMoveCodePointOffset("a\ud800b", 0, 1, 1);
        checkMoveCodePointOffset("a\ud800b", 1, 1, 2);
        checkMoveCodePointOffset("a\ud800b", 2, 1, 3);

        checkMoveCodePointOffset("a\udc00b", 0, 1, 1);
        checkMoveCodePointOffset("a\udc00b", 1, 1, 2);
        checkMoveCodePointOffset("a\udc00b", 2, 1, 3);

        checkMoveCodePointOffset("a\udc00\ud800b", 0, 1, 1);
        checkMoveCodePointOffset("a\udc00\ud800b", 1, 1, 2);
        checkMoveCodePointOffset("a\udc00\ud800b", 2, 1, 3);
        checkMoveCodePointOffset("a\udc00\ud800b", 3, 1, 4);

        checkMoveCodePointOffset("a\ud800b", 1, -1, 0);
        checkMoveCodePointOffset("a\ud800b", 2, -1, 1);
        checkMoveCodePointOffset("a\ud800b", 3, -1, 2);

        checkMoveCodePointOffset("a\udc00b", 1, -1, 0);
        checkMoveCodePointOffset("a\udc00b", 2, -1, 1);
        checkMoveCodePointOffset("a\udc00b", 3, -1, 2);

        checkMoveCodePointOffset("a\udc00\ud800b", 1, -1, 0);
        checkMoveCodePointOffset("a\udc00\ud800b", 2, -1, 1);
        checkMoveCodePointOffset("a\udc00\ud800b", 3, -1, 2);
        checkMoveCodePointOffset("a\udc00\ud800b", 4, -1, 3);

        // Unpaired surrogate at start
        checkMoveCodePointOffset("\udc00ab", 0, 1, 1);
        checkMoveCodePointOffset("\ud800ab", 0, 2, 2);
        checkMoveCodePointOffset("\ud800\ud800ab", 0, 3, 3);
        checkMoveCodePointOffset("\udc00\udc00ab", 0, 4, 4);

        checkMoveCodePointOffset("\udc00ab", 2, -1, 1);
        checkMoveCodePointOffset("\ud800ab", 1, -1, 0);
        checkMoveCodePointOffset("\ud800ab", 1, -2, -1);
        checkMoveCodePointOffset("\ud800\ud800ab", 2, -1, 1);
        checkMoveCodePointOffset("\udc00\udc00ab", 2, -2, 0);
        checkMoveCodePointOffset("\udc00\udc00ab", 2, -3, -1);

        // Unpaired surrogate at end
        checkMoveCodePointOffset("ab\udc00\udc00ab", 3, 1, 4);
        checkMoveCodePointOffset("ab\udc00\udc00ab", 2, 1, 3);
        checkMoveCodePointOffset("ab\udc00\udc00ab", 1, 1, 2);

        checkMoveCodePointOffset("ab\udc00\udc00ab", 4, -1, 3);
        checkMoveCodePointOffset("ab\udc00\udc00ab", 3, -1, 2);
        checkMoveCodePointOffset("ab\udc00\udc00ab", 2, -1, 1);


                               //01234567890     1     2     3     45678901234
        String str = new String("0123456789\ud800\udc00\ud801\udc010123456789");
        int move1[] = { 1,  2,  3,  4,  5,  6,  7,  8,  9, 10,
                       12, 12, 14, 14, 15, 16, 17, 18, 19, 20,
                       21, 22, 23, 24};
        int move2[] = { 2,  3,  4,  5,  6,  7,  8,  9, 10, 12,
                       14, 14, 15, 15, 16, 17, 18, 19, 20, 21,
                       22, 23, 24, -1};
        int move3[] = { 3,  4,  5,  6,  7,  8,  9, 10, 12, 14,
                       15, 15, 16, 16, 17, 18, 19, 20, 21, 22,
                       23, 24, -1, -1};
        int size = str.length();
        for (int i = 0; i < size; i ++) {
            checkMoveCodePointOffset(str, i, 1, move1[i]);
            checkMoveCodePointOffset(str, i, 2, move2[i]);
            checkMoveCodePointOffset(str, i, 3, move3[i]);
        }

        char strarray[] = str.toCharArray();
        if (UTF16.moveCodePointOffset(strarray, 9, 13, 0, 2) != 3) {
            errln("FAIL: Moving offset 0 by 2 codepoint in subarray [9, 13] " +
            "expected result 3");
        }
        if (UTF16.moveCodePointOffset(strarray, 9, 13, 1, 2) != 4) {
            errln("FAIL: Moving offset 1 by 2 codepoint in subarray [9, 13] " +
            "expected result 4");
        }
        if (UTF16.moveCodePointOffset(strarray, 11, 14, 0, 2) != 3) {
            errln("FAIL: Moving offset 0 by 2 codepoint in subarray [11, 14] "
                    + "expected result 3");
        }
    }

    /**
     * Testing UTF16 class methods setCharAt
     */
    @Test
    public void TestSetCharAt()
    {
        StringBuffer strbuff = new StringBuffer("012345");
        char array[] = new char[128];
        int srcEnd = strbuff.length();
        if (0 != srcEnd) {
            strbuff.getChars(0, srcEnd, array, 0);
        }
        int length = 6;
        for (int i = 0; i < length; i ++) {
        UTF16.setCharAt(strbuff, i, '0');
        UTF16.setCharAt(array, length, i, '0');
        }
        String str = new String(array, 0, length);
        if (!(strbuff.toString().equals("000000")) ||
        !(str.equals("000000"))) {
        errln("FAIL: setChar to '0' failed");
        }
        UTF16.setCharAt(strbuff, 0, 0x10000);
        UTF16.setCharAt(strbuff, 4, 0x10000);
        UTF16.setCharAt(strbuff, 7, 0x10000);
        if (!(strbuff.toString().equals(
                    "\ud800\udc0000\ud800\udc000\ud800\udc00"))) {
        errln("FAIL: setChar to 0x10000 failed");
        }
        length = UTF16.setCharAt(array, length, 0, 0x10000);
        length = UTF16.setCharAt(array, length, 4, 0x10000);
        length = UTF16.setCharAt(array, length, 7, 0x10000);
        str = new String(array, 0, length);
        if (!(str.equals("\ud800\udc0000\ud800\udc000\ud800\udc00"))) {
        errln("FAIL: setChar to 0x10000 failed");
        }
        UTF16.setCharAt(strbuff, 0, '0');
        UTF16.setCharAt(strbuff, 1, '1');
        UTF16.setCharAt(strbuff, 2, '2');
        UTF16.setCharAt(strbuff, 4, '3');
        UTF16.setCharAt(strbuff, 4, '4');
        UTF16.setCharAt(strbuff, 5, '5');
        if (!strbuff.toString().equals("012345")) {
        errln("Fail converting supplementaries in StringBuffer to BMP " +
          "characters");
        }
        length = UTF16.setCharAt(array, length, 0, '0');
        length = UTF16.setCharAt(array, length, 1, '1');
        length = UTF16.setCharAt(array, length, 2, '2');
        length = UTF16.setCharAt(array, length, 4, '3');
        length = UTF16.setCharAt(array, length, 4, '4');
        length = UTF16.setCharAt(array, length, 5, '5');
        str = new String(array, 0, length);
        if (!str.equals("012345")) {
        errln("Fail converting supplementaries in array to BMP " +
          "characters");
        }
        try {
        UTF16.setCharAt(strbuff, -1, 0);
        errln("FAIL: setting character at invalid offset");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.setCharAt(array, length, -1, 0);
        errln("FAIL: setting character at invalid offset");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.setCharAt(strbuff, length, 0);
        errln("FAIL: setting character at invalid offset");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.setCharAt(array, length, length, 0);
        errln("FAIL: setting character at invalid offset");
        } catch (Exception e) {
        System.out.print("");
        }
    }

    /**
     * Testing UTF16 valueof APIs
     */
    @Test
    public void TestValueOf()
    {
        if(UCharacter.getCodePoint('\ud800','\udc00')!=0x10000){
            errln("FAIL: getCodePoint('\ud800','\udc00')");
        }
        if (!UTF16.valueOf(0x61).equals("a") ||
        !UTF16.valueOf(0x10000).equals("\ud800\udc00")) {
        errln("FAIL: valueof(char32)");
        }
        String str = new String("01234\ud800\udc0056789");
        StringBuffer strbuff = new StringBuffer(str);
        char array[] = str.toCharArray();
        int length = str.length();

        String expected[] = {"0", "1", "2", "3", "4", "\ud800\udc00",
                 "\ud800\udc00", "5", "6", "7", "8", "9"};
        for (int i = 0; i < length; i ++) {
        if (!UTF16.valueOf(str, i).equals(expected[i]) ||
                !UTF16.valueOf(strbuff, i).equals(expected[i]) ||
                !UTF16.valueOf(array, 0, length, i).equals(expected[i])) {
                errln("FAIL: valueOf() expected " + expected[i]);
        }
        }
        try {
        UTF16.valueOf(str, -1);
        errln("FAIL: out of bounds error expected");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.valueOf(strbuff, -1);
        errln("FAIL: out of bounds error expected");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.valueOf(array, 0, length, -1);
        errln("FAIL: out of bounds error expected");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.valueOf(str, length);
        errln("FAIL: out of bounds error expected");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.valueOf(strbuff, length);
        errln("FAIL: out of bounds error expected");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.valueOf(array, 0, length, length);
        errln("FAIL: out of bounds error expected");
        } catch (Exception e) {
        System.out.print("");
        }
        if (!UTF16.valueOf(array, 6, length, 0).equals("\udc00") ||
        !UTF16.valueOf(array, 0, 6, 5).equals("\ud800")) {
        errln("FAIL: error getting partial supplementary character");
        }
        try {
        UTF16.valueOf(array, 3, 5, -1);
        errln("FAIL: out of bounds error expected");
        } catch (Exception e) {
        System.out.print("");
        }
        try {
        UTF16.valueOf(array, 3, 5, 3);
        errln("FAIL: out of bounds error expected");
        } catch (Exception e) {
        System.out.print("");
        }
    }

    @Test
    public void TestIndexOf()
    {
    //012345678901234567890123456789012345
        String test1     = "test test ttest tetest testesteststt";
        String test2     = "test";
        int    testChar1 = 0x74;
        int    testChar2 = 0x20402;
        // int    testChar3 = 0xdc02;
        // int    testChar4 = 0xd841;
        String test3     = "\ud841\udc02\u0071\udc02\ud841\u0071\ud841\udc02\u0071\u0072\ud841\udc02\u0071\ud841\udc02\u0071\udc02\ud841\u0073";
        String test4     = UCharacter.toString(testChar2);

        if (UTF16.indexOf(test1, test2) != 0 ||
            UTF16.indexOf(test1, test2, 0) != 0) {
            errln("indexOf failed: expected to find '" + test2 +
                  "' at position 0 in text '" + test1 + "'");
        }
        if (UTF16.indexOf(test1, testChar1) != 0 ||
            UTF16.indexOf(test1, testChar1, 0) != 0) {
            errln("indexOf failed: expected to find 0x" +
                  Integer.toHexString(testChar1) +
                  " at position 0 in text '" + test1 + "'");
        }
        if (UTF16.indexOf(test3, testChar2) != 0 ||
            UTF16.indexOf(test3, testChar2, 0) != 0) {
            errln("indexOf failed: expected to find 0x" +
                  Integer.toHexString(testChar2) +
                  " at position 0 in text '" + Utility.hex(test3) + "'");
        }
        String test5 = "\ud841\ud841\udc02";
        if (UTF16.indexOf(test5, testChar2) != 1 ||
            UTF16.indexOf(test5, testChar2, 0) != 1) {
            errln("indexOf failed: expected to find 0x" +
                  Integer.toHexString(testChar2) +
                  " at position 0 in text '" + Utility.hex(test3) + "'");
        }
        if (UTF16.lastIndexOf(test1, test2) != 29 ||
            UTF16.lastIndexOf(test1, test2, test1.length()) != 29) {
            errln("lastIndexOf failed: expected to find '" + test2 +
                  "' at position 29 in text '" + test1 + "'");
        }
        if (UTF16.lastIndexOf(test1, testChar1) != 35 ||
            UTF16.lastIndexOf(test1, testChar1, test1.length()) != 35) {
            errln("lastIndexOf failed: expected to find 0x" +
                  Integer.toHexString(testChar1) +
                  " at position 35 in text '" + test1 + "'");
        }
        if (UTF16.lastIndexOf(test3, testChar2) != 13 ||
            UTF16.lastIndexOf(test3, testChar2, test3.length()) != 13) {
            errln("indexOf failed: expected to find 0x" +
                  Integer.toHexString(testChar2) +
                  " at position 13 in text '" + Utility.hex(test3) + "'");
        }
        int occurrences = 0;
        for (int startPos = 0; startPos != -1 && startPos < test1.length();)
        {
        startPos = UTF16.indexOf(test1, test2, startPos);
        if (startPos >= 0) {
            ++ occurrences;
            startPos += 4;
        }
        }
        if (occurrences != 6) {
            errln("indexOf failed: expected to find 6 occurrences, found "
                  + occurrences);
        }

        occurrences = 0;
        for (int startPos = 10; startPos != -1 && startPos < test1.length();)
        {
        startPos = UTF16.indexOf(test1, test2, startPos);
        if (startPos >= 0) {
            ++ occurrences;
            startPos += 4;
        }
        }
        if (occurrences != 4) {
            errln("indexOf with starting offset failed: expected to find 4 occurrences, found "
                  + occurrences);
        }

        occurrences = 0;
        for (int startPos = 0;
         startPos != -1 && startPos < test3.length();) {
            startPos = UTF16.indexOf(test3, test4, startPos);
            if (startPos != -1) {
                ++ occurrences;
                startPos += 2;
            }
        }
        if (occurrences != 4) {
            errln("indexOf failed: expected to find 4 occurrences, found "
          + occurrences);
        }

        occurrences = 0;
        for (int startPos = 10;
             startPos != -1 && startPos < test3.length();) {
            startPos = UTF16.indexOf(test3, test4, startPos);
            if (startPos != -1) {
                ++ occurrences;
                startPos += 2;
            }
        }
        if (occurrences != 2) {
            errln("indexOf failed: expected to find 2 occurrences, found "
                  + occurrences);
        }

        occurrences = 0;
        for (int startPos = 0;
         startPos != -1 && startPos < test1.length();) {
            startPos = UTF16.indexOf(test1, testChar1, startPos);
            if (startPos != -1) {
                ++ occurrences;
                startPos += 1;
            }
        }
        if (occurrences != 16) {
            errln("indexOf with character failed: expected to find 16 occurrences, found "
                  + occurrences);
        }

        occurrences = 0;
        for (int startPos = 10;
         startPos != -1 && startPos < test1.length();) {
            startPos = UTF16.indexOf(test1, testChar1, startPos);
            if (startPos != -1) {
                ++ occurrences;
                startPos += 1;
            }
        }
        if (occurrences != 12) {
            errln("indexOf with character & start offset failed: expected to find 12 occurrences, found "
          + occurrences);
        }

        occurrences = 0;
        for (int startPos = 0;
         startPos != -1 && startPos < test3.length();) {
            startPos = UTF16.indexOf(test3, testChar2, startPos);
            if (startPos != -1) {
                ++ occurrences;
                startPos += 1;
            }
        }
        if (occurrences != 4) {
            errln("indexOf failed: expected to find 4 occurrences, found "
                  + occurrences);
        }

        occurrences = 0;
        for (int startPos = 5; startPos != -1 && startPos < test3.length();) {
            startPos = UTF16.indexOf(test3, testChar2, startPos);
            if (startPos != -1) {
                ++ occurrences;
                startPos += 1;
            }
        }
        if (occurrences != 3) {
            errln("indexOf with character & start & end offsets failed: expected to find 2 occurrences, found "
          + occurrences);
        }
        occurrences = 0;
        for (int startPos = 32; startPos != -1;) {
            startPos = UTF16.lastIndexOf(test1, test2, startPos);
            if (startPos != -1) {
                ++ occurrences;
                startPos -= 5;
            }
        }
        if (occurrences != 6) {
            errln("lastIndexOf with starting and ending offsets failed: expected to find 4 occurrences, found "
                  + occurrences);
        }
        occurrences = 0;
        for (int startPos = 32; startPos != -1;) {
            startPos = UTF16.lastIndexOf(test1, testChar1, startPos);
            if (startPos != -1) {
                ++ occurrences;
                startPos -= 5;
            }
        }
        if (occurrences != 7) {
            errln("lastIndexOf with character & start & end offsets failed: expected to find 11 occurrences, found "
          + occurrences);
        }

        //testing UChar32
        occurrences = 0;
        for (int startPos = test3.length(); startPos != -1;) {
            startPos = UTF16.lastIndexOf(test3, testChar2, startPos - 5);
            if (startPos != -1) {
                ++ occurrences;
            }
        }
        if (occurrences != 3) {
            errln("lastIndexOf with character & start & end offsets failed: expected to find 3 occurrences, found "
          + occurrences);
        }

        // testing supplementary
        for (int i = 0; i < INDEXOF_SUPPLEMENTARY_CHAR_.length; i ++) {
        int ch = INDEXOF_SUPPLEMENTARY_CHAR_[i];
        for (int j = 0; j < INDEXOF_SUPPLEMENTARY_CHAR_INDEX_[i].length;
         j ++) {
        int index = 0;
        int expected = INDEXOF_SUPPLEMENTARY_CHAR_INDEX_[i][j];
        if  (j > 0) {
            index = INDEXOF_SUPPLEMENTARY_CHAR_INDEX_[i][j - 1] + 1;
        }
        if (UTF16.indexOf(INDEXOF_SUPPLEMENTARY_STRING_, ch, index) !=
            expected ||
            UTF16.indexOf(INDEXOF_SUPPLEMENTARY_STRING_,
                  UCharacter.toString(ch), index) !=
            expected) {
            errln("Failed finding index for supplementary 0x" +
              Integer.toHexString(ch));
        }
        index = INDEXOF_SUPPLEMENTARY_STRING_.length();
        if (j < INDEXOF_SUPPLEMENTARY_CHAR_INDEX_[i].length - 1) {
            index = INDEXOF_SUPPLEMENTARY_CHAR_INDEX_[i][j + 1] - 1;
        }
        if (UTF16.lastIndexOf(INDEXOF_SUPPLEMENTARY_STRING_, ch,
                      index) != expected ||
            UTF16.lastIndexOf(INDEXOF_SUPPLEMENTARY_STRING_,
                      UCharacter.toString(ch), index)
            != expected)
            {
            errln("Failed finding last index for supplementary 0x" +
                  Integer.toHexString(ch));
            }
        }
        }

        for (int i = 0; i < INDEXOF_SUPPLEMENTARY_STR_INDEX_.length; i ++) {
        int index = 0;
        int expected = INDEXOF_SUPPLEMENTARY_STR_INDEX_[i];
        if  (i > 0) {
        index = INDEXOF_SUPPLEMENTARY_STR_INDEX_[i - 1] + 1;
        }
        if (UTF16.indexOf(INDEXOF_SUPPLEMENTARY_STRING_,
                  INDEXOF_SUPPLEMENTARY_STR_, index) != expected) {
        errln("Failed finding index for supplementary string " +
              hex(INDEXOF_SUPPLEMENTARY_STRING_));
        }
        index = INDEXOF_SUPPLEMENTARY_STRING_.length();
        if (i < INDEXOF_SUPPLEMENTARY_STR_INDEX_.length - 1) {
        index = INDEXOF_SUPPLEMENTARY_STR_INDEX_[i + 1] - 1;
        }
        if (UTF16.lastIndexOf(INDEXOF_SUPPLEMENTARY_STRING_,
                              INDEXOF_SUPPLEMENTARY_STR_, index) != expected) {
        errln("Failed finding last index for supplementary string " +
              hex(INDEXOF_SUPPLEMENTARY_STRING_));
        }
        }
    }

    @Test
    public void TestReplace()
    {
        String test1 = "One potato, two potato, three potato, four\n";
        String test2 = "potato";
        String test3 = "MISSISSIPPI";

        String result = UTF16.replace(test1, test2, test3);
        String expectedValue =
            "One MISSISSIPPI, two MISSISSIPPI, three MISSISSIPPI, four\n";
        if (!result.equals(expectedValue)) {
            errln("findAndReplace failed: expected \"" + expectedValue +
                  "\", got \"" + test1 + "\".");
        }
        result = UTF16.replace(test1, test3, test2);
        expectedValue = test1;
        if (!result.equals(expectedValue)) {
            errln("findAndReplace failed: expected \"" + expectedValue +
                  "\", got \"" + test1 + "\".");
        }

        result = UTF16.replace(test1, ',', 'e');
        expectedValue = "One potatoe two potatoe three potatoe four\n";
        if (!result.equals(expectedValue)) {
            errln("findAndReplace failed: expected \"" + expectedValue +
                  "\", got \"" + test1 + "\".");
        }

        result = UTF16.replace(test1, ',', 0x10000);
        expectedValue = "One potato\ud800\udc00 two potato\ud800\udc00 three potato\ud800\udc00 four\n";
        if (!result.equals(expectedValue)) {
            errln("findAndReplace failed: expected \"" + expectedValue +
                  "\", got \"" + test1 + "\".");
        }

        result = UTF16.replace(test1, "potato", "\ud800\udc00\ud801\udc01");
        expectedValue = "One \ud800\udc00\ud801\udc01, two \ud800\udc00\ud801\udc01, three \ud800\udc00\ud801\udc01, four\n";
        if (!result.equals(expectedValue)) {
            errln("findAndReplace failed: expected \"" + expectedValue +
                  "\", got \"" + test1 + "\".");
        }

        String test4 = "\ud800\ud800\udc00\ud800\udc00\udc00\ud800\ud800\udc00\ud800\udc00\udc00";
        result = UTF16.replace(test4, 0xd800, 'A');
        expectedValue = "A\ud800\udc00\ud800\udc00\udc00A\ud800\udc00\ud800\udc00\udc00";
        if (!result.equals(expectedValue)) {
            errln("findAndReplace failed: expected \"" + expectedValue +
                  "\", got \"" + test1 + "\".");
        }

        result = UTF16.replace(test4, 0xdC00, 'A');
        expectedValue = "\ud800\ud800\udc00\ud800\udc00A\ud800\ud800\udc00\ud800\udc00A";
        if (!result.equals(expectedValue)) {
            errln("findAndReplace failed: expected \"" + expectedValue +
                  "\", got \"" + test1 + "\".");
        }

        result = UTF16.replace(test4, 0x10000, 'A');
        expectedValue = "\ud800AA\udc00\ud800AA\udc00";
        if (!result.equals(expectedValue)) {
            errln("findAndReplace failed: expected \"" + expectedValue +
                  "\", got \"" + test1 + "\".");
        }
    }

    @Test
    public void TestReverse()
    {
        StringBuffer test = new StringBuffer(
                         "backwards words say to used I");

        StringBuffer result = UTF16.reverse(test);
        if (!result.toString().equals("I desu ot yas sdrow sdrawkcab")) {
            errln("reverse() failed:  Expected \"I desu ot yas sdrow sdrawkcab\",\n got \""
          + result + "\"");
        }
        StringBuffer testbuffer = new StringBuffer();
        UTF16.append(testbuffer, 0x2f999);
        UTF16.append(testbuffer, 0x1d15f);
        UTF16.append(testbuffer, 0x00c4);
        UTF16.append(testbuffer, 0x1ed0);
        result = UTF16.reverse(testbuffer);
        if (result.charAt(0) != 0x1ed0 ||
            result.charAt(1) != 0xc4 ||
            UTF16.charAt(result, 2) != 0x1d15f ||
            UTF16.charAt(result, 4)!=0x2f999) {
            errln("reverse() failed with supplementary characters");
        }
    }

    /**
     * Testing the setter and getter apis for StringComparator
     */
    @Test
    public void TestStringComparator()
    {
        UTF16.StringComparator compare = new UTF16.StringComparator();
        if (compare.getCodePointCompare() != false) {
            errln("Default string comparator should be code unit compare");
        }
        if (compare.getIgnoreCase() != false) {
            errln("Default string comparator should be case sensitive compare");
        }
        if (compare.getIgnoreCaseOption()
            != UTF16.StringComparator.FOLD_CASE_DEFAULT) {
            errln("Default string comparator should have fold case default compare");
        }
        compare.setCodePointCompare(true);
        if (compare.getCodePointCompare() != true) {
            errln("Error setting code point compare");
        }
        compare.setCodePointCompare(false);
        if (compare.getCodePointCompare() != false) {
            errln("Error setting code point compare");
        }
        compare.setIgnoreCase(true, UTF16.StringComparator.FOLD_CASE_DEFAULT);
        if (compare.getIgnoreCase() != true
            || compare.getIgnoreCaseOption()
        != UTF16.StringComparator.FOLD_CASE_DEFAULT) {
            errln("Error setting ignore case and options");
        }
        compare.setIgnoreCase(false, UTF16.StringComparator.FOLD_CASE_EXCLUDE_SPECIAL_I);
        if (compare.getIgnoreCase() != false
            || compare.getIgnoreCaseOption()
        != UTF16.StringComparator.FOLD_CASE_EXCLUDE_SPECIAL_I) {
            errln("Error setting ignore case and options");
        }
        compare.setIgnoreCase(true, UTF16.StringComparator.FOLD_CASE_EXCLUDE_SPECIAL_I);
        if (compare.getIgnoreCase() != true
            || compare.getIgnoreCaseOption()
        != UTF16.StringComparator.FOLD_CASE_EXCLUDE_SPECIAL_I) {
            errln("Error setting ignore case and options");
        }
        compare.setIgnoreCase(false, UTF16.StringComparator.FOLD_CASE_DEFAULT);
        if (compare.getIgnoreCase() != false
            || compare.getIgnoreCaseOption()
        != UTF16.StringComparator.FOLD_CASE_DEFAULT) {
            errln("Error setting ignore case and options");
        }
    }

    @Test
    public void TestCodePointCompare()
    {
        // these strings are in ascending order
        String str[] = {"\u0061", "\u20ac\ud801", "\u20ac\ud800\udc00",
                        "\ud800", "\ud800\uff61", "\udfff",
                        "\uff61\udfff", "\uff61\ud800\udc02", "\ud800\udc02",
                        "\ud84d\udc56"};
        UTF16.StringComparator cpcompare
            = new UTF16.StringComparator(true, false,
                     UTF16.StringComparator.FOLD_CASE_DEFAULT);
        UTF16.StringComparator cucompare
            = new UTF16.StringComparator();
        for (int i = 0; i < str.length - 1; ++ i) {
            if (cpcompare.compare(str[i], str[i + 1]) >= 0) {
                errln("error: compare() in code point order fails for string "
                      + Utility.hex(str[i]) + " and "
                      + Utility.hex(str[i + 1]));
            }
            // test code unit compare
            if (cucompare.compare(str[i], str[i + 1])
                != str[i].compareTo(str[i + 1])) {
                errln("error: compare() in code unit order fails for string "
                      + Utility.hex(str[i]) + " and "
                      + Utility.hex(str[i + 1]));
            }
        }
    }

    @Test
    public void TestCaseCompare()
    {
        String mixed = "\u0061\u0042\u0131\u03a3\u00df\ufb03\ud93f\udfff";
        String otherDefault = "\u0041\u0062\u0131\u03c3\u0073\u0053\u0046\u0066\u0049\ud93f\udfff";
        String otherExcludeSpecialI = "\u0041\u0062\u0131\u03c3\u0053\u0073\u0066\u0046\u0069\ud93f\udfff";
        String different = "\u0041\u0062\u0131\u03c3\u0073\u0053\u0046\u0066\u0049\ud93f\udffd";

        UTF16.StringComparator compare = new UTF16.StringComparator();
        compare.setIgnoreCase(true, UTF16.StringComparator.FOLD_CASE_DEFAULT);
        // test u_strcasecmp()
        int result = compare.compare(mixed, otherDefault);
        if (result != 0) {
            errln("error: default compare(mixed, other) = " + result
                  + " instead of 0");
        }

        // test u_strcasecmp() - exclude special i
        compare.setIgnoreCase(true,
                  UTF16.StringComparator.FOLD_CASE_EXCLUDE_SPECIAL_I);
        result = compare.compare(mixed, otherExcludeSpecialI);
        if (result != 0) {
            errln("error: exclude_i compare(mixed, other) = " + result
                  + " instead of 0");
        }

        // test u_strcasecmp()
        compare.setIgnoreCase(true,
                              UTF16.StringComparator.FOLD_CASE_DEFAULT);
        result = compare.compare(mixed, different);
        if (result <= 0) {
            errln("error: default compare(mixed, different) = " + result
                  + " instead of positive");
        }

        // test substrings - stop before the sharp s (U+00df)
        compare.setIgnoreCase(true,
                              UTF16.StringComparator.FOLD_CASE_DEFAULT);
        result = compare.compare(mixed.substring(0, 4),
                                 different.substring(0, 4));
        if (result != 0) {
            errln("error: default compare(mixed substring, different substring) = "
          + result + " instead of 0");
        }
        // test substrings - stop in the middle of the sharp s (U+00df)
        compare.setIgnoreCase(true,
                              UTF16.StringComparator.FOLD_CASE_DEFAULT);
        result = compare.compare(mixed.substring(0, 5),
                                 different.substring(0, 5));
        if (result <= 0) {
            errln("error: default compare(mixed substring, different substring) = "
          + result + " instead of positive");
        }
    }

    @Test
    public void TestHasMoreCodePointsThan()
    {
        String str = "\u0061\u0062\ud800\udc00\ud801\udc01\u0063\ud802\u0064"
        + "\udc03\u0065\u0066\ud804\udc04\ud805\udc05\u0067";
        int length = str.length();
        while (length >= 0) {
            for (int i = 0; i <= length; ++ i) {
                String s = str.substring(0, i);
                for (int number = -1; number <= ((length - i) + 2); ++ number) {
                    boolean flag = UTF16.hasMoreCodePointsThan(s, number);
                    if (flag != (UTF16.countCodePoint(s) > number)) {
                        errln("hasMoreCodePointsThan(" + Utility.hex(s)
                              + ", " + number + ") = " + flag + " is wrong");
                    }
                }
            }
            -- length;
        }

        // testing for null bad input
        for(length = -1; length <= 1; ++ length) {
            for (int i = 0; i <= length; ++ i) {
                for (int number = -2; number <= 2; ++ number) {
                    boolean flag = UTF16.hasMoreCodePointsThan((String)null,
                                                               number);
                    if (flag != (UTF16.countCodePoint((String)null) > number)) {
                        errln("hasMoreCodePointsThan(null, " + number + ") = "
                  + flag + " is wrong");
                    }
                }
            }
        }

        length = str.length();
        while (length >= 0) {
            for (int i = 0; i <= length; ++ i) {
                StringBuffer s = new StringBuffer(str.substring(0, i));
                for (int number = -1; number <= ((length - i) + 2); ++ number) {
                    boolean flag = UTF16.hasMoreCodePointsThan(s, number);
                    if (flag != (UTF16.countCodePoint(s) > number)) {
                        errln("hasMoreCodePointsThan(" + Utility.hex(s)
                              + ", " + number + ") = " + flag + " is wrong");
                    }
                }
            }
            -- length;
        }

        // testing for null bad input
        for (length = -1; length <= 1; ++ length) {
            for (int i = 0; i <= length; ++ i) {
                for (int number = -2; number <= 2; ++ number) {
                    boolean flag = UTF16.hasMoreCodePointsThan(
                                   (StringBuffer)null, number);
                    if (flag
                        != (UTF16.countCodePoint((StringBuffer)null) > number))
            {
                errln("hasMoreCodePointsThan(null, " + number + ") = "
                  + flag + " is wrong");
            }
                }
            }
        }

        char strarray[] = str.toCharArray();
        while (length >= 0) {
            for (int limit = 0; limit <= length; ++ limit) {
                for (int start = 0; start <= limit; ++ start) {
                    for (int number = -1; number <= ((limit - start) + 2);
                         ++ number) {
                        boolean flag = UTF16.hasMoreCodePointsThan(strarray,
                                   start, limit, number);
                        if (flag != (UTF16.countCodePoint(strarray, start,
                                                          limit) > number)) {
                            errln("hasMoreCodePointsThan("
                                  + Utility.hex(str.substring(start, limit))
                                  + ", " + start + ", " + limit + ", " + number
                                  + ") = " + flag + " is wrong");
                        }
                    }
                }
            }
            -- length;
        }

        // testing for null bad input
        for (length = -1; length <= 1; ++ length) {
            for (int i = 0; i <= length; ++ i) {
                for (int number = -2; number <= 2; ++ number) {
                    boolean flag = UTF16.hasMoreCodePointsThan(
                                   (StringBuffer)null, number);
                    if (flag
                        != (UTF16.countCodePoint((StringBuffer)null) > number))
            {
                errln("hasMoreCodePointsThan(null, " + number + ") = "
                  + flag + " is wrong");
            }
                }
            }
        }

        // bad input
        try {
            UTF16.hasMoreCodePointsThan(strarray, -2, -1, 5);
            errln("hasMoreCodePointsThan(chararray) with negative indexes has to throw an exception");
        } catch (Exception e) {
            logln("PASS: UTF16.hasMoreCodePointsThan failed as expected");
        }
        try {
            UTF16.hasMoreCodePointsThan(strarray, 5, 2, 5);
            errln("hasMoreCodePointsThan(chararray) with limit less than start index has to throw an exception");
        } catch (Exception e) {
            logln("PASS: UTF16.hasMoreCodePointsThan failed as expected");
        }
        try {
            if (UTF16.hasMoreCodePointsThan(strarray, -2, 2, 5)) {
                errln("hasMoreCodePointsThan(chararray) with negative start indexes can't return true");
            }
        } catch (Exception e) {
        }
    }

    @Test
    public void TestUtilities() {
        String[] tests = {
                "a",
                "\uFFFF",
                "😀",
                "\uD800",
                "\uDC00",
                "\uDBFF\uDfff",
                "",
                "\u0000",
                "\uDC00\uD800",
                "ab",
                "😀a",
                null,
        };
        StringComparator sc = new UTF16.StringComparator(true,false,0);
        for (String item1 : tests) {
            String nonNull1 = item1 == null ? "" : item1;
            int count = UTF16.countCodePoint(nonNull1);
            int expected = count == 0 || count > 1 ? -1 : nonNull1.codePointAt(0);
            assertEquals("codepoint test " + Utility.hex(nonNull1), expected, UTF16.getSingleCodePoint(item1));
            if (expected == -1) {
                continue;
            }
            for (String item2 : tests) {
                String nonNull2 = item2 == null ? "" : item2;
                int scValue = Integer.signum(sc.compare(nonNull1, nonNull2));
                int fValue = Integer.signum(UTF16.compareCodePoint(expected, item2));
                assertEquals("comparison " + Utility.hex(nonNull1) + ", " + Utility.hex(nonNull2), scValue, fValue);
            }
        }
    }

    @Test
    public void TestNewString() {
    final int[] codePoints = {
        UCharacter.toCodePoint(UCharacter.MIN_HIGH_SURROGATE, UCharacter.MAX_LOW_SURROGATE),
        UCharacter.toCodePoint(UCharacter.MAX_HIGH_SURROGATE, UCharacter.MIN_LOW_SURROGATE),
        UCharacter.MAX_HIGH_SURROGATE,
        'A',
        -1,
    };
    

    final String cpString = "" +
        UCharacter.MIN_HIGH_SURROGATE +
        UCharacter.MAX_LOW_SURROGATE +
        UCharacter.MAX_HIGH_SURROGATE +
        UCharacter.MIN_LOW_SURROGATE +
        UCharacter.MAX_HIGH_SURROGATE +
        'A';

    final int[][] tests = {
        { 0, 1, 0, 2 },
        { 0, 2, 0, 4 },
        { 1, 1, 2, 2 },
        { 1, 2, 2, 3 },
        { 1, 3, 2, 4 },
        { 2, 2, 4, 2 },
        { 2, 3, 0, -1 },
        { 4, 5, 0, -1 },
        { 3, -1, 0, -1 }
    };

     for (int i = 0; i < tests.length; ++i) {
        int[] t = tests[i];
        int s = t[0];
        int c = t[1];
        int rs = t[2];
        int rc = t[3];

        Exception e = null;
        try {
        String str = UTF16.newString(codePoints, s, c);
        if (rc == -1 || !str.equals(cpString.substring(rs, rs+rc))) {
            errln("failed codePoints iter: " + i + " start: " + s + " len: " + c);
        }
        continue;
        }
        catch (IndexOutOfBoundsException e1) {
        e = e1;
        }
        catch (IllegalArgumentException e2) {
        e = e2;
        }
        if (rc != -1) {
        errln(e.getMessage());
        }
    }
    }

    // private data members ----------------------------------------------

    private final static String INDEXOF_SUPPLEMENTARY_STRING_ =
        "\ud841\udc02\u0071\udc02\ud841\u0071\ud841\udc02\u0071\u0072" +
        "\ud841\udc02\u0071\ud841\udc02\u0071\udc02\ud841\u0073";
    private final static int INDEXOF_SUPPLEMENTARY_CHAR_[] =
    {0x71, 0xd841, 0xdc02,
     UTF16Util.getRawSupplementary((char)0xd841,
                 (char)0xdc02)};
    private final static int INDEXOF_SUPPLEMENTARY_CHAR_INDEX_[][] =
    {{2, 5, 8, 12, 15},
     {4, 17},
     {3, 16},
     {0, 6, 10, 13}
    };
    private final static String INDEXOF_SUPPLEMENTARY_STR_ = "\udc02\ud841";
    private final static int INDEXOF_SUPPLEMENTARY_STR_INDEX_[] =
    {3, 16};

    // private methods ---------------------------------------------------
}

