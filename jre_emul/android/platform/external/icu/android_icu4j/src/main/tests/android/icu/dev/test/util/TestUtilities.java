/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.util.CollectionUtilities;

public class TestUtilities extends TestFmwk {
    @Test
    public void TestCollectionUtilitySpeed() {
        TreeSet ts1 = new TreeSet();
        TreeSet ts2 = new TreeSet();
        int size = 1000;
        int iterations = 1000;
        String prefix =  "abc";
        String postfix = "nop";
        for (int i = 0; i < size; ++i) {
            ts1.add(prefix + String.valueOf(i) + postfix);
            ts2.add(prefix + String.valueOf(i) + postfix);
        }
        // warm up
        CollectionUtilities.containsAll(ts1, ts2);
        ts1.containsAll(ts2);

        timeAndCompare(ts1, ts2, iterations, true, .75);
        // now different sets
        ts1.add("Able");
        timeAndCompare(ts1, ts2, iterations, true, .75);
        timeAndCompare(ts2, ts1, iterations*100, false, 1.05);
    }

    private void timeAndCompare(TreeSet ts1, TreeSet ts2, int iterations, boolean expected, double factorOfStandard) {
        double utilityTimeSorted = timeUtilityContainsAll(iterations, ts1, ts2, expected)/(double)iterations;
        double standardTimeSorted = timeStandardContainsAll(iterations, ts1, ts2, expected)/(double)iterations;

        if (utilityTimeSorted < standardTimeSorted*factorOfStandard) {
            logln("Sorted: Utility time (" + utilityTimeSorted + ") << Standard duration (" + standardTimeSorted + "); " + 100*(utilityTimeSorted/standardTimeSorted) + "%");
        } else {
            /*errln*/logln("Sorted: Utility time (" + utilityTimeSorted + ") !<< Standard duration (" + standardTimeSorted + "); " + 100*(utilityTimeSorted/standardTimeSorted) + "%");
        }
    }

    private long timeStandardContainsAll(int iterations, Set hs1, Set hs2, boolean expected) {
        long standardTime;
        {
            long start, end;
            boolean temp = false;

            start = System.currentTimeMillis();
            for (int i = 0; i < iterations; ++i) {
                temp = hs1.containsAll(hs2);
                if (temp != expected) {
                    errln("Bad result");
                }
            }
            end = System.currentTimeMillis();
            standardTime = end - start;
        }
        return standardTime;
    }

    private long timeUtilityContainsAll(int iterations, Set hs1, Set hs2, boolean expected) {
        long utilityTime;
        {
            long start, end;
            boolean temp = false;
            start = System.currentTimeMillis();
            for (int i = 0; i < iterations; ++i) {
                temp = CollectionUtilities.containsAll(hs1, hs2);
                if (temp != expected) {
                    errln("Bad result");
                }
            }
            end = System.currentTimeMillis();
            utilityTime = end - start;
        }
        return utilityTime;
    }

    @Test
    public void TestCollectionUtilities() {
        String[][] test = {{"a", "c", "e", "g", "h", "z"}, {"b", "d", "f", "h", "w"}, { "a", "b" }, { "a", "d" }, {"d"}, {}}; //
        int resultMask = 0;
        for (int i = 0; i < test.length; ++i) {
            Collection a = new TreeSet(Arrays.asList(test[i]));
            for (int j = 0; j < test.length; ++j) {
                Collection b = new TreeSet(Arrays.asList(test[j]));
                int relation = CollectionUtilities.getContainmentRelation(a, b);
                resultMask |= (1 << relation);
                switch (relation) {
                case CollectionUtilities.ALL_EMPTY:
                    checkContainment(a.size() == 0 && b.size() == 0, a, relation, b);
                    break;
                case CollectionUtilities.NOT_A_SUPERSET_B:
                    checkContainment(a.size() == 0 && b.size() != 0, a, relation, b);
                    break;
                case CollectionUtilities.NOT_A_DISJOINT_B:
                    checkContainment(a.equals(b) && a.size() != 0, a, relation, b);
                    break;
                case CollectionUtilities.NOT_A_SUBSET_B:
                    checkContainment(a.size() != 0 && b.size() == 0, a, relation, b);
                    break;
                case CollectionUtilities.A_PROPER_SUBSET_OF_B:
                    checkContainment(b.containsAll(a) && !a.equals(b), a, relation, b);
                    break;
                case CollectionUtilities.NOT_A_EQUALS_B:
                    checkContainment(!CollectionUtilities.containsSome(a, b) && a.size() != 0 && b.size() != 0, a, relation, b);
                    break;
                case CollectionUtilities.A_PROPER_SUPERSET_B:
                    checkContainment(a.containsAll(b) && !a.equals(b), a, relation, b);
                break;
                case CollectionUtilities.A_PROPER_OVERLAPS_B:
                    checkContainment(!b.containsAll(a) && !a.containsAll(b) && CollectionUtilities.containsSome(a, b), a, relation, b);
                break;
                }
            }
        }
        if (resultMask != 0xFF) {
            String missing = "";
            for (int i = 0; i < 8; ++i) {
                if ((resultMask & (1 << i)) == 0) {
                    if (missing.length() != 0) missing += ", ";
                    missing += RelationName[i];
                }
            }
            errln("Not all ContainmentRelations checked: " + missing);
        }
    }

    static final String[] RelationName = {"ALL_EMPTY",
            "NOT_A_SUPERSET_B",
            "NOT_A_DISJOINT_B",
            "NOT_A_SUBSET_B",
            "A_PROPER_SUBSET_OF_B",
            "A_PROPER_DISJOINT_B",
            "A_PROPER_SUPERSET_B",
            "A_PROPER_OVERLAPS_B"};

    /**
     *
     */
    private void checkContainment(boolean c, Collection a, int relation, Collection b) {
        if (!c) {
            errln("Fails relation: " + a + " \t" + RelationName[relation] + " \t" + b);
        }
    }
}
