/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

public abstract class SortedMapTestBase extends TestCase {

    final int N = 1000;
    final int TRIES = 100;

    SortedMap<Integer, Integer> map;
    SortedMap<Integer, Integer> ref;

    Random rnd;

    protected void setUp() throws Exception {
        rnd = new Random(-1);
        for (int i = 0; i < N; i++) {
            ref.put(rnd.nextInt(N) * 2, rnd.nextBoolean() ? null : rnd.nextInt(N) * 2);
        }
    }

    public final void testClear() {
        map.clear();
        assertTrue(map.isEmpty());
    }

    public final void testContainsKey() {
        for (int i = 0; i < TRIES; i++) {
            int key = rnd.nextInt(N);
            assertEquals(ref.containsKey(key), map.containsKey(key));
        }
    }


    public final void testContainsValue() {
        for (int i = 0; i < TRIES; i++) {
            int value = rnd.nextInt(N);
            assertEquals(ref.containsValue(value), map.containsValue(value));
        }
    }


    public final void testEntrySet() {
        Set<Map.Entry<Integer, Integer>> refSet = ref.entrySet();
        Set<Map.Entry<Integer, Integer>> mapSet = map.entrySet();
        for (Map.Entry<Integer, Integer> e : refSet) {
            assertTrue(mapSet.contains(e));
        }
        for (Map.Entry<Integer, Integer> e : mapSet) {
            assertTrue(refSet.contains(e));
        }
        assertEquals(ref.entrySet(), map.entrySet());
    }


    public final void testGet() {
        for (int i = 0; i < TRIES; i++) {
            int key = rnd.nextInt(N);
            assertEquals(ref.get(key), map.get(key));
        }
    }


    public final void testKeySet() {
        assertEquals(ref.keySet(), map.keySet());
        Iterator<Integer> i = ref.keySet().iterator();
        Iterator<Integer> j = map.keySet().iterator();
        while (i.hasNext()) {
            assertEquals(i.next(), j.next());
            if (rnd.nextBoolean()) {
                j.remove();
                i.remove();
            }
        }
    }


    public final void testPut() {
        for (int i = 0; i < TRIES; i++) {
            int key = rnd.nextInt(N);
            int value = rnd.nextInt(N);
            assertEquals(ref.put(key, value), map.put(key, value));
            assertEquals(ref.get(key), map.get(key));
            assertEquals(ref, map);
        }
    }

    public final void testPut0() {
        ref.clear();
        map.clear();
        for (int i = 0; i < N; i++) {
            int key = rnd.nextInt(N);
            int value = rnd.nextInt(N);
            assertEquals(ref.put(key, value), map.put(key, value));
            assertEquals(ref.get(key), map.get(key));
        }
    }

    public final void testPutAll() {
        Map<Integer, Integer> mixin = new HashMap<Integer, Integer>(TRIES);
        for (int i = 0; i < TRIES; i++) {
            mixin.put(rnd.nextInt(N), rnd.nextInt(N));
        }
        ref.putAll(mixin);
        map.putAll(mixin);
        assertEquals(ref, map);
    }


    public final void testRemove() {
        for (int i = 0; i < N; i++) {
            int key = rnd.nextInt(N);
            assertEquals(ref.remove(key), map.remove(key));
            if (i % (N / TRIES) == 0) {
                assertEquals(ref, map);
            }
        }
    }

    public final void testRemove0() {
        while (!ref.isEmpty()) {
            int key = ref.tailMap((ref.firstKey() + ref.lastKey()) / 2)
                    .firstKey();
            assertEquals(ref.remove(key), map.remove(key));
        }
    }

    public final void testSize() {
        assertEquals(ref.size(), map.size());
    }


    public final void testValues() {
        assertEquals(ref.values().size(), map.values().size());
        assertTrue(ref.values().containsAll(map.values()));
        assertTrue(map.values().containsAll(ref.values()));

        Iterator<Integer> i = ref.values().iterator();
        Iterator<Integer> j = map.values().iterator();
        while (i.hasNext()) {
            assertEquals(i.next(), j.next());
            if (rnd.nextBoolean()) {
                j.remove();
                i.remove();
            }
        }
    }

    public final void testComparator() {
        assertEquals(ref.comparator(), map.comparator());
    }


    public final void testFirstKey() {
        assertEquals(ref.firstKey(), map.firstKey());
    }


    public final void testHeadMap() {
        for (int i = 0; i < TRIES; i++) {
            int key = rnd.nextInt(N);
            checkSubMap(ref.headMap(key), map.headMap(key));
        }
        checkSubMap(ref.headMap(-1), map.headMap(-1));
    }

    public final void testLastKey() {
        assertEquals(ref.lastKey(), map.lastKey());
    }

    public final void testSubMap() {
        for (int i = 0; i < TRIES; i++) {
            int key0 = rnd.nextInt(N / 2);
            int key1 = rnd.nextInt(N / 2) + N / 2;
            if (ref.comparator() != null &&
                    ref.comparator().compare(key0, key1) > 0) {

                int tmp = key0;
                key0 = key1;
                key1 = tmp;
            }
            checkSubMap(ref.subMap(key0, key1), map.subMap(key0, key1));
        }
        boolean caught = false;
        try {
            if (ref.comparator() != null && ref.comparator().compare(100, 0) < 0) {
                map.subMap(0, 100);
            } else {
                map.subMap(100, 0);
            }
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught);

        int firstKey = ref.firstKey();
        Map.Entry<Integer, Integer> refE = ref.entrySet().iterator().next();
        Map.Entry<Integer, Integer> mapE = map.entrySet().iterator().next();
        mapE.setValue(-1);
        refE.setValue(-1);
        assertEquals(ref.get(firstKey), map.get(firstKey));
    }


    public final void testTailMap() {
        for (int i = 0; i < TRIES; i++) {
            int key = rnd.nextInt(2 * N);
            checkSubMap(ref.tailMap(key), map.tailMap(key));
        }
        checkSubMap(ref.tailMap(2 * N + 1), map.tailMap(2 * N + 1));
    }


    public final void testHashCode() {
        assertEquals(ref.hashCode(), map.hashCode());
    }

    public final void testEqualsObject() {
        assertTrue(map.equals(ref));
        map.put(N + 1, N + 1);
        assertFalse(map.equals(ref));
    }


    public final void testIsEmpty() {
        assertEquals(ref.isEmpty(), map.isEmpty());
    }

    public final void testIsEmpty2() {
        TreeMap<String, String> map = new TreeMap<String, String>();
        map.put("one", "1");
        assertEquals("size should be one", 1, map.size());
        map.clear();
        assertEquals("size should be zero", 0, map.size());
        assertTrue("Should not have entries", !map.entrySet().iterator()
                .hasNext());

        map.put("one", "1");
        assertEquals("size should be one", 1, map.size());
        map.remove("one");
        assertEquals("size should be zero", 0, map.size());
        assertTrue("Should not have entries", !map.entrySet().iterator()
                .hasNext());

        map.clear();
        map.put("0", "1");
        map.clear();
        assertTrue(map.isEmpty());
        assertFalse(map.entrySet().iterator().hasNext());
        assertFalse(map.keySet().iterator().hasNext());
        assertFalse(map.values().iterator().hasNext());
    }

    public final void testToString() {
        assertEquals(ref.toString(), map.toString());
    }

    private void checkSubMap(SortedMap<Integer, Integer> ref,
            SortedMap<Integer, Integer> map) {

        assertEquals(ref.size(), map.size());
        assertEquals(ref, map);
        assertEquals(ref.isEmpty(), map.isEmpty());
        if (!ref.isEmpty()) {
            assertEquals(ref.firstKey(), map.firstKey());
            assertEquals(ref.lastKey(), map.lastKey());

            testViews(ref, map);
        } else {
            boolean caught = false;
            try {
                map.firstKey();
            } catch (NoSuchElementException e) {
                caught = true;
            }
            caught = false;
            try {
                map.lastKey();
            } catch (NoSuchElementException e) {
                caught = true;
            }
            assertTrue(caught);
        }

    }

    public final void testViews() {
        testViews(ref, map);
    }

    private void testViews(SortedMap<Integer, Integer> ref, SortedMap<Integer, Integer> map) {
        assertEquals(ref.keySet().size(), map.keySet().size());
        assertEquals(ref.keySet(), map.keySet());
        compareIterators(ref.keySet(), map.keySet());

        assertEquals(ref.values().size(), map.values().size());
        compareIterators(ref.values(), map.values());

        assertEquals(ref.entrySet(), map.entrySet());
        compareIterators(ref.entrySet(), map.entrySet());
    }

    private void compareIterators(Collection ref, Collection map) {
        Iterator i = ref.iterator();
        Iterator j = map.iterator();
        while (i.hasNext()) {
            assertEquals(i.next(), j.next());
            if (rnd.nextBoolean()) {
                j.remove();
                i.remove();
                assertEquals(ref.size(), map.size());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public final void testSerialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Object read = ois.readObject();
        assertEquals(ref, read);
    }

    public final void testClone() throws Exception {
        Method refClone = ref.getClass().getMethod("clone", new Class[0]);
        Method mapClone = map.getClass().getMethod("clone", new Class[0]);
        SortedMap<Integer, Integer> map2 = (SortedMap<Integer, Integer>) mapClone.invoke(map, new Object[0]);
        assertEquals(refClone.invoke(ref, new Object[0]), map2);
        map2.remove(map2.lastKey());
        assertFalse(ref.equals(map2));
    }

}
