/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2013-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import android.icu.text.PluralRules.FixedDecimal;
import android.icu.text.PluralRules.KeywordStatus;
import android.icu.util.Output;

/**
 * @author markdavis
 * Refactor samples as first step to moving into CLDR
 * 
 * @deprecated This API is ICU internal only.
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
@Deprecated
public class PluralSamples {

    private PluralRules pluralRules;
    private final Map<String, List<Double>> _keySamplesMap;

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public final Map<String, Boolean> _keyLimitedMap;
    private final Map<String, Set<FixedDecimal>> _keyFractionSamplesMap;
    private final Set<FixedDecimal> _fractionSamples;

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public PluralSamples(PluralRules pluralRules) {
        this.pluralRules = pluralRules;
        Set<String> keywords = pluralRules.getKeywords();
        // ensure both _keySamplesMap and _keyLimitedMap are initialized.
        // If this were allowed to vary on a per-call basis, we'd have to recheck and
        // possibly rebuild the samples cache.  Doesn't seem worth it.
        // This 'max samples' value only applies to keywords that are unlimited, for
        // other keywords all the matching values are returned.  This might be a lot.
        final int MAX_SAMPLES = 3;

        Map<String, Boolean> temp = new HashMap<String, Boolean>();
        for (String k : keywords) {
            temp.put(k, pluralRules.isLimited(k));
        }
        _keyLimitedMap = temp;

        Map<String, List<Double>> sampleMap = new HashMap<String, List<Double>>();
        int keywordsRemaining = keywords.size();

        int limit = 128; // Math.max(5, getRepeatLimit() * MAX_SAMPLES) * 2;

        for (int i = 0; keywordsRemaining > 0 && i < limit; ++i) {
            keywordsRemaining = addSimpleSamples(pluralRules, MAX_SAMPLES, sampleMap, keywordsRemaining, i / 2.0);
        }
        // Hack for Celtic
        keywordsRemaining = addSimpleSamples(pluralRules, MAX_SAMPLES, sampleMap, keywordsRemaining, 1000000);


        // collect explicit samples
        Map<String, Set<FixedDecimal>> sampleFractionMap = new HashMap<String, Set<FixedDecimal>>();
        Set<FixedDecimal> mentioned = new TreeSet<FixedDecimal>();
        // make sure that there is at least one 'other' value
        Map<String, Set<FixedDecimal>> foundKeywords = new HashMap<String, Set<FixedDecimal>>();
        for (FixedDecimal s : mentioned) {
            String keyword = pluralRules.select(s);
            addRelation(foundKeywords, keyword, s);
        }
        main:
            if (foundKeywords.size() != keywords.size()) {
                for (int i = 1; i < 1000; ++i) {
                    boolean done = addIfNotPresent(i, mentioned, foundKeywords);
                    if (done) break main;
                }
                // if we are not done, try tenths
                for (int i = 10; i < 1000; ++i) {
                    boolean done = addIfNotPresent(i/10d, mentioned, foundKeywords);
                    if (done) break main;
                }
                System.out.println("Failed to find sample for each keyword: " + foundKeywords + "\n\t" + pluralRules + "\n\t" + mentioned);
            }
        mentioned.add(new FixedDecimal(0)); // always there
        mentioned.add(new FixedDecimal(1)); // always there
        mentioned.add(new FixedDecimal(2)); // always there
        mentioned.add(new FixedDecimal(0.1,1)); // always there
        mentioned.add(new FixedDecimal(1.99,2)); // always there
        mentioned.addAll(fractions(mentioned));
        for (FixedDecimal s : mentioned) {
            String keyword = pluralRules.select(s);
            Set<FixedDecimal> list = sampleFractionMap.get(keyword);
            if (list == null) {
                list = new LinkedHashSet<FixedDecimal>(); // will be sorted because the iteration is
                sampleFractionMap.put(keyword, list);
            }
            list.add(s);
        }

        if (keywordsRemaining > 0) {
            for (String k : keywords) {
                if (!sampleMap.containsKey(k)) {
                    sampleMap.put(k, Collections.<Double>emptyList());
                }
                if (!sampleFractionMap.containsKey(k)) {
                    sampleFractionMap.put(k, Collections.<FixedDecimal>emptySet());
                }
            }
        }

        // Make lists immutable so we can return them directly
        for (Entry<String, List<Double>> entry : sampleMap.entrySet()) {
            sampleMap.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        for (Entry<String, Set<FixedDecimal>> entry : sampleFractionMap.entrySet()) {
            sampleFractionMap.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        _keySamplesMap = sampleMap;
        _keyFractionSamplesMap = sampleFractionMap;
        _fractionSamples = Collections.unmodifiableSet(mentioned);
    }

    private int addSimpleSamples(PluralRules pluralRules, final int MAX_SAMPLES, Map<String, List<Double>> sampleMap,
            int keywordsRemaining, double val) {
        String keyword = pluralRules.select(val);
        boolean keyIsLimited = _keyLimitedMap.get(keyword);

        List<Double> list = sampleMap.get(keyword);
        if (list == null) {
            list = new ArrayList<Double>(MAX_SAMPLES);
            sampleMap.put(keyword, list);
        } else if (!keyIsLimited && list.size() == MAX_SAMPLES) {
            return keywordsRemaining;
        }
        list.add(Double.valueOf(val));

        if (!keyIsLimited && list.size() == MAX_SAMPLES) {
            --keywordsRemaining;
        }
        return keywordsRemaining;
    }

    private void addRelation(Map<String, Set<FixedDecimal>> foundKeywords, String keyword, FixedDecimal s) {
        Set<FixedDecimal> set = foundKeywords.get(keyword);
        if (set == null) {
            foundKeywords.put(keyword, set = new HashSet<FixedDecimal>());
        }
        set.add(s);
    }

    private boolean addIfNotPresent(double d, Set<FixedDecimal> mentioned, Map<String, Set<FixedDecimal>> foundKeywords) {
        FixedDecimal numberInfo = new FixedDecimal(d);
        String keyword = pluralRules.select(numberInfo);
        if (!foundKeywords.containsKey(keyword) || keyword.equals("other")) {
            addRelation(foundKeywords, keyword, numberInfo);
            mentioned.add(numberInfo);
            if (keyword.equals("other")) {
                if (foundKeywords.get("other").size() > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final int[] TENS = {1, 10, 100, 1000, 10000, 100000, 1000000};

    private static final int LIMIT_FRACTION_SAMPLES = 3;


    private Set<FixedDecimal> fractions(Set<FixedDecimal> original) {
        Set<FixedDecimal> toAddTo = new HashSet<FixedDecimal>();

        Set<Integer> result = new HashSet<Integer>();
        for (FixedDecimal base1 : original) {
            result.add((int)base1.integerValue);
        }
        List<Integer> ints = new ArrayList<Integer>(result);
        Set<String> keywords = new HashSet<String>();

        for (int j = 0; j < ints.size(); ++j) {
            Integer base = ints.get(j);
            String keyword = pluralRules.select(base);
            if (keywords.contains(keyword)) {
                continue;
            }
            keywords.add(keyword);
            toAddTo.add(new FixedDecimal(base,1)); // add .0
            toAddTo.add(new FixedDecimal(base,2)); // add .00
            Integer fract = getDifferentCategory(ints, keyword);
            if (fract >= TENS[LIMIT_FRACTION_SAMPLES-1]) { // make sure that we always get the value
                toAddTo.add(new FixedDecimal(base + "." + fract));
            } else {
                for (int visibleFractions = 1; visibleFractions < LIMIT_FRACTION_SAMPLES; ++visibleFractions) {
                    for (int i = 1; i <= visibleFractions; ++i) {
                        // with visible fractions = 3, and fract = 1, then we should get x.10, 0.01
                        // with visible fractions = 3, and fract = 15, then we should get x.15, x.15
                        if (fract >= TENS[i]) {
                            continue;
                        }
                        toAddTo.add(new FixedDecimal(base + fract/(double)TENS[i], visibleFractions));
                    }
                }
            }
        }
        return toAddTo;
    }

    private Integer getDifferentCategory(List<Integer> ints, String keyword) {
        for (int i = ints.size() - 1; i >= 0; --i) {
            Integer other = ints.get(i);
            String keywordOther = pluralRules.select(other);
            if (!keywordOther.equals(keyword)) {
                return other;
            }
        }
        return 37;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public KeywordStatus getStatus(String keyword, int offset, Set<Double> explicits, Output<Double> uniqueValue) {
        if (uniqueValue != null) {
            uniqueValue.value = null;
        }

        if (!pluralRules.getKeywords().contains(keyword)) {
            return KeywordStatus.INVALID;
        }
        Collection<Double> values = pluralRules.getAllKeywordValues(keyword);
        if (values == null) {
            return KeywordStatus.UNBOUNDED;
        }
        int originalSize = values.size();

        if (explicits == null) {
            explicits = Collections.emptySet();
        }

        // Quick check on whether there are multiple elements

        if (originalSize > explicits.size()) {
            if (originalSize == 1) {
                if (uniqueValue != null) {
                    uniqueValue.value = values.iterator().next();
                }
                return KeywordStatus.UNIQUE;
            }
            return KeywordStatus.BOUNDED;
        }

        // Compute if the quick test is insufficient.

        HashSet<Double> subtractedSet = new HashSet<Double>(values);
        for (Double explicit : explicits) {
            subtractedSet.remove(explicit - offset);
        }
        if (subtractedSet.size() == 0) {
            return KeywordStatus.SUPPRESSED;
        }

        if (uniqueValue != null && subtractedSet.size() == 1) {
            uniqueValue.value = subtractedSet.iterator().next();
        }

        return originalSize == 1 ? KeywordStatus.UNIQUE : KeywordStatus.BOUNDED;
    }

    Map<String, List<Double>> getKeySamplesMap() {
        return _keySamplesMap;
    }

    Map<String, Set<FixedDecimal>> getKeyFractionSamplesMap() {
        return _keyFractionSamplesMap;
    }

    Set<FixedDecimal> getFractionSamples() {
        return _fractionSamples;
    }

    /**
     * Returns all the values that trigger this keyword, or null if the number of such
     * values is unlimited.
     *
     * @param keyword the keyword
     * @return the values that trigger this keyword, or null.  The returned collection
     * is immutable. It will be empty if the keyword is not defined.
     */

    Collection<Double> getAllKeywordValues(String keyword) {
        // HACK for now
        if (!pluralRules.getKeywords().contains(keyword)) {
            return Collections.<Double>emptyList();
        }
        Collection<Double> result = getKeySamplesMap().get(keyword);

        // We depend on MAX_SAMPLES here.  It's possible for a conjunction
        // of unlimited rules that 'looks' unlimited to return a limited
        // number of values.  There's no bounds to this limited number, in
        // general, because you can construct arbitrarily complex rules.  Since
        // we always generate 3 samples if a rule is really unlimited, that's
        // where we put the cutoff.
        if (result.size() > 2 && !_keyLimitedMap.get(keyword)) {
            return null;
        }
        return result;
    }
}
