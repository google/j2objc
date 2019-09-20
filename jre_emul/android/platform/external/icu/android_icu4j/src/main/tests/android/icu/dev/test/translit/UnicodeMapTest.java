/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.translit;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;

import android.icu.dev.test.TestBoilerplate;
import android.icu.dev.test.TestFmwk;
import android.icu.dev.util.CollectionUtilities;
import android.icu.dev.util.UnicodeMap;
import android.icu.dev.util.UnicodeMap.EntryRange;
import android.icu.dev.util.UnicodeMapIterator;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;

/**
 * @test
 * @summary General test of UnicodeSet
 */
public class UnicodeMapTest extends TestFmwk {

    static final int MODIFY_TEST_LIMIT = 32;
    static final int MODIFY_TEST_ITERATIONS = 100000;

    @Test
    public void TestIterations() {
        UnicodeMap<Double> foo = new UnicodeMap();
        checkToString(foo, "");
        foo.put(3, 6d).put(5, 10d);
        checkToString(foo, "0003=6.0\n0005=10.0\n");
        foo.put(0x10FFFF, 666d);
        checkToString(foo, "0003=6.0\n0005=10.0\n10FFFF=666.0\n");
        foo.put("neg", -555d);
        checkToString(foo, "0003=6.0\n0005=10.0\n10FFFF=666.0\n006E,0065,0067=-555.0\n");

        double i = 0;
        for (EntryRange<Double> entryRange : foo.entryRanges()) {
            i += entryRange.value;
        }
        assertEquals("EntryRange<T>", 127d, i);
    }

    public void checkToString(UnicodeMap<Double> foo, String expected) {
        assertEquals("EntryRange<T>", expected, CollectionUtilities.join(foo.entryRanges(), "\n") + (foo.size() == 0 ? "" : "\n"));
        assertEquals("EntryRange<T>", expected, foo.toString());
    }
    
    @Test
    public void TestRemove() {
        UnicodeMap<Double> foo = new UnicodeMap()
        .putAll(0x20, 0x29, -2d)
        .put("abc", 3d)
        .put("xy", 2d)
        .put("mark", 4d)
        .freeze();
        UnicodeMap<Double> fii = new UnicodeMap()
        .putAll(0x21, 0x25, -2d)
        .putAll(0x26, 0x28, -3d)
        .put("abc", 3d)
        .put("mark", 999d)
        .freeze();
        
        UnicodeMap<Double> afterFiiRemoval = new UnicodeMap()
        .put(0x20, -2d)
        .putAll(0x26, 0x29, -2d)
        .put("xy", 2d)
        .put("mark", 4d)
        .freeze();
        
        UnicodeMap<Double> afterFiiRetained = new UnicodeMap()
        .putAll(0x21, 0x25, -2d)
        .put("abc", 3d)
        .freeze();

        UnicodeMap<Double> test = new UnicodeMap<Double>().putAll(foo)
                .removeAll(fii);
        assertEquals("removeAll", afterFiiRemoval, test);
        
        test = new UnicodeMap<Double>().putAll(foo)
                .retainAll(fii);
        assertEquals("retainAll", afterFiiRetained, test);
    }

    @Test
    public void TestAMonkey() {
        SortedMap<String,Integer> stayWithMe = new TreeMap<String,Integer>(OneFirstComparator);

        UnicodeMap<Integer> me = new UnicodeMap<Integer>().putAll(stayWithMe);
        // check one special case, removal near end
        me.putAll(0x10FFFE, 0x10FFFF, 666);
        me.remove(0x10FFFF);

        int iterations = 100000;
        SortedMap<String,Integer> test = new TreeMap();

        Random rand = new Random(0);
        String other;
        Integer value;
        // try modifications
        for (int i = 0; i < iterations ; ++i) {
            switch(rand.nextInt(20)) {
            case 0:
                logln("clear");
                stayWithMe.clear();
                me.clear();
                break;
            case 1:
                fillRandomMap(rand, 5, test);
                logln("putAll\t" + test);
                stayWithMe.putAll(test);
                me.putAll(test);
                break;
            case 2: case 3: case 4: case 5: case 6: case 7: case 8:
                other = getRandomKey(rand);
                //                if (other.equals("\uDBFF\uDFFF") && me.containsKey(0x10FFFF) && me.get(0x10FFFF).equals(me.get(0x10FFFE))) {
                //                    System.out.println("Remove\t" + other + "\n" + me);
                //                }
                logln("remove\t" + other);
                stayWithMe.remove(other);
                try {
                    me.remove(other);
                } catch (IllegalArgumentException e) {
                    errln("remove\t" + other + "\tfailed: " + e.getMessage() + "\n" + me);
                    me.clear();
                    stayWithMe.clear();
                }
                break;
            default:
                other = getRandomKey(rand);
                value = rand.nextInt(50)+50;
                logln("put\t" + other + " = " + value);
                stayWithMe.put(other, value);
                me.put(other,value);
                break;
            }
            checkEquals(me, stayWithMe);
        }
    }

    /**
     * @param rand 
     * @param nextInt
     * @param test 
     * @return
     */
    private SortedMap<String, Integer> fillRandomMap(Random rand, int max, SortedMap<String, Integer> test) {
        test.clear();
        max = rand.nextInt(max);
        for (int i = 0; i < max; ++i) {
            test.put(getRandomKey(rand), rand.nextInt(50)+50);
        }
        return test;
    }

    Set temp = new HashSet();
    /**
     * @param me
     * @param stayWithMe
     */
    private void checkEquals(UnicodeMap<Integer> me, SortedMap<String, Integer> stayWithMe) {
        temp.clear();
        for (Entry<String, Integer> e : me.entrySet()) {
            temp.add(e);
        }
        Set<Entry<String, Integer>> entrySet = stayWithMe.entrySet();
        if (!entrySet.equals(temp)) {
            logln(me.entrySet().toString());
            logln(me.toString());
            assertEquals("are in parallel", entrySet, temp);
            // we failed. Reset and start again
            entrySet.clear();
            temp.clear();
            return;
        }
        for (String key : stayWithMe.keySet()) {
            assertEquals("containsKey", stayWithMe.containsKey(key), me.containsKey(key));
            Integer value = stayWithMe.get(key);
            assertEquals("get", value, me.get(key));
            assertEquals("containsValue", stayWithMe.containsValue(value), me.containsValue(value));
            int cp = UnicodeSet.getSingleCodePoint(key);
            if (cp != Integer.MAX_VALUE) {
                assertEquals("get", value, me.get(cp));
            }
        }
        Set<String> nonCodePointStrings = stayWithMe.tailMap("").keySet();
        if (nonCodePointStrings.size() == 0) nonCodePointStrings = null; // for parallel api
        assertEquals("getNonRangeStrings", nonCodePointStrings, me.getNonRangeStrings());

        TreeSet<Integer> values = new TreeSet<Integer>(stayWithMe.values());
        TreeSet<Integer> myValues = new TreeSet<Integer>(me.values());
        assertEquals("values", myValues, values);

        for (String key : stayWithMe.keySet()) {
            assertEquals("containsKey", stayWithMe.containsKey(key), me.containsKey(key));
        }
    }

    static Comparator<String> OneFirstComparator = new Comparator<String>() {
        public int compare(String o1, String o2) {
            int cp1 = UnicodeSet.getSingleCodePoint(o1);
            int cp2 = UnicodeSet.getSingleCodePoint(o2);
            int result = cp1 - cp2;
            if (result != 0) {
                return result;
            }
            if (cp1 == Integer.MAX_VALUE) {
                return o1.compareTo(o2);
            }
            return 0;
        }

    };

    /**
     * @param rand 
     * @param others
     * @return
     */
    private String getRandomKey(Random rand) {
        int r = rand.nextInt(30);
        if (r == 0) {
            return UTF16.valueOf(r); 
        } else if (r < 10) {
            return UTF16.valueOf('A'-1+r);
        } else if (r < 20) {
            return UTF16.valueOf(0x10FFFF - (r-10));
            //        } else if (r == 20) {
            //            return "";
        }
        return "a" + UTF16.valueOf(r + 'a'-1);
    }

    @Test
    public void TestModify() {
        Random random = new Random(0);
        UnicodeMap unicodeMap = new UnicodeMap();
        HashMap hashMap = new HashMap();
        String[] values = {null, "the", "quick", "brown", "fox"};
        for (int count = 1; count <= MODIFY_TEST_ITERATIONS; ++count) {
            String value = values[random.nextInt(values.length)];
            int start = random.nextInt(MODIFY_TEST_LIMIT); // test limited range
            int end = random.nextInt(MODIFY_TEST_LIMIT);
            if (start > end) {
                int temp = start;
                start = end;
                end = temp;
            }
            int modCount = count & 0xFF;
            if (modCount == 0 && isVerbose()) {
                logln("***"+count);
                logln(unicodeMap.toString());
            }
            unicodeMap.putAll(start, end, value);
            if (modCount == 1 && isVerbose()) {
                logln(">>>\t" + Utility.hex(start) + ".." + Utility.hex(end) + "\t" + value);
                logln(unicodeMap.toString());
            }
            for (int i = start; i <= end; ++i) {
                hashMap.put(new Integer(i), value);
            }
            if (!hasSameValues(unicodeMap, hashMap)) {
                errln("Failed at " + count);
            }
        }
    }

    private boolean hasSameValues(UnicodeMap unicodeMap, HashMap hashMap) {
        for (int i = 0; i < MODIFY_TEST_LIMIT; ++i) {
            Object unicodeMapValue = unicodeMap.getValue(i);
            Object hashMapValue = hashMap.get(new Integer(i));
            if (unicodeMapValue != hashMapValue) {
                return false;
            }
        }
        return true;
    }
    
    @Test
    public void TestCloneAsThawed11721 () {
        UnicodeMap<Integer> test = new UnicodeMap().put("abc", 3).freeze();
        UnicodeMap<Integer> copy = test.cloneAsThawed();
        copy.put("def", 4);
        assertEquals("original-abc", (Integer) 3, test.get("abc"));
        assertNull("original-def", test.get("def"));
        assertEquals("copy-def", (Integer) 4, copy.get("def"));
    }

    private static final int LIMIT = 0x15; // limit to make testing more realistic in terms of collisions
    private static final int ITERATIONS = 1000000;
    private static final boolean SHOW_PROGRESS = false;
    private static final boolean DEBUG = false;

    SortedSet<String> log = new TreeSet<String>();
    static String[] TEST_VALUES = {"A", "B", "C", "D", "E", "F"};
    static Random random = new Random(12345);

    public void TestUnicodeMapRandom() {
        // do random change to both, then compare
        random.setSeed(12345); // reproducible results
        logln("Comparing against HashMap");
        UnicodeMap<String> map1 = new UnicodeMap();
        Map<Integer, String> map2 = new HashMap<Integer, String>();
        for (int counter = 0; counter < ITERATIONS; ++counter) {
            int start = random.nextInt(LIMIT);
            String value = TEST_VALUES[random.nextInt(TEST_VALUES.length)];
            String logline = Utility.hex(start) + "\t" + value;
            if (SHOW_PROGRESS) logln(counter + "\t" + logline);
            log.add(logline);
            if (DEBUG && counter == 144) {
                System.out.println(" debug");
            }
            map1.put(start, value);
            map2.put(start, value);
            check(map1, map2, counter);
        }
        checkNext(map1, map2, LIMIT);
    }

    private static final int SET_LIMIT = 0x10FFFF;
    private static final int propEnum = UProperty.GENERAL_CATEGORY;

    public void TestUnicodeMapGeneralCategory() {
        logln("Setting General Category");
        UnicodeMap<String> map1 = new UnicodeMap();
        Map<Integer, String> map2 = new HashMap<Integer, String>();
        //Map<Integer, String> map3 = new TreeMap<Integer, String>();
        map1 = new UnicodeMap<String>();
        map2 = new TreeMap<Integer,String>();
        for (int cp = 0; cp <= SET_LIMIT; ++cp) {
              int enumValue = UCharacter.getIntPropertyValue(cp, propEnum);
              //if (enumValue <= 0) continue; // for smaller set
              String value = UCharacter.getPropertyValueName(propEnum,enumValue, UProperty.NameChoice.LONG);
              map1.put(cp, value);
              map2.put(cp, value);
        }
        checkNext(map1, map2, Integer.MAX_VALUE);

        logln("Comparing General Category");
        check(map1, map2, -1);
        logln("Comparing Values");
        Set<String> values1 = map1.getAvailableValues(new TreeSet<String>());
        Set<String> values2 = new TreeSet<String>(map2.values());
        if (!TestBoilerplate.verifySetsIdentical(this, values1, values2)) {
            throw new IllegalArgumentException("Halting");
        }
        logln("Comparing Sets");
        for (Iterator<String> it = values1.iterator(); it.hasNext();) {
            String value = it.next();
            logln(value == null ? "null" : value);
            UnicodeSet set1 = map1.keySet(value);
            UnicodeSet set2 = TestBoilerplate.getSet(map2, value);
            if (!TestBoilerplate.verifySetsIdentical(this, set1, set2)) {
                throw new IllegalArgumentException("Halting");
            }
        }
    }

    public void TestAUnicodeMap2() {
        UnicodeMap foo = new UnicodeMap();
        @SuppressWarnings("unused")
        int hash = foo.hashCode(); // make sure doesn't NPE
        @SuppressWarnings("unused")
        Set fii = foo.stringKeys(); // make sure doesn't NPE
    }

    public void TestAUnicodeMapInverse() {
        UnicodeMap<Character> foo1 = new UnicodeMap<Character>()
                .putAll('a', 'z', 'b')
                .put("ab", 'c')
                .put('x', 'b')
                .put("xy", 'c')
                ;
        Map<Character, UnicodeSet> target = new HashMap<Character, UnicodeSet>();
        foo1.addInverseTo(target);
        UnicodeMap<Character> reverse = new UnicodeMap().putAllInverse(target);
        assertEquals("", foo1, reverse);
    }

    private void checkNext(UnicodeMap<String> map1, Map<Integer,String> map2, int limit) {
        logln("Comparing nextRange");
        Map localMap = new TreeMap();
        UnicodeMapIterator<String> mi = new UnicodeMapIterator<String>(map1);
        while (mi.nextRange()) {
            logln(Utility.hex(mi.codepoint) + ".." + Utility.hex(mi.codepointEnd) + " => " + mi.value);
            for (int i = mi.codepoint; i <= mi.codepointEnd; ++i) {
                //if (i >= limit) continue;
                localMap.put(i, mi.value);
            }
        }
        checkMap(map2, localMap);

        logln("Comparing next");
        mi.reset();
        localMap = new TreeMap();
//        String lastValue = null;
        while (mi.next()) {
//            if (!UnicodeMap.areEqual(lastValue, mi.value)) {
//                // System.out.println("Change: " + Utility.hex(mi.codepoint) + " => " + mi.value);
//                lastValue = mi.value;
//            }
            //if (mi.codepoint >= limit) continue;
            localMap.put(mi.codepoint, mi.value);
        }
        checkMap(map2, localMap);
    }

    public void check(UnicodeMap<String> map1, Map<Integer,String> map2, int counter) {
        for (int i = 0; i < LIMIT; ++i) {
            String value1 = map1.getValue(i);
            String value2 = map2.get(i);
            if (!UnicodeMap.areEqual(value1, value2)) {
                errln(counter + " Difference at " + Utility.hex(i)
                     + "\t UnicodeMap: " + value1
                     + "\t HashMap: " + value2);
                errln("UnicodeMap: " + map1);
                errln("Log: " + TestBoilerplate.show(log));
                errln("HashMap: " + TestBoilerplate.show(map2));
            }
        }
    }

    void checkMap(Map m1, Map m2) {
        if (m1.equals(m2)) return;
        StringBuilder buffer = new StringBuilder();
        Set m1entries = m1.entrySet();
        Set m2entries = m2.entrySet();
        getEntries("\r\nIn First, and not Second", m1entries, m2entries, buffer, 20);
        getEntries("\r\nIn Second, and not First", m2entries, m1entries, buffer, 20);
        errln(buffer.toString());
    }

    static Comparator<Map.Entry<Integer, String>> ENTRY_COMPARATOR = new Comparator<Map.Entry<Integer, String>>() {
        public int compare(Map.Entry<Integer, String> o1, Map.Entry<Integer, String> o2) {
            if (o1 == o2) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
            Map.Entry<Integer, String> a = o1;
            Map.Entry<Integer, String> b = o2;
            int result = compare2(a.getKey(), b.getKey());
            if (result != 0) return result;
            return compare2(a.getValue(), b.getValue());
        }
        private <T extends Comparable> int compare2(T o1, T o2) {
            if (o1 == o2) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
            return o1.compareTo(o2);
        }
    };

    private void getEntries(String title, Set<Map.Entry<Integer,String>> m1entries, Set<Map.Entry<Integer, String>> m2entries, StringBuilder buffer, int limit) {
        Set<Map.Entry<Integer, String>> m1_m2 = new TreeSet<Map.Entry<Integer, String>>(ENTRY_COMPARATOR);
        m1_m2.addAll(m1entries);
        m1_m2.removeAll(m2entries);
        buffer.append(title + ": " + m1_m2.size() + "\r\n");
        for (Entry<Integer, String> entry : m1_m2) {
            if (limit-- < 0) return;
            buffer.append(entry.getKey()).append(" => ")
             .append(entry.getValue()).append("\r\n");
        }
    }
}
