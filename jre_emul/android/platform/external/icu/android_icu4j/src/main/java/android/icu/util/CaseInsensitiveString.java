/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2013, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.util;

import android.icu.lang.UCharacter;

/**
 * A string used as a key in java.util.Hashtable and other
 * collections.  It retains case information, but its equals() and
 * hashCode() methods ignore case.
 * @hide Only a subset of ICU is exposed in Android
 */
public class CaseInsensitiveString {
    
    private String string;

    private int hash = 0;
    
    private String folded = null;
    
    private static String foldCase(String foldee)
    {
        return UCharacter.foldCase(foldee, true);
    }
    
    private void getFolded()
    {
        if (folded == null) {
            folded = foldCase(string);
        }
    }
    
    /**
     * Constructs an CaseInsentiveString object from the given string
     * @param s The string to construct this object from 
     */
    public CaseInsensitiveString(String s) {
        string = s;
    }
    /**
     * returns the underlying string 
     * @return String
     */
    public String getString() {
        return string;
    }
    /**
     * Compare the object with this 
     * @param o Object to compare this object with 
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o instanceof CaseInsensitiveString) {
            getFolded();
            CaseInsensitiveString cis = (CaseInsensitiveString) o;
            cis.getFolded();
            return folded.equals(cis.folded);
        }
        return false;
    }
    
    /**
     * Returns the hashCode of this object
     * @return int hashcode
     */
    public int hashCode() {
        getFolded();
        
        if (hash == 0) {
            hash = folded.hashCode();
        }
        
        return hash;
    }
    
    /**
     * Overrides superclass method
     */
    public String toString() {
        return string;
    }
}
