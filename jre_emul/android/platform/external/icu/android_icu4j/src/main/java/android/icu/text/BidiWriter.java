/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2001-2010, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/
/* Written by Simon Montagu, Matitiahu Allouche
 * (ported from C code written by Markus W. Scherer)
 */

package android.icu.text;

import android.icu.lang.UCharacter;

final class BidiWriter {

    /** Bidi control code points */
    static final char LRM_CHAR = 0x200e;
    static final char RLM_CHAR = 0x200f;
    static final int MASK_R_AL = (1 << UCharacter.RIGHT_TO_LEFT |
                                  1 << UCharacter.RIGHT_TO_LEFT_ARABIC);

    private static boolean IsCombining(int type)
    {
        return ((1<<type &
                (1<<UCharacter.NON_SPACING_MARK |
                 1<<UCharacter.COMBINING_SPACING_MARK |
                 1<<UCharacter.ENCLOSING_MARK)) != 0);
    }

    /*
     * When we have OUTPUT_REVERSE set on writeReordered(), then we
     * semantically write RTL runs in reverse and later reverse them again.
     * Instead, we actually write them in forward order to begin with.
     * However, if the RTL run was to be mirrored, we need to mirror here now
     * since the implicit second reversal must not do it.
     * It looks strange to do mirroring in LTR output, but it is only because
     * we are writing RTL output in reverse.
     */
    private static String doWriteForward(String src, int options) {
        /* optimize for several combinations of options */
        switch(options&(Bidi.REMOVE_BIDI_CONTROLS|Bidi.DO_MIRRORING)) {
        case 0: {
            /* simply return the LTR run */
            return src;
        }
        case Bidi.DO_MIRRORING: {
            StringBuffer dest = new StringBuffer(src.length());

            /* do mirroring */
            int i=0;
            int c;

            do {
                c = UTF16.charAt(src, i);
                i += UTF16.getCharCount(c);
                UTF16.append(dest, UCharacter.getMirror(c));
            } while(i < src.length());
            return dest.toString();
        }
        case Bidi.REMOVE_BIDI_CONTROLS: {
            StringBuilder dest = new StringBuilder(src.length());

            /* copy the LTR run and remove any Bidi control characters */
            int i = 0;
            char c;
            do {
                c = src.charAt(i++);
                if(!Bidi.IsBidiControlChar(c)) {
                    dest.append(c);
                }
            } while(i < src.length());
            return dest.toString();
        }
        default: {
            StringBuffer dest = new StringBuffer(src.length());

            /* remove Bidi control characters and do mirroring */
            int i = 0;
            int c;
            do {
                c = UTF16.charAt(src, i);
                i += UTF16.getCharCount(c);
                if(!Bidi.IsBidiControlChar(c)) {
                    UTF16.append(dest, UCharacter.getMirror(c));
                }
            } while(i < src.length());
            return dest.toString();
        }
        } /* end of switch */
    }

    private static String doWriteForward(char[] text, int start, int limit,
                                         int options)
    {
        return doWriteForward(new String(text, start, limit - start), options);
    }

    static String writeReverse(String src, int options) {
        /*
         * RTL run -
         *
         * RTL runs need to be copied to the destination in reverse order
         * of code points, not code units, to keep Unicode characters intact.
         *
         * The general strategy for this is to read the source text
         * in backward order, collect all code units for a code point
         * (and optionally following combining characters, see below),
         * and copy all these code units in ascending order
         * to the destination for this run.
         *
         * Several options request whether combining characters
         * should be kept after their base characters,
         * whether Bidi control characters should be removed, and
         * whether characters should be replaced by their mirror-image
         * equivalent Unicode characters.
         */
        StringBuffer dest = new StringBuffer(src.length());

        /* optimize for several combinations of options */
        switch (options &
                (Bidi.REMOVE_BIDI_CONTROLS |
                 Bidi.DO_MIRRORING |
                 Bidi.KEEP_BASE_COMBINING)) {

        case 0:
            /*
             * With none of the "complicated" options set, the destination
             * run will have the same length as the source run,
             * and there is no mirroring and no keeping combining characters
             * with their base characters.
             *
             * XXX: or dest = UTF16.reverse(new StringBuffer(src));
             */

            int srcLength = src.length();

            /* preserve character integrity */
            do {
                /* i is always after the last code unit known to need to be kept
                 *  in this segment */
                int i = srcLength;

                /* collect code units for one base character */
                srcLength -= UTF16.getCharCount(UTF16.charAt(src,
                                                             srcLength - 1));

                /* copy this base character */
                dest.append(src.substring(srcLength, i));
            } while(srcLength > 0);
            break;

        case Bidi.KEEP_BASE_COMBINING:
            /*
             * Here, too, the destination
             * run will have the same length as the source run,
             * and there is no mirroring.
             * We do need to keep combining characters with their base
             * characters.
             */
            srcLength = src.length();

            /* preserve character integrity */
            do {
                /* i is always after the last code unit known to need to be kept
                 *  in this segment */
                int c;
                int i = srcLength;

                /* collect code units and modifier letters for one base
                 * character */
                do {
                    c = UTF16.charAt(src, srcLength - 1);
                    srcLength -= UTF16.getCharCount(c);
                } while(srcLength > 0 && IsCombining(UCharacter.getType(c)));

                /* copy this "user character" */
                dest.append(src.substring(srcLength, i));
            } while(srcLength > 0);
            break;

        default:
            /*
             * With several "complicated" options set, this is the most
             * general and the slowest copying of an RTL run.
             * We will do mirroring, remove Bidi controls, and
             * keep combining characters with their base characters
             * as requested.
             */
            srcLength = src.length();

            /* preserve character integrity */
            do {
                /* i is always after the last code unit known to need to be kept
                 *  in this segment */
                int i = srcLength;

                /* collect code units for one base character */
                int c = UTF16.charAt(src, srcLength - 1);
                srcLength -= UTF16.getCharCount(c);
                if ((options & Bidi.KEEP_BASE_COMBINING) != 0) {
                    /* collect modifier letters for this base character */
                    while(srcLength > 0 && IsCombining(UCharacter.getType(c))) {
                        c = UTF16.charAt(src, srcLength - 1);
                        srcLength -= UTF16.getCharCount(c);
                    }
                }

                if ((options & Bidi.REMOVE_BIDI_CONTROLS) != 0 &&
                    Bidi.IsBidiControlChar(c)) {
                    /* do not copy this Bidi control character */
                    continue;
                }

                /* copy this "user character" */
                int j = srcLength;
                if((options & Bidi.DO_MIRRORING) != 0) {
                    /* mirror only the base character */
                    c = UCharacter.getMirror(c);
                    UTF16.append(dest, c);
                    j += UTF16.getCharCount(c);
                }
                dest.append(src.substring(j, i));
            } while(srcLength > 0);
            break;
        } /* end of switch */

        return dest.toString();
    }

    static String doWriteReverse(char[] text, int start, int limit, int options)
    {
        return writeReverse(new String(text, start, limit - start), options);
    }

    static String writeReordered(Bidi bidi, int options)
    {
        int run, runCount;
        StringBuilder dest;
        char[] text = bidi.text;
        runCount = bidi.countRuns();

        /*
         * Option "insert marks" implies Bidi.INSERT_LRM_FOR_NUMERIC if the
         * reordering mode (checked below) is appropriate.
         */
        if ((bidi.reorderingOptions & Bidi.OPTION_INSERT_MARKS) != 0) {
            options |= Bidi.INSERT_LRM_FOR_NUMERIC;
            options &= ~Bidi.REMOVE_BIDI_CONTROLS;
        }
        /*
         * Option "remove controls" implies Bidi.REMOVE_BIDI_CONTROLS
         * and cancels Bidi.INSERT_LRM_FOR_NUMERIC.
         */
        if ((bidi.reorderingOptions & Bidi.OPTION_REMOVE_CONTROLS) != 0) {
            options |= Bidi.REMOVE_BIDI_CONTROLS;
            options &= ~Bidi.INSERT_LRM_FOR_NUMERIC;
        }
        /*
         * If we do not perform the "inverse Bidi" algorithm, then we
         * don't need to insert any LRMs, and don't need to test for it.
         */
        if ((bidi.reorderingMode != Bidi.REORDER_INVERSE_NUMBERS_AS_L) &&
            (bidi.reorderingMode != Bidi.REORDER_INVERSE_LIKE_DIRECT)  &&
            (bidi.reorderingMode != Bidi.REORDER_INVERSE_FOR_NUMBERS_SPECIAL) &&
            (bidi.reorderingMode != Bidi.REORDER_RUNS_ONLY)) {
            options &= ~Bidi.INSERT_LRM_FOR_NUMERIC;
        }
        dest = new StringBuilder((options & Bidi.INSERT_LRM_FOR_NUMERIC) != 0 ?
                                 bidi.length * 2 : bidi.length);
        /*
         * Iterate through all visual runs and copy the run text segments to
         * the destination, according to the options.
         *
         * The tests for where to insert LRMs ignore the fact that there may be
         * BN codes or non-BMP code points at the beginning and end of a run;
         * they may insert LRMs unnecessarily but the tests are faster this way
         * (this would have to be improved for UTF-8).
         */
        if ((options & Bidi.OUTPUT_REVERSE) == 0) {
            /* forward output */
            if ((options & Bidi.INSERT_LRM_FOR_NUMERIC) == 0) {
                /* do not insert Bidi controls */
                for (run = 0; run < runCount; ++run) {
                    BidiRun bidiRun = bidi.getVisualRun(run);
                    if (bidiRun.isEvenRun()) {
                        dest.append(doWriteForward(text, bidiRun.start,
                                                   bidiRun.limit,
                                                   options & ~Bidi.DO_MIRRORING));
                     } else {
                        dest.append(doWriteReverse(text, bidiRun.start,
                                                   bidiRun.limit, options));
                     }
                }
            } else {
                /* insert Bidi controls for "inverse Bidi" */
                byte[] dirProps = bidi.dirProps;
                char uc;
                int markFlag;

                for (run = 0; run < runCount; ++run) {
                    BidiRun bidiRun = bidi.getVisualRun(run);
                    markFlag=0;
                    /* check if something relevant in insertPoints */
                    markFlag = bidi.runs[run].insertRemove;
                    if (markFlag < 0) { /* bidi controls count */
                        markFlag = 0;
                    }
                    if (bidiRun.isEvenRun()) {
                        if (bidi.isInverse() &&
                                dirProps[bidiRun.start] != Bidi.L) {
                            markFlag |= Bidi.LRM_BEFORE;
                        }
                        if ((markFlag & Bidi.LRM_BEFORE) != 0) {
                            uc = LRM_CHAR;
                        } else if ((markFlag & Bidi.RLM_BEFORE) != 0) {
                            uc = RLM_CHAR;
                        } else {
                            uc = 0;
                        }
                        if (uc != 0) {
                            dest.append(uc);
                        }
                        dest.append(doWriteForward(text,
                                                   bidiRun.start, bidiRun.limit,
                                                   options & ~Bidi.DO_MIRRORING));

                        if (bidi.isInverse() &&
                             dirProps[bidiRun.limit - 1] != Bidi.L) {
                            markFlag |= Bidi.LRM_AFTER;
                        }
                        if ((markFlag & Bidi.LRM_AFTER) != 0) {
                            uc = LRM_CHAR;
                        } else if ((markFlag & Bidi.RLM_AFTER) != 0) {
                            uc = RLM_CHAR;
                        } else {
                            uc = 0;
                        }
                        if (uc != 0) {
                            dest.append(uc);
                        }
                    } else { /* RTL run */
                        if (bidi.isInverse() &&
                            !bidi.testDirPropFlagAt(MASK_R_AL,
                                                    bidiRun.limit - 1)) {
                            markFlag |= Bidi.RLM_BEFORE;
                        }
                        if ((markFlag & Bidi.LRM_BEFORE) != 0) {
                            uc = LRM_CHAR;
                        } else if ((markFlag & Bidi.RLM_BEFORE) != 0) {
                            uc = RLM_CHAR;
                        } else {
                            uc = 0;
                        }
                        if (uc != 0) {
                            dest.append(uc);
                        }
                        dest.append(doWriteReverse(text, bidiRun.start,
                                                   bidiRun.limit, options));

                        if(bidi.isInverse() &&
                                (MASK_R_AL & Bidi.DirPropFlag(dirProps[bidiRun.start])) == 0) {
                            markFlag |= Bidi.RLM_AFTER;
                        }
                        if ((markFlag & Bidi.LRM_AFTER) != 0) {
                            uc = LRM_CHAR;
                        } else if ((markFlag & Bidi.RLM_AFTER) != 0) {
                            uc = RLM_CHAR;
                        } else {
                            uc = 0;
                        }
                        if (uc != 0) {
                            dest.append(uc);
                        }
                    }
                }
            }
        } else {
            /* reverse output */
            if((options & Bidi.INSERT_LRM_FOR_NUMERIC) == 0) {
                /* do not insert Bidi controls */
                for(run = runCount; --run >= 0; ) {
                    BidiRun bidiRun = bidi.getVisualRun(run);
                    if (bidiRun.isEvenRun()) {
                        dest.append(doWriteReverse(text,
                                                   bidiRun.start, bidiRun.limit,
                                                   options & ~Bidi.DO_MIRRORING));
                    } else {
                        dest.append(doWriteForward(text, bidiRun.start,
                                                   bidiRun.limit, options));
                    }
                }
            } else {
                /* insert Bidi controls for "inverse Bidi" */

                byte[] dirProps = bidi.dirProps;

                for (run = runCount; --run >= 0; ) {
                    /* reverse output */
                    BidiRun bidiRun = bidi.getVisualRun(run);
                    if (bidiRun.isEvenRun()) {
                        if (dirProps[bidiRun.limit - 1] != Bidi.L) {
                            dest.append(LRM_CHAR);
                        }

                        dest.append(doWriteReverse(text, bidiRun.start,
                                bidiRun.limit, options & ~Bidi.DO_MIRRORING));

                        if (dirProps[bidiRun.start] != Bidi.L) {
                            dest.append(LRM_CHAR);
                        }
                    } else {
                        if ((MASK_R_AL & Bidi.DirPropFlag(dirProps[bidiRun.start])) == 0) {
                            dest.append(RLM_CHAR);
                        }

                        dest.append(doWriteForward(text, bidiRun.start,
                                                   bidiRun.limit, options));

                        if ((MASK_R_AL & Bidi.DirPropFlag(dirProps[bidiRun.limit - 1])) == 0) {
                            dest.append(RLM_CHAR);
                        }
                    }
                }
            }
        }

        return dest.toString();
    }
}
