/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 *
 * created on: 2014feb10
 * created by: Markus W. Scherer
 */
package android.icu.impl.coll;

// TODO: There must be a Java class for a growable array of longs without auto-boxing to Long?!
// Keep the API parallel to the C++ version for ease of porting. Port methods only as needed.
// If & when we start using something else, we might keep this as a thin wrapper for porting.
/**
 * @hide Only a subset of ICU is exposed in Android
 */
public final class UVector64 {
    public UVector64() {}
    public boolean isEmpty() { return length == 0; }
    public int size() { return length; }
    public long elementAti(int i) { return buffer[i]; }
    public long[] getBuffer() { return buffer; }
    public void addElement(long e) {
        ensureAppendCapacity();
        buffer[length++] = e;
    }
    public void setElementAt(long elem, int index) { buffer[index] = elem; }
    public void insertElementAt(long elem, int index) {
        ensureAppendCapacity();
        System.arraycopy(buffer, index, buffer, index + 1, length - index);
        buffer[index] = elem;
        ++length;
    }
    public void removeAllElements() {
        length = 0;
    }

    private void ensureAppendCapacity() {
        if(length >= buffer.length) {
            int newCapacity = buffer.length <= 0xffff ? 4 * buffer.length : 2 * buffer.length;
            long[] newBuffer = new long[newCapacity];
            System.arraycopy(buffer, 0, newBuffer, 0, length);
            buffer = newBuffer;
        }
    }
    private long[] buffer = new long[32];
    private int length = 0;
}
