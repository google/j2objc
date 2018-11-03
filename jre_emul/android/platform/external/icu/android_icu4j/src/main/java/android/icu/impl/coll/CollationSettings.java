/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2013-2015, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationSettings.java, ported from collationsettings.h/.cpp
*
* C++ version created on: 2013feb07
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import java.util.Arrays;

import android.icu.text.Collator;

/**
 * Collation settings/options/attributes.
 * These are the values that can be changed via API.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CollationSettings extends SharedObject {
    /**
     * Options bit 0: Perform the FCD check on the input text and deliver normalized text.
     */
    public static final int CHECK_FCD = 1;
    /**
     * Options bit 1: Numeric collation.
     * Also known as CODAN = COllate Digits As Numbers.
     *
     * Treat digit sequences as numbers with CE sequences in numeric order,
     * rather than returning a normal CE for each digit.
     */
    public static final int NUMERIC = 2;
    /**
     * "Shifted" alternate handling, see ALTERNATE_MASK.
     */
    static final int SHIFTED = 4;
    /**
     * Options bits 3..2: Alternate-handling mask. 0 for non-ignorable.
     * Reserve values 8 and 0xc for shift-trimmed and blanked.
     */
    static final int ALTERNATE_MASK = 0xc;
    /**
     * Options bits 6..4: The 3-bit maxVariable value bit field is shifted by this value.
     */
    static final int MAX_VARIABLE_SHIFT = 4;
    /** maxVariable options bit mask before shifting. */
    static final int MAX_VARIABLE_MASK = 0x70;
    /** Options bit 7: Reserved/unused/0. */
    /**
     * Options bit 8: Sort uppercase first if caseLevel or caseFirst is on.
     */
    static final int UPPER_FIRST = 0x100;
    /**
     * Options bit 9: Keep the case bits in the tertiary weight (they trump other tertiary values)
     * unless case level is on (when they are *moved* into the separate case level).
     * By default, the case bits are removed from the tertiary weight (ignored).
     *
     * When CASE_FIRST is off, UPPER_FIRST must be off too, corresponding to
     * the tri-value UCOL_CASE_FIRST attribute: UCOL_OFF vs. UCOL_LOWER_FIRST vs. UCOL_UPPER_FIRST.
     */
    public static final int CASE_FIRST = 0x200;
    /**
     * Options bit mask for caseFirst and upperFirst, before shifting.
     * Same value as caseFirst==upperFirst.
     */
    public static final int CASE_FIRST_AND_UPPER_MASK = CASE_FIRST | UPPER_FIRST;
    /**
     * Options bit 10: Insert the case level between the secondary and tertiary levels.
     */
    public static final int CASE_LEVEL = 0x400;
    /**
     * Options bit 11: Compare secondary weights backwards. ("French secondary")
     */
    public static final int BACKWARD_SECONDARY = 0x800;
    /**
     * Options bits 15..12: The 4-bit strength value bit field is shifted by this value.
     * It is the top used bit field in the options. (No need to mask after shifting.)
     */
    static final int STRENGTH_SHIFT = 12;
    /** Strength options bit mask before shifting. */
    static final int STRENGTH_MASK = 0xf000;

    /** maxVariable values */
    static final int MAX_VAR_SPACE = 0;
    static final int MAX_VAR_PUNCT = 1;
    static final int MAX_VAR_SYMBOL = 2;
    static final int MAX_VAR_CURRENCY = 3;

    CollationSettings() {}

    @Override
    public CollationSettings clone() {
        CollationSettings newSettings = (CollationSettings)super.clone();
        // Note: The reorderTable, reorderRanges, and reorderCodes need not be cloned
        // because, in Java, they only get replaced but not modified.
        newSettings.fastLatinPrimaries = fastLatinPrimaries.clone();
        return newSettings;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) { return false; }
        if(!this.getClass().equals(other.getClass())) { return false; }
        CollationSettings o = (CollationSettings)other;
        if(options != o.options) { return false; }
        if((options & ALTERNATE_MASK) != 0 && variableTop != o.variableTop) { return false; }
        if(!Arrays.equals(reorderCodes, o.reorderCodes)) { return false; }
        return true;
    }

    @Override
    public int hashCode() {
        int h = options << 8;
        if((options & ALTERNATE_MASK) != 0) { h ^= variableTop; }
        h ^= reorderCodes.length;
        for(int i = 0; i < reorderCodes.length; ++i) {
            h ^= (reorderCodes[i] << i);
        }
        return h;
    }

    public void resetReordering() {
        // When we turn off reordering, we want to set a null permutation
        // rather than a no-op permutation.
        reorderTable = null;
        minHighNoReorder = 0;
        reorderRanges = null;
        reorderCodes = EMPTY_INT_ARRAY;
    }

    void aliasReordering(CollationData data, int[] codesAndRanges, int codesLength, byte[] table) {
        int[] codes;
        if(codesLength == codesAndRanges.length) {
            codes = codesAndRanges;
        } else {
            // TODO: Java 6: Arrays.copyOf(codes, codesLength);
            codes = new int[codesLength];
            System.arraycopy(codesAndRanges, 0, codes, 0, codesLength);
        }
        int rangesStart = codesLength;
        int rangesLimit = codesAndRanges.length;
        int rangesLength = rangesLimit - rangesStart;
        if(table != null &&
                (rangesLength == 0 ?
                        !reorderTableHasSplitBytes(table) :
                        rangesLength >= 2 &&
                        // The first offset must be 0. The last offset must not be 0.
                        (codesAndRanges[rangesStart] & 0xffff) == 0 &&
                        (codesAndRanges[rangesLimit - 1] & 0xffff) != 0)) {
            reorderTable = table;
            reorderCodes = codes;
            // Drop ranges before the first split byte. They are reordered by the table.
            // This then speeds up reordering of the remaining ranges.
            int firstSplitByteRangeIndex = rangesStart;
            while(firstSplitByteRangeIndex < rangesLimit &&
                    (codesAndRanges[firstSplitByteRangeIndex] & 0xff0000) == 0) {
                // The second byte of the primary limit is 0.
                ++firstSplitByteRangeIndex;
            }
            if(firstSplitByteRangeIndex == rangesLimit) {
                assert(!reorderTableHasSplitBytes(table));
                minHighNoReorder = 0;
                reorderRanges = null;
            } else {
                assert(table[codesAndRanges[firstSplitByteRangeIndex] >>> 24] == 0);
                minHighNoReorder = codesAndRanges[rangesLimit - 1] & 0xffff0000L;
                setReorderRanges(codesAndRanges, firstSplitByteRangeIndex,
                        rangesLimit - firstSplitByteRangeIndex);
            }
            return;
        }
        // Regenerate missing data.
        setReordering(data, codes);
    }

    public void setReordering(CollationData data, int[] codes) {
        if(codes.length == 0 || (codes.length == 1 && codes[0] == Collator.ReorderCodes.NONE)) {
            resetReordering();
            return;
        }
        UVector32 rangesList = new UVector32();
        data.makeReorderRanges(codes, rangesList);
        int rangesLength = rangesList.size();
        if(rangesLength == 0) {
            resetReordering();
            return;
        }
        int[] ranges = rangesList.getBuffer();
        // ranges[] contains at least two (limit, offset) pairs.
        // The first offset must be 0. The last offset must not be 0.
        // Separators (at the low end) and trailing weights (at the high end)
        // are never reordered.
        assert(rangesLength >= 2);
        assert((ranges[0] & 0xffff) == 0 && (ranges[rangesLength - 1] & 0xffff) != 0);
        minHighNoReorder = ranges[rangesLength - 1] & 0xffff0000L;

        // Write the lead byte permutation table.
        // Set a 0 for each lead byte that has a range boundary in the middle.
        byte[] table = new byte[256];
        int b = 0;
        int firstSplitByteRangeIndex = -1;
        for(int i = 0; i < rangesLength; ++i) {
            int pair = ranges[i];
            int limit1 = pair >>> 24;
            while(b < limit1) {
                table[b] = (byte)(b + pair);
                ++b;
            }
            // Check the second byte of the limit.
            if((pair & 0xff0000) != 0) {
                table[limit1] = 0;
                b = limit1 + 1;
                if(firstSplitByteRangeIndex < 0) {
                    firstSplitByteRangeIndex = i;
                }
            }
        }
        while(b <= 0xff) {
            table[b] = (byte)b;
            ++b;
        }
        int rangesStart;
        if(firstSplitByteRangeIndex < 0) {
            // The lead byte permutation table alone suffices for reordering.
            rangesStart = rangesLength = 0;
        } else {
            // Remove the ranges below the first split byte.
            rangesStart = firstSplitByteRangeIndex;
            rangesLength -= firstSplitByteRangeIndex;
        }
        setReorderArrays(codes, ranges, rangesStart, rangesLength, table);
    }

    private void setReorderArrays(int[] codes,
            int[] ranges, int rangesStart, int rangesLength, byte[] table) {
        // Very different from C++. See the comments after the reorderCodes declaration.
        if(codes == null) {
            codes = EMPTY_INT_ARRAY;
        }
        assert (codes.length == 0) == (table == null);
        reorderTable = table;
        reorderCodes = codes;
        setReorderRanges(ranges, rangesStart, rangesLength);
    }

    private void setReorderRanges(int[] ranges, int rangesStart, int rangesLength) {
        if(rangesLength == 0) {
            reorderRanges = null;
        } else {
            reorderRanges = new long[rangesLength];
            int i = 0;
            do {
                reorderRanges[i++] = ranges[rangesStart++] & 0xffffffffL;
            } while(i < rangesLength);
        }
    }

    public void copyReorderingFrom(CollationSettings other) {
        if(!other.hasReordering()) {
            resetReordering();
            return;
        }
        minHighNoReorder = other.minHighNoReorder;
        reorderTable = other.reorderTable;
        reorderRanges = other.reorderRanges;
        reorderCodes = other.reorderCodes;
    }

    public boolean hasReordering() { return reorderTable != null; }

    private static boolean reorderTableHasSplitBytes(byte[] table) {
        assert(table[0] == 0);
        for(int i = 1; i < 256; ++i) {
            if(table[i] == 0) {
                return true;
            }
        }
        return false;
    }

    public long reorder(long p) {
        byte b = reorderTable[(int)p >>> 24];
        if(b != 0 || p <= Collation.NO_CE_PRIMARY) {
            return ((b & 0xffL) << 24) | (p & 0xffffff);
        } else {
            return reorderEx(p);
        }
    }

    private long reorderEx(long p) {
        assert minHighNoReorder > 0;
        if(p >= minHighNoReorder) { return p; }
        // Round up p so that its lower 16 bits are >= any offset bits.
        // Then compare q directly with (limit, offset) pairs.
        long q = p | 0xffff;
        long r;
        int i = 0;
        while(q >= (r = reorderRanges[i])) { ++i; }
        return p + ((long)(short)r << 24);
    }

    // In C++, we use enums for attributes and their values, with a special value for the default.
    // Combined getter/setter methods handle many attributes.
    // In Java, we have specific methods for getting, setting, and set-to-default,
    // except that this class uses bits in its own bit set for simple values.

    public void setStrength(int value) {
        int noStrength = options & ~STRENGTH_MASK;
        switch(value) {
        case Collator.PRIMARY:
        case Collator.SECONDARY:
        case Collator.TERTIARY:
        case Collator.QUATERNARY:
        case Collator.IDENTICAL:
            options = noStrength | (value << STRENGTH_SHIFT);
            break;
        default:
            throw new IllegalArgumentException("illegal strength value " + value);
        }
    }

    public void setStrengthDefault(int defaultOptions) {
        int noStrength = options & ~STRENGTH_MASK;
        options = noStrength | (defaultOptions & STRENGTH_MASK);
    }

    static int getStrength(int options) {
        return options >> STRENGTH_SHIFT;
    }

    public int getStrength() {
        return getStrength(options);
    }

    /** Sets the options bit for an on/off attribute. */
    public void setFlag(int bit, boolean value) {
        if(value) {
            options |= bit;
        } else {
            options &= ~bit;
        }
    }

    public void setFlagDefault(int bit, int defaultOptions) {
        options = (options & ~bit) | (defaultOptions & bit);
    }

    public boolean getFlag(int bit) {
        return (options & bit) != 0;
    }

    public void setCaseFirst(int value) {
        assert value == 0 || value == CASE_FIRST || value == CASE_FIRST_AND_UPPER_MASK;
        int noCaseFirst = options & ~CASE_FIRST_AND_UPPER_MASK;
        options = noCaseFirst | value;
    }

    public void setCaseFirstDefault(int defaultOptions) {
        int noCaseFirst = options & ~CASE_FIRST_AND_UPPER_MASK;
        options = noCaseFirst | (defaultOptions & CASE_FIRST_AND_UPPER_MASK);
    }

    public int getCaseFirst() {
        return options & CASE_FIRST_AND_UPPER_MASK;
    }

    public void setAlternateHandlingShifted(boolean value) {
        int noAlternate = options & ~ALTERNATE_MASK;
        if(value) {
            options = noAlternate | SHIFTED;
        } else {
            options = noAlternate;
        }
    }

    public void setAlternateHandlingDefault(int defaultOptions) {
        int noAlternate = options & ~ALTERNATE_MASK;
        options = noAlternate | (defaultOptions & ALTERNATE_MASK);
    }

    public boolean getAlternateHandling() {
        return (options & ALTERNATE_MASK) != 0;
    }

    public void setMaxVariable(int value, int defaultOptions) {
        int noMax = options & ~MAX_VARIABLE_MASK;
        switch(value) {
        case MAX_VAR_SPACE:
        case MAX_VAR_PUNCT:
        case MAX_VAR_SYMBOL:
        case MAX_VAR_CURRENCY:
            options = noMax | (value << MAX_VARIABLE_SHIFT);
            break;
        case -1:
            options = noMax | (defaultOptions & MAX_VARIABLE_MASK);
            break;
        default:
            throw new IllegalArgumentException("illegal maxVariable value " + value);
        }
    }

    public int getMaxVariable() {
        return (options & MAX_VARIABLE_MASK) >> MAX_VARIABLE_SHIFT;
    }

    /**
     * Include case bits in the tertiary level if caseLevel=off and caseFirst!=off.
     */
    static boolean isTertiaryWithCaseBits(int options) {
        return (options & (CASE_LEVEL | CASE_FIRST)) == CASE_FIRST;
    }
    static int getTertiaryMask(int options) {
        // Remove the case bits from the tertiary weight when caseLevel is on or caseFirst is off.
        return isTertiaryWithCaseBits(options) ?
                Collation.CASE_AND_TERTIARY_MASK : Collation.ONLY_TERTIARY_MASK;
    }

    static boolean sortsTertiaryUpperCaseFirst(int options) {
        // On tertiary level, consider case bits and sort uppercase first
        // if caseLevel is off and caseFirst==upperFirst.
        return (options & (CASE_LEVEL | CASE_FIRST_AND_UPPER_MASK)) == CASE_FIRST_AND_UPPER_MASK;
    }

    public boolean dontCheckFCD() {
        return (options & CHECK_FCD) == 0;
    }

    boolean hasBackwardSecondary() {
        return (options & BACKWARD_SECONDARY) != 0;
    }

    public boolean isNumeric() {
        return (options & NUMERIC) != 0;
    }

    /** CHECK_FCD etc. */
    public int options = (Collator.TERTIARY << STRENGTH_SHIFT) |  // DEFAULT_STRENGTH
            (MAX_VAR_PUNCT << MAX_VARIABLE_SHIFT);
    /** Variable-top primary weight. */
    public long variableTop;
    /**
     * 256-byte table for reordering permutation of primary lead bytes; null if no reordering.
     * A 0 entry at a non-zero index means that the primary lead byte is "split"
     * (there are different offsets for primaries that share that lead byte)
     * and the reordering offset must be determined via the reorderRanges.
     */
    public byte[] reorderTable;
    /** Limit of last reordered range. 0 if no reordering or no split bytes. */
    long minHighNoReorder;
    /**
     * Primary-weight ranges for script reordering,
     * to be used by reorder(p) for split-reordered primary lead bytes.
     *
     * <p>Each entry is a (limit, offset) pair.
     * The upper 16 bits of the entry are the upper 16 bits of the
     * exclusive primary limit of a range.
     * Primaries between the previous limit and this one have their lead bytes
     * modified by the signed offset (-0xff..+0xff) stored in the lower 16 bits.
     *
     * <p>CollationData.makeReorderRanges() writes a full list where the first range
     * (at least for terminators and separators) has a 0 offset.
     * The last range has a non-zero offset.
     * minHighNoReorder is set to the limit of that last range.
     *
     * <p>In the settings object, the initial ranges before the first split lead byte
     * are omitted for efficiency; they are handled by reorder(p) via the reorderTable.
     * If there are no split-reordered lead bytes, then no ranges are needed.
     */
    long[] reorderRanges;
    /** Array of reorder codes; ignored if length == 0. */
    public int[] reorderCodes = EMPTY_INT_ARRAY;
    // Note: In C++, we keep a memory block around for the reorder codes,
    // the ranges, and the permutation table,
    // and modify them for new codes.
    // In Java, we simply copy references and then never modify the array contents.
    // The caller must abandon the arrays.
    // Reorder codes from the public setter API must be cloned.
    private static final int[] EMPTY_INT_ARRAY = new int[0];

    /** Options for CollationFastLatin. Negative if disabled. */
    public int fastLatinOptions = -1;
    // fastLatinPrimaries.length must be equal to CollationFastLatin.LATIN_LIMIT,
    // but we do not import CollationFastLatin to reduce circular dependencies.
    public char[] fastLatinPrimaries = new char[0x180];  // mutable contents
}
