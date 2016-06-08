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

import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;
import junit.framework.TestCase;
import libcore.util.SerializationTester;

public final class TreeSetTest extends TestCase {

    public void testEmptySetSerialization() {
        String s = "aced0005737200116a6176612e7574696c2e54726565536574dd98509395ed87"
                + "5b03000078707077040000000078";
        TreeSet<String> set = new TreeSet<String>();
        new SerializationTester<TreeSet<String>>(set, s).test();
    }

    public void testSerializationWithComparator() {
        String s = "aced0005737200116a6176612e7574696c2e54726565536574dd98509395ed87"
                + "5b03000078707372002a6a6176612e6c616e672e537472696e672443617365496"
                + "e73656e736974697665436f6d70617261746f7277035c7d5c50e5ce0200007870"
                + "770400000002740001617400016278";
        TreeSet<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        set.add("a");
        set.add("b");
        new SerializationTester<NavigableSet<String>>(set, s) {
            @Override protected void verify(NavigableSet<String> deserialized) {
                assertEquals(0, deserialized.comparator().compare("X", "x"));
            }
        }.test();
    }

    public void testSubSetSerialization() {
        String s = "aced0005737200116a6176612e7574696c2e54726565536574dd98509395ed87"
                + "5b03000078707372002a6a6176612e6c616e672e537472696e672443617365496"
                + "e73656e736974697665436f6d70617261746f7277035c7d5c50e5ce0200007870"
                + "770400000002740001617400016278";
        TreeSet<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        set.add("a");
        set.add("b");
        set.add("c");
        set.add("d");
        final SortedSet<String> subSet = set.subSet("a", "c");
        new SerializationTester<SortedSet<String>>(subSet, s) {
            @Override protected void verify(SortedSet<String> deserialized) {
                assertBounded(deserialized, deserialized == subSet);
            }
        }.test();
    }

    public void testNavigableSubSetSerialization() {
        String s = "aced0005737200116a6176612e7574696c2e54726565536574dd98509395ed87"
                + "5b03000078707372002a6a6176612e6c616e672e537472696e672443617365496"
                + "e73656e736974697665436f6d70617261746f7277035c7d5c50e5ce0200007870"
                + "770400000002740001627400016378";
        TreeSet<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        set.add("a");
        set.add("b");
        set.add("c");
        set.add("d");
        final SortedSet<String> subSet = set.subSet("a", false, "c", true);
        new SerializationTester<SortedSet<String>>(subSet, s) {
            @Override protected void verify(SortedSet<String> deserialized) {
                assertBounded(deserialized, deserialized == subSet);
            }
        }.test();
    }

    /**
     * Regrettably, serializing a TreeSet causes it to forget its bounds. This
     * is unlike a serialized TreeMap which retains its bounds when serialized.
     */
    private void assertBounded(SortedSet<String> deserialized, boolean bounded) {
        if (bounded) {
            try {
                deserialized.add("e");
                fail();
            } catch (IllegalArgumentException expected) {
            }
        } else {
            assertTrue(deserialized.add("e"));
            assertTrue(deserialized.remove("e"));
        }
    }

    /**
     * Test that TreeSet never attempts to serialize a non-serializable
     * comparator. http://b/5552608
     */
    public void testDescendingSetSerialization() {
        String s = "aced0005737200116a6176612e7574696c2e54726565536574dd98509395ed87"
                + "5b0300007870737200276a6176612e7574696c2e436f6c6c656374696f6e73245"
                + "2657665727365436f6d70617261746f7264048af0534e4ad00200007870770400"
                + "000002740001627400016178";
        TreeSet<String> set = new TreeSet<String>();
        set.add("a");
        set.add("b");
        NavigableSet<String> descendingSet = set.descendingSet();
        new SerializationTester<NavigableSet<String>>(descendingSet, s) {
            @Override protected void verify(NavigableSet<String> deserialized) {
                assertEquals("b", deserialized.first());
            }
        }.test();
    }

    public void testJava5SerializationWithComparator() {
        String s = "aced0005737200116a6176612e7574696c2e54726565536574dd98509395ed87"
                + "5b03000078707372002a6a6176612e6c616e672e537472696e672443617365496"
                + "e73656e736974697665436f6d70617261746f7277035c7d5c50e5ce0200007870"
                + "770400000002740001617400016278";
        TreeSet<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        set.add("a");
        set.add("b");
        new SerializationTester<TreeSet<String>>(set, s) {
            @Override protected void verify(TreeSet<String> deserialized) {
                assertEquals(0, deserialized.comparator().compare("X", "x"));
            }
        }.test();
    }
}
