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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import android.icu.lang.UCharacter;
import android.icu.text.UTF16;

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
public class IntTrieBuilder extends TrieBuilder
{
    // public constructor ----------------------------------------------
                
    /**
     * Copy constructor
     */
    public IntTrieBuilder(IntTrieBuilder table)
    {
        super(table);
        m_data_ = new int[m_dataCapacity_];
        System.arraycopy(table.m_data_, 0, m_data_, 0, m_dataLength_);
        m_initialValue_ = table.m_initialValue_;
        m_leadUnitValue_ = table.m_leadUnitValue_;
    }
    
    /**
     * Constructs a build table
     * @param aliasdata data to be filled into table
     * @param maxdatalength maximum data length allowed in table
     * @param initialvalue inital data value
     * @param latin1linear is latin 1 to be linear
     */
    public IntTrieBuilder(int aliasdata[], int maxdatalength, 
                          int initialvalue, int leadunitvalue, 
                          boolean latin1linear) 
    {
        super();
        if (maxdatalength < DATA_BLOCK_LENGTH || (latin1linear 
                                                  && maxdatalength < 1024)) {
            throw new IllegalArgumentException(
                                               "Argument maxdatalength is too small");
        }
            
        if (aliasdata != null) {
            m_data_ = aliasdata;
        } 
        else {
            m_data_ = new int[maxdatalength];
        }
        
        // preallocate and reset the first data block (block index 0)
        int j = DATA_BLOCK_LENGTH;
        
        if (latin1linear) {
            // preallocate and reset the first block (number 0) and Latin-1 
            // (U+0000..U+00ff) after that made sure above that 
            // maxDataLength >= 1024
            // set indexes to point to consecutive data blocks
            int i = 0;
            do {
                // do this at least for trie->index[0] even if that block is 
                // only partly used for Latin-1
                m_index_[i ++] = j;
                j += DATA_BLOCK_LENGTH;
            } while (i < (256 >> SHIFT_));
        }
        
        m_dataLength_ = j;
        // reset the initially allocated blocks to the initial value
        Arrays.fill(m_data_, 0, m_dataLength_, initialvalue);
        m_initialValue_ = initialvalue;
        m_leadUnitValue_ = leadunitvalue;
        m_dataCapacity_ = maxdatalength;
        m_isLatin1Linear_ = latin1linear;
        m_isCompacted_ = false;
    }

    // public methods -------------------------------------------------------
     
    /*public final void print()
      {
      int i = 0;
      int oldvalue = m_index_[i];
      int count = 0;
      System.out.println("index length " + m_indexLength_ 
      + " --------------------------");
      while (i < m_indexLength_) {
      if (m_index_[i] != oldvalue) {
      System.out.println("index has " + count + " counts of " 
      + Integer.toHexString(oldvalue));
      count = 0;
      oldvalue = m_index_[i];
      }
      count ++;
      i ++;
      }
      System.out.println("index has " + count + " counts of " 
      + Integer.toHexString(oldvalue));
      i = 0;
      oldvalue = m_data_[i];
      count = 0;
      System.out.println("data length " + m_dataLength_ 
      + " --------------------------");
      while (i < m_dataLength_) {
      if (m_data_[i] != oldvalue) {
      if ((oldvalue & 0xf1000000) == 0xf1000000) {
      int temp = oldvalue & 0xffffff; 
      temp += 0x320;
      oldvalue = 0xf1000000 | temp;
      }
      if ((oldvalue & 0xf2000000) == 0xf2000000) {
      int temp = oldvalue & 0xffffff; 
      temp += 0x14a;
      oldvalue = 0xf2000000 | temp;
      }
      System.out.println("data has " + count + " counts of " 
      + Integer.toHexString(oldvalue));
      count = 0;
      oldvalue = m_data_[i];
      }
      count ++;
      i ++;
      }
      if ((oldvalue & 0xf1000000) == 0xf1000000) {
      int temp = oldvalue & 0xffffff; 
      temp += 0x320;
      oldvalue = 0xf1000000 | temp;
      }
      if ((oldvalue & 0xf2000000) == 0xf2000000) {
      int temp = oldvalue & 0xffffff; 
      temp += 0x14a;
      oldvalue = 0xf2000000 | temp;
      }
      System.out.println("data has " + count + " counts of " 
      + Integer.toHexString(oldvalue));
      }
    */   
    /**
     * Gets a 32 bit data from the table data
     * @param ch codepoint which data is to be retrieved
     * @return the 32 bit data
     */
    public int getValue(int ch) 
    {
        // valid, uncompacted trie and valid c?
        if (m_isCompacted_ || ch > UCharacter.MAX_VALUE || ch < 0) {
            return 0;
        }
    
        int block = m_index_[ch >> SHIFT_];
        return m_data_[Math.abs(block) + (ch & MASK_)];
    }
    
    /**
     * Get a 32 bit data from the table data
     * @param ch  code point for which data is to be retrieved.
     * @param inBlockZero  Output parameter, inBlockZero[0] returns true if the
     *                      char maps into block zero, otherwise false.
     * @return the 32 bit data value.
     */
    public int getValue(int ch, boolean [] inBlockZero) 
    {
        // valid, uncompacted trie and valid c?
        if (m_isCompacted_ || ch > UCharacter.MAX_VALUE || ch < 0) {
            if (inBlockZero != null) {
                inBlockZero[0] = true;
            }
            return 0;
        }
    
        int block = m_index_[ch >> SHIFT_];
        if (inBlockZero != null) {
            inBlockZero[0] = (block == 0);
        }
        return m_data_[Math.abs(block) + (ch & MASK_)];
    }
    
    
    /**
     * Sets a 32 bit data in the table data
     * @param ch codepoint which data is to be set
     * @param value to set
     * @return true if the set is successful, otherwise 
     *              if the table has been compacted return false
     */
    public boolean setValue(int ch, int value) 
    {
        // valid, uncompacted trie and valid c? 
        if (m_isCompacted_ || ch > UCharacter.MAX_VALUE || ch < 0) {
            return false;
        }
    
        int block = getDataBlock(ch);
        if (block < 0) {
            return false;
        }
    
        m_data_[block + (ch & MASK_)] = value;
        return true;
    }
    
    /**
     * Serializes the build table with 32 bit data
     * @param datamanipulate builder raw fold method implementation
     * @param triedatamanipulate result trie fold method
     * @return a new trie
     */
    public IntTrie serialize(TrieBuilder.DataManipulate datamanipulate, 
                             Trie.DataManipulate triedatamanipulate)
    {
        if (datamanipulate == null) {
            throw new IllegalArgumentException("Parameters can not be null");
        }
        // fold and compact if necessary, also checks that indexLength is 
        // within limits 
        if (!m_isCompacted_) {
            // compact once without overlap to improve folding
            compact(false);
            // fold the supplementary part of the index array
            fold(datamanipulate);
            // compact again with overlap for minimum data array length
            compact(true);
            m_isCompacted_ = true;
        }
        // is dataLength within limits? 
        if (m_dataLength_ >= MAX_DATA_LENGTH_) {
            throw new ArrayIndexOutOfBoundsException("Data length too small");
        }
    
        char index[] = new char[m_indexLength_];
        int data[] = new int[m_dataLength_];
        // write the index (stage 1) array and the 32-bit data (stage 2) array
        // write 16-bit index values shifted right by INDEX_SHIFT_ 
        for (int i = 0; i < m_indexLength_; i ++) {
            index[i] = (char)(m_index_[i] >>> INDEX_SHIFT_);
        }
        // write 32-bit data values
        System.arraycopy(m_data_, 0, data, 0, m_dataLength_);
        
        int options = SHIFT_ | (INDEX_SHIFT_ << OPTIONS_INDEX_SHIFT_);
        options |= OPTIONS_DATA_IS_32_BIT_;
        if (m_isLatin1Linear_) {
            options |= OPTIONS_LATIN1_IS_LINEAR_;
        }
        return new IntTrie(index, data, m_initialValue_, options, 
                           triedatamanipulate);
    }
    
    
    /**
     * Serializes the build table to an output stream.
     * 
     * Compacts the build-time trie after all values are set, and then
     * writes the serialized form onto an output stream.
     * 
     * After this, this build-time Trie can only be serialized again and/or closed;
     * no further values can be added.
     * 
     * This function is the rough equivalent of utrie_seriaize() in ICU4C.
     * 
     * @param os the output stream to which the seriaized trie will be written.
     *         If nul, the function still returns the size of the serialized Trie.
     * @param reduceTo16Bits If true, reduce the data size to 16 bits.  The resulting
     *         serialized form can then be used to create a CharTrie.
     * @param datamanipulate builder raw fold method implementation
     * @return the number of bytes written to the output stream.
     */
     public int serialize(OutputStream os, boolean reduceTo16Bits,
            TrieBuilder.DataManipulate datamanipulate)  throws IOException {
         if (datamanipulate == null) {
             throw new IllegalArgumentException("Parameters can not be null");
         }

         // fold and compact if necessary, also checks that indexLength is 
         // within limits 
         if (!m_isCompacted_) {
             // compact once without overlap to improve folding
             compact(false);
             // fold the supplementary part of the index array
             fold(datamanipulate);
             // compact again with overlap for minimum data array length
             compact(true);
             m_isCompacted_ = true;
         }
         
         // is dataLength within limits? 
         int length;
         if (reduceTo16Bits) {
             length = m_dataLength_ + m_indexLength_;
         } else {
             length = m_dataLength_;
         }
         if (length >= MAX_DATA_LENGTH_) {
             throw new ArrayIndexOutOfBoundsException("Data length too small");
         }
         
         //  struct UTrieHeader {
         //      int32_t   signature;
         //      int32_t   options  (a bit field)
         //      int32_t   indexLength
         //      int32_t   dataLength
         length = Trie.HEADER_LENGTH_ + 2*m_indexLength_;
         if(reduceTo16Bits) {
             length+=2*m_dataLength_;
         } else {
             length+=4*m_dataLength_;
         }
         
         if (os == null) {
             // No output stream.  Just return the length of the serialized Trie, in bytes.
             return length;
         }

         DataOutputStream dos = new DataOutputStream(os);
         dos.writeInt(Trie.HEADER_SIGNATURE_);  
         
         int options = Trie.INDEX_STAGE_1_SHIFT_ | (Trie.INDEX_STAGE_2_SHIFT_<<Trie.HEADER_OPTIONS_INDEX_SHIFT_);
         if(!reduceTo16Bits) {
             options |= Trie.HEADER_OPTIONS_DATA_IS_32_BIT_;
         }
         if(m_isLatin1Linear_) {
             options |= Trie.HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_;
         }
         dos.writeInt(options);
         
         dos.writeInt(m_indexLength_);
         dos.writeInt(m_dataLength_);
         
         /* write the index (stage 1) array and the 16/32-bit data (stage 2) array */
         if(reduceTo16Bits) {
             /* write 16-bit index values shifted right by UTRIE_INDEX_SHIFT, after adding indexLength */
             for (int i=0; i<m_indexLength_; i++) {
                 int v = (m_index_[i] + m_indexLength_) >>> Trie.INDEX_STAGE_2_SHIFT_;
                 dos.writeChar(v);
             }
             
             /* write 16-bit data values */
             for(int i=0; i<m_dataLength_; i++) {
                 int v = m_data_[i] & 0x0000ffff;
                 dos.writeChar(v);
             }
         } else {
             /* write 16-bit index values shifted right by UTRIE_INDEX_SHIFT */
             for (int i=0; i<m_indexLength_; i++) {
                 int v = (m_index_[i]) >>> Trie.INDEX_STAGE_2_SHIFT_;
                 dos.writeChar(v);
             }
             
             /* write 32-bit data values */
             for(int i=0; i<m_dataLength_; i++) {
                 dos.writeInt(m_data_[i]);
             }
        }

         return length;

    }
    
    
    /**
     * Set a value in a range of code points [start..limit].
     * All code points c with start &lt;= c &lt; limit will get the value if
     * overwrite is true or if the old value is 0.
     * @param start the first code point to get the value
     * @param limit one past the last code point to get the value
     * @param value the value
     * @param overwrite flag for whether old non-initial values are to be 
     *        overwritten
     * @return false if a failure occurred (illegal argument or data array 
     *               overrun)
     */
    public boolean setRange(int start, int limit, int value, 
                            boolean overwrite) 
    {
        // repeat value in [start..limit[
        // mark index values for repeat-data blocks by setting bit 31 of the 
        // index values fill around existing values if any, if(overwrite)
            
        // valid, uncompacted trie and valid indexes?
        if (m_isCompacted_ || start < UCharacter.MIN_VALUE 
            || start > UCharacter.MAX_VALUE || limit < UCharacter.MIN_VALUE
            || limit > (UCharacter.MAX_VALUE + 1) || start > limit) {
            return false;
        }
            
        if (start == limit) {
            return true; // nothing to do
        }
        
        if ((start & MASK_) != 0) {
            // set partial block at [start..following block boundary[
            int block = getDataBlock(start);
            if (block < 0) {
                return false;
            }
        
            int nextStart = (start + DATA_BLOCK_LENGTH) & ~MASK_;
            if (nextStart <= limit) {
                fillBlock(block, start & MASK_, DATA_BLOCK_LENGTH,
                          value, overwrite);
                start = nextStart;
            } 
            else {
                fillBlock(block, start & MASK_, limit & MASK_,
                          value, overwrite);
                return true;
            }
        }
        
        // number of positions in the last, partial block
        int rest = limit & MASK_;
        
        // round down limit to a block boundary 
        limit &= ~MASK_;
        
        // iterate over all-value blocks 
        int repeatBlock = 0;
        if (value == m_initialValue_) {
            // repeatBlock = 0; assigned above
        } 
        else {
            repeatBlock = -1;
        }
        while (start < limit) {
            // get index value 
            int block = m_index_[start >> SHIFT_];
            if (block > 0) {
                // already allocated, fill in value
                fillBlock(block, 0, DATA_BLOCK_LENGTH, value, overwrite);
            } 
            else if (m_data_[-block] != value && (block == 0 || overwrite)) {
                // set the repeatBlock instead of the current block 0 or range 
                // block 
                if (repeatBlock >= 0) {
                    m_index_[start >> SHIFT_] = -repeatBlock;
                } 
                else {
                    // create and set and fill the repeatBlock
                    repeatBlock = getDataBlock(start);
                    if (repeatBlock < 0) {
                        return false;
                    }
        
                    // set the negative block number to indicate that it is a 
                    // repeat block
                    m_index_[start >> SHIFT_] = -repeatBlock;
                    fillBlock(repeatBlock, 0, DATA_BLOCK_LENGTH, value, true);
                }
            }
        
            start += DATA_BLOCK_LENGTH;
        }
        
        if (rest > 0) {
            // set partial block at [last block boundary..limit[
            int block = getDataBlock(start);
            if (block < 0) {
                return false;
            }
            fillBlock(block, 0, rest, value, overwrite);
        }
        
        return true;
    }
    
    // protected data member ------------------------------------------------
                
    protected int m_data_[];
    protected int m_initialValue_;  
    
    //  private data member ------------------------------------------------
        
    private int m_leadUnitValue_;  
     
    // private methods ------------------------------------------------------
   
    private int allocDataBlock() 
    {
        int newBlock = m_dataLength_;
        int newTop = newBlock + DATA_BLOCK_LENGTH;
        if (newTop > m_dataCapacity_) {
            // out of memory in the data array
            return -1;
        }
        m_dataLength_ = newTop;
        return newBlock;
    }

    /**
     * No error checking for illegal arguments.
     * @param ch codepoint to look for
     * @return -1 if no new data block available (out of memory in data array)
     */
    private int getDataBlock(int ch) 
    {
        ch >>= SHIFT_;
        int indexValue = m_index_[ch];
        if (indexValue > 0) {
            return indexValue;
        }
    
        // allocate a new data block
        int newBlock = allocDataBlock();
        if (newBlock < 0) {
            // out of memory in the data array 
            return -1;
        }
        m_index_[ch] = newBlock;
    
        // copy-on-write for a block from a setRange()
        System.arraycopy(m_data_, Math.abs(indexValue), m_data_, newBlock,  
                         DATA_BLOCK_LENGTH << 2);
        return newBlock;
    }
    
    /**
     * Compact a folded build-time trie.
     * The compaction
     * - removes blocks that are identical with earlier ones
     * - overlaps adjacent blocks as much as possible (if overlap == true)
     * - moves blocks in steps of the data granularity
     * - moves and overlaps blocks that overlap with multiple values in the overlap region
     *
     * It does not
     * - try to move and overlap blocks that are not already adjacent
     * @param overlap flag
     */
    private void compact(boolean overlap) 
    {
        if (m_isCompacted_) {
            return; // nothing left to do
        }
    
        // compaction
        // initialize the index map with "block is used/unused" flags
        findUnusedBlocks();
        
        // if Latin-1 is preallocated and linear, then do not compact Latin-1 
        // data
        int overlapStart = DATA_BLOCK_LENGTH;
        if (m_isLatin1Linear_ && SHIFT_ <= 8) {
            overlapStart += 256;
        }
       
        int newStart = DATA_BLOCK_LENGTH;
        int i;
        for (int start = newStart; start < m_dataLength_;) {
            // start: index of first entry of current block
            // newStart: index where the current block is to be moved
            //           (right after current end of already-compacted data)
            // skip blocks that are not used 
            if (m_map_[start >>> SHIFT_] < 0) {
                // advance start to the next block 
                start += DATA_BLOCK_LENGTH;
                // leave newStart with the previous block!
                continue;
            }
            // search for an identical block
            if (start >= overlapStart) {
                i = findSameDataBlock(m_data_, newStart, start,
                                          overlap ? DATA_GRANULARITY_ : DATA_BLOCK_LENGTH);
                if (i >= 0) {
                    // found an identical block, set the other block's index 
                    // value for the current block
                    m_map_[start >>> SHIFT_] = i;
                    // advance start to the next block
                    start += DATA_BLOCK_LENGTH;
                    // leave newStart with the previous block!
                    continue;
                }
            }
            // see if the beginning of this block can be overlapped with the 
            // end of the previous block
            if(overlap && start>=overlapStart) {
                /* look for maximum overlap (modulo granularity) with the previous, adjacent block */
                for(i=DATA_BLOCK_LENGTH-DATA_GRANULARITY_;
                    i>0 && !equal_int(m_data_, newStart-i, start, i);
                    i-=DATA_GRANULARITY_) {}
            } else {
                i=0;
            }
            if (i > 0) {
                // some overlap
                m_map_[start >>> SHIFT_] = newStart - i;
                // move the non-overlapping indexes to their new positions
                start += i;
                for (i = DATA_BLOCK_LENGTH - i; i > 0; -- i) {
                    m_data_[newStart ++] = m_data_[start ++];
                }
            } 
            else if (newStart < start) {
                // no overlap, just move the indexes to their new positions
                m_map_[start >>> SHIFT_] = newStart;
                for (i = DATA_BLOCK_LENGTH; i > 0; -- i) {
                    m_data_[newStart ++] = m_data_[start ++];
                }
            } 
            else { // no overlap && newStart==start
                m_map_[start >>> SHIFT_] = start;
                newStart += DATA_BLOCK_LENGTH;
                start = newStart;
            }
        }
        // now adjust the index (stage 1) table
        for (i = 0; i < m_indexLength_; ++ i) {
            m_index_[i] = m_map_[Math.abs(m_index_[i]) >>> SHIFT_];
        }
        m_dataLength_ = newStart;
    }

    /**
     * Find the same data block
     * @param data array
     * @param dataLength
     * @param otherBlock
     * @param step
     */
    private static final int findSameDataBlock(int data[], int dataLength,
                                               int otherBlock, int step) 
    {
        // ensure that we do not even partially get past dataLength
        dataLength -= DATA_BLOCK_LENGTH;

        for (int block = 0; block <= dataLength; block += step) {
            if(equal_int(data, block, otherBlock, DATA_BLOCK_LENGTH)) {
                return block;
            }
        }
        return -1;
    }
    
    /**
     * Fold the normalization data for supplementary code points into
     * a compact area on top of the BMP-part of the trie index,
     * with the lead surrogates indexing this compact area.
     *
     * Duplicate the index values for lead surrogates:
     * From inside the BMP area, where some may be overridden with folded values,
     * to just after the BMP area, where they can be retrieved for
     * code point lookups.
     * @param manipulate fold implementation
     */
    private final void fold(DataManipulate manipulate) 
    {
        int leadIndexes[] = new int[SURROGATE_BLOCK_COUNT_];
        int index[] = m_index_;
        // copy the lead surrogate indexes into a temporary array
        System.arraycopy(index, 0xd800 >> SHIFT_, leadIndexes, 0, 
                         SURROGATE_BLOCK_COUNT_);
        
        // set all values for lead surrogate code *units* to leadUnitValue
        // so that by default runtime lookups will find no data for associated
        // supplementary code points, unless there is data for such code points
        // which will result in a non-zero folding value below that is set for
        // the respective lead units
        // the above saved the indexes for surrogate code *points*
        // fill the indexes with simplified code from utrie_setRange32()
        int block = 0;
        if (m_leadUnitValue_ == m_initialValue_) {
            // leadUnitValue == initialValue, use all-initial-value block
            // block = 0; if block here left empty
        } 
        else {
            // create and fill the repeatBlock
            block = allocDataBlock();
            if (block < 0) {
                // data table overflow
                throw new IllegalStateException("Internal error: Out of memory space");
            }
            fillBlock(block, 0, DATA_BLOCK_LENGTH, m_leadUnitValue_, true);
            // negative block number to indicate that it is a repeat block
            block = -block; 
        }
        for (int c = (0xd800 >> SHIFT_); c < (0xdc00 >> SHIFT_); ++ c) {
            m_index_[c] = block;
        }

        // Fold significant index values into the area just after the BMP 
        // indexes.
        // In case the first lead surrogate has significant data,
        // its index block must be used first (in which case the folding is a 
        // no-op).
        // Later all folded index blocks are moved up one to insert the copied
        // lead surrogate indexes.
        int indexLength = BMP_INDEX_LENGTH_;
        // search for any index (stage 1) entries for supplementary code points 
        for (int c = 0x10000; c < 0x110000;) {
            if (index[c >> SHIFT_] != 0) {
                // there is data, treat the full block for a lead surrogate
                c &= ~0x3ff;
                // is there an identical index block?
                block = findSameIndexBlock(index, indexLength, c >> SHIFT_);
                
                // get a folded value for [c..c+0x400[ and,
                // if different from the value for the lead surrogate code 
                // point, set it for the lead surrogate code unit

                int value = manipulate.getFoldedValue(c, 
                                                      block + SURROGATE_BLOCK_COUNT_);
                if (value != getValue(UTF16.getLeadSurrogate(c))) {
                    if (!setValue(UTF16.getLeadSurrogate(c), value)) {
                        // data table overflow 
                        throw new ArrayIndexOutOfBoundsException(
                                                                 "Data table overflow");
                    }
                    // if we did not find an identical index block...
                    if (block == indexLength) {
                        // move the actual index (stage 1) entries from the 
                        // supplementary position to the new one
                        System.arraycopy(index, c >> SHIFT_, index, indexLength,
                                         SURROGATE_BLOCK_COUNT_);
                        indexLength += SURROGATE_BLOCK_COUNT_;
                    }
                }
                c += 0x400;
            } 
            else {
                c += DATA_BLOCK_LENGTH;
            }
        }
        
        // index array overflow?
        // This is to guarantee that a folding offset is of the form
        // UTRIE_BMP_INDEX_LENGTH+n*UTRIE_SURROGATE_BLOCK_COUNT with n=0..1023.
        // If the index is too large, then n>=1024 and more than 10 bits are 
        // necessary.
        // In fact, it can only ever become n==1024 with completely unfoldable 
        // data and the additional block of duplicated values for lead 
        // surrogates.
        if (indexLength >= MAX_INDEX_LENGTH_) {
            throw new ArrayIndexOutOfBoundsException("Index table overflow");
        }
        // make space for the lead surrogate index block and insert it between 
        // the BMP indexes and the folded ones
        System.arraycopy(index, BMP_INDEX_LENGTH_, index, 
                         BMP_INDEX_LENGTH_ + SURROGATE_BLOCK_COUNT_,
                         indexLength - BMP_INDEX_LENGTH_);
        System.arraycopy(leadIndexes, 0, index, BMP_INDEX_LENGTH_,
                         SURROGATE_BLOCK_COUNT_);
        indexLength += SURROGATE_BLOCK_COUNT_;
        m_indexLength_ = indexLength;
    }
    
    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    private void fillBlock(int block, int start, int limit, int value, 
                           boolean overwrite) 
    {
        limit += block;
        block += start;
        if (overwrite) {
            while (block < limit) {
                m_data_[block ++] = value;
            }
        } 
        else {
            while (block < limit) {
                if (m_data_[block] == m_initialValue_) {
                    m_data_[block] = value;
                }
                ++ block;
            }
        }
    }
}

