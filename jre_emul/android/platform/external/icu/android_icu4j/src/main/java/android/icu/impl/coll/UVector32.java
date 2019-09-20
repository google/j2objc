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

// TODO: There must be a Java class for a growable array of ints without auto-boxing to Integer?!
// Keep the API parallel to the C++ version for ease of porting. Port methods only as needed.
// If & when we start using something else, we might keep this as a thin wrapper for porting.
/**
 * @hide Only a subset of ICU is exposed in Android
 */
public final class UVector32 {
    public UVector32() {}
    public boolean isEmpty() { return length == 0; }
    public int size() { return length; }
    public int elementAti(int i) { return buffer[i]; }
    public int[] getBuffer() { return buffer; }
    public void addElement(int e) {
        ensureAppendCapacity();
        buffer[length++] = e;
    }
    public void setElementAt(int elem, int index) { buffer[index] = elem; }
    public void insertElementAt(int elem, int index) {
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
            int[] newBuffer = new int[newCapacity];
            System.arraycopy(buffer, 0, newBuffer, 0, length);
            buffer = newBuffer;
        }
    }
    private int[] buffer = new int[32];
    private int length = 0;
}
