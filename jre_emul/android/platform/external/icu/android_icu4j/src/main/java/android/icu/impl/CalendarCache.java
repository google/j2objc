/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2011, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.impl;

/**
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public class CalendarCache
{
    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public CalendarCache() {
        makeArrays(arraySize);
    }
    
    private void makeArrays(int newSize) {
        keys    = new long[newSize];
        values  = new long[newSize];
        
        for (int i = 0; i < newSize; i++) {
            values[i] = EMPTY;
        }
        arraySize = newSize;
        threshold = (int)(arraySize * 0.75);
        size = 0;
    }
    
    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public synchronized long get(long key) {
        return values[findIndex(key)];
    }
    
    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public synchronized void put(long key, long value)
    {
        if (size >= threshold) {
            rehash();
        }
        int index = findIndex(key);
        
        keys[index] = key;
        values[index] = value;
        size++;
    }
    
    private final int findIndex(long key) {
        int index = hash(key);
        int delta = 0;
        
        while (values[index] != EMPTY && keys[index] != key)
        {
            if (delta == 0) {
                delta = hash2(key);
            }
            index = (index + delta) % arraySize;
        }
        return index;
    }
    
    private void rehash()
    {
        int oldSize = arraySize;
        long[] oldKeys = keys;
        long[] oldValues = values;
        
        if (pIndex < primes.length - 1) {
            arraySize = primes[++pIndex];
        } else {
            arraySize = arraySize * 2 + 1;
        }
        size = 0;
        
        makeArrays(arraySize);
        for (int i = 0; i < oldSize; i++) {
            if (oldValues[i] != EMPTY) {
                put(oldKeys[i], oldValues[i]);
            }
        }
    }
    
    
    /**
     * Produce a uniformly-distributed hash value from an integer key.
     * This is essentially a linear congruential random number generator
     * that uses the key as its seed value.
     */
    private final int hash(long key)
    {
        int h = (int)((key * 15821 + 1) % arraySize);
        if (h < 0) {
            h += arraySize;
        }
        return h;
    }
    
    private final int hash2(long key) {
        return arraySize - 2 - (int)(key % (arraySize-2) );
    }
    
    static private final int primes[] = {  // 5, 17, 31, 47, // for testing
        61, 127, 509, 1021, 2039, 4093, 8191, 16381, 32749, 65521,
        131071, 262139, 
    };

    private int     pIndex      = 0;
    private int     size        = 0;
    private int     arraySize   = primes[pIndex];
    private int     threshold   = (arraySize * 3) / 4;
    
    private long[]  keys        = new long[arraySize];
    private long[]  values      = new long[arraySize];

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    static public  long EMPTY   = Long.MIN_VALUE;
}
