/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
*******************************************************************************
* Copyright (C) 1996-2014, International Business Machines Corporation and
* others. All Rights Reserved.
*******************************************************************************
*/
package android.icu.impl.coll;

import android.icu.util.ByteArrayWrapper;

/**
 * <p>Binary Ordered Compression Scheme for Unicode</p>
 * 
 * <p>Users are strongly encouraged to read the ICU paper on 
 * <a href="http://www.icu-project.org/docs/papers/binary_ordered_compression_for_unicode.html">
 * BOCU</a> before attempting to use this class.</p>
 * 
 * <p>BOCU is used to compress unicode text into a stream of unsigned
 * bytes.  For many kinds of text the compression compares favorably
 * to UTF-8, and for some kinds of text (such as CJK) it does better.
 * The resulting bytes will compare in the same order as the original
 * code points.  The byte stream does not contain the values 0, 1, or
 * 2.</p>
 * 
 * <p>One example of a use of BOCU is in 
 * android.icu.text.Collator#getCollationKey(String) for a RuleBasedCollator object with 
 * collation strength IDENTICAL. The result CollationKey will consist of the 
 * collation order of the source string followed by the BOCU result of the 
 * source string. 
 * </p>
 *
 * <p>Unlike a UTF encoding, BOCU-compressed text is not suitable for
 * random access.</p>
 * 
 * <p>Method: Slope Detection<br> Remember the previous code point
 * (initial 0).  For each code point in the string, encode the
 * difference with the previous one.  Similar to a UTF, the length of
 * the byte sequence is encoded in the lead bytes.  Unlike a UTF, the
 * trail byte values may overlap with lead/single byte values.  The
 * signedness of the difference must be encoded as the most
 * significant part.</p>
 *
 * <p>We encode differences with few bytes if their absolute values
 * are small.  For correct ordering, we must treat the entire value
 * range -10ffff..+10ffff in ascending order, which forbids encoding
 * the sign and the absolute value separately. Instead, we split the
 * lead byte range in the middle and encode non-negative values going
 * up and negative values going down.</p>
 *
 * <p>For very small absolute values, the difference is added to a
 * middle byte value for single-byte encoded differences.  For
 * somewhat larger absolute values, the difference is divided by the
 * number of byte values available, the modulo is used for one trail
 * byte, and the remainder is added to a lead byte avoiding the
 * single-byte range.  For large absolute values, the difference is
 * similarly encoded in three bytes. (Syn Wee, I need examples
 * here.)</p>
 *
 * <p>BOCU does not use byte values 0, 1, or 2, but uses all other
 * byte values for lead and single bytes, so that the middle range of
 * single bytes is as large as possible.</p>
 *
 * <p>Note that the lead byte ranges overlap some, but that the
 * sequences as a whole are well ordered. I.e., even if the lead byte
 * is the same for sequences of different lengths, the trail bytes
 * establish correct order.  It would be possible to encode slightly
 * larger ranges for each length (>1) by subtracting the lower bound
 * of the range. However, that would also slow down the calculation.
 * (Syn Wee, need an example).</p>
 *
 * <p>For the actual string encoding, an optimization moves the
 * previous code point value to the middle of its Unicode script block
 * to minimize the differences in same-script text runs.  (Syn Wee,
 * need an example.)</p>
 *
 * @author Syn Wee Quek
 * @hide Only a subset of ICU is exposed in Android
 */
public class BOCSU 
{      
    // public methods -------------------------------------------------------

    /**
     * Encode the code points of a string as
     * a sequence of byte-encoded differences (slope detection),
     * preserving lexical order.
     *
     * <p>Optimize the difference-taking for runs of Unicode text within
     * small scripts:
     *
     * <p>Most small scripts are allocated within aligned 128-blocks of Unicode
     * code points. Lexical order is preserved if "prev" is always moved
     * into the middle of such a block.
     *
     * <p>Additionally, "prev" is moved from anywhere in the Unihan
     * area into the middle of that area.
     * Note that the identical-level run in a sort key is generated from
     * NFD text - there are never Hangul characters included.
     */
    public static int writeIdenticalLevelRun(int prev, CharSequence s, int i, int length, ByteArrayWrapper sink) {
        while (i < length) {
            // We must have capacity>=SLOPE_MAX_BYTES in case writeDiff() writes that much,
            // but we do not want to force the sink to allocate
            // for a large min_capacity because we might actually only write one byte.
            ensureAppendCapacity(sink, 16, s.length() * 2);
            byte[] buffer = sink.bytes;
            int capacity = buffer.length;
            int p = sink.size;
            int lastSafe = capacity - SLOPE_MAX_BYTES_;
            while (i < length && p <= lastSafe) {
                if (prev < 0x4e00 || prev >= 0xa000) {
                    prev = (prev & ~0x7f) - SLOPE_REACH_NEG_1_;
                } else {
                    // Unihan U+4e00..U+9fa5:
                    // double-bytes down from the upper end
                    prev = 0x9fff - SLOPE_REACH_POS_2_;
                }

                int c = Character.codePointAt(s, i);
                i += Character.charCount(c);
                if (c == 0xfffe) {
                    buffer[p++] = 2;  // merge separator
                    prev = 0;
                } else {
                    p = writeDiff(c - prev, buffer, p);
                    prev = c;
                }
            }
            sink.size = p;
        }
        return prev;
    }

    private static void ensureAppendCapacity(ByteArrayWrapper sink, int minCapacity, int desiredCapacity) {
        int remainingCapacity = sink.bytes.length - sink.size;
        if (remainingCapacity >= minCapacity) { return; }
        if (desiredCapacity < minCapacity) { desiredCapacity = minCapacity; }
        sink.ensureCapacity(sink.size + desiredCapacity);
    }

    // private data members --------------------------------------------------

    /** 
     * Do not use byte values 0, 1, 2 because they are separators in sort keys.
     */
    private static final int SLOPE_MIN_ = 3;
    private static final int SLOPE_MAX_ = 0xff;
    private static final int SLOPE_MIDDLE_ = 0x81;
    private static final int SLOPE_TAIL_COUNT_ = SLOPE_MAX_ - SLOPE_MIN_ + 1;
    private static final int SLOPE_MAX_BYTES_ = 4;

    /**
     * Number of lead bytes:
     * 1        middle byte for 0
     * 2*80=160 single bytes for !=0
     * 2*42=84  for double-byte values
     * 2*3=6    for 3-byte values
     * 2*1=2    for 4-byte values
     *
     * The sum must be <=SLOPE_TAIL_COUNT.
     *
     * Why these numbers?
     * - There should be >=128 single-byte values to cover 128-blocks
     *   with small scripts.
     * - There should be >=20902 single/double-byte values to cover Unihan.
     * - It helps CJK Extension B some if there are 3-byte values that cover
     *   the distance between them and Unihan.
     *   This also helps to jump among distant places in the BMP.
     * - Four-byte values are necessary to cover the rest of Unicode.
     *
     * Symmetrical lead byte counts are for convenience.
     * With an equal distribution of even and odd differences there is also
     * no advantage to asymmetrical lead byte counts.
     */
    private static final int SLOPE_SINGLE_ = 80;
    private static final int SLOPE_LEAD_2_ = 42;
    private static final int SLOPE_LEAD_3_ = 3;
    //private static final int SLOPE_LEAD_4_ = 1;

    /** 
     * The difference value range for single-byters.
     */
    private static final int SLOPE_REACH_POS_1_ = SLOPE_SINGLE_;
    private static final int SLOPE_REACH_NEG_1_ = (-SLOPE_SINGLE_);

    /** 
     * The difference value range for double-byters.
     */
    private static final int SLOPE_REACH_POS_2_ = 
        SLOPE_LEAD_2_ * SLOPE_TAIL_COUNT_ + SLOPE_LEAD_2_ - 1;
    private static final int SLOPE_REACH_NEG_2_ = (-SLOPE_REACH_POS_2_ - 1);

    /** 
     * The difference value range for 3-byters.
     */
    private static final int SLOPE_REACH_POS_3_ = SLOPE_LEAD_3_ 
        * SLOPE_TAIL_COUNT_ 
        * SLOPE_TAIL_COUNT_ 
        + (SLOPE_LEAD_3_ - 1)
        * SLOPE_TAIL_COUNT_ +
        (SLOPE_TAIL_COUNT_ - 1);
    private static final int SLOPE_REACH_NEG_3_ = (-SLOPE_REACH_POS_3_ - 1);

    /** 
     * The lead byte start values.
     */
    private static final int SLOPE_START_POS_2_ = SLOPE_MIDDLE_ 
        + SLOPE_SINGLE_ + 1;
    private static final int SLOPE_START_POS_3_ = SLOPE_START_POS_2_ 
        + SLOPE_LEAD_2_;
    private static final int SLOPE_START_NEG_2_ = SLOPE_MIDDLE_ + 
        SLOPE_REACH_NEG_1_;
    private static final int SLOPE_START_NEG_3_ = SLOPE_START_NEG_2_
        - SLOPE_LEAD_2_;
                                                                                                        
    // private constructor ---------------------------------------------------
        
    /**
     * Constructor private to prevent initialization
     */
    ///CLOVER:OFF
    private BOCSU()
    {
    }            
    ///CLOVER:ON                                                                                       
    
    // private methods -------------------------------------------------------
    
    /**
     * Integer division and modulo with negative numerators
     * yields negative modulo results and quotients that are one more than
     * what we need here.
     * @param number which operations are to be performed on
     * @param factor the factor to use for division
     * @return (result of division) << 32 | modulo 
     */
    private static final long getNegDivMod(int number, int factor) 
    {
        int modulo = number % factor; 
        long result = number / factor;
        if (modulo < 0) { 
            -- result; 
            modulo += factor; 
        } 
        return (result << 32) | modulo;
    }
        
    /**
     * Encode one difference value -0x10ffff..+0x10ffff in 1..4 bytes,
     * preserving lexical order
     * @param diff
     * @param buffer byte buffer to append to
     * @param offset to the byte buffer to start appending
     * @return end offset where the appending stops
     */
    private static final int writeDiff(int diff, byte buffer[], int offset) 
    {
        if (diff >= SLOPE_REACH_NEG_1_) {
            if (diff <= SLOPE_REACH_POS_1_) {
                buffer[offset ++] = (byte)(SLOPE_MIDDLE_ + diff);
            } 
            else if (diff <= SLOPE_REACH_POS_2_) {
                buffer[offset ++] = (byte)(SLOPE_START_POS_2_ 
                                           + (diff / SLOPE_TAIL_COUNT_));
                buffer[offset ++] = (byte)(SLOPE_MIN_ + 
                                           (diff % SLOPE_TAIL_COUNT_));
            } 
            else if (diff <= SLOPE_REACH_POS_3_) {
                buffer[offset + 2] = (byte)(SLOPE_MIN_ 
                                            + (diff % SLOPE_TAIL_COUNT_));
                diff /= SLOPE_TAIL_COUNT_;
                buffer[offset + 1] = (byte)(SLOPE_MIN_ 
                                            + (diff % SLOPE_TAIL_COUNT_));
                buffer[offset] = (byte)(SLOPE_START_POS_3_ 
                                        + (diff / SLOPE_TAIL_COUNT_));
                offset += 3;
            } 
            else {
                buffer[offset + 3] = (byte)(SLOPE_MIN_ 
                                            + diff % SLOPE_TAIL_COUNT_);
                diff /= SLOPE_TAIL_COUNT_;
                buffer[offset + 2] = (byte)(SLOPE_MIN_ 
                                        + diff % SLOPE_TAIL_COUNT_);
                diff /= SLOPE_TAIL_COUNT_;
                buffer[offset + 1] = (byte)(SLOPE_MIN_ 
                                            + diff % SLOPE_TAIL_COUNT_);
                buffer[offset] = (byte)SLOPE_MAX_;
                offset += 4;
            }
        } 
        else {
            long division = getNegDivMod(diff, SLOPE_TAIL_COUNT_);
            int modulo = (int)division;
            if (diff >= SLOPE_REACH_NEG_2_) {
                diff = (int)(division >> 32);
                buffer[offset ++] = (byte)(SLOPE_START_NEG_2_ + diff);
                buffer[offset ++] = (byte)(SLOPE_MIN_ + modulo);
            } 
            else if (diff >= SLOPE_REACH_NEG_3_) {
                buffer[offset + 2] = (byte)(SLOPE_MIN_ + modulo);
                diff = (int)(division >> 32);
                division = getNegDivMod(diff, SLOPE_TAIL_COUNT_);
                modulo = (int)division;
                diff = (int)(division >> 32);
                buffer[offset + 1] = (byte)(SLOPE_MIN_ + modulo);
                buffer[offset] = (byte)(SLOPE_START_NEG_3_ + diff);
                offset += 3;
            } 
            else {
                buffer[offset + 3] = (byte)(SLOPE_MIN_ + modulo);
                diff = (int)(division >> 32);
                division = getNegDivMod(diff, SLOPE_TAIL_COUNT_);
                modulo = (int)division;
                diff = (int)(division >> 32);
                buffer[offset + 2] = (byte)(SLOPE_MIN_ + modulo);
                division = getNegDivMod(diff, SLOPE_TAIL_COUNT_);
                modulo = (int)division;
                buffer[offset + 1] = (byte)(SLOPE_MIN_ + modulo);
                buffer[offset] = SLOPE_MIN_;
                offset += 4;
            }
        }
        return offset;
    }
}
