/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.dev.test.normalizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.StringCharacterIterator;

import org.junit.Ignore;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.impl.Utility;
import android.icu.text.Normalizer;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;

public class ConformanceTest extends TestFmwk {

    Normalizer normalizer;

    public ConformanceTest() {
        // Doesn't matter what the string and mode are; we'll change
        // them later as needed.
        normalizer = new Normalizer("", Normalizer.NFC, 0);
    }
    // more interesting conformance test cases, not in the unicode.org NormalizationTest.txt
    static  String[] moreCases ={
        // Markus 2001aug30
        "0061 0332 0308;00E4 0332;0061 0332 0308;00E4 0332;0061 0332 0308; # Markus 0",
    
        // Markus 2001oct26 - test edge case for iteration: U+0f73.cc==0 but decomposition.lead.cc==129
        "0061 0301 0F73;00E1 0F71 0F72;0061 0F71 0F72 0301;00E1 0F71 0F72;0061 0F71 0F72 0301; # Markus 1"
    };

    /**
     * Test the conformance of Normalizer to
     * http://www.unicode.org/unicode/reports/tr15/conformance/Draft-TestSuite.txt.* http://www.unicode.org/Public/UNIDATA/NormalizationTest.txt
     * This file must be located at the path specified as TEST_SUITE_FILE.
     */
    @Test
    public void TestConformance() throws Exception{
        runConformance("unicode/NormalizationTest.txt",0);
    }
    @Test
    public void TestConformance_3_2() throws Exception{
        runConformance("unicode/NormalizationTest-3.2.0.txt",Normalizer.UNICODE_3_2);
    }
    
    public void runConformance(String fileName, int options) throws Exception{
        String line = null;
        String[] fields = new String[5];
        StringBuffer buf = new StringBuffer();
        int passCount = 0;
        int failCount = 0;
        UnicodeSet other = new UnicodeSet(0, 0x10ffff);
        int c=0;
        BufferedReader input = null;
        try {
            input = TestUtil.getDataReader(fileName);
            for (int count = 0;;++count) {
                line = input.readLine();
                if (line == null) {
                    //read the extra test cases
                    if(count > moreCases.length) {
                        count = 0;
                    } else if(count == moreCases.length) {
                        // all done
                        break;
                    }
                    line = moreCases[count++];
                }
                if (line.length() == 0) continue;

                // Expect 5 columns of this format:
                // 1E0C;1E0C;0044 0323;1E0C;0044 0323; # <comments>

                // Skip comments
                if (line.charAt(0) == '#'  || line.charAt(0)=='@') continue;

                // Parse out the fields
                hexsplit(line, ';', fields, buf);
                
                // Remove a single code point from the "other" UnicodeSet
                if(fields[0].length()==UTF16.moveCodePointOffset(fields[0],0, 1)) {
                    c=UTF16.charAt(fields[0],0); 
                    if(0xac20<=c && c<=0xd73f) {
                        // not an exhaustive test run: skip most Hangul syllables
                        if(c==0xac20) {
                            other.remove(0xac20, 0xd73f);
                        }
                        continue;
                    }
                    other.remove(c);
                }
                if (checkConformance(fields, line,options)) {
                    ++passCount;
                } else {
                    ++failCount;
                }
                if ((count % 1000) == 999) {
                    logln("Line " + (count+1));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException("Couldn't read file "
              + ex.getClass().getName() + " " + ex.getMessage()
              + " line = " + line
              );
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (failCount != 0) {
            errln("Total: " + failCount + " lines failed, " +
                  passCount + " lines passed");
        } else {
            logln("Total: " + passCount + " lines passed");
        }
    }
    
    /**
     * Verify the conformance of the given line of the Unicode
     * normalization (UTR 15) test suite file.  For each line,
     * there are five columns, corresponding to field[0]..field[4].
     *
     * The following invariants must be true for all conformant implementations
     *  c2 == NFC(c1) == NFC(c2) == NFC(c3)
     *  c3 == NFD(c1) == NFD(c2) == NFD(c3)
     *  c4 == NFKC(c1) == NFKC(c2) == NFKC(c3) == NFKC(c4) == NFKC(c5)
     *  c5 == NFKD(c1) == NFKD(c2) == NFKD(c3) == NFKD(c4) == NFKD(c5)
     *
     * @param field the 5 columns
     * @param line the source line from the test suite file
     * @return true if the test passes
     */
    private boolean checkConformance(String[] field, String line, int options) throws Exception{
        boolean pass = true;
        StringBuffer buf = new StringBuffer(); // scratch
        String out,fcd;
        int i=0;
        for (i=0; i<5; ++i) {
            if (i<3) {
                out = Normalizer.normalize(field[i], Normalizer.NFC, options);
                pass &= assertEqual("C", field[i], out, field[1], "c2!=C(c" + (i+1));
                
                out = iterativeNorm(field[i], Normalizer.NFC, buf, +1,options);
                pass &= assertEqual("C(+1)", field[i], out, field[1], "c2!=C(c" + (i+1));
                
                out = iterativeNorm(field[i], Normalizer.NFC, buf, -1,options);
                pass &= assertEqual("C(-1)", field[i], out, field[1], "c2!=C(c" + (i+1));

                out = iterativeNorm(new StringCharacterIterator(field[i]), Normalizer.NFC, buf, +1,options);
                pass &= assertEqual("C(+1)", field[i], out, field[1], "c2!=C(c" + (i+1));
                
                out = iterativeNorm(new StringCharacterIterator(field[i]), Normalizer.NFC, buf, -1,options);
                pass &= assertEqual("C(-1)", field[i], out, field[1], "c2!=C(c" + (i+1));
                 
                out = Normalizer.normalize(field[i], Normalizer.NFD);
                pass &= assertEqual("D", field[i], out, field[2], "c3!=D(c" + (i+1));
                
                out = iterativeNorm(field[i], Normalizer.NFD, buf, +1,options);
                pass &= assertEqual("D(+1)", field[i], out, field[2], "c3!=D(c" + (i+1));
                
                out = iterativeNorm(field[i], Normalizer.NFD, buf, -1,options);
                pass &= assertEqual("D(-1)", field[i], out, field[2], "c3!=D(c" + (i+1));

                out = iterativeNorm(new StringCharacterIterator(field[i]), Normalizer.NFD, buf, +1,options);
                pass &= assertEqual("D(+1)", field[i], out, field[2], "c3!=D(c" + (i+1));
                
                out = iterativeNorm(new StringCharacterIterator(field[i]), Normalizer.NFD, buf, -1,options);
                pass &= assertEqual("D(-1)", field[i], out, field[2], "c3!=D(c" + (i+1));
                
                cross(field[2] /*NFD String*/, field[1]/*NFC String*/, Normalizer.NFC);
                cross(field[1] /*NFC String*/, field[2]/*NFD String*/, Normalizer.NFD);
            }
            out = Normalizer.normalize(field[i], Normalizer.NFKC,options);
            pass &= assertEqual("KC", field[i], out, field[3], "c4!=KC(c" + (i+1));
            
            out = iterativeNorm(field[i], Normalizer.NFKC, buf, +1,options);
            pass &= assertEqual("KD(+1)", field[i], out, field[3], "c4!=KC(c" + (i+1));
            
            out = iterativeNorm(field[i], Normalizer.NFKC, buf, -1,options);
            pass &= assertEqual("KD(-1)", field[i], out, field[3], "c4!=KC(c" + (i+1));

            out = iterativeNorm(new StringCharacterIterator(field[i]), Normalizer.NFKC, buf, +1,options);
            pass &= assertEqual("KD(+1)", field[i], out, field[3], "c4!=KC(c" + (i+1));
            
            out = iterativeNorm(new StringCharacterIterator(field[i]), Normalizer.NFKC, buf, -1,options);
            pass &= assertEqual("KD(-1)", field[i], out, field[3], "c4!=KC(c" + (i+1));
              

            out = Normalizer.normalize(field[i], Normalizer.NFKD,options);
            pass &= assertEqual("KD", field[i], out, field[4], "c5!=KD(c" + (i+1));
            
            out = iterativeNorm(field[i], Normalizer.NFKD, buf, +1,options);
            pass &= assertEqual("KD(+1)", field[i], out, field[4], "c5!=KD(c" + (i+1));
            
            out = iterativeNorm(field[i], Normalizer.NFKD, buf, -1,options);
            pass &= assertEqual("KD(-1)", field[i], out, field[4], "c5!=KD(c" + (i+1));
         
            out = iterativeNorm(new StringCharacterIterator(field[i]), Normalizer.NFKD, buf, +1,options);
            pass &= assertEqual("KD(+1)", field[i], out, field[4], "c5!=KD(c" + (i+1));
            
            out = iterativeNorm(new StringCharacterIterator(field[i]), Normalizer.NFKD, buf, -1,options);
            pass &= assertEqual("KD(-1)", field[i], out, field[4], "c5!=KD(c" + (i+1));
            
            cross(field[4] /*NFKD String*/, field[3]/*NFKC String*/, Normalizer.NFKC);
            cross(field[3] /*NFKC String*/, field[4]/*NFKD String*/, Normalizer.NFKD);
  
        }
        compare(field[1],field[2]);
        compare(field[0],field[1]);
        compare(field[0],field[2]);
         // test quick checks
        if(Normalizer.NO == Normalizer.quickCheck(field[1], Normalizer.NFC,options)) {
            errln("Normalizer error: quickCheck(NFC(s), Normalizer.NFC) is Normalizer.NO");
            pass = false;
        }
        if(Normalizer.NO == Normalizer.quickCheck(field[2], Normalizer.NFD,options)) {
            errln("Normalizer error: quickCheck(NFD(s), Normalizer.NFD) is Normalizer.NO");
            pass = false;
        }
        if(Normalizer.NO == Normalizer.quickCheck(field[3], Normalizer.NFKC,options)) {
            errln("Normalizer error: quickCheck(NFKC(s), Normalizer.NFKC) is Normalizer.NO");
            pass = false;
        }
        if(Normalizer.NO == Normalizer.quickCheck(field[4], Normalizer.NFKD,options)) {
            errln("Normalizer error: quickCheck(NFKD(s), Normalizer.NFKD) is Normalizer.NO");
            pass = false;
        }
    
        if(!Normalizer.isNormalized(field[1], Normalizer.NFC, options)) {
            errln("Normalizer error: isNormalized(NFC(s), Normalizer.NFC) is false");
            pass = false;
        }
        if(!field[0].equals(field[1]) && Normalizer.isNormalized(field[0], Normalizer.NFC, options)) {
            errln("Normalizer error: isNormalized(s, Normalizer.NFC) is TRUE");
            pass = false;
        }
        if(!Normalizer.isNormalized(field[3], Normalizer.NFKC, options)) {
            errln("Normalizer error: isNormalized(NFKC(s), Normalizer.NFKC) is false");
            pass = false;
        }
        if(!field[0].equals(field[3]) && Normalizer.isNormalized(field[0], Normalizer.NFKC, options)) {
            errln("Normalizer error: isNormalized(s, Normalizer.NFKC) is TRUE");
            pass = false;
        }
        // test api that takes a char[]
        if(!Normalizer.isNormalized(field[1].toCharArray(),0,field[1].length(), Normalizer.NFC,options)) {
            errln("Normalizer error: isNormalized(NFC(s), Normalizer.NFC) is false");
            pass = false;
        }
        // test api that takes a codepoint
        if(!Normalizer.isNormalized(UTF16.charAt(field[1],0), Normalizer.NFC,options)) {
            errln("Normalizer error: isNormalized(NFC(s), Normalizer.NFC) is false");
            pass = false;
        }
        // test FCD quick check and "makeFCD"
        fcd=Normalizer.normalize(field[0], Normalizer.FCD);
        if(Normalizer.NO == Normalizer.quickCheck(fcd, Normalizer.FCD,options)) {
            errln("Normalizer error: quickCheck(FCD(s), Normalizer.FCD) is Normalizer.NO");
            pass = false;
        }
        // check FCD return length
        {
            char[] fcd2 = new char[ fcd.length() * 2 ];
            char[] src = field[0].toCharArray();
            int fcdLen = Normalizer.normalize(src, 0, src.length, fcd2, fcd.length(), fcd2.length,Normalizer.FCD, 0);
            if(fcdLen != fcd.length()){
                errln("makeFCD did not return the correct length");
            }
        }
        if(Normalizer.NO == Normalizer.quickCheck(fcd, Normalizer.FCD, options)) {
            errln("Normalizer error: quickCheck(FCD(s), Normalizer.FCD) is Normalizer.NO");
            pass = false;
        }
        if(Normalizer.NO == Normalizer.quickCheck(field[2], Normalizer.FCD, options)) {
            errln("Normalizer error: quickCheck(NFD(s), Normalizer.FCD) is Normalizer.NO");
            pass = false;
        }

        if(Normalizer.NO == Normalizer.quickCheck(field[4], Normalizer.FCD, options)) {
            errln("Normalizer error: quickCheck(NFKD(s), Normalizer.FCD) is Normalizer.NO");
            pass = false;
        }
        
        out = iterativeNorm(new StringCharacterIterator(field[0]), Normalizer.FCD, buf, +1,options);
        out = iterativeNorm(new StringCharacterIterator(field[0]), Normalizer.FCD, buf, -1,options);
        
        out = iterativeNorm(new StringCharacterIterator(field[2]), Normalizer.FCD, buf, +1,options);
        out = iterativeNorm(new StringCharacterIterator(field[2]), Normalizer.FCD, buf, -1,options);
        
        out = iterativeNorm(new StringCharacterIterator(field[4]), Normalizer.FCD, buf, +1,options);
        out = iterativeNorm(new StringCharacterIterator(field[4]), Normalizer.FCD, buf, -1,options);
        
        out=Normalizer.normalize(fcd, Normalizer.NFD);
        if(!out.equals(field[2])) {
            errln("Normalizer error: NFD(FCD(s))!=NFD(s)");
            pass = false;
        }    
        if (!pass) {
            errln("FAIL: " + line);
        }     
        if(field[0]!=field[2]) {
            // two strings that are canonically equivalent must test
            // equal under a canonical caseless match
            // see UAX #21 Case Mappings and Jitterbug 2021 and
            // Unicode Technical Committee meeting consensus 92-C31
            int rc;
            if((rc = Normalizer.compare(field[0], field[2], (options<<Normalizer.COMPARE_NORM_OPTIONS_SHIFT)|Normalizer.COMPARE_IGNORE_CASE))!=0){
               errln("Normalizer.compare(original, NFD, case-insensitive) returned "+rc+" instead of 0 for equal");
               pass=false;
            }
        }
        
        return pass;
    }
    // two strings that are canonically equivalent must test
    // equal under a canonical caseless match
    // see UAX #21 Case Mappings and Jitterbug 2021 and
    // Unicode Technical Committee meeting consensus 92-C31
    private void compare(String s1, String s2){
        if(s1.length()==1 && s2.length()==1){
            if(Normalizer.compare(UTF16.charAt(s1,0),UTF16.charAt(s2,0),Normalizer.COMPARE_IGNORE_CASE)!=0){
                errln("Normalizer.compare(int,int) failed for s1: "
                        +Utility.hex(s1) + " s2: " + Utility.hex(s2));
            }    
        }
        if(s1.length()==1 && s2.length()>1){
            if(Normalizer.compare(UTF16.charAt(s1,0),s2,Normalizer.COMPARE_IGNORE_CASE)!=0){
                errln("Normalizer.compare(int,String) failed for s1: "
                        +Utility.hex(s1) + " s2: " + Utility.hex(s2));
            }    
        }
        if(s1.length()>1 && s2.length()>1){
            // TODO: Re-enable this tests after UTC fixes UAX 21
            if(Normalizer.compare(s1.toCharArray(),s2.toCharArray(),Normalizer.COMPARE_IGNORE_CASE)!=0){
                errln("Normalizer.compare(char[],char[]) failed for s1: "
                        +Utility.hex(s1) + " s2: " + Utility.hex(s2));
            }    
        }    
    }
    private void cross(String s1, String s2,Normalizer.Mode mode){
        String result = Normalizer.normalize(s1,mode);
        if(!result.equals(s2)){
            errln("cross test failed s1: " + Utility.hex(s1) + " s2: " 
                        +Utility.hex(s2));
        }
    }
    /**
     * Do a normalization using the iterative API in the given direction.
     * @param buf scratch buffer
     * @param dir either +1 or -1
     */
    private String iterativeNorm(String str, Normalizer.Mode mode,
                                 StringBuffer buf, int dir ,int options) throws Exception{
        normalizer.setText(str);
        normalizer.setMode(mode);
        buf.setLength(0);
        normalizer.setOption(-1, false);      // reset all options
        normalizer.setOption(options, true);  // set desired options

        int ch;
        if (dir > 0) {
            for (ch = normalizer.first(); ch != Normalizer.DONE;
                 ch = normalizer.next()) {
                buf.append(UTF16.valueOf(ch));
            }
        } else {
            for (ch = normalizer.last(); ch != Normalizer.DONE;
                 ch = normalizer.previous()) {
                buf.insert(0, UTF16.valueOf(ch));
            }
        }
        return buf.toString();
    }
    
    /**
     * Do a normalization using the iterative API in the given direction.
     * @param str a Java StringCharacterIterator
     * @param buf scratch buffer
     * @param dir either +1 or -1
     */
    private String iterativeNorm(StringCharacterIterator str, Normalizer.Mode mode,
                                 StringBuffer buf, int dir,int options) throws Exception{
        normalizer.setText(str);
        normalizer.setMode(mode);
        buf.setLength(0);
        normalizer.setOption(-1, false);      // reset all options
        normalizer.setOption(options, true);  // set desired options

        int ch;
        if (dir > 0) {
            for (ch = normalizer.first(); ch != Normalizer.DONE;
                 ch = normalizer.next()) {
                buf.append(UTF16.valueOf(ch));
            }
        } else {
            for (ch = normalizer.last(); ch != Normalizer.DONE;
                 ch = normalizer.previous()) {
                buf.insert(0, UTF16.valueOf(ch));
            }
        }
        return buf.toString();
    }

    /**
     * @param op name of normalization form, e.g., "KC"
     * @param s string being normalized
     * @param got value received
     * @param exp expected value
     * @param msg description of this test
     * @returns true if got == exp
     */
    private boolean assertEqual(String op, String s, String got,
                                String exp, String msg) {
        if (exp.equals(got)) {
            return true;
        }
        errln(("      " + msg + ") " + op + "(" + s + ")=" + hex(got) +
                             ", exp. " + hex(exp)));
        return false;
    }

    /**
     * Split a string into pieces based on the given delimiter
     * character.  Then, parse the resultant fields from hex into
     * characters.  That is, "0040 0400;0C00;0899" -> new String[] {
     * "\u0040\u0400", "\u0C00", "\u0899" }.  The output is assumed to
     * be of the proper length already, and exactly output.length
     * fields are parsed.  If there are too few an exception is
     * thrown.  If there are too many the extras are ignored.
     *
     * @param buf scratch buffer
     */
    private static void hexsplit(String s, char delimiter,
                                 String[] output, StringBuffer buf) {
        int i;
        int pos = 0;
        for (i=0; i<output.length; ++i) {
            int delim = s.indexOf(delimiter, pos);
            if (delim < 0) {
                throw new IllegalArgumentException("Missing field in " + s);
            }
            // Our field is from pos..delim-1.
            buf.setLength(0);
            
            String toHex = s.substring(pos,delim);
            pos = delim;
            int index = 0;
            int len = toHex.length();
            while(index< len){
                if(toHex.charAt(index)==' '){
                    index++;
                }else{
                    int spacePos = toHex.indexOf(' ', index);
                    if(spacePos==-1){
                        appendInt(buf,toHex.substring(index,len),s);
                        spacePos = len;
                    }else{
                        appendInt(buf,toHex.substring(index, spacePos),s);
                    }
                    index = spacePos+1;
                }
            }
            
            if (buf.length() < 1) {
                throw new IllegalArgumentException("Empty field " + i + " in " + s);
            }
            output[i] = buf.toString();
            ++pos; // Skip over delim
        }
    }
    public static void appendInt(StringBuffer buf, String strToHex, String s){
        int hex = Integer.parseInt(strToHex,16);
        if (hex < 0 ) {
            throw new IllegalArgumentException("Out of range hex " +
                                                hex + " in " + s);
        }else if (hex > 0xFFFF){
            buf.append((char)((hex>>10)+0xd7c0)); 
            buf.append((char)((hex&0x3ff)|0xdc00));
        }else{
            buf.append((char) hex);
        }
    }
            
    // Specific tests for debugging.  These are generally failures
    // taken from the conformance file, but culled out to make
    // debugging easier.  These can be eliminated without affecting
    // coverage.
    @Ignore
    @Test
    public void _hideTestCase6(/*int options*/) throws Exception{
        _testOneLine("0385;0385;00A8 0301;0020 0308 0301;0020 0308 0301;", /*options*/ 0);
    }

    private void _testOneLine(String line,int options) throws Exception{
        String[] fields = new String[5];
        StringBuffer buf = new StringBuffer();
        // Parse out the fields
        hexsplit(line, ';', fields, buf);
        checkConformance(fields, line,options);
    }
    

}
