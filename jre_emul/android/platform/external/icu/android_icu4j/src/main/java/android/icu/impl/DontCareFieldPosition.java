/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
* Copyright (c) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
*/
package android.icu.impl;

import java.text.FieldPosition;

/**
 * DontCareFieldPosition is a subclass of FieldPosition that indicates that the
 * caller is not interested in the start and end position of any field.
 * <p>
 * DontCareFieldPosition is a singleton, and its instance is immutable.
 * <p>
 * A <code>format</code> method use <code>fpos == DontCareFieldPosition.INSTANCE</code>
 * to tell whether or not it needs to calculate a field position.
 * @hide Only a subset of ICU is exposed in Android
 *
 */
public final class DontCareFieldPosition extends FieldPosition {
    
    public static final DontCareFieldPosition INSTANCE = new DontCareFieldPosition(); 
    
    private DontCareFieldPosition() {
        // Pick some random number to be sure that we don't accidentally match with
        // a field.
        super(-913028704);
    }
    
    @Override
    public void setBeginIndex(int i) {
        // Do nothing
    }
    
    @Override
    public void setEndIndex(int i) {
        // Do nothing
    }

}
