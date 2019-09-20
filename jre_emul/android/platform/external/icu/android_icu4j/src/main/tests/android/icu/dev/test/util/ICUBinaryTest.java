/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.dev.test.util;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.ICUBinary;

/**
* Testing class for Trie. Tests here will be simple, since both CharTrie and 
* IntTrie are very similar and are heavily used in other parts of ICU4J.
* Codes using Tries are expected to have detailed tests.
* @author Syn Wee Quek
* @since release 2.1 Jan 01 2002
*/
public final class ICUBinaryTest extends TestFmwk 
{ 
    // constructor ---------------------------------------------------
  
    /**
    * Constructor
    */
    public ICUBinaryTest()
    {
    }
      
    // public methods -----------------------------------------------
    
    /**
     * Testing the constructors of the Tries
     */
    @Test
    public void TestReadHeader()
    {
        int formatid = 0x01020304;
        byte array[] = {
            // header size
            0, 0x18,
            // magic numbers
            (byte)0xda, 0x27,
            // size
            0, 0x14,
            // reserved word
            0, 0,
            // bigendian
            1,
            // charset
            0,
            // charsize
            2,
            // reserved byte
            0,
            // data format id
            1, 2, 3, 4,
            // dataVersion
            1, 2, 3, 4,
            // unicodeVersion
            3, 2, 0, 0
        };
        ByteBuffer bytes = ByteBuffer.wrap(array);
        ICUBinary.Authenticate authenticate
                = new ICUBinary.Authenticate() {
                    public boolean isDataVersionAcceptable(byte version[])
                    {
                        return version[0] == 1;
                    }
                };
        // check full data version
        try {
            ICUBinary.readHeader(bytes, formatid, authenticate);
        } catch (IOException e) {
            errln("Failed: Lenient authenticate object should pass ICUBinary.readHeader");
        }
        // no restriction to the data version
        try {
            bytes.rewind();
            ICUBinary.readHeader(bytes, formatid, null);
        } catch (IOException e) {
            errln("Failed: Null authenticate object should pass ICUBinary.readHeader");
        }
        // lenient data version
        array[17] = 9;
        try {
            bytes.rewind();
            ICUBinary.readHeader(bytes, formatid, authenticate);
        } catch (IOException e) {
            errln("Failed: Lenient authenticate object should pass ICUBinary.readHeader");
        }
        // changing the version to an incorrect one, expecting failure
        array[16] = 2;
        try {
            bytes.rewind();
            ICUBinary.readHeader(bytes, formatid, authenticate);
            errln("Failed: Invalid version number should not pass authenticate object");
        } catch (IOException e) {
            logln("PASS: ICUBinary.readHeader with invalid version number failed as expected");
        }
    }
}
