/*
 * Copyright (C) 2020 The Android Open Source Project
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
 * limitations under the License.
 */

package libcore.java.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import libcore.libcore.util.SerializationTester;

import static java.util.Map.entry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests {@code Map.of()} overloads and {@code Map.ofEntries(...)}.
 */
@RunWith(Parameterized.class)
public class MapOfTest {

    @Test public void serializationCompatibility_empty() {
        String golden = "ACED0005737200256A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F646966"
                + "6961626C654D6170F1A5A8FE74F507420200014C00016D74000F4C6A6176612F7574696C2F4D6170"
                + "3B7870737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F"
                + "6164466163746F724900097468726573686F6C6478703F4000000000000177080000000100000000"
                + "78";
        new SerializationTester<>(create(), golden).test();
    }

    @Test public void serializationCompatibility_oneElement() {
        String golden = "ACED0005737200256A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F646966"
                + "6961626C654D6170F1A5A8FE74F507420200014C00016D74000F4C6A6176612F7574696C2F4D6170"
                + "3B7870737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F"
                + "6164466163746F724900097468726573686F6C6478703F4000000000000177080000000200000001"
                + "7400036F6E65737200116A6176612E6C616E672E496E746567657212E2A0A4F78187380200014900"
                + "0576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B020000787000"
                + "00000178";
        new SerializationTester<>(create(entry("one", 1)), golden).test();
    }

    @Test public void serializationCompatibility_manyElements() {
        String golden = "ACED0005737200256A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F646966"
                + "6961626C654D6170F1A5A8FE74F507420200014C00016D74000F4C6A6176612F7574696C2F4D6170"
                + "3B7870737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F"
                + "6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000A"
                + "7400046E696E65737200116A6176612E6C616E672E496E746567657212E2A0A4F781873802000149"
                + "000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870"
                + "000000097400037369787371007E000600000006740004666F75727371007E000600000004740003"
                + "6F6E657371007E000600000001740005736576656E7371007E00060000000774000374656E737100"
                + "7E00060000000A74000374776F7371007E00060000000274000574687265657371007E0006000000"
                + "03740004666976657371007E00060000000574000565696768747371007E00060000000878";
        new SerializationTester<>(
                create(entry("one", 1), entry("two", 2), entry("three", 3),
                entry("four", 4), entry("five", 5), entry("six", 6), entry("seven", 7),
                entry("eight", 8), entry("nine", 9), entry("ten", 10)), golden).test();
    }

    @Test public void duplicates_sameKey() {
        Map.Entry[] entries = { entry("duplicateKey", 23), entry("duplicateKey", 42) };
        assertThrowsIae(() -> creator.create(entries));
    }

    @Test public void duplicates_sameEntry() {
        Map.Entry[] entries = { entry("duplicateKey", 42), entry("duplicateKey", 42) };
        assertThrowsIae(() -> creator.create(entries));
    }

    @Test public void duplicates_manyElements() {
        Map.Entry[] entries = {
                entry("key1", 1),
                entry("duplicateKey", 23),
                entry("key2", 2),
                entry("duplicateKey", 42) };
        assertThrowsIae(() -> creator.create(entries));
    }

    @Test public void empty() {
        assertBehaviorCommonToAllOfInstances("exampleKey", 42);
    }

    @Test public void nullEntries() {
        assertThrowsNpe(() -> Map.ofEntries((Map.Entry[]) null));
        assertThrowsNpe(() -> Map.ofEntries((Map.Entry) null));
        List<Map.Entry<String, Integer>> sampleEntries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            sampleEntries.add(entry("key" + i, i));
        }
        for (int size = 0; size <= sampleEntries.size(); size++) {
            for (int nullIndex = 0; nullIndex < size; nullIndex++) {
                Map.Entry[] entries = sampleEntries.subList(0, size).toArray(
                        (Map.Entry<String, Integer>[]) new Map.Entry[0]);
                entries[nullIndex] = null;
                assertThrowsNpe(() -> Map.ofEntries(entries));
            }
        }
    }

    @Test public void oneEntry() {
        assertBehaviorCommonToAllOfInstances(
                "exampleKey", 42, entry("key", "value"));
    }

    @Test public void twoEntries() {
        assertBehaviorCommonToAllOfInstances(
                "exampleKey", 42, entry("key1", "value1"), entry("key2", "value2"));
    }

    @Test public void manyEntries() {
        List<Map.Entry<String, Integer>> sampleEntries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            sampleEntries.add(entry("key" + i, i));
        }
        for (int size = 0; size <= sampleEntries.size(); size++) {
            Map.Entry[] entries = sampleEntries.subList(0, size).toArray(
                    (Map.Entry<String, Integer>[]) new Map.Entry[0]);
            assertBehaviorCommonToAllOfInstances("key0", 42, entries);
        }
    }

    @Test public void entry_nullKeyOrValue() {
        assertThrowsNpe(() -> entry(null, "value"));
        assertThrowsNpe(() -> entry("key", null));
        assertThrowsNpe(() -> entry(null, null));

        // This one works
        entry("key", "value");
    }

    @Test public void of_nullKeyOrValue() {
        assertThrowsNpe(() -> Map.of(null, "value"));
        assertThrowsNpe(() -> Map.of("key", null));
        assertThrowsNpe(() -> Map.of("k1", "v1", "k2", "v2", null, "v3", "k4", "v4"));
        assertThrowsNpe(() -> Map.of("k1", "v1", "k2", "v2", "k3", null, "k4", "v4"));
    }

    @Test public void mixedEntryTypes() {
        assertBehaviorCommonToAllOfInstances(
                "onekey", "new value", entry("oneKey", 1), entry(2, "twoValue"));
    }

    private static<K, V> void assertUnmodifiable(Map<K, V> map, K exampleKey, V exampleValue) {
        Map<K, V> exampleEntries = Collections.singletonMap(exampleKey, exampleValue);
        assertThrowsUoe(() -> map.put(exampleKey, exampleValue));
        assertThrowsUoe(() -> map.putAll(exampleEntries));
        assertThrowsUoe(() -> map.remove(exampleKey));
        assertThrowsUoe(() -> map.remove(exampleKey, exampleValue));
        assertThrowsUoe(() -> map.clear());
        assertThrowsUoe(() -> map.replace(exampleKey, exampleValue, null));
        assertThrowsUoe(() -> map.putIfAbsent(exampleKey, exampleValue));
        assertThrowsUoe(() -> map.entrySet().clear());
        assertThrowsUoe(() -> map.keySet().clear());
        assertThrowsUoe(() -> map.values().clear());

        if (!map.isEmpty()) {
            Map.Entry<K, V> firstEntry = map.entrySet().iterator().next();
            assertThrowsUoe(() -> firstEntry.setValue(exampleValue));
        }
    }

    /** Checks assertions that hold for all Map.of() / Map.ofEntries() instances. */
    private <K, V> void assertBehaviorCommonToAllOfInstances(K exampleKey, V exampleValue,
            Map.Entry<K, V>...entries) {
        Map<K, V> expected = hashMapOf(entries);
        Map<K, V> actual = creator.create(entries);
        assertBehaviorCommonToAllOfInstances(expected, actual, exampleKey, exampleValue);
    }

    private static<K, V> void assertBehaviorCommonToAllOfInstances(Map<K, V> expected,
            Map<K, V> actual, K exampleKey, V exampleValue) {
        assertDoesNotSupportNull(actual);
        assertMapEquals(expected, actual);
        assertUnmodifiable(actual, exampleKey, exampleValue);
    }

    private static<K, V> void assertDoesNotSupportNull(Map<K, V> map) {
        assertThrowsNpe(() -> map.containsKey(null));
        assertThrowsNpe(() -> map.keySet().contains(null));
        assertThrowsNpe(() -> map.values().contains(null));
    }

    private static<K, V> void assertMapEquals(Map<K, V> expected, Map<K, V> actual) {
        assertEquals(expected, actual);
        assertEquals(actual, expected);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.entrySet(), actual.entrySet());

        assertSetEquals(expected.entrySet(), actual.entrySet());
        assertSetEquals(expected.keySet(), actual.keySet());
        assertCollectionEquals(new HashSet<>(expected.values()), new HashSet<>(actual.values()));
    }

    private static<T> void assertSetEquals(Set<T> expected, Set<T> actual) {
        assertCollectionEquals(expected, actual);
    }

    private static<T> void assertCollectionEquals(Collection<T> expected, Collection<T> actual) {
        assertEquals(expected, actual);
        assertEquals(actual, expected);
        assertEquals(expected.hashCode(), actual.hashCode());

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }

    private final Creator creator;

    public MapOfTest(Creator creator) {
        this.creator = Objects.requireNonNull(creator);
    }

    private<K, V> Map<K, V> create(Map.Entry<K, V>... entries) {
        return creator.create(entries);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Creator> getCreators() {
        return Arrays.asList(Creator.OF, Creator.OF_ENTRIES);
    }

    private static<K, V> Map<K, V> hashMapOf(Map.Entry<K, V>... entries) {
        HashMap<K, V> result = new HashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    enum Creator {
        OF {
            private<K,V> K k(int index, Map.Entry<K, V>... entries) {
                return entries[index].getKey();
            }
            private<K,V> V v(int index, Map.Entry<K, V>... entries) {
                return entries[index].getValue();
            }

            @Override
            <K, V> Map<K, V> create(Map.Entry<K, V>... e) {
                switch (e.length) {
                    case 0: return Map.of();
                    case 1: return Map.of(k(0, e), v(0, e));
                    case 2: return Map.of(k(0, e), v(0, e), k(1, e), v(1, e));
                    case 3: return Map.of(k(0, e), v(0, e), k(1, e), v(1, e), k(2, e), v(2, e));
                    case 4: return Map.of(k(0, e), v(0, e), k(1, e), v(1, e), k(2, e), v(2, e), k(3, e), v(3, e));
                    case 5: return Map.of(k(0, e), v(0, e), k(1, e), v(1, e), k(2, e), v(2, e), k(3, e), v(3, e), k(4, e), v(4, e));
                    case 6: return Map.of(k(0, e), v(0, e), k(1, e), v(1, e), k(2, e), v(2, e), k(3, e), v(3, e), k(4, e), v(4, e), k(5, e), v(5, e));
                    case 7: return Map.of(k(0, e), v(0, e), k(1, e), v(1, e), k(2, e), v(2, e), k(3, e), v(3, e), k(4, e), v(4, e), k(5, e), v(5, e), k(6, e), v(6, e));
                    case 8: return Map.of(k(0, e), v(0, e), k(1, e), v(1, e), k(2, e), v(2, e), k(3, e), v(3, e), k(4, e), v(4, e), k(5, e), v(5, e), k(6, e), v(6, e), k(7, e), v(7, e));
                    case 9: return Map.of(k(0, e), v(0, e), k(1, e), v(1, e), k(2, e), v(2, e), k(3, e), v(3, e), k(4, e), v(4, e), k(5, e), v(5, e), k(6, e), v(6, e), k(7, e), v(7, e), k(8, e), v(8, e));
                   case 10: return Map.of(k(0, e), v(0, e), k(1, e), v(1, e), k(2, e), v(2, e), k(3, e), v(3, e), k(4, e), v(4, e), k(5, e), v(5, e), k(6, e), v(6, e), k(7, e), v(7, e), k(8, e), v(8, e), k(9, e), v(9, e));
                    default:
                        fail(this + " requires 0 to 10 entries");
                        throw new AssertionError("unreachable");
                }

            }
        },
        OF_ENTRIES {
            @Override
            <K, V> Map<K, V> create(Map.Entry<K, V>... entries) {
                return Map.ofEntries(entries);
            }
        }
        ;
        abstract <K,V> Map<K, V> create(Map.Entry<K,V>... entries);
    }

    private static void assertThrowsIae(Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    private static void assertThrowsUoe(Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    private static void assertThrowsNpe(Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (NullPointerException expected) {
        }
    }
}
