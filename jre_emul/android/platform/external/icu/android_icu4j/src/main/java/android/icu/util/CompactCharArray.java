/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.util;

import android.icu.impl.Utility;

/**
 * class CompactATypeArray : use only on primitive data types
 * Provides a compact way to store information that is indexed by Unicode
 * values, such as character properties, types, keyboard values, etc.This
 * is very useful when you have a block of Unicode data that contains
 * significant values while the rest of the Unicode data is unused in the
 * application or when you have a lot of redundance, such as where all 21,000
 * Han ideographs have the same value.  However, lookup is much faster than a
 * hash table.
 * A compact array of any primitive data type serves two purposes:
 * <UL type = round>
 *     <LI>Fast access of the indexed values.
 *     <LI>Smaller memory footprint.
 * </UL>
 * A compact array is composed of a index array and value array.  The index
 * array contains the indicies of Unicode characters to the value array.
 * @see                CompactByteArray
 * @author             Helena Shih
 * @deprecated This API is ICU internal only.
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
@Deprecated
public final class CompactCharArray implements Cloneable {

    /**
     * The total number of Unicode characters.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static  final int UNICODECOUNT = 65536;

    /**
     * Default constructor for CompactCharArray, the default value of the
     * compact array is 0.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public CompactCharArray()
    {
        this((char)0);
    }

    /**
     * Constructor for CompactCharArray.
     * @param defaultValue the default value of the compact array.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public CompactCharArray(char defaultValue)
    {
        int i;
        values = new char[UNICODECOUNT];
        indices = new char[INDEXCOUNT];
        hashes = new int[INDEXCOUNT];
        for (i = 0; i < UNICODECOUNT; ++i) {
            values[i] = defaultValue;
        }
        for (i = 0; i < INDEXCOUNT; ++i) {
            indices[i] = (char)(i<<BLOCKSHIFT);
            hashes[i] = 0;
        }
        isCompact = false;

        this.defaultValue = defaultValue;
    }

    /**
     * Constructor for CompactCharArray.
     * @param indexArray the indicies of the compact array.
     * @param newValues the values of the compact array.
     * @exception IllegalArgumentException If the index is out of range.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public CompactCharArray(char indexArray[],
                             char newValues[])
    {
        int i;
        if (indexArray.length != INDEXCOUNT)
            throw new IllegalArgumentException("Index out of bounds.");
        for (i = 0; i < INDEXCOUNT; ++i) {
            char index = indexArray[i];
            if (index >= newValues.length+BLOCKCOUNT)
                throw new IllegalArgumentException("Index out of bounds.");
        }
        indices = indexArray;
        values = newValues;
        isCompact = true;
    }

    /**
     * Constructor for CompactCharArray.
     *
     * @param indexArray the RLE-encoded indicies of the compact array.
     * @param valueArray the RLE-encoded values of the compact array.
     *
     * @throws IllegalArgumentException if the index or value array is
     *          the wrong size.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public CompactCharArray(String indexArray,
                String valueArray)
    {
        this( Utility.RLEStringToCharArray(indexArray),
              Utility.RLEStringToCharArray(valueArray));
    }

    /**
     * Get the mapped value of a Unicode character.
     * @param index the character to get the mapped value with
     * @return the mapped value of the given character
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public char elementAt(char index)
    {
    int ix = (indices[index >> BLOCKSHIFT] & 0xFFFF)
        + (index & BLOCKMASK);
    return ix >= values.length ? defaultValue : values[ix];
    }

    /**
     * Set a new value for a Unicode character.
     * Set automatically expands the array if it is compacted.
     * @param index the character to set the mapped value with
     * @param value the new mapped value
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public void setElementAt(char index, char value)
    {
        if (isCompact)
            expand();
         values[index] = value;
        touchBlock(index >> BLOCKSHIFT, value);
    }

    /**
     * Set new values for a range of Unicode character.
     *
     * @param start the starting offset of the range
     * @param end the ending offset of the range
     * @param value the new mapped value
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public void setElementAt(char start, char end, char value)
    {
        int i;
        if (isCompact) {
            expand();
        }
        for (i = start; i <= end; ++i) {
            values[i] = value;
            touchBlock(i >> BLOCKSHIFT, value);
        }
    }
    /**
     * Compact the array
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public void compact() {
        compact(true);
    }

    /**
     * Compact the array.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public void compact(boolean exhaustive)
    {
        if (!isCompact) {
            int iBlockStart = 0;
            char iUntouched = 0xFFFF;
            int newSize = 0;

            char[] target = exhaustive ? new char[UNICODECOUNT] : values;

            for (int i = 0; i < indices.length; ++i, iBlockStart += BLOCKCOUNT) {
                indices[i] = 0xFFFF;
                boolean touched = blockTouched(i);
                if (!touched && iUntouched != 0xFFFF) {
                    // If no values in this block were set, we can just set its
                    // index to be the same as some other block with no values
                    // set, assuming we've seen one yet.
                    indices[i] = iUntouched;
                } else {
                    int jBlockStart = 0;
                    // See if we can find a previously compacted block that's identical
                    for (int j = 0; j < i; ++j, jBlockStart += BLOCKCOUNT) {
                        if (hashes[i] == hashes[j] &&
                                arrayRegionMatches(values, iBlockStart,
                                                   values, jBlockStart, BLOCKCOUNT)) {
                            indices[i] = indices[j];
                        }
                    }
                    if (indices[i] == 0xFFFF) {
                        int dest;   // Where to copy
                        if (exhaustive) {
                            // See if we can find some overlap with another block
                            dest = FindOverlappingPosition(iBlockStart, target,
                                                            newSize);
                        } else {
                            // Just copy to the end; it's quicker
                            dest = newSize;
                        }
                        int limit = dest + BLOCKCOUNT;
                        if (limit > newSize) {
                            for (int j = newSize; j < limit; ++j) {
                                target[j] = values[iBlockStart + j - dest];
                            }
                            newSize = limit;
                        }
                        indices[i] = (char)dest;
                        if (!touched) {
                            // If this is the first untouched block we've seen,
                            // remember its index.
                            iUntouched = (char)jBlockStart;
                        }
                    }
                }
            }
            // we are done compacting, so now make the array shorter
            char[] result = new char[newSize];
            System.arraycopy(target, 0, result, 0, newSize);
            values = result;
            isCompact = true;
            hashes = null;
        }
    }

    private int FindOverlappingPosition(int start, char[] tempValues, int tempCount)
    {
        for (int i = 0; i < tempCount; i += 1) {
            int currentCount = BLOCKCOUNT;
            if (i + BLOCKCOUNT > tempCount) {
                currentCount = tempCount - i;
            }
            if (arrayRegionMatches(values, start, tempValues, i, currentCount))
                return i;
        }
        return tempCount;
    }

    /**
     * Convenience utility to compare two arrays of doubles.
     * @param len the length to compare.
     * The start indices and start+len must be valid.
     */
    final static boolean arrayRegionMatches(char[] source, int sourceStart,
                                            char[] target, int targetStart,
                                            int len)
    {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (source[i] != target[i + delta])
            return false;
        }
        return true;
    }

    /**
     * Remember that a specified block was "touched", i.e. had a value set.
     * Untouched blocks can be skipped when compacting the array
     */
    private final void touchBlock(int i, int value) {
        hashes[i] = (hashes[i] + (value<<1)) | 1;
    }

    /**
     * Query whether a specified block was "touched", i.e. had a value set.
     * Untouched blocks can be skipped when compacting the array
     */
    private final boolean blockTouched(int i) {
        return hashes[i] != 0;
    }

    /**
     * For internal use only.  Do not modify the result, the behavior of
     * modified results are undefined.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public char[] getIndexArray()
    {
        return indices;
    }

    /**
     * For internal use only.  Do not modify the result, the behavior of
     * modified results are undefined.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public char[] getValueArray()
    {
        return values;
    }

    /**
     * Overrides Cloneable
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    @Deprecated
    public Object clone()
    {
        try {
            CompactCharArray other = (CompactCharArray) super.clone();
            other.values = values.clone();
            other.indices = indices.clone();
            if (hashes != null) other.hashes = hashes.clone();
            return other;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    /**
     * Compares the equality of two compact array objects.
     * @param obj the compact array object to be compared with this.
     * @return true if the current compact array object is the same
     * as the compact array object obj; false otherwise.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    @Deprecated
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj)                      // quick check
            return true;
        if (getClass() != obj.getClass())         // same class?
            return false;
        CompactCharArray other = (CompactCharArray) obj;
        for (int i = 0; i < UNICODECOUNT; i++) {
            // could be sped up later
            if (elementAt((char)i) != other.elementAt((char)i))
                return false;
        }
        return true; // we made it through the guantlet.
    }

    /**
     * Generates the hash code for the compact array object
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    @Deprecated
    public int hashCode() {
        int result = 0;
        int increment = Math.min(3, values.length/16);
        for (int i = 0; i < values.length; i+= increment) {
            result = result * 37 + values[i];
        }
        return result;
    }


    // --------------------------------------------------------------
    // private
    // --------------------------------------------------------------

    /**
     * Expanding takes the array back to a 65536 element array.
     */
    private void expand()
    {
        int i;
        if (isCompact) {
            char[] tempArray;
            hashes = new int[INDEXCOUNT];
            tempArray = new char[UNICODECOUNT];
            for (i = 0; i < UNICODECOUNT; ++i) {
                tempArray[i] = elementAt((char)i);
            }
            for (i = 0; i < INDEXCOUNT; ++i) {
                indices[i] = (char)(i<<BLOCKSHIFT);
            }
            values = null;
            values = tempArray;
            isCompact = false;
        }
    }
    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static  final int BLOCKSHIFT = 5; // NormalizerBuilder needs - liu
    static  final int BLOCKCOUNT =(1<<BLOCKSHIFT);
    static  final int INDEXSHIFT =(16-BLOCKSHIFT);
    static  final int INDEXCOUNT =(1<<INDEXSHIFT);
    static  final int BLOCKMASK = BLOCKCOUNT - 1;

    private char values[];
    private char indices[];
    private int[] hashes;
    private boolean isCompact;
    char defaultValue;
}
