/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2013-2015, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationFastLatinBuilder.java, ported from collationfastlatinbuilder.h/.cpp
*
* C++ version created on: 2013aug09
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import android.icu.lang.UScript;
import android.icu.text.Collator;
import android.icu.util.CharsTrie;

final class CollationFastLatinBuilder {
    // #define DEBUG_COLLATION_FAST_LATIN_BUILDER 0  // 0 or 1 or 2

    /**
     * Compare two signed long values as if they were unsigned.
     */
    private static final int compareInt64AsUnsigned(long a, long b) {
        a += 0x8000000000000000L;
        b += 0x8000000000000000L;
        if(a < b) {
            return -1;
        } else if(a > b) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Like Java Collections.binarySearch(List, String, Comparator).
     *
     * @return the index>=0 where the item was found,
     *         or the index<0 for inserting the string at ~index in sorted order
     */
    private static final int binarySearch(long[] list, int limit, long ce) {
        if (limit == 0) { return ~0; }
        int start = 0;
        for (;;) {
            int i = (int)(((long)start + (long)limit) / 2);
            int cmp = compareInt64AsUnsigned(ce, list[i]);
            if (cmp == 0) {
                return i;
            } else if (cmp < 0) {
                if (i == start) {
                    return ~start;  // insert ce before i
                }
                limit = i;
            } else {
                if (i == start) {
                    return ~(start + 1);  // insert ce after i
                }
                start = i;
            }
        }
    }

    CollationFastLatinBuilder() {
        ce0 = 0;
        ce1 = 0;
        contractionCEs = new UVector64();
        uniqueCEs = new UVector64();
        miniCEs = null;
        firstDigitPrimary = 0;
        firstLatinPrimary = 0;
        lastLatinPrimary = 0;
        firstShortPrimary = 0;
        shortPrimaryOverflow = false;
        headerLength = 0;
    }

    boolean forData(CollationData data) {
        if(result.length() != 0) {  // This builder is not reusable.
            throw new IllegalStateException("attempt to reuse a CollationFastLatinBuilder");
        }
        if(!loadGroups(data)) { return false; }

        // Fast handling of digits.
        firstShortPrimary = firstDigitPrimary;
        getCEs(data);
        encodeUniqueCEs();
        if(shortPrimaryOverflow) {
            // Give digits long mini primaries,
            // so that there are more short primaries for letters.
            firstShortPrimary = firstLatinPrimary;
            resetCEs();
            getCEs(data);
            encodeUniqueCEs();
        }
        // Note: If we still have a short-primary overflow but not a long-primary overflow,
        // then we could calculate how many more long primaries would fit,
        // and set the firstShortPrimary to that many after the current firstShortPrimary,
        // and try again.
        // However, this might only benefit the en_US_POSIX tailoring,
        // and it is simpler to suppress building fast Latin data for it in genrb,
        // or by returning false here if shortPrimaryOverflow.

        boolean ok = !shortPrimaryOverflow;
        if(ok) {
            encodeCharCEs();
            encodeContractions();
        }
        contractionCEs.removeAllElements();  // might reduce heap memory usage
        uniqueCEs.removeAllElements();
        return ok;
    }

    // C++ returns one combined array with the contents of the result buffer.
    // Java returns two arrays (header & table) because we cannot use pointer arithmetic,
    // and we do not want to index into the table with an offset.
    char[] getHeader() {
        char[] resultArray = new char[headerLength];
        result.getChars(0, headerLength, resultArray, 0);
        return resultArray;
    }

    char[] getTable() {
        char[] resultArray = new char[result.length() - headerLength];
        result.getChars(headerLength, result.length(), resultArray, 0);
        return resultArray;
    }

    private boolean loadGroups(CollationData data) {
        headerLength = 1 + NUM_SPECIAL_GROUPS;
        int r0 = (CollationFastLatin.VERSION << 8) | headerLength;
        result.append((char)r0);
        // The first few reordering groups should be special groups
        // (space, punct, ..., digit) followed by Latn, then Grek and other scripts.
        for(int i = 0; i < NUM_SPECIAL_GROUPS; ++i) {
            lastSpecialPrimaries[i] = data.getLastPrimaryForGroup(Collator.ReorderCodes.FIRST + i);
            if(lastSpecialPrimaries[i] == 0) {
                // missing data
                return false;
            }
            result.append(0);  // reserve a slot for this group
        }

        firstDigitPrimary = data.getFirstPrimaryForGroup(Collator.ReorderCodes.DIGIT);
        firstLatinPrimary = data.getFirstPrimaryForGroup(UScript.LATIN);
        lastLatinPrimary = data.getLastPrimaryForGroup(UScript.LATIN);
        if(firstDigitPrimary == 0 || firstLatinPrimary == 0) {
            // missing data
            return false;
        }
        return true;
    }

    private boolean inSameGroup(long p, long q) {
        // Both or neither need to be encoded as short primaries,
        // so that we can test only one and use the same bit mask.
        if(p >= firstShortPrimary) {
            return q >= firstShortPrimary;
        } else if(q >= firstShortPrimary) {
            return false;
        }
        // Both or neither must be potentially-variable,
        // so that we can test only one and determine if both are variable.
        long lastVariablePrimary = lastSpecialPrimaries[NUM_SPECIAL_GROUPS - 1];
        if(p > lastVariablePrimary) {
            return q > lastVariablePrimary;
        } else if(q > lastVariablePrimary) {
            return false;
        }
        // Both will be encoded with long mini primaries.
        // They must be in the same special reordering group,
        // so that we can test only one and determine if both are variable.
        assert(p != 0 && q != 0);
        for(int i = 0;; ++i) {  // will terminate
            long lastPrimary = lastSpecialPrimaries[i];
            if(p <= lastPrimary) {
                return q <= lastPrimary;
            } else if(q <= lastPrimary) {
                return false;
            }
        }
    }

    private void resetCEs() {
        contractionCEs.removeAllElements();
        uniqueCEs.removeAllElements();
        shortPrimaryOverflow = false;
        result.setLength(headerLength);
    }

    private void getCEs(CollationData data) {
        int i = 0;
        for(char c = 0;; ++i, ++c) {
            if(c == CollationFastLatin.LATIN_LIMIT) {
                c = CollationFastLatin.PUNCT_START;
            } else if(c == CollationFastLatin.PUNCT_LIMIT) {
                break;
            }
            CollationData d;
            int ce32 = data.getCE32(c);
            if(ce32 == Collation.FALLBACK_CE32) {
                d = data.base;
                ce32 = d.getCE32(c);
            } else {
                d = data;
            }
            if(getCEsFromCE32(d, c, ce32)) {
                charCEs[i][0] = ce0;
                charCEs[i][1] = ce1;
                addUniqueCE(ce0);
                addUniqueCE(ce1);
            } else {
                // bail out for c
                charCEs[i][0] = ce0 = Collation.NO_CE;
                charCEs[i][1] = ce1 = 0;
            }
            if(c == 0 && !isContractionCharCE(ce0)) {
                // Always map U+0000 to a contraction.
                // Write a contraction list with only a default value if there is no real contraction.
                assert(contractionCEs.isEmpty());
                addContractionEntry(CollationFastLatin.CONTR_CHAR_MASK, ce0, ce1);
                charCEs[0][0] = (Collation.NO_CE_PRIMARY << 32) | CONTRACTION_FLAG;
                charCEs[0][1] = 0;
            }
        }
        // Terminate the last contraction list.
        contractionCEs.addElement(CollationFastLatin.CONTR_CHAR_MASK);
    }

    private boolean getCEsFromCE32(CollationData data, int c, int ce32) {
        ce32 = data.getFinalCE32(ce32);
        ce1 = 0;
        if(Collation.isSimpleOrLongCE32(ce32)) {
            ce0 = Collation.ceFromCE32(ce32);
        } else {
            switch(Collation.tagFromCE32(ce32)) {
            case Collation.LATIN_EXPANSION_TAG:
                ce0 = Collation.latinCE0FromCE32(ce32);
                ce1 = Collation.latinCE1FromCE32(ce32);
                break;
            case Collation.EXPANSION32_TAG: {
                int index = Collation.indexFromCE32(ce32);
                int length = Collation.lengthFromCE32(ce32);
                if(length <= 2) {
                    ce0 = Collation.ceFromCE32(data.ce32s[index]);
                    if(length == 2) {
                        ce1 = Collation.ceFromCE32(data.ce32s[index + 1]);
                    }
                    break;
                } else {
                    return false;
                }
            }
            case Collation.EXPANSION_TAG: {
                int index = Collation.indexFromCE32(ce32);
                int length = Collation.lengthFromCE32(ce32);
                if(length <= 2) {
                    ce0 = data.ces[index];
                    if(length == 2) {
                        ce1 = data.ces[index + 1];
                    }
                    break;
                } else {
                    return false;
                }
            }
            // Note: We could support PREFIX_TAG (assert c>=0)
            // by recursing on its default CE32 and checking that none of the prefixes starts
            // with a fast Latin character.
            // However, currently (2013) there are only the L-before-middle-dot
            // prefix mappings in the Latin range, and those would be rejected anyway.
            case Collation.CONTRACTION_TAG:
                assert(c >= 0);
                return getCEsFromContractionCE32(data, ce32);
            case Collation.OFFSET_TAG:
                assert(c >= 0);
                ce0 = data.getCEFromOffsetCE32(c, ce32);
                break;
            default:
                return false;
            }
        }
        // A mapping can be completely ignorable.
        if(ce0 == 0) { return ce1 == 0; }
        // We do not support an ignorable ce0 unless it is completely ignorable.
        long p0 = ce0 >>> 32;
        if(p0 == 0) { return false; }
        // We only support primaries up to the Latin script.
        if(p0 > lastLatinPrimary) { return false; }
        // We support non-common secondary and case weights only together with short primaries.
        int lower32_0 = (int)ce0;
        if(p0 < firstShortPrimary) {
            int sc0 = lower32_0 & Collation.SECONDARY_AND_CASE_MASK;
            if(sc0 != Collation.COMMON_SECONDARY_CE) { return false; }
        }
        // No below-common tertiary weights.
        if((lower32_0 & Collation.ONLY_TERTIARY_MASK) < Collation.COMMON_WEIGHT16) { return false; }
        if(ce1 != 0) {
            // Both primaries must be in the same group,
            // or both must get short mini primaries,
            // or a short-primary CE is followed by a secondary CE.
            // This is so that we can test the first primary and use the same mask for both,
            // and determine for both whether they are variable.
            long p1 = ce1 >>> 32;
            if(p1 == 0 ? p0 < firstShortPrimary : !inSameGroup(p0, p1)) { return false; }
            int lower32_1 = (int)ce1;
            // No tertiary CEs.
            if((lower32_1 >>> 16) == 0) { return false; }
            // We support non-common secondary and case weights
            // only for secondary CEs or together with short primaries.
            if(p1 != 0 && p1 < firstShortPrimary) {
                int sc1 = lower32_1 & Collation.SECONDARY_AND_CASE_MASK;
                if(sc1 != Collation.COMMON_SECONDARY_CE) { return false; }
            }
            // No below-common tertiary weights.
            if((lower32_0 & Collation.ONLY_TERTIARY_MASK) < Collation.COMMON_WEIGHT16) { return false; }
        }
        // No quaternary weights.
        if(((ce0 | ce1) & Collation.QUATERNARY_MASK) != 0) { return false; }
        return true;
    }

    private boolean getCEsFromContractionCE32(CollationData data, int ce32) {
        int trieIndex = Collation.indexFromCE32(ce32);
        ce32 = data.getCE32FromContexts(trieIndex);  // Default if no suffix match.
        // Since the original ce32 is not a prefix mapping,
        // the default ce32 must not be another contraction.
        assert(!Collation.isContractionCE32(ce32));
        int contractionIndex = contractionCEs.size();
        if(getCEsFromCE32(data, Collation.SENTINEL_CP, ce32)) {
            addContractionEntry(CollationFastLatin.CONTR_CHAR_MASK, ce0, ce1);
        } else {
            // Bail out for c-without-contraction.
            addContractionEntry(CollationFastLatin.CONTR_CHAR_MASK, Collation.NO_CE, 0);
        }
        // Handle an encodable contraction unless the next contraction is too long
        // and starts with the same character.
        int prevX = -1;
        boolean addContraction = false;
        CharsTrie.Iterator suffixes = CharsTrie.iterator(data.contexts, trieIndex + 2, 0);
        while(suffixes.hasNext()) {
            CharsTrie.Entry entry = suffixes.next();
            CharSequence suffix = entry.chars;
            int x = CollationFastLatin.getCharIndex(suffix.charAt(0));
            if(x < 0) { continue; }  // ignore anything but fast Latin text
            if(x == prevX) {
                if(addContraction) {
                    // Bail out for all contractions starting with this character.
                    addContractionEntry(x, Collation.NO_CE, 0);
                    addContraction = false;
                }
                continue;
            }
            if(addContraction) {
                addContractionEntry(prevX, ce0, ce1);
            }
            ce32 = entry.value;
            if(suffix.length() == 1 && getCEsFromCE32(data, Collation.SENTINEL_CP, ce32)) {
                addContraction = true;
            } else {
                addContractionEntry(x, Collation.NO_CE, 0);
                addContraction = false;
            }
            prevX = x;
        }
        if(addContraction) {
            addContractionEntry(prevX, ce0, ce1);
        }
        // Note: There might not be any fast Latin contractions, but
        // we need to enter contraction handling anyway so that we can bail out
        // when there is a non-fast-Latin character following.
        // For example: Danish &Y<<u+umlaut, when we compare Y vs. u\u0308 we need to see the
        // following umlaut and bail out, rather than return the difference of Y vs. u.
        ce0 = (Collation.NO_CE_PRIMARY << 32) | CONTRACTION_FLAG | contractionIndex;
        ce1 = 0;
        return true;
    }

    private void addContractionEntry(int x, long cce0, long cce1) {
        contractionCEs.addElement(x);
        contractionCEs.addElement(cce0);
        contractionCEs.addElement(cce1);
        addUniqueCE(cce0);
        addUniqueCE(cce1);
    }

    private void addUniqueCE(long ce) {
        if(ce == 0 || (ce >>> 32) == Collation.NO_CE_PRIMARY) { return; }
        ce &= ~(long)Collation.CASE_MASK;  // blank out case bits
        int i = binarySearch(uniqueCEs.getBuffer(), uniqueCEs.size(), ce);
        if(i < 0) {
            uniqueCEs.insertElementAt(ce, ~i);
        }
    }

    private int getMiniCE(long ce) {
        ce &= ~(long)Collation.CASE_MASK;  // blank out case bits
        int index = binarySearch(uniqueCEs.getBuffer(), uniqueCEs.size(), ce);
        assert(index >= 0);
        return miniCEs[index];
    }

    private void encodeUniqueCEs() {
        miniCEs = new char[uniqueCEs.size()];
        int group = 0;
        long lastGroupPrimary = lastSpecialPrimaries[group];
        // The lowest unique CE must be at least a secondary CE.
        assert(((int)uniqueCEs.elementAti(0) >>> 16) != 0);
        long prevPrimary = 0;
        int prevSecondary = 0;
        int pri = 0;
        int sec = 0;
        int ter = CollationFastLatin.COMMON_TER;
        for(int i = 0; i < uniqueCEs.size(); ++i) {
            long ce = uniqueCEs.elementAti(i);
            // Note: At least one of the p/s/t weights changes from one unique CE to the next.
            // (uniqueCEs does not store case bits.)
            long p = ce >>> 32;
            if(p != prevPrimary) {
                while(p > lastGroupPrimary) {
                    assert(pri <= CollationFastLatin.MAX_LONG);
                    // Set the group's header entry to the
                    // last "long primary" in or before the group.
                    result.setCharAt(1 + group, (char)pri);
                    if(++group < NUM_SPECIAL_GROUPS) {
                        lastGroupPrimary = lastSpecialPrimaries[group];
                    } else {
                        lastGroupPrimary = 0xffffffffL;
                        break;
                    }
                }
                if(p < firstShortPrimary) {
                    if(pri == 0) {
                        pri = CollationFastLatin.MIN_LONG;
                    } else if(pri < CollationFastLatin.MAX_LONG) {
                        pri += CollationFastLatin.LONG_INC;
                    } else {
    /* #if DEBUG_COLLATION_FAST_LATIN_BUILDER
                        printf("long-primary overflow for %08x\n", p);
    #endif */
                        miniCEs[i] = CollationFastLatin.BAIL_OUT;
                        continue;
                    }
                } else {
                    if(pri < CollationFastLatin.MIN_SHORT) {
                        pri = CollationFastLatin.MIN_SHORT;
                    } else if(pri < (CollationFastLatin.MAX_SHORT - CollationFastLatin.SHORT_INC)) {
                        // Reserve the highest primary weight for U+FFFF.
                        pri += CollationFastLatin.SHORT_INC;
                    } else {
    /* #if DEBUG_COLLATION_FAST_LATIN_BUILDER
                        printf("short-primary overflow for %08x\n", p);
    #endif */
                        shortPrimaryOverflow = true;
                        miniCEs[i] = CollationFastLatin.BAIL_OUT;
                        continue;
                    }
                }
                prevPrimary = p;
                prevSecondary = Collation.COMMON_WEIGHT16;
                sec = CollationFastLatin.COMMON_SEC;
                ter = CollationFastLatin.COMMON_TER;
            }
            int lower32 = (int)ce;
            int s = lower32 >>> 16;
            if(s != prevSecondary) {
                if(pri == 0) {
                    if(sec == 0) {
                        sec = CollationFastLatin.MIN_SEC_HIGH;
                    } else if(sec < CollationFastLatin.MAX_SEC_HIGH) {
                        sec += CollationFastLatin.SEC_INC;
                    } else {
                        miniCEs[i] = CollationFastLatin.BAIL_OUT;
                        continue;
                    }
                    prevSecondary = s;
                    ter = CollationFastLatin.COMMON_TER;
                } else if(s < Collation.COMMON_WEIGHT16) {
                    if(sec == CollationFastLatin.COMMON_SEC) {
                        sec = CollationFastLatin.MIN_SEC_BEFORE;
                    } else if(sec < CollationFastLatin.MAX_SEC_BEFORE) {
                        sec += CollationFastLatin.SEC_INC;
                    } else {
                        miniCEs[i] = CollationFastLatin.BAIL_OUT;
                        continue;
                    }
                } else if(s == Collation.COMMON_WEIGHT16) {
                    sec = CollationFastLatin.COMMON_SEC;
                } else {
                    if(sec < CollationFastLatin.MIN_SEC_AFTER) {
                        sec = CollationFastLatin.MIN_SEC_AFTER;
                    } else if(sec < CollationFastLatin.MAX_SEC_AFTER) {
                        sec += CollationFastLatin.SEC_INC;
                    } else {
                        miniCEs[i] = CollationFastLatin.BAIL_OUT;
                        continue;
                    }
                }
                prevSecondary = s;
                ter = CollationFastLatin.COMMON_TER;
            }
            assert((lower32 & Collation.CASE_MASK) == 0);  // blanked out in uniqueCEs
            int t = lower32 & Collation.ONLY_TERTIARY_MASK;
            if(t > Collation.COMMON_WEIGHT16) {
                if(ter < CollationFastLatin.MAX_TER_AFTER) {
                    ++ter;
                } else {
                    miniCEs[i] = CollationFastLatin.BAIL_OUT;
                    continue;
                }
            }
            if(CollationFastLatin.MIN_LONG <= pri && pri <= CollationFastLatin.MAX_LONG) {
                assert(sec == CollationFastLatin.COMMON_SEC);
                miniCEs[i] = (char)(pri | ter);
            } else {
                miniCEs[i] = (char)(pri | sec | ter);
            }
        }
    /* #if DEBUG_COLLATION_FAST_LATIN_BUILDER
        printf("last mini primary: %04x\n", pri);
    #endif */
    /* #if DEBUG_COLLATION_FAST_LATIN_BUILDER >= 2
        for(int i = 0; i < uniqueCEs.size(); ++i) {
            long ce = uniqueCEs.elementAti(i);
            printf("unique CE 0x%016lx -> 0x%04x\n", ce, miniCEs[i]);
        }
    #endif */
    }

    private void encodeCharCEs() {
        int miniCEsStart = result.length();
        for(int i = 0; i < CollationFastLatin.NUM_FAST_CHARS; ++i) {
            result.append(0);  // initialize to completely ignorable
        }
        int indexBase = result.length();
        for(int i = 0; i < CollationFastLatin.NUM_FAST_CHARS; ++i) {
            long ce = charCEs[i][0];
            if(isContractionCharCE(ce)) { continue; }  // defer contraction
            int miniCE = encodeTwoCEs(ce, charCEs[i][1]);
            if((miniCE >>> 16) > 0) {   // if ((unsigned)miniCE > 0xffff)
                // Note: There is a chance that this new expansion is the same as a previous one,
                // and if so, then we could reuse the other expansion.
                // However, that seems unlikely.
                int expansionIndex = result.length() - indexBase;
                if(expansionIndex > CollationFastLatin.INDEX_MASK) {
                    miniCE = CollationFastLatin.BAIL_OUT;
                } else {
                    result.append((char)(miniCE >> 16)).append((char)miniCE);
                    miniCE = CollationFastLatin.EXPANSION | expansionIndex;
                }
            }
            result.setCharAt(miniCEsStart + i, (char)miniCE);
        }
    }

    private void encodeContractions() {
        // We encode all contraction lists so that the first word of a list
        // terminates the previous list, and we only need one additional terminator at the end.
        int indexBase = headerLength + CollationFastLatin.NUM_FAST_CHARS;
        int firstContractionIndex = result.length();
        for(int i = 0; i < CollationFastLatin.NUM_FAST_CHARS; ++i) {
            long ce = charCEs[i][0];
            if(!isContractionCharCE(ce)) { continue; }
            int contractionIndex = result.length() - indexBase;
            if(contractionIndex > CollationFastLatin.INDEX_MASK) {
                result.setCharAt(headerLength + i, (char) CollationFastLatin.BAIL_OUT);
                continue;
            }
            boolean firstTriple = true;
            for(int index = (int)ce & 0x7fffffff;; index += 3) {
                long x = contractionCEs.elementAti(index);
                if(x == CollationFastLatin.CONTR_CHAR_MASK && !firstTriple) { break; }
                long cce0 = contractionCEs.elementAti(index + 1);
                long cce1 = contractionCEs.elementAti(index + 2);
                int miniCE = encodeTwoCEs(cce0, cce1);
                if(miniCE == CollationFastLatin.BAIL_OUT) {
                    result.append((char)(x | (1 << CollationFastLatin.CONTR_LENGTH_SHIFT)));
                } else if((miniCE >>> 16) == 0) {  // if ((unsigned)miniCE <= 0xffff)
                    result.append((char)(x | (2 << CollationFastLatin.CONTR_LENGTH_SHIFT)));
                    result.append((char)miniCE);
                } else {
                    result.append((char)(x | (3 << CollationFastLatin.CONTR_LENGTH_SHIFT)));
                    result.append((char)(miniCE >> 16)).append((char)miniCE);
                }
                firstTriple = false;
            }
            // Note: There is a chance that this new contraction list is the same as a previous one,
            // and if so, then we could truncate the result and reuse the other list.
            // However, that seems unlikely.
            result.setCharAt(headerLength + i,
                            (char)(CollationFastLatin.CONTRACTION | contractionIndex));
        }
        if(result.length() > firstContractionIndex) {
            // Terminate the last contraction list.
            result.append((char)CollationFastLatin.CONTR_CHAR_MASK);
        }
    /* #if DEBUG_COLLATION_FAST_LATIN_BUILDER
        printf("** fast Latin %d * 2 = %d bytes\n", result.length(), result.length() * 2);
        puts("   header & below-digit groups map");
        int i = 0;
        for(; i < headerLength; ++i) {
            printf(" %04x", result[i]);
        }
        printf("\n   char mini CEs");
        assert(CollationFastLatin.NUM_FAST_CHARS % 16 == 0);
        for(; i < indexBase; i += 16) {
            int c = i - headerLength;
            if(c >= CollationFastLatin.LATIN_LIMIT) {
                c = CollationFastLatin.PUNCT_START + c - CollationFastLatin.LATIN_LIMIT;
            }
            printf("\n %04x:", c);
            for(int j = 0; j < 16; ++j) {
                printf(" %04x", result[i + j]);
            }
        }
        printf("\n   expansions & contractions");
        for(; i < result.length(); ++i) {
            if((i - indexBase) % 16 == 0) { puts(""); }
            printf(" %04x", result[i]);
        }
        puts("");
    #endif */
    }

    private int encodeTwoCEs(long first, long second) {
        if(first == 0) {
            return 0;  // completely ignorable
        }
        if(first == Collation.NO_CE) {
            return CollationFastLatin.BAIL_OUT;
        }
        assert((first >>> 32) != Collation.NO_CE_PRIMARY);

        int miniCE = getMiniCE(first);
        if(miniCE == CollationFastLatin.BAIL_OUT) { return miniCE; }
        if(miniCE >= CollationFastLatin.MIN_SHORT) {
            // Extract & copy the case bits.
            // Shift them from normal CE bits 15..14 to mini CE bits 4..3.
            int c = (((int)first & Collation.CASE_MASK) >> (14 - 3));
            // Only in mini CEs: Ignorable case bits = 0, lowercase = 1.
            c += CollationFastLatin.LOWER_CASE;
            miniCE |= c;
        }
        if(second == 0) { return miniCE; }

        int miniCE1 = getMiniCE(second);
        if(miniCE1 == CollationFastLatin.BAIL_OUT) { return miniCE1; }

        int case1 = (int)second & Collation.CASE_MASK;
        if(miniCE >= CollationFastLatin.MIN_SHORT &&
                (miniCE & CollationFastLatin.SECONDARY_MASK) == CollationFastLatin.COMMON_SEC) {
            // Try to combine the two mini CEs into one.
            int sec1 = miniCE1 & CollationFastLatin.SECONDARY_MASK;
            int ter1 = miniCE1 & CollationFastLatin.TERTIARY_MASK;
            if(sec1 >= CollationFastLatin.MIN_SEC_HIGH && case1 == 0 &&
                    ter1 == CollationFastLatin.COMMON_TER) {
                // sec1>=sec_high implies pri1==0.
                return (miniCE & ~CollationFastLatin.SECONDARY_MASK) | sec1;
            }
        }

        if(miniCE1 <= CollationFastLatin.SECONDARY_MASK || CollationFastLatin.MIN_SHORT <= miniCE1) {
            // Secondary CE, or a CE with a short primary, copy the case bits.
            case1 = (case1 >> (14 - 3)) + CollationFastLatin.LOWER_CASE;
            miniCE1 |= case1;
        }
        return (miniCE << 16) | miniCE1;
    }

    private static boolean isContractionCharCE(long ce) {
        return (ce >>> 32) == Collation.NO_CE_PRIMARY && ce != Collation.NO_CE;
    }

    // space, punct, symbol, currency (not digit)
    private static final int NUM_SPECIAL_GROUPS =
            Collator.ReorderCodes.CURRENCY - Collator.ReorderCodes.FIRST + 1;

    private static final long CONTRACTION_FLAG = 0x80000000L;

    // temporary "buffer"
    private long ce0, ce1;

    private long[][] charCEs = new long[CollationFastLatin.NUM_FAST_CHARS][2];

    private UVector64 contractionCEs;
    private UVector64 uniqueCEs;

    /** One 16-bit mini CE per unique CE. */
    private char[] miniCEs;

    // These are constant for a given root collator.
    long[] lastSpecialPrimaries = new long[NUM_SPECIAL_GROUPS];
    private long firstDigitPrimary;
    private long firstLatinPrimary;
    private long lastLatinPrimary;
    // This determines the first normal primary weight which is mapped to
    // a short mini primary. It must be >=firstDigitPrimary.
    private long firstShortPrimary;

    private boolean shortPrimaryOverflow;

    private StringBuilder result = new StringBuilder();
    private int headerLength;
}
