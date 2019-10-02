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

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertSame;

public class MapDefaultMethodTester {

    private MapDefaultMethodTester() {}

    /**
     * @param getAcceptsAnyObject whether get() and getOrDefault() allow any
     *        nonnull key Object, returning false rather than throwing
     *        ClassCastException if the key is inappropriate for the map.
     */
    public static void test_getOrDefault(Map<String, String> m, boolean acceptsNullKey,
            boolean acceptsNullValue, boolean getAcceptsAnyObject) {
        // absent key
        if (acceptsNullKey) {
            checkGetOrDefault("default", m, null, "default");
            if (acceptsNullValue) {
                checkGetOrDefault(null, m, null, null);
            }
        }
        m.put("key", "value");
        if (acceptsNullValue) {
            checkGetOrDefault(null, m, "absentkey", null);
            if (acceptsNullKey) {
                checkGetOrDefault(null, m, null, null);
            }
        }
        checkGetOrDefault("default", m, "absentkey", "default");
        m.put("anotherkey", "anothervalue");
        checkGetOrDefault("default", m, "absentkey", "default");

        // absent key - inappropriate type
        boolean getAcceptedObject;
        try {
            assertSame("default", m.getOrDefault(new Object(), "default"));
            getAcceptedObject = true;
        } catch (ClassCastException e) {
            getAcceptedObject = false;
        }
        assertEquals(getAcceptsAnyObject, getAcceptedObject);

        // present key
        checkGetOrDefault("value", m, "key", "default");
        checkGetOrDefault("value", m, "key", new String("value"));

        // null value
        if (acceptsNullValue) {
            m.put("keyWithNullValue", null);
            checkGetOrDefault(null, m, "keyWithNullValue", "default");
        }

        // null key
        if (acceptsNullKey) {
            m.put(null, "valueForNullKey");
            checkGetOrDefault("valueForNullKey", m, null, "valueForNullKey");
        }
    }

    /**
     * Checks that the value returned by {@link LinkedHashMap#getOrDefault(Object, Object)}
     * is consistent with various other ways getOrDefault() could be computed.
     */
    private static<K, V> void checkGetOrDefault(
            V expected, Map<K, V> map, K key, V defaultValue) {
        V actual = map.getOrDefault(key, defaultValue);
        assertSame(expected, actual);

        assertSame(expected, getOrDefault_hashMap(map, key, defaultValue));
        assertSame(expected, getOrDefault_optimizeForPresent(map, key, defaultValue));
        assertSame(expected, getOrDefault_optimizeForAbsent(map, key, defaultValue));
    }

    /** Implementation of getOrDefault() on top of HashMap.getOrDefault(). */
    private static<K, V> V getOrDefault_hashMap(Map<K, V> map, K key, V defaultValue) {
        return new HashMap<>(map).getOrDefault(key, defaultValue);
    }

    /**
     * Implementation of Map.getOrDefault() that only needs one lookup if the key is
     * absent.
     */
    private static<K, V> V getOrDefault_optimizeForAbsent(Map<K, V> map, K key, V defaultValue) {
        return map.containsKey(key) ? map.get(key) : defaultValue;
    }

    /**
     *  Implementation of  getOrDefault() that only needs one lookup if the key is
     *  present and not mapped to null.
     */
    private static<K, V> V getOrDefault_optimizeForPresent(Map<K, V> map, K key, V defaultValue) {
        V result = map.get(key);
        if (result == null && !map.containsKey(key)) {
            result = defaultValue;
        }
        return result;
    }

    public static void test_forEach(Map<Integer, Double> m) {
        Map<Integer, Double> replica = new HashMap<>();
        m.put(1, 10.0);
        m.put(2, 20.0);
        m.forEach(replica::put);
        assertEquals(10.0, replica.get(1));
        assertEquals(20.0, replica.get(2));
        assertEquals(2, replica.size());

        // Null pointer exception for empty function
        try {
            m.forEach(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public static void test_putIfAbsent(Map<Integer, Double> m, boolean acceptsNullKey,
            boolean acceptsNullValue) {
        // For unmapped key
        assertNull(m.putIfAbsent(1, 1.0));
        assertEquals(1.0, m.getOrDefault(1, -1.0));

        // For already mapped key
        m.put(1, 1.0);
        assertEquals(1.0, m.putIfAbsent(1, 2.0));
        assertEquals(1.0, m.getOrDefault(1, -1.0));

        if (acceptsNullValue) {
            m.put(1, null);
            assertNull(m.putIfAbsent(1, 1.0));
            assertEquals(1.0, m.getOrDefault(1, -1.0));

            m.remove(1);
            assertNull(m.putIfAbsent(1, null));
            assertNull(m.getOrDefault(1, -1.0));
        } else {
            try {
                m.remove(1);
                m.putIfAbsent(1, null);
                fail();
            } catch (NullPointerException expected) {
            }
        }

        if (acceptsNullKey) {
            m.put(null, 1.0);
            assertEquals(1.0, m.putIfAbsent(null, 5.0));
            assertEquals(1.0, m.getOrDefault(null, -1.0));
        } else {
            try {
                m.putIfAbsent(null, 1.0);
                fail();
            } catch (NullPointerException expected) {
            }
        }
    }

    public static void test_remove(Map<Integer, Double> m, boolean acceptsNullKey,
            boolean acceptsNullValue) {
        // For unmapped key
        assertFalse(m.remove(1, 1.0));

        // mapped key with the wrong value
        m.put(1, 1.0);
        assertFalse(m.remove(1, 2.0));
        assertTrue(m.containsKey(1));

        // mapped key with the correct value
        assertTrue(m.remove(1, 1.0));
        assertFalse(m.containsKey(1));

        // Check for null key and value
        if (acceptsNullValue) {
            m.put(1, null);
            assertTrue(m.remove(1, null));
            assertFalse(m.containsKey(1));
        }

        if (acceptsNullKey) {
            m.put(null, 1.0);
            assertTrue(m.remove(null, 1.0));
            assertFalse(m.containsKey(null));
        } else {
            try {
                m.remove(null, 1.0);
                fail();
            } catch (NullPointerException expected) {}
        }
    }

    public static void test_replace$K$V$V(Map<Integer, Double> m, boolean acceptsNullKey,
            boolean acceptsNullValue) {
        // For unmapped key
        assertFalse(m.replace(1, 1.0, 2.0));
        assertFalse(m.containsKey(1));

        // For mapped key and wrong value
        m.put(1, 1.0);
        assertFalse(m.replace(1, 2.0, 2.0));
        assertEquals(1.0, m.getOrDefault(1, -1.0));

        // For mapped key and correct value
        m.put(1, 1.0);
        assertTrue(m.replace(1, 1.0, 2.0));
        assertEquals(2.0, m.getOrDefault(1, -1.0));

        if (acceptsNullKey) {
            m.put(null, 1.0);
            assertTrue(m.replace(null, 1.0, 2.0));
            assertEquals(2.0, m.getOrDefault(null, -1.0));
        } else {
            try {
                m.replace(null, 1.0, 2.0);
                fail();
            } catch (NullPointerException expected) {}
        }

        if (acceptsNullValue) {
            m.put(1, null);
            assertTrue(m.replace(1, null, 1.0));
            assertEquals(1.0, m.getOrDefault(1, -1.0));
        } else {
            try {
                m.put(1, 1.0);
                m.replace(1, 1.0, null);
                fail();
            } catch (NullPointerException expected) {}
        }
    }

    public static void test_replace$K$V(Map<Integer, Double> m, boolean acceptsNullKey,
            boolean acceptsNullValue) {
        // For unmapped key
        assertNull(m.replace(1, 1.0));
        assertFalse(m.containsKey(1));

        // For already mapped key
        m.put(1, 1.0);
        assertEquals(1.0, m.replace(1, 2.0));
        assertEquals(2.0, m.getOrDefault(1, -1.0));

        if (acceptsNullValue) {
            m.put(1, 1.0);
            assertEquals(1.0, m.replace(1, null));
            assertNull(m.getOrDefault(1, -1.0));
        } else {
            try {
                m.put(1, 5.0);
                m.replace(1, null);
                fail();
            } catch (NullPointerException expected) {}
        }

        if (acceptsNullKey) {
            m.put(null, 1.0);
            assertEquals(1.0, m.replace(null, 2.0));
            assertEquals(2.0, m.getOrDefault(null, -1.0));
        } else {
            try {
                m.replace(null, 5.0);
                fail();
            } catch (NullPointerException expected) {}
        }
    }

    public static void test_computeIfAbsent(Map<Integer, Double> m, boolean acceptsNullKey,
            boolean acceptsNullValue) {
        // For unmapped key
        assertEquals(5.0, m.computeIfAbsent(1, (k) -> 5.0 * k));
        assertEquals(5.0, m.getOrDefault(1, -1.0));

        // For already mapped key
        m.put(1, 1.0);
        assertEquals(1.0, m.computeIfAbsent(1, k -> 6.0 * k));
        assertEquals(1.0, m.getOrDefault(1, -1.0));

        // If mapping function returns null for a mapped key
        m.put(1, 1.0);
        assertEquals(1.0, m.computeIfAbsent(1, k -> null));
        assertEquals(1.0, m.getOrDefault(1, -1.0));

        // If mapping function returns null for an unmapped key
        assertNull(m.computeIfAbsent(100, k-> null));

        // If mapping function is null
        try {
            m.computeIfAbsent(1, null);
            fail();
        } catch (NullPointerException expected) {}

        if (acceptsNullValue) {
            // For key associated to a null value
            m.put(1, null);
            assertEquals(1.0, m.computeIfAbsent(1, k -> 1.0));
            assertEquals(1.0, m.getOrDefault(1, -1.0));
        }

        if (acceptsNullKey) {
            m.put(null, 1.0);
            assertEquals(1.0, m.computeIfAbsent(null, (k) -> 5.0 * k));
        } else {
            try {
                m.computeIfAbsent(null, k -> 5.0);
                fail();
            } catch (NullPointerException expected) {}
        }
    }

    public static void test_computeIfPresent(Map<Integer, Double> m, boolean acceptsNullKey) {
        // For an unmapped key
        assertNull(m.computeIfPresent(1, (k, v) -> 5.0 * k + v));

        // For a mapped key
        m.put(1, 5.0);
        assertEquals(11.0, m.computeIfPresent(1, (k, v) -> 6.0 * k + v));
        assertEquals(11.0, m.getOrDefault(1, -1.0));

        // If the remapping function returns null
        assertNull(m.computeIfPresent(1, (k, v) -> null));
        assertFalse(m.containsKey(1));

        // If the remapping function is null
        try {
            m.computeIfPresent(1, null);
            fail();
        } catch (NullPointerException expected) {}

        if (acceptsNullKey) {
            m.put(null, 1.0);
            assertEquals(1.0, m.computeIfPresent(null, (k, v) -> v));
        } else {
            try {
                m.computeIfPresent(null, (k, v) -> 5.0);
                fail();
            } catch (NullPointerException expected) {}
        }
    }

    public static void test_compute(Map<Integer, Double> m, boolean acceptsNullKey) {
        // For unmapped key
        assertEquals(5.0, m.compute(1, (k, v) -> 5.0));
        assertEquals(5.0, m.getOrDefault(1, -1.0));

        // For already mapped key
        m.put(1, 10.0);
        assertEquals(11.0, m.compute(1, (k, v) -> k + v));
        assertEquals(11.0, m.getOrDefault(1, -1.0));

        // If the remapping function returns null
        assertNull(m.compute(1, (k, v) -> null));
        assertFalse(m.containsKey(1));

        // If the remapping function is null
        try {
            m.compute(1, null);
            fail();
        } catch (NullPointerException expected) {}

        if (acceptsNullKey) {
            assertEquals(10.0, m.compute(null, (k, v) -> 10.0));
            assertEquals(10.0, m.getOrDefault(null, -1.0));
        } else {
            try {
                m.compute(null, (k, v) -> 5.0);
                fail();
            } catch (NullPointerException expected) {
            }
        }
    }

    public static void test_merge(Map<Integer, Double> m, boolean acceptsNullKey) {
        // Checking for unmapped key
        assertEquals(10.0, m.merge(1, 10.0, (v1, v2) -> v2));
        assertEquals(10.0, m.getOrDefault(1, -1.0));

        // Checking for already mapped key
        m.put(1, 10.0);
        assertEquals(25.0, m.merge(1, 15.0, (v1, v2) -> v1 + v2));
        assertEquals(25.0, m.getOrDefault(1, -1.0));

        // If lambda function returns null
        m.put(1, 10.0);
        m.merge(1, 10.0, (k, v) -> null);
        assertFalse(m.containsKey(1));

        // If the remapping function is null
        try {
            m.merge(1, 5.0, null);
            fail();
        } catch (NullPointerException expected) {}

        if (acceptsNullKey) {
            m.put(null, 1.0);
            assertEquals(15.0, m.merge(null, 15.0, (v1, v2) -> v2));
            assertEquals(15.0, m.getOrDefault(null, -1.0));
        } else {
            try {
                m.merge(null, 15.0, (v1, v2) -> v2);
                fail();
            } catch (NullPointerException expected) {}
        }
    }

    public static void test_entrySet_spliterator_unordered(Map<String, String> m) {
        checkEntrySpliterator(m);
        m.put("key", "value");
        checkEntrySpliterator(m, "key", "value");
        m.put("key2", "value2");
        checkEntrySpliterator(m, "key", "value", "key2", "value2");
        m.put("key", "newValue");
        checkEntrySpliterator(m, "key", "newValue", "key2", "value2");
        m.remove("key2");
        checkEntrySpliterator(m, "key", "newValue");
        m.clear();

        // Check 100 entries in random order
        Random random = new Random(1000); // arbitrary

        final List<Integer> order = new ArrayList<>(new AbstractList<Integer>() {
            @Override public Integer get(int index) { return index; }
            @Override public int size() { return 100; }
        });
        List<Map.Entry<String, String>> entries = new AbstractList<Map.Entry<String, String>>() {
            @Override
            public Map.Entry<String, String> get(int index) {
                int i = order.get(index);
                return new AbstractMap.SimpleEntry<>("key" + i, "value" + i);
            }
            @Override public int size() { return order.size(); }
        };
        Collections.shuffle(order, random); // Pick a random put() order of the entries
        for (Map.Entry<String, String> entry : entries) {
            m.put(entry.getKey(), entry.getValue());
        }
        Collections.shuffle(order, random); // Pick a different random order for the assertion
        checkEntrySpliterator(m, new ArrayList<>(entries));
    }

    private static void checkEntrySpliterator(Map<String, String> m,
            String... expectedKeysAndValues) {
        checkEntrySpliterator(m, makeEntries(expectedKeysAndValues));
    }

    private static void checkEntrySpliterator(Map<String, String> m,
            ArrayList<Map.Entry<String, String>> expectedEntries) {
        Set<Map.Entry<String, String>> entrySet = m.entrySet();
        Comparator<Map.Entry<String, String>> keysThenValuesComparator =
                Map.Entry.<String, String>comparingByKey()
                        .thenComparing(Map.Entry.comparingByValue());

        assertTrue(entrySet.spliterator().hasCharacteristics(Spliterator.DISTINCT));

        SpliteratorTester.runBasicIterationTests_unordered(entrySet.spliterator(),
                expectedEntries, keysThenValuesComparator);
        SpliteratorTester.runBasicSplitTests(entrySet.spliterator(),
                expectedEntries, keysThenValuesComparator);
        SpliteratorTester.testSpliteratorNPE(entrySet.spliterator());

        boolean isSized = entrySet.spliterator().hasCharacteristics(Spliterator.SIZED);
        if (isSized) {
            SpliteratorTester.runSizedTests(entrySet.spliterator(), entrySet.size());
        }
        Spliterator<?> subSpliterator = entrySet.spliterator().trySplit();
        if (subSpliterator != null && subSpliterator.hasCharacteristics(Spliterator.SIZED)) {
            SpliteratorTester.runSubSizedTests(entrySet.spliterator(), entrySet.size());
        }
    }

    private static<T> ArrayList<Map.Entry<T, T>> makeEntries(T... keysAndValues) {
        assertEquals(0, keysAndValues.length % 2);
        ArrayList<Map.Entry<T, T>> result = new ArrayList<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            result.add(new AbstractMap.SimpleEntry<>(keysAndValues[i], keysAndValues[i+1]));
        }
        return result;
    }

}
