/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.impl;

/**
 * @author aheninger
 *
 * A Trie2Writable is a modifiable, or build-time Trie2.
 * Functions for reading data from the Trie are all from class Trie2.
 * @hide Only a subset of ICU is exposed in Android
 * 
 */
public class Trie2Writable extends Trie2 {
    
    
    /**
     * Create a new, empty, writable Trie2. 32-bit data values are used.
     *
     * @param initialValueP the initial value that is set for all code points
     * @param errorValueP the value for out-of-range code points and illegal UTF-8
     */
    public  Trie2Writable(int initialValueP, int errorValueP) {       
        // This constructor corresponds to utrie2_open() in ICU4C.
        init(initialValueP, errorValueP);
    }
    
    
    private void init(int initialValueP, int errorValueP) { 
        this.initialValue = initialValueP;
        this.errorValue   = errorValueP;
        this.highStart    = 0x110000;

        this.data           = new int[UNEWTRIE2_INITIAL_DATA_LENGTH];
        this.dataCapacity   = UNEWTRIE2_INITIAL_DATA_LENGTH;
        this.initialValue   = initialValueP;
        this.errorValue     = errorValueP;
        this.highStart      = 0x110000;
        this.firstFreeBlock = 0;  /* no free block in the list */
        this.isCompacted    = false;

        /*
         * preallocate and reset
         * - ASCII
         * - the bad-UTF-8-data block
         * - the null data block
         */
        int i, j;
        for(i=0; i<0x80; ++i) {
            data[i] = initialValue;
        }
        for(; i<0xc0; ++i) {
            data[i] = errorValue;
        }
        for(i=UNEWTRIE2_DATA_NULL_OFFSET; i<UNEWTRIE2_DATA_START_OFFSET; ++i) {
            data[i] = initialValue;
        }
        dataNullOffset = UNEWTRIE2_DATA_NULL_OFFSET;
        dataLength     = UNEWTRIE2_DATA_START_OFFSET;

        /* set the index-2 indexes for the 2=0x80>>UTRIE2_SHIFT_2 ASCII data blocks */
        for(i=0, j=0; j<0x80; ++i, j+=UTRIE2_DATA_BLOCK_LENGTH) {
            index2[i]=j;
            map[i]=1;
        }
        
        /* reference counts for the bad-UTF-8-data block */
        for(; j<0xc0; ++i, j+=UTRIE2_DATA_BLOCK_LENGTH) {
            map[i]=0;
        }
        
        /*
         * Reference counts for the null data block: all blocks except for the ASCII blocks.
         * Plus 1 so that we don't drop this block during compaction.
         * Plus as many as needed for lead surrogate code points.
         */
        /* i==newTrie->dataNullOffset */
        map[i++] =
            (0x110000>>UTRIE2_SHIFT_2) -
            (0x80>>UTRIE2_SHIFT_2) +
            1 +
            UTRIE2_LSCP_INDEX_2_LENGTH;
        j += UTRIE2_DATA_BLOCK_LENGTH;
        for(; j<UNEWTRIE2_DATA_START_OFFSET; ++i, j+=UTRIE2_DATA_BLOCK_LENGTH) {
            map[i]=0;
        }

        /*
         * set the remaining indexes in the BMP index-2 block
         * to the null data block
         */
        for(i=0x80>>UTRIE2_SHIFT_2; i<UTRIE2_INDEX_2_BMP_LENGTH; ++i) {
            index2[i]=UNEWTRIE2_DATA_NULL_OFFSET;
        }

        /*
         * Fill the index gap with impossible values so that compaction
         * does not overlap other index-2 blocks with the gap.
         */
        for(i=0; i<UNEWTRIE2_INDEX_GAP_LENGTH; ++i) {
            index2[UNEWTRIE2_INDEX_GAP_OFFSET+i]=-1;
        }

        /* set the indexes in the null index-2 block */
        for(i=0; i<UTRIE2_INDEX_2_BLOCK_LENGTH; ++i) {
            index2[UNEWTRIE2_INDEX_2_NULL_OFFSET+i]=UNEWTRIE2_DATA_NULL_OFFSET;
        }
        index2NullOffset=UNEWTRIE2_INDEX_2_NULL_OFFSET;
        index2Length=UNEWTRIE2_INDEX_2_START_OFFSET;

        /* set the index-1 indexes for the linear index-2 block */
        for(i=0, j=0;
            i<UTRIE2_OMITTED_BMP_INDEX_1_LENGTH;
            ++i, j+=UTRIE2_INDEX_2_BLOCK_LENGTH
        ) {
            index1[i]=j;
        }

        /* set the remaining index-1 indexes to the null index-2 block */
        for(; i<UNEWTRIE2_INDEX_1_LENGTH; ++i) {
            index1[i]=UNEWTRIE2_INDEX_2_NULL_OFFSET;
        }

        /*
         * Preallocate and reset data for U+0080..U+07ff,
         * for 2-byte UTF-8 which will be compacted in 64-blocks
         * even if UTRIE2_DATA_BLOCK_LENGTH is smaller.
         */
        for(i=0x80; i<0x800; i+=UTRIE2_DATA_BLOCK_LENGTH) {
            set(i, initialValue);
        }

    }
    
    
    /**
     * Create a new build time (modifiable) Trie2 whose contents are the same as the source Trie2.
     * 
     * @param source the source Trie2.  Its contents will be copied into the new Trie2.
     */
    public Trie2Writable(Trie2 source) {
        init(source.initialValue, source.errorValue);
        
        for (Range r: source) {
            setRange(r, true);
        }
    }
    
    
    private boolean isInNullBlock(int c, boolean forLSCP) {
        int i2, block;

        if(Character.isHighSurrogate((char)c) && forLSCP) {
            i2=(UTRIE2_LSCP_INDEX_2_OFFSET-(0xd800>>UTRIE2_SHIFT_2))+
                (c>>UTRIE2_SHIFT_2);
        } else {
            i2=index1[c>>UTRIE2_SHIFT_1]+
                ((c>>UTRIE2_SHIFT_2)&UTRIE2_INDEX_2_MASK);
        }
        block=index2[i2];
        return (block==dataNullOffset);
    }

    private int allocIndex2Block() {
        int newBlock, newTop;

        newBlock=index2Length;
        newTop=newBlock+UTRIE2_INDEX_2_BLOCK_LENGTH;
        if(newTop > index2.length) {
            throw new IllegalStateException("Internal error in Trie2 creation.");
            /*
             * Should never occur.
             * Either UTRIE2_MAX_BUILD_TIME_INDEX_LENGTH is incorrect,
             * or the code writes more values than should be possible.
             */
        }
        index2Length=newTop;
        System.arraycopy(index2, index2NullOffset, index2, newBlock, UTRIE2_INDEX_2_BLOCK_LENGTH);
        return newBlock;
    }

    private int getIndex2Block(int c, boolean forLSCP) {
        int i1, i2;

        if(c>=0xd800 && c<0xdc00 && forLSCP) {
            return UTRIE2_LSCP_INDEX_2_OFFSET;
        }

        i1=c>>UTRIE2_SHIFT_1;
        i2=index1[i1];
        if(i2==index2NullOffset) {
            i2=allocIndex2Block();
            index1[i1]=i2;
        }
        return i2;
    }

    private int allocDataBlock(int copyBlock) {
        int newBlock, newTop;

        if(firstFreeBlock!=0) {
            /* get the first free block */
            newBlock=firstFreeBlock;
            firstFreeBlock=-map[newBlock>>UTRIE2_SHIFT_2];
        } else {
            /* get a new block from the high end */
            newBlock=dataLength;
            newTop=newBlock+UTRIE2_DATA_BLOCK_LENGTH;
            if(newTop>dataCapacity) {
                /* out of memory in the data array */
                int capacity;
                int[] newData;

                if(dataCapacity<UNEWTRIE2_MEDIUM_DATA_LENGTH) {
                    capacity=UNEWTRIE2_MEDIUM_DATA_LENGTH;
                } else if(dataCapacity<UNEWTRIE2_MAX_DATA_LENGTH) {
                    capacity=UNEWTRIE2_MAX_DATA_LENGTH;
                } else {
                    /*
                     * Should never occur.
                     * Either UNEWTRIE2_MAX_DATA_LENGTH is incorrect,
                     * or the code writes more values than should be possible.
                     */
                    throw new IllegalStateException("Internal error in Trie2 creation.");
                }
                newData = new int[capacity];
                System.arraycopy(data, 0, newData, 0, dataLength);
                data=newData;
                dataCapacity=capacity;
            }
            dataLength=newTop;
        }
        System.arraycopy(data, copyBlock, data, newBlock, UTRIE2_DATA_BLOCK_LENGTH);
        map[newBlock>>UTRIE2_SHIFT_2]=0;
        return newBlock;
    }

    
    /* call when the block's reference counter reaches 0 */
    private void releaseDataBlock(int block) {
        /* put this block at the front of the free-block chain */
        map[block>>UTRIE2_SHIFT_2]=-firstFreeBlock;
        firstFreeBlock=block;
    }

    
    private boolean isWritableBlock(int block) {
        return (block!=dataNullOffset && 1==map[block>>UTRIE2_SHIFT_2]);
    }

    private void setIndex2Entry(int i2, int block) {
        int oldBlock;
        ++map[block>>UTRIE2_SHIFT_2];  /* increment first, in case block==oldBlock! */
        oldBlock=index2[i2];
        if(0 == --map[oldBlock>>UTRIE2_SHIFT_2]) {
            releaseDataBlock(oldBlock);
        }
        index2[i2]=block;
    }

    
    /**
     * No error checking for illegal arguments.
     * 
     * @hide draft / provisional / internal are hidden on Android
     */
    private int getDataBlock(int c, boolean forLSCP) {
        int i2, oldBlock, newBlock;

        i2=getIndex2Block(c, forLSCP);
        
        i2+=(c>>UTRIE2_SHIFT_2)&UTRIE2_INDEX_2_MASK;
        oldBlock=index2[i2];
        if(isWritableBlock(oldBlock)) {
            return oldBlock;
        }

        /* allocate a new data block */
        newBlock=allocDataBlock(oldBlock);
        setIndex2Entry(i2, newBlock);
        return newBlock;
    }
    /**
     * Set a value for a code point.
     *
     * @param c the code point
     * @param value the value
     */
    public Trie2Writable set(int c, int value) {
        if (c<0 || c>0x10ffff) {
            throw new IllegalArgumentException("Invalid code point.");
        }
        set(c, true, value);
        fHash = 0;
        return this;
    }
    
    private Trie2Writable set(int c, boolean forLSCP, int value) {
        int block;
        if (isCompacted) {
            uncompact();
        }
        block = getDataBlock(c, forLSCP);
        data[block + (c&UTRIE2_DATA_MASK)] = value;
        return this;
    }
    
    
    /*
     * Uncompact a compacted Trie2Writable.
     * This is needed if a the WritableTrie2 was compacted in preparation for creating a read-only
     * Trie2, and then is subsequently altered.
     * 
     * The structure is a bit awkward - it would be cleaner to leave the original
     * Trie2 unaltered - but compacting in place was taken directly from the ICU4C code.
     * 
     * The approach is to create a new (uncompacted) Trie2Writable from this one, then transfer
     * the guts from the new to the old.
     */
    private void uncompact() {
        Trie2Writable tempTrie = new Trie2Writable(this);
        
        // Members from Trie2Writable
        this.index1       = tempTrie.index1;
        this.index2       = tempTrie.index2;
        this.data         = tempTrie.data;
        this.index2Length = tempTrie.index2Length;
        this.dataCapacity = tempTrie.dataCapacity;
        this.isCompacted  = tempTrie.isCompacted;
        
        // Members From Trie2
        this.header           = tempTrie.header;
        this.index            = tempTrie.index;
        this.data16           = tempTrie.data16;
        this.data32           = tempTrie.data32;
        this.indexLength      = tempTrie.indexLength;
        this.dataLength       = tempTrie.dataLength;
        this.index2NullOffset = tempTrie.index2NullOffset;
        this.initialValue     = tempTrie.initialValue;        
        this.errorValue       = tempTrie.errorValue;        
        this.highStart        = tempTrie.highStart;        
        this.highValueIndex   = tempTrie.highValueIndex;        
        this.dataNullOffset   = tempTrie.dataNullOffset;        
    }
    
    
    private void writeBlock(int  block, int value) {
        int  limit=block+UTRIE2_DATA_BLOCK_LENGTH;
        while(block<limit) {
            data[block++]=value;
        }
    }

    /**
     * initialValue is ignored if overwrite=TRUE
     * @hide draft / provisional / internal are hidden on Android
     */
    private void fillBlock(int block, /*UChar32*/ int start, /*UChar32*/ int limit,
              int value, int initialValue, boolean overwrite) {
        int i;
        int pLimit = block+limit;
        if(overwrite) {
            for (i=block+start; i<pLimit; i++) {
                data[i] = value;
            }
        } else {
            for (i=block+start; i<pLimit; i++) {
                if(data[i]==initialValue) {
                    data[i]=value;
                }
            }
        }
    }

    /**
     * Set a value in a range of code points [start..end].
     * All code points c with start<=c<=end will get the value if
     * overwrite is TRUE or if the old value is the initial value.
     *
     * @param start the first code point to get the value
     * @param end the last code point to get the value (inclusive)
     * @param value the value
     * @param overwrite flag for whether old non-initial values are to be overwritten
     */
     public Trie2Writable setRange(int start, int end,
                           int value, boolean overwrite) {
         /*
          * repeat value in [start..end]
          * mark index values for repeat-data blocks by setting bit 31 of the index values
          * fill around existing values if any, if(overwrite)
          */
         int block, rest, repeatBlock;
         int /*UChar32*/ limit;

         if(start>0x10ffff || start<0 || end>0x10ffff || end<0 || start>end) {
             throw new IllegalArgumentException("Invalid code point range.");
         }
         if(!overwrite && value==initialValue) {
             return this; /* nothing to do */
         }
         fHash = 0;
         if(isCompacted) {
             this.uncompact();
         }

         limit=end+1;
         if((start&UTRIE2_DATA_MASK) != 0) {
             int  /*UChar32*/ nextStart;

             /* set partial block at [start..following block boundary[ */
             block=getDataBlock(start, true);

             nextStart=(start+UTRIE2_DATA_BLOCK_LENGTH)&~UTRIE2_DATA_MASK;
             if(nextStart<=limit) {
                 fillBlock(block, start&UTRIE2_DATA_MASK, UTRIE2_DATA_BLOCK_LENGTH,
                           value, initialValue, overwrite);
                 start=nextStart;
             } else {
                 fillBlock(block, start&UTRIE2_DATA_MASK, limit&UTRIE2_DATA_MASK,
                           value, initialValue, overwrite);
                 return this;
             }
         }

         /* number of positions in the last, partial block */
         rest=limit&UTRIE2_DATA_MASK;

         /* round down limit to a block boundary */
         limit&=~UTRIE2_DATA_MASK;

         /* iterate over all-value blocks */
         if(value==initialValue) {
             repeatBlock=dataNullOffset;
         } else {
             repeatBlock=-1;
         }

         while(start<limit) {
             int i2;
             boolean setRepeatBlock=false;

             if(value==initialValue && isInNullBlock(start, true)) {
                 start+=UTRIE2_DATA_BLOCK_LENGTH; /* nothing to do */
                 continue;
             }

             /* get index value */
             i2=getIndex2Block(start, true);
             i2+=(start>>UTRIE2_SHIFT_2)&UTRIE2_INDEX_2_MASK;
             block=index2[i2];
             if(isWritableBlock(block)) {
                 /* already allocated */
                 if(overwrite && block>=UNEWTRIE2_DATA_0800_OFFSET) {
                     /*
                      * We overwrite all values, and it's not a
                      * protected (ASCII-linear or 2-byte UTF-8) block:
                      * replace with the repeatBlock.
                      */
                     setRepeatBlock=true;
                 } else {
                     /* !overwrite, or protected block: just write the values into this block */
                     fillBlock(block,
                               0, UTRIE2_DATA_BLOCK_LENGTH,
                               value, initialValue, overwrite);
                 }
             } else if(data[block]!=value && (overwrite || block==dataNullOffset)) {
                 /*
                  * Set the repeatBlock instead of the null block or previous repeat block:
                  *
                  * If !isWritableBlock() then all entries in the block have the same value
                  * because it's the null block or a range block (the repeatBlock from a previous
                  * call to utrie2_setRange32()).
                  * No other blocks are used multiple times before compacting.
                  *
                  * The null block is the only non-writable block with the initialValue because
                  * of the repeatBlock initialization above. (If value==initialValue, then
                  * the repeatBlock will be the null data block.)
                  *
                  * We set our repeatBlock if the desired value differs from the block's value,
                  * and if we overwrite any data or if the data is all initial values
                  * (which is the same as the block being the null block, see above).
                  */
                 setRepeatBlock=true;
             }
             if(setRepeatBlock) {
                 if(repeatBlock>=0) {
                     setIndex2Entry(i2, repeatBlock);
                 } else {
                     /* create and set and fill the repeatBlock */
                     repeatBlock=getDataBlock(start, true);
                     writeBlock(repeatBlock, value);
                 }
             }

             start+=UTRIE2_DATA_BLOCK_LENGTH;
         }

         if(rest>0) {
             /* set partial block at [last block boundary..limit[ */
             block=getDataBlock(start, true);
             fillBlock(block, 0, rest, value, initialValue, overwrite);
         }

         return this;
     }    
     
     /**
      * Set the values from a Trie2.Range.
      * 
      * All code points within the range will get the value if
      * overwrite is TRUE or if the old value is the initial value.
      *
      * Ranges with the lead surrogate flag set will set the alternate
      * lead-surrogate values in the Trie, rather than the code point values.
      * 
      * This function is intended to work with the ranges produced when iterating
      * the contents of a source Trie.
      * 
      * @param range contains the range of code points and the value to be set.
      * @param overwrite flag for whether old non-initial values are to be overwritten
      */
      public Trie2Writable setRange(Trie2.Range range, boolean overwrite) {
          fHash = 0;
          if (range.leadSurrogate) {
              for (int c=range.startCodePoint; c<=range.endCodePoint; c++) {
                  if (overwrite || getFromU16SingleLead((char)c) == this.initialValue)  {
                      setForLeadSurrogateCodeUnit((char)c, range.value); 
                  }
              }
          } else {
              setRange(range.startCodePoint, range.endCodePoint, range.value, overwrite);
          }
          return this;
      }
     
     /**
      * Set a value for a UTF-16 code unit.
      * Note that a Trie2 stores separate values for 
      * supplementary code points in the lead surrogate range
      * (accessed via the plain set() and get() interfaces)
      * and for lead surrogate code units.
      * 
      * The lead surrogate code unit values are set via this function and
      * read by the function getFromU16SingleLead().
      * 
      * For code units outside of the lead surrogate range, this function
      * behaves identically to set().
      * 
      * @param codeUnit A UTF-16 code unit. 
      * @param value the value to be stored in the Trie2.
      */
     public Trie2Writable setForLeadSurrogateCodeUnit(char codeUnit, int value) {
         fHash = 0;
         set(codeUnit, false, value);
         return this;
     }

     
     /**
      * Get the value for a code point as stored in the Trie2.
      *
      * @param codePoint the code point
      * @return the value
      */
    @Override
    public int get(int codePoint) {
        if (codePoint<0 || codePoint>0x10ffff) {
            return errorValue;
        } else {
            return get(codePoint, true);
        }
    }

    
    private int get(int c, boolean fromLSCP) {
        int i2, block;

        if(c>=highStart && (!(c>=0xd800 && c<0xdc00) || fromLSCP)) {
            return data[dataLength-UTRIE2_DATA_GRANULARITY];
        }

        if((c>=0xd800 && c<0xdc00) && fromLSCP) {
            i2=(UTRIE2_LSCP_INDEX_2_OFFSET-(0xd800>>UTRIE2_SHIFT_2))+
                (c>>UTRIE2_SHIFT_2);
        } else {
            i2=index1[c>>UTRIE2_SHIFT_1]+
                ((c>>UTRIE2_SHIFT_2)&UTRIE2_INDEX_2_MASK);
        }
        block=index2[i2];
        return data[block+(c&UTRIE2_DATA_MASK)];
    }

    /**
     * Get a trie value for a UTF-16 code unit.
     * 
     * This function returns the same value as get() if the input 
     * character is outside of the lead surrogate range
     * 
     * There are two values stored in a Trie for inputs in the lead
     * surrogate range.  This function returns the alternate value,
     * while Trie2.get() returns the main value.
     * 
     * @param c the code point or lead surrogate value.
     * @return the value
     */
    @Override
    public int getFromU16SingleLead(char c) {
        return get(c, false);
    }
      
    /* compaction --------------------------------------------------------------- */

    private boolean equal_int(int[] a, int s, int t, int length) {
        for (int i=0; i<length; i++) {
            if (a[s+i] != a[t+i]) {
                return false;
            }
        }
        return true;
    }


    private int findSameIndex2Block(int index2Length, int otherBlock) {
        int block;

        /* ensure that we do not even partially get past index2Length */
        index2Length-=UTRIE2_INDEX_2_BLOCK_LENGTH;

        for(block=0; block<=index2Length; ++block) {
            if(equal_int(index2, block, otherBlock, UTRIE2_INDEX_2_BLOCK_LENGTH)) {
                return block;
            }
        }
        return -1;
    }


    private int findSameDataBlock(int dataLength, int otherBlock, int blockLength) {
        int block;

        /* ensure that we do not even partially get past dataLength */
        dataLength-=blockLength;

        for(block=0; block<=dataLength; block+=UTRIE2_DATA_GRANULARITY) {
            if(equal_int(data, block, otherBlock, blockLength)) {
                return block;
            }
        }
        return -1;
    }

    /*
     * Find the start of the last range in the trie by enumerating backward.
     * Indexes for supplementary code points higher than this will be omitted.
     */
    private int findHighStart(int highValue) {

        int value;
        int c, prev;
        int i1, i2, j, i2Block, prevI2Block, block, prevBlock;


        /* set variables for previous range */
        if(highValue==initialValue) {
            prevI2Block=index2NullOffset;
            prevBlock=dataNullOffset;
        } else {
            prevI2Block=-1;
            prevBlock=-1;
        }
        prev=0x110000;

        /* enumerate index-2 blocks */
        i1=UNEWTRIE2_INDEX_1_LENGTH;
        c=prev;
        while(c>0) {
            i2Block=index1[--i1];
            if(i2Block==prevI2Block) {
                /* the index-2 block is the same as the previous one, and filled with highValue */
                c-=UTRIE2_CP_PER_INDEX_1_ENTRY;
                continue;
            }
            prevI2Block=i2Block;
            if(i2Block==index2NullOffset) {
                /* this is the null index-2 block */
                if(highValue!=initialValue) {
                    return c;
                }
                c-=UTRIE2_CP_PER_INDEX_1_ENTRY;
            } else {
                /* enumerate data blocks for one index-2 block */
                for(i2=UTRIE2_INDEX_2_BLOCK_LENGTH; i2>0;) {
                    block=index2[i2Block+ --i2];
                    if(block==prevBlock) {
                        /* the block is the same as the previous one, and filled with highValue */
                        c-=UTRIE2_DATA_BLOCK_LENGTH;
                        continue;
                    }
                    prevBlock=block;
                    if(block==dataNullOffset) {
                        /* this is the null data block */
                        if(highValue!=initialValue) {
                            return c;
                        }
                        c-=UTRIE2_DATA_BLOCK_LENGTH;
                    } else {
                        for(j=UTRIE2_DATA_BLOCK_LENGTH; j>0;) {
                            value=data[block+ --j];
                            if(value!=highValue) {
                                return c;
                            }
                            --c;
                        }
                    }
                }
            }
        }

        /* deliver last range */
        return 0;
    }

    /*
     * Compact a build-time trie.
     *
     * The compaction
     * - removes blocks that are identical with earlier ones
     * - overlaps adjacent blocks as much as possible (if overlap==TRUE)
     * - moves blocks in steps of the data granularity
     * - moves and overlaps blocks that overlap with multiple values in the overlap region
     *
     * It does not
     * - try to move and overlap blocks that are not already adjacent
     */
    private void compactData() {
        int start, newStart, movedStart;
        int blockLength, overlap;
        int i, mapIndex, blockCount;

        /* do not compact linear-ASCII data */
        newStart=UTRIE2_DATA_START_OFFSET;
        for(start=0, i=0; start<newStart; start+=UTRIE2_DATA_BLOCK_LENGTH, ++i) {
            map[i]=start;
        }

        /*
         * Start with a block length of 64 for 2-byte UTF-8,
         * then switch to UTRIE2_DATA_BLOCK_LENGTH.
         */
        blockLength=64;
        blockCount=blockLength>>UTRIE2_SHIFT_2;
        for(start=newStart; start<dataLength;) {
            /*
             * start: index of first entry of current block
             * newStart: index where the current block is to be moved
             *           (right after current end of already-compacted data)
             */
            if(start==UNEWTRIE2_DATA_0800_OFFSET) {
                blockLength=UTRIE2_DATA_BLOCK_LENGTH;
                blockCount=1;
            }

            /* skip blocks that are not used */
            if(map[start>>UTRIE2_SHIFT_2]<=0) {
                /* advance start to the next block */
                start+=blockLength;

                /* leave newStart with the previous block! */
                continue;
            }

            /* search for an identical block */
            movedStart=findSameDataBlock(newStart, start, blockLength);
            if(movedStart >= 0) {
                /* found an identical block, set the other block's index value for the current block */
                for(i=blockCount, mapIndex=start>>UTRIE2_SHIFT_2; i>0; --i) {
                    map[mapIndex++]=movedStart;
                    movedStart+=UTRIE2_DATA_BLOCK_LENGTH;
                }

                /* advance start to the next block */
                start+=blockLength;

                /* leave newStart with the previous block! */
                continue;
            }

            /* see if the beginning of this block can be overlapped with the end of the previous block */
            /* look for maximum overlap (modulo granularity) with the previous, adjacent block */
            for(overlap=blockLength-UTRIE2_DATA_GRANULARITY;
                overlap>0 && !equal_int(data, (newStart-overlap), start, overlap);
                overlap-=UTRIE2_DATA_GRANULARITY) {}

            if(overlap>0 || newStart<start) {
                /* some overlap, or just move the whole block */
                movedStart=newStart-overlap;
                for(i=blockCount, mapIndex=start>>UTRIE2_SHIFT_2; i>0; --i) {
                    map[mapIndex++]=movedStart;
                    movedStart+=UTRIE2_DATA_BLOCK_LENGTH;
                }

                /* move the non-overlapping indexes to their new positions */
                start+=overlap;
                for(i=blockLength-overlap; i>0; --i) {
                    data[newStart++]=data[start++];
                }
            } else /* no overlap && newStart==start */ {
                for(i=blockCount, mapIndex=start>>UTRIE2_SHIFT_2; i>0; --i) {
                    map[mapIndex++]=start;
                    start+=UTRIE2_DATA_BLOCK_LENGTH;
                }
                newStart=start;
            }
        }

        /* now adjust the index-2 table */
        for(i=0; i<index2Length; ++i) {
            if(i==UNEWTRIE2_INDEX_GAP_OFFSET) {
                /* Gap indexes are invalid (-1). Skip over the gap. */
                i+=UNEWTRIE2_INDEX_GAP_LENGTH;
            }
            index2[i]=map[index2[i]>>UTRIE2_SHIFT_2];
        }
        dataNullOffset=map[dataNullOffset>>UTRIE2_SHIFT_2];

        /* ensure dataLength alignment */
        while((newStart&(UTRIE2_DATA_GRANULARITY-1))!=0) {
            data[newStart++]=initialValue;
        }

        if  (UTRIE2_DEBUG) {
            /* we saved some space */
            System.out.printf("compacting UTrie2: count of 32-bit data words %d->%d%n",
                dataLength, newStart);
        }

        dataLength=newStart;
    }

    private void compactIndex2() {
        int i, start, newStart, movedStart, overlap;

        /* do not compact linear-BMP index-2 blocks */
        newStart=UTRIE2_INDEX_2_BMP_LENGTH;
        for(start=0, i=0; start<newStart; start+=UTRIE2_INDEX_2_BLOCK_LENGTH, ++i) {
            map[i]=start;
        }

        /* Reduce the index table gap to what will be needed at runtime. */
        newStart+=UTRIE2_UTF8_2B_INDEX_2_LENGTH+((highStart-0x10000)>>UTRIE2_SHIFT_1);

        for(start=UNEWTRIE2_INDEX_2_NULL_OFFSET; start<index2Length;) {
            /*
             * start: index of first entry of current block
             * newStart: index where the current block is to be moved
             *           (right after current end of already-compacted data)
             */

            /* search for an identical block */
            if( (movedStart=findSameIndex2Block(newStart, start))
                 >=0
            ) {
                /* found an identical block, set the other block's index value for the current block */
                map[start>>UTRIE2_SHIFT_1_2]=movedStart;

                /* advance start to the next block */
                start+=UTRIE2_INDEX_2_BLOCK_LENGTH;

                /* leave newStart with the previous block! */
                continue;
            }

            /* see if the beginning of this block can be overlapped with the end of the previous block */
            /* look for maximum overlap with the previous, adjacent block */
            for(overlap=UTRIE2_INDEX_2_BLOCK_LENGTH-1;
                overlap>0 && !equal_int(index2, newStart-overlap, start, overlap);
                --overlap) {}

            if(overlap>0 || newStart<start) {
                /* some overlap, or just move the whole block */
                map[start>>UTRIE2_SHIFT_1_2]=newStart-overlap;

                /* move the non-overlapping indexes to their new positions */
                start+=overlap;
                for(i=UTRIE2_INDEX_2_BLOCK_LENGTH-overlap; i>0; --i) {
                    index2[newStart++]=index2[start++];
                }
            } else /* no overlap && newStart==start */ {
                map[start>>UTRIE2_SHIFT_1_2]=start;
                start+=UTRIE2_INDEX_2_BLOCK_LENGTH;
                newStart=start;
            }
        }

        /* now adjust the index-1 table */
        for(i=0; i<UNEWTRIE2_INDEX_1_LENGTH; ++i) {
            index1[i]=map[index1[i]>>UTRIE2_SHIFT_1_2];
        }
        index2NullOffset=map[index2NullOffset>>UTRIE2_SHIFT_1_2];

        /*
         * Ensure data table alignment:
         * Needs to be granularity-aligned for 16-bit trie
         * (so that dataMove will be down-shiftable),
         * and 2-aligned for uint32_t data.
         */
        while((newStart&((UTRIE2_DATA_GRANULARITY-1)|1))!=0) {
            /* Arbitrary value: 0x3fffc not possible for real data. */
            index2[newStart++]=0x0000ffff<<UTRIE2_INDEX_SHIFT;
        }

        if (UTRIE2_DEBUG) {
            /* we saved some space */
            System.out.printf("compacting UTrie2: count of 16-bit index-2 words %d->%d%n",
                    index2Length, newStart);
        }

        index2Length=newStart;
    }

    private void compactTrie() {
        int localHighStart; 
        int suppHighStart;
        int highValue;

        /* find highStart and round it up */
        highValue=get(0x10ffff);
        localHighStart=findHighStart(highValue);
        localHighStart=(localHighStart+(UTRIE2_CP_PER_INDEX_1_ENTRY-1))&~(UTRIE2_CP_PER_INDEX_1_ENTRY-1);
        if(localHighStart==0x110000) {
            highValue=errorValue;
        }

        /*
         * Set trie->highStart only after utrie2_get32(trie, highStart).
         * Otherwise utrie2_get32(trie, highStart) would try to read the highValue.
         */
        this.highStart=localHighStart;

        if (UTRIE2_DEBUG) {
            System.out.printf("UTrie2: highStart U+%04x  highValue 0x%x  initialValue 0x%x%n",
                highStart, highValue, initialValue);
        }

        if(highStart<0x110000) {
            /* Blank out [highStart..10ffff] to release associated data blocks. */
            suppHighStart= highStart<=0x10000 ? 0x10000 : highStart;
            setRange(suppHighStart, 0x10ffff, initialValue, true);
        }

        compactData();
        if(highStart>0x10000) {
            compactIndex2();
        } else {
            if (UTRIE2_DEBUG) {
                 System.out.printf("UTrie2: highStart U+%04x  count of 16-bit index-2 words %d->%d%n",
                         highStart, index2Length, UTRIE2_INDEX_1_OFFSET);
            }
        }

        /*
         * Store the highValue in the data array and round up the dataLength.
         * Must be done after compactData() because that assumes that dataLength
         * is a multiple of UTRIE2_DATA_BLOCK_LENGTH.
         */
        data[dataLength++]=highValue;
        while((dataLength&(UTRIE2_DATA_GRANULARITY-1))!=0) {
            data[dataLength++]=initialValue;
        }

        isCompacted=true;
    }


    /**
     * Produce an optimized, read-only Trie2_16 from this writable Trie.
     * The data values outside of the range that will fit in a 16 bit
     * unsigned value will be truncated.
     */
    public Trie2_16 toTrie2_16() {
        Trie2_16 frozenTrie = new Trie2_16();
        freeze(frozenTrie, ValueWidth.BITS_16);
        return frozenTrie;
    }
     

    /**
     * Produce an optimized, read-only Trie2_32 from this writable Trie.
     * 
     */
    public Trie2_32 toTrie2_32() {
        Trie2_32 frozenTrie = new Trie2_32();
        freeze(frozenTrie, ValueWidth.BITS_32);
        return frozenTrie;
    }

  
    /**
     * Maximum length of the runtime index array.
     * Limited by its own 16-bit index values, and by uint16_t UTrie2Header.indexLength.
     * (The actual maximum length is lower,
     * (0x110000>>UTRIE2_SHIFT_2)+UTRIE2_UTF8_2B_INDEX_2_LENGTH+UTRIE2_MAX_INDEX_1_LENGTH.)
     */
    private static final int UTRIE2_MAX_INDEX_LENGTH = 0xffff;

    /**
     * Maximum length of the runtime data array.
     * Limited by 16-bit index values that are left-shifted by UTRIE2_INDEX_SHIFT,
     * and by uint16_t UTrie2Header.shiftedDataLength.
     */
    private static final int  UTRIE2_MAX_DATA_LENGTH = 0xffff<<UTRIE2_INDEX_SHIFT;

    /* Compact the data and then populate an optimized read-only Trie.  */
    private void freeze(Trie2 dest, ValueWidth valueBits) {
        int  i;
        int  allIndexesLength;
        int  dataMove;  /* >0 if the data is moved to the end of the index array */


        /* compact if necessary */
        if(!isCompacted) {
            compactTrie();
        }

        if(highStart<=0x10000) {
            allIndexesLength=UTRIE2_INDEX_1_OFFSET;
        } else {
            allIndexesLength=index2Length;
        }
        if(valueBits==ValueWidth.BITS_16) {
            dataMove=allIndexesLength;
        } else {
            dataMove=0;
        }

        /* are indexLength and dataLength within limits? */
        if( /* for unshifted indexLength */
            allIndexesLength>UTRIE2_MAX_INDEX_LENGTH ||
            /* for unshifted dataNullOffset */
            (dataMove+dataNullOffset)>0xffff ||
            /* for unshifted 2-byte UTF-8 index-2 values */
            (dataMove+UNEWTRIE2_DATA_0800_OFFSET)>0xffff ||
            /* for shiftedDataLength */
            (dataMove+dataLength)>UTRIE2_MAX_DATA_LENGTH) {
                throw new UnsupportedOperationException("Trie2 data is too large.");
        }

        /* calculate the sizes of, and allocate, the index and data arrays */
        int indexLength = allIndexesLength;
        if (valueBits==ValueWidth.BITS_16) {
            indexLength += dataLength;
        } else {
            dest.data32 = new int[dataLength];
        }
        dest.index = new char[indexLength];
        
        dest.indexLength = allIndexesLength;
        dest.dataLength  = dataLength;
        if(highStart<=0x10000) {
            dest.index2NullOffset = 0xffff;
        } else {
            dest.index2NullOffset = UTRIE2_INDEX_2_OFFSET + index2NullOffset;
        }
        dest.initialValue   = initialValue;
        dest.errorValue     = errorValue;
        dest.highStart      = highStart;
        dest.highValueIndex = dataMove + dataLength - UTRIE2_DATA_GRANULARITY;
        dest.dataNullOffset = (dataMove+dataNullOffset);
        
        // Create a header and set the its fields.
        //   (This is only used in the event that we serialize the Trie, but is
        //    convenient to do here.)
        dest.header = new Trie2.UTrie2Header();
        dest.header.signature         = 0x54726932; /* "Tri2" */
        dest.header.options           = valueBits==ValueWidth.BITS_16 ? 0 : 1;
        dest.header.indexLength       = dest.indexLength;
        dest.header.shiftedDataLength = dest.dataLength>>UTRIE2_INDEX_SHIFT;
        dest.header.index2NullOffset  = dest.index2NullOffset;
        dest.header.dataNullOffset    = dest.dataNullOffset;
        dest.header.shiftedHighStart  = dest.highStart>>UTRIE2_SHIFT_1;
            


        /* write the index-2 array values shifted right by UTRIE2_INDEX_SHIFT, after adding dataMove */
        int destIdx = 0;
        for(i=0; i<UTRIE2_INDEX_2_BMP_LENGTH; i++) {
            dest.index[destIdx++] = (char)((index2[i]+dataMove) >> UTRIE2_INDEX_SHIFT);
        }
        if (UTRIE2_DEBUG) {
            System.out.println("\n\nIndex2 for BMP limit is " + Integer.toHexString(destIdx));
        }

        /* write UTF-8 2-byte index-2 values, not right-shifted */
        for(i=0; i<(0xc2-0xc0); ++i) {                                  /* C0..C1 */
            dest.index[destIdx++] = (char)(dataMove+UTRIE2_BAD_UTF8_DATA_OFFSET);
        }
        for(; i<(0xe0-0xc0); ++i) {                                     /* C2..DF */
            dest.index[destIdx++]=(char)(dataMove+index2[i<<(6-UTRIE2_SHIFT_2)]);
        }
        if (UTRIE2_DEBUG) {
            System.out.println("Index2 for UTF-8 2byte values limit is " + Integer.toHexString(destIdx));
        }

        if(highStart>0x10000) {
            int index1Length = (highStart-0x10000)>>UTRIE2_SHIFT_1;
            int index2Offset = UTRIE2_INDEX_2_BMP_LENGTH + UTRIE2_UTF8_2B_INDEX_2_LENGTH + index1Length;

            /* write 16-bit index-1 values for supplementary code points */
            //p=(uint32_t *)newTrie->index1+UTRIE2_OMITTED_BMP_INDEX_1_LENGTH;
            for(i=0; i<index1Length; i++) {
                //*dest16++=(uint16_t)(UTRIE2_INDEX_2_OFFSET + *p++);
                dest.index[destIdx++] = (char)(UTRIE2_INDEX_2_OFFSET + index1[i+UTRIE2_OMITTED_BMP_INDEX_1_LENGTH]);
            }
            if (UTRIE2_DEBUG) {
                System.out.println("Index 1 for supplementals, limit is " + Integer.toHexString(destIdx));
            }

            /*
             * write the index-2 array values for supplementary code points,
             * shifted right by UTRIE2_INDEX_SHIFT, after adding dataMove
             */
            for(i=0; i<index2Length-index2Offset; i++) {
                dest.index[destIdx++] = (char)((dataMove + index2[index2Offset+i])>>UTRIE2_INDEX_SHIFT);
            }
            if (UTRIE2_DEBUG) {
                System.out.println("Index 2 for supplementals, limit is " + Integer.toHexString(destIdx));
            }
        }
        
        /* write the 16/32-bit data array */
        switch(valueBits) {
        case BITS_16:
            /* write 16-bit data values */
            assert(destIdx == dataMove);
            dest.data16 = destIdx;
            for(i=0; i<dataLength; i++) {
                dest.index[destIdx++] = (char)data[i];
            }
            break;
        case BITS_32:
            /* write 32-bit data values */
            for (i=0; i<dataLength; i++) {
                dest.data32[i] = this.data[i];
            }
            break;
        }        
        // The writable, but compressed, Trie2 stays around unless the caller drops its references to it.
    }


    /* Start with allocation of 16k data entries. */
    private static final int UNEWTRIE2_INITIAL_DATA_LENGTH = 1<<14;

    /* Grow about 8x each time. */
    private static final int UNEWTRIE2_MEDIUM_DATA_LENGTH = 1<<17;
    
    /** The null index-2 block, following the gap in the index-2 table. */
    private static final int UNEWTRIE2_INDEX_2_NULL_OFFSET = UNEWTRIE2_INDEX_GAP_OFFSET + UNEWTRIE2_INDEX_GAP_LENGTH;

    /** The start of allocated index-2 blocks. */
    private static final int UNEWTRIE2_INDEX_2_START_OFFSET = UNEWTRIE2_INDEX_2_NULL_OFFSET + UTRIE2_INDEX_2_BLOCK_LENGTH;

    /**
     * The null data block.
     * Length 64=0x40 even if UTRIE2_DATA_BLOCK_LENGTH is smaller,
     * to work with 6-bit trail bytes from 2-byte UTF-8.
     */
    private static final int UNEWTRIE2_DATA_NULL_OFFSET = UTRIE2_DATA_START_OFFSET;

    /** The start of allocated data blocks. */
    private static final int UNEWTRIE2_DATA_START_OFFSET = UNEWTRIE2_DATA_NULL_OFFSET+0x40;

    /**
     * The start of data blocks for U+0800 and above.
     * Below, compaction uses a block length of 64 for 2-byte UTF-8.
     * From here on, compaction uses UTRIE2_DATA_BLOCK_LENGTH.
     * Data values for 0x780 code points beyond ASCII.
     */
    private static final int UNEWTRIE2_DATA_0800_OFFSET = UNEWTRIE2_DATA_START_OFFSET+0x780;

    //
    // Private data members.  From struct UNewTrie2 in ICU4C
    //
    private  int[]   index1 = new int[UNEWTRIE2_INDEX_1_LENGTH];
    private  int[]   index2 = new int[UNEWTRIE2_MAX_INDEX_2_LENGTH];
    private  int[]   data;

    private  int     index2Length;
    private  int     dataCapacity;
    private  int     firstFreeBlock;
    private  int     index2NullOffset;
    private  boolean isCompacted;


    /*
     * Multi-purpose per-data-block table.
     *
     * Before compacting:
     *
     * Per-data-block reference counters/free-block list.
     *  0: unused
     * >0: reference counter (number of index-2 entries pointing here)
     * <0: next free data block in free-block list
     *
     * While compacting:
     *
     * Map of adjusted indexes, used in compactData() and compactIndex2().
     * Maps from original indexes to new ones.
     */
     private  int[]   map = new int[UNEWTRIE2_MAX_DATA_LENGTH>>UTRIE2_SHIFT_2];
     
     
     private boolean UTRIE2_DEBUG = false;

}
