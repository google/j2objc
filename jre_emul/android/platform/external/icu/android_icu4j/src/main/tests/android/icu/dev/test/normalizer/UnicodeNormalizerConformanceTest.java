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

import org.junit.Ignore;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;


public class UnicodeNormalizerConformanceTest extends TestFmwk {

    UnicodeNormalizer normalizer_C, normalizer_D, normalizer_KC, normalizer_KD;

    public UnicodeNormalizerConformanceTest() {
        // Doesn't matter what the string and mode are; we'll change
        // them later as needed.
        normalizer_C = new UnicodeNormalizer(UnicodeNormalizer.C, true);
        normalizer_D = new UnicodeNormalizer(UnicodeNormalizer.D, false);
        normalizer_KC = new UnicodeNormalizer(UnicodeNormalizer.KC, false);
        normalizer_KD = new UnicodeNormalizer(UnicodeNormalizer.KD, false);
        
    }
    // more interesting conformance test cases, not in the unicode.org NormalizationTest.txt
    static  String[] moreCases ={
        // Markus 2001aug30
        "0061 0332 0308;00E4 0332;0061 0332 0308;00E4 0332;0061 0332 0308; # Markus 0",
    
        // Markus 2001oct26 - test edge case for iteration: U+0f73.cc==0 but decomposition.lead.cc==129
        "0061 0301 0F73;00E1 0F71 0F72;0061 0F71 0F72 0301;00E1 0F71 0F72;0061 0F71 0F72 0301; # Markus 1"
    };

    /**
     * Test the conformance of NewNormalizer to
     * http://www.unicode.org/unicode/reports/tr15/conformance/Draft-TestSuite.txt.
     * This file must be located at the path specified as TEST_SUITE_FILE.
     */
    @Test
    public void TestConformance() throws Exception{
        String line = null;
        String[] fields = new String[5];
        StringBuffer buf = new StringBuffer();
        int passCount = 0;
        int failCount = 0;
        UnicodeSet other = new UnicodeSet(0, 0x10ffff);
        int c=0;
        BufferedReader input = null;
        try {
            input = TestUtil.getDataReader("unicode/NormalizationTest.txt");
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
                if (checkConformance(fields, line)) {
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
                } catch (Exception ignored) {
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
    private boolean checkConformance(String[] field, String line) throws Exception{
        boolean pass = true;
       // StringBuffer buf = new StringBuffer(); // scratch
        String out;
        int i=0;
        for (i=0; i<5; ++i) {
            if (i<3) {
                out = normalizer_C.normalize(field[i]);
                pass &= assertEqual("C", field[i], out, field[1], "c2!=C(c" + (i+1));
                 
                out = normalizer_D.normalize(field[i]);
                pass &= assertEqual("D", field[i], out, field[2], "c3!=D(c" + (i+1));
                
            }
            out = normalizer_KC.normalize(field[i]);
            pass &= assertEqual("KC", field[i], out, field[3], "c4!=KC(c" + (i+1));

            out = normalizer_KD.normalize(field[i]);
            pass &= assertEqual("KD", field[i], out, field[4], "c5!=KD(c" + (i+1));
              
        }
        
        if (!pass) {
            errln("FAIL: " + line);
        }     
       
        return pass;
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
    public void _hideTestCase6() throws Exception{
        _testOneLine("0385;0385;00A8 0301;0020 0308 0301;0020 0308 0301;");
    }

    private void _testOneLine(String line) throws Exception{
        String[] fields = new String[5];
        StringBuffer buf = new StringBuffer();
        // Parse out the fields
        hexsplit(line, ';', fields, buf);
        checkConformance(fields, line);
    }
    

}
