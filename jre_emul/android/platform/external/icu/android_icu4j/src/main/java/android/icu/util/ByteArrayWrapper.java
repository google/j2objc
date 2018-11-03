/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.util;

import java.nio.ByteBuffer;

import android.icu.impl.Utility;

/**
 * A simple utility class to wrap a byte array.
 * <p>
 * Generally passed as an argument object into a method. The method takes
 * responsibility of writing into the internal byte array and increasing its
 * size when necessary.
 *
 * @author syn wee
 * @hide Only a subset of ICU is exposed in Android
 */
public class ByteArrayWrapper implements Comparable<ByteArrayWrapper>
{
    // public data member ------------------------------------------------
    
    /**
     * Internal byte array.
     */
    public byte[] bytes;

    /**
     * Size of the internal byte array used. 
     * Different from bytes.length, size will be &lt;= bytes.length. 
     * Semantics of size is similar to java.util.Vector.size().
     */
    public int size;
    
    // public constructor ------------------------------------------------

    /** 
     * Construct a new ByteArrayWrapper with no data.
     */
    public ByteArrayWrapper() {
        // leave bytes null, don't allocate twice
    }

    /**
     * Construct a new ByteArrayWrapper from a byte array and size
     * @param bytesToAdopt the byte array to adopt
     * @param size the length of valid data in the byte array
     * @throws IndexOutOfBoundsException if bytesToAdopt == null and size != 0, or
     * size &lt; 0, or size &gt; bytesToAdopt.length.
     */
    public ByteArrayWrapper(byte[] bytesToAdopt, int size) {
        if ((bytesToAdopt == null && size != 0) || size < 0 || (bytesToAdopt != null && size > bytesToAdopt.length)) {
            throw new IndexOutOfBoundsException("illegal size: " + size);
        }
        this.bytes = bytesToAdopt;
        this.size = size;
    }

    /**
     * Construct a new ByteArrayWrapper from the contents of a ByteBuffer.
     * @param source the ByteBuffer from which to get the data.
     */
    public ByteArrayWrapper(ByteBuffer source) {
        size = source.limit();
        bytes = new byte[size];
        source.get(bytes,0,size);
    }

    /**
     * Create from ByteBuffer
     * @param byteBuffer
    public ByteArrayWrapper(ByteArrayWrapper source) {
        size = source.size;
        bytes = new byte[size];
        copyBytes(source.bytes, 0, bytes, 0, size);
    }
     */

    /**
     * create from byte buffer
     * @param src
     * @param start
     * @param limit
    public ByteArrayWrapper(byte[] src, int start, int limit) {
        size = limit - start;
        bytes = new byte[size];
        copyBytes(src, start, bytes, 0, size);
    }
     */

    // public methods ----------------------------------------------------

    /**
     * Ensure that the internal byte array is at least of length capacity.     
     * If the byte array is null or its length is less than capacity, a new 
     * byte array of length capacity will be allocated.  
     * The contents of the array (between 0 and size) remain unchanged. 
     * @param capacity minimum length of internal byte array.
     * @return this ByteArrayWrapper
     */
    public ByteArrayWrapper ensureCapacity(int capacity) 
    {
        if (bytes == null || bytes.length < capacity) {
            byte[] newbytes = new byte[capacity];
            if (bytes != null) {
                copyBytes(bytes, 0, newbytes, 0, size);
            }
            bytes = newbytes;
        }
        return this;
    }
    
    /**
     * Set the internal byte array from offset 0 to (limit - start) with the 
     * contents of src from offset start to limit. If the byte array is null or its length is less than capacity, a new 
     * byte array of length (limit - start) will be allocated.  
     * This resets the size of the internal byte array to (limit - start).
     * @param src source byte array to copy from
     * @param start start offset of src to copy from
     * @param limit end + 1 offset of src to copy from
     * @return this ByteArrayWrapper
     */
    public final ByteArrayWrapper set(byte[] src, int start, int limit) 
    {
        size = 0;
        append(src, start, limit);
        return this;
    }
    
    /*
    public final ByteArrayWrapper get(byte[] target, int start, int limit) 
    {
        int len = limit - start;
        if (len > size) throw new IllegalArgumentException("limit too long");
        copyBytes(bytes, 0, target, start, len);
        return this;
    }
    */

    /**
     * Appends the internal byte array from offset size with the 
     * contents of src from offset start to limit. This increases the size of
     * the internal byte array to (size + limit - start).
     * @param src source byte array to copy from
     * @param start start offset of src to copy from
     * @param limit end + 1 offset of src to copy from
     * @return this ByteArrayWrapper
     */
    public final ByteArrayWrapper append(byte[] src, int start, int limit) 
    {
        int len = limit - start;
        ensureCapacity(size + len);
        copyBytes(src, start, bytes, size, len);
        size += len;
        return this;
    }

    /*
    public final ByteArrayWrapper append(ByteArrayWrapper other) 
    {
        return append(other.bytes, 0, other.size);
    }
    */

    /**
     * Releases the internal byte array to the caller, resets the internal
     * byte array to null and its size to 0.
     * @return internal byte array.
     */
    public final byte[] releaseBytes()
    {
        byte result[] = bytes;
        bytes = null;
        size = 0;
        return result;
    }
    
    // Boilerplate ----------------------------------------------------
    
    /**
     * Returns string value for debugging
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            if (i != 0) result.append(" ");
            result.append(Utility.hex(bytes[i]&0xFF,2));
        }
        return result.toString();
    }

    /**
     * Return true if the bytes in each wrapper are equal.
     * @param other the object to compare to.
     * @return true if the two objects are equal.
     */
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        try {
            ByteArrayWrapper that = (ByteArrayWrapper)other;
            if (size != that.size) return false;
            for (int i = 0; i < size; ++i) {
                if (bytes[i] != that.bytes[i]) return false;
            }
            return true;
        }
        catch (ClassCastException e) {
        }
        return false;
    }

    /**
     * Return the hashcode.
     * @return the hashcode.
     */
    public int hashCode() {
        int result = bytes.length;
        for (int i = 0; i < size; ++i) {
            result = 37*result + bytes[i];
        }
        return result;
    }

    /**
     * Compare this object to another ByteArrayWrapper, which must not be null.
     * @param other the object to compare to.
     * @return a value &lt;0, 0, or &gt;0 as this compares less than, equal to, or
     * greater than other.
     * @throws ClassCastException if the other object is not a ByteArrayWrapper
     */
    public int compareTo(ByteArrayWrapper other) {
        if (this == other) return 0;
        int minSize = size < other.size ? size : other.size;
        for (int i = 0; i < minSize; ++i) {
            if (bytes[i] != other.bytes[i]) {
                return (bytes[i] & 0xFF) - (other.bytes[i] & 0xFF);
            }
        }
        return size - other.size;
    }
    
    // private methods -----------------------------------------------------
    
    /**
     * Copies the contents of src byte array from offset srcoff to the 
     * target of tgt byte array at the offset tgtoff.
     * @param src source byte array to copy from
     * @param srcoff start offset of src to copy from
     * @param tgt target byte array to copy to
     * @param tgtoff start offset of tgt to copy to
     * @param length size of contents to copy
     */
    private static final void copyBytes(byte[] src, int srcoff, byte[] tgt, 
                                       int tgtoff, int length) {
        if (length < 64) {
            for (int i = srcoff, n = tgtoff; -- length >= 0; ++ i, ++ n) {
                tgt[n] = src[i];
            }
        } 
        else {
            System.arraycopy(src, srcoff, tgt, tgtoff, length);
        }
    }      
}
