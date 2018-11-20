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
import android.icu.text.UnicodeCompressor;
import android.icu.text.UnicodeDecompressor;

public class ExhaustiveTest extends TestFmwk {
    /** Test simple compress/decompress API, returning # of errors */
    @Test
    public void testSimple() throws Exception {
        for(int i = 0; i < fTestCases.length; i++) {
            simpleTest(fTestCases[i]);
        }
    }
    private void simpleTest(String s) throws Exception {
        byte [] compressed = UnicodeCompressor.compress(s);
        String res = UnicodeDecompressor.decompress(compressed);
        if (logDiffs(s.toCharArray(), s.length(), 
                res.toCharArray(), res.length()) == false) {
            logln(s.length() + " chars ===> " 
                    + compressed.length + " bytes ===> " 
                    + res.length() + " chars");
        } else {
            logln("Compressed:");
            printBytes(compressed, compressed.length);
            errln("testSimple did not compress correctly");
        }
    }

    /** Test iterative compress/decompress API, returning # of errors */
    @Test
    public void testIterative() throws Exception {
        for(int i = 0; i < fTestCases.length; i++) {
            myTest(fTestCases[i].toCharArray(), fTestCases[i].length());
        }
    }
    private void myTest(char[] chars, int len) {
        UnicodeCompressor myCompressor = new UnicodeCompressor();
        UnicodeDecompressor myDecompressor = new UnicodeDecompressor();
        
        // variables for my compressor
        int myByteCount = 0;
        int myCharCount = 0;
        int myCompressedSize = Math.max(512, 3*len);
        byte[] myCompressed = new byte[myCompressedSize];
        int myDecompressedSize = Math.max(2, 2 * len);
        char[] myDecompressed = new char[myDecompressedSize];
        int[] unicharsRead = new int[1];
        int[] bytesRead = new int[1];
        
        myByteCount = myCompressor.compress(chars, 0, len, unicharsRead,
                myCompressed, 0, myCompressedSize);

        myCharCount = myDecompressor.decompress(myCompressed, 0, myByteCount,
                bytesRead, myDecompressed, 0, myDecompressedSize);

        if (logDiffs(chars, len, myDecompressed, myCharCount) == false) {
            logln(len + " chars ===> " 
                    + myByteCount + " bytes ===> " 
                    + myCharCount + " chars");
        } else {
            logln("Compressed:");
            printBytes(myCompressed, myByteCount);
            errln("Iterative test failed");
        }
    }

    /** Test iterative compress/decompress API */
    @Test
    public void testMultipass() throws Exception {
        for(int i = 0; i < fTestCases.length; i++) {
            myMultipassTest(fTestCases[i].toCharArray(), fTestCases[i].length());
        }
    }
    private void myMultipassTest(char [] chars, int len) throws Exception {
        UnicodeCompressor myCompressor = new UnicodeCompressor();
        UnicodeDecompressor myDecompressor = new UnicodeDecompressor();
        
        // variables for my compressor
        
        // for looping
        int byteBufferSize = 4;//Math.max(4, len / 4);
        byte[] byteBuffer = new byte [byteBufferSize];
        // real target
        int compressedSize = Math.max(512, 3 * len);
        byte[] compressed = new byte[compressedSize];

        // for looping
        int unicharBufferSize = 2;//byteBufferSize;
        char[] unicharBuffer = new char[unicharBufferSize];
        // real target
        int decompressedSize = Math.max(2, 2 * len);
        char[] decompressed = new char[decompressedSize];

        int bytesWritten = 0;
        int unicharsWritten = 0;

        int[] unicharsRead = new int[1];
        int[] bytesRead = new int[1];
        
        int totalCharsCompressed = 0;
        int totalBytesWritten = 0;

        int totalBytesDecompressed  = 0;
        int totalCharsWritten = 0;

        // not used boolean err = false;


        // perform the compression in a loop
        do {
            
            // do the compression
            bytesWritten = myCompressor.compress(chars, totalCharsCompressed, 
                   len, unicharsRead, byteBuffer, 0, byteBufferSize);

            // copy the current set of bytes into the target buffer
            System.arraycopy(byteBuffer, 0, compressed, 
                   totalBytesWritten, bytesWritten);
            
            // update the no. of characters compressed
            totalCharsCompressed += unicharsRead[0];
            
            // update the no. of bytes written
            totalBytesWritten += bytesWritten;
            
            /*System.out.logln("Compression pass complete.  Compressed "
                               + unicharsRead[0] + " chars into "
                               + bytesWritten + " bytes.");*/
        } while(totalCharsCompressed < len);

        if (totalCharsCompressed != len) {
            errln("ERROR: Number of characters compressed("
                    + totalCharsCompressed + ") != len(" + len + ")");
        } else {
            logln("MP: " + len + " chars ===> " + totalBytesWritten + " bytes.");
        }
        
        // perform the decompression in a loop
        do {
            
            // do the decompression
            unicharsWritten = myDecompressor.decompress(compressed, 
                    totalBytesDecompressed, totalBytesWritten, 
                    bytesRead, unicharBuffer, 0, unicharBufferSize);

            // copy the current set of chars into the target buffer
            System.arraycopy(unicharBuffer, 0, decompressed, 
                    totalCharsWritten, unicharsWritten);
            
            // update the no. of bytes decompressed
            totalBytesDecompressed += bytesRead[0];
            
            // update the no. of chars written
            totalCharsWritten += unicharsWritten;
            
            /*System.out.logln("Decompression pass complete.  Decompressed "
                               + bytesRead[0] + " bytes into "
                               + unicharsWritten + " chars.");*/
        } while (totalBytesDecompressed < totalBytesWritten);

        if (totalBytesDecompressed != totalBytesWritten) {
            errln("ERROR: Number of bytes decompressed(" 
                    + totalBytesDecompressed 
                    + ") != totalBytesWritten(" 
                    + totalBytesWritten + ")");
        } else {
            logln("MP: " + totalBytesWritten
                    + " bytes ===> " + totalCharsWritten + " chars.");
        }
        
        if (logDiffs(chars, len, decompressed, totalCharsWritten)) {
            errln("ERROR: buffer contents incorrect");
        }
    }

    /** Print differences between two character buffers */
    private boolean logDiffs(char[] s1, int s1len, char[] s2, int s2len) {
        boolean result  = false;
        
        if(s1len != s2len) {
            logln("====================");
            logln("Length doesn't match: expected " + s1len
                               + ", got " + s2len);
            logln("Expected:");
            printChars(s1, s1len);
            logln("Got:");
            printChars(s2, s2len);
            result = true;
        }
        
        int len = Math.min(s1len, s2len);
        for(int i = 0; i < len; ++i) {
            if(s1[i] != s2[i]) {
                if(result == false) {
                    logln("====================");
                }
                logln("First difference at char " + i);
                logln("Exp. char: " + Integer.toHexString(s1[i]));
                logln("Got char : " + Integer.toHexString(s2[i]));
                logln("Expected:");
                printChars(s1, s1len);
                logln("Got:");
                printChars(s2, s2len);
                result = true;
                break;
            }
        }
    
        return result;
    }

    // generate a string of characters, with simulated runs of characters
    /*private static char[] randomChars(int len, Random random) {
        char[] result = new char [len];
        int runLen = 0;
        int used = 0;
        
        while(used < len) {
            runLen = (int) (30 * random.nextDouble());
            if(used + runLen >= len) {
                runLen = len - used;
            }
            randomRun(result, used, runLen, random);
            used += runLen;
        }
    
        return result;
    }*/

    // generate a run of characters in a "window"
    /*private static void randomRun(char[] target, int pos, int len, Random random) {
        int offset = (int) (0xFFFF * random.nextDouble());

        // don't overflow 16 bits
        if(offset > 0xFF80) {
            offset = 0xFF80;
        }

        for(int i = pos; i < pos + len; i++) {
            target[i] = (char)(offset + (0x7F * random.nextDouble()));
        }
    }*/

    private static final String [] fTestCases = {
        "Hello \u9292 \u9192 World!",
        "Hell\u0429o \u9292 \u9192 W\u0084rld!",
        "Hell\u0429o \u9292 \u9292W\u0084rld!",

        "\u0648\u06c8", // catch missing reset
        "\u0648\u06c8",

        "\u4444\uE001", // lowest quotable
        "\u4444\uf2FF", // highest quotable
        "\u4444\uf188\u4444",
        "\u4444\uf188\uf288",
        "\u4444\uf188abc\0429\uf288",
        "\u9292\u2222",
        "Hell\u0429\u04230o \u9292 \u9292W\u0084\u0192rld!",
        "Hell\u0429o \u9292 \u9292W\u0084rld!",
        "Hello World!123456",
        "Hello W\u0081\u011f\u0082!", // Latin 1 run

        "abc\u0301\u0302",  // uses SQn for u301 u302
        "abc\u4411d",      // uses SQU
        "abc\u4411\u4412d",// uses SCU
        "abc\u0401\u0402\u047f\u00a5\u0405", // uses SQn for ua5
        "\u9191\u9191\u3041\u9191\u3041\u3041\u3000", // SJIS like data
        "\u9292\u2222",
        "\u9191\u9191\u3041\u9191\u3041\u3041\u3000",
        "\u9999\u3051\u300c\u9999\u9999\u3060\u9999\u3065\u3065\u3065\u300c",
        "\u3000\u266a\u30ea\u30f3\u30b4\u53ef\u611b\u3044\u3084\u53ef\u611b\u3044\u3084\u30ea\u30f3\u30b4\u3002",

        "", // empty input
        "\u0000", // smallest BMP character
        "\uFFFF", // largest BMP character

        "\ud800\udc00", // smallest surrogate
        "\ud8ff\udcff", // largest surrogate pair
        
        // regression tests
        "\u6441\ub413\ua733\uf8fe\ueedb\u587f\u195f\u4899\uf23d\u49fd\u0aac\u5792\ufc22\ufc3c\ufc46\u00aa",
        "\u30f9\u8321\u05e5\u181c\ud72b\u2019\u99c9\u2f2f\uc10c\u82e1\u2c4d\u1ebc\u6013\u66dc\ubbde\u94a5\u4726\u74af\u3083\u55b9\u000c",
        "\u0041\u00df\u0401\u015f",
        "\u9066\u2123abc",
        "\ud266\u43d7\\\ue386\uc9c0\u4a6b\u9222\u901f\u7410\ua63f\u539b\u9596\u482e\u9d47\ucfe4\u7b71\uc280\uf26a\u982f\u862a\u4edd\uf513\ufda6\u869d\u2ee0\ua216\u3ff6\u3c70\u89c0\u9576\ud5ec\ubfda\u6cca\u5bb3\ubcea\u554c\u914e\ufa4a\uede3\u2990\ud2f5\u2729\u5141\u0f26\uccd8\u5413\ud196\ubbe2\u51b9\u9b48\u0dc8\u2195\u21a2\u21e9\u00e4\u9d92\u0bc0\u06c5",
        "\uf95b\u2458\u2468\u0e20\uf51b\ue36e\ubfc1\u0080\u02dd\uf1b5\u0cf3\u6059\u7489"

    };

    //==========================
    // Compression modes
    //==========================
    private final static int SINGLEBYTEMODE                 = 0;
    private final static int UNICODEMODE                    = 1;
    
    //==========================
    // Single-byte mode tags
    //==========================
    private final static int SDEFINEX                   = 0x0B;
    //private final static int SRESERVED                  = 0x0C;             // this is a reserved value
    private final static int SQUOTEU                    = 0x0E;
    private final static int SSWITCHU                   = 0x0F;

    private final static int SQUOTE0                        = 0x01;
    private final static int SQUOTE1                        = 0x02;
    private final static int SQUOTE2                        = 0x03;
    private final static int SQUOTE3                        = 0x04;
    private final static int SQUOTE4                        = 0x05;
    private final static int SQUOTE5                        = 0x06;
    private final static int SQUOTE6                        = 0x07;
    private final static int SQUOTE7                        = 0x08;

    private final static int SSWITCH0                       = 0x10;
    private final static int SSWITCH1                       = 0x11;
    private final static int SSWITCH2                       = 0x12;
    private final static int SSWITCH3                       = 0x13;
    private final static int SSWITCH4                       = 0x14;
    private final static int SSWITCH5                       = 0x15;
    private final static int SSWITCH6                       = 0x16;
    private final static int SSWITCH7                       = 0x17;

    private final static int SDEFINE0                       = 0x18;
    private final static int SDEFINE1                       = 0x19;
    private final static int SDEFINE2                       = 0x1A;
    private final static int SDEFINE3                       = 0x1B;
    private final static int SDEFINE4                       = 0x1C;
    private final static int SDEFINE5                       = 0x1D;
    private final static int SDEFINE6                       = 0x1E;
    private final static int SDEFINE7                       = 0x1F;

    //==========================
    // Unicode mode tags
    //==========================
    private final static int USWITCH0                       = 0xE0;
    private final static int USWITCH1                       = 0xE1;
    private final static int USWITCH2                       = 0xE2;
    private final static int USWITCH3                       = 0xE3;
    private final static int USWITCH4                       = 0xE4;
    private final static int USWITCH5                       = 0xE5;
    private final static int USWITCH6                       = 0xE6;
    private final static int USWITCH7                       = 0xE7;

    private final static int UDEFINE0                       = 0xE8;
    private final static int UDEFINE1                       = 0xE9;
    private final static int UDEFINE2                       = 0xEA;
    private final static int UDEFINE3                       = 0xEB;
    private final static int UDEFINE4                       = 0xEC;
    private final static int UDEFINE5                       = 0xED;
    private final static int UDEFINE6                       = 0xEE;
    private final static int UDEFINE7                       = 0xEF;

    private final static int UQUOTEU                        = 0xF0;
    private final static int UDEFINEX                       = 0xF1;
    //private final static int URESERVED                      = 0xF2;         // this is a reserved value

    /* Print out an array of characters, with non-printables (for me) 
       displayed as hex values */
    private void printChars(char[] chars, int len) {
        for(int i = 0; i < len; i++) {
            int c = (int)chars[i];
            if(c < 0x0020 || c >= 0x7f) {
                log("[0x");
                log(Integer.toHexString(c));
                log("]");
            } else {
                log(String.valueOf((char)c));
            }
        }
        logln("");
    }

    private void printBytes(byte[] byteBuffer, int len) {
        int curByteIndex = 0;
        int byteBufferLimit = len;
        int mode = SINGLEBYTEMODE;
        int aByte = 0x00;
        
        if(len > byteBuffer.length) {
            logln("Warning: printBytes called with length too large. Truncating");
            byteBufferLimit = byteBuffer.length;
        }
        
        while(curByteIndex < byteBufferLimit) {
            switch(mode) {  
            case SINGLEBYTEMODE:
                while(curByteIndex < byteBufferLimit 
                      && mode == SINGLEBYTEMODE)  {
                    aByte = ((int)byteBuffer[curByteIndex++]) & 0xFF;
                    switch(aByte) {
                    default:
                        log(Integer.toHexString(((int) aByte) & 0xFF) + " ");
                        break;
                        // quote unicode
                    case SQUOTEU:
                        log("SQUOTEU ");
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        break;
                        
                        // switch to Unicode mode
                    case SSWITCHU:
                        log("SSWITCHU ");
                        mode = UNICODEMODE;
                        break;
                        
                        // handle all quote tags
                    case SQUOTE0: case SQUOTE1: case SQUOTE2: case SQUOTE3:
                    case SQUOTE4: case SQUOTE5: case SQUOTE6: case SQUOTE7:
                        log("SQUOTE" + (aByte - SQUOTE0) + " ");
                        if(curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        break;
                        
                        // handle all switch tags
                    case SSWITCH0: case SSWITCH1: case SSWITCH2: case SSWITCH3:
                    case SSWITCH4: case SSWITCH5: case SSWITCH6: case SSWITCH7:
                        log("SSWITCH" + (aByte - SSWITCH0) + " ");
                        break;
                                        
                        // handle all define tags
                    case SDEFINE0: case SDEFINE1: case SDEFINE2: case SDEFINE3:
                    case SDEFINE4: case SDEFINE5: case SDEFINE6: case SDEFINE7:
                        log("SDEFINE" + (aByte - SDEFINE0) + " ");
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        break;
                        
                        // handle define extended tag
                    case SDEFINEX:
                        log("SDEFINEX ");
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        break;
                        
                    } // end switch
                } // end while
                break;
                
            case UNICODEMODE:
                while(curByteIndex < byteBufferLimit && mode == UNICODEMODE) {
                    aByte = ((int)byteBuffer[curByteIndex++]) & 0xFF;
                    switch(aByte) {
                        // handle all define tags
                    case UDEFINE0: case UDEFINE1: case UDEFINE2: case UDEFINE3:
                    case UDEFINE4: case UDEFINE5: case UDEFINE6: case UDEFINE7:
                        log("UDEFINE" + (aByte - UDEFINE0) + " ");
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        mode = SINGLEBYTEMODE;
                        break;
                        
                        // handle define extended tag
                    case UDEFINEX:
                        log("UDEFINEX ");
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        break;
                        
                        // handle all switch tags
                    case USWITCH0: case USWITCH1: case USWITCH2: case USWITCH3:
                    case USWITCH4: case USWITCH5: case USWITCH6: case USWITCH7:
                        log("USWITCH" + (aByte - USWITCH0) + " ");
                        mode = SINGLEBYTEMODE;
                        break;
                        
                        // quote unicode
                    case UQUOTEU:
                        log("UQUOTEU ");
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        break;
                        
                    default:
                        log(Integer.toHexString(((int) aByte) & 0xFF) + " ");
                        if (curByteIndex < byteBufferLimit) {
                            log(Integer.toHexString(((int) byteBuffer[curByteIndex++]) & 0xFF) + " ");
                        }
                        break;
                        
                    } // end switch
                } // end while
                break;
                
            } // end switch( mode )
        } // end while
        
        logln("");
    }    
}






