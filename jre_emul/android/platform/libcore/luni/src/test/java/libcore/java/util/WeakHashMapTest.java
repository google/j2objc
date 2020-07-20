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

import junit.framework.TestCase;

import java.util.ConcurrentModificationException;
import java.util.WeakHashMap;

public class WeakHashMapTest extends TestCase {

    static Data[] data = new Data[100];

    public void test_replaceAll() {
        initializeData();
        WeakHashMap<Data, String> map = new WeakHashMap<>();
        for(int i = 0; i < data.length; i++) {
            map.put(data[i], "");
        }
        map.replaceAll((k, v) -> k.value);

        for(int i = 0; i < data.length; i++) {
            assertEquals(data[i].value, map.get(data[i]));
        }

        try {
            map.replaceAll(new java.util.function.BiFunction<Data, String, String>() {
                @Override
                public String apply(Data k, String v) {
                    map.put(new Data(), "");
                    return v;
                }
            });
            fail();
        } catch (ConcurrentModificationException expected) {
        }

        try {
            map.replaceAll(null);
            fail();
        } catch (NullPointerException expected) {
        }

        map.clear();
        for(int i = 0; i < data.length; i++) {
            map.put(data[i], data[i].value);
        }

        map.replaceAll((k, v) -> null);

        for(int i = 0; i < data.length; i++) {
            assertNull(map.get(data[i]));
        }
        assertEquals(data.length, map.size());
    }

    private void initializeData() {
        for (int i = 0; i < data.length; i++) {
            data[i] = new Data();
            data[i].value = Integer.toString(i);
        }
    }

    private static class Data {
        public String value = "";
    }
}
