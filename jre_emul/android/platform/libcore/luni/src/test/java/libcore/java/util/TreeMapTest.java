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

import junit.framework.TestCase;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.TreeMap;
import libcore.libcore.util.SerializationTester;

public class TreeMapTest extends TestCase {

    /**
     * Test that the entrySet() method produces correctly mutable entries.
     */
    public void testEntrySetSetValue() {
        TreeMap<String, String> map = new TreeMap<String, String>();
        map.put("A", "a");
        map.put("B", "b");
        map.put("C", "c");

        Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
        Entry<String, String> entryA = iterator.next();
        assertEquals("a", entryA.setValue("x"));
        assertEquals("x", entryA.getValue());
        assertEquals("x", map.get("A"));
        Entry<String, String> entryB = iterator.next();
        assertEquals("b", entryB.setValue("y"));
        Entry<String, String> entryC = iterator.next();
        assertEquals("c", entryC.setValue("z"));
        assertEquals("y", entryB.getValue());
        assertEquals("y", map.get("B"));
        assertEquals("z", entryC.getValue());
        assertEquals("z", map.get("C"));
    }

    /**
     * Test that the entrySet() method of a sub map produces correctly mutable
     * entries that propagate changes to the original map.
     */
    public void testSubMapEntrySetSetValue() {
        TreeMap<String, String> map = new TreeMap<String, String>();
        map.put("A", "a");
        map.put("B", "b");
        map.put("C", "c");
        map.put("D", "d");
        NavigableMap<String, String> subMap = map.subMap("A", true, "C", true);

        Iterator<Entry<String, String>> iterator = subMap.entrySet().iterator();
        Entry<String, String> entryA = iterator.next();
        assertEquals("a", entryA.setValue("x"));
        assertEquals("x", entryA.getValue());
        assertEquals("x", subMap.get("A"));
        assertEquals("x", map.get("A"));
        Entry<String, String> entryB = iterator.next();
        assertEquals("b", entryB.setValue("y"));
        Entry<String, String> entryC = iterator.next();
        assertEquals("c", entryC.setValue("z"));
        assertEquals("y", entryB.getValue());
        assertEquals("y", subMap.get("B"));
        assertEquals("y", map.get("B"));
        assertEquals("z", entryC.getValue());
        assertEquals("z", subMap.get("C"));
        assertEquals("z", map.get("C"));
    }

    /**
     * Test that an Entry given by any method except entrySet() is immutable.
     */
    public void testExceptionsOnSetValue() {
        TreeMap<String, String> map = new TreeMap<String, String>();
        map.put("A", "a");
        map.put("B", "b");
        map.put("C", "c");

        assertAllEntryMethodsReturnImmutableEntries(map);
    }

    /**
     * Test that an Entry given by any method except entrySet() of a sub map is immutable.
     */
    public void testExceptionsOnSubMapSetValue() {
        TreeMap<String, String> map = new TreeMap<String, String>();
        map.put("A", "a");
        map.put("B", "b");
        map.put("C", "c");
        map.put("D", "d");

        assertAllEntryMethodsReturnImmutableEntries(map.subMap("A", true, "C", true));
    }

    /**
     * Asserts that each NavigableMap method that returns an Entry (except entrySet()) returns an
     * immutable one. Assumes that the map contains at least entries with keys "A", "B" and "C".
     */
    private void assertAllEntryMethodsReturnImmutableEntries(NavigableMap<String, String> map) {
        assertImmutable(map.ceilingEntry("B"));
        assertImmutable(map.firstEntry());
        assertImmutable(map.floorEntry("D"));
        assertImmutable(map.higherEntry("A"));
        assertImmutable(map.lastEntry());
        assertImmutable(map.lowerEntry("C"));
        assertImmutable(map.pollFirstEntry());
        assertImmutable(map.pollLastEntry());
    }

    private void assertImmutable(Entry<String, String> entry) {
        String initialValue = entry.getValue();
        try {
            entry.setValue("x");
            fail();
        } catch (UnsupportedOperationException e) {
        }
        assertEquals(initialValue, entry.getValue());
    }

    public void testConcurrentModificationDetection() {
        Map<String, String> map = new TreeMap<String, String>();
        map.put("A", "a");
        map.put("B", "b");
        map.put("C", "c");
        Iterator<Entry<String,String>> iterator = map.entrySet().iterator();
        iterator.next();
        iterator.next();
        iterator.remove();
        map.put("D", "d");
        try {
            iterator.next();
            fail();
        } catch (ConcurrentModificationException e) {
        }
    }

    public void testIteratorRemoves() {
        Map<String, String> map = new TreeMap<String, String>();
        map.put("A", "a");
        map.put("B", "b");
        map.put("C", "c");
        Iterator<Entry<String,String>> iterator = map.entrySet().iterator();
        assertEquals("A", iterator.next().getKey());
        assertEquals("B", iterator.next().getKey());
        iterator.remove();
        assertEquals("C", iterator.next().getKey());
        iterator.remove();
        assertFalse(iterator.hasNext());
    }

    /**
     * Test that entry set contains and removal use the comparator rather than equals.
     */
    public void testEntrySetUsesComparatorOnly() {
        Map<String,String> map = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        map.put("ABC", "a");
        assertTrue(map.entrySet().contains(new SimpleEntry<String, String>("abc", "a")));
        assertTrue(map.entrySet().remove(new SimpleEntry<String, String>("abc", "a")));
        assertEquals(0, map.size());
    }

    public void testMapConstructorPassingSortedMap() {
        Map<String,String> source = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        NavigableMap<String,String> copy = new TreeMap<String, String>(source);
        assertEquals(null, copy.comparator());
    }

    public void testNullsWithNaturalOrder() {
        HashMap<String, String> copyFrom = new HashMap<String, String>();
        copyFrom.put(null, "b");
        try {
            new TreeMap<String, String>(copyFrom);
            fail(); // the RI doesn't throw if null is the only key
        } catch (NullPointerException expected) {
        }

        TreeMap<String,String> map = new TreeMap<String, String>();
        try {
            map.put(null, "b");
            fail();
        } catch (NullPointerException e) {
        }

        try {
            map.descendingMap().put(null, "b");
            fail();
        } catch (NullPointerException e) {
        }

        try {
            map.subMap("a", "z").put(null, "b");
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testClassCastExceptions() {
        Map<Object, Object> map = new TreeMap<Object, Object>();
        map.put("A", "a");
        try {
            map.get(5);
            fail();
        } catch (ClassCastException e) {
        }
        try {
            map.containsKey(5);
            fail();
        } catch (ClassCastException e) {
        }
        try {
            map.remove(5);
            fail();
        } catch (ClassCastException e) {
        }
    }

    public void testClassCastExceptionsOnBounds() {
        Map<Object, Object> map = new TreeMap<Object, Object>().subMap("a", "z");
        try {
            map.get(5);
            fail();
        } catch (ClassCastException e) {
        }
        try {
            map.containsKey(5);
            fail();
        } catch (ClassCastException e) {
        }
        try {
            map.remove(5);
            fail();
        } catch (ClassCastException e) {
        }
    }

    public void testClone() {
        TreeMap<String, String> map = new TreeMap<String, String>() {
            @Override public String toString() {
                return "x:" + super.toString();
            }
        };

        map.put("A", "a");
        map.put("B", "b");

        @SuppressWarnings("unchecked")
        TreeMap<String, String> clone = (TreeMap<String, String>) map.clone();
        assertEquals(map.getClass(), clone.getClass());
        assertEquals("x:{A=a, B=b}", map.toString());
    }

    public void testEmptyMapSerialization() {
        String s = "aced0005737200116a6176612e7574696c2e547265654d61700cc1f63e2d256a"
                + "e60300014c000a636f6d70617261746f727400164c6a6176612f7574696c2f436"
                + "f6d70617261746f723b78707077040000000078";
        TreeMap<String, String> map = new TreeMap<String, String>();
        new SerializationTester<TreeMap<String, String>>(map, s).test();
    }

    public void testSerializationWithComparator() {
        String s = "aced0005737200116a6176612e7574696c2e547265654d61700cc1f63e2d256a"
                + "e60300014c000a636f6d70617261746f727400164c6a6176612f7574696c2f436"
                + "f6d70617261746f723b78707372002a6a6176612e6c616e672e537472696e6724"
                + "43617365496e73656e736974697665436f6d70617261746f7277035c7d5c50e5c"
                + "e02000078707704000000027400016171007e00057400016271007e000678";
        TreeMap<String, String> map = new TreeMap<String, String>(
                String.CASE_INSENSITIVE_ORDER);
        map.put("a", "a");
        map.put("b", "b");
        new SerializationTester<NavigableMap<String, String>>(map, s) {
            @Override protected void verify(NavigableMap<String, String> deserialized) {
                assertEquals(0, deserialized.comparator().compare("X", "x"));
            }
        }.test();
    }

    public void testSubMapSerialization() {
        // Updated golden value since we have a different serialVersionUID in OpenJDK.
        String s = "aced0005737200216a6176612e7574696c2e547265654d617024417363656e646"
                + "96e675375624d61700cab946d1f0fab1c020000787200216a6176612e7574696c2"
                + "e547265654d6170244e6176696761626c655375624d617026617d4eacdd5933020"
                + "0075a000966726f6d53746172745a000b6869496e636c75736976655a000b6c6f4"
                + "96e636c75736976655a0005746f456e644c000268697400124c6a6176612f6c616"
                + "e672f4f626a6563743b4c00026c6f71007e00024c00016d7400134c6a6176612f7"
                + "574696c2f547265654d61703b7870000001007400016374000161737200116a617"
                + "6612e7574696c2e547265654d61700cc1f63e2d256ae60300014c000a636f6d706"
                + "17261746f727400164c6a6176612f7574696c2f436f6d70617261746f723b78707"
                + "372002a6a6176612e6c616e672e537472696e672443617365496e73656e7369746"
                + "97665436f6d70617261746f7277035c7d5c50e5ce0200007870770400000004710"
                + "07e000671007e00067400016271007e000c71007e000571007e000574000164710"
                + "07e000d78";
        TreeMap<String, String> map = new TreeMap<String, String>(
                String.CASE_INSENSITIVE_ORDER);
        map.put("a", "a");
        map.put("b", "b");
        map.put("c", "c");
        map.put("d", "d");
        SortedMap<String, String> subMap = map.subMap("a", "c");
        new SerializationTester<SortedMap<String, String>>(subMap, s) {
            @Override protected void verify(SortedMap<String, String> deserialized) {
                try {
                    deserialized.put("e", "e");
                    fail();
                } catch (IllegalArgumentException expected) {
                }
            }
        }.test();
    }

    public void testNavigableSubMapSerialization() {
        // Updated golden value since we have a different serialVersionUID in OpenJDK.
        String s = "aced0005737200216a6176612e7574696c2e547265654d617024417363656e646"
                + "96e675375624d61700cab946d1f0fab1c020000787200216a6176612e7574696c2"
                + "e547265654d6170244e6176696761626c655375624d617026617d4eacdd5933020"
                + "0075a000966726f6d53746172745a000b6869496e636c75736976655a000b6c6f4"
                + "96e636c75736976655a0005746f456e644c000268697400124c6a6176612f6c616"
                + "e672f4f626a6563743b4c00026c6f71007e00024c00016d7400134c6a6176612f7"
                + "574696c2f547265654d61703b7870000100007400016374000161737200116a617"
                + "6612e7574696c2e547265654d61700cc1f63e2d256ae60300014c000a636f6d706"
                + "17261746f727400164c6a6176612f7574696c2f436f6d70617261746f723b78707"
                + "372002a6a6176612e6c616e672e537472696e672443617365496e73656e7369746"
                + "97665436f6d70617261746f7277035c7d5c50e5ce0200007870770400000004710"
                + "07e000671007e00067400016271007e000c71007e000571007e000574000164710"
                + "07e000d78";
        TreeMap<String, String> map = new TreeMap<String, String>(
                String.CASE_INSENSITIVE_ORDER);
        map.put("a", "a");
        map.put("b", "b");
        map.put("c", "c");
        map.put("d", "d");
        SortedMap<String, String> subMap = map.subMap("a", false, "c", true);
        new SerializationTester<SortedMap<String, String>>(subMap, s) {
            @Override protected void verify(SortedMap<String, String> deserialized) {
                try {
                    deserialized.put("e", "e");
                    fail();
                } catch (IllegalArgumentException expected) {
                }
            }
        }.test();
    }

    public void testDescendingMapSerialization() {
        // Updated golden value since we have a different serialVersionUID in OpenJDK.
        String s = "aced0005737200226a6176612e7574696c2e547265654d61702444657363656e6"
                + "4696e675375624d61700cab946d1f0f9d0c0200014c001172657665727365436f6"
                + "d70617261746f727400164c6a6176612f7574696c2f436f6d70617261746f723b7"
                + "87200216a6176612e7574696c2e547265654d6170244e6176696761626c6553756"
                + "24d617026617d4eacdd59330200075a000966726f6d53746172745a000b6869496"
                + "e636c75736976655a000b6c6f496e636c75736976655a0005746f456e644c00026"
                + "8697400124c6a6176612f6c616e672f4f626a6563743b4c00026c6f71007e00034"
                + "c00016d7400134c6a6176612f7574696c2f547265654d61703b787001010101707"
                + "0737200116a6176612e7574696c2e547265654d61700cc1f63e2d256ae60300014"
                + "c000a636f6d70617261746f7271007e000178707372002a6a6176612e6c616e672"
                + "e537472696e672443617365496e73656e736974697665436f6d70617261746f727"
                + "7035c7d5c50e5ce02000078707704000000027400016171007e000a74000162710"
                + "07e000b78737200286a6176612e7574696c2e436f6c6c656374696f6e732452657"
                + "665727365436f6d70617261746f7232000003fa6c354d510200014c0003636d707"
                + "1007e0001787071007e0009";
        TreeMap<String, String> map = new TreeMap<String, String>(
                String.CASE_INSENSITIVE_ORDER);
        map.put("a", "a");
        map.put("b", "b");
        NavigableMap<String, String> descendingMap = map.descendingMap();
        new SerializationTester<NavigableMap<String, String>>(descendingMap, s) {
            @Override protected void verify(NavigableMap<String, String> deserialized) {
                assertEquals("b", deserialized.navigableKeySet().first());
            }
        }.test();
    }

    public void testJava5SerializationWithComparator() {
        String s = "aced0005737200116a6176612e7574696c2e547265654d61700cc1f63e2d256a"
                + "e60300014c000a636f6d70617261746f727400164c6a6176612f7574696c2f436"
                + "f6d70617261746f723b78707372002a6a6176612e6c616e672e537472696e6724"
                + "43617365496e73656e736974697665436f6d70617261746f7277035c7d5c50e5c"
                + "e02000078707704000000027400016171007e00057400016271007e000678";
        TreeMap<String, String> map = new TreeMap<String, String>(
                String.CASE_INSENSITIVE_ORDER);
        map.put("a", "a");
        map.put("b", "b");
        new SerializationTester<TreeMap<String, String>>(map, s) {
            @Override protected void verify(TreeMap<String, String> deserialized) {
                assertEquals(0, deserialized.comparator().compare("X", "x"));
            }
        }.test();
    }

    /**
     * On JDK5, this fails with a NullPointerException after deserialization!
     */
    public void testJava5SubMapSerialization() {
        String s = "aced0005737200186a6176612e7574696c2e547265654d6170245375624d6170"
                + "a5818343a213c27f0200055a000966726f6d53746172745a0005746f456e644c0"
                + "00766726f6d4b65797400124c6a6176612f6c616e672f4f626a6563743b4c0006"
                + "7468697324307400134c6a6176612f7574696c2f547265654d61703b4c0005746"
                + "f4b657971007e00017870000074000161737200116a6176612e7574696c2e5472"
                + "65654d61700cc1f63e2d256ae60300014c000a636f6d70617261746f727400164"
                + "c6a6176612f7574696c2f436f6d70617261746f723b78707372002a6a6176612e"
                + "6c616e672e537472696e672443617365496e73656e736974697665436f6d70617"
                + "261746f7277035c7d5c50e5ce020000787077040000000471007e000471007e00"
                + "047400016271007e000a7400016371007e000b7400016471007e000c7871007e0"
                + "00b";
        TreeMap<String, String> map = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        map.put("a", "a");
        map.put("b", "b");
        map.put("c", "c");
        map.put("d", "d");
        SortedMap<String, String> subMap = map.subMap("a", "c");
        new SerializationTester<SortedMap<String, String>>(subMap, s) {
            @Override protected void verify(SortedMap<String, String> deserialized) {
                try {
                    deserialized.put("e", "e");
                    fail();
                } catch (IllegalArgumentException expected) {
                }
            }
        }.test();
    }

    /**
     * Taking a headMap or tailMap (exclusive or inclusive of the bound) of
     * a TreeMap with unbounded range never throws IllegalArgumentException.
     */
    public void testBounds_fromUnbounded() {
        applyBound('[', new TreeMap<>());
        applyBound(']', new TreeMap<>());
        applyBound('(', new TreeMap<>());
        applyBound(')', new TreeMap<>());
    }

    /**
     * Taking an exclusive-end submap of a parent map with an exclusive
     * range is allowed only if the bounds go in the same direction
     * (if parent and child are either both headMaps or both tailMaps,
     * but not otherwise).
     */
    public void testBounds_openSubrangeOfOpenRange() {
        // NavigableMap.{tail,head}Map(T key, boolean inclusive)'s
        // documentation says that it throws IAE "if this map itself has a
        // restricted range, and key lies outside the bounds of the range".
        // Since that documentation doesn't mention the value of inclusive,
        // one could argue that the following two cases should throw IAE,
        // but the actual implementation in TreeMap does not. This test
        // asserts the actual behavior.
        assertTrue(isWithinBounds(')', ')'));
        assertTrue(isWithinBounds('(', '('));

        // The following two tests check that TreeMap's behavior matches
        // that from earlier versions of Android (from before Android N).
        // AOSP commit b4105e7f1e3ab24131976f68be4554e694a0e1d4 ensured
        // that Android N was consistent with earlier versions' behavior.
        // Specifically, on Android,
        //      new TreeMap<>().headMap(0, false).tailMap(0, false)
        // and  new TreeMap<>().tailMap(0, false).headMap(0, false)
        // are both not allowed.
        assertFalse(isWithinBounds(')', '('));
        assertFalse(isWithinBounds('(', ')'));
    }

    /**
     * Taking a exclusive-end submap of an inclusive-end parent map is not
     * allowed regardless of the direction of the constraints (headMap
     * vs. tailMap) because the inclusive bound of the submap is not
     * contained in the exclusive range of the parent map.
     */
    public void testBounds_closedSubrangeOfOpenRange() {
        assertFalse(isWithinBounds(']', '('));
        assertFalse(isWithinBounds('[', ')'));
        assertFalse(isWithinBounds(']', ')'));
        assertFalse(isWithinBounds('[', '('));
    }

    /**
     * Taking an inclusive-end submap of an inclusive-end parent map
     * is allowed regardless of the direction of the constraints (headMap
     * vs. tailMap) because the inclusive bound of the submap is
     * contained in the inclusive range of the parent map.
     */
    public void testBounds_closedSubrangeOfClosedRange() {
        assertTrue(isWithinBounds(']', '['));
        assertTrue(isWithinBounds('[', ']'));
        assertTrue(isWithinBounds(']', ']'));
        assertTrue(isWithinBounds('[', '['));
    }

    /**
     * Taking an exclusive-end submap of an inclusive-end parent map
     * is allowed regardless of the direction of the constraints (headMap
     * vs. tailMap) because the exclusive bound of the submap is
     * contained in the inclusive range of the parent map.
     *
     * Note that
     * (a) isWithinBounds(')', '[') == true, while
     * (b) isWithinBounds('[', ')') == false
     * means that
     *   {@code new TreeMap<>().tailMap(0, true).headMap(0, false)}
     * is allowed but
     *   {@code new TreeMap<>().headMap(0, false).tailMap(0, true)}
     * is not.
     */
    public void testBounds_openSubrangeOfClosedRange() {
        assertTrue(isWithinBounds(')', '['));
        assertTrue(isWithinBounds('(', ']'));
        assertTrue(isWithinBounds('(', '['));
        assertTrue(isWithinBounds(')', ']'));

        // This is allowed:
        new TreeMap<>().tailMap(0, true).headMap(0, false);

        // This is not:
        try {
            new TreeMap<>().headMap(0, false).tailMap(0, true);
            fail("Should have thrown");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * Asserts whether constructing a (head or tail) submap with (inclusive or
     * exclusive) bound 0 is allowed on a (head or tail) map with (inclusive or
     * exclusive) bound 0. For example,
     *
     * {@code isWithinBounds(')', ']'} is true because the boundary of "0)"
     * (an infinitesimally small negative value) lies within the range "0]",
     * but {@code isWithinBounds(']', ')'} is false because 0 does not lie
     * within the range "0)".
     */
    private static boolean isWithinBounds(char submapBound, char mapBound) {
        NavigableMap<Integer, Void> m = applyBound(mapBound, new TreeMap<>());
        IllegalArgumentException thrownException = null;
        try {
            applyBound(submapBound, m);
        } catch (IllegalArgumentException e) {
            thrownException = e;
        }
        return (thrownException == null);
    }

    /**
     * Constructs a submap of the specified map, constrained by the given bound.
     */
    private static<V> NavigableMap<Integer, V> applyBound(char bound, NavigableMap<Integer, V> m) {
        Integer boundValue = 0; // arbitrary
        if (isLowerBound(bound)) {
            return m.tailMap(boundValue, isBoundInclusive(bound));
        } else {
            return m.headMap(boundValue, isBoundInclusive(bound));
        }
    }

    private static boolean isBoundInclusive(char bound) {
        return bound == '[' || bound == ']';
    }

    /**
     * Returns whether the specified bound corresponds to a tailMap, i.e. a Map whose
     * range of values has an (exclusive or inclusive) lower bound.
     */
    private static boolean isLowerBound(char bound) {
        return bound == '[' || bound == '(';
    }

    public void test_spliterator_keySet() {
        TreeMap<String, String> treeMap = new TreeMap<>();
        // Elements are added out of order to ensure ordering is still preserved.
        treeMap.put("a", "1");
        treeMap.put("i", "9");
        treeMap.put("j", "10");
        treeMap.put("k", "11");
        treeMap.put("l", "12");
        treeMap.put("b", "2");
        treeMap.put("c", "3");
        treeMap.put("d", "4");
        treeMap.put("e", "5");
        treeMap.put("n", "14");
        treeMap.put("o", "15");
        treeMap.put("p", "16");
        treeMap.put("f", "6");
        treeMap.put("g", "7");
        treeMap.put("h", "8");
        treeMap.put("m", "13");

        Set<String> keys = treeMap.keySet();
        List<String> expectedKeys = Arrays.asList(
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k" , "l", "m", "n", "o", "p");

        SpliteratorTester.runBasicIterationTests_unordered(keys.spliterator(), expectedKeys,
                String::compareTo);
        SpliteratorTester.runBasicSplitTests(keys, expectedKeys);
        SpliteratorTester.testSpliteratorNPE(keys.spliterator());
        assertEquals(
                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SORTED,
                keys.spliterator().characteristics());
        SpliteratorTester.runSortedTests(keys);
        SpliteratorTester.runOrderedTests(keys);
        SpliteratorTester.assertSupportsTrySplit(keys);
    }

    public void test_spliterator_values() {
        TreeMap<String, String> treeMap = new TreeMap<>();
        // Elements are added out of order to ensure ordering is still preserved.
        treeMap.put("a", "1");
        treeMap.put("i", "9");
        treeMap.put("j", "10");
        treeMap.put("k", "11");
        treeMap.put("l", "12");
        treeMap.put("b", "2");
        treeMap.put("c", "3");
        treeMap.put("d", "4");
        treeMap.put("e", "5");
        treeMap.put("n", "14");
        treeMap.put("o", "15");
        treeMap.put("p", "16");
        treeMap.put("f", "6");
        treeMap.put("g", "7");
        treeMap.put("h", "8");
        treeMap.put("m", "13");

        Collection<String> values = treeMap.values();
        List<String> expectedValues = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9",
                "10", "11", "12", "13", "14", "15", "16");

        SpliteratorTester.runBasicIterationTests_unordered(
                values.spliterator(), expectedValues, String::compareTo);
        SpliteratorTester.runBasicSplitTests(values, expectedValues);
        SpliteratorTester.testSpliteratorNPE(values.spliterator());

        assertEquals(Spliterator.ORDERED | Spliterator.SIZED,
                values.spliterator().characteristics());
        SpliteratorTester.runSizedTests(values, 16);
        SpliteratorTester.runOrderedTests(values);
        SpliteratorTester.assertSupportsTrySplit(values);
    }

    public void test_spliterator_entrySet() {
        TreeMap<String, String> treeMap = new TreeMap<>();
        // Elements are added out of order to ensure ordering is still preserved.
        treeMap.put("a", "1");
        treeMap.put("i", "9");
        treeMap.put("j", "10");
        treeMap.put("k", "11");
        treeMap.put("l", "12");
        treeMap.put("b", "2");
        treeMap.put("c", "3");
        treeMap.put("d", "4");
        treeMap.put("e", "5");
        treeMap.put("n", "14");
        treeMap.put("o", "15");
        treeMap.put("p", "16");
        treeMap.put("f", "6");
        treeMap.put("g", "7");
        treeMap.put("h", "8");
        treeMap.put("m", "13");

        Set<Map.Entry<String, String>> entries = treeMap.entrySet();
        List<Map.Entry<String, String>> expectedValues = Arrays.asList(
                entry("a", "1"),
                entry("b", "2"),
                entry("c", "3"),
                entry("d", "4"),
                entry("e", "5"),
                entry("f", "6"),
                entry("g", "7"),
                entry("h", "8"),
                entry("i", "9"),
                entry("j", "10"),
                entry("k", "11"),
                entry("l", "12"),
                entry("m", "13"),
                entry("n", "14"),
                entry("o", "15"),
                entry("p", "16")
        );

        Comparator<Map.Entry<String, String>> comparator =
                (a, b) -> (a.getKey().compareTo(b.getKey()));

        SpliteratorTester.runBasicIterationTests_unordered(entries.spliterator(), expectedValues,
                (a, b) -> (a.getKey().compareTo(b.getKey())));
        SpliteratorTester.runBasicSplitTests(entries, expectedValues, comparator);
        SpliteratorTester.testSpliteratorNPE(entries.spliterator());

        assertEquals(
                Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SORTED,
                entries.spliterator().characteristics());
        SpliteratorTester.runSortedTests(entries, (a, b) -> (a.getKey().compareTo(b.getKey())));
        SpliteratorTester.runOrderedTests(entries);
        SpliteratorTester.assertSupportsTrySplit(entries);
    }

    private static<K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public void test_replaceAll() throws Exception {
        TreeMap<String, String> map = new TreeMap<>();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        TreeMap<String, String> output = new TreeMap<>();
        map.replaceAll((k, v) -> k + v);
        assertEquals("one1", map.get("one"));
        assertEquals("two2", map.get("two"));
        assertEquals("three3", map.get("three"));

        try {
            map.replaceAll(null);
            fail();
        } catch(NullPointerException expected) {}

        try {
            map.replaceAll((k, v) -> {
                map.put("foo", v);
                return v;
            });
            fail();
        } catch(ConcurrentModificationException expected) {}
    }

    public void test_replace$K$V$V() {
        MapDefaultMethodTester.test_replace$K$V$V(new TreeMap<>(), false /* acceptsNullKey */,
                true /* acceptsNullValue */);
    }

    public void test_replace$K$V() {
        MapDefaultMethodTester.test_replace$K$V(new TreeMap<>(), false /* acceptsNullKey */,
                true /* acceptsNullValue */);
    }
}
