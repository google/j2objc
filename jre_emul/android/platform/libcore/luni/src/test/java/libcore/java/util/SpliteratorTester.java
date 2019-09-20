/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package libcore.java.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.List;
import java.util.Locale;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;
import static java.util.Spliterator.SUBSIZED;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class SpliteratorTester {
    public static <T> void runBasicIterationTests(Spliterator<T> spliterator,
            List<T> expectedElements) {
        List<T> recorder = new ArrayList<T>(expectedElements.size());
        Consumer<T> consumer = (T value) -> recorder.add(value);

        // tryAdvance.
        boolean didAdvance = spliterator.tryAdvance(consumer);
        assertEquals(!expectedElements.isEmpty(), didAdvance);

        // forEachRemaining.
        spliterator.forEachRemaining(consumer);
        assertEquals(expectedElements, recorder);

        // There should be no more elements remaining in this spliterator.
        assertFalse(spliterator.tryAdvance(consumer));
        spliterator.forEachRemaining((T) -> fail());
    }

    public static <T> void runBasicIterationTests_unordered(Spliterator<T> spliterator,
            List<T> expectedElements, Comparator<T> comparator) {
        ArrayList<T> recorder = new ArrayList<T>(expectedElements.size());
        Consumer<T> consumer = (T value) -> recorder.add(value);

        // tryAdvance.
        if (expectedElements.isEmpty()) {
            assertFalse(spliterator.tryAdvance(consumer));
        } else {
            assertTrue(spliterator.tryAdvance(consumer));
            assertTrue(expectedElements.contains(recorder.get(0)));
        }

        // forEachRemaining.
        spliterator.forEachRemaining(consumer);
        Collections.sort(expectedElements, comparator);
        Collections.sort(recorder, comparator);
        assertEquals(expectedElements, recorder);

        // There should be no more elements remaining in this spliterator.
        assertFalse(spliterator.tryAdvance(consumer));
        spliterator.forEachRemaining((T) -> fail());
    }

    private static <T> void recordAndAssertBasicIteration(
            Spliterator<T> spliterator, ArrayList<T> recorder) {
        spliterator.tryAdvance(value -> recorder.add(value));
        spliterator.forEachRemaining(value -> recorder.add(value));

        // There shouldn't be any elements left in the spliterator.
        assertFalse(spliterator.tryAdvance(value -> recorder.add(value)));
        spliterator.tryAdvance(value -> fail());

        // And all subsequent splits should fail.
        assertNull(spliterator.trySplit());
    }

    public static void testSpliteratorNPE(Spliterator<?> spliterator) {
        try {
            spliterator.tryAdvance(null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            spliterator.forEachRemaining(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public static <T extends Comparable<T>> void runBasicSplitTests(
            Iterable<T> spliterable, List<T> expectedElements) {
        runBasicSplitTests(spliterable, expectedElements, T::compareTo);
    }

    public static <T> void runBasicSplitTests(Spliterator<T> spliterator,
            List<T> expectedElements, Comparator<T> comparator) {
        boolean empty = expectedElements.isEmpty();
        ArrayList<T> recorder = new ArrayList<>();

        // Advance the original spliterator by one element.
        boolean didAdvance = spliterator.tryAdvance(value -> recorder.add(value));
        assertEquals(!empty, didAdvance);

        // Try splitting it.
        Spliterator<T> split1 = spliterator.trySplit();
        // trySplit() may always return null, but is only required to when empty
        if (empty) {
            assertNull(split1);
        } else if (split1 != null) {
            // Try to split the resulting split.
            Spliterator<T> split1_1 = split1.trySplit();
            Spliterator<T> split1_2 = split1.trySplit();
            if (split1_1 != null) {
                recordAndAssertBasicIteration(split1_1, recorder);
            }
            if (split1_2 != null) {
                recordAndAssertBasicIteration(split1_2, recorder);
            }

            // Iterate over the remainder of split1.
            recordAndAssertBasicIteration(split1, recorder);
        }
        // Try to split the original iterator again.
        Spliterator<T> split2 = spliterator.trySplit();
        if (split2 != null) {
            recordAndAssertBasicIteration(split2, recorder);
        }

        // Record all remaining elements of the original spliterator.
        recordAndAssertBasicIteration(spliterator, recorder);

        Collections.sort(expectedElements, comparator);
        Collections.sort(recorder, comparator);
        assertEquals(expectedElements, recorder);
    }

    public static <T> void assertSupportsTrySplit(Iterable spliterable) {
        assertNotNull(spliterable.spliterator().trySplit());
        // only non-empty Iterables may return a non-null value from trySplit()
        assertTrue("Expected nonempty iterable, got " + spliterable,
                spliterable.iterator().hasNext());
    }

    /**
     * Note that the contract of trySplit() is generally quite weak (as it must be). There
     * are no demands about when the spliterator can or cannot split itself. In general, this
     * test is quite loose. All it does is exercise the basic methods on the splits (if any)
     * and confirms that the union of all elements in the split is the collection that was
     * iterated over.
     */
    public static <T> void runBasicSplitTests(Iterable<T> spliterable,
            List<T> expectedElements, Comparator<T> comparator) {
        runBasicSplitTests(spliterable.spliterator(), expectedElements, comparator);
    }

    private static<T> List<T> toList(Iterator<T> iterator) {
        List<T> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    private static<T> List<T> toList(Spliterator<T> spliterator) {
        List<T> result = new ArrayList<>();
        spliterator.forEachRemaining(value -> result.add(value));
        return result;
    }

    public static <T> void runOrderedTests(Iterable<T> spliterable) {
        List<T> elements = toList(spliterable.spliterator());
        assertEquals("Ordering should be consistent", elements, toList(spliterable.spliterator()));

        // NOTE: This would fail for some Collections because of b/34757089:
        // assertTrue(spliterable.spliterator().hasCharacteristics(ORDERED));

        if (spliterable instanceof Collection) {
            assertEquals("ORDERED Spliterator must be consistent with Iterator: "
                            + spliterable.getClass(), elements, toList(spliterable.iterator()));
        }

        boolean isEmpty = !spliterable.iterator().hasNext();

        Spliterator<T> sa = spliterable.spliterator();
        Spliterator<T> sb = spliterable.spliterator();
        Spliterator<T> saSplit = sa.trySplit();
        Spliterator<T> sbSplit = sb.trySplit();
        // trySplit() may always return null, but is only required to when empty
        if (isEmpty) {
            assertNull(saSplit);
            assertNull(sbSplit);
        } else {
            // A non-empty Iterable may still return null from trySplit();
            // if it does, then the un-split parent spliterators (sa, sb) must
            // each still contain all of the elements. Regardless of whether
            // the split was successful, sa and sb must behave the consistently
            // with each other since they came from the same Iterable.
            if (saSplit != null) {
                assertEquals(toList(saSplit), toList(sbSplit));
                assertEquals(toList(sa), toList(sb));
            } else {
                assertEquals(elements, toList(sa));
                assertEquals(elements, toList(sb));
            }
        }
    }

    /**
     * Checks that the specified SIZED Spliterator reports containing the
     * specified number of elements.
     */
    public static <T> void runSizedTests(Spliterator<T> spliterator, int expectedSize) {
        assertHasCharacteristics(SIZED, spliterator);
        assertEquals(expectedSize, spliterator.estimateSize());
        assertEquals(expectedSize, spliterator.getExactSizeIfKnown());
    }

    public static <T> void runSizedTests(Iterable<T> spliterable, int expectedSize) {
        runSizedTests(spliterable.spliterator(), expectedSize);
    }

    /**
     * Checks that the specified Spliterator and its {@link Spliterator#trySplit()
     * children} are SIZED and SUBSIZED and report containing the specified number
     * of elements.
     */
    public static <T> void runSubSizedTests(Spliterator<T> spliterator, int expectedSize) {
        assertHasCharacteristics(SIZED | SUBSIZED, spliterator);
        assertEquals(expectedSize, spliterator.estimateSize());
        assertEquals(expectedSize, spliterator.getExactSizeIfKnown());

        Spliterator<T> child = spliterator.trySplit();
        assertHasCharacteristics(SIZED | SUBSIZED, spliterator);
        if (expectedSize == 0) {
            assertNull(child);
            assertEquals(expectedSize, spliterator.estimateSize());
            assertEquals(expectedSize, spliterator.getExactSizeIfKnown());
        } else {
            assertHasCharacteristics(SIZED | SUBSIZED, child);
            assertEquals(expectedSize, spliterator.estimateSize() + child.estimateSize());
            assertEquals(expectedSize,
                    spliterator.getExactSizeIfKnown() + child.getExactSizeIfKnown());
        }
    }

    public static <T> void runSubSizedTests(Iterable<T> spliterable, int expectedSize) {
        runSubSizedTests(spliterable.spliterator(), expectedSize);
    }

    public static <T> void runDistinctTests(Iterable<T> spliterable) {
        HashSet<T> distinct = new HashSet<>();
        ArrayList<T> allElements = new ArrayList<>();

        Spliterator<T> spliterator = spliterable.spliterator();
        Spliterator<T> split1 = spliterator.trySplit();

        // First test that iterating via the spliterator using forEachRemaining
        // yields distinct elements.
        spliterator.forEachRemaining(value -> { distinct.add(value); allElements.add(value); });
        // trySplit() may return null, even when non-empty
        if (split1 != null) {
            split1.forEachRemaining(value -> { distinct.add(value); allElements.add(value); });
        }
        assertEquals(distinct.size(), allElements.size());

        distinct.clear();
        allElements.clear();
        spliterator = spliterable.spliterator();
        split1 = spliterator.trySplit();

        // Then test whether using tryAdvance yields the same results.
        while (spliterator.tryAdvance(value -> { distinct.add(value); allElements.add(value); })) {
        }

        // trySplit() may return null, even when non-empty
        if (split1 != null) {
            while (split1.tryAdvance(value -> { distinct.add(value); allElements.add(value); })) {
            }
        }

        assertEquals(distinct.size(), allElements.size());
    }

    public static <T> void runSortedTests(Iterable<T> spliterable, Comparator<T> comparator) {
        Spliterator<T> spliterator = spliterable.spliterator();
        Spliterator<T> split1 = spliterator.trySplit();

        ArrayList<T> elements = new ArrayList<>();
        spliterator.forEachRemaining(value -> elements.add(value));

        ArrayList<T> sortedElements = new ArrayList<>(elements);
        Collections.sort(sortedElements, comparator);
        assertEquals(elements, sortedElements);

        elements.clear();

        split1.forEachRemaining(value -> elements.add(value));
        sortedElements = new ArrayList<>(elements);
        Collections.sort(sortedElements, comparator);
        assertEquals(elements, sortedElements);
    }

    public static <T extends Comparable<T>> void runSortedTests(Iterable<T> spliterable) {
        runSortedTests(spliterable, T::compareTo);
    }

    public static void assertHasCharacteristics(int expectedCharacteristics,
            Spliterator<?> spliterator) {
        int actualCharacteristics = spliterator.characteristics();
        String msg = String.format(Locale.US,
                "Expected expectedCharacteristics containing 0x%x, got 0x%x",
                expectedCharacteristics, actualCharacteristics);
        assertTrue(msg, spliterator.hasCharacteristics(expectedCharacteristics));
    }
}
