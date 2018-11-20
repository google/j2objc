/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2012-2015, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationDataBuilder.java, ported from collationdatabuilder.h/.cpp
*
* C++ version created on: 2012apr01
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.impl.Trie2;
import android.icu.impl.Trie2Writable;
import android.icu.lang.UCharacter;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrieBuilder;
import android.icu.util.StringTrieBuilder;

/**
 * Low-level CollationData builder.
 * Takes (character, CE) pairs and builds them into runtime data structures.
 * Supports characters with context prefixes and contraction suffixes.
 */
final class CollationDataBuilder {  // not final in C++
    /**
     * Collation element modifier. Interface class for a modifier
     * that changes a tailoring builder's temporary CEs to final CEs.
     * Called for every non-special CE32 and every expansion CE.
     */
    interface CEModifier {
        /** Returns a new CE to replace the non-special input CE32, or else Collation.NO_CE. */
        long modifyCE32(int ce32);
        /** Returns a new CE to replace the input CE, or else Collation.NO_CE. */
        long modifyCE(long ce);
    }

    CollationDataBuilder() {
        nfcImpl = Norm2AllModes.getNFCInstance().impl;
        base = null;
        baseSettings = null;
        trie = null;
        ce32s = new UVector32();
        ce64s = new UVector64();
        conditionalCE32s = new ArrayList<ConditionalCE32>();
        modified = false;
        fastLatinEnabled = false;
        fastLatinBuilder = null;
        collIter = null;
        // Reserve the first CE32 for U+0000.
        ce32s.addElement(0);
    }

    void initForTailoring(CollationData b) {
        if(trie != null) {
            throw new IllegalStateException("attempt to reuse a CollationDataBuilder");
        }
        if(b == null) {
            throw new IllegalArgumentException("null CollationData");
        }
        base = b;

        // For a tailoring, the default is to fall back to the base.
        trie = new Trie2Writable(Collation.FALLBACK_CE32, Collation.FFFD_CE32);

        // Set the Latin-1 letters block so that it is allocated first in the data array,
        // to try to improve locality of reference when sorting Latin-1 text.
        // Do not use utrie2_setRange32() since that will not actually allocate blocks
        // that are filled with the default value.
        // ASCII (0..7F) is already preallocated anyway.
        for(int c = 0xc0; c <= 0xff; ++c) {
            trie.set(c, Collation.FALLBACK_CE32);
        }

        // Hangul syllables are not tailorable (except via tailoring Jamos).
        // Always set the Hangul tag to help performance.
        // Do this here, rather than in buildMappings(),
        // so that we see the HANGUL_TAG in various assertions.
        int hangulCE32 = Collation.makeCE32FromTagAndIndex(Collation.HANGUL_TAG, 0);
        trie.setRange(Hangul.HANGUL_BASE, Hangul.HANGUL_END, hangulCE32, true);

        // Copy the set contents but don't copy/clone the set as a whole because
        // that would copy the isFrozen state too.
        unsafeBackwardSet.addAll(b.unsafeBackwardSet);
    }

    boolean isCompressibleLeadByte(int b) {
        return base.isCompressibleLeadByte(b);
    }

    boolean isCompressiblePrimary(long p) {
        return isCompressibleLeadByte((int)p >>> 24);
    }

    /**
     * @return true if this builder has mappings (e.g., add() has been called)
     */
    boolean hasMappings() { return modified; }

    /**
     * @return true if c has CEs in this builder
     */
    boolean isAssigned(int c) {
        return Collation.isAssignedCE32(trie.get(c));
    }

    void add(CharSequence prefix, CharSequence s, long ces[], int cesLength) {
        int ce32 = encodeCEs(ces, cesLength);
        addCE32(prefix, s, ce32);
    }

    /**
     * Encodes the ces as either the returned ce32 by itself,
     * or by storing an expansion, with the returned ce32 referring to that.
     *
     * <p>add(p, s, ces, cesLength) = addCE32(p, s, encodeCEs(ces, cesLength))
     */
    int encodeCEs(long ces[], int cesLength) {
        if(cesLength < 0 || cesLength > Collation.MAX_EXPANSION_LENGTH) {
            throw new IllegalArgumentException("mapping to too many CEs");
        }
        if(!isMutable()) {
            throw new IllegalStateException("attempt to add mappings after build()");
        }
        if(cesLength == 0) {
            // Convenience: We cannot map to nothing, but we can map to a completely ignorable CE.
            // Do this here so that callers need not do it.
            return encodeOneCEAsCE32(0);
        } else if(cesLength == 1) {
            return encodeOneCE(ces[0]);
        } else if(cesLength == 2) {
            // Try to encode two CEs as one CE32.
            long ce0 = ces[0];
            long ce1 = ces[1];
            long p0 = ce0 >>> 32;
            if((ce0 & 0xffffffffff00ffL) == Collation.COMMON_SECONDARY_CE &&
                    (ce1 & 0xffffffff00ffffffL) == Collation.COMMON_TERTIARY_CE &&
                    p0 != 0) {
                // Latin mini expansion
                return
                    (int)p0 |
                    (((int)ce0 & 0xff00) << 8) |
                    (((int)ce1 >> 16) & 0xff00) |
                    Collation.SPECIAL_CE32_LOW_BYTE |
                    Collation.LATIN_EXPANSION_TAG;
            }
        }
        // Try to encode two or more CEs as CE32s.
        int[] newCE32s = new int[Collation.MAX_EXPANSION_LENGTH];  // TODO: instance field?
        for(int i = 0;; ++i) {
            if(i == cesLength) {
                return encodeExpansion32(newCE32s, 0, cesLength);
            }
            int ce32 = encodeOneCEAsCE32(ces[i]);
            if(ce32 == Collation.NO_CE32) { break; }
            newCE32s[i] = ce32;
        }
        return encodeExpansion(ces, 0, cesLength);
    }

    void addCE32(CharSequence prefix, CharSequence s, int ce32) {
        if(s.length() == 0) {
            throw new IllegalArgumentException("mapping from empty string");
        }
        if(!isMutable()) {
            throw new IllegalStateException("attempt to add mappings after build()");
        }
        int c = Character.codePointAt(s, 0);
        int cLength = Character.charCount(c);
        int oldCE32 = trie.get(c);
        boolean hasContext = prefix.length() != 0|| s.length() > cLength;
        if(oldCE32 == Collation.FALLBACK_CE32) {
            // First tailoring for c.
            // If c has contextual base mappings or if we add a contextual mapping,
            // then copy the base mappings.
            // Otherwise we just override the base mapping.
            int baseCE32 = base.getFinalCE32(base.getCE32(c));
            if(hasContext || Collation.ce32HasContext(baseCE32)) {
                oldCE32 = copyFromBaseCE32(c, baseCE32, true);
                trie.set(c, oldCE32);
            }
        }
        if(!hasContext) {
            // No prefix, no contraction.
            if(!isBuilderContextCE32(oldCE32)) {
                trie.set(c, ce32);
            } else {
                ConditionalCE32 cond = getConditionalCE32ForCE32(oldCE32);
                cond.builtCE32 = Collation.NO_CE32;
                cond.ce32 = ce32;
            }
        } else {
            ConditionalCE32 cond;
            if(!isBuilderContextCE32(oldCE32)) {
                // Replace the simple oldCE32 with a builder context CE32
                // pointing to a new ConditionalCE32 list head.
                int index = addConditionalCE32("\0", oldCE32);
                int contextCE32 = makeBuilderContextCE32(index);
                trie.set(c, contextCE32);
                contextChars.add(c);
                cond = getConditionalCE32(index);
            } else {
                cond = getConditionalCE32ForCE32(oldCE32);
                cond.builtCE32 = Collation.NO_CE32;
            }
            CharSequence suffix = s.subSequence(cLength, s.length());
            String context = new StringBuilder().append((char)prefix.length()).
                    append(prefix).append(suffix).toString();
            unsafeBackwardSet.addAll(suffix);
            for(;;) {
                // invariant: context > cond.context
                int next = cond.next;
                if(next < 0) {
                    // Append a new ConditionalCE32 after cond.
                    int index = addConditionalCE32(context, ce32);
                    cond.next = index;
                    break;
                }
                ConditionalCE32 nextCond = getConditionalCE32(next);
                int cmp = context.compareTo(nextCond.context);
                if(cmp < 0) {
                    // Insert a new ConditionalCE32 between cond and nextCond.
                    int index = addConditionalCE32(context, ce32);
                    cond.next = index;
                    getConditionalCE32(index).next = next;
                    break;
                } else if(cmp == 0) {
                    // Same context as before, overwrite its ce32.
                    nextCond.ce32 = ce32;
                    break;
                }
                cond = nextCond;
            }
        }
        modified = true;
    }

    /**
     * Copies all mappings from the src builder, with modifications.
     * This builder here must not be built yet, and should be empty.
     */
    void copyFrom(CollationDataBuilder src, CEModifier modifier) {
        if(!isMutable()) {
            throw new IllegalStateException("attempt to copyFrom() after build()");
        }
        CopyHelper helper = new CopyHelper(src, this, modifier);
        Iterator<Trie2.Range> trieIterator = src.trie.iterator();
        Trie2.Range range;
        while(trieIterator.hasNext() && !(range = trieIterator.next()).leadSurrogate) {
            enumRangeForCopy(range.startCodePoint, range.endCodePoint, range.value, helper);
        }
        // Update the contextChars and the unsafeBackwardSet while copying,
        // in case a character had conditional mappings in the source builder
        // and they were removed later.
        modified |= src.modified;
    }

    void optimize(UnicodeSet set) {
        if(set.isEmpty()) { return; }
        UnicodeSetIterator iter = new UnicodeSetIterator(set);
        while(iter.next() && iter.codepoint != UnicodeSetIterator.IS_STRING) {
            int c = iter.codepoint;
            int ce32 = trie.get(c);
            if(ce32 == Collation.FALLBACK_CE32) {
                ce32 = base.getFinalCE32(base.getCE32(c));
                ce32 = copyFromBaseCE32(c, ce32, true);
                trie.set(c, ce32);
            }
        }
        modified = true;
    }

    void suppressContractions(UnicodeSet set) {
        if(set.isEmpty()) { return; }
        UnicodeSetIterator iter = new UnicodeSetIterator(set);
        while(iter.next() && iter.codepoint != UnicodeSetIterator.IS_STRING) {
            int c = iter.codepoint;
            int ce32 = trie.get(c);
            if(ce32 == Collation.FALLBACK_CE32) {
                ce32 = base.getFinalCE32(base.getCE32(c));
                if(Collation.ce32HasContext(ce32)) {
                    ce32 = copyFromBaseCE32(c, ce32, false /* without context */);
                    trie.set(c, ce32);
                }
            } else if(isBuilderContextCE32(ce32)) {
                ce32 = getConditionalCE32ForCE32(ce32).ce32;
                // Simply abandon the list of ConditionalCE32.
                // The caller will copy this builder in the end,
                // eliminating unreachable data.
                trie.set(c, ce32);
                contextChars.remove(c);
            }
        }
        modified = true;
    }

    void enableFastLatin() { fastLatinEnabled = true; }
    void build(CollationData data) {
        buildMappings(data);
        if(base != null) {
            data.numericPrimary = base.numericPrimary;
            data.compressibleBytes = base.compressibleBytes;
            data.numScripts = base.numScripts;
            data.scriptsIndex = base.scriptsIndex;
            data.scriptStarts = base.scriptStarts;
        }
        buildFastLatinTable(data);
    }

    /**
     * Looks up CEs for s and appends them to the ces array.
     * Does not handle normalization: s should be in FCD form.
     *
     * Does not write completely ignorable CEs.
     * Does not write beyond Collation.MAX_EXPANSION_LENGTH.
     *
     * @return incremented cesLength
     */
    int getCEs(CharSequence s, long ces[], int cesLength) {
        return getCEs(s, 0, ces, cesLength);
    }

    int getCEs(CharSequence prefix, CharSequence s, long ces[], int cesLength) {
        int prefixLength = prefix.length();
        if(prefixLength == 0) {
            return getCEs(s, 0, ces, cesLength);
        } else {
            return getCEs(new StringBuilder(prefix).append(s), prefixLength, ces, cesLength);
        }
    }

    /**
     * Build-time context and CE32 for a code point.
     * If a code point has contextual mappings, then the default (no-context) mapping
     * and all conditional mappings are stored in a singly-linked list
     * of ConditionalCE32, sorted by context strings.
     *
     * Context strings sort by prefix length, then by prefix, then by contraction suffix.
     * Context strings must be unique and in ascending order.
     */
    private static final class ConditionalCE32 {
        ConditionalCE32(String ct, int ce) {
            context = ct;
            ce32 = ce;
            defaultCE32 = Collation.NO_CE32;
            builtCE32 = Collation.NO_CE32;
            next = -1;
        }

        boolean hasContext() { return context.length() > 1; }
        int prefixLength() { return context.charAt(0); }

        /**
         * "\0" for the first entry for any code point, with its default CE32.
         *
         * Otherwise one unit with the length of the prefix string,
         * then the prefix string, then the contraction suffix.
         */
        String context;
        /**
         * CE32 for the code point and its context.
         * Can be special (e.g., for an expansion) but not contextual (prefix or contraction tag).
         */
        int ce32;
        /**
         * Default CE32 for all contexts with this same prefix.
         * Initially NO_CE32. Set only while building runtime data structures,
         * and only on one of the nodes of a sub-list with the same prefix.
         */
        int defaultCE32;
        /**
         * CE32 for the built contexts.
         * When fetching CEs from the builder, the contexts are built into their runtime form
         * so that the normal collation implementation can process them.
         * The result is cached in the list head. It is reset when the contexts are modified.
         */
        int builtCE32;
        /**
         * Index of the next ConditionalCE32.
         * Negative for the end of the list.
         */
        int next;
    }

    protected int getCE32FromOffsetCE32(boolean fromBase, int c, int ce32) {
        int i = Collation.indexFromCE32(ce32);
        long dataCE = fromBase ? base.ces[i] : ce64s.elementAti(i);
        long p = Collation.getThreeBytePrimaryForOffsetData(c, dataCE);
        return Collation.makeLongPrimaryCE32(p);
    }

    protected int addCE(long ce) {
        int length = ce64s.size();
        for(int i = 0; i < length; ++i) {
            if(ce == ce64s.elementAti(i)) { return i; }
        }
        ce64s.addElement(ce);
        return length;
    }

    protected int addCE32(int ce32) {
        int length = ce32s.size();
        for(int i = 0; i < length; ++i) {
            if(ce32 == ce32s.elementAti(i)) { return i; }
        }
        ce32s.addElement(ce32);  
        return length;
    }

    protected int addConditionalCE32(String context, int ce32) {
        assert(context.length() != 0);
        int index = conditionalCE32s.size();
        if(index > Collation.MAX_INDEX) {
            throw new IndexOutOfBoundsException("too many context-sensitive mappings");
            // BufferOverflowException is a better fit
            // but cannot be constructed with a message string.
        }
        ConditionalCE32 cond = new ConditionalCE32(context, ce32);
        conditionalCE32s.add(cond);
        return index;
    }

    protected ConditionalCE32 getConditionalCE32(int index) {
        return conditionalCE32s.get(index);
    }
    protected ConditionalCE32 getConditionalCE32ForCE32(int ce32) {
        return getConditionalCE32(Collation.indexFromCE32(ce32));
    }

    protected static int makeBuilderContextCE32(int index) {
        return Collation.makeCE32FromTagAndIndex(Collation.BUILDER_DATA_TAG, index);
    }
    protected static boolean isBuilderContextCE32(int ce32) {
        return Collation.hasCE32Tag(ce32, Collation.BUILDER_DATA_TAG);
    }

    protected static int encodeOneCEAsCE32(long ce) {
        long p = ce >>> 32;
        int lower32 = (int)ce;
        int t = lower32 & 0xffff;
        assert((t & 0xc000) != 0xc000);  // Impossible case bits 11 mark special CE32s.
        if((ce & 0xffff00ff00ffL) == 0) {
            // normal form ppppsstt
            return (int)p | (lower32 >>> 16) | (t >> 8);
        } else if((ce & 0xffffffffffL) == Collation.COMMON_SEC_AND_TER_CE) {
            // long-primary form ppppppC1
            return Collation.makeLongPrimaryCE32(p);
        } else if(p == 0 && (t & 0xff) == 0) {
            // long-secondary form ssssttC2
            return Collation.makeLongSecondaryCE32(lower32);
        }
        return Collation.NO_CE32;
    }

    protected int encodeOneCE(long ce) {
        // Try to encode one CE as one CE32.
        int ce32 = encodeOneCEAsCE32(ce);
        if(ce32 != Collation.NO_CE32) { return ce32; }
        int index = addCE(ce);
        if(index > Collation.MAX_INDEX) {
            throw new IndexOutOfBoundsException("too many mappings");
            // BufferOverflowException is a better fit
            // but cannot be constructed with a message string.
        }
        return Collation.makeCE32FromTagIndexAndLength(Collation.EXPANSION_TAG, index, 1);
    }

    protected int encodeExpansion(long ces[], int start, int length) {
        // See if this sequence of CEs has already been stored.
        long first = ces[start];
        int ce64sMax = ce64s.size() - length;
        for(int i = 0; i <= ce64sMax; ++i) {
            if(first == ce64s.elementAti(i)) {
                if(i > Collation.MAX_INDEX) {
                    throw new IndexOutOfBoundsException("too many mappings");
                    // BufferOverflowException is a better fit
                    // but cannot be constructed with a message string.
                }
                for(int j = 1;; ++j) {
                    if(j == length) {
                        return Collation.makeCE32FromTagIndexAndLength(
                                Collation.EXPANSION_TAG, i, length);
                    }
                    if(ce64s.elementAti(i + j) != ces[start + j]) { break; }
                }
            }
        }
        // Store the new sequence.
        int i = ce64s.size();
        if(i > Collation.MAX_INDEX) {
            throw new IndexOutOfBoundsException("too many mappings");
            // BufferOverflowException is a better fit
            // but cannot be constructed with a message string.
        }
        for(int j = 0; j < length; ++j) {
            ce64s.addElement(ces[start + j]);
        }
        return Collation.makeCE32FromTagIndexAndLength(Collation.EXPANSION_TAG, i, length);
    }

    protected int encodeExpansion32(int newCE32s[], int start, int length) {
        // See if this sequence of CE32s has already been stored.
        int first = newCE32s[start];
        int ce32sMax = ce32s.size() - length;
        for(int i = 0; i <= ce32sMax; ++i) {
            if(first == ce32s.elementAti(i)) {
                if(i > Collation.MAX_INDEX) {
                    throw new IndexOutOfBoundsException("too many mappings");
                    // BufferOverflowException is a better fit
                    // but cannot be constructed with a message string.
                }
                for(int j = 1;; ++j) {
                    if(j == length) {
                        return Collation.makeCE32FromTagIndexAndLength(
                                Collation.EXPANSION32_TAG, i, length);
                    }
                    if(ce32s.elementAti(i + j) != newCE32s[start + j]) { break; }
                }
            }
        }
        // Store the new sequence.
        int i = ce32s.size();
        if(i > Collation.MAX_INDEX) {
            throw new IndexOutOfBoundsException("too many mappings");
            // BufferOverflowException is a better fit
            // but cannot be constructed with a message string.
        }
        for(int j = 0; j < length; ++j) {
            ce32s.addElement(newCE32s[start + j]);
        }
        return Collation.makeCE32FromTagIndexAndLength(Collation.EXPANSION32_TAG, i, length);
    }

    protected int copyFromBaseCE32(int c, int ce32, boolean withContext) {
        if(!Collation.isSpecialCE32(ce32)) { return ce32; }
        switch(Collation.tagFromCE32(ce32)) {
        case Collation.LONG_PRIMARY_TAG:
        case Collation.LONG_SECONDARY_TAG:
        case Collation.LATIN_EXPANSION_TAG:
            // copy as is
            break;
        case Collation.EXPANSION32_TAG: {
            int index = Collation.indexFromCE32(ce32);
            int length = Collation.lengthFromCE32(ce32);
            ce32 = encodeExpansion32(base.ce32s, index, length);
            break;
        }
        case Collation.EXPANSION_TAG: {
            int index = Collation.indexFromCE32(ce32);
            int length = Collation.lengthFromCE32(ce32);
            ce32 = encodeExpansion(base.ces, index, length);
            break;
        }
        case Collation.PREFIX_TAG: {
            // Flatten prefixes and nested suffixes (contractions)
            // into a linear list of ConditionalCE32.
            int trieIndex = Collation.indexFromCE32(ce32);
            ce32 = base.getCE32FromContexts(trieIndex);  // Default if no prefix match.
            if(!withContext) {
                return copyFromBaseCE32(c, ce32, false);
            }
            ConditionalCE32 head = new ConditionalCE32("", 0);
            StringBuilder context = new StringBuilder("\0");
            int index;
            if(Collation.isContractionCE32(ce32)) {
                index = copyContractionsFromBaseCE32(context, c, ce32, head);
            } else {
                ce32 = copyFromBaseCE32(c, ce32, true);
                head.next = index = addConditionalCE32(context.toString(), ce32);
            }
            ConditionalCE32 cond = getConditionalCE32(index);  // the last ConditionalCE32 so far
            CharsTrie.Iterator prefixes = CharsTrie.iterator(base.contexts, trieIndex + 2, 0);
            while(prefixes.hasNext()) {
                CharsTrie.Entry entry = prefixes.next();
                context.setLength(0);
                context.append(entry.chars).reverse().insert(0, (char)entry.chars.length());
                ce32 = entry.value;
                if(Collation.isContractionCE32(ce32)) {
                    index = copyContractionsFromBaseCE32(context, c, ce32, cond);
                } else {
                    ce32 = copyFromBaseCE32(c, ce32, true);
                    cond.next = index = addConditionalCE32(context.toString(), ce32);
                }
                cond = getConditionalCE32(index);
            }
            ce32 = makeBuilderContextCE32(head.next);
            contextChars.add(c);
            break;
        }
        case Collation.CONTRACTION_TAG: {
            if(!withContext) {
                int index = Collation.indexFromCE32(ce32);
                ce32 = base.getCE32FromContexts(index);  // Default if no suffix match.
                return copyFromBaseCE32(c, ce32, false);
            }
            ConditionalCE32 head = new ConditionalCE32("", 0);
            StringBuilder context = new StringBuilder("\0");
            copyContractionsFromBaseCE32(context, c, ce32, head);
            ce32 = makeBuilderContextCE32(head.next);
            contextChars.add(c);
            break;
        }
        case Collation.HANGUL_TAG:
            throw new UnsupportedOperationException("We forbid tailoring of Hangul syllables.");
        case Collation.OFFSET_TAG:
            ce32 = getCE32FromOffsetCE32(true, c, ce32);
            break;
        case Collation.IMPLICIT_TAG:
            ce32 = encodeOneCE(Collation.unassignedCEFromCodePoint(c));
            break;
        default:
            throw new AssertionError("copyFromBaseCE32(c, ce32, withContext) " +
                    "requires ce32 == base.getFinalCE32(ce32)");
        }
        return ce32;
    }

    /**
     * Copies base contractions to a list of ConditionalCE32.
     * Sets cond.next to the index of the first new item
     * and returns the index of the last new item.
     */
    protected int copyContractionsFromBaseCE32(StringBuilder context, int c, int ce32,
            ConditionalCE32 cond) {
        int trieIndex = Collation.indexFromCE32(ce32);
        int index;
        if((ce32 & Collation.CONTRACT_SINGLE_CP_NO_MATCH) != 0) {
            // No match on the single code point.
            // We are underneath a prefix, and the default mapping is just
            // a fallback to the mappings for a shorter prefix.
            assert(context.length() > 1);
            index = -1;
        } else {
            ce32 = base.getCE32FromContexts(trieIndex);  // Default if no suffix match.
            assert(!Collation.isContractionCE32(ce32));
            ce32 = copyFromBaseCE32(c, ce32, true);
            cond.next = index = addConditionalCE32(context.toString(), ce32);
            cond = getConditionalCE32(index);
        }

        int suffixStart = context.length();
        CharsTrie.Iterator suffixes = CharsTrie.iterator(base.contexts, trieIndex + 2, 0);
        while(suffixes.hasNext()) {
            CharsTrie.Entry entry = suffixes.next();
            context.append(entry.chars);
            ce32 = copyFromBaseCE32(c, entry.value, true);
            cond.next = index = addConditionalCE32(context.toString(), ce32);
            // No need to update the unsafeBackwardSet because the tailoring set
            // is already a copy of the base set.
            cond = getConditionalCE32(index);
            context.setLength(suffixStart);
        }
        assert(index >= 0);
        return index;
    }

    private static final class CopyHelper {
        CopyHelper(CollationDataBuilder s, CollationDataBuilder d,
                  CollationDataBuilder.CEModifier m) {
            src = s;
            dest = d;
            modifier = m;
        }

        void copyRangeCE32(int start, int end, int ce32) {
            ce32 = copyCE32(ce32);
            dest.trie.setRange(start, end, ce32, true);
            if(CollationDataBuilder.isBuilderContextCE32(ce32)) {
                dest.contextChars.add(start, end);
            }
        }

        int copyCE32(int ce32) {
            if(!Collation.isSpecialCE32(ce32)) {
                long ce = modifier.modifyCE32(ce32);
                if(ce != Collation.NO_CE) {
                    ce32 = dest.encodeOneCE(ce);
                }
            } else {
                int tag = Collation.tagFromCE32(ce32);
                if(tag == Collation.EXPANSION32_TAG) {
                    int[] srcCE32s = src.ce32s.getBuffer();
                    int srcIndex = Collation.indexFromCE32(ce32);
                    int length = Collation.lengthFromCE32(ce32);
                    // Inspect the source CE32s. Just copy them if none are modified.
                    // Otherwise copy to modifiedCEs, with modifications.
                    boolean isModified = false;
                    for(int i = 0; i < length; ++i) {
                        ce32 = srcCE32s[srcIndex + i];
                        long ce;
                        if(Collation.isSpecialCE32(ce32) ||
                                (ce = modifier.modifyCE32(ce32)) == Collation.NO_CE) {
                            if(isModified) {
                                modifiedCEs[i] = Collation.ceFromCE32(ce32);
                            }
                        } else {
                            if(!isModified) {
                                for(int j = 0; j < i; ++j) {
                                    modifiedCEs[j] = Collation.ceFromCE32(srcCE32s[srcIndex + j]);
                                }
                                isModified = true;
                            }
                            modifiedCEs[i] = ce;
                        }
                    }
                    if(isModified) {
                        ce32 = dest.encodeCEs(modifiedCEs, length);
                    } else {
                        ce32 = dest.encodeExpansion32(srcCE32s, srcIndex, length);
                    }
                } else if(tag == Collation.EXPANSION_TAG) {
                    long[] srcCEs = src.ce64s.getBuffer();
                    int srcIndex = Collation.indexFromCE32(ce32);
                    int length = Collation.lengthFromCE32(ce32);
                    // Inspect the source CEs. Just copy them if none are modified.
                    // Otherwise copy to modifiedCEs, with modifications.
                    boolean isModified = false;
                    for(int i = 0; i < length; ++i) {
                        long srcCE = srcCEs[srcIndex + i];
                        long ce = modifier.modifyCE(srcCE);
                        if(ce == Collation.NO_CE) {
                            if(isModified) {
                                modifiedCEs[i] = srcCE;
                            }
                        } else {
                            if(!isModified) {
                                for(int j = 0; j < i; ++j) {
                                    modifiedCEs[j] = srcCEs[srcIndex + j];
                                }
                                isModified = true;
                            }
                            modifiedCEs[i] = ce;
                        }
                    }
                    if(isModified) {
                        ce32 = dest.encodeCEs(modifiedCEs, length);
                    } else {
                        ce32 = dest.encodeExpansion(srcCEs, srcIndex, length);
                    }
                } else if(tag == Collation.BUILDER_DATA_TAG) {
                    // Copy the list of ConditionalCE32.
                    ConditionalCE32 cond = src.getConditionalCE32ForCE32(ce32);
                    assert(!cond.hasContext());
                    int destIndex = dest.addConditionalCE32(
                            cond.context, copyCE32(cond.ce32));
                    ce32 = CollationDataBuilder.makeBuilderContextCE32(destIndex);
                    while(cond.next >= 0) {
                        cond = src.getConditionalCE32(cond.next);
                        ConditionalCE32 prevDestCond = dest.getConditionalCE32(destIndex);
                        destIndex = dest.addConditionalCE32(
                                cond.context, copyCE32(cond.ce32));
                        int suffixStart = cond.prefixLength() + 1;
                        dest.unsafeBackwardSet.addAll(cond.context.substring(suffixStart));
                        prevDestCond.next = destIndex;
                    }
                } else {
                    // Just copy long CEs and Latin mini expansions (and other expected values) as is,
                    // assuming that the modifier would not modify them.
                    assert(tag == Collation.LONG_PRIMARY_TAG ||
                            tag == Collation.LONG_SECONDARY_TAG ||
                            tag == Collation.LATIN_EXPANSION_TAG ||
                            tag == Collation.HANGUL_TAG);
                }
            }
            return ce32;
        }

        CollationDataBuilder src;
        CollationDataBuilder dest;
        CollationDataBuilder.CEModifier modifier;
        long[] modifiedCEs = new long[Collation.MAX_EXPANSION_LENGTH];
    }

    private static void
    enumRangeForCopy(int start, int end, int value, CopyHelper helper) {
        if(value != Collation.UNASSIGNED_CE32 && value != Collation.FALLBACK_CE32) {
            helper.copyRangeCE32(start, end, value);
        }
    }

    protected boolean getJamoCE32s(int jamoCE32s[]) {
        boolean anyJamoAssigned = base == null;  // always set jamoCE32s in the base data
        boolean needToCopyFromBase = false;
        for(int j = 0; j < CollationData.JAMO_CE32S_LENGTH; ++j) {  // Count across Jamo types.
            int jamo = jamoCpFromIndex(j);
            boolean fromBase = false;
            int ce32 = trie.get(jamo);
            anyJamoAssigned |= Collation.isAssignedCE32(ce32);
            // TODO: Try to prevent [optimize [Jamo]] from counting as anyJamoAssigned.
            // (As of CLDR 24 [2013] the Korean tailoring does not optimize conjoining Jamo.)
            if(ce32 == Collation.FALLBACK_CE32) {
                fromBase = true;
                ce32 = base.getCE32(jamo);
            }
            if(Collation.isSpecialCE32(ce32)) {
                switch(Collation.tagFromCE32(ce32)) {
                case Collation.LONG_PRIMARY_TAG:
                case Collation.LONG_SECONDARY_TAG:
                case Collation.LATIN_EXPANSION_TAG:
                    // Copy the ce32 as-is.
                    break;
                case Collation.EXPANSION32_TAG:
                case Collation.EXPANSION_TAG:
                case Collation.PREFIX_TAG:
                case Collation.CONTRACTION_TAG:
                    if(fromBase) {
                        // Defer copying until we know if anyJamoAssigned.
                        ce32 = Collation.FALLBACK_CE32;
                        needToCopyFromBase = true;
                    }
                    break;
                case Collation.IMPLICIT_TAG:
                    // An unassigned Jamo should only occur in tests with incomplete bases.
                    assert(fromBase);
                    ce32 = Collation.FALLBACK_CE32;
                    needToCopyFromBase = true;
                    break;
                case Collation.OFFSET_TAG:
                    ce32 = getCE32FromOffsetCE32(fromBase, jamo, ce32);
                    break;
                case Collation.FALLBACK_TAG:
                case Collation.RESERVED_TAG_3:
                case Collation.BUILDER_DATA_TAG:
                case Collation.DIGIT_TAG:
                case Collation.U0000_TAG:
                case Collation.HANGUL_TAG:
                case Collation.LEAD_SURROGATE_TAG:
                    throw new AssertionError(String.format("unexpected special tag in ce32=0x%08x", ce32));
                }
            }
            jamoCE32s[j] = ce32;
        }
        if(anyJamoAssigned && needToCopyFromBase) {
            for(int j = 0; j < CollationData.JAMO_CE32S_LENGTH; ++j) {
                if(jamoCE32s[j] == Collation.FALLBACK_CE32) {
                    int jamo = jamoCpFromIndex(j);
                    jamoCE32s[j] = copyFromBaseCE32(jamo, base.getCE32(jamo),
                                                    /*withContext=*/ true);
                }
            }
        }
        return anyJamoAssigned;
    }

    protected void setDigitTags() {
        UnicodeSet digits = new UnicodeSet("[:Nd:]");
        UnicodeSetIterator iter = new UnicodeSetIterator(digits);
        while(iter.next()) {
            assert(iter.codepoint != UnicodeSetIterator.IS_STRING);
            int c = iter.codepoint;
            int ce32 = trie.get(c);
            if(ce32 != Collation.FALLBACK_CE32 && ce32 != Collation.UNASSIGNED_CE32) {
                int index = addCE32(ce32);
                if(index > Collation.MAX_INDEX) {
                    throw new IndexOutOfBoundsException("too many mappings");
                    // BufferOverflowException is a better fit
                    // but cannot be constructed with a message string.
                }
                ce32 = Collation.makeCE32FromTagIndexAndLength(
                        Collation.DIGIT_TAG, index, UCharacter.digit(c));  // u_charDigitValue(c)
                trie.set(c, ce32);
            }
        }
    }

    protected void setLeadSurrogates() {
        for(char lead = 0xd800; lead < 0xdc00; ++lead) {
            int leadValue = -1;
            // utrie2_enumForLeadSurrogate(trie, lead, null, , &value);
            Iterator<Trie2.Range> trieIterator = trie.iteratorForLeadSurrogate(lead);
            while(trieIterator.hasNext()) {
                Trie2.Range range = trieIterator.next();
                // The rest of this loop is equivalent to C++ enumRangeLeadValue().
                int value = range.value;
                if(value == Collation.UNASSIGNED_CE32) {
                    value = Collation.LEAD_ALL_UNASSIGNED;
                } else if(value == Collation.FALLBACK_CE32) {
                    value = Collation.LEAD_ALL_FALLBACK;
                } else {
                    leadValue = Collation.LEAD_MIXED;
                    break;
                }
                if(leadValue < 0) {
                    leadValue = value;
                } else if(leadValue != value) {
                    leadValue = Collation.LEAD_MIXED;
                    break;
                }
            }
            trie.setForLeadSurrogateCodeUnit(lead,
                    Collation.makeCE32FromTagAndIndex(Collation.LEAD_SURROGATE_TAG, 0) | leadValue);
        }
    }

    protected void buildMappings(CollationData data) {
        if(!isMutable()) {
            throw new IllegalStateException("attempt to build() after build()");
        }

        buildContexts();

        int[] jamoCE32s = new int[CollationData.JAMO_CE32S_LENGTH];
        int jamoIndex = -1;
        if(getJamoCE32s(jamoCE32s)) {
            jamoIndex = ce32s.size();
            for(int i = 0; i < CollationData.JAMO_CE32S_LENGTH; ++i) {
                ce32s.addElement(jamoCE32s[i]);
            }
            // Small optimization: Use a bit in the Hangul ce32
            // to indicate that none of the Jamo CE32s are isSpecialCE32()
            // (as it should be in the root collator).
            // It allows CollationIterator to avoid recursive function calls and per-Jamo tests.
            // In order to still have good trie compression and keep this code simple,
            // we only set this flag if a whole block of 588 Hangul syllables starting with
            // a common leading consonant (Jamo L) has this property.
            boolean isAnyJamoVTSpecial = false;
            for(int i = Hangul.JAMO_L_COUNT; i < CollationData.JAMO_CE32S_LENGTH; ++i) {
                if(Collation.isSpecialCE32(jamoCE32s[i])) {
                    isAnyJamoVTSpecial = true;
                    break;
                }
            }
            int hangulCE32 = Collation.makeCE32FromTagAndIndex(Collation.HANGUL_TAG, 0);
            int c = Hangul.HANGUL_BASE;
            for(int i = 0; i < Hangul.JAMO_L_COUNT; ++i) {  // iterate over the Jamo L
                int ce32 = hangulCE32;
                if(!isAnyJamoVTSpecial && !Collation.isSpecialCE32(jamoCE32s[i])) {
                    ce32 |= Collation.HANGUL_NO_SPECIAL_JAMO;
                }
                int limit = c + Hangul.JAMO_VT_COUNT;
                trie.setRange(c, limit - 1, ce32, true);
                c = limit;
            }
        } else {
            // Copy the Hangul CE32s from the base in blocks per Jamo L,
            // assuming that HANGUL_NO_SPECIAL_JAMO is set or not set for whole blocks.
            for(int c = Hangul.HANGUL_BASE; c < Hangul.HANGUL_LIMIT;) {
                int ce32 = base.getCE32(c);
                assert(Collation.hasCE32Tag(ce32, Collation.HANGUL_TAG));
                int limit = c + Hangul.JAMO_VT_COUNT;
                trie.setRange(c, limit - 1, ce32, true);
                c = limit;
            }
        }

        setDigitTags();
        setLeadSurrogates();

        // For U+0000, move its normal ce32 into CE32s[0] and set U0000_TAG.
        ce32s.setElementAt(trie.get(0), 0);
        trie.set(0, Collation.makeCE32FromTagAndIndex(Collation.U0000_TAG, 0));

        data.trie = trie.toTrie2_32();

        // Mark each lead surrogate as "unsafe"
        // if any of its 1024 associated supplementary code points is "unsafe".
        int c = 0x10000;
        for(char lead = 0xd800; lead < 0xdc00; ++lead, c += 0x400) {
            if(unsafeBackwardSet.containsSome(c, c + 0x3ff)) {
                unsafeBackwardSet.add(lead);
            }
        }
        unsafeBackwardSet.freeze();

        data.ce32s = ce32s.getBuffer();
        data.ces = ce64s.getBuffer();
        data.contexts = contexts.toString();

        data.base = base;
        if(jamoIndex >= 0) {
            data.jamoCE32s = jamoCE32s;  // C++: data.ce32s + jamoIndex
        } else {
            data.jamoCE32s = base.jamoCE32s;
        }
        data.unsafeBackwardSet = unsafeBackwardSet;
    }

    protected void clearContexts() {
        contexts.setLength(0);
        UnicodeSetIterator iter = new UnicodeSetIterator(contextChars);
        while(iter.next()) {
            assert(iter.codepoint != UnicodeSetIterator.IS_STRING);
            int ce32 = trie.get(iter.codepoint);
            assert(isBuilderContextCE32(ce32));
            getConditionalCE32ForCE32(ce32).builtCE32 = Collation.NO_CE32;
        }
    }

    protected void buildContexts() {
        // Ignore abandoned lists and the cached builtCE32,
        // and build all contexts from scratch.
        contexts.setLength(0);
        UnicodeSetIterator iter = new UnicodeSetIterator(contextChars);
        while(iter.next()) {
            assert(iter.codepoint != UnicodeSetIterator.IS_STRING);
            int c = iter.codepoint;
            int ce32 = trie.get(c);
            if(!isBuilderContextCE32(ce32)) {
                throw new AssertionError("Impossible: No context data for c in contextChars.");
            }
            ConditionalCE32 cond = getConditionalCE32ForCE32(ce32);
            ce32 = buildContext(cond);
            trie.set(c, ce32);
        }
    }

    protected int buildContext(ConditionalCE32 head) {
        // The list head must have no context.
        assert(!head.hasContext());
        // The list head must be followed by one or more nodes that all do have context.
        assert(head.next >= 0);
        CharsTrieBuilder prefixBuilder = new CharsTrieBuilder();
        CharsTrieBuilder contractionBuilder = new CharsTrieBuilder();
        for(ConditionalCE32 cond = head;; cond = getConditionalCE32(cond.next)) {
            // After the list head, the prefix or suffix can be empty, but not both.
            assert(cond == head || cond.hasContext());
            int prefixLength = cond.prefixLength();
            StringBuilder prefix = new StringBuilder().append(cond.context, 0, prefixLength + 1);
            String prefixString = prefix.toString();
            // Collect all contraction suffixes for one prefix.
            ConditionalCE32 firstCond = cond;
            ConditionalCE32 lastCond = cond;
            while(cond.next >= 0 &&
                    (cond = getConditionalCE32(cond.next)).context.startsWith(prefixString)) {
                lastCond = cond;
            }
            int ce32;
            int suffixStart = prefixLength + 1;  // == prefix.length()
            if(lastCond.context.length() == suffixStart) {
                // One prefix without contraction suffix.
                assert(firstCond == lastCond);
                ce32 = lastCond.ce32;
                cond = lastCond;
            } else {
                // Build the contractions trie.
                contractionBuilder.clear();
                // Entry for an empty suffix, to be stored before the trie.
                int emptySuffixCE32 = Collation.NO_CE32;  // Will always be set to a real value.
                int flags = 0;
                if(firstCond.context.length() == suffixStart) {
                    // There is a mapping for the prefix and the single character c. (p|c)
                    // If no other suffix matches, then we return this value.
                    emptySuffixCE32 = firstCond.ce32;
                    cond = getConditionalCE32(firstCond.next);
                } else {
                    // There is no mapping for the prefix and just the single character.
                    // (There is no p|c, only p|cd, p|ce etc.)
                    flags |= Collation.CONTRACT_SINGLE_CP_NO_MATCH;
                    // When the prefix matches but none of the prefix-specific suffixes,
                    // then we fall back to the mappings with the next-longest prefix,
                    // and ultimately to mappings with no prefix.
                    // Each fallback might be another set of contractions.
                    // For example, if there are mappings for ch, p|cd, p|ce, but not for p|c,
                    // then in text "pch" we find the ch contraction.
                    for(cond = head;; cond = getConditionalCE32(cond.next)) {
                        int length = cond.prefixLength();
                        if(length == prefixLength) { break; }
                        if(cond.defaultCE32 != Collation.NO_CE32 &&
                                (length==0 || prefixString.regionMatches(
                                        prefix.length() - length, cond.context, 1, length)
                                        /* C++: prefix.endsWith(cond.context, 1, length) */)) {
                            emptySuffixCE32 = cond.defaultCE32;
                        }
                    }
                    cond = firstCond;
                }
                // Optimization: Set a flag when
                // the first character of every contraction suffix has lccc!=0.
                // Short-circuits contraction matching when a normal letter follows.
                flags |= Collation.CONTRACT_NEXT_CCC;
                // Add all of the non-empty suffixes into the contraction trie.
                for(;;) {
                    String suffix = cond.context.substring(suffixStart);
                    int fcd16 = nfcImpl.getFCD16(suffix.codePointAt(0));
                    if(fcd16 <= 0xff) {
                        flags &= ~Collation.CONTRACT_NEXT_CCC;
                    }
                    fcd16 = nfcImpl.getFCD16(suffix.codePointBefore(suffix.length()));
                    if(fcd16 > 0xff) {
                        // The last suffix character has lccc!=0, allowing for discontiguous contractions.
                        flags |= Collation.CONTRACT_TRAILING_CCC;
                    }
                    contractionBuilder.add(suffix, cond.ce32);
                    if(cond == lastCond) { break; }
                    cond = getConditionalCE32(cond.next);
                }
                int index = addContextTrie(emptySuffixCE32, contractionBuilder);
                if(index > Collation.MAX_INDEX) {
                    throw new IndexOutOfBoundsException("too many context-sensitive mappings");
                    // BufferOverflowException is a better fit
                    // but cannot be constructed with a message string.
                }
                ce32 = Collation.makeCE32FromTagAndIndex(Collation.CONTRACTION_TAG, index) | flags;
            }
            assert(cond == lastCond);
            firstCond.defaultCE32 = ce32;
            if(prefixLength == 0) {
                if(cond.next < 0) {
                    // No non-empty prefixes, only contractions.
                    return ce32;
                }
            } else {
                prefix.delete(0, 1);  // Remove the length unit.
                prefix.reverse();
                prefixBuilder.add(prefix, ce32);
                if(cond.next < 0) { break; }
            }
        }
        assert(head.defaultCE32 != Collation.NO_CE32);
        int index = addContextTrie(head.defaultCE32, prefixBuilder);
        if(index > Collation.MAX_INDEX) {
            throw new IndexOutOfBoundsException("too many context-sensitive mappings");
            // BufferOverflowException is a better fit
            // but cannot be constructed with a message string.
        }
        return Collation.makeCE32FromTagAndIndex(Collation.PREFIX_TAG, index);
    }

    protected int addContextTrie(int defaultCE32, CharsTrieBuilder trieBuilder) {
        StringBuilder context = new StringBuilder();
        context.append((char)(defaultCE32 >> 16)).append((char)defaultCE32);
        context.append(trieBuilder.buildCharSequence(StringTrieBuilder.Option.SMALL));
        int index = contexts.indexOf(context.toString());
        if(index < 0) {
            index = contexts.length();
            contexts.append(context);
        }
        return index;
    }

    protected void buildFastLatinTable(CollationData data) {
        if(!fastLatinEnabled) { return; }

        fastLatinBuilder = new CollationFastLatinBuilder();
        if(fastLatinBuilder.forData(data)) {
            char[] header = fastLatinBuilder.getHeader();
            char[] table = fastLatinBuilder.getTable();
            if(base != null &&
                    Arrays.equals(header, base.fastLatinTableHeader) &&
                    Arrays.equals(table, base.fastLatinTable)) {
                // Same fast Latin table as in the base, use that one instead.
                fastLatinBuilder = null;
                header = base.fastLatinTableHeader;
                table = base.fastLatinTable;
            }
            data.fastLatinTableHeader = header;
            data.fastLatinTable = table;
        } else {
            fastLatinBuilder = null;
        }
    }

    protected int getCEs(CharSequence s, int start, long ces[], int cesLength) {
        if(collIter == null) {
            collIter = new DataBuilderCollationIterator(this, new CollationData(nfcImpl));
            if(collIter == null) { return 0; }
        }
        return collIter.fetchCEs(s, start, ces, cesLength);
    }

    protected static int jamoCpFromIndex(int i) {
        // 0 <= i < CollationData.JAMO_CE32S_LENGTH = 19 + 21 + 27
        if(i < Hangul.JAMO_L_COUNT) { return Hangul.JAMO_L_BASE + i; }
        i -= Hangul.JAMO_L_COUNT;
        if(i < Hangul.JAMO_V_COUNT) { return Hangul.JAMO_V_BASE + i; }
        i -= Hangul.JAMO_V_COUNT;
        // i < 27
        return Hangul.JAMO_T_BASE + 1 + i;
    }

    /**
     * Build-time collation element and character iterator.
     * Uses the runtime CollationIterator for fetching CEs for a string
     * but reads from the builder's unfinished data structures.
     * In particular, this class reads from the unfinished trie
     * and has to avoid CollationIterator.nextCE() and redirect other
     * calls to data.getCE32() and data.getCE32FromSupplementary().
     *
     * We do this so that we need not implement the collation algorithm
     * again for the builder and make it behave exactly like the runtime code.
     * That would be more difficult to test and maintain than this indirection.
     *
     * Some CE32 tags (for example, the DIGIT_TAG) do not occur in the builder data,
     * so the data accesses from those code paths need not be modified.
     *
     * This class iterates directly over whole code points
     * so that the CollationIterator does not need the finished trie
     * for handling the LEAD_SURROGATE_TAG.
     */
    private static final class DataBuilderCollationIterator extends CollationIterator {
        DataBuilderCollationIterator(CollationDataBuilder b, CollationData newData) {
            super(newData, /*numeric=*/ false);
            builder = b;
            builderData = newData;
            builderData.base = builder.base;
            // Set all of the jamoCE32s[] to indirection CE32s.
            for(int j = 0; j < CollationData.JAMO_CE32S_LENGTH; ++j) {  // Count across Jamo types.
                int jamo = CollationDataBuilder.jamoCpFromIndex(j);
                jamoCE32s[j] = Collation.makeCE32FromTagAndIndex(Collation.BUILDER_DATA_TAG, jamo) |
                        CollationDataBuilder.IS_BUILDER_JAMO_CE32;
            }
            builderData.jamoCE32s = jamoCE32s;
        }

        int fetchCEs(CharSequence str, int start, long ces[], int cesLength) {
            // Set the pointers each time, in case they changed due to reallocation.
            builderData.ce32s = builder.ce32s.getBuffer();
            builderData.ces = builder.ce64s.getBuffer();
            builderData.contexts = builder.contexts.toString();
            // Modified copy of CollationIterator.nextCE() and CollationIterator.nextCEFromCE32().
            reset();
            s = str;
            pos = start;
            while(pos < s.length()) {
                // No need to keep all CEs in the iterator buffer.
                clearCEs();
                int c = Character.codePointAt(s, pos);
                pos += Character.charCount(c);
                int ce32 = builder.trie.get(c);
                CollationData d;
                if(ce32 == Collation.FALLBACK_CE32) {
                    d = builder.base;
                    ce32 = builder.base.getCE32(c);
                } else {
                    d = builderData;
                }
                appendCEsFromCE32(d, c, ce32, /*forward=*/ true);
                for(int i = 0; i < getCEsLength(); ++i) {
                    long ce = getCE(i);
                    if(ce != 0) {
                        if(cesLength < Collation.MAX_EXPANSION_LENGTH) {
                            ces[cesLength] = ce;
                        }
                        ++cesLength;
                    }
                }
            }
            return cesLength;
        }

        @Override
        public void resetToOffset(int newOffset) {
            reset();
            pos = newOffset;
        }

        @Override
        public int getOffset() {
            return pos;
        }

        @Override
        public int nextCodePoint() {
            if(pos == s.length()) {
                return Collation.SENTINEL_CP;
            }
            int c = Character.codePointAt(s, pos);
            pos += Character.charCount(c);
            return c;
        }

        @Override
        public int previousCodePoint() {
            if(pos == 0) {
                return Collation.SENTINEL_CP;
            }
            int c = Character.codePointBefore(s, pos);
            pos -= Character.charCount(c);
            return c;
        }

        @Override
        protected void forwardNumCodePoints(int num) {
            pos = Character.offsetByCodePoints(s, pos, num);
        }

        @Override
        protected void backwardNumCodePoints(int num) {
            pos = Character.offsetByCodePoints(s, pos, -num);
        }

        @Override
        protected int getDataCE32(int c) {
            return builder.trie.get(c);
        }

        @Override
        protected int getCE32FromBuilderData(int ce32) {
            assert(Collation.hasCE32Tag(ce32, Collation.BUILDER_DATA_TAG));
            if((ce32 & CollationDataBuilder.IS_BUILDER_JAMO_CE32) != 0) {
                int jamo = Collation.indexFromCE32(ce32);
                return builder.trie.get(jamo);
            } else {
                ConditionalCE32 cond = builder.getConditionalCE32ForCE32(ce32);
                if(cond.builtCE32 == Collation.NO_CE32) {
                    // Build the context-sensitive mappings into their runtime form and cache the result.
                    try {
                        cond.builtCE32 = builder.buildContext(cond);
                    } catch(IndexOutOfBoundsException e) {
                        builder.clearContexts();
                        cond.builtCE32 = builder.buildContext(cond);
                    }
                    builderData.contexts = builder.contexts.toString();
                }
                return cond.builtCE32;
            }
        }

        protected final CollationDataBuilder builder;
        protected final CollationData builderData;
        protected final int[] jamoCE32s = new int[CollationData.JAMO_CE32S_LENGTH];
        protected CharSequence s;
        protected int pos;
    }

    protected final boolean isMutable() {
        // C++ tests !(trie == NULL || utrie2_isFrozen(trie))
        // but Java Trie2Writable does not have an observable isFrozen() state.
        return trie != null && unsafeBackwardSet != null && !unsafeBackwardSet.isFrozen();
    }

    /** @see Collation.BUILDER_DATA_TAG */
    private static final int IS_BUILDER_JAMO_CE32 = 0x100;

    protected Normalizer2Impl nfcImpl;
    protected CollationData base;
    protected CollationSettings baseSettings;
    protected Trie2Writable trie;
    protected UVector32 ce32s;
    protected UVector64 ce64s;
    protected ArrayList<ConditionalCE32> conditionalCE32s;  // vector of ConditionalCE32
    // Characters that have context (prefixes or contraction suffixes).
    protected UnicodeSet contextChars = new UnicodeSet();
    // Serialized UCharsTrie structures for finalized contexts.
    protected StringBuilder contexts = new StringBuilder();
    protected UnicodeSet unsafeBackwardSet = new UnicodeSet();
    protected boolean modified;

    protected boolean fastLatinEnabled;
    protected CollationFastLatinBuilder fastLatinBuilder;

    protected DataBuilderCollationIterator collIter;
}
