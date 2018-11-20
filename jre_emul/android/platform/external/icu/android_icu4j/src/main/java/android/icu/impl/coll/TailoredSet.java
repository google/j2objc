/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* TailoredSet.java, ported from collationsets.h/.cpp
*
* C++ version created on: 2013feb09
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import java.util.Iterator;

import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.impl.Trie2;
import android.icu.impl.Utility;
import android.icu.text.UnicodeSet;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrie.Entry;

/**
 * Finds the set of characters and strings that sort differently in the tailoring
 * from the base data.
 *
 * Every mapping in the tailoring needs to be compared to the base,
 * because some mappings are copied for optimization, and
 * all contractions for a character are copied if any contractions for that character
 * are added, modified or removed.
 *
 * It might be simpler to re-parse the rule string, but:
 * - That would require duplicating some of the from-rules builder code.
 * - That would make the runtime code depend on the builder.
 * - That would only work if we have the rule string, and we allow users to
 *   omit the rule string from data files.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class TailoredSet {

    private CollationData data;
    private CollationData baseData;
    private UnicodeSet tailored;
    private StringBuilder unreversedPrefix = new StringBuilder();
    private String suffix;

    public TailoredSet(UnicodeSet t) {
        tailored = t;
    }

    public void forData(CollationData d) {
        data = d;
        baseData = d.base;
        assert (baseData != null);
        // utrie2_enum(data->trie, NULL, enumTailoredRange, this);
        Iterator<Trie2.Range> trieIterator = data.trie.iterator();
        Trie2.Range range;
        while (trieIterator.hasNext() && !(range = trieIterator.next()).leadSurrogate) {
            enumTailoredRange(range.startCodePoint, range.endCodePoint, range.value, this);
        }
    }

    private void enumTailoredRange(int start, int end, int ce32, TailoredSet ts) {
        if (ce32 == Collation.FALLBACK_CE32) {
            return; // fallback to base, not tailored
        }
        ts.handleCE32(start, end, ce32);
    }

    // Java porting note: ICU4C returns U_SUCCESS(error) and it's not applicable to ICU4J.
    //  Also, ICU4C requires handleCE32() to be public because it is used by the callback
    //  function (enumTailoredRange()). This is not necessary for Java implementation.
    private void handleCE32(int start, int end, int ce32) {
        assert (ce32 != Collation.FALLBACK_CE32);
        if (Collation.isSpecialCE32(ce32)) {
            ce32 = data.getIndirectCE32(ce32);
            if (ce32 == Collation.FALLBACK_CE32) {
                return;
            }
        }
        do {
            int baseCE32 = baseData.getFinalCE32(baseData.getCE32(start));
            // Do not just continue if ce32 == baseCE32 because
            // contractions and expansions in different data objects
            // normally differ even if they have the same data offsets.
            if (Collation.isSelfContainedCE32(ce32) && Collation.isSelfContainedCE32(baseCE32)) {
                // fastpath
                if (ce32 != baseCE32) {
                    tailored.add(start);
                }
            } else {
                compare(start, ce32, baseCE32);
            }
        } while (++start <= end);
    }

    private void compare(int c, int ce32, int baseCE32) {
        if (Collation.isPrefixCE32(ce32)) {
            int dataIndex = Collation.indexFromCE32(ce32);
            ce32 = data.getFinalCE32(data.getCE32FromContexts(dataIndex));
            if (Collation.isPrefixCE32(baseCE32)) {
                int baseIndex = Collation.indexFromCE32(baseCE32);
                baseCE32 = baseData.getFinalCE32(baseData.getCE32FromContexts(baseIndex));
                comparePrefixes(c, data.contexts, dataIndex + 2, baseData.contexts, baseIndex + 2);
            } else {
                addPrefixes(data, c, data.contexts, dataIndex + 2);
            }
        } else if (Collation.isPrefixCE32(baseCE32)) {
            int baseIndex = Collation.indexFromCE32(baseCE32);
            baseCE32 = baseData.getFinalCE32(baseData.getCE32FromContexts(baseIndex));
            addPrefixes(baseData, c, baseData.contexts, baseIndex + 2);
        }

        if (Collation.isContractionCE32(ce32)) {
            int dataIndex = Collation.indexFromCE32(ce32);
            if ((ce32 & Collation.CONTRACT_SINGLE_CP_NO_MATCH) != 0) {
                ce32 = Collation.NO_CE32;
            } else {
                ce32 = data.getFinalCE32(data.getCE32FromContexts(dataIndex));
            }
            if (Collation.isContractionCE32(baseCE32)) {
                int baseIndex = Collation.indexFromCE32(baseCE32);
                if ((baseCE32 & Collation.CONTRACT_SINGLE_CP_NO_MATCH) != 0) {
                    baseCE32 = Collation.NO_CE32;
                } else {
                    baseCE32 = baseData.getFinalCE32(baseData.getCE32FromContexts(baseIndex));
                }
                compareContractions(c, data.contexts, dataIndex + 2, baseData.contexts, baseIndex + 2);
            } else {
                addContractions(c, data.contexts, dataIndex + 2);
            }
        } else if (Collation.isContractionCE32(baseCE32)) {
            int baseIndex = Collation.indexFromCE32(baseCE32);
            baseCE32 = baseData.getFinalCE32(baseData.getCE32FromContexts(baseIndex));
            addContractions(c, baseData.contexts, baseIndex + 2);
        }

        int tag;
        if (Collation.isSpecialCE32(ce32)) {
            tag = Collation.tagFromCE32(ce32);
            assert (tag != Collation.PREFIX_TAG);
            assert (tag != Collation.CONTRACTION_TAG);
            // Currently, the tailoring data builder does not write offset tags.
            // They might be useful for saving space,
            // but they would complicate the builder,
            // and in tailorings we assume that performance of tailored characters is more important.
            assert (tag != Collation.OFFSET_TAG);
        } else {
            tag = -1;
        }
        int baseTag;
        if (Collation.isSpecialCE32(baseCE32)) {
            baseTag = Collation.tagFromCE32(baseCE32);
            assert (baseTag != Collation.PREFIX_TAG);
            assert (baseTag != Collation.CONTRACTION_TAG);
        } else {
            baseTag = -1;
        }

        // Non-contextual mappings, expansions, etc.
        if (baseTag == Collation.OFFSET_TAG) {
            // We might be comparing a tailoring CE which is a copy of
            // a base offset-tag CE, via the [optimize [set]] syntax
            // or when a single-character mapping was copied for tailored contractions.
            // Offset tags always result in long-primary CEs,
            // with common secondary/tertiary weights.
            if (!Collation.isLongPrimaryCE32(ce32)) {
                add(c);
                return;
            }
            long dataCE = baseData.ces[Collation.indexFromCE32(baseCE32)];
            long p = Collation.getThreeBytePrimaryForOffsetData(c, dataCE);
            if (Collation.primaryFromLongPrimaryCE32(ce32) != p) {
                add(c);
                return;
            }
        }

        if (tag != baseTag) {
            add(c);
            return;
        }

        if (tag == Collation.EXPANSION32_TAG) {
            int length = Collation.lengthFromCE32(ce32);
            int baseLength = Collation.lengthFromCE32(baseCE32);

            if (length != baseLength) {
                add(c);
                return;
            }

            int idx0 = Collation.indexFromCE32(ce32);
            int idx1 = Collation.indexFromCE32(baseCE32);

            for (int i = 0; i < length; ++i) {
                if (data.ce32s[idx0 + i] != baseData.ce32s[idx1 + i]) {
                    add(c);
                    break;
                }
            }
        } else if (tag == Collation.EXPANSION_TAG) {
            int length = Collation.lengthFromCE32(ce32);
            int baseLength = Collation.lengthFromCE32(baseCE32);

            if (length != baseLength) {
                add(c);
                return;
            }

            int idx0 = Collation.indexFromCE32(ce32);
            int idx1 = Collation.indexFromCE32(baseCE32);

            for (int i = 0; i < length; ++i) {
                if (data.ces[idx0 + i] != baseData.ces[idx1 + i]) {
                    add(c);
                    break;
                }
            }
        } else if (tag == Collation.HANGUL_TAG) {
            StringBuilder jamos = new StringBuilder();
            int length = Hangul.decompose(c, jamos);
            if (tailored.contains(jamos.charAt(0)) || tailored.contains(jamos.charAt(1))
                    || (length == 3 && tailored.contains(jamos.charAt(2)))) {
                add(c);
            }
        } else if (ce32 != baseCE32) {
            add(c);
        }
    }

    private void comparePrefixes(int c, CharSequence p, int pidx, CharSequence q, int qidx) {
        // Parallel iteration over prefixes of both tables.
        CharsTrie.Iterator prefixes = new CharsTrie(p, pidx).iterator();
        CharsTrie.Iterator basePrefixes = new CharsTrie(q, qidx).iterator();
        String tp = null; // Tailoring prefix.
        String bp = null; // Base prefix.
        // Use a string with a U+FFFF as the limit sentinel.
        // U+FFFF is untailorable and will not occur in prefixes.
        String none = "\uffff";
        Entry te = null, be = null;
        for (;;) {
            if (tp == null) {
                if (prefixes.hasNext()) {
                    te = prefixes.next();
                    tp = te.chars.toString();
                } else {
                    te = null;
                    tp = none;
                }
            }
            if (bp == null) {
                if (basePrefixes.hasNext()) {
                    be = basePrefixes.next();
                    bp = be.chars.toString();
                } else {
                    be = null;
                    bp = none;
                }
            }
            if (Utility.sameObjects(tp, none) && Utility.sameObjects(bp, none)) {
                break;
            }
            int cmp = tp.compareTo(bp);
            if (cmp < 0) {
                // tp occurs in the tailoring but not in the base.
                assert (te != null);
                addPrefix(data, tp, c, te.value);
                te = null;
                tp = null;
            } else if (cmp > 0) {
                // bp occurs in the base but not in the tailoring.
                assert (be != null);
                addPrefix(baseData, bp, c, be.value);
                be = null;
                bp = null;
            } else {
                setPrefix(tp);
                assert (te != null && be != null);
                compare(c, te.value, be.value);
                resetPrefix();
                te = be = null;
                tp = bp = null;
            }
        }
    }

    private void compareContractions(int c, CharSequence p, int pidx, CharSequence q, int qidx) {
        // Parallel iteration over suffixes of both tables.
        CharsTrie.Iterator suffixes = new CharsTrie(p, pidx).iterator();
        CharsTrie.Iterator baseSuffixes = new CharsTrie(q, qidx).iterator();
        String ts = null; // Tailoring suffix.
        String bs = null; // Base suffix.
        // Use a string with two U+FFFF as the limit sentinel.
        // U+FFFF is untailorable and will not occur in contractions except maybe
        // as a single suffix character for a root-collator boundary contraction.
        String none = "\uffff\uffff";
        Entry te = null, be = null;
        for (;;) {
            if (ts == null) {
                if (suffixes.hasNext()) {
                    te = suffixes.next();
                    ts = te.chars.toString();
                } else {
                    te = null;
                    ts = none;
                }
            }
            if (bs == null) {
                if (baseSuffixes.hasNext()) {
                    be = baseSuffixes.next();
                    bs = be.chars.toString();
                } else {
                    be = null;
                    bs = none;
                }
            }
            if (Utility.sameObjects(ts, none) && Utility.sameObjects(bs, none)) {
                break;
            }
            int cmp = ts.compareTo(bs);
            if (cmp < 0) {
                // ts occurs in the tailoring but not in the base.
                addSuffix(c, ts);
                te = null;
                ts = null;
            } else if (cmp > 0) {
                // bs occurs in the base but not in the tailoring.
                addSuffix(c, bs);
                be = null;
                bs = null;
            } else {
                suffix = ts;
                compare(c, te.value, be.value);
                suffix = null;
                te = be = null;
                ts = bs = null;
            }
        }
    }

    private void addPrefixes(CollationData d, int c, CharSequence p, int pidx) {
        CharsTrie.Iterator prefixes = new CharsTrie(p, pidx).iterator();
        while (prefixes.hasNext()) {
            Entry e = prefixes.next();
            addPrefix(d, e.chars, c, e.value);
        }
    }

    private void addPrefix(CollationData d, CharSequence pfx, int c, int ce32) {
        setPrefix(pfx);
        ce32 = d.getFinalCE32(ce32);
        if (Collation.isContractionCE32(ce32)) {
            int idx = Collation.indexFromCE32(ce32);
            addContractions(c, d.contexts, idx + 2);
        }
        tailored.add(new StringBuilder(unreversedPrefix.appendCodePoint(c)));
        resetPrefix();
    }

    private void addContractions(int c, CharSequence p, int pidx) {
        CharsTrie.Iterator suffixes = new CharsTrie(p, pidx).iterator();
        while (suffixes.hasNext()) {
            Entry e = suffixes.next();
            addSuffix(c, e.chars);
        }
    }

    private void addSuffix(int c, CharSequence sfx) {
        tailored.add(new StringBuilder(unreversedPrefix).appendCodePoint(c).append(sfx));
    }

    private void add(int c) {
        if (unreversedPrefix.length() == 0 && suffix == null) {
            tailored.add(c);
        } else {
            StringBuilder s = new StringBuilder(unreversedPrefix);
            s.appendCodePoint(c);
            if (suffix != null) {
                s.append(suffix);
            }
            tailored.add(s);
        }
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

