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
import java.lang.Iterable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkedHashMapTest extends junit.framework.TestCase {

    public void test_getOrDefault() {
        MapDefaultMethodTester
                .test_getOrDefault(new LinkedHashMap<>(), true /*acceptsNullKey*/,
                        true /*acceptsNullValue*/, true /*getAcceptsAnyObject*/);

        // Test for access order
        Map<String, String> m = new LinkedHashMap<String, String>(8, .75f, true);
        m.put("key", "value");
        m.put("key1", "value1");
        m.put("key2", "value2");
        m.getOrDefault("key1", "value");
        Map.Entry<String, String> newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key1", newest.getKey());
        assertEquals("value1", newest.getValue());
    }

    public void test_forEach() {
        MapDefaultMethodTester.test_forEach(new LinkedHashMap<>());
    }

    public void test_putIfAbsent() {
        MapDefaultMethodTester.test_putIfAbsent(new LinkedHashMap<>(), true /*acceptsNullKey*/,
                true /*acceptsNullValue*/);

        // Test for access order
        Map<String, String> m = new LinkedHashMap<String, String>(8, .75f, true);
        m.putIfAbsent("key", "value");
        m.putIfAbsent("key1", "value1");
        m.putIfAbsent("key2", "value2");
        Map.Entry<String, String> newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key2", newest.getKey());
        assertEquals("value2", newest.getValue());

        // for existed key
        m.putIfAbsent("key1", "value1");
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key1", newest.getKey());
        assertEquals("value1", newest.getValue());
    }

    public void test_remove() {
        MapDefaultMethodTester.test_remove(new LinkedHashMap<>(), true /*acceptsNullKey*/,
                true /*acceptsNullValue*/);
    }

    public void test_replace$K$V$V() {
        MapDefaultMethodTester.
                test_replace$K$V$V(new LinkedHashMap<>(), true /*acceptsNullKey*/,
                        true /*acceptsNullValue*/);

        // Test for access order
        Map<String, String> m = new LinkedHashMap<>(8, .75f, true  /*accessOrder*/);
        m.put("key", "value");
        m.put("key1", "value1");
        m.put("key2", "value2");
        m.replace("key1", "value1", "value2");
        Map.Entry<String, String> newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key1", newest.getKey());
        assertEquals("value2", newest.getValue());

        // for wrong pair of key and value, last accessed node should
        // not change
        m.replace("key2", "value1", "value3");
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key1", newest.getKey());
        assertEquals("value2", newest.getValue());
    }

    public void test_replace$K$V() {
        MapDefaultMethodTester.test_replace$K$V(new LinkedHashMap<>(), true /*acceptsNullKey*/,
                true /*acceptsNullValue*/);

        // Test for access order
        Map<String, String> m = new LinkedHashMap<>(8, .75f, true  /*accessOrder*/);
        m.put("key", "value");
        m.put("key1", "value1");
        m.put("key2", "value2");
        m.replace("key1", "value2");
        Map.Entry<String, String> newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key1", newest.getKey());
        assertEquals("value2", newest.getValue());
    }

    public void test_computeIfAbsent() {
        MapDefaultMethodTester.test_computeIfAbsent(new LinkedHashMap<>(), true /*acceptsNullKey*/,
                true /*acceptsNullValue*/);

        // Test for access order
        Map<String, String> m = new LinkedHashMap<>(8, .75f, true  /*accessOrder*/);
        m.put("key", "value");
        m.put("key1", "value1");
        m.put("key2", "value2");
        m.computeIfAbsent("key1", (k) -> "value3");
        Map.Entry<String, String> newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key1", newest.getKey());
        assertEquals("value1", newest.getValue());

        // When value is absent
        m.computeIfAbsent("key4", (k) -> "value3");
        newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key4", newest.getKey());
        assertEquals("value3", newest.getValue());
    }

    public void test_computeIfPresent() {
        MapDefaultMethodTester.test_computeIfPresent(new LinkedHashMap<>(), true /*acceptsNullKey*/);

        // Test for access order
        Map<String, String> m = new LinkedHashMap<>(8, .75f, true  /*accessOrder*/);
        m.put("key", "value");
        m.put("key1", "value1");
        m.put("key2", "value2");
        m.computeIfPresent("key1", (k, v) -> "value3");
        Map.Entry<String, String> newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key1", newest.getKey());
        assertEquals("value3", newest.getValue());
    }

    public void test_compute() {
        MapDefaultMethodTester.test_compute(new LinkedHashMap<>(), true /*acceptsNullKey*/);

        // Test for access order
        Map<String, String> m = new LinkedHashMap<>(8, .75f, true  /*accessOrder*/);
        m.put("key", "value");
        m.put("key1", "value1");
        m.put("key2", "value2");
        m.compute("key1", (k, v) -> "value3");
        Map.Entry<String, String> newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key1", newest.getKey());
        assertEquals("value3", newest.getValue());

        m.compute("key4", (k, v) -> "value4");
        newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key4", newest.getKey());
        assertEquals("value4", newest.getValue());
    }

    public void test_merge() {
        MapDefaultMethodTester.test_merge(new LinkedHashMap<>(), true /*acceptsNullKey*/);

        // Test for access order
        Map<String, String> m = new LinkedHashMap<>(8, .75f, true  /*accessOrder*/);
        m.put("key", "value");
        m.put("key1", "value1");
        m.put("key2", "value2");
        m.merge("key1", "value3", (k, v) -> "value3");
        Map.Entry<String, String> newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("key1", newest.getKey());
        assertEquals("value3", newest.getValue());
    }

    // This tests the behaviour is consistent with the RI.
    // This behaviour is NOT consistent with earlier Android releases up to
    // and including Android N, see http://b/27929722
    public void test_removeEldestEntry() {
        final AtomicBoolean removeEldestEntryReturnValue = new AtomicBoolean(false);
        final AtomicInteger removeEldestEntryCallCount = new AtomicInteger(0);
        LinkedHashMap<String, String> m = new LinkedHashMap<String, String>() {
            @Override
            protected boolean removeEldestEntry(Entry eldest) {
                int size = size();
                assertEquals(size, iterableSize(entrySet()));
                assertEquals(size, iterableSize(keySet()));
                assertEquals(size, iterableSize(values()));
                assertEquals(size, removeEldestEntryCallCount.get() + 1);
                removeEldestEntryCallCount.incrementAndGet();
                return removeEldestEntryReturnValue.get();
            }
        };

        assertEquals(0, removeEldestEntryCallCount.get());
        m.put("foo", "bar");
        assertEquals(1, removeEldestEntryCallCount.get());
        m.put("baz", "quux");
        assertEquals(2, removeEldestEntryCallCount.get());

        removeEldestEntryReturnValue.set(true);
        m.put("foob", "faab");
        assertEquals(3, removeEldestEntryCallCount.get());
        assertEquals(2, m.size());
        assertFalse(m.containsKey("foo"));
    }

    private static<E> int iterableSize(Iterable<E> iterable) {
        int result = 0;
        for (E element : iterable) {
            result++;
        }
        return result;
    }

    public void test_replaceAll() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
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

    public void test_eldest_empty() {
        LinkedHashMap<String, String> emptyMap = createMap();
        assertNull(eldest(emptyMap));
    }

    public void test_eldest_nonempty() {
        assertEntry("key", "value", eldest(createMap("key", "value")));
        assertEntry("A", "1", eldest(createMap("A", "1", "B", "2", "C", "3")));
        assertEntry("A", "4", eldest(createMap("A", "1", "B", "2", "C", "3", "A", "4")));
        assertEntry("A", "4", eldest(createMap("A", "1", "B", "2", "C", "3", "A", "4", "D", "5")));
    }

    public void test_eldest_compatibleWithIterationOrder() {
        check_eldest_comparibleWithIterationOrder(createMap());
        check_eldest_comparibleWithIterationOrder(createMap("key", "value"));
        check_eldest_comparibleWithIterationOrder(createMap("A", "1", "B", "2"));
        check_eldest_comparibleWithIterationOrder(createMap("A", "1", "B", "2", "A", "3"));
        check_eldest_comparibleWithIterationOrder(createMap("A", "1", "A", "2", "A", "3"));

        Random random = new Random(31337); // arbitrary
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < 8000; i++) {
            m.put(String.valueOf(random.nextInt(4000)), String.valueOf(random.nextDouble()));
        }
        check_eldest_comparibleWithIterationOrder(m);
    }

    private void check_eldest_comparibleWithIterationOrder(LinkedHashMap<?, ?> map) {
        Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry<?, ?> expected = it.next();
            Object expectedKey = expected.getKey();
            Object expectedValue = expected.getValue();
            assertEntry(expectedKey, expectedValue, eldest(map));
        } else {
            assertNull(eldest(map));
        }
    }

    /**
     * Check that {@code LinkedHashMap.Entry} compiles and refers to
     * {@link java.util.Map.Entry}, which is required for source
     * compatibility with earlier versions of Android.
     */
    public void test_entryCompatibility_compiletime() {
        assertEquals(Map.Entry.class, LinkedHashMap.Entry.class);
    }

    /**
     * Checks that there is no nested class named 'Entry' in LinkedHashMap.
     * If {@link #test_entryCompatibility_compiletime()} passes but
     * this test fails, then the test was probably compiled against a
     * version of LinkedHashMap that does not have a nested Entry class,
     * but run against a version that does.
     */
    public void test_entryCompatibility_runtime() {
        String forbiddenClassName = "java.util.LinkedHashMap$Entry";
        try {
            Class.forName(forbiddenClassName);
            fail("Class " + forbiddenClassName + " should not exist");
        } catch (ClassNotFoundException expected) {
        }
    }

    public void test_spliterator_keySet() {
        Map<String, Integer> m = new LinkedHashMap<>();
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
        SpliteratorTester.runOrderedTests(keys);
        SpliteratorTester.runSizedTests(keys.spliterator(), 10);
        SpliteratorTester.runSubSizedTests(keys.spliterator(), 10);
        assertEquals(
                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SIZED
                        | Spliterator.SUBSIZED,
                keys.spliterator().characteristics());
        SpliteratorTester.assertSupportsTrySplit(keys);
    }

    public void test_spliterator_values() {
        Map<String, Integer> m = new LinkedHashMap<>();
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
        SpliteratorTester.runBasicIterationTests(
                values.spliterator(), expectedValues);
        SpliteratorTester.runBasicSplitTests(values, expectedValues);
        SpliteratorTester.testSpliteratorNPE(values.spliterator());
        SpliteratorTester.runOrderedTests(values);
        SpliteratorTester.runSizedTests(values, 10);
        SpliteratorTester.runSubSizedTests(values, 10);
        assertEquals(Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED,
                values.spliterator().characteristics());
        SpliteratorTester.assertSupportsTrySplit(values);
    }

    public void test_spliterator_entrySet() {
        MapDefaultMethodTester
                .test_entrySet_spliterator_unordered(new LinkedHashMap<>());

        Map<String, Integer> m = new LinkedHashMap<>(Collections.singletonMap("key", 23));
        assertEquals(
                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SIZED |
                        Spliterator.SUBSIZED,
                m.entrySet().spliterator().characteristics());
    }

    private static Map.Entry<?, ?> eldest(LinkedHashMap<?,?> map) {
        // Should be the same as: return (map.isEmpty()) ? null : map.entrySet().iterator().next();
        return map.eldest();
    }

    private static void assertEntry(Object key, Object value, Map.Entry<?, ?> entry) {
        String msg = String.format(Locale.US, "Expected (%s, %s), got (%s, %s)",
                key, value, entry.getKey(), entry.getValue());
        boolean equal = Objects.equals(key, entry.getKey())
                && Objects.equals(value, entry.getValue());
        if (!equal) {
            fail(msg);
        }
    }

    private static<T> LinkedHashMap<T, T> createMap(T... keysAndValues) {
        assertEquals(0, keysAndValues.length % 2);
        LinkedHashMap<T, T> result = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            result.put(keysAndValues[i], keysAndValues[i+1]);
        }
        return result;
    }

}
