/*
 * Copyright (C) 2010 Google Inc.
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

import java.util.HashMap;
import junit.framework.TestCase;

public final class OldAndroidHashMapTest extends TestCase {

    private static final Integer ONE = 1;
    private static final Integer TWO = 2;
    private static final Integer THREE = 3;
    private static final Integer FOUR = 4;

    private void addItems(HashMap<String, Integer> map) {
        map.put("one", ONE);
        map.put("two", TWO);
        map.put("three", THREE);
        map.put("four", FOUR);

        assertEquals(4, map.size());

        assertEquals(ONE, map.get("one"));
        assertEquals(TWO, map.get("two"));
        assertEquals(THREE, map.get("three"));
        assertEquals(FOUR, map.get("four"));
    }

    public void testAdd() throws Exception {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        addItems(map);
    }

    public void testClear() throws Exception {
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        addItems(map);
        map.clear();
        assertEquals(0, map.size());
    }

    public void testRemove() throws Exception {
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        addItems(map);
        map.remove("three");
        assertNull(map.get("three"));
    }

    public void testManipulate() throws Exception {
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get(null));
        assertNull(map.get("one"));
        assertFalse(map.containsKey("one"));
        assertFalse(map.containsValue(new Integer(1)));
        assertNull(map.remove(null));
        assertNull(map.remove("one"));

        assertNull(map.put(null, -1));
        assertNull(map.put("one", 1));
        assertNull(map.put("two", 2));
        assertNull(map.put("three", 3));
        assertEquals(-1, map.put(null, 0).intValue());

        assertEquals(0, map.get(null).intValue());
        assertEquals(1, map.get("one").intValue());
        assertEquals(2, map.get("two").intValue());
        assertEquals(3, map.get("three").intValue());

        assertTrue(map.containsKey(null));
        assertTrue(map.containsKey("one"));
        assertTrue(map.containsKey("two"));
        assertTrue(map.containsKey("three"));

        assertTrue(map.containsValue(new Integer(0)));
        assertTrue(map.containsValue(new Integer(1)));
        assertTrue(map.containsValue(new Integer(2)));
        assertTrue(map.containsValue(new Integer(3)));

        assertEquals(0, map.remove(null).intValue());
        assertEquals(1, map.remove("one").intValue());
        assertEquals(2, map.remove("two").intValue());
        assertEquals(3, map.remove("three").intValue());

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get(null));
        assertNull(map.get("one"));
        assertFalse(map.containsKey("one"));
        assertFalse(map.containsValue(new Integer(1)));
        assertNull(map.remove(null));
        assertNull(map.remove("one"));
    }

    public void testKeyIterator() throws Exception {
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        boolean[] slots = new boolean[4];

        addItems(map);

        for (String s : map.keySet()) {
            int slot = 0;

            if (s.equals("one")) {
                slot = 0;
            } else if (s.equals("two")) {
                slot = 1;
            } else if (s.equals("three")) {
                slot = 2;
            } else if (s.equals("four")) {
                slot = 3;
            } else {
                fail("Unknown key in HashMap");
            }

            if (slots[slot]) {
                fail("key returned more than once");
            } else {
                slots[slot] = true;
            }
        }

        assertTrue(slots[0]);
        assertTrue(slots[1]);
        assertTrue(slots[2]);
        assertTrue(slots[3]);
    }

    public void testValueIterator() throws Exception {
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        boolean[] slots = new boolean[4];

        addItems(map);

        for (Object o : map.values()) {
            int slot = 0;

            if (o.equals(ONE)) {
                slot = 0;
            } else if (o.equals(TWO)) {
                slot = 1;
            } else if (o.equals(THREE)) {
                slot = 2;
            } else if (o.equals(FOUR)) {
                slot = 3;
            } else {
                fail("Unknown value in HashMap");
            }

            if (slots[slot]) {
                fail("value returned more than once");
            } else {
                slots[slot] = true;
            }
        }

        assertTrue(slots[0]);
        assertTrue(slots[1]);
        assertTrue(slots[2]);
        assertTrue(slots[3]);
    }

    public void testEntryIterator() throws Exception {
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        boolean[] slots = new boolean[4];

        addItems(map);

        for (Object o : map.entrySet()) {
            int slot = 0;

            if (o.toString().equals("one=1")) {
                slot = 0;
            } else if (o.toString().equals("two=2")) {
                slot = 1;
            } else if (o.toString().equals("three=3")) {
                slot = 2;
            } else if (o.toString().equals("four=4")) {
                slot = 3;
            } else {
                fail("Unknown entry in HashMap");
            }

            if (slots[slot]) {
                fail("entry returned more than once");
            } else {
                slots[slot] = true;
            }
        }

        assertTrue(slots[0]);
        assertTrue(slots[1]);
        assertTrue(slots[2]);
        assertTrue(slots[3]);
    }

    public void testEquals() throws Exception {
        HashMap<String, String> map1 = new HashMap<String, String>();
        HashMap<String, String> map2 = new HashMap<String, String>();
        HashMap<String, String> map3 = new HashMap<String, String>();

        map1.put("one", "1");
        map1.put("two", "2");
        map1.put("three", "3");

        map2.put("one", "1");
        map2.put("two", "2");
        map2.put("three", "3");

        assertTrue(map1.equals(map2));

        map3.put("one", "1");
        map3.put("two", "1");
        map3.put("three", "1");

        assertFalse(map1.equals(map3));
        assertFalse(map2.equals(map3));
    }
}
