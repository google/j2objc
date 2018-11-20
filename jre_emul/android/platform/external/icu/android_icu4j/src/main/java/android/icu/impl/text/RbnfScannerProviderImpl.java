/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2009-2014, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

package android.icu.impl.text;

import java.util.HashMap;
import java.util.Map;

import android.icu.impl.ICUDebug;
import android.icu.text.CollationElementIterator;
import android.icu.text.Collator;
import android.icu.text.RbnfLenientScanner;
import android.icu.text.RbnfLenientScannerProvider;
import android.icu.text.RuleBasedCollator;
import android.icu.util.ULocale;

/**
 * Returns RbnfLenientScanners that use the old RuleBasedNumberFormat
 * implementation behind setLenientParseMode, which is based on Collator.
 * @deprecated This API is ICU internal only.
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
@Deprecated
public class RbnfScannerProviderImpl implements RbnfLenientScannerProvider {
    private static final boolean DEBUG = ICUDebug.enabled("rbnf");
    private Map<String, RbnfLenientScanner> cache;

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public RbnfScannerProviderImpl() {
        cache = new HashMap<String, RbnfLenientScanner>();
    }

    /**
     * Returns a collation-based scanner.
     *
     * Only primary differences are treated as significant.  This means that case
     * differences, accent differences, alternate spellings of the same letter
     * (e.g., ae and a-umlaut in German), ignorable characters, etc. are ignored in
     * matching the text.  In many cases, numerals will be accepted in place of words
     * or phrases as well.
     *
     * For example, all of the following will correctly parse as 255 in English in
     * lenient-parse mode:
     * <br>"two hundred fifty-five"
     * <br>"two hundred fifty five"
     * <br>"TWO HUNDRED FIFTY-FIVE"
     * <br>"twohundredfiftyfive"
     * <br>"2 hundred fifty-5"
     *
     * The Collator used is determined by the locale that was
     * passed to this object on construction.  The description passed to this object
     * on construction may supply additional collation rules that are appended to the
     * end of the default collator for the locale, enabling additional equivalences
     * (such as adding more ignorable characters or permitting spelled-out version of
     * symbols; see the demo program for examples).
     *
     * It's important to emphasize that even strict parsing is relatively lenient: it
     * will accept some text that it won't produce as output.  In English, for example,
     * it will correctly parse "two hundred zero" and "fifteen hundred".
     * 
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public RbnfLenientScanner get(ULocale locale, String extras) {
        RbnfLenientScanner result = null;
        String key = locale.toString() + "/" + extras;
        synchronized(cache) {
            result = cache.get(key);
            if (result != null) {
                return result;
            }
        }
        result = createScanner(locale, extras);
        synchronized(cache) {
            cache.put(key, result);
        }
        return result;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected RbnfLenientScanner createScanner(ULocale locale, String extras) {
        RuleBasedCollator collator = null;
        try {
            // create a default collator based on the locale,
            // then pull out that collator's rules, append any additional
            // rules specified in the description, and create a _new_
            // collator based on the combination of those rules
            collator = (RuleBasedCollator)Collator.getInstance(locale.toLocale());
            if (extras != null) {
                String rules = collator.getRules() + extras;
                collator = new RuleBasedCollator(rules);
            }
            collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        }
        catch (Exception e) {
            // If we get here, it means we have a malformed set of
            // collation rules, which hopefully won't happen
            ///CLOVER:OFF
            if (DEBUG){ // debug hook
                e.printStackTrace(); System.out.println("++++");
            }
            collator = null;
            ///CLOVER:ON
        }

        return new RbnfLenientScannerImpl(collator);
    }

    private static class RbnfLenientScannerImpl implements RbnfLenientScanner {
        private final RuleBasedCollator collator;

        private RbnfLenientScannerImpl(RuleBasedCollator rbc) {
            this.collator = rbc;
        }

        public boolean allIgnorable(String s) {
            CollationElementIterator iter = collator.getCollationElementIterator(s);

            int o = iter.next();
            while (o != CollationElementIterator.NULLORDER
                   && CollationElementIterator.primaryOrder(o) == 0) {
                o = iter.next();
            }
            return o == CollationElementIterator.NULLORDER;
        }

        public int[] findText(String str, String key, int startingAt) {
            int p = startingAt;
            int keyLen = 0;

            // basically just isolate smaller and smaller substrings of
            // the target string (each running to the end of the string,
            // and with the first one running from startingAt to the end)
            // and then use prefixLength() to see if the search key is at
            // the beginning of each substring.  This is excruciatingly
            // slow, but it will locate the key and tell use how long the
            // matching text was.
            while (p < str.length() && keyLen == 0) {
                keyLen = prefixLength(str.substring(p), key);
                if (keyLen != 0) {
                    return new int[] { p, keyLen };
                }
                ++p;
            }
            // if we make it to here, we didn't find it.  Return -1 for the
            // location.  The length should be ignored, but set it to 0,
            // which should be "safe"
            return new int[] { -1, 0 };
        }

        ///CLOVER:OFF
        // The following method contains the same signature as findText
        //  and has never been used by anything once.
        @SuppressWarnings("unused")
        public int[] findText2(String str, String key, int startingAt) {

            CollationElementIterator strIter = collator.getCollationElementIterator(str);
            CollationElementIterator keyIter = collator.getCollationElementIterator(key);

            int keyStart = -1;

            strIter.setOffset(startingAt);

            int oStr = strIter.next();
            int oKey = keyIter.next();
            while (oKey != CollationElementIterator.NULLORDER) {
                while (oStr != CollationElementIterator.NULLORDER &&
                       CollationElementIterator.primaryOrder(oStr) == 0) {
                    oStr = strIter.next();
                }

                while (oKey != CollationElementIterator.NULLORDER &&
                       CollationElementIterator.primaryOrder(oKey) == 0) {
                    oKey = keyIter.next();
                }

                if (oStr == CollationElementIterator.NULLORDER) {
                    return new int[] { -1, 0 };
                }

                if (oKey == CollationElementIterator.NULLORDER) {
                    break;
                }

                if (CollationElementIterator.primaryOrder(oStr) ==
                    CollationElementIterator.primaryOrder(oKey)) {
                    keyStart = strIter.getOffset();
                    oStr = strIter.next();
                    oKey = keyIter.next();
                } else {
                    if (keyStart != -1) {
                        keyStart = -1;
                        keyIter.reset();
                    } else {
                        oStr = strIter.next();
                    }
                }
            }

            return new int[] { keyStart, strIter.getOffset() - keyStart };
        }
        ///CLOVER:ON

        public int prefixLength(String str, String prefix) {
            // Create two collation element iterators, one over the target string
            // and another over the prefix.
            //
            // Previous code was matching "fifty-" against " fifty" and leaving
            // the number " fifty-7" to parse as 43 (50 - 7).
            // Also it seems that if we consume the entire prefix, that's ok even
            // if we've consumed the entire string, so I switched the logic to
            // reflect this.

            CollationElementIterator strIter = collator.getCollationElementIterator(str);
            CollationElementIterator prefixIter = collator.getCollationElementIterator(prefix);

            // match collation elements between the strings
            int oStr = strIter.next();
            int oPrefix = prefixIter.next();

            while (oPrefix != CollationElementIterator.NULLORDER) {
                // skip over ignorable characters in the target string
                while (CollationElementIterator.primaryOrder(oStr) == 0 && oStr !=
                       CollationElementIterator.NULLORDER) {
                    oStr = strIter.next();
                }

                // skip over ignorable characters in the prefix
                while (CollationElementIterator.primaryOrder(oPrefix) == 0 && oPrefix !=
                       CollationElementIterator.NULLORDER) {
                    oPrefix = prefixIter.next();
                }

                // if skipping over ignorables brought to the end of
                // the prefix, we DID match: drop out of the loop
                if (oPrefix == CollationElementIterator.NULLORDER) {
                    break;
                }

                // if skipping over ignorables brought us to the end
                // of the target string, we didn't match and return 0
                if (oStr == CollationElementIterator.NULLORDER) {
                    return 0;
                }

                // match collation elements from the two strings
                // (considering only primary differences).  If we
                // get a mismatch, dump out and return 0
                if (CollationElementIterator.primaryOrder(oStr) != 
                    CollationElementIterator.primaryOrder(oPrefix)) {
                    return 0;
                }

                // otherwise, advance to the next character in each string
                // and loop (we drop out of the loop when we exhaust
                // collation elements in the prefix)

                oStr = strIter.next();
                oPrefix = prefixIter.next();
            }

            int result = strIter.getOffset();
            if (oStr != CollationElementIterator.NULLORDER) {
                --result;
            }
            return result;
        }
    }
}
