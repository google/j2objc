/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.libcore.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

import libcore.util.BasicLruCache;

public final class BasicLruCacheTest extends TestCase {

    public void testCreateOnCacheMiss() {
        BasicLruCache<String, String> cache = newCreatingCache();
        String created = cache.get("aa");
        assertEquals("created-aa", created);
    }

    public void testNoCreateOnCacheHit() {
        BasicLruCache<String, String> cache = newCreatingCache();
        cache.put("aa", "put-aa");
        assertEquals("put-aa", cache.get("aa"));
    }

    public void testConstructorDoesNotAllowZeroCacheSize() {
        try {
            new BasicLruCache<String, String>(0);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testCannotPutNullKey() {
        BasicLruCache<String, String> cache = new BasicLruCache<String, String>(3);
        try {
            cache.put(null, "A");
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testCannotPutNullValue() {
        BasicLruCache<String, String> cache = new BasicLruCache<String, String>(3);
        try {
            cache.put("a", null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testEvictionWithSingletonCache() {
        BasicLruCache<String, String> cache = new BasicLruCache<String, String>(1);
        cache.put("a", "A");
        cache.put("b", "B");
        assertSnapshot(cache, "b", "B");
    }

    public void testEntryEvictedWhenFull() {
        List<String> expectedEvictionLog = new ArrayList<String>();
        final List<String> evictionLog = new ArrayList<String>();
        BasicLruCache<String, String> cache = new BasicLruCache<String, String>(3) {
            @Override protected void entryEvicted(String key, String value) {
                evictionLog.add(key + "=" + value);
            }
        };

        cache.put("a", "A");
        cache.put("b", "B");
        cache.put("c", "C");
        assertEquals(expectedEvictionLog, evictionLog);

        cache.put("d", "D");
        expectedEvictionLog.add("a=A");
        assertEquals(expectedEvictionLog, evictionLog);
    }

    /**
     * Replacing the value for a key doesn't cause an eviction but it does bring
     * the replaced entry to the front of the queue.
     */
    public void testPutDoesNotCauseEviction() {
        final List<String> evictionLog = new ArrayList<String>();
        List<String> expectedEvictionLog = new ArrayList<String>();
        BasicLruCache<String, String> cache = new BasicLruCache<String, String>(3) {
            @Override protected void entryEvicted(String key, String value) {
                evictionLog.add(key + "=" + value);
            }
        };

        cache.put("a", "A");
        cache.put("b", "B");
        cache.put("c", "C");
        cache.put("b", "B2");
        assertEquals(expectedEvictionLog, evictionLog);
        assertSnapshot(cache, "a", "A", "c", "C", "b", "B2");
    }

    public void testEvictAll() {
        final List<String> evictionLog = new ArrayList<String>();
        BasicLruCache<String, String> cache = new BasicLruCache<String, String>(10) {
            @Override protected void entryEvicted(String key, String value) {
                evictionLog.add(key + "=" + value);
            }
        };

        cache.put("a", "A");
        cache.put("b", "B");
        cache.put("c", "C");
        cache.evictAll();
        assertSnapshot(cache);
        assertEquals(Arrays.asList("a=A", "b=B", "c=C"), evictionLog);
    }

    private BasicLruCache<String, String> newCreatingCache() {
        return new BasicLruCache<String, String>(3) {
            @Override protected String create(String key) {
                return (key.length() > 1) ? ("created-" + key) : null;
            }
        };
    }

    private <T> void assertSnapshot(BasicLruCache<T, T> cache, T... keysAndValues) {
        List<T> actualKeysAndValues = new ArrayList<T>();
        for (Map.Entry<T, T> entry : cache.snapshot().entrySet()) {
            actualKeysAndValues.add(entry.getKey());
            actualKeysAndValues.add(entry.getValue());
        }

        // assert using lists because order is important for LRUs
        assertEquals(Arrays.asList(keysAndValues), actualKeysAndValues);
    }
}
