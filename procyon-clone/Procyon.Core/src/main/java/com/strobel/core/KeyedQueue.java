/*
 * KeyedQueue.java
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

package com.strobel.core;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * @author strobelm
 */
public final class KeyedQueue<K, V> {
    private final Map<K, Queue<V>> _data;
    
    public KeyedQueue() {
        _data = new HashMap<>();
    }

    private Queue<V> getQueue(final K key) {
        Queue<V> queue = _data.get(key);
        
        if (queue == null) {
            _data.put(key, (queue = new ArrayDeque<>()));
        }
        
        return queue;
    }

    public boolean add(final K key, final V value) {
        return getQueue(key).add(value);
    }

    public boolean offer(final K key, final V value) {
        return getQueue(key).offer(value);
    }

    public V poll(final K key) {
        return getQueue(key).poll();
    }
    
    public V peek(final K key) {
        return getQueue(key).peek();
    }
    
    public int size(final K key) {
        final Queue<V> queue = _data.get(key);
        return queue != null ? queue.size() : 0;
    }
    
    public void clear() {
        _data.clear();
    }
}
