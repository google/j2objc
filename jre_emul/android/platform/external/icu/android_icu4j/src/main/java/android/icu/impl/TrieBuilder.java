/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 1996-2010, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl;

import java.util.Arrays;

import android.icu.lang.UCharacter;

/**
 * Builder class to manipulate and generate a trie.
 * This is useful for ICU data in primitive types.
 * Provides a compact way to store information that is indexed by Unicode 
 * values, such as character properties, types, keyboard values, etc. This is 
 * very useful when you have a block of Unicode data that contains significant 
 * values while the rest of the Unicode data is unused in the application or 
 * when you have a lot of redundance, such as where all 21,000 Han ideographs 
 * have the same value.  However, lookup is much faster than a hash table.
 * A trie of any primitive data type serves two purposes:
 * <UL type = round>
 *     <LI>Fast access of the indexed values.
 *     <LI>Smaller memory footprint.
 * </UL>
 * This is a direct port from the ICU4C version
 * @author             Syn Wee Quek
 * @hide Only a subset of ICU is exposed in Android
 */
public class TrieBuilder
{
    // public data member ----------------------------------------------
        
    /** 
     * Number of data values in a stage 2 (data array) block. 2, 4, 8, .., 
     * 0x200 
     */
    public static final int DATA_BLOCK_LENGTH = 1 << Trie.INDEX_STAGE_1_SHIFT_;
    
    // public class declaration ----------------------------------------
    
    /**
     * Character data in com.ibm.impl.Trie have different user-specified format
     * for different purposes.
     * This interface specifies methods to be implemented in order for
     * com.ibm.impl.Trie, to surrogate offset information encapsulated within 
     * the data.
     */
    public static interface DataManipulate
    {
        /**
         * Build-time trie callback function, used with serialize().
         * This function calculates a lead surrogate's value including a 
         * folding offset from the 1024 supplementary code points 
         * [start..start+1024[ . 
         * It is U+10000 <= start <= U+10fc00 and (start&0x3ff)==0.
         * The folding offset is provided by the caller. 
         * It is offset=UTRIE_BMP_INDEX_LENGTH+n*UTRIE_SURROGATE_BLOCK_COUNT 
         * with n=0..1023. 
         * Instead of the offset itself, n can be stored in 10 bits - or fewer 
         * if it can be assumed that few lead surrogates have associated data.
         * The returned value must be
         *  - not zero if and only if there is relevant data for the 
         *                        corresponding 1024 supplementary code points
         *  - such that UTrie.getFoldingOffset(UNewTrieGetFoldedValue(..., 
         *                                                    offset))==offset
         * @return a folded value, or 0 if there is no relevant data for the 
         *         lead surrogate.
         */
        public int getFoldedValue(int start, int offset); 
    }
    
    // public methods ----------------------------------------------------
  
    /**
     * Checks if the character belongs to a zero block in the trie
     * @param ch codepoint which data is to be retrieved
     * @return true if ch is in the zero block
     */
    public boolean isInZeroBlock(int ch) 
    {
        // valid, uncompacted trie and valid c?
        if (m_isCompacted_ || ch > UCharacter.MAX_VALUE 
            || ch < UCharacter.MIN_VALUE) {
            return true;
        }
    
        return m_index_[ch >> SHIFT_] == 0;
    }
    
    // package private method -----------------------------------------------
    
    // protected data member -----------------------------------------------
          
    /**
     * Index values at build-time are 32 bits wide for easier processing.
     * Bit 31 is set if the data block is used by multiple index values 
     * (from setRange()).
     */
    protected int m_index_[];
    protected int m_indexLength_;
    protected int m_dataCapacity_; 
    protected int m_dataLength_;
    protected boolean m_isLatin1Linear_;
    protected boolean m_isCompacted_;
    /**
     * Map of adjusted indexes, used in utrie_compact().
     * Maps from original indexes to new ones.
     */
    protected int m_map_[];
        
    /**
     * Shift size for shifting right the input index. 1..9 
     */
    protected static final int SHIFT_ = Trie.INDEX_STAGE_1_SHIFT_;
    /**
     * Length of the index (stage 1) array before folding.
     * Maximum number of Unicode code points (0x110000) shifted right by 
     * SHIFT.
     */
    protected static final int MAX_INDEX_LENGTH_ = (0x110000 >> SHIFT_);
    /** 
     * Length of the BMP portion of the index (stage 1) array. 
     */
    protected static final int BMP_INDEX_LENGTH_ = 0x10000 >> SHIFT_;   
    /**
     * Number of index (stage 1) entries per lead surrogate.
     * Same as number of indexe entries for 1024 trail surrogates,
     * ==0x400>>UTRIE_SHIFT
     * 10 - SHIFT == Number of bits of a trail surrogate that are used in 
     *               index table lookups. 
     */
    protected static final int SURROGATE_BLOCK_COUNT_ = 1 << (10 - SHIFT_);
    /**
     * Mask for getting the lower bits from the input index.
     * DATA_BLOCK_LENGTH - 1.
     */
    protected static final int MASK_ = Trie.INDEX_STAGE_3_MASK_;
    /**
     * Shift size for shifting left the index array values.
     * Increases possible data size with 16-bit index values at the cost
     * of compactability.
     * This requires blocks of stage 2 data to be aligned by UTRIE_DATA_GRANULARITY.
     * 0..UTRIE_SHIFT
     */
    protected static final int INDEX_SHIFT_ = Trie.INDEX_STAGE_2_SHIFT_;
    /**
     * Maximum length of the runtime data (stage 2) array.
     * Limited by 16-bit index values that are left-shifted by INDEX_SHIFT_.
     */
    protected static final int MAX_DATA_LENGTH_ = (0x10000 << INDEX_SHIFT_);
    /**
     * Shifting to position the index value in options
     */
    protected static final int OPTIONS_INDEX_SHIFT_ = 4;
    /** 
     * If set, then the data (stage 2) array is 32 bits wide. 
     */
    protected static final int OPTIONS_DATA_IS_32_BIT_ = 0x100;
    /**
     * If set, then Latin-1 data (for U+0000..U+00ff) is stored in the data 
     * (stage 2) array as a simple, linear array at data + DATA_BLOCK_LENGTH.
     */
    protected static final int OPTIONS_LATIN1_IS_LINEAR_ = 0x200;
    /** 
     * The alignment size of a stage 2 data block. Also the granularity for 
     * compaction. 
     */
    protected static final int DATA_GRANULARITY_ = 1 << INDEX_SHIFT_;
    
    // protected constructor ----------------------------------------------
    
    protected TrieBuilder()
    {
        m_index_ = new int[MAX_INDEX_LENGTH_];
        m_map_ = new int[MAX_BUILD_TIME_DATA_LENGTH_ >> SHIFT_];
        m_isLatin1Linear_ = false;
        m_isCompacted_ = false;
        m_indexLength_ = MAX_INDEX_LENGTH_;
    }
        
    protected TrieBuilder(TrieBuilder table)
    {
        m_index_ = new int[MAX_INDEX_LENGTH_];
        m_indexLength_ = table.m_indexLength_;
        System.arraycopy(table.m_index_, 0, m_index_, 0, m_indexLength_);
        m_dataCapacity_ = table.m_dataCapacity_;
        m_dataLength_ = table.m_dataLength_;
        m_map_ = new int[table.m_map_.length];
        System.arraycopy(table.m_map_, 0, m_map_, 0, m_map_.length);
        m_isLatin1Linear_ = table.m_isLatin1Linear_;
        m_isCompacted_ = table.m_isCompacted_;
    }
        
    // protected functions ------------------------------------------------

    /**
     * Compare two sections of an array for equality.
     */
    protected static final boolean equal_int(int[] array, int start1, int start2, int length) {
        while(length>0 && array[start1]==array[start2]) {
            ++start1;
            ++start2;
            --length;
        }
        return length==0;
    }

    /**
     * Set a value in the trie index map to indicate which data block
     * is referenced and which one is not.
     * utrie_compact() will remove data blocks that are not used at all.
     * Set
     * - 0 if it is used
     * - -1 if it is not used
     */
    protected void findUnusedBlocks() 
    {
        // fill the entire map with "not used" 
        Arrays.fill(m_map_, 0xff);
    
        // mark each block that _is_ used with 0
        for (int i = 0; i < m_indexLength_; ++ i) {
            m_map_[Math.abs(m_index_[i]) >> SHIFT_] = 0;
        }
    
        // never move the all-initial-value block 0
        m_map_[0] = 0;
    }
    
    /**
     * Finds the same index block as the otherBlock
     * @param index array
     * @param indexLength size of index
     * @param otherBlock
     * @return same index block
     */
    protected static final int findSameIndexBlock(int index[], int indexLength,
                                                  int otherBlock) 
    {
        for (int block = BMP_INDEX_LENGTH_; block < indexLength; 
             block += SURROGATE_BLOCK_COUNT_) {
            if(equal_int(index, block, otherBlock, SURROGATE_BLOCK_COUNT_)) {
                return block;
            }
        }
        return indexLength;
    }
    
    // private data member ------------------------------------------------
        
    /**
     * Maximum length of the build-time data (stage 2) array.
     * The maximum length is 0x110000 + DATA_BLOCK_LENGTH + 0x400.
     * (Number of Unicode code points + one all-initial-value block +
     *  possible duplicate entries for 1024 lead surrogates.)
     */
    private static final int MAX_BUILD_TIME_DATA_LENGTH_ = 
        0x110000 + DATA_BLOCK_LENGTH + 0x400;
}
