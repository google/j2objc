/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2002-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.util;

public interface Equator {
    /**
      * Comparator function. If overridden, must handle case of null,
      * and compare any two objects that could be compared.
      * Must obey normal rules of symmetry: a=b => b=a
      * and transitivity: a=b & b=c => a=b)
      * @param a
      * @param b
      * @return true if a and b are equal
      */
     public boolean isEqual(Object a, Object b);

    /**
     * Must obey normal rules: a=b => getHashCode(a)=getHashCode(b)
     * @param object
     * @return a hash code for the object
     */
    public int getHashCode(Object object);
}