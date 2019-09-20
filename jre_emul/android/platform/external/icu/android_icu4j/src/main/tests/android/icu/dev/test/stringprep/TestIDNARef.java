/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2003-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
*/
package android.icu.dev.test.stringprep;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.StringPrepParseException;
import android.icu.text.UCharacterIterator;

/**
 * @author ram
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TestIDNARef extends TestFmwk {
    private StringPrepParseException unassignedException = new StringPrepParseException("",StringPrepParseException.UNASSIGNED_ERROR);

    @Test
    public void TestToUnicode() throws Exception{
        try{
            for(int i=0; i<TestData.asciiIn.length; i++){
                // test StringBuffer toUnicode
                doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNAReference.DEFAULT, null);
                doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNAReference.ALLOW_UNASSIGNED, null);
                //doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNAReference.USE_STD3_RULES, null); 
                //doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNAReference.USE_STD3_RULES|IDNAReference.ALLOW_UNASSIGNED, null); 
        
            }
        }catch(java.lang.ExceptionInInitializerError ex){
            warnln("Could not load NamePrepTransform data");
        }catch(java.lang.NoClassDefFoundError ex){
            warnln("Could not load NamePrepTransform data");
        } 
    }
    
    @Test
    public void TestToASCII() throws Exception{
        try{
            for(int i=0; i<TestData.asciiIn.length; i++){
                // test StringBuffer toUnicode
                doTestToASCII(new String(TestData.unicodeIn[i]),TestData.asciiIn[i],IDNAReference.DEFAULT, null);
                doTestToASCII(new String(TestData.unicodeIn[i]),TestData.asciiIn[i],IDNAReference.ALLOW_UNASSIGNED, null);
                //doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNAReference.USE_STD3_RULES, null); 
                //doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNAReference.USE_STD3_RULES|IDNAReference.ALLOW_UNASSIGNED, null); 
        
            }
        }catch(java.lang.ExceptionInInitializerError ex){
            warnln("Could not load NamePrepTransform data");
        }catch(java.lang.NoClassDefFoundError ex){
            warnln("Could not load NamePrepTransform data");
        } 
    }
    
    @Test
    public void TestIDNToASCII() throws Exception{
        try{
            for(int i=0; i<TestData.domainNames.length; i++){
                doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNAReference.DEFAULT, null);
                doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNAReference.ALLOW_UNASSIGNED, null);
                doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNAReference.USE_STD3_RULES, null);
                doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNAReference.ALLOW_UNASSIGNED|IDNAReference.USE_STD3_RULES, null);
            }
            
            for(int i=0; i<TestData.domainNames1Uni.length; i++){
                doTestIDNToASCII(TestData.domainNames1Uni[i],TestData.domainNamesToASCIIOut[i],IDNAReference.DEFAULT, null);
                doTestIDNToASCII(TestData.domainNames1Uni[i],TestData.domainNamesToASCIIOut[i],IDNAReference.ALLOW_UNASSIGNED, null);
                doTestIDNToASCII(TestData.domainNames1Uni[i],TestData.domainNamesToASCIIOut[i],IDNAReference.USE_STD3_RULES, null);
                doTestIDNToASCII(TestData.domainNames1Uni[i],TestData.domainNamesToASCIIOut[i],IDNAReference.ALLOW_UNASSIGNED|IDNAReference.USE_STD3_RULES, null);
    
            }
        }catch(java.lang.ExceptionInInitializerError ex){
            warnln("Could not load NamePrepTransform data");
        }catch(java.lang.NoClassDefFoundError ex){
            warnln("Could not load NamePrepTransform data");
        } 
    }
    @Test
    public void TestIDNToUnicode() throws Exception{
        try{
            for(int i=0; i<TestData.domainNames.length; i++){
                doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNAReference.DEFAULT, null);
                doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNAReference.ALLOW_UNASSIGNED, null);
                doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNAReference.USE_STD3_RULES, null);
                doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNAReference.ALLOW_UNASSIGNED|IDNAReference.USE_STD3_RULES, null);
            }
            for(int i=0; i<TestData.domainNamesToASCIIOut.length; i++){
                doTestIDNToUnicode(TestData.domainNamesToASCIIOut[i],TestData.domainNamesToUnicodeOut[i],IDNAReference.DEFAULT, null);
                doTestIDNToUnicode(TestData.domainNamesToASCIIOut[i],TestData.domainNamesToUnicodeOut[i],IDNAReference.ALLOW_UNASSIGNED, null);
            }
        }catch(java.lang.ExceptionInInitializerError ex){
            warnln("Could not load NamePrepTransform data");
        }catch(java.lang.NoClassDefFoundError ex){
            warnln("Could not load NamePrepTransform data");
        } 
    }
    
    private void doTestToUnicode(String src, String expected, int options, Object expectedException) 
                throws Exception{

        if (!IDNAReference.isReady()) {
            logln("Transliterator is not available on this environment.  Skipping doTestToUnicode.");
            return;
        }

        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{
            
            StringBuffer out = IDNAReference.convertToUnicode(src,options);
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
            
            StringBuffer out = IDNAReference.convertToUnicode(inBuf,options);
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
            StringBuffer out = IDNAReference.convertToUnicode(inIter,options);
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

        if (!IDNAReference.isReady()) {
            logln("Transliterator is not available on this environment.  Skipping doTestIDNToUnicode.");
            return;
        }

        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{
            
            StringBuffer out = IDNAReference.convertIDNToUnicode(src,options);
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
            StringBuffer out = IDNAReference.convertIDNToUnicode(inBuf,options);
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
            StringBuffer out = IDNAReference.convertIDNToUnicode(inIter,options);
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

        if (!IDNAReference.isReady()) {
            logln("Transliterator is not available on this environment.  Skipping doTestToASCII.");
            return;
        }

        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{
            
            StringBuffer out = IDNAReference.convertToASCII(src,options);
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
            StringBuffer out = IDNAReference.convertToASCII(inBuf,options);
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
            StringBuffer out = IDNAReference.convertToASCII(inIter,options);
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

        if (!IDNAReference.isReady()) {
            logln("Transliterator is not available on this environment.  Skipping doTestIDNToASCII.");
            return;
        }

        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{
            
            StringBuffer out = IDNAReference.convertIDNToASCII(src,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
                errln("convertToIDNAReferenceASCII did not return expected result with options : "+ options + 
                      " Expected: " + expected+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToIDNAReferenceASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertToIDNAReferenceASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
        try{
            StringBuffer out = IDNAReference.convertIDNtoASCII(inBuf,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToIDNAReferenceASCII did not return expected result with options : "+ options + 
                     " Expected: " + expected+" Got: "+out);
            }           
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToIDNAReferenceSCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertToIDNAReferenceSCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
        
        try{
            StringBuffer out = IDNAReference.convertIDNtoASCII(inIter,options);
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
        try{
            for(int i=0; i<TestData.conformanceTestCases.length;i++){
                
                TestData.ConformanceTestCase testCase = TestData.conformanceTestCases[i];
                if(testCase.expected != null){
                    //Test toASCII
                    doTestToASCII(testCase.input,testCase.output,IDNAReference.DEFAULT,testCase.expected);
                    doTestToASCII(testCase.input,testCase.output,IDNAReference.ALLOW_UNASSIGNED,testCase.expected);
                }
                //Test toUnicode
                //doTestToUnicode(testCase.input,testCase.output,IDNAReference.DEFAULT,testCase.expected);
            }
        }catch(java.lang.ExceptionInInitializerError ex){
            warnln("Could not load NamePrepTransform data");
        }catch(java.lang.NoClassDefFoundError ex){
            warnln("Could not load NamePrepTransform data");
        } 
    }
    @Test
    public void TestNamePrepConformance() throws Exception{
        try{
            NamePrepTransform namePrep = NamePrepTransform.getInstance();
            if (!namePrep.isReady()) {
                logln("Transliterator is not available on this environment.");
                return;
            }
            for(int i=0; i<TestData.conformanceTestCases.length;i++){
                TestData.ConformanceTestCase testCase = TestData.conformanceTestCases[i];
                UCharacterIterator iter = UCharacterIterator.getInstance(testCase.input);
                try{
                    StringBuffer output = namePrep.prepare(iter,NamePrepTransform.NONE);
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
                    StringBuffer output = namePrep.prepare(iter,NamePrepTransform.ALLOW_UNASSIGNED);
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
        }catch(java.lang.ExceptionInInitializerError e){
            warnln("Could not load NamePrepTransformData");
        }catch(java.lang.NoClassDefFoundError ex){
            warnln("Could not load NamePrepTransform data");
        } 
        
    }
    @Test
    public void TestErrorCases() throws Exception{
        try{
            for(int i=0; i < TestData.errorCases.length; i++){
                TestData.ErrorCase errCase = TestData.errorCases[i];
                if(errCase.testLabel==true){
                    // Test ToASCII
                    doTestToASCII(new String(errCase.unicode),errCase.ascii,IDNAReference.DEFAULT,errCase.expected);
                    doTestToASCII(new String(errCase.unicode),errCase.ascii,IDNAReference.ALLOW_UNASSIGNED,errCase.expected);
                    if(errCase.useSTD3ASCIIRules){
                        doTestToASCII(new String(errCase.unicode),errCase.ascii,IDNAReference.USE_STD3_RULES,errCase.expected);
                    }
                }
                if(errCase.useSTD3ASCIIRules!=true){
                    
                    // Test IDNToASCII
                    doTestIDNToASCII(new String(errCase.unicode),errCase.ascii,IDNAReference.DEFAULT,errCase.expected);
                    doTestIDNToASCII(new String(errCase.unicode),errCase.ascii,IDNAReference.ALLOW_UNASSIGNED,errCase.expected);
                    
                }else{
                    doTestIDNToASCII(new String(errCase.unicode),errCase.ascii,IDNAReference.USE_STD3_RULES,errCase.expected);
                }
                
                //TestToUnicode
                if(errCase.testToUnicode==true){
                    if(errCase.useSTD3ASCIIRules!=true){
                        // Test IDNToUnicode
                        doTestIDNToUnicode(errCase.ascii,new String(errCase.unicode),IDNAReference.DEFAULT,errCase.expected);
                        doTestIDNToUnicode(errCase.ascii,new String(errCase.unicode),IDNAReference.ALLOW_UNASSIGNED,errCase.expected);
                    
                    }else{
                        doTestIDNToUnicode(errCase.ascii,new String(errCase.unicode),IDNAReference.USE_STD3_RULES,errCase.expected);
                    }
                }
            }
        }catch(java.lang.ExceptionInInitializerError ex){
            warnln("Could not load NamePrepTransform data");
        }catch(java.lang.NoClassDefFoundError ex){
            warnln("Could not load NamePrepTransform data");
        } 
    }
    private void doTestCompare(String s1, String s2, boolean isEqual){

        if (!IDNAReference.isReady()) {
            logln("Transliterator is not available on this environment.  Skipping doTestCompare.");
            return;
        }

        try{
            int retVal = IDNAReference.compare(s1,s2,IDNAReference.DEFAULT);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                      " s2: "+prettify(s2));
            }
            retVal = IDNAReference.compare(new StringBuffer(s1), new StringBuffer(s2), IDNAReference.DEFAULT);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                     " s2: "+prettify(s2));
            }
            retVal = IDNAReference.compare(UCharacterIterator.getInstance(s1), UCharacterIterator.getInstance(s2), IDNAReference.DEFAULT);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                     " s2: "+prettify(s2));
            }
        }catch(Exception e){
            e.printStackTrace();
            errln("Unexpected exception thrown by IDNAReference.compare");
        }
        
        try{
            int retVal = IDNAReference.compare(s1,s2,IDNAReference.ALLOW_UNASSIGNED);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                      " s2: "+prettify(s2));
            }
            retVal = IDNAReference.compare(new StringBuffer(s1), new StringBuffer(s2), IDNAReference.ALLOW_UNASSIGNED);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                     " s2: "+prettify(s2));
            }
            retVal = IDNAReference.compare(UCharacterIterator.getInstance(s1), UCharacterIterator.getInstance(s2), IDNAReference.ALLOW_UNASSIGNED);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                     " s2: "+prettify(s2));
            }
        }catch(Exception e){
            errln("Unexpected exception thrown by IDNAReference.compare");
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
        try{
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
        }catch(java.lang.ExceptionInInitializerError ex){
            warnln("Could not load NamePrepTransform data");
        }catch(java.lang.NoClassDefFoundError ex){
            warnln("Could not load NamePrepTransform data");
        } 
    }

    //  test and ascertain
    //  func(func(func(src))) == func(src)
     private void doTestChainingToASCII(String source) throws Exception {

        if (!IDNAReference.isReady()) {
            logln("Transliterator is not available on this environment.  Skipping doTestChainingToASCII.");
            return;
        }

        StringBuffer expected; 
        StringBuffer chained;
        
        // test convertIDNToASCII
        expected = IDNAReference.convertIDNToASCII(source,IDNAReference.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNAReference.convertIDNtoASCII(chained,IDNAReference.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertIDNToASCII");
        }
        // test convertIDNToA
        expected = IDNAReference.convertToASCII(source,IDNAReference.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNAReference.convertToASCII(chained,IDNAReference.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertToASCII");
        } 
        
    }
    //  test and ascertain
    //  func(func(func(src))) == func(src)
    public void doTestChainingToUnicode(String source) throws Exception {

        if (!IDNAReference.isReady()) {
            logln("Transliterator is not available on this environment.  Skipping doTestChainingToUnicode.");
            return;
        }

        StringBuffer expected; 
        StringBuffer chained;
        
        // test convertIDNToUnicode
        expected = IDNAReference.convertIDNToUnicode(source,IDNAReference.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNAReference.convertIDNToUnicode(chained,IDNAReference.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertIDNToUnicode");
        }
        // test convertIDNToA
        expected = IDNAReference.convertToUnicode(source,IDNAReference.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNAReference.convertToUnicode(chained,IDNAReference.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertToUnicode");
        }

    }
    @Test
    public void TestChaining() throws Exception{
        try{
            for(int i=0; i< TestData.unicodeIn.length; i++){
                doTestChainingToASCII(new String(TestData.unicodeIn[i]));
            }
            for(int i=0; i< TestData.asciiIn.length; i++){
                doTestChainingToUnicode(TestData.asciiIn[i]);
            }
        }catch(java.lang.ExceptionInInitializerError ex){
            warnln("Could not load NamePrepTransform data");
        }catch(java.lang.NoClassDefFoundError ex){
            warnln("Could not load NamePrepTransform data");
        } 
    }
    @Test
    public void TestRootLabelSeparator() throws Exception{
        String www = "www.";
        String com = ".com."; /*root label separator*/
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
        try{
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
        }catch(java.lang.ExceptionInInitializerError ex){
            warnln("Could not load NamePrepTransform data");
        }catch(java.lang.NoClassDefFoundError ex){
            warnln("Could not load NamePrepTransform data");
        } 

    }
}
