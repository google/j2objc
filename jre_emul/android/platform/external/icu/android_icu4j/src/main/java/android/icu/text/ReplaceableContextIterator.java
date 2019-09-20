/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*
*   Copyright (C) 2004-2010, International Business Machines
*   Corporation and others.  All Rights Reserved.
*
*******************************************************************************
*   file name:  ReplaceableContextIterator.java
*   encoding:   US-ASCII
*   tab size:   8 (not used)
*   indentation:4
*
*   created on: 2005feb04
*   created by: Markus W. Scherer
*
*   Implementation of UCaseProps.ContextIterator, iterates over a Replaceable.
*   Java port of casetrn.cpp/utrans_rep_caseContextIterator().
*/

package android.icu.text;

import android.icu.impl.UCaseProps;

/**
 * Implementation of UCaseProps.ContextIterator, iterates over a Replaceable.
 * See casetrn.cpp/utrans_rep_caseContextIterator().
 * See also UCharacter.StringContextIterator.
 */
class ReplaceableContextIterator implements UCaseProps.ContextIterator {
    /**
     * Constructor.
     * @param rep Replaceable to iterate over. 
     */
    ReplaceableContextIterator() {
        this.rep=null;
        limit=cpStart=cpLimit=index=contextStart=contextLimit=0;
        dir=0;
        reachedLimit=false;
    }

    /**
     * Set the text for iteration.
     * @param rep Iteration text.
     */
    public void setText(Replaceable rep) {
        this.rep=rep;
        limit=contextLimit=rep.length();
        cpStart=cpLimit=index=contextStart=0;
        dir=0;
        reachedLimit=false;
    }

    /**
     * Set the index where nextCaseMapCP() is to start iterating.
     * @param index Iteration start index for nextCaseMapCP().
     */
    public void setIndex(int index) {
        cpStart=cpLimit=index;
        this.index=0;
        dir=0;
        reachedLimit=false;
    }

    /**
     * Get the index of where the code point currently being case-mapped starts.
     * @return The start index of the current code point.
     */
    public int getCaseMapCPStart() {
        return cpStart;
    }

    /**
     * Set the iteration limit for nextCaseMapCP() to an index within the string.
     * If the limit parameter is negative or past the string, then the
     * string length is restored as the iteration limit.
     *
     * @param lim The iteration limit.
     */
    public void setLimit(int lim) {
        if(0<=lim && lim<=rep.length()) {
            limit=lim;
        } else {
            limit=rep.length();
        }
        reachedLimit=false;
    }

    /**
     * Set the start and limit indexes for context iteration with next().
     * @param contextStart Start of context for next().
     * @param contextLimit Limit of context for next().
     */
    public void setContextLimits(int contextStart, int contextLimit) {
        if(contextStart<0) {
            this.contextStart=0;
        } else if(contextStart<=rep.length()) {
            this.contextStart=contextStart;
        } else {
            this.contextStart=rep.length();
        }
        if(contextLimit<this.contextStart) {
            this.contextLimit=this.contextStart;
        } else if(contextLimit<=rep.length()) {
            this.contextLimit=contextLimit;
        } else {
            this.contextLimit=rep.length();
        }
        reachedLimit=false;
    }

    /**
     * Iterate forward through the string to fetch the next code point
     * to be case-mapped, and set the context indexes for it.
     *
     * @return The next code point to be case-mapped, or <0 when the iteration is done.
     */
    public int nextCaseMapCP() {
        int c;
        if(cpLimit<limit) {
            cpStart=cpLimit;
            c=rep.char32At(cpLimit);
            cpLimit+=UTF16.getCharCount(c);
            return c;
        } else {
            return -1;
        }
    }

    /**
     * Replace the current code point by its case mapping,
     * and update the indexes.
     *
     * @param text Replacement text.
     * @return The delta for the change of the text length.
     */
    public int replace(String text) {
        int delta=text.length()-(cpLimit-cpStart);
        rep.replace(cpStart, cpLimit, text);
        cpLimit+=delta;
        limit+=delta;
        contextLimit+=delta;
        return delta;
    }

    /**
     * Did forward context iteration with next() reach the iteration limit?
     * @return Boolean value.
     */
    public boolean didReachLimit() {
        return reachedLimit;
    }

    // implement UCaseProps.ContextIterator
    public void reset(int direction) {
        if(direction>0) {
            /* reset for forward iteration */
            this.dir=1;
            index=cpLimit;
        } else if(direction<0) {
            /* reset for backward iteration */
            this.dir=-1;
            index=cpStart;
        } else {
            // not a valid direction
            this.dir=0;
            index=0;
        }
        reachedLimit=false;
    }

    public int next() {
        int c;

        if(dir>0) {
            if(index<contextLimit) {
                c=rep.char32At(index);
                index+=UTF16.getCharCount(c);
                return c;
            } else {
                // forward context iteration reached the limit
                reachedLimit=true;
            }
        } else if(dir<0 && index>contextStart) {
            c=rep.char32At(index-1);
            index-=UTF16.getCharCount(c);
            return c;
        }
        return -1;
    }

    // variables
    protected Replaceable rep;
    protected int index, limit, cpStart, cpLimit, contextStart, contextLimit;
    protected int dir; // 0=initial state  >0=forward  <0=backward
    protected boolean reachedLimit;
}
