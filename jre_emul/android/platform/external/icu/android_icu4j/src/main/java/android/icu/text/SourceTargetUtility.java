/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2010-2011, Google, International Business Machines            *
 * Corporation and others. All Rights Reserved.                                *
 *******************************************************************************
 */
package android.icu.text;

import java.util.HashSet;
import java.util.Set;

import android.icu.lang.CharSequences;

/**
 * Simple internal utility class for helping with getSource/TargetSet
 */
class SourceTargetUtility {
    final Transform<String, String> transform;
    final UnicodeSet sourceCache;
    final Set<String> sourceStrings;
    static final UnicodeSet NON_STARTERS = new UnicodeSet("[:^ccc=0:]").freeze();
    static Normalizer2 NFC = Normalizer2.getNFCInstance();
    //static final UnicodeSet TRAILING_COMBINING = new UnicodeSet();

    public SourceTargetUtility(Transform<String, String> transform) {
        this(transform, null);
    }

    public SourceTargetUtility(Transform<String, String> transform, Normalizer2 normalizer) {
        this.transform = transform;
        if (normalizer != null) {
//            synchronized (SourceTargetUtility.class) {
//                if (NFC == null) {
//                    NFC = Normalizer2.getInstance(null, "nfc", Mode.COMPOSE);
//                    for (int i = 0; i <= 0x10FFFF; ++i) {
//                        String d = NFC.getDecomposition(i);
//                        if (d == null) {
//                            continue;
//                        }
//                        String s = NFC.normalize(d);
//                        if (!CharSequences.equals(i, s)) {
//                            continue;
//                        }
//                        // composes
//                        boolean first = false;
//                        for (int trailing : CharSequences.codePoints(d)) {
//                            if (first) {
//                                first = false;
//                            } else {
//                                TRAILING_COMBINING.add(trailing);
//                            }
//                        }
//                    }
//                }
//            }
            sourceCache = new UnicodeSet("[:^ccc=0:]");
        } else {
            sourceCache = new UnicodeSet();
        }
        sourceStrings = new HashSet<String>();
        for (int i = 0; i <= 0x10FFFF; ++i) {
            String s = transform.transform(UTF16.valueOf(i));
            boolean added = false;
            if (!CharSequences.equals(i, s)) {
                sourceCache.add(i);
                added = true;
            }
            if (normalizer == null) {
                continue;
            }
            String d = NFC.getDecomposition(i);
            if (d == null) {
                continue;
            }
            s = transform.transform(d);
            if (!d.equals(s)) {
                sourceStrings.add(d);
            }
            if (added) {
                continue;
            }
            if (!normalizer.isInert(i)) {
                sourceCache.add(i);
                continue;
            }
            // see if any of the non-starters change s; if so, add i
//            for (String ns : TRAILING_COMBINING) {
//                String s2 = transform.transform(s + ns);
//                if (!s2.startsWith(s)) {
//                    sourceCache.add(i);
//                    break;
//                }
//            }

            // int endOfFirst = CharSequences.onCharacterBoundary(d, 1) ? 1 : 2;
            // if (endOfFirst >= d.length()) {
            // continue;
            // }
            // // now add all initial substrings
            // for (int j = 1; j < d.length(); ++j) {
            // if (!CharSequences.onCharacterBoundary(d, j)) {
            // continue;
            // }
            // String dd = d.substring(0,j);
            // s = transform.transform(dd);
            // if (!dd.equals(s)) {
            // sourceStrings.add(dd);
            // }
            // }
        }
        sourceCache.freeze();
    }

    public void addSourceTargetSet(Transliterator transliterator, UnicodeSet inputFilter, UnicodeSet sourceSet,
            UnicodeSet targetSet) {
        UnicodeSet myFilter = transliterator.getFilterAsUnicodeSet(inputFilter);
        UnicodeSet affectedCharacters = new UnicodeSet(sourceCache).retainAll(myFilter);
        sourceSet.addAll(affectedCharacters);
        for (String s : affectedCharacters) {
            targetSet.addAll(transform.transform(s));
        }
        for (String s : sourceStrings) {
            if (myFilter.containsAll(s)) {
                String t = transform.transform(s);
                if (!s.equals(t)) {
                    targetSet.addAll(t);
                    sourceSet.addAll(s);
                }
            }
        }
    }
}
