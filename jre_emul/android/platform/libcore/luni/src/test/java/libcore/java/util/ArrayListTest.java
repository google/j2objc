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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayListTest extends junit.framework.TestCase {
    public void test_replaceAll() {
        ListDefaultMethodTester.test_replaceAll(new ArrayList<>());
    }

    public void test_sort() {
        ListDefaultMethodTester.test_sort(new ArrayList<>());
    }

    public void test_sublist_throws() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("zero", "one", "two"));

        // For comparison, a few cases that should not throw
        list.subList(1, 2);
        list.subList(0, 3);
        list.subList(1, 1);

        // Cases that should throw
        try {
            list.subList(-1, 1); // fromIndex out of bounds
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            list.subList(0, 4); // toIndex out of bounds
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            list.subList(-1, 4); // both fromIndex and toIndex out of bounds
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            list.subList(1, 0); // fromIndex > toIndex
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_sublist_set() {
        List<String> list = new ArrayList<>(Arrays.asList("zero", "one", "two"));
        list.subList(1, 2).set(0, "ONE");
        assertEquals(Arrays.asList("zero", "ONE", "two"), list);

        list.subList(1, 2).subList(0, 1).set(0, "1");
        assertEquals(Arrays.asList("zero", "1", "two"), list);

        try {
            list.subList(1, 2).set(2, "out of bounds");
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }
}
