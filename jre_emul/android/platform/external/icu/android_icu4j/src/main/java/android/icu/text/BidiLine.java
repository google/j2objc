/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2001-2014, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/
/* Written by Simon Montagu, Matitiahu Allouche
 * (ported from C code written by Markus W. Scherer)
 */

package android.icu.text;


import java.util.Arrays;

final class BidiLine {

    /*
     * General remarks about the functions in this file:
     *
     * These functions deal with the aspects of potentially mixed-directional
     * text in a single paragraph or in a line of a single paragraph
     * which has already been processed according to
     * the Unicode 3.0 Bidi algorithm as defined in
     * http://www.unicode.org/unicode/reports/tr9/ , version 13,
     * also described in The Unicode Standard, Version 4.0.1 .
     *
     * This means that there is a Bidi object with a levels
     * and a dirProps array.
     * paraLevel and direction are also set.
     * Only if the length of the text is zero, then levels==dirProps==NULL.
     *
     * The overall directionality of the paragraph
     * or line is used to bypass the reordering steps if possible.
     * Even purely RTL text does not need reordering there because
     * the getLogical/VisualIndex() methods can compute the
     * index on the fly in such a case.
     *
     * The implementation of the access to same-level-runs and of the reordering
     * do attempt to provide better performance and less memory usage compared to
     * a direct implementation of especially rule (L2) with an array of
     * one (32-bit) integer per text character.
     *
     * Here, the levels array is scanned as soon as necessary, and a vector of
     * same-level-runs is created. Reordering then is done on this vector.
     * For each run of text positions that were resolved to the same level,
     * only 8 bytes are stored: the first text position of the run and the visual
     * position behind the run after reordering.
     * One sign bit is used to hold the directionality of the run.
     * This is inefficient if there are many very short runs. If the average run
     * length is <2, then this uses more memory.
     *
     * In a further attempt to save memory, the levels array is never changed
     * after all the resolution rules (Xn, Wn, Nn, In).
     * Many methods have to consider the field trailingWSStart:
     * if it is less than length, then there is an implicit trailing run
     * at the paraLevel,
     * which is not reflected in the levels array.
     * This allows a line Bidi object to use the same levels array as
     * its paragraph parent object.
     *
     * When a Bidi object is created for a line of a paragraph, then the
     * paragraph's levels and dirProps arrays are reused by way of setting
     * a pointer into them, not by copying. This again saves memory and forbids to
     * change the now shared levels for (L1).
     */

    /* handle trailing WS (L1) -------------------------------------------------- */

    /*
     * setTrailingWSStart() sets the start index for a trailing
     * run of WS in the line. This is necessary because we do not modify
     * the paragraph's levels array that we just point into.
     * Using trailingWSStart is another form of performing (L1).
     *
     * To make subsequent operations easier, we also include the run
     * before the WS if it is at the paraLevel - we merge the two here.
     *
     * This method is called only from setLine(), so paraLevel is
     * set correctly for the line even when contextual multiple paragraphs.
     */

    static void setTrailingWSStart(Bidi bidi)
    {
        byte[] dirProps = bidi.dirProps;
        byte[] levels = bidi.levels;
        int start = bidi.length;
        byte paraLevel = bidi.paraLevel;

        /* If the line is terminated by a block separator, all preceding WS etc...
           are already set to paragraph level.
           Setting trailingWSStart to pBidi->length will avoid changing the
           level of B chars from 0 to paraLevel in getLevels when
           orderParagraphsLTR==TRUE
        */
        if (dirProps[start - 1] == Bidi.B) {
            bidi.trailingWSStart = start;   /* currently == bidi.length */
            return;
        }
        /* go backwards across all WS, BN, explicit codes */
        while (start > 0 &&
                (Bidi.DirPropFlag(dirProps[start - 1]) & Bidi.MASK_WS) != 0) {
            --start;
        }

        /* if the WS run can be merged with the previous run then do so here */
        while (start > 0 && levels[start - 1] == paraLevel) {
            --start;
        }

        bidi.trailingWSStart=start;
    }

    static Bidi setLine(Bidi paraBidi, int start, int limit) {
        int length;

        Bidi lineBidi = new Bidi();

        /* set the values in lineBidi from its paraBidi parent */
        /* class members are already initialized to 0 */
        // lineBidi.paraBidi = null;        /* mark unfinished setLine */
        // lineBidi.flags = 0;
        // lineBidi.controlCount = 0;

        length = lineBidi.length = lineBidi.originalLength =
                lineBidi.resultLength = limit - start;

        lineBidi.text = new char[length];
        System.arraycopy(paraBidi.text, start, lineBidi.text, 0, length);
        lineBidi.paraLevel = paraBidi.GetParaLevelAt(start);
        lineBidi.paraCount = paraBidi.paraCount;
        lineBidi.runs = new BidiRun[0];
        lineBidi.reorderingMode = paraBidi.reorderingMode;
        lineBidi.reorderingOptions = paraBidi.reorderingOptions;
        if (paraBidi.controlCount > 0) {
            int j;
            for (j = start; j < limit; j++) {
                if (Bidi.IsBidiControlChar(paraBidi.text[j])) {
                    lineBidi.controlCount++;
                }
            }
            lineBidi.resultLength -= lineBidi.controlCount;
        }
        /* copy proper subset of DirProps */
        lineBidi.getDirPropsMemory(length);
        lineBidi.dirProps = lineBidi.dirPropsMemory;
        System.arraycopy(paraBidi.dirProps, start, lineBidi.dirProps, 0,
                         length);
        /* copy proper subset of Levels */
        lineBidi.getLevelsMemory(length);
        lineBidi.levels = lineBidi.levelsMemory;
        System.arraycopy(paraBidi.levels, start, lineBidi.levels, 0,
                         length);
        lineBidi.runCount = -1;

        if (paraBidi.direction != Bidi.MIXED) {
            /* the parent is already trivial */
            lineBidi.direction = paraBidi.direction;

            /*
             * The parent's levels are all either
             * implicitly or explicitly ==paraLevel;
             * do the same here.
             */
            if (paraBidi.trailingWSStart <= start) {
                lineBidi.trailingWSStart = 0;
            } else if (paraBidi.trailingWSStart < limit) {
                lineBidi.trailingWSStart = paraBidi.trailingWSStart - start;
            } else {
                lineBidi.trailingWSStart = length;
            }
        } else {
            byte[] levels = lineBidi.levels;
            int i, trailingWSStart;
            byte level;

            setTrailingWSStart(lineBidi);
            trailingWSStart = lineBidi.trailingWSStart;

            /* recalculate lineBidi.direction */
            if (trailingWSStart == 0) {
                /* all levels are at paraLevel */
                lineBidi.direction = (byte)(lineBidi.paraLevel & 1);
            } else {
                /* get the level of the first character */
                level = (byte)(levels[0] & 1);

                /* if there is anything of a different level, then the line
                   is mixed */
                if (trailingWSStart < length &&
                    (lineBidi.paraLevel & 1) != level) {
                    /* the trailing WS is at paraLevel, which differs from
                       levels[0] */
                    lineBidi.direction = Bidi.MIXED;
                } else {
                    /* see if levels[1..trailingWSStart-1] have the same
                       direction as levels[0] and paraLevel */
                    for (i = 1; ; i++) {
                        if (i == trailingWSStart) {
                            /* the direction values match those in level */
                            lineBidi.direction = level;
                            break;
                        } else if ((levels[i] & 1) != level) {
                            lineBidi.direction = Bidi.MIXED;
                            break;
                        }
                    }
                }
            }

            switch(lineBidi.direction) {
                case Bidi.DIRECTION_LEFT_TO_RIGHT:
                    /* make sure paraLevel is even */
                    lineBidi.paraLevel = (byte)
                        ((lineBidi.paraLevel + 1) & ~1);

                    /* all levels are implicitly at paraLevel (important for
                       getLevels()) */
                    lineBidi.trailingWSStart = 0;
                    break;
                case Bidi.DIRECTION_RIGHT_TO_LEFT:
                    /* make sure paraLevel is odd */
                    lineBidi.paraLevel |= 1;

                    /* all levels are implicitly at paraLevel (important for
                       getLevels()) */
                    lineBidi.trailingWSStart = 0;
                    break;
                default:
                    break;
            }
        }
        lineBidi.paraBidi = paraBidi;     /* mark successful setLine */
        return lineBidi;
    }

    static byte getLevelAt(Bidi bidi, int charIndex)
    {
        /* return paraLevel if in the trailing WS run, otherwise the real level */
        if (bidi.direction != Bidi.MIXED || charIndex >= bidi.trailingWSStart) {
            return bidi.GetParaLevelAt(charIndex);
        } else {
            return bidi.levels[charIndex];
        }
    }

    static byte[] getLevels(Bidi bidi)
    {
        int start = bidi.trailingWSStart;
        int length = bidi.length;

        if (start != length) {
            /* the current levels array does not reflect the WS run */
            /*
             * After the previous if(), we know that the levels array
             * has an implicit trailing WS run and therefore does not fully
             * reflect itself all the levels.
             * This must be a Bidi object for a line, and
             * we need to create a new levels array.
             */
            /* bidi.paraLevel is ok even if contextual multiple paragraphs,
               since bidi is a line object                                     */
            Arrays.fill(bidi.levels, start, length, bidi.paraLevel);

            /* this new levels array is set for the line and reflects the WS run */
            bidi.trailingWSStart = length;
        }
        if (length < bidi.levels.length) {
            byte[] levels = new byte[length];
            System.arraycopy(bidi.levels, 0, levels, 0, length);
            return levels;
        }
        return bidi.levels;
    }

    static BidiRun getLogicalRun(Bidi bidi, int logicalPosition)
    {
        /* this is done based on runs rather than on levels since levels have
           a special interpretation when REORDER_RUNS_ONLY
         */
        BidiRun newRun = new BidiRun(), iRun;
        getRuns(bidi);
        int runCount = bidi.runCount;
        int visualStart = 0, logicalLimit = 0;
        iRun = bidi.runs[0];

        for (int i = 0; i < runCount; i++) {
            iRun = bidi.runs[i];
            logicalLimit = iRun.start + iRun.limit - visualStart;
            if ((logicalPosition >= iRun.start) &&
                (logicalPosition < logicalLimit)) {
                break;
            }
            visualStart = iRun.limit;
        }
        newRun.start = iRun.start;
        newRun.limit = logicalLimit;
        newRun.level = iRun.level;
        return newRun;
    }

    static BidiRun getVisualRun(Bidi bidi, int runIndex)
    {
        int start = bidi.runs[runIndex].start;
        int limit;
        byte level = bidi.runs[runIndex].level;

        if (runIndex > 0) {
            limit = start +
                    bidi.runs[runIndex].limit -
                    bidi.runs[runIndex - 1].limit;
        } else {
            limit = start + bidi.runs[0].limit;
        }
        return new BidiRun(start, limit, level);
    }

    /* in trivial cases there is only one trivial run; called by getRuns() */
    static void getSingleRun(Bidi bidi, byte level) {
        /* simple, single-run case */
        bidi.runs = bidi.simpleRuns;
        bidi.runCount = 1;

        /* fill and reorder the single run */
        bidi.runs[0] = new BidiRun(0, bidi.length, level);
    }

    /* reorder the runs array (L2) ---------------------------------------------- */

    /*
     * Reorder the same-level runs in the runs array.
     * Here, runCount>1 and maxLevel>=minLevel>=paraLevel.
     * All the visualStart fields=logical start before reordering.
     * The "odd" bits are not set yet.
     *
     * Reordering with this data structure lends itself to some handy shortcuts:
     *
     * Since each run is moved but not modified, and since at the initial maxLevel
     * each sequence of same-level runs consists of only one run each, we
     * don't need to do anything there and can predecrement maxLevel.
     * In many simple cases, the reordering is thus done entirely in the
     * index mapping.
     * Also, reordering occurs only down to the lowest odd level that occurs,
     * which is minLevel|1. However, if the lowest level itself is odd, then
     * in the last reordering the sequence of the runs at this level or higher
     * will be all runs, and we don't need the elaborate loop to search for them.
     * This is covered by ++minLevel instead of minLevel|=1 followed
     * by an extra reorder-all after the reorder-some loop.
     * About a trailing WS run:
     * Such a run would need special treatment because its level is not
     * reflected in levels[] if this is not a paragraph object.
     * Instead, all characters from trailingWSStart on are implicitly at
     * paraLevel.
     * However, for all maxLevel>paraLevel, this run will never be reordered
     * and does not need to be taken into account. maxLevel==paraLevel is only reordered
     * if minLevel==paraLevel is odd, which is done in the extra segment.
     * This means that for the main reordering loop we don't need to consider
     * this run and can --runCount. If it is later part of the all-runs
     * reordering, then runCount is adjusted accordingly.
     */
    private static void reorderLine(Bidi bidi, byte minLevel, byte maxLevel) {

        /* nothing to do? */
        if (maxLevel<=(minLevel|1)) {
            return;
        }

        BidiRun[] runs;
        BidiRun tempRun;
        byte[] levels;
        int firstRun, endRun, limitRun, runCount;

        /*
         * Reorder only down to the lowest odd level
         * and reorder at an odd minLevel in a separate, simpler loop.
         * See comments above for why minLevel is always incremented.
         */
        ++minLevel;

        runs = bidi.runs;
        levels = bidi.levels;
        runCount = bidi.runCount;

        /* do not include the WS run at paraLevel<=old minLevel except in the simple loop */
        if (bidi.trailingWSStart < bidi.length) {
            --runCount;
        }

        while (--maxLevel >= minLevel) {
            firstRun = 0;

            /* loop for all sequences of runs */
            for ( ; ; ) {
                /* look for a sequence of runs that are all at >=maxLevel */
                /* look for the first run of such a sequence */
                while (firstRun < runCount && levels[runs[firstRun].start] < maxLevel) {
                    ++firstRun;
                }
                if (firstRun >= runCount) {
                    break;  /* no more such runs */
                }

                /* look for the limit run of such a sequence (the run behind it) */
                for (limitRun = firstRun; ++limitRun < runCount &&
                      levels[runs[limitRun].start]>=maxLevel; ) {}

                /* Swap the entire sequence of runs from firstRun to limitRun-1. */
                endRun = limitRun - 1;
                while (firstRun < endRun) {
                    tempRun = runs[firstRun];
                    runs[firstRun] = runs[endRun];
                    runs[endRun] = tempRun;
                    ++firstRun;
                    --endRun;
                }

                if (limitRun == runCount) {
                    break;  /* no more such runs */
                } else {
                    firstRun = limitRun + 1;
                }
            }
        }

        /* now do maxLevel==old minLevel (==odd!), see above */
        if ((minLevel & 1) == 0) {
            firstRun = 0;

            /* include the trailing WS run in this complete reordering */
            if (bidi.trailingWSStart == bidi.length) {
                --runCount;
            }

            /* Swap the entire sequence of all runs. (endRun==runCount) */
            while (firstRun < runCount) {
                tempRun = runs[firstRun];
                runs[firstRun] = runs[runCount];
                runs[runCount] = tempRun;
                ++firstRun;
                --runCount;
            }
        }
    }

    /* compute the runs array --------------------------------------------------- */

    static int getRunFromLogicalIndex(Bidi bidi, int logicalIndex) {
        BidiRun[] runs = bidi.runs;
        int runCount = bidi.runCount, visualStart = 0, i, length, logicalStart;

        for (i = 0; i < runCount; i++) {
            length = runs[i].limit - visualStart;
            logicalStart = runs[i].start;
            if ((logicalIndex >= logicalStart) && (logicalIndex < (logicalStart+length))) {
                return i;
            }
            visualStart += length;
        }
        ///CLOVER:OFF
        /* we should never get here */
        throw new IllegalStateException("Internal ICU error in getRunFromLogicalIndex");
        ///CLOVER:ON
    }

    /*
     * Compute the runs array from the levels array.
     * After getRuns() returns true, runCount is guaranteed to be >0
     * and the runs are reordered.
     * Odd-level runs have visualStart on their visual right edge and
     * they progress visually to the left.
     * If option OPTION_INSERT_MARKS is set, insertRemove will contain the
     * sum of appropriate LRM/RLM_BEFORE/AFTER flags.
     * If option OPTION_REMOVE_CONTROLS is set, insertRemove will contain the
     * negative number of BiDi control characters within this run.
     */
    static void getRuns(Bidi bidi) {
        /*
         * This method returns immediately if the runs are already set. This
         * includes the case of length==0 (handled in setPara)..
         */
        if (bidi.runCount >= 0) {
            return;
        }
        if (bidi.direction != Bidi.MIXED) {
            /* simple, single-run case - this covers length==0 */
            /* bidi.paraLevel is ok even for contextual multiple paragraphs */
            getSingleRun(bidi, bidi.paraLevel);
        } else /* Bidi.MIXED, length>0 */ {
            /* mixed directionality */
            int length = bidi.length, limit;
            byte[] levels = bidi.levels;
            int i, runCount;
            byte level = -1;    /* initialize with no valid level */
            /*
             * If there are WS characters at the end of the line
             * and the run preceding them has a level different from
             * paraLevel, then they will form their own run at paraLevel (L1).
             * Count them separately.
             * We need some special treatment for this in order to not
             * modify the levels array which a line Bidi object shares
             * with its paragraph parent and its other line siblings.
             * In other words, for the trailing WS, it may be
             * levels[]!=paraLevel but we have to treat it like it were so.
             */
            limit = bidi.trailingWSStart;
            /* count the runs, there is at least one non-WS run, and limit>0 */
            runCount = 0;
            for (i = 0; i < limit; ++i) {
                /* increment runCount at the start of each run */
                if (levels[i] != level) {
                    ++runCount;
                    level = levels[i];
                }
            }

            /*
             * We don't need to see if the last run can be merged with a trailing
             * WS run because setTrailingWSStart() would have done that.
             */
            if (runCount == 1 && limit == length) {
                /* There is only one non-WS run and no trailing WS-run. */
                getSingleRun(bidi, levels[0]);
            } else /* runCount>1 || limit<length */ {
                /* allocate and set the runs */
                BidiRun[] runs;
                int runIndex, start;
                byte minLevel = Bidi.MAX_EXPLICIT_LEVEL + 1;
                byte maxLevel=0;

                /* now, count a (non-mergeable) WS run */
                if (limit < length) {
                    ++runCount;
                }

                /* runCount > 1 */
                bidi.getRunsMemory(runCount);
                runs = bidi.runsMemory;

                /* set the runs */
                /* FOOD FOR THOUGHT: this could be optimized, e.g.:
                 * 464->444, 484->444, 575->555, 595->555
                 * However, that would take longer. Check also how it would
                 * interact with BiDi control removal and inserting Marks.
                 */
                runIndex = 0;

                /* search for the run limits and initialize visualLimit values with the run lengths */
                i = 0;
                do {
                    /* prepare this run */
                    start = i;
                    level = levels[i];
                    if (level < minLevel) {
                        minLevel = level;
                    }
                    if (level > maxLevel) {
                        maxLevel = level;
                    }

                    /* look for the run limit */
                    while (++i < limit && levels[i] == level) {}

                    /* i is another run limit */
                    runs[runIndex] = new BidiRun(start, i - start, level);
                    ++runIndex;
                } while (i < limit);

                if (limit < length) {
                    /* there is a separate WS run */
                    runs[runIndex] = new BidiRun(limit, length - limit, bidi.paraLevel);
                    /* For the trailing WS run, bidi.paraLevel is ok even
                       if contextual multiple paragraphs.                   */
                    if (bidi.paraLevel < minLevel) {
                        minLevel = bidi.paraLevel;
                    }
                }

                /* set the object fields */
                bidi.runs = runs;
                bidi.runCount = runCount;

                reorderLine(bidi, minLevel, maxLevel);

                /* now add the direction flags and adjust the visualLimit's to be just that */
                /* this loop will also handle the trailing WS run */
                limit = 0;
                for (i = 0; i < runCount; ++i) {
                    runs[i].level = levels[runs[i].start];
                    limit = (runs[i].limit += limit);
                }

                /* Set the embedding level for the trailing WS run. */
                /* For a RTL paragraph, it will be the *first* run in visual order. */
                /* For the trailing WS run, bidi.paraLevel is ok even if
                   contextual multiple paragraphs.                          */
                if (runIndex < runCount) {
                    int trailingRun = ((bidi.paraLevel & 1) != 0)? 0 : runIndex;
                    runs[trailingRun].level = bidi.paraLevel;
                }
            }
        }

        /* handle insert LRM/RLM BEFORE/AFTER run */
        if (bidi.insertPoints.size > 0) {
            Bidi.Point point;
            int runIndex, ip;
            for (ip = 0; ip < bidi.insertPoints.size; ip++) {
                point = bidi.insertPoints.points[ip];
                runIndex = getRunFromLogicalIndex(bidi, point.pos);
                bidi.runs[runIndex].insertRemove |= point.flag;
            }
        }

        /* handle remove BiDi control characters */
        if (bidi.controlCount > 0) {
            int runIndex, ic;
            char c;
            for (ic = 0; ic < bidi.length; ic++) {
                c = bidi.text[ic];
                if (Bidi.IsBidiControlChar(c)) {
                    runIndex = getRunFromLogicalIndex(bidi, ic);
                    bidi.runs[runIndex].insertRemove--;
                }
            }
        }
    }

    static int[] prepareReorder(byte[] levels, byte[] pMinLevel, byte[] pMaxLevel)
    {
        int start;
        byte level, minLevel, maxLevel;

        if (levels == null || levels.length <= 0) {
            return null;
        }

        /* determine minLevel and maxLevel */
        minLevel = Bidi.MAX_EXPLICIT_LEVEL + 1;
        maxLevel = 0;
        for (start = levels.length; start>0; ) {
            level = levels[--start];
            if (level < 0) {
                return null;
            }
           if (level > (Bidi.MAX_EXPLICIT_LEVEL + 1)) {
                return null;
            }
            if (level < minLevel) {
                minLevel = level;
            }
            if (level > maxLevel) {
                maxLevel = level;
            }
        }
        pMinLevel[0] = minLevel;
        pMaxLevel[0] = maxLevel;

        /* initialize the index map */
        int[] indexMap = new int[levels.length];
        for (start = levels.length; start > 0; ) {
            --start;
            indexMap[start] = start;
        }

        return indexMap;
    }

    static int[] reorderLogical(byte[] levels)
    {
        byte[] aMinLevel = new byte[1];
        byte[] aMaxLevel = new byte[1];
        int start, limit, sumOfSosEos;
        byte minLevel, maxLevel;
        int[] indexMap = prepareReorder(levels, aMinLevel, aMaxLevel);
        if (indexMap == null) {
            return null;
        }

        minLevel = aMinLevel[0];
        maxLevel = aMaxLevel[0];

        /* nothing to do? */
        if (minLevel == maxLevel && (minLevel & 1) == 0) {
            return indexMap;
        }

        /* reorder only down to the lowest odd level */
        minLevel |= 1;

        /* loop maxLevel..minLevel */
        do {
            start = 0;

            /* loop for all sequences of levels to reorder at the current maxLevel */
            for ( ; ; ) {
                /* look for a sequence of levels that are all at >=maxLevel */
                /* look for the first index of such a sequence */
                while (start < levels.length && levels[start] < maxLevel) {
                    ++start;
                }
                if (start >= levels.length) {
                    break;  /* no more such sequences */
                }

                /* look for the limit of such a sequence (the index behind it) */
                for (limit = start; ++limit < levels.length && levels[limit] >= maxLevel; ) {}

                /*
                 * sos=start of sequence, eos=end of sequence
                 *
                 * The closed (inclusive) interval from sos to eos includes all the logical
                 * and visual indexes within this sequence. They are logically and
                 * visually contiguous and in the same range.
                 *
                 * For each run, the new visual index=sos+eos-old visual index;
                 * we pre-add sos+eos into sumOfSosEos ->
                 * new visual index=sumOfSosEos-old visual index;
                 */
                sumOfSosEos = start + limit - 1;

                /* reorder each index in the sequence */
                do {
                    indexMap[start] = sumOfSosEos - indexMap[start];
                } while (++start < limit);

                /* start==limit */
                if (limit == levels.length) {
                    break;  /* no more such sequences */
                } else {
                    start = limit + 1;
                }
            }
        } while (--maxLevel >= minLevel);
        return indexMap;
    }

    static int[] reorderVisual(byte[] levels)
    {
        byte[] aMinLevel = new byte[1];
        byte[] aMaxLevel = new byte[1];
        int start, end, limit, temp;
        byte minLevel, maxLevel;

        int[] indexMap = prepareReorder(levels, aMinLevel, aMaxLevel);
        if (indexMap == null) {
            return null;
        }

        minLevel = aMinLevel[0];
        maxLevel = aMaxLevel[0];

        /* nothing to do? */
        if (minLevel == maxLevel && (minLevel & 1) == 0) {
            return indexMap;
        }

        /* reorder only down to the lowest odd level */
        minLevel |= 1;

        /* loop maxLevel..minLevel */
        do {
            start = 0;

            /* loop for all sequences of levels to reorder at the current maxLevel */
            for ( ; ; ) {
                /* look for a sequence of levels that are all at >=maxLevel */
                /* look for the first index of such a sequence */
                while (start < levels.length && levels[start] < maxLevel) {
                    ++start;
                }
                if (start >= levels.length) {
                    break;  /* no more such runs */
                }

                /* look for the limit of such a sequence (the index behind it) */
                for (limit = start; ++limit < levels.length && levels[limit] >= maxLevel; ) {}

                /*
                 * Swap the entire interval of indexes from start to limit-1.
                 * We don't need to swap the levels for the purpose of this
                 * algorithm: the sequence of levels that we look at does not
                 * move anyway.
                 */
                end = limit - 1;
                while (start < end) {
                    temp = indexMap[start];
                    indexMap[start] = indexMap[end];
                    indexMap[end] = temp;

                    ++start;
                    --end;
                }

                if (limit == levels.length) {
                    break;  /* no more such sequences */
                } else {
                    start = limit + 1;
                }
            }
        } while (--maxLevel >= minLevel);

        return indexMap;
    }

    static int getVisualIndex(Bidi bidi, int logicalIndex)
    {
        int visualIndex = Bidi.MAP_NOWHERE;

        /* we can do the trivial cases without the runs array */
        switch(bidi.direction) {
        case Bidi.LTR:
            visualIndex = logicalIndex;
            break;
        case Bidi.RTL:
            visualIndex = bidi.length - logicalIndex - 1;
            break;
        default:
            getRuns(bidi);
            BidiRun[] runs = bidi.runs;
            int i, visualStart = 0, offset, length;

            /* linear search for the run, search on the visual runs */
            for (i = 0; i < bidi.runCount; ++i) {
                length = runs[i].limit - visualStart;
                offset = logicalIndex - runs[i].start;
                if (offset >= 0 && offset < length) {
                    if (runs[i].isEvenRun()) {
                        /* LTR */
                        visualIndex = visualStart + offset;
                    } else {
                        /* RTL */
                        visualIndex = visualStart + length - offset - 1;
                    }
                    break;                  /* exit for loop */
                }
                visualStart += length;
            }
            if (i >= bidi.runCount) {
                return Bidi.MAP_NOWHERE;
            }
        }

        if (bidi.insertPoints.size > 0) {
            /* add the number of added marks until the calculated visual index */
            BidiRun runs[] = bidi.runs;
            int i, length, insertRemove;
            int visualStart = 0, markFound = 0;
            for (i = 0; ; i++, visualStart += length) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                if ((insertRemove & (Bidi.LRM_BEFORE|Bidi.RLM_BEFORE)) > 0) {
                    markFound++;
                }
                /* is it the run containing the visual index? */
                if (visualIndex < runs[i].limit) {
                    return visualIndex + markFound;
                }
                if ((insertRemove & (Bidi.LRM_AFTER|Bidi.RLM_AFTER)) > 0) {
                    markFound++;
                }
            }
        }
        else if (bidi.controlCount > 0) {
            /* subtract the number of controls until the calculated visual index */
            BidiRun[] runs = bidi.runs;
            int i, j, start, limit, length, insertRemove;
            int visualStart = 0, controlFound = 0;
            char uchar = bidi.text[logicalIndex];
            /* is the logical index pointing to a control ? */
            if (Bidi.IsBidiControlChar(uchar)) {
                return Bidi.MAP_NOWHERE;
            }
            /* loop on runs */
            for (i = 0; ; i++, visualStart += length) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                /* calculated visual index is beyond this run? */
                if (visualIndex >= runs[i].limit) {
                    controlFound -= insertRemove;
                    continue;
                }
                /* calculated visual index must be within current run */
                if (insertRemove == 0) {
                    return visualIndex - controlFound;
                }
                if (runs[i].isEvenRun()) {
                    /* LTR: check from run start to logical index */
                    start = runs[i].start;
                    limit = logicalIndex;
                } else {
                    /* RTL: check from logical index to run end */
                    start = logicalIndex + 1;
                    limit = runs[i].start + length;
                }
                for (j = start; j < limit; j++) {
                    uchar = bidi.text[j];
                    if (Bidi.IsBidiControlChar(uchar)) {
                        controlFound++;
                    }
                }
                return visualIndex - controlFound;
            }
        }

        return visualIndex;
    }

    static int getLogicalIndex(Bidi bidi, int visualIndex)
    {
        BidiRun[] runs;
        int i, runCount, start;

        runs = bidi.runs;
        runCount = bidi.runCount;
        if (bidi.insertPoints.size > 0) {
            /* handle inserted LRM/RLM */
            int markFound = 0, insertRemove;
            int visualStart = 0, length;
            /* subtract number of marks until visual index */
            for (i = 0; ; i++, visualStart += length) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                if ((insertRemove & (Bidi.LRM_BEFORE|Bidi.RLM_BEFORE)) > 0) {
                    if (visualIndex <= (visualStart+markFound)) {
                        return Bidi.MAP_NOWHERE;
                    }
                    markFound++;
                }
                /* is adjusted visual index within this run? */
                if (visualIndex < (runs[i].limit + markFound)) {
                    visualIndex -= markFound;
                    break;
                }
                if ((insertRemove & (Bidi.LRM_AFTER|Bidi.RLM_AFTER)) > 0) {
                    if (visualIndex == (visualStart + length + markFound)) {
                        return Bidi.MAP_NOWHERE;
                    }
                    markFound++;
                }
            }
        }
        else if (bidi.controlCount > 0) {
            /* handle removed BiDi control characters */
            int controlFound = 0, insertRemove, length;
            int logicalStart, logicalEnd, visualStart = 0, j, k;
            char uchar;
            boolean evenRun;
            /* add number of controls until visual index */
            for (i = 0; ; i++, visualStart += length) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                /* is adjusted visual index beyond current run? */
                if (visualIndex >= (runs[i].limit - controlFound + insertRemove)) {
                    controlFound -= insertRemove;
                    continue;
                }
                /* adjusted visual index is within current run */
                if (insertRemove == 0) {
                    visualIndex += controlFound;
                    break;
                }
                /* count non-control chars until visualIndex */
                logicalStart = runs[i].start;
                evenRun = runs[i].isEvenRun();
                logicalEnd = logicalStart + length - 1;
                for (j = 0; j < length; j++) {
                    k= evenRun ? logicalStart+j : logicalEnd-j;
                    uchar = bidi.text[k];
                    if (Bidi.IsBidiControlChar(uchar)) {
                        controlFound++;
                    }
                    if ((visualIndex + controlFound) == (visualStart + j)) {
                        break;
                    }
                }
                visualIndex += controlFound;
                break;
            }
        }
        /* handle all cases */
        if (runCount <= 10) {
            /* linear search for the run */
            for (i = 0; visualIndex >= runs[i].limit; ++i) {}
        } else {
            /* binary search for the run */
            int begin = 0, limit = runCount;

            /* the middle if() is guaranteed to find the run, we don't need a loop limit */
            for ( ; ; ) {
                i = (begin + limit) >>> 1;
                if (visualIndex >= runs[i].limit) {
                    begin = i + 1;
                } else if (i==0 || visualIndex >= runs[i-1].limit) {
                    break;
                } else {
                    limit = i;
                }
            }
        }

        start= runs[i].start;
        if (runs[i].isEvenRun()) {
            /* LTR */
            /* the offset in runs[i] is visualIndex-runs[i-1].visualLimit */
            if (i > 0) {
                visualIndex -= runs[i - 1].limit;
            }
            return start + visualIndex;
        } else {
            /* RTL */
            return start + runs[i].limit - visualIndex - 1;
        }
    }

    static int[] getLogicalMap(Bidi bidi)
    {
        /* fill a logical-to-visual index map using the runs[] */
        BidiRun[] runs = bidi.runs;
        int logicalStart, visualStart, logicalLimit, visualLimit;
        int[] indexMap = new int[bidi.length];
        if (bidi.length > bidi.resultLength) {
            Arrays.fill(indexMap, Bidi.MAP_NOWHERE);
        }

        visualStart = 0;
        for (int j = 0; j < bidi.runCount; ++j) {
            logicalStart = runs[j].start;
            visualLimit = runs[j].limit;
            if (runs[j].isEvenRun()) {
                do { /* LTR */
                    indexMap[logicalStart++] = visualStart++;
                } while (visualStart < visualLimit);
            } else {
                logicalStart += visualLimit - visualStart;  /* logicalLimit */
                do { /* RTL */
                    indexMap[--logicalStart] = visualStart++;
                } while (visualStart < visualLimit);
            }
            /* visualStart==visualLimit; */
        }

        if (bidi.insertPoints.size > 0) {
            int markFound = 0, runCount = bidi.runCount;
            int length, insertRemove, i, j;
            runs = bidi.runs;
            visualStart = 0;
            /* add number of marks found until each index */
            for (i = 0; i < runCount; i++, visualStart += length) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                if ((insertRemove & (Bidi.LRM_BEFORE|Bidi.RLM_BEFORE)) > 0) {
                    markFound++;
                }
                if (markFound > 0) {
                    logicalStart = runs[i].start;
                    logicalLimit = logicalStart + length;
                    for (j = logicalStart; j < logicalLimit; j++) {
                        indexMap[j] += markFound;
                    }
                }
                if ((insertRemove & (Bidi.LRM_AFTER|Bidi.RLM_AFTER)) > 0) {
                    markFound++;
                }
            }
        }
        else if (bidi.controlCount > 0) {
            int controlFound = 0, runCount = bidi.runCount;
            int length, insertRemove, i, j, k;
            boolean evenRun;
            char uchar;
            runs = bidi.runs;
            visualStart = 0;
            /* subtract number of controls found until each index */
            for (i = 0; i < runCount; i++, visualStart += length) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                /* no control found within previous runs nor within this run */
                if ((controlFound - insertRemove) == 0) {
                    continue;
                }
                logicalStart = runs[i].start;
                evenRun = runs[i].isEvenRun();
                logicalLimit = logicalStart + length;
                /* if no control within this run */
                if (insertRemove == 0) {
                    for (j = logicalStart; j < logicalLimit; j++) {
                        indexMap[j] -= controlFound;
                    }
                    continue;
                }
                for (j = 0; j < length; j++) {
                    k = evenRun ? logicalStart + j : logicalLimit - j - 1;
                    uchar = bidi.text[k];
                    if (Bidi.IsBidiControlChar(uchar)) {
                        controlFound++;
                        indexMap[k] = Bidi.MAP_NOWHERE;
                        continue;
                    }
                    indexMap[k] -= controlFound;
                }
            }
        }
        return indexMap;
    }

    static int[] getVisualMap(Bidi bidi)
    {
        /* fill a visual-to-logical index map using the runs[] */
        BidiRun[] runs = bidi.runs;
        int logicalStart, visualStart, visualLimit;
        int allocLength = bidi.length > bidi.resultLength ? bidi.length
                                                          : bidi.resultLength;
        int[] indexMap = new int[allocLength];

        visualStart = 0;
        int idx = 0;
        for (int j = 0; j < bidi.runCount; ++j) {
            logicalStart = runs[j].start;
            visualLimit = runs[j].limit;
            if (runs[j].isEvenRun()) {
                do { /* LTR */
                    indexMap[idx++] = logicalStart++;
                } while (++visualStart < visualLimit);
            } else {
                logicalStart += visualLimit - visualStart;  /* logicalLimit */
                do { /* RTL */
                    indexMap[idx++] = --logicalStart;
                } while (++visualStart < visualLimit);
            }
            /* visualStart==visualLimit; */
        }

        if (bidi.insertPoints.size > 0) {
            int markFound = 0, runCount = bidi.runCount;
            int insertRemove, i, j, k;
            runs = bidi.runs;
            /* count all inserted marks */
            for (i = 0; i < runCount; i++) {
                insertRemove = runs[i].insertRemove;
                if ((insertRemove & (Bidi.LRM_BEFORE|Bidi.RLM_BEFORE)) > 0) {
                    markFound++;
                }
                if ((insertRemove & (Bidi.LRM_AFTER|Bidi.RLM_AFTER)) > 0) {
                    markFound++;
                }
            }
            /* move back indexes by number of preceding marks */
            k = bidi.resultLength;
            for (i = runCount - 1; i >= 0 && markFound > 0; i--) {
                insertRemove = runs[i].insertRemove;
                if ((insertRemove & (Bidi.LRM_AFTER|Bidi.RLM_AFTER)) > 0) {
                    indexMap[--k] = Bidi.MAP_NOWHERE;
                    markFound--;
                }
                visualStart = i > 0 ? runs[i-1].limit : 0;
                for (j = runs[i].limit - 1; j >= visualStart && markFound > 0; j--) {
                    indexMap[--k] = indexMap[j];
                }
                if ((insertRemove & (Bidi.LRM_BEFORE|Bidi.RLM_BEFORE)) > 0) {
                    indexMap[--k] = Bidi.MAP_NOWHERE;
                    markFound--;
                }
            }
        }
        else if (bidi.controlCount > 0) {
            int runCount = bidi.runCount, logicalEnd;
            int insertRemove, length, i, j, k, m;
            char uchar;
            boolean evenRun;
            runs = bidi.runs;
            visualStart = 0;
            /* move forward indexes by number of preceding controls */
            k = 0;
            for (i = 0; i < runCount; i++, visualStart += length) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                /* if no control found yet, nothing to do in this run */
                if ((insertRemove == 0) && (k == visualStart)) {
                    k += length;
                    continue;
                }
                /* if no control in this run */
                if (insertRemove == 0) {
                    visualLimit = runs[i].limit;
                    for (j = visualStart; j < visualLimit; j++) {
                        indexMap[k++] = indexMap[j];
                    }
                    continue;
                }
                logicalStart = runs[i].start;
                evenRun = runs[i].isEvenRun();
                logicalEnd = logicalStart + length - 1;
                for (j = 0; j < length; j++) {
                    m = evenRun ? logicalStart + j : logicalEnd - j;
                    uchar = bidi.text[m];
                    if (!Bidi.IsBidiControlChar(uchar)) {
                        indexMap[k++] = m;
                    }
                }
            }
        }
        if (allocLength == bidi.resultLength) {
            return indexMap;
        }
        int[] newMap = new int[bidi.resultLength];
        System.arraycopy(indexMap, 0, newMap, 0, bidi.resultLength);
        return newMap;
    }

    static int[] invertMap(int[] srcMap)
    {
        int srcLength = srcMap.length;
        int destLength = -1, count = 0, i, srcEntry;

        /* find highest value and count positive indexes in srcMap */
        for (i = 0; i < srcLength; i++) {
            srcEntry = srcMap[i];
            if (srcEntry > destLength) {
                destLength = srcEntry;
            }
            if (srcEntry >= 0) {
                count++;
            }
        }
        destLength++;           /* add 1 for origin 0 */
        int[] destMap = new int[destLength];
        if (count < destLength) {
            /* we must fill unmatched destMap entries with -1 */
            Arrays.fill(destMap, Bidi.MAP_NOWHERE);
        }
        for (i = 0; i < srcLength; i++) {
            srcEntry = srcMap[i];
            if (srcEntry >= 0) {
                destMap[srcEntry] = i;
            }
        }
        return destMap;
    }
}
