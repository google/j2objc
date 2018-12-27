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

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class ListDefaultMethodTester {

    public static void test_replaceAll(List<Integer> l) {
        l.add(5);
        l.add(2);
        l.add(-3);
        l.replaceAll(v -> v * 2);
        assertEquals((Integer)10, l.get(0));
        assertEquals((Integer)4, l.get(1));
        assertEquals((Integer)(-6), l.get(2));

        try {
            l.replaceAll(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public static void test_sort(List<Double> l) {
        l.add(5.0);
        l.add(2.0);
        l.add(-3.0);
        l.sort((v1, v2) -> v1.compareTo(v2));
        assertEquals(-3.0, l.get(0));
        assertEquals(2.0, l.get(1));
        assertEquals(5.0, l.get(2));
    }
}
