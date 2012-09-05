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

package org.apache.harmony.luni.tests.java.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class EnumSetTest extends TestCase {
    
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
     * @tests java.util.EnumSet#noneOf(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public void test_NoneOf_LClass() {
        try {
            EnumSet.noneOf((Class) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.noneOf(Enum.class);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch (ClassCastException cce) {
            // expected
        }

        Class<EnumWithAllInnerClass> c = (Class<EnumWithAllInnerClass>) EnumWithAllInnerClass.a
                .getClass();
        try {
            EnumSet.noneOf(c);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
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
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch (ClassCastException e) {
            // expected
        }

        EnumSet<HugeEnumWithInnerClass> hugeSetWithInnerClass = EnumSet
            .noneOf(HugeEnumWithInnerClass.class);
        assertNotNull(hugeSetWithInnerClass);
    }
    
    /**
     * @tests java.util.HugeEnumSet#iterator()
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
     * @tests java.util.EnumSet#allOf(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public void test_AllOf_LClass() {
        try {
            EnumSet.allOf((Class) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.allOf(Enum.class);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch (ClassCastException cce) {
            // expected
        }

        EnumSet<EnumFoo> enumSet = EnumSet.allOf(EnumFoo.class);
        assertEquals("Size of enumSet should be 64", 64, enumSet.size()); //$NON-NLS-1$

        assertFalse(
                "enumSet should not contain null value", enumSet.contains(null)); //$NON-NLS-1$
        assertTrue(
                "enumSet should contain EnumFoo.a", enumSet.contains(EnumFoo.a)); //$NON-NLS-1$
        assertTrue(
                "enumSet should contain EnumFoo.b", enumSet.contains(EnumFoo.b)); //$NON-NLS-1$

        enumSet.add(EnumFoo.a);
        assertEquals("Should be equal", 64, enumSet.size()); //$NON-NLS-1$

        EnumSet<EnumFoo> anotherSet = EnumSet.allOf(EnumFoo.class);
        assertEquals("Should be equal", enumSet, anotherSet); //$NON-NLS-1$
        assertNotSame("Should not be identical", enumSet, anotherSet); //$NON-NLS-1$
        
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
     * @tests java.util.EnumSet#add(E)
     */
    @SuppressWarnings("unchecked")
    public void test_add_E() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.a);
        set.add(EnumFoo.b);
        
        try {
            set.add(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }
        
        // test enum type with more than 64 elements
        Set rawSet = set;
        try {
            rawSet.add(HugeEnumWithInnerClass.b);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch (ClassCastException e) {
            // expected
        }

        set.clear();
        try {
            set.add(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        boolean result = set.add(EnumFoo.a);
        assertEquals("Size should be 1:", 1, set.size()); //$NON-NLS-1$
        assertTrue("Return value should be true", result); //$NON-NLS-1$

        result = set.add(EnumFoo.a);
        assertEquals("Size should be 1:", 1, set.size()); //$NON-NLS-1$
        assertFalse("Return value should be false", result); //$NON-NLS-1$

        set.add(EnumFoo.b);
        assertEquals("Size should be 2:", 2, set.size()); //$NON-NLS-1$
        
        rawSet = set;
        try {
            rawSet.add(EnumWithAllInnerClass.a);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch(ClassCastException e) {
            // expected
        }
        
        try {
            rawSet.add(EnumWithInnerClass.a);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch(ClassCastException e) {
            // expected
        }
        
        try {
            rawSet.add(new Object());
            fail("Should throw ClassCastException"); //$NON-NLS-1$
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
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        rawSet = hugeSet;
        try {
            rawSet.add(HugeEnumWithInnerClass.b);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch (ClassCastException e) {
            // expected
        }

        try {
            rawSet.add(new Object());
            fail("Should throw ClassCastException"); //$NON-NLS-1$
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
     * @tests java.util.EnumSet#addAll(Collection)
     */
    @SuppressWarnings( { "unchecked", "boxing" })
    public void test_addAll_LCollection() {

        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        assertEquals("Size should be 0:", 0, set.size()); //$NON-NLS-1$

        try {
            set.addAll(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
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
        assertTrue("addAll should be successful", result); //$NON-NLS-1$
        assertEquals("Size should be 2:", 2, set.size()); //$NON-NLS-1$

        set = EnumSet.noneOf(EnumFoo.class);

        Collection rawCollection = new ArrayList<Integer>();
        result = set.addAll(rawCollection);
        assertFalse(result);
        rawCollection.add(1);
        try {
            set.addAll(rawCollection);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch (ClassCastException e) {
            // expected
        }

        Set<EnumFoo> fullSet = EnumSet.noneOf(EnumFoo.class);
        fullSet.add(EnumFoo.a);
        fullSet.add(EnumFoo.b);
        result = set.addAll(fullSet);
        assertTrue("addAll should be successful", result); //$NON-NLS-1$
        assertEquals("Size of set should be 2", 2, set.size()); //$NON-NLS-1$

        try {
            fullSet.addAll(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
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
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch (ClassCastException e) {
            // expected
        }
        Set<EnumWithInnerClass> setWithSubclass = fullSetWithSubclass;
        result = setWithSubclass.addAll(setWithSubclass);
        assertFalse("Should return false", result); //$NON-NLS-1$

        Set<EnumWithInnerClass> anotherSetWithSubclass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        elements = EnumWithInnerClass.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            anotherSetWithSubclass.add((EnumWithInnerClass) elements[i]);
        }
        result = setWithSubclass.addAll(anotherSetWithSubclass);
        assertFalse("Should return false", result); //$NON-NLS-1$

        anotherSetWithSubclass.remove(EnumWithInnerClass.a);
        result = setWithSubclass.addAll(anotherSetWithSubclass);
        assertFalse("Should return false", result); //$NON-NLS-1$
        
        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.noneOf(HugeEnum.class);
        assertEquals(0, hugeSet.size());

        try {
            hugeSet.addAll(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
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
            fail("Should throw ClassCastException"); //$NON-NLS-1$
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
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        Set hugeSetWithSubclass = EnumSet.allOf(HugeEnumWithInnerClass.class);
        try {
            hugeSet.addAll(hugeSetWithSubclass);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
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
     * @tests java.util.EnumSet#remove(Object)
     */
    public void test_remove_LObject() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        Enum[] elements = EnumFoo.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            set.add((EnumFoo) elements[i]);
        }
        
        boolean result = set.remove(null);
        assertFalse("'set' does not contain null", result); //$NON-NLS-1$

        result = set.remove(EnumFoo.a);
        assertTrue("Should return true", result); //$NON-NLS-1$
        result = set.remove(EnumFoo.a);
        assertFalse("Should return false", result); //$NON-NLS-1$

        assertEquals("Size of set should be 63:", 63, set.size()); //$NON-NLS-1$

        result = set.remove(EnumWithInnerClass.a);
        assertFalse("Should return false", result); //$NON-NLS-1$
        result = set.remove(EnumWithInnerClass.f);
        assertFalse("Should return false", result); //$NON-NLS-1$
        
        // test enum with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.allOf(HugeEnum.class);
        
        result = hugeSet.remove(null);
        assertFalse("'set' does not contain null", result); //$NON-NLS-1$

        result = hugeSet.remove(HugeEnum.a);
        assertTrue("Should return true", result); //$NON-NLS-1$
        result = hugeSet.remove(HugeEnum.a);
        assertFalse("Should return false", result); //$NON-NLS-1$

        assertEquals("Size of set should be 64:", 64, hugeSet.size()); //$NON-NLS-1$

        result = hugeSet.remove(HugeEnumWithInnerClass.a);
        assertFalse("Should return false", result); //$NON-NLS-1$
        result = hugeSet.remove(HugeEnumWithInnerClass.f);
        assertFalse("Should return false", result); //$NON-NLS-1$
    }
    
    /**
     * @tests java.util.EnumSet#equals(Object)
     */
    public void test_equals_LObject() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        Enum[] elements = EnumFoo.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            set.add((EnumFoo) elements[i]);
        }
        
        assertFalse("Should return false", set.equals(null)); //$NON-NLS-1$
        assertFalse(
                "Should return false", set.equals(new Object())); //$NON-NLS-1$

        Set<EnumFoo> anotherSet = EnumSet.noneOf(EnumFoo.class);
        elements = EnumFoo.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            anotherSet.add((EnumFoo) elements[i]);
        }
        assertTrue("Should return true", set.equals(anotherSet)); //$NON-NLS-1$
        
        anotherSet.remove(EnumFoo.a);
        assertFalse(
                "Should return false", set.equals(anotherSet)); //$NON-NLS-1$

        Set<EnumWithInnerClass> setWithInnerClass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        elements = EnumWithInnerClass.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            setWithInnerClass.add((EnumWithInnerClass) elements[i]);
        }
        
        assertFalse(
                "Should return false", set.equals(setWithInnerClass)); //$NON-NLS-1$

        setWithInnerClass.clear();
        set.clear();
        assertTrue("Should be equal", set.equals(setWithInnerClass)); //$NON-NLS-1$
        
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
     * @tests java.util.EnumSet#clear()
     */
    public void test_clear() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.a);
        set.add(EnumFoo.b);
        assertEquals("Size should be 2", 2, set.size()); //$NON-NLS-1$

        set.clear();

        assertEquals("Size should be 0", 0, set.size()); //$NON-NLS-1$
        
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
     * @tests java.util.EnumSet#size()
     */
    public void test_size() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.a);
        set.add(EnumFoo.b);
        assertEquals("Size should be 2", 2, set.size()); //$NON-NLS-1$
        
        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.noneOf(HugeEnum.class);
        hugeSet.add(HugeEnum.a);
        hugeSet.add(HugeEnum.bb);
        assertEquals("Size should be 2", 2, hugeSet.size()); //$NON-NLS-1$
    }
    
    /**
     * @tests java.util.EnumSet#complementOf(java.util.EnumSet)
     */
    public void test_ComplementOf_LEnumSet() {

        try {
            EnumSet.complementOf((EnumSet<EnumFoo>) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }

        EnumSet<EnumWithInnerClass> set = EnumSet
                .noneOf(EnumWithInnerClass.class);
        set.add(EnumWithInnerClass.d);
        set.add(EnumWithInnerClass.e);
        set.add(EnumWithInnerClass.f);

        assertEquals("Size should be 3:", 3, set.size()); //$NON-NLS-1$

        EnumSet<EnumWithInnerClass> complementOfE = EnumSet.complementOf(set);
        assertTrue(set.contains(EnumWithInnerClass.d));
        assertEquals(
                "complementOfE should have size 3", 3, complementOfE.size()); //$NON-NLS-1$
        assertTrue("complementOfE should contain EnumWithSubclass.a:", //$NON-NLS-1$ 
                complementOfE.contains(EnumWithInnerClass.a));
        assertTrue("complementOfE should contain EnumWithSubclass.b:", //$NON-NLS-1$
                complementOfE.contains(EnumWithInnerClass.b));
        assertTrue("complementOfE should contain EnumWithSubclass.c:", //$NON-NLS-1$
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
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
    }
    
    /**
     * @tests java.util.EnumSet#contains(Object)
     */
    public void test_contains_LObject() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        Enum[] elements = EnumFoo.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            set.add((EnumFoo)elements[i]);
        }
        boolean result = set.contains(null);
        assertFalse("Should not contain null:", result); //$NON-NLS-1$

        result = set.contains(EnumFoo.a);
        assertTrue("Should contain EnumFoo.a", result); //$NON-NLS-1$
        result = set.contains(EnumFoo.ll);
        assertTrue("Should contain EnumFoo.ll", result); //$NON-NLS-1$

        result = set.contains(EnumFoo.b);
        assertTrue("Should contain EnumFoo.b", result); //$NON-NLS-1$

        result = set.contains(new Object());
        assertFalse("Should not contain Object instance", result); //$NON-NLS-1$

        result = set.contains(EnumWithInnerClass.a);
        assertFalse("Should not contain EnumWithSubclass.a", result); //$NON-NLS-1$
        
        set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.aa);
        set.add(EnumFoo.bb);
        set.add(EnumFoo.cc);
        
        assertEquals("Size of set should be 3", 3, set.size()); //$NON-NLS-1$
        assertTrue("set should contain EnumFoo.aa", set.contains(EnumFoo.aa)); //$NON-NLS-1$

        Set<EnumWithInnerClass> setWithSubclass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        setWithSubclass.add(EnumWithInnerClass.a);
        setWithSubclass.add(EnumWithInnerClass.b);
        setWithSubclass.add(EnumWithInnerClass.c);
        setWithSubclass.add(EnumWithInnerClass.d);
        setWithSubclass.add(EnumWithInnerClass.e);
        setWithSubclass.add(EnumWithInnerClass.f);
        result = setWithSubclass.contains(EnumWithInnerClass.f);
        assertTrue("Should contain EnumWithSubclass.f", result); //$NON-NLS-1$
        
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
     * @tests java.util.EnumSet#containsAll(Collection)
     */
    @SuppressWarnings( { "unchecked", "boxing" })
    public void test_containsAll_LCollection() {
        EnumSet<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        Enum[] elements = EnumFoo.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            set.add((EnumFoo)elements[i]);
        }
        try {
            set.containsAll(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        EnumSet<EmptyEnum> emptySet = EnumSet.noneOf(EmptyEnum.class);
        elements = EmptyEnum.class.getEnumConstants();
        for(int i = 0; i < elements.length; i++) {
            emptySet.add((EmptyEnum)elements[i]);
        }
        boolean result = set.containsAll(emptySet);
        assertTrue("Should return true", result); //$NON-NLS-1$

        Collection rawCollection = new ArrayList();
        result = set.containsAll(rawCollection);
        assertTrue("Should contain empty collection:", result); //$NON-NLS-1$

        rawCollection.add(1);
        result = set.containsAll(rawCollection);
        assertFalse("Should return false", result); //$NON-NLS-1$

        rawCollection.add(EnumWithInnerClass.a);
        result = set.containsAll(rawCollection);
        assertFalse("Should return false", result); //$NON-NLS-1$

        EnumSet rawSet = EnumSet.noneOf(EnumFoo.class);
        result = set.containsAll(rawSet);
        assertTrue("Should contain empty set", result); //$NON-NLS-1$

        emptySet = EnumSet.noneOf(EmptyEnum.class);
        result = set.containsAll(emptySet);
        assertTrue("No class cast should be performed on empty set", result); //$NON-NLS-1$

        Collection<EnumFoo> collection = new ArrayList<EnumFoo>();
        collection.add(EnumFoo.a);
        result = set.containsAll(collection);
        assertTrue("Should contain all elements in collection", result); //$NON-NLS-1$

        EnumSet<EnumFoo> fooSet = EnumSet.noneOf(EnumFoo.class);
        fooSet.add(EnumFoo.a);
        result = set.containsAll(fooSet);
        assertTrue("Should return true", result); //$NON-NLS-1$

        set.clear();
        try {
            set.containsAll(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        Collection<EnumWithInnerClass> collectionWithSubclass = new ArrayList<EnumWithInnerClass>();
        collectionWithSubclass.add(EnumWithInnerClass.a);
        result = set.containsAll(collectionWithSubclass);
        assertFalse("Should return false", result); //$NON-NLS-1$

        EnumSet<EnumWithInnerClass> setWithSubclass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        setWithSubclass.add(EnumWithInnerClass.a);
        result = set.containsAll(setWithSubclass);
        assertFalse("Should return false", result); //$NON-NLS-1$
        
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
            fail("Should throw NullPointerException"); //$NON-NLS-1$
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
        assertTrue("Should contain empty collection:", result); //$NON-NLS-1$
        
        rawCollection.add(1);
        result = hugeSet.containsAll(rawCollection);
        assertFalse("Should return false", result); //$NON-NLS-1$
        
        rawCollection.add(EnumWithInnerClass.a);
        result = set.containsAll(rawCollection);
        assertFalse("Should return false", result); //$NON-NLS-1$

        rawSet = EnumSet.noneOf(HugeEnum.class);
        result = hugeSet.containsAll(rawSet);
        assertTrue("Should contain empty set", result); //$NON-NLS-1$

        EnumSet<HugeEnumWithInnerClass> emptyHugeSet 
            = EnumSet.noneOf(HugeEnumWithInnerClass.class);
        result = hugeSet.containsAll(emptyHugeSet);
        assertTrue("No class cast should be performed on empty set", result); //$NON-NLS-1$

        Collection<HugeEnum> hugeCollection = new ArrayList<HugeEnum>();
        hugeCollection.add(HugeEnum.a);
        result = hugeSet.containsAll(hugeCollection);
        assertTrue("Should contain all elements in collection", result); //$NON-NLS-1$

        hugeSet.clear();
        try {
            hugeSet.containsAll(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        Collection<HugeEnumWithInnerClass> hugeCollectionWithSubclass = new ArrayList<HugeEnumWithInnerClass>();
        hugeCollectionWithSubclass.add(HugeEnumWithInnerClass.a);
        result = hugeSet.containsAll(hugeCollectionWithSubclass);
        assertFalse("Should return false", result); //$NON-NLS-1$

        EnumSet<HugeEnumWithInnerClass> hugeSetWithSubclass = EnumSet
                .noneOf(HugeEnumWithInnerClass.class);
        hugeSetWithSubclass.add(HugeEnumWithInnerClass.a);
        result = hugeSet.containsAll(hugeSetWithSubclass);
        assertFalse("Should return false", result); //$NON-NLS-1$
    }
    
    /**
     * @tests java.util.EnumSet#copyOf(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void test_CopyOf_LCollection() {
        try {
            EnumSet.copyOf((Collection) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }

        Collection collection = new ArrayList();
        try {
            EnumSet.copyOf(collection);
            fail("Should throw IllegalArgumentException"); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }

        collection.add(new Object());
        try {
            EnumSet.copyOf(collection);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch (ClassCastException e) {
            // expected
        }

        Collection<EnumFoo> enumCollection = new ArrayList<EnumFoo>();
        enumCollection.add(EnumFoo.b);

        EnumSet<EnumFoo> copyOfEnumCollection = EnumSet.copyOf(enumCollection);
        assertEquals("Size of copyOfEnumCollection should be 1:", //$NON-NLS-1$
                1, copyOfEnumCollection.size());
        assertTrue("copyOfEnumCollection should contain EnumFoo.b:", //$NON-NLS-1$
                copyOfEnumCollection.contains(EnumFoo.b));

        enumCollection.add(null);
        assertEquals("Size of enumCollection should be 2:", //$NON-NLS-1$
                2, enumCollection.size());

        try {
            copyOfEnumCollection = EnumSet.copyOf(enumCollection);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
        
        Collection rawEnumCollection = new ArrayList();
        rawEnumCollection.add(EnumFoo.a);
        rawEnumCollection.add(EnumWithInnerClass.a);
        try {
            EnumSet.copyOf(rawEnumCollection);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
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
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
        
        rawEnumCollection = new ArrayList();
        rawEnumCollection.add(HugeEnum.a);
        rawEnumCollection.add(HugeEnumWithInnerClass.a);
        try {
            EnumSet.copyOf(rawEnumCollection);
            fail("Should throw ClassCastException"); //$NON-NLS-1$
        } catch(ClassCastException e) {
            // expected
        }
    }
    
    /**
     * @tests java.util.EnumSet#copyOf(java.util.EnumSet)
     */
    @SuppressWarnings("unchecked")
    public void test_CopyOf_LEnumSet() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet
                .noneOf(EnumWithInnerClass.class);
        enumSet.add(EnumWithInnerClass.a);
        enumSet.add(EnumWithInnerClass.f);
        EnumSet<EnumWithInnerClass> copyOfE = EnumSet.copyOf(enumSet);
        assertEquals("Size of enumSet and copyOfE should be equal", //$NON-NLS-1$
                enumSet.size(), copyOfE.size());

        assertTrue("EnumWithSubclass.a should be contained in copyOfE", //$NON-NLS-1$
                copyOfE.contains(EnumWithInnerClass.a));
        assertTrue("EnumWithSubclass.f should be contained in copyOfE", //$NON-NLS-1$
                copyOfE.contains(EnumWithInnerClass.f));

        Object[] enumValue = copyOfE.toArray();
        assertSame("enumValue[0] should be identical with EnumWithSubclass.a", //$NON-NLS-1$
                enumValue[0], EnumWithInnerClass.a);
        assertSame("enumValue[1] should be identical with EnumWithSubclass.f", //$NON-NLS-1$
                enumValue[1], EnumWithInnerClass.f);

        try {
            EnumSet.copyOf((EnumSet) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
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
     * @tests java.util.EnumSet#removeAll(Collection)
     */
    @SuppressWarnings("unchecked")
    public void test_removeAll_LCollection() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        try {
            set.removeAll(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        set = EnumSet.allOf(EnumFoo.class);
        assertEquals("Size of set should be 64:", 64, set.size()); //$NON-NLS-1$

        try {
            set.removeAll(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        Collection<EnumFoo> collection = new ArrayList<EnumFoo>();
        collection.add(EnumFoo.a);

        boolean result = set.removeAll(collection);
        assertTrue("Should return true", result); //$NON-NLS-1$
        assertEquals("Size of set should be 63", 63, set.size()); //$NON-NLS-1$

        collection = new ArrayList();
        result = set.removeAll(collection);
        assertFalse("Should return false", result); //$NON-NLS-1$

        Set<EmptyEnum> emptySet = EnumSet.noneOf(EmptyEnum.class);
        result = set.removeAll(emptySet);
        assertFalse("Should return false", result); //$NON-NLS-1$

        EnumSet<EnumFoo> emptyFooSet = EnumSet.noneOf(EnumFoo.class);
        result = set.removeAll(emptyFooSet);
        assertFalse("Should return false", result); //$NON-NLS-1$

        emptyFooSet.add(EnumFoo.a);
        result = set.removeAll(emptyFooSet);
        assertFalse("Should return false", result); //$NON-NLS-1$

        Set<EnumWithInnerClass> setWithSubclass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        result = set.removeAll(setWithSubclass);
        assertFalse("Should return false", result); //$NON-NLS-1$

        setWithSubclass.add(EnumWithInnerClass.a);
        result = set.removeAll(setWithSubclass);
        assertFalse("Should return false", result); //$NON-NLS-1$

        Set<EnumFoo> anotherSet = EnumSet.noneOf(EnumFoo.class);
        anotherSet.add(EnumFoo.a);

        set = EnumSet.allOf(EnumFoo.class);
        result = set.removeAll(anotherSet);
        assertTrue("Should return true", result); //$NON-NLS-1$
        assertEquals("Size of set should be 63:", 63, set.size()); //$NON-NLS-1$

        Set<EnumWithInnerClass> setWithInnerClass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        setWithInnerClass.add(EnumWithInnerClass.a);
        setWithInnerClass.add(EnumWithInnerClass.b);

        Set<EnumWithInnerClass> anotherSetWithInnerClass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        anotherSetWithInnerClass.add(EnumWithInnerClass.c);
        anotherSetWithInnerClass.add(EnumWithInnerClass.d);
        result = anotherSetWithInnerClass.removeAll(setWithInnerClass);
        assertFalse("Should return false", result); //$NON-NLS-1$

        anotherSetWithInnerClass.add(EnumWithInnerClass.a);
        result = anotherSetWithInnerClass.removeAll(setWithInnerClass);
        assertTrue("Should return true", result); //$NON-NLS-1$
        assertEquals("Size of anotherSetWithInnerClass should remain 2", //$NON-NLS-1$
                2, anotherSetWithInnerClass.size());

        anotherSetWithInnerClass.remove(EnumWithInnerClass.c);
        anotherSetWithInnerClass.remove(EnumWithInnerClass.d);
        result = anotherSetWithInnerClass.remove(setWithInnerClass);
        assertFalse("Should return false", result); //$NON-NLS-1$

        Set rawSet = EnumSet.allOf(EnumWithAllInnerClass.class);
        result = rawSet.removeAll(EnumSet.allOf(EnumFoo.class));
        assertFalse("Should return false", result); //$NON-NLS-1$

        setWithInnerClass = EnumSet.allOf(EnumWithInnerClass.class);
        anotherSetWithInnerClass = EnumSet.allOf(EnumWithInnerClass.class);
        setWithInnerClass.remove(EnumWithInnerClass.a);
        anotherSetWithInnerClass.remove(EnumWithInnerClass.f);
        result = setWithInnerClass.removeAll(anotherSetWithInnerClass);
        assertTrue("Should return true", result); //$NON-NLS-1$
        assertEquals("Size of setWithInnerClass should be 1", 1, setWithInnerClass.size()); //$NON-NLS-1$

        result = setWithInnerClass.contains(EnumWithInnerClass.f);
        assertTrue("Should return true", result); //$NON-NLS-1$
        
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
        assertFalse("Should return false", result); //$NON-NLS-1$

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
     * @tests java.util.EnumSet#retainAll(Collection)
     */
    @SuppressWarnings("unchecked")
    public void test_retainAll_LCollection() {
        Set<EnumFoo> set = EnumSet.allOf(EnumFoo.class);

        try {
            set.retainAll(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        set.clear();
        boolean result = set.retainAll(null);
        assertFalse("Should return false", result); //$NON-NLS-1$

        Collection rawCollection = new ArrayList();
        result = set.retainAll(rawCollection);
        assertFalse("Should return false", result); //$NON-NLS-1$

        rawCollection.add(EnumFoo.a);
        result = set.retainAll(rawCollection);
        assertFalse("Should return false", result); //$NON-NLS-1$

        rawCollection.add(EnumWithInnerClass.a);
        result = set.retainAll(rawCollection);
        assertFalse("Should return false", result); //$NON-NLS-1$
        assertEquals("Size of set should be 0:", 0, set.size()); //$NON-NLS-1$

        rawCollection.remove(EnumFoo.a);
        result = set.retainAll(rawCollection);
        assertFalse("Should return false", result); //$NON-NLS-1$

        Set<EnumFoo> anotherSet = EnumSet.allOf(EnumFoo.class);
        result = set.retainAll(anotherSet);
        assertFalse("Should return false", result); //$NON-NLS-1$
        assertEquals("Size of set should be 0", 0, set.size()); //$NON-NLS-1$

        Set<EnumWithInnerClass> setWithInnerClass = EnumSet
                .allOf(EnumWithInnerClass.class);
        result = set.retainAll(setWithInnerClass);
        assertFalse("Should return false", result); //$NON-NLS-1$
        assertEquals("Size of set should be 0", 0, set.size()); //$NON-NLS-1$

        setWithInnerClass = EnumSet.noneOf(EnumWithInnerClass.class);
        result = set.retainAll(setWithInnerClass);
        assertFalse("Should return false", result); //$NON-NLS-1$

        Set<EmptyEnum> emptySet = EnumSet.allOf(EmptyEnum.class);
        result = set.retainAll(emptySet);
        assertFalse("Should return false", result); //$NON-NLS-1$

        Set<EnumWithAllInnerClass> setWithAllInnerClass = EnumSet
                .allOf(EnumWithAllInnerClass.class);
        result = set.retainAll(setWithAllInnerClass);
        assertFalse("Should return false", result); //$NON-NLS-1$

        set.add(EnumFoo.a);
        result = set.retainAll(setWithInnerClass);
        assertTrue("Should return true", result); //$NON-NLS-1$
        assertEquals("Size of set should be 0", 0, set.size()); //$NON-NLS-1$

        setWithInnerClass = EnumSet.allOf(EnumWithInnerClass.class);
        setWithInnerClass.remove(EnumWithInnerClass.f);
        Set<EnumWithInnerClass> anotherSetWithInnerClass = EnumSet
                .noneOf(EnumWithInnerClass.class);
        anotherSetWithInnerClass.add(EnumWithInnerClass.e);
        anotherSetWithInnerClass.add(EnumWithInnerClass.f);

        result = setWithInnerClass.retainAll(anotherSetWithInnerClass);
        assertTrue("Should return true", result); //$NON-NLS-1$
        result = setWithInnerClass.contains(EnumWithInnerClass.e);
        assertTrue("Should contain EnumWithInnerClass.e", result); //$NON-NLS-1$
        result = setWithInnerClass.contains(EnumWithInnerClass.b);
        assertFalse("Should not contain EnumWithInnerClass.b", result); //$NON-NLS-1$
        assertEquals("Size of set should be 1:", 1, setWithInnerClass.size()); //$NON-NLS-1$

        anotherSetWithInnerClass = EnumSet.allOf(EnumWithInnerClass.class);
        result = setWithInnerClass.retainAll(anotherSetWithInnerClass);

        assertFalse("Return value should be false", result); //$NON-NLS-1$

        rawCollection = new ArrayList();
        rawCollection.add(EnumWithInnerClass.e);
        rawCollection.add(EnumWithInnerClass.f);
        result = setWithInnerClass.retainAll(rawCollection);
        assertFalse("Should return false", result); //$NON-NLS-1$

        set = EnumSet.allOf(EnumFoo.class);
        set.remove(EnumFoo.a);
        anotherSet = EnumSet.noneOf(EnumFoo.class);
        anotherSet.add(EnumFoo.a);
        result = set.retainAll(anotherSet);
        assertTrue("Should return true", result); //$NON-NLS-1$
        assertEquals("size should be 0", 0, set.size()); //$NON-NLS-1$
        
        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.allOf(HugeEnum.class);

        try {
            hugeSet.retainAll(null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        hugeSet.clear();
        result = hugeSet.retainAll(null);
        assertFalse(result);

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
        assertTrue("Should contain HugeEnumWithInnerClass.e", result); //$NON-NLS-1$
        result = hugeSetWithInnerClass.contains(HugeEnumWithInnerClass.b);
        assertFalse("Should not contain HugeEnumWithInnerClass.b", result); //$NON-NLS-1$
        assertEquals("Size of hugeSet should be 1:", 1, hugeSetWithInnerClass.size()); //$NON-NLS-1$

        anotherHugeSetWithInnerClass = EnumSet.allOf(HugeEnumWithInnerClass.class);
        result = hugeSetWithInnerClass.retainAll(anotherHugeSetWithInnerClass);

        assertFalse("Return value should be false", result); //$NON-NLS-1$

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
     * @tests java.util.EnumSet#iterator()
     */
    public void test_iterator() {
        Set<EnumFoo> set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.a);
        set.add(EnumFoo.b);

        Iterator<EnumFoo> iterator = set.iterator();
        Iterator<EnumFoo> anotherIterator = set.iterator();
        assertNotSame("Should not be same", iterator, anotherIterator); //$NON-NLS-1$
        try {
            iterator.remove();
            fail("Should throw IllegalStateException"); //$NON-NLS-1$
        } catch (IllegalStateException e) {
            // expectedd
        }

        assertTrue("Should has next element:", iterator.hasNext()); //$NON-NLS-1$
        assertSame("Should be identical", EnumFoo.a, iterator.next()); //$NON-NLS-1$
        iterator.remove();
        assertTrue("Should has next element:", iterator.hasNext()); //$NON-NLS-1$
        assertSame("Should be identical", EnumFoo.b, iterator.next()); //$NON-NLS-1$
        assertFalse("Should not has next element:", iterator.hasNext()); //$NON-NLS-1$
        assertFalse("Should not has next element:", iterator.hasNext()); //$NON-NLS-1$

        assertEquals("Size should be 1:", 1, set.size()); //$NON-NLS-1$

        try {
            iterator.next();
            fail("Should throw NoSuchElementException"); //$NON-NLS-1$
        } catch (NoSuchElementException e) {
            // expected
        }
        set = EnumSet.noneOf(EnumFoo.class);
        set.add(EnumFoo.a);
        iterator = set.iterator();
        assertEquals("Should be equal", EnumFoo.a, iterator.next()); //$NON-NLS-1$
        iterator.remove();
        try {
            iterator.remove();
            fail("Should throw IllegalStateException"); //$NON-NLS-1$
        } catch(IllegalStateException e) {
            // expected
        }
        
        Set<EmptyEnum> emptySet = EnumSet.allOf(EmptyEnum.class);
        Iterator<EmptyEnum> emptyIterator = emptySet.iterator();
        try {
            emptyIterator.next();
            fail("Should throw NoSuchElementException"); //$NON-NLS-1$
        } catch (NoSuchElementException e) {
            // expected
        }

        Set<EnumWithInnerClass> setWithSubclass = EnumSet
                .allOf(EnumWithInnerClass.class);
        setWithSubclass.remove(EnumWithInnerClass.e);
        Iterator<EnumWithInnerClass> iteratorWithSubclass = setWithSubclass
                .iterator();
        assertSame("Should be same", EnumWithInnerClass.a, iteratorWithSubclass.next()); //$NON-NLS-1$

        assertTrue("Should return true", iteratorWithSubclass.hasNext()); //$NON-NLS-1$
        assertSame("Should be same", EnumWithInnerClass.b, iteratorWithSubclass.next()); //$NON-NLS-1$

        setWithSubclass.remove(EnumWithInnerClass.c);
        assertTrue("Should return true", iteratorWithSubclass.hasNext()); //$NON-NLS-1$
        assertSame("Should be same", EnumWithInnerClass.c, iteratorWithSubclass.next()); //$NON-NLS-1$

        assertTrue("Should return true", iteratorWithSubclass.hasNext()); //$NON-NLS-1$
        assertSame("Should be same", EnumWithInnerClass.d, iteratorWithSubclass.next()); //$NON-NLS-1$

        setWithSubclass.add(EnumWithInnerClass.e);
        assertTrue("Should return true", iteratorWithSubclass.hasNext()); //$NON-NLS-1$
        assertSame("Should be same", EnumWithInnerClass.f, iteratorWithSubclass.next()); //$NON-NLS-1$

        set = EnumSet.noneOf(EnumFoo.class);
        iterator = set.iterator();
        try {
            iterator.next();
            fail("Should throw NoSuchElementException"); //$NON-NLS-1$
        } catch (NoSuchElementException e) {
            // expected
        }
        
        set.add(EnumFoo.a);
        iterator = set.iterator();
        assertEquals("Should return EnumFoo.a", EnumFoo.a, iterator.next()); //$NON-NLS-1$
        assertEquals("Size of set should be 1", 1, set.size()); //$NON-NLS-1$
        iterator.remove();
        assertEquals("Size of set should be 0", 0, set.size()); //$NON-NLS-1$
        assertFalse("Should return false", set.contains(EnumFoo.a)); //$NON-NLS-1$
        
        set.add(EnumFoo.a);
        set.add(EnumFoo.b);
        iterator = set.iterator();
        assertEquals("Should be equals", EnumFoo.a, iterator.next()); //$NON-NLS-1$
        iterator.remove();
        try {
            iterator.remove();
            fail("Should throw IllegalStateException"); //$NON-NLS-1$
        } catch(IllegalStateException e) {
            // expected
        }
        
        assertTrue("Should have next element", iterator.hasNext()); //$NON-NLS-1$
        try {
            iterator.remove();
            fail("Should throw IllegalStateException"); //$NON-NLS-1$
        } catch (IllegalStateException e) {
            // expected
        }
        assertEquals("Size of set should be 1", 1, set.size()); //$NON-NLS-1$
        assertTrue("Should have next element", iterator.hasNext()); //$NON-NLS-1$
        assertEquals("Should return EnumFoo.b", EnumFoo.b, iterator.next()); //$NON-NLS-1$
        set.remove(EnumFoo.b);
        assertEquals("Size of set should be 0", 0, set.size()); //$NON-NLS-1$
        iterator.remove();
        assertFalse("Should return false", set.contains(EnumFoo.a)); //$NON-NLS-1$
        
        // RI's bug, EnumFoo.b should not exist at the moment.
        assertFalse("Should return false", set.contains(EnumFoo.b)); //$NON-NLS-1$
        
        // test enum type with more than 64 elements
        Set<HugeEnum> hugeSet = EnumSet.noneOf(HugeEnum.class);
        hugeSet.add(HugeEnum.a);
        hugeSet.add(HugeEnum.b);

        Iterator<HugeEnum> hIterator = hugeSet.iterator();
        Iterator<HugeEnum> anotherHugeIterator = hugeSet.iterator();
        assertNotSame(hIterator, anotherHugeIterator);
        try {
            hIterator.remove();
            fail("Should throw IllegalStateException"); //$NON-NLS-1$
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
            fail("Should throw NoSuchElementException"); //$NON-NLS-1$
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
            fail("Should throw NoSuchElementException"); //$NON-NLS-1$
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
            fail("Should throw IllegalStateException"); //$NON-NLS-1$
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
        assertFalse("Should return false", set.contains(EnumFoo.b)); //$NON-NLS-1$
        
        // Regression for HARMONY-4728
        hugeSet = EnumSet.allOf(HugeEnum.class);
        hIterator = hugeSet.iterator();
        for( int i = 0; i < 63; i++) {
            hIterator.next();
        }
        assertSame(HugeEnum.ll, hIterator.next());
    }
    
    /**
     * @tests java.util.EnumSet#of(E)
     */
    public void test_Of_E() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a);
        assertEquals("enumSet should have length 1:", 1, enumSet.size()); //$NON-NLS-1$

        assertTrue("enumSet should contain EnumWithSubclass.a:", //$NON-NLS-1$
                enumSet.contains(EnumWithInnerClass.a));

        try {
            EnumSet.of((EnumWithInnerClass) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
        
        // test enum type with more than 64 elements
        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a);
        assertEquals(1, hugeEnumSet.size());

        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.a));
    }
    
    /**
     * @tests java.util.EnumSet#of(E, E)
     */
    public void test_Of_EE() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a,
                EnumWithInnerClass.b);
        assertEquals("enumSet should have length 2:", 2, enumSet.size()); //$NON-NLS-1$

        assertTrue("enumSet should contain EnumWithSubclass.a:", //$NON-NLS-1$
                enumSet.contains(EnumWithInnerClass.a));
        assertTrue("enumSet should contain EnumWithSubclass.b:", //$NON-NLS-1$
                enumSet.contains(EnumWithInnerClass.b));

        try {
            EnumSet.of((EnumWithInnerClass) null, EnumWithInnerClass.a);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
        
        try {
            EnumSet.of( EnumWithInnerClass.a, (EnumWithInnerClass) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
        
        try {
            EnumSet.of( (EnumWithInnerClass) null, (EnumWithInnerClass) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }

        enumSet = EnumSet.of(EnumWithInnerClass.a, EnumWithInnerClass.a);
        assertEquals("Size of enumSet should be 1", //$NON-NLS-1$
                1, enumSet.size());
        
        // test enum type with more than 64 elements
        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a,
                HugeEnumWithInnerClass.b);
        assertEquals(2, hugeEnumSet.size());

        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.a));
        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.b));

        try {
            EnumSet.of((HugeEnumWithInnerClass) null, HugeEnumWithInnerClass.a);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
        
        try {
            EnumSet.of( HugeEnumWithInnerClass.a, (HugeEnumWithInnerClass) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
        
        try {
            EnumSet.of( (HugeEnumWithInnerClass) null, (HugeEnumWithInnerClass) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }

        hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a, HugeEnumWithInnerClass.a);
        assertEquals(1, hugeEnumSet.size());
    }
    
    /**
     * @tests java.util.EnumSet#of(E, E, E)
     */
    public void test_Of_EEE() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a,
                EnumWithInnerClass.b, EnumWithInnerClass.c);
        assertEquals("Size of enumSet should be 3:", 3, enumSet.size()); //$NON-NLS-1$

        assertTrue(
                "enumSet should contain EnumWithSubclass.a:", enumSet.contains(EnumWithInnerClass.a)); //$NON-NLS-1$
        assertTrue("Should return true", enumSet.contains(EnumWithInnerClass.c)); //$NON-NLS-1$

        try {
            EnumSet.of((EnumWithInnerClass) null, null, null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }

        enumSet = EnumSet.of(EnumWithInnerClass.a, EnumWithInnerClass.b,
                EnumWithInnerClass.b);
        assertEquals("enumSet should contain 2 elements:", 2, enumSet.size()); //$NON-NLS-1$
        
        // test enum type with more than 64 elements
        EnumSet<HugeEnumWithInnerClass> hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a,
                HugeEnumWithInnerClass.b, HugeEnumWithInnerClass.c);
        assertEquals(3, hugeEnumSet.size());

        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.a));
        assertTrue(hugeEnumSet.contains(HugeEnumWithInnerClass.c));

        try {
            EnumSet.of((HugeEnumWithInnerClass) null, null, null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }

        hugeEnumSet = EnumSet.of(HugeEnumWithInnerClass.a, HugeEnumWithInnerClass.b,
                HugeEnumWithInnerClass.b);
        assertEquals(2, hugeEnumSet.size());
    }
    
    /**
     * @tests java.util.EnumSet#of(E, E, E, E)
     */
    public void test_Of_EEEE() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a,
                EnumWithInnerClass.b, EnumWithInnerClass.c,
                EnumWithInnerClass.d);
        assertEquals("Size of enumSet should be 4", 4, enumSet.size()); //$NON-NLS-1$

        assertTrue(
                "enumSet should contain EnumWithSubclass.a:", enumSet.contains(EnumWithInnerClass.a)); //$NON-NLS-1$
        assertTrue("enumSet should contain EnumWithSubclass.d:", enumSet //$NON-NLS-1$
                .contains(EnumWithInnerClass.d));

        try {
            EnumSet.of((EnumWithInnerClass) null, null, null, null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
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
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
    }
    
    /**
     * @tests java.util.EnumSet#of(E, E, E, E, E)
     */
    public void test_Of_EEEEE() {
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a,
                EnumWithInnerClass.b, EnumWithInnerClass.c,
                EnumWithInnerClass.d, EnumWithInnerClass.e);
        assertEquals("Size of enumSet should be 5:", 5, enumSet.size()); //$NON-NLS-1$

        assertTrue("Should return true", enumSet.contains(EnumWithInnerClass.a)); //$NON-NLS-1$
        assertTrue("Should return true", enumSet.contains(EnumWithInnerClass.e)); //$NON-NLS-1$

        try {
            EnumSet.of((EnumWithInnerClass) null, null, null, null, null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
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
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
    }
    
    /**
     * @tests java.util.EnumSet#of(E, E...)
     */
    public void test_Of_EEArray() {
        EnumWithInnerClass[] enumArray = new EnumWithInnerClass[] {
                EnumWithInnerClass.b, EnumWithInnerClass.c };
        EnumSet<EnumWithInnerClass> enumSet = EnumSet.of(EnumWithInnerClass.a,
                enumArray);
        assertEquals("Should be equal", 3, enumSet.size()); //$NON-NLS-1$

        assertTrue("Should return true", enumSet.contains(EnumWithInnerClass.a)); //$NON-NLS-1$
        assertTrue("Should return true", enumSet.contains(EnumWithInnerClass.c)); //$NON-NLS-1$

        try {
            EnumSet.of(EnumWithInnerClass.a, (EnumWithInnerClass[])null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException npe) {
            // expected
        }
        
        EnumFoo[] foos = {EnumFoo.a, EnumFoo.c, EnumFoo.d};
        EnumSet<EnumFoo> set = EnumSet.of(EnumFoo.c, foos);
        assertEquals("size of set should be 1", 3, set.size()); //$NON-NLS-1$
        assertTrue("Should contain EnumFoo.a", set.contains(EnumFoo.a)); //$NON-NLS-1$
        assertTrue("Should contain EnumFoo.c", set.contains(EnumFoo.c)); //$NON-NLS-1$
        assertTrue("Should contain EnumFoo.d", set.contains(EnumFoo.d)); //$NON-NLS-1$
        
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
            fail("Should throw NullPointerException"); //$NON-NLS-1$
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
     * @tests java.util.EnumSet#range(E, E)
     */
    public void test_Range_EE() {
        try {
            EnumSet.range(EnumWithInnerClass.c, null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(null, EnumWithInnerClass.c);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(null, (EnumWithInnerClass) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(EnumWithInnerClass.b, EnumWithInnerClass.a);
            fail("Should throw IllegalArgumentException"); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }

        EnumSet<EnumWithInnerClass> enumSet = EnumSet.range(
                EnumWithInnerClass.a, EnumWithInnerClass.a);
        assertEquals("Size of enumSet should be 1", 1, enumSet.size()); //$NON-NLS-1$
        
        enumSet = EnumSet.range(
                EnumWithInnerClass.a, EnumWithInnerClass.c);
        assertEquals("Size of enumSet should be 3", 3, enumSet.size()); //$NON-NLS-1$
        
        // test enum with more than 64 elements
        try {
            EnumSet.range(HugeEnumWithInnerClass.c, null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(null, HugeEnumWithInnerClass.c);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(null, (HugeEnumWithInnerClass) null);
            fail("Should throw NullPointerException"); //$NON-NLS-1$
        } catch (NullPointerException e) {
            // expected
        }

        try {
            EnumSet.range(HugeEnumWithInnerClass.b, HugeEnumWithInnerClass.a);
            fail("Should throw IllegalArgumentException"); //$NON-NLS-1$
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
     * @tests java.util.EnumSet#clone()
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
}
