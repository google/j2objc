/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
*******************************************************************************
* Copyright (C) 1996-2010, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

package android.icu.dev.test.util;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.CharTrie;
import android.icu.impl.IntTrie;
import android.icu.impl.IntTrieBuilder;
import android.icu.impl.Trie;
import android.icu.impl.TrieBuilder;
import android.icu.impl.TrieIterator;
import android.icu.text.UTF16;
import android.icu.util.RangeValueIterator;

/**
* Testing class for Trie. Tests here will be simple, since both CharTrie and 
* IntTrie are very similar and are heavily used in other parts of ICU4J.
* Codes using Tries are expected to have detailed tests.
* @author Syn Wee Quek
* @since release 2.1 Jan 01 2002
*/
public final class TrieTest extends TestFmwk 
{ 
    // constructor ---------------------------------------------------
  
    /**
    * Constructor
    */
    public TrieTest()
    {
    }
      
    // public methods -----------------------------------------------
    
    /** 
     * Values for setting possibly overlapping, out-of-order ranges of values
     */
    private static final class SetRange 
    {
        SetRange(int start, int limit, int value, boolean overwrite)
        {
            this.start = start;
            this.limit = limit;
            this.value = value;
            this.overwrite = overwrite;
        }
        
        int start, limit;
        int value;
        boolean overwrite;
    }
    
    /**
     * Values for testing:
     * value is set from the previous boundary's limit to before
     * this boundary's limit
     */
    private static final class CheckRange 
    {
        CheckRange(int limit, int value)
        {
            this.limit = limit;
            this.value = value;
        }
        
        int limit;
        int value;
    }
    
    private static final class _testFoldedValue 
                                        implements TrieBuilder.DataManipulate  
    {
        public _testFoldedValue(IntTrieBuilder builder)
        {
            m_builder_ = builder;
        }
        
        public int getFoldedValue(int start, int offset)
        {
            int foldedValue = 0;
            int limit = start + 0x400;
            while (start < limit) {
                int value = m_builder_.getValue(start);
                if (m_builder_.isInZeroBlock(start)) {
                    start += TrieBuilder.DATA_BLOCK_LENGTH;
                } 
                else {
                    foldedValue |= value;
                    ++ start;
                }
            }
        
            if (foldedValue != 0) {
                return (offset << 16) | foldedValue;
            } 
            return 0;
        }
        
        private IntTrieBuilder m_builder_;
    }
    
    private static final class _testFoldingOffset 
                                                implements Trie.DataManipulate 
    {
        public int getFoldingOffset(int value)
        {
            return value >>> 16;
        }
    }
    
    private static final class _testEnumValue extends TrieIterator
    {
        public _testEnumValue(Trie data)
        {
            super(data);
        }
        
        protected int extract(int value)
        {
            return value ^ 0x5555;
        }
    }
    
    private void _testTrieIteration(IntTrie trie, CheckRange checkRanges[],
                                    int countCheckRanges) 
    {
        // write a string
        int countValues = 0;
        StringBuffer s = new StringBuffer();
        int values[] = new int[30];
        for (int i = 0; i < countCheckRanges; ++ i) {
            int c = checkRanges[i].limit;
            if (c != 0) {
                -- c;
                UTF16.append(s, c);
                values[countValues ++] = checkRanges[i].value;
            }
        }
        int limit = s.length();
        // try forward
        int p = 0;
        int i = 0;
        while(p < limit) {
            int c = UTF16.charAt(s, p);
            p += UTF16.getCharCount(c);
            int value = trie.getCodePointValue(c);
            if (value != values[i]) {
                errln("wrong value from UTRIE_NEXT(U+" 
                      + Integer.toHexString(c) + "): 0x" 
                      + Integer.toHexString(value) + " instead of 0x"
                      + Integer.toHexString(values[i]));
            }
            // unlike the c version lead is 0 if c is non-supplementary
            char lead = UTF16.getLeadSurrogate(c);
            char trail = UTF16.getTrailSurrogate(c); 
            if (lead == 0 
                ? trail != s.charAt(p - 1) 
                : !UTF16.isLeadSurrogate(lead) 
                  || !UTF16.isTrailSurrogate(trail) || lead != s.charAt(p - 2) 
                  || trail != s.charAt(p - 1)) {
                errln("wrong (lead, trail) from UTRIE_NEXT(U+" 
                      + Integer.toHexString(c));
                continue;
            }
            if (lead != 0) {
                value = trie.getLeadValue(lead);
                value = trie.getTrailValue(value, trail);
                if (value != trie.getSurrogateValue(lead, trail)
                    && value != values[i]) {
                    errln("wrong value from getting supplementary " 
                          + "values (U+" 
                          + Integer.toHexString(c) + "): 0x"
                          + Integer.toHexString(value) + " instead of 0x"
                          + Integer.toHexString(values[i]));
                }
            }
            ++ i;
        }
    }
    
    private void _testTrieRanges(SetRange setRanges[], int countSetRanges, 
                                 CheckRange checkRanges[], int countCheckRanges,
                                 boolean latin1Linear) 
    {
        IntTrieBuilder newTrie = new IntTrieBuilder(null, 2000,
                                                    checkRanges[0].value, 
                                                    checkRanges[0].value,
                                                    latin1Linear);
    
        // set values from setRanges[]
        boolean ok = true;
        for (int i = 0; i < countSetRanges; ++ i) {
            int start = setRanges[i].start;
            int limit = setRanges[i].limit;
            int value = setRanges[i].value;
            boolean overwrite = setRanges[i].overwrite;
            if ((limit - start) == 1 && overwrite) {
                ok &= newTrie.setValue(start, value);
            } 
            else {
                ok &= newTrie.setRange(start, limit, value, overwrite);
            }
        }
        if (!ok) {
            errln("setting values into a trie failed");
            return;
        }
    
        // verify that all these values are in the new Trie
        int start = 0;
        for (int i = 0; i < countCheckRanges; ++ i) {
            int limit = checkRanges[i].limit;
            int value = checkRanges[i].value;
    
            while (start < limit) {
                if (value != newTrie.getValue(start)) {
                    errln("newTrie [U+" 
                          + Integer.toHexString(start) + "]==0x" 
                          + Integer.toHexString(newTrie.getValue(start)) 
                          + " instead of 0x" + Integer.toHexString(value));
                }
                ++ start;
            }
        }
    
        IntTrie trie = newTrie.serialize(new _testFoldedValue(newTrie), 
                                         new _testFoldingOffset());
    
        // test linear Latin-1 range from utrie_getData()
        if (latin1Linear) {
            start = 0;
            for (int i = 0; i < countCheckRanges && start <= 0xff; ++ i) {
                int limit = checkRanges[i].limit;
                int value = checkRanges[i].value;
    
                while (start < limit && start <= 0xff) {
                    if (value != trie.getLatin1LinearValue((char)start)) {
                        errln("IntTrie.getLatin1LinearValue[U+" 
                              + Integer.toHexString(start) + "]==0x"
                              + Integer.toHexString(
                                        trie.getLatin1LinearValue((char) start)) 
                              + " instead of 0x" + Integer.toHexString(value));
                    }
                    ++ start;
                }
            }
        }
    
        if (latin1Linear != trie.isLatin1Linear()) {
            errln("trie serialization did not preserve "
                  + "Latin-1-linearity");
        }
    
        // verify that all these values are in the serialized Trie
        start = 0;
        for (int i = 0; i < countCheckRanges; ++ i) {
            int limit = checkRanges[i].limit;
            int value = checkRanges[i].value;
    
            if (start == 0xd800) {
                // skip surrogates
                start = limit;
                continue;
            }
    
            while (start < limit) {
                if (start <= 0xffff) {
                    int value2 = trie.getBMPValue((char)start);
                    if (value != value2) {
                        errln("serialized trie.getBMPValue(U+"
                              + Integer.toHexString(start) + " == 0x" 
                              + Integer.toHexString(value2) + " instead of 0x"
                              + Integer.toHexString(value));
                    }
                    if (!UTF16.isLeadSurrogate((char)start)) {
                        value2 = trie.getLeadValue((char)start);
                        if (value != value2) {
                            errln("serialized trie.getLeadValue(U+"
                              + Integer.toHexString(start) + " == 0x" 
                              + Integer.toHexString(value2) + " instead of 0x"
                              + Integer.toHexString(value));
                        }
                    }
                }
                int value2 = trie.getCodePointValue(start);
                if (value != value2) {
                    errln("serialized trie.getCodePointValue(U+"
                          + Integer.toHexString(start) + ")==0x" 
                          + Integer.toHexString(value2) + " instead of 0x" 
                          + Integer.toHexString(value));
                }
                ++ start;
            }
        }
    
        // enumerate and verify all ranges
        
        int enumRanges = 1;
        TrieIterator iter  = new _testEnumValue(trie);
        RangeValueIterator.Element result = new RangeValueIterator.Element();
        while (iter.next(result)) {
            if (result.start != checkRanges[enumRanges -1].limit 
                || result.limit != checkRanges[enumRanges].limit
                || (result.value ^ 0x5555) != checkRanges[enumRanges].value) {
                errln("utrie_enum() delivers wrong range [U+"
                      + Integer.toHexString(result.start) + "..U+" 
                      + Integer.toHexString(result.limit) + "].0x" 
                      + Integer.toHexString(result.value ^ 0x5555) 
                      + " instead of [U+"
                      + Integer.toHexString(checkRanges[enumRanges -1].limit) 
                      + "..U+" 
                      + Integer.toHexString(checkRanges[enumRanges].limit) 
                      + "].0x" 
                      + Integer.toHexString(checkRanges[enumRanges].value));
            }
            enumRanges ++;
        }
    
        // test linear Latin-1 range
        if (trie.isLatin1Linear()) {
            for (start = 0; start < 0x100; ++ start) {
                if (trie.getLatin1LinearValue((char)start) 
                    != trie.getLeadValue((char)start)) {
                    errln("trie.getLatin1LinearValue[U+"
                          + Integer.toHexString(start) + "]=0x"
                          + Integer.toHexString(
                                        trie.getLatin1LinearValue((char)start))
                          + " instead of 0x" 
                          + Integer.toHexString(
                                        trie.getLeadValue((char)start)));
                }
            }
        }
    
        _testTrieIteration(trie, checkRanges, countCheckRanges);
    }
    
    private void _testTrieRanges2(SetRange setRanges[], 
                                  int countSetRanges, 
                                  CheckRange checkRanges[], 
                                  int countCheckRanges) 
    {
        _testTrieRanges(setRanges, countSetRanges, checkRanges, countCheckRanges,
                        false);
        
        _testTrieRanges(setRanges, countSetRanges, checkRanges, countCheckRanges,
                        true);
    }
    
    private void _testTrieRanges4(SetRange setRanges[], int countSetRanges,
                                  CheckRange checkRanges[], 
                                  int countCheckRanges) 
    {
        _testTrieRanges2(setRanges, countSetRanges, checkRanges, 
                         countCheckRanges);
    }
    
    // test data ------------------------------------------------------------
    
    /** 
     * set consecutive ranges, even with value 0
     */
    private static SetRange setRanges1[]={
        new SetRange(0,      0x20,       0,      false),
        new SetRange(0x20,   0xa7,       0x1234, false),
        new SetRange(0xa7,   0x3400,     0,      false),
        new SetRange(0x3400, 0x9fa6,     0x6162, false),
        new SetRange(0x9fa6, 0xda9e,     0x3132, false),
        // try to disrupt _testFoldingOffset16()
        new SetRange(0xdada, 0xeeee,     0x87ff, false), 
        new SetRange(0xeeee, 0x11111,    1,      false),
        new SetRange(0x11111, 0x44444,   0x6162, false),
        new SetRange(0x44444, 0x60003,   0,      false),
        new SetRange(0xf0003, 0xf0004,   0xf,    false),
        new SetRange(0xf0004, 0xf0006,   0x10,   false),
        new SetRange(0xf0006, 0xf0007,   0x11,   false),
        new SetRange(0xf0007, 0xf0020,   0x12,   false),
        new SetRange(0xf0020, 0x110000,  0,      false)
    };
    
    private static CheckRange checkRanges1[]={
        new CheckRange(0,      0), // dummy start range to make _testEnumRange() simpler
        new CheckRange(0x20,   0),
        new CheckRange(0xa7,   0x1234),
        new CheckRange(0x3400, 0),
        new CheckRange(0x9fa6, 0x6162),
        new CheckRange(0xda9e, 0x3132),
        new CheckRange(0xdada, 0),
        new CheckRange(0xeeee, 0x87ff),
        new CheckRange(0x11111,1),
        new CheckRange(0x44444,0x6162),
        new CheckRange(0xf0003,0),
        new CheckRange(0xf0004,0xf),
        new CheckRange(0xf0006,0x10),
        new CheckRange(0xf0007,0x11),
        new CheckRange(0xf0020,0x12),
        new CheckRange(0x110000, 0)
    };
    
    /** 
     * set some interesting overlapping ranges
     */
    private static SetRange setRanges2[]={
        new SetRange(0x21,   0x7f,       0x5555, true),
        new SetRange(0x2f800,0x2fedc,    0x7a,   true),
        new SetRange(0x72,   0xdd,       3,      true),
        new SetRange(0xdd,   0xde,       4,      false),
        new SetRange(0x2f987,0x2fa98,    5,      true),
        new SetRange(0x2f777,0x2f833,    0,      true),
        new SetRange(0x2f900,0x2ffee,    1,      false),
        new SetRange(0x2ffee,0x2ffef,    2,      true)
    };
    
    private static CheckRange checkRanges2[]={
        // dummy start range to make _testEnumRange() simpler
        new CheckRange(0,      0),       
        new CheckRange(0x21,   0),
        new CheckRange(0x72,   0x5555),
        new CheckRange(0xdd,   3),
        new CheckRange(0xde,   4),
        new CheckRange(0x2f833,0),
        new CheckRange(0x2f987,0x7a),
        new CheckRange(0x2fa98,5),
        new CheckRange(0x2fedc,0x7a),
        new CheckRange(0x2ffee,1),
        new CheckRange(0x2ffef,2),
        new CheckRange(0x110000, 0)
    };
    
    /** 
     * use a non-zero initial value
     */
    private static SetRange setRanges3[]={
        new SetRange(0x31,   0xa4,   1,  false),
        new SetRange(0x3400, 0x6789, 2,  false),
        new SetRange(0x30000,0x34567,9,  true),
        new SetRange(0x45678,0x56789,3,  true)
    };
    
    private static CheckRange checkRanges3[]={
        // dummy start range, also carries the initial value
        new CheckRange(0,      9),  
        new CheckRange(0x31,   9),
        new CheckRange(0xa4,   1),
        new CheckRange(0x3400, 9),
        new CheckRange(0x6789, 2),
        new CheckRange(0x45678,9),
        new CheckRange(0x56789,3),
        new CheckRange(0x110000,9)
    };
    
    @Test
    public void TestIntTrie() 
    {
        _testTrieRanges4(setRanges1, setRanges1.length, checkRanges1, 
                         checkRanges1.length);
        _testTrieRanges4(setRanges2, setRanges2.length, checkRanges2, 
                         checkRanges2.length); 
        _testTrieRanges4(setRanges3, setRanges3.length, checkRanges3, 
                         checkRanges3.length);
    }

    private static class DummyGetFoldingOffset implements Trie.DataManipulate {
        public int getFoldingOffset(int value) {
            return -1; /* never get non-initialValue data for supplementary code points */
        }
    }

    @Test
    public void TestDummyCharTrie() {
        CharTrie trie;
        final int initialValue=0x313, leadUnitValue=0xaffe; 
        int value;
        int c;
        trie=new CharTrie(initialValue, leadUnitValue, new DummyGetFoldingOffset());

        /* test that all code points have initialValue */
        for(c=0; c<=0x10ffff; ++c) {
            value=trie.getCodePointValue(c);
            if(value!=initialValue) {
                errln("CharTrie/dummy.getCodePointValue(c)(U+"+hex(c)+")=0x"+hex(value)+" instead of 0x"+hex(initialValue));
            }
        }

        /* test that the lead surrogate code units have leadUnitValue */
        for(c=0xd800; c<=0xdbff; ++c) {
            value=trie.getLeadValue((char)c);
            if(value!=leadUnitValue) {
                errln("CharTrie/dummy.getLeadValue(c)(U+"+hex(c)+")=0x"+hex(value)+" instead of 0x"+hex(leadUnitValue));
            }
        }
    }

    @Test
    public void TestDummyIntTrie() {
        IntTrie trie;
        final int initialValue=0x01234567, leadUnitValue=0x89abcdef; 
        int value;
        int c;
        trie=new IntTrie(initialValue, leadUnitValue, new DummyGetFoldingOffset());

        /* test that all code points have initialValue */
        for(c=0; c<=0x10ffff; ++c) {
            value=trie.getCodePointValue(c);
            if(value!=initialValue) {
                errln("IntTrie/dummy.getCodePointValue(c)(U+"+hex(c)+")=0x"+hex(value)+" instead of 0x"+hex(initialValue));
            }
        }

        /* test that the lead surrogate code units have leadUnitValue */
        for(c=0xd800; c<=0xdbff; ++c) {
            value=trie.getLeadValue((char)c);
            if(value!=leadUnitValue) {
                errln("IntTrie/dummy.getLeadValue(c)(U+"+hex(c)+")=0x"+hex(value)+" instead of 0x"+hex(leadUnitValue));
            }
        }
    }
}
