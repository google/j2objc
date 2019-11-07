/*
 * Cache.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.collections;

import com.strobel.annotations.Nullable;
import com.strobel.core.VerifyArgument;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author strobelm
 */
public abstract class Cache<K, V> {
    protected Cache() {
    }

    /**
     * Gets a value indicating whether a cached value exists for the given key.
     */
    public boolean contains(final K key) {
        return get(key) != null;
    }

    /**
     * Gets a value indicating whether the cached value matches the given value
     * for a specified key.
     */
    public boolean contains(final K key, final V value) {
        final V cachedValue = get(key);
        return cachedValue != null && cachedValue.equals(value);
    }

    /**
     * Returns a thread-specific satellite cache chained to this cache.  If the target
     * cache is already a satellite cache, it will simply return itself.  Note that the
     * returned cache is completely unsynchronized and is not safe for concurrent access.
     *
     * @return A thread-specific satellite cache.
     */
    public abstract Cache<K, V> getSatelliteCache();

    /**
     * <p>
     * Replaces the value associated with a given key if the current value matches the
     * expected value.
     * </p>
     * <p>
     * Note that the replaced value <b>will not be propagated</b> to child caches that
     * already have a value for the same key.  As such, this method is of limited usefulness
     * and should only be called on isolated Level 1 caches.
     * </p>
     *
     * @param key
     *     The key for which to change the associated value.
     * @param expectedValue
     *     The expected value to be replaced.
     * @param updatedValue
     *     The new value.
     *
     * @return {@code true} if the expected value was replaced; otherwise, {@code false}.
     */
    public abstract boolean replace(final K key, @Nullable final V expectedValue, final V updatedValue);

    /**
     * Gets the value associated with the given key.
     *
     * @param key
     *     The key associated with the desired value.
     *
     * @return The value corresponding the given key, or {@code null} if no value was found.
     */
    public abstract V get(final K key);

    /**
     * Places a value in the cache only if no value exists with the same key.
     *
     * @param key
     *     The key associated with the given value.
     * @param value
     *     The value to insert into the cache.
     *
     * @return The cached value associated with the given key, which will be the provided
     *         value if no existing value was found.
     */
    public abstract V cache(final K key, final V value);

    /**
     * Creates a concurrency-safe Level 1 cache that may be used in isolation or as
     * the root cache in a multi-level cache design.
     *
     * @param <K>
     *     The type of keys used to identify values in the cache.
     * @param <V>
     *     The type of values stored in the cache.
     *
     * @return The newly created cache.
     */
    public static <K, V> Cache<K, V> createTopLevelCache() {
        return new TopLevelCache<>();
    }

    /**
     * Creates an unsynchronized, concurrency-unsafe Level 1 cache that can only be
     * used safely by a single thread.
     *
     * @param <K>
     *     The type of keys used to identify values in the cache.
     * @param <V>
     *     The type of values stored in the cache.
     *
     * @return The newly created cache.
     */
    public static <K, V> Cache<K, V> createSatelliteCache() {
        return new SatelliteCache<>();
    }

    /**
     * Creates an unsynchronized, concurrency-unsafe Level 2 cache that can only be
     * used safely by a single thread.  On a cache miss, the parent cache will be
     * checked.  On an insert, the value will propagate up to the parent cache.
     *
     * @param <K>
     *     The type of keys used to identify values in the cache.
     * @param <V>
     *     The type of values stored in the cache.
     *
     * @return The newly created cache.
     */
    public static <K, V> Cache<K, V> createSatelliteCache(final Cache<K, V> parent) {
        return new SatelliteCache<>(VerifyArgument.notNull(parent, "parent"));
    }

    /**
     * Creates an unsynchronized, concurrency-unsafe Level 1 cache that can only be
     * used safely by a single thread.  Keys are compared by reference identity.
     *
     * @param <K>
     *     The type of keys used to identify values in the cache.
     * @param <V>
     *     The type of values stored in the cache.
     *
     * @return The newly created cache.
     */
    public static <K, V> Cache<K, V> createSatelliteIdentityCache() {
        return new SatelliteCache<>();
    }

    /**
     * Creates an unsynchronized, concurrency-unsafe Level 2 cache that can only be
     * used safely by a single thread.  On a cache miss, the parent cache will be
     * checked.  On an insert, the value will propagate up to the parent cache.
     * Keys are compared by reference identity.
     *
     * @param <K>
     *     The type of keys used to identify values in the cache.
     * @param <V>
     *     The type of values stored in the cache.
     *
     * @return The newly created cache.
     */
    public static <K, V> Cache<K, V> createSatelliteIdentityCache(final Cache<K, V> parent) {
        return new SatelliteCache<>(VerifyArgument.notNull(parent, "parent"));
    }

    /**
     * Creates a Level 1 cache that internally maintains a separate satellite cache
     * for each thread that accesses it.
     *
     * @param <K>
     *     The type of keys used to identify values in the cache.
     * @param <V>
     *     The type of values stored in the cache.
     *
     * @return The newly created cache.
     */
    public static <K, V> Cache<K, V> createThreadLocalCache() {
        return new ThreadLocalCache<>();
    }

    /**
     * Creates a Level 1 cache that internally maintains a separate satellite cache
     * for each thread that accesses it.  Keys are compared by reference identity.
     *
     * @param <K>
     *     The type of keys used to identify values in the cache.
     * @param <V>
     *     The type of values stored in the cache.
     *
     * @return The newly created cache.
     */
    public static <K, V> Cache<K, V> createThreadLocalIdentityCache() {
        return new ThreadLocalCache<>();
    }

    /**
     * Creates a Level 2 cache that internally maintains a separate satellite cache
     * for each thread that accesses it.  On a cache miss, the parent cache will be
     * checked.  On an insert, the value will propagate up to the parent cache.
     *
     * @param <K>
     *     The type of keys used to identify values in the cache.
     * @param <V>
     *     The type of values stored in the cache.
     *
     * @return The newly created cache.
     */
    public static <K, V> Cache<K, V> createThreadLocalCache(final Cache<K, V> parent) {
        return new ThreadLocalCache<>(VerifyArgument.notNull(parent, "parent"));
    }

    /**
     * Creates a Level 2 cache that internally maintains a separate satellite cache
     * for each thread that accesses it.  On a cache miss, the parent cache will be
     * checked.  On an insert, the value will propagate up to the parent cache.
     * Keys are compared by reference identity.
     *
     * @param <K>
     *     The type of keys used to identify values in the cache.
     * @param <V>
     *     The type of values stored in the cache.
     *
     * @return The newly created cache.
     */
    public static <K, V> Cache<K, V> createThreadLocalIdentityCache(final Cache<K, V> parent) {
        return new ThreadLocalIdentityCache<>(VerifyArgument.notNull(parent, "parent"));
    }
}

final class TopLevelCache<K, V> extends Cache<K, V> {
    private final ConcurrentHashMap<K, V> _cache = new ConcurrentHashMap<>();

    @Override
    public V cache(final K key, final V value) {
        final V cachedValue = _cache.putIfAbsent(key, value);
        return cachedValue != null ? cachedValue : value;
    }

    @Override
    public Cache<K, V> getSatelliteCache() {
        return createSatelliteCache(this);
    }

    @Override
    public boolean replace(final K key, final V expectedValue, final V updatedValue) {
        if (expectedValue == null) {
            return _cache.putIfAbsent(key, updatedValue) == null;
        }
        return _cache.replace(key, expectedValue, updatedValue);
    }

    @Override
    public V get(final K key) {
        return _cache.get(key);
    }
}

final class SatelliteCache<K, V> extends Cache<K, V> {
    private final Cache<K, V> _parent;
    private final HashMap<K, V> _cache = new HashMap<>();

    public SatelliteCache() {
        _parent = null;
    }

    @Override
    public Cache<K, V> getSatelliteCache() {
        return this;
    }

    @Override
    public boolean replace(final K key, final V expectedValue, final V updatedValue) {
        if (_parent != null && !_parent.replace(key, expectedValue, updatedValue)) {
            return false;
        }
        _cache.put(key, updatedValue);
        return true;
    }

    public SatelliteCache(final Cache<K, V> parent) {
        _parent = parent;
    }

    @Override
    public V cache(final K key, final V value) {
        V cachedValue = _cache.get(key);

        if (cachedValue != null) {
            return cachedValue;
        }

        if (_parent != null) {
            cachedValue = _parent.cache(key, value);
        }
        else {
            cachedValue = value;
        }

        _cache.put(key, cachedValue);

        return cachedValue;
    }

    @Override
    public V get(final K key) {
        V cachedValue = _cache.get(key);

        if (cachedValue != null) {
            return cachedValue;
        }

        if (_parent != null) {
            cachedValue = _parent.get(key);

            if (cachedValue != null) {
                _cache.put(key, cachedValue);
            }
        }

        return cachedValue;
    }
}

final class ThreadLocalCache<K, V> extends Cache<K, V> {
    private final Cache<K, V> _parent;

    @SuppressWarnings("ThreadLocalNotStaticFinal")
    private final ThreadLocal<SatelliteCache<K, V>> _threadCaches = new ThreadLocal<SatelliteCache<K, V>>() {
        @Override
        protected SatelliteCache<K, V> initialValue() {
            return new SatelliteCache<>(_parent);
        }
    };

    public ThreadLocalCache() {
        _parent = null;
    }

    @Override
    public Cache<K, V> getSatelliteCache() {
        return _threadCaches.get();
    }

    @Override
    public boolean replace(final K key, final V expectedValue, final V updatedValue) {
        return _threadCaches.get().replace(key, expectedValue, updatedValue);
    }

    public ThreadLocalCache(final Cache<K, V> parent) {
        _parent = parent;
    }

    @Override
    public V cache(final K key, final V value) {
        return _threadCaches.get().cache(key, value);
    }

    @Override
    public V get(final K key) {
        return _threadCaches.get().get(key);
    }
}

final class ThreadLocalIdentityCache<K, V> extends Cache<K, V> {
    private final Cache<K, V> _parent;

    @SuppressWarnings("ThreadLocalNotStaticFinal")
    private final ThreadLocal<SatelliteCache<K, V>> _threadCaches = new ThreadLocal<SatelliteCache<K, V>>() {
        @Override
        protected SatelliteCache<K, V> initialValue() {
            return new SatelliteCache<>(_parent);
        }
    };

    public ThreadLocalIdentityCache() {
        _parent = null;
    }

    @Override
    public Cache<K, V> getSatelliteCache() {
        return _threadCaches.get();
    }

    @Override
    public boolean replace(final K key, final V expectedValue, final V updatedValue) {
        return _threadCaches.get().replace(key, expectedValue, updatedValue);
    }

    public ThreadLocalIdentityCache(final Cache<K, V> parent) {
        _parent = parent;
    }

    @Override
    public V cache(final K key, final V value) {
        return _threadCaches.get().cache(key, value);
    }

    @Override
    public V get(final K key) {
        return _threadCaches.get().get(key);
    }
}