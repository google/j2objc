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
 * limitations under the License.
 */

package libcore.java.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

public final class ComparatorTest extends TestCase {
    private static final Item ZERO = new Item(0);
    private static final Item ONE = new Item(1);
    private static final Item TWO = new Item(2);
    private static final Item THREE = new Item(3);
    private static final Item FOUR = new Item(4);

    private static final Item ZEROZERO = new Item(0, 0);
    private static final Item ZEROONE = new Item(0, 1);
    private static final Item ONEZERO = new Item(1, 0);
    private static final Item ONEONE = new Item(1, 1);

    private static final Item[] orderedItems = new Item[]{ZERO, ONE, TWO, THREE, FOUR};
    private static final Item[] nullsFirstItems = new Item[]{null, ZERO, ONE, TWO, THREE, FOUR};
    private static final Item[] nullsLastItems = new Item[]{ZERO, ONE, TWO, THREE, FOUR, null};
    private static final Item[] orderedItemsMatrix = new Item[]{ZEROZERO, ZEROONE, ONEZERO, ONEONE};

    private <T> void checkComparison(Comparator<T> comparator, T[] items) {
        for (int i = 0; i < items.length - 1; ++i) {
            assertEquals(0, comparator.compare(items[i], items[i]));
            for (int j = i + 1; j < items.length; ++j) {
                assertTrue(comparator.compare(items[i], items[j]) < 0);
                assertTrue(comparator.compare(items[j], items[i]) > 0);
            }
        }
    }

    public void testComparingDouble() {
        Comparator<Item> comparator = Comparator.comparingDouble(Item::getOrderAsDouble);
        checkComparison(comparator, orderedItems);
    }

    public void testComparingInt() {
        Comparator<Item> comparator = Comparator.comparingInt(Item::getOrderAsInt);
        checkComparison(comparator, orderedItems);
    }

    public void testComparingLong() {
        Comparator<Item> comparator = Comparator.comparingLong(Item::getOrderAsLong);
        checkComparison(comparator, orderedItems);
    }

    public void testComparing() {
        Comparator<Item> comparator = Comparator.comparing(Item::getOrderAsString);
        checkComparison(comparator, orderedItems);
    }

    public void testComparing2() {
        Comparator<Item> comparator = Comparator.comparing(Item::getOrderAsString,
                String.CASE_INSENSITIVE_ORDER);
        checkComparison(comparator, orderedItems);
    }

    public void testNaturalOrder() {
        Comparator<Item> comparator = Comparator.naturalOrder();
        checkComparison(comparator, orderedItems);
        checkComparison(comparator, orderedItemsMatrix);
    }

    // J2ObjC: reverse a copy of orderedItems to avoid interfering with other tests (b/139015474).
    public void testReverseOrder() {
        List<Item> itemsList = new ArrayList<Item>(Arrays.asList(orderedItems));
        Collections.reverse(itemsList);
        Comparator<Item> comparator = Comparator.reverseOrder();
        checkComparison(comparator, itemsList.toArray(new Item[0]));
    }

    public void testReverse() {
        Comparator<Item> revComparator = Comparator.reverseOrder();
        Comparator<Item> revRevComparator = revComparator.reversed();
        checkComparison(revRevComparator, orderedItems);
        checkComparison(revRevComparator, orderedItemsMatrix);
    }

    public void testReverse2() {
        Comparator<Item> comparator = Comparator.naturalOrder();
        assertSame(comparator, comparator.reversed().reversed());
    }

    public void testReverse3() {
        Comparator<Item> comparator = Comparator.comparing(Item::getOrderAsString);
        assertSame(comparator, comparator.reversed().reversed());
    }

    public void testNullsFirst() {
        Comparator<Item> comparator = Comparator.nullsFirst(Comparator.naturalOrder());
        checkComparison(comparator, nullsFirstItems);
    }

    public void testNullsLast() {
        Comparator<Item> comparator = Comparator.nullsLast(Comparator.naturalOrder());
        checkComparison(comparator, nullsLastItems);
    }

    public void testThenComparingDouble() {
        Comparator<Item> comparator = Comparator.comparingDouble(Item::getOrderAsDouble)
                .thenComparingDouble(Item::getSecondaryOrderAsDouble);
        checkComparison(comparator, orderedItemsMatrix);
    }

    public void testThenComparingInt() {
        Comparator<Item> comparator = Comparator.comparingInt(Item::getOrderAsInt)
                .thenComparingInt(Item::getSecondaryOrderAsInt);
        checkComparison(comparator, orderedItemsMatrix);
    }

    public void testThenComparingLong() {
        Comparator<Item> comparator = Comparator.comparingLong(Item::getOrderAsLong)
                .thenComparingLong(Item::getSecondaryOrderAsLong);
        checkComparison(comparator, orderedItemsMatrix);
    }

    public void testThenComparing() {
        Comparator<Item> comparator = Comparator.comparing(Item::getOrderAsString)
                .thenComparing(Item::getSecondaryOrderAsString);
        checkComparison(comparator, orderedItemsMatrix);
    }

    private static final class Item implements Comparable<Item> {
        private final int primaryOrder;
        private final int secondaryOrder;

        public Item(int primaryOrder) {
            this(primaryOrder, 0);
        }

        public Item(int primaryOrder, int secondaryOrder) {
            this.primaryOrder = primaryOrder;
            this.secondaryOrder = secondaryOrder;
        }

        public Integer getOrderAsInt() {
            return primaryOrder;
        }

        public Long getOrderAsLong() {
            return Long.valueOf(primaryOrder);
        }

        public Double getOrderAsDouble() {
            return Double.valueOf(primaryOrder);
        }

        public String getOrderAsString() {
            return String.valueOf(primaryOrder);
        }

        public Integer getSecondaryOrderAsInt() {
            return secondaryOrder;
        }

        public Long getSecondaryOrderAsLong() {
            return Long.valueOf(secondaryOrder);
        }

        public Double getSecondaryOrderAsDouble() {
            return Double.valueOf(secondaryOrder);
        }

        public String getSecondaryOrderAsString() {
            return String.valueOf(secondaryOrder);
        }

        @Override
        public int compareTo(Item other) {
            return primaryOrder - other.getOrderAsInt() != 0 ?
                    primaryOrder - other.getOrderAsInt() :
                    secondaryOrder - other.getSecondaryOrderAsInt();
        }
    }

}
