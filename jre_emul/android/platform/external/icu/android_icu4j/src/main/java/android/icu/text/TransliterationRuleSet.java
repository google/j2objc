/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

import java.util.ArrayList;
import java.util.List;

import android.icu.impl.UtilityExtensions;

/**
 * A set of rules for a <code>RuleBasedTransliterator</code>.  This set encodes
 * the transliteration in one direction from one set of characters or short
 * strings to another.  A <code>RuleBasedTransliterator</code> consists of up to
 * two such sets, one for the forward direction, and one for the reverse.
 *
 * <p>A <code>TransliterationRuleSet</code> has one important operation, that of
 * finding a matching rule at a given point in the text.  This is accomplished
 * by the <code>findMatch()</code> method.
 *
 * <p>Copyright &copy; IBM Corporation 1999.  All rights reserved.
 *
 * @author Alan Liu
 */
class TransliterationRuleSet {
    /**
     * Vector of rules, in the order added.
     */
    private List<TransliterationRule> ruleVector;

    /**
     * Length of the longest preceding context
     */
    private int maxContextLength;

    /**
     * Sorted and indexed table of rules.  This is created by freeze() from
     * the rules in ruleVector.  rules.length >= ruleVector.size(), and the
     * references in rules[] are aliases of the references in ruleVector.
     * A single rule in ruleVector is listed one or more times in rules[].
     */
    private TransliterationRule[] rules;

    /**
     * Index table.  For text having a first character c, compute x = c&0xFF.
     * Now use rules[index[x]..index[x+1]-1].  This index table is created by
     * freeze().
     */
    private int[] index;

    /**
     * Construct a new empty rule set.
     */
    public TransliterationRuleSet() {
        ruleVector = new ArrayList<TransliterationRule>();
        maxContextLength = 0;
    }

    /**
     * Return the maximum context length.
     * @return the length of the longest preceding context.
     */
    public int getMaximumContextLength() {
        return maxContextLength;
    }

    /**
     * Add a rule to this set.  Rules are added in order, and order is
     * significant.
     * @param rule the rule to add
     */
    public void addRule(TransliterationRule rule) {
        ruleVector.add(rule);
        int len;
        if ((len = rule.getAnteContextLength()) > maxContextLength) {
            maxContextLength = len;
        }

        rules = null;
    }

    /**
     * Close this rule set to further additions, check it for masked rules,
     * and index it to optimize performance.
     * @exception IllegalArgumentException if some rules are masked
     */
    public void freeze() {
        /* Construct the rule array and index table.  We reorder the
         * rules by sorting them into 256 bins.  Each bin contains all
         * rules matching the index value for that bin.  A rule
         * matches an index value if string whose first key character
         * has a low byte equal to the index value can match the rule.
         *
         * Each bin contains zero or more rules, in the same order
         * they were found originally.  However, the total rules in
         * the bins may exceed the number in the original vector,
         * since rules that have a variable as their first key
         * character will generally fall into more than one bin.
         *
         * That is, each bin contains all rules that either have that
         * first index value as their first key character, or have
         * a set containing the index value as their first character.
         */
        int n = ruleVector.size();
        index = new int[257]; // [sic]
        List<TransliterationRule> v = new ArrayList<TransliterationRule>(2*n); // heuristic; adjust as needed

        /* Precompute the index values.  This saves a LOT of time.
         */
        int[] indexValue = new int[n];
        for (int j=0; j<n; ++j) {
            TransliterationRule r = ruleVector.get(j);
            indexValue[j] = r.getIndexValue();
        }
        for (int x=0; x<256; ++x) {
            index[x] = v.size();
            for (int j=0; j<n; ++j) {
                if (indexValue[j] >= 0) {
                    if (indexValue[j] == x) {
                        v.add(ruleVector.get(j));
                    }
                } else {
                    // If the indexValue is < 0, then the first key character is
                    // a set, and we must use the more time-consuming
                    // matchesIndexValue check.  In practice this happens
                    // rarely, so we seldom tread this code path.
                    TransliterationRule r = ruleVector.get(j);
                    if (r.matchesIndexValue(x)) {
                        v.add(r);
                    }
                }
            }
        }
        index[256] = v.size();

        /* Freeze things into an array.
         */
        rules = new TransliterationRule[v.size()];
        v.toArray(rules);

        StringBuilder errors = null;

        /* Check for masking.  This is MUCH faster than our old check,
         * which was each rule against each following rule, since we
         * only have to check for masking within each bin now.  It's
         * 256*O(n2^2) instead of O(n1^2), where n1 is the total rule
         * count, and n2 is the per-bin rule count.  But n2<<n1, so
         * it's a big win.
         */
        for (int x=0; x<256; ++x) {
            for (int j=index[x]; j<index[x+1]-1; ++j) {
                TransliterationRule r1 = rules[j];
                for (int k=j+1; k<index[x+1]; ++k) {
                    TransliterationRule r2 = rules[k];
                    if (r1.masks(r2)) {
                        if (errors == null) {
                            errors = new StringBuilder();
                        } else {
                            errors.append("\n");
                        }
                        errors.append("Rule " + r1 + " masks " + r2);
                    }
                }
            }
        }

        if (errors != null) {
            throw new IllegalArgumentException(errors.toString());
        }
    }

    /**
     * Transliterate the given text with the given UTransPosition
     * indices.  Return TRUE if the transliteration should continue
     * or FALSE if it should halt (because of a U_PARTIAL_MATCH match).
     * Note that FALSE is only ever returned if isIncremental is TRUE.
     * @param text the text to be transliterated
     * @param pos the position indices, which will be updated
     * @param incremental if TRUE, assume new text may be inserted
     * at index.limit, and return FALSE if thre is a partial match.
     * @return TRUE unless a U_PARTIAL_MATCH has been obtained,
     * indicating that transliteration should stop until more text
     * arrives.
     */
    public boolean transliterate(Replaceable text,
                                 Transliterator.Position pos,
                                 boolean incremental) {
        int indexByte = text.char32At(pos.start) & 0xFF;
        for (int i=index[indexByte]; i<index[indexByte+1]; ++i) {
            int m = rules[i].matchAndReplace(text, pos, incremental);
            switch (m) {
            case UnicodeMatcher.U_MATCH:
                if (Transliterator.DEBUG) {
                    System.out.println((incremental ? "Rule.i: match ":"Rule: match ") +
                                       rules[i].toRule(true) + " => " +
                                       UtilityExtensions.formatInput(text, pos));
                }
                return true;
            case UnicodeMatcher.U_PARTIAL_MATCH:
                if (Transliterator.DEBUG) {
                    System.out.println((incremental ? "Rule.i: partial match ":"Rule: partial match ") +
                                       rules[i].toRule(true) + " => " +
                                       UtilityExtensions.formatInput(text, pos));
                }
                return false;
                default:
                    if (Transliterator.DEBUG) {
                        System.out.println("Rule: no match " + rules[i]);
                    }
            }
        }
        // No match or partial match from any rule
        pos.start += UTF16.getCharCount(text.char32At(pos.start));
        if (Transliterator.DEBUG) {
            System.out.println((incremental ? "Rule.i: no match => ":"Rule: no match => ") +
                               UtilityExtensions.formatInput(text, pos));
        }
        return true;
    }

    /**
     * Create rule strings that represents this rule set.
     */
    String toRules(boolean escapeUnprintable) {
        int i;
        int count = ruleVector.size();
        StringBuilder ruleSource = new StringBuilder();
        for (i=0; i<count; ++i) {
            if (i != 0) {
                ruleSource.append('\n');
            }
            TransliterationRule r = ruleVector.get(i);
            ruleSource.append(r.toRule(escapeUnprintable));
        }
        return ruleSource.toString();
    }

    // TODO Handle the case where we have :: [a] ; a > |b ; b > c ;
    // TODO Merge into r.addSourceTargetSet, to avoid duplicate testing
    void addSourceTargetSet(UnicodeSet filter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet currentFilter = new UnicodeSet(filter);
        UnicodeSet revisiting = new UnicodeSet();
        int count = ruleVector.size();
        for (int i=0; i<count; ++i) {
            TransliterationRule r = ruleVector.get(i);
            r.addSourceTargetSet(currentFilter, sourceSet, targetSet, revisiting.clear());
            currentFilter.addAll(revisiting);
        }
    }

}
