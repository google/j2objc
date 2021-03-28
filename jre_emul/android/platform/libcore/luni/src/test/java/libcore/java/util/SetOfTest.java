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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import libcore.libcore.util.SerializationTester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SetOfTest {

    @Test public void of_serializationCompatibility_empty() {
        String golden = "ACED00057372001E6A6176612E7574696C2E436F6C6C656374696F6E7324456D7074795365"
                + "7415F5721DB403CB280200007870";
        new SerializationTester<>(Set.<String>of(), golden).test();
    }

    @Test public void of_serializationCompatibility_oneElement() {
        String golden = "ACED0005737200256A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F646966"
                + "6961626C65536574801D92D18F9B80550200007872002C6A6176612E7574696C2E436F6C6C656374"
                + "696F6E7324556E6D6F6469666961626C65436F6C6C656374696F6E19420080CB5EF71E0200014C00"
                + "01637400164C6A6176612F7574696C2F436F6C6C656374696F6E3B7870737200116A6176612E7574"
                + "696C2E48617368536574BA44859596B8B7340300007870770C000000023F40000000000001740003"
                + "6F6E6578";
        new SerializationTester<>(Set.of("one"), golden).test();
    }

    @Test public void of_serializationCompatibility_manyElements() {
        String golden = "ACED0005737200256A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F646966"
                + "6961626C65536574801D92D18F9B80550200007872002C6A6176612E7574696C2E436F6C6C656374"
                + "696F6E7324556E6D6F6469666961626C65436F6C6C656374696F6E19420080CB5EF71E0200014C00"
                + "01637400164C6A6176612F7574696C2F436F6C6C656374696F6E3B7870737200116A6176612E7574"
                + "696C2E48617368536574BA44859596B8B7340300007870770C000000203F4000000000000D737200"
                + "116A6176612E6C616E672E496E746567657212E2A0A4F781873802000149000576616C7565787200"
                + "106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000007371007E0006"
                + "000000017371007E0006000000027371007E0006000000037371007E0006000000047371007E0006"
                + "000000057371007E0006000000067371007E0006000000077371007E0006000000087371007E0006"
                + "000000097371007E00060000000A7371007E00060000000B7371007E00060000000C78";
        new SerializationTester<>(Set.of(12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0), golden).test();
    }

    @Test public void mixedTypes() {
        Set<?> list = Set.of("element", 42);
        assertEquals(asSet("element", 42), list);

        assertTrue(list.contains("element"));
        assertTrue(list.contains(42));
        assertFalse(list.contains(new Object()));
        assertFalse(list.contains(31));
    }

    @Test public void duplicate() {
        assertThrowsIae(() -> { Set.of("duplicate", "duplicate"); });
        assertThrowsIae(() -> { Set.of("duplicate", "duplicate", "duplicate"); });
        assertThrowsIae(() -> { Set.of("a", "duplicate", "duplicate"); });
        assertThrowsIae(() -> {
            Set.of("a", "duplicate", "b", "c", "d", "e", "f", "g", "duplicate");
        });
        assertThrowsIae(() -> { Set.of("a", "duplicate", "b", "c", "d", "e", "f", "g",
                "duplicate", "h", "i", "j", "k", "l"); });
    }

    /**
     * Checks that when there is both a duplicate and null, the exception that is thrown
     * depends on which occurs first.
     */
    @Test public void duplicateAndNull() {
        assertThrowsNpe(() -> { Set.of("duplicate", null, "duplicate"); }); // null first
        assertThrowsIae(() -> { Set.of("duplicate", "duplicate", null); }); // duplicate first
    }

    @Test public void empty() {
        check_commonBehavior(Collections.<String>emptySet(), Set.<String>of(), "non-null example");
    }

    @Test public void nonEmpty() {
        check_nonEmpty(asSet("one"), Set.of("one"));
        check_nonEmpty(asSet("one", "two"), Set.of("one", "two"));
        check_nonEmpty(asSet("one", "two", "three"), Set.of("one", "two", "three"));
        check_nonEmpty(asSet("one", "two", "three", "four"),
                Set.of("one", "two", "three", "four"));
        check_nonEmpty(asSet("one", "two", "three", "four", "five"),
                Set.of("one", "two", "three", "four", "five"));
        check_nonEmpty(asSet("one", "two", "three", "four", "five", "six"),
                Set.of("one", "two", "three", "four", "five", "six"));
        check_nonEmpty(asSet("one", "two", "three", "four", "five", "six", "seven"),
                Set.of("one", "two", "three", "four", "five", "six", "seven"));
        check_nonEmpty(asSet(1, 2, 3, 4, 5, 6, 7, 8), Set.of(1, 2, 3, 4, 5, 6, 7, 8));
        check_nonEmpty(asSet(1, 2, 3, 4, 5, 6, 7, 8, 9), Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
        check_nonEmpty(asSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        check_nonEmpty(asSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
        check_nonEmpty(asSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        check_nonEmpty(asSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13),
                Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13));
    }

    @Test public void null_examples() {
        assertThrowsNpe(() -> List.<String>of((String) null)); // one-arg version
        assertThrowsNpe(() -> List.<String[]>of((String[]) null)); // null var-args array
        assertThrowsNpe(() -> List.<String>of(new String[] { "one", null, "three"})); // var-args

        assertThrowsNpe(() -> Set.of(null, "two"));
        assertThrowsNpe(() -> Set.of("one", null));
        assertThrowsNpe(
                () -> Set.of(null, "two", "three", "four", "five", "six", "seven"));
        assertThrowsNpe(
                () -> Set.of("one", "two", "three", "four", "five", "six", null));
    }

    @Test public void null_comprehensive() {
        Set<Integer> template = asSet(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        for (int i = 0; i < template.size(); i++) {
            Integer[] l = template.toArray(new Integer[0]);
            l[i] = null;
            npe(i <= 0, () -> Set.of(l[0]));
            npe(i <= 1, () -> Set.of(l[0], l[1]));
            npe(i <= 2, () -> Set.of(l[0], l[1], l[2]));
            npe(i <= 3, () -> Set.of(l[0], l[1], l[2], l[3]));
            npe(i <= 4, () -> Set.of(l[0], l[1], l[2], l[3], l[4]));
            npe(i <= 5, () -> Set.of(l[0], l[1], l[2], l[3], l[4], l[5]));
            npe(i <= 6, () -> Set.of(l[0], l[1], l[2], l[3], l[4], l[5], l[6]));
            npe(i <= 7, () -> Set.of(l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7]));
            npe(i <= 8, () -> Set.of(l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8]));
            npe(i <= 9, () -> Set.of(l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8], l[9]));
            npe(i <= 10, () -> Set.of(
                    l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8], l[9], l[10]));
            npe(i <= 11, () -> Set.of(
                    l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8], l[9], l[10], l[11]));
            npe(i <= 12, () -> Set.of(
                    l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8], l[9], l[10], l[11], l[12])
            );
        }
    }

    /**
     * Asserts that {@code runnable.run()} throws {@link NullPointerException} if
     * any only if {@code expectedToThrowNpe}.
     */
    private static void npe(boolean expectedToThrowNpe, Runnable runnable) {
        if (expectedToThrowNpe) {
            assertThrowsNpe(runnable);
        } else {
            runnable.run(); // should not throw
        }
    }

    private static<T extends Comparable<T>> void check_nonEmpty(Set<T> expected, Set<T> actual) {
        T example = actual.iterator().next();
        check_commonBehavior(expected, actual, example);
    }

    /** Checks assertions that hold for all Set.of() instances. */
    private static<T extends Comparable<T>> void check_commonBehavior(
            Set<T> expected, Set<T> actual, T nonNullExample) {
        assertNotNull(nonNullExample);
        assertEquals(expected, actual);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(actual.isEmpty(), actual.isEmpty());
        assertEquals(actual.size() == 0, actual.isEmpty());
        assertEquals(expected.contains(nonNullExample), actual.contains(nonNullExample));
        assertFalse(actual.contains("absent-element"));
        assertFalse(actual.contains(new Object()));
        assertThrowsNpe(() -> actual.contains(null));

        assertUnmodifiable(actual, nonNullExample);
        assertEquals(actual, reserialize((Serializable) actual));
    }

    private static<T extends Comparable<T>> void assertUnmodifiable(Set<T> set, T example) {
        Set<T> examples = Collections.singleton(example);
        assertTrue(throwsUoe(() -> { set.add(example); } ));
        assertTrue(throwsUoe(() -> { set.addAll(examples); } ));
        // List.of() documentation guarantees that the following operations throw
        // UnsupportedOperationException, even though some other implementations don't
        // do that in the case of isEmpty().
        assertTrue(throwsUoe(() -> { set.clear(); }));
        assertTrue(throwsUoe(() -> { set.remove(example); } ));
        assertTrue(throwsUoe(() -> { set.removeAll(examples); } ));
        assertTrue(throwsUoe(() -> { set.removeIf(Predicate.isEqual(example)); } ));
        assertTrue(throwsUoe(() -> { set.retainAll(examples); } ));
    }

    private static boolean throwsUoe(Runnable runnable) {
        try {
            runnable.run();
            return false;
        } catch (UnsupportedOperationException tolerated) {
            return  true;
        }
    }

    private static void assertThrowsIae(Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    private static void assertThrowsNpe(Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (NullPointerException expected) {
        }
    }

    private static <S extends Serializable> S reserialize(S value) {
        try {
            return (S) SerializationTester.reserialize(value);
        } catch (IOException | ClassNotFoundException e) {
            fail("Unexpected exception: " + e.getMessage());
            throw new AssertionError(e); // unreachable
        }
    }

    private static<T> Set<T> asSet(T... values) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(values)));
    }

}
