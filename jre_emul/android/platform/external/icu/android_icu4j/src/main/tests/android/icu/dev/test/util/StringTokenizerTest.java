/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 1996-2010, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

package android.icu.dev.test.util;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.Utility;
import android.icu.text.ReplaceableString;
import android.icu.text.UnicodeSet;
import android.icu.util.StringTokenizer;

/**
* Testing class for StringTokenizer class
* @author Syn Wee Quek
* @since oct 26 2002
*/
public final class StringTokenizerTest extends TestFmwk
{ 
      // constructor ===================================================
  
      /**
      * Constructor
      */
      public StringTokenizerTest()
      {
      }
  
      // public methods --------------------------------------------------------
    
    /**
     * Testing constructors
     */
    @Test
    public void TestConstructors()
    {
        String str = "this\tis\na\rstring\ftesting\tStringTokenizer\nconstructors!";
        String delimiter = " \t\n\r\f";
        String expected[] = {"this", "is", "a", "string", "testing", 
                             "StringTokenizer", "constructors!"};
        StringTokenizer defaultst = new StringTokenizer(str);
        StringTokenizer stdelimiter = new StringTokenizer(str, delimiter);
        StringTokenizer stdelimiterreturn = new StringTokenizer(str, delimiter,
                                                                false);
        UnicodeSet delimiterset = new UnicodeSet("[" + delimiter + "]", false);
        StringTokenizer stdelimiterset = new StringTokenizer(str, delimiterset);
        StringTokenizer stdelimitersetreturn = new StringTokenizer(str, 
                                                                delimiterset,
                                                                false);
        for (int i = 0; i < expected.length; i ++) {
            if (!(defaultst.nextElement().equals(expected[i]) 
                  && stdelimiter.nextElement().equals(expected[i])
                  && stdelimiterreturn.nextElement().equals(expected[i])
                  && stdelimiterset.nextElement().equals(expected[i])
                  && stdelimitersetreturn.nextElement().equals(expected[i]))) {
                errln("Constructor with default delimiter gives wrong results");
            }
        }
        
        UnicodeSet delimiterset1 = new UnicodeSet("[" + delimiter + "]", true);
        StringTokenizer stdelimiterset1 = new StringTokenizer(str, delimiterset1);
        if(!(stdelimiterset1.nextElement().equals(str)))
            errln("Constructor with a UnicodeSet to ignoreWhiteSpace is " +
                    "to return the same string.");
        
        String expected1[] = {"this", "\t", "is", "\n", "a", "\r", "string", "\f",
                            "testing", "\t", "StringTokenizer", "\n",
                            "constructors!"};
        stdelimiterreturn = new StringTokenizer(str, delimiter, true);
        stdelimitersetreturn = new StringTokenizer(str, delimiterset, true);
        for (int i = 0; i < expected1.length; i ++) {
            if (!(stdelimiterreturn.nextElement().equals(expected1[i])
                  && stdelimitersetreturn.nextElement().equals(expected1[i]))) {
                errln("Constructor with default delimiter and delimiter tokens gives wrong results");
            }
        }
                            
        stdelimiter = new StringTokenizer(str, (String)null);
        stdelimiterreturn = new StringTokenizer(str, (String)null, false);
        delimiterset = null;
        stdelimiterset = new StringTokenizer(str, delimiterset);
        stdelimitersetreturn = new StringTokenizer(str, delimiterset, false);
        
        if (!(stdelimiter.nextElement().equals(str)
              && stdelimiterreturn.nextElement().equals(str)
              && stdelimiterset.nextElement().equals(str)
              && stdelimitersetreturn.nextElement().equals(str))) {
            errln("Constructor with null delimiter gives wrong results");
        }
        
        delimiter = "";
        stdelimiter = new StringTokenizer(str, delimiter);
        stdelimiterreturn = new StringTokenizer(str, delimiter, false);
        delimiterset = new UnicodeSet();
        stdelimiterset = new StringTokenizer(str, delimiterset);
        stdelimitersetreturn = new StringTokenizer(str, delimiterset, false);
        
        if (!(stdelimiter.nextElement().equals(str)
              && stdelimiterreturn.nextElement().equals(str)
              && stdelimiterset.nextElement().equals(str)
              && stdelimitersetreturn.nextElement().equals(str))) {
            errln("Constructor with empty delimiter gives wrong results");
        }
        
        try {
            defaultst = new StringTokenizer(null);
            errln("null string should throw an exception");
        } catch (Exception e) {
            logln("PASS: Constructor with null string failed as expected");
        }
        try {
            stdelimiter = new StringTokenizer(null, delimiter);
            errln("null string should throw an exception");
        } catch (Exception e) {
            logln("PASS: Constructor with null string failed as expected");
        }
        try {
            stdelimiterreturn = new StringTokenizer(null, delimiter, false);
            errln("null string should throw an exception");
        } catch (Exception e) {
            logln("PASS: Constructor with null string failed as expected");
        }
        try {
            stdelimiterset = new StringTokenizer(null, delimiterset);
            errln("null string should throw an exception");
        } catch (Exception e) {
            logln("PASS: Constructor with null string failed as expected");
        }
        try {
            stdelimitersetreturn = new StringTokenizer(null, delimiterset,
                                                       false);
            errln("null string should throw an exception");
        } catch (Exception e) {
            logln("PASS: Constructor with null string failed as expected");
        }
    }
    
    /**
     * Testing supplementary
     */
    @Test
    public void TestSupplementary()
    {
        String str = "bmp string \ud800 with a unmatched surrogate character";
        String delimiter = "\ud800\udc00";
        String expected[] = {str};
            
        StringTokenizer tokenizer = new StringTokenizer(str, delimiter);
        if (!tokenizer.nextElement().equals(expected[0])) {
            errln("Error parsing \"" + Utility.hex(str) + "\"");
        }
        if (tokenizer.hasMoreElements()) {
            errln("Number of tokens exceeded expected");
        }
        delimiter = "\ud800";
        String expected1[] = {"bmp string ", 
                              " with a unmatched surrogate character"};
        tokenizer = new StringTokenizer(str, delimiter);
        int i = 0;
        while (tokenizer.hasMoreElements()) {
            if (!tokenizer.nextElement().equals(expected1[i ++])) {
                errln("Error parsing \"" + Utility.hex(str) + "\"");
            }
        }
        if (tokenizer.hasMoreElements()) {
            errln("Number of tokens exceeded expected");
        }
        
        str = "string \ud800\udc00 with supplementary character";
        delimiter = "\ud800";
        String expected2[] = {str};
        tokenizer = new StringTokenizer(str, delimiter);
        if (!tokenizer.nextElement().equals(expected2[0])) {
            errln("Error parsing \"" + Utility.hex(str) + "\"");
        }
        if (tokenizer.hasMoreElements()) {
            errln("Number of tokens exceeded expected");
        }
  
        delimiter = "\ud800\udc00";
        String expected3[] = {"string ", " with supplementary character"};
        tokenizer = new StringTokenizer(str, delimiter);
        i = 0;
        while (tokenizer.hasMoreElements()) {
            if (!tokenizer.nextElement().equals(expected3[i ++])) {
                errln("Error parsing \"" + Utility.hex(str) + "\"");
            }
        }
        if (tokenizer.hasMoreElements()) {
            errln("Number of tokens exceeded expected");
        }
        
        str = "\ud800 \ud800\udc00 \ud800 \ud800\udc00";
        delimiter = "\ud800";
        String expected4[] = {" \ud800\udc00 ", " \ud800\udc00"};
        i = 0;
        while (tokenizer.hasMoreElements()) {
            if (!tokenizer.nextElement().equals(expected4[i ++])) {
                errln("Error parsing \"" + Utility.hex(str) + "\"");
            }
        }
        if (tokenizer.hasMoreElements()) {
            errln("Number of tokens exceeded expected");
        }
        
        delimiter = "\ud800\udc00";
        String expected5[] = {"\ud800 ", " \ud800 "};
        i = 0;
        while (tokenizer.hasMoreElements()) {
            if (!tokenizer.nextElement().equals(expected5[i ++])) {
                errln("Error parsing \"" + Utility.hex(str) + "\"");
            }
        }
        if (tokenizer.hasMoreElements()) {
            errln("Number of tokens exceeded expected");
        }
    }
  
      /**
      * Testing next api
      */
    @Test
      public void TestNextNonDelimiterToken()
      {
        String str = "  ,  1 2 3  AHHHHH! 5.5 6 7    ,        8\n";
        String expected[] = {",", "1", "2", "3", "AHHHHH!", "5.5", "6", "7", 
                             ",", "8\n"};
        String delimiter = " ";
                           
        StringTokenizer tokenizer = new StringTokenizer(str, delimiter);
        int currtoken = 0;
        while (tokenizer.hasMoreElements()) {
            if (!tokenizer.nextElement().equals(expected[currtoken])) {
                errln("Error token mismatch, expected " + expected[currtoken]);
            }
            currtoken ++;
        }

        if (currtoken != expected.length) {
            errln("Didn't get correct number of tokens");
        }
        
        tokenizer = new StringTokenizer("", delimiter);
        if (tokenizer.hasMoreElements()) {
            errln("Empty string should not have any tokens");
        }
        try {
            tokenizer.nextElement();
            errln("Empty string should not have any tokens");
        } catch (Exception e) {
            logln("PASS: empty string failed as expected");
        }
        
        tokenizer = new StringTokenizer(", ,", ", ");
        if (tokenizer.hasMoreElements()) {
            errln("String with only delimiters should not have any tokens");
        }
        try {
            tokenizer.nextElement();
            errln("String with only delimiters should not have any tokens");
        } catch (Exception e) {
            logln("PASS: String with only delimiters failed as expected");
        }

        tokenizer = new StringTokenizer("q, ,", ", ");
        if (!tokenizer.hasMoreElements()) {
            errln("String that does not begin with delimiters should have some tokens");
        }
        if (!tokenizer.nextElement().equals("q")) {
            errln("String that does not begin with delimiters should have some tokens");
        } 
        try {
            tokenizer.nextElement();
            errln("String has only one token");
        } catch (Exception e) {
            logln("PASS: String with only one token failed as expected");
        }

        try {
            tokenizer = new StringTokenizer(null, delimiter);
            errln("StringTokenizer constructed with null source should throw a nullpointerexception");
        } catch (Exception e) {
            logln("PASS: StringTokenizer constructed with null source failed as expected");
        }

        tokenizer = new StringTokenizer(str, "q");
        if (!tokenizer.nextElement().equals(str)) {
            errln("Should have received the same string when there are no delimiters");
        }
    }

    /**
     * Test java compatibility, except we support surrogates.
     */
    @Test
    public void TestNoCoalesce() {
        String str = "This is   a test\rto see if\nwhitespace is handled \n\r unusually\r\n by our tokenizer\n\n\n!!!plus some other odd ones like \ttab\ttab\ttab\nand form\ffeed\ffoo.\n";
        String delims = " \t\n\r\f\ud800\udc00";

        java.util.StringTokenizer jt = new java.util.StringTokenizer(str, delims, true);
        android.icu.util.StringTokenizer it = new android.icu.util.StringTokenizer(str, delims, true);
        int n = 0;
        while (jt.hasMoreTokens() && it.hasMoreTokens()) {
            assertEquals("[" + String.valueOf(n++) + "]", jt.nextToken(), it.nextToken());
        }
        assertFalse("java tokenizer has no more tokens", jt.hasMoreTokens());
        assertFalse("icu tokenizer has no more tokens", it.hasMoreTokens());

        String sur = "Even\ud800\udc00 works.\n\n";
        it = new android.icu.util.StringTokenizer(sur, delims, true); // no coalesce
        assertEquals("sur1", it.nextToken(), "Even");
        assertEquals("sur2", it.nextToken(), "\ud800\udc00");
        assertEquals("sur3", it.nextToken(), " ");
        assertEquals("sur4", it.nextToken(), "works.");
        assertEquals("sur5", it.nextToken(), "\n");
        assertEquals("sur6", it.nextToken(), "\n");
        assertFalse("sur7", it.hasMoreTokens());
    }

    /**
    * Testing next api
    */
    @Test
    public void TestNextDelimiterToken()
    {
        String str = "  ,  1 2 3  AHHHHH! 5.5 6 7    ,        8\n";
        String expected[] = {"  ", ",", "  ", "1", " ", "2", " ", "3", "  ",
                             "AHHHHH!", " ", "5.5", " ", "6", " ", "7", "    ",
                             ",", "        ", "8\n"};
        String delimiter = " ";
                           
        StringTokenizer tokenizer = new StringTokenizer(str, delimiter, true, true);

        int currtoken = 0;
        while (tokenizer.hasMoreElements()) {
            if (!tokenizer.nextElement().equals(expected[currtoken])) {
                errln("Error token mismatch, expected " + expected[currtoken]);
            }
            currtoken ++;
        }

        if (currtoken != expected.length) {
            errln("Didn't get correct number of tokens");
        }
        
        tokenizer = new StringTokenizer("", delimiter, true);
        if (tokenizer.hasMoreElements()) {
            errln("Empty string should not have any tokens");
        }
        try {
            tokenizer.nextElement();
            errln("Empty string should not have any tokens");
        } catch (Exception e) {
            logln("PASS: Empty string failed as expected");
        }
        
        tokenizer = new StringTokenizer(", ,", ", ", true, true);
        if (!tokenizer.hasMoreElements()) {
            errln("String with only delimiters should have tokens when delimiter is treated as tokens");
        }
        if (!tokenizer.nextElement().equals(", ,")) {
            errln("String with only delimiters should return itself when delimiter is treated as tokens");
        }

        tokenizer = new StringTokenizer("q, ,", ", ", true, true);
        
        if (!tokenizer.hasMoreElements()) {
            errln("String should have some tokens");
        }
        if (!tokenizer.nextElement().equals("q") 
            || !tokenizer.nextElement().equals(", ,")) {
            errln("String tokens do not match expected results");
        } 

        try {
            tokenizer = new StringTokenizer(null, delimiter, true);
            errln("StringTokenizer constructed with null source should throw a nullpointerexception");
        } catch (Exception e) {
            logln("PASS: StringTokenizer constructed with null source failed as expected");
        }

        tokenizer = new StringTokenizer(str, "q", true);
        if (!tokenizer.nextElement().equals(str)) {
            errln("Should have recieved the same string when there are no delimiters");
        }
    }
    
    /**
     * Testing count tokens
     */
    @Test
    public void TestCountTokens()
    {
        String str = "this\tis\na\rstring\ftesting\tStringTokenizer\nconstructors!";
        String delimiter = " \t\n\r\f";
        String expected[] = {"this", "is", "a", "string", "testing", 
                             "StringTokenizer", "constructors!"};
        String expectedreturn[] = {"this", "\t", "is", "\n", "a", "\r", 
                                   "string", "\f", "testing", "\t", 
                                   "StringTokenizer", "\n", "constructors!"};
        StringTokenizer st = new StringTokenizer(str, delimiter);
        StringTokenizer streturn = new StringTokenizer(str, delimiter, true);
        if (st.countTokens() != expected.length) {
            errln("CountTokens failed for non-delimiter tokens");
        }
        if (streturn.countTokens() != expectedreturn.length) {
            errln("CountTokens failed for delimiter tokens");
        }
        for (int i = 0; i < expected.length; i ++) {
            if (!st.nextElement().equals(expected[i])
                || st.countTokens() != expected.length - i - 1) {
                errln("CountTokens default delimiter gives wrong results");
            }
        }
        for (int i = 0; i < expectedreturn.length; i ++) {
            if (!streturn.nextElement().equals(expectedreturn[i])
                || streturn.countTokens() != expectedreturn.length - i - 1) {
                errln("CountTokens with default delimiter and delimiter tokens gives wrong results");
            }
        }    
    }
        
    /**
     * Next token with new delimiters
     */
    @Test
    public void TestNextNewDelimiters()
    {
        String str = "abc0def1ghi2jkl3mno4pqr0stu1vwx2yza3bcd4efg0hij1klm2nop3qrs4tuv";
        String delimiter[] = {"0", "1", "2", "3", "4"};
        String expected[][] = {{"abc", "pqr", "efg"},
                               {"def", "stu", "hij"},
                               {"ghi", "vwx", "klm"},
                               {"jkl", "yza", "nop"},
                               {"mno", "bcd", "qrs"}
                              };
        StringTokenizer st = new StringTokenizer(str);
        int size = expected[0].length;
        for (int i = 0; i < size; i ++) {
            for (int j = 0; j < delimiter.length; j ++) {
                if (!st.nextToken(delimiter[j]).equals(expected[j][i])) {
                    errln("nextToken() with delimiters error " + i + " " + j);
                }
                if (st.countTokens() != expected[j].length - i) {            
                    errln("countTokens() after nextToken() with delimiters error"
                          + i + " " + j);
                }
            }
        }    
        st = new StringTokenizer(str);
        String delimiter1[] = {"0", "2", "4"};
        String expected1[] = {"abc", "def1ghi", "jkl3mno", "pqr", "stu1vwx", 
                              "yza3bcd", "efg", "hij1klm", "nop3qrs", "tuv"};
        for (int i = 0; i < expected1.length; i ++) {
            if (!st.nextToken(delimiter1[i % delimiter1.length]).equals(
                                                            expected1[i])) {
                errln("nextToken() with delimiters error " + i);
            }
        }
    }
    
    @Test
    public void TestBug4423()
    {
        // bug 4423:  a bad interaction between countTokens() and hasMoreTokens().
        //
        String s1 = "This is a test";
        StringTokenizer tzr = new StringTokenizer(s1);
        int  tokenCount = 0;
        
        int t = tzr.countTokens();
        if (t!= 4) {
            errln("tzr.countTokens() returned " + t + ".  Expected 4");
        }
        while (tzr.hasMoreTokens()) {
            String  tok = tzr.nextToken();
            if (tok.length() == 0) {
                errln("token with length == 0");
            }
            tokenCount++;
        }
        if (tokenCount != 4) {
            errln("Incorrect number of tokens found = " + tokenCount);
        }
        
        // Precomputed tokens arrays can grow.  Check for edge cases around
        //  boundary where growth is forced.  Array grows in increments of 100 tokens.
        String s2 = "";
        for (int i=1; i<250; i++) {
            s2 = s2 + " " + i;
            StringTokenizer tzb = new StringTokenizer(s2);
            int t2 = tzb.countTokens();
            if (t2 != i) {
                errln("tzb.countTokens() returned " + t + ".  Expected " + i);
                break;
            }
            int j = 0;
            while (tzb.hasMoreTokens()) {
                String tok = tzb.nextToken();
                j++;
                if (tok.equals(Integer.toString(j)) == false) {
                    errln("Wrong token string.  Expected \"" + j + "\", got \""
                            + tok + "\".");
                    break;
                }
            }
            if (j != i) {
                errln("Wrong number of tokens.  Expected " + i + ".  Got " + j
                        + ".");
                break;
            }
        }
        
    }

    @Test
    public void TestCountTokensNoCoalesce() {
        // jitterbug 5207
        String str = "\"\"";
        String del = "\"";
        StringTokenizer st = new StringTokenizer(str, del, true);
        int count = 0;
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            logln("[" + count + "] '" + t + "'");
            ++count;
        }
        st = new StringTokenizer(str, del, true);
        int ncount = st.countTokens();
        int xcount = 0;
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            logln("[" + xcount + "] '" + t + "'");
            ++xcount;
        }
        if (count != ncount || count != xcount) {
            errln("inconsistent counts " + count + ", " + ncount + ", " + xcount);
        }
    }

    /* Tests the method
     *      public StringBuffer _generatePattern(StringBuffer result, boolean escapeUnprintable)
     */
    @Test
    public void Test_GeneratePattern(){
        UnicodeSet us = new UnicodeSet();
        StringBuffer sb = new StringBuffer();
        try{
            us._generatePattern(sb, true);
            us._generatePattern(sb, false);
            us._generatePattern(sb.append(1), true);
            us._generatePattern(sb.append(1.0), true);
            us._generatePattern(sb.reverse(), true);
        } catch(Exception e){
            errln("UnicodeSet._generatePattern is not suppose to return an exception.");
        }
        
        try{
            us._generatePattern(null, true);
            errln("UnicodeSet._generatePattern is suppose to return an exception.");
        } catch(Exception e){}
    }
    
    /* Tests the method
     *      public int matches(Replaceable text, int[] offset, int limit, boolean incremental)
     */
    @Test
    public void TestMatches(){
        // Tests when "return incremental ? U_PARTIAL_MATCH : U_MATCH;" is true and false
        ReplaceableString rs = new ReplaceableString("dummy");
        UnicodeSet us = new UnicodeSet(0,100000); // Create a large Unicode set
        us.add("dummy");
        
        int[] offset = {0};
        int limit = 0;
        
        if(us.matches(null, offset, limit, true) != UnicodeSet.U_PARTIAL_MATCH){
            errln("UnicodeSet.matches is suppose to return " + UnicodeSet.U_PARTIAL_MATCH +
                    " but got " + us.matches(null, offset, limit, true));
        }
        
        if(us.matches(null, offset, limit, false) != UnicodeSet.U_MATCH){
            errln("UnicodeSet.matches is suppose to return " + UnicodeSet.U_MATCH +
                    " but got " + us.matches(null, offset, limit, false));
        }
        
        // Tests when "int maxLen = forward ? limit-offset[0] : offset[0]-limit;" is true and false
        try{
            offset[0] = 0; // Takes the letter "d"
            us.matches(rs, offset, 1, true);
            offset[0] = 4; // Takes the letter "y"
            us.matches(rs, offset, 1, true);
        } catch(Exception e) {
            errln("UnicodeSet.matches is not suppose to return an exception");
        }
        
        // TODO: Tests when "if (forward && length < highWaterLength)" is true
    }
    
    /* Tests the method
     *      private static int matchRest (Replaceable text, int start, int limit, String s)
     * from public int matches(Replaceable text, ...
     */
    @Test
    public void TestMatchRest(){
        // TODO: Tests when "if (maxLen > slen) maxLen = slen;" is true and false
    }
    
    /* Tests the method
     *      public int matchesAt(CharSequence text, int offset)
     */
    @Test
    public void TestMatchesAt(){
        UnicodeSet us = new UnicodeSet();           // Empty set
        us.matchesAt((CharSequence)"dummy", 0);
        us.add("dummy");                            // Add an item
        
        us.matchesAt((CharSequence)"dummy", 0);
        us.add("dummy2");                           // Add another item
        
        us.matchesAt((CharSequence)"yummy", 0);     //charAt(0) >
        us.matchesAt((CharSequence)"amy", 0);       //charAt(0) <
        
        UnicodeSet us1 = new UnicodeSet(0,100000);  // Increase the set
        us1.matchesAt((CharSequence)"dummy", 0);
    }
    
    /* Tests the method
     *      public int indexOf(int c)
     */
    @Test
    public void TestIndexOf(){
        // Tests when "if (c < MIN_VALUE || c > MAX_VALUE)" is true
        UnicodeSet us = new UnicodeSet();
        int[] invalid = {UnicodeSet.MIN_VALUE-1, UnicodeSet.MIN_VALUE-2,
                UnicodeSet.MAX_VALUE+1, UnicodeSet.MAX_VALUE+2};
        int[] valid = {UnicodeSet.MIN_VALUE, UnicodeSet.MIN_VALUE+1, 
                UnicodeSet.MAX_VALUE, UnicodeSet.MAX_VALUE-1};
        
        for(int i=0; i < invalid.length; i++){
            try{
                us.indexOf(invalid[i]);
                errln("UnicodeSet.indexOf is suppose to return an exception " +
                        "for a value of " + invalid[i]);
            } catch(Exception e){}
        }
        
        for(int i=0; i < valid.length; i++){
            try{
                us.indexOf(valid[i]);
            } catch(Exception e){
                errln("UnicodeSet.indexOf is not suppose to return an exception " +
                        "for a value of " + valid[i]);
            }
        }
    }
    
    /* Tests the method
     *      public int charAt(int index)
     */
    @Test
    public void TestCharAt(){
        UnicodeSet us = new UnicodeSet();
        
        // Test when "if (index >= 0)" is false
        int[] invalid = {-100,-10,-5,-2,-1};
        for(int i=0; i < invalid.length; i++){
            if(us.charAt(invalid[i]) != -1){
                errln("UnicodeSet.charAt(int index) was suppose to return -1 "
                        + "for an invalid input of " + invalid[i]);
            }
        }
    }
    
    /* Tests the method
     *      private UnicodeSet add_unchecked(int start, int end)
     * from public UnicodeSet add(int start, int end)
     */
    @Test
     public void TestAdd_int_int(){
         UnicodeSet us = new UnicodeSet();
         int[] invalid = {UnicodeSet.MIN_VALUE-1, UnicodeSet.MIN_VALUE-2,
                 UnicodeSet.MAX_VALUE+1, UnicodeSet.MAX_VALUE+2};
         
         // Tests when "if (start < MIN_VALUE || start > MAX_VALUE)" is true
         for(int i=0; i < invalid.length; i++){
             try{
                 us.add(invalid[i], UnicodeSet.MAX_VALUE);
                 errln("UnicodeSet.add(int start, int end) was suppose to give "
                         + "an exception for an start invalid input of "
                         + invalid[i]);
             } catch (Exception e){}
         }
         
         // Tests when "if (end < MIN_VALUE || end > MAX_VALUE)" is true
         for(int i=0; i < invalid.length; i++){
             try{
                 us.add(UnicodeSet.MIN_VALUE, invalid[i]);
                 errln("UnicodeSet.add(int start, int end) was suppose to give "
                         + "an exception for an end invalid input of "
                         + invalid[i]);
             } catch (Exception e){}
         }
         
         // Tests when "else if (start == end)" is false
         if(!(us.add(UnicodeSet.MIN_VALUE+1, UnicodeSet.MIN_VALUE).equals(us)))
             errln("UnicodeSet.add(int start, int end) was suppose to return "
                     + "the same object because start of value " + (UnicodeSet.MIN_VALUE+1)
                     + " is greater than end of value " + UnicodeSet.MIN_VALUE);
         
         if(!(us.add(UnicodeSet.MAX_VALUE, UnicodeSet.MAX_VALUE-1).equals(us)))
             errln("UnicodeSet.add(int start, int end) was suppose to return "
                     + "the same object because start of value " + UnicodeSet.MAX_VALUE
                     + " is greater than end of value " + (UnicodeSet.MAX_VALUE-1));
     }
     
     /* Tests the method
      *     private final UnicodeSet add_unchecked(int c)
      * from public final UnicodeSet add(int c)
      */
    @Test
     public void TestAdd_int(){
         UnicodeSet us = new UnicodeSet();
         int[] invalid = {UnicodeSet.MIN_VALUE-1, UnicodeSet.MIN_VALUE-2,
                 UnicodeSet.MAX_VALUE+1, UnicodeSet.MAX_VALUE+2};
         
         // Tests when "if (c < MIN_VALUE || c > MAX_VALUE)" is true
         for(int i=0; i < invalid.length; i++){
             try{
                 us.add(invalid[i]);
                 errln("UnicodeSet.add(int c) was suppose to give "
                         + "an exception for an start invalid input of "
                         + invalid[i]);
             } catch (Exception e){}
         }
         
         // Tests when "if (c == MAX_VALUE)" is true
         // TODO: Check comment in UnicodeSet.java
     }
     
     /* Tests the method
      *     private static int getSingleCP(String s)
      * from public final boolean contains(String s)
      */
    @Test
     public void TestGetSingleCP(){
         UnicodeSet us = new UnicodeSet();
         // Tests when "if (s.length() < 1)" is true
         try{
             us.contains("");
             errln("UnicodeSet.getSingleCP is suppose to give an exception for " +
                     "an empty string.");
         } catch (Exception e){}
         
         try{
             us.contains((String)null);
             errln("UnicodeSet.getSingleCP is suppose to give an exception for " +
             "a null string.");
         } catch (Exception e){}
         
         // Tests when "if (cp > 0xFFFF)" is true
         String[] cases = {"\uD811\uDC00","\uD811\uDC11","\uD811\uDC22"}; 
         for(int i=0; i<cases.length; i++){
             try{
                 us.contains(cases[i]);
             } catch (Exception e){
                 errln("UnicodeSet.getSingleCP is not suppose to give an exception for " +
                     "a null string.");
             }
         }
     }
     
     /* Tests the method
      *     public final UnicodeSet removeAllStrings()
      */
    @Test
     public void TestRemoveAllString(){
         // Tests when "if (strings.size() != 0)" is false
         UnicodeSet us = new UnicodeSet();
         try{
             us.removeAllStrings();
         } catch(Exception e){
             errln("UnicodeSet.removeAllString() was not suppose to given an " +
                     "exception for a strings size of 0");
         }
     }
     
     /* Tests the method
      *     public UnicodeSet retain(int start, int end)
      */
    @Test
      public void TestRetain_int_int(){
          UnicodeSet us = new UnicodeSet();
          int[] invalid = {UnicodeSet.MIN_VALUE-1, UnicodeSet.MIN_VALUE-2,
                  UnicodeSet.MAX_VALUE+1, UnicodeSet.MAX_VALUE+2};
          
          // Tests when "if (start < MIN_VALUE || start > MAX_VALUE)" is true
          for(int i=0; i < invalid.length; i++){
              try{
                  us.retain(invalid[i], UnicodeSet.MAX_VALUE);
                  errln("UnicodeSet.retain(int start, int end) was suppose to give "
                          + "an exception for an start invalid input of "
                          + invalid[i]);
              } catch (Exception e){}
          }
          
          // Tests when "if (end < MIN_VALUE || end > MAX_VALUE)" is true
          for(int i=0; i < invalid.length; i++){
              try{
                  us.retain(UnicodeSet.MIN_VALUE, invalid[i]);
                  errln("UnicodeSet.retain(int start, int end) was suppose to give "
                          + "an exception for an end invalid input of "
                          + invalid[i]);
              } catch (Exception e){}
          }
          
          // Tests when "if (start <= end)" is false
          try{
              us.retain(UnicodeSet.MIN_VALUE+1, UnicodeSet.MIN_VALUE);
          } catch(Exception e){
              errln("UnicodeSet.retain(int start, int end) was not suppose to give "
                      + "an exception.");
          }
          
          try{
              us.retain(UnicodeSet.MAX_VALUE, UnicodeSet.MAX_VALUE-1);
          } catch(Exception e){
              errln("UnicodeSet.retain(int start, int end) was not suppose to give "
                      + "an exception.");
          }
      }
      
      /* Tests the method
       *        public final UnicodeSet retain(String s)
       */
    @Test
      public void TestRetain_String(){
          // Tests when "if (isIn && size() == 1)" is true
          UnicodeSet us = new UnicodeSet();
          us.add("dummy");
          if(!(us.retain("dummy").equals(us))){
              errln("UnicodeSet.retain(String s) was suppose to return the " +
                      "same UnicodeSet since the string was found in the original.");
          }
      }
      
      /* Tests the method
       *     public UnicodeSet remove(int start, int end)
       */
    @Test
       public void TestRemove(){
           UnicodeSet us = new UnicodeSet();
           int[] invalid = {UnicodeSet.MIN_VALUE-1, UnicodeSet.MIN_VALUE-2,
                   UnicodeSet.MAX_VALUE+1, UnicodeSet.MAX_VALUE+2};
           
           // Tests when "if (start < MIN_VALUE || start > MAX_VALUE)" is true
           for(int i=0; i < invalid.length; i++){
               try{
                   us.remove(invalid[i], UnicodeSet.MAX_VALUE);
                   errln("UnicodeSet.remove(int start, int end) was suppose to give "
                           + "an exception for an start invalid input of "
                           + invalid[i]);
               } catch (Exception e){}
           }
           
           // Tests when "if (end < MIN_VALUE || end > MAX_VALUE)" is true
           for(int i=0; i < invalid.length; i++){
               try{
                   us.remove(UnicodeSet.MIN_VALUE, invalid[i]);
                   errln("UnicodeSet.remove(int start, int end) was suppose to give "
                           + "an exception for an end invalid input of "
                           + invalid[i]);
               } catch (Exception e){}
           }
           
           // Tests when "if (start <= end)" is false
           try{
               us.remove(UnicodeSet.MIN_VALUE+1, UnicodeSet.MIN_VALUE);
           } catch(Exception e){
               errln("UnicodeSet.remove(int start, int end) was not suppose to give "
                       + "an exception.");
           }
           
           try{
               us.remove(UnicodeSet.MAX_VALUE, UnicodeSet.MAX_VALUE-1);
           } catch(Exception e){
               errln("UnicodeSet.remove(int start, int end) was not suppose to give "
                       + "an exception.");
           }
       }
       
       /* Tests the method
        *     public UnicodeSet complement(int start, int end)
        */
    @Test
        public void TestComplement_int_int(){
            UnicodeSet us = new UnicodeSet();
            int[] invalid = {UnicodeSet.MIN_VALUE-1, UnicodeSet.MIN_VALUE-2,
                    UnicodeSet.MAX_VALUE+1, UnicodeSet.MAX_VALUE+2};
            
            // Tests when "if (start < MIN_VALUE || start > MAX_VALUE)" is true
            for(int i=0; i < invalid.length; i++){
                try{
                    us.complement(invalid[i], UnicodeSet.MAX_VALUE);
                    errln("UnicodeSet.complement(int start, int end) was suppose to give "
                            + "an exception for an start invalid input of "
                            + invalid[i]);
                } catch (Exception e){}
            }
            
            // Tests when "if (end < MIN_VALUE || end > MAX_VALUE)" is true
            for(int i=0; i < invalid.length; i++){
                try{
                    us.complement(UnicodeSet.MIN_VALUE, invalid[i]);
                    errln("UnicodeSet.complement(int start, int end) was suppose to give "
                            + "an exception for an end invalid input of "
                            + invalid[i]);
                } catch (Exception e){}
            }
            
            // Tests when "if (start <= end)" is false
            try{
                us.complement(UnicodeSet.MIN_VALUE+1, UnicodeSet.MIN_VALUE);
            } catch(Exception e){
                errln("UnicodeSet.complement(int start, int end) was not suppose to give "
                        + "an exception.");
            }
            
            try{
                us.complement(UnicodeSet.MAX_VALUE, UnicodeSet.MAX_VALUE-1);
            } catch(Exception e){
                errln("UnicodeSet.complement(int start, int end) was not suppose to give "
                        + "an exception.");
            }
        }
        
        /* Tests the method
         *      public final UnicodeSet complement(String s)
         */
    @Test
        public void TestComplement_String(){
            // Tests when "if (cp < 0)" is false
            UnicodeSet us = new UnicodeSet();
            us.add("dummy");
            try{
                us.complement("dummy");
            } catch (Exception e){
                errln("UnicodeSet.complement(String s) was not suppose to give "
                        + "an exception for 'dummy'.");
            }
            
            // Tests when "if (strings.contains(s))" is true
            us = new UnicodeSet();
            us.add("\uDC11");
            try{
                us.complement("\uDC11");
            } catch (Exception e){
                errln("UnicodeSet.complement(String s) was not suppose to give "
                        + "an exception for '\uDC11'.");
            }
        }
        
        /* Tests the method
         *      public boolean contains(int c)
         */
    @Test
        public void TestContains_int(){
            UnicodeSet us = new UnicodeSet();
            int[] invalid = {UnicodeSet.MIN_VALUE-1, UnicodeSet.MIN_VALUE-2,
                    UnicodeSet.MAX_VALUE+1, UnicodeSet.MAX_VALUE+2};
            
            // Tests when "if (c < MIN_VALUE || c > MAX_VALUE)" is true
            for(int i=0; i < invalid.length; i++){
                try{
                    us.contains(invalid[i]);
                    errln("UnicodeSet.contains(int c) was suppose to give "
                            + "an exception for an start invalid input of "
                            + invalid[i]);
                } catch (Exception e){}
            }
        }
        
        /* Tests the method
         *     public boolean contains(int start, int end)
         */
    @Test
         public void TestContains_int_int(){
             UnicodeSet us = new UnicodeSet();
             int[] invalid = {UnicodeSet.MIN_VALUE-1, UnicodeSet.MIN_VALUE-2,
                     UnicodeSet.MAX_VALUE+1, UnicodeSet.MAX_VALUE+2};
             
             // Tests when "if (start < MIN_VALUE || start > MAX_VALUE)" is true
             for(int i=0; i < invalid.length; i++){
                 try{
                     us.contains(invalid[i], UnicodeSet.MAX_VALUE);
                     errln("UnicodeSet.contains(int start, int end) was suppose to give "
                             + "an exception for an start invalid input of "
                             + invalid[i]);
                 } catch (Exception e){}
             }
             
             // Tests when "if (end < MIN_VALUE || end > MAX_VALUE)" is true
             for(int i=0; i < invalid.length; i++){
                 try{
                     us.contains(UnicodeSet.MIN_VALUE, invalid[i]);
                     errln("UnicodeSet.contains(int start, int end) was suppose to give "
                             + "an exception for an end invalid input of "
                             + invalid[i]);
                 } catch (Exception e){}
             }
         }
         
         /* Tests the method
          *     public String getRegexEquivalent()
          */
    @Test
         public void TestGetRegexEquivalent(){
             UnicodeSet us = new UnicodeSet();
             String res = us.getRegexEquivalent();
             if(!(res.equals("[]")))
                 errln("UnicodeSet.getRegexEquivalent is suppose to return '[]' " +
                         "but got " + res);
         }
         
         /* Tests the method
          *     public boolean containsNone(int start, int end)
          */
    @Test
          public void TestContainsNone(){
              UnicodeSet us = new UnicodeSet();
              int[] invalid = {UnicodeSet.MIN_VALUE-1, UnicodeSet.MIN_VALUE-2,
                      UnicodeSet.MAX_VALUE+1, UnicodeSet.MAX_VALUE+2};
              
              // Tests when "if (start < MIN_VALUE || start > MAX_VALUE)" is true
              for(int i=0; i < invalid.length; i++){
                  try{
                      us.containsNone(invalid[i], UnicodeSet.MAX_VALUE);
                      errln("UnicodeSet.containsNoneint start, int end) was suppose to give "
                              + "an exception for an start invalid input of "
                              + invalid[i]);
                  } catch (Exception e){}
              }
              
              // Tests when "if (end < MIN_VALUE || end > MAX_VALUE)" is true
              for(int i=0; i < invalid.length; i++){
                  try{
                      us.containsNone(UnicodeSet.MIN_VALUE, invalid[i]);
                      errln("UnicodeSet.containsNone(int start, int end) was suppose to give "
                              + "an exception for an end invalid input of "
                              + invalid[i]);
                  } catch (Exception e){}
              }
              
              // Tests when "if (start < list[++i])" is false
              try{
                  us.add(0);
                  us.containsNone(1, 2); // 1 > 0
              } catch (Exception e){
                  errln("UnicodeSet.containsNone(int start, int end) was not suppose to give " +
                          "an exception.");
              }
          }
}