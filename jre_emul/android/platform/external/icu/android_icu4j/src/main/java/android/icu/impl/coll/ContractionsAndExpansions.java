/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* ContractionsAndExpansions.java, ported from collationsets.h/.cpp
*
* C++ version created on: 2013feb09
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import java.util.Iterator;

import android.icu.impl.Trie2;
import android.icu.text.UnicodeSet;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrie.Entry;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public final class ContractionsAndExpansions {
    // C++: The following fields are @internal, only public for access by callback.
    private CollationData data;
    private UnicodeSet contractions;
    private UnicodeSet expansions;
    private CESink sink;
    private boolean addPrefixes;
    private int checkTailored = 0;  // -1: collected tailored  +1: exclude tailored
    private UnicodeSet tailored = new UnicodeSet();
    private UnicodeSet ranges;
    private StringBuilder unreversedPrefix = new StringBuilder();
    private String suffix;
    private long[] ces = new long[Collation.MAX_EXPANSION_LENGTH];

    public static interface CESink {
        void handleCE(long ce);
        void handleExpansion(long ces[], int start, int length);
    }

    public ContractionsAndExpansions(UnicodeSet con, UnicodeSet exp, CESink s, boolean prefixes) {
        contractions = con;
        expansions = exp;
        sink = s;
        addPrefixes = prefixes;
    }

    public void forData(CollationData d) {
        // Add all from the data, can be tailoring or base.
        if (d.base != null) {
            checkTailored = -1;
        }
        data = d;
        Iterator<Trie2.Range> trieIterator = data.trie.iterator();
        Trie2.Range range;
        while (trieIterator.hasNext() && !(range = trieIterator.next()).leadSurrogate) {
            enumCnERange(range.startCodePoint, range.endCodePoint, range.value, this);
        }
        if (d.base == null) {
            return;
        }
        // Add all from the base data but only for un-tailored code points.
        tailored.freeze();
        checkTailored = 1;
        data = d.base;
        trieIterator = data.trie.iterator();
        while (trieIterator.hasNext() && !(range = trieIterator.next()).leadSurrogate) {
            enumCnERange(range.startCodePoint, range.endCodePoint, range.value, this);
        }
    }

    private void enumCnERange(int start, int end, int ce32, ContractionsAndExpansions cne) {
        if (cne.checkTailored == 0) {
            // There is no tailoring.
            // No need to collect nor check the tailored set.
        } else if (cne.checkTailored < 0) {
            // Collect the set of code points with mappings in the tailoring data.
            if (ce32 == Collation.FALLBACK_CE32) {
                return; // fallback to base, not tailored
            } else {
                cne.tailored.add(start, end);
            }
            // checkTailored > 0: Exclude tailored ranges from the base data enumeration.
        } else if (start == end) {
            if (cne.tailored.contains(start)) {
                return;
            }
        } else if (cne.tailored.containsSome(start, end)) {
            if (cne.ranges == null) {
                cne.ranges = new UnicodeSet();
            }
            cne.ranges.set(start, end).removeAll(cne.tailored);
            int count = cne.ranges.getRangeCount();
            for (int i = 0; i < count; ++i) {
                cne.handleCE32(cne.ranges.getRangeStart(i), cne.ranges.getRangeEnd(i), ce32);
            }
        }
        cne.handleCE32(start, end, ce32);
    }

    public void forCodePoint(CollationData d, int c) {
        int ce32 = d.getCE32(c);
        if (ce32 == Collation.FALLBACK_CE32) {
            d = d.base;
            ce32 = d.getCE32(c);
        }
        data = d;
        handleCE32(c, c, ce32);
    }

    private void handleCE32(int start, int end, int ce32) {
        for (;;) {
            if ((ce32 & 0xff) < Collation.SPECIAL_CE32_LOW_BYTE) {
                // !isSpecialCE32()
                if (sink != null) {
                    sink.handleCE(Collation.ceFromSimpleCE32(ce32));
                }
                return;
            }
            switch (Collation.tagFromCE32(ce32)) {
            case Collation.FALLBACK_TAG:
                return;
            case Collation.RESERVED_TAG_3:
            case Collation.BUILDER_DATA_TAG:
            case Collation.LEAD_SURROGATE_TAG:
                // Java porting note: U_INTERNAL_PROGRAM_ERROR is set to errorCode in ICU4C.
                throw new AssertionError(
                        String.format("Unexpected CE32 tag type %d for ce32=0x%08x",
                                Collation.tagFromCE32(ce32), ce32));
            case Collation.LONG_PRIMARY_TAG:
                if (sink != null) {
                    sink.handleCE(Collation.ceFromLongPrimaryCE32(ce32));
                }
                return;
            case Collation.LONG_SECONDARY_TAG:
                if (sink != null) {
                    sink.handleCE(Collation.ceFromLongSecondaryCE32(ce32));
                }
                return;
            case Collation.LATIN_EXPANSION_TAG:
                if (sink != null) {
                    ces[0] = Collation.latinCE0FromCE32(ce32);
                    ces[1] = Collation.latinCE1FromCE32(ce32);
                    sink.handleExpansion(ces, 0, 2);
                }
                // Optimization: If we have a prefix,
                // then the relevant strings have been added already.
                if (unreversedPrefix.length() == 0) {
                    addExpansions(start, end);
                }
                return;
            case Collation.EXPANSION32_TAG:
                if (sink != null) {
                    int idx = Collation.indexFromCE32(ce32);
                    int length = Collation.lengthFromCE32(ce32);
                    for (int i = 0; i < length; ++i) {
                        ces[i] = Collation.ceFromCE32(data.ce32s[idx + i]);
                    }
                    sink.handleExpansion(ces, 0, length);
                }
                // Optimization: If we have a prefix,
                // then the relevant strings have been added already.
                if (unreversedPrefix.length() == 0) {
                    addExpansions(start, end);
                }
                return;
            case Collation.EXPANSION_TAG:
                if (sink != null) {
                    int idx = Collation.indexFromCE32(ce32);
                    int length = Collation.lengthFromCE32(ce32);
                    sink.handleExpansion(data.ces, idx, length);
                }
                // Optimization: If we have a prefix,
                // then the relevant strings have been added already.
                if (unreversedPrefix.length() == 0) {
                    addExpansions(start, end);
                }
                return;
            case Collation.PREFIX_TAG:
                handlePrefixes(start, end, ce32);
                return;
            case Collation.CONTRACTION_TAG:
                handleContractions(start, end, ce32);
                return;
            case Collation.DIGIT_TAG:
                // Fetch the non-numeric-collation CE32 and continue.
                ce32 = data.ce32s[Collation.indexFromCE32(ce32)];
                break;
            case Collation.U0000_TAG:
                assert (start == 0 && end == 0);
                // Fetch the normal ce32 for U+0000 and continue.
                ce32 = data.ce32s[0];
                break;
            case Collation.HANGUL_TAG:
                if (sink != null) {
                    // TODO: This should be optimized,
                    // especially if [start..end] is the complete Hangul range. (assert that)
                    UTF16CollationIterator iter = new UTF16CollationIterator(data);
                    StringBuilder hangul = new StringBuilder(1);
                    for (int c = start; c <= end; ++c) {
                        hangul.setLength(0);
                        hangul.appendCodePoint(c);
                        iter.setText(false, hangul, 0);
                        int length = iter.fetchCEs();
                        // Ignore the terminating non-CE.
                        assert (length >= 2 && iter.getCE(length - 1) == Collation.NO_CE);
                        sink.handleExpansion(iter.getCEs(), 0, length - 1);
                    }
                }
                // Optimization: If we have a prefix,
                // then the relevant strings have been added already.
                if (unreversedPrefix.length() == 0) {
                    addExpansions(start, end);
                }
                return;
            case Collation.OFFSET_TAG:
                // Currently no need to send offset CEs to the sink.
                return;
            case Collation.IMPLICIT_TAG:
                // Currently no need to send implicit CEs to the sink.
                return;
            }
        }
    }

    private void handlePrefixes(int start, int end, int ce32) {
        int index = Collation.indexFromCE32(ce32);
        ce32 = data.getCE32FromContexts(index); // Default if no prefix match.
        handleCE32(start, end, ce32);
        if (!addPrefixes) {
            return;
        }
        CharsTrie.Iterator prefixes = new CharsTrie(data.contexts, index + 2).iterator();
        while (prefixes.hasNext()) {
            Entry e = prefixes.next();
            setPrefix(e.chars);
            // Prefix/pre-context mappings are special kinds of contractions
            // that always yield expansions.
            addStrings(start, end, contractions);
            addStrings(start, end, expansions);
            handleCE32(start, end, e.value);
        }
        resetPrefix();
    }

    void handleContractions(int start, int end, int ce32) {
        int index = Collation.indexFromCE32(ce32);
        if ((ce32 & Collation.CONTRACT_SINGLE_CP_NO_MATCH) != 0) {
            // No match on the single code point.
            // We are underneath a prefix, and the default mapping is just
            // a fallback to the mappings for a shorter prefix.
            assert (unreversedPrefix.length() != 0);
        } else {
            ce32 = data.getCE32FromContexts(index); // Default if no suffix match.
            assert (!Collation.isContractionCE32(ce32));
            handleCE32(start, end, ce32);
        }
        CharsTrie.Iterator suffixes = new CharsTrie(data.contexts, index + 2).iterator();
        while (suffixes.hasNext()) {
            Entry e = suffixes.next();
            suffix = e.chars.toString();
            addStrings(start, end, contractions);
            if (unreversedPrefix.length() != 0) {
                addStrings(start, end, expansions);
            }
            handleCE32(start, end, e.value);
        }
        suffix = null;
    }

    void addExpansions(int start, int end) {
        if (unreversedPrefix.length() == 0 && suffix == null) {
            if (expansions != null) {
                expansions.add(start, end);
            }
        } else {
            addStrings(start, end, expansions);
        }
    }

    void addStrings(int start, int end, UnicodeSet set) {
        if (set == null) {
            return;
        }
        StringBuilder s = new StringBuilder(unreversedPrefix);
        do {
            s.appendCodePoint(start);
            if (suffix != null) {
                s.append(suffix);
            }
            set.add(s);
            s.setLength(unreversedPrefix.length());
        } while (++start <= end);
    }

    // Prefixes are reversed in the data structure.
    private void setPrefix(CharSequence pfx) {
        unreversedPrefix.setLength(0);
        unreversedPrefix.append(pfx).reverse();
    }

    private void resetPrefix() {
        unreversedPrefix.setLength(0);
    }
}