/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.dev.test.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.ICUBinary;
import android.icu.impl.Trie2;
import android.icu.impl.Trie2Writable;
import android.icu.impl.Trie2_16;
import android.icu.impl.Trie2_32;

public class Trie2Test extends TestFmwk {
    /**
     * Constructor
     */
     public Trie2Test()
     {
     }
       
     // public methods -----------------------------------------------
     
     //
     //  TestAPI.  Check that all API methods can be called, and do at least some minimal
     //            operation correctly.  This is not a full test of correct behavior.
     //
    @Test
     public void TestTrie2API() {
         // Trie2.createFromSerialized()
         //   This function is well exercised by TestRanges().   
         
         // Trie2.getVersion(InputStream is, boolean anyEndianOk)
         //
         
         try {
             Trie2Writable trie = new Trie2Writable(0,0);
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             trie.toTrie2_16().serialize(os);
             ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
             assertEquals(null, 2, Trie2.getVersion(is, true));
         } catch (IOException e) {
             errln(where() + e.toString());            
         }
         
         // Equals & hashCode
         //
         {
             Trie2Writable trieWA = new Trie2Writable(0,0);
             Trie2Writable trieWB = new Trie2Writable(0,0);
             Trie2 trieA = trieWA;
             Trie2 trieB = trieWB;
             assertTrue("", trieA.equals(trieB));
             assertEquals("", trieA, trieB);
             assertEquals("", trieA.hashCode(), trieB.hashCode());
             trieWA.set(500, 2);
             assertNotEquals("", trieA, trieB);
             // Note that the hash codes do not strictly need to be different,
             //   but it's highly likely that something is wrong if they are the same.
             assertNotEquals("", trieA.hashCode(), trieB.hashCode());
             trieWB.set(500, 2);
             trieA = trieWA.toTrie2_16();
             assertEquals("", trieA, trieB);
             assertEquals("", trieA.hashCode(), trieB.hashCode());
         }
         
         // 
         // Iterator creation
         //
         {
             Trie2Writable trie = new Trie2Writable(17,0);
             Iterator<Trie2.Range>   it;
             it = trie.iterator();
             
             Trie2.Range r = it.next();
             assertEquals("", 0, r.startCodePoint);
             assertEquals("", 0x10ffff, r.endCodePoint);
             assertEquals("", 17, r.value);
             assertEquals("", false, r.leadSurrogate);
             
             r = it.next();
             assertEquals("", 0xd800, r.startCodePoint);
             assertEquals("", 0xdbff, r.endCodePoint);
             assertEquals("", 17, r.value);
             assertEquals("", true, r.leadSurrogate);
             
        
             int i = 0;
             for (Trie2.Range rr: trie) {
                 switch (i) {
                 case 0:
                     assertEquals("", 0, rr.startCodePoint);
                     assertEquals("", 0x10ffff, rr.endCodePoint);
                     assertEquals("", 17, rr.value);
                     assertEquals("", false, rr.leadSurrogate);
                     break;
                 case 1:
                     assertEquals("", 0xd800, rr.startCodePoint);
                     assertEquals("", 0xdbff, rr.endCodePoint);
                     assertEquals("", 17, rr.value);
                     assertEquals("", true, rr.leadSurrogate);
                     break;
                 default:
                     errln(where() + " Unexpected iteration result");
                 }
                 i++;
             }
         }
         
         // Iteration with a value mapping function
         //
         {
             Trie2Writable trie = new Trie2Writable(0xbadfeed, 0);
             trie.set(0x10123, 42);
             
             Trie2.ValueMapper vm = new Trie2.ValueMapper() {
                 public int map(int v) {
                     if (v == 0xbadfeed) {
                         v = 42;
                     }
                     return v;
                 }
             };
             Iterator<Trie2.Range> it = trie.iterator(vm);
             Trie2.Range r = it.next();
             assertEquals("", 0, r.startCodePoint);
             assertEquals("", 0x10ffff, r.endCodePoint);
             assertEquals("", 42, r.value);
             assertEquals("", false, r.leadSurrogate);
         }
         
         
         // Iteration over a leading surrogate range.
         //
         {
             Trie2Writable trie = new Trie2Writable(0xdefa17, 0);
             trie.set(0x2f810, 10);
             Iterator<Trie2.Range> it = trie.iteratorForLeadSurrogate((char)0xd87e);
             Trie2.Range r = it.next();
             assertEquals("", 0x2f800,  r.startCodePoint);
             assertEquals("", 0x2f80f,  r.endCodePoint);
             assertEquals("", 0xdefa17, r.value);
             assertEquals("", false,    r.leadSurrogate);
             
             r = it.next();
             assertEquals("", 0x2f810, r.startCodePoint);
             assertEquals("", 0x2f810, r.endCodePoint);
             assertEquals("", 10,      r.value);
             assertEquals("", false,   r.leadSurrogate);

             r = it.next();
             assertEquals("", 0x2f811,  r.startCodePoint);
             assertEquals("", 0x2fbff,  r.endCodePoint);
             assertEquals("", 0xdefa17, r.value);
             assertEquals("", false,    r.leadSurrogate);
             
             assertFalse("", it.hasNext());
         }
         
         // Iteration over a leading surrogate range with a ValueMapper.
         //
         {
             Trie2Writable trie = new Trie2Writable(0xdefa17, 0);
             trie.set(0x2f810, 10);
             Trie2.ValueMapper m = new Trie2.ValueMapper() {
                 public int map(int in) {
                     if (in==10) {
                         in = 0xdefa17;                         
                     }
                     return in;
                 }               
             };
             Iterator<Trie2.Range> it = trie.iteratorForLeadSurrogate((char)0xd87e, m);
             Trie2.Range r = it.next();
             assertEquals("", 0x2f800,  r.startCodePoint);
             assertEquals("", 0x2fbff,  r.endCodePoint);
             assertEquals("", 0xdefa17, r.value);
             assertEquals("", false,    r.leadSurrogate);

             assertFalse("", it.hasNext());
         }
         
         // Trie2.serialize()
         //     Test the implementation in Trie2, which is used with Read Only Tries.
         //
         {
             Trie2Writable trie = new Trie2Writable(101, 0);
             trie.setRange(0xf000, 0x3c000, 200, true);
             trie.set(0xffee, 300);
             Trie2_16 frozen16 = trie.toTrie2_16();
             Trie2_32 frozen32 = trie.toTrie2_32();
             assertEquals("", trie, frozen16);
             assertEquals("", trie, frozen32);
             assertEquals("", frozen16, frozen32);
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             try {
                 frozen16.serialize(os);
                 Trie2 unserialized16 = Trie2.createFromSerialized(ByteBuffer.wrap(os.toByteArray()));
                 assertEquals("", trie, unserialized16);
                 assertEquals("", Trie2_16.class, unserialized16.getClass());
                 
                 os.reset();
                 frozen32.serialize(os);
                 Trie2 unserialized32 = Trie2.createFromSerialized(ByteBuffer.wrap(os.toByteArray()));
                 assertEquals("", trie, unserialized32);
                 assertEquals("", Trie2_32.class, unserialized32.getClass());
             } catch (IOException e) {
                 errln(where() + " Unexpected exception:  " + e);
             }
                 
             
         }
     }
     
     
    @Test
     public void TestTrie2WritableAPI() {
         //
         //   Trie2Writable methods.  Check that all functions are present and 
         //      nominally working.  Not an in-depth test.
         //
                 
         // Trie2Writable constructor
         Trie2 t1 = new Trie2Writable(6, 666);
         
         // Constructor from another Trie2
         Trie2 t2 = new Trie2Writable(t1);
         assertTrue("", t1.equals(t2));
         
         // Set / Get
         Trie2Writable t1w = new Trie2Writable(10, 666);
         t1w.set(0x4567, 99);
         assertEquals("", 10, t1w.get(0x4566));
         assertEquals("", 99, t1w.get(0x4567));
         assertEquals("", 666, t1w.get(-1));
         assertEquals("", 666, t1w.get(0x110000));
         
         
         // SetRange
         t1w = new Trie2Writable(10, 666);
         t1w.setRange(13 /*start*/, 6666 /*end*/, 7788 /*value*/, false  /*overwrite */);
         t1w.setRange(6000, 7000, 9900, true);
         assertEquals("",   10, t1w.get(12));
         assertEquals("", 7788, t1w.get(13));
         assertEquals("", 7788, t1w.get(5999));
         assertEquals("", 9900, t1w.get(6000));
         assertEquals("", 9900, t1w.get(7000));
         assertEquals("",   10, t1w.get(7001));
         assertEquals("",  666, t1w.get(0x110000));
         
         // setRange from a Trie2.Range
         //    (Ranges are more commonly created by iterating over a Trie2,
         //     but create one by hand here)
         Trie2.Range r = new Trie2.Range();
         r.startCodePoint = 50;
         r.endCodePoint   = 52;
         r.value          = 0x12345678;
         r.leadSurrogate  = false;
         t1w = new Trie2Writable(0, 0xbad);
         t1w.setRange(r, true);
         assertEquals(null, 0, t1w.get(49));
         assertEquals("", 0x12345678, t1w.get(50));
         assertEquals("", 0x12345678, t1w.get(52));
         assertEquals("", 0, t1w.get(53));
         
         
         // setForLeadSurrogateCodeUnit / getFromU16SingleLead
         t1w = new Trie2Writable(10, 0xbad);
         assertEquals("", 10, t1w.getFromU16SingleLead((char)0x0d801));
         t1w.setForLeadSurrogateCodeUnit((char)0xd801, 5000);
         t1w.set(0xd801, 6000);
         assertEquals("", 5000, t1w.getFromU16SingleLead((char)0x0d801));
         assertEquals("", 6000, t1w.get(0x0d801));
         
         // get().  Is covered by nearly every other test.
                 
         
         // Trie2_16 getAsFrozen_16()
         t1w = new Trie2Writable(10, 666);
         t1w.set(42, 5555);
         t1w.set(0x1ff00, 224);
         Trie2_16 t1_16 = t1w.toTrie2_16();
         assertTrue("", t1w.equals(t1_16));
         // alter the writable Trie2 and then re-freeze.
         t1w.set(152, 129);
         t1_16 = t1w.toTrie2_16();
         assertTrue("", t1w.equals(t1_16));
         assertEquals("", 129, t1w.get(152));
         
         // Trie2_32 getAsFrozen_32()
         //
         t1w = new Trie2Writable(10, 666);
         t1w.set(42, 5555);
         t1w.set(0x1ff00, 224);
         Trie2_32 t1_32 = t1w.toTrie2_32();
         assertTrue("", t1w.equals(t1_32));
         // alter the writable Trie2 and then re-freeze.
         t1w.set(152, 129);
         assertNotEquals("", t1_32, t1w);
         t1_32 = t1w.toTrie2_32();
         assertTrue("", t1w.equals(t1_32));
         assertEquals("", 129, t1w.get(152));
         
         
         // serialize(OutputStream os, ValueWidth width)
         // 
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         t1w = new Trie2Writable(0, 0xbad);
         t1w.set(0x41, 0x100);
         t1w.set(0xc2, 0x200);
         t1w.set(0x404, 0x300);
         t1w.set(0xd903, 0x500);
         t1w.set(0xdd29, 0x600);
         t1w.set(0x1055d3, 0x700);
         t1w.setForLeadSurrogateCodeUnit((char)0xda1a, 0x800);
         try {
             // Serialize to 16 bits.
             int serializedLen = t1w.toTrie2_16().serialize(os);
             // Fragile test.  Serialized length could change with changes to compaction.
             //                But it should not change unexpectedly.
             assertEquals("", 3508, serializedLen);
             Trie2 t1ws16 = Trie2.createFromSerialized(ByteBuffer.wrap(os.toByteArray()));
             assertEquals("", t1ws16.getClass(), Trie2_16.class);
             assertEquals("", t1w, t1ws16);
             
             // Serialize to 32 bits
             os.reset();
             serializedLen = t1w.toTrie2_32().serialize(os);
             // Fragile test.  Serialized length could change with changes to compaction.
             //                But it should not change unexpectedly.
             assertEquals("", 4332, serializedLen);
             Trie2 t1ws32 = Trie2.createFromSerialized(ByteBuffer.wrap(os.toByteArray()));
             assertEquals("", t1ws32.getClass(), Trie2_32.class);
             assertEquals("", t1w, t1ws32);
         } catch (IOException e) {
             errln(where() + e.toString());
         }
         
                
     }
     
    @Test
     public void TestCharSequenceIterator() {
         String text = "abc123\ud800\udc01 ";    // Includes a Unicode supplemental character
         String vals = "LLLNNNX?S";
         
         Trie2Writable  tw = new Trie2Writable(0, 666);
         tw.setRange('a', 'z', 'L', false);
         tw.setRange('1', '9', 'N', false);
         tw.set(' ', 'S');
         tw.set(0x10001, 'X');

         Trie2.CharSequenceIterator it = tw.charSequenceIterator(text, 0);
         
         // Check forwards iteration.
         Trie2.CharSequenceValues ir;
         int i;
         for (i=0; it.hasNext(); i++) {
             ir = it.next();
             int expectedCP = Character.codePointAt(text, i);
             assertEquals("" + " i="+i, expectedCP,     ir.codePoint);
             assertEquals("" + " i="+i, i,              ir.index);
             assertEquals("" + " i="+i, vals.charAt(i), ir.value);
             if (expectedCP >= 0x10000) {
                 i++;
             }
         }
         assertEquals("", text.length(), i);
         
         // Check reverse iteration, starting at an intermediate point.
         it.set(5);
         for (i=5; it.hasPrevious(); ) {
             ir = it.previous();
             int expectedCP = Character.codePointBefore(text, i);
             i -= (expectedCP < 0x10000? 1 : 2);            
             assertEquals("" + " i="+i, expectedCP,     ir.codePoint);
             assertEquals("" + " i="+i, i,              ir.index);
             assertEquals("" + " i="+i, vals.charAt(i), ir.value);
         }
         assertEquals("", 0, i);
         
     }
     
     
     //
     //  Port of Tests from ICU4C ...
     //
     //     setRanges array elements are
     //        {start Code point, limit CP, value, overwrite}
     //
     //     There must be an entry with limit 0 and with the intialValue.
     //     It may be preceded by an entry with negative limit and the errorValue.
     //
     //     checkRanges array elemets are
     //        { limit code point, value}
     // 
     //     The expected value range is from the previous boundary's limit to before
     //        this boundary's limit

     // 
     String[] trieNames = {"setRanges1", "setRanges2", "setRanges3", "setRangesEmpty", "setRangesSingleValue"};
     /* set consecutive ranges, even with value 0 */
          
    
         
     private static int[][] setRanges1 ={
         { 0,        0,        0,      0 },
         { 0,        0x40,     0,      0 },
         { 0x40,     0xe7,     0x1234, 0 },
         { 0xe7,     0x3400,   0,      0 },
         { 0x3400,   0x9fa6,   0x6162, 0 },
         { 0x9fa6,   0xda9e,   0x3132, 0 },
         { 0xdada,   0xeeee,   0x87ff, 0 },
         { 0xeeee,   0x11111,  1,      0 },
         { 0x11111,  0x44444,  0x6162, 0 },
         { 0x44444,  0x60003,  0,      0 },
         { 0xf0003,  0xf0004,  0xf,    0 },
         { 0xf0004,  0xf0006,  0x10,   0 },
         { 0xf0006,  0xf0007,  0x11,   0 },
         { 0xf0007,  0xf0040,  0x12,   0 },
         { 0xf0040,  0x110000, 0,      0 }
     };

     private static int[][]  checkRanges1 = {
         { 0,        0 },
         { 0x40,     0 },
         { 0xe7,     0x1234 },
         { 0x3400,   0 },
         { 0x9fa6,   0x6162 },
         { 0xda9e,   0x3132 },
         { 0xdada,   0 },
         { 0xeeee,   0x87ff },
         { 0x11111,  1 },
         { 0x44444,  0x6162 },
         { 0xf0003,  0 },
         { 0xf0004,  0xf },
         { 0xf0006,  0x10 },
         { 0xf0007,  0x11 },
         { 0xf0040,  0x12 },
         { 0x110000, 0 }
     };

     /* set some interesting overlapping ranges */
     private static  int [][] setRanges2={
         { 0,        0,        0,      0 },
         { 0x21,     0x7f,     0x5555, 1 },
         { 0x2f800,  0x2fedc,  0x7a,   1 },
         { 0x72,     0xdd,     3,      1 },
         { 0xdd,     0xde,     4,      0 },
         { 0x201,    0x240,    6,      1 },  /* 3 consecutive blocks with the same pattern but */
         { 0x241,    0x280,    6,      1 },  /* discontiguous value ranges, testing utrie2_enum() */
         { 0x281,    0x2c0,    6,      1 },
         { 0x2f987,  0x2fa98,  5,      1 },
         { 0x2f777,  0x2f883,  0,      1 },
         { 0x2f900,  0x2ffaa,  1,      0 },
         { 0x2ffaa,  0x2ffab,  2,      1 },
         { 0x2ffbb,  0x2ffc0,  7,      1 }
     };

     private static int[] [] checkRanges2={
         { 0,        0 },
         { 0x21,     0 },
         { 0x72,     0x5555 },
         { 0xdd,     3 },
         { 0xde,     4 },
         { 0x201,    0 },
         { 0x240,    6 },
         { 0x241,    0 },
         { 0x280,    6 },
         { 0x281,    0 },
         { 0x2c0,    6 },
         { 0x2f883,  0 },
         { 0x2f987,  0x7a },
         { 0x2fa98,  5 },
         { 0x2fedc,  0x7a },
         { 0x2ffaa,  1 },
         { 0x2ffab,  2 },
         { 0x2ffbb,  0 },
         { 0x2ffc0,  7 },
         { 0x110000, 0 }
     };

/*
     private static int[] [] checkRanges2_d800={
         { 0x10000,  0 },
         { 0x10400,  0 }
     };

     private static int[][] checkRanges2_d87e={
         { 0x2f800,  6 },
         { 0x2f883,  0 },
         { 0x2f987,  0x7a },
         { 0x2fa98,  5 },
         { 0x2fc00,  0x7a }
     };

     private static int[][] checkRanges2_d87f={
         { 0x2fc00,  0 },
         { 0x2fedc,  0x7a },
         { 0x2ffaa,  1 },
         { 0x2ffab,  2 },
         { 0x2ffbb,  0 },
         { 0x2ffc0,  7 },
         { 0x30000,  0 }
     };

     private static int[][]  checkRanges2_dbff={
         { 0x10fc00, 0 },
         { 0x110000, 0 }
     };
*/

     /* use a non-zero initial value */
     private static int[][] setRanges3={
         { 0,        0,        9, 0 },     // non-zero initial value.
         { 0x31,     0xa4,     1, 0 },
         { 0x3400,   0x6789,   2, 0 },
         { 0x8000,   0x89ab,   9, 1 },
         { 0x9000,   0xa000,   4, 1 },
         { 0xabcd,   0xbcde,   3, 1 },
         { 0x55555,  0x110000, 6, 1 },  /* highStart<U+ffff with non-initialValue */
         { 0xcccc,   0x55555,  6, 1 }
     };

     private static int[][] checkRanges3={
         { 0,        9 },  /* non-zero initialValue */
         { 0x31,     9 },
         { 0xa4,     1 },
         { 0x3400,   9 },
         { 0x6789,   2 },
         { 0x9000,   9 },
         { 0xa000,   4 },
         { 0xabcd,   9 },
         { 0xbcde,   3 },
         { 0xcccc,   9 },
         { 0x110000, 6 }
     };

     /* empty or single-value tries, testing highStart==0 */
     private static int[][] setRangesEmpty={
         { 0,        0,        3, 0 }         // Only the element with the initial value.
     };

     private static int[][] checkRangesEmpty={
         { 0,        3 },
         { 0x110000, 3 }
     };

     private static int[][] setRangesSingleValue={
         { 0,        0,        3,  0 },   // Initial value = 3
         { 0,        0x110000, 5, 1 },
     };

     private static int[][] checkRangesSingleValue={
         { 0,        3 },
         { 0x110000, 5 }
     };


     //
     // Create a test Trie2 from a setRanges test array.
     //    Range data ported from C.
     //
     private Trie2Writable genTrieFromSetRanges(int [][] ranges) {
         int i = 0;
         int initialValue = 0;
         int errorValue   = 0x0bad;
         
         if (ranges[i][1] < 0) {
             errorValue = ranges[i][2];
             i++;
         }
         initialValue = ranges[i++][2];
         Trie2Writable trie = new Trie2Writable(initialValue, errorValue);
         
         for (; i<ranges.length; i++) {
             int     rangeStart = ranges[i][0];
             int     rangeEnd   = ranges[i][1]-1;
             int     value      = ranges[i][2];
             boolean overwrite = (ranges[i][3] != 0);
             trie.setRange(rangeStart, rangeEnd, value, overwrite);
         }
         
         // Insert some non-default values for lead surrogates.
         //   TODO:  this should be represented in the data.
         trie.setForLeadSurrogateCodeUnit((char)0xd800, 90);
         trie.setForLeadSurrogateCodeUnit((char)0xd999, 94);
         trie.setForLeadSurrogateCodeUnit((char)0xdbff, 99);
         
         return trie;
     }

     
     //
     //  Check the expected values from a single Trie2.
     //
     private void trieGettersTest(String           testName,
                                  Trie2            trie,         // The Trie2 to test.
                                  int[][]          checkRanges)  // Expected data. 
                                                                 //   Tuples of (value, high limit code point)
                                                                 //   High limit is first code point following the range
                                                                 //   with the indicated value.
                                                                 //      (Structures copied from ICU4C tests.)
     {
         int countCheckRanges = checkRanges.length;

         int initialValue, errorValue;
         int value, value2;
         int start, limit;
         int i, countSpecials;

         countSpecials=0;  /*getSpecialValues(checkRanges, countCheckRanges, &initialValue, &errorValue);*/
         errorValue = 0x0bad;
         initialValue = 0;
         if (checkRanges[countSpecials][0] == 0) {
             initialValue = checkRanges[countSpecials][1];
             countSpecials++;
         }

         start=0;
         for(i=countSpecials; i<countCheckRanges; ++i) {
             limit=checkRanges[i][0];
             value=checkRanges[i][1];

             while(start<limit) {
                 value2=trie.get(start);
                 if (value != value2) {
                     // The redundant if, outside of the assert, is for speed.  
                     // It makes a significant difference for this test.
                     assertEquals("wrong value for " + testName + " of " + Integer.toHexString(start), value, value2);
                 }
                 ++start;
             }
         }


         if(!testName.startsWith("dummy") && !testName.startsWith("trie1")) {
             /* Test values for lead surrogate code units.
              * For non-lead-surrogate code units,  getFromU16SingleLead() and get()
              *   should be the same.
              */
             for(start=0xd7ff; start<0xdc01; ++start) {
                 switch(start) {
                 case 0xd7ff:
                 case 0xdc00:
                     value=trie.get(start);
                     break;
                 case 0xd800:
                     value=90;
                     break;
                 case 0xd999:
                     value=94;
                     break;
                 case 0xdbff:
                     value=99;
                     break;
                 default:
                     value=initialValue;
                     break;
                 }
                 value2 = trie.getFromU16SingleLead((char)start);
                 if(value2!=value) {
                     errln(where() + " testName: " + testName + " getFromU16SingleLead() failed." +
                             "char, exected, actual = " + Integer.toHexString(start) + ", " + 
                             Integer.toHexString(value) + ", " + Integer.toHexString(value2));
                 }
             }
         }

         /* test errorValue */
         value=trie.get(-1);
         value2=trie.get(0x110000);
         if(value!=errorValue || value2!=errorValue) {
             errln("trie2.get() error value test.  Expected, actual1, actual2 = " +
                     errorValue + ", " + value + ", " + value2);
         }
         
         // Check that Trie enumeration produces the same contents as simple get()
         for (Trie2.Range range: trie) {
             for (int cp=range.startCodePoint; cp<=range.endCodePoint; cp++) {
                 if (range.leadSurrogate) {
                     assertTrue(testName, cp>=(char)0xd800 && cp<(char)0xdc00);
                     assertEquals(testName, range.value, trie.getFromU16SingleLead((char)cp));
                 } else {
                     assertEquals(testName, range.value, trie.get(cp));
                 }
             }
         }
     }
                     
     // Was testTrieRanges in ICU4C.  Renamed to not conflict with ICU4J test framework.
     private void checkTrieRanges(String testName, String serializedName, boolean withClone,
             int[][] setRanges, int [][] checkRanges) throws IOException {
         
         // Run tests against Tries that were built by ICU4C and serialized.
         String fileName16 = "Trie2Test." + serializedName + ".16.tri2";
         String fileName32 = "Trie2Test." + serializedName + ".32.tri2";
         
         InputStream is = Trie2Test.class.getResourceAsStream(fileName16);
         Trie2 trie16;
         try {
             trie16 = Trie2.createFromSerialized(ICUBinary.getByteBufferFromInputStreamAndCloseStream(is));
         } finally {
             is.close();
         }
         trieGettersTest(testName, trie16, checkRanges);

         is = Trie2Test.class.getResourceAsStream(fileName32);
         Trie2 trie32;
         try {
             trie32 = Trie2.createFromSerialized(ICUBinary.getByteBufferFromInputStreamAndCloseStream(is));
         } finally {
             is.close();
         }
         trieGettersTest(testName, trie32, checkRanges);

         // Run the same tests against locally contructed Tries.
         Trie2Writable trieW = genTrieFromSetRanges(setRanges);
         trieGettersTest(testName, trieW,  checkRanges);
         assertEquals("", trieW, trie16);   // Locally built tries must be
         assertEquals("", trieW, trie32);   //   the same as those imported from ICU4C
         
         
         Trie2_32 trie32a = trieW.toTrie2_32();
         trieGettersTest(testName, trie32a, checkRanges);

         Trie2_16 trie16a = trieW.toTrie2_16();
         trieGettersTest(testName, trie16a, checkRanges);
         
     }
     
     // Was "TrieTest" in trie2test.c 
    @Test
     public void TestRanges() throws IOException {
         checkTrieRanges("set1",           "setRanges1",     false, setRanges1,     checkRanges1);         
         checkTrieRanges("set2-overlap",   "setRanges2",     false, setRanges2,     checkRanges2);
         checkTrieRanges("set3-initial-9", "setRanges3",     false, setRanges3,     checkRanges3);
         checkTrieRanges("set-empty",      "setRangesEmpty", false, setRangesEmpty, checkRangesEmpty);
         checkTrieRanges("set-single-value", "setRangesSingleValue", false, setRangesSingleValue, 
             checkRangesSingleValue);
         checkTrieRanges("set2-overlap.withClone", "setRanges2", true, setRanges2,     checkRanges2);
     }

     
     private String where() {
         StackTraceElement[] st = new Throwable().getStackTrace();
         String w = "File: " + st[1].getFileName() + ", Line " + st[1].getLineNumber();
         return w;
     }
}
