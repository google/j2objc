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

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

public class MapDefaultMethodTester {

    private MapDefaultMethodTester() {}

    public static void test_getOrDefault(Map<Integer, Double> m, boolean acceptsNullKey,
            boolean acceptsNullValue) {
        // Unmapped key
        assertEquals(-1.0, m.getOrDefault(1, -1.0));

        // Mapped key
        m.put(1, 11.0);
        assertEquals(11.0, m.getOrDefault(1, -1.0));

        // Check for null value
        if (acceptsNullValue) {
            m.put(1, null);
            assertEquals(null, m.getOrDefault(1, -1.0));
        }

        // Check for null key
        if (acceptsNullKey) {
            m.put(null, 1.0);
            assertEquals(1.0, m.getOrDefault(null, -1.0));
        }
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
        } catch (NullPointerException expected) {}

        if (acceptsNullKey) {
            m.put(null, 1.0);
            assertEquals(1.0, m.computeIfPresent(null, (k, v) -> v));
        } else {
            try {
                m.computeIfPresent(null, (k, v) -> 5.0);
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
}
