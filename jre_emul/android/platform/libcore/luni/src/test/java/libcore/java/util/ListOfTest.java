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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import libcore.libcore.util.SerializationTester;
import libcore.util.NonNull;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ListOfTest {

    @Test
    public void serializationCompatibility_empty() {
        String golden = "ACED00057372001F6A6176612E7574696C2E436F6C6C656374696F6E7324456D7074794C69"
                + "73747AB817B43CA79EDE0200007870";
        new SerializationTester<>(List.<String>of(), golden).test();
    }

    @Test public void serializationCompatibility_oneElement() {
        String golden = "ACED0005737200266A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F646966"
                + "6961626C654C697374FC0F2531B5EC8E100200014C00046C6973747400104C6A6176612F7574696C"
                + "2F4C6973743B7872002C6A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F64696669"
                + "61626C65436F6C6C656374696F6E19420080CB5EF71E0200014C0001637400164C6A6176612F7574"
                + "696C2F436F6C6C656374696F6E3B7870737200136A6176612E7574696C2E41727261794C69737478"
                + "81D21D99C7619D03000149000473697A657870000000017704000000017400036F6E657871007E00"
                + "06";
        new SerializationTester<>(List.of("one"), golden).test();
    }

    @Test public void serializationCompatibility_manyElements() {
        String golden = "ACED0005737200266A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F646966"
                + "6961626C654C697374FC0F2531B5EC8E100200014C00046C6973747400104C6A6176612F7574696C"
                + "2F4C6973743B7872002C6A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F64696669"
                + "61626C65436F6C6C656374696F6E19420080CB5EF71E0200014C0001637400164C6A6176612F7574"
                + "696C2F436F6C6C656374696F6E3B7870737200136A6176612E7574696C2E41727261794C69737478"
                + "81D21D99C7619D03000149000473697A6578700000000D77040000000D737200116A6176612E6C61"
                + "6E672E496E746567657212E2A0A4F781873802000149000576616C7565787200106A6176612E6C61"
                + "6E672E4E756D62657286AC951D0B94E08B02000078700000000C7371007E00070000000B7371007E"
                + "00070000000A7371007E0007000000097371007E0007000000087371007E0007000000077371007E"
                + "0007000000067371007E0007000000057371007E0007000000047371007E0007000000037371007E"
                + "0007000000027371007E0007000000017371007E0007000000007871007E0006";
        new SerializationTester<>(List.of(12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0), golden).test();
    }

    @Test public void mixedTypes() {
        List<?> list = List.of("element", 42);
        assertEquals(asList("element", 42), list);

        assertTrue(list.contains("element"));
        assertTrue(list.contains(42));
        assertFalse(list.contains(new Object()));
        assertFalse(list.contains(31));
    }

    @Test public void duplicates_allowed() {
        // duplicates values are allowed for List.of(), but not for Set.of()
        check_nonEmpty(asList("duplicate", "duplicate"), List.of("duplicate", "duplicate"));
    }

    @Test public void empty() {
        assertBehaviorCommonToAllOfInstances(
                Collections.<String>emptyList(), List.<String>of(), "non-null example String");
    }

    @Test public void nonEmpty() {
        check_nonEmpty(asList("one"), List.of("one"));
        check_nonEmpty(asList("one", "two"), List.of("one", "two"));
        check_nonEmpty(asList("one", "two", "three"), List.of("one", "two", "three"));
        check_nonEmpty(asList("one", "two", "three", "four"),
                List.of("one", "two", "three", "four"));
        check_nonEmpty(asList("one", "two", "three", "four", "five"),
                List.of("one", "two", "three", "four", "five"));
        check_nonEmpty(asList("one", "two", "three", "four", "five", "six"),
                List.of("one", "two", "three", "four", "five", "six"));
        check_nonEmpty(asList("one", "two", "three", "four", "five", "six", "seven"),
                List.of("one", "two", "three", "four", "five", "six", "seven"));
        check_nonEmpty(asList(1, 2, 3, 4, 5, 6, 7, 8), List.of(1, 2, 3, 4, 5, 6, 7, 8));
        check_nonEmpty(asList(1, 2, 3, 4, 5, 6, 7, 8, 9), List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
        check_nonEmpty(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        check_nonEmpty(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
        check_nonEmpty(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        check_nonEmpty(asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13),
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13));
    }

    @Test public void null_examples() {
        assertThrowsNpe(() -> List.<String>of((String) null)); // one-arg version
        assertThrowsNpe(() -> List.<String[]>of((String[]) null)); // null var-args array
        assertThrowsNpe(() -> List.<String>of(new String[] { "one", null, "three"})); // var-args

        assertThrowsNpe(() -> List.of(null, "two"));
        assertThrowsNpe(() -> List.of("one", null));
        assertThrowsNpe(
                () -> List.of(null, "two", "three", "four", "five", "six", "seven"));
        assertThrowsNpe(
                () -> List.of("one", "two", "three", "four", "five", "six", null));
    }

    @Test public void null_comprehensive() {
        List<Integer> template = asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        for (int i = 0; i < template.size(); i++) {
            Integer[] l = template.toArray(new Integer[0]);
            l[i] = null;
            npe(i <= 0, () -> List.of(l[0]));
            npe(i <= 1, () -> List.of(l[0], l[1]));
            npe(i <= 2, () -> List.of(l[0], l[1], l[2]));
            npe(i <= 3, () -> List.of(l[0], l[1], l[2], l[3]));
            npe(i <= 4, () -> List.of(l[0], l[1], l[2], l[3], l[4]));
            npe(i <= 5, () -> List.of(l[0], l[1], l[2], l[3], l[4], l[5]));
            npe(i <= 6, () -> List.of(l[0], l[1], l[2], l[3], l[4], l[5], l[6]));
            npe(i <= 7, () -> List.of(l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7]));
            npe(i <= 8, () -> List.of(l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8]));
            npe(i <= 9, () -> List.of(l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8], l[9]));
            npe(i <= 10, () -> List.of(
                    l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8], l[9], l[10]));
            npe(i <= 11, () -> List.of(
                    l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8], l[9], l[10], l[11]));
            npe(i <= 12, () -> List.of(
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

    private static<T extends Comparable<T>> void check_nonEmpty(List<T> expected, List<T> actual) {
        T example = actual.iterator().next();
        assertBehaviorCommonToAllOfInstances(expected, actual, example);
    }

    /** Checks assertions that hold for all List.of() instances. */
    private static<T extends Comparable<T>> void assertBehaviorCommonToAllOfInstances(
            List<T> expected, List<T> actual, @NonNull T example) {
        assertNotNull(example);
        assertEquals(expected, actual);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(actual.isEmpty(), actual.isEmpty());
        assertEquals(actual.size() == 0, actual.isEmpty());
        SpliteratorTester.runBasicIterationTests(actual.spliterator(), expected);
        if (expected.isEmpty()) {
            assertEquals(expected, actual.subList(0, 0));
        } else {
            assertEquals(expected.subList(1, expected.size()), actual.subList(1, actual.size()));
        }
        assertEquals(expected.contains(example), actual.contains(example));
        assertFalse(actual.contains("absent-element"));
        assertFalse(actual.contains(new Object()));
        assertThrowsNpe(() -> actual.contains(null));
        assertEquals(expected.toString(), actual.toString());

        assertUnmodifiable(actual, example);
        assertEquals(actual, reserialize((Serializable) actual));
        assertTrue(actual instanceof RandomAccess);
    }

    private static<T extends Comparable<T>> void assertUnmodifiable(List<T> list, T example) {
        List<T> examples = Collections.singletonList(example);
        assertThrowsUoe(() -> { list.add(example); } );
        assertThrowsUoe(() -> { list.addAll(examples); } );
        assertThrowsUoe(() -> { list.addAll(0, examples); } );
        // List.of() documentation guarantees that the following operations throw
        // UnsupportedOperationException, even though some other implementations don't
        // do that in the case of isEmpty().
        assertThrowsUoe(() -> { list.clear(); });
        assertThrowsUoe(() -> { list.remove(example); } );
        assertThrowsUoe(() -> { list.removeAll(examples); } );
        assertThrowsUoe(() -> { list.removeIf(Predicate.isEqual(example)); } );
        assertThrowsUoe(() -> { list.replaceAll(UnaryOperator.identity()); } );
        assertThrowsUoe(() -> { list.retainAll(examples); } );
        assertThrowsUoe(() -> { list.sort(Comparator.<T>naturalOrder()); } );
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

    private static <S extends Serializable> S reserialize(S value) {
        try {
            return (S) SerializationTester.reserialize(value);
        } catch (IOException | ClassNotFoundException e) {
            fail("Unexpected exception: " + e.getMessage());
            throw new AssertionError(e); // unreachable
        }
    }
}
