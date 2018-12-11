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

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Tests behavior common to all implementations of {@link Collection#removeIf(Predicate)}.
 */
public class RemoveIfTester {

    private static final Predicate<Integer> isEven = x -> x % 2 == 0;
    private static final Predicate<Integer> isOdd = isEven.negate();

    public static void runBasicRemoveIfTests(Supplier<Collection<Integer>> supp) {
        Collection<Integer> integers = supp.get();
        for (int h = 0; h < 100; ++h) {
            // Insert some ordered integers.
            integers.add(h);
        }

        integers.removeIf(isEven);
        Integer prev = null;
        for (Integer i : integers) {
            assertTrue(i % 2 != 0);
            if (prev != null) {
                assertTrue(prev <= i);
            }
            prev = i;
        }

        integers.removeIf(isOdd);
        assertTrue(integers.isEmpty());
    }

    public static void runBasicRemoveIfTestsUnordered(Supplier<Collection<Integer>> supp) {
        Collection<Integer> integers = supp.get();
        for (int h = 0; h < 100; ++h) {
            // Insert a bunch of arbitrary integers.
            integers.add((h >>> 2) ^ (h >>> 5) ^ (h >>> 11) ^ (h >>> 17));
        }

        integers.removeIf(isEven);
        for (Integer i : integers) {
            assertTrue(i % 2 != 0);
        }

        integers.removeIf(isOdd);
        assertTrue(integers.isEmpty());
    }

    /**
     * Removing from an empty collection should not fail.
     */
    public static void runRemoveIfOnEmpty(Supplier<Collection<Integer>> supp) {
        supp.get().removeIf(x -> {
            fail();
            return false;
        });
    }

    public static void testRemoveIfNPE(Supplier<Collection<Integer>> supp) {
        try {
            supp.get().removeIf(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public static void testRemoveIfCME(Supplier<Collection<Integer>> supp) {
        Collection<Integer> c = supp.get();
        c.add(0);

        try {
            c.removeIf(x -> {c.add(42); return true;});
            fail();
        } catch (ConcurrentModificationException expected) {
        }
    }
}
