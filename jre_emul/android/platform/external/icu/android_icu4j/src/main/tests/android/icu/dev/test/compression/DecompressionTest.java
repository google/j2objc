/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.compression;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.UnicodeDecompressor;

public class DecompressionTest extends TestFmwk {
    /** Print out a segment of a character array, if in verbose mode */
    private void log(char [] chars, int start, int count) {
        log("|");
        for(int i = start; i < start + count; ++i) {
            log(String.valueOf(chars[i]));
        }
        log("|");
    }

    /** Print out a segment of a character array, followed by a newline */
    private void logln(char [] chars, int start, int count)
    {
        log(chars, start, count);
        logln("");
    }

    /** Decompress the two segments */
    private String decompressTest(byte [] segment1, byte [] segment2) {
        StringBuffer s = new StringBuffer();
        UnicodeDecompressor myDecompressor = new UnicodeDecompressor();

        int [] bytesRead = new int[1];
        char [] charBuffer = new char [2*(segment1.length + segment2.length)];
        int count1 = 0, count2 = 0;

        count1 = myDecompressor.decompress(segment1, 0, segment1.length,
                                           bytesRead,
                                           charBuffer, 0, charBuffer.length);
        
        logln("Segment 1 (" + segment1.length + " bytes) " +
                "decompressed into " + count1  + " chars");
        logln("Bytes consumed: " + bytesRead[0]);

        logln("Got chars: ");
        logln(charBuffer, 0, count1);
        s.append(charBuffer, 0, count1);

        count2 = myDecompressor.decompress(segment2, 0, segment2.length,
                                           bytesRead,
                                           charBuffer, count1, 
                                           charBuffer.length);
        
        logln("Segment 2 (" + segment2.length + " bytes) " +
                "decompressed into " + count2  + " chars");
        logln("Bytes consumed: " + bytesRead[0]);

        logln("Got chars: ");
        logln(charBuffer, count1, count2);
        
        s.append(charBuffer, count1, count2);

        logln("Result: ");
        logln(charBuffer, 0, count1 + count2);
        logln("====================");

        return s.toString();
    }


    @Test
    public void TestDecompression() throws Exception {
        String result;

        // compressed segment breaking on a define window sequence
        /*                   B     o     o     t     h     SD1  */
        byte [] segment1 = { 0x42, 0x6f, 0x6f, 0x74, 0x68, 0x19 };

        // continuation
        /*                   IDX   ,           S     .          */
        byte [] segment2 = { 0x01, 0x2c, 0x20, 0x53, 0x2e };
        
        result = decompressTest(segment1, segment2);
        if(! result.equals("Booth, S.")) {
            errln("Decompression test failed");
            return;
        }

        // compressed segment breaking on a quote unicode sequence
        /*                   B     o     o     t     SQU        */
        byte [] segment3 = { 0x42, 0x6f, 0x6f, 0x74, 0x0e, 0x00 };

        // continuation
        /*                   h     ,           S     .          */
        byte [] segment4 = { 0x68, 0x2c, 0x20, 0x53, 0x2e };

        result = decompressTest(segment3, segment4);
        if(! result.equals("Booth, S.")) {
            errln("Decompression test failed");
            return;
        }


        // compressed segment breaking on a quote unicode sequence
        /*                   SCU   UQU                         */
        byte [] segment5 = { 0x0f, (byte)0xf0, 0x00 };

        // continuation
        /*                   B                                 */
        byte [] segment6 = { 0x42 };

        result = decompressTest(segment5, segment6);
        if(! result.equals("B")) {
            errln("Decompression test failed");
            return;
        }
    }
    
    /* Testing the method
     *      public int decompress(*** 
     */
    @Test
    public void TestDecompress(){
        char[] charBufferBlank = {};
        char[] charBuffer1 = {'a'};
        char[] charValid = {'d','u','m','m','y'};
        
        // Test when "if(charBuffer.length < 2 || (charBufferLimit - charBufferStart) < 2)" is true
        //      The following tests when "charBuffer.length < 2"
        UnicodeDecompressor ud = new UnicodeDecompressor();
        try{
            ud.decompress(null, 0, 0, null, null, 4, 0);
            errln("UnicodeDecompressor.decompress was suppose to return an exception.");
        } catch(Exception e){}
        
        try{
            ud.decompress(null, 0, 0, null, charBufferBlank, 4, 0);
            errln("UnicodeDecompressor.decompress was suppose to return an exception.");
        } catch(Exception e){}
        
        try{
            ud.decompress(null, 0, 0, null, charBuffer1, 4, 0);
            errln("UnicodeDecompressor.decompress was suppose to return an exception.");
        } catch(Exception e){}
        
        //      The following tests when "(charBufferLimit - charBufferStart) < 2"
        try{
            ud.decompress(null, 0, 0, null, charValid, 0, 0);
            errln("UnicodeDecompressor.decompress was suppose to return an exception.");
        } catch(Exception e){}
        
        try{
            ud.decompress(null, 0, 0, null, charValid, 1, 0);
            errln("UnicodeDecompressor.decompress was suppose to return an exception.");
        } catch(Exception e){}
        
        try{
            ud.decompress(null, 0, 0, null, charValid, 1, 1);
            errln("UnicodeDecompressor.decompress was suppose to return an exception.");
        } catch(Exception e){}
        
        try{
            ud.decompress(null, 0, 0, null, charValid, 0, 1);
            errln("UnicodeDecompressor.decompress was suppose to return an exception.");
        } catch(Exception e){}
        
        try{
            ud = new UnicodeDecompressor();
            byte[] b = {
                    (byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, 
                    (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x89,
                    (byte) 0x8A, (byte) 0x8B, (byte) 0x8C, (byte) 0x8D, (byte) 0x8E,
                    (byte) 0x8F, (byte) 0x90, (byte) 0x91, (byte) 0x92, (byte) 0x93,
                    (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97, (byte) 0x98,
                    (byte) 0x99, (byte) 0x9A, (byte) 0x9B, (byte) 0x9C, (byte) 0x9D,
                    (byte) 0x9E, (byte) 0x9F, (byte) 0xA0, (byte) 0xA1, (byte) 0xA2,
                    (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0xA6, (byte) 0xA7,
                    (byte) 0xA8, (byte) 0xA9, (byte) 0xAA, (byte) 0xAB, (byte) 0xAC,
                    (byte) 0xAD, (byte) 0xAE, (byte) 0xAF, (byte) 0xB0, (byte) 0xB1,
                    (byte) 0xB2, (byte) 0xB3, (byte) 0xB4, (byte) 0xB5, (byte) 0xB6,
                    (byte) 0xB7, (byte) 0xB8, (byte) 0xB9, (byte) 0xBA, (byte) 0xBB,
                    (byte) 0xBC, (byte) 0xBD, (byte) 0xBE, (byte) 0xBF, (byte) 0xC0,
                    (byte) 0xC1, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4, (byte) 0xC5,
                    (byte) 0xC6, (byte) 0xC7, (byte) 0xC8, (byte) 0xC9, (byte) 0xCA,
                    (byte) 0xCB, (byte) 0xCC, (byte) 0xCD, (byte) 0xCE, (byte) 0xCF,
                    (byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3, (byte) 0xD4,
                    (byte) 0xD5, (byte) 0xD6, (byte) 0xD7, (byte) 0xD8, (byte) 0xD9,
                    (byte) 0xDA, (byte) 0xDB, (byte) 0xDC, (byte) 0xDD, (byte) 0xDE,
                    (byte) 0xDF, (byte) 0xE0, (byte) 0xE1, (byte) 0xE2, (byte) 0xE3,
                    (byte) 0xE4, (byte) 0xE5, (byte) 0xE6, (byte) 0xE7, (byte) 0xE8,
                    (byte) 0xE9, (byte) 0xEA, (byte) 0xEB, (byte) 0xEC, (byte) 0xED,
                    (byte) 0xEE, (byte) 0xEF, (byte) 0xF0, (byte) 0xF1, (byte) 0xF2,
                    (byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6, (byte) 0xF7,
                    (byte) 0xF8, (byte) 0xF9, (byte) 0xFA, (byte) 0xFB, (byte) 0xFC,
                    (byte) 0xFD, (byte) 0xFE, (byte) 0xFF,
                    (byte) 0x00, (byte) 0x09, (byte) 0x0A, (byte) 0x0D,
                    (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24,
                    (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29,
                    (byte) 0x2A, (byte) 0x2B, (byte) 0x2C, (byte) 0x2D, (byte) 0x2E,
                    (byte) 0x2F, (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33,
                    (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38,
                    (byte) 0x39, (byte) 0x3A, (byte) 0x3B, (byte) 0x3C, (byte) 0x3D,
                    (byte) 0x3E, (byte) 0x3F, (byte) 0x40, (byte) 0x41, (byte) 0x42,
                    (byte) 0x43, (byte) 0x44, (byte) 0x45, (byte) 0x46, (byte) 0x47,
                    (byte) 0x48, (byte) 0x49, (byte) 0x4A, (byte) 0x4B, (byte) 0x4C,
                    (byte) 0x4D, (byte) 0x4E, (byte) 0x4F, (byte) 0x50, (byte) 0x51,
                    (byte) 0x52, (byte) 0x53, (byte) 0x54, (byte) 0x55, (byte) 0x56,
                    (byte) 0x57, (byte) 0x58, (byte) 0x59, (byte) 0x5A, (byte) 0x5B,
                    (byte) 0x5C, (byte) 0x5D, (byte) 0x5E, (byte) 0x5F, (byte) 0x60,
                    (byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x64, (byte) 0x65,
                    (byte) 0x66, (byte) 0x67, (byte) 0x68, (byte) 0x69, (byte) 0x6A,
                    (byte) 0x6B, (byte) 0x6C, (byte) 0x6D, (byte) 0x6E, (byte) 0x6F,
                    (byte) 0x70, (byte) 0x71, (byte) 0x72, (byte) 0x73, (byte) 0x74,
                    (byte) 0x75, (byte) 0x76, (byte) 0x77, (byte) 0x78, (byte) 0x79,
                    (byte) 0x7A, (byte) 0x7B, (byte) 0x7C, (byte) 0x7D, (byte) 0x7E,
                    (byte) 0x7F,
                    (byte) UnicodeDecompressor.SQUOTEU,
                    (byte) UnicodeDecompressor.SCHANGEU, 
                    (byte) UnicodeDecompressor.SQUOTE0, (byte) UnicodeDecompressor.SQUOTE1, (byte) UnicodeDecompressor.SQUOTE2, (byte) UnicodeDecompressor.SQUOTE3,
                    (byte) UnicodeDecompressor.SQUOTE4, (byte) UnicodeDecompressor.SQUOTE5, (byte) UnicodeDecompressor.SQUOTE6, (byte) UnicodeDecompressor.SQUOTE7,
                    (byte) UnicodeDecompressor.SCHANGE0, (byte) UnicodeDecompressor.SCHANGE1, (byte) UnicodeDecompressor.SCHANGE2, (byte) UnicodeDecompressor.SCHANGE3,
                    (byte) UnicodeDecompressor.SCHANGE4, (byte) UnicodeDecompressor.SCHANGE5, (byte) UnicodeDecompressor.SCHANGE6, (byte) UnicodeDecompressor.SCHANGE7,
                    (byte) UnicodeDecompressor.SDEFINE0, (byte) UnicodeDecompressor.SDEFINE1, (byte) UnicodeDecompressor.SDEFINE2, (byte) UnicodeDecompressor.SDEFINE3,
                    (byte) UnicodeDecompressor.SDEFINE4, (byte) UnicodeDecompressor.SDEFINE5, (byte) UnicodeDecompressor.SDEFINE6, (byte) UnicodeDecompressor.SDEFINE7,
                    (byte) UnicodeDecompressor.SDEFINEX, (byte) UnicodeDecompressor.SRESERVED,
                    };
            char[] c = new char[b.length];
            ud.decompress(b, 0, b.length, null, c, 0, c.length);
        } catch(Exception e){
            errln("UnicodeDecompressor.decompress() was not suppose to return an exception.");
        }
    }

}
