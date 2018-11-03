/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.text;

import android.icu.util.ByteArrayWrapper;

/**
 * <p>
 * Simple class wrapper to store the internal byte representation of a 
 * CollationKey. Unlike the CollationKey, this class do not contain information 
 * on the source string the sort order represents. RawCollationKey is mutable 
 * and users can reuse its objects with the method in 
 * RuleBasedCollator.getRawCollationKey(..).
 * </p>
 * <p>
 * Please refer to the documentation on CollationKey for a detail description
 * on the internal byte representation. Note the internal byte representation 
 * is always null-terminated.
 * </p> 
 * <code>
 * Example of use:<br>
 * String str[] = {.....};
 * RuleBasedCollator collator = (RuleBasedCollator)Collator.getInstance();
 * RawCollationKey key = new RawCollationKey(128);
 * for (int i = 0; i &lt; str.length; i ++) {
 *     collator.getRawCollationKey(str[i], key);
 *     // do something with key.bytes
 * }
 * </code>
 * <p><strong>Note:</strong> Comparison between RawCollationKeys created by 
 * different Collators might return incorrect results.  
 * See class documentation for Collator.</p>
 * @see RuleBasedCollator
 * @see CollationKey
 * @hide Only a subset of ICU is exposed in Android
 */
public final class RawCollationKey extends ByteArrayWrapper
{
    // public constructors --------------------------------------------------
    
    /**
     * Default constructor, internal byte array is null and its size set to 0.
     */
    public RawCollationKey() 
    {
    }

    /**
     * RawCollationKey created with an empty internal byte array of length 
     * capacity. Size of the internal byte array will be set to 0.
     * @param capacity length of internal byte array
     */
    public RawCollationKey(int capacity) 
    {
        bytes = new byte[capacity];
    }

    /**
     * RawCollationKey created, adopting bytes as the internal byte array.
     * Size of the internal byte array will be set to 0.
     * @param bytes byte array to be adopted by RawCollationKey
     */
    public RawCollationKey(byte[] bytes) 
    {
        this.bytes = bytes;
    }
    
    /**
     * Construct a RawCollationKey from a byte array and size.
     * @param bytesToAdopt the byte array to adopt
     * @param size the length of valid data in the byte array
     * @throws IndexOutOfBoundsException if bytesToAdopt == null and size != 0, or
     * size &lt; 0, or size &gt; bytesToAdopt.length.
     */
    public RawCollationKey(byte[] bytesToAdopt, int size) 
    {
        super(bytesToAdopt, size);
    }

    /**
     * Compare this RawCollationKey to another, which must not be null.  This overrides
     * the inherited implementation to ensure the returned values are -1, 0, or 1.
     * @param rhs the RawCollationKey to compare to.
     * @return -1, 0, or 1 as this compares less than, equal to, or
     * greater than rhs.
     * @throws ClassCastException if the other object is not a RawCollationKey.
     */
    public int compareTo(RawCollationKey rhs) {
        int result = super.compareTo(rhs);
        return result < 0 ? -1 : result == 0 ? 0 : 1;
    }
}
