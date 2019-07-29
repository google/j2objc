/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

public class EnumSetTest extends TestCase {
    static final boolean disableRIBugs = true;

    static enum EnumWithInnerClass {
        a, b, c, d, e, f {
        },
    }

    enum EnumWithAllInnerClass {
        a {},
        b {},
    }

    static enum EnumFoo {
        a, b,c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, aa, bb, cc, dd, ee, ff, gg, hh, ii, jj, kk, ll,
    }

    static enum EmptyEnum {
        // expected
    }

    static enum HugeEnumWithInnerClass {
        a{}, b{}, c{}, d{}, e{}, f{}, g{}, h{}, i{}, j{}, k{}, l{}, m{}, n{}, o{}, p{}, q{}, r{}, s{}, t{}, u{}, v{}, w{}, x{}, y{}, z{}, A{}, B{}, C{}, D{}, E{}, F{}, G{}, H{}, I{}, J{}, K{}, L{}, M{}, N{}, O{}, P{}, Q{}, R{}, S{}, T{}, U{}, V{}, W{}, X{}, Y{}, Z{}, aa{}, bb{}, cc{}, dd{}, ee{}, ff{}, gg{}, hh{}, ii{}, jj{}, kk{}, ll{}, mm{},
    }

    static enum HugeEnum {
        a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, aa, bb, cc, dd, ee, ff, gg, hh, ii, jj, kk, ll, mm,
    }

    static enum HugeEnumCount {
        NO1, NO2, NO3, NO4, NO5, NO6, NO7, NO8, NO9, NO10, NO11, NO12, NO13, NO14, NO15, NO16, NO17, NO18, NO19, NO20,
        NO21, NO22, NO23, NO24, NO25, NO26, NO27, NO28, NO29, NO30, NO31, NO32, NO33, NO34, NO35, NO36, NO37, NO38, NO39, NO40,
        NO41, NO42, NO43, NO44, NO45, NO46, NO47, NO48, NO49, NO50, NO51, NO52, NO53, NO54, NO55, NO56, NO57, NO58, NO59, NO60,
        NO61, NO62, NO63, NO64, NO65, NO66, NO67, NO68, NO69, NO70, NO71, NO72, NO73, NO74, NO75, NO76, NO77, NO78, NO79, NO80,
        NO81, NO82, NO83, NO84, NO85, NO86, NO87, NO88, NO89, NO90, NO91, NO92, NO93, NO94, NO95, NO96, NO97, NO98, NO99, NO100,
        NO101, NO102, NO103, NO104, NO105, NO106, NO107, NO108, NO109, NO110, NO111, NO112, NO113, NO114, NO115, NO116, NO117, NO118, NO119, NO120,
        NO121, NO122, NO123, NO124, NO125, NO126, NO127, NO128, NO129, NO130,
    }

    /**
     * java.util.EnumSet#noneOf(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public void test_NoneOf_LClass() {
        try {
            EnumSet.noneOf((Class) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.noneOf(Enum.class);
            fail("Should throw ClassCastException");
        } catch (ClassCastException cce) {
            // expected
        }

        Class<EnumWithAllInnerClass> c = (Class<EnumWithAllInnerClass>) EnumWithAllInnerClass.a
                .getClass();
        try {
            EnumSet.noneOf(c);
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        EnumSet<EnumWithAllInnerClass> setWithInnerClass = EnumSet
                .noneOf(EnumWithAllInnerClass.class);
        assertNotNull(setWithInnerClass);

        // test enum type with more than 64 elements
        Class<HugeEnumWithInnerClass> hc = (Class<HugeEnumWithInnerClass>) HugeEnumWithInnerClass.a
            .getClass();
        try {
            EnumSet.noneOf(hc);
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        EnumSet<HugeEnumWithInnerClass> hugeSetWithInnerClass = EnumSet
            .noneOf(HugeEnumWithInnerClass.class);
        assertNotNull(hugeSetWithInnerClass);
    }

    /**
     * java.util.HugeEnumSet#iterator()
     */
    public void test_iterator_HugeEnumSet() {
        EnumSet<HugeEnumCount> set;
        Object[] array;

        // Test HugeEnumSet with 65 elements
        // which is more than the bits of Long
        set = EnumSet.range(HugeEnumCount.NO1, HugeEnumCount.NO65);
        array = set.toArray();
        for (HugeEnumCount count : set) {
            assertEquals(count, (HugeEnumCount) array[count.ordinal()]);
        }

        // Test HugeEnumSet with 130 elements
        // which is more than twice of the bits of Long
        set = EnumSet.range(HugeEnumCount.NO1, HugeEnumCount.NO130);
        array = set.toArray();
        for (HugeEnumCount count : set) {
            assertEquals(count, (HugeEnumCount) array[count.ordinal()]);
        }
    }

    public void testRemoveIteratorRemoveFromHugeEnumSet() {
        EnumSet<HugeEnumCount> set = EnumSet.noneOf(HugeEnumCount.class);
        set.add(HugeEnumCount.NO64);
        set.add(HugeEnumCount.NO65);
        set.add(HugeEnumCount.NO128);
        Iterator<HugeEnumCount> iterator = set.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(HugeEnumCount.NO64, iterator.next());
        assertTrue(iterator.hasNext());
        iterator.remove();
        assertEquals(HugeEnumCount.NO65, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(HugeEnumCount.NO128, iterator.next());
        assertFalse(iterator.hasNext());
        assertEquals(EnumSet.of(HugeEnumCount.NO65, HugeEnumCount.NO128), set);
        iterator.remove();
        assertEquals(EnumSet.of(HugeEnumCount.NO65), set);
    }

    /**
     * java.util.EnumSet#allOf(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public void test_AllOf_LClass() {
        try {
            EnumSet.allOf((Class) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.allOf(Enum.class);
            fail("Should throw ClassCastException");
        } catch (ClassCastException cce) {
            // expected
        }

        EnumSet<EnumFoo> enumSet = EnumSet.allOf(EnumFoo.class);
        assertEquals("Size of enumSet should be 64", 64, enumSet.size());

        assertFalse(
                "enumSet should not contain null value", enumSet.contains(null));
        assertTrue(
                "enumSet should contain EnumFoo.a", enumSet.contains(EnumFoo.a));
        assertTrue(
                "enumSet should contain EnumFoo.b", enumSet.contains(EnumFoo.b));

        enumSet.add(EnumFoo.a);
        assertEquals("Should be equal", 64, enumSet.size());

        EnumSet<EnumFoo> anotherSet = EnumSet.allOf(EnumFoo.class);
        assertEquals("Should be equal", enumSet, anotherSet);
        assertNotSame("Should not be identical", enumSet, anotherSet);

        // test enum with more than 64 elements
        EnumSet<HugeEnum> hugeEnumSet = EnumSet.allOf(HugeEnum.class);
        assertEquals(65, hugeEnumSet.size());

        assertFalse(hugeEnumSet.contains(null));
        assertTrue(hugeEnumSet.contains(HugeEnum.a));
        assertTrue(hugeEnumSet.contains(HugeEnum.b));

        hugeEnumSet.add(HugeEnum.a);
        assertEquals(65, hugeEnumSet.size());

        EnumSet<HugeEnum> anotherHugeSet = EnumSet.allOf(HugeEnum.class);
        assertEquals(hugeEnumSet, anotherHugeSet);
        assertNotSame(hugeEnumSet, anotherHugeSet);
    }

    /**
     * java.util.EnumSet#add(E)
     */
    @SuppressWarnings("unchecked")
    public void test_add_E() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.a);
        set.add(EnumFoo.b);

        try {
            set.add(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        // test enum type with more than 64 elements
        Set rawSet = set;
        try {
            rawSet.add(HugeEnumWithInnerClass.b);
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        set.clear();
        try {
            set.add(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        boolean result = set.add(EnumFoo.a);
        assertEquals("Size should be 1:", 1, set.size());
        assertTrue("Return value should be true", result);

        result = set.add(EnumFoo.a);
        assertEquals("Size should be 1:", 1, set.size());
        assertFalse("Return value should be false", result);

        set.add(EnumFoo.b);
        assertEquals("Size should be 2:", 2, set.size());

        rawSet = set;
        try {
            rawSet.add(EnumWithAllInnerClass.a);
            fail("Should throw ClassCastException");
        } catch(ClassCastException e) {
            // expected
        }

        try {
            rawSet.add(EnumWithInnerClass.a);
            fail("Should throw ClassCastException");
        } catch(ClassCastException e) {
            // expected
        }

        try {
            rawSet.add(new Object());
            fail("Should throw ClassCastException");
        } catch(ClassCastException e) {
            // expected
        }

        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.noneOf(HugeEnum.class);
        result = hugeSet.add(HugeEnum.a);
        assertTrue(result);

        result = hugeSet.add(HugeEnum.a);
        assertFalse(result);

        try {
            hugeSet.add(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        rawSet = hugeSet;
        try {
            rawSet.add(HugeEnumWithInnerClass.b);
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        try {
            rawSet.add(new Object());
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        result = hugeSet.add(HugeEnum.mm);
        assertTrue(result);
        result = hugeSet.add(HugeEnum.mm);
        assertFalse(result);
        assertEquals(2, hugeSet.size());

    }

    /**
     * java.util.EnumSet#addAll(Collection)
     */
    @SuppressWarnings( { "unchecked", "boxing" })
    public void test_addAll_LCollection() {

        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        assertEquals("Size should be 0:", 0, set.size());

        try {
            set.addAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        Set emptySet = EnumSet.noneOf(EmptyEnum.class);
        Enum[] elements = EmptyEnum.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            emptySet.add(elements[i]);
        }
        boolean result = set.addAll(emptySet);
        assertFalse(result);

        Collection<EnumFoo> collection = new ArrayList<EnumFoo>();
        collection.add(EnumFoo.a);
        collection.add(EnumFoo.b);
        result = set.addAll(collection);
        assertTrue("addAll should be successful", result);
        assertEquals("Size should be 2:", 2, set.size());

        set = EnumSet.noneOf(EnumFoo.class);

        Collection rawCollection = new ArrayList<Integer>();
        result = set.addAll(rawCollection);
        assertFalse(result);
        rawCollection.add(1);
        try {
            set.addAll(rawCollection);
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        Set<EnumFoo> fullSet = EnumSet.noneOf(EnumFoo.class);
        fullSet.add(EnumFoo.a);
        fullSet.add(EnumFoo.b);
        result = set.addAll(fullSet);
        assertTrue("addAll should be successful", result);
        assertEquals("Size of set should be 2", 2, set.size());

        try {
            fullSet.addAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        Set fullSetWithSubclass = EnumSet.noneOf(EnumWithInnerClass.class);
        elements = EnumWithInnerClass.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            fullSetWithSubclass.add(elements[i]);
        }
        try {
            set.addAll(fullSetWithSubclass);
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }
        Set<EnumWithInnerClass> setWithSubclass = fullSetWithSubclass;
        result = setWithSubclass.addAll(setWithSubclass);
        assertFalse("Should return false", result);

        Set<EnumWithInnerClass> anotherSetWithSubclass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        elements = EnumWithInnerClass.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            anotherSetWithSubclass.add((EnumWithInnerClass) elements[i]);
        }
        result = setWithSubclass.addAll(anotherSetWithSubclass);
        assertFalse("Should return false", result);

        anotherSetWithSubclass.remove(EnumWithInnerClass.a);
        result = setWithSubclass.addAll(anotherSetWithSubclass);
        assertFalse("Should return false", result);

        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.noneOf(HugeEnum.class);
        assertEquals(0, hugeSet.size());

        try {
            hugeSet.addAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        hugeSet = EnumSet.allOf(HugeEnum.class);
        result = hugeSet.addAll(hugeSet);
        assertFalse(result);

        hugeSet = EnumSet.noneOf(HugeEnum.class);
        Collection<HugeEnum> hugeCollection = new ArrayList<HugeEnum>();
        hugeCollection.add(HugeEnum.a);
        hugeCollection.add(HugeEnum.b);
        result = hugeSet.addAll(hugeCollection);
        assertTrue(result);
        assertEquals(2, set.size());

        hugeSet = EnumSet.noneOf(HugeEnum.class);

        rawCollection = new ArrayList<Integer>();
        result = hugeSet.addAll(rawCollection);
        assertFalse(result);
        rawCollection.add(1);
        try {
            hugeSet.addAll(rawCollection);
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        EnumSet<HugeEnum> aHugeSet = EnumSet.noneOf(HugeEnum.class);
        aHugeSet.add(HugeEnum.a);
        aHugeSet.add(HugeEnum.b);
        result = hugeSet.addAll(aHugeSet);
        assertTrue(result);
        assertEquals(2, hugeSet.size());

        try {
            aHugeSet.addAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        Set hugeSetWithSubclass = EnumSet.allOf(HugeEnumWithInnerClass.class);
        try {
            hugeSet.addAll(hugeSetWithSubclass);
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }
        Set<HugeEnumWithInnerClass> hugeSetWithInnerSubclass = hugeSetWithSubclass;
        result = hugeSetWithInnerSubclass.addAll(hugeSetWithInnerSubclass);
        assertFalse(result);

        Set<HugeEnumWithInnerClass> anotherHugeSetWithSubclass = EnumSet
                .allOf(HugeEnumWithInnerClass.class);
        result = hugeSetWithSubclass.addAll(anotherHugeSetWithSubclass);
        assertFalse(result);

        anotherHugeSetWithSubclass.remove(HugeEnumWithInnerClass.a);
        result = setWithSubclass.addAll(anotherSetWithSubclass);
        assertFalse(result);

    }

    /**
     * java.util.EnumSet#remove(Object)
     */
    @SuppressWarnings("CollectionIncompatibleType")
    public void test_remove_LOject() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        Enum[] elements = EnumFoo.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            set.add((EnumFoo) elements[i]);
        }

        boolean result = set.remove(null);
        assertFalse("'set' does not contain null", result);

        result = set.remove(EnumFoo.a);
        assertTrue("Should return true", result);
        result = set.remove(EnumFoo.a);
        assertFalse("Should return false", result);

        assertEquals("Size of set should be 63:", 63, set.size());

        result = set.remove(EnumWithInnerClass.a);
        assertFalse("Should return false", result);
        result = set.remove(EnumWithInnerClass.f);
        assertFalse("Should return false", result);

        // test enum with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.allOf(HugeEnum.class);

        result = hugeSet.remove(null);
        assertFalse("'set' does not contain null", result);

        result = hugeSet.remove(HugeEnum.a);
        assertTrue("Should return true", result);
        result = hugeSet.remove(HugeEnum.a);
        assertFalse("Should return false", result);

        assertEquals("Size of set should be 64:", 64, hugeSet.size());

        result = hugeSet.remove(HugeEnumWithInnerClass.a);
        assertFalse("Should return false", result);
        result = hugeSet.remove(HugeEnumWithInnerClass.f);
        assertFalse("Should return false", result);
    }

    /**
     * java.util.EnumSet#equals(Object)
     */
    public void test_equals_LObject() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        Enum[] elements = EnumFoo.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            set.add((EnumFoo) elements[i]);
        }

        assertFalse("Should return false", set.equals(null));
        assertFalse(
                "Should return false", set.equals(new Object()));

        Set<EnumFoo> anotherSet = EnumSet.noneOf(EnumFoo.class);
        elements = EnumFoo.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            anotherSet.add((EnumFoo) elements[i]);
        }
        assertTrue("Should return true", set.equals(anotherSet));

        anotherSet.remove(EnumFoo.a);
        assertFalse(
                "Should return false", set.equals(anotherSet));

        Set<EnumWithInnerClass> setWithInnerClass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        elements = EnumWithInnerClass.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            setWithInnerClass.add((EnumWithInnerClass) elements[i]);
        }

        assertFalse(
                "Should return false", set.equals(setWithInnerClass));

        setWithInnerClass.clear();
        set.clear();
        assertTrue("Should be equal", set.equals(setWithInnerClass));

        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.noneOf(HugeEnum.class);
        assertTrue(hugeSet.equals(set));

        hugeSet = EnumSet.allOf(HugeEnum.class);
        assertFalse(hugeSet.equals(null));
        assertFalse(hugeSet.equals(new Object()));

        Set<HugeEnum> anotherHugeSet = EnumSet.allOf(HugeEnum.class);
        anotherHugeSet.remove(HugeEnum.a);
        assertFalse(hugeSet.equals(anotherHugeSet));

        Set<HugeEnumWithInnerClass> hugeSetWithInnerClass = EnumSet
                .allOf(HugeEnumWithInnerClass.class);
        assertFalse(hugeSet.equals(hugeSetWithInnerClass));
        hugeSetWithInnerClass.clear();
        hugeSet.clear();
        assertTrue(hugeSet.equals(hugeSetWithInnerClass));
    }

    /**
     * java.util.EnumSet#clear()
     */
    public void test_clear() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.a);
        set.add(EnumFoo.b);
        assertEquals("Size should be 2", 2, set.size());

        set.clear();

        assertEquals("Size should be 0", 0, set.size());

        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.allOf(HugeEnum.class);
        assertEquals(65, hugeSet.size());

        boolean result = hugeSet.contains(HugeEnum.aa);
        assertTrue(result);

        hugeSet.clear();
        assertEquals(0, hugeSet.size());
        result = hugeSet.contains(HugeEnum.aa);
        assertFalse(result);
    }

    /**
     * java.util.EnumSet#size()
     */
    public void test_size() {
        assertEmpty(EnumSet.noneOf(EnumFoo.class));
        assertSize(2, EnumSet.of(EnumFoo.a, EnumFoo.b));

        // enum type with more than 64 elements
        assertEmpty(EnumSet.noneOf(HugeEnum.class));
        assertSize(2, EnumSet.of(HugeEnum.a, HugeEnum.bb));
        assertSize(65, EnumSet.allOf(HugeEnum.class));
    }

    public void test_size_modification_regular() {
        EnumSet<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        assertEmpty(set);
        set.addAll(Arrays.asList(EnumFoo.a, EnumFoo.b));
        assertSize(2, set);
        set.add(EnumFoo.c);
        assertSize(3, set);
        set.remove(EnumFoo.d);
        assertSize(3, set);
        set.remove(EnumFoo.b);
        assertSize(2, set);
        set.clear();
        assertEmpty(set);
    }

    public void test_size_modification_jumbo() {
        EnumSet<HugeEnum> set = EnumSet.allOf(HugeEnum.class);
        assertSize(65, set);
        set.remove(HugeEnum.b);
        assertSize(64, set);
        set.clear();
        assertEmpty(set);
    }

    private static void assertEmpty(EnumSet<?> set) {
        assertSize(0, set);
    }

    private static void assertSize(int expectedSize, Set<?> set) {
        assertEquals(expectedSize, set.size());
        assertEquals(expectedSize == 0, set.isEmpty());
    }

    /**
     * java.util.EnumSet#complementOf(java.util.EnumSet)
     */
    public void test_ComplementOf_LEnumSet() {

        try {
            EnumSet.complementOf((EnumSet<EnumFoo>) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        EnumSet<EnumWithInnerClass> set = EnumSet
                .noneOf(EnumWithInnerClass.class);
        set.add(EnumWithInnerClass.d);
        set.add(EnumWithInnerClass.e);
        set.add(EnumWithInnerClass.f);

        assertEquals("Size should be 3:", 3, set.size());

        EnumSet<EnumWithInnerClass> complementOfE = EnumSet.complementOf(set);
        assertTrue(set.contains(EnumWithInnerClass.d));
        assertEquals(
                "complementOfE should have size 3", 3, complementOfE.size());
        assertTrue("complementOfE should contain EnumWithSubclass.a:",
                complementOfE.contains(EnumWithInnerClass.a));
        assertTrue("complementOfE should contain EnumWithSubclass.b:",
                complementOfE.contains(EnumWithInnerClass.b));
        assertTrue("complementOfE should contain EnumWithSubclass.c:",
                complementOfE.contains(EnumWithInnerClass.c));

        // test enum type with more than 64 elements
        EnumSet<HugeEnum> hugeSet = EnumSet.noneOf(HugeEnum.class);
        assertEquals(0, hugeSet.size());
        Set<HugeEnum> complementHugeSet = EnumSet.complementOf(hugeSet);
        assertEquals(65, complementHugeSet.size());

        hugeSet.add(HugeEnum.A);
        hugeSet.add(HugeEnum.mm);
        complementHugeSet = EnumSet.complementOf(hugeSet);
        assertEquals(63, complementHugeSet.size());

        try {
            EnumSet.complementOf((EnumSet<HugeEnum>) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * java.util.EnumSet#contains(Object)
     */
    @SuppressWarnings("CollectionIncompatibleType")
    public void test_contains_LObject() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        Enum[] elements = EnumFoo.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            set.add((EnumFoo)elements[i]);
        }
        boolean result = set.contains(null);
        assertFalse("Should not contain null:", result);

        result = set.contains(EnumFoo.a);
        assertTrue("Should contain EnumFoo.a", result);
        result = set.contains(EnumFoo.ll);
        assertTrue("Should contain EnumFoo.ll", result);

        result = set.contains(EnumFoo.b);
        assertTrue("Should contain EnumFoo.b", result);

        result = set.contains(new Object());
        assertFalse("Should not contain Object instance", result);

        result = set.contains(EnumWithInnerClass.a);
        assertFalse("Should not contain EnumWithSubclass.a", result);

        set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.aa);
        set.add(EnumFoo.bb);
        set.add(EnumFoo.cc);

        assertEquals("Size of set should be 3", 3, set.size());
        assertTrue("set should contain EnumFoo.aa", set.contains(EnumFoo.aa));

        Set<EnumWithInnerClass> setWithSubclass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        setWithSubclass.add(EnumWithInnerClass.a);
        setWithSubclass.add(EnumWithInnerClass.b);
        setWithSubclass.add(EnumWithInnerClass.c);
        setWithSubclass.add(EnumWithInnerClass.d);
        setWithSubclass.add(EnumWithInnerClass.e);
        setWithSubclass.add(EnumWithInnerClass.f);
        result = setWithSubclass.contains(EnumWithInnerClass.f);
        assertTrue("Should contain EnumWithSubclass.f", result);

        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.allOf(HugeEnum.class);
        hugeSet.add(HugeEnum.a);
        result = hugeSet.contains(HugeEnum.a);
        assertTrue(result);

        result = hugeSet.contains(HugeEnum.b);
        assertTrue(result);

        result = hugeSet.contains(null);
        assertFalse(result);

        result = hugeSet.contains(HugeEnum.a);
        assertTrue(result);

        result = hugeSet.contains(HugeEnum.ll);
        assertTrue(result);

        result = hugeSet.contains(new Object());
        assertFalse(result);

        result = hugeSet.contains(Enum.class);
        assertFalse(result);

    }

    /**
     * java.util.EnumSet#containsAll(Collection)
     */
    @SuppressWarnings( { "unchecked", "boxing", "CollectionIncompatibleType" })
    public void test_containsAll_LCollection() {
        EnumSet<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        Enum[] elements = EnumFoo.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            set.add((EnumFoo)elements[i]);
        }
        try {
            set.containsAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        EnumSet<EmptyEnum> emptySet = EnumSet.noneOf(EmptyEnum.class);
        elements = EmptyEnum.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            emptySet.add((EmptyEnum)elements[i]);
        }
        boolean result = set.containsAll(emptySet);
        assertTrue("Should return true", result);

        Collection rawCollection = new ArrayList();
        result = set.containsAll(rawCollection);
        assertTrue("Should contain empty collection:", result);

        rawCollection.add(1);
        result = set.containsAll(rawCollection);
        assertFalse("Should return false", result);

        rawCollection.add(EnumWithInnerClass.a);
        result = set.containsAll(rawCollection);
        assertFalse("Should return false", result);

        EnumSet rawSet = EnumSet.noneOf(EnumFoo.class);
        result = set.containsAll(rawSet);
        assertTrue("Should contain empty set", result);

        emptySet = EnumSet.noneOf(EmptyEnum.class);
        result = set.containsAll(emptySet);
        assertTrue("No class cast should be performed on empty set", result);

        Collection<EnumFoo> collection = new ArrayList<EnumFoo>();
        collection.add(EnumFoo.a);
        result = set.containsAll(collection);
        assertTrue("Should contain all elements in collection", result);

        EnumSet<EnumFoo> fooSet = EnumSet.noneOf(EnumFoo.class);
        fooSet.add(EnumFoo.a);
        result = set.containsAll(fooSet);
        assertTrue("Should return true", result);

        set.clear();
        try {
            set.containsAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        Collection<EnumWithInnerClass> collectionWithSubclass = new ArrayList<EnumWithInnerClass>();
        collectionWithSubclass.add(EnumWithInnerClass.a);
        result = set.containsAll(collectionWithSubclass);
        assertFalse("Should return false", result);

        EnumSet<EnumWithInnerClass> setWithSubclass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        setWithSubclass.add(EnumWithInnerClass.a);
        result = set.containsAll(setWithSubclass);
        assertFalse("Should return false", result);

        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.noneOf(HugeEnum.class);
        hugeSet.add(HugeEnum.a);
        hugeSet.add(HugeEnum.b);
        hugeSet.add(HugeEnum.aa);
        hugeSet.add(HugeEnum.bb);
        hugeSet.add(HugeEnum.cc);
        hugeSet.add(HugeEnum.dd);

        Set<HugeEnum> anotherHugeSet = EnumSet.noneOf(HugeEnum.class);
        hugeSet.add(HugeEnum.b);
        hugeSet.add(HugeEnum.cc);
        result = hugeSet.containsAll(anotherHugeSet);
        assertTrue(result);

        try {
            hugeSet.containsAll(null);
            fail("Should throw NullPointerException");
        } catch(NullPointerException e) {
            // expected
        }

        Set<HugeEnumWithInnerClass> hugeSetWithInnerClass = EnumSet
                .noneOf(HugeEnumWithInnerClass.class);
        hugeSetWithInnerClass.add(HugeEnumWithInnerClass.a);
        hugeSetWithInnerClass.add(HugeEnumWithInnerClass.b);
        result = hugeSetWithInnerClass.containsAll(hugeSetWithInnerClass);
        assertTrue(result);
        result = hugeSet.containsAll(hugeSetWithInnerClass);
        assertFalse(result);

        rawCollection = new ArrayList();
        result = hugeSet.containsAll(rawCollection);
        assertTrue("Should contain empty collection:", result);

        rawCollection.add(1);
        result = hugeSet.containsAll(rawCollection);
        assertFalse("Should return false", result);

        rawCollection.add(EnumWithInnerClass.a);
        result = set.containsAll(rawCollection);
        assertFalse("Should return false", result);

        rawSet = EnumSet.noneOf(HugeEnum.class);
        result = hugeSet.containsAll(rawSet);
        assertTrue("Should contain empty set", result);

        EnumSet<HugeEnumWithInnerClass> emptyHugeSet
            = EnumSet.noneOf(HugeEnumWithInnerClass.class);
        result = hugeSet.containsAll(emptyHugeSet);
        assertTrue("No class cast should be performed on empty set", result);

        Collection<HugeEnum> hugeCollection = new ArrayList<HugeEnum>();
        hugeCollection.add(HugeEnum.a);
        result = hugeSet.containsAll(hugeCollection);
        assertTrue("Should contain all elements in collection", result);

        hugeSet.clear();
        try {
            hugeSet.containsAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        Collection<HugeEnumWithInnerClass> hugeCollectionWithSubclass = new ArrayList<HugeEnumWithInnerClass>();
        hugeCollectionWithSubclass.add(HugeEnumWithInnerClass.a);
        result = hugeSet.containsAll(hugeCollectionWithSubclass);
        assertFalse("Should return false", result);

        EnumSet<HugeEnumWithInnerClass> hugeSetWithSubclass = EnumSet
                .noneOf(HugeEnumWithInnerClass.class);
        hugeSetWithSubclass.add(HugeEnumWithInnerClass.a);
        result = hugeSet.containsAll(hugeSetWithSubclass);
        assertFalse("Should return false", result);
    }

    /**
     * java.util.EnumSet#copyOf(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void test_CopyOf_LCollection() {
        try {
            EnumSet.copyOf((Collection) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        Collection collection = new ArrayList();
        try {
            EnumSet.copyOf(collection);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        collection.add(new Object());
        try {
            EnumSet.copyOf(collection);
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        Collection<EnumFoo> enumCollection = new ArrayList<EnumFoo>();
        enumCollection.add(EnumFoo.b);

        EnumSet<EnumFoo> copyOfEnumCollection = EnumSet.copyOf(enumCollection);
        assertEquals("Size of copyOfEnumCollection should be 1:",
                1, copyOfEnumCollection.size());
        assertTrue("copyOfEnumCollection should contain EnumFoo.b:",
                copyOfEnumCollection.contains(EnumFoo.b));

        enumCollection.add(null);
        assertEquals("Size of enumCollection should be 2:",
                2, enumCollection.size());

        try {
            copyOfEnumCollection = EnumSet.copyOf(enumCollection);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        Collection rawEnumCollection = new ArrayList();
        rawEnumCollection.add(EnumFoo.a);
        rawEnumCollection.add(EnumWithInnerClass.a);
        try {
            EnumSet.copyOf(rawEnumCollection);
            fail("Should throw ClassCastException");
        } catch(ClassCastException e) {
            // expected
        }

        // test enum type with more than 64 elements
        Collection<HugeEnum> hugeEnumCollection = new ArrayList<HugeEnum>();
        hugeEnumCollection.add(HugeEnum.b);

        EnumSet<HugeEnum> copyOfHugeEnumCollection = EnumSet.copyOf(hugeEnumCollection);
        assertEquals(1, copyOfHugeEnumCollection.size());
        assertTrue(copyOfHugeEnumCollection.contains(HugeEnum.b));

        hugeEnumCollection.add(null);
        assertEquals(2, hugeEnumCollection.size());

        try {
            copyOfHugeEnumCollection = EnumSet.copyOf(hugeEnumCollection);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        rawEnumCollection = new ArrayList();
        rawEnumCollection.add(HugeEnum.a);
        rawEnumCollection.add(HugeEnumWithInnerClass.a);
        try {
            EnumSet.copyOf(rawEnumCollection);
            fail("Should throw ClassCastException");
        } catch(ClassCastException e) {
            // expected
        }
    }

    /**
     * java.util.EnumSet#copyOf(java.util.EnumSet)
     */
    @SuppressWarnings("unchecked")
    public void test_CopyOf_LEnumSet() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet
                .noneOf(EnumWithInnerClass.class);
        enumSet.add(EnumWithInnerClass.a);
        enumSet.add(EnumWithInnerClass.f);
        EnumSet<EnumWithInnerClass> copyOfE = EnumSet.copyOf(enumSet);
        assertEquals("Size of enumSet and copyOfE should be equal",
                enumSet.size(), copyOfE.size());

        assertTrue("EnumWithSubclass.a should be contained in copyOfE",
                copyOfE.contains(EnumWithInnerClass.a));
        assertTrue("EnumWithSubclass.f should be contained in copyOfE",
                copyOfE.contains(EnumWithInnerClass.f));

        Object[] enumValue = copyOfE.toArray();
        assertSame("enumValue[0] should be identical with EnumWithSubclass.a",
                enumValue[0], EnumWithInnerClass.a);
        assertSame("enumValue[1] should be identical with EnumWithSubclass.f",
                enumValue[1], EnumWithInnerClass.f);

        try {
            EnumSet.copyOf((EnumSet) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        // test enum type with more than 64 elements
        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet
            .noneOf(HugeEnumWithInnerClass.class);
        hugeEnumSet.add(HugeEnumWithInnerClass.a);
        hugeEnumSet.add(HugeEnumWithInnerClass.f);
        EnumSet<HugeEnumWithInnerClass> copyOfHugeEnum = EnumSet.copyOf(hugeEnumSet);
        assertEquals(enumSet.size(), copyOfE.size());

        assertTrue(copyOfHugeEnum.contains(HugeEnumWithInnerClass.a));
        assertTrue(copyOfHugeEnum.contains(HugeEnumWithInnerClass.f));

        Object[] hugeEnumValue = copyOfHugeEnum.toArray();
        assertSame(hugeEnumValue[0], HugeEnumWithInnerClass.a);
        assertSame(hugeEnumValue[1], HugeEnumWithInnerClass.f);
    }

    /**
     * java.util.EnumSet#removeAll(Collection)
     */
    @SuppressWarnings({ "unchecked", "CollectionIncompatibleType" })
    public void test_removeAll_LCollection() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        try {
            set.removeAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        set = EnumSet.allOf(EnumFoo.class);
        assertEquals("Size of set should be 64:", 64, set.size());

        try {
            set.removeAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        Collection<EnumFoo> collection = new ArrayList<EnumFoo>();
        collection.add(EnumFoo.a);

        boolean result = set.removeAll(collection);
        assertTrue("Should return true", result);
        assertEquals("Size of set should be 63", 63, set.size());

        collection = new ArrayList();
        result = set.removeAll(collection);
        assertFalse("Should return false", result);

        Set<EmptyEnum> emptySet = EnumSet.noneOf(EmptyEnum.class);
        result = set.removeAll(emptySet);
        assertFalse("Should return false", result);

        EnumSet<EnumFoo> emptyFooSet = EnumSet.noneOf(EnumFoo.class);
        result = set.removeAll(emptyFooSet);
        assertFalse("Should return false", result);

        emptyFooSet.add(EnumFoo.a);
        result = set.removeAll(emptyFooSet);
        assertFalse("Should return false", result);

        Set<EnumWithInnerClass> setWithSubclass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        result = set.removeAll(setWithSubclass);
        assertFalse("Should return false", result);

        setWithSubclass.add(EnumWithInnerClass.a);
        result = set.removeAll(setWithSubclass);
        assertFalse("Should return false", result);

        Set<EnumFoo> anotherSet = EnumSet.noneOf(EnumFoo.class);
        anotherSet.add(EnumFoo.a);

        set = EnumSet.allOf(EnumFoo.class);
        result = set.removeAll(anotherSet);
        assertTrue("Should return true", result);
        assertEquals("Size of set should be 63:", 63, set.size());

        Set<EnumWithInnerClass> setWithInnerClass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        setWithInnerClass.add(EnumWithInnerClass.a);
        setWithInnerClass.add(EnumWithInnerClass.b);

        Set<EnumWithInnerClass> anotherSetWithInnerClass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        anotherSetWithInnerClass.add(EnumWithInnerClass.c);
        anotherSetWithInnerClass.add(EnumWithInnerClass.d);
        result = anotherSetWithInnerClass.removeAll(setWithInnerClass);
        assertFalse("Should return false", result);

        anotherSetWithInnerClass.add(EnumWithInnerClass.a);
        result = anotherSetWithInnerClass.removeAll(setWithInnerClass);
        assertTrue("Should return true", result);
        assertEquals("Size of anotherSetWithInnerClass should remain 2",
                2, anotherSetWithInnerClass.size());

        anotherSetWithInnerClass.remove(EnumWithInnerClass.c);
        anotherSetWithInnerClass.remove(EnumWithInnerClass.d);
        result = anotherSetWithInnerClass.remove(setWithInnerClass);
        assertFalse("Should return false", result);

        Set rawSet = EnumSet.allOf(EnumWithAllInnerClass.class);
        result = rawSet.removeAll(EnumSet.allOf(EnumFoo.class));
        assertFalse("Should return false", result);

        setWithInnerClass = EnumSet.allOf(EnumWithInnerClass.class);
        anotherSetWithInnerClass = EnumSet.allOf(EnumWithInnerClass.class);
        setWithInnerClass.remove(EnumWithInnerClass.a);
        anotherSetWithInnerClass.remove(EnumWithInnerClass.f);
        result = setWithInnerClass.removeAll(anotherSetWithInnerClass);
        assertTrue("Should return true", result);
        assertEquals("Size of setWithInnerClass should be 1", 1, setWithInnerClass.size());

        result = setWithInnerClass.contains(EnumWithInnerClass.f);
        assertTrue("Should return true", result);

        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.allOf(HugeEnum.class);

        Collection<HugeEnum> hugeCollection = new ArrayList<HugeEnum>();
        hugeCollection.add(HugeEnum.a);

        result = hugeSet.removeAll(hugeCollection);
        assertTrue(result);
        assertEquals(64, hugeSet.size());

        collection = new ArrayList();
        result = hugeSet.removeAll(collection);
        assertFalse(result);

        Set<HugeEnum> emptyHugeSet = EnumSet.noneOf(HugeEnum.class);
        result = hugeSet.removeAll(emptyHugeSet);
        assertFalse(result);

        Set<HugeEnumWithInnerClass> hugeSetWithSubclass = EnumSet
                .noneOf(HugeEnumWithInnerClass.class);
        result = hugeSet.removeAll(hugeSetWithSubclass);
        assertFalse(result);

        hugeSetWithSubclass.add(HugeEnumWithInnerClass.a);
        result = hugeSet.removeAll(hugeSetWithSubclass);
        assertFalse(result);

        Set<HugeEnum> anotherHugeSet = EnumSet.noneOf(HugeEnum.class);
        anotherHugeSet.add(HugeEnum.a);

        hugeSet = EnumSet.allOf(HugeEnum.class);
        result = hugeSet.removeAll(anotherHugeSet);
        assertTrue(result);
        assertEquals(63, set.size());

        Set<HugeEnumWithInnerClass> hugeSetWithInnerClass = EnumSet
                .noneOf(HugeEnumWithInnerClass.class);
        hugeSetWithInnerClass.add(HugeEnumWithInnerClass.a);
        hugeSetWithInnerClass.add(HugeEnumWithInnerClass.b);

        Set<HugeEnumWithInnerClass> anotherHugeSetWithInnerClass = EnumSet
                .noneOf(HugeEnumWithInnerClass.class);
        anotherHugeSetWithInnerClass.add(HugeEnumWithInnerClass.c);
        anotherHugeSetWithInnerClass.add(HugeEnumWithInnerClass.d);
        result = anotherHugeSetWithInnerClass.removeAll(setWithInnerClass);
        assertFalse("Should return false", result);

        anotherHugeSetWithInnerClass.add(HugeEnumWithInnerClass.a);
        result = anotherHugeSetWithInnerClass.removeAll(hugeSetWithInnerClass);
        assertTrue(result);
        assertEquals(2, anotherHugeSetWithInnerClass.size());

        anotherHugeSetWithInnerClass.remove(HugeEnumWithInnerClass.c);
        anotherHugeSetWithInnerClass.remove(HugeEnumWithInnerClass.d);
        result = anotherHugeSetWithInnerClass.remove(hugeSetWithInnerClass);
        assertFalse(result);

        rawSet = EnumSet.allOf(HugeEnumWithInnerClass.class);
        result = rawSet.removeAll(EnumSet.allOf(HugeEnum.class));
        assertFalse(result);

        hugeSetWithInnerClass = EnumSet.allOf(HugeEnumWithInnerClass.class);
        anotherHugeSetWithInnerClass = EnumSet.allOf(HugeEnumWithInnerClass.class);
        hugeSetWithInnerClass.remove(HugeEnumWithInnerClass.a);
        anotherHugeSetWithInnerClass.remove(HugeEnumWithInnerClass.f);
        result = hugeSetWithInnerClass.removeAll(anotherHugeSetWithInnerClass);
        assertTrue(result);
        assertEquals(1, hugeSetWithInnerClass.size());

        result = hugeSetWithInnerClass.contains(HugeEnumWithInnerClass.f);
        assertTrue(result);
    }

    /**
     * java.util.EnumSet#retainAll(Collection)
     */
    @SuppressWarnings({ "unchecked", "CollectionIncompatibleType" })
    public void test_retainAll_LCollection() {
        Set<EnumFoo> set = EnumSet.allOf(EnumFoo.class);

        try {
            set.retainAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        set.clear();
        try {
            set.retainAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        Collection rawCollection = new ArrayList();
        boolean result = set.retainAll(rawCollection);
        assertFalse("Should return false", result);

        rawCollection.add(EnumFoo.a);
        result = set.retainAll(rawCollection);
        assertFalse("Should return false", result);

        rawCollection.add(EnumWithInnerClass.a);
        result = set.retainAll(rawCollection);
        assertFalse("Should return false", result);
        assertEquals("Size of set should be 0:", 0, set.size());

        rawCollection.remove(EnumFoo.a);
        result = set.retainAll(rawCollection);
        assertFalse("Should return false", result);

        Set<EnumFoo> anotherSet = EnumSet.allOf(EnumFoo.class);
        result = set.retainAll(anotherSet);
        assertFalse("Should return false", result);
        assertEquals("Size of set should be 0", 0, set.size());

        Set<EnumWithInnerClass> setWithInnerClass = EnumSet
                .allOf(EnumWithInnerClass.class);
        result = set.retainAll(setWithInnerClass);
        assertFalse("Should return false", result);
        assertEquals("Size of set should be 0", 0, set.size());

        setWithInnerClass = EnumSet.noneOf(EnumWithInnerClass.class);
        result = set.retainAll(setWithInnerClass);
        assertFalse("Should return false", result);

        Set<EmptyEnum> emptySet = EnumSet.allOf(EmptyEnum.class);
        result = set.retainAll(emptySet);
        assertFalse("Should return false", result);

        Set<EnumWithAllInnerClass> setWithAllInnerClass = EnumSet
                .allOf(EnumWithAllInnerClass.class);
        result = set.retainAll(setWithAllInnerClass);
        assertFalse("Should return false", result);

        set.add(EnumFoo.a);
        result = set.retainAll(setWithInnerClass);
        assertTrue("Should return true", result);
        assertEquals("Size of set should be 0", 0, set.size());

        setWithInnerClass = EnumSet.allOf(EnumWithInnerClass.class);
        setWithInnerClass.remove(EnumWithInnerClass.f);
        Set<EnumWithInnerClass> anotherSetWithInnerClass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        anotherSetWithInnerClass.add(EnumWithInnerClass.e);
        anotherSetWithInnerClass.add(EnumWithInnerClass.f);

        result = setWithInnerClass.retainAll(anotherSetWithInnerClass);
        assertTrue("Should return true", result);
        result = setWithInnerClass.contains(EnumWithInnerClass.e);
        assertTrue("Should contain EnumWithInnerClass.e", result);
        result = setWithInnerClass.contains(EnumWithInnerClass.b);
        assertFalse("Should not contain EnumWithInnerClass.b", result);
        assertEquals("Size of set should be 1:", 1, setWithInnerClass.size());

        anotherSetWithInnerClass = EnumSet.allOf(EnumWithInnerClass.class);
        result = setWithInnerClass.retainAll(anotherSetWithInnerClass);

        assertFalse("Return value should be false", result);

        rawCollection = new ArrayList();
        rawCollection.add(EnumWithInnerClass.e);
        rawCollection.add(EnumWithInnerClass.f);
        result = setWithInnerClass.retainAll(rawCollection);
        assertFalse("Should return false", result);

        set = EnumSet.allOf(EnumFoo.class);
        set.remove(EnumFoo.a);
        anotherSet = EnumSet.noneOf(EnumFoo.class);
        anotherSet.add(EnumFoo.a);
        result = set.retainAll(anotherSet);
        assertTrue("Should return true", result);
        assertEquals("size should be 0", 0, set.size());

        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.allOf(HugeEnum.class);

        try {
            hugeSet.retainAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        hugeSet.clear();
        try {
            hugeSet.retainAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        rawCollection = new ArrayList();
        result = hugeSet.retainAll(rawCollection);
        assertFalse(result);

        rawCollection.add(HugeEnum.a);
        result = hugeSet.retainAll(rawCollection);
        assertFalse(result);

        rawCollection.add(HugeEnumWithInnerClass.a);
        result = hugeSet.retainAll(rawCollection);
        assertFalse(result);
        assertEquals(0, set.size());

        rawCollection.remove(HugeEnum.a);
        result = set.retainAll(rawCollection);
        assertFalse(result);

        Set<HugeEnum> anotherHugeSet = EnumSet.allOf(HugeEnum.class);
        result = hugeSet.retainAll(anotherHugeSet);
        assertFalse(result);
        assertEquals(0, hugeSet.size());

        Set<HugeEnumWithInnerClass> hugeSetWithInnerClass = EnumSet
                .allOf(HugeEnumWithInnerClass.class);
        result = hugeSet.retainAll(hugeSetWithInnerClass);
        assertFalse(result);
        assertEquals(0, hugeSet.size());

        hugeSetWithInnerClass = EnumSet.noneOf(HugeEnumWithInnerClass.class);
        result = hugeSet.retainAll(hugeSetWithInnerClass);
        assertFalse(result);

        Set<HugeEnumWithInnerClass> hugeSetWithAllInnerClass = EnumSet
                .allOf(HugeEnumWithInnerClass.class);
        result = hugeSet.retainAll(hugeSetWithAllInnerClass);
        assertFalse(result);

        hugeSet.add(HugeEnum.a);
        result = hugeSet.retainAll(hugeSetWithInnerClass);
        assertTrue(result);
        assertEquals(0, hugeSet.size());

        hugeSetWithInnerClass = EnumSet.allOf(HugeEnumWithInnerClass.class);
        hugeSetWithInnerClass.remove(HugeEnumWithInnerClass.f);
        Set<HugeEnumWithInnerClass> anotherHugeSetWithInnerClass = EnumSet
                .noneOf(HugeEnumWithInnerClass.class);
        anotherHugeSetWithInnerClass.add(HugeEnumWithInnerClass.e);
        anotherHugeSetWithInnerClass.add(HugeEnumWithInnerClass.f);

        result = hugeSetWithInnerClass.retainAll(anotherHugeSetWithInnerClass);
        assertTrue(result);
        result = hugeSetWithInnerClass.contains(HugeEnumWithInnerClass.e);
        assertTrue("Should contain HugeEnumWithInnerClass.e", result);
        result = hugeSetWithInnerClass.contains(HugeEnumWithInnerClass.b);
        assertFalse("Should not contain HugeEnumWithInnerClass.b", result);
        assertEquals("Size of hugeSet should be 1:", 1, hugeSetWithInnerClass.size());

        anotherHugeSetWithInnerClass = EnumSet.allOf(HugeEnumWithInnerClass.class);
        result = hugeSetWithInnerClass.retainAll(anotherHugeSetWithInnerClass);

        assertFalse("Return value should be false", result);

        rawCollection = new ArrayList();
        rawCollection.add(HugeEnumWithInnerClass.e);
        rawCollection.add(HugeEnumWithInnerClass.f);
        result = hugeSetWithInnerClass.retainAll(rawCollection);
        assertFalse(result);

        hugeSet = EnumSet.allOf(HugeEnum.class);
        hugeSet.remove(HugeEnum.a);
        anotherHugeSet = EnumSet.noneOf(HugeEnum.class);
        anotherHugeSet.add(HugeEnum.a);
        result = hugeSet.retainAll(anotherHugeSet);
        assertTrue(result);
        assertEquals(0, hugeSet.size());
    }

    /**
     * java.util.EnumSet#iterator()
     */
    @SuppressWarnings("CollectionIncompatibleType")
    public void test_iterator() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.a);
        set.add(EnumFoo.b);

        Iterator<EnumFoo> iterator = set.iterator();
        Iterator<EnumFoo> anotherIterator = set.iterator();
        assertNotSame("Should not be same", iterator, anotherIterator);
        try {
            iterator.remove();
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expectedd
        }

        assertTrue("Should has next element:", iterator.hasNext());
        assertSame("Should be identical", EnumFoo.a, iterator.next());
        iterator.remove();
        assertTrue("Should has next element:", iterator.hasNext());
        assertSame("Should be identical", EnumFoo.b, iterator.next());
        assertFalse("Should not has next element:", iterator.hasNext());
        assertFalse("Should not has next element:", iterator.hasNext());

        assertEquals("Size should be 1:", 1, set.size());

        try {
            iterator.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
        set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.a);
        iterator = set.iterator();
        assertEquals("Should be equal", EnumFoo.a, iterator.next());
        iterator.remove();
        try {
            iterator.remove();
            fail("Should throw IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        Set<EmptyEnum> emptySet = EnumSet.allOf(EmptyEnum.class);
        Iterator<EmptyEnum> emptyIterator = emptySet.iterator();
        try {
            emptyIterator.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        Set<EnumWithInnerClass> setWithSubclass = EnumSet
                .allOf(EnumWithInnerClass.class);
        setWithSubclass.remove(EnumWithInnerClass.e);
        Iterator<EnumWithInnerClass> iteratorWithSubclass = setWithSubclass
                .iterator();
        assertSame("Should be same", EnumWithInnerClass.a, iteratorWithSubclass.next());

        assertTrue("Should return true", iteratorWithSubclass.hasNext());
        assertSame("Should be same", EnumWithInnerClass.b, iteratorWithSubclass.next());

        setWithSubclass.remove(EnumWithInnerClass.c);
        assertTrue("Should return true", iteratorWithSubclass.hasNext());
        assertSame("Should be same", EnumWithInnerClass.c, iteratorWithSubclass.next());

        assertTrue("Should return true", iteratorWithSubclass.hasNext());
        assertSame("Should be same", EnumWithInnerClass.d, iteratorWithSubclass.next());

        setWithSubclass.add(EnumWithInnerClass.e);
        assertTrue("Should return true", iteratorWithSubclass.hasNext());
        assertSame("Should be same", EnumWithInnerClass.f, iteratorWithSubclass.next());

        set = EnumSet.noneOf(EnumFoo.class);
        iterator = set.iterator();
        try {
            iterator.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        set.add(EnumFoo.a);
        iterator = set.iterator();
        assertEquals("Should return EnumFoo.a", EnumFoo.a, iterator.next());
        assertEquals("Size of set should be 1", 1, set.size());
        iterator.remove();
        assertEquals("Size of set should be 0", 0, set.size());
        assertFalse("Should return false", set.contains(EnumFoo.a));

        set.add(EnumFoo.a);
        set.add(EnumFoo.b);
        iterator = set.iterator();
        assertEquals("Should be equals", EnumFoo.a, iterator.next());
        iterator.remove();
        try {
            iterator.remove();
            fail("Should throw IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        assertTrue("Should have next element", iterator.hasNext());
        try {
            iterator.remove();
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        assertEquals("Size of set should be 1", 1, set.size());
        assertTrue("Should have next element", iterator.hasNext());
        assertEquals("Should return EnumFoo.b", EnumFoo.b, iterator.next());
        set.remove(EnumFoo.b);
        assertEquals("Size of set should be 0", 0, set.size());
        iterator.remove();
        assertFalse("Should return false", set.contains(EnumFoo.a));

        // RI's bug, EnumFoo.b should not exist at the moment.
        if (!disableRIBugs) {
            assertFalse("Should return false", set.contains(EnumFoo.b));
        }

        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.noneOf(HugeEnum.class);
        hugeSet.add(HugeEnum.a);
        hugeSet.add(HugeEnum.b);

        Iterator<HugeEnum> hIterator = hugeSet.iterator();
        Iterator<HugeEnum> anotherHugeIterator = hugeSet.iterator();
        assertNotSame(hIterator, anotherHugeIterator);
        try {
            hIterator.remove();
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expectedd
        }

        assertTrue(hIterator.hasNext());
        assertSame(HugeEnum.a, hIterator.next());
        hIterator.remove();
        assertTrue(hIterator.hasNext());
        assertSame(HugeEnum.b, hIterator.next());
        assertFalse(hIterator.hasNext());
        assertFalse(hIterator.hasNext());

        assertEquals(1, hugeSet.size());

        try {
            hIterator.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        Set<HugeEnumWithInnerClass> hugeSetWithSubclass = EnumSet
                .allOf(HugeEnumWithInnerClass.class);
        hugeSetWithSubclass.remove(HugeEnumWithInnerClass.e);
        Iterator<HugeEnumWithInnerClass> hugeIteratorWithSubclass = hugeSetWithSubclass
                .iterator();
        assertSame(HugeEnumWithInnerClass.a, hugeIteratorWithSubclass.next());

        assertTrue(hugeIteratorWithSubclass.hasNext());
        assertSame(HugeEnumWithInnerClass.b, hugeIteratorWithSubclass.next());

        setWithSubclass.remove(HugeEnumWithInnerClass.c);
        assertTrue(hugeIteratorWithSubclass.hasNext());
        assertSame(HugeEnumWithInnerClass.c, hugeIteratorWithSubclass.next());

        assertTrue(hugeIteratorWithSubclass.hasNext());
        assertSame(HugeEnumWithInnerClass.d, hugeIteratorWithSubclass.next());

        hugeSetWithSubclass.add(HugeEnumWithInnerClass.e);
        assertTrue(hugeIteratorWithSubclass.hasNext());
        assertSame(HugeEnumWithInnerClass.f, hugeIteratorWithSubclass.next());

        hugeSet = EnumSet.noneOf(HugeEnum.class);
        hIterator = hugeSet.iterator();
        try {
            hIterator.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        hugeSet.add(HugeEnum.a);
        hIterator = hugeSet.iterator();
        assertEquals(HugeEnum.a, hIterator.next());
        assertEquals(1, hugeSet.size());
        hIterator.remove();
        assertEquals(0, hugeSet.size());
        assertFalse(hugeSet.contains(HugeEnum.a));

        hugeSet.add(HugeEnum.a);
        hugeSet.add(HugeEnum.b);
        hIterator = hugeSet.iterator();
        hIterator.next();
        hIterator.remove();

        assertTrue(hIterator.hasNext());
        try {
            hIterator.remove();
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        assertEquals(1, hugeSet.size());
        assertTrue(hIterator.hasNext());
        assertEquals(HugeEnum.b, hIterator.next());
        hugeSet.remove(HugeEnum.b);
        assertEquals(0, hugeSet.size());
        hIterator.remove();
        assertFalse(hugeSet.contains(HugeEnum.a));
        // RI's bug, EnumFoo.b should not exist at the moment.
        if(!disableRIBugs) {
            assertFalse("Should return false", set.contains(EnumFoo.b));
        }
    }

    /**
     * java.util.EnumSet#of(E)
     */
    public void test_Of_E() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a);
        assertEquals("enumSet should have length 1:", 1, enumSet.size());

        assertTrue("enumSet should contain EnumWithSubclass.a:",
                enumSet.contains(EnumWithInnerClass.a));

        try {
            EnumSet.of((EnumWithInnerClass) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        // test enum type with more than 64 elements
        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a);
        assertEquals(1, hugeEnumSet.size());

        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.a));
    }

    /**
     * java.util.EnumSet#of(E, E)
     */
    public void test_Of_EE() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a,
                EnumWithInnerClass.b);
        assertEquals("enumSet should have length 2:", 2, enumSet.size());

        assertTrue("enumSet should contain EnumWithSubclass.a:",
                enumSet.contains(EnumWithInnerClass.a));
        assertTrue("enumSet should contain EnumWithSubclass.b:",
                enumSet.contains(EnumWithInnerClass.b));

        try {
            EnumSet.of((EnumWithInnerClass) null, EnumWithInnerClass.a);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        try {
            EnumSet.of( EnumWithInnerClass.a, (EnumWithInnerClass) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        try {
            EnumSet.of( (EnumWithInnerClass) null, (EnumWithInnerClass) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        enumSet = EnumSet.of(EnumWithInnerClass.a, EnumWithInnerClass.a);
        assertEquals("Size of enumSet should be 1",
                1, enumSet.size());

        // test enum type with more than 64 elements
        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a,
                HugeEnumWithInnerClass.b);
        assertEquals(2, hugeEnumSet.size());

        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.a));
        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.b));

        try {
            EnumSet.of((HugeEnumWithInnerClass) null, HugeEnumWithInnerClass.a);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        try {
            EnumSet.of( HugeEnumWithInnerClass.a, (HugeEnumWithInnerClass) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        try {
            EnumSet.of( (HugeEnumWithInnerClass) null, (HugeEnumWithInnerClass) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a, HugeEnumWithInnerClass.a);
        assertEquals(1, hugeEnumSet.size());
    }

    /**
     * java.util.EnumSet#of(E, E, E)
     */
    public void test_Of_EEE() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a,
                EnumWithInnerClass.b, EnumWithInnerClass.c);
        assertEquals("Size of enumSet should be 3:", 3, enumSet.size());

        assertTrue(
                "enumSet should contain EnumWithSubclass.a:", enumSet.contains(EnumWithInnerClass.a));
        assertTrue("Should return true", enumSet.contains(EnumWithInnerClass.c));

        try {
            EnumSet.of((EnumWithInnerClass) null, null, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        enumSet = EnumSet.of(EnumWithInnerClass.a, EnumWithInnerClass.b,
                EnumWithInnerClass.b);
        assertEquals("enumSet should contain 2 elements:", 2, enumSet.size());

        // test enum type with more than 64 elements
        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a,
                HugeEnumWithInnerClass.b, HugeEnumWithInnerClass.c);
        assertEquals(3, hugeEnumSet.size());

        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.a));
        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.c));

        try {
            EnumSet.of((HugeEnumWithInnerClass) null, null, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a, HugeEnumWithInnerClass.b,
                HugeEnumWithInnerClass.b);
        assertEquals(2, hugeEnumSet.size());
    }

    /**
     * java.util.EnumSet#of(E, E, E, E)
     */
    public void test_Of_EEEE() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a,
                EnumWithInnerClass.b, EnumWithInnerClass.c,
                EnumWithInnerClass.d);
        assertEquals("Size of enumSet should be 4", 4, enumSet.size());

        assertTrue(
                "enumSet should contain EnumWithSubclass.a:", enumSet.contains(EnumWithInnerClass.a));
        assertTrue("enumSet should contain EnumWithSubclass.d:", enumSet
                .contains(EnumWithInnerClass.d));

        try {
            EnumSet.of((EnumWithInnerClass) null, null, null, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        // test enum type with more than 64 elements
        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a,
                HugeEnumWithInnerClass.b, HugeEnumWithInnerClass.c,
                HugeEnumWithInnerClass.d);
        assertEquals(4, hugeEnumSet.size());

        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.a));
        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.d));

        try {
            EnumSet.of((HugeEnumWithInnerClass) null, null, null, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * java.util.EnumSet#of(E, E, E, E, E)
     */
    public void test_Of_EEEEE() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a,
                EnumWithInnerClass.b, EnumWithInnerClass.c,
                EnumWithInnerClass.d, EnumWithInnerClass.e);
        assertEquals("Size of enumSet should be 5:", 5, enumSet.size());

        assertTrue("Should return true", enumSet.contains(EnumWithInnerClass.a));
        assertTrue("Should return true", enumSet.contains(EnumWithInnerClass.e));

        try {
            EnumSet.of((EnumWithInnerClass) null, null, null, null, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        // test enum with more than 64 elements
        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a,
                HugeEnumWithInnerClass.b, HugeEnumWithInnerClass.c,
                HugeEnumWithInnerClass.d, HugeEnumWithInnerClass.e);
        assertEquals(5, hugeEnumSet.size());

        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.a));
        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.e));

        try {
            EnumSet.of((HugeEnumWithInnerClass) null, null, null, null, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * java.util.EnumSet#of(E, E...)
     */
    public void test_Of_EEArray() {
        EnumWithInnerClass[] enumArray = new EnumWithInnerClass[] {
                EnumWithInnerClass.b, EnumWithInnerClass.c };
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a,
                enumArray);
        assertEquals("Should be equal", 3, enumSet.size());

        assertTrue("Should return true", enumSet.contains(EnumWithInnerClass.a));
        assertTrue("Should return true", enumSet.contains(EnumWithInnerClass.c));

        try {
            EnumSet.of(EnumWithInnerClass.a, (EnumWithInnerClass[])null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        EnumFoo[] foos = {EnumFoo.a, EnumFoo.c, EnumFoo.d};
        EnumSet<EnumFoo> set = EnumSet.of(EnumFoo.c, foos);
        assertEquals("size of set should be 1", 3, set.size());
        assertTrue("Should contain EnumFoo.a", set.contains(EnumFoo.a));
        assertTrue("Should contain EnumFoo.c", set.contains(EnumFoo.c));
        assertTrue("Should contain EnumFoo.d", set.contains(EnumFoo.d));

        // test enum type with more than 64 elements
        HugeEnumWithInnerClass[] hugeEnumArray = new HugeEnumWithInnerClass[] {
                HugeEnumWithInnerClass.b, HugeEnumWithInnerClass.c };
        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a,
                hugeEnumArray);
        assertEquals(3, hugeEnumSet.size());

        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.a));
        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.c));

        try {
            EnumSet.of(HugeEnumWithInnerClass.a, (HugeEnumWithInnerClass[])null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }

        HugeEnumWithInnerClass[] huges = {HugeEnumWithInnerClass.a, HugeEnumWithInnerClass.c, HugeEnumWithInnerClass.d};
        EnumSet<HugeEnumWithInnerClass> hugeSet = EnumSet.of(HugeEnumWithInnerClass.c, huges);
        assertEquals(3, hugeSet.size());
        assertTrue(hugeSet.contains(HugeEnumWithInnerClass.a));
        assertTrue(hugeSet.contains(HugeEnumWithInnerClass.c));
        assertTrue(hugeSet.contains(HugeEnumWithInnerClass.d));
    }

    /**
     * java.util.EnumSet#range(E, E)
     */
    public void test_Range_EE() {
        try {
            EnumSet.range(EnumWithInnerClass.c, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(null, EnumWithInnerClass.c);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(null, (EnumWithInnerClass) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(EnumWithInnerClass.b, EnumWithInnerClass.a);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        EnumSet<EnumWithInnerClass> enumSet = EnumSet.range(
                EnumWithInnerClass.a, EnumWithInnerClass.a);
        assertEquals("Size of enumSet should be 1", 1, enumSet.size());

        enumSet = EnumSet.range(
                EnumWithInnerClass.a, EnumWithInnerClass.c);
        assertEquals("Size of enumSet should be 3", 3, enumSet.size());

        // test enum with more than 64 elements
        try {
            EnumSet.range(HugeEnumWithInnerClass.c, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(null, HugeEnumWithInnerClass.c);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(null, (HugeEnumWithInnerClass) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(HugeEnumWithInnerClass.b, HugeEnumWithInnerClass.a);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet.range(
                HugeEnumWithInnerClass.a, HugeEnumWithInnerClass.a);
        assertEquals(1, hugeEnumSet.size());

        hugeEnumSet = EnumSet.range(
                HugeEnumWithInnerClass.c, HugeEnumWithInnerClass.aa);
        assertEquals(51, hugeEnumSet.size());

        hugeEnumSet = EnumSet.range(
                HugeEnumWithInnerClass.a, HugeEnumWithInnerClass.mm);
        assertEquals(65, hugeEnumSet.size());

        hugeEnumSet = EnumSet.range(
                HugeEnumWithInnerClass.b, HugeEnumWithInnerClass.mm);
        assertEquals(64, hugeEnumSet.size());
    }

    /**
     * java.util.EnumSet#clone()
     */
    public void test_Clone() {
        EnumSet<EnumFoo> enumSet = EnumSet.allOf(EnumFoo.class);
        EnumSet<EnumFoo> clonedEnumSet = enumSet.clone();
        assertEquals(enumSet, clonedEnumSet);
        assertNotSame(enumSet, clonedEnumSet);
        assertTrue(clonedEnumSet.contains(EnumFoo.a));
        assertTrue(clonedEnumSet.contains(EnumFoo.b));
        assertEquals(64, clonedEnumSet.size());

        // test enum type with more than 64 elements
        EnumSet<HugeEnum> hugeEnumSet = EnumSet.allOf(HugeEnum.class);
        EnumSet<HugeEnum> hugeClonedEnumSet = hugeEnumSet.clone();
        assertEquals(hugeEnumSet, hugeClonedEnumSet);
        assertNotSame(hugeEnumSet, hugeClonedEnumSet);
        assertTrue(hugeClonedEnumSet.contains(HugeEnum.a));
        assertTrue(hugeClonedEnumSet.contains(HugeEnum.b));
        assertEquals(65, hugeClonedEnumSet.size());

        hugeClonedEnumSet.remove(HugeEnum.a);
        assertEquals(64, hugeClonedEnumSet.size());
        assertFalse(hugeClonedEnumSet.contains(HugeEnum.a));
        assertEquals(65, hugeEnumSet.size());
        assertTrue(hugeEnumSet.contains(HugeEnum.a));
    }

    /**
     * java.util.EnumSet#Serialization()
     */
    public void test_serialization() throws Exception {
        EnumSet<EnumFoo> set = EnumSet.allOf(EnumFoo.class);
        SerializationTest.verifySelf(set);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    @SuppressWarnings( { "unchecked", "boxing" })
    public void testSerializationCompatibility() throws Exception {
        EnumSet<EnumFoo> set = EnumSet.allOf(EnumFoo.class);
        SerializationTest.verifyGolden(this, set);
    }
}
