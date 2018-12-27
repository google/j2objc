/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingDeque;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import libcore.util.Objects;

/* J2ObjC removed.
import dalvik.system.VMRuntime; */

import static java.util.Collections.checkedNavigableMap;
import static java.util.Collections.checkedQueue;
import static java.util.Collections.synchronizedNavigableMap;
import static java.util.Collections.unmodifiableNavigableMap;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;
import static java.util.Spliterator.SUBSIZED;
import static libcore.java.util.SpliteratorTester.assertHasCharacteristics;

public final class CollectionsTest extends TestCase {

    private static final Object NOT_A_STRING = new Object();
    private static final Object A_STRING = "string";

    public void testEmptyEnumeration() {
        Enumeration<Object> e = Collections.emptyEnumeration();
        assertFalse(e instanceof Serializable);
        assertFalse(e.hasMoreElements());
        try {
            e.nextElement();
            fail();
        } catch (NoSuchElementException expected) {
        }
    }

    public void testEmptyIterator() {
        testEmptyIterator(Collections.emptyIterator());
        testEmptyIterator(Collections.emptyList().iterator());
        testEmptyIterator(Collections.emptySet().iterator());
        testEmptyIterator(Collections.emptyMap().keySet().iterator());
        testEmptyIterator(Collections.emptyMap().entrySet().iterator());
        testEmptyIterator(Collections.emptyMap().values().iterator());
    }

    private void testEmptyIterator(Iterator<?> i) {
        assertFalse(i instanceof Serializable);
        assertFalse(i.hasNext());
        try {
            i.next();
            fail();
        } catch (NoSuchElementException expected) {
        }
        try {
            i.remove();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    public void testEmptyListIterator() {
        testEmptyListIterator(Collections.emptyListIterator());
        testEmptyListIterator(Collections.emptyList().listIterator());
        testEmptyListIterator(Collections.emptyList().listIterator(0));
    }

    private void testEmptyListIterator(ListIterator<?> i) {
        assertFalse(i instanceof Serializable);
        assertFalse(i.hasNext());
        assertFalse(i.hasPrevious());
        assertEquals(0, i.nextIndex());
        try {
            i.next();
            fail();
        } catch (NoSuchElementException expected) {
        }
        assertEquals(-1, i.previousIndex());
        try {
            i.previous();
            fail();
        } catch (NoSuchElementException expected) {
        }
        try {
            i.add(null);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
        try {
            i.remove();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    static final class ArrayListInheritor<T> extends ArrayList<T> {
        private int numSortCalls = 0;
        public ArrayListInheritor(Collection<T> initialElements) {
            super(initialElements);
        }

        @Override
        public void sort(Comparator<? super T> c) {
            super.sort(c);
            numSortCalls++;
        }

        public int numSortCalls() {
            return numSortCalls;
        }
    }

    /* J2ObjC removed.
    /**
     * Tests that when targetSdk {@code <= 25}, Collections.sort() does not delegate
     * to List.sort().
     *
    public void testSort_nougatOrEarlier_doesNotDelegateToListSort() {
        runOnTargetSdk(25, () -> { // Nougat MR1 / MR2
            ArrayListInheritor<String> list = new ArrayListInheritor<>(
                    Arrays.asList("a", "c", "b"));
            assertEquals(0, list.numSortCalls());
            Collections.sort(list);
            assertEquals(0, list.numSortCalls());
        });
    }

    public void testSort_postNougat_delegatesToListSort() {
        runOnTargetSdkAtLeast(26, () -> {
            ArrayListInheritor<String> list = new ArrayListInheritor<>(
                    Arrays.asList("a", "c", "b"));
            assertEquals(0, list.numSortCalls());
            Collections.sort(list);
            assertEquals(1, list.numSortCalls());
        });
    }

    public void testSort_modcountUnmodifiedForLinkedList() {
        runOnTargetSdkAtLeast(26, () -> {
            LinkedList<String> list = new LinkedList<>(Arrays.asList(
                    "red", "green", "blue", "violet"));
            Iterator<String> it = list.iterator();
            it.next();
            Collections.sort(list);
            it.next(); // does not throw ConcurrentModificationException
        });
    }

    public void testSort_modcountModifiedForArrayListAndSubclasses() {
        runOnTargetSdkAtLeast(26, () -> {
            List<String> testData = Arrays.asList("red", "green", "blue", "violet");

            ArrayList<String> list = new ArrayList<>(testData);
            Iterator<String> it = list.iterator();
            it.next();
            Collections.sort(list);
            try {
                it.next();
                fail();
            } catch (ConcurrentModificationException expected) {
            }

            list = new ArrayListInheritor<>(testData);
            it = list.iterator();
            it.next();
            Collections.sort(list);
            try {
                it.next();
                fail();
            } catch (ConcurrentModificationException expected) {
            }
        });
    }

    /**
     * Runs the given runnable on this thread with the targetSdkVersion temporarily set
     * to the specified value, unless the current value is already higher.
     *
    private static void runOnTargetSdkAtLeast(int minimumTargetSdkForTest, Runnable runnable) {
        int targetSdkForTest = Math.max(minimumTargetSdkForTest,
                VMRuntime.getRuntime().getTargetSdkVersion());
        runOnTargetSdk(targetSdkForTest, runnable);
    }

    /**
     * Runs the given runnable on this thread with the targetSdkVersion temporarily set
     * to the specified value. This helps test behavior that depends on an API level
     * other than the current one (e.g. between releases).
     *
    private static void runOnTargetSdk(int targetSdkForTest, Runnable runnable) {
        VMRuntime runtime = VMRuntime.getRuntime();
        int targetSdk = runtime.getTargetSdkVersion();
        try {
            runtime.setTargetSdkVersion(targetSdkForTest);
            runnable.run();
        } finally {
            runtime.setTargetSdkVersion(targetSdk);
        }
    } */

    /**
     * A value type whose {@code compareTo} method returns one of {@code 0},
     * {@code Integer.MIN_VALUE} and {@code Integer.MAX_VALUE}.
     */
    static final class IntegerWithExtremeComparator
            implements Comparable<IntegerWithExtremeComparator> {
        private final int value;

        public IntegerWithExtremeComparator(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(IntegerWithExtremeComparator another) {
            if (another.value == this.value) {
                return 0;
            } else if (another.value > this.value) {
                return Integer.MIN_VALUE;
            } else {
                return Integer.MAX_VALUE;
            }
        }
    }

    // http://b/19749094
    public void testBinarySearch_comparatorThatReturnsMinAndMaxValue() {
        ArrayList<Integer> list = new ArrayList<Integer>(16);
        list.add(4);
        list.add(9);
        list.add(11);
        list.add(14);
        list.add(16);

        int index = Collections.binarySearch(list, 9, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                final int compare = lhs.compareTo(rhs);
                if (compare == 0) {
                    return 0;
                } else if (compare < 0) {
                    return Integer.MIN_VALUE;
                } else {
                    return Integer.MAX_VALUE;
                }
            }
        });
        assertEquals(1, index);

        ArrayList<IntegerWithExtremeComparator> list2 =
                new ArrayList<IntegerWithExtremeComparator>();
        list2.add(new IntegerWithExtremeComparator(4));
        list2.add(new IntegerWithExtremeComparator(9));
        list2.add(new IntegerWithExtremeComparator(11));
        list2.add(new IntegerWithExtremeComparator(14));
        list2.add(new IntegerWithExtremeComparator(16));

        assertEquals(1, Collections.binarySearch(list2, new IntegerWithExtremeComparator(9)));
    }

    public void testBinarySearch_emptyCollection() {
        assertEquals(-1, Collections.binarySearch(new ArrayList<Integer>(), 9));

        assertEquals(-1, Collections.binarySearch(new ArrayList<>(), 9, Integer::compareTo));
    }

    public void testSingletonSpliterator() {
        Spliterator<String> sp = Collections.singletonList("spiff").spliterator();

        assertEquals(1, sp.estimateSize());
        assertEquals(1, sp.getExactSizeIfKnown());
        assertNull(sp.trySplit());
        assertEquals(true, sp.tryAdvance(value -> assertEquals("spiff", value)));
        assertEquals(false, sp.tryAdvance(value -> fail()));
    }

    public void test_checkedNavigableMap_replaceAll() {
        NavigableMap<String, Integer> map = checkedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)),
                String.class, Integer.class);
        map.replaceAll((k, v) -> 5 * v);
        assertEquals(
                createMap("key3", 15, "key1", 5, "key4", 20, "key2", 10),
                map);
    }

    public void test_checkedNavigableMap_putIfAbsent() {
        NavigableMap<Integer, Double> map =
                checkedNavigableMap(new TreeMap<>(), Integer.class, Double.class);
        MapDefaultMethodTester.test_putIfAbsent(map,
                false /* acceptsNullKey */, true /* acceptsNullValue */);
    }

    public void test_checkedNavigableMap_remove() {
        NavigableMap<Integer, Double> map =
                checkedNavigableMap(new TreeMap<>(), Integer.class, Double.class);
        MapDefaultMethodTester.test_remove(map,
                false /* acceptsNullKey */, true /* acceptsNullValue */);
    }

    public void test_checkedNavigableMap_replace$K$V() {
        NavigableMap<Integer, Double> map =
                checkedNavigableMap(new TreeMap<>(), Integer.class, Double.class);
        MapDefaultMethodTester.test_replace$K$V$V(map,
                false /* acceptsNullKey */, true /* acceptsNullValue */);
    }

    public void test_checkedNavigableMap_replace$K$V$V() {
        NavigableMap<Integer, Double> map =
                checkedNavigableMap(new TreeMap<>(), Integer.class, Double.class);
        MapDefaultMethodTester.test_replace$K$V$V(map,
                false /* acceptsNullKey */, true /* acceptsNullValue */);
    }

    public void test_checkedNavigableMap_computeIfAbsent() {
        NavigableMap<Integer, Double> map =
                checkedNavigableMap(new TreeMap<>(), Integer.class, Double.class);
        MapDefaultMethodTester.test_computeIfAbsent(map,
                false /* acceptsNullKey */, true /* acceptsNullValue */);
    }

    public void test_checkedNavigableMap_computeIfPresent() {
        NavigableMap<Integer, Double> map =
                checkedNavigableMap(new TreeMap<>(), Integer.class, Double.class);
        MapDefaultMethodTester.test_computeIfPresent(map, false /* acceptsNullKey */);
    }

    public void test_checkedNavigableMap_compute() {
        NavigableMap<Integer, Double> map =
                checkedNavigableMap(new TreeMap<>(), Integer.class, Double.class);
        MapDefaultMethodTester.test_compute(map, false /* acceptsNullKey */);
    }

    public void test_checkedNavigableMap_merge() {
        NavigableMap<Integer, Double> map =
                checkedNavigableMap(new TreeMap<>(), Integer.class, Double.class);
        MapDefaultMethodTester.test_merge(map, false /* acceptsNullKey */);
    }

    public void test_checkedNavigableMap_navigableKeySet() {
        NavigableMap<String, Integer> map = checkedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)),
                String.class, Integer.class);
        check_navigableSet(
                map.navigableKeySet(),
                Arrays.asList("key1", "key2", "key3", "key4") /* expectedElementsInOrder */,
                "absent" /* absentElement */);
    }

    public void test_checkedNavigableMap_values() {
        NavigableMap<String, Integer> map = checkedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)),
                String.class, Integer.class);
        check_orderedCollection(map.values(), Arrays.asList(1, 2, 3, 4) /* expectedElementsInOrder */);
    }

    public void test_checkedNavigableMap_isChecked() {
        NavigableMap<String, Integer> delegate = new TreeMap<>();
        delegate.put("present", 1);
        delegate.put("another key", 2);
        check_navigableMap_isChecked(
                checkedNavigableMap(delegate, String.class, Integer.class),
                "present", 1, "aaa absent", "zzz absent", 42);
    }

    public void test_checkedNavigableSet() {
        NavigableSet set = Collections.checkedNavigableSet(new TreeSet<>(), String.class);
        check_navigableSet(set, Arrays.asList(), "absent element");

        set.add("element 1");
        set.add("element 2");
        List<String> elementsInOrder = Arrays.asList("element 1", "element 2");
        check_navigableSet(set, elementsInOrder, "absent element");

        assertEquals(set, new HashSet<>(elementsInOrder));
        assertEquals(new HashSet<>(elementsInOrder), set);
        assertEquals(2, set.size());
        assertTrue(set.contains("element 1"));
        assertTrue(set.contains("element 2"));
        assertFalse(set.contains("absent element"));
    }

    public void test_checkedNavigableSet_isChecked() {
        NavigableSet set = Collections.checkedNavigableSet(new TreeSet<>(), String.class);
        assertThrowsCce(() -> { set.add(new Object()); });
        assertThrowsCce(() -> { set.addAll(Arrays.asList(new Object())); });
    }

    public void test_checkedQueue() {
        Queue queue = checkedQueue(new LinkedBlockingDeque<>(2), CharSequence.class);
        assertQueueEmpty(queue);
        // Demonstrate that any implementation of CharSequence works by using two
        // different ones (StringBuilder and String) as values.
        StringBuilder firstElement = new StringBuilder("first element");
        assertTrue(queue.add(firstElement));
        assertFalse(queue.isEmpty());
        assertTrue(queue.add("second element"));
        assertEquals(2, queue.size());

        assertFalse(queue.offer("third element")); // queue is at capacity
        try {
            queue.add("third element");
            fail();
        } catch (IllegalStateException expected) {
        }
        assertThrowsCce(() -> { queue.add(new Object()); }); // fails the type check
        assertEquals(2, queue.size()); // size is unchanged

        // element() and peek() don't remove the first element
        assertSame(firstElement, queue.element());
        assertSame(firstElement, queue.peek());

        assertSame(firstElement, queue.poll());
        assertSame("second element", queue.poll());
        assertQueueEmpty(queue);

        assertThrowsCce(() -> { queue.add(new Object()); }); // fails the type check
    }

    /**
     * Asserts properties that should hold for any empty queue.
     */
    private static void assertQueueEmpty(Queue queue) {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.peek());
        try {
            queue.element();
            fail();
        } catch (NoSuchElementException expected) {
        }
        assertNull(queue.poll());
    }

    public void test_unmodifiableMap_getOrDefault() {
        Map<Integer, Double> delegate = new HashMap<>();
        delegate.put(2, 12.0);
        delegate.put(3, null);
        Map<Integer, Double> m = Collections.unmodifiableMap(delegate);
        assertEquals(-1.0, m.getOrDefault(1, -1.0));
        assertEquals(12.0, m.getOrDefault(2, -1.0));
        assertEquals(null, m.getOrDefault(3, -1.0));
    }

    public void test_unmodifiableMap_forEach() {
        Map<Integer, Double> delegate = new HashMap<>();
        Map<Integer, Double> replica = new HashMap<>();
        delegate.put(1, 10.0);
        delegate.put(2, 20.0);
        Collections.unmodifiableMap(delegate).forEach(replica::put);
        assertEquals(10.0, replica.get(1));
        assertEquals(20.0, replica.get(2));
        assertEquals(2, replica.size());
    }

    public void test_unmodifiableMap_putIfAbsent() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).putIfAbsent(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        Map<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).putIfAbsent(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_remove() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).remove(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        Map<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).remove(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_replace$K$V$V() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).replace(1, 5.0, 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        Map<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).replace(1, 5.0, 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_replace$K$V() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).replace(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        Map<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).replace(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_computeIfAbsent() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).computeIfAbsent(1, k -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        Map<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).computeIfAbsent(1, k -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_computeIfPresent() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).computeIfPresent(1, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        Map<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).computeIfPresent(1, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_compute() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).compute(1, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        Map<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).compute(1, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableMap_merge() {
        try {
            Collections.unmodifiableMap(new HashMap<>()).merge(1, 2.0, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // For existing key
        Map<Integer, Double> m = new HashMap<>();
        m.put(1, 5.0);
        try {
            Collections.unmodifiableMap(m).merge(1, 2.0, (k, v) -> 1.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableNavigableMap_empty() {
        NavigableMap<String, Integer> map = unmodifiableNavigableMap(new TreeMap<>());

        check_unmodifiableNavigableMap_defaultMethods(map,
                Arrays.<String>asList(),
                Arrays.<Integer>asList(),
                "absent key", -1 /* absentValue */);

        check_unmodifiableNavigableMap_collectionViews(map,
                Arrays.<String>asList(),
                Arrays.<Integer>asList(),
                "absent key");
    }

    public void test_unmodifiableNavigableMap_nonEmpty() {
        NavigableMap<String, Integer> map = unmodifiableNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)));

        check_unmodifiableNavigableMap_defaultMethods(map,
                Arrays.asList("key1", "key2", "key3", "key4"),
                Arrays.asList(1, 2, 3, 4),
                "absent key", -1 /* absentValue */);

        check_unmodifiableNavigableMap_collectionViews(map,
                Arrays.asList("key1", "key2", "key3", "key4"),
                Arrays.asList(1, 2, 3, 4),
                "absent key");
    }

    public void test_unmodifiableNavigableSet_empty() {
        NavigableSet<String> set = Collections.unmodifiableNavigableSet(new TreeSet<>());
        check_unmodifiableSet(set, "absent element");
        check_navigableSet(set, new ArrayList<>(), "absent element");
    }

    public void test_unmodifiableNavigableSet_nonEmpty() {
        NavigableSet<String> delegate = new TreeSet<>();
        NavigableSet<String> set = Collections.unmodifiableNavigableSet(delegate);
        delegate.add("pear");
        delegate.add("banana");
        delegate.add("apple");
        delegate.add("melon");

        check_unmodifiableNavigableSet(set,
                Arrays.asList("apple", "banana", "melon", "pear"),
                "absent element");

        assertEquals("pear", set.ceiling("nonexistent"));
        assertEquals("melon", set.floor("nonexistent"));
    }

    public void test_synchronizedNavigableMap_replaceAll() {
        NavigableMap<String, Integer> map = synchronizedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)));
        map.replaceAll((k, v) -> 5 * v);
        assertEquals(map, createMap("key3", 15,  "key1", 5, "key4", 20, "key2", 10));
    }

    public void test_synchronizedNavigableMap_putIfAbsent() {
        MapDefaultMethodTester.test_putIfAbsent(
                Collections.synchronizedNavigableMap(new TreeMap<>()),
                false /* acceptsNullKey */, true /* acceptsNullValue */);
    }

    public void test_synchronizedNavigableMap_remove() {
        MapDefaultMethodTester.test_remove(
                Collections.synchronizedNavigableMap(new TreeMap<>()),
                false /* acceptsNullKey */, true /* acceptsNullValue */);
    }

    public void test_synchronizedNavigableMap_replace$K$V$V() {
        MapDefaultMethodTester.test_replace$K$V$V(
                Collections.synchronizedNavigableMap(new TreeMap<>()),
                false /* acceptsNullKey */, true /* acceptsNullValue */);
    }

    public void test_synchronizedNavigableMap_replace$K$V() {
        MapDefaultMethodTester.test_replace$K$V(
                Collections.synchronizedNavigableMap(new TreeMap<>()),
                false /* acceptsNullKey */, true /* acceptsNullValue */);
    }

    public void test_synchronizedNavigableMap_computeIfAbsent() {
        MapDefaultMethodTester.test_computeIfAbsent(
                Collections.synchronizedNavigableMap(new TreeMap<>()),
                false /* acceptsNullKey */, true /* acceptsNullValue */);
    }

    public void test_synchronizedNavigableMap_computeIfPresent() {
        MapDefaultMethodTester.test_computeIfPresent(
                Collections.synchronizedNavigableMap(new TreeMap<>()),
                false /* acceptsNullKey */);
    }

    public void test_synchronizedNavigableMap_compute() {
        MapDefaultMethodTester.test_compute(
                Collections.synchronizedNavigableMap(new TreeMap<>()),
                false /* acceptsNullKey */);
    }

    public void test_synchronizedNavigableMap_merge() {
        MapDefaultMethodTester.test_merge(
                Collections.synchronizedNavigableMap(new TreeMap<>()),
                false /* acceptsNullKey */);
    }

    public void test_synchronizedNavigableMap_keySet() {
        NavigableMap<String, Integer> map = synchronizedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)));
        // Note: keySet() returns a Collections$UnmodifiableSet (not instanceof NavigableSet)
        Set<String> set = map.keySet();
        check_orderedSet(set, Arrays.asList("key1", "key2", "key3", "key4"));
    }

    public void test_synchronizedNavigableMap_navigableKeySet() {
        NavigableMap<String, Integer> map = synchronizedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)));
        NavigableSet<String> set = map.navigableKeySet();
        check_navigableSet(set, Arrays.asList("key1", "key2", "key3", "key4"), "absent element");
    }

    public void test_synchronizedNavigableMap_descendingMap_descendingKeySet() {
        NavigableMap<String, Integer> map = synchronizedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)));
        NavigableSet<String> set = map.descendingMap().descendingKeySet();
        check_navigableSet(set, Arrays.asList("key1", "key2", "key3", "key4"), "absent element");
    }

    public void test_synchronizedNavigableMap_descendingKeySet() {
        NavigableMap<String, Integer> map = synchronizedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)));
        NavigableSet<String> set = map.descendingKeySet();
        check_navigableSet(set, Arrays.asList("key4", "key3", "key2", "key1"), "absent element");
    }

    public void test_synchronizedNavigableMap_descendingMap_keySet() {
        NavigableMap<String, Integer> map = synchronizedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)));
        // Note: keySet() returns a Collections$UnmodifiableSet (not instanceof NavigableSet)
        Set<String> set = map.descendingMap().keySet();
        check_orderedSet(set, Arrays.asList("key4", "key3", "key2", "key1"));
    }

    public void test_synchronizedNavigableMap_descendingMap_navigableKeySet() {
        NavigableMap<String, Integer> map = synchronizedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)));
        NavigableSet<String> set = map.descendingMap().navigableKeySet();
        check_navigableSet(set, Arrays.asList("key4", "key3", "key2", "key1"), "absent element");
    }

    public void test_synchronizedNavigableMap_values() {
        NavigableMap<String, Integer> map = synchronizedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)));
        Collection<Integer> values = map.values();
        check_orderedCollection(values, Arrays.asList(1, 2, 3, 4));
    }

    public void test_synchronizedNavigableMap_descendingMap_values() {
        NavigableMap<String, Integer> map = synchronizedNavigableMap(
                new TreeMap<>(createMap("key3", 3, "key1", 1, "key4", 4, "key2", 2)));
        Collection<Integer> values = map.descendingMap().values();
        check_orderedCollection(values, Arrays.asList(4, 3, 2, 1));
    }

    public void test_synchronizedNavigableSet_empty() {
        NavigableSet<String> set = Collections.synchronizedNavigableSet(new TreeSet<>());
        check_navigableSet(set, new ArrayList<>(), "absent element");
    }

    public void test_synchronizedNavigableSet_nonEmpty() {
        List<String> elements = Arrays.asList("apple", "banana", "melon", "pear");
        NavigableSet<String> set = Collections.synchronizedNavigableSet(new TreeSet<>(elements));
        check_navigableSet(set, elements, "absent element");
    }

    private static<K,V> void check_unmodifiableNavigableMap_defaultMethods(NavigableMap<K,V> map,
            List<K> keysInOrder, List<V> valuesInOrder, K absentKey, V absentValue) {
        check_unmodifiableOrderedMap_defaultMethods(map, keysInOrder, valuesInOrder,
                absentKey, absentValue);

        List<K> reverseKeys = reverseCopyOf(keysInOrder);
        List<V> reverseValues = reverseCopyOf(valuesInOrder);

        check_unmodifiableOrderedMap_defaultMethods(map.descendingMap(), reverseKeys,
                reverseValues, absentKey, absentValue);

        int numEntries = keysInOrder.size();
        for (int i = 0; i < numEntries; i++) {
            K key = keysInOrder.get(i);
            V value = valuesInOrder.get(i);

            check_unmodifiableOrderedMap_defaultMethods(
                    map.headMap(key),
                    keysInOrder.subList(0, i),
                    valuesInOrder.subList(0, i),
                    absentKey,
                    absentValue);
            check_unmodifiableOrderedMap_defaultMethods(
                    map.headMap(key, false /* inclusive */),
                    keysInOrder.subList(0, i),
                    valuesInOrder.subList(0, i),
                    absentKey,
                    absentValue);
            check_unmodifiableOrderedMap_defaultMethods(
                    map.headMap(key, true /* inclusive */),
                    keysInOrder.subList(0, i + 1),
                    valuesInOrder.subList(0, i + 1),
                    absentKey,
                    absentValue);
            K lowerKey = map.lowerKey(key);
            if (lowerKey != null) {
                // headMap inclusive of lowerKey is same as exclusive of key
                check_unmodifiableOrderedMap_defaultMethods(
                        map.headMap(lowerKey, true /* inclusive */),
                        keysInOrder.subList(0, i),
                        valuesInOrder.subList(0, i),
                        absentKey,
                        absentValue);
            }

            check_unmodifiableOrderedMap_defaultMethods(
                    map.tailMap(key),
                    keysInOrder.subList(i, numEntries),
                    valuesInOrder.subList(i, numEntries),
                    absentKey,
                    absentValue);
            check_unmodifiableOrderedMap_defaultMethods(
                    map.tailMap(key, true /* inclusive */),
                    keysInOrder.subList(i, numEntries),
                    valuesInOrder.subList(i, numEntries),
                    absentKey,
                    absentValue);
            check_unmodifiableOrderedMap_defaultMethods(
                    map.tailMap(key, false /* inclusive */),
                    keysInOrder.subList(i + 1, numEntries),
                    valuesInOrder.subList(i + 1, numEntries),
                    absentKey,
                    absentValue);
            K higherKey = map.higherKey(key);
            if (higherKey != null) {
                // headMap inclusive of higherKey is same as exclusive of key
                check_unmodifiableOrderedMap_defaultMethods(
                        map.tailMap(higherKey, true /* inclusive */),
                        keysInOrder.subList(i + 1, numEntries),
                        valuesInOrder.subList(i + 1, numEntries),
                        absentKey,
                        absentValue);
            }

            int headSize = map.headMap(absentKey).size();
            check_unmodifiableOrderedMap_defaultMethods(
                    map.headMap(absentKey, true /* inclusive */),
                    keysInOrder.subList(0, headSize),
                    valuesInOrder.subList(0, headSize),
                    absentKey,
                    absentValue);
            check_unmodifiableOrderedMap_defaultMethods(
                    map.tailMap(absentKey, true /* inclusive */),
                    keysInOrder.subList(headSize, numEntries),
                    valuesInOrder.subList(headSize, numEntries),
                    absentKey,
                    absentValue);

            assertEquals(key, map.floorKey(key));
            assertEquals(key, map.ceilingKey(key));
            assertEquals(new AbstractMap.SimpleEntry<>(key, value), map.floorEntry(key));
            assertEquals(new AbstractMap.SimpleEntry<>(key, value), map.ceilingEntry(key));
        }

        K floor = map.floorKey(absentKey);
        K ceiling = map.ceilingKey(absentKey);
        if (numEntries == 0) {
            assertNull(floor);
            assertNull(ceiling);
        } else {
            assertFalse(Objects.equal(floor, ceiling));
            assertTrue(floor != null || ceiling != null);
            assertEquals(ceiling, floor == null ? map.firstKey() : map.higherKey(floor));
            assertEquals(floor, ceiling == null ? map.lastKey() : map.lowerKey(ceiling));
        }
    }

    /**
     * Tests Map's default methods (getOrDefault, forEach, ...) on the given Map.
     *
     * @param keysInOrder the expected keys in the map, in iteration order
     * @param valuesInOrder the expected values in the map, in iteration order
     * @param absentKey a key that does not occur in the map
     * @param absentValue a value that does not occur in the map
     */
    private static<K,V> void check_unmodifiableOrderedMap_defaultMethods(Map<K,V> map,
            List<K> keysInOrder, List<V> valuesInOrder, K absentKey, V absentValue) {
        if (keysInOrder.size() != valuesInOrder.size()) {
            throw new IllegalArgumentException();
        }
        Map<K, V> mapCopy = new LinkedHashMap<K, V>(map);

        // getOrDefault
        int numEntries = keysInOrder.size();
        for (int i = 0; i < numEntries; i++) {
            assertEquals(valuesInOrder.get(i), map.getOrDefault(keysInOrder.get(i), null));
        }

        // forEach
        List<K> keysCopy = new ArrayList<>();
        List<V> valuesCopy = new ArrayList<>();
        map.forEach((k, v) -> {
            keysCopy.add(k);
            valuesCopy.add(v);
        });
        assertEquals(keysInOrder, keysCopy);
        assertEquals(valuesInOrder, valuesCopy);

        assertThrowsUoe(() -> { map.putIfAbsent(absentKey, absentValue); });
        assertThrowsUoe(() -> { map.remove(absentKey); });
        assertThrowsUoe(() -> { map.replace(absentKey, absentValue, absentValue); });
        assertThrowsUoe(() -> { map.replace(absentKey, absentValue); });
        assertThrowsUoe(() -> { map.computeIfAbsent(absentKey, k -> absentValue); });
        assertThrowsUoe(() -> { map.computeIfPresent(absentKey, (k, v) -> absentValue); });
        assertThrowsUoe(() -> { map.compute(absentKey, (k, v) -> absentValue); });
        assertThrowsUoe(() -> { map.merge(absentKey, absentValue, (k, v) -> absentValue); });

        if (numEntries > 0) {
            K sampleKey = keysInOrder.get(0);
            V sampleValue = valuesInOrder.get(0);
            assertThrowsUoe(() -> { map.putIfAbsent(sampleKey, absentValue); });
            assertThrowsUoe(() -> { map.remove(sampleKey); });
            assertThrowsUoe(() -> { map.replace(sampleKey, sampleValue, absentValue); });
            assertThrowsUoe(() -> { map.replace(sampleKey, absentValue); });
            assertThrowsUoe(() -> { map.computeIfAbsent(sampleKey, k -> absentValue); });
            assertThrowsUoe(() -> { map.computeIfPresent(sampleKey, (k, v) -> absentValue); });
            assertThrowsUoe(() -> { map.compute(sampleKey, (k, v) -> absentValue); });
            assertThrowsUoe(() -> { map.merge(sampleKey, sampleValue, (k, v) -> absentValue); });
        }

        // Check that map is unchanged
        assertEquals(mapCopy, map);
    }

    /**
     * Tests the various {@code Collection} views of the given Map for contents/
     * iteration order consistent with the given expectations.
     */
    private static<K,V> void check_unmodifiableNavigableMap_collectionViews(
            NavigableMap<K, V> map, List<K> keysInOrder, List<V> valuesInOrder, K absentKey) {
        List<K> reverseKeys = reverseCopyOf(keysInOrder);

        // keySet
        check_unmodifiableSet(map.keySet(), absentKey);
        check_orderedSet(map.keySet(), keysInOrder);

        // navigableKeySet
        check_unmodifiableNavigableSet(map.navigableKeySet(), keysInOrder, absentKey);

        // descendingMap -> descendingKeySet
        check_unmodifiableNavigableSet(
                map.descendingMap().descendingKeySet(), keysInOrder, absentKey);

        // descendingKeySet
        check_unmodifiableNavigableSet(map.descendingKeySet(), reverseKeys, absentKey);

        // descendingMap -> keySet
        check_unmodifiableSet(map.descendingMap().keySet(), absentKey);
        check_orderedSet(map.descendingMap().keySet(), reverseKeys);

        // descendingMap -> navigableKeySet
        check_unmodifiableNavigableSet(
                map.descendingMap().navigableKeySet(), reverseKeys, absentKey);

        // values
        check_unmodifiableOrderedCollection(map.values(), valuesInOrder);
        check_orderedCollection(map.values(), valuesInOrder);

        // descendingValues
        check_unmodifiableOrderedCollection(map.descendingMap().values(), reverseCopyOf(valuesInOrder));
        check_orderedCollection(map.descendingMap().values(), reverseCopyOf(valuesInOrder));
    }

    /**
     * @param absentKeyHead absent key smaller than {@code presentKey}, under the Map's ordering
     * @param absentKeyTail absent key larger than {@code presentKey}, under the Map's ordering
     */
    private static<K,V> void check_navigableMap_isChecked(NavigableMap map,
            K presentKey, V presentValue, K absentKeyHead, K absentKeyTail, V absentValue) {
        check_map_isChecked(map,
                presentKey, presentValue, absentKeyHead, absentValue);
        check_map_isChecked(map.descendingMap(),
                presentKey, presentValue, absentKeyHead, absentValue);

        // Need to pass correct absent key since the Map might check for
        // range inclusion before checking the type of a value
        check_map_isChecked(map.headMap(presentKey, true /* inclusive */),
                presentKey, presentValue, absentKeyHead, absentValue);
        check_map_isChecked(map.tailMap(presentKey, true /* inclusive */),
                presentKey, presentValue, absentKeyTail, absentValue);
    }

    /**
     * Asserts that the given map is checked (rejects keys/values of type Object).
     *
     * @param map a checked Map that contains the entry (presentKey, preventValue), does not
     *            contain key absentKey or value absentValue, and rejects keys/types of type Object.
     */
    private static<K,V> void check_map_isChecked(Map map,
            K presentKey, V presentValue, K absentKey, V absentValue) {
        Map copyOfMap = new HashMap(map);
        assertEquals(map.get(presentKey), presentValue);
        assertFalse(map.containsKey(absentKey));
        assertFalse(map.values().contains(absentValue));

        assertThrowsCce(() -> { map.replaceAll((k, v) -> new Object()); });

        assertThrowsCce(() -> { map.putIfAbsent(presentKey, new Object()); });
        assertThrowsCce(() -> { map.putIfAbsent(absentKey, new Object()); });
        assertThrowsCce(() -> { map.putIfAbsent(new Object(), presentValue); });

        assertThrowsCce(() -> { map.remove(new Object()); });

        assertThrowsCce(() -> { map.replace(new Object(), presentValue); });
        assertThrowsCce(() -> { map.replace(presentKey, new Object()); });

        assertThrowsCce(() -> { map.replace(new Object(), presentValue, absentValue); });
        // doesn't throw, but has no effect since oldValue doesn't match
        assertFalse(map.replace(presentKey, new Object(), absentValue));
        assertThrowsCce(() -> { map.replace(presentKey, presentValue, new Object()); });

        assertThrowsCce(() -> { map.computeIfAbsent(new Object(), k -> presentValue); });
        // doesn't throw, but has no effect since presentKey is present
        assertEquals(presentValue, map.computeIfAbsent(presentKey, k -> new Object()));
        assertThrowsCce(() -> { map.computeIfAbsent(absentKey, k -> new Object()); });

        assertThrowsCce(() -> { map.computeIfPresent(new Object(), (k, v) -> presentValue); });
        assertThrowsCce(() -> { map.computeIfPresent(presentKey, (k, v) -> new Object()); });
        // doesn't throw, but has no effect since absentKey is absent
        assertNull(map.computeIfPresent(absentKey, (k, v) -> new Object()));

        assertThrowsCce(() -> { map.compute(new Object(), (k, v) -> presentValue); });
        assertThrowsCce(() -> { map.compute(presentKey, (k, v) -> new Object()); });
        assertThrowsCce(() -> { map.compute(absentKey, (k, v) -> new Object()); });

        assertThrowsCce(() -> { map.merge(new Object(), presentValue, (v1, v2) -> presentValue); });
        assertThrowsCce(() -> { map.merge(presentKey, presentValue, (v1, v2) -> new Object()); });

        // doesn't throw, puts (absentKey, absentValue) into the map
        map.merge(absentKey, absentValue, (v1, v2) -> new Object());
        assertEquals(absentValue, map.remove(absentKey)); // restore previous state

        assertThrowsCce(() -> { map.put(new Object(), absentValue); });
        assertThrowsCce(() -> { map.put(absentKey, new Object()); });
        assertThrowsCce(() -> { map.put(new Object(), presentValue); });
        assertThrowsCce(() -> { map.put(presentKey, new Object()); });

        assertEquals("map should be unchanged", copyOfMap, map);
    }

    private static <K> void check_unmodifiableNavigableSet(NavigableSet<K> set,
            List<K> expectedElementsInOrder, K absentElement) {
        check_unmodifiableSet(set, absentElement);
        check_unmodifiableSet(set.descendingSet(), absentElement);
        check_navigableSet(set, expectedElementsInOrder, absentElement);
        if (!expectedElementsInOrder.isEmpty()) {
            K sampleElement = expectedElementsInOrder.get(expectedElementsInOrder.size() / 2);
            check_unmodifiableSet(set.headSet(sampleElement), absentElement);
            check_unmodifiableSet(set.tailSet(sampleElement), absentElement);
        }
    }

    private static <K> void check_navigableSet(NavigableSet<K> set,
            List<K> expectedElementsInOrder, K absentElement) {
        check_orderedSet(set, expectedElementsInOrder);
        check_set(set, absentElement);

        int numElements = set.size();
        List<K> reverseOrder = new ArrayList<>(expectedElementsInOrder);
        Collections.reverse(reverseOrder);
        check_orderedSet(set.descendingSet(), reverseOrder);

        for (int i = 0; i < numElements; i++) {
            K element = expectedElementsInOrder.get(i);
            check_orderedSet(
                    set.headSet(element),
                    expectedElementsInOrder.subList(0, i));
            check_orderedSet(
                    set.headSet(element, false /* inclusive */),
                    expectedElementsInOrder.subList(0, i));
            check_orderedSet(
                    set.headSet(element, true /* inclusive */),
                    expectedElementsInOrder.subList(0, i + 1));

            check_orderedSet(
                    set.tailSet(element),
                    expectedElementsInOrder.subList(i, numElements));
            check_orderedSet(
                    set.tailSet(element, true /* inclusive */),
                    expectedElementsInOrder.subList(i, numElements));
            check_orderedSet(
                    set.tailSet(element, false /* inclusive */),
                    expectedElementsInOrder.subList(i + 1, numElements));

            assertEquals(element, set.floor(element));
            assertEquals(element, set.ceiling(element));
        }

        K floor = set.floor(absentElement);
        K ceiling = set.ceiling(absentElement);
        if (numElements == 0) {
            assertNull(floor);
            assertNull(ceiling);
        } else {
            assertFalse(Objects.equal(floor, ceiling));
            assertTrue(floor != null || ceiling != null);
        }
    }

    /**
     * Checks a Set that may or may not be instanceof SortedSet / NavigableSet
     * for adherence to a specified iteration order.
     */
    private static <K> void check_orderedSet(
            Set<K> set, List<K> expectedElementsInOrder) {
        assertEquals(expectedElementsInOrder, new ArrayList<>(set));
        Set<K> copy = new HashSet<>(expectedElementsInOrder);
        assertEquals(copy, set);
        assertEquals(copy.hashCode(), set.hashCode());

        int numElements = set.size();
        assertEquals(expectedElementsInOrder.size(), numElements);
        Spliterator<K> spliterator = set.spliterator();
        SpliteratorTester.runBasicIterationTests(spliterator, expectedElementsInOrder);
        if (spliterator.hasCharacteristics(SIZED)) {
            SpliteratorTester.runSizedTests(set, numElements);
        }
        if (spliterator.hasCharacteristics(SUBSIZED)) {
            SpliteratorTester.runSubSizedTests(set, numElements);
        }
        assertHasCharacteristics(ORDERED | DISTINCT, spliterator);
        SpliteratorTester.runOrderedTests(set);
    }

    private static <K> void check_unmodifiableSet(Set<K> set, K absentElement) {
        assertThrowsUoe(() -> { set.remove(null); } );
        assertThrowsUoe(set::clear);
        assertThrowsUoe(() -> { set.add(null); } );
        if (set.isEmpty()) {
            assertEquals(0, set.size());
        } else {
            assertTrue(set.size() > 0);
            Iterator<K> iterator = set.iterator();
            K firstElement = iterator.next();
            assertThrowsUoe(() -> { set.remove(firstElement); } );
            assertThrowsUoe(iterator::remove);
        }
        SpliteratorTester.runDistinctTests(set);

        check_set(set, absentElement);
    }

    private static <K> void check_set(Set<K> set, K absentElement) {
        // some basic properties that must hold for all sets (regardless of whether
        // they're ordered, strict, support null, ...):
        if (!set.isEmpty()) {
            K sampleElement = set.iterator().next();
            assertTrue(set.contains(sampleElement));
        }
        assertFalse(set.contains(absentElement));
    }

    private static <V> void check_unmodifiableOrderedCollection(
            Collection<V> values, List<V> elementsInOrder) {
        assertThrowsUoe(() -> { values.remove(null); } );
        assertThrowsUoe(values::clear);
        assertThrowsUoe(() -> { values.add(null); } );

        Iterator<V> iterator = values.iterator();
        if (!elementsInOrder.isEmpty()) {
            iterator.next();
            assertThrowsUoe(iterator::remove);
            assertThrowsUoe(() -> { values.remove(elementsInOrder.get(0)); });
        }
        check_orderedCollection(values, elementsInOrder);
    }

    private static <V> void check_orderedCollection(
            Collection<V> collection, List<V> elementsInOrder) {
        Spliterator<V> spliterator = collection.spliterator();
        SpliteratorTester.runBasicIterationTests(spliterator, elementsInOrder);
        if (spliterator.hasCharacteristics(SIZED)) {
            SpliteratorTester.runSizedTests(collection, elementsInOrder.size());
        }
        if (spliterator.hasCharacteristics(SUBSIZED)) {
            SpliteratorTester.runSubSizedTests(collection, elementsInOrder.size());
        }
        SpliteratorTester.runOrderedTests(collection);
    }

    public void test_EmptyMap_getOrDefault() {
        Map<Integer, Double> m = Collections.emptyMap();
        assertEquals(-1.0, m.getOrDefault(1, -1.0));
        assertEquals(-1.0, m.getOrDefault(2, -1.0));
    }

    public void test_EmptyMap_forEach() {
        try {
            Collections.emptyMap().forEach(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_EmptyMap_putIfAbsent() {
        try {
            Collections.emptyMap().putIfAbsent(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_remove() {
        try {
            Collections.emptyMap().remove(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_replace$K$V$V() {
        try {
            Collections.emptyMap().replace(1, 5.0, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_replace$K$V() {
        try {
            Collections.emptyMap().replace(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_computeIfAbsent() {
        try {
            Collections.emptyMap().computeIfAbsent(1, k -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_computeIfPresent() {
        try {
            Collections.emptyMap().computeIfPresent(1, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_compute() {
        try {
            Collections.emptyMap().compute(1, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_EmptyMap_merge() {
        try {
            Collections.emptyMap().merge(1, 5.0, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_getOrDefault() {
        Map<Integer, Double> m = Collections.singletonMap(1, 11.0);
        assertEquals(11.0, m.getOrDefault(1, -1.0));
        assertEquals(-1.0, m.getOrDefault(2, -1.0));
    }

    public void test_SingletonMap_forEach() {
        Map<Integer, Double> m = new HashMap<>();
        Collections.singletonMap(1, 11.0).forEach(m::put);
        assertEquals(11.0, m.getOrDefault(1, -1.0));
        assertEquals(1, m.size());
    }

    public void test_SingletonMap_putIfAbsent() {
        try {
            Collections.singletonMap(1, 11.0).putIfAbsent(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_remove() {
        try {
            Collections.singletonMap(1, 11.0).remove(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_replace$K$V$V() {
        try {
            Collections.singletonMap(1, 11.0).replace(1, 5.0, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_replace$K$V() {
        try {
            Collections.singletonMap(1, 11.0).replace(1, 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_computeIfAbsent() {
        try {
            Collections.singletonMap(1, 11.0).computeIfAbsent(1, k -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_computeIfPresent() {
        try {
            Collections.singletonMap(1, 11.0).computeIfPresent(1, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_compute() {
        try {
            Collections.singletonMap(1, 11.0).compute(1, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonMap_merge() {
        try {
            Collections.singletonMap(1, 11.0).merge(1, 5.0, (k, v) -> 5.0);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SynchronizedList_replaceAll() {
        ListDefaultMethodTester.test_replaceAll(Collections.synchronizedList(new ArrayList<>()));
    }

    public void test_SynchronizedList_sort() {
        ListDefaultMethodTester.test_sort(Collections.synchronizedList(new ArrayList<>()));
    }

    public void test_CheckedList_replaceAll() {
        ListDefaultMethodTester.test_replaceAll(Collections.checkedList(new ArrayList<>(), Integer.class));
    }

    public void test_CheckedList_sort() {
        ListDefaultMethodTester.test_sort(Collections.checkedList(new ArrayList<>(), Double.class));
    }

    public void test_EmptyList_replaceAll() {
        Collections.emptyList().replaceAll(k -> 1);

        try {
            Collections.emptyList().replaceAll(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_EmptyList_sort() {
        Collections.emptyList().sort((k1, k2) -> 1);
    }

    public void test_emptyNavigableMap() {
        NavigableMap<String, Integer> map = Collections.emptyNavigableMap();
        check_unmodifiableNavigableMap_defaultMethods(
                map,
                new ArrayList<>() /* keysInOrder */,
                new ArrayList<>() /* valuesInOrder */,
                "absent key" /* absentKey */,
                -1 /* absentValue */
        );
        check_unmodifiableNavigableMap_collectionViews(
                map,
                new ArrayList<>() /* keysInOrder */,
                new ArrayList<>() /* valuesInOrder */,
                "absent key" /* absentKey */);
    }

    public void test_emptyNavigableSet() {
        NavigableSet<String> set = Collections.emptyNavigableSet();
        check_unmodifiableNavigableSet(set, new ArrayList<>() /* expectedElementsInOrder */,
                "absent element");
        check_navigableSet(set, new ArrayList<>() /* expectedElementsInOrder */, "absent element");
    }

    public void test_emptySortedMap() {
        SortedMap<String, Integer> map = Collections.emptySortedMap();

        check_unmodifiableOrderedMap_defaultMethods(
                map,
                new ArrayList<>() /* keysInOrder */,
                new ArrayList<>() /* valuesInOrder */,
                "absent key" /* absentKey */,
                -1 /* absentValue */);
        check_unmodifiableSet(map.keySet(), "absent element");
        check_orderedSet(map.keySet(), new ArrayList<>() /* expectedElementsInOrder */);
        check_unmodifiableSet(map.entrySet(), new AbstractMap.SimpleEntry<>("absent element", 42));
        check_orderedCollection(map.values(), new ArrayList<>() /* expectedValuesInOrder */);
    }

    public void test_emptySortedSet() {
        SortedSet<String> set = Collections.emptySortedSet();
        check_unmodifiableSet(set, "absent element");
        check_orderedSet(set, new ArrayList<>() /* expectedElementsInOrder */);
    }

    public void test_unmodifiableList_replaceAll() {
        try {
            Collections.unmodifiableList(new ArrayList<>()).replaceAll(k -> 1);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // with non empty list

        try {
            ArrayList l = new ArrayList();
            l.add(1);
            l.add(2);
            Collections.unmodifiableList(l).replaceAll(k -> 1);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_unmodifiableList_sort() {
        try {
            Collections.unmodifiableList(new ArrayList<>()).sort((k1, k2) -> 1);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        // with non empty list

        try {
            ArrayList l = new ArrayList();
            l.add(1);
            l.add(2);
            Collections.unmodifiableList(l).sort((k1, k2) -> 1);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonList_replaceAll() {
        try {
            Collections.singletonList(1).replaceAll(k -> 2);
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_SingletonList_sort() {
        Collections.singletonList(1).sort((k1, k2) -> 2);
    }

    public void test_CheckedMap_replaceAll() {
        Map<Integer, Integer> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Integer.class);
        checkedMap.put(1, 10);
        checkedMap.put(2, 20);
        checkedMap.put(3, 30);
        checkedMap.replaceAll((k, v) -> (Integer)k + (Integer)v);
        assertEquals(11, checkedMap.get(1));
        assertEquals(22, checkedMap.get(2));
        assertEquals(33, checkedMap.get(3));
        assertEquals(3, checkedMap.size());
    }

    public void test_CheckedMap_putIfAbsent() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_putIfAbsent(checkedMap, true /* acceptsNullKey */,
                true /* acceptsNullValue */);

        // Without generics to check the typeCheck implementation
        Map checkedMap2 = Collections.checkedMap(new HashMap<>(), Integer.class, String.class);

        // When key is present
        checkedMap2.putIfAbsent(1, A_STRING);
        try {
            checkedMap2.putIfAbsent(1, NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}

        // When key is absent
        checkedMap2.clear();
        try {
            checkedMap2.putIfAbsent(1, NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_remove() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_remove(checkedMap, true /* acceptsNullKey */,
                true /* acceptsNullValue */);
    }

    public void test_CheckedMap_replace$K$V$V() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_replace$K$V$V(checkedMap, true /* acceptsNullKey */,
                true /* acceptsNullValue */);

        // Without generics to check the typeCheck implementation
        Map checkedMap2 = Collections.checkedMap(new HashMap<>(), Integer.class, String.class);
        checkedMap2.put(1, A_STRING);

        try {
            checkedMap2.replace(1, NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_replace$K$V() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_replace$K$V(checkedMap, true /* acceptsNullKey */,
                true /* acceptsNullValue */);

        // Without generics to check the typeCheck implementation
        Map checkedMap2 = Collections.checkedMap(new HashMap<>(), Integer.class, String.class);
        checkedMap2.put(1, A_STRING);

        try {
            checkedMap2.replace(1, 1, NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_computeIfAbsent() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_computeIfAbsent(checkedMap, true /* acceptsNullKey */,
                true /* acceptsNullValue */);

        // Without generics to check the typeCheck implementation
        Map checkedMap2 = Collections.checkedMap(new HashMap<>(), Integer.class, String.class);
        checkedMap2.put(1, A_STRING);

        // When key is present, function should not be invoked
        assertSame(A_STRING, checkedMap2.computeIfAbsent(1, k -> {
            throw new AssertionFailedError("key present: function should not be invoked");
        }));

        // When key is absent, computed value's type should be checked
        checkedMap2.clear();
        try {
            checkedMap2.computeIfAbsent(1, k -> NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_computeIfPresent() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_computeIfPresent(checkedMap, true /* acceptsNullKey */);

        // Without generics to check the typeCheck implementation
        Map m = new HashMap();
        Map checkedMap2 = Collections.checkedMap(m, Integer.class, String.class);
        checkedMap2.put(1, A_STRING);

        try {
            checkedMap2.computeIfPresent(1, (k, v) -> NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_compute() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_compute(checkedMap, true /* acceptsNullKey */);

        Map checkedMap2 = Collections.checkedMap(new HashMap(), Integer.class, String.class);
        checkedMap2.put(1, A_STRING);
        try {
            checkedMap2.compute(1, (k, v) -> NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    public void test_CheckedMap_merge() {
        Map<Integer, Double> map = new HashMap<>();
        Map checkedMap = Collections.checkedMap(map, Integer.class, Double.class);
        MapDefaultMethodTester.test_merge(checkedMap, true /* acceptsNullKey */);

        // Without generics to check the typeCheck implementation
        Map checkedMap2 =
                Collections.checkedMap(new HashMap<>(), Integer.class, String.class);
        checkedMap2.put(1, A_STRING);

        try {
            checkedMap2.merge(1, A_STRING, (v1, v2) -> NOT_A_STRING);
            fail();
        } catch (ClassCastException expected) {}
    }

    private static<K,V> Map<K, V> createMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> result = new HashMap<>();
        result.put(k1, v1);
        result.put(k2, v2);
        result.put(k3, v3);
        result.put(k4, v4);
        return result;
    }

    private static void assertThrowsUoe(Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    private static void assertThrowsCce(Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (ClassCastException expected) {
        }
    }

    private static<T> List<T> reverseCopyOf(List<T> list) {
        List<T> result = new LinkedList<>();
        for (T element : list) {
            result.add(0, element);
        }
        return result;
    }
}
