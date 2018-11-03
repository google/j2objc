/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.impl;

import java.text.CharacterIterator;

import android.icu.text.UTF16;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CharacterIteration {
    // disallow instantiation
    private CharacterIteration() { }

    // 32 bit Char value returned from when an iterator has run out of range.
    //     Positive value so fast case (not end, not surrogate) can be checked
    //     with a single test.
    public static final int DONE32 = 0x7fffffff;

    /**
     * Move the iterator forward to the next code point, and return that code point,
     *   leaving the iterator positioned at char returned.
     *   For Supplementary chars, the iterator is left positioned at the lead surrogate.
     * @param ci  The character iterator
     * @return    The next code point.
     */
    public static int next32(CharacterIterator ci) {
        // If the current position is at a surrogate pair, move to the trail surrogate
        //   which leaves it in position for underlying iterator's next() to work.
        int c = ci.current();
        if (c >= UTF16.LEAD_SURROGATE_MIN_VALUE && c<=UTF16.LEAD_SURROGATE_MAX_VALUE) {
            c = ci.next();   
            if (c<UTF16.TRAIL_SURROGATE_MIN_VALUE || c>UTF16.TRAIL_SURROGATE_MAX_VALUE) {
                ci.previous();   
            }
        }

        // For BMP chars, this next() is the real deal.
        c = ci.next();
        
        // If we might have a lead surrogate, we need to peak ahead to get the trail 
        //  even though we don't want to really be positioned there.
        if (c >= UTF16.LEAD_SURROGATE_MIN_VALUE) {
            c = nextTrail32(ci, c);   
        }
        
        if (c >= UTF16.SUPPLEMENTARY_MIN_VALUE && c != DONE32) {
            // We got a supplementary char.  Back the iterator up to the postion
            // of the lead surrogate.
            ci.previous();   
        }
        return c;
   }

    
    // Out-of-line portion of the in-line Next32 code.
    // The call site does an initial ci.next() and calls this function
    //    if the 16 bit value it gets is >= LEAD_SURROGATE_MIN_VALUE.
    // NOTE:  we leave the underlying char iterator positioned in the
    //        middle of a surrogate pair.  ci.next() will work correctly
    //        from there, but the ci.getIndex() will be wrong, and needs
    //        adjustment.
    public static int nextTrail32(CharacterIterator ci, int lead) {
        if (lead == CharacterIterator.DONE && ci.getIndex() >= ci.getEndIndex()) {
            return DONE32;
        }
        int retVal = lead;
        if (lead <= UTF16.LEAD_SURROGATE_MAX_VALUE) {
            char  cTrail = ci.next();
            if (UTF16.isTrailSurrogate(cTrail)) {
                retVal = ((lead  - UTF16.LEAD_SURROGATE_MIN_VALUE) << 10) +
                            (cTrail - UTF16.TRAIL_SURROGATE_MIN_VALUE) +
                            UTF16.SUPPLEMENTARY_MIN_VALUE;
            } else {
                ci.previous();
            }
        }
        return retVal;
    }
       
    public static int previous32(CharacterIterator ci) {
        if (ci.getIndex() <= ci.getBeginIndex()) {
            return DONE32;   
        }
        char trail = ci.previous();
        int retVal = trail;
        if (UTF16.isTrailSurrogate(trail) && ci.getIndex()>ci.getBeginIndex()) {
            char lead = ci.previous();
            if (UTF16.isLeadSurrogate(lead)) {
                retVal = (((int)lead  - UTF16.LEAD_SURROGATE_MIN_VALUE) << 10) +
                          ((int)trail - UTF16.TRAIL_SURROGATE_MIN_VALUE) +
                          UTF16.SUPPLEMENTARY_MIN_VALUE;
            } else {
                ci.next();
            }           
        }
        return retVal;
    }
   
    public static int current32(CharacterIterator ci) {
        char  lead   = ci.current();
        int   retVal = lead;
        if (retVal < UTF16.LEAD_SURROGATE_MIN_VALUE) {
            return retVal;   
        }
        if (UTF16.isLeadSurrogate(lead)) {
            int  trail = (int)ci.next();
            ci.previous();
            if (UTF16.isTrailSurrogate((char)trail)) {
                retVal = ((lead  - UTF16.LEAD_SURROGATE_MIN_VALUE) << 10) +
                         (trail - UTF16.TRAIL_SURROGATE_MIN_VALUE) +
                         UTF16.SUPPLEMENTARY_MIN_VALUE;
            }
         } else {
            if (lead == CharacterIterator.DONE) {
                if (ci.getIndex() >= ci.getEndIndex())   {
                    retVal = DONE32;   
                }
            }
         }
        return retVal;
    }
}
