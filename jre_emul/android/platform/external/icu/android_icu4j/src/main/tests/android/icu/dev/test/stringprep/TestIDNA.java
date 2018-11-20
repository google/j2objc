/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2003-2011, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
*/
package android.icu.dev.test.stringprep;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.Utility;
import android.icu.text.IDNA;
import android.icu.text.StringPrep;
import android.icu.text.StringPrepParseException;
import android.icu.text.UCharacterIterator;
import android.icu.text.UTF16;

/**
 * @author ram
 */
public class TestIDNA extends TestFmwk {
    private StringPrepParseException unassignedException = new StringPrepParseException("",StringPrepParseException.UNASSIGNED_ERROR);

    @Test
    public void TestToUnicode() throws Exception{
        for(int i=0; i<TestData.asciiIn.length; i++){
            // test StringBuffer toUnicode
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.DEFAULT, null);
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.ALLOW_UNASSIGNED, null);
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.USE_STD3_RULES, null);
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.USE_STD3_RULES|IDNA.ALLOW_UNASSIGNED, null);

        }
    }

    @Test
    public void TestToASCII() throws Exception{
        for(int i=0; i<TestData.asciiIn.length; i++){
            // test StringBuffer toUnicode
            doTestToASCII(new String(TestData.unicodeIn[i]),TestData.asciiIn[i],IDNA.DEFAULT, null);
            doTestToASCII(new String(TestData.unicodeIn[i]),TestData.asciiIn[i],IDNA.ALLOW_UNASSIGNED, null);
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.USE_STD3_RULES, null);
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.USE_STD3_RULES|IDNA.ALLOW_UNASSIGNED, null);

        }
    }

    @Test
    public void TestIDNToASCII() throws Exception{
        for(int i=0; i<TestData.domainNames.length; i++){
            doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNA.DEFAULT, null);
            doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNA.ALLOW_UNASSIGNED, null);
            doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNA.USE_STD3_RULES, null);
            doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNA.ALLOW_UNASSIGNED|IDNA.USE_STD3_RULES, null);
        }

        for(int i=0; i<TestData.domainNames1Uni.length; i++){
            doTestIDNToASCII(TestData.domainNames1Uni[i],TestData.domainNamesToASCIIOut[i],IDNA.DEFAULT, null);
            doTestIDNToASCII(TestData.domainNames1Uni[i],TestData.domainNamesToASCIIOut[i],IDNA.ALLOW_UNASSIGNED, null);
        }
    }
    @Test
    public void TestIDNToUnicode() throws Exception{
        for(int i=0; i<TestData.domainNames.length; i++){
            doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNA.DEFAULT, null);
            doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNA.ALLOW_UNASSIGNED, null);
            doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNA.USE_STD3_RULES, null);
            doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNA.ALLOW_UNASSIGNED|IDNA.USE_STD3_RULES, null);
        }
        for(int i=0; i<TestData.domainNamesToASCIIOut.length; i++){
            doTestIDNToUnicode(TestData.domainNamesToASCIIOut[i],TestData.domainNamesToUnicodeOut[i],IDNA.DEFAULT, null);
            doTestIDNToUnicode(TestData.domainNamesToASCIIOut[i],TestData.domainNamesToUnicodeOut[i],IDNA.ALLOW_UNASSIGNED, null);
        }
    }

    private void doTestToUnicode(String src, String expected, int options, Object expectedException)
                throws Exception{
        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{

            StringBuffer out = IDNA.convertToUnicode(src,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
                errln("convertToUnicode did not return expected result with options : "+ options +
                      " Expected: " + prettify(expected)+" Got: "+prettify(out));
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception for source: " + prettify(src) +" Got:  "+ ex.toString());
            }
        }
        try{

            StringBuffer out = IDNA.convertToUnicode(inBuf,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToUnicode did not return expected result with options : "+ options +
                     " Expected: " + prettify(expected)+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception for source: " + prettify(src) +" Got:  "+ ex.toString());
            }
        }

        try{
            StringBuffer out = IDNA.convertToUnicode(inIter,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToUnicode did not return expected result with options : "+ options +
                     " Expected: " + prettify(expected)+" Got: "+prettify(out));
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("Did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("Did not get the expected exception for source: " + prettify(src) +" Got:  "+ ex.toString());
            }
        }
    }

    private void doTestIDNToUnicode(String src, String expected, int options, Object expectedException)
                throws Exception{
        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{

            StringBuffer out = IDNA.convertIDNToUnicode(src,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
                errln("convertToUnicode did not return expected result with options : "+ options +
                      " Expected: " + prettify(expected)+" Got: "+prettify(out));
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("convertToUnicode did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
        try{
            StringBuffer out = IDNA.convertIDNToUnicode(inBuf,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToUnicode did not return expected result with options : "+ options +
                     " Expected: " + prettify(expected)+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("convertToUnicode did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }

        try{
            StringBuffer out = IDNA.convertIDNToUnicode(inIter,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToUnicode did not return expected result with options : "+ options +
                     " Expected: " + prettify(expected)+" Got: "+prettify(out));
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("Did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("Did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
    }
    private void doTestToASCII(String src, String expected, int options, Object expectedException)
                throws Exception{
        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{

            StringBuffer out = IDNA.convertToASCII(src,options);
            if(!unassignedException.equals(expectedException) && expected!=null && out != null && expected!=null && out != null && !out.toString().equals(expected.toLowerCase())){
                errln("convertToASCII did not return expected result with options : "+ options +
                      " Expected: " + expected+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("convertToASCII did not get the expected exception for source: " +src +"\n Got:  "+ ex.toString() +"\n Expected: " +ex.toString());
            }
        }

        try{
            StringBuffer out = IDNA.convertToASCII(inBuf,options);
            if(!unassignedException.equals(expectedException) && expected!=null && out != null && expected!=null && out != null && !out.toString().equals(expected.toLowerCase())){
               errln("convertToASCII did not return expected result with options : "+ options +
                     " Expected: " + expected+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("convertToASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }

        try{
            StringBuffer out = IDNA.convertToASCII(inIter,options);
            if(!unassignedException.equals(expectedException) && expected!=null && out != null && expected!=null && out != null && !out.toString().equals(expected.toLowerCase())){
               errln("convertToASCII did not return expected result with options : "+ options +
                     " Expected: " + expected+" Got: "+ out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("convertToASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
    }
    private void doTestIDNToASCII(String src, String expected, int options, Object expectedException)
                throws Exception{
        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{

            StringBuffer out = IDNA.convertIDNToASCII(src,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
                errln("convertToIDNASCII did not return expected result with options : "+ options +
                      " Expected: " + expected+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToIDNASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertToIDNASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
        try{
            StringBuffer out = IDNA.convertIDNToASCII(inBuf,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToIDNASCII did not return expected result with options : "+ options +
                     " Expected: " + expected+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToIDNASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertToIDNASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }

        try{
            StringBuffer out = IDNA.convertIDNToASCII(inIter,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertIDNToASCII did not return expected result with options : "+ options +
                     " Expected: " + expected+" Got: "+ out);
            }

            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertIDNToASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertIDNToASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
    }
    @Test
    public void TestConformance()throws Exception{
        for(int i=0; i<TestData.conformanceTestCases.length;i++){

            TestData.ConformanceTestCase testCase = TestData.conformanceTestCases[i];
            if(testCase.expected != null){
                //Test toASCII
                doTestToASCII(testCase.input,testCase.output,IDNA.DEFAULT,testCase.expected);
                doTestToASCII(testCase.input,testCase.output,IDNA.ALLOW_UNASSIGNED,testCase.expected);
            }
            //Test toUnicode
            //doTestToUnicode(testCase.input,testCase.output,IDNA.DEFAULT,testCase.expected);
        }
    }
    @Test
    public void TestNamePrepConformance() throws Exception{
        StringPrep namePrep = StringPrep.getInstance(StringPrep.RFC3491_NAMEPREP);
        for(int i=0; i<TestData.conformanceTestCases.length;i++){
            TestData.ConformanceTestCase testCase = TestData.conformanceTestCases[i];
            UCharacterIterator iter = UCharacterIterator.getInstance(testCase.input);
            try{
                StringBuffer output = namePrep.prepare(iter,StringPrep.DEFAULT);
                if(testCase.output !=null && output!=null && !testCase.output.equals(output.toString())){
                    errln("Did not get the expected output. Expected: " + prettify(testCase.output)+
                          " Got: "+ prettify(output) );
                }
                if(testCase.expected!=null && !unassignedException.equals(testCase.expected)){
                    errln("Did not get the expected exception. The operation succeeded!");
                }
            }catch(StringPrepParseException ex){
                if(testCase.expected == null || !ex.equals(testCase.expected)){
                    errln("Did not get the expected exception for source: " +testCase.input +" Got:  "+ ex.toString());
                }
            }

            try{
                iter.setToStart();
                StringBuffer output = namePrep.prepare(iter,StringPrep.ALLOW_UNASSIGNED);
                if(testCase.output !=null && output!=null && !testCase.output.equals(output.toString())){
                    errln("Did not get the expected output. Expected: " + prettify(testCase.output)+
                          " Got: "+ prettify(output) );
                }
                if(testCase.expected!=null && !unassignedException.equals(testCase.expected)){
                    errln("Did not get the expected exception. The operation succeeded!");
                }
            }catch(StringPrepParseException ex){
                if(testCase.expected == null || !ex.equals(testCase.expected)){
                    errln("Did not get the expected exception for source: " +testCase.input +" Got:  "+ ex.toString());
                }
            }
        }

    }
    @Test
    public void TestErrorCases() throws Exception{
        for(int i=0; i < TestData.errorCases.length; i++){
            TestData.ErrorCase errCase = TestData.errorCases[i];
            if(errCase.testLabel==true){
                // Test ToASCII
                doTestToASCII(new String(errCase.unicode),errCase.ascii,IDNA.DEFAULT,errCase.expected);
                doTestToASCII(new String(errCase.unicode),errCase.ascii,IDNA.ALLOW_UNASSIGNED,errCase.expected);
                if(errCase.useSTD3ASCIIRules){
                    doTestToASCII(new String(errCase.unicode),errCase.ascii,IDNA.USE_STD3_RULES,errCase.expected);
                }
            }
            if(errCase.useSTD3ASCIIRules!=true){

                // Test IDNToASCII
                doTestIDNToASCII(new String(errCase.unicode),errCase.ascii,IDNA.DEFAULT,errCase.expected);
                doTestIDNToASCII(new String(errCase.unicode),errCase.ascii,IDNA.ALLOW_UNASSIGNED,errCase.expected);

            }else{
                doTestIDNToASCII(new String(errCase.unicode),errCase.ascii,IDNA.USE_STD3_RULES,errCase.expected);
            }
            //TestToUnicode
            if(errCase.testToUnicode==true){
                if(errCase.useSTD3ASCIIRules!=true){
                    // Test IDNToUnicode
                    doTestIDNToUnicode(errCase.ascii,new String(errCase.unicode),IDNA.DEFAULT,errCase.expected);
                    doTestIDNToUnicode(errCase.ascii,new String(errCase.unicode),IDNA.ALLOW_UNASSIGNED,errCase.expected);

                }else{
                    doTestIDNToUnicode(errCase.ascii,new String(errCase.unicode),IDNA.USE_STD3_RULES,errCase.expected);
                }
            }
        }
    }
    private void doTestCompare(String s1, String s2, boolean isEqual){
        try{
            int retVal = IDNA.compare(s1,s2,IDNA.DEFAULT);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+
                      " s2: "+prettify(s2));
            }
            retVal = IDNA.compare(new StringBuffer(s1), new StringBuffer(s2), IDNA.DEFAULT);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+
                     " s2: "+prettify(s2));
            }
            retVal = IDNA.compare(UCharacterIterator.getInstance(s1), UCharacterIterator.getInstance(s2), IDNA.DEFAULT);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+
                     " s2: "+prettify(s2));
            }
        }catch(Exception e){
            e.printStackTrace();
            errln("Unexpected exception thrown by IDNA.compare");
        }

        try{
            int retVal = IDNA.compare(s1,s2,IDNA.ALLOW_UNASSIGNED);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+
                      " s2: "+prettify(s2));
            }
            retVal = IDNA.compare(new StringBuffer(s1), new StringBuffer(s2), IDNA.ALLOW_UNASSIGNED);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+
                     " s2: "+prettify(s2));
            }
            retVal = IDNA.compare(UCharacterIterator.getInstance(s1), UCharacterIterator.getInstance(s2), IDNA.ALLOW_UNASSIGNED);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+
                     " s2: "+prettify(s2));
            }
        }catch(Exception e){
            errln("Unexpected exception thrown by IDNA.compare");
        }
    }
    @Test
    public void TestCompare() throws Exception{
        String www = "www.";
        String com = ".com";
        StringBuffer source = new StringBuffer(www);
        StringBuffer uni0   = new StringBuffer(www);
        StringBuffer uni1   = new StringBuffer(www);
        StringBuffer ascii0 = new StringBuffer(www);
        StringBuffer ascii1 = new StringBuffer(www);

        uni0.append(TestData.unicodeIn[0]);
        uni0.append(com);

        uni1.append(TestData.unicodeIn[1]);
        uni1.append(com);

        ascii0.append(TestData.asciiIn[0]);
        ascii0.append(com);

        ascii1.append(TestData.asciiIn[1]);
        ascii1.append(com);

        for(int i=0;i< TestData.unicodeIn.length; i++){

            // for every entry in unicodeIn array
            // prepend www. and append .com
            source.setLength(4);
            source.append(TestData.unicodeIn[i]);
            source.append(com);

            // a) compare it with itself
            doTestCompare(source.toString(),source.toString(),true);

            // b) compare it with asciiIn equivalent
            doTestCompare(source.toString(),www+TestData.asciiIn[i]+com,true);

            // c) compare it with unicodeIn not equivalent
            if(i==0){
                doTestCompare(source.toString(), uni1.toString(), false);
            }else{
                doTestCompare(source.toString(),uni0.toString(), false);
            }
            // d) compare it with asciiIn not equivalent
            if(i==0){
                doTestCompare(source.toString(),ascii1.toString(), false);
            }else{
                doTestCompare(source.toString(),ascii0.toString(), false);
            }

        }
    }

    //  test and ascertain
    //  func(func(func(src))) == func(src)
    private void doTestChainingToASCII(String source) throws Exception {
        StringBuffer expected;
        StringBuffer chained;

        // test convertIDNToASCII
        expected = IDNA.convertIDNToASCII(source,IDNA.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNA.convertIDNToASCII(chained,IDNA.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertIDNToASCII");
        }
        // test convertIDNToA
        expected = IDNA.convertToASCII(source,IDNA.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNA.convertToASCII(chained,IDNA.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertToASCII");
        }
    }

    //  test and ascertain
    //  func(func(func(src))) == func(src)
    private void doTestChainingToUnicode(String source) throws Exception {
        StringBuffer expected;
        StringBuffer chained;

        // test convertIDNToUnicode
        expected = IDNA.convertIDNToUnicode(source,IDNA.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNA.convertIDNToUnicode(chained,IDNA.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertIDNToUnicode");
        }
        // test convertIDNToA
        expected = IDNA.convertToUnicode(source,IDNA.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNA.convertToUnicode(chained,IDNA.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertToUnicode");
        }
    }
    @Test
    public void TestChaining() throws Exception{
        for(int i=0; i< TestData.asciiIn.length; i++){
            doTestChainingToUnicode(TestData.asciiIn[i]);
        }
        for(int i=0; i< TestData.unicodeIn.length; i++){
            doTestChainingToASCII(new String(TestData.unicodeIn[i]));
        }
    }


    /* IDNA RFC Says:
    A label is an individual part of a domain name.  Labels are usually
    shown separated by dots; for example, the domain name
    "www.example.com" is composed of three labels: "www", "example", and
    "com".  (The zero-length root label described in [STD13], which can
    be explicit as in "www.example.com." or implicit as in
    "www.example.com", is not considered a label in this specification.)
    */
    @Test
    public void TestRootLabelSeparator() throws Exception{
        String www = "www.";
        String com = ".com."; //root label separator
        StringBuffer source = new StringBuffer(www);
        StringBuffer uni0   = new StringBuffer(www);
        StringBuffer uni1   = new StringBuffer(www);
        StringBuffer ascii0 = new StringBuffer(www);
        StringBuffer ascii1 = new StringBuffer(www);

        uni0.append(TestData.unicodeIn[0]);
        uni0.append(com);

        uni1.append(TestData.unicodeIn[1]);
        uni1.append(com);

        ascii0.append(TestData.asciiIn[0]);
        ascii0.append(com);

        ascii1.append(TestData.asciiIn[1]);
        ascii1.append(com);

        for(int i=0;i< TestData.unicodeIn.length; i++){

            // for every entry in unicodeIn array
            // prepend www. and append .com
            source.setLength(4);
            source.append(TestData.unicodeIn[i]);
            source.append(com);

            // a) compare it with itself
            doTestCompare(source.toString(),source.toString(),true);

            // b) compare it with asciiIn equivalent
            doTestCompare(source.toString(),www+TestData.asciiIn[i]+com,true);

            // c) compare it with unicodeIn not equivalent
            if(i==0){
                doTestCompare(source.toString(), uni1.toString(), false);
            }else{
                doTestCompare(source.toString(),uni0.toString(), false);
            }
            // d) compare it with asciiIn not equivalent
            if(i==0){
                doTestCompare(source.toString(),ascii1.toString(), false);
            }else{
                doTestCompare(source.toString(),ascii0.toString(), false);
            }

        }

    }


    private static final int loopCount = 100;
    private static final int maxCharCount = 15;
   // private static final int maxCodePoint = 0x10ffff;
    private Random random = null;

    /**
     * Return a random integer i where 0 <= i < n.
     * A special function that gets random codepoints from planes 0,1,2 and 14
     */
    private int rand_uni()
    {
       int retVal = (int)(random.nextLong()& 0x3FFFF);
       if(retVal >= 0x30000){
           retVal+=0xB0000;
       }
       return retVal;
    }

    private int randi(int n){
        return (random.nextInt(0x7fff) % (n+1));
    }

    private StringBuffer getTestSource(StringBuffer fillIn) {
        // use uniform seed value from the framework
        if(random==null){
            random = createRandom();
        }
        int i = 0;
        int charCount = (randi(maxCharCount) + 1);
        while (i <charCount ) {
            int codepoint = rand_uni();
            if(codepoint == 0x0000){
                continue;
            }
            UTF16.append(fillIn, codepoint);
            i++;
        }
        return fillIn;

    }

    // TODO(user): turned off because not running before
    @Ignore
    @Test
    public void MonkeyTest() throws Exception{
         StringBuffer source = new StringBuffer();
         /* do the monkey test   */
         for(int i=0; i<loopCount; i++){
             source.setLength(0);
             getTestSource(source);
             doTestCompareReferenceImpl(source);
         }

         // test string with embedded null
         source.append( "\\u0000\\u2109\\u3E1B\\U000E65CA\\U0001CAC5" );

         source = new StringBuffer(Utility.unescape(source.toString()));
         doTestCompareReferenceImpl(source);

         //StringBuffer src = new StringBuffer(Utility.unescape("\\uDEE8\\U000E228C\\U0002EE8E\\U000E6350\\U00024DD9\u4049\\U000E0DE4\\U000E448C\\U0001869B\\U000E3380\\U00016A8E\\U000172D5\\U0001C408\\U000E9FB5"));
         //doTestCompareReferenceImpl(src);

         //test deletion of code points
         source = new StringBuffer(Utility.unescape("\\u043f\\u00AD\\u034f\\u043e\\u0447\\u0435\\u043c\\u0443\\u0436\\u0435\\u043e\\u043d\\u0438\\u043d\\u0435\\u0433\\u043e\\u0432\\u043e\\u0440\\u044f\\u0442\\u043f\\u043e\\u0440\\u0443\\u0441\\u0441\\u043a\\u0438"));
         StringBuffer expected = new StringBuffer("xn--b1abfaaepdrnnbgefbadotcwatmq2g4l");
         doTestCompareReferenceImpl(source);
         doTestToASCII(source.toString(),expected.toString(), IDNA.DEFAULT, null);
    }

    private StringBuffer _doTestCompareReferenceImpl(StringBuffer src, boolean toASCII, int options) {
        String refIDNAName = toASCII ? "IDNAReference.convertToASCII" : "IDNAReference.convertToUnicode";
        String uIDNAName = toASCII ? "IDNA.convertToASCII" : "IDNA.convertToUnicode";

        logln("Comparing " + refIDNAName + " with " + uIDNAName + " for input: "
                + prettify(src) + " with options: " + options);

        StringBuffer exp = null;
        int expStatus = -1;
        try {
            exp = toASCII ? IDNAReference.convertToASCII(src, options) : IDNAReference.convertToUnicode(src, options);
        } catch (StringPrepParseException e) {
            expStatus = e.getError();
        }

        StringBuffer got = null;
        int gotStatus = -1;
        try {
            got = toASCII ? IDNA.convertToASCII(src, options) : IDNA.convertToUnicode(src, options);
        } catch (StringPrepParseException e) {
            gotStatus = e.getError();
        }

        if (expStatus != gotStatus) {
            errln("Did not get the expected status while comparing " + refIDNAName + " with " + uIDNAName
                    + " Expected: " + expStatus
                    + " Got: " + gotStatus
                    + " for Source: "+ prettify(src)
                    + " Options: " + options);
        } else {
            // now we know that both implementation yielded same status
            if (gotStatus == -1) {
                // compare the outputs
                if (!got.toString().equals(exp.toString())) {
                    errln("Did not get the expected output while comparing " + refIDNAName + " with " + uIDNAName
                            + " Expected: " + exp
                            + " Got: " + got
                            + " for Source: "+ prettify(src)
                            + " Options: " + options);
                }
            } else {
                logln("Got the same error while comparing " + refIDNAName + " with " + uIDNAName
                        +" for input: " + prettify(src) + " with options: " + options);
            }
        }

        return exp;
    }

    private void doTestCompareReferenceImpl(StringBuffer src) throws Exception{
        // test toASCII
        src.setLength(0);
        src.append("[");
        StringBuffer asciiLabel = _doTestCompareReferenceImpl(src, true, IDNA.ALLOW_UNASSIGNED);
        _doTestCompareReferenceImpl(src, true, IDNA.DEFAULT);
        _doTestCompareReferenceImpl(src, true, IDNA.USE_STD3_RULES);
        _doTestCompareReferenceImpl(src, true, IDNA.USE_STD3_RULES | IDNA.ALLOW_UNASSIGNED);

        if (asciiLabel != null) {
            // test toUnicode
            _doTestCompareReferenceImpl(src, false, IDNA.ALLOW_UNASSIGNED);
            _doTestCompareReferenceImpl(src, false, IDNA.DEFAULT);
            _doTestCompareReferenceImpl(src, false, IDNA.USE_STD3_RULES);
            _doTestCompareReferenceImpl(src, false, IDNA.USE_STD3_RULES | IDNA.ALLOW_UNASSIGNED);
        }
    }

    @Test
    public void TestCompareRefImpl() throws Exception {
        for (int i = 65; i < 0x10FFFF; i++) {
            StringBuffer src = new StringBuffer();
            if (isQuick() == true && i > 0x0FFF) {
                return;
            }
            if (i == 0x30000) {
                // jump to E0000, no characters assigned in plain 3 to plain 13 as of Unicode 6.0
                i = 0xE0000;
            }
            UTF16.append(src, i);
            doTestCompareReferenceImpl(src);
        }
    }

    @Test
    public void TestJB4490(){
        String[] in = new String[]{
                "\u00F5\u00dE\u00dF\u00dD",
                "\uFB00\uFB01"
               };
        for ( int i=0; i< in.length; i++){
            try{
                String ascii = IDNA.convertToASCII(in[i],IDNA.DEFAULT).toString();
                try{
                    String unicode = IDNA.convertToUnicode(ascii,IDNA.DEFAULT).toString();
                    logln("result " + unicode);
                }catch(StringPrepParseException ex){
                    errln("Unexpected exception for convertToUnicode: " + ex.getMessage());
                }
            }catch(StringPrepParseException ex){
                errln("Unexpected exception for convertToASCII: " + ex.getMessage());
            }
        }
    }
    @Test
    public void TestJB4475(){
        String[] in = new String[]{
                        "TEST",
                        "test"
                       };
        for ( int i=0; i< in.length; i++){

            try{
                String ascii = IDNA.convertToASCII(in[i],IDNA.DEFAULT).toString();
                if(!ascii.equals(in[i])){
                    errln("Did not get the expected string for convertToASCII. Expected: "+ in[i] +" Got: " + ascii);
                }
            }catch(StringPrepParseException ex){
                errln("Unexpected exception: " + ex.getMessage());
            }
        }

    }

    @Test
    public void TestDebug(){
        try{
            String src = "\u00ED4dn";
            String uni = IDNA.convertToUnicode(src,IDNA.DEFAULT).toString();
            if(!uni.equals(src)){
                errln("Did not get the expected result. Expected: "+ prettify(src) +" Got: " +uni);
            }
        }catch(StringPrepParseException ex){
            logln("Unexpected exception: " + ex.getMessage());
        }
        try{
            String ascii = IDNA.convertToASCII("\u00AD",IDNA.DEFAULT).toString();
            if(ascii!=null){
                errln("Did not get the expected exception");
            }
        }catch(StringPrepParseException ex){
            logln("Got the expected exception: " + ex.getMessage());
        }
    }
    @Test
    public void TestJB5273(){
        String INVALID_DOMAIN_NAME = "xn--m\u00FCller.de";
        try {
            IDNA.convertIDNToUnicode(INVALID_DOMAIN_NAME, IDNA.DEFAULT);
            IDNA.convertIDNToUnicode(INVALID_DOMAIN_NAME, IDNA.USE_STD3_RULES);

        } catch (StringPrepParseException ex) {
            errln("Unexpected exception: " + ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException ex) {
            errln("Got an ArrayIndexOutOfBoundsException calling convertIDNToUnicode(\"" + INVALID_DOMAIN_NAME + "\")");
        }

        String domain = "xn--m\u00FCller.de";
        try{
            IDNA.convertIDNToUnicode(domain, IDNA.DEFAULT);
        }catch(StringPrepParseException ex){
            logln("Got the expected exception. "+ex.getMessage());
        }catch (Exception ex){
            errln("Unexpected exception: " + ex.getMessage());
        }
        try{
            IDNA.convertIDNToUnicode(domain, IDNA.USE_STD3_RULES);
        }catch(StringPrepParseException ex){
            logln("Got the expected exception. "+ex.getMessage());
        }catch (Exception ex){
            errln("Unexpected exception: " + ex.getMessage());
        }
        try{
            IDNA.convertToUnicode("xn--m\u00FCller", IDNA.DEFAULT);
        }catch(Exception ex){
            errln("ToUnicode operation failed! "+ex.getMessage());
        }
        try{
            IDNA.convertToUnicode("xn--m\u00FCller", IDNA.USE_STD3_RULES);
        }catch(Exception ex){
            errln("ToUnicode operation failed! "+ex.getMessage());
        }
        try{
            IDNA.convertIDNToUnicode("xn--m\u1234ller", IDNA.USE_STD3_RULES);
        }catch(StringPrepParseException ex){
            errln("ToUnicode operation failed! "+ex.getMessage());
        }
    }

    @Test
    public void TestLength(){
        String ul = "my_very_very_very_very_very_very_very_very_very_very_very_very_very_long_and_incredibly_uncreative_domain_label";

        /* this unicode string is longer than MAX_LABEL_BUFFER_SIZE and produces an
           IDNA prepared string (including xn--)that is exactly 63 bytes long */
        String ul1 ="\uC138\uACC4\uC758\uBAA8\uB4E0\uC0AC\uB78C\uB4E4\uC774"+
                    "\uD55C\uAD6D\uC5B4\uB97C\uC774\u00AD\u034F\u1806\u180B"+
                    "\u180C\u180D\u200B\u200C\u200D\u2060\uFE00\uFE01\uFE02"+
                    "\uFE03\uFE04\uFE05\uFE06\uFE07\uFE08\uFE09\uFE0A\uFE0B"+
                    "\uFE0C\uFE0D\uFE0E\uFE0F\uFEFF\uD574\uD55C\uB2E4\uBA74"+
                    "\uC138\u0041\u00AD\u034F\u1806\u180B\u180C\u180D\u200B"+
                    "\u200C\u200D\u2060\uFE00\uFE01\uFE02\uFE03\uFE04\uFE05"+
                    "\uFE06\uFE07\uFE08\uFE09\uFE0A\uFE0B\uFE0C\uFE0D\uFE0E"+
                    "\uFE0F\uFEFF\u00AD\u034F\u1806\u180B\u180C\u180D\u200B"+
                    "\u200C\u200D\u2060\uFE00\uFE01\uFE02\uFE03\uFE04\uFE05"+
                    "\uFE06\uFE07\uFE08\uFE09\uFE0A\uFE0B\uFE0C\uFE0D\uFE0E"+
                    "\uFE0F\uFEFF\u00AD\u034F\u1806\u180B\u180C\u180D\u200B"+
                    "\u200C\u200D\u2060\uFE00\uFE01\uFE02\uFE03\uFE04\uFE05"+
                    "\uFE06\uFE07\uFE08\uFE09\uFE0A\uFE0B\uFE0C\uFE0D\uFE0E"+
                    "\uFE0F\uFEFF";
        try{
            IDNA.convertToASCII(ul, IDNA.DEFAULT);
            errln("IDNA.convertToUnicode did not fail!");
        }catch (StringPrepParseException ex){
            if(ex.getError()!= StringPrepParseException.LABEL_TOO_LONG_ERROR){
                errln("IDNA.convertToASCII failed with error: "+ex.toString());
            }else{
                logln("IDNA.convertToASCII(ul, IDNA.DEFAULT) Succeeded");
            }
        }
        try{
            IDNA.convertToASCII(ul1, IDNA.DEFAULT);
        }catch (StringPrepParseException ex){
            errln("IDNA.convertToASCII failed with error: "+ex.toString());
        }
        try{
            IDNA.convertToUnicode(ul1, IDNA.DEFAULT);
        }catch (StringPrepParseException ex){
            errln("IDNA.convertToASCII failed with error: "+ex.toString());
        }
        try{
            IDNA.convertToUnicode(ul, IDNA.DEFAULT);
        }catch (StringPrepParseException ex){
            errln("IDNA.convertToASCII failed with error: "+ex.toString());
        }

        String idn = "my_very_very_long_and_incredibly_uncreative_domain_label.my_very_very_long_and_incredibly_uncreative_domain_label.my_very_very_long_and_incredibly_uncreative_domain_label.my_very_very_long_and_incredibly_uncreative_domain_label.my_very_very_long_and_incredibly_uncreative_domain_label.my_very_very_long_and_incredibly_uncreative_domain_label.ibm.com";
        try{
            IDNA.convertIDNToASCII(idn, IDNA.DEFAULT);
            errln("IDNA.convertToUnicode did not fail!");
        }catch (StringPrepParseException ex){
            if(ex.getError()!= StringPrepParseException.DOMAIN_NAME_TOO_LONG_ERROR){
                errln("IDNA.convertToASCII failed with error: "+ex.toString());
            }else{
                logln("IDNA.convertToASCII(idn, IDNA.DEFAULT) Succeeded");
            }
        }
        try{
            IDNA.convertIDNToUnicode(idn, IDNA.DEFAULT);
            errln("IDNA.convertToUnicode did not fail!");
        }catch (StringPrepParseException ex){
            if(ex.getError()!= StringPrepParseException.DOMAIN_NAME_TOO_LONG_ERROR){
                errln("IDNA.convertToUnicode failed with error: "+ex.toString());
            }else{
                logln("IDNA.convertToUnicode(idn, IDNA.DEFAULT) Succeeded");
            }
        }
    }

    /* Tests the method public static StringBuffer convertToASCII(String src, int options) */
    @Test
    public void TestConvertToASCII() {
        try {
            if (!IDNA.convertToASCII("dummy", 0).toString().equals("dummy")) {
                errln("IDNA.convertToASCII(String,int) was suppose to return the same string passed.");
            }
        } catch (Exception e) {
            errln("IDNA.convertToASCII(String,int) was not suppose to return an exception.");
        }
    }

    /*
     * Tests the method public static StringBuffer convertIDNToASCII(UCharacterIterator src, int options), method public
     * static StringBuffer public static StringBuffer convertIDNToASCII(StringBuffer src, int options), public static
     * StringBuffer convertIDNToASCII(UCharacterIterator src, int options)
     */
    @Test
    public void TestConvertIDNToASCII() {
        try {
            UCharacterIterator uci = UCharacterIterator.getInstance("dummy");
            if (!IDNA.convertIDNToASCII(uci, 0).toString().equals("dummy")) {
                errln("IDNA.convertIDNToASCII(UCharacterIterator, int) was suppose to "
                        + "return the same string passed.");
            }
            if (!IDNA.convertIDNToASCII(new StringBuffer("dummy"), 0).toString().equals("dummy")) {
                errln("IDNA.convertIDNToASCII(StringBuffer, int) was suppose to " + "return the same string passed.");
            }
        } catch (Exception e) {
            errln("IDNA.convertIDNToASCII was not suppose to return an exception.");
        }
    }

    /*
     * Tests the method public static StringBuffer convertToUnicode(String src, int options), public static StringBuffer
     * convertToUnicode(StringBuffer src, int options)
     */
    @Test
    public void TestConvertToUnicode() {
        try {
            if (!IDNA.convertToUnicode("dummy", 0).toString().equals("dummy")) {
                errln("IDNA.convertToUnicode(String, int) was suppose to " + "return the same string passed.");
            }
            if (!IDNA.convertToUnicode(new StringBuffer("dummy"), 0).toString().equals("dummy")) {
                errln("IDNA.convertToUnicode(StringBuffer, int) was suppose to " + "return the same string passed.");
            }
        } catch (Exception e) {
            errln("IDNA.convertToUnicode was not suppose to return an exception.");
        }
    }

    /* Tests the method public static StringBuffer convertIDNToUnicode(UCharacterIterator src, int options) */
    @Test
    public void TestConvertIDNToUnicode() {
        try {
            UCharacterIterator uci = UCharacterIterator.getInstance("dummy");
            if (!IDNA.convertIDNToUnicode(uci, 0).toString().equals("dummy")) {
                errln("IDNA.convertIDNToUnicode(UCharacterIterator, int) was suppose to "
                        + "return the same string passed.");
            }
            if (!IDNA.convertIDNToUnicode(new StringBuffer("dummy"), 0).toString().equals("dummy")) {
                errln("IDNA.convertIDNToUnicode(StringBuffer, int) was suppose to " + "return the same string passed.");
            }
        } catch (Exception e) {
            errln("IDNA.convertIDNToUnicode was not suppose to return an exception.");
        }
    }

    /* Tests the method public static int compare */
    @Test
    public void TestIDNACompare() {
        // Testing the method public static int compare(String s1, String s2, int options)
        try {
            IDNA.compare((String) null, (String) null, 0);
            errln("IDNA.compare((String)null,(String)null) was suppose to return an exception.");
        } catch (Exception e) {
        }

        try {
            IDNA.compare((String) null, "dummy", 0);
            errln("IDNA.compare((String)null,'dummy') was suppose to return an exception.");
        } catch (Exception e) {
        }

        try {
            IDNA.compare("dummy", (String) null, 0);
            errln("IDNA.compare('dummy',(String)null) was suppose to return an exception.");
        } catch (Exception e) {
        }

        try {
            if (IDNA.compare("dummy", "dummy", 0) != 0) {
                errln("IDNA.compare('dummy','dummy') was suppose to return a 0.");
            }
        } catch (Exception e) {
            errln("IDNA.compare('dummy','dummy') was not suppose to return an exception.");
        }

        // Testing the method public static int compare(StringBuffer s1, StringBuffer s2, int options)
        try {
            IDNA.compare((StringBuffer) null, (StringBuffer) null, 0);
            errln("IDNA.compare((StringBuffer)null,(StringBuffer)null) was suppose to return an exception.");
        } catch (Exception e) {
        }

        try {
            IDNA.compare((StringBuffer) null, new StringBuffer("dummy"), 0);
            errln("IDNA.compare((StringBuffer)null,'dummy') was suppose to return an exception.");
        } catch (Exception e) {
        }

        try {
            IDNA.compare(new StringBuffer("dummy"), (StringBuffer) null, 0);
            errln("IDNA.compare('dummy',(StringBuffer)null) was suppose to return an exception.");
        } catch (Exception e) {
        }

        try {
            if (IDNA.compare(new StringBuffer("dummy"), new StringBuffer("dummy"), 0) != 0) {
                errln("IDNA.compare(new StringBuffer('dummy'),new StringBuffer('dummy')) was suppose to return a 0.");
            }
        } catch (Exception e) {
            errln("IDNA.compare(new StringBuffer('dummy'),new StringBuffer('dummy')) was not suppose to return an exception.");
        }

        // Testing the method public static int compare(UCharacterIterator s1, UCharacterIterator s2, int options)
        UCharacterIterator uci = UCharacterIterator.getInstance("dummy");
        try {
            IDNA.compare((UCharacterIterator) null, (UCharacterIterator) null, 0);
            errln("IDNA.compare((UCharacterIterator)null,(UCharacterIterator)null) was suppose to return an exception.");
        } catch (Exception e) {
        }

        try {
            IDNA.compare((UCharacterIterator) null, uci, 0);
            errln("IDNA.compare((UCharacterIterator)null,UCharacterIterator) was suppose to return an exception.");
        } catch (Exception e) {
        }

        try {
            IDNA.compare(uci, (UCharacterIterator) null, 0);
            errln("IDNA.compare(UCharacterIterator,(UCharacterIterator)null) was suppose to return an exception.");
        } catch (Exception e) {
        }

        try {
            if (IDNA.compare(uci, uci, 0) != 0) {
                errln("IDNA.compare(UCharacterIterator('dummy'),UCharacterIterator('dummy')) was suppose to return a 0.");
            }
        } catch (Exception e) {
            errln("IDNA.compare(UCharacterIterator('dummy'),UCharacterIterator('dummy')) was not suppose to return an exception.");
        }
    }
}
