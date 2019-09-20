/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationRootElements.java, ported from collationrootelements.h/.cpp
*
* C++ version created on: 2013mar01
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

/**
 * Container and access methods for collation elements and weights
 * that occur in the root collator.
 * Needed for finding boundaries for building a tailoring.
 *
 * This class takes and returns 16-bit secondary and tertiary weights.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CollationRootElements {
    public CollationRootElements(long[] rootElements) {
        elements = rootElements;
    }

    /**
     * Higher than any root primary.
     */
    public static final long PRIMARY_SENTINEL = 0xffffff00L;

    /**
     * Flag in a root element, set if the element contains secondary & tertiary weights,
     * rather than a primary.
     */
    public static final int SEC_TER_DELTA_FLAG = 0x80;
    /**
     * Mask for getting the primary range step value from a primary-range-end element.
     */
    public static final int PRIMARY_STEP_MASK = 0x7f;

    /**
     * Index of the first CE with a non-zero tertiary weight.
     * Same as the start of the compact root elements table.
     */
    public static final int IX_FIRST_TERTIARY_INDEX = 0;
    /**
     * Index of the first CE with a non-zero secondary weight.
     */
    static final int IX_FIRST_SECONDARY_INDEX = 1;
    /**
     * Index of the first CE with a non-zero primary weight.
     */
    static final int IX_FIRST_PRIMARY_INDEX = 2;
    /**
     * Must match Collation.COMMON_SEC_AND_TER_CE.
     */
    static final int IX_COMMON_SEC_AND_TER_CE = 3;
    /**
     * Secondary & tertiary boundaries.
     * Bits 31..24: [fixed last secondary common byte 45]
     * Bits 23..16: [fixed first ignorable secondary byte 80]
     * Bits 15.. 8: reserved, 0
     * Bits  7.. 0: [fixed first ignorable tertiary byte 3C]
     */
    static final int IX_SEC_TER_BOUNDARIES = 4;
    /**
     * The current number of indexes.
     * Currently the same as elements[IX_FIRST_TERTIARY_INDEX].
     */
    static final int IX_COUNT = 5;

    /**
     * Returns the boundary between tertiary weights of primary/secondary CEs
     * and those of tertiary CEs.
     * This is the upper limit for tertiaries of primary/secondary CEs.
     * This minus one is the lower limit for tertiaries of tertiary CEs.
     */
    public int getTertiaryBoundary() {
        return ((int)elements[IX_SEC_TER_BOUNDARIES] << 8) & 0xff00;
    }

    /**
     * Returns the first assigned tertiary CE.
     */
    long getFirstTertiaryCE() {
        return elements[(int)elements[IX_FIRST_TERTIARY_INDEX]] & ~SEC_TER_DELTA_FLAG;
    }

    /**
     * Returns the last assigned tertiary CE.
     */
    long getLastTertiaryCE() {
        return elements[(int)elements[IX_FIRST_SECONDARY_INDEX] - 1] & ~SEC_TER_DELTA_FLAG;
    }

    /**
     * Returns the last common secondary weight.
     * This is the lower limit for secondaries of primary CEs.
     */
    public int getLastCommonSecondary() {
        return ((int)elements[IX_SEC_TER_BOUNDARIES] >> 16) & 0xff00;
    }

    /**
     * Returns the boundary between secondary weights of primary CEs
     * and those of secondary CEs.
     * This is the upper limit for secondaries of primary CEs.
     * This minus one is the lower limit for secondaries of secondary CEs.
     */
    public int getSecondaryBoundary() {
        return ((int)elements[IX_SEC_TER_BOUNDARIES] >> 8) & 0xff00;
    }

    /**
     * Returns the first assigned secondary CE.
     */
    long getFirstSecondaryCE() {
        return elements[(int)elements[IX_FIRST_SECONDARY_INDEX]] & ~SEC_TER_DELTA_FLAG;
    }

    /**
     * Returns the last assigned secondary CE.
     */
    long getLastSecondaryCE() {
        return elements[(int)elements[IX_FIRST_PRIMARY_INDEX] - 1] & ~SEC_TER_DELTA_FLAG;
    }

    /**
     * Returns the first assigned primary weight.
     */
    long getFirstPrimary() {
        return elements[(int)elements[IX_FIRST_PRIMARY_INDEX]];  // step=0: cannot be a range end
    }

    /**
     * Returns the first assigned primary CE.
     */
    long getFirstPrimaryCE() {
        return Collation.makeCE(getFirstPrimary());
    }

    /**
     * Returns the last root CE with a primary weight before p.
     * Intended only for reordering group boundaries.
     */
    long lastCEWithPrimaryBefore(long p) {
        if(p == 0) { return 0; }
        assert(p > elements[(int)elements[IX_FIRST_PRIMARY_INDEX]]);
        int index = findP(p);
        long q = elements[index];
        long secTer;
        if(p == (q & 0xffffff00L)) {
            // p == elements[index] is a root primary. Find the CE before it.
            // We must not be in a primary range.
            assert((q & PRIMARY_STEP_MASK) == 0);
            secTer = elements[index - 1];
            if((secTer & SEC_TER_DELTA_FLAG) == 0) {
                // Primary CE just before p.
                p = secTer & 0xffffff00L;
                secTer = Collation.COMMON_SEC_AND_TER_CE;
            } else {
                // secTer = last secondary & tertiary for the previous primary
                index -= 2;
                for(;;) {
                    p = elements[index];
                    if((p & SEC_TER_DELTA_FLAG) == 0) {
                        p &= 0xffffff00L;
                        break;
                    }
                    --index;
                }
            }
        } else {
            // p > elements[index] which is the previous primary.
            // Find the last secondary & tertiary weights for it.
            p = q & 0xffffff00L;
            secTer = Collation.COMMON_SEC_AND_TER_CE;
            for(;;) {
                q = elements[++index];
                if((q & SEC_TER_DELTA_FLAG) == 0) {
                    // We must not be in a primary range.
                    assert((q & PRIMARY_STEP_MASK) == 0);
                    break;
                }
                secTer = q;
            }
        }
        return (p << 32) | (secTer & ~SEC_TER_DELTA_FLAG);
    }

    /**
     * Returns the first root CE with a primary weight of at least p.
     * Intended only for reordering group boundaries.
     */
    long firstCEWithPrimaryAtLeast(long p) {
        if(p == 0) { return 0; }
        int index = findP(p);
        if(p != (elements[index] & 0xffffff00L)) {
            for(;;) {
                p = elements[++index];
                if((p & SEC_TER_DELTA_FLAG) == 0) {
                    // First primary after p. We must not be in a primary range.
                    assert((p & PRIMARY_STEP_MASK) == 0);
                    break;
                }
            }
        }
        // The code above guarantees that p has at most 3 bytes: (p & 0xff) == 0.
        return (p << 32) | Collation.COMMON_SEC_AND_TER_CE;
    }

    /**
     * Returns the primary weight before p.
     * p must be greater than the first root primary.
     */
    long getPrimaryBefore(long p, boolean isCompressible) {
        int index = findPrimary(p);
        int step;
        long q = elements[index];
        if(p == (q & 0xffffff00L)) {
            // Found p itself. Return the previous primary.
            // See if p is at the end of a previous range.
            step = (int)q & PRIMARY_STEP_MASK;
            if(step == 0) {
                // p is not at the end of a range. Look for the previous primary.
                do {
                    p = elements[--index];
                } while((p & SEC_TER_DELTA_FLAG) != 0);
                return p & 0xffffff00L;
            }
        } else {
            // p is in a range, and not at the start.
            long nextElement = elements[index + 1];
            assert(isEndOfPrimaryRange(nextElement));
            step = (int)nextElement & PRIMARY_STEP_MASK;
        }
        // Return the previous range primary.
        if((p & 0xffff) == 0) {
            return Collation.decTwoBytePrimaryByOneStep(p, isCompressible, step);
        } else {
            return Collation.decThreeBytePrimaryByOneStep(p, isCompressible, step);
        }
    }

    /** Returns the secondary weight before [p, s]. */
    int getSecondaryBefore(long p, int s) {
        int index;
        int previousSec, sec;
        if(p == 0) {
            index = (int)elements[IX_FIRST_SECONDARY_INDEX];
            // Gap at the beginning of the secondary CE range.
            previousSec = 0;
            sec = (int)(elements[index] >> 16);
        } else {
            index = findPrimary(p) + 1;
            previousSec = Collation.BEFORE_WEIGHT16;
            sec = (int)getFirstSecTerForPrimary(index) >>> 16;
        }
        assert(s >= sec);
        while(s > sec) {
            previousSec = sec;
            assert((elements[index] & SEC_TER_DELTA_FLAG) != 0);
            sec = (int)(elements[index++] >> 16);
        }
        assert(sec == s);
        return previousSec;
    }

    /** Returns the tertiary weight before [p, s, t]. */
    int getTertiaryBefore(long p, int s, int t) {
        assert((t & ~Collation.ONLY_TERTIARY_MASK) == 0);
        int index;
        int previousTer;
        long secTer;
        if(p == 0) {
            if(s == 0) {
                index = (int)elements[IX_FIRST_TERTIARY_INDEX];
                // Gap at the beginning of the tertiary CE range.
                previousTer = 0;
            } else {
                index = (int)elements[IX_FIRST_SECONDARY_INDEX];
                previousTer = Collation.BEFORE_WEIGHT16;
            }
            secTer = elements[index] & ~SEC_TER_DELTA_FLAG;
        } else {
            index = findPrimary(p) + 1;
            previousTer = Collation.BEFORE_WEIGHT16;
            secTer = getFirstSecTerForPrimary(index);
        }
        long st = ((long)s << 16) | t;
        while(st > secTer) {
            if((int)(secTer >> 16) == s) { previousTer = (int)secTer; }
            assert((elements[index] & SEC_TER_DELTA_FLAG) != 0);
            secTer = elements[index++] & ~SEC_TER_DELTA_FLAG;
        }
        assert(secTer == st);
        return previousTer & 0xffff;
    }

    /**
     * Finds the index of the input primary.
     * p must occur as a root primary, and must not be 0.
     */
    int findPrimary(long p) {
        // Requirement: p must occur as a root primary.
        assert((p & 0xff) == 0);  // at most a 3-byte primary
        int index = findP(p);
        // If p is in a range, then we just assume that p is an actual primary in this range.
        // (Too cumbersome/expensive to check.)
        // Otherwise, it must be an exact match.
        assert(isEndOfPrimaryRange(elements[index + 1]) || p == (elements[index] & 0xffffff00L));
        return index;
    }

    /**
     * Returns the primary weight after p where index=findPrimary(p).
     * p must be at least the first root primary.
     */
    long getPrimaryAfter(long p, int index, boolean isCompressible) {
        assert(p == (elements[index] & 0xffffff00L) || isEndOfPrimaryRange(elements[index + 1]));
        long q = elements[++index];
        int step;
        if((q & SEC_TER_DELTA_FLAG) == 0 && (step = (int)q & PRIMARY_STEP_MASK) != 0) {
            // Return the next primary in this range.
            if((p & 0xffff) == 0) {
                return Collation.incTwoBytePrimaryByOffset(p, isCompressible, step);
            } else {
                return Collation.incThreeBytePrimaryByOffset(p, isCompressible, step);
            }
        } else {
            // Return the next primary in the list.
            while((q & SEC_TER_DELTA_FLAG) != 0) {
                q = elements[++index];
            }
            assert((q & PRIMARY_STEP_MASK) == 0);
            return q;
        }
    }
    /**
     * Returns the secondary weight after [p, s] where index=findPrimary(p)
     * except use index=0 for p=0.
     *
     * <p>Must return a weight for every root [p, s] as well as for every weight
     * returned by getSecondaryBefore(). If p!=0 then s can be BEFORE_WEIGHT16.
     *
     * <p>Exception: [0, 0] is handled by the CollationBuilder:
     * Both its lower and upper boundaries are special.
     */
    int getSecondaryAfter(int index, int s) {
        long secTer;
        int secLimit;
        if(index == 0) {
            // primary = 0
            assert(s != 0);
            index = (int)elements[IX_FIRST_SECONDARY_INDEX];
            secTer = elements[index];
            // Gap at the end of the secondary CE range.
            secLimit = 0x10000;
        } else {
            assert(index >= (int)elements[IX_FIRST_PRIMARY_INDEX]);
            secTer = getFirstSecTerForPrimary(index + 1);
            // If this is an explicit sec/ter unit, then it will be read once more.
            // Gap for secondaries of primary CEs.
            secLimit = getSecondaryBoundary();
        }
        for(;;) {
            int sec = (int)(secTer >> 16);
            if(sec > s) { return sec; }
            secTer = elements[++index];
            if((secTer & SEC_TER_DELTA_FLAG) == 0) { return secLimit; }
        }
    }
    /**
     * Returns the tertiary weight after [p, s, t] where index=findPrimary(p)
     * except use index=0 for p=0.
     *
     * <p>Must return a weight for every root [p, s, t] as well as for every weight
     * returned by getTertiaryBefore(). If s!=0 then t can be BEFORE_WEIGHT16.
     *
     * <p>Exception: [0, 0, 0] is handled by the CollationBuilder:
     * Both its lower and upper boundaries are special.
     */
    int getTertiaryAfter(int index, int s, int t) {
        long secTer;
        int terLimit;
        if(index == 0) {
            // primary = 0
            if(s == 0) {
                assert(t != 0);
                index = (int)elements[IX_FIRST_TERTIARY_INDEX];
                // Gap at the end of the tertiary CE range.
                terLimit = 0x4000;
            } else {
                index = (int)elements[IX_FIRST_SECONDARY_INDEX];
                // Gap for tertiaries of primary/secondary CEs.
                terLimit = getTertiaryBoundary();
            }
            secTer = elements[index] & ~SEC_TER_DELTA_FLAG;
        } else {
            assert(index >= (int)elements[IX_FIRST_PRIMARY_INDEX]);
            secTer = getFirstSecTerForPrimary(index + 1);
            // If this is an explicit sec/ter unit, then it will be read once more.
            terLimit = getTertiaryBoundary();
        }
        long st = (((long)s & 0xffffffffL) << 16) | t;
        for(;;) {
            if(secTer > st) {
                assert((secTer >> 16) == s);
                return (int)secTer & 0xffff;
            }
            secTer = elements[++index];
            // No tertiary greater than t for this primary+secondary.
            if((secTer & SEC_TER_DELTA_FLAG) == 0 || (secTer >> 16) > s) { return terLimit; }
            secTer &= ~SEC_TER_DELTA_FLAG;
        }
    }

    /**
     * Returns the first secondary & tertiary weights for p where index=findPrimary(p)+1.
     */
    private long getFirstSecTerForPrimary(int index) {
        long secTer = elements[index];
        if((secTer & SEC_TER_DELTA_FLAG) == 0) {
            // No sec/ter delta.
            return Collation.COMMON_SEC_AND_TER_CE;
        }
        secTer &= ~SEC_TER_DELTA_FLAG;
        if(secTer > Collation.COMMON_SEC_AND_TER_CE) {
            // Implied sec/ter.
            return Collation.COMMON_SEC_AND_TER_CE;
        }
        // Explicit sec/ter below common/common.
        return secTer;
    }

    /**
     * Finds the largest index i where elements[i]<=p.
     * Requires first primary<=p<0xffffff00 (PRIMARY_SENTINEL).
     * Does not require that p is a root collator primary.
     */
    private int findP(long p) {
        // p need not occur as a root primary.
        // For example, it might be a reordering group boundary.
        assert((p >> 24) != Collation.UNASSIGNED_IMPLICIT_BYTE);
        // modified binary search
        int start = (int)elements[IX_FIRST_PRIMARY_INDEX];
        assert(p >= elements[start]);
        int limit = elements.length - 1;
        assert(elements[limit] >= PRIMARY_SENTINEL);
        assert(p < elements[limit]);
        while((start + 1) < limit) {
            // Invariant: elements[start] and elements[limit] are primaries,
            // and elements[start]<=p<=elements[limit].
            int i = (int)(((long)start + (long)limit) / 2);
            long q = elements[i];
            if((q & SEC_TER_DELTA_FLAG) != 0) {
                // Find the next primary.
                int j = i + 1;
                for(;;) {
                    if(j == limit) { break; }
                    q = elements[j];
                    if((q & SEC_TER_DELTA_FLAG) == 0) {
                        i = j;
                        break;
                    }
                    ++j;
                }
                if((q & SEC_TER_DELTA_FLAG) != 0) {
                    // Find the preceding primary.
                    j = i - 1;
                    for(;;) {
                        if(j == start) { break; }
                        q = elements[j];
                        if((q & SEC_TER_DELTA_FLAG) == 0) {
                            i = j;
                            break;
                        }
                        --j;
                    }
                    if((q & SEC_TER_DELTA_FLAG) != 0) {
                        // No primary between start and limit.
                        break;
                    }
                }
            }
            if(p < (q & 0xffffff00L)) {  // Reset the "step" bits of a range end primary.
                limit = i;
            } else {
                start = i;
            }
        }
        return start;
    }

    private static boolean isEndOfPrimaryRange(long q) {
        return (q & SEC_TER_DELTA_FLAG) == 0 && (q & PRIMARY_STEP_MASK) != 0;
    }

    /**
     * Data structure: See ICU4C source/i18n/collationrootelements.h.
     */
    private long[] elements;
}
