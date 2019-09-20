/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 **********************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 **********************************************************************
 *   Date        Name        Description
 *   06/08/01    aliu        Creation.
 **********************************************************************
 */

package android.icu.text;
import java.util.HashMap;
import java.util.Map;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;

/**
 * @author Alan Liu, Markus Scherer
 */
final class NormalizationTransliterator extends Transliterator {
    private final Normalizer2 norm2;

    /**
     * System registration hook.
     */
    static void register() {
        Transliterator.registerFactory("Any-NFC", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFC", Normalizer2.getNFCInstance());
            }
        });
        Transliterator.registerFactory("Any-NFD", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFD", Normalizer2.getNFDInstance());
            }
        });
        Transliterator.registerFactory("Any-NFKC", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFKC", Normalizer2.getNFKCInstance());
            }
        });
        Transliterator.registerFactory("Any-NFKD", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("NFKD", Normalizer2.getNFKDInstance());
            }
        });
        Transliterator.registerFactory("Any-FCD", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("FCD", Norm2AllModes.getFCDNormalizer2());
            }
        });
        Transliterator.registerFactory("Any-FCC", new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new NormalizationTransliterator("FCC", Norm2AllModes.getNFCInstance().fcc);
            }
        });
        Transliterator.registerSpecialInverse("NFC", "NFD", true);
        Transliterator.registerSpecialInverse("NFKC", "NFKD", true);
        Transliterator.registerSpecialInverse("FCC", "NFD", false);
        Transliterator.registerSpecialInverse("FCD", "FCD", false);
    }

    /**
     * Constructs a transliterator.
     */
    private NormalizationTransliterator(String id, Normalizer2 n2) {
        super(id, null);
        norm2 = n2;
    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    @Override
    protected void handleTransliterate(Replaceable text,
            Position offsets, boolean isIncremental) {
        // start and limit of the input range
        int start = offsets.start;
        int limit = offsets.limit;
        if(start >= limit) {
            return;
        }

        /*
         * Normalize as short chunks at a time as possible even in
         * bulk mode, so that styled text is minimally disrupted.
         * In incremental mode, a chunk that ends with offsets.limit
         * must not be normalized.
         *
         * If it was known that the input text is not styled, then
         * a bulk mode normalization could be used.
         * (For details, see the comment in the C++ version.)
         */
        StringBuilder segment = new StringBuilder();
        StringBuilder normalized = new StringBuilder();
        int c = text.char32At(start);
        do {
            int prev = start;
            // Skip at least one character so we make progress.
            // c holds the character at start.
            segment.setLength(0);
            do {
                segment.appendCodePoint(c);
                start += Character.charCount(c);
            } while(start < limit && !norm2.hasBoundaryBefore(c = text.char32At(start)));
            if(start == limit && isIncremental && !norm2.hasBoundaryAfter(c)) {
                // stop in incremental mode when we reach the input limit
                // in case there are additional characters that could change the
                // normalization result
                start=prev;
                break;
            }
            norm2.normalize(segment, normalized);
            if(!Normalizer2Impl.UTF16Plus.equal(segment, normalized)) {
                // replace the input chunk with its normalized form
                text.replace(prev, start, normalized.toString());

                // update all necessary indexes accordingly
                int delta = normalized.length() - (start - prev);
                start += delta;
                limit += delta;
            }
        } while(start < limit);

        offsets.start = start;
        offsets.contextLimit += limit - offsets.limit;
        offsets.limit = limit;
    }

    static final Map<Normalizer2, SourceTargetUtility> SOURCE_CACHE = new HashMap<Normalizer2, SourceTargetUtility>();

    // TODO Get rid of this if Normalizer2 becomes a Transform
    static class NormalizingTransform implements Transform<String,String> {
        final Normalizer2 norm2;
        public NormalizingTransform(Normalizer2 norm2) {
            this.norm2 = norm2;
        }
        @Override
        public String transform(String source) {
            return norm2.normalize(source);
        }
    }

    /* (non-Javadoc)
     * @see android.icu.text.Transliterator#addSourceTargetSet(android.icu.text.UnicodeSet, android.icu.text.UnicodeSet, android.icu.text.UnicodeSet)
     */
    @Override
    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        SourceTargetUtility cache;
        synchronized (SOURCE_CACHE) {
            //String id = getID();
            cache = SOURCE_CACHE.get(norm2);
            if (cache == null) {
                cache = new SourceTargetUtility(new NormalizingTransform(norm2), norm2);
                SOURCE_CACHE.put(norm2, cache);
            }
        }
        cache.addSourceTargetSet(this, inputFilter, sourceSet, targetSet);
    }
}
