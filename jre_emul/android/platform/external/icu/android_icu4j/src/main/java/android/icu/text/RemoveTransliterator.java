/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;


/**
 * A transliterator that removes characters.  This is useful in conjunction
 * with a filter.
 */
class RemoveTransliterator extends Transliterator {

    /**
     * ID for this transliterator.
     */
    private static final String _ID = "Any-Remove";

    /**
     * System registration hook.
     */
    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            @Override
            public Transliterator getInstance(String ID) {
                return new RemoveTransliterator();
            }
        });
        Transliterator.registerSpecialInverse("Remove", "Null", false);
    }

    /**
     * Constructs a transliterator.
     */
    public RemoveTransliterator() {
        super(_ID, null);
    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    @Override
    protected void handleTransliterate(Replaceable text,
                                       Position index, boolean incremental) {
        // Our caller (filteredTransliterate) has already narrowed us
        // to an unfiltered run.  Delete it.
        text.replace(index.start, index.limit, "");
        int len = index.limit - index.start;
        index.contextLimit -= len;
        index.limit -= len;
    }

    /* (non-Javadoc)
     * @see android.icu.text.Transliterator#addSourceTargetSet(boolean, android.icu.text.UnicodeSet, android.icu.text.UnicodeSet)
     */
    @Override
    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        // intersect myFilter with the input filter
        UnicodeSet myFilter = getFilterAsUnicodeSet(inputFilter);
        sourceSet.addAll(myFilter);
        // do nothing with the target
    }
}
