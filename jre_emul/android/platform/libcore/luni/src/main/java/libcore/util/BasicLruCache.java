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

package libcore.util;

import dalvik.annotation.compat.UnsupportedAppUsage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A minimal least-recently-used cache for libcore. Prefer {@code
 * android.util.LruCache} where that is available.
 * @hide
 */
public class BasicLruCache<K, V> {
    @UnsupportedAppUsage
    private final LinkedHashMap<K, V> map;
    private final int maxSize;

    @UnsupportedAppUsage
    public BasicLruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
    }

    /**
     * Returns the value for {@code key} if it exists in the cache or can be
     * created by {@code #create}. If a value was returned, it is moved to the
     * head of the queue. This returns null if a value is not cached and cannot
     * be created.
     */
    @UnsupportedAppUsage
    public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V result;
        synchronized (this) {
            result = map.get(key);
            if (result != null) {
                return result;
            }
        }

        // Don't hold any locks while calling create.
        result = create(key);

        synchronized (this) {
            // NOTE: Another thread might have already inserted a value for |key| into the map.
            // This shouldn't be an observable change as long as create creates equal values for
            // equal keys. We will however attempt to trim the map twice, but that shouldn't be
            // a big deal since uses of this class aren't heavily contended (and this class
            // isn't design for such usage anyway).
            if (result != null) {
                map.put(key, result);
                trimToSize(maxSize);
            }
        }

        return result;
    }

    /**
     * Caches {@code value} for {@code key}. The value is moved to the head of
     * the queue.
     *
     * @return the previous value mapped by {@code key}. Although that entry is
     *     no longer cached, it has not been passed to {@link #entryEvicted}.
     */
    @UnsupportedAppUsage
    public synchronized final V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key == null");
        } else if (value == null) {
            throw new NullPointerException("value == null");
        }

        V previous = map.put(key, value);
        trimToSize(maxSize);
        return previous;
    }

    private void trimToSize(int maxSize) {
        while (map.size() > maxSize) {
            Map.Entry<K, V> toEvict = map.eldest();

            K key = toEvict.getKey();
            V value = toEvict.getValue();
            map.remove(key);

            entryEvicted(key, value);
        }
    }

    /**
     * Called for entries that have reached the tail of the least recently used
     * queue and are be removed. The default implementation does nothing.
     */
    protected void entryEvicted(K key, V value) {}

    /**
     * Called after a cache miss to compute a value for the corresponding key.
     * Returns the computed value or null if no value can be computed. The
     * default implementation returns null.
     */
    protected V create(K key) {
        return null;
    }

    /**
     * Returns a copy of the current contents of the cache, ordered from least
     * recently accessed to most recently accessed.
     */
    public synchronized final Map<K, V> snapshot() {
        return new LinkedHashMap<K, V>(map);
    }

    /**
     * Clear the cache, calling {@link #entryEvicted} on each removed entry.
     */
    @UnsupportedAppUsage
    public synchronized final void evictAll() {
        trimToSize(0);
    }
}
