/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2001-2016, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/
/* Written by Simon Montagu, Matitiahu Allouche
 * (ported from C code written by Markus W. Scherer)
 */

package android.icu.text;

/**
 * A BidiRun represents a sequence of characters at the same embedding level.
 * The Bidi algorithm decomposes a piece of text into sequences of characters
 * at the same embedding level, each such sequence is called a "run".
 *
 * <p>A BidiRun represents such a run by storing its essential properties,
 * but does not duplicate the characters which form the run.
 *
 * <p>The &quot;limit&quot; of the run is the position just after the
 * last character, i.e., one more than that position.
 *
 * <p>This class has no public constructor, and its members cannot be
 * modified by users.
 *
 * @see android.icu.text.Bidi
 * @hide Only a subset of ICU is exposed in Android
 */
public class BidiRun {

    int start;              /* first logical position of the run */
    int limit;              /* last visual position of the run +1 */
    int insertRemove;       /* if >0, flags for inserting LRM/RLM before/after run,
                               if <0, count of bidi controls within run            */
    byte level;

    /*
     * Default constructor
     *
     * Note that members start and limit of a run instance have different
     * meanings depending whether the run is part of the runs array of a Bidi
     * object, or if it is a reference returned by getVisualRun() or
     * getLogicalRun().
     * For a member of the runs array of a Bidi object,
     *   - start is the first logical position of the run in the source text.
     *   - limit is one after the last visual position of the run.
     * For a reference returned by getLogicalRun() or getVisualRun(),
     *   - start is the first logical position of the run in the source text.
     *   - limit is one after the last logical position of the run.
     */
    BidiRun()
    {
        this(0, 0, (byte)0);
    }

    /*
     * Constructor
     */
    BidiRun(int start, int limit, byte embeddingLevel)
    {
        this.start = start;
        this.limit = limit;
        this.level = embeddingLevel;
    }

    /*
     * Copy the content of a BidiRun instance
     */
    void copyFrom(BidiRun run)
    {
        this.start = run.start;
        this.limit = run.limit;
        this.level = run.level;
        this.insertRemove = run.insertRemove;
    }

    /**
     * Get the first logical position of the run in the source text
     */
    public int getStart()
    {
        return start;
    }

    /**
     * Get position of one character after the end of the run in the source text
     */
    public int getLimit()
    {
        return limit;
    }

    /**
     * Get length of run
     */
    public int getLength()
    {
        return limit - start;
    }

    /**
     * Get level of run
     */
    public byte getEmbeddingLevel()
    {
        return level;
    }

    /**
     * Check if run level is odd
     * @return true if the embedding level of this run is odd, i.e. it is a
     *  right-to-left run.
     */
    public boolean isOddRun()
    {
        return (level & 1) == 1;
    }

    /**
     * Check if run level is even
     * @return true if the embedding level of this run is even, i.e. it is a
     *  left-to-right run.
     */
    public boolean isEvenRun()
    {
        return (level & 1) == 0;
    }

    /**
     * Get direction of run
     */
    public byte getDirection()
    {
        return (byte)(level & 1);
    }

    /**
     * String to display run
     */
    @Override
    public String toString()
    {
        return "BidiRun " + start + " - " + limit + " @ " + level;
    }
}
