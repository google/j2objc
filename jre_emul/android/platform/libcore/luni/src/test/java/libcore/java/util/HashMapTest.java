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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeMap;

public class HashMapTest extends junit.framework.TestCase {

    public void test_getOrDefault() {
        MapDefaultMethodTester.test_getOrDefault(new HashMap<>(), true /*acceptsNullKey*/,
                true /*acceptsNullValue*/, true /*getAcceptsAnyObject*/);
    }

    public void test_forEach() {
        MapDefaultMethodTester.test_forEach(new HashMap<>());
    }

    public void test_putIfAbsent() {
        MapDefaultMethodTester.test_putIfAbsent(new HashMap<>(), true /*acceptsNullKey*/,
                true /*acceptsNullValue*/);
    }

    public void test_remove() {
        MapDefaultMethodTester
                .test_remove(new HashMap<>(), true /*acceptsNullKey*/, true /*acceptsNullValue*/);
    }

    public void test_replace$K$V$V() {
        MapDefaultMethodTester
                .test_replace$K$V$V(new HashMap<>(), true /*acceptsNullKey*/,
                        true /*acceptsNullValue*/);
    }

    public void test_replace$K$V() {
        MapDefaultMethodTester.test_replace$K$V(new HashMap<>(), true /*acceptsNullKey*/,
                true /*acceptsNullValue*/);
    }

    public void test_computeIfAbsent() {
        MapDefaultMethodTester.test_computeIfAbsent(new HashMap<>(), true /*acceptsNullKey*/,
                true /*acceptsNullValue*/);
    }

    public void test_computeIfPresent() {
        MapDefaultMethodTester.test_computeIfPresent(new HashMap<>(), true /*acceptsNullKey*/);
    }

    public void test_compute() {
        MapDefaultMethodTester
                .test_compute(new HashMap<>(), true /*acceptsNullKey*/);
    }

    public void test_merge() {
        MapDefaultMethodTester
                .test_merge(new HashMap<>(), true /*acceptsNullKey*/);
    }

    /* J2ObjC: these tests depend on the implementation of the String hash function. For example:
     * "a".hashCode() returns 97, when transpiled returns 1062.
     * "b".hashCode() returns 98, when transpiled returns 1065.
    public void test_spliterator_keySet() {
        Map<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        m.put("d", 4);
        m.put("e", 5);
        m.put("f", 6);
        m.put("g", 7);
        m.put("h", 8);
        m.put("i", 9);
        m.put("j", 10);
        ArrayList<String> expectedKeys = new ArrayList<>(
                Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"));
        Set<String> keys = m.keySet();
        SpliteratorTester.runBasicIterationTests(keys.spliterator(), expectedKeys);
        SpliteratorTester.runBasicSplitTests(keys, expectedKeys);
        SpliteratorTester.testSpliteratorNPE(keys.spliterator());
        SpliteratorTester.runSizedTests(keys.spliterator(), 10);
        assertEquals(Spliterator.DISTINCT | Spliterator.SIZED,
                keys.spliterator().characteristics());
        SpliteratorTester.assertSupportsTrySplit(keys);
    }

    public void test_spliterator_values() {
        Map<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        m.put("d", 4);
        m.put("e", 5);
        m.put("f", 6);
        m.put("g", 7);
        m.put("h", 8);
        m.put("i", 9);
        m.put("j", 10);
        ArrayList<Integer> expectedValues = new ArrayList<>(
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        );
        Collection<Integer> values = m.values();
        SpliteratorTester.runBasicIterationTests(values.spliterator(), expectedValues);
        SpliteratorTester.runBasicSplitTests(values, expectedValues);
        SpliteratorTester.testSpliteratorNPE(values.spliterator());
        SpliteratorTester.runSizedTests(values, 10);
        assertEquals(Spliterator.SIZED, values.spliterator().characteristics());
        SpliteratorTester.assertSupportsTrySplit(values);
    }*/

    public void test_spliterator_entrySet() {
        MapDefaultMethodTester.test_entrySet_spliterator_unordered(new HashMap<>());

        Map<String, Integer> m = new HashMap<>(Collections.singletonMap("key", 42));
        assertEquals(Spliterator.DISTINCT | Spliterator.SIZED,
                m.entrySet().spliterator().characteristics());
    }

    /**
     * Checks that {@code HashMap.entrySet().spliterator().trySplit()}
     * estimates half of the parents' estimate (rounded down, which
     * can be an underestimate) but is not itself SIZED.
     *
     * These assertions are still stronger than what the documentation
     * guarantees since un-SIZED Spliterators' size estimates may be off by
     * an arbitrary amount.
     */
    public void test_entrySet_subsizeEstimates() {
        Map<String, String> m = new HashMap<>();
        assertNull(m.entrySet().spliterator().trySplit());
        // For the empty map, the estimates are exact
        assertEquals(0, m.entrySet().spliterator().estimateSize());
        assertEquals(0, m.entrySet().spliterator().getExactSizeIfKnown());

        m.put("key1", "value1");
        assertSubsizeEstimate(m.entrySet().spliterator(), 0);
        m.put("key2", "value2");
        assertSubsizeEstimate(m.entrySet().spliterator(), 1);
        m.put("key3", "value3");
        m.put("key4", "value4");
        m.put("key5", "value5");
        m.put("key6", "value6");
        m.put("key7", "value7");
        m.put("key8", "value8");
        assertSubsizeEstimate(m.entrySet().spliterator(), 4);

        m.put("key9", "value9");
        assertSubsizeEstimate(m.entrySet().spliterator(), 4);
        assertFalse(m.entrySet().spliterator().trySplit().hasCharacteristics(Spliterator.SIZED));
    }

    /**
     * Checks that HashMap.entrySet()'s spliterator halfs its estimate (rounding down)
     * for each split, even though this estimate may be inaccurate.
     */
    public void test_entrySet_subsizeEstimates_recursive() {
        Map<Integer, String> m = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            m.put(i, "value");
        }
        Set<Map.Entry<Integer, String>> entries = m.entrySet();
        // Recursive splitting - HashMap will estimate the size halving each split, rounding down.
        assertSubsizeEstimate(entries.spliterator(), 50);
        assertSubsizeEstimate(entries.spliterator().trySplit(), 25);
        assertSubsizeEstimate(entries.spliterator().trySplit().trySplit(), 12);
        assertSubsizeEstimate(entries.spliterator().trySplit().trySplit().trySplit(), 6);
        assertSubsizeEstimate(entries.spliterator().trySplit().trySplit().trySplit().trySplit(), 3);
        assertSubsizeEstimate(
                entries.spliterator().trySplit().trySplit().trySplit().trySplit().trySplit(), 1);
        assertSubsizeEstimate(entries.spliterator().trySplit().trySplit().trySplit().trySplit()
                .trySplit().trySplit(), 0);
    }

    /**
     * Checks that HashMap.EntryIterator is SIZED but not SUBSIZED.
     */
    public void test_entrySet_spliterator_sizedButNotSubsized() {
        Map<String, String> m = new HashMap<>();
        assertTrue(m.entrySet().spliterator().hasCharacteristics(Spliterator.SIZED));
        assertFalse(m.entrySet().spliterator().hasCharacteristics(Spliterator.SUBSIZED));
        m.put("key1", "value1");
        m.put("key2", "value2");
        assertTrue(m.entrySet().spliterator().hasCharacteristics(Spliterator.SIZED));
        assertFalse(m.entrySet().spliterator().hasCharacteristics(Spliterator.SUBSIZED));
        Spliterator<Map.Entry<String, String>> parent = m.entrySet().spliterator();
        Spliterator<Map.Entry<String, String>> child = parent.trySplit();
        assertFalse(parent.hasCharacteristics(Spliterator.SIZED));
        assertFalse(child.hasCharacteristics(Spliterator.SIZED));
        assertFalse(parent.hasCharacteristics(Spliterator.SUBSIZED));
        assertFalse(child.hasCharacteristics(Spliterator.SUBSIZED));
    }

    /**
     * Tests that the given spliterator can be trySplit(), resulting in children that each
     * estimate the specified size.
     */
    private static<T> void assertSubsizeEstimate(Spliterator<T> spliterator,
            long expectedEstimate) {
        Spliterator<T> child = spliterator.trySplit();
        assertNotNull(child);
        assertEquals(expectedEstimate, spliterator.estimateSize());
        assertEquals(expectedEstimate, child.estimateSize());
    }

    public void test_replaceAll() throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        map.replaceAll((k, v) -> k + v);
        assertEquals("one1", map.get("one"));
        assertEquals("two2", map.get("two"));
        assertEquals("three3", map.get("three"));
        assertEquals(3, map.size());

        try {
            map.replaceAll((k, v) -> {
                map.put("foo1", v);
                return v;
            });
            fail();
        } catch(ConcurrentModificationException expected) {}

        try {
            map.replaceAll(null);
            fail();
        } catch(NullPointerException expected) {}
    }
}
