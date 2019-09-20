/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2011, Google, International Business Machines Corporation
 * and others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import android.icu.impl.UCaseProps;
import android.icu.lang.UCharacter;

/**
 * A transliterator that performs locale-sensitive toLower()
 * case mapping.
 */
class CaseFoldTransliterator extends Transliterator{

    /**
     * Package accessible ID.
     */
    static final String _ID = "Any-CaseFold";

    // TODO: Add variants for tr, az, lt, default = default locale

    /**
     * System registration hook.
     */
    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new CaseFoldTransliterator();
            }
        });

        Transliterator.registerSpecialInverse("CaseFold", "Upper", false);
    }

    private final UCaseProps csp;
    private ReplaceableContextIterator iter;
    private StringBuilder result;

    /**
     * Constructs a transliterator.
     */

    public CaseFoldTransliterator() {
        super(_ID, null);
        csp=UCaseProps.INSTANCE;
        iter=new ReplaceableContextIterator();
        result = new StringBuilder();
    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    @Override
    protected synchronized void handleTransliterate(Replaceable text,
                                       Position offsets, boolean isIncremental) {
        if(csp==null) {
            return;
        }

        if(offsets.start >= offsets.limit) {
            return;
        }

        iter.setText(text);
        result.setLength(0);
        int c, delta;

        // Walk through original string
        // If there is a case change, modify corresponding position in replaceable

        iter.setIndex(offsets.start);
        iter.setLimit(offsets.limit);
        iter.setContextLimits(offsets.contextStart, offsets.contextLimit);
        while((c=iter.nextCaseMapCP())>=0) {
            c=csp.toFullFolding(c, result, 0); // toFullFolding(int c, StringBuffer out, int options)

            if(iter.didReachLimit() && isIncremental) {
                // the case mapping function tried to look beyond the context limit
                // wait for more input
                offsets.start=iter.getCaseMapCPStart();
                return;
            }

            /* decode the result */
            if(c<0) {
                /* c mapped to itself, no change */
                continue;
            } else if(c<=UCaseProps.MAX_STRING_LENGTH) {
                /* replace by the mapping string */
                delta=iter.replace(result.toString());
                result.setLength(0);
            } else {
                /* replace by single-code point mapping */
                delta=iter.replace(UTF16.valueOf(c));
            }

            if(delta!=0) {
                offsets.limit += delta;
                offsets.contextLimit += delta;
            }
        }
        offsets.start = offsets.limit;
    }

    static SourceTargetUtility sourceTargetUtility = null;

    /* (non-Javadoc)
     * @see android.icu.text.Transliterator#addSourceTargetSet(android.icu.text.UnicodeSet, android.icu.text.UnicodeSet, android.icu.text.UnicodeSet)
     */
    @Override
    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        synchronized (UppercaseTransliterator.class) {
            if (sourceTargetUtility == null) {
                sourceTargetUtility = new SourceTargetUtility(new Transform<String,String>() {
                    @Override
                    public String transform(String source) {
                        return UCharacter.foldCase(source, true);
                    }
                });
            }
        }
        sourceTargetUtility.addSourceTargetSet(this, inputFilter, sourceSet, targetSet);
    }
}
